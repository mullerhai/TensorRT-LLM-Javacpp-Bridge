#!/usr/bin/env zsh
# 一键用 Maven 调用 JavaCPP Builder 生成并编译绑定（针对 macOS）
# 使用前请在 pom.xml 中确认 <trtllm.headers> 指向你的 C++ 源根。

set -euo pipefail

ROOT_DIR=$(cd "$(dirname "$0")/.." && pwd)
cd "$ROOT_DIR"

echo "生成 JavaCPP 绑定，headers = ${trtllm_headers:-$TRTLLM_HEADERS}"

# 你可以通过环境变量覆盖 pom 中的 trtllm.headers：
if [[ -n "${TRTLLM_HEADERS:-}" ]]; then
  echo "使用环境变量 TRTLLM_HEADERS=$TRTLLM_HEADERS"
  MAVEN_INCLUDES="-Dtrtllm.headers=$TRTLLM_HEADERS"
else
  MAVEN_INCLUDES=""
fi

# 针对 macosx-arm64 生成（如需 x86_64，替换平台）
mvn org.bytedeco:javacpp:1.5.8:build -Djavacpp.platform=macosx-arm64 $MAVEN_INCLUDES

echo "生成完成。生成的 Java 将放在 target/generated-sources/javacpp 下，本地库放在 target/native 或 Maven 输出目录。"

