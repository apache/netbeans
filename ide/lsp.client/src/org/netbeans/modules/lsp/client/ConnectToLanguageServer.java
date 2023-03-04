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
package org.netbeans.modules.lsp.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import org.openide.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Tools",
        id = "org.netbeans.modules.lsp.client.ConnectToLanguageServer"
)
@ActionRegistration(
        displayName = "#CTL_ConnectToLanguageServer"
)
@Messages("CTL_ConnectToLanguageServer=Connect to Language Server")
public final class ConnectToLanguageServer implements ActionListener {

    @Override
    @Messages({"ERR_CanFindRoot=Can't find the specified root directory",
               "ERR_InvalidPort=The specified port is invalid"
    })
    public void actionPerformed(ActionEvent e) {
        ConnectToLanguageServerParameters params = new ConnectToLanguageServerParameters();
        DialogDescriptor dd = new DialogDescriptor(params, "Connect to Language Server", true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, null);

        if (DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION) {
            params.confirm();
            FileObject root = FileUtil.toFileObject(new File(params.getRoot()));

            if (root == null) {
                DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(Bundle.ERR_CanFindRoot(), NotifyDescriptor.ERROR_MESSAGE));
                return ;
            }

            int port;

            try {
                port = Integer.parseInt(params.getPort());
            } catch (NumberFormatException ex) {
                DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(Bundle.ERR_InvalidPort(), NotifyDescriptor.ERROR_MESSAGE));
                return ;
            }

            LSPBindings.addBindings(root, port, params.getExtensions().split(","));
        }
    }
}
