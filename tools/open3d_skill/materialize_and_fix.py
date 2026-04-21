#!/usr/bin/env python3
import argparse
import re
import shutil
from pathlib import Path

CPP_TYPE_RE = re.compile(r"\b(?:std|Eigen|open3d)::[A-Za-z0-9_:]+(?:<[^(){};]*>)?")
POINTER_TEMPLATE_RE = re.compile(r"\bPointer\s*<[^>]+>")

COMMON_IMPORTS = [
    "org.bytedeco.open3d.Vector3d",
    "org.bytedeco.open3d.Vector3i",
    "org.bytedeco.open3d.Vector2d",
    "org.bytedeco.open3d.Vector2i",
    "org.bytedeco.open3d.Vector4i",
    "org.bytedeco.open3d.Matrix3d",
    "org.bytedeco.open3d.Matrix4d",
    "org.bytedeco.open3d.SizeVector",
]

REQUIRED_CPP_TYPES = [
    "Vector2d.java",
    "Vector2i.java",
    "Vector3d.java",
    "Vector3i.java",
    "Vector4i.java",
    "Matrix3d.java",
    "Matrix4d.java",
    "SizeVector.java",
]


def sanitize_source(text: str, package_name: str) -> str:
    # Replace parser-leaked C++ type tokens.
    text = CPP_TYPE_RE.sub("Pointer", text)
    text = POINTER_TEMPLATE_RE.sub("Pointer", text)
    text = text.replace("@StdVector Pointer", "Pointer")
    text = text.replace("@ByVal Pointer<", "@ByVal Pointer")

    if package_name != "org.bytedeco.open3d":
        insert_after = "import org.bytedeco.javacpp.annotation.*;"
        idx = text.find(insert_after)
        if idx != -1:
            end = text.find("\n", idx)
            import_block = "\n" + "\n".join([f"import {imp};" for imp in COMMON_IMPORTS]) + "\n"
            if "import org.bytedeco.open3d.Vector3d;" not in text:
                text = text[: end + 1] + import_block + text[end + 1 :]

    return text


def materialize_and_fix(project_root: Path) -> int:
    generated_org = project_root / "org"
    src_org = project_root / "src/main/java/org"
    if not generated_org.exists():
        raise RuntimeError(f"missing generated directory: {generated_org}")

    if src_org.exists():
        shutil.rmtree(src_org)
    shutil.copytree(generated_org, src_org)

    changed = 0
    java_root = src_org / "bytedeco/open3d"
    for f in java_root.rglob("*.java"):
        text = f.read_text(encoding="utf-8", errors="ignore")
        m = re.search(r"^package\s+([\w\.]+);", text, re.MULTILINE)
        pkg = m.group(1) if m else ""
        new_text = sanitize_source(text, pkg)
        if new_text != text:
            f.write_text(new_text, encoding="utf-8")
            changed += 1

    root_pkg_dir = src_org / "bytedeco/open3d"
    missing = [name for name in REQUIRED_CPP_TYPES if not (root_pkg_dir / name).exists()]
    if missing:
        raise RuntimeError(
            "missing C++-translated base types: " + ", ".join(missing) +
            ". Ensure Open3DCommonConfig is parsed and generated before materialization."
        )

    return changed


def main() -> int:
    parser = argparse.ArgumentParser(description="Copy generated Open3D bindings into src/main/java and apply compile fixes")
    parser.add_argument("--project-root", required=True)
    args = parser.parse_args()

    changed = materialize_and_fix(Path(args.project_root))
    print(f"[OK] materialize_and_fix changed files: {changed}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

