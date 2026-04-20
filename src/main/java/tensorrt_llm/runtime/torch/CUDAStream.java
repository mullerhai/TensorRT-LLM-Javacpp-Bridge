// JavaCPP wrapper for ``c10::cuda::CUDAStream`` (libtorch CUDA stream handle).
//
// Placed in the ``tensorrt_llm.runtime.torch`` sub-package because the simple
// name ``CUDAStream`` would otherwise collide on case-insensitive file
// systems (macOS / Windows) with the existing JavaCPP-generated
// ``tensorrt_llm.runtime.CudaStream`` (for ``tensorrt_llm::runtime::CudaStream``).
package tensorrt_llm.runtime.torch;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("c10::cuda::CUDAStream")
@Properties(inherit = tensorrt_llm.presets.RuntimeConfig.class)
public class CUDAStream extends Pointer {
    static { Loader.load(); }

    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public CUDAStream(Pointer p) { super(p); }

    /** Allocate a new, empty native ``c10::cuda::CUDAStream``. */
    public CUDAStream() { super((Pointer) null); allocate(); }
    private native void allocate();
}

