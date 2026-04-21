#!/bin/bash
#
# bundle_native_deps.sh — 将 CUDA / TensorRT / TensorRT-LLM 依赖打包进平台 native JAR
#
# 适用于: Linux x86_64 / Linux arm64 (需要 CUDA 环境)
#
# 打包后 JAR 结构:
#   org/bytedeco/tensorrt_llm/<platform>/
#     libjniTRTLLM.so            ← JNI bridge
#     libtensorrt_llm.so         ← TensorRT-LLM core
#     libnvinfer.so.10           ← TensorRT runtime
#     libnvinfer_plugin.so.10    ← TensorRT plugins
#     libcudart.so.12            ← CUDA runtime
#     libcublas.so.12            ← cuBLAS
#     libcublasLt.so.12          ← cuBLAS Lt
#     libnccl.so.2               ← NCCL (multi-GPU comms)
#     libcurand.so.10            ← cuRAND
#     README.properties
#
# 用法:
#   # 自动检测平台:
#   ./scripts/bundle_native_deps.sh
#
#   # 指定 TensorRT-LLM 构建目录:
#   TRTLLM_BUILD_DIR=/path/to/TensorRT-LLM/cpp/build \
#   CUDA_LIB_DIR=/usr/local/cuda/lib64 \
#   TENSORRT_LIB_DIR=/usr/lib/x86_64-linux-gnu \
#   ./scripts/bundle_native_deps.sh
#
# 前置条件:
#   1. 在 Linux 机器上构建 TensorRT-LLM:
#      git clone https://github.com/NVIDIA/TensorRT-LLM.git
#      cd TensorRT-LLM
#      python3 scripts/build_wheel.py --clean --trt_root /usr/local/tensorrt
#   2. 生成 JNI 库 (在本仓库根目录):
#      mvn -pl . org.bytedeco:javacpp:build -Djavacpp.compiler.skip=false \
#          -Dtrtllm.include.path=$TRTLLM_DIR/cpp/include \
#          -Dtrtllm.lib.path=$TRTLLM_BUILD_DIR
#

set -euo pipefail

COPIED_LIBS=()

# ============================================================
# 配置
# ============================================================
BASE_DIR="$(cd "$(dirname "$0")/.." && pwd)"
VERSION="0.17.0-1.5.13"
GROUP_ID="org.bytedeco"
ARTIFACT_ID="tensorrt-llm"
OUTPUT_DIR="${BASE_DIR}/dist"

# 自动检测平台
ARCH=$(uname -m)
OS=$(uname -s | tr '[:upper:]' '[:lower:]')
if [[ "$OS" == "linux" && "$ARCH" == "x86_64" ]]; then
    PLATFORM="linux-x86_64"
elif [[ "$OS" == "linux" && ("$ARCH" == "aarch64" || "$ARCH" == "arm64") ]]; then
    PLATFORM="linux-arm64"
elif [[ "$OS" == "darwin" && "$ARCH" == "arm64" ]]; then
    PLATFORM="macosx-arm64"
else
    echo "⚠️  Unsupported platform: ${OS}-${ARCH}. Defaulting to linux-x86_64"
    PLATFORM="linux-x86_64"
fi

# 可通过环境变量覆盖
PLATFORM="${PLATFORM_OVERRIDE:-$PLATFORM}"
TRTLLM_BUILD_DIR="${TRTLLM_BUILD_DIR:-/workspace/TensorRT-LLM/cpp/build}"
CUDA_LIB_DIR="${CUDA_LIB_DIR:-/usr/local/cuda/lib64}"
TENSORRT_LIB_DIR="${TENSORRT_LIB_DIR:-/usr/lib/x86_64-linux-gnu}"
NCCL_LIB_DIR="${NCCL_LIB_DIR:-/usr/lib/x86_64-linux-gnu}"
REQUIRE_FULL_DEPS="${REQUIRE_FULL_DEPS:-1}"

NATIVE_RESOURCES_DIR="${BASE_DIR}/trtllm-native/${PLATFORM}/src/main/resources/org/bytedeco/tensorrt_llm/${PLATFORM}"

echo "============================================================"
echo "  TensorRT-LLM Native Dependency Bundler"
echo "============================================================"
echo "Platform      : ${PLATFORM}"
echo "TRTLLM build  : ${TRTLLM_BUILD_DIR}"
echo "CUDA libs     : ${CUDA_LIB_DIR}"
echo "TensorRT libs : ${TENSORRT_LIB_DIR}"
echo "Target dir    : ${NATIVE_RESOURCES_DIR}"
echo ""

mkdir -p "${NATIVE_RESOURCES_DIR}" "${OUTPUT_DIR}"

# ============================================================
# 辅助函数
# ============================================================
copy_lib() {
    local LIB_NAME="$1"
    local SEARCH_DIRS=("${@:2}")
    local FOUND=0

    for DIR in "${SEARCH_DIRS[@]}"; do
        # 尝试精确匹配和 glob 匹配
        for FILE in "${DIR}/${LIB_NAME}" "${DIR}/${LIB_NAME}."* ; do
            if [[ -f "$FILE" ]]; then
                BASENAME=$(basename "$FILE")
                cp "$FILE" "${NATIVE_RESOURCES_DIR}/${BASENAME}"
                echo "   ✅ Copied: ${BASENAME} ($(du -sh "$FILE" | cut -f1))"
                COPIED_LIBS+=("${BASENAME}")
                FOUND=1
                break 2
            fi
        done
    done

    if [[ $FOUND -eq 0 ]]; then
        echo "   ⚠️  Not found: ${LIB_NAME} (searched: ${SEARCH_DIRS[*]})"
    fi
}

# ============================================================
# 1. 复制 JNI bridge (libjniTRTLLM.so)
# ============================================================
echo "📦 [1/6] JNI Bridge library..."
JNILIB_EXTENSION="so"
[[ "$PLATFORM" == "macosx-arm64" ]] && JNILIB_EXTENSION="dylib"

JNI_LIB="${BASE_DIR}/build-jni/${PLATFORM}/libjniTRTLLM.${JNILIB_EXTENSION}"
if [[ -f "$JNI_LIB" ]]; then
    cp "$JNI_LIB" "${NATIVE_RESOURCES_DIR}/"
    echo "   ✅ libjniTRTLLM.${JNILIB_EXTENSION} ($(du -sh "$JNI_LIB" | cut -f1))"
else
    echo "   ⚠️  JNI lib not found at ${JNI_LIB}"
    echo "   Run: mvn org.bytedeco:javacpp:build -Djavacpp.compiler.skip=false"
fi

# ============================================================
# 2. 复制 TensorRT-LLM 核心库
# ============================================================
echo "📦 [2/6] TensorRT-LLM core libraries..."
TRTLLM_SUBDIRS=(
    "${TRTLLM_BUILD_DIR}/tensorrt_llm"
    "${TRTLLM_BUILD_DIR}"
    "${TRTLLM_BUILD_DIR}/lib"
)

for LIB in libtensorrt_llm libtensorrt_llm_executor_static; do
    copy_lib "${LIB}.so" "${TRTLLM_SUBDIRS[@]}"
done

# ============================================================
# 3. 复制 TensorRT 库
# ============================================================
echo "📦 [3/6] TensorRT libraries..."
TENSORRT_DIRS=(
    "${TENSORRT_LIB_DIR}"
    "/usr/local/tensorrt/lib"
    "/opt/tensorrt/lib"
    "/usr/lib/$(uname -m)-linux-gnu"
)

for LIB in libnvinfer libnvinfer_plugin libnvinfer_vc_plugin; do
    copy_lib "${LIB}.so" "${TENSORRT_DIRS[@]}"
done

# ============================================================
# 4. 复制 CUDA 运行时库
# ============================================================
echo "📦 [4/6] CUDA runtime libraries..."
CUDA_GLOB_DIRS=()
for D in /usr/local/cuda-*/lib64; do
    [[ -d "$D" ]] && CUDA_GLOB_DIRS+=("$D")
done
CUDA_DIRS=(
    "${CUDA_LIB_DIR}"
    "/usr/local/cuda/lib64"
    "/usr/lib/x86_64-linux-gnu"
)
if [[ ${#CUDA_GLOB_DIRS[@]} -gt 0 ]]; then
    CUDA_DIRS+=("${CUDA_GLOB_DIRS[@]}")
fi

for LIB in libcudart libcublas libcublasLt libcurand; do
    copy_lib "${LIB}.so" "${CUDA_DIRS[@]}"
done

# ============================================================
# 5. 复制 NCCL (多 GPU 通信)
# ============================================================
echo "📦 [5/6] NCCL library..."
NCCL_DIRS=(
    "${NCCL_LIB_DIR}"
    "/usr/lib/x86_64-linux-gnu"
    "/usr/local/nccl/lib"
)
copy_lib "libnccl.so" "${NCCL_DIRS[@]}"

# ============================================================
# 5.5 Linux 平台依赖完整性检查
# ============================================================
if [[ "$PLATFORM" == linux-* ]]; then
    echo ""
    echo "🔍 Validating required native libraries for ${PLATFORM}..."
    JNI_BASENAME="libjniTRTLLM.so"
    REQUIRED_LIBS=(
        "$JNI_BASENAME"
        "libtensorrt_llm.so"
        "libnvinfer.so"
        "libcudart.so"
    )

    MISSING_REQUIRED=()
    for REQ in "${REQUIRED_LIBS[@]}"; do
        if ! compgen -G "${NATIVE_RESOURCES_DIR}/${REQ}*" > /dev/null; then
            MISSING_REQUIRED+=("$REQ")
        fi
    done

    if [[ ${#MISSING_REQUIRED[@]} -gt 0 ]]; then
        echo "❌ Missing required libraries: ${MISSING_REQUIRED[*]}"
        echo "   Resource dir: ${NATIVE_RESOURCES_DIR}"
        echo "   Hint: set TRTLLM_BUILD_DIR / CUDA_LIB_DIR / TENSORRT_LIB_DIR to real Linux paths."
        if [[ "$REQUIRE_FULL_DEPS" == "1" ]]; then
            echo "   REQUIRE_FULL_DEPS=1, aborting instead of creating a tiny stub JAR."
            exit 1
        else
            echo "   REQUIRE_FULL_DEPS=0, continuing with partial/stub native JAR."
        fi
    else
        echo "✅ Required libraries found."
    fi
fi

# ============================================================
# 6. 打包成 JAR
# ============================================================
echo "📦 [6/6] Packaging native JAR..."
JAR_NAME="${ARTIFACT_ID}-${VERSION}-${PLATFORM}.jar"
TEMP_DIR=$(mktemp -d)

# 保留完整的 org/bytedeco/tensorrt_llm/<platform>/ 目录结构
NATIVE_RESOURCES_PARENT="${BASE_DIR}/trtllm-native/${PLATFORM}/src/main/resources"
cp -r "${NATIVE_RESOURCES_PARENT}/"* "${TEMP_DIR}/" 2>/dev/null || true

# 添加 Maven 元数据
mkdir -p "${TEMP_DIR}/META-INF/maven/${GROUP_ID}/${ARTIFACT_ID}"
cat > "${TEMP_DIR}/META-INF/maven/${GROUP_ID}/${ARTIFACT_ID}/pom.properties" <<EOF
version=${VERSION}
groupId=${GROUP_ID}
artifactId=${ARTIFACT_ID}
classifier=${PLATFORM}
EOF

cd "${TEMP_DIR}"
jar cf "${OUTPUT_DIR}/${JAR_NAME}" .
rm -rf "${TEMP_DIR}"

JAR_SIZE=$(du -sh "${OUTPUT_DIR}/${JAR_NAME}" | cut -f1)
echo ""
echo "============================================================"
echo "✅ Native JAR built: ${OUTPUT_DIR}/${JAR_NAME}"
echo "   Size: ${JAR_SIZE}"
echo "   Copied native libs: ${#COPIED_LIBS[@]}"
echo "============================================================"
echo ""

# 检查大小
JAR_BYTES=$(stat -c%s "${OUTPUT_DIR}/${JAR_NAME}" 2>/dev/null || stat -f%z "${OUTPUT_DIR}/${JAR_NAME}")
MIN_SIZE=$((10 * 1024 * 1024))  # 10 MB
if [[ $JAR_BYTES -lt $MIN_SIZE ]]; then
    echo "⚠️  JAR size (${JAR_SIZE}) is less than 10 MB."
    echo "   This may indicate that CUDA/TensorRT libraries were not found."
    echo "   Please check TRTLLM_BUILD_DIR, CUDA_LIB_DIR, TENSORRT_LIB_DIR."
else
    echo "✅ JAR size check passed (${JAR_SIZE} >= 10 MB)"
fi

# ============================================================
# 安装到本地 Maven 仓库
# ============================================================
echo ""
echo "📥 Installing to local Maven repository..."
cd "${BASE_DIR}"
mvn install:install-file \
    -Dfile="${OUTPUT_DIR}/${JAR_NAME}" \
    -DgroupId=${GROUP_ID} \
    -DartifactId=${ARTIFACT_ID} \
    -Dversion=${VERSION} \
    -Dpackaging=jar \
    -Dclassifier=${PLATFORM} -q
echo "   ✅ Installed: ${GROUP_ID}:${ARTIFACT_ID}:${VERSION}:${PLATFORM}"

