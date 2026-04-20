#!/usr/bin/env python3
"""Balance unbalanced angle brackets inside @Cast("...") annotations."""
import os
import re

SRC = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                   "src", "main", "java", "tensorrt_llm")
CAST_RE = re.compile(r'@Cast\("([^"]*)"\)')

fixed_count = 0
files_changed = 0


def balance(match):
    global fixed_count
    s = match.group(1)
    opens = s.count('<')
    closes = s.count('>')
    if opens <= closes:
        return match.group(0)
    tm = re.search(r'\*+$', s)
    if not tm:
        return match.group(0)
    missing = '>' * (opens - closes)
    new = s[:tm.start()] + missing + s[tm.start():]
    fixed_count += 1
    return '@Cast("' + new + '")'


for root, _dirs, files in os.walk(SRC):
    for name in files:
        if not name.endswith('.java'):
            continue
        path = os.path.join(root, name)
        with open(path, 'r', encoding='utf-8') as fp:
            original = fp.read()
        updated = CAST_RE.sub(balance, original)
        if updated != original:
            files_changed += 1
            with open(path, 'w', encoding='utf-8') as fp:
                fp.write(updated)

print("balanced %d casts across %d files" % (fixed_count, files_changed))

