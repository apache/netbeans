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
package org.netbeans.spi.editor.document;

import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.undo.UndoableEdit;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.editor.lib2.document.DocumentSpiPackageAccessor;
import org.netbeans.modules.editor.lib2.document.ModRootElement;
import org.netbeans.spi.editor.mimelookup.MimeLocation;
import org.openide.util.Cancellable;

/**
 * Task done right before document is saved.
 * Factories need to be registered in MimeLookup.
 *
 * @author Miloslav Metelka
 * @since 1.66
 */
public interface OnSaveTask extends Cancellable {

    /**
     * Perform the task on the context (given to factory that produced this task).
     */
    void performTask();

    /**
     * Perform the given runnable under a lock specific for this task.
     * The runnable will include a call to {@link #performTask() } but it may
     * also call other tasks if there are multiple ones.
     * <br>
     * For multiple task factories registered their {@link #runLocked(java.lang.Runnable) }
     * methods will be called subsequently (according to their registration order) in a nested way.
     *
     * @param run non-null runnable provided by the infrastructure.
     */
    void runLocked(@NonNull Runnable run);

    /**
     * Cancel processing of the job. Called not more than once for specific job.
     *
     * @return true if the job was successfully canceled, false if job
     *         can't be canceled for some reason.
     * @see Cancellable
     */
    public boolean cancel();

    /**
     * Factory for creation of on-save task.
     * It should be registered in MimeLookup by using mime registration e.g.<br>
     * <code>@MimeRegistration(mimeType="", service=OnSaveTask.Factory.class, position=300)</code><br>
     * Optional 'position' parameter may be used to force an order of tasks.
     * Currently there are two default global factories:
     * <ul>
     *   <li> Reformat before save uses position=500. </li>
     *   <li> Trailing-whitespace-removal task uses position=1000. </li.
     * </ul>
     */
    @MimeLocation(subfolderName="OnSave")
    public interface Factory {

        /**
         * Create on-save task.
         *
         * @param context non-null context containing info for the task.
         * @return task instance or null if the task is not appropriate for the given context.
         */
        OnSaveTask createTask(@NonNull Context context);

    }

    /**
     * Context given to factory for production of on-save task.
     */
    public static final class Context {

        static {
            DocumentSpiPackageAccessor.register(new PackageAccessor());
        }

        private final Document doc;
        
        private UndoableEdit undoEdit;
        
        private boolean taskStarted;

        Context(Document doc) {
            this.doc = doc;
        }

        /**
         * Get a document on which the task is being executed.
         * @return 
         */
        public Document getDocument() {
            return doc;
        }


        /**
         * Task may add a custom undoable edit related to its operation by using this method.
         * <br>
         * When undo would be performed after the save then this edit would be undone
         * (together with any possible modification changes performed by the task on the underlying document).
         * <br>
         * Note: this method should only be called during {@link OnSaveTask#performTask() }.
         * 
         * @param edit a custom undoable edit provided by the task.
         */
        public void addUndoEdit(UndoableEdit edit) {
            if (!taskStarted) {
                throw new IllegalStateException("This method may only be called during OnSaveTask.performTask()");
            }
            if (undoEdit != null) {
                undoEdit.addEdit(edit);
            }
        }
        
        /**
         * Get a root element with zero or more child elements each designating a modified region
         * of a document.
         * <br>
         * Tasks may use this information to work on modified document parts only.
         * <br>
         * Note: unlike in some other root element implementations here the child elements
         * do not fully cover the root element's offset space.
         *
         * @return root element containing modified regions of document as child elements.
         *  Null if document's implementation does not support modified elements collecting.
         */
        public Element getModificationsRootElement() {
            return ModRootElement.get(doc);
        }

        void setUndoEdit(UndoableEdit undoEdit) {
            this.undoEdit = undoEdit;
        }
        
        void setTaskStarted(boolean taskStarted) {
            this.taskStarted = taskStarted;
        }

    }

    static final class PackageAccessor extends DocumentSpiPackageAccessor {

        @Override
        public Context createContext(Document doc) {
            return new Context(doc);
        }

        @Override
        public void setUndoEdit(Context context, UndoableEdit undoEdit) {
            context.setUndoEdit(undoEdit);
        }
        
        @Override
        public void setTaskStarted(Context context, boolean taskStarted) {
            context.setTaskStarted(taskStarted);
        }

    }

}
