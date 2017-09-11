/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.terminal.action;

import java.awt.Dialog;
import org.netbeans.modules.dlight.terminal.ui.RemoteInfoDialog;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Voskresensky
 */
@ActionID(id = "RemoteTerminalAction", category = "Window")
@ActionRegistration(iconInMenu = true, displayName = "#RemoteTerminalShortDescr", iconBase = "org/netbeans/modules/dlight/terminal/action/remote_term.png")
@ActionReference(path = TerminalAction.TERMINAL_ACTIONS_PATH, name = "org-netbeans-modules-dlight-terminal-action-RemoteTerminalAction", position = 200)
public final class RemoteTerminalAction extends TerminalAction {

    private final RemoteInfoDialog cfgPanel;

    public RemoteTerminalAction() {
        super("RemoteTerminalAction", NbBundle.getMessage(RemoteTerminalAction.class, "RemoteTerminalShortDescr"), // NOI18N
                ImageUtilities.loadImageIcon("org/netbeans/modules/dlight/terminal/action/remote_term.png", false)); // NOI18N
        cfgPanel = new RemoteInfoDialog(System.getProperty("user.name"));
    }

    @Override
    protected ExecutionEnvironment getEnvironment() {
        String title = NbBundle.getMessage(RemoteTerminalAction.class, "RemoteConnectionTitle");
        cfgPanel.init();
        DialogDescriptor dd = new DialogDescriptor(cfgPanel, title, // NOI18N
                true, DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION, null);

        Dialog cfgDialog = DialogDisplayer.getDefault().createDialog(dd);
        
        try {
            cfgDialog.setVisible(true);
        } catch (Throwable th) {
            if (!(th.getCause() instanceof InterruptedException)) {
                throw new RuntimeException(th);
            }
            dd.setValue(DialogDescriptor.CANCEL_OPTION);
        } finally {
            cfgDialog.dispose();
        }

        if (dd.getValue() != DialogDescriptor.OK_OPTION) {
            return null;
        }

        final ExecutionEnvironment env = cfgPanel.getExecutionEnvironment();
        return env;
    }
}
