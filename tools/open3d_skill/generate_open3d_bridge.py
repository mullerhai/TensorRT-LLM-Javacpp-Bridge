#!/usr/bin/env python3
import argparse
import shutil
from pathlib import Path
from textwrap import dedent


MODULE_PATHS = {
    "common": ["open3d/utility", "open3d/core"],
    "camera": ["open3d/camera"],
    "geometry": ["open3d/geometry"],
    "io": ["open3d/io"],
    "pipelines": ["open3d/pipelines"],
    "utility": ["open3d/utility"],
    "core": ["open3d/core", "open3d/data"],
    "ml": ["open3d/ml"],
}

SAFE_MODULES = ["common", "camera", "geometry", "io", "pipelines", "utility"]
FULL_MODULES = ["common", "camera", "geometry", "io", "pipelines", "utility", "core", "ml"]

COMMON_HEADERS = [
    "open3d/Macro.h",
    "open3d/Open3DConfig.h",
    "open3d/utility/Eigen.h",
    "open3d/core/SizeVector.h",
]

BLOCKED_HEADERS = {
    "open3d/utility/MiniVec.h",
    "open3d/utility/Overload.h",
    "open3d/ml/impl/misc/NeighborSearchCommon.h",
}

BLOCKED_PREFIXES = (
    "open3d/core/nns/",
)


def collect_headers(include_root: Path, module: str, limit: int, profile: str):
    if module == "common":
        return [h for h in COMMON_HEADERS if (include_root / h).is_file()]
    headers = []
    for rel_root in MODULE_PATHS[module]:
        root = include_root / rel_root
        if not root.exists():
            continue
        for p in sorted(root.rglob("*.h")):
            rel = p.relative_to(include_root).as_posix()
            if "/CUDA/" in rel or rel.endswith(".cuh"):
                continue
            if rel.startswith(BLOCKED_PREFIXES):
                continue
            if rel in BLOCKED_HEADERS:
                continue
            headers.append(rel)
            if len(headers) >= limit:
                return sorted(set(headers))
    return sorted(set(headers))


def gen_pom(group_id: str, artifact_id: str, version: str, javacpp_version: str):
    return dedent(
        f"""\
        <project xmlns="http://maven.apache.org/POM/4.0.0"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
            <modelVersion>4.0.0</modelVersion>
            <groupId>{group_id}</groupId>
            <artifactId>{artifact_id}</artifactId>
            <version>{version}</version>
            <properties>
                <maven.compiler.source>11</maven.compiler.source>
                <maven.compiler.target>11</maven.compiler.target>
                <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                <javacpp.version>{javacpp_version}</javacpp.version>
            </properties>
            <dependencies>
                <dependency>
                    <groupId>org.bytedeco</groupId>
                    <artifactId>javacpp</artifactId>
                    <version>${{javacpp.version}}</version>
                </dependency>
            </dependencies>
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.bytedeco</groupId>
                        <artifactId>javacpp</artifactId>
                        <version>${{javacpp.version}}</version>
                        <configuration>
                            <classPath>${{project.build.outputDirectory}}</classPath>
                            <classOrPackageNames>
                                <classOrPackageName>org.bytedeco.open3d.presets.Open3DCameraConfig</classOrPackageName>
                                <classOrPackageName>org.bytedeco.open3d.presets.Open3DCommonConfig</classOrPackageName>
                                <classOrPackageName>org.bytedeco.open3d.presets.Open3DGeometryConfig</classOrPackageName>
                                <classOrPackageName>org.bytedeco.open3d.presets.Open3DIOConfig</classOrPackageName>
                                <classOrPackageName>org.bytedeco.open3d.presets.Open3DPipelinesConfig</classOrPackageName>
                                <classOrPackageName>org.bytedeco.open3d.presets.Open3DUtilityConfig</classOrPackageName>
                                <classOrPackageName>org.bytedeco.open3d.presets.Open3DCoreConfig</classOrPackageName>
                                <classOrPackageName>org.bytedeco.open3d.presets.Open3DMLConfig</classOrPackageName>
                            </classOrPackageNames>
                        </configuration>
                    </plugin>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-compiler-plugin</artifactId>
                        <version>3.11.0</version>
                    </plugin>
                </plugins>
            </build>
        </project>
        """
    )


def preset_class_name(module: str) -> str:
    special = {"io": "IO", "ml": "ML"}
    token = special.get(module, module.capitalize())
    return f"Open3D{token}Config"


def global_class_name(module: str) -> str:
    special = {"io": "IO", "ml": "ML"}
    token = special.get(module, module.capitalize())
    return f"Open3D{token}"


def target_package(module: str) -> str:
    if module == "common":
        return "org.bytedeco.open3d"
    return f"org.bytedeco.open3d.{module}"


def gen_module_preset(module: str, include_root: str, headers):
    class_name = preset_class_name(module)
    global_name = global_class_name(module)
    include_entries = "\n".join([f'                "{h}",' for h in headers])
    return dedent(
        f"""\
        package org.bytedeco.open3d.presets;

        import org.bytedeco.javacpp.*;
        import org.bytedeco.javacpp.annotation.*;
        import org.bytedeco.javacpp.tools.*;

        @Properties(
            value = {{
                @Platform(
                    includepath = {{"{include_root}"}},
                    include = {{
{include_entries}
                    }},
                    link = {{"Open3D"}}
                )
            }},
            target = "{target_package(module)}",
            global = "org.bytedeco.open3d.global.{global_name}"
        )
        public class {class_name} implements InfoMapper {{
            @Override
            public void map(InfoMap infoMap) {{
                infoMap.put(new Info("std::function").skip());
                infoMap.put(new Info("std::variant").skip());
                infoMap.put(new Info("std::unordered_map").skip());
                infoMap.put(new Info("std::unordered_set").skip());
                infoMap.put(new Info("std::tuple").pointerTypes("Pointer").skip());
                infoMap.put(new Info("std::pair").pointerTypes("Pointer").skip());
                infoMap.put(new Info("open3d::utility::Vector2d").pointerTypes("Vector2d"));
                infoMap.put(new Info("open3d::utility::Vector2i").pointerTypes("Vector2i"));
                infoMap.put(new Info("open3d::utility::Vector3d").pointerTypes("Vector3d"));
                infoMap.put(new Info("open3d::utility::Vector3i").pointerTypes("Vector3i"));
                infoMap.put(new Info("open3d::utility::Vector4i").pointerTypes("Vector4i"));
                infoMap.put(new Info("open3d::utility::Matrix3d").pointerTypes("Matrix3d"));
                infoMap.put(new Info("open3d::utility::Matrix4d").pointerTypes("Matrix4d"));
                infoMap.put(new Info("open3d::core::SizeVector").pointerTypes("SizeVector"));
                infoMap.put(new Info("Eigen::").pointerTypes("Pointer").skip());
                infoMap.put(new Info("open3d::utility::nullopt").skip());
                infoMap.put(new Info("size_t").valueTypes("long").pointerTypes("SizeTPointer"));
            }}
        }}
        """
    )


def gen_open3d_anchor():
    return dedent(
        """\
        package org.bytedeco.open3d;

        public final class Open3DAnchor {
            private Open3DAnchor() {}

            public static String project() {
                return "open3d-javacpp-bridge";
            }
        }
        """
    )


def gen_example_pointcloud():
    return dedent(
        """\
        package org.bytedeco.open3d.examples;

        public class ReadPointCloudExample {
            public static void main(String[] args) {
                // Demonstrates module-specific global class lookup after JavaCPP parse.
                String ioGlobal = "org.bytedeco.open3d.global.Open3DIO";
                String geoGlobal = "org.bytedeco.open3d.global.Open3DGeometry";
                try {
                    Class.forName(ioGlobal);
                    Class.forName(geoGlobal);
                    System.out.println("Open3D IO + Geometry global classes found.");
                } catch (ClassNotFoundException e) {
                    System.out.println("Run javacpp:parse first to generate global bindings.");
                }
            }
        }
        """
    )


def gen_example_registration():
    return dedent(
        """\
        package org.bytedeco.open3d.examples;

        public class ICPRegistrationExample {
            public static void main(String[] args) {
                // Inspired by Open3D tutorial flow: read source/target then run ICP registration.
                String pipelinesGlobal = "org.bytedeco.open3d.global.Open3DPipelines";
                try {
                    Class.forName(pipelinesGlobal);
                    System.out.println("Open3D pipelines global class found.");
                    System.out.println("Next: call generated APIs under org.bytedeco.open3d.pipelines");
                } catch (ClassNotFoundException e) {
                    System.out.println("Run javacpp:parse first to generate pipeline bindings.");
                }
            }
        }
        """
    )


def gen_readme(profile: str, module_counts):
    lines = [f"- {m}: {c} headers" for m, c in module_counts.items()]
    modules_block = "\n".join(lines)
    return dedent(
        f"""\
        # Open3D JavaCPP Bridge (Modular Generated)

        This project is generated by the reusable JavaCPP transpile skill.

        Profile: `{profile}`

        ## Module Header Coverage

        {modules_block}

        ## Parse

        ```bash
        mvn -q org.bytedeco:javacpp:parse \
          -Djavacpp.parser.skip=false
        ```

        Generated outputs are placed by JavaCPP under project-root `org/bytedeco/open3d/...`
        with module subdirectories (`camera`, `geometry`, `io`, `pipelines`, `utility`, etc.).

        ## Example Runner

        ```bash
        mvn -q -DskipTests=true compile
        java -cp target/classes org.bytedeco.open3d.examples.ReadPointCloudExample
        ```
        """
    )


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate modular Open3D JavaCPP bridge project")
    parser.add_argument("--open3d-root", required=True)
    parser.add_argument("--output", required=True)
    parser.add_argument("--group-id", default="org.bytedeco")
    parser.add_argument("--artifact-id", default="open3d")
    parser.add_argument("--version", default="0.19.0-1.5.13")
    parser.add_argument("--javacpp-version", default="1.5.13")
    parser.add_argument("--header-limit", type=int, default=220)
    parser.add_argument("--profile", choices=["safe", "full"], default="safe")
    args = parser.parse_args()

    open3d_root = Path(args.open3d_root)
    include_root = open3d_root / "cpp"
    if not include_root.is_dir():
        raise SystemExit(f"missing include root: {include_root}")

    modules = SAFE_MODULES if args.profile == "safe" else FULL_MODULES

    out = Path(args.output)
    java_dir = out / "src/main/java/org/bytedeco/open3d"
    preset_dir = java_dir / "presets"
    examples_dir = java_dir / "examples"
    out.mkdir(parents=True, exist_ok=True)

    # Always reset Java source tree to avoid stale generated bindings from previous runs.
    if java_dir.exists():
        shutil.rmtree(java_dir)

    preset_dir.mkdir(parents=True, exist_ok=True)
    examples_dir.mkdir(parents=True, exist_ok=True)


    (out / "pom.xml").write_text(
        gen_pom(args.group_id, args.artifact_id, args.version, args.javacpp_version),
        encoding="utf-8",
    )

    module_counts = {}
    for module in FULL_MODULES:
        headers = collect_headers(include_root, module, args.header_limit, args.profile)
        module_counts[module] = len(headers)
        if not headers:
            headers = ["open3d/Macro.h"]
        (preset_dir / f"{preset_class_name(module)}.java").write_text(
            gen_module_preset(module, str(include_root), headers), encoding="utf-8"
        )

    (java_dir / "Open3DAnchor.java").write_text(gen_open3d_anchor(), encoding="utf-8")
    (examples_dir / "ReadPointCloudExample.java").write_text(gen_example_pointcloud(), encoding="utf-8")
    (examples_dir / "ICPRegistrationExample.java").write_text(gen_example_registration(), encoding="utf-8")
    (out / "README.md").write_text(gen_readme(args.profile, module_counts), encoding="utf-8")

    print(f"[OK] project generated at: {out}")
    for m in FULL_MODULES:
        print(f"[OK] module {m}: {module_counts[m]} headers")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

