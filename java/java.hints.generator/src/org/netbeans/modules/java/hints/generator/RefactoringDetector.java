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
package org.netbeans.modules.java.hints.generator;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.ImageIcon;
import javax.swing.text.Document;
import javax.tools.Diagnostic;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.support.EditorAwareJavaSourceTaskFactory;
import org.netbeans.modules.java.hints.generator.PatternGenerator.Fact;
import org.netbeans.modules.java.hints.generator.PatternGenerator.OfflineTree;
import org.netbeans.modules.java.hints.generator.PatternGenerator.OfflineTree.Key;
import org.netbeans.modules.java.hints.generator.PatternGenerator.PatternDescription;
import org.netbeans.modules.java.hints.generator.PatternGenerator.PatternStatistics;
import org.netbeans.modules.java.hints.generator.PatternGenerator.Result;
import static org.netbeans.modules.java.hints.generator.PatternGenerator.computeResult;
import static org.netbeans.modules.java.hints.generator.PatternGenerator.determineSources;
import org.netbeans.modules.java.hints.generator.borrowed.matching.Occurrence;
import org.netbeans.modules.java.hints.generator.ui.AdaptiveRefactoringTopComponent;
import org.netbeans.modules.java.hints.generator.ui.ProgressDialog;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.parsing.CompilationInfoImpl;
import org.netbeans.modules.java.source.save.ElementOverlay;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Pair;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

/**
 *
 * @author lahvac
 */
public class RefactoringDetector {

    static int minimalFacts = 2;

    private static final ImageIcon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/java/hints/generator/resources/adaptive_refactoring.png", false);

    private static final Map<ClassPath, RefactoringDetector> sourcePath2Detector = new HashMap<>();

    public static RefactoringDetector forSourcePath(ClassPath sourcePath) {
        return sourcePath2Detector.computeIfAbsent(sourcePath, sp -> new RefactoringDetector(sourcePath));
    }

    public static EditorPeer editorPeer(CompilationInfo info) {
        Map<Key, OfflineTree> treeCache = new HashMap<>();
        return forSourcePath(info.getClasspathInfo().getClassPath(PathKind.SOURCE)).new EditorPeer(OfflineTree.of(info, new TreePath(info.getCompilationUnit()), treeCache, new IdentityHashMap<>()), treeCache);
    }

    private final ClassPath forSourcePath;
    private final Map<String, PatternDescription> patterns = new HashMap<>();
    private final PatternStatistics patternStatistics = new PatternStatistics();

    public RefactoringDetector(ClassPath forSourcePath) {
        this.forSourcePath = forSourcePath;
    }

    private Set<List<String>> notifiedPatterns = new HashSet<>();

    public void report(Collection<PatternDescription> patterns) {
        for (PatternDescription desc : patterns) {
            if (notifiedPatterns.add(Arrays.asList(desc.getInputPattern(), desc.getTargetPattern()))) {
                NotificationDisplayer.getDefault().notify("Possible refactoring detected!", icon, desc.getInputPattern() + "=>" + desc.getTargetPattern(), evt -> {
                    computeAndShowRefactoring(desc);
                });
            }
        }
    }

    private void computeAndShowRefactoring(PatternDescription desc) {
        Result result = ProgressDialog.showProgress("Inspecting changes", (progress, cancel) -> {
            try {
                progress.start();
                progress.progress("Updating - determining files...");

                Pair<List<JavaSource>, Integer> sources = determineSources(Arrays.asList(forSourcePath.getRoots()), cancel);

                if (cancel.get())
                    return null;

                progress.switchToDeterminate(sources.second());
                progress.progress("Applying changes...");

                AtomicInteger done = new AtomicInteger();

                try {
                    return computeResult(sources.first(), patterns.values(), () -> progress.progress(done.incrementAndGet()), cancel);
                } finally {
                    progress.finish();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        });

        if (result != null) {
            AdaptiveRefactoringTopComponent tc = new AdaptiveRefactoringTopComponent(result);
            Mode outputMode = WindowManager.getDefault().findMode("output");
            if (outputMode != null) {
                outputMode.dockInto(tc);
            }
            tc.open();
            tc.requestActive();
        }
    }

    public final class EditorPeer {

        private final Map<Key, OfflineTree> treeCache;
        private final List<OfflineTree> seenTrees;

        private EditorPeer(OfflineTree original, Map<Key, OfflineTree> treeCache) {
            this.seenTrees = new ArrayList<>();
            this.seenTrees.add(original);
            this.treeCache = treeCache;
        }

        public Collection<PatternDescription> reparse(CompilationInfo info) {
            for (Diagnostic d : info.getDiagnostics()) {
                if (d.getKind() == Diagnostic.Kind.ERROR)
                    return Collections.emptyList(); //ignore erroneous - TODO: could check original errors vs. current errors
            }

            WorkingCopy copy;

            try {
                ElementOverlay overlay = ElementOverlay.getOrCreateOverlay();
                Constructor<WorkingCopy> wcConstr = WorkingCopy.class.getDeclaredConstructor(CompilationInfoImpl.class, ElementOverlay.class);
                wcConstr.setAccessible(true);

                copy = wcConstr.newInstance(JavaSourceAccessor.getINSTANCE().getCompilationInfoImpl(info), overlay);
                Method setJavaSource = CompilationInfo.class.getDeclaredMethod("setJavaSource", JavaSource.class);
                setJavaSource.setAccessible(true);

        //                copy.setJavaSource(JavaSource.this);
                setJavaSource.invoke(copy, info.getJavaSource());

                copy.toPhase(Phase.RESOLVED);
            } catch (IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException | InstantiationException | IOException ex) {
                throw new IllegalStateException(ex);
            }

            Map<Tree, OfflineTree> tree2Offline = new IdentityHashMap<>();
            OfflineTree currentTree = OfflineTree.of(info, new TreePath(info.getCompilationUnit()), treeCache, tree2Offline);

            patternStatistics.fileProcessingStarted(info.getFileObject());
            try {
                for (OfflineTree original : seenTrees) {
                    PatternGenerator.diff(patterns, patternStatistics, original, copy, tree2Offline);
                }
            } finally {
                patternStatistics.fileProcessingFinished();
            }


            //XXX: eviction policy!!!!
            seenTrees.add(currentTree);

            if (!patterns.isEmpty()) {
                Collection<PatternDescription> result = new ArrayList<>();

                //XXX: cancel!
                for (PatternDescription desc : patterns.values()) {
                    if (patternStatistics.getPatternUseCount(desc.getInputPattern()) < minimalFacts)
                        continue; //must have at least minimalFacts number of uses
                    if (desc.getPositiveFacts().iterator().next().getVariableCount() == 0)
                        continue; //too simple?

                    Set<ElementHandle<TypeElement>> expectedElements = new HashSet<>();

                    for (Fact f : desc.getPositiveFacts()) {
                        for (String[] method : f.methodInvocation2Method.values()) {
                            expectedElements.add(ElementHandle.createTypeElementHandle(ElementKind.CLASS, method[0]));
                        }
                    }

                    Set<FileObject> files = new HashSet<>();

                    for (ElementHandle<TypeElement> expectedEl : expectedElements) {
                        Set<FileObject> resources = info.getClasspathInfo().getClassIndex().getResources(expectedEl, EnumSet.of(SearchKind.METHOD_REFERENCES), EnumSet.of(SearchScope.SOURCE));
                        if (resources == null) { //XXX: cancelled!!!!
                            break;
                        }
                        files.addAll(resources);
                    }

                    if (!files.isEmpty()) {
                        try {
                            boolean[] foundCandidate = new boolean[1];

                            JavaSource.create(info.getClasspathInfo(), files).runModificationTask(wc -> {
                                if (wc.toPhase(Phase.PARSED).compareTo(Phase.PARSED) < 0 || foundCandidate[0]) {
                                    return ;
                                }
                                for (Occurrence occ : PatternGenerator.findMatches(wc, desc)) {
                                    if (wc.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                                        return ;
                                    }
                                    Fact currentFact = PatternGenerator.occurrence2Fact(wc, occ);
                                    if (PatternGenerator.estimate(desc, currentFact).first() == Result.Kind.POSITIVE) {
                                        foundCandidate[0] = true;
                                        break;
                                    }
                                }
                            });

                            if (foundCandidate[0]) {
                                result.add(desc);
                            }
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }

                return result;
            }

            return Collections.emptyList();
        }

        public RefactoringDetector getDetector() {
            return RefactoringDetector.this;
        }
    }

    public static final class EditorBinding implements CancellableTask<CompilationInfo> {

        @Override
        public void run(CompilationInfo info) throws Exception {
            HintsSettings hs = HintsSettings.getSettingsFor(info.getFileObject());
            HintMetadata hm = HintMetadata.Builder.create("AdaptiveRefactoringDetectorEnabler")
                                                  .setEnabled(false)
                                                  .build();

            if (!hs.isEnabled(hm)) {
                return ;
            }

            Document doc = info.getDocument();

            if (doc == null)
                return ;

            EditorPeer ep = (EditorPeer) doc.getProperty(EditorPeer.class);

            if (ep == null) {
                doc.putProperty(EditorPeer.class, RefactoringDetector.editorPeer(info));
            } else {
                ep.getDetector().report(ep.reparse(info));
            }
        }
        
        @Override
        public void cancel() {
        }

        @ServiceProvider(service=JavaSourceTaskFactory.class)
        public static final class Factory extends EditorAwareJavaSourceTaskFactory {

            public Factory() {
                super(Phase.RESOLVED, Priority.LOW, TaskIndexingMode.ALLOWED_DURING_SCAN, new String[0]);
            }

            @Override
            protected CancellableTask<CompilationInfo> createTask(FileObject file) {
                return new EditorBinding();
            }

        }
    }

}
