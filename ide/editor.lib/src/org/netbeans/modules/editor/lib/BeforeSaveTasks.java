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

package org.netbeans.modules.editor.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.document.DocumentSpiPackageAccessor;
import org.netbeans.modules.editor.lib2.document.EditorDocumentHandler;
import org.netbeans.modules.editor.lib2.document.EditorDocumentServices;
import org.netbeans.modules.editor.lib2.document.ModRootElement;
import org.netbeans.spi.editor.document.OnSaveTask;
import org.openide.util.Mutex;

/**
 * Registration of tasks performed right before document save.
 *
 * @author Miloslav Metelka
 * @since 1.9
 */
public final class BeforeSaveTasks {
    
    private static final Logger LOG = Logger.getLogger(BeforeSaveTasks.class.getName());

    public static synchronized BeforeSaveTasks get(BaseDocument doc) {
        BeforeSaveTasks beforeSaveTasks = (BeforeSaveTasks) doc.getProperty(BeforeSaveTasks.class);
        if (beforeSaveTasks == null) {
            beforeSaveTasks = new BeforeSaveTasks(doc);
            doc.putProperty(BeforeSaveTasks.class, beforeSaveTasks);
        }
        return beforeSaveTasks;
    }

    private static final ThreadLocal<Boolean> ignoreOnSaveTasks = new ThreadLocal<Boolean>() {
        @Override protected Boolean initialValue() {
            return false;
        }
    };
    
    public static <T> T runWithOnSaveTasksDisabled(Mutex.Action<T> run) {
        Boolean originalIgnore = ignoreOnSaveTasks.get();
        ignoreOnSaveTasks.set(true);
        try {
            return run.run();
        } finally {
            ignoreOnSaveTasks.set(originalIgnore);
        }
    }
    
    private final BaseDocument doc;

    private BeforeSaveTasks(BaseDocument doc) {
        this.doc = doc;
        Runnable beforeSaveRunnable = (Runnable)
                doc.getProperty("beforeSaveRunnable"); // Name of prop in sync with CloneableEditorSupport NOI18N
        if (beforeSaveRunnable != null) {
            throw new IllegalStateException("\"beforeSaveRunnable\" property of document " + doc + // NOI18N
                    " is already occupied by " + beforeSaveRunnable); // NOI18N
        }
        beforeSaveRunnable = new Runnable() {
            public @Override void run() {
                runTasks();
            }
        };
        doc.putProperty("beforeSaveRunnable", beforeSaveRunnable); // NOI18N
    }

    void runTasks() {
        if (ignoreOnSaveTasks.get() == Boolean.TRUE) return ;
        String mimeType = DocumentUtilities.getMimeType(doc);
        Collection<? extends OnSaveTask.Factory> factories = MimeLookup.getLookup(mimeType).
                lookupAll(OnSaveTask.Factory.class);
        OnSaveTask.Context context = DocumentSpiPackageAccessor.get().createContext(doc);
        List<OnSaveTask> tasks = new ArrayList<OnSaveTask>(factories.size());
        for (OnSaveTask.Factory factory : factories) {
            OnSaveTask task = factory.createTask(context);
            if (task != null) {
                tasks.add(task);
            }
        }
        new TaskRunnable(doc, tasks, context).run();
    }

    private static final class TaskRunnable implements Runnable {
        
        final BaseDocument doc;

        final List<OnSaveTask> tasks;
        
        final OnSaveTask.Context context;

        int lockedTaskIndex;

        public TaskRunnable(BaseDocument doc, List<OnSaveTask> tasks, OnSaveTask.Context context) {
            this.doc = doc;
            this.tasks = tasks;
            this.context = context;
        }

        @Override
        public void run() {
            if (lockedTaskIndex < tasks.size()) {
                OnSaveTask task = tasks.get(lockedTaskIndex++);
                task.runLocked(this);

            } else {
                doc.runAtomicAsUser(new Runnable() {
                    @Override
                    public void run() {
                        // See CloneableEditorSupport for property explanation
                        Runnable beforeSaveStart = (Runnable) doc.getProperty("beforeSaveStart");
                        if (beforeSaveStart != null) {
                            beforeSaveStart.run();
                        }

                        UndoableEdit atomicEdit = EditorDocumentHandler.startOnSaveTasks(doc);
                        assert (atomicEdit != null) : "Null atomic edit"; // NOI18N
                        boolean success = false;
                        try {
                            DocumentSpiPackageAccessor.get().setUndoEdit(context, atomicEdit);
                            for (int i = 0; i < tasks.size(); i++) {
                                OnSaveTask task = tasks.get(i);
                                DocumentSpiPackageAccessor.get().setTaskStarted(context, true);
                                task.performTask();
                            }
                            ModRootElement modRootElement = ModRootElement.get(doc);
                            if (modRootElement != null) {
                                modRootElement.resetMods(atomicEdit);
                            }
                            success = true;

                        } finally {
                            // The save should be done even if the save tasks fail so that the user
                            // is not left with an unsaved document.
                            // Just undo an effect of the failed save tasks.
                            EditorDocumentHandler.endOnSaveTasks(doc, success);

                            // See CloneableEditorSupport for property explanation
                            Runnable beforeSaveEnd = (Runnable) doc.getProperty("beforeSaveEnd");
                            if (beforeSaveEnd != null) {
                                beforeSaveEnd.run();
                            }
                        }
                    }
                });
            }
        }

    }

}
