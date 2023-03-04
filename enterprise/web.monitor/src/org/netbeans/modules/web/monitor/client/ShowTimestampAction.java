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

/**
 * ShowTimestampAction.java
 *
 * Created on June 23, 2004, 10:35 AM
 *
 * @author  Stepan Herold
 * @version
 */

package org.netbeans.modules.web.monitor.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.BooleanStateAction;


public class ShowTimestampAction extends BooleanStateAction {

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected void initialize() {
        super.initialize();
        TransactionView transView =  TransactionView.getInstance();
        setBooleanState(transView.isTimestampButtonSelected());
        // listen to changes made by toolbar button
        transView.addTimestampButtonActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                setBooleanState(!getBooleanState());
            }
        });
        setIcon(null);
    }    
    
    public String getName() {
        return NbBundle.getBundle(ReloadAction.class).getString("MON_Show_timestamp");
    }
        
    public void actionPerformed(ActionEvent ev) {
        super.actionPerformed(ev);
        TransactionNode.toggleTimeStamp();
        TransactionView.getInstance().setTimestampButtonSelected(getBooleanState());
        MonitorAction.getController().updateNodeNames();
    }    
}
