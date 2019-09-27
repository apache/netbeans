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
package org.netbeans.modules.docker.ui.commit;

import java.awt.Dialog;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.docker.api.DockerContainer;
import org.netbeans.modules.docker.api.DockerException;
import org.netbeans.modules.docker.api.DockerAction;
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
public class CommitContainerAction extends NodeAction {

    private static final Logger LOGGER = Logger.getLogger(CommitContainerAction.class.getName());

    @NbBundle.Messages({
        "LBL_Commit=&Commit",
        "# {0} - container id",
        "LBL_CommitContainer=Commit {0}"
    })
    @Override
    protected void performAction(Node[] activatedNodes) {
        DockerContainer container = activatedNodes[0].getLookup().lookup(DockerContainer.class);
        if (container != null) {
            JButton commitButton = new JButton();
            Mnemonics.setLocalizedText(commitButton, Bundle.LBL_Commit());
            CommitPanel panel = new CommitPanel(container.getInstance(), commitButton);

            DialogDescriptor descriptor
                    = new DialogDescriptor(panel, Bundle.LBL_CommitContainer(container.getShortId()),
                            true, new Object[] {commitButton, DialogDescriptor.CANCEL_OPTION}, commitButton,
                            DialogDescriptor.DEFAULT_ALIGN, null, null);
            descriptor.setClosingOptions(new Object[] {commitButton, DialogDescriptor.CANCEL_OPTION});
            panel.setMessageLine(descriptor.createNotificationLineSupport());
            Dialog dlg = null;

            try {
                dlg = DialogDisplayer.getDefault().createDialog(descriptor);
                dlg.setVisible(true);

                if (descriptor.getValue() == commitButton) {
                    perform(container, panel.getRepository(), panel.getTag(),
                            panel.getAuthor(), panel.getMessage(), panel.isPause());
                }
            } finally {
                if (dlg != null) {
                    dlg.dispose();
                }
            }
        }
    }

    @NbBundle.Messages({
        "# {0} - container id",
        "MSG_Commiting=Commiting {0}"
    })
    private void perform(final DockerContainer container, final String repository,
            final String tag, final String author, final String message, final boolean pause) {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                ProgressHandle handle = ProgressHandle.createHandle(Bundle.MSG_Commiting(container.getShortId()));
                handle.start();
                try {
                    DockerAction facade = new DockerAction(container.getInstance());
                    facade.commit(container, repository, tag, author, message, pause);
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
        return activatedNodes[0].getLookup().lookup(DockerContainer.class) != null;
    }

    @NbBundle.Messages("LBL_CommitContainerAction=Commit...")
    @Override
    public String getName() {
        return Bundle.LBL_CommitContainerAction();
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
