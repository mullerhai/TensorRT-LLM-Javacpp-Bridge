#!/usr/bin/env python3
"""Create minimal JavaCPP wrapper classes for each C++ typedef / opaque type
still referenced by the newly-imported bindings but not yet materialised.

Each wrapper is a thin ``extends Pointer`` (or ``extends LongPointer`` /
``IntPointer`` for integer typedefs) with a ``@Name`` annotation that binds
it to the real C++ type.  This is enough for ``javac`` to accept the
signatures while JavaCPP still produces correct native glue."""
import os
from typing import Dict

ROOT = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(ROOT, "src", "main", "java", "tensorrt_llm")

# Maps (package_subdir, simple_name) -> (cpp_name, parent_java_class)
WRAPPERS: Dict[tuple[str, str], tuple[str, str]] = {
    # --- batch_manager ------------------------------------------------------
    ("batch_manager", "IdType"): (
        "tensorrt_llm::batch_manager::kv_cache_manager::KVCacheBlock::IdType",
        "IntPointer",
    ),
    ("batch_manager", "LoraTaskIdType"): (
        "tensorrt_llm::runtime::LoraTaskIdType", "LongPointer"),
    ("batch_manager", "RequestIdType"): (
        "tensorrt_llm::batch_manager::RequestIdType", "LongPointer"),
    ("batch_manager", "CacheSaltIDType"): (
        "tensorrt_llm::batch_manager::CacheSaltIDType", "LongPointer"),
    ("batch_manager", "IterationType"): (
        "tensorrt_llm::batch_manager::IterationType", "LongPointer"),
    ("batch_manager", "VecUniqueTokens"): (
        "tensorrt_llm::runtime::VecUniqueTokens", "Pointer"),
    ("batch_manager", "RequestList"): (
        "tensorrt_llm::batch_manager::RequestList", "Pointer"),
    ("batch_manager", "BlockPtr"): (
        "tensorrt_llm::batch_manager::kv_cache_manager::BlockPtr", "Pointer"),
    ("batch_manager", "CudaStreamPtr"): (
        "tensorrt_llm::runtime::CudaStreamPtr", "Pointer"),
    ("batch_manager", "ContextChunkingPolicy"): (
        "tensorrt_llm::batch_manager::ContextChunkingPolicy", "IntPointer"),
    ("batch_manager", "FinishReason"): (
        "tensorrt_llm::executor::FinishReason", "IntPointer"),
    ("batch_manager", "MpiType"): (
        "tensorrt_llm::mpi::MpiType", "IntPointer"),
    ("batch_manager", "RetentionPriority"): (
        "tensorrt_llm::executor::RetentionPriority", "IntPointer"),
    ("batch_manager", "TrtGptModelType"): (
        "tensorrt_llm::batch_manager::TrtGptModelType", "IntPointer"),
    ("batch_manager", "LogitsPostProcessorBatched"): (
        "tensorrt_llm::batch_manager::LogitsPostProcessorBatched", "Pointer"),
    ("batch_manager", "path"): ("std::filesystem::path", "Pointer"),
    ("batch_manager", "milliseconds"): (
        "std::chrono::milliseconds", "LongPointer"),
    ("batch_manager", "duration"): (
        "std::chrono::duration<int64_t>", "LongPointer"),
    ("batch_manager", "mode_t"): ("mode_t", "IntPointer"),

    # --- executor -----------------------------------------------------------
    ("executor", "MpiType"): ("tensorrt_llm::mpi::MpiType", "IntPointer"),
    ("executor", "MpiMessageData"): (
        "tensorrt_llm::executor::MpiMessageData", "Pointer"),
    ("executor", "ConnectionInfoType"): (
        "tensorrt_llm::executor::ConnectionInfoType", "Pointer"),
    ("executor", "SyncMessage"): (
        "tensorrt_llm::executor::SyncMessage", "Pointer"),
    ("executor", "RegisterDescs"): (
        "tensorrt_llm::executor::RegisterDescs", "Pointer"),
    ("executor", "TransferState"): (
        "tensorrt_llm::executor::TransferState", "Pointer"),
    ("executor", "TransferOp"): (
        "tensorrt_llm::executor::TransferOp", "IntPointer"),
    ("executor", "transfer_engine_t"): (
        "mooncake::transfer_engine_t", "Pointer"),
    ("executor", "nixl_xfer_dlist_t"): ("nixl_xfer_dlist_t", "Pointer"),
    ("executor", "nixl_reg_dlist_t"): ("nixl_reg_dlist_t", "Pointer"),
    ("executor", "nixl_xfer_op_t"): ("nixl_xfer_op_t", "IntPointer"),
    ("executor", "nixl_mem_t"): ("nixl_mem_t", "IntPointer"),
    ("executor", "nixl_opt_args_t"): ("nixl_opt_args_t", "Pointer"),
    ("executor", "nixlBasicDesc"): ("nixlBasicDesc", "Pointer"),
    ("executor", "nixlAgent"): ("nixlAgent", "Pointer"),
    ("executor", "nixlXferReqH"): ("nixlXferReqH", "Pointer"),
    ("executor", "Endpoint"): ("mooncake::Endpoint", "Pointer"),
    ("executor", "TransferDescs"): (
        "mooncake::TransferDescs", "Pointer"),
    ("executor", "TimePoint"): (
        "std::chrono::system_clock::time_point", "Pointer"),
    ("executor", "time_point"): (
        "std::chrono::system_clock::time_point", "Pointer"),
    ("executor", "mode_t"): ("mode_t", "IntPointer"),
    ("executor", "path"): ("std::filesystem::path", "Pointer"),
    ("executor", "milliseconds"): (
        "std::chrono::milliseconds", "LongPointer"),

    # --- plugins (TensorRT nvinfer1 interfaces) -----------------------------
    ("plugins", "IPluginV2"): ("nvinfer1::IPluginV2", "Pointer"),
    ("plugins", "IPluginV2DynamicExt"): (
        "nvinfer1::IPluginV2DynamicExt", "Pointer"),
    ("plugins", "IPluginV3"): ("nvinfer1::IPluginV3", "Pointer"),
    ("plugins", "IPluginV3OneBuild"): (
        "nvinfer1::v_1_0::IPluginV3OneBuild", "Pointer"),
    ("plugins", "IPluginV3OneCore"): (
        "nvinfer1::v_1_0::IPluginV3OneCore", "Pointer"),
    ("plugins", "IPluginV3OneRuntime"): (
        "nvinfer1::v_1_0::IPluginV3OneRuntime", "Pointer"),
    ("plugins", "IPluginCapability"): (
        "nvinfer1::IPluginCapability", "Pointer"),
    ("plugins", "PluginCapabilityType"): (
        "nvinfer1::PluginCapabilityType", "IntPointer"),
    ("plugins", "PluginTensorDesc"): (
        "nvinfer1::PluginTensorDesc", "Pointer"),
    ("plugins", "DynamicPluginTensorDesc"): (
        "nvinfer1::DynamicPluginTensorDesc", "Pointer"),
    ("plugins", "PluginFieldCollection"): (
        "nvinfer1::PluginFieldCollection", "Pointer"),
    ("plugins", "DimsExprs"): ("nvinfer1::DimsExprs", "Pointer"),
    ("plugins", "IExprBuilder"): ("nvinfer1::IExprBuilder", "Pointer"),
    ("plugins", "ContextFMHAType"): (
        "tensorrt_llm::plugins::ContextFMHAType", "IntPointer"),
    ("plugins", "SharedConstPtr"): (
        "tensorrt_llm::runtime::ITensor::SharedConstPtr", "Pointer"),
    ("plugins", "ITensorPtr"): (
        "tensorrt_llm::runtime::ITensor::SharedPtr", "Pointer"),

    # --- plugins: TRT-LLM + nvinfer1 enums / structs -----------------------
    ("plugins", "DimType64"): ("tensorrt_llm::plugins::DimType64", "LongPointer"),
    ("plugins", "SizeType32"): ("tensorrt_llm::runtime::SizeType32", "IntPointer"),
    ("plugins", "TensorRTPhase"): ("nvinfer1::TensorRTPhase", "IntPointer"),
    ("plugins", "ActivationType"): (
        "tensorrt_llm::ActivationType", "IntPointer"),
    ("plugins", "AttentionMaskType"): (
        "tensorrt_llm::kernels::AttentionMaskType", "IntPointer"),
    ("plugins", "AttentionOp"): (
        "tensorrt_llm::kernels::AttentionOp", "Pointer"),
    ("plugins", "BlockSparseParams"): (
        "tensorrt_llm::kernels::BlockSparseParams", "Pointer"),
    ("plugins", "PositionEmbeddingType"): (
        "tensorrt_llm::kernels::PositionEmbeddingType", "IntPointer"),
    ("plugins", "RotaryScalingType"): (
        "tensorrt_llm::kernels::RotaryScalingType", "IntPointer"),
    ("plugins", "KernelType"): (
        "tensorrt_llm::plugins::KernelType", "IntPointer"),
    ("plugins", "GemmToProfile"): (
        "tensorrt_llm::plugins::GemmPluginProfiler::GemmToProfile", "IntPointer"),
    ("plugins", "MOEParallelismConfig"): (
        "tensorrt_llm::kernels::MOEParallelismConfig", "Pointer"),
    ("plugins", "PersistentWorkspaceInterface"): (
        "tensorrt_llm::plugins::PersistentWorkspaceInterface", "Pointer"),
    ("plugins", "IPluginResource"): (
        "nvinfer1::IPluginResource", "Pointer"),
    ("plugins", "IPluginResourceContext"): (
        "nvinfer1::IPluginResourceContext", "Pointer"),

    # --- executor spill-over -----------------------------------------------
    ("executor", "IPluginResourceContext"): (
        "nvinfer1::IPluginResourceContext", "Pointer"),

    # --- runtime: CUDA driver + TRT stream-reader + MOE -------------------
    ("runtime", "CUdeviceptr"): ("CUdeviceptr", "LongPointer"),
    ("runtime", "CUstream"): ("CUstream", "Pointer"),
    ("runtime", "CUmemGenericAllocationHandle"): (
        "CUmemGenericAllocationHandle", "LongPointer"),
    ("runtime", "IStreamReaderV2"): (
        "nvinfer1::v_2_0::IStreamReaderV2", "Pointer"),
    ("runtime", "SeekPosition"): ("nvinfer1::SeekPosition", "IntPointer"),
    ("runtime", "ScalarType"): ("c10::ScalarType", "IntPointer"),
    ("runtime", "path"): ("std::filesystem::path", "Pointer"),
    ("runtime", "MoeLoadBalanceMetaInfo"): (
        "tensorrt_llm::runtime::MoeLoadBalanceMetaInfo", "Pointer"),
    ("runtime", "MoeLoadBalanceStatisticInfo"): (
        "tensorrt_llm::runtime::MoeLoadBalanceStatisticInfo", "Pointer"),
    ("runtime", "MoeLoadBalanceSingleLayerSignal"): (
        "tensorrt_llm::runtime::MoeLoadBalanceSingleLayerSignal", "Pointer"),
    ("runtime", "MoePlacementInfo"): (
        "tensorrt_llm::runtime::MoePlacementInfo", "Pointer"),

    # --- runtime: extra CUDA + torch handles -------------------------------
    ("runtime", "CUdevice"): ("CUdevice", "IntPointer"),
    ("runtime", "CUmemAccessDesc"): ("CUmemAccessDesc", "Pointer"),
    ("runtime", "CUDAStream"): ("c10::cuda::CUDAStream", "Pointer"),
    ("runtime", "Device"): ("c10::Device", "Pointer"),
    ("runtime", "DeviceType"): ("c10::DeviceType", "IntPointer"),
    ("runtime", "IntArrayRef"): ("c10::IntArrayRef", "Pointer"),
    ("runtime", "IStreamReader"): ("nvinfer1::IStreamReader", "Pointer"),
    ("runtime", "value_type"): ("size_t", "LongPointer"),
    ("executor", "CUdevice"): ("CUdevice", "IntPointer"),
}

PRESET_BY_PKG = {
    "batch_manager": "tensorrt_llm.presets.BatchmanagerConfig",
    "executor": "tensorrt_llm.presets.ExecutorConfig",
    "runtime": "tensorrt_llm.presets.RuntimeConfig",
    "common": "tensorrt_llm.presets.CommonConfig",
    "layers": "tensorrt_llm.presets.LayersConfig",
    "plugins": "tensorrt_llm.presets.PluginsConfig",
}

TEMPLATE = """\
// Auto-generated JavaCPP wrapper for the C++ typedef ``{cpp_name}``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.{pkg};

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("{cpp_name}")
@Properties(inherit = {preset}.class)
public class {simple} extends {parent} {{
    static {{ Loader.load(); }}

    public {simple}() {{ super((Pointer) null); allocate(); }}
    public {simple}(Pointer p) {{ super(p); }}
    private native void allocate();
}}
"""


def main() -> None:
    written = 0
    skipped = 0
    for (pkg, simple), (cpp_name, parent) in WRAPPERS.items():
        target_dir = os.path.join(SRC, pkg)
        os.makedirs(target_dir, exist_ok=True)
        path = os.path.join(target_dir, f"{simple}.java")
        if os.path.exists(path):
            skipped += 1
            continue
        with open(path, "w", encoding="utf-8") as fp:
            fp.write(TEMPLATE.format(
                cpp_name=cpp_name,
                pkg=pkg,
                simple=simple,
                parent=parent,
                preset=PRESET_BY_PKG[pkg],
            ))
        written += 1
    print(f"wrote {written} wrapper classes ({skipped} already existed)")


if __name__ == "__main__":
    main()

