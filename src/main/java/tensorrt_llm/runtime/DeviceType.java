// Auto-generated JavaCPP wrapper for the C++ typedef ``c10::DeviceType``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.runtime;


import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("c10::DeviceType")
@Properties(inherit = tensorrt_llm.presets.RuntimeConfig.class)
public class DeviceType extends IntPointer {
    static { Loader.load(); }

    public DeviceType() { super((Pointer) null); allocate(); }
    public DeviceType(Pointer p) { super(p); }
    private native void allocate();
}
