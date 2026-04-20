// Auto-generated JavaCPP wrapper for the C++ typedef ``nvinfer1::IPluginV3``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.plugins;


import tensorrt_llm.presets.PluginsConfig;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("nvinfer1::IPluginV3")
@Properties(inherit = tensorrt_llm.presets.PluginsConfig.class)
public class IPluginV3 extends Pointer {
    static { Loader.load(); }

    public IPluginV3() { super((Pointer) null); allocate(); }
    public IPluginV3(Pointer p) { super(p); }
    private native void allocate();
}
