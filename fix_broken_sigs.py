#!/usr/bin/env python3
"""Fix broken method signatures where ); appears mid-parameter-list."""
import os, re, glob

SRC = "src/main/java"

def fix_file(path):
    with open(path, 'r') as f:
        lines = f.readlines()

    changed = False
    i = 0
    while i < len(lines) - 1:
        line = lines[i]
        next_line = lines[i + 1]

        # Pattern: line ends with ");$" but next line starts with whitespace + @ or identifier
        # that looks like a continuation parameter (not a new declaration)
        stripped = line.rstrip()
        next_stripped = next_line.strip()

        if stripped.endswith(');') and next_stripped and not next_stripped.startswith('//') \
           and not next_stripped.startswith('/*') and not next_stripped.startswith('*') \
           and not next_stripped.startswith('}') and not next_stripped.startswith('{') \
           and not next_stripped.startswith('public') and not next_stripped.startswith('private') \
           and not next_stripped.startswith('protected') and not next_stripped.startswith('@Namespace') \
           and not next_stripped.startswith('@Override') and not next_stripped.startswith('static') \
           and not next_stripped.startswith('return') and not next_stripped.startswith('//') \
           and not next_stripped.startswith('class ') and not next_stripped.startswith('import ') \
           and not next_stripped.startswith('package ') and not next_stripped.startswith('// ') \
           and not next_stripped.startswith('/**') and not next_stripped.startswith('native') \
           and not next_stripped == '':

            # Check if next line looks like continuation params (has @SharedPtr, @Const, @ByRef, @ByVal, @Cast, @Optional, boolean, int, long, etc.)
            param_indicators = ['@SharedPtr', '@Const', '@ByRef', '@ByVal', '@Cast', '@Optional',
                                '@StdVector', '@MemberGetter', 'boolean ', 'int ', 'long ', 'double ', 'float ',
                                'SharedConstPtr', 'SharedPtr', 'Pointer ', 'BufferManager', 'TargetRanksInfo',
                                'DecodingLayerWorkspace', 'CudaStream']

            if any(ind in next_stripped for ind in param_indicators):
                # Replace ); with , at end of current line
                lines[i] = stripped[:-2] + ',\n'
                changed = True
                i += 1
                continue

        i += 1

    if changed:
        with open(path, 'w') as f:
            f.writelines(lines)
        print(f"Fixed: {path}")

for path in glob.glob(os.path.join(SRC, "**/*.java"), recursive=True):
    fix_file(path)

