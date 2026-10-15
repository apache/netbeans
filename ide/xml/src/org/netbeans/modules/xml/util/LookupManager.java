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
package org.netbeans.modules.xml.util;

import java.util.*;

import org.openide.util.*;

/**
 *
 * @author Libor Kramolis, Jesse Glick
 * @version 0.2
 */
public abstract class LookupManager {
    /** */
    private static final Map<Class,Handle> handles = new WeakHashMap<Class,Handle>();

    /** */
    private Handle handle = null;


    //
    // init
    //

    /**
     * Create new LookupManager. Call register() when ready.
     */
    public LookupManager () {
    }


    /** To be called when it is fully initialized and ready to receive events.
     * Subclasses may wish to call addedToResult (getResults()) immediately.
     */
    protected final void register (Class clazz) {
        if ( handle != null ) {
            throw new IllegalStateException();
        }
        synchronized (handles) {
            handle = (Handle)handles.get (clazz);
            if ( handle == null ) {
                handles.put (clazz, handle = new Handle (clazz));
            }
        }
        handle.register (this);
    }


    //
    // itself
    //

    /**
     */
    protected final Collection getResult() {
        return handle.getInstances();
    }


    /**
     */
    protected abstract void removedFromResult (Collection removed);

    /**
     */
    protected abstract void addedToResult (Collection added);


    //
    // class Handle
    //

    /**
     *
     */
    private static final class Handle implements LookupListener {

        private final Class clazz;
        // @GuardedBy(this)
        private Lookup.Result lookupResult = null;
        // @GuardedBy(this)
        private Collection lastResult = null;
        // @GuardedBy(self)
        private final Set<LookupManager> lms = Collections.newSetFromMap(new WeakHashMap<>(300));

        //
        // init
        //

        /**
         */
        private Handle (Class clazz) {
            this.clazz = clazz;
        }

        /**
         */
        public void register (LookupManager lm) {
            synchronized (lms) {
                lms.add (lm);
            }
        }

        
        //
        // itself
        //
        
        /**
         */
        // @GuardedBy(this)
        private Lookup.Result getLookupResult () {
            if ( lookupResult == null ) {
                lookupResult = (Lookup.getDefault()).lookup (new Lookup.Template (clazz));
                lookupResult.addLookupListener (this);
            }
            return lookupResult;
        }

        /**
         */
        public void resultChanged (LookupEvent evt) {
            final Collection removed;
            final Collection added;
            
            synchronized (this) {
                Collection currentResult = getLookupResult().allInstances();
                removed = new HashSet (lastResult);
                removed.removeAll (currentResult);
                added = new HashSet (currentResult);
                added.removeAll (lastResult);
                lastResult = currentResult;
                
                // guarantee delivery order, since diffs{added,removed} must be
                // delivered in the proper order to give a consistent view.
                CHANGES.post(new Runnable() {
                    @Override
                    public void run() {
                        if ( ( removed.isEmpty() == false ) ||
                            ( added.isEmpty() == false ) ) {
                            synchronized (lms) {
                                Iterator it = lms.iterator();
                                while (it.hasNext()) {
                                    LookupManager lm = (LookupManager)it.next();
                                    if ( removed.isEmpty() == false ) {
                                        lm.removedFromResult(removed);
                                    }
                                    if ( added.isEmpty() == false ) {
                                        lm.addedToResult(added);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }

        /**
         */
        public synchronized Collection getInstances() {
            //!!! can we use caching? I'm affraid we cannot because
            // lookup callbakcs are asynchronous so we can miss some
            // registrations (it may be crucuial for cookies)
            if (lastResult == null) {
                lastResult = getLookupResult().allInstances();
            }
            return lastResult;
        }

    } // end: class Handle
    
    private static final RequestProcessor CHANGES = new RequestProcessor("Lookup changes", 1); // NOI18N
    
}
