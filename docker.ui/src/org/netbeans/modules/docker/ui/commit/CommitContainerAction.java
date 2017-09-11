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
