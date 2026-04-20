package tensorrt_llm.presets;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;
import org.bytedeco.javacpp.tools.*;

@Properties(
    value = @Platform(
        includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include", "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp", "/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/cuda-stubs"},
        include = {
            "tensorrt_llm/thop/thUtils.h",
            "tensorrt_llm/thop/attentionOp.h",
            "tensorrt_llm/thop/dynamicDecodeOp.h",
            "tensorrt_llm/thop/fp4Quantize.h",
            "tensorrt_llm/thop/fp8Op.h",
            "tensorrt_llm/thop/cublasScaledMM.h",
            "tensorrt_llm/thop/cublasScaledMMLut.h",
            "tensorrt_llm/thop/weightOnlyQuantGemm.h",
            "tensorrt_llm/thop/userbuffersTensor.h",
            "tensorrt_llm/thop/ncclCommunicatorOp.h",
            "tensorrt_llm/thop/moeAlltoAllMeta.h",
            "tensorrt_llm/thop/finegrained_mixed_dtype_gemm_thop.h"
        }
    ),
    target = "tensorrt_llm.thop",
    global = "tensorrt_llm.global.Thop"
)
public class ThopConfig implements InfoMapper {
    @Override
    public void map(InfoMap infoMap) {
        // Type aliases
        infoMap.put(new Info("tensorrt_llm::runtime::SizeType32", "SizeType32", "runtime::SizeType32").valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::TokenIdType", "TokenIdType").valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("std::int32_t", "int32_t").valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("std::int64_t", "int64_t").valueTypes("long").pointerTypes("LongPointer"));
        infoMap.put(new Info("std::uint32_t", "uint32_t").valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("std::uint64_t", "uint64_t").valueTypes("long").pointerTypes("LongPointer"));
        infoMap.put(new Info("std::size_t", "size_t").valueTypes("long").pointerTypes("LongPointer"));
        infoMap.put(new Info("bool").valueTypes("boolean").pointerTypes("BoolPointer"));
        infoMap.put(new Info("auto").cppTypes("int"));

        // Torch types -> Pointer (mapped as opaque)
        infoMap.put(new Info("at::Tensor", "torch::Tensor", "th::Tensor").pointerTypes("Pointer"));
        infoMap.put(new Info("c10::ScalarType").valueTypes("int"));
        infoMap.put(new Info("torch::jit::CustomClassHolder", "torch::CustomClassHolder").pointerTypes("Pointer"));

        // Skip unparseable constructs
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

        // CUDA types
        infoMap.put(new Info("nvinfer1::ILogger").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::DataType").valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("cudaStream_t").valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("cudaEvent_t").valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("CUmemPoolHandle_st").pointerTypes("Pointer"));
        infoMap.put(new Info("__host__", "__device__", "__forceinline__", "__global__", "__launch_bounds__").cppTypes().annotations());
        infoMap.put(new Info("curandState_t", "curandState").pointerTypes("Pointer"));

        // Skip problematic
        infoMap.put(new Info("tensorrt_llm::common::getDTypeSize", "tensorrt_llm::common::getDTypeSizeInBits", "tensorrt_llm::common::getDtypeString").skip());

        // Skip functions that return std::tuple (can't map)
        infoMap.put(new Info("symmetric_quantize_weight", "symmetric_quantize_activation", "symmetric_quantize_per_tensor",
                "symmetric_static_quantize_weight", "symmetric_static_quantize_activation", "symmetric_static_quantize_per_tensor").skip());
    }
}
