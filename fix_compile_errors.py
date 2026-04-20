#!/usr/bin/env python3
"""Fix remaining compilation errors in generated Java files."""
import re, os, glob

BASE = 'src/main/java/tensorrt_llm'

# 1. Fix LlmRequest.java - remove bad import
def fix_llm_request():
    path = f'{BASE}/batch_manager/LlmRequest.java'
    with open(path, 'r') as f:
        content = f.read()
    content = content.replace('import org.bytedeco.tensorrt_llm.Request;\n', '// import org.bytedeco.tensorrt_llm.Request; // SKIPPED: package does not exist\n')
    with open(path, 'w') as f:
        f.write(content)
    print(f'Fixed: {path}')

# 2. Fix BufferManager.java - wrong MemoryType import
def fix_buffer_manager():
    path = f'{BASE}/runtime/BufferManager.java'
    with open(path, 'r') as f:
        content = f.read()
    content = content.replace('import java.lang.management.MemoryType;\n', '')
    with open(path, 'w') as f:
        f.write(content)
    print(f'Fixed: {path}')

# 3. Fix Common.java - comment out lines with leaked C++ types
def fix_common():
    path = f'{BASE}/global/Common.java'
    with open(path, 'r') as f:
        lines = f.readlines()

    leaked_types = ['TRTLLM_NAMESPACE_BEGIN', 'ncclComm_t', 'CUDADriverWrapper',
                    'AttentionMaskType', 'PositionEmbeddingType', 'RotaryScalingType',
                    'BlockSparseParams', 'MlaMetaParams', 'SparseAttentionParams']

    new_lines = []
    for line in lines:
        stripped = line.strip()
        # Skip empty or already commented lines
        if not stripped or stripped.startswith('//') or stripped.startswith('/*') or stripped.startswith('*'):
            new_lines.append(line)
            continue

        should_comment = False
        for lt in leaked_types:
            if lt in stripped and ('native' in stripped or 'public' in stripped):
                should_comment = True
                break

        if should_comment:
            new_lines.append('    // SKIPPED (leaked C++ type): ' + stripped + '\n')
        else:
            new_lines.append(line)

    with open(path, 'w') as f:
        f.writelines(new_lines)
    print(f'Fixed: {path}')

# 4. Fix Batchmanager.java - comment out lines with leaked C++ types
def fix_batchmanager():
    path = f'{BASE}/global/Batchmanager.java'
    with open(path, 'r') as f:
        lines = f.readlines()

    leaked_types = ['CUstream', 'TensorMap', 'UnderlyingType', 'IterationType',
                    'CacheState', 'DebugConfig', 'AdditionalModelOutput', 'MmKey',
                    'TargetRanksInfo', 'ncclComm_t']

    new_lines = []
    for line in lines:
        stripped = line.strip()
        if not stripped or stripped.startswith('//') or stripped.startswith('/*') or stripped.startswith('*'):
            new_lines.append(line)
            continue

        should_comment = False
        for lt in leaked_types:
            # Match as a type reference (not as part of a longer word)
            if re.search(r'\b' + re.escape(lt) + r'\b', stripped) and ('native' in stripped or 'public' in stripped):
                should_comment = True
                break

        if should_comment:
            new_lines.append('    // SKIPPED (leaked C++ type): ' + stripped + '\n')
        else:
            new_lines.append(line)

    with open(path, 'w') as f:
        f.writelines(new_lines)
    print(f'Fixed: {path}')

# 5. Fix RuntimeBuffers.java - add missing imports and comment out bad refs
def fix_runtime_buffers():
    path = f'{BASE}/batch_manager/RuntimeBuffers.java'
    with open(path, 'r') as f:
        content = f.read()

    # Add missing imports if not present
    imports_needed = [
        'import tensorrt_llm.runtime.WorldConfig;',
        'import tensorrt_llm.runtime.ModelConfig;',
    ]
    for imp in imports_needed:
        if imp not in content:
            content = content.replace(
                'import static tensorrt_llm.global.Batchmanager.*;',
                imp + '\n' + 'import static tensorrt_llm.global.Batchmanager.*;'
            )

    # Check if Inputs class is missing - it might be ExplicitDraftTokensBuffers.Inputs
    # Comment out references to bare Inputs if the class doesn't exist
    with open(path, 'w') as f:
        f.write(content)
    print(f'Fixed: {path}')

# 6. Fix Batchmanager.java - add missing imports for ITensor, WorldConfig
def fix_batchmanager_imports():
    path = f'{BASE}/global/Batchmanager.java'
    with open(path, 'r') as f:
        content = f.read()

    imports_needed = [
        'import tensorrt_llm.runtime.ITensor;',
        'import tensorrt_llm.runtime.WorldConfig;',
    ]

    for imp in imports_needed:
        if imp not in content:
            # Add after last import line
            content = content.replace(
                'import static tensorrt_llm.global.Batchmanager.*;',
                imp + '\nimport static tensorrt_llm.global.Batchmanager.*;'
            )

    with open(path, 'w') as f:
        f.write(content)
    print(f'Fixed imports: {path}')

fix_llm_request()
fix_buffer_manager()
fix_common()
fix_batchmanager()
fix_runtime_buffers()
fix_batchmanager_imports()

