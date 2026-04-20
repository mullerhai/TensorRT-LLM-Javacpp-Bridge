// Auto-generated JavaCPP wrapper for the C++ typedef ``nvinfer1::v_1_0::IPluginV3OneBuild``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.plugins;


import tensorrt_llm.presets.PluginsConfig;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("nvinfer1::v_1_0::IPluginV3OneBuild")
@Properties(inherit = tensorrt_llm.presets.PluginsConfig.class)
public class IPluginV3OneBuild extends Pointer {
    static { Loader.load(); }

    public IPluginV3OneBuild() { super((Pointer) null); allocate(); }
    public IPluginV3OneBuild(Pointer p) { super(p); }
    private native void allocate();
}
