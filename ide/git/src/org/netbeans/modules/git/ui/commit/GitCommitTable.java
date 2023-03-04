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

package org.netbeans.modules.git.ui.commit;

import java.util.List;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.GitFileNode.GitLocalFileNode;
import org.netbeans.modules.versioning.util.common.VCSCommitOptions;
import org.netbeans.modules.versioning.util.common.VCSCommitTable;
import org.netbeans.modules.versioning.util.common.VCSCommitTableModel;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Stupka
 */
public class GitCommitTable extends VCSCommitTable<GitLocalFileNode> {

    private String errroMessage;
    private boolean amend;
    private boolean emptyAllowed;
    
    public GitCommitTable() {
        this(true, false);
    }

    public GitCommitTable (boolean editable, boolean allowEmpty) {
        super(new VCSCommitTableModel<GitLocalFileNode>(), editable);
        this.emptyAllowed = allowEmpty;
    }

    @Override
    @NbBundle.Messages("MSG_Warning_RenamedFiles=Some files were renamed only by changing the case in their names. "
            + "You should use a commandline client to commit this state, the NetBeans IDE cannot handle it properly.")
    public boolean containsCommitable() {
        List<GitLocalFileNode> list = getCommitFiles();
        boolean ret = false;        
        errroMessage = null;
        boolean isEmpty = true;
        if (amend) {
            return true;
        }
        for (GitLocalFileNode fileNode : list) {                        
            
            VCSCommitOptions co = fileNode.getCommitOptions();
            if(co == VCSCommitOptions.EXCLUDE) {
                continue;
            }
            isEmpty = false;
            FileInformation info = fileNode.getInformation();
            if(info.containsStatus(FileInformation.Status.IN_CONFLICT)) {
                errroMessage = NbBundle.getMessage(CommitAction.class, "MSG_CommitForm_ErrorConflicts"); // NOI18N
                return false;
            } else if (info.isRenamed() && info.getOldFile() != null
                    && (Utilities.isMac() || Utilities.isWindows())
                    && info.getOldFile().getAbsolutePath().equalsIgnoreCase(fileNode.getFile().getAbsolutePath())) {
                errroMessage = Bundle.MSG_Warning_RenamedFiles();
            }
            ret = true;
        }
        if (isEmpty) {
            if (emptyAllowed) {
                ret = true;
            } else {
                errroMessage = NbBundle.getMessage(CommitAction.class, "MSG_ERROR_NO_FILES"); //NOI18N
            }
        }
        return ret;
    }

    @Override
    public String getErrorMessage() {
        return errroMessage;
    }    

    public boolean isAmend() {
        return amend;
    }

    public void setAmend(boolean amend) {
        this.amend = amend;
    }
    
}
