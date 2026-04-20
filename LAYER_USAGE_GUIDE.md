# TensorRT-LLM JavaCPP Layer类使用指南

## 📋 概述

本项目通过JavaCPP工具自动生成了TensorRT-LLM的所有Layer类的Java绑定，共计**15个Layer类**。

## ✅ 已生成的Layer类清单

### 1. 核心Layer类
- ✅ **BaseLayer** - 所有layer的基类
- ✅ **DecodingLayer** - 主解码层，调度到具体算法
- ✅ **DynamicDecodeLayer** - 动态解码层（主入口）

### 2. 采样Layer类
- ✅ **TopKSamplingLayer** - Top-K采样（最常用）
- ✅ **TopPSamplingLayer** - Top-P/Nucleus采样
- ✅ **SamplingLayer** - 综合采样层

### 3. 控制Layer类
- ✅ **PenaltyLayer** - 应用各种惩罚（repetition, presence, frequency）
- ✅ **BanWordsLayer** - 禁止特定token序列
- ✅ **StopCriteriaLayer** - 停止条件（EOS, max length, stop words）

### 4. 搜索Layer类
- ✅ **BeamSearchLayer** - Beam搜索解码

### 5. 投机解码Layer类（加速推理）
- ✅ **MedusaDecodingLayer** - Medusa投机解码
- ✅ **EagleDecodingLayer** - EAGLE投机解码
- ✅ **LookaheadDecodingLayer** - Lookahead投机解码
- ✅ **ExplicitDraftTokensLayer** - 显式draft tokens
- ✅ **ExternalDraftTokensLayer** - 外部draft tokens

## 🔧 生成流程

所有Layer类都是通过JavaCPP自动生成，**不是手动编写**：

```bash
# 1. 编译preset配置
mvn compile -DskipTests

# 2. 运行JavaCPP parse生成Java绑定
mvn org.bytedeco:javacpp:parse

# 3. 验证生成的文件
find src/main/java/org/bytedeco/tensorrt_llm -name "*Layer.java"
```

## 📝 使用示例

### 示例1：TopKSamplingLayer - Top-K采样

```java
import org.bytedeco.tensorrt_llm.*;
import org.bytedeco.javacpp.*;

// 创建TopKSamplingLayer
int maxBatchSize = 4;
int vocabSize = 50257;  // GPT-2 vocab size
int vocabSizePadded = 50304;
Pointer stream = ...; // CUDA stream
Pointer allocator = ...; // Memory allocator

TopKSamplingLayer layer = new TopKSamplingLayer(
    maxBatchSize, vocabSize, vocabSizePadded, stream, allocator
);

// 配置参数
SamplingSetupParams setupParams = new SamplingSetupParams();
setupParams.setTopK(50); // Top-50 sampling

// 执行sampling
layer.setup(batchSize, beamWidth, batchSlots, setupParams, workspace);
layer.forwardAsync(outputs, inputs, workspace);
```

### 示例2：DynamicDecodeLayer - 完整推理

```java
// DynamicDecodeLayer是主要入口
DynamicDecodeLayer decoder = new DynamicDecodeLayer(
    maxBatchSize, maxBeamWidth, vocabSize, vocabSizePadded, stream, allocator
);

// Setup阶段
DynamicDecodeSetupParams setupParams = new DynamicDecodeSetupParams();
decoder.setup(batchSize, beamWidth, batchSlots, setupParams, workspace);

// Forward阶段
BaseDecodingOutputs outputs = ...;
DecodingInputs inputs = ...;
decoder.forwardAsync(outputs, inputs, workspace);
```

### 示例3：PenaltyLayer - 应用惩罚

```java
PenaltyLayer penaltyLayer = new PenaltyLayer(
    maxBatchSize, maxSeqLength, vocabSize, vocabSizePadded, stream, allocator
);

// 配置惩罚参数
PenaltySetupParams params = new PenaltySetupParams();
// repetitionPenalty > 1.0 会惩罚重复
// presencePenalty 降低已出现过的tokens概率
```

### 示例4：BeamSearchLayer - Beam搜索

```java
int beamWidth = 4;
BeamSearchLayer beamSearch = new BeamSearchLayer(
    maxBatchSize, beamWidth, vocabSize, vocabSizePadded, stream, allocator
);

BeamSearchSetupParams params = new BeamSearchSetupParams();
// 配置beam search参数
```

## 🧪 测试用例

已提供完整的测试用例：

```bash
# 运行测试
mvn test -Dtest=LayerClassTest
mvn test -Dtest=LayerUsageExample
```

测试内容：
1. ✅ 验证所有15个Layer类存在
2. ✅ 验证继承关系
3. ✅ 验证Pointer构造函数
4. ✅ 使用示例和最佳实践

## 📦 打包

```bash
# 完整构建和打包
mvn clean package -DskipTests

# 生成的JAR
target/trtllm-bridge-1.0.0.jar
```

## 🎯 典型应用场景

### 场景1：文本生成（Top-K采样）
```
Executor → DynamicDecodeLayer → TopKSamplingLayer
```

### 场景2：高质量翻译（Beam搜索）
```
Executor → DynamicDecodeLayer → BeamSearchLayer
```

### 场景3：加速推理（投机解码）
```
Executor → DynamicDecodeLayer → MedusaDecodingLayer/EagleDecodingLayer
```

## ⚠️ 注意事项

1. **Native库依赖**：这些Java类需要TensorRT-LLM的native库（.so/.dylib/.dll）才能运行
2. **CUDA环境**：需要NVIDIA GPU和CUDA环境
3. **Memory管理**：Layer对象内部使用native内存，使用后需要适当释放
4. **线程安全**：Layer类通常不是线程安全的，需要外部同步

## 🔗 相关文档

- [JavaCPP文档](https://github.com/bytedeco/javacpp)
- [TensorRT-LLM C++ API](https://github.com/NVIDIA/TensorRT-LLM)
- [项目示例代码](src/main/java/example/)

## 🐛 故障排除

### 问题1：ClassNotFoundException
**解决**：确保运行了 `mvn org.bytedeco:javacpp:parse` 生成所有类

### 问题2：UnsatisfiedLinkError
**解决**：确保TensorRT-LLM native库在java.library.path中

### 问题3：编译错误
**解决**：运行 `./build_and_verify.sh` 检查完整的构建流程

---

**最后更新**: 2026-03-20
**JavaCPP版本**: 1.5.13
**TensorRT-LLM版本**: 0.17.0

