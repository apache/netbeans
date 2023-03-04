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
package org.openide.nodes;

import java.util.Iterator;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.AbstractLookup.Pair;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;


/** Content for a cookie set.
 */
final class CookieSetLkp extends AbstractLookup {
    private final CookieSet.Before before;
    
    public CookieSetLkp(CookieSet.Before b) {
        this.before = b;
    }
    
    public void add(Object obj) {
        addPair(new SimpleItem<Object>(obj));
    }
    public final <T,R> void add(T inst, InstanceContent.Convertor<T,R> conv) {
        addPair(new ConvertingItem<T,R>(inst, conv));
    }
    
    public void remove(Object obj) {
        removePair(new SimpleItem<Object>(obj));
    }
    public final <T,R> void remove(T inst, InstanceContent.Convertor<T,R> conv) {
        removePair(new ConvertingItem<T,R>(inst, conv));
    }

    void superRemovePair(Pair pair) {
        removePair(pair);
    }

    private ThreadLocal<Object> isInReplaceInst = new ThreadLocal<Object>();
    <T> void replaceInstances(Class<? extends T> clazz, T[] instances, CookieSet set) {
        Iterator<? extends Lookup.Item> it;
        Set<Lookup.Item> toRemove;
        List<AbstractLookup.Pair> pairs;
        
        Object prev = isInReplaceInst.get();
        try {
            isInReplaceInst.set(this);
            
            it = lookupResult(Object.class).allItems().iterator();
            pairs = new ArrayList<>();
        
            boolean change = false;
            int index = 0;
            while (it.hasNext()) {
                Lookup.Item item = it.next();
                assert item instanceof AbstractLookup.Pair : "Not Pair: " + item;

                if (clazz.isAssignableFrom(item.getType())) {
                    if (index < instances.length) {
                        if (item instanceof SimpleItem) {
                            SimpleItem<?> simple = (SimpleItem<?>)item;
                            if (simple.obj == instances[index]) {
                                index++;
                                pairs.add(simple);
                                continue;
                            }
                        }

                        change = true;
                        pairs.add(new SimpleItem<T>(instances[index++]));
                    } else {
                        change = true;
                    }
                } else {
                    pairs.add((AbstractLookup.Pair)item);
                }
            }

            while (index < instances.length) {
                change = true;
                pairs.add(new SimpleItem<T>(instances[index++]));
            }

            if (change) {
                setPairs(pairs);
                set.fireChangeEvent();
            }
        } finally {
            isInReplaceInst.set(prev);
        }
    }

    @Override
    protected void beforeLookup(Lookup.Template<?> template) {
        beforeLookupImpl(template.getType());
    }
    
    final void beforeLookupImpl(Class<?> clazz) {
        if (before != null && isInReplaceInst.get() == null) {
            before.beforeLookup(clazz);
        }
    }

    /** Instance of one item representing an object.
     */
    static final class SimpleItem<T> extends Pair<T> {
        private T obj;

        /** Create an item.
         * @obj object to register
         */
        public SimpleItem(T obj) {
            if (obj == null) {
                throw new NullPointerException();
            }
            this.obj = obj;
        }

        /** Tests whether this item can produce object
         * of class c.
         */
        public boolean instanceOf(Class<?> c) {
            return c.isInstance(obj);
        }

        /** Get instance of registered object. If convertor is specified then
         *  method InstanceLookup.Convertor.convertor is used and weak reference
         * to converted object is saved.
         * @return the instance of the object.
         */
        public T getInstance() {
            return obj;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof SimpleItem) {
                return obj.equals(((SimpleItem) o).obj);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return obj.hashCode();
        }

        /** An identity of the item.
         * @return string representing the item, that can be used for
         *   persistance purposes to locate the same item next time
         */
        public String getId() {
            return "IL[" + obj.toString(); // NOI18N
        }

        /** Getter for display name of the item.
         */
        public String getDisplayName() {
            return obj.toString();
        }

        /** Method that can test whether an instance of a class has been created
         * by this item.
         *
         * @param obj the instance
         * @return if the item has already create an instance and it is the same
         *  as obj.
         */
        protected boolean creatorOf(Object obj) {
            return obj == this.obj;
        }

        /** The class of this item.
         * @return the correct class
         */
        @SuppressWarnings("unchecked")
        public Class<? extends T> getType() {
            return (Class<? extends T>)obj.getClass();
        }
    } // end of SimpleItem

    /** Instance of one item registered in the map.
     */
    static final class ConvertingItem<T,R> extends Pair<R> {
        /** registered object */
        private T obj;

        /** Reference to converted object. */
        private WeakReference<R> ref;

        /** convertor to use */
        private InstanceContent.Convertor<? super T,R> conv;

        /** Create an item.
         * @obj object to register
         * @conv a convertor, can be <code>null</code>.
         */
        public ConvertingItem(T obj, InstanceContent.Convertor<? super T,R> conv) {
            this.obj = obj;
            this.conv = conv;
        }

        /** Tests whether this item can produce object
         * of class c.
         */
        public boolean instanceOf(Class<?> c) {
            return c.isAssignableFrom(getType());
        }

        /** Returns converted object or null if obj has not been converted yet
         * or reference was cleared by garbage collector.
         */
        private R getConverted() {
            if (ref == null) {
                return null;
            }

            return ref.get();
        }

        /** Get instance of registered object. If convertor is specified then
         *  method InstanceLookup.Convertor.convertor is used and weak reference
         * to converted object is saved.
         * @return the instance of the object.
         */
        public synchronized R getInstance() {
            R converted = getConverted();

            if (converted == null) {
                converted = conv.convert(obj);
                ref = new WeakReference<R>(converted);
            }

            return converted;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ConvertingItem) {
                return obj.equals(((ConvertingItem) o).obj);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return obj.hashCode();
        }

        /** An identity of the item.
         * @return string representing the item, that can be used for
         *   persistance purposes to locate the same item next time
         */
        public String getId() {
            return conv.id(obj);
        }

        /** Getter for display name of the item.
         */
        public String getDisplayName() {
            return conv.displayName(obj);
        }

        /** Method that can test whether an instance of a class has been created
         * by this item.
         *
         * @param obj the instance
         * @return if the item has already create an instance and it is the same
         *  as obj.
         */
        protected boolean creatorOf(Object obj) {
            if (conv == null) {
                return obj == this.obj;
            } else {
                return obj == getConverted();
            }
        }

        /** The class of this item.
         * @return the correct class
         */
        @SuppressWarnings("unchecked")
        public Class<? extends R> getType() {
            R converted = getConverted();

            if (converted == null) {
                return conv.type(obj);
            }

            return (Class<? extends R>)converted.getClass();
        }
    } // end of ConvertingItem
}
