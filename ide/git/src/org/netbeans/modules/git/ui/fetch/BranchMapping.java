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
        if (isDestructive()) {
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
        if (isDestructive()) {
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
            if (isDestructive() && other.isDestructive()) {
                return localBranch.getName().compareTo(other.localBranch.getName());
            } else if (isDestructive() && !other.isDestructive()) {
                // deleted branches should be at the bottom
                return 1;
            } else if (!isDestructive() && other.isDestructive()) {
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
