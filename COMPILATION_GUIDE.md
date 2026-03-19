# TensorRT-LLM JavaCPP Binding - Compilation Guide

## 概述

本指南说明如何使用 JavaCPP 工具将 NVIDIA TensorRT-LLM C++ 库完整转译为 Java 代码。

## 项目结构

```
TRTLLM-Java-Bridge/
├── pom.xml                           # Maven 配置文件
├── src/main/java/
│   └── tensorrt_llm/
│       └── presets/
│           └── TRTLLMFullConfig.java # JavaCPP 转译配置
└── src/main/java/org/bytedeco/       # 生成的 Java 代码目录
    └── tensorrt_llm/
        ├── executor/                 # Executor API 绑定
        ├── runtime/                  # Runtime API 绑定
        ├── common/                   # Common utilities 绑定
        ├── batch_manager/            # Batch manager 绑定
        ├── kernels/                  # Kernels 绑定
        ├── layers/                   # Layers 绑定
        └── global/                   # Global constants
```

## 前置要求

### 系统依赖

1. **Java Development Kit (JDK) 11 或更高**
   ```bash
   java -version
   ```

2. **Maven 3.6 或更高**
   ```bash
   mvn -version
   ```

3. **Clang/LLVM** (用于 C++ 代码解析)
   ```bash
   # macOS (已安装)
   clang --version
   ```

4. **TensorRT-LLM 源代码**
   - 路径: `/Users/mullerzhang/Documents/code/TensorRT-LLM`
   - 需要包含 `cpp/include` 目录

### 环境变量配置

```bash
# 设置 TensorRT-LLM 源代码路径
export TRTLLM_ROOT=/Users/mullerzhang/Documents/code/TensorRT-LLM
export TRTLLM_INCLUDE=$TRTLLM_ROOT/cpp/include
export TRTLLM_LIB=$TRTLLM_ROOT/cpp/build
```

## 编译步骤

### 步骤 1: 验证源代码目录

```bash
# 检查 TensorRT-LLM 头文件
ls -la /Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/include/tensorrt_llm/

# 预期输出应包含:
# - common/
# - executor/
# - runtime/
# - batch_manager/
# - kernels/
```

### 步骤 2: JavaCPP 解析阶段

这一步会将 C++ 头文件解析为 JavaCPP 能理解的格式。

```bash
cd /Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge

# 运行 JavaCPP 解析器
mvn clean org.bytedeco:javacpp:parse
```

**说明:**
- JavaCPP 会根据 `TRTLLMFullConfig.java` 的配置解析 C++ 头文件
- 生成的 Java 源代码会放在 `src/main/java/org/bytedeco/` 目录下
- 解析过程可能需要 5-10 分钟

**故障排除:**
- 如果出现 "Unexpected token" 错误，通常表示某个宏或 C++ 语法不被支持
  - 解决方案: 在 `TRTLLMFullConfig.java` 的 `.skip()` 列表中添加问题项
- 如果找不到头文件，检查 `pom.xml` 中的 `includepath` 配置是否正确

### 步骤 3: Java 代码编译

```bash
# 编译生成的 Java 代码
mvn clean compile
```

**说明:**
- 这步验证生成的 Java 代码是否有语法错误
- 会在 `target/classes/` 下生成 `.class` 文件

### 步骤 4: 打包

```bash
# 生成 JAR 文件
mvn package
```

**输出:**
- JAR 文件位置: `target/trtllm-bridge-1.0.0.jar`

## 完整编译命令

一键编译 (推荐):

```bash
cd /Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge

# 清理 -> 解析 -> 编译 -> 打包
mvn clean org.bytedeco:javacpp:parse compile package
```

## 配置说明

### TRTLLMFullConfig.java 的 10 个阶段

#### Phase 1: Core Macro & Attribute Handling
- 处理 CUDA 特定的属性 (`__device__`, `__host__`, `__forceinline__`)
- 定义这些属性为空宏，使 CPU 端编译不出错

#### Phase 2: Function-Level Skip Rules
- 跳过无法解析的复杂函数 (如 `getDTypeSize`, `getDtypeString`)
- 这些函数通常包含 switch-case 或 constexpr 逻辑

#### Phase 3: Namespace to Java Package Mapping
- 将 C++ 命名空间映射到 Java 包
- 例: `tensorrt_llm::executor` → `org.bytedeco.tensorrt_llm.executor`

#### Phase 4: Smart Pointer & Container Handling
- 跳过智能指针 (`std::shared_ptr`, `std::unique_ptr`)
- 映射 `std::vector<T>` 到对应的指针类型 (`IntPointer`, `LongPointer` 等)

#### Phase 5: Inheritance Chain Flattening
- 展平继承链以避免复杂的虚继承问题
- 使 `ITensor` 等接口继承父类的所有方法

#### Phase 6: External Dependency Handling
- 跳过外部依赖 (`nvinfer1`, `NvInferRuntime.h`)
- 将 CUDA 类型映射为 `void*` 指针

#### Phase 7: Special Type Conversions
- 处理特殊类型 (`size_t`, `bool`, 整数类型)
- 映射到 Java 基本类型

#### Phase 8: Warning Suppression & Parser Fixes
- 抑制编译警告
- 处理 `typename` 等 C++ 关键字

#### Phase 9: Platform-Specific Handling
- 虚基类的 upcast 处理

#### Phase 10: Class Instantiation & Construction
- 确保核心类可以被实例化
- 跳过默认构造函数，使用显式构造函数

## 已知问题与解决方案

### 1. `dataType.h` 解析错误

**问题:** Unexpected token in `tensorrt_llm/common/dataType.h` at line 38

**原因:** 
- 文件包含 `switch-case` 语句和 `constexpr` 修饰符
- JavaCPP Parser 无法处理复杂的 constexpr 逻辑

**解决方案:**
```java
// TRTLLMFullConfig.java Phase 2
infoMap.put(new Info(
    "tensorrt_llm::common::getDTypeSize",
    "tensorrt_llm::common::getDTypeSizeInBits",
    "tensorrt_llm::common::getDtypeString"
).skip());
```

### 2. `defaultDecodingParams.h` CUDA 依赖错误

**问题:** 找不到 CUDA 头文件或无法解析 `.cu` 文件引用

**原因:**
- 某些头文件依赖 CUDA 特定的类型和函数
- macOS 没有 CUDA SDK

**解决方案:**
```java
// TRTLLMFullConfig.java Phase 2
infoMap.put(new Info(
    "tensorrt_llm::layers::DefaultDecodingParams::generateRandomSeed",
    "tensorrt_llm::layers::DefaultDecodingParams"
).skip());
```

### 3. 找不到 `NvInferRuntime.h`

**问题:** TensorRT 头文件不在系统中

**原因:** macOS 没有预装 TensorRT SDK

**解决方案:**
```java
// TRTLLMFullConfig.java Phase 6
infoMap.put(new Info("NvInferRuntime.h", "NvInfer.h")
    .skip());
infoMap.put(new Info("nvinfer1")
    .skip());
```

### 4. 智能指针导致的方法消失

**问题:** 很多使用 `std::shared_ptr<T>` 返回值的方法在生成的 Java 代码中消失

**原因:** JavaCPP 默认无法处理智能指针

**解决方案:**
```java
// TRTLLMFullConfig.java Phase 4
infoMap.put(new Info("std::shared_ptr", "std::unique_ptr")
    .skip());
```

## 验证编译结果

### 检查生成的文件

```bash
# 查看生成的 Java 文件
find src/main/java/org/bytedeco -type f -name "*.java" | head -20

# 统计生成的 Java 类数量
find src/main/java/org/bytedeco -type f -name "*.java" | wc -l

# 预期: 100+ 个 Java 类文件
```

### 编译测试

```bash
# 编译检查
mvn compile

# 预期输出:
# [INFO] BUILD SUCCESS
# [INFO] Total time: XX.XXX s
```

### 查看生成的类

```bash
# 列出主要生成的类
find target/classes/org/bytedeco/tensorrt_llm -type f -name "*.class" | \
  sed 's|.*/||' | sed 's|\.class||' | sort | head -30
```

## 参考项目

- **TensorRT JavaCPP 绑定:**
  https://github.com/bytedeco/javacpp-presets/tree/master/tensorrt

- **Llama.cpp JavaCPP 绑定:**
  https://github.com/ihmcrobotics/llama.cpp-javacpp

## 下一步

1. **编写使用示例:**
   - 创建 `ExecutorExample.java` 演示基本用法
   - 加载模型、执行推理

2. **测试绑定:**
   - 编写单元测试验证关键类的可用性
   - 测试类型转换 (C++ ↔ Java)

3. **性能优化:**
   - 分析生成的代码中是否有不必要的开销
   - 对关键路径进行优化

4. **文档完善:**
   - 记录所有生成的 Java 类和方法
   - 说明性能特性和限制

## 常见问题 (FAQ)

**Q: 为什么某些 C++ 类没有被生成？**
A: 通常是因为该类被标记为 `.skip()`。检查 `TRTLLMFullConfig.java` 的配置。

**Q: 生成的 JAR 文件很大吗？**
A: 是的。包含完整的 Java 绑定代码可能在 5-20 MB 范围内。

**Q: 能在 Windows 或 Linux 上使用吗？**
A: 可以。需要修改 `pom.xml` 中的路径和平台配置。

**Q: 性能如何？**
A: JavaCPP 绑定通常只在 JNI 调用处有小的开销，整体性能接近原生 C++ 代码。

## 获取帮助

如果遇到问题:

1. 检查 Maven 的详细输出: `mvn -X ...`
2. 查看生成的 Java 代码: `src/main/java/org/bytedeco/...`
3. 参考 JavaCPP 官方文档: https://github.com/bytedeco/javacpp
4. 检查 TensorRT-LLM 源代码中的注释和文档

