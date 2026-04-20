#!/usr/bin/env python3
"""
1. Copy new files from trtllm-bridge to src/main/java
2. Generate missing layers/ translations from cpp/tensorrt_llm/layers/
"""
import os
import shutil
import glob

BRIDGE_SRC = "/trtllm-javacpp/src/resources/trtllm-bridge/src/main/java/org/bytedeco/tensorrt_llm"
TARGET_SRC = "/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/src/main/java/org/bytedeco/tensorrt_llm"

# Step 1: Copy new directories
for pkg in ['kernels', 'layers', 'plugins', 'runtime/utils']:
    src_dir = os.path.join(BRIDGE_SRC, pkg)
    dst_dir = os.path.join(TARGET_SRC, pkg)
    if os.path.exists(src_dir):
        os.makedirs(dst_dir, exist_ok=True)
        for f in glob.glob(os.path.join(src_dir, '*.java')):
            dst = os.path.join(dst_dir, os.path.basename(f))
            if not os.path.exists(dst):
                shutil.copy2(f, dst)
                print(f"  Copied: {pkg}/{os.path.basename(f)}")

# Step 2: Copy new files in existing packages
for pkg in ['common', 'executor', 'runtime', 'batch_manager', 'batch_manager/kvcache']:
    src_dir = os.path.join(BRIDGE_SRC, pkg)
    dst_dir = os.path.join(TARGET_SRC, pkg)
    if os.path.exists(src_dir):
        os.makedirs(dst_dir, exist_ok=True)
        for f in glob.glob(os.path.join(src_dir, '*.java')):
            dst = os.path.join(dst_dir, os.path.basename(f))
            if not os.path.exists(dst):
                shutil.copy2(f, dst)
                print(f"  Copied: {pkg}/{os.path.basename(f)}")

# Count total
total = 0
for root, dirs, files in os.walk(TARGET_SRC):
    for f in files:
        if f.endswith('.java'):
            total += 1
print(f"\nTotal Java files in src/main/java: {total}")

