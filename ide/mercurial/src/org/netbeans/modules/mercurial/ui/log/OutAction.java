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
package org.netbeans.modules.mercurial.ui.log;

import org.netbeans.modules.versioning.spi.VCSContext;
import java.io.File;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Log action for mercurial:
 * hg log - show revision history of entire repository or files
 *
 * @author John Rice
 */
@NbBundle.Messages({
    "CTL_MenuItem_ShowOut=Show &Outgoing"
})
public class OutAction extends SearchHistoryAction {

    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_ShowOut";                                  //NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        openOut(context);
    }

    /**
     * Opens the Seach History panel to view Mercurial Out Changesets that will be sent on next Push to remote repo
     * using: hg out - to get the data
     */
    private void openOut (VCSContext context) {
        File repositoryRoot = getRepositoryRoot(context);
        if (repositoryRoot == null) {
            return;
        }
        outputSearchContextTab(repositoryRoot, "MSG_LogOut_Title");
        SearchHistoryTopComponent tc = new SearchHistoryTopComponent(HgUtils.filterForRepository(context, repositoryRoot, false), "", ""); //NOI18N
        tc.setDisplayName(NbBundle.getMessage(OutAction.class, "MSG_Out_TabTitle", repositoryRoot.getName()));
        tc.open();
        tc.requestActive();
        tc.searchOut();
    }
}
