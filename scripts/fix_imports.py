#!/usr/bin/env python3
"""修复移动后子包内文件的交叉 import"""
import os

BASE = "/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/src/main/java/org/bytedeco/tensorrt_llm"
OLD_PKG = "org.bytedeco.tensorrt_llm"

# 所有子包
SUB_PKGS = [
    "executor", "executor.kvcache", "runtime", "batch_manager",
    "batch_manager.kvcache", "common", "mpi", "nvinfer"
]

ALL_IMPORTS = "\n".join(f"import {OLD_PKG}.{p}.*;" for p in SUB_PKGS)
ALL_IMPORTS = f"import {OLD_PKG}.*;\n{ALL_IMPORTS}"

def fix_file(fpath):
    with open(fpath, 'r') as f:
        content = f.read()

    # 替换现有的 import org.bytedeco.tensorrt_llm.* 行，加上所有子包
    old_import = f"import {OLD_PKG}.*;"
    if old_import in content and f"import {OLD_PKG}.executor.*;" not in content:
        content = content.replace(old_import, ALL_IMPORTS)

    with open(fpath, 'w') as f:
        f.write(content)

def walk_and_fix(root):
    count = 0
    for dirpath, _, filenames in os.walk(root):
        for fn in filenames:
            if fn.endswith(".java"):
                fix_file(os.path.join(dirpath, fn))
                count += 1
    return count

if __name__ == "__main__":
    n = walk_and_fix(BASE)
    print(f"✅ 修复了 {n} 个文件的 import")

