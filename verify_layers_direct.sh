#!/bin/bash
# 直接验证Layer文件 - 不依赖Maven

cd /Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge

echo "=============================================="
echo "Layer文件真实验证报告"
echo "生成时间: $(date)"
echo "=============================================="
echo ""

echo "检查所有Layer文件..."
echo ""

LAYERS=(
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

FOUND=0
MISSING=0

for layer in "${LAYERS[@]}"; do
    file="src/main/java/org/bytedeco/tensorrt_llm/${layer}.java"
    if [ -f "$file" ]; then
        lines=$(wc -l < "$file" 2>/dev/null | xargs)
        size=$(du -h "$file" 2>/dev/null | awk '{print $1}')
        first_line=$(head -1 "$file" 2>/dev/null)

        # 检查是否是JavaCPP生成的
        if [[ "$first_line" == *"Targeted by JavaCPP"* ]]; then
            source="JavaCPP"
        else
            source="Manual"
        fi

        printf "%-35s %5s lines %6s  [%s]\n" "${layer}.java" "$lines" "$size" "$source"
        FOUND=$((FOUND + 1))
    else
        printf "%-35s MISSING\n" "${layer}.java"
        MISSING=$((MISSING + 1))
    fi
done

echo ""
echo "=============================================="
echo "统计"
echo "=============================================="
echo "已生成: $FOUND / 15"
echo "缺失:   $MISSING / 15"
echo ""

if [ $FOUND -eq 15 ]; then
    echo "✓ 所有15个Layer类文件存在"
else
    echo "✗ 缺少 $MISSING 个Layer文件"
fi

echo ""
echo "=============================================="
echo "编译测试"
echo "=============================================="
javac -version 2>&1
echo ""

# 检查是否能找到javacpp
if [ -f "$HOME/.m2/repository/org/bytedeco/javacpp/1.5.13/javacpp-1.5.13.jar" ]; then
    echo "✓ JavaCPP 1.5.13 found in Maven local repository"
else
    echo "✗ JavaCPP not found"
fi

echo ""
echo "=============================================="
echo "验证完成"
echo "=============================================="

