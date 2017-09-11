/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.ui.diff;

import java.io.File;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.ui.actions.GitAction;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.ui.repository.Revision;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Ondra Vrabec
 */
@ActionID(id = "org.netbeans.modules.git.ui.diff.DiffCurrentToTrackedAction", category = "Git")
@ActionRegistration(displayName = "#LBL_DiffCurrentToTrackedAction_Name")
@NbBundle.Messages({
    "LBL_DiffCurrentToTrackedAction_Name=Diff To T&racked",
    "LBL_DiffCurrentToTrackedAction_PopupName=Diff To Tracked"
})
public class DiffCurrentToTrackedAction extends GitAction {

    @Override
    protected boolean enableFull (Node[] activatedNodes) {
        VCSContext context = getCurrentContext(activatedNodes);
        return GitUtils.getRepositoryRoots(context).size() == 1;
    }

    @Override
    protected void performContextAction (Node[] nodes) {
        VCSContext context = getCurrentContext(nodes);
        diffToTracked(context);
    }

    @NbBundle.Messages({
        "LBL_DiffCurrentToTrackedAction.noTracking=Tracking Not Found"
    })
    public void diffToTracked (VCSContext context) {
        if (GitUtils.getRepositoryRoots(context).size() == 1) {
            File repository = GitUtils.getRootFile(context);
            RepositoryInfo info = RepositoryInfo.getInstance(repository);
            GitBranch tracked = GitUtils.getTrackedBranch(info, Bundle.LBL_DiffCurrentToTrackedAction_noTracking());
            if (tracked != null) {
                SystemAction.get(DiffAction.class).diff(context, new Revision.BranchReference(tracked), Revision.HEAD);
            }
        }
    }
}
