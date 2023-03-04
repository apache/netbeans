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
package org.netbeans.modules.refactoring.spi.impl;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.AtomicLockEvent;
import org.netbeans.api.editor.document.AtomicLockListener;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.ProgressListener;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.spi.BackupFacility;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jan Becicka
 */
public final class UndoManager {

    /**
     * stack of undo items
     */
    private LinkedHashMap<RefactoringSession, LinkedList<UndoItem>> undoList;
    /**
     * stack of redo items
     */
    private LinkedHashMap<RefactoringSession, LinkedList<UndoItem>> redoList;
    
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private boolean wasUndo = false;
    private boolean wasRedo = false;
    private boolean transactionStart;
    private IdentityHashMap<LinkedList, String> descriptionMap;
    private String description;
    private ProgressListener progress;
    private static UndoManager instance;
    
    public boolean autoConfirm = false;
    
    /**
     * Suppresses saveAll operation; if suppressed, the sources will be saved
     * after the refactoring operation completes.
     */
    private boolean suppressSaveAll;
    
    boolean setSupressSaveAll(boolean suppressSaveAll) {
        boolean res = this.suppressSaveAll;
        this.suppressSaveAll = suppressSaveAll;
        return res;
    }

    /**
     * Set the value of autoConfirm.
     * Used just for tests to disable UI.
     *
     * @param autoConfirm new value of autoConfirm
     */
    public void setAutoConfirm(boolean autoConfirm) {
        this.autoConfirm = autoConfirm;
    }


    /**
     * Singleton instance
     * @return
     */
    public static synchronized UndoManager getDefault() {
        if (instance == null) {
            instance = new UndoManager();
        }
        return instance;
    }

    /**
     * Creates a new instance of UndoManager
     */
    private UndoManager() {
        undoList = new LinkedHashMap<RefactoringSession, LinkedList<UndoItem>>();
        redoList = new LinkedHashMap<RefactoringSession, LinkedList<UndoItem>>();
        descriptionMap = new IdentityHashMap<LinkedList, String>();
    }

    private UndoManager(ProgressListener progress) {
        this();
        this.progress = progress;
    }

    /**
     * Setter for undo description. For instance "Rename"
     * @param desc 
     */
    public void setUndoDescription(String desc) {
        description = desc;
    }

    /**
     * Getter for undo description.
     * @return
     */
    public String getUndoDescription(RefactoringSession refactoringSession) {
        if(refactoringSession == null) {
            refactoringSession = getLastUndo();
        }
        final RefactoringSession session = refactoringSession;
        LinkedList<UndoItem> undoitems = undoList.get(session);
        if (undoitems == null) {
            return null;
        }
        return descriptionMap.get(undoitems);
    }

    /**
     * Getter for Redo description.
     * @return
     */
    public String getRedoDescription(RefactoringSession refactoringSession) {
        if(refactoringSession == null) {
            refactoringSession = getLastUndo();
        }
        final RefactoringSession session = refactoringSession;
        LinkedList<UndoItem> redoitems = redoList.get(session);
        if (redoitems == null) {
            return null;
        }
        return descriptionMap.get(redoitems);
    }

    /**
     * called to mark transaction start
     */
    public void transactionStarted() {
        transactionStart = true;
    }

    /**
     * called to mark end of transaction
     */
    public void transactionEnded(boolean fail, RefactoringSession session) {
        description = null;
        if (fail && !undoList.isEmpty()) {
            //XXX todo 
            //undoList.removeFirst();
        } else {
            // [TODO] (jb) this code disables undos for changes using org.openide.src
            if (isUndoAvailable(session) && getUndoDescription(session) == null) {
                descriptionMap.remove(undoList.remove(session));
            }
        }
        fireChange();
    }
    
    private static final RequestProcessor SAVE_RP = new RequestProcessor(UndoManager.class);
    
    /**
     * Undoes the last transaction. If the document is under an atomic lock,
     * postpones save-all on the modified sources until after the document unlocks.
     * This method should be used from within operations which start with the document
     * already locked. Saving [all] documents leads to numerous low-level events, which
     * may wake up unrelated processes that can deadlock on the already-locked document.
     * 
     * @param session refactoring session
     * @param doc reference document.
     */
    void undo(RefactoringSession session, Document doc) {
        if (!DocumentUtilities.isWriteLocked(doc)) {
            undo(session);
        } else {
            AtomicLockDocument ald = LineDocumentUtils.as(doc, AtomicLockDocument.class);
            if (ald == null) {
                undo(session);
            } else {
                final boolean orig = setSupressSaveAll(true);

                class L implements AtomicLockListener, Runnable {
                    @Override
                    public void atomicLock(AtomicLockEvent evt) {
                    }

                    @Override
                    public void atomicUnlock(AtomicLockEvent evt) {
                        setSupressSaveAll(orig);
                        SAVE_RP.post(this);
                        ald.removeAtomicLockListener(this);
                    }

                    @Override
                    public void run() {
                        LifecycleManager.getDefault().saveAll();
                    }
                }
                final L l = new L();
                ald.addAtomicLockListener(l);
                undo(session);
            }
        }
    }

    /**
     * undo last transaction
     */
    public void undo(RefactoringSession refactoringSession) {
        if(refactoringSession == null) {
            refactoringSession = getLastUndo();
        }
        final RefactoringSession session = refactoringSession;
        //System.out.println("************* Starting UNDO");
        LinkedList<UndoItem> undoitems = undoList.get(session);
        if (undoitems != null && !undoitems.isEmpty()) {
            final LinkedList<UndoItem> undo = undoitems;
            if(!autoConfirm) {
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(UndoManager.class, "MSG_ReallyUndo", getUndoDescription(session)), NbBundle.getMessage(UndoManager.class, "MSG_ConfirmUndo"), NotifyDescriptor.YES_NO_OPTION);
                Object result = DialogDisplayer.getDefault().notify(nd);
                if (!NotifyDescriptor.OK_OPTION.equals(result)) {
                    throw new CannotUndoException();
                }
            }

            Runnable run = new Runnable() {

                public void run() {
                    boolean fail = true;
                    try {
                        transactionStarted();
                        wasUndo = true;
                        fireProgressListenerStart(0, undo.size());
                        Iterator undoIterator = undo.iterator();
                        UndoItem item;
                        redoList.put(session, new LinkedList<UndoItem>());
                        descriptionMap.put(redoList.get(session), descriptionMap.remove(undo));
                        try {
                            while (undoIterator.hasNext()) {
                                fireProgressListenerStep();
                                item = (UndoItem) undoIterator.next();
                                item.undo();
                                if (item instanceof SessionUndoItem) {
                                    SessionUndoItem sessionUndoItem = (SessionUndoItem) item;
                                    addItem(item, sessionUndoItem.change);
                                }
                            }
                        } catch (CannotUndoException e) {
                            descriptionMap.put(undo, descriptionMap.get(redoList.get(session)));
                            descriptionMap.remove(redoList.get(session));
                            redoList.remove(session);
                            throw e;
                        }
                        undoList.remove(session);
                        fail = false;
                    } finally {
                        try {
                            wasUndo = false;
                            transactionEnded(fail, session);
                        } finally {
                            fireProgressListenerStop();
                            fireChange();
                        }
                    }

                }
            };

//            if (SwingUtilities.isEventDispatchThread()) {
//                ProgressUtils.runOffEventDispatchThread(run,
//                        "Undoing... ",
//                        new AtomicBoolean(),
//                        false);
//            } else {
            run.run();
//            }
        }
    }

    /**
     * redo last undo
     */
    public void redo(RefactoringSession refactoringSession) {
        //System.out.println("************* Starting REDO");
        if(refactoringSession == null) {
            refactoringSession = getLastRedo();
        }
        final RefactoringSession session = refactoringSession;
        LinkedList<UndoItem> redoitems = redoList.get(session);
        if (redoitems != null) {
            final LinkedList<UndoItem> redo = redoitems;
            if (!autoConfirm && JOptionPane.showConfirmDialog(
                    WindowManager.getDefault().getMainWindow(),
                    NbBundle.getMessage(UndoManager.class, "MSG_ReallyRedo", getRedoDescription(session)),
                    NbBundle.getMessage(UndoManager.class, "MSG_ConfirmRedo"),
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                throw new CannotRedoException();
            }
            Runnable run = new Runnable() {

                public void run() {
                    boolean fail = true;
                    try {
                        transactionStarted();
                        wasRedo = true;
                        fireProgressListenerStart(1, redo.size());
                        Iterator<UndoItem> redoIterator = redo.iterator();
                        UndoItem item;
                        description = descriptionMap.remove(redo);
                        try {
                            while (redoIterator.hasNext()) {
                                fireProgressListenerStep();
                                item = redoIterator.next();
                                item.redo();
                                if (item instanceof SessionUndoItem) {
                                    addItem(item, session);
                                }
                            }
                        } catch (CannotRedoException ex) {
                            descriptionMap.put(redo, description);
                            throw ex;
                        }
                        redoList.remove(session);
                        fail = false;
                    } finally {
                        try {
                            wasRedo = false;
                            transactionEnded(fail, session);
                        } finally {
                            fireProgressListenerStop();
                            fireChange();
                        }
                    }
                }
            };

//            if (SwingUtilities.isEventDispatchThread()) {
//                ProgressUtils.runOffEventDispatchThread(run,
//                        "Redoing... ",
//                        new AtomicBoolean(),
//                        false);
//            } else {
            run.run();
//            }
        }
    }

    /**
     * clean undo/redo stacks
     */
    public void clear() {
        undoList.clear();
        redoList.clear();
        descriptionMap.clear();
        BackupFacility.getDefault().clear();
        fireChange();
    }

    public void addItem(RefactoringSession session) {
        addItem(new SessionUndoItem(session), session);
    }

    /**
     * add new item to undo/redo list
     */
    private void addItem(UndoItem item, RefactoringSession session) {
        if (wasUndo) {
            LinkedList<UndoItem> redo = redoList.get(session);
            redo.addFirst(item);
        } else {
            if (transactionStart) {
                undoList.put(session, new LinkedList<UndoItem>());
                descriptionMap.put(undoList.get(session), description);
                transactionStart = false;
            }
            LinkedList<UndoItem> undo = this.undoList.get(session);
            undo.addFirst(item);
        }
        if (!(wasUndo || wasRedo)) {
            redoList.clear();
        }
    }

    public boolean isUndoAvailable(RefactoringSession session) {
        return undoList.containsKey(session);
    }

    public boolean isRedoAvailable(RefactoringSession session) {
        return redoList.containsKey(session);
    }
    
    public boolean isUndoAvailable() {
        return !undoList.isEmpty();
    }
    
    public boolean isRedoAvailable() {
        return !redoList.isEmpty();
    }

    public void addChangeListener(ChangeListener cl) {
        changeSupport.addChangeListener(cl);
    }

    public void removeChangeListener(ChangeListener cl) {
        changeSupport.removeChangeListener(cl);
    }

    private void fireChange() {
        changeSupport.fireChange();
    }

    private void fireProgressListenerStart(int type, int count) {
        stepCounter = 0;
        if (progress == null) {
            return;
        }
        progress.start(new ProgressEvent(this, ProgressEvent.START, type, count));
    }
    private int stepCounter = 0;

    /**
     * Notifies all registered listeners about the event.
     */
    private void fireProgressListenerStep() {
        if (progress == null) {
            return;
        }
        progress.step(new ProgressEvent(this, ProgressEvent.STEP, 0, ++stepCounter));
    }

    /**
     * Notifies all registered listeners about the event.
     */
    private void fireProgressListenerStop() {
        if (progress == null) {
            return;
        }
        progress.stop(new ProgressEvent(this, ProgressEvent.STOP));
    }

    private RefactoringSession getLastUndo() {
        RefactoringSession session = null;
        if(!undoList.isEmpty()) {
            Iterator<RefactoringSession> iterator = undoList.keySet().iterator();
            while(iterator.hasNext()) {
                session = iterator.next();
            }
        }
        return session;
    }

    private RefactoringSession getLastRedo() {
        RefactoringSession session = null;
        if(!redoList.isEmpty()) {
            Iterator<RefactoringSession> iterator = redoList.keySet().iterator();
            while(iterator.hasNext()) {
                session = iterator.next();
            }
        }
        return session;
    }

    private interface UndoItem {

        void undo();

        void redo();
    }

    private final class SessionUndoItem implements UndoItem {

        private RefactoringSession change;

        public SessionUndoItem(RefactoringSession change) {
            this.change = change;
        }

        @Override
        public void undo() {
            change.undoRefactoring(!suppressSaveAll);
        }

        @Override
        public void redo() {
            change.doRefactoring(!suppressSaveAll);
        }
    }
}
