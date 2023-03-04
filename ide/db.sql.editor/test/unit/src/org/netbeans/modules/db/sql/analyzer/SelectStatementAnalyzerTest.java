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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import junit.framework.TestCase;
import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.db.explorer.test.api.SQLIdentifiersTestUtilities;
import org.netbeans.modules.db.sql.analyzer.SQLStatement.Context;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;

/**
 *
 * @author Andrei Badea
 */
public class SelectStatementAnalyzerTest extends TestCase {

    public SelectStatementAnalyzerTest(String testName) {
        super(testName);
    }

    /**
     * Just ensuring the analyzer doesn't end in an infinite loop.
     */
    public void testCanAnalyze() throws Exception {
        assertCanAnalyze("select");
        assertCanAnalyze("select from dual");
        assertCanAnalyze("select count(*) table_count from table");
        assertCanAnalyze("select component, count(id) from issuezilla group by component");
        assertCanAnalyze("select * from a where (select count(*) from b where a.x = b.x) > 42");
        assertCanAnalyze("select * from a where (select (select");
    }

    public void testAnalyzeSimple() throws Exception {
        SelectStatement statement = doAnalyze("select f.bar as bingo, count(*), max(id) + 1 themax, cat.sch.foo.baz from foo f inner join bar");
        List<List<String>> selectValues = statement.getSelectValues();
        assertEquals(3, selectValues.size());
        assertEquals(Arrays.asList("bingo"), selectValues.get(0));
        assertEquals(Arrays.asList("themax"), selectValues.get(1));
        assertEquals(Arrays.asList("cat", "sch", "foo", "baz"), selectValues.get(2));
        TablesClause fromClause = statement.getTablesClause();
        assertEquals(Collections.singletonMap("f", new QualIdent("foo")), fromClause.getAliasedTableNames());
        assertEquals(Collections.singleton(new QualIdent("bar")), fromClause.getUnaliasedTableNames());
    }

    public void testAnalyzeFromCommaDelimitedTableNames() throws Exception {
        SelectStatement statement = doAnalyze("select * from foo, bar b");
        assertEquals(Collections.singleton(new QualIdent("foo")), statement.getTablesClause().getUnaliasedTableNames());
        assertEquals(Collections.singletonMap("b", new QualIdent("bar")), statement.getTablesClause().getAliasedTableNames());
    }

    public void testAnalyzeFromJoinTableNames() throws Exception {
        SelectStatement statement = doAnalyze("select * from foo f inner join bar on f.");
        assertEquals(Collections.singleton(new QualIdent("bar")), statement.getTablesClause().getUnaliasedTableNames());
        assertEquals(Collections.singletonMap("f", new QualIdent("foo")), statement.getTablesClause().getAliasedTableNames());
    }

    public void testEndOfFromClauseIssue145143() throws Exception {
        Set<QualIdent> expected = Collections.singleton(new QualIdent("foo"));
        assertEquals(expected, doAnalyze("select * from foo where max(bar, baz) < 0").getTablesClause().getUnaliasedTableNames());
        assertEquals(expected, doAnalyze("select * from foo having max(bar, baz) < 0").getTablesClause().getUnaliasedTableNames());
        assertEquals(expected, doAnalyze("select * from foo order by bar, baz").getTablesClause().getUnaliasedTableNames());
        assertEquals(expected, doAnalyze("select * from foo group by bar, baz").getTablesClause().getUnaliasedTableNames());
    }

    public void testFromClause() throws Exception {
        SelectStatement statement = doAnalyze("select foo");
        assertNull(statement.getTablesClause());

        statement = doAnalyze("select foo from");
        assertNotNull(statement.getTablesClause());
    }

    public void testUnquote() throws Exception {
        SelectStatement statement = doAnalyze("select * from \"foo\".\"bar\"");
        assertEquals(Collections.singleton(new QualIdent("foo", "bar")), statement.getTablesClause().getUnaliasedTableNames());

        statement = doAnalyze("select * from \"foo\".\"\" inner join \"baz\"");
        assertEquals(new HashSet<QualIdent>(Arrays.asList(new QualIdent("foo"), new QualIdent("baz"))), statement.getTablesClause().getUnaliasedTableNames());

        statement = doAnalyze("select * from \"\"");
        assertTrue(statement.getTablesClause().getUnaliasedTableNames().isEmpty());
    }

    public void testUnknownIdentifiers() throws Exception {
        // SQL generated from embedded code may have __UNKNOWN__ flags in it -
        // make sure these are handled correctly
        SelectStatement stmt = doAnalyze("SELECT __UNKNOWN__");
        List<List<String>> selectValues = stmt.getSelectValues();
        assertEquals(1, selectValues.size());
        assertEquals(Arrays.asList("__UNKNOWN__"), selectValues.get(0));

        stmt = doAnalyze("SELECT foo FROM __UNKNOWN__, bar");
        selectValues = stmt.getSelectValues();
        assertEquals(1, selectValues.size());
        assertEquals(Arrays.asList("foo"), selectValues.get(0));
        TablesClause fromClause = stmt.getTablesClause();
        assertEquals(new HashSet<QualIdent>(Arrays.asList(new QualIdent("__UNKNOWN__"), new QualIdent("bar"))), fromClause.getUnaliasedTableNames());

        // From PHP, sometimes you get weird things if the string isn't complete,
        // we should be able to handle this...
        stmt = doAnalyze("SELECT  FROM foo,\necho");
        selectValues = stmt.getSelectValues();
        assertEquals(0, selectValues.size());
        fromClause = stmt.getTablesClause();
        Set<QualIdent> tableNames = fromClause.getUnaliasedTableNames();
        assertEquals(new HashSet<QualIdent>(Arrays.asList(new QualIdent("foo"), new QualIdent("echo"))), tableNames);
    }

    public void testSubqueries() throws Exception {
        String sql = " select * from foo where exists (select id from bar where bar.id = foo.id and (select count(id) from baz where bar.id = baz.id) = 1) order by xyz";
        int firstSubStart = sql.indexOf("(select") + 1;
        int firstSubEnd = sql.indexOf(" order", firstSubStart) - 1;
        int secondSubStart = sql.indexOf("(select", firstSubStart) + 1;
        int secondSubEnd = sql.indexOf(" = 1", secondSubStart) - 1;

        SelectStatement statement = doAnalyze(sql);
        assertEquals(0, statement.startOffset);
        assertEquals(sql.length(), statement.endOffset);
        assertTrue(statement.getTablesClause().getUnaliasedTableNames().contains(new QualIdent("foo")));
        assertEquals(1, statement.getSubqueries().size());

        SelectStatement subquery = statement.getSubqueries().get(0);
        assertEquals(firstSubStart, subquery.startOffset);
        assertEquals(firstSubEnd, subquery.endOffset);
        assertEquals(sql.length(), statement.endOffset);
        assertTrue(subquery.getTablesClause().getUnaliasedTableNames().contains(new QualIdent("bar")));
        assertEquals(1, statement.getSubqueries().size());

        subquery = subquery.getSubqueries().get(0);
        assertEquals(secondSubStart, subquery.startOffset);
        assertEquals(secondSubEnd, subquery.endOffset);
        assertTrue(subquery.getTablesClause().getUnaliasedTableNames().contains(new QualIdent("baz")));
        assertEquals(0, subquery.getSubqueries().size());
    }

    public void testSubqueriesContext() {
        String sql = "select * from foo where exists ";
        sql += "(select id from bar where bar.id = foo.id and ";
        sql += "(select count(id) from baz where bar.id = baz.id) = 1) order by xyz";
        SelectStatement statement = doAnalyze(sql);
        Context context = statement.getContextAtOffset(sql.indexOf("bar"));
        assertEquals(Context.FROM, context);

        context = statement.getContextAtOffset(sql.indexOf("baz"));
        assertEquals(Context.FROM, context);

        context = statement.getContextAtOffset(sql.indexOf("1"));
        assertEquals(Context.WHERE, context);

        sql = "select * from foo where exists ";
        sql += "(select id from bar where bar.id = foo.id) and foo.a";
        statement = doAnalyze(sql);
        context = statement.getContextAtOffset(sql.indexOf("foo.a"));
        assertEquals(Context.WHERE, context);

        // unfinished subquery
        sql = "select * from foo where exists " +
                "(select id from ";
        statement = doAnalyze(sql);
        context = statement.getContextAtOffset(sql.length() -1);
        assertEquals(Context.FROM, context);
    }

    public void testContext() throws Exception {
        String sql = "select customer_id from customer inner join invoice on customer.id = invoice.customer_id, foobar " +
                "where vip = 1 group by customer_id having count(items) < 2 order by customer_id asc";
        SelectStatement statement = doAnalyze(sql);
        assertNull(statement.getContextAtOffset(0));
        assertEquals(Context.SELECT, statement.getContextAtOffset(sql.indexOf(" customer_id")));
        assertEquals(Context.FROM, statement.getContextAtOffset(sql.indexOf("customer ")));
        assertEquals(Context.JOIN_CONDITION, statement.getContextAtOffset(sql.indexOf(".id =")));
        assertEquals(Context.FROM, statement.getContextAtOffset(sql.indexOf(" foobar")));
        assertEquals(Context.WHERE, statement.getContextAtOffset(sql.indexOf("vip")));
        assertEquals(Context.GROUP_BY, statement.getContextAtOffset(sql.indexOf("customer_id having")));
        assertEquals(Context.HAVING, statement.getContextAtOffset(sql.indexOf("count")));
        assertEquals(Context.ORDER_BY, statement.getContextAtOffset(sql.indexOf("customer_id asc")));
    }

    public void testDetectKind() throws Exception {
        assertNull(doDetectKind("foo"));
        assertEquals(SQLStatementKind.SELECT, doDetectKind("select"));
        assertEquals(SQLStatementKind.SELECT, doDetectKind("select * from foo"));
    }

    private static SelectStatement doAnalyze(String sql) {
        TokenHierarchy<String> hi = TokenHierarchy.create(sql, SQLTokenId.language());
        Quoter quoter = SQLIdentifiersTestUtilities.createNonASCIIQuoter("\"");
        return SelectStatementAnalyzer.analyze(hi.tokenSequence(SQLTokenId.language()), quoter);
    }

    private static SQLStatementKind doDetectKind(String sql) {
        TokenHierarchy<String> hi = TokenHierarchy.create(sql, SQLTokenId.language());
        return SQLStatementAnalyzer.analyzeKind(hi.tokenSequence(SQLTokenId.language()));
    }

    public static void assertCanAnalyze(String sql) throws IOException {
        assertNotNull(doAnalyze(sql));
    }
}
