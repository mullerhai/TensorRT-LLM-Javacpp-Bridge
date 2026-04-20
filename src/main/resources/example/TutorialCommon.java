package example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

final class TutorialCommon {
    private TutorialCommon() {
    }

    static String chatCompletions(String baseUrl, String model, String prompt, int maxTokens, double temperature)
            throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        String body = "{"
                + "\"model\":\"" + jsonEscape(model) + "\","
                + "\"messages\":[{\"role\":\"user\",\"content\":\"" + jsonEscape(prompt) + "\"}],"
                + "\"max_tokens\":" + maxTokens + ","
                + "\"temperature\":" + temperature + ","
                + "\"stream\":false"
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/chat/completions"))
                .timeout(Duration.ofSeconds(120))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() / 100 != 2) {
            throw new IOException("chat/completions returned status=" + response.statusCode() + ", body=" + response.body());
        }
        return response.body();
    }

    static int runCommand(List<String> command, Map<String, String> extraEnv, String workingDir, boolean inheritIo)
            throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command);
        if (workingDir != null && !workingDir.isEmpty()) {
            builder.directory(new java.io.File(workingDir));
        }
        if (extraEnv != null && !extraEnv.isEmpty()) {
            builder.environment().putAll(extraEnv);
        }

        if (inheritIo) {
            builder.inheritIO();
        } else {
            builder.redirectErrorStream(true);
        }

        Process process = builder.start();
        if (!inheritIo) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
        }
        return process.waitFor();
    }

    static int runShell(String shellCommand, String workingDir, boolean inheritIo)
            throws IOException, InterruptedException {
        return runCommand(Arrays.asList("zsh", "-lc", shellCommand), Map.of(), workingDir, inheritIo);
    }

    static void requireArgs(String[] args, int min, String usage) {
        if (args.length < min) {
            throw new IllegalArgumentException("Missing args. Usage: " + usage);
        }
    }

    private static String jsonEscape(String input) {
        StringBuilder sb = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}


