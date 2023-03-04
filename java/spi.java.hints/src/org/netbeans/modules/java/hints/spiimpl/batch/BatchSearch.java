/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.hints.spiimpl.batch;

import org.netbeans.spi.java.hints.HintContext.MessageKind;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.java.hints.spiimpl.MessageImpl;
import org.netbeans.modules.java.hints.spiimpl.Utilities;
import org.netbeans.modules.java.hints.spiimpl.hints.HintsInvoker;
import org.netbeans.modules.java.hints.spiimpl.pm.BulkSearch;
import org.netbeans.modules.java.hints.spiimpl.pm.BulkSearch.BulkPattern;
import org.netbeans.modules.java.hints.providers.spi.HintDescription.AdditionalQueryConstraints;
import org.netbeans.modules.java.hints.providers.spi.Trigger.PatternDescription;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.api.java.source.matching.Matcher;
import org.netbeans.api.java.source.matching.Pattern;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class BatchSearch {

    private static final Logger LOG = Logger.getLogger(BatchSearch.class.getName());

    public static BatchResult findOccurrences(Iterable<? extends HintDescription> patterns, Scope scope) {
        return findOccurrences(patterns, scope, new ProgressHandleWrapper(null), HintsSettings.getGlobalSettings());
    }

    public static BatchResult findOccurrences(final Iterable<? extends HintDescription> patterns, final Scope scope, final ProgressHandleWrapper progress, @NullAllowed HintsSettings settingsProvider) {
        return findOccurrencesLocal(patterns, scope.getIndexMapper(patterns), scope.getTodo(), progress, settingsProvider);
    }

    private static BatchResult findOccurrencesLocal(final Iterable<? extends HintDescription> patterns, final MapIndices indexMapper, final Collection<? extends Folder> todo, final ProgressHandleWrapper progress, final @NullAllowed HintsSettings settingsProvider) {
        final BatchResult[] result = new BatchResult[1];

        try {
            JavaSource.create(Utilities.createUniversalCPInfo()).runUserActionTask((CompilationController parameter) -> {
                result[0] = findOccurrencesLocalImpl(parameter, patterns, indexMapper, todo, progress, settingsProvider);
            }, true);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

        return result[0];
    }
    
    private static BatchResult findOccurrencesLocalImpl(final CompilationInfo info, final Iterable<? extends HintDescription> patterns, MapIndices indexMapper, Collection<? extends Folder> todo, ProgressHandleWrapper progress, HintsSettings settingsProvider) {
        boolean hasKindPatterns = false;

        for (HintDescription pattern : patterns) {
            if (!(pattern.getTrigger() instanceof PatternDescription)) {
                hasKindPatterns = true;
                break;
            }
        }

        final Callable<BulkPattern> bulkPattern = hasKindPatterns ? null : new Callable<BulkPattern>() {
            private final AtomicReference<BulkPattern> pattern = new AtomicReference<>();
            @Override
            public BulkPattern call() {
                if (pattern.get() == null) {
                    pattern.set(preparePattern(patterns, info));
                }

                return pattern.get();
            }
        };
        final Map<IndexEnquirer, Collection<? extends Resource>> result = new HashMap<>();
        final Collection<MessageImpl> problems = new LinkedList<>();
        ProgressHandleWrapper innerForAll = progress.startNextPartWithEmbedding(ProgressHandleWrapper.prepareParts(2 * todo.size()));
        
        for (final Folder src : todo) {
            LOG.log(Level.FINE, "Processing: {0}", FileUtil.getFileDisplayName(src.getFileObject()));
            
            IndexEnquirer indexEnquirer;// = indexMapper.findIndex(src.getFileObject(), innerForAll, src.isRecursive());

//            if (indexEnquirer == null) {
                indexEnquirer = new FileSystemBasedIndexEnquirer(src.getFileObject(), src.isRecursive());
//            }

            Collection<? extends Resource> occurrences = indexEnquirer.findResources(patterns, innerForAll, bulkPattern, problems, settingsProvider);

            if (!occurrences.isEmpty()) {
                result.put(indexEnquirer, occurrences);
            }

            innerForAll.tick();
        }

        return new BatchResult(result, problems);
    }

    private static BulkPattern preparePattern(final Iterable<? extends HintDescription> patterns, CompilationInfo info) {
        Collection<String> code = new LinkedList<>();
        Collection<Tree> trees = new LinkedList<>();
        Collection<AdditionalQueryConstraints> additionalConstraints = new LinkedList<>();

        for (HintDescription pattern : patterns) {
            String textPattern = ((PatternDescription) pattern.getTrigger()).getPattern();

            code.add(textPattern);
            trees.add(Utilities.parseAndAttribute(info, textPattern, null));
            additionalConstraints.add(pattern.getAdditionalConstraints());
        }

        return BulkSearch.getDefault().create(code, trees, additionalConstraints, new AtomicBoolean());
    }

    public static void getVerifiedSpans(BatchResult candidates, @NonNull ProgressHandleWrapper progress, final VerifiedSpansCallBack callback, final Collection<? super MessageImpl> problems, AtomicBoolean cancel) {
        getVerifiedSpans(candidates, progress, callback, false, problems, cancel);
    }

    public static void getVerifiedSpans(BatchResult candidates, @NonNull ProgressHandleWrapper progress, final VerifiedSpansCallBack callback, boolean doNotRegisterClassPath, final Collection<? super MessageImpl> problems, AtomicBoolean cancel) {
        int[] parts = new int[candidates.projectId2Resources.size()];
        int   index = 0;

        for (Entry<? extends IndexEnquirer, ? extends Collection<? extends Resource>> e : candidates.projectId2Resources.entrySet()) {
            parts[index++] = e.getValue().size();
        }

        ProgressHandleWrapper inner = progress.startNextPartWithEmbedding(parts);

        for (Entry<? extends IndexEnquirer, ? extends Collection<? extends Resource>> e : candidates.projectId2Resources.entrySet()) {
            if (cancel.get()) 
                return;
            inner.startNextPart(e.getValue().size());

            e.getKey().validateResource(e.getValue(), inner, callback, doNotRegisterClassPath, problems, cancel);
        }
    }

    private static void getLocalVerifiedSpans(Collection<? extends Resource> resources, @NonNull final ProgressHandleWrapper progress, final VerifiedSpansCallBack callback, boolean doNotRegisterClassPath, final Collection<? super MessageImpl> problems, final AtomicBoolean cancel) {
        Collection<FileObject> files = new LinkedList<>();
        final Map<FileObject, Resource> file2Resource = new HashMap<>();

        for (Resource r : resources) {
            FileObject file = r.getResolvedFile();

            if (file != null) {
                files.add(file);
                file2Resource.put(file, r);
            } else {
                callback.cannotVerifySpan(r);
                progress.tick();
            }
        }

        Map<ClasspathInfo, Collection<FileObject>> cp2Files = BatchUtilities.sortFiles(files);
        ClassPath[] toRegister = null;

        if (!doNotRegisterClassPath) {
            Set<ClassPath> toRegisterSet = new HashSet<>();

            for (ClasspathInfo cpInfo : cp2Files.keySet()) {
                toRegisterSet.add(cpInfo.getClassPath(PathKind.SOURCE));
            }

            toRegister = !toRegisterSet.isEmpty() ? toRegisterSet.toArray(new ClassPath[0]) : null;

            if (toRegister != null) {
                GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, toRegister);
                try {
                    Utilities.waitScanFinished();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        try {
            for (Entry<ClasspathInfo, Collection<FileObject>> e : cp2Files.entrySet()) {
                try {
                    List<FileObject> toProcess = new ArrayList<>(e.getValue());
                    final AtomicInteger currentPointer = new AtomicInteger();
                    callback.groupStarted();

//                    for (FileObject f : toProcess) {
                    while (currentPointer.get() < toProcess.size()) {
                        if (cancel.get())
                            return;
                        final AtomicBoolean stop = new AtomicBoolean();
                        List<FileObject> currentInputList = toProcess.subList(currentPointer.get(), toProcess.size());
//                        JavaSource js = JavaSource.create(e.getKey(), f);
                        JavaSource js = JavaSource.create(e.getKey(), currentInputList);

                        js.runUserActionTask(new Task<CompilationController>() {
                            @Override
                            public void run(CompilationController parameter) throws Exception {
                                if (stop.get()) return;
                                if (cancel.get()) return;

                                boolean cont = true;

                                try {
                                    if (parameter.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                                        if (currentInputList.size() == 1) {
                                            //the javac crashed while processing the (single) file, we must ensure progress, otherwise infinite loop in processing would happen:
                                            problems.add(new MessageImpl(MessageKind.WARNING, "An error occurred while processing file: " + FileUtil.getFileDisplayName(parameter.getFileObject()) + ", please see the IDE log for more information."));
                                            currentPointer.incrementAndGet();
                                        }

                                        return ;
                                    }

                                    progress.setMessage("processing: " + FileUtil.getFileDisplayName(parameter.getFileObject()));
                                    Resource r = file2Resource.get(parameter.getFileObject());

                                    HintsSettings settings = r.settings;
                                    Iterable<? extends HintDescription> enabledHints;
                                    
                                    if (settings == null) {
                                        settings = HintsSettings.getSettingsFor(parameter.getFileObject());
                                        List<HintDescription> hintsCopy = new ArrayList<>();
                                        for (HintDescription hd : r.hints) {
                                            if (settings.isEnabled(hd.getMetadata())) {
                                                hintsCopy.add(hd);
                                            }
                                        }
                                        enabledHints = hintsCopy;
                                    } else {
                                        enabledHints = r.hints;
                                    }
                                    
                                    List<ErrorDescription> hints = new HintsInvoker(settings, true, new AtomicBoolean()).computeHints(parameter, enabledHints, problems);

                                    assert hints != null;
                                    
                                    cont = callback.spansVerified(parameter, r, hints);
                                } catch (ThreadDeath td) {
                                    throw td;
                                } catch (Throwable t) {
                                    LOG.log(Level.INFO, "Exception while performing batch processing in " + FileUtil.getFileDisplayName(parameter.getFileObject()), t);
                                    problems.add(new MessageImpl(MessageKind.WARNING, "An exception occurred while processing file: " + FileUtil.getFileDisplayName(parameter.getFileObject()) + " (" + t.getLocalizedMessage() + ")."));
                                }
                                
                                if (cont) {
                                    progress.tick();
                                    currentPointer.incrementAndGet();
                                } else {
                                    stop.set(true);
                                }
                            }
                        }, true);
                    }

                    callback.groupFinished();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } finally {
            if (toRegister != null) {
                GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, toRegister);
            }
            progress.finish();
        }
    }

    public interface VerifiedSpansCallBack {
        public void groupStarted();
        public boolean spansVerified(CompilationController wc, Resource r, Collection<? extends ErrorDescription> hints) throws Exception;
        public void groupFinished();
        public void cannotVerifySpan(Resource r);
    }

    
    public static class Folder {

        private FileObject file;
        private NonRecursiveFolder folder;
        
        public Folder(FileObject file) {
            this.file = file;
        }
        
        public Folder(NonRecursiveFolder folder) {
            this.folder = folder;
        }
        
        public FileObject getFileObject() {
            if (file!=null) {
                return file;
            }
            return folder.getFolder();
            
        }
        
        private boolean isRecursive() {
            if (file!=null) {
                return file.isFolder();
            }
            return false;
        }

        public static Folder[] convert(FileObject... files) {
            Folder[] result = new Folder[files.length];
            for (int i=0;i<files.length;i++) {
                result[i] = new Folder(files[i]);
            }
            return result;
        }

        public static Folder[] convert(Collection<?> list) {
            Folder[] result = new Folder[list.size()];
            int i=0;
            for (Object item:list) {
                if (item instanceof FileObject)
                    result[i] = new Folder((FileObject) item);
                else 
                    result[i] = new Folder((NonRecursiveFolder) item);
                i++;
            }
            return result;
        }

        @Override
        public String toString() {
            return !isRecursive()?"Non":"" + "Recursive file " + getFileObject().getPath();
        }
        
        
    }
    
    public abstract static class Scope {

        public abstract String getDisplayName();
        public abstract Collection<? extends Folder> getTodo();
        public abstract MapIndices getIndexMapper(Iterable<? extends HintDescription> hints);
        
    }
    
    public static final class BatchResult {
        
        private final Map<? extends IndexEnquirer, ? extends Collection<? extends Resource>> projectId2Resources;
        public final Collection<? extends MessageImpl> problems;
        
        public BatchResult(Map<? extends IndexEnquirer, ? extends Collection<? extends Resource>> projectId2Resources, Collection<? extends MessageImpl> problems) {
            this.projectId2Resources = projectId2Resources;
            this.problems = problems;
        }

        public Collection<? extends Collection<? extends Resource>> getResources() {
            return projectId2Resources.values();
        }

        public Map<FileObject, Collection<? extends Resource>> getResourcesWithRoots() {
            Map<FileObject, Collection<? extends Resource>> result = new HashMap<>();

            for (Entry<? extends IndexEnquirer, ? extends Collection<? extends Resource>> e : projectId2Resources.entrySet()) {
                result.put(e.getKey().src, e.getValue());
            }

            return result;
        }
    }

    public static final class Resource {
        private final IndexEnquirer indexEnquirer;
        private final String relativePath;
        final Iterable<? extends HintDescription> hints;
        private final BulkPattern pattern;
        final HintsSettings settings;

        public Resource(IndexEnquirer indexEnquirer, String relativePath, Iterable<? extends HintDescription> hints, BulkPattern pattern, HintsSettings settings) {
            this.indexEnquirer = indexEnquirer;
            this.relativePath = relativePath;
            this.hints = hints;
            this.pattern = pattern;
            this.settings = settings;
        }

        public String getRelativePath() {
            return relativePath;
        }
        
        public Iterable<int[]> getCandidateSpans() {
            FileObject file = getResolvedFile();
            JavaSource js;

            if (file != null) {
                js = JavaSource.forFileObject(file);
            } else {
                CharSequence text = getSourceCode();

                if (text == null) {
                    return null;
                }

                Writer out = null;

                try {
                    file = FileUtil.createData(FileUtil.createMemoryFileSystem().getRoot(), relativePath);
                    out = new OutputStreamWriter(file.getOutputStream());

                    out.write(text.toString());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    return null;
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }

                js = JavaSource.create(Utilities.createUniversalCPInfo(), file);
            }

            final List<int[]> span = new LinkedList<>();

            try {
                js.runUserActionTask((CompilationController cc) -> {
                    cc.toPhase(Phase.PARSED);
                    
                    span.addAll(doComputeSpans(cc));
                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            return span;
        }

        private Collection<int[]> doComputeSpans(CompilationInfo ci) {
            Collection<int[]> result = new LinkedList<>();
            Map<String, Collection<TreePath>> found = BulkSearch.getDefault().match(ci, new AtomicBoolean(), new TreePath(ci.getCompilationUnit()), pattern);
            
            for (Entry<String, Collection<TreePath>> e : found.entrySet()) {
                Tree treePattern = Utilities.parseAndAttribute(ci, e.getKey(), null);
                
                for (TreePath tp : e.getValue()) {
                    //XXX: this pass will not be performed on the web!!!
                    if (   BulkSearch.getDefault().requiresLightweightVerification()
                        && !Matcher.create(ci).setCancel(new AtomicBoolean()).setSearchRoot(tp).setTreeTopSearch().setUntypedMatching().match(Pattern.createSimplePattern(new TreePath(new TreePath(ci.getCompilationUnit()), treePattern))).iterator().hasNext()) {
                        continue;
                    }
                    int[] span = new int[] {
                        (int) ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), tp.getLeaf()),
                        (int) ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), tp.getLeaf())
                    };

                    result.add(span);
                }
            }

            return result;
        }
        
        public FileObject getResolvedFile() {
            return indexEnquirer.src.getFileObject(relativePath);
        }

        public String getDisplayName() {
            FileObject file = getResolvedFile();

            if (file != null) {
                return FileUtil.getFileDisplayName(file);
            } else {
                return relativePath; //TODO:+container
            }
        }
        
        public CharSequence getSourceCode() {
            try {
                FileObject file = getResolvedFile();
                ByteBuffer bb = ByteBuffer.wrap(file.asBytes());

                return FileEncodingQuery.getEncoding(file).decode(bb);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }

        public FileObject getRoot() {
            return indexEnquirer.src;
        }
    }

    public static interface MapIndices {
        public IndexEnquirer findIndex(FileObject root, ProgressHandleWrapper progress, boolean recursive);
    }

    public abstract static class IndexEnquirer {
        final FileObject src;
        public IndexEnquirer(FileObject src) {
            this.src = src;
        }
        public abstract Collection<? extends Resource> findResources(Iterable<? extends HintDescription> hints, ProgressHandleWrapper progress, @NullAllowed Callable<BulkPattern> bulkPattern, Collection<? super MessageImpl> problems, HintsSettings settingsProvider);
        public abstract void validateResource(Collection<? extends Resource> resources, ProgressHandleWrapper progress, VerifiedSpansCallBack callback, boolean doNotRegisterClassPath, Collection<? super MessageImpl> problems, AtomicBoolean cancel);
//        public int[] getEstimatedSpan(Resource r);
    }

    public abstract static class LocalIndexEnquirer extends IndexEnquirer {
        public LocalIndexEnquirer(FileObject src) {
            super(src);
        }
        @Override
        public void validateResource(Collection<? extends Resource> resources, ProgressHandleWrapper progress, VerifiedSpansCallBack callback, boolean doNotRegisterClassPath, Collection<? super MessageImpl> problems, AtomicBoolean cancel) {
            getLocalVerifiedSpans(resources, progress, callback, doNotRegisterClassPath, problems, cancel);
        }
    }

    public static final class FileSystemBasedIndexEnquirer extends LocalIndexEnquirer {
        private boolean recursive;
        public FileSystemBasedIndexEnquirer(FileObject src, boolean recursive) {
            super(src);
            this.recursive = recursive;
        }
        @Override
        public Collection<? extends Resource> findResources(final Iterable<? extends HintDescription> hints, ProgressHandleWrapper progress, final @NullAllowed Callable<BulkPattern> bulkPattern, final Collection<? super MessageImpl> problems, final HintsSettings settingsProvider) {
            Collection<FileObject> files = new LinkedList<>();

            final ProgressHandleWrapper innerProgress = progress.startNextPartWithEmbedding(30, 70);

            BatchUtilities.recursive(src, src, files, innerProgress, 0, null, null, recursive);

            LOG.log(Level.FINE, "files: {0}", files);

            innerProgress.startNextPart(files.size());

            final Collection<Resource> result = new ArrayList<>();

            if (!files.isEmpty()) {
                try {
                    if (bulkPattern != null) {
                        long start = System.currentTimeMillis();

                        JavaSource.create(Utilities.createUniversalCPInfo(), files).runUserActionTask((CompilationController cc) -> {
                            if (cc.toPhase(Phase.PARSED).compareTo(Phase.PARSED) <0) {
                                return ;
                            }
                            
                            try {
                                boolean matches = BulkSearch.getDefault().matches(cc, new AtomicBoolean(), new TreePath(cc.getCompilationUnit()), bulkPattern.call());
                                
                                if (matches) {
                                    result.add(new Resource(FileSystemBasedIndexEnquirer.this, FileUtil.getRelativePath(src, cc.getFileObject()), hints, bulkPattern.call(), settingsProvider));
                                }
                            } catch (ThreadDeath td) {
                                throw td;
                            } catch (Throwable t) {
                                LOG.log(Level.INFO, "Exception while performing batch search in " + FileUtil.getFileDisplayName(cc.getFileObject()), t);
                                problems.add(new MessageImpl(MessageKind.WARNING, "An exception occurred while testing file: " + FileUtil.getFileDisplayName(cc.getFileObject()) + " (" + t.getLocalizedMessage() + ")."));
                            }
                            
                            innerProgress.tick();
                        }, true);

                        long end = System.currentTimeMillis();

                        LOG.log(Level.FINE, "took: {0}, per file: {1}", new Object[]{end - start, (end - start) / files.size()});
                    } else {
                        for (FileObject file : files) {
                            result.add(new Resource(this, FileUtil.getRelativePath(src, file), hints, null, settingsProvider));
                        }
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            return result;
        }

    }

}
