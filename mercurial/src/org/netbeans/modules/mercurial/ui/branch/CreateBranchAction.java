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

import java.io.File;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.WorkingCopyInfo;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

@ActionID(id = "org.netbeans.modules.mercurial.ui.branch.CreateBranchAction", category = "Mercurial")
@ActionRegistration(displayName = "#CTL_MenuItem_CreateBranch")
@NbBundle.Messages({
    "CTL_MenuItem_CreateBranch=Create &Branch...",
    "CTL_PopupMenuItem_CreateBranch=Create Branch..."
})
public class CreateBranchAction extends ContextAction {
    
    @Override
    protected boolean enable(Node[] nodes) {
        return HgUtils.isFromHgRepository(HgUtils.getCurrentContext(nodes));
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_CreateBranch"; // NOI18N
    }

    @Override
    @NbBundle.Messages({
        "# {0} - branch name", "MSG_CREATE_WC_MARKED=Working copy marked as branch {0}.\nDo not forget to commit to make the branch permanent."
    })
    protected void performContextAction(Node[] nodes) {
        VCSContext ctx = HgUtils.getCurrentContext(nodes);
        final File roots[] = HgUtils.getActionRoots(ctx);
        if (roots == null || roots.length == 0) return;
        final File root = Mercurial.getInstance().getRepositoryRoot(roots[0]);

        CreateBranch createBranch = new CreateBranch();
        if (!createBranch.showDialog()) {
            return;
        }
        final String branchName = createBranch.getBranchName();
        
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                OutputLogger logger = getLogger();
                try {
                    logger.outputInRed(NbBundle.getMessage(CreateBranchAction.class, "MSG_CREATE_TITLE")); //NOI18N
                    logger.outputInRed(NbBundle.getMessage(CreateBranchAction.class, "MSG_CREATE_TITLE_SEP")); //NOI18N
                    logger.output(NbBundle.getMessage(CreateBranchAction.class, "MSG_CREATE_INFO_SEP", branchName, root.getAbsolutePath())); //NOI18N
                    HgCommand.markBranch(root, branchName, logger);
                    WorkingCopyInfo.refreshAsync(root);
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                    Bundle.MSG_CREATE_WC_MARKED(branchName),
                                    NotifyDescriptor.INFORMATION_MESSAGE));
                    logger.output(Bundle.MSG_CREATE_WC_MARKED(branchName));
                } catch (HgException.HgCommandCanceledException ex) {
                    // canceled by user, do nothing
                } catch (HgException ex) {
                    HgUtils.notifyException(ex);
                }
                logger.outputInRed(NbBundle.getMessage(CreateBranchAction.class, "MSG_CREATE_DONE")); // NOI18N
                logger.output(""); // NOI18N
            }
        };
        support.start(rp, root, org.openide.util.NbBundle.getMessage(CreateBranchAction.class, "MSG_CreateBranch_Progress", branchName)); //NOI18N
    }
}
