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
package org.netbeans.modules.mercurial.ui.push;

import java.net.URISyntaxException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.netbeans.modules.versioning.spi.VCSContext;

import javax.swing.*;
import java.io.File;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.ui.repository.Repository;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.repository.RepositoryConnection;
import org.netbeans.modules.mercurial.ui.wizards.CloneRepositoryWizardPanel;
import org.netbeans.modules.mercurial.util.HgProjectUtils;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.HelpCtx;

/**
 * Push Other action for mercurial: 
 * hg push - push changes to the specified target
 * 
 * @author John Rice
 */
@NbBundle.Messages({
    "CTL_MenuItem_PushOther=Pus&h..."
})
@ActionID(id = "org.netbeans.modules.mercurial.ui.push.PushOtherAction", category = "Mercurial")
@ActionRegistration(lazy = false, displayName = "#CTL_MenuItem_PushOther")
public class PushOtherAction extends ContextAction {
    
    public static final String ICON_RESOURCE = "org/netbeans/modules/mercurial/resources/icons/push-setting.png"; //NOI18N
    
    public PushOtherAction () {
        super(ICON_RESOURCE);
    }
    
    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        Set<File> ctxFiles = context != null? context.getRootFiles(): null;
        if(!HgUtils.isFromHgRepository(context) || ctxFiles == null || ctxFiles.size() == 0)
            return false;
        return true; // #121293: Speed up menu display, warn user if not set when Push selected
    }

    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_PushOther";                                //NOI18N
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        final File roots[] = HgUtils.getActionRoots(context);
        if (roots == null || roots.length == 0) return;
        final File root = Mercurial.getInstance().getRepositoryRoot(roots[0]);
        int repositoryModeMask = Repository.FLAG_URL_ENABLED | Repository.FLAG_SHOW_HINTS | Repository.FLAG_SHOW_PROXY;
        String title = org.openide.util.NbBundle.getMessage(CloneRepositoryWizardPanel.class, "CTL_Repository_Location");       // NOI18N
        final JButton pushButton = new JButton();
        final Repository repository = new Repository(repositoryModeMask, title, true, root);
        repository.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                pushButton.setEnabled(repository.isValid());
            }
        });
        org.openide.awt.Mnemonics.setLocalizedText(pushButton, org.openide.util.NbBundle.getMessage(PushOtherAction.class, "CTL_Push_Action_Push")); // NOI18N
        pushButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PushOtherAction.class, "ACSD_Push_Action_Push")); // NOI18N
        pushButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PushOtherAction.class, "ACSN_Push_Action_Push")); // NOI18N
        JButton cancelButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(PushOtherAction.class, "CTL_Push_Action_Cancel")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PushOtherAction.class, "ACSD_Push_Action_Cancel")); //NOI18N
        cancelButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PushOtherAction.class, "ACSN_Push_Action_Cancel")); // NOI18N

        pushButton.setEnabled(false);
        Object option = repository.show(org.openide.util.NbBundle.getMessage(PushOtherAction.class, "CTL_PushDialog_Title"),
                                        new HelpCtx(PushOtherAction.class),
                                        new Object[] {pushButton, cancelButton},
                                        true,
                                        "hg.push.dialog");

        if (option == pushButton) {
            final HgURL pushPath;
            try {
                pushPath = repository.getUrl();
            } catch (URISyntaxException ex) {
                Mercurial.LOG.log(Level.SEVERE,
                                  this.getClass().getName()
                                          + ": Could not push because of invalid URI." //NOI18N
                                          + repository.getUrlString());
                Mercurial.LOG.log(Level.SEVERE,
                                  this.getClass().getName()
                                  + ": Invalid URI: "                   //NOI18N
                                  + repository.getUrlString());
                return;
            }

            push(context, root, pushPath, repository.getRepositoryConnection());
        }
    }

    private static void push(final VCSContext ctx, final File root, final HgURL pushPath, final RepositoryConnection rc) {
        if (root == null || pushPath == null) return;
        final String fromPrjName = HgProjectUtils.getProjectName(root);
        final String toPrjName = NbBundle.getMessage(PushAction.class, "MSG_EXTERNAL_REPOSITORY"); // NOI18N
         
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() { 
                if (rc != null) {
                    HgModuleConfig.getDefault().insertRecentUrl(rc);
                }
               PushAction.performPush(root, pushPath, fromPrjName, toPrjName, null, null, this.getLogger(), false);
            } 
        };

        support.start(rp, root,
                org.openide.util.NbBundle.getMessage(PushAction.class, "MSG_PUSH_PROGRESS")); // NOI18N
    }
}
