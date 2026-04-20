package tensorrt_llm.presets;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;
import org.bytedeco.javacpp.tools.*;

@Properties(
    value = @Platform(
        includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include", "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp", "/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/cuda-stubs"},
        include = {
            "tensorrt_llm/kernels/gptKernels.h",
            "tensorrt_llm/kernels/customAllReduceKernels.h",
            "tensorrt_llm/kernels/quantization.h",
            "tensorrt_llm/kernels/decodingCommon.h",
            "tensorrt_llm/kernels/kvCacheIndex.h",
            "tensorrt_llm/kernels/beamSearchKernels.h",
            "tensorrt_llm/kernels/penaltyKernels.h",
            "tensorrt_llm/kernels/penaltyTypes.h",
            "tensorrt_llm/kernels/banBadWords.h",
            "tensorrt_llm/kernels/banRepeatNgram.h",
            "tensorrt_llm/kernels/stopCriteriaKernels.h",
            "tensorrt_llm/kernels/decodingKernels.h",
            "tensorrt_llm/kernels/attentionMask.h",
            "tensorrt_llm/kernels/arcquantFP4.h",
            "tensorrt_llm/kernels/rmsnormKernels.h",
            "tensorrt_llm/kernels/layernormKernels.h",
            "tensorrt_llm/kernels/moeAlignKernels.h",
            "tensorrt_llm/kernels/customMoeRoutingKernels.h",
            "tensorrt_llm/kernels/cumsumLastDim.h",
            "tensorrt_llm/kernels/logitsBitmask.h",
            "tensorrt_llm/kernels/doraScaling.h",
            "tensorrt_llm/kernels/fusedActivationQuant.h",
            "tensorrt_llm/kernels/fusedCatFp8.h",
            "tensorrt_llm/kernels/fusedMoeCommKernels.h",
            "tensorrt_llm/kernels/fusedQKNormRopeKernel.h",
            "tensorrt_llm/kernels/groupGemm.h",
            "tensorrt_llm/kernels/lookupKernels.h",
            "tensorrt_llm/kernels/mambaConv1dKernels.h",
            "tensorrt_llm/kernels/mlaKernels.h",
            "tensorrt_llm/kernels/moePrepareKernels.h",
            "tensorrt_llm/kernels/preQuantScaleKernel.h",
            "tensorrt_llm/kernels/topkLastDim.h",
            "tensorrt_llm/kernels/delayStream.h",
            "tensorrt_llm/kernels/kvCacheUtils.h",
            "tensorrt_llm/kernels/kvCachePartialCopy.h",
            "tensorrt_llm/kernels/math.h",
            "tensorrt_llm/kernels/archCondition.h"
        }
    ),
    target = "tensorrt_llm.kernels",
    global = "tensorrt_llm.global.Kernels"
)
public class KernelsConfig implements InfoMapper {
    @Override
    public void map(InfoMap infoMap) {
        // Type aliases -> Java primitives
        infoMap.put(new Info("tensorrt_llm::runtime::SizeType32", "SizeType32", "runtime::SizeType32").valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::TokenIdType", "TokenIdType").valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("std::int32_t", "int32_t").valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("std::int64_t", "int64_t").valueTypes("long").pointerTypes("LongPointer"));
        infoMap.put(new Info("std::uint32_t", "uint32_t").valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("std::uint64_t", "uint64_t").valueTypes("long").pointerTypes("LongPointer"));
        infoMap.put(new Info("std::size_t", "size_t").valueTypes("long").pointerTypes("LongPointer"));
        infoMap.put(new Info("bool").valueTypes("boolean").pointerTypes("BoolPointer"));

        // Fix auto constexpr
        infoMap.put(new Info("auto").cppTypes("int"));

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
        infoMap.put(new Info("std::array").skip());
        infoMap.put(new Info("std::vector").skip());
        infoMap.put(new Info("std::set").skip());

        // CUDA attributes - strip
        infoMap.put(new Info("__host__", "__device__", "__forceinline__", "__global__", "__launch_bounds__", "__align__",
                "[[nodiscard]]", "__inline__").cppTypes().annotations());

        // nvinfer1::DataType
        infoMap.put(new Info("nvinfer1::DataType").valueTypes("int").pointerTypes("IntPointer"));

        // CUDA types
        infoMap.put(new Info("nvinfer1::ILogger").pointerTypes("Pointer"));
        infoMap.put(new Info("cudaStream_t").valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("cudaEvent_t").valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("CUmemPoolHandle_st").pointerTypes("Pointer"));
        infoMap.put(new Info("curandState_t", "curandState").pointerTypes("Pointer"));

        // Skip problematic functions/templates
        infoMap.put(new Info("tensorrt_llm::common::getDTypeSize", "tensorrt_llm::common::getDTypeSizeInBits", "tensorrt_llm::common::getDtypeString").skip());

        // Skip template-heavy types and inline functions returning initializer lists
        infoMap.put(new Info("tensorrt_llm::kernels::FinishedState::empty", "tensorrt_llm::kernels::FinishedState::finished",
                "tensorrt_llm::kernels::FinishedState::skipDecoding", "tensorrt_llm::kernels::FinishedState::finishedEOS",
                "tensorrt_llm::kernels::FinishedState::finishedMaxLength", "tensorrt_llm::kernels::FinishedState::finishedStopWords").skip());

        // Skip inline workspace-size functions that return initializer lists (can't parse)
        infoMap.put(new Info("getTopKWorkspaceSizes", "getTopKInitWorkspaceSizes",
                "getTopPWorkspaceSizes", "getTopPInitWorkspaceSizes",
                "getAirTopPWorkspaceSizes", "getAirTopPInitWorkspaceSizes",
                "getAirTopPBlockSortWorkspaceSizes",
                "getSamplingWorkspaceSizes").skip());

        // executor types
        infoMap.put(new Info("tensorrt_llm::executor::DecodingMode").pointerTypes("Pointer"));
    }
}
