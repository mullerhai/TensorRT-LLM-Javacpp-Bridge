#!/usr/bin/env python3
"""Promote the ``RequestVector`` and ``PeftTable`` C++ typedefs from opaque
``Pointer`` with ``@Cast(...)`` into their real Java wrapper classes:

    @Cast("const tensorrt_llm::batch_manager::RequestVector*") @ByRef Pointer x
        -->  @Const @ByRef RequestVector x

    @Cast("tensorrt_llm::batch_manager::RequestVector*") @ByRef Pointer x
        -->  @ByRef RequestVector x

    @ByVal @Cast("tensorrt_llm::batch_manager::BasePeftCacheManager::PeftTable*")
    Pointer method(...)     -->  @ByVal PeftTable method(...)

    @ByRef @Cast("tensorrt_llm::batch_manager::RequestVector*") Pointer field()
        -->  @ByRef RequestVector field()

The same substitution is applied whether the cast uses
``BasePeftCacheManager::`` / ``PeftCacheManager::`` / ``LoraManager::`` /
``PauseRequests::`` as a containing scope, because all those typedefs resolve
to the same C++ type.

After rewriting, we add an ``import tensorrt_llm.batch_manager.RequestVector``
and / or ``.PeftTable`` statement to every touched Java file that does not
already live in the ``tensorrt_llm.batch_manager`` package.
"""
import os
import re
import sys

ROOT = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(ROOT, "src", "main", "java", "tensorrt_llm")

# Fully qualified cast strings that identify each wrapper type.  All variants
# collapse to the same Java class.
REQUEST_VECTOR_TYPES = (
    "tensorrt_llm::batch_manager::RequestVector",
    "tensorrt_llm::batch_manager::PauseRequests::RequestVector",
)
PEFT_TABLE_TYPES = (
    "tensorrt_llm::batch_manager::BasePeftCacheManager::PeftTable",
    "tensorrt_llm::batch_manager::PeftCacheManager::PeftTable",
    "tensorrt_llm::runtime::LoraManager::PeftTable",
)


def _cast_alt(types):
    return "(?:" + "|".join(re.escape(t) for t in types) + ")"


RV = _cast_alt(REQUEST_VECTOR_TYPES)
PT = _cast_alt(PEFT_TABLE_TYPES)


# Ordered list of (regex, replacement).  Order matters because some patterns
# are strict refinements of later ones.
PATTERNS = [
    # ---- RequestVector ----------------------------------------------------
    # const ref parameter
    (re.compile(rf'@Cast\("const {RV}\*"\)\s*@ByRef\s+Pointer\b'),
     '@Const @ByRef RequestVector'),
    # mutable ref parameter
    (re.compile(rf'@Cast\("{RV}\*"\)\s*@ByRef\s+Pointer\b'),
     '@ByRef RequestVector'),
    # MemberGetter style: @ByRef @Cast("...RequestVector*") Pointer foo()
    (re.compile(rf'@ByRef\s+@Cast\("{RV}\*"\)\s*Pointer\b'),
     '@ByRef RequestVector'),

    # ---- PeftTable --------------------------------------------------------
    # @ByVal return: @ByVal @Cast("...PeftTable*") Pointer m(
    (re.compile(rf'@ByVal\s+@Cast\("{PT}\*"\)\s*Pointer\b'),
     '@ByVal PeftTable'),
    # @Const @ByRef param / const ref parameter
    (re.compile(rf'@Cast\("const {PT}\*"\)\s*@ByRef\s+Pointer\b'),
     '@Const @ByRef PeftTable'),
    # mutable ref parameter
    (re.compile(rf'@Cast\("{PT}\*"\)\s*@ByRef\s+Pointer\b'),
     '@ByRef PeftTable'),
]


def ensure_import(text: str, file_path: str, simple_name: str, fqn: str) -> str:
    """Add an ``import`` line for *fqn* unless the file is already in the
    same package or already imports it."""
    rel = os.path.relpath(file_path, SRC).replace(os.sep, "/")
    pkg_dir = os.path.dirname(rel)
    target_pkg = ".".join(fqn.split(".")[:-1])
    if pkg_dir.replace("/", ".") == target_pkg:
        return text
    if re.search(rf'(?m)^\s*import\s+{re.escape(fqn)}\s*;', text):
        return text
    # Insert after the package declaration.
    return re.sub(
        r'(?m)(^package\s+[^;]+;\s*\n)',
        rf'\1\nimport {fqn};\n',
        text,
        count=1,
    )


def main() -> int:
    changed_files = 0
    total_subs = 0
    per_file_log = []
    for root, _dirs, files in os.walk(SRC):
        for name in files:
            if not name.endswith(".java"):
                continue
            path = os.path.join(root, name)
            with open(path, "r", encoding="utf-8") as fp:
                original = fp.read()

            updated = original
            file_subs = 0
            for pattern, repl in PATTERNS:
                updated, n = pattern.subn(repl, updated)
                file_subs += n

            if file_subs == 0:
                continue

            # Detect whether the rewrite introduced the wrapper class name as
            # a Java *token* (not inside a quoted C++ cast string).  We strip
            # all double-quoted string literals before looking for it.
            stripped_updated = re.sub(r'"[^"]*"', '', updated)
            stripped_original = re.sub(r'"[^"]*"', '', original)
            if ("RequestVector" in stripped_updated
                    and "RequestVector" not in stripped_original):
                updated = ensure_import(
                    updated, path, "RequestVector",
                    "tensorrt_llm.batch_manager.RequestVector",
                )
            if ("PeftTable" in stripped_updated
                    and "PeftTable" not in stripped_original):
                updated = ensure_import(
                    updated, path, "PeftTable",
                    "tensorrt_llm.batch_manager.PeftTable",
                )

            with open(path, "w", encoding="utf-8") as fp:
                fp.write(updated)
            changed_files += 1
            total_subs += file_subs
            per_file_log.append((os.path.relpath(path, SRC), file_subs))

    print(f"rewrote {total_subs} signatures across {changed_files} files")
    for rel, n in per_file_log:
        print(f"  {rel}: {n}")
    return 0


if __name__ == "__main__":
    sys.exit(main())


