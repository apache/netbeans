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
 * Holds registered scans which can be delayed and thus not interfere with opening projects or indexing
 * @author ondra
 */
public final class DelayScanRegistry {
    private final WeakHashMap<RequestProcessor.Task, DelayedScan> registry;
    private static DelayScanRegistry instance;
    private static final int MAX_WAITING_TIME = 180000; // wait max 3 mins
    private static final int WAITING_PERIOD = 10000;
    private static final boolean BLOCK_INDEFINITELY = Boolean.getBoolean("versioning.delayscan.nolimit"); //NOI18N
    private static final boolean DELAY_SCAN = Boolean.getBoolean("versioning.delayscan"); //NOI18N

    public static synchronized DelayScanRegistry getInstance() {
        if (instance == null) {
            instance = new DelayScanRegistry();
        }
        return instance;
    }

    private DelayScanRegistry() {
        registry = new WeakHashMap<>(5);
    }

    /**
     * Delays given task if necessary - e.g. projects are currently opening - and reschedules the task if indexing is running
     * This method waits for projects to open and thus blocks the current thread.
     * @param task task to be delayed
     * @param logger 
     * @param logMessagePrefix
     * @return true if the task was rescheduled
     */
    public boolean isDelayed(RequestProcessor.Task task, Logger logger, String logMessagePrefix) {
        DelayedScan scan = getRegisteredScan(task);
        Future<Project[]> projectOpenTask = OpenProjects.getDefault().openProjects();
        if (!projectOpenTask.isDone()) {
            try {
                projectOpenTask.get();
            } catch (Exception ex) {
                // not interested
            }
        }
        if (DELAY_SCAN && IndexingBridge.getInstance().isIndexingInProgress()
                && (BLOCK_INDEFINITELY || scan.waitingLoops * WAITING_PERIOD < MAX_WAITING_TIME)) {
            // do not steal disk from openning projects and indexing tasks
            Level level = ++scan.waitingLoops < 10 ? Level.FINE : Level.INFO;
            logger.log(level, "{0}: Scanning in progress, trying again in {1}ms", new Object[]{logMessagePrefix, WAITING_PERIOD}); //NOI18N
            task.schedule(WAITING_PERIOD); // try again later
            return true;
        } else {
            scan.waitingLoops = 0;
        }
        return false;
    }

    private DelayedScan getRegisteredScan(Task task) {
        synchronized (registry) {
            return registry.computeIfAbsent(task, k -> new DelayedScan());
        }
    }

    private static class DelayedScan {
        private int waitingLoops;
    }
}
