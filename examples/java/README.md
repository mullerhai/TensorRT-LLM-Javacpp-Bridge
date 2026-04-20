# TensorRT-LLM Tutorials Java CLI (JavaCPP)

This folder documents the Java translation entrypoint for Python tutorials under `src/main/resources/tensorrt_llm_tutorials`.

Main class: `example.TutorialCli`

## Tutorial Mapping

- `lab-1-hf-trt-llm/trt-llm-generation.ipynb` -> `lab1-trt`
- `lab-2-offline-inference/trtllm-batch-infer-fp8-8k-4k-optimized.ipynb` -> `lab2-batch`
- `lab-3-online-inference/client-trt-llm.ipynb` -> `lab3-client`
- `lab-3-online-inference/server-trt-llm.ipynb` -> `lab3-server-cmd`
- `lab-4-model-opt/quantize.py` -> `lab4-quantize`
- `lab-5-disagg/disagg-client.ipynb` -> `lab5-client`
- `lab-5-disagg/disagg-server.sh` (+ `ctx-*.sh`, `gen.sh`) -> `lab5-server-cmd`
- `lab-6-speculative-eagle/trt-engine-build.ipynb` -> `lab6-eagle-build`
- `lab-7-auto-deploy/auto-deploy-client.ipynb` -> `lab7-client`
- `lab-7-auto-deploy/auto-deploy-server.ipynb` -> `lab7-server-cmd`

## Quick Usage

```bash
# Show commands
java -cp trtllm-bridge/target/classes example.TutorialCli help

# Offline single prompt
java -cp trtllm-bridge/target/classes example.TutorialCli \
  lab1-trt /path/to/trtllm-engine "Explain continuous batching"

# Offline batch prompt set
java -cp trtllm-bridge/target/classes example.TutorialCli \
  lab2-batch /path/to/trtllm-engine

# OpenAI-compatible client call (Lab3/Lab5/Lab7)
java -cp trtllm-bridge/target/classes example.TutorialCli \
  lab3-client http://127.0.0.1:8000 default "Hello"

# Print equivalent server launch command for Lab3
java -cp trtllm-bridge/target/classes example.TutorialCli \
  lab3-server-cmd /workspace/models/Qwen3-30B-A3B-Instruct-2507-FP8/ \
  /workspace/extra-llm-api-config.yml pytorch
```

## Notes

- Current JavaCPP binding in this workspace has partial Executor response APIs generated; therefore the Java examples currently focus on request enqueue/readiness polling and command parity with Python notebooks.
- Full token-by-token decode printing can be enabled once `awaitResponses` and related wrappers are available in generated bindings.

