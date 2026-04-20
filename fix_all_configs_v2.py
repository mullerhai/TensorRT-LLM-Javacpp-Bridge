#!/usr/bin/env python3
"""Fix all JavaCPP preset configs with proper Info mappings."""
import os
import subprocess

BASE = "/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/src/main/java/tensorrt_llm/presets"
INCLUDE = "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include"
CPP = "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp"
STUBS = "/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/cuda-stubs"

# Common Info mappings that all configs need
COMMON_INFO = '''        // Type aliases -> Java primitives
        infoMap.put(new Info("tensorrt_llm::runtime::SizeType32", "SizeType32", "runtime::SizeType32").valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::TokenIdType", "TokenIdType").valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("std::int32_t", "int32_t").valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("std::int64_t", "int64_t").valueTypes("long").pointerTypes("LongPointer"));
        infoMap.put(new Info("std::uint32_t", "uint32_t").valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("std::uint64_t", "uint64_t").valueTypes("long").pointerTypes("LongPointer"));
        infoMap.put(new Info("std::size_t", "size_t").valueTypes("long").pointerTypes("LongPointer"));
        infoMap.put(new Info("bool").valueTypes("boolean").pointerTypes("BoolPointer"));

        // C++ auto constexpr string literals -> String
        infoMap.put(new Info("auto").valueTypes("String").pointerTypes("@Cast(\\"const char*\\") BytePointer"));

        // Skip unparseable C++ constructs
        infoMap.put(new Info("std::optional").skip());
        infoMap.put(new Info("std::variant").skip());
        infoMap.put(new Info("std::function").skip());
        infoMap.put(new Info("std::promise").skip());
        infoMap.put(new Info("std::shared_ptr").annotations("@SharedPtr"));
        infoMap.put(new Info("std::unique_ptr").annotations("@UniquePtr"));
        infoMap.put(new Info("std::atomic").skip());
        infoMap.put(new Info("std::mutex").skip());
        infoMap.put(new Info("std::condition_variable").skip());
        infoMap.put(new Info("std::thread").skip());
        infoMap.put(new Info("std::unordered_map").skip());
        infoMap.put(new Info("std::map").skip());
        infoMap.put(new Info("std::tuple").skip());
        infoMap.put(new Info("std::pair").skip());

        // CUDA types
        infoMap.put(new Info("nvinfer1::ILogger").pointerTypes("Pointer"));
        infoMap.put(new Info("cudaStream_t").valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("cudaEvent_t").valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("CUmemPoolHandle_st").pointerTypes("Pointer"));

        // Skip problematic functions
        infoMap.put(new Info("tensorrt_llm::common::getDTypeSize", "tensorrt_llm::common::getDTypeSizeInBits", "tensorrt_llm::common::getDtypeString").skip());'''

def find_headers(subdir, include_only=False):
    result = []
    d = os.path.join(INCLUDE, "tensorrt_llm", subdir)
    if os.path.isdir(d):
        for root, dirs, files in os.walk(d):
            for f in sorted(files):
                if f.endswith(".h"):
                    rel = os.path.relpath(os.path.join(root, f), INCLUDE)
                    result.append(rel)
    if not include_only:
        d2 = os.path.join(CPP, "tensorrt_llm", subdir)
        if os.path.isdir(d2):
            for root, dirs, files in os.walk(d2):
                for f in sorted(files):
                    if f.endswith(".h"):
                        rel = os.path.relpath(os.path.join(root, f), CPP)
                        if rel not in result:
                            result.append(rel)
    return sorted(result)

def write_config(name, headers, target, globalClass, extra_info=""):
    includes_str = ",\n".join([f'            "{h}"' for h in headers])
    java = f'''package tensorrt_llm.presets;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;
import org.bytedeco.javacpp.tools.*;

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
{COMMON_INFO}
{extra_info}
    }}
}}
'''
    path = os.path.join(BASE, f"{name}.java")
    with open(path, 'w') as f:
        f.write(java)
    print(f"Written {name}.java with {len(headers)} headers")

# ====== BATCH_MANAGER ======
headers = find_headers("batch_manager")
write_config("BatchmanagerConfig", headers,
    "tensorrt_llm.batch_manager", "tensorrt_llm.global.Batchmanager")

# ====== RUNTIME ======
headers = find_headers("runtime")
write_config("RuntimeConfig", headers,
    "tensorrt_llm.runtime", "tensorrt_llm.global.TrtllmRuntime")

# ====== LAYERS ======
headers = find_headers("layers")
write_config("LayersConfig", headers,
    "tensorrt_llm.layers", "tensorrt_llm.global.Layers")

# ====== EXECUTOR ======
headers = find_headers("executor")
write_config("ExecutorConfig", headers,
    "tensorrt_llm.executor", "tensorrt_llm.global.Executor")

# ====== COMMON ======
headers = find_headers("common")
write_config("CommonConfig", headers,
    "tensorrt_llm.common", "tensorrt_llm.global.Common")

# ====== PLUGINS ======
headers = find_headers("plugins")
write_config("PluginsConfig", headers,
    "tensorrt_llm.plugins", "tensorrt_llm.global.Plugins")

# ====== KERNELS - use public headers + selected internal ones (skip cutlass_kernels) ======
public_kernel_headers = find_headers("kernels", include_only=True)
# Add select internal kernel headers that don't use cutlass templates
internal_kernel_dir = os.path.join(CPP, "tensorrt_llm", "kernels")
skip_dirs = {"cutlass_kernels", "trtllmGenKernels", "communicationKernels", "cuteDslKernels"}
internal_kernels = []
if os.path.isdir(internal_kernel_dir):
    for f in sorted(os.listdir(internal_kernel_dir)):
        if f.endswith(".h") and os.path.isfile(os.path.join(internal_kernel_dir, f)):
            rel = f"tensorrt_llm/kernels/{f}"
            if rel not in public_kernel_headers:
                internal_kernels.append(rel)
all_kernel_headers = sorted(set(public_kernel_headers + internal_kernels))
write_config("KernelsConfig", all_kernel_headers,
    "tensorrt_llm.kernels", "tensorrt_llm.global.Kernels")

# ====== THOP - requires torch headers, create stub that wraps available headers ======
# thop depends on PyTorch C++ (ATen), which we don't have. We need to create a minimal stub.
# Only parse headers that don't require torch
thop_dir = os.path.join(CPP, "tensorrt_llm", "thop")
thop_headers = []
if os.path.isdir(thop_dir):
    for f in sorted(os.listdir(thop_dir)):
        if f.endswith(".h"):
            thop_headers.append(f"tensorrt_llm/thop/{f}")

# Check which headers need torch
parseable_thop = []
for h in thop_headers:
    full = os.path.join(CPP, h)
    with open(full) as hf:
        content = hf.read()
    if "torch/" not in content and "ATen/" not in content:
        parseable_thop.append(h)

if parseable_thop:
    write_config("ThopConfig", parseable_thop,
        "tensorrt_llm.thop", "tensorrt_llm.global.Thop")
else:
    # All thop headers need torch - create config with all headers but skip torch deps
    write_config("ThopConfig", thop_headers,
        "tensorrt_llm.thop", "tensorrt_llm.global.Thop",
        '        // Skip torch-dependent types\n        infoMap.put(new Info("at::Tensor", "torch::Tensor", "c10::ScalarType").skip());\n        infoMap.put(new Info("ATen/cuda/CUDAContext.h", "torch/custom_class.h", "torch/extension.h", "torch/script.h").skip());')
    print("WARNING: All thop headers require PyTorch C++ - these will likely fail to parse")

# ====== CUTLASS_EXTENSIONS - only top-level headers ======
cutlass_headers = [
    "tensorrt_llm/cutlass_extensions/include/cutlass_extensions/epilogue_helpers.h",
    "tensorrt_llm/cutlass_extensions/include/cutlass_extensions/tile_interleaved_layout.h",
]
write_config("CutlassextensionsConfig", cutlass_headers,
    "tensorrt_llm.cutlass_extensions", "tensorrt_llm.global.CutlassExtensions")

print("\nAll configs written! Now compile and parse...")

