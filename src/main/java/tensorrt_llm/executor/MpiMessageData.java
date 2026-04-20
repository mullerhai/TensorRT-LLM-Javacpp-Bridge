// Auto-generated JavaCPP wrapper for the C++ typedef ``tensorrt_llm::executor::MpiMessageData``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.executor;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("tensorrt_llm::executor::MpiMessageData")
@Properties(inherit = tensorrt_llm.presets.ExecutorConfig.class)
public class MpiMessageData extends Pointer {
    static { Loader.load(); }

    public MpiMessageData() { super((Pointer) null); allocate(); }
    public MpiMessageData(Pointer p) { super(p); }
    private native void allocate();
}
