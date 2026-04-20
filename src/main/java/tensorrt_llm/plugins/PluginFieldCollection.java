// Auto-generated JavaCPP wrapper for the C++ typedef ``nvinfer1::PluginFieldCollection``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.plugins;


import tensorrt_llm.presets.PluginsConfig;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("nvinfer1::PluginFieldCollection")
@Properties(inherit = tensorrt_llm.presets.PluginsConfig.class)
public class PluginFieldCollection extends Pointer {
    static { Loader.load(); }

    public PluginFieldCollection() { super((Pointer) null); allocate(); }
    public PluginFieldCollection(Pointer p) { super(p); }
    private native void allocate();
}
