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

package org.netbeans.modules.db.sql.editor.completion;

import javax.swing.text.JTextComponent;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.modules.db.api.sql.execute.SQLExecution;
import org.netbeans.modules.db.sql.editor.SQLPreferences;
import org.netbeans.modules.db.sql.editor.ui.actions.SQLExecutionBaseAction;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class SQLCompletionProvider implements CompletionProvider {

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType == CompletionProvider.COMPLETION_QUERY_TYPE || queryType == CompletionProvider.COMPLETION_ALL_QUERY_TYPE) {
            DatabaseConnection dbconn = findDBConn(component);
            if (dbconn == null) {
                // XXX perhaps should have an item in the completion instead?
                Completion.get().hideAll();
                SQLExecutionBaseAction.notifyNoDatabaseConnection();
                return null;
            }
            return new AsyncCompletionTask(new SQLCompletionQuery(dbconn), component);
        }
        return null;
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        if (typedText == null || typedText.trim().isEmpty() ) {
            return 0;
        }
        char lastChar = typedText.charAt(typedText.length() - 1);
        if (SQLPreferences.autoPopupTriggers().indexOf(lastChar) < 0) {
            return 0;
        }
        DatabaseConnection dbconn = findDBConn(component);
        if (dbconn == null) {
            String message = NbBundle.getMessage(SQLCompletionProvider.class, "MSG_NoDatabaseConnection"); // NOI18N
            StatusDisplayer.getDefault().setStatusText(message);
            return 0;
        }
        if (dbconn.getJDBCConnection() == null) {
            String message = NbBundle.getMessage(SQLCompletionProvider.class, "MSG_NotConnected"); // NOI18N
            StatusDisplayer.getDefault().setStatusText(message);
            return 0;
        }
        return SQLPreferences.autoPopupCompletionWindow() ? COMPLETION_QUERY_TYPE : 0;
    }

    private static DatabaseConnection findDBConn(JTextComponent component) {
        Lookup context = findContext(component);
        if (context == null) {
            return null;
        }
        SQLExecution sqlExecution = context.lookup(SQLExecution.class);
        if (sqlExecution == null) {
            return null;
        }
        return sqlExecution.getDatabaseConnection();
    }

    private static Lookup findContext(JTextComponent component) {
        for (java.awt.Component comp = component; comp != null; comp = comp.getParent()) {
            if (comp instanceof Lookup.Provider) {
                Lookup lookup = ((Lookup.Provider)comp).getLookup ();
                if (lookup != null) {
                    return lookup;
                }
            }
        }
        return null;
    }

}
