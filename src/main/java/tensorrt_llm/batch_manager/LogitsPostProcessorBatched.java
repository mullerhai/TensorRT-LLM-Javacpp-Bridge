// Auto-generated JavaCPP wrapper for the C++ typedef ``tensorrt_llm::batch_manager::LogitsPostProcessorBatched``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.batch_manager;


import tensorrt_llm.presets.BatchmanagerConfig;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("tensorrt_llm::batch_manager::LogitsPostProcessorBatched")
@Properties(inherit = tensorrt_llm.presets.BatchmanagerConfig.class)
public class LogitsPostProcessorBatched extends Pointer {
    static { Loader.load(); }

    public LogitsPostProcessorBatched() { super((Pointer) null); allocate(); }
    public LogitsPostProcessorBatched(Pointer p) { super(p); }
    private native void allocate();
}
