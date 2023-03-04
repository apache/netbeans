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
package org.netbeans.modules.docker.ui.credentials;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
@ActionID(id = "org.netbeans.modules.docker.ui.credentials.CredentialsAction", category = "System")
@ActionRegistration(displayName = "#LBL_CredentialsAction")
@ActionReferences(
    @ActionReference(path = "Docker/Credentials", position = 200)
)
public class CredentialsAction implements ActionListener {

    @NbBundle.Messages({
        "LBL_Credentials=Credentials",
        "LBL_Close=Close"
    })
    @Override
    public void actionPerformed(ActionEvent e) {
        CredentialsListPanel panel = new CredentialsListPanel();
        JButton closeButton = new JButton(Bundle.LBL_Close());
        DialogDescriptor descriptor = new DialogDescriptor(panel, Bundle.LBL_Credentials(),
                        true, new Object[]{closeButton}, closeButton,
                        DialogDescriptor.DEFAULT_ALIGN, null, null);
        descriptor.setClosingOptions(new Object[]{closeButton});

        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            dlg.setVisible(true);
        } finally {
            if (dlg != null) {
                dlg.dispose();
            }
        }
    }
}
