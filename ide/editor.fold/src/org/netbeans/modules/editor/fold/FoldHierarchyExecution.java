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

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.netbeans.api.editor.fold.FoldStateChange;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.netbeans.lib.editor.util.PriorityMutex;
import org.netbeans.spi.editor.fold.FoldHierarchyMonitor;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.WeakListeners;

/**
 * Class backing the <code>FoldHierarchy</code> in one-to-one relationship.
 * <br>
 * The <code>FoldHierarchy</code> delegates all its operations
 * to this object.
 *
 * <p>
 * All the changes performed in to the folds are always done
 * in terms of a transaction represented by {@link FoldHierarchyTransactionImpl}.
 * The transaction can be opened by {@link #openTransaction()}.
 *
 * <p>
 * This class changes its state upon displayability change
 * of the associated component by listening on "ancestor" component property.
 * <br>
 * If the component is not displayable then the list of root folds becomes empty
 * while if the component gets displayable the root folds are created
 * according to registered managers.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class FoldHierarchyExecution implements DocumentListener, Runnable {
    private static final Logger LOG = Logger.getLogger(FoldHierarchyExecution.class.getName());
    // logging to catch issue #231362
    private static final Logger PREF_LOG = Logger.getLogger(FoldHierarchy.class.getName() + ".enabled");
    
    /**
     * Runs rebuild(). Although it theoretically could work in parallel for several views, the original code
     * was written to run in EQ and many managers depend on parsing API, which also runs in 1 thread.
     */
    private static final RequestProcessor RP = new RequestProcessor("Folding initializer");
    
    private static final String PROPERTY_FOLD_HIERARCHY_MUTEX = "foldHierarchyMutex"; //NOI18N

    private static final String PROPERTY_FOLDING_ENABLED = FoldUtilitiesImpl.PREF_CODE_FOLDING_ENABLED; //NOI18N
    
    private static final boolean DEFAULT_CODE_FOLDING_ENABLED = true;

    private static final boolean debug
        = Boolean.getBoolean("netbeans.debug.editor.fold"); //NOI18N
    
    private static final boolean debugFire
        = Boolean.getBoolean("netbeans.debug.editor.fold.fire"); //NOI18N
    
    private static final FoldOperationImpl[] EMPTY_FOLD_OPERTAION_IMPL_ARRAY
        = new FoldOperationImpl[0];
    
    static {
        // The following call will make sure that the SpiPackageAccessor gets initialized
        FoldOperation.isBoundsValid(0, 0, 0, 0);
    }
    
    private final JTextComponent component;
    
    private FoldHierarchy hierarchy;
    
    private Fold rootFold;
    
    private FoldOperationImpl[] operations = EMPTY_FOLD_OPERTAION_IMPL_ARRAY;
    
    /**
     * Map containing [blocked-fold, blocking-fold] pairs.
     */
    private Map blocked2block = new HashMap(4);
    
    /**
     * Map containing [blocking-fold, blocked-fold-set] pairs.
     */
    private Map block2blockedSet = new HashMap(4);
    
    /** True, if the hierarchy has been damaged, will log more information during updates */
    private int damaged;
    
    /**
     * Content when the last transaction was committed.
     */
    private String committedContent;
    
    private AbstractDocument lastDocument;
    
    /**
     * This is different from lastDocument, which can be cleared after rebuild(false).
     * This reference exactly tracks the parent document of the root fold.
     */
    private Reference<Document> lastRootDocument;
    
    private PriorityMutex mutex;
    
    private final EventListenerList listenerList;
    
    private boolean foldingEnabled;
    
    private FoldHierarchyTransactionImpl activeTransaction;
    
    private PropertyChangeListener componentChangesListener;
    
    private RequestProcessor.Task initTask;
    
    private volatile boolean active;
    private volatile Preferences foldPreferences;
    private PreferenceChangeListener prefL;
    
    private DocumentListener updateListener = new DL();
    
    private static final AtomicInteger TASK_WATCH = new AtomicInteger(0);
    
    private int modCount;
    
    public static synchronized FoldHierarchy getOrCreateFoldHierarchy(JTextComponent component) {
        return getOrCreateFoldExecution(component).getHierarchy();
    }
    
    void incModCount() {
        // just for debugging
        if (++modCount % 5 == 0) {
//            throw new HierarchyErrorException(null, null, -1, false, "debug");
        }
    }
    
    private static synchronized FoldHierarchyExecution getOrCreateFoldExecution(JTextComponent component) {
        if (component == null) {
            throw new NullPointerException("component cannot be null"); // NOI18N
        }

        FoldHierarchyExecution execution
            = (FoldHierarchyExecution)component.getClientProperty(FoldHierarchyExecution.class);
        
        if (execution == null) {
            execution = new FoldHierarchyExecution(component);
            execution.init();

            component.putClientProperty(FoldHierarchyExecution.class, execution);

            String mime = DocumentUtilities.getMimeType(component);
            Collection<? extends FoldHierarchyMonitor> monitors = MimeLookup.getLookup(mime).lookupAll(FoldHierarchyMonitor.class);
            for (FoldHierarchyMonitor m : monitors) {
                try {
                    m.foldsAttached(execution.getHierarchy());
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return execution;
    }
    
    /**
     * Construct new fold hierarchy SPI
     *
     * @param hierarchy hierarchy for which this SPI gets created.
     * @param component comoponent for which this all happens.
     */
    private FoldHierarchyExecution(JTextComponent component) {
        this.component = component;
        this.listenerList = new EventListenerList();
    }
    
    public boolean hasProviders() {
        return active;
    }
    
    /**
     * Initialize this spi by existing hierarchy instance
     * (the one for which this spi was created).
     * <br>
     * This is called lazily upon first attempt to lock
     * the hierarchy.
     */
    private void init() {
        // Assign mutex
        mutex = (PriorityMutex)component.getClientProperty(PROPERTY_FOLD_HIERARCHY_MUTEX);
        if (mutex == null) {
            mutex = new PriorityMutex();
            component.putClientProperty(PROPERTY_FOLD_HIERARCHY_MUTEX, mutex);
        }

        this.hierarchy = ApiPackageAccessor.get().createFoldHierarchy(this);
        
        updateRootFold(component.getDocument());
        
        foldingEnabled = getFoldingEnabledSetting();

        // Start listening on component changes
        startComponentChangesListening();
        
        // initialize conservatively the active flag
        active = !FoldManagerFactoryProvider.getDefault().getFactoryList(hierarchy).isEmpty();

        this.initTask = RP.create(this);
        scheduleInit(500);
    }
    
    private void updateRootFold(Document doc) {
        if (lastRootDocument != null) {
            Document d = lastRootDocument.get();
            if (d == doc) {
                return;
            }
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Updating root fold. lastDocument = {0}, newDocument = {1}", new Object[] { lastRootDocument, doc });
        }
        try {
            rootFold = ApiPackageAccessor.get().createFold(
                new FoldOperationImpl(this, null, Integer.MAX_VALUE),
                FoldHierarchy.ROOT_FOLD_TYPE,
                "root", // NOI18N
                false,
                doc,
                0, doc.getEndPosition().getOffset(),
                0, 0,
                null
            );
            lastRootDocument = new WeakReference(doc);
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    /* testing only */
    public static void waitHierarchyInitialized(JTextComponent panel) {
        getOrCreateFoldExecution(panel).getInitTask().waitFinished();
    }
    
    /* testing only */
    static boolean waitAllTasks() throws InterruptedException {
        synchronized (TASK_WATCH) {
            while (TASK_WATCH.get() > 0) {
                TASK_WATCH.wait(30000);
            }
        }
        return true;
    }
    
    private Task getInitTask() {
        return initTask;
    }
    
    @Override
    public void run() {
        rebuild(false);
        notifyTaskFinished();
    }
    
    /**
     * Get the fold hierarchy associated with this SPI
     * in one-to-one relationship.
     */
    public final FoldHierarchy getHierarchy() {
        return hierarchy;
    }
    
    /**
     * Lock the hierarchy for exclusive use. This method must only
     * be used together with {@link #unlock()} in <code>try..finally</code> block.
     * <br>
     * Prior using this method the document must be locked.
     * The document lock can be either readlock
     * e.g. by using {@link javax.swing.text.Document#render(Runnable)}
     * or writelock
     * e.g. when in {@link javax.swing.event.DocumentListener})
     * and must be obtained on component's document
     * i.e. {@link javax.swing.text.JTextComponent#getDocument()}
     * should be used.
     *
     * <p>
     * The <code>FoldHierarchy</code> delegates to this method.
     *
     * <p>
     * <font color="red">
     * <b>Note:</b> The clients using this method must ensure that
     * they <b>always</b> use this method in the following pattern:<pre>
     *
     *     lock();
     *     try {
     *         ...
     *     } finally {
     *         unlock();
     *     }
     * </pre>
     * </font>
     */
    public final void lock() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Locked FoldHierarchy for " + System.identityHashCode(getComponent())); // NOI18N
        }
        mutex.lock();
    }
    
    public final boolean isLockedByCaller() {
        return mutex.getLockThread() == Thread.currentThread();
    }
    
    /**
     * Unlock the hierarchy from exclusive use. This method must only
     * be used together with {@link #lock()} in <code>try..finally</code> block.
     */
    public void unlock() {
        unlock(activeTransaction);
    }
    
    void unlock(FoldHierarchyTransactionImpl tran) {
        if (activeTransaction != null) {
            activeTransaction.cancelled();
        }
        if (activeTransaction != tran) {
            activeTransaction = tran;
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Unlocked FoldHierarchy for " + System.identityHashCode(getComponent())); // NOI18N
        }
        mutex.unlock();
    }
    
    /**
     * Get the text component for which this fold hierarchy was created.
     * <br>
     * The <code>FoldHierarchy</code> delegates to this method.
     *
     * @return non-null text component for which this fold hierarchy was created.
     */
    public JTextComponent getComponent() {
        return component;
    }

    /**
     * Get the root fold of this hierarchy.
     * <br>
     * The <code>FoldHierarchy</code> delegates to this method.
     *
     * @return root fold of this hierarchy.
     *   The root fold covers the whole document and is uncollapsable.
     */
    public Fold getRootFold() {
        return rootFold;
    }
    
    /**
     * Add listener for changes done in the hierarchy.
     * <br>
     * The <code>FoldHierarchy</code> delegates to this method.
     *
     * @param l non-null listener to be added.
     */
    public void addFoldHierarchyListener(FoldHierarchyListener l) {
        synchronized (listenerList) {
            listenerList.add(FoldHierarchyListener.class, l);
        }
    }
    
    /**
     * Remove previously added listener for changes done in the hierarchy.
     * <br>
     * The <code>FoldHierarchy</code> delegates to this method.
     *
     * @param l non-null listener to be removed.
     */
    public void removeFoldHierarchyListener(FoldHierarchyListener l) {
        synchronized (listenerList) {
            listenerList.remove(FoldHierarchyListener.class, l);
        }
    }
    
    void fireFoldHierarchyListener(FoldHierarchyEvent evt) {
        if (debugFire) {
            /*DEBUG*/System.err.println("Firing FoldHierarchyEvent:\n" + evt); // NOI18N
        }

        Object[] listeners = listenerList.getListenerList(); // no need to sync
        // fire events to the listeners in the same order as they were registered (#70915)
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == FoldHierarchyListener.class) {
                ((FoldHierarchyListener)listeners[i + 1]).foldHierarchyChanged(evt);
            }
        }
        
    }

    /**
     * Attempt to add the given fold to the code folding hierarchy.
     * The fold will either become part of the hierarchy or it will
     * become blocked by another fold present in the hierarchy.
     * <br>
     * Only folds created by the fold operations of this hierarchy
     * can be added.
     *
     * @param fold fold to be added
     * @param transaction transaction under which the fold should be added.
     * @return true if the fold was added successfully or false
     *  if it became blocked.
     */
    public boolean add(Fold fold, FoldHierarchyTransactionImpl transaction) {
        if (fold.getParent() != null || isBlocked(fold)) {
            throw new IllegalStateException("Fold already added: " + fold); // NOI18N
        }
        
        boolean added = transaction.addFold(fold);
        
//        checkConsistency();
        
        return added;
    }
    
    /**
     * Remove the fold that is either present in the hierarchy or blocked
     * by another fold.
     *
     * @param fold fold to be removed
     * @param transaction non-null transaction under which the fold should be removed.
     */
    public void remove(Fold fold, FoldHierarchyTransactionImpl transaction) {
        transaction.removeFold(fold);
//        checkConsistency();
    }
    
    /**
     * Check whether the fold is currently present in the hierarchy or blocked.
     *
     * @return true if the fold is currently present in the hierarchy or blocked
     *  or false otherwise.
     */
    public boolean isAddedOrBlocked(Fold fold) {
        return (fold.getParent() != null || isBlocked(fold));
    }
    
    /**
     * Is the given fold blocked by another fold?
     */
    public boolean isBlocked(Fold fold) {
        return (getBlock(fold) != null);
    }
    
    /**
     * Get the fold blocking the given fold or null
     * if the fold is not blocked.
     */
    Fold getBlock(Fold fold) {
        return (blocked2block.size() > 0)
            ? (Fold)blocked2block.get(fold)
            : null;
    }
    
    Set<Fold> getBlockedFolds(Fold f) {
        return (Set<Fold>)block2blockedSet.get(f);
    }
    
    public boolean rebuilding = false;
    
    /**
     * Rebuilds the fold hierarchy. Does so by removing ALL folds and inserting them
     * back again. Folds may be reparented, reordered, ... all change events made during
     * the rebuild will be suppressed and will not reach the clients.
     */
    void rebuildHierarchy() {
        LOG.log(Level.WARNING, "Fold hierarchy damaged, rebuilding: " + this);
        FoldHierarchyTransactionImpl timpl = this.activeTransaction;
        lock();
        rebuilding = true;
        try {
            // temporary transaction which sinks all changes
            markClean();
            activeTransaction = new FoldHierarchyTransactionImpl(this, true);
            if (operations.length == 0) {
                return;
            }
            int fc = rootFold.getFoldCount();
            Fold[] folds = new Fold[fc];
            for (int i = 0; i < fc; i++) {
                folds[i] = rootFold.getFold(i);
            }
            for (Fold f : folds) {
                activeTransaction.reinsertFoldTree(f);
            }
            activeTransaction.commit();
        } finally {
            if (timpl != null) {
                timpl.resetCaches();
            }
            rebuilding = false;
            unlock(timpl);
        }
        LOG.log(Level.WARNING, "Fold hierarchy after rebuild: " + this);
    }
    
    /**
     * Mark given fold as blocked by the block fold.
     */
    void markBlocked(Fold blocked, Fold block) {
        blocked2block.put(blocked, block);

        Set blockedSet = (Set)block2blockedSet.get(block);
        if (blockedSet == null) {
            blockedSet = new HashSet();
            block2blockedSet.put(block, blockedSet);
        }
        if (!blockedSet.add(blocked)) { // already added
            throw new IllegalStateException("fold " + blocked + " already blocked"); // NOI18N
        }
    }
    
    /**
     * Remove blocked fold from mappings.
     *
     * @param blocked fold
     * @return fold that blocked the blocked fold.
     * @throws IllegalArgumentException if the given blocked fold was not really blocked.
     */
    Fold unmarkBlocked(Fold blocked) {
        // Find block for the given blocked fold
        Fold block = (Fold)blocked2block.remove(blocked);
        if (block == null) { // not blocked
            throw new IllegalArgumentException("Not blocked: " + blocked); // NOI18N
        }

        // Remove the fold from set of blocked folds of the block
        Set blockedSet = (Set)block2blockedSet.get(block);
        if (!blockedSet.remove(blocked)) {
            throw new IllegalStateException("Not blocker for " + blocked); // NOI18N
        }
        if (blockedSet.isEmpty()) { // Remove the blocker as well
            block2blockedSet.remove(block);
        }
        return block;
    }

    /**
     * Mark the given block fold to be no longer blocking
     * (and mark the folds blocked by the given block fold as not blocked).
     *
     * @param block the fold blocking others
     * @return set of folds blocked by the block or null if the given fold
     *  was not block.
     */
    Set unmarkBlock(Fold block) {
        Set blockedSet = (Set)block2blockedSet.remove(block);
        if (blockedSet != null) {
            // Remove all items of blocked set
            int size = blocked2block.size();
            blocked2block.keySet().removeAll(blockedSet);
            if (size - blocked2block.size() != blockedSet.size()) { // not all removed
                throw new IllegalStateException("Not all removed: " + blockedSet); // NOI18N
            }
        }
        return blockedSet;
    }

    /**
     * Collapse all folds in the given collection.
     * <br>
     * The <code>FoldHierarchy</code> delegates to this method.
     */
    public void collapse(Collection c) {
        setCollapsed(c, true);
    }
    
    /**
     * Expand all folds in the given collection.
     * <br>
     * The <code>FoldHierarchy</code> delegates to this method.
     */
    public void expand(Collection c) {
        setCollapsed(c, false);
    }
        
    private void setCollapsed(Collection c, boolean collapsed) {
        FoldHierarchyTransactionImpl transaction = openTransaction();
        try {
            for (Iterator it = c.iterator(); it.hasNext();) {
                Fold fold = (Fold)it.next();
                transaction.setCollapsed(fold, collapsed);
            }
        } finally {
            transaction.commit();
        }
        // update the active flag
        active = operations.length > 0;
    }
    
    /**
     * Open a new transaction on the fold hierarchy
     * to make a change in the hierarchy.
     * <br>
     * Transaction is active until commited
     * by calling <code>transaction.commit()</code>.
     * <br>
     * Only one transaction can be active at the time.
     * <br>
     * There is currently no way to rollback the transaction.
     *
     * <p>
     * <font color="red">
     * <b>Note:</b> The clients using this method must ensure that
     * they <b>always</b> use this method in the following pattern:<pre>
     *
     *     FoldHierarchyTransaction transaction = operation.openTransaction();
     *     try {
     *         ...
     *     } finally {
     *         transaction.commit();
     *     }
     * </pre>
     * </font>

     */
    public FoldHierarchyTransactionImpl openTransaction() {
        if (activeTransaction != null) {
            throw new IllegalStateException("Active transaction already exists."); // NOI18N
        }
        activeTransaction = new FoldHierarchyTransactionImpl(this);
        return activeTransaction;
    }
    
    void clearActiveTransaction() {
        if (activeTransaction == null) {
            throw new IllegalStateException("No transaction in progress"); // NOI18N
        }
        activeTransaction = null;
    }

    void createAndFireFoldHierarchyEvent(
    Fold[] removedFolds, Fold[] addedFolds,
    FoldStateChange[] foldStateChanges,
    int affectedStartOffset, int affectedEndOffset) {
        
        // Check correctness
        if (affectedStartOffset < 0) {
            throw new IllegalArgumentException("affectedStartOffset=" // NOI18N
                + affectedStartOffset + " < 0"); // NOI18N
        }
        
        if (affectedEndOffset < affectedStartOffset) {
            throw new IllegalArgumentException("affectedEndOffset=" // NOI18N
                + affectedEndOffset + " < affectedStartOffset=" + affectedStartOffset); // NOI18N
        }

        FoldHierarchyEvent evt = ApiPackageAccessor.get().createFoldHierarchyEvent(
            hierarchy,
            removedFolds, addedFolds, foldStateChanges,
            affectedStartOffset, affectedEndOffset
        );

        fireFoldHierarchyListener(evt);
    }
    
    private volatile boolean suspended = false;
    
    /**
     * Suspend reaction to document changes iff the component was removed off screen. 
     * The component will be typically never added again to the visual hierarchy; but if it 
     * will, rebuild() must be called to reinitialize all the folding.
     */
    private void postWatchDocumentChanges(final boolean stop) {
        if (suspended == stop) {
            return;
        }
        TASK_WATCH.incrementAndGet();
        RP.post(new Runnable() {
            public void run() {
                rebuild(stop);
                suspended = stop;
                notifyTaskFinished();
            }
        });
    }
    
    private static void notifyTaskFinished() {
        synchronized (TASK_WATCH) {
            TASK_WATCH.getAndDecrement();
            TASK_WATCH.notifyAll();
        }
    }
    /**
     * Rebuild the fold hierarchy - the fold managers will be recreated.
     */
    public void rebuild() {
        rebuild(false);
    }
    
    private DocumentListener wUpdateL;
    private DocumentListener wDocL;
    
    public void rebuild(boolean doRelease) {
        Document doc = getComponent().getDocument();
        AbstractDocument adoc;
        boolean releaseOnly; // only release the current hierarchy root folds

        // Stop listening on the original document
        if (lastDocument != null) {
            // Remove document listener with specific priority
            if (wUpdateL != null) {
                invokeUpdateListener(doc, wUpdateL, false);
            }
            if (wDocL != null) {
                DocumentUtilities.removeDocumentListener(lastDocument, wDocL, DocumentListenerPriority.FOLD_UPDATE);
            }
            lastDocument = null;
            wUpdateL = null;
            wDocL = null;
        }

        if (doc instanceof AbstractDocument) {
            adoc = (AbstractDocument)doc;
            releaseOnly = false;
        } else { // doc is null or non-AbstractDocument => release the hierarchy
            adoc = null;
            releaseOnly = true;
        }

        if (!foldingEnabled) { // folding not enabled => release
            releaseOnly = true;
        }

        releaseOnly |= doRelease;
        
        if (adoc != null) {
            adoc.readLock();
            
            updateRootFold(doc);

            // Start listening for changes
            if (!releaseOnly) {
                lastDocument = adoc;
                // Add document listener with specific priority
                DocumentUtilities.addDocumentListener(lastDocument, wDocL = WeakListeners.document(this, lastDocument), DocumentListenerPriority.FOLD_UPDATE);
                invokeUpdateListener(doc,  wUpdateL = WeakListeners.document(updateListener, doc), true);
                suspended = false;
            }
        }
        try {
            lock();
            try {
                rebuildManagers(releaseOnly);
            } finally {
                unlock();
            }
        } finally {
            if (adoc != null) {
                adoc.readUnlock();
            }
        }
    }
    
    private String getMimeType() {
        EditorKit ek = component.getUI().getEditorKit(component);
        String mimeType;

        if (ek != null) {
            mimeType = ek.getContentType();
        } else if (component.getDocument() != null) {
            mimeType = DocumentUtilities.getMimeType(component.getDocument());
        } else {
            mimeType = "";
        }
        return mimeType;
    }
        
    /**
     * Rebuild (or release) the root folds of the hierarchy in the event dispatch thread.
     *
     * @param releaseOnly release the current root folds
     *  but make the new root folds array empty.
     */
    private void rebuildManagers(boolean releaseOnly) {
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "rebuilding fold managers, release = {0}, document = {1}, component = {2}", new Object[] {
               releaseOnly, this.lastDocument, Integer.toHexString(System.identityHashCode(this.component))
            });
        }
        for (int i = 0; i < operations.length; i++) {
            operations[i].release();
        }
        operations = EMPTY_FOLD_OPERTAION_IMPL_ARRAY; // really release
        
        // Call all the providers
        List factoryList = releaseOnly ? Collections.emptyList() : 
                FoldManagerFactoryProvider.getDefault().getFactoryList(getHierarchy());
        int factoryListLength = factoryList.size();

        if (debug) {
            /*DEBUG*/System.err.println("FoldHierarchy rebuild():" // NOI18N
                + " FoldManager factory count=" + factoryListLength // NOI18N
            );
        }
        
        // Create fold managers
        int priority = factoryListLength - 1; // highest priority (till lowest == 0)
        boolean ok = false;
        try {
            operations = new FoldOperationImpl[factoryListLength];
            int i;
            for (i = 0; i < factoryListLength; i++) {
                FoldManagerFactory factory = (FoldManagerFactory)factoryList.get(i);
                FoldManager manager = factory.createFoldManager();
                if (manager == null) {
                    continue;
                }
                operations[i] = new FoldOperationImpl(this, manager, priority);
                priority--;
            }
            // trim the array in the unlikely case
            if (i < factoryListLength) {
                FoldOperationImpl[] ops = new FoldOperationImpl[i];
                System.arraycopy(operations, 0, ops, 0, i);
                operations = ops;
            }
            ok = true;
        } finally {
            this.damaged = 0;
            if (!ok) {
                operations = EMPTY_FOLD_OPERTAION_IMPL_ARRAY;
            }
        }

        // Init managers under a local transaction
        FoldHierarchyTransactionImpl transaction = openTransaction();
        ok = false;
        try {
            // Remove all original folds - pass array of all blocked folds
            Fold[] allBlocked = new Fold[blocked2block.size()];
            blocked2block.keySet().toArray(allBlocked);
            transaction.removeAllFolds(allBlocked);

            // Init folds in all fold managers
            // Go from the manager with highest priority (index 0)
            for (int i = 0; i < factoryListLength; i++) {
                operations[i].initFolds(transaction);
            }
            ok = true; // inited successfully
        } finally {
            if (!ok) {
                // TODO - remove folds under root fold
                operations = EMPTY_FOLD_OPERTAION_IMPL_ARRAY;
            }
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "Fold managers initialized. New managers = {0}, status = {1}", new Object[] {
                    Arrays.asList(operations), ok
                });
            }
            transaction.commit();
        }
        // update the active flag
        active = operations.length > 0;
    }
    
    private void scheduleInit(int delay) {
        if (!initTask.cancel()) {
            TASK_WATCH.incrementAndGet();
        }
        initTask.schedule(delay);
    }
    
    private class ComponentL implements PropertyChangeListener, HierarchyListener,
            Runnable {
        private boolean editorKitLive = true;

        private boolean updating;

        @Override
        public void hierarchyChanged(HierarchyEvent e) {
            if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) == 0) {
                return;
            }
            // the component may be reparented, usually in the same execution sequence
            // within an event. Let's update (suspend or resume) the changes depending on
            // the stabilized state in a next AWT event:
            if (updating) {
                return;
            }
            updating = true;
            SwingUtilities.invokeLater(this);
        }
        
        /**
         * See issue #233759; in fullscreen mode, the Component without focus
         * is removed from the visible hierarchy so the displayable returns false. 
         * It seems that during *real* close of CloneableEditor the editor kit gets
         * reset, but in a way not observable through getter (for null, a new default
         * EK is created by the getter). So we also observe property changes during
         * hierarchy processing and if ekit becomes null, it's the real editor close
         * 
         * TODO - possibly the check could be made using EditorRegistry
         */
        public void run() {
            updating = false;
            JTextComponent c = getComponent();
            boolean disableFolding = !c.isDisplayable() &&
                    !editorKitLive;
            postWatchDocumentChanges(disableFolding);
            editorKitLive = true;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if ("document".equals(propName)) { //NOI18N
                foldingEnabled = getFoldingEnabledSetting();
                scheduleInit(0);
            } else if (PROPERTY_FOLDING_ENABLED.equals(propName)) {
                foldingEnabledSettingChange();
            } else if ("editorKit".equals(propName)) { // NOI18N
                editorKitLive = evt.getNewValue() != null;
            }
        }
        
        
    }
    
    private void startComponentChangesListening() {
        if (componentChangesListener == null) {
            final ComponentL l = new ComponentL();
            // Start listening on component changes
            componentChangesListener = l;
            // Start listening on the component.
            // As the hierarchy instance is stored as a property of the component
            // (and in fact the spi and the reference to the listener as well)
            // the listener does not need to be removed
            getComponent().addPropertyChangeListener(componentChangesListener);
            
            // offload to Swing EDT, to prevent deadlock on treelock
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    // will suspend document listening iff the component is not shown
                    // fixes some bugs, and even improves performance :)
                    getComponent().addHierarchyListener(l);
                }
            });
        }
    }
    
    public void insertUpdate(DocumentEvent evt) {
        lock();
        try {
            FoldHierarchyTransactionImpl transaction = openTransaction();
            try {
                transaction.insertUpdate(evt);
                
                int operationsLength = operations.length;
                for (int i = 0; i < operationsLength; i++) {
                    operations[i].insertUpdate(evt, transaction);
                }
            } finally {
                transaction.commit();
            }
        } finally {
            unlock();
        }
    }
    
    public void removeUpdate(DocumentEvent evt) {
        lock();
        try {
            FoldHierarchyTransactionImpl transaction = openTransaction();
            try {
                transaction.removeUpdate(evt);

                int operationsLength = operations.length;
                for (int i = 0; i < operationsLength; i++) {
                    operations[i].removeUpdate(evt, transaction);
                }
            } finally {
                transaction.commit();
            }
        } finally {
            unlock();
        }
    }

    public void changedUpdate(DocumentEvent evt) {
        lock();
        try {
            FoldHierarchyTransactionImpl transaction = openTransaction();
            try {
                transaction.changedUpdate(evt);
                
                int operationsLength = operations.length;
                for (int i = 0; i < operationsLength; i++) {
                    operations[i].changedUpdate(evt, transaction);
                }
            } finally {
                transaction.commit();
            }
        } finally {
            unlock();
        }
    }
    
    private boolean getFoldingEnabledSetting() {
        return getFoldingEnabledSetting(true);
    }

    private boolean getFoldingEnabledSetting(boolean useProperty) {
        Boolean b = useProperty ? (Boolean)component.getClientProperty(SimpleValueNames.CODE_FOLDING_ENABLE) : null;
        // no preferences in component; get from lookup:
        if (b == null && component.getDocument() != null) {
            String mime = DocumentUtilities.getMimeType(component.getDocument());
            if (mime != null) {
                Preferences prefs = getFoldPreferences();
                b = prefs.getBoolean(PROPERTY_FOLDING_ENABLED, true);
            }
        }
        boolean ret = (b != null) ? b.booleanValue() : DEFAULT_CODE_FOLDING_ENABLED;
        PREF_LOG.log(Level.FINE, "Execution read enable: " + ret);
        component.putClientProperty(SimpleValueNames.CODE_FOLDING_ENABLE, ret);
        return ret;
    }
    
    public void foldingEnabledSettingChange() {
        boolean origFoldingEnabled = foldingEnabled;
        foldingEnabled = getFoldingEnabledSetting(false);
        if (origFoldingEnabled != foldingEnabled) {
            PREF_LOG.log(Level.FINE, "Execution scheduled fold update: " + foldingEnabled);
            scheduleInit(100);
        }
    }
    
    /**
     * Check the internal consistency of the hierarchy
     * and its contained folds. This is useful for testing purposes.
     *
     * @throws IllegalStateException in case an inconsistency is found.
     */
    public void checkConsistency() {
        try {
            checkFoldConsistency(getRootFold());
        } catch (RuntimeException e) {
            /*DEBUG*/System.err.println("FOLD HIERARCHY INCONSISTENCY FOUND\n" + this); // NOI18N
            throw e; // rethrow the exception
        }
    }
    
    private static void checkFoldConsistency(Fold fold) {
        int startOffset = fold.getStartOffset();
        int endOffset = fold.getEndOffset();
        int lastEndOffset = startOffset;
        
        for (int i = 0; i < fold.getFoldCount(); i++) {
            Fold child = fold.getFold(i);
            if (child.getParent() != fold) {
                throw new IllegalStateException("Wrong parent of child=" // NOI18N
                    + child + ": " + child.getParent() // NOI18N
                    + " != " + fold); // NOI18N
            }
            int foldIndex = fold.getFoldIndex(child);
            if (foldIndex != i) {
                throw new IllegalStateException("Fold index " + foldIndex // NOI18N
                    + " instead of " + i); // NOI18N
            }
            
            int childStartOffset = child.getStartOffset();
            int childEndOffset = child.getEndOffset();
            if (childStartOffset < lastEndOffset) {
                throw new IllegalStateException("childStartOffset=" + childStartOffset // NOI18N
                    + " < lastEndOffset=" + lastEndOffset); // NOI18N
            }
            // must also check zero-lengh folds, these are not permitted.
            if (childStartOffset >= childEndOffset) {
                throw new IllegalStateException("childStartOffset=" + childStartOffset // NOI18N
                    + " > childEndOffset=" + childEndOffset); // NOI18N
            }
            if (childStartOffset < startOffset || childEndOffset > endOffset) {
                throw new IllegalStateException("Invalid child offsets. Child = " + child + ", parent =" + fold);
            }
            lastEndOffset = childEndOffset;
            
            checkFoldConsistency(child);
        }
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("component="); // NOI18N
        sb.append(System.identityHashCode(getComponent()));
        sb.append('\n');

        // Append info about root folds
        sb.append(FoldUtilitiesImpl.foldToStringChildren(hierarchy.getRootFold(), 0));
        sb.append('\n');
        
        // Append info about blocked folds
        if (blocked2block != null) {
            sb.append("BLOCKED\n"); // NOI18N
            for (Iterator it = blocked2block.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry)it.next();
                sb.append("    "); // NOI18N
                sb.append(entry.getKey());
                sb.append(" blocked-by "); // NOI18N
                sb.append(entry.getValue());
                sb.append('\n');
            }
        }
        
        // Append info about blockers
        if (block2blockedSet != null) {
            sb.append("BLOCKERS\n"); // NOI18N
            for (Iterator it = block2blockedSet.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry)it.next();
                sb.append("    "); // NOI18N
                sb.append(entry.getKey());
                sb.append('\n');
                Set blockedSet = (Set)entry.getValue();
                for (Iterator it2 = blockedSet.iterator(); it2.hasNext();) {
                    sb.append("        blocks "); // NOI18N
                    sb.append(it2.next());
                    sb.append('\n');
                }
            }
        }
        
        int operationsLength = operations.length;
        if (operationsLength > 0) {
            sb.append("Fold Managers\n"); // NOI18N
            for (int i = 0; i < operationsLength; i++) {
                sb.append("FOLD MANAGER ["); // NOI18N
                sb.append(i);
                sb.append("]:\n"); // NOI18N
                sb.append(operations[i].getManager());
                sb.append("\n"); // NOI18N
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Cache for initial states for individual FoldTypes. 
     * The cache is invalidated iff the Preferences change in a key which starts with the collapse- prefix.
     * The cache is NOT invalidated on the FoldType set change, as if the foldtype set changes, either new FoldTypes
     * appear (they will enter the cache eventually), or the obsolete FoldTypes will not be used in the future,
     * so they may rote in the cache until the Component is closed.
     */
    private volatile Map<FoldType, Boolean>  initialFoldState = new HashMap<FoldType, Boolean>();
    
    /**
     * Returns the cached value for initial folding state of the specific type. The method may be only
     * called under a lock, since it populates the cache; no concurrency is permitted.
     * 
     * @param ft the FoldType to inspect
     * @return true, if the fold should be collapsed initially.
     */
    public boolean getInitialFoldState(FoldType ft) {
        if (!isLockedByCaller()) {
            throw new IllegalStateException("Must be called under FH lock");
        }
        Boolean b = initialFoldState.get(ft);
        if (b != null) {
            return b;
        }
        b = FoldUtilities.isAutoCollapsed(ft, hierarchy);
        initialFoldState.put(ft, b);
        return b;
    }
    
    /**
     * Obtains Preferences that control folding for this Hierarchy.
     * 
     * @return Preferences object
     */
    public Preferences getFoldPreferences() {
        if (foldPreferences == null) {
            synchronized (this) {
                if (foldPreferences != null) {
                    return foldPreferences;
                }
                String mimeType = getMimeType();
                // internally does MimeLookup lookup(Preferences.class)
                Preferences prefs = LegacySettingsSync.get().processMime(mimeType);
                if ("".equals(mimeType)) {
                    // do not cache; typically the editor kit will be changed to something other
                    return prefs;
                }
                foldPreferences = prefs;
                PreferenceChangeListener weakPrefL = WeakListeners.create(PreferenceChangeListener.class, 
                    prefL = new PreferenceChangeListener() {
                    @Override
                    public void preferenceChange(PreferenceChangeEvent evt) {
                        if (evt.getKey() == null || evt.getKey().startsWith(FoldUtilitiesImpl.PREF_COLLAPSE_PREFIX)) {
                            if (!initialFoldState.isEmpty()) {
                                initialFoldState = new HashMap<FoldType, Boolean>();
                            }
                        }
                        if (evt.getKey() != null && FoldUtilitiesImpl.PREF_CODE_FOLDING_ENABLED.equals(evt.getKey())) {
                            foldingEnabledSettingChange();
                        }
                    }
                }, foldPreferences);
                foldPreferences.addPreferenceChangeListener(weakPrefL);
            }
        }
        return foldPreferences;
    }
    
    /**
     * Save structure of post process operations.
     * In addition to the collection for post-processing, DocumentEvent that provoked the operation
     * is saved.
     */
    private static class Save {
        private DocumentEvent evt;
        private Collection postProcess;

        public Save(DocumentEvent evt, Collection postProcess) {
            this.evt = evt;
            this.postProcess = postProcess;
        }
    }
    
    /**
     * Keeps saved fold states/operations to be processed after remove
     */
    private ThreadLocal<Save>   removePostProcess = new ThreadLocal<Save>();

    /**
     * The listener saves position information for folds, which will be affected by remove operation.
     * Folds, which are fully contained within the removed block are not saved, they will be ultimately damaged. 
     */
    private class DL implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {}

        @Override
        public void changedUpdate(DocumentEvent e) {}
        
        @Override
        public void removeUpdate(DocumentEvent evt) {
            Collection pp = new ArrayList(16);
            preRemoveCheckDamaged(rootFold, evt, pp);
            removePostProcess.set(new Save(evt, pp));
        }
    }
    
    /**
     * Fold should be declared as empty
     */
    static final int OPERATION_EMPTY = 0;
    
    /**
     * The fold should be marked as damaged. Bits at {@link FoldUtilitiesImpl#FLAGS_DAMAGED} specifies the damage done.
     */
    static final int OPERATION_DAMAGE = 8; 
    
    /**
     * The fold should collapse - the start or end of an unguarded fold has been touched
     */
    static final int OPERATION_COLLAPSE = 16;
    
    /**
     * The fold should be only updated.
     */
    static final int OPERATION_UPDATE = 24;
    
    /**
     * Mask for the operation code.
     */
    static final int OPERATION_MASK = 3 << 3;
    
    
    /**
     * Returns the post-processing operations after remove.
     * If the saved operations do not match the document event, empty collection is returned - this is for cleanup
     * after failed operations. Each call will clear out the thread-local where the post-process is collected,
     * so call only ONCE !
     * <p/>
     * The returned collection contains pairs of Objects: Integer, which is a bitfield composed from two pieces of
     * information: The OPERATION_ code and the {@link FoldUtilitiesImpl} FLAG_ constants that specify the damage
     * done to the fold by the remove operation.
     * <p/>
     * 
     * @param evt the event that provoked the mutation. Used as an identity key of post process operations.
     * @return Collection of post-process operations.
     */
    public Collection getRemovePostProcess(DocumentEvent evt) {
        Save p = removePostProcess.get();
        removePostProcess.remove();
        if (p == null || p.evt != evt) {
            return Collections.EMPTY_LIST;
        }
        return p.postProcess;
    }
    
    void preRemoveCheckDamaged(Fold fold, DocumentEvent evt, Collection damaged) {
        int removeOffset = evt.getOffset();
        int endRemove = removeOffset + evt.getLength();
        
        int childIndex = FoldUtilitiesImpl.findFoldStartIndex(fold, removeOffset, true);
        if (childIndex == -1) {
            if (fold.getFoldCount() == 0) {
                return;
            }
            Fold first = fold.getFold(0);
            if (first.getStartOffset() <= endRemove) {
                childIndex = 0;
            } else {
                return;
            }
        }
        // Check if previous fold was affected too
        if (childIndex >= 1) {
            Fold prevChildFold = fold.getFold(childIndex - 1);
            if (prevChildFold.getEndOffset() == removeOffset) {
                preRemoveCheckDamaged(prevChildFold, evt, damaged);
            }
        }
        boolean removed;
        boolean startsWithin = false;
        do {
            int flag;
            
            Fold childFold = fold.getFold(childIndex);
            startsWithin = childFold.getStartOffset() < removeOffset && 
                           childFold.getEndOffset() <= endRemove;
            removed = false;
            if (FoldUtilitiesImpl.becomesEmptyAfterRemove(childFold, evt)) {
                damaged.add(OPERATION_EMPTY | FoldUtilitiesImpl.FLAG_START_DAMAGED | FoldUtilitiesImpl.FLAG_END_DAMAGED);
                damaged.add(childFold);
                preRemoveCheckDamaged(childFold, evt, damaged); // nest prior removing
                removed = true;

                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("preRemoveCheck: removed empty " // NOI18N
                    + childFold + '\n');
                }

            } else if ((flag = FoldUtilitiesImpl.becomesDamagedByRemove(childFold, evt, false)) != 0) {
                damaged.add(OPERATION_DAMAGE | flag);
                damaged.add(childFold);
                preRemoveCheckDamaged(childFold, evt, damaged); // nest prior removing
                removed = true;

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.finer("preRemoveCheck: removed damaged " // NOI18N
                    + childFold + '\n');
                }

            } else if (childFold.getFoldCount() > 0) { // check children
                // Some children could be damaged even if this one was not
                preRemoveCheckDamaged(childFold, evt, damaged);
            }

            // Check whether the expand is necessary
            if (!removed) { // only if not removed yet
                if (childFold.isCollapsed() && ((flag = FoldUtilitiesImpl.becomesDamagedByRemove(childFold, evt, true)) > 0)) {
                    damaged.add(OPERATION_COLLAPSE | flag);
                    damaged.add(childFold);
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.finer("preRemoveCheck: expansion needed " // NOI18N
                        + childFold + '\n');
                    }
                } else {
                    damaged.add(OPERATION_UPDATE);
                    damaged.add(childFold);
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.finer("preRemoveCheck: removeUpdate call " // NOI18N
                        + childFold + '\n');
                    }
                }
            }

            childIndex++;
        } while ((startsWithin || removed) && childIndex < fold.getFoldCount());
    }
    
    
    
    private static volatile Method addUpdateListener;
    private static volatile Method removeUpdateListener;
    private static volatile Method eventInUndo;
    private static volatile Method eventInRedo;
    
    /**
     * This is a hacky way how to get info from BaseDocumentEvent defined by editor.lib; folding does not have
     * access to it.
     * 
     * @param evt document event
     * @return 
     */
    static boolean isEventInUndoRedoHack(DocumentEvent evt) {
        if (eventInRedo == null) {
            if (!evt.getClass().getName().endsWith("BaseDocumentEvent")) { // NOI18N
                return false;
            }
            try {
                eventInUndo = evt.getClass().getMethod("isInUndo");
                eventInRedo = evt.getClass().getMethod("isInRedo");
            } catch (NoSuchMethodException | SecurityException ex) {
                // should not happen
                Exceptions.printStackTrace(ex);
                return false;
            }
        }
        // faster than getClass().getName + string compare.
        if (eventInRedo.getDeclaringClass() != evt.getClass()) {
            return false;
        }
        try {
            return (Boolean)eventInUndo.invoke(evt) || (Boolean)eventInRedo.invoke(evt);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
        
    }

    private static void invokeUpdateListener(Document doc, DocumentListener l, boolean add) {
        Method m = add ? addUpdateListener : removeUpdateListener;
        if (m == null) {
            try {
                m = doc.getClass().getMethod(add ?
                        "addUpdateDocumentListener" : "removeUpdateDocumentListener", // NOI18N
                        DocumentListener.class);
                if (!"org.netbeans.editor.BaseDocument".equals(m.getDeclaringClass().getName())) { // NOI18N
                    return;
                }
                if (add) {
                    addUpdateListener = m;
                } else {
                    removeUpdateListener = m;
                }
            } catch (NoSuchMethodException ex) {
                return;
            } catch (SecurityException ex) {
                Exceptions.printStackTrace(ex);
                return;
            }
        }
        Class<?> clazz = m.getDeclaringClass();
        if (!clazz.isInstance(doc)) {
            return;
        }
        try {
            m.invoke(doc, l);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    int getDamagedCount() {
        return rebuilding ? 0 : this.damaged;
    }
    
    void markDamaged() {
        this.damaged++;
    }
    
    void markClean() {
        this.damaged = 0;
    }
    
    String getCommittedContent() {
        return committedContent;
    }
    
    void transactionCommitted() {
        if (damaged > 0) {
            Document d = component.getDocument();
            try {
                committedContent = d.getText(0, d.getLength());
            } catch (BadLocationException ex) {
                // no op
            }
        }
    }
}
