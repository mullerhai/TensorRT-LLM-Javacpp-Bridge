#!/bin/bash
CUDA_VISIBLE_DEVICES=1 trtllm-serve /workspace/models/Llama-3.1-8B-Instruct-FP8/ \
    --host localhost --port 8006 \
    --extra_llm_api_options ./ctx_extra-llm-api-config.yaml