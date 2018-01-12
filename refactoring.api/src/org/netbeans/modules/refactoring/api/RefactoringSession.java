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
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeLookup;
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

    private static final int COMMITSTEPS = 100;

    private final ArrayList<RefactoringElementImplementation> internalList = new ArrayList<>();
    private final Collection<RefactoringElement> refactoringElements = new ElementsCollection();
    private final UndoManager undoManager = UndoManager.getDefault();
    private final AtomicBoolean finished = new AtomicBoolean(false);
    private final RefactoringElementsBag bag;
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
        return Utilities.runWithOnSaveTasksDisabled(() -> reallyDoRefactoring(saveAfterDone));
    }

    private Problem reallyDoRefactoring(boolean saveAfterDone) {
        final long start = System.currentTimeMillis();
        Iterator it = internalList.iterator();
        ArrayList<Transaction> commits = SPIAccessor.DEFAULT.getCommits(bag);
        float progressStep = (float) COMMITSTEPS / internalList.size();
        float current = 0F;
        fireProgressListenerStart(0, COMMITSTEPS + commits.size() * COMMITSTEPS + 1);
        ProgressListener progressListener = new ProgressL(commits, COMMITSTEPS);
        if (realcommit) {
            undoManager.transactionStarted();
            undoManager.setUndoDescription(description);
        }
        try {
            try {
                while (it.hasNext()) {
                    RefactoringElementImplementation element = (RefactoringElementImplementation) it.next();
                    changeRefactoringElement(element, true);
                    current += progressStep;
                    fireProgressListenerStep((int) current);
                }
            } finally {
                commits.forEach(commit -> SPIAccessor.DEFAULT.check(commit, false));
                UndoableWrapper undoableWrapper = MimeLookup.getLookup("").lookup(UndoableWrapper.class);
                commits.forEach(commit -> {
                    if (undoableWrapper != null) {
                        undoableWrapper.setActive(true, this);
                    }
                    if (commit instanceof ProgressProvider) {
                        ProgressProvider progressProvider = (ProgressProvider) commit;
                        progressProvider.addProgressListener(progressListener);
                        try {
                            commit.commit();
                        } finally {
                            progressProvider.removeProgressListener(progressListener);
                        }
                    } else {
                        commit.commit();
                    }
                    if (undoableWrapper != null) {
                        undoableWrapper.setActive(false, null);
                    }
                });
                if (undoableWrapper != null) {
                    undoableWrapper.close();
                }
                commits.forEach(commit -> SPIAccessor.DEFAULT.sum(commit));
            }
            if (saveAfterDone) {
                LifecycleManager.getDefault().saveAll();
                for (DataObject dataObject : DataObject.getRegistry().getModified()) {
                    SaveCookie cookie = dataObject.getLookup().lookup(SaveCookie.class);
                    try {
                        cookie.save();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            SPIAccessor.DEFAULT.getFileChanges(bag).stream().filter(fileChange -> fileChange.isEnabled()).forEachOrdered(fileChange -> fileChange.performChange());
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
    }

    private class ProgressL implements ProgressListener {

        private float progressStep;
        private float current;
        private final ArrayList<Transaction> commits;
        private final int start;

        ProgressL(ArrayList<Transaction> commits, int start) {
            this.commits = commits;
            this.start = start;
        }

        @Override
        public void start(ProgressEvent event) {
            progressStep = (float) COMMITSTEPS / event.getCount();
            current = start + commits.indexOf(event.getSource()) * COMMITSTEPS;
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
        return Utilities.runWithOnSaveTasksDisabled(() -> reallyUndoRefactoring(saveAfterDone));
    }

    private Problem reallyUndoRefactoring(boolean saveAfterDone) {
        try {
            ListIterator<RefactoringElementImplementation> internalListIterator = internalList.listIterator(internalList.size());
            fireProgressListenerStart(0, internalList.size() + 1);
            ArrayList<RefactoringElementImplementation> fileChanges = SPIAccessor.DEFAULT.getFileChanges(bag);
            ArrayList<Transaction> commits = SPIAccessor.DEFAULT.getCommits(bag);
            for (ListIterator<RefactoringElementImplementation> fileChangeIterator = fileChanges.listIterator(fileChanges.size()); fileChangeIterator.hasPrevious();) {
                RefactoringElementImplementation f = fileChangeIterator.previous();
                if (f.isEnabled()) {
                    f.undoChange();
                }
            }
            SPIAccessor.DEFAULT.getCommits(bag).forEach(commit -> SPIAccessor.DEFAULT.check(commit, true));
            UndoableWrapper undoableWrapper = MimeLookup.getLookup("").lookup(UndoableWrapper.class);
            for (ListIterator<Transaction> commitIterator = commits.listIterator(commits.size()); commitIterator.hasPrevious();) {
                final Transaction commit = commitIterator.previous();
                undoableWrapper.setActive(true, this);
                commit.rollback();
                undoableWrapper.setActive(false, null);
            }
            undoableWrapper.close();
            SPIAccessor.DEFAULT.getCommits(bag).forEach((commit) -> SPIAccessor.DEFAULT.sum(commit));
            while (internalListIterator.hasPrevious()) {
                fireProgressListenerStep();
                RefactoringElementImplementation element = internalListIterator.previous();
                changeRefactoringElement(element, false);
            }
            if (saveAfterDone) {
                LifecycleManager.getDefault().saveAll();
            }
            fireProgressListenerStep();
        } finally {
            fireProgressListenerStop();
        }
        return null;
    }

    private void changeRefactoringElement(RefactoringElementImplementation element, boolean change) {
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
                public RefactoringElement next() {
                    if (index < internalList.size()) {
                        RefactoringElementImplementation implementation = internalList.get(index);
                        ++index;
                        return new RefactoringElement(implementation);
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
