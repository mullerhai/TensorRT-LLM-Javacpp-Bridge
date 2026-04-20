#!/bin/bash
# 简化版：直接执行最小命令集
cd /Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge

echo "=== 1. Clean ==="
mvn clean
echo ""

echo "=== 2. Compile preset ==="
mvn compile -DskipTests
echo ""

echo "=== 3. Parse ==="
mvn org.bytedeco:javacpp:parse
echo ""

echo "=== 4. 检查结果 ==="
echo "所有Layer文件:"
find src/main/java/org/bytedeco/tensorrt_llm -name "*Layer.java" -type f | while read f; do
    lines=$(wc -l < "$f")
    echo "  $(basename $f): $lines lines"
done
echo ""

echo "=== 5. 统计 ==="
LAYER_COUNT=$(find src/main/java/org/bytedeco/tensorrt_llm -name "*Layer.java" -type f | wc -l)
echo "Total Layer files: $LAYER_COUNT"

