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
package org.openide.util.lookup;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.implspi.ActiveQueue;


/** Implementation of the lookup from OpenAPIs that is based on the
 * introduction of Item. This class should provide the default way
 * of how to store (Class, Object) pairs in the lookups. It offers
 * protected methods for subclasses to register the pairs.
 * <p>Serializable since 3.27.
 * @author  Jaroslav Tulach
 * @since 1.9
 */
public class AbstractLookup extends Lookup implements Serializable {
    static final Logger LOG = Logger.getLogger(AbstractLookup.class.getName());
    static final long serialVersionUID = 5L;

    /** lock for initialization of the maps of lookups */
    private static final Object treeLock = new Object();

    /** the tree that registers all items (or Integer as a treshold size) */
    private Object tree;

    /** count of items in to lookup */
    private int count;

    /** Constructor to create this lookup and associate it with given
     * Content. The content than allows the creator to invoke protected
     * methods which are not accessible for any other user of the lookup.
     *
     * @param content the content to assciate with
     *
     * @since 1.25
     */
    public AbstractLookup(Content content) {
        content.attach(this);
    }

    /** Constructor for testing purposes that allows specification of storage
     * as mechanism as well.
     */
    AbstractLookup(Content content, Storage<?> storage) {
        this(content);
        this.tree = storage;
        initialize();
    }

    /** Constructor for testing purposes that allows specification of storage
     * as mechanism as well.
     * @param trashhold number of Pair to "remain small"
     */
    AbstractLookup(Content content, Integer trashhold) {
        this(content);
        this.tree = trashhold;
    }

    /** Default constructor for subclasses that do not need to provide a content
     */
    protected AbstractLookup() {
    }

    @Override
    public String toString() {
        if (tree instanceof Storage) {
            return "AbstractLookup" + lookup(new Lookup.Template<Object>(Object.class)).allItems(); // NOI18N
        } else {
            return super.toString();
        }
    }

    /** Entres the storage management system.
     */
    @SuppressWarnings("unchecked")
    private <T> AbstractLookup.Storage<T> enterStorage() {
        for (;;) {
            synchronized (treeLock) {
                if (tree instanceof AbstractLookup.Storage) {
                    if (tree instanceof DelegatingStorage) {
                        // somebody is using the lookup right now
                        DelegatingStorage del = (DelegatingStorage) tree;

                        // check whether there is not access from the same 
                        // thread (can throw exception)
                        del.checkForTreeModification();

                        try {
                            treeLock.wait();
                        } catch (InterruptedException ex) {
                            // ignore and go on
                        }

                        continue;
                    } else {
                        // ok, tree is initialized and nobody is using it yet
                        tree = new DelegatingStorage((Storage<T>) tree);

                        return (Storage<T>) tree;
                    }
                }

                // first time initialization of the tree
                if (tree instanceof Integer) {
                    tree = new ArrayStorage((Integer) tree);
                } else {
                    tree = new ArrayStorage();
                }
            }

            // the tree has not yet been initilized, initialize and go on again
            initialize();
        }
    }

    /** Exists tree ownership.
     */
    private AbstractLookup.Storage exitStorage() {
        synchronized (treeLock) {
            AbstractLookup.Storage stor = ((DelegatingStorage) tree).exitDelegate();
            tree = stor;
            treeLock.notifyAll();

            return stor;
        }
    }

    /** Method for subclasses to initialize them selves.
     */
    protected void initialize() {
    }

    /** Notifies subclasses that a query is about to be processed.
     * @param template the template
     */
    protected void beforeLookup(Template<?> template) {
    }

    /** currently a hook for MetaInfServicesLookup to initialize itself
     * before the result is created. Prevents unwanted change events delivered
     * later. Possible candidate for a public API, if needed by other lookups.
     */
    void beforeLookupResult(Template<?> template) {
    }
    
    /** The method to add instance to the lookup with.
     * @param pair class/instance pair
     */
    protected final void addPair(Pair<?> pair) {
        addPairImpl(pair, null);
    }

    /** The method to add instance to the lookup with.
     * @param pair class/instance pair
     * @param notifyIn the executor that will handle the notification of events
     * @since 7.16
     */
    protected final void addPair(Pair<?> pair, Executor notifyIn) {
        addPairImpl(pair, notifyIn);
    }

    private final <Transaction> void addPairImpl(Pair<?> pair, Executor notifyIn) {
        HashSet<R> toNotify = new HashSet<R>();

        AbstractLookup.Storage<Transaction> t = enterStorage();
        Transaction transaction = null;

        try {
            transaction = t.beginTransaction(-2);

            if (t.add(pair, transaction)) {
                try {
                    pair.setIndex(t, count++);
                } catch (IllegalStateException ex) {
                    // remove the pair
                    t.remove(pair, transaction);

                    // rethrow the exception
                    throw ex;
                }

                // if the pair is newly added and was not there before
                t.endTransaction(transaction, toNotify);
            } else {
                // just finish the process by calling endTransaction
                t.endTransaction(transaction, new HashSet<R>());
            }
        } finally {
            exitStorage();
        }

        notifyIn(notifyIn, toNotify);
    }

    /** Remove instance.
     * @param pair class/instance pair
     */
    protected final void removePair(Pair<?> pair) {
        removePairImpl(pair, null);
    }
    /** Remove instance.
     * @param pair class/instance pair
     * @param notifyIn the executor that will handle the notification of events
     * @since 7.16
     */
    protected final void removePair(Pair<?> pair, Executor notifyIn) {
        removePairImpl(pair, notifyIn);
    }

    private <Transaction> void removePairImpl(Pair<?> pair, Executor notifyIn) {
        HashSet<R> toNotify = new HashSet<R>();

        AbstractLookup.Storage<Transaction> t = enterStorage();
        Transaction transaction = null;

        try {
            transaction = t.beginTransaction(-1);
            t.remove(pair, transaction);
            t.endTransaction(transaction, toNotify);
        } finally {
            exitStorage();
        }

        notifyIn(notifyIn, toNotify);
    }

    /** Changes all pairs in the lookup to new values.
     * @param collection the collection of (Pair) objects
     */
    protected final void setPairs(Collection<? extends Pair> collection) {
        setPairs(collection, null);
    }

    /** Changes all pairs in the lookup to new values, notifies listeners
     * using provided executor.
     * 
     * @param collection the collection of (Pair) objects
     * @param notifyIn the executor that will handle the notification of events
     * @since 7.16
     */
    protected final void setPairs(Collection<? extends Pair> collection, Executor notifyIn) {
        HashSet<R> listeners = setPairsAndCollectListeners(collection);
        notifyIn(notifyIn, listeners);
    }
    
    final void notifyIn(Executor notifyIn, final HashSet<R> listeners) {
        NotifyListeners notify = new NotifyListeners(listeners);
        if (notify.shallRun()) {
            if (notifyIn == null) {
                notify.run();
            } else {
                notifyIn.execute(notify);
            }
        }
    }
    
    /** Getter for set of pairs. Package private contract with MetaInfServicesLookup.
     * @return a LinkedHashSet that can be modified
     */
    final LinkedHashSet<Pair<?>> getPairsAsLHS() {
        AbstractLookup.Storage<?> t = enterStorage();

        try {
            Enumeration<Pair<Object>> en = t.lookup(Object.class);
            TreeSet<Pair<?>> arr = new TreeSet<Pair<?>>(ALPairComparator.DEFAULT);
            while (en.hasMoreElements()) {
                Pair<Object> item = en.nextElement();
                arr.add(item);
            }
            return new LinkedHashSet<Pair<?>>(arr);
        } finally {
            exitStorage();
        }
    }

    /** Collects listeners without notification. Needed in MetaInfServicesLookup
     * right now, but maybe will become an API later.
     */
    final <Transaction> HashSet<R> setPairsAndCollectListeners(Collection<? extends Pair> collection) {
        HashSet<R> toNotify = new HashSet<R>(27);

        AbstractLookup.Storage<Transaction> t = enterStorage();
        Transaction transaction = null;

        try {
            // map between the Items and their indexes (Integer)
            HashMap<Item<?>,Info> shouldBeThere = new HashMap<Item<?>,Info>(collection.size() * 2);

            count = 0;

            Iterator it = collection.iterator();
            transaction = t.beginTransaction(collection.size());

            while (it.hasNext()) {
                Pair item = (Pair) it.next();

                if (t.add(item, transaction)) {
                    // the item has not been there yet
                    //t.endTransaction(transaction, toNotify);
                }

                // remeber the item, because it should not be removed
                shouldBeThere.put(item, new Info(count++, transaction));

                //                    arr.clear ();
            }

            //            Object transaction = t.beginTransaction ();
            // deletes all objects that should not be there and
            t.retainAll(shouldBeThere, transaction);

            // collect listeners
            t.endTransaction(transaction, toNotify);

            /*
            // check consistency
            Enumeration en = t.lookup (java.lang.Object.class);
            boolean[] max = new boolean[count];
            int mistake = -1;
            while (en.hasMoreElements ()) {
                Pair item = (Pair)en.nextElement ();

                if (max[item.index]) {
                    mistake = item.index;
                }
                max[item.index] = true;
            }

            if (mistake != -1) {
                System.err.println ("Mistake at: " + mistake);
                tree.print (System.err, true);
            }
            */
        } finally {
            exitStorage();
        }

        return toNotify;
    }

    private final void writeObject(ObjectOutputStream oos)
    throws IOException {
        AbstractLookup.Storage s = enterStorage();

        try {
            // #36830: Serializing only InheritanceTree no ArrayStorage
            s.beginTransaction(Integer.MAX_VALUE);

            // #32040: don't write half-made changes
            oos.defaultWriteObject();
        } finally {
            exitStorage();
        }
    }

    public final <T> T lookup(Class<T> clazz) {
        Lookup.Item<T> item = lookupItem(new Lookup.Template<T>(clazz));
        return (item == null) ? null : item.getInstance();
    }

    @Override
    public final <T> Lookup.Item<T> lookupItem(Lookup.Template<T> template) {
        AbstractLookup.this.beforeLookup(template);

        ArrayList<Pair<T>> list = null;
        AbstractLookup.Storage<?> t = enterStorage();

        try {
            Enumeration<Pair<T>> en;

            try {
                en = t.lookup(template.getType());

                return findSmallest(en, template, false);
            } catch (AbstractLookup.ISE ex) {
                // not possible to enumerate the exception, ok, copy it 
                // to create new
                list = new ArrayList<Pair<T>>();
                en = t.lookup(null); // this should get all the items without any checks

                // the checks will be done out side of the storage
                while (en.hasMoreElements()) {
                    list.add(en.nextElement());
                }
            }
        } finally {
            exitStorage();
        }

        return findSmallest(Collections.enumeration(list), template, true);
    }

    private static <T> Pair<T> findSmallest(Enumeration<Pair<T>> en, Lookup.Template<T> template, boolean deepCheck) {
        int smallest = InheritanceTree.unsorted(en) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        Pair<T> res = null;

        while (en.hasMoreElements()) {
            Pair<T> item = en.nextElement();

            if (matches(template, item, deepCheck)) {
                if (smallest == Integer.MIN_VALUE) {
                    // ok, sorted enumeration the first that matches is fine
                    return item;
                } else {
                    // check for the smallest item
                    if (smallest > item.getIndex()) {
                        smallest = item.getIndex();
                        res = item;
                    }
                }
            }
        }

        return res;
    }

    @Override
    public final <T> Lookup.Result<T> lookup(Lookup.Template<T> template) {
        beforeLookupResult(template);
        for (;;) {
            AbstractLookup.ISE toRun = null;

            AbstractLookup.Storage<?> t = enterStorage();

            try {
                Lookup.Result<T> prev = t.findResult(template);
                if (prev != null) {
                    return prev;
                }
                
                R<T> r = new R<T>();
                ReferenceToResult<T> newRef = new ReferenceToResult<T>(r, this, template);
                newRef.next = t.registerReferenceToResult(newRef);

                return r;
            } catch (AbstractLookup.ISE ex) {
                toRun = ex;
            } finally {
                exitStorage();
            }

            toRun.recover(this);

            // and try again
        }
    }

    /** Notifies listeners.
     * @param allAffectedResults set of R
     */
    static final class NotifyListeners implements Runnable {
        private final ArrayList<Object> evAndListeners;
        
        public NotifyListeners(Set<R> allAffectedResults) {
            if (allAffectedResults.isEmpty()) {
                evAndListeners = null;
                return;
            }

            evAndListeners = new ArrayList<Object>();
            {
                for (R<?> result : allAffectedResults) {
                    result.collectFires(evAndListeners);
                }
            }
        }

        public boolean shallRun() {
            return evAndListeners != null && !evAndListeners.isEmpty();
        }

        public void run() {
            Iterator it = evAndListeners.iterator();
            while (it.hasNext()) {
                LookupEvent ev = (LookupEvent)it.next();
                LookupListener l = (LookupListener)it.next();
                try {
                    l.resultChanged(ev);
                } catch (RuntimeException x) {
                    LOG.log(Level.WARNING, null, x);
                }
            }
        }
    }

    /**
     * Call resultChanged on all listeners.
     * @param listeners array of listeners
     * @param ev the event to fire
     */
    static void notifyListeners(Object[] listeners, LookupEvent ev, Collection<Object> evAndListeners) {
        for (int i = listeners.length - 1; i >= 0; i--) {
            if (! (listeners[i] instanceof LookupListener)) {
                continue;
            }
            LookupListener ll = (LookupListener)listeners[i];

            try {
                if (evAndListeners != null) {
                    if (ll instanceof WaitableResult) {
                        WaitableResult<?> wr = (WaitableResult<?>)ll;
                        wr.collectFires(evAndListeners);
                    } else {
                        evAndListeners.add(ev);
                        evAndListeners.add(ll);
                    }
                } else {
                    ll.resultChanged(ev);
                }
            } catch (CycleError err) {
                err.add(evAndListeners);
                throw err;
            } catch (StackOverflowError err) {
                CycleError cycle = new CycleError(err.getMessage());
                cycle.add(evAndListeners);
                throw cycle;
            } catch (ISE ex) {
                throw ex;
            } catch (RuntimeException e) {
                // Such as e.g. occurred in #32040. Do not halt other things.
                e.printStackTrace();
            }
        }
    }

    private static class CycleError extends StackOverflowError {
        private final Set<Collection<Object>> print = new HashSet<Collection<Object>>();

        public CycleError(String s) {
            super(s);
        }
        
        public void add(Collection<Object> evAndListeners) {
            this.print.add(evAndListeners);
        }

        @Override
        public String getMessage() {
            StringBuilder sb = new StringBuilder();
            sb.append("StackOverflowError. There is ").append(print.size()).append(" listeners:"); // NOI18N
            int round = 0;
            for (Collection<Object> list : print) {
                sb.append("\nRound ").append(++round).append("\n"); // NOI18N
                if (list != null) {
                    for (Object o : list) {
                        sb.append("\n  ").append(o); // NOI18N
                        if (sb.length() > 10000) {
                            break;
                        }
                    }
                } else {
                    sb.append("listeners are null"); // NOI18N
                }
            }
            return sb.toString();
        }
    } // end of CycleError

    /** A method that defines matching between Item and Template.
     * @param t template providing the criteria
     * @param item the item to match
     * @param deepCheck true if type of the pair should be tested, false if it is already has been tested
     * @return true if item matches the template requirements, false if not
     */
    static boolean matches(Template<?> t, Pair<?> item, boolean deepCheck) {
        String id = t.getId();

        if (id != null && !id.equals(item.getId())) {
            return false;
        }

        Object instance = t.getInstance();

        if ((instance != null) && !item.creatorOf(instance)) {
            return false;
        }

        if (deepCheck) {
            return item.instanceOf(t.getType());
        } else {
            return true;
        }
    }

    /**
     * Compares the array elements for equality.
     * @return true if all elements in the arrays are equal
     *  (by calling equals(Object x) method)
     */
    private static boolean compareArrays(Object[] a, Object[] b) {
        // handle null values
        if (a == null) {
            return (b == null);
        } else {
            if (b == null) {
                return false;
            }
        }

        if (a.length != b.length) {
            return false;
        }

        for (int i = 0; i < a.length; i++) {
            // handle null values for individual elements
            if (a[i] == null) {
                if (b[i] != null) {
                    return false;
                }

                // both are null --> ok, take next
                continue;
            } else {
                if (b[i] == null) {
                    return false;
                }
            }

            // perform the comparison
            if (!a[i].equals(b[i])) {
                return false;
            }
        }

        return true;
    }

    /** Method to be called when a result is cleared to signal that the list
     * of all result should be checked for clearing.
     * @param template the template the result was for
     * @return true if the hash map with all items has been cleared
     */
    <T> boolean cleanUpResult(Lookup.Template<T> template) {
        AbstractLookup.Storage<?> t = enterStorage();

        try {
            return t.cleanUpResult(template) == null;
        } finally {
            exitStorage();
            Thread.yield();
        }
    }

    /** Storage check for tests. */
    static boolean isSimple(AbstractLookup l) {
        return DelegatingStorage.isSimple((Storage)l.tree);
    }

    /** Generic support for listeners, so it can be used in other results
     * as well.
     * @param add true to add it, false to modify
     * @param l listener to modify
     * @param ref the value of the reference to listener or listener list
     * @return new value to the reference to listener or list
     */
    @SuppressWarnings("unchecked")
    static Object modifyListenerList(boolean add, LookupListener l, Object ref) {
        if (add) {
            if (ref == null) {
                return l;
            }

            if (ref instanceof LookupListener) {
                ArrayList arr = new ArrayList();
                arr.add(ref);
                ref = arr;
            }

            ((ArrayList) ref).add(l);

            return ref;
        } else {
            // remove
            if (ref == null) {
                return null;
            }

            if (ref == l) {
                return null;
            }
            
            if (!(ref instanceof ArrayList)) {
                return ref;
            }
            
            ArrayList arr = (ArrayList) ref;
            arr.remove(l);

            if (arr.size() == 1) {
                return arr.iterator().next();
            } else {
                return arr;
            }
        }
    }

    private static ReferenceQueue<Object> activeQueue() {
        return ActiveQueue.queue();
    }

    /** Storage to keep the internal structure of Pairs and to answer
     * different queries.
     */
    interface Storage<Transaction> {
        /** Initializes a modification operation by creating an object
         * that will be passsed to all add, remove, retainAll methods
         * and should collect enough information about the change to
         * notify listeners about the transaction later
         *
         * @param ensure the amount of items that will appear in the storage
         *   after the modifications (-1 == remove one, -2 == add one, >= 0
         *   the amount of objects at the end
         * @return a token to identify the transaction
         */
        public Transaction beginTransaction(int ensure);

        /** Collects all affected results R that were modified in the
         * given transaction.
         *
         * @param transaction the transaction indentification
         * @param modifiedResults place to add results R to
         */
        public void endTransaction(Transaction transaction, Set<R> modifiedResults);

        /** Adds an item into the storage.
        * @param item to add
        * @param transaction transaction token
        * @return true if the Item has been added for the first time or false if some other
        *    item equal to this one already existed in the lookup
        */
        public boolean add(AbstractLookup.Pair<?> item, Transaction transaction);

        /** Removes an item.
        */
        public void remove(AbstractLookup.Pair item, Transaction transaction);

        /** Removes all items that are not present in the provided collection.
        * @param retain collection of Pairs to keep them in
        * @param transaction the transaction context
        */
        public void retainAll(Map retain, Transaction transaction);

        /** Queries for instances of given class.
        * @param clazz the class to check
        * @return enumeration of Item
        * @see #unsorted
        */
        public <T> Enumeration<Pair<T>> lookup(Class<T> clazz);

        /** Registers another reference to a result with the storage. This method
         * has also a special meaning.
         *
         * @param newRef the new reference to remember
         * @return the previous reference that was kept (null if newRef is the first one)
         *    the applications is expected to link from newRef to this returned
         *    value to form a linked list
         */
        public ReferenceToResult<?> registerReferenceToResult(ReferenceToResult<?> newRef);

        /** Given the provided template, Do cleanup the results.
         * @param templ template of a result(s) that should be checked
         * @return null if all references for this template were cleared or one of them
         */
        public ReferenceToResult<?> cleanUpResult(Lookup.Template<?> templ);

        public <T> Result<T> findResult(Template<T> template);
    }

    /** Extension to the default lookup item that offers additional information
     * for the data structures use in AbstractLookup
     */
    public abstract static class Pair<T> extends Lookup.Item<T> implements Serializable {
        private static final long serialVersionUID = 1L;

        /** possition of this item in the lookup, manipulated in addPair, removePair, setPairs methods */
        private int index = -1;

        /** For use by subclasses. */
        protected Pair() {
        }

        final int getIndex() {
            return index;
        }

        final void setIndex(AbstractLookup.Storage<?> tree, int x) {
            if (tree == null) {
                this.index = x;

                return;
            }

            if (this.index == -1) {
                this.index = x;
            } else {
                throw new IllegalStateException("You cannot use " + this + " in more than one AbstractLookup. Prev: " + this.index + " new: " + x); // NOI18N
            }
        }

        /** Tests whether this item can produce object
        * of class c.
        * <p>Typically this will produce the same result as
        * {@code c.isAssignableFrom(}{@link #getType() getType}{@code ())}
        * but may avoid loading the concrete type's class in doing so.
        * 
        * @param c class to check against
        * @return true if this item can produce object of class c
        */
        protected abstract boolean instanceOf(Class<?> c);

        /** Method that can test whether an instance of a class has been created
         * by this item.
         *
         * @param obj the instance
         * @return if the item has already create an instance and it is the same
         *   as obj.
         */
        protected abstract boolean creatorOf(Object obj);
    }

    /** Result based on one instance returned.
     */
    static final class R<T> extends WaitableResult<T> {
        /** reference our result is attached to (do not modify) */
        public ReferenceToResult<T> reference;

        /** listeners on the results or pointer to one listener */
        private Object listeners;

        public R() {
        }

        /** Checks whether we have simple behaviour of complex.
         */
        private boolean isSimple() {
            Storage s = (Storage) reference.lookup.tree;

            return DelegatingStorage.isSimple(s);
        }

        //
        // Handling cache management for both cases, no caches
        // for simple (but mark that we needed them, so refresh can
        // be done in cloneList) and complex when all 3 types
        // of result are cached
        //
        private Object getFromCache(int indx) {
            if (isSimple()) {
                return null;
            }

            Object maybeArray = reference.caches;

            if (maybeArray instanceof Object[]) {
                return ((Object[]) maybeArray)[indx];
            }

            return null;
        }

        @SuppressWarnings("unchecked")
        private Set<Class<? extends T>> getClassesCache() {
            return (Set<Class<? extends T>>) getFromCache(0);
        }

        private void setClassesCache(Set s) {
            if (isSimple()) {
                // mark it as being used
                reference.caches = reference;

                return;
            }

            setReferences(0, s);
        }

        @SuppressWarnings("unchecked")
        private Collection<T> getInstancesCache() {
            return (Collection<T>) getFromCache(1);
        }

        private void setInstancesCache(Collection c) {
            if (isSimple()) {
                // mark it as being used
                reference.caches = reference;

                return;
            }

            setReferences(1, c);
        }

        @SuppressWarnings("unchecked")
        private Pair<T>[] getItemsCache() {
            return (Pair<T>[]) getFromCache(2);
        }

        private void setItemsCache(Collection<?> c) {
            if (isSimple()) {
                // mark it as being used
                reference.caches = reference;

                return;
            }

            setReferences(2, c.toArray(new Pair[0]));
        }
        
        private void setReferences(int index, Object value) {
            Object obj = reference.caches;
            Object[] arr;
            if (obj instanceof Object[]) {
                arr = (Object[])obj;
            } else {
                reference.caches = arr = new Object[3];
            }
            arr[index] = value;
        }

        private void clearCaches() {
            if (reference.caches instanceof Object[]) {
                reference.caches = new Object[3];
            }
        }

        /** Ok, register listeners to all classes and super classes.
         */
        public synchronized void addLookupListener(LookupListener l) {
            listeners = modifyListenerList(true, l, listeners);
        }

        /** Ok, register listeners to all classes and super classes.
         */
        public synchronized void removeLookupListener(LookupListener l) {
            listeners = modifyListenerList(false, l, listeners);
        }

        /** Delete all cached values, the template changed.
         */
        protected  void collectFires(Collection<Object> evAndListeners) {
            Object[] previousItems = getItemsCache();
            clearCaches();
            
            if (previousItems != null) {
                Object[] newArray = allItemsWithoutBeforeLookup().toArray();

                if (compareArrays(previousItems, newArray)) {
                    // do not fire any change if nothing has been changed
                    return;
                }
            }

            LookupListener[] arr;

            synchronized (this) {
                if (listeners == null) {
                    return;
                }

                if (listeners instanceof LookupListener) {
                    arr = new LookupListener[] { (LookupListener) listeners };
                } else {
                    ArrayList<?> l = (ArrayList<?>) listeners;
                    arr = l.toArray(new LookupListener[0]);
                }
            }

            final LookupListener[] ll = arr;
            final LookupEvent ev = new LookupEvent(this);
            notifyListeners(ll, ev, evAndListeners);
        }

        public Collection<T> allInstances() {
            return allInstances(true);
        }
        protected Collection<T> allInstances(boolean callBeforeLookup) {
            if (callBeforeLookup) {
                reference.lookup.beforeLookup(reference.template);
            }

            Collection<T> s = getInstancesCache();

            if (s != null) {
                return s;
            }

            Collection<Pair<T>> items = allItemsWithoutBeforeLookup();
            ArrayList<T> list = new ArrayList<T>(items.size());

            Iterator<Pair<T>> it = items.iterator();

            while (it.hasNext()) {
                Pair<T> item = it.next();
                T obj = item.getInstance();

                if (reference.template.getType().isInstance(obj)) {
                    list.add(obj);
                }
            }
            
            s = Collections.unmodifiableList(list);
            setInstancesCache(s);

            return s;
        }

        /** Set of all classes.
         *
         */
        @Override
        public Set<Class<? extends T>> allClasses() {
            reference.lookup.beforeLookup(reference.template);

            Set<Class<? extends T>> s = getClassesCache();

            if (s != null) {
                return s;
            }

            s = new HashSet<Class<? extends T>>();

            for (Pair<T> item : allItemsWithoutBeforeLookup()) {
                Class<? extends T> clazz = item.getType();

                if (clazz != null) {
                    s.add(clazz);
                }
            }

            s = Collections.unmodifiableSet(s);
            setClassesCache(s);

            return s;
        }

        /** Items are stored directly in the allItems.
         */
        @Override
        public Collection<? extends Item<T>> allItems() {
            return allItems(true);
        }
        @Override
        protected Collection<? extends Item<T>> allItems(boolean callBeforeLookup) {
            if (callBeforeLookup) {
                reference.lookup.beforeLookup(reference.template);
            }
            return allItemsWithoutBeforeLookup();
        }

        /** Implements the search for allItems, but without asking for before lookup */
        private Collection<Pair<T>> allItemsWithoutBeforeLookup() {
            Pair<T>[] c = getItemsCache();

            if (c != null) {
                return Collections.unmodifiableList(Arrays.asList(c));
            }

            ArrayList<Pair<Object>> saferCheck = null;
            AbstractLookup.Storage<?> t = reference.lookup.enterStorage();

            try {
                try {
                    return Collections.unmodifiableCollection(initItems(t));
                } catch (AbstractLookup.ISE ex) {
                    // do less effective evaluation of items outside of the 
                    // locked storage
                    saferCheck = new ArrayList<Pair<Object>>();

                    Enumeration<Pair<Object>> en = t.lookup(null); // get all Pairs

                    while (en.hasMoreElements()) {
                        Pair<Object> i = en.nextElement();
                        saferCheck.add(i);
                    }
                }
            } finally {
                reference.lookup.exitStorage();
            }
            return extractPairs(saferCheck);
        }

        @SuppressWarnings("unchecked")
        private Collection<Pair<T>> extractPairs(final ArrayList<Pair<Object>> saferCheck) {
            TreeSet<Pair<T>> items = new TreeSet<Pair<T>>(ALPairComparator.DEFAULT);
            for (Pair<Object> i : saferCheck) {
                if (matches(reference.template, i, false)) {
                    items.add((Pair<T>)i);
                }
            }
            return Collections.unmodifiableCollection(items);
        }

        /** Initializes items.
         */
        private Collection<Pair<T>> initItems(Storage<?> t) {
            // manipulation with the tree must be synchronized
            Enumeration<Pair<T>> en = t.lookup(reference.template.getType());

            // InheritanceTree is comparator for AbstractLookup.Pairs
            TreeSet<Pair<T>> items = new TreeSet<Pair<T>>(ALPairComparator.DEFAULT);

            while (en.hasMoreElements()) {
                Pair<T> i = en.nextElement();

                if (matches(reference.template, i, false)) {
                    items.add(i);
                }
            }

            // create a correctly sorted copy using the tree as the comparator
            setItemsCache(items);

            return items;
        }

        /** Used by proxy results to synchronize before lookup.
         */
        protected void beforeLookup(Lookup.Template t) {
            if (t.getType() == reference.template.getType()) {
                reference.lookup.beforeLookup(t);
            }
        }

        /* Do not need to implement it, the default way is ok.
        public boolean equals(java.lang.Object obj) {
            return obj == this;
        }
        */
        @Override
        public String toString() {
            return super.toString() + " for " + reference.template;
        }
    }
     // end of R

    /** A class that can be used by the creator of the 
     * {@link AbstractLookup} to
     * control its content (a kind of 
     * <a href="http://wiki.apidesign.org/wiki/Privileged_API">Privileged API</a>
     * giving creator of the lookup more rights than subsequent users of the lookup).
     * Typical usage: <pre>
     * {@link Content} ic = new {@link InstanceContent#InstanceContent() InstanceContent()};
     * {@link Lookup} lookup = new {@link AbstractLookup#AbstractLookup(org.openide.util.lookup.AbstractLookup.Content) AbstractLookup(ic)};
     * {@link Content#addPair(org.openide.util.lookup.AbstractLookup.Pair) ic.addPair(...)};
     * </pre>
     * @since 1.25
     */
    public static class Content extends Object implements Serializable {
        private static final long serialVersionUID = 1L;

        // one of them is always null (except attach stage)

        /** abstract lookup we are connected to */
        private AbstractLookup al;
        private transient Object notifyIn;
        
        /** Default constructor.
         */
        public Content() {
            this(null);
        }
        
        /** Creates a content associated with an executor to handle dispatch
         * of changes.
         * @param notifyIn the executor to notify changes in
         * @since  7.16
         */
        public Content(Executor notifyIn) {
            this.notifyIn = notifyIn;
        }
        
        /** for testing purposes */
        final void attachExecutor(Executor notifyIn) {
            this.notifyIn = notifyIn;
        }

        /** A lookup attaches to this object.
         */
        final synchronized void attach(AbstractLookup al) {
            if (this.al == null) {
                this.al = al;

                ArrayList<Pair> ep = getEarlyPairs();
                if (ep != null) {
                    notifyIn = null;
                    setPairs(ep);
                }
            } else {
                throw new IllegalStateException(
                    "Trying to use content for " + al + " but it is already used for " + this.al
                ); // NOI18N
            }
        }

        /** The method to add a pair to the associated {@link AbstractLookup}.
         * Preferably call this method when lookup is already associated with
         * this <em>content</em> (association is done by passing this object to some
         * {@link AbstractLookup#AbstractLookup(org.openide.util.lookup.AbstractLookup.Content) AbstractLookup's constructor}
         * once).
         *
         * @param pair class/instance pair
         * @throws NullPointerException may throw NullPointerException if called
         *   before association with {@link AbstractLookup}
         */
        public final void addPair(Pair<?> pair) {
            AbstractLookup a = al;
            Executor e = getExecutor();

            if (a != null || e != null) {
                if (a == null) {
                    throw new NullPointerException(
                        "Adding a pair to Content not connected to Lookup is not supported!" // NOI18N
                    );
                }
                a.addPair(pair, e);
            } else {
                if (notifyIn == null) {
                    notifyIn = new ArrayList<Pair>(3);
                }

                getEarlyPairs().add(pair);
            }
        }

        /** Remove instance.
         * @param pair class/instance pair
         */
        public final void removePair(Pair<?> pair) {
            AbstractLookup a = al;
            Executor e = getExecutor();

            if (a != null || e != null) {
                a.removePair(pair, e);
            } else {
                if (notifyIn == null) {
                    notifyIn = new ArrayList<Pair>(3);
                }

                getEarlyPairs().remove(pair);
            }
        }

        /** Changes all pairs in the lookup to new values.
         * @param c the collection of (Pair) objects
         */
        public final void setPairs(Collection<? extends Pair> c) {
            AbstractLookup a = al;
            Executor e = getExecutor();
            
            if (a != null || e != null) {
                a.setPairs(c, e);
            } else {
                notifyIn = new ArrayList<Pair>(c);
            }
        }

        @SuppressWarnings("unchecked")
        private ArrayList<Pair> getEarlyPairs() {
            Object o = notifyIn;
            return o instanceof ArrayList ? (ArrayList<Pair>)o : null;
        }
        
        private Executor getExecutor() {
            Object o = notifyIn;
            return o instanceof Executor ? (Executor)o : null;
        }
    }
     // end of Content

    /** Just a holder for index & modified values.
     */
    static final class Info extends Object {
        public int index;
        public Object transaction;

        public Info(int i, Object t) {
            index = i;
            transaction = t;
        }
    }

    /** Reference to a result R
     */
    static final class ReferenceToResult<T> extends WeakReference<R<T>> implements Runnable {
        /** next refernece in chain, modified only from AbstractLookup or this */
        private ReferenceToResult<?> next;

        /** the template for the result */
        public final Template<T> template;

        /** the lookup we are attached to */
        public final AbstractLookup lookup;

        /** caches for results */
        public Object caches;

        /** Creates a weak refernece to a new result R in context of lookup
         * for given template
         */
        private ReferenceToResult(R<T> result, AbstractLookup lookup, Template<T> template) {
            super(result, activeQueue());
            this.template = template;
            this.lookup = lookup;
            getResult().reference = this;
        }

        /** Returns the result or null
         */
        R<T> getResult() {
            return get();
        }

        /** Cleans the reference. Implements Runnable interface, do not call
         * directly.
         */
        public void run() {
            lookup.cleanUpResult(this.template);
        }

        /** Clones the reference list to given Storage.
         * @param storage storage to clone to
         */
        public void cloneList(AbstractLookup.Storage<?> storage) {
            ReferenceIterator it = new ReferenceIterator(this);

            while (it.next()) {
                ReferenceToResult<?> current = it.current();
                ReferenceToResult<?> newRef = current.cloneRef();
                newRef.next = storage.registerReferenceToResult(newRef);
                newRef.caches = current.caches;

                if (current.caches == current) {
                    current.getResult().initItems(storage);
                }
            }
        }

        private ReferenceToResult<T> cloneRef() {
            return new ReferenceToResult<T>(getResult(), lookup, template);
        }
    }
     // end of ReferenceToResult

    /** Supporting class to iterate over linked list of ReferenceToResult
     * Use:
     * <PRE>
     *  ReferenceIterator it = new ReferenceIterator (this.ref);
     *  while (it.next ()) {
     *    it.current (): // do some work
     *  }
     *  this.ref = it.first (); // remember the first one
     */
    static final class ReferenceIterator extends Object {
        private ReferenceToResult<?> first;
        private ReferenceToResult<?> current;

        /** hard reference to current result, so it is not GCed meanwhile */
        private R<?> currentResult;

        /** Initializes the iterator with first reference.
         */
        public ReferenceIterator(ReferenceToResult<?> first) {
            this.first = first;
        }

        /** Moves the current to next possition */
        public boolean next() {
            ReferenceToResult<?> prev;
            ReferenceToResult<?> ref;

            if (current == null) {
                ref = first;
                prev = null;
            } else {
                prev = current;
                ref = current.next;
            }

            while (ref != null) {
                R<?> result = ref.get();

                if (result == null) {
                    if (prev == null) {
                        // move the head
                        first = ref.next;
                    } else {
                        // skip over this reference
                        prev.next = ref.next;
                    }

                    prev = ref;
                    ref = ref.next;
                } else {
                    // we have found next item
                    currentResult = result;
                    current = ref;

                    return true;
                }
            }

            currentResult = null;
            current = null;

            return false;
        }

        /** Access to current reference.
         */
        public ReferenceToResult<?> current() {
            return current;
        }

        /** Access to reference that is supposed to be the first one.
         */
        public ReferenceToResult<?> first() {
            return first;
        }
    }

    /** Signals that a lookup is being modified from a lookup query.
     *
     * @author  Jaroslav Tulach
     */
    static final class ISE extends IllegalStateException {
        static final long serialVersionUID = 100L;
        
        /** list of jobs to execute. */
        private java.util.List<Job> jobs;

        /** @param msg message
         */
        public ISE(String msg) {
            super(msg);
        }

        /** Registers a job to be executed partially out and partially in
         * the lock over storage.
         */
        public void registerJob(Job job) {
            if (jobs == null) {
                jobs = new java.util.ArrayList<Job>();
            }

            jobs.add(job);
        }

        /** Executes the jobs outside, and then inside a locked session.
         */
        public void recover(AbstractLookup lookup) {
            if (jobs == null) {
                // no recovery plan, throw itself
                throw this;
            }

            for (Job j : jobs) {
                j.before();
            }

            AbstractLookup.Storage s = lookup.enterStorage();

            try {
                for (Job j : jobs) {
                    j.inside();
                }
            } finally {
                lookup.exitStorage();
            }
        }

        /** A job to be executed partially outside and partially inside
         * the storage lock.
         */
        static interface Job {
            public void before();

            public void inside();
        }
    }
     // end of ISE
}
