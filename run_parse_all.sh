#!/bin/bash
set -e
cd /Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge

JAVACPP_JAR="$HOME/.m2/repository/org/bytedeco/javacpp/1.5.13/javacpp-1.5.13.jar"
CP="target/classes:$JAVACPP_JAR"
OUT="src/main/java"

CONFIGS=(
    "tensorrt_llm.presets.CommonConfig"
    "tensorrt_llm.presets.RuntimeConfig"
    "tensorrt_llm.presets.BatchmanagerConfig"
    "tensorrt_llm.presets.LayersConfig"
    "tensorrt_llm.presets.ExecutorConfig"
    "tensorrt_llm.presets.KernelsConfig"
    "tensorrt_llm.presets.PluginsConfig"
    "tensorrt_llm.presets.ThopConfig"
    "tensorrt_llm.presets.CutlassextensionsConfig"
)

for cfg in "${CONFIGS[@]}"; do
    echo "========================================"
    echo "Parsing: $cfg"
    echo "========================================"
    java -cp "$CP" org.bytedeco.javacpp.tools.Builder -classpath "$CP" -d "$OUT" -nocompile -nogenerate "$cfg" 2>&1 || {
        echo "FAILED: $cfg"
    }
    echo ""
done

echo "Done parsing all configs."
