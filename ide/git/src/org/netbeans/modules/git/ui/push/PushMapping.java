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
package org.netbeans.modules.git.ui.push;

import java.text.MessageFormat;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitTag;
import org.netbeans.modules.git.options.AnnotationColorProvider;
import org.netbeans.modules.git.ui.selectors.ItemSelector;
import org.netbeans.modules.git.ui.selectors.ItemSelector.Item;
import org.netbeans.modules.git.utils.GitUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public abstract class PushMapping extends ItemSelector.Item {

    private final String label;
    private final String tooltip;
    private static final String BRANCH_MAPPING_LABEL = "{0} -> {1} [{2}]"; //NOI18N
    private static final String BRANCH_DELETE_MAPPING_LABEL = "{0} [{1}]"; //NOI18N
    private static final String BRANCH_MAPPING_LABEL_UPTODATE = "{0} -> {1}"; //NOI18N
    private final String localName;
    private final String remoteName;
    private static final String COLOR_NEW = GitUtils.getColorString(AnnotationColorProvider.getInstance().ADDED_FILE.getActualColor());
    private static final String COLOR_MODIFIED = GitUtils.getColorString(AnnotationColorProvider.getInstance().MODIFIED_FILE.getActualColor());
    private static final String COLOR_REMOVED = GitUtils.getColorString(AnnotationColorProvider.getInstance().REMOVED_FILE.getActualColor());
    private static final String COLOR_CONFLICT = GitUtils.getColorString(AnnotationColorProvider.getInstance().CONFLICT_FILE.getActualColor());
    
    protected PushMapping (String localName, String localId, String remoteName, boolean conflict, boolean preselected, boolean updateNeeded) {
        super(preselected, localName == null || conflict);
        this.localName = localName;
        this.remoteName = remoteName == null ? localName : remoteName;
        if (localName == null) {
            // to remove
            label = MessageFormat.format(BRANCH_DELETE_MAPPING_LABEL, remoteName, "<font color=\"" + COLOR_REMOVED + "\">R</font>"); //NOI18N
            tooltip = NbBundle.getMessage(
                    PushBranchesStep.class,
                    "LBL_PushBranchMapping.description", //NOI18N
                    new Object[]{
                        remoteName,
                        NbBundle.getMessage(PushBranchesStep.class, "LBL_PushBranchMapping.Mode.deleted.description") //NOI18N
                    }); //NOI18N
        } else if (remoteName == null) {
            // added
            label = MessageFormat.format(BRANCH_MAPPING_LABEL, localName, localName, "<font color=\"" + COLOR_NEW + "\">A</font>"); //NOI18N
            tooltip = NbBundle.getMessage(
                    PushBranchesStep.class,
                    "LBL_PushBranchMapping.description", //NOI18N
                    new Object[]{
                        localName,
                        NbBundle.getMessage(PushBranchesStep.class, "LBL_PushBranchMapping.Mode.added.description") //NOI18N
                    }); //NOI18N
        } else if (conflict) {
            // modified
            label = MessageFormat.format(BRANCH_MAPPING_LABEL, localName, remoteName, "<font color=\"" + COLOR_CONFLICT + "\">C</font>"); //NOI18N
            tooltip = NbBundle.getMessage(
                    PushBranchesStep.class,
                    "LBL_PushBranchMapping.Mode.conflict.description", //NOI18N
                    new Object[]{
                        remoteName
                    });
        } else if (updateNeeded) {
            // modified
            label = MessageFormat.format(BRANCH_MAPPING_LABEL, localName, remoteName, "<font color=\"" + COLOR_MODIFIED + "\">U</font>"); //NOI18N
            tooltip = NbBundle.getMessage(
                    PushBranchesStep.class,
                    "LBL_PushBranchMapping.description", //NOI18N
                    new Object[]{
                        remoteName,
                        NbBundle.getMessage(PushBranchesStep.class, "LBL_PushBranchMapping.Mode.updated.description") //NOI18N
                    });
        } else {
            // up to date
            label = MessageFormat.format(BRANCH_MAPPING_LABEL_UPTODATE, localName, remoteName);
            tooltip = NbBundle.getMessage(PushBranchesStep.class,
                    "LBL_PushBranchMapping.Mode.uptodate.description", //NOI18N
                    remoteName);
        }
    }


    public abstract String getRefSpec ();

    String getInfoMessage () {
        return null;
    }

    @Override
    public String getText () {
        return label;
    }

    @Override
    public String getTooltipText () {
        return tooltip;
    }
    
    public final String getLocalName () {
        return localName;
    }

    @Override
    public int compareTo (Item t) {
        if (t == null) {
            return 1;
        }
        if (t instanceof PushMapping) {
            PushMapping other = (PushMapping) t;
            if (isDestructive() && other.isDestructive()) {
                return remoteName.compareTo(other.remoteName);
            } else if (isDestructive() && !other.isDestructive()) {
                // destructive changes should be at the bottom
                return 1;
            } else if (!isDestructive() && other.isDestructive()) {
                // destructive changes should be at the bottom
                return -1;
            } else {
                return localName.compareTo(other.localName);
            }
        }
        return 0;
    }

    String getRemoteName () {
        return remoteName;
    }

    abstract boolean isCreateBranchMapping ();
    
    public static final class PushBranchMapping extends PushMapping {
        private final GitBranch localBranch;
        private final String remoteBranchName;
        private final String remoteBranchId;
        
        /**
         * Denotes a branch to be deleted in a remote repository
         */
        public PushBranchMapping (String remoteBranchName, String remoteBranchId, boolean preselected) {
            this(remoteBranchName, remoteBranchId, preselected, false);
        }
        
        /**
         * Denotes a branch to be deleted in a remote repository
         */
        public PushBranchMapping (String remoteBranchName, String remoteBranchId, boolean preselected, boolean updateNeeded) {
            super(null, null, remoteBranchName, false, preselected, updateNeeded);
            this.localBranch = null;
            this.remoteBranchName = remoteBranchName;
            this.remoteBranchId = remoteBranchId;
        }
        
        public PushBranchMapping (String remoteBranchName, String remoteBranchId, GitBranch localBranch, boolean conflict, boolean preselected) {
            this(remoteBranchName, remoteBranchId, localBranch, conflict, preselected, false);
        }
        
        public PushBranchMapping (String remoteBranchName, String remoteBranchId, GitBranch localBranch, boolean conflict, boolean preselected, boolean updateNeeded) {
            super(localBranch.getName(), localBranch.getId(), 
                    remoteBranchName, 
                    conflict,
                    preselected,
                    updateNeeded);
            this.localBranch = localBranch;
            this.remoteBranchName = remoteBranchName;
            this.remoteBranchId = remoteBranchId;
        }

        public String getRemoteRepositoryBranchName () {
            return remoteBranchName == null ? localBranch.getName() : remoteBranchName;
        }

        public String getRemoteRepositoryBranchHeadId () {
            return remoteBranchId;
        }

        public String getLocalRepositoryBranchHeadId () {
            return localBranch == null ? null : localBranch.getId();
        }

        @Override
        public String getRefSpec () {
            if (isDestructive() && getLocalName() == null) {
                return GitUtils.getPushDeletedRefSpec(remoteBranchName);
            } else {
                return GitUtils.getPushRefSpec(
                        localBranch.getName(),
                        remoteBranchName == null ? localBranch.getName() : remoteBranchName,
                        isDestructive()
                );
            }
        }
        
        @Override
        @NbBundle.Messages({
            "# {0} - branch name",
            "MSG_PushMapping.toBeDeletedBranch=Branch {0} will be permanently removed from the remote repository.",
            "# {0} - branch name",
            "MSG_PushMapping.toBeForcepushedBranch=Branch {0} will be force pushed to the remote repository."
        })
        String getInfoMessage () {
            if (isDestructive() &&  getLocalName() == null) {
                return Bundle.MSG_PushMapping_toBeDeletedBranch(remoteBranchName);
            } else {
                if(isDestructive()) {
                    return Bundle.MSG_PushMapping_toBeForcepushedBranch(remoteBranchName);
                } else {
                    return super.getInfoMessage();
                }
            }
        }

        @Override
        boolean isCreateBranchMapping () {
            return localBranch != null && remoteBranchName == null;
        }
    }
    
    public static final class PushTagMapping extends PushMapping {
        private final GitTag tag; //local tag
        private final boolean isUpdate;
        private final String remoteTagName;

        /**
         * Tag that we need to delete in the remote repository
         *
         * @param remoteName remote tag name
         */
        public PushTagMapping(String remoteName) {
            super(null, null, remoteName, false, false, remoteName != null);
            this.tag = null;
            this.isUpdate = remoteName != null;
            this.remoteTagName = remoteName;
        }

        /**
         * Adding or updating tag in the remote repository
         *
         * @param tag representation of a local tag
         * @param remoteName remote tag name, can be null. If null than we create tag.
         */
        public PushTagMapping (GitTag tag, String remoteName) {
            super("tags/" + tag.getTagName(), tag.getTaggedObjectId(), remoteName, false, false, remoteName != null); //NOI18N
            this.tag = tag;
            this.isUpdate = remoteName != null;
            this.remoteTagName = remoteName;
        }

        @Override
        public String getRefSpec() {
            if (isDestructive() && !isUpdate) {
                //get command for tag deletion
                return GitUtils.getPushDeletedTagRefSpec(remoteTagName);
            } else {
                return GitUtils.getPushTagRefSpec(tag.getTagName(), isUpdate);
            }
        }

        @Override
        @NbBundle.Messages({
            "# {0} - tag name",
            "MSG_PushMapping.toBeDeletedTag=Tag {0} will be permanently removed from the remote repository."
        })
        String getInfoMessage() {
            if (isDestructive() && !isUpdate) {
                return Bundle.MSG_PushMapping_toBeDeletedTag(remoteTagName);
            } else {
                return super.getInfoMessage();
            }
        }

        @Override
        boolean isCreateBranchMapping () {
            return false;
        }
    }
}
