# JavaCPP C++ Transpile Skill (Practical)

This skill is designed for large C++ codebases (Open3D, TensorRT-LLM style APIs).

## Core Pattern

1. Build one umbrella preset first (`Open3DConfig`), then split by module.
2. Keep parser moving: aggressively `skip()` unsupported template/function wrappers.
3. Use stable type aliases in `InfoMap` (`std::string`, `std::vector`, `Eigen` wrappers).
4. Separate parse success from native link success.
5. Add strict validation so small fake native JARs cannot pass CI.

## Reusable InfoMap Rules

- **Primitive aliases**: map `size_t`, `int64_t`, `uint32_t` to Java primitives/pointers.
- **Enums**: `new Info("...").enumerate()`.
- **Heavy templates**: keep opaque (`@Opaque`) first, expose later.
- **Callbacks and `std::function`**: skip initially to keep parser deterministic.
- **STL containers**: add selective `@StdVector` mappings for high-frequency APIs.
- **No synthetic math stubs**: `Vector3d`, `Matrix3d`, `SizeVector` and related wrappers must be generated from C++ typedef/alias headers (for Open3D: `utility/Eigen.h`, `core/SizeVector.h`).

## Open3D-Specific Mapping Heuristics

- Start includes from `open3d/Open3D.h`.
- Add high-value modules first: `geometry`, `camera`, `utility`, `pipelines/registration`, `io`.
- Treat Tensor, ML and visualization internals as phase-2 due to template depth.

## Validation Checklist

- [ ] Header inventory > 100 files.
- [ ] Preset and Maven project generated.
- [ ] `mvn org.bytedeco:javacpp:parse` exits 0.
- [ ] Generated Java classes exist in target generated output.
- [ ] Native build step only required on Linux build host.

## Typical Failure Fixes

- Missing includes: add `-Dopen3d.include.path` and 3rd-party include dirs.
- Parser recursion/template explosion: convert symbols to `skip()`.
- Link errors: separate parse phase from build phase and validate shared libs path.

