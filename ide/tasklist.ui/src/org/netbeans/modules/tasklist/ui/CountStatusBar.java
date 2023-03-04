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

import java.util.List;
import javax.swing.JLabel;
import org.netbeans.modules.tasklist.impl.TaskList;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.modules.tasklist.trampoline.TaskGroup;
import org.openide.util.NbBundle;

/**
 *
 * @author S. Aubrecht
 */
class CountStatusBar extends JLabel {

    private TaskList tasks;
    private TaskList.Listener listener;
    
    /** Creates a new instance of StatusBar */
    public CountStatusBar( TaskList tasks ) {
        this.tasks = tasks;
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
        
        updateText();
    }

    private void updateText() {
        StringBuffer buffer = new StringBuffer();
        for( TaskGroup tg : TaskGroup.getGroups() ) {
            int count = tasks.countTasks( tg );
            if( count > 0 ) {
                if( buffer.length() > 0 )
                    buffer.append( "  " ); //NOI18N
                else 
                    buffer.append( ' ' );
                buffer.append( tg.getDisplayName() );
                buffer.append( ": " ); //NOI18N
                buffer.append( count );
            }
        }
        if( buffer.length() == 0 ) 
            buffer.append(NbBundle.getMessage(CountStatusBar.class, "LBL_NoTasks"));//NOI18N
        buffer.append( ' ' );
        setText( buffer.toString() );
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        
        tasks.removeListener( listener );
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        
        tasks.addListener( listener );
        updateText();
    }
}
