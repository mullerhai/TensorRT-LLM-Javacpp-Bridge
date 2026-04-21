package example;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.LongPointer;
import tensorrt_llm.executor.Executor;
import tensorrt_llm.executor.ExecutorConfig;
import tensorrt_llm.executor.KvCacheConfig;
import tensorrt_llm.executor.Request;
import tensorrt_llm.builder.RequestBuilder;
import tensorrt_llm.builder.SamplingConfigBuilder;
//import org.bytedeco.tensorrt_llm.executor.Executor;
//import org.bytedeco.tensorrt_llm.executor.ExecutorConfig;
//import org.bytedeco.tensorrt_llm.executor.KvCacheConfig;
//import org.bytedeco.tensorrt_llm.executor.Request;
//import tensorrt_llm.global.TRTLLM;

import java.util.ArrayList;
import java.util.List;

import static tensorrt_llm.global.Executor.ModelType.kDECODER_ONLY;

final class OfflineInferenceExamples {
    private static final int EOS_TOKEN_ID = 151645;
    private static final int PAD_TOKEN_ID = 151643;

    private OfflineInferenceExamples() {
    }

    static void runLab1TrtGeneration(String[] args) {
        TutorialCommon.requireArgs(args, 2,
                "lab1-trt <engineDir> [prompt] [maxNewTokens]");

        String engineDir = args[1];
        String prompt = args.length > 2 ? args[2] : "请用三句话解释 Transformer 的核心思想。";
        int maxNewTokens = args.length > 3 ? Integer.parseInt(args[3]) : 256;

        int[] inputTokenIds = mockTokenize(prompt);

        KvCacheConfig kvCacheConfig = new KvCacheConfig();
        kvCacheConfig.setEnableBlockReuse(true);
        kvCacheConfig.setFreeGpuMemoryFraction(0.85f);

        ExecutorConfig executorConfig = new ExecutorConfig();
        executorConfig.setKvCacheConfig(kvCacheConfig);
        executorConfig.setEnableChunkedContext(true);
        executorConfig.setMaxBeamWidth(1);

        try (Executor executor = new Executor(
                new BytePointer(engineDir), kDECODER_ONLY, executorConfig)) {
            Request request = RequestBuilder.builder(inputTokenIds, maxNewTokens)
                    .streaming(false)
                    .endId(EOS_TOKEN_ID)
                    .padId(PAD_TOKEN_ID)
                    .samplingConfig(SamplingConfigBuilder.builder()
                            .temperature(0.7f)
                            .topP(0.9f)
                            .topK(50)
                            .build())
                    .build();

            long requestId = executor.enqueueRequest(request);
            waitUntilReady(executor, requestId);

            System.out.println("Lab1(TRT-LLM) request submitted and marked ready. requestId=" + requestId);
            System.out.println("Note: response decoding API is currently not generated in JavaCPP bindings.");
        }
    }

    static void runLab2Batch(String[] args) {
        TutorialCommon.requireArgs(args, 2,
                "lab2-batch <engineDir>");

        String engineDir = args[1];
        List<String> prompts = List.of(
                "总结一下 FP8 量化在推理中的作用。",
                "解释 TensorRT-LLM 的 paged KV cache。",
                "写一段 100 字以内的 Java 性能优化建议。",
                "什么是 continuous batching?",
                "给一个离线批推理队列设计示例。"
        );

        KvCacheConfig kvCacheConfig = new KvCacheConfig();
        kvCacheConfig.setEnableBlockReuse(true);
        kvCacheConfig.setFreeGpuMemoryFraction(0.90f);

        ExecutorConfig executorConfig = new ExecutorConfig();
        executorConfig.setKvCacheConfig(kvCacheConfig);
        executorConfig.setEnableChunkedContext(true);
        executorConfig.setMaxBeamWidth(1);

        try (Executor executor = new Executor(
                new BytePointer(engineDir), kDECODER_ONLY, executorConfig)) {
            List<Long> requestIds = new ArrayList<>();
            for (String prompt : prompts) {
                int[] inputTokenIds = mockTokenize(prompt);
                Request request = RequestBuilder.builder(inputTokenIds, 256)
                        .streaming(false)
                        .endId(EOS_TOKEN_ID)
                        .padId(PAD_TOKEN_ID)
                        .samplingConfig(SamplingConfigBuilder.builder()
                                .temperature(0.3f)
                                .topP(0.95f)
                                .build())
                        .build();
                requestIds.add(executor.enqueueRequest(request));
            }

            for (long requestId : requestIds) {
                waitUntilReady(executor, requestId);
                System.out.println("Lab2 batch request ready: " + requestId);
            }

            System.out.println("Lab2 batch flow completed for " + requestIds.size() + " requests.");
        }
    }

    private static void waitUntilReady(Executor executor, long requestId) {
        LongPointer ptr = new LongPointer(1);
        ptr.put(0, requestId);
        while (executor.getNumResponsesReady() == 0) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for response", e);
            }
        }
    }

    private static int[] mockTokenize(String prompt) {
        int base = Math.abs(prompt.hashCode() % 10000);
        return new int[]{151644, 882, 198, base, base + 1, base + 2, 151645, 198, 151644, 77091, 198};
    }
}


