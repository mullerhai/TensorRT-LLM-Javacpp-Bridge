import os

BASE = "/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/src/main/java/tensorrt_llm/presets"
INCLUDE = "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include"
CPP = "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp"
STUBS = "/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/cuda-stubs"

COMMON_SKIP = '''        infoMap.put(new Info("std::optional").skip());
        infoMap.put(new Info("std::variant").skip());
        infoMap.put(new Info("std::function").skip());
        infoMap.put(new Info("std::promise").skip());
        infoMap.put(new Info("std::shared_ptr").skip());
        infoMap.put(new Info("std::unique_ptr").skip());
        infoMap.put(new Info("std::atomic").skip());
        infoMap.put(new Info("std::mutex").skip());
        infoMap.put(new Info("std::condition_variable").skip());
        infoMap.put(new Info("std::thread").skip());
        infoMap.put(new Info("nvinfer1::ILogger").pointerTypes("Pointer"));
        infoMap.put(new Info("cudaStream_t").valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("cudaEvent_t").valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::common::getDTypeSize", "tensorrt_llm::common::getDTypeSizeInBits", "tensorrt_llm::common::getDtypeString").skip());'''

def find_headers(subdir):
    result = []
    d = os.path.join(INCLUDE, "tensorrt_llm", subdir)
    if os.path.isdir(d):
        for root, dirs, files in os.walk(d):
            for f in sorted(files):
                if f.endswith(".h"):
                    rel = os.path.relpath(os.path.join(root, f), INCLUDE)
                    result.append(rel)
    d2 = os.path.join(CPP, "tensorrt_llm", subdir)
    if os.path.isdir(d2):
        for root, dirs, files in os.walk(d2):
            for f in sorted(files):
                if f.endswith(".h"):
                    rel = os.path.relpath(os.path.join(root, f), CPP)
                    if rel not in result:
                        result.append(rel)
    return sorted(result)

configs = {
    "RuntimeConfig": ("runtime", "tensorrt_llm.runtime", "tensorrt_llm.global.TrtllmRuntime"),
    "BatchmanagerConfig": ("batch_manager", "tensorrt_llm.batch_manager", "tensorrt_llm.global.Batchmanager"),
    "LayersConfig": ("layers", "tensorrt_llm.layers", "tensorrt_llm.global.Layers"),
    "ExecutorConfig": ("executor", "tensorrt_llm.executor", "tensorrt_llm.global.Executor"),
    "KernelsConfig": ("kernels", "tensorrt_llm.kernels", "tensorrt_llm.global.Kernels"),
    "CommonConfig": ("common", "tensorrt_llm.common", "tensorrt_llm.global.Common"),
    "PluginsConfig": ("plugins", "tensorrt_llm.plugins", "tensorrt_llm.global.Plugins"),
    "ThopConfig": ("thop", "tensorrt_llm.thop", "tensorrt_llm.global.Thop"),
    "CutlassextensionsConfig": ("cutlass_extensions", "tensorrt_llm.cutlass_extensions", "tensorrt_llm.global.CutlassExtensions"),
}

for name, (subdir, target, globalClass) in configs.items():
    headers = find_headers(subdir)
    if not headers:
        print(f"WARNING: No headers for {name} ({subdir})")
        continue

    includes_str = ",\n".join([f'            "{h}"' for h in headers])

    java = f'''package tensorrt_llm.presets;

import org.bytedeco.javacpp.annotation.Platform;
import org.bytedeco.javacpp.annotation.Properties;
import org.bytedeco.javacpp.tools.Info;
import org.bytedeco.javacpp.tools.InfoMap;
import org.bytedeco.javacpp.tools.InfoMapper;

@Properties(
    value = @Platform(
        includepath = {{"{INCLUDE}", "{CPP}", "{STUBS}"}},
        include = {{
{includes_str}
        }}
    ),
    target = "{target}",
    global = "{globalClass}"
)
public class {name} implements InfoMapper {{
    @Override
    public void map(InfoMap infoMap) {{
{COMMON_SKIP}
    }}
}}
'''
    path = os.path.join(BASE, f"{name}.java")
    with open(path, 'w') as f:
        f.write(java)
    print(f"Written {name}.java with {len(headers)} headers")

print("Done!")

