# trtllm-javacpp 模块（JavaCPP 生成器）

这个模块是一个生成绑定的脚手架：它配置了 JavaCPP（org.bytedeco:javacpp），你需要在本机把 `trtllm.headers` 指向真实的 C++ 源树（例如 `/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/tensorrt_llm`），然后运行生成命令。该模块不会自动伪造绑定；JavaCPP 会基于真实头文件解析并生成 JNI/Java 源。

快速使用：

1. 编辑 `pom.xml` 的 `<trtllm.headers>` 属性，或在命令行设置环境变量 `TRTLLM_HEADERS` 指向你的 C++ 头文件根目录（包含 `tensorrt_llm` 子目录）。

2. 生成 macOS arm64 的绑定并编译本地库：

```zsh
cd /Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/trtllm-javacpp
TRTLLM_HEADERS=/Users/mullerzhang/Documents/code/TensorRT-LLM/cpp/tensorrt_llm ./generate_bindings.sh
```

3. 生成结果位置：
  - Java 源： `target/generated-sources/javacpp`
  - 本地库： `target/native` 或生成时输出到 Maven 本地仓库的 native 资源目录

4. 将生成的 Java 源合并到上游模块（例如 `trtllm-bridge/src/main/java`），并将本地库放到 `trtllm-native/<platform>/src/main/resources` 以供打包。

注意：
- 为确保可链接，你的系统需要安装与 C++ 源所依赖的第三方库（TensorRT、CUDA、cuBLAS 等）。可能需要在环境中设置 `DYLD_LIBRARY_PATH`（macOS）或在 Maven plugin 的配置中设置 linker options。
- `batch_manager` 包在 `src/main/java/tensorrt_llm/batch_manager/BatchManager.java` 有示例模板；真实方法签名和类映射请根据对应头文件调整。

如果你希望我继续：
- 我可以把完整的 `pom.xml` plugin `<configuration>`（包含编译器/linker flags、额外 include）写得更详细；
- 或者我可以在本仓库内把生成命令集成到顶级 `pom.xml` 或创建 `trtllm-native` 平台模块的打包脚本。

