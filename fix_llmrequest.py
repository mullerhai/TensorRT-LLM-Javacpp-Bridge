#!/usr/bin/env python3
"""Fix LlmRequest.java broken multi-line constructor."""
import re

filepath = 'src/main/java/tensorrt_llm/batch_manager/LlmRequest.java'
with open(filepath, 'r') as f:
    lines = f.readlines()

new_lines = []
i = 0
while i < len(lines):
    line = lines[i]
    s = line.strip()

    # Find the broken constructor/allocate - look for "private native void allocate"
    # that has parameters spanning into SKIPPED lines
    if 'private native void allocate(' in s and i + 1 < len(lines) and '// SKIPPED' in lines[i+2] if i+2 < len(lines) else False:
        # Comment out this line and all continuation lines until we find the closing );
        new_lines.append('    // SKIPPED (broken multi-line declaration):\n')
        while i < len(lines):
            cl = lines[i].strip()
            new_lines.append('    // ' + cl + '\n')
            i += 1
            if cl.endswith(');'):
                break
        continue

    # Also fix the public constructor that calls allocate with all those params
    if 'super((Pointer)null); allocate(' in s and len(s) > 500:
        new_lines.append('    // SKIPPED (broken constructor call): ' + s[:80] + '...\n')
        i += 1
        continue

    new_lines.append(line)
    i += 1

with open(filepath, 'w') as f:
    f.writelines(new_lines)
print(f'Fixed {filepath}')

