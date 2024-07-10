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
package org.netbeans.modules.java.hints.spiimpl.batch;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath.PathConversionMode;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.queries.VisibilityQuery;
//import org.netbeans.modules.java.editor.semantic.SemanticHighlighter;
import org.netbeans.modules.java.hints.spiimpl.JavaFixImpl;
import org.netbeans.modules.java.hints.spiimpl.JavaFixImpl.Accessor;
import org.netbeans.modules.java.hints.spiimpl.MessageImpl;
import org.netbeans.modules.java.hints.spiimpl.SyntheticFix;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.BatchResult;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.Resource;
import org.netbeans.modules.java.hints.spiimpl.ipi.upgrade.ProjectDependencyUpgrader;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.parsing.CompilationInfoImpl;
import org.netbeans.modules.java.source.save.DiffUtilities;
import org.netbeans.modules.java.source.save.ElementOverlay;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.HintContext.MessageKind;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Lahoda
 */
public class BatchUtilities {

    private static final Logger LOG = Logger.getLogger(BatchUtilities.class.getName());
    
    public static Collection<ModificationResult> applyFixes(BatchResult candidates, @NonNull final ProgressHandleWrapper progress, AtomicBoolean cancel, final Collection<? super MessageImpl> problems) {
        return applyFixes(candidates, progress, cancel, new ArrayList<>(), problems);
    }
    
    public static Collection<ModificationResult> applyFixes(BatchResult candidates, @NonNull final ProgressHandleWrapper progress, AtomicBoolean cancel, final Collection<? super RefactoringElementImplementation> fileChanges, final Collection<? super MessageImpl> problems) {
        return applyFixes(candidates, progress, cancel, fileChanges, null, problems);
    }
    
    public static Collection<ModificationResult> applyFixes(BatchResult candidates, @NonNull final ProgressHandleWrapper progress, AtomicBoolean cancel, final Collection<? super RefactoringElementImplementation> fileChanges, final Map<JavaFix, ModificationResult> changesPerFix, final Collection<? super MessageImpl> problems) {
        return applyFixes(candidates, progress, cancel, fileChanges, changesPerFix, false, problems);
    }

    @SuppressWarnings("unchecked")
    public static Collection<ModificationResult> applyFixes(BatchResult candidates, @NonNull final ProgressHandleWrapper progress, AtomicBoolean cancel, final Collection<? super RefactoringElementImplementation> fileChanges, final Map<JavaFix, ModificationResult> changesPerFix, boolean doNotRegisterClassPath, final Collection<? super MessageImpl> problems) {
        final Map<Project, Set<String>> processedDependencyChanges = new IdentityHashMap<>();
        final Map<FileObject, List<ModificationResult.Difference>> result = new LinkedHashMap<>();
        final Map<FileObject, byte[]> resourceContentChanges = new HashMap<>();

        BatchSearch.VerifiedSpansCallBack callback = new BatchSearch.VerifiedSpansCallBack() {
            private ElementOverlay overlay;
            @Override
            public void groupStarted() {
                overlay = ElementOverlay.getOrCreateOverlay();
            }
            @Override
            public boolean spansVerified(CompilationController wc, Resource r, Collection<? extends ErrorDescription> hints) throws Exception {
                if (hints.isEmpty()) return true;
                
                Constructor<WorkingCopy> wcConstr = WorkingCopy.class.getDeclaredConstructor(CompilationInfoImpl.class, ElementOverlay.class);
                wcConstr.setAccessible(true);

//                final WorkingCopy copy = new WorkingCopy(JavaSourceAccessor.getINSTANCE().getCompilationInfoImpl(parameter), overlay);
                WorkingCopy copy = wcConstr.newInstance(JavaSourceAccessor.getINSTANCE().getCompilationInfoImpl(wc), overlay);
                Method setJavaSource = CompilationInfo.class.getDeclaredMethod("setJavaSource", JavaSource.class);
                setJavaSource.setAccessible(true);

//                copy.setJavaSource(JavaSource.this);
                setJavaSource.invoke(copy, wc.getJavaSource());

                copy.toPhase(Phase.RESOLVED);
                progress.tick();
                
                if (applyFixes(copy, processedDependencyChanges, hints, resourceContentChanges, fileChanges, changesPerFix, problems)) {
                    return false;
                }

                Method getChanges = WorkingCopy.class.getDeclaredMethod("getChanges", Map.class);
                getChanges.setAccessible(true);

                result.put(copy.getFileObject(), (List<ModificationResult.Difference>) getChanges.invoke(copy, new HashMap<Object, int[]>()));

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "fixes applied to: {0}", FileUtil.getFileDisplayName(wc.getFileObject()));
                }

                return true;
            }

            @Override
            public void groupFinished() {
                overlay = null;
            }

            @Override
            public void cannotVerifySpan(Resource r) {
                problems.add(new MessageImpl(MessageKind.WARNING, "Cannot parse: " + r.getRelativePath()));
            }
        };

        BatchSearch.getVerifiedSpans(candidates, progress, callback, doNotRegisterClassPath, problems, cancel);
        
        addResourceContentChanges(resourceContentChanges, result);

        return List.of(JavaSourceAccessor.getINSTANCE().createModificationResult(result, Map.of()));
    }

    public static void addResourceContentChanges(final Map<FileObject, byte[]> resourceContentChanges, final Map<FileObject, List<Difference>> result) {
        for (Entry<FileObject, byte[]> e : resourceContentChanges.entrySet()) {
            try {
                Charset encoding = FileEncodingQuery.getEncoding(e.getKey());
                final Document originalDocument = getDocument(e.getKey());
                final String[] origContent = new String[1];
                final Source[] s = new Source[1];
                if (originalDocument != null) {
                    originalDocument.render(() -> {
                        try {
                            origContent[0] = originalDocument.getText(0, originalDocument.getLength());
                            s[0] = Source.create(originalDocument);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    });
                }
                
                if (origContent[0] == null) {
                    byte[] origBytes = e.getKey().asBytes();
                    origContent[0] = encoding.newDecoder().decode(ByteBuffer.wrap(origBytes)).toString();
                    s[0] = Source.create(e.getKey());
                }
                String newContent  = encoding.newDecoder().decode(ByteBuffer.wrap(e.getValue())).toString();

                result.put(e.getKey(), DiffUtilities.diff2ModificationResultDifference(e.getKey(), null, Map.of(), origContent[0], newContent, s[0]));
            } catch (BadLocationException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    public static @CheckForNull Document getDocument(@NonNull FileObject file) {
        try {
            DataObject od = DataObject.find(file);
            EditorCookie ec = od.getLookup().lookup(EditorCookie.class);

            if (ec == null) return null;

            return ec.getDocument();
        } catch (DataObjectNotFoundException ex) {
            LOG.log(Level.FINE, null, ex);
            return null;
        }
    }

    private static String positionToString(ErrorDescription ed) {
        try {
            return ed.getFile().getNameExt() + ":" + ed.getRange().getBegin().getLine();
        } catch (IOException ex) {
            LOG.log(Level.FINE, null, ex);
            return ed.getFile().getNameExt();
        }
    }

//    public static void removeUnusedImports(Collection<? extends FileObject> files) throws IOException {
//        Map<ClasspathInfo, Collection<FileObject>> sortedFastFiles = sortFiles(files);
//
//        for (Entry<ClasspathInfo, Collection<FileObject>> e : sortedFastFiles.entrySet()) {
//            JavaSource.create(e.getKey(), e.getValue()).runModificationTask(new RemoveUnusedImports()).commit();
//        }
//    }
//
//    private static final class RemoveUnusedImports implements Task<WorkingCopy> {
//        public void run(WorkingCopy wc) throws IOException {
//            Document doc = wc.getSnapshot().getSource().getDocument(true);
//            
//            if (wc.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
//                return;
//            }
//
//            //compute imports to remove:
//            List<TreePathHandle> unusedImports = SemanticHighlighter.computeUnusedImports(wc);
//            CompilationUnitTree cut = wc.getCompilationUnit();
//            // make the changes to the source
//            for (TreePathHandle handle : unusedImports) {
//                TreePath path = handle.resolve(wc);
//                assert path != null;
//                cut = wc.getTreeMaker().removeCompUnitImport(cut,
//                        (ImportTree) path.getLeaf());
//            }
//
//            if (!unusedImports.isEmpty()) {
//                wc.rewrite(wc.getCompilationUnit(), cut);
//            }
//        }
//    }

    public static boolean applyFixes(WorkingCopy copy, Map<Project, Set<String>> processedDependencyChanges, Collection<? extends ErrorDescription> hints, Map<FileObject, byte[]> resourceContentChanges, Collection<? super RefactoringElementImplementation> fileChanges, Collection<? super MessageImpl> problems) throws IllegalStateException, Exception {
        return applyFixes(copy, processedDependencyChanges, hints, resourceContentChanges, fileChanges, null, problems);
    }
    
    @SuppressWarnings("unchecked")
    public static boolean applyFixes(WorkingCopy copy, Map<Project, Set<String>> processedDependencyChanges, Collection<? extends ErrorDescription> hints, Map<FileObject, byte[]> resourceContentChanges, Collection<? super RefactoringElementImplementation> fileChanges, Map<JavaFix, ModificationResult> changesPerFix, Collection<? super MessageImpl> problems) throws IllegalStateException, Exception {
        Set<JavaFix> fixes = new LinkedHashSet<>();
        for (ErrorDescription ed : hints) {
            if (!ed.getFixes().isComputed()) {
                throw new IllegalStateException();//TODO: should be problem
            }

            Fix toApply = null;

            for (Fix f : ed.getFixes().getFixes()) {
                if (f instanceof SyntheticFix) continue;
                if (toApply == null) toApply = f;
                else problems.add(new MessageImpl(MessageKind.WARNING, "More than one fix for: " + ed.getDescription() + " at " + positionToString(ed) + ", only the first one will be used."));
            }

            if (toApply == null) {
                //TODO: currently giving a warning so that the hints can be augmented with "Options.QUERY", but that should be removed
                //if a non-query hint cannot produce any fix, it is likely Ok - if not, the hint should produce a warning itself
                boolean doWarning = false;
                assert doWarning = true;
                if (doWarning) {
                    problems.add(new MessageImpl(MessageKind.WARNING, "No fix for: " + ed.getDescription() + " at " + positionToString(ed) + "."));
                }
                continue;
            }

            if (!(toApply instanceof JavaFixImpl)) {
                throw new IllegalStateException(toApply.getClass().getName());//XXX: hints need to provide JavaFixes
            }


            fixes.add(((JavaFixImpl) toApply).jf);
        }
        if (fixDependencies(copy.getFileObject(), fixes, processedDependencyChanges)) {
            return true;
        }
        for (JavaFix f : fixes) {
//                    if (cancel.get()) return ;

            JavaFixImpl.Accessor.INSTANCE.process(f, copy, false, resourceContentChanges, fileChanges);
            
            if (changesPerFix != null) {
                ElementOverlay overlay = ElementOverlay.getOrCreateOverlay(); //XXX: will use the incorrect overlay?
                Constructor<WorkingCopy> wcConstr = WorkingCopy.class.getDeclaredConstructor(CompilationInfoImpl.class, ElementOverlay.class);
                wcConstr.setAccessible(true);

//                final WorkingCopy copy = new WorkingCopy(JavaSourceAccessor.getINSTANCE().getCompilationInfoImpl(parameter), overlay);
                WorkingCopy perFixCopy = wcConstr.newInstance(JavaSourceAccessor.getINSTANCE().getCompilationInfoImpl(copy), overlay);
                Method setJavaSource = CompilationInfo.class.getDeclaredMethod("setJavaSource", JavaSource.class);
                setJavaSource.setAccessible(true);

//                copy.setJavaSource(JavaSource.this);
                setJavaSource.invoke(perFixCopy, copy.getJavaSource());

                perFixCopy.toPhase(Phase.RESOLVED);
                
                final Map<FileObject, byte[]> perFixResourceContentChanges = new HashMap<>();
        
                JavaFixImpl.Accessor.INSTANCE.process(f, perFixCopy, false, perFixResourceContentChanges, new ArrayList<>());
                
                Method getChanges = WorkingCopy.class.getDeclaredMethod("getChanges", Map.class);
                getChanges.setAccessible(true);
                
                Map<FileObject, List<Difference>> changes = new HashMap<>();
                
                changes.put(perFixCopy.getFileObject(), (List<ModificationResult.Difference>) getChanges.invoke(perFixCopy, new HashMap<Object, int[]>()));
                
                addResourceContentChanges(resourceContentChanges, changes);
                
                for (Iterator<Entry<FileObject, List<Difference>>> it = changes.entrySet().iterator(); it.hasNext();) {
                    if (it.next().getValue().isEmpty()) it.remove();
                }

                if (!changes.isEmpty()) {
                    ModificationResult perFixResult = JavaSourceAccessor.getINSTANCE().createModificationResult(changes, Map.of());
                    changesPerFix.put(f, perFixResult);
                }
            }
        }
        return false;
    }
    
    public static Collection<ModificationResult> applyFixes(final Map<FileObject, Collection<JavaFix>> toRun) {
        final Map<FileObject, List<ModificationResult.Difference>> result = new LinkedHashMap<>();
        final Map<FileObject, byte[]> resourceContentChanges = new HashMap<>();
        Map<ClasspathInfo, Collection<FileObject>> cp2Files = BatchUtilities.sortFiles(toRun.keySet());

        for (Entry<ClasspathInfo, Collection<FileObject>> e : cp2Files.entrySet()) {
            try {
                ModificationResult mr = JavaSource.create(e.getKey(), e.getValue()).runModificationTask((WorkingCopy parameter) -> {
                    if (parameter.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) return ;
                    
                    for (JavaFix jf : toRun.get(parameter.getFileObject())) {
                        JavaFixImpl.Accessor.INSTANCE.process(jf, parameter, false, resourceContentChanges, new ArrayList<>());
                    }
                });
                
                result.putAll(JavaSourceAccessor.getINSTANCE().getDiffsFromModificationResult(mr));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        addResourceContentChanges(resourceContentChanges, result);
        
        return List.of(JavaSourceAccessor.getINSTANCE().createModificationResult(result, Map.of()));
    }

    public static Collection<FileObject> getSourceGroups(Iterable<? extends Project> prjs) {
        List<FileObject> result = new LinkedList<>();
        
        for (Project p : prjs) {
            Sources s = ProjectUtils.getSources(p);

            for (SourceGroup sg : s.getSourceGroups("java")) {
                result.add(sg.getRootFolder());
            }
        }

        return result;
    }

    public static Map<ClasspathInfo, Collection<FileObject>> sortFiles(Collection<? extends FileObject> from) {
        Map<CPCategorizer, Collection<FileObject>> m = new HashMap<>();

        for (FileObject f : from) {
            CPCategorizer cpCategorizer = new CPCategorizer(f);
            m.computeIfAbsent(cpCategorizer, k -> new LinkedList<>())
             .add(f);
        }
        
        Map<ClasspathInfo, Collection<FileObject>> result = new IdentityHashMap<>();

        for (Entry<CPCategorizer, Collection<FileObject>> e : m.entrySet()) {
            ClasspathInfo cpInfo = new ClasspathInfo.Builder(e.getKey().boot)
                                                    .setClassPath(e.getKey().compile)
                                                    .setSourcePath(e.getKey().source)
                                                    .setModuleSourcePath(e.getKey().moduleSrcPath)
                                                    .setModuleBootPath(e.getKey().moduleBootPath)
                                                    .setModuleCompilePath(e.getKey().moduleCompilePath)
                                                    .setModuleClassPath(e.getKey().moduleClassPath)
                                                    .build();
            result.put(cpInfo, e.getValue());
        }
        
        return result;
    }
    
    private static final ClassPath getClassPath(FileObject forFO, String id) {
        ClassPath result = ClassPath.getClassPath(forFO, id);
        
        if (result == null) {
            if (ClassPath.BOOT.equals(id)) {
                result = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
            } else {
                result = ClassPath.EMPTY;
            }
        }
        
        return result;
    }
    
    private static final class CPCategorizer {
        private final String cps;
        private final ClassPath boot;
        private final ClassPath compile;
        private final ClassPath source;
        private final ClassPath moduleSrcPath;
        private final ClassPath moduleBootPath;
        private final ClassPath moduleCompilePath;
        private final ClassPath moduleClassPath;
        private final FileObject sourceRoot;

        public CPCategorizer(FileObject file) {
            this.boot = getClassPath(file, ClassPath.BOOT);
            this.compile = getClassPath(file, ClassPath.COMPILE);
            this.source = getClassPath(file, ClassPath.SOURCE);
            this.moduleSrcPath = getClassPath(file, JavaClassPathConstants.MODULE_SOURCE_PATH);
            this.moduleBootPath = getClassPath(file, JavaClassPathConstants.MODULE_BOOT_PATH);
            this.moduleCompilePath = getClassPath(file, JavaClassPathConstants.MODULE_COMPILE_PATH);
            this.moduleClassPath = getClassPath(file, JavaClassPathConstants.MODULE_CLASS_PATH);
            this.sourceRoot = source != null ? source.findOwnerRoot(file) : null;
            
            StringBuilder cps = new StringBuilder();
            
            if (boot != null) cps.append(boot.toString(PathConversionMode.PRINT));
            if (compile != null) cps.append(compile.toString(PathConversionMode.PRINT));
            if (source != null) cps.append(source.toString(PathConversionMode.PRINT));
            if (moduleSrcPath != null) cps.append(moduleSrcPath.toString(PathConversionMode.PRINT));
            if (moduleBootPath != null) cps.append(moduleBootPath.toString(PathConversionMode.PRINT));
            if (moduleCompilePath != null) cps.append(moduleCompilePath.toString(PathConversionMode.PRINT));
            if (moduleClassPath != null) cps.append(moduleClassPath.toString(PathConversionMode.PRINT));
            
            this.cps = cps.toString();
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 53 * hash + this.cps.hashCode();
            hash = 53 * hash + (this.sourceRoot != null ? this.sourceRoot.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CPCategorizer other = (CPCategorizer) obj;
            if (!this.cps.equals(other.cps)) {
                return false;
            }
            if (this.sourceRoot != other.sourceRoot && (this.sourceRoot == null || !this.sourceRoot.equals(other.sourceRoot))) {
                return false;
            }
            return true;
        }
        
    }

    public static final String ENSURE_DEPENDENCY = "ensure-dependency";

    public static boolean fixDependencies(FileObject file, Iterable<? extends JavaFix> toProcess, Map<Project, Set<String>> alreadyProcessed) {
        boolean modified = false;
//        for (FileObject file : toProcess.keySet()) {
            for (JavaFix fix : toProcess) {
                String updateTo = Accessor.INSTANCE.getOptions(fix).get(ENSURE_DEPENDENCY);

                if (updateTo != null) {
                    Project p = FileOwnerQuery.getOwner(file);

                    if (p != null) {
                        Set<String> seen = alreadyProcessed.computeIfAbsent(p, k -> new HashSet<>());

                        if (seen.add(updateTo)) {
                            for (ProjectDependencyUpgrader up : Lookup.getDefault().lookupAll(ProjectDependencyUpgrader.class)) {
                                if (up.ensureDependency(p, updateTo, false)) { //XXX: should check whether the given project was actually modified
                                    modified = true;
                                    break;
                                }
                            }
                            //TODO: fail if cannot update the dependency?
                        }
                    }
                }
            }

            return modified;
//        }
    }

    public static void recursive(FileObject root, FileObject file, Collection<FileObject> collected, ProgressHandleWrapper progress, int depth, Properties timeStamps, Set<String> removedFiles, boolean recursive) {
        if (!VisibilityQuery.getDefault().isVisible(file)) return;

        if (file.isData()) {
            if (timeStamps != null) {
                String relativePath = FileUtil.getRelativePath(root, file);
                String lastModified = Long.toHexString(file.lastModified().getTime());

                removedFiles.remove(relativePath);

                if (lastModified.equals(timeStamps.getProperty(relativePath))) {
                    return;
                }

                timeStamps.setProperty(relativePath, lastModified);
            }

            if (/*???:*/"java".equals(file.getExt()) || "text/x-java".equals(FileUtil.getMIMEType(file, "text/x-java"))) {
                collected.add(file);
            }
        } else {
            FileObject[] children = file.getChildren();

            if (children.length == 0) return;

            ProgressHandleWrapper inner = depth < 2 ? progress.startNextPartWithEmbedding(ProgressHandleWrapper.prepareParts(children.length)) : null;

            if (inner == null && progress != null) {
                progress.startNextPart(children.length);
            } else {
                progress = null;
            }

            for (FileObject c : children) {
                if (recursive || c.isData())
                    recursive(root, c, collected, inner, depth + 1, timeStamps, removedFiles, recursive);

                if (progress != null) progress.tick();
            }
        }
    }    
}
