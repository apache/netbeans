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
package org.netbeans.modules.cnd.highlight;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner.Callback;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service=PushTaskScanner.class, path="TaskList/Scanners")
public class CndTodoScanner extends PushTaskScanner {
    private final RequestProcessor RP = new RequestProcessor("CND todo scanner", 1); //NOI18N
    private final RequestProcessor.Task scanTask;
    private final FileTaskScanner todoScanner;
    private final AtomicReference<ScanJob> jobRef = new AtomicReference<>();
        
    public CndTodoScanner() {
        super(
            NbBundle.getMessage(CndTodoScanner.class, "CndTodoTasks"), //NOI18N
            NbBundle.getMessage(CndTodoScanner.class, "CndTodoTasksDesc"), //NOI18N
            null);  //NOI18N
        todoScanner = getTodoScanner();
        scanTask = RP.create(new Runnable() { //NOI18N
            @Override
            public void run() {
                final ScanJob job = jobRef.getAndSet(null);
                if (job == null) {
                    return;
                }
                for (FileObject fileObject : job.files) {
                    if (jobRef.get() != null) {
                        return;
                    }
                    List<? extends Task> newTasks = todoScanner.scan(fileObject);
                    if (newTasks != null) {
                        job.callback.setTasks(fileObject, newTasks);
                    }
                }
            }
        });
    }
    
    private static class ScanJob {
        private final Set<FileObject> files;
        private final Callback callback;

        public ScanJob(Set<FileObject> files, Callback callback) {
            this.files = files;
            this.callback = callback;
        }
    }
    
    private FileTaskScanner getTodoScanner() {
        // workaround for implementation dependecy to tasklist.todo
        Lookup lkp = Lookups.forPath("TaskList/Scanners"); //NOI18N
        Collection<? extends FileTaskScanner> scanners = lkp.lookupAll(FileTaskScanner.class);
        for (FileTaskScanner fileTaskScanner : scanners) {
            if (fileTaskScanner.getClass().getName().equals("org.netbeans.modules.tasklist.todo.TodoTaskScanner")) { //NOI18N
                return fileTaskScanner;
            }
        }
        assert false : "TodoTaskScanner not found";
        return null;
    }
    
    @Override
    public void setScope(TaskScanningScope scope, Callback callback) {
        if (scope == null || callback == null) {
            return;
        }
        
        Set<FileObject> files = new WeakSet<>();
        for (FileObject file : scope.getLookup().lookupAll(FileObject.class)) {
            Project prj = FileOwnerQuery.getOwner(file);
            if (prj != null && prj.getLookup().lookup(NativeProject.class) != null) {
                files.add(file);
            }
        }

        for (Project p : scope.getLookup().lookupAll(Project.class)) {
            NativeProject prj = p.getLookup().lookup(NativeProject.class);
            if (prj != null) {
                for (NativeFileItem nativeFileItem : prj.getAllFiles()) {
                    files.add(nativeFileItem.getFileObject());
                }
            }
        }
        jobRef.set(new ScanJob(files, callback));
        scanTask.schedule(0);
    }
}
