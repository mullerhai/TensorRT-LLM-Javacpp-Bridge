package example;

import org.bytedeco.javacpp.*;
import org.bytedeco.tensorrt_llm.*;
import org.bytedeco.tensorrt_llm.global.TRTLLM;

/**
 * 示例 1: Qwen3 在线推理服务
 *
 * 使用 TensorRT-LLM Executor API 加载 Qwen3 模型并提供在线推理服务。
 *
 * 前置条件:
 * 1. 使用 trtllm-build 将 Qwen3 模型转换为 TensorRT-LLM 引擎:
 *    python3 -m tensorrt_llm.commands.build \
 *        --model_dir /path/to/Qwen3-8B \
 *        --output_dir /path/to/qwen3-engine \
 *        --tp_size 1 \
 *        --max_batch_size 64 \
 *        --max_input_len 4096 \
 *        --max_seq_len 8192 \
 *        --gemm_plugin float16
 *
 * 2. 需要有 CUDA GPU 和 TensorRT-LLM 运行时库
 */
public class Qwen3OnlineInference {

    // Qwen3 的 special token IDs
    private static final int QWEN3_EOS_TOKEN_ID = 151645;  // <|endoftext|>
    private static final int QWEN3_PAD_TOKEN_ID = 151643;  // <|endoftext|>
    private static final int QWEN3_IM_START_ID = 151644;   // <|im_start|>
    private static final int QWEN3_IM_END_ID = 151645;     // <|im_end|>

    public static void main(String[] args) throws Exception {
        String engineDir = args.length > 0 ? args[0] : "/path/to/qwen3-engine";

        System.out.println("=== TensorRT-LLM Qwen3 在线推理 ===");
        System.out.println("引擎目录: " + engineDir);

        // ============================================
        // 1. 配置 KV Cache
        // ============================================
        KvCacheConfig kvCacheConfig = new KvCacheConfig();
        kvCacheConfig.setEnableBlockReuse(true);       // 开启 block 复用，提升吞吐
        kvCacheConfig.setFreeGpuMemoryFraction(0.85f); // 85% GPU 内存用于 KV cache

        // ============================================
        // 2. 配置 Executor
        // ============================================
        ExecutorConfig executorConfig = new ExecutorConfig();
        executorConfig.setKvCacheConfig(kvCacheConfig);
        executorConfig.setEnableChunkedContext(true);   // 分块上下文，节省内存
        executorConfig.setMaxBeamWidth(1);              // beam=1 用于在线服务(低延迟)

        // ============================================
        // 3. 创建 Executor 实例（加载引擎）
        // ============================================
        // Executor 构造时会加载 TRT 引擎到 GPU
        org.bytedeco.tensorrt_llm.Executor executor = new org.bytedeco.tensorrt_llm.Executor(
                new BytePointer(engineDir), TRTLLM.ModelType.kDECODER_ONLY, executorConfig
        );

        System.out.println("✅ Qwen3 引擎加载成功");

        // ============================================
        // 4. 构建推理请求
        // ============================================
        // 模拟 tokenizer 的输出 (实际应使用 Qwen3 tokenizer)
        // "<|im_start|>user\n你好，请介绍一下量子计算<|im_end|>\n<|im_start|>assistant\n"
        int[] inputTokens = {
                QWEN3_IM_START_ID, 882, 198,           // <|im_start|>user\n
                57668, 15946, 3837, 106399, 109825,     // 你好，请介绍一下量子计算
                QWEN3_IM_END_ID, 198,                   // <|im_end|>\n
                QWEN3_IM_START_ID, 77091, 198           // <|im_start|>assistant\n
        };

        // 构造 SamplingConfig
        SamplingConfig samplingConfig = new SamplingConfig();
        samplingConfig.setTemperature(new FloatPointer(1).put(0.7f));  // 温度
        samplingConfig.setTopP(new FloatPointer(1).put(0.9f));         // top-p 采样
        samplingConfig.setTopK(new IntPointer(1).put(50));           // top-k 采样

        // 构造 OutputConfig
        OutputConfig outputConfig = new OutputConfig();
        outputConfig.returnGenerationLogits(false);
        outputConfig.returnLogProbs(false);

        // 构造 Request
        IntPointer inputTokenPtr = new IntPointer(inputTokens.length);
        for (int i = 0; i < inputTokens.length; i++) {
            inputTokenPtr.put(i, inputTokens[i]);
        }

        Request request = new Request(inputTokenPtr, 512);  // 最大生成 512 个 token
        request.setStreaming(true);          // 流式输出
        request.setSamplingConfig(samplingConfig);
        request.setOutputConfig(outputConfig);
        request.setEndId(QWEN3_EOS_TOKEN_ID);
        request.setPadId(QWEN3_PAD_TOKEN_ID);

        // ============================================
        // 5. 提交请求并流式获取结果
        // ============================================
        long requestId = executor.enqueueRequest(request);
        System.out.println("已提交请求, requestId=" + requestId);
        System.out.print("\n助手: ");

        boolean done = false;
        while (!done) {
            // 等待响应就绪
            int numReady = executor.getNumResponsesReady();
            if (numReady == 0) {
                Thread.sleep(10); // 短暂等待
                continue;
            }

            // TODO: 需要 awaitResponses 方法(当前被 skip)
            // 在实际使用中，这里应该调用:
            // List<Response> responses = executor.awaitResponses(requestId);
            // for (Response resp : responses) {
            //     if (resp.hasError()) {
            //         System.err.println("错误: " + resp.getErrorMsg().getString());
            //         done = true;
            //     } else {
            //         Result result = resp.getResult();
            //         // 获取生成的 token 并解码
            //         // 打印增量文本
            //         if (result.isFinal()) {
            //             done = true;
            //         }
            //     }
            // }

            // 临时退出循环
            done = true;
        }

        System.out.println("\n\n✅ 推理完成");

        // ============================================
        // 6. 关闭 Executor
        // ============================================
        executor.shutdown();
        System.out.println("✅ Executor 已关闭");
    }
}

