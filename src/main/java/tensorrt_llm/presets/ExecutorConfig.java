package tensorrt_llm.presets;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;
import org.bytedeco.javacpp.tools.*;

@Properties(
    value = @Platform(
        includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include", "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp", "/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/cuda-stubs"},
        include = {
            "tensorrt_llm/executor/types.h",
            "tensorrt_llm/executor/tensor.h",
            "tensorrt_llm/executor/executor.h"
        }
    ),
    target = "tensorrt_llm.executor",
    global = "tensorrt_llm.global.Executor"
)
public class ExecutorConfig implements InfoMapper {
    @Override
    public void map(InfoMap infoMap) {
        CommonConfig.mapCommonTypes(infoMap);

        // ===== Executor type aliases -> Java primitives or skip =====
        infoMap.put(new Info("tensorrt_llm::executor::SizeType32").valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("tensorrt_llm::executor::SizeType64").valueTypes("long").pointerTypes("LongPointer"));
        infoMap.put(new Info("tensorrt_llm::executor::FloatType").valueTypes("float").pointerTypes("FloatPointer"));
        infoMap.put(new Info("tensorrt_llm::executor::TokenIdType").valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("tensorrt_llm::executor::IdType").valueTypes("long").pointerTypes("LongPointer"));
        infoMap.put(new Info("tensorrt_llm::executor::IterationType").valueTypes("long").pointerTypes("LongPointer"));
        infoMap.put(new Info("tensorrt_llm::executor::RandomSeedType").valueTypes("long").pointerTypes("LongPointer"));
        infoMap.put(new Info("tensorrt_llm::executor::PriorityType").valueTypes("float").pointerTypes("FloatPointer"));
        infoMap.put(new Info("tensorrt_llm::executor::CacheSaltIDType").valueTypes("long").pointerTypes("LongPointer"));

        // Complex type aliases -> skip
        infoMap.put(new Info("tensorrt_llm::executor::TensorPtr").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::VecTokens").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::BeamTokens").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::VecTokenExtraIds").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::VecLogProbs").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::StreamPtr").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::MillisecondsType").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::LogitsPostProcessor").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::LogitsPostProcessorMap").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::LogitsPostProcessorBatched").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::MedusaChoices").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::EagleChoices").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::BufferView").pointerTypes("Pointer"));

        // Executor enums -> enumerate
        infoMap.put(new Info("tensorrt_llm::executor::DataType").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::RequestType").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::MemoryType").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::ModelType").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::BatchingType").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::CapacitySchedulerPolicy").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::ContextChunkingPolicy").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::CommunicationType").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::CommunicationMode").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::RequestStage").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::FinishReason").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::KvCacheTransferMode").enumerate());

        // ===== Skip MmKey (uses std::array) =====
        infoMap.put(new Info("tensorrt_llm::executor::MmKey").pointerTypes("Pointer"));

        // ===== Skip TypeTraits (template specializations) =====
        infoMap.put(new Info("tensorrt_llm::executor::TypeTraits").pointerTypes("Pointer"));

        // ===== Skip all NIXL/Mooncake/UCX transfer classes =====
        infoMap.put(new Info("tensorrt_llm::executor::kv_cache::NixlHelper").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::kv_cache::NixlTransferAgent").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::kv_cache::NixlLoopbackAgent").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::kv_cache::MooncakeTransferAgent").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::kv_cache::UcxConnection").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::kv_cache::UcxConnectionManager").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::kv_cache::UcxCacheCommunicator").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::kv_cache::MpiConnection").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::kv_cache::CacheSplitConcat").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::kv_cache::CacheCommunicator").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::kv_cache::DataTransceiverState").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::kv_cache::TransferAgent").pointerTypes("Pointer"));

        // NIXL external types
        infoMap.put(new Info("nixlAgent", "nixl_opt_args_t", "nixl_xfer_dlist_t", "nixlBasicDesc",
                "nixl_mem_t", "nixl_xfer_op_t", "nixlXferReqH", "nixl_reg_dlist_t",
                "nixl_notifs_t", "nixl_b_desc_t").skip());

        // ===== Skip heavy classes =====
        infoMap.put(new Info("tensorrt_llm::executor::ExecutorImpl").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::RequestImpl").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::ResponseImpl").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::DynamicBatchTuner").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::Serialization").pointerTypes("Pointer"));

        // Shape type (inherits from span-like base)
        infoMap.put(new Info("tensorrt_llm::executor::Shape").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::detail::DimType64").pointerTypes("Pointer"));

        // ===== Additional template specializations leaking into generated Java =====
        // std::vector<X> specializations that JavaCPP failed to map — route to Pointer
        infoMap.put(new Info(
                "std::vector<tensorrt_llm::executor::Response>",
                "std::vector<tensorrt_llm::executor::AdditionalModelOutput>",
                "std::vector<tensorrt_llm::executor::VecLogProbs>",
                "std::vector<tensorrt_llm::executor::TokenIdType>",
                "std::vector<std::string>",
                "std::vector<std::optional<std::string> >",
                "std::vector<std::optional<std::string>>",
                "std::vector<runtime::ITensor::SharedPtr>",
                "std::vector<tensorrt_llm::runtime::ITensor::SharedPtr>"
        ).pointerTypes("Pointer").cast());

        // std::shared_ptr<X> specializations
        infoMap.put(new Info(
                "std::shared_ptr<tensorrt_llm::executor::KVCacheEventManager>",
                "std::shared_ptr<tensorrt_llm::executor::AdditionalModelOutput>"
        ).pointerTypes("Pointer").cast());

        // Additional executor leaf types referenced via vectors but not yet mapped
        infoMap.put(new Info("tensorrt_llm::executor::AdditionalModelOutput").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::Response").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::KVCacheEventManager").pointerTypes("Pointer"));
    }
}
