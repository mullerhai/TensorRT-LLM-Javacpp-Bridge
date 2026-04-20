package example;

import java.io.IOException;
import tensorrt_llm.executor.Executor;
import tensorrt_llm.executor.ExecutorConfig;
import tensorrt_llm.executor.KvCacheConfig;
import tensorrt_llm.executor.Request;
import tensorrt_llm.builder.RequestBuilder;
import tensorrt_llm.builder.SamplingConfigBuilder;

final class ServiceModeExamples {
    private ServiceModeExamples() {
    }

    static void runLab3Client(String[] args) throws IOException, InterruptedException {
        TutorialCommon.requireArgs(args, 2,
                "lab3-client <baseUrl> [model] [prompt]");

        String baseUrl = args[1];
        String model = args.length > 2 ? args[2] : "default";
        String prompt = args.length > 3 ? args[3] : "请介绍 TensorRT-LLM 在线服务架构。";

        String response = TutorialCommon.chatCompletions(baseUrl, model, prompt, 256, 0.7);
        System.out.println(response);
    }

    static void runLab3ServerCommand(String[] args) {
        TutorialCommon.requireArgs(args, 2,
                "lab3-server-cmd <modelPath> [extraConfigPath] [backend]");

        String modelPath = args[1];
        String extraConfigPath = args.length > 2 ? args[2] : "extra-llm-api-config.yml";
        String backend = args.length > 3 ? args[3] : "pytorch";

        String cmd = "trtllm-serve " + shellQuote(modelPath)
                + " --backend " + shellQuote(backend)
                + " --extra_llm_api_options " + shellQuote(extraConfigPath);
        System.out.println(cmd);
    }

    static void runLab5Client(String[] args) throws IOException, InterruptedException {
        TutorialCommon.requireArgs(args, 2,
                "lab5-client <baseUrl> [model] [prompt]");

        String baseUrl = args[1];
        String model = args.length > 2 ? args[2] : "default";
        String prompt = args.length > 3 ? args[3] : "解释 disaggregated serving 的 Prefill/Decode 分离。";

        String response = TutorialCommon.chatCompletions(baseUrl, model, prompt, 192, 0.2);
        System.out.println(response);
    }

    static void runLab5ServerCommands(String[] args) {
        TutorialCommon.requireArgs(args, 2,
                "lab5-server-cmd <tutorialDir>");

        String tutorialDir = args[1];
        System.out.println("cd " + shellQuote(tutorialDir));
        System.out.println("./ctx-0.sh");
        System.out.println("./ctx-1.sh");
        System.out.println("./gen.sh");
        System.out.println("./disagg-server.sh");
    }

    static void runLab7Client(String[] args) throws IOException, InterruptedException {
        TutorialCommon.requireArgs(args, 2,
                "lab7-client <baseUrl> [model] [prompt]");

        String baseUrl = args[1];
        String model = args.length > 2 ? args[2] : "default";
        String prompt = args.length > 3 ? args[3] : "请简要说明 AutoDeploy backend 的核心流程。";

        String response = TutorialCommon.chatCompletions(baseUrl, model, prompt, 256, 0.7);
        System.out.println(response);
    }

    static void runLab7ServerCommand(String[] args) {
        TutorialCommon.requireArgs(args, 2,
                "lab7-server-cmd <modelPath> [extraConfigPath]");

        String modelPath = args[1];
        String extraConfigPath = args.length > 2 ? args[2] : "extra-llm-api-config.yml";
        String cmd = "trtllm-serve " + shellQuote(modelPath)
                + " --backend _autodeploy"
                + " --extra_llm_api_options " + shellQuote(extraConfigPath);
        System.out.println(cmd);
    }

    private static String shellQuote(String value) {
        return "'" + value.replace("'", "'\\''") + "'";
    }
}


