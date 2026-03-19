package example;

import org.bytedeco.javacpp.*;
import org.bytedeco.tensorrt_llm.*;
import org.bytedeco.tensorrt_llm.global.TRTLLM;

/**
 * 示例 2: Qwen3 离线批量推理
 *
 * 批量提交多个请求，一次性获取所有结果。适合离线批处理场景。
 *
 * 使用场景:
 * - 大规模数据标注
 * - 离线内容生成
 * - 批量文本摘要
 * - 离线翻译
 */
public class Qwen3BatchInference {

    private static final int EOS_TOKEN_ID = 151645;
    private static final int PAD_TOKEN_ID = 151643;

    public static void main(String[] args) throws Exception {
        String engineDir = args.length > 0 ? args[0] : "/path/to/qwen3-engine";

        System.out.println("=== TensorRT-LLM Qwen3 离线批量推理 ===");

        // ============================================
        // 1. 配置 - 针对批量推理优化
        // ============================================
        KvCacheConfig kvCacheConfig = new KvCacheConfig();
        kvCacheConfig.setEnableBlockReuse(true);
        kvCacheConfig.setFreeGpuMemoryFraction(0.9f); // 批量推理可以用更多内存

        SchedulerConfig schedulerConfig = new SchedulerConfig();
        // GUARANTEED_NO_EVICT: 保证请求不会被中途驱逐，适合离线批处理
        // schedulerConfig.setCapacitySchedulerPolicy(
        //     TRTLLM.kGUARANTEED_NO_EVICT
        // );

        ExecutorConfig executorConfig = new ExecutorConfig();
        executorConfig.setKvCacheConfig(kvCacheConfig);
        executorConfig.setSchedulerConfig(schedulerConfig);
        executorConfig.setEnableChunkedContext(true);
        executorConfig.setMaxBeamWidth(1);

        // ============================================
        // 2. 加载引擎
        // ============================================
        org.bytedeco.tensorrt_llm.Executor executor = new org.bytedeco.tensorrt_llm.Executor(
                new BytePointer(engineDir), TRTLLM.ModelType.kDECODER_ONLY, executorConfig
        );
        System.out.println("✅ 引擎加载成功");

        // ============================================
        // 3. 构造批量请求
        // ============================================
        String[] prompts = {
                "请用三句话总结量子力学的核心思想。",
                "Java和Python的主要区别是什么？",
                "写一首关于春天的五言绝句。",
                "解释什么是Transformer架构。",
                "TensorRT-LLM 的主要优势有哪些？"
        };

        // 默认的 SamplingConfig
        SamplingConfig samplingConfig = new SamplingConfig();
        samplingConfig.setTemperature(new FloatPointer(1).put(0.3f));  // 低温度，输出更确定性
        samplingConfig.setTopP(new FloatPointer(1).put(0.95f));

        // 存放所有请求的 ID
        long[] requestIds = new long[prompts.length];

        for (int i = 0; i < prompts.length; i++) {
            // 实际使用中，这里应该用 Qwen3Tokenizer 对 prompt 进行 tokenize
            // 这里用占位 token IDs
            int[] tokenizedPrompt = tokenize(prompts[i]);

            IntPointer inputTokens = new IntPointer(tokenizedPrompt.length);
            for (int j = 0; j < tokenizedPrompt.length; j++) {
                inputTokens.put(j, tokenizedPrompt[j]);
            }

            Request request = new Request(inputTokens, 256);  // 最大生成 256 tokens
            request.setStreaming(false);                       // 非流式，一次返回完整结果
            request.setSamplingConfig(samplingConfig);
            request.setEndId(EOS_TOKEN_ID);
            request.setPadId(PAD_TOKEN_ID);

            // 提交请求
            requestIds[i] = executor.enqueueRequest(request);
            System.out.printf("  提交请求 [%d]: %s... (requestId=%d)%n",
                    i, prompts[i].substring(0, Math.min(20, prompts[i].length())), requestIds[i]);
        }
        System.out.println("\n✅ 所有 " + prompts.length + " 个请求已提交");

        // ============================================
        // 4. 收集所有结果
        // ============================================
        System.out.println("\n等待推理完成...\n");

        int completedCount = 0;
        boolean[] completed = new boolean[prompts.length];

        while (completedCount < prompts.length) {
            // 轮询检查是否有响应
            for (int i = 0; i < prompts.length; i++) {
                if (completed[i]) continue;

                // 检查该请求是否有响应就绪
                LongPointer reqIdPtr = new LongPointer(1);
                reqIdPtr.put(0, requestIds[i]);
                int numReady = executor.getNumResponsesReady(reqIdPtr);

                if (numReady > 0) {
                    // TODO: 调用 awaitResponses 获取结果
                    // Response response = executor.awaitResponses(requestIds[i]).get(0);
                    // Result result = response.getResult();
                    //
                    // 解码输出 tokens:
                    // String output = detokenize(result.getOutputTokenIds());
                    // System.out.printf("--- 请求 [%d] 完成 ---%n", i);
                    // System.out.printf("  输入: %s%n", prompts[i]);
                    // System.out.printf("  输出: %s%n%n", output);

                    completed[i] = true;
                    completedCount++;
                    System.out.printf("  ✅ 请求 [%d] 完成 (requestId=%d)%n", i, requestIds[i]);
                }
            }

            if (completedCount < prompts.length) {
                Thread.sleep(50); // 等待 50ms 再检查
            }
        }

        System.out.println("\n✅ 所有请求完成! 共处理 " + prompts.length + " 个请求");

        // ============================================
        // 5. 关闭
        // ============================================
        executor.shutdown();
        System.out.println("✅ Executor 已关闭");
    }

    /**
     * 模拟 tokenize - 实际使用中请调用 Qwen3 Tokenizer
     * 推荐使用 HuggingFace tokenizers 的 Java 绑定
     * 例如: https://github.com/huggingface/tokenizers (Rust + JNI)
     */
    private static int[] tokenize(String text) {
        // 占位实现 - 实际应使用 Qwen3 的 tokenizer
        // 可以通过以下方式实现:
        // 1. 使用 tokenizers-java (HuggingFace 的 Rust tokenizer 的 Java 绑定)
        // 2. 使用 Python subprocess 调用 transformers tokenizer
        // 3. 使用 JNI 调用 sentencepiece
        return new int[]{151644, 882, 198, 100, 200, 300, 151645, 198, 151644, 77091, 198};
    }
}

