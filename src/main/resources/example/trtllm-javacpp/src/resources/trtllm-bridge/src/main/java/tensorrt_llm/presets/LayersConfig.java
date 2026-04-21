package tensorrt_llm.presets;

import org.bytedeco.javacpp.annotation.Platform;
import org.bytedeco.javacpp.annotation.Properties;
import org.bytedeco.javacpp.tools.InfoMapper;
import org.bytedeco.javacpp.tools.InfoMap;

@Properties(
    value = @Platform(
        includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include", "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp"},
        include = {
            "tensorrt_llm/layers/decodingLayer.h",
            "tensorrt_llm/layers/lookaheadDecodingUtils.h",
            "tensorrt_llm/layers/baseLayer.h",
            "tensorrt_llm/layers/lookaheadPoolManager.h",
            "tensorrt_llm/layers/topPSamplingLayer.h",
            "tensorrt_llm/layers/medusaDecodingLayer.h",
            "tensorrt_llm/layers/lookaheadAlgorithm.h",
            "tensorrt_llm/layers/explicitDraftTokensLayer.h",
            "tensorrt_llm/layers/lookaheadDecodingLayer.h",
            "tensorrt_llm/layers/layerUtils.h",
            "tensorrt_llm/layers/layersFactory.h",
            "tensorrt_llm/layers/dynamicDecodeLayer.h",
            "tensorrt_llm/layers/beamSearchLayer.h",
            "tensorrt_llm/layers/decodingParams.h",
            "tensorrt_llm/layers/topKSamplingLayer.h",
            "tensorrt_llm/layers/samplingLayer.h",
            "tensorrt_llm/layers/externalDraftTokensLayer.h",
            "tensorrt_llm/layers/banWordsLayer.h",
            "tensorrt_llm/layers/eagleDecodingLayer.h",
            "tensorrt_llm/layers/penaltyLayer.h",
            "tensorrt_llm/layers/stopCriteriaLayer.h"
        }
    ),
    inherit = TRTLLMFullConfig.class,
    target = "org.bytedeco.tensorrt_llm.layers",
    global = "org.bytedeco.tensorrt_llm.global.TRTLLM"
)
public class LayersConfig implements InfoMapper {
    @Override public void map(InfoMap infoMap) {
        // Handles specifics here
    }
}
