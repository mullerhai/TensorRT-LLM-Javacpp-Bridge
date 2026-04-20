// Auto-generated JavaCPP wrapper for the C++ typedef ``nvinfer1::v_2_0::IStreamReaderV2``.
// This class exists because the underlying C++ type has no natural Java
// counterpart; the minimal shell is sufficient for JavaCPP to marshal values
// across method boundaries.  Do not add Java-side state here.

package tensorrt_llm.runtime;


import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("nvinfer1::v_2_0::IStreamReaderV2")
@Properties(inherit = tensorrt_llm.presets.RuntimeConfig.class)
public class IStreamReaderV2 extends Pointer {
    static { Loader.load(); }

    public IStreamReaderV2() { super((Pointer) null); allocate(); }
    public IStreamReaderV2(Pointer p) { super(p); }
    private native void allocate();
}
