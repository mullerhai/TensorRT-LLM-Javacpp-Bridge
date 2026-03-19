package org.bytedeco.tensorrt_llm.util;

import org.bytedeco.javacpp.*;
import org.bytedeco.tensorrt_llm.executor.Executor;
import org.bytedeco.tensorrt_llm.executor.ExecutorConfig;
import org.bytedeco.tensorrt_llm.executor.Model;
import org.bytedeco.tensorrt_llm.global.TRTLLM;

import java.io.IOException;
import java.nio.file.*;

/**
 * TensorRT-LLM 模型加载工具
 *
 * 支持多种模型格式和加载方式：
 * <ul>
 *   <li>从目录加载编译好的 TRT-LLM 引擎</li>
 *   <li>从内存 buffer 加载引擎</li>
 *   <li>Encoder-Decoder 模型加载</li>
 *   <li>从预加载的 Model 对象创建</li>
 * </ul>
 *
 * <h3>支持的模型格式</h3>
 * <ul>
 *   <li><b>TRT-LLM Engine (标准格式)</b>: 通过 trtllm-build 编译生成的引擎目录，包含
 *       rank0.engine + config.json</li>
 *   <li><b>AOT (Ahead-of-Time) 格式</b>: 即预编译的 .engine 文件，这是 TRT-LLM 的标准交付格式。
 *       使用 trtllm-build 将 HuggingFace 模型转换后产生。</li>
 *   <li><b>EP (Execution Provider) 格式</b>: 如果指的是 TensorRT EP for ONNX Runtime，
 *       需要先用 trtllm-build 转换为 TRT-LLM 引擎格式才能使用。</li>
 * </ul>
 *
 * <h3>模型转换命令示例</h3>
 * <pre>
 * # Qwen3 转换为 TRT-LLM 引擎 (AOT 编译)
 * python3 -m tensorrt_llm.commands.build \
 *     --model_dir /path/to/Qwen3-8B \
 *     --output_dir /path/to/qwen3-engine \
 *     --tp_size 1 \
 *     --max_batch_size 64 \
 *     --max_input_len 4096 \
 *     --max_seq_len 8192
 *
 * # ONNX → TRT-LLM 引擎
 * trtllm-build --model_dir /path/to/onnx_model \
 *     --output_dir /path/to/engine
 * </pre>
 */
public class ModelLoader {

    private ModelLoader() {} // 工具类

    // ==========================================
    // 1. 从目录加载 (最常用)
    // ==========================================

    /**
     * 从 TRT-LLM 引擎目录加载 Decoder-Only 模型 (如 Qwen3, LLaMA, GPT)
     *
     * @param engineDir  引擎目录路径 (包含 rank0.engine + config.json)
     * @param config     Executor 配置
     * @return Executor 实例
     */
    public static Executor loadDecoderOnly(String engineDir, ExecutorConfig config) {
        validateEngineDir(engineDir);
        return new Executor(new BytePointer(engineDir), TRTLLM.ModelType.kDECODER_ONLY, config);
    }

    /**
     * 从 TRT-LLM 引擎目录加载 Encoder-Only 模型 (如 BERT, BGE Embedding)
     */
    public static Executor loadEncoderOnly(String engineDir, ExecutorConfig config) {
        validateEngineDir(engineDir);
        return new Executor(new BytePointer(engineDir), TRTLLM.ModelType.kENCODER_ONLY, config);
    }

    /**
     * 从 TRT-LLM 引擎目录加载 Encoder-Decoder 模型 (如 T5, Whisper)
     *
     * @param encoderDir Encoder 引擎目录
     * @param decoderDir Decoder 引擎目录
     */
    public static Executor loadEncoderDecoder(String encoderDir, String decoderDir, ExecutorConfig config) {
        validateEngineDir(encoderDir);
        validateEngineDir(decoderDir);
        return new Executor(
                new BytePointer(encoderDir), new BytePointer(decoderDir),
                TRTLLM.ModelType.kENCODER_DECODER, config
        );
    }

    // ==========================================
    // 2. 从内存 Buffer 加载 (适合 AOT 场景)
    // ==========================================

    /**
     * 从 .engine 文件加载引擎 buffer
     *
     * 用于预加载 AOT 编译的引擎到内存，然后创建 Executor。
     * 适合需要精细控制加载时机的场景。
     *
     * @param engineFilePath .engine 文件路径
     * @return 引擎的字节内容
     */
    public static byte[] readEngineFile(String engineFilePath) throws IOException {
        Path path = Paths.get(engineFilePath);
        if (!Files.exists(path)) {
            throw new IOException("Engine file not found: " + engineFilePath);
        }
        return Files.readAllBytes(path);
    }

    /**
     * 从内存中的引擎 buffer 和 JSON 配置创建 Encoder-Decoder Executor
     *
     * @param encoderEngineBuffer Encoder 引擎二进制数据
     * @param encoderJsonConfig   Encoder 的 config.json 内容
     * @param decoderEngineBuffer Decoder 引擎二进制数据
     * @param decoderJsonConfig   Decoder 的 config.json 内容
     */
    public static Executor loadFromBuffers(
            byte[] encoderEngineBuffer, String encoderJsonConfig,
            byte[] decoderEngineBuffer, String decoderJsonConfig,
            ExecutorConfig config) {
        return new Executor(
                new BytePointer(encoderEngineBuffer),
                new BytePointer(encoderJsonConfig),
                new BytePointer(decoderEngineBuffer),
                new BytePointer(decoderJsonConfig),
                TRTLLM.ModelType.kENCODER_DECODER, config
        );
    }

    // ==========================================
    // 3. 从 AOT 预编译文件加载
    // ==========================================

    /**
     * 从 AOT (Ahead-of-Time) 编译的引擎目录加载
     *
     * AOT 在 TRT-LLM 中就是标准的预编译引擎格式：
     * 使用 trtllm-build 将模型编译为 .engine 文件，
     * 然后部署时直接加载，无需重新编译。
     *
     * 这是生产环境的推荐方式。
     *
     * @param aotEngineDir AOT 引擎目录
     * @param config       Executor 配置
     */
    public static Executor loadFromAOT(String aotEngineDir, ExecutorConfig config) {
        return loadDecoderOnly(aotEngineDir, config);
    }

    // ==========================================
    // 4. 从 Model 对象加载
    // ==========================================

    /**
     * 从预加载的 Model 对象创建 Executor
     *
     * 适用于需要在多个 Executor 之间共享模型的场景。
     */
    public static Executor loadFromModel(Model model, ExecutorConfig config) {
        return new Executor(model, config);
    }

    // ==========================================
    // 验证工具
    // ==========================================

    /**
     * 验证引擎目录是否存在且包含必要文件
     */
    public static void validateEngineDir(String engineDir) {
        Path dir = Paths.get(engineDir);
        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Engine directory does not exist: " + engineDir);
        }
        // 检查是否包含 config.json
        if (!Files.exists(dir.resolve("config.json"))) {
            throw new IllegalArgumentException(
                    "Missing config.json in engine directory: " + engineDir +
                            "\nPlease ensure the engine was built with trtllm-build");
        }
    }

    /**
     * 检查目录下有多少个 rank engine 文件
     */
    public static int countRankFiles(String engineDir) {
        Path dir = Paths.get(engineDir);
        try {
            return (int) Files.list(dir)
                    .filter(p -> p.getFileName().toString().matches("rank\\d+\\.engine"))
                    .count();
        } catch (IOException e) {
            return 0;
        }
    }
}

