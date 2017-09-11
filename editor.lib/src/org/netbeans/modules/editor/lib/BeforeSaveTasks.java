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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

    private final static ThreadLocal<Boolean> ignoreOnSaveTasks = new ThreadLocal<Boolean>() {
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
