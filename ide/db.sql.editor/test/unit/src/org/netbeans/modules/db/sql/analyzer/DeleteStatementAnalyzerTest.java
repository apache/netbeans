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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
public class DeleteStatementAnalyzerTest extends NbTestCase {

    public DeleteStatementAnalyzerTest(String testName) {
        super(testName);
    }

    private static DeleteStatement doAnalyze(String sql) {
        TokenHierarchy<String> hi = TokenHierarchy.create(sql, SQLTokenId.language());
        Quoter quoter = SQLIdentifiersTestUtilities.createNonASCIIQuoter("\"");
        return DeleteStatementAnalyzer.analyze(hi.tokenSequence(SQLTokenId.language()), quoter);
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
        assertEquals(SQLStatementKind.DELETE, doDetectKind("# comment\n delete"));
        assertEquals(SQLStatementKind.DELETE, doDetectKind("delete"));
        assertEquals(SQLStatementKind.DELETE, doDetectKind("delete from foo"));
    }

    /** Just ensuring the analyzer doesn't end in an infinite loop. */
    public void testCanAnalyze() {
        assertCanAnalyze("delete");
        assertCanAnalyze("delete from foo where a = 1");
        assertCanAnalyze("delete from foo where (select c from car where car.x = bar.x) > 42");
        assertCanAnalyze("delete from foo where (select (select");
    }

    public void testContext() {
        String sql = "DELETE sch.t1 alt1 FROM sch.t1 alt1 INNER JOIN sch.t2 ON sch.t1.c1 = sch.t2.c1 ";
        sql += "WHERE alt1.c1 > sch.t2.c3";
        DeleteStatement statement = doAnalyze(sql);
        assertNull(statement.getContextAtOffset(0));
        assertEquals(Context.DELETE, statement.getContextAtOffset(sql.indexOf(" sch.t1")));
        assertEquals(Context.FROM, statement.getContextAtOffset(sql.indexOf("alt1 INNER")));
        assertEquals(Context.FROM, statement.getContextAtOffset(sql.indexOf(" sch.t2")));
        assertEquals(Context.JOIN_CONDITION, statement.getContextAtOffset(sql.indexOf("sch.t1.c1")));
        assertEquals(Context.WHERE, statement.getContextAtOffset(sql.indexOf("sch.t2.c3")));

        sql = "DELETE FROM sch.t1 AS alt1 ";
        sql += "WHERE alt1.c1 > sch.t2.c3";
        statement = doAnalyze(sql);
        assertNull(statement.getContextAtOffset(0));
        assertEquals(Context.FROM, statement.getContextAtOffset(sql.indexOf(" sch.t1")));
        assertEquals(Context.WHERE, statement.getContextAtOffset(sql.indexOf("sch.t2.c3")));
    }

    public void testAnalyzeTables() {
        DeleteStatement statement = doAnalyze("DELETE sch.t1 alt1 FROM sch.t1 alt1 INNER JOIN sch.t2 ON sch.t1.c1 = sch.t2.c1 ");
        Map<String, QualIdent> expectedAliased = new HashMap<String, QualIdent>();
        expectedAliased.put("alt1", new QualIdent("sch", "t1"));
        assertEquals(expectedAliased, statement.getTablesClause().getAliasedTableNames());
        assertEquals(Collections.singleton(new QualIdent("sch", "t2")), statement.getTablesClause().getUnaliasedTableNames());
    }

    public void testUnquote() {
        DeleteStatement statement = doAnalyze("DELETE \"sch\".\"t1\" FROM \"sch\".\"t1\" INNER JOIN \"sch\".\"t2\" ON sch.t1.c1 = sch.t2.c1 ");
        HashSet<QualIdent> expectedUnaliased = new HashSet<QualIdent>();
        expectedUnaliased.add(new QualIdent("sch", "t1"));
        expectedUnaliased.add(new QualIdent("sch", "t2"));
        assertEquals(expectedUnaliased, statement.getTablesClause().getUnaliasedTableNames());

        statement = doAnalyze("DELETE FROM \"\"");
        assertTrue(statement.getTablesClause().getUnaliasedTableNames().isEmpty());
    }

    public void testSubqueries() {
        String sql = "DELETE FROM sch.t1 ";
        sql += "WHERE sch.t1.c1 > (SELECT c2 FROM t2 WHERE c3 = (SELECT c4 FROM t4)) ";
        DeleteStatement statement = doAnalyze(sql);
        assertEquals(0, statement.startOffset);
        assertEquals(sql.length(), statement.endOffset);
        assertTrue(statement.getTablesClause().getUnaliasedTableNames().contains(new QualIdent("sch", "t1")));
        assertEquals(1, statement.getSubqueries().size());

        //(SELECT c2 FROM t2 WHERE c3 = (SELECT c4 FROM t4))
        SelectStatement subquery = statement.getSubqueries().get(0);
        int offset = sql.indexOf("SELECT");
        assertEquals(offset, subquery.startOffset);
        int endOffset = sql.indexOf(") ", offset);
        assertEquals(endOffset, subquery.endOffset);
        assertTrue(subquery.getTablesClause().getUnaliasedTableNames().contains(new QualIdent("t2")));
        assertEquals(1, subquery.getSubqueries().size());

        {
            //(SELECT c4 FROM t4)
            subquery = subquery.getSubqueries().get(0);
            offset = sql.indexOf("SELECT", offset + 1);
            assertEquals(offset, subquery.startOffset);
            offset = sql.indexOf(")", offset);
            assertEquals(offset, subquery.endOffset);
            assertTrue(subquery.getTablesClause().getUnaliasedTableNames().contains(new QualIdent("t4")));
            assertEquals(0, subquery.getSubqueries().size());
        }
    }
}
