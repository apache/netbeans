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

package org.netbeans.spi.editor.fold;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldTemplate;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.modules.editor.fold.ApiPackageAccessor;
import org.netbeans.modules.editor.fold.FoldHierarchyTransactionImpl;
import org.netbeans.modules.editor.fold.FoldOperationImpl;
import org.netbeans.modules.editor.fold.HierarchyErrorException;
import org.netbeans.modules.editor.fold.SpiPackageAccessor;
import org.openide.util.Parameters;


/**
 * Fold operation represents services
 * provided to an individual fold manager.
 * <br>
 * Each manager has its own dedicated instance
 * of fold operation.
 *
 * <p>
 * There are three main services - creation of a new fold
 * and adding or removing it from the hierarchy.
 * <br>
 * Adding and removing of the folds requires a valid transaction
 * that can be obtained by {@link #openTransaction()}.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class FoldOperation {
    
    private static boolean spiPackageAccessorRegistered;

    static {
        ensureSpiAccessorRegistered();
    }
    
    private static void ensureSpiAccessorRegistered() {
        if (!spiPackageAccessorRegistered) {
            spiPackageAccessorRegistered = true;
            SpiPackageAccessor.register(new SpiPackageAccessorImpl());
        }
    }
    
    private final FoldOperationImpl impl;
    
    private FoldOperation(FoldOperationImpl impl) {
        this.impl = impl;
    }
 
    
    /**
     * Create new fold instance and add it to the hierarchy.
     * <br>
     * The fold will either become part of the hierarchy directly or it will
     * become blocked by another fold already present in the hierarchy.
     * <br>
     * Once the blocking fold gets removed this fold will be phyiscally added
     * to the hierarchy automatically.
     *
     * <p>
     * The fold is logically bound to the fold manager that uses this fold operation.
     * <br>
     * The fold can only be removed by this fold operation.
     *
     * @param type type of the fold to be assigned to the fold.
     * @param description textual description of the fold that will be displayed
     *  once the fold becomes collapsed.
     * @param collapsed whether the fold should initially be collapsed or expanded.
     * @param startOffset starting offset of the fold. The fold creates swing position
     *  for the offset.
     * @param endOffset ending offset of the fold. The fold creates swing position
     *  for the offset.
     * @param startGuardedLength &gt;=0 initial guarded area of the fold (starting at the start offset).
     *  If the guarded area is modified the fold will be removed automatically.
     * @param endGuardedLength &gt;=0 ending guarded area of the fold (ending at the end offset).
     *  If the guarded area is modified the fold will be removed automatically.
     * @param extraInfo arbitrary extra information specific for the fold being created.
     *  It's not touched or used by the folding infrastructure in any way.
     *  <code>null<code> can be passed if there is no extra information.
     *  <br>
     *  The extra info of the existing fold can be obtained by
     *  {@link #getExtraInfo(org.netbeans.api.editor.fold.Fold)}.
     *
     * @return new fold instance that was added to the hierarchy.
     * @deprecated please use {@link #addToHierarchy(org.netbeans.api.editor.fold.FoldType, int, int, java.lang.Boolean, org.netbeans.api.editor.fold.FoldTemplate, java.lang.String, java.lang.Object, org.netbeans.spi.editor.fold.FoldHierarchyTransaction)}.
     * This form of call does not support automatic state assignment and fold templates.
     */
    @Deprecated
    public Fold addToHierarchy(FoldType type, String description, boolean collapsed,
    int startOffset, int endOffset, int startGuardedLength, int endGuardedLength,
    Object extraInfo, FoldHierarchyTransaction transaction)
    throws BadLocationException {
        Fold fold = impl.createFold(type, description, collapsed,
            startOffset, endOffset, startGuardedLength, endGuardedLength,
            extraInfo
        );
        impl.addToHierarchy(fold, transaction.getImpl());
        return fold;
    }
    
    /**
     * Adds a fold to the hierarchy.
     * The description and the guarded start/end is taken from the 'template' FoldTemplate. As the fold template display
     * is the most common override, the override string can be passed in 'displayOverride' (and will be used instead
     * of template and instead of type's template).
     * <p/>
     * The collapsed state can be prescribed, but can {@code null} can be passed to indicate the infrastructure should
     * assign collapsed state based on e.g. user preferences. The exact assignment algorithm is left unspecified. Callers
     * are recommended not to assign collapsed/expanded state explicitly.
     * <p/>
     * Usually, it's OK to pass null for collapsed, template and possibly extraInfo.
     * <p/>
     * Events produced by this add- call will be fired when the 'transaction' is committed. However fold hierarch will
     * be changed immediately.
     * 
     * @param type type of the fold, cannot be {@code null}
     * @param startOffset starting offset
     * @param endOffset end offset
     * @param collapsed the initial collapsed state; if {@code null}, the state will be assigned automatically.
     * @param template the FoldTemplate to use instead of default template of the type. {@code null}, if the type's template should be used.
     * @param extraInfo arbitrary extra information specific for the fold being created.
     *  It's not touched or used by the folding infrastructure in any way.
     *  <code>null<code> can be passed if there is no extra information.
     *  <br>
     *  The extra info of the existing fold can be obtained by
     *  {@link #getExtraInfo(org.netbeans.api.editor.fold.Fold)}.
     * @param transaction the transaction that manages events, cannot be null.
     * @return the created Fold instance
     * @throws BadLocationException 
     * @since 1.35
     */
    public Fold addToHierarchy(
            FoldType type, 
            int startOffset, int endOffset,
            Boolean collapsed,
            FoldTemplate template, String displayOverride, 
            Object extraInfo, FoldHierarchyTransaction transaction) 
            throws BadLocationException {
        Parameters.notNull("type", type);
        Parameters.notNull("transaction", transaction);

        boolean c;
        if (collapsed == null) {
            c = impl.getInitialState(type);
        } else {
            c = collapsed;
        }
        if (template == null) {
            template = type.getTemplate();
        }
        if (displayOverride == null) {
            displayOverride = template.getDescription();
        }
        Fold fold = impl.createFold(type, displayOverride, 
                c, startOffset, endOffset, template.getGuardedStart(),
                template.getGuardedEnd(), extraInfo);
        impl.addToHierarchy(fold, transaction.getImpl());
        return fold;
    }
    
    /**
     * This static method can be used to check whether the bounds
     * of the fold that is planned to be added are valid.
     * <br>
     * The conditions are:<pre>
     *  startOffset &lt; endOffset
     * </pre>
     *
     * <pre>
     *  startGuardedLength &gt;= 0
     * </pre>
     *
     * <pre>
     *  endGuardedLength &gt;= 0
     * </pre>
     *
     * <pre>
     *  startOffset + startGuardedLength &lt;= endOffset - endGuardedLength
     * </pre>
     *
     * @return true if the bounds are OK or false otherwise.
     */
    public static boolean isBoundsValid(int startOffset, int endOffset,
    int startGuardedLength, int endGuardedLength) {
        return (startOffset < endOffset)
            && (startGuardedLength >= 0)
            && (endGuardedLength >= 0)
            && ((startOffset + startGuardedLength) <= (endOffset -endGuardedLength));
    }
    
    /**
     * Remove the fold that is either present in the hierarchy or blocked
     * by another fold.
     *
     * @param fold fold to be removed
     * @param transaction non-null transaction under which the fold should be removed.
     */
    public void removeFromHierarchy(Fold fold, FoldHierarchyTransaction transaction) {
        impl.removeFromHierarchy(fold, transaction.getImpl());
    }
    
    /**
     * Check whether this fold operation has produced the given fold.
     * 
     * @param fold non-null fold.
     * @return true if this fold operation produced the given fold (by its <code>addToHierarchy()</code> method)
     *   or false otherwise.
     */
    public boolean owns(Fold fold) {
        return (ApiPackageAccessor.get().foldGetOperation(fold) == impl);
    }
    
    /**
     * Return extra info object passed to fold at time of its creation.
     *
     * @return extra information object specific for the fold
     *  or null if there was no extra info.
     */
    public Object getExtraInfo(Fold fold) {
        return impl.getExtraInfo(fold);
    }
    
    /**
     * Check whether the starting guarded area of the fold
     * is damaged by a document modification.
     *
     * @param fold fold to check. The fold must be managed by this fold operation.
     * @return true if the starting area of the fold was damaged by the modification
     *  or false otherwise.
     */
    public boolean isStartDamaged(Fold fold) {
        return impl.isStartDamaged(fold);
    }

    /**
     * Check whether the ending guarded area of the fold
     * is damaged by a document modification.
     *
     * @param fold fold to check. The fold must be managed by this fold operation.
     * @return true if the ending area of the fold was damaged by the modification
     *  or false otherwise.
     */
    public boolean isEndDamaged(Fold fold) {
        return impl.isEndDamaged(fold);
    }

    /**
     * Open a new transaction over the fold hierarchy.
     * <br>
     * <b>Note:</b> Always use the following pattern:
     * <pre>
     *     FoldHierarchyTransaction transaction = operation.openTransaction();
     *     try {
     *         ...
     *     } finally {
     *         transaction.commit();
     *     }
     * </pre>
     *
     * @return opened transaction for further use.
     */
    public FoldHierarchyTransaction openTransaction() {
        return impl.openTransaction().getTransaction();
    }
    
    /**
     * Check whether the fold is currently present in the hierarchy or blocked.
     *
     * @return true if the fold is currently present in the hierarchy or blocked
     *  or false otherwise.
     */
    public boolean isAddedOrBlocked(Fold fold) {
        return impl.isAddedOrBlocked(fold);
    }
    
    /**
     * Is the given fold blocked by another fold?
     */
    public boolean isBlocked(Fold fold) {
        return impl.isBlocked(fold);
    }
    
    /**
     * Get the hierarchy for which this fold operations works.
     */
    public FoldHierarchy getHierarchy() {
        return impl.getHierarchy();
    }

    /**
     * Informs that the manager was released.
     * Use the method to check whether the {@link FoldManager} should be still operational.
     * Once released, the FoldManager (and its Operation) will not be used again by the infrastructure.
     * 
     * @return true, if release() was called on the manager
     * @since 1.35
     */
    public boolean isReleased() {
        return impl.isReleased();
    }
    
    /**
     * Enumerates all Folds defined by this Operation, in the document-range order.
     * Outer folds precede the inner ones. Folds, which overlap are enumerated strictly
     * in the order of their starting positions. 
     * <p/>
     * The method may be only called under {@link FoldHierarchy#lock}. The Iterator may
     * be only used until that lock is released. After releasing the lock, the Iterator
     * may fail.
     * 
     * @return readonly iterator for all folds defined through this FoldOperation
     * @since 1.35
     */
    public Iterator<Fold>  foldIterator() {
        return impl.foldIterator();
    }
    
    /**
     * Performs refresh of folding information. The method will:
     * <ul>
     * <li>remove Folds, which do not appear among the supplied FoldInfos
     * <li>add Folds, which do not exist, but are described by some FoldInfo
     * <li>attempt to update Folds, which match the FoldInfos
     * </ul>
     * For each of the supplied FoldInfos, there should be at most 1 Fold either created or found existing, and no
     * Folds without a corresponding input FoldInfo should remain in the hierarchy after the call. The mapping from the
     * input FoldInfo to the corresponding Fold (created or found existing) is returned.
     * <p/>
     * Note that Folds, which are blocked (e.g. by a higher-priority manager) will be added/removed/updated and
     * returned as well. In order to find whether a specific Fold is blocked, please call {@link #isBlocked}.
     * <p/>
     * If the {@code removed} or {@code created} parameters are not null, the removed Fold instances, or the FoldInfos
     * that created new Folds will be put into those collection as a supplemental return value. The caller may then
     * update its own data with respect to the changes and the current Fold set.
     * <p/>
     * <b>Note:</b> The method may be only called under {@link FoldHierarchy#lock}. This implies the document is also read-locked.
     * The caller <b>must check</b> whether a modification happen in between the FoldInfos were produced at the 
     * document + hierarchy lock. The method creates and commits its own {@link FoldTransaction} - they are not reentrant,
     * so do not call the method under a transaction.
     * 
     * @param infos current state of folds that should be updated to the hierarchy
     * @param removed Collection that will receive Folds, which have been removed from the hierarchy, or {@code null}, if the caller
     * does not want to receive the information
     * @param created Collection that will receive FoldInfos that created new Folds in the hierarchy, {@code null} means
     * the caller is not interested in the creation information.
     * 
     * @return the mapping from FoldInfos supplied as an input to current Folds. {@code null}, if the manager has been
     * released.
     * @since 1.35
     */
    public Map<FoldInfo, Fold> update(
            Collection<FoldInfo> infos, 
            Collection<Fold> removed, 
            Collection<FoldInfo> created) throws BadLocationException {
        Parameters.notNull("infos", infos);
        return impl.update(infos, removed, created);
    }
    
    private static final class SpiPackageAccessorImpl extends SpiPackageAccessor {

        @Override
        public FoldHierarchyTransaction createFoldHierarchyTransaction(
        FoldHierarchyTransactionImpl impl) {
            return new FoldHierarchyTransaction(impl);
        }
        
        @Override
        public FoldHierarchyTransactionImpl getImpl(FoldHierarchyTransaction transaction) {
            return transaction.getImpl();
        }
        
        @Override
        public FoldOperation createFoldOperation(FoldOperationImpl impl) {
            return new FoldOperation(impl);
        }
        
    }
}
