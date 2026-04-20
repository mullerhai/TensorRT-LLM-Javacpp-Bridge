// Auto-generated JavaCPP wrapper for the C++ typedef ``std::chrono::system_clock::time_point``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.executor;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("Pointer::system_clock::time_point")
@Properties(inherit = tensorrt_llm.presets.ExecutorConfig.class)
public class time_point extends Pointer {
    static { Loader.load(); }

    public time_point() { super((Pointer) null); allocate(); }
    public time_point(Pointer p) { super(p); }
    private native void allocate();
}
