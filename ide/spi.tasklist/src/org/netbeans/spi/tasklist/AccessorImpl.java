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

import java.awt.Image;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.Action;
import org.netbeans.modules.tasklist.trampoline.Accessor;
import org.netbeans.modules.tasklist.trampoline.TaskGroup;
import org.netbeans.modules.tasklist.trampoline.TaskManager;
import org.openide.filesystems.FileObject;

/**
 * API trampoline
 * 
 * @author S. Aubrecht
 */
class AccessorImpl extends Accessor {
    
    public String getDescription( Task t ) {
        return t.getDescription();
    }

    public FileObject getFile(Task t) {
        return t.getFile();
    }

    public URL getURL(Task t) {
        return t.getURL();
    }

    public TaskGroup getGroup(Task t) {
        return t.getGroup();
    }

    public int getLine(Task t) {
        return t.getLine();
    }
    
    public ActionListener getDefaultAction(Task t) {
        return t.getDefaultAction();
    }

    public Action[] getActions(Task t) {
        return t.getActions();
    }

    public String getDisplayName(TaskScanningScope scope) {
        return scope.getDisplayName();
    }

    public String getDescription(TaskScanningScope scope) {
        return scope.getDescription();
    }

    public Image getIcon(TaskScanningScope scope) {
        return scope.getIcon();
    }
    
    public boolean isDefault( TaskScanningScope scope ) {
        return scope.isDefault();
    }

    public String getDisplayName(FileTaskScanner scanner) {
        return scanner.getDisplayName();
    }

    public String getDescription(FileTaskScanner scanner) {
        return scanner.getDescription();
    }

    public String getOptionsPath(FileTaskScanner scanner) {
        return scanner.getOptionsPath();
    }

    public String getDisplayName(PushTaskScanner scanner) {
        return scanner.getDisplayName();
    }

    public String getDescription(PushTaskScanner scanner) {
        return scanner.getDescription();
    }

    public String getOptionsPath(PushTaskScanner scanner) {
        return scanner.getOptionsPath();
    }

    public TaskScanningScope.Callback createCallback(TaskManager tm, TaskScanningScope scope) {
        return new TaskScanningScope.Callback( tm, scope );
    }

    public FileTaskScanner.Callback createCallback(TaskManager tm, FileTaskScanner scanner) {
        return new FileTaskScanner.Callback( tm, scanner );
    }

    public PushTaskScanner.Callback createCallback(TaskManager tm, PushTaskScanner scanner) {
        return new PushTaskScanner.Callback( tm, scanner );
    }
}
