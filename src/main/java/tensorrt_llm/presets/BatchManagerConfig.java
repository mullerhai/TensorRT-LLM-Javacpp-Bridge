package tensorrt_llm.presets;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;
import org.bytedeco.javacpp.tools.*;

@Properties(
    value = @Platform(
        includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include", "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp", "/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/cuda-stubs"},
        include = {
            "tensorrt_llm/batch_manager/common.h",
            "tensorrt_llm/batch_manager/llmRequest.h",
            "tensorrt_llm/batch_manager/kvCacheManager.h",
            "tensorrt_llm/batch_manager/kvCacheUtils.h",
            "tensorrt_llm/batch_manager/kvCacheType.h",
            "tensorrt_llm/batch_manager/blockKey.h",
            "tensorrt_llm/batch_manager/radixBlockTree.h",
            "tensorrt_llm/batch_manager/evictionPolicy.h",
            "tensorrt_llm/batch_manager/capacityScheduler.h",
            "tensorrt_llm/batch_manager/contextProgress.h",
            "tensorrt_llm/batch_manager/decoderBuffers.h",
            "tensorrt_llm/batch_manager/encoderBuffers.h",
            "tensorrt_llm/batch_manager/loraBuffers.h",
            "tensorrt_llm/batch_manager/medusaBuffers.h",
            "tensorrt_llm/batch_manager/pauseRequests.h",
            "tensorrt_llm/batch_manager/peftCacheManagerConfig.h",
            "tensorrt_llm/batch_manager/peftCacheManager.h",
            "tensorrt_llm/batch_manager/promptTuningBuffers.h",
            "tensorrt_llm/batch_manager/rnnStateBuffers.h",
            "tensorrt_llm/batch_manager/rnnStateManager.h",
            "tensorrt_llm/batch_manager/runtimeBuffers.h",
            "tensorrt_llm/batch_manager/sequenceSlotManager.h",
            "tensorrt_llm/batch_manager/transformerBuffers.h",
            "tensorrt_llm/batch_manager/trtGptModel.h",
            "tensorrt_llm/batch_manager/kvCacheEventManager.h",
            "tensorrt_llm/batch_manager/kvCacheTransferManager.h",
            "tensorrt_llm/batch_manager/logitsPostProcessor.h",
            "tensorrt_llm/batch_manager/microBatchScheduler.h",
            "tensorrt_llm/batch_manager/allocateKvCache.h",
            "tensorrt_llm/batch_manager/createNewDecoderRequests.h",
            "tensorrt_llm/batch_manager/makeDecodingBatchInputOutput.h",
            "tensorrt_llm/batch_manager/updateDecoderBuffers.h",
            "tensorrt_llm/batch_manager/assignReqSeqSlots.h",
            "tensorrt_llm/batch_manager/handleContextLogits.h",
            "tensorrt_llm/batch_manager/handleGenerationLogits.h",
            "tensorrt_llm/batch_manager/kvCacheConnector.h",
            "tensorrt_llm/batch_manager/stringSetTrie.h",
            // Private implementation headers (expose more classes)
            "tensorrt_llm/batch_manager/cacheFormatter.h",
            "tensorrt_llm/batch_manager/cacheTransBuffer.h",
            "tensorrt_llm/batch_manager/cacheTransferLayer.h",
            "tensorrt_llm/batch_manager/baseTransBuffer.h",
            "tensorrt_llm/batch_manager/dataTransceiver.h",
            "tensorrt_llm/batch_manager/mlaCacheFormatter.h",
            "tensorrt_llm/batch_manager/rnnCacheTransBuffer.h",
            "tensorrt_llm/batch_manager/scheduledBlocksManager.h"
        }
    ),
    target = "tensorrt_llm.batch_manager",
    global = "tensorrt_llm.global.Batchmanager"
)
public class BatchmanagerConfig implements InfoMapper {
    @Override
    public void map(InfoMap infoMap) {
        CommonConfig.mapCommonTypes(infoMap);

        // ===== Cross-package executor type aliases =====
        infoMap.put(new Info("tensorrt_llm::executor::IdType").valueTypes("long").pointerTypes("LongPointer"));
        infoMap.put(new Info("tensorrt_llm::executor::IterationType").valueTypes("long").pointerTypes("LongPointer"));
        infoMap.put(new Info("tensorrt_llm::executor::PriorityType").valueTypes("float").pointerTypes("FloatPointer"));
        infoMap.put(new Info("tensorrt_llm::executor::CacheSaltIDType").valueTypes("long").pointerTypes("LongPointer"));
        infoMap.put(new Info("tensorrt_llm::executor::TokenIdType").valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("tensorrt_llm::executor::SizeType32").valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("tensorrt_llm::executor::FloatType").valueTypes("float").pointerTypes("FloatPointer"));
        infoMap.put(new Info("tensorrt_llm::executor::RandomSeedType").valueTypes("long").pointerTypes("LongPointer"));

        // Complex executor aliases -> skip
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
        infoMap.put(new Info("tensorrt_llm::executor::MmKey").pointerTypes("Pointer"));

        // Executor enums
        infoMap.put(new Info("tensorrt_llm::executor::DataType").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::ModelType").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::BatchingType").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::CapacitySchedulerPolicy").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::ContextChunkingPolicy").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::KvCacheTransferMode").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::FinishReason").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::RequestStage").enumerate());
        infoMap.put(new Info("tensorrt_llm::executor::CommunicationType").enumerate());

        // Skip executor classes that appear via cross-reference
        infoMap.put(new Info("tensorrt_llm::executor::AdditionalModelOutput").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::DebugConfig").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::Request").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::Result").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::Response").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::Shape").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::executor::Tensor").pointerTypes("Pointer"));

        // ===== Batch manager specific skips =====
        // Skip template types
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager_v2::Task").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager_v2::DiskAddress").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager_v2::MemAddress").pointerTypes("Pointer"));

        // Skip OptionalRef template types
        infoMap.put(new Info("tensorrt_llm::common::OptionalRef").pointerTypes("Pointer"));
        infoMap.put(new Info("OptionalRef").pointerTypes("Pointer"));

        // Skip methods that use OptionalRef parameters
        infoMap.put(new Info("tensorrt_llm::batch_manager::AllocateKvCache::allocate").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::BaseKVCacheManager::addSequence").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::BaseKVCacheManager::removeSequence").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::BaseKVCacheManager::getCacheBlockIds").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::BaseKVCacheManager::getBatchCacheBlockIds").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::BlockManager::storeNewBlock").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::BlockManager::addSequence").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::BlockManager::removeSequence").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::HandleContextLogits::handle").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::HandleGenerationLogits::handle").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::utils::CudaGraphExecutor").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::utils::CudaGraphExecutorCache::get").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::DecoderInputBuffers::batchLogits").pointerTypes("Pointer"));

        // Skip the heavy transport inner ops (keep class stubs generated below)

        // Skip copy functions
        infoMap.put(new Info("copyDiskToDisk", "copyDiskToHost", "copyHostToDisk", "copyHostToHost",
                "copyHostToDevice", "copyDeviceToHost", "copyDeviceToDevice").skip());

        // StringPtrMap (TensorMap) -> skip
        infoMap.put(new Info("tensorrt_llm::runtime::StringPtrMap").pointerTypes("Pointer"));

        // nvinfer1::int in batch manager context -> int
        infoMap.put(new Info("nvinfer1::DataType").valueTypes("int").pointerTypes("IntPointer"));

        // GenericLlmRequest is a template - LlmRequest extends it, map to Pointer base
        infoMap.put(new Info("tensorrt_llm::batch_manager::GenericLlmRequest").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::LlmRequest").base("Pointer"));

        // runtime types used cross-package
        infoMap.put(new Info("tensorrt_llm::runtime::CudaStream").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::ITensor").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::IBuffer").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::ModelConfig").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::WorldConfig").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::TllmRuntime").pointerTypes("Pointer"));

        // BlocksPerWindow and other types that leak from kv_cache headers
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::BlocksPerWindow").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::TempAttentionWindowInputs").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::KVCacheBlock").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::BlockPtr").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::UnifiedBlockTree").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::KVBlockArray").pointerTypes("Pointer"));

        // Skip getCacheBlockIds/getBatchCacheBlockIds (returns nested std::vector)
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::KVCacheManager::getCacheBlockIds").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::KVCacheManager::getBatchCacheBlockIds").pointerTypes("Pointer"));

        // New headers additions - skip complex types
        infoMap.put(new Info("tensorrt_llm::batch_manager::eviction_policy::BaseEvictionPolicy").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::eviction_policy::LRUEvictionPolicy").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::eviction_policy::PriorityEvictionPolicy").pointerTypes("Pointer"));

        // RnnStateManager
        infoMap.put(new Info("tensorrt_llm::batch_manager::rnn_state_manager::RnnStateManager").pointerTypes("Pointer"));

        // PeftCacheManager types
        infoMap.put(new Info("tensorrt_llm::batch_manager::PeftTaskNotCachedException").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::WorkerPool").pointerTypes("Pointer"));

        // KVCacheEventManager - keep class, skip complex methods
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::KVCacheEventManager::getEvents").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::KVCacheEventManager::enqueueStoredEvent").pointerTypes("Pointer"));

        // Skip LogitsPostProcessor complex lambda members
        infoMap.put(new Info("tensorrt_llm::batch_manager::LogitsPostProcessor::LogitsPostProcessorBatched").pointerTypes("Pointer"));

        // MicroBatchScheduler
        infoMap.put(new Info("tensorrt_llm::batch_manager::batch_scheduler::ContextChunkingConfig").pointerTypes("Pointer"));

        // AllocateKvCache
        // (class emitted; constructor + method signatures kept)

        // Scheduling types
        // (classes emitted; keep only nested typedefs skipped above)

        // KvCacheConnector
        // (emitted)

        // StringSetTrie
        infoMap.put(new Info("tensorrt_llm::batch_manager::templated_trie::StringSet").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::templated_trie::Trie").pointerTypes("Pointer"));

        // RadixBlockTree
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::RadixBlockTree").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::PrefixKey").pointerTypes("Pointer"));

        // BlockKey types
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::BlockKey").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::generateBlockHashExtraKeys").pointerTypes("Pointer"));

        // ===== Unblock full class generation: remove Pointer-stubs, keep typedef skips =====
        // The following classes used to be skipped as Pointer, but we want their methods
        // exposed. Only skip TRULY template / unparseable members below.

        // Typedefs used inside BasePeftCacheManager / PeftCacheManager methods
        infoMap.put(new Info(
                "tensorrt_llm::batch_manager::BasePeftCacheManager::LlmRequestPtr",
                "tensorrt_llm::batch_manager::BasePeftCacheManager::RequestVector",
                "tensorrt_llm::batch_manager::BasePeftCacheManager::PeftTable",
                "tensorrt_llm::batch_manager::BasePeftCacheManager::TaskPeftTable",
                "tensorrt_llm::batch_manager::BasePeftCacheManager::TaskIdToReqIds",
                "tensorrt_llm::batch_manager::BasePeftCacheManager::EnsureBatchTaskResult",
                "tensorrt_llm::batch_manager::PeftCacheManager::LlmRequestPtr",
                "tensorrt_llm::batch_manager::PeftCacheManager::RequestVector",
                "tensorrt_llm::batch_manager::PeftCacheManager::PeftTable",
                "tensorrt_llm::batch_manager::PeftCacheManager::EnsureBatchTaskResult",
                "tensorrt_llm::batch_manager::RequestVector",
                "tensorrt_llm::batch_manager::RequestList",
                "tensorrt_llm::batch_manager::ScheduledRequests"
        ).pointerTypes("Pointer").cast());

        // Typedefs used inside MicroBatchScheduler / schedulers
        infoMap.put(new Info(
                "tensorrt_llm::batch_manager::batch_scheduler::ScheduledRequests",
                "tensorrt_llm::batch_manager::batch_scheduler::RequestVector",
                "tensorrt_llm::batch_manager::batch_scheduler::RequestList"
        ).pointerTypes("Pointer").cast());

        // Typedefs used inside createNewDecoderRequests / pauseRequests / updateDecoderBuffers
        infoMap.put(new Info(
                "tensorrt_llm::batch_manager::CreateNewDecoderRequests::RequestVector",
                "tensorrt_llm::batch_manager::UpdateDecoderBuffers::RequestVector",
                "tensorrt_llm::batch_manager::PauseRequests::RequestVector",
                "tensorrt_llm::batch_manager::AssignReqSeqSlots::RequestVector",
                "tensorrt_llm::batch_manager::HandleContextLogits::RequestVector",
                "tensorrt_llm::batch_manager::HandleGenerationLogits::RequestVector",
                "tensorrt_llm::batch_manager::MakeDecodingBatchInputOutput::RequestVector",
                "tensorrt_llm::batch_manager::AllocateKvCache::RequestVector",
                "tensorrt_llm::batch_manager::LogitsPostProcessor::RequestVector",
                "tensorrt_llm::batch_manager::GuidedDecoder::RequestVector"
        ).pointerTypes("Pointer").cast());

        // LoraCache::TaskLayerModuleConfig is a runtime nested class
        infoMap.put(new Info(
                "tensorrt_llm::runtime::LoraCache::TaskLayerModuleConfig",
                "tensorrt_llm::runtime::LoraCache::TaskLayerModuleConfigListPtr",
                "tensorrt_llm::runtime::LoraExpectedException"
        ).pointerTypes("Pointer"));

        // Remove earlier pointer-stubs so the classes get Java bodies
        infoMap.put(new Info("tensorrt_llm::batch_manager::BasePeftCacheManager").base("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::PeftCacheManager").base("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::NoPeftCacheManager").base("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::CreateNewDecoderRequests").base("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::MakeDecodingBatchInputOutput").base("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::UpdateDecoderBuffers").base("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::AssignReqSeqSlots").base("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::HandleContextLogits").base("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::HandleGenerationLogits").base("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::AllocateKvCache").base("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::PauseRequests").base("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::MicroBatchScheduler").base("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::batch_scheduler::MicroBatchScheduler").base("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::GuidedDecoder").base("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::LogitsPostProcessor").base("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::CacheTransceiver").base("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::BaseCacheTransceiver").base("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::CacheTransceiverFactory").base("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::KVCacheTransferManager").base("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::KvCacheConnector").base("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::KVCacheEventManager").base("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::rnn_state_manager::RnnStateManager").base("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::eviction_policy::BaseEvictionPolicy").base("Pointer"));
        infoMap.put(new Info("tensorrt_llm::batch_manager::eviction_policy::LRUEvictionPolicy").base("Pointer"));

        // BufferKind is an enum-like byte typedef used in BaseTransBufferManager hierarchy
        infoMap.put(new Info("tensorrt_llm::batch_manager::BufferKind").valueTypes("byte").pointerTypes("BytePointer").cast());

        // Skip move-constructor / operator= in generated transfer layer classes
        infoMap.put(new Info("tensorrt_llm::batch_manager::CacheTransferLayer::CacheTransferLayer(tensorrt_llm::batch_manager::CacheTransferLayer&&)").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::CacheTransferLayer::operator=").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::CacheFormatter::operator=").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::MLACacheFormatter::operator=").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::RnnCacheFormatter::operator=").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::CacheTransBufferManager::operator=").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::RnnCacheTransBufferManager::operator=").skip());

        // ===== Template specializations that leak into generated Java signatures =====
        infoMap.put(new Info(
                "std::vector<runtime::ITensor::SharedPtr>",
                "std::vector<tensorrt_llm::runtime::ITensor::SharedPtr>",
                "std::vector<tensorrt_llm::batch_manager::kv_cache_manager::GenerationRequest::SizeType32>",
                "std::vector<tensorrt_llm::batch_manager::PromptTuningBuffers::SizeType32>",
                "std::vector<std::vector<tensorrt_llm::batch_manager::PromptTuningBuffers::SizeType32> >",
                "std::vector<std::vector<tensorrt_llm::batch_manager::PromptTuningBuffers::SizeType32>>",
                "std::vector<tensorrt_llm::executor::AdditionalModelOutput>",
                "std::vector<tensorrt_llm::batch_manager::LlmRequest>",
                "std::vector<std::shared_ptr<tensorrt_llm::batch_manager::LlmRequest> >",
                "std::vector<std::shared_ptr<tensorrt_llm::batch_manager::LlmRequest>>",
                "std::list<std::shared_ptr<tensorrt_llm::batch_manager::LlmRequest> >",
                "std::list<std::shared_ptr<tensorrt_llm::batch_manager::LlmRequest>>",
                "std::unordered_map<uint64_t,std::vector<tensorrt_llm::runtime::LoraCache::TaskLayerModuleConfig> >",
                "std::unordered_map<uint64_t,std::vector<tensorrt_llm::runtime::LoraCache::TaskLayerModuleConfig>>"
        ).pointerTypes("Pointer").cast());
    }
}
