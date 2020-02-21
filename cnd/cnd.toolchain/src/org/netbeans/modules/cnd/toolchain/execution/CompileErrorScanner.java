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
package org.netbeans.modules.cnd.toolchain.execution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.cnd.spi.toolchain.ErrorParserProvider;
import org.netbeans.modules.cnd.spi.toolchain.OutputListenerExt;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.OutputListener;

/**
 *
 */
@ServiceProvider(service = PushTaskScanner.class, path = "TaskList/Scanners")
public class CompileErrorScanner extends PushTaskScanner {

    private static final Logger LOG = Logger.getLogger(CompileErrorScanner.class.getName());
    private static final Map<RequestProcessor.Task, Work> TASKS = new HashMap<RequestProcessor.Task, Work>();
    private static boolean clearing;
    private static final RequestProcessor RP = new RequestProcessor("CND compiler scanner"); //NOI18N
    private static CompileErrorScanner INSTANCE;
    private static final WeakHashMap<Project, ErrorParserProvider.OutputListenerRegistry> storage = new WeakHashMap<Project, ErrorParserProvider.OutputListenerRegistry>();
    private TaskScanningScope scope;
    private Callback callback;

    public CompileErrorScanner() {
        super(
                NbBundle.getMessage(CompileErrorScanner.class, "CompileErrorTasks"), //NOI18N
                NbBundle.getMessage(CompileErrorScanner.class, "CompileErrorTasksDesc"), //NOI18N
                null);
        INSTANCE = this;
    }

    public static void attach(ErrorParserProvider.OutputListenerRegistry registry, Project project) {
        synchronized(storage) {
            // TODO: What is about to keep errors in the file storage?
            // Current behaviour: errors are avalible omly after build in the IDE
            storage.put(project, registry);
        }
        CompileErrorScanner.refreshAll();
    }

    private static void refreshAll() {
        if (INSTANCE != null) {
            synchronized (INSTANCE) {
                INSTANCE.setScope(INSTANCE.scope, INSTANCE.callback);
            }
        }
    }

    @Override
    public synchronized void setScope(TaskScanningScope scope, Callback callback) {
        //cancel all current operations:
        cancelAllCurrent();

        this.scope = scope;
        this.callback = callback;

        if (scope == null || callback == null) {
            return;
        }

        for (FileObject file : scope.getLookup().lookupAll(FileObject.class)) {
            enqueue(new Work(file, callback));
        }

        for (Project p : scope.getLookup().lookupAll(Project.class)) {
            for (SourceGroup generic : ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC)) {
                enqueue(new Work(generic.getRootFolder(), callback));
            }
        }
    }

    private static void enqueue(Work w) {
        synchronized (TASKS) {
            final RequestProcessor.Task task = RP.post(w);
            TASKS.put(task, w);
            task.addTaskListener(new TaskListener() {
                @Override
                public void taskFinished(org.openide.util.Task task) {
                    synchronized (TASKS) {
                        if (!clearing) {
                            TASKS.remove(task);
                        }
                    }
                }
            });
            if (task.isFinished()) {
                TASKS.remove(task);
            }
        }
    }

    private static void cancelAllCurrent() {
        synchronized (TASKS) {
            clearing = true;
            try {
                for (Map.Entry<RequestProcessor.Task, Work> t : TASKS.entrySet()) {
                    t.getKey().cancel();
                    t.getValue().cancel();
                }
                TASKS.clear();
            } finally {
                clearing = false;
            }
        }
    }

    //only for tests:
    static void waitWorkFinished() throws Exception {
        while (true) {
            RequestProcessor.Task t;
            synchronized (TASKS) {
                if (TASKS.isEmpty()) {
                    return;
                }
                t = TASKS.keySet().iterator().next();
            }

            t.waitFinished();
        }
    }

    private static final class Work implements Runnable {

        private final FileObject fileOrRoot;
        private final Callback callback;
        private final AtomicBoolean canceled = new AtomicBoolean();

        public Work(FileObject fileOrRoot, Callback callback) {
            this.fileOrRoot = fileOrRoot;
            this.callback = callback;
        }

        public FileObject getFileOrRoot() {
            return fileOrRoot;
        }

        public Callback getCallback() {
            return callback;
        }

        public void cancel() {
            canceled.set(true);
        }

        @Override
        public void run() {
            FileObject file = getFileOrRoot();

            LOG.log(Level.FINE, "dequeued work for: {0}", file);

            Project p = FileOwnerQuery.getOwner(file);
            if (p == null) {
                LOG.log(Level.FINE, "project == null");
                return;
            }
            ErrorParserProvider.OutputListenerRegistry aRegestry;
            synchronized(storage) {
                aRegestry = storage.get(p);
            }
            List<Task> tasks = new ArrayList<Task>();
            if (aRegestry != null) {
                getCallback().started();
                getCallback().clearAllTasks();
                LOG.log(Level.FINE, "setting {1} for {0}", new Object[]{file, tasks});
                Set<OutputListener> fileListeners = aRegestry.getFileListeners(file);
                if (fileListeners != null) {
                    for (OutputListener listener : fileListeners) {
                        if (listener instanceof OutputListenerExt) {
                            OutputListenerExt impl = (OutputListenerExt) listener;
                            String type;
                            if (impl.isError()) {
                                type = "nb-tasklist-error"; //NOI18N
                            } else {
                                type = "nb-tasklist-warning"; //NOI18N
                            }
                            Task task = Task.create(impl.getFile(), type, impl.getDescription(), impl.getLine()+1);
                            tasks.add(task);
                        }
                    }
                }
                getCallback().setTasks(file, tasks);
                getCallback().finished();
            }
        }
    }
}
