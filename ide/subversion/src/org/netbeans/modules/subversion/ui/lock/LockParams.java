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
package org.netbeans.modules.subversion.ui.lock;

import java.awt.Dialog;
import javax.swing.JButton;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
class LockParams {
    private final LockPanel panel;

    public LockParams (boolean containsRemotelyLocked) {
        panel = new LockPanel();
        panel.lblInfo.setVisible(containsRemotelyLocked);
    }

    boolean show () {
        JButton okButton = new JButton(NbBundle.getMessage(LockParams.class, "CTL_LockParams.ok.label")); //NOI18N
        DialogDescriptor dialogDescriptor = new DialogDescriptor(panel, NbBundle.getMessage(LockParams.class, "MSG_LockParams.title"), // NOI18N
                true, new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(this.getClass()), null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setVisible(true);
        return dialogDescriptor.getValue() == okButton;
    }

    boolean isForce () {
        return panel.cbForce.isSelected();
    }

    String getLockMessage () {
        return panel.txtLockMessage.getText().trim();
    }
    
}
