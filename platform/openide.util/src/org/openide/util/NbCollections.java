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

package org.openide.util;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Enumerations;

/**
 * Utilities for working with generics.
 * <p>Note that there is no <code>checkedListByFilter</code> method currently.
 * If constant-time operation is important (e.g. your raw list is large and {@link RandomAccess})
 * you can use {@link #checkedListByCopy}, assuming you do not need to modify the underlying list.
 * If you are only interested in an iterator anyway, try {@link #checkedIteratorByFilter}.
 * @author Jesse Glick
 * @since org.openide.util 7.1
 */
public class NbCollections {

    private NbCollections() {}
    
    private static final Logger LOG = Logger.getLogger(NbCollections.class.getName());

    /**
     * Create a typesafe copy of a raw set.
     * @param rawSet an unchecked set
     * @param type the desired supertype of the entries
     * @param strict true to throw a <code>ClassCastException</code> if the raw set has an invalid entry,
     *               false to skip over such entries (warnings may be logged)
     * @return a typed set guaranteed to contain only entries assignable
     *         to the named type (or they may be null)
     * @throws ClassCastException if some entry in the raw set was not well-typed, and only if <code>strict</code> was true
     */
    public static <E> Set<E> checkedSetByCopy(Set rawSet, Class<E> type, boolean strict) throws ClassCastException {
        Set<E> s = new HashSet<E>(rawSet.size() * 4 / 3 + 1);
        Iterator it = rawSet.iterator();
        while (it.hasNext()) {
            Object e = it.next();
            try {
                s.add(type.cast(e));
            } catch (ClassCastException x) {
                if (strict) {
                    throw x;
                } else {
                    LOG.log(Level.WARNING, "Element {0} not assignable to {1}", new Object[] {e, type});
                }
            }
        }
        return s;
    }

    /**
     * Create a typesafe copy of a raw list.
     * @param rawList an unchecked list
     * @param type the desired supertype of the entries
     * @param strict true to throw a <code>ClassCastException</code> if the raw list has an invalid entry,
     *               false to skip over such entries (warnings may be logged)
     * @return a typed list guaranteed to contain only entries assignable
     *         to the named type (or they may be null)
     * @throws ClassCastException if some entry in the raw list was not well-typed, and only if <code>strict</code> was true
     */
    public static <E> List<E> checkedListByCopy(List rawList, Class<E> type, boolean strict) throws ClassCastException {
        List<E> l = (rawList instanceof RandomAccess) ? new ArrayList<E>(rawList.size()) : new LinkedList<E>();
        Iterator it = rawList.iterator();
        while (it.hasNext()) {
            Object e = it.next();
            try {
                l.add(type.cast(e));
            } catch (ClassCastException x) {
                if (strict) {
                    throw x;
                } else {
                    LOG.log(Level.WARNING, "Element {0} not assignable to {1}", new Object[] {e, type});
                }
            }
        }
        return l;
    }

    /**
     * Create a typesafe copy of a raw map.
     * @param rawMap an unchecked map
     * @param keyType the desired supertype of the keys
     * @param valueType the desired supertype of the values
     * @param strict true to throw a <code>ClassCastException</code> if the raw map has an invalid key or value,
     *               false to skip over such map entries (warnings may be logged)
     * @return a typed map guaranteed to contain only keys and values assignable
     *         to the named types (or they may be null)
     * @throws ClassCastException if some key or value in the raw map was not well-typed, and only if <code>strict</code> was true
     */
    public static <K,V> Map<K,V> checkedMapByCopy(Map rawMap, Class<K> keyType, Class<V> valueType, boolean strict) throws ClassCastException {
        Map<K,V> m2 = new HashMap<K,V>(rawMap.size() * 4 / 3 + 1);
        Iterator it = rawMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            try {
                m2.put(keyType.cast(e.getKey()), valueType.cast(e.getValue()));
            } catch (ClassCastException x) {
                if (strict) {
                    throw x;
                } else {
                    LOG.log(Level.WARNING, "Entry {0} not assignable to <{1},{2}>", new Object[] {e, keyType, valueType});
                }
            }
        }
        return m2;
    }

    private static abstract class CheckedIterator<E> implements Iterator<E> {

        private static final Object WAITING = new Object();

        private final Iterator it;
        private Object next = WAITING;

        public CheckedIterator(Iterator it) {
            this.it = it;
        }

        protected abstract boolean accept(Object o);

        public boolean hasNext() {
            if (next != WAITING) {
                return true;
            }
            while (it.hasNext()) {
                next = it.next();
                if (accept(next)) {
                    return true;
                }
            }
            next = WAITING;
            return false;
        }

        public E next() {
            if (next == WAITING && !hasNext()) {
                throw new NoSuchElementException();
            }
            assert next != WAITING;
            @SuppressWarnings("unchecked") // type-checking is done by accept()
            E x = (E) next;
            next = WAITING;
            return x;
        }

        public void remove() {
            it.remove();
        }

    }

    /**
     * Create a typesafe filter of an unchecked iterator.
     * {@link Iterator#remove} will work if it does in the unchecked iterator.
     * @param rawIterator an unchecked iterator
     * @param type the desired enumeration type
     * @param strict if false, elements which are not null but not assignable to the requested type are omitted;
     *               if true, {@link ClassCastException} may be thrown from an iterator operation
     * @return an iterator guaranteed to contain only objects of the requested type (or null)
     */
    public static <E> Iterator<E> checkedIteratorByFilter(Iterator rawIterator, final Class<E> type, final boolean strict) {
        return new CheckedIterator<E>(rawIterator) {
            protected boolean accept(Object o) {
                if (o == null) {
                    return true;
                } else if (type.isInstance(o)) {
                    return true;
                } else if (strict) {
                    throw new ClassCastException(o + " was not a " + type.getName()); // NOI18N
                } else {
                    return false;
                }
            }
        };
    }

    /**
     * Create a typesafe view over an underlying raw set.
     * Mutations affect the underlying set (this is not a copy).
     * {@link Set#clear} will make the view empty but may not clear the underlying set.
     * You may add elements only of the requested type.
     * {@link Set#contains} also performs a type check and will throw {@link ClassCastException}
     * for an illegal argument.
     * The view is serializable if the underlying set is.
     * @param rawSet an unchecked set
     * @param type the desired element type
     * @param strict if false, elements in the underlying set which are not null and which are not assignable
     *               to the requested type are omitted from the view;
     *               if true, a {@link ClassCastException} may arise during some set operation
     * @return a view over the raw set guaranteed to match the specified type
     */
    public static <E> Set<E> checkedSetByFilter(Set rawSet, Class<E> type, boolean strict) {
        return new CheckedSet<E>(rawSet, type, strict);
    }
    private static final class CheckedSet<E> extends AbstractSet<E> implements Serializable {

        private static final long serialVersionUID = 1L;

        private final Set rawSet;
        private final Class<E> type;
        private final boolean strict;

        public CheckedSet(Set rawSet, Class<E> type, boolean strict) {
            this.rawSet = rawSet;
            this.type = type;
            this.strict = strict;
        }

        private boolean acceptEntry(Object o) {
            if (o == null) {
                return true;
            } else if (type.isInstance(o)) {
                return true;
            } else if (strict) {
                throw new ClassCastException(o + " was not a " + type.getName()); // NOI18N
            } else {
                return false;
            }
        }

        @Override
        public Iterator<E> iterator() {
            return new CheckedIterator<E>(rawSet.iterator()) {
                @Override
                protected boolean accept(Object o) {
                    return acceptEntry(o);
                }
            };
        }

        @Override
        public int size() {
            int c = 0;
            Iterator it = rawSet.iterator();
            while (it.hasNext()) {
                if (acceptEntry(it.next())) {
                    c++;
                }
            }
            return c;
        }

        @Override
        @SuppressWarnings("unchecked") // complains about usage of raw set
        public boolean add(E o) {
            return rawSet.add(type.cast(o));
        }

        @Override
        public boolean contains(Object o) {
            return rawSet.contains(type.cast(o));
        }

    }

    /**
     * Create a typesafe view over an underlying raw map.
     * Mutations affect the underlying map (this is not a copy).
     * {@link Map#clear} will make the view empty but may not clear the underlying map.
     * You may add entries only of the requested type pair.
     * {@link Map#get}, {@link Map#containsKey}, and {@link Map#containsValue} also perform a type check
     * and will throw {@link ClassCastException} for an illegal argument.
     * The view is serializable if the underlying map is.
     * @param rawMap an unchecked map
     * @param keyType the desired entry key type
     * @param valueType the desired entry value type
     * @param strict if false, entries in the underlying map for which the key is not null but not assignable
     *               to the requested key type, and/or the value is not null but not assignable to
     *               the requested value type, are omitted from the view;
     *               if true, a {@link ClassCastException} may arise during some map operation
     * @return a view over the raw map guaranteed to match the specified type
     */
    public static <K,V> Map<K,V> checkedMapByFilter(Map rawMap, Class<K> keyType, Class<V> valueType, boolean strict) {
        return new CheckedMap<K,V>(rawMap, keyType, valueType, strict);
    }
    private static final class CheckedMap<K,V> extends AbstractMap<K,V> implements Serializable {

        private static final long serialVersionUID = 1L;

        private final Map rawMap;
        private final Class<K> keyType;
        private final Class<V> valueType;
        private final boolean strict;

        public CheckedMap(Map rawMap, Class<K> keyType, Class<V> valueType, boolean strict) {
            this.rawMap = rawMap;
            this.keyType = keyType;
            this.valueType = valueType;
            this.strict = strict;
        }

        private boolean acceptKey(Object o) {
            if (o == null) {
                return true;
            } else if (keyType.isInstance(o)) {
                return true;
            } else if (strict) {
                throw new ClassCastException(o + " was not a " + keyType.getName()); // NOI18N
            } else {
                return false;
            }
        }

        private boolean acceptValue(Object o) {
            if (o == null) {
                return true;
            } else if (valueType.isInstance(o)) {
                return true;
            } else if (strict) {
                throw new ClassCastException(o + " was not a " + valueType.getName()); // NOI18N
            } else {
                return false;
            }
        }

        private boolean acceptEntry(Map.Entry e) {
            return acceptKey(e.getKey()) && acceptValue(e.getValue());
        }

        private final class EntrySet extends AbstractSet<Map.Entry<K, V>> {

            @Override
            public Iterator<Map.Entry<K,V>> iterator() {
                return new CheckedIterator<Map.Entry<K,V>>(rawMap.entrySet().iterator()) {
                    @Override
                    protected boolean accept(Object o) {
                        return acceptEntry((Map.Entry) o);
                    }
                };
            }

            @Override
            public int size() {
                int c = 0;
                Iterator it = rawMap.entrySet().iterator();
                while (it.hasNext()) {
                    if (acceptEntry((Map.Entry) it.next())) {
                        c++;
                    }
                }
                return c;
            }

        }
        @Override
        public Set<Map.Entry<K, V>> entrySet() {
            return new EntrySet();
        }

        @Override
        public V get(Object key) {
            Object o = rawMap.get(keyType.cast(key));
            if (acceptValue(o)) {
                @SuppressWarnings("unchecked")
                V v = (V) o;
                return v;
            } else {
                return null;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public V put(K key, V value) {
            Object old = rawMap.put(keyType.cast(key), valueType.cast(value));
            if (acceptValue(old)) {
                return (V) old;
            } else {
                return null;
            }
        }

        @Override
        public V remove(Object key) {
            Object old = rawMap.remove(keyType.cast(key));
            if (acceptValue(old)) {
                @SuppressWarnings("unchecked")
                V v = (V) old;
                return v;
            } else {
                return null;
            }
        }

        @Override
        public boolean containsKey(Object key) {
            return rawMap.containsKey(keyType.cast(key)) &&
                    acceptValue(rawMap.get(key));
        }

        @Override
        public boolean containsValue(Object value) {
            // Cannot just ask rawMap since we could not check type of key.
            return super.containsValue(valueType.cast(value));
        }

        @Override
        public int size() {
            int c = 0;
            Iterator it = rawMap.entrySet().iterator();
            while (it.hasNext()) {
                if (acceptEntry((Map.Entry) it.next())) {
                    c++;
                }
            }
            return c;
        }

        // keySet, values cannot be so easily overridden because we type-check whole entries

    }

    /**
     * Create a typesafe filter of an unchecked enumeration.
     * @param rawEnum an unchecked enumeration
     * @param type the desired enumeration type
     * @param strict if false, elements which are not null but not assignable to the requested type are omitted;
     *               if true, {@link ClassCastException} may be thrown from an enumeration operation
     * @return an enumeration guaranteed to contain only objects of the requested type (or null)
     */
    public static <E> Enumeration<E> checkedEnumerationByFilter(Enumeration<?> rawEnum, final Class<E> type, final boolean strict) {
        @SuppressWarnings("unchecked")
        Enumeration<?> _rawEnum = rawEnum;
        return Enumerations.<Object,E>filter(_rawEnum, new Enumerations.Processor<Object,E>() {
            public E process(Object o, Collection<Object> ignore) {
                if (o == null) {
                    return null;
                } else {
                    try {
                        return type.cast(o);
                    } catch (ClassCastException x) {
                        if (strict) {
                            throw x;
                        } else {
                            return null;
                        }
                    }
                }
            }
        });
    }

    /**
     * Treat an {@link Iterator} as an {@link Iterable} so it can be used in an enhanced for-loop.
     * Bear in mind that the iterator is "consumed" by the loop and so should be used only once.
     * Generally it is best to put the code which obtains the iterator inside the loop header.
     * <div class="nonnormative">
     * <p>Example of correct usage:</p>
     * <pre>
     * String text = ...;
     * for (String token : NbCollections.iterable(new {@link java.util.Scanner}(text))) {
     *     // ...
     * }
     * </pre>
     * </div>
     * @param iterator an iterator
     * @return an iterable wrapper which will traverse the iterator once
     * @throws NullPointerException if the iterator is null
     * @see <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6312085">Java bug #6312085</a>
     * @see <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6360734">Java bug #6360734</a>
     * @see <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4988624">Java bug #4988624</a>
     * @since org.openide.util 7.5
     */
    public static <E> Iterable<E> iterable(final Iterator<E> iterator) {
        if (iterator == null) {
            throw new NullPointerException();
        }
        return new Iterable<E>() {
            public Iterator<E> iterator() {
                return iterator;
            }
        };
    }

    /**
     * Treat an {@link Enumeration} as an {@link Iterable} so it can be used in an enhanced for-loop.
     * Bear in mind that the enumeration is "consumed" by the loop and so should be used only once.
     * Generally it is best to put the code which obtains the enumeration inside the loop header.
     * <div class="nonnormative">
     * <p>Example of correct usage:</p>
     * <pre>
     * ClassLoader loader = ...;
     * String name = ...;
     * for (URL resource : NbCollections.iterable(loader.{@link ClassLoader#getResources getResources}(name))) {
     *     // ...
     * }
     * </pre>
     * </div>
     * @param enumeration an enumeration
     * @return an iterable wrapper which will traverse the enumeration once
     *         ({@link Iterator#remove} is not supported)
     * @throws NullPointerException if the enumeration is null
     * @see <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6349852">Java bug #6349852</a>
     * @since org.openide.util 7.5
     */
    public static <E> Iterable<E> iterable(final Enumeration<E> enumeration) {
        if (enumeration == null) {
            throw new NullPointerException();
        }
        return new Iterable<E>() {
            public Iterator<E> iterator() {
                return new Iterator<E>() {
                    public boolean hasNext() {
                        return enumeration.hasMoreElements();
                    }
                    public E next() {
                        return enumeration.nextElement();
                    }
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

}
