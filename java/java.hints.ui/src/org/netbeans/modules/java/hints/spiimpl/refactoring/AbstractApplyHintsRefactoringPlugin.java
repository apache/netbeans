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

package org.netbeans.modules.java.hints.spiimpl.refactoring;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.modules.java.hints.spiimpl.MessageImpl;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.BatchResult;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.Resource;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.Scope;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchUtilities;
import org.netbeans.modules.java.hints.spiimpl.batch.ProgressHandleWrapper;
import org.netbeans.modules.java.hints.spiimpl.batch.ProgressHandleWrapper.ProgressHandleAbstraction;
import org.netbeans.spi.java.hints.HintContext.MessageKind;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.ProgressListener;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.ProgressProvider;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Lookup;

/**
 *
 * @author lahvac
 */
public abstract class AbstractApplyHintsRefactoringPlugin extends ProgressProviderAdapter implements RefactoringPlugin, ProgressHandleAbstraction {

    private static final Logger LOG = Logger.getLogger(AbstractApplyHintsRefactoringPlugin.class.getName());

    private final AbstractRefactoring refactoring;
    private Comparator<FileObject> FILE_COMPARATOR = new Comparator<FileObject>() {
        @Override public int compare(FileObject o1, FileObject o2) {
            return o1.getPath().compareTo(o2.getPath());
        }
    };
    protected final AtomicBoolean cancel = new AtomicBoolean();

    protected AbstractApplyHintsRefactoringPlugin(AbstractRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    public void cancelRequest() {
        cancel.set(true);
    }

    protected final Problem messagesToProblem(Collection<MessageImpl> problems) throws IllegalStateException {
        Problem current = null;

        for (MessageImpl problem : problems) {
            Problem p = new Problem(problem.kind == MessageKind.ERROR, problem.text);

            if (current != null)
                p.setNext(current);
            current = p;
        }

        return current;
    }

    protected Collection<MessageImpl> performApplyPattern(Iterable<? extends HintDescription> pattern, Scope scope, RefactoringElementsBag refactoringElements) {
        ProgressHandleWrapper w = new ProgressHandleWrapper(this, 30, 70);
        BatchResult candidates = BatchSearch.findOccurrences(pattern, scope, w, /*XXX:*/HintsSettings.getGlobalSettings());
        Collection<RefactoringElementImplementation> fileChanges = new ArrayList<RefactoringElementImplementation>();
        Collection<MessageImpl> problems = new LinkedList<MessageImpl>(candidates.problems);
        Map<JavaFix, ModificationResult> changesPerFix = new IdentityHashMap<JavaFix, ModificationResult>();
        Collection<? extends ModificationResult> res = BatchUtilities.applyFixes(candidates, w, cancel, fileChanges, changesPerFix, problems);
        Set<ModificationResult> enabled = Collections.newSetFromMap(new IdentityHashMap<ModificationResult, Boolean>());
        Map<FileObject, Map<JavaFix, ModificationResult>> file2Fixes2Changes = new HashMap<FileObject, Map<JavaFix, ModificationResult>>();
        Map<FileObject, Set<FileObject>> affectedFiles = new HashMap<FileObject, Set<FileObject>>();
        Map<FileObject, List<RefactoringElementImplementation>> file2Changes = new TreeMap<FileObject, List<RefactoringElementImplementation>>(FILE_COMPARATOR);
        
        for (Entry<JavaFix, ModificationResult> changesPerFixEntry : changesPerFix.entrySet()) {
            enabled.add(changesPerFixEntry.getValue());
            
            for (FileObject file : changesPerFixEntry.getValue().getModifiedFileObjects()) {
                List<RefactoringElementImplementation> currentFileChanges = file2Changes.get(file);
                
                if (currentFileChanges == null) {
                    file2Changes.put(file, currentFileChanges = new ArrayList<RefactoringElementImplementation>());
                }
                
                currentFileChanges.add(new ModificationResultElement(file, changesPerFixEntry.getKey(), changesPerFixEntry.getValue(), enabled));
                
                Map<JavaFix, ModificationResult> perFile = file2Fixes2Changes.get(file);
                
                if (perFile == null) {
                    file2Fixes2Changes.put(file, perFile = new IdentityHashMap<JavaFix, ModificationResult>());
                }
                
                perFile.put(changesPerFixEntry.getKey(), changesPerFixEntry.getValue());
                
                Set<FileObject> aff = affectedFiles.get(file);
                
                if (aff == null) {
                    affectedFiles.put(file, aff = new HashSet<FileObject>());
                }
                
                aff.addAll(changesPerFixEntry.getValue().getModifiedFileObjects());
            }
        }
        
        for (List<RefactoringElementImplementation> changes : file2Changes.values()) {
            changes.sort(new Comparator<RefactoringElementImplementation>() {
                @Override public int compare(RefactoringElementImplementation o1, RefactoringElementImplementation o2) {
                    return o1.getPosition().getBegin().getOffset() - o2.getPosition().getBegin().getOffset();
                }
            });
            
            refactoringElements.addAll(refactoring, changes);
        }
        
        refactoringElements.registerTransaction(new DelegatingTransaction(enabled, file2Fixes2Changes, affectedFiles, res));

        for (RefactoringElementImplementation fileChange : fileChanges) {
            refactoringElements.addFileChange(refactoring, fileChange);
        }

        w.finish();

        return problems;
    }

    protected final void prepareElements(BatchResult candidates, ProgressHandleWrapper w, final RefactoringElementsBag refactoringElements, final boolean verify, List<MessageImpl> problems) {
        final Map<FileObject, Collection<RefactoringElementImplementation>> file2Changes = new TreeMap<FileObject, Collection<RefactoringElementImplementation>>(FILE_COMPARATOR);
        if (verify) {
            BatchSearch.getVerifiedSpans(candidates, w, new BatchSearch.VerifiedSpansCallBack() {
                public void groupStarted() {}
                public boolean spansVerified(CompilationController wc, Resource r, Collection<? extends ErrorDescription> hints) throws Exception {
                    List<PositionBounds> spans = new LinkedList<PositionBounds>();

                    for (ErrorDescription ed : hints) {
                        spans.add(ed.getRange());
                    }
                    
                    file2Changes.put(r.getResolvedFile(), Utilities.createRefactoringElementImplementation(r.getResolvedFile(), spans, verify));
                    return true;
                }
                public void groupFinished() {}
                public void cannotVerifySpan(Resource r) {
                    file2Changes.put(r.getResolvedFile(), Utilities.createRefactoringElementImplementation(r.getResolvedFile(), prepareSpansFor(r), verify));
                }
            }, problems, cancel);
        } else {
            int[] parts = new int[candidates.getResources().size()];
            int   index = 0;

            for (Collection<? extends Resource> resources : candidates.getResources()) {
                parts[index++] = resources.size();
            }

            ProgressHandleWrapper inner = w.startNextPartWithEmbedding(parts);

            for (Collection<? extends Resource> it :candidates.getResources()) {
                inner.startNextPart(it.size());

                for (Resource r : it) {
                    file2Changes.put(r.getResolvedFile(), Utilities.createRefactoringElementImplementation(r.getResolvedFile(), prepareSpansFor(r), verify));
                    inner.tick();
                }
            }
        }
        
        for (Collection<RefactoringElementImplementation> res : file2Changes.values()) {
            refactoringElements.addAll(refactoring, res);
        }
    }

    private static List<PositionBounds> prepareSpansFor(Resource r) {
        return Utilities.prepareSpansFor(r.getResolvedFile(), r.getCandidateSpans());
    }

    public void start(int totalWork) {
        fireProgressListenerStart(-1, totalWork);
        lastWorkDone = 0;
    }

    private int lastWorkDone;
    public void progress(int currentWorkDone) {
        while (lastWorkDone < currentWorkDone) {
            fireProgressListenerStep(currentWorkDone);
            lastWorkDone++;
        }
    }

    public void progress(String message) {
        //ignored
    }

    public void finish() {
        fireProgressListenerStop();
    }

    private static final class ModificationResultElement extends SimpleRefactoringElementImplementation {

        private PositionBounds bounds;
        private String displayText;
        private FileObject parentFile;
        private ModificationResult modification;
        private WeakReference<String> newFileContent;
        private final Set<ModificationResult> enabledResults;

        private ModificationResultElement(FileObject parentFile, JavaFix jf, ModificationResult modification, Set<ModificationResult> enabledResults) {
            // FIXME - unwanted openide.text dependency
            PositionRef s = (PositionRef)modification.getDifferences(parentFile).iterator().next().getStartPosition();
            this.bounds = new PositionBounds(s, s);
            this.displayText = jf.toEditorFix().getText();
            this.parentFile = parentFile;
            this.modification = modification;
            this.enabledResults = enabledResults;
        }

        @Override
        public String getDisplayText() {
            return displayText;
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public void setEnabled(boolean enabled) {
            if (enabled) {
                enabledResults.add(modification);
            } else {
                enabledResults.remove(modification);
            }
            super.setEnabled(enabled);
        }

        @Override
        public boolean isEnabled() {
            return enabledResults.contains(modification);
        }

        @Override
        public PositionBounds getPosition() {
            return bounds;
        }

        @Override
        public String getText() {
            return displayText;
        }

        @Override
        public void performChange() {
        }

        @Override
        public FileObject getParentFile() {
            return parentFile;
        }

        @Override
        protected String getNewFileContent() {
            if (!isEnabled()) {
                try {
                    return parentFile.asText();
                } catch (IOException ex) {
                    LOG.log(Level.INFO, null, ex);
                    return null;
                }
            }
            String result = newFileContent != null ? newFileContent.get() : null;
            if (result != null) return result;
            try {
                result = modification.getResultingSource(parentFile);
            } catch (IOException ex) {
                LOG.log(Level.INFO, null, ex);
                return null;
            }
            newFileContent = new WeakReference<String>(result);
            return result;
        }
    }
    
    private static final class DelegatingTransaction implements Transaction, ProgressProvider {

        private final Set<ModificationResult> enabled;
        private final Map<FileObject, Map<JavaFix, ModificationResult>> file2Fixes2Changes;
        private final Map<FileObject, Set<FileObject>> affectedFiles;
        private final Collection<? extends ModificationResult> completeModificationResult;
        
        private Transaction delegate;
        private ProgressSupport progressSupport;
        private final ProgressListener listener;

        public DelegatingTransaction(Set<ModificationResult> enabled, Map<FileObject, Map<JavaFix, ModificationResult>> file2Fixes2Changes, Map<FileObject, Set<FileObject>> affectedFiles, Collection<? extends ModificationResult> completeModificationResult) {
            this.enabled = enabled;
            this.file2Fixes2Changes = file2Fixes2Changes;
            this.affectedFiles = affectedFiles;
            this.completeModificationResult = completeModificationResult;
            listener = new ProgressListener() {

                    @Override
                    public void start(ProgressEvent event) {
                        fireProgressListenerStart(event.getOperationType(), event.getCount());
                    }

                    @Override
                    public void step(ProgressEvent event) {
                        fireProgressListenerStep(event.getCount());
                    }

                    @Override
                    public void stop(ProgressEvent event) {
                        fireProgressListenerStop();
                    }
                };
        }
        
        @Override
        public synchronized void commit() {
            if (delegate == null) {
                Set<FileObject> toRecompute = new HashSet<FileObject>();
                
                for (ModificationResult mr : completeModificationResult) {
                    for (FileObject modified : mr.getModifiedFileObjects()) {
                        if (!affectedFiles.containsKey(modified)) {
                            assert mr.getDifferences(modified).isEmpty();
                            continue;
                        }
                        for (FileObject affected : affectedFiles.get(modified)) {
                            for (Entry<JavaFix, ModificationResult> e : file2Fixes2Changes.get(affected).entrySet()) {
                                if (!enabled.contains(e.getValue())) {
                                    toRecompute.add(affected);
                                    break;
                                }
                            }
                        }
                    }
                }
                
                final Map<FileObject, Collection<JavaFix>> toRun = new HashMap<FileObject, Collection<JavaFix>>();
                
                for (Iterator<FileObject> it = toRecompute.iterator(); it.hasNext();) {
                    FileObject r = it.next();
                    
                    for (ModificationResult mr : completeModificationResult) {
                        List<? extends Difference> diffs = mr.getDifferences(r);
                        
                        if (diffs == null) continue;
                        
                        for (Difference c : diffs) {
                            c.exclude(true);
                        }
                    }
                    
                    for (Entry<JavaFix, ModificationResult> e : file2Fixes2Changes.get(r).entrySet()) {
                        if (enabled.contains(e.getValue())) {
                            Collection<JavaFix> fixes2Run = toRun.get(r);
                            
                            if (fixes2Run == null) {
                                toRun.put(r, fixes2Run = new ArrayList<JavaFix>());
                            }
                            
                            fixes2Run.add(e.getKey());
                        }
                    }
                }

                List<ModificationResult> real = new ArrayList<ModificationResult>(completeModificationResult);
                
                real.addAll(BatchUtilities.applyFixes(toRun));
                
                delegate = JavaRefactoringPlugin.createTransaction(new LinkedList<ModificationResult>(real));
            }
            if(delegate instanceof ProgressProvider) {
                ProgressProvider progressProvider = (ProgressProvider) delegate;
                progressProvider.addProgressListener(listener);
            }
            try {
                delegate.commit();
            } finally {
                if(delegate instanceof ProgressProvider) {
                    ProgressProvider progressProvider = (ProgressProvider) delegate;
                    progressProvider.removeProgressListener(listener);
                }
            }
        }

        @Override
        public synchronized void rollback() {
            delegate.rollback();
        }

        /**
         * Registers ProgressListener to receive events.
         *
         * @param listener The listener to register.
         *
         */
        @Override
        public synchronized void addProgressListener(ProgressListener listener) {
            if (progressSupport == null) {
                progressSupport = new ProgressSupport();
            }
            progressSupport.addProgressListener(listener);
        }

        /**
         * Removes ProgressListener from the list of listeners.
         *
         * @param listener The listener to remove.
         *
         */
        @Override
        public synchronized void removeProgressListener(ProgressListener listener) {
            if (progressSupport != null) {
                progressSupport.removeProgressListener(listener);
            }
        }

        private void fireProgressListenerStart(int type, int count) {
            if (progressSupport != null) {
                progressSupport.fireProgressListenerStart(this, type, count);
            }
        }

        private void fireProgressListenerStep(int count) {
            if (progressSupport != null) {
                progressSupport.fireProgressListenerStep(this, count);
            }
        }

        private void fireProgressListenerStop() {
            if (progressSupport != null) {
                progressSupport.fireProgressListenerStop(this);
            }
        }

        /**
         * Support class for progress notifications.
         * Copy of org.netbeans.modules.refactoring.api.impl.ProgressSupport
         * @author Martin Matula, Jan Becicka
         */
        public final class ProgressSupport {

            /**
             * Utility field holding list of ProgressListeners.
             */
            private final List<ProgressListener> progressListenerList = new ArrayList<ProgressListener>();
            private int counter;
            private boolean deterministic;

            public boolean isEmpty() {
                return progressListenerList.isEmpty();
            }

            public synchronized void addProgressListener(ProgressListener listener) {
                progressListenerList.add(listener);
            }

            /**
             * Removes ProgressListener from the list of listeners.
             *
             * @param listener The listener to remove.
             *
             */
            public synchronized void removeProgressListener(ProgressListener listener) {
                progressListenerList.remove(listener);
            }

            /**
             * Notifies all registered listeners about the event.
             *
             * @param type Type of operation that is starting.
             * @param count Number of steps the operation consists of.
             *
             */
            public void fireProgressListenerStart(Object source, int type, int count) {
                counter = -1;
                deterministic = count > 0;
                ProgressEvent event = new ProgressEvent(source, ProgressEvent.START, type, count);
                ProgressListener[] listeners = getListenersCopy();
                for (ProgressListener listener : listeners) {
                    try {
                        listener.start(event);
                    } catch (RuntimeException e) {
                        log(e);
                    }
                }
            }

            /**
             * Notifies all registered listeners about the event.
             *
             * @param type Type of operation that is starting.
             * @param count Number of steps the operation consists of.
             *
             */
            public void fireProgressListenerStart(int type, int count) {
                fireProgressListenerStart(this, type, count);
            }

            /**
             * Notifies all registered listeners about the event.
             */
            public void fireProgressListenerStep(Object source, int count) {
                if (deterministic) {
                    if (count < 0) {
                        deterministic = false;
                    }
                    counter = count;
                } else {
                    if (count > 0) {
                        deterministic = true;
                        counter = -1;
                    } else {
                        counter = count;
                    }
                }
                ProgressEvent event = new ProgressEvent(source, ProgressEvent.STEP, 0, count);
                ProgressListener[] listeners = getListenersCopy();
                for (ProgressListener listener : listeners) {
                    try {
                        listener.step(event);
                    } catch (RuntimeException e) {
                        log(e);
                    }
                }
            }

            /**
             * Notifies all registered listeners about the event.
             */
            public void fireProgressListenerStep(Object source) {
                if (deterministic) {
                    ++counter;
                }
                fireProgressListenerStep(source, counter);
            }

            /**
             * Notifies all registered listeners about the event.
             */
            public void fireProgressListenerStop(Object source) {
                ProgressEvent event = new ProgressEvent(source, ProgressEvent.STOP);
                ProgressListener[] listeners = getListenersCopy();
                for (ProgressListener listener : listeners) {
                    try {
                        listener.stop(event);
                    } catch (RuntimeException e) {
                        log(e);
                    }
                }
            }

            /**
             * Notifies all registered listeners about the event.
             */
            public void fireProgressListenerStop() {
                fireProgressListenerStop(this);
            }

            private synchronized ProgressListener[] getListenersCopy() {
                return progressListenerList.toArray(new ProgressListener[0]);
            }

            private void log(Exception e) {
                Logger.getLogger(ProgressSupport.class.getName()).log(Level.INFO, e.getMessage(), e);
            }
        }
    }
}
