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
package org.netbeans.modules.jshell.launch;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.jshell.env.JShellEnvironment;
import org.netbeans.modules.jshell.env.ShellRegistry;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
@ActionID(
        category = "Window",
        id = "org.netbeans.modules.jshell.launch.PlatformShellAction"
)
@ActionRegistration(
        displayName = "#DN_PlatformShell",
        iconInMenu = false
)
@ActionReference(
        position = 190, 
        name = "PlatformShellAction", path = "Menu/Tools"
)
@NbBundle.Messages({
    "DN_PlatformShell=Open Java Platform Shell",
    "ERR_NoShellPlatform=No suitable Java Platform configured. Do you want to configure Java Shell now ?"
})
public class PlatformShellAction extends AbstractAction {

    private ShellOptions options = ShellOptions.get();

    @NbBundle.Messages({
        "# {0} - error message",
        "ERR_RunPlatformShell=Error starting Java Shell: {0}"
    })
    @Override
    public void actionPerformed(ActionEvent e) {
        JavaPlatform platform = options.getSelectedPlatform();
        if (platform == null) {
            NotifyDescriptor.Confirmation conf = new NotifyDescriptor.Confirmation(
                    Bundle.ERR_NoShellPlatform(), NotifyDescriptor.Confirmation.OK_CANCEL_OPTION
            );
            Object result = DialogDisplayer.getDefault().notify(conf);
            if (result == NotifyDescriptor.Confirmation.OK_OPTION) {
                OptionsDisplayer.getDefault().open("Java/JShell", true);
            }
            platform = options.getSelectedPlatform();
            if (platform == null) {
                return;
            }
        }
        try {
            JShellEnvironment env = ShellRegistry.get().openDefaultSession(platform);
            env.open();
        } catch (IOException ex) {
            Message msg = new Message(Bundle.ERR_RunPlatformShell(ex.getLocalizedMessage()), Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(msg);
        }
    }
}
