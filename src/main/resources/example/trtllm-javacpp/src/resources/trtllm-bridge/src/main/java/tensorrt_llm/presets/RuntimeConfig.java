package tensorrt_llm.presets;

import org.bytedeco.javacpp.annotation.Platform;
import org.bytedeco.javacpp.annotation.Properties;
import org.bytedeco.javacpp.tools.InfoMapper;
import org.bytedeco.javacpp.tools.InfoMap;

@Properties(
    value = @Platform(
        includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include", "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp"},
        include = {
            "tensorrt_llm/runtime/jsonSerialization.h",
            "tensorrt_llm/runtime/bufferView.h",
            "tensorrt_llm/runtime/ipcSocket.h",
            "tensorrt_llm/runtime/tllmStreamReaders.h",
            "tensorrt_llm/runtime/loraUtils.h",
            "tensorrt_llm/runtime/tensorView.h",
            "tensorrt_llm/runtime/loraManager.h",
            "tensorrt_llm/runtime/torchView.h",
            "tensorrt_llm/runtime/decodingLayerWorkspace.h",
            "tensorrt_llm/runtime/mcastDeviceMemory.h",
            "tensorrt_llm/runtime/torch.h",
            "tensorrt_llm/runtime/runtimeKernels.h",
            "tensorrt_llm/runtime/explicitDraftTokensModule.h",
            "tensorrt_llm/runtime/layerProfiler.h",
            "tensorrt_llm/runtime/tllmBuffers.h",
            "tensorrt_llm/runtime/ncclCommunicator.h",
            "tensorrt_llm/runtime/tllmRuntime.h",
            "tensorrt_llm/runtime/cudaMemPool.h",
            "tensorrt_llm/runtime/mcastGPUBuffer.h",
            "tensorrt_llm/runtime/torchUtils.h",
            "tensorrt_llm/runtime/workerPool.h",
            "tensorrt_llm/runtime/moeLoadBalancer/topologyDetector.h",
            "tensorrt_llm/runtime/moeLoadBalancer/moeLoadBalancer.h",
            "tensorrt_llm/runtime/moeLoadBalancer/hostAccessibleDeviceAllocator.h",
            "tensorrt_llm/runtime/moeLoadBalancer/gdrwrap.h",
            "tensorrt_llm/runtime/utils/runtimeUtils.h",
            "tensorrt_llm/runtime/utils/numpyUtils.h"
        }
    ),
    inherit = TRTLLMFullConfig.class,
    target = "org.bytedeco.tensorrt_llm.runtime",
    global = "org.bytedeco.tensorrt_llm.global.TRTLLM"
)
public class RuntimeConfig implements InfoMapper {
    @Override public void map(InfoMap infoMap) {
        // Handles specifics here
    }
}
