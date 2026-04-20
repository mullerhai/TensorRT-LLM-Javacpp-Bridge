package tensorrt_llm.presets;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;
import org.bytedeco.javacpp.tools.*;

@Properties(
    value = @Platform(
        includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include", "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp", "/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/cuda-stubs"},
        include = {
            "tensorrt_llm/layers/baseLayer.h",
            "tensorrt_llm/layers/decodingParams.h",
            "tensorrt_llm/layers/defaultDecodingParams.h",
            "tensorrt_llm/layers/decodingLayer.h",
            "tensorrt_llm/layers/dynamicDecodeLayer.h",
            "tensorrt_llm/layers/layerUtils.h",
            "tensorrt_llm/layers/layersFactory.h",
            "tensorrt_llm/layers/lookaheadAlgorithm.h",
            "tensorrt_llm/layers/lookaheadPoolManager.h",
            "tensorrt_llm/layers/penaltyLayer.h",
            "tensorrt_llm/layers/samplingLayer.h",
            "tensorrt_llm/layers/stopCriteriaLayer.h"
        }
    ),
    target = "tensorrt_llm.layers",
    global = "tensorrt_llm.global.Layers"
)
public class LayersConfig implements InfoMapper {
    @Override
    public void map(InfoMap infoMap) {
        CommonConfig.mapCommonTypes(infoMap);

        // Executor DecodingMode used by layers
        infoMap.put(new Info("tensorrt_llm::executor::DecodingMode").pointerTypes("Pointer"));

        // Skip template-heavy layer types
        infoMap.put(new Info("tensorrt_llm::layers::BanWordsLayer").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::layers::BeamSearchLayer").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::layers::TopKSamplingLayer").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::layers::TopPSamplingLayer").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::layers::EagleDecodingLayer").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::layers::ExplicitDraftTokensLayer").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::layers::ExternalDraftTokensLayer").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::layers::MedusaDecodingLayer").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::layers::LookaheadDecodingLayer").pointerTypes(\"Pointer\"));
    }
}
