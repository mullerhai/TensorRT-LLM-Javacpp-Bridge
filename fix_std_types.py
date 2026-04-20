import os

files = [
    'src/main/java/org/bytedeco/tensorrt_llm/executor/AgentConnection.java',
    'src/main/java/org/bytedeco/tensorrt_llm/executor/AgentConnectionManager.java',
    'src/main/java/org/bytedeco/tensorrt_llm/executor/BaseAgentConfig.java',
    'src/main/java/org/bytedeco/tensorrt_llm/executor/BaseTransferAgent.java',
    'src/main/java/org/bytedeco/tensorrt_llm/executor/DataContext.java',
    'src/main/java/org/bytedeco/tensorrt_llm/executor/DebugTensorsPerIteration.java',
    'src/main/java/org/bytedeco/tensorrt_llm/executor/DisaggExecutorOrchestrator.java',
    'src/main/java/org/bytedeco/tensorrt_llm/executor/DynamicBatchConfig.java'
]

for filepath in files:
    if not os.path.exists(filepath): continue
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    lines = content.split('\n')
    lines_to_delete = set()
    for i, line in enumerate(lines):
        if 'std::' in line:
            lines_to_delete.add(i)
            # check backwards
            j = i - 1
            while j >= 0:
                if not lines[j].strip():
                    j -= 1
                    continue
                if lines[j].strip().endswith(';') or lines[j].strip().endswith('}') or lines[j].strip().endswith('{'):
                    break
                lines_to_delete.add(j)
                if 'public ' in lines[j] or 'private ' in lines[j]:
                    break
                j -= 1
            # check forwards
            j = i + 1
            while j < len(lines):
                lines_to_delete.add(j)
                if lines[j].strip().endswith(';') or lines[j].strip().endswith('}'):
                    break
                j += 1

    new_content = '\n'.join([line for i, line in enumerate(lines) if i not in lines_to_delete])
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(new_content)
    print(f'Fixed {filepath}')
