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

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.db.api.sql.execute.SQLExecution;
import org.netbeans.modules.db.sql.editor.ui.actions.SQLExecutionBaseAction;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;
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

    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        if (!".".equals(typedText)) { // NOI18N
            return 0;
        }
        if (!isDotAtOffset(component, component.getSelectionStart() - 1)) {
            return 0;
        }
        DatabaseConnection dbconn = findDBConn(component);
        if (dbconn == null) {
            String message = NbBundle.getMessage(SQLCompletionProvider.class, "MSG_NoDatabaseConnection");
            StatusDisplayer.getDefault().setStatusText(message);
            return 0;
        }
        if (dbconn.getJDBCConnection() == null) {
            String message = NbBundle.getMessage(SQLCompletionProvider.class, "MSG_NotConnected");
            StatusDisplayer.getDefault().setStatusText(message);
            return 0;
        }
        return COMPLETION_QUERY_TYPE;
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

    private static boolean isDotAtOffset(JTextComponent component, final int offset) {
        final Document doc = component.getDocument();
        final boolean[] result = { false };
        doc.render(new Runnable() {
            public void run() {
                TokenSequence<SQLTokenId> seq = getSQLTokenSequence(doc);
                if (seq == null) {
                    return;
                }
                seq.move(offset);
                if (!seq.moveNext() && !seq.movePrevious()) {
                    return;
                }
                if (seq.offset() != offset) {
                    return;
                }
                result[0] = (seq.token().id() == SQLTokenId.DOT);
            }
        });
        return result[0];
    }

    private static TokenSequence<SQLTokenId> getSQLTokenSequence(Document doc) {
        // Hack until the SQL editor is entirely ported to the Lexer API.
        if (doc.getProperty(Language.class) == null) {
            doc.putProperty(Language.class, SQLTokenId.language());
        }
        TokenHierarchy<?> hierarchy = TokenHierarchy.get(doc);
        return hierarchy.tokenSequence(SQLTokenId.language());
    }
}
