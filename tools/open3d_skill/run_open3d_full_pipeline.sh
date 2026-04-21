#!/usr/bin/env bash
set -euo pipefail

BASE_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
OPEN3D_ROOT="${OPEN3D_ROOT:-/Users/mullerzhang/Documents/code/Open3D}"
OUT_DIR="${OUT_DIR:-${BASE_DIR}/open3d-javacpp-bridge}"
PROFILE="${PROFILE:-full}"

echo "[1/4] Generate presets + compile + parse"
python3 "${BASE_DIR}/tools/open3d_skill/test_skill.py" \
  --skill "${BASE_DIR}/skills/javacpp_cpp_transpile_skill.yaml" \
  --open3d-root "${OPEN3D_ROOT}" \
  --output "${OUT_DIR}" \
  --profile "${PROFILE}" \
  --skip-maven \
  --skip-parse

cd "${OUT_DIR}"
mvn -q -DskipTests=true compile
mvn -q org.bytedeco:javacpp:parse -Djavacpp.parser.skip=false -Dopen3d.include.path="${OPEN3D_ROOT}/cpp"

echo "[2/4] Materialize generated C++ bindings into src/main/java"
python3 "${BASE_DIR}/tools/open3d_skill/materialize_and_fix.py" --project-root "${OUT_DIR}"

echo "[3/4] Build package"
mvn -q -DskipTests=true clean package

echo "[4/4] Verify key generated C++ type wrappers"
for f in Vector3d.java Vector3i.java Matrix3d.java Matrix4d.java SizeVector.java; do
  test -f "${OUT_DIR}/src/main/java/org/bytedeco/open3d/${f}"
  echo "  OK ${f}"
done

echo "DONE: ${OUT_DIR}/target/open3d-0.19.0-1.5.13.jar"

