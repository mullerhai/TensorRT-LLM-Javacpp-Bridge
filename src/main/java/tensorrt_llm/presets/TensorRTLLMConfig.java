package tensorrt_llm.presets;

import org.bytedeco.javacpp.annotation.Platform;
import org.bytedeco.javacpp.annotation.Properties;
import org.bytedeco.javacpp.tools.Info;
import org.bytedeco.javacpp.tools.InfoMap;
import org.bytedeco.javacpp.tools.InfoMapper;

@Properties(
        value = {
                @Platform(
                        value = "linux-x86_64",
                        includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include"},
                        // --- 核心：列出所有顶层模块的入口头文件 ---
                        include = {
                                "tensorrt_llm/common/common.h",
                                "tensorrt_llm/common/logger.h",
                                "tensorrt_llm/common/stringUtils.h",
                                "tensorrt_llm/executor/executor.h",
                                "tensorrt_llm/runtime/iBuffer.h",
                                "tensorrt_llm/runtime/iTensor.h",
                                "tensorrt_llm/runtime/tllmRuntime.h",
                                "tensorrt_llm/runtime/samplingConfig.h",
                                "tensorrt_llm/runtime/generationInput.h",
                                "tensorrt_llm/runtime/generationOutput.h",
                                "tensorrt_llm/runtime/modelConfig.h",
                                "tensorrt_llm/runtime/worldConfig.h",
                                "tensorrt_llm/batch_manager/batchManager.h" // 批处理核心
                        }
                )
        },
        target = "org.bytedeco.tensorrt_llm",
        global = "TRTLLMCore"
)
public class TensorRTLLMConfig implements InfoMapper {

    @Override
    public void map(InfoMap infoMap) {
        // --- 1. 基础环境补丁 (针对 Mac 解析优化) ---
        infoMap.put(new Info("__device__", "__host__", "__forceinline__", "TLLM_CUDA_CHECK").cppText("#define __device__\n#define __host__\n#define __forceinline__\n#define TLLM_CUDA_CHECK(x) (x)"));

        // 屏蔽所有复杂的宏函数，这些是解析中断的元凶
        infoMap.put(new Info("TLLM_THROW", "TLLM_CHECK", "TLLM_CHECK_WITH_INFO", "TLLM_LOG_DEBUG", "TLLM_LOG_TRACE", "TLLM_LOG_INFO", "TLLM_LOG_WARNING", "TLLM_LOG_ERROR").skip());

        // --- 2. 泛型与智能指针全量补丁 ---
        // TRT-LLM 几乎所有的类都包装在 shared_ptr 中。如果不映射，方法会消失。
        // 我们告诉 Parser 遇到 shared_ptr<T> 统一当做 T 的指针处理
        infoMap.put(new Info("std::shared_ptr").pointerTypes("Pointer"));
        infoMap.put(new Info("std::unique_ptr").pointerTypes("Pointer"));
        infoMap.put(new Info("std::optional").pointerTypes("Pointer"));
        infoMap.put(new Info("std::variant").pointerTypes("Pointer"));

        // --- 3. 命名空间全量展开 ---
        // 确保这些命名空间下的所有类都能被 Parser 看到
        infoMap.put(new Info("tensorrt_llm::executor"));
        infoMap.put(new Info("tensorrt_llm::runtime"));
        infoMap.put(new Info("tensorrt_llm::common"));
        infoMap.put(new Info("tensorrt_llm::batch_manager"));

        // --- 4. 容器类全量映射 ---
        infoMap.put(new Info("std::vector<int64_t>", "std::vector<long long>").pointerTypes("LongPointer"));
        infoMap.put(new Info("std::vector<int32_t>", "std::vector<int>").pointerTypes("IntPointer"));
        infoMap.put(new Info("std::vector<float>").pointerTypes("FloatPointer"));
        infoMap.put(new Info("std::vector<std::string>").pointerTypes("StringPointer")); // 假设有对应的 StringPointer
        infoMap.put(new Info("std::string").pointerTypes("BytePointer").cast());

        // --- 5. 外部依赖屏蔽 (解决 Mac 找不到库的问题) ---
        infoMap.put(new Info("nvinfer1").pointerTypes("Pointer"));
        infoMap.put(new Info("cudaStream_t", "cudaEvent_t", "cudaDeviceProp").cppTypes("void*"));
        infoMap.put(new Info("ncclComm_t").cppTypes("void*")); // 屏蔽分布式通信依赖
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
//        value = {
//                @Platform(
//                        value = "linux-x86_64",
//                        includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include"},
//                        // 扩充头文件，涵盖 Runtime 和常用定义
//                        include = {
//                                "tensorrt_llm/common/common.h",
//                                "tensorrt_llm/executor/executor.h",
//                                "tensorrt_llm/runtime/iBuffer.h",
//                                "tensorrt_llm/runtime/iTensor.h"
//                        }
//                )
//        },
//        target = "org.bytedeco.tensorrt_llm",
//        global = "TRTLLMCore"
//)
//public class TensorRTLLMConfig implements InfoMapper {
//
//    @Override
//    public void map(InfoMap infoMap) {
//        // --- 1. 宏定义替换 (解决 Mac 解析 CUDA 关键字报错) ---
//        infoMap.put(new Info("__device__").cppText("#define __device__"));
//        infoMap.put(new Info("__host__").cppText("#define __host__"));
//        infoMap.put(new Info("__forceinline__").cppText("#define __forceinline__"));
//
//        // --- 2. 屏蔽掉 Mac 无法处理的依赖和异常 ---
//        infoMap.put(new Info(
//                "TLLM_THROW", "TLLM_CHECK", "TLLM_CHECK_WITH_INFO",
//                "TLLM_LOG_DEBUG", "TLLM_LOG_TRACE", "TLLM_LOG_INFO"
//        ).skip());
//
//        // --- 3. 核心：处理 std::shared_ptr (TRT-LLM 的命脉) ---
//        // 注意：JavaCPP 默认不处理 shared_ptr，如果不映射，返回它的函数会消失
//        // 我们通过告诉它 cppTypes 是指针来“绕过”模板解析
//        infoMap.put(new Info("std::shared_ptr<tensorrt_llm::executor::Executor>").cppTypes("void*"));
//        infoMap.put(new Info("std::unique_ptr").pointerTypes("Pointer"));
//        infoMap.put(new Info("std::optional").pointerTypes("Pointer"));
//
//        // --- 4. 容器与基础类型映射 ---
//        infoMap.put(new Info("std::vector<int64_t>").pointerTypes("LongPointer"));
//        infoMap.put(new Info("std::vector<int32_t>").pointerTypes("IntPointer"));
//        infoMap.put(new Info("std::string").pointerTypes("BytePointer").cast());
//
//        // --- 5. 处理 TRT-LLM 内部复杂的命名空间和类 ---
//        // 显式指定某些类不被 skip（如果 Parser 漏掉了）
//        infoMap.put(new Info("tensorrt_llm::executor::Executor"));
//        infoMap.put(new Info("tensorrt_llm::runtime::ITensor"));
//
//        // --- 6. 屏蔽掉 Mac 找不到的外部库符号 ---
//        infoMap.put(new Info("nvinfer1").pointerTypes("Pointer"));
//        infoMap.put(new Info("cudaStream_t").cppTypes("void*"));
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
//        value = {
//                @Platform(
//                        value = "linux-x86_64",
//                        includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include"},
//                        include = {
//                                "tensorrt_llm/common/common.h",
//                                "tensorrt_llm/executor/executor.h",
//                                "tensorrt_llm/runtime/tllmRuntime.h",
//                                "tensorrt_llm/runtime/iBuffer.h",
//                                "tensorrt_llm/runtime/iTensor.h",
//                                "tensorrt_llm/runtime/samplingConfig.h"
//                        }
//                )
//        },
//        target = "org.bytedeco.tensorrt_llm",
//        global = "TRTLLMCore"
//)
//public class TensorRTLLMConfig implements InfoMapper {
//
//    @Override
//    public void map(InfoMap infoMap) {
//        // --- 1. 宏定义补丁 ---
//        infoMap.put(new Info("__device__", "__host__", "__forceinline__").cppText("#define __device__\n#define __host__\n#define __forceinline__"));
//        infoMap.put(new Info("TLLM_THROW", "TLLM_CHECK", "TLLM_LOG_DEBUG", "TLLM_LOG_TRACE").skip());
//
//        // --- 2. 处理 std::optional (这是转译中最容易漏掉的) ---
//        // Java 不支持 std::optional，通常需要映射为对象指针或直接 skip 掉包装层
//        infoMap.put(new Info("std::optional").pointerTypes("Pointer"));
//
//        // --- 3. 基础类型与容器映射 ---
//        infoMap.put(new Info("std::vector<int64_t>").pointerTypes("LongPointer"));
//        infoMap.put(new Info("std::vector<int32_t>").pointerTypes("IntPointer"));
//        infoMap.put(new Info("std::string").pointerTypes("BytePointer").cast());
//
//        // --- 4. 运行时核心类声明 ---
//        infoMap.put(new Info("tensorrt_llm::runtime::IBuffer").concepts("class"));
//        infoMap.put(new Info("tensorrt_llm::runtime::ITensor").concepts("class"));
//
//        // --- 5. 屏蔽掉过于底层或依赖外部 CUDA 库的符号 ---
//        infoMap.put(new Info("nvinfer1::ILogger").pointerTypes("Pointer"));
//        infoMap.put(new Info("cudaStream_t").valueTypes("Pointer")); // 将 CUDA 流映射为通用指针
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
//        value = {
//                @Platform(
//                        // 注意：这里先不要写 linux-x86_64，让它默认适配你的 macosx-arm64 进行解析
//                        includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include"},
//                        include = {
//                                "tensorrt_llm/executor/executor.h"
//                        }
//                )
//        },
//        target = "org.bytedeco.tensorrt_llm",
//        global = "TRTLLMCore"
//)
//public class TensorRTLLMConfig implements InfoMapper {
//    @Override
//    public void map(InfoMap infoMap) {
//        // 保持之前的 skip 逻辑
//        infoMap.put(new Info("__device__", "__host__", "TLLM_THROW").skip());
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
//        value = {
//                @Platform(
//                        // 目标平台设为 linux-x86_64
//                        value = "linux-x86_64",
//                        includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include"},
//                        include = {
//                                "tensorrt_llm/executor/executor.h"
//                        }
//                )
//        },
//        target = "org.bytedeco.tensorrt_llm",
//        global = "TRTLLMCore"
//)
//public class TensorRTLLMConfig implements InfoMapper {
//
//    @Override
//    public void map(InfoMap infoMap) {
//        // 1. 处理宏定义：利用 cppText 替换掉可能导致 Mac 解析失败的 CUDA 关键字
//        infoMap.put(new Info("__device__").cppText("#define __device__"));
//        infoMap.put(new Info("__host__").cppText("#define __host__"));
//        infoMap.put(new Info("__forceinline__").cppText("#define __forceinline__"));
//
//        // 2. 屏蔽掉一些过于复杂的、且在 Mac 上无法解析的 TensorRT 依赖
//        // 就像 llama 示例中 skip 掉一些 enum 和 api 一样
//        infoMap.put(new Info(
//                "nvinfer1::ILogger",
//                "nvinfer1::IExecutionContext",
//                "nvinfer1::ICudaEngine",
//                "tensorrt_llm::common::TllmException"
//        ).skip());
//
//        // 3. 类型映射：将复杂的 std 类型或内部指针简化
//        // 参考 llama 示例中对 llama_context_t 的处理
//        infoMap.put(new Info("std::vector<int64_t>").pointerTypes("LongPointer"));
//        infoMap.put(new Info("std::shared_ptr").pointerTypes("Pointer"));
//
//        // 4. 如果某些类型 Parser 认不出来，强制指定为指针类型
//        // 参考示例：infoMap.put(new Info("llama_context_t").cppTypes("llama_context*"));
//        infoMap.put(new Info("tensorrt_llm::executor::Executor").cppTypes("void*"));
//
//        // 5. 屏蔽掉报错频率最高的一些宏函数
//        infoMap.put(new Info("TLLM_CHECK", "TLLM_CHECK_WITH_INFO", "TLLM_THROW").skip());
//    }
//}


//package tensorrt_llm.presets;
//
//import org.bytedeco.javacpp.annotation.*;
//import org.bytedeco.javacpp.tools.*;
//
//@Properties(
//        target = "org.bytedeco.tensorrt_llm",
//        global = "org.bytedeco.tensorrt_llm.global.tensorrt_llm",
//        value = @Platform(
//                include = {
//                        "tensorrt_llm/executor/executor.h"
//                }
//        )
//)
//public class TensorRTLLMConfig implements InfoMapper {
//    @Override
//    public void map(InfoMap infoMap) {
//        // 1. 基础宏过滤
//        infoMap.put(new Info("__device__", "__host__", "TLLM_CUDA_CHECK", "TLLM_THROW").skip());
//
//        // 2. 关键类型映射
//        infoMap.put(new Info("tensorrt_llm::executor::Executor").concepts("struct", "class"));
//        infoMap.put(new Info("std::vector<int64_t>").pointerTypes("LongPointer"));
//
//        // 3. 针对 Mac 环境屏蔽掉找不到的 TensorRT 基础类型
//        infoMap.put(new Info("nvinfer1").pointerTypes("Pointer"));
//        infoMap.put(new Info("std::optional").pointerTypes("Pointer"));
//    }
//}

//package tensorrt_llm.presets;
//
//import org.bytedeco.javacpp.annotation.*;
//import org.bytedeco.javacpp.tools.*;
//
//@Properties(
//        target = "org.bytedeco.tensorrt_llm",
//        global = "org.bytedeco.tensorrt_llm.global.tensorrt_llm",
//        value = @Platform(
//                // 先不写 linux-x86_64，让它适配你当前的 Mac
//                include = {
//                        "tensorrt_llm/executor/executor.h",
//                        "tensorrt_llm/common/common.h"
//                }
//        )
//)
//public class TensorRTLLMConfig implements InfoMapper {
//    @Override
//    public void map(InfoMap infoMap) {
//        // JavaCPP 默认可能不认识这些宏，先 skip 掉防止报错
//        infoMap.put(new Info("__device__").pointerTypes("Pointer"));
//        infoMap.put(new Info("__host__").pointerTypes("Pointer"));
//    }
//}
//
////package tensorrt_llm.presets;
////
////import org.bytedeco.javacpp.annotation.*;
////import org.bytedeco.javacpp.tools.*;
////
////@Properties(
////        target = "org.bytedeco.tensorrt_llm", // 这是生成的代码所在的包名
////        value = @Platform(
////                value = "linux-x86_64",
////                include = {"tensorrt_llm/executor/executor.h"}
////        )
////)
////public class TensorRTLLMConfig implements InfoMapper {
////    public void map(InfoMap infoMap) {
////        // 暂时留空也可以，只要有上面的注解就能生成基础代码
////    }
////}