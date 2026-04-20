// Auto-generated JavaCPP wrapper for the C++ typedef ``CUdeviceptr``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.runtime;


import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("CUdeviceptr")
@Properties(inherit = tensorrt_llm.presets.RuntimeConfig.class)
public class CUdeviceptr extends LongPointer {
    static { Loader.load(); }

    public CUdeviceptr() { super((Pointer) null); allocate(); }
    public CUdeviceptr(Pointer p) { super(p); }
    private native void allocate();
}
