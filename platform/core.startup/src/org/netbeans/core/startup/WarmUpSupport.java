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

package org.netbeans.core.startup;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.lookup.Lookups;

/**
 * This class controls "warm-up" initialization after IDE startup (some time
 * after main window is shown). It scans WarmUp folder for individual tasks
 * to be performed. The tasks should be instance objects implementing Runnable.
 *
 * The tasks may be provided by modules via xml layer.
 *
 * @author Tomas Pavek
 */

final class WarmUpSupport implements Runnable {
    private static final RequestProcessor.Task TASK;
    static {
        RequestProcessor RP = new RequestProcessor("Warm Up");
        TASK = RP.create(new WarmUpSupport(), true);
    } // NOI18N

    private static final Logger err = Logger.getLogger("org.netbeans.core.WarmUpSupport");

    static Task warmUp(long delay) {
        TASK.schedule((int)delay);
        return TASK;
    }
    
    static Task waitTask() {
        return TASK;
    }

    // -------

    @Override
    public void run() {
        err.fine("Warmup starting..."); // NOI18N
        StartLog.logStart("Warmup"); // NOI18N
        try {

        Collection<? extends Lookup.Item<Runnable>> warmObjects =
            Lookups.forPath("WarmUp").lookupResult(Runnable.class).allItems(); // NOI18N
        err.log(Level.FINE, "Found {0} warm up task(s)", warmObjects.size()); // NOI18N

        for (Lookup.Item<Runnable> warmer : warmObjects) {
            try {
                Runnable r = warmer.getInstance();
                if (r == null) {
                  err.log(Level.WARNING,
                      "Got null warmup task from lookup: id={0}, displayName={1}, class={2}, type={3}",
                      new Object[] {
                      warmer.getId(), warmer.getDisplayName(), warmer.getClass(), warmer.getType() });
                } else {
                  r.run();
                  StartLog.logProgress("Warmup task executed " + warmer.getId()); // NOI18N
                }
            } catch (Exception ex) {
                Logger.getLogger(WarmUpSupport.class.getName()).log(Level.WARNING, null, ex);
            }
        }
        err.fine("Warmup done."); // NOI18N
        } finally {
        StartLog.logEnd("Warmup"); // NOI18N
        StartLog.impl.flush();
        }
    }
}
