#!/usr/bin/env python3
"""Fix Kernels.java compile errors after re-parse."""
import re, os

ROOT = '/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/src/main/java/tensorrt_llm'

def rename_restrict_in_sigs(text):
    """Rename all __restrict params in each native decl so they don't collide."""
    def process_decl(m):
        sig = m.group(0)
        i = [0]
        def rep(x):
            i[0] += 1
            return '__restrict_' + str(i[0])
        return re.sub(r'\b__restrict\b', rep, sig)
    # Matches "public (static )?native <RET> NAME(...);" across newlines.
    return re.sub(r'public\s+(?:static\s+)?native\s+[^;]*?;', process_decl, text, flags=re.DOTALL)

def drop_orphan_empty_first_arg(text):
    # drop any "(\s*,\s*" decls that leaked after type stripping
    return re.sub(r'(?s)(?:@Namespace\([^)]*\)\s*)?public\s+(?:static\s+)?native\s+[^;(]+\(\s*,\s*[^;]*?;\s*\n', '', text)

def normalize_sig(decl):
    s = re.sub(r'/\*[^*]*\*/', '', decl)
    s = re.sub(r'\s+', '', s)
    # strip param names — keep only types and annotations before commas
    # simplest: drop ident after last word char sequence followed by ',' or ')'
    s = re.sub(r'([A-Za-z_][A-Za-z0-9_]*)(?=[,\)])', '', s)
    return s

def dedupe_native_decls(text):
    """Collapse duplicate @Namespace public (static) native signatures."""
    out = []
    i = 0
    n = len(text)
    seen = set()
    # We'll find each decl boundary: match declaration start to ';' (naively).
    pattern = re.compile(r'(?s)((?:@[A-Za-z_]\w*(?:\([^)]*\))?\s*)*public\s+(?:static\s+)?native\s+[^;]*?;)')
    last = 0
    new_text = []
    for m in pattern.finditer(text):
        new_text.append(text[last:m.start()])
        decl = m.group(1)
        key = normalize_sig(decl)
        if key in seen:
            pass  # skip duplicate
        else:
            seen.add(key)
            new_text.append(decl)
        last = m.end()
    new_text.append(text[last:])
    return ''.join(new_text)

def dedupe_constructors_and_methods_per_file(text):
    """Per-class dedupe: keep first-seen Java method signatures (including
    constructors) and drop later duplicates.  We compare by method NAME + ordered
    Java parameter TYPES (ignoring param names, annotations and defaults)."""
    lines = text.split('\n')
    out = []
    seen = set()
    i = 0
    while i < len(lines):
        line = lines[i]
        # Try to detect a method/constructor declaration spanning one or more lines.
        if re.match(r'\s*(public|private|protected)\s.*\([^)]*\)?', line) and \
           ('native' in line or ' void ' in line or re.search(r'\)\s*\{', line) or '=' in line or line.strip().endswith(';')):
            # Gather continuation until we find ';' or '{' or line ending ');' or ') {'
            buf = [line]; j = i
            while ';' not in lines[j] and '{' not in lines[j]:
                j += 1
                if j >= len(lines): break
                buf.append(lines[j])
            decl = '\n'.join(buf)
            # Extract method name and parameter types.
            m = re.search(r'\b([A-Za-z_]\w*)\s*\(([^)]*)\)', decl)
            if m:
                name = m.group(1)
                params = m.group(2)
                # strip annotations and /* */ comments and default values
                params = re.sub(r'@\w+(?:\([^)]*\))?', '', params)
                params = re.sub(r'/\*.*?\*/', '', params, flags=re.DOTALL)
                # keep only type tokens — rough: first word of each comma-split
                types = []
                for p in params.split(','):
                    p = p.strip()
                    if not p: continue
                    toks = p.split()
                    if len(toks) >= 1:
                        types.append(toks[0])
                key = name + '|' + ','.join(types)
                if key in seen and name not in ('static',):
                    # skip duplicate
                    i = j + 1
                    continue
                seen.add(key)
            out.extend(buf)
            i = j + 1
            continue
        out.append(line)
        i += 1
    return '\n'.join(out)

total = 0
for dp, _, fs in os.walk(ROOT):
    for f in fs:
        if not f.endswith('.java'): continue
        fn = os.path.join(dp, f)
        with open(fn) as fh: s = fh.read()
        orig = s
        s = drop_orphan_empty_first_arg(s)
        s = rename_restrict_in_sigs(s)
        s = dedupe_native_decls(s)
        # per-file dedupe only needed for the biggest globals which get a lot of duplicates
        if f in ('Kernels.java', 'Common.java', 'Batchmanager.java', 'Executor.java',
                 'Layers.java', 'Plugins.java', 'Thop.java', 'TrtllmRuntime.java',
                 'CacheTransceiverComm.java'):
            s = dedupe_constructors_and_methods_per_file(s)
        if s != orig:
            with open(fn, 'w') as fh: fh.write(s)
            total += 1
print('fixed', total, 'files')

