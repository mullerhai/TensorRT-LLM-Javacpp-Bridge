#!/usr/bin/env python3
"""
Fix files where class declaration was wrongly commented out.
Restore '// SKIPPED: public class X extends Pointer {' lines
and the @Namespace annotations before them.
"""
import re, glob, os

def fix_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    original = content

    # Fix: // SKIPPED: public class X extends Pointer {  ->  public class X extends Pointer {
    content = re.sub(
        r'^\s*// SKIPPED: (public class \w+ extends Pointer \{)',
        r'\1',
        content, flags=re.MULTILINE
    )

    # Fix: // static { Loader.load(); }  ->  static { Loader.load(); }
    content = re.sub(
        r'^\s*// (static \{ Loader\.load\(\); \})',
        r'    \1',
        content, flags=re.MULTILINE
    )

    # Fix: // [CPP-FIX] @Namespace("...") @Properties(...)  ->  restore if it's a class-level annotation
    # Only restore lines that are immediately followed by a class declaration
    lines = content.split('\n')
    new_lines = []
    for i, line in enumerate(lines):
        s = line.strip()
        # Check if this is a commented-out @Namespace that precedes a class declaration
        if (s.startswith('// [CPP-FIX] @Namespace(') and '@Properties(' in s):
            # Check if next non-empty, non-comment line is a class declaration
            j = i + 1
            while j < len(lines) and (not lines[j].strip() or lines[j].strip().startswith('//')):
                j += 1
            if j < len(lines) and 'public class' in lines[j]:
                # Restore this annotation
                restored = s.replace('// [CPP-FIX] ', '')
                new_lines.append(restored)
                continue
        new_lines.append(line)

    content = '\n'.join(new_lines)

    if content != original:
        with open(filepath, 'w') as f:
            f.write(content)
        print(f'Fixed class decl: {os.path.basename(filepath)}')
        return True
    return False

patterns = [
    'src/main/java/tensorrt_llm/**/*.java',
]

for pat in patterns:
    for f in sorted(glob.glob(pat, recursive=True)):
        if '/presets/' in f:
            continue
        fix_file(f)

