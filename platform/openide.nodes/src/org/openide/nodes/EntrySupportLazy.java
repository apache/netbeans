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
package org.openide.nodes;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.Children.Entry;
import org.openide.nodes.EntrySupportLazyState.EntryInfo;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
class EntrySupportLazy extends EntrySupport {
    private static final int prefetchCount = Math.max(Integer.getInteger("org.openide.explorer.VisualizerNode.prefetchCount", 50), 0); // NOI18N
    static final Logger LOGGER = Logger.getLogger(EntrySupportLazy.class.getName());
        
    /** represents state of this object. The state itself should not 
     * mutate, the reference to different states, however may -
     * in future.
     */
    protected final AtomicReference<EntrySupportLazyState> internal = new AtomicReference<EntrySupportLazyState>(EntrySupportLazyState.UNINITIALIZED);
    
    //private static final boolean LOG_ENABLED = LOGGER.isLoggable(Level.FINER);

    public EntrySupportLazy(Children ch) {
        super(ch);
    }
    /** lock to guard creation of snapshots */
    protected final Object LOCK = new Object();
    /** @GuardedBy("LOCK")*/
    private int snapshotCount;
    
    private void setState(EntrySupportLazyState old, EntrySupportLazyState s) {
        assert Thread.holdsLock(LOCK);
        boolean success = internal.compareAndSet(old, s);
        assert success : "Somebody changed internal state meanwhile!\n"
            + "Expected: " + old + "\n"
            + "Current : " + internal.get(); // NOI18N
    }

    public boolean checkInit() {
        EntrySupportLazyState state;
        
        boolean doInit = false;
        synchronized (LOCK) {
            state = internal.get();

            if (state.isInited()) {
                return true;
            }
            if (!state.isInitInProgress()) {
                doInit = true;
                final EntrySupportLazyState newState = state.changeProgress(true).changeThread(Thread.currentThread());
                setState(state, newState);
                state = newState;
            }
        }
        final boolean LOG_ENABLED = LOGGER.isLoggable(Level.FINER);
        if (doInit) {
            if (LOG_ENABLED) {
                LOGGER.finer("Initialize " + this + " on " + Thread.currentThread());
                LOGGER.finer("    callAddNotify()"); // NOI18N
            }
            try {
                children.callAddNotify();
            } finally {
                synchronized (LOCK) {
                    class Notify implements Runnable {
                        public Notify(EntrySupportLazyState old) {
                            EntrySupportLazyState s = internal.get();
                            setState(s, s.changeInited(true));
                        }
                        
                        @Override
                        public void run() {
                            synchronized (LOCK) {
                                EntrySupportLazyState s = internal.get();
                                if (s.isInited()) {
                                    setState(s, s.changeThread(null));
                                } else {
                                    // can this happen?
                                    throw new IllegalStateException();
                                }
                                LOCK.notifyAll();
                            }
                        }
                    }
                    Notify notify = new Notify(state);
                    if (Children.MUTEX.isReadAccess()) {
                        Children.MUTEX.postWriteRequest(notify);
                    } else {
                        notify.run();
                    }
                }
            }
        } else {
            if (Children.MUTEX.isReadAccess() || Children.MUTEX.isWriteAccess() || (state.initThread() == Thread.currentThread())) {
                if (LOG_ENABLED) {
                    LOGGER.log(Level.FINER, "Cannot wait for finished initialization " + this + " on " + Thread.currentThread() + " read access: " + Children.MUTEX.isReadAccess() + " write access: " + Children.MUTEX.isWriteAccess() + " initThread: " + state.initThread());
                }
                // we cannot wait
                notifySetEntries();
                return false;
            }
            // otherwise we can wait
            synchronized (LOCK) {
                for (;;) {
                    EntrySupportLazyState current = internal.get();
                    if (current.initThread() != null) {
                        try {
                            LOCK.wait();
                        } catch (InterruptedException ex) {
                        }
                        continue;
                    }
                    break;
                }
            }
        }
        return true;
    }

    final int getSnapshotCount() {
        assert Thread.holdsLock(LOCK);
        return snapshotCount;
    }

    final void incrementCount() {
        assert Thread.holdsLock(LOCK);
        snapshotCount++;
    }

    final void decrementCount() {
        assert Thread.holdsLock(LOCK);
        snapshotCount++;
    }

    @Override
    List<Node> snapshot() {
        checkInit();
        try {
            Children.PR.enterReadAccess();
            return createSnapshot();
        } finally {
            Children.PR.exitReadAccess();
        }
    }

    final void registerNode(int delta, EntryInfo who) {
        if (delta == -1) {
            try {
                Children.PR.enterWriteAccess();
                boolean zero = false;
                LOGGER.finer("register node"); // NOI18N
                synchronized (EntrySupportLazy.this.LOCK) {
                    EntrySupportLazyState state = internal.get();
                    int cnt = 0;
                    boolean found = false;
                    cnt += getSnapshotCount();
                    if (cnt == 0) {
                        for (Entry entry : notNull(state.getVisibleEntries())) {
                            EntryInfo info = state.getEntryToInfo().get(entry);
                            if (info.currentNode() != null) {
                                cnt++;
                                break;
                            }
                            if (info == who) {
                                found = true;
                            }
                        }
                    }
                    zero = cnt == 0 && (found || who == null);
                    if (zero) {
                        setState(state, state.changeInited(false).changeThread(null).changeProgress(false));
                        if (children.getEntrySupport() == this) {
                            if (LOGGER.isLoggable(Level.FINER)) {
                                LOGGER.finer("callRemoveNotify() " + this); // NOI18N
                            }
                            children.callRemoveNotify();
                        }
                    }
                }
            } finally {
                Children.PR.exitWriteAccess();
            }
        }
    }

    @Override
    public Node getNodeAt(int index) {
        if (!checkInit()) {
            return null;
        }
        Node node = null;
        while (true) {
            try {
                Children.PR.enterReadAccess();
                EntrySupportLazyState state = internal.get();
                List<Entry> e = notNull(state.getVisibleEntries());
                if (index >= e.size()) {
                    return node;
                }
                Entry entry = e.get(index);
                EntryInfo info = state.getEntryToInfo().get(entry);
                node = info.getNode();
                if (!isDummyNode(node)) {
                    return node;
                }
                hideEmpty(null, entry);
            } finally {
                Children.PR.exitReadAccess();
            }
            if (Children.MUTEX.isReadAccess()) {
                return node;
            }
        }
    }

    @Override
    public Node[] getNodes(boolean optimalResult) {
        if (!checkInit()) {
            return new Node[0];
        }
        Node holder = null;
        if (optimalResult) {
            holder = children.findChild(null);
        }
        Children.LOG.log(Level.FINEST, "findChild returns: {0}", holder); // NOI18N
        Children.LOG.log(Level.FINEST, "after findChild: {0}", optimalResult); // NOI18N
        while (true) {
            Set<Entry> invalidEntries = null;
            Node[] tmpNodes = null;
            try {
                Children.PR.enterReadAccess();
                EntrySupportLazyState state = internal.get();
                List<Entry> e = notNull(state.getVisibleEntries());
                List<Node> toReturn = new ArrayList<Node>(e.size());
                for (Entry entry : e) {
                    EntryInfo info = state.getEntryToInfo().get(entry);
                    assert !info.isHidden();
                    Node node = info.getNode();
                    if (isDummyNode(node)) {
                        if (invalidEntries == null) {
                            invalidEntries = new HashSet<Entry>();
                        }
                        invalidEntries.add(entry);
                    }
                    toReturn.add(node);
                }
                tmpNodes = toReturn.toArray(new Node[0]);
                if (invalidEntries == null) {
                    return tmpNodes;
                }
                hideEmpty(invalidEntries, null);
            } finally {
                Children.PR.exitReadAccess();
            }
            if (Children.MUTEX.isReadAccess()) {
                return tmpNodes;
            }
        }
    }

    @Override
    public Node[] testNodes() {
        EntrySupportLazyState state = internal.get();
        if (!state.isInited()) {
            return null;
        }
        List<Node> created = new ArrayList<Node>();
        try {
            Children.PR.enterReadAccess();
            for (Entry entry : notNull(state.getVisibleEntries())) {
                EntryInfo info = state.getEntryToInfo().get(entry);
                Node node = info.currentNode();
                if (node != null) {
                    created.add(node);
                }
            }
        } finally {
            Children.PR.exitReadAccess();
        }
        return created.isEmpty() ? null : created.toArray(new Node[0]);
    }

    @Override
    public int getNodesCount(boolean optimalResult) {
        checkInit();
        try {
            Children.PR.enterReadAccess();
            EntrySupportLazyState state = internal.get();
            return notNull(state.getVisibleEntries()).size();
        } finally {
            Children.PR.exitReadAccess();
        }
    }

    @Override
    public boolean isInitialized() {
        EntrySupportLazyState state = internal.get();
        return state.isInited();
    }

    Entry entryForNode(Node key) {
        EntrySupportLazyState state = internal.get();
        for (Map.Entry<Entry, EntryInfo> entry : state.getEntryToInfo().entrySet()) {
            if (entry.getValue().currentNode() == key) {
                return entry.getKey();
            }
        }
        return null;
    }

    static boolean isDummyNode(Node node) {
        return node.getClass() == DummyNode.class;
    }

    @Override
    void refreshEntry(Entry entry) {
        assert Children.MUTEX.isWriteAccess();
        final boolean LOG_ENABLED = LOGGER.isLoggable(Level.FINER);
        if (LOG_ENABLED) {
            LOGGER.finer("refreshEntry() " + this);
            LOGGER.finer("    entry: " + entry); // NOI18N
        }
        EntrySupportLazyState[] stateHolder = { internal.get() };
        if (!stateHolder[0].isInited()) {
            return;
        }
        EntryInfo info = stateHolder[0].getEntryToInfo().get(entry);
        if (info == null) {
            if (LOG_ENABLED) {
                LOGGER.finer("    no such entry: " + entry); // NOI18N
            }
            // no such entry
            return;
        }
        Node oldNode = info.currentNode();
        EntryInfo newInfo = null;
        Node newNode = null;
        if (info.isHidden()) {
            newNode = info.getNode(true, null);
            newInfo = info.changeIndex(-1);
        } else {
            newInfo = info.changeNode(null);
            newNode = newInfo.getNode(true, null);
        }
        boolean newIsDummy = isDummyNode(newNode);
        if (newIsDummy && info.isHidden()) {
            // dummy is already hidden
            return;
        }
        if (newNode.equals(oldNode)) {
            // same node =>
            return;
        }
        if (!info.isHidden() || newIsDummy) {
            removeEntries(stateHolder, null, entry, newInfo, true, true);
        }
        if (newIsDummy) {
            return;
        }
        
        EntrySupportLazyState state = stateHolder[0];
        final Map<Entry,EntryInfo> new2Info = new HashMap<Entry, EntryInfo>(state.getEntryToInfo());
        // recompute indexes
        int index = 0;
        int changedIndex = -1;
        List<Entry> arr = new ArrayList<Entry>();
        for (Entry tmpEntry : state.getEntries()) {
            EntryInfo tmpInfo = null;
            if (tmpEntry.equals(entry)) {
                tmpInfo = newInfo;
                changedIndex = index;
            }
            if (tmpInfo == null) {
                tmpInfo = state.getEntryToInfo().get(tmpEntry);
            }
            if (tmpInfo.isHidden()) {
                continue;
            }
            new2Info.put(tmpEntry, tmpInfo.changeIndex(index++));
            arr.add(tmpEntry);
        }
        assert changedIndex != -1;
        synchronized (LOCK) {
            setState(state, state.changeEntries(null, arr, new2Info));
        }
        fireSubNodesChangeIdx(true, new int[]{changedIndex}, entry, createSnapshot(), null);
    }

    void notifySetEntries() {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("notifySetEntries() " + this); // NOI18N
        }
        synchronized (LOCK) {
            EntrySupportLazyState state = internal.get();
            setState(state, state.changeMustNotify(true));
        }
    }

    @Override
    void setEntries(Collection<? extends Entry> newEntries, boolean noCheck) {
        assert Children.MUTEX.isWriteAccess();
        for (;;) {
            EntrySupportLazyState[] stateHolder = { null };
            Set<Entry> entriesToRemove = setEntriesSimple(stateHolder, newEntries);
            if (entriesToRemove == null) {
                return;
            }
            if (!entriesToRemove.isEmpty()) {
                removeEntries(stateHolder, entriesToRemove, null, null, false, false);
            }
            // change the order of entries, notifies
            // it and again brings children to up-to-date state, recomputes indexes
            // state has been modified
            Collection<Entry> toAdd = updateOrder(stateHolder, newEntries);
            EntrySupportLazyState state = stateHolder[0];
            if (!toAdd.isEmpty()) {
                ArrayList<Entry> newStateEntries = new ArrayList<Entry>(newEntries);
                int[] idxs = new int[toAdd.size()];
                int addIdx = 0;
                int inx = 0;
                boolean createNodes = toAdd.size() == 2 && prefetchCount > 0;
                ArrayList<Entry> newStateVisibleEntries = new ArrayList<Entry>();
                Map<Entry, EntryInfo> newState2Info = new HashMap<Entry, EntryInfo>(state.getEntryToInfo());
                for (int i = 0; i < newStateEntries.size(); i++) {
                    Entry entry = newStateEntries.get(i);
                    EntryInfo info = newState2Info.get(entry);
                    if (info == null) {
                        info = new EntryInfo(this, entry);
                        if (createNodes) {
                            Node n = info.getNode();
                            if (isDummyNode(n)) {
                                // mark as hidden
                                newState2Info.put(entry, info.changeIndex(-2));
                                continue;
                            }
                        }
                        idxs[addIdx++] = inx;
                    }
                    if (info.isHidden()) {
                        continue;
                    }
                    newState2Info.put(entry, info.changeIndex(inx++));
                    newStateVisibleEntries.add(entry);
                }
                synchronized (LOCK) {
                    final EntrySupportLazyState newState = state.changeEntries(newStateEntries, newStateVisibleEntries, newState2Info);
                    if (internal.get() != state) {
                        // try once again
                        state = internal.get();
                        continue;
                    }
                    setState(state, newState);
                }
                if (addIdx == 0) {
                    return;
                }
                if (idxs.length != addIdx) {
                    int[] tmp = new int[addIdx];
                    for (int i = 0; i < tmp.length; i++) {
                        tmp[i] = idxs[i];
                    }
                    idxs = tmp;
                }
                fireSubNodesChangeIdx(true, idxs, null, createSnapshot(), null);
            }
            return;
        }
    }

    /** Updates the order of entries.
     * @param stateHolder current state of nodes
     * @param newEntries new set of entries
     * @return list of infos that should be added
     */
    private List<Entry> updateOrder(EntrySupportLazyState[] stateHolder, Collection<? extends Entry> newEntries) {
        assert Children.MUTEX.isWriteAccess();
        EntrySupportLazyState state = stateHolder[0];
        List<Entry> toAdd = new LinkedList<Entry>();
        int[] perm = new int[state.getVisibleEntries().size()];
        int currentPos = 0;
        int permSize = 0;
        List<Entry> reorderedEntries = null;
        List<Entry> newVisible = null;
        Map<Entry,EntryInfo> new2Infos = null;
        final Map<Entry, EntryInfo> old2Infos = state.getEntryToInfo();
        for (Entry entry : newEntries) {
            final EntryInfo info = old2Infos.get(entry);
            if (info == null) {
                // this entry has to be added
                toAdd.add(entry);
            } else {
                if (reorderedEntries == null) {
                    reorderedEntries = new LinkedList<Entry>();
                    newVisible = new ArrayList<Entry>();
                    new2Infos = new HashMap<Entry, EntryInfo>(old2Infos);
                }
                reorderedEntries.add(entry);
                if (info.isHidden()) {
                    continue;
                }
                newVisible.add(entry);
                int oldPos = info.getIndex();
                // already there => test if it should not be reordered
                if (currentPos != oldPos) {
                    new2Infos.put(entry, info.changeIndex(currentPos));
                    perm[oldPos] = 1 + currentPos;
                    permSize++;
                }
                currentPos++;
            }
        }
        if (permSize > 0) {
            // now the perm array contains numbers 1 to ... and
            // 0 one places where no permutation occures =>
            // decrease numbers, replace zeros
            for (int i = 0; i < perm.length; i++) {
                if (perm[i] == 0) {
                    // fixed point
                    perm[i] = i;
                } else {
                    // decrease
                    perm[i]--;
                }
            }
            // reorderedEntries are not null
            synchronized (LOCK) {
                final EntrySupportLazyState newState = state.changeEntries(reorderedEntries, newVisible, new2Infos);
                setState(state, newState);
                stateHolder[0] = newState;
            }
            Node p = children.parent;
            if (p != null) {
                p.fireReorderChange(perm);
            }
        }
        return toAdd;
    }

    Node getNode(Entry entry) {
        checkInit();
        try {
            Children.PR.enterReadAccess();
            EntrySupportLazyState state = internal.get();
            EntryInfo info = state.getEntryToInfo().get(entry);
            if (info == null) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("getNode() " + this);
                    LOGGER.finer("    no such entry: " + entry); // NOI18N
                }
                return null;
            }
            Node node = info.getNode();
            return isDummyNode(node) ? null : node;
        } finally {
            Children.PR.exitReadAccess();
        }
    }

    /** @param added added or removed
     *  @param idxs list of integers with indexes that changed
     */
    protected void fireSubNodesChangeIdx(boolean added, int[] idxs, Entry sourceEntry, List<Node> current, List<Node> previous) {
        if (children.parent != null && children.getEntrySupport() == this) {
            children.parent.fireSubNodesChangeIdx(added, idxs, sourceEntry, current, previous);
        }
    }

    static <T> List<T> notNull(List<T> it) {
        if (it == null) {
            return Collections.emptyList();
        } else {
            return it;
        }
    }

    static String dumpEntriesInfos(List<Entry> entries, Map<Entry, EntryInfo> entryToInfo) {
        StringBuilder sb = new StringBuilder();
        int cnt = 0;
        for (Entry entry : entries) {
            sb.append("\n").append(++cnt).append(" entry ").append(entry).append(" -> ").append(entryToInfo.get(entry)); // NOI18N
        }
        sb.append("\n\n"); // NOI18N
        for (Map.Entry<Entry, EntryInfo> e : entryToInfo.entrySet()) {
            if (entries.contains(e.getKey())) {
                sb.append("\n").append(" contained ").append(e.getValue()); // NOI18N
            } else {
                sb.append("\n").append(" missing   ").append(e.getValue()).append(" for ").append(e.getKey()); // NOI18N
            }
        }
        return sb.toString();
    }

    @Override
    protected List<Entry> getEntries() {
        EntrySupportLazyState state = internal.get();
        return state.getEntries();
    }

    private Set<Entry> setEntriesSimple(EntrySupportLazyState[] stateHolder, Collection<? extends Entry> newEntries) {
        for (;;) {
            EntrySupportLazyState state = stateHolder[0] = internal.get();
            final boolean LOG_ENABLED = LOGGER.isLoggable(Level.FINER);
            if (LOG_ENABLED) {
                LOGGER.finer("setEntries(): " + this); // NOI18N
                LOGGER.finer("    inited: " + state.isInited()); // NOI18N
                LOGGER.finer("    mustNotifySetEnties: " + state.isMustNotify()); // NOI18N
                LOGGER.finer("    newEntries size: " + newEntries.size() + " data:" + newEntries); // NOI18N
                LOGGER.finer("    entries size: " + state.getEntries().size() + " data:" + state.getEntries()); // NOI18N
                LOGGER.finer("    visibleEntries size: " + notNull(state.getVisibleEntries()).size() + " data:" + state.getVisibleEntries()); // NOI18N
                LOGGER.finer("    entryToInfo size: " + state.getEntryToInfo().size()); // NOI18N
            }
            int entriesSize = 0;
            int entryToInfoSize = 0;
            assert (entriesSize = state.getEntries().size()) >= 0;
            assert (entryToInfoSize = state.getEntryToInfo().size()) >= 0;
            assert state.getEntries().size() == state.getEntryToInfo().size() : "Entries: " + state.getEntries().size() + "; vis. entries: " + notNull(state.getVisibleEntries()).size() + "; Infos: " + state.getEntryToInfo().size() + "; entriesSize: " + entriesSize + "; entryToInfoSize: " + entryToInfoSize + dumpEntriesInfos(state.getEntries(), state.getEntryToInfo()); // NOI18N
            if (!state.isMustNotify() && !state.isInited()) {
                ArrayList<Entry> newStateEntries = new ArrayList<Entry>(newEntries);
                ArrayList<Entry> newStateVisibleEntries = new ArrayList<Entry>(newEntries);
                Map<Entry, EntryInfo> newState2Info = new HashMap<Entry, EntryInfo>();
                {
                    Map<Entry, EntryInfo> oldState2Info = state.getEntryToInfo();
                    for (Entry entry : newEntries) {
                        final EntryInfo prev = oldState2Info.get(entry);
                        if (prev != null) {
                            newState2Info.put(entry, prev);
                        }
                    }
                }
                for (int i = 0; i < newStateEntries.size(); i++) {
                    Entry entry = newStateEntries.get(i);
                    EntryInfo info = newState2Info.get(entry);
                    if (info == null) {
                        info = new EntryInfo(this, entry);
                    }
                    newState2Info.put(entry, info.changeIndex(i));
                }
                synchronized (LOCK) {
                    if (state != internal.get()) {
                        continue;
                    }
                    final EntrySupportLazyState newState = state.changeEntries(newStateEntries, newStateVisibleEntries, newState2Info);
                    setState(state, newState);
                    stateHolder[0] = newState;
                }
                return null;
            }
            Set<Entry> entriesToRemove = new HashSet<Entry>(state.getEntries());
            removeAllOpt(entriesToRemove, newEntries);
            return entriesToRemove;
        }
    }

    /**
     * Optimized version of removeAll for HashSets. The implementation in
     * {@link java.util.AbstractSet#removeAll(java.util.Collection)} (at least
     * in Java 8) calls toRemove.contains(x) for each element x in base if
     * base.size() &lt;= toRemove.size(), which is very slow if toRemove is big
     * ArrayList, whose complexity of method "contains" is linear.
     *
     * See bug 230180.
     *
     * @param base A set from which the elements will be removed.
     * @param toRemove A collection with elements to remove.
     *
     * @return True if the base collection was modified, false otherwise.
     */
    private static boolean removeAllOpt(
            Set<Entry> base, Collection<? extends Entry> toRemove) {

        if ((toRemove instanceof ArrayList
                && toRemove.size() > 100
                && toRemove.size() >= base.size())) {
            HashSet<Entry> toRemoveAsSet = new HashSet<Entry>();
            toRemoveAsSet.addAll(toRemove);
            return base.removeAll(toRemoveAsSet);
        } else {
            return base.removeAll(toRemove);
        }
    }

    /** holds node for entry; 1:1 mapping */

    /** Dummy node class for entries without any node */
    static class DummyNode extends AbstractNode {
        public DummyNode() {
            super(Children.LEAF);
        }
    }

    void hideEmpty(final Set<Entry> entries, final Entry entry) {
        Children.MUTEX.postWriteRequest(new Runnable() {
            @Override
            public void run() {
                EntrySupportLazyState[] stateHolder = { internal.get() };
                removeEntries(stateHolder, entries, entry, null, true, true);
            }
        });
    }

    private void removeEntries(
        EntrySupportLazyState[] stateHolder,
        Set<Entry> entriesToRemove, Entry entryToRemove, EntryInfo newEntryInfo, 
        boolean justHide, boolean delayed
    ) {
        assert Children.MUTEX.isWriteAccess();
        final boolean LOG_ENABLED = LOGGER.isLoggable(Level.FINER);
        if (LOG_ENABLED) {
            LOGGER.finer("removeEntries(): " + this); // NOI18N
            LOGGER.finer("    entriesToRemove: " + entriesToRemove); // NOI18N
            LOGGER.finer("    entryToRemove: " + entryToRemove); // NOI18N
            LOGGER.finer("    newEntryInfo: " + newEntryInfo); // NOI18N
            LOGGER.finer("    justHide: " + justHide); // NOI18N
            LOGGER.finer("    delayed: " + delayed); // NOI18N
        }
        int index = 0;
        int removedIdx = 0;
        int removedNodesIdx = 0;
        int expectedSize = entriesToRemove != null ? entriesToRemove.size() : 1;
        int[] idxs = new int[expectedSize];
        EntrySupportLazyState state = stateHolder[0];
        List<Entry> previousEntries = state.getVisibleEntries();
        Map<Entry, EntryInfo> previousInfos = null;
        Map<Entry, EntryInfo> new2Infos = null;
        List<Entry> newEntries = justHide ? null : new ArrayList<Entry>();
        Node[] removedNodes = null;
        ArrayList<Entry> newStateVisibleEntries = new ArrayList<Entry>();
        Map<Entry, EntryInfo> oldState2Info = state.getEntryToInfo();
        for (Entry entry : state.getEntries()) {
            EntryInfo info = oldState2Info.get(entry);
            if (info == null) {
                continue;
            }
            boolean remove;
            if (entriesToRemove != null) {
                remove = entriesToRemove.remove(entry);
            } else {
                remove = entryToRemove.equals(entry);
            }
            if (remove) {
                if (info.isHidden()) {
                    if (!justHide) {
                        if (new2Infos == null) {
                            new2Infos = new HashMap<Entry, EntryInfo>(oldState2Info);
                        }
                        new2Infos.remove(entry);
                    }
                    continue;
                }
                idxs[removedIdx++] = info.getIndex();
                if (previousInfos == null) {
                    previousInfos = new HashMap<Entry, EntryInfo>(oldState2Info);
                }
                Node node = info.currentNode();
                if (!info.isHidden() && node != null && !isDummyNode(node)) {
                    if (removedNodes == null) {
                        removedNodes = new Node[expectedSize];
                    }
                    removedNodes[removedNodesIdx++] = node;
                }
                if (new2Infos == null) {
                    new2Infos = new HashMap<Entry, EntryInfo>(oldState2Info);
                }
                if (justHide) {
                    EntryInfo dup = newEntryInfo != null ? newEntryInfo : info.changeNode(null);
                    // mark as hidden 
                    new2Infos.put(info.entry(), dup.changeIndex(-2));
                } else {
                    new2Infos.remove(entry);
                }
            } else {
                if (new2Infos == null) {
                    new2Infos = new HashMap<Entry, EntryInfo>(oldState2Info);
                }
                if (!info.isHidden()) {
                    newStateVisibleEntries.add(info.entry());
                    new2Infos.put(info.entry(), info.changeIndex(index++));
                } else {
                    new2Infos.put(info.entry(), info.changeIndex(-2));
                }
                if (!justHide) {
                    newEntries.add(info.entry());
                }
            }
        }
        if (!justHide) {
            //state.entries = newEntries;
        }
        synchronized (LOCK) {
            final EntrySupportLazyState newState = state.changeEntries(newEntries, newStateVisibleEntries, new2Infos);
            setState(state, newState);
            stateHolder[0] = newState;
        }
        if (removedIdx == 0) {
            return;
        }
        if (removedIdx < idxs.length) {
            idxs = (int[]) resizeArray(idxs, removedIdx);
        }
        List<Node> curSnapshot = createSnapshot(newStateVisibleEntries, new HashMap<Entry, EntryInfo>(new2Infos), delayed);
        List<Node> prevSnapshot = createSnapshot(previousEntries, previousInfos, false);
        fireSubNodesChangeIdx(false, idxs, entryToRemove, curSnapshot, prevSnapshot);
        if (removedNodesIdx > 0) {
            if (removedNodesIdx < removedNodes.length) {
                removedNodes = (Node[]) resizeArray(removedNodes, removedNodesIdx);
            }
            if (children.parent != null) {
                for (Node node : removedNodes) {
                    node.deassignFrom(children);
                    node.fireParentNodeChange(children.parent, null);
                }
            }
            children.destroyNodes(removedNodes);
        }
    }

    private static Object resizeArray(Object oldArray, int newSize) {
        int oldSize = java.lang.reflect.Array.getLength(oldArray);
        Class elementType = oldArray.getClass().getComponentType();
        Object newArray = java.lang.reflect.Array.newInstance(elementType, newSize);
        int preserveLength = Math.min(oldSize, newSize);
        if (preserveLength > 0) {
            System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
        }
        return newArray;
    }

    LazySnapshot createSnapshot() {
        EntrySupportLazyState state = internal.get();
        return createSnapshot(state.getVisibleEntries(), new HashMap<Entry, EntryInfo>(state.getEntryToInfo()), false);
    }

    protected LazySnapshot createSnapshot(List<Entry> entries, Map<Entry, EntryInfo> e2i, boolean delayed) {
        synchronized (LOCK) {
            return delayed ? new DelayedLazySnapshot(entries, e2i) : new LazySnapshot(entries, e2i);
        }
    }

    class LazySnapshot extends AbstractList<Node> {

        final List<Entry> entries;
        final Map<Entry, EntryInfo> entryToInfo;

        public LazySnapshot(List<Entry> entries, Map<Entry, EntryInfo> e2i) {
            incrementCount();
            this.entries = entries;
            assert entries != null;
            this.entryToInfo = e2i != null ? e2i : Collections.<Entry, EntryInfo>emptyMap();
            assert entries.size() <= entryToInfo.size();
        }

        public Node get(int index) {
            Entry entry = entries.get(index);
            return get(entry);
        }

        Node get(Entry entry) {
            EntryInfo info = entryToInfo.get(entry);
            Node node = info.getNode();
            if (isDummyNode(node)) {
                // force new snapshot
                hideEmpty(null, entry);
            }
            return node;
        }

        @Override
        public String toString() {
            return entries.toString();
        }

        public int size() {
            return entries.size();
        }

        @Override
        protected void finalize() throws Throwable {
            boolean unregister = false;
            synchronized (LOCK) {
                decrementCount();
                if (getSnapshotCount() == 0) {
                    unregister = true;
                }
            }
            if (unregister) {
                registerNode(-1, null);
            }
        }
    }

    final class DelayedLazySnapshot extends LazySnapshot {

        public DelayedLazySnapshot(List<Entry> entries, Map<Entry, EntryInfo> e2i) {
            super(entries, e2i);
        }
    }
    
}
