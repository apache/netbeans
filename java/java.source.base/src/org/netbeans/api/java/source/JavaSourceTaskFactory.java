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
package org.netbeans.api.java.source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.JavaSourceTaskFactoryManager;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 * A factory for tasks that will be run in the {@link JavaSource} Java parsing harness.
 *
 * Please note that there is usually no need to implement this class directly,
 * as there are support classes for common {@link JavaSourceTaskFactory} implementations.
 *
 * This factory should be registered in the global lookup using {@link org.openide.util.lookup.ServiceProvider}.
 * 
 * @see EditorAwareJavaSourceTaskFactory
 * @see CaretAwareJavaSourceTaskFactory
 * @see LookupBasedJavaSourceTaskFactory
 *
 * @author Jan Lahoda
 */
public abstract class JavaSourceTaskFactory {

    private static final Logger LOG = Logger.getLogger(JavaSourceTaskFactory.class.getName());
            static final String BEFORE_ADDING_REMOVING_TASKS = "beforeAddingRemovingTasks"; //NOI18N
            static final String FILEOBJECTS_COMPUTATION = "fileObjectsComputation"; //NOI18N
            
    private final Phase phase;
    private final Priority priority;
    private final TaskIndexingMode taskIndexingMode;

    /**Construct the JavaSourceTaskFactory with given {@link Phase} and {@link Priority}.
     *
     * @param phase phase to use for tasks created by {@link #createTask}
     * @param priority priority to use for tasks created by {@link #createTask}
     */
    protected JavaSourceTaskFactory(@NonNull Phase phase, @NonNull Priority priority) {
        this.phase = phase;
        this.priority = priority;
        this.taskIndexingMode = TaskIndexingMode.DISALLOWED_DURING_SCAN;
        this.file2Task = new HashMap<FileObject, CancellableTask<CompilationInfo>>();
        this.file2JS = new HashMap<FileObject, JavaSource>();
    }
    
    /**Construct the JavaSourceTaskFactory with given {@link Phase}, {@link Priority}
     * and {@link TaskIndexingMode}.
     *
     * @param phase phase to use for tasks created by {@link #createTask}
     * @param priority priority to use for tasks created by {@link #createTask}
     * @param taskIndexingMode the awareness of indexing. For tasks which can run
     * during indexing use {@link TaskIndexingMode#ALLOWED_DURING_SCAN} for tasks
     * which cannot run during indexing use {@link TaskIndexingMode#DISALLOWED_DURING_SCAN}.
     * @since 0.94
     */
    protected JavaSourceTaskFactory(
            @NonNull final Phase phase,
            @NonNull final Priority priority,
            @NonNull final TaskIndexingMode taskIndexingMode) {
        Parameters.notNull("phase", phase); //NOI18N
        Parameters.notNull("priority", priority);   //NOI18N
        Parameters.notNull("taskIndexingMode", taskIndexingMode);   //NOI18N
        this.phase = phase;
        this.priority = priority;
        this.taskIndexingMode = taskIndexingMode;
        this.file2Task = new HashMap<FileObject, CancellableTask<CompilationInfo>>();
        this.file2JS = new HashMap<FileObject, JavaSource>();
    }

    /**Create task for a given file. This task will be registered into the {@link JavaSource}
     * parsing harness with a given {@link #getPriority priority} and {@link #getPhase phase}.
     *
     * Please note that this method should run as quickly as possible.
     *
     * @param file for which file the task should be created.
     * @return created {@link CancellableTask}  for a given file.
     */
    protected abstract @NonNull CancellableTask<CompilationInfo> createTask(FileObject file);

    /**Specifies on which files should be registered tasks created by this factory.
     * On {@link JavaSource}'s corresponding to {@link FileObject}s returned from
     * this method will be registered tasks created by the {@link #createTask} method
     * of this factory.
     *
     * If this list changes, a change event should be fired to all registered
     * {@link ChangeListener}s.
     *
     * @return list of {@link FileObject} on which tasks from this factory should be
     * registered.
     * @see #createTask
     * @see #addChangeListener
     * @see EditorAwareJavaSourceTaskFactory
     * @see CaretAwareJavaSourceTaskFactory
     */
    protected abstract @NonNull Collection<FileObject> getFileObjects();

    /**Notify the infrastructure that the collection of fileobjects has been changed.
     * The infrastructure calls {@link #getFileObjects()} to get a new collection files.
     */
    protected final void fileObjectsChanged() {
        LOG.log(Level.FINEST, FILEOBJECTS_COMPUTATION);

        final List<FileObject> currentFiles = new ArrayList<FileObject>(getFileObjects());

        if (SYNCHRONOUS_EVENTS) {
            stateChangedImpl(currentFiles);
        } else {
            WORKER.post(new Runnable() {
                public void run() {
                    stateChangedImpl(currentFiles);
                }
            });
        }
    }

    /**for tests:
     */
    static boolean SYNCHRONOUS_EVENTS = false;

    private void stateChangedImpl(List<FileObject> currentFiles) {
        LOG.log(Level.FINEST, BEFORE_ADDING_REMOVING_TASKS);
        synchronized (this.filesLock) {                
            List<FileObject> addedFiles = new ArrayList<FileObject>(currentFiles);
            List<FileObject> removedFiles = new ArrayList<FileObject>(file2Task.keySet());
            
            addedFiles.removeAll(file2Task.keySet());
            removedFiles.removeAll(currentFiles);
            
            //remove old tasks:
            for (FileObject r : removedFiles) {
                JavaSource source = file2JS.remove(r);
                
                if (source == null) {
                    //TODO: log
                    continue;
                }
                ACCESSOR2.removePhaseCompletionTask(source, file2Task.remove(r));
            }
            
            //add new tasks:
            for (FileObject a : addedFiles) {
                if (a == null)
                    continue;
                if (!a.isValid()) {
                    continue;
                }
                JavaSource js = JavaSource.forFileObject(a);
                
                if (js != null) {
                    CancellableTask<CompilationInfo> task = createTask(a);

                    if (task == null) {
                        throw new IllegalStateException("createTask(FileObject) returned null for factory: " + getClass().getName());
                    }                    
                    ACCESSOR2.addPhaseCompletionTask(js, task, phase, priority, taskIndexingMode);                    
                    file2Task.put(a, task);
                    file2JS.put(a, js);
                }
            }
        }
    }
       
    /**Re-run task created by this factory for given file.
     * If the task has not yet been run, does nothing.
     *
     * @param file task created by this factory for this file is re-run.
     */
    protected final void reschedule(FileObject file) throws IllegalArgumentException {
        synchronized(this.filesLock) {
            JavaSource source = file2JS.get(file);

            if (source == null) {
    //            throw new IllegalArgumentException("No JavaSource for given file.");
                return ;
    }

            CancellableTask<CompilationInfo> task = file2Task.get(file);

            if (task == null) {
    //                    throw new IllegalArgumentException("This factory did not created any task for " + FileUtil.getFileDisplayName(file)); // NOI18N
                return ;
            }

            ACCESSOR2.rescheduleTask(source, task);
        }
    }

    private final Map<FileObject, CancellableTask<CompilationInfo>> file2Task;
    private final Map<FileObject, JavaSource> file2JS;
    private final Object filesLock = new Object();

    private static RequestProcessor WORKER = new RequestProcessor("JavaSourceTaskFactory", 1, false, false); // NOI18N
    
    static {
        JavaSourceTaskFactoryManager.ACCESSOR = new JavaSourceTaskFactoryManager.Accessor() {
            public void fireChangeEvent(JavaSourceTaskFactory f) {
                f.fileObjectsChanged();
            }
        };
        ACCESSOR2 = new Accessor2() {
            public void addPhaseCompletionTask(JavaSource js, CancellableTask<CompilationInfo> task, Phase phase, Priority priority, TaskIndexingMode taskIndexingMode) {
                JavaSourceAccessor.getINSTANCE().addPhaseCompletionTask (js, task, phase, priority, taskIndexingMode);
            }

            public void removePhaseCompletionTask(JavaSource js, CancellableTask<CompilationInfo> task) {
                JavaSourceAccessor.getINSTANCE().removePhaseCompletionTask(js,task);
            }

            public void rescheduleTask(JavaSource js, CancellableTask<CompilationInfo> task) {
                JavaSourceAccessor.getINSTANCE().rescheduleTask(js, task);
            }
        };
    }

    static interface Accessor2 {
        public abstract void addPhaseCompletionTask(JavaSource js, CancellableTask<CompilationInfo> task, Phase phase, Priority priority, TaskIndexingMode taskIndexingMode );
        public abstract void removePhaseCompletionTask(JavaSource js, CancellableTask<CompilationInfo> task );
        public abstract void rescheduleTask(JavaSource js, CancellableTask<CompilationInfo> task);
    }
    
    
    static Accessor2 ACCESSOR2;
    
}
