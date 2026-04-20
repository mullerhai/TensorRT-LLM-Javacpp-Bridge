#!/usr/bin/env python3
"""Restore incorrectly commented valid allocate methods in LlmRequest.java"""
filepath = 'src/main/java/tensorrt_llm/batch_manager/LlmRequest.java'
with open(filepath, 'r') as f:
    lines = f.readlines()

new_lines = []
i = 0
while i < len(lines):
    s = lines[i].strip()

    # Check for "// SKIPPED (broken multi-line allocate):" blocks
    if s == '// SKIPPED (broken multi-line allocate):':
        # Collect all following comment lines
        block = []
        j = i + 1
        while j < len(lines) and lines[j].strip().startswith('//'):
            block.append(lines[j])
            j += 1

        # Check if block contains genuinely broken content
        has_leaked = any('SKIPPED (leaked C++ type)' in b or
                        ('std::vector<' in b and '@Cast' not in b) or
                        'std::future<' in b
                        for b in block)

        if has_leaked:
            # Keep the whole block commented
            new_lines.append(lines[i])
            for b in block:
                new_lines.append(b)
            i = j
        else:
            # Restore the block - uncomment valid lines
            for b in block:
                content = b.strip()
                if content.startswith('// '):
                    content = content[3:]
                new_lines.append('    ' + content + '\n')
            i = j
        continue

    new_lines.append(lines[i])
    i += 1

with open(filepath, 'w') as f:
    f.writelines(new_lines)
print('Fixed LlmRequest.java')

