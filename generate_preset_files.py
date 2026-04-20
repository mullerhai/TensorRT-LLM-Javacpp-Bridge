import os

CPP_DIR = "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp"
PRESET_DIR = "/trtllm-javacpp/src/resources/trtllm-bridge/src/main/java/tensorrt_llm/presets"
POM_FILE = "/trtllm-javacpp/src/resources/trtllm-bridge/pom.xml"

MODULES = ["runtime", "layers", "flash_mla", "executor", "kernels"]

def capitalize_first(s):
    return s[0].upper() + s[1:] if s else ""

for mod in MODULES:
    search_mod = f"tensorrt_llm/{mod}"
    if mod == "flash_mla" and not os.path.exists(os.path.join(CPP_DIR, search_mod)):
        search_mod = "tensorrt_llm/kernels/flashMLA"

    actual_path = os.path.join(CPP_DIR, search_mod)
    if not os.path.exists(actual_path):
        print(f"Skipping {mod}, does not exist")
        continue

    headers = []
    for root, dirs, files in os.walk(actual_path):
        for f in files:
            if f.endswith(".h"):
                rel_path = os.path.relpath(os.path.join(root, f), CPP_DIR)
                headers.append(f'"{rel_path}"')

    headers_str = ",\n            ".join(headers)

    class_name = f"{capitalize_first(mod)}Config"
    content = f"""package tensorrt_llm.presets;

import org.bytedeco.javacpp.annotation.Platform;
import org.bytedeco.javacpp.annotation.Properties;
import org.bytedeco.javacpp.tools.InfoMapper;
import org.bytedeco.javacpp.tools.InfoMap;
import org.bytedeco.javacpp.tools.Info;

@Properties(
    value = @Platform(
        includepath = {{"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include", "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp"}},
        include = {{
            {headers_str}
        }}
    ),
    inherit = TRTLLMFullConfig.class,
    target = "org.bytedeco.tensorrt_llm.{mod}",
    global = "org.bytedeco.tensorrt_llm.global.TRTLLM"
)
public class {class_name} implements InfoMapper {{
    @Override public void map(InfoMap infoMap) {{
        // Handles specifics here
    }}
}}
"""
    dest = os.path.join(PRESET_DIR, f"{class_name}.java")
    with open(dest, "w") as f:
        f.write(content)

    print(f"Wrote {dest}")


