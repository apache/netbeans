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

import java.util.Collections;
import java.util.List;
import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.db.sql.analyzer.SQLStatement.Context;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;

/**
 * Parse SQL Create procedure/function statement. Expected format is:
 *
 * DELIMITER \\
 * CREATE PROCEDURE p2()
 * BEGIN
 *   SELECT * FROM a;
 *   SELECT * FROM b;
 * END\\
 *
 * @author Jiri Skrivanek
 */
class CreateStatementAnalyzer extends SQLStatementAnalyzer {

    private int bodyStartOffset;
    private int bodyEndOffset;

    public static CreateStatement analyze(TokenSequence<SQLTokenId> seq, Quoter quoter) {
        seq.moveStart();
        if (!seq.moveNext()) {
            return null;
        }
        CreateStatementAnalyzer sa = new CreateStatementAnalyzer(seq, quoter);
        sa.parse();
        return new CreateStatement(sa.startOffset, seq.offset() + seq.token().length(), sa.offset2Context, sa.bodyStartOffset, sa.bodyEndOffset, null, Collections.unmodifiableList(sa.subqueries));
    }

    private CreateStatementAnalyzer(TokenSequence<SQLTokenId> seq, Quoter quoter) {
        super(seq, quoter);
    }

    private void parse() {
        startOffset = seq.offset();
        do {
            switch (context) {
                case START:
                    if (SQLStatementAnalyzer.isKeyword("CREATE", seq)) { // NOI18N
                        moveToContext(Context.CREATE);
                    }
                    break;
                case CREATE:
                    if (SQLStatementAnalyzer.isKeyword("PROCEDURE", seq)) { // NOI18N
                        moveToContext(Context.CREATE_PROCEDURE);
                    } else if (SQLStatementAnalyzer.isKeyword("FUNCTION", seq)) { // NOI18N
                        moveToContext(Context.CREATE_FUNCTION);
                    } else if (SQLStatementAnalyzer.isKeyword("TABLE", seq)) {
                        moveToContext(Context.CREATE_TABLE);
                    } else if (SQLStatementAnalyzer.isKeyword("TEMPORARY", seq)) {
                        moveToContext(Context.CREATE_TEMPORARY_TABLE);
                    } else if (SQLStatementAnalyzer.isKeyword("DATABASE", seq)) {
                        moveToContext(Context.CREATE_DATABASE);
                    } else if (SQLStatementAnalyzer.isKeyword("SCHEMA", seq)) {
                        moveToContext(Context.CREATE_SCHEMA);
                    } else if (SQLStatementAnalyzer.isKeyword("VIEW", seq)) {
                        moveToContext(Context.CREATE_VIEW);
                    }
                    break;
                case CREATE_PROCEDURE:
                case CREATE_FUNCTION:
                    if (SQLStatementAnalyzer.isKeyword("BEGIN", seq)) { // NOI18N
                        moveToContext(Context.BEGIN);
                        bodyStartOffset = seq.offset() + seq.token().length();
                    }
                    break;
                case BEGIN:
                    if (SQLStatementAnalyzer.isKeyword("END", seq)) { // NOI18N
                        moveToContext(Context.END);
                        bodyEndOffset = seq.offset();
                    }
                    break;
                case CREATE_TEMPORARY_TABLE:
                    if (SQLStatementAnalyzer.isKeyword("TABLE", seq)) {
                        moveToContext(Context.CREATE_TABLE);
                    }
                    break;
                case CREATE_VIEW:
                    if(SQLStatementAnalyzer.isKeyword("AS", seq)) {
                        moveToContext(Context.CREATE_VIEW_AS);
                    }
                    break;
                case CREATE_VIEW_AS:
                    if(SQLStatementAnalyzer.isKeyword("SELECT", seq)) {
                        moveToContext(Context.SELECT);
                    }
                    break;
                default:
                // skip anything else
            }
        } while (nextToken());
        // unfinished body
        if (context == Context.BEGIN) {
            bodyEndOffset = seq.offset() + seq.token().length();
        }
    }
}
