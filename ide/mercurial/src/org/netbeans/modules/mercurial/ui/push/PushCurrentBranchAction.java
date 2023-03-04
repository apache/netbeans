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

import java.io.File;
import java.util.Set;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Ondrej Vrabec
 */
@NbBundle.Messages({
    "CTL_MenuItem_PushBranchLocal=Pu&sh Current Branch",
    "# {0} - repository folder name",
    "CTL_MenuItem_PushBranchRoot=Pu&sh Current Branch - {0}"
})
@ActionID(id = "org.netbeans.modules.mercurial.ui.push.PushCurrentBranchAction", category = "Mercurial")
@ActionRegistration(lazy = false, displayName = "#CTL_MenuItem_PushBranchLocal")
public class PushCurrentBranchAction extends ContextAction {
    
    public static final String ICON_RESOURCE = "org/netbeans/modules/mercurial/resources/icons/push.png"; //NOI18N
    
    public PushCurrentBranchAction () {
        super(ICON_RESOURCE);
    }
    
    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        return HgUtils.isFromHgRepository(context);
    }
    
    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_PushBranchLocal"; //NOI18N
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }

    @Override
    public String getName(String role, Node[] activatedNodes) {
        VCSContext ctx = HgUtils.getCurrentContext(activatedNodes);
        Set<File> roots = HgUtils.getRepositoryRoots(ctx);
        return roots.size() == 1 
                ? NbBundle.getMessage(PushCurrentBranchAction.class, "CTL_MenuItem_PushBranchRoot", roots.iterator().next().getName()) //NOI18N
                : NbBundle.getMessage(PushCurrentBranchAction.class, "CTL_MenuItem_PushBranchLocal"); //NOI18N
    }

    @Override
    @NbBundle.Messages({
        "# {0} - branch name", "MSG_PUSH_BRANCH_PROGRESS=Pushing {0}"
    })
    protected void performContextAction (Node[] nodes) {
        final VCSContext context = HgUtils.getCurrentContext(nodes);
        final Set<File> repositoryRoots = HgUtils.getRepositoryRoots(context);
        // run the whole bulk operation in background
        Mercurial.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                for (File repositoryRoot : repositoryRoots) {
                    final File root = repositoryRoot;
                    final boolean[] canceled = new boolean[1];
                    final String branch;
                    try {
                        branch = HgCommand.getBranch(root);
                        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
                        // run every repository fetch in its own support with its own output window
                        HgProgressSupport support = new HgProgressSupport() {
                            @Override
                            public void perform() {
                                PushAction.getDefaultAndPerformPush(root, null, branch, this.getLogger());
                                canceled[0] = isCanceled();
                            }
                        };
                        support.start(rp, root, Bundle.MSG_PUSH_BRANCH_PROGRESS(branch)).waitFinished();
                        if (canceled[0]) {
                            break;
                        }
                    } catch (HgException.HgCommandCanceledException ex) {
                        // canceled by user, do nothing
                    } catch (HgException ex) {
                        HgUtils.notifyException(ex);
                    }
                }
            }
        });
    }
}
