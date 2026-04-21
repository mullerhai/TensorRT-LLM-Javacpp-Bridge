package example;

/**
 * TensorRT-LLM tutorials Java entrypoint.
 *
 * Maps the Python tutorial examples under src/main/resources/tensorrt_llm_tutorials
 * to Java/Javacpp runnable commands.
 */
public class TutorialCli {

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0]) || "--help".equalsIgnoreCase(args[0])) {
            printHelp();
            return;
        }

        String command = args[0];
        switch (command) {
            case "lab1-trt":
                OfflineInferenceExamples.runLab1TrtGeneration(args);
                break;
            case "lab2-batch":
                OfflineInferenceExamples.runLab2Batch(args);
                break;
            case "lab3-client":
                ServiceModeExamples.runLab3Client(args);
                break;
            case "lab3-server-cmd":
                ServiceModeExamples.runLab3ServerCommand(args);
                break;
            case "lab4-quantize":
                BuildPipelineExamples.runLab4Quantize(args);
                break;
            case "lab5-client":
                ServiceModeExamples.runLab5Client(args);
                break;
            case "lab5-server-cmd":
                ServiceModeExamples.runLab5ServerCommands(args);
                break;
            case "lab6-eagle-build":
                BuildPipelineExamples.runLab6EagleBuild(args);
                break;
            case "lab7-client":
                ServiceModeExamples.runLab7Client(args);
                break;
            case "lab7-server-cmd":
                ServiceModeExamples.runLab7ServerCommand(args);
                break;
            default:
                throw new IllegalArgumentException("Unknown command: " + command + ". Run with help for usage.");
        }
    }

    private static void printHelp() {
        System.out.println("Usage: TutorialCli <command> [args]");
        System.out.println();
        System.out.println("Offline inference:");
        System.out.println("  lab1-trt <engineDir> [prompt] [maxNewTokens]");
        System.out.println("  lab2-batch <engineDir>");
        System.out.println();
        System.out.println("Online serving:");
        System.out.println("  lab3-client <baseUrl> [model] [prompt]");
        System.out.println("  lab3-server-cmd <modelPath> [extraConfigPath] [backend]");
        System.out.println("  lab5-client <baseUrl> [model] [prompt]");
        System.out.println("  lab5-server-cmd <tutorialDir>");
        System.out.println("  lab7-client <baseUrl> [model] [prompt]");
        System.out.println("  lab7-server-cmd <modelPath> [extraConfigPath]");
        System.out.println();
        System.out.println("Build/model optimization:");
        System.out.println("  lab4-quantize <pythonExec> <quantizePyPath> [extraArgs]");
        System.out.println("  lab6-eagle-build <tutorialRootInTensorRTLLMRepo>");
    }
}


