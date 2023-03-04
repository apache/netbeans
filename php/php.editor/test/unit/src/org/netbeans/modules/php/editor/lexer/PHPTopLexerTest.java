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

package org.netbeans.modules.php.editor.lexer;

import java.io.File;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Petr Pisl
 */
public class PHPTopLexerTest extends PHPLexerTestBase {

    public PHPTopLexerTest(String testName) {
        super(testName);
    }

    public void testNoPHP01() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<html>", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "<html>");
    }

    public void testNoPHP02() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<html>\n" +
                "   <", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "<html>\n   <");
    }

    public void testNoPHP03() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<html>\n" +
                "   <?", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "<html>\n   ");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?");
    }

    public void testPHPStartDelimiter() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<html>\n" +
                "   <? ", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "<html>\n   ");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, " ");
    }

    public void testPHP01() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<html>\n" +
                "   <? \n" +
                "   echo 'Test';\n" +
                "   ?>", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "<html>\n   ");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, " \n   echo 'Test';\n   ");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_CLOSE_DELIMITER, "?>");
    }

    public void testPHP02() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<html>\n" +
                "   <? \n" +
                "   echo 'Test';\n" +
                "   ?>\n" +
                "</html>", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "<html>\n   ");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, " \n   echo 'Test';\n   ");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_CLOSE_DELIMITER, "?>");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "\n</html>");
    }

    public void testPHP03() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<html>\n" +
                "   <? \n" +
                "   echo '?>';\n" +
                "   ?>\n" +
                "</html>", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "<html>\n   ");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, " \n   echo '?>';\n   ");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_CLOSE_DELIMITER, "?>");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "\n</html>");
    }

    public void testPHP04() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<html>\n" +
                "   <? \n" +
                "   echo '\\\'?>';\n" +
                "   ?>\n" +
                "</html>", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "<html>\n   ");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, " \n   echo '\\\'?>';\n   ");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_CLOSE_DELIMITER, "?>");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "\n</html>");
    }

    public void testPHP05() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<html>\n" +
                "   <? \n" +
                "   echo '\\\'?>\\\'';\n" +
                "   ?>\n" +
                "</html>", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "<html>\n   ");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, " \n   echo '\\\'?>\\\'';\n   ");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_CLOSE_DELIMITER, "?>");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "\n</html>");
    }

    public void testPHP06() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<html>\n" +
                "   <? \n" +
                "   echo '\"?>';\n" +
                "   ?>\n" +
                "</html>", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "<html>\n   ");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, " \n   echo '\"?>';\n   ");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_CLOSE_DELIMITER, "?>");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "\n</html>");
    }

    public void testPHP07() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<html>\n" +
                "   <? \n" +
                "   echo \"?>\";\n" +
                "   ?>\n" +
                "</html>", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "<html>\n   ");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, " \n   echo \"?>\";\n   ");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_CLOSE_DELIMITER, "?>");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "\n</html>");
    }

    public void testPHP08() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<html>\n" +
                "   <? \n" +
                "   echo \"\\\"?>\";\n" +
                "   ?>\n" +
                "</html>", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "<html>\n   ");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, " \n   echo \"\\\"?>\";\n   ");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_CLOSE_DELIMITER, "?>");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "\n</html>");
    }

    public void testPHP09() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<html>\n" +
                "   <? \n" +
                "   echo \"\\\"'?>'\";\n" +
                "   ?>\n" +
                "</html>", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "<html>\n   ");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, " \n   echo \"\\\"'?>'\";\n   ");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_CLOSE_DELIMITER, "?>");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "\n</html>");
    }

    public void testPHP10() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<html>\n" +
                "   <?php\n" +
                "   echo 'Test';\n" +
                "   ?>\n" +
                "</html>", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "<html>\n   ");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?php");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, "\n   echo 'Test';\n   ");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_CLOSE_DELIMITER, "?>");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "\n</html>");
    }

    public void testPHPTopParser01() throws Exception {
        performTest("lexer/test01");
    }

    public void testPHPTopParser02() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText(
                "<?echo 'ahoj';?>", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, "echo 'ahoj';");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_CLOSE_DELIMITER, "?>");
    }

    public void testPHPTopParser03() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText(
                "<?ppp 'ahoj';?>", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, "ppp 'ahoj';");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_CLOSE_DELIMITER, "?>");
    }

    public void testPHPTopParser04() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText(
                "<?pppp 'ahoj';?>", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, "pppp 'ahoj';");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_CLOSE_DELIMITER, "?>");
    }

    public void testPHPTopParser05() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText(
                "<html><?ppp 'ahoj';?>", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "<html>");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, "ppp 'ahoj';");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_CLOSE_DELIMITER, "?>");
    }

    public void testPHPTopParser06() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText(
                "<html><?php", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "<html>");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?php");
    }

    public void testPHPTopParser07() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText(
                "<html><?php", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "<html>");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?php");
    }

    public void testPHPTopParser08() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText(
                "<html><?phpecho", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "<html>");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?php");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, "echo");
    }

    public void testPHPTopParser09() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText(
                "<html><?ph", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_HTML, "<html>");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?ph");
    }

    public void testHereDoc01() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText(
                "<?php\n" +
                "   echo <", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?php");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, "\n   echo <");
    }

    public void testHereDoc02() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText(
                "<?php\n" +
                "   echo <<", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?php");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, "\n   echo <<");
    }

    public void testHereDoc03() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText(
                "<?php\n" +
                "   echo <<<", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?php");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, "\n   echo <<<");
    }

    public void testHereDoc04() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText(
                "<?php\n" +
                "   echo <<<H", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?php");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, "\n   echo <<<H");
    }

    public void testHereDoc05() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText(
                "<?php\n" +
                "   echo <<<HEREDOC", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?php");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, "\n   echo <<<HEREDOC");
    }

    public void testHereDoc06() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText(
                "<?php\n" +
                "   echo <<<HEREDOC\n", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?php");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, "\n   echo <<<HEREDOC\n");
    }

    public void testHereDoc07() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText(
                "<?php\n" +
                "   echo <<<HEREDOC\n" +
                "Test", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?php");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, "\n   echo <<<HEREDOC\nTest");
    }

    public void testHereDoc08() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText(
                "<?php\n" +
                "   echo <<<HEREDOC\n" +
                "Test\n", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?php");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, "\n   echo <<<HEREDOC\nTest\n");
    }

    public void testHereDoc09() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText(
                "<?php\n" +
                "   echo <<<HEREDOC\n" +
                "Test\n" +
                "HEREDOC", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?php");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, "\n   echo <<<HEREDOC\nTest\nHEREDOC");
    }

    public void testHereDoc10() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText(
                "<?php\n" +
                "   echo <<<HEREDOC\n" +
                "Test\n" +
                "HEREDOC;", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?php");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, "\n   echo <<<HEREDOC\nTest\nHEREDOC;");
    }

    public void testHereDoc11() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText(
                "<?php\n" +
                "   echo <<<HEREDOC\n" +
                "Test\n" +
                "HEREDOC;\n" +
                "?>", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?php");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, "\n   echo <<<HEREDOC\nTest\nHEREDOC;\n");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_CLOSE_DELIMITER, "?>");
    }

    public void testHereDoc12() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText(
                "<?php\n" +
                "   echo <<<HEREDOC\n" +
                "Te?>st\n" +
                "HEREDOC;\n" +
                "?>", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?php");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, "\n   echo <<<HEREDOC\nTe?>st\nHEREDOC;\n");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_CLOSE_DELIMITER, "?>");
    }

    public void testHereDoc13() throws Exception {
        performTest("lexer/heredoc00");
    }

    public void testHereDoc14() throws Exception {
        performTest("lexer/heredoc01");
    }

    public void testHereDoc15() throws Exception {
        performTest("lexer/heredoc_002");
    }

    public void testHereDoc16() throws Exception {
        performTest("lexer/heredoc_003");
    }

    public void testHereDoc17() throws Exception {
        performTest("lexer/heredoc_004");
    }

    public void testHereDoc18() throws Exception {
        performTest("lexer/heredoc_005");
    }

    public void testHereDoc19() throws Exception {
        performTest("lexer/heredoc_006");
    }

    public void testHereDoc20() throws Exception {
        performTest("lexer/heredoc_007");
    }

    public void testHereDoc21() throws Exception {
        performTest("lexer/heredoc_008");
    }

    public void testHereDoc22() throws Exception {
        performTest("lexer/heredoc_009");
    }

    public void testHereDoc23() throws Exception {
        performTest("lexer/heredoc_010");
    }

    public void testHereDoc24() throws Exception {
        performTest("lexer/heredoc_011");
    }

    public void testHereDoc25() throws Exception {
        performTest("lexer/heredoc_012");
    }

    public void testHereDoc26() throws Exception {
        performTest("lexer/heredoc_013");
    }

    public void testHereDoc27() throws Exception {
        performTest("lexer/heredoc_014");
    }

    public void testHereDoc28() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText(
                "<?php\n" +
                "   echo <<<\"HEREDOC\"\n" +
                "Test\n" +
                "HEREDOC;\n" +
                "?>", PHPTopTokenId.language());
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_OPEN_DELIMITER, "<?php");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP, "\n   echo <<<\"HEREDOC\"\nTest\nHEREDOC;\n");
        PHPLexerUtils.next(ts, PHPTopTokenId.T_PHP_CLOSE_DELIMITER, "?>");
    }

    public void testHereDoc29() throws Exception {
        performTest("lexer/heredoc_015");
    }

    public void testNowDoc01() throws Exception {
        performTest("lexer/nowdoc01");
    }

    public void testNowDoc02() throws Exception {
        performTest("lexer/nowdoc02");
    }


    public void testNowDoc03() throws Exception {
        performTest("lexer/nowdoc_000");
    }


    public void testNowDoc04() throws Exception {
        performTest("lexer/nowdoc_001");
    }

    public void testNowDoc05() throws Exception {
        performTest("lexer/nowdoc_002");
    }

    public void testNowDoc06() throws Exception {
        performTest("lexer/nowdoc_003");
    }

    public void testNowDoc07() throws Exception {
        performTest("lexer/nowdoc_004");
    }

    public void testNowDoc08() throws Exception {
        performTest("lexer/nowdoc_005");
    }

    public void testNowDoc09() throws Exception {
        performTest("lexer/nowdoc_006");
    }

    public void testNowDoc10() throws Exception {
        performTest("lexer/nowdoc_007");
    }

    public void testNowDoc11() throws Exception {
        performTest("lexer/nowdoc_008");
    }

    public void testNowDoc12() throws Exception {
        performTest("lexer/nowdoc_009");
    }

    public void testNowDoc13() throws Exception {
        performTest("lexer/nowdoc_010");
    }

    public void testNowDoc14() throws Exception {
        performTest("lexer/nowdoc_011");
    }

    public void testNowDoc15() throws Exception {
        performTest("lexer/nowdoc_012");
    }

    public void testNowDoc16() throws Exception {
        performTest("lexer/nowdoc_013");
    }

    public void testNowDoc17() throws Exception {
        performTest("lexer/nowdoc_014");
    }

    public void testNowDoc18() throws Exception {
        performTest("lexer/nowdoc_015");
    }

    @Override
    protected String getTestResult(String filename) throws Exception {
        String content = PHPLexerUtils.getFileContent(new File(getDataDir(), "testfiles/" + filename + ".php"));
        TokenSequence<?> ts = PHPLexerUtils.seqForText(content, PHPTopTokenId.language());
        return createResult(ts);
    }
}
