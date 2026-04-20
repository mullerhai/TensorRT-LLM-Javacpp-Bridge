// Auto-generated JavaCPP wrapper for the C++ typedef ``std::chrono::milliseconds``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.batch_manager;


import tensorrt_llm.presets.BatchmanagerConfig;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("Pointer::milliseconds")
@Properties(inherit = tensorrt_llm.presets.BatchmanagerConfig.class)
public class milliseconds extends LongPointer {
    static { Loader.load(); }

    public milliseconds() { super((Pointer) null); allocate(); }
    public milliseconds(Pointer p) { super(p); }
    private native void allocate();
}
