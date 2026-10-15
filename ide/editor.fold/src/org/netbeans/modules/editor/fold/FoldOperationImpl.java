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
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.spi.editor.fold.FoldInfo;
import org.netbeans.api.editor.fold.FoldStateChange;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.openide.util.Exceptions;


/**
 * This is SPI (Service Provider Interface) object corresponding
 * to the <code>FoldHierarchy</code> in one-to-one relationship.
 * <br>
 * The <code>FoldHierarchy</code> delegates all its operations
 * to this object.
 *
 * <p>
 * All the changes performed in to the folds are always done
 * in terms of a transaction represented by {@link FoldHierarchyTransaction}.
 * The transaction can be opened by {@link #openTransaction()}.
 *
 * <p>
 * This class changes its state upon displayability change
 * of the associated copmonent by listening on "ancestor" component property.
 * <br>
 * If the component is not displayable then the list of root folds becomes empty
 * while if the component gets displayable the root folds are created
 * according to registered managers.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class FoldOperationImpl {

    // -J-Dorg.netbeans.api.editor.fold.FoldHierarchy.level=FINEST
    private static final Logger LOG = Logger.getLogger(FoldHierarchy.class.getName());
    
    private FoldOperation operation;
    
    private FoldHierarchyExecution execution;
    
    private FoldManager manager;
    
    private int priority;
    
    private boolean released;
    
    public FoldOperationImpl(FoldHierarchyExecution execution,
    FoldManager manager, int priority) {
        this.execution = execution;
        this.manager = manager;
        this.priority = priority;

        this.operation = SpiPackageAccessor.get().createFoldOperation(this);
        if (manager != null) { // manager for root-fold is null
            manager.init(getOperation());
        }
    }
    
    public FoldOperation getOperation() {
        return operation;
    }
    
    public void initFolds(FoldHierarchyTransactionImpl transaction) {
        manager.initFolds(transaction.getTransaction());
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Fold Hierarchy after initFolds():\n" + execution + '\n');
            execution.checkConsistency();
        }
    }
    
    public FoldHierarchy getHierarchy() {
        return execution.getHierarchy();
    }
    
    public FoldManager getManager() {
        return manager;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public Document getDocument() {
        return execution.getComponent().getDocument();
    }
    
    public Fold createFold(FoldType type, String description, boolean collapsed,
    int startOffset, int endOffset, int startGuardedLength, int endGuardedLength,
    Object extraInfo)
    throws BadLocationException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Creating fold: type=" + type // NOI18N
                + ", description='" + description + "', collapsed=" + collapsed // NOI18N
                + ", startOffset=" + startOffset + ", endOffset=" + endOffset // NOI18N
                + ", startGuardedLength=" + startGuardedLength // NOI18N
                + ", endGuardedLength=" + endGuardedLength // NOI18N
                + ", extraInfo=" + extraInfo + '\n' // NOI18N
            );
            
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.INFO, "Fold creation stack", new Exception());
            }
        }
        if (type == null) {
            LOG.warning("Null fold type supplier for fold start=" + startOffset + ", end=" + endOffset + 
                        "by manager " + manager);
            type = FoldType.CODE_BLOCK;
        }

        return getAccessor().createFold(this,
            type, description, collapsed,
            getDocument(), startOffset, endOffset,
            startGuardedLength, endGuardedLength,
            extraInfo
        );
    }
    
    public Object getExtraInfo(Fold fold) {
        checkFoldOperation(fold);
        return getAccessor().foldGetExtraInfo(fold);
    }
    
    public boolean isStartDamaged(Fold fold) {
        checkFoldOperation(fold);
        return getAccessor().foldIsStartDamaged(fold);
    }
    
    public boolean isEndDamaged(Fold fold) {
        checkFoldOperation(fold);
        return getAccessor().foldIsEndDamaged(fold);
    }
    
    public FoldHierarchyTransactionImpl openTransaction() {
        return execution.openTransaction();
    }
    
    public boolean addToHierarchy(Fold fold, FoldHierarchyTransactionImpl transaction) {
        checkFoldOperation(fold);
        try {
            execution.incModCount();
            return execution.add(fold, transaction);
        } catch (HierarchyErrorException ex) {
            try {
                rebuildHierarchy(ex);
                return execution.add(fold, transaction);
            } catch (HierarchyErrorException ex2) {
                Exceptions.printStackTrace(ex2);
                return false;
            }
        }
    }

    public void removeFromHierarchy(Fold fold, FoldHierarchyTransactionImpl transaction) {
        checkFoldOperation(fold);
        try {
            execution.incModCount();
            execution.remove(fold, transaction);
        } catch (HierarchyErrorException ex) {
            rebuildHierarchy(ex);
            try {
                execution.remove(fold, transaction);
            } catch (HierarchyErrorException ex2) {
                Exceptions.printStackTrace(ex2);
            }
        }
    }
    
    public boolean isAddedOrBlocked(Fold fold) {
        checkFoldOperation(fold);
        return execution.isAddedOrBlocked(fold);
    }
    
    public boolean isBlocked(Fold fold) {
        checkFoldOperation(fold);
        return execution.isBlocked(fold);
    }
    
    public void setEndOffset(Fold fold, int endOffset, FoldHierarchyTransactionImpl transaction)
    throws BadLocationException {
        checkFoldOperation(fold);
        int origEndOffset = fold.getEndOffset();
        if (origEndOffset == endOffset) {
            return;
        }
        ApiPackageAccessor api = getAccessor();
        FoldStateChange state = transaction.getFoldStateChange(fold);
        if (state.getOriginalStartOffset() >= 0 && state.getOriginalStartOffset() > endOffset) {
            LOG.warning("Original start offset > end offset, dumping fold hierarchy: " + execution);
        }
        api.foldSetEndOffset(fold, getDocument(), endOffset);
        api.foldStateChangeEndOffsetChanged(transaction.getFoldStateChange(fold), origEndOffset);
    }
    
    public void insertUpdate(DocumentEvent evt, FoldHierarchyTransactionImpl transaction) {
        if (!isReleased()) {
            manager.insertUpdate(evt, transaction.getTransaction());
        }
    }
    
    public void removeUpdate(DocumentEvent evt, FoldHierarchyTransactionImpl transaction) {
        if (!isReleased()) {
            manager.removeUpdate(evt, transaction.getTransaction());
        }
    }
    
    public void changedUpdate(DocumentEvent evt, FoldHierarchyTransactionImpl transaction) {
        if (!isReleased()) {
            manager.changedUpdate(evt, transaction.getTransaction());
        }
    }

    public void release() {
        released = true;
        manager.release();
    }
    
    public boolean isReleased() {
        return released;
    }
    
    public void rebuildHierarchy(HierarchyErrorException ex) {
        LOG.log(Level.WARNING, "Hirerachy error in parent {0}, index {1}, fold {2}, operation: {3}",
                new Object[] {
                    ex.getParentFold(),
                    ex.getOpAtIndex(),
                    ex.getInsertOrRemove(),
                    ex.isAdd() ? "add" : "remove"
        });
        LOG.log(Level.FINE, "Stacktrace: ", Exceptions.attachSeverity(ex, Level.FINER));
        execution.rebuildHierarchy();
    }
    
    /**
     * Enumerates all folds contributed by this Operation, whether blocked or active.
     * 
     * @return 
     */
    public Iterator<Fold>   foldIterator() {
        return new BI(new DFSI(execution.getRootFold()));
    }
    
    private void checkFoldOperation(Fold fold) {
        FoldOperationImpl foldOperation = getAccessor().foldGetOperation(fold);
        if (foldOperation != this) {
            throw new IllegalStateException(
                "Attempt to use the fold " + fold // NOI18N
                + " with invalid fold operation " // NOI18N
                + foldOperation + " instead of " + this // NOI18N
            );
        }
    }
    
    private static ApiPackageAccessor getAccessor() {
        return ApiPackageAccessor.get();
    }
    
    /**
     * Compares two folds A, B. Fold "A" precedes "B", if and only if:
     * <ul>
     * <li>A fully encloses B, or
     * <li>A starts before B, or
     * <li>A and B occupy the same range, and A's priority is lower
     * </ul>
     * 
     * @param a fold to compare
     * @param b fold to compare
     * @return -1, 1, 0 as appropriate for a Comparator
     */
    private static final Comparator<Fold> FOLD_COMPARATOR = new Comparator<Fold>() {
        @Override
        public int compare(Fold a, Fold b) {
            int diff = a.getStartOffset() - b.getStartOffset();
            if (diff != 0) {
                return diff;
            }
            int diff2 = b.getEndOffset() - a.getEndOffset();
            if (diff2 != 0) {
                return diff2;
            }
            ApiPackageAccessor accessor = getAccessor();
            return accessor.foldGetOperation(a).getPriority() - accessor.foldGetOperation(b).getPriority();
        }
    };

    /**
     * Level of depth-first traversal
     */
    static class PS {
        private Fold    parent;
        private int     childIndex = -1;
        private PS      next;
        
        PS(Fold parent, PS next) {
            this.parent = parent;
            this.next = next;
        }
    }
    
    /**
     * Implmentation of depth-first pre-order traversal through Fold hierarchy.
     * Each level is iterated in the fold order = start offset order.
     */
    private class DFSI implements Iterator<Fold> {
        PS  level;
        
        private DFSI(Fold root) {
            level = new PS(root, null);
        }
        
        @Override
        public Fold next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            if (level.childIndex == -1) {
                level.childIndex++;
                return level.parent;
            }
            // note that hasNext also pops levels, as necessary, so there's a
            // level, which is not yet exhausted.
            Fold f = level.parent.getFold(level.childIndex++);
            if (f.getFoldCount() > 0) {
                level = new PS(f, level);
                level.childIndex++;
                return level.parent;
            }
            return f;
        }
        
        @Override
        public boolean hasNext() {
            while (level != null) {
                if (level.childIndex == -1) {
                    return true;
                } else if (level.childIndex >= level.parent.getFoldCount()) {
                    level = level.next;
                } else {
                    return true;
                }
            }
            return false;
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    /**
     * Iterator, which processes all blocked folds along with their blocker.
     * The blocker + blockers folds are ordered using the fold order. Results
     * are filtered to contain just Folds produced by this Operation. Folds 
     * owned by other operations/executions are skipped.
     * <p>
     * Note that blocked folds do not form a hierarchy; they were removed from
     * the fold hierarchy when it was decided to block those folds. So prior to
     * iterating further in FoldHierarchy, all (recursively) blocked folds must
     * be processed.
     */
    private class BI implements Iterator<Fold> {
        private Iterator<Fold>  dfsi;
        private Iterator<Fold>  blockedFolds;
        private Fold ret;
        private Stack<Object[]> blockStack = new Stack<Object[]>();
        private Fold blocker;

        public BI(Iterator<Fold> dfsi) {
            this.dfsi = dfsi;
        }
        
        /**
         * If fold 'f' blocks some other folds, those blocked folds will b processed
         * instead of 'f'. f will be mixed among and ordered with its blocked folds, so the
         * entire chain will be processed in the document order.
         * 
         * @param f
         * @return true, if blocked folds should be processed.
         */
        private boolean processBlocked(Fold f) {
            if (f == blocker) {
                return false;
            }
            Collection<Fold> blocked = execution.getBlockedFolds(f);
            if (blocked != null && !blocked.isEmpty()) {
                List<Fold> blockedSorted = new ArrayList<Fold>(blocked.size() + 1);
                blockedSorted.addAll(blocked);
                // enumerate together with blocked ones
                blockedSorted.add(f);
                blockedSorted.sort(FOLD_COMPARATOR);
                blockStack.push(new Object[] { blockedFolds, blocker});
                blockedFolds = blockedSorted.iterator();
                blocker = f;
                return true;
            } else {
                return false;
            }
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public Fold next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Fold f = ret;
            ret = null;
            return f;
        }
        
        @Override
        public boolean hasNext() {
            if (ret != null) {
                return true;
            }
            if (blockedFolds != null) {
                while (blockedFolds.hasNext()) {
                    Fold f = blockedFolds.next();
                    if (processBlocked(f)) {
                        // continue with a different level of blocking
                        continue;
                    }
                    if (operation.owns(f)) {
                        ret = f;
                        return true;
                    }
                }
                blockedFolds = null;
            }
            if (!blockStack.isEmpty()) {
                Object[] o = blockStack.pop();
                blocker = (Fold)o[1];
                blockedFolds = (Iterator<Fold>)o[0];
                return hasNext();
            }
            
            while (dfsi.hasNext()) {
                Fold f = dfsi.next();
                if (processBlocked(f)) {
                    return hasNext();
                }
                if (operation.owns(f)) {
                    ret = f;
                    return true;
                }
            }
            return false;
        }
    }
    
    public Map<FoldInfo, Fold> update(Collection<FoldInfo> fi, Collection<Fold> removed, Collection<FoldInfo> created) throws BadLocationException {
        Refresher r;
        if (isReleased()) {
            return null;
        }
        if (!execution.isLockedByCaller()) {
            throw new IllegalStateException("Update must run under FoldHierarchy lock");
        }
        try {
            r = new Refresher(fi);
            r.run();
        } catch (HierarchyErrorException ex) {
            // second attempt:
            rebuildHierarchy(ex);
            r = new Refresher(fi);
            try {
                r.run();
            } catch (HierarchyErrorException ex2) {
                Exceptions.printStackTrace(ex2);
                return null;
            }
        }
        if (removed != null) {
            removed.addAll(r.toRemove);
        }
        if (created != null) {
            created.addAll(r.toAdd);
        }
        return r.currentFolds;
    }
    
    private void checkLocked() {
        Document d = getDocument();
            if (d != null) {
                // diagnostics, not throwing exceptions since it could disturb
                // editing
                if (!DocumentUtilities.isReadLocked(d)) {
                    LOG.log(Level.WARNING, "Underlying document not read/write locked", 
                            Exceptions.attachSeverity(new Throwable(), Level.FINE));
                }
                if (!execution.isLockedByCaller()) {
                    LOG.log(Level.WARNING, "Fold hierarchy is not locked on transaction open", 
                            Exceptions.attachSeverity(new Throwable(), Level.FINE));
                }
            }
    }
    
    public boolean getInitialState(FoldType ft) {
        return execution.getInitialFoldState(ft);
    }

    private class Refresher implements Comparator<FoldInfo> {
        private Collection<FoldInfo>    foldInfos;
        // toRemove will be interated in the reverse order, cannot be represented together with removeFolds as LinkedHashSet.
        // removedFolds will be checked often, so HashSet saves some lookup time compared to List
        private List<Fold>              toRemove = new ArrayList<Fold>();
        private Set<Fold>               removedFolds = new HashSet<Fold>();
        private Collection<FoldInfo>    toAdd = new ArrayList<FoldInfo>();
        private Map<FoldInfo, Fold>     currentFolds = new LinkedHashMap<FoldInfo, Fold>();
        private Map<Fold, FoldInfo>     foldsToUpdate = new IdentityHashMap<Fold, FoldInfo>();

        /**
         * Transaction which covers the update
         */
        private FoldHierarchyTransactionImpl tran;

        public Refresher(Collection<FoldInfo> foldInfos) {
            this.foldInfos = foldInfos;
        }
        
        @Override
        public int compare(FoldInfo a, FoldInfo b) {
            int diff = a.getStart() - b.getStart();
            if (diff != 0) {
                return diff;
            }
            int diff2 = b.getEnd() - a.getEnd();
            return diff2;
        }
        
            
        private int compare(FoldInfo info, Fold f) {
            if (info == null) {
                if (f == null) {
                    return 0;
                } else {
                    return 1;
                }
            } else if (f == null) {
                return -1;
            }

            int diff = info.getStart() - f.getStartOffset();
            if (diff != 0) {
                return diff;
            }
            diff = f.getEndOffset() - info.getEnd();
            if (diff != 0) {
                return diff;
            }
            if (info.getType() == f.getType()) {
                return 0;
            }
            return info.getType().code().compareToIgnoreCase(f.getType().code());
        }
        
        private Iterator<Fold>  foldIt;
        private Iterator<FoldInfo>  infoIt;
        private FoldInfo nextInfo;
        
        private FoldInfo ni() {
            FoldInfo f = null;
            do {
                if (nextInfo != null) {
                    f = nextInfo;
                    nextInfo = null;
                    if (isValidFold(f)) {
                        return f;
                    }
                } 
                if (infoIt.hasNext()) {
                    f = infoIt.next();
                } else {
                    return null;
                }
                // ignore folds with invalid boundaries
            } while (!isValidFold(f));
            return f;
        }
        
        private FoldInfo peek() {
            FoldInfo f = ni();
            nextInfo = f;
            return f;
        }
        
        private boolean containsOneAnother(FoldInfo i, Fold f) {
            int s1 = i.getStart();
            int s2 = f.getStartOffset();
            int e1 = i.getEnd();
            int e2 = f.getEndOffset();
            
            return ((s1 >= s2 && e2 >= e1)  ||
                (s2 >= s1 && e1 >= e2));
        }
        
        private boolean nextSameRange(FoldInfo i, Fold f) {
            if (i == null || f == null) {
                return false;
            }
            if (i.getType() != f.getType() || !containsOneAnother(i, f)) {
                return false;
            }
            FoldInfo next = peek();
            if (next == null) {
                return true;
            }
            // do not attempt to resize non-leaf folds; the shifting feature is
            // intentionally used only in import and include - like blocks, they are
            // both leaf. If non-leaf folds should update/resize, checks must be put
            // that they still fit into the parent's hierarchy and if not, all parent's
            // deep-children have to be re-inserted (folds might cross after update)
            return next.getStart() > i.getEnd();

        }
        
        private Fold markRemoveFold(Fold f) {
            toRemove.add(f);
            removedFolds.add(f);
            f = foldIt.hasNext() ? foldIt.next() : null;
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.finest("Advanced fold, next = " + f);
            }
            return f;
        }
        
        /**
         * Validates the FoldInfo. Because of issue #237964, #237576 and similar, it is better 
         * to check that the FoldInfo does not contain invalid data centrally. The original idea was
         * to inform the fold manager early, but the issue seems hardly fixable at source
         * @param fi fold info to check
         * @return true, if the info is valid and should proceed to fold creation / update
         */
        private boolean isValidFold(FoldInfo fi) {
            // disallow zero-length folds and reversed folds.
            if (fi.getStart() >= fi.getEnd()) {
                return false;
            }
            int glen = fi.getTemplate().getGuardedEnd() + fi.getTemplate().getGuardedStart();
            // disallow folds, whiose length is less than the length of start/end guarded areas.
            return (fi.getStart() + glen <= fi.getEnd());
        }
        
        private boolean isValidFold(Fold f) {
            if (f.getParent() != null) {
                return true;
            }
            return execution.isBlocked(f);
        }
        
        public void run() throws BadLocationException {
            // first order the supplied folds:
            List ll = new ArrayList<FoldInfo>(foldInfos);
            ll.sort(this);
            
            foldIt = foldIterator();
            infoIt = ll.iterator();
            
            Document d = getDocument();
            if (d != null) {
                // diagnostics, not throwing exceptions since it could disturb
                // editing
                if (!DocumentUtilities.isReadLocked(d)) {
                    LOG.log(Level.WARNING, "Underlying document not read/write locked", 
                            Exceptions.attachSeverity(new Throwable(), Level.FINE));
                }
                if (!execution.isLockedByCaller()) {
                    LOG.log(Level.WARNING, "Fold hierarchy is not locked on transaction open", 
                            Exceptions.attachSeverity(new Throwable(), Level.FINE));
                }
            }
            tran = openTransaction();
            int len = d.getLength();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Updating fold hierarchy, doclen = {1}, foldInfos = " + ll, len );
                Collection c = new ArrayList<Fold>();
                while (foldIt.hasNext()) {
                    c.add(foldIt.next());
                }
                LOG.log(Level.FINE, "Current ordered folds: " + c);
                LOG.log(Level.FINE, "Current hierarchy: " + getOperation().getHierarchy());
                foldIt = foldIterator();
            }
            
            Fold f = foldIt.hasNext() ? foldIt.next() : null;
            FoldInfo i = ni();

            try {
                while (f != null || i != null) {
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.finest("Fold = " + f + ", FoldInfo = " + i);
                    }
                    int action = compare(i, f);
                    boolean nextSameRange = nextSameRange(i, f);
                    if (f == null || (action < 0 && !nextSameRange)) {
                        // create a new fold from the FoldInfo
                        toAdd.add(i);
                        i = ni();
                        if (LOG.isLoggable(Level.FINEST)) {
                            LOG.finest("Advanced info, next = " + i);
                        }
                        continue;
                    } else if (i == null || (action > 0 && !nextSameRange)) {
                        f = markRemoveFold(f);
                        continue;
                    }
                    
                    // ignore folds that have not been changed; the information what folds were actually updated will
                    // be useful later.
                    if (isChanged(f, i)) {
                        foldsToUpdate.put(f, i);
                    }
                    // all surviving folds should be in the output set
                    currentFolds.put(i, f);
                    i = ni();
                    f = foldIt.hasNext() ? foldIt.next() : null;
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.finest("Advanced both info & fold");
                    }
                }
                // remove folds in reverse order. If a fold ceases to exist with all its children, the children are
                // removed first (if they should be removed) instead of propagating up to the hierarchy.
                // other folds currently in the hierarchy should have their positions updated by document, so even
                for (int ri = toRemove.size() - 1; ri >= 0; ri--) {
                    Fold fold = toRemove.get(ri);
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.finest("Removing: " + f);
                    }
                    if (fold.getParent() != null) {
                        removeFromHierarchy(fold, tran);
                    }
                }
                for (Map.Entry<Fold, FoldInfo> updateEntry : foldsToUpdate.entrySet()) {
                    FoldInfo fi = updateEntry.getValue();
                    Fold ff = updateEntry.getKey();
                    if (!checkFoldInPlace(ff, fi)) {
                        LOG.finest("Updated fold does not fit in hierarchy, scheduling reinsertion: " + ff + ", info: " + fi);
                        // if the fold does not fit the location, it will be removed with the entire subtree. Otherwise it would
                        // compromise hierarchy constraints after update(x,y) and subsequent add() would not find the appropriate
                        // place for insertion of new folds.
                        tran.reinsertFoldTree(ff);
                    }
                    // update the data, if the fold is still valid
                    update(ff, fi);
                }
                for (FoldInfo info : toAdd) {
                    try {
                        int s = info.getStart();
                        int e = info.getEnd();
                        if (s > len || e > (len + 1) || s  >= e) {
                            // invalid fold info; possibly document has changed from the time FoldInfo was created.
                            continue;
                        }
                        currentFolds.put(info, getOperation().addToHierarchy(
                                info.getType(), 
                                info.getStart(), info.getEnd(),
                                info.getCollapsed(), 
                                info.getTemplate(),
                                info.getDescriptionOverride(),
                                info.getExtraInfo(),
                                tran.getTransaction()));
                        if (LOG.isLoggable(Level.FINEST)) {
                            LOG.finest("Adding: " + i);
                        }
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                if (LOG.isLoggable(Level.FINE)) {
                    execution.checkConsistency();
                }
            } finally {
                tran.commit();
                execution.incModCount();
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Updated fold hierarchy: " + getOperation().getHierarchy());
                }
            }
        }
        
        /**
         * Determines if FoldInfo changes the fold in some way. PENDING - extract to some functional interface,
         * as if FoldInfo is expanded in the future, the comparison should be as well
         */
        private boolean isChanged(Fold f, FoldInfo info) {
            int fs = f.getStartOffset();
            int fe = f.getEndOffset();
            boolean c = f.isCollapsed();
            String fd = f.getDescription();
            
            if (fs != info.getStart() || fe != info.getEnd()) {
                return true;
            }
            if (info.getCollapsed() != null && c != info.getCollapsed()) {
                return true;
            }
            return !fd.equals(getInfoDescription(info));
        }
        
        private String getInfoDescription(FoldInfo info) {
            String desc = info.getDescriptionOverride();
            if (desc == null) {
                desc = info.getTemplate().getDescription();
            }
            return desc;
        }
        
        private int getUpdatedFoldStart(Fold f) {
            FoldInfo update = foldsToUpdate.get(f);
            if (update == null) {
                return f.getStartOffset();
            } else {
                return update.getStart();
            }
        }
        
        private int getUpdatedFoldEnd(Fold f) {
            FoldInfo update = foldsToUpdate.get(f);
            if (update == null) {
                return f.getEndOffset();
            } else {
                return update.getEnd();
            }
        }
        
        /**
         * Checks if the fold will fit in its place even after updates are done.
         * 
         * @param f the existing fold
         * @param info the future state of the fold
         * @return true, if the fold may remain as it is, or false if it needs to be reinserted. 
         */
        private boolean checkFoldInPlace(Fold f, FoldInfo info) {
            if (getHierarchy().getRootFold() == f) {
                // root fold is always OK
                return true;
            }
            Fold parent = f.getParent();
            
            // check if the fold's boundary does not cross its parent
            if (parent == null) {
                // return false, if the fold is blocked - will be also reinserted, since it has been updated somehow.
                return !execution.isBlocked(f);
            }
            int s = getUpdatedFoldStart(parent);
            int e = getUpdatedFoldEnd(parent);
            
            int is = info.getStart(), ie = info.getEnd();
            
            if (is < s || ie > e) {
                return false;
            }
            
            // check if the fold does not cross its siblings
            int index = parent.getFoldIndex(f);
            if (index > 0) {
                Fold prev = parent.getFold(index - 1);
                e = getUpdatedFoldEnd(prev);
                if (is < e) {
                    return false;
                }
            }
            if (index < parent.getFoldCount() - 1) {
                Fold next = parent.getFold(index + 1);
                s = getUpdatedFoldStart(next);
                if (ie > s) {
                    return false;
                }
            }
            
            // last: if the fold has some children && the start/end crosses the 1st child or last child
            int cc = f.getFoldCount();
            if (cc > 0) {
                Fold c1 = f.getFold(0);
                s = getUpdatedFoldStart(c1);
                if (is > s) {
                    return false;
                }
                Fold c2 = cc > 1 ? f.getFold(cc - 1) : c1;
                e = getUpdatedFoldEnd(c2);
                if (ie < e) {
                    return false;
                }
            }
            
            return true;
        }
        
        public Fold update(Fold f, FoldInfo info) throws BadLocationException {
            this.fsch = null;
            int soffs = f.getStartOffset();
            int origStart = soffs;
            ApiPackageAccessor acc = getAccessor();
            int len = getDocument().getLength();
            if (info.getStart() > len || info.getEnd() > (len + 1)) {
                // no update done, new values are not valid
                return f;
            }
            if (info.getStart() >= info.getEnd()) {
                // the fold would become invalid after update. Should never happen as FoldInfo asserts
                // on creation
                LOG.warning("FoldInfo: " + info + ", invalid start and end offsets");
                return f;
            }
            Fold p = f.getParent();
            int infoStart = info.getStart();
            int infoEnd = info.getEnd();
            if (p != null) {
                int parentStart = p.getStartOffset();
                int parentEnd = p.getEndOffset();

                if (infoStart < parentStart) {
                    execution.markDamaged();
                    LOG.warning("Updated start < parent, dumping fold hierarchy: " + execution);
                    LOG.warning("FoldInfo: " + info + ", fold: " + f);
                    
                    infoStart = parentStart;
                }
                if (infoEnd > parentEnd) {
                    execution.markDamaged();
                    LOG.warning("Updated end > parent, dumping fold hierarchy: " + execution);
                    LOG.warning("FoldInfo: " + info + ", fold: " + f);
                    
                    infoEnd = parentEnd;
                }
            }
            
            if (infoStart != soffs) {
                acc.foldSetStartOffset(f, getDocument(), infoStart);
                FoldStateChange state = getFSCH(f);
                if (state.getOriginalEndOffset() >= 0 && state.getOriginalEndOffset() < soffs) {
                    execution.markDamaged();
                    LOG.warning("Original start offset > end offset, dumping fold hierarchy: " + execution);
                    LOG.warning("FoldInfo: " + info + ", fold: " + f);
                }
                acc.foldStateChangeStartOffsetChanged(state, soffs);
                soffs = info.getStart();
            }
            int eoffs = f.getEndOffset();
            int origEnd = eoffs;
            if (infoEnd != eoffs) {
                FoldStateChange state = getFSCH(f);
                if (state.getOriginalStartOffset()>= 0 && state.getOriginalStartOffset() > eoffs) {
                    execution.markDamaged();
                    LOG.warning("Original end offset < start offset, dumping fold hierarchy: " + execution);
                    LOG.warning("FoldInfo: " + info + ", fold: " + f);
                }
                acc.foldSetEndOffset(f, getDocument(), infoEnd);
                acc.foldStateChangeEndOffsetChanged(state, eoffs);
                eoffs = info.getEnd();
            }
            if (soffs > eoffs) {
                execution.markDamaged();
                LOG.warning("Updated end offset < start offset, dumping fold hierarchy: " + execution);
                LOG.warning("FoldInfo: " + info + ", fold: " + f);
            }
            String desc = getInfoDescription(info);
            // sanity check
            if (p != null) {
                int index = p.getFoldIndex(f);
                if (index != -1) {
                    if (index > 0) {
                        Fold prev = p.getFold(index - 1);
                        if (prev.getEndOffset() > f.getStartOffset()) {
                            execution.markDamaged();
                            LOG.warning("Wrong fold nesting after update, hierarchy: " + execution);
                            LOG.warning("FoldInfo: " + info + ", fold: " + f + " origStart-End" + origStart + "-" + origEnd);
                        }
                    }
                    if (index < p.getFoldCount() - 1) {
                        Fold next = p.getFold(index + 1);
                        if (next.getStartOffset() < f.getEndOffset()) {
                            execution.markDamaged();
                            LOG.warning("Wrong fold nesting after update, hierarchy: " + execution);
                            LOG.warning("FoldInfo: " + info + ", fold: " + f + " origStart-End" + origStart + "-" + origEnd);
                        }
                    }
                }
            }
            /*
            Object ei = info.getExtraInfo();
            if (ei != null && !ei.equals(acc.foldGetExtraInfo(f))) {
                acc.foldSetExtraInfo(f, ei);
                // this is a fake, but causes fold visualization to be refreshed
                acc.foldStateChangeDescriptionChanged(getFSCH(f));
            }
            */
            if (!f.getDescription().equals(desc)) {
                acc.foldSetDescription(f, desc);
                acc.foldStateChangeDescriptionChanged(getFSCH(f));
            }
            if (info.getCollapsed() != null && f.isCollapsed() != info.getCollapsed()) {
                acc.foldSetCollapsed(f, info.getCollapsed());
                acc.foldStateChangeCollapsedChanged(getFSCH(f));
            }
            return f;
        }

        /**
         * FoldStateChange for the current fold being updated;
         * just an optimization.
         */
        private FoldStateChange fsch;

        private FoldStateChange getFSCH(Fold f) {
            if (fsch != null) {
                return fsch;
            }
            return fsch = tran.getFoldStateChange(f);
        }
        
    }
    
    public String toString() {
        return "FoldOp[mgr = " + manager + ", rel = " + released + "]"; // NOI18N
    }
}
