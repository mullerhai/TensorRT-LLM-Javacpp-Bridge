// Auto-generated JavaCPP wrapper for the C++ typedef ``c10::Device``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.runtime;


import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("c10::Device")
@Properties(inherit = tensorrt_llm.presets.RuntimeConfig.class)
public class Device extends Pointer {
    static { Loader.load(); }

    public Device() { super((Pointer) null); allocate(); }
    public Device(Pointer p) { super(p); }
    private native void allocate();
}
