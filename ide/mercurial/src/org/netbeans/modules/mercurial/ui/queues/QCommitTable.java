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

package org.netbeans.modules.mercurial.ui.queues;

import java.util.List;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier;
import org.netbeans.modules.versioning.util.common.VCSCommitOptions;
import org.netbeans.modules.versioning.util.common.VCSCommitTable;
import org.netbeans.modules.versioning.util.common.VCSCommitTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class QCommitTable extends VCSCommitTable<QFileNode> {

    private String errroMessage;
    
    public QCommitTable (VCSCommitPanelModifier modifier) {
        super(new VCSCommitTableModel<QFileNode>(modifier), true);
    }

    @Override
    public boolean containsCommitable() {
        List<QFileNode> list = getCommitFiles();
        boolean ret = true;        
        errroMessage = null;
        for(QFileNode fileNode : list) {                        
            VCSCommitOptions co = fileNode.getCommitOptions();
            if(co == QFileNode.EXCLUDE) {
                continue;
            }
            FileInformation info = fileNode.getInformation();
            if ((info.getStatus() & FileInformation.STATUS_VERSIONED_CONFLICT) != 0) {
                errroMessage = NbBundle.getMessage(QCommitTable.class, "MSG_CommitForm_ErrorConflicts"); // NOI18N
                return false;
            }            
            errroMessage = null;            
        }
        return ret;
    }

    @Override
    public String getErrorMessage() {
        return errroMessage;
    }    
    
}
