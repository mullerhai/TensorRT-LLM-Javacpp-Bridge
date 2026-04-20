// Hand-written JavaCPP wrapper for ``tensorrt_llm::batch_manager::RequestVector``
// (``std::vector<Pointer``).  The type is treated as an
// opaque reference so callers can pass it around without exposing the
// underlying@ByVal Pointer template, which JavaCPP cannot
// model directly.  Use {@link #RequestVector()} to obtain a fresh, empty
// native vector owned by the JVM.

package tensorrt_llm.batch_manager;


import tensorrt_llm.presets.BatchmanagerConfig;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("tensorrt_llm::batch_manager::RequestVector")
@Properties(inherit = tensorrt_llm.presets.BatchmanagerConfig.class)
public class RequestVector extends Pointer {
    static { Loader.load(); }

    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public RequestVector(Pointer p) { super(p); }

    /** Allocate a new, empty native ``RequestVector``. */
    public RequestVector() { super((Pointer) null); allocate(); }
    private native void allocate();

    /** Number of requests held by the underlying ``Pointer``. */
    public native @Cast("size_t") long size();

    /** Remove all requests from the underlying ``Pointer``. */
    public native void clear();
}

