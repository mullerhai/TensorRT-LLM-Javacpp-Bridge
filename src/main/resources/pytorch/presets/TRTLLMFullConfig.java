package tensorrt_llm.presets;

import org.bytedeco.javacpp.annotation.Platform;
import org.bytedeco.javacpp.annotation.Properties;
import org.bytedeco.javacpp.tools.Info;
import org.bytedeco.javacpp.tools.InfoMap;
import org.bytedeco.javacpp.tools.InfoMapper;

@Properties(
        value = @Platform(
                includepath = {
                        "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include",
                        "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp",
                        "/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/cuda-stubs",
                        "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/tensorrt_llm/layers",
                        "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/tensorrt_llm/kernels"
                },
                include = {
                        // ... existing common, executor, runtime, batch_manager headers ...
                        "tensorrt_llm/common/config.h",
                        "tensorrt_llm/common/logger.h",
                        "tensorrt_llm/common/tllmException.h",
                        "tensorrt_llm/common/assert.h",
                        "tensorrt_llm/common/dataType.h",
                        "tensorrt_llm/executor/executor.h",
                        "tensorrt_llm/executor/types.h",
                        "tensorrt_llm/executor/tensor.h",
                        "tensorrt_llm/executor/serialization.h",
                        "tensorrt_llm/runtime/iBuffer.h",
                        "tensorrt_llm/runtime/iTensor.h",
                        "tensorrt_llm/runtime/bufferManager.h",
                        "tensorrt_llm/runtime/samplingConfig.h",
                        "tensorrt_llm/runtime/worldConfig.h",
                        "tensorrt_llm/batch_manager/llmRequest.h",
                        "tensorrt_llm/batch_manager/kvCacheType.h",
                        "tensorrt_llm/batch_manager/common.h",

                        // ============================================
                        // layers headers
                        // ============================================
                        "tensorrt_llm/layers/decodingParams.h",
                        "tensorrt_llm/layers/baseLayer.h",
                        "tensorrt_llm/layers/decodingLayer.h",
                        "tensorrt_llm/layers/dynamicDecodeLayer.h",
                        "tensorrt_llm/layers/samplingLayer.h",
                        "tensorrt_llm/layers/topKSamplingLayer.h",
                        "tensorrt_llm/layers/topPSamplingLayer.h",
                        "tensorrt_llm/layers/penaltyLayer.h",
                        "tensorrt_llm/layers/banWordsLayer.h",
                        "tensorrt_llm/layers/stopCriteriaLayer.h",
                        "tensorrt_llm/layers/beamSearchLayer.h",
                        "tensorrt_llm/layers/medusaDecodingLayer.h",
                        "tensorrt_llm/layers/eagleDecodingLayer.h",
                        "tensorrt_llm/layers/lookaheadAlgorithm.h",
                        "tensorrt_llm/layers/lookaheadPoolManager.h",
                        "tensorrt_llm/layers/lookaheadDecodingLayer.h",
                        "tensorrt_llm/layers/lookaheadDecodingUtils.h",
                        "tensorrt_llm/layers/explicitDraftTokensLayer.h",
                        "tensorrt_llm/layers/externalDraftTokensLayer.h",
                        "tensorrt_llm/layers/layerUtils.h",
                        "tensorrt_llm/layers/layersFactory.h",

                        // ============================================
                        // kernels headers - CUDA核函数映射
                        // ============================================
                        "tensorrt_llm/kernels/decodingKernels.h",
                        "tensorrt_llm/kernels/banBadWords.h",
                        "tensorrt_llm/kernels/banRepeatNgram.h",
                        "tensorrt_llm/kernels/beamSearchKernels.h",
                        "tensorrt_llm/kernels/attentionMask.h",
                        "tensorrt_llm/kernels/layernormKernels.h",
                        "tensorrt_llm/kernels/penaltyKernels.h",
                        "tensorrt_llm/kernels/samplingTopKKernels.h",
                        "tensorrt_llm/kernels/samplingTopPKernels.h",
                        "tensorrt_llm/kernels/stopCriteriaKernels.h",
                        "tensorrt_llm/kernels/decoderMaskedMultiheadAttention.h",
                        "tensorrt_llm/kernels/contextFusedMultiHeadAttention.h",
                        "tensorrt_llm/kernels/unfusedAttentionKernels.h",
                        "tensorrt_llm/kernels/kvCacheUtils.h",
                        "tensorrt_llm/kernels/quantization.h",
                        "tensorrt_llm/kernels/fusedLayernormKernels.h",
                        "tensorrt_llm/kernels/customAllReduceKernels.h",
                        "tensorrt_llm/kernels/communicationKernels.h",
                        "tensorrt_llm/kernels/lookupKernels.h",
                        "tensorrt_llm/kernels/gptKernels.h"
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
        infoMap.put(new Info("std::optional").skip());
        infoMap.put(new Info("std::nullopt").skip());
        infoMap.put(new Info("std::variant").skip());

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
        // vector<TensorPtr> from layers
        infoMap.put(new Info("std::vector<tensorrt_llm::layers::TensorPtr>")
                .annotations("@StdVector").pointerTypes("PointerPointer"));

        // 复杂容器 - 跳过
        infoMap.put(new Info("std::vector<std::vector", "std::vector<std::string>",
                "std::vector<std::pair", "std::list", "std::deque",
                "tensorrt_llm::executor::BeamTokens", "BeamTokens",
                "tensorrt_llm::executor::VecTokenExtraIds", "VecTokenExtraIds",
                "tensorrt_llm::executor::VecUniqueTokens", "VecUniqueTokens",
                "tensorrt_llm::executor::MedusaChoices", "MedusaChoices",
                "tensorrt_llm::executor::EagleChoices", "EagleChoices")
                .skip());

        // Skip nested vector fields that cause duplicate @StdVector
        infoMap.put(new Info("tensorrt_llm::layers::MedusaDecodingInputs::medusaLogits",
                "tensorrt_llm::layers::MedusaSetupParams::runtimeHeadsTopK",
                "tensorrt_llm::layers::BeamSearchSetupParams::beamWidthArray")
                .skip());

        // MultimodalInput has nested vectors that cause duplicate @StdVector annotations
        infoMap.put(new Info("tensorrt_llm::executor::MultimodalInput").skip());

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
        infoMap.put(new Info("tensorrt_llm::executor::DecodingMode").skip());

        // ======================================================
        // 9. Shape 基类问题 - 跳过整个Shape和Serialization类
        // ======================================================
        infoMap.put(new Info("tensorrt_llm::executor::Shape").skip());
        infoMap.put(new Info("tensorrt_llm::executor::Serialization").skip());
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
        // NOTE: Do not skip DefaultDecodingParams; generate it from headers.
        infoMap.put(new Info("tensorrt_llm::runtime::CudaEvent").skip());

        // ======================================================
        // 12a. Kernels / CUDA types needed by layers
        // ======================================================
        // curandState_t used by layers
        infoMap.put(new Info("curandState_t", "curandStateMtgp32_t", "curandStatePhilox4_32_10_t")
                .cast().pointerTypes("Pointer"));
        // DecodingLayerWorkspace - opaque
        infoMap.put(new Info("tensorrt_llm::runtime::DecodingLayerWorkspace")
                .cast().pointerTypes("Pointer"));
        // BeamHypotheses - used by beamSearchLayer, keep as opaque Pointer
        infoMap.put(new Info("tensorrt_llm::kernels::BeamHypotheses")
                .cast().pointerTypes("Pointer"));
        // FinishedState
        infoMap.put(new Info("tensorrt_llm::kernels::FinishedState")
                .pointerTypes("Pointer"));
        // Skip kernel-level functions and internal kernel types
        infoMap.put(new Info("tensorrt_llm::kernels::invokeTopkLastDim",
                "tensorrt_llm::kernels::topkLastDimKernel",
                "tensorrt_llm::kernels::invokeSoftMax",
                "tensorrt_llm::kernels::invokeBatchTopKSampling",
                "tensorrt_llm::kernels::invokeTopPSampling",
                "tensorrt_llm::kernels::beamSearchKernel",
                "tensorrt_llm::kernels::TopkLastDimContext",
                "tensorrt_llm::kernels::DecodingKernelParams",
                "tensorrt_llm::kernels::BeamSearchParams",
                "tensorrt_llm::kernels::KVLinearBuffer",
                "tensorrt_llm::kernels::KVBlockArray")
                .skip());
        // SpeculativeDecodingModule
        infoMap.put(new Info("tensorrt_llm::runtime::SpeculativeDecodingModule")
                .cast().pointerTypes("Pointer"));
        // layers TensorPtr / BufferPtr type aliases
        infoMap.put(new Info("tensorrt_llm::layers::TensorPtr", "tensorrt_llm::layers::TensorConstPtr",
                "tensorrt_llm::layers::BufferPtr", "tensorrt_llm::layers::BufferConstPtr")
                .pointerTypes("Pointer"));
        // layers OptVec template
        infoMap.put(new Info("tensorrt_llm::layers::OptVec").skip());
        // layers internal complex types
        infoMap.put(new Info("tensorrt_llm::layers::DecodingSetupParams",
                "tensorrt_llm::layers::DecodingForwardParams",
                "tensorrt_llm::layers::DecodingOutputParams")
                .pointerTypes("Pointer"));

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
        infoMap.put(new Info("tensorrt_llm::runtime::BufferManager::IBufferManagedTest").skip());

        // DimType64 和复杂类型表达式
        infoMap.put(new Info("tensorrt_llm::runtime::ITensor::DimType64",
                "tensorrt_llm::executor::Shape::DimType64",
                "detail::DimType64",
                "std::remove_reference_t")
                .cast().valueTypes("long").pointerTypes("LongPointer"));

        // vector<Response> 用于 Executor.awaitResponses
        infoMap.put(new Info("std::vector<tensorrt_llm::executor::Response>").skip());

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
        infoMap.put(new Info("auto").skip());
        // size_type - skip Shape constructor that uses it
        infoMap.put(new Info("size_type", "std::size_t", "size_t",
                "tensorrt_llm::executor::Shape::size_type")
                .cast().valueTypes("long").pointerTypes("LongPointer"));
        // Skip Shape constructor with size_type param (generates invalid code)
        infoMap.put(new Info("tensorrt_llm::executor::Shape(const DimType64*, size_type)",
                "tensorrt_llm::executor::Shape::Shape(const DimType64*, size_type)")
                .skip());
        // Skip Serialization methods that use skipped types
        infoMap.put(new Info("tensorrt_llm::executor::Serialization::serializedSize(const tensorrt_llm::executor::AgentState&)",
                "tensorrt_llm::executor::Serialization::serializedSize(const tensorrt_llm::executor::UniqueToken&)")
                .skip());
        // runtime_error 基类
        infoMap.put(new Info("std::runtime_error").pointerTypes("Pointer").base("Pointer"));
        // RuntimeDefaults
        infoMap.put(new Info("tensorrt_llm::runtime::RuntimeDefaults", "RuntimeDefaults").skip());
        // BlockKey / AgentState / UniqueToken - skip types that cause cascading issues
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::BlockKey", "BlockKey")
                .pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::AgentState", "AgentState")
                .pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::UniqueToken", "UniqueToken")
                .skip());
        // KVCacheStoredBlockData references VecUniqueTokens which is skipped
        infoMap.put(new Info("tensorrt_llm::executor::KVCacheStoredBlockData").skip());
        // MultimodalInput 中的 vector<optional<string>>
        infoMap.put(new Info("std::vector<std::optional<std::string>>",
                "std::vector<std::optional<std::string> >")
                .skip());

        // ======================================================
        // 12b. Layer classes - generate Java classes for layer implementations
        // ======================================================
        // BaseLayer - non-template base class
        infoMap.put(new Info("tensorrt_llm::layers::BaseLayer").pointerTypes("BaseLayer"));

        // Template layer classes - specify concrete instantiations
        // Most layers use template<typename T> where T is typically float or half
        // We'll generate the float version for Java usage

        // TopKSamplingLayer<T> -> TopKSamplingLayer (float instantiation)
        infoMap.put(new Info("tensorrt_llm::layers::TopKSamplingLayer<float>").pointerTypes("TopKSamplingLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::TopKSamplingLayer").pointerTypes("TopKSamplingLayer"));

        // TopPSamplingLayer<T> -> TopPSamplingLayer
        infoMap.put(new Info("tensorrt_llm::layers::TopPSamplingLayer<float>").pointerTypes("TopPSamplingLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::TopPSamplingLayer").pointerTypes("TopPSamplingLayer"));

        // SamplingLayer<T> -> SamplingLayer
        infoMap.put(new Info("tensorrt_llm::layers::SamplingLayer<float>").pointerTypes("SamplingLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::SamplingLayer").pointerTypes("SamplingLayer"));

        // PenaltyLayer<T> -> PenaltyLayer
        infoMap.put(new Info("tensorrt_llm::layers::PenaltyLayer<float>").pointerTypes("PenaltyLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::PenaltyLayer").pointerTypes("PenaltyLayer"));

        // ======================================================
        // 生成剩余10个Layer的template<float>实例化
        // 添加显式的template实例化声明供JavaCPP使用
        // ======================================================

        // 为JavaCPP注入显式的template实例化声明
        String templateInstantiations =
            "namespace tensorrt_llm::layers {" +
            "template class DecodingLayer<float>;" +
            "template class DynamicDecodeLayer<float>;" +
            "template class BanWordsLayer<float>;" +
            "template class StopCriteriaLayer<float>;" +
            "template class BeamSearchLayer<float>;" +
            "template class MedusaDecodingLayer<float>;" +
            "template class EagleDecodingLayer<float>;" +
            "template class LookaheadDecodingLayer<float>;" +
            "template class ExplicitDraftTokensLayer<float>;" +
            "template class ExternalDraftTokensLayer<float>;" +
            "}";

        infoMap.put(new Info("").cppText(templateInstantiations));

        // DecodingLayer<float>
        infoMap.put(new Info("tensorrt_llm::layers::DecodingLayer<float>").pointerTypes("DecodingLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::DecodingLayer").pointerTypes("DecodingLayer"));

        // DynamicDecodeLayer<float>
        infoMap.put(new Info("tensorrt_llm::layers::DynamicDecodeLayer<float>").pointerTypes("DynamicDecodeLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::DynamicDecodeLayer").pointerTypes("DynamicDecodeLayer"));

        // BanWordsLayer<float>
        infoMap.put(new Info("tensorrt_llm::layers::BanWordsLayer<float>").pointerTypes("BanWordsLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::BanWordsLayer").pointerTypes("BanWordsLayer"));

        // StopCriteriaLayer<float>
        infoMap.put(new Info("tensorrt_llm::layers::StopCriteriaLayer<float>").pointerTypes("StopCriteriaLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::StopCriteriaLayer").pointerTypes("StopCriteriaLayer"));

        // BeamSearchLayer<float>
        infoMap.put(new Info("tensorrt_llm::layers::BeamSearchLayer<float>").pointerTypes("BeamSearchLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::BeamSearchLayer").pointerTypes("BeamSearchLayer"));

        // MedusaDecodingLayer<float>
        infoMap.put(new Info("tensorrt_llm::layers::MedusaDecodingLayer<float>").pointerTypes("MedusaDecodingLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::MedusaDecodingLayer").pointerTypes("MedusaDecodingLayer"));

        // EagleDecodingLayer<float>
        infoMap.put(new Info("tensorrt_llm::layers::EagleDecodingLayer<float>").pointerTypes("EagleDecodingLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::EagleDecodingLayer").pointerTypes("EagleDecodingLayer"));

        // LookaheadDecodingLayer<float>
        infoMap.put(new Info("tensorrt_llm::layers::LookaheadDecodingLayer<float>").pointerTypes("LookaheadDecodingLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::LookaheadDecodingLayer").pointerTypes("LookaheadDecodingLayer"));

        // ExplicitDraftTokensLayer<float>
        infoMap.put(new Info("tensorrt_llm::layers::ExplicitDraftTokensLayer<float>").pointerTypes("ExplicitDraftTokensLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::ExplicitDraftTokensLayer").pointerTypes("ExplicitDraftTokensLayer"));

        // ExternalDraftTokensLayer<float>
        infoMap.put(new Info("tensorrt_llm::layers::ExternalDraftTokensLayer<float>").pointerTypes("ExternalDraftTokensLayer"));
        infoMap.put(new Info("tensorrt_llm::layers::ExternalDraftTokensLayer").pointerTypes("ExternalDraftTokensLayer"));

        // ======================================================
        // LayerUtils (layerUtils.h) 映射 - 强制生成所有结构体和函数
        // ======================================================

        // FillBuffers struct - 必须生成为真实类
        infoMap.put(new Info("tensorrt_llm::layers::FillBuffers")
                .pointerTypes("FillBuffers"));

        // FillBuffers 成员变量
        infoMap.put(new Info("tensorrt_llm::layers::FillBuffers::batchSize").cast());
        infoMap.put(new Info("tensorrt_llm::layers::FillBuffers::maxBatchSize").cast());
        infoMap.put(new Info("tensorrt_llm::layers::FillBuffers::mBufferManager").cast());

        // FillBuffers 的operator() - 模板方法，需要为常用类型生成
        infoMap.put(new Info("tensorrt_llm::layers::FillBuffers::operator()<std::optional<std::vector<float>>>").cast());
        infoMap.put(new Info("tensorrt_llm::layers::FillBuffers::operator()<float>").cast());

        // LayerUtils 工具函数 - 强制生成
        infoMap.put(new Info("tensorrt_llm::layers::getLocalDecoderDomain")
                .cast());
        infoMap.put(new Info("tensorrt_llm::layers::allOfBatchSlots<float>")
                .cast());
        infoMap.put(new Info("tensorrt_llm::layers::maxOfBatchSlots<float>")
                .cast());
        infoMap.put(new Info("tensorrt_llm::layers::expandMatchElements")
                .cast());

        // ======================================================
        // LayersFactory (layersFactory.h) 映射 - 强制生成枚举和工厂函数
        // ======================================================

        // DecodingLayers_t enum - 必须生成为真实枚举
        infoMap.put(new Info("tensorrt_llm::layers::DecodingLayers_t")
                .cast());

        // 枚举值 - 强制映射
        infoMap.put(new Info("tensorrt_llm::layers::PENALTY_LAYER",
                            "tensorrt_llm::layers::BAN_WORDS_LAYER",
                            "tensorrt_llm::layers::DECODING_LAYER",
                            "tensorrt_llm::layers::STOP_CRITERIA_LAYER")
                .cast());

        // createDecodingLayerTypes 工厂函数 - 强制生成
        infoMap.put(new Info("tensorrt_llm::layers::createDecodingLayerTypes")
                .cast());

        // createLayers<T> 工厂模板函数 - 强制为float版本生成
        infoMap.put(new Info("tensorrt_llm::layers::createLayers<float>")
                .cast());

        // LayerUtils 和 LayersFactory 类包装器
        infoMap.put(new Info("tensorrt_llm::layers::LayerUtils")
                .pointerTypes("LayerUtils"));
        infoMap.put(new Info("tensorrt_llm::layers::LayersFactory")
                .pointerTypes("LayersFactory"));


        // ======================================================
        // Kernels 映射 - 强制JavaCPP生成所有kernel struct和函数
        // ======================================================

        // 为template kernel struct注入显式实例化
        String kernelTemplateInstantiations =
            "namespace tensorrt_llm::kernels {" +
            "template struct TopKSamplingKernelParams<float>;" +
            "template struct TopKSamplingKernelParams<__half>;" +
            "template struct TopPSamplingKernelParams<float>;" +
            "template struct TopPSamplingKernelParams<__half>;" +
            "template struct Multihead_attention_params<float, false>;" +
            "template struct Multihead_attention_params<float, true>;" +
            "template struct Multihead_attention_params<__half, false>;" +
            "template struct Multihead_attention_params<__half, true>;" +
            "template struct InvokeBatchApplyPenaltyParams<float>;" +
            "template struct InvokeBatchApplyPenaltyParams<__half>;" +
            "}";
        infoMap.put(new Info("").cppText(kernelTemplateInstantiations));

        // ========== Decoding Kernels ==========
        // beamSearchKernels.h
        infoMap.put(new Info("tensorrt_llm::kernels::gatherTreeParam")
                .pointerTypes("GatherTreeParam"));
        infoMap.put(new Info("tensorrt_llm::kernels::BeamHypotheses")
                .pointerTypes("BeamHypotheses"));
        infoMap.put(new Info("tensorrt_llm::kernels::invokeGatherTree").cast());
        infoMap.put(new Info("tensorrt_llm::kernels::invokeInsertUnfinishedPath").cast());
        infoMap.put(new Info("tensorrt_llm::kernels::invokeFinalize").cast());
        infoMap.put(new Info("tensorrt_llm::kernels::invokeInitializeOutput").cast());

        // decodingKernels.h
        infoMap.put(new Info("tensorrt_llm::kernels::invokeDecoding").cast());
        infoMap.put(new Info("tensorrt_llm::kernels::invokeBanBadWords").cast());
        infoMap.put(new Info("tensorrt_llm::kernels::invokeBanRepeatNgrams").cast());
        infoMap.put(new Info("tensorrt_llm::kernels::invokePenalty").cast());

        // ========== Sampling Kernels ==========
        // samplingTopKKernels.h - 必须生成TopKSamplingKernelParams<float>和<__half>
        infoMap.put(new Info("tensorrt_llm::kernels::TopKSamplingKernelParams<float>")
                .pointerTypes("TopKSamplingKernelParams"));
        infoMap.put(new Info("tensorrt_llm::kernels::TopKSamplingKernelParams")
                .pointerTypes("TopKSamplingKernelParams"));
        infoMap.put(new Info("tensorrt_llm::kernels::invokeBatchTopKSampling").cast());
        infoMap.put(new Info("tensorrt_llm::kernels::invokeSetupTopKRuntimeArgs").cast());
        infoMap.put(new Info("tensorrt_llm::kernels::invokeSetupTopKTopPRuntimeArgs").cast());

        // samplingTopPKernels.h - 必须生成TopPSamplingKernelParams<float>和<__half>
        infoMap.put(new Info("tensorrt_llm::kernels::TopPSamplingKernelParams<float>")
                .pointerTypes("TopPSamplingKernelParams"));
        infoMap.put(new Info("tensorrt_llm::kernels::TopPSamplingKernelParams")
                .pointerTypes("TopPSamplingKernelParams"));
        infoMap.put(new Info("tensorrt_llm::kernels::invokeBatchTopPSampling").cast());
        infoMap.put(new Info("tensorrt_llm::kernels::invokeComputeToppDecay").cast());
        infoMap.put(new Info("tensorrt_llm::kernels::invokeBatchAirTopPSampling").cast());
        infoMap.put(new Info("tensorrt_llm::kernels::invokeSetTopPRuntimeArgs").cast());

        // ========== Attention Kernels ==========
        // decoderMaskedMultiheadAttention.h - 必须生成Multihead_attention_params<T, bool>
        infoMap.put(new Info("tensorrt_llm::kernels::Multihead_attention_params_base<float>")
                .pointerTypes("Multihead_attention_params_base"));
        infoMap.put(new Info("tensorrt_llm::kernels::Multihead_attention_params_base")
                .pointerTypes("Multihead_attention_params_base"));
        infoMap.put(new Info("tensorrt_llm::kernels::Multihead_attention_params<float, false>")
                .pointerTypes("Multihead_attention_params"));
        infoMap.put(new Info("tensorrt_llm::kernels::Multihead_attention_params<float, true>")
                .pointerTypes("Multihead_attention_params"));
        infoMap.put(new Info("tensorrt_llm::kernels::Multihead_attention_params")
                .pointerTypes("Multihead_attention_params"));
        infoMap.put(new Info("tensorrt_llm::kernels::invokeDecoderMaskedMultiheadAttention").cast());

        // ========== Quantization Kernels ==========

        // ======================================================
        // LayerFactory 和 LayerUtils - 工厂和工具函数
        // 使用JavaCPP自动生成，注入显式声明强制生成
        // ======================================================

        // 为JavaCPP注入显式的LayerFactory和LayerUtils声明
        String layerFactoryUtilsCode =
            "namespace tensorrt_llm::layers {" +
            // Inject enum for JavaCPP to parse
            "enum DecodingLayers_t { PENALTY_LAYER = 0, BAN_WORDS_LAYER = 1, DECODING_LAYER = 2, STOP_CRITERIA_LAYER = 3 };" +
            // Inject template instantiations for factory functions
            "template std::vector<DecodingLayers_t> createDecodingLayerTypes(executor::DecodingMode const&);" +
            "template std::vector<std::unique_ptr<BaseLayer>> createLayers<float>(executor::DecodingMode const&, DecoderDomain const&, std::shared_ptr<runtime::BufferManager> const&);" +
            // Inject utility function instantiations
            "template bool allOfBatchSlots<float>(int const*, float const*, int, float);" +
            "template bool allOfBatchSlots<int>(int const*, int const*, int, int);" +
            "template float maxOfBatchSlots<float>(int const*, float const*, int);" +
            "template int maxOfBatchSlots<int>(int const*, int const*, int);" +
            "template size_t expandMatchElements<int>(size_t, std::vector<int>&);" +
            "}";
        infoMap.put(new Info("").cppText(layerFactoryUtilsCode));

        // FillBuffers struct - 由JavaCPP自动生成
        infoMap.put(new Info("tensorrt_llm::layers::FillBuffers").pointerTypes("FillBuffers"));

        // DecodingLayers_t enum - 由JavaCPP自动生成为枚举
        infoMap.put(new Info("tensorrt_llm::layers::DecodingLayers_t").enumerate());

        // Enum values
        infoMap.put(new Info("tensorrt_llm::layers::PENALTY_LAYER",
                            "tensorrt_llm::layers::BAN_WORDS_LAYER",
                            "tensorrt_llm::layers::DECODING_LAYER",
                            "tensorrt_llm::layers::STOP_CRITERIA_LAYER").enumerate());

        // createDecodingLayerTypes - 由JavaCPP自动生成
        infoMap.put(new Info("tensorrt_llm::layers::createDecodingLayerTypes").cast());

        // createLayers<float> - 由JavaCPP自动生成
        infoMap.put(new Info("tensorrt_llm::layers::createLayers<float>").cast());
        infoMap.put(new Info("tensorrt_llm::layers::createLayers").cast());

        // LayerUtils functions - 由JavaCPP自动生成
        infoMap.put(new Info("tensorrt_llm::layers::getLocalDecoderDomain").cast());

        // allOfBatchSlots template instantiations
        infoMap.put(new Info("tensorrt_llm::layers::allOfBatchSlots<float>").cast());
        infoMap.put(new Info("tensorrt_llm::layers::allOfBatchSlots<int>").cast());
        infoMap.put(new Info("tensorrt_llm::layers::allOfBatchSlots").cast());

        // maxOfBatchSlots template instantiations
        infoMap.put(new Info("tensorrt_llm::layers::maxOfBatchSlots<float>").cast());
        infoMap.put(new Info("tensorrt_llm::layers::maxOfBatchSlots<int>").cast());
        infoMap.put(new Info("tensorrt_llm::layers::maxOfBatchSlots").cast());

        // expandMatchElements
        infoMap.put(new Info("tensorrt_llm::layers::expandMatchElements").cast());


        // Decoding params classes
        infoMap.put(new Info("tensorrt_llm::layers::DefaultDecodingParams").pointerTypes("DefaultDecodingParams"));
        infoMap.put(new Info("tensorrt_llm::layers::DecodingSetupParams").pointerTypes("DecodingSetupParams"));
        infoMap.put(new Info("tensorrt_llm::layers::DecodingForwardParams").pointerTypes("DecodingForwardParams"));
        infoMap.put(new Info("tensorrt_llm::layers::DecodingOutputParams").pointerTypes("DecodingOutputParams"));

        // Skip OptVec template
        infoMap.put(new Info("tensorrt_llm::layers::OptVec").skip());
    }
}

