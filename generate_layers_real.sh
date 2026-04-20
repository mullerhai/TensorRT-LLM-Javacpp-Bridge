#!/bin/bash
# 真正生成Layer类并验证的脚本
set -x  # 显示执行的每个命令

PROJECT=/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge
cd "$PROJECT"

echo "=========================================="
echo "真实生成并验证Layer类"
echo "=========================================="

# Step 1: 清理
echo "[Step 1] Cleaning..."
rm -rf target/
rm -rf src/main/java/org/bytedeco/tensorrt_llm/*Layer.java

# Step 2: 编译preset
echo "[Step 2] Compiling preset..."
timeout 120 mvn clean compile -DskipTests > /tmp/compile.log 2>&1
COMPILE_STATUS=$?
echo "Compile exit code: $COMPILE_STATUS"
if [ $COMPILE_STATUS -ne 0 ]; then
    echo "编译失败！查看日志："
    tail -30 /tmp/compile.log
    exit 1
fi

# Step 3: 验证preset class存在
echo "[Step 3] Verifying preset class..."
if [ ! -f "target/classes/tensorrt_llm/presets/TRTLLMFullConfig.class" ]; then
    echo "ERROR: Preset class not found!"
    exit 1
fi
echo "Preset class found: OK"

# Step 4: 运行JavaCPP parse
echo "[Step 4] Running JavaCPP parse..."
timeout 180 mvn org.bytedeco:javacpp:parse > /tmp/parse.log 2>&1
PARSE_STATUS=$?
echo "Parse exit code: $PARSE_STATUS"
if [ $PARSE_STATUS -ne 0 ]; then
    echo "Parse失败！查看日志："
    tail -50 /tmp/parse.log
    exit 1
fi

# Step 5: 验证生成的Layer文件
echo "[Step 5] Verifying generated Layer files..."
echo ""
echo "=== Layer文件列表 ==="
LAYER_FILES=$(find src/main/java/org/bytedeco/tensorrt_llm -name "*Layer.java" -type f 2>/dev/null)
LAYER_COUNT=$(echo "$LAYER_FILES" | grep -c "." || echo 0)

echo "生成的Layer文件数量: $LAYER_COUNT"
echo ""

if [ "$LAYER_COUNT" -eq 0 ]; then
    echo "ERROR: 没有生成任何Layer文件！"
    echo "检查parse日志："
    tail -100 /tmp/parse.log
    exit 1
fi

# 显示每个Layer文件的详细信息
echo "Layer文件详情："
for file in $LAYER_FILES; do
    if [ -f "$file" ]; then
        name=$(basename "$file" .java)
        lines=$(wc -l < "$file" 2>/dev/null || echo 0)
        printf "  ✓ %-35s %4d lines\n" "$name" "$lines"
    fi
done

# Step 6: 检查必须的Layer文件
echo ""
echo "=== 检查必需的15个Layer文件 ==="
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

MISSING_COUNT=0
for layer in "${REQUIRED_LAYERS[@]}"; do
    file="src/main/java/org/bytedeco/tensorrt_llm/${layer}.java"
    if [ -f "$file" ]; then
        lines=$(wc -l < "$file")
        printf "  ✓ %-35s exists (%d lines)\n" "$layer" "$lines"
    else
        printf "  ✗ %-35s MISSING\n" "$layer"
        MISSING_COUNT=$((MISSING_COUNT + 1))
    fi
done

if [ $MISSING_COUNT -gt 0 ]; then
    echo ""
    echo "ERROR: 缺少 $MISSING_COUNT 个Layer文件！"
    exit 1
fi

# Step 7: 尝试编译
echo ""
echo "=== 编译所有代码 ==="
mvn compile -DskipTests > /tmp/compile2.log 2>&1
if [ $? -ne 0 ]; then
    echo "编译失败！"
    grep ERROR /tmp/compile2.log | head -20
    exit 1
fi
echo "编译成功！"

# Step 8: 尝试打包
echo ""
echo "=== 打包JAR ==="
mvn package -DskipTests > /tmp/package.log 2>&1
if [ $? -ne 0 ]; then
    echo "打包失败！"
    grep ERROR /tmp/package.log | head -20
    exit 1
fi
echo "打包成功！"

# 最终报告
echo ""
echo "=========================================="
echo "✓✓✓ 所有Layer类已成功生成！✓✓✓"
echo "=========================================="
echo "生成的Layer类: $LAYER_COUNT 个"
echo "JAR文件: $(ls -lh target/*.jar | grep -v original | awk '{print $9, $5}')"
echo ""

