#!/bin/bash
# Generate JNI glue from the 656 parsed Java bindings and compile to a shared lib.
set -e

ROOT=/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge
M2=$HOME/.m2/repository
PLATFORM=${1:-macosx-arm64}
NOCOMPILE=${2:-}

OUT="$ROOT/build-jni/$PLATFORM"
mkdir -p "$OUT"

CP="$ROOT/target/classes:$ROOT/target/trtllm-bridge-1.0.0.jar"
CP="$CP:$M2/org/bytedeco/javacpp/1.5.13/javacpp-1.5.13.jar"
CP="$CP:$M2/org/bytedeco/javacpp/1.5.13/javacpp-1.5.13-${PLATFORM}.jar"

echo "== generating JNI for $PLATFORM into $OUT =="
cd "$OUT"

java -jar "$M2/org/bytedeco/javacpp/1.5.13/javacpp-1.5.13.jar" \
  -cp "$CP" \
  -d . \
  $NOCOMPILE \
  tensorrt_llm.global.Common \
  tensorrt_llm.global.Batchmanager \
  tensorrt_llm.global.Executor \
  tensorrt_llm.global.Layers \
  tensorrt_llm.global.Kernels \
  tensorrt_llm.global.Plugins \
  tensorrt_llm.global.Thop \
  tensorrt_llm.global.CutlassExtensions \
  tensorrt_llm.global.TrtllmRuntime

