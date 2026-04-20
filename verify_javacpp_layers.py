#!/usr/bin/env python3
"""验证所有JavaCPP生成的Layer文件"""

import os
import glob

BASE_DIR = "/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/src/main/java/org/bytedeco/tensorrt_llm"

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
print("TensorRT-LLM JavaCPP自动生成的Layer文件验证")
print("=" * 80)
print()

total_lines = 0
found_count = 0
javacpp_mark = "// Targeted by JavaCPP"

print(f"{'Layer类名':<40} {'行数':<8} {'JavaCPP生成':<15}")
print("-" * 80)

for layer in REQUIRED_LAYERS:
    filepath = os.path.join(BASE_DIR, f"{layer}.java")

    if os.path.exists(filepath):
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
            lines = len(content.split('\n'))
            is_javacpp = javacpp_mark in content

        total_lines += lines
        found_count += 1
        status = "✅ 是" if is_javacpp else "❌ 否"
        print(f"{layer:<40} {lines:<8} {status:<15}")
    else:
        print(f"{layer:<40} {'缺失':<8} {'❌':<15}")

print()
print("=" * 80)
print(f"总计: {found_count}/{len(REQUIRED_LAYERS)} 个Layer类存在")
print(f"总代码行数: {total_lines} 行")
print("=" * 80)

if found_count == len(REQUIRED_LAYERS):
    print("✅ 所有Layer类都已通过JavaCPP自动生成！")
else:
    print(f"❌ 还有 {len(REQUIRED_LAYERS) - found_count} 个Layer类缺失")

