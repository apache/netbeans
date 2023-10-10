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

package org.netbeans.modules.editor.fold;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldStateChange;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;

/**
 * Class encapsulating a modification
 * of the code folding hierarchy.
 * <br>
 * It's provided by {@link RootFold#createHierarchyTransaction()}.
 * <br>
 * It can accumulate arbitrary number of changes of various folds.
 * <br>
 * Only one transaction can be active at the time.
 * <br>
 * Once all the modifications are done the transaction must be
 * committed by {@link #commit()} which creates
 * a {@link org.netbeans.api.editor.fold.FoldHierarchyEvent}
 * and fires it to the listeners automatically.
 * <br>
 * Once the transaction is committed no additional
 * changes can be made to it.
 * <br>
 * There is currently no way to rollback the transaction.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class FoldHierarchyTransactionImpl {

    // -J-Dorg.netbeans.api.editor.fold.FoldHierarchy.level=FINE
    private static final Logger LOG = Logger.getLogger(org.netbeans.api.editor.fold.FoldHierarchy.class.getName());
    static final Logger DEBUG_LOG = Logger.getLogger(org.netbeans.api.editor.fold.FoldHierarchy.class.getName() + ".debug");
    
    static final boolean debugFoldHierarchy = DEBUG_LOG.isLoggable(Level.FINE);
    
    private static final Fold[] EMPTY_FOLDS = new Fold[0];

    private static final FoldStateChange[] EMPTY_FOLD_STATE_CHANGES
        = new FoldStateChange[0];
    
    private static final int[] EMPTY_INT_ARRAY = new int[0];

    // The max number of nested folds
    private static final int MAX_NESTING_LEVEL = 1000;
    
    private FoldHierarchyTransaction transaction;

    private boolean committed;
    
    private FoldHierarchyExecution execution;
    
    /**
     * Fold inside which the last operation (insert or remove)
     * was done.
     */
    private Fold lastOperationFold;

    /**
     * Index at which the last operation (insert or remove) was done.
     */
    private int lastOperationIndex;
    
    /**
     * Fold that is block in case the inspectOverlap() returns null.
     * <br>
     * This is instance var so that inspectOverlap() can set it.
     */
    private Fold addFoldBlock;
    
    /**
     * List of lists of folds that were unblocked by removing
     * of a blocked fold indexed by the fold priority.
     * <br>
     * Prior to the commit of the transaction the unblocked
     * folds are attempted to be reinserted into the hierarchy
     * starting with folds with the highest priority
     * going to folds with the lowest priority.
     */
    private List unblockedFoldLists = new ArrayList(4);
    
    /**
     * Maximum priority of the unblocked folds added
     * since the start of this transaction.
     */
    private int unblockedFoldMaxPriority = -1;
    
    /**
     * Set of folds that were added to the hierarchy
     * during this transaction.
     */
    private Set addedToHierarchySet;
    
    /**
     * Set of folds that were removed from the hierarchy
     * during this transaction.
     */
    private Set removedFromHierarchySet;

    private Map fold2StateChange;
    
    private int affectedStartOffset;
    
    private int affectedEndOffset;
    
    /**
     * Folds that have gone out of sync with hierarchy and have to be reinserted.
     */
    private Set<Fold> reinsertSet;
    
    /**
     * If the hierarchy was damaged, the hierarchy will be dumped at transaction start, to provide
     * context for additional logs.
     */
    private String initialSnapshot;
    
    private final int dmgCounter;
    
    private final boolean suppressEvents;
    
    public FoldHierarchyTransactionImpl(FoldHierarchyExecution execution) {
        this(execution, false);
    }
    
    public FoldHierarchyTransactionImpl(FoldHierarchyExecution execution, boolean suppressEvents) {
        this.execution = execution;
        this.affectedStartOffset = -1;
        this.affectedEndOffset = -1;
        this.suppressEvents = suppressEvents;
        
        this.transaction = SpiPackageAccessor.get().createFoldHierarchyTransaction(this);
        this.dmgCounter = execution.getDamagedCount();
        if (dmgCounter > 0) {
            String t = null;
            JTextComponent comp = execution.getComponent();
            if (comp != null) {
                Document doc = comp.getDocument();
                try {
                    t = doc.getText(0, doc.getLength());
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (debugFoldHierarchy) {
                initialSnapshot = execution.toString() + 
                                  "\nContent at previous commit:\n====\n" + execution.getCommittedContent() + "\n====\n" +
                                  "\nText content:\n====\n" + t + "\n====\n";
            }
        }
    }
    
    public FoldHierarchyTransaction getTransaction() {
        return transaction;
    }
    
    public void resetCaches() {
        this.lastOperationFold = null;
        this.lastOperationIndex = -1;
    }
    
    void cancelled() {
        checkNotCommitted();
        LOG.log(Level.WARNING, "Fold transaction not committed at unlock");
        // just reused the flag, only checked in checkNotCommitted
        committed = true;
        execution.clearActiveTransaction();
    }

    /**
     * Commit this active transaction.
     * <br>
     * The <code>FoldHierarchyEvent</code> will be fired automatically
     * (if there were any changes done during this transaction).
     * <br>
     * The transaction can only be commited once.
     */
    public void commit() {
        checkNotCommitted();
        try {
            if (!isEmpty()) {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.finest("FoldHierarchy BEFORE transaction commit:\n" + execution);
                    execution.checkConsistency();
                }

                int size;
                Fold[] removedFolds;
                if (removedFromHierarchySet != null && ((size = removedFromHierarchySet.size()) != 0)) {
                    removedFolds = new Fold[size];
                    removedFromHierarchySet.toArray(removedFolds);

                } else {
                    removedFolds = EMPTY_FOLDS;
                }

                Fold[] addedFolds;
                if (addedToHierarchySet != null && ((size = addedToHierarchySet.size()) != 0)) {
                    addedFolds = new Fold[size];
                    addedToHierarchySet.toArray(addedFolds);

                } else {
                    addedFolds = EMPTY_FOLDS;
                }

                // the following will generate some additions to addedToHierarchySet, but not important
                if (reinsertSet != null) {
                    ApiPackageAccessor acc = ApiPackageAccessor.get();
                    for (Fold f : reinsertSet) {
                        acc.foldSetParent(f, null);
                        if (f.getFoldCount() <= 0) {
                            addFold(f);
                        } else {
                            LOG.warning("Unexpected children for fold: " + f + ", dumping hierarchy: " + execution);
                        }
                    }
                }

                committed = true;
                execution.clearActiveTransaction();
                FoldStateChange[] stateChanges = null;
                
                if (!suppressEvents) {
                    if (fold2StateChange != null) {
                        stateChanges = new FoldStateChange[fold2StateChange.size()];
                        fold2StateChange.values().toArray(stateChanges);
                    } else { // no state changes => use empty array
                        stateChanges = EMPTY_FOLD_STATE_CHANGES;
                    }

                    for (int i = stateChanges.length - 1; i >= 0; i--) {
                        FoldStateChange change = stateChanges[i];
                        Fold fold = change.getFold();
                        updateAffectedOffsets(fold);
                        int startOffset = change.getOriginalStartOffset();
                        int endOffset = change.getOriginalEndOffset();
                        if (endOffset >= 0 && endOffset < startOffset) {
                            LOG.warning("startOffset=" + startOffset + " > endOffset=" + endOffset); // NOI18N;
                            endOffset = startOffset;
                        }
                        if (startOffset != -1) {
                            updateAffectedStartOffset(startOffset);
                        }
                        if (endOffset != -1) {
                            updateAffectedEndOffset(endOffset);
                        }
                    }

                }
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("FoldHierarchy AFTER transaction commit:\n" + execution);
                    execution.checkConsistency();
                }

                if (stateChanges != null) {
                    int so = Math.max(0, affectedStartOffset);
                    execution.createAndFireFoldHierarchyEvent(
                        removedFolds, addedFolds, stateChanges,
                        so, 
                        Math.max(affectedEndOffset, so));
                }
            } else {
                committed = true;
                execution.clearActiveTransaction();

            }
        } finally {
            execution.transactionCommitted();
            if (execution.getDamagedCount() > dmgCounter && dmgCounter > 0) {
                LOG.warning("Fold Hierarchy damaged. Dumping initial state:\n----------------------");
                LOG.warning(initialSnapshot);
                LOG.warning("\n----------------------\nCurrent state:");
                LOG.warning(execution.toString());
                // will also reset the damaged count
                execution.rebuildHierarchy();
            }
        }
    }

    /**
     * The method is run after 'undo' and should validate all folds affected by the change. Undo operation
     * revives some Position offsets, and the effect on the data in fold hierarchy proved unreliable - Positions of
     * a child were reverted to a state they did not fit into parent's Positions (also restored by Undo).
     * 
     * @param fold parent fold
     * @param evt current event
     * @param damaged damaged folds
     */
    void validateAffectedFolds(Fold fold, DocumentEvent evt) {
        Collection pp = new ArrayList();
        validateAffectedFolds(fold, evt, pp);
        if (!pp.isEmpty()) {
            ApiPackageAccessor api = ApiPackageAccessor.get();
            for (Iterator it = pp.iterator(); it.hasNext(); ) {
                Fold childFold = (Fold)it.next();
                // go through Operation to catch errors & rebuild hierarchy:
                getOperation(childFold).removeFromHierarchy(childFold, this);
                removeEmptyNotify(childFold);
            }
        }
    }
    
    
    void validateAffectedFolds(Fold fold, DocumentEvent evt, Collection damaged) {
        int startOffset = evt.getOffset();
        int endOffset = startOffset + evt.getLength();
        
        int childIndex = FoldUtilitiesImpl.findFoldStartIndex(fold, startOffset, true);
        if (childIndex == -1) {
            if (fold.getFoldCount() == 0) {
                return;
            }
            Fold first = fold.getFold(0);
            if (first.getStartOffset() <= endOffset) {
                childIndex = 0;
            } else {
                return;
            }
        }
        if (childIndex >= 1) {
            Fold prevChildFold = fold.getFold(childIndex - 1);
            if (prevChildFold.getEndOffset() == startOffset) {
                validateAffectedFolds(prevChildFold, evt, damaged);
            }
        }
        int pStart = fold.getStartOffset();
        int pEnd = fold.getEndOffset();
        boolean removed;
        boolean startsWithin = false;
        do {
            Fold childFold = fold.getFold(childIndex);
            int cStart = childFold.getStartOffset();
            int cEnd = childFold.getEndOffset();
            startsWithin = cStart < startOffset && 
                           cEnd <= endOffset;
            removed = false;
            if (cStart < pStart || cEnd > pEnd || cStart == cEnd) {
                damaged.add(childFold);
            }
            if (childFold.getFoldCount() > 0) { // check children
                // Some children could be damaged even if this one was not
                validateAffectedFolds(childFold, evt, damaged);
            }
            childIndex++;
        } while ((startsWithin || removed) && childIndex < fold.getFoldCount());
    }
    
    /**
     * This method implements the <code>DocumentListener</code>.
     * <br>
     * It is not intended to be called by clients.
     */
    public void insertUpdate(DocumentEvent evt) {
        // Check whether there was an insert done right
        // at the original ending offset of the fold
        // so the fold end offset should be moved back.
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("insertUpdate: offset=" + evt.getOffset() // NOI18N
                + ", length=" + evt.getLength() + '\n'); // NOI18N
        }
        try {
            if (FoldHierarchyExecution.isEventInUndoRedoHack(evt)) {
                validateAffectedFolds(execution.getRootFold(), evt);
            }
            
            insertCheckEndOffset(execution.getRootFold(), evt);

        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    private void insertCheckEndOffset(Fold fold, DocumentEvent evt)
    throws BadLocationException {
        int insertOffset = evt.getOffset();
        int insertEndOffset = insertOffset + evt.getLength();
        // Find first fold that starts at (or best represents) the insertEndOffset
        int childIndex = FoldUtilitiesImpl.findFoldStartIndex(fold, insertEndOffset, false);
        if (childIndex >= 0) { // could be at end of the child fold with the index
            Fold childFold = fold.getFold(childIndex);
            // Check whether not in fact searching for previous fold
            if (childIndex > 0 && childFold.getStartOffset() == insertEndOffset) {
                childIndex--;
                childFold = fold.getFold(childIndex);
            }
            
            int childFoldStartOffset = childFold.getStartOffset();
            if (childFoldStartOffset >= insertOffset) {
                // should never happen, but the following code relies on that the fold contains insertion point
                // and the end mark has moved.
                return;
            }
            
            int childFoldEndOffset = childFold.getEndOffset();
            // Check whether the child fold "contains" the insert
            // i.e. the children of the child must be checked as well
            if (childFoldEndOffset >= insertEndOffset) { // check children
                // Must dig into children first to maintain consistency
                // in case when the last child fold would end right at end offset
                // of this child.
                insertCheckEndOffset(childFold, evt);

                // Inform the fold about insertion
                ApiPackageAccessor api = ApiPackageAccessor.get();
                api.foldInsertUpdate(childFold, evt);
                
                if (childFoldEndOffset < childFoldStartOffset) {
                        LOG.warning("Child start offset > end offset, dumping fold hierarchy: " + execution);
                        LOG.warning("Document event was: " + evt + " offset: " + insertOffset + ", len: " + evt.getLength());
                        execution.markDamaged();
                        execution.remove(childFold, this);
                        api.foldMarkDamaged(childFold, FoldUtilitiesImpl.FLAG_START_DAMAGED | FoldUtilitiesImpl.FLAG_END_DAMAGED);
                        removeDamagedNotify(childFold);
                        return;
                }

                if (childFoldEndOffset == insertEndOffset) {
                    Document doc = evt.getDocument();
                    if (childFoldStartOffset == childFoldEndOffset) {
                        // Reset start-position of the fold since otherwise
                        // the subsequent resetting of end-position produces a new position
                        // which could eventully swap with the original start position.
                        api.foldSetStartOffset(childFold, doc, insertOffset);
                        FoldStateChange state = getFoldStateChange(childFold);
                        if (state.getOriginalEndOffset() >= 0 && state.getOriginalEndOffset() < childFoldStartOffset) {
                            execution.markDamaged();
                            LOG.warning("Original start offset > end offset, dumping fold hierarchy: " + execution);
                            LOG.warning("Document event was: " + evt + " offset: " + insertOffset + ", len: " + evt.getLength());
                        } 
                        api.foldStateChangeStartOffsetChanged(state, childFoldStartOffset);
                    }
                    // Now correct the end offset to the one before insertion
                    api.foldSetEndOffset(childFold, doc, insertOffset);
                    FoldStateChange state = getFoldStateChange(childFold);
                    if (state.getOriginalEndOffset() >= 0 && state.getOriginalStartOffset() > childFoldEndOffset) {
                        execution.markDamaged();
                        LOG.warning("Original start offset > end offset, dumping fold hierarchy: " + execution);
                        LOG.warning("Document event was: " + evt + " offset: " + insertOffset + ", len: " + evt.getLength());
                    }
                    api.foldStateChangeEndOffsetChanged(state, childFoldEndOffset);
                    if (childFold.getStartOffset() > childFold.getEndOffset()) {
                        execution.markDamaged();
                        LOG.warning("Updated fold " + childFold + " is inconsistent, dumping fold hierarchy: " + execution);
                        LOG.warning("The original offsets were: " + childFoldStartOffset + "-" + childFoldEndOffset);
                        LOG.warning("Document event was: " + evt + " offset: " + insertOffset + ", len: " + evt.getLength());
                    }
                    
                } else { // not right at the end of the fold -> check damaged
                    int dmg = FoldUtilitiesImpl.isFoldDamagedByInsert(childFold, evt);
                    if (dmg > 0) {
                        execution.remove(childFold, this);
                        api.foldMarkDamaged(childFold, dmg);
                        removeDamagedNotify(childFold);
                        
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("insertUpdate: removed damaged " // NOI18N
                                + childFold + '\n');
                        }
                    }
                }
            }
        }
    }
    
    
    private FoldOperationImpl getOperation(Fold fold) {
        return ApiPackageAccessor.get().foldGetOperation(fold);
    }
    
    private FoldManager getManager(Fold fold) {
        return getOperation(fold).getManager();
    }
    
    public void setCollapsed(Fold fold, boolean collapsed) {
        boolean oldCollapsed = fold.isCollapsed();
        if (oldCollapsed != collapsed) {
            ApiPackageAccessor api = ApiPackageAccessor.get();
            api.foldSetCollapsed(fold, collapsed);
            api.foldStateChangeCollapsedChanged(getFoldStateChange(fold));
        }
    }
    
    private void removeDamagedNotify(Fold fold) {
        getManager(fold).removeDamagedNotify(fold);
    }
    
    private void removeEmptyNotify(Fold fold) {
        getManager(fold).removeEmptyNotify(fold);
    }
    
    /**
     * This method implements the <code>DocumentListener</code>.
     * <br>
     * It is not intended to be called by clients.
     */
    public void removeUpdate(DocumentEvent evt) {
        // Check whether the remove damaged any folds
        // or made them empty.
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("removeUpdate: offset=" + evt.getOffset() + ", len=" + evt.getLength() + '\n'); // NOI18N
        }
        if (FoldHierarchyExecution.isEventInUndoRedoHack(evt)) {
            validateAffectedFolds(execution.getRootFold(), evt);
        }
        removeCheckDamaged2(evt);
    }
    
    /**
     * The method replays changes to folds identified by {@link FoldHierarchyExecution} in the pre-update event handler.
     * 
     * @param evt the document event that initiated the operation
     */
    private void removeCheckDamaged2(DocumentEvent evt) {
        Collection pp = execution.getRemovePostProcess(evt);
        if (pp.isEmpty()) {
            return;
        }
        ApiPackageAccessor api = ApiPackageAccessor.get();
        for (Iterator it = pp.iterator(); it.hasNext(); ) {
            int cmd = (Integer)it.next();
            int damagedFlags = cmd & FoldUtilitiesImpl.FLAGS_DAMAGED;
            cmd &= FoldHierarchyExecution.OPERATION_MASK;
            Fold childFold = (Fold)it.next();
            
            switch (cmd) {
                case FoldHierarchyExecution.OPERATION_EMPTY:
                    removeFold(childFold);
                    getManager(childFold).removeEmptyNotify(childFold);
                    api.foldMarkDamaged(childFold, damagedFlags);
                    break;
                    
                case FoldHierarchyExecution.OPERATION_DAMAGE:
                    api.foldMarkDamaged(childFold, damagedFlags);
                    removeFold(childFold);
                    getManager(childFold).removeDamagedNotify(childFold);
                    break;
                    
                case FoldHierarchyExecution.OPERATION_COLLAPSE:
                    setCollapsed(childFold , false);
                    // fall through
                case FoldHierarchyExecution.OPERATION_UPDATE:
                    api.foldRemoveUpdate(childFold, evt);
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
    }
    
    private boolean isEmpty() {
        return (fold2StateChange == null || fold2StateChange.isEmpty())
            && (addedToHierarchySet == null || addedToHierarchySet.isEmpty())
            && (removedFromHierarchySet == null || removedFromHierarchySet.isEmpty())
            && (reinsertSet == null || reinsertSet.isEmpty());
    }

    public FoldStateChange getFoldStateChange(Fold fold) {
        if (fold2StateChange == null) {
            fold2StateChange = new HashMap();
        }
        
        FoldStateChange change = (FoldStateChange)fold2StateChange.get(fold);
        if (change == null) {
            change = ApiPackageAccessor.get().createFoldStateChange(fold);
            fold2StateChange.put(fold, change);
        }

        return change;
    }

    /**
     * Remove the fold either from the hierarchy or from the blocked list.
     */
    void removeFold(Fold fold) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("removeFold: " + fold + '\n');
        }
        checkNotCommitted();
        
        Fold parent = fold.getParent();
        if (parent != null) { // present in hierarchy
            int index = parent.getFoldIndex(fold);
            removeFoldFromHierarchy(parent, index, null); // no block passed here

            lastOperationFold = parent;
            lastOperationIndex = index;

        } else { // not present in hierarchy - must be blocked (or error)
            if (!execution.isBlocked(fold)) { // not blocked i.e. already removed
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Fold already removed: " + fold + '\n'); // NOI18N
                }
                return;
            } else { // Blocked fold (not present in hierarchy) -> just unblock it
                execution.unmarkBlocked(fold);
                // If the fold was blocking other folds then unblock them here
                unblockBlocked(fold);
            }
        }
        
        processUnblocked(); // attempt to reinsert unblocked folds
    }
    
    // for debugging only, set during removeAllFolds()
    private boolean removeAll;
    
    /**
     * Remove all present folds in the hierarchy
     * once the managers are going to be switched.
     */
    void removeAllFolds(Fold[] allBlocked) {
        removeAll = true;
        try {
            // First remove all blocked folds 
            for (int i = allBlocked.length - 1; i >= 0; i--) {
                removeFold(allBlocked[i]);
            }

            removeAllChildrenAndSelf(execution.getRootFold());
        } finally {
            removeAll = false;
        }
    }
    
    private void removeAllChildrenAndSelf(Fold fold) {
        int foldCount = fold.getFoldCount();
        if (foldCount > 0) {
            for (int i = foldCount - 1; i >= 0; i--) {
                removeAllChildrenAndSelf(fold.getFold(i));
            }
        }
        if (!FoldUtilities.isRootFold(fold)) {
            removeFold(fold);
        }
    }
    
    public void changedUpdate(DocumentEvent evt) {
        // No explicit checking actions upon document change notification
    }

    /**
     * Called by FoldHierarchySpi to attempt to insert
     * the fold into hierarchy. It's also possible that
     * the fold cannot be inserted and will be added to the list
     * of blocked folds.
     *
     * @param fold fold to add
     * @return true if the fold was successfully added to hierarchy
     *  or false if it could not be added and became blocked.
     */
    boolean addFold(Fold fold) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("addFold: " + fold + '\n'); // NOI18N
        }
        
        if (getOperation(fold).isReleased()) {
            throw new IllegalStateException("The manager has been already released");
        }
        checkNotCommitted();

        return addFold(fold, null, 0);
    }
    
    /**
     * Handle a special case that the found fold is actually a child of the
     * to-be-inserted one. In that case, the new fold has to be inserted before
     * the already existing child in order to the addFold() algorithm work OK.
     * Outer folds must precede nested folds.
     * 
     */
    private int findFoldInsertIndex(Fold parentFold, int startOffset, int endOffset) {
        int index = FoldUtilitiesImpl.findFoldInsertIndex(parentFold, startOffset);
        if (index < 1) {
            return index;
        }
        Fold pF = parentFold.getFold(index - 1);
        if (pF.getStartOffset() == startOffset &&
            pF.getEndOffset() < endOffset) {
            return index - 1;
        } else {
            return index;
        }
    }
    
    /**
     * Recursive method to add fold under the given parent.
     *
     * @param fold non-null fold to be inserted into hierarchy
     * @param parentFold parent fold under which to insert. If it's null
     *  then attempt to use hints from lastOperationFold and lastOperationIndex.
     *  The explicit passing of root fold can be used to force to ignore the hints.
     * @return true if the fold was successfully added or false if it became blocked.
     */
    private boolean addFold(Fold fold, Fold parentFold, int level) {
        int foldStartOffset = fold.getStartOffset();
        int foldEndOffset = fold.getEndOffset();
        int foldPriority = getOperation(fold).getPriority();
        StringBuilder sbDebug = new StringBuilder();
        boolean ea = false;
        assert ea = true;
        if (ea) sbDebug.append("\n addFold1 ENTER");
        int index;
        boolean useLast; // use hints from lastOperationFold and lastOperationIndex
        
        if (parentFold == null) { // attempt to guess
            parentFold = lastOperationFold;
            if (parentFold == null // no valid guess
                || foldStartOffset < parentFold.getStartOffset()
                || foldEndOffset > parentFold.getEndOffset()
            ) { // Use root fold
                parentFold = execution.getRootFold();
                index = findFoldInsertIndex(parentFold, foldStartOffset, foldEndOffset);
                useLast = false;
            } else {
                index = lastOperationIndex;
                useLast = true;
            }

        } else { // already valid parentFold (do not use last* vars)
            index = findFoldInsertIndex(parentFold, foldStartOffset, foldEndOffset);
            useLast = false;
        }            
        if (ea) sbDebug.append(", rootFold = " + execution.getRootFold()).append(", useLast = " + useLast + ", parentFold = " + parentFold +
                ", index = " + index);
        
        // Check whether the index is withing bounds
        int foldCount = parentFold.getFoldCount();
        if (useLast && index > foldCount) {
            index = findFoldInsertIndex(parentFold, foldStartOffset, foldEndOffset);
            useLast = false;
        }

        // Fill in the prevFold variable
        // and verify that the guessed index is correct - startOffset
        // of the prev fold must be lower than foldStartOffset
        // and start offset of the next fold must be greater than foldStartOffset

        Fold prevFold; // fold that precedes fold being added
        if (index > 0) {
            prevFold = parentFold.getFold(index - 1);
            // prev fold must end at most before this fold's start
            if (useLast && foldStartOffset < prevFold.getEndOffset()) { // bad guess
                index = findFoldInsertIndex(parentFold, foldStartOffset, foldEndOffset);
                useLast = false;
                prevFold = (index > 0) ? parentFold.getFold(index - 1) : null;
                if (ea) sbDebug.append("\n reset prev fold, new index = " + index);
            }

        } else { // index == 0
            prevFold = null;
        }

        // Fold that will follow the fold being inserted
        // By default guess it's the fold at "index" but it may be a fold
        // at higher index as well.
        Fold nextFold;
        if (index < foldCount) { // next fold exists
            nextFold = parentFold.getFold(index);           
            // TODO: >= is probably suboptimal. The next fold can start at the same offset and nest within
            if (useLast && foldStartOffset >= nextFold.getStartOffset()) { // bad guess
                index = findFoldInsertIndex(parentFold, foldStartOffset, foldEndOffset);
                useLast = false;
                prevFold = (index > 0) ? parentFold.getFold(index - 1) : null;
                nextFold = (index < foldCount) ? parentFold.getFold(index) : null;
                if (ea) sbDebug.append("\n reset next fold, new index = " + index);
            }

        } else { // index >= foldCount
            nextFold = null;
        }

        if (ea) sbDebug.append("\nprevFold = " + prevFold + ", nextFold = " + nextFold);
        // Check whether the fold to be added overlaps
        // with previous fold (it's start offset is before end offset
        // of the previous fold.
        // Check whether end offset of the fold
        // does not overlap with folds that would follow it
        boolean blocked;
        // Index hints:
        //   null - no overlapping (clear insert of start offset)
        //   length == 0 - overlapping but no children
        //   length > 0 - overlapping and children - see inspectOverlap()
        int[] prevOverlapIndexes;
        if (prevFold != null && foldStartOffset < prevFold.getEndOffset()) { // overlap
            if (foldEndOffset <= prevFold.getEndOffset()) { // fold fully nested
                if (level < MAX_NESTING_LEVEL) {
                    if (getManager(fold) == getManager(prevFold) &&
                        fold.getStartOffset() == prevFold.getStartOffset() &&
                        fold.getEndOffset() == prevFold.getEndOffset())
                    {
                        if (LOG.isLoggable(Level.WARNING)) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Adding a fold that is identical with another previously added fold " + //NOI18N
                                "from the same FoldManager is not allowed.\n"); //NOI18N
                            sb.append("Existing fold: "); //NOI18N
                            sb.append(prevFold.toString());
                            sb.append("; FoldManager: ").append(getManager(prevFold)); //NOI18N
                            sb.append("\n"); //NOI18N
                            sb.append("     New fold: "); //NOI18N
                            sb.append(fold.toString());
                            sb.append("; FoldManager: ").append(getManager(fold)); //NOI18N
                            sb.append("\n"); //NOI18N

                            LOG.warning(sb.toString());
                        }
                    } else {
                        // Nest into prevFold
                        return addFold(fold, prevFold, level + 1);
                    }
                } else {
                    if (LOG.isLoggable(Level.WARNING)) {
                        StringBuilder sb = new StringBuilder();

                        Document doc = getOperation(prevFold).getDocument();
                        sb.append("Too many nested folds in "); //NOI18N
                        sb.append(doc.getClass().getName());
                        sb.append("@"); //NOI18N
                        sb.append(Integer.toHexString(System.identityHashCode(doc)));
                        sb.append("['").append((String)doc.getProperty("mimeType")).append("', "); //NOI18N
                        sb.append(findFilePath(doc)).append("]\n"); //NOI18N

                        sb.append("Dumping the nesting folds:\n"); //NOI18N
                        for(Fold f = prevFold; f != null; f = f.getParent()) {
                            sb.append(f.toString());
                            sb.append("; FoldManager: ").append(getManager(f));
                            sb.append("\n"); //NOI18N
                        }

                        boolean assertionsOn = false;
                        assert assertionsOn = true;
                        if (assertionsOn) {
                            LOG.log(Level.WARNING, null, new Throwable(sb.toString()));
                        } else {
                            LOG.warning(sb.toString());
                        }
                    }
                }

                blocked = true;
                addFoldBlock = prevFold;
                prevOverlapIndexes = null;
                
            } else { // fold overlaps with prevFold
                if (foldPriority > getOperation(prevFold).getPriority()) { // can replace
                    if (prevFold.getFoldCount() > 0) { // must check children too
                        prevOverlapIndexes = inspectOverlap(prevFold,
                            foldStartOffset, foldPriority, 1);

                        if (prevOverlapIndexes == null) { // blocked
                            // "addFoldBlock" var was assigned by inspectOverlap()
                            blocked = true;
                        } else { // not blocked
                            blocked = false;
                        }

                    } else { // prevFold has no children
                        blocked = false;
                        prevOverlapIndexes = EMPTY_INT_ARRAY;
                    }
                } else { // cannot remove -> overlaps
                    blocked = true;
                    addFoldBlock = prevFold;
                    prevOverlapIndexes = null; 
                }
            }

        } else { // no overlapping with prevFold -> insert after
            blocked = false;
            prevOverlapIndexes = null;
        }
        
        if (!blocked) {
            // Which fold will be the next important for the insert (possibly overlapped)
            int nextIndex = index;
            if (ea) sbDebug.append("\n addFold2 nextIndex:" + nextIndex + " index:" + index);
            // Non-null in case of active overlapping for foldEndOffset
            int[] nextOverlapIndexes = null;
            if (nextFold != null) { // next fold exists
                if (foldEndOffset > nextFold.getStartOffset()) {
                    // End inside or after the current fold
                    if (foldEndOffset >= nextFold.getEndOffset()) {
                        // Fold ends after end offset of the current nextFold
                        // Find the fold in (or after) which the inserted fold really ends.
                        // Do binary search to have deterministic non-linear perf
                        // Third param is false i.e. get possibly last fold
                        //  in multiple empty folds (same like in findFoldInsertIndex())
                        nextIndex = FoldUtilitiesImpl.findFoldStartIndex(parentFold,
                        foldEndOffset, false);

                        // nextIndex should not be -1 - otherwise should not reach this code
                        nextFold = parentFold.getFold(nextIndex);

                        if (ea) sbDebug.append("\n addFold3 nextIndex = FoldUtilitiesImpl.findFoldStartIndex(parentFold, foldEndOffset, false)"
                        + " nextIndex:" + nextIndex + " index:" + index + ", new nextFold = " + nextFold);
                    }

                    if (foldEndOffset < nextFold.getEndOffset()) { // ends inside
                        if (foldPriority > getOperation(nextFold).getPriority()) { // remove next fold
                            if (nextFold.getFoldCount() > 0) { // next has children
                                nextOverlapIndexes = inspectOverlap(nextFold, foldEndOffset, foldPriority, 1);
                                if (nextOverlapIndexes == null) { // blocked
                                    // "addFoldBlock" var was assigned by inspectOverlap()
                                    blocked = true;
                                } // can remove nested folds

                            } else { // nextFold has no children => can be removed
                                nextOverlapIndexes = EMPTY_INT_ARRAY;
                            }

                        } else { // blocked by next fold
                            blocked = true;
                            addFoldBlock = nextFold;
                        }

                    } else { // fold ends after bounds of nextFold but prior start of next fold
                        nextIndex++; // insert clearly after the nextFold
                        if (ea) sbDebug.append("\n addFold4 nextIndex++ nextIndex:" + nextIndex + " index:" + index);
                    }

                } // fold ends before start offset of nextFold => insert normally later
            } // next fold does not exist - no folds at index or after it

            
            if (!blocked) {
                // Here it should be possible to insert the fold
                // prevOverlapIndexes and nextOverlapIndexes need to be resolved first
                // (and the possible index shift consequences)
                // Finally the lastOperationFold and lastOperationIndex
                // should be set for future use.
                
                if (prevOverlapIndexes != null) {
                    int replaceIndexShift;
                    if (prevOverlapIndexes.length == 0) { // no children
                        replaceIndexShift = 0;
                    } else { // children
                        replaceIndexShift = removeOverlap(prevFold,
                            prevOverlapIndexes, fold);
                        // Must shift nextIndex by number of replaced children
                        nextIndex += prevFold.getFoldCount();
                        if (ea) sbDebug.append("\n addFold5 nextIndex += prevFold.getFoldCount()"
                        + " nextIndex:" + nextIndex + " index:" + index);
                    }

                    removeFoldFromHierarchy(parentFold, index - 1, fold);
                    index += replaceIndexShift - 1; // -1 for removed prevFold
                    nextIndex--; // -1 for removed prevFold
                    if (ea) sbDebug.append("\n addFold6 nextIndex-- nextIndex:" + nextIndex + " index:" + index);
                }
                
                if (nextOverlapIndexes != null) {
                    int replaceIndexShift;
                    if (nextOverlapIndexes.length == 0) { // no children
                        replaceIndexShift = 0;
                    } else { // children
                        replaceIndexShift = removeOverlap(nextFold,
                            nextOverlapIndexes, fold);
                    }
                    
                    removeFoldFromHierarchy(parentFold, nextIndex, fold);
                    nextIndex += replaceIndexShift;
                    if (ea) sbDebug.append("\n addFold7 nextIndex += replaceIndexShift"
                    + " nextIndex:" + nextIndex
                    + " replaceIndexShift:" + replaceIndexShift + " index:" + index);
                }
                if (ea) sbDebug.append("\n addFold8 INVOKE ApiPackageAccessor.get().foldExtractToChildren"
                + " index:" + index + " nextIndex:" + nextIndex + " diff:" + (nextIndex - index)
                + " parentFold.getFoldCount():" + parentFold.getFoldCount());
                if (!((nextIndex - index) >= 0)) {
                    throw new HierarchyErrorException(parentFold, fold, index, true, "Negative length");
                }
                if (!(nextIndex <= parentFold.getFoldCount())) {
                    throw new HierarchyErrorException(parentFold, fold, index, true, "End index exceeds children list size." + sbDebug.toString());
                }
                ApiPackageAccessor.get().foldExtractToChildren(parentFold, index, nextIndex - index, fold);
                
                // sanity check:
                Fold realPF = fold.getParent();
                if (realPF == null || realPF != parentFold || realPF.getStartOffset() > fold.getStartOffset() || realPF.getEndOffset() < fold.getEndOffset()) {
                    LOG.warning("Invalid parent fold after insertion. Fold = " + fold + ", parent = " + realPF);
                    LOG.warning("debug info: " + sbDebug.toString());
                } else {

                    int ix = realPF.getFoldIndex(fold);
                    Fold realPrev = ix > 0 ? realPF.getFold(ix - 1) : null;
                    Fold realNext = ix < realPF.getFoldCount() - 1 ? realPF.getFold(ix + 1) : null;
                    if ((realPrev != null && realPrev.getEndOffset() > fold.getStartOffset()) ||
                        (realNext != null && realNext.getStartOffset() < fold.getEndOffset())) {
                        LOG.warning("Invalid next/prev offsets: fold = " + fold + ", prev = " + realPrev + ", next = " + realNext);
                        LOG.warning("debug info: " + sbDebug.toString());
                    }
                }
                
                // Update affected offsets
                updateAffectedOffsets(fold);
                markFoldAddedToHierarchy(fold);
                processUnblocked();
            }
        }
        
        if (blocked) {
             // Fold is blocked - "addFoldBlock" var holds the blocker
            execution.markBlocked(fold, addFoldBlock);
            addFoldBlock = null; // enable GC
        }
        
        // Remember hints for next call
        lastOperationFold = parentFold;
        lastOperationIndex = index + 1;
        
        return !blocked;
    }
    
    private String findFilePath(Document doc) {
        Object o = doc.getProperty(Document.StreamDescriptionProperty);
        if (o != null) {
            return o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o)) + ":" + o.toString(); //NOI18N
        } else {
            return "null"; //NOI18N
        }
    }
    
    /**
     * Nested check of possibility of inserting a fold.
     *
     * @param fold that has at least one child fold. Folds with empty
     *  children cannot be used here.
     * @param offset of inserting
     * @param priority of the fold
     * @param level nesting level of check - starting at 1
     * @return array of ints containing deepest-level + 1 entries
     *   where each item presents the index of the overlapped item
     *   that needs to be removed. The first array item
     *   is either 0 - clean insert after the index inside the deepest level
     *   or 1 - overlapping but removable (has no children).
     *   <br>
     *   <code>null</code> is returned if folds overlap but priority
     *   of present fold is higher so the attempted fold will become blocked.
     *   <code>checkOverlapBlock</code> will be filled with the deep fold
     *   that actually blocks.
     *
     *   <p>
     *   Example:<pre>
     *   [0] = 0   - clean insert after fold at index 5
     *   [1] = 2   - overlapping with fold at index 2 at level 1
     *   [2] = 5   - deepest level ?overlapping? => no, index 0 says clean insert
     *   </pre>
     *
     *   <p>
     *   Example 2:<pre>
     *   [0] = 1   - overlap with fold at index 4 (fold will be removed)
     *   [1] = 1   - overlapping with fold at index 1 at level 1
     *   [2] = 4   - deepest level ?overlapping? => yes, index 0 says overlapping
     *   </pre>

     */
    private int[] inspectOverlap(Fold fold, int offset, int priority, int level) {
        int index = FoldUtilitiesImpl.findFoldStartIndex(fold, offset, false);
        int[] result;
        Fold indexFold;
        if (index >= 0 && FoldUtilities.containsOffset(
            (indexFold = fold.getFold(index)), offset)
        ) {
            if (priority > getOperation(indexFold).getPriority()) { // can be replaced
                if (indexFold.getFoldCount() > 0) { // has non-empty children
                    result = inspectOverlap(indexFold, offset, priority, level + 1);
                    if (result != null) { // no blocking in children
                        result[level] = index;
                    } // result == null => blocking in children
                    
                } else { // has no or empty children
                    result = new int[level + 1];
                    result[0] = 1; // overlapping at the last level
                    result[level] = index; // will later insert at index 0
                }
            } else { // higher priority of existing fold -> return null
                addFoldBlock = indexFold; // remember the blocking fold
                result = null;
            }

        } else { // before first child fold or no overlapping
            result = new int[level + 1];
            result[0] = 0; // clearly nested
            result[level] = index; // will later insert at index 0
        }
        return result;
    }
    
    /**
     * Remove overlapping folds based on information from previous call
     * to <code>inspectOverlap()</code>.
     *
     * @param fold fold which blocking children will be removed. The fold itself
     *  will remain (must be removed by caller).
     * @param indexes indexes array obtained by previous call to inspectOverlap().
     * @param block blocking fold that will be used when marking the removed
     *  children as blocked.
     *
     * @return fold insert index that corresponds to the originally used offset.
     */
    private int removeOverlap(Fold fold, int[] indexes, Fold block) {
        int indexShift = 0; // how many new children was inserted prior to offset
        int indexesLengthM1 = indexes.length - 1;
        for (int i = 1; i < indexesLengthM1; i++) {
            int index = indexes[i] + indexShift;
            removeFoldFromHierarchy(fold, index, block);
            indexShift = index;
        }
        
        // Need to process last (most inner) fold
        int index = indexes[indexesLengthM1] + indexShift;
        if (indexes[0] == 0) { // clearly nested after the fold
            index++; // move after the fold
        } else { // indexes[0] == 1 => remove the overlap fold
            removeFoldFromHierarchy(fold, index, block);
        }
        return index;
    }
    
    /**
     * Physically remove the fold from the hierarchy and update the appropriate
     * state variables.
     */
    private void removeFoldFromHierarchy(Fold parentFold, int index, Fold block) {
        Fold removedFold = parentFold.getFold(index);
        updateAffectedOffsets(removedFold);
        ApiPackageAccessor.get().foldReplaceByChildren(parentFold, index);
        markFoldRemovedFromHierarchy(removedFold);
        unblockBlocked(removedFold);
        // only retain in blocked if the fold is valid:
        if (block != null) {
            int so = removedFold.getStartOffset();
            int eo = removedFold.getEndOffset();
            if (so < eo) {
                execution.markBlocked(removedFold, block);
            } else {
                execution.markDamaged();
            }
        }
    }
    
    /**
     * Remove the block that was removed from hierarchy
     * because of adding of another fold. Remember
     * all folds that were blocked by the remove block
     * because they will be attempted to be reinserted
     * prior committing of this transaction.
     */
    private void unblockBlocked(Fold block) {
        Set blockedSet = execution.unmarkBlock(block);
        if (blockedSet != null) {
            for (Iterator it = blockedSet.iterator(); it.hasNext();) {
                Fold blocked = (Fold)it.next();
                int priority = getOperation(blocked).getPriority();
                while (unblockedFoldLists.size() <= priority) {
                    unblockedFoldLists.add(new ArrayList(4));
                }
                ((List)unblockedFoldLists.get(priority)).add(blocked);
                if (priority > unblockedFoldMaxPriority) {
                    unblockedFoldMaxPriority = priority;
                }
            }
        }
    }

    /**
     * Attempt to reinsert the folds unblocked by particular add/remove operation.
     */
    private void processUnblocked() {
        ApiPackageAccessor api = ApiPackageAccessor.get();
        if (unblockedFoldMaxPriority >= 0) { // some folds became unblocked
            for (int priority = unblockedFoldMaxPriority; priority >= 0; priority--) {
                List foldList = (List)unblockedFoldLists.get(priority);
                Fold rootFold = execution.getRootFold();
                for (int i = foldList.size() - 1; i >= 0; i--) {
                    // Remove last fold from the list
                    Fold unblocked = (Fold)foldList.remove(i);
                    
                    if (!execution.isAddedOrBlocked(unblocked)) { // not yet processed
                        unblockedFoldMaxPriority = -1;

                        int start = unblocked.getStartOffset();
                        int end = unblocked.getEndOffset();
                        if (start >= end) {
                            api.foldMarkDamaged(unblocked, FoldUtilitiesImpl.FLAG_START_DAMAGED | FoldUtilitiesImpl.FLAG_END_DAMAGED);
                            getManager(unblocked).removeEmptyNotify(unblocked);
                            continue;
                        }
                        // Attempt to reinsert the fold - random order - use root fold
                        addFold(unblocked, rootFold, 0);

                        if (unblockedFoldMaxPriority >= priority) {
                            throw new IllegalStateException("Folds removed with priority=" // NOI18N
                                + unblockedFoldMaxPriority);
                        }
                        if (foldList.size() != i) {
                            throw new IllegalStateException("Same priority folds removed"); // NOI18N
                        }
                    }
                }
            }
        }
        unblockedFoldMaxPriority = -1;
    }
    
    /**
     * Removes the fold from the hierarchy temporarily, and re-adds it at the end of the transaction at the appropriate
     * position in the tree. This operation preserves Fold identity and could cause less unexpected collapsing when
     * folds are recomputed.<p>
     * The entire fold subtree will be removed from the hierarchy and put to the reinsert set (linked) in the 
     * 
     * @param f fold
     */
    void reinsertFoldTree(Fold f) {
        if (reinsertSet != null && reinsertSet.contains(f)) {
            return;
        }
        boolean bl = false;
        if (f.getParent() == null) {
            if (execution.isBlocked(f)) {
                execution.unmarkBlocked(f);
                bl = true;
            } else {
                return;
            }
        }
        if (reinsertSet == null) {
            reinsertSet = new LinkedHashSet<>();
        }
        if (bl) {
            if (f.getFoldCount() > 0 || f.getParent() != null) {
                LOG.warning("Blocked fold should have no parent and no children: " + f + ", dumping hierarchy: " + execution);
            }
            if (lastOperationFold == f) {
                lastOperationFold = null;
                lastOperationIndex = -1;
            }
            reinsertSet.add(f);
        } else {
            Collection<Fold> c = new ArrayList<Fold>();
            ApiPackageAccessor.get().foldTearOut(f, c);
            if (f.getFoldCount() > 0 || f.getParent() != null) {
                LOG.warning("Paret fold should have no parent and no children: " + f + ", dumping hierarchy: " + execution);
            }
            for (Fold x : c) {
                if (execution.isBlocked(x)) {
                    execution.unmarkBlocked(x);
                }
                unblockBlocked(x);
                if (lastOperationFold == x) {
                    lastOperationFold = null;
                    lastOperationIndex = -1;
                }
                if (x.getFoldCount() > 0 || x.getParent() != null) {
                    LOG.warning("Teared-out fold should have no parent and no children: " + f + ", dumping hierarchy: " + execution);
                }
            }
            reinsertSet.addAll(c);
        }
        processUnblocked();
    }
    
    boolean isReinserting(Fold f) {
        return reinsertSet != null && reinsertSet.contains(f);
    }

    private void markFoldAddedToHierarchy(Fold fold) {
        // Check and remove from removedFromHierarchySet if marked removed
        if (removedFromHierarchySet == null || !removedFromHierarchySet.remove(fold)) {
            if (addedToHierarchySet == null) {
                addedToHierarchySet = new HashSet();
            }
            addedToHierarchySet.add(fold);
        }
    }
    
    private void markFoldRemovedFromHierarchy(Fold fold) {
        // Check and remove from addedToHierarchySet if marked added
        if (addedToHierarchySet == null || !addedToHierarchySet.remove(fold)) {
            if (removedFromHierarchySet == null) {
                removedFromHierarchySet = new HashSet();
            }
            removedFromHierarchySet.add(fold);
        }
    }
    
    
    private void updateAffectedOffsets(Fold fold) {
        int startOffset = fold.getStartOffset();
        int endOffset = fold.getEndOffset();
        if (startOffset > endOffset) {
            execution.markDamaged();
            LOG.warning("Invalid fold range: " + fold + ". Dumping hierarchy");
            LOG.warning(execution.toString());
            // take the damaged area from the parent
            Fold f = fold.getParent();
            startOffset = f.getStartOffset();
            endOffset = f.getEndOffset();
        }
        updateAffectedStartOffset(startOffset);
        updateAffectedEndOffset(endOffset);
    }

    /**
     * Extend affectedStartOffset in downward direction.
     */
    private void updateAffectedStartOffset(int offset) {
        if (offset >= 0 && (affectedStartOffset == -1 || offset < affectedStartOffset)) {
            if (affectedEndOffset < 0 || offset <= affectedEndOffset) {
                affectedStartOffset = offset;
            }
        }
    }
            
    /**
     * Extend affectedEndOffset in upward direction.
     */
    private void updateAffectedEndOffset(int offset) {
        if (offset >= 0 && offset > affectedEndOffset && offset >= affectedStartOffset) {
            affectedEndOffset = offset;
        }
    }
            
    private void checkNotCommitted() {
        if (committed) {
            throw new IllegalStateException("FoldHierarchyChange already committed."); // NOI18N
        }
    }
    
}
