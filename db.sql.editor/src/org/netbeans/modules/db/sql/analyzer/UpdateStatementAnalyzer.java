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
 * Parse SQL Update statement. It should parse syntax regardless particular
 * database specialities. Format for selected databases is as follows:
 *
 * JavaDB
 *   update APP.A, B AS B1, B B2 SET A = 1, B2.B1 = 2;
 *   update A SET A = (select A from A where A = 2);
 * Oracle
 *   update A SET A = DEFAULT;
 *   update A SET (A, B) = (SELECT * FROM A);
 *   update A SET A = (select A from A);
 *   update (select A from A) SET A = (select A from A)
 * MySQL
 *   update A, B AS B, (select * from A) AS R SET A.A = 1, B.A = 2, A.A = DEFAULT WHERE A.A = B.A;
 *   update A inner join B on A.A = B.A SET A.A = 1;
 *
 * @author Jiri Skrivanek
 */
class UpdateStatementAnalyzer extends SQLStatementAnalyzer {

    private final List<TableIdent> tables = new ArrayList<TableIdent>();

    public static UpdateStatement analyze(TokenSequence<SQLTokenId> seq, Quoter quoter) {
        seq.moveStart();
        if (!seq.moveNext()) {
            return null;
        }
        UpdateStatementAnalyzer sa = new UpdateStatementAnalyzer(seq, quoter);
        sa.parse();
        TablesClause tablesClause = sa.context.isAfter(Context.FROM) ? sa.createTablesClause(sa.tables) : null;
        return new UpdateStatement(sa.startOffset, seq.offset() + seq.token().length(), tablesClause, Collections.unmodifiableList(sa.subqueries), sa.offset2Context);
    }

    private UpdateStatementAnalyzer(TokenSequence<SQLTokenId> seq, Quoter quoter) {
        super(seq, quoter);
    }

    private void parse() {
        startOffset = seq.offset();
        do {
            switch (context) {
                case START:
                    if (SQLStatementAnalyzer.isKeyword("UPDATE", seq)) { // NOI18N
                        moveToContext(Context.UPDATE);
                    }
                    break;
                case UPDATE:
                    switch (seq.token().id()) {
                        case IDENTIFIER:
                            TableIdent tableIdent = parseTableIdent();
                            if (tableIdent != null) {
                                tables.add(tableIdent);
                            }
                            break;
                        case KEYWORD:
                            if (SQLStatementAnalyzer.isKeyword("SET", seq)) {  //NOI18N
                                moveToContext(Context.SET);
                            } else if (SQLStatementAnalyzer.isKeyword("ON", seq)) {  //NOI18N
                                moveToContext(Context.JOIN_CONDITION);
                            }
                            break;
                    }
                    break;
                case JOIN_CONDITION:
                    switch (seq.token().id()) {
                        case COMMA:
                            moveToContext(Context.UPDATE);
                            break;
                        case KEYWORD:
                            if (SQLStatementAnalyzer.isKeyword("JOIN", seq)) { // NOI18N
                                moveToContext(Context.UPDATE);
                            } else if (SQLStatementAnalyzer.isKeyword("SET", seq)) {  //NOI18N
                                moveToContext(Context.SET);
                            }
                            break;
                    }
                    break;
                case SET:
                    if (SQLStatementAnalyzer.isKeyword("WHERE", seq)) { // NOI18N
                        moveToContext(Context.WHERE);
                    }
                    // otherwise skip all tokens possible in SET clause (IDENTIFIER, OPERATOR, COMMA, LPAREN, RPAREN)
                    break;
                default:
                // skip anything else
            }
        } while (nextToken());
    }
}
