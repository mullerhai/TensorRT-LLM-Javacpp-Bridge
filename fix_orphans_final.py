#!/usr/bin/env python3
"""
Fix orphaned continuation lines after [CPP-FIX] comments.
When a multi-line Java method/constructor is partially commented out,
the remaining parameter lines become orphaned and cause compile errors.
"""
import re, glob, os

def fix_file(filepath):
    with open(filepath, 'r') as f:
        lines = f.readlines()

    new_lines = []
    changed = False
    in_broken_decl = False
    brace_depth = 0

    for i, line in enumerate(lines):
        s = line.strip()

        # Track if we're inside a broken declaration
        if s.startswith('// [CPP-FIX]') or s.startswith('// SKIPPED') or s.startswith('// FIXED'):
            new_lines.append(line)
            # Check if the commented line ends a statement
            content = s.split('] ', 1)[-1] if '] ' in s else s[3:]
            if not content.rstrip().endswith(';') and not content.rstrip().endswith('}') and not content.rstrip().endswith('{'):
                in_broken_decl = True
            else:
                in_broken_decl = False
            continue

        if in_broken_decl:
            # Check if this line is a continuation of the broken declaration
            if s and not s.startswith('//') and not s.startswith('/*') and not s.startswith('*'):
                # This is an orphaned continuation - comment it out
                new_lines.append('    // [CPP-FIX] ' + s + '\n')
                changed = True
                if s.endswith(';') or s.endswith('}') or s.endswith('{'):
                    in_broken_decl = False
                continue
            elif not s:
                in_broken_decl = False
                new_lines.append(line)
                continue
            else:
                # It's a comment line
                new_lines.append(line)
                continue

        new_lines.append(line)

    if changed:
        with open(filepath, 'w') as f:
            f.writelines(new_lines)
        print(f'Fixed orphans: {os.path.basename(filepath)}')
    return changed

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
        if '/presets/' in f:
            continue
        if fix_file(f):
            total += 1

print(f'\nTotal files fixed: {total}')

