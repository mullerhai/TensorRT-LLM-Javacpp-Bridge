package tensorrt_llm.presets;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;
import org.bytedeco.javacpp.tools.*;

@Properties(
    value = @Platform(
        includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include", "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp", "/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/cuda-stubs"},
        include = {
            "tensorrt_llm/kernels/decodingCommon.h",
            "tensorrt_llm/kernels/kvCacheIndex.h",
            "tensorrt_llm/kernels/penaltyTypes.h",
            "tensorrt_llm/kernels/attentionMask.h",
            "tensorrt_llm/kernels/banBadWords.h",
            "tensorrt_llm/kernels/banRepeatNgram.h",
            "tensorrt_llm/kernels/stopCriteriaKernels.h",
            "tensorrt_llm/kernels/decodingKernels.h",
            "tensorrt_llm/kernels/penaltyKernels.h",
            "tensorrt_llm/kernels/samplingTopKKernels.h",
            "tensorrt_llm/kernels/samplingTopPKernels.h",
            "tensorrt_llm/kernels/layernormKernels.h",
            "tensorrt_llm/kernels/rmsnormKernels.h",
            "tensorrt_llm/kernels/quantization.h",
            "tensorrt_llm/kernels/customAllReduceKernels.h",
            "tensorrt_llm/kernels/lookupKernels.h",
            "tensorrt_llm/kernels/moeAlignKernels.h",
            "tensorrt_llm/kernels/moePrepareKernels.h",
            "tensorrt_llm/kernels/kvCacheUtils.h",
            "tensorrt_llm/kernels/multiHeadAttentionCommon.h"
        }
    ),
    target = "tensorrt_llm.kernels",
    global = "tensorrt_llm.global.Kernels"
)
public class KernelsConfig implements InfoMapper {
    @Override
    public void map(InfoMap infoMap) {
        CommonConfig.mapCommonTypes(infoMap);

        // nvinfer1::DataType as int
        infoMap.put(new Info("nvinfer1::DataType").valueTypes("int").pointerTypes("IntPointer"));

        // executor DecodingMode
        infoMap.put(new Info("tensorrt_llm::executor::DecodingMode").pointerTypes("Pointer"));

        // Skip FinishedState factory methods (inline functions returning initializer lists)
        infoMap.put(new Info("tensorrt_llm::kernels::FinishedState::empty", "tensorrt_llm::kernels::FinishedState::finished",
                "tensorrt_llm::kernels::FinishedState::skipDecoding", "tensorrt_llm::kernels::FinishedState::finishedEOS",
                "tensorrt_llm::kernels::FinishedState::finishedMaxLength", "tensorrt_llm::kernels::FinishedState::finishedStopWords").skip());

        // Skip workspace-size functions returning initializer lists
        infoMap.put(new Info("getTopKWorkspaceSizes", "getTopKInitWorkspaceSizes",
                "getTopPWorkspaceSizes", "getTopPInitWorkspaceSizes",
                "getAirTopPWorkspaceSizes", "getAirTopPInitWorkspaceSizes",
                "getAirTopPBlockSortWorkspaceSizes", "getSamplingWorkspaceSizes").skip());

        // Skip template structs (generated per-type)
        infoMap.put(new Info("tensorrt_llm::kernels::InvokeBatchApplyPenaltyParams").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::kernels::TopKSamplingKernelParams").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::kernels::TopPSamplingKernelParams").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::kernels::BanBadWordsParams").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::kernels::StopCriteriaKernelParams").pointerTypes(\"Pointer\"));

        // Skip template CUDA kernel invocations
        infoMap.put(new Info("tensorrt_llm::kernels::invokeBatchApplyPenalty").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::kernels::invokeSamplingTopK").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::kernels::invokeSamplingTopP").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::kernels::invokeApplyTemperaturePenalty").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::kernels::invokeAddBiasSoftMax").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::kernels::invokeLayerNorm").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::kernels::invokeRmsNorm").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::kernels::invokeGeneralLayerNorm").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::kernels::invokeGeneralRmsNorm").pointerTypes(\"Pointer\"));

        // KV cache types from runtime
        infoMap.put(new Info("tensorrt_llm::runtime::KVBlockArray").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::KVLinearBuffer").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::ITensor").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::IBuffer").pointerTypes("Pointer"));

        // KvCacheUtils types
        infoMap.put(new Info("tensorrt_llm::kernels::KVCacheListParams").pointerTypes(\"Pointer\"));

        // MOE complex types - skip
        infoMap.put(new Info("tensorrt_llm::kernels::MoeLoadBalanceMetaInfo").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::kernels::MoePlacementInfo").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::kernels::MoeLoadBalanceSingleLayerSignal").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::kernels::MoeLoadBalanceStatisticInfo").pointerTypes(\"Pointer\"));

        // Custom allreduce types
        infoMap.put(new Info("tensorrt_llm::kernels::AllReduceStrategyConfig").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::kernels::AllReduceFusionOp").enumerate());
        infoMap.put(new Info("tensorrt_llm::kernels::AllReduceStrategyType").enumerate());

        // Attention types
        infoMap.put(new Info("tensorrt_llm::kernels::PositionEmbeddingType").enumerate());
        infoMap.put(new Info("tensorrt_llm::kernels::RotaryScalingType").enumerate());
        infoMap.put(new Info("tensorrt_llm::kernels::AttentionType").enumerate());
        infoMap.put(new Info("tensorrt_llm::kernels::KVCacheDataType").enumerate());

        // runtime types used in kernel headers
        infoMap.put(new Info("tensorrt_llm::runtime::CudaStream").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::SamplingConfig").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::DecodingInput").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::DecodingOutput").pointerTypes("Pointer"));

        // Skip complex lookupKernel template functions
        infoMap.put(new Info("tensorrt_llm::kernels::invokeLookupKernel").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::kernels::invokeTopkLastDim").pointerTypes(\"Pointer\"));
    }
}
