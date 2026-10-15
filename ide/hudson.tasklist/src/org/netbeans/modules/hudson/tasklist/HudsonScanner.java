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

package org.netbeans.modules.hudson.tasklist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.ui.spi.ProjectHudsonProvider;
import static org.netbeans.modules.hudson.tasklist.Bundle.*;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=PushTaskScanner.class, path="TaskList/Scanners")
public class HudsonScanner extends PushTaskScanner implements Runnable {

    private static final Logger LOG = Logger.getLogger(HudsonScanner.class.getName());

    private RequestProcessor.Task task;
    private Thread taskThread;
    private TaskScanningScope scope;
    private Callback callback;
    private static class CacheEntry {
        String jobUrl;
        int lastSuccessfulBuild;
        List<Task> tasks;
    }
    private final Map<Project,CacheEntry> cache = new WeakHashMap<Project,CacheEntry>();

    @Messages({
        "HudsonScanner.displayName=Jenkins Warnings",
        "HudsonScanner.description=Warnings and other action items coming from Jenkins servers, currently supporting the Static Analysis plugin suite."
    })
    public HudsonScanner() {
        super(HudsonScanner_displayName(), HudsonScanner_description(), null);
    }

    @Override public void setScope(TaskScanningScope scope, Callback callback) {
        try {
            // XXX make Installer.active into API
            if (!NbPreferences.root().nodeExists("org/netbeans/modules/hudson/instances")) {
                return; // avoid loading any more classes
            }
        } catch (BackingStoreException x) {
            LOG.log(Level.INFO, null, x);
        }
        doSetScope(scope, callback);
    }
    // in a separate method to avoid resolving anything else if shortcut above is used
    private void doSetScope(TaskScanningScope scope, Callback callback) {
        if (task == null) {
            task = new RequestProcessor(HudsonScanner.class).create(this);
            task.setPriority(Thread.MIN_PRIORITY);
        }
        if (scope == null) {
            LOG.fine("canceling scan");
            task.cancel();
            if (taskThread != null) {
                taskThread.interrupt();
            }
            return;
        }
        this.scope = scope;
        this.callback = callback;
        // XXX also support individual FileObject scope
        if (scope.getLookup().lookup(Project.class) == null) { // shortcut
            LOG.finer("no projects to scan");
            callback.clearAllTasks();
            return;
        }
        LOG.fine("scheduling scan");
        task.schedule(1000);
    }

    @Override public void run() {
        callback.started();
        taskThread = Thread.currentThread();
        try {
            final List<Task> tasks = new ArrayList<Task>();
            for (Project p : scope.getLookup().lookupAll(Project.class)) {
                if (Thread.interrupted()) {
                    return;
                }
                ProjectHudsonProvider.Association assoc = ProjectHudsonProvider.getDefault().findAssociation(p);
                if (assoc == null) {
                    continue;
                }
                HudsonJob job = assoc.getJob();
                if (job == null) {
                    continue;
                }
                CacheEntry entry = cache.get(p);
                if (entry == null) {
                    entry = new CacheEntry();
                    cache.put(p, entry);
                }
                List<Task> cachedTasks = entry.tasks;
                int lastSuccessfulBuild = job.getLastSuccessfulBuild();
                String jobUrl = job.getUrl();
                if (cachedTasks == null || lastSuccessfulBuild != entry.lastSuccessfulBuild || !jobUrl.equals(entry.jobUrl)) { // cache miss
                    final List<Task> newTasks = new ArrayList<Task>();
                    for (JobScanner s : Lookup.getDefault().lookupAll(JobScanner.class)) {
                        if (Thread.interrupted()) {
                            return;
                        }
                        try {
                            final AtomicInteger count = new AtomicInteger();
                            s.findTasks(p, job, lastSuccessfulBuild, new JobScanner.TaskAdder() {
                                @Override public void add(Task task) {
                                    count.incrementAndGet();
                                    newTasks.add(task);
                                    tasks.add(task);
                                    callback.setTasks(tasks);
                                }
                            });
                            LOG.log(Level.FINE, "discovered {0} tasks for {1} from {2}", new Object[] {count, p, s});
                        } catch (IOException x) {
                            LOG.log(Level.INFO, "from " + assoc, x);
                        } catch (RuntimeException x) {
                            LOG.log(Level.WARNING, "from " + assoc, x);
                        }
                    }
                    entry.tasks = newTasks;
                    entry.lastSuccessfulBuild = lastSuccessfulBuild;
                    entry.jobUrl = jobUrl;
                } else { // cache hit
                    LOG.log(Level.FINE, "using {0} cached tasks for {1}", new Object[] {cachedTasks.size(), p});
                    tasks.addAll(cachedTasks);
                    callback.setTasks(tasks);
                }
            }
        } finally {
            taskThread = null;
            callback.finished();
        }
    }

}
