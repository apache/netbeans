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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.sql.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.db.sql.analyzer.SQLStatement.Context;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;

/**
 * Parse SQL Delete statement. It should parse syntax regardless particular
 * database specialities. Format for selected databases is as follows:
 *
 * JavaDB
 *   DELETE FROM A AS A1 WHERE A1.A = 1;
 *   DELETE FROM A AS A1 WHERE A1.A = (SELECT B FROM B WHERE B = A1.A);
 * Oracle
 *   DELETE FROM A A1 WHERE A1.A = 1;
 *   DELETE FROM A A1 WHERE A1.A = (SELECT B FROM A WHERE B = A1.A);
 *   DELETE FROM (SELECT A FROM A) A1 WHERE A1.A = (SELECT B FROM A WHERE B = A1.A);
 * MySQL
 *   DELETE FROM A WHERE A.A = 1;
 *   DELETE A, B1 FROM A, B AS B1, B B2 WHERE A.A = B1.A;
 *   DELETE A FROM A inner join B on A.A = B.A WHERE A.A = 1;
 *
 * @author Jiri Skrivanek
 */
class DeleteStatementAnalyzer extends SQLStatementAnalyzer {

    private final List<TableIdent> tables = new ArrayList<TableIdent>();

    public static DeleteStatement analyze(TokenSequence<SQLTokenId> seq, Quoter quoter) {
        seq.moveStart();
        if (!seq.moveNext()) {
            return null;
        }
        DeleteStatementAnalyzer sa = new DeleteStatementAnalyzer(seq, quoter);
        sa.parse();
        TablesClause tablesClause = sa.context.isAfter(Context.FROM) ? sa.createTablesClause(sa.tables) : null;
        return new DeleteStatement(sa.startOffset, seq.offset() + seq.token().length(), tablesClause, Collections.unmodifiableList(sa.subqueries), sa.offset2Context);
    }

    private DeleteStatementAnalyzer(TokenSequence<SQLTokenId> seq, Quoter quoter) {
        super(seq, quoter);
    }

    private void parse() {
        startOffset = seq.offset();
        do {
            switch (context) {
                case START:
                    if (SQLStatementAnalyzer.isKeyword("DELETE", seq)) { // NOI18N
                        moveToContext(Context.DELETE);
                    }
                    break;
                case DELETE:
                    switch (seq.token().id()) {
                        case IDENTIFIER:
                            TableIdent tableIdent = parseTableIdent();
                            if (tableIdent != null) {
                                tables.add(tableIdent);
                            }
                            break;
                        case KEYWORD:
                            if (SQLStatementAnalyzer.isKeyword("FROM", seq)) {  //NOI18N
                                moveToContext(Context.FROM);
                            } else if (SQLStatementAnalyzer.isKeyword("ON", seq)) {  //NOI18N
                                moveToContext(Context.JOIN_CONDITION);
                            }
                            break;
                    }
                    break;
                case FROM:
                    switch (seq.token().id()) {
                        case IDENTIFIER:
                            TableIdent tableIdent = parseTableIdent();
                            if (tableIdent != null) {
                                tables.add(tableIdent);
                            }
                            break;
                        case KEYWORD:
                            if (SQLStatementAnalyzer.isKeyword("ON", seq)) {  //NOI18N
                                moveToContext(Context.JOIN_CONDITION);
                            } else if (SQLStatementAnalyzer.isKeyword("WHERE", seq)) { // NOI18N
                                moveToContext(Context.WHERE);
                            }
                            break;
                    }
                    break;
                case JOIN_CONDITION:
                    switch (seq.token().id()) {
                        case COMMA:
                            moveToContext(Context.FROM);
                            break;
                        case KEYWORD:
                            if (SQLStatementAnalyzer.isKeyword("JOIN", seq)) { // NOI18N
                                moveToContext(Context.FROM);
                            } else if (SQLStatementAnalyzer.isKeyword("WHERE", seq)) {  //NOI18N
                                moveToContext(Context.WHERE);
                            }
                            break;
                    }
                    break;
                default:
                // skip anything else
            }
        } while (nextToken());
    }
}
