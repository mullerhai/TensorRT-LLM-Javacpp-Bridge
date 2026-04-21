package tensorrt_llm.presets;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;
import org.bytedeco.javacpp.tools.*;

@Properties(
    value = @Platform(
        includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include", "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp", "/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/cuda-stubs"},
        include = {
            "tensorrt_llm/cutlass_extensions/include/cutlass_extensions/gemm_configs.h",
            "tensorrt_llm/cutlass_extensions/include/cutlass_extensions/weight_only_quant_op.h",
            "tensorrt_llm/cutlass_extensions/include/cutlass_extensions/tile_interleaved_layout.h"
        }
    ),
    target = "tensorrt_llm.cutlass_extensions",
    global = "tensorrt_llm.global.CutlassExtensions"
)
public class CutlassextensionsConfig implements InfoMapper {
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

        // Skip template specializations
        infoMap.put(new Info("cutlass::ColumnMajorTileInterleave", "cutlass::IsColumnMajorTileInterleave").skip());

        // CUDA types
        infoMap.put(new Info("nvinfer1::DataType").valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("cudaStream_t").valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("cudaEvent_t").valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("CUmemPoolHandle_st").pointerTypes("Pointer"));
        infoMap.put(new Info("__host__", "__device__", "__forceinline__", "__global__").cppTypes().annotations());

        // Skip problematic functions
        infoMap.put(new Info("tensorrt_llm::common::getDTypeSize", "tensorrt_llm::common::getDTypeSizeInBits", "tensorrt_llm::common::getDtypeString").skip());

    }
}
