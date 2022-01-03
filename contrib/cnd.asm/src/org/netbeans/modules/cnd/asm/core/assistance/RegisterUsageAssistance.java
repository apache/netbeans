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


package org.netbeans.modules.cnd.asm.core.assistance;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.modules.cnd.asm.core.dataobjects.AsmObjectUtilities;

import org.netbeans.modules.cnd.asm.model.AsmModelAccessor;
import org.netbeans.modules.cnd.asm.model.AsmState;


public class RegisterUsageAssistance implements CaretListener {
    private final JTextComponent pane;
    private final RegisterUsageAction action;
    
    public RegisterUsageAssistance(JTextComponent pane, RegisterUsageAccesor acc) {        
        this.action = new RegisterUsageAction(acc);
        this.pane = pane;
        
        pane.addCaretListener(this);                                
    }

    public void caretUpdate(CaretEvent e) {
        Document doc = AsmObjectUtilities.getDocument(pane); 
        AsmModelAccessor acc = AsmObjectUtilities.getAccessor(doc);                
        
        if (acc == null) {
            return;
        }
            
        AsmState state = acc.getState();
        
        if (state == null) {
            return ;
        }
        
        action.computeUsage(state, e.getDot());       
    }       
}
