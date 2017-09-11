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
