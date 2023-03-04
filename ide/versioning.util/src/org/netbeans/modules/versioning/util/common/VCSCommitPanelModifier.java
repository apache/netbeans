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
package org.netbeans.modules.versioning.util.common;

import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class VCSCommitPanelModifier {
    
    public static enum BundleMessage {
        FILE_TABLE_INCLUDE_ACTION_NAME(NbBundle.getMessage(VCSCommitPanelModifier.class, "CTL_CommitTable_IncludeAction")), //NOI18N
        FILE_TABLE_EXCLUDE_ACTION_NAME(NbBundle.getMessage(VCSCommitPanelModifier.class, "CTL_CommitTable_ExcludeAction")), //NOI18N
        FILE_TABLE_ACCESSIBLE_NAME(NbBundle.getMessage(VCSCommitPanelModifier.class, "ACSN_CommitTable")), //NOI18N
        FILE_TABLE_ACCESSIBLE_DESCRIPTION(NbBundle.getMessage(VCSCommitPanelModifier.class, "ACSD_CommitTable")), //NOI18N
        FILE_TABLE_HEADER_COMMIT(NbBundle.getMessage(VCSCommitPanelModifier.class, "CTL_CommitTable_Column_Commit")), //NOI18N
        FILE_TABLE_HEADER_COMMIT_DESC(NbBundle.getMessage(VCSCommitPanelModifier.class, "CTL_CommitTable_Column_Description")), //NOI18N
        FILE_TABLE_HEADER_ACTION(NbBundle.getMessage(VCSCommitPanelModifier.class, "CTL_CommitTable_Column_Action")), //NOI18N
        FILE_TABLE_HEADER_ACTION_DESC(NbBundle.getMessage(VCSCommitPanelModifier.class, "CTL_CommitTable_Column_Action")), //NOI18N
        FILE_PANEL_TITLE(NbBundle.getMessage(VCSCommitPanelModifier.class, "LBL_CommitDialog_FilesToCommit")), //NOI18N
        COMMIT_BUTTON_LABEL(NbBundle.getMessage(VCSCommitPanelModifier.class, "CTL_Commit_Action_Commit")), //NOI18N
        COMMIT_BUTTON_ACCESSIBLE_NAME(NbBundle.getMessage(VCSCommitPanelModifier.class, "ACSN_Commit_Action_Commit")), //NOI18N
        COMMIT_BUTTON_ACCESSIBLE_DESCRIPTION(NbBundle.getMessage(VCSCommitPanelModifier.class, "ACSD_Commit_Action_Commit")), //NOI18N
        PANEL_ACCESSIBLE_NAME(NbBundle.getMessage(VCSCommitPanelModifier.class, "ACSN_CommitDialog")), //NOI18N
        PANEL_ACCESSIBLE_DESCRIPTION(NbBundle.getMessage(VCSCommitPanelModifier.class, "ACSD_CommitDialog")), //NOI18N
        TABS_MAIN_NAME(NbBundle.getMessage(VCSCommitPanelModifier.class, "CTL_CommitDialog_Tab_Commit")), //NOI18N
        MESSAGE_FINISHING_FROM_DIFF(NbBundle.getMessage(VCSCommitPanelModifier.class, "MSG_CommitDialog_CommitFromDiff")), //NOI18N
        MESSAGE_FINISHING_FROM_DIFF_TITLE(NbBundle.getMessage(VCSCommitPanelModifier.class, "LBL_CommitDialog_CommitFromDiff")), //NOI18N
        MESSAGE_NO_FILES(NbBundle.getMessage(VCSCommitPanelModifier.class, "MSG_ERROR_NO_FILES")); //NOI18N
        
        private String message;

        private BundleMessage (String message) {
            this.message = message;
        }
        
        @Override
        public String toString () {
            return message;
        }
        
    }
    
    public String getMessage (BundleMessage message) {
        return message.toString();
    }

    public VCSCommitOptions getExcludedOption () {
        return VCSCommitOptions.EXCLUDE;
    }
}
