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
public class UpdateStatementAnalyzerTest extends NbTestCase {

    public UpdateStatementAnalyzerTest(String testName) {
        super(testName);
    }

    private static UpdateStatement doAnalyze(String sql) {
        TokenHierarchy<String> hi = TokenHierarchy.create(sql, SQLTokenId.language());
        Quoter quoter = SQLIdentifiersTestUtilities.createNonASCIIQuoter("\"");
        return UpdateStatementAnalyzer.analyze(hi.tokenSequence(SQLTokenId.language()), quoter);
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
        assertEquals(SQLStatementKind.UPDATE, doDetectKind("# comment\n update"));
        assertEquals(SQLStatementKind.UPDATE, doDetectKind("update"));
        assertEquals(SQLStatementKind.UPDATE, doDetectKind("update foo set a = 1"));
    }

    /** Just ensuring the analyzer doesn't end in an infinite loop. */
    public void testCanAnalyze() {
        assertCanAnalyze("update");
        assertCanAnalyze("update foo set a = 1");
        assertCanAnalyze("update foo set a = 1, b = 2 where c = 3");
        assertCanAnalyze("update foo, bar set foo.a = 1, bar.a = 1 where (select c from car where car.x = bar.x) > 42");
        assertCanAnalyze("update foo set a = 1 where (select (select");
    }

    public void testContext() {
        String sql = "UPDATE sch.t1 AS alt1 INNER JOIN sch.t2 ON sch.t1.c1 = sch.t2.c1, sch.t3 alt3 ";
        sql += "SET alt1.c2 = 1, alt3 = 4 WHERE alt1.c1 > alt3.c3";
        UpdateStatement statement = doAnalyze(sql);
        assertNull(statement.getContextAtOffset(0));
        assertEquals(Context.UPDATE, statement.getContextAtOffset(sql.indexOf(" sch.t1")));
        assertEquals(Context.UPDATE, statement.getContextAtOffset(sql.indexOf(" sch.t2")));
        assertEquals(Context.JOIN_CONDITION, statement.getContextAtOffset(sql.indexOf("sch.t1.c1")));
        assertEquals(Context.UPDATE, statement.getContextAtOffset(sql.indexOf("sch.t3 alt3")));
        assertEquals(Context.SET, statement.getContextAtOffset(sql.indexOf("= 1")));
        assertEquals(Context.SET, statement.getContextAtOffset(sql.indexOf("= 4")));
        assertEquals(Context.WHERE, statement.getContextAtOffset(sql.indexOf("alt3.c3")));
    }

    public void testAnalyzeTables() {
        UpdateStatement statement = doAnalyze("UPDATE sch.t1 AS alt1 INNER JOIN sch.t2 ON sch.t1.c1 = sch.t2.c1, sch.t3 alt3 SET");
        Map<String, QualIdent> expectedAliased = new HashMap<String, QualIdent>();
        expectedAliased.put("alt1", new QualIdent("sch", "t1"));
        expectedAliased.put("alt3", new QualIdent("sch", "t3"));
        assertEquals(expectedAliased, statement.getTablesClause().getAliasedTableNames());
        assertEquals(Collections.singleton(new QualIdent("sch", "t2")), statement.getTablesClause().getUnaliasedTableNames());
    }

    public void testUnquote() throws Exception {
        UpdateStatement statement = doAnalyze("UPDATE \"sch\".\"t1\" INNER JOIN \"sch\".\"t2\" ON sch.t1.c1 = sch.t2.c1, \"t3\".\"\" SET");
        HashSet<QualIdent> expectedUnaliased = new HashSet<QualIdent>();
        expectedUnaliased.add(new QualIdent("sch", "t1"));
        expectedUnaliased.add(new QualIdent("sch", "t2"));
        expectedUnaliased.add(new QualIdent("t3"));
        assertEquals(expectedUnaliased, statement.getTablesClause().getUnaliasedTableNames());

        statement = doAnalyze("UPDATE \"\"");
        assertTrue(statement.getTablesClause().getUnaliasedTableNames().isEmpty());
    }

    public void testSubqueries() throws Exception {
        String sql = "UPDATE (SELECT tablename FROM sch.t1) ";
        sql += "SET c1 = (SELECT c2 FROM t2 WHERE c3 = (SELECT c4 FROM t4)) ";
        sql += "WHERE alt1.c1 > (SELECT c5 FROM t5)";

        UpdateStatement statement = doAnalyze(sql);
        assertEquals(0, statement.startOffset);
        assertEquals(sql.length(), statement.endOffset);
        assertTrue(statement.getTablesClause().getUnaliasedTableNames().isEmpty());
        assertEquals(3, statement.getSubqueries().size());

        // (SELECT tablename FROM sch.t1)
        SelectStatement subquery = statement.getSubqueries().get(0);
        int offset = sql.indexOf("SELECT");
        assertEquals(offset, subquery.startOffset);
        offset = sql.indexOf(")", offset);
        assertEquals(offset, subquery.endOffset);
        assertTrue(subquery.getTablesClause().getUnaliasedTableNames().contains(new QualIdent("sch", "t1")));
        assertTrue(subquery.getSubqueries().isEmpty());

        //(SELECT c2 FROM t2 WHERE c3 = (SELECT c4 FROM t4))
        subquery = statement.getSubqueries().get(1);
        offset = sql.indexOf("SELECT", offset);
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

        // (SELECT c5 FROM t5)
        subquery = statement.getSubqueries().get(2);
        offset = sql.indexOf("SELECT", offset);
        assertEquals(offset, subquery.startOffset);
        offset = sql.indexOf(")", offset);
        assertEquals(offset, subquery.endOffset);
        assertTrue(subquery.getTablesClause().getUnaliasedTableNames().contains(new QualIdent("t5")));
        assertEquals(0, subquery.getSubqueries().size());
    }
}
