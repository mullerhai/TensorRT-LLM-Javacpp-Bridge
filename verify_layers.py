#!/usr/bin/env python3
"""
真实验证所有Layer类文件
"""
import os
from datetime import datetime

PROJECT_DIR = "/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge"
LAYER_DIR = os.path.join(PROJECT_DIR, "src/main/java/org/bytedeco/tensorrt_llm")

REQUIRED_LAYERS = [
    "BaseLayer",
    "TopKSamplingLayer",
    "TopPSamplingLayer",
    "SamplingLayer",
    "PenaltyLayer",
    "DecodingLayer",
    "DynamicDecodeLayer",
    "BanWordsLayer",
    "StopCriteriaLayer",
    "BeamSearchLayer",
    "MedusaDecodingLayer",
    "EagleDecodingLayer",
    "LookaheadDecodingLayer",
    "ExplicitDraftTokensLayer",
    "ExternalDraftTokensLayer",
]

print("=" * 80)
print("TensorRT-LLM Layer类真实验证报告")
print(f"验证时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
print("=" * 80)
print()

found_count = 0
missing_count = 0
file_info = []

for layer_name in REQUIRED_LAYERS:
    filepath = os.path.join(LAYER_DIR, f"{layer_name}.java")

    if os.path.exists(filepath):
        # 读取文件统计行数
        with open(filepath, 'r', encoding='utf-8') as f:
            lines = len(f.readlines())

        # 检查是否是JavaCPP生成的
        with open(filepath, 'r', encoding='utf-8') as f:
            first_line = f.readline().strip()
            is_javacpp = "Targeted by JavaCPP" in first_line

        # 获取文件大小
        size_bytes = os.path.getsize(filepath)
        size_kb = size_bytes / 1024

        source = "JavaCPP" if is_javacpp else "Manual"
        status = "✓"

        file_info.append({
            'name': f"{layer_name}.java",
            'lines': lines,
            'size': f"{size_kb:.1f}KB",
            'source': source,
            'status': status
        })

        found_count += 1
    else:
        file_info.append({
            'name': f"{layer_name}.java",
            'lines': 0,
            'size': "N/A",
            'source': "N/A",
            'status': "✗"
        })
        missing_count += 1

# 打印详细信息
print(f"{'文件名':<40} {'状态':<6} {'行数':<8} {'大小':<10} {'来源'}")
print("-" * 80)
for info in file_info:
    print(f"{info['name']:<40} {info['status']:<6} {info['lines']:<8} {info['size']:<10} {info['source']}")

print()
print("=" * 80)
print(f"统计汇总")
print("=" * 80)
print(f"  总计需要: {len(REQUIRED_LAYERS)} 个Layer类")
print(f"  已生成:   {found_count} 个Layer类")
print(f"  缺失:     {missing_count} 个Layer类")
print()

if found_count == len(REQUIRED_LAYERS):
    print("✓✓✓ 所有Layer类文件都已生成！✓✓✓")
else:
    print(f"✗✗✗ 还有 {missing_count} 个Layer类未生成 ✗✗✗")

print()
print("=" * 80)
print("文件位置:")
print(f"  {LAYER_DIR}")
print()
print("下一步:")
print("  1. 编译: mvn compile -DskipTests")
print("  2. 打包: mvn package -DskipTests")
print("  3. 测试: mvn test -Dtest=LayerClassTest")
print("=" * 80)

