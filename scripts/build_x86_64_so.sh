#!/bin/bash
#
# 编译 Linux x86_64 的 JNI .so 文件并重新打包 native JAR
#
# 先等 Docker 镜像建好:
#   docker build --platform linux/amd64 -t trtllm-gcc-x86 -f docker/Dockerfile.x86_64-gcc docker/
#
# 然后运行此脚本:
#   bash scripts/build_x86_64_so.sh
#
set -e

BASE_DIR="$(cd "$(dirname "$0")/.." && pwd)"
OUTPUT="$BASE_DIR/trtllm-native/linux-x86_64/src/main/resources/org/bytedeco/tensorrt_llm/linux-x86_64"
TRTLLM_SRC="/Users/mullerzhang/Documents/code/TensorRT-LLM"

echo "=== Building Linux x86_64 libjniTRTLLM.so ==="

docker run --rm --platform linux/amd64 \
    -v "$BASE_DIR/trtllm-bridge/src/main/java:/src:ro" \
    -v "$TRTLLM_SRC/cpp/include:/trtllm-include:ro" \
    -v "$BASE_DIR/cuda-stubs:/cuda-stubs:ro" \
    -v "$OUTPUT:/output" \
    trtllm-gcc-x86 bash -c '
echo "Platform: $(uname -m)"
g++ --version | head -1
g++ -std=c++17 \
    -I/cuda-stubs -I/trtllm-include \
    /src/jniTRTLLM.cpp /src/jnijavacpp.cpp \
    -O2 -fPIC -pthread -shared \
    -Wl,--unresolved-symbols=ignore-all -w \
    -o /output/libjniTRTLLM.so
echo "✅ SUCCESS"
ls -lh /output/libjniTRTLLM.so
file /output/libjniTRTLLM.so
'

echo ""
echo "✅ 验证:"
file "$OUTPUT/libjniTRTLLM.so"
echo ""

# 重新打包 JAR
echo "📦 Rebuilding linux-x86_64 JAR..."
bash "$BASE_DIR/scripts/build_platform_jars.sh"

