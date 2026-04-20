// Auto-generated JavaCPP wrapper for the C++ typedef ``nixl_xfer_dlist_t``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.executor;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("nixl_xfer_dlist_t")
@Properties(inherit = tensorrt_llm.presets.ExecutorConfig.class)
public class nixl_xfer_dlist_t extends Pointer {
    static { Loader.load(); }

    public nixl_xfer_dlist_t() { super((Pointer) null); allocate(); }
    public nixl_xfer_dlist_t(Pointer p) { super(p); }
    private native void allocate();
}
