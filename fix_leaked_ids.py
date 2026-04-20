#!/usr/bin/env python3
"""Round 4: sanitize remaining C++ leaks & half-commented method signatures."""
import re, os, glob

ROOT = '/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/src/main/java/tensorrt_llm'

# Simple identifier substitutions (leaked typedef names JavaCPP emitted raw)
REPL = [
    (r'@ByVal\s+RequestIdType\b',          '@ByVal long'),
    (r'@StdVector\s+RequestIdType\b',      '@StdVector long'),
    (r'\bRequestIdType\b',                 'long'),
    (r'@ByVal\s+CacheType\b',              '@ByVal int'),
    (r'\bCacheType\s+(\w+)',               r'int \1'),
    (r'@ByVal\s+RetentionPriority\b',      '@ByVal int'),
    (r'\bRetentionPriority\b',             'int'),
    (r'@Const\s+@ByRef\s+VecUniqueTokens\b', '@Const @ByRef Pointer'),
    (r'@ByRef\s+VecUniqueTokens\b',        '@ByRef Pointer'),
    (r'\bVecUniqueTokens\b',               'Pointer'),
    (r'\bDataType\s+(\w+)',                r'int \1'),
    (r'@Cast\("[^"]*"\)\s+DataType\b',     'int'),
]

# Regex: find any `public/private ... native ... (` whose parameter list
# (across multiple lines) contains a line starting with `// [CPP-FIX]`.
# When found, comment out the whole method signature block.
SIG_START = re.compile(r'^(\s*)(public|private|protected|static)\b[^;{]*\bnative\b[^;{]*\(\s*$', re.M)

def nuke_broken_sigs(src):
    lines = src.split('\n')
    out = []
    i = 0
    n = len(lines)
    while i < n:
        line = lines[i]
        stripped = line.strip()
        # Candidate: ctor or native decl whose signature spans lines and isn't closed here
        is_method_start = (
            re.match(r'\s*(public|private|protected|static)\s', line) and
            '(' in line and not re.search(r'\);\s*(\{.*\})?\s*(//.*)?$', line)
            and not re.search(r'\{\s*$', line)  # not a method body opener that already ended sig
        )
        if is_method_start:
            # collect continuation until closing `);` or `){...}` or blank
            buf = [line]
            j = i + 1
            has_fix = '// [CPP-FIX]' in line
            closed = False
            while j < n:
                buf.append(lines[j])
                if '// [CPP-FIX]' in lines[j]:
                    has_fix = True
                # close condition: line ends with ');' optionally followed by body { ... }
                if re.search(r'\);\s*(\{.*\}\s*)?(//.*)?$', lines[j]):
                    closed = True
                    break
                if lines[j].strip() == '' and j - i > 30:
                    break
                j += 1
            if has_fix and closed:
                indent = re.match(r'\s*', line).group(0)
                out.append(f'{indent}// [CPP-FIX] broken decl removed:')
                for b in buf:
                    out.append('    // ' + b.lstrip())
                i = j + 1
                continue
        out.append(line)
        i += 1
    return '\n'.join(out)

total = 0
for dp, _, fs in os.walk(ROOT):
    for f in fs:
        if not f.endswith('.java'):
            continue
        fn = os.path.join(dp, f)
        with open(fn) as fh:
            s = fh.read()
        orig = s
        for pat, rep in REPL:
            s = re.sub(pat, rep, s)
        s = nuke_broken_sigs(s)
        if s != orig:
            with open(fn, 'w') as fh:
                fh.write(s)
            total += 1
print(f'done; {total} files touched')

