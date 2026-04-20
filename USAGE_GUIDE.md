# 📖 TensorRT-LLM JavaCPP 使用指南

**版本**: 1.0.0  
**基于**: JavaCPP 1.5.13 + CUDA 13.1

---

## 🎯 **快速开始**

### **1. 编译项目**

```bash
cd /Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge
mvn clean compile -DskipTests
```

### **2. 打包JAR**

```bash
mvn clean package -DskipTests
# 输出: target/trtllm-bridge-1.0.0.jar
```

### **3. 运行测试**

```bash
# 运行所有测试
mvn test

# 运行Kernel测试
mvn test -Dtest=KernelFunctionsTest
```

---

## 🔧 **API使用示例**

### **示例1: 使用DecodingLayer**

```java
import org.bytedeco.tensorrt_llm.*;
import org.bytedeco.javacpp.Pointer;

public class DecodingExample {
    public static void main(String[] args) {
        // 初始化参数
        executor.DecodingMode mode = new executor.DecodingMode();
        DecoderDomain domain = new DecoderDomain();
        
        // 创建Layer
        DecodingLayer layer = new DecodingLayer(mode, domain, bufferManager);
        
        // 设置Layer
        layer.setup(batchSize, beamWidth, batchSlots, setupParams, workspace);
        
        // 执行推理
        layer.forwardAsync(outputs, inputs, workspace);
        
        // 同步操作
        layer.forwardSync(outputs, inputs, workspace);
    }
}
```

### **示例2: 使用TopKSamplingLayer**

```java
import org.bytedeco.tensorrt_llm.*;

public class SamplingExample {
    public static void main(String[] args) {
        // 创建TopK采样Layer
        TopKSamplingLayer samplingLayer = new TopKSamplingLayer();
        
        // 设置采样参数
        TopKSamplingKernelParams params = new TopKSamplingKernelParams();
        
        // 执行采样
        samplingLayer.forwardAsync(outputs, inputs, workspace);
    }
}
```

### **示例3: 使用Beam Search Layer**

```java
import org.bytedeco.tensorrt_llm.*;

public class BeamSearchExample {
    public static void main(String[] args) {
        // 创建Beam Search Layer
        BeamSearchLayer beamLayer = new BeamSearchLayer();
        
        // 执行Beam Search解码
        beamLayer.forwardAsync(outputs, inputs, workspace);
    }
}
```

### **示例4: 使用注意力Layer**

```java
import org.bytedeco.tensorrt_llm.*;

public class AttentionExample {
    public static void main(String[] args) {
        // 创建注意力参数
        Multihead_attention_params params = new Multihead_attention_params();
        
        // 执行多头注意力计算
        // ... 使用params配置参数
    }
}
```

### **示例5: 使用Layer工厂**

```java
import org.bytedeco.tensorrt_llm.*;
import java.util.Vector;

public class LayerFactoryExample {
    public static void main(String[] args) {
        // 根据模式创建Layer类型
        Vector<Integer> layerTypes = LayersFactory.createDecodingLayerTypes(mode);
        
        // 创建Layer实例列表
        Vector<BaseLayer> layers = LayersFactory.createLayers(mode, domain, bufferManager);
    }
}
```

---

## 📦 **依赖和导入**

### **Maven依赖**

```xml
<dependency>
    <groupId>org.bytedeco.tensorrt_llm</groupId>
    <artifactId>trtllm-bridge</artifactId>
    <version>1.0.0</version>
</dependency>

<dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>cuda</artifactId>
    <version>13.1-9.19-1.5.13</version>
    <scope>compile</scope>
</dependency>
```

### **常见导入**

```java
import org.bytedeco.tensorrt_llm.*;           // 所有Layer和struct
import org.bytedeco.tensorrt_llm.executor.*;  // Executor相关
import org.bytedeco.tensorrt_llm.runtime.*;   // Runtime相关
import org.bytedeco.tensorrt_llm.kernels.*;   // Kernel相关
import org.bytedeco.tensorrt_llm.layers.*;    // Layer相关
```

---

## 🧪 **测试**

### **运行所有测试**

```bash
mvn test
```

### **运行特定测试**

```bash
mvn test -Dtest=KernelFunctionsTest
mvn test -Dtest=LayerTest
```

### **生成测试报告**

```bash
mvn test -DargLine="-Xmx2048m"
```

---

## 🚀 **部署**

### **Linux x86_64部署**

```bash
# 使用特定平台profile构建
mvn clean package -Plinux-x86_64

# 生成平台特定JAR
# target/trtllm-bridge-1.0.0-linux-x86_64.jar
```

### **Linux ARM64部署**

```bash
mvn clean package -Plinux-arm64
```

### **macOS ARM64部署**

```bash
mvn clean package -Pmacosx-arm64
```

### **全平台构建**

```bash
mvn clean package -Pall-platforms
```

---

## 🔍 **故障排除**

### **问题1: CUDA库未找到**

```bash
# 确保CUDA路径正确
export CUDA_HOME=/usr/local/cuda-13.1
export LD_LIBRARY_PATH=$CUDA_HOME/lib64:$LD_LIBRARY_PATH
```

### **问题2: JavaCPP错误**

```bash
# 清理缓存
mvn clean

# 重新编译
mvn compile -DskipTests
```

### **问题3: 编译失败**

```bash
# 检查Java版本 (需要11+)
java -version

# 检查Maven版本
mvn -v
```

---

## 📚 **API参考**

### **核心类**

| 类名 | 说明 | 文件 |
|-----|------|------|
| BaseLayer | Layer基类 | BaseLayer.java |
| DecodingLayer | 通用解码Layer | DecodingLayer.java |
| DynamicDecodeLayer | 动态解码Layer | DynamicDecodeLayer.java |
| TopKSamplingLayer | TopK采样 | TopKSamplingLayer.java |
| TopPSamplingLayer | TopP采样 | TopPSamplingLayer.java |
| BeamSearchLayer | Beam Search | BeamSearchLayer.java |
| LayersFactory | 工厂类 | LayersFactory.java |
| LayerUtils | 工具函数 | LayerUtils.java |

### **参数结构体**

| 类名 | 说明 | 文件 |
|-----|------|------|
| TopKSamplingKernelParams | TopK参数 | TopKSamplingKernelParams.java |
| TopPSamplingKernelParams | TopP参数 | TopPSamplingKernelParams.java |
| Multihead_attention_params | 注意力参数 | Multihead_attention_params.java |
| InvokeBatchApplyPenaltyParams | 罚项参数 | InvokeBatchApplyPenaltyParams.java |

---

## 🔗 **参考链接**

- [TensorRT-LLM官方](https://github.com/NVIDIA/TensorRT-LLM)
- [JavaCPP文档](https://github.com/bytedeco/javacpp)
- [CUDA文档](https://docs.nvidia.com/cuda/)

---

**文档版本**: 1.0.0  
**最后更新**: 2026-03-20  
**维护者**: GitHub Copilot


