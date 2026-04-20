package tensorrt_llm.presets;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;
import org.bytedeco.javacpp.tools.*;

@Properties(
    value = @Platform(
        includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include", "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp", "/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/cuda-stubs"},
        include = {
            "tensorrt_llm/thop/thUtils.h",
            "tensorrt_llm/thop/dynamicDecodeOp.h",
            "tensorrt_llm/thop/ncclCommunicatorOp.h",
            "tensorrt_llm/thop/attentionOp.h",
            "tensorrt_llm/thop/fp8Op.h",
            "tensorrt_llm/thop/fp4Quantize.h",
            "tensorrt_llm/thop/weightOnlyQuantGemm.h",
            "tensorrt_llm/thop/moeAlltoAllMeta.h",
            "tensorrt_llm/thop/cublasScaledMM.h",
            "tensorrt_llm/thop/cublasScaledMMLut.h"
        }
    ),
    target = "tensorrt_llm.thop",
    global = "tensorrt_llm.global.Thop"
)
public class ThopConfig implements InfoMapper {
    @Override
    public void map(InfoMap infoMap) {
        CommonConfig.mapCommonTypes(infoMap);

        // nvinfer1::DataType as int
        infoMap.put(new Info("nvinfer1::DataType").valueTypes("int").pointerTypes("IntPointer"));

        // Skip functions that return std::tuple or use complex torch types
        infoMap.put(new Info("symmetric_quantize_weight", "symmetric_quantize_activation", "symmetric_quantize_per_tensor",
                "symmetric_static_quantize_weight", "symmetric_static_quantize_activation", "symmetric_static_quantize_per_tensor").skip());

        // th::Tensor / at::Tensor -> Pointer
        infoMap.put(new Info("at::Tensor", "torch::Tensor", "th::Tensor").pointerTypes("Pointer"));
        infoMap.put(new Info("at::TensorList", "torch::TensorList").pointerTypes("Pointer"));
        infoMap.put(new Info("c10::optional").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("torch::optional", "th::optional").skip());

        // c10 types
        infoMap.put(new Info("c10::ScalarType").valueTypes("int"));
        infoMap.put(new Info("c10::Device").pointerTypes("Pointer"));
        infoMap.put(new Info("at::cuda::CUDAStream").pointerTypes("Pointer"));

        // Skip template FtDynamicDecode - too complex
        infoMap.put(new Info("tensorrt_llm::torch_ext::FtDynamicDecode").pointerTypes(\"Pointer\"));

        // Skip complex attention/gemm op template functions
        infoMap.put(new Info("tensorrt_llm::torch_ext::GemmToProfile").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::torch_ext::KernelType").enumerate());

        // Runtime types used across
        infoMap.put(new Info("tensorrt_llm::runtime::ITensor").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::IBuffer").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::CudaStream").pointerTypes("Pointer"));

        // Layers types used in dynamicDecodeOp
        infoMap.put(new Info("tensorrt_llm::layers::DynamicDecodeLayer").pointerTypes(\"Pointer\"));

        // IFtDynamicDecode - keep but skip methods with optional<Tensor> params
        infoMap.put(new Info("tensorrt_llm::torch_ext::IFtDynamicDecode::setup").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::torch_ext::IFtDynamicDecode::forward").pointerTypes(\"Pointer\"));

        // Skip moe/alltoall complex functions
        infoMap.put(new Info("tensorrt_llm::torch_ext::MoeLoadBalanceMetaInfo").pointerTypes(\"Pointer\"));

        // Skip fp8/fp4 template functions
        infoMap.put(new Info("tensorrt_llm::torch_ext::fp8Quantize").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::torch_ext::fp4Quantize").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::torch_ext::weightOnlyQuantGemm").pointerTypes(\"Pointer\"));
    }
}
