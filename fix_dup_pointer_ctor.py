#!/usr/bin/env python3
"""Round 3: drop duplicate (Pointer) allocating ctors that collide with
the Pointer-cast ctor after type stripping."""
import re, os
ROOT='/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/src/main/java/tensorrt_llm'

ANN = r'(?:@[A-Za-z_]\w*(?:\([^)]*\))?\s+)*'

total = 0
for dp,_,fs in os.walk(ROOT):
    for f in fs:
        if not f.endswith('.java'): continue
        fn=os.path.join(dp,f)
        with open(fn) as fh: s=fh.read()
        orig=s
        cls = f[:-5]
        pat = re.compile(
            r'[ \t]*' + ANN + r'public\s+' + re.escape(cls) +
            r'\s*\(\s*' + ANN + r'Pointer\s+\w+\s*\)\s*\{\s*super\(\(Pointer\)null\);\s*allocate\([^)]*\);\s*\}\s*\n'
            r'[ \t]*' + ANN + r'private\s+native\s+void\s+allocate\(\s*' + ANN + r'Pointer\s+\w+\s*\);\s*\n'
        )
        s2, n = pat.subn('', s)
        if n:
            s = s2
            total += n
            print(f'{fn}: removed {n}')
        cast_pat = re.compile(r'public\s+' + re.escape(cls) + r'\s*\(\s*Pointer\s+\w+\s*\)\s*\{\s*super\(\s*\w+\s*\);\s*\}')
        if n and not cast_pat.search(s):
            inject = f'    /** Pointer cast constructor. Invokes {{@link Pointer#Pointer(Pointer)}}. */\n    public {cls}(Pointer p) {{ super(p); }}\n'
            s3, m = re.subn(r'(static\s*\{\s*Loader\.load\(\);\s*\}\s*\n)', r'\1' + inject, s, count=1)
            if m:
                s = s3
                print(f'{fn}: injected cast ctor')
            else:
                s3, m = re.subn(r'(public\s+class\s+' + re.escape(cls) + r'\s+extends\s+\w+\s*\{\s*\n)', r'\1' + inject, s, count=1)
                if m:
                    s = s3
                    print(f'{fn}: injected cast ctor (no Loader)')
        if s!=orig:
            with open(fn,'w') as fh: fh.write(s)
print(f'done; total removed {total}')

