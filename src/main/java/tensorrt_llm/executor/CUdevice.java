// Auto-generated JavaCPP wrapper for the C++ typedef ``CUdevice``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.executor;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("CUdevice")
@Properties(inherit = tensorrt_llm.presets.ExecutorConfig.class)
public class CUdevice extends IntPointer {
    static { Loader.load(); }

    public CUdevice() { super((Pointer) null); allocate(); }
    public CUdevice(Pointer p) { super(p); }
    private native void allocate();
}
