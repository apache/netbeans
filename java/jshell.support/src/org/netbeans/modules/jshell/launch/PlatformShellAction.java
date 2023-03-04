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
