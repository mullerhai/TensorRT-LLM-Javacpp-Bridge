#!/usr/bin/env python3
"""
Fix Kernels.java: repair broken multi-line native method declarations,
remove malformed overloads, and fix invalid Java parameter types/names.
"""
import re
import sys

INPUT  = "src/main/java/tensorrt_llm/global/Kernels.java"
OUTPUT = INPUT

with open(INPUT, "r") as f:
    lines = f.readlines()

out = []
i = 0
while i < len(lines):
    line = lines[i]

    # ----------------------------------------------------------------
    # 1.  Remove malformed overloads that start with "addCumLogProbs(,"
    #     or "gatherId(," -- these are broken second-overload stubs.
    # ----------------------------------------------------------------
    stripped = line.rstrip('\n')
    if re.search(r'public static native \w+\s+\w+\(,', stripped):
        # Skip this line and all continuation lines until a line that
        # ends with ');' and is NOT a continuation (i.e. it starts with
        # @Namespace / public, or a blank line / comment follows).
        # Simplest: skip until we pass a line ending ');' that has
        # indentation level 0 (no leading spaces on the closing ).
        j = i
        while j < len(lines):
            ln = lines[j].rstrip('\n')
            j += 1
            if ln.rstrip().endswith(');') and not ln.startswith('    '):
                break
            if ln.rstrip().endswith(');'):
                break  # continuation ');' -- end of this bad overload
        i = j
        continue

    # ----------------------------------------------------------------
    # 2.  Orphaned continuation lines.
    #     Detect: current line ends with ');' (closing a method early),
    #     and the NEXT line is an indented parameter continuation.
    # ----------------------------------------------------------------
    def is_orphaned_continuation(l):
        s = l.rstrip('\n')
        # Continuation lines start with 4+ spaces and contain Java param
        # syntax but are NOT a @Namespace method start.
        if not s.startswith('    '):
            return False
        if s.lstrip().startswith('@Namespace'):
            return False
        if s.lstrip().startswith('//'):
            return False
        # Must contain at least one comma or end with ');'
        return s.rstrip().endswith(');') or ',' in s

    if stripped.rstrip().endswith(');') and (i + 1) < len(lines):
        # Look ahead: are there orphaned continuation lines?
        continuations = []
        j = i + 1
        while j < len(lines) and is_orphaned_continuation(lines[j]):
            continuations.append(lines[j].rstrip('\n').strip())
            j += 1

        if continuations:
            # Remove the premature closing ')' from current line
            fixed = stripped.rstrip()
            if fixed.endswith(');'):
                fixed = fixed[:-2]  # remove ');'
            # Append continuation, then final ');'
            # The last continuation already has ');' -- keep it
            merged = fixed + ',\n'
            for k, cont in enumerate(continuations):
                if k == len(continuations) - 1:
                    merged += '    ' + cont + '\n'
                else:
                    merged += '    ' + cont + '\n'
            out.append(merged)
            i = j
            continue

    # ----------------------------------------------------------------
    # 3.  Fix `__restrict/*paramName*/` -- rename parameter to the
    #     comment content, e.g. __restrict/*pStage1LogProbs*/ -> pStage1LogProbs
    # ----------------------------------------------------------------
    line = re.sub(r'__restrict/\*(\w+)\*/', r'\1', line)

    # ----------------------------------------------------------------
    # 4.  Fix remaining bare `__restrict` parameter names (duplicates).
    #     Replace them with positional names _p1, _p2 ...
    # ----------------------------------------------------------------
    count = [0]
    def replace_restrict(m):
        count[0] += 1
        return f'_p{count[0]}'
    line = re.sub(r'\b__restrict\b', replace_restrict, line)

    # ----------------------------------------------------------------
    # 5.  Replace `half ` type (CUDA half-precision, unknown in Java)
    #     with ShortPointer.
    # ----------------------------------------------------------------
    line = re.sub(r'\bhalf\b(?!\w)', 'ShortPointer', line)

    # ----------------------------------------------------------------
    # 6.  Fix setupTopKTopPRuntimeArgOne duplicate empty declarations.
    #     Keep only the first declaration; remove duplicate stubs that
    #     follow comment lines containing "[CPP-FIX]".
    # ----------------------------------------------------------------
    # (handled naturally: the duplicate lines have ');' and the
    # comment lines are just comments -- no extra action needed here
    # because the duplication is complete valid stubs)

    out.append(line)
    i += 1

with open(OUTPUT, "w") as f:
    f.writelines(out)

print(f"Fixed {INPUT}")

