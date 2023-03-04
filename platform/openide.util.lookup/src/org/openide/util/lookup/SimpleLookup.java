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
package org.openide.util.lookup;

import org.openide.util.Lookup;
import org.openide.util.LookupListener;

import java.util.*;


/**
 * Simple lookup implementation. It can be used to create temporary lookups
 * that do not change over time. The result stores references to all objects
 * passed in the constructor. Those objecst are the only ones returned as
 * result.
 * @author David Strupl
 */
class SimpleLookup extends org.openide.util.Lookup {
    /** This variable is initialized in constructor and thus null
     * value is not allowed as its value. */
    private Collection<Item<?>> allItems;

    /**
     * Creates new Result object with supplied instances parameter.
     * @param instances to be used to return from the lookup
     */
    SimpleLookup(Collection<Object> instances) {
        allItems = new ArrayList<Item<?>>(instances.size());

        for (Iterator i = instances.iterator(); i.hasNext();) {
            allItems.add(new InstanceContent.SimpleItem<Object>(i.next()));
        }
    }

    <T,R> SimpleLookup(Collection<T> keys, InstanceContent.Convertor<? super T,R> conv) {
        allItems = new ArrayList<Item<?>>(keys.size());

        for (T item : keys) {
            allItems.add(new InstanceContent.ConvertingItem<T,R>(item, conv));
        }
    }

    public String toString() {
        return "SimpleLookup" + lookup(new Template<Object>(Object.class)).allInstances();
    }

    public <T> Result<T> lookup(Template<T> template) {
        if (template == null) {
            throw new NullPointerException();
        }

        return new SimpleResult<T>(template);
    }

    public <T> T lookup(Class<T> clazz) {
        for (Iterator i = allItems.iterator(); i.hasNext();) {
            Object o = i.next();

            if (o instanceof AbstractLookup.Pair) {
                AbstractLookup.Pair<?> p = (AbstractLookup.Pair<?>)o;
                if (p.instanceOf(clazz)) {
                    Object ret = p.getInstance();
                    if (clazz.isInstance(ret)) {
                        return clazz.cast(ret);
                    }
                }
            }
        }
        return null;
    }

    /** A method that defines matching between Item and Template.
     * @param item the item to match
     * @return true if item matches the template requirements, false if not
     */
    private static boolean matches(Template<?> t, AbstractLookup.Pair<?> item) {
        if (!AbstractLookup.matches(t, item, true)) {
            return false;
        }

        Class<?> type = t.getType();

        if ((type != null) && !type.isAssignableFrom(item.getType())) {
            return false;
        }

        return true;
    }

    /**
     * Result used in SimpleLookup. It holds a reference to the collection
     * passed in constructor. As the contents of this lookup result never
     * changes the addLookupListener and removeLookupListener are empty.
     */
    private class SimpleResult<T> extends Lookup.Result<T> {
        /** can be null and is initialized lazily */
        private Set<Class<? extends T>> classes;

        /** can be null and is initialized lazily */
        private Collection<? extends Item<T>> items;

        /** Template used for this result. It is never null.*/
        private Template<T> template;

        /** can be null and is initialized lazily */
        private Collection<T> results;

        /** Just remembers the supplied argument in variable template.*/
        SimpleResult(Template<T> template) {
            this.template = template;
        }

        /**
         * Intentionally does nothing because the lookup does not change
         * and no notification is needed.
         */
        public void addLookupListener(LookupListener l) {
        }

        /**
         * Intentionally does nothing because the lookup does not change
         * and no notification is needed.
         */
        public void removeLookupListener(LookupListener l) {
        }

        /**
         * Lazy initializes the results collection. Uses a call to allItems
         * to obtain the instances.
         */
        public java.util.Collection<? extends T> allInstances() {
            synchronized (this) {
                if (results != null) {
                    return results;
                }
            }


            Collection<T> res = new ArrayList<T>(allItems.size());

            for (Item<T> item : allItems()) {
                res.add(item.getInstance());
            }

            synchronized (this) {
                results = Collections.unmodifiableCollection(res);
            }

            return results;
        }

        /**
         * Lazy initializes variable classes. Uses a call to allItems to
         * compute the result.
         */
        public Set<Class<? extends T>> allClasses() {
            synchronized (this) {
                if (classes != null) {
                    return classes;
                }
            }

            Set<Class<? extends T>> res = new HashSet<Class<? extends T>>();

            for (Item<T> item : allItems()) {
                res.add(item.getType());
            }

            synchronized (this) {
                classes = Collections.unmodifiableSet(res);
            }

            return classes;
        }

        /**
         * Lazy initializes variable items. Creates an item for each
         * element in the instances collection. It puts either SimpleItem
         * or ConvertingItem to the collection.
         */
        public Collection<? extends Item<T>> allItems() {
            synchronized (this) {
                if (items != null) {
                    return items;
                }
            }

            Collection<Item<T>> res = new ArrayList<Item<T>>(allItems.size());

            for (Iterator<Item<?>> i = allItems.iterator(); i.hasNext();) {
                Item<?> o = i.next();

                if (o instanceof AbstractLookup.Pair) {
                    if (matches(template, (AbstractLookup.Pair) o)) {
                        res.add(cast(o));
                    }
                }
            }

            synchronized (this) {
                items = Collections.unmodifiableCollection(res);
            }

            return items;
        }

        @SuppressWarnings("unchecked")
        private Item<T> cast(Item<?> i) {
            return (Item<T>)i;
        }
    }
}
