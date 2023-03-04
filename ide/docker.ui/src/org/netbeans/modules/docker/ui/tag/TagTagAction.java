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
package org.netbeans.modules.docker.ui.tag;

import java.awt.Dialog;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.docker.api.DockerTag;
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
public class TagTagAction extends NodeAction {

    private static final Logger LOGGER = Logger.getLogger(TagTagAction.class.getName());

    @NbBundle.Messages({
        "LBL_Tag=&Tag",
        "# {0} - tag",
        "LBL_TagTag=Tag {0}"
    })
    @Override
    protected void performAction(Node[] activatedNodes) {
        DockerTag tag = activatedNodes[0].getLookup().lookup(DockerTag.class);
        if (tag != null) {
            JButton tagButton = new JButton();
            Mnemonics.setLocalizedText(tagButton, Bundle.LBL_Tag());
            TagPanel panel = new TagPanel(tag.getImage().getInstance(), tagButton);

            DialogDescriptor descriptor
                    = new DialogDescriptor(panel, Bundle.LBL_TagTag(tag.getTag()),
                            true, new Object[] {tagButton, DialogDescriptor.CANCEL_OPTION}, tagButton,
                            DialogDescriptor.DEFAULT_ALIGN, null, null);
            descriptor.setClosingOptions(new Object[] {tagButton, DialogDescriptor.CANCEL_OPTION});
            panel.setMessageLine(descriptor.createNotificationLineSupport());
            Dialog dlg = null;

            try {
                dlg = DialogDisplayer.getDefault().createDialog(descriptor);
                dlg.setVisible(true);

                if (descriptor.getValue() == tagButton) {
                    perform(tag, panel.getRepository(), panel.getTag(), panel.isForce());
                }
            } finally {
                if (dlg != null) {
                    dlg.dispose();
                }
            }
        }
    }

    @NbBundle.Messages({
        "# {0} - tag",
        "MSG_Tagging=Tagging {0}"
    })
    private void perform(final DockerTag source, final String repository,
            final String tag, final boolean force) {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                ProgressHandle handle = ProgressHandle.createHandle(Bundle.MSG_Tagging(source.getTag()));
                handle.start();
                try {
                    DockerAction facade = new DockerAction(source.getImage().getInstance());
                    facade.tag(source, repository, tag, force);
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
        return activatedNodes[0].getLookup().lookup(DockerTag.class) != null;
    }

    @NbBundle.Messages("LBL_TagTagAction=Tag...")
    @Override
    public String getName() {
        return Bundle.LBL_TagTagAction();
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
