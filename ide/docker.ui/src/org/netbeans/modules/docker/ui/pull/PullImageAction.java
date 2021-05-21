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
package org.netbeans.modules.docker.ui.pull;

import org.netbeans.modules.docker.ui.output.StatusOutputListener;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.docker.api.Credentials;
import org.netbeans.modules.docker.api.DockerInstance;
import org.netbeans.modules.docker.api.DockerException;
import org.netbeans.modules.docker.api.DockerAction;
import org.netbeans.modules.docker.api.DockerAuthenticationException;
import org.netbeans.modules.docker.api.DockerName;
import org.netbeans.modules.docker.ui.credentials.CredentialsUtils;
import org.netbeans.modules.docker.ui.node.StatefulDockerInstance;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public class PullImageAction extends NodeAction {

    private static final Logger LOGGER = Logger.getLogger(PullImageAction.class.getName());

    @NbBundle.Messages({
        "LBL_Pull=&Pull",
        "LBL_SearchImage=Search Image"
    })
    @Override
    protected void performAction(Node[] activatedNodes) {
        DockerInstance instance = activatedNodes[0].getLookup().lookup(DockerInstance.class);
        if (instance != null) {
            JButton pullButton = new JButton();
            Mnemonics.setLocalizedText(pullButton, Bundle.LBL_Pull());
            DockerHubSearchPanel panel = new DockerHubSearchPanel(instance, pullButton);

            DialogDescriptor descriptor
                    = new DialogDescriptor(panel, Bundle.LBL_SearchImage(),
                            true, new Object[]{pullButton, DialogDescriptor.CANCEL_OPTION}, pullButton,
                            DialogDescriptor.DEFAULT_ALIGN, null, null);
            descriptor.setClosingOptions(new Object[]{pullButton, DialogDescriptor.CANCEL_OPTION});
            Dialog dlg = null;

            try {
                dlg = DialogDisplayer.getDefault().createDialog(descriptor);
                dlg.setVisible(true);

                if (descriptor.getValue() == pullButton) {
                    perform(instance, panel.getImage());
                }
            } finally {
                if (dlg != null) {
                    dlg.dispose();
                }
            }
        }
    }

    private void perform(final DockerInstance instance, final String image) {
        RequestProcessor.getDefault().post(new Pull(instance, image));
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        StatefulDockerInstance checked = activatedNodes[0].getLookup().lookup(StatefulDockerInstance.class);
        if (checked == null || !checked.isAvailable()) {
            return false;
        }

        return activatedNodes[0].getLookup().lookup(DockerInstance.class) != null;
    }

    @NbBundle.Messages("LBL_PullImageAction=Pull...")
    @Override
    public String getName() {
        return Bundle.LBL_PullImageAction();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    private static class Pull implements Runnable {

        private final DockerInstance instance;

        private final String image;

        public Pull(DockerInstance instance, String image) {
            this.instance = instance;
            this.image = image;
        }

        @NbBundle.Messages({
            "# {0} - image name",
            "MSG_Pulling=Pulling {0}",
            "MSG_EditCredentials=Authentication failed. Do you want to configure credentials for the registry and retry?"
        })
        @Override
        public void run() {
            final InputOutput io = IOProvider.getDefault().getIO(Bundle.MSG_Pulling(image), false);
            ProgressHandle handle = ProgressHandleFactory.createHandle(Bundle.MSG_Pulling(image), new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    io.select();
                }
            });
            handle.start();
            try {
                io.getOut().reset();
                io.select();
                DockerAction facade = new DockerAction(instance);
                facade.pull(image, new StatusOutputListener(io));
            } catch (DockerAuthenticationException ex) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        NotifyDescriptor desc = new NotifyDescriptor.Confirmation(
                                Bundle.MSG_EditCredentials(), NotifyDescriptor.YES_NO_OPTION);
                        if (DialogDisplayer.getDefault().notify(desc) != NotifyDescriptor.YES_OPTION) {
                            return;
                        }
                        DockerName name = DockerName.parse(image);
                        Credentials c = CredentialsUtils.askForCredentials(name.getRegistry());
                        if (c != null) {
                            RequestProcessor.getDefault().post(Pull.this);
                        }
                    }
                });
            } catch (DockerException ex) {
                LOGGER.log(Level.INFO, null, ex);
                io.getErr().println(ex.getMessage());
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } finally {
                io.getOut().close();
                handle.finish();
            }
        }
    }
}
