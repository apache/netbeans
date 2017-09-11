/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
