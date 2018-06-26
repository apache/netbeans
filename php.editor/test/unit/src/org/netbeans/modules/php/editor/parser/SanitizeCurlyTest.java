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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.parser;

import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.parsing.api.Source;
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

    private void execute(String original, String expected, int expectedDelta) throws Exception {
        int originalLength = original.length();
        GSFPHPParser parser = new GSFPHPParser();
        BaseDocument doc = new BaseDocument(true, "text/x-php5");
        doc.insertString(0, original, null);
        Context context = new GSFPHPParser.Context(Source.create(doc).createSnapshot() , -1);
        parser.sanitizeCurly(context);
        assertEquals(expected, context.getSanitizedSource());
        assertEquals(originalLength+expectedDelta, context.getSanitizedSource().length());
    }
}
