#!/usr/bin/env python3
"""
fix_pointer_casts.py

Post-processor that repairs JavaCPP-generated @Cast(...) annotations where the
literal Java class name ``Pointer`` leaked into the C++ cast string (e.g.
``@Cast("const Pointer*")``).  This happens when InfoMap entries map a C++
typedef with ``.pointerTypes("Pointer")`` without providing a matching
``.cast()`` qualified name.

The script keeps the Java-side type as ``Pointer`` (so no new Java wrappers are
required) but rewrites the C++ cast string so the native ABI matches the real
C++ signature (e.g. ``tensorrt_llm::batch_manager::RequestVector const&`` for
``contextRequests``).

Usage:
    python3 fix_pointer_casts.py [--check]
"""
from __future__ import annotations

import argparse
import os
import re
import sys
from typing import Dict, Tuple, List

ROOT = os.path.dirname(os.path.abspath(__file__))
SRC_DIR = os.path.join(ROOT, "src", "main", "java", "tensorrt_llm")

# ---------------------------------------------------------------------------
# Context-aware replacement table.
#
# Keys are *identifier tokens* that follow the broken `@Cast(...)` annotation.
# A broken token is one of:
#   - ``Pointer*``                              (scalar ref/ptr to a typedef)
#   - ``std::vector<Pointer*``                  (vector of a typedef)
#   - ``std::vector<std::vector<Pointer*``      (nested vector)
# The literal ``Pointer`` is replaced with the real C++ qualified type name.
#
# ``file_overrides`` let us disambiguate when the same identifier has different
# meanings in different files (e.g. ``dims`` in ITensor vs. elsewhere).
# ---------------------------------------------------------------------------

# Default mapping keyed by *Java identifier* (parameter name, method name,
# field name, or member-getter name).  Value is the canonical C++ type that
# should replace the literal ``Pointer`` inside the cast string.
DEFAULT_BY_NAME: Dict[str, str] = {
    # --- RequestVector ------------------------------------------------------
    "contextRequests": "tensorrt_llm::batch_manager::RequestVector",
    "generationRequests": "tensorrt_llm::batch_manager::RequestVector",
    "genRequests": "tensorrt_llm::batch_manager::RequestVector",
    "activeRequests": "tensorrt_llm::batch_manager::RequestVector",
    "ctxRequests": "tensorrt_llm::batch_manager::RequestVector",
    "finishedContextRequests": "tensorrt_llm::batch_manager::RequestVector",
    "requests": "tensorrt_llm::batch_manager::RequestVector",

    # --- Shape (nvinfer1::Dims) --------------------------------------------
    # ITensor.java only – handled through file_overrides, but kept here too
    # as harmless fallback for dim-like names.
    "dims": "tensorrt_llm::runtime::ITensor::Shape",
    "shape": "tensorrt_llm::runtime::ITensor::Shape",
    "offsetDims": "tensorrt_llm::runtime::ITensor::Shape",

    # --- PEFT / LoRA --------------------------------------------------------
    "peftTable": "tensorrt_llm::runtime::LoraManager::PeftTable",

    # --- Tensor maps --------------------------------------------------------
    "tensorMap": "tensorrt_llm::runtime::ITensor::TensorMap",

    # --- Runtime configs ----------------------------------------------------
    "modelConfig": "tensorrt_llm::runtime::ModelConfig",
    "worldConfig": "tensorrt_llm::runtime::WorldConfig",
    "runtime": "tensorrt_llm::runtime::TllmRuntime",
    "runtimeStream": "tensorrt_llm::runtime::CudaStreamPtr",
    "decoderStream": "tensorrt_llm::runtime::CudaStreamPtr",

    # --- KV cache -----------------------------------------------------------
    "lastBlockKey": "tensorrt_llm::batch_manager::kv_cache_manager::BlockKey",
    "getLastBlockKey": "tensorrt_llm::batch_manager::kv_cache_manager::BlockKey",

    # --- Lookahead / tokens (TensorConstPtr leaks) --------------------------
    "prompt": "tensorrt_llm::runtime::ITensor::SharedConstPtr",
    "generatedTokens": "tensorrt_llm::runtime::ITensor::SharedConstPtr",
    "sampledTokens": "tensorrt_llm::runtime::ITensor::SharedConstPtr",
    "endToken": "tensorrt_llm::runtime::ITensor::SharedConstPtr",
    "lastPositionIdPtr": "tensorrt_llm::runtime::ITensor::SharedConstPtr",
    "lastTokenPtr": "tensorrt_llm::runtime::ITensor::SharedConstPtr",
    "posIds": "tensorrt_llm::runtime::ITensor::SharedConstPtr",
    "keyTokens": "tensorrt_llm::runtime::ITensor::SharedConstPtr",
    "ngramTokens": "tensorrt_llm::runtime::ITensor::SharedConstPtr",

    # --- DebugConfig / StringVec -------------------------------------------
    "debugTensorNames": "tensorrt_llm::executor::StringVec",
    "getDebugTensorNames": "tensorrt_llm::executor::StringVec",
    "setDebugTensorNames": "tensorrt_llm::executor::StringVec",

    # --- executor collections ----------------------------------------------
    "additionalModelOutputs": "tensorrt_llm::executor::AdditionalModelOutput",
    "getAdditionalModelOutputs": "tensorrt_llm::executor::AdditionalModelOutput",
    "setAdditionalModelOutputs": "tensorrt_llm::executor::AdditionalModelOutput",
    "stopTokenIds": "tensorrt_llm::executor::TokenIdType",
    "getStopTokenIds": "tensorrt_llm::executor::TokenIdType",
    "setStopTokenIds": "tensorrt_llm::executor::TokenIdType",
    "getKVCacheEventManager": "std::shared_ptr<tensorrt_llm::batch_manager::kv_cache_manager::KVCacheEventManager>",

    # --- GenerationRequest.getCacheBlockIds --------------------------------
    "getCacheBlockIds": "tensorrt_llm::runtime::SizeType32",

    # --- PromptTuningBuffers -----------------------------------------------
    "mChunkPtableBufferStartPositions": "tensorrt_llm::runtime::SizeType32",
}

# Per-file overrides (take priority over DEFAULT_BY_NAME).
FILE_OVERRIDES: Dict[str, Dict[str, str]] = {
    "runtime/ITensor.java": {
        "other": "tensorrt_llm::runtime::ITensor::Shape",
        "lhs": "tensorrt_llm::runtime::ITensor::Shape",
        "rhs": "tensorrt_llm::runtime::ITensor::Shape",
        "getShape": "tensorrt_llm::runtime::ITensor::Shape",
    },
    "runtime/TllmRuntime.java": {
        "tensorMap": "tensorrt_llm::runtime::ITensor::TensorMap",
    },
    "runtime/LoraManager.java": {
        "peftTable": "tensorrt_llm::runtime::LoraManager::PeftTable",
    },
    "batch_manager/CreateNewDecoderRequests.java": {
        "inputIds": "tensorrt_llm::runtime::ITensor::SharedPtr",
    },
    "batch_manager/MicroBatchScheduler.java": {
        # `inflightReqIds` is actually a ReqIdsSet, not RequestVector.
        "inflightReqIds": "tensorrt_llm::batch_manager::ReqIdsSet",
    },
    "batch_manager/LoraBuffers.java": {
        "peftTable": "tensorrt_llm::runtime::LoraManager::PeftTable",
    },
    "batch_manager/DecoderSlotAsyncSend.java": {
        "outputIds": "tensorrt_llm::runtime::ITensor::SharedPtr",
        "sequenceLengths": "tensorrt_llm::runtime::ITensor::SharedPtr",
        "cumLogProbs": "tensorrt_llm::runtime::ITensor::SharedPtr",
        "logProbs": "tensorrt_llm::runtime::ITensor::SharedPtr",
    },
    "layers/LookaheadAlgorithm.java": {
        "draftTokens": "tensorrt_llm::runtime::ITensor::SharedPtr",
        "positionIds": "tensorrt_llm::runtime::ITensor::SharedPtr",
        "draftLengthPtr": "tensorrt_llm::runtime::ITensor::SharedPtr",
        "attentionMask": "tensorrt_llm::runtime::ITensor::SharedPtr",
        "acceptedTokens": "tensorrt_llm::runtime::ITensor::SharedPtr",
        "acceptedOffsets": "tensorrt_llm::runtime::ITensor::SharedPtr",
        "acceptedLength": "tensorrt_llm::runtime::ITensor::SharedPtr",
        "mask": "tensorrt_llm::runtime::ITensor::SharedPtr",
        "tokens": "tensorrt_llm::runtime::ITensor::SharedPtr",
        "masks": "tensorrt_llm::runtime::ITensor::SharedPtr",
        "encodeMap": "tensorrt_llm::runtime::ITensor::SharedPtr",
    },
    "batch_manager/TransformerBuffers.java": {
        "decoderPositionIds": "tensorrt_llm::runtime::ITensor::SharedPtr",
        "decoderCacheIndirectionOutput": "tensorrt_llm::runtime::ITensor::SharedPtr",
        "encoderInputLengths": "tensorrt_llm::runtime::ITensor::SharedPtr",
        "decoderContextLengthsDevice": "tensorrt_llm::runtime::ITensor::SharedPtr",
    },
    "executor/Executor.java": {
        # awaitResponses returns std::vector<Response> / std::vector<std::vector<Response>>;
        # both collapse to Response once `std::vector<...>` wrapper is preserved.
        "awaitResponses": "tensorrt_llm::executor::Response",
    },
    "executor/Result.java": {
        "logProbs": "tensorrt_llm::executor::VecLogProbs",
    },
    "global/Thop.java": {
        "spark_bf16_algo_list": "tensorrt_llm::torch_ext::cublas_lut::AlgoList",
        "bf16_algo_list": "tensorrt_llm::torch_ext::cublas_lut::AlgoList",
        "fp8_algo_list": "tensorrt_llm::torch_ext::cublas_lut::AlgoList",
    },
}

# Patterns that locate a broken cast plus the identifier it guards.  We apply
# them on a per-line basis; multi-line signatures are handled because every
# leaked cast always sits on the same line as its identifier in the generated
# source.

# Match any @Cast("...Pointer*") where the trailing content after Pointer is
# optional ">" chars (closing template brackets) followed by "*" (optionally
# quoted).  Group 1 is the prefix inside the quotes before "Pointer".
CAST_RE = re.compile(r'@Cast\("((?:const\s+)?(?:std::vector<)*)Pointer(\*"?\))')

# Find the identifier that the cast annotation targets on the same line.  The
# identifier is either the next parameter name or the method/field name.
IDENT_RE = re.compile(r'\b([A-Za-z_][A-Za-z0-9_]*)\b')

SKIP_IDENTS = {
    "Pointer", "SharedPtr", "IntPointer", "BytePointer", "Optional", "ByRef",
    "ByVal", "Const", "StdVector", "NoException", "MemberGetter", "Name",
    "Namespace", "Cast", "SharedConstPtr", "public", "private", "protected",
    "static", "native", "void", "boolean", "int", "long", "float", "double",
    "final", "return", "new", "this", "super", "null", "true", "false",
    "if", "else", "for", "while", "try", "catch", "throw", "throws",
    "class", "interface", "extends", "implements", "import", "package",
}


def pick_identifier(after_cast: str) -> str | None:
    """Return the first meaningful Java identifier in ``after_cast``.

    Skips annotation-related tokens, common Java keywords, and primitive
    types so we end up with the parameter/method name the cast refers to.
    """
    for tok in IDENT_RE.findall(after_cast):
        if tok in SKIP_IDENTS:
            continue
        # Skip template / qualifier tokens that can appear inside annotations.
        if tok.startswith("tensorrt_llm") or tok.startswith("std") or tok.startswith("nvinfer"):
            continue
        return tok
    return None


def file_key(path: str) -> str:
    return os.path.relpath(path, SRC_DIR).replace(os.sep, "/")


def resolve_type(rel_key: str, ident: str) -> str | None:
    override = FILE_OVERRIDES.get(rel_key, {}).get(ident)
    if override:
        return override
    return DEFAULT_BY_NAME.get(ident)


def fix_line(line: str, rel_key: str, stats: Dict[str, int]) -> Tuple[str, bool, List[str]]:
    warnings: List[str] = []
    changed = False

    def repl(match: re.Match) -> str:
        nonlocal changed
        prefix, suffix = match.group(1), match.group(2)
        tail = line[match.end():]
        ident = pick_identifier(tail)
        cpp_type = resolve_type(rel_key, ident) if ident else None
        if not cpp_type:
            warnings.append(
                f"{rel_key}: could not resolve type for identifier "
                f"{ident!r} in fragment: {line.strip()}"
            )
            stats["unresolved"] += 1
            return match.group(0)
        # Close any template brackets that ``std::vector<`` opened in the
        # prefix so we emit ``std::vector<T>*`` instead of ``std::vector<T*``.
        open_count = prefix.count("std::vector<")
        closers = ">" * open_count
        changed = True
        stats["fixed"] += 1
        return f'@Cast("{prefix}{cpp_type}{closers}{suffix}'

    new_line = CAST_RE.sub(repl, line)
    return new_line, changed, warnings


def process_file(path: str, check_only: bool, stats: Dict[str, int]) -> List[str]:
    warnings: List[str] = []
    rel_key = file_key(path)
    with open(path, "r", encoding="utf-8") as fp:
        original = fp.read()

    new_lines: List[str] = []
    file_changed = False
    for line in original.splitlines(keepends=True):
        if 'Pointer*"' not in line:
            new_lines.append(line)
            continue
        fixed, changed, line_warns = fix_line(line, rel_key, stats)
        warnings.extend(line_warns)
        if changed:
            file_changed = True
        new_lines.append(fixed)

    if file_changed and not check_only:
        with open(path, "w", encoding="utf-8") as fp:
            fp.writelines(new_lines)
        stats["files_changed"] += 1
    return warnings


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--check", action="store_true",
                    help="report what would change without writing")
    args = ap.parse_args()

    stats = {"fixed": 0, "unresolved": 0, "balanced": 0, "files_changed": 0, "files_scanned": 0}
    all_warnings: List[str] = []
    for root, _dirs, files in os.walk(SRC_DIR):
        for name in files:
            if not name.endswith(".java"):
                continue
            path = os.path.join(root, name)
            stats["files_scanned"] += 1
            all_warnings.extend(process_file(path, args.check, stats))

    print(f"scanned {stats['files_scanned']} java files")
    print(f"fixed {stats['fixed']} @Cast sites, balanced {stats['balanced']} templates across {stats['files_changed']} files")
    if stats["unresolved"]:
        print(f"WARNING: {stats['unresolved']} @Cast sites could not be resolved:")
        for w in all_warnings:
            print("  -", w)
        return 1 if args.check else 0
    return 0


if __name__ == "__main__":
    sys.exit(main())


