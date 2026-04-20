// Auto-generated JavaCPP wrapper for the C++ typedef ``tensorrt_llm::batch_manager::ContextChunkingPolicy``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.batch_manager;


import tensorrt_llm.presets.BatchmanagerConfig;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("tensorrt_llm::batch_manager::ContextChunkingPolicy")
@Properties(inherit = tensorrt_llm.presets.BatchmanagerConfig.class)
public class ContextChunkingPolicy extends IntPointer {
    static { Loader.load(); }

    public ContextChunkingPolicy() { super((Pointer) null); allocate(); }
    public ContextChunkingPolicy(Pointer p) { super(p); }
    private native void allocate();
}
