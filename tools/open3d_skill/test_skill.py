#!/usr/bin/env python3
import argparse
import shutil
import subprocess
import sys
from pathlib import Path

from skill_engine import build_context, load_skill, run_discovery_checks


def run_cmd(cmd, cwd=None):
    print("[RUN]", " ".join(cmd))
    return subprocess.run(cmd, cwd=cwd, check=True)


def main() -> int:
    parser = argparse.ArgumentParser(description="Load and test JavaCPP skill against Open3D")
    parser.add_argument("--skill", required=True)
    parser.add_argument("--open3d-root", required=True)
    parser.add_argument("--output", required=True)
    parser.add_argument("--profile", choices=["safe", "full"], default="safe")
    parser.add_argument("--skip-maven", action="store_true")
    parser.add_argument("--skip-parse", action="store_true")
    parser.add_argument("--materialize-generated", action="store_true")
    parser.add_argument("--verify-package", action="store_true")
    args = parser.parse_args()

    skill = load_skill(args.skill)
    ctx = build_context(
        skill,
        {
            "cpp_root": args.open3d_root,
            "include_root": f"{args.open3d_root}/cpp",
            "output_root": args.output,
        },
    )

    try:
        run_discovery_checks(skill, ctx)
    except RuntimeError as e:
        print("[FAIL] discovery checks")
        print(e)
        return 2

    print("[OK] skill loaded + discovery checks passed")

    generator = Path(__file__).parent / "generate_open3d_bridge.py"
    run_cmd(
        [
            sys.executable,
            str(generator),
            "--open3d-root",
            args.open3d_root,
            "--output",
            args.output,
            "--profile",
            args.profile,
        ]
    )

    preset_file = Path(args.output) / "src/main/java/org/bytedeco/open3d/presets/Open3DGeometryConfig.java"
    if not preset_file.is_file():
        print("[FAIL] modular preset not generated")
        return 3
    print(f"[OK] modular preset generated: {preset_file}")

    if not args.skip_maven:
        run_cmd(["mvn", "-q", "-DskipTests=true", "compile"], cwd=args.output)
        print("[OK] generated project compiles")

    if not args.skip_parse:
        run_cmd(
            [
                "mvn",
                "-q",
                "org.bytedeco:javacpp:parse",
                "-Djavacpp.parser.skip=false",
                f"-Dopen3d.include.path={args.open3d_root}/cpp",
            ],
            cwd=args.output,
        )
        print("[OK] javacpp parse smoke test passed")

    generated_root = Path(args.output) / "org/bytedeco/open3d"
    expected_dirs = ["camera", "geometry", "io", "pipelines", "utility", "global"]
    missing = [d for d in expected_dirs if not (generated_root / d).exists()]
    if missing:
        print("[FAIL] missing generated module dirs:", ", ".join(missing))
        return 4
    print("[OK] generated module dirs exist:", ", ".join(expected_dirs))

    required_types = [
        "Vector3d.java",
        "Vector3i.java",
        "Matrix3d.java",
        "Matrix4d.java",
        "SizeVector.java",
    ]
    missing_types = [t for t in required_types if not (generated_root / t).exists()]
    if missing_types:
        print("[FAIL] missing C++-translated base type wrappers:", ", ".join(missing_types))
        return 5
    print("[OK] required C++-translated base types generated:", ", ".join(required_types))

    if args.materialize_generated:
        src_root = Path(args.output) / "src/main/java/org"
        if src_root.exists():
            shutil.rmtree(src_root)
        shutil.copytree(Path(args.output) / "org", src_root)
        post = Path(__file__).parent / "postprocess_generated_java.py"
        run_cmd([sys.executable, str(post), "--java-root", str(src_root / "bytedeco/open3d")])
        print("[OK] generated bindings materialized into src/main/java and postprocessed")

    if args.verify_package and not args.materialize_generated:
        resources_root = Path(args.output) / "src/main/resources/generated-bindings"
        if resources_root.exists():
            shutil.rmtree(resources_root)
        resources_root.mkdir(parents=True, exist_ok=True)
        shutil.copytree(Path(args.output) / "org", resources_root / "org")
        print("[OK] copied generated bindings into src/main/resources/generated-bindings")

    if args.verify_package:
        run_cmd(["mvn", "-q", "-DskipTests=true", "package"], cwd=args.output)
        print("[OK] package verification passed")

    print("[DONE] skill is loadable and project generation path is verified")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
