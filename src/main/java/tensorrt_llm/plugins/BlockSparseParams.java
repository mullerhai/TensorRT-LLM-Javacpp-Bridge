// Auto-generated JavaCPP wrapper for the C++ typedef ``tensorrt_llm::kernels::BlockSparseParams``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.plugins;


import tensorrt_llm.presets.PluginsConfig;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("tensorrt_llm::kernels::BlockSparseParams")
@Properties(inherit = tensorrt_llm.presets.PluginsConfig.class)
public class BlockSparseParams extends Pointer {
    static { Loader.load(); }

    public BlockSparseParams() { super((Pointer) null); allocate(); }
    public BlockSparseParams(Pointer p) { super(p); }
    private native void allocate();
}
