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
                        "tensorrt_llm/common/config.h",
                        "tensorrt_llm/common/logger.h",
                        "tensorrt_llm/common/tllmException.h",
                        "tensorrt_llm/common/assert.h",
                        "tensorrt_llm/executor/executor.h",
                        "tensorrt_llm/executor/types.h",
                        "tensorrt_llm/executor/tensor.h",
                        "tensorrt_llm/executor/serialization.h",
                        "tensorrt_llm/runtime/iBuffer.h",
                        "tensorrt_llm/runtime/iTensor.h",
                        "tensorrt_llm/runtime/bufferManager.h",
                        "tensorrt_llm/runtime/samplingConfig.h",
                        "tensorrt_llm/runtime/worldConfig.h",
                        // batch_manager: full source-driven header set
                        "tensorrt_llm/batch_manager/common.h",
                        "tensorrt_llm/batch_manager/kvCacheType.h",
                        "tensorrt_llm/batch_manager/llmRequest.h",
                        "tensorrt_llm/batch_manager/allocateKvCache.h",
                        "tensorrt_llm/batch_manager/assignReqSeqSlots.h",
                        "tensorrt_llm/batch_manager/blockKey.h",
                        "tensorrt_llm/batch_manager/cacheTransceiver.h",
                        "tensorrt_llm/batch_manager/capacityScheduler.h",
                        "tensorrt_llm/batch_manager/contextProgress.h",
                        "tensorrt_llm/batch_manager/createNewDecoderRequests.h",
                        "tensorrt_llm/batch_manager/decoderBuffers.h",
                        "tensorrt_llm/batch_manager/evictionPolicy.h",
                        "tensorrt_llm/batch_manager/guidedDecoder.h",
                        "tensorrt_llm/batch_manager/handleContextLogits.h",
                        "tensorrt_llm/batch_manager/handleGenerationLogits.h",
                        "tensorrt_llm/batch_manager/kvCacheConnector.h",
                        "tensorrt_llm/batch_manager/kvCacheEventManager.h",
                        "tensorrt_llm/batch_manager/kvCacheManager.h",
                        "tensorrt_llm/batch_manager/kvCacheTransferManager.h",
                        "tensorrt_llm/batch_manager/kvCacheUtils.h",
                        "tensorrt_llm/batch_manager/logitsPostProcessor.h",
                        "tensorrt_llm/batch_manager/makeDecodingBatchInputOutput.h",
                        "tensorrt_llm/batch_manager/medusaBuffers.h",
                        "tensorrt_llm/batch_manager/microBatchScheduler.h",
                        "tensorrt_llm/batch_manager/pauseRequests.h",
                        "tensorrt_llm/batch_manager/peftCacheManager.h",
                        "tensorrt_llm/batch_manager/peftCacheManagerConfig.h",
                        "tensorrt_llm/batch_manager/promptTuningBuffers.h",
                        "tensorrt_llm/batch_manager/radixBlockTree.h",
                        "tensorrt_llm/batch_manager/rnnCacheFormatter.h",
                        "tensorrt_llm/batch_manager/rnnStateManager.h",
                        "tensorrt_llm/batch_manager/runtimeBuffers.h",
                        "tensorrt_llm/batch_manager/sequenceSlotManager.h",
                        "tensorrt_llm/batch_manager/stringSetTrie.h",
                        "tensorrt_llm/batch_manager/templatedTrie.h",
                        "tensorrt_llm/batch_manager/transformerBuffers.h",
                        "tensorrt_llm/batch_manager/updateDecoderBuffers.h"
                }
        ),
        target = "org.bytedeco.tensorrt_llm",
        global = "org.bytedeco.tensorrt_llm.global.TRTLLM"
)
public class TRTLLMFullConfig implements InfoMapper {

    @Override
    public void map(InfoMap infoMap) {

        // ======================================================
        // 1. Config macros - 定义为空，避免生成无效代码
        // ======================================================
        infoMap.put(new Info("TRTLLM_ABI_NAMESPACE", "TRTLLM_ABI_NAMESPACE_BEGIN", "TRTLLM_ABI_NAMESPACE_END",
                "TRTLLM_NAMESPACE_BEGIN", "TRTLLM_NAMESPACE_END", "_v1")
                .cppText("#define TRTLLM_ABI_NAMESPACE\n#define TRTLLM_ABI_NAMESPACE_BEGIN\n" +
                        "#define TRTLLM_ABI_NAMESPACE_END\n#define TRTLLM_NAMESPACE_BEGIN\n#define TRTLLM_NAMESPACE_END\n#define _v1 1\n"));

        // ======================================================
        // 2. CUDA 和编译器属性 - 定义为空
        // ======================================================
        infoMap.put(new Info("__device__", "__host__", "__forceinline__", "__global__", "__constant__", "__shared__",
                "__restrict__", "__restrict", "[[fallthrough]]", "[[maybe_unused]]", "[[nodiscard]]")
                .annotations().cppText("#define __device__\n#define __host__\n#define __forceinline__\n" +
                        "#define __global__\n#define __constant__\n#define __shared__\n" +
                        "#define __restrict__\n#define __restrict\n"));

        // ======================================================
        // 3. 跳过的宏
        // ======================================================
        infoMap.put(new Info("TLLM_THROW", "TLLM_CHECK", "TLLM_CHECK_WITH_INFO",
                "TLLM_LOG_DEBUG", "TLLM_LOG_INFO", "TLLM_LOG_WARNING", "TLLM_LOG_ERROR",
                "TLLM_CUDA_CHECK", "TLLM_NCCL_CHECK", "NEW_TLLM_EXCEPTION", "TLLM_WRAP",
                "TLLM_REQUEST_EXCEPTION", "ENABLE_BF16", "ENABLE_FP8")
                .skip());

        // ======================================================
        // 4. 核心类型别名映射 - 这是最关键的！把 C++ typedef 映射到 Java 类型
        // ======================================================
        // int32_t 类型
        infoMap.put(new Info("tensorrt_llm::executor::SizeType32", "tensorrt_llm::runtime::SizeType32",
                "SizeType32")
                .cast().valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("tensorrt_llm::executor::TokenIdType", "tensorrt_llm::runtime::TokenIdType",
                "TokenIdType")
                .cast().valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("tensorrt_llm::executor::RetentionPriority", "RetentionPriority")
                .cast().valueTypes("int").pointerTypes("IntPointer"));

        // int64_t 类型
        infoMap.put(new Info("tensorrt_llm::executor::SizeType64", "tensorrt_llm::runtime::SizeType64",
                "SizeType64")
                .cast().valueTypes("long").pointerTypes("LongPointer"));

        // uint64_t 类型
        infoMap.put(new Info("tensorrt_llm::executor::IdType", "IdType",
                "tensorrt_llm::executor::RequestIdType", "RequestIdType")
                .cast().valueTypes("long").pointerTypes("LongPointer"));
        infoMap.put(new Info("tensorrt_llm::executor::IterationType", "IterationType",
                "tensorrt_llm::executor::RandomSeedType", "RandomSeedType",
                "tensorrt_llm::runtime::LoraTaskIdType", "LoraTaskIdType",
                "tensorrt_llm::runtime::TokenExtraIdType", "TokenExtraIdType",
                "tensorrt_llm::executor::CacheSaltIDType", "CacheSaltIDType")
                .cast().valueTypes("long").pointerTypes("LongPointer"));

        // float 类型
        infoMap.put(new Info("tensorrt_llm::executor::FloatType", "FloatType",
                "tensorrt_llm::executor::PriorityType", "PriorityType")
                .cast().valueTypes("float").pointerTypes("FloatPointer"));

        // uint32_t 类型
        infoMap.put(new Info("uint32_t").cast().valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("uint64_t").cast().valueTypes("long").pointerTypes("LongPointer"));

        // chrono
        infoMap.put(new Info("tensorrt_llm::executor::MillisecondsType", "MillisecondsType",
                "std::chrono::milliseconds", "std::chrono::time_point", "std::chrono::steady_clock")
                .cast().valueTypes("long").pointerTypes("LongPointer"));

        // ======================================================
        // 5. 指针和智能指针类型映射
        // ======================================================
        infoMap.put(new Info("tensorrt_llm::executor::TensorPtr", "TensorPtr")
                .valueTypes("@SharedPtr Tensor").pointerTypes("Tensor"));
        infoMap.put(new Info("tensorrt_llm::executor::StreamPtr", "StreamPtr",
                "tensorrt_llm::executor::CudaStreamPtr", "CudaStreamPtr",
                "tensorrt_llm::runtime::CudaStream")
                .cast().pointerTypes("Pointer"));

        // IBuffer/ITensor 的 UniquePtr 和 SharedPtr
        infoMap.put(new Info("tensorrt_llm::runtime::IBuffer::UniquePtr",
                "tensorrt_llm::runtime::IBuffer::SharedPtr")
                .pointerTypes("IBuffer"));
        infoMap.put(new Info("tensorrt_llm::runtime::ITensor::UniquePtr",
                "tensorrt_llm::runtime::ITensor::SharedPtr")
                .pointerTypes("ITensor"));

        // std::shared_ptr/unique_ptr/optional 泛型跳过
        infoMap.put(new Info("std::shared_ptr", "std::unique_ptr", "std::weak_ptr").annotations("@SharedPtr"));
        infoMap.put(new Info("std::optional").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("std::nullopt").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("std::variant").pointerTypes(\"Pointer\"));

        // ======================================================
        // 6. STL 容器映射
        // ======================================================
        infoMap.put(new Info("std::string").annotations("@StdString").valueTypes("BytePointer").pointerTypes("BytePointer"));
        infoMap.put(new Info("std::vector<int32_t>", "std::vector<int>",
                "tensorrt_llm::executor::VecTokens", "VecTokens")
                .annotations("@StdVector").pointerTypes("IntPointer"));
        infoMap.put(new Info("std::vector<float>",
                "tensorrt_llm::executor::VecLogProbs", "VecLogProbs")
                .annotations("@StdVector").pointerTypes("FloatPointer"));
        infoMap.put(new Info("std::vector<int64_t>", "std::vector<long long>")
                .annotations("@StdVector").pointerTypes("LongPointer"));
        infoMap.put(new Info("std::vector<uint8_t>", "std::vector<unsigned char>")
                .annotations("@StdVector").pointerTypes("BytePointer"));
        infoMap.put(new Info("std::vector<bool>").annotations("@StdVector").pointerTypes("BooleanPointer"));

        // 复杂容器 - 跳过
        infoMap.put(new Info("std::vector<std::vector", "std::vector<std::string>",
                "std::vector<std::pair", "std::list", "std::deque",
                "tensorrt_llm::executor::BeamTokens", "BeamTokens",
                "tensorrt_llm::executor::VecTokenExtraIds", "VecTokenExtraIds",
                "tensorrt_llm::executor::VecUniqueTokens", "VecUniqueTokens",
                "tensorrt_llm::executor::MedusaChoices", "MedusaChoices",
                "tensorrt_llm::executor::EagleChoices", "EagleChoices")
                .skip());

        // vector<SizeType32> 映射
        infoMap.put(new Info("std::vector<tensorrt_llm::executor::SizeType32>",
                "std::vector<tensorrt_llm::runtime::SizeType32>")
                .annotations("@StdVector").pointerTypes("IntPointer"));
        // vector<TokenIdType>
        infoMap.put(new Info("std::vector<tensorrt_llm::executor::TokenIdType>",
                "std::vector<tensorrt_llm::runtime::TokenIdType>")
                .annotations("@StdVector").pointerTypes("IntPointer"));
        // vector<AdditionalModelOutput> 等复杂 vector
        infoMap.put(new Info("std::vector<tensorrt_llm::executor::AdditionalModelOutput>")
                .skip());
        // vector<Response>
        infoMap.put(new Info("std::vector<tensorrt_llm::executor::Response>")
                .skip());
        infoMap.put(new Info("std::pair", "std::tuple", "std::array",
                "std::map", "std::unordered_map", "std::set", "std::unordered_set",
                "std::queue", "std::priority_queue")
                .skip());
        infoMap.put(new Info("std::function", "std::bind", "std::placeholders",
                "tensorrt_llm::executor::LogitsPostProcessor",
                "tensorrt_llm::executor::LogitsPostProcessorMap",
                "tensorrt_llm::executor::LogitsPostProcessorBatched")
                .skip());
        infoMap.put(new Info("std::ostream", "std::istream", "std::iostream",
                "std::ofstream", "std::ifstream")
                .skip());
        infoMap.put(new Info("std::filesystem::path").pointerTypes("BytePointer"));
        infoMap.put(new Info("tensorrt_llm::executor::KVCacheEventData", "KVCacheEventData").skip());

        // ======================================================
        // 7. 枚举冲突处理 - 重命名冲突的枚举常量
        // ======================================================
        // DataType 和 MemoryType 都有 kUNKNOWN
        infoMap.put(new Info("tensorrt_llm::executor::DataType").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::MemoryType").enumerate());
        // 其他可能冲突的枚举
        infoMap.put(new Info("tensorrt_llm::executor::RequestType").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::ModelType").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::BatchingType").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::CapacitySchedulerPolicy").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::ContextChunkingPolicy").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::CommunicationType").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::CommunicationMode").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::RequestStage").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::FinishReason").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::KvCacheTransferMode").enumerate());
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::CacheType").enumerate());

        // ======================================================
        // 8. DecodingMode 处理 - auto constexpr 返回值问题
        // ======================================================
        // DecodingMode 的 static auto constexpr 工厂方法无法正确生成
        // 跳过整个类，后续可以手写 Java wrapper
        infoMap.put(new Info("tensorrt_llm::executor::DecodingMode").pointerTypes(\"Pointer\"));

        // ======================================================
        // 9. Shape 基类问题 - 跳过基类但保留 Shape 本身
        // ======================================================
        infoMap.put(new Info("tensorrt_llm::common::ArrayView<const tensorrt_llm::executor::detail::DimType64>",
                "tensorrt_llm::common::ArrayView<detail::DimType64 const>",
                "tensorrt_llm::common::ArrayView")
                .pointerTypes("Pointer").base("Pointer"));
        infoMap.put(new Info("std::initializer_list", "std::initializer_list<tensorrt_llm::executor::Shape::DimType64>")
                .skip());
        infoMap.put(new Info("tensorrt_llm::executor::detail::DimType64", "detail::DimType64")
                .cast().valueTypes("long").pointerTypes("LongPointer"));

        // ======================================================
        // 10. LlmRequest 模板基类
        // ======================================================
        infoMap.put(new Info("tensorrt_llm::batch_manager::GenericLlmRequest",
                "tensorrt_llm::batch_manager::GenericLlmRequest<tensorrt_llm::runtime::ITensor::SharedPtr>")
                .skip());

        // ======================================================
        // 11. 外部依赖 (TensorRT / CUDA / NCCL)
        // ======================================================
        infoMap.put(new Info("nvinfer1", "nvinfer1::DataType", "nvinfer1::Dims",
                "NvInferRuntime.h", "NvInfer.h", "NvInferPlugin.h")
                .skip());
        infoMap.put(new Info("cudaStream_t", "cudaEvent_t", "cudaDeviceProp",
                "ncclComm_t", "cublasHandle_t", "cudnnHandle_t")
                .cast().valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("cudaError_t").cast().valueTypes("int"));
        infoMap.put(new Info("cuda_runtime.h", "cuda.h", "nccl.h",
                "cuda_fp16.h", "cuda_bf16.h", "cuda_fp8.h",
                "curand_kernel.h")
                .skip());
        infoMap.put(new Info("half", "__half", "__nv_bfloat16", "__nv_fp8_e4m3")
                .cast().valueTypes("short").pointerTypes("ShortPointer"));

        // ======================================================
        // 12. 跳过无法解析的函数和模板特化
        // ======================================================
        infoMap.put(new Info("tensorrt_llm::common::getDTypeSize",
                "tensorrt_llm::common::getDTypeSizeInBits",
                "tensorrt_llm::common::getDtypeString")
                .skip());
        infoMap.put(new Info("tensorrt_llm::layers::DefaultDecodingParams").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::runtime::CudaEvent").pointerTypes(\"Pointer\"));
        infoMap.put(new Info("tensorrt_llm::kernels").pointerTypes(\"Pointer\"));

        // 模板特化跳过
        infoMap.put(new Info("TypeTraits", "DataTypeTraits", "TRTDataType",
                "MemoryTypeString", "KVCacheEventDiff", "BufferDataType")
                .skip());

        // hash 函数
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::hash32Mix",
                "tensorrt_llm::batch_manager::kv_cache_manager::hash64Mix")
                .skip());

        // operator<< 跳过
        infoMap.put(new Info("operator <<", "operator>>").skip());

        // ======================================================
        // 13. IBuffer/ITensor upcast 处理
        // ======================================================
        infoMap.put(new Info("tensorrt_llm::runtime::IBuffer").upcast());

        // ======================================================
        // 14. BufferManager 中依赖 CUDA 的内部类型
        // ======================================================
        infoMap.put(new Info("tensorrt_llm::runtime::CudaMemPool", "tensorrt_llm::runtime::BufferManager::CudaMemPoolPtr")
                .cast().pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::IExecutionContext")
                .cast().pointerTypes("Pointer"));

        // 跳过 BufferManager 测试类
        infoMap.put(new Info("tensorrt_llm::runtime::BufferManager::IBufferManagedTest").pointerTypes(\"Pointer\"));

        // DimType64 和复杂类型表达式
        infoMap.put(new Info("tensorrt_llm::runtime::ITensor::DimType64",
                "tensorrt_llm::executor::Shape::DimType64",
                "detail::DimType64",
                "std::remove_reference_t")
                .cast().valueTypes("long").pointerTypes("LongPointer"));

        // vector<Response> 用于 Executor.awaitResponses
        infoMap.put(new Info("std::vector<tensorrt_llm::executor::Response>").pointerTypes(\"Pointer\"));

        // 更多复杂 vector 类型
        infoMap.put(new Info("std::vector<tensorrt_llm::executor::VecLogProbs>",
                "std::vector<std::optional<std::string> >",
                "std::vector<std::optional",
                "std::shared_ptr<tensorrt_llm::executor::KVCacheEventManager>")
                .skip());

        // 跳过 Executor 中返回 vector<Response> 和 getKVCacheEventManager 的方法
        infoMap.put(new Info("tensorrt_llm::executor::Executor::awaitResponses",
                "tensorrt_llm::executor::Executor::getKVCacheEventManager")
                .skip());

        // BufferView 类型
        infoMap.put(new Info("tensorrt_llm::executor::BufferView", "BufferView",
                "std::basic_string_view", "std::string_view")
                .pointerTypes("BytePointer"));

        // ======================================================
        // 15. 缺失类型的映射
        // ======================================================
        // C++ auto 返回值
        infoMap.put(new Info("auto").pointerTypes(\"Pointer\"));
        // size_type
        infoMap.put(new Info("size_type", "std::size_t", "size_t",
                "tensorrt_llm::executor::Shape::size_type")
                .cast().valueTypes("long").pointerTypes("LongPointer"));
        // runtime_error 基类
        infoMap.put(new Info("std::runtime_error").pointerTypes("Pointer").base("Pointer"));
        // RuntimeDefaults
        infoMap.put(new Info("tensorrt_llm::runtime::RuntimeDefaults", "RuntimeDefaults").skip());
        // BlockKey / AgentState / UniqueToken / VecUniqueTokens - 映射为 Pointer 而不是 skip
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::BlockKey", "BlockKey")
                .pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::AgentState", "AgentState")
                .pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::UniqueToken", "UniqueToken")
                .pointerTypes("Pointer"));
        // MultimodalInput 中的 vector<optional<string>>
        infoMap.put(new Info("std::vector<std::optional<std::string>>",
                "std::vector<std::optional<std::string> >")
                .skip());
    }
}

