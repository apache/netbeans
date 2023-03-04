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
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.explorer.test.api.SQLIdentifiersTestUtilities;
import org.netbeans.modules.db.sql.analyzer.SQLStatement.Context;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;

/**
 *
 * @author Jiri Skrivanek
 */
public class CreateStatementAnalyzerTest extends NbTestCase {

    private static final String SQL =
            "create procedure p1()\n" +
            "begin\n" +
            "  select * from sch.t1;\n" +
            "  select * from sch.t1;\n" +
            "end";

    public CreateStatementAnalyzerTest(String testName) {
        super(testName);
    }

    private static CreateStatement doAnalyze(String sql) {
        TokenHierarchy<String> hi = TokenHierarchy.create(sql, SQLTokenId.language());
        Quoter quoter = SQLIdentifiersTestUtilities.createNonASCIIQuoter("\"");
        return CreateStatementAnalyzer.analyze(hi.tokenSequence(SQLTokenId.language()), quoter);
    }

    private static SQLStatementKind doDetectKind(String sql) {
        TokenHierarchy<String> hi = TokenHierarchy.create(sql, SQLTokenId.language());
        return SQLStatementAnalyzer.analyzeKind(hi.tokenSequence(SQLTokenId.language()));
    }

    public static void assertCanAnalyze(String sql) {
        assertNotNull(doAnalyze(sql));
    }

    public void testDetectKind() {
        assertNull(doDetectKind("foo"));
        assertEquals(SQLStatementKind.CREATE, doDetectKind("# comment\n create"));
        assertEquals(SQLStatementKind.CREATE, doDetectKind("create procedure"));
    }

    /** Just ensuring the analyzer doesn't end in an infinite loop. */
    public void testCanAnalyze() {
        assertCanAnalyze("create");
        assertCanAnalyze(SQL);
    }

    public void testContext() {
        CreateStatement statement = doAnalyze(SQL);
        assertNull(statement.getContextAtOffset(0));
        assertEquals(Context.CREATE, statement.getContextAtOffset(SQL.indexOf(" procedure")));
        assertEquals(Context.CREATE_PROCEDURE, statement.getContextAtOffset(SQL.indexOf(" p1")));
        assertEquals(Context.BEGIN, statement.getContextAtOffset(SQL.indexOf("select")));
        assertEquals(Context.END, statement.getContextAtOffset(SQL.length()));
    }

    public void testBody() {
        CreateStatement statement = doAnalyze(SQL);
        assertTrue(statement.hasBody());
        assertEquals(27, statement.getBodyStartOffset());
        assertEquals(76, statement.getBodyEndOffset());

        String sql = "create";
        statement = doAnalyze(sql);
        assertFalse(statement.hasBody());

        sql = "create procedure p1()\n" +
            "begin\n" +
            " sel";
        statement = doAnalyze(sql);
        assertTrue(statement.hasBody());
        assertEquals(27, statement.getBodyStartOffset());
        assertEquals(sql.length(), statement.getBodyEndOffset());
    }
}
