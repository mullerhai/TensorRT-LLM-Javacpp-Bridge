package tensorrt_llm.presets;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;
import org.bytedeco.javacpp.tools.*;

@Properties(
    value = @Platform(
        includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include", "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp", "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/tensorrt_llm/cutlass_extensions/include", "/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/cuda-stubs"},
        include = {
            "tensorrt_llm/cutlass_extensions/include/cutlass_extensions/gemm_configs.h",
            "tensorrt_llm/cutlass_extensions/include/cutlass_extensions/weight_only_quant_op.h"
        }
    ),
    target = "tensorrt_llm.cutlass_extensions",
    global = "tensorrt_llm.global.CutlassExtensions"
)
public class CutlassextensionsConfig implements InfoMapper {
    @Override
    public void map(InfoMap infoMap) {
        CommonConfig.mapCommonTypes(infoMap);

        // nvinfer1::int as int
        infoMap.put(new Info("nvinfer1::DataType").valueTypes("int").pointerTypes("IntPointer"));

        // Skip template specializations
        infoMap.put(new Info("cutlass::ColumnMajorTileInterleave", "cutlass::IsColumnMajorTileInterleave").skip());

        // Skip template-heavy cutlass types
        infoMap.put(new Info("cutlass::arch::Sm80").pointerTypes("Pointer"));
        infoMap.put(new Info("cutlass::arch::Sm90").pointerTypes("Pointer"));

        // CutlassTileConfigSM90 has enum values with inline comments that break JavaCPP generation - skip the enum
        infoMap.put(new Info("tensorrt_llm::cutlass_extensions::CutlassTileConfigSM90").pointerTypes("Pointer"));
    }
}
