// Auto-generated JavaCPP wrapper for the C++ typedef ``tensorrt_llm::batch_manager::RequestIdType`` (underlying type: long).

package tensorrt_llm.batch_manager;

import tensorrt_llm.presets.BatchmanagerConfig;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

/** Wrapper for the C++ RequestIdType typedef (a 64-bit integer request identifier). */
@Name("tensorrt_llm::batch_manager::RequestIdType")
@Properties(inherit = tensorrt_llm.presets.BatchmanagerConfig.class)
public class RequestIdType extends LongPointer {
    static { Loader.load(); }

    public RequestIdType() { super((Pointer) null); allocate(); }
    public RequestIdType(Pointer p) { super(p); }
    private native void allocate();
}
