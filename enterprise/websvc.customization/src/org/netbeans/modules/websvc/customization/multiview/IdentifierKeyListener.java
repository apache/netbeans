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
/*
 * ClassKeyListener.java
 *
 * Created on April 8, 2006, 11:50 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.multiview;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JTextField;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author rico
 */
class IdentifierKeyListener extends KeyAdapter{
    JTextField classText;
    public IdentifierKeyListener(JTextField classText){
        this.classText = classText;
    }
    
    public void keyTyped(KeyEvent e){
        char c = e.getKeyChar();
        String val = classText.getText() + String.valueOf(c);
        if(!val.trim().equals("") &&(
                !JavaUtilities.isValidTypeIdentifier(val))){
            NotifyDescriptor.Message notifyDesc =
                    new NotifyDescriptor.Message(NbBundle.getMessage(PortTypePanel.class, "ERR_INVALID_IDENTIFIER", val),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(notifyDesc);
            e.consume();
        }
    }
}
