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
#   ./compile_jni_linux.sh /path/to/TensorRT-LLM
#
set -e

TRTLLM_DIR="${1:-/opt/tensorrt_llm}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
CUDA_HOME="${CUDA_HOME:-/usr/local/cuda}"
JAVA_HOME="${JAVA_HOME:-$(dirname $(dirname $(readlink -f $(which java))))}"

echo "=== Compiling JNI Native Library for Linux ==="
echo "TRT-LLM: ${TRTLLM_DIR}"
echo "CUDA:    ${CUDA_HOME}"
echo "JAVA:    ${JAVA_HOME}"
echo ""

# 查找 JNI source
JNI_SRC="${SCRIPT_DIR}/jniTRTLLM.cpp"
JNI_BASE="${SCRIPT_DIR}/jnijavacpp.cpp"

if [ ! -f "${JNI_SRC}" ]; then
    echo "❌ jniTRTLLM.cpp not found in ${SCRIPT_DIR}"
    exit 1
fi

echo "📦 Compiling JNI native library..."

g++ -std=c++17 \
    -I"${TRTLLM_DIR}/cpp/include" \
    -I"${CUDA_HOME}/include" \
    -I"${JAVA_HOME}/include" \
    -I"${JAVA_HOME}/include/linux" \
    -L"${TRTLLM_DIR}/cpp/build/tensorrt_llm" \
    -L"${CUDA_HOME}/lib64" \
    "${JNI_SRC}" "${JNI_BASE}" \
    -O3 -fPIC -pthread -shared \
    -ltensorrt_llm \
    -Wl,-rpath,'$ORIGIN' \
    -o "${SCRIPT_DIR}/libjniTRTLLM.so"

echo ""
echo "✅ libjniTRTLLM.so generated:"
ls -lh "${SCRIPT_DIR}/libjniTRTLLM.so"
file "${SCRIPT_DIR}/libjniTRTLLM.so"
echo ""
echo "To use, copy libjniTRTLLM.so to your classpath"
echo "or add to: org/bytedeco/tensorrt_llm/linux-x86_64/"

