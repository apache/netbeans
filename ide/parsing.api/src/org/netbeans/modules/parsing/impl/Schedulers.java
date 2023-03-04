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

package org.netbeans.modules.parsing.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.netbeans.modules.parsing.spi.Scheduler;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;


/**
 *
 * @author Jan Jancura
 */
public class Schedulers {

    //@GuardedBy("Schedulers.class")
    private Collection<? extends Scheduler> taskSchedulers;
    //@GuardedBy("Scheduler.class")
    private Lookup.Result<Scheduler> result;
    //@GuardedBy("Scheduler.class")
    private LookupListener listener;
    
    private Lookup lookup;
    
    private static final Schedulers INSTANCE = new Schedulers(null);
    
    private Schedulers(Lookup lookup) {
        this.lookup = lookup;
    }
    
    synchronized void init () {
        if (taskSchedulers == null) {
            if (result == null) {
                assert listener == null;
                listener = new LkpListener();
                result = (lookup == null ?Lookup.getDefault() : lookup).lookupResult(Scheduler.class);
                result.addLookupListener(WeakListeners.create(
                    LookupListener.class,
                    listener,
                    result));
            }
            taskSchedulers = Collections.unmodifiableCollection(
                    new ArrayList<Scheduler>(result.allInstances()));
        }
    }
    
    public static Schedulers getInstance(Lookup lkp) {
        return new Schedulers(lkp);
    }
    
    public static Collection<? extends Scheduler> getSchedulers () {
        Schedulers inst = INSTANCE;
        return inst.getSchedulerList();
    }

    /**
     * For tests only.
     */
    public Collection<? extends Scheduler> getSchedulerList() {
        init();
        return taskSchedulers;
    }

    private class LkpListener implements LookupListener {
        @Override
        public void resultChanged(LookupEvent ev) {
            synchronized (Schedulers.class) {
                taskSchedulers = null;
            }
        }
    }
}



