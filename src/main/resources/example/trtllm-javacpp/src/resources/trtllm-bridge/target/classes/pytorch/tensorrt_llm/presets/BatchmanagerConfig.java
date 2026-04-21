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
            "tensorrt_llm/batch_manager/capacityScheduler.h",
            "tensorrt_llm/batch_manager/contextProgress.h",
            "tensorrt_llm/batch_manager/decoderBuffers.h",
            "tensorrt_llm/batch_manager/encoderBuffers.h",
            "tensorrt_llm/batch_manager/loraBuffers.h",
            "tensorrt_llm/batch_manager/medusaBuffers.h",
            "tensorrt_llm/batch_manager/pauseRequests.h",
            "tensorrt_llm/batch_manager/peftCacheManagerConfig.h",
            "tensorrt_llm/batch_manager/promptTuningBuffers.h",
            "tensorrt_llm/batch_manager/rnnStateBuffers.h",
            "tensorrt_llm/batch_manager/runtimeBuffers.h",
            "tensorrt_llm/batch_manager/sequenceSlotManager.h",
            "tensorrt_llm/batch_manager/transformerBuffers.h",
            "tensorrt_llm/batch_manager/trtGptModel.h"
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
        infoMap.put(new Info("tensorrt_llm::executor::MmKey").skip());

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
        infoMap.put(new Info("tensorrt_llm::executor::AdditionalModelOutput").skip());
        infoMap.put(new Info("tensorrt_llm::executor::DebugConfig").skip());
        infoMap.put(new Info("tensorrt_llm::executor::Request").skip());
        infoMap.put(new Info("tensorrt_llm::executor::Result").skip());
        infoMap.put(new Info("tensorrt_llm::executor::Response").skip());
        infoMap.put(new Info("tensorrt_llm::executor::Shape").skip());
        infoMap.put(new Info("tensorrt_llm::executor::Tensor").skip());

        // ===== Batch manager specific skips =====
        // Skip template types
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager_v2::Task").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager_v2::DiskAddress").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager_v2::MemAddress").skip());

        // Skip OptionalRef template types
        infoMap.put(new Info("tensorrt_llm::common::OptionalRef").skip());
        infoMap.put(new Info("OptionalRef").skip());

        // Skip methods that use OptionalRef parameters
        infoMap.put(new Info("tensorrt_llm::batch_manager::AllocateKvCache::allocate").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::BaseKVCacheManager::addSequence").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::BaseKVCacheManager::removeSequence").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::BaseKVCacheManager::getCacheBlockIds").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::BaseKVCacheManager::getBatchCacheBlockIds").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::BlockManager::storeNewBlock").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::BlockManager::addSequence").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::kv_cache_manager::BlockManager::removeSequence").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::HandleContextLogits::handle").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::HandleGenerationLogits::handle").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::utils::CudaGraphExecutor").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::utils::CudaGraphExecutorCache::get").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::DecoderInputBuffers::batchLogits").skip());

        // Skip CacheReceiver/CacheSender/transfer classes
        infoMap.put(new Info("tensorrt_llm::batch_manager::CacheReceiver").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::CacheSender").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::CacheTransceiverComm").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::CacheTransceiver").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::CacheTransceiverFactory").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::CacheTransBufferManager").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::RnnCacheTransBufferManager").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::BaseCacheTransceiver").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::CacheFormatter").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::MlaCacheFormatter").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::RnnCacheFormatter").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::KVCacheTransferManager").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::KvCacheConnector").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::KVCacheEventManager").skip());

        // Skip heavy inflight batching / model classes
        infoMap.put(new Info("tensorrt_llm::batch_manager::TrtGptModelInflightBatching").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::TrtGptModelFactory").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::TrtEncoderModel").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::MicroBatchScheduler").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::DataTransceiver").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::GuidedDecoder").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::RequestStatuses").skip());
        infoMap.put(new Info("tensorrt_llm::batch_manager::LogitsPostProcessor").skip());

        // Skip copy functions
        infoMap.put(new Info("copyDiskToDisk", "copyDiskToHost", "copyHostToDisk", "copyHostToHost",
                "copyHostToDevice", "copyDeviceToHost", "copyDeviceToDevice").skip());

        // StringPtrMap (TensorMap) -> skip
        infoMap.put(new Info("tensorrt_llm::runtime::StringPtrMap").skip());

        // nvinfer1::DataType in batch manager context -> int
        infoMap.put(new Info("nvinfer1::DataType").valueTypes("int").pointerTypes("IntPointer"));
    }
}
