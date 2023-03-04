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
 *
 * @author Jiri Rechtacek
 */
class InsertStatementAnalyzer extends SQLStatementAnalyzer {

    private final List<String> columns = new ArrayList<String> ();
    private final List<String> values = new ArrayList<String> ();
    private QualIdent table = null;

    public static InsertStatement analyze (TokenSequence<SQLTokenId> seq, Quoter quoter) {
        seq.moveStart();
        if (!seq.moveNext()) {
            return null;
        }
        InsertStatementAnalyzer sa = new InsertStatementAnalyzer(seq, quoter);
        sa.parse();
        TableIdent ti = new TableIdent(sa.table, null);
        TablesClause tablesClause = sa.context.isAfter(Context.FROM) ? sa.createTablesClause(Collections.singletonList(ti)) : null;
        return new InsertStatement(
                sa.startOffset, seq.offset() + seq.token().length(),
                sa.getTable(),
                Collections.unmodifiableList(sa.columns),
                Collections.unmodifiableList(sa.values),
                sa.offset2Context,
                tablesClause,
                Collections.unmodifiableList(sa.subqueries)
        );
    }

    private InsertStatementAnalyzer (TokenSequence<SQLTokenId> seq, Quoter quoter) {
        super(seq, quoter);
    }

    private void parse () {
        startOffset = seq.offset ();
        do {
            switch (context) {
                case START:
                    if (SQLStatementAnalyzer.isKeyword ("INSERT", seq)) { // NOI18N
                        moveToContext(Context.INSERT);
                    }
                    break;
                case INSERT:
                    if (SQLStatementAnalyzer.isKeyword("INTO", seq)) { // NOI18N
                        moveToContext(Context.INSERT_INTO);
                    }
                    break;
                case INSERT_INTO:
                    switch (seq.token ().id ()) {
                        case IDENTIFIER:
                            table = parseIdentifier();
                            break;
                        case LPAREN:
                            moveToContext(Context.COLUMNS);
                            break;
                        case KEYWORD:
                            if (SQLStatementAnalyzer.isKeyword ("VALUES", seq)) {  //NOI18N
                                moveToContext(Context.VALUES);
                            } else if (SQLStatementAnalyzer.isKeyword ("SET", seq)) {  //NOI18N
                                moveToContext(Context.SET);
                            }
                            break;
                    }
                    break;
                case COLUMNS:
                    switch (seq.token ().id ()) {
                        case IDENTIFIER:
                            List<String> chosenColumns = analyzeChosenColumns ();
                            if ( ! chosenColumns.isEmpty ()) {
                                columns.addAll (chosenColumns);
                            }
                            break;
                        case KEYWORD:
                            if (SQLStatementAnalyzer.isKeyword ("VALUES", seq)) {  //NOI18N
                                moveToContext(Context.VALUES);
                            }
                            break;
                        case RPAREN:
                            moveToContext(Context.VALUES);
                            break;
                    }
                    break;
                case VALUES:
                    switch (seq.token ().id ()) {
                        case IDENTIFIER:
                            List<String> newValues = analyzeChosenColumns ();
                            if ( ! newValues.isEmpty ()) {
                                values.addAll (newValues);
                            }
                            break;
                    }
                    break;
                default:
            }
        } while (nextToken ());
    }

    private List<String> analyzeChosenColumns () {
        List<String> parts = new ArrayList<String> ();
        parts.add (getUnquotedIdentifier ());
        while (seq.moveNext ()) {
            switch (seq.token ().id ()) {
                case WHITESPACE:
                    continue;
                case COMMA:
                    continue;
                case RPAREN:
                    return parts;
                default:
                    parts.add (getUnquotedIdentifier ());
            }
        }
        return parts;
    }

    private QualIdent getTable () {
        return table;
    }
}
