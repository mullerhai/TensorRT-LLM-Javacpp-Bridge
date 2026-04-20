#!/bin/bash
# Compile the generated JNI .cpp files into a native library for the current
# platform.  On macOS we use "-undefined dynamic_lookup" so missing TensorRT-LLM
# symbols defer to runtime (when deployed on Linux with libtensorrt_llm.so).
set -e

ROOT=/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge
PLATFORM=${1:-macosx-arm64}
OUT="$ROOT/build-jni/$PLATFORM"

TRTLLM_INC=/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include
STUBS=$ROOT/cuda-stubs

JAVA_HOME_GUESS=$(/usr/libexec/java_home 2>/dev/null || echo "$JAVA_HOME")
JAVA_INC="$JAVA_HOME_GUESS/include"
if [ "$PLATFORM" = "macosx-arm64" ]; then
  JAVA_INC_OS="$JAVA_INC/darwin"
  EXT=dylib
  LINK_FLAGS="-dynamiclib -undefined dynamic_lookup"
  CXX=clang++
  ARCH_FLAGS="-arch arm64"
else
  JAVA_INC_OS="$JAVA_INC/linux"
  EXT=so
  LINK_FLAGS="-shared -Wl,--unresolved-symbols=ignore-all"
  CXX=clang++
  ARCH_FLAGS=""
fi

cd "$OUT"
echo "== compiling JNI for $PLATFORM =="

INCLUDES="-I. -I$STUBS -I$TRTLLM_INC -I$JAVA_INC -I$JAVA_INC_OS"
CXXFLAGS="-std=c++17 -O1 -fPIC -w $ARCH_FLAGS $INCLUDES"

OBJS=()
for f in *.cpp; do
  obj="${f%.cpp}.o"
  echo "  cc $f"
  $CXX $CXXFLAGS -c "$f" -o "$obj" 2>>compile_err.log || {
    echo "    (failed, will retry with permissive mode)"
    $CXX $CXXFLAGS -fpermissive -c "$f" -o "$obj" 2>>compile_err.log || true
  }
  if [ -f "$obj" ]; then OBJS+=("$obj"); fi
done

LIB=libjniTRTLLM.$EXT
echo "== linking $LIB =="
$CXX $ARCH_FLAGS $LINK_FLAGS -o "$LIB" "${OBJS[@]}" 2>>compile_err.log || true

ls -la "$LIB" 2>/dev/null || echo "no lib produced"

