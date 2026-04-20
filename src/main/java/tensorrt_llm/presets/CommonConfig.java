package tensorrt_llm.presets;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;
import org.bytedeco.javacpp.tools.*;

@Properties(
    value = @Platform(
        includepath = {"/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include", "/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp", "/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/cuda-stubs"},
        include = {
            "tensorrt_llm/common/assert.h",
            "tensorrt_llm/common/config.h",
            "tensorrt_llm/common/envUtils.h",
            "tensorrt_llm/common/logger.h",
            "tensorrt_llm/common/quantization.h",
            "tensorrt_llm/common/stringUtils.h",
            "tensorrt_llm/common/tllmException.h",
            "tensorrt_llm/common/dataType.h",
            "tensorrt_llm/common/utils.h",
            "tensorrt_llm/common/algorithm.h"
        }
    ),
    target = "tensorrt_llm.common",
    global = "tensorrt_llm.global.Common"
)
public class CommonConfig implements InfoMapper {

    /** Shared InfoMap entries used across all preset configs */
    public static void mapCommonTypes(InfoMap infoMap) {
        // ===== Primitive type aliases =====
        infoMap.put(new Info("tensorrt_llm::runtime::SizeType32", "SizeType32", "runtime::SizeType32").valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("tensorrt_llm::runtime::TokenIdType", "TokenIdType").valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("std::int32_t", "int32_t").valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("std::int64_t", "int64_t").valueTypes("long").pointerTypes("LongPointer"));
        infoMap.put(new Info("std::uint32_t", "uint32_t").valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("std::uint64_t", "uint64_t").valueTypes("long").pointerTypes("LongPointer"));
        infoMap.put(new Info("std::size_t", "size_t").valueTypes("long").pointerTypes("LongPointer"));
        infoMap.put(new Info("std::ptrdiff_t", "ptrdiff_t").valueTypes("long").pointerTypes("LongPointer"));
        infoMap.put(new Info("bool").valueTypes("boolean").pointerTypes("BoolPointer"));
        infoMap.put(new Info("auto").cppTypes("int"));

        // ===== STL containers & constructs -> skip =====
        infoMap.put(new Info("std::optional").pointerTypes("Pointer"));
        infoMap.put(new Info("std::variant").pointerTypes("Pointer"));
        infoMap.put(new Info("std::function").pointerTypes("Pointer"));
        infoMap.put(new Info("std::promise").pointerTypes("Pointer"));
        infoMap.put(new Info("std::future").pointerTypes("Pointer"));
        infoMap.put(new Info("std::shared_ptr").annotations("@SharedPtr"));
        infoMap.put(new Info("std::unique_ptr").annotations("@UniquePtr"));
        infoMap.put(new Info("std::atomic").pointerTypes("Pointer"));
        infoMap.put(new Info("std::mutex").pointerTypes("Pointer"));
        infoMap.put(new Info("std::recursive_mutex").pointerTypes("Pointer"));
        infoMap.put(new Info("std::condition_variable").pointerTypes("Pointer"));
        infoMap.put(new Info("std::thread").pointerTypes("Pointer"));
        infoMap.put(new Info("std::unordered_map").pointerTypes("Pointer"));
        infoMap.put(new Info("std::map").pointerTypes("Pointer"));
        infoMap.put(new Info("std::multimap").pointerTypes("Pointer"));
        infoMap.put(new Info("std::tuple").pointerTypes("Pointer"));
        infoMap.put(new Info("std::pair").pointerTypes("Pointer"));
        infoMap.put(new Info("std::array").pointerTypes("Pointer"));
        infoMap.put(new Info("std::set").pointerTypes("Pointer"));
        infoMap.put(new Info("std::unordered_set").pointerTypes("Pointer"));
        infoMap.put(new Info("std::deque").pointerTypes("Pointer"));
        infoMap.put(new Info("std::list").pointerTypes("Pointer"));
        infoMap.put(new Info("std::queue").pointerTypes("Pointer"));
        infoMap.put(new Info("std::vector").pointerTypes("Pointer"));
        infoMap.put(new Info("std::initializer_list").pointerTypes("Pointer"));
        infoMap.put(new Info("std::enable_shared_from_this").pointerTypes("Pointer"));
        infoMap.put(new Info("std::reference_wrapper").pointerTypes("Pointer"));
        infoMap.put(new Info("std::bitset").pointerTypes("Pointer"));
        infoMap.put(new Info("std::basic_string_view").pointerTypes("Pointer"));
        infoMap.put(new Info("std::string_view").pointerTypes("Pointer"));
        infoMap.put(new Info("std::runtime_error").pointerTypes("Pointer"));
        infoMap.put(new Info("std::exception").pointerTypes("Pointer"));
        infoMap.put(new Info("std::logic_error").pointerTypes("Pointer"));
        infoMap.put(new Info("std::chrono").pointerTypes("Pointer"));
        infoMap.put(new Info("std::chrono::milliseconds").pointerTypes("Pointer"));
        infoMap.put(new Info("std::chrono::time_point").pointerTypes("Pointer"));
        infoMap.put(new Info("std::chrono::steady_clock").pointerTypes("Pointer"));
        infoMap.put(new Info("std::chrono::duration").pointerTypes("Pointer"));
        infoMap.put(new Info("std::filesystem").pointerTypes("Pointer"));
        infoMap.put(new Info("std::filesystem::path").pointerTypes("Pointer"));
        infoMap.put(new Info("std::remove_cv_t").pointerTypes("Pointer"));
        infoMap.put(new Info("std::underlying_type_t").pointerTypes("Pointer"));
        infoMap.put(new Info("std::numeric_limits").pointerTypes("Pointer"));

        // ===== Macros =====
        infoMap.put(new Info("TRTLLM_NAMESPACE_BEGIN").cppText("#define TRTLLM_NAMESPACE_BEGIN namespace tensorrt_llm {"));
        infoMap.put(new Info("TRTLLM_NAMESPACE_END").cppText("#define TRTLLM_NAMESPACE_END }"));
        infoMap.put(new Info("TRTLLM_ABI_NAMESPACE_BEGIN").cppText("#define TRTLLM_ABI_NAMESPACE_BEGIN"));
        infoMap.put(new Info("TRTLLM_ABI_NAMESPACE_END").cppText("#define TRTLLM_ABI_NAMESPACE_END"));
        infoMap.put(new Info("TRTLLM_ABI_NAMESPACE").cppText("#define TRTLLM_ABI_NAMESPACE"));
        infoMap.put(new Info("TRTLLM_CHECK", "TRTLLM_CHECK_WITH_INFO", "TRTLLM_CHECK_DEBUG", "TRTLLM_LOG_TRACE",
                "TRTLLM_LOG_DEBUG", "TRTLLM_LOG_INFO", "TRTLLM_LOG_WARNING", "TRTLLM_LOG_ERROR",
                "TRTLLM_LOG_EXCEPTION", "TRTLLM_THROW", "FMT_DIM", "LIKELY", "UNLIKELY").cppTypes().annotations());
        infoMap.put(new Info("ENABLE_BF16").define(true));
        infoMap.put(new Info("ENABLE_FP8").define(true));
        infoMap.put(new Info("ENABLE_MULTI_DEVICE").define(true));
        infoMap.put(new Info("[[nodiscard]]", "[[maybe_unused]]", "[[deprecated]]").cppTypes().annotations());

        // ===== CUDA attributes =====
        infoMap.put(new Info("__host__", "__device__", "__forceinline__", "__global__",
                "__launch_bounds__", "__align__", "__inline__", "__restrict__",
                "__constant__", "__shared__").cppTypes().annotations());

        // ===== nvinfer1 types =====
        infoMap.put(new Info("nvinfer1::DataType").enumerate());
        infoMap.put(new Info("nvinfer1::Dims").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::ILogger").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::ILogger::Severity").enumerate());
        infoMap.put(new Info("nvinfer1::IRuntime").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::ICudaEngine").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::IExecutionContext").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::IHostMemory").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::Weights").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::IProfiler").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::IPluginV2").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::IPluginV2Ext").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::IPluginV2IOExt").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::IPluginV2DynamicExt").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::IPluginV3").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::IPluginV3OneCore").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::IPluginV3OneBuild").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::IPluginV3OneRuntime").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::IPluginCapability").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::IPluginResource").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::PersistentWorkspaceInterface").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::IPluginCreator").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::IPluginCreatorV3One").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::PluginFieldCollection").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::PluginField").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::PluginTensorDesc").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::DynamicPluginTensorDesc").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::DimsExprs").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::IExprBuilder").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::IDimensionExpr").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::TensorFormat").enumerate());
        infoMap.put(new Info("nvinfer1::PluginFieldType").enumerate());
        infoMap.put(new Info("nvinfer1::TensorIOMode").enumerate());
        infoMap.put(new Info("nvinfer1::IStreamReaderV2").pointerTypes("Pointer"));
        infoMap.put(new Info("nvinfer1::IStreamReaderV2::SeekPosition").enumerate());

        // ===== CUDA runtime types =====
        infoMap.put(new Info("cudaStream_t").valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("cudaEvent_t").valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("CUmemPoolHandle_st").pointerTypes("Pointer"));
        infoMap.put(new Info("cudaMemPool_t").valueTypes("Pointer").pointerTypes("Pointer"));

        // ===== CUDA driver types =====
        infoMap.put(new Info("CUdeviceptr").valueTypes("long").pointerTypes("LongPointer"));
        infoMap.put(new Info("CUdevice").valueTypes("int").pointerTypes("IntPointer"));
        infoMap.put(new Info("CUcontext").valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("CUmodule").valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("CUfunction").valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("CUstream").valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("CUevent").valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("CUmemGenericAllocationHandle").valueTypes("long").pointerTypes("LongPointer"));
        infoMap.put(new Info("CUmemAccessDesc").pointerTypes("Pointer"));
        infoMap.put(new Info("CUmemAllocationProp").pointerTypes("Pointer"));
        infoMap.put(new Info("CUmemAllocationHandleType").valueTypes("int"));
        infoMap.put(new Info("CUresult").valueTypes("int"));
        infoMap.put(new Info("curandState_t", "curandState").pointerTypes("Pointer"));

        // ===== NCCL types =====
        infoMap.put(new Info("ncclComm_t").valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("ncclDataType_t").valueTypes("int"));
        infoMap.put(new Info("ncclRedOp_t").valueTypes("int"));
        infoMap.put(new Info("ncclResult_t").valueTypes("int"));

        // ===== MPI types =====
        infoMap.put(new Info("MPI_Comm").valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("MPI_Datatype").valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("MPI_Op").valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("MPI_Request").valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("MPI_Status").pointerTypes("Pointer"));
        infoMap.put(new Info("OMPI_COMM_WORLD").pointerTypes("Pointer"));

        // ===== Torch / ATen types =====
        infoMap.put(new Info("at::Tensor", "torch::Tensor", "th::Tensor").pointerTypes("Pointer"));
        infoMap.put(new Info("c10::ScalarType").valueTypes("int"));
        infoMap.put(new Info("c10::Device").pointerTypes("Pointer"));
        infoMap.put(new Info("at::cuda::CUDAStream").pointerTypes("Pointer"));
        infoMap.put(new Info("c10::intrusive_ptr").pointerTypes("Pointer"));
        infoMap.put(new Info("c10d::ProcessGroup").pointerTypes("Pointer"));
        infoMap.put(new Info("torch::jit::CustomClassHolder", "torch::CustomClassHolder").pointerTypes("Pointer"));

        // ===== NVML types =====
        infoMap.put(new Info("nvmlDevice_t").valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("nvmlReturn_t").valueTypes("int"));

        // ===== GDR types =====
        infoMap.put(new Info("gdr_t").valueTypes("Pointer").pointerTypes("Pointer"));
        infoMap.put(new Info("gdr_mh_t", "gdr_info_t").pointerTypes("Pointer"));

        // ===== nlohmann JSON =====
        infoMap.put(new Info("nlohmann::json", "nlohmann::ordered_json").pointerTypes("Pointer"));

        // ===== Skip problematic global functions =====
        infoMap.put(new Info("tensorrt_llm::common::getDTypeSize", "tensorrt_llm::common::getDTypeSizeInBits",
                "tensorrt_llm::common::getDtypeString").skip());

        // Skip operator<< for CUDA types that produce invalid Java
        infoMap.put(new Info("tensorrt_llm::common::operator <<").pointerTypes("Pointer"));
        infoMap.put(new Info("std::basic_ostream").pointerTypes("Pointer"));
        infoMap.put(new Info("__nv_bfloat16", "__nv_bfloat162").pointerTypes("Pointer"));
        infoMap.put(new Info("__half", "__half2").pointerTypes("Pointer"));

        // Skip cudaUtils complex template types
        infoMap.put(new Info("tensorrt_llm::common::packed_type").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::common::num_elems").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::common::TRTLLMCudaDataType").enumerate());
        infoMap.put(new Info("tensorrt_llm::common::CublasDataType").enumerate());
        infoMap.put(new Info("tensorrt_llm::common::OperationType").enumerate());
    }

    @Override
    public void map(InfoMap infoMap) {
        mapCommonTypes(infoMap);

        // Common-specific: skip heavy headers that leak too many types
        infoMap.put(new Info("tensorrt_llm::common::CUDADriverWrapper").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::common::cublasMMWrapper").pointerTypes("Pointer"));
        infoMap.put(new Info("tensorrt_llm::common::cublasAlgoMap").pointerTypes("Pointer"));

        // Skip array-based types
        infoMap.put(new Info("tensorrt_llm::common::ArrayView").pointerTypes("Pointer"));
    }
}
