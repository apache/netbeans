/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
