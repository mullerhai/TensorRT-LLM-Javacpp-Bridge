// Stub class for nvinfer1::Dims - generated for TRT-LLM Java Bridge

package tensorrt_llm.runtime;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Namespace("nvinfer1") @Properties(inherit = tensorrt_llm.presets.RuntimeConfig.class)
public class Dims extends Pointer {
    static { Loader.load(); }
    public Dims() { super((Pointer)null); allocate(); }
    private native void allocate();
    public Dims(Pointer p) { super(p); }
    public Dims(long size) { super((Pointer)null); allocateArray(size); }
    private native void allocateArray(long size);

    public static final int MAX_DIMS = 8;

    public native int nbDims(); public native Dims nbDims(int setter);
    // d[MAX_DIMS]
}

