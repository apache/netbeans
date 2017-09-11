/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.db.sql.execute;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.sql.execute.SQLExecuteHelper.Compatibility;

/**
 *
 * @author Andrei Badea
 */
public class SQLExecuteHelperTest extends NbTestCase {
    
    public SQLExecuteHelperTest(String testName) {
        super(testName);
    }

    public void testSplit() {
        // removing line comments
        assertSplit("select --line\n from dual", "select  from dual");
        assertSplit("select ----line\n from dual", "select  from dual");
        assertSplit("select --line from dual", "select");
        assertSplit("-- This should be ignored \nselect from dual", "select from dual");
        assertSplit("select #line\n from dual", "select  from dual");
        assertSplit("select ##line\n from dual", "select  from dual");
        assertSplit("select #line from dual", "select");
        assertSplit("# This should be ignored \nselect from dual", "select from dual");
        assertSplit("select #line\n from dual", Compatibility.COMPAT_GENERIC, "select #line\n from dual");
        assertSplit("select ##line\n from dual", Compatibility.COMPAT_GENERIC, "select ##line\n from dual");
        assertSplit("select #line from dual", Compatibility.COMPAT_GENERIC, "select #line from dual");
        assertSplit("# This should not valid", Compatibility.COMPAT_GENERIC, "# This should not valid");

        // removing block comments
        assertSplit("select /* block */ from dual", "select  from dual");
        assertSplit("select ///* block */ from dual", "select // from dual");
        assertSplit("select /* block * block ***/ from dual", "select  from dual");
        assertSplit("select /* block from dual", "select");
        assertSplit("select a - b / c from dual", "select a - b / c from dual");
        assertSplit("select 'foo /* bar */ -- baz' from dual", "select 'foo /* bar */ -- baz' from dual");
        assertSplit("select 'foo /* bar */ # baz' from dual", "select 'foo /* bar */ # baz' from dual");

        assertSplit("This is a test; #comment\n#A full comment line\nAnother test;",
                new String[]{"This is a test", "Another test"});


        // ; in comments should not be considered a statement separator
        assertSplit("select --comment; \n foo", "select  foo");
        assertSplit("select /* ; */ foo", "select  foo");

        // splitting
        assertSplit(" ;; ; ", new String[0]);
        assertSplit("/* comment */ select foo; /* comment */ select bar -- comment", new String[] { "select foo", "select bar" });

        // newlines in strings
        assertSplit("select 'foo\nbar';", new String[] { "select 'foo\nbar'" });

        // test changing the delimiter
        assertSplit("select delimiter foo; " +
                    "delimiter ?? " +
                    "select bar; select delimiter baz?? " +
                    "delimiter ; " +
                    "select beetle; " +
                    "select baddle;",
                    new String[] {
                        "select delimiter foo",
                        "select bar; select delimiter baz",
                        "select beetle",
                        "select baddle"
                    });

        // double-slash delimiter
        assertSplit("DELIMITER //\n" +
                "CREATE PROCEDURE p1()\n" +
                "BEGIN\n" +
                "  SELECT * FROM tab_customer;\n" +
                "  SELECT * FROM tab_customer;\n" +
                "END//",
                "CREATE PROCEDURE p1()\n" +
                "BEGIN\n" +
                "  SELECT * FROM tab_customer;\n" +
                "  SELECT * FROM tab_customer;\n" +
                "END"
                );
        
        // splitting and start/end positions
        String test = "  select foo  ;   select /* comment */bar;\n   select baz -- comment";
        
        Map<Integer,Integer> posMap1 = new HashMap<>();
        posMap1.put(0, 2);
        Map<Integer,Integer> posMap2 = new HashMap<>();
        posMap2.put(0, 18);
        posMap2.put(7, 38);
        Map<Integer,Integer> posMap3 = new HashMap<>();
        posMap3.put(0, 46);
        
        assertSplit(test, new StatementInfo[] { 
            new StatementInfo("select foo", 0, 2, 0, 2, 12, 14, posMap1, Arrays.asList(42)),
            new StatementInfo("select bar", 15, 18, 0, 18, 41, 41, posMap2, Arrays.asList(42)),
            new StatementInfo("select baz", 42, 46, 1, 3, 56, 67, posMap3, Arrays.asList(42))
        });

        // correct start line with line comments (#171865)
        String sql = "\n\n-- ---\n-- line comment\n-- ---\nselect foo  ;";

        Map<Integer,Integer> posMap4 = new HashMap<>();
        posMap4.put(0, 32);        
        assertSplit(sql, "select foo");
        assertSplit(sql, new StatementInfo("select foo", 0, 32, 5, 0, 42, 44, posMap4, Arrays.asList(0,1,8,24, 31)));
    }
    
    public void testQuoteHandling() {
        assertSplit("select * from b where a = ';';"   // Normal String quoting
                + "select * from b where \";\" = 'a';" // SQL Identifier quoting
                + "select * from b where [;] = 'a';" // MSSQL Identifier Quoting
                + "select * from b where `;` = 'a';" // MySQL Identifier Quoting
                // The follwing statemens are variants of the four previous
                // statements with identifier escaping
                + "select * from b where a = ''';';" 
                + "select * from b where \";\"\"\" = 'a';"
                + "select * from b where []];] = 'a';"
                + "select * from b where `;``` = 'a';"
                , "select * from b where a = ';'"
                , "select * from b where \";\" = 'a'"
                , "select * from b where [;] = 'a'"
                , "select * from b where `;` = 'a'"
                , "select * from b where a = ''';'"
                , "select * from b where \";\"\"\" = 'a'"
                , "select * from b where []];] = 'a'"
                , "select * from b where `;``` = 'a'"
        );

        assertSplit("select * from b where b = $$a;b$$;" // Simple Postgresql $-Quote
                + "SELECT * from b WHERE a = ';';"       // SQL-99 Quote
                + "select * from b where b = $function$SELECT $a from b WHERE c = $inner$d$inner$; SELECT dummy;$function$;"  // Postgresql-Quote with embedded token and embedded quote
                  , Compatibility.COMPAT_POSTGERSQL
                  , "select * from b where b = $$a;b$$"
                  , "SELECT * from b WHERE a = ';'"
                  , "select * from b where b = $function$SELECT $a from b WHERE c = $inner$d$inner$; SELECT dummy;$function$");
    }
    
    public void testDelimiterComment() {
        // See bug #234246
        // Block comment
        assertSplit("SELECT * FROM a;SELECT * FROM b/*DELIMITER //*///"
                + "SELECT * FROM a//SELECT * FROM b"
                , "SELECT * FROM a"
                , "SELECT * FROM b"
                , "SELECT * FROM a"
                , "SELECT * FROM b");
        // Line comment
        assertSplit("SELECT * FROM a;\n"
                + "SELECT * FROM b\n"
                + "--   DELIMITER //\n"
                + "//"
                + "SELECT * FROM a//\n"
                + "SELECT * FROM b"
                , "SELECT * FROM a"
                , "SELECT * FROM b"
               , "SELECT * FROM a", "SELECT * FROM b");
        // Block comment
        assertSplit("/*DELIMITER --*--*/\n"
                + "SELECT * FROM a\n"
                + "--*--"
                + "SELECT * FROM b\n"
                , "SELECT * FROM a"
                , "SELECT * FROM b");
        // Guard against problems at line end
        assertSplit("SELECT * FROM b/*DELIMITER --*--*/", "SELECT * FROM b");
        assertSplit("SELECT * FROM b--DELIMITER --*--", "SELECT * FROM b");
    }
    
    private static void assertSplit(String script, String... expected) {
        assertSplit(script, Compatibility.COMPAT_MYSQL, expected);
    }

    private static void assertSplit(String script, Compatibility compat, String... expected) {
        List<StatementInfo> stmts = SQLExecuteHelper.split(script, compat);
        
        for (int i = 0; i < Math.min(expected.length, stmts.size()); i++) {
            assertEquals(expected[i], stmts.get(i).getSQL());
        }
        
        assertEquals(expected.length, stmts.size());
    }
    
    private static void assertSplit(String script, StatementInfo... expected) {
        List<StatementInfo> stmts = SQLExecuteHelper.split(script);
        assertEquals(expected.length, stmts.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i].getSQL(), stmts.get(i).getSQL());
            assertEquals(expected[i].getRawStartOffset(), stmts.get(i).getRawStartOffset());
            assertEquals(expected[i].getStartOffset(), stmts.get(i).getStartOffset());
            assertEquals(expected[i].getStartLine(), stmts.get(i).getStartLine());
            assertEquals(expected[i].getStartColumn(), stmts.get(i).getStartColumn());
            assertEquals(expected[i].getEndOffset(), stmts.get(i).getEndOffset());
            assertEquals(expected[i].getRawEndOffset(), stmts.get(i).getRawEndOffset());
            assertEquals(expected[i].getNewLineOffsets(), stmts.get(i).getNewLineOffsets());
            assertEquals(expected[i].getSqlPosToRawPos(), stmts.get(i).getSqlPosToRawPos());
        }
    }

    public void testFindStatementAtOffset() {
        String sql = "select * from APP.JHTEST; \n"
                + "select * from APP.JHTEST where COL_NUMERIC = 2;";
        List<StatementInfo> l = SQLExecuteHelper.split(sql, Compatibility.COMPAT_GENERIC);

        StatementInfo s1 = l.get(0);
        StatementInfo s2 = l.get(1);

        assertSame(s1, SQLExecuteHelper.findStatementAtOffset(l, 24, sql)); //;
        assertSame(s1, SQLExecuteHelper.findStatementAtOffset(l, 25, sql)); //_
        assertSame(s1, SQLExecuteHelper.findStatementAtOffset(l, 26, sql)); //\n
        assertSame(s2, SQLExecuteHelper.findStatementAtOffset(l, 27, sql)); //s
        assertSame(s2, SQLExecuteHelper.findStatementAtOffset(l, 28, sql)); //e
    }

    public void testFindStatementAtOffsetAtTheEndOfFile() {
        String sql = "select * from APP.JHTEST;\n"
                + "select * from APP.JHTEST;";
        List<StatementInfo> l = SQLExecuteHelper.split(sql, Compatibility.COMPAT_GENERIC);

        StatementInfo s2 = l.get(1);
        assertSame(s2, SQLExecuteHelper.findStatementAtOffset(l, 51, sql));
    }
}
