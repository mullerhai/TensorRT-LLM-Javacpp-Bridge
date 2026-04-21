package example;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.LongPointer;
import tensorrt_llm.executor.*;
//import org.bytedeco.tensorrt_llm.*;
//import org.bytedeco.tensorrt_llm.global.TRTLLM;

import java.util.concurrent.atomic.AtomicLong;

import static tensorrt_llm.global.Executor.ModelType.kDECODER_ONLY;

/**
 * 示例 5: 完整的 HTTP 在线推理服务
 *
 * 将 TensorRT-LLM 封装为一个线程安全的推理服务，
 * 可以集成到 Spring Boot / Vert.x / Netty 等 Web 框架中。
 *
 * 架构:
 *   HTTP Request → InferenceService.submit() → Executor.enqueueRequest()
 *                                                    ↓
 *   HTTP Response ← InferenceService.poll()  ← Executor responses
 */
public class Qwen3InferenceServices implements AutoCloseable {

    private final Executor executor;
    private final int defaultMaxTokens;
    private final int eosTokenId;
    private final int padTokenId;
    private final AtomicLong requestCounter = new AtomicLong(0);

    /**
     * 初始化推理服务
     *
     * @param engineDir      TRT-LLM 引擎目录
     * @param maxBatchSize   最大批处理大小
     * @param defaultMaxTokens 默认最大生成 token 数
     */
    public Qwen3InferenceServices(String engineDir, int maxBatchSize, int defaultMaxTokens) {
        this.defaultMaxTokens = defaultMaxTokens;
        this.eosTokenId = 151645;
        this.padTokenId = 151643;

        // 配置
        KvCacheConfig kvCacheConfig = new KvCacheConfig();
        kvCacheConfig.setEnableBlockReuse(true);
        kvCacheConfig.setFreeGpuMemoryFraction(0.85f);

        ExecutorConfig executorConfig = new ExecutorConfig();
        executorConfig.setKvCacheConfig(kvCacheConfig);
        executorConfig.setEnableChunkedContext(true);
        executorConfig.setMaxBeamWidth(1);
        executor = new Executor(
                new BytePointer(engineDir), kDECODER_ONLY, executorConfig
        );
        // 加载引擎 TRTLLM.ModelType.kDECODER_ONLY
        System.out.println("✅ InferenceService 初始化完成, engine=" + engineDir);
    }

    // ========================================
    // 核心 API
    // ========================================

    /**
     * 提交推理请求
     *
     * @param inputTokenIds 输入 token IDs
     * @param maxTokens     最大生成 token 数 (0 = 使用默认值)
     * @param temperature   温度参数
     * @param topP          top-p 采样
     * @param topK          top-k 采样
     * @param streaming     是否流式返回
     * @return requestId    请求 ID，用于后续获取结果
     */
    public long submit(int[] inputTokenIds, int maxTokens, float temperature,
                       float topP, int topK, boolean streaming) {

        if (maxTokens <= 0) maxTokens = defaultMaxTokens;

        // 构造 input tokens
        IntPointer inputTokenPtr = new IntPointer(inputTokenIds.length);
        for (int i = 0; i < inputTokenIds.length; i++) {
            inputTokenPtr.put(i, inputTokenIds[i]);
        }

        // SamplingConfig
        SamplingConfig samplingConfig = new SamplingConfig();
        if (temperature > 0) samplingConfig.setTemperature(new FloatPointer(1).put(temperature));
        if (topP > 0) samplingConfig.setTopP(new FloatPointer(1).put(topP));
        if (topK > 0) samplingConfig.setTopK(new IntPointer(1).put(topK));

        // OutputConfig
        OutputConfig outputConfig = new OutputConfig();
        outputConfig.returnLogProbs(false);
        outputConfig.returnGenerationLogits(false);

        // Request
        Request request = new Request(inputTokenPtr, maxTokens);
        request.setStreaming(streaming);
        request.setSamplingConfig(samplingConfig);
        request.setOutputConfig(outputConfig);
        request.setEndId(eosTokenId);
        request.setPadId(padTokenId);

        long requestId = executor.enqueueRequest(request);
        requestCounter.incrementAndGet();
        return requestId;
    }

    /**
     * 简化版提交 - 使用默认参数
     */
    public long submit(int[] inputTokenIds) {
        return submit(inputTokenIds, 0, 0.7f, 0.9f, 50, false);
    }

    /**
     * 检查请求是否有结果可用
     */
    public boolean isReady(long requestId) {
        LongPointer reqIdPtr = new LongPointer(1);
        reqIdPtr.put(0, requestId);
        return executor.getNumResponsesReady() > 0;
    }

    /**
     * 取消请求
     */
    public void cancel(long requestId) {
        executor.cancelRequest(requestId);
    }

    /**
     * 获取服务状态
     */
    public boolean canAcceptRequests() {
        return executor.canEnqueueRequests();
    }

    /**
     * 获取已处理的请求总数
     */
    public long getTotalRequests() {
        return requestCounter.get();
    }

    @Override
    public void close() {
        executor.shutdown();
        System.out.println("✅ InferenceService 已关闭");
    }

    // ========================================
    // 使用示例
    // ========================================
    public static void main(String[] args) throws Exception {
        String engineDir = args.length > 0 ? args[0] : "/path/to/qwen3-engine";

        // 创建服务
        try (Qwen3InferenceServices service = new Qwen3InferenceServices(engineDir, 64, 512)) {

            System.out.println("\n=== 模拟在线请求 ===\n");

            // 模拟多个并发请求
            int[] chatTokens1 = {151644, 882, 198, 100, 200, 151645, 198, 151644, 77091, 198};
            int[] chatTokens2 = {151644, 882, 198, 300, 400, 151645, 198, 151644, 77091, 198};
            int[] chatTokens3 = {151644, 882, 198, 500, 600, 151645, 198, 151644, 77091, 198};

            long id1 = service.submit(chatTokens1, 256, 0.7f, 0.9f, 50, false);
            long id2 = service.submit(chatTokens2, 128, 0.3f, 0.95f, 0, false);
            long id3 = service.submit(chatTokens3);  // 使用默认参数

            System.out.printf("提交了 3 个请求: [%d, %d, %d]%n", id1, id2, id3);
            System.out.println("服务可接受请求: " + service.canAcceptRequests());
            System.out.println("总请求数: " + service.getTotalRequests());

            // 等待完成
            long[] ids = {id1, id2, id3};
            for (long id : ids) {
                while (!service.isReady(id)) {
                    Thread.sleep(10);
                }
                System.out.printf("  ✅ 请求 %d 完成%n", id);
            }

            System.out.println("\n=== 所有请求处理完成 ===");

            // ============================================
            // 集成到 Spring Boot 的示例代码:
            // ============================================
            /*
            @RestController
            public class LLMController {
                @Autowired
                private Qwen3InferenceService inferenceService;

                @PostMapping("/v1/chat/completions")
                public ResponseEntity<?> chat(@RequestBody ChatRequest chatRequest) {
                    int[] tokens = tokenizer.encode(chatRequest.getMessages(),
                    long requestId = inferenceService.submit(
                        tokens,
                        chatRequest.getMaxTokens(),
                        chatRequest.getTemperature(),
                        chatRequest.getTopP(),
                        chatRequest.getTopK(),
                        chatRequest.isStream()
                    );

                    // 等待结果
                    while (!inferenceService.isReady(requestId)) {
                        Thread.sleep(10);
                    }
                    // 获取并返回结果...
                    return ResponseEntity.ok(result);
                }

                @PostMapping("/v1/embeddings")
                public ResponseEntity<?> embeddings(@RequestBody EmbeddingRequest request) {
                    // Embedding 推理
                    int[] tokens = tokenizer.encode(request.getInput(),
                    long requestId = inferenceService.submit(tokens, 1, 0, 0, 0, false);
                    // ...
                }
            }
            */
        }
    }
}

