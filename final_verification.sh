#!/bin/bash
# 最终验证脚本 - 检查所有15个Layer文件

cd /Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge

echo "=========================================="
echo "TensorRT-LLM Layer类最终验证报告"
echo "=========================================="
echo ""

LAYER_DIR="src/main/java/org/bytedeco/tensorrt_llm"

# 定义15个Layer类名称
declare -a LAYERS=(
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

echo "检查每个Layer文件："
echo ""

TOTAL=0
FOUND=0

for layer in "${LAYERS[@]}"; do
    file="$LAYER_DIR/${layer}.java"
    TOTAL=$((TOTAL + 1))

    if [ -f "$file" ]; then
        lines=$(wc -l < "$file" 2>/dev/null | xargs)
        size=$(du -h "$file" 2>/dev/null | awk '{print $1}')
        echo "✓ ${layer:30}.java       $lines lines    $size"
        FOUND=$((FOUND + 1))
    else
        echo "✗ ${layer}.java                 MISSING"
    fi
done

echo ""
echo "=========================================="
echo "统计结果"
echo "=========================================="
echo "已找到: $FOUND / $TOTAL 个Layer文件"
echo ""

if [ $FOUND -eq $TOTAL ]; then
    echo "✓✓✓ 所有15个Layer文件都存在！"
    echo ""
    echo "开始编译测试..."
    mvn clean compile -DskipTests > /tmp/compile_test.log 2>&1
    if [ $? -eq 0 ]; then
        echo "✓ 编译成功"
        mvn package -DskipTests > /tmp/package_test.log 2>&1
        if [ $? -eq 0 ]; then
            echo "✓ 打包成功"
            echo ""
            echo "成功！所有Layer文件都已正确生成且可编译打包。"
            JAR_FILE=$(find target -name "*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" | head -1)
            if [ -n "$JAR_FILE" ]; then
                JAR_SIZE=$(du -h "$JAR_FILE" | awk '{print $1}')
                echo "JAR: $JAR_FILE ($JAR_SIZE)"
            fi
        else
            echo "✗ 打包失败"
            tail -20 /tmp/package_test.log
        fi
    else
        echo "✗ 编译失败"
        tail -20 /tmp/compile_test.log
    fi
else
    echo "✗✗✗ 缺少 $((TOTAL - FOUND)) 个Layer文件！"
fi

echo ""
echo "=========================================="

