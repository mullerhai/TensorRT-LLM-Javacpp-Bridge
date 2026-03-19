package org.bytedeco.tensorrt_llm.builder;

import org.bytedeco.javacpp.*;
import org.bytedeco.tensorrt_llm.executor.*;

/**
 * KvCacheConfig 链式 Builder
 *
 * 用法:
 * <pre>
 * KvCacheConfig config = KvCacheConfigBuilder.builder()
 *     .enableBlockReuse(true)
 *     .freeGpuMemoryFraction(0.85f)
 *     .maxTokens(8192)
 *     .build();
 * </pre>
 */
public class KvCacheConfigBuilder {
    private final KvCacheConfig config;

    private KvCacheConfigBuilder() {
        this.config = new KvCacheConfig();
    }

    public static KvCacheConfigBuilder builder() {
        return new KvCacheConfigBuilder();
    }

    public KvCacheConfigBuilder enableBlockReuse(boolean v) { config.setEnableBlockReuse(v); return this; }
    public KvCacheConfigBuilder freeGpuMemoryFraction(float v) { config.setFreeGpuMemoryFraction(v); return this; }
    public KvCacheConfigBuilder maxTokens(int v) { config.setMaxTokens(new IntPointer(1).put(v)); return this; }
    public KvCacheConfigBuilder sinkTokenLength(int v) { config.setSinkTokenLength(v); return this; }
    public KvCacheConfigBuilder hostCacheSize(long v) { config.setHostCacheSize(v); return this; }
    public KvCacheConfigBuilder onboardBlocks(boolean v) { config.setOnboardBlocks(v); return this; }
    public KvCacheConfigBuilder maxGpuTotalBytes(long v) { config.setMaxGpuTotalBytes(v); return this; }

    public KvCacheConfig build() { return config; }
}
