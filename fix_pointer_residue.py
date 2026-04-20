#!/usr/bin/env python3
"""Strip additional leaked `Pointer` fragments that JavaCPP produced when
InfoMap typedef substitution corrupted non-cast annotations:

* ``@Cast("const Pointere")``  — mangled substitution of an enum's
  underlying type.  The enclosing method already uses a Java primitive
  (int/byte/long) which matches the native ABI, so we drop the cast
  annotation entirely.
* ``@Cast("const Pointer**")`` / ``@Cast("const Pointer*const*")`` —
  pointer-to-pointer leaks (mostly token-id arrays in kernels).  Rewrite
  the leaked ``Pointer`` to ``tensorrt_llm::runtime::TokenIdType`` which
  matches the upstream C++ signatures.
* ``@ByVal(nullValue = "OptionalRef<Pointer(Pointer)") Pointer x`` — the
  mangled C++ default-value expression is unusable, so we drop the
  ``nullValue`` attribute and mark the parameter ``@Optional``.
"""
import os
import re

ROOT = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(ROOT, "src", "main", "java", "tensorrt_llm")

# Patterns to apply in sequence.  Each entry is (regex, replacement).
PATTERNS = [
    # Drop trailing `@Cast("const Pointere")` annotations; the associated Java
    # primitive already carries the correct ABI.  Works both when the cast is
    # followed by other annotations and when it is the sole annotation.
    (re.compile(r'@Cast\("const Pointere"\)\s*'), ''),
    # Rewrite leaked pointer-to-pointer enum/TokenIdType casts.
    (re.compile(r'@Cast\("const Pointer\*\*"\)'),
     '@Cast("const tensorrt_llm::runtime::TokenIdType**")'),
    (re.compile(r'@Cast\("const Pointer\*const\*"\)'),
     '@Cast("const tensorrt_llm::runtime::TokenIdType*const*")'),
    # Drop the garbled OptionalRef<Pointer(Pointer) default expression so the
    # parameter falls back to a plain `@Optional` Java null.  Retain any
    # leading whitespace the annotation was attached to.
    (re.compile(r'@ByVal\(nullValue\s*=\s*"OptionalRef<[^"]*"\)'),
     '@ByVal @Optional'),
    # Also drop the mangled `nullValue = "Pointer(Pointer)"` / similar
    # nested-Pointer default expressions.
    (re.compile(r'@ByVal\(nullValue\s*=\s*"[^"]*\bPointer\([^"]*"\)'),
     '@ByVal @Optional'),
    # Fix `nullValue = "tensorrt_llm::executor::Pointer{}"` default ctor — the
    # typedef substituted was actually `Shape` (a small dim wrapper).
    (re.compile(r'nullValue\s*=\s*"tensorrt_llm::executor::Pointer\{\}"'),
     'nullValue = "tensorrt_llm::executor::Shape{}"'),
]


def main() -> None:
    changed_files = 0
    total_subs = 0
    for root, _dirs, files in os.walk(SRC):
        for name in files:
            if not name.endswith('.java'):
                continue
            path = os.path.join(root, name)
            with open(path, 'r', encoding='utf-8') as fp:
                original = fp.read()
            updated = original
            file_subs = 0
            for pattern, repl in PATTERNS:
                updated, n = pattern.subn(repl, updated)
                file_subs += n
            if updated != original:
                changed_files += 1
                total_subs += file_subs
                with open(path, 'w', encoding='utf-8') as fp:
                    fp.write(updated)
    print("rewrote %d leaked Pointer fragments across %d files"
          % (total_subs, changed_files))


if __name__ == "__main__":
    main()



