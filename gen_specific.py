import os
import re

cpp_dir = "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/tensorrt_llm"
include_dir = "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include"

packages = [
    "thop", "deep_gemm", "cutlass_extensions", "deep_ep",
    "executor",
    "kernels",
    "common",
    "plugins",
    "layers"
]

EXCLUDE_PATTERNS = [
    "template",
    "sm90",
    "sm89",
    "sm100",
    "/cutlass_kernels/",
    "/cuteDslKernels/", "cubin", "launch", "samplingTopKKernels", "samplingTopPKernels", "banRepeatNgram", "selectiveScan", "trtllmGenKernels", "unfusedAttentionKernels", "weightOnlyBatchedGemv", "epilogue", "gemm", "transform"
]

def get_headers(pkg):
    headers = []

    # 1. cpp_dir/pkg
    if os.path.exists(os.path.join(cpp_dir, pkg)):
        for root, dirs, files in os.walk(os.path.join(cpp_dir, pkg)):
            for f in files:
                if f.endswith('.h'):
                    skip = False
                    abs_path = os.path.join(root, f)
                    lower_path = abs_path.lower()
                    for pat in EXCLUDE_PATTERNS:
                        if pat.lower() in lower_path:
                            skip = True
                            break
                    if not skip:
                        rel = os.path.relpath(abs_path, "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp")
                        headers.append(rel)

    # 2. include_dir/tensorrt_llm/pkg
    if os.path.exists(os.path.join(include_dir, "tensorrt_llm", pkg)):
        for root, dirs, files in os.walk(os.path.join(include_dir, "tensorrt_llm", pkg)):
            for f in files:
                if f.endswith('.h'):
                    skip = False
                    abs_path = os.path.join(root, f)
                    lower_path = abs_path.lower()
                    for pat in EXCLUDE_PATTERNS:
                        if pat.lower() in lower_path:
                            skip = True
                            break
                    if not skip:
                        rel = os.path.relpath(abs_path, include_dir)
                        headers.append(rel)

    return sorted(list(set(headers)))

template = """package tensorrt_llm.presets;

import org.bytedeco.javacpp.annotation.Platform;
import org.bytedeco.javacpp.annotation.Properties;
import org.bytedeco.javacpp.tools.Info;
import org.bytedeco.javacpp.tools.InfoMap;
import org.bytedeco.javacpp.tools.InfoMapper;

@Properties(
    value = @Platform(
        includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include", "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp", "/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/cuda-stubs"},
        include = {
{includes}
        }
    ),
    target = "org.bytedeco.tensorrt_llm.{pkg_name}",
    global = "org.bytedeco.tensorrt_llm.global.{pkg_name_capitalized}"
)
public class {config_name} implements InfoMapper {
    @Override
    public void map(InfoMap infoMap) {
        infoMap.put(new Info("std::optional").skip());
        infoMap.put(new Info("std::variant").skip());
        infoMap.put(new Info("std::function").skip());
        infoMap.put(new Info("std::promise").skip());

        infoMap.put(new Info("tensorrt_llm::common::getDTypeSize", "tensorrt_llm::common::getDTypeSizeInBits", "tensorrt_llm::common::getDtypeString").skip());
    }
}
"""

all_configs = []
if not os.path.exists("src/main/java/tensorrt_llm/presets"):
    os.makedirs("src/main/java/tensorrt_llm/presets", exist_ok=True)

for pkg in packages:
    headers = get_headers(pkg)
    if not headers:
        print(f"Skipping {pkg} : No headers")
        continue

    valid_headers = []
    for h in headers:
        if "pybind" not in h and "test" not in h:
            valid_headers.append(h)

    if not valid_headers:
        continue

    incs = ",\n".join([f'            "{h}"' for h in valid_headers])

    config_name = pkg.capitalize().replace("_", "") + "Config"
    pkg_name = pkg.lower()
    pkg_name_capitalized = pkg_name.capitalize().replace("_", "")

    content = template.replace("{includes}", incs)\
                      .replace("{pkg_name}", pkg_name)\
                      .replace("{pkg_name_capitalized}", pkg_name_capitalized)\
                      .replace("{config_name}", config_name)

    with open(f"src/main/java/tensorrt_llm/presets/{config_name}.java", "w") as f:
        f.write(content)

    all_configs.append(config_name)

print(f"Generated {len(all_configs)} configs")

with open('pom.xml', 'r') as f:
    pom = f.read()

xml_lines = []
for c in all_configs:
    xml_lines.append(f"                        <classOrPackageName>tensorrt_llm.presets.{c}</classOrPackageName>")
new_block = "<classOrPackageNames>\n" + "\n".join(xml_lines) + "\n                    </classOrPackageNames>"

pom = re.sub(r'<classOrPackageNames>.*?</classOrPackageNames>', new_block, pom, flags=re.DOTALL)
with open('pom.xml', 'w') as f:
    f.write(pom)

print("Updated pom.xml")
