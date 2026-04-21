#!/bin/bash
CUDA_VISIBLE_DEVICES=2 trtllm-serve /workspace/models/Llama-3.1-8B-Instruct-FP8/ \
    --host localhost --port 8007 \
    --extra_llm_api_options ./gen_extra-llm-api-config.yaml