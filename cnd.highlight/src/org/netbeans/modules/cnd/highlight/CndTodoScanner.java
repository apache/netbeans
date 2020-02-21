/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
