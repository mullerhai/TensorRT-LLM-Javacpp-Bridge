#!/bin/bash
#
# 在 Linux GPU 机器上编译 JNI native 库
#
# 前置条件:
#   - CUDA toolkit 已安装 (nvcc, cuda_fp16.h 等)
#   - TensorRT-LLM 已编译 (libtensorrt_llm.so)
#   - JDK 11+ 已安装
#
# 用法:
#   ./compile_jni_linux.sh /path/to/TensorRT-LLM [/path/to/cuda] [/path/to/tensorrt]
#
# 示例:
#   ./compile_jni_linux.sh /workspace/TensorRT-LLM /usr/local/cuda /usr/local/tensorrt
#
set -e

TRTLLM_DIR="${1:-/opt/tensorrt_llm}"
CUDA_HOME="${CUDA_HOME:-${2:-/usr/local/cuda}}"
TENSORRT_HOME="${TENSORRT_HOME:-${3:-/usr/local/tensorrt}}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAVA_HOME="${JAVA_HOME:-$(dirname $(dirname $(readlink -f $(which java))))}"

echo "=== Compiling JNI Native Library for Linux x86_64 ==="
echo "TRT-LLM : ${TRTLLM_DIR}"
echo "CUDA    : ${CUDA_HOME}"
echo "TensorRT: ${TENSORRT_HOME}"
echo "JAVA    : ${JAVA_HOME}"
echo ""

# 收集所有 JNI 源文件 (JavaCPP 生成多个 .cpp)
JNI_SRCS=()
for CPP in \
    jnijavacpp.cpp \
    jniExecutor.cpp \
    jniBatchmanager.cpp \
    jniTrtllmRuntime.cpp \
    jniCommon.cpp \
    jniKernels.cpp \
    jniLayers.cpp \
    jniPlugins.cpp \
    jniCutlassExtensions.cpp \
    jniThop.cpp; do
    if [[ -f "${SCRIPT_DIR}/${CPP}" ]]; then
        JNI_SRCS+=("${SCRIPT_DIR}/${CPP}")
    fi
done

# 兼容旧的单文件布局
if [[ -f "${SCRIPT_DIR}/jniTRTLLM.cpp" ]] && [[ ${#JNI_SRCS[@]} -le 1 ]]; then
    JNI_SRCS=("${SCRIPT_DIR}/jniTRTLLM.cpp" "${SCRIPT_DIR}/jnijavacpp.cpp")
fi

if [[ ${#JNI_SRCS[@]} -eq 0 ]]; then
    echo "❌ No JNI source files found in ${SCRIPT_DIR}"
    exit 1
fi
echo "📦 Found ${#JNI_SRCS[@]} source files, compiling..."

# 搜索 TensorRT-LLM 库路径
TRTLLM_LIB_DIR=""
for D in \
    "${TRTLLM_DIR}/cpp/build/tensorrt_llm" \
    "${TRTLLM_DIR}/cpp/build" \
    "${TRTLLM_DIR}/cpp/build/lib" \
    "${TRTLLM_DIR}/build"; do
    if ls "$D"/libtensorrt_llm*.so 2>/dev/null | head -1 | grep -q .; then
        TRTLLM_LIB_DIR="$D"
        echo "✅ Found libtensorrt_llm.so in: ${TRTLLM_LIB_DIR}"
        break
    fi
done
[[ -z "$TRTLLM_LIB_DIR" ]] && echo "⚠️  libtensorrt_llm.so not found, linking without it"

LINK_FLAGS=()
[[ -n "$TRTLLM_LIB_DIR" ]] && LINK_FLAGS+=("-L${TRTLLM_LIB_DIR}" "-ltensorrt_llm")
LINK_FLAGS+=(
    "-L${CUDA_HOME}/lib64" "-lcudart"
    "-L${TENSORRT_HOME}/lib" "-lnvinfer"
    "-Wl,-rpath,\$ORIGIN"
)

echo "Compiling with g++ -std=c++17 ..."
g++ -std=c++17 \
    -I"${TRTLLM_DIR}/cpp/include" \
    -I"${CUDA_HOME}/include" \
    -I"${JAVA_HOME}/include" \
    -I"${JAVA_HOME}/include/linux" \
    -I"${TENSORRT_HOME}/include" \
    -O2 -fPIC -pthread -shared \
    "${JNI_SRCS[@]}" \
    "${LINK_FLAGS[@]}" \
    -o "${SCRIPT_DIR}/libjniTRTLLM.so"

echo ""
echo "✅ libjniTRTLLM.so generated:"
ls -lh "${SCRIPT_DIR}/libjniTRTLLM.so"
file "${SCRIPT_DIR}/libjniTRTLLM.so"
echo ""
echo "Next steps:"
echo "  1. Run: cd <project-root> && scripts/bundle_native_deps.sh"
echo "  2. Run: scripts/build_platform_jars.sh"

