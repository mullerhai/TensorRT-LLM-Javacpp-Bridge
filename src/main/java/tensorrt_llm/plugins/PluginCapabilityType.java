// Auto-generated JavaCPP wrapper for the C++ typedef ``nvinfer1::PluginCapabilityType``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.plugins;


import tensorrt_llm.presets.PluginsConfig;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("nvinfer1::PluginCapabilityType")
@Properties(inherit = tensorrt_llm.presets.PluginsConfig.class)
public class PluginCapabilityType extends IntPointer {
    static { Loader.load(); }

    public PluginCapabilityType() { super((Pointer) null); allocate(); }
    public PluginCapabilityType(Pointer p) { super(p); }
    private native void allocate();
}
