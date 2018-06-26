/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.parser;

import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.editor.parser.GSFPHPParser.Context;

/**
 *
 * @author Ondřej Brejla <ondrej@brejla.cz>
 */
public class SanitizeRequireTest extends PHPTestBase {

    private static final String PHP_OPEN_DELIMITER = "<?php\n";

    public SanitizeRequireTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testRequireOnceWithUglyWhitespaces() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "require_once  \t    \n      \"\" ";
        String expected = PHP_OPEN_DELIMITER +
                "require_once  \t    \n      \"\";";

        execute(original, expected);
    }

    public void testRequireWithSpaceAndTwoQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "require \"\" ";
        String expected = PHP_OPEN_DELIMITER +
                "require \"\";";

        execute(original, expected);
    }

    public void testRequireWithSpaceAndOneQuote() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "require \"  ";
        String expected = PHP_OPEN_DELIMITER +
                "require \"\";";

        execute(original, expected);
    }

    public void testRequireWithSpaceAndOpenCloseParentheseAndTwoQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "require (\"\") ";
        String expected = PHP_OPEN_DELIMITER +
                "require (\"\");";

        execute(original, expected);
    }

    public void testRequireWithSpaceAndOpenParentheseAndTwoQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "require (\"\"  ";
        String expected = PHP_OPEN_DELIMITER +
                "require (\"\");";

        execute(original, expected);
    }

    public void testRequireWithSpaceAndOpenParentheseAndOneQuote() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "require (\"   ";
        String expected = PHP_OPEN_DELIMITER +
                "require (\"\");";

        execute(original, expected);
    }

    public void testRequireWithSpaceAndTwoSingleQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "require '' ";
        String expected = PHP_OPEN_DELIMITER +
                "require '';";

        execute(original, expected);
    }

    public void testRequireWithSpaceAndOneSingleQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "require '  ";
        String expected = PHP_OPEN_DELIMITER +
                "require '';";

        execute(original, expected);
    }

    public void testRequireWithSpaceAndOpenCloseParentheseAndTwoSingleQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "require ('') ";
        String expected = PHP_OPEN_DELIMITER +
                "require ('');";

        execute(original, expected);
    }

    public void testRequireWithSpaceAndOpenParentheseAndTwoSingleQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "require (''  ";
        String expected = PHP_OPEN_DELIMITER +
                "require ('');";

        execute(original, expected);
    }

    public void testRequireWithSpaceAndOpenParentheseAndOneSingleQuote() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "require ('   ";
        String expected = PHP_OPEN_DELIMITER +
                "require ('');";

        execute(original, expected);
    }

    public void testRequireWithTwoQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "require\"\" ";
        String expected = PHP_OPEN_DELIMITER +
                "require\"\";";

        execute(original, expected);
    }

    public void testRequireWithOneQuote() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "require\"  ";
        String expected = PHP_OPEN_DELIMITER +
                "require\"\";";

        execute(original, expected);
    }

    public void testRequireWithOpenCloseParentheseAndTwoQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "require(\"\") ";
        String expected = PHP_OPEN_DELIMITER +
                "require(\"\");";

        execute(original, expected);
    }

    public void testRequireWithOpenParentheseAndTwoQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "require(\"\"  ";
        String expected = PHP_OPEN_DELIMITER +
                "require(\"\");";

        execute(original, expected);
    }

    public void testRequireWithOpenParentheseAndOneQuote() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "require(\"   ";
        String expected = PHP_OPEN_DELIMITER +
                "require(\"\");";

        execute(original, expected);
    }

    public void testRequireWithTwoSingleQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "require'' ";
        String expected = PHP_OPEN_DELIMITER +
                "require'';";

        execute(original, expected);
    }

    public void testRequireWithOneSingleQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "require'  ";
        String expected = PHP_OPEN_DELIMITER +
                "require'';";

        execute(original, expected);
    }

    public void testRequireWithOpenCloseParentheseAndTwoSingleQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "require('') ";
        String expected = PHP_OPEN_DELIMITER +
                "require('');";

        execute(original, expected);
    }

    public void testRequireWithOpenParentheseAndTwoSingleQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "require(''  ";
        String expected = PHP_OPEN_DELIMITER +
                "require('');";

        execute(original, expected);
    }

    public void testRequireWithOpenParentheseAndOneSingleQuote() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "require('   ";
        String expected = PHP_OPEN_DELIMITER +
                "require('');";

        execute(original, expected);
    }

    public void testIncludeOnceWithUglyWhitespaces() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "include_once\t    \n      \"\" ";
        String expected = PHP_OPEN_DELIMITER +
                "include_once\t    \n      \"\";";

        execute(original, expected);
    }

    public void testIncludeWithSpaceAndTwoQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "include \"\" ";
        String expected = PHP_OPEN_DELIMITER +
                "include \"\";";

        execute(original, expected);
    }

    public void testIncludeWithSpaceAndOneQuote() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "include \"  ";
        String expected = PHP_OPEN_DELIMITER +
                "include \"\";";

        execute(original, expected);
    }

    public void testIncludeWithSpaceAndOpenCloseParentheseAndTwoQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "include (\"\") ";
        String expected = PHP_OPEN_DELIMITER +
                "include (\"\");";

        execute(original, expected);
    }

    public void testIncludeWithSpaceAndOpenParentheseAndTwoQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "include (\"\"  ";
        String expected = PHP_OPEN_DELIMITER +
                "include (\"\");";

        execute(original, expected);
    }

    public void testIncludeWithSpaceAndOpenParentheseAndOneQuote() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "include (\"   ";
        String expected = PHP_OPEN_DELIMITER +
                "include (\"\");";

        execute(original, expected);
    }

    public void testIncludeWithSpaceAndTwoSingleQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "include '' ";
        String expected = PHP_OPEN_DELIMITER +
                "include '';";

        execute(original, expected);
    }

    public void testIncludeWithSpaceAndOneSingleQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "include '  ";
        String expected = PHP_OPEN_DELIMITER +
                "include '';";

        execute(original, expected);
    }

    public void testIncludeWithSpaceAndOpenCloseParentheseAndTwoSingleQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "include ('') ";
        String expected = PHP_OPEN_DELIMITER +
                "include ('');";

        execute(original, expected);
    }

    public void testIncludeWithSpaceAndOpenParentheseAndTwoSingleQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "include (''  ";
        String expected = PHP_OPEN_DELIMITER +
                "include ('');";

        execute(original, expected);
    }

    public void testIncludeWithSpaceAndOpenParentheseAndOneSingleQuote() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "include ('   ";
        String expected = PHP_OPEN_DELIMITER +
                "include ('');";

        execute(original, expected);
    }

    public void testIncludeWithTwoQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "include\"\" ";
        String expected = PHP_OPEN_DELIMITER +
                "include\"\";";

        execute(original, expected);
    }

    public void testIncludeWithOneQuote() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "include\"  ";
        String expected = PHP_OPEN_DELIMITER +
                "include\"\";";

        execute(original, expected);
    }

    public void testIncludeWithOpenCloseParentheseAndTwoQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "include(\"\") ";
        String expected = PHP_OPEN_DELIMITER +
                "include(\"\");";

        execute(original, expected);
    }

    public void testIncludeWithOpenParentheseAndTwoQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "include(\"\"  ";
        String expected = PHP_OPEN_DELIMITER +
                "include(\"\");";

        execute(original, expected);
    }

    public void testIncludeWithOpenParentheseAndOneQuote() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "include(\"   ";
        String expected = PHP_OPEN_DELIMITER +
                "include(\"\");";

        execute(original, expected);
    }

    public void testIncludeWithTwoSingleQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "include'' ";
        String expected = PHP_OPEN_DELIMITER +
                "include'';";

        execute(original, expected);
    }

    public void testIncludeWithOneSingleQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "include'  ";
        String expected = PHP_OPEN_DELIMITER +
                "include'';";

        execute(original, expected);
    }

    public void testIncludeWithOpenCloseParentheseAndTwoSingleQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "include('') ";
        String expected = PHP_OPEN_DELIMITER +
                "include('');";

        execute(original, expected);
    }

    public void testIncludeWithOpenParentheseAndTwoSingleQuotes() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "include(''  ";
        String expected = PHP_OPEN_DELIMITER +
                "include('');";

        execute(original, expected);
    }

    public void testIncludeWithOpenParentheseAndOneSingleQuote() throws Exception {
        String original = PHP_OPEN_DELIMITER +
                "include('   ";
        String expected = PHP_OPEN_DELIMITER +
                "include('');";

        execute(original, expected);
    }

    private void execute(String original, String expected) throws Exception {
        GSFPHPParser parser = new GSFPHPParser();
        BaseDocument doc = new BaseDocument(true, "text/x-php5");
        doc.insertString(0, original, null);
        Context context = new GSFPHPParser.Context(Source.create(doc).createSnapshot(), -1);

        parser.sanitizeRequireAndInclude(context, PHP_OPEN_DELIMITER.length(), original.length());

        assertEquals(expected, context.getSanitizedSource());
    }

}
