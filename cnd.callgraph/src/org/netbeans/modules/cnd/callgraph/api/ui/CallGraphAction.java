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

package org.netbeans.modules.cnd.callgraph.api.ui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.openide.util.RequestProcessor;

/**
 *
 */
abstract public class CallGraphAction extends AbstractAction{
    private final RequestProcessor RP = new RequestProcessor("CallGraphAction", 1);//NOI18N
    private final CallGraphActionEDTRunnable runnable;
    
    
    public CallGraphAction (CallGraphActionEDTRunnable runnable){
        this.runnable = runnable;
    }
    
    abstract public void doNonEDTAction();
    abstract public void doEDTAction();

    @Override
    public final void actionPerformed(ActionEvent e) {
        doEDTAction();
        RP.post(new Runnable() {

            @Override
            public void run() {
                doNonEDTAction();
                SwingUtilities.invokeLater(runnable);
            }
            
        });
    }
    
    
}
