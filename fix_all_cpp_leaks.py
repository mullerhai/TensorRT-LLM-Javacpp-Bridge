#!/usr/bin/env python3
"""
Final comprehensive fix: comment out any line containing raw C++ syntax
that Java compiler cannot understand. Handles all edge cases including:
- std::unordered_set, std::future, std::vector with nested templates
- runtime::StringPtrMap
- Multi-line method declarations broken by partial commenting
- Class extends clauses with C++ types
"""
import re, glob, os

# Very broad pattern: any line with "::" that is NOT inside a string literal
# and NOT a safe annotation like @Namespace or @Cast
CPP_COLONCOLON = re.compile(r'::')

# Safe patterns (these are OK to have :: in)
SAFE_PATTERNS = [
    r'^\s*//',          # already commented
    r'^\s*\*',          # javadoc
    r'^\s*/\*',         # block comment
    r'^\s*package\s',   # package decl
    r'^\s*import\s',    # import
    r'^\s*$',           # empty
]

def is_safe_line(line):
    for pat in SAFE_PATTERNS:
        if re.match(pat, line):
            return True
    return False

def remove_string_content(line):
    """Remove content inside double quotes to avoid false positives from @Cast("...") etc."""
    return re.sub(r'"[^"]*"', '""', line)

def line_has_leaked_cpp(line):
    """Check if a line has leaked C++ syntax outside of string literals."""
    if is_safe_line(line):
        return False
    cleaned = remove_string_content(line)
    # Check for :: outside strings - this is the definitive C++ leak indicator
    if '::' in cleaned:
        return True
    # Check for other C++ constructs
    if re.search(r'\b__host__\b|\b__device__\b', cleaned):
        return True
    return False

def fix_file(filepath):
    with open(filepath, 'r') as f:
        lines = f.readlines()

    new_lines = []
    changed = False
    i = 0

    while i < len(lines):
        line = lines[i]

        if line_has_leaked_cpp(line):
            # Comment out this line
            new_lines.append('    // [CPP-FIX] ' + line.lstrip())
            changed = True
            i += 1

            # If this line doesn't end the statement, comment out continuations
            stripped = line.strip()
            if not stripped.endswith(';') and not stripped.endswith('{') and not stripped.endswith('}'):
                while i < len(lines):
                    next_line = lines[i]
                    next_stripped = next_line.strip()

                    # Stop at empty lines, new public/private declarations, closing braces, or new annotations
                    if not next_stripped:
                        new_lines.append(next_line)
                        i += 1
                        break
                    if next_stripped.startswith('//') or next_stripped.startswith('/*') or next_stripped.startswith('*'):
                        new_lines.append(next_line)
                        i += 1
                        continue
                    if (next_stripped.startswith('public ') or next_stripped.startswith('private ') or
                        next_stripped.startswith('protected ') or next_stripped.startswith('@Namespace') or
                        next_stripped.startswith('}') or next_stripped.startswith('class ')):
                        # Check if THIS line also has leaked cpp
                        if line_has_leaked_cpp(next_line):
                            new_lines.append('    // [CPP-FIX] ' + next_stripped + '\n')
                            changed = True
                            i += 1
                            if next_stripped.endswith(';') or next_stripped.endswith('{') or next_stripped.endswith('}'):
                                break
                            continue
                        break

                    # This is a continuation line - comment it out
                    new_lines.append('    // [CPP-FIX] ' + next_stripped + '\n')
                    changed = True
                    i += 1
                    if next_stripped.endswith(';') or next_stripped.endswith('{') or next_stripped.endswith('}'):
                        break
            continue

        new_lines.append(line)
        i += 1

    # Second pass: fix orphaned lines after commented-out method declarations
    # e.g., a "private native void allocate(...," was commented but the next param lines weren't
    final_lines = []
    for idx, line in enumerate(new_lines):
        stripped = line.strip()
        if stripped.startswith('// [CPP-FIX]') or stripped.startswith('// SKIPPED') or stripped.startswith('// FIXED'):
            final_lines.append(line)
            continue

        # Check if previous non-empty line was a CPP-FIX comment
        prev_idx = idx - 1
        while prev_idx >= 0 and not new_lines[prev_idx].strip():
            prev_idx -= 1
        if prev_idx >= 0:
            prev = new_lines[prev_idx].strip()
            if (prev.startswith('// [CPP-FIX]') or prev.startswith('// SKIPPED') or prev.startswith('// FIXED')):
                # Check if the previous commented line ended a statement
                prev_content = prev.split('] ', 1)[-1] if '] ' in prev else prev[3:]
                if not prev_content.rstrip().endswith(';') and not prev_content.rstrip().endswith('{') and not prev_content.rstrip().endswith('}'):
                    # Previous was incomplete, this might be orphaned continuation
                    if stripped and not stripped.startswith('public') and not stripped.startswith('private') and \
                       not stripped.startswith('protected') and not stripped.startswith('//') and \
                       not stripped.startswith('}') and not stripped.startswith('@Namespace') and \
                       not stripped.startswith('class') and not stripped.startswith('static {'):
                        final_lines.append('    // [CPP-FIX] ' + stripped + '\n')
                        changed = True
                        continue

        final_lines.append(line)

    if changed:
        with open(filepath, 'w') as f:
            f.writelines(final_lines)
        print(f'Fixed: {os.path.basename(filepath)}')
    return changed

# Also fix "extends C++::Type" in class declarations
def fix_extends(filepath):
    with open(filepath, 'r') as f:
        content = f.read()
    new_content = re.sub(r'extends\s+\S*::\S+', 'extends Pointer', content)
    if new_content != content:
        with open(filepath, 'w') as f:
            f.write(new_content)
        print(f'Fixed extends: {os.path.basename(filepath)}')
        return True
    return False

patterns = [
    'src/main/java/tensorrt_llm/batch_manager/*.java',
    'src/main/java/tensorrt_llm/global/*.java',
    'src/main/java/tensorrt_llm/runtime/*.java',
    'src/main/java/tensorrt_llm/executor/*.java',
    'src/main/java/tensorrt_llm/layers/*.java',
    'src/main/java/tensorrt_llm/plugins/*.java',
    'src/main/java/tensorrt_llm/common/*.java',
    'src/main/java/tensorrt_llm/kernels/*.java',
    'src/main/java/tensorrt_llm/thop/*.java',
    'src/main/java/tensorrt_llm/cutlass_extensions/*.java',
]

total = 0
for pat in patterns:
    for f in sorted(glob.glob(pat)):
        # Skip preset config files
        if '/presets/' in f:
            continue
        fix_extends(f)
        if fix_file(f):
            total += 1

print(f'\nTotal files fixed: {total}')

