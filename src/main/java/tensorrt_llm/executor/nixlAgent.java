// Auto-generated JavaCPP wrapper for the C++ typedef ``nixlAgent``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.executor;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("nixlAgent")
@Properties(inherit = tensorrt_llm.presets.ExecutorConfig.class)
public class nixlAgent extends Pointer {
    static { Loader.load(); }

    public nixlAgent() { super((Pointer) null); allocate(); }
    public nixlAgent(Pointer p) { super(p); }
    private native void allocate();
}
