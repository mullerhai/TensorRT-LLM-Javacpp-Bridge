package org.bytedeco.tensorrt_llm.test;

import org.bytedeco.javacpp.Pointer;
import org.junit.Test;

import tensorrt_llm.layers.BaseLayer;
import tensorrt_llm.layers.DecoderDomain;
import tensorrt_llm.layers.SamplingInputs;
import tensorrt_llm.layers.BaseDecodingInputs;
import tensorrt_llm.layers.BaseDecodingOutputs;
import tensorrt_llm.layers.BeamSearchOutputs;
import tensorrt_llm.layers.LookaheadAlgorithm;
import tensorrt_llm.layers.LookaheadPoolManager;

import static org.junit.Assert.*;

/**
 * 使用真实 JavaCPP 生成的 tensorrt_llm.layers.* 类的测试。
 *
 * 旧版测试引用了 org.bytedeco.tensorrt_llm.* 占位类（TopKSamplingLayer 等），
 * 但当前生成的绑定把所有 layer 类放在 tensorrt_llm.layers 包下，且并非所有
 * Top{K,P}SamplingLayer 都被导出。这里只用实际存在的类做静态加载/继承链校验。
 */
public class RealLayerTest {

    @Test
    public void testBaseLayerLoaded() {
        assertNotNull(BaseLayer.class);
        assertTrue("BaseLayer should extend Pointer",
                Pointer.class.isAssignableFrom(BaseLayer.class));
        assertTrue("BaseLayer should declare native methods",
                BaseLayer.class.getDeclaredMethods().length > 0);
    }

    @Test
    public void testLayerSupportTypesLoaded() {
        Class<?>[] types = {
                DecoderDomain.class,
                SamplingInputs.class,
                BaseDecodingInputs.class,
                BaseDecodingOutputs.class,
                BeamSearchOutputs.class,
                LookaheadAlgorithm.class,
                LookaheadPoolManager.class,
        };
        for (Class<?> c : types) {
            assertNotNull(c);
            assertTrue(c.getName() + " should extend Pointer",
                    Pointer.class.isAssignableFrom(c));
        }
    }

    @Test
    public void testKnownLayerClassesByName() {
        String[] layerClasses = {
                "tensorrt_llm.layers.BaseLayer",
                "tensorrt_llm.layers.DecoderDomain",
                "tensorrt_llm.layers.LookaheadAlgorithm",
                "tensorrt_llm.layers.LookaheadPoolManager",
        };
        int loaded = 0;
        for (String name : layerClasses) {
            try {
                Class.forName(name);
                loaded++;
            } catch (ClassNotFoundException ignored) {
                // skip
            }
        }
        assertEquals("All referenced layer classes should load",
                layerClasses.length, loaded);
    }
}

