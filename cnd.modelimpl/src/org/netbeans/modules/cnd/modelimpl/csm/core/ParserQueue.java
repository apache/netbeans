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
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 * A queue that hold a list of files to parse.
 */
public final class ParserQueue {

    /**
     * Entry position in the parser queue.
     */
    public static enum Position {

        /**
         * <code>IMMEDIATE</code> entries are parsed first, before
         * <code>HEAD</code> and <code>TAIL</code> entries. If there are
         * several <code>IMMEDIATE</code> entries, they are parsed
         * in the order of their insertion to the queue.
         */
        IMMEDIATE,
        /**
         * <code>HEAD</code> entries are parsed after <code>IMMEDIATE</code>,
         * and before <code>TAIL</code> entries. The are parsed in the
         * <em>reversed</em> order of their insertion to the queue.
         */
        HEAD,
        /**
         * <code>TAIL</code> entries are parsed last, after
         * <code>IMMEDIATE</code> and <code>HEAD</code> entries.
         * They are parsed in the order of their insertion to the queue.
         */
        TAIL
    }

    public static final class Entry implements Comparable<Entry> {

        private final FileImpl file;
        /** either PreprocHandler.State or Collection<PreprocHandler.State> */
        private Object ppState;
        private final Position position;
        private final int serial;

        private Entry(FileImpl file, Collection<PreprocHandler.State> ppStates, Position position, int serial) {
            if (TraceFlags.TRACE_PARSER_QUEUE) {
                System.err.println("creating entry for " + file.getAbsolutePath() + // NOI18N
                        " as " + tracePreprocStates(ppStates)); // NOI18N
            }
            this.file = file;
            if (ppStates.size() == 1) {
                this.ppState = ppStates.iterator().next();
            } else {
                this.ppState = new ArrayList<>(ppStates);
            }

            this.position = position;
            this.serial = serial;
        }

        public FileImpl getFile() {
            return file;
        }

        @SuppressWarnings("unchecked")
        public Collection<PreprocHandler.State> getPreprocStates() {
            Object state = ppState;
            if (state instanceof PreprocHandler.State || state == null) {
                return Collections.singleton((PreprocHandler.State) state);
            } else {
                return (Collection<PreprocHandler.State>) state;
            }
        }

        public Position getPosition() {
            return position;
        }

        @Override
        public String toString() {
            return toString(true);
        }

        public String toString(boolean detailed) {
            StringBuilder retValue = new StringBuilder();
            retValue.append("ParserQueue.Entry ").append(file).append(" of project ").append(file.getProject()); // NOI18N
            if (detailed) {
                retValue.append("\nposition: ").append(position); // NOI18N
                retValue.append(", serial: ").append(serial); // NOI18N
                retValue.append("\nwith PreprocStates:"); // NOI18N
                for (PreprocHandler.State state : getPreprocStates()) {
                    retValue.append('\n');
                    retValue.append(state);
                }
            }
            return retValue.toString();
        }

        @SuppressWarnings("unchecked")
        private synchronized void addStates(Collection<PreprocHandler.State> ppStates) {
            if (this.ppState instanceof PreprocHandler.State) {
                PreprocHandler.State oldState = (PreprocHandler.State) ppState;
                this.ppState = new ArrayList<>();
                if (oldState != FileImpl.DUMMY_STATE && oldState != FileImpl.PARTIAL_REPARSE_STATE) {
                    ((Collection<PreprocHandler.State>) this.ppState).add(oldState);
                } else {
                    if (TraceFlags.TIMING_PARSE_PER_FILE_FLAT) {
                        System.err.println("skip adding old dummy state");
                    }
                }
            }
            Collection<PreprocHandler.State> states = (Collection<PreprocHandler.State>) this.ppState;
            for (PreprocHandler.State state : ppStates) {
                if (state != FileImpl.DUMMY_STATE && state != FileImpl.PARTIAL_REPARSE_STATE) {
                    if (!states.contains(state)) {
                        states.add(state);
                    } else {
                        if (TraceFlags.TIMING_PARSE_PER_FILE_FLAT) {
                            System.err.println("array already has the state " + state);
                        }
                    }
                } else {
                    if (TraceFlags.TIMING_PARSE_PER_FILE_FLAT) {
                        System.err.println("skip adding dummy state");
                    }
                }
            }
            if (states.isEmpty()) {
                states.addAll(ppStates);
            }
        }

        private synchronized void setStates(Collection<PreprocHandler.State> ppStates) {
            // TODO: IZ#87204: AssertionError on _Bvector_base opening
            // review why it could be null
            // FIXUP: remove assert checks and update if statements to prevent NPE
            //            assert (ppState != null) : "why do pass null snapshot?";
            //            assert (this.ppState != null) : "if it was already included, where is the state?";

            if (TraceFlags.TRACE_PARSER_QUEUE) {
                System.err.println("setPreprocStateIfNeed for " + file.getAbsolutePath() + // NOI18N
                        " as " + tracePreprocStates(ppStates) + " with current " + tracePreprocStates(getPreprocStates())); // NOI18N
            }
            // we don't need check here - all logic is in ProjectBase.onFileIncluded
            this.ppState = new ArrayList<>(ppStates);
        }

        @Override
        public int compareTo(Entry that) {
            int cmp = this.position.compareTo(that.position);
            if (cmp == 0) {
                cmp = this.serial - that.serial;
                return this.position == Position.HEAD ? -cmp : cmp;
            } else {
                return cmp;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Entry) {
                return compareTo((Entry) obj) == 0;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + (this.position != null ? this.position.ordinal() : 0);
            hash = 97 * hash + this.serial;
            return hash;
        }
    }

    /**
     * Determines what to do with a file that is being added to the queue
     */
    public static enum FileAction {

        /**
         * Nothing should be done
         */
        NOTHING,
        /**
         * File should be marked as "addition parse needed", i.e.
         * FileImpl.markMoreParseNeeded() should be called
         */
        MARK_MORE_PARSE,
        /**
         * File should be marked as "reparse needed",
         * without invalidating the APT cache, i.e.
         * FileImpl.markReparseNeeded(false) should be called
         */
        MARK_REPARSE,
        /**
         * File should be marked as "reparse needed",
         * and APT cache should be invalidates, i.e.
         * FileImpl.markReparseNeeded(true) should be called
         */
        MARK_REPARSE_AND_INVALIDATE
    }

    /*package*/
    static String tracePreprocStates(Collection<PreprocHandler.State> ppStates) {
        StringBuilder sb = new StringBuilder('('); //NOI18N
        boolean first = false;
        for (PreprocHandler.State state : ppStates) {
            sb.append('(');
            if (!first) {
                sb.append(';');
            }
            first = false;
            sb.append(tracePreprocState(state));
            sb.append(')');
        }
        sb.append(')');
        return sb.toString();
    }

    public static String tracePreprocState(PreprocHandler.State ppState) {
        if (ppState == null) {
            return "null"; // NOI18N
        } else {
            StringBuilder msg = new StringBuilder("["); // NOI18N
            if (!ppState.isCleaned()) {
                msg.append("not"); // NOI18N
            }
            msg.append(" cleaned, "); // NOI18N
            if (!ppState.isValid()) {
                msg.append("not"); // NOI18N
            }
            msg.append(" valid, "); // NOI18N
            if (!ppState.isCompileContext()) {
                msg.append("not"); // NOI18N
            }
            msg.append(" correct State]"); // NOI18N
            return msg.toString();
        }

    }

    private static final class ProjectData {

        private final Set<FileImpl> filesInQueue = new HashSet<>();

        // there are no more simultaneously parsing files than threads, so LinkedList suites even better
        private final Collection<FileImpl> filesBeingParsed = new LinkedHashSet<>();
        private volatile int pendingActivity;
        ProjectData() {
            this.pendingActivity = 0;
        }

        public boolean isEmpty() {
            return filesInQueue.isEmpty() && filesBeingParsed.isEmpty();
        }

        public boolean noActivity() {
            return filesInQueue.isEmpty() && filesBeingParsed.isEmpty() && (pendingActivity == 0);
        }
        public int size() {
            return filesInQueue.size();
        }
    }

    private static enum State {

        ON, OFF, SUSPENDED
    }
    private static final ParserQueue instance = new ParserQueue(false);
    private final PriorityQueue<Entry> queue = new PriorityQueue<>();
    private volatile State state;
    private static final class SuspendLock {
        private final AtomicInteger counter = new AtomicInteger(0);
    }

    private final SuspendLock suspendLock = new SuspendLock();

    // do not need UIDs for ProjectBase in parsing data collection
    private final Map<ProjectBase, ProjectData> projectData = new HashMap<>();
    private final Map<CsmProject, ProjectWaitLatch> projectsAwaitLatches = new HashMap<>();
    private final AtomicInteger serial = new AtomicInteger(0);
    private static final class Lock {}
    private final Object lock = new Lock();
    private final boolean addAlways;
    private final Diagnostic.StopWatch stopWatch = TraceFlags.TIMING ? new Diagnostic.StopWatch(false) : null;
    private final Diagnostic.ProjectStat parseWatch = TraceFlags.TIMING ? new Diagnostic.ProjectStat() : null;
    private final Map<ProjectBase, AtomicInteger> onStartLevel = new HashMap<>();

    private ParserQueue(boolean addAlways) {
        this.addAlways = addAlways;
    }

    public static ParserQueue instance() {
        return instance;
    }

    public static ParserQueue testInstance() {
        return new ParserQueue(true);
    }

    private String traceState4File(FileImpl file, Set<FileImpl> files) {
        StringBuilder builder = new StringBuilder(" "); // NOI18N
        builder.append(file);
        builder.append("\n of project ").append(file.getProjectImpl(true)); // NOI18N
        builder.append("\n content of projects files set:\n"); // NOI18N
        if (files != null) {
            builder.append(files);
            builder.append("\nqueue content is:\n"); // NOI18N
            builder.append(toString(queue, false));
            builder.append("\nprojectData content is:\n"); // NOI18N
            builder.append(projectData);
        }
        return builder.toString();
    }

    /**
     * If file isn't yet enqueued, places it at the beginning of the queue,
     * otherwise moves it there
     */
    public void add(FileImpl file, PreprocHandler.State ppState, Position position) {
        add(file, Collections.singleton(ppState), position, true, FileAction.NOTHING);
    }

    /**
     * If file isn't yet enqueued, places it at the beginning of the queue,
     * otherwise moves it there
     */
    public void add(FileImpl file, Collection<PreprocHandler> ppHandlers, Position position) {
        assert ppHandlers != FileImpl.DUMMY_HANDLERS : "dummy handlers can not be added directly (only through shiftToBeParsedNext)";
        assert ppHandlers != FileImpl.PARTIAL_REPARSE_HANDLERS : "partial reparse handlers can not be added directly (only through addForPartialReparse)";
        Collection<PreprocHandler.State> ppStates = new ArrayList<>(ppHandlers.size());
        for (PreprocHandler handler : ppHandlers) {
            ppStates.add(handler.getState());
        }
        add(file, ppStates, position, true, FileAction.NOTHING);
    }

    /**
     * @param file
     * @return true if file was successfully added and placed in the head of parse queue
     */
    boolean addToBeParsedNext(FileImpl file) {
        return add(file, Collections.singleton(FileImpl.DUMMY_STATE), Position.IMMEDIATE, false, FileAction.NOTHING);
    }

    /**
     * @param file
     * @return true if file was successfully added to queue
     */
    boolean addForPartialReparse(FileImpl file) {
        return add(file, Collections.singleton(FileImpl.PARTIAL_REPARSE_STATE), Position.HEAD, false, FileAction.NOTHING);
    }

    /**
     * If file isn't yet enqueued, places it at the beginning of the queue,
     * otherwise moves it there
     * @return true if file was added into queue
     */
    public boolean add(FileImpl file, Collection<PreprocHandler.State> ppStates, Position position,
            boolean clearPrevState, FileAction fileAction) {
        if (ProjectBase.WAIT_PARSE_LOGGER.isLoggable(Level.FINE)) {
            ProjectBase.WAIT_PARSE_LOGGER.log(Level.FINE, String.format("##> ParserQueue.add %s %d", file, System.currentTimeMillis()), new Exception());
        }
        try {
            return addImpl(file, ppStates, position, clearPrevState, fileAction);
        } finally {
            if (ProjectBase.WAIT_PARSE_LOGGER.isLoggable(Level.FINE)) {
                ProjectBase.WAIT_PARSE_LOGGER.fine(String.format("##< ParserQueue.add %s %d", file.getAbsolutePath(), System.currentTimeMillis()));
            }
        }
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    private boolean addImpl(FileImpl file, Collection<PreprocHandler.State> ppStates, Position position,
            boolean clearPrevState, FileAction fileAction) {
        if (TraceFlags.TRACE_182342_BUG) {
            new Exception("ParserQueue: add for " + file).printStackTrace(System.err);  // NOI18N
            int i = 0;
            for (PreprocHandler.State aState : ppStates) {
                System.err.printf("ParserQueue: State %d from original %s\n", i++, aState);
            }
        }
        if (ppStates.isEmpty()) {
            ProjectBase pi = file.getProjectImpl(false);
            if (pi != null && !pi.isDisposing()) {
                Utils.LOG.log(Level.SEVERE, "Adding a file {0} with an emty preprocessor state set", file.getAbsolutePath()); //NOI18N
            } else {
                if (pi != null) {
                    pi.removeModifiedFile(file);
                }
                return false;
            }
        }
        assert state != null;
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("ParserQueue: add " + file.getAbsolutePath() + " as " + position); // NOI18N
        }
        boolean newEntry = false;
        synchronized (lock) {
            if (state == State.OFF) {
                return false;
            }
            switch (fileAction) {
                case MARK_MORE_PARSE:
                    file.markMoreParseNeeded();
                    break;
                case MARK_REPARSE:
                    file.markReparseNeeded(false);
                    break;
                case MARK_REPARSE_AND_INVALIDATE:
                    file.markReparseNeeded(true);
                    break;
            }
            ProjectBase pi = file.getProjectImpl(false);
            if (pi != null) {
                pi.removeModifiedFile(file);
            }
            if (!needEnqueue(file)) {
                if (TraceFlags.TRACE_PARSER_QUEUE) {
                    System.err.println("ParserQueue: do not add parsing or parsed " + file.getAbsolutePath()); // NOI18N
                }
                return false;
            }
            if (queue.isEmpty()) {
                serial.set(0);
            }
            Set<FileImpl> files = getProjectFiles(file.getProjectImpl(true));
            Entry entry = null;
            boolean addEntry = false;
            if (files.contains(file)) {
                entry = findEntry(file); //TODO: think over / profile, probably this line is expensive
                if (entry == null) {
                    FileImpl findFile = null;
                    for (FileImpl aFile : files) {
                        if (aFile.equals(file)) {
                            findFile = aFile;
                        }
                    }
                    if (findFile == file) {
                        CndUtils.assertTrue(false, "ProjectData contains file " + file + ", but there is no matching entry in the queue"); // NOI18N
                    } else {
                        CndUtils.assertTrue(false, "ProjectData contains another instance of file " + file + ", so there is no matching entry in the queue"); // NOI18N
                    }
                    System.err.println(traceState4File(file, files));
                    System.err.println(traceState4File(findFile, null));
                } else {
                    if (clearPrevState) {
                        entry.setStates(ppStates);
                    } else {
                        entry.addStates(ppStates);
                    }
                    if (file != entry.file) {
                        // Replace old file instance by new
                        queue.remove(entry);
                        entry = new Entry(file, entry.getPreprocStates(), position, serial.incrementAndGet());
                        addEntry = true;
                    } else if (position.compareTo(entry.getPosition()) < 0) {
                        queue.remove(entry);
                        entry = new Entry(file, entry.getPreprocStates(), position, serial.incrementAndGet());
                        addEntry = true;
                    }
                }
            } else {
                assert (findEntry(file) == null) : "The queue should not contain the file " + traceState4File(file, files); // NOI18N
                files.add(file);
                newEntry = true;
            }
            if (entry == null) {
                entry = new Entry(file, ppStates, position, serial.incrementAndGet());
                addEntry = true;
            }
            if (addEntry) {
                queue.add(entry);
                if (TraceFlags.TRACE_PARSER_QUEUE) {
                    System.err.println("ParserQueue: added entry " + entry.toString(TraceFlags.TRACE_PARSER_QUEUE_DETAILS)); // NOI18N
                }
            }
            lock.notifyAll();
        }
        ProgressSupport.instance().fireFileInvalidated(file);
        if (newEntry) {
            ProgressSupport.instance().fireFileAddedToParse(file);
        }
        return true;
    }

    public void waitReady() throws InterruptedException {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("ParserQueue: waitReady() ..."); // NOI18N
        }
        synchronized (lock) {
            while (findFirstNotBeeingParsedEntry(false) == null && state != State.OFF) {
                lock.wait();
            }
        }
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("ParserQueue: waiting finished"); // NOI18N
        }
    }

    public void suspend() {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("ParserQueue: suspending"); // NOI18N
        }
        synchronized (suspendLock) {
            suspendLock.counter.incrementAndGet();
            state = State.SUSPENDED;
        }
    }

    public void resume() {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("ParserQueue: resuming"); // NOI18N
        }
        synchronized (suspendLock) {
            if (suspendLock.counter.decrementAndGet() == 0) {
                state = State.ON;
                suspendLock.notifyAll();
            }
        }
    }

    private Entry findFirstNotBeeingParsedEntry(boolean removeFoundEntryFromQueue) {
        Entry e = null;
        FileImpl file = null;
        ProjectData data = null;
        ProjectBase project = null;
        Iterator<Entry> iterator = queue.iterator();
        // 'poll' that filters out files that are being parsed.
        // Used to prevent parsing the same file from different threads at the same time.
        while (true) {
            if (!iterator.hasNext()) {
                return null;
            }
            e = iterator.next();
            file = e.getFile();
            project = file.getProjectImpl(true);
            data = getProjectData(project, true);
            if (data.filesBeingParsed.contains(file)) {
                if (TraceFlags.TRACE_PARSER_QUEUE) {
                    System.err.println(Thread.currentThread().getName() + ": beeing parsed by another thread " + file); // NOI18N
                }
            } else {
                if (removeFoundEntryFromQueue) {
                    iterator.remove();
                }
                break;
            }
        }
        return e;
    }

    public Entry poll() throws InterruptedException {

        synchronized (suspendLock) {
            while (state == State.SUSPENDED) {
                if (TraceFlags.TRACE_PARSER_QUEUE) {
                    System.err.println("ParserQueue: waiting for resume"); // NOI18N
                }
                suspendLock.wait();
            }
        }

        Entry e = null;

        ProjectBase project;
        boolean lastFileInProject;

        FileImpl file = null;

        ProjectData data;
        synchronized (lock) {
            e = findFirstNotBeeingParsedEntry(true);
            if (e == null) {
                return null;
            }
            file = e.getFile();
            project = file.getProjectImpl(true);
            data = getProjectData(project, true);
            data.filesInQueue.remove(file);
            data.filesBeingParsed.add(file);
            lastFileInProject = markLastProjectFileActivityIfNeeded(data);
            if (TraceFlags.TIMING && stopWatch != null && !stopWatch.isRunning()) {
                stopWatch.start();
                System.err.println("=== Starting parser queue stopwatch " + project.getName() + " (" + project.getFileContainerSize() + " files)"); // NOI18N
            }
        }
        // TODO: think over, whether this should be under if( notifyListeners
        ProgressSupport.instance().fireFileParsingStarted(file);
        if (lastFileInProject) {
            handleLastProjectFile(project, data);
        }
        if (TraceFlags.TRACE_PARSER_QUEUE_POLL) {
            System.err.printf("ParserQueue: polling %s with %d states in thread %s%n", // NOI18N
                    e.getFile().getAbsolutePath(), e.getPreprocStates().size(), Thread.currentThread().getName());
        }
        return e;
    }

    public void remove(FileImpl file) {

        ProjectBase project;
        boolean lastFileInProject = false;
        ProjectData data;
        synchronized (lock) {
            project = file.getProjectImpl(true);
            data = getProjectData(project, true);
            if (data.filesInQueue.contains(file)) {
                //queue.remove(file); //TODO: think over / profile, probably this line is expensive
                Entry e = findEntry(file);//TODO: think over / profile, probably this line is expensive
                if (e != null) {
                    queue.remove(e);
                }
                data.filesInQueue.remove(file);
                lastFileInProject = markLastProjectFileActivityIfNeeded(data);
            }
        }

        if (lastFileInProject) {
            handleLastProjectFile(project, data);
        }
    }

    public void shutdown() {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("ParserQueue: clearing"); // NOI18N
        }
        Collection<ProjectBase> copiedProjects = null;
        synchronized (lock) {
            state = State.OFF;
            queue.clear();
            copiedProjects = new ArrayList<>(projectData.keySet());
            lock.notifyAll();
        }
        for (ProjectBase prj : copiedProjects) {
            ProgressSupport.instance().fireProjectParsingFinished(prj);
        }
        clearParseWatch();
    }

    public void startup() {
        state = State.ON;
    }

    void clearParseWatch() {
        if (parseWatch != null) {
            parseWatch.clear();
        }
    }

    void addParseStatistics(ProjectBase project, FileImpl file, long parseTime) {
        if (parseWatch != null) {
            parseWatch.addParseFileStatistics(project, file, parseTime);
        }
    }

    public void removeAll(ProjectBase project) {
        ProjectData data;
        boolean lastFileInProject;
        synchronized (lock) {
            data = _clean(project);
            lastFileInProject = markLastProjectFileActivityIfNeeded(data);
        }
        if (lastFileInProject) {
            handleLastProjectFile(project, data);
        }
    }

    /**
     * Clean query without any notifications.
     * Used for recreate project query after error recovery.
     */
    public void clean(ProjectBase project) {
        synchronized (lock) {
            _clean(project);
        }
    }

    private ProjectData _clean(ProjectBase project) {
        ProjectData data = getProjectData(project, true);
        for (Object file : data.filesInQueue) {
            Entry e = findEntry((FileImpl) file);
            queue.remove(e);
        }
        data.filesInQueue.clear();
        return data;
    }
    /**
     * Determines whether any files of the given project are now being parsed
     * @return true if any files of the project are being parsed, otherwise false
     */
    public boolean isParsing(ProjectBase project) {
        synchronized (lock) {
            ProjectData data = getProjectData(project, false);
            if (data != null) {
                return !data.filesBeingParsed.isEmpty();
            }
        }
        return false;
    }

    public boolean hasPendingProjectRelatedWork(ProjectBase project, FileImpl skipFile) {
        return getPendingProjectRelatedLatch(project, skipFile) != null;
    }

    private ProjectWaitLatch getPendingProjectRelatedLatch(ProjectBase project, FileImpl skipFile) {
        synchronized (lock) {
            ProjectWaitLatch latch = null;
            boolean hasProjectActivity;
            ProjectData data = getProjectData(project, false);
            if (data == null || data.noActivity()) {
                // nothing in queue and nothing in progress => no files
                hasProjectActivity = false;
            } else {
                if (skipFile == null) {
                    // not empty, but nothing to skip => has files
                    hasProjectActivity = true;
                } else {
                    if (data.filesBeingParsed.contains(skipFile) ||
                            data.filesInQueue.contains(skipFile)) {
                        hasProjectActivity = (data.filesBeingParsed.size() + data.filesInQueue.size() + data.pendingActivity) > 1;
                    } else {
                        hasProjectActivity = !data.noActivity();
                    }
                }
            }
            if (hasProjectActivity) {
                synchronized (projectsAwaitLatches) {
                    latch = projectsAwaitLatches.get(project);
                    if (latch == null) {
                        latch = new ProjectWaitLatch();
                        if (TraceFlags.TRACE_CLOSE_PROJECT) {
                            Utils.LOG.log(Level.WARNING, "Adding a latch {0} for {1}:{2}", new Object[] { System.identityHashCode(latch), project.getName(), System.identityHashCode(project)});
                        }
                        projectsAwaitLatches.put(project, latch);
                    } else {
                        if (TraceFlags.TRACE_CLOSE_PROJECT) {
                            Utils.LOG.log(Level.WARNING, "Reuse latch {0} for {1}:{2}", new Object[]{System.identityHashCode(latch), project.getName(), System.identityHashCode(project)});
                        }
                    }
                }
            }
            return latch;
        }
    }

    private Set<FileImpl> getProjectFiles(ProjectBase project) {
        return getProjectData(project, true).filesInQueue;
    }

    private void createProjectDataIfNeeded(ProjectBase project) {
        getProjectData(project, true);
    }

    private ProjectData getProjectData(ProjectBase project, boolean create) {
        // must be in synchronized( lock ) block
        synchronized (lock) {
            ProjectBase key = project;
            ProjectData data = projectData.get(key);
            if (data == null && create) {
                data = new ProjectData();
                projectData.put(key, data);
            }
            return data;
        }
    }

    private boolean needEnqueue(FileImpl file) {
        // with multiple parse we can not check parsed state
        return !file.getProjectImpl(true).isDisposing() || addAlways;
    }

    public void onStartAddingProjectFiles(ProjectBase project) {
        suspend();
        createProjectDataIfNeeded(project);
        boolean fire;
        synchronized(onStartLevel) {
            AtomicInteger level = onStartLevel.get(project);
            if (level == null) {
                level = new AtomicInteger();
                onStartLevel.put(project, level);
            }
            fire = level.incrementAndGet() == 1;
        }
        if (fire) {
            ProgressSupport.instance().fireProjectParsingStarted(project);
        }
    }

    public void onEndAddingProjectFiles(ProjectBase project) {
        boolean fire;
        synchronized(onStartLevel) {
            AtomicInteger level = onStartLevel.get(project);
            if (level == null) {
                assert false : "Not balanced start/end adding in project"; // NOI18N
                level = new AtomicInteger(1);
                onStartLevel.put(project, level);
            }
            fire = level.decrementAndGet() == 0;
            if (fire) {
                onStartLevel.remove(project);
            }
        }
        if (fire) {
            ProjectData pd;
            boolean noFiles;
            synchronized (lock) {
                pd = getProjectData(project, true);
                noFiles = markLastProjectFileActivityIfNeeded(pd);
            }
            ProgressSupport.instance().fireProjectFilesCounted(project, pd.filesInQueue.size());
            if (noFiles) {
                handleLastProjectFile(project, pd);
            }
        }
        resume();
    }

    /*package*/ void onFileParsingFinished(FileImpl file) {
        boolean lastFileInProject;
        boolean idle = false;
        ProjectBase project;
        ProjectData data;
        synchronized (lock) {
            project = file.getProjectImpl(true);
            data = getProjectData(project, true);
            data.filesBeingParsed.remove(file);
            lastFileInProject = markLastProjectFileActivityIfNeeded(data);
            if (lastFileInProject) {
                idle = projectData.isEmpty();
                // this work only for a single project
                // but on the other hand in the case of multiple projects such measuring will never work
                // since project files might be shuffled in queue
                if (TraceFlags.TIMING && stopWatch != null && stopWatch.isRunning()) {
                    stopWatch.stopAndReport("=== Stopping parser queue stopwatch " + project.getName() + " (" + project.getFileContainerSize() + " files): \t"); // NOI18N
                    if (parseWatch != null) {
                        parseWatch.traceProjectData(project);
                    }
                }
            }
            lock.notifyAll();
        }
        ProgressSupport.instance().fireFileParsingFinished(file);
        if (lastFileInProject) {
            if (TraceFlags.TRACE_CLOSE_PROJECT) {
                System.err.println("Last file in project " + project.getName() + " (" + project.getFileContainerSize() + " files)"); // NOI18N
            }
            handleLastProjectFile(project, data);
            if (idle) {
                ProgressSupport.instance().fireIdle();
            }
        }
    }

    private boolean markLastProjectFileActivityIfNeeded(ProjectData data) {
        if (data.isEmpty()) {
            data.pendingActivity++;
            return true;
        }
        return false;
    }

    private void handleLastProjectFile(ProjectBase project, ProjectData data) {
        project.onParseFinish();
        boolean last = false;
        synchronized (lock) {
            data.pendingActivity--;
            if (data.noActivity()) {
                projectData.remove(project);
                last = true;
            }
        }
        if (last) {
            // notify all "wait" empty listeners
            notifyWaitEmpty(project);
            project.notifyOnWaitParseLock();
            ProgressSupport.instance().fireProjectParsingFinished(project);
        }
    }

    private void notifyWaitEmpty(ProjectBase project) {
        synchronized (projectsAwaitLatches) {
            if (TraceFlags.TRACE_CLOSE_PROJECT) {
                Utils.LOG.log(Level.WARNING, "notifyWaitEmpty for {0}:{1}", new Object[] {project.getName(), System.identityHashCode(project)});
            }
            CountDownLatch latch = projectsAwaitLatches.remove(project);
            if (latch != null) {
                if (TraceFlags.TRACE_CLOSE_PROJECT) {
                    Utils.LOG.log(Level.WARNING, "notifyWaitEmpty on latch {0} for {1}:{2}", new Object[] { System.identityHashCode(latch), project.getName(), System.identityHashCode(project)});
                }
                latch.countDown();
            }
        }
    }

    private static final class ProjectWaitLatch extends CountDownLatch {
        public ProjectWaitLatch() {
            super(1);
        }
    }

    /*package*/ void waitEmpty(ProjectBase project) {
        if (TraceFlags.TRACE_CLOSE_PROJECT) {
            System.err.println("Waiting Empty Project " + project.getName()); // NOI18N
        }
        CountDownLatch latch;
        while ((latch = getPendingProjectRelatedLatch(project, null)) != null) {
            if (TraceFlags.TRACE_CLOSE_PROJECT) {
                System.err.println("Waiting Empty Project 2 " + project.getName()); // NOI18N
            }
            try {
                latch.await();
            } catch (InterruptedException ex) {
                // nothing
            }
        }
        if (TraceFlags.TRACE_CLOSE_PROJECT) {
            System.err.println("Finished waiting on Empty Project " + project.getName()); // NOI18N
        }
    }

    public long getStopWatchTime() {
        return TraceFlags.TIMING ? stopWatch.getTime() : -1;
    }

    private String toString(PriorityQueue<Entry> queue, boolean detailed) {
        StringBuilder builder = new StringBuilder();
        for (Entry e : queue) {
            builder.append(e.toString(detailed)).append("\n"); // NOI18N
        }
        return builder.toString();
    }

    private Entry findEntry(FileImpl file) {
//        return fileEntry.get(file);
        int fileHashCode = file.hashCode();
        for (Entry e : queue) {
            FileImpl f = e.getFile();
            if (f == file) {
                return e;
            }
            if (fileHashCode == f.hashCode()) {
                if (file.equals(f)) {
                    return e;
                }
            }
        }
        return null;
    }
}
