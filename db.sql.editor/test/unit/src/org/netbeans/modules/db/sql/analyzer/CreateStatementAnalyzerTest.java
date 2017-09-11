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
