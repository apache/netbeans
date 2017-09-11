/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
