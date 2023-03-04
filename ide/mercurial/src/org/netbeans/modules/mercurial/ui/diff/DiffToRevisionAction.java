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
package org.netbeans.modules.mercurial.ui.diff;

import java.awt.EventQueue;
import java.io.File;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage.HgRevision;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Ondrej Vrabec
 */
@NbBundle.Messages({
    "CTL_MenuItem_DiffToRevision=Diff &To Revision...",
    "CTL_PopupMenuItem_DiffToRevision=Diff To Revision..."
})
public class DiffToRevisionAction extends ContextAction {
    
    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        return HgUtils.isFromHgRepository(context);
    }

    @Override
    protected String getBaseName (Node[] nodes) {
        return "CTL_MenuItem_DiffToRevision"; //NOI18N
    }

    @Override
    protected void performContextAction (Node[] nodes) {
        final VCSContext context = HgUtils.getCurrentContext(nodes);
        final File[] actionRoots = HgUtils.getActionRoots(context);
        if (actionRoots == null || actionRoots.length == 0) {
            return;
        }
        Utils.post(new Runnable() {
            @Override
            public void run () {
                final File repository = Mercurial.getInstance().getRepositoryRoot(actionRoots[0]);
                try {
                    final HgRevision parent = HgCommand.getParent(repository, null, null);
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            String contextName = Utils.getContextDisplayName(context);
                            diff(repository, actionRoots, parent, contextName);
                        }
                    });
                } catch (HgException.HgCommandCanceledException ex) {
                } catch (HgException ex) {
                    HgUtils.notifyException(ex);
                }
            }
        });
    }
    
    private void diff (File repository, File[] roots, HgRevision wcParent, String contextDisplayName) {
        DiffToRevision diffPanel = new DiffToRevision(repository, wcParent);
        if (diffPanel.showDialog()) {
            SystemAction.get(DiffAction.class).diff(roots, diffPanel.getSelectedTreeFirst(),
                    diffPanel.getSelectedTreeSecond(), contextDisplayName, false, true);
        }
    }
}
