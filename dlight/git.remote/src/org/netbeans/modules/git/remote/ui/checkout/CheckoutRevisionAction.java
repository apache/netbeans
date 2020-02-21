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

package org.netbeans.modules.git.remote.ui.checkout;

import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.remote.ui.repository.RevisionDialogController;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.git.remote.ui.checkout.CheckoutRevisionAction", category = "GitRemote")
@ActionRegistration(displayName = "#LBL_CheckoutRevisionAction_Name")
public class CheckoutRevisionAction extends AbstractCheckoutAction {

    private static final String ICON_RESOURCE = "org/netbeans/modules/git/remote/resources/icons/checkout.png"; //NOI18N
    
    public CheckoutRevisionAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    @Override
    protected void performAction (VCSFileProxy repository, VCSFileProxy[] roots, VCSContext context) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        checkoutRevision(repository, info.getActiveBranch().getName().equals(GitBranch.NO_BRANCH) ? GitUtils.HEAD : info.getActiveBranch().getName());
    }

    public void checkoutRevision (final VCSFileProxy repository, String preselectedRevision) {
        checkoutRevision(repository, new CheckoutRevision(repository, RepositoryInfo.getInstance(repository), preselectedRevision), "LBL_CheckoutRevisionAction.progressName", //NOI18N
                new HelpCtx(CheckoutRevisionAction.class));
    }
    
    private static class CheckoutRevision extends AbstractCheckoutRevision {

        public CheckoutRevision (VCSFileProxy repository, RepositoryInfo info, String initialRevision) {
            super(info, new RevisionDialogController(repository, new VCSFileProxy[] { repository }, initialRevision));
            panel.jLabel1.setText(NbBundle.getMessage(CheckoutRevisionAction.class, "CheckoutRevision.jLabel1.text")); //NOI18N
        }

        @Override
        protected String getOkButtonLabel () {
            return NbBundle.getMessage(CheckoutRevisionAction.class, "LBL_CheckoutRevision.OKButton.text"); //NOI18N
        }

        @Override
        protected String getDialogTitle () {
            return NbBundle.getMessage(CheckoutRevisionAction.class, "LBL_CheckoutRevision.title"); //NOI18N
        }
    }
}
