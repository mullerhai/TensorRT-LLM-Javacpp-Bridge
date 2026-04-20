// Auto-generated JavaCPP wrapper for the C++ typedef ``tensorrt_llm::runtime::LoraTaskIdType``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.batch_manager;


import tensorrt_llm.presets.BatchmanagerConfig;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("tensorrt_llm::runtime::LoraTaskIdType")
@Properties(inherit = tensorrt_llm.presets.BatchmanagerConfig.class)
public class LoraTaskIdType extends LongPointer {
    static { Loader.load(); }

    public LoraTaskIdType() { super((Pointer) null); allocate(); }
    public LoraTaskIdType(Pointer p) { super(p); }
    private native void allocate();
}
