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

package org.netbeans.spi.tasklist;

import java.util.List;
import org.netbeans.modules.tasklist.trampoline.TaskManager;
import org.openide.filesystems.FileObject;

/**
 * Task Scanner that can provide new Tasks for specified resources (files/folders) 
 * when asked by the Task List framework.
 * 
 * @author S. Aubrecht
 */
public abstract class FileTaskScanner {
    
    private String displayName;
    private String description;
    private String optionsPath;
    
    /**
     * Creates a new instance of FileTaskScanner
     * 
     * @param displayName Scanner's display name, will appear in Task List's filter window.
     * @param description Scanner's description, will be used for tooltips.
     * @param optionsPath Path that identifies panel in the global Options dialog window, 
     * or null if the scanner has no user settings. When scanner's settings changed the 
     * scanner must notify - {@link FileTaskScanner.Callback#refreshAll} - the Task List framework that a re-scan is needed.
     */
    public FileTaskScanner( String displayName, String description, String optionsPath ) {
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
     * Notification from the Task List framework that the scanning phase is about to begin.
     * (Time to create expensive parsers, compilers etc...)
     */
    public void notifyPrepare() {
    }
    
    /**
     * Notification from the Task List framework that the scanning phase ended.
     * (Time to release expensive parsers, compilers etc...)
     */
    public void notifyFinish() {
    }
    
    /**
     * Scan the given resource (file or folder?) for tasks. This method is always
     * called within {@link #notifyPrepare} and {@link #notifyFinish} calls. Depending on Task
     * List's current scope this method may be called repeatedly for different resources.
     * 
     * @param resource Resource to be scanned.
     * 
     * @return List of scanned Tasks or null if the provider is busy at the moment and 
     * previously scanned tasks are to be used instead.
     */
    public abstract List<? extends Task> scan( FileObject resource );
    
    /**
     * Called by the framework when this Task type is enabled/disabled in Task List's filter window.
     * @param callback Callback into Task List's framework, null value indicates that user has disabled
     * this type of Tasks.
     */
    public abstract void attach( Callback callback );

    /**
     * Callback into Task List's framework.
     */
    public static final class Callback {

        private FileTaskScanner scanner;
        private TaskManager tm;
        
        /** Creates a new instance of FileBasedTaskScannerCallback */
        Callback( TaskManager tm, FileTaskScanner scanner ) {
            this.tm = tm;
            this.scanner = scanner;
        }

        /**
         * Notify the Task List framework that the given resources need to be
         * rescanned by this scanner.
         * @param resources Resources to be rescanned.
         */
        public void refresh( FileObject... resources ) {
            tm.refresh( scanner, resources );
        }

        /**
         * Notify the Task List framework that all resources 
         * under the current scope need to be rescanned by this scanner.
         */
        public void refreshAll() {
            tm.refresh( scanner );
        }
    }
}
