package tensorrt_llm.presets;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;
import org.bytedeco.javacpp.tools.*;

@Properties(
    value = @Platform(
        includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include", "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp", "/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/cuda-stubs"},
        include = {
            "tensorrt_llm/plugins/api/tllmPlugin.h",
            "tensorrt_llm/plugins/common/pluginUtils.h",
            "tensorrt_llm/plugins/common/checkMacrosPlugin.h",
            "tensorrt_llm/plugins/common/plugin.h",
            "tensorrt_llm/plugins/common/gemmPluginProfiler.h"
        }
    ),
    target = "tensorrt_llm.plugins",
    global = "tensorrt_llm.global.Plugins"
)
public class PluginsConfig implements InfoMapper {
    @Override
    public void map(InfoMap infoMap) {
        CommonConfig.mapCommonTypes(infoMap);

        // nvinfer1::int as int in plugin context
        infoMap.put(new Info("nvinfer1::DataType").valueTypes("int").pointerTypes("IntPointer"));

        // nvinfer1 plugin interfaces as Pointer
        infoMap.put(new Info("nvinfer1::IPluginV2DynamicExt").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::IPluginV3").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::IPluginV3OneCore").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::IPluginV3OneBuild").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::IPluginV3OneRuntime").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::IPluginCapability").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::IPluginResource").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::PersistentWorkspaceInterface").pointerTypes("Pointer"));

        // Skip problematic types from gemmPluginProfiler
        infoMap.put(new Info("tensorrt_llm::plugins::GemmPluginProfiler").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::plugins::GemmPluginProfilerManager").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::plugins::GemmDims").pointerTypes("Pointer"));

        // common::op namespace
        infoMap.put(new Info("tensorrt_llm::common::op").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::plugins::BasePlugin").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::plugins::BasePluginV3").pointerTypes("Pointer"));

        // opUtils.h dependencies
        infoMap.put(new Info("tensorrt_llm::common::cublasMMWrapper").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::common::workspace").pointerTypes("Pointer"));
        infoMap.put(new Info("cudaDataType_t").valueTypes("int"));
        infoMap.put(new Info("cublasComputeType_t").valueTypes("int"));

        // nvml
        infoMap.put(new Info("nvmlDevice_t").valueTypes("Pointer").pointerTypes("Pointer"));
    }
}
