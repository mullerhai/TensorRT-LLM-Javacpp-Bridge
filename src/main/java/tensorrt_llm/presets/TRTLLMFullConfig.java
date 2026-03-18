package tensorrt_llm.presets;

import org.bytedeco.javacpp.annotation.Platform;
import org.bytedeco.javacpp.annotation.Properties;
import org.bytedeco.javacpp.tools.Info;
import org.bytedeco.javacpp.tools.InfoMap;
import org.bytedeco.javacpp.tools.InfoMapper;

@Properties(
        value = @Platform(
                includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include"},
                include = {
                        // --- 严格按你提供的树状图列出核心入口 ---
                        "tensorrt_llm/common/config.h",
//                        "tensorrt_llm/common/dataType.h",
                        "tensorrt_llm/common/logger.h",
                        "tensorrt_llm/common/tllmException.h",
                        "tensorrt_llm/executor/executor.h",
                        "tensorrt_llm/executor/types.h",
                        "tensorrt_llm/runtime/iBuffer.h",
                        "tensorrt_llm/runtime/iTensor.h",
                        "tensorrt_llm/runtime/bufferManager.h",
                        "tensorrt_llm/runtime/samplingConfig.h",
                        "tensorrt_llm/runtime/worldConfig.h",
//                        "tensorrt_llm/batch_manager/batchManager.h",
                        "tensorrt_llm/batch_manager/llmRequest.h",
//                        "tensorrt_llm/kernels/decodingCommon.h",
//                        "tensorrt_llm/layers/defaultDecodingParams.h"
                }
        ),
        target = "org.bytedeco.tensorrt_llm",
        global = "org.bytedeco.tensorrt_llm.global.TRTLLM"
)
public class TRTLLMFullConfig implements InfoMapper {

    @Override
    public void map(InfoMap infoMap) {
        // --- 1. 精准爆破 dataType.h 的错误 ---
        // 报错是因为 Parser 处理不了 switch-case 逻辑，我们直接 skip 函数体
        infoMap.put(new Info(
                "tensorrt_llm::common::getDTypeSize",
                "tensorrt_llm::common::getDTypeSizeInBits",
                "tensorrt_llm::common::getDtypeString"
        ).skip());

        // --- 2. 宏与语法补丁 ---
        infoMap.put(new Info("__device__", "__host__", "__forceinline__").cppText("#define __device__\n#define __host__\n#define __forceinline__"));
        infoMap.put(new Info("TLLM_THROW", "TLLM_CHECK", "TLLM_CHECK_WITH_INFO", "[[fallthrough]]", "[[maybe_unused]]").skip());
        infoMap.put(new Info("tensorrt_llm::runtime::IBuffer").upcast()); // 消除虚继承警告

        // --- 3. 严格分包 (Namespace -> Java Package) ---
        infoMap.put(new Info("tensorrt_llm::executor").javaNames("org.bytedeco.tensorrt_llm.executor"));
        infoMap.put(new Info("tensorrt_llm::runtime").javaNames("org.bytedeco.tensorrt_llm.runtime"));

        infoMap.put(new Info("tensorrt_llm::common").javaNames("org.bytedeco.tensorrt_llm.common"));
        infoMap.put(new Info("tensorrt_llm::batch_manager").javaNames("org.bytedeco.tensorrt_llm.batch_manager"));

        // --- 4. 容器与智能指针补丁 (全量的关键) ---
        infoMap.put(new Info("std::shared_ptr", "std::unique_ptr", "std::optional").skip());
        infoMap.put(new Info("std::vector<int64_t>", "std::vector<long long>").pointerTypes("LongPointer"));
        infoMap.put(new Info("std::vector<int32_t>", "std::vector<int>").pointerTypes("IntPointer"));
        infoMap.put(new Info("std::vector<float>").pointerTypes("FloatPointer"));
        infoMap.put(new Info("std::string").pointerTypes("BytePointer").cast());

        // --- 5. 拍平继承链 ---
        infoMap.put(new Info("tensorrt_llm::runtime::ITensor").flatten());

        // --- 6. 屏蔽外部依赖 (Mac 缺少的头文件) ---
        infoMap.put(new Info("nvinfer1").skip());
        infoMap.put(new Info("NvInferRuntime.h", "NvInfer.h").skip());
        infoMap.put(new Info("cudaStream_t", "cudaEvent_t").cppTypes("void*"));
    }
}

//package tensorrt_llm.presets;
//
//import org.bytedeco.javacpp.annotation.Platform;
//import org.bytedeco.javacpp.annotation.Properties;
//import org.bytedeco.javacpp.tools.Info;
//import org.bytedeco.javacpp.tools.InfoMap;
//import org.bytedeco.javacpp.tools.InfoMapper;
//
//@Properties(
//        value = @Platform(
//                includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include"},
//                include = {
//                        "tensorrt_llm/common/config.h",
////                        "tensorrt_llm/common/dataType.h",
//                        "tensorrt_llm/common/logger.h",
//                        "tensorrt_llm/executor/executor.h",
//                        "tensorrt_llm/runtime/iBuffer.h",
//                        "tensorrt_llm/runtime/iTensor.h",
//                        "tensorrt_llm/runtime/tllmRuntime.h",
//                        "tensorrt_llm/batch_manager/batchManager.h",
//                        "tensorrt_llm/batch_manager/llmRequest.h"
//                }
//        ),
//        target = "org.bytedeco.tensorrt_llm",
//        global = "org.bytedeco.tensorrt_llm.global.TRTLLM"
//)
//public class TRTLLMFullConfig implements InfoMapper {
//
//    @Override
//    public void map(InfoMap infoMap) {
//        // --- 1. 核心修复：跳过导致解析崩溃的函数体 ---
//        // JavaCPP 主要是为了映射接口，不需要这些静态 helper 函数的逻辑
//        infoMap.put(new Info(
//                "tensorrt_llm::common::getDTypeSize",
//                "tensorrt_llm::common::getDTypeSizeInBits",
//                "tensorrt_llm::common::getDtypeString"
//        ).skip());
//
//        // --- 2. 宏定义补丁 ---
//        infoMap.put(new Info("__device__", "__host__", "__forceinline__").cppText("#define __device__\n#define __host__\n#define __forceinline__"));
//        // 屏蔽所有可能干扰解析的宏
//        infoMap.put(new Info("TLLM_THROW", "TLLM_CHECK", "TLLM_CHECK_WITH_INFO", "[[fallthrough]]", "[[maybe_unused]]").skip());
//
//        // --- 3. 命名空间分包 ---
//        infoMap.put(new Info("tensorrt_llm::executor").javaNames("org.bytedeco.tensorrt_llm.executor"));
//        infoMap.put(new Info("tensorrt_llm::runtime").javaNames("org.bytedeco.tensorrt_llm.runtime"));
//        infoMap.put(new Info("tensorrt_llm::common").javaNames("org.bytedeco.tensorrt_llm.common"));
//        infoMap.put(new Info("tensorrt_llm::batch_manager").javaNames("org.bytedeco.tensorrt_llm.batch_manager"));
//
//        // --- 4. 指针与容器补丁 ---
//        infoMap.put(new Info("std::shared_ptr", "std::unique_ptr", "std::optional").skip());
//        infoMap.put(new Info("std::vector<int64_t>", "std::vector<long long>").pointerTypes("LongPointer"));
//        infoMap.put(new Info("std::vector<int32_t>", "std::vector<int>").pointerTypes("IntPointer"));
//        infoMap.put(new Info("std::vector<float>").pointerTypes("FloatPointer"));
//        infoMap.put(new Info("std::string").pointerTypes("BytePointer").cast());
//
//        // --- 5. 屏蔽外部头文件引用 (NvInferRuntime.h 是 Mac 上没有的) ---
//        infoMap.put(new Info("nvinfer1::DataType").cast().valueTypes("int"));
//        infoMap.put(new Info("NvInferRuntime.h", "NvInfer.h").skip());
//    }
//}

//package tensorrt_llm.presets;
//
//import org.bytedeco.javacpp.annotation.Platform;
//import org.bytedeco.javacpp.annotation.Properties;
//import org.bytedeco.javacpp.tools.Info;
//import org.bytedeco.javacpp.tools.InfoMap;
//import org.bytedeco.javacpp.tools.InfoMapper;
//
//@Properties(
//        value = @Platform(
//                includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include"},
//                include = {
//                        "tensorrt_llm/common/config.h",
////                        "tensorrt_llm/common/dataType.h", // 报错就在这里
//                        "tensorrt_llm/common/logger.h",
//                        "tensorrt_llm/executor/executor.h",
//                        "tensorrt_llm/runtime/iBuffer.h",
//                        "tensorrt_llm/runtime/iTensor.h",
//                        "tensorrt_llm/runtime/tllmRuntime.h",
//                        "tensorrt_llm/batch_manager/batchManager.h",
//                        "tensorrt_llm/batch_manager/llmRequest.h"
//                }
//        ),
//        target = "org.bytedeco.tensorrt_llm",
//        global = "org.bytedeco.tensorrt_llm.global.TRTLLM"
//)
//public class TRTLLMFullConfig implements InfoMapper {
//
//    @Override
//    public void map(InfoMap infoMap) {
//        // --- 1. 核心补丁：处理 dataType.h 中的 Unexpected token '2' ---
//        // 我们直接定位报错的头文件，并把里面可能导致解析失败的行“伪造”成 Parser 能理解的简单常量
//        // 同时也把 C++20 的 constexpr 替换为 const 降低解析难度
//        infoMap.put(new Info("constexpr").cppText("const"));
//        infoMap.put(new Info("uint8_t", "int8_t", "uint16_t", "int16_t", "uint32_t", "int32_t", "uint64_t", "int64_t").cast());
//
//        // --- 2. 宏定义补丁 ---
//        infoMap.put(new Info("__device__", "__host__", "__forceinline__").cppText("#define __device__\n#define __host__\n#define __forceinline__"));
//        infoMap.put(new Info("TLLM_THROW", "TLLM_CHECK", "TLLM_CHECK_WITH_INFO", "TLLM_LOG_DEBUG", "TLLM_LOG_INFO").skip());
//
//        // --- 3. 命名空间分包 (全量 Package 映射) ---
//        infoMap.put(new Info("tensorrt_llm::executor").javaNames("org.bytedeco.tensorrt_llm.executor"));
//        infoMap.put(new Info("tensorrt_llm::runtime").javaNames("org.bytedeco.tensorrt_llm.runtime"));
//        infoMap.put(new Info("tensorrt_llm::common").javaNames("org.bytedeco.tensorrt_llm.common"));
//        infoMap.put(new Info("tensorrt_llm::batch_manager").javaNames("org.bytedeco.tensorrt_llm.batch_manager"));
//
//        // --- 4. 关键类型映射 (解决 std::shared_ptr 导致的 API 缺失) ---
//        infoMap.put(new Info("std::shared_ptr", "std::unique_ptr", "std::optional").skip());
//        infoMap.put(new Info("std::vector<int64_t>", "std::vector<long long>").pointerTypes("LongPointer"));
//        infoMap.put(new Info("std::vector<int32_t>", "std::vector<int>").pointerTypes("IntPointer"));
//        infoMap.put(new Info("std::vector<float>").pointerTypes("FloatPointer"));
//        infoMap.put(new Info("std::string").pointerTypes("BytePointer").cast());
//
//        // --- 5. 拍平核心类 ---
//        infoMap.put(new Info("tensorrt_llm::runtime::ITensor").flatten());
//        infoMap.put(new Info("tensorrt_llm::runtime::IBuffer").flatten());
//
//        // --- 6. 屏蔽外部依赖 ---
//        infoMap.put(new Info("nvinfer1").skip());
//        infoMap.put(new Info("cudaStream_t", "cudaEvent_t", "cudaDeviceProp").cppTypes("void*"));
//    }
//




//    @Override
//    public void map(InfoMap infoMap) {
//        // --- 1. 强力宏补丁：解决 Unexpected token '2' 等解析错误 ---
//        infoMap.put(new Info("__device__", "__host__", "__forceinline__").cppText("#define __device__\n#define __host__\n#define __forceinline__"));
//
//        // 如果 dataType.h 38行左右有 constexpr 或者特定的 C++20 语法，我们直接通过定义宏来“欺骗”Parser
//        infoMap.put(new Info("TLLM_CUDA_CHECK", "TLLM_THROW", "TLLM_CHECK").skip());
//
//        // --- 2. 针对 dataType.h 的精准打击 ---
//        // 很多时候是解析器不认识 C++20 的字面量，我们把 dataType 相关的类名占个位，防止它去深挖
//        infoMap.put(new Info("tensorrt_llm::common::DataType").cast());
//
//        // 如果还是报错，直接把报错的那一部分内容用空文本替换（根据你的报错行数 38 行尝试）
//        // 这里我们把一些复杂的字面量或者 constexpr 屏蔽
//        infoMap.put(new Info("constexpr").cppText("const"));
//
//        // --- 3. 命名空间分包 (保持之前的工业级规范) ---
//        infoMap.put(new Info("tensorrt_llm::executor").javaNames("org.bytedeco.tensorrt_llm.executor"));
//        infoMap.put(new Info("tensorrt_llm::runtime").javaNames("org.bytedeco.tensorrt_llm.runtime"));
//        infoMap.put(new Info("tensorrt_llm::common").javaNames("org.bytedeco.tensorrt_llm.common"));
//        infoMap.put(new Info("tensorrt_llm::batch_manager").javaNames("org.bytedeco.tensorrt_llm.batch_manager"));
//
//        // --- 4. 核心类型映射 ---
//        infoMap.put(new Info("std::shared_ptr", "std::unique_ptr", "std::optional").skip());
//        infoMap.put(new Info("std::vector<int64_t>", "std::vector<long long>").pointerTypes("LongPointer"));
//        infoMap.put(new Info("std::vector<int32_t>", "std::vector<int>").pointerTypes("IntPointer"));
//        infoMap.put(new Info("std::string").pointerTypes("BytePointer").cast());
//
//        // --- 5. 继承链拍平 ---
//        infoMap.put(new Info("tensorrt_llm::runtime::ITensor").flatten());
//        infoMap.put(new Info("tensorrt_llm::runtime::IBuffer").flatten());
//    }
//}

//package tensorrt_llm.presets;
//
//import org.bytedeco.javacpp.annotation.Platform;
//import org.bytedeco.javacpp.annotation.Properties;
//import org.bytedeco.javacpp.tools.Info;
//import org.bytedeco.javacpp.tools.InfoMap;
//import org.bytedeco.javacpp.tools.InfoMapper;
//
//@Properties(
//        value = @Platform(
//                includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include"},
//                include = {
//                        // --- Common 模块 ---
////                        "tensorrt_llm/common/config.h",
////                        "tensorrt_llm/common/dataType.h",
////                        "tensorrt_llm/common/logger.h",
////                        "tensorrt_llm/common/tllmException.h",
////                        "tensorrt_llm/common/cudaUtils.h",
////                        "tensorrt_llm/common/algorithm.h",
////                        "tensorrt_llm/common/quantization.h",
//
//                        // --- Executor 模块 (入口) ---
//                        "tensorrt_llm/executor/executor.h",
//                        "tensorrt_llm/executor/types.h",
//                        "tensorrt_llm/executor/tensor.h",
//                        "tensorrt_llm/executor/serialization.h",
//
//                        // --- Runtime 模块 ---
//                        "tensorrt_llm/runtime/iBuffer.h",
//                        "tensorrt_llm/runtime/iTensor.h",
//                        "tensorrt_llm/runtime/bufferManager.h",
//                        "tensorrt_llm/runtime/tllmRuntime.h",
//                        "tensorrt_llm/runtime/samplingConfig.h",
//                        "tensorrt_llm/runtime/worldConfig.h",
//                        "tensorrt_llm/runtime/modelConfig.h",
//                        "tensorrt_llm/runtime/gptDecoderBatched.h",
//                        "tensorrt_llm/runtime/cudaStream.h",
//                        "tensorrt_llm/runtime/cudaEvent.h",
//
//                        // --- Batch Manager 模块 (全量导入) ---
//                        "tensorrt_llm/batch_manager/batchManager.h",
//                        "tensorrt_llm/batch_manager/llmRequest.h",
//                        "tensorrt_llm/batch_manager/kvCacheManager.h",
//                        "tensorrt_llm/batch_manager/capacityScheduler.h",
//                        "tensorrt_llm/batch_manager/decoderBuffers.h",
//                        "tensorrt_llm/batch_manager/kvCacheType.h",
//                        "tensorrt_llm/batch_manager/microBatchScheduler.h",
//                        "tensorrt_llm/batch_manager/peftCacheManager.h",
//
//                        // --- Kernels & Layers & Plugins ---
//                        "tensorrt_llm/kernels/decodingCommon.h",
//                        "tensorrt_llm/layers/defaultDecodingParams.h",
//                        "tensorrt_llm/plugins/api/tllmPlugin.h"
//                }
//        ),
//        target = "org.bytedeco.tensorrt_llm",
//        global = "org.bytedeco.tensorrt_llm.global.TRTLLM"
//)
//public class TRTLLMFullConfig implements InfoMapper {
//
//    @Override
//    public void map(InfoMap infoMap) {
//        // --- 1. 宏与符号预处理 ---
//        infoMap.put(new Info("__device__", "__host__", "__forceinline__").cppText("#define __device__\n#define __host__\n#define __forceinline__"));
//        // 仅屏蔽导致解析中断的宏，保留其他逻辑
//        infoMap.put(new Info("TLLM_THROW", "TLLM_CHECK", "TLLM_CHECK_WITH_INFO").skip());
//
////        infoMap.put(new Info("tensorrt_llm::common::DataType").cast());
//        // --- 2. 严格分包映射 (按 Namespace 分 Package) ---
//        infoMap.put(new Info("tensorrt_llm::executor").javaNames("org.bytedeco.tensorrt_llm.executor"));
//        infoMap.put(new Info("tensorrt_llm::runtime").javaNames("org.bytedeco.tensorrt_llm.runtime"));
////        infoMap.put(new Info("tensorrt_llm::common").javaNames("org.bytedeco.tensorrt_llm.common"));
//        infoMap.put(new Info("tensorrt_llm::batch_manager").javaNames("org.bytedeco.tensorrt_llm.batch_manager"));
//        infoMap.put(new Info("tensorrt_llm::layers").javaNames("org.bytedeco.tensorrt_llm.layers"));
//        infoMap.put(new Info("tensorrt_llm::plugins").javaNames("org.bytedeco.tensorrt_llm.plugins"));
//
//        // --- 3. 核心容器与指针映射 (解决 API “消失”问题的关键) ---
//        infoMap.put(new Info("std::shared_ptr").skip());
//        infoMap.put(new Info("std::unique_ptr").skip());
//        infoMap.put(new Info("std::optional").skip());
//
//        // 映射所有核心 vector，确保函数重载不会失败
//        infoMap.put(new Info("std::vector<int64_t>", "std::vector<long long>").pointerTypes("LongPointer"));
//        infoMap.put(new Info("std::vector<int32_t>", "std::vector<int>").pointerTypes("IntPointer"));
//        infoMap.put(new Info("std::vector<float>").pointerTypes("FloatPointer"));
//        infoMap.put(new Info("std::vector<double>").pointerTypes("DoublePointer"));
//        infoMap.put(new Info("std::string").pointerTypes("BytePointer").cast());
//
//        // --- 4. 拍平核心继承链 (爆炸式增加代码量的秘诀) ---
//        infoMap.put(new Info("tensorrt_llm::runtime::ITensor").flatten());
//        infoMap.put(new Info("tensorrt_llm::runtime::IBuffer").flatten());
//        infoMap.put(new Info("tensorrt_llm::executor::Executor").flatten());
//
//        // --- 5. 外部依赖处理 ---
//        infoMap.put(new Info("nvinfer1").skip());
//        infoMap.put(new Info("cudaStream_t", "cudaEvent_t", "cudaDeviceProp", "ncclComm_t").cppTypes("void*"));
//    }
//}

//package tensorrt_llm.presets;
//
//import org.bytedeco.javacpp.annotation.Platform;
//import org.bytedeco.javacpp.annotation.Properties;
//import org.bytedeco.javacpp.tools.Info;
//import org.bytedeco.javacpp.tools.InfoMap;
//import org.bytedeco.javacpp.tools.InfoMapper;
//
//@Properties(
//        value = @Platform(
//                // includepath 必须指向 include 这一层
//                includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include"},
//                include = {
//                        // --- 核心模块全覆盖 ---
//                        "tensorrt_llm/common/common.h",
//                        "tensorrt_llm/common/logger.h",
//                        "tensorrt_llm/common/dataType.h",
//                        "tensorrt_llm/executor/executor.h",
//                        "tensorrt_llm/executor/types.h",
//                        "tensorrt_llm/runtime/iBuffer.h",
//                        "tensorrt_llm/runtime/iTensor.h",
//                        "tensorrt_llm/runtime/bufferManager.h",
//                        "tensorrt_llm/runtime/tllmRuntime.h",
//                        "tensorrt_llm/runtime/samplingConfig.h",
//                        "tensorrt_llm/runtime/worldConfig.h",
//                        "tensorrt_llm/runtime/modelConfig.h",
//                        "tensorrt_llm/batch_manager/batchManager.h",
//                        "tensorrt_llm/batch_manager/llmRequest.h",
//                        "tensorrt_llm/batch_manager/kvCacheManager.h"
//                }
//        ),
//        target = "org.bytedeco.tensorrt_llm",
//        global = "org.bytedeco.tensorrt_llm.global.TRTLLM"
//)
//public class TRTLLMFullConfig implements InfoMapper {
//
//    @Override
//    public void map(InfoMap infoMap) {
//        // --- 1. 宏定义伪造：绕过 CUDA/Linux 特有语法 ---
//        infoMap.put(new Info("__device__", "__host__", "__forceinline__").cppText("#define __device__\n#define __host__\n#define __forceinline__"));
//        infoMap.put(new Info("TLLM_THROW", "TLLM_CHECK", "TLLM_LOG_DEBUG", "TLLM_LOG_INFO").skip());
//
//        // --- 2. 核心：分包映射 (Namespace -> Package) ---
//        // 这样生成的代码会按文件夹归类，不再乱成一团
//        infoMap.put(new Info("tensorrt_llm::executor").javaNames("org.bytedeco.tensorrt_llm.executor"));
//        infoMap.put(new Info("tensorrt_llm::runtime").javaNames("org.bytedeco.tensorrt_llm.runtime"));
//        infoMap.put(new Info("tensorrt_llm::common").javaNames("org.bytedeco.tensorrt_llm.common"));
//        infoMap.put(new Info("tensorrt_llm::batch_manager").javaNames("org.bytedeco.tensorrt_llm.batch_manager"));
//
//        // --- 3. 屏蔽无法在 Mac 解析的外部依赖 ---
//        infoMap.put(new Info("nvinfer1").skip());
//        infoMap.put(new Info("nvToolsExt.h").skip());
//        infoMap.put(new Info("cudaStream_t", "cudaEvent_t", "cudaDeviceProp").cppTypes("void*"));
//
//        // --- 4. 容器与高级类型补丁 ---
//        // TRT-LLM 极度依赖智能指针，必须处理，否则很多方法会消失
//        infoMap.put(new Info("std::shared_ptr").skip());
//        infoMap.put(new Info("std::unique_ptr").skip());
//        infoMap.put(new Info("std::optional").skip());
//
//        // 映射 std::vector 家族
//        infoMap.put(new Info("std::vector<int64_t>", "std::vector<long long>").pointerTypes("LongPointer"));
//        infoMap.put(new Info("std::vector<int32_t>", "std::vector<int>").pointerTypes("IntPointer"));
//        infoMap.put(new Info("std::vector<float>").pointerTypes("FloatPointer"));
//        infoMap.put(new Info("std::string").pointerTypes("BytePointer").cast());
//
//        // --- 5. 拍平继承链 (Flattening) ---
//        // 这样 ITensor 会继承 IBuffer 的所有方法
//        infoMap.put(new Info("tensorrt_llm::runtime::ITensor").flatten());
//        infoMap.put(new Info("tensorrt_llm::runtime::IBuffer").flatten());
//    }
//}
//
////package tensorrt_llm.presets;
////
////import org.bytedeco.javacpp.annotation.Platform;
////import org.bytedeco.javacpp.annotation.Properties;
////import org.bytedeco.javacpp.tools.Info;
////import org.bytedeco.javacpp.tools.InfoMap;
////import org.bytedeco.javacpp.tools.InfoMapper;
////
/////**
//// * 核心：通过 target 指定基础包，通过 InfoMap 映射子包
//// */
////@Properties(
////        value = @Platform(
//////                value = "linux-x86_64",
////                includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include"},
////                include = {
////                        "tensorrt_llm/common/common.h",
////                        "tensorrt_llm/common/logger.h",
////                        "tensorrt_llm/executor/executor.h",
////                        "tensorrt_llm/runtime/iBuffer.h",
////                        "tensorrt_llm/runtime/iTensor.h",
////                        "tensorrt_llm/runtime/tllmRuntime.h",
////                        "tensorrt_llm/runtime/worldConfig.h",
////                        "tensorrt_llm/batch_manager/batchManager.h"
////                }
////        ),
////        target = "org.bytedeco.tensorrt_llm", // 基础包
////        global = "org.bytedeco.tensorrt_llm.global.TRTLLM" // Global 隔离包
////)
////public class TRTLLMFullConfig implements InfoMapper {
////    @Override
////    public void map(InfoMap infoMap) {
////        // --- 1. 宏定义补丁 (防止 Mac 解析中断) ---
////        infoMap.put(new Info("__device__", "__host__", "__forceinline__").cppText("#define __device__\n#define __host__\n#define __forceinline__"));
////        infoMap.put(new Info("TLLM_THROW", "TLLM_CHECK", "TLLM_LOG_DEBUG", "TLLM_LOG_INFO").skip());
////
////        // --- 2. 核心：分包映射逻辑 ---
////        // JavaCPP 会根据 javaNames 里的包名路径自动创建文件夹
////        infoMap.put(new Info("tensorrt_llm::executor").javaNames("org.bytedeco.tensorrt_llm.executor"));
////        infoMap.put(new Info("tensorrt_llm::runtime").javaNames("org.bytedeco.tensorrt_llm.runtime"));
////        infoMap.put(new Info("tensorrt_llm::common").javaNames("org.bytedeco.tensorrt_llm.common"));
////        infoMap.put(new Info("tensorrt_llm::batch_manager").javaNames("org.bytedeco.tensorrt_llm.batch_manager"));
////
////        // --- 3. 强制递归与类型扩展 ---
////        // 映射 std::shared_ptr 为虚指针，否则使用该返回值的函数会消失
////        infoMap.put(new Info("std::shared_ptr").skip());
////        infoMap.put(new Info("std::unique_ptr").skip());
////        infoMap.put(new Info("std::optional").skip());
////
////        // --- 4. 显存管理与数据结构 (全量核心) ---
////        infoMap.put(new Info("tensorrt_llm::runtime::ITensor").flatten());
////        infoMap.put(new Info("tensorrt_llm::runtime::IBuffer").flatten());
////
////        // 映射所有常见的 std 容器，否则大量方法会因为无法映射参数而失效
////        infoMap.put(new Info("std::vector<int64_t>").pointerTypes("LongPointer"));
////        infoMap.put(new Info("std::vector<int32_t>").pointerTypes("IntPointer"));
////        infoMap.put(new Info("std::vector<float>").pointerTypes("FloatPointer"));
////        infoMap.put(new Info("std::string").pointerTypes("BytePointer").cast());
////
////        // --- 5. 屏蔽外部不可解析依赖 ---
////        infoMap.put(new Info("nvinfer1").skip());
////        infoMap.put(new Info("cudaStream_t", "cudaEvent_t").cppTypes("void*"));
////    }
////}