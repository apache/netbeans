/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.git.ui.commit;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.ui.actions.MultipleRepositoryAction;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.commit.ExcludeFromCommitAction", category = "Git")
@ActionRegistration(displayName = "#LBL_ExcludeFromCommitAction_Name")
@NbBundle.Messages({
    "LBL_ExcludeFromCommitAction_Name=Ex&clude From Commit"
})
public class ExcludeFromCommitAction extends MultipleRepositoryAction {

    @Override
    protected Task performAction (File repository, File[] roots, VCSContext context) {
        return exclude(repository, roots);
    }

    @Override
    protected boolean enableFull (Node[] activatedNodes) {
        boolean enabled = super.enableFull(activatedNodes);
        if (enabled) {
            enabled = false;
            GitModuleConfig config = GitModuleConfig.getDefault();
            for (File root : getCurrentContext(activatedNodes).getRootFiles()) {
                if (!config.isExcludedFromCommit(root.getAbsolutePath())) {
                    enabled = true;
                    break;
                }
            }
        }
        return enabled;
    }

    public Task exclude (File repository, File[] roots) {
        List<String> toExclude = filterRoots(roots);
        GitModuleConfig config = GitModuleConfig.getDefault();
        config.addExclusionPaths(toExclude);
        SystemAction.get(IncludeInCommitAction.class).setEnabled(false);
        SystemAction.get(ExcludeFromCommitAction.class).setEnabled(false);
        return null;
    }
    
    private static List<String> filterRoots (File[] roots) {
        List<String> toExclude = new LinkedList<String>();
        GitModuleConfig config = GitModuleConfig.getDefault();
        for (File root : roots) {
            String path = root.getAbsolutePath();
            if (!config.isExcludedFromCommit(path)) {
                toExclude.add(path);
            }
        }
        return toExclude;
    }
    
}
