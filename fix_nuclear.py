#!/usr/bin/env python3
"""
Nuclear option: find ANY Java method/constructor declaration that spans multiple lines
and has some lines commented out with [CPP-FIX]. Comment out the ENTIRE declaration.
Also find orphaned parameter lines (lines starting with @Cast, @Optional, @SharedPtr, etc.)
that are not inside a valid method.
"""
import re, glob, os

def fix_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    lines = content.split('\n')
    n = len(lines)
    new_lines = []
    changed = False
    i = 0

    while i < n:
        line = lines[i]
        s = line.strip()

        # Detect start of a multi-line method/constructor declaration:
        # - Starts with "public" or "private" (not a comment)
        # - Does NOT end with ; or { or }
        # - Is NOT already a [CPP-FIX] line
        if not s.startswith('//') and not s.startswith('/*') and not s.startswith('*'):
            # Check if this is the start of a declaration that doesn't complete on this line
            is_decl_start = (
                (s.startswith('public ') or s.startswith('private ') or s.startswith('protected ')
                 or s.startswith('@Namespace') or s.startswith('@NoOffset'))
                and not s.endswith(';') and not s.endswith('{') and not s.endswith('}')
                and ('(' in s)  # has opening paren - likely a method
            )

            if is_decl_start:
                # Collect all lines of this multi-line declaration
                block = [line]
                j = i + 1
                has_cpp_fix = False

                while j < n:
                    bline = lines[j]
                    bs = bline.strip()

                    if bs.startswith('// [CPP-FIX]') or bs.startswith('// SKIPPED') or bs.startswith('// FIXED'):
                        has_cpp_fix = True
                        block.append(bline)
                        j += 1
                        # Check if the commented content ends the declaration
                        content_part = bs.split('] ', 1)[-1] if '] ' in bs else bs[3:]
                        if content_part.rstrip().endswith(';') or content_part.rstrip().endswith('}') or content_part.rstrip().endswith('{'):
                            break
                        continue

                    if not bs:
                        # Empty line - end of declaration
                        break

                    if bs.startswith('//') or bs.startswith('/*') or bs.startswith('*'):
                        block.append(bline)
                        j += 1
                        continue

                    block.append(bline)
                    j += 1

                    if bs.endswith(';') or bs.endswith('}') or bs.endswith('{'):
                        break

                if has_cpp_fix:
                    # Comment out the ENTIRE block
                    for bline in block:
                        bs = bline.strip()
                        if not bs.startswith('//'):
                            new_lines.append('    // [CPP-FIX] ' + bs)
                            changed = True
                        else:
                            new_lines.append(bline)
                    i = j
                    continue

            # Also detect orphaned continuation lines:
            # Lines starting with @Cast, @Optional, @SharedPtr, @Const, @ByVal, @ByRef, @StdVector, etc.
            # that appear right after a [CPP-FIX] comment where the previous statement wasn't complete
            if (s.startswith('@') and not s.startswith('@Override') and not s.startswith('@Namespace')
                and not s.startswith('@Properties') and not s.startswith('@Deprecated')
                and not s.startswith('@MemberGetter') and not s.startswith('@MemberSetter')):
                # Check if previous line is a [CPP-FIX] that didn't end with ; or { or }
                prev_idx = i - 1
                while prev_idx >= 0 and not lines[prev_idx].strip():
                    prev_idx -= 1
                if prev_idx >= 0:
                    prev_s = lines[prev_idx].strip()
                    if prev_s.startswith('// [CPP-FIX]') or prev_s.startswith('// SKIPPED'):
                        content_part = prev_s.split('] ', 1)[-1] if '] ' in prev_s else prev_s[3:]
                        if not content_part.rstrip().endswith(';') and not content_part.rstrip().endswith('{') and not content_part.rstrip().endswith('}'):
                            # This is orphaned
                            new_lines.append('    // [CPP-FIX] ' + s)
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

patterns = [
    'src/main/java/tensorrt_llm/batch_manager/*.java',
    'src/main/java/tensorrt_llm/global/*.java',
    'src/main/java/tensorrt_llm/runtime/*.java',
    'src/main/java/tensorrt_llm/executor/*.java',
    'src/main/java/tensorrt_llm/layers/*.java',
    'src/main/java/tensorrt_llm/plugins/*.java',
    'src/main/java/tensorrt_llm/common/*.java',
]

# Run multiple passes
for pass_num in range(5):
    total = 0
    for pat in patterns:
        for f in sorted(glob.glob(pat)):
            if '/presets/' in f:
                continue
            if fix_file(f):
                total += 1
    print(f'Pass {pass_num+1}: {total} files fixed')
    if total == 0:
        break


