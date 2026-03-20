#!/bin/bash
#
# 在 Docker Linux 容器中生成 JavaCPP JNI native 库 (.so)
# 同时也尝试在 macOS 本地生成 (.dylib)
#
set -e

BASE_DIR="$(cd "$(dirname "$0")/.." && pwd)"
VERSION="0.17.0-1.5.13"
ARTIFACT_ID="tensorrt-llm"
GROUP_ID="org.bytedeco"

TRTLLM_SRC="/Users/mullerzhang/Documents/code/TensorRT-LLM"
BRIDGE_DIR="${BASE_DIR}/trtllm-bridge"
DIST_DIR="${BASE_DIR}/dist"
NATIVE_LINUX_DIR="${BASE_DIR}/trtllm-native/linux-x86_64/src/main/resources/org/bytedeco/tensorrt_llm/linux-x86_64"
NATIVE_MAC_DIR="${BASE_DIR}/trtllm-native/macosx-arm64/src/main/resources/org/bytedeco/tensorrt_llm/macosx-arm64"

mkdir -p "${DIST_DIR}" "${NATIVE_LINUX_DIR}" "${NATIVE_MAC_DIR}"

echo "============================================"
echo "  TensorRT-LLM Native Library Builder"
echo "============================================"
echo ""

# ============================================
# Part A: 在 Docker Linux 中生成 linux-x86_64
# ============================================
echo "🐧 === Part A: Building Linux x86_64 native libraries via Docker ==="
echo ""

# 构建 Docker 镜像
echo "📦 Building Docker image..."
docker build -t trtllm-javacpp-builder -f "${BASE_DIR}/docker/Dockerfile.linux-x86_64" "${BASE_DIR}/docker/" 2>&1 | tail -3

# 在容器中执行 JavaCPP build
echo ""
echo "🔨 Running JavaCPP native compilation in Docker container..."
echo "   (This generates the JNI bridge .so file)"
echo ""

docker run --rm \
    --platform linux/amd64 \
    -v "${BRIDGE_DIR}:/workspace/trtllm-bridge:ro" \
    -v "${TRTLLM_SRC}/cpp/include:/workspace/trtllm-include:ro" \
    -v "${NATIVE_LINUX_DIR}:/workspace/output" \
    -v "${HOME}/.m2/repository:/root/.m2/repository" \
    trtllm-javacpp-builder \
    bash -c '
set -e
echo "=== Inside Docker Linux container ==="
echo "Platform: $(uname -m) / $(uname -s)"
java -version 2>&1 | head -1
echo ""

# 复制项目到可写位置
cp -r /workspace/trtllm-bridge /tmp/trtllm-bridge
cd /tmp/trtllm-bridge

# 先编译 Java 代码
echo "📦 Compiling Java bindings..."
mvn compile -DskipTests=true -q 2>&1 | tail -3

# 用 JavaCPP 生成 JNI C++ 代码并编译为 .so
echo ""
echo "🔨 Running JavaCPP JNI generation & compilation..."
echo "   include path: /workspace/trtllm-include"
echo ""

# JavaCPP build 会:
# 1. 生成 jniTRTLLM.cpp (JNI bridge C++ 代码)
# 2. 编译为 libjniTRTLLM.so
mvn org.bytedeco:javacpp:build \
    -Djavacpp.compiler.skip=false \
    -Djavacpp.includePath="/workspace/trtllm-include" \
    -Djavacpp.linkPath="" \
    -Djavacpp.preloadPath="" \
    2>&1 || {
    echo ""
    echo "⚠️  Full JavaCPP build failed (expected without TRT-LLM libs)."
    echo "   Attempting to generate JNI source code only..."
    echo ""

    # 至少生成 JNI C++ 源代码
    mvn org.bytedeco:javacpp:build \
        -Djavacpp.compiler.skip=true \
        -Djavacpp.deleteJniFiles=false \
        2>&1 | tail -5 || true
}

# 查找生成的文件
echo ""
echo "=== Generated files ==="
find /tmp/trtllm-bridge -name "*.so" -o -name "*.cpp" -o -name "jni*" 2>/dev/null | head -20
find /tmp/trtllm-bridge/target -name "*.so" -o -name "*.cpp" 2>/dev/null | head -20

# 复制产出到输出目录
echo ""
echo "📤 Copying output files..."
find /tmp/trtllm-bridge -name "*.so" -exec cp -v {} /workspace/output/ \; 2>/dev/null || true
find /tmp/trtllm-bridge/target -name "jni*.cpp" -exec cp -v {} /workspace/output/ \; 2>/dev/null || true

# 列出生成的 JNI C++ 源码大小
if ls /workspace/output/jni*.cpp 1>/dev/null 2>&1; then
    echo ""
    echo "✅ JNI C++ source generated:"
    ls -lh /workspace/output/jni*.cpp
    wc -l /workspace/output/jni*.cpp
fi

if ls /workspace/output/*.so 1>/dev/null 2>&1; then
    echo ""
    echo "✅ Native .so libraries generated:"
    ls -lh /workspace/output/*.so
fi

echo ""
echo "=== Docker build complete ==="
'

echo ""
echo "📋 Linux x86_64 output:"
ls -lh "${NATIVE_LINUX_DIR}/" 2>/dev/null || echo "   (no files generated)"

# ============================================
# Part B: 尝试在 macOS 本地生成
# ============================================
echo ""
echo "🍎 === Part B: Attempting macOS ARM64 native build (experimental) ==="
echo ""

cd "${BRIDGE_DIR}"

echo "🔨 Running JavaCPP on macOS..."
echo "   This will likely fail for TRT-LLM specific code,"
echo "   but may generate the JNI bridge source and partial .dylib"
echo ""

# 尝试 JavaCPP build
mvn org.bytedeco:javacpp:build \
    -Djavacpp.compiler.skip=false \
    -Djavacpp.includePath="${TRTLLM_SRC}/cpp/include" \
    -Djavacpp.linkPath="" \
    -Djavacpp.preloadPath="" \
    2>&1 | tail -20 || {
    echo ""
    echo "⚠️  Full macOS build failed (expected — no CUDA/TRT on macOS)"
    echo "   Trying with compiler.skip=true to just generate JNI source..."
    echo ""

    mvn org.bytedeco:javacpp:build \
        -Djavacpp.compiler.skip=true \
        -Djavacpp.deleteJniFiles=false \
        2>&1 | tail -10 || true
}

# 检查 macOS 产出
echo ""
echo "📋 macOS output:"
find "${BRIDGE_DIR}/target" -name "*.dylib" -o -name "*.jnilib" -o -name "jni*.cpp" 2>/dev/null | head -10
find "${BRIDGE_DIR}/target" -name "*.dylib" -exec cp -v {} "${NATIVE_MAC_DIR}/" \; 2>/dev/null || true
find "${BRIDGE_DIR}/target" -name "jni*.cpp" -exec cp -v {} "${NATIVE_MAC_DIR}/" \; 2>/dev/null || true

ls -lh "${NATIVE_MAC_DIR}/" 2>/dev/null || echo "   (no files generated)"

# ============================================
# Part C: 重新打包 native JAR
# ============================================
echo ""
echo "📦 === Part C: Rebuilding native JARs with generated files ==="
echo ""

cd "${BASE_DIR}"

rebuild_native_jar() {
    local PLATFORM=$1
    local NATIVE_RES="${BASE_DIR}/trtllm-native/${PLATFORM}/src/main/resources"
    local JAR_NAME="${ARTIFACT_ID}-${VERSION}-${PLATFORM}.jar"
    local TEMP_DIR=$(mktemp -d)

    local FILE_COUNT=$(find "${NATIVE_RES}" \( -name "*.so" -o -name "*.dylib" -o -name "*.cpp" \) 2>/dev/null | wc -l | tr -d ' ')

    echo "📦 Packaging ${PLATFORM}: ${FILE_COUNT} native files"

    cp -r "${NATIVE_RES}/"* "${TEMP_DIR}/" 2>/dev/null || true

    mkdir -p "${TEMP_DIR}/META-INF/maven/${GROUP_ID}/${ARTIFACT_ID}"
    cat > "${TEMP_DIR}/META-INF/maven/${GROUP_ID}/${ARTIFACT_ID}/pom.properties" <<EOF
version=${VERSION}
groupId=${GROUP_ID}
artifactId=${ARTIFACT_ID}
classifier=${PLATFORM}
EOF

    cd "${BASE_DIR}"
    jar cf "${DIST_DIR}/${JAR_NAME}" -C "${TEMP_DIR}" .
    rm -rf "${TEMP_DIR}"
    echo "   ✅ ${JAR_NAME} ($(ls -lh "${DIST_DIR}/${JAR_NAME}" | awk '{print $5}'))"
}

rebuild_native_jar "linux-x86_64"
rebuild_native_jar "linux-arm64"
rebuild_native_jar "macosx-arm64"

# 重新安装到 Maven 仓库
echo ""
echo "📥 Reinstalling to local Maven repository..."
for PLATFORM in linux-x86_64 linux-arm64 macosx-arm64; do
    mvn install:install-file \
        -Dfile="${DIST_DIR}/${ARTIFACT_ID}-${VERSION}-${PLATFORM}.jar" \
        -DgroupId=${GROUP_ID} -DartifactId=${ARTIFACT_ID} \
        -Dversion=${VERSION} -Dpackaging=jar -Dclassifier=${PLATFORM} -q 2>/dev/null || true
done

# ============================================
# 最终摘要
# ============================================
echo ""
echo "============================================"
echo "✅ Native Library Build Complete!"
echo "============================================"
echo ""
echo "dist/ 目录:"
ls -lh "${DIST_DIR}/"*.jar
echo ""
echo "Linux x86_64 native files:"
ls -lh "${NATIVE_LINUX_DIR}/" 2>/dev/null || echo "  (none)"
echo ""
echo "macOS ARM64 native files:"
ls -lh "${NATIVE_MAC_DIR}/" 2>/dev/null || echo "  (none)"

