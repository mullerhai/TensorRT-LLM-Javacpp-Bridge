#!/usr/bin/env python3
"""Remove bogus `import` lines that reference classes which don't exist in the
workspace (typical artefact from the legacy `src/main/resources` tree which
used the old `org.bytedeco.tensorrt_llm.*` package)."""
import os
import re
import sys

ROOT = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(ROOT, "src", "main", "java")

IMPORT_RE = re.compile(r"(?m)^import\s+([\w.]+)\s*;\s*\n")


def main():
    # Index all declared FQNs.
    declared: set[str] = set()
    for root, _dirs, files in os.walk(SRC):
        for name in files:
            if not name.endswith(".java") or not name[0].isupper():
                continue
            with open(os.path.join(root, name), "r", encoding="utf-8") as fp:
                text = fp.read()
            m = re.search(r"^package\s+([\w.]+)\s*;", text, re.MULTILINE)
            if m:
                declared.add(f"{m.group(1)}.{name[:-5]}")

    # Well-known external packages whose classes we cannot enumerate but are
    # valid at runtime (javacpp + stdlib + bytedeco).
    EXTERNAL_PREFIXES = (
        "java.",
        "javax.",
        "org.bytedeco.javacpp.",
        # Do NOT whitelist `org.bytedeco.tensorrt_llm.*` – that namespace is
        # what the legacy files use and does not exist.
    )

    dropped = 0
    edited = 0
    for root, _dirs, files in os.walk(SRC):
        for name in files:
            if not name.endswith(".java"):
                continue
            path = os.path.join(root, name)
            with open(path, "r", encoding="utf-8") as fp:
                text = fp.read()
            new_text, n = IMPORT_RE.subn(
                lambda m: m.group(0) if _is_valid(m.group(1), declared, EXTERNAL_PREFIXES) else "",
                text,
            )
            # IMPORT_RE.subn reports n as total matches; count real removals
            # by diffing length instead.
            if new_text != text:
                with open(path, "w", encoding="utf-8") as fp:
                    fp.write(new_text)
                edited += 1
                dropped += text.count("\nimport ") - new_text.count("\nimport ")
    print(f"dropped {dropped} bogus imports across {edited} files")


def _is_valid(fqn: str, declared: set[str], externals: tuple[str, ...]) -> bool:
    if fqn.endswith(".*"):
        return True
    if fqn in declared:
        return True
    for p in externals:
        if fqn.startswith(p):
            return True
    return False


if __name__ == "__main__":
    sys.exit(main() or 0)

