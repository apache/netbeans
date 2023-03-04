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
package org.netbeans.modules.docker.ui.rename;

import java.awt.Dialog;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.docker.api.DockerException;
import org.netbeans.modules.docker.api.DockerAction;
import org.netbeans.modules.docker.ui.node.StatefulDockerContainer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Petr Hejl
 */
public class RenameContainerAction extends NodeAction {

    private static final Logger LOGGER = Logger.getLogger(RenameContainerAction.class.getName());

    @NbBundle.Messages({
        "LBL_Rename=&Rename",
        "# {0} - container name",
        "LBL_RenameContainer=Rename {0}"
    })
    @Override
    protected void performAction(Node[] activatedNodes) {
        StatefulDockerContainer container = activatedNodes[0].getLookup().lookup(StatefulDockerContainer.class);
        if (container != null) {
            JButton renameButton = new JButton();
            Mnemonics.setLocalizedText(renameButton, Bundle.LBL_Rename());
            RenamePanel panel = new RenamePanel(renameButton);

            DialogDescriptor descriptor
                    = new DialogDescriptor(panel, Bundle.LBL_RenameContainer(container.getDetail().getName()),
                            true, new Object[] {renameButton, DialogDescriptor.CANCEL_OPTION}, renameButton,
                            DialogDescriptor.DEFAULT_ALIGN, null, null);
            descriptor.setClosingOptions(new Object[] {renameButton, DialogDescriptor.CANCEL_OPTION});
            panel.setMessageLine(descriptor.createNotificationLineSupport());
            Dialog dlg = null;

            try {
                dlg = DialogDisplayer.getDefault().createDialog(descriptor);
                dlg.setVisible(true);

                if (descriptor.getValue() == renameButton) {
                    perform(container, panel.getContainerName());
                }
            } finally {
                if (dlg != null) {
                    dlg.dispose();
                }
            }
        }
    }

    @NbBundle.Messages({
        "# {0} - container name",
        "MSG_Renaming=Renaming {0}"
    })
    private void perform(final StatefulDockerContainer container, final String name) {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                ProgressHandle handle = ProgressHandle.createHandle(Bundle.MSG_Renaming(container.getDetail().getName()));
                handle.start();
                try {
                    DockerAction facade = new DockerAction(container.getContainer().getInstance());
                    facade.rename(container.getContainer(), name);
                } catch (DockerException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                    String msg = ex.getLocalizedMessage();
                    NotifyDescriptor desc = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                } finally {
                    handle.finish();
                }
            }
        });
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        return activatedNodes[0].getLookup().lookup(StatefulDockerContainer.class) != null;
    }

    @NbBundle.Messages("LBL_RenameContainerAction=Rename...")
    @Override
    public String getName() {
        return Bundle.LBL_RenameContainerAction();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

}
