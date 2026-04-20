package example;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.tensorrt_llm.*;

// Note: GuidedDecodingParams enum constants may not be available as static imports
// import static org.bytedeco.tensorrt_llm.GuidedDecodingParams.kJSON_SCHEMA;
// import static org.bytedeco.tensorrt_llm.GuidedDecodingParams.kREGEX;

/**
 * 示例 6: 高级配置 - LoRA / 投机解码 / Guided Decoding
 *
 * 展示 TensorRT-LLM Java 绑定中的高级功能配置。
 */
public class AdvancedFeatures {

    private static final int EOS_TOKEN_ID = 151645;
    private static final int PAD_TOKEN_ID = 151643;

    public static void main(String[] args) throws Exception {
        String engineDir = args.length > 0 ? args[0] : "/path/to/qwen3-engine";

        // ============================================
        // A. LoRA 适配器推理
        // ============================================
        System.out.println("=== A. LoRA 适配器推理 ===");
        demoLoRA(engineDir);

        // ============================================
        // B. 投机解码 (Speculative Decoding)
        // ============================================
        System.out.println("\n=== B. 投机解码 ===");
        demoSpeculativeDecoding(engineDir);

        // ============================================
        // C. Guided Decoding (JSON 结构化输出)
        // ============================================
        System.out.println("\n=== C. Guided Decoding ===");
        demoGuidedDecoding(engineDir);
    }

    /**
     * A. LoRA 适配器推理
     *
     * 在同一个基座模型上，为不同请求使用不同的 LoRA 适配器。
     * 适用场景: 多租户、多任务
     */
    static void demoLoRA(String engineDir) {
        // 配置 PEFT (LoRA) Cache
        PeftCacheConfig peftCacheConfig = new PeftCacheConfig();
        // peftCacheConfig.setNumHostModuleLayer(4);    // 主机侧缓存的 LoRA 层数
        // peftCacheConfig.setNumDeviceModuleLayer(2);  // 设备侧缓存的 LoRA 层数

        ExecutorConfig config = new ExecutorConfig();
        config.setPeftCacheConfig(peftCacheConfig);

        // 创建请求并指定 LoRA
        int[] tokens = {151644, 882, 198, 100, 200, 151645, 198, 151644, 77091, 198};
        IntPointer inputTokens = new IntPointer(tokens.length);
        for (int i = 0; i < tokens.length; i++) inputTokens.put(i, tokens[i]);

        Request request = new Request(inputTokens, 256);
        request.setEndId(EOS_TOKEN_ID);

        // 配置 LoRA
        LoraConfig loraConfig = new LoraConfig(
                42L  // LoRA task ID - 对应预加载的 LoRA 适配器
                // 也可以指定 LoRA 权重 tensor
        );
        request.setLoraConfig(loraConfig);

        System.out.println("  ✅ LoRA 请求已构建, taskId=42");
        System.out.println("  提示: 使用 trtllm-build 时需指定 --lora_dir 来支持 LoRA");
    }

    /**
     * B. 投机解码 (Speculative Decoding)
     *
     * 使用小模型(draft model)生成候选 token，大模型验证，加速推理。
     */
    static void demoSpeculativeDecoding(String engineDir) {
        // 投机解码配置
        SpeculativeDecodingConfig specDecConfig = new SpeculativeDecodingConfig();
        // 可以配置 draft model 路径等

        ExecutorConfig config = new ExecutorConfig();
        config.setSpecDecConfig(specDecConfig);

        // Eagle 解码 (另一种投机解码方式)
        int[] tokens = {151644, 882, 198, 100, 200, 151645, 198, 151644, 77091, 198};
        IntPointer inputTokens = new IntPointer(tokens.length);
        for (int i = 0; i < tokens.length; i++) inputTokens.put(i, tokens[i]);

        Request request = new Request(inputTokens, 512);
        request.setEndId(EOS_TOKEN_ID);

        // 设置 Eagle 解码配置
        // EagleConfig eagleConfig = new EagleConfig(...);
        // request.setEagleConfig(eagleConfig);

        // 或者 Lookahead 解码
        // LookaheadDecodingConfig lookaheadConfig = new LookaheadDecodingConfig(5, 3, 3);
        // request.setLookaheadConfig(lookaheadConfig);

        System.out.println("  ✅ 投机解码请求已构建");
        System.out.println("  支持的策略: Eagle, Lookahead, Draft Model");
    }

    /**
     * C. Guided Decoding (结构化输出)
     *
     * 强制模型按照指定的 JSON Schema 或正则表达式生成输出。
     * 适用场景: API 返回结构化 JSON、数据提取
     */
    static void demoGuidedDecoding(String engineDir) {
        // JSON Schema 示例 - 强制输出符合指定格式
        String jsonSchema = "{"
                + "\"type\": \"object\","
                + "\"properties\": {"
                + "  \"name\": {\"type\": \"string\"},"
                + "  \"age\": {\"type\": \"integer\", \"minimum\": 0},"
                + "  \"skills\": {\"type\": \"array\", \"items\": {\"type\": \"string\"}}"
                + "},"
                + "\"required\": [\"name\", \"age\", \"skills\"]"
                + "}";

        // 设置 GuidedDecodingConfig         kJSON_SCHEMA,
        GuidedDecodingConfig guidedConfig = new GuidedDecodingConfig(
                new BytePointer(jsonSchema)
        );

        // 在 ExecutorConfig 级别设置全局 guided decoding
        ExecutorConfig config = new ExecutorConfig();
        config.setGuidedDecodingConfig(guidedConfig);

        // 或者在单个请求级别设置
        int[] tokens = {151644, 882, 198, 100, 200, 151645, 198, 151644, 77091, 198};
        IntPointer inputTokens = new IntPointer(tokens.length);
        for (int i = 0; i < tokens.length; i++) inputTokens.put(i, tokens[i]);

        Request request = new Request(inputTokens, 256);
        request.setEndId(EOS_TOKEN_ID);

        // 也可以用正则表达式
        GuidedDecodingParams guidedParams = new GuidedDecodingParams(
                1, // kREGEX enum value
                new BytePointer("[0-9]{4}-[0-9]{2}-[0-9]{2}")  // 日期格式
        );
        request.setGuidedDecodingParams(guidedParams);

        System.out.println("  ✅ Guided Decoding 请求已构建");
        System.out.println("  支持的引导类型:");
        System.out.println("    - JSON Schema: 强制输出符合 JSON 格式");
        System.out.println("    - Regex: 强制输出匹配正则表达式");
        System.out.println("    - Grammar: 使用 BNF 语法约束输出");
    }
}

