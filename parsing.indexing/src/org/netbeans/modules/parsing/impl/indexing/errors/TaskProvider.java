/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.parsing.impl.indexing.errors;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;
import org.openide.util.WeakSet;

/**
 *
 * @author Stanislav Aubrecht, Jan Lahoda
 */
public final class TaskProvider extends PushTaskScanner {
    
    private static final Logger LOG = Logger.getLogger(TaskProvider.class.getName());
    
    private static TaskProvider INSTANCE;
    
    private TaskScanningScope scope;
    private Callback callback;
    
    public TaskProvider() {
        super( NbBundle.getBundle(TaskProvider.class).getString("LBL_ProviderName"),
                NbBundle.getBundle(TaskProvider.class).getString("LBL_ProviderDescription"), null); //NOI18N
        INSTANCE = this;
    }
    
    private synchronized void refreshImpl(FileObject file) {
        LOG.log(Level.FINE, "refresh: {0}", file);
        
        if (scope == null || callback == null)
            return ; //nothing to refresh
        if (!scope.isInScope(file)) {
            if (!file.isFolder())
                return;
            
            //the given file may be a parent of some file that is in the scope:
            for (FileObject inScope : scope.getLookup().lookupAll(FileObject.class)) {
                if (FileUtil.isParentOf(file, inScope)) {
                    enqueue(new Work(inScope, callback));
                }
            }
            
            return ;
        }
        
        LOG.log(Level.FINE, "enqueing work for: {0}", file);
        enqueue(new Work(file, callback));
    }
    
    public static void refresh(FileObject file) {
        if (INSTANCE != null) {
            INSTANCE.refreshImpl(file);
        }
    }
    
    public static void refreshAll() {
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
        
        if (scope == null || callback == null)
            return ;
        
        for (FileObject file : scope.getLookup().lookupAll(FileObject.class)) {
            enqueue(new Work(file, callback));
        }
        
        for (Project p : scope.getLookup().lookupAll(Project.class)) {
            for (SourceGroup generic : ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC)) {
                for (FileObject root : Utilities.findIndexedRootsUnderDirectory(p, generic.getRootFolder())) {
                    enqueue(new Work(root, callback));
                }
            }
        }
    }
    
    private static final Map<RequestProcessor.Task,Work> TASKS = new HashMap<RequestProcessor.Task,Work>();
    private static boolean clearing;
    private static final RequestProcessor WORKER = new RequestProcessor("Java Task Provider");
    private static Map<FileObject, Set<FileObject>> root2FilesWithAttachedErrors = new WeakHashMap<FileObject, Set<FileObject>>();
    
    private static void enqueue(Work w) {
        synchronized (TASKS) {
            final RequestProcessor.Task task = WORKER.post(w);
            TASKS.put(task,w);
            task.addTaskListener(new TaskListener() {
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
                for (Map.Entry<RequestProcessor.Task,Work> t : TASKS.entrySet()) {
                    t.getKey().cancel();
                    t.getValue().cancel();
                }
                TASKS.clear();
            } finally {
                clearing = false;
            }
        }
        
        synchronized (TaskProvider.class) {
            root2FilesWithAttachedErrors.clear();
        }
        
    }
    
    //only for tests:
    static void waitWorkFinished() throws Exception {
        while (true) {
            RequestProcessor.Task t = null;
            synchronized (TASKS) {
                if (TASKS.isEmpty())
                    return;
                t = TASKS.keySet().iterator().next();
            }
            
            t.waitFinished();
        }
    }

    private static Set<FileObject> getFilesWithAttachedErrors(FileObject root) {
        Set<FileObject> result = root2FilesWithAttachedErrors.get(root);
        
        if (result == null) {
            root2FilesWithAttachedErrors.put(root, result = new WeakSet<FileObject>());
        }
        
        return result;
    }
    
    private static synchronized void updateErrorsInRoot(
            final Callback callback,
            final FileObject root,
            final AtomicBoolean cancelled) {
        Set<FileObject> filesWithErrors = getFilesWithAttachedErrors(root);
        Set<FileObject> fixedFiles = new HashSet<FileObject>(filesWithErrors);
        Set<FileObject> nueFilesWithErrors = new HashSet<FileObject>();

        try {
            for (URL u : TaskCache.getDefault().getAllFilesWithRecord(root.toURL())) {
                if (cancelled.get()) {
                    return;
                }
                FileObject file = URLMapper.findFileObject(u);

                if (file != null) {
                    List<Task> result = TaskCache.getDefault().getErrors(file);

                    LOG.log(Level.FINE, "Setting {1} for {0}\n", new Object[] {file, result});

                    callback.setTasks(file, result);

                    if (!fixedFiles.remove(file)) {
                        nueFilesWithErrors.add(file);
                    }
                }
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        
        for (FileObject f : fixedFiles) {
            LOG.log(Level.FINE, "Clearing errors for {0}", f);
            callback.setTasks(f, Collections.<Task>emptyList());
        }
        
        filesWithErrors.addAll(nueFilesWithErrors);
    }
    
    private static final class Work implements Runnable {
        private final FileObject fileOrRoot;
        private final Callback callback;
        private final AtomicBoolean canceled = new AtomicBoolean();

        public Work(FileObject fileOrRoot, Callback callback) {
            this.fileOrRoot = fileOrRoot;
            this.callback = callback;
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(
                    Level.FINER,
                    "Work created by: {0}", //NOI18N
                    Arrays.toString(Thread.currentThread().getStackTrace()));
            }
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

            ClassPath cp = Utilities.getSourceClassPathFor(file);

            if (cp == null) {
                LOG.log(Level.FINE, "cp == null");
                return;
            }
            
            FileObject root = cp.findOwnerRoot(file);

            if (root == null) {
                Project p = FileOwnerQuery.getOwner(file);
                
                LOG.log(Level.WARNING,
                        "file: {0} is not on its own source classpath: {1}, project: {2}",
                        new Object[] {
                            FileUtil.getFileDisplayName(file),
                            cp.toString(ClassPath.PathConversionMode.PRINT),
                            p != null ? p.getClass() : "null"
                        });

                return ;
            }

            if (file.isData()) {
                List<? extends Task> tasks = TaskCache.getDefault().getErrors(file);
                Set<FileObject> filesWithErrors = getFilesWithAttachedErrors(root);

                if (tasks.isEmpty()) {
                    filesWithErrors.remove(file);
                } else {
                    filesWithErrors.add(file);
                }

                LOG.log(Level.FINE, "setting {1} for {0}", new Object[]{file, tasks});
                getCallback().setTasks(file, tasks);
            } else {
                updateErrorsInRoot(getCallback(), root, canceled);
            }
        }
    }
}
