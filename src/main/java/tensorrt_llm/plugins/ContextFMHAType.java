// Auto-generated JavaCPP wrapper for the C++ typedef ``tensorrt_llm::plugins::ContextFMHAType``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.plugins;


import tensorrt_llm.presets.PluginsConfig;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("tensorrt_llm::plugins::ContextFMHAType")
@Properties(inherit = tensorrt_llm.presets.PluginsConfig.class)
public class ContextFMHAType extends IntPointer {
    static { Loader.load(); }

    public ContextFMHAType() { super((Pointer) null); allocate(); }
    public ContextFMHAType(Pointer p) { super(p); }
    private native void allocate();
}
