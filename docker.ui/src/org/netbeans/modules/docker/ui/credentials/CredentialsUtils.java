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
package org.netbeans.modules.docker.ui.credentials;

import java.awt.Dialog;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.docker.api.Credentials;
import org.netbeans.modules.docker.api.CredentialsManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public final class CredentialsUtils {

    private CredentialsUtils() {
        super();
    }

    @CheckForNull
    public static Credentials askForCredentials(String registry) {
        assert SwingUtilities.isEventDispatchThread();

        String realRegistry = registry;
        if (realRegistry == null) {
            realRegistry = "https://index.docker.io/v1/"; // NOI18N
        }

        try {
            Credentials existing = CredentialsManager.getDefault().getCredentials(realRegistry);
            if (existing == null) {
                existing = new Credentials(realRegistry, null, new char[0], null);
            }
            return editCredentials(existing, Collections.<String>emptySet());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @NbBundle.Messages({
        "LBL_OK=&OK",
        "LBL_CredentialsDetailTitle=Registry Credentials"
    })
    @CheckForNull
    static Credentials editCredentials(Credentials existing, Set<String> registries) {
        assert SwingUtilities.isEventDispatchThread();

        try {
            JButton actionButton = new JButton();
            Mnemonics.setLocalizedText(actionButton, Bundle.LBL_OK());
            CredentialsPanel panel = new CredentialsPanel(actionButton, registries);
            if (existing != null) {
                panel.setCredentials(existing);
            }
            DialogDescriptor descriptor = new DialogDescriptor(panel, Bundle.LBL_CredentialsDetailTitle(), true,
                    new Object[]{actionButton, DialogDescriptor.CANCEL_OPTION}, actionButton,
                    DialogDescriptor.DEFAULT_ALIGN, null, null);
            descriptor.setClosingOptions(new Object[]{actionButton, DialogDescriptor.CANCEL_OPTION});
            panel.setMessageLine(descriptor.createNotificationLineSupport());

            Dialog dlg = null;
            try {
                dlg = DialogDisplayer.getDefault().createDialog(descriptor);
                dlg.setVisible(true);

                if (descriptor.getValue() == actionButton) {
                    Credentials credentials = panel.getCredentials();
                    CredentialsManager.getDefault().setCredentials(credentials);
                    return credentials;
                }
            } finally {
                if (dlg != null) {
                    dlg.dispose();
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
