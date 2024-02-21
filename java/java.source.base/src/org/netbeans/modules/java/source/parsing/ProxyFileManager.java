/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.source.parsing;

import com.sun.tools.javac.api.ClientCodeWrapper.Trusted;
import com.sun.tools.javac.code.Source;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.NullUnknown;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.util.Iterators;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
@Trusted
public final class ProxyFileManager implements JavaFileManager {

    private static final Logger LOG = Logger.getLogger(ProxyFileManager.class.getName());
    private static final Function<URL,Collection<? extends URL>> ROOT_TO_COLLECTION = (u) -> Collections.singleton(u);

    private static final Location ALL = new Location () {
        @Override
        public String getName() { return "ALL";}   //NOI18N

        @Override
        public boolean isOutputLocation() { return false; }
    };

    /**
     * Workaround to allow Filer ask for getFileForOutput for StandardLocation.SOURCE_PATH
     * which is not allowed but Filer does not allow write anyway => safe to do it.
     */
    private static final Location SOURCE_PATH_WRITE = new Location () {
        @Override
        public String getName() { return "SOURCE_PATH_WRITE"; }  //NOI18N
        @Override
        public boolean isOutputLocation() { return false;}
    };

    private final Configuration cfg;
    private final Object ownerThreadLock = new Object();
    private JavaFileObject lastInfered;
    private String lastInferedResult;
    //@GuardedBy("ownerThreadLock")
    private Thread ownerThread;


    /** Creates a new instance of ProxyFileManager */
    public ProxyFileManager(
            @NonNull Configuration cfg) {
        assert cfg != null;
        this.cfg = cfg;
    }


    @Override
    @NonNull
    public Iterable<JavaFileObject> list(
            @NonNull final Location l,
            @NonNull final String packageName,
            @NonNull final Set<JavaFileObject.Kind> kinds,
            final boolean recurse) throws IOException {
        checkSingleOwnerThread();
        try {
            final JavaFileManager[] fms = cfg.getFileManagers (l, null);
            List<Iterable<JavaFileObject>> iterables = new ArrayList<>(fms.length);
            for (JavaFileManager fm : fms) {
                iterables.add(fm.list(l, packageName, kinds, recurse));
            }
            final Iterable<JavaFileObject> result = Iterators.chained(iterables);
            if (LOG.isLoggable(Level.FINER)) {
                final StringBuilder urls = new StringBuilder ();
                for (JavaFileObject jfo : result ) {
                    urls.append(jfo.toUri().toString());
                    urls.append(", ");  //NOI18N
                }
                LOG.log(
                    Level.FINER,
                    "List {0} Package: {1} Kinds: {2} -> {3}", //NOI18N
                    new Object[] {
                        l,
                        packageName,
                        kinds,
                        urls
                    });
            }
            return result;
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    @CheckForNull
    public FileObject getFileForInput(
            @NonNull final Location l,
            @NonNull final String packageName,
            @NonNull final String relativeName) throws IOException {
        checkSingleOwnerThread();
        try {
            JavaFileManager[] fms = cfg.getFileManagers(l, null);
            for (JavaFileManager fm : fms) {
                FileObject result = fm.getFileForInput(l, packageName, relativeName);
                if (result != null) {
                    return result;
                }
            }
            return null;
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    @CheckForNull
    public FileObject getFileForOutput(
            @NonNull final Location l,
            @NonNull final String packageName,
            @NonNull final String relativeName,
            @NullAllowed final FileObject sibling)
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        checkSingleOwnerThread();
        try {
            JavaFileManager[] fms = cfg.getFileManagers(
                    l == StandardLocation.SOURCE_PATH ?
                        SOURCE_PATH_WRITE : l,
                    null);
            if (fms.length == 0) {
                throw new UnsupportedOperationException("No JavaFileManager for location: " + l);  //NOI18N
            } else {
                return mark(
                        fms[0].getFileForOutput(l, packageName, relativeName, sibling),
                        l);
            }
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    @CheckForNull
    public ClassLoader getClassLoader (@NonNull final Location l) {
        checkSingleOwnerThread();
        try {
            return null;
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    public void flush() throws IOException {
        checkSingleOwnerThread();
        try {
            for (JavaFileManager fm : cfg.getFileManagers(ALL, null)) {
                fm.flush();
            }
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    public void close() throws IOException {
        checkSingleOwnerThread();
        try {
            for (JavaFileManager fm : cfg.getFileManagers(ALL, null)) {
                fm.close();
            }
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    public int isSupportedOption(@NonNull final String string) {
        checkSingleOwnerThread();
        try {
            return -1;
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    public boolean handleOption (
            @NonNull final String current,
            @NonNull final Iterator<String> remains) {
        checkSingleOwnerThread();
        try {
            boolean isSourceElement;
            if (AptSourceFileManager.ORIGIN_FILE.equals(current)) {
                if (!remains.hasNext()) {
                    throw new IllegalArgumentException("The apt-source-root requires folder.");    //NOI18N
                }
                final String sib = remains.next();
                if(sib.length() != 0) {
                    final URL sibling = asURL(sib);
                    final boolean inSourceRoot =
                        cfg.getProcessorGeneratedFiles().findSibling(Collections.singleton(sibling)) != null;
                    cfg.getSiblings().push(sibling, inSourceRoot);
                } else {
                    cfg.getSiblings().pop();
                }
                return true;
            } else if ((isSourceElement=AptSourceFileManager.ORIGIN_SOURCE_ELEMENT_URL.equals(current)) ||
                       AptSourceFileManager.ORIGIN_RESOURCE_ELEMENT_URL.equals(current)) {
                if (remains.hasNext()) {
                    final Collection<? extends URL> urls = asURLs(remains);
                    URL sibling = cfg.getProcessorGeneratedFiles().findSibling(urls);
                    boolean inSourceRoot = true;
                    if (sibling == null) {
                        sibling = cfg.getSiblings().getProvider().getSibling();
                        inSourceRoot = cfg.getSiblings().getProvider().isInSourceRoot();
                    }
                    cfg.getSiblings().push(sibling, inSourceRoot);
                    if (LOG.isLoggable(Level.INFO) && isSourceElement && urls.size() > 1) {
                        final StringBuilder sb = new StringBuilder();
                        for (URL url : urls) {
                            if (sb.length() > 0) {
                                sb.append(", ");    //NOI18N
                            }
                            sb.append(url);
                        }
                        LOG.log(
                            Level.FINE,
                            "Multiple source files passed as ORIGIN_SOURCE_ELEMENT_URL: {0}; using: {1}",  //NOI18N
                            new Object[]{
                                sb,
                                cfg.getSiblings().getProvider().getSibling()
                            });
                    }
                } else {
                   cfg.getSiblings().pop();
                }
                return true;
            }
            final RepeatableIterator<String> it = RepeatableIterator.create(remains);
            boolean res = false;
            for (JavaFileManager m : cfg.getFileManagers(ALL, current)) {
                res |= m.handleOption(current, it);
                it.reset();
            }
            return res;
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    public boolean hasLocation(@NonNull final Location location) {
        
        checkSingleOwnerThread();
        try {
            return cfg.hasLocations(location);
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    @CheckForNull
    public Location getLocationForModule(Location location, String moduleName) throws IOException {
        checkSingleOwnerThread();
        try {
            for (JavaFileManager jfm : cfg.getFileManagers(location, null)) {
                final Location res = jfm.getLocationForModule(location, moduleName);
                if (res != null) {
                    return res;
                }
            }
            return  null;
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    @CheckForNull
    public Location getLocationForModule(Location location, JavaFileObject fo) throws IOException {
        checkSingleOwnerThread();
        try {
            for (JavaFileManager jfm : cfg.getFileManagers(location, null)) {
                final Location res = jfm.getLocationForModule(location, fo);
                if (res != null) {
                    return res;
                }
             }
            return null;
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    @CheckForNull
    public String inferModuleName(@NonNull final Location location) throws IOException {
        checkSingleOwnerThread();
        try {
            for (JavaFileManager jfm : cfg.getFileManagers(location, null)) {
                final String modName = jfm.inferModuleName(location);
                if (modName != null) {
                    return modName;
                }
            }
            return null;
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    @NonNull
    public Iterable<Set<Location>> listLocationsForModules(@NonNull final Location location) throws IOException {
        checkSingleOwnerThread();
        try {
            final JavaFileManager[] jfms = cfg.getFileManagers(location, null);
            switch(jfms.length) {
                case 0:
                    return Collections.<Set<Location>>emptySet();
                case 1:
                    return jfms[0].listLocationsForModules(location);
                default:
                    final List<Set<Location>> res = new ArrayList<>();
                    for (JavaFileManager jfm : jfms) {
                        jfm.listLocationsForModules(location).forEach(res::add);
                    }
                    return res;
            }
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    @CheckForNull
    public JavaFileObject getJavaFileForInput (
            @NonNull final Location l,
            @NonNull final String className,
            @NonNull final JavaFileObject.Kind kind) throws IOException {
        checkSingleOwnerThread();
        try {
            JavaFileManager[] fms = cfg.getFileManagers (l, null);
            for (JavaFileManager fm : fms) {
                JavaFileObject result = fm.getJavaFileForInput(l,className,kind);
                if (result != null) {
                    return result;
                }
            }
            return null;
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    @CheckForNull
    public JavaFileObject getJavaFileForOutput(
            @NonNull final Location l,
            @NonNull final String className,
            @NonNull final JavaFileObject.Kind kind,
            @NonNull final FileObject sibling)
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        checkSingleOwnerThread();
        try {
            final JavaFileManager[] fms = cfg.getFileManagers (l, null);
            if (fms.length == 0) {
                throw new UnsupportedOperationException("No JavaFileManager for location: " + l);  //NOI18N
            } else {
                return mark (
                        fms[0].getJavaFileForOutput (l, className, kind, sibling),
                        l);
            }
        } finally {
            clearOwnerThread();
        }
    }


    @Override
    @CheckForNull
    public String inferBinaryName(
            @NonNull final JavaFileManager.Location location,
            @NonNull final JavaFileObject javaFileObject) {
        checkSingleOwnerThread();
        try {
            assert javaFileObject != null;
            //If cached return it dirrectly
            if (javaFileObject == lastInfered) {
                return lastInferedResult;
            }
            String result;
            //If instanceof FileObject.Base no need to delegate it
            if (javaFileObject instanceof InferableJavaFileObject) {
                final InferableJavaFileObject ifo = (InferableJavaFileObject) javaFileObject;
                result = ifo.inferBinaryName();
                if (result != null) {
                    this.lastInfered = javaFileObject;
                    this.lastInferedResult = result;
                    return result;
                }
            }
            //Ask delegates to infer the binary name
            JavaFileManager[] fms = cfg.getFileManagers (location, null);
            for (JavaFileManager fm : fms) {
                result = fm.inferBinaryName (location, javaFileObject);
                if (result != null && result.length() > 0) {
                    this.lastInfered = javaFileObject;
                    this.lastInferedResult = result;
                    return result;
                }
            }
            return null;
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    public boolean isSameFile(FileObject fileObject, FileObject fileObject0) {
        checkSingleOwnerThread();
        try {
            final JavaFileManager[] fms = cfg.getFileManagers(ALL, null);
            for (JavaFileManager fm : fms) {
                if (fm.isSameFile(fileObject, fileObject0)) {
                    return true;
                }
            }
            return fileObject.toUri().equals (fileObject0.toUri());
        } finally {
            clearOwnerThread();
        }
    }

    @SuppressWarnings("unchecked")
    @NonNull
    private <T extends javax.tools.FileObject> T mark(
            @NonNull final T result,
            @NonNull JavaFileManager.Location l) throws MalformedURLException {
        if (ModuleLocation.isInstance(l)) {
            l = ModuleLocation.cast(l).getBaseLocation();
        }
        boolean valid = true;
        ProcessorGenerated.Type type = null;
        if (l == StandardLocation.CLASS_OUTPUT) {
            type = ProcessorGenerated.Type.RESOURCE;
        } else if (l == StandardLocation.SOURCE_OUTPUT) {
            type = ProcessorGenerated.Type.SOURCE;
        }
        if (cfg.getSiblings().getProvider().hasSibling() &&
            cfg.getSiblings().getProvider().isInSourceRoot()) {
            if (type == ProcessorGenerated.Type.SOURCE) {
                cfg.getProcessorGeneratedFiles().register(
                    cfg.getSiblings().getProvider().getSibling(),
                    result,
                    type);
            } else if (type == ProcessorGenerated.Type.RESOURCE) {
                try {
                    result.openInputStream().close();
                } catch (IOException ioe) {
                    //Marking only created files
                    cfg.getProcessorGeneratedFiles().register(
                        cfg.getSiblings().getProvider().getSibling(),
                        result,
                        type);
                }
            }
            if (!FileObjects.isValidFileName(result)) {
                LOG.log(
                    Level.WARNING,
                    "Cannot write Annotation Processor generated file: {0} ({1})",   //NOI18N
                    new Object[] {
                        result.getName(),
                        result.toUri()
                    });
                valid = false;
            }
        }
        return valid && (cfg.getProcessorGeneratedFiles().canWrite() || !cfg.getSiblings().getProvider().hasSibling()) ?
                result :
                (T) FileObjects.nullWriteFileObject((InferableJavaFileObject)result);    //safe - NullFileObject subclass of both JFO and FO.
    }

    private void checkSingleOwnerThread() {
        final Thread currentThread = Thread.currentThread();
        synchronized (ownerThreadLock) {
            if (ownerThread == null) {
                ownerThread = currentThread;
            } else if (ownerThread != currentThread) {
                //Dump both stacks and throw ISE.
                throw new ConcurrentModificationException(
                    String.format(
                        "Current owner: %s, New Owner: %s", //NOI18N
                        Arrays.asList(ownerThread.getStackTrace()),
                        Arrays.asList(currentThread.getStackTrace())));
            }
        }
    }

    private void clearOwnerThread() {
        synchronized (ownerThreadLock) {
            ownerThread = null;
        }
    }

    private static URL asURL(final String url) throws IllegalArgumentException {
        try {
            return new URL(url);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Invalid path argument: " + url, ex);    //NOI18N
        }
    }

    private static Collection<? extends URL> asURLs(Iterator<? extends String> surls) {
        final ArrayDeque<URL> result = new ArrayDeque<>();
        while (surls.hasNext()) {
            final String surl = surls.next();
            if (FileObjects.JAVA.equals(FileObjects.getExtension(surl))) {
                result.add(asURL(surl));
            }
        }
        return result;
    }

    /*test*/
    //@NotThreadSafe
    static final class RepeatableIterator<T> implements Iterator<T> {
        private final Iterator<T> base;
        private final List<T> seen = new ArrayList<>();
        private Iterator<T> current;
        
        private RepeatableIterator(@NonNull final Iterator<T> base) {
            Parameters.notNull("base", base);   //NOI18N
            this.base = base;
            reset();
        }

        @Override
        public boolean hasNext() {
            boolean res = current.hasNext();
            if (!res && current != base) {
                current = base;
                res  = current.hasNext();
            }
            return res;
        }

        @Override
        @NullUnknown
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            final T res = current.next();
            if (current == base) {
                seen.add(res);
            }
            return res;
        }
        
        void reset() {
            current = seen.iterator();
        }
        
        @NonNull
        static <T> RepeatableIterator<T> create(@NonNull final Iterator<T> base) {
            return new RepeatableIterator<>(base);
        }
    }


    public static final class Configuration {
        private static final int BOOT = 0;
        private static final int COMPILE = BOOT + 1;
        private static final int OUTPUT = COMPILE + 1;
        private static final int TREE_LOADER = OUTPUT + 1;
        private static final int SRC = TREE_LOADER + 1;
        private static final int APT_SRC = SRC + 1;
        private static final int MEM = APT_SRC + 1;
        private static final int SYS_MODULES = MEM + 1;
        private static final int USER_MODULES = SYS_MODULES + 1;
        private static final int SRC_MODULES = USER_MODULES + 1;
        private static final int MODULE_PATCHES = SRC_MODULES + 1;

        private static final JavaFileManager[] EMPTY = new JavaFileManager[0];

        private final CachingArchiveProvider cap;
        private final ClassPath moduleBoot;
        private final ClassPath moduleCompile;
        private final ClassPath bootCached;
        private final ClassPath compiledCached;
        private final ClassPath src;
        private final ClassPath srcCached;
        private final ClassPath moduleSrcCached;
        private final ClassPath outputCached;
        private final ClassPath aptSrcCached;
        private final Map<Location,Entry> fileManagers;
        private final JavaFileManager[] emitted;

        private final SiblingSource siblings;
        private final FileManagerTransaction fmTx;
        private final ProcessorGenerated processorGeneratedFiles;
        private final Map<ClassPath,Function<URL,Collection<? extends URL>>> peersMap;

        private boolean useModifiedFiles = true;
        private JavaFileFilterImplementation filter;
        private boolean ignoreExcludes;
        private Function<JavaFileManager.Location, JavaFileManager> jfmProvider;
        private Source sourceLevel;

        private Configuration(
                @NonNull final ClassPath moduleBoot,
                @NonNull final ClassPath moduleCompile,
                @NonNull final ClassPath bootCached,
                @NonNull final ClassPath compiledCached,
                @NonNull final ClassPath src,
                @NonNull final ClassPath srcCached,
                @NonNull final ClassPath moduleSrcCached,
                @NonNull final ClassPath outputCached,
                @NonNull final ClassPath aptSrcCached,
                @NonNull final SiblingSource siblings,
                @NonNull final FileManagerTransaction fmTx,
                @NonNull final ProcessorGenerated processorGeneratedFiles) {
            assert moduleBoot != null;
            assert moduleCompile != null;
            assert bootCached != null;
            assert compiledCached != null;
            assert src != null;
            assert srcCached != null;
            assert outputCached != null;
            assert aptSrcCached != null;
            assert siblings != null;
            assert fmTx != null;
            assert processorGeneratedFiles != null;
            this.cap = CachingArchiveProvider.getDefault();
            this.moduleBoot = moduleBoot;
            this.moduleCompile = moduleCompile;
            this.bootCached = bootCached;
            this.compiledCached = compiledCached;
            this.src = src;
            this.srcCached = srcCached;
            this.moduleSrcCached = moduleSrcCached;
            this.outputCached = outputCached;
            this.aptSrcCached = aptSrcCached;
            this.siblings = siblings;
            this.fmTx = fmTx;
            this.processorGeneratedFiles = processorGeneratedFiles;
            this.fileManagers = createFactories();
            this.emitted = new JavaFileManager[MODULE_PATCHES+1];
            this.peersMap = new IdentityHashMap<>();
        }

        public void setUseModifiedFiles(final boolean useModifiedFiles) {
            this.useModifiedFiles = useModifiedFiles;
        }

        public boolean isUseModifiedFiles() {
            return this.useModifiedFiles;
        }

        public void setFilter(@NullAllowed final JavaFileFilterImplementation filter) {
            this.filter = filter;
        }

        @CheckForNull
        public JavaFileFilterImplementation getFilter() {
            return this.filter;
        }

        public void setIgnoreExcludes(final boolean ignoreExcludes) {
            this.ignoreExcludes = ignoreExcludes;
        }

        public boolean isIgnoreExcludes() {
            return this.ignoreExcludes;
        }
        
        public void setPeers(
                @NonNull final ClassPath cachedPath,
                @NonNull final Function<URL,Collection<? extends URL>> provider) {
            Parameters.notNull("cachedPath", cachedPath);   //NOI18N
            Parameters.notNull("provider", provider);       //NOI18N
            peersMap.put(cachedPath, provider);
        }
        
        public void setCustomFileManagerProvider(@NullAllowed final Function<JavaFileManager.Location, JavaFileManager> jfmProvider) {
            this.jfmProvider = jfmProvider;
        }
        
        public void setSourceLevel(@NullAllowed final String sourceLevel) {
            this.sourceLevel = sourceLevel == null ?
                    null :
                    Source.lookup(sourceLevel);
        }

        @NonNull
        JavaFileManager[] getFileManagers(@NonNull Location location, @NullAllowed String hint) {
            if (ModuleLocation.isInstance(location)) {
                location = ModuleLocation.cast(location).getBaseLocation();
            }
            if (location == ALL) {
                //Todo: create factories with options when there are more than one option.
                if (TreeLoaderOutputFileManager.OUTPUT_ROOT.equals(hint)) {
                    createTreeLoaderFileManager();
                }
                if (JavacParser.OPTION_PATCH_MODULE.equals(hint) || (hint != null && hint.startsWith(JavacParser.NB_X_MODULE))) {
                    createPatchFileManager();
                    createModuleSrcFileManager();
                }
                final List<JavaFileManager> res = new ArrayList<>(emitted.length);
                for (JavaFileManager jfm : emitted) {
                    if (jfm != null) {
                        res.add(jfm);
                    }
                }
                return res.toArray(new JavaFileManager[0]);
            } else {
                final Entry result = fileManagers.get(location);
                return result == null ?
                        EMPTY :
                        result.get();
            }
        }

        boolean hasLocations(@NonNull Location l) {
            if (ModuleLocation.isInstance(l)) {
                l = ModuleLocation.cast(l).getBaseLocation();
            }
            final Entry e = fileManagers.get(l);
            return e != null ?
                    e.hasLocation() :
                    false;
        }

        @NonNull
        SiblingSource getSiblings() {
            return siblings;
        }

        @NonNull
        ProcessorGenerated getProcessorGeneratedFiles() {
            return processorGeneratedFiles;
        }

        @NonNull
        private Map<Location,Entry> createFactories() {
            final Map<Location,Entry> m = new HashMap<>();
            m.put(StandardLocation.PLATFORM_CLASS_PATH, new Entry(() -> new JavaFileManager[] {createBootFileManager()}));
            m.put(StandardLocation.CLASS_PATH, new Entry(() -> {
                    final JavaFileManager compile = createCompileFileManager();
                    final JavaFileManager output = createModuleSrcFileManager() == null ?
                            createOutputFileManager() :
                            null;
                    return output == null ?
                        new JavaFileManager[] {compile}:
                        new JavaFileManager[] {output, compile};
            }));
            m.put(StandardLocation.SOURCE_PATH, new Entry(() -> {
                    final JavaFileManager src = createSrcFileManager();
                    final JavaFileManager mem = createMemFileManager();
                    return src == null ?
                        EMPTY :
                        mem == null ?
                            new JavaFileManager[] {src}:
                        new JavaFileManager[] {
                            src,
                            mem};
            }));
            m.put(StandardLocation.CLASS_OUTPUT,new Entry(
                    () -> {
                        final JavaFileManager output = createOutputFileManager();
                        final JavaFileManager treeLoader = createTreeLoaderFileManager();
                        final JavaFileManager patches = createPatchFileManager();
                        return output == null ?
                            new JavaFileManager[] {treeLoader, patches} :
                            new JavaFileManager[] {treeLoader, output, patches};
                    },
                    (fms) -> {
                        final BitSet active = new BitSet(fms.length);
                        int bc = 0;
                        for (int i = 0; i < fms.length; i++) {
                            if (fms[i].hasLocation(StandardLocation.CLASS_OUTPUT)) {
                                active.set(i);
                                bc++;
                            }
                        }
                        if (bc == 0) {
                            return EMPTY;
                        } else {
                            final JavaFileManager[] res = new JavaFileManager[bc];
                            for(int i = active.nextSetBit(0), j = 0; i >= 0; i = active.nextSetBit(i+1)) {
                                res[j++] = fms[i];
                            }
                            return res;
                        }
                    }));
            m.put(StandardLocation.SOURCE_OUTPUT, new Entry(() -> {
                    final JavaFileManager aptSrcOut = createAptSrcOutputFileManager();
                    return aptSrcOut == null ?
                        EMPTY:
                        new JavaFileManager[] {aptSrcOut};
            }));
            m.put(SOURCE_PATH_WRITE, new Entry(() -> {
                    final JavaFileManager src = createSrcFileManager();
                    return src == null ?
                        EMPTY:
                        new JavaFileManager[] {src};
            }));
            m.put(StandardLocation.SYSTEM_MODULES, new Entry(() -> new JavaFileManager[] {createSystemModuleFileManager()}));
            m.put(StandardLocation.MODULE_PATH, new Entry(() -> new JavaFileManager[] {createModuleFileManager()}));
            m.put(StandardLocation.MODULE_SOURCE_PATH, new Entry(() -> {
                    final JavaFileManager moduleSrc = createModuleSrcFileManager();
                    return moduleSrc == null ?
                        EMPTY:
                        new JavaFileManager[] {moduleSrc};
            }));
            m.put(StandardLocation.PATCH_MODULE_PATH, new Entry(() -> new JavaFileManager[] {createPatchFileManager()}));
            return m;
        }

        @NonNull
        private JavaFileManager createBootFileManager() {
            if (emitted[BOOT] == null) {
                emitted[BOOT] = new CachingFileManager (cap, bootCached, sourceLevel, true, true);
            }
            return emitted[BOOT];
        }

        @NonNull
        private JavaFileManager createCompileFileManager() {
            if (emitted[COMPILE] == null) {
                emitted[COMPILE] = new CachingFileManager (cap, compiledCached, sourceLevel, false, true);
            }
            return emitted[COMPILE];
        }

        @CheckForNull
        private JavaFileManager createSrcFileManager() {
            if (emitted[SRC] == null) {
                final boolean srcNonEmpty = !this.srcCached.entries().isEmpty();
                final boolean hasModules =  srcNonEmpty && this.moduleSrcCached != ClassPath.EMPTY;
                final boolean hasSources = !hasModules && srcNonEmpty;
                emitted[SRC] = hasSources ?
                        (!useModifiedFiles ?
                                new CachingFileManager (cap, srcCached, filter, null, false, ignoreExcludes) :
                                new SourceFileManager (srcCached, ignoreExcludes)) :
                        null;
            }
            return emitted[SRC];
        }

        @CheckForNull
        private JavaFileManager createOutputFileManager() {
            if (emitted[OUTPUT] == null) {
                final boolean hasSources = !this.srcCached.entries().isEmpty();
                final JavaFileManager outFm;
                if (hasSources) {
                    JavaFileManager tmp;
                    if (jfmProvider != null && (tmp = jfmProvider.apply(StandardLocation.CLASS_OUTPUT)) != null) {
                        outFm = tmp;
                    } else {
                        outFm = new OutputFileManager(
                            cap,
                            outputCached,
                            srcCached,
                            this.aptSrcCached,
                            siblings.getProvider(),
                            fmTx,
                            createModuleSrcFileManager());
                    }
                } else {
                    outFm = null;
                }
                emitted[OUTPUT] = outFm;
            }
            return emitted[OUTPUT];
        }

        @NonNull
        private JavaFileManager createTreeLoaderFileManager() {
            if (emitted[TREE_LOADER] == null) {
                emitted[TREE_LOADER] = new TreeLoaderOutputFileManager(cap, fmTx);
            }
            return emitted[TREE_LOADER];
        }

        @CheckForNull
        private JavaFileManager createAptSrcOutputFileManager() {
            if (emitted[APT_SRC] == null) {
            final boolean hasAptSources = this.aptSrcCached != ClassPath.EMPTY;
            emitted[APT_SRC] = hasAptSources ?
                    new AptSourceFileManager(
                            srcCached,
                            aptSrcCached,
                            siblings.getProvider(),
                            fmTx,
                            createModuleSrcFileManager()) :
                    null;
            }
            return emitted[APT_SRC];
        }

        @CheckForNull
        private JavaFileManager createMemFileManager() {
            return emitted[MEM];
        }

        @NonNull
        private JavaFileManager createSystemModuleFileManager() {
            if (emitted[SYS_MODULES] == null) {
                emitted[SYS_MODULES] = new ModuleFileManager(
                    cap,
                    moduleBoot,
                    peersMap.getOrDefault(moduleBoot, ROOT_TO_COLLECTION),
                    sourceLevel,
                    StandardLocation.SYSTEM_MODULES,
                    true);
            }
            return emitted[SYS_MODULES];
        }

        @NonNull
        private JavaFileManager createModuleFileManager() {
            if (emitted[USER_MODULES] == null) {
                emitted[USER_MODULES] = new ModuleFileManager(
                    cap,
                    moduleCompile,
                    peersMap.getOrDefault(moduleCompile, ROOT_TO_COLLECTION),
                    sourceLevel,
                    StandardLocation.MODULE_PATH,
                    false);
            }
            return emitted[USER_MODULES];
        }

        @CheckForNull
        private ModuleSourceFileManager createModuleSrcFileManager() {
            if (emitted[SRC_MODULES] == null) {
                final boolean hasModules = !this.srcCached.entries().isEmpty() && this.moduleSrcCached != ClassPath.EMPTY;
                emitted[SRC_MODULES] = hasModules ?
                        new ModuleSourceFileManager(
                                srcCached,
                                moduleSrcCached,
                                ignoreExcludes) :
                        null;
            }
            return (ModuleSourceFileManager) emitted[SRC_MODULES];
        }

        @CheckForNull
        private JavaFileManager createPatchFileManager() {
            if (emitted[MODULE_PATCHES] == null) {
                emitted[MODULE_PATCHES] = new PatchModuleFileManager(
                        new ModuleFileManager(cap, ClassPath.EMPTY, ROOT_TO_COLLECTION, sourceLevel, StandardLocation.MODULE_PATH, false),
                        new ModuleSourceFileManager(ClassPath.EMPTY, ClassPath.EMPTY, ignoreExcludes),
                        this.src
                );
            }
            return emitted[MODULE_PATCHES];
        }

        @NonNull
        public static Configuration create (
                @NonNull final ClassPath moduleBoot,
                @NonNull final ClassPath moduleCompile,
                @NonNull final ClassPath bootCached,
                @NonNull final ClassPath compiledCached,
                @NonNull final ClassPath src,
                @NonNull final ClassPath srcCached,
                @NonNull final ClassPath moduleSrcCached,
                @NonNull final ClassPath outputCached,
                @NonNull final ClassPath aptSrcCached,
                @NonNull final SiblingSource siblings,
                @NonNull final FileManagerTransaction fmTx,
                @NonNull final ProcessorGenerated processorGeneratedFiles) {
            return new Configuration(
                moduleBoot,
                moduleCompile,
                bootCached,
                compiledCached,
                src,
                srcCached,
                moduleSrcCached,
                outputCached,
                aptSrcCached,
                siblings,
                fmTx,
                processorGeneratedFiles);
        }

        private static final class Entry {

            private JavaFileManager[] fileManagers;
            private Supplier<JavaFileManager[]> factory;
            private final Function<JavaFileManager[],JavaFileManager[]> filter;

            private Entry(@NonNull final Supplier<JavaFileManager[]> factory) {
                this(factory, null);
            }

            private Entry(
                    @NonNull final Supplier<JavaFileManager[]> factory,
                    @NullAllowed final Function<JavaFileManager[],JavaFileManager[]> filter) {
                assert factory != null;
                this.factory = factory;
                this.filter = filter;
            }

            boolean hasLocation() {
                return get().length > 0;
            }

            @NonNull
            JavaFileManager[] get() {
                JavaFileManager[] res;
                 if (fileManagers != null) {
                    res = fileManagers;
                } else {
                    fileManagers = factory.get();
                    assert fileManagers != null;
                    factory = null;
                    res = fileManagers;
                }
                if (filter != null) {
                    res = filter.apply(res);
                }
                return res;
            }
        }
    }
    public boolean contains(Location l, FileObject f) {
        return true;
    }
}
