#!/usr/bin/env python3
"""
Post-process JavaCPP-generated Java files to fix leaked C++ types.
Uses a statement-aware approach: when a C++ leak is detected, the entire
Java statement (spanning multiple lines) is commented out.
"""
import re, glob, os

BASE = 'src/main/java/tensorrt_llm'
fixed_count = 0

CPP_LEAK_RE = re.compile(
    r'(?<!["\'])(?:'
    r'std::vector|std::map|std::set|std::pair|std::tuple|std::optional|'
    r'std::shared_ptr|std::unique_ptr|std::function|std::chrono|'
    r'std::filesystem|std::string(?!.*")|'
    r'runtime::|executor::|batch_manager::|kv_cache_manager::|'
    r'nvinfer1::|tensorrt_llm::'
    r')'
)

def strip_annotations(line):
    """Remove @Cast("..."), nullValue="...", and all string literals for leak detection."""
    s = re.sub(r'@Cast\("[^"]*"\)', '', line)
    s = re.sub(r'nullValue\s*=\s*"[^"]*"', '', s)
    s = re.sub(r'"[^"]*"', '', s)
    return s

def has_leaked_cpp(line):
    stripped = line.strip()
    if not stripped or stripped.startswith('//') or stripped.startswith('/*') or stripped.startswith('*'):
        return False
    return bool(CPP_LEAK_RE.search(strip_annotations(stripped)))

def fix_extends_clause(content):
    return re.sub(
        r'public class (\w+) extends [a-zA-Z_:]+(?:<[^{]*?>)\s*\{',
        r'public class \1 extends Pointer {',
        content
    )

def collect_statement(lines, start):
    """Collect a complete Java statement starting at `start`.
    Returns (list_of_line_indices, next_index)."""
    indices = [start]
    # Count braces/parens to find statement end
    depth_paren = 0
    depth_brace = 0
    i = start
    while i < len(lines):
        s = lines[i].strip()
        # Skip string contents for counting
        clean = re.sub(r'"[^"]*"', '', s)
        depth_paren += clean.count('(') - clean.count(')')
        depth_brace += clean.count('{') - clean.count('}')
        if i > start:
            indices.append(i)
        # Statement ends when we're balanced and line ends with ; or { or }
        if depth_paren <= 0 and re.search(r'[;{}]\s*$', s):
            return indices, i + 1
        # Also stop if next line starts a new declaration
        if i > start and depth_paren <= 0:
            next_i = i + 1
            if next_i < len(lines):
                ns = lines[next_i].strip()
                if ns.startswith('public ') or ns.startswith('private ') or ns.startswith('/**') or ns.startswith('@') or ns == '}' or not ns:
                    return indices, i + 1
        i += 1
    return indices, i

def fix_file(filepath):
    global fixed_count
    with open(filepath, 'r') as f:
        content = f.read()

    original = content
    content = fix_extends_clause(content)
    lines = content.split('\n')

    # First pass: identify lines to comment out
    comment_lines = set()
    i = 0
    while i < len(lines):
        if has_leaked_cpp(lines[i]):
            indices, next_i = collect_statement(lines, i)
            comment_lines.update(indices)
            i = next_i
        else:
            i += 1

    # Second pass: also check for orphaned continuation lines
    # (lines that follow a commented line and look like they're part of it)
    changed = True
    while changed:
        changed = False
        for i in range(len(lines)):
            if i in comment_lines:
                continue
            stripped = lines[i].strip()
            if not stripped or stripped.startswith('//') or stripped.startswith('/*') or stripped.startswith('*'):
                continue
            # If previous non-empty line is commented and this line looks like a continuation
            prev = i - 1
            while prev >= 0 and not lines[prev].strip():
                prev -= 1
            if prev >= 0 and prev in comment_lines:
                # Check if this line starts with parameter-like syntax
                if re.match(r'^\s*(@\w|,\s*@|\)\s*\{|\)\s*;)', stripped):
                    comment_lines.add(i)
                    changed = True
                # Or if this line has unbalanced parens (continuation of constructor)
                elif stripped.startswith('@Optional') or stripped.startswith('@StdVector') or stripped.startswith('@SharedPtr') or stripped.startswith('@Const') or stripped.startswith('@ByRef') or stripped.startswith('@ByVal'):
                    comment_lines.add(i)
                    changed = True

    if not comment_lines:
        if content != original:
            with open(filepath, 'w') as f:
                f.write(content)
            fixed_count += 1
            print(f'Fixed (extends only): {os.path.relpath(filepath)}')
        return

    # Apply comments
    new_lines = []
    for i, line in enumerate(lines):
        if i in comment_lines:
            new_lines.append('    // [CPP-LEAK] ' + line.strip())
        else:
            new_lines.append(line)

    result = '\n'.join(new_lines)
    if result != original:
        with open(filepath, 'w') as f:
            f.write(result)
        fixed_count += 1
        print(f'Fixed: {os.path.relpath(filepath)}')

for f in sorted(glob.glob(f'{BASE}/**/*.java', recursive=True)):
    if '/presets/' in f:
        continue
    fix_file(f)

print(f'\nTotal files fixed: {fixed_count}')

