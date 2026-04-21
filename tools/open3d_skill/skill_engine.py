#!/usr/bin/env python3
import argparse
import os
import re
import sys
from dataclasses import dataclass
from typing import Any, Dict

import yaml

VAR_RE = re.compile(r"\$\{([^}]+)\}")


@dataclass
class SkillContext:
    values: Dict[str, str]

    def render(self, text: str) -> str:
        def repl(match: re.Match[str]) -> str:
            key = match.group(1)
            return self.values.get(key, match.group(0))

        return VAR_RE.sub(repl, text)


def load_skill(path: str) -> Dict[str, Any]:
    with open(path, "r", encoding="utf-8") as f:
        return yaml.safe_load(f)


def build_context(skill: Dict[str, Any], overrides: Dict[str, str]) -> SkillContext:
    inputs = dict(skill.get("inputs", {}))
    inputs.update({k: v for k, v in overrides.items() if v is not None})
    return SkillContext(values={k: str(v) for k, v in inputs.items()})


def run_discovery_checks(skill: Dict[str, Any], ctx: SkillContext) -> None:
    phases = skill.get("phases", [])
    discover = next((p for p in phases if p.get("id") == "discover"), None)
    if not discover:
        return

    checks = discover.get("checks", [])
    errors = []
    for check in checks:
        ctype = check.get("type")
        value = ctx.render(str(check.get("value", "")))
        if ctype == "path_exists" and not os.path.isdir(value):
            errors.append(f"Missing directory: {value}")
        elif ctype == "file_exists" and not os.path.isfile(value):
            errors.append(f"Missing file: {value}")

    if errors:
        raise RuntimeError("\n".join(errors))


def main() -> int:
    parser = argparse.ArgumentParser(description="Load and validate JavaCPP skill")
    parser.add_argument("--skill", required=True)
    parser.add_argument("--open3d-root", required=False)
    parser.add_argument("--output", required=False)
    args = parser.parse_args()

    skill = load_skill(args.skill)
    overrides = {
        "cpp_root": args.open3d_root,
        "include_root": f"{args.open3d_root}/cpp" if args.open3d_root else None,
        "output_root": args.output,
    }
    ctx = build_context(skill, overrides)

    try:
        run_discovery_checks(skill, ctx)
    except RuntimeError as e:
        print("[FAIL] skill discovery checks failed")
        print(e)
        return 2

    print("[OK] skill loaded")
    print(f"name={skill.get('name')} version={skill.get('version')}")
    print(f"cpp_root={ctx.values.get('cpp_root')}")
    print(f"include_root={ctx.values.get('include_root')}")
    return 0


if __name__ == "__main__":
    sys.exit(main())

