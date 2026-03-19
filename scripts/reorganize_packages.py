#!/usr/bin/env python3
"""
将 JavaCPP 生成的 Java 类按 @Namespace 重新组织到对应的子包中
"""
import os
import re
import shutil

BASE_DIR = "/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/src/main/java/org/bytedeco/tensorrt_llm"
GLOBAL_FILE = os.path.join(BASE_DIR, "global", "TRTLLM.java")

# Namespace → 子包映射
NS_MAP = {
    "tensorrt_llm::executor::kv_cache": "executor.kvcache",
    "tensorrt_llm::executor":           "executor",
    "tensorrt_llm::runtime":            "runtime",
    "tensorrt_llm::batch_manager::kv_cache_manager": "batch_manager.kvcache",
    "tensorrt_llm::batch_manager":      "batch_manager",
    "tensorrt_llm::mpi":                "mpi",
    "common":                           "common",
    "nvinfer1":                         "nvinfer",
}

OLD_PKG = "org.bytedeco.tensorrt_llm"

def get_namespace(filepath):
    """从 Java 文件中提取 @Namespace 值"""
    with open(filepath, 'r') as f:
        content = f.read()
    m = re.search(r'@Namespace\("([^"]+)"\)', content)
    if m:
        return m.group(1)
    return None

def determine_subpkg(ns):
    """根据 namespace 决定子包"""
    if ns is None:
        return None  # 留在根包
    for prefix, pkg in NS_MAP.items():
        if ns == prefix or ns.startswith(prefix + "::"):
            return pkg
    return None

def move_class(filepath, subpkg):
    """把类移到子包"""
    filename = os.path.basename(filepath)
    classname = filename.replace(".java", "")
    new_pkg = f"{OLD_PKG}.{subpkg}"
    new_dir = os.path.join(BASE_DIR, subpkg.replace(".", "/"))
    os.makedirs(new_dir, exist_ok=True)

    with open(filepath, 'r') as f:
        content = f.read()

    # 替换 package 声明
    content = content.replace(f"package {OLD_PKG};", f"package {new_pkg};")

    # 添加根包和其他子包的 import
    import_block = f"import {OLD_PKG}.*;\n"
    # 在 import 区域后面添加
    if import_block not in content:
        content = content.replace(
            f"package {new_pkg};\n",
            f"package {new_pkg};\n\n{import_block}"
        )

    new_path = os.path.join(new_dir, filename)
    with open(new_path, 'w') as f:
        f.write(content)

    os.remove(filepath)
    return classname, new_pkg

def update_global_file(moved_classes):
    """更新 TRTLLM.java 的 import"""
    with open(GLOBAL_FILE, 'r') as f:
        content = f.read()

    # 找到现有 import 区域，添加子包的 import
    sub_pkgs = sorted(set(pkg for _, pkg in moved_classes.items()))
    new_imports = "\n".join(f"import {pkg}.*;" for pkg in sub_pkgs)

    # 在 "import org.bytedeco.tensorrt_llm.*;" 之后添加子包 import
    old_import = f"import {OLD_PKG}.*;"
    if old_import in content:
        content = content.replace(old_import, f"{old_import}\n{new_imports}")

    with open(GLOBAL_FILE, 'w') as f:
        f.write(content)

def main():
    moved = {}  # classname -> new_pkg
    skipped = []

    # 扫描根包下所有 .java 文件 (排除 global/ 子目录)
    for f in sorted(os.listdir(BASE_DIR)):
        if not f.endswith(".java"):
            continue
        filepath = os.path.join(BASE_DIR, f)
        if not os.path.isfile(filepath):
            continue

        ns = get_namespace(filepath)
        subpkg = determine_subpkg(ns)

        if subpkg:
            classname, new_pkg = move_class(filepath, subpkg)
            moved[classname] = new_pkg
            print(f"  ✅ {classname} → {new_pkg}")
        else:
            skipped.append(f.replace(".java", ""))

    print(f"\n移动了 {len(moved)} 个类")
    print(f"保留在根包: {skipped}")

    # 更新 TRTLLM.java
    update_global_file(moved)
    print("✅ TRTLLM.java 已更新 import")

    # 更新子包内文件的交叉引用
    # 因为所有子包文件都已经有 import org.bytedeco.tensorrt_llm.*
    # 需要额外 import 其他子包
    sub_pkgs = sorted(set(moved.values()))
    for subpkg in sub_pkgs:
        # 子包相对于 BASE_DIR 的路径
        rel_path = subpkg.replace(OLD_PKG + ".", "").replace(".", "/")
        pkg_dir = os.path.join(BASE_DIR, rel_path)
        for f in os.listdir(pkg_dir):
            if not f.endswith(".java"):
                continue
            fpath = os.path.join(pkg_dir, f)
            with open(fpath, 'r') as fh:
                content = fh.read()
            # 添加其他子包的 import
            for other_pkg in sub_pkgs:
                if other_pkg == subpkg:
                    continue
                imp = f"import {OLD_PKG}.{other_pkg}.*;"
                if imp not in content:
                    content = content.replace(
                        f"import {OLD_PKG}.*;\n",
                        f"import {OLD_PKG}.*;\nimport {OLD_PKG}.{other_pkg}.*;\n",
                        1  # 只替换一次
                    )
            with open(fpath, 'w') as fh:
                fh.write(content)

    print("✅ 所有子包交叉引用已更新")

if __name__ == "__main__":
    main()

