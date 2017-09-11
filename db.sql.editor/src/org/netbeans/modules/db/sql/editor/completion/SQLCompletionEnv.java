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

package org.netbeans.modules.db.sql.editor.completion;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.db.api.sql.execute.SQLScript;
import org.netbeans.modules.db.api.sql.execute.SQLScriptStatement;
import org.netbeans.modules.db.sql.editor.api.completion.SubstitutionHandler;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;

/**
 *
 * @author Andrei Badea
 */
public class SQLCompletionEnv {

    private final String statement;
    private final int statementOffset;
    private final int caretOffset;
    private final SubstitutionHandler substitutionHandler;
    private final TokenSequence<SQLTokenId> seq;

    public static SQLCompletionEnv forDocument(Document doc, int caretOffset) {
        String documentText = getDocumentText(doc);
        if (documentText != null) {
            return forScript(documentText, caretOffset);
        }
        return null;
    }

    public static SQLCompletionEnv forStatement(String statement, int caretOffset, SubstitutionHandler substitutionHandler) {
        return new SQLCompletionEnv(statement, 0, caretOffset, substitutionHandler);
    }

    /** Returns SQLCompletionEnv for given SQL script. Not private because of unit tests.
     * @param script SQL script
     * @param caretOffset caret offset within script
     */
    static SQLCompletionEnv forScript(String script, int caretOffset) {
        return forScript(script, caretOffset, 0);
    }

    /** Returns SQLCompletionEnv for given SQL script. Parameter scriptOffset
     * used for embedded scripts, e.g. in create procedure statement. Called
     * directly from SQLCompletionQuery.
     * @param script SQL script
     * @param caretOffset caret offset within script
     * @param scriptOffset script offset in document
     */
    static SQLCompletionEnv forScript(String script, int caretOffset, int scriptOffset) {
        SQLScriptStatement statement = SQLScript.create(script).getStatementAtOffset(caretOffset);
        if (statement != null) {
            return new SQLCompletionEnv(statement.getText(), statement.getStartOffset(), caretOffset - statement.getStartOffset(),
                    new ScriptSubstitutionHandler(statement.getStartOffset() + scriptOffset));
        } else {
            // empty script
            return new SQLCompletionEnv("", 0, caretOffset, new ScriptSubstitutionHandler(scriptOffset));  //NOI18N
        }
    }

    private SQLCompletionEnv(String statement, int statementOffset, int caretOffset, SubstitutionHandler substitutionHandler) {
        this.statement = statement;
        this.statementOffset = statementOffset;
        this.caretOffset = caretOffset;
        this.substitutionHandler = substitutionHandler;
        TokenHierarchy<String> hi = TokenHierarchy.create(statement, SQLTokenId.language());
        seq = hi.tokenSequence(SQLTokenId.language());
    }

    /**
     * The text of the SQL statement.
     */
    public String getStatement() {
        return statement;
    }

    /**
     * The offset of the SQL statement in the document or SQL script.
     */
    public int getStatementOffset() {
        return statementOffset;
    }

    public SubstitutionHandler getSubstitutionHandler() {
        return substitutionHandler;
    }

    /**
     * The caret offset, relative to {@link #getStatementOffset}.
     */
    public int getCaretOffset() {
        return caretOffset;
    }

    public TokenSequence<SQLTokenId> getTokenSequence() {
        return seq;
    }

    private static String getDocumentText(final Document doc) {
        final String[] result = { null };
        doc.render(new Runnable() {
            public void run() {
                try {
                    result[0] = doc.getText(0, doc.getLength());
                } catch (BadLocationException e) {
                    // Should not happen.
                }
            }
        });
        return result[0];
    }

    private static final class ScriptSubstitutionHandler implements SubstitutionHandler {

        private final int statementOffset;

        public ScriptSubstitutionHandler(int statementOffset) {
            this.statementOffset = statementOffset;
        }

        public void substituteText(JTextComponent component, final int offset, final String text) {
            final int caretOffset = component.getSelectionEnd();
            final BaseDocument baseDoc = (BaseDocument) component.getDocument();
            baseDoc.runAtomicAsUser(new Runnable() {
                public void run() {
                    int documentOffset = statementOffset + offset;
                    try {
                        baseDoc.remove(documentOffset, caretOffset - documentOffset);
                        baseDoc.insertString(documentOffset, text, null);
                    } catch (BadLocationException ex) {
                        // No can do, document may have changed.
                    }
                }
            });
        }
    }
}
