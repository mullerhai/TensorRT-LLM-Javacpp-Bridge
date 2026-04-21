#!/bin/bash
CUDA_VISIBLE_DEVICES=0 trtllm-serve /workspace/models/Llama-3.1-8B-Instruct-FP8/ \
    --host localhost --port 8005 \
    --extra_llm_api_options ./ctx_extra-llm-api-config.yaml