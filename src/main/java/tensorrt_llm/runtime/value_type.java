// Auto-generated JavaCPP wrapper for the C++ typedef ``size_t``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.runtime;


import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("size_t")
@Properties(inherit = tensorrt_llm.presets.RuntimeConfig.class)
public class value_type extends LongPointer {
    static { Loader.load(); }

    public value_type() { super((Pointer) null); allocate(); }
    public value_type(Pointer p) { super(p); }
    private native void allocate();
}
