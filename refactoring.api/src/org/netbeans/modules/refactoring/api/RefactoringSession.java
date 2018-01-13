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
package org.netbeans.modules.refactoring.api;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.refactoring.api.impl.ProgressSupport;
import org.netbeans.modules.refactoring.api.impl.SPIAccessor;
import org.netbeans.modules.refactoring.spi.ProgressProvider;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.netbeans.modules.refactoring.spi.impl.UndoManager;
import org.netbeans.modules.refactoring.spi.impl.UndoableWrapper;
import org.openide.LifecycleManager;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 * Class used to invoke refactorings.
 *
 * @author Martin Matula, Daniel Prusa, Jan Becicka
 */
public final class RefactoringSession {

    private static final int COMMIT_STEPS = 100;

    private final ArrayList<RefactoringElementImplementation> internalList = new ArrayList<>();
    private final Collection<RefactoringElement> refactoringElements = new ElementsCollection();
    private final RefactoringElementsBag bag;
    private final UndoManager undoManager = UndoManager.getDefault();
    private final AtomicBoolean finished = new AtomicBoolean(false);
    private final String description;

    @SuppressWarnings("PackageVisibleField")
    boolean realcommit = true;

    private ProgressSupport progressSupport;

    @SuppressWarnings("LeakingThisInConstructor")
    private RefactoringSession(String description) {
        bag = SPIAccessor.DEFAULT.createBag(this, internalList);
        this.description = description;
    }

    /**
     * Creates a new refactoring session.
     *
     * @param description textual description of this session
     * @return instance of RefactoringSession
     */
    @NonNull
    public static RefactoringSession create(@NonNull String description) {
        Parameters.notNull("description", description); // NOI18N
        return new RefactoringSession(description);
    }

    /**
     * process all elements from elements bags, do all fileChanges and call all
     * commits
     *
     * @param saveAfterDone save all if true
     * @return instance of Problem or null, if everything is OK
     */
    @CheckForNull
    public Problem doRefactoring(final boolean saveAfterDone) {
        return Utilities.runWithOnSaveTasksDisabled(() -> {

            final long start = System.currentTimeMillis();

            /* commits */
            ArrayList<Transaction> commits = SPIAccessor.DEFAULT.getCommits(bag);
            {
                int count = COMMIT_STEPS + commits.size() * COMMIT_STEPS + 1;
                fireProgressListenerStart(0, count);
            }
            if (realcommit) {
                undoManager.transactionStarted();
                undoManager.setUndoDescription(description);
            }
            try {

                try {
                    float progressStep = (float) COMMIT_STEPS / internalList.size();
                    float current = 0F;

                    /* internal list */
                    for (RefactoringElementImplementation element : internalList) {
                        performChange(element, true);
                        current += progressStep;
                        fireProgressListenerStep((int) current);
                    }
                } finally {
                    commits.forEach(commit -> SPIAccessor.DEFAULT.check(commit, false));
                    UndoableWrapper undoableWrapper = MimeLookup.getLookup(MimePath.EMPTY).lookup(UndoableWrapper.class);
                    undoableWrapper.setActive(true, this);
                    commits.stream().forEachOrdered(commit -> {
                        if (commit instanceof ProgressProvider) {
                            ProgressProvider provider = (ProgressProvider) commit;
                            ProgressListener listener = new ProgressListenerImplementation(commits, COMMIT_STEPS);
                            provider.addProgressListener(listener);
                            try {
                                commit.commit();
                            } finally {
                                provider.removeProgressListener(listener);
                            }
                        } else {
                            commit.commit();
                        }
                    });
                    undoableWrapper.setActive(false, null);
                    undoableWrapper.close();
                    commits.forEach(commit -> SPIAccessor.DEFAULT.sum(commit));
                }

                if (saveAfterDone) {
                    LifecycleManager.getDefault().saveAll();
                    DataObject[] dataObjects = DataObject.getRegistry().getModified();
                    Stream.of(dataObjects)
                            .forEach(dataObject -> {
                                SaveCookie cookie = dataObject.getLookup().lookup(SaveCookie.class);
                                try {
                                    cookie.save();
                                } catch (IOException exception) {
                                    Exceptions.printStackTrace(exception);
                                }
                            });
                }

                /* file changes */
                SPIAccessor.DEFAULT.getFileChanges(bag)
                        .stream()
                        .filter(fileChange -> fileChange.isEnabled())
                        .forEachOrdered(fileChange -> fileChange.performChange());

                fireProgressListenerStep();
            } finally {
                fireProgressListenerStop();
                if (realcommit) {
                    undoManager.addItem(this);
                    undoManager.transactionEnded(false, this);
                    realcommit = false;
                }
            }

            Logger timer = Logger.getLogger("TIMER.RefactoringSession");
            if (timer.isLoggable(Level.FINE)) {
                final long timeTaken = System.currentTimeMillis() - start;
                timer.log(Level.FINE, "refactoringSession.doRefactoring", new Object[]{description, RefactoringSession.this, timeTaken});
            }

            return null;

        });
    }

    private class ProgressListenerImplementation implements ProgressListener {

        private float progressStep;
        private float current;
        private final ArrayList<Transaction> commits;
        private final int start;

        ProgressListenerImplementation(ArrayList<Transaction> commits, int start) {
            this.commits = commits;
            this.start = start;
        }

        @Override
        public void start(ProgressEvent event) {
            progressStep = (float) COMMIT_STEPS / event.getCount();
            current = start + commits.indexOf(event.getSource()) * COMMIT_STEPS;
            fireProgressListenerStep((int) current);
        }

        @Override
        public void step(ProgressEvent event) {
            current += progressStep;
            fireProgressListenerStep((int) current);
        }

        @Override
        public void stop(ProgressEvent event) {
            /* do not rely on plugins; */
        }

    }

    /**
     * do undo of previous doRefactoring()
     *
     * @param saveAfterDone save all if true
     * @return instance of Problem or null, if everything is OK
     */
    @CheckForNull
    public Problem undoRefactoring(final boolean saveAfterDone) {
        return Utilities.runWithOnSaveTasksDisabled(() -> {

            try {
                {
                    int count = internalList.size() + 1;
                    fireProgressListenerStart(0, count);
                }

                {
                    ArrayList<RefactoringElementImplementation> fileChanges = SPIAccessor.DEFAULT.getFileChanges(bag);
                    ArrayList<RefactoringElementImplementation> fileChangesReversed = new ArrayList<>(fileChanges);
                    Collections.reverse(fileChangesReversed);
                    fileChangesReversed.stream()
                            .filter(fileChange -> fileChange.isEnabled())
                            .forEachOrdered(fileChange -> fileChange.undoChange());
                }

                {
                    ArrayList<Transaction> commits = SPIAccessor.DEFAULT.getCommits(bag);
                    commits.forEach(commit -> SPIAccessor.DEFAULT.check(commit, true));
                    ArrayList<Transaction> commitsReversed = new ArrayList<>(commits);
                    Collections.reverse(commitsReversed);
                    UndoableWrapper undoableWrapper = MimeLookup.getLookup(MimePath.EMPTY).lookup(UndoableWrapper.class);
                    undoableWrapper.setActive(true, this);
                    commitsReversed.stream()
                            .forEachOrdered(commit -> commit.rollback());
                    undoableWrapper.setActive(false, null);
                    undoableWrapper.close();
                    commits.forEach(commit -> SPIAccessor.DEFAULT.sum(commit));
                }

                {
                    ArrayList<RefactoringElementImplementation> internalListReversed = new ArrayList<>(internalList);
                    Collections.reverse(internalListReversed);
                    internalListReversed.stream().forEachOrdered(element -> {
                        fireProgressListenerStep();
                        performChange(element, false);
                    });
                }

                if (saveAfterDone) {
                    LifecycleManager.getDefault().saveAll();
                }

                fireProgressListenerStep();
            } finally {
                fireProgressListenerStop();
            }

            return null;

        });
    }

    /**
     * Performs either the specified refactoring change, or undoes it.
     *
     * @param element the refactoring change
     * @param change true, if change should be applied
     */
    private void performChange(RefactoringElementImplementation element, boolean change) {
        boolean enabled = element.isEnabled();
        boolean guarded = element.getStatus() == RefactoringElement.GUARDED;
        boolean readOnly = element.getStatus() == RefactoringElement.READ_ONLY;
        if (enabled && !(guarded || readOnly)) {
            if (change) {
                element.performChange();
            } else {
                element.undoChange();
            }
        }
    }

    /**
     * Get elements from session
     *
     * @since 1.23 the returned collection is blocking until finished.
     * @see #finished()
     * @return collection of RefactoringElements
     */
    @NonNull
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public Collection<RefactoringElement> getRefactoringElements() {
        return refactoringElements;
    }

    /**
     * Inform the session internalListIterator, and all its plugins, are
     * finished.
     *
     * @since 1.28
     * @see #getRefactoringElements()
     */
    public void finished() {
        finished.set(true);
    }

    boolean isFinished() {
        return finished.get();
    }

    /**
     * Adds progress listener to this RefactoringSession
     *
     * @param listener to add
     */
    public synchronized void addProgressListener(@NonNull ProgressListener listener) {
        Parameters.notNull("listener", listener); // NOI18N
        if (progressSupport == null) {
            progressSupport = new ProgressSupport();
        }
        progressSupport.addProgressListener(listener);
    }

    /**
     * Remove progress listener from this RefactoringSession
     *
     * @param listener to remove
     */
    public synchronized void removeProgressListener(@NonNull ProgressListener listener) {
        Parameters.notNull("listener", listener); // NOI18N
        if (progressSupport != null) {
            progressSupport.removeProgressListener(listener);
        }
    }

    RefactoringElementsBag getElementsBag() {
        return bag;
    }

    private void fireProgressListenerStart(int type, int count) {
        if (progressSupport != null) {
            progressSupport.fireProgressListenerStart(this, type, count);
        }
    }

    private void fireProgressListenerStep() {
        if (progressSupport != null) {
            progressSupport.fireProgressListenerStep(this);
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

    private class ElementsCollection extends AbstractCollection<RefactoringElement> {

        @Override
        public Iterator<RefactoringElement> iterator() {
            return new Iterator() {

                private final Iterator<RefactoringElementImplementation> inner2 = SPIAccessor.DEFAULT.getFileChanges(bag).iterator();
                private int index = 0;

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }

                @Override
                @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
                public RefactoringElement next() {
                    if (index < internalList.size()) {
                        return new RefactoringElement(internalList.get(index++));
                    } else {
                        return new RefactoringElement(inner2.next());
                    }
                }

                @Override
                @SuppressWarnings("SleepWhileInLoop")
                public boolean hasNext() {
                    boolean hasNext = index < internalList.size();
                    if (hasNext) {
                        return hasNext;
                    }
                    while (!finished.get()) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        hasNext = index < internalList.size();
                        if (hasNext) {
                            return hasNext;
                        }
                    }
                    return index < internalList.size() || inner2.hasNext();
                }

            };

        }

        @Override
        public int size() {
            return internalList.size() + SPIAccessor.DEFAULT.getFileChanges(bag).size();
        }

    }

}
