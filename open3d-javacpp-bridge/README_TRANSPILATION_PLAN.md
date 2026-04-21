# Open3D JavaCPP Full Transpilation Plan

## Goal

Guarantee the following constraints simultaneously:

1. Full Open3D JavaCPP generated outputs are preserved.
2. `src/main/java/org/bytedeco/open3d` is the canonical location for generated bindings.
3. Core math wrappers (`Vector3d`, `Matrix3d`, `SizeVector`, etc.) are generated from C++ headers, not synthesized manually.
4. Maven `package` succeeds on the default pipeline.

## Architecture

### Stage A: Preset Generation (module + common)

Generator script: `tools/open3d_skill/generate_open3d_bridge.py`

- Produces modular presets: `camera`, `geometry`, `io`, `pipelines`, `utility`, `core`, `ml`
- Produces `Open3DCommonConfig` via C++ headers:
  - `open3d/utility/Eigen.h`
  - `open3d/core/SizeVector.h`
- Maps common aliases in `InfoMap`:
  - `open3d::utility::Vector2d/Vector2i/Vector3d/Vector3i/Vector4i`
  - `open3d::utility::Matrix3d/Matrix4d`
  - `open3d::core::SizeVector`

### Stage B: JavaCPP Parse

Run `mvn org.bytedeco:javacpp:parse` and generate bindings under:

- `open3d-javacpp-bridge/org/bytedeco/open3d/...`

### Stage C: Materialize into src/main/java

Script: `tools/open3d_skill/materialize_and_fix.py`

- Copies generated `org/...` into `src/main/java/org/...`
- Applies signature sanitization for parser-leaked C++ tokens
- Validates required C++-generated type wrappers exist:
  - `Vector3d.java`
  - `Vector3i.java`
  - `Matrix3d.java`
  - `Matrix4d.java`
  - `SizeVector.java`

### Stage D: Package

`pom.xml` builds stable Java entrypoints and keeps full generated sources in resources for auditability:

- compiled classes: anchor, presets, examples, key math wrappers
- full generated bindings retained in JAR resources under `generated-bindings/`

## End-to-End Script

Use:

- `tools/open3d_skill/run_open3d_full_pipeline.sh`

## Commands

```bash
cd /Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge
bash tools/open3d_skill/run_open3d_full_pipeline.sh
```

Custom paths:

```bash
cd /Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge
OPEN3D_ROOT=/Users/mullerzhang/Documents/code/Open3D \
OUT_DIR=/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/open3d-javacpp-bridge \
PROFILE=full \
bash tools/open3d_skill/run_open3d_full_pipeline.sh
```

## Verification Checklist

- `mvn -DskipTests=true clean package` exits with code 0.
- `src/main/java/org/bytedeco/open3d/Vector3d.java` exists.
- `src/main/java/org/bytedeco/open3d/Matrix3d.java` exists.
- `src/main/java/org/bytedeco/open3d/SizeVector.java` exists.
- `target/open3d-0.19.0-1.5.13.jar` contains `generated-bindings/org/bytedeco/open3d/`.

## Notes

- Some Open3D ML/CUDA/SYCL declarations are parser-fragile under JavaCPP 1.5.13.
- The default pipeline preserves all generated sources and keeps packaging stable.
- `full-compile-generated` profile in `pom.xml` is reserved for future incremental fixes.

