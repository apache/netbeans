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

import org.openide.util.lookup.AbstractLookup.Pair;

import java.lang.ref.WeakReference;

import java.util.*;
import java.util.concurrent.Executor;
import org.openide.util.Lookup.Item;


/** A special content implementation that can be passed to AbstractLookup
 * and provides methods for registration of instances and lazy instances.
 * <PRE>
 * {@link InstanceContent} ic = new {@link InstanceContent#InstanceContent() InstanceContent()};
 * {@link org.openide.util.Lookup} lookup = new {@link AbstractLookup#AbstractLookup(org.openide.util.lookup.AbstractLookup.Content) AbstractLookup(ic)};
 *
 * ic.{@link #add(java.lang.Object) add(new Object ())};
 * ic.{@link #add(java.lang.Object) add(new Dimension (...))};
 *
 * {@link java.awt.Dimension Dimension} theDim = lookup.lookup ({@link java.awt.Dimension Dimension}.class);
 * </PRE>
 *
 * @author  Jaroslav Tulach
 *
 * @since 1.25
 */
public final class InstanceContent extends AbstractLookup.Content {
    /**
     * Create a new, empty content.
     */
    public InstanceContent() {
    }

    /** Creates a content associated with an executor to handle dispatch
     * of changes.
     * @param notifyIn the executor to notify changes in
     * @since  7.16
     */
    public InstanceContent(Executor notifyIn) {
        super(notifyIn);
    }
    
    /** Adds an instance to the lookup. If <code>inst</code> already exists 
     * in the lookup (equality is determined by object's {@link Object#equals(java.lang.Object)}
     * method) then the new instance replaces the old one 
     * in the lookup but listener notifications are <i>not</i> delivered in 
     * such case.
     * 
     * @param inst instance
     */
    public final void add(Object inst) {
        addPair(new SimpleItem<Object>(inst));
    }

    /** Adds a convertible instance into the lookup. The <code>inst</code>
     * argument is just a key, not the actual value to appear in the lookup.
     * The value will be created on demand, later when it is really needed
     * by calling <code>convertor</code> methods.
     * <p>
     * This method is useful to delay creation of heavy weight objects.
     * Instead just register lightweight key and a convertor.
     * <p>
     * To remove registered object from lookup use {@link #remove(java.lang.Object, org.openide.util.lookup.InstanceContent.Convertor)}
     * with the same arguments.
     * 
     * @param <T> type of instance
     * @param <R> type to convert instance to
     * @param inst instance
     * @param conv convertor which postponing an instantiation,
     * if <code>conv==null</code> then the instance is registered directly.
     */
    public final <T,R> void add(T inst, Convertor<T,R> conv) {
        addPair(new ConvertingItem<T,R>(inst, conv));
    }

    /** Remove instance.
     * @param inst instance
     */
    public final void remove(Object inst) {
        removePair(new SimpleItem<Object>(inst));
    }

    /** Remove instance added with a convertor.
     * @param <T> type of instance
     * @param <R> type to convert instance to
     * @param inst instance
     * @param conv convertor, if <code>conv==null</code> it is same like
     * remove(Object)
     */
    public final <T,R> void remove(T inst, Convertor<T,R> conv) {
        removePair(new ConvertingItem<T,R>(inst, conv));
    }

    /** Changes all pairs in the lookup to new values. Converts collection of
     * instances to collection of pairs.
     * @param <T> type of instance
     * @param <R> type to convert instance to
     * @param col the collection of (Item) objects
     * @param conv the convertor to use or null
     */
    public final <T,R> void set(Collection<T> col, Convertor<T,R> conv) {
        ArrayList<Pair<?>> l = new ArrayList<Pair<?>>(col.size());
        Iterator<T> it = col.iterator();

        if (conv == null) {
            while (it.hasNext()) {
                l.add(new SimpleItem<T>(it.next()));
            }
        } else {
            while (it.hasNext()) {
                l.add(new ConvertingItem<T,R>(it.next(), conv));
            }
        }

        setPairs(l);
    }

    /** Convertor postpones an instantiation of an object.
     * @since 1.25
     */
    public static interface Convertor<T,R> {
        /** Convert obj to other object. There is no need to implement
         * cache mechanism. It is provided by
         * {@link Item#getInstance()} method itself. However the
         * method can be called more than once because instance is held
         * just by weak reference.
         *
         * @param obj the registered object
         * @return the object converted from this object
         */
        public R convert(T obj);

        /** Return type of converted object. Accessible via
         * {@link Item#getType()}
         * @param obj the registered object
         * @return the class that will be produced from this object (class or
         *      superclass of convert (obj))
         */
        public Class<? extends R> type(T obj);

        /** Computes the ID of the resulted object. Accessible via
         * {@link Item#getId()}.
         * @param obj the registered object
         * @return the ID for the object
         */
        public String id(T obj);

        /** The human presentable name for the object. Accessible via
         * {@link Item#getDisplayName()}.
         * @param obj the registered object
         * @return the name representing the object for the user
         */
        public String displayName(T obj);
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
        @Override
        protected boolean creatorOf(Object obj) {
            return obj == null ? null == this.obj : obj.equals(this.obj);
        }

        /** The class of this item.
         * @return the correct class
         */
        @SuppressWarnings("unchecked")
        public Class<? extends T> getType() {
            return (Class<? extends T>)obj.getClass();
        }
    }
     // end of SimpleItem

    /** Instance of one item registered in the map.
     */
    static final class ConvertingItem<T,R> extends Pair<R> {
        /** registered object */
        private T obj;

        /** Reference to converted object. */
        private WeakReference<R> ref;

        /** convertor to use */
        private Convertor<? super T,R> conv;

        /** Create an item.
         * @obj object to register
         * @conv a convertor, can be <code>null</code>.
         */
        public ConvertingItem(T obj, Convertor<? super T,R> conv) {
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
    }
     // end of ConvertingItem
}
