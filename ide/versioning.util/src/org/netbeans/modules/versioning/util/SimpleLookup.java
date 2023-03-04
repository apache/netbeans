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

package org.netbeans.modules.versioning.util;

import java.util.ArrayList;
import java.util.Collection;
import org.openide.util.lookup.AbstractLookup;

/**
 * Simple lookup with modifiable content.
 *
 * @author Marian Petras
 * @since 1.9.1
 */
public class SimpleLookup extends AbstractLookup {

    protected final Object dataSetLock = new Object();

    public void setData(Object... data) {
        validateData(data);
        data = rectifyData(data);
        if (data == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        setValidatedData(data);
    }

    /**
     * Validates the data.
     * The default implementation just checks that the input array is not
     * {@code null}.
     * @param  data  the input data
     * @exception  java.lang.IllegalArgumentException
     *             if the data are invalid, e.g. if the passed array is
     *             {@code null}
     */
    protected void validateData(Object[] data) throws IllegalArgumentException {
        if (data == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }
    }

    /**
     * Rectifies the data.
     * This method is called after the data had passed
     * method {@code validateData()}.
     * The default implementation removes {@code null}s and duplicate items
     * from the array.
     *
     * @param  data  data to be rectified
     * @return  rectified data
     * @see  #validateData
     */
    protected Object[] rectifyData(Object[] data) {
        return CollectionUtils.removeItem(
                    CollectionUtils.removeDuplicates(data),
                    null);
    }

    protected void setValidatedData(Object[] data) {
        synchronized (dataSetLock) {
            setDataImpl(data);
        }
    }

    protected final void setDataImpl(Object[] data) {
        if (!Thread.holdsLock(dataSetLock)) {
            throw new IllegalStateException(
                    "This method must be called with the dataSetLock held being held by the current thread."); //NOI18N
        }
        if (data == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        Collection<Pair> pairs = new ArrayList<Pair>(data.length);
        for (Object d : data) {
            pairs.add(new SimpleItem(d));
        }
        setPairs(pairs);
    }
    
    /** Copy from AbstractLookup.SimpleItem */
    private static final class SimpleItem<T> extends Pair<T> {
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
        @Override
        public boolean instanceOf(Class<?> c) {
            return c.isInstance(obj);
        }

        /** Get instance of registered object. If convertor is specified then
         *  method InstanceLookup.Convertor.convertor is used and weak reference
         * to converted object is saved.
         * @return the instance of the object.
         */
        @Override
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
        @Override
        public String getId() {
            return "IL[" + obj.toString(); // NOI18N
        }

        /** Getter for display name of the item.
         */
        @Override
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
            return obj == this.obj;
        }

        /** The class of this item.
         * @return the correct class
         */
        @SuppressWarnings("unchecked")
        @Override
        public Class<? extends T> getType() {
            return (Class<? extends T>)obj.getClass();
        }
    }

}
