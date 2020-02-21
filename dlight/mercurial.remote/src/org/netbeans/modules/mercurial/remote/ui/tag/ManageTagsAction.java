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
package org.netbeans.modules.mercurial.remote.ui.tag;

import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.mercurial.remote.ui.actions.ContextAction;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;

/**
 * 
 */
@ActionID(id = "org.netbeans.modules.mercurial.remote.ui.tag.ManageTagsAction", category = "MercurialRemote")
@ActionRegistration(displayName = "#CTL_MenuItem_ManageTags")
@Messages({
    "CTL_MenuItem_ManageTags=Mana&ge Tags...",
    "CTL_PopupMenuItem_ManageTags=Manage Tags..."
})
public class ManageTagsAction extends ContextAction {
    
    @Override
    protected boolean enable(Node[] nodes) {
        return HgUtils.isFromHgRepository(HgUtils.getCurrentContext(nodes));
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_ManageTags"; //NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        VCSContext ctx = HgUtils.getCurrentContext(nodes);
        final VCSFileProxy roots[] = HgUtils.getActionRoots(ctx);
        if (roots == null || roots.length == 0) {
            return;
        }
        final VCSFileProxy repository = Mercurial.getInstance().getRepositoryRoot(roots[0]);

        TagManager manager = new TagManager(repository);
        manager.showDialog();
    }
}
