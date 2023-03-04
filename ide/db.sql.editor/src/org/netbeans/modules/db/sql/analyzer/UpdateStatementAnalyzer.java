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
