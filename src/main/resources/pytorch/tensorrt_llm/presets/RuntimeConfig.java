package tensorrt_llm.presets;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;
import org.bytedeco.javacpp.tools.*;

@Properties(
    value = @Platform(
        includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include", "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp", "/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/cuda-stubs"},
        include = {
            "tensorrt_llm/runtime/common.h",
            "tensorrt_llm/runtime/iBuffer.h",
            "tensorrt_llm/runtime/iTensor.h",
            "tensorrt_llm/runtime/bufferManager.h",
            "tensorrt_llm/runtime/bufferView.h",
            "tensorrt_llm/runtime/cudaEvent.h",
            "tensorrt_llm/runtime/cudaMemPool.h",
            "tensorrt_llm/runtime/cudaStream.h",
            "tensorrt_llm/runtime/decoderState.h",
            "tensorrt_llm/runtime/decodingInput.h",
            "tensorrt_llm/runtime/decodingLayerWorkspace.h",
            "tensorrt_llm/runtime/decodingOutput.h",
            "tensorrt_llm/runtime/eagleBuffers.h",
            "tensorrt_llm/runtime/eagleModule.h",
            "tensorrt_llm/runtime/explicitDraftTokensBuffers.h",
            "tensorrt_llm/runtime/explicitDraftTokensModule.h",
            "tensorrt_llm/runtime/gptJsonConfig.h",
            "tensorrt_llm/runtime/iGptDecoderBatched.h",
            "tensorrt_llm/runtime/layerProfiler.h",
            "tensorrt_llm/runtime/lookaheadBuffers.h",
            "tensorrt_llm/runtime/lookaheadModule.h",
            "tensorrt_llm/runtime/loraCache.h",
            "tensorrt_llm/runtime/loraCachePageManagerConfig.h",
            "tensorrt_llm/runtime/loraManager.h",
            "tensorrt_llm/runtime/loraModule.h",
            "tensorrt_llm/runtime/loraUtils.h",
            "tensorrt_llm/runtime/medusaModule.h",
            "tensorrt_llm/runtime/memoryCounters.h",
            "tensorrt_llm/runtime/modelConfig.h",
            "tensorrt_llm/runtime/ncclCommunicator.h",
            "tensorrt_llm/runtime/promptTuningParams.h",
            "tensorrt_llm/runtime/rawEngine.h",
            "tensorrt_llm/runtime/runtimeDefaults.h",
            "tensorrt_llm/runtime/samplingConfig.h",
            "tensorrt_llm/runtime/speculativeDecodingMode.h",
            "tensorrt_llm/runtime/speculativeDecodingModule.h",
            "tensorrt_llm/runtime/tllmBuffers.h",
            "tensorrt_llm/runtime/tllmLogger.h",
            "tensorrt_llm/runtime/tllmRuntime.h",
            "tensorrt_llm/runtime/worldConfig.h"
        }
    ),
    target = "tensorrt_llm.runtime",
    global = "tensorrt_llm.global.TrtllmRuntime"
)
public class RuntimeConfig implements InfoMapper {
    @Override
    public void map(InfoMap infoMap) {
        CommonConfig.mapCommonTypes(infoMap);

        // ===== Runtime-specific type aliases =====
        infoMap.put(new Info("tensorrt_llm::runtime::TensorPtr").skip());
        infoMap.put(new Info("tensorrt_llm::runtime::IBuffer::DataType").enumerate());
        infoMap.put(new Info("tensorrt_llm::runtime::MemoryType").enumerate());

        // ===== Skip torch-related headers/classes =====
        infoMap.put(new Info("tensorrt_llm::runtime::TorchView").skip());
        infoMap.put(new Info("tensorrt_llm::runtime::TorchUtils").skip());

        // ===== Skip heavy/problematic runtime classes =====
        infoMap.put(new Info("tensorrt_llm::runtime::StringPtrMap").skip());
        infoMap.put(new Info("tensorrt_llm::runtime::GptDecoder").skip());
        infoMap.put(new Info("tensorrt_llm::runtime::GptDecoderBatched").skip());
        infoMap.put(new Info("tensorrt_llm::runtime::IpcNvlsMemory").skip());
        infoMap.put(new Info("tensorrt_llm::runtime::IpcSocket").skip());
        infoMap.put(new Info("tensorrt_llm::runtime::NcclIpcSocket").skip());
        infoMap.put(new Info("tensorrt_llm::runtime::MulticastTensor").skip());
        infoMap.put(new Info("tensorrt_llm::runtime::McastDeviceMemory").skip());
        infoMap.put(new Info("tensorrt_llm::runtime::McastGPUBuffer").skip());
        infoMap.put(new Info("tensorrt_llm::runtime::CudaAllocator").skip());
        infoMap.put(new Info("tensorrt_llm::runtime::CudaAllocatorAsync").skip());
        infoMap.put(new Info("tensorrt_llm::runtime::CudaVirtualMemoryAllocator").skip());
        infoMap.put(new Info("tensorrt_llm::runtime::CUDAVirtualMemoryChunk").skip());
        infoMap.put(new Info("tensorrt_llm::runtime::CudaVirtualMemoryManager").skip());
        infoMap.put(new Info("tensorrt_llm::runtime::HostAccessibleDeviceAllocator").skip());
        infoMap.put(new Info("tensorrt_llm::runtime::MoeLoadBalancer").skip());
        infoMap.put(new Info("tensorrt_llm::runtime::TopologyDetector").skip());
        infoMap.put(new Info("tensorrt_llm::runtime::GdrMemDesc").skip());
        infoMap.put(new Info("tensorrt_llm::runtime::GDSStreamReader").skip());
        infoMap.put(new Info("tensorrt_llm::runtime::WorkerPool").skip());

        // Skip runtime kernels (template-heavy)
        infoMap.put(new Info("tensorrt_llm::runtime::kernels").skip());

        // Skip pg/mpi utils (MPI dependency)
        infoMap.put(new Info("tensorrt_llm::pg_utils").skip());

        // Skip debug utils
        infoMap.put(new Info("tensorrt_llm::runtime::utils::debugUtils").skip());
    }
}
