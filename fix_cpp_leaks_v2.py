#!/usr/bin/env python3
"""
Post-process JavaCPP-generated Java files to fix leaked C++ types.
Comments out entire method/constructor blocks containing raw C++ syntax.
"""
import re, glob, os

BASE = 'src/main/java/tensorrt_llm'
fixed_count = 0

def has_leaked_cpp(line):
    """Check if a line contains leaked C++ syntax (outside @Cast annotations)"""
    stripped = line.strip()
    if not stripped or stripped.startswith('//') or stripped.startswith('/*') or stripped.startswith('*'):
        return False
    # Remove @Cast("...") and nullValue="..." strings
    no_cast = re.sub(r'@Cast\("[^"]*"\)', '', stripped)
    no_cast = re.sub(r'nullValue\s*=\s*"[^"]*"', '', no_cast)
    no_cast = re.sub(r'"[^"]*"', '', no_cast)  # Remove all remaining string literals

    cpp_patterns = [
        r'\bstd::vector\b', r'\bstd::map\b', r'\bstd::set\b', r'\bstd::pair\b',
        r'\bstd::tuple\b', r'\bstd::optional\b', r'\bstd::shared_ptr\b',
        r'\bstd::unique_ptr\b', r'\bstd::function\b', r'\bstd::chrono\b',
        r'\bstd::filesystem\b', r'\bstd::string\b',
        r'\bruntime::', r'\bexecutor::', r'\bbatch_manager::',
        r'\bkv_cache_manager::', r'\bnvinfer1::', r'\btensorrt_llm::',
    ]
    for pat in cpp_patterns:
        if re.search(pat, no_cast):
            return True
    return False

def fix_extends_clause(content):
    """Fix class declarations that extend C++ template types"""
    content = re.sub(
        r'public class (\w+) extends [a-zA-Z_:]+(?:<[^{]*?>)\s*\{',
        r'public class \1 extends Pointer {',
        content
    )
    return content

def fix_file(filepath):
    global fixed_count
    with open(filepath, 'r') as f:
        lines = f.readlines()

    original = ''.join(lines)

    # Fix extends clauses
    content = fix_extends_clause(original)
    lines = content.split('\n')

    new_lines = []
    i = 0
    while i < len(lines):
        line = lines[i]
        stripped = line.strip()

        # Check if this line has leaked C++ AND is part of a multi-line statement
        if has_leaked_cpp(line):
            # Find the full extent of this statement (until ; or { or })
            block = [line]
            j = i + 1
            # Check if this line is complete (has ; or { at end)
            combined = stripped
            while j < len(lines) and not re.search(r'[;{}]\s*$', combined):
                next_stripped = lines[j].strip()
                if not next_stripped or next_stripped.startswith('//'):
                    break
                block.append(lines[j])
                combined += ' ' + next_stripped
                j += 1
            # If the next line is a closing part, include it too
            if j < len(lines) and lines[j].strip() and not lines[j].strip().startswith('//'):
                # Check if combined is part of a constructor/method call and next line continues
                if not re.search(r'[;{}]\s*$', combined):
                    block.append(lines[j])
                    j += 1

            # Comment out entire block
            for bline in block:
                new_lines.append('    // [CPP-LEAK] ' + bline.strip())
            i = j
        else:
            # Check if this line is an orphan fragment (referenced vars from commented lines)
            # e.g. a continuation of a constructor call where first line was commented out
            if stripped and not stripped.startswith('//') and not stripped.startswith('/*') and not stripped.startswith('*'):
                # Check if prev line was commented with [CPP-LEAK]
                if new_lines and new_lines[-1].strip().startswith('// [CPP-LEAK]'):
                    # This might be a continuation - check if it starts with @, comma, or closing paren
                    if re.match(r'^(\s*@|\s*,|\s*\))', stripped) or (stripped.startswith('@') and not stripped.startswith('@Override') and not stripped.startswith('@Namespace') and not stripped.startswith('@Properties') and not stripped.startswith('@MemberGetter')):
                        # Check if this line references variables from commented lines
                        # or is a dangling parameter line
                        if not re.match(r'^\s*public\s', stripped) and not re.match(r'^\s*private\s', stripped) and not re.match(r'^\s*@Namespace', stripped):
                            new_lines.append('    // [CPP-LEAK] ' + stripped)
                            i += 1
                            continue
            new_lines.append(line)
            i += 1

    content = '\n'.join(new_lines)

    if content != original:
        with open(filepath, 'w') as f:
            f.write(content)
        fixed_count += 1
        print(f'Fixed: {os.path.relpath(filepath)}')

for f in sorted(glob.glob(f'{BASE}/**/*.java', recursive=True)):
    if '/presets/' in f:
        continue
    fix_file(f)

print(f'\nTotal files fixed: {fixed_count}')

