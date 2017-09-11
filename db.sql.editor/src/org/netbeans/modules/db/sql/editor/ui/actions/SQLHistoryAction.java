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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.sql.editor.ui.actions;

import org.netbeans.modules.db.api.sql.execute.SQLExecution;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author jbaker
 */
public class SQLHistoryAction extends SQLExecutionBaseAction {
    private static final String ICON_PATH = "org/netbeans/modules/db/sql/editor/resources/sql_history_16.png"; // NOI18N
    private static final String SQL_HISTORY_FOLDER = "Databases/SQLHISTORY"; // NOI18N

    protected String getIconBase() {
        return ICON_PATH;
    }

    protected String getDisplayName(SQLExecution sqlExecution) { 
        return NbBundle.getMessage(SQLHistoryAction.class, "LBL_SQLHistoryAction");
    }

    protected void actionPerformed(SQLExecution sqlExecution) {
        FileObject historyRoot = FileUtil.getConfigFile(SQL_HISTORY_FOLDER);
        if (historyRoot == null || historyRoot.getChildren().length == 0) {    
            notifyNoSQLExecuted();
        } else {
            historyRoot.refresh(true);
            sqlExecution.showHistory();
        }
    }
    
    private static void notifyNoSQLExecuted() {
        String message = NbBundle.getMessage(SQLExecutionBaseAction.class, "LBL_NoSQLExecuted");
        NotifyDescriptor desc = new NotifyDescriptor.Message(message, NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(desc);
    }
}
