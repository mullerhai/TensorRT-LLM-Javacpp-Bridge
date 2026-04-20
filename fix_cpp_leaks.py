#!/usr/bin/env python3
"""
Post-process JavaCPP-generated Java files to fix leaked C++ types.
Comments out lines containing raw C++ syntax that can't compile in Java.
"""
import re, glob, os

BASE = 'src/main/java/tensorrt_llm'
fixed_count = 0

def has_leaked_cpp(line):
    """Check if a line contains leaked C++ syntax"""
    stripped = line.strip()
    if not stripped or stripped.startswith('//') or stripped.startswith('/*') or stripped.startswith('*'):
        return False
    # Check for C++ template types in code (not in @Cast strings)
    # Remove @Cast("...") strings first
    no_cast = re.sub(r'@Cast\("[^"]*"\)', '', stripped)
    no_cast = re.sub(r'nullValue\s*=\s*"[^"]*"', '', no_cast)
    # Check for leaked C++ patterns
    cpp_patterns = [
        r'\bstd::vector\b',
        r'\bstd::map\b',
        r'\bstd::set\b',
        r'\bstd::pair\b',
        r'\bstd::tuple\b',
        r'\bstd::string\b(?!.*")',  # not inside a string
        r'\bstd::optional\b',
        r'\bstd::shared_ptr\b',
        r'\bstd::unique_ptr\b',
        r'\bstd::function\b',
        r'\bstd::chrono\b',
        r'\bstd::filesystem\b',
        r'\bruntime::(?!class\b)',  # C++ namespace separator
        r'\bexecutor::(?!class\b)',
        r'\bbatch_manager::(?!class\b)',
        r'\bkv_cache_manager::',
        r'\bnvinfer1::(?!.*")',
        r'\btensorrt_llm::(?!.*")',  # not inside annotation strings
    ]
    for pat in cpp_patterns:
        # Don't match inside @Cast or string literals
        if re.search(pat, no_cast):
            return True
    return False

def fix_extends_clause(content):
    """Fix class declarations that extend C++ template types"""
    # Pattern: public class X extends some::cpp::Template<...> {
    content = re.sub(
        r'public class (\w+) extends [a-zA-Z_:]+(?:<[^{]*?>)\s*\{',
        r'public class \1 extends Pointer {',
        content
    )
    return content

def fix_file(filepath):
    global fixed_count
    with open(filepath, 'r') as f:
        content = f.read()

    original = content

    # Fix extends clauses with C++ template syntax
    content = fix_extends_clause(content)

    # Fix individual lines with leaked C++ types
    lines = content.split('\n')
    new_lines = []
    for line in lines:
        if has_leaked_cpp(line):
            new_lines.append('    // [CPP-LEAK] ' + line.strip())
        else:
            new_lines.append(line)

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

