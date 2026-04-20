#!/usr/bin/env python3
"""
Ultimate fix for all JavaCPP-generated Java files with leaked C++ types.
Strategy: find ANY line containing raw C++ types outside of strings/annotations,
and comment out the whole method/declaration it belongs to.
"""
import re, glob, os

# Patterns for leaked C++ that should NOT appear in Java code
LEAKED_RE = re.compile(
    r'(?:std::(?:future|vector|enable_shared_from_this|optional<|string>)|'
    r'c10::|c10d::|'
    r'runtime::ITensor::|runtime::StringPtrMap|'
    r'tensorrt_llm::\w+::\w+::\w+::\w+|'  # deeply nested C++ names
    r'tensorrt_llm::batch_manager::\w+::(?:OptionalRef|TensorConstPtr|TensorMap|SizeType32))'
)

def clean_for_check(line):
    """Remove annotation string content to avoid false positives."""
    s = re.sub(r'@Cast\("[^"]*"\)', '', line)
    s = re.sub(r'@Name\("[^"]*"\)', '', s)
    s = re.sub(r'@ByVal\(nullValue\s*=\s*"[^"]*"\)', '', s)
    s = re.sub(r'@Namespace\("[^"]*"\)', '', s)
    s = re.sub(r'nullValue\s*=\s*"[^"]*"', '', s)
    return s

def has_leaked(line):
    cleaned = clean_for_check(line)
    return bool(LEAKED_RE.search(cleaned))

def fix_file(filepath):
    with open(filepath, 'r') as f:
        lines = f.readlines()

    new_lines = []
    changed = False
    i = 0

    while i < len(lines):
        line = lines[i]
        s = line.strip()

        # Skip already commented lines
        if s.startswith('//') or s.startswith('/*') or s.startswith('*') or not s:
            new_lines.append(line)
            i += 1
            continue

        # Skip safe lines
        if s.startswith('package') or s.startswith('import') or s.startswith('}'):
            new_lines.append(line)
            i += 1
            continue

        if has_leaked(line):
            # Comment out this line
            new_lines.append('    // FIXED: ' + s + '\n')
            changed = True
            i += 1

            # If this was part of a multi-line declaration, comment out continuations
            # A line is a continuation if the previous line doesn't end with ; or { or }
            if not s.endswith(';') and not s.endswith('{') and not s.endswith('}'):
                while i < len(lines):
                    ns = lines[i].strip()
                    if not ns or ns.startswith('//') or ns.startswith('/*'):
                        new_lines.append(lines[i])
                        i += 1
                        break
                    if ns.startswith('public ') or ns.startswith('private ') or ns.startswith('@Namespace') or ns.startswith('}') or ns.startswith('class '):
                        break
                    new_lines.append('    // FIXED: ' + ns + '\n')
                    changed = True
                    i += 1
                    if ns.endswith(';') or ns.endswith('{') or ns.endswith('}'):
                        break
            continue

        # Check for orphaned continuation lines (not starting with a keyword, after a FIXED line)
        if new_lines and '// FIXED:' in new_lines[-1]:
            if s and not s.startswith('public') and not s.startswith('private') and not s.startswith('//') and \
               not s.startswith('}') and not s.startswith('@Namespace') and not s.startswith('class') and \
               not s.startswith('static') and not s.startswith('@Properties'):
                new_lines.append('    // FIXED: ' + s + '\n')
                changed = True
                i += 1
                continue

        new_lines.append(line)
        i += 1

    if changed:
        with open(filepath, 'w') as f:
            f.writelines(new_lines)
        print(f'Fixed: {os.path.basename(filepath)}')
    return changed

# Also fix "extends C++::Type" in class declarations
def fix_extends(filepath):
    with open(filepath, 'r') as f:
        content = f.read()
    # Replace "extends tensorrt_llm::..." with "extends Pointer"
    new_content = re.sub(
        r'extends\s+tensorrt_llm::\S+',
        'extends Pointer',
        content
    )
    # Replace "extends std::..." with "extends Pointer"
    new_content = re.sub(
        r'extends\s+std::\S+',
        'extends Pointer',
        new_content
    )
    if new_content != content:
        with open(filepath, 'w') as f:
            f.write(new_content)
        print(f'Fixed extends: {os.path.basename(filepath)}')

patterns = [
    'src/main/java/tensorrt_llm/batch_manager/*.java',
    'src/main/java/tensorrt_llm/global/*.java',
    'src/main/java/tensorrt_llm/runtime/*.java',
    'src/main/java/tensorrt_llm/executor/*.java',
    'src/main/java/tensorrt_llm/layers/*.java',
    'src/main/java/tensorrt_llm/plugins/*.java',
    'src/main/java/tensorrt_llm/common/*.java',
]

total = 0
for pat in patterns:
    for f in sorted(glob.glob(pat)):
        fix_extends(f)
        if fix_file(f):
            total += 1

print(f'\nTotal files fixed: {total}')

