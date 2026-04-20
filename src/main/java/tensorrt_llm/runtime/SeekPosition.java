// Auto-generated JavaCPP wrapper for the C++ typedef ``nvinfer1::SeekPosition``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.runtime;


import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("nvinfer1::SeekPosition")
@Properties(inherit = tensorrt_llm.presets.RuntimeConfig.class)
public class SeekPosition extends IntPointer {
    static { Loader.load(); }

    public SeekPosition() { super((Pointer) null); allocate(); }
    public SeekPosition(Pointer p) { super(p); }
    private native void allocate();
}
