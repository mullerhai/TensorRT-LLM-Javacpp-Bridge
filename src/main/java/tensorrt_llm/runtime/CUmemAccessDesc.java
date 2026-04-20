// Auto-generated JavaCPP wrapper for the C++ typedef ``CUmemAccessDesc``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.runtime;


import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("CUmemAccessDesc")
@Properties(inherit = tensorrt_llm.presets.RuntimeConfig.class)
public class CUmemAccessDesc extends Pointer {
    static { Loader.load(); }

    public CUmemAccessDesc() { super((Pointer) null); allocate(); }
    public CUmemAccessDesc(Pointer p) { super(p); }
    private native void allocate();
}
