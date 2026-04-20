#!/usr/bin/env python3
"""Close unterminated method signatures where the closing `);` was commented
out because it sat on a `// [CPP-FIX]` line together with an invalid param."""
import re, os, glob

ROOT = '/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/src/main/java/tensorrt_llm'

def fix(fn):
    with open(fn) as f:
        lines = f.readlines()
    out = []
    i = 0
    n = len(lines)
    changed = False
    while i < n:
        line = lines[i]
        # method-start heuristic: contains `(` and `native`/`public`/`allocate`, no `);`
        if ('(' in line and
            (' native ' in line or line.lstrip().startswith('public ') or line.lstrip().startswith('private ')) and
            ');' not in line and '{' not in line.rstrip()):
            # scan forward: collect real (non-comment) lines & commented lines
            buf = [i]
            j = i + 1
            last_real = i
            found_close = False
            while j < n:
                stripped = lines[j].lstrip()
                if not stripped:
                    break
                if stripped.startswith('//'):
                    buf.append(j)
                    j += 1
                    continue
                buf.append(j)
                last_real = j
                if ');' in lines[j]:
                    found_close = True
                    break
                # stop if next public/private/class/@Namespace (top-level)
                if re.match(r'(public |private |protected |@Namespace|@Properties|\}|class )', stripped):
                    break
                j += 1
            if not found_close:
                # terminate the last real line: replace trailing `,` with `);`
                last = lines[last_real].rstrip()
                if last.endswith(','):
                    lines[last_real] = last[:-1] + ');\n'
                    changed = True
                elif last.endswith(')'):
                    lines[last_real] = last + ';\n'
                    changed = True
                else:
                    # append `);`
                    lines[last_real] = last + ');\n'
                    changed = True
                # drop commented-out tail lines between last_real+1 .. j-1
                # (keep them as they are; they're harmless comments)
        out.append(lines[i])
        i += 1
    if changed:
        with open(fn, 'w') as f:
            f.writelines(lines)
    return changed

total = 0
for dp, _, fs in os.walk(ROOT):
    for f in fs:
        if f.endswith('.java') and fix(os.path.join(dp, f)):
            total += 1
print(f'terminated sigs in {total} files')

