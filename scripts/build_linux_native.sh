#!/bin/bash
#
# build_linux_native.sh — 在 Linux CUDA 机器上编译并打包 native JAR
#
# 此脚本完成以下工作:
#   1. 编译所有 JNI .cpp 文件 → libjniTRTLLM.so
#   2. 将 .so 复制到 trtllm-native/<platform>/src/main/resources/
#   3. 调用 bundle_native_deps.sh 捆绑 CUDA/TRT 依赖库
#   4. 重新打包 dist/<platform>.jar
#
# 用法:
#   # 自动检测平台 (linux-x86_64 / linux-arm64):
#   ./scripts/build_linux_native.sh /workspace/TensorRT-LLM
#
#   # 指定所有路径:
#   TRTLLM_DIR=/workspace/TensorRT-LLM \
#   CUDA_HOME=/usr/local/cuda \
#   TENSORRT_HOME=/usr/local/tensorrt \
#   TRTLLM_BUILD_DIR=/workspace/TensorRT-LLM/cpp/build \
#   CUDA_LIB_DIR=/usr/local/cuda/lib64 \
#   TENSORRT_LIB_DIR=/usr/local/tensorrt/lib \
#   ./scripts/build_linux_native.sh
#
set -euo pipefail

BASE_DIR="$(cd "$(dirname "$0")/.." && pwd)"

# ── 路径配置 ──────────────────────────────────────────────────
TRTLLM_DIR="${TRTLLM_DIR:-${1:-/opt/tensorrt_llm}}"
CUDA_HOME="${CUDA_HOME:-/usr/local/cuda}"
TENSORRT_HOME="${TENSORRT_HOME:-/usr/local/tensorrt}"
TRTLLM_BUILD_DIR="${TRTLLM_BUILD_DIR:-${TRTLLM_DIR}/cpp/build}"
CUDA_LIB_DIR="${CUDA_LIB_DIR:-${CUDA_HOME}/lib64}"
TENSORRT_LIB_DIR="${TENSORRT_LIB_DIR:-${TENSORRT_HOME}/lib}"
JAVA_HOME="${JAVA_HOME:-$(dirname $(dirname $(readlink -f $(which java))))}"
REQUIRE_FULL_DEPS="${REQUIRE_FULL_DEPS:-1}"

# ── 平台检测 ──────────────────────────────────────────────────
ARCH=$(uname -m)
if [[ "$ARCH" == "x86_64" ]]; then
    PLATFORM="linux-x86_64"
elif [[ "$ARCH" == "aarch64" || "$ARCH" == "arm64" ]]; then
    PLATFORM="linux-arm64"
else
    echo "❌ Unsupported architecture: $ARCH"; exit 1
fi

NATIVE_RESOURCES_DIR="${BASE_DIR}/trtllm-native/${PLATFORM}/src/main/resources/org/bytedeco/tensorrt_llm/${PLATFORM}"
JNI_BUILD_DIR="${BASE_DIR}/build-jni/${PLATFORM}"

echo "============================================================"
echo "  TensorRT-LLM Linux Native Build"
echo "============================================================"
echo "Platform      : ${PLATFORM}"
echo "TRT-LLM dir   : ${TRTLLM_DIR}"
echo "CUDA home     : ${CUDA_HOME}"
echo "TensorRT home : ${TENSORRT_HOME}"
echo "TRTLLM build  : ${TRTLLM_BUILD_DIR}"
echo "Java home     : ${JAVA_HOME}"
echo "Strict deps   : ${REQUIRE_FULL_DEPS}"
echo "Output dir    : ${NATIVE_RESOURCES_DIR}"
echo "============================================================"
echo ""

mkdir -p "${NATIVE_RESOURCES_DIR}"

# ── Step 1: 同步最新 JNI 源码 ─────────────────────────────────
echo "📋 [1/3] Syncing JNI sources from build-jni/${PLATFORM}/ ..."
if [[ ! -d "${JNI_BUILD_DIR}" ]]; then
    echo "❌ JNI source directory not found: ${JNI_BUILD_DIR}"
    echo "   Run: mvn org.bytedeco:javacpp:parse first (can be done on any machine)"
    exit 1
fi

for CPP in "${JNI_BUILD_DIR}"/*.cpp; do
    [ -f "$CPP" ] && cp "$CPP" "${NATIVE_RESOURCES_DIR}/"
done
echo "   ✅ Copied $(ls "${NATIVE_RESOURCES_DIR}"/*.cpp 2>/dev/null | wc -l | tr -d ' ') .cpp files"

# ── Step 2: 编译 libjniTRTLLM.so ────────────────────────────
echo ""
echo "🔨 [2/3] Compiling libjniTRTLLM.so ..."

# 收集所有 JNI .cpp 源文件
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
    F="${NATIVE_RESOURCES_DIR}/${CPP}"
    [[ -f "$F" ]] && JNI_SRCS+=("$F")
done

echo "   Compiling ${#JNI_SRCS[@]} source files..."
if [[ ${#JNI_SRCS[@]} -eq 0 ]]; then
    echo "❌ No JNI sources found under ${NATIVE_RESOURCES_DIR}"
    exit 1
fi

# 搜索 libtensorrt_llm.so
TRTLLM_LIB_DIR=""
for D in \
    "${TRTLLM_BUILD_DIR}/tensorrt_llm" \
    "${TRTLLM_BUILD_DIR}" \
    "${TRTLLM_BUILD_DIR}/lib"; do
    if ls "$D"/libtensorrt_llm*.so 2>/dev/null | head -1 | grep -q .; then
        TRTLLM_LIB_DIR="$D"; break
    fi
done

LINK_FLAGS=()
if [[ -n "$TRTLLM_LIB_DIR" ]]; then
    echo "   Using libtensorrt_llm from: ${TRTLLM_LIB_DIR}"
    LINK_FLAGS+=("-L${TRTLLM_LIB_DIR}" "-ltensorrt_llm")
else
    echo "❌ libtensorrt_llm.so not found under ${TRTLLM_BUILD_DIR}"
    echo "   This script is for full native packaging; refusing to build a stub .so."
    exit 1
fi

LINK_FLAGS+=(
    "-L${CUDA_LIB_DIR}" "-lcudart"
    "-L${TENSORRT_LIB_DIR}" "-lnvinfer"
    "-Wl,-rpath,\$ORIGIN"
)

g++ -std=c++17 \
    -I"${TRTLLM_DIR}/cpp/include" \
    -I"${CUDA_HOME}/include" \
    -I"${JAVA_HOME}/include" \
    -I"${JAVA_HOME}/include/linux" \
    -I"${TENSORRT_HOME}/include" \
    -O2 -fPIC -pthread -shared \
    "${JNI_SRCS[@]}" \
    "${LINK_FLAGS[@]}" \
    -o "${NATIVE_RESOURCES_DIR}/libjniTRTLLM.so"

echo ""
SO_SIZE=$(du -sh "${NATIVE_RESOURCES_DIR}/libjniTRTLLM.so" | cut -f1)
echo "   ✅ libjniTRTLLM.so (${SO_SIZE})"
file "${NATIVE_RESOURCES_DIR}/libjniTRTLLM.so"

# 同时复制到 build-jni/ 作为缓存
cp "${NATIVE_RESOURCES_DIR}/libjniTRTLLM.so" "${JNI_BUILD_DIR}/"

# ── Step 3: 捆绑 CUDA/TRT 依赖并打包 JAR ────────────────────
echo ""
echo "📦 [3/3] Bundling CUDA/TRT dependencies and packaging JAR ..."
PLATFORM_OVERRIDE=${PLATFORM} \
TRTLLM_BUILD_DIR="${TRTLLM_BUILD_DIR}" \
CUDA_LIB_DIR="${CUDA_LIB_DIR}" \
TENSORRT_LIB_DIR="${TENSORRT_LIB_DIR}" \
REQUIRE_FULL_DEPS="${REQUIRE_FULL_DEPS}" \
    "${BASE_DIR}/scripts/bundle_native_deps.sh"

echo ""
echo "============================================================"
echo "✅ Native build complete!"
echo "   JAR: ${BASE_DIR}/dist/tensorrt-llm-0.17.0-1.5.13-${PLATFORM}.jar"
JAR="${BASE_DIR}/dist/tensorrt-llm-0.17.0-1.5.13-${PLATFORM}.jar"
if [[ -f "$JAR" ]]; then
    echo "   Size: $(du -sh "$JAR" | cut -f1)"
fi
echo "============================================================"

