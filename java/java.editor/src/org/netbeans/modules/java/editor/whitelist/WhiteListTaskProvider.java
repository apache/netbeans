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
package org.netbeans.modules.java.editor.whitelist;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.netbeans.api.whitelist.index.WhiteListIndex;
import org.netbeans.api.whitelist.index.WhiteListIndexEvent;
import org.netbeans.api.whitelist.index.WhiteListIndexListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;

/**
 *
 * @author Tomas Zezula
 */
public class WhiteListTaskProvider extends  PushTaskScanner {

    private static final RequestProcessor WORKER = new RequestProcessor(WhiteListTaskProvider.class);
    private static final Logger LOG = Logger.getLogger(WhiteListTaskProvider.class.getName());
    //@GuardedBy("TASKS")
    private static final Map<RequestProcessor.Task,Work> TASKS = new HashMap<RequestProcessor.Task, Work>();
    //@GuardedBy("root2FilesWithAttachedErrors")
    private static final Map<FileObject, Set<FileObject>> root2FilesWithAttachedErrors = new WeakHashMap<FileObject, Set<FileObject>>();
    //@GuardedBy("TASKS")
    private static boolean clearing;

    //@GuardedBy("this")
    private Callback currentCallback;
    //@GuardedBy("this")
    private Set<FileObject> currentFiles;

    @NbBundle.Messages({
        "LBL_ProviderName=White lists violations",
        "LBL_ProviderDescription=Lists violations of white list rules"
    })
    @SuppressWarnings("LeakingThisInConstructor")
    public WhiteListTaskProvider() {
        super(Bundle.LBL_ProviderName(),
              Bundle.LBL_ProviderDescription(),
              null);
        WhiteListIndex.getDefault().addWhiteListIndexListener(new WhiteListIndexListener() {
            @Override
            public void indexChanged(WhiteListIndexEvent event) {
                refresh(event.getRoot());
            }
        });
    }

    @Override
    public void setScope(TaskScanningScope scope, Callback callback) {
        cancelAllCurrent();
        if (scope == null || callback == null)
            return ;

        final Set<FileObject> files = Collections.newSetFromMap(new WeakHashMap<>());
        for (FileObject file : scope.getLookup().lookupAll(FileObject.class)) {
            files.add(file);
        }

        for (Project p : scope.getLookup().lookupAll(Project.class)) {
            for (SourceGroup javaSG : ProjectUtils.getSources(p).getSourceGroups("java")) {    //NOI18N
                files.add(javaSG.getRootFolder());
            }
        }
        for (FileObject fo : files) {
            enqueue(new Work(fo, callback));
        }
        synchronized (this) {
            currentFiles = files;
            currentCallback = callback;
        }
    }

    private void refresh(final @NonNull URL root) {
        assert root != null;
        final FileObject rootFo = URLMapper.findFileObject(root);
        final Set<FileObject> files;
        final Callback callback;
        synchronized (this) {
            files = currentFiles;
            callback = currentCallback;
        }
        if (rootFo != null &&
            files != null &&
            files.contains(rootFo)) {
            assert callback != null;
            enqueue(new Work(rootFo, currentCallback));
        }
    }

    private static void enqueue(Work w) {
        synchronized (TASKS) {
            final RequestProcessor.Task task = WORKER.post(w);
            TASKS.put(task,w);
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
        }
    }

    private static void cancelAllCurrent() {
        synchronized (TASKS) {
            clearing = true;
            try {
                for (final Iterator<Map.Entry<RequestProcessor.Task,Work>> it =  TASKS.entrySet().iterator();
                     it.hasNext();) {
                    final Map.Entry<RequestProcessor.Task,Work> t = it.next();
                    t.getKey().cancel();
                    t.getValue().cancel();
                    it.remove();
                }
            } finally {
                clearing = false;
            }
        }
        synchronized (root2FilesWithAttachedErrors) {
            root2FilesWithAttachedErrors.clear();
        }
    }

    private static Set<FileObject> getFilesWithAttachedErrors(FileObject root) {
        synchronized (root2FilesWithAttachedErrors) {
            Set<FileObject> result = root2FilesWithAttachedErrors.get(root);
            if (result == null) {
                root2FilesWithAttachedErrors.put(root, result = Collections.newSetFromMap(new WeakHashMap<>()));
            }
            return result;
        }
    }

    @CheckForNull
    private static Map.Entry<FileObject,List<? extends Task>> createTask(@NonNull final WhiteListIndex.Problem problem) {
        final FileObject file = problem.getFile();
        if (file == null) {
            return null;
        }
        final WhiteListQuery.Result result = problem.getResult();
        assert result != null;
        assert !result.isAllowed() : problem;
        final List<Task> tasks = new ArrayList<Task>(result.getViolatedRules().size());
        for (WhiteListQuery.RuleDescription ruleDesc : result.getViolatedRules()) {
            tasks.add(Task.create(
                file,
                "nb-whitelist-warning", //NOI18N
                ruleDesc.getRuleDescription(),
                problem.getLine()));
        }
        return new Map.Entry<FileObject, List< ? extends Task>>() {
                @Override
                public FileObject getKey() {
                    return file;
                }
                @Override
                public List<? extends Task> getValue() {
                    return tasks;
                }

                @Override
                public List<? extends Task> setValue(List<? extends Task> value) {
                    throw new UnsupportedOperationException();
                }
            };
    }

    private static void updateErrorsInRoot(
            @NonNull final Callback callback,
            @NonNull final FileObject root,
            @NonNull final AtomicBoolean canceled) {
        Set<FileObject> filesWithErrors = getFilesWithAttachedErrors(root);
        Set<FileObject> fixedFiles = new HashSet<FileObject>(filesWithErrors);
        filesWithErrors.clear();
        Set<FileObject> nueFilesWithErrors = new HashSet<FileObject>();
        final Map<FileObject,List<Task>> filesToTasks = new HashMap<FileObject,List<Task>>();
        for (WhiteListIndex.Problem problem : WhiteListIndex.getDefault().getWhiteListViolations(root, null)) {
            if (canceled.get()) {
                return;
            }
            final Map.Entry<FileObject,List<? extends Task>> task = createTask(problem);
            if (task != null) {
                List<Task> tasks = filesToTasks.get(task.getKey());
                if (tasks == null) {
                    tasks = new ArrayList<Task>();
                    filesToTasks.put(task.getKey(), tasks);
                }
                tasks.addAll(task.getValue());
            }
        }
        for (Map.Entry<FileObject,List<Task>> e : filesToTasks.entrySet()) {
            LOG.log(Level.FINE, "Setting {1} for {0}\n",
                    new Object[] {e.getKey(), e.getValue()});
                    callback.setTasks(e.getKey(), e.getValue());
            if (!fixedFiles.remove(e.getKey())) {
                nueFilesWithErrors.add(e.getKey());
            }
        }
        for (FileObject f : fixedFiles) {
            LOG.log(Level.FINE, "Clearing errors for {0}", f);
            callback.setTasks(f, Collections.<Task>emptyList());
        }
        filesWithErrors.addAll(nueFilesWithErrors);
    }

    private static void updateErrorsInFile(
        @NonNull final Callback callback,
        @NonNull final FileObject root,
        @NonNull final FileObject file) {
        final List<Task> tasks = new ArrayList<Task>();
        for (WhiteListIndex.Problem problem : WhiteListIndex.getDefault().getWhiteListViolations(root, file)) {
            final Map.Entry<FileObject,List<? extends Task>> task = createTask(problem);
            if (task != null) {
                tasks.addAll(task.getValue());
            }
        }
        final Set<FileObject> filesWithErrors = getFilesWithAttachedErrors(root);
        if (tasks.isEmpty()) {
            filesWithErrors.remove(file);
        } else {
            filesWithErrors.add(file);
        }
        LOG.log(Level.FINE, "setting {1} for {0}", new Object[]{file, tasks});
        callback.setTasks(file, tasks);
    }

    private static final class Work implements Runnable {
        private final FileObject fileOrRoot;
        private final Callback callback;
        private final AtomicBoolean canceled;

        public Work(FileObject fileOrRoot, Callback callback) {
            this.fileOrRoot = fileOrRoot;
            this.callback = callback;
            this.canceled = new AtomicBoolean();
        }

        @Override
        public void run() {

            LOG.log(Level.FINE, "dequeued work for: {0}", fileOrRoot);
            final ClassPath cp = ClassPath.getClassPath(fileOrRoot, ClassPath.SOURCE);
            if (cp == null) {
                LOG.log(Level.FINE, "cp == null");
                return;
            }
            FileObject root = cp.findOwnerRoot(fileOrRoot);

            if (root == null) {
                Project p = FileOwnerQuery.getOwner(fileOrRoot);

                LOG.log(Level.WARNING,
                        "file: {0} is not on its own source classpath: {1}, project: {2}",
                        new Object[] {
                            FileUtil.getFileDisplayName(fileOrRoot),
                            cp.toString(ClassPath.PathConversionMode.PRINT),
                            p != null ? p.getClass() : "null"
                        });

                return ;
            }

            if (fileOrRoot.isData()) {
                updateErrorsInFile(callback, root, fileOrRoot);
            } else {
                updateErrorsInRoot(callback, root, canceled);
            }
        }

        private void cancel() {
            canceled.set(true);
        }
    }

}
