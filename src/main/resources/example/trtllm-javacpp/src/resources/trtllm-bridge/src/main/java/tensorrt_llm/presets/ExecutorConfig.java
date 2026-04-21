package tensorrt_llm.presets;

import org.bytedeco.javacpp.annotation.Platform;
import org.bytedeco.javacpp.annotation.Properties;
import org.bytedeco.javacpp.tools.InfoMapper;
import org.bytedeco.javacpp.tools.InfoMap;

@Properties(
    value = @Platform(
        includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include", "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp"},
        include = {
            "tensorrt_llm/executor/orchestratorUtils.h",
            "tensorrt_llm/executor/requestImpl.h",
            "tensorrt_llm/executor/responseImpl.h",
            "tensorrt_llm/executor/requestUtils.h",
            "tensorrt_llm/executor/executorImpl.h",
            "tensorrt_llm/executor/requestWithId.h",
            "tensorrt_llm/executor/model.h",
            "tensorrt_llm/executor/dynamicBatchTuner.h",
            "tensorrt_llm/executor/intervalSet.h",
            "tensorrt_llm/executor/serializeUtils.h",
            "tensorrt_llm/executor/cache_transmission/cacheSplitConcat.h",
            "tensorrt_llm/executor/cache_transmission/agent_utils/connection.h",
            "tensorrt_llm/executor/cache_transmission/mooncake_utils/transferAgent.h",
            "tensorrt_llm/executor/cache_transmission/mpi_utils/connection.h",
            "tensorrt_llm/executor/cache_transmission/ucx_utils/connection.h",
            "tensorrt_llm/executor/cache_transmission/ucx_utils/ucxCacheCommunicator.h",
            "tensorrt_llm/executor/cache_transmission/nixl_utils/interfaces.h",
            "tensorrt_llm/executor/cache_transmission/nixl_utils/transferAgent.h"
        }
    ),
    inherit = TRTLLMFullConfig.class,
    target = "org.bytedeco.tensorrt_llm.executor",
    global = "org.bytedeco.tensorrt_llm.global.TRTLLM"
)
public class ExecutorConfig implements InfoMapper {
    @Override public void map(InfoMap infoMap) {
        // Handles specifics here
    }
}
