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
package org.netbeans.modules.git.ui.fetch;

import java.text.MessageFormat;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.modules.git.options.AnnotationColorProvider;
import org.netbeans.modules.git.ui.selectors.ItemSelector;
import org.netbeans.modules.git.ui.selectors.ItemSelector.Item;
import org.netbeans.modules.git.utils.GitUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class BranchMapping extends ItemSelector.Item {
    private final String label;
    private final String tooltip;
    private final String remoteBranchName;
    private final GitBranch localBranch;
    private final GitRemoteConfig remote;
    private static final String BRANCH_MAPPING_LABEL = "{0} -> {1}/{0} [{2}]"; //NOI18N
    private static final String BRANCH_DELETE_MAPPING_LABEL = "{0} [{1}]"; //NOI18N
    private static final String BRANCH_MAPPING_LABEL_UPTODATE = "{0} -> {1}/{0}"; //NOI18N
    private static final String COLOR_NEW = GitUtils.getColorString(AnnotationColorProvider.getInstance().ADDED_FILE.getActualColor());
    private static final String COLOR_MODIFIED = GitUtils.getColorString(AnnotationColorProvider.getInstance().MODIFIED_FILE.getActualColor());
    private static final String COLOR_REMOVED = GitUtils.getColorString(AnnotationColorProvider.getInstance().REMOVED_FILE.getActualColor());

    public BranchMapping (String remoteBranchName, String remoteBranchId, GitBranch localBranch, GitRemoteConfig remote, boolean preselected) {
        super(preselected, remoteBranchName == null);
        this.remoteBranchName = remoteBranchName;
        this.localBranch = localBranch;
        this.remote = remote;
        if (isDeletion()) {
            // to remove
            label = MessageFormat.format(BRANCH_DELETE_MAPPING_LABEL, localBranch.getName(), "<font color=\"" + COLOR_REMOVED + "\">R</font>");

            tooltip = NbBundle.getMessage(
                BranchMapping.class, 
                "LBL_FetchBranchesPanel.BranchMapping.description", //NOI18N
                new Object[] { 
                    localBranch.getName(),
                    NbBundle.getMessage(BranchMapping.class, "LBL_FetchBranchesPanel.BranchMapping.Mode.deleted.description") //NOI18N
                }); //NOI18N
        } else if (localBranch == null) {
            // added
            label = MessageFormat.format(BRANCH_MAPPING_LABEL, remoteBranchName, remote.getRemoteName(), "<font color=\"" + COLOR_NEW + "\">A</font>");

            tooltip = NbBundle.getMessage(
                BranchMapping.class, 
                "LBL_FetchBranchesPanel.BranchMapping.description", //NOI18N
                new Object[] { 
                    remote.getRemoteName() + "/" + remoteBranchName, //NOI18N
                    NbBundle.getMessage(BranchMapping.class, "LBL_FetchBranchesPanel.BranchMapping.Mode.added.description") //NOI18N
                }); //NOI18N
        } else if (localBranch.getId().equals(remoteBranchId)) {
            // up to date
            label = MessageFormat.format(BRANCH_MAPPING_LABEL_UPTODATE, remoteBranchName, remote.getRemoteName());

            tooltip = NbBundle.getMessage(
                BranchMapping.class, 
                "LBL_FetchBranchesPanel.BranchMapping.Mode.uptodate.description", localBranch.getName()); //NOI18N
        } else {
            // modified
            label = MessageFormat.format(BRANCH_MAPPING_LABEL, remoteBranchName, remote.getRemoteName(), "<font color=\"" + COLOR_MODIFIED + "\">U</font>"); //NOI18N                 

            tooltip = NbBundle.getMessage(
                BranchMapping.class, 
                "LBL_FetchBranchesPanel.BranchMapping.description", //NOI18N
                new Object[] { 
                    localBranch.getName(),
                    NbBundle.getMessage(BranchMapping.class, "LBL_FetchBranchesPanel.BranchMapping.Mode.updated.description") //NOI18N
                }); 
        }
    }

    public String getRefSpec () {
        if (isDeletion()) {
            return GitUtils.getDeletedRefSpec(localBranch);
        } else {
            return GitUtils.getRefSpec(remoteBranchName, remote.getRemoteName());
        }
    }

    @Override
    public String getText () {
        return label;
    }

    @Override
    public String getTooltipText() {
        return tooltip;
    }

    public String getRemoteBranchName () {
        return remoteBranchName;
    }
    
    public String getRemoteName () {
        return remote.getRemoteName();
    }

    @Override
    public int compareTo(Item t) {
        if(t == null) {
            return 1;
        }
        if(t instanceof BranchMapping) {
            BranchMapping other = (BranchMapping) t;
            if (isDeletion() && other.isDeletion()) {
                return localBranch.getName().compareTo(other.localBranch.getName());
            } else if (isDeletion() && !other.isDeletion()) {
                // deleted branches should be at the bottom
                return 1;
            } else if (!isDeletion() && other.isDeletion()) {
                // deleted branches should be at the bottom
                return -1;
            } else {
                return remoteBranchName.compareTo(other.remoteBranchName);
            }
        }
        return 0;            
    }

    public GitBranch getLocalBranch () {
        return localBranch;
    }   
    
}
