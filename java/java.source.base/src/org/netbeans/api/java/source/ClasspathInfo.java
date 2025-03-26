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
package org.netbeans.api.java.source;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.NullUnknown;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.java.source.classpath.CacheClassPath;
import org.netbeans.modules.java.source.parsing.ProxyFileManager;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.classpath.AptSourcePath;
import org.netbeans.modules.java.source.classpath.SourcePath;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.indexing.TransactionContext;
import org.netbeans.modules.java.source.parsing.*;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.impl.indexing.PathRegistry;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.ChangeSupport;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/** Class which contains info about classpath
 *
 * @author Tomas Zezula, Petr Hrebejk
 */
public final class ClasspathInfo {

    private static final Logger log = Logger.getLogger(ClasspathInfo.class.getName());

    static {
        ClasspathInfoAccessor.setINSTANCE(new ClasspathInfoAccessorImpl());
        try {
            Class.forName(ClassIndex.class.getName(), true, CompilationInfo.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            if (log.isLoggable(Level.SEVERE))
                log.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private final ClassPath srcClassPath;
    private final ClassPath moduleSrcPath;
    private final ClassPath bootClassPath;
    private final ClassPath moduleBootPath;
    private final ClassPath compileClassPath;
    private final ClassPath moduleCompilePath;
    private final ClassPath moduleClassPath;
    private final ClassPath cachedAptSrcClassPath;
    private final ClassPath cachedSrcClassPath;
    private final ClassPath cachedBootClassPath;
    private final ClassPath cachedCompileClassPath;
    private final ClassPath cachedModuleCompilePath;
    private final ClassPath cachedModuleClassPath;
    private final ClassPath outputClassPath;

    private final ClassPathListener cpListener;
    private final boolean useModifiedFiles;
    private final boolean ignoreExcludes;
    private final JavaFileFilterImplementation filter;
    private final MemoryFileManager memoryFileManager;
    private final ChangeSupport listenerList;
    private final FileManagerTransaction fmTx;
    private final ProcessorGenerated pgTx;
    private final Map<ClassPath,Function<URL,Collection<? extends URL>>> peerProviders;
    private final Function<JavaFileManager.Location, JavaFileManager> jfmProvider;

    //@GuardedBy("this")
    private ClassIndex usagesQuery;

    /** Creates a new instance of ClasspathInfo (private use the factory methods) */
    private ClasspathInfo(final @NonNull ClassPath bootCp,
                          final @NonNull ClassPath moduleBootP,
                          final @NonNull ClassPath compileCp,
                          final @NonNull ClassPath moduleCompileP,
                          final @NonNull ClassPath moduleClassP,
                          final @NullAllowed ClassPath srcCp,
                          final @NullAllowed ClassPath moduleSrcCp,
                          final @NullAllowed JavaFileFilterImplementation filter,
                          final boolean backgroundCompilation,
                          final boolean ignoreExcludes,
                          final boolean hasMemoryFileManager,
                          final boolean useModifiedFiles,
                          final boolean requiresSourceRoots,
                          @NullAllowed final Function<JavaFileManager.Location, JavaFileManager> jfmProvider) {
        assert bootCp != null;
        assert compileCp != null;
        this.cpListener = new ClassPathListener ();
        this.bootClassPath = bootCp;
        this.moduleBootPath = moduleBootP;
        this.compileClassPath = compileCp;
        this.moduleCompilePath = moduleCompileP;
        this.moduleClassPath = moduleClassP;
        this.listenerList = new ChangeSupport(this);
        this.cachedBootClassPath = CacheClassPath.forBootPath(this.bootClassPath,backgroundCompilation);
        this.cachedCompileClassPath = CacheClassPath.forClassPath(this.compileClassPath,backgroundCompilation);
        this.cachedModuleCompilePath = CacheClassPath.forClassPath(this.moduleCompilePath,backgroundCompilation);
        this.cachedModuleClassPath = CacheClassPath.forClassPath(this.moduleClassPath,backgroundCompilation);
        if (!backgroundCompilation) {
            this.cachedBootClassPath.addPropertyChangeListener(WeakListeners.propertyChange(this.cpListener,this.cachedBootClassPath));
            this.cachedCompileClassPath.addPropertyChangeListener(WeakListeners.propertyChange(this.cpListener,this.cachedCompileClassPath));
        }
        if (srcCp == null || srcCp == ClassPath.EMPTY) {
            this.cachedSrcClassPath = this.srcClassPath = this.outputClassPath = this.cachedAptSrcClassPath = ClassPath.EMPTY;
        } else {
            this.srcClassPath = srcCp;
            final ClassPathImplementation noApt = AptSourcePath.sources(srcCp);
            this.cachedSrcClassPath = ClassPathFactory.createClassPath(SourcePath.filtered(noApt, backgroundCompilation));
            this.cachedAptSrcClassPath = ClassPathFactory.createClassPath(
                    SourcePath.filtered(AptSourcePath.aptCache(srcCp), backgroundCompilation));
            this.outputClassPath = CacheClassPath.forSourcePath (ClassPathFactory.createClassPath(noApt),backgroundCompilation);
            if (!backgroundCompilation) {
                this.cachedSrcClassPath.addPropertyChangeListener(WeakListeners.propertyChange(this.cpListener,this.cachedSrcClassPath));
            }
        }
        if (requiresSourceRoots && this.cachedSrcClassPath.entries().isEmpty()) {
            throw new OutputFileManager.InvalidSourcePath();
        }
        if (moduleSrcCp == null) {
            this.moduleSrcPath = ClassPath.EMPTY;
        } else {
            this.moduleSrcPath = moduleSrcCp;
        }
        this.ignoreExcludes = ignoreExcludes;
        this.useModifiedFiles = useModifiedFiles;
        this.filter = filter;
        if (hasMemoryFileManager) {
            if (srcCp == null) {
                throw new IllegalStateException ();
            }
            this.memoryFileManager = new MemoryFileManager();
        } else {
            this.memoryFileManager = null;
        }
        if (backgroundCompilation) {
            final TransactionContext txCtx = TransactionContext.get();
            fmTx = txCtx.get(FileManagerTransaction.class);
            pgTx = txCtx.get(ProcessorGenerated.class);
        } else {
            //No real transaction, read-only mode.
            fmTx = FileManagerTransaction.treeLoaderOnly();
            pgTx = ProcessorGenerated.nullWrite();
        }
        this.peerProviders = new IdentityHashMap<>();
        this.peerProviders.put(cachedModuleCompilePath, new Peers(this.moduleCompilePath));
        assert fmTx != null : "No file manager transaction.";   //NOI18N
        assert pgTx != null : "No processor generated transaction.";   //NOI18N
        this.jfmProvider = jfmProvider;
    }

    @Override
    public String toString() {
        return String.format(
            "ClasspathInfo [boot: %s, module boot: %s, compile: %s, module compile: %s, module class: %s, src: %s, internal boot: %s, internal compile: %s, internal module class: %s, internal src: %s, internal out: %s]", //NOI18N
                bootClassPath,
                moduleBootPath,
                compileClassPath,
                moduleCompilePath,
                moduleClassPath,
                srcClassPath,
                cachedBootClassPath,
                cachedCompileClassPath,
                cachedModuleClassPath,
                cachedSrcClassPath,
                outputClassPath);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(toURIs(this.srcClassPath));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ClasspathInfo)) {
            return false;
        }
        final ClasspathInfo other = (ClasspathInfo) obj;
        return Arrays.equals(toURIs(this.srcClassPath), toURIs(other.srcClassPath)) &&
            Arrays.equals(toURIs(this.compileClassPath), toURIs(other.compileClassPath)) &&
            Arrays.equals(toURIs(this.bootClassPath), toURIs(other.bootClassPath)) &&
            Arrays.equals(toURIs(this.moduleBootPath), toURIs(other.moduleBootPath)) &&
            Arrays.equals(toURIs(this.moduleCompilePath), toURIs(other.moduleCompilePath)) &&
            Arrays.equals(toURIs(this.moduleClassPath), toURIs(other.moduleClassPath));
    }
    // Factory methods ---------------------------------------------------------


    /** Creates new interface to the compiler
     * @param file for which the CompilerInterface should be created
     * @return ClasspathInfo or null if the file does not exist on the
     * local file system or it has no classpath associated
     */
    @NullUnknown
    public static ClasspathInfo create (@NonNull final File file) {
        if (file == null) {
            throw new IllegalArgumentException ("Cannot pass null as parameter of ClasspathInfo.create(java.io.File)");     //NOI18N
        }
        final FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) {
            return null;
        }
        else {
            return create (fo);
        }
    }

    /**
     * Creates a new instance of the ClasspathInfo for given Document.
     * <div class="nonnormative">
     * <p>
     * It uses the {@link Document#StreamDescriptionProperty} to obtain the
     * <a href="@org-openide-loaders@/org/openide/loaders/DataObject.html">DataObject</a> for the {@link Document} and creates a {@link ClasspathInfo}
     * for the primary file of the <a href="@org-openide-loaders@/org/openide/loaders/DataObject.html">DataObject</a>
     * </p>
     * </div>
     * @param doc a document for which the {@link ClasspathInfo} should be created
     * @return a {@link ClasspathInfo} or null when the document source cannot be
     * found.
     * @since 0.42
     */
    @NullUnknown
    public static ClasspathInfo create(@NonNull final Document doc) {
        Parameters.notNull("doc", doc);
        FileObject fileObject = Utilities.getFileObject(doc);
        if (fileObject != null) {
            return create(fileObject);
        }
        return null;
    }

    /** Creates new interface to the compiler
     * @param fo for which the CompilerInterface should be created
     */
    @NonNull
    public static ClasspathInfo create(@NonNull final FileObject fo) {
        return create (fo, null, false, false, false, true);
    }

    @NonNull
    public static ClasspathInfo create(
            @NonNull final ClassPath bootPath,
            @NonNull final ClassPath classPath,
            @NullAllowed final ClassPath sourcePath) {
        Parameters.notNull("bootPath", bootPath);       //NOI18N
        Parameters.notNull("classPath", classPath);     //NOI18N
        return new Builder(bootPath)
                .setClassPath(classPath)
                .setSourcePath(sourcePath)
                .build();
    }

    /**
     * Builder for {@link ClasspathInfo}.
     * @since 2.23
     */
    public static final class Builder {
        private final ClassPath bootPath;
        private ClassPath moduleBootPath = ClassPath.EMPTY;
        private ClassPath classPath = ClassPath.EMPTY;
        private ClassPath moduleCompilePath = ClassPath.EMPTY;
        private ClassPath moduleClassPath = ClassPath.EMPTY;
        private ClassPath sourcePath = ClassPath.EMPTY;
        private ClassPath moduleSourcePath = ClassPath.EMPTY;

        public Builder(@NonNull final ClassPath bootPath) {
            Parameters.notNull("bootPath", bootPath);   //NOI18N
            this.bootPath = bootPath;
        }

        @NonNull
        public Builder setModuleBootPath(@NullAllowed ClassPath moduleBootPath) {
            if (moduleBootPath == null) {
                moduleBootPath = ClassPath.EMPTY;
            }
            this.moduleBootPath = moduleBootPath;
            return this;
        }

        @NonNull
        public Builder setClassPath(@NullAllowed ClassPath classPath) {
            if (classPath == null) {
                classPath = ClassPath.EMPTY;
            }
            this.classPath = classPath;
            return this;
        }

        @NonNull
        public Builder setModuleCompilePath(@NullAllowed ClassPath moduleCompilePath) {
            if (moduleCompilePath == null) {
                moduleCompilePath = ClassPath.EMPTY;
            }
            this.moduleCompilePath = moduleCompilePath;
            return this;
        }

        @NonNull
        public Builder setModuleClassPath(@NullAllowed ClassPath moduleClassPath) {
            if (moduleClassPath == null) {
                moduleClassPath = ClassPath.EMPTY;
            }
            this.moduleClassPath = moduleClassPath;
            return this;
        }

        @NonNull
        public Builder setSourcePath(@NullAllowed ClassPath sourcePath) {
            if (sourcePath == null) {
                sourcePath = ClassPath.EMPTY;
            }
            this.sourcePath = sourcePath;
            return this;
        }

        @NonNull
        public Builder setModuleSourcePath(@NullAllowed ClassPath moduleSourcePath) {
            if (moduleSourcePath == null) {
                moduleSourcePath = ClassPath.EMPTY;
            }
            this.moduleSourcePath = moduleSourcePath;
            return this;
        }

        /**
         * Creates a new {@link ClasspathInfo}.
         * @return the {@link ClasspathInfo}
         */
        @NonNull
        public ClasspathInfo build() {
            return create (
                    bootPath,
                    moduleBootPath,
                    classPath,
                    moduleCompilePath,
                    moduleClassPath,
                    sourcePath,
                    moduleSourcePath,
                    null,
                    false,
                    false,
                    false,
                    true,
                    false,
                    null);
        }
    }

    @NonNull
    private static ClasspathInfo create (
            @NonNull final FileObject fo,
            @NullAllowed final JavaFileFilterImplementation filter,
            final boolean backgroundCompilation,
            final boolean ignoreExcludes,
            final boolean hasMemoryFileManager,
            final boolean useModifiedFiles) {
        ClassPath bootPath = ClassPath.getClassPath(fo, ClassPath.BOOT);
        if (bootPath == null) {
            //javac requires at least java.lang
            bootPath = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
        }
        ClassPath moduleBootPath = ClassPath.getClassPath(fo, JavaClassPathConstants.MODULE_BOOT_PATH);
        if (moduleBootPath == null) {
            moduleBootPath = ClassPath.EMPTY;
        }
        ClassPath compilePath = ClassPath.getClassPath(fo, ClassPath.COMPILE);
        if (compilePath == null) {
            compilePath = ClassPath.EMPTY;
        }
        ClassPath moduleCompilePath = ClassPath.getClassPath(fo, JavaClassPathConstants.MODULE_COMPILE_PATH);
        if (moduleCompilePath == null) {
            moduleCompilePath = ClassPath.EMPTY;
        }
        ClassPath moduleClassPath = ClassPath.getClassPath(fo, JavaClassPathConstants.MODULE_CLASS_PATH);
        if (moduleClassPath == null) {
            moduleClassPath = ClassPath.EMPTY;
        }
        ClassPath srcPath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        ClassPath moduleSrcPath = ClassPath.getClassPath(fo, JavaClassPathConstants.MODULE_SOURCE_PATH);
        return create (bootPath, moduleBootPath, compilePath, moduleCompilePath, moduleClassPath, srcPath, moduleSrcPath, filter, backgroundCompilation, ignoreExcludes, hasMemoryFileManager, useModifiedFiles, false, null);
    }

    @NonNull
    private static ClasspathInfo create(
            @NonNull final ClassPath bootPath,
            @NonNull final ClassPath moduleBootPath,
            @NonNull final ClassPath classPath,
            @NonNull final ClassPath moduleCompilePath,
            @NonNull final ClassPath moduleClassPath,
            @NullAllowed final ClassPath sourcePath,
            @NullAllowed final ClassPath moduleSourcePath,
            @NullAllowed final JavaFileFilterImplementation filter,
            final boolean backgroundCompilation,
            final boolean ignoreExcludes,
            final boolean hasMemoryFileManager,
            final boolean useModifiedFiles,
            final boolean requiresSourceRoots,
            @NullAllowed final Function<JavaFileManager.Location, JavaFileManager> jfmProvider) {
        return new ClasspathInfo(
                bootPath,
                moduleBootPath,
                classPath,
                moduleCompilePath,
                moduleClassPath,
                sourcePath,
                moduleSourcePath,
                filter,
                backgroundCompilation,
                ignoreExcludes,
                hasMemoryFileManager,
                useModifiedFiles,
                requiresSourceRoots,
                jfmProvider);
    }

    // Public methods ----------------------------------------------------------

    /** Registers ChangeListener which will be notified about the changes in the classpath.
     * @param listener The listener to register.
     */
    public void addChangeListener(@NonNull final ChangeListener listener) {
        listenerList.addChangeListener(listener);
    }

    /**Removes ChangeListener from the list of listeners.
     * @param listener The listener to remove.
     */
    public synchronized void removeChangeListener(@NonNull final ChangeListener listener) {
        listenerList.removeChangeListener(listener);
    }

    public ClassPath getClassPath (@NonNull PathKind pathKind) {
	switch( pathKind ) {
	    case BOOT:
		return this.bootClassPath;
	    case MODULE_BOOT:
		return this.moduleBootPath;
	    case COMPILE:
		return this.compileClassPath;
	    case MODULE_COMPILE:
		return this.moduleCompilePath;
	    case MODULE_CLASS:
		return this.moduleClassPath;
	    case SOURCE:
		return this.srcClassPath;
	    case MODULE_SOURCE:
		return this.moduleSrcPath;
	    default:
		assert false : "Unknown path type";     //NOI18N
		return null;
	}
    }

    ClassPath getCachedClassPath (PathKind pathKind) {
        switch( pathKind ) {
	    case BOOT:
		return this.cachedBootClassPath;
	    case COMPILE:
		return this.cachedCompileClassPath;
	    case MODULE_COMPILE:
		return this.cachedModuleCompilePath;
	    case MODULE_CLASS:
		return this.cachedModuleClassPath;
	    case SOURCE:
		return this.cachedSrcClassPath;
	    case OUTPUT:
		return this.outputClassPath;
	    default:
		assert false : "Unknown path type";     //NOI18N
		return null;
	}
    }


    public synchronized @NonNull ClassIndex getClassIndex () {
        if ( usagesQuery == null ) {
            usagesQuery = new ClassIndex (
                    this.bootClassPath,
                    this.compileClassPath,
                    this.cachedSrcClassPath);
        }
        return usagesQuery;
    }

    // Package private methods -------------------------------------------------

    @NonNull
    private synchronized JavaFileManager createFileManager(@NullAllowed final String sourceLevel) {
        final SiblingSource siblings = SiblingSupport.create();
        final ProxyFileManager.Configuration cfg = ProxyFileManager.Configuration.create(
            moduleBootPath,
            cachedModuleCompilePath,
            cachedBootClassPath,
            moduleCompilePath.entries().isEmpty() ? cachedCompileClassPath : cachedModuleClassPath,
            srcClassPath,
            cachedSrcClassPath,
            moduleSrcPath,
            outputClassPath,
            cachedAptSrcClassPath,
            siblings,
            fmTx,
            pgTx);
        cfg.setFilter(filter);
        cfg.setIgnoreExcludes(ignoreExcludes);
        cfg.setUseModifiedFiles(useModifiedFiles);
        cfg.setCustomFileManagerProvider(jfmProvider);
        for (Map.Entry<ClassPath,Function<URL,Collection<? extends URL>>> e : peerProviders.entrySet()) {
            cfg.setPeers(e.getKey(), e.getValue());
        }
        cfg.setSourceLevel(sourceLevel);
        return new ProxyFileManager(cfg);
    }


    // Private methods ---------------------------------------------------------

    private void fireChangeListenerStateChanged() {
        listenerList.fireChange();
    }

    @CheckForNull
    private static URI[] toURIs(@NullAllowed final ClassPath cp) {
        if (cp == null) {
            return null;
        }
        final List<ClassPath.Entry> entries = cp.entries();
        final List<URI> roots = new ArrayList<>(entries.size());
        for (ClassPath.Entry entry : entries) {
            try {
                roots.add(entry.getURL().toURI());
            } catch (URISyntaxException ex) {
                log.log(
                    Level.INFO,
                    "Cannot convert {0} to URI.",   //NOI18N
                    entry.getURL());
            }
        }
        return roots.toArray(new URI[0]);
    }


    // Innerclasses ------------------------------------------------------------


    public static enum PathKind {
	BOOT,
        MODULE_BOOT,
	COMPILE,
        MODULE_COMPILE,
        MODULE_CLASS,
	SOURCE,
	MODULE_SOURCE,
	OUTPUT,

    }

    private class ClassPathListener implements PropertyChangeListener {
        @Override
        public void propertyChange (PropertyChangeEvent event) {
            if (ClassPath.PROP_ROOTS.equals(event.getPropertyName())) {
                fireChangeListenerStateChanged();
            }
        }
    }

    private static final class Peers implements Function<URL,Collection<? extends URL>>, PropertyChangeListener {        

        private final ClassPath base;
        private volatile Map<URL,Collection<? extends URL>> cache;
        
        Peers(@NonNull final ClassPath base) {
            assert base != null;
            this.base = base;
            this.base.addPropertyChangeListener(WeakListeners.propertyChange(this, this.base));
        }
        
        @Override
        public Collection<? extends URL> apply(URL t) {
            return getCache().getOrDefault(t, Collections.singleton(t));
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ClassPath.PROP_ENTRIES.equals(evt.getPropertyName())) {
                cache = null;
            }
        }

        private Map<URL,Collection<? extends URL>> getCache() {
            Map<URL,Collection<? extends URL>> res = cache;
            if (res == null) {
                res = new HashMap<>();
                final PathRegistry pr = PathRegistry.getDefault();
                for (ClassPath.Entry e : base.entries()) {
                    URL[] srcs = pr.sourceForBinaryQuery(e.getURL(), base, false);
                    if (srcs != null) {
                        final Collection<URL> cfs = Arrays.stream(srcs)
                                .map((u) -> {
                                    try {
                                        return BaseUtilities.toURI(JavaIndex.getClassFolder(u)).toURL();
                                    } catch (IOException ioe) {
                                        return null;
                                    }
                                })
                                .filter((u) -> u != null)
                                .collect(Collectors.toList());
                        if (cfs.size() > 1) {
                            for (URL u : cfs) {
                                res.put(u, cfs);
                            }
                        }
                    }
                }
                cache = res;
            }
            return res;
        }
    }
    
    private static class ClasspathInfoAccessorImpl extends ClasspathInfoAccessor {

        @Override
        @NonNull
        public JavaFileManager createFileManager(
                @NonNull final ClasspathInfo cpInfo,
                @NullAllowed final String sourceLevel) {
            return cpInfo.createFileManager(sourceLevel);
        }

        @Override
        @NonNull
        public FileManagerTransaction getFileManagerTransaction(@NonNull ClasspathInfo cpInfo) {
            return cpInfo.fmTx;
        }

        @Override
        public ClassPath getCachedClassPath(final ClasspathInfo cpInfo, final PathKind kind) {
            return cpInfo.getCachedClassPath(kind);
        }

        @Override
        public ClasspathInfo create (
                @NonNull final ClassPath bootPath,
                @NonNull final ClassPath moduleBootPath,
                @NonNull final ClassPath classPath,
                @NonNull final ClassPath moduleCompilePath,
                @NonNull final ClassPath moduleClassPath,
                @NullAllowed final ClassPath sourcePath,
                @NullAllowed final ClassPath moduleSourcePath,
                @NullAllowed final JavaFileFilterImplementation filter,
                final boolean backgroundCompilation,
                final boolean ignoreExcludes,
                final boolean hasMemoryFileManager,
                final boolean useModifiedFiles,
                final boolean requiresSourceRoots,
                @NullAllowed final Function<JavaFileManager.Location, JavaFileManager> jfmProvider) {
            return ClasspathInfo.create(
                    bootPath,
                    moduleBootPath,
                    classPath,
                    moduleCompilePath,
                    moduleClassPath,
                    sourcePath,
                    moduleSourcePath,
                    filter,
                    backgroundCompilation,
                    ignoreExcludes,
                    hasMemoryFileManager,
                    useModifiedFiles,
                    requiresSourceRoots,
                    jfmProvider);
        }

        @Override
        public ClasspathInfo create (final @NonNull FileObject fo,
                final JavaFileFilterImplementation filter,
                final boolean backgroundCompilation,
                final boolean ignoreExcludes,
                final boolean hasMemoryFileManager,
                final boolean useModifiedFiles) {
            return ClasspathInfo.create(fo, filter, backgroundCompilation, ignoreExcludes, hasMemoryFileManager, useModifiedFiles);
        }

        @Override
        public boolean registerVirtualSource(final ClasspathInfo cpInfo, final InferableJavaFileObject jfo) throws UnsupportedOperationException {
            if (cpInfo.memoryFileManager == null) {
                throw new UnsupportedOperationException ("The ClassPathInfo doesn't support memory JavacFileManager");  //NOI18N
            }
            return cpInfo.memoryFileManager.register(jfo);
        }

        @Override
        public boolean unregisterVirtualSource(final ClasspathInfo cpInfo, final String fqn) throws UnsupportedOperationException {
            if (cpInfo.memoryFileManager == null) {
                throw new UnsupportedOperationException();
            }
            return cpInfo.memoryFileManager.unregister(fqn);
        }
    }

    /** Interface for {@link Task}s that want to provide {@link ClasspathInfo}. The interface is to be implemented
     * on a {@link Task}, which needs to provide its own classpath information. When the task is run, reinitializes the parser
     * to use that classpath.
     * 
     * @since 2.20
     */
    public interface Provider {
        public ClasspathInfo getClasspathInfo ();
    }
}