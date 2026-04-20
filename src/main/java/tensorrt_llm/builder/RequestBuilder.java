package tensorrt_llm.builder;

import tensorrt_llm.executor.EagleConfig;
import tensorrt_llm.executor.ExternalDraftTokensConfig;
import tensorrt_llm.executor.GuidedDecodingParams;
import tensorrt_llm.executor.KvCacheRetentionConfig;
import tensorrt_llm.executor.LookaheadDecodingConfig;
import tensorrt_llm.executor.MropeConfig;
import tensorrt_llm.executor.MultimodalInput;
import tensorrt_llm.executor.OutputConfig;
import tensorrt_llm.executor.PromptTuningConfig;
import tensorrt_llm.executor.Request;
import tensorrt_llm.executor.SamplingConfig;
import org.bytedeco.javacpp.IntPointer;
import tensorrt_llm.executor.*;
import tensorrt_llm.builder.SamplingConfigBuilder;
//import org.bytedeco.tensorrt_llm.*;

/**
 * Request 链式 Builder
 *
 * 用法:
 * <pre>
 * Request request = RequestBuilder.builder(inputTokenIds, 512)
 *     .streaming(true)
 *     .endId(151645)
 *     .padId(151643)
 *     .samplingConfig(SamplingConfigBuilder.builder()
 *         .temperature(0.7f)
 *         .topP(0.9f)
 *         .build())
 *     .build();
 * </pre>
 */
public class RequestBuilder {
    private final Request request;

    private RequestBuilder(int[] inputTokenIds, int maxTokens) {
        IntPointer ptr = new IntPointer(inputTokenIds.length);
        for (int i = 0; i < inputTokenIds.length; i++) {
            ptr.put(i, inputTokenIds[i]);
        }
        this.request = new Request(ptr, maxTokens);
    }

    public static RequestBuilder builder(int[] inputTokenIds, int maxTokens) {
        return new RequestBuilder(inputTokenIds, maxTokens);
    }

    // --- 基础参数 ---

    public RequestBuilder streaming(boolean v) { request.setStreaming(v); return this; }
    public RequestBuilder endId(int v) { request.setEndId(v); return this; }
    public RequestBuilder padId(int v) { request.setPadId(v); return this; }
    public RequestBuilder returnAllGeneratedTokens(boolean v) { request.setReturnAllGeneratedTokens(v); return this; }

    // --- 子配置 ---

    public RequestBuilder samplingConfig(SamplingConfig v) { request.setSamplingConfig(v); return this; }
    public RequestBuilder outputConfig(OutputConfig v) { request.setOutputConfig(v); return this; }
    public RequestBuilder loraConfig(LoraConfig v) { request.setLoraConfig(v); return this; }
    public RequestBuilder promptTuningConfig(PromptTuningConfig v) { request.setPromptTuningConfig(v); return this; }
    public RequestBuilder mrope(MropeConfig v) { request.setMropeConfig(v); return this; }
    // MultimodalInput is skipped in JavaCPP due to nested vector issues
    // public RequestBuilder multimodalInput(MultimodalInput v) { request.setMultimodalInput(v); return this; }
    public RequestBuilder multimodalEmbedding(Tensor v) { request.setMultimodalEmbedding(v); return this; }
    public RequestBuilder eagleConfig(EagleConfig v) { request.setEagleConfig(v); return this; }
    public RequestBuilder externalDraftTokensConfig(ExternalDraftTokensConfig v) { request.setExternalDraftTokensConfig(v); return this; }
    public RequestBuilder lookaheadConfig(LookaheadDecodingConfig v) { request.setLookaheadConfig(v); return this; }
    public RequestBuilder kvCacheRetentionConfig(KvCacheRetentionConfig v) { request.setKvCacheRetentionConfig(v); return this; }
    public RequestBuilder guidedDecodingParams(GuidedDecodingParams v) { request.setGuidedDecodingParams(v); return this; }

    // --- 可选参数 ---

    public RequestBuilder clientId(long v) { request.setClientId(v); return this; }
    public RequestBuilder priority(float v) { request.setPriority(v); return this; }

    // --- Encoder-Decoder ---

    public RequestBuilder encoderInputTokenIds(int[] v) {
        IntPointer ptr = new IntPointer(v.length);
        for (int i = 0; i < v.length; i++) ptr.put(i, v[i]);
        request.setEncoderInputTokenIds(ptr);
        return this;
    }

    public Request build() { return request; }
}

