/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.php.editor.parser;

import java.io.StringReader;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.editor.parser.GSFPHPParser.Context;

/**
 *
 * @author Petr Pisl
 */
public class SanitizeCurlyTest extends PHPTestBase {

    public SanitizeCurlyTest(String testName) {
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

    public void testCurly01() throws Exception {
        String orig = "<?\n" +
                "function h () {";
        String expected = "<?\n" +
                "function h () {}";
        execute(orig, expected, 1);
    }

    public void testCurly02() throws Exception {
        String orig = "<?\n" +
                "function h () {" +
                "   function g () {";
        String expected = "<?\n" +
                "function h () {" +
                "   function g () {}}";
        execute(orig, expected, 2);
    }

    public void testCurly03() throws Exception {
        String orig = "<?\n" +
                "class A {" +
                "   function g () {";
        String expected = "<?\n" +
                "class A {" +
                "   function g () {}}";
        execute(orig, expected, 2);
    }

    public void testCurly04() throws Exception {
        String orig = "<?\n" +
                "class test {\n" +
                "    private $name;\n" +
                "    function ahoj() {\n" +
                "\n" +
                "    }\n" +
                "    public function neco () {";
        String expected = "<?\n" +
                "class test {\n" +
                "    private $name;\n" +
                "    function ahoj() {\n" +
                "\n" +
                "    }\n" +
                "    public function neco () {}}";
        execute(orig, expected, 2);
    }

    public void testCurly05() throws Exception {
        String orig = "<?\n" +
                "class test {\n" +
                "    private $name;\n" +
                "    function ahoj() {\n" +
                "\n" +
                "    public function neco () {";
        String expected = "<?\n" +
                "class test {\n" +
                "    private $name;\n" +
                "    function ahoj() {\n" +
                "\n" +
                "    public function neco () {}}}";
        execute(orig, expected, 3);
    }

    public void testCurly06() throws Exception {
        String orig = "<?\n" +
                "\n" +
                "class prd {\n" +
                "    function __construct() {\n" +
                "        ;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "class test {\n" +
                "    private $name;\n" +
                "    function ahoj() {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "class hello {\n" +
                "    function __construct() {\n" +
                "\n" +
                "    }\n" +
                "}";
        String expected = "<?\n" +
                "\n" +
                "class prd {\n" +
                "    function __construct() {\n" +
                "        ;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "class test {\n" +
                "    private $name;\n" +
                "    function ahoj() {\n" +
                "\n" +
                "    }\n" +
                "}class hello {\n" +
                "    function __construct() {\n" +
                "\n" +
                "    }\n" +
                "}";
        execute(orig, expected, 0);
    }

    public void testCurly07() throws Exception {
        String orig = "<?\n" +
                "\n" +
                "class prd {\n" +
                "    function __construct() {\n" +
                "        ;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "class test {\n" +
                "    private $name;\n" +
                "    function ahoj() {\n" +
                "\n" +
                "    }\n" +
                "    public function neco () {\n" +
                "\n" +
                "class hello {\n" +
                "    function __construct() {\n" +
                "\n" +
                "    }\n" +
                "}";
        String expected = "<?\n" +
                "\n" +
                "class prd {\n" +
                "    function __construct() {\n" +
                "        ;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "class test {\n" +
                "    private $name;\n" +
                "    function ahoj() {\n" +
                "\n" +
                "    }\n" +
                "    public function neco () {" +
                "}}class hello {\n" +
                "    function __construct() {\n" +
                "\n" +
                "    }\n" +
                "}";
        execute(orig, expected, 0);
    }

    public void testCurly08() throws Exception {
        String orig = "<?\n" +
                "\n" +
                "class prd {\n" +
                "    function __construct() {\n" +
                "        ;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "class test {\n" +
                "    private $name;\n" +
                "    function ahoj() {\n" +
                "\n" +
                "    }\n" +
                "    public function neco (){\n" +
                "class hello {\n" +
                "    function __construct() {\n" +
                "\n" +
                "    }\n" +
                "}";
        String expected = "<?\n" +
                "\n" +
                "class prd {\n" +
                "    function __construct() {\n" +
                "        ;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "class test {\n" +
                "    private $name;\n" +
                "    function ahoj() {\n" +
                "\n" +
                "    }}" +
                "    public function neco (){}" +
                "class hello {\n" +
                "    function __construct() {\n" +
                "\n" +
                "    }\n" +
                "}";
        execute(orig, expected, 0);
    }

    public void testCurlyBalance01() throws Exception {
        String source = ""
                + "<?php\n"
                + "class Example {\n"
                + "    public function run(int $param1) : void {\n"
                + "        $example = new class() {};\n"
                + "        \"'single ${$complex->quote()}'\";\n"
                + "    }\n"
                + "}";
        checkCurlyBalance(source, 0);
    }

    private void execute(String original, String expected, int expectedDelta) throws Exception {
        int originalLength = original.length();
        GSFPHPParser parser = new GSFPHPParser();
        BaseDocument doc = new BaseDocument(true, FileUtils.PHP_MIME_TYPE);
        doc.insertString(0, original, null);
        Context context = new GSFPHPParser.Context(Source.create(doc).createSnapshot() , -1);
        parser.sanitizeCurly(context);
        assertEquals(expected, context.getSanitizedSource());
        assertEquals(originalLength+expectedDelta, context.getSanitizedSource().length());
    }

    private void checkCurlyBalance(String source, int expectedBalance) throws Exception {
        ASTPHP5Scanner scanner = new ASTPHP5Scanner(new StringReader(source), false, false);
        assertEquals(expectedBalance, scanner.getCurlyBalance());
    }
}
