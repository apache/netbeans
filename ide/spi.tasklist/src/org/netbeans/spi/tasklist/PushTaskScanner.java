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

package org.netbeans.spi.tasklist;

import java.util.List;
import org.netbeans.modules.tasklist.trampoline.TaskManager;
import org.openide.filesystems.FileObject;


/**
 * <p>Task Scanner that can push new Tasks into Task List window.</p>
 * 
 * <p>You should use this scanner type if it takes too long to compute your tasks
 * or if your tasks are calculated asynchronously. <br>
 * In most cases it is easier to use {@link FileTaskScanner} instead.</p>
 * 
 * @author S. Aubrecht
 */
public abstract class PushTaskScanner {
    
    private String displayName;
    private String description;
    private String optionsPath;
    
    /**
     * Creates a new instance of PushTaskScanner
     * 
     * @param displayName Scanner's display name, will appear in Task List's filter window.
     * @param description Scanner's description, will be used for tooltips.
     * @param optionsPath Path that identifies panel in the global Options dialog window, 
     * or null if the scanner has no user settings. When scanner's settings changed the 
     * scanner must refresh its tasks the Task List window 
     * ({@link PushTaskScanner.Callback#clearAllTasks}, {@link PushTaskScanner.Callback#setTasks}).
     */
    public PushTaskScanner( String displayName, String description, String optionsPath ) {
        assert null != displayName;
        this.displayName = displayName;
        this.description = description;
        this.optionsPath = optionsPath;
    }
    
    /**
     * Scanner's display name.
     * @return Scanner's display name.
     */
    final String getDisplayName() {
        return displayName;
    }
    
    /**
     * Scanner's description (e.g. for tooltips).
     * @return Scanner's description (e.g. for tooltips).
     */
    final String getDescription() {
        return description;
    }
    
    /**
     * Path to the global options panel.
     * @return Path that identifies panel in the global Options dialog window, 
     * or null if the scanner has no user settings.
     */
    final String getOptionsPath() {
        return optionsPath;
    }
    
    /**
     * Called by the framework when the user switches to a different scanning scope
     * or when the currently used scope needs to be refreshed.
     *
     * @param scope New scanning scope, null value indicates that task scanning is to be cancelled.
     * @param callback Callback into Task List framework.
     */
    public abstract void setScope( TaskScanningScope scope, Callback callback );

    
    /**
     * Callback into Task List framework
     */
    public static final class Callback {
        
        private PushTaskScanner scanner;
        private TaskManager tm;
        
        /** Creates a new instance of SimpleTaskScannerCallback */
        Callback( TaskManager tm, PushTaskScanner scanner ) {
            this.tm = tm;
            this.scanner = scanner;
        }

        /**
         * Notify the framework that the scanner started looking for available Tasks.
         */
        public void started() {
            tm.started( scanner );
        }

        /**
         * Add/remove Tasks for the given file/folder.
         * @param file Resource (file or folder) the tasks are associated with.
         * @param tasks Tasks associated with the given resource or an empty list to remove previously provided Tasks.
         */
        public void setTasks( FileObject file, List<? extends Task> tasks ) {
            tm.setTasks( scanner, file, tasks );
        }

        /**
         * Add/remove Tasks that aren't associated with a particular resource.
         * @param tasks Tasks associated with this TaskScanner or an empty list to remove previously provided Tasks.
         * @since 1.6
         */
        public void setTasks( List<? extends Task> tasks ) {
            tm.setTasks( scanner, tasks );
        }
        
        /**
         * Remove from the Task List window all Tasks that were provided by this scanner.
         */
        public void clearAllTasks() {
            tm.clearAllTasks( scanner );
        }

        /**
         * Notify the framework that the scanner has finished.
         */
        public void finished() {
            tm.finished( scanner );
        }

        /**
         * @return true, if the framework is observed.
         * @since spi.tasklist/1.24
         */
        public boolean isObserved() {
            return tm.isObserved();
        }

        /**
         * @return true, if the current editor scope is set in the framework.
         * Performance helper method.
         * @since spi.tasklist/1.24
         */
        public boolean isCurrentEditorScope() {
            return tm.isCurrentEditorScope();
        }
    }
}
