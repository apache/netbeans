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
