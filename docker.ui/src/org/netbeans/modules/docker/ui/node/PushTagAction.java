/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.ui.node;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
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
            ProgressHandle handle = ProgressHandleFactory.createHandle(Bundle.MSG_Pushing(image), new AbstractAction() {
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
