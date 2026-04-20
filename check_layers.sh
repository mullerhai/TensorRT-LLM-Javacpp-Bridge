#!/bin/bash
# 最终验证：直接检查文件是否存在
cd /Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge

echo "====================================="
echo "Layer文件真实性验证"
echo "====================================="
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

TOTAL=0
FOUND=0

for layer in "${LAYERS[@]}"; do
    file="src/main/java/org/bytedeco/tensorrt_llm/${layer}.java"
    TOTAL=$((TOTAL + 1))
    if [ -f "$file" ]; then
        lines=$(wc -l < "$file" | xargs)
        size=$(ls -lh "$file" | awk '{print $5}')
        printf "✓ %-35s %5s lines  %6s\n" "${layer}.java" "$lines" "$size"
        FOUND=$((FOUND + 1))
    else
        printf "✗ %-35s NOT FOUND\n" "${layer}.java"
    fi
done

echo ""
echo "====================================="
echo "汇总"
echo "====================================="
echo "总计需要: $TOTAL 个Layer类"
echo "实际生成: $FOUND 个Layer类"
echo "缺失:     $((TOTAL - FOUND)) 个Layer类"
echo ""

if [ $FOUND -eq $TOTAL ]; then
    echo "✓✓✓ 所有Layer类已生成！✓✓✓"
    exit 0
else
    echo "✗✗✗ 还有Layer类未生成 ✗✗✗"
    exit 1
fi

