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

import java.util.WeakHashMap;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * Holds registered scans which can be delayed and thus not interfere with openning projects or indexing
 * @author ondra
 */
public final class DelayScanRegistry {
    private final WeakHashMap<RequestProcessor.Task, DelayedScan> registry;
    private static DelayScanRegistry instance;
    private static int MAX_WAITING_TIME = 180000; // wait max 3 mins
    private static int WAITING_PERIOD = 10000;
    private static final boolean BLOCK_INDEFINITELY = "true".equals(System.getProperty("versioning.delayscan.nolimit", "false")); //NOI18N

    public static synchronized DelayScanRegistry getInstance() {
        if (instance == null) {
            instance = new DelayScanRegistry();
        }
        return instance;
    }

    private DelayScanRegistry () {
        registry = new WeakHashMap<Task, DelayedScan>(5);
    }

    /**
     * Delays given task if neccessary - e.g. projects are currently openning - and reschedules the task if indexing is running
     * This method waits for projects to open and thus blocks the current thread.
     * @param task task to be delayed
     * @param logger 
     * @param logMessagePrefix
     * @return true if the task was rescheduled
     */
    public boolean isDelayed (RequestProcessor.Task task, Logger logger, String logMessagePrefix) {
        boolean rescheduled = false;
        DelayedScan scan = getRegisteredScan(task);
        Future<Project[]> projectOpenTask = OpenProjects.getDefault().openProjects();
        if (!projectOpenTask.isDone()) {
            try {
                projectOpenTask.get();
            } catch (Exception ex) {
                // not interested
            }
        }
        if (IndexingBridge.getInstance().isIndexingInProgress()
                && (BLOCK_INDEFINITELY || scan.waitingLoops * WAITING_PERIOD < MAX_WAITING_TIME)) {
            // do not steal disk from openning projects and indexing tasks
            Level level = ++scan.waitingLoops < 10 ? Level.FINE : Level.INFO;
            logger.log(level, "{0}: Scanning in progress, trying again in {1}ms", new Object[]{logMessagePrefix, WAITING_PERIOD}); //NOI18N
            task.schedule(WAITING_PERIOD); // try again later
            rescheduled = true;
        } else {
            scan.waitingLoops = 0;
        }
        return rescheduled;
    }

    private DelayedScan getRegisteredScan(Task task) {
        synchronized (registry) {
            DelayedScan scan = registry.get(task);
            if (scan == null) {
                registry.put(task, scan = new DelayedScan());
            }
            return scan;
        }
    }

    private static class DelayedScan {
        private int waitingLoops;
    }
}
