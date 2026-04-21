#!/usr/bin/env python3
"""
Clean FP8 Post-Training Quantization script for Hugging Face models.
Simplified version focusing on FP8 quantization with essential features.
"""

import argparse
import torch
from transformers import AutoModelForCausalLM, AutoTokenizer
from datasets import load_dataset

import modelopt.torch.quantization as mtq
from modelopt.torch.export import export_hf_checkpoint


def get_calibration_dataloader(tokenizer, dataset_name="cnn_dailymail", 
                               num_samples=512, max_length=512, batch_size=1):
    """
    Create calibration dataloader from dataset.
    
    Args:
        tokenizer: HuggingFace tokenizer
        dataset_name: Name of dataset to use
        num_samples: Number of calibration samples
        max_length: Maximum sequence length
        batch_size: Batch size for calibration
    
    Returns:
        List of tokenized samples
    """
    print(f"Loading {num_samples} samples from {dataset_name}...")
    
    # Load dataset with streaming to avoid disk space issues
    if dataset_name == "cnn_dailymail":
        dataset = load_dataset("cnn_dailymail", "3.0.0", split="train", streaming=True)
        text_key = "article"
    elif dataset_name == "wikitext":
        dataset = load_dataset("wikitext", "wikitext-2-raw-v1", split="train", streaming=True)
        text_key = "text"
    else:
        raise ValueError(f"Unsupported dataset: {dataset_name}")
    
    # Prepare calibration data
    calib_data = []
    for i, sample in enumerate(dataset):
        if i >= num_samples:
            break
        
        text = sample[text_key]
        if not text.strip():
            continue
            
        # Tokenize
        tokens = tokenizer(
            text,
            return_tensors="pt",
            max_length=max_length,
            truncation=True,
            padding="max_length"
        )
        
        calib_data.append(tokens)
        
        if (i + 1) % 100 == 0:
            print(f"Processed {i + 1}/{num_samples} samples")
    
    print(f"Calibration data ready: {len(calib_data)} samples")
    return calib_data


def calibrate_model(model, calib_dataloader):
    """
    Simple calibration loop.
    
    Args:
        model: Model to calibrate
        calib_dataloader: Calibration data
    """
    def forward_loop():
        for i, batch in enumerate(calib_dataloader):
            # Move batch to model device
            batch = {k: v.to(model.device) for k, v in batch.items()}
            model(**batch)
            
            if (i + 1) % 50 == 0:
                print(f"Calibrated {i + 1}/{len(calib_dataloader)} batches")
    
    return forward_loop


def main(args):
    # Set device
    if not torch.cuda.is_available():
        raise RuntimeError("CUDA is required for quantization")
    
    device = "cuda"
    print(f"Using device: {device}")
    
    # Load tokenizer
    print(f"Loading tokenizer from {args.model_path}...")
    tokenizer = AutoTokenizer.from_pretrained(
        args.model_path,
        trust_remote_code=args.trust_remote_code,
        padding_side="left"  # Better for calibration
    )
    
    # Ensure padding token is set
    if tokenizer.pad_token is None:
        tokenizer.pad_token = tokenizer.eos_token
    
    # Load model
    print(f"Loading model from {args.model_path}...")
    model = AutoModelForCausalLM.from_pretrained(
        args.model_path,
        torch_dtype=torch.float16,
        device_map="auto",
        trust_remote_code=args.trust_remote_code
    )
    
    print(f"Model loaded on {model.device}")
    
    # Get calibration data
    calib_dataloader = get_calibration_dataloader(
        tokenizer,
        dataset_name=args.dataset,
        num_samples=args.calib_size,
        max_length=args.calib_seq_len,
        batch_size=args.batch_size
    )
    
    # Setup FP8 quantization config
    print("Setting up FP8 quantization configuration...")
    quant_cfg = mtq.FP8_DEFAULT_CFG
    
    # Optional: Add KV cache quantization
    if args.kv_cache_quant:
        print("Enabling FP8 KV cache quantization...")
        kv_quant_cfg = mtq.FP8_KV_CFG["quant_cfg"]
        # Merge KV cache config with default config
        quant_cfg = {
            "quant_cfg": {
                **quant_cfg["quant_cfg"],
                **kv_quant_cfg
            },
            "algorithm": quant_cfg["algorithm"]
        }
    
    # Run test generation before quantization
    print("\n" + "="*50)
    print("Testing model before quantization...")
    test_input = tokenizer("Hello, how are you?", return_tensors="pt").to(model.device)
    with torch.no_grad():
        output_before = model.generate(**test_input, max_new_tokens=50)
    print("Output before quantization:")
    print(tokenizer.decode(output_before[0], skip_special_tokens=True))
    print("="*50 + "\n")
    
    # Quantize model
    print("Starting FP8 quantization...")
    forward_loop = calibrate_model(model, calib_dataloader)
    
    model = mtq.quantize(
        model,
        quant_cfg,
        forward_loop=forward_loop
    )
    
    print("Quantization complete!")
    
    # Print quantization summary
    if args.verbose:
        print("\nQuantization Summary:")
        mtq.print_quant_summary(model)
    
    # Run test generation after quantization
    print("\n" + "="*50)
    print("Testing model after quantization...")
    torch.cuda.empty_cache()
    with torch.no_grad():
        output_after = model.generate(**test_input, max_new_tokens=50)
    print("Output after quantization:")
    print(tokenizer.decode(output_after[0], skip_special_tokens=True))
    print("="*50 + "\n")
    
    # Export quantized model
    print(f"Exporting quantized model to {args.export_path}...")
    export_hf_checkpoint(model, export_dir=args.export_path)
    
    # Save tokenizer
    tokenizer.save_pretrained(args.export_path)
    
    print(f"\n✓ Quantized model exported successfully to: {args.export_path}")
    print(f"  - Model files: {args.export_path}/")
    print(f"  - Tokenizer: {args.export_path}/tokenizer_config.json")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="FP8 Post-Training Quantization for Hugging Face models"
    )
    
    # Required arguments
    parser.add_argument(
        "--model_path",
        type=str,
        required=True,
        help="Path to the Hugging Face model"
    )
    
    parser.add_argument(
        "--export_path",
        type=str,
        default="./quantized_model",
        help="Path to export quantized model (default: ./quantized_model)"
    )
    
    # Calibration arguments
    parser.add_argument(
        "--dataset",
        type=str,
        default="cnn_dailymail",
        choices=["cnn_dailymail", "wikitext"],
        help="Dataset for calibration (default: cnn_dailymail)"
    )
    
    parser.add_argument(
        "--calib_size",
        type=int,
        default=512,
        help="Number of calibration samples (default: 512)"
    )
    
    parser.add_argument(
        "--calib_seq_len",
        type=int,
        default=512,
        help="Maximum sequence length for calibration (default: 512)"
    )
    
    parser.add_argument(
        "--batch_size",
        type=int,
        default=1,
        help="Batch size for calibration (default: 1)"
    )
    
    # Model loading arguments
    parser.add_argument(
        "--trust_remote_code",
        action="store_true",
        help="Trust remote code when loading model"
    )
    
    # Quantization arguments
    parser.add_argument(
        "--kv_cache_quant",
        action="store_true",
        help="Enable FP8 KV cache quantization"
    )
    
    parser.add_argument(
        "--verbose",
        action="store_true",
        help="Print verbose output including quantization summary"
    )
    
    args = parser.parse_args()
    
    main(args)