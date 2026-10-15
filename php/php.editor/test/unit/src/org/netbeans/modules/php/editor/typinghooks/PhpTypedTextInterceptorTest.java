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
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.php.editor.indent.CodeStyle;
import org.netbeans.modules.php.editor.indent.FmtOptions;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PhpTypedTextInterceptorTest extends PhpTypinghooksTestBase {

    public PhpTypedTextInterceptorTest(String testName) {
        super(testName);
    }


    private void insertChar(String original, char insertText, String expected) throws Exception {
        insertChar(original, insertText, expected, null);
    }

    private void insertChar(String original, char insertText, String expected, String selection) throws Exception {
        insertChar(original, insertText, expected, selection, false);
    }

    @Override
    protected void insertChar(String original, char insertText, String expected, String selection, boolean codeTemplateMode) throws Exception {
        original = wrapAsPhp(original);
        expected = wrapAsPhp(expected);
        super.insertChar(original, insertText, expected, selection, codeTemplateMode);
    }

    protected void insertChar(String original, char insertText, String expected, String selection, boolean codeTemplateMode, Map<String, Object> formatPrefs) throws Exception {
        String source = wrapAsPhp(original);
        String reformatted = wrapAsPhp(expected);
        Formatter formatter = getFormatter(null);

        int sourcePos = source.indexOf('^');
        assertNotNull(sourcePos);
        source = source.substring(0, sourcePos) + source.substring(sourcePos+1);

        int reformattedPos = reformatted.indexOf('^');
        assertNotNull(reformattedPos);
        reformatted = reformatted.substring(0, reformattedPos) + reformatted.substring(reformattedPos+1);

        JEditorPane ta = getPane(source);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePos);
        if (selection != null) {
            int start = original.indexOf(selection);
            assertTrue(start != -1);
            assertTrue("Ambiguous selection - multiple occurrences of selection string",
                    original.indexOf(selection, start+1) == -1);
            ta.setSelectionStart(start);
            ta.setSelectionEnd(start+selection.length());
            assertEquals(selection, ta.getSelectedText());
        }

        BaseDocument doc = (BaseDocument) ta.getDocument();

        if (codeTemplateMode) {
            // Copied from editor/codetemplates/src/org/netbeans/lib/editor/codetemplates/CodeTemplateInsertHandler.java
            String EDITING_TEMPLATE_DOC_PROPERTY = "processing-code-template"; // NOI18N
            doc.putProperty(EDITING_TEMPLATE_DOC_PROPERTY, Boolean.TRUE);
        }

        if (formatter != null) {
            configureIndenters(doc, formatter, true);
        }

        setupDocumentIndentation(doc, null);

        if (formatter != null && formatPrefs != null) {
            setOptionsForDocument(doc, formatPrefs);
        }
        runKitAction(ta, DefaultEditorKit.defaultKeyTypedAction, ""+insertText);

        String formatted = doc.getText(0, doc.getLength());
        assertEquals(reformatted, formatted);

        if (reformattedPos != -1) {
            assertEquals(reformattedPos, caret.getDot());
        }
    }


    public void testInsertX() throws Exception {
        insertChar("c^ass", 'l', "cl^ass");
    }

    public void testInsertX2() throws Exception {
        insertChar("clas^", 's', "class^");
    }

    public void testNoMatchInComments() throws Exception {
        insertChar("// Hello^", '"', "// Hello\"^");
        insertChar("// Hello^", '\'', "// Hello'^");
        insertChar("// Hello^", '[', "// Hello[^");
        insertChar("// Hello^", '(', "// Hello(^");
    }

    public void testNoMatchInStrings() throws Exception {
        insertChar("x = \"^\"", '\'', "x = \"'^\"");
        insertChar("x = \"^\"", '[', "x = \"[^\"");
        insertChar("x = \"^\"", '(', "x = \"(^\"");
        insertChar("x = \"^)\"", ')', "x = \")^)\"");
        insertChar("x = '^'", '"', "x = '\"^'");
        insertChar("x = \"\nf^\n\"", '\'', "x = \"\nf'^\n\"");
        insertChar("x = \"\nf^\n\"", '[', "x = \"\nf[^\n\"");
        insertChar("x = \"\nf^\n\"", '(', "x = \"\nf(^\n\"");
        insertChar("x = '\nf^\n'", '"', "x = '\nf\"^\n'");
    }

    public void testSingleQuotes1() throws Exception {
        insertChar("x = ^", '\'', "x = '^'");
    }

    public void testSingleQuotes2() throws Exception {
        insertChar("x = '^'", '\'', "x = ''^");
    }

    public void testSingleQuotes3() throws Exception {
        insertChar("x = '^'", 'a', "x = 'a^'");
    }

    public void testSingleQuotes4() throws Exception {
        insertChar("x = '\\^'", '\'', "x = '\\'^'");
    }

    public void testSingleQuotes5() throws Exception {
        insertChar("x = '\\'^", '\'', "x = '\\''^");
    }

    public void testIssue209867_01() throws Exception {
        insertChar("$x = 'this is (long'^) string';", '\'', "$x = 'this is (long''^) string';");
    }

    public void testIssue209867_02() throws Exception {
        insertChar("$x = 'this is long'^ string';", '\'', "$x = 'this is long''^ string';");
    }

    public void testIssue209867_03() throws Exception {
        insertChar("$x = 'this is long^ string';", '\'', "$x = 'this is long'^ string';");
    }

    public void testIssue209867_04() throws Exception {
        insertChar("if ($x == ^) {}", '\'', "if ($x == '^') {}");
    }

    public void testIssue209867_05() throws Exception {
        insertChar("if ($x == '^) {}", '\'', "if ($x == ''^) {}");
    }

    public void testIssue209867_06() throws Exception {
        insertChar("$x = 'this is long string' . $foo . ^;", '\'', "$x = 'this is long string' . $foo . '^';");
    }

    public void testIssue209867_07() throws Exception {
        insertChar("$x = 'this is long string'^;", '\'', "$x = 'this is long string''^;");
    }

    public void testDoubleQuotes1() throws Exception {
        insertChar("x = ^", '"', "x = \"^\"");
    }

    public void testDoubleQuotes2() throws Exception {
        insertChar("x = \"^\"", '"', "x = \"\"^");
    }

    public void testDoubleQuotes3() throws Exception {
        insertChar("x = \"^\"", 'a', "x = \"a^\"");
    }

    public void testDobuleQuotes4() throws Exception {
        insertChar("x = \"\\^\"", '"', "x = \"\\\"^\"");
    }

    public void testDobuleQuotes5() throws Exception {
        insertChar("x = \"\\\"^", '"', "x = \"\\\"\"^");
    }

    public void testIssue153062() throws Exception {
        insertChar("//line comment\n^", '"', "//line comment\n\"^\"");
    }

    public void testIssue153062_2() throws Exception {
        insertChar("//line comment^", '"', "//line comment\"^");
    }

    public void testIssue162139() throws Exception {
        insertChar("^\\", '"', "\"^\\");
    }

    public void testBrackets1() throws Exception {
        insertChar("x = ^", '[', "x = [^]");
    }

    public void testBrackets2() throws Exception {
        insertChar("x = [^]", ']', "x = []^");
    }

    public void testBracketsSpecialName() throws Exception {
        // "[]" and "[]=" are valid method names!
        insertChar("def ^", '[', "def [^]");
    }

    public void testBracketsSpecialName2() throws Exception {
        // "[]" and "[]=" are valid method names!
        insertChar("def [^]", ']', "def []^");
    }

    public void testBrackets3() throws Exception {
        insertChar("x = [^]", 'a', "x = [a^]");
    }

    public void testBrackets4() throws Exception {
        insertChar("x = [^]", '[', "x = [[^]]");
    }

    public void testBrackets5() throws Exception {
        insertChar("x = [[^]]", ']', "x = [[]^]");
    }

    public void testBrackets6() throws Exception {
        insertChar("x = [[]^]", ']', "x = [[]]^");
    }

    public void testAttributeSyntaxBrackets_01() throws Exception {
        insertChar("#^", '[', "#[^]");
    }

    public void testAttributeSyntaxBrackets_02() throws Exception {
        insertChar("#[^]", ']', "#[]^");
    }

    public void testParens1() throws Exception {
        insertChar("x = ^", '(', "x = (^)");
    }

    public void testParens2() throws Exception {
        insertChar("x = (^)", ')', "x = ()^");
    }

    public void testParens3() throws Exception {
        insertChar("x = (^)", 'a', "x = (a^)");
    }

    public void testParens4() throws Exception {
        insertChar("x = (^)", '(', "x = ((^))");
    }

    public void testParens5() throws Exception {
        insertChar("x = ((^))", ')', "x = (()^)");
    }

    public void testParens6() throws Exception {
        insertChar("x = (()^)", ')', "x = (())^");
    }

    public void testReplaceSelection1() throws Exception {
        insertChar("x = foo^", 'y', "x = y^", "foo");
    }

    public void testReplaceSelection4() throws Exception {
        insertChar("x = 'foo^bar'", '#', "x = '#^bar'", "foo");
    }

    public void testReplaceSelection2() throws Exception {
        insertChar("x = foo^", '"', "x = \"foo\"^", "foo");
    }

    public void testReplaceSelection5() throws Exception {
        insertChar("'(^position:absolute;'", '{', "'{^position:absolute;'", "(");
    }

    public void testReplaceSelection6() throws Exception {
        insertChar("'position^:absolute;'", '{', "'pos{^:absolute;'", "ition");
    }

    public void testReplaceSelectionChangeType1() throws Exception {
        insertChar("x = \"foo\"^", '\'', "x = 'foo'^", "\"foo\"");
    }

    public void testReplaceSelectionChangeType2() throws Exception {
        insertChar("x = \"foo\"^", '{', "x = {foo}^", "\"foo\"");
    }

    public void testReplaceSelectionNotInTemplateMode1() throws Exception {
        insertChar("x = foo^", '"', "x = \"^\"", "foo", true);
    }

    public void testReplaceCommentSelectionBold() throws Exception {
        insertChar("# foo^", '*', "# *foo*^", "foo");
    }

    public void testReplaceCommentSelectionTerminal() throws Exception {
        insertChar("# foo^", '+', "# +foo+^", "foo");
    }

    public void testReplaceCommentSelectionItalic() throws Exception {
        insertChar("# foo^", '_', "# _foo_^", "foo");
    }

    public void testReplaceCommentSelectionWords() throws Exception {
        // No replacement if it contains multiple lines
        insertChar("# foo bar^", '*', "# *^", "foo bar");
    }

    public void testReplaceCommentOther() throws Exception {
        // No replacement if it's not one of the three chars
        insertChar("# foo^", 'x', "# x^", "foo");
    }

    public void test108889() throws Exception {
        // Reproduce 108889: AIOOBE and AE during editing
        // NOTE: While the test currently throws an exception, when the
        // exception is fixed the test won't actually pass; that's an expected
        // fail I will deal with later
        insertChar("x = %q((^))", 'a', "x = %q((a^))");
    }

    public void testInsertBrace01() throws Exception {
        String testString = "if (true)" +
                "\n" +
                "    ^";
        String result  = "if (true)" +
                "\n" +
                "{^";
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar(testString, '{', result, null, false, options);
    }

    public void testInsertBrace02() throws Exception {
        String testString = "    class Name\n" +
                "          ^";
        String result  = "    class Name\n" +
                "    {^";
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar(testString, '{', result, null, false, options);
    }

    public void testInsertBrace03() throws Exception {
        String testString =
                "    if ($a == 10\n" +
                "            || $b == 11\n" +
                "            || $a == $b)\n" +
                "          ^";
        String result  =
                "    if ($a == 10\n" +
                "            || $b == 11\n" +
                "            || $a == $b)\n" +
                "    {^";
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        insertChar(testString, '{', result, null, false, options);
    }

    public void testInsertBrace04() throws Exception {
        String testString =
                "    if ($a == 10\n" +
                "            || $b == 11\n" +
                "            || $a == $b)\n" +
                "^";
        String result  =
                "    if ($a == 10\n" +
                "            || $b == 11\n" +
                "            || $a == $b)\n" +
                "    {^";
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar(testString, '{', result, null, false, options);
    }

    public void testInsertBrace05() throws Exception {
        String testString =
                "    $a = 10;\n" +
                "    while ($a == 10\n" +
                "            || $b == 11\n" +
                "            || $a == $b)\n" +
                "          ^";
        String result  =
                "    $a = 10;\n" +
                "    while ($a == 10\n" +
                "            || $b == 11\n" +
                "            || $a == $b)\n" +
                "    {^";
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar(testString, '{', result, null, false, options);
    }

    public void testInsertBrace06() throws Exception {
        String testString =
                "    $a = 10;\n" +
                "    do\n" +
                "          ^";
        String result  =
                "    $a = 10;\n" +
                "    do\n" +
                "    {^";
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar(testString, '{', result, null, false, options);
    }

    public void testInsertBrace07() throws Exception {
        String testString =
                "    foreach($zzz as $zzzz)\n" +
                "          ^";
        String result  =
                "    foreach($zzz as $zzzz)\n" +
                "    {^";
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar(testString, '{', result, null, false, options);
    }

    public void testInsertBrace08() throws Exception {
        String testString =
                "    for($i = 0; $i < 10; $i++)\n" +
                "          ^";
        String result  =
                "    for($i = 0; $i < 10; $i++)\n" +
                "    {^";
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar(testString, '{', result, null, false, options);
    }

    public void testBracePlacement01() throws Exception {
        String testString = "class Name\n" +
                "    ^";
        String result  = "class Name\n" +
                "    {^";
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        insertChar(testString, '{', result, null, false, options);
    }


    public void testIsseu191443() throws Exception {
        String testString = "$test = (string^) ahoj;";
        String result  = "$test = (string)^ ahoj;";
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar(testString, ')', result, null, false, options);
    }

    public void testIssue198699_01() throws Exception {
        insertChar("a selected^ word", '"', "a \"selected\"^ word", "selected");
    }

    public void testIssue198708_01() throws Exception {
        insertChar("if ($a=($i+1^)", ')', "if ($a=($i+1)^)");
    }

    public void testIssue198708_02() throws Exception {
        insertChar("if (($a=($i+1^))", ')', "if (($a=($i+1)^))");
    }

    public void testIssue198708_03() throws Exception {
        insertChar("if ($a=($i+1^))", ')', "if ($a=($i+1)^)");
    }

    public void testIssue198708_04() throws Exception {
        insertChar("if (($a=($i+1^)))", ')', "if (($a=($i+1)^))");
    }

    public void testIssue209638() throws Exception {
        insertChar("$test = array(\n"
                + "    array(^)\n"
                + ");", ')', "$test = array(\n"
                + "    array()^\n"
                + ");");
    }

    public void testIssue212301_01() throws Exception {
        insertChar("$foo = 'bar';^", '/', "/^", "$foo = 'bar';");
    }

    public void testIssue212301_02() throws Exception {
        insertChar("$foo = 'bar'^;", '/', "/^;", "$foo = 'bar'");
    }

    public void testIssue212301_03() throws Exception {
        insertChar("$foo = 'bar'^;", '/', "$foo = /^;", "'bar'");
    }

    public void testIssue198810_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar("if (true)\n    ^", '{', "if (true)\n{^", null, false, options);
    }

    public void testIssue198810_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar("if (true)\n{    foo();\n    ^", '}', "if (true)\n{    foo();\n}^", null, false, options);
    }

    public void testIssue198810_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        insertChar("if (true)\n    ^", '{', "if (true)\n    {^", null, false, options);
    }

    public void testIssue198810_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        insertChar("if (true)\n    {    foo();\n    ^", '}', "if (true)\n    {    foo();\n    }^", null, false, options);
    }

    public void testIssue198810_05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar("function foo()\n{\n    while ($bar)\n    ^", '{', "function foo()\n{\n    while ($bar)\n    {^", null, false, options);
    }

    public void testIssue198810_06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar("function foo()\n{\n    while ($bar)\n    {\n        if (true)\n        ^", '{', "function foo()\n{\n    while ($bar)\n    {\n        if (true)\n        {^", null, false, options);
    }

    public void testIssue198810_07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar("function foo()\n{\n    while ($bar)\n    {\n        if (true)\n        {\n            doSmt();\n            ^", '}', "function foo()\n{\n    while ($bar)\n    {\n        if (true)\n        {\n            doSmt();\n        }^", null, false, options);
    }

    public void testIssue198810_08() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar("function foo()\n{\n    while ($bar)\n    {\n        if (true)\n        {\n            doSmt();\n        }\n        ^", '}', "function foo()\n{\n    while ($bar)\n    {\n        if (true)\n        {\n            doSmt();\n        }\n    }^", null, false, options);
    }

    public void testIssue198810_09() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        insertChar("function foo()\n{\n    while ($bar)\n    {\n        if (true)\n        {\n            doSmt();\n        }\n    }\n    ^", '}', "function foo()\n{\n    while ($bar)\n    {\n        if (true)\n        {\n            doSmt();\n        }\n    }\n}^", null, false, options);
    }

    public void testIssue198810_10() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        insertChar("function foo()\n{\n    while ($bar)\n    ^", '{', "function foo()\n{\n    while ($bar)\n        {^", null, false, options);
    }

    public void testIssue198810_11() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        insertChar("function foo()\n{\n    while ($bar)\n        {\n        if (true)\n        ^", '{', "function foo()\n{\n    while ($bar)\n        {\n        if (true)\n            {^", null, false, options);
    }

    public void testIssue198810_12() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        insertChar("function foo()\n{\n    while ($bar)\n        {\n        if (true)\n            {\n            doSmt();\n            ^", '}', "function foo()\n{\n    while ($bar)\n        {\n        if (true)\n            {\n            doSmt();\n            }^", null, false, options);
    }

    public void testIssue198810_13() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        insertChar("function foo()\n{\n    while ($bar)\n        {\n        if (true)\n            {\n            doSmt();\n            }\n        ^", '}', "function foo()\n{\n    while ($bar)\n        {\n        if (true)\n            {\n            doSmt();\n            }\n        }^", null, false, options);
    }

    public void testIssue198810_14() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        insertChar("function foo()\n    {\n    while ($bar)\n        {\n        if (true)\n            {\n            doSmt();\n            }\n        }\n    ^", '}', "function foo()\n    {\n    while ($bar)\n        {\n        if (true)\n            {\n            doSmt();\n            }\n        }\n    }^", null, false, options);
    }

    public void testIssue170779_01() throws Exception {
        String original = "switch($value) {\n    case^\n}";
        String expected = "switch($value) {\n    case ^\n}";
        insertChar(original, ' ', expected);
    }

    public void testIssue170779_02() throws Exception {
        String original = "switch ($value) {\n    case 1:\n        break;\n    case^\n}";
        String expected = "switch ($value) {\n    case 1:\n        break;\n    case ^\n}";
        insertChar(original, ' ', expected);
    }

    public void testIssue170779_03() throws Exception {
        String original = "switch ($value) {\n    case 1:\n        break;\n    case 2:\n        case^\n}";
        String expected = "switch ($value) {\n    case 1:\n        break;\n    case 2:\n    case ^\n}";
        insertChar(original, ' ', expected);
    }

    public void testIssue170779_04() throws Exception {
        String original = "switch ($value) {\n    case 1:\n        break;\n    case 2:\n        default^\n}";
        String expected = "switch ($value) {\n    case 1:\n        break;\n    case 2:\n    default:^\n}";
        insertChar(original, ':', expected);
    }

    public void testIssue223165() throws Exception {
        String original = "switch ($a) {\n    case 1: break;\n}if^";
        String expected = "switch ($a) {\n    case 1: break;\n}if ^";
        insertChar(original, ' ', expected);
    }

    // the first char of the selected text is a specific char '", )]}\n\t:
    public void testIssue256659_01() throws Exception {
        insertChar("$foo = 'bar' => $qux^;", '[', "$foo = ['bar' => $qux]^;", "'bar' => $qux");
    }

    public void testIssue256659_02() throws Exception {
        insertChar("$foo = \"bar\" => $qux^;", '(', "$foo = (\"bar\" => $qux)^;", "\"bar\" => $qux");
    }

    public void testIssue256659_03() throws Exception {
        insertChar("$foo = ,$bar => $qux^;", '[', "$foo = [,$bar => $qux]^;", ",$bar => $qux");
    }

    public void testIssue256659_04() throws Exception {
        insertChar("$foo = $bar => $qux^;", '(', "$foo =( $bar => $qux)^;", " $bar => $qux");
    }

    public void testIssue256659_05() throws Exception {
        insertChar(")foo^", '(', "()foo)^", ")foo");
    }

    public void testIssue256659_06() throws Exception {
        insertChar("]foo^", '(', "(]foo)^", "]foo");
    }

    public void testIssue256659_07() throws Exception {
        insertChar("}foo^", '(', "(}foo)^", "}foo");
    }

    public void testIssue256659_08() throws Exception {
        insertChar("\nfoo^", '(', "(\nfoo)^", "\nfoo");
    }

    public void testIssue256659_09() throws Exception {
        insertChar("\tfoo^", '(', "(\tfoo)^", "\tfoo");
    }

    public void testIssue256659_10() throws Exception {
        // replace ' -> [
        insertChar("'\"(a)\"'^", '[', "[\"(a)\"]^", "'\"(a)\"'");
    }

    public void testIssueGH5707_01() throws Exception {
        String original = ""
                + "switch ($variable) {\n"
                + "    case 1:\n"
                + "        break;\n"
                + "    case 2:\n"
                + "        break;\n"
                + "    ^\n"
                + "}";
        String expected = ""
                + "switch ($variable) {\n"
                + "    case 1:\n"
                + "        break;\n"
                + "    case 2:\n"
                + "        break;\n"
                + "     ^\n"
                + "}";
        insertChar(original, ' ', expected);
    }

    public void testIssueGH5707_02() throws Exception {
        String original = ""
                + "enum Enum1 {\n"
                + "    case A = 'A';\n"
                + "    case B = 'B';\n"
                + "    ^\n"
                + "}";
        String expected = ""
                + "enum Enum1 {\n"
                + "    case A = 'A';\n"
                + "    case B = 'B';\n"
                + "     ^\n"
                + "}";
        insertChar(original, ' ', expected);
    }

    public void testIssueGH6706_01() throws Exception {
        String original = ""
                + "$test = \"[$variable test]\";\n"
                + "$array['key'^];";
        String expected = ""
                + "$test = \"[$variable test]\";\n"
                + "$array['key']^;";
        insertChar(original, ']', expected);
    }

    public void testIssueGH6706_02() throws Exception {
        String original = ""
                + "$test = \"[test $variable]\";\n"
                + "$array['key'^];";
        String expected = ""
                + "$test = \"[test $variable]\";\n"
                + "$array['key']^;";
        insertChar(original, ']', expected);
    }

    public void testIssueGH6706_03() throws Exception {
        String original = ""
                + "$test = \"[$variable]\";\n"
                + "$array['key'^];";
        String expected = ""
                + "$test = \"[$variable]\";\n"
                + "$array['key']^;";
        insertChar(original, ']', expected);
    }

//    Uncomment when CslTestBase.insertChar() will support ambiguous selection strings
//
//    public void testIssue242358() throws Exception {
//        String original = "foo(\"*.txt^\");";
//        String expected = "foo('*.txt^');";
//        insertChar(original, '\'', expected, "\"");
//    }

}
