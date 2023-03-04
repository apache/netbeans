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
