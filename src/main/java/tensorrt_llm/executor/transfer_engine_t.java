// Auto-generated JavaCPP wrapper for the C++ typedef ``mooncake::transfer_engine_t``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.executor;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("mooncake::transfer_engine_t")
@Properties(inherit = tensorrt_llm.presets.ExecutorConfig.class)
public class transfer_engine_t extends Pointer {
    static { Loader.load(); }

    public transfer_engine_t() { super((Pointer) null); allocate(); }
    public transfer_engine_t(Pointer p) { super(p); }
    private native void allocate();
}
