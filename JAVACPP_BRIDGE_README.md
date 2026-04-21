# TensorRT-LLM Java Bridge — 完整使用指南

> **JavaCPP 转译技术文档 · 脚本使用手册 · 样例代码**

---

## 目录

1. [项目概述](#1-项目概述)
2. [JavaCPP 转译原理与技巧 (Skill 详解)](#2-javacpp-转译原理与技巧)
3. [快速开始](#3-快速开始)
4. [构建打包脚本](#4-构建打包脚本)
5. [Java API 样例](#5-java-api-样例)
6. [依赖配置](#6-依赖配置)
7. [运行时环境要求](#7-运行时环境要求)
8. [常见问题](#8-常见问题)

---

## 1. 项目概述

本项目通过 **JavaCPP** 将 NVIDIA [TensorRT-LLM](https://github.com/NVIDIA/TensorRT-LLM) (v0.17.0) 的 C++ API 完整转译为 Java 绑定，使 Java 应用程序能够直接调用 TensorRT-LLM 进行高性能 LLM 推理。

### 模块结构

```
TRTLLM-Java-Bridge/
├── src/main/java/
│   ├── tensorrt_llm/
│   │   ├── presets/          ← JavaCPP 配置文件 (InfoMapper)
│   │   ├── executor/         ← 生成的 Java 绑定 — Executor API
│   │   ├── batch_manager/    ← 生成的 Java 绑定 — BatchManager
│   │   ├── runtime/          ← 生成的 Java 绑定 — Runtime
│   │   ├── kernels/          ← 生成的 Java 绑定 — CUDA Kernels
│   │   ├── layers/           ← 生成的 Java 绑定 — Layers
│   │   ├── plugins/          ← 生成的 Java 绑定 — TensorRT Plugins
│   │   ├── builder/          ← Java 便捷 Builder API
│   │   └── common/           ← 公共类型
│   └── example/              ← 使用示例
├── trtllm-native/
│   ├── linux-x86_64/         ← Linux x86_64 native JAR 模块
│   ├── linux-arm64/          ← Linux ARM64 native JAR 模块
│   └── macosx-arm64/         ← macOS ARM64 native JAR 模块
├── build-jni/                ← JavaCPP 生成的 JNI C++ 源码
├── cuda-stubs/               ← CUDA 头文件 stub (用于 macOS 解析)
└── scripts/
    ├── build_platform_jars.sh   ← 打包所有平台 JAR
    ├── bundle_native_deps.sh    ← 打包 CUDA/TensorRT 依赖
    └── build_all.sh             ← 一键全量构建
```

### JAR 产出说明

| JAR 文件 | 内容 | 大小 |
|---------|------|------|
| `tensorrt-llm-0.17.0-1.5.13.jar` | 纯 Java 绑定类 | ~17 MB |
| `tensorrt-llm-0.17.0-1.5.13-linux-x86_64.jar` | Linux x86_64 native 库 + CUDA deps | **>100 MB** |
| `tensorrt-llm-0.17.0-1.5.13-linux-arm64.jar` | Linux ARM64 native 库 | **>50 MB** |
| `tensorrt-llm-0.17.0-1.5.13-macosx-arm64.jar` | macOS ARM64 stub | ~1 MB |
| `tensorrt-llm-platform-0.17.0-1.5.13.jar` | 平台聚合 POM | ~2 KB |

---

## 2. JavaCPP 转译原理与技巧

### 2.1 转译架构

JavaCPP 通过两步将 C++ API 暴露给 Java：

```
C++ Headers (.h)
      ↓  [JavaCPP Parser — mvn javacpp:parse]
InfoMapper Config (.java in presets/)
      ↓  [JavaCPP Generator — mvn javacpp:build]
Java Bindings (.java) + JNI Bridge (.cpp)
      ↓  [C++ Compiler — g++/nvcc]
libjniTRTLLM.so  (JNI 动态库)
```

### 2.2 Preset 配置文件详解

每个模块对应一个 `*Config.java` 文件，位于 `tensorrt_llm/presets/`：

```java
@Properties(
    value = @Platform(
        includepath = {
            "/path/to/TensorRT-LLM/cpp/include",
            "/path/to/cuda-stubs"
        },
        include = {
            "tensorrt_llm/executor/types.h",
            "tensorrt_llm/executor/executor.h"
        },
        link = {"tensorrt_llm", "nvinfer", "cudart"}   // -l 链接选项
    ),
    target = "tensorrt_llm.executor",        // 生成的 Java 包名
    global = "tensorrt_llm.global.Executor"  // 全局函数/常量类
)
public class ExecutorConfig implements InfoMapper {
    @Override
    public void map(InfoMap infoMap) {
        // 类型映射规则...
    }
}
```

### 2.3 InfoMap 类型映射技巧

#### Skill 1: 原始类型别名映射

```java
// C++: typedef int32_t SizeType32;
infoMap.put(new Info("tensorrt_llm::executor::SizeType32")
    .valueTypes("int").pointerTypes("IntPointer"));

// C++: typedef int64_t SizeType64;
infoMap.put(new Info("tensorrt_llm::executor::SizeType64")
    .valueTypes("long").pointerTypes("LongPointer"));
```

#### Skill 2: 跳过不支持的复杂类型

```java
// C++ std::function, std::variant, std::array 等复杂类型 → 映射为 Pointer 跳过
infoMap.put(new Info("tensorrt_llm::executor::LogitsPostProcessor")
    .pointerTypes("Pointer").skip());

// 模板特化 → 跳过
infoMap.put(new Info("tensorrt_llm::executor::TypeTraits")
    .pointerTypes("Pointer").skip());
```

#### Skill 3: 枚举映射

```java
// C++ enum class → Java 枚举常量
infoMap.put(new Info("tensorrt_llm::executor::ModelType").enumerate());
infoMap.put(new Info("tensorrt_llm::executor::FinishReason").enumerate());
```

用法:
```java
import static tensorrt_llm.global.Executor.ModelType.kDECODER_ONLY;
Executor executor = new Executor(engineDir, kDECODER_ONLY, config);
```

#### Skill 4: 智能指针 (shared_ptr/unique_ptr) 处理

```java
// C++: std::shared_ptr<KvCacheConfig>
infoMap.put(new Info("std::shared_ptr<tensorrt_llm::executor::KvCacheConfig>")
    .pointerTypes("@SharedPtr KvCacheConfig"));
```

#### Skill 5: std::optional 映射

```java
// C++: std::optional<float> temperature;
infoMap.put(new Info("std::optional<float>")
    .pointerTypes("@ByRef FloatOptional"));
```

#### Skill 6: std::vector 映射

```java
// C++: std::vector<int32_t> inputTokenIds;
infoMap.put(new Info("std::vector<int32_t>")
    .pointerTypes("@StdVector IntPointer"));
```

#### Skill 7: 跨模块依赖 (Preset 继承)

```java
// BatchmanagerConfig 依赖 CommonConfig
@Properties(
    inherit = CommonConfig.class,  // 继承公共映射
    value = @Platform(
        include = {"tensorrt_llm/batch_manager/batchScheduler.h"}
    )
)
public class BatchmanagerConfig implements InfoMapper { ... }
```

#### Skill 8: 条件编译平台差异

```java
@Properties(
    value = {
        @Platform(value = "linux",
            link = {"tensorrt_llm", "nvinfer", "cudart:12"},
            preload = {"cudart", "cublas", "cublasLt"}),
        @Platform(value = "macosx",
            link = {},   // macOS 无 CUDA，stub 模式
            preload = {})
    }
)
```

#### Skill 9: 自定义 Java 包装类

对于 C++ 复杂构造函数，编写 Java Builder 封装：

```java
// src/main/java/tensorrt_llm/builder/RequestBuilder.java
public class RequestBuilder {
    public static RequestBuilder builder(int[] inputTokenIds, int maxNewTokens) {
        return new RequestBuilder(inputTokenIds, maxNewTokens);
    }

    public RequestBuilder temperature(float temp) {
        this.temperature = temp;
        return this;
    }

    public Request build() {
        Request req = new Request();
        // 调用 JavaCPP 生成的底层 API
        IntPointer tokenPtr = new IntPointer(inputTokenIds);
        req.setInputTokenIds(tokenPtr, inputTokenIds.length);
        req.setMaxNewTokens(maxNewTokens);
        // ...
        return req;
    }
}
```

#### Skill 10: JNI 生成与编译 (Linux CUDA 环境)

```bash
# 步骤 1: 解析 C++ 头文件，生成 Java 绑定
mvn org.bytedeco:javacpp:parse \
    -Djavacpp.parser.skip=false \
    -Dtrtllm.include.path=/path/to/TensorRT-LLM/cpp/include

# 步骤 2: 生成 JNI C++ 代码并编译 .so
mvn org.bytedeco:javacpp:build \
    -Djavacpp.compiler.skip=false \
    -Dtrtllm.include.path=/path/to/TensorRT-LLM/cpp/include \
    -Dtrtllm.lib.path=/path/to/TensorRT-LLM/cpp/build

# 步骤 3: 验证生成的 .so
file build-jni/linux-x86_64/libjniTRTLLM.so
ldd  build-jni/linux-x86_64/libjniTRTLLM.so
```

### 2.4 已映射模块总览

| 模块 | Preset 配置 | C++ 头文件 | Java 包 |
|------|------------|-----------|---------|
| Executor API | `ExecutorConfig.java` | `executor/executor.h` | `tensorrt_llm.executor` |
| Batch Manager | `BatchmanagerConfig.java` | `batch_manager/*.h` | `tensorrt_llm.batch_manager` |
| Runtime | `RuntimeConfig.java` | `runtime/*.h` | `tensorrt_llm.runtime` |
| CUDA Kernels | `KernelsConfig.java` | `kernels/*.h` | `tensorrt_llm.kernels` |
| Layers | `LayersConfig.java` | `layers/*.h` | `tensorrt_llm.layers` |
| TensorRT Plugins | `PluginsConfig.java` | `plugins/*.h` | `tensorrt_llm.plugins` |
| CUTLASS Extensions | `CutlassextensionsConfig.java` | `cutlass_extensions/*.h` | `tensorrt_llm.cutlass_extensions` |
| Thop | `ThopConfig.java` | `thop/*.h` | `tensorrt_llm.thop` |

---

## 3. 快速开始

### 3.1 环境要求

| 组件 | 版本 |
|------|------|
| Java | 11+ |
| Maven | 3.8+ |
| CUDA | 12.x (推理时) |
| TensorRT | 10.x |
| GPU | NVIDIA Volta 及以上 (sm_70+) |
| OS (推理) | Linux x86_64 / Linux ARM64 |

### 3.2 macOS 快速编译 (无 CUDA，仅 Java 绑定)

```bash
git clone https://github.com/your-org/TRTLLM-Java-Bridge.git
cd TRTLLM-Java-Bridge

# 编译纯 Java 绑定 (无需 CUDA)
mvn clean package -DskipTests=true

# 验证
ls -lh target/trtllm-bridge-1.0.0.jar   # ~17 MB
```

### 3.3 Linux (CUDA 环境) 完整构建

```bash
# 前置: 已安装 CUDA 12.x, TensorRT 10.x, 并构建了 TensorRT-LLM
export TRTLLM_BUILD_DIR=/workspace/TensorRT-LLM/cpp/build
export CUDA_LIB_DIR=/usr/local/cuda/lib64
export TENSORRT_LIB_DIR=/usr/lib/x86_64-linux-gnu

# 一键构建
./scripts/build_all.sh

# 产出
ls -lh dist/
# tensorrt-llm-0.17.0-1.5.13.jar             17 MB  (Java bindings)
# tensorrt-llm-0.17.0-1.5.13-linux-x86_64.jar  >100 MB (CUDA+TRT native)
# tensorrt-llm-platform-0.17.0-1.5.13.jar    2 KB   (aggregator)
```

---

## 4. 构建打包脚本

### 4.1 `scripts/build_all.sh` — 一键全量构建

```bash
# 基本用法
./scripts/build_all.sh

# 指定路径
TRTLLM_BUILD_DIR=/path/to/build \
CUDA_LIB_DIR=/usr/local/cuda/lib64 \
TENSORRT_LIB_DIR=/usr/lib/x86_64-linux-gnu \
./scripts/build_all.sh
```

### 4.2 `scripts/build_platform_jars.sh` — 打包所有平台 JAR

```bash
# 构建并安装 5 个 JAR 到本地 Maven 仓库
./scripts/build_platform_jars.sh
```

**产出文件:**
```
dist/
├── tensorrt-llm-0.17.0-1.5.13.jar           ← 核心 Java 绑定
├── tensorrt-llm-0.17.0-1.5.13-linux-x86_64.jar  ← Linux x86_64 native
├── tensorrt-llm-0.17.0-1.5.13-linux-arm64.jar   ← Linux ARM64 native
├── tensorrt-llm-0.17.0-1.5.13-macosx-arm64.jar  ← macOS ARM64 stub
└── tensorrt-llm-platform-0.17.0-1.5.13.jar      ← 平台聚合
```

### 4.3 `scripts/bundle_native_deps.sh` — 打包 CUDA/TensorRT 依赖

> **仅在 Linux 有 CUDA 环境时有效**。此脚本将所有运行时依赖打入 native JAR，使其自包含。
> 若找不到关键依赖 (`libjniTRTLLM.so`, `libtensorrt_llm.so`, `libnvinfer.so`, `libcudart.so`)，脚本默认会 **直接失败**，避免产出几十 KB 的假完整 JAR。

```bash
# 自动检测平台
./scripts/bundle_native_deps.sh

# 手动指定路径
PLATFORM_OVERRIDE=linux-x86_64 \
TRTLLM_BUILD_DIR=/workspace/TensorRT-LLM/cpp/build \
CUDA_LIB_DIR=/usr/local/cuda/lib64 \
TENSORRT_LIB_DIR=/opt/tensorrt/lib \
NCCL_LIB_DIR=/usr/lib/x86_64-linux-gnu \
./scripts/bundle_native_deps.sh

# 如果你只想临时产出 stub/部分依赖 JAR（不推荐生产）
REQUIRE_FULL_DEPS=0 ./scripts/bundle_native_deps.sh
```

**打包的库文件:**
```
org/bytedeco/tensorrt_llm/linux-x86_64/
├── libjniTRTLLM.so          (JNI bridge, ~30 MB 编译后)
├── libtensorrt_llm.so       (TRT-LLM core, ~2 GB)
├── libnvinfer.so.10          (TensorRT, ~500 MB)
├── libnvinfer_plugin.so.10   (TRT plugins, ~200 MB)
├── libcudart.so.12           (CUDA runtime, ~50 MB)
├── libcublas.so.12           (cuBLAS, ~400 MB)
├── libcublasLt.so.12         (cuBLAS Lt, ~300 MB)
├── libnccl.so.2              (NCCL, ~70 MB)
└── libcurand.so.10           (cuRAND, ~50 MB)
```

> **预计 native JAR 大小: 100 MB ~ 3 GB** (视打包的 CUDA 库范围)

### 4.4 Docker 交叉编译 (macOS → Linux)

```bash
# 使用预置 Dockerfile
docker build -f docker/Dockerfile.linux-x86_64 -t trtllm-builder .

docker run --rm --gpus all \
    -v $(pwd):/workspace/TRTLLM-Java-Bridge \
    -e TRTLLM_BUILD_DIR=/workspace/TensorRT-LLM/cpp/build \
    trtllm-builder \
    bash /workspace/TRTLLM-Java-Bridge/scripts/build_all.sh
```

### 4.5 重新生成 JNI (修改了 Preset 后)

```bash
# 重新解析 C++ 头文件
mvn org.bytedeco:javacpp:parse \
    -Djavacpp.parser.skip=false \
    -Dtrtllm.include.path=/path/to/TensorRT-LLM/cpp/include \
    2>&1 | tee parse.log

# 重新编译 JNI (Linux CUDA 环境)
mvn org.bytedeco:javacpp:build \
    -Djavacpp.compiler.skip=false \
    -Dtrtllm.include.path=/path/to/TensorRT-LLM/cpp/include \
    -Dtrtllm.lib.path=/path/to/TensorRT-LLM/cpp/build
```

---

## 5. Java API 样例

### 5.1 离线单次推理 (Lab1 — 最简单)

```java
import org.bytedeco.javacpp.BytePointer;
import tensorrt_llm.executor.*;
import tensorrt_llm.builder.*;
import static tensorrt_llm.global.Executor.ModelType.kDECODER_ONLY;

public class SimpleInference {
    public static void main(String[] args) {
        String engineDir = "/path/to/qwen3-trt-engine";

        // 1. 配置 KV Cache
        KvCacheConfig kvCache = new KvCacheConfig();
        kvCache.setEnableBlockReuse(true);
        kvCache.setFreeGpuMemoryFraction(0.85f);

        // 2. 配置 Executor
        ExecutorConfig config = new ExecutorConfig();
        config.setKvCacheConfig(kvCache);
        config.setEnableChunkedContext(true);
        config.setMaxBeamWidth(1);

        // 3. 创建 Executor
        try (Executor executor = new Executor(
                new BytePointer(engineDir), kDECODER_ONLY, config)) {

            // 4. 构造请求
            int[] inputTokenIds = {9707, 374, 279, 16665, 315, 8201};  // tokenized
            Request request = RequestBuilder.builder(inputTokenIds, 256)
                    .streaming(false)
                    .endId(151645)   // <|im_end|>
                    .padId(151643)   // <|endoftext|>
                    .samplingConfig(SamplingConfigBuilder.builder()
                            .temperature(0.7f)
                            .topP(0.9f)
                            .topK(50)
                            .build())
                    .build();

            // 5. 提交并等待
            long requestId = executor.enqueueRequest(request);
            System.out.println("Request submitted, id=" + requestId);

            // 6. 轮询结果
            while (true) {
                Response response = executor.awaitResponse(requestId, 1000);
                if (response != null && response.hasError()) break;
                if (response != null) {
                    // 处理 token 结果
                    System.out.println("Got response for request " + requestId);
                    break;
                }
            }
        }
    }
}
```

### 5.2 批量推理 (Lab2)

```java
import tensorrt_llm.executor.*;
import tensorrt_llm.builder.*;
import java.util.*;
import static tensorrt_llm.global.Executor.ModelType.kDECODER_ONLY;

public class BatchInference {
    public static void main(String[] args) {
        String engineDir = "/path/to/engine";

        ExecutorConfig config = new ExecutorConfig();
        config.setMaxBeamWidth(1);

        try (Executor executor = new Executor(
                new BytePointer(engineDir), kDECODER_ONLY, config)) {

            // 批量提交
            List<Long> requestIds = new ArrayList<>();
            List<int[]> prompts = Arrays.asList(
                new int[]{1, 2, 3},
                new int[]{4, 5, 6},
                new int[]{7, 8, 9}
            );

            for (int[] tokens : prompts) {
                Request req = RequestBuilder.builder(tokens, 128)
                        .endId(151645).padId(151643).build();
                requestIds.add(executor.enqueueRequest(req));
            }

            System.out.println("Submitted " + requestIds.size() + " requests");

            // 等待所有完成
            for (long id : requestIds) {
                executor.awaitResponse(id, 5000);
                System.out.println("Request " + id + " completed");
            }
        }
    }
}
```

### 5.3 在线推理服务 (Lab5 — 生产级)

```java
import tensorrt_llm.executor.*;
import tensorrt_llm.builder.*;
import java.util.concurrent.*;
import static tensorrt_llm.global.Executor.ModelType.kDECODER_ONLY;

/**
 * 线程安全的推理服务，可集成到 Spring Boot / Vert.x
 */
public class InferenceService implements AutoCloseable {

    private final Executor executor;
    private final ConcurrentHashMap<Long, CompletableFuture<int[]>> pendingRequests
        = new ConcurrentHashMap<>();

    public InferenceService(String engineDir) {
        KvCacheConfig kv = new KvCacheConfig();
        kv.setEnableBlockReuse(true);
        kv.setFreeGpuMemoryFraction(0.85f);

        ExecutorConfig config = new ExecutorConfig();
        config.setKvCacheConfig(kv);
        config.setEnableChunkedContext(true);

        this.executor = new Executor(
            new BytePointer(engineDir), kDECODER_ONLY, config);
    }

    /**
     * 异步提交推理请求
     */
    public CompletableFuture<int[]> submitAsync(
            int[] inputTokenIds, int maxTokens,
            float temperature, float topP) {

        Request req = RequestBuilder.builder(inputTokenIds, maxTokens)
                .streaming(false)
                .endId(151645)
                .padId(151643)
                .samplingConfig(SamplingConfigBuilder.builder()
                        .temperature(temperature)
                        .topP(topP)
                        .build())
                .build();

        long requestId = executor.enqueueRequest(req);
        CompletableFuture<int[]> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);
        return future;
    }

    @Override
    public void close() {
        executor.close();
    }

    // ========== Spring Boot 集成示例 ==========
    // @RestController
    // public class LLMController {
    //     @Autowired InferenceService inferenceService;
    //
    //     @PostMapping("/generate")
    //     public Mono<String> generate(@RequestBody GenerateRequest req) {
    //         return Mono.fromFuture(
    //             inferenceService.submitAsync(req.tokens(), 256, 0.7f, 0.9f)
    //         ).map(tokens -> decode(tokens));
    //     }
    // }
}
```

### 5.4 Embedding 推理 (Lab4)

```java
import tensorrt_llm.executor.*;
import tensorrt_llm.builder.*;
import static tensorrt_llm.global.Executor.ModelType.kENCODER_ONLY;

public class EmbeddingInference {
    public static void main(String[] args) {
        String engineDir = "/path/to/embedding-engine";

        ExecutorConfig config = new ExecutorConfig();

        try (Executor executor = new Executor(
                new BytePointer(engineDir), kENCODER_ONLY, config)) {

            int[] inputTokenIds = {1, 2, 3, 4, 5};
            Request req = RequestBuilder.builder(inputTokenIds, 0)
                    .outputConfig(OutputConfig.EMBEDDINGS)
                    .build();

            long requestId = executor.enqueueRequest(req);
            System.out.println("Embedding request submitted: " + requestId);
        }
    }
}
```

### 5.5 多模态推理 (Qwen3-VL)

```java
import tensorrt_llm.executor.*;
import tensorrt_llm.builder.*;
import static tensorrt_llm.global.Executor.ModelType.kDECODER_ONLY;

public class MultimodalInference {
    public static void main(String[] args) {
        String engineDir = "/path/to/qwen3-vl-engine";

        ExecutorConfig config = new ExecutorConfig();
        config.setMaxBeamWidth(1);

        try (Executor executor = new Executor(
                new BytePointer(engineDir), kDECODER_ONLY, config)) {

            // 文本 + 图像 token (视觉 token 已由 vision encoder 转换)
            int[] multimodalTokens = {
                151644,  // <|im_start|>
                872,     // user
                151645,  // <|im_end|>
                // ... 视觉 token IDs ...
                // ... 文本 token IDs ...
            };

            Request req = RequestBuilder.builder(multimodalTokens, 512)
                    .endId(151645)
                    .padId(151643)
                    .samplingConfig(SamplingConfigBuilder.builder()
                            .temperature(0.1f).topP(0.8f).build())
                    .build();

            executor.enqueueRequest(req);
        }
    }
}
```

---

## 6. 依赖配置

### 6.1 Maven 依赖

```xml
<!-- pom.xml -->

<!-- 方式 1: 引入所有平台 native 库 (推荐生产环境) -->
<dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>tensorrt-llm-platform</artifactId>
    <version>0.17.0-1.5.13</version>
</dependency>

<!-- 方式 2: 只引入 Java API (自行管理 native 库) -->
<dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>tensorrt-llm</artifactId>
    <version>0.17.0-1.5.13</version>
</dependency>

<!-- 方式 3: 只引入特定平台 native 库 -->
<dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>tensorrt-llm</artifactId>
    <version>0.17.0-1.5.13</version>
    <classifier>linux-x86_64</classifier>
</dependency>

<!-- JavaCPP 核心 (必须) -->
<dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>javacpp</artifactId>
    <version>1.5.13</version>
</dependency>

<!-- CUDA bindings (可选，用于直接操作 CUDA 对象) -->
<dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>cuda</artifactId>
    <version>13.1-9.19-1.5.13</version>
</dependency>
```

### 6.2 Gradle 依赖

```groovy
dependencies {
    // 全平台 (推荐)
    implementation 'org.bytedeco:tensorrt-llm-platform:0.17.0-1.5.13'
    implementation 'org.bytedeco:javacpp:1.5.13'

    // 或仅 Linux x86_64
    implementation 'org.bytedeco:tensorrt-llm:0.17.0-1.5.13'
    implementation 'org.bytedeco:tensorrt-llm:0.17.0-1.5.13:linux-x86_64'
}
```

### 6.3 本地 Maven 仓库安装

```bash
# 运行打包脚本后，JARs 会自动安装到 ~/.m2
./scripts/build_platform_jars.sh

# 验证安装
ls ~/.m2/repository/org/bytedeco/tensorrt-llm/0.17.0-1.5.13/
# tensorrt-llm-0.17.0-1.5.13.jar
# tensorrt-llm-0.17.0-1.5.13-linux-x86_64.jar
# ...
```

---

## 7. 运行时环境要求

### 7.1 Linux x86_64 生产环境

```bash
# 必要的系统库
apt-get install -y \
    libcuda.so \        # CUDA Driver (来自 nvidia-driver)
    libcudart.so.12 \   # CUDA Runtime
    libcublas.so.12 \   # cuBLAS
    libnvinfer.so.10    # TensorRT Runtime

# 验证 GPU 驱动
nvidia-smi
nvcc --version

# 验证 JVM 加载 native 库
java -Dorg.bytedeco.javacpp.logger.debug=true \
     -cp "tensorrt-llm-platform-0.17.0-1.5.13.jar:*" \
     example.SimpleInference /path/to/engine
```

### 7.2 JVM 启动参数

```bash
java \
  -Xmx4g \                                    # JVM 堆内存
  -XX:+UseG1GC \                              # GC 策略
  -Djava.library.path=/path/to/native/libs \  # 备用 native 路径
  -Dorg.bytedeco.javacpp.maxretries=100 \    # JNI 加载重试
  -cp "dist/*:target/*" \
  example.SimpleInference /path/to/engine "prompt text" 256
```

### 7.3 Docker 运行

```dockerfile
FROM nvcr.io/nvidia/tensorrt:24.01-py3

RUN apt-get update && apt-get install -y openjdk-17-jre-headless

COPY dist/ /app/lib/
COPY target/trtllm-bridge-1.0.0.jar /app/lib/

WORKDIR /app
CMD ["java", "-cp", "lib/*", "example.SimpleInference", \
     "/models/qwen3-engine", "Hello, world!", "256"]
```

```bash
docker run --rm --gpus all \
    -v /path/to/engine:/models/qwen3-engine \
    trtllm-java-bridge
```

---

## 8. 常见问题

### Q1: `UnsatisfiedLinkError: libjniTRTLLM.so`

```
java.lang.UnsatisfiedLinkError: no jniTRTLLM in java.library.path
```

**解决:**
```bash
# 方式1: 确保 native JAR 在 classpath
java -cp "tensorrt-llm-0.17.0-1.5.13.jar:\
tensorrt-llm-0.17.0-1.5.13-linux-x86_64.jar:app.jar" Main

# 方式2: 设置 java.library.path
java -Djava.library.path=/path/to/extracted/libs -cp app.jar Main
```

### Q2: `Cannot find CUDA libraries`

```bash
# 检查 CUDA 库是否在 LD_LIBRARY_PATH
export LD_LIBRARY_PATH=/usr/local/cuda/lib64:$LD_LIBRARY_PATH
ldconfig -p | grep libcudart
```

### Q3: `OutOfMemoryError: GPU memory`

```java
// 调整 GPU 内存占用
KvCacheConfig kv = new KvCacheConfig();
kv.setFreeGpuMemoryFraction(0.70f);  // 降低到 70%
kv.setMaxNumTokens(4096);            // 限制最大 token 数
```

### Q4: macOS 无法运行推理

macOS 版本为 **stub JAR**，不包含真实的 CUDA/TensorRT 库。macOS 仅用于开发和编译 Java 代码，实际推理必须在 Linux + NVIDIA GPU 环境运行。

### Q5: 重新生成绑定 (更新到新版 TensorRT-LLM)

```bash
# 1. 更新 TensorRT-LLM
git -C /path/to/TensorRT-LLM pull

# 2. 修改 pom.xml 中的版本号
# <version>0.18.0-1.5.13</version>

# 3. 重新解析
mvn org.bytedeco:javacpp:parse \
    -Dtrtllm.include.path=/path/to/TensorRT-LLM/cpp/include

# 4. 检查并修复 InfoMap 冲突
cat parse.log | grep "WARNING\|ERROR"

# 5. 重新编译
mvn clean package -DskipTests
```

---

## 附录: 脚本快速参考

| 脚本 | 用途 | 运行环境 |
|------|------|---------|
| `scripts/build_linux_native.sh` | **一键编译 .so + 捆绑 CUDA/TRT 依赖** | **Linux + CUDA ← 推荐** |
| `scripts/build_all.sh` | 全量构建所有 JAR | Linux + CUDA |
| `scripts/build_platform_jars.sh` | 打包多平台 JAR (同步 JNI 源码) | macOS / Linux |
| `scripts/bundle_native_deps.sh` | 仅捆绑 CUDA 依赖到 native JAR | Linux + CUDA |
| `scripts/build_and_verify.sh` | 构建后验证 | Linux |
| `generate_jni.sh` | 生成 JNI C++ 代码 | Linux + CUDA |
| `compile_jni.sh` | 编译 JNI .so (旧版，单文件) | Linux + CUDA |
| `trtllm-native/linux-x86_64/.../compile_jni_linux.sh` | 独立编译 linux-x86_64 .so | Linux x86_64 + CUDA |

---

*文档版本: 2026-04-21 · TensorRT-LLM 0.17.0 · JavaCPP 1.5.13*

