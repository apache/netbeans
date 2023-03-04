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
package org.netbeans.modules.mercurial.ui.pull;

import java.net.URISyntaxException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.versioning.spi.VCSContext;

import javax.swing.*;
import java.io.File;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.ui.repository.Repository;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
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
 * Pull Other action for mercurial: 
 * hg pull - pull changes from the specified source
 * 
 * @author John Rice
 */
@NbBundle.Messages({
    "CTL_MenuItem_PullOther=Pu&ll..."
})
@ActionID(id = "org.netbeans.modules.mercurial.ui.pull.PullOtherAction", category = "Mercurial")
@ActionRegistration(lazy = false, displayName = "#CTL_MenuItem_PullOther")
public class PullOtherAction extends ContextAction {
    
    public static final String ICON_RESOURCE = "org/netbeans/modules/mercurial/resources/icons/pull-setting.png"; //NOI18N
    
    public PullOtherAction () {
        super(ICON_RESOURCE);
    }
    
    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        return HgUtils.isFromHgRepository(context);
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_PullOther";                                //NOI18N
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
        final JButton pullButton = new JButton();
        final Repository repository = new Repository(repositoryModeMask, title, true, root);
        repository.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                pullButton.setEnabled(repository.isValid());
            }
        });
        
        org.openide.awt.Mnemonics.setLocalizedText(pullButton, org.openide.util.NbBundle.getMessage(PullOtherAction.class, "CTL_Pull_Action_Pull")); // NOI18N
        pullButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PullOtherAction.class, "ACSD_Pull_Action_Pull")); // NOI18N
        pullButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PullOtherAction.class, "ACSN_Pull_Action_Pull")); // NOI18N
        JButton cancelButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(PullOtherAction.class, "CTL_Pull_Action_Cancel")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PullOtherAction.class, "ACSD_Pull_Action_Cancel")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PullOtherAction.class, "ACSN_Pull_Action_Cancel")); // NOI18N

        boolean dataValid = repository.isValid();
        pullButton.setEnabled(dataValid);

        Object option = repository.show(org.openide.util.NbBundle.getMessage(PullOtherAction.class, "CTL_PullDialog_Title"), 
                                        new HelpCtx(PullOtherAction.class),
                                        new Object[] {pullButton, cancelButton},
                                        true,
                                        "hg.pull.dialog");
        if (option == pullButton) {
            final HgURL pullSource;
            try {
                pullSource = repository.getUrl();
            } catch (URISyntaxException ex) {
                Mercurial.LOG.log(Level.SEVERE,
                                  this.getClass().getName()
                                          + ": Could not pull because of invalid URI." //NOI18N
                                          + repository.getUrlString());
                Mercurial.LOG.log(Level.SEVERE,
                                  this.getClass().getName()
                                          + ": Invalid URI: "           //NOI18N
                                          + repository.getUrlString());
                return;
            }
            pull(context, root, pullSource, repository.getRepositoryConnection());
        }
    }

    /**
     *
     * @param ctx
     * @param root
     * @param pullSource password is nulled
     */
    private static void pull(final VCSContext ctx, final File root, final HgURL pullSource, final RepositoryConnection rc) {
        if (root == null || pullSource == null) return;
        final String fromPrjName = NbBundle.getMessage(PullAction.class, "MSG_EXTERNAL_REPOSITORY"); // NOI18N
        final String toPrjName = HgProjectUtils.getProjectName(root);
         
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() { 
                if (rc != null) {
                    HgModuleConfig.getDefault().insertRecentUrl(rc);
                }
                PullAction.performPull(PullAction.PullType.OTHER, root, pullSource, fromPrjName, toPrjName, null, null, this);
            }
        };

        support.start(rp, root,
                org.openide.util.NbBundle.getMessage(PullAction.class, "MSG_PULL_PROGRESS")); // NOI18N
    }
}
