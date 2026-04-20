// Auto-generated JavaCPP wrapper for the C++ typedef ``tensorrt_llm::runtime::MoePlacementInfo``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.runtime;


import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("tensorrt_llm::runtime::MoePlacementInfo")
@Properties(inherit = tensorrt_llm.presets.RuntimeConfig.class)
public class MoePlacementInfo extends Pointer {
    static { Loader.load(); }

    public MoePlacementInfo() { super((Pointer) null); allocate(); }
    public MoePlacementInfo(Pointer p) { super(p); }
    private native void allocate();
}
