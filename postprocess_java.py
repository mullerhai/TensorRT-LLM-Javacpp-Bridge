#!/usr/bin/env python3
"""Post-process generated Java files to fix remaining C++ type leaks."""
import os
import re

BASE = "/Users/mullerzhang/IdeaProjects/TRTLLM-Java-Bridge/src/main/java/tensorrt_llm"

fixes_applied = 0

def fix_file(filepath):
    global fixes_applied
    with open(filepath, 'r') as f:
        content = f.read()

    original = content

    # Fix std::enable_shared_from_this<...> in extends clause
    content = re.sub(r'extends std::enable_shared_from_this<[^>]+>', 'extends Pointer', content)

    # Fix std::future<void> -> void
    content = re.sub(r'std::future<void>', 'void', content)

    # Fix std::deque<...> -> Pointer
    content = re.sub(r'std::deque<[^>]+>', 'Pointer', content)

    # Iteratively collapse all std:: template specializations (nested-safe).
    # Skip anything inside @Cast("..."), @Name("...") and comments.
    def collapse_std_templates(text):
        lines_out = []
        std_tpl_names = (
            'vector', 'shared_ptr', 'unique_ptr', 'weak_ptr', 'optional',
            'deque', 'list', 'set', 'map', 'unordered_map', 'unordered_set',
            'tuple', 'pair', 'function', 'array', 'variant', 'span',
            'initializer_list', 'multiset', 'multimap'
        )
        tpl_re = re.compile(r'std::(?:' + '|'.join(std_tpl_names) + r')\s*<[^<>]*>')
        for line in text.split('\n'):
            if line.lstrip().startswith('//') or line.lstrip().startswith('*'):
                lines_out.append(line)
                continue
            # Protect @Cast/@Name string literals
            parts = re.split(r'(@(?:Cast|Name)\("[^"]*"\))', line)
            new_parts = []
            for p in parts:
                if p.startswith('@Cast(') or p.startswith('@Name('):
                    new_parts.append(p)
                else:
                    prev = None
                    while prev != p:
                        prev = p
                        p = tpl_re.sub('Pointer', p)
                    new_parts.append(p)
            lines_out.append(''.join(new_parts))
        return '\n'.join(lines_out)

    content = collapse_std_templates(content)

    # Fix std::shared_ptr<...> -> Pointer (fallback for any non-nested remainders)
    content = re.sub(r'std::shared_ptr<[^>]+>', 'Pointer', content)

    # Fix c10::intrusive_ptr<...> -> Pointer
    content = re.sub(r'c10::intrusive_ptr<[^>]+>', 'Pointer', content)

    # Fix c10d::ProcessGroup -> Pointer
    content = re.sub(r'c10d::ProcessGroup', 'Pointer', content)

    # Fix runtime::StringPtrMap<...> -> Pointer
    content = re.sub(r'runtime::StringPtrMap<[^>]+>', 'Pointer', content)

    # Fix fully qualified C++ type references with nested templates: tensorrt_llm::...::OptionalRef<...>
    content = re.sub(r'tensorrt_llm::[a-zA-Z0-9_:]+::OptionalRef<[^>]*>', 'Pointer', content)

    # Fix fully qualified C++ namespaced types in method signatures (not in @Cast)
    # Match tensorrt_llm::...::SomeType that is NOT inside quotes
    def fix_cpp_qualified_types(line):
        if '@Cast(' in line:
            # Don't touch stuff inside @Cast("...")
            parts = re.split(r'(@Cast\("[^"]*"\))', line)
            result = []
            for p in parts:
                if p.startswith('@Cast('):
                    result.append(p)
                else:
                    # Fix bare qualified types not in Cast
                    p = re.sub(r'(?<!")tensorrt_llm::[a-zA-Z0-9_:]+(?![\w"])', 'Pointer', p)
                    result.append(p)
            return ''.join(result)
        else:
            return re.sub(r'tensorrt_llm::[a-zA-Z0-9_:]+', 'Pointer', line)

    # Fix milliseconds type
    content = re.sub(r'@Optional milliseconds ', '@Optional long ', content)

    # Fix TRTLLM_NAMESPACE_BEGIN lines
    content = re.sub(r'.*TRTLLM_NAMESPACE_BEGIN.*\n', '', content)

    # Fix __host__ as return type
    content = re.sub(r'@ByVal __host__', '@ByVal float', content)
    content = re.sub(r'@ByRef __host__', '@ByRef int', content)

    # Fix remaining UnderlyingType (not in @Cast)
    lines = content.split('\n')
    new_lines = []
    for line in lines:
        if 'UnderlyingType' in line and '@Cast' not in line:
            line = line.replace('UnderlyingType', 'int')
        # Fix bare tensorrt_llm:: qualified names that leaked through (not in @Cast strings or comments)
        if 'tensorrt_llm::' in line and not line.strip().startswith('//') and not line.strip().startswith('*'):
            # Replace outside @Cast("...") strings
            line = re.sub(r'(?<!")tensorrt_llm::[a-zA-Z0-9_:<>]+(?!")', 'Pointer', line)
        # Fix std:: qualified names (outside @Cast strings)
        if 'std::' in line and not line.strip().startswith('//') and not line.strip().startswith('*'):
            # Split on @Cast("...") to preserve those
            parts = re.split(r'(@Cast\("[^"]*"\))', line)
            new_parts = []
            for p in parts:
                if p.startswith('@Cast('):
                    new_parts.append(p)
                else:
                    p = re.sub(r'std::[a-zA-Z0-9_]+<[^>]*>', 'Pointer', p)
                    p = re.sub(r'std::[a-zA-Z0-9_]+', 'Pointer', p)
                    new_parts.append(p)
            line = ''.join(new_parts)
        new_lines.append(line)
    content = '\n'.join(new_lines)

    # Fix broken "Pointer >" from nested template replacements
    content = re.sub(r'Pointer\s*>', 'Pointer', content)
    # Fix "/*=Pointer*/" default values that should be "/*=std::nullopt*/"
    content = re.sub(r'/\*=Pointer\*/', '/*=std::nullopt*/', content)
    # Fix double Pointer Pointer
    content = re.sub(r'Pointer\s+Pointer', 'Pointer', content)

    # Fix invalid annotation placement inside enum-constant declarations generated
    # by JavaCPP for re-declared enum class aliases:
    #   public static final int
    #       @Name("...::Foo") Foo = Foo();
    # `@Name` has no TYPE_USE target, so javac rejects it. Drop the inline @Name;
    # the MemberGetter method above already carries the @Name mapping.
    content = re.sub(
        r'(public\s+static\s+final\s+int\s*(?:\r?\n)?\s*(?:/\*\*?[^*]*\*/\s*)*(?://[^\n]*\n\s*)*)'
        r'@Name\("[^"]*"\)\s+',
        r'\1',
        content
    )
    # Also strip @Name from individual enum-alias lines that follow a comma (chained)
    content = re.sub(
        r',\s*(?:\r?\n)?\s*@Name\("[^"]*"\)\s+',
        ',\n    ',
        content
    )

    # Strip @Name from enum-alias lines that follow a stand-alone comment/blank line
    # inside an enum group (after a previous `;` or `,`). General form:
    #   // comment or blank
    #   @Name("...") IDENT = IDENT();
    # Replace by dropping the @Name("...") prefix.
    content = re.sub(
        r'^(\s*)@Name\("[^"]*"\)\s+([A-Za-z_]\w*\s*=)',
        r'\1\2',
        content,
        flags=re.MULTILINE
    )

    # Collapse C++ multi-inheritance leak: `extends Pointer,Pointer` -> `extends Pointer`
    content = re.sub(r'extends\s+Pointer\s*(?:,\s*Pointer\s*)+', 'extends Pointer ', content)

    # Drop generated `operator <<` / `operator >>` / `operator ==` free-functions
    # which are invalid Java identifiers. Match a single-line native declaration.
    content = re.sub(
        r'^.*native[^\n;]*\boperator\s*(?:<<|>>|==|!=|<=|>=|\+=|-=|\*=|/=)[^\n;]*;\s*\n',
        '',
        content,
        flags=re.MULTILINE
    )

    # Clean up half-parsed OptionalRef/template fragments whose closing `>` was lost.
    # e.g. `@ByVal OptionalRef<const Pointer llmRequest);` -> `@ByVal Pointer llmRequest);`
    content = re.sub(
        r'(?:@ByVal|@ByRef|@Const|@Optional)?\s*'
        r'(?:tensorrt_llm::)?[A-Za-z_][\w:]*<\s*(?:const\s+)?Pointer\s+',
        '@ByVal Pointer ',
        content
    )

    # Clean up extends fragments like `extends Pointer ,false>` or `extends Pointer,true>`
    content = re.sub(r'extends\s+Pointer\s*,\s*(?:true|false)\s*>', 'extends Pointer', content)
    content = re.sub(r'extends\s+Pointer\s*,\s*[A-Za-z_][\w:<>,\s]*>', 'extends Pointer', content)

    # Dedupe repeated adjacent annotations like `@ByVal(...)@ByVal` or `@StdVector @StdVector`
    # which occur when postprocess injected an alias alongside a pre-existing annotation.
    def dedupe_adjacent_annotations(text):
        # Repeat until stable
        prev = None
        cur = text
        while prev != cur:
            prev = cur
            # @X("...any...")@X or @X("...")  @X => @X("...")
            cur = re.sub(r'(@([A-Za-z_][\w.]*)\("[^"]*"\))\s*@\2\b(?!\s*\()', r'\1', cur)
            # @X(foo = "...string with any chars including parens...") @X => @X(...)
            cur = re.sub(r'(@([A-Za-z_][\w.]*)\([A-Za-z_]\w*\s*=\s*"[^"]*"\))\s*@\2\b(?!\s*\()',
                         r'\1', cur)
            # Same annotation name twice (param-ised followed by bare):  @X(..) @X  -> @X(..)
            cur = re.sub(r'(@([A-Za-z_][\w.]*)\([^()]*\))\s*@\2\b(?!\s*\()', r'\1', cur)
            # Annotation with kv-style arguments whose value contains parens in a string
            cur = re.sub(r'(@([A-Za-z_][\w.]*)\([^)]*"[^"]*"[^)]*\))\s*@\2\b(?!\s*\()', r'\1', cur)
            # Bare same annotation repeated: `@X  @X`
            cur = re.sub(r'(@([A-Za-z_][\w.]*))\b(?!\s*\()\s+@\2\b(?!\s*\()', r'\1', cur)
        return cur
    content = dedupe_adjacent_annotations(content)

    # Rename native `toString` methods that return non-String (BytePointer/Pointer) since
    # they clash with Object.toString(). Map them to `toStringNative` with @Name("toString").
    content = re.sub(
        r'(public\s+(?:static\s+)?native\s+[^;]*?)\btoString\(',
        r'@Name("toString") \1toStringNative(',
        content
    )

    # Strip C-preprocessor artifact constants that leaked into generated globals.
    content = re.sub(
        r'^public\s+static\s+final\s+int\s+__PRETTY_FUNCTION__\s*=\s*__FUNCSIG__\s*;\s*\n',
        '', content, flags=re.MULTILINE
    )

    # Map unqualified executor/runtime type aliases that leaked into batch_manager/runtime
    # Java files without imports. They are mapped via presets to primitives/Pointer.
    primitive_aliases = {
        'RequestIdType': 'long',
        'IdType':        'long',
        'LoraTaskIdType': 'long',
        'CacheSaltIDType': 'long',
        'MillisecondsType': 'long',
        'TimePoint':      'long',
        'IterationType':  'long',
        'RandomSeedType': 'long',
        'PriorityType':   'float',
        'FloatType':      'float',
        'TokenIdType':    'int',
        'SizeType':       'int',
        'SizeType32':     'int',
        'SizeType64':     'long',
        # Executor enums collapsed to int when referenced bare without import
        'CapacitySchedulerPolicy': 'int',
        'KvCacheTransferMode':     'int',
        'ContextChunkingPolicy':   'int',
        'CommunicationType':       'int',
        'CommunicationMode':       'int',
        'RequestType':             'int',
        'RequestStage':            'int',
        'FinishReason':            'int',
        'MemoryType':              'int',
        'BatchingType':            'int',
        'ModelType':               'int',
        'RetentionPriority':       'int',
        'CacheType':               'int',
        'DataType':                'int',
    }
    pointer_aliases = (
        'TensorPtr', 'VecTokens', 'BeamTokens', 'VecTokenExtraIds', 'VecLogProbs',
        'VecUniqueTokens', 'StreamPtr', 'LogitsPostProcessor', 'LogitsPostProcessorMap',
        'LogitsPostProcessorBatched', 'MedusaChoices', 'EagleChoices', 'BufferView',
        'BaseAgentConfig', 'MpiComm',
        # Unresolved bare class refs from C++ that preset skipped
        'BeamHypotheses', 'Shape', 'ScalarType', 'PeftTable', 'PrefixKey',
        'BaseLoopbackAgent', 'QuantizationSFLayout', 'Inputs', 'cudaDataType_t',
        'IEngineInspector', 'SharedConstPtr',
        'Severity', 'AsciiChar',
        'CacheState', 'CommState', 'Connection', 'ConnectionManager',
        'DataContext', 'DataTransceiverState', 'TargetRanksInfo',
        'BaseCacheFormatter', 'CacheFormatter', 'RnnCacheFormatter', 'MLACacheFormatter',
        'CacheTransBufferManager', 'CacheTransferLayer',
        'TransferSession', 'RequestInfo',
        'CacheSender', 'CacheReceiver',
    )
    # Only rewrite usages OUTSIDE @Cast("...") strings and comments.
    # Also skip declarations (where the token follows `enum`, `class`, `interface`, `extends`, `implements`)
    # and enum-constant constructors (e.g. `private DataType(...)`, `DataType(0)`).
    # Skip global/*.java (these are enum+function container files with self-reference types)
    # and runtime/ITensor.java-like files where the type name is the enclosing class.
    is_global_file = '/global/' in filepath
    decl_kw = re.compile(r'\b(?:enum|class|interface|extends|implements)\s+$')
    def rewrite_aliases(line):
        if line.lstrip().startswith('//') or line.lstrip().startswith('*'):
            return line
        # Skip enum declaration line entirely
        if re.search(r'\b(?:enum|class|interface)\s+[A-Za-z_]\w*', line):
            return line
        parts = re.split(r'(@(?:Cast|Name)\("[^"]*"\))', line)
        out = []
        for p in parts:
            if p.startswith('@Cast(') or p.startswith('@Name('):
                out.append(p)
                continue
            if not is_global_file:
                for alias, prim in primitive_aliases.items():
                    p = re.sub(r'(?<![\w.])' + alias + r'\b(?!\s*\()', prim, p)
            for alias in pointer_aliases:
                p = re.sub(r'(?<![\w.])' + alias + r'\b(?!\s*\()', 'Pointer', p)
            out.append(p)
        return ''.join(out)
    content = '\n'.join(rewrite_aliases(l) for l in content.split('\n'))

    # Add imports for unqualified executor classes used in non-executor packages.
    pkg_match = re.search(r'^package\s+(tensorrt_llm\.[\w.]+);', content, re.MULTILINE)
    # Determine current file's primary class name (to avoid self-import collisions)
    own_class_match = re.search(r'^public\s+(?:class|enum|interface)\s+([A-Za-z_]\w*)', content, re.MULTILINE)
    own_class = own_class_match.group(1) if own_class_match else ''
    if pkg_match:
        own_pkg = pkg_match.group(1)
        executor_types = ('ContextPhaseParams', 'EagleConfig', 'GuidedDecodingParams',
                          'KvCacheRetentionConfig', 'LookaheadDecodingConfig',
                          'ParallelConfig', 'DebugConfig', 'PeftCacheConfig',
                          'SchedulerConfig', 'KvCacheConfig', 'DecodingMode',
                          'DecodingConfig', 'Model',
                          'OutputConfig', 'SamplingConfig', 'ExtendedRuntimePerfKnobConfig',
                          'ExternalDraftTokensConfig', 'OrchestratorConfig',
                          'SpeculativeDecodingConfig', 'AdditionalModelOutput',
                          'CacheTransceiverConfig', 'DynamicBatchConfig',
                          'DebugTensorsPerIteration', 'GuidedDecodingConfig',
                          'PromptTuningConfig', 'MropeConfig', 'MultimodalInput',
                          'RequestStats', 'IterationStats', 'DisServingRequestStats',
                          'KVCacheUpdatedData', 'Request', 'Response', 'Result')
        needed = set()
        for t in executor_types:
            if t == own_class:
                continue
            if own_pkg == 'tensorrt_llm.executor':
                continue  # executor package already has these
            if re.search(r'(?<![\w.])' + t + r'\b', content):
                if not re.search(r'^import\s+tensorrt_llm\.executor\.' + t + r'\b',
                                 content, re.MULTILINE):
                    needed.add(t)

        # Additional foreign packages.
        xpkg_needed = []
        xpkg_map = {
            'tensorrt_llm.kernels.KVCacheIndex':          'KVCacheIndex',
            'tensorrt_llm.runtime.RuntimeDefaults':       'RuntimeDefaults',
            'tensorrt_llm.runtime.EagleBuffers':          'EagleBuffers',
            'tensorrt_llm.runtime.ExplicitDraftTokensBuffers': 'ExplicitDraftTokensBuffers',
            'tensorrt_llm.runtime.LookaheadRuntimeBuffers':    'LookaheadRuntimeBuffers',
            'tensorrt_llm.runtime.LoraManager':           'LoraManager',
            'tensorrt_llm.runtime.PromptTuningParams':    'PromptTuningParams',
            'tensorrt_llm.runtime.SpeculativeDecodingModule': 'SpeculativeDecodingModule',
            'tensorrt_llm.common.Algorithm':              'Algorithm',
            'tensorrt_llm.common.QuantMode':              'QuantMode',
            'tensorrt_llm.kernels.AllReduceParams':       'AllReduceParams',
            'tensorrt_llm.batch_manager.BufferManager':   'BufferManager',
            'tensorrt_llm.layers.DecoderDomain':          'DecoderDomain',
            'tensorrt_llm.runtime.DecodingLayerWorkspace':'DecodingLayerWorkspace',
            'tensorrt_llm.runtime.ITensor':               'ITensor',
            'tensorrt_llm.global.Executor.DataType':      'DataType',
        }
        for fq, short in xpkg_map.items():
            if short == own_class:
                continue
            if short == pkg_match.group(1).rsplit('.', 1)[-1]:
                continue
            if re.search(r'(?<![\w.])' + short + r'\b', content) and \
               not re.search(r'^import\s+' + re.escape(fq) + r'\b',
                             content, re.MULTILINE) and \
               f'.{short}.' not in pkg_match.group(1) and \
               not content.count(f'package {fq.rsplit(".", 1)[0]};'):
                xpkg_needed.append(fq)

        imports = []
        for t in sorted(needed):
            imports.append(f'import tensorrt_llm.executor.{t};')
        for fq in xpkg_needed:
            imports.append(f'import {fq};')
        if imports:
            import_block = '\n'.join(imports)
            content = re.sub(
                r'(^package\s+[\w.]+;\s*\n)',
                r'\1' + import_block + '\n',
                content, count=1, flags=re.MULTILINE
            )

    # Remove self-referencing imports (e.g. `import tensorrt_llm.executor.SamplingConfig;`
    # inside `batch_manager/SamplingConfig.java`).
    if own_class:
        content = re.sub(
            r'^import\s+tensorrt_llm\.[\w.]+\.' + re.escape(own_class) + r';\s*\n',
            '', content, flags=re.MULTILINE
        )

    # Dedupe duplicate top-level field, method, and class declarations inside a file.
    # Only acts when the exact same declaration (by unique simple identifier) recurs.
    def dedupe_declarations(text):
        lines = text.split('\n')
        out = []
        seen_field = set()
        seen_method = set()
        seen_class = set()
        for line in lines:
            # Match enum-constant-style field names (chained multi-line form or single):
            #   `    Undefined = 0,` | `    NAME = NAME();` | `public static final int NAME = ...;`
            #   May have a trailing `// comment`.
            m = re.match(
                r'^\s*(?:public\s+static\s+final\s+int\s+)?([A-Za-z_]\w*)\s*=\s*[^;/]*[,;]\s*(?://.*)?$',
                line
            )
            if m and 'native' not in line and '@' not in line and ' class ' not in line \
                    and ' enum ' not in line and ' interface ' not in line:
                key = m.group(1)
                # Filter only names that look like enum/int constants (CamelCase or UPPER_CASE)
                if re.match(r'^(?:[A-Z][A-Za-z0-9_]*|[A-Z_][A-Z0-9_]*)$', key):
                    if key in seen_field:
                        continue
                    seen_field.add(key)
                    out.append(line); continue
            # Match a native member-getter/method line ending with `;`.
            # Parens inside the argument list can contain `@Cast("...")` with their
            # own parens, so we can't require `[^)]*` — just check the line ends
            # in `);` and has `native ` token and a method-name identifier before `(`.
            if re.search(r'\bnative\b', line) and line.rstrip().endswith(');') \
                    and 'operator' not in line:
                # Build an erasure-like signature by stripping all annotations,
                # return-type modifiers and parameter names, keeping only the
                # method name and parameter type tokens.
                sig_body = line.strip()
                # Iteratively remove @Anno(...) (possibly nested `("...")`)
                prev = None
                while prev != sig_body:
                    prev = sig_body
                    sig_body = re.sub(r'@[A-Za-z_][\w.]*\("[^"]*"\)', '', sig_body)
                    sig_body = re.sub(r'@[A-Za-z_][\w.]*\([^()]*\)', '', sig_body)
                sig_body = re.sub(r'@[A-Za-z_][\w.]*', '', sig_body)
                sig_body = re.sub(r'\bpublic\b|\bstatic\b|\bnative\b|\bfinal\b', '', sig_body)
                sig_body = re.sub(r'\s+', ' ', sig_body).strip().rstrip(';').strip()
                # Split off the method name and parameter list
                m2 = re.match(r'^(.*?)\b([A-Za-z_]\w*)\s*\((.*)\)\s*$', sig_body)
                if m2:
                    method_name = m2.group(2)
                    params = m2.group(3)
                    pieces = []
                    for part in params.split(','):
                        part = part.strip()
                        toks = part.split()
                        if len(toks) >= 2:
                            pieces.append(' '.join(toks[:-1]))
                        else:
                            pieces.append(part)
                    sig_key = f'{method_name}({",".join(pieces)})'
                    if sig_key in seen_method:
                        continue
                    seen_method.add(sig_key)
                out.append(line); continue
            # Match class declaration line
            m = re.match(r'^\s*public\s+(?:abstract\s+|final\s+)?(class|enum|interface)\s+([A-Za-z_]\w*)\b', line)
            if m:
                key = m.group(2)
                if key in seen_class:
                    continue
                seen_class.add(key)
            out.append(line)
        return '\n'.join(out)

    # Dedup only for files known to have duplicates (global/* and certain runtime/layer files)
    if '/global/' in filepath or filepath.endswith(('ITensor.java', 'CutlassGemmConfig.java',
                                                     'SamplingConfig.java', 'DebugConfig.java')):
        content = dedupe_declarations(content)

        # Clean up orphaned `public static final int` lines that are left when
        # dedupe removed all their followed field entries. An orphan has no
        # `IDENT = ...` before the next top-level declaration or `}`.
        def clean_orphan_finalint(text):
            lines = text.split('\n')
            out = []
            i = 0
            while i < len(lines):
                line = lines[i]
                if re.match(r'^\s*public\s+static\s+final\s+int\s*$', line):
                    # Look ahead - if no `IDENT = ...[,;]` is found before next
                    # non-comment/non-blank line that is a real decl, drop this.
                    j = i + 1
                    has_field = False
                    while j < len(lines):
                        nxt = lines[j]
                        s = nxt.strip()
                        if not s or s.startswith('//') or s.startswith('*') or s.startswith('/*'):
                            j += 1; continue
                        if re.match(r'^[A-Za-z_]\w*\s*=', s):
                            has_field = True
                        break
                    if not has_field:
                        i += 1
                        continue
                out.append(line)
                i += 1
            return '\n'.join(out)
        content = clean_orphan_finalint(content)

    if content != original:
        with open(filepath, 'w') as f:
            f.write(content)
        fixes_applied += 1
        print(f"  Fixed: {os.path.relpath(filepath, BASE)}")

for root, dirs, files in os.walk(BASE):
    for f in sorted(files):
        if f.endswith('.java') and 'presets' not in root:
            fix_file(os.path.join(root, f))

print(f"\nTotal files fixed: {fixes_applied}")

