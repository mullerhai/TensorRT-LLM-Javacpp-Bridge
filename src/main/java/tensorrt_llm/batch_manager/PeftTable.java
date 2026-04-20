// Hand-written JavaCPP wrapper for the PEFT cache table typedef:
//     using PeftTable = std::unordered_map<
//         uint64_t,
//         std::vector<runtime::LoraCache::TaskLayerModuleConfig>>;
// defined both as ``BasePeftCacheManager::PeftTable`` and re-aliased as
// ``runtime::LoraManager::PeftTable``.  Because the map's value type involves
// a nested ``std::vector`` of a non-trivial C++ class, it cannot be modelled
// directly by JavaCPP's ``@StdMap``; instead we expose it as an opaque
// reference whose instances are owned by native code (or freshly allocated);
// here when a Java-side empty table is needed).

package tensorrt_llm.batch_manager;


import tensorrt_llm.runtime.LoraManager;
import tensorrt_llm.presets.BatchmanagerConfig;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

@Name("tensorrt_llm::batch_manager::BasePeftCacheManager::PeftTable")
@Properties(inherit = tensorrt_llm.presets.BatchmanagerConfig.class)
public class PeftTable extends Pointer {
    static { Loader.load(); }

    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public PeftTable(Pointer p) { super(p); }

    /** Allocate a new, empty native ``Pointer``. */
    public PeftTable() { super((Pointer) null); allocate(); }
    private native void allocate();

    /** Number of (requestId &rarr; task layer module config list) entries. */
    public native @Cast("size_t") long size();

    /** Remove all entries from the underlying ``Pointer``. */
    public native void clear();
}

