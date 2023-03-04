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

package org.netbeans.modules.php.editor.sql;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.sql.editor.api.completion.SQLCompletion;
import org.netbeans.modules.db.sql.editor.api.completion.SQLCompletionContext;
import org.netbeans.modules.db.sql.editor.api.completion.SQLCompletionResultSet;
import org.netbeans.modules.db.sql.editor.api.completion.SubstitutionHandler;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.text.NbDocument;

/**
 *
 * @author Andrei Badea, David Van Couvering
 */
public class PHPSQLCompletion implements CompletionProvider {
    private static final Boolean NO_COMPLETION = Boolean.getBoolean("netbeans.php.nosqlcompletion"); // NOI18N

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        return new AsyncCompletionTask(new Query(), component);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    private static final class Query extends AsyncCompletionQuery {

        @Override
        protected void query(CompletionResultSet resultSet, Document document, int caretOffset) {
            doQuery(resultSet, document, caretOffset);
            resultSet.finish();
        }

        private void doQuery(CompletionResultSet resultSet, Document document, int caretOffset) {
            if (NO_COMPLETION) {
                return;
            }
            PHPSQLStatement stmt = PHPSQLStatement.computeSQLStatement(document, caretOffset);
            if (stmt == null) {
                return;
            }

            SQLCompletionContext context = SQLCompletionContext.empty();
            context = context.setStatement(stmt.getStatement());
            if (!SQLCompletion.canComplete(context)) {
                return;
            }
            context = context.setOffset(stmt.sourceToGeneratedPos(caretOffset));

            DatabaseConnection dbconn = DatabaseConnectionSupport.getDatabaseConnection(document, true);
            if (dbconn == null) {
                resultSet.addItem(new SelectConnectionItem(document));
            } else {
                context = context.setDatabaseConnection(dbconn);
                SQLCompletion completion = SQLCompletion.create(context);
                SQLCompletionResultSet sqlResultSet = SQLCompletionResultSet.create();
                completion.query(sqlResultSet, new SQLSubstitutionHandler(stmt));
                resultSet.addAllItems(sqlResultSet.getItems());
                resultSet.setAnchorOffset(stmt.generatedToSourcePos(sqlResultSet.getAnchorOffset()));
            }
        }


        static class SQLSubstitutionHandler implements SubstitutionHandler {
            final PHPSQLStatement statement;

            public SQLSubstitutionHandler(PHPSQLStatement statement) {
                this.statement = statement;
            }

            @Override
            public void substituteText(JTextComponent component, int offset, final String text) {
                final int caretOffset = component.getSelectionEnd();
                final int sourceOffset = statement.generatedToSourcePos(offset);
                final StyledDocument document = (StyledDocument) component.getDocument();
                try {
                    NbDocument.runAtomicAsUser(document, new Runnable() {

                        @Override
                        public void run() {
                            try {
                                int documentOffset = sourceOffset;
                                document.remove(documentOffset, caretOffset - documentOffset);
                                document.insertString(documentOffset, text, null);
                            } catch (BadLocationException ex) {
                            }
                        }
                    });
                } catch (BadLocationException ex) {
                }
            }
        }
    }

}
