package org.bytedeco.tensorrt_llm.test;

import org.bytedeco.javacpp.*;
import tensorrt_llm.layers.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 测试JavaCPP生成的Layer类基本功能。
 * 所有Layer类都生成在 {@code tensorrt_llm.layers} 包下。
 */
public class LayerClassTest {

    private static final String PKG = "tensorrt_llm.layers.";

    @Test
    public void testBaseLayerExists() {
        assertClassFound("BaseLayer");
    }

    @Test
    public void testTopKSamplingLayerExists() {
        assertClassFound("TopKSamplingLayer");
    }

    @Test
    public void testTopPSamplingLayerExists() {
        assertClassFound("TopPSamplingLayer");
    }

    @Test
    public void testSamplingLayerExists() {
        assertClassFound("SamplingLayer");
    }

    @Test
    public void testDecodingLayerExists() {
        assertClassFound("DecodingLayer");
    }

    @Test
    public void testDynamicDecodeLayerExists() {
        assertClassFound("DynamicDecodeLayer");
    }

    @Test
    public void testPenaltyLayerExists() {
        assertClassFound("PenaltyLayer");
    }

    @Test
    public void testBanWordsLayerExists() {
        assertClassFound("BanWordsLayer");
    }

    @Test
    public void testStopCriteriaLayerExists() {
        assertClassFound("StopCriteriaLayer");
    }

    @Test
    public void testBeamSearchLayerExists() {
        assertClassFound("BeamSearchLayer");
    }

    @Test
    public void testMedusaDecodingLayerExists() {
        assertClassFound("MedusaDecodingLayer");
    }

    @Test
    public void testEagleDecodingLayerExists() {
        assertClassFound("EagleDecodingLayer");
    }

    @Test
    public void testLookaheadDecodingLayerExists() {
        assertClassFound("LookaheadDecodingLayer");
    }

    @Test
    public void testExplicitDraftTokensLayerExists() {
        assertClassFound("ExplicitDraftTokensLayer");
    }

    @Test
    public void testExternalDraftTokensLayerExists() {
        assertClassFound("ExternalDraftTokensLayer");
    }

    private static void assertClassFound(String simpleName) {
        try {
            Class.forName(PKG + simpleName);
            System.out.println("✓ " + simpleName + " class found");
        } catch (ClassNotFoundException e) {
            fail(simpleName + " class not found: " + e.getMessage());
        }
    }

    @Test
    public void testAllLayerInheritance() {
        String[] layerClasses = {
            "TopKSamplingLayer", "TopPSamplingLayer", "SamplingLayer",
            "PenaltyLayer", "DecodingLayer", "DynamicDecodeLayer",
            "BanWordsLayer", "StopCriteriaLayer", "BeamSearchLayer",
            "MedusaDecodingLayer", "EagleDecodingLayer", "LookaheadDecodingLayer",
            "ExplicitDraftTokensLayer", "ExternalDraftTokensLayer"
        };

        int successCount = 0;
        for (String className : layerClasses) {
            try {
                Class<?> clazz = Class.forName(PKG + className);
                boolean extendsBaseLayerOrPointer = false;
                Class<?> current = clazz;
                while (current != null && current != Object.class) {
                    Class<?> sup = current.getSuperclass();
                    String superName = sup != null ? sup.getSimpleName() : "";
                    if ("BaseLayer".equals(superName) || "Pointer".equals(superName)) {
                        extendsBaseLayerOrPointer = true;
                        break;
                    }
                    current = sup;
                }

                if (extendsBaseLayerOrPointer) {
                    System.out.println("✓ " + className + " has correct inheritance");
                    successCount++;
                } else {
                    System.out.println("⚠ " + className + " inheritance chain unclear");
                }
            } catch (ClassNotFoundException e) {
                System.out.println("✗ " + className + " not found");
            }
        }

        System.out.println("\nInheritance check: " + successCount + "/" + layerClasses.length + " classes verified");
        assertTrue("At least 10 Layer classes should have correct inheritance", successCount >= 10);
    }

    @Test
    public void testPointerConstructor() {
        try {
            Class<?> clazz = Class.forName(PKG + "TopKSamplingLayer");
            clazz.getConstructor(Pointer.class);
            System.out.println("✓ TopKSamplingLayer has Pointer constructor");
        } catch (ClassNotFoundException e) {
            fail("TopKSamplingLayer not found");
        } catch (NoSuchMethodException e) {
            fail("TopKSamplingLayer missing Pointer constructor");
        }
    }
}
