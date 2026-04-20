// Auto-generated JavaCPP wrapper for the C++ typedef ``nvinfer1::TensorRTPhase``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.plugins;


import tensorrt_llm.presets.PluginsConfig;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("nvinfer1::TensorRTPhase")
@Properties(inherit = tensorrt_llm.presets.PluginsConfig.class)
public class TensorRTPhase extends IntPointer {
    static { Loader.load(); }

    public TensorRTPhase() { super((Pointer) null); allocate(); }
    public TensorRTPhase(Pointer p) { super(p); }
    private native void allocate();
}
