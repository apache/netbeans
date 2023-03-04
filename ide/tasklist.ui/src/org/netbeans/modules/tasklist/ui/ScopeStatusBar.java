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

package org.netbeans.modules.tasklist.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import org.netbeans.modules.tasklist.impl.TaskList;
import org.netbeans.modules.tasklist.impl.TaskManagerImpl;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;

/**
 *
 * @author S. Aubrecht
 */
class ScopeStatusBar extends JLabel implements PropertyChangeListener {

    private TaskManagerImpl taskManager;
    private TaskList.Listener listener;
    
    /** Creates a new instance of StatusBar */
    public ScopeStatusBar( TaskManagerImpl taskManager ) {
        this.taskManager = taskManager;        
        updateText();
        listener = new TaskList.Listener() {
            public void tasksAdded(List<? extends Task> tasks) {
                updateText();
            }

            public void tasksRemoved(List<? extends Task> tasks) {
                updateText();
            }

            public void cleared() {
                updateText();
            }
        };
    }

    private void updateText() {
        StringBuffer buffer = new StringBuffer();
        TaskScanningScope scope = taskManager.getScope();
        Map<String, String> descriptions = scope.getLookup().lookup(Map.class);
        if( null != descriptions ) {
            String label = descriptions.get("StatusBarLabel"); //NOI18N
            if( null != label ) {
                buffer.append( "  (" + label + ")" );
            }
        }
        setText( buffer.toString() );
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        
        taskManager.removePropertyChangeListener( TaskManagerImpl.PROP_SCOPE, this );
        taskManager.getTasks().removeListener( listener );
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        
        taskManager.addPropertyChangeListener( TaskManagerImpl.PROP_SCOPE, this );
        taskManager.getTasks().addListener( listener );
    }

    public void propertyChange(PropertyChangeEvent evt) {
        updateText();
    }
}
