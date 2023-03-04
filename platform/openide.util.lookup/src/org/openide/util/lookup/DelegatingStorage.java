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

import java.io.*;


import java.util.*;
import org.openide.util.lookup.AbstractLookup.Pair;


/** Storages that can switch between another storages.
 * @author  Jaroslav Tulach
 */
final class DelegatingStorage<Transaction> extends Object
implements Serializable, AbstractLookup.Storage<Transaction> {
    /** object to delegate to */
    private AbstractLookup.Storage<Transaction> delegate;

    /** thread just accessing the storage */
    private Thread owner;

    public DelegatingStorage(AbstractLookup.Storage<Transaction> d) {
        this.delegate = d;
        this.owner = Thread.currentThread();
    }

    /** Never serialize yourself, always put there the delegate */
    public Object writeReplace() {
        return this.delegate;
    }

    /** Method to check whether there is not multiple access from the same thread.
     */
    public void checkForTreeModification() {
        if (Thread.currentThread() == owner) {
            throw new AbstractLookup.ISE("You are trying to modify lookup from lookup query!"); // NOI18N
        }
    }

    /** Checks whether we have simple behaviour or complex.
     */
    public static boolean isSimple(AbstractLookup.Storage s) {
        if (s instanceof DelegatingStorage) {
            return ((DelegatingStorage) s).delegate instanceof ArrayStorage;
        } else {
            return s instanceof ArrayStorage;
        }
    }

    /** Exits from the owners ship of the storage.
     */
    public AbstractLookup.Storage<Transaction> exitDelegate() {
        if (Thread.currentThread() != owner) {
            throw new IllegalStateException("Onwer: " + owner + " caller: " + Thread.currentThread()); // NOI18N
        }

        AbstractLookup.Storage<Transaction> d = delegate;
        delegate = null;

        return d;
    }

    public boolean add(AbstractLookup.Pair<?> item, Transaction transaction) {
        return delegate.add(item, transaction);
    }

    public void remove(org.openide.util.lookup.AbstractLookup.Pair item, Transaction transaction) {
        delegate.remove(item, transaction);
    }

    public void retainAll(Map retain, Transaction transaction) {
        delegate.retainAll(retain, transaction);
    }

    /** A special method to change the backing storage.
     * In fact it is not much typesafe as it changes the
     * type of Transaction but we know that nobody is currently
     * holding a transaction object, so there cannot be inconsitencies.
     */
    @SuppressWarnings("unchecked")
    private void changeDelegate(InheritanceTree st) {
        delegate = (AbstractLookup.Storage<Transaction>)st;
    }

    public Transaction beginTransaction(int ensure) {
        try {
            return delegate.beginTransaction(ensure);
        } catch (UnsupportedOperationException ex) {
            // let's convert to InheritanceTree
            ArrayStorage arr = (ArrayStorage) delegate;
            InheritanceTree inh = new InheritanceTree();
            changeDelegate(inh);

            //
            // Copy content
            //
            Enumeration<Pair<Object>> en = arr.lookup(Object.class);

            while (en.hasMoreElements()) {
                if (!inh.add(en.nextElement(), new ArrayList<Class>())) {
                    throw new IllegalStateException("All objects have to be accepted"); // NOI18N
                }
            }

            //
            // Copy listeners
            //
            AbstractLookup.ReferenceToResult<?> ref = arr.cleanUpResult(null);

            if (ref != null) {
                ref.cloneList(inh);
            }

            // we have added the current content and now we can start transaction
            return delegate.beginTransaction(ensure);
        }
    }

    public org.openide.util.lookup.AbstractLookup.ReferenceToResult cleanUpResult(
        org.openide.util.Lookup.Template templ
    ) {
        return delegate.cleanUpResult(templ);
    }

    public void endTransaction(Transaction transaction, Set<AbstractLookup.R> modified) {
        delegate.endTransaction(transaction, modified);
    }

    public <T> Enumeration<Pair<T>> lookup(Class<T> clazz) {
        return delegate.lookup(clazz);
    }

    public org.openide.util.lookup.AbstractLookup.ReferenceToResult registerReferenceToResult(
        org.openide.util.lookup.AbstractLookup.ReferenceToResult newRef
    ) {
        return delegate.registerReferenceToResult(newRef);
    }

    @Override
    public <T> Lookup.Result<T> findResult(Lookup.Template<T> template) {
        return delegate.findResult(template);
    }
}
