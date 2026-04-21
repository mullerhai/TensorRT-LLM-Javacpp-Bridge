package org.bytedeco.tensorrt_llm.builder;

import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.tensorrt_llm.*;

/**
 * ExecutorConfig 链式 Builder
 *
 * 用法:
 * <pre>
 * ExecutorConfig config = ExecutorConfigBuilder.builder()
 *     .maxBeamWidth(1)
 *     .enableChunkedContext(true)
 *     .kvCacheConfig(KvCacheConfigBuilder.builder()
 *         .enableBlockReuse(true)
 *         .freeGpuMemoryFraction(0.85f)
 *         .build())
 *     .schedulerConfig(new SchedulerConfig())
 *     .build();
 * </pre>
 */
public class ExecutorConfigBuilder {
    private final ExecutorConfig config;

    private ExecutorConfigBuilder() {
        this.config = new ExecutorConfig();
    }

    public static ExecutorConfigBuilder builder() {
        return new ExecutorConfigBuilder();
    }

    // --- 基础参数 ---

    public ExecutorConfigBuilder maxBeamWidth(int v) { config.setMaxBeamWidth(v); return this; }
    public ExecutorConfigBuilder enableChunkedContext(boolean v) { config.setEnableChunkedContext(v); return this; }
    public ExecutorConfigBuilder normalizeLogProbs(boolean v) { config.setNormalizeLogProbs(v); return this; }
    public ExecutorConfigBuilder iterStatsMaxIterations(int v) { config.setIterStatsMaxIterations(v); return this; }
    public ExecutorConfigBuilder requestStatsMaxIterations(int v) { config.setRequestStatsMaxIterations(v); return this; }
    public ExecutorConfigBuilder useGpuDirectStorage(boolean v) { config.setUseGpuDirectStorage(v); return this; }
    public ExecutorConfigBuilder gpuWeightsPercent(float v) { config.setGpuWeightsPercent(v); return this; }
    public ExecutorConfigBuilder gatherGenerationLogits(boolean v) { config.setGatherGenerationLogits(v); return this; }
    public ExecutorConfigBuilder promptTableOffloading(boolean v) { config.setPromptTableOffloading(v); return this; }

    // --- 子配置 ---

    public ExecutorConfigBuilder kvCacheConfig(KvCacheConfig v) { config.setKvCacheConfig(v); return this; }
    public ExecutorConfigBuilder schedulerConfig(SchedulerConfig v) { config.setSchedulerConfig(v); return this; }
    public ExecutorConfigBuilder parallelConfig(ParallelConfig v) { config.setParallelConfig(v); return this; }
    public ExecutorConfigBuilder peftCacheConfig(PeftCacheConfig v) { config.setPeftCacheConfig(v); return this; }
    public ExecutorConfigBuilder decodingConfig(DecodingConfig v) { config.setDecodingConfig(v); return this; }
    public ExecutorConfigBuilder debugConfig(DebugConfig v) { config.setDebugConfig(v); return this; }
    public ExecutorConfigBuilder specDecConfig(SpeculativeDecodingConfig v) { config.setSpecDecConfig(v); return this; }
    public ExecutorConfigBuilder guidedDecodingConfig(GuidedDecodingConfig v) { config.setGuidedDecodingConfig(v); return this; }
    public ExecutorConfigBuilder logitsPostProcessorConfig(LogitsPostProcessorConfig v) { config.setLogitsPostProcessorConfig(v); return this; }
    public ExecutorConfigBuilder extendedRuntimePerfKnobConfig(ExtendedRuntimePerfKnobConfig v) { config.setExtendedRuntimePerfKnobConfig(v); return this; }

    // --- 可选 int 参数 ---

    public ExecutorConfigBuilder maxBatchSize(int v) { config.setMaxBatchSize(v); return this; }
    public ExecutorConfigBuilder maxNumTokens(int v) { config.setMaxNumTokens(v); return this; }
    public ExecutorConfigBuilder maxQueueSize(int v) { config.setMaxQueueSize(new IntPointer(1).put(v)); return this; }

    public ExecutorConfig build() { return config; }
}
