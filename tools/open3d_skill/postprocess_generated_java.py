#!/usr/bin/env python3
import argparse
import re
from pathlib import Path

# Replace parser-leaked C++ template types with Pointer so Java can compile.
STD_TYPE_RE = re.compile(r"std::[A-Za-z0-9_:<>,\s\*\&]+")
EIGEN_TYPE_RE = re.compile(r"Eigen::[A-Za-z0-9_:<>,\s\*\&]+")


def sanitize_java(text: str) -> str:
    text = STD_TYPE_RE.sub("Pointer", text)
    text = EIGEN_TYPE_RE.sub("Pointer", text)

    # Normalize common broken annotation combinations after replacement.
    text = text.replace("@StdVector Pointer", "Pointer")
    text = text.replace("(Pointer setter)", "(Pointer setter)")

    # Avoid duplicate imports if user reruns.
    if "import org.bytedeco.javacpp.Pointer;" not in text:
        text = text.replace(
            "import org.bytedeco.javacpp.*;",
            "import org.bytedeco.javacpp.*;\nimport org.bytedeco.javacpp.Pointer;",
        )
    return text


def process_dir(java_root: Path) -> int:
    count = 0
    for f in java_root.rglob("*.java"):
        text = f.read_text(encoding="utf-8", errors="ignore")
        new_text = sanitize_java(text)
        if new_text != text:
            f.write_text(new_text, encoding="utf-8")
            count += 1
    return count


def main() -> int:
    parser = argparse.ArgumentParser(description="Postprocess JavaCPP generated Open3D Java files")
    parser.add_argument("--java-root", required=True, help="directory containing generated org/bytedeco/open3d")
    args = parser.parse_args()

    root = Path(args.java_root)
    if not root.exists():
        raise SystemExit(f"missing java root: {root}")

    changed = process_dir(root)
    print(f"[OK] postprocess changed files: {changed}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

