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

package org.netbeans.modules.java.source.indexing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.lang.model.SourceVersion;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.management.MBeanServer;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.CheckForNull;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
//import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.queries.AnnotationProcessingQuery;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.JavaSourceTaskFactoryManager;
import org.netbeans.modules.java.source.ModuleNames;
import org.netbeans.modules.java.source.NoJavacHelper;
import org.netbeans.modules.java.source.base.Module;
import org.netbeans.modules.java.source.parsing.FileManagerTransaction;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.InferableJavaFileObject;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.parsing.PrefetchableJavaFileObject;
import org.netbeans.modules.java.source.parsing.SourceFileManager;
import org.netbeans.modules.java.source.tasklist.TasklistSettings;
import org.netbeans.modules.java.source.usages.*;
import org.netbeans.modules.java.source.util.Iterators;
import org.netbeans.modules.java.ui.UIProvider;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.IndexableImpl;
import org.netbeans.modules.parsing.impl.indexing.SPIAccessor;
import org.netbeans.modules.parsing.impl.indexing.friendapi.IndexingController;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache.Convertor;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache.ErrorKind;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
//import org.openide.awt.HtmlBrowser;
//import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
//import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.TopologicalSortException;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Lahoda, Dusan Balek, Tomas Zezula
 */
public class JavaCustomIndexer extends CustomIndexer {

    public  static       boolean NO_ONE_PASS_COMPILE_WORKER = Boolean.getBoolean(JavaCustomIndexer.class.getName() + ".no.one.pass.compile.worker");
    private static final String DUMP_ON_LOW_MEM = System.getProperty(JavaCustomIndexer.class.getName() + ".dump.on.low.mem");    //NOI18N
    private static final String SOURCE_PATH = "sourcePath"; //NOI18N
    private static final String APT_SOURCE_OUTPUT = "apSrcOut"; //NOI18N
    private static final Pattern ANONYMOUS = Pattern.compile("\\$[0-9]"); //NOI18N
    private static final ClassPath EMPTY = ClassPathSupport.createClassPath(new URL[0]);
    private static final String PROP_JAVAC_VERSION = "javac.version"; //NOI18N

    @Override
    protected void index(final Iterable<? extends Indexable> files, final Context context) {
        JavaIndex.LOG.log(Level.FINE, context.isSupplementaryFilesIndexing() ? "index suplementary({0})" :"index({0})", context.isAllFilesIndexing() ? context.getRootURI() : files); //NOI18N
        final TransactionContext txCtx = TransactionContext.get();
        final FileManagerTransaction fmTx = txCtx.get(FileManagerTransaction.class);
        assert fmTx != null;
        final ClassIndexEventsTransaction ciTx = txCtx.get(ClassIndexEventsTransaction.class);
        assert ciTx != null;
        try {
            final FileObject root = context.getRoot();
            if (root == null) {
                JavaIndex.LOG.fine("Ignoring request with no root"); //NOI18N
                return;
            }
            BinaryForSourceQuery.Result2 binRes = BinaryForSourceQuery.findBinaryRoots2(root.toURL());
            if (binRes.preferBinaries()) {
                final URL[] binaryRoots = binRes.getRoots();
                final FileObject[] binaryRootsFo = new FileObject[binaryRoots.length];
                Long newestFile = null;
                int at = 0;
                for (URL u : binaryRoots) {
                    FileObject ufo = URLMapper.findFileObject(u);
                    if (ufo == null) {
                        newestFile = null;
                        break;
                    }
                    binaryRootsFo[at++] = ufo;
                    Enumeration<? extends FileObject> en = ufo.getChildren(true);
                    while (en.hasMoreElements()) {
                        FileObject ch = en.nextElement();
                        long modified = ch.lastModified().getTime();
                        if (newestFile == null || newestFile < modified) {
                            newestFile = modified;
                        }
                    }
                }

                Set<String> shortNames = new HashSet<>();
                boolean binariesAreNewer = true;
                for (Indexable index : files) {
                    FileObject fo = context.getRoot().getFileObject(index.getRelativePath());
                    if (newestFile == null || fo == null || fo.lastModified().getTime() > newestFile) {
                        binariesAreNewer = false;
                        break;
                    }
                    shortNames.add(fo.getNameExt());
                }

                if (binariesAreNewer) {
                    JavaIndex.LOG.log(Level.FINE, "Using binaries for {0}", FileUtil.getFileDisplayName(root)); // NOI18N
                    File copyTo = JavaIndex.getClassFolder(context);
                    at = 0;
                    for (URL singleBinaryRoot : binaryRoots) {
                        FileObject singleBinaryRootFo = binaryRootsFo[at++];
                       JavaBinaryIndexer.doIndex(context, singleBinaryRoot, (cf) -> {
                            String src = cf.getSourceFileName();
                            if (src != null) {
                                String srcName = src.substring(src.lastIndexOf(File.separatorChar) + 1);
                                if (!shortNames.contains(srcName)) {
                                    return false;
                                }
                            }
                            return true;
                       });

                        JavaIndex.LOG.log(Level.FINE, "  copying from {0} to {1}", new Object[] { FileUtil.getFileDisplayName(singleBinaryRootFo), copyTo }); // NOI18N
                        Enumeration<? extends FileObject> en = singleBinaryRootFo.getChildren(true);
                        while (en.hasMoreElements()) {
                            FileObject ch = en.nextElement();
                            if (!ch.isData()) {
                                continue;
                            }
                            String path = FileUtil.getRelativePath(singleBinaryRootFo, ch.getParent());
                            if (path == null) {
                                continue;
                            }
                            String name = ch.getNameExt();
                            if (ch.hasExt("class")) {
                                name = ch.getName() + ".sig";
                            }
                            File toDir = new File(copyTo, path.replace('/', File.separatorChar));
                            toDir.mkdirs();
                            File to = new File(toDir, name);
                            try (OutputStream os = new FileOutputStream(to); InputStream is = ch.getInputStream()) {
                                FileUtil.copy(is, os);
                            }
                        }
                    }
                    APTUtils.sourceRootRegistered(context.getRoot(), context.getRootURI());
                    JavaIndex.LOG.log(Level.FINE, "Binaries copied for {0}", FileUtil.getFileDisplayName(root)); // NOI18N
                    return;
                }
            }

            APTUtils.sourceRootRegistered(context.getRoot(), context.getRootURI());
            final ClassPath sourcePath = ClassPath.getClassPath(root, ClassPath.SOURCE);
            final ClassPath moduleSourcePath = ClassPath.getClassPath(root, JavaClassPathConstants.MODULE_SOURCE_PATH);
            final ClassPath bootPath = ClassPath.getClassPath(root, ClassPath.BOOT);
            final ClassPath moduleBootPath = ClassPath.getClassPath(root, JavaClassPathConstants.MODULE_BOOT_PATH);
            final ClassPath compilePath = ClassPath.getClassPath(root, ClassPath.COMPILE);                                    
            final ClassPath moduleCompilePath = ClassPath.getClassPath(root, JavaClassPathConstants.MODULE_COMPILE_PATH);
            final ClassPath moduleClassPath = ClassPath.getClassPath(root, JavaClassPathConstants.MODULE_CLASS_PATH);
            if (sourcePath == null || bootPath == null || compilePath == null) {
                txCtx.get(CacheAttributesTransaction.class).setInvalid(true);
                JavaIndex.LOG.log(Level.WARNING, "Ignoring root with no ClassPath: {0}", FileUtil.getFileDisplayName(root)); // NOI18N
                return;
            }            
            if (!Arrays.asList(sourcePath.getRoots()).contains(root)) {
                txCtx.get(CacheAttributesTransaction.class).setInvalid(true);
                JavaIndex.LOG.log(Level.WARNING, "Source root: {0} is not on its sourcepath", FileUtil.getFileDisplayName(root)); // NOI18N
                return;
            }
            if (isAptBuildGeneratedFolder(context.getRootURI(),sourcePath)) {
                txCtx.get(CacheAttributesTransaction.class).setInvalid(true);
                JavaIndex.setAttribute(context.getRootURI(), APT_SOURCE_OUTPUT, Boolean.TRUE.toString());
                JavaIndex.LOG.fine("Ignoring annotation processor build generated folder"); //NOI18N
                return;
            }
            JavaIndex.setAttribute(context.getRootURI(), APT_SOURCE_OUTPUT, null);
            if (!files.iterator().hasNext() && !context.isAllFilesIndexing()) {
                boolean success = false;
                try {
                    final JavaParsingContext javaContext = new JavaParsingContext(
                            context,
                            bootPath,
                            moduleBootPath != null ? moduleBootPath : ClassPath.EMPTY,
                            compilePath,
                            moduleCompilePath != null ? moduleCompilePath : ClassPath.EMPTY,
                            moduleClassPath != null ? moduleClassPath : ClassPath.EMPTY,
                            sourcePath,
                            moduleSourcePath,
                            Collections.<CompileTuple>emptySet());
                    try {
                        final ClassIndexImpl cii = javaContext.getClassIndexImpl();
                        if (cii != null) {  //Null when IDE is exiting, indeces are already closed.
                            cii.setDirty(null);
                        }
                    } finally {
                        javaContext.finish();
                    }
                    success = true;
                } finally {
                    if (!success) {
                        JavaIndex.setAttribute(context.getRootURI(), ClassIndexManager.PROP_DIRTY_ROOT, Boolean.TRUE.toString());
                    }
                }
            } else {
                final List<Indexable> javaSources = new ArrayList<Indexable>();
                final Collection<? extends CompileTuple> virtualSourceTuples = translateVirtualSources (
                    splitSources(files,javaSources),
                    context.getRootURI());
                final JavaParsingContext javaContext;
                try {
                    //todo: Ugly hack, the ClassIndexManager.createUsagesQuery has to be called before the root is set to dirty mode.
                    javaContext = new JavaParsingContext(
                            context,
                            bootPath,
                            moduleBootPath != null ? moduleBootPath : ClassPath.EMPTY,
                            compilePath,
                            moduleCompilePath != null ? moduleCompilePath : ClassPath.EMPTY,
                            moduleClassPath != null ? moduleClassPath : ClassPath.EMPTY,
                            sourcePath,
                            moduleSourcePath,
                            virtualSourceTuples);
                } finally {
                    JavaIndex.setAttribute(context.getRootURI(), ClassIndexManager.PROP_DIRTY_ROOT, Boolean.TRUE.toString());
                }
                boolean finished = false;
                final Set<ElementHandle<TypeElement>> removedTypes = new HashSet <> ();
                ElementHandle<ModuleElement> removedModule = null;
                final Set<File> removedFiles = new HashSet<> ();
                final List<CompileTuple> toCompile = new ArrayList<>(javaSources.size()+virtualSourceTuples.size());
                CompileWorker.ParsingOutput compileResult = null;
                try {
                    if (context.isAllFilesIndexing()) {
                        cleanUpResources(context, fmTx);
                    }
                    if (javaContext.getClassIndexImpl() == null) {
                        return; //IDE is exiting, indeces are already closed.
                    }
                    javaContext.getClassIndexImpl().setDirty(null);
                    final SourceFileManager.ModifiedFilesTransaction mftx = txCtx.get(SourceFileManager.ModifiedFilesTransaction.class);
                    final boolean[] isModuleInfo = new boolean[1];
                    URL[] binaries = null;
                    for (Indexable i : javaSources) {
                        final CompileTuple tuple = createTuple(context, javaContext, i);
                        if (tuple != null) {
                            toCompile.add(tuple);
                        }
                        if (mftx != null) {
                            try {
                                mftx.cacheUpdated(i.getURL().toURI());
                            } catch (URISyntaxException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                        clear(context, javaContext, i, removedTypes, removedFiles, fmTx, isModuleInfo);
                        if (isModuleInfo[0]) {
                            final String moduleName = JavaIndex.getAttribute(context.getRootURI(), JavaIndex.ATTR_MODULE_NAME, null);
                            removedModule = moduleName == null ?
                                    null :
                                    ElementHandleAccessor.getInstance().create(ElementKind.MODULE, moduleName);
                            JavaIndex.setAttribute(context.getRootURI(), JavaIndex.ATTR_MODULE_NAME, null);
                            binaries = findBinaries(context.getRootURI());
                            for (URL binary : binaries) {
                                ModuleNames.getInstance().reset(binary);
                            }
                        }
                    }
                    for (CompileTuple tuple : virtualSourceTuples) {
                        clear(context, javaContext, tuple.indexable, removedTypes, removedFiles, fmTx, null);
                    }
                    toCompile.addAll(virtualSourceTuples);
                    List<CompileTuple> toCompileRound = toCompile;
                    int round = 0;
                    String moduleName = null;
                    while (round++ < 2) {
                        CompileWorker[] WORKERS = Lookup.getDefault().lookup(CompileWorkerProvider.class).getWorkers(toCompileRound);
                        for (CompileWorker w : WORKERS) {
                            compileResult = w.compile(compileResult, context, javaContext, toCompileRound);
                            if (compileResult == null || context.isCancelled()) {
                                return; // cancelled, IDE is sutting down
                            }
                            if (compileResult.lowMemory) {
                                w.freeMemory(false);
                            }
                            moduleName = moduleName(moduleName, compileResult);
                            if (compileResult.success) {
                                break;
                            }
                        }
                        if (compileResult.aptGenerated.isEmpty()) {
                            round++;
                        } else {
                            toCompileRound = new ArrayList<CompileTuple>(compileResult.aptGenerated.size());
                            final SPIAccessor accessor = SPIAccessor.getInstance();
                            for (javax.tools.FileObject fo : compileResult.aptGenerated) {
                                final PrefetchableJavaFileObject pfo = (PrefetchableJavaFileObject) fo;
                                final Indexable i = accessor.create(new AptGeneratedIndexable(pfo));
                                CompileTuple ct = new CompileTuple(pfo, i, false, true, true);
                                toCompileRound.add(ct);
                                toCompile.add(ct);
                            }
                            compileResult.aptGenerated.clear();
                        }
                    }
                    if (moduleName != null) {
                        JavaIndex.setAttribute(context.getRootURI(), JavaIndex.ATTR_MODULE_NAME, moduleName);
                        if (binaries == null) {
                            binaries = findBinaries(context.getRootURI());
                        }
                        for (URL binary : binaries) {
                            ModuleNames.getInstance().reset(binary);
                        }
                    }
                    finished = compileResult.success;

                    if (compileResult.lowMemory) {
                        final String rootName = FileUtil.getFileDisplayName(context.getRoot());
                        JavaIndex.LOG.log(
                            Level.WARNING,
                            "Not enough memory to compile folder: {0}.",     //NOI18N
                            rootName);
                        UIProvider uip = Lookup.getDefault().lookup(UIProvider.class);
                        if (uip != null) {
                            uip.notifyLowMemory(rootName);
                        }
                        if (DUMP_ON_LOW_MEM != null) {
                            final File heapDump = dumpHeap(DUMP_ON_LOW_MEM);
                            if (heapDump != null) {
                                JavaIndex.LOG.log(
                                    Level.INFO,
                                    "Heap dump generated into: {0}.",     //NOI18N
                                    heapDump.getAbsolutePath());
                            } else {
                                JavaIndex.LOG.log(
                                    Level.WARNING,
                                    "Cannot generate heap dump.");     //NOI18N
                            }
                        }
                    }
                } finally {
                    try {
                        javaContext.finish();
                    } finally {
                        if (finished) {
                            JavaIndex.setAttribute(context.getRootURI(), ClassIndexManager.PROP_DIRTY_ROOT, null);
                        } else {
                            txCtx.get(CacheAttributesTransaction.class).setInvalid(true);
                        }
                    }
                }
                assert compileResult != null;

                Set<ElementHandle<TypeElement>> _at = new HashSet<> (compileResult.addedTypes); //Added types
                Set<ElementHandle<TypeElement>> _rt = new HashSet<> (removedTypes); //Removed types
                _at.removeAll(removedTypes);
                _rt.removeAll(compileResult.addedTypes);
                compileResult.addedTypes.retainAll(removedTypes); //Changed types
                final ElementHandle<ModuleElement> addedModule = compileResult.addedModules.isEmpty() ?
                        null :
                        compileResult.addedModules.iterator().next();

                if (!context.isSupplementaryFilesIndexing() && !context.isCancelled()) {
                    compileResult.modifiedTypes.addAll(_rt);
                    Map<URL, Set<URL>> root2Rebuild = findDependent(context.getRootURI(), compileResult.modifiedTypes, !_at.isEmpty());
                    Set<URL> urls = root2Rebuild.get(context.getRootURI());
                    if (urls != null) {
                        if (context.isAllFilesIndexing()) {
                            root2Rebuild.remove(context.getRootURI());
                        } else {
                            for (CompileTuple ct : toCompile)
                                urls.remove(ct.indexable.getURL());
                            if (urls.isEmpty())
                                root2Rebuild.remove(context.getRootURI());
                        }
                    }
                    for (Map.Entry<URL, Set<URL>> entry : root2Rebuild.entrySet()) {
                        context.addSupplementaryFiles(entry.getKey(), entry.getValue());
                    }
                }
                try {
                    javaContext.store();
                } catch (JavaParsingContext.BrokenIndexException bi) {
                    JavaIndex.LOG.log(
                        Level.WARNING,
                        "Broken index for root: {0} reason {1}, recovering.",  //NOI18N
                        new Object[] {
                            context.getRootURI()
                        });
                    final PersistentIndexTransaction piTx = txCtx.get(PersistentIndexTransaction.class);
                    piTx.setBroken();
                }
                ciTx.addedTypes(context.getRootURI(), removedModule == null ? addedModule : null,  _at);
                ciTx.removedTypes(context.getRootURI(), addedModule == null ? removedModule : null, _rt);
                ciTx.changedTypes(context.getRootURI(), removedModule != null && addedModule != null ? addedModule : null, compileResult.addedTypes);
                if (!context.checkForEditorModifications()) { // #152222
                    ciTx.addedCacheFiles(context.getRootURI(), compileResult.createdFiles);
                    ciTx.removedCacheFiles(context.getRootURI(), removedFiles);
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    @NonNull
    private static URL[] findBinaries(@NonNull final URL sourceRoot) throws IOException {
        final URL[] artefacts = BinaryForSourceQuery.findBinaryRoots(sourceRoot).getRoots();
        final List<URL> bin = new ArrayList<>(artefacts.length+1);
        Collections.addAll(bin, artefacts);
        bin.add(BaseUtilities.toURI(JavaIndex.getClassFolder(sourceRoot, false, false)).toURL());
        return bin.toArray(new URL[0]);
    }

    private static List<? extends Indexable> splitSources(final Iterable<? extends Indexable> indexables, final List<? super Indexable> javaSources) {
        List<Indexable> virtualSources = new LinkedList<Indexable>();
        for (Indexable indexable : indexables) {
            if (indexable.getURL() == null) {
                /*
                    Issue #168179: This is probably deleted source file. Just skipping. 
                 */
                continue;
            }
            if (VirtualSourceProviderQuery.hasVirtualSource(indexable)) {
                virtualSources.add(indexable);
            }
            else {
                javaSources.add(indexable);
            }
        }
        return virtualSources;
    }

    private static Collection<? extends CompileTuple> translateVirtualSources(final Collection<? extends Indexable> virtualSources, final URL rootURL) throws IOException {
        if (virtualSources.isEmpty()) {
            return Collections.<CompileTuple>emptySet();
        }
        try {
            final File root = BaseUtilities.toFile(URI.create(rootURL.toString()));
            return VirtualSourceProviderQuery.translate(virtualSources, root);
        } catch (IllegalArgumentException e) {
            //Called on non local fs => not supported, log and ignore.
            JavaIndex.LOG.log(Level.WARNING, "Virtual sources in the root: {0} are ignored due to: {1}", new Object[]{rootURL, e.getMessage()}); //NOI18N
            return Collections.<CompileTuple>emptySet();
        }
    }

    private static CompileTuple createTuple(Context context, JavaParsingContext javaContext, Indexable indexable) {
        File root = null;
        if (!context.checkForEditorModifications() && "file".equals(indexable.getURL().getProtocol()) && (root = FileUtil.toFile(context.getRoot())) != null) { //NOI18N
            try {
                return new CompileTuple(
                    FileObjects.fileFileObject(
                        indexable,
                        root,
                        javaContext.getJavaFileFilter(),
                        javaContext.getEncoding()),
                    indexable);
            } catch (Exception ex) {
                //pass
            }
        }
        FileObject fo = URLMapper.findFileObject(indexable.getURL());
        return fo != null ? new CompileTuple(FileObjects.sourceFileObject(fo, context.getRoot()), indexable) : null;
    }

    private static void clearFiles(final Context context, final Iterable<? extends Indexable> files) throws IOException {
        final TransactionContext txCtx =  TransactionContext.get();
        assert txCtx != null;
        final FileManagerTransaction fmTx = txCtx.get(FileManagerTransaction.class);
        assert fmTx != null;
        final ClassIndexEventsTransaction ciTx = txCtx.get(ClassIndexEventsTransaction.class);
        assert ciTx != null;
        final JavaParsingContext javaContext = new JavaParsingContext(context, true);
        try {
            if (javaContext.getClassIndexImpl() == null)
                return; //IDE is exiting, indeces are already closed.
            if (javaContext.getClassIndexImpl().getType() == ClassIndexImpl.Type.EMPTY)
                return; //No java no need to continue
            final Set<ElementHandle<TypeElement>> removedTypes = new HashSet <ElementHandle<TypeElement>> ();
            final Set<File> removedFiles = new HashSet<File> ();
            final boolean[] isModuleInfo = new boolean[1];
            ElementHandle<ModuleElement> module = null;
            for (Indexable i : files) {
                clear(context, javaContext, i, removedTypes, removedFiles, fmTx, isModuleInfo);
                if (isModuleInfo[0]) {
                    final String moduleName = JavaIndex.getAttribute(context.getRootURI(), JavaIndex.ATTR_MODULE_NAME, null);
                    JavaIndex.setAttribute(context.getRootURI(), JavaIndex.ATTR_MODULE_NAME, null);
                    module = moduleName == null ?
                            null :
                            ElementHandleAccessor.getInstance().create(ElementKind.MODULE, moduleName);
                }
                ErrorsCache.setErrors(context.getRootURI(), i, Collections.<Diagnostic<?>>emptyList(), ERROR_CONVERTOR);
                ExecutableFilesIndex.DEFAULT.setMainClass(context.getRootURI(), i.getURL(), false);
                javaContext.getCheckSums().remove(i.getURL());
            }
            for (Map.Entry<URL, Set<URL>> entry : findDependent(context.getRootURI(), removedTypes, false).entrySet()) {
                context.addSupplementaryFiles(entry.getKey(), entry.getValue());
            }
            try {
                javaContext.store();
            } catch (JavaParsingContext.BrokenIndexException bi) {
                JavaIndex.LOG.log(
                    Level.WARNING,
                    "Broken index for root: {0} reason: {1}, recovering.",  //NOI18N
                    new Object[] {
                        context.getRootURI(),
                        bi.getMessage()
                    });
                final PersistentIndexTransaction piTx = txCtx.get(PersistentIndexTransaction.class);
                assert piTx != null;
                piTx.setBroken();
            }
            ciTx.removedCacheFiles(context.getRootURI(), removedFiles);
            ciTx.removedTypes(context.getRootURI(), module, removedTypes);
        } finally {
            javaContext.finish();
        }
    }

    private static void clear(
            @NonNull final Context context,
            @NonNull final JavaParsingContext javaContext,
            @NonNull final Indexable indexable,
            @NonNull final Set<ElementHandle<TypeElement>> removedTypes,
            @NonNull final Set<File> removedFiles,
            @NonNull final FileManagerTransaction fmTx,
            @NullAllowed final boolean[] isModuleInfo) throws IOException {
        assert fmTx != null;
        if (isModuleInfo != null) {
            isModuleInfo[0] = false;
        }
        final List<Pair<String,String>> toDelete = new ArrayList<Pair<String,String>>();
        final File classFolder = JavaIndex.getClassFolder(context);
        final File aptFolder = JavaIndex.getAptFolder(context.getRootURI(), false);
        final String sourceRelative = indexable.getRelativePath();
        final List<Pair<String,URL>> sourceRelativeURLPairs = new LinkedList<Pair<String,URL>>();
        sourceRelativeURLPairs.add(Pair.of(sourceRelative,indexable.getURL()));
        File file;
        if (aptFolder.exists()) {
            file = new File(classFolder,  FileObjects.stripExtension(sourceRelative) + '.' + FileObjects.RAPT);
            if (file.exists()) {
                try {
                    for (String fileName : readRSFile(file)) {
                        File f = new File (aptFolder, fileName);
                        if (f.exists() && FileObjects.JAVA.equals(FileObjects.getExtension(f.getName()))) {
                            sourceRelativeURLPairs.add(Pair.of(fileName, BaseUtilities.toURI(f).toURL()));
                        }
                        fmTx.delete(f);
                    }
                } catch (IOException ioe) {
                    //The signature file is broken, report it but don't stop scanning
                    Exceptions.printStackTrace(ioe);
                }
                fmTx.delete(file);
            }
        }
        for (Pair<String,URL> relURLPair : sourceRelativeURLPairs) {
            final String ext = FileObjects.getExtension(relURLPair.first());
            final String withoutExt = FileObjects.stripExtension(relURLPair.first());
            final boolean dieIfNoRefFile = VirtualSourceProviderQuery.hasVirtualSource(ext);
            if (dieIfNoRefFile) {
                file = new File(classFolder, relURLPair.first() + '.' + FileObjects.RX);
            } else {
                file = new File(classFolder, withoutExt + '.' + FileObjects.RS);
            }
            boolean cont = !dieIfNoRefFile;
            if (file.exists()) {
                cont = false;
                try {
                    String binaryName = FileObjects.getBinaryName(file, classFolder);
                    for (String className : readRSFile(file)) {
                        File f = new File(classFolder, FileObjects.convertPackage2Folder(className) + '.' + FileObjects.SIG);
                        if (!binaryName.equals(className)) {
                            if (javaContext.getFQNs().remove(className, relURLPair.second())) {
                                toDelete.add(Pair.<String, String>of(className, relURLPair.first()));
                                removedTypes.add(ElementHandleAccessor.getInstance().create(ElementKind.OTHER, className));
                                removedFiles.add(f);
                                fmTx.delete(f);
                            }
                        } else {
                            cont = !dieIfNoRefFile;
                        }
                    }
                } catch (IOException ioe) {
                    //The signature file is broken, report it but don't stop scanning
                    Exceptions.printStackTrace(ioe);
                }
                fmTx.delete(file);
            }
            if (cont) {
                file = new File(classFolder, withoutExt + '.' + FileObjects.SIG);
                if (file.exists()) {
                    String fileName = file.getName();
                    fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                    final String[][] patterns = new String[][]{
                        new String[]{fileName + '.', "", FileObjects.SIG, FileObjects.RS, FileObjects.RAPT, FileObjects.RX},    //NOI18N
                        new String[]{fileName + '$', null, FileObjects.SIG}                                                       //NOI18N
                    };
                    File parent = file.getParentFile();
                    FilenameFilter filter = new FilenameFilter() {

                        @Override
                        public boolean accept(File dir, String name) {
                            for (final String[] pattern : patterns) {
                                if (name.startsWith(pattern[0])) {
                                    final String ext = FileObjects.getExtension(name);
                                    for (int i = 2; i< pattern.length; i++) {
                                        if (pattern[i].equals(ext) && (pattern[1] == null || name.length() == pattern[0].length() + pattern[i].length())) {
                                            return true;
                                        }
                                    }
                                }
                            }
                            return false;
                        }
                    };
                    final File[] children = parent.listFiles(filter);
                    if (children != null) {
                        for (File f : children) {
                            String className = FileObjects.getBinaryName(f, classFolder);
                            if (!javaContext.getFQNs().check(className, relURLPair.second())) {
                                javaContext.getFQNs().remove(className, relURLPair.second());
                                toDelete.add(
                                        FileObjects.MODULE_INFO.equals(className) ?
                                                Pair.<String, String>of(null, relURLPair.first()) :
                                                Pair.<String, String>of(className, null));
                                removedTypes.add(ElementHandleAccessor.getInstance().create(ElementKind.OTHER, className));
                                removedFiles.add(f);
                                fmTx.delete(f);
                            }
                        }
                    }
                }
                if (FileObjects.MODULE_INFO.equals(withoutExt)) {
                    if (isModuleInfo != null) {
                        isModuleInfo[0] = true;
                    }
                }
            }
        }
        javaContext.delete(indexable, toDelete);
    }

    private static void markDirtyFiles(final Context context, final Iterable<? extends Indexable> files) {
        ClassIndexImpl indexImpl = ClassIndexManager.getDefault().getUsagesQuery(context.getRootURI(), false);
        if (indexImpl != null) {
            for (Indexable i : files) {
                indexImpl.setDirty(i.getURL());
            }
        }
    }

    public static Collection<? extends ElementHandle<TypeElement>> getRelatedTypes (final File source, final File root) throws IOException {
        final List<ElementHandle<TypeElement>> result = new LinkedList<ElementHandle<TypeElement>>();
        final File classFolder = JavaIndex.getClassFolder(root);
        final String path = FileObjects.getRelativePath(root, source);
        final String ext = FileObjects.getExtension(path);
        final String pathNoExt = FileObjects.stripExtension(path);
        final boolean dieIfNoRefFile = VirtualSourceProviderQuery.hasVirtualSource(ext);
        File file;
        if (dieIfNoRefFile) {
            file = new File (classFolder, path + '.' + FileObjects.RX); //NOI18N
        }
        else {
            file = new File (classFolder, pathNoExt + '.' + FileObjects.RS); //NOI18N
        }
        
        boolean cont = !dieIfNoRefFile;
        if (file.exists()) {
            cont = false;
            try {
                String binaryName = FileObjects.getBinaryName(file, classFolder);
                for (String className : readRSFile(file)) {
                    if (!binaryName.equals(className)) {
                        result.add(ElementHandleAccessor.getInstance().create(ElementKind.CLASS, className));
                    } else {
                        cont = !dieIfNoRefFile;
                    }
                }
            } catch (IOException ioe) {
                //The signature file is broken, report it but don't stop scanning
                Exceptions.printStackTrace(ioe);
            }
        }
        if (cont && (file = new File(classFolder, pathNoExt + '.' + FileObjects.SIG)).exists()) {
            String fileName = file.getName();
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
            final String[] patterns = new String[] {fileName + '.', fileName + '$'}; //NOI18N
            File parent = file.getParentFile();
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (!name.endsWith(FileObjects.SIG)) {
                        return false;
                    }
                    for (int i=0; i< patterns.length; i++) {
                        if (name.startsWith(patterns[i])) {
                            return true;
                        }
                    }
                    return false;
                }
            };
            for (File f : parent.listFiles(filter)) {
                String className = FileObjects.getBinaryName (f, classFolder);
                result.add(ElementHandleAccessor.getInstance().create(ElementKind.CLASS, className));
            }
        }
        return result;
    }

    public static void addAptGenerated(
            @NonNull final Context context,
            @NonNull JavaParsingContext javaContext,
            @NonNull final CompileTuple source,
            @NonNull final Set<javax.tools.FileObject> aptGenerated) throws IOException {
        final Set<javax.tools.FileObject> genSources = javaContext.getProcessorGeneratedFiles().getGeneratedSources(source.indexable.getURL());
        if (genSources != null) {
            aptGenerated.addAll(genSources);
        }
    }

    public static void setErrors(Context context, CompileTuple active, DiagnosticListenerImpl errors) {
        if (!active.virtual) {
            Iterable<Diagnostic<? extends JavaFileObject>> filteredErrorsList = Iterators.filter(errors.getDiagnostics(active.jfo), new FilterOutJDK7AndLaterWarnings());
            ErrorsCache.setErrors(context.getRootURI(), active.indexable, filteredErrorsList, active.aptGenerated ? ERROR_CONVERTOR_NO_BADGE : ERROR_CONVERTOR);
        }
    }

    public static void brokenPlatform(
            @NonNull final Context ctx,
            @NonNull final Iterable<? extends CompileTuple> files,
            @NullAllowed final Diagnostic<JavaFileObject> diagnostic) {
        if (diagnostic == null) {
            return;
        }
        final Diagnostic<JavaFileObject> error = new Diagnostic<JavaFileObject>() {

            @Override
            public Kind getKind() {
                return Kind.ERROR;
            }

            @Override
            public JavaFileObject getSource() {
                return diagnostic.getSource();
            }

            @Override
            public long getPosition() {
                return diagnostic.getPosition();
            }

            @Override
            public long getStartPosition() {
                return diagnostic.getStartPosition();
            }

            @Override
            public long getEndPosition() {
                return diagnostic.getEndPosition();
            }

            @Override
            public long getLineNumber() {
                return diagnostic.getLineNumber();
            }

            @Override
            public long getColumnNumber() {
                return diagnostic.getColumnNumber();
            }

            @Override
            public String getCode() {
                return diagnostic.getCode();
            }

            @Override
            public String getMessage(Locale locale) {
                return diagnostic.getMessage(locale);
            }
        };
        for (CompileTuple file : files) {
            if (!file.virtual) {
                ErrorsCache.setErrors(
                    ctx.getRootURI(),
                    file.indexable,
                    Collections.<Diagnostic<JavaFileObject>>singleton(error),
                    ERROR_CONVERTOR);
            }
        }
    }

    private static Iterable<String> readRSFile (final File file) throws IOException {
        final LinkedHashSet<String> binaryNames = new LinkedHashSet<String>();
        BufferedReader in = new BufferedReader (new InputStreamReader ( new FileInputStream (file), StandardCharsets.UTF_8));
        try {
            String binaryName;
            while ((binaryName=in.readLine())!=null) {
                binaryNames.add(binaryName);
            }
        } finally {
            in.close();
        }
        return binaryNames;
    }

    private static Map<URL, Set<URL>> findDependent(final URL root, final Collection<ElementHandle<TypeElement>> classes, boolean includeFilesInError) throws IOException {
        //get dependencies
        Map<URL, List<URL>> deps = IndexingController.getDefault().getRootDependencies();
        Map<URL, List<URL>> peers = IndexingController.getDefault().getRootPeers();
        //create inverse dependencies
        final Map<URL, List<URL>> inverseDeps = new HashMap<URL, List<URL>> ();
        for (Map.Entry<URL,List<URL>> entry : deps.entrySet()) {
            final URL u1 = entry.getKey();
            final List<URL> l1 = entry.getValue();
            for (URL u2 : l1) {
                List<URL> l2 = inverseDeps.get(u2);
                if (l2 == null) {
                    l2 = new ArrayList<URL>();
                    inverseDeps.put (u2,l2);
                }
                l2.add (u1);
            }
        }
        return findDependent(root, deps, inverseDeps, peers, classes, includeFilesInError, true);
    }

    @CheckForNull
    private static String moduleName(
        @NullAllowed final String moduleName,
        @NonNull final CompileWorker.ParsingOutput res) {
        if (res.success) {
            return res.moduleName;
        } else {
            return res.moduleName != null ?
                res.moduleName :
                moduleName;
        }
    }


    public static Map<URL, Set<URL>> findDependent(final URL root,
            final Map<URL, List<URL>> sourceDeps,
            final Map<URL, List<URL>> inverseDeps,
            final Map<URL, List<URL>> peers,
            final Collection<ElementHandle<TypeElement>> classes,
            boolean includeFilesInError,
            boolean includeCurrentSourceRoot) throws IOException {
        final Map<URL, Set<URL>> ret = new LinkedHashMap<URL, Set<URL>>();

        //performance: filter out anonymous innerclasses:
        for (Iterator<ElementHandle<TypeElement>> i = classes.iterator(); i.hasNext(); ) {
            if (ANONYMOUS.matcher(i.next().getBinaryName()).find()) {
                i.remove();
            }
        }
        if (classes.isEmpty() && !includeFilesInError) {
            return ret;
        }

        //get sorted list of depenedent roots
        List<URL> depRoots = inverseDeps.get(root);
        try {
            switch (TasklistSettings.getDependencyTracking()) {
                case DISABLED:
                    if (depRoots == null) {
                        JavaIndex.setAttribute(root, ClassIndexManager.PROP_DIRTY_ROOT, Boolean.TRUE.toString());
                    } else {
                        for (URL url : depRoots) {
                            JavaIndex.setAttribute(url, ClassIndexManager.PROP_DIRTY_ROOT, Boolean.TRUE.toString());
                        }
                    }
                    return ret;
                case ENABLED_WITHIN_ROOT:
                    if (depRoots != null) {
                        for (URL url : depRoots) {
                            JavaIndex.setAttribute(url, ClassIndexManager.PROP_DIRTY_ROOT, Boolean.TRUE.toString());
                        }
                    }
                    depRoots = Collections.singletonList(root);
                    break;
                case ENABLED_WITHIN_PROJECT:
                    final Project rootPrj = FileOwnerQuery.getOwner(root.toURI());
                    if (depRoots == null) {
                        if (rootPrj == null) {
                            depRoots = Collections.singletonList(root);
                        } else {
                            depRoots = new ArrayList<URL>();
                            depRoots.add(root);
                            int index = depRoots.indexOf(root);
                            depRoots.addAll(index+1, getSrcRootPeers(peers, root));
                        }
                    } else {                        
                        if (rootPrj == null) {
                            for (URL url : depRoots) {
                                JavaIndex.setAttribute(url, ClassIndexManager.PROP_DIRTY_ROOT, Boolean.TRUE.toString());
                            }
                            depRoots = Collections.singletonList(root);
                        } else {
                            List<URL> l = new ArrayList<URL>(depRoots.size());
                            for (URL url : depRoots) {
                                if (FileOwnerQuery.getOwner(url.toURI()) == rootPrj) {
                                    l.add(url);
                                } else {
                                    JavaIndex.setAttribute(url, ClassIndexManager.PROP_DIRTY_ROOT, Boolean.TRUE.toString());
                                }
                            }
                            l.add(root);
                            depRoots = BaseUtilities.topologicalSort(l, inverseDeps);                            
                            int index = depRoots.indexOf(root);
                            depRoots.addAll(index+1, getSrcRootPeers(peers, root));
                        }
                    }
                    break;
                case ENABLED:
                    if (depRoots == null) {
                        depRoots = new ArrayList<URL>();
                        depRoots.add(root);
                    } else {
                        List<URL> l = new ArrayList<URL>(depRoots);
                        l.add(root);
                        depRoots = BaseUtilities.topologicalSort(l, inverseDeps);
                    }
                    int index = depRoots.indexOf(root);
                    depRoots.addAll(index+1, getSrcRootPeers(peers, root));
                    break;
            }
        } catch (TopologicalSortException ex) {
            JavaIndex.LOG.log(Level.WARNING, "Cycle in the source root dependencies detected: {0}", ex.unsortableSets()); //NOI18N
            List part = ex.partialSort();
            part.retainAll(depRoots);
            depRoots = part;
        } catch (URISyntaxException urise) {
            depRoots = Collections.singletonList(root);
        }


        final Queue<ElementHandle<TypeElement>> queue = new LinkedList<ElementHandle<TypeElement>>(classes);
        final Map<URL, Set<ElementHandle<TypeElement>>> bases = new HashMap<URL, Set<ElementHandle<TypeElement>>>();
        for (URL depRoot : depRoots) {
            final ClassIndexImpl ciImpl = ClassIndexManager.getDefault().getUsagesQuery(depRoot, true);
            if (ciImpl != null) {
                final ClassIndex index = ClasspathInfo.create(EMPTY, EMPTY, ClassPathSupport.createClassPath(depRoot)).getClassIndex();
                final Collection<Map<URL,List<URL>>> depMaps = new ArrayList<Map<URL,List<URL>>>(2);
                if (sourceDeps != null) {
                    depMaps.add(sourceDeps);
                }
                depMaps.add(peers);
                for (Map<URL,List<URL>> depMap : depMaps) {
                    final List<URL> dep =  depMap.get(depRoot);
                    if (dep != null) {
                        for (URL url : dep) {
                            final Set<ElementHandle<TypeElement>> b = bases.get(url);
                            if (b != null)
                                queue.addAll(b);
                        }
                    }
                }

                final Set<ElementHandle<TypeElement>> toHandle = new HashSet<ElementHandle<TypeElement>>();
                while (!queue.isEmpty()) {
                    final ElementHandle<TypeElement> e = queue.poll();
                    if (toHandle.add(e))
                        queue.addAll(index.getElements(e, EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                }
                bases.put(depRoot, toHandle);

                if (!includeCurrentSourceRoot && depRoot.equals(root)) {
                    continue;
                }

                final Set<FileObject> files = new HashSet<FileObject>();
                for (ElementHandle<TypeElement> e : toHandle)
                    files.addAll(index.getResources(e, EnumSet.complementOf(EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS)), EnumSet.of(ClassIndex.SearchScope.SOURCE)));

                final Set<URL> urls = new HashSet<URL>();
                for (FileObject file : files)
                    urls.add(file.toURL());

                if (includeFilesInError) {
                    final Collection<? extends URL> errUrls = ErrorsCache.getAllFilesInError(depRoot);
                    if (!errUrls.isEmpty()) {
                        //new type creation may cause/fix some errors
                        //not 100% correct (consider eg. a file that has two .* imports
                        //new file creation may cause new error in this case
                        urls.addAll(errUrls);
                    }
                }

                if (!urls.isEmpty())
                    ret.put(depRoot, urls);
            }
        }
        return ret;
    }

    private static void cleanUpResources (
            @NonNull final Context ctx,
            @NonNull final FileManagerTransaction fmTx) throws IOException {
        final File classFolder = JavaIndex.getClassFolder(ctx);
        final File resourcesFile = new File (classFolder,FileObjects.RESOURCES);
        try {
            for (String fileName : readRSFile(resourcesFile)) {
                File f = new File (classFolder, fileName);
                fmTx.delete(f);
            }
            fmTx.delete(resourcesFile);
        } catch (IOException ioe) {
            //Nothing to delete - pass
        }
    }
    
    private static boolean isAptBuildGeneratedFolder(
            @NonNull final URL root,
            @NonNull final ClassPath srcPath) {
        Parameters.notNull("root", root);       //NOI18N
        Parameters.notNull("srcPath", srcPath); //NOI18N
        for (FileObject srcRoot : srcPath.getRoots()) {
            if (root.equals(AnnotationProcessingQuery.getAnnotationProcessingOptions(srcRoot).sourceOutputDirectory())) {
               return true;
            }
        }
        return false;
    }

    public static class Factory extends CustomIndexerFactory {

        private static AtomicBoolean javaTaskFactoriesInitialized = new AtomicBoolean(false);

        public Factory() {
            if (!javaTaskFactoriesInitialized.getAndSet(true)) {
                JavaSourceTaskFactoryManager.register();
            }
        }

        @Override
        public boolean scanStarted(final Context context) {
            JavaIndex.LOG.log(Level.FINE, "scan started for root ({0})", context.getRootURI()); //NOI18N
            final TransactionContext txctx = TransactionContext.beginStandardTransaction(
                    context.getRootURI(),
                    true,
                    context::isAllFilesIndexing,
                    context.checkForEditorModifications());
            try {
                return JavaIndexerWorker.reduce(
                    Boolean.TRUE,
                    JavaIndexerWorker.Bool.AND,
                    new IndexCheck(context, txctx.get(ClassIndexEventsTransaction.class)),
                    new MetadataCheck(context));
            } catch (final ExecutionException ee) {
                JavaIndex.LOG.log(
                    Level.WARNING,
                    "Exception while checking cache validity for root: "+context.getRootURI(),//NOI18N
                    ee.getCause());
                return false;
            } catch (InterruptedException ie) {
                //Ending
                return false;
            }
        }

        @Override
        public void scanFinished(final Context context) {            
            final TransactionContext txCtx = TransactionContext.get();
            assert txCtx != null;
            try {
                if (context.isCancelled()) {
                    txCtx.rollBack();
                } else {
                    txCtx.commit();
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        
        @Override
        public CustomIndexer createIndexer() {
            return new JavaCustomIndexer();
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            JavaIndex.LOG.log(Level.FINE, "filesDeleted({0})", deleted); //NOI18N
            try {
                if(!Boolean.parseBoolean(JavaIndex.getAttribute(context.getRootURI(), APT_SOURCE_OUTPUT, null))) {
                    clearFiles(context, deleted);
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        @Override
        public void rootsRemoved(final Iterable<? extends URL> removedRoots) {
            assert removedRoots != null;
            JavaIndex.LOG.log(Level.FINE, "roots removed: {0}", removedRoots);
            final TransactionContext txCtx = TransactionContext.beginTrans().
                    register(ClassIndexEventsTransaction.class, ClassIndexEventsTransaction.create(true, ()->false));
            try {
                APTUtils.sourceRootUnregistered(removedRoots);
                final ClassIndexManager cim = ClassIndexManager.getDefault();
                final JavaFileFilterListener ffl = JavaFileFilterListener.getDefault();
                try {
                    final Set<URL> toRefresh = new HashSet<URL>();
                    for (URL removedRoot : removedRoots) {
                        if (Module.isClosed()) {
                            return;
                        }
                        cim.removeRoot(removedRoot);
                        ffl.stopListeningOn(removedRoot);
                        final FileObject root = URLMapper.findFileObject(removedRoot);
                        if (root == null) {
                            JavaIndex.setAttribute(removedRoot, ClassIndexManager.PROP_DIRTY_ROOT, Boolean.TRUE.toString());
                        } else {
                            ensureSourcePath(root);
                        }
                    }
                    for (URL removedRoot : removedRoots) {
                        toRefresh.remove(removedRoot);
                    }                    
                    for (URL url : toRefresh) {
                        IndexingManager.getDefault().refreshIndex(url, null, true);
                    }
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            } finally {
                try {
                    if (Module.isClosed()) {
                        txCtx.rollBack();
                    } else {
                        txCtx.commit();
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
            JavaIndex.LOG.log(Level.FINE, "filesDirty({0})", dirty); //NOI18N
            markDirtyFiles(context, dirty);
        }

        @Override
        @NonNull
        public String getIndexerName() {
            return JavaIndex.NAME;
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return true;
        }

        @Override
        public int getIndexVersion() {
            return JavaIndex.VERSION;
        }

        /**
         * Equals for JavaCustomIndexer.Factory.
         * Some Java Source tests register the JavaCustomIndexer.Factory twice im mime lookup.
         * The implementation of equals and hashCode removes such duplicity causing
         * the TransactionContext to fail.
         * @param obj
         * @return true if the Factory creates the same indexer (the same name and the same version).
         */
        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof JavaCustomIndexer.Factory)) {
                return false;
            }
            final Factory of = (Factory) obj;
            return of.getIndexerName().equals(getIndexerName()) && of.getIndexVersion() == getIndexVersion();
        }

        @Override
        public int hashCode() {
            return getIndexerName().hashCode();
        }

        @MimeRegistration(mimeType="text/x-java", service=CustomIndexerFactory.class)
        public static Factory register() {
            return NoJavacHelper.hasWorkingJavac() ? new Factory() : null;
        }
    }

    public static final class CompileTuple {
        public final PrefetchableJavaFileObject jfo;
        public final Indexable indexable;
        public final boolean virtual;
        public final boolean index;
        public final boolean aptGenerated;

        public CompileTuple (final PrefetchableJavaFileObject jfo, final Indexable indexable,
                final boolean virtual, final boolean index) {
            this(jfo, indexable, virtual, index, false);
        }

        public CompileTuple (final PrefetchableJavaFileObject jfo, final Indexable indexable,
                final boolean virtual, final boolean index, final boolean aptGenerated) {
            this.jfo = jfo;
            this.indexable = indexable;
            this.virtual = virtual;
            this.index = index;
            this.aptGenerated = aptGenerated;
        }

        public CompileTuple (final PrefetchableJavaFileObject jfo, final Indexable indexable) {
            this(jfo,indexable,false, true);
        }
    }

    private static final Convertor<Diagnostic<?>> ERROR_CONVERTOR = new ErrorConvertorImpl(ErrorKind.ERROR);
    private static final Convertor<Diagnostic<?>> ERROR_CONVERTOR_NO_BADGE = new ErrorConvertorImpl(ErrorKind.ERROR_NO_BADGE);
    
    private static final class ErrorConvertorImpl implements Convertor<Diagnostic<?>> {
        private final ErrorKind errorKind;
        public ErrorConvertorImpl(ErrorKind errorKind) {
            this.errorKind = errorKind;
        }
        @Override
        public ErrorKind getKind(Diagnostic<?> t) {
            return t.getKind() == Kind.ERROR ? errorKind : ErrorKind.WARNING;
        }
        @Override
        public int getLineNumber(Diagnostic<?> t) {
            return (int) t.getLineNumber();
        }
        @Override
        public String getMessage(Diagnostic<?> t) {
            return t.getMessage(null);
        }
    }

    private static List<? extends URL> getSrcRootPeers(final Map<URL,List<URL>> root2Peers, final URL rootURL) {        
        List<URL> result = root2Peers.get(rootURL);
        if (result == null) {
            result = Collections.<URL>emptyList();
        }
        JavaIndex.LOG.log(Level.FINE,"Peer source roots for root {0} -> {1}",
            new Object[] {
                rootURL,
                result
            });
        return result;
    }

    private static boolean ensureSourcePath(final @NonNull FileObject root) throws IOException {
        final ClassPath srcPath = ClassPath.getClassPath(root, ClassPath.SOURCE);
        String srcPathStr;
        if (srcPath != null) {
            final StringBuilder sb = new StringBuilder();
            for (ClassPath.Entry entry : srcPath.entries()) {
                sb.append(entry.getURL()).append(' ');  //NOI18N
            }
            srcPathStr = sb.toString();
        } else {
            srcPathStr = "";    //NOI18N
        }
        return JavaIndex.ensureAttributeValue(root.toURL(), SOURCE_PATH, srcPathStr);
    }

    private static final Set<String> JDK7AndLaterWarnings = new HashSet<String>(Arrays.asList(
            "compiler.warn.diamond.redundant.args", 
            "compiler.warn.diamond.redundant.args.1",
            "compiler.note.potential.lambda.found"));

    @CheckForNull
    private static File dumpHeap(@NonNull final String path) {
        try {
            if (heapDumper == null) {
                final Class<?> clz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");   //NOI18N
                final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                final Object bean = ManagementFactory.newPlatformMXBeanProxy(
                    server,
                    "com.sun.management:type=HotSpotDiagnostic",    //NOI18N
                    clz);
                final Method m = clz.getDeclaredMethod("dumpHeap", String.class, Boolean.TYPE);
                heapDumper = Pair.of(bean,m);
            }
            final File folder = new File(path);
            final File[] children = folder.listFiles();
            if (children != null) {
                final String namePattern = "heapdump_"; //NOI18N
                final Set<String> names = Arrays.stream(children)
                        .map((f) -> f.getName())
                        .filter((n) -> n.startsWith(namePattern))
                        .collect(Collectors.toSet());
                int index = 1;
                while (true) {
                    if (!names.contains(namePattern + index)) {
                        break;
                    }
                    index++;
                }
                final File file = new File(folder, namePattern + index);
                heapDumper.second().invoke(heapDumper.first(), file.getAbsolutePath(), true);
                return file;
            }
        } catch (Exception e) {
            //pass
        }
        return null;
    }

    private static Pair<Object,Method> heapDumper;

    private static String computeJavacVersion() {
        return SourceVersion.latest().toString();
    }

    private static class FilterOutJDK7AndLaterWarnings implements Comparable<Diagnostic<? extends JavaFileObject>> {
        @Override public int compareTo(Diagnostic<? extends JavaFileObject> o) {
            return JDK7AndLaterWarnings.contains(o.getCode()) ? 0 : -1;
        }
    }
    
    private static final class AptGeneratedIndexable implements IndexableImpl {
        
        private final InferableJavaFileObject jfo;
        
        AptGeneratedIndexable(@NonNull final InferableJavaFileObject jfo) {
            this.jfo = jfo;
        }

        @Override
        public String getRelativePath() {
            final StringBuilder sb = new StringBuilder(FileObjects.convertPackage2Folder(jfo.inferBinaryName(), '/'));  //NOI18N
            sb.append('.'); //NOI18N
            sb.append(FileObjects.getExtension(jfo.toUri().getPath()));
            return sb.toString();
        }

        @Override
        public URL getURL() {
            try {
                return jfo.toUri().toURL();
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public String getMimeType() {
            return JavacParser.MIME_TYPE;
        }

        @Override
        public boolean isTypeOf(String mimeType) {
            return JavacParser.MIME_TYPE.equals(mimeType);
        }
    }
    
    private abstract static class Check implements Callable<Boolean> {
        
        protected final Context ctx;
        
        protected Check(@NonNull final Context ctx) {
            Parameters.notNull("ctx", ctx); //NOI18N
            this.ctx = ctx;
        }        
    }

    private static final class IndexCheck extends Check {

        private final ClassIndexEventsTransaction cietx;
        
        IndexCheck(
            @NonNull final Context ctx,
            @NonNull final ClassIndexEventsTransaction cietx) {
            super(ctx);
            Parameters.notNull("cietx", cietx); //NOI18N
            this.cietx = cietx;
        }

        @NonNull
        @Override
        public Boolean call() throws Exception {
            boolean vote = true;
            final ClassIndexImpl uq = ClassIndexManager.getDefault().createUsagesQuery(
                ctx.getRootURI(),
                true,
                cietx);
            final boolean classIndexConsistent = uq != null ?
                uq.getState() != ClassIndexImpl.State.NEW ?
                    true: //Already checked
                    uq.isValid():
                true;

            if (!classIndexConsistent) {
                vote = false;
            }            
            return vote;            
        }

    }

    private static final class MetadataCheck extends Check {
        MetadataCheck(@NonNull final Context ctx) {
            super(ctx);
        }

        @NonNull
        @Override
        public Boolean call() throws Exception {
            boolean vote = true;
            FileObject root = ctx.getRoot();
            if (root == null) {
                return vote;
            }
            if (Optional.ofNullable(APTUtils.get(root))
                    .map((apt) -> apt.verifyAttributes(ctx.getRoot(), false))
                    .orElse(Boolean.FALSE)) {
                vote = false;
            }
            if (ensureSourcePath(root)) {
                JavaIndex.LOG.fine("forcing reindex due to source path change"); //NOI18N
                vote = false;
            }
            if (JavaIndex.ensureAttributeValue(ctx.getRootURI(), ClassIndexManager.PROP_DIRTY_ROOT, null)) {
                JavaIndex.LOG.fine("forcing reindex due to dirty root"); //NOI18N
                vote = false;
            }
            if (JavaIndex.ensureAttributeValue(ctx.getRootURI(), PROP_JAVAC_VERSION, computeJavacVersion())) {
                JavaIndex.LOG.fine("forcing reindex due to nb-javac status change"); //NOI18N
                vote = false;
            }
            if (!JavaFileFilterListener.getDefault().startListeningOn(ctx.getRoot())) {
                JavaIndex.LOG.fine("Forcing reindex due to changed JavaFileFilter"); // NOI18N
                vote = false;
            }
            return vote;
        }
    }
    
    public static interface CompileWorkerProvider {
        public CompileWorker[] getWorkers(List<CompileTuple> toCompile);
    }

    @ServiceProvider(service=CompileWorkerProvider.class, position=1000)
    public static class DefaultCompileWorkerProvider implements CompileWorkerProvider {

        @Override
        public CompileWorker[] getWorkers(List<CompileTuple> toCompile) {
            return new CompileWorker[] {
                new VanillaCompileWorker()
            };
        }
        
    }
}
