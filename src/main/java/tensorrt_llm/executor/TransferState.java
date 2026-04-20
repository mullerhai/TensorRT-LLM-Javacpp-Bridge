// Auto-generated JavaCPP wrapper for the C++ typedef ``tensorrt_llm::executor::TransferState``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.executor;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("tensorrt_llm::executor::TransferState")
@Properties(inherit = tensorrt_llm.presets.ExecutorConfig.class)
public class TransferState extends Pointer {
    static { Loader.load(); }

    public TransferState() { super((Pointer) null); allocate(); }
    public TransferState(Pointer p) { super(p); }
    private native void allocate();
}
