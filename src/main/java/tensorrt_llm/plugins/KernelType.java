// Auto-generated JavaCPP wrapper for the C++ typedef ``tensorrt_llm::plugins::KernelType``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.plugins;


import tensorrt_llm.presets.PluginsConfig;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("tensorrt_llm::plugins::KernelType")
@Properties(inherit = tensorrt_llm.presets.PluginsConfig.class)
public class KernelType extends IntPointer {
    static { Loader.load(); }

    public KernelType() { super((Pointer) null); allocate(); }
    public KernelType(Pointer p) { super(p); }
    private native void allocate();
}
