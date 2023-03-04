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
