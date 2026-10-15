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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.Children.Entry;
import org.openide.util.Utilities;

/** Default support that just fires changes directly to children and is suitable
 * for simple mappings.
 */
class EntrySupportDefault extends EntrySupport {
    private List<Entry> entries = Collections.emptyList();

    private static final Reference<ChildrenArray> EMPTY = new WeakReference<ChildrenArray>(null);
    /** array of children Reference (ChildrenArray) */
    private Reference<ChildrenArray> array = EMPTY;
    /** mapping from entries to info about them */
    private Map<Entry, Info> map;
    private static final Object LOCK = new Object();
    private static final Logger LOGGER = Logger.getLogger(EntrySupportDefault.class.getName()); // NOI18N
    //private static final boolean LOG_ENABLED = LOGGER.isLoggable(Level.FINER);
    private Thread initThread;
    private boolean inited = false;

    public EntrySupportDefault(Children ch) {
        super(ch);
    }

    @Override
    public String toString() {
        return super.toString() + " array: " + array.get(); // NOI18N
    }
    
    

    public boolean isInitialized() {
        ChildrenArray arr = array.get();
        return inited && arr != null && arr.isInitialized();
    }

    @Override
    List<Node> snapshot() {
        Node[] nodes = getNodes();
        try {
            Children.PR.enterReadAccess();
            return createSnapshot();
        } finally {
            Children.PR.exitReadAccess();
        }
    }

    DefaultSnapshot createSnapshot() {
        return new DefaultSnapshot(getNodes(), array.get());
    }

    public final Node[] getNodes() {
        final boolean LOG_ENABLED = LOGGER.isLoggable(Level.FINER);
        if (LOG_ENABLED) {
            LOGGER.finer("getNodes() " + this);
        }
        boolean[] results = new boolean[2];
        for (;;) {
            // initializes the ChildrenArray possibly calls
            // addNotify if this is for the first time
            ChildrenArray tmpArray = getArray(results); // fils results[0]
            Node[] nodes;
            try {
                Children.PR.enterReadAccess();
                if (this != children.getEntrySupport()) {
                    // support was switched while we were waiting for access
                    return new Node[0];
                }
                results[1] = isInitialized();
                nodes = tmpArray.nodes();
            } finally {
                Children.PR.exitReadAccess();
            }
            if (LOG_ENABLED) {
                LOGGER.finer("  length     : " + (nodes == null ? "nodes is null" : nodes.length)); // NOI18N
                LOGGER.finer("  init now   : " + isInitialized()); // NOI18N
            }
            // if not initialized that means that after
            // we computed the nodes, somebody changed them (as a
            // result of addNotify) => we have to compute them
            // again
            if (results[1]) {
                // otherwise it is ok.
                return nodes;
            }
            if (results[0]) {
                // looks like the result cannot be computed, just give empty one
                notifySetEntries();
                return (nodes == null) ? new Node[0] : nodes;
            }
        }
    }

    public Node[] getNodes(boolean optimalResult) {
        final boolean LOG_ENABLED = LOGGER.isLoggable(Level.FINER);
        ChildrenArray hold = null;
        Node find = null;
        if (optimalResult) {
            if (LOG_ENABLED) {
                LOGGER.finer("computing optimal result"); // NOI18N
            }
            hold = getArray(null);
            if (LOG_ENABLED) {
                LOGGER.finer("optimal result is here: " + hold); // NOI18N
            }
            find = children.findChild(null);
            if (LOG_ENABLED) {
                LOGGER.finer("Find child got: " + find); // NOI18N
            }
            Children.LOG.log(Level.FINEST, "after findChild: {0}", optimalResult);
        }
        return getNodes();
    }

    public final int getNodesCount(boolean optimalResult) {
        return getNodes(optimalResult).length;
    }

    @Override
    public Node getNodeAt(int index) {
        Node[] nodes = getNodes();
        return index < nodes.length ? nodes[index] : null;
    }

    /** Computes the nodes now.
     */
    final Node[] justComputeNodes() {
        if (map == null) {
            map = Collections.synchronizedMap(new HashMap<Entry, Info>(17));
            LOGGER.finer("Map initialized");
        }
        List<Node> l = new LinkedList<Node>();
        for (Entry entry : entries) {
            Info info = findInfo(entry);
            l.addAll(info.nodes(false));
        }
        Node[] arr = l.toArray(new Node[0]);
        // initialize parent nodes
        for (int i = 0; i < arr.length; i++) {
            Node n = arr[i];
            if (n == null) {
                LOGGER.warning("null node among children!");
                for (int j = 0; j < arr.length; j++) {
                    LOGGER.log(Level.WARNING, "  {0} = {1}", new Object[]{j, arr[j]});
                }
                for (Entry entry : entries) {
                    Info info = findInfo(entry);
                    LOGGER.log(Level.WARNING, "  entry: {0} info {1} nodes: {2}", new Object[]{entry, info, info.nodes(false)});
                }
                throw new NullPointerException("arr[" + i + "] is null"); // NOI18N
            }
            n.assignTo(children, i);
            n.fireParentNodeChange(null, children.parent);
        }
        return arr;
    }

    /** Finds info for given entry, or registers
     * it, if not registered yet.
     */
    private Info findInfo(Entry entry) {
        synchronized (map) {
            Info info = map.get(entry);
            if (info == null) {
                info = new Info(entry);
                map.put(entry, info);
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("Put: " + entry + " info: " + info);
                }
            }
            return info;
        }
    }
    //
    // Entries
    //
    private boolean mustNotifySetEnties = false;

    void notifySetEntries() {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(this + " mustNotifySetEntries()");
        }
        mustNotifySetEnties = true;
    }

    private void checkConsistency() {
        assert map.size() == this.entries.size() : "map.size()=" + map.size() + " entries.size()=" + this.entries.size();
    }

    @Override
    protected void setEntries(Collection<? extends Entry> entries, boolean noCheck) {
        assert noCheck || Children.MUTEX.isWriteAccess();
        final boolean LOG_ENABLED = LOGGER.isLoggable(Level.FINER);
        // current list of nodes
        ChildrenArray holder = array.get();
        if (LOG_ENABLED) {
            LOGGER.finer("setEntries for " + this + " on " + Thread.currentThread()); // NOI18N
            LOGGER.finer("       values: " + entries); // NOI18N
            LOGGER.finer("       holder: " + holder); // NOI18N
            LOGGER.finer("       mustNotifySetEntries: " + mustNotifySetEnties); // NOI18N
        }
        Node[] current = holder == null ? null : holder.nodes();
        if (mustNotifySetEnties) {
            if (holder == null) {
                holder = getArray(null);
            }
            if (current == null) {
                holder.entrySupport = this;
                current = holder.nodes();
            }
            mustNotifySetEnties = false;
        } else if (holder == null || current == null) {
            this.entries = new ArrayList<Entry>(entries);
            if (map != null) {
                map.keySet().retainAll(new HashSet<Entry>(this.entries));
            }
            return;
        }
        checkConsistency();
        // what should be removed
        Set<Entry> toRemove = new LinkedHashSet<Entry>(this.entries);
        Set<Entry> entriesSet = new HashSet<Entry>(entries);
        toRemove.removeAll(entriesSet);
        if (!toRemove.isEmpty()) {
            // notify removing, the set must be ready for
            // callbacks with questions
            updateRemove(current, toRemove);
            current = holder.nodes();
        }
        // change the order of entries, notifies
        // it and again brings children to up-to-date state
        Collection<Info> toAdd = updateOrder(current, entries);
        if (!toAdd.isEmpty()) {
            // toAdd contains Info objects that should bee added
            updateAdd(toAdd, new ArrayList<Entry>(entries));
        }
    }

    private void checkInfo(Info info, Entry entry, Collection<? extends Entry> entries, java.util.Map<Entry, Info> map) {
        if (info == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Error in ").append(getClass().getName()).
                append(" with entry ").append(entry).append(" from among entries:");
            for (Entry e : entries) {
                sb.append("\n  ").append(e).append(" contained: ").append(map.containsKey(e));
            }
            sb.append("\nprobably caused by faulty key implementation. The key hashCode() and equals() methods must behave as for an IMMUTABLE object" + " and the hashCode() must return the same value for equals() keys."); // NOI18N
            sb.append("\nmapping:");
            for (Map.Entry<Entry, Info> ei : map.entrySet()) {
                sb.append("\n  ").append(ei.getKey()).append(" => ").append(ei.getValue());
            }
            throw new IllegalStateException(sb.toString());
        }
    }

    /** Removes the objects from the children.
     */
    private void updateRemove(Node[] current, Set<Entry> toRemove) {
        assert Children.MUTEX.isWriteAccess();
        List<Node> nodes = new LinkedList<Node>();
        ChildrenArray cha = array.get();
        for (Entry en : toRemove) {
            Info info = map.remove(en);
            checkInfo(info, en, new ArrayList<Entry>(), map);
            nodes.addAll(info.nodes(true));
            cha.remove(info);
        }
        // modify the current set of entries
        entries.removeAll(toRemove);
        checkConsistency();
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("Current : " + this.entries);
            LOGGER.finer("Removing: " + toRemove);
        }
        // empty the list of nodes so it has to be recreated again
        if (!nodes.isEmpty()) {
            clearNodes();
            notifyRemove(nodes, current);
        }
    }

    /** Updates the order of entries.
     * @param current current state of nodes
     * @param entries new set of entries
     * @return list of infos that should be added
     */
    private List<Info> updateOrder(Node[] current, Collection<? extends Entry> newEntries) {
        assert Children.MUTEX.isWriteAccess();
        List<Info> toAdd = new LinkedList<Info>();
        // that assignes entries their begining position in the array
        // of nodes
        java.util.Map<Info, Integer> offsets = new HashMap<Info, Integer>();
        {
            int previousPos = 0;
            for (Entry entry : entries) {
                Info info = map.get(entry);
                checkInfo(info, entry, entries, map);
                offsets.put(info, previousPos);
                previousPos += info.length();
            }
        }
        int[] perm = new int[current.length];
        int currentPos = 0;
        int permSize = 0;
        List<Entry> reorderedEntries = null;
        for (Entry entry : newEntries) {
            Info info = map.get(entry);
            if (info == null) {
                // this info has to be added
                info = new Info(entry);
                toAdd.add(info);
            } else {
                int len = info.length();
                if (reorderedEntries == null) {
                    reorderedEntries = new LinkedList<Entry>();
                }
                reorderedEntries.add(entry);
                // already there => test if it should not be reordered
                Integer previousInt = offsets.get(info);
                /*
                if (previousInt == null) {
                System.err.println("Offsets: " + offsets);
                System.err.println("Info: " + info);
                System.err.println("Entry: " + info.entry);
                System.err.println("This entries: " + this.entries);
                System.err.println("Entries: " + entries);
                System.err.println("Map: " + map);
                System.err.println("---------vvvvv");
                System.err.println(debug);
                System.err.println("---------^^^^^");
                }
                 */
                int previousPos = previousInt;
                if (currentPos != previousPos) {
                    for (int i = 0; i < len; i++) {
                        perm[previousPos + i] = 1 + currentPos + i;
                    }
                    permSize += len;
                }
            }
            currentPos += info.length();
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
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("Entries before reordering: " + entries);
                LOGGER.finer("Entries after reordering: " + reorderedEntries);
            }
            // reorderedEntries are not null
            entries = reorderedEntries;
            checkConsistency();
            // notify the permutation to the parent
            clearNodes();
            Node p = children.parent;
            if (p != null) {
                p.fireReorderChange(perm);
            }
        }
        return toAdd;
    }

    /** Updates the state of children by adding given Infos.
     * @param infos list of Info objects to add
     * @param entries the final state of entries that should occur
     */
    private void updateAdd(Collection<Info> infos, List<Entry> entries) {
        assert Children.MUTEX.isWriteAccess();
        List<Node> nodes = new LinkedList<Node>();
        for (Info info : infos) {
            nodes.addAll(info.nodes(false));
            map.put(info.entry, info);
        }
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("Entries before updateAdd(): " + this.entries);
            LOGGER.finer("Entries after updateAdd(): " + entries);
        }
        this.entries = entries;
        checkConsistency();
        if (!nodes.isEmpty()) {
            clearNodes();
            notifyAdd(nodes);
        }
    }

    /** Refreshes content of one entry. Updates the state of children
     * appropriately.
     */
    final void refreshEntry(Entry entry) {
        // current list of nodes
        ChildrenArray holder = array.get();
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("refreshEntry: " + entry + " holder=" + holder);
        }
        if (holder == null) {
            return;
        }
        Node[] current = holder.nodes();
        if (current == null) {
            // the initialization is not finished yet =>
            return;
        }
        checkConsistency();
        Info info = map.get(entry);
        if (info == null) {
            // refresh of entry that is not present =>
            return;
        }
        Collection<Node> oldNodes = info.nodes(false);
        Collection<Node> newNodes = info.entry.nodes(null);
        if (oldNodes.equals(newNodes)) {
            // nodes are the same =>
            return;
        }
        Set<Node> toRemove = new HashSet<Node>(oldNodes);
        toRemove.removeAll(new HashSet<Node>(newNodes));
        if (!toRemove.isEmpty()) {
            // notify removing, the set must be ready for
            // callbacks with questions
            // modifies the list associated with the info
            oldNodes.removeAll(toRemove);
            clearNodes();
            // now everything should be consistent => notify the remove
            notifyRemove(toRemove, current);
            current = holder.nodes();
        }
        List<Node> toAdd = refreshOrder(entry, oldNodes, newNodes);
        info.useNodes(newNodes);
        if (!toAdd.isEmpty()) {
            // modifies the list associated with the info
            clearNodes();
            notifyAdd(toAdd);
        }
    }

    /** Updates the order of nodes after a refresh.
     * @param entry the refreshed entry
     * @param oldNodes nodes that are currently in the list
     * @param newNodes new nodes (defining the order of oldNodes and some more)
     * @return list of infos that should be added
     */
    private List<Node> refreshOrder(Entry entry, Collection<Node> oldNodes, Collection<Node> newNodes) {
        List<Node> toAdd = new LinkedList<Node>();
        Set<Node> oldNodesSet = new HashSet<Node>(oldNodes);
        Set<Node> toProcess = new HashSet<Node>(oldNodesSet);
        Node[] permArray = new Node[oldNodes.size()];
        Iterator<Node> it2 = newNodes.iterator();
        int pos = 0;
        while (it2.hasNext()) {
            Node n = it2.next();
            if (oldNodesSet.remove(n)) {
                // the node is in the old set => test for permuation
                permArray[pos++] = n;
            } else {
                if (!toProcess.contains(n)) {
                    // if the node has not been processed yet
                    toAdd.add(n);
                } else {
                    it2.remove();
                }
            }
        }
        // JST: If you get IllegalArgumentException in following code
        // then it can be cause by wrong synchronization between
        // equals and hashCode methods. First of all check them!
        int[] perm = NodeOp.computePermutation(oldNodes.toArray(new Node[0]), permArray);
        if (perm != null) {
            // apply the permutation
            clearNodes();
            // temporarily change the nodes the entry should use
            findInfo(entry).useNodes(Arrays.asList(permArray));
            Node p = children.parent;
            if (p != null) {
                p.fireReorderChange(perm);
            }
        }
        return toAdd;
    }

    /** Notifies that a set of nodes has been removed from
     * children. It is necessary that the system is already
     * in consistent state, so any callbacks will return
     * valid values.
     *
     * @param nodes list of removed nodes
     * @param current state of nodes
     * @return array of nodes that were deleted
     */
    Node[] notifyRemove(Collection<Node> nodes, Node[] current) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("notifyRemove: " + nodes);
            LOGGER.finer("Current     : " + Arrays.asList(current));
        }
        // during a deserialization it may have parent == null
        Node[] arr = nodes.toArray(new Node[0]);
        if (children.parent != null) {
            // fire change of nodes
            if (children.getEntrySupport() == this) {
                children.parent.fireSubNodesChange(false, arr, current);
            }
            // fire change of parent
            Iterator<Node> it = nodes.iterator();
            while (it.hasNext()) {
                Node n = it.next();
                n.deassignFrom(children);
                n.fireParentNodeChange(children.parent, null);
            }
        }
        children.destroyNodes(arr);
        return arr;
    }

    /** Notifies that a set of nodes has been add to
     * children. It is necessary that the system is already
     * in consistent state, so any callbacks will return
     * valid values.
     *
     * @param nodes list of removed nodes
     */
    void notifyAdd(Collection<Node> nodes) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("notifyAdd: " + nodes);
        }
        // notify about parent change
        for (Node n : nodes) {
            n.assignTo(children, -1);
            n.fireParentNodeChange(null, children.parent);
        }
        Node[] arr = nodes.toArray(new Node[0]);
        Node n = children.parent;
        if (n != null && children.getEntrySupport() == this) {
            n.fireSubNodesChange(true, arr, null);
        }
    }

    /**
     * @return either nodes associated with this children or null if they are not created
     */
    public Node[] testNodes() {
        ChildrenArray arr = array.get();
        if (arr == null) {
            return null;
        }
        try {
            Children.PR.enterReadAccess();
            return arr.nodes();
        } finally {
            Children.PR.exitReadAccess();
        }
    }

    /** Obtains references to array holder. If it does not exist, it is created.
     *
     * @param cannotWorkBetter array of size 1 or null, will contain true, if
     *    the getArray cannot be initialized (we are under read access
     *    and another thread is responsible for initialization, in such case
     *    give up on computation of best result
     */
    private ChildrenArray getArray(boolean[] cannotWorkBetter) {
        final boolean LOG_ENABLED = LOGGER.isLoggable(Level.FINER);
        ChildrenArray arr;
        boolean doInitialize = false;
        synchronized (LOCK) {
            arr = array.get();
            if (arr == null) {
                arr = new ChildrenArray();
                // register the array with the children
                registerChildrenArray(arr, true);
                doInitialize = true;
                initThread = Thread.currentThread();
            }
        }
        if (doInitialize) {
            if (LOG_ENABLED) {
                LOGGER.finer("Initialize " + this + " on " + Thread.currentThread()); // NOI18N
            }
            // this call can cause a lot of callbacks => be prepared
            // to handle them as clean as possible
            try {
                children.callAddNotify();
                if (LOG_ENABLED) {
                    LOGGER.finer("addNotify successfully called for " + this + " on " + Thread.currentThread()); // NOI18N
                }
            } finally {
                boolean notifyLater;
                notifyLater = Children.MUTEX.isReadAccess();
                if (LOG_ENABLED) {
                    LOGGER.finer("notifyAll for " + this + " on " + Thread.currentThread() + "  notifyLater: " + notifyLater); // NOI18N
                }
                // now attach to entrySupport, so when entrySupport == null => we are
                // not fully initialized!!!!
                arr.entrySupport = this;
                inited = true;

                class SetAndNotify implements Runnable {

                    public ChildrenArray toSet;
                    public Children whatSet;

                    public void run() {
                        synchronized (LOCK) {
                            initThread = null;
                            LOCK.notifyAll();
                        }
                        if (LOG_ENABLED) {
                            LOGGER.finer("notifyAll done"); // NOI18N
                        }
                    }
                }
                SetAndNotify setAndNotify = new SetAndNotify();
                setAndNotify.toSet = arr;
                setAndNotify.whatSet = children;
                if (notifyLater) {
                    // the notify to the lock has to be done later than
                    // setKeys is executed, otherwise the result of addNotify
                    // might not be visible to other threads
                    // fix for issue 50308
                    Children.MUTEX.postWriteRequest(setAndNotify);
                } else {
                    setAndNotify.run();
                }
            }
        } else if (initThread != null) {
            // otherwise, if not initialize yet (arr.children) wait
            // for the initialization to finish, but only if we can wait
            if (Children.MUTEX.isReadAccess() || Children.MUTEX.isWriteAccess() || (initThread == Thread.currentThread())) {
                // fail, we are in read access
                if (LOG_ENABLED) {
                    LOGGER.log(Level.FINER, "cannot initialize better " + this + " on " + Thread.currentThread() + " read access: " + Children.MUTEX.isReadAccess() + " write access: " + Children.MUTEX.isWriteAccess() + " initThread: " + initThread);
                }
                if (cannotWorkBetter != null) {
                    cannotWorkBetter[0] = true;
                }
                arr.entrySupport = this;
                return arr;
            }
            // otherwise we can wait
            synchronized (LOCK) {
                while (initThread != null) {
                    if (LOG_ENABLED) {
                        LOGGER.finer("waiting for children for " + this + " on " + Thread.currentThread());
                    }
                    try {
                        LOCK.wait();
                    } catch (InterruptedException ex) {
                    }
                }
            }
            if (LOG_ENABLED) {
                LOGGER.finer(" children are here for " + this + " on " + Thread.currentThread() + " children " + children); // NOI18N
            }
        }
        return arr;
    }

    /** Clears the nodes
     */
    private void clearNodes() {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("  clearNodes()"); // NOI18N
        }
        ChildrenArray arr = array.get();
        if (arr != null) {
            // clear the array
            arr.clear();
        }
    }

    /** Registration of ChildrenArray.
     * @param chArr the associated ChildrenArray
     * @param weak use weak or hard reference
     */
    final void registerChildrenArray(final ChildrenArray chArr, boolean weak) {
        final boolean LOG_ENABLED = LOGGER.isLoggable(Level.FINER);
        if (LOG_ENABLED) {
            LOGGER.finer("registerChildrenArray: " + chArr + " weak: " + weak); // NOI18N
        }
        synchronized (LOCK) {
            if (this.array != null && this.array.get() == chArr && ((ChArrRef) this.array).isWeak() == weak) {
                return;
            }
            this.array = new ChArrRef(chArr, weak);
        }
        if (LOG_ENABLED) {
            LOGGER.finer("pointed by: " + chArr + " to: " + this.array); // NOI18N
        }
    }

    /** Finalized.
     */
    final void finalizedChildrenArray(Reference<ChildrenArray> caller) {
        assert caller.get() == null : "Should be null";
        // usually in removeNotify setKeys is called => better require write access
        try {
            Children.PR.enterWriteAccess();
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.fine("previous array: " + array + " caller: " + caller); // NOI18N
            }
            synchronized (LOCK) {
                if (array == caller && children.getEntrySupport() == this) {
                    // really finalized and not reconstructed
                    mustNotifySetEnties = false;
                    array = EMPTY;
                    inited = false;
                    children.callRemoveNotify();
                    assert array == EMPTY;
                }
            }
        } finally {
            Children.PR.exitWriteAccess();
        }
    }

    @Override
    protected List<Entry> getEntries() {
        return new ArrayList<Entry>(entries);
    }

    /** Information about an entry. Contains number of nodes,
     * position in the array of nodes, etc.
     */
    final class Info extends Object {

        int length;
        final Entry entry;

        public Info(Entry entry) {
            this.entry = entry;
        }

        public Collection<Node> nodes(boolean hasToExist) {
            // forces creation of the array
            assert !hasToExist || array.get() != null : "ChildrenArray is not initialized";
            ChildrenArray arr = getArray(null);
            return arr.nodesFor(this, hasToExist);
        }

        public void useNodes(Collection<Node> nodes) {
            // forces creation of the array
            ChildrenArray arr = getArray(null);
            arr.useNodes(this, nodes);
            // assign all there nodes the new children
            for (Node n : nodes) {
                n.assignTo(EntrySupportDefault.this.children, -1);
                n.fireParentNodeChange(null, children.parent);
            }
        }

        public int length() {
            return length;
        }

        @Override
        public String toString() {
            return "Children.Info[" + entry + ",length=" + length + "]"; // NOI18N
        }
    }

    static class DefaultSnapshot extends AbstractList<Node> {

        private Node[] nodes;
        Object holder;

        public DefaultSnapshot(Node[] nodes, ChildrenArray cha) {
            this.nodes = nodes;
            this.holder = cha;
        }

        public Node get(int index) {
            return nodes != null && index < nodes.length ? nodes[index] : null;
        }

        public int size() {
            return nodes != null ? nodes.length : 0;
        }
    }

    private class ChArrRef extends WeakReference<ChildrenArray> implements Runnable {
        private final ChildrenArray chArr;

        public ChArrRef(ChildrenArray referent, boolean weak) {
            super(referent, Utilities.activeReferenceQueue());
            this.chArr = weak ? null : referent;
        }

        @Override
        public ChildrenArray get() {
            return chArr != null ? chArr : super.get();
        }

        boolean isWeak() {
            return chArr == null;
        }

        @Override
        public void run() {
            finalizedChildrenArray(this);
        }
    }
    
}
