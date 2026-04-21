package tensorrt_llm.presets;

import org.bytedeco.javacpp.annotation.Platform;
import org.bytedeco.javacpp.annotation.Properties;
import org.bytedeco.javacpp.tools.InfoMapper;
import org.bytedeco.javacpp.tools.InfoMap;
import org.bytedeco.javacpp.tools.Info;

@Properties(
    value = @Platform(
        includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include", "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp"},
        include = {
            
        }
    ),
    inherit = TRTLLMFullConfig.class,
    target = "org.bytedeco.tensorrt_llm.flash_mla",
    global = "org.bytedeco.tensorrt_llm.global.TRTLLM"
)
public class Flash_mlaConfig implements InfoMapper {
    @Override public void map(InfoMap infoMap) {
        // Handles specifics here
    }
}
