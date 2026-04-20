#!/usr/bin/env python3
"""Scan `src/main/java/tensorrt_llm/**/*.java`, build a map of simple class
name -> fully-qualified package, then for every file add the missing
`import` statements for unresolved simple class names that appear as type
tokens in method signatures / annotations.

This is a safety net for resource-tree Java files that were copied into the
source tree without the cross-package imports JavaCPP would normally emit.
"""
from __future__ import annotations

import os
import re
import sys
from collections import defaultdict

ROOT = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(ROOT, "src", "main", "java")

# Tokens we never want to treat as a class name to import.
KEYWORDS = {
    "public", "private", "protected", "static", "final", "native", "void",
    "boolean", "int", "long", "float", "double", "char", "byte", "short",
    "class", "interface", "extends", "implements", "import", "package",
    "return", "new", "this", "super", "null", "true", "false", "if",
    "else", "for", "while", "try", "catch", "throw", "throws", "switch",
    "case", "default", "break", "continue", "abstract", "synchronized",
    "volatile", "transient", "enum", "instanceof", "do",
    "String", "Object", "Override", "Deprecated", "Integer", "Long",
    "Float", "Double", "Boolean", "Byte", "Short", "Character", "Number",
    "Math", "System", "Exception", "RuntimeException", "Throwable", "Class",
    # Bytedeco top-levels - always wildcard imported already.
    "Pointer", "BytePointer", "IntPointer", "LongPointer", "FloatPointer",
    "DoublePointer", "ShortPointer", "BooleanPointer", "CharPointer",
    "PointerPointer", "FunctionPointer", "Loader",
    # Annotations from org.bytedeco.javacpp.annotation (wildcard import).
    "Cast", "ByVal", "ByRef", "Const", "SharedPtr", "UniquePtr", "Optional",
    "StdVector", "StdString", "StdMap", "StdPair", "StdSet", "Name",
    "Namespace", "NoOffset", "NoException", "Properties", "MemberGetter",
    "MemberSetter", "Function", "Platform", "Opaque", "Virtual", "Raw",
    "Index", "Convention", "Adapter", "NullablePointer", "Allocator",
    "UniquePtrAdapter", "SharedPtrAdapter", "StdMove", "CLong",
    # java.nio wildcard already imported.
    "Buffer", "ByteBuffer", "IntBuffer", "LongBuffer", "FloatBuffer",
    "DoubleBuffer", "ShortBuffer", "CharBuffer",
}

PKG_RE = re.compile(r"^package\s+([\w.]+)\s*;", re.MULTILINE)
CLASS_DECL_RE = re.compile(r"\bpublic\s+(?:final\s+|abstract\s+)?"
                           r"(?:class|interface|enum)\s+(\w+)")
IDENT_RE = re.compile(r"\b([A-Z][A-Za-z0-9_]*)\b")
IMPORT_RE = re.compile(r"^import\s+([\w.]+(?:\.\*)?)\s*;", re.MULTILINE)


def scan_classes():
    """Return {simpleName: {fqn, ...}} for every top-level public class under
    SRC.  We keep all candidates so the importer can disambiguate against the
    *referencing* file's package."""
    result: dict[str, set[str]] = defaultdict(set)
    for root, _dirs, files in os.walk(SRC):
        for name in files:
            if not name.endswith(".java"):
                continue
            if not name[0].isupper():
                continue
            path = os.path.join(root, name)
            simple = name[:-5]
            with open(path, "r", encoding="utf-8") as fp:
                text = fp.read()
            pm = PKG_RE.search(text)
            if not pm:
                continue
            pkg = pm.group(1)
            if not CLASS_DECL_RE.search(text):
                continue
            result[simple].add(f"{pkg}.{simple}")
    return result


def resolve_import(simple: str, candidates: set[str], my_pkg: str) -> str | None:
    if not candidates:
        return None
    # Filter out preset configuration classes – they are never referenced
    # directly from generated bindings, only via @Properties(inherit=...).
    filtered = {c for c in candidates
                if not c.startswith("tensorrt_llm.presets.")}
    if not filtered:
        filtered = candidates
    if len(filtered) == 1:
        return next(iter(filtered))
    # Prefer a candidate that shares a longer package prefix with the
    # referencing file.
    best, best_score = None, -1
    my_parts = my_pkg.split(".")
    for c in filtered:
        cp = c.rsplit(".", 1)[0].split(".")
        score = 0
        for a, b in zip(my_parts, cp):
            if a == b:
                score += 1
            else:
                break
        if score > best_score:
            best, best_score = c, score
    return best


def gather_referenced(text: str) -> set[str]:
    """Gather simple names that look like class references in a Java file,
    ignoring anything inside double-quoted strings, line / block comments,
    and annotation value strings."""
    # Strip comments.
    t = re.sub(r"/\*.*?\*/", "", text, flags=re.DOTALL)
    t = re.sub(r"//[^\n]*", "", t)
    # Strip double-quoted strings.
    t = re.sub(r'"[^"]*"', '""', t)
    return set(IDENT_RE.findall(t))


def existing_imports(text: str) -> set[str]:
    return set(IMPORT_RE.findall(text))


def file_pkg(text: str) -> str:
    m = PKG_RE.search(text)
    return m.group(1) if m else ""


def add_imports(text: str, imports: list[str]) -> str:
    if not imports:
        return text
    new_lines = "\n".join(f"import {i};" for i in sorted(imports)) + "\n"
    # Insert right after package line, before other imports.
    return re.sub(
        r"(?m)(^package\s+[\w.]+;\s*\n)",
        r"\1\n" + new_lines,
        text,
        count=1,
    )


def main() -> int:
    class_map = scan_classes()
    print(f"indexed {len(class_map)} class names")

    edited = 0
    added_total = 0
    for root, _dirs, files in os.walk(SRC):
        for name in files:
            if not name.endswith(".java"):
                continue
            path = os.path.join(root, name)
            with open(path, "r", encoding="utf-8") as fp:
                text = fp.read()
            my_pkg = file_pkg(text)
            if not my_pkg:
                continue
            referenced = gather_referenced(text)
            imports = existing_imports(text)
            imported_simple = {imp.rsplit(".", 1)[-1] for imp in imports
                               if not imp.endswith(".*")}
            to_add: list[str] = []
            for simple in referenced:
                if simple in KEYWORDS:
                    continue
                if simple in imported_simple:
                    continue
                # Defined in same package?  No import needed.
                candidates = class_map.get(simple, set())
                fqn_same = f"{my_pkg}.{simple}"
                if fqn_same in candidates:
                    continue
                fqn = resolve_import(simple, candidates, my_pkg)
                if not fqn:
                    continue
                to_add.append(fqn)
            if to_add:
                text = add_imports(text, to_add)
                with open(path, "w", encoding="utf-8") as fp:
                    fp.write(text)
                edited += 1
                added_total += len(to_add)
    print(f"added {added_total} imports across {edited} files")
    return 0


if __name__ == "__main__":
    sys.exit(main())

