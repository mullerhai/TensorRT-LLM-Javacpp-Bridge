package example;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.LongPointer;
import tensorrt_llm.executor.*;
import tensorrt_llm.builder.RequestBuilder;
import tensorrt_llm.builder.SamplingConfigBuilder;

//import org.bytedeco.tensorrt_llm.*;
//import org.bytedeco.tensorrt_llm.global.TRTLLM;

import static tensorrt_llm.global.Executor.ModelType.kDECODER_ONLY;

/**
 * 示例 4: Embedding 模型推理
 *
 * 使用 TensorRT-LLM 的 Encoder-Only 模式加载 Embedding 模型。
 * 适用于:
 * - 文本向量化 (text embedding)
 * - 语义搜索
 * - RAG (Retrieval Augmented Generation) 中的文档编码
 *
 * 支持的 Embedding 模型:
 * - BGE 系列 (bge-large-zh, bge-m3)
 * - GTE 系列
 * - E5 系列
 * - Qwen3-Embedding (如果可用)
 *
 * 前置条件:
 *   trtllm-build --model_dir /path/to/embedding-model \
 *       --output_dir /path/to/embedding-engine \
 *       --max_batch_size 128 \
 *       --max_input_len 512
 */
public class EmbeddingInferences {

    public static void main(String[] args) throws Exception {
        String engineDir = args.length > 0 ? args[0] : "/path/to/embedding-engine";

        System.out.println("=== TensorRT-LLM Embedding 模型推理 ===");

        // ============================================
        // 1. 配置 - Embedding 模型配置
        // ============================================
        ExecutorConfig executorConfig = new ExecutorConfig();
        executorConfig.setMaxBeamWidth(1);

        KvCacheConfig kvCacheConfig = new KvCacheConfig();
        kvCacheConfig.setFreeGpuMemoryFraction(0.9f);
        executorConfig.setKvCacheConfig(kvCacheConfig);

        // ============================================
        // 2. 加载引擎
        // ============================================
        Executor executor = new Executor(
                new BytePointer(engineDir), kDECODER_ONLY.value, executorConfig
        );
        System.out.println("✅ Embedding 引擎加载成功");

        // ============================================
        // 3. 批量编码文档
        // ============================================
        String[] documents = {
                "TensorRT-LLM 是 NVIDIA 开发的高性能 LLM 推理引擎",
                "JavaCPP 可以将 C++ 库绑定到 Java",
                "向量数据库用于存储和检索 embedding 向量",
                "RAG 技术结合了检索和生成两个步骤",
                "Qwen3 是一个强大的开源大语言模型"
        };

        System.out.println("\n编码 " + documents.length + " 个文档...\n");

        long[] requestIds = new long[documents.length];
        for (int i = 0; i < documents.length; i++) {
            int[] tokens = tokenize(documents[i]);

            IntPointer inputTokens = new IntPointer(tokens.length);
            for (int j = 0; j < tokens.length; j++) {
                inputTokens.put(j, tokens[j]);
            }

            // Embedding 模型: maxTokens=1 (只需要 encoder 输出，不需要生成)
            Request request = new Request(inputTokens, 1);
            request.setStreaming(false);

            // 对于 Encoder-Only 模型，设置 requestType
            // request.setRequestType(TRTLLM.REQUEST_TYPE_CONTEXT_ONLY);

            // 需要返回 encoder output 用于提取 embedding
            OutputConfig outputConfig = new OutputConfig();
//             outputConfig.setReturnEncoderOutput(true);
            request.setOutputConfig(outputConfig);

            requestIds[i] = executor.enqueueRequest(request);
        }

        // ============================================
        // 4. 收集 Embedding 结果
        // ============================================
        float[][] embeddings = new float[documents.length][];

        int completed = 0;
        while (completed < documents.length) {
            for (int i = 0; i < documents.length; i++) {
                if (embeddings[i] != null) continue;

                LongPointer reqIdPtr = new LongPointer(1);
                reqIdPtr.put(0, requestIds[i]);
                if (executor.getNumResponsesReady(reqIdPtr) > 0) {
                    // TODO: 从 response 中获取 encoder output 作为 embedding
                    // Response response = executor.awaitResponses(requestIds[i]).get(0);
                    // Tensor encoderOutput = response.getResult().encoderOutput();
                    // embeddings[i] = extractEmbedding(encoderOutput);

                    embeddings[i] = new float[]{0.1f, 0.2f, 0.3f}; // 占位
                    completed++;
                    System.out.printf("  ✅ 文档 [%d] 编码完成, dim=%d%n", i, embeddings[i].length);
                }
            }
            Thread.sleep(10);

            // 临时退出 - 实际使用中应等待所有完成
            if (completed == 0) break;
        }

        // ============================================
        // 5. 使用 Embedding (示例: 计算相似度)
        // ============================================
        System.out.println("\n=== 文档相似度矩阵 ===");
        System.out.println("(实际使用中，可将 embedding 存入向量数据库如 Milvus/Faiss)");

        // 关闭
        executor.shutdown();
        System.out.println("\n✅ Embedding 推理完成");
    }

    /**
     * 从 encoder output tensor 中提取 embedding 向量
     * 通常取 [CLS] token 的 hidden state，或对所有 token 做 mean pooling
     */
    private static float[] extractEmbedding(Tensor encoderOutput) {
        // 方法 1: CLS pooling - 取第一个 token 的 hidden state
        // 方法 2: Mean pooling - 对所有 token 的 hidden state 取平均
        // 方法 3: Last token pooling - 取最后一个 token

        // 实际实现取决于模型的 pooling 策略
        return new float[1536]; // 返回 embedding 向量
    }

    private static int[] tokenize(String text) {
        // 占位 - 实际应使用 Embedding 模型对应的 tokenizer
        return new int[]{101, 200, 300, 400, 102}; // [CLS] ... [SEP]
    }
}

