#!/usr/bin/env python3
"""Round 2: drop half-param decls, dedupe imports, dedupe ctors."""
import re, os
ROOT='/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/src/main/java/tensorrt_llm'

for dp,_,fs in os.walk(ROOT):
    for f in fs:
        if not f.endswith('.java'): continue
        fn=os.path.join(dp,f)
        with open(fn) as fh: s=fh.read()
        orig=s
        # 1) Remove native decls containing raw "half " param type
        s = re.sub(r'(?s)(?:@Namespace\([^)]*\)\s*)?public\s+(?:static\s+)?native\s+[^;]*?\bhalf\s+[^;]*?;\s*\n', '', s)
        # 2) Deduplicate imports within file (first wins)
        lines=s.split('\n'); seen=set(); new=[]
        for ln in lines:
            m=re.match(r'import\s+([\w\.]+);',ln)
            if m:
                key=m.group(1).split('.')[-1]
                if key in seen: continue
                seen.add(key)
            new.append(ln)
        s='\n'.join(new)
        # 3) Dedupe Pointer-cast constructors
        s = re.sub(r'(public\s+(\w+)\(Pointer\s+p\)\s*\{\s*super\(p\);\s*\})\s*\n\s*\1', r'\1', s)
        if s!=orig:
            with open(fn,'w') as fh: fh.write(s)
print('done')

