// Auto-generated JavaCPP wrapper for the C++ typedef ``tensorrt_llm::executor::RetentionPriority`` (underlying type: int).

package tensorrt_llm.batch_manager;

import tensorrt_llm.presets.BatchmanagerConfig;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

/** Wrapper for the C++ RetentionPriority typedef (a 32-bit integer priority value). */
@Name("tensorrt_llm::executor::RetentionPriority")
@Properties(inherit = tensorrt_llm.presets.BatchmanagerConfig.class)
public class RetentionPriority extends IntPointer {
    static { Loader.load(); }

    public RetentionPriority() { super((Pointer) null); allocate(); }
    public RetentionPriority(Pointer p) { super(p); }
    private native void allocate();
}
