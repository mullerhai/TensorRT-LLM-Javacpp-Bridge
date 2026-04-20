import os

cpp_dir = "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/tensorrt_llm"
include_dir = "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include"

packages = [
    "thop",
    "executor",
    "kernels",
    "deep_gemm",
    "cutlass_extensions",
    "common",
    "deep_ep",
    "plugins"
]

def get_headers(pkg):
    headers = []

    # Check cpp_dir/pkg
    if os.path.exists(os.path.join(cpp_dir, pkg)):
        for root, dirs, files in os.walk(os.path.join(cpp_dir, pkg)):
            for f in files:
                if f.endswith('.h'):
                    rel_path = os.path.relpath(os.path.join(root, f), "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp")
                    headers.append(rel_path)

    # Check include_dir/tensorrt_llm/pkg
    if os.path.exists(os.path.join(include_dir, "tensorrt_llm", pkg)):
        for root, dirs, files in os.walk(os.path.join(include_dir, "tensorrt_llm", pkg)):
            for f in files:
                if f.endswith('.h'):
                    rel_path = os.path.relpath(os.path.join(root, f), include_dir)
                    headers.append(rel_path)

    return sorted(list(set(headers)))

template = """package tensorrt_llm.presets;

import org.bytedeco.javacpp.annotation.Platform;
import org.bytedeco.javacpp.annotation.Properties;
import org.bytedeco.javacpp.tools.Info;
import org.bytedeco.javacpp.tools.InfoMap;
import org.bytedeco.javacpp.tools.InfoMapper;

@Properties(
    value = @Platform(
        includepath = {{
            "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include",
            "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp"
        }},
        include = {{
{includes}
        }}
    ),
    inherit = TRTLLMFullConfig.class,
    target = "org.bytedeco.tensorrt_llm.{pkg_name}",
    global = "org.bytedeco.tensorrt_llm.global.{pkg_name_capitalized}"
)
public class {config_name} implements InfoMapper {{
    @Override
    public void map(InfoMap infoMap) {{
        infoMap.put(new Info("std::optional").skip());
        infoMap.put(new Info("std::variant").skip());
        infoMap.put(new Info("std::function").skip());
        infoMap.put(new Info("std::promise").skip());
    }}
}}
"""

all_configs = []
for pkg in packages:
    headers = get_headers(pkg)
    if not headers:
        print(f"No headers found for {pkg}")
        continue

    includes = ",\n".join([f'            "{h}"' for h in headers])
    config_name = pkg.capitalize().replace("_", "") + "Config"
    pkg_name = pkg.lower()
    pkg_name_capitalized = pkg_name.capitalize()

    content = template.format(
        includes=includes,
        pkg_name=pkg_name,
        pkg_name_capitalized=pkg_name_capitalized,
        config_name=config_name
    )

    with open(f"src/main/java/tensorrt_llm/presets/{config_name}.java", "w") as f:
        f.write(content)

    all_configs.append(f"tensorrt_llm.presets.{config_name}")

print("Generated config classes:")
for c in all_configs:
    print(c)

