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

package org.netbeans.modules.php.editor.sql;

import javax.swing.text.Document;
import org.netbeans.modules.db.sql.editor.api.completion.SQLCompletion;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.parser.ParserTestBase;

/**
 *
 * @author David Van Couvering
 */
public class PHPSQLCompletionTest extends ParserTestBase {
    static final String UNKNOWN = SQLCompletion.UNKNOWN_TAG;

    public PHPSQLCompletionTest(String name) {
        super(name);
    }

    public void testBasic_001() {
        checkPHPSQLStatement("<?php echo \"SELECT ab|cde\" ?>", " SELECT abcde ");
    }

    public void testBasic_002() {
        checkPHPSQLStatement("<?php echo \"SELECT * FROM foo WHERE x='abc' AND |\" ?>",
                " SELECT * FROM foo WHERE x='abc' AND  ");
    }

    public void testBasic_003() {
        checkPHPSQLStatement("<?php echo 'SELECT * from foo WHERE | ' ?>", " SELECT * from foo WHERE   ");
    }

    public void testVariables_001() {
        checkPHPSQLStatement("<?php echo \"SELECT ${v|ar}\" ?>", "SELECT __UNKNOWN__");
    }

    public void testVariables_002() {
        checkPHPSQLStatement("<?php echo \"SELECT {$var} FROM |\"", "SELECT __UNKNOWN__ FROM ");
    }

    public void testVariables_003() {
        checkPHPSQLStatement("<?php echo \"SELECT foo{$var}b|ar\" ?>", "SELECT foo__UNKNOWN__bar");
    }

    public void testVariables_004() {
        checkPHPSQLStatement("<?php echo \"SELECT ${foo}, c.| FROM bar\" ?>", "SELECT __UNKNOWN__, c. FROM bar");
    }

    public void testVariables_005() {
        checkPHPSQLStatement("<?php echo \"SELECT ${foo}, bar FROM |, bar\" ?>", "SELECT __UNKNOWN__, bar FROM , bar");
    }

    public void testVariables_006() {
        checkPHPSQLStatement("<?php echo \"SELECT ${fruits['banana']} FROM |\" ?>", "SELECT __UNKNOWN__ FROM ");
    }

    public void testVariables_007() {
        checkPHPSQLStatement("<?php echo \"SELECT \" . ${fruits['banana']} . \" FROM |\" ?>",
                " SELECT  __UNKNOWN__  FROM  ");
    }

    public void testVariables_008() {
        checkPHPSQLStatement("<?php echo \"SELECT \" . {$fruits['banana']} . \" FROM |\" ?>",
                " SELECT  __UNKNOWN__  FROM  ");
    }

    public void testVariables_009() {
        checkPHPSQLStatement("<?php echo \"SELECT \" . $fruits['banana'] . \" FROM |\" ?>",
                " SELECT  __UNKNOWN__  FROM  ");
    }

    public void testIncompleteString_001() {
        // Need to be able to pick up a SQL statement even if the string is not
        // completed.
        checkPHPSQLStatement("<?php echo \"SELECT | FROM foo,\necho \"$var\";\n?>", " SELECT  FROM foo,\necho  ");
    }

    public void testIncompleteString_002() {
        checkPHPSQLStatement("<?php echo \"SELECT | FROM foo\n?>", " SELECT  FROM foo\n?>\n");
    }

    public void testIncompleteString_003() {
        checkPHPSQLStatement("<?php echo <<<HERE\nSELECT | FROM ?>", "SELECT  FROM ?>");
    }

    public void testIncompleteString_004() {
        checkPHPSQLStatement("<?php echo \"SELECT | FROM foo\necho \"this is another string\"; ?>",
                " SELECT  FROM foo\necho  ");
    }

    // If you have a long enough section that maps to __UNKNOWN__, then if you
    // place the caret in the unknown section the mapping from source to virtual
    // pushes the caret to the end of the __UNKNOWN__.  Then when we map back
    // to source, it's not in the same place.  This is OK as long as the caret is
    // in the unknown section, because we'll never do completion on an unknown section,
    // but we need to make sure stuff *after* the long unknown section maps back correctly
    public void testLongUnknowns_001() {
        checkPHPSQLStatement("<?php echo \"SELECT ${thisisaverylongvariable} FROM | WHERE foo\" ?>",
                "SELECT __UNKNOWN__ FROM  WHERE foo");
    }

    public void testConcatenation_001() {
        checkPHPSQLStatement("<?php echo \"SELECT foo\" . \"${v|ar}\" ?>", " SELECT foo __UNKNOWN__");

    }
    public void testConcatenation_002() {
        checkPHPSQLStatement("<?php echo 42; echo \"SELECT foo{$var}b|ar\" ?>", "SELECT foo__UNKNOWN__bar");
    }

    public void testConcatenation_003() {
        checkPHPSQLStatement("<?php echo \"SELECT ${foo}\" . \"${bar} FROM |\" ?>", "SELECT __UNKNOWN__ FROM ");
    }

    public void testSingleQuotes_001() {
        checkPHPSQLStatement("<?php echo 'SELECT foo ' . ${foo} . \" ${var} testing $var |\" ?>",
                " SELECT foo  __UNKNOWN__ __UNKNOWN__ testing __UNKNOWN__ ");
    }

    public void testHereDoc_001() {
        checkPHPSQLStatement("<?php\n" +
                "   echo <<<HERE\n" +
                "select * from |$foo\n" +
                "HERE\n ?>", "select * from __UNKNOWN__");
    }

    public void testHereDoc_002() {
        checkPHPSQLStatement("<?php\n echo <<<HERE\nselect * from |$foo\nHERE\n . \" where foo| = bar\" ?>",
                "select * from __UNKNOWN__  where foo = bar ");
    }

    public void testNowDoc_001() {
        checkPHPSQLStatement("<?php\n" +
                "   echo <<<'NOW'\n" +
                "select * from |$foo\n" +
                "NOW;\n ?>", "select * from $foo\n");
    }

    /** #156915 - Tests unfinished NOWDOC. */
    public void testNowDoc_002() {
        checkPHPSQLStatement("<?php\n" +
                "$var = <<<'NOW'\n" +
                "select * from |\n" +
                "?>", "select * from \n?>\n");
    }

    // Taking some real-life examples from Google code search
    public void testRealSourceCode_001() {
        checkPHPSQLStatement("<?php echo \"SELECT mid FROM \".$xoopsDB->prefix('modules').\" WHERE dirname='\".$modversion['dirname'].\"' AND |\" ?>",
                " SELECT mid FROM  __UNKNOWN__  WHERE dirname=' __UNKNOWN__ ' AND  ");
    }
    public void testRealSourceCode_002() {
        checkPHPSQLStatement("<?php echo \"SELECT mid FROM \".$xoopsDB->prefix('modules').\" WHERE |dirname='\".$modversion['dirname'].\"' AND \" ?>",
                 " SELECT mid FROM  __UNKNOWN__  WHERE dirname=' __UNKNOWN__ ' AND  ");
    }

    public void testRealSourceCode_003() {
        checkPHPSQLStatement("<?php echo \"SELECT mid FROM \".$xoopsDB->prefix('modules').\" WHERE dirname='\".$mod|version['dirname'].\"' |\"",
                null);
    }

    public void testRealSourceCode_004() {
        checkPHPSQLStatement("<?php echo 'SELECT ' . $this->_params['pass_col'] . ' FROM ' . $this->_params['table'] .\n' WHERE ' . $this->_params['user_col'] . ' = ?|' ?>",
                " SELECT  __UNKNOWN__  FROM  __UNKNOWN__  WHERE  __UNKNOWN__  = ? ");
    }

    public void testRealSourceCode_005() {
        checkPHPSQLStatement("<?php echo 'SELECT ' . $this->_params['pass_col'] . ' FROM |, ' . $this->_params['table'] .\n' WHERE ' . $this->_params['user_col'] . ' = ?' ?>",
                " SELECT  __UNKNOWN__  FROM ,  __UNKNOWN__  WHERE  __UNKNOWN__  = ? ");
    }

    public void testRealSourceCode_006() {
        checkPHPSQLStatement("<?php echo 'SELECT ' . $this->_params['pass_col'] .| ' FROM ' . $this->_params['table'] .\n' WHERE ' . $this->_params['user_col'] . ' = ?' ?>",
                null);
    }

    private void checkPHPSQLStatement(String testString, String resultString) {
        int caretOffset = testString.indexOf('|');
        testString = testString.replace("|", "");

        Document document = getDocument(testString, FileUtils.PHP_MIME_TYPE, PHPTokenId.language());

        PHPSQLStatement stmt = PHPSQLStatement.computeSQLStatement(document, caretOffset);
        if (resultString == null) {
            assertNull(stmt);
            return;
        } else {
            assertNotNull(stmt);
        }

        assertEquals(resultString, stmt.getStatement());

        int virtualOffset = stmt.sourceToGeneratedPos(caretOffset);
        assertFalse(virtualOffset == -1);
        int sourceOffset = stmt.generatedToSourcePos(virtualOffset);
        assertEquals(caretOffset, sourceOffset);
    }

    @Override
    protected String getTestResult(String filename) throws Exception {
        return null;
    }
}
