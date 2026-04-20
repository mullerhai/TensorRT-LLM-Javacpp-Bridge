// Auto-generated JavaCPP wrapper for the C++ typedef ``tensorrt_llm::executor::TransferOp``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.executor;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("tensorrt_llm::executor::TransferOp")
@Properties(inherit = tensorrt_llm.presets.ExecutorConfig.class)
public class TransferOp extends IntPointer {
    static { Loader.load(); }

    public TransferOp() { super((Pointer) null); allocate(); }
    public TransferOp(Pointer p) { super(p); }
    private native void allocate();
}
