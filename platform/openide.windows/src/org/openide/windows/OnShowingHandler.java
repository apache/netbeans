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
package org.openide.windows;

import java.util.HashSet;
import java.util.Set;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class OnShowingHandler implements LookupListener, Runnable {
    private final Set<String> onShowing = new HashSet<String>();
    private final Lookup lkpShowing;
    private final WindowManager wm;
    private Lookup.Result<Runnable> resShow;
    

    OnShowingHandler(Lookup lkp, WindowManager wm) {
        lkpShowing = lkp;
        this.wm = wm;
    }
    
    void initialize() {
        for (Lookup.Item<Runnable> item : onShowing().allItems()) {
            synchronized (onShowing) {
                if (onShowing.add(item.getId())) {
                    Runnable r = item.getInstance();
                    if (r != null) {
                        wm.invokeWhenUIReady(r);
                    }
                }
            }
        }
        
    }

    private synchronized Lookup.Result<Runnable> onShowing() {
        if (resShow == null) {
            Lookup lkp = lkpShowing != null ? lkpShowing : Lookups.forPath("Modules/UIReady"); // NOI18N
            resShow = lkp.lookupResult(Runnable.class);
            resShow.addLookupListener(this);
        }
        return resShow;
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        initialize();
    }

    @Override
    public void run() {
        initialize();
    }
}
