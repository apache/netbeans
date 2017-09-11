/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
