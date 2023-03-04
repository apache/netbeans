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

package org.openide.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import org.openide.util.Parameters;

/**
 * Factory methods for various types of {@link Enumeration}.
 * Allows composition of existing enumerations, filtering their contents, and/or modifying them.
 * All of this is designed to be done lazily, i.e. elements created on demand.
 * @since 4.37
 * @author Jaroslav Tulach
 * @see NbCollections#checkedEnumerationByFilter
 * @see NbCollections#iterable(Enumeration)
 */
public final class Enumerations extends Object {
    /** No instances */
    private Enumerations() {
    }

    /**
     * An empty enumeration.
     * Always returns <code>false</code> from
     * <code>empty().hasMoreElements()</code> and throws <code>NoSuchElementException</code>
     * from <code>empty().nextElement()</code>.
     * @param <T> type of initial content
     * @return the enumeration
     */
    public static final <T> Enumeration<T> empty() {
        Collection<T> emptyL = Collections.emptyList();
        return Collections.enumeration(emptyL);
    }

    /**
     * Creates an enumeration with one element.
     * @param <T> type of initial content
     * @param obj the element to be present in the enumeration.
     * @return enumeration
     */
    public static <T> Enumeration<T> singleton(T obj) {
        return Collections.enumeration(Collections.singleton(obj));
    }

    /**
     * Concatenates the content of two enumerations into one.
     * Until the
     * end of <code>en1</code> is reached its elements are being served.
     * As soon as the <code>en1</code> has no more elements, the content
     * of <code>en2</code> is being returned.
     * @param <T> type of initial content
     * @param en1 first enumeration
     * @param en2 second enumeration
     * @return enumeration
     */
    public static <T> Enumeration<T> concat(Enumeration<? extends T> en1, Enumeration<? extends T> en2) {
        ArrayList<Enumeration<? extends T>> two = new ArrayList<Enumeration<? extends T>>();
        two.add(en1);
        two.add(en2);
        return new SeqEn<T>(Collections.enumeration(two));
    }

    /**
     * Concatenates the content of many enumerations.
     * The input value
     * is enumeration of Enumeration elements and the result is composed
     * all their content. Each of the provided enumeration is fully read
     * and their content returned before the next enumeration is asked for
     * their elements.
     * @param <T> type of initial content
     * @param enumOfEnums Enumeration of Enumeration elements
     * @return enumeration
     */
    public static <T> Enumeration<T> concat(Enumeration<? extends Enumeration<? extends T>> enumOfEnums) {
        return new SeqEn<T>(enumOfEnums);
    }

    /**
     * Filters the input enumeration to new one that should contain
     * each of the provided elements just once.
     * The elements are compared
     * using their default <code>equals</code> and <code>hashCode</code> methods.
     * @param <T> type of initial content
     * @param en enumeration to filter
     * @return enumeration without duplicated items
     */
    public static <T> Enumeration<T> removeDuplicates(Enumeration<T> en) {
        class RDupls implements Processor<T,T> {
            private Set<T> set = new HashSet<T>();

            public T process(T o, Collection<T> nothing) {
                return set.add(o) ? o : null;
            }
        }

        return filter(en, new RDupls());
    }

    /**
     * Returns an enumeration that iterates over provided array.
     * @param <T> type of initial content
     * @param arr the array of object
     * @return enumeration of those objects
     */
    public static <T> Enumeration<T> array(T... arr) {
        return Collections.enumeration(Arrays.asList(arr));
    }

    /**
     * Removes all <code>null</code>s from the input enumeration.
     * @param <T> type of initial content
     * @param en enumeration that can contain nulls
     * @return new enumeration without null values
     */
    public static <T> Enumeration<T> removeNulls(Enumeration<T> en) {
        return filter(en, new RNulls<T>());
    }

    /**
     * For each element of the input enumeration <code>en</code> asks the
     * {@link Processor} to provide a replacement.
     * The <code>toAdd</code> argument of the processor is always null.
     * <p>
     * Example to convert any objects into strings:
     * <pre>
     * Processor convertToString = new Processor() {
     *     public Object process(Object obj, Collection alwaysNull) {
     *         return obj.toString(); // converts to string
     *     }
     * };
     * Enumeration strings = Enumerations.convert(elems, convertToString);
     * </pre>
     * @param <T> type of initial content
     * @param <R> type of result content
     * @param en enumeration of any objects
     * @param processor a callback processor for the elements (its toAdd arguments is always null)
     * @return new enumeration where all elements has been processed
     */
    public static <T,R> Enumeration<R> convert(Enumeration<? extends T> en, Processor<T,R> processor) {
        return new AltEn<T,R>(en, processor);
    }

    /**
     * Filters some elements out from the input enumeration.
     * Just make the
     * {@link Processor} return <code>null</code>. Please notice the <code>toAdd</code>
     * argument of the processor is always <code>null</code>.
     * <p>
     * Example to remove all objects that are not strings:
     * <pre>
     * Processor onlyString = new Processor() {
     *     public Object process(Object obj, Collection alwaysNull) {
     *         if (obj instanceof String) {
     *             return obj;
     *         } else {
     *             return null;
     *         }
     *     }
     * };
     * Enumeration strings = Enumerations.filter(elems, onlyString);
     * </pre>
     * @param <T> type of initial content
     * @param <R> type of result content
     * @param en enumeration of any objects
     * @param filter a callback processor for the elements (its toAdd arguments is always null)
     * @return new enumeration which does not include non-processed (returned null from processor) elements
     * @see NbCollections#checkedEnumerationByFilter
     */
    public static <T,R> Enumeration<R> filter(Enumeration<? extends T> en, Processor<T,R> filter) {
        Parameters.notNull("en", en);
        Parameters.notNull("filter", filter);
        return new FilEn<T,R>(en, filter);
    }

    /**
     * Support for breadth-first enumerating.
     * Before any element is returned
     * for the resulting enumeration it is processed in the {@link Processor} and
     * the processor is allowed to modify it and also add additional elements
     * at the (current) end of the <code>queue</code> by calling <code>toAdd.add</code>
     * or <code>toAdd.addAll</code>. No other methods can be called on the
     * provided <code>toAdd</code> collection.
     * <p>
     * Example of doing breadth-first walk through a tree:
     * <pre>
     * Processor queueSubnodes = new Processor() {
     *     public Object process(Object obj, Collection toAdd) {
     *         Node n = (Node)obj;
     *         toAdd.addAll (n.getChildrenList());
     *         return n;
     *     }
     * };
     * Enumeration strings = Enumerations.queue(elems, queueSubnodes);
     * </pre>
     * @param <T> type of initial content
     * @param <R> type of result content
     * @param en initial content of the resulting enumeration
     * @param filter the processor that is called for each element and can
     *        add and addAll elements to its toAdd Collection argument and
     *        also change the value to be returned
     * @return enumeration with the initial and queued content (it can contain
     *       <code>null</code> if the filter returned <code>null</code> from its
     *       {@link Processor#process} method.
     */
    public static <T,R> Enumeration<R> queue(Enumeration<? extends T> en, Processor<T,R> filter) {
        QEn<T,R> q = new QEn<T,R>(filter);

        while (en.hasMoreElements()) {
            q.put(en.nextElement());
        }

        return q;
    }

    /**
     * Processor interface that can filter out objects from the enumeration,
     * change them or add aditional objects to the end of the current enumeration.
     */
    public static interface Processor<T,R> {
        /** @param original the object that is going to be returned from the enumeration right now
         * @return a replacement for this object
         * @param toAdd can be non-null if one can add new objects at the end of the enumeration
         */
        public R process(T original, Collection<T> toAdd);
    }

    /** Altering enumeration implementation */
    private static final class AltEn<T,R> extends Object implements Enumeration<R> {
        /** enumeration to filter */
        private Enumeration<? extends T> en;

        /** map to alter */
        private Processor<T,R> process;

        /**
        * @param en enumeration to filter
        */
        public AltEn(Enumeration<? extends T> en, Processor<T,R> process) {
            this.en = en;
            this.process = process;
        }

        /** @return true if there is more elements in the enumeration
        */
        public boolean hasMoreElements() {
            return en.hasMoreElements();
        }

        /** @return next object in the enumeration
        * @exception NoSuchElementException can be thrown if there is no next object
        *   in the enumeration
        */
        public R nextElement() {
            return process.process(en.nextElement(), null);
        }
    }
     // end of AltEn

    /** Sequence of enumerations */
    private static final class SeqEn<T> extends Object implements Enumeration<T> {
        /** enumeration of Enumerations */
        private Enumeration<? extends Enumeration<? extends T>> en;

        /** current enumeration */
        private Enumeration<? extends T> current;

        /** is {@link #current} up-to-date and has more elements?
        * The combination <CODE>current == null</CODE> and
        * <CODE>checked == true means there are no more elements
        * in this enumeration.
        */
        private boolean checked = false;

        /** Constructs new enumeration from already existing. The elements
        * of <CODE>en</CODE> should be also enumerations. The resulting
        * enumeration contains elements of such enumerations.
        *
        * @param en enumeration of Enumerations that should be sequenced
        */
        public SeqEn(Enumeration<? extends Enumeration <? extends T>> en) {
            this.en = en;
        }

        /** Ensures that current enumeration is set. If there aren't more
        * elements in the Enumerations, sets the field <CODE>current</CODE> to null.
        */
        private void ensureCurrent() {
            while ((current == null) || !current.hasMoreElements()) {
                if (en.hasMoreElements()) {
                    current = en.nextElement();
                } else {
                    // no next valid enumeration
                    current = null;

                    return;
                }
            }
        }

        /** @return true if we have more elements */
        public boolean hasMoreElements() {
            if (!checked) {
                ensureCurrent();
                checked = true;
            }

            return current != null;
        }

        /** @return next element
        * @exception NoSuchElementException if there is no next element
        */
        public T nextElement() {
            if (!checked) {
                ensureCurrent();
            }

            if (current != null) {
                checked = false;

                return current.nextElement();
            } else {
                checked = true;
                throw new java.util.NoSuchElementException();
            }
        }
    }
     // end of SeqEn

    /** QueueEnumeration
     */
    private static class QEn<T,R> extends Object implements Enumeration<R> {
        /** next object to be returned */
        private ListItem<T> next = null;

        /** last object in the queue */
        private ListItem<T> last = null;

        /** processor to use */
        private Processor<T,R> processor;

        public QEn(Processor<T,R> p) {
            this.processor = p;
        }

        /** Put adds new object to the end of queue.
        * @param o the object to add
        */
        public void put(T o) {
            if (last != null) {
                ListItem<T> li = new ListItem<T>(o);
                last.next = li;
                last = li;
            } else {
                next = last = new ListItem<T>(o);
            }
        }

        /** Adds array of objects into the queue.
        * @param arr array of objects to put into the queue
        */
        public void put(Collection<? extends T> arr) {
            for (T e : arr) {
                put(e);
            }
        }

        /** Is there any next object?
        * @return true if there is next object, false otherwise
        */
        public boolean hasMoreElements() {
            return next != null;
        }

        /** @return next object in enumeration
        * @exception NoSuchElementException if there is no next object
        */
        public R nextElement() {
            if (next == null) {
                throw new NoSuchElementException();
            }

            T res = next.object;

            if ((next = next.next) == null) {
                last = null;
            }

            ;

            ToAdd<T,R> toAdd = new ToAdd<T,R>(this);
            R out = processor.process(res, toAdd);
            toAdd.finish();

            return out;
        }

        /** item in linked list of Objects */
        private static final class ListItem<T> {
            T object;
            ListItem<T> next;

            /** @param o the object for this item */
            ListItem(T o) {
                object = o;
            }
        }

        /** Temporary collection that supports only add and addAll operations*/
        private static final class ToAdd<T,R> extends Object implements Collection<T> {
            private QEn<T,R> q;

            public ToAdd(QEn<T,R> q) {
                this.q = q;
            }

            public void finish() {
                this.q = null;
            }

            public boolean add(T o) {
                q.put(o);

                return true;
            }

            public boolean addAll(Collection<? extends T> c) {
                q.put(c);

                return true;
            }

            private String msg() {
                return "Only add and addAll are implemented"; // NOI18N
            }

            public void clear() {
                throw new UnsupportedOperationException(msg());
            }

            public boolean contains(Object o) {
                throw new UnsupportedOperationException(msg());
            }

            public boolean containsAll(Collection c) {
                throw new UnsupportedOperationException(msg());
            }

            public boolean isEmpty() {
                throw new UnsupportedOperationException(msg());
            }

            public Iterator<T> iterator() {
                throw new UnsupportedOperationException(msg());
            }

            public boolean remove(Object o) {
                throw new UnsupportedOperationException(msg());
            }

            public boolean removeAll(Collection c) {
                throw new UnsupportedOperationException(msg());
            }

            public boolean retainAll(Collection c) {
                throw new UnsupportedOperationException(msg());
            }

            public int size() {
                throw new UnsupportedOperationException(msg());
            }

            public Object[] toArray() {
                throw new UnsupportedOperationException(msg());
            }

            public<X> X[] toArray(X[] a) {
                throw new UnsupportedOperationException(msg());
            }
        }
         // end of ToAdd
    }
     // end of QEn

    /** Filtering enumeration */
    private static final class FilEn<T,R> extends Object implements Enumeration<R> {
        /** marker object stating there is no nexte element prepared */
        private static final Object EMPTY = new Object();

        /** enumeration to filter */
        private Enumeration<? extends T> en;

        /** element to be returned next time or {@link #EMPTY} if there is
        * no such element prepared */
        private R next = empty();

        /** the set to use as filter */
        private Processor<T,R> filter;

        /**
        * @param en enumeration to filter
        */
        public FilEn(Enumeration<? extends T> en, Processor<T,R> filter) {
            this.en = en;
            this.filter = filter;
        }

        /** @return true if there is more elements in the enumeration
        */
        public boolean hasMoreElements() {
            if (next != empty()) {
                // there is a object already prepared
                return true;
            }

            while (en.hasMoreElements()) {
                // read next
                next = filter.process(en.nextElement(), null);

                if (next != null) {
                    // if the object is accepted
                    return true;
                }

                ;
            }

            next = empty();

            return false;
        }

        /** @return next object in the enumeration
        * @exception NoSuchElementException can be thrown if there is no next object
        *   in the enumeration
        */
        public R nextElement() {
            if ((next == EMPTY) && !hasMoreElements()) {
                throw new NoSuchElementException();
            }

            R res = next;
            next = empty();

            return res;
        }

        @SuppressWarnings("unchecked")
        private R empty() {
            return (R)EMPTY;
        }
    }
     // end of FilEn

    /** Returns true from contains if object is not null */
    private static class RNulls<T> implements Processor<T,T> {
        public T process(T original, Collection<T> toAdd) {
            return original;
        }
    }
     // end of RNulls
}
