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
package org.netbeans.modules.php.editor.typinghooks;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.modules.php.editor.indent.CodeStyle;
import org.netbeans.modules.php.editor.indent.FmtOptions;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PhpTypedBreakInterceptorTest extends PhpTypinghooksTestBase {

    public PhpTypedBreakInterceptorTest(String testName) {
        super(testName);
    }

    @Override
    public void insertBreak(String original, String expected) throws Exception {
        insertBreak(original, expected, new HashMap<String, Object>(FmtOptions.getDefaults()));
    }

    public void insertBreak(String original, String expected, Map<String, Object> options) throws Exception {
        JEditorPane ta = getPane(original);
        Document doc = ta.getDocument();
        setOptionsForDocument(doc, options);
        super.insertBreak(wrapAsPhp(original), wrapAsPhp(expected));
    }

    private void insertBreakMultiLineComment(String original, String expected, boolean insertAsterisk) throws Exception {
        PhpTypedBreakInterceptor.setInsertAsteriskToPHPComment(insertAsterisk);
        try {
            insertBreak(original, expected);
        } finally {
            PhpTypedBreakInterceptor.setInsertAsteriskToPHPComment(null);
        }
    }

    public void testInsertBreakAfterClass2() throws Exception {
        insertBreak("class Foo {^\n    \n}", "class Foo {\n    ^\n    \n}");
    }

    public void testInsertBreakAfterClass() throws Exception {
        insertBreak("class Foo {^", "class Foo {\n    ^\n}");
    }

    public void testInsertBreakAfterFunction_01() throws Exception {
        insertBreak("function foo() {^", "function foo() {\n    ^\n}");
    }

    public void testInsertBreakAfterFunction_02() throws Exception {
        // #259148 there is the "use" before the "function"
        insertBreak(
                "namespace Foo;\n"
                + "\n"
                + "class C {\n"
                + "    const CONSTANT = \"CONSTANT\";\n"
                + "}\n"
                + "\n"
                + "namespace Bar;\n"
                + "\n"
                + "use Foo\\C;\n"
                + "\n"
                + "function test() {^\n"
                + "test();",
                "namespace Foo;\n"
                + "\n"
                + "class C {\n"
                + "    const CONSTANT = \"CONSTANT\";\n"
                + "}\n"
                + "\n"
                + "namespace Bar;\n"
                + "\n"
                + "use Foo\\C;\n"
                + "\n"
                + "function test() {\n"
                + "    ^\n"
                + "}\n"
                + "test();"
        );
    }

    public void testInsertBreakAfterFunction_03() throws Exception {
        // #259148 there is the "use" before the "function"
        insertBreak(
                "namespace Foo;\n"
                + "\n"
                + "class C {\n"
                + "    const CONSTANT = \"CONSTANT\";\n"
                + "}\n"
                + "\n"
                + "namespace Bar;\n"
                + "\n"
                + "use Foo\\C;\n"
                + "\n"
                + "class D {\n"
                + "    public function test() {^\n"
                + "}",
                "namespace Foo;\n"
                + "\n"
                + "class C {\n"
                + "    const CONSTANT = \"CONSTANT\";\n"
                + "}\n"
                + "\n"
                + "namespace Bar;\n"
                + "\n"
                + "use Foo\\C;\n"
                + "\n"
                + "class D {\n"
                + "    public function test() {\n"
                + "        ^\n"
                + "    }\n"
                + "}"
        );
    }

    public void testInsertBreakAfterIf() throws Exception {
        insertBreak("if (1) {^", "if (1) {\n    ^\n}");
    }

    public void testInsertBreakAfterIfElse() throws Exception {
        insertBreak("if (1) {\n    \n} else {^", "if (1) {\n    \n} else {\n    ^\n}");
    }
    public void testInsertBreakAfterWhile() throws Exception {
        insertBreak("while (1) {^", "while (1) {\n    ^\n}");
    }
    public void testInsertBreakAfterCatch() throws Exception {
        insertBreak("try {\n    \n} catch (Exception $exc) {^",
                "try {\n    \n} catch (Exception $exc) {\n    ^\n}");
    }
    public void testInsertBreakAfterTry() throws Exception {
        insertBreak("try {^\n} catch (Exception $ex) {\n}",
                "try {\n    ^\n} catch (Exception $ex) {\n}");
    }
    public void testInsertBreakAfterForEach() throws Exception {
        insertBreak("foreach ($array_variable as $number_variable => $variable) {^",
                "foreach ($array_variable as $number_variable => $variable) {\n    ^\n}");
    }

    public void testInsertBreakInArray1() throws Exception {
        insertBreak("array(^)", "array(\n    ^\n)");
    }

    public void testInsertBreakInArray2() throws Exception {
        insertBreak("array(^\n)", "array(\n    ^\n)");
    }

    public void testInsertBreakInArray3() throws Exception {
        insertBreak("array(\n    'a',^\n)", "array(\n    'a',\n    ^\n)");
    }

    public void testInsertBreakInArray4() throws Exception {
        insertBreak("function a() {\n    array(\n        'a',^\n    )\n}", "function a() {\n    array(\n        'a',\n        ^\n    )\n}");
    }

    public void testInsertBreakInArray5() throws Exception {
        insertBreak("array(array(array(^)))", "array(array(array(\n    ^\n)))");
    }

    public void testInsertBreakInArray6() throws Exception {
        insertBreak("array(array(array(^\n)))", "array(array(array(\n    ^\n)))");
    }

    public void testInsertBreakInSwitch1() throws Exception {
        insertBreak("switch ($a) {\n    case 'a':^\n}", "switch ($a) {\n    case 'a':\n        ^\n}");
    }

    public void testInsertBreakInSwitch2() throws Exception {
        insertBreak("switch ($a) {\n    case 'a':\n        echo 'a';\n        break;^\n}", "switch ($a) {\n    case 'a':\n        echo 'a';\n        break;\n    ^\n}");
    }

    public void testInsertBreakInSwitch3() throws Exception {
        insertBreak("switch ($a) {\n    case 'a':\n        echo 'a';\n        break 1;^\n}", "switch ($a) {\n    case 'a':\n        echo 'a';\n        break 1;\n    ^\n}");
    }

    public void testInsertBreakInSwitch8() throws Exception {
        insertBreak("switch ($a) {\n    case 'a':\n        switch ($b) {\n            case 'b':\n                echo 'b';\n                break;^\n        }\n       \nbreak;^\n}", "switch ($a) {\n    case 'a':\n        switch ($b) {\n            case 'b':\n                echo 'b';\n                break;\n            ^\n        }\n       \nbreak;\n}");
    }

    public void testInsertBreakInFor() throws Exception {
        insertBreak("for (;;) {\n    break;^\n}", "for (;;) {\n    break;\n    ^\n}");
    }

    public void testInsertBreakInForeach() throws Exception {
        insertBreak("foreach ($arr as $val) {\n    break;^\n}", "foreach ($arr as $val) {\n    break;\n    ^\n}");
    }

    public void testInsertBreakInWhile() throws Exception {
        insertBreak("while (true) {\n    break;^\n}", "while (true) {\n    break;\n    ^\n}");
    }

    public void testInsertBreakInDo() throws Exception {
        insertBreak("do {\n    break;^\n} while (true)", "do {\n    break;\n    ^\n} while (true)");
    }


    public void testInsertEnd1() throws Exception {
        insertBreak("x^", "x\n^");
    }

    public void testInsertEnd2() throws Exception {
        insertBreak("class Foo {^", "class Foo {\n    ^\n}");
    }

    public void testInsertEnd3() throws Exception {
        insertBreak("class Foo {^\n}", "class Foo {\n    ^\n}");
    }

    public void testInsertEnd4() throws Exception {
        insertBreak("for(;;) {^", "for(;;) {\n    ^\n}");
    }

    public void testInsertEnd5() throws Exception {
        insertBreak("if ($something) {^", "if ($something) {\n    ^\n}");
    }

    public void testInsertEnd6() throws Exception {
        insertBreak("if ($something) {\n  \n} else {^", "if ($something) {\n  \n} else {\n    ^\n}");
    }

    public void testInsertIf1() throws Exception {
        insertBreak("if ($something)^", "if ($something)\n    ^");
    }

    public void testInsertIf2() throws Exception {
        insertBreak("if ($something)\n  echo 'Hi!';\nelse^", "if ($something)\n  echo 'Hi!';\nelse\n    ^");
    }


    public void testContComment() throws Exception {
        if (PhpTypedBreakInterceptor.CONTINUE_COMMENTS) {
            insertBreak("// ^", "// \n// ^");
        } else {
            insertBreak("// ^", "// \n^");
        }
    }

    public void testContComment4() throws Exception {
        insertBreak("// foo\n^", "// foo\n\n^");
    }

    public void testContComment6() throws Exception {
        insertBreak("   // foo^bar", "   // foo\n   // ^bar");
    }

    public void testContComment7() throws Exception {
        insertBreak("   // foo^\n   // bar", "   // foo\n   // ^\n   // bar");
    }

    public void testContComment9() throws Exception {
        insertBreak("^// foobar", "\n^// foobar");
    }

    public void testContComment11() throws Exception {
        insertBreak("code //foo\n^// foobar", "code //foo\n\n^// foobar");
    }

    public void testContComment15() throws Exception {
        insertBreak("\n\n^// foobar", "\n\n\n^// foobar");
    }

    public void testContComment16() throws Exception {
        insertBreak("\n  \n^// foobar", "\n  \n\n^// foobar");
    }

    public void testNoContComment() throws Exception {
        // No auto-// on new lines
        insertBreak("foo // ^", "foo // \n^");
    }


    public void testIssue223395_01() throws Exception {
        String original = "# first^\n# second";
        String expected = "# first\n# ^\n# second";
        insertBreak(original, expected);
    }

    public void testIssue223395_02() throws Exception {
        String original = "    # first^\n    # second";
        String expected = "    # first\n    # ^\n    # second";
        insertBreak(original, expected);
    }

    public void testStringConcatination_01() throws Exception {
        String original = "$f=^\"lorem ipsum $foo dolor sit amet\";";
        String expected = "$f=\n        ^\"lorem ipsum $foo dolor sit amet\";";
        insertBreak(original, expected);
    }

    public void testStringConcatination_02() throws Exception {
        String original = "$f=\"^lorem ipsum $foo dolor sit amet\";";
        String expected = "$f=\"\"\n        . \"^lorem ipsum $foo dolor sit amet\";";
        insertBreak(original, expected);
    }

    public void testStringConcatination_03() throws Exception {
        String original = "$f=\"lorem ip^sum $foo dolor sit amet\";";
        String expected = "$f=\"lorem ip\"\n        . \"^sum $foo dolor sit amet\";";
        insertBreak(original, expected);
    }

    public void testStringConcatination_04() throws Exception {
        String original = "$f=\"lorem ipsum ^$foo dolor sit amet\";";
        String expected = "$f=\"lorem ipsum \"\n        . \"^$foo dolor sit amet\";";
        insertBreak(original, expected);
    }

    public void testStringConcatination_05() throws Exception {
        String original = "$f=\"lorem ipsum $foo^ dolor sit amet\";";
        String expected = "$f=\"lorem ipsum $foo\"\n        . \"^ dolor sit amet\";";
        insertBreak(original, expected);
    }

    public void testStringConcatination_06() throws Exception {
        String original = "$f=\"lorem ipsum $foo dol^or sit amet\";";
        String expected = "$f=\"lorem ipsum $foo dol\"\n        . \"^or sit amet\";";
        insertBreak(original, expected);
    }

    public void testStringConcatination_07() throws Exception {
        String original = "$f=\"lorem ipsum $foo dolor sit amet^\";";
        String expected = "$f=\"lorem ipsum $foo dolor sit amet\"\n        . \"^\";";
        insertBreak(original, expected);
    }

    public void testStringConcatination_08() throws Exception {
        String original = "$f=\"lorem ipsum $foo dolor sit amet\"^;";
        String expected = "$f=\"lorem ipsum $foo dolor sit amet\"\n        ^;";
        insertBreak(original, expected);
    }

    public void testStringConcatination_09() throws Exception {
        String original = "$b=^'lorem iprsum dolor';";
        String expected = "$b=\n        ^'lorem iprsum dolor';";
        insertBreak(original, expected);
    }

    public void testStringConcatination_10() throws Exception {
        String original = "$b='^lorem iprsum dolor';";
        String expected = "$b=''\n        . '^lorem iprsum dolor';";
        insertBreak(original, expected);
    }

    public void testStringConcatination_11() throws Exception {
        String original = "$b='lorem ipr^sum dolor';";
        String expected = "$b='lorem ipr'\n        . '^sum dolor';";
        insertBreak(original, expected);
    }

    public void testStringConcatination_12() throws Exception {
        String original = "$b='lorem iprsum dolor^';";
        String expected = "$b='lorem iprsum dolor'\n        . '^';";
        insertBreak(original, expected);
    }

    public void testStringConcatination_13() throws Exception {
        String original = "$b='lorem iprsum dolor'^;";
        String expected = "$b='lorem iprsum dolor'\n        ^;";
        insertBreak(original, expected);
    }

    public void testStringConcatination_14() throws Exception {
        String original = "$c=^\"PHP version\";";
        String expected = "$c=\n        ^\"PHP version\";";
        insertBreak(original, expected);
    }

    public void testStringConcatination_15() throws Exception {
        String original = "$c=\"^PHP version\";";
        String expected = "$c=\"\"\n        . \"^PHP version\";";
        insertBreak(original, expected);
    }

    public void testStringConcatination_16() throws Exception {
        String original = "$c=\"PHP ver^sion\";";
        String expected = "$c=\"PHP ver\"\n        . \"^sion\";";
        insertBreak(original, expected);
    }

    public void testStringConcatination_17() throws Exception {
        String original = "$c=\"PHP version^\";";
        String expected = "$c=\"PHP version\"\n        . \"^\";";
        insertBreak(original, expected);
    }

    public void testStringConcatination_18() throws Exception {
        String original = "$c=\"PHP version\"^;";
        String expected = "$c=\"PHP version\"\n        ^;";
        insertBreak(original, expected);
    }

    public void testStringConcatination_19() throws Exception {
        String original = "$checks[] = new G_Check(\"PHP version\",^ \"a\");";
        String expected = "$checks[] = new G_Check(\"PHP version\",\n        ^\"a\");";
        insertBreak(original, expected);
    }

    public void testStringConcatination_20() throws Exception {
        String original = "$checks[] = new G_Check(\"PHP version\", ^\"a\");";
        String expected = "$checks[] = new G_Check(\"PHP version\", \n        ^\"a\");";
        insertBreak(original, expected);
    }

    public void testStringConcatination_21() throws Exception {
        String original = "$checks[] = new G_Check(\"PHP version\", \"^a\");";
        String expected = "$checks[] = new G_Check(\"PHP version\", \"\"\n        . \"^a\");";
        insertBreak(original, expected);
    }

    public void testStringConcatination_22() throws Exception {
        String original = "$checks[] = new G_Check(\"PHP version\", \"a^\");";
        String expected = "$checks[] = new G_Check(\"PHP version\", \"a\"\n        . \"^\");";
        insertBreak(original, expected);
    }

    public void testStringConcatination_23() throws Exception {
        String original = "$checks[] = new G_Check(\"PHP version\", \"a\"^);";
        String expected = "$checks[] = new G_Check(\"PHP version\", \"a\"\n        ^);";
        insertBreak(original, expected);
    }

    public void testIssue228860_01() throws Exception {
        String original = "echo <<<EOT\n<div>^</div>\nEOT;\n";
        String expected = "echo <<<EOT\n<div>\n    ^</div>\nEOT;\n";
        insertBreak(original, expected);
    }

    public void testIssue228860_02() throws Exception {
        String original = "echo <<<EOT\n<div></div>\nEOT;\n\n$foo = \"ba^r\";";
        String expected = "echo <<<EOT\n<div></div>\nEOT;\n\n$foo = \"ba\"\n        . \"^r\";";
        insertBreak(original, expected);
    }

    public void testIssue227105() throws Exception {
        String original = "switch(true)\n" +
                "{\n" +
                "    case 1: if(true) break;\n" +
                "    default:^\n" +
                "}";
        String expected = "switch(true)\n" +
                "{\n" +
                "    case 1: if(true) break;\n" +
                "    default:\n" +
                "        ^\n" +
                "}";
        insertBreak(original, expected);
    }

    public void testIssue229960() throws Exception {
        String original = "<?php\n# What is this?^\n#";
        String expected = "<?php\n# What is this?\n# ^\n#";
        insertBreak(original, expected);
    }

    public void testIssue229710() throws Exception {
        String original = "<?php\nfunction functionName($param) {\n    try {^\n}";
        String expected = "<?php\nfunction functionName($param) {\n    try {\n        ^\n    } catch (Exception $ex) {\n\n    }\n}";
        insertBreak(original, expected);
    }


    public void testIssue200729_01() throws Exception {
        insertBreak("function foo() {\n"
                + "    /*^\n"
                + "}",
                "function foo() {\n"
                + "    /*\n"
                + "     * ^\n"
                + "     */\n"
                + "}");
    }

    public void testIssue200729_02() throws Exception {
        insertBreak("function foo() {\n"
                + "    /**^\n"
                + "}",
                "function foo() {\n"
                + "    /**\n"
                + "     * ^\n"
                + "     */\n"
                + "}");
    }

    public void testIssue200729_03() throws Exception {
        insertBreak("function foo() {\n"
                + "    /**\n"
                + "     * ^\n"
                + "}",
                "function foo() {\n"
                + "    /**\n"
                + "     * \n"
                + "     * ^\n"
                + "     */\n"
                + "}");
    }

    public void testIssue200729_04() throws Exception {
        insertBreak("function foo() {\n"
                + "    /*\n"
                + "     * ^\n"
                + "}",
                "function foo() {\n"
                + "    /*\n"
                + "     * \n"
                + "     * ^\n"
                + "     */\n"
                + "}");
    }

    public void testIssue202644() throws Exception {
        insertBreak("function foo($bar) {^\n    echo($bar);\n}\n\nfunction bar($foo) {",
                "function foo($bar) {\n    ^\n    echo($bar);\n}\n\nfunction bar($foo) {");
    }

    public void testIssue185001() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        insertBreak("while (true)\n    {^", "while (true)\n    {\n    ^\n    }", options);
    }

    public void testIssue174891() throws Exception {
       insertNewline("\n" +
               "^/**\n" +
               "*/",
               "\n" +
               "\n" +
               "^/**\n" +
               "*/", null);
    }

    public void testIssue244259_01() throws Exception {
        String original = "<?php\n$here = <<<HERE\n" +
"<script type='text/javascript' language=\"JavaScript\" src=\"{$CMS_JS_URL}jquery/ui/i18n/ui.datepicker-$lang.js\"></script>\n" +
"<script type='text/javascript' language=\"JavaScript\" src=\"{$CMS_JS_URL}jquery/ui/i18n/ui.timepicker-$lang.js\"></script>\n" +
"^<script type='text/javascript' language=\"JavaScript\" src=\"{$CMS_JS_URL}jquery/jquery.spectrum.js\"></script>\n" +
"<script type='text/javascript' language=\"JavaScript\" src=\"{$CMS_JS_URL}jquery/jquery.spectrum.js\"></script>\n" +
"HERE;";
        String expected = "<?php\n$here = <<<HERE\n" +
"<script type='text/javascript' language=\"JavaScript\" src=\"{$CMS_JS_URL}jquery/ui/i18n/ui.datepicker-$lang.js\"></script>\n" +
"<script type='text/javascript' language=\"JavaScript\" src=\"{$CMS_JS_URL}jquery/ui/i18n/ui.timepicker-$lang.js\"></script>\n" +
"\n" +
"   ^<script type='text/javascript' language=\"JavaScript\" src=\"{$CMS_JS_URL}jquery/jquery.spectrum.js\"></script>\n" +
"<script type='text/javascript' language=\"JavaScript\" src=\"{$CMS_JS_URL}jquery/jquery.spectrum.js\"></script>\n" +
"HERE;";
        insertBreak(original, expected);
    }

    public void testIssue244259_02() throws Exception {
        String original = "<?php\n$here = <<<HERE\n" +
"<script type='text/javascript' language=\"JavaScript\" src=\"{$CMS_JS_URL}jquery/ui/i18n/ui.datepicker-$lang.js\"></script>\n" +
"<script type='text/javascript' language=\"JavaScript\" src=\"{$CMS_JS_URL}jquery/ui/i18n/ui.timepicker-$lang.js\"></script>^\n" +
"<script type='text/javascript' language=\"JavaScript\" src=\"{$CMS_JS_URL}jquery/jquery.spectrum.js\"></script>\n" +
"<script type='text/javascript' language=\"JavaScript\" src=\"{$CMS_JS_URL}jquery/jquery.spectrum.js\"></script>\n" +
"HERE;";
        String expected = "<?php\n$here = <<<HERE\n" +
"<script type='text/javascript' language=\"JavaScript\" src=\"{$CMS_JS_URL}jquery/ui/i18n/ui.datepicker-$lang.js\"></script>\n" +
"<script type='text/javascript' language=\"JavaScript\" src=\"{$CMS_JS_URL}jquery/ui/i18n/ui.timepicker-$lang.js\"></script>\n" +
"        ^\n" +
"<script type='text/javascript' language=\"JavaScript\" src=\"{$CMS_JS_URL}jquery/jquery.spectrum.js\"></script>\n" +
"<script type='text/javascript' language=\"JavaScript\" src=\"{$CMS_JS_URL}jquery/jquery.spectrum.js\"></script>\n" +
"HERE;";
        insertBreak(original, expected);
    }

    public void testIssue244259_03() throws Exception {
        String original = "<?php\n$here = <<<HERE\n" +
"foo {$CMS_JS_URL} bar\n" +
"HERE;\n" +
"$foo =  \"b $baz^ ar\";";
        String expected = "<?php\n$here = <<<HERE\n" +
"foo {$CMS_JS_URL} bar\n" +
"HERE;\n" +
"$foo =  \"b $baz\"\n" +
"        . \"^ ar\";";
        insertBreak(original, expected);
    }

    public void testIssue244259_04() throws Exception {
        String original = "<?php\n$here = <<<HERE\n" +
"foo {$CMS_JS_URL} bar\n" +
"HERE;\n" +
"$foo =  \"b $baz ^ar\";";
        String expected = "<?php\n$here = <<<HERE\n" +
"foo {$CMS_JS_URL} bar\n" +
"HERE;\n" +
"$foo =  \"b $baz \"\n" +
"        . \"^ar\";";
        insertBreak(original, expected);
    }

    public void testIssue244259_05() throws Exception {
        String original = "<?php\n$here = <<<HERE\n" +
"foo {$CMS_JS_URL} bar\n" +
"HERE;\n" +
"$foo = \"fdsf aks^dhzf lkasjdh\";";
        String expected = "<?php\n$here = <<<HERE\n" +
"foo {$CMS_JS_URL} bar\n" +
"HERE;\n" +
"$foo = \"fdsf aks\"\n" +
"        . \"^dhzf lkasjdh\";";
        insertBreak(original, expected);
    }

    public void testIssue246078() throws Exception {
        String original = "<?php\n\"$foo\"^";
        String expected = "<?php\n\"$foo\"\n        ^";
        insertBreak(original, expected);
    }

    // #259148
    public void testInsertBreakAfterGroupUse_01() throws Exception {
        insertBreak(
                "use Foo\\{^",
                "use Foo\\{\n"
                + "    ^\n"
                + "};"
        );
    }

    public void testInsertBreakAfterGroupUse_02() throws Exception {
        insertBreak(
                "use Foo\\ {^",
                "use Foo\\ {\n"
                + "    ^\n"
                + "};"
        );
    }

    public void testInsertBreakAfterGroupUse_03() throws Exception {
        insertBreak(
                "use Foo\\ // comment \n"
                + "{^",
                "use Foo\\ // comment \n"
                + "{\n"
                + "    ^\n"
                + "};"
        );
    }

    public void testInsertBreakAfterGroupUse_04() throws Exception {
        insertBreak(
                "use Foo\\ /* comment */{^",
                "use Foo\\ /* comment */{\n"
                + "    ^\n"
                + "};"
        );
    }

    public void testInsertBreakAfterGroupUse_05() throws Exception {
        insertBreak(
                "use function Foo\\{^",
                "use function Foo\\{\n"
                + "    ^\n"
                + "};"
        );
    }

    public void testInsertBreakAfterGroupUse_06() throws Exception {
        insertBreak(
                "use const Foo\\{^",
                "use const Foo\\{\n"
                + "    ^\n"
                + "};"
        );
    }

    public void testInsertBreakAfterGroupUse_07() throws Exception {
        // there is a "use function" before a "use" keyword
        insertBreak(
                "use function Foo\\Bar;\n"
                + "use Baz\\{^",
                "use function Foo\\Bar;\n"
                + "use Baz\\{\n"
                + "    ^\n"
                + "};"
        );
    }

    public void testInsertBreakAfterGroupUse_08() throws Exception {
        insertBreak(
                "use Foo\\{^ // comment",
                "use Foo\\{\n"
                + "    ^// comment\n"
                + "};"
        );
    }

    public void testInsertBreakAfterUseTrait() throws Exception {
        insertBreak(
                "class C {\n"
                + "    use MyTrait {^\n"
                + "}",
                "class C {\n"
                + "    use MyTrait {\n"
                + "        ^\n"
                + "    }\n"
                + "}"
        );
    }

    public void testInsertBreakAfterClosureWithUse() throws Exception {
        insertBreak(
                "function test() {\n"
                + "    $test = 'test';\n"
                + "    $closure = function() use ($test){^\n"
                + "}",
                "function test() {\n"
                + "    $test = 'test';\n"
                + "    $closure = function() use ($test){\n"
                + "        ^\n"
                + "    }\n"
                + "}"
        );
    }

    public void testIssue270233_01() throws Exception {
        insertBreak(
                "class Foo {\n"
                + "    public function test1(): ?string {^\n"
                + "}",
                "class Foo {\n"
                + "    public function test1(): ?string {\n"
                + "        ^\n"
                + "    }\n"
                + "}"
        );
    }

    public void testIssue270233_02() throws Exception {
        insertBreak(
                "class Foo {\n"
                + "    public function test2(?string $string) {^\n"
                + "}",
                "class Foo {\n"
                + "    public function test2(?string $string) {\n"
                + "        ^\n"
                + "    }\n"
                + "}"
        );
    }

    public void testIssue270233_03() throws Exception {
        insertBreak(
                "class Foo {\n"
                + "    public function test3(?string $string): ?string {^\n"
                + "}",
                "class Foo {\n"
                + "    public function test3(?string $string): ?string {\n"
                + "        ^\n"
                + "    }\n"
                + "}"
        );
    }

    public void testIssue270233_04() throws Exception {
        insertBreak(
                "$anon = function(?string $string) {^",
                "$anon = function(?string $string) {\n"
                + "    ^\n"
                + "}"
        );
    }

    public void testIssue270233_05() throws Exception {
        insertBreak(
                "$anon = function(): ?string {^",
                "$anon = function(): ?string {\n"
                + "    ^\n"
                + "}"
        );
    }

    public void testIssue270233_06() throws Exception {
        insertBreak(
                "$anon = function(?string $string): ?string {^",
                "$anon = function(?string $string): ?string {\n"
                + "    ^\n"
                + "}"
        );
    }

    // #230814
    public void testDoNotInsertCommentAsterisk_01() throws Exception {
        insertBreakMultiLineComment(
                "/*^",
                "/*\n"
                + "^\n"
                + "*/",
                false
        );
    }

    public void testDoNotInsertCommentAsterisk_02() throws Exception {
        insertBreakMultiLineComment(
                "/*\n"
                + "^\n"
                + "*/",
                "/*\n"
                + "\n"
                + "^\n"
                + "*/",
                false
        );
    }

    public void testDoNotInsertCommentAsterisk_03() throws Exception {
        insertBreakMultiLineComment(
                "/*\n"
                + " * ^\n"
                + "*/",
                "/*\n"
                + " * \n"
                + "^\n"
                + "*/",
                false
        );
    }

    public void testDoNotInsertCommentAsterisk_04() throws Exception {
        insertBreakMultiLineComment(
                "/*\n"
                + "something^\n"
                + "*/",
                "/*\n"
                + "something\n"
                + "^\n"
                + "*/",
                false
        );
    }

    public void testDoNotInsertCommentAsterisk_05() throws Exception {
        insertBreakMultiLineComment(
                "class MyClass {\n"
                + "\n"
                + "    /*^\n"
                + "    public function test() {\n"
                + "        \n"
                + "    }\n"
                + "}\n",
                "class MyClass {\n"
                + "\n"
                + "    /*\n"
                + "    ^\n"
                + "    */\n"
                + "    public function test() {\n"
                + "        \n"
                + "    }\n"
                + "}\n",
                false
        );
    }

    public void testDoNotInsertCommentAsterisk_06() throws Exception {
        insertBreakMultiLineComment(
                "class MyClass {\n"
                + "\n"
                + "    /*^\n"
                + "    */\n"
                + "    public function test() {\n"
                + "        \n"
                + "    }\n"
                + "}\n",
                "class MyClass {\n"
                + "\n"
                + "    /*\n"
                + "    ^\n"
                + "    */\n"
                + "    public function test() {\n"
                + "        \n"
                + "    }\n"
                + "}\n",
                false
        );
    }

    public void testDoNotInsertCommentAsterisk_07() throws Exception {
        insertBreakMultiLineComment(
                "class MyClass {\n"
                + "\n"
                + "    /*\n"
                + "    ^\n"
                + "    */\n"
                + "    public function test() {\n"
                + "        \n"
                + "    }\n"
                + "}\n",
                "class MyClass {\n"
                + "\n"
                + "    /*\n"
                + "    \n"
                + "    ^\n"
                + "    */\n"
                + "    public function test() {\n"
                + "        \n"
                + "    }\n"
                + "}\n",
                false
        );
    }

    public void testDoNotInsertCommentAsterisk_08() throws Exception {
        insertBreakMultiLineComment(
                "class MyClass {\n"
                + "\n"
                + "    /*\n"
                + "    something^\n"
                + "    */\n"
                + "    public function test() {\n"
                + "        \n"
                + "    }\n"
                + "}\n",
                "class MyClass {\n"
                + "\n"
                + "    /*\n"
                + "    something\n"
                + "    ^\n"
                + "    */\n"
                + "    public function test() {\n"
                + "        \n"
                + "    }\n"
                + "}\n",
                false
        );
    }

    public void testDoNotInsertCommentAsterisk_09() throws Exception {
        // PHPDoc
        insertBreakMultiLineComment(
                "class MyClass {\n"
                + "\n"
                + "    /**^\n"
                + "     */\n"
                + "    public function test() {\n"
                + "        \n"
                + "    }\n"
                + "}\n",
                "class MyClass {\n"
                + "\n"
                + "    /**\n"
                + "     * ^\n"
                + "     */\n"
                + "    public function test() {\n"
                + "        \n"
                + "    }\n"
                + "}\n",
                false
        );
    }

    public void testInsertCommentAsterisk_01() throws Exception {
        insertBreakMultiLineComment(
                "/*^",
                "/*\n"
                + " * ^\n"
                + " */",
                true
        );
    }

    public void testInsertCommentAsterisk_02() throws Exception {
        insertBreakMultiLineComment(
                "/*\n"
                + " * ^\n"
                + " */",
                "/*\n"
                + " * \n"
                + " * ^\n"
                + " */",
                true
        );
    }

    public void testInsertCommentAsterisk_03() throws Exception {
        insertBreakMultiLineComment(
                "/*\n"
                + " * something^\n"
                + " */",
                "/*\n"
                + " * something\n"
                + " * ^\n"
                + " */",
                true
        );
    }

    public void testInsertCommentAsterisk_04() throws Exception {
        insertBreakMultiLineComment(
                "class MyClass {\n"
                + "\n"
                + "    /*^\n"
                + "    public function test() {\n"
                + "        \n"
                + "    }\n"
                + "}\n",
                "class MyClass {\n"
                + "\n"
                + "    /*\n"
                + "     * ^\n"
                + "     */\n"
                + "    public function test() {\n"
                + "        \n"
                + "    }\n"
                + "}\n",
                true
        );
    }

    public void testInsertCommentAsterisk_05() throws Exception {
        insertBreakMultiLineComment(
                "class MyClass {\n"
                + "\n"
                + "    /*^\n"
                + "     */\n"
                + "    public function test() {\n"
                + "        \n"
                + "    }\n"
                + "}\n",
                "class MyClass {\n"
                + "\n"
                + "    /*\n"
                + "     * ^\n"
                + "     */\n"
                + "    public function test() {\n"
                + "        \n"
                + "    }\n"
                + "}\n",
                true
        );
    }

    public void testInsertCommentAsterisk_06() throws Exception {
        insertBreakMultiLineComment(
                "class MyClass {\n"
                + "\n"
                + "    /*\n"
                + "     * ^\n"
                + "     */\n"
                + "    public function test() {\n"
                + "        \n"
                + "    }\n"
                + "}\n",
                "class MyClass {\n"
                + "\n"
                + "    /*\n"
                + "     * \n"
                + "     * ^\n"
                + "     */\n"
                + "    public function test() {\n"
                + "        \n"
                + "    }\n"
                + "}\n",
                true
        );
    }

    public void testInsertCommentAsterisk_07() throws Exception {
        insertBreakMultiLineComment(
                "class MyClass {\n"
                + "\n"
                + "    /*\n"
                + "     * something^\n"
                + "     */\n"
                + "    public function test() {\n"
                + "        \n"
                + "    }\n"
                + "}\n",
                "class MyClass {\n"
                + "\n"
                + "    /*\n"
                + "     * something\n"
                + "     * ^\n"
                + "     */\n"
                + "    public function test() {\n"
                + "        \n"
                + "    }\n"
                + "}\n",
                true
        );
    }

}
