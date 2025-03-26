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
package org.netbeans.core.startup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import org.netbeans.Util;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class NbStartStop implements LookupListener {
    private static final RequestProcessor RP = new RequestProcessor("On Start/Stop", 8); // NOI18N
    
    private final Map<String, RequestProcessor.Task> onStart = new HashMap<String, RequestProcessor.Task>();
    private final Lookup lkpStart;
    private final Lookup lkpStop;
    
    private Lookup.Result<Runnable> resStart;
    

    NbStartStop(Lookup lkp, Lookup lkp2) {
        lkpStart = lkp;
        lkpStop = lkp2;
    }
    
    void initialize() {
        for (Lookup.Item<Runnable> item : onStart().allItems()) {
            synchronized (onStart) {
                RequestProcessor.Task already = onStart.get(item.getId());
                if (already == null) {
                    Runnable r = item.getInstance();
                    if (r != null) {
                        onStart.put(item.getId(), RP.post(r));
                    }
                }
            }
        }
        
    }

    private synchronized Lookup.Result<Runnable> onStart() {
        if (resStart == null) {
            Lookup lkp = lkpStart != null ? lkpStart : Lookups.forPath("Modules/Start"); // NOI18N
            resStart = lkp.lookupResult(Runnable.class);
            resStart.addLookupListener(this);
        }
        return resStart;
    }
    private Lookup onStop() {
        return lkpStop != null ? lkpStop : Lookups.forPath("Modules/Stop"); // NOI18N
    }

    void waitOnStart() {
        RequestProcessor.Task[] all;
        synchronized (onStart) {
            Collection<RequestProcessor.Task> values = onStart.values();
            all = values.toArray(new RequestProcessor.Task[0]);
        }
        for (RequestProcessor.Task t : all) {
            t.waitFinished();
        }
    }
    
    boolean closing(List<? extends ModuleInfo> modules) {
        for (Callable<?> c : onStop().lookupAll(Callable.class)) { // NOI18N
            if (!modules.contains(Modules.getDefault().ownerOf(c.getClass()))) {
                continue;
            }
            try {
                if (Boolean.FALSE.equals(c.call())) {
                    Util.err.log(Level.FINE, "{0} refused to close", c.getClass()); // NOI18N
                    return false;
                }
            } catch (Exception ex) {
                Util.err.log(Level.FINE, c.getClass() + " thrown an exception", ex); // NOI18N
                return false;
            }
        }
        return true;
    }

    List<Task> startClose(List<? extends ModuleInfo> modules) {
        List<Task> waitFor = new ArrayList<Task>();
        for (Runnable r : onStop().lookupAll(Runnable.class)) { // NOI18N
            if (!modules.contains(Modules.getDefault().ownerOf(r.getClass()))) {
                continue;
            }
            waitFor.add(RP.post(r));
        }
        return waitFor;
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        initialize();
    }
}
