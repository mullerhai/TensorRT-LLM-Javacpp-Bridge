// Auto-generated JavaCPP wrapper for the C++ typedef ``tensorrt_llm::runtime::MoeLoadBalanceStatisticInfo``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.runtime;


import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("tensorrt_llm::runtime::MoeLoadBalanceStatisticInfo")
@Properties(inherit = tensorrt_llm.presets.RuntimeConfig.class)
public class MoeLoadBalanceStatisticInfo extends Pointer {
    static { Loader.load(); }

    public MoeLoadBalanceStatisticInfo() { super((Pointer) null); allocate(); }
    public MoeLoadBalanceStatisticInfo(Pointer p) { super(p); }
    private native void allocate();
}
