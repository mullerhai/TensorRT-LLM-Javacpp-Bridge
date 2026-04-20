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
        infoMap.put(new Info("tensorrt_llm::executor::TensorPtr").skip());
        infoMap.put(new Info("tensorrt_llm::executor::VecTokens").skip());
        infoMap.put(new Info("tensorrt_llm::executor::BeamTokens").skip());
        infoMap.put(new Info("tensorrt_llm::executor::VecTokenExtraIds").skip());
        infoMap.put(new Info("tensorrt_llm::executor::VecLogProbs").skip());
        infoMap.put(new Info("tensorrt_llm::executor::StreamPtr").skip());
        infoMap.put(new Info("tensorrt_llm::executor::MillisecondsType").skip());
        infoMap.put(new Info("tensorrt_llm::executor::LogitsPostProcessor").skip());
        infoMap.put(new Info("tensorrt_llm::executor::LogitsPostProcessorMap").skip());
        infoMap.put(new Info("tensorrt_llm::executor::LogitsPostProcessorBatched").skip());
        infoMap.put(new Info("tensorrt_llm::executor::MedusaChoices").skip());
        infoMap.put(new Info("tensorrt_llm::executor::EagleChoices").skip());
        infoMap.put(new Info("tensorrt_llm::executor::BufferView").skip());

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
        infoMap.put(new Info("tensorrt_llm::executor::MmKey").skip());

        // ===== Skip TypeTraits (template specializations) =====
        infoMap.put(new Info("tensorrt_llm::executor::TypeTraits").skip());

        // ===== Skip all NIXL/Mooncake/UCX transfer classes =====
        infoMap.put(new Info("tensorrt_llm::executor::kv_cache::NixlHelper").skip());
        infoMap.put(new Info("tensorrt_llm::executor::kv_cache::NixlTransferAgent").skip());
        infoMap.put(new Info("tensorrt_llm::executor::kv_cache::NixlLoopbackAgent").skip());
        infoMap.put(new Info("tensorrt_llm::executor::kv_cache::MooncakeTransferAgent").skip());
        infoMap.put(new Info("tensorrt_llm::executor::kv_cache::UcxConnection").skip());
        infoMap.put(new Info("tensorrt_llm::executor::kv_cache::UcxConnectionManager").skip());
        infoMap.put(new Info("tensorrt_llm::executor::kv_cache::UcxCacheCommunicator").skip());
        infoMap.put(new Info("tensorrt_llm::executor::kv_cache::MpiConnection").skip());
        infoMap.put(new Info("tensorrt_llm::executor::kv_cache::CacheSplitConcat").skip());
        infoMap.put(new Info("tensorrt_llm::executor::kv_cache::CacheCommunicator").skip());
        infoMap.put(new Info("tensorrt_llm::executor::kv_cache::DataTransceiverState").skip());
        infoMap.put(new Info("tensorrt_llm::executor::kv_cache::TransferAgent").skip());

        // NIXL external types
        infoMap.put(new Info("nixlAgent", "nixl_opt_args_t", "nixl_xfer_dlist_t", "nixlBasicDesc",
                "nixl_mem_t", "nixl_xfer_op_t", "nixlXferReqH", "nixl_reg_dlist_t",
                "nixl_notifs_t", "nixl_b_desc_t").skip());

        // ===== Skip heavy classes =====
        infoMap.put(new Info("tensorrt_llm::executor::ExecutorImpl").skip());
        infoMap.put(new Info("tensorrt_llm::executor::RequestImpl").skip());
        infoMap.put(new Info("tensorrt_llm::executor::ResponseImpl").skip());
        infoMap.put(new Info("tensorrt_llm::executor::DynamicBatchTuner").skip());
        infoMap.put(new Info("tensorrt_llm::executor::Serialization").skip());

        // Shape type (inherits from span-like base)
        infoMap.put(new Info("tensorrt_llm::executor::Shape").skip());
        infoMap.put(new Info("tensorrt_llm::executor::detail::DimType64").skip());
    }
}
