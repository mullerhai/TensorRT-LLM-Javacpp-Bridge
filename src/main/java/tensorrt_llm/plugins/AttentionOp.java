// Auto-generated JavaCPP wrapper for the C++ typedef ``tensorrt_llm::kernels::AttentionOp``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.plugins;


import tensorrt_llm.presets.PluginsConfig;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("tensorrt_llm::kernels::AttentionOp")
@Properties(inherit = tensorrt_llm.presets.PluginsConfig.class)
public class AttentionOp extends Pointer {
    static { Loader.load(); }

    public AttentionOp() { super((Pointer) null); allocate(); }
    public AttentionOp(Pointer p) { super(p); }
    private native void allocate();
}
