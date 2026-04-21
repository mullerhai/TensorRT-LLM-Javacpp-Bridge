package org.bytedeco.tensorrt_llm.builder;

import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.LongPointer;
import org.bytedeco.tensorrt_llm.SamplingConfig;

/**
 * SamplingConfig 链式 Builder
 *
 * 隐藏了 FloatPointer/IntPointer 的复杂性，直接使用原始类型。
 *
 * 用法:
 * <pre>
 * SamplingConfig config = SamplingConfigBuilder.builder()
 *     .temperature(0.7f)
 *     .topP(0.9f)
 *     .topK(50)
 *     .seed(42L)
 *     .repetitionPenalty(1.1f)
 *     .build();
 * </pre>
 */
public class SamplingConfigBuilder {
    private final SamplingConfig config;

    private SamplingConfigBuilder() {
        this.config = new SamplingConfig();
    }

    public static SamplingConfigBuilder builder() {
        return new SamplingConfigBuilder();
    }

    // --- 核心采样参数 ---

    public SamplingConfigBuilder beamWidth(int v) { config.setBeamWidth(v); return this; }
    public SamplingConfigBuilder temperature(float v) { config.setTemperature(new FloatPointer(1).put(v)); return this; }
    public SamplingConfigBuilder topK(int v) { config.setTopK(new IntPointer(1).put(v)); return this; }
    public SamplingConfigBuilder topP(float v) { config.setTopP(new FloatPointer(1).put(v)); return this; }
    public SamplingConfigBuilder topPMin(float v) { config.setTopPMin(new FloatPointer(1).put(v)); return this; }
    public SamplingConfigBuilder topPDecay(float v) { config.setTopPDecay(new FloatPointer(1).put(v)); return this; }
    public SamplingConfigBuilder topPResetIds(int v) { config.setTopPResetIds(new IntPointer(1).put(v)); return this; }
    public SamplingConfigBuilder seed(long v) { config.setSeed(new LongPointer(1).put(v)); return this; }
    public SamplingConfigBuilder minP(float v) { config.setMinP(new FloatPointer(1).put(v)); return this; }

    // --- 惩罚参数 ---

    public SamplingConfigBuilder repetitionPenalty(float v) { config.setRepetitionPenalty(new FloatPointer(1).put(v)); return this; }
    public SamplingConfigBuilder presencePenalty(float v) { config.setPresencePenalty(new FloatPointer(1).put(v)); return this; }
    public SamplingConfigBuilder frequencyPenalty(float v) { config.setFrequencyPenalty(new FloatPointer(1).put(v)); return this; }
    public SamplingConfigBuilder lengthPenalty(float v) { config.setLengthPenalty(new FloatPointer(1).put(v)); return this; }
    public SamplingConfigBuilder noRepeatNgramSize(int v) { config.setNoRepeatNgramSize(new IntPointer(1).put(v)); return this; }

    // --- 生成控制 ---

    public SamplingConfigBuilder minTokens(int v) { config.setMinTokens(new IntPointer(1).put(v)); return this; }
    public SamplingConfigBuilder earlyStopping(int v) { config.setEarlyStopping(new IntPointer(1).put(v)); return this; }
    public SamplingConfigBuilder beamSearchDiversityRate(float v) { config.setBeamSearchDiversityRate(new FloatPointer(1).put(v)); return this; }
    public SamplingConfigBuilder numReturnSequences(int v) { config.setNumReturnSequences(new IntPointer(1).put(v)); return this; }

    public SamplingConfig build() { return config; }
}

