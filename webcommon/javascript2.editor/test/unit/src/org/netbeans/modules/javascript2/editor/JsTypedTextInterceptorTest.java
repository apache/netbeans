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

package org.netbeans.modules.javascript2.editor;

import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.editor.options.OptionsUtils;

/**
 * @todo Try typing in whole source files and other than tracking missing end and } closure
 *   statements the buffer should be identical - both in terms of quotes to the rhs not having
 *   accumulated as well as indentation being correct.
 * @todo
 *   // automatic reindentation of "end", "else" etc.
 *
 *
 *
 * @author Tor Norbye
 */
public class JsTypedTextInterceptorTest extends JsTestBase {

    public JsTypedTextInterceptorTest(String testName) {
        super(testName);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        OptionsUtils options = OptionsUtils.forLanguage(getPreferredLanguage().getLexerLanguage());
        options.setTestDisablePreferencesTracking();
        options.setTestCompletionSmartQuotes(OptionsUtils.AUTO_COMPLETION_SMART_QUOTES_DEFAULT);
        MimeLookup.getLookup(JsTokenId.JAVASCRIPT_MIME_TYPE).lookup(Preferences.class).clear();
    }


    @Override
    protected Formatter getFormatter(IndentPrefs preferences) {
        return null;
    }

    private void insertChar(String original, char insertText, String expected) throws Exception {
        insertChar(original, insertText, expected, null);
    }

    private void insertChar(String original, char insertText, String expected, String selection) throws Exception {
        insertChar(original, insertText, expected, selection, false);
    }

    public void testInsertX() throws Exception {
        insertChar("c^ass", 'l', "cl^ass");
    }

    public void testInsertX2() throws Exception {
        insertChar("clas^", 's', "class^");
    }

    public void testNoMatchInComments() throws Exception {
        insertChar("// Hello^", '\'', "// Hello'^");
        insertChar("// Hello^", '"', "// Hello\"^");
        insertChar("// Hello^", '[', "// Hello[^");
        insertChar("// Hello^", '(', "// Hello(^");
        insertChar("/* Hello^*/", '\'', "/* Hello'^*/");
        insertChar("/* Hello^*/", '"', "/* Hello\"^*/");
        insertChar("/* Hello^*/", '[', "/* Hello[^*/");
        insertChar("/* Hello^*/", '(', "/* Hello(^*/");
    }

    public void testNoMatchInStrings() throws Exception {
        insertChar("x = \"^\"", '\'', "x = \"'^\"");
        insertChar("x = \"^\"", '[', "x = \"[^\"");
        insertChar("x = \"^\"", '(', "x = \"(^\"");
        insertChar("x = \"^)\"", ')', "x = \")^)\"");
        insertChar("x = '^'", '"', "x = '\"^'");
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

    public void testTemplateQuotes1() throws Exception {
        insertChar("x = ^", '`', "x = `^`");
    }

    public void testTemplateQuotes2() throws Exception {
        insertChar("x = `^`", '`', "x = ``^");
    }

    public void testTemplateQuotes3() throws Exception {
        insertChar("x = `^`", 'a', "x = `a^`");
    }

    public void testTemplateQuotes4() throws Exception {
        insertChar("x = '\\^`", '`', "x = '\\`^`");
    }

    public void testInsertBrokenQuote() throws Exception {
        insertChar("System.out.prinlnt(\"pavel^)", '"',
                "System.out.prinlnt(\"pavel\"^)");
    }

    public void testInsertBrokenQuote2() throws Exception {
        insertChar("System.out.prinlnt(\"pavel^\n", '"',
                "System.out.prinlnt(\"pavel\"^\n");
    }

    public void testInsertBrokenQuote3() throws Exception {
        insertChar("System.out.prinlnt(\"^\n", '"',
                "System.out.prinlnt(\"\"^\n");
    }

    public void testInsertBrokenQuote4() throws Exception {
        insertChar("System.out.prinlnt(\"pavel^", '"',
                "System.out.prinlnt(\"pavel\"^");
    }

    public void testInsertBrokenTemplate1() throws Exception {
        insertChar("System.out.prinlnt(`pavel^)", '`',
                "System.out.prinlnt(`pavel`^)");
    }

    public void testInsertBrokenTemplate2() throws Exception {
        insertChar("System.out.prinlnt(`pavel^\n", '`',
                "System.out.prinlnt(`pavel`^\n");
    }

    public void testInsertBrokenTemplate3() throws Exception {
        insertChar("System.out.prinlnt(`^\n", '`',
                "System.out.prinlnt(``^\n");
    }

    public void testInsertBrokenTemplate4() throws Exception {
        insertChar("System.out.prinlnt(`pavel^", '`',
                "System.out.prinlnt(`pavel`^");
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

    public void testBrackets1() throws Exception {
        insertChar("x = ^", '[', "x = [^]");
    }

    public void testBrackets2() throws Exception {
        insertChar("x = [^]", ']', "x = []^");
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

    public void testBrace1() throws Exception {
        insertChar("function test(){^}", '}', "function test(){}^");
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

    public void testParens7() throws Exception {
        insertChar("x = ((^)", ')', "x = (()^)");
    }

    public void testRegexp3() throws Exception {
        insertChar("x = /^/", 'a', "x = /a^/");
    }

    public void testRegexp4() throws Exception {
        insertChar("x = /\\^/", '/', "x = /\\/^/");
    }

    public void testRegexp5() throws Exception {
        insertChar("    regexp = /fofo^\n      // Subsequently, you can make calls to it by name with <tt>yield</tt> in", '/',
                "    regexp = /fofo/^\n      // Subsequently, you can make calls to it by name with <tt>yield</tt> in");
    }

    public void testRegexp6() throws Exception {
        insertChar("    regexp = /fofo^\n", '/',
                "    regexp = /fofo/^\n");
    }

    public void testRegexp9() throws Exception {
        insertChar("x = /^/\n", 'a', "x = /a^/\n");
    }

    public void testRegexp10() throws Exception {
        insertChar("x = /\\^/\n", '/', "x = /\\/^/\n");
    }

    public void testRegexp11() throws Exception {
        insertChar("/foo^", '/',
                "/foo/^");
    }
    public void testNotRegexp1() throws Exception {
        insertChar("x = 10 ^", '/', "x = 10 /^");
    }

    public void testNotRegexp2() throws Exception {
        insertChar("x = 3.14 ^", '/', "x = 3.14 /^");
    }

    public void testNotRegexp4() throws Exception {
        insertChar("x = y^", '/', "x = y/^");
    }

    public void testNotRegexp5() throws Exception {
        insertChar("/^", '/', "//^");
    }

    public void testNoInsertPercentElsewhere() throws Exception {
        insertChar("x = ^", '#', "x = #^");
    }

    public void testReplaceSelection1() throws Exception {
        insertChar("x = foo^", 'y', "x = y^", "foo");
    }

    public void testReplaceSelection2() throws Exception {
        insertChar("x = foo^", '"', "x = \"foo\"^", "foo");
    }

    public void testReplaceSelection4() throws Exception {
        insertChar("x = 'foo^bar'", '#', "x = '#^bar'", "foo");
    }

    public void testReplaceSelection5() throws Exception {
        insertChar("'(^position:absolute;'", '{', "'{^position:absolute;'", "(");
    }

    public void testReplaceSelection6() throws Exception {
        insertChar("'position^:absolute;'", '{', "'pos{^:absolute;'", "ition");
    }

    public void testReplaceSelection7() throws Exception {
        insertChar("x = foo^", '`', "x = `foo`^", "foo");
    }

    public void testReplaceSelectionChangeType1() throws Exception {
        insertChar("x = \"foo\"^", '\'', "x = 'foo'^", "\"foo\"");
    }

    public void testReplaceSelectionChangeType2() throws Exception {
        insertChar("x = \"foo\"^", '{', "x = {foo}^", "\"foo\"");
    }

    public void testReplaceSelectionChangeType3() throws Exception {
        insertChar("x = \"foo\"^", '`', "x = `foo`^", "\"foo\"");
    }

    public void testReplaceSelectionChangeType4() throws Exception {
        insertChar("x = 'foo'^", '`', "x = `foo`^", "'foo'");
    }

    public void testReplaceSelectionNotInTemplateMode1() throws Exception {
        insertChar("x = foo^", '"', "x = \"^\"", "foo", true);
    }

    public void testEnabledSmartQuotes() throws Exception {
        insertChar("x = ^", '"', "x = \"^\"");
    }

    @SuppressWarnings("unchecked")
    public void testDisabledSmartQuotes() throws Exception {
        OptionsUtils.forLanguage(getPreferredLanguage().getLexerLanguage()).setTestCompletionSmartQuotes(false);
        insertChar("x = ^", '"', "x = \"^");
    }

    public void testDisabledBrackets1() throws Exception {
        MimeLookup.getLookup(JsTokenId.JAVASCRIPT_MIME_TYPE).lookup(Preferences.class)
                .putBoolean(SimpleValueNames.COMPLETION_PAIR_CHARACTERS, false);
        insertChar("x = ^", '(', "x = (^");
    }

    public void testDisabledBrackets2() throws Exception {
        MimeLookup.getLookup(JsTokenId.JAVASCRIPT_MIME_TYPE).lookup(Preferences.class)
                .putBoolean(SimpleValueNames.COMPLETION_PAIR_CHARACTERS, false);
        insertChar("x = ^", '[', "x = [^");
    }

    public void testDisabledBrackets3() throws Exception {
        MimeLookup.getLookup(JsTokenId.JAVASCRIPT_MIME_TYPE).lookup(Preferences.class)
                .putBoolean(SimpleValueNames.COMPLETION_PAIR_CHARACTERS, false);
        insertChar("x = ^", '{', "x = {^");
    }

    public void testIssue233067() throws Exception {
        insertChar("window.console.log(\"^\")", 'a', "window.console.log(\"a^\")", null, true);
    }

    public void testIssue233292_1() throws Exception {
        insertChar("var a = \"test\"^", ';', "var a = \"test\";^");
    }

    public void testIssue233292_2() throws Exception {
        insertChar("a.replace(\"aaa\"^)", ',', "a.replace(\"aaa\",^)");
    }

    public void testIssue233292_3() throws Exception {
        insertChar("test(\"asasa\", {pes:\"1\"^)", '}', "test(\"asasa\", {pes:\"1\"}^)");
    }

    public void testIssue233292_4() throws Exception {
        insertChar("var a = {\n    pes: \"1\"^\n}\n", ',', "var a = {\n    pes: \"1\",^\n}\n");
    }

    public void testIssue189443() throws Exception {
        insertChar("function x() {\n"
            + "for(var j in child)^\n"
            + "            alert(child.item(j));\n"
            + "            child.item(j).checked = true;\n"
            + "\n"
            +"}\n",
            '{',
            "function x() {\n"
            + "for(var j in child){^\n"
            + "            alert(child.item(j));\n"
            + "            child.item(j).checked = true;\n"
            + "\n"
            +"}\n");
    }

    public void testIssue195515() throws Exception {
        insertChar("function name() { {^}", '}', "function name() { {}^");
    }
}
