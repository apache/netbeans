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

import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.db.sql.analyzer.SQLStatement.Context;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;

/**
 *
 * @author Jiri Skrivanek
 */
class DropStatementAnalyzer extends SQLStatementAnalyzer {

    private QualIdent table = null;

    private DropStatementAnalyzer(TokenSequence<SQLTokenId> seq, Quoter quoter) {
        super(seq, quoter);
    }

    public static DropStatement analyze(TokenSequence<SQLTokenId> seq, Quoter quoter) {
        seq.moveStart();
        if (!seq.moveNext()) {
            return null;
        }
        DropStatementAnalyzer sa = new DropStatementAnalyzer(seq, quoter);
        sa.parse();
        return new DropStatement(sa.startOffset, seq.offset() + seq.token().length(), sa.table, sa.offset2Context);
    }

    private void parse() {
        startOffset = seq.offset();
        do {
            switch (context) {
                case START:
                    if (SQLStatementAnalyzer.isKeyword("DROP", seq)) { // NOI18N
                        moveToContext(Context.DROP);
                    }
                    break;
                case DROP:
                    if (SQLStatementAnalyzer.isKeyword("TABLE", seq)) { // NOI18N
                        moveToContext(Context.DROP_TABLE);
                    }
                    break;
                case DROP_TABLE:
                    if (seq.token().id() == SQLTokenId.IDENTIFIER) {
                        table = parseIdentifier();
                    }
                    break;
                default:
            }
        } while (nextToken());
    }
}
