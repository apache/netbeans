/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
        "HudsonScanner.displayName=Hudson Warnings",
        "HudsonScanner.description=Warnings and other action items coming from Hudson servers, currently supporting the Static Analysis plugin suite."
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
