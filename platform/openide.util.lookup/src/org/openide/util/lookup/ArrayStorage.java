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

import org.openide.util.Lookup;



import java.util.*;
import java.util.logging.Level;
import org.openide.util.lookup.AbstractLookup.Pair;


/** ArrayStorage of Pairs from AbstractLookup.
 * @author  Jaroslav Tulach
 */
final class ArrayStorage extends Object
implements AbstractLookup.Storage<ArrayStorage.Transaction> {

    /** list of items */
    private Object content;

    /** linked list of refernces to results */
    private transient AbstractLookup.ReferenceToResult<?> results;

    /** Constructor
     */
    public ArrayStorage() {
        this(/*default threshold*/11);
    }

    /** Constructs new ArrayStorage */
    public ArrayStorage(Integer treshhold) {
        this.content = treshhold;
    }

    /** Adds an item into the tree.
    * @param item to add
    * @return true if the Item has been added for the first time or false if some other
    *    item equal to this one already existed in the lookup
    */
    public boolean add(AbstractLookup.Pair<?> item, Transaction changed) {
        Object[] arr = changed.current;

        if (changed.arr == null) {
            // just simple add of one item
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == null) {
                    arr[i] = item;
                    changed.add(item);

                    return true;
                }

                if (arr[i].equals(item)) {
                    // reassign the item number
                    item.setIndex(null, ((AbstractLookup.Pair) arr[i]).getIndex());

                    // already there, but update it
                    arr[i] = item;

                    return false;
                }
            }

            // cannot happen as the beginTransaction ensured we can finish 
            // correctly
            throw new IllegalStateException(
                "current objects: " + Arrays.toString(changed.current) +
                "\nnew objects: " + Arrays.toString(changed.arr) + 
                "\ncnt: " + changed.cnt
            );
        } else {
            // doing remainAll after that, let Transaction hold the new array
            int newIndex = changed.addPair(item);

            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == null) {
                    changed.add(item);

                    return true;
                }

                if (arr[i].equals(item)) {
                    // already there
                    if (i != newIndex) {
                        // change in index
                        changed.add(item);

                        return false;
                    } else {
                        // no change
                        return false;
                    }
                }
            }

            // if not found in the original array
            changed.add(item);

            return true;
        }
    }

    /** Removes an item.
    */
    public void remove(AbstractLookup.Pair item, Transaction changed) {
        Object[] arr = changed.current;
        if (arr == null) {
            return;
        }

        int found = -1;

        for (int i = 0; i < arr.length;) {
            if (arr[i] == null) {
                // end of task
                return;
            }

            if ((found == -1) && arr[i].equals(item)) {
                // already there
                Pair<?> p = (Pair<?>)arr[i];
                p.setIndex(null, -1);
                changed.add(p);
                found = i;
            }

            i++;

            if (found != -1) {
                if (i < arr.length && !(arr[i] instanceof Integer)) {
                    // moving the array
                    arr[i - 1] = arr[i];
                } else {
                    arr[i - 1] = null;
                }
            }
        }
    }

    /** Removes all items that are not present in the provided collection.
    * @param retain Pair -> AbstractLookup.Info map
    * @param changed set of Classes that has possibly changed
    */
    public void retainAll(Map retain, Transaction changed) {
        Object[] arr = changed.current;

        for (int from = 0; from < arr.length; from++) {
            if (!(arr[from] instanceof AbstractLookup.Pair)) {
                // end of content
                break;
            }

            AbstractLookup.Pair p = (AbstractLookup.Pair) arr[from];

            AbstractLookup.Info info = (AbstractLookup.Info) retain.get(p);

            if (info == null) {
                // was removed

                /*
                if (info != null) {
                if (info.index < arr.length) {
                    newArr[info.index] = p;
                }

                if (p.getIndex() != info.index) {
                    p.setIndex (null, info.index);
                    changed.add (p);
                }
                } else {
                // removed
                 */
                changed.add(p);
            }
        }
    }

    /** Queries for instances of given class.
    * @param clazz the class to check
    * @return enumeration of Item
    * @see #unsorted
    */
    public <T> Enumeration<Pair<T>> lookup(final Class<T> clazz) {
        if (content instanceof Object[]) {
            final Enumeration<Object> all = InheritanceTree.arrayEn((Object[]) content);
            class JustPairs implements Enumeration<Pair<T>> {
                private Pair<T> next;

                @SuppressWarnings("unchecked")
                private Pair<T> findNext() {
                    for (;;) {
                        if (next != null) {
                            return next;
                        }
                        if (!all.hasMoreElements()) {
                            return null;
                        }
                        Object o = all.nextElement();
                        boolean ok;
                        if (o instanceof AbstractLookup.Pair) {
                            ok = (clazz == null) || ((AbstractLookup.Pair<?>) o).instanceOf(clazz);
                        } else {
                            ok = false;
                        }

                        next = ok ? (Pair<T>) o : null;
                    }
                }
                
                public boolean hasMoreElements() {
                    return findNext() != null;
                }

                public Pair<T> nextElement() {
                    Pair<T> r = findNext();
                    if (r == null) {
                        throw new NoSuchElementException();
                    }
                    next = null;
                    return r;
                }
            } // end of JustPairs
            return new JustPairs();
        } else {
            return InheritanceTree.emptyEn();
        }
    }
    
    @Override
    public <T> Lookup.Result<T> findResult(Lookup.Template<T> t) {
        AbstractLookup.ReferenceIterator it = new AbstractLookup.ReferenceIterator(this.results);
        while (it.next()) {
            if (it.current().template.equals(t)) {
                return (Lookup.Result<T>) it.current().getResult();
            }
        }
        return null;
    }

    /** Associates another result with this storage.
     */
    public AbstractLookup.ReferenceToResult registerReferenceToResult(AbstractLookup.ReferenceToResult<?> newRef) {
        AbstractLookup.ReferenceToResult prev = this.results;
        this.results = newRef;
        return prev;
    }

    /** Cleanup the references
     */
    @Override
    public AbstractLookup.ReferenceToResult cleanUpResult(Lookup.Template<?> templ) {
        long now = System.currentTimeMillis();
        AbstractLookup.ReferenceIterator it = new AbstractLookup.ReferenceIterator(this.results);

        int cnt = 0;
        while (it.next()) {
            cnt++;
        }
        
        long took = System.currentTimeMillis() - now;
        if (took > 500 && AbstractLookup.LOG != null) {
            AbstractLookup.LOG.log(Level.WARNING, 
                "Too long ({0} ms and {1} references) cleanUpResult for {2}",
                new Object[]{took, cnt, templ != null ? templ.getType() : null}
            );
        }

        return this.results = it.first();
    }

    /** We use a hash set of all modified Pair to handle the transaction */
    public Transaction beginTransaction(int ensure) {
        return new Transaction(ensure, content);
    }

    /** Extract all results.
     */
    public void endTransaction(Transaction changed, Set<AbstractLookup.R> modified) {
        AbstractLookup.ReferenceIterator it = new AbstractLookup.ReferenceIterator(this.results);

        if (changed.arr == null) {
            // either add or remove, only check the content of check HashSet
            while (it.next()) {
                AbstractLookup.ReferenceToResult ref = it.current();
                Iterator<Pair<?>> pairs = changed.iterator();

                while (pairs.hasNext()) {
                    AbstractLookup.Pair p = (AbstractLookup.Pair) pairs.next();

                    if (AbstractLookup.matches(ref.template, p, true)) {
                        modified.add(ref.getResult());
                    }
                }
            }
        } else {
            // do full check of changes
            while (it.next()) {
                AbstractLookup.ReferenceToResult ref = it.current();

                int oldIndex = -1;
                int newIndex = -1;

                for (;;) {
                    oldIndex = findMatching(ref.template, changed.current, oldIndex);
                    newIndex = findMatching(ref.template, changed.arr, newIndex);

                    if ((oldIndex == -1) && (newIndex == -1)) {
                        break;
                    }

                    if (
                        (oldIndex == -1) || (newIndex == -1) ||
                            !changed.current[oldIndex].equals(changed.arr[newIndex])
                    ) {
                        modified.add(ref.getResult());

                        break;
                    }
                }
            }
        }

        this.results = it.first();
        this.content = changed.newContent(this.content);
    }

    private static int findMatching(Lookup.Template t, Object[] arr, int from) {
        while (++from < arr.length) {
            if (arr[from] instanceof AbstractLookup.Pair) {
                if (AbstractLookup.matches(t, (AbstractLookup.Pair) arr[from], true)) {
                    return from;
                }
            }
        }

        return -1;
    }

    /** HashSet with additional field for new array which is callocated
     * in case we are doing replace to hold all new items.
     */
    static final class Transaction extends HashSet<Pair<?>> {
        /** array with current objects */
        public final Object[] current;

        /** array with new objects */
        public final Object[] arr;

        /** number of objects in the array */
        private int cnt;

        public Transaction(int ensure, Object currentContent) {
            Integer trashold;
            Object[] _arr;

            if (currentContent instanceof Integer) {
                trashold = (Integer) currentContent;
                _arr = null;
            } else {
                _arr = (Object[]) currentContent;

                if (_arr[_arr.length - 1] instanceof Integer) {
                    trashold = (Integer) _arr[_arr.length - 1];
                } else {
                    // nowhere to grow we have reached the limit
                    trashold = null;
                }
            }

            int maxSize = (trashold == null) ? _arr.length : trashold.intValue();

            if (ensure > maxSize) {
                throw new UnsupportedOperationException();
            }

            if (ensure == -1) {
                // remove => it is ok
                this.current = currentContent instanceof Integer ? null : (Object[]) currentContent;
                this.arr = null;

                return;
            }

            if (ensure == -2) {
                // adding one
                if (_arr == null) {
                    // first time add, let's allocate the array
                    _arr = new Object[2];
                    _arr[1] = trashold;
                } else {
                    if (_arr[_arr.length - 1] instanceof AbstractLookup.Pair) {
                        // we are full
                        throw new UnsupportedOperationException();
                    } else {
                        // ensure we have allocated enough space
                        if (_arr.length < 2 || _arr[_arr.length - 2] != null) {
                            // double the array
                            int newSize = (_arr.length - 1) * 2;
                            
                            if (newSize <= 1) {
                                newSize = 2;
                            }

                            if (newSize > maxSize) {
                                newSize = maxSize;

                                if (newSize <= _arr.length) {
                                    // no space to get in
                                    throw new UnsupportedOperationException();
                                }

                                _arr = new Object[newSize];
                            } else {
                                // still a lot of space
                                _arr = new Object[newSize + 1];
                                _arr[newSize] = trashold;
                            }

                            // copy content of original array without the last Integer into 
                            // the new one
                            System.arraycopy(currentContent, 0, _arr, 0, ((Object[]) currentContent).length - 1);
                        }
                    }
                }

                this.current = _arr;
                this.arr = null;
            } else {
                // allocate array for complete replacement
                if (ensure == maxSize) {
                    this.arr = new Object[ensure];
                } else {
                    this.arr = new Object[ensure + 1];
                    this.arr[ensure] = trashold;
                }

                this.current = (currentContent instanceof Object[]) ? (Object[]) currentContent : new Object[0];
            }
        }

        public int addPair(AbstractLookup.Pair<?> p) {
            p.setIndex(null, cnt);
            arr[cnt++] = p;

            return p.getIndex();
        }

        public Object newContent(Object prev) {
            if (arr == null) {
                if (current == null) {
                    return prev;
                } else {
                    return current;
                }
            } else {
                return arr;
            }
        }
    }
     // end of Transaction
}
