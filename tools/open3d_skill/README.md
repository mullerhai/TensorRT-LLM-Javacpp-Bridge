# Open3D JavaCPP Skill Harness

This mini toolchain loads a reusable JavaCPP skill definition, validates Open3D source inputs, generates a modular Open3D JavaCPP bridge project, and runs smoke tests.

## Files

- `skill_engine.py`: load/validate skill YAML and render variables
- `generate_open3d_bridge.py`: generate `open3d-javacpp-bridge` Maven project + modular presets
- `postprocess_generated_java.py`: sanitize generated Java sources for packaging
- `test_skill.py`: one-shot test runner (load skill + generate + parse + optional package)

## Quick Start (Modular Parse)

```bash
cd /Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge
python3 -m pip install -r tools/open3d_skill/requirements.txt
python3 tools/open3d_skill/test_skill.py \
  --skill skills/javacpp_cpp_transpile_skill.yaml \
  --open3d-root /Users/mullerzhang/Documents/code/Open3D \
  --output /Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/open3d-javacpp-bridge \
  --profile full
```

Generated JavaCPP outputs will be in module directories under:

- `open3d-javacpp-bridge/org/bytedeco/open3d/camera`
- `open3d-javacpp-bridge/org/bytedeco/open3d/geometry`
- `open3d-javacpp-bridge/org/bytedeco/open3d/io`
- `open3d-javacpp-bridge/org/bytedeco/open3d/pipelines`
- `open3d-javacpp-bridge/org/bytedeco/open3d/utility`
- `open3d-javacpp-bridge/org/bytedeco/open3d/global`

## Verify Package Build (with generated sources)

```bash
cd /Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge
python3 tools/open3d_skill/test_skill.py \
  --skill skills/javacpp_cpp_transpile_skill.yaml \
  --open3d-root /Users/mullerzhang/Documents/code/Open3D \
  --output /Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/open3d-javacpp-bridge \
  --profile safe \
  --verify-package
```

If you want to try compiling generated Java bindings directly (experimental), add:

```bash
--materialize-generated
```

## Usage Samples

After generation, example files are placed in:

- `open3d-javacpp-bridge/src/main/java/org/bytedeco/open3d/examples/ReadPointCloudExample.java`
- `open3d-javacpp-bridge/src/main/java/org/bytedeco/open3d/examples/ICPRegistrationExample.java`

Run sample classpath check:

```bash
cd /Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/open3d-javacpp-bridge
mvn -q -DskipTests=true compile
java -cp target/classes org.bytedeco.open3d.examples.ReadPointCloudExample
```

> `safe` profile excludes parser-fragile headers for deterministic parse.
> `full` profile expands module coverage and directory generation.
