package tensorrt_llm.presets;

import org.bytedeco.javacpp.annotation.Platform;
import org.bytedeco.javacpp.annotation.Properties;

@Properties(
        value = @Platform(
                include = {
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
        inherit = TRTLLMFullConfig.class,
        target = "org.bytedeco.tensorrt_llm.batch_manager",
        global = "org.bytedeco.tensorrt_llm.global.TRTLLM"
)
public class BatchManagerConfig extends TRTLLMFullConfig {
}

