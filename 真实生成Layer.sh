#!/bin/bash
# 真实生成Layer类 - macOS compatible版本
set -e

PROJECT=/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge
cd "$PROJECT"

echo "=========================================="
echo "JavaCPP Layer类生成流程"
echo "=========================================="

# Step 1: 清理
echo "[Step 1] Cleaning..."
mvn clean > /dev/null 2>&1
echo "✓ Clean completed"

# Step 2: 编译preset
echo "[Step 2] Compiling preset..."
mvn compile -DskipTests > /tmp/compile.log 2>&1
if [ $? -ne 0 ]; then
    echo "✗ Compile failed!"
    tail -30 /tmp/compile.log
    exit 1
fi
echo "✓ Preset compiled"

# Step 3: 验证preset class
echo "[Step 3] Verifying preset class..."
if [ ! -f "target/classes/tensorrt_llm/presets/TRTLLMFullConfig.class" ]; then
    echo "✗ Preset class not found!"
    ls -la target/classes/tensorrt_llm/presets/ 2>&1
    exit 1
fi
echo "✓ Preset class exists"

# Step 4: 运行JavaCPP parse
echo "[Step 4] Running JavaCPP parse..."
mvn org.bytedeco:javacpp:parse > /tmp/parse.log 2>&1
if [ $? -ne 0 ]; then
    echo "✗ Parse failed!"
    tail -50 /tmp/parse.log
    exit 1
fi
echo "✓ JavaCPP parse completed"

# Step 5: 验证生成的Layer文件
echo ""
echo "=========================================="
echo "验证生成的Layer文件"
echo "=========================================="

REQUIRED_LAYERS=(
    "BaseLayer"
    "TopKSamplingLayer"
    "TopPSamplingLayer"
    "SamplingLayer"
    "PenaltyLayer"
    "DecodingLayer"
    "DynamicDecodeLayer"
    "BanWordsLayer"
    "StopCriteriaLayer"
    "BeamSearchLayer"
    "MedusaDecodingLayer"
    "EagleDecodingLayer"
    "LookaheadDecodingLayer"
    "ExplicitDraftTokensLayer"
    "ExternalDraftTokensLayer"
)

FOUND_COUNT=0
MISSING_COUNT=0

echo ""
for layer in "${REQUIRED_LAYERS[@]}"; do
    file="src/main/java/org/bytedeco/tensorrt_llm/${layer}.java"
    if [ -f "$file" ]; then
        lines=$(wc -l < "$file" | tr -d ' ')
        printf "  ✓ %-35s %4d lines\n" "$layer.java" "$lines"
        FOUND_COUNT=$((FOUND_COUNT + 1))
    else
        printf "  ✗ %-35s MISSING\n" "$layer.java"
        MISSING_COUNT=$((MISSING_COUNT + 1))
    fi
done

echo ""
echo "=========================================="
echo "统计结果"
echo "=========================================="
echo "  已生成: $FOUND_COUNT / 15"
echo "  缺失:   $MISSING_COUNT / 15"

if [ $MISSING_COUNT -gt 0 ]; then
    echo ""
    echo "✗ 有 $MISSING_COUNT 个Layer文件未生成！"
    echo ""
    echo "查看parse日志以了解原因："
    echo "  cat /tmp/parse.log | grep -i 'topk\|topp\|sampling\|decoding\|layer'"
    exit 1
fi

# Step 6: 编译验证
echo ""
echo "=========================================="
echo "编译验证"
echo "=========================================="
mvn compile -DskipTests > /tmp/compile2.log 2>&1
if [ $? -ne 0 ]; then
    echo "✗ 编译失败！"
    grep ERROR /tmp/compile2.log | head -20
    exit 1
fi
echo "✓ 编译成功"

# Step 7: 打包验证
echo ""
echo "=========================================="
echo "打包验证"
echo "=========================================="
mvn package -DskipTests > /tmp/package.log 2>&1
if [ $? -ne 0 ]; then
    echo "✗ 打包失败！"
    grep ERROR /tmp/package.log | head -20
    exit 1
fi
echo "✓ 打包成功"

# 最终报告
echo ""
echo "=========================================="
echo "✓✓✓ 所有Layer类已成功生成并验证！✓✓✓"
echo "=========================================="
echo ""
echo "生成的JAR:"
ls -lh target/*.jar 2>/dev/null | grep -v original | awk '{print "  " $9 " (" $5 ")"}'
echo ""
echo "验证完成！"

