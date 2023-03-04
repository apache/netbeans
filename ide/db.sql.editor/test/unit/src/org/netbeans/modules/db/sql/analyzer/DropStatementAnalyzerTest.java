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

import java.io.IOException;
import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.explorer.test.api.SQLIdentifiersTestUtilities;
import org.netbeans.modules.db.sql.analyzer.SQLStatement.Context;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;

/**
 * @author Jiri Skrivanek
 */
public class DropStatementAnalyzerTest extends NbTestCase {

    public DropStatementAnalyzerTest(String testName) {
        super(testName);
    }

    private static DropStatement doAnalyze(String sql) {
        TokenHierarchy<String> hi = TokenHierarchy.create(sql, SQLTokenId.language());
        Quoter quoter = SQLIdentifiersTestUtilities.createNonASCIIQuoter("\"");
        return DropStatementAnalyzer.analyze(hi.tokenSequence(SQLTokenId.language()), quoter);
    }

    private static SQLStatementKind doDetectKind(String sql) {
        TokenHierarchy<String> hi = TokenHierarchy.create(sql, SQLTokenId.language());
        return SQLStatementAnalyzer.analyzeKind(hi.tokenSequence(SQLTokenId.language()));
    }

    public static void assertCanAnalyze(String sql) throws IOException {
        assertNotNull(doAnalyze(sql));
    }

    /**
     * Just ensuring the analyzer doesn't end in an infinite loop.
     */
    public void testCanAnalyze() throws Exception {
        assertCanAnalyze("drop");
        assertCanAnalyze("drop table customer");
    }

    public void testAnalyzeSimple() throws Exception {
        DropStatement statement = doAnalyze("drop table cat_1.schema_1.customer");
        assertEquals("cat_1.schema_1.customer", statement.getTable().toString());
    }

    public void testContext() throws Exception {
        String sql = "drop table customer";
        DropStatement statement = doAnalyze(sql);
        assertNull(statement.getContextAtOffset(0));
        assertEquals(Context.DROP, statement.getContextAtOffset(sql.indexOf("table")));
        assertEquals(Context.DROP_TABLE, statement.getContextAtOffset(sql.indexOf("customer")));
    }

    public void testDetectKind() throws Exception {
        assertNull(doDetectKind("foo"));
        assertEquals(SQLStatementKind.DROP, doDetectKind("drop"));
        assertEquals(SQLStatementKind.DROP, doDetectKind("drop table customer"));
    }
}
