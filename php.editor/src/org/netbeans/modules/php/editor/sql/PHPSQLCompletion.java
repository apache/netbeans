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
