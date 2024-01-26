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

package org.netbeans.modules.php.editor.indent;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class PHPNewLineIndenterTest extends PHPTestBase {
    public PHPNewLineIndenterTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
	//System.setProperty("org.netbeans.editor.linewrap.disable", "true");
        try {
            TestLanguageProvider.register(HTMLTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
        try {
            TestLanguageProvider.register(PHPTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
    }

    public void testSmartEnter() throws Exception{
        testIndentInFile("testfiles/indent/smart_enter.php");
    }

    public void testSmartEnter02() throws Exception{
        testIndentInFile("testfiles/indent/smart_enter_02.php");
    }

    public void testSmartEnter03() throws Exception{
        testIndentInFile("testfiles/indent/smart_enter_03.php");
    }

    public void testHtmlIndentInPHP() throws Exception{
        testIndentInFile("testfiles/indent/html_indent_in_php.php");
    }

    public void testFirstLine01() throws Exception{
        testIndentInFile("testfiles/indent/firstline_01.php");
    }

    public void testFirstLine02() throws Exception{
        testIndentInFile("testfiles/indent/firstline_02.php");
    }

    public void testFirstLine03() throws Exception{
        testIndentInFile("testfiles/indent/firstline_03.php");
    }

    public void testFirstLine04() throws Exception{
        testIndentInFile("testfiles/indent/firstline_04.php");
    }

    public void testTrivialRepeatedIndent() throws Exception{
        testIndentInFile("testfiles/indent/trivial_repeated_indent.php");
    }

    public void testAfterSwitchCase() throws Exception{
        testIndentInFile("testfiles/indent/after_switch_case.php");
    }

    public void testAfterSwitchBreak() throws Exception{
        testIndentInFile("testfiles/indent/after_switch_break.php");
    }

    public void testAfterSwitchBreak1() throws Exception{
        testIndentInFile("testfiles/indent/after_switch_break_1.php");
    }

    public void testBreakInWhile() throws Exception{
        testIndentInFile("testfiles/indent/break_in_while.php");
    }

    public void testMultilineFunctionCall() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call.php");
    }

    public void testMultilineFunctionCall01() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_01.php");
    }

    public void testMultilineFunctionCall02() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_02.php");
    }

    public void testMultilineFunctionCall03() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_03.php");
    }

    public void testMultilineFunctionCall04() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_04.php");
    }

    public void testMultilineFunctionCall05() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_05.php");
    }

    public void testMultilineFunctionCall06() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_06.php");
    }

    public void testMultilineFunctionCall07() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_07.php");
    }

    public void testMultilineFunctionCall08() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_08.php");
    }

    public void testMultilineFunctionCall09() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_09.php");
    }

    public void testMultilineFunctionCall10() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_10.php");
    }

    public void testMultilineFunctionCall11() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_11.php");
    }

    public void testMultilineFunctionCall12() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_12.php");
    }

    public void testMultilineFunctionCall13() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_13.php");
    }

    public void testMultilineFunctionCall14() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_14.php");
    }

    public void testMultilineFunctionCall15() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_15.php");
    }

    public void testMultilineFunctionCall16() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_16.php");
    }

    public void testMultilineFunctionCall17() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_17.php");
    }

    public void testMultilineFunctionCall18() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_18.php");
    }

    public void testMultilineFunctionCall19() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_19.php");
    }

    public void testMultilineFunctionCall20() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_20.php");
    }

    public void testMultilineFunctionCall21() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_21.php");
    }

    public void testMultilineFunctionCall22() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_22.php");
    }

    public void testMultilineFunctionCall23() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_23.php");
    }

    public void testMultilineFunctionCall24() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_24.php");
    }

    public void testMultilineFunctionCall25() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_25.php");
    }

    public void testMultilineFunctionCall26() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_26.php");
    }

    public void testMultilineFunctionCall27() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_27.php");
    }

    public void testMultilineFunctionCall28() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_28.php");
    }

    public void testMultilineFunctionCall29() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_29.php");
    }

    public void testMultilineFunctionCall30() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_30.php");
    }

    public void testMultilineFunctionCall31() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_31.php");
    }

    public void testMultilineFunctionCall32() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_32.php");
    }

    public void testMultilineFunctionCall33() throws Exception{
        testIndentInFile("testfiles/indent/multiline_function_call_33.php");
    }

    public void testIndentAfterClosingBracket() throws Exception{
        testIndentInFile("testfiles/indent/indent_after_closing_bracket.php");
    }

    public void testControlStmtWithoutBracket1() throws Exception{
        testIndentInFile("testfiles/indent/control_stmt_without_bracket1.php");
    }

    public void testControlStmtWithoutBracket2() throws Exception{
        testIndentInFile("testfiles/indent/control_stmt_without_bracket2.php");
    }

    public void testControlStmtWithoutBracket3() throws Exception{
        testIndentInFile("testfiles/indent/control_stmt_without_bracket3.php");
    }

    public void testIndentAfterMultilineStmt1() throws Exception{
        testIndentInFile("testfiles/indent/indent_after_multiline_stmt1.php");
    }

    public void testMultilineString1() throws Exception{
        testIndentInFile("testfiles/indent/multiline_string1.php");
    }

    public void testMultilineString2() throws Exception{
        testIndentInFile("testfiles/indent/multiline_string2.php");
    }

    public void testMultilineString3() throws Exception{
        testIndentInFile("testfiles/indent/multiline_string3.php");
    }

    public void testMultilineString4() throws Exception{
        testIndentInFile("testfiles/indent/multiline_string4.php");
    }

    public void testArrays1() throws Exception{
        testIndentInFile("testfiles/indent/arrays1.php");
    }

    public void testArrays2() throws Exception{
        testIndentInFile("testfiles/indent/arrays2.php");
    }

    public void testArrays3() throws Exception{
        testIndentInFile("testfiles/indent/arrays3.php");
    }

    public void testArrays4() throws Exception{
        testIndentInFile("testfiles/indent/arrays4.php");
    }

    public void testArrays5() throws Exception{
        testIndentInFile("testfiles/indent/arrays5.php");
    }

    public void testArrays6() throws Exception{
        testIndentInFile("testfiles/indent/arrays6.php");
    }

    public void testArrays7() throws Exception{
        testIndentInFile("testfiles/indent/arrays7.php");
    }

    public void testArrays8() throws Exception{
        testIndentInFile("testfiles/indent/arrays8.php");
    }

    public void testArrays9() throws Exception{
        testIndentInFile("testfiles/indent/arrays9.php");
    }

    public void testArrays10() throws Exception{
        testIndentInFile("testfiles/indent/arrays10.php");
    }

    public void testArrays11() throws Exception{
        testIndentInFile("testfiles/indent/arrays11.php");
    }

    public void testArrays12() throws Exception{
        testIndentInFile("testfiles/indent/arrays12.php");
    }

    public void testArrays13() throws Exception{
        testIndentInFile("testfiles/indent/arrays13.php");
    }

    public void testArrays14() throws Exception{
        testIndentInFile("testfiles/indent/arrays14.php");
    }

    public void testArrays15() throws Exception{
        testIndentInFile("testfiles/indent/arrays15.php");
    }

    public void testArrays16() throws Exception{
        testIndentInFile("testfiles/indent/arrays16.php");
    }

    public void testArrays17() throws Exception{
        testIndentInFile("testfiles/indent/arrays17.php");
    }

    public void testArrays18() throws Exception{
        testIndentInFile("testfiles/indent/arrays18.php");
    }

    public void testArrays19() throws Exception{
        testIndentInFile("testfiles/indent/arrays19.php");
    }

    public void testArrays20() throws Exception{
        testIndentInFile("testfiles/indent/arrays20.php");
    }

    public void testArrays21() throws Exception{
        testIndentInFile("testfiles/indent/arrays21.php");
    }

    public void testArrays22() throws Exception{
        testIndentInFile("testfiles/indent/arrays22.php");
    }

    public void testArrays23() throws Exception{
        testIndentInFile("testfiles/indent/arrays23.php");
    }

    public void testArrays24() throws Exception{
        testIndentInFile("testfiles/indent/arrays24.php");
    }

    public void testArrays25() throws Exception{
        testIndentInFile("testfiles/indent/arrays25.php");
    }

    public void testArrays26() throws Exception{
        testIndentInFile("testfiles/indent/arrays26.php");
    }

    public void testArrays27() throws Exception{
        testIndentInFile("testfiles/indent/arrays27.php");
    }

    public void testArrays28() throws Exception{
        testIndentInFile("testfiles/indent/arrays28.php");
    }

    public void testArrays29() throws Exception{
        testIndentInFile("testfiles/indent/arrays29.php");
    }

    public void testShortArrays1() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays1.php");
    }

    public void testShortArrays2() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays2.php");
    }

    public void testShortArrays3() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays3.php");
    }

    public void testShortArrays4() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays4.php");
    }

    public void testShortArrays5() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays5.php");
    }

    public void testShortArrays6() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays6.php");
    }

    public void testShortArrays7() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays7.php");
    }

    public void testShortArrays8() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays8.php");
    }

    public void testShortArrays9() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays9.php");
    }

    public void testShortArrays10() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays10.php");
    }

    public void testShortArrays11() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays11.php");
    }

    public void testShortArrays12() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays12.php");
    }

    public void testShortArrays13() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays13.php");
    }

    public void testShortArrays14() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays14.php");
    }

    public void testShortArrays15() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays15.php");
    }

    public void testShortArrays16() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays16.php");
    }

    public void testShortArrays17() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays17.php");
    }

    public void testShortArrays18() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays18.php");
    }

    public void testShortArrays19() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays19.php");
    }

    public void testShortArrays20() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays20.php");
    }

    public void testShortArrays21() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays21.php");
    }

    public void testShortArrays22() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays22.php");
    }

    public void testShortArrays23() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays23.php");
    }

    public void testShortArrays24() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays24.php");
    }

    public void testShortArrays25() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays25.php");
    }

    public void testShortArrays26() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays26.php");
    }

    public void testShortArrays27() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays27.php");
    }

    public void testShortArrays28() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays28.php");
    }

    public void testShortArrays29() throws Exception{
        testIndentInFile("testfiles/indent/shortArrays29.php");
    }

    public void test157137() throws Exception{
        testIndentInFile("testfiles/indent/issue157137.php");
    }

    public void test162586() throws Exception{
        testIndentInFile("testfiles/indent/issue162586.php");
    }

    public void test176061() throws Exception{
        testIndentInFile("testfiles/indent/issue176061.php");
    }

    public void test166552() throws Exception{
        testIndentInFile("testfiles/indent/issue166552.php");
    }

    public void test168908() throws Exception{
        testIndentInFile("testfiles/indent/issue168908.php");
    }

   public void test173979_1_stableFixed() throws Exception {
        testIndentInFile("testfiles/indent/issue173979_1.php");
    }

    public void test173979_2() throws Exception {
        testIndentInFile("testfiles/indent/issue173979_2.php");
    }

    public void test175118_01() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_01.php");
    }

    public void test175118_02() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_02.php");
    }

    public void test175118_03() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_03.php");
    }

    public void test175118_04() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_04.php");
    }

    public void test175118_05() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_05.php");
    }

    public void test175118_06() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_06.php");
    }

    public void test175118_07() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_07.php");
    }

    public void test175118_08() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_08.php");
    }

    public void test175118_09() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_09.php");
    }

    public void test175118_10() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_10.php");
    }

    public void test175118_11() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_11.php");
    }

    public void test175118_12() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_12.php");
    }

    public void test175118_13() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_13.php");
    }

    public void test175118_14() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_14.php");
    }

    public void test175118_15() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_15.php");
    }

    public void test175118_16() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_16.php");
    }

    public void test175118_17() throws Exception {
        testIndentInFile("testfiles/indent/issue175118_17.php");
    }

    public void test175437_1() throws Exception {
        testIndentInFile("testfiles/indent/issue175437_1.php");
    }

    public void test175437_2() throws Exception {
        testIndentInFile("testfiles/indent/issue175437_2.php");
    }

    public void test175437_3() throws Exception {
        testIndentInFile("testfiles/indent/issue175437_3.php");
    }

    public void test175437_4() throws Exception {
        testIndentInFile("testfiles/indent/issue175437_4.php");
    }

    public void test175437_5() throws Exception {
        testIndentInFile("testfiles/indent/issue175437_5.php");
    }

    public void test175437_6() throws Exception {
        testIndentInFile("testfiles/indent/issue175437_6.php");
    }

    public void test175437_7() throws Exception {
        testIndentInFile("testfiles/indent/issue175437_7.php");
    }

    public void test175437_8() throws Exception {
        testIndentInFile("testfiles/indent/issue175437_8.php");
    }

    public void test173937_01() throws Exception {
        testIndentInFile("testfiles/indent/issue173937_01.php");
    }

//  need to be fiexed the multi line expressions
//    public void test175437_9() throws Exception {
//        testIndentInFile("testfiles/indent/issue175437_9.php");
//    }
//
//    public void test175437_10() throws Exception {
//        testIndentInFile("testfiles/indent/issue175437_10.php");
//    }

    public void test175437_11() throws Exception {
        testIndentInFile("testfiles/indent/issue175437_11.php");
    }

    public void test175437_12() throws Exception {
        testIndentInFile("testfiles/indent/issue175437_12.php");
    }

    public void test175437_13() throws Exception {
        testIndentInFile("testfiles/indent/issue175437_13.php");
    }

    public void testLineComment_1() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_1.php");
    }

    public void testLineComment_2() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_2.php");
    }

    public void testLineComment_3() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_3.php");
    }

    public void testLineComment_4() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_4.php");
    }

    public void testLineComment_175685_1() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_175685_1.php");
    }

    public void testLineComment_175685_2() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_175685_2.php");
    }

    public void testLineComment_175685_3() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_175685_3.php");
    }

    public void testLineComment_175685_4() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_175685_4.php");
    }

    public void testLineComment_175685_5() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_175685_5.php");
    }

    public void testLineComment_175685_6() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_175685_6.php");
    }

    public void testLineComment_175685_7() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_175685_7.php");
    }

    public void testLineComment_175685_8() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_175685_8.php");
   }

    public void testLineComment_175685_9() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_175685_9.php");
    }

    public void testLineComment_175685_10() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_175685_10.php");
    }

    public void testLineComment_175685_11() throws Exception {
        testIndentInFile("testfiles/indent/linecomment_175685_11.php");
    }

    public void testPhpInHtml_01() throws Exception {
        testIndentInFile("testfiles/indent/phpInHtml_01.php");
    }

    public void testInitialIndentation_01() throws Exception {
        testIndentInFile("testfiles/indent/initialIndentation_01.php", new IndentPrefs(4, 4), 0);
    }

    public void testInitialIndentation_02() throws Exception {
        testIndentInFile("testfiles/indent/initialIndentation_02.php", new IndentPrefs(4, 4), 4);
    }

    public void testInitialIndentation_03() throws Exception {
        testIndentInFile("testfiles/indent/initialIndentation_03.php", new IndentPrefs(4, 4), 0);
    }

    public void testInitialIndentation_04() throws Exception {
        testIndentInFile("testfiles/indent/initialIndentation_04.php", new IndentPrefs(4, 4), 4);
    }

    public void testObjectOperatorContinue01() throws Exception {
        testIndentInFile("testfiles/indent/objectOperatorContinue_01.php");
    }

    public void testObjectOperatorContinue02() throws Exception {
        testIndentInFile("testfiles/indent/objectOperatorContinue_02.php");
    }

    public void testObjectOperatorContinue03() throws Exception {
        testIndentInFile("testfiles/indent/objectOperatorContinue_03.php");
    }

    public void testObjectOperatorContinue04() throws Exception {
        testIndentInFile("testfiles/indent/objectOperatorContinue_04.php");
    }

    public void testObjectOperatorContinue05() throws Exception {
        testIndentInFile("testfiles/indent/objectOperatorContinue_05.php");
    }

    public void testObjectOperatorContinue06() throws Exception {
        testIndentInFile("testfiles/indent/objectOperatorContinue_06.php");
    }

    public void testObjectOperatorContinue07() throws Exception {
        testIndentInFile("testfiles/indent/objectOperatorContinue_07.php");
    }

    public void testObjectOperatorContinue09() throws Exception {
        testIndentInFile("testfiles/indent/objectOperatorContinue_09.php");
    }

    public void testObjectOperatorContinue10() throws Exception {
        testIndentInFile("testfiles/indent/objectOperatorContinue_10.php");
    }

    public void testObjectOperatorContinue11() throws Exception {
        testIndentInFile("testfiles/indent/objectOperatorContinue_11.php");
    }

    // NETBEANS-4443 PHP 8.0 Support
    public void testNullsafeObjectOperatorContinue01() throws Exception {
        testIndentInFile("testfiles/indent/php80/nullsafeObjectOperatorContinue_01.php");
    }

    public void testNullsafeObjectOperatorContinue02() throws Exception {
        testIndentInFile("testfiles/indent/php80/nullsafeObjectOperatorContinue_02.php");
    }

    public void testNullsafeObjectOperatorContinue03() throws Exception {
        testIndentInFile("testfiles/indent/php80/nullsafeObjectOperatorContinue_03.php");
    }

    public void testNullsafeObjectOperatorContinue04() throws Exception {
        testIndentInFile("testfiles/indent/php80/nullsafeObjectOperatorContinue_04.php");
    }

    public void testNullsafeObjectOperatorContinue05() throws Exception {
        testIndentInFile("testfiles/indent/php80/nullsafeObjectOperatorContinue_05.php");
    }

    public void testNullsafeObjectOperatorContinue06() throws Exception {
        testIndentInFile("testfiles/indent/php80/nullsafeObjectOperatorContinue_06.php");
    }

    public void testNullsafeObjectOperatorContinue07() throws Exception {
        testIndentInFile("testfiles/indent/php80/nullsafeObjectOperatorContinue_07.php");
    }

    public void testNullsafeObjectOperatorContinue09() throws Exception {
        testIndentInFile("testfiles/indent/php80/nullsafeObjectOperatorContinue_09.php");
    }

    public void testNullsafeObjectOperatorContinue10() throws Exception {
        testIndentInFile("testfiles/indent/php80/nullsafeObjectOperatorContinue_10.php");
    }

    public void testNullsafeObjectOperatorContinue11() throws Exception {
        testIndentInFile("testfiles/indent/php80/nullsafeObjectOperatorContinue_11.php");
    }

    public void test174835_01()throws Exception {
        testIndentInFile("testfiles/indent/issue174835_01.php");
    }

    public void test178542_01()throws Exception {
        testIndentInFile("testfiles/indent/issue178542_01.php");
    }

    public void test178542_02()throws Exception {
        testIndentInFile("testfiles/indent/issue178542_02.php");
    }

    public void test178542_03()throws Exception {
        testIndentInFile("testfiles/indent/issue178542_03.php");
    }

    public void testSwitch_01()throws Exception {
        testIndentInFile("testfiles/indent/switch_01.php");
    }

    public void testSwitch_02()throws Exception {
        testIndentInFile("testfiles/indent/switch_02.php");
    }

    public void testSwitch_03()throws Exception {
        testIndentInFile("testfiles/indent/switch_03.php");
    }

    public void testSwitch_04()throws Exception {
        testIndentInFile("testfiles/indent/switch_04.php");
    }

    public void testSwitch_05()throws Exception {
        testIndentInFile("testfiles/indent/switch_05.php");
    }

    public void testSwitch_06()throws Exception {
        testIndentInFile("testfiles/indent/switch_06.php");
    }

    public void testSwitch_07()throws Exception {
        testIndentInFile("testfiles/indent/switch_07.php");
    }

    public void testSwitch_08()throws Exception {
        testIndentInFile("testfiles/indent/switch_08.php");
    }

    public void testSwitch_09()throws Exception {
        testIndentInFile("testfiles/indent/switch_09.php");
    }

    public void testIssue197583_01() throws Exception {
        testIndentInFile("testfiles/indent/issue197583_01.php");
    }

    public void testIssue197583_02() throws Exception {
        testIndentInFile("testfiles/indent/issue197583_02.php");
    }

    public void testIssue179522_01() throws Exception {
        testIndentInFile("testfiles/indent/issue179522_01.php");
    }

    public void testIssue179522_02() throws Exception {
        testIndentInFile("testfiles/indent/issue179522_02.php");
    }

    public void testIssue179522_03() throws Exception {
        testIndentInFile("testfiles/indent/issue179522_03.php");
    }

    public void testIssue179522_04() throws Exception {
        testIndentInFile("testfiles/indent/issue179522_04.php");
    }

    public void testIssue179522_05() throws Exception {
        testIndentInFile("testfiles/indent/issue179522_05.php");
    }

    public void testIssue201330_01() throws Exception {
        testIndentInFile("testfiles/indent/issue201330_01.php");
    }

    public void testIssue201330_02() throws Exception {
        testIndentInFile("testfiles/indent/issue201330_02.php");
    }

    public void testIssue201330_03() throws Exception {
        testIndentInFile("testfiles/indent/issue201330_03.php");
    }

    public void testIssue201330_04() throws Exception {
        testIndentInFile("testfiles/indent/issue201330_04.php");
    }

    public void testIssue201285_01() throws Exception {
        testIndentInFile("testfiles/indent/issue201285_01.php");
    }

    public void testIssue201285_02() throws Exception {
        testIndentInFile("testfiles/indent/issue201285_02.php");
    }

    public void testIssue202151() throws Exception {
        testIndentInFile("testfiles/indent/issue202151.php");
    }

    public void testIssue178024_01() throws Exception {
        testIndentInFile("testfiles/indent/issue178024_01.php");
    }

    public void testIssue178024_02() throws Exception {
        testIndentInFile("testfiles/indent/issue178024_02.php");
    }

    public void testIssue178024_03() throws Exception {
        testIndentInFile("testfiles/indent/issue178024_03.php");
    }

    public void testIssue178024_04() throws Exception {
        testIndentInFile("testfiles/indent/issue178024_04.php");
    }

    public void testIssue178024_05() throws Exception {
        testIndentInFile("testfiles/indent/issue178024_05.php");
    }

    public void testIssue178024_06() throws Exception {
        testIndentInFile("testfiles/indent/issue178024_06.php");
    }

    public void testIssue203389() throws Exception {
        testIndentInFile("testfiles/indent/issue203389.php");
    }

    public void testIssue222753_01() throws Exception {
        testIndentInFile("testfiles/indent/issue222753_01.php");
    }

    public void testIssue222753_02() throws Exception {
        testIndentInFile("testfiles/indent/issue222753_02.php");
    }

    public void testIssue222753_03() throws Exception {
        testIndentInFile("testfiles/indent/issue222753_03.php");
    }

    public void testIssue191896_01() throws Exception {
        testIndentInFile("testfiles/indent/issue191896_01.php");
    }

    public void testIssue191896_02() throws Exception {
        testIndentInFile("testfiles/indent/issue191896_02.php");
    }

    public void testIssue191896_03() throws Exception {
        testIndentInFile("testfiles/indent/issue191896_03.php");
    }

    public void testIssue191896_04() throws Exception {
        testIndentInFile("testfiles/indent/issue191896_04.php");
    }

    public void testIssue222980_01() throws Exception {
        testIndentInFile("testfiles/indent/issue222980_01.php");
    }

    public void testIssue222980_02() throws Exception {
        testIndentInFile("testfiles/indent/issue222980_02.php");
    }

    public void testIssue222980_03() throws Exception {
        testIndentInFile("testfiles/indent/issue222980_03.php");
    }

    public void testIssue222980_04() throws Exception {
        testIndentInFile("testfiles/indent/issue222980_04.php");
    }

    public void testIssue222980_05() throws Exception {
        testIndentInFile("testfiles/indent/issue222980_05.php");
    }

    public void testIssue222980_06() throws Exception {
        testIndentInFile("testfiles/indent/issue222980_06.php");
    }

    public void testIssue222980_07() throws Exception {
        testIndentInFile("testfiles/indent/issue222980_07.php");
    }

    public void testIssue222980_08() throws Exception {
        testIndentInFile("testfiles/indent/issue222980_08.php");
    }

    public void testIssue222980_09() throws Exception {
        testIndentInFile("testfiles/indent/issue222980_09.php");
    }

    public void testGroupUse_01() throws Exception {
        testIndentInFile("testfiles/indent/groupUse_01.php");
    }

    public void testGroupUse_02() throws Exception {
        testIndentInFile("testfiles/indent/groupUse_02.php");
    }

    public void testGroupUse_03() throws Exception {
        testIndentInFile("testfiles/indent/groupUse_03.php");
    }

    public void testGroupUse_04() throws Exception {
        testIndentInFile("testfiles/indent/groupUse_04.php");
    }

    public void testGroupUse_05() throws Exception {
        testIndentInFile("testfiles/indent/groupUse_05.php");
    }

    public void testGroupUseConst_01() throws Exception {
        testIndentInFile("testfiles/indent/groupUseConst_01.php");
    }

    public void testGroupUseConst_02() throws Exception {
        testIndentInFile("testfiles/indent/groupUseConst_02.php");
    }

    public void testGroupUseConst_03() throws Exception {
        testIndentInFile("testfiles/indent/groupUseConst_03.php");
    }

    public void testGroupUseFunction_01() throws Exception {
        testIndentInFile("testfiles/indent/groupUseFunction_01.php");
    }

    public void testGroupUseFunction_02() throws Exception {
        testIndentInFile("testfiles/indent/groupUseFunction_02.php");
    }

    public void testGroupUseFunction_03() throws Exception {
        testIndentInFile("testfiles/indent/groupUseFunction_03.php");
    }

    public void testIssue268621_01() throws Exception {
        testIndentInFile("testfiles/indent/issue268621_01.php");
    }

    public void testIssue268621_02() throws Exception {
        testIndentInFile("testfiles/indent/issue268621_02.php");
    }

    public void testIssue268621_03() throws Exception {
        testIndentInFile("testfiles/indent/issue268621_03.php");
    }

    public void testIssue268621_04() throws Exception {
        testIndentInFile("testfiles/indent/issue268621_04.php");
    }

    public void testIssue268621_05() throws Exception {
        testIndentInFile("testfiles/indent/issue268621_05.php");
    }

    public void testIssue268621_06() throws Exception {
        testIndentInFile("testfiles/indent/issue268621_06.php");
    }

    // PHP 8.0
    public void testMatchExpression_01() throws Exception {
        testIndentInFile("testfiles/indent/php80/matchExpression_01.php");
    }

    public void testMatchExpression_02() throws Exception {
        testIndentInFile("testfiles/indent/php80/matchExpression_02.php");
    }

    public void testMatchExpression_03() throws Exception {
        testIndentInFile("testfiles/indent/php80/matchExpression_03.php");
    }

    public void testMatchExpression_04() throws Exception {
        testIndentInFile("testfiles/indent/php80/matchExpression_04.php");
    }

    public void testMatchExpression_05() throws Exception {
        testIndentInFile("testfiles/indent/php80/matchExpression_05.php");
    }

    public void testMatchExpression_06() throws Exception {
        testIndentInFile("testfiles/indent/php80/matchExpression_06.php");
    }

    public void testMatchExpression_07() throws Exception {
        testIndentInFile("testfiles/indent/php80/matchExpression_07.php");
    }

    public void testMatchExpression_08() throws Exception {
        testIndentInFile("testfiles/indent/php80/matchExpression_08.php");
    }

    public void testMatchExpression_09() throws Exception {
        testIndentInFile("testfiles/indent/php80/matchExpression_09.php");
    }

    public void testMatchExpression_10() throws Exception {
        testIndentInFile("testfiles/indent/php80/matchExpression_10.php");
    }

    public void testMatchExpression_11() throws Exception {
        testIndentInFile("testfiles/indent/php80/matchExpression_11.php");
    }

    public void testMatchExpression_12() throws Exception {
        testIndentInFile("testfiles/indent/php80/matchExpression_12.php");
    }

    public void testAttributeSyntax_01() throws Exception {
        testIndentInFile("testfiles/indent/php80/attributeSyntax_01.php");
    }

    public void testAttributeSyntax_02() throws Exception {
        testIndentInFile("testfiles/indent/php80/attributeSyntax_02.php");
    }

    public void testAttributeSyntax_03() throws Exception {
        testIndentInFile("testfiles/indent/php80/attributeSyntax_03.php");
    }

    public void testAttributeSyntax_04() throws Exception {
        testIndentInFile("testfiles/indent/php80/attributeSyntax_04.php");
    }

    public void testAttributeSyntax_05() throws Exception {
        testIndentInFile("testfiles/indent/php80/attributeSyntax_05.php");
    }

    public void testAttributeSyntax_06() throws Exception {
        testIndentInFile("testfiles/indent/php80/attributeSyntax_06.php");
    }

    public void testAttributeSyntax_07() throws Exception {
        testIndentInFile("testfiles/indent/php80/attributeSyntax_07.php");
    }

    public void testAttributeSyntax_08() throws Exception {
        testIndentInFile("testfiles/indent/php80/attributeSyntax_08.php");
    }

    public void testAttributeSyntax_09() throws Exception {
        testIndentInFile("testfiles/indent/php80/attributeSyntax_09.php");
    }

    public void testAttributeSyntax_10() throws Exception {
        testIndentInFile("testfiles/indent/php80/attributeSyntax_10.php");
    }

    public void testAttributeSyntax_11() throws Exception {
        testIndentInFile("testfiles/indent/php80/attributeSyntax_11.php");
    }

    public void testAttributeSyntax_12() throws Exception {
        testIndentInFile("testfiles/indent/php80/attributeSyntax_12.php");
    }

    public void testAttributeSyntax_13() throws Exception {
        testIndentInFile("testfiles/indent/php80/attributeSyntax_13.php");
    }

    public void testAttributeSyntax_14() throws Exception {
        testIndentInFile("testfiles/indent/php80/attributeSyntax_14.php");
    }

    public void testAttributeSyntax_15() throws Exception {
        testIndentInFile("testfiles/indent/php80/attributeSyntax_15.php");
    }

    public void testAttributeSyntax_16() throws Exception {
        testIndentInFile("testfiles/indent/php80/attributeSyntax_16.php");
    }

    public void testConstructorPropertyPromotion_01() throws Exception {
        testIndentInFile("testfiles/indent/php80/constructorPropertyPromotion_01.php");
    }

    public void testConstructorPropertyPromotion_02() throws Exception {
        testIndentInFile("testfiles/indent/php80/constructorPropertyPromotion_02.php");
    }

    public void testConstructorPropertyPromotion_03() throws Exception {
        testIndentInFile("testfiles/indent/php80/constructorPropertyPromotion_03.php");
    }

    public void testConstructorPropertyPromotion_04() throws Exception {
        testIndentInFile("testfiles/indent/php80/constructorPropertyPromotion_04.php");
    }

    public void testConstructorPropertyPromotion_05() throws Exception {
        testIndentInFile("testfiles/indent/php80/constructorPropertyPromotion_05.php");
    }

    public void testConstructorPropertyPromotion_06() throws Exception {
        testIndentInFile("testfiles/indent/php80/constructorPropertyPromotion_06.php");
    }

    public void testConstructorPropertyPromotion_07() throws Exception {
        testIndentInFile("testfiles/indent/php80/constructorPropertyPromotion_07.php");
    }

    public void testConstructorPropertyPromotion_08() throws Exception {
        testIndentInFile("testfiles/indent/php80/constructorPropertyPromotion_08.php");
    }

    public void testEnumerations_01() throws Exception {
        testIndentInFile("testfiles/indent/php81/enumerations_01.php");
    }

    public void testEnumerations_02() throws Exception {
        testIndentInFile("testfiles/indent/php81/enumerations_02.php");
    }

    public void testEnumerations_03() throws Exception {
        testIndentInFile("testfiles/indent/php81/enumerations_03.php");
    }

    public void testEnumerationsWithBackingType_01() throws Exception {
        testIndentInFile("testfiles/indent/php81/enumerationsWithBackingType_01.php");
    }

    public void testEnumerationsWithBackingType_02() throws Exception {
        testIndentInFile("testfiles/indent/php81/enumerationsWithBackingType_02.php");
    }

    public void testEnumerationsWithBackingType_03() throws Exception {
        testIndentInFile("testfiles/indent/php81/enumerationsWithBackingType_03.php");
    }

    public void testGH6731_01() throws Exception {
        testIndentInFile("testfiles/indent/gh6731_01.php");
    }

    public void testGH6731_02() throws Exception {
        testIndentInFile("testfiles/indent/gh6731_02.php");
    }

    public void testGH6731_03() throws Exception {
        testIndentInFile("testfiles/indent/gh6731_03.php");
    }

    public void testGH6731_04() throws Exception {
        testIndentInFile("testfiles/indent/gh6731_04.php");
    }

    public void testGH6731_05() throws Exception {
        testIndentInFile("testfiles/indent/gh6731_05.php");
    }

    public void testGH6731_06() throws Exception {
        testIndentInFile("testfiles/indent/gh6731_06.php");
    }

    public void testGH6731_07() throws Exception {
        testIndentInFile("testfiles/indent/gh6731_07.php");
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    protected void testIndentInFile(String file) throws Exception {
        testIndentInFile(file, null, 0);
    }

    protected void testIndentInFile(String file, IndentPrefs indentPrefs, int initialIndent) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);
        String source = readFile(fo);

        int sourcePos = source.indexOf('^');
        assertNotNull(sourcePos);
        String sourceWithoutMarker = source.substring(0, sourcePos) + source.substring(sourcePos+1);
        Formatter formatter = getFormatter(indentPrefs);

        JEditorPane ta = getPane(sourceWithoutMarker);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePos);
        BaseDocument doc = (BaseDocument) ta.getDocument();
        if (formatter != null) {
            configureIndenters(doc, formatter, true);
        }

        setupDocumentIndentation(doc, indentPrefs);


        Map<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, initialIndent);
        if (indentPrefs != null) {
            options.put(FmtOptions.INDENT_SIZE, indentPrefs.getIndentation());
        }
        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        for (Map.Entry<String, Object> entry : options.entrySet()) {
            String option = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Integer) {
                prefs.putInt(option, ((Integer) value).intValue());
            } else if (value instanceof String) {
                prefs.put(option, (String) value);
            } else if (value instanceof Boolean) {
                prefs.put(option, ((Boolean) value).toString());
            } else if (value instanceof CodeStyle.BracePlacement) {
                prefs.put(option, ((CodeStyle.BracePlacement) value).name());
            } else if (value instanceof CodeStyle.WrapStyle) {
                prefs.put(option, ((CodeStyle.WrapStyle) value).name());
            }
        }

        runKitAction(ta, DefaultEditorKit.insertBreakAction, "\n");

        doc.getText(0, doc.getLength());
        doc.insertString(caret.getDot(), "^", null);

        String target = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, target, false, ".indented");
    }
}
