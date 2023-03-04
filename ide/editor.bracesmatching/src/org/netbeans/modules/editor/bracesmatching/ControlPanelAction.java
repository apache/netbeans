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
package org.netbeans.modules.editor.bracesmatching;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 *
 * @author vita
 */
public class ControlPanelAction extends TextAction {

    public ControlPanelAction() {
        super("match-brace-control-properties"); //NOI18N
    }

    public void actionPerformed(ActionEvent e) {
        JTextComponent component = getTextComponent(e);
        ControlPanel panel = new ControlPanel(component);
        
        DialogDescriptor dd = new DialogDescriptor(
            panel, 
            "Braces Matching Control Panel", //NOI18N
            true,
            null
        ); 
        
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.setVisible(true);
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            panel.applyChanges();
        }
    }
    
}
