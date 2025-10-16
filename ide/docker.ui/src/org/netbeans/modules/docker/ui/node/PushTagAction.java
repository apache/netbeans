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
package org.netbeans.modules.docker.ui.node;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.docker.api.Credentials;
import org.netbeans.modules.docker.api.DockerTag;
import org.netbeans.modules.docker.api.DockerAction;
import org.netbeans.modules.docker.api.DockerAuthenticationException;
import org.netbeans.modules.docker.api.DockerException;
import org.netbeans.modules.docker.api.DockerName;
import org.netbeans.modules.docker.ui.credentials.CredentialsUtils;
import org.netbeans.modules.docker.ui.output.StatusOutputListener;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
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
public class PushTagAction extends NodeAction {

    private static final Logger LOGGER = Logger.getLogger(PushTagAction.class.getName());

    @Override
    protected void performAction(Node[] activatedNodes) {
        for (final Node node : activatedNodes) {
            final DockerTag tag = node.getLookup().lookup(DockerTag.class);
            if (tag != null) {
                perform(tag);
            }
        }
    }

    @NbBundle.Messages({
        "# {0} - image name",
        "MSG_PushQuestion=Do you really want to push the image {0} to the registry?"
    })
    private void perform(final DockerTag tag) {
        NotifyDescriptor desc = new NotifyDescriptor.Confirmation(
                Bundle.MSG_PushQuestion(tag.getTag()), NotifyDescriptor.YES_NO_OPTION);
        if (DialogDisplayer.getDefault().notify(desc) != NotifyDescriptor.YES_OPTION) {
            return;
        }

        RequestProcessor.getDefault().post(new Push(tag));
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        DockerTag tag = activatedNodes[0].getLookup().lookup(DockerTag.class);
        if (tag == null) {
            return false;
        }
        return !"<none>:<none>".equals(tag.getTag());
    }

    @NbBundle.Messages("LBL_PushTagAction=Push...")
    @Override
    public String getName() {
        return Bundle.LBL_PushTagAction();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    private static class Push implements Runnable {

        private final DockerTag tag;

        public Push(DockerTag tag) {
            this.tag = tag;
        }

        @NbBundle.Messages({
            "# {0} - image name",
            "MSG_Pushing=Pushing {0}",
            "MSG_EditCredentials=Authentication failed. Do you want to configure credentials for the registry and retry?"
        })
        @Override
        public void run() {
            String image = tag.getTag();
            final InputOutput io = IOProvider.getDefault().getIO(Bundle.MSG_Pushing(image), false);
            ProgressHandle handle = ProgressHandle.createHandle(Bundle.MSG_Pushing(image), null, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    io.select();
                }
            });
            handle.start();
            try {
                io.getOut().reset();
                io.select();
                DockerAction facade = new DockerAction(tag.getImage().getInstance());
                facade.push(tag, new StatusOutputListener(io));
            } catch (DockerAuthenticationException ex) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        NotifyDescriptor desc = new NotifyDescriptor.Confirmation(
                                Bundle.MSG_EditCredentials(), NotifyDescriptor.YES_NO_OPTION);
                        if (DialogDisplayer.getDefault().notify(desc) != NotifyDescriptor.YES_OPTION) {
                            return;
                        }
                        DockerName name = DockerName.parse(tag.getTag());
                        Credentials c = CredentialsUtils.askForCredentials(name.getRegistry());
                        if (c != null) {
                            RequestProcessor.getDefault().post(Push.this);
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
