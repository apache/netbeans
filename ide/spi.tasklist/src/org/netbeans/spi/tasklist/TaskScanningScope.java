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
import java.util.Map;
import javax.swing.AbstractAction;
import org.netbeans.modules.tasklist.trampoline.TaskManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * A class that defines the set of resources (files and/or folders) that will be scanned for Tasks.
 * 
 * @author S. Aubrecht
 */
public abstract class TaskScanningScope implements Iterable <FileObject>, Lookup.Provider {
    
    private String displayName;
    private String description;
    private Image icon;
    private boolean isDefault;
    
    /**
     * Create a new instance
     * @param displayName Label for Task List's popup menu
     * @param description Description for tooltips Task List's toolbar
     * @param icon Icon to be displayed in Task List's toolbar
     */
    public TaskScanningScope( String displayName, String description, Image icon ) {
        this( displayName, description, icon, false );
    }
    
    /**
     * Create a new instance
     * @param displayName Label for Task List's popup menu
     * @param description Description for tooltips Task List's toolbar
     * @param icon Icon to be displayed in Task List's toolbar
     * @param isDefault True if this scope should be selected by default when the Task List is opened for the first time.
     */
    public TaskScanningScope( String displayName, String description, Image icon, boolean isDefault ) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.isDefault = isDefault;
    }
    
    /**
     * Display name used for Task List's popup menu, cannot be null.
     * @return Display name used for Task List's popup menu, cannot be null.
     */
    final String getDisplayName() {
        String res = null;
        Map<String,String> labels = getLookup().lookup(Map.class);
        if( null != labels ) {
            res = labels.get(AbstractAction.NAME);
        }
        if( null == res )
            res = displayName;
        return res;
    }
    
    /**
     * Long description (e.g. for tooltip)
     * @return Long description (e.g. for tooltip)
     */
    final String getDescription() {
        String res = null;
        Map<String,String> labels = getLookup().lookup(Map.class);
        if( null != labels ) {
            res = labels.get(AbstractAction.SHORT_DESCRIPTION);
        }
        if( null == res )
            res = description;
        return res;
    }
    
    /**
     * Icon to be displayed in Task List's window toolbar, cannot be null.
     * @return Icon to be displayed in Task List's window toolbar, cannot be null.
     */
    final Image getIcon() {
        return icon;
    }
    
    /**
     * True if this scope should be selected by default when the Task List is opened for the first time.
     * @return True if this scope should be selected by default when the Task List is opened for the first time.
     */
    final boolean isDefault() {
        return isDefault;
    }
    
    /**
     * Check whether the given resource is in this scanning scope.
     * @param resource Resource to be checked.
     * @return True if the given resource is in this scope.
     */
    public abstract boolean isInScope( FileObject resource );
    
    /**
     * Called by the framework when the user switches to this scanning scope.
     * 
     * @param callback 
     */
    public abstract void attach( Callback callback );
    
    /**
     * Lookup with scope's contents.
     * @return Lookup that contains either the {@link org.openide.filesystems.FileObject}s to be scanned (for example when 
     * the scope is 'currently edited file') or {@link org.netbeans.api.project.Project}s that are in this scope.
     */
    public abstract Lookup getLookup();
    
    /**
     * Callback to Task List's framework.
     */
    public static final class Callback {
        private TaskScanningScope scope;
        private TaskManager tm;
        
        Callback( TaskManager tm, TaskScanningScope scope ) {
            this.tm = tm;
            this.scope = scope;
        }
        
        /**
         * Notify the framework that all resources under this scope must be re-scanned.
         */
        public void refresh() {
            tm.refresh( scope );
        }
    }
}
