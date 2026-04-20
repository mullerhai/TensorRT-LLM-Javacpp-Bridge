#!/usr/bin/env python3
"""Fix leaked C++ types in JavaCPP-generated Java files."""
import os, re, glob

# Patterns that indicate leaked C++ types in Java code
LEAKED_PATTERNS = [
    r'std::future<',
    r'std::vector<[A-Za-z]',
    r'std::enable_shared_from_this<',
    r'c10::intrusive_ptr<',
    r'c10d::',
    r'(?<!@Cast\(")(?<!@Namespace\(")(?<!@Name\(")tensorrt_llm::batch_manager::\w+::\w+::\w+',
    r'(?<!")tensorrt_llm::batch_manager::kv_cache_manager::OptionalRef',
    r'(?<!")tensorrt_llm::batch_manager::HandleContextLogits::OptionalRef',
    r'(?<!")tensorrt_llm::batch_manager::HandleGenerationLogits::OptionalRef',
    r'(?<!")tensorrt_llm::batch_manager::AllocateKvCache::OptionalRef',
    r'(?<!")tensorrt_llm::batch_manager::DecoderInputBuffers::TensorConstPtr',
    r'(?<!")tensorrt_llm::batch_manager::utils::CudaGraphExecutor',
]

combined = re.compile('|'.join(LEAKED_PATTERNS))

dirs = [
    'src/main/java/tensorrt_llm/batch_manager/*.java',
    'src/main/java/tensorrt_llm/global/*.java',
    'src/main/java/tensorrt_llm/runtime/*.java',
    'src/main/java/tensorrt_llm/executor/*.java',
    'src/main/java/tensorrt_llm/layers/*.java',
    'src/main/java/tensorrt_llm/plugins/*.java',
    'src/main/java/tensorrt_llm/common/*.java',
]

for pattern in dirs:
    for f in glob.glob(pattern):
        with open(f, 'r') as fh:
            lines = fh.readlines()
        changed = False
        new_lines = []
        for line in lines:
            stripped = line.strip()
            # Skip comment lines
            if stripped.startswith('//') or stripped.startswith('/*') or stripped.startswith('*'):
                new_lines.append(line)
                continue
            # Skip safe lines
            if stripped.startswith('package') or stripped.startswith('import') or stripped.startswith('@Namespace'):
                new_lines.append(line)
                continue
            # Remove text inside @Cast("...") before checking
            cleaned = re.sub(r'@Cast\("[^"]*"\)', '', line)
            cleaned = re.sub(r'@Name\("[^"]*"\)', '', cleaned)
            cleaned = re.sub(r'@ByVal\(nullValue\s*=\s*"[^"]*"\)', '', cleaned)

            if combined.search(cleaned):
                new_lines.append('    // SKIPPED (leaked C++ type): ' + stripped + '\n')
                changed = True
                continue
            new_lines.append(line)
        if changed:
            with open(f, 'w') as fh:
                fh.writelines(new_lines)
            print(f'Fixed: {os.path.basename(f)}')

print("Done.")

