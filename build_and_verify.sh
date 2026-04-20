#!/bin/bash
# 编译和打包验证脚本

cd /Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge

echo "=========================================="
echo "开始编译和打包"
echo "=========================================="
echo ""

echo "[1/3] 编译..."
mvn clean compile -DskipTests 2>&1 | tail -5
if [ $? -eq 0 ]; then
    echo "✓ 编译成功"
else
    echo "✗ 编译失败"
    exit 1
fi

echo ""
echo "[2/3] 打包..."
mvn package -DskipTests 2>&1 | tail -5
if [ $? -eq 0 ]; then
    echo "✓ 打包成功"
else
    echo "✗ 打包失败"
    exit 1
fi

echo ""
echo "[3/3] 验证生成的JAR..."
JAR_FILE=$(find target -name "trtllm-bridge-*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" | head -1)
if [ -f "$JAR_FILE" ]; then
    JAR_SIZE=$(du -h "$JAR_FILE" | awk '{print $1}')
    echo "✓ JAR生成成功"
    echo "  文件: $JAR_FILE"
    echo "  大小: $JAR_SIZE"
else
    echo "✗ JAR文件未找到"
    exit 1
fi

echo ""
echo "=========================================="
echo "✓✓✓ 所有步骤完成！✓✓✓"
echo "=========================================="

