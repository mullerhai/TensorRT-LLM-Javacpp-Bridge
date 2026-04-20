package example;

import java.io.IOException;

final class BuildPipelineExamples {
    private BuildPipelineExamples() {
    }

    static void runLab4Quantize(String[] args) throws IOException, InterruptedException {
        TutorialCommon.requireArgs(args, 3,
                "lab4-quantize <pythonExec> <quantizePyPath> [extraArgs]");

        String pythonExec = args[1];
        String quantizePyPath = args[2];
        String extraArgs = args.length > 3 ? args[3] : "";

        String cmd = shellQuote(pythonExec) + " " + shellQuote(quantizePyPath)
                + (extraArgs.isEmpty() ? "" : " " + extraArgs);
        int exitCode = TutorialCommon.runShell(cmd, null, true);
        if (exitCode != 0) {
            throw new RuntimeException("lab4 quantize failed with exitCode=" + exitCode);
        }
    }

    static void runLab6EagleBuild(String[] args) throws IOException, InterruptedException {
        TutorialCommon.requireArgs(args, 2,
                "lab6-eagle-build <tutorialRootInTensorRTLLMRepo>");

        String root = args[1];

        String convertCmd = "python /app/tensorrt_llm/examples/eagle/convert_checkpoint.py "
                + "--model_dir /workspace/models/vicuna-7b-v1.3/ "
                + "--eagle_model_dir /workspace/models/eagle-vicuna-7b-v1.3/ "
                + "--output_dir ./tllm_checkpoint_1gpu_eagle";
        String buildCmd = "trtllm-build --checkpoint_dir ./tllm_checkpoint_1gpu_eagle "
                + "--output_dir ./tmp/eagle/7B/trt_engines/fp16/1-gpu/ "
                + "--gemm_plugin float16 --max_batch_size 8 --max_num_tokens 2048 "
                + "--speculative_decoding_mode eagle";
        String runCmd = "python /app/tensorrt_llm/examples/run.py "
                + "--engine_dir ./tmp/eagle/7B/trt_engines/fp16/1-gpu/ "
                + "--tokenizer_dir /workspace/models/vicuna-7b-v1.3/ "
                + "--max_output_len 128 --input_text \"What is speculative decoding?\"";

        runChecked(convertCmd, root, "lab6 convert_checkpoint");
        runChecked(buildCmd, root, "lab6 trtllm-build");
        runChecked(runCmd, root, "lab6 run.py");
    }

    private static void runChecked(String cmd, String cwd, String stepName) throws IOException, InterruptedException {
        int exitCode = TutorialCommon.runShell(cmd, cwd, true);
        if (exitCode != 0) {
            throw new RuntimeException(stepName + " failed with exitCode=" + exitCode);
        }
    }

    private static String shellQuote(String value) {
        return "'" + value.replace("'", "'\\''") + "'";
    }
}


