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
 * @author Andrei Badea
 */
class SelectStatementAnalyzer extends SQLStatementAnalyzer {

    private final List<List<String>> selectValues = new ArrayList<List<String>>();
    private final List<TableIdent> fromTables = new ArrayList<TableIdent>();

    public static SelectStatement analyze(TokenSequence<SQLTokenId> seq, Quoter quoter) {
        seq.moveStart();
        if (!seq.moveNext()) {
            return null;
        }
        SelectStatementAnalyzer sa = new SelectStatementAnalyzer(seq, quoter);
        sa.parse();
        // Return a non-null TablesClause if there was a FROM clause in the statement.
        TablesClause fromClause = sa.context.isAfter(Context.FROM) ? sa.createTablesClause(sa.fromTables) : null;
        return new SelectStatement(sa.startOffset, seq.offset() + seq.token().length(), Collections.unmodifiableList(sa.selectValues), fromClause, Collections.unmodifiableList(sa.subqueries), sa.offset2Context);
    }

    private SelectStatementAnalyzer(TokenSequence<SQLTokenId> seq, Quoter quoter) {
        super(seq, quoter);
    }

    private void parse() {
        startOffset = seq.offset();
        boolean afterFromTableKeyword = false;
        do {
            switch (context) {
                case START:
                    if (SQLStatementAnalyzer.isKeyword ("SELECT", seq)) { // NOI18N
                        moveToContext(Context.SELECT);
                    }
                    break;
                case SELECT:
                    switch (seq.token().id()) {
                        case IDENTIFIER:
                            List<String> selectValue = analyzeSelectValue();
                            if (!selectValue.isEmpty()) {
                                selectValues.add(selectValue);
                            }
                            break;
                        case KEYWORD:
                            if (SQLStatementAnalyzer.isKeyword ("FROM", seq)) { // NOI18N
                                moveToContext(Context.FROM);
                                afterFromTableKeyword = true;
                            }
                            break;
                    }
                    break;
                case FROM:
                    switch (seq.token().id()) {
                        case IDENTIFIER:
                            if (afterFromTableKeyword) {
                                TableIdent fromTable = parseTableIdent();
                                if (fromTable != null) {
                                    fromTables.add(fromTable);
                                }
                                afterFromTableKeyword = false;
                            }
                            break;
                        case COMMA:
                            afterFromTableKeyword = true;
                            break;
                        case KEYWORD:
                            if (SQLStatementAnalyzer.isKeyword ("JOIN", seq)) { // NOI18N
                                afterFromTableKeyword = true;
                            } else {
                                Context newContext = getContextForKeywordAfterFrom();
                                if (newContext != null) {
                                    moveToContext(newContext);
                                }
                            }
                            break;
                    }
                    break;
                case JOIN_CONDITION:
                    switch (seq.token().id()) {
                        case COMMA:
                            moveToContext(Context.FROM);
                            afterFromTableKeyword = true;
                            break;
                        case KEYWORD:
                            if (SQLStatementAnalyzer.isKeyword ("JOIN", seq)) { // NOI18N
                                moveToContext(Context.FROM);
                                afterFromTableKeyword = true;
                            }
                            break;
                    }
                    break;
                case GROUP:
                    if (SQLStatementAnalyzer.isKeyword("BY", seq)) { // NOI18N
                        moveToContext(Context.GROUP_BY);
                    }
                    break;
                case ORDER:
                    if (SQLStatementAnalyzer.isKeyword("BY", seq)) { // NOI18N
                        moveToContext(Context.ORDER_BY);
                    }
                    break;
                default:
                    Context newState = getContextForKeywordAfterFrom();
                    if (newState != null) {
                        moveToContext(newState);
                    }
                    break;
            }
        } while (nextToken());
    }

    private List<String> analyzeSelectValue() {
        List<String> parts = new ArrayList<String>();
        parts.add(getUnquotedIdentifier());
        boolean afterDot = false;
        main: for (;;) {
            if (!nextToken()) {
                return parts;
            }
            switch (seq.token().id()) {
                case DOT:
                    // Tentatively considering this a qualified identifier.
                    afterDot = true;
                    break;
                case IDENTIFIER:
                    if (afterDot) {
                        afterDot = false;
                        parts.add(getUnquotedIdentifier());
                    } else {
                        // Alias like "foo.bar baz".
                        parts.clear();
                        parts.add(getUnquotedIdentifier());
                    }
                    break;
                case LPAREN:
                    // Looks like function call.
                    parts.clear();
                    break;
                case COMMA:
                    break main;
                case KEYWORD:
                    if (SQLStatementAnalyzer.isKeyword ("AS", seq)) { // NOI18N
                        // Alias will follow.
                        afterDot = false;
                        parts.clear();
                    } else if (SQLStatementAnalyzer.isKeyword ("FROM", seq) || isKeywordAfterFrom()) { // NOI18N
                        break main;
                    }
                    break;
                default:
            }
        }
        // Need to process the current token,
        // which doesn't belong to the current SELECT value, once again.
        seq.movePrevious();
        return parts;
    }

    private boolean isKeywordAfterFrom() {
        return getContextForKeywordAfterFrom() != null;
    }

    private Context getContextForKeywordAfterFrom() {
        if (SQLStatementAnalyzer.isKeyword("ON", seq)) { // NOI18N
            return Context.JOIN_CONDITION;
        } else if (SQLStatementAnalyzer.isKeyword("WHERE", seq)) { // NOI18N
            return Context.WHERE;
        } else if (SQLStatementAnalyzer.isKeyword("GROUP", seq)) { // NOI18N
            return Context.GROUP;
        } else if (SQLStatementAnalyzer.isKeyword("HAVING", seq)) { // NOI18N
            return Context.HAVING;
        } else if (SQLStatementAnalyzer.isKeyword("ORDER", seq)) { // NOI18N
            return Context.ORDER;
        }
        return null;
    }
}
