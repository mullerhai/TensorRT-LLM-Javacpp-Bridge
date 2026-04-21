#!/usr/bin/env python3
"""Fix orphaned continuation lines in all broken Java files."""
import re, sys, os, glob

def fix_file(path):
    with open(path) as f:
        lines = f.readlines()

    def is_orphaned(l):
        s = l.rstrip()
        # Orphaned continuations are indented 8+ spaces (typically 12)
        # Method declarations use exactly 4 spaces
        if not s.startswith('        '): return False  # must have 8+ spaces
        ls = s.lstrip()
        if ls.startswith('//'): return False  # skip comments
        # Must end with ); or contain , (but not be a pure comment line)
        return s.endswith(');') or (',' in s and not ls.startswith('*'))

    out = []
    i = 0
    while i < len(lines):
        line = lines[i]
        stripped = line.rstrip()

        # Remove malformed overloads: "method(," with empty first param
        if re.search(r'(?:public|private) (?:static )?native \w[\w<>]* \w+\(,', stripped):
            j = i
            while j < len(lines):
                ln = lines[j].rstrip()
                j += 1
                if ln.endswith(');'):
                    break
            i = j
            continue

        # Fix orphaned continuation lines
        if stripped.endswith(');') and (i + 1) < len(lines):
            conts = []
            j = i + 1
            while j < len(lines) and is_orphaned(lines[j]):
                conts.append(lines[j].rstrip().strip())
                j += 1
            if conts:
                fixed = stripped[:-2]  # remove ');'
                # If the trigger line had empty parens "();", don't add a comma
                sep = '\n' if fixed.endswith('(') else ',\n'
                merged = fixed + sep
                for k, c in enumerate(conts):
                    merged += '    ' + c + '\n'
                out.append(merged)
                i = j
                continue

        # Fix `__restrict/*paramName*/` → paramName
        line = re.sub(r'__restrict/\*(\w+)\*/', r'\1', line)
        # Fix bare `__restrict`
        line = re.sub(r'\b__restrict\b', 'pRestrict', line)
        # Fix `half` CUDA type → ShortPointer
        line = re.sub(r'\bhalf\b(?![\w])', 'ShortPointer', line)

        out.append(line)
        i += 1

    with open(path, 'w') as f:
        f.writelines(out)
    print(f"  Fixed: {os.path.basename(path)}")

if __name__ == '__main__':
    base = "src/main/java/tensorrt_llm"
    targets = (
        list(glob.glob(f"{base}/global/*.java")) +
        list(glob.glob(f"{base}/batch_manager/*.java"))
    )
    for p in sorted(targets):
        fix_file(p)
    print("Done.")

