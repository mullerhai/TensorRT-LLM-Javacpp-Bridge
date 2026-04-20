// Auto-generated JavaCPP wrapper for the C++ typedef ``std::chrono::duration<int64_t>``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.batch_manager;


import tensorrt_llm.presets.BatchmanagerConfig;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("Pointer::duration<int64_t>")
@Properties(inherit = tensorrt_llm.presets.BatchmanagerConfig.class)
public class duration extends LongPointer {
    static { Loader.load(); }

    public duration() { super((Pointer) null); allocate(); }
    public duration(Pointer p) { super(p); }
    private native void allocate();
}
