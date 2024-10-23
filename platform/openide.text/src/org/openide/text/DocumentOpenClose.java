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
package org.openide.text;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

import org.openide.awt.UndoRedo;
import org.openide.cookies.EditorCookie;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.UserQuestionException;

/**
 * Processing of document open/close in a dedicated RequestProcessor.
 *
 * @author Miloslav Metelka
 */
final class DocumentOpenClose {

    // RP should have throughput 1 so that closeDocument() followed by openDocument() are ordered properly
    // and also openDocument() called from close() notification behaves as expected.
    static final RequestProcessor RP = new RequestProcessor("org.openide.text Document Processing", 1, false, false);
    
    static String getSimpleName(Object o) {
        return (o != null)
                ? o.getClass().getSimpleName() + "@" + System.identityHashCode(o) // NOI18N
                : "null"; // NOI18N
    }
    
    /**
     * Number of milliseconds to wait before processing a close that resulted
     * from GC of a document.
     * The delay prevents a frequent repetitive closing/opening loop in case a listener
     * on EditorCookie.Observable notified about document close would synchronously request 
     * document (re)opening.
     */
    private static final int NULL_DOCUMENT_CLOSE_DELAY = 1000;
    
    private static final Logger LOG = CloneableEditorSupport.ERR;
    
    final CloneableEditorSupport ces;
    
    final Object lock;
    
    /**
     * Current status of the document.
     */
    DocumentStatus documentStatus = DocumentStatus.CLOSED;

    DocumentLoad activeOpen;

    RequestProcessor.Task activeOpenTask;
    
    DocumentClose activeClose;
    
    RequestProcessor.Task activeCloseTask;

    /**
     * Grabbing of info in EDT precedes actual reload and schedules reload task.
     */
    Runnable preReloadEDT;
    
    /**
     * Possible pending reload.
     * It's always beyond possible activeOpen task in the RP since for closed
     * document there's no reload.
     */
    DocumentLoad activeReload;
    
    RequestProcessor.Task activeReloadTask;
    
    /**
     * Reference to an open document (or null).
     * Opening process initializes this variable and if there are any clients
     * that work with the document the reference will remain valid.
     * If there are no clients the reference will GCed which will trigger automatic
     * closing.
     */
    DocumentRef docRef;
    
    final Object docRefLock;
    
    /**
     * Strong reference to document is used when document becomes modified.
     */
    StyledDocument strongDocRef;
    
    boolean firingCloseDocument;
    
    StyledDocument docOpenedWhenFiringCloseDocument;

    DocumentOpenClose(CloneableEditorSupport ces) {
        this.ces = ces;
        this.lock = ces.getLock();
        this.docRefLock = new Object();
    }

    public DocumentStatus getDocumentStatusLA() { // Lock acquired mandatory
        return documentStatus;
    }

    void setDocumentStatusLA(DocumentStatus documentStatus) {
        this.documentStatus = documentStatus;
    }
    
    StyledDocument getDocument() {
        // Do not sync on "lock" since CND model calls getDocument()
        // during notifyModify() which gets rescheduled from EDT
        // (which already holds CES.getLock()) into non-EDT which would lead to starvation.
        return getRefDocument();
    }

    /**
     * Get document that is currently open or it's being loaded or reloaded.
     *
     * @return document instance from docRef.
     */
    StyledDocument getRefDocument() {
        synchronized (docRefLock) {
            return (docRef != null) ? docRef.get() : null;
        }
    }
    
    void setDocRef(StyledDocument doc) {
        synchronized (docRefLock) {
            docRef = (doc != null) ? new DocumentRef(doc) : null;
        }
    }
    
    void setDocumentStronglyReferenced(boolean stronglyReferenced) {
        if (stronglyReferenced) {
            StyledDocument doc = getRefDocument();
            // doc should be non-null although the following assert statement
            // was triggered from CES.setAlreadyModified() in #240075
            // (probably there was an explicit close operation while an action was running).
//            assert (doc != null) : "Null doc cannot be strongly referenced."; // NOI18N
            strongDocRef = doc;
        } else {
            strongDocRef = null;
        }
    }
    
    StyledDocument open() throws IOException {
        DocumentLoad load;
        Task task;
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "open() requested by", new Exception());
        }
        synchronized (lock) {
            StyledDocument openDoc = retainExistingDocLA();
            if (openDoc != null) {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("open(): Existing openDoc retained.\n"); // NOI18N
                }
                return openDoc;
            }
            switch (documentStatus) {
                case OPENED:
                    // Doc was null (retainDocLA() failed) but automatic close()
                    // due to docRef GC might already be scheduled or not yet.
                    // Anyway ensure closing task gets scheduled before opening task (by passing false).
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.finer("open(): status OPENED but doc GCed. Schedule close task followed by possible open task\n"); // NOI18N
                    }
                    closeImplLA(null, false);
                    if (activeOpen == null) {
                        initLoadTaskLA();
                    }
                    load = activeOpen;
                    task = activeOpenTask;
                    break;
                case CLOSED:
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.finer("open(): status CLOSED. Schedule a synchronous open task\n"); // NOI18N
                    }
                    if (activeOpen == null) {
                        initLoadTaskLA();
                    }
                    load = activeOpen;
                    task = activeOpenTask;
                    break;
                case RELOADING:
                    load = activeReload;
                    task = activeReloadTask;
                    break;
                case LOADING:
                    load = activeOpen;
                    task = activeOpenTask;
                    break;
                default:
                    throw invalidStatus();
            }
        }
        if (load == null || task == null) {
            LOG.info("load=" + load + ", task=" + task + ", this: " + this); // Will throw NPE so dump state
        }
        // Thread may be RP thread in case CES.openDocument() is called synchronously
        // from fireDocumentChange() upon document close in which case
        // task.waitFinished() will run the task synchronously.
        task.waitFinished();
        if (load.loadIOException != null) {
            throw load.loadIOException;
        }
        if (load.loadRuntimeException != null) {
            throw load.loadRuntimeException;
        }
        return load.loadDoc;
    }
    
    Task openTask() {
        synchronized (lock) {
            final StyledDocument existingDoc = retainExistingDocLA();
            if (existingDoc != null) {
                Task existingDocTask = new Task(new Runnable() {
                    private final StyledDocument doc = existingDoc; // Hold ref to doc in returned task
                    public void run() {
                    }
                });
                existingDocTask.run();
                return existingDocTask;
            }
            if (activeOpenTask != null) {
                return activeOpenTask;
            }
            switch (documentStatus) {
                case OPENED:
                    // Doc was null (retainDocLA() failed) but automatic close()
                    // due to docRef GC might already be scheduled or not yet.
                    // Anyway ensure closing task gets scheduled before opening task (by passing false).
                    closeImplLA(null, false);
                    initLoadTaskLA();
                    break;
                case CLOSED:
                    initLoadTaskLA();
                    break;
                case RELOADING:
                    return activeReloadTask;
                case LOADING:
                    assert (activeOpenTask != null);
                    break;
                default:
                    throw invalidStatus();
            }
            return activeOpenTask;
        }
    }
    
    private void waitForCloseFinish() {
        Task closeTask;
        synchronized (lock) {
            closeTask = activeCloseTask;
        }
        // Must wait for finishing outside of "lock" otherwise deadlock with close task processing
        if (closeTask != null) {
            closeTask.waitFinished();
        }
    }
    
    boolean isDocumentLoadedOrLoading() {
        // Close used to be synchronous so wait for any pending close task for compatibility
        waitForCloseFinish();

        synchronized (lock) {
            switch (documentStatus) {
                case CLOSED:
                    return false;
                case RELOADING:
                case LOADING:
                case OPENED:
                    return true;
                default:
                    throw invalidStatus();

            }
        }
    }

    boolean isDocumentOpened() {
        // Close used to be synchronous so wait for any pending close task for compatibility
        waitForCloseFinish();

        synchronized (lock) {
            switch (documentStatus) {
                case CLOSED:
                case RELOADING:
                case LOADING:
                    return false;
                case OPENED:
                    return true;
                default:
                    throw invalidStatus();

            }
        }
    }

    Task reloadTask() {
        // Return either currently shceduled reload task or an empty task
        // if there's no reload scheduled.
        synchronized (lock) {
            if (activeReloadTask != null) {
                return activeReloadTask;
            }
            return Task.EMPTY;
        }
    }

    void reload(JEditorPane[] openedPanes) { // Schedule a reload in RP
        Runnable reloadEDTTask = null;
        synchronized (lock) {
            switch (documentStatus) {
                case CLOSED: // Closed and loading not started yet -> do nothing
                    break;
                case RELOADING: // Reload already pending
                    break;
                case LOADING:
                case OPENED:
                    if (activeClose == null && activeReload == null) { // Only reload when no pending close or reload
                        StyledDocument reloadDoc = docRef.get();
                        if (reloadDoc != null) {
                            // Init the task but do not start it because "lock" is acquired
                            initReloadTaskLA(reloadDoc, openedPanes);
                            reloadEDTTask = activeReload;
                        }
                    }
                    break;

                default:
                    throw invalidStatus();

            }
        }

        if (reloadEDTTask != null) {
            // Initial part of reload runs in EDT (collects caret positions) but outside "lock"
            Mutex.EVENT.readAccess(reloadEDTTask);
        }
    }   
    
    private StyledDocument retainExistingDocLA() { // Lock acquired mandatory
        switch (documentStatus) {
            case CLOSED:
                break;
            case RELOADING:
            case LOADING:
                cancelCloseLA(); // Cancel possible closing
                break;

            case OPENED:
                StyledDocument openDoc = getRefDocument();
                if (openDoc != null) { // Still opened
                    // Check if a close attempt is not active and possibly cancel it
                    cancelCloseLA();
                    if (activeClose == null) {
                        return openDoc;
                    }
                }
                break;

            default:
                throw invalidStatus();

        }
        return null;
    }
    
    private void initLoadTaskLA() { // Lock acquired mandatory
        if (activeOpen != null) {
            throw new IllegalStateException("Open task already inited. State:\n" + this); // NOI18N
        }
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("initLoadTaskLA(): Schedule open task followed by change firing task.\n"); // NOI18N
        }
        activeOpen = new DocumentLoad();
        activeOpenTask = RP.create(activeOpen);
        // Btw RP task runs synchronously when waitFinished() is called from RP thread
        // so openDocument() done from closeDocument() processing (in RP thread)
        // is handled in the same way like a regular open from non-RP thread.
        activeOpenTask.schedule(0);
        // In addition to activeOpenTask schedule a DocumentOpenFire task so that
        // activeOpenTask gets truly finished before actual firing gets done.
        RP.create(new DocumentOpenFire(activeOpen)).schedule(0);
    }
    
    private void initReloadTaskLA(StyledDocument reloadDoc, JEditorPane[] openedPanes) { // Lock acquired mandatory
        assert (activeReload == null) : "Reload task already inited."; // NOI18N
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("initLoadTaskLA(): Schedule reload task.\n"); // NOI18N
        }
        activeReload = new DocumentLoad(reloadDoc, openedPanes);
    }
    
    void close() {
        synchronized (lock) {
            StyledDocument doc = getRefDocument();
            closeImplLA(doc, doc == null);
        }
    }
    
    void closeImplLA(StyledDocument doc, boolean delayedClose) { // Lock acquired mandatory
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "Close requested by:\n", new Exception()); // NOI18N
        }
        if (activeClose != null) {
            // If immediate closing is necessary possibly reschedule
            if (!delayedClose && activeClose.delayedClose) {
                cancelCloseLA();
                if (activeClose != null) { // Close already running and can't be cancelled
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.finer("closeImplLA(): Delayed active close already running (can't be cancelled). Return.\n"); // NOI18N
                    }
                    return;
                }
            } else { // Let existing close requrest finish
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("closeImplLA(): Close already in progress. Return.\n"); // NOI18N
                }
                return;
            }
        }
        assert (activeClose == null);
        activeClose = new DocumentClose(doc, delayedClose);
        activeCloseTask = RP.create(activeClose);
        int delay = delayedClose ? NULL_DOCUMENT_CLOSE_DELAY : 0;
        activeCloseTask.schedule(delay);
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("closeImplLA(): Scheduled close task with delay=" + delay + ".\n"); // NOI18N
        }
    }
    
    /**
     * @return true if canceling was successful or false (and activeClose != null retained)
     *  if close is already running.
     */
    void cancelCloseLA() { // Lock acquired mandatory
        if (LOG.isLoggable(Level.FINER)) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "cancelCloseLA(): Attempt to cancel close by\n", new Exception());
            } else {
                LOG.finer("cancelCloseLA(): Attempt to cancel close.\n"); // NOI18N
            }
        }
        if (activeClose != null && activeClose.cancel()) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("cancelCloseLA(): activeClose().cancel() successful.\n"); // NOI18N
            }
            activeCloseTask.cancel();
            activeCloseTask = null;
            activeClose = null;
        }
    }
    
    void updateLines(final StyledDocument doc, final boolean close) {
        LineVector lineVector = ces.findLineVector();
        lineVector.updateLines(new LineVector.LineUpdater() {
            @Override
            public void updateLine(Line line) {
                if (line instanceof DocumentLine) {
                    ((DocumentLine)line).documentOpenedClosed(doc, close);
                }
            }
        });
    }

    IllegalStateException invalidStatus() {
        return new IllegalStateException("Unknown documentStatus=" + documentStatus); // NOI18N
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("DocumentOpenClose: ").append(getSimpleName(ces)). // NOI18N
                append(", documentStatus=").append(documentStatus);
        Reference<StyledDocument> ref = docRef;
        sb.append(", docRef=");
        if (ref != null) {
            StyledDocument doc = ref.get();
            sb.append("(").append(getSimpleName(doc)).append(")");
        } else {
            sb.append("null");
        }
        if (activeOpen != null) {
            sb.append("\n  activeOpen: ").append(activeOpen);
        }
        if (activeReload != null) {
            sb.append("\n  activeReload: ").append(activeReload);
        }
        if (activeClose != null) {
            sb.append("\n  activeClose: ").append(activeClose);
        }
        return sb.toString();
    }

    private final class DocumentLoad implements Runnable {

        /**
         * Whether reload of an existing document is done or a fresh document loading.
         */
        final boolean reload;

        /**
         * Document to be opened.
         */
        StyledDocument loadDoc;

        boolean loadSuccess;

        /**
         * Possible IO exception during document loading.
         */
        IOException loadIOException;
        
        /**
         * Possible runtime exception during document loading.
         */
        RuntimeException loadRuntimeException;
        
        /**
         * UQE thrown in reload during InputStream reading.
         */
        boolean userQuestionExceptionInReload;
        
        /**
         * In case of UQE during reload and user's refuse of the UQE do not read the content.
         */
        boolean skipInputStreamReading;
        
        /**
         * Panes that were collected in EDT or null.
         * Their carets should have the position retained after reload.
         */
        JEditorPane[] reloadOpenPanes;
        
        int[] reloadCaretOffsets;
        
        private boolean atomicLockedRun;
        
        /**
         * Initial part of reload runs in EDT.
         */
        private boolean preReloadInEDT;
        
        DocumentLoad() { // Constructor for a new document load
            this.reload = false;
        }

        DocumentLoad(StyledDocument loadDoc, JEditorPane[] reloadOpenPanes) {
            assert (loadDoc != null) : "loadDoc cannot be null for reload";
            this.reload = true;
            this.preReloadInEDT = true;
            this.loadDoc = loadDoc;
            this.reloadOpenPanes = reloadOpenPanes;
        }
        
        @Override
        public void run() {
            if (preReloadInEDT) {
                preReloadInEDT = false;
                preReloadInEDT();
                return;
            }
            if (atomicLockedRun) {
                atomicLockedRun = false;
                atomicLockedRun();
                return;
            }
            
            // Non-atomic locked run
            try {
                UndoRedo.Manager undoRedoManager = ces.getUndoRedo();
                if (!userQuestionExceptionInReload) {
                    synchronized (lock) {
                        if (reload) {
                            assert (documentStatus == DocumentStatus.OPENED) :
                                    "Invalid documentStatus=" + documentStatus + " expected OPENED"; // NOI18N
                            documentStatus = DocumentStatus.RELOADING;
                        } else {
                            assert (documentStatus == DocumentStatus.CLOSED) :
                                    "Invalid documentStatus=" + documentStatus + " expected CLOSED"; // NOI18N
                            documentStatus = DocumentStatus.LOADING;
                        }
                    }

                    if (reload) {
                        // Detach UndoManager from the document now so that
                        // it won't absorb loading (or reloading) modifications.
                        loadDoc.removeUndoableEditListener(undoRedoManager);

                        // Discard all edits before subsequent operations since the edits may consume
                        // considerable amount of memory.
                        undoRedoManager.discardAllEdits();

                    } else {
                        synchronized (lock) {
                            EditorKit kit = ces.createEditorKit();
                            loadDoc = ces.createStyledDocument(kit);
                            assert (loadDoc != null) : "kit.createDefaultDocument() returned null"; // NOI18N
                        }
                    }
                }

                // Perform atomicLockedRun() under atomic lock
                atomicLockedRun = true;
                NbDocument.runAtomic(loadDoc, this);

                if (loadIOException == null && loadRuntimeException == null) {
                    if (reload) {
                        // Discard whole document remove and re-insert edits.
                        undoRedoManager.discardAllEdits();
                    } else {
                        // Start listening on changes in Env
                        ces.setListeningOnEnv(true);
                    }
                    // For document load the undo manager is already empty (see document close)
                    if (undoRedoManager instanceof UndoRedoManager) {
                        ((UndoRedoManager) undoRedoManager).markSavepoint();
                    }

                    // Attach undo listener and allow modifications again
                    if (loadDoc != null) {
                        LOG.fine("task-addUndoableEditListener");
                        loadDoc.addUndoableEditListener(undoRedoManager);
                    }

                    // If a user did modification right before reload the change is lost
                    ces.callNotifyUnmodified();
                    
                    // Attach annotations
                    updateLines(loadDoc, false);

                    synchronized (lock) {
                        documentStatus = DocumentStatus.OPENED; // common for both reload and open
                    }
                    loadSuccess = true;
                }

                // Handle UserQuestionException thrown during reload operation
                if (reload && loadIOException instanceof UserQuestionException) {
                    reloadUQEThrown((UserQuestionException) loadIOException);
                }

            } catch (RuntimeException ex) {
                loadRuntimeException = ex;

            } finally {
                if (!userQuestionExceptionInReload) { // For UQE during reload this will be done later
                    synchronized (lock) {
                        if (!loadSuccess) {
                            documentStatus = DocumentStatus.CLOSED;
                            setDocRef(null);
                            ces.setListeningOnEnv(false);
                        }

                        ces.setPreventModification(false);
                        // Clear the tasks (before change firing)
                        if (reload) {
                            activeReloadTask = null;
                            activeReload = null;
                        } else {
                            activeOpenTask = null;
                            activeOpen = null;
                        }
                        if (LOG.isLoggable(Level.FINER)) {
                            LOG.finer("documentLoad(): reload=" + reload + // NOI18N
                                    ", documentStatus=" + documentStatus + // NOI18N
                                    ", loadSuccess=" + loadSuccess + "\n"); // NOI18N
                        }
                    }
                    if(reload) {
                        Mutex.EVENT.postReadRequest(() -> 
                                ces.firePropertyChange(EditorCookie.Observable.PROP_RELOADING, true, false));
                    }
                }
            }
        }

        private void atomicLockedRun() {
            try {
                if (!userQuestionExceptionInReload) {
                    if (reload) {
                        // Earlier impl fired a document close but to detach annotations
                        // but there may be listeners that request document open sychronously
                        // and it's unclear how to solve openDocument() request 
                        // over a document being just reloaded.
                        ces.getPositionManager().documentClosed();
                        ces.updateLineSet(true);
                    }

                    /* Remove existing listener before running the loading or reloading
                     * prevents firing of insertUpdate() and removeUpdate() (and callNotifyModify())
                     * during the load.
                     */
                    ces.removeDocListener(loadDoc);

                    if (reload) {
                        LOG.fine("clearDocument");
                        try {
                            // Remove all text in case there is any
                            if (loadDoc.getLength() > 0) {
                                loadDoc.remove(0, loadDoc.getLength());
                            }
                        } catch (BadLocationException ex) {
                            LOG.log(Level.INFO, null, ex);
                        }
                    }
                } else {
                    // Turn userQuestionExceptionInReload flag off since now the work interrupted by UQE
                    // should be finished.
                    userQuestionExceptionInReload = false;
                }

                // Load doc's content from IS
                if (!skipInputStreamReading) {
                    InputStream is = new BufferedInputStream(ces.cesEnv().inputStream());
                    try {
                        // read the document
                        ces.loadFromStreamToKit(loadDoc, is, ces.createEditorKit());
                    } finally {
                        is.close();
                    }
                }

                // Start to return the document from CES.getDocument()
                setDocRef(loadDoc);

                // opening the document, inform position manager
                if (reload && reloadOpenPanes != null) {
                    ces.getPositionManager().documentOpened(new WeakReference<StyledDocument>(loadDoc));
                }

                // create new description of lines
                ces.updateLineSet(true);

                ces.updateLastSaveTime();

                // Start listening on changes in document
                ces.addDocListener(loadDoc);

                if (reload) {
                    if (reloadCaretOffsets != null) {
                        int docLen = loadDoc.getLength();
                        // Remember caret positions and set them later in EDT
                        final Position[] caretPositions = new Position[reloadCaretOffsets.length];
                        for (int i = 0; i < reloadCaretOffsets.length; i++) {
                            try {
                                int offset = reloadCaretOffsets[i];
                                offset = Math.max(Math.min(offset, docLen), 0);
                                caretPositions[i] = loadDoc.createPosition(offset);
                            } catch (BadLocationException ex) {
                                // Cannot use loadDoc.getEndPosition() since pane.setCaretPosition() does not accept doc.getLength()+1 offset
                                caretPositions[i] = null;
                            }
                        }
                        Mutex.EVENT.postReadRequest(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < reloadOpenPanes.length; i++) {
                                    JEditorPane pane = reloadOpenPanes[i];
                                    // Ensure that the doc is the reloaded one and position valid
                                    if (pane.getDocument() == loadDoc && caretPositions[i] != null) {
                                        reloadOpenPanes[i].setCaretPosition(caretPositions[i].getOffset());
                                    }
                                }
                            }
                        });
                    }
                }

                // Prevent user modifications between this atomic change finishes
                // and undo listener gets attached.
                ces.setPreventModification(true);
                
            } catch (BadLocationException ex) {
                // Wrap BLE into ISE and throw it as runtime exception
                loadRuntimeException = new IllegalStateException(ex);
            } catch (IOException ex) {
                loadIOException = ex; // Handle UQE during reload in upper run()
            }
        }

        void preReloadInEDT() {
            boolean success = false;
            try {
                loadDoc.render(new Runnable() {
                    @Override
                    public void run() {
                        // Remember caret positions in all opened panes
                        if (reloadOpenPanes != null) {
                            reloadCaretOffsets = new int[reloadOpenPanes.length];
                            for (int i = 0; i < reloadOpenPanes.length; i++) {
                                reloadCaretOffsets[i] = reloadOpenPanes[i].getCaretPosition();
                            }
                        }
                    }
                });

                ces.firePropertyChange(EditorCookie.Observable.PROP_RELOADING, false, true);
                // Next portion will run as Task in RP
                activeReloadTask = RP.create(this);
                activeReloadTask.schedule(0);
                success = true;
            } finally {
                if (!success) {
                    activeReload = null;
                    activeReloadTask = null;
                }
            }
        }
        
        void reloadUQEThrown(UserQuestionException uqe) {
            userQuestionExceptionInReload = true;
            // In case of reload handle UQE so that the reloading finishes with doc's content loading
            UserQuestionExceptionHandler handler = new UserQuestionExceptionHandler(
                    ces, (UserQuestionException) loadIOException)
            {
                    @Override
                    protected StyledDocument openDocument() throws IOException {
                        loadIOException = null;
                        // Reuse current reload task
                        activeReloadTask.schedule(0);
                        return loadDoc;
                    }

                    @Override
                    protected void openRefused() {
                        loadIOException = null;
                        // This is a problematic situation since the user refused document loading
                        // during reload.
                        skipInputStreamReading = true;
                        activeReloadTask.schedule(0);
                    }
            };
            handler.runInEDT();
            // This however means that the task returned by CES.reloadDocument() will no longer ensure
            // finished reloading. There does not seem to be a good way for possible tests to sync
            // on finished reloading so currently they have to only wait for certain amount of time.
        }

        void fireDocumentChange() {
            // Fire outside of "synchronized (lock)"
            if (loadSuccess) {
                ces.fireDocumentChange(loadDoc, false);
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(200);
            sb.append(getSimpleName(this)).append(": reload=").append(reload).
                    append(", loadDoc=").append(getSimpleName(loadDoc)). // NOI18N
                    append(", loadSuccess=").append(loadSuccess);
            if (reload) {
                if (reloadOpenPanes != null) {
                    sb.append(", reloadOpenPanes.length=").append(reloadOpenPanes.length);
                }
            }
            return sb.toString();
        }
        
    }
    
    /**
     * A task that allows DocumentLoad task to become finished before actual firing gets done.
     */
    private static final class DocumentOpenFire implements Runnable {
        
        final DocumentLoad documentOpen;
        
        public DocumentOpenFire(DocumentLoad documentOpen) {
            this.documentOpen = documentOpen;
        }
        
        @Override
        public void run() {
            assert (!documentOpen.reload) : "This task should not be posted for reloads."; // NOI18N
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("documentLoad(): Going to fireDocumentChange...\n"); // NOI18N
            }
            boolean success = false;
            try {
                documentOpen.fireDocumentChange();
                success = true;
            } finally {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("documentLoad(): fireDocumentChange: success=" + success + "\n"); // NOI18N
                }
            }
        }
        
    }

    private final class DocumentClose implements Runnable {
        
        /**
         * Document to be closed.
         */
        final StyledDocument closeDoc;
        
        final boolean delayedClose;
        
        boolean cancelled;
        
        boolean started;
        
        boolean readLockedRun;

        public DocumentClose(StyledDocument closeDoc, boolean delayedClose) {
            this.closeDoc = closeDoc;
            this.delayedClose = delayedClose;
        }
        
        @Override
        public void run() {
            if (readLockedRun) {
                readLockedRun = false;
                readLockedRun();
                return;
            }

            // Perform document closing
            synchronized (lock) {
                if (cancelled) {
                    return;
                }
                started = true;
            }
            setDocRef(null); // getDocument() will no longer return the document being closed
            try {
                // Stop listening on the Env
                ces.setListeningOnEnv(false);

                // Perform readLockedRun() under read lock
                readLockedRun = true;
                if (closeDoc != null) {
                    closeDoc.render(this);
                }

                ces.updateLineSet(true);
                updateLines(closeDoc, true);
                
            } finally {
                synchronized (lock) {
                    documentStatus = DocumentStatus.CLOSED;
                    activeCloseTask = null;
                    activeClose = null;
                }
                
                // Some listeners may request openDocument() directly from closed document notification
                // Open the document synchronously for them.
                firingCloseDocument = true;
                boolean success = false;
                try {
                    ces.fireDocumentChange(closeDoc, true);
                    success = true;
                } finally {
                    firingCloseDocument = false;
                    docOpenedWhenFiringCloseDocument = null;
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.finer("documentClose(): fireDocumentChange: success=" + success + "\n"); // NOI18N
                    }
                }
            }
        }
        
        void readLockedRun() {
            ces.callNotifyUnmodified();

            if (closeDoc != null) {
                closeDoc.removeUndoableEditListener(ces.getUndoRedo());
                ces.removeDocListener(closeDoc);
            }

            ces.getPositionManager().documentClosed();

            ces.getUndoRedo().discardAllEdits();
        }
        
        boolean cancel() {
            synchronized (lock) {
                if (started) {
                    return false;
                }
                cancelled = true;
                return true;
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(200);
            sb.append("closeDoc=").append(getSimpleName(closeDoc)). // NOI18N
                    append(", delayedClose=").append(delayedClose). // NOI18N
                    append(", cancelled=").append(cancelled). // NOI18N
                    append(", started=").append(started); // NOI18N
            return sb.toString();
        }
        
    }
    
    final class DocumentRef extends WeakReference<StyledDocument> implements Runnable {

        public DocumentRef(StyledDocument doc) {
            super(doc, org.openide.util.Utilities.activeReferenceQueue());
            Logger.getLogger("TIMER").log(Level.FINE, "TextDocument", doc);
        }

        @Override
        public void run() {
            synchronized (lock) {
                if (this == docRef) {
                    closeImplLA(null, true); // Delayed close
                }
            }
        }
        
    }
    
}
