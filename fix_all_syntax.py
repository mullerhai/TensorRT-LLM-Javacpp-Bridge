#!/usr/bin/env python3
"""Fix all broken syntax in Java files."""
import os, re, glob

SRC = "src/main/java"

def fix_file(path):
    with open(path, 'r') as f:
        text = f.read()
    original = text

    # 1. Fix "new IntPointer(tokens.length," or "new IntPointer(inputTokenIds.length," etc -> close paren + semicolon
    #    Pattern: new SomePointer(expr,\n  where next line is a for loop or statement
    text = re.sub(r'new (\w+Pointer)\(([^)]+),\s*\n(\s*for\b)', r'new \1(\2);\n\3', text)

    # 2. Fix "new StringBuilder(input.length() + 16,\n" -> close paren
    text = re.sub(r'new StringBuilder\(([^)]+),\s*\n(\s*for\b)', r'new StringBuilder(\1);\n\2', text)

    # 3. Fix "BIAS(," -> "BIAS();"  pattern: = METHODNAME(,
    text = re.sub(r'= (\w+)\(,\s*$', r'= \1();', text, flags=re.MULTILINE)

    # 4. Fix ".skip()," -> ".skip());" at end of infoMap.put lines
    text = re.sub(r'\.skip\(\),\s*$', '.skip());', text, flags=re.MULTILINE)

    # 5. Fix '.pointerTypes("Pointer"),' -> '.pointerTypes("Pointer"));'
    text = re.sub(r'\.pointerTypes\("(\w+)"\),\s*$', r'.pointerTypes("\1"));', text, flags=re.MULTILINE)

    # 6. Fix '.pointerTypes("FloatPointer"),' etc
    text = re.sub(r'\.pointerTypes\("(\w+)"\),\s*$', r'.pointerTypes("\1"));', text, flags=re.MULTILINE)

    # 7. Fix "setter,\n" at end of native array setter decls -> "setter);\n"
    text = re.sub(r'(\w+) setter,\s*\n(\s*@MemberGetter)', r'\1 setter);\n\2', text)

    # 8. Fix "allocate(\n" missing closing -> needs );
    # Pattern in RuntimeBuffers: "private native void allocate(\n    private native"
    text = re.sub(r'(private native void allocate)\(\s*\n(\s*private native void)', r'\1();\n\2', text)

    # 9. Fix "kCACHE_LENGTH(\n" -> "kCACHE_LENGTH();\n"
    text = re.sub(r'(static native \w+ \w+)\(\s*\n(\s*public static final)', r'\1();\n\2', text)

    # 10. Fix "public native ... _allocate(,\n" -> remove leading comma
    text = re.sub(r'(_allocate)\(,\s*\n(\s+)', r'\1(\n\2', text)

    # 11. Fix "kOPT_PROFILES_SPLIT_POINTS(,\n" -> skip this broken decl
    text = re.sub(
        r'@MemberGetter public static native @Const @ByRef Pointer kOPT_PROFILES_SPLIT_POINTS\(,',
        '// [CPP-FIX] @MemberGetter public static native @Const @ByRef Pointer kOPT_PROFILES_SPLIT_POINTS();',
        text
    )

    # 12. Fix "extraArgs.isEmpty() ? "" : " " + extraArgs," -> missing closing paren
    text = re.sub(
        r'\+ \(extraArgs\.isEmpty\(\) \? "" : " " \+ extraArgs,',
        '+ (extraArgs.isEmpty() ? "" : " " + extraArgs);',
        text
    )

    # 13. Fix "reqIdPtr.put(0, requestIds[i]," -> missing )
    text = text.replace('reqIdPtr.put(0, requestIds[i],', 'reqIdPtr.put(0, requestIds[i]);')

    # 14. Fix "service.submit(..., false," at end -> missing )
    text = re.sub(r'(service\.submit\([^;]*), false,\s*$', r'\1, false);', text, flags=re.MULTILINE)

    # 15. Fix "@MemberGetter public native @Const FloatPointer logProbsPtrs(int i," -> missing )
    text = re.sub(r'(\w+Pointer \w+\(int i),\s*$', r'\1);', text, flags=re.MULTILINE)

    if text != original:
        with open(path, 'w') as f:
            f.write(text)
        print(f"Fixed: {path}")

for path in glob.glob(os.path.join(SRC, "**/*.java"), recursive=True):
    fix_file(path)

