/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.mercurial.ui.branch;

import java.awt.EventQueue;
import java.io.File;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.commit.CommitAction;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.RequestProcessor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

@ActionID(id = "org.netbeans.modules.mercurial.ui.branch.CloseBranchAction", category = "Mercurial")
@ActionRegistration(displayName = "#CTL_MenuItem_CloseBranch")
@NbBundle.Messages({
    "CTL_MenuItem_CloseBranch=C&lose Branch...",
    "CTL_PopupMenuItem_CloseBranch=Close Branch..."
})
public class CloseBranchAction extends ContextAction {
    
    @Override
    protected boolean enable(Node[] nodes) {
        return HgUtils.isFromHgRepository(HgUtils.getCurrentContext(nodes));
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_CloseBranch"; //NOI18N
    }

    @NbBundle.Messages({
        "# {0} - repository name", "MSG_CloseBranchAction.interruptedRebase.error=Repository {0} is in the middle of an interrupted rebase.\n"
            + "Finish the rebase before closing this branch."
    })
    @Override
    protected void performContextAction(Node[] nodes) {
        VCSContext ctx = HgUtils.getCurrentContext(nodes);
        final File roots[] = HgUtils.getActionRoots(ctx);
        if (roots == null || roots.length == 0) return;
        final File root = Mercurial.getInstance().getRepositoryRoot(roots[0]);

        final RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                setDisplayName(NbBundle.getMessage(CloseBranchAction.class, "MSG_CloseBranch.Progress.preparing")); //NOI18N
                try {
                    final String branchName = HgCommand.getBranch(root);
                    if (branchName.equals(HgBranch.DEFAULT_NAME)) {
                        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(CloseBranchAction.class, "MSG_CloseBranch.error.defaultBranch"), NotifyDescriptor.ERROR_MESSAGE); //NOI18N
                        DialogDisplayer.getDefault().notifyLater(nd);
                        return;
                    }
                    int numberOfHeads = getBranchHeadCount(branchName);
                    if (numberOfHeads == 0) {
                        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(CloseBranchAction.class, "MSG_CloseBranch.error.branchClosed", //NOI18N
                                new Object[] { branchName }), NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(nd);
                    } else if (numberOfHeads > 1) {
                        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(CloseBranchAction.class, "MSG_CloseBranch.error.moreHeads", //NOI18N
                                new Object[] { branchName, numberOfHeads }), NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(nd);
                    } else if (HgUtils.isRebasing(root)) {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                Bundle.MSG_CloseBranchAction_interruptedRebase_error(root.getName()),
                                NotifyDescriptor.ERROR_MESSAGE));
                    } else if (!isCanceled()) {
                        final VCSContext ctx = VCSContext.forNodes(new Node[] { new AbstractNode(Children.LEAF, Lookups.fixed((Object[]) roots)) {
                            @Override
                            public String getDisplayName () {
                                return root.getName();
                            }
                        }});
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run () {
                                SystemAction.get(CommitAction.class).closeBranch(branchName, ctx, 
                                        NbBundle.getMessage(CloseBranchAction.class, "MSG_CloseBranch.commit.title", new Object[] { branchName, Utils.getContextDisplayName(ctx) })); //NOI18N
                            }
                        });
                    }
                } catch (HgException.HgCommandCanceledException ex) {
                    // canceled by user, do nothing
                } catch (HgException ex) {
                    HgUtils.notifyException(ex);
                }
            }

            private int getBranchHeadCount (String branchName) throws HgException {
                HgLogMessage[] messages = HgCommand.getHeadRevisionsInfo(root, false, getLogger());
                int headsInBranch = 0;
                for (HgLogMessage message : messages) {
                    if (message.getBranches().length == 0 && branchName.equals(HgBranch.DEFAULT_NAME)) {
                        ++headsInBranch;
                    }
                    for (String b : message.getBranches()) {
                        if (b.equals(branchName)) {
                            ++headsInBranch;
                        }
                    }
                }
                return headsInBranch;
            }
        };
        support.start(rp, root, NbBundle.getMessage(CloseBranchAction.class, "MSG_CloseBranch.Progress")); //NOI18N
    }
}
