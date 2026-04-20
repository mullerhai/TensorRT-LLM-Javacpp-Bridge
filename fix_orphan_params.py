#!/usr/bin/env python3
"""Fix orphaned parameter lines after SKIPPED comments in generated Java files."""
import glob

for f in glob.glob('src/main/java/tensorrt_llm/batch_manager/*.java'):
    with open(f, 'r') as fh:
        lines = fh.readlines()
    new_lines = []
    skip_next = False
    changed = False
    for line in lines:
        s = line.strip()
        if '// SKIPPED' in s:
            skip_next = True
            new_lines.append(line)
            continue
        if skip_next:
            # If it looks like orphaned params (starts with @ByVal, @Cast, @Const, @StdVector, etc or is just parameters)
            if s and not s.startswith('//') and not s.startswith('public') and not s.startswith('private') and not s.startswith('protected') and not s.startswith('}') and not s.startswith('@Namespace') and not s.startswith('@Properties') and not s.startswith('class ') and not s.startswith('static'):
                new_lines.append('    // ' + s + '\n')
                changed = True
                continue
            else:
                skip_next = False
        new_lines.append(line)
    if changed:
        with open(f, 'w') as fh:
            fh.writelines(new_lines)
        print(f'Fixed orphan params: {f}')

print("Done.")

