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
            "tensorrt_llm/layers/stopCriteriaLayer.h",
            // Previously-missing layer headers — produce concrete Java classes
            "tensorrt_llm/layers/banWordsLayer.h",
            "tensorrt_llm/layers/beamSearchLayer.h",
            "tensorrt_llm/layers/topKSamplingLayer.h",
            "tensorrt_llm/layers/topPSamplingLayer.h",
            "tensorrt_llm/layers/medusaDecodingLayer.h",
            "tensorrt_llm/layers/lookaheadDecodingLayer.h",
            "tensorrt_llm/layers/eagleDecodingLayer.h",
            "tensorrt_llm/layers/explicitDraftTokensLayer.h",
            "tensorrt_llm/layers/externalDraftTokensLayer.h"
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

        // =====================================================================
        // Template-class Layer types — emit ONE concrete Java class per layer
        // (instantiated with <float>).  Previously these were mapped opaquely
        // to ``Pointer``; with ``.define()`` JavaCPP produces a full Java
        // wrapper with all constructors/methods.
        // =====================================================================
        infoMap.put(new Info("tensorrt_llm::layers::BanWordsLayer<float>")
                .pointerTypes("BanWordsLayer").define());
        infoMap.put(new Info("tensorrt_llm::layers::BeamSearchLayer<float>")
                .pointerTypes("BeamSearchLayer").define());
        infoMap.put(new Info("tensorrt_llm::layers::TopKSamplingLayer<float>")
                .pointerTypes("TopKSamplingLayer").define());
        infoMap.put(new Info("tensorrt_llm::layers::TopPSamplingLayer<float>")
                .pointerTypes("TopPSamplingLayer").define());
        infoMap.put(new Info("tensorrt_llm::layers::SamplingLayer<float>")
                .pointerTypes("SamplingLayer").define());
        infoMap.put(new Info("tensorrt_llm::layers::PenaltyLayer<float>")
                .pointerTypes("PenaltyLayer").define());
        infoMap.put(new Info("tensorrt_llm::layers::DecodingLayer<float>")
                .pointerTypes("DecodingLayer").define());
        infoMap.put(new Info("tensorrt_llm::layers::DynamicDecodeLayer<float>")
                .pointerTypes("DynamicDecodeLayer").define());
        infoMap.put(new Info("tensorrt_llm::layers::StopCriteriaLayer<float>")
                .pointerTypes("StopCriteriaLayer").define());
        infoMap.put(new Info("tensorrt_llm::layers::MedusaDecodingLayer<float>")
                .pointerTypes("MedusaDecodingLayer").define());
        infoMap.put(new Info("tensorrt_llm::layers::LookaheadDecodingLayer<float>")
                .pointerTypes("LookaheadDecodingLayer").define());
        infoMap.put(new Info("tensorrt_llm::layers::EagleDecodingLayer<float>")
                .pointerTypes("EagleDecodingLayer").define());
        infoMap.put(new Info("tensorrt_llm::layers::ExplicitDraftTokensLayer<float>")
                .pointerTypes("ExplicitDraftTokensLayer").define());
        infoMap.put(new Info("tensorrt_llm::layers::ExternalDraftTokensLayer<float>")
                .pointerTypes("ExternalDraftTokensLayer").define());

        // Also make the generic (unqualified) template name resolve to the
        // <float> instantiation so references elsewhere compile.
        infoMap.put(new Info("tensorrt_llm::layers::BanWordsLayer").pointerTypes("BanWordsLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::BeamSearchLayer").pointerTypes("BeamSearchLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::TopKSamplingLayer").pointerTypes("TopKSamplingLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::TopPSamplingLayer").pointerTypes("TopPSamplingLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::SamplingLayer").pointerTypes("SamplingLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::PenaltyLayer").pointerTypes("PenaltyLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::DecodingLayer").pointerTypes("DecodingLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::DynamicDecodeLayer").pointerTypes("DynamicDecodeLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::StopCriteriaLayer").pointerTypes("StopCriteriaLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::MedusaDecodingLayer").pointerTypes("MedusaDecodingLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::LookaheadDecodingLayer").pointerTypes("LookaheadDecodingLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::EagleDecodingLayer").pointerTypes("EagleDecodingLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::ExplicitDraftTokensLayer").pointerTypes("ExplicitDraftTokensLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::ExternalDraftTokensLayer").pointerTypes("ExternalDraftTokensLayer"));

        // DecoderDomain is a template — instantiate <float> as the canonical.
        infoMap.put(new Info("tensorrt_llm::layers::DecoderDomain<float>")
                .pointerTypes("DecoderDomain").define());

        // Skip layer factory templated free functions which return unique_ptr
        // of templated types (not usable from Java directly).
        infoMap.put(new Info("tensorrt_llm::layers::createLayer", "tensorrt_llm::layers::DecodingLayerWorkspace::borrow",
                "tensorrt_llm::layers::DecodingLayerWorkspace::mirrorInWorkspace").skip());
    }
}
