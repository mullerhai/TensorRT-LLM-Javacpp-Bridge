#!/bin/bash
#
# build_all.sh — 一键打包所有平台的 TensorRT-LLM Java Bridge JARs
#
# 执行步骤:
#   1. mvn clean package (核心 Java 绑定 JAR)
#   2. 为当前平台打包 native JAR (含依赖库)
#   3. 构建 platform 聚合 JAR
#   4. 安装所有 JAR 到本地 Maven 仓库
#
# 用法:
#   ./scripts/build_all.sh
#
#   # 指定 CUDA 环境 (Linux):
#   TRTLLM_BUILD_DIR=/workspace/TensorRT-LLM/cpp/build \
#   CUDA_LIB_DIR=/usr/local/cuda/lib64 \
#   ./scripts/build_all.sh
#

set -euo pipefail
BASE_DIR="$(cd "$(dirname "$0")/.." && pwd)"
SCRIPTS_DIR="${BASE_DIR}/scripts"
OUTPUT_DIR="${BASE_DIR}/dist"
OS=$(uname -s | tr '[:upper:]' '[:lower:]')

echo "╔══════════════════════════════════════════════════════╗"
echo "║      TensorRT-LLM Java Bridge — Full Build           ║"
echo "╚══════════════════════════════════════════════════════╝"
echo ""

mkdir -p "${OUTPUT_DIR}"

# ── Step 1: 编译核心 Java 绑定 ──────────────────────────────
echo "▶  Step 1/3: Building core Java bindings..."
cd "${BASE_DIR}"
mvn clean package -DskipTests=true -q
JAR_SRC=$(ls target/trtllm-bridge-*.jar 2>/dev/null | head -1)
if [[ -z "$JAR_SRC" ]]; then
    echo "ERROR: mvn package failed, no JAR in target/"; exit 1
fi
cp "${JAR_SRC}" "${OUTPUT_DIR}/tensorrt-llm-0.17.0-1.5.13.jar"
echo "   ✅ Core JAR: tensorrt-llm-0.17.0-1.5.13.jar ($(du -sh "$JAR_SRC" | cut -f1))"

# ── Step 2: 打包 native JAR (含 CUDA 依赖) ──────────────────
echo ""
echo "▶  Step 2/3: Bundling native libraries..."
if [[ "$OS" != "linux" ]]; then
    echo "ERROR: Full native bundling requires a Linux CUDA host."
    echo "       Current host: ${OS}"
    echo "       Use scripts/build_linux_native.sh on Linux, or run in Docker/CI Linux."
    exit 1
fi
bash "${SCRIPTS_DIR}/bundle_native_deps.sh"

# ── Step 3: 构建 platform 聚合 JAR ──────────────────────────
echo ""
echo "▶  Step 3/3: Building platform aggregator JAR..."
bash "${SCRIPTS_DIR}/build_platform_jars.sh" --platform-only 2>/dev/null || \
bash "${SCRIPTS_DIR}/build_platform_jars.sh"

# ── 摘要 ─────────────────────────────────────────────────────
echo ""
echo "══════════════════════════════════════════════════════"
echo "✅ Build Complete!  Output in ${OUTPUT_DIR}/"
echo "══════════════════════════════════════════════════════"
ls -lh "${OUTPUT_DIR}/"*.jar
echo ""
echo "Maven 依赖引用:"
echo '  <dependency>'
echo '    <groupId>org.bytedeco</groupId>'
echo '    <artifactId>tensorrt-llm-platform</artifactId>'
echo '    <version>0.17.0-1.5.13</version>'
echo '  </dependency>'

