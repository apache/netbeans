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
import java.util.List;
import junit.framework.TestCase;
import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.db.explorer.test.api.SQLIdentifiersTestUtilities;
import org.netbeans.modules.db.sql.analyzer.SQLStatement.Context;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;

/**
 *
 * @author Jiri Rechtacek
 */
public class InsertStatementAnalyzerTest extends TestCase {

    public InsertStatementAnalyzerTest(String testName) {
        super(testName);
    }

    /**
     * Just ensuring the analyzer doesn't end in an infinite loop.
     */
    public void testCanAnalyze() throws Exception {
        assertNull(doAnalyze(""));
        assertCanAnalyze("insert");
        assertCanAnalyze("insert into");
        assertCanAnalyze("insert into tab (id, name) values (\"1\", \"John\")");
        assertCanAnalyze("insert into tab values (\"2\", \"Scott\", \"Staff\"");
    }

    public void testAnalyzeInsertWholeTable() throws Exception {
        InsertStatement statement = doAnalyze("insert into tab values (\"2\", \"Scott\", \"Staff\"");
        List<String> columns = statement.getColumns ();
        assertEquals(0, columns.size());
    }

    public void testAnalyzeInsertChosenColumns() throws Exception {
        InsertStatement statement = doAnalyze("insert into tab (id, name) values (\"1\", \"John\")");
        List<String> columns = statement.getColumns ();
        assertEquals(2, columns.size());
    }

    public void testDetectKind() throws Exception {
        assertNull(doDetectKind("foo"));
        assertEquals(SQLStatementKind.INSERT, doDetectKind("insert"));
        assertEquals(SQLStatementKind.INSERT, doDetectKind("insert into tab"));
        assertFalse(SQLStatementKind.INSERT.equals (doDetectKind("select")));
        assertFalse(SQLStatementKind.INSERT.equals (doDetectKind("select * from foo")));
    }

    public void testContext() throws Exception {
        String sql = "INSERT INTO sch.t1 (c1) VALUES ('val1', 'val2', 'val3')";
        InsertStatement statement = doAnalyze(sql);
        assertNull(statement.getContextAtOffset(0));
        assertEquals(Context.INSERT, statement.getContextAtOffset(sql.indexOf(" INTO")));
        assertEquals(Context.INSERT_INTO, statement.getContextAtOffset(sql.indexOf(" sch.t1")));
        assertEquals(Context.COLUMNS, statement.getContextAtOffset(sql.indexOf("c1)")));
        assertEquals(Context.COLUMNS, statement.getContextAtOffset(sql.indexOf("VALUES")));
        assertEquals(Context.VALUES, statement.getContextAtOffset(sql.indexOf(" ('val1")));
    }

    private static InsertStatement doAnalyze(String sql) {
        TokenHierarchy<String> hi = TokenHierarchy.create(sql, SQLTokenId.language());
        Quoter quoter = SQLIdentifiersTestUtilities.createNonASCIIQuoter("\"");
        return InsertStatementAnalyzer.analyze(hi.tokenSequence(SQLTokenId.language()), quoter);
    }

    private static SQLStatementKind doDetectKind(String sql) {
        TokenHierarchy<String> hi = TokenHierarchy.create(sql, SQLTokenId.language());
        return SQLStatementAnalyzer.analyzeKind(hi.tokenSequence(SQLTokenId.language()));
    }

    public static void assertCanAnalyze(String sql) throws IOException {
        assertNotNull(doAnalyze(sql));
    }
}
