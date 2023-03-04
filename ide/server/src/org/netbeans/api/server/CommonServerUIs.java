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
package org.netbeans.api.server;

import java.awt.Dialog;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.server.ServerRegistry;
import org.netbeans.modules.server.ui.manager.ServerManagerPanel;
import org.netbeans.modules.server.ui.wizard.AddServerInstanceWizard;

/**
 * Class providing access to UI dialogs managing instances.
 *
 * @author Petr Hejl
 */
public final class CommonServerUIs {

    private CommonServerUIs() {
        super();
    }

    /**
     * Displays the modal server manager dialog with the specified server instance
     * preselected. This method must be called form the AWT event dispatch
     * thread.
     *
     * @param instance server instance which should be preselected,
     *             if <code>null</code> the first server instance will be preselected
     */
    public static void showCustomizer(ServerInstance instance) {
        showCustomizer(instance, ServerRegistry.getInstance());
    }

    /**
     * Displays the modal cloud manager dialog with the specified cloud instance
     * preselected. This method must be called form the AWT event dispatch
     * thread.
     *
     * @param instance cloud instance which should be preselected,
     *             if <code>null</code> the first cloud instance will be preselected
     * @since 1.15
     */
    public static void showCloudCustomizer(ServerInstance instance) {
        showCustomizer(instance, ServerRegistry.getCloudInstance());
    }
    
    private static void showCustomizer(ServerInstance instance, ServerRegistry registry) {
        assert SwingUtilities.isEventDispatchThread() : "Invocation of the UI dialog outside of the EDT"; // NOI18N

        ServerManagerPanel customizer = new ServerManagerPanel(instance, registry);

        JButton close = new JButton(NbBundle.getMessage(CommonServerUIs.class, "CTL_Close"));
        close.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CommonServerUIs.class, "AD_Close"));

        DialogDescriptor descriptor = new DialogDescriptor(customizer,
                NbBundle.getMessage(CommonServerUIs.class, registry.isCloud() ? "TXT_CloudManager" : "TXT_ServerManager"),
                true,
                new Object[] {close},
                close,
                DialogDescriptor.DEFAULT_ALIGN,
                registry.isCloud() ? new HelpCtx("org.netbeans.api.server.CommonCloudUIs"): new HelpCtx("org.netbeans.api.server.CommonServerUIs"),
                null);

        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        try {
            dlg.setVisible(true);
        } finally {
            dlg.dispose();
        }
    }

    /**
     * Displays the modal wizard for creating new server instance. This method must be
     * called from the AWT event dispatch thread.
     *
     * @return created instance or {@code null} if user canceled the operation
     * @since 1.18
     */
    @CheckForNull
    public static ServerInstance showAddServerInstanceWizard() {
        assert SwingUtilities.isEventDispatchThread() : "Invocation of the UI dialog outside of the EDT"; //NOI18N

        final ServerInstance instance = AddServerInstanceWizard.showAddServerInstanceWizard();
        return instance;
    }
}
