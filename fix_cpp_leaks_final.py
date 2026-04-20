#!/usr/bin/env python3
"""
Post-process JavaCPP-generated Java files to fix leaked C++ types.
Uses balanced-paren tracking to identify full statements, and comments
out entire statements if ANY part contains leaked C++ syntax.
"""
import re, glob, os

BASE = 'src/main/java/tensorrt_llm'
fixed_count = 0

CPP_LEAK_RE = re.compile(
    r'(?:'
    r'std::vector|std::map|std::set|std::pair|std::tuple|std::optional|'
    r'std::shared_ptr|std::unique_ptr|std::function|std::chrono|'
    r'std::filesystem|std::string(?!\s*\()|'
    r'(?<!\.)runtime::|(?<!\.)executor::|(?<!\.)batch_manager::|'
    r'kv_cache_manager::|nvinfer1::|tensorrt_llm::'
    r')'
)

def strip_strings(line):
    s = re.sub(r'@Cast\("[^"]*"\)', '', line)
    s = re.sub(r'nullValue\s*=\s*"[^"]*"', '', s)
    s = re.sub(r'"[^"]*"', '', s)
    return s

def line_has_leak(line):
    stripped = line.strip()
    if not stripped or stripped.startswith('//') or stripped.startswith('/*') or stripped.startswith('*'):
        return False
    return bool(CPP_LEAK_RE.search(strip_strings(stripped)))

def fix_extends_clause(content):
    return re.sub(
        r'public class (\w+) extends [a-zA-Z_:]+(?:<[^{]*?>)\s*\{',
        r'public class \1 extends Pointer {',
        content
    )

def parse_statements(lines):
    """Parse lines into statements. Returns list of (start_idx, end_idx) tuples.
    Each statement is a contiguous range of lines forming a Java statement."""
    statements = []
    i = 0
    n = len(lines)
    while i < n:
        stripped = lines[i].strip()
        # Skip blank lines, comments, annotations-only lines
        if not stripped or stripped.startswith('//') or stripped.startswith('/*') or stripped.startswith('*'):
            i += 1
            continue

        # Start of a statement
        start = i
        paren_depth = 0
        brace_depth = 0

        while i < n:
            s = lines[i].strip()
            if not s:
                break
            if s.startswith('//') or s.startswith('/*'):
                break

            clean = re.sub(r'"[^"]*"', '', s)
            paren_depth += clean.count('(') - clean.count(')')
            brace_depth += clean.count('{') - clean.count('}')

            i += 1

            # Statement ends when parens are balanced and line ends with ; or { or }
            if paren_depth <= 0 and brace_depth <= 0 and re.search(r'[;{}]\s*$', s):
                break
            # Also ends if parens go back to 0 and next line starts a new decl
            if paren_depth <= 0 and i < n:
                ns = lines[i].strip()
                if (ns.startswith('public ') or ns.startswith('private ') or
                    ns.startswith('/**') or ns.startswith('@Override') or
                    ns.startswith('@MemberGetter') or ns == '}' or
                    ns.startswith('// ') or not ns):
                    break

        statements.append((start, i))

    return statements

def fix_file(filepath):
    global fixed_count
    with open(filepath, 'r') as f:
        content = f.read()

    original = content
    content = fix_extends_clause(content)
    lines = content.split('\n')

    statements = parse_statements(lines)

    # Check each statement for leaks
    comment_lines = set()
    for start, end in statements:
        has_leak = False
        for i in range(start, end):
            if line_has_leak(lines[i]):
                has_leak = True
                break
        if has_leak:
            for i in range(start, end):
                comment_lines.add(i)

    if not comment_lines and content == original:
        return

    new_lines = []
    for i, line in enumerate(lines):
        if i in comment_lines:
            stripped = line.strip()
            if stripped and not stripped.startswith('//'):
                new_lines.append('    // [CPP-LEAK] ' + stripped)
            else:
                new_lines.append(line)
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

