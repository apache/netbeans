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

package org.netbeans.modules.tasklist.trampoline;

import java.util.List;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;

/**
 * Task List framework callback.
 * 
 * @author S. Aubrecht
 */
public abstract class TaskManager {
    
    /**
     * 
     * @param scanner 
     * @param files 
     */
    public abstract void refresh( FileTaskScanner scanner, FileObject... files );
    
    /**
     * 
     * @param scanner 
     */
    public abstract void refresh( FileTaskScanner scanner );
    
    /**
     * 
     * @param scope 
     */
    public abstract void refresh( TaskScanningScope scope );
    
    /**
     * 
     * @param scanner 
     */
    public abstract void started( PushTaskScanner scanner );
    
    /**
     * 
     * @param scanner 
     */
    public abstract void finished( PushTaskScanner scanner );
    
    /**
     * 
     * @param scanner 
     * @param resource 
     * @param tasks 
     */
    public abstract void setTasks( PushTaskScanner scanner, FileObject resource, List<? extends Task> tasks );

    public void setTasks( PushTaskScanner scanner, List<? extends Task> tasks ) {
        setTasks(scanner, null, tasks);
    }
    
    /**
     * 
     * @param scanner 
     */
    public abstract void clearAllTasks( PushTaskScanner scanner );

    /**
     * @returns true, if the UI displaying tasks is observed, false otherwise.
     */
    public abstract boolean isObserved();

    /**
     * @returns true, if currentEditor scope is set, false otherwise.
     */
    public abstract boolean isCurrentEditorScope();
}
