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
#   - trtllm-bridge 已编译: cd trtllm-bridge && mvn package
#   - (可选) Linux native 库已放入对应的 resources 目录
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
# 1. 构建核心 Java 绑定 JAR
# ============================================
echo "📦 Building core Java bindings JAR..."
cd "${BASE_DIR}/trtllm-bridge"
mvn clean package -DskipTests=true -q
cp "target/tensorrt-llm-${VERSION}.jar" "${OUTPUT_DIR}/"
echo "   ✅ ${ARTIFACT_ID}-${VERSION}.jar"

# ============================================
# 2. 构建 native classifier JAR 文件
# ============================================
build_native_jar() {
    local PLATFORM=$1
    local NATIVE_DIR="${BASE_DIR}/trtllm-native/${PLATFORM}/src/main/resources"
    local JAR_NAME="${ARTIFACT_ID}-${VERSION}-${PLATFORM}.jar"
    local TEMP_DIR=$(mktemp -d)

    echo "📦 Building native JAR: ${PLATFORM}..."

    # 检查是否有 native 库文件
    local SO_COUNT=$(find "${NATIVE_DIR}" -name "*.so" -o -name "*.dylib" 2>/dev/null | wc -l | tr -d ' ')

    if [ "${SO_COUNT}" = "0" ]; then
        echo "   ⚠️  No native libraries found for ${PLATFORM}"
        echo "   Creating stub JAR with README only"
    else
        echo "   Found ${SO_COUNT} native libraries"
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
    echo "   ✅ ${JAR_NAME}"
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

