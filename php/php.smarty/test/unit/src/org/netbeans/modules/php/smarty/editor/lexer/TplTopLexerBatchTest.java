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
package org.netbeans.modules.php.smarty.editor.lexer;

import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.php.smarty.SmartyFramework.Version;
import org.netbeans.modules.php.smarty.TplTestBase;
import org.netbeans.modules.php.smarty.ui.options.SmartyOptions;

/**
 * Test TPL top-level lexer analyzis.
 *
 * @author Martin Fousek
 */
public class TplTopLexerBatchTest extends TplTestBase {

    public TplTopLexerBatchTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        resetSmartyOptions();
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    @Override
    protected void tearDown() throws java.lang.Exception {
    }

    public void testTopSmartyAndHtmlTags() {
        String text = "{include file='head.tpl'}{if $logged neq 0}<span color=\"{#fontColor#}\">{$name|upper}!{/if}</span>";

        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "include file='head.tpl'");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "if $logged neq 0");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, "<span color=\"");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "#fontColor#");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, "\">");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "$name|upper");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, "!");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "/if");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, "</span>");
    }

    public void testSmartyLiteralTags() {
        String text = "{literal}{{/literal}{";

        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "literal");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "/literal");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
    }

    public void testSmartyLiteralTags2() {
        String text = "{literal} any text here { also some bracket{/literal}{";

        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "literal");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, " any text here { also some bracket");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "/literal");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
    }

    public void testSmartyLiteralTags3() {
        String text = "{literal} any text here {/ also some bracket{/literal}{";

        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "literal");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, " any text here ");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, "/ also some bracket");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "/literal");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
    }

    public void testSmartyLiteralTags4() {
        String text = "{literal} any text here {/ also some bracket";

        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "literal");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, " any text here ");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, "/ also some bracket");
    }

    public void testSmartyCommentsTags() {
        String text = "{*{c*}{if}{*c*}{/if}";

        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "c");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "if");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "c");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "/if");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
    }

    public void testSmartyPhpTags() {
        String text = "{php}function { this. {/php}{";

        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "php");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_PHP, "fu");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_PHP, "nc");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_PHP, "ti");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_PHP, "on");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_PHP, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_PHP, "{ this.");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_PHP, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "/php");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
    }

    public void testSmarty3CurlyBracesFeature() {
        String text = "{ var tmp = 1; }";

        SmartyOptions.getInstance().setSmartyVersion(Version.SMARTY3);
        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, "{ var tmp = 1; }");
    }

    public void testSmarty2CurlyBracesFeature() {
        String text = "{ var tmp = 1; }";

        SmartyOptions.getInstance().setSmartyVersion(Version.SMARTY2);
        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
    }

    public void testSmarty2AndAnotherOpenDelims() {
        String text = "{ var tmp = 1; }";

        SmartyOptions.getInstance().setDefaultOpenDelimiter("LDELIM");
        SmartyOptions.getInstance().setSmartyVersion(Version.SMARTY2);
        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, "{ var tmp = 1; }");
    }

    public void testSmarty3AndAnotherOpenDelims() {
        String text = "{ var tmp = 1; }";

        SmartyOptions.getInstance().setDefaultOpenDelimiter("LDELIM");
        SmartyOptions.getInstance().setSmartyVersion(Version.SMARTY3);
        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, "{ var tmp = 1; }");
    }

    public void testSmarty2NonCurlyBracesFeatureAndAnotherOpenDelims() {
        String text = "LDELIM var tmp = 1; }";

        SmartyOptions.getInstance().setDefaultOpenDelimiter("LDELIM");
        SmartyOptions.getInstance().setSmartyVersion(Version.SMARTY2);
        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "LDELIM");
    }

    public void testSmarty3NonCurlyBracesFeatureAndAnotherOpenDelims() {
        String text = "LDELIM var tmp = 1; }";

        SmartyOptions.getInstance().setDefaultOpenDelimiter("LDELIM");
        SmartyOptions.getInstance().setSmartyVersion(Version.SMARTY3);
        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, "LDELIM var tmp = 1; }");
    }

    public void testIssue205742() {
        String text = "{** End of \"AREA\" *}";

        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "E");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "n");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "d");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "o");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "f");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "A");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "R");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "E");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "A");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
    }

    public void testIssue205540_1() {
        String text = "{button type=\"add\" url=\"{$root}\" href=\"foo\"}";

        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "button type=\"add\" url=\"{$root}\" href=\"foo\"");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
    }

    public void testIssue205540_2() {
        String text = "{foofunction param=\"{barfunction param={bazfunction}\"} ";

        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "foofunction param=\"{barfunction param={bazfunction}\"");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, " ");
    }

    public void testIssue205540_3() {
        String text = "{foo var=\"bar'baz\"} ";

        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "foo var=\"bar'baz\"");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, " ");
    }

    public void testIssue215941() {
        String text = "[{* text *}]";
        setupSmartyOptions("[{", "}]", Version.SMARTY3);

        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "[{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "t");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "e");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "t");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_COMMENT, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}]");
    }

    public void testIssue212121_1() {
        String text = "{$script = \"var p = { a: 0, b: 0 } ; var s = { t: true, u: false } ;\"}";

        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "$script = \"var p = { a: 0, b: 0 } ; var s = { t: true, u: false } ;\"");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
    }

    public void testIssue212121_2() {
        String text = "{$script = \"var p = { a : 0 } \"} ";

        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "$script = \"var p = { a : 0 } \"");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, " ");
    }

    public void testIssue212121_3() {
        String text = "<title>{$title}</title><style type=\"text/css\">.a { margin-left: 15px; }</style>";

        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, "<title>");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "$title");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, "</title><style type=\"text/css\">.a { margin-left: 15px; }</style>");
    }

    public void testIssue232290_1() {
        String text = "{include file=\"admin/inc/availability.tpl\"\n" +
                        "    availability_color=$product->availability_color\n" +
                        "    availability_value=$product->availability_value } ";

        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "include file=\"admin/inc/availability.tpl\"\n    availability_color=$product->availability_color\n    availability_value=$product->availability_value ");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, " ");
    }

    public void testIssue232290_2() {
        String text = "{include file=\"admin/inc/availability.tpl\"\n" +
                        "        availability_color=$product->availability_color\n" +
                        "        availability_value=$product->availability_value\n" +
                        "    } ";

        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "include file=\"admin/inc/availability.tpl\"\n        availability_color=$product->availability_color\n        availability_value=$product->availability_value\n    ");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, " ");
    }

    public void testIssue235733_1() {
        String text = "{assign var='sss' value='I\"m string'}";

        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "assign var='sss' value='I\"m string'");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
    }

    public void testIssue235733_2() {
        String text = "{assign var='sss' value=\"I'm string\"}";

        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "assign var='sss' value=\"I'm string\"");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
    }

    public void testIssue235733_3() {
        String text = "{assign var='sss' value='I\\'m string'}";

        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "assign var='sss' value='I\\'m string'");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
    }

    public void testIssue235733_4() {
        // this is unescaped apostrophe
        String text = "{assign var='sss' value='I\'m string'}";

        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, "assign var='sss' value='I\'m string'}");
    }

    public void testIssue235733_5() {
        String text = "{assign var='sss' value=\"escape \\\" char \"}";

        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "assign var='sss' value=\"escape \\\" char \"");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
    }

    public void testIssue243418_1() {
        String text = "<img src=\"{php_thumb file=$i.file width=$i.width}\" style=\"width:{$i.width}px;\" />";

        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, "<img src=\"");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "php_thumb file=$i.file width=$i.width");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, "\" style=\"width:");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "$i.width");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_HTML, "px;\" />");
    }

    public void testIssue243418_2() {
        String text = "{literal_custom}";

        TokenSequence ts = createTokenSequence(text);
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_OPEN_DELIMITER, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY, "literal_custom");
        LexerTestUtilities.assertNextTokenEquals(ts, TplTopTokenId.T_SMARTY_CLOSE_DELIMITER, "}");
    }

    private TokenSequence createTokenSequence(String text) {
        TokenHierarchy<?> hi = TokenHierarchy.create(text, TplTopTokenId.language());
        return hi.tokenSequence();
    }
}
