// Auto-generated JavaCPP wrapper for the C++ typedef ``nvinfer1::DynamicPluginTensorDesc``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.plugins;


import tensorrt_llm.presets.PluginsConfig;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("nvinfer1::DynamicPluginTensorDesc")
@Properties(inherit = tensorrt_llm.presets.PluginsConfig.class)
public class DynamicPluginTensorDesc extends Pointer {
    static { Loader.load(); }

    public DynamicPluginTensorDesc() { super((Pointer) null); allocate(); }
    public DynamicPluginTensorDesc(Pointer p) { super(p); }
    private native void allocate();
}
