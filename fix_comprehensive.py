#!/usr/bin/env python3
"""
Comprehensive fix for JavaCPP-generated Java files with leaked C++ types.
Handles multi-line method declarations where some lines were commented out.
"""
import re, glob, os

LEAKED = re.compile(r'(?<!")(?:std::future<|std::vector<[A-Za-z]|std::enable_shared_from_this|c10::intrusive_ptr|c10d::|runtime::StringPtrMap|runtime::ITensor::SharedPtr)')

def fix_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    lines = content.split('\n')
    new_lines = []
    i = 0
    changed = False

    while i < len(lines):
        line = lines[i]
        stripped = line.strip()

        # Skip already commented lines
        if stripped.startswith('//') or stripped.startswith('/*') or stripped.startswith('*'):
            new_lines.append(line)
            i += 1
            continue

        # Check for leaked types
        # Remove @Cast("..."), @ByVal(nullValue="..."), @Name("...") content before checking
        cleaned = re.sub(r'@Cast\("[^"]*"\)', '', line)
        cleaned = re.sub(r'@Name\("[^"]*"\)', '', cleaned)
        cleaned = re.sub(r'@ByVal\(nullValue\s*=\s*"[^"]*"\)', '', cleaned)
        cleaned = re.sub(r'@Namespace\("[^"]*"\)', '', cleaned)

        if LEAKED.search(cleaned):
            # Comment out this line and any continuation lines
            new_lines.append('    // SKIPPED: ' + stripped)
            changed = True
            i += 1

            # Check if we're in the middle of a method declaration (no semicolon or opening brace)
            while i < len(lines):
                next_stripped = lines[i].strip()
                if not next_stripped:
                    new_lines.append('')
                    i += 1
                    break
                # If it's a continuation of the broken declaration
                if (next_stripped.startswith('@') and not next_stripped.startswith('@Namespace') and not next_stripped.startswith('@Properties')) or \
                   next_stripped.startswith('private native') or \
                   (not next_stripped.startswith('public') and not next_stripped.startswith('//') and
                    not next_stripped.startswith('}') and not next_stripped.startswith('class') and
                    not next_stripped.startswith('static') and not next_stripped.startswith('/*') and
                    not next_stripped.startswith('*') and next_stripped and
                    not next_stripped.startswith('@Namespace') and not next_stripped.startswith('@Properties')):

                    # Check if this line ends the method (has ; or {)
                    if ';' in next_stripped or '{' in next_stripped:
                        new_lines.append('    // ' + next_stripped)
                        changed = True
                        i += 1
                        break
                    else:
                        new_lines.append('    // ' + next_stripped)
                        changed = True
                        i += 1
                else:
                    break
            continue

        # Check if this is an orphaned line after a SKIPPED comment
        # (parameter line from a multi-line declaration that was partially commented out)
        if len(new_lines) > 0 and '// SKIPPED' in new_lines[-1]:
            if stripped and not stripped.startswith('public') and not stripped.startswith('private') and not stripped.startswith('//') and not stripped.startswith('}'):
                new_lines.append('    // ' + stripped)
                changed = True
                i += 1
                continue

        new_lines.append(line)
        i += 1

    if changed:
        with open(filepath, 'w') as f:
            f.write('\n'.join(new_lines))
        print(f'Fixed: {os.path.basename(filepath)}')
    return changed

# Process all Java files
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
        if fix_file(f):
            total += 1

# Second pass: fix any remaining broken multi-line declarations
# where a SKIPPED line left orphaned parameter lines
for pat in patterns:
    for filepath in sorted(glob.glob(pat)):
        with open(filepath, 'r') as f:
            lines = f.readlines()
        new_lines = []
        changed2 = False
        in_skipped_block = False
        for line in lines:
            s = line.strip()
            if '// SKIPPED' in s or (in_skipped_block and s.startswith('//')):
                in_skipped_block = True
                new_lines.append(line)
                continue
            if in_skipped_block:
                in_skipped_block = False
            new_lines.append(line)
        if changed2:
            with open(filepath, 'w') as f:
                f.writelines(new_lines)

print(f'\nTotal files fixed: {total}')

