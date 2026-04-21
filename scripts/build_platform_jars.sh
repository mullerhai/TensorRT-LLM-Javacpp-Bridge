#!/bin/bash
#
# 构建 TensorRT-LLM platform JAR 包
#
# 产出的 JAR 文件结构 (参照 org.bytedeco:tensorrt-platform):
#
#   tensorrt-llm-0.17.0-1.5.13.jar                    — 纯 Java 绑定
#   tensorrt-llm-0.17.0-1.5.13-linux-x86_64.jar       — Linux x86_64 native 库
#   tensorrt-llm-0.17.0-1.5.13-linux-arm64.jar         — Linux ARM64 native 库
#   tensorrt-llm-0.17.0-1.5.13-macosx-arm64.jar        — macOS ARM64 stub
#   tensorrt-llm-platform-0.17.0-1.5.13.jar            — 平台聚合 (依赖上面所有)
#
# 用法:
#   ./scripts/build_platform_jars.sh
#
# 前置条件:
#   - trtllm-bridge 已编译: mvn package -DskipTests
#   - Linux native .so 已放入对应的 resources 目录 (见 bundle_native_deps.sh)
#     或 build-jni/<platform>/ 下有编译好的 libjniTRTLLM.so
#

set -e

VERSION="0.17.0-1.5.13"
GROUP_ID="org.bytedeco"
ARTIFACT_ID="tensorrt-llm"
BASE_DIR="$(cd "$(dirname "$0")/.." && pwd)"
OUTPUT_DIR="${BASE_DIR}/dist"

echo "=== Building TensorRT-LLM Platform JARs ==="
echo "Version: ${VERSION}"
echo "Output: ${OUTPUT_DIR}"
echo ""

mkdir -p "${OUTPUT_DIR}"

# ============================================
# 0. 同步 build-jni/ JNI 源码到 native 模块资源目录
# ============================================
echo "🔄 Syncing JNI sources from build-jni/ ..."
for PLATFORM in linux-x86_64 linux-arm64; do
    BSRC="${BASE_DIR}/build-jni/${PLATFORM}"
    RDST="${BASE_DIR}/trtllm-native/${PLATFORM}/src/main/resources/org/bytedeco/tensorrt_llm/${PLATFORM}"
    if [ -d "${BSRC}" ]; then
        mkdir -p "${RDST}"
        # 只复制 .cpp 文件 (不覆盖已编译的 .so)
        for CPP in "${BSRC}"/*.cpp; do
            [ -f "$CPP" ] && cp "$CPP" "${RDST}/"
        done
        # 如果 build-jni 有编译好的 .so, 优先使用
        SO_SRC="${BSRC}/libjniTRTLLM.so"
        if [ -f "${SO_SRC}" ]; then
            ARCH=$(file "${SO_SRC}" | grep -o "x86-64\|aarch64\|ARM aarch64" | head -1)
            echo "   ${PLATFORM}: found libjniTRTLLM.so (${ARCH})"
            cp "${SO_SRC}" "${RDST}/"
        fi
    fi
done

# macosx-arm64: 同步 macosx-arm64 build-jni
BSRC_MAC="${BASE_DIR}/build-jni/macosx-arm64"
RDST_MAC="${BASE_DIR}/trtllm-native/macosx-arm64/src/main/resources/org/bytedeco/tensorrt_llm/macosx-arm64"
if [ -d "${BSRC_MAC}" ]; then
    for CPP in "${BSRC_MAC}"/*.cpp; do
        [ -f "$CPP" ] && cp "$CPP" "${RDST_MAC}/"
    done
    if [ -f "${BSRC_MAC}/libjniTRTLLM.dylib" ]; then
        cp "${BSRC_MAC}/libjniTRTLLM.dylib" "${RDST_MAC}/"
    fi
fi
echo "   ✅ JNI sources synced"

# ============================================
# 1. 构建核心 Java 绑定 JAR
# ============================================
echo "📦 Building core Java bindings JAR..."
cd "${BASE_DIR}"
mvn clean package -DskipTests=true -q
JAR_SRC=$(ls target/trtllm-bridge-*.jar 2>/dev/null | head -1)
if [ -z "${JAR_SRC}" ]; then
  echo "ERROR: could not find packaged JAR in target/"; exit 1
fi
cp "${JAR_SRC}" "${OUTPUT_DIR}/${ARTIFACT_ID}-${VERSION}.jar"
echo "   ✅ ${ARTIFACT_ID}-${VERSION}.jar ($(du -sh "${OUTPUT_DIR}/${ARTIFACT_ID}-${VERSION}.jar" | cut -f1))"

# ============================================
# 2. 构建 native classifier JAR 文件
# ============================================
build_native_jar() {
    local PLATFORM=$1
    local NATIVE_DIR="${BASE_DIR}/trtllm-native/${PLATFORM}/src/main/resources"
    local JAR_NAME="${ARTIFACT_ID}-${VERSION}-${PLATFORM}.jar"
    local TEMP_DIR=$(mktemp -d)

    echo "📦 Building native JAR: ${PLATFORM}..."

    # 检查 .so / .dylib 文件
    local SO_FILES
    SO_FILES=$(find "${NATIVE_DIR}" \( -name "*.so" -o -name "*.so.*" -o -name "*.dylib" \) 2>/dev/null)
    local SO_COUNT
    SO_COUNT=$(echo "$SO_FILES" | grep -c . 2>/dev/null || echo 0)

    if [ "${SO_COUNT}" -eq "0" ]; then
        echo "   ⚠️  No native libraries (.so/.dylib) found for ${PLATFORM}"
        echo "      → JAR will contain JNI sources only (stub JAR)"
        echo "      → Run scripts/bundle_native_deps.sh on a Linux+CUDA machine to add real libs"
    else
        echo "   Found ${SO_COUNT} native library file(s):"
        while IFS= read -r F; do
            [ -n "$F" ] && echo "      $(ls -lh "$F" | awk '{print $5, $NF}')"
        done <<< "$SO_FILES"
    fi

    # 复制 resources 到临时目录
    cp -r "${NATIVE_DIR}/"* "${TEMP_DIR}/" 2>/dev/null || true

    # 创建 META-INF
    mkdir -p "${TEMP_DIR}/META-INF/maven/${GROUP_ID}/${ARTIFACT_ID}"
    cat > "${TEMP_DIR}/META-INF/maven/${GROUP_ID}/${ARTIFACT_ID}/pom.properties" <<EOF
version=${VERSION}
groupId=${GROUP_ID}
artifactId=${ARTIFACT_ID}
classifier=${PLATFORM}
EOF

    # 打包
    cd "${TEMP_DIR}"
    jar cf "${OUTPUT_DIR}/${JAR_NAME}" .
    rm -rf "${TEMP_DIR}"

    local JAR_SIZE
    JAR_SIZE=$(du -sh "${OUTPUT_DIR}/${JAR_NAME}" | cut -f1)
    local JAR_BYTES
    JAR_BYTES=$(stat -f%z "${OUTPUT_DIR}/${JAR_NAME}" 2>/dev/null || stat -c%s "${OUTPUT_DIR}/${JAR_NAME}")
    local WARN=""
    # 如果小于 1MB 且平台不是 macOS，给出警告
    if [[ $JAR_BYTES -lt $((1*1024*1024)) ]] && [[ "$PLATFORM" != "macosx-arm64" ]]; then
        WARN=" ⚠️  < 1 MB — CUDA/TRT libs not bundled yet!"
    fi
    echo "   ✅ ${JAR_NAME} (${JAR_SIZE})${WARN}"
}

build_native_jar "linux-x86_64"
build_native_jar "linux-arm64"
build_native_jar "macosx-arm64"

# ============================================
# 3. 构建 platform 聚合 JAR
# ============================================
echo "📦 Building platform aggregator JAR..."
PLATFORM_JAR="${ARTIFACT_ID}-platform-${VERSION}.jar"
TEMP_DIR=$(mktemp -d)

mkdir -p "${TEMP_DIR}/META-INF/maven/${GROUP_ID}/${ARTIFACT_ID}-platform"

# 生成 pom.xml (嵌入到 JAR 中)
cat > "${TEMP_DIR}/META-INF/maven/${GROUP_ID}/${ARTIFACT_ID}-platform/pom.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>${GROUP_ID}</groupId>
    <artifactId>${ARTIFACT_ID}-platform</artifactId>
    <version>${VERSION}</version>
    <name>TensorRT-LLM JavaCPP Platform</name>
    <dependencies>
        <dependency>
            <groupId>${GROUP_ID}</groupId>
            <artifactId>${ARTIFACT_ID}</artifactId>
            <version>${VERSION}</version>
        </dependency>
        <dependency>
            <groupId>${GROUP_ID}</groupId>
            <artifactId>${ARTIFACT_ID}</artifactId>
            <version>${VERSION}</version>
            <classifier>linux-x86_64</classifier>
        </dependency>
        <dependency>
            <groupId>${GROUP_ID}</groupId>
            <artifactId>${ARTIFACT_ID}</artifactId>
            <version>${VERSION}</version>
            <classifier>linux-arm64</classifier>
        </dependency>
        <dependency>
            <groupId>${GROUP_ID}</groupId>
            <artifactId>${ARTIFACT_ID}</artifactId>
            <version>${VERSION}</version>
            <classifier>macosx-arm64</classifier>
        </dependency>
    </dependencies>
</project>
EOF

cat > "${TEMP_DIR}/META-INF/maven/${GROUP_ID}/${ARTIFACT_ID}-platform/pom.properties" <<EOF
version=${VERSION}
groupId=${GROUP_ID}
artifactId=${ARTIFACT_ID}-platform
EOF

cd "${TEMP_DIR}"
jar cf "${OUTPUT_DIR}/${PLATFORM_JAR}" .
rm -rf "${TEMP_DIR}"
echo "   ✅ ${PLATFORM_JAR}"

# ============================================
# 4. 安装到本地 Maven 仓库
# ============================================
echo ""
echo "📥 Installing to local Maven repository..."
cd "${BASE_DIR}"

# 安装核心 JAR
mvn install:install-file \
    -Dfile="${OUTPUT_DIR}/${ARTIFACT_ID}-${VERSION}.jar" \
    -DgroupId=${GROUP_ID} -DartifactId=${ARTIFACT_ID} \
    -Dversion=${VERSION} -Dpackaging=jar -q

# 安装 classifier JAR
for PLATFORM in linux-x86_64 linux-arm64 macosx-arm64; do
    mvn install:install-file \
        -Dfile="${OUTPUT_DIR}/${ARTIFACT_ID}-${VERSION}-${PLATFORM}.jar" \
        -DgroupId=${GROUP_ID} -DartifactId=${ARTIFACT_ID} \
        -Dversion=${VERSION} -Dpackaging=jar -Dclassifier=${PLATFORM} -q
done

# 安装 platform JAR
mvn install:install-file \
    -Dfile="${OUTPUT_DIR}/${PLATFORM_JAR}" \
    -DgroupId=${GROUP_ID} -DartifactId=${ARTIFACT_ID}-platform \
    -Dversion=${VERSION} -Dpackaging=jar \
    -DpomFile="${OUTPUT_DIR}/${PLATFORM_JAR}" -q 2>/dev/null || \
mvn install:install-file \
    -Dfile="${OUTPUT_DIR}/${PLATFORM_JAR}" \
    -DgroupId=${GROUP_ID} -DartifactId=${ARTIFACT_ID}-platform \
    -Dversion=${VERSION} -Dpackaging=jar -q

echo "   ✅ All JARs installed to ~/.m2/repository"

# ============================================
# 5. 输出摘要
# ============================================
echo ""
echo "============================================"
echo "✅ Build Complete!"
echo "============================================"
echo ""
echo "Generated JARs in ${OUTPUT_DIR}/:"
ls -lh "${OUTPUT_DIR}/"*.jar
echo ""
echo "Maven coordinates:"
echo "  <!-- 只引入 Java API (不含 native 库) -->"
echo "  <dependency>"
echo "    <groupId>${GROUP_ID}</groupId>"
echo "    <artifactId>${ARTIFACT_ID}</artifactId>"
echo "    <version>${VERSION}</version>"
echo "  </dependency>"
echo ""
echo "  <!-- 引入所有平台 native 库 (推荐) -->"
echo "  <dependency>"
echo "    <groupId>${GROUP_ID}</groupId>"
echo "    <artifactId>${ARTIFACT_ID}-platform</artifactId>"
echo "    <version>${VERSION}</version>"
echo "  </dependency>"
echo ""
echo "  <!-- 只引入特定平台 -->"
echo "  <dependency>"
echo "    <groupId>${GROUP_ID}</groupId>"
echo "    <artifactId>${ARTIFACT_ID}</artifactId>"
echo "    <version>${VERSION}</version>"
echo "    <classifier>linux-x86_64</classifier>"
echo "  </dependency>"

