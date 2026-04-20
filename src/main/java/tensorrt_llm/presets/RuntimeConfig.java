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
        infoMap.put(new Info("tensorrt_llm::runtime::TensorPtr").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::IBuffer::DataType").enumerate());
        infoMap.put(new Info("tensorrt_llm::runtime::MemoryType").enumerate());

        // ===== Skip torch-related headers/classes =====
        infoMap.put(new Info("tensorrt_llm::runtime::TorchView").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::TorchUtils").pointerTypes("Pointer"));

        // ===== Skip heavy/problematic runtime classes =====
        infoMap.put(new Info("tensorrt_llm::runtime::StringPtrMap").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::GptDecoder").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::GptDecoderBatched").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::IpcNvlsMemory").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::IpcSocket").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::NcclIpcSocket").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::MulticastTensor").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::McastDeviceMemory").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::McastGPUBuffer").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::CudaAllocator").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::CudaAllocatorAsync").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::CudaVirtualMemoryAllocator").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::CUDAVirtualMemoryChunk").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::CudaVirtualMemoryManager").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::HostAccessibleDeviceAllocator").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::MoeLoadBalancer").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::TopologyDetector").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::GdrMemDesc").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::GDSStreamReader").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::WorkerPool").pointerTypes("Pointer"));

        // Skip runtime kernels (template-heavy)
        infoMap.put(new Info("tensorrt_llm::runtime::kernels").pointerTypes("Pointer"));

        // Skip pg/mpi utils (MPI dependency)
        infoMap.put(new Info("tensorrt_llm::pg_utils").pointerTypes("Pointer"));

        // Skip debug utils
        infoMap.put(new Info("tensorrt_llm::runtime::utils::debugUtils").pointerTypes("Pointer"));

        // Skip ITensor::slice overloads that use DimType64 (std::remove_reference_t) which generates invalid Java
        infoMap.put(new Info("tensorrt_llm::runtime::ITensor::DimType64").valueTypes("long").pointerTypes("LongPointer"));
        infoMap.put(new Info("std::remove_reference_t").pointerTypes("Pointer"));

        // Skip ITensor methods that use initializer_list or complex template types
        infoMap.put(new Info("tensorrt_llm::runtime::ITensor::slice").pointerTypes("Pointer"));
    }
}
