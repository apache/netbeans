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

import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author petr
 */
public class PHPLexerTest extends PHPLexerTestBase {

    public PHPLexerTest(String testName) {
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

    public void testNoPHPg() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<html>", PHPTokenId.language());
        PHPLexerUtils.next(ts, PHPTokenId.T_INLINE_HTML, "<html>");
    }

    public void testOpenTag() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<?php ?>", PHPTokenId.language());
        PHPLexerUtils.next(ts, PHPTokenId.PHP_OPENTAG, "<?php");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, " ");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testOpenTag2() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<?php \t ?>", PHPTokenId.language());
        PHPLexerUtils.next(ts, PHPTokenId.PHP_OPENTAG, "<?php");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, " \t ");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testLineComment1() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<?php // comment\n$a?>", PHPTokenId.language());
        PHPLexerUtils.next(ts, PHPTokenId.PHP_OPENTAG, "<?php");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, " ");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_LINE_COMMENT, "//");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_LINE_COMMENT, " comment\n");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_VARIABLE, "$a");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testLineComment2() throws Exception{
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<?php # comment\n$a?>", PHPTokenId.language());
        PHPLexerUtils.next(ts, PHPTokenId.PHP_OPENTAG, "<?php");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, " ");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_LINE_COMMENT, "#");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_LINE_COMMENT, " comment\n");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_VARIABLE, "$a");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testLineComment3() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<?php // comment", PHPTokenId.language());
        PHPLexerUtils.next(ts, PHPTokenId.PHP_OPENTAG, "<?php");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, " ");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_LINE_COMMENT, "//");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_LINE_COMMENT, " comment");
    }

    public void testPHPCommnet1() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<?php/*$a*/$b?>", PHPTokenId.language());
        PHPLexerUtils.next(ts, PHPTokenId.PHP_OPENTAG, "<?php");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_COMMENT_START, "/*");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_COMMENT, "$a");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_COMMENT_END, "*/");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_VARIABLE, "$b");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testPHPCommnet2() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<?php/***\n**$a***\n****/$b?>", PHPTokenId.language());
        PHPLexerUtils.next(ts, PHPTokenId.PHP_OPENTAG, "<?php");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_COMMENT_START, "/*");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_COMMENT, "**\n**$a***\n***");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_COMMENT_END, "*/");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_VARIABLE, "$b");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testPHPCommnet3() throws Exception {
        // test unfinished comment at the end of file
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<?php/*a**\n**$a***\n***hello\nword", PHPTokenId.language());
        PHPLexerUtils.next(ts, PHPTokenId.PHP_OPENTAG, "<?php");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_COMMENT_START, "/*");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_COMMENT, "a**\n**$a***\n***hello\nword");
    }

    public void testPHPCommnet4() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<?php/*comment1*/echo/*comment2*/?>", PHPTokenId.language());
        PHPLexerUtils.next(ts, PHPTokenId.PHP_OPENTAG, "<?php");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_COMMENT_START, "/*");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_COMMENT, "comment1");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_COMMENT_END, "*/");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_ECHO, "echo");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_COMMENT_START, "/*");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_COMMENT, "comment2");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_COMMENT_END, "*/");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testPHPCommnet5() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<?php\n/*\nRevision 1.6  2007/01/07 18:41:01\n*/echo?>", PHPTokenId.language());
        PHPLexerUtils.next(ts, PHPTokenId.PHP_OPENTAG, "<?php");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, "\n");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_COMMENT_START, "/*");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_COMMENT, "\nRevision 1.6  2007/01/07 18:41:01\n");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_COMMENT_END, "*/");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_ECHO, "echo");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testPHPCommnet6() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<?php\n/*\nRevision 1.6  2007/01/07 18:41:01 it can be * /\n another text\n*/echo?>", PHPTokenId.language());
        PHPLexerUtils.next(ts, PHPTokenId.PHP_OPENTAG, "<?php");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, "\n");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_COMMENT_START, "/*");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_COMMENT, "\nRevision 1.6  2007/01/07 18:41:01 it can be * /\n another text\n");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_COMMENT_END, "*/");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_ECHO, "echo");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testPHPDocumentor1() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<?php/**\n * Enter description here...\n * @access private\n * @var string $name\n */\nvar $name = \"ahoj\"\n?>", PHPTokenId.language());
        PHPLexerUtils.next(ts, PHPTokenId.PHP_OPENTAG, "<?php");
        PHPLexerUtils.next(ts, PHPTokenId.PHPDOC_COMMENT_START, "/**");
        PHPLexerUtils.next(ts, PHPTokenId.PHPDOC_COMMENT, "\n * Enter description here...\n * @access private\n * @var string $name\n ");
        PHPLexerUtils.next(ts, PHPTokenId.PHPDOC_COMMENT_END, "*/");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, "\n");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_VAR, "var");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, " ");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_VARIABLE, "$name");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, " ");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_OPERATOR, "=");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, " ");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING, "\"ahoj\"");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, "\n");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testPHPDocumentor2() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<?php/**\n * Enter description here...\n * @ppp private\n * @var string $name\n */\nvar $name = \"ahoj\"\n?>", PHPTokenId.language());
        PHPLexerUtils.next(ts, PHPTokenId.PHP_OPENTAG, "<?php");
        PHPLexerUtils.next(ts, PHPTokenId.PHPDOC_COMMENT_START, "/**");
        PHPLexerUtils.next(ts, PHPTokenId.PHPDOC_COMMENT, "\n * Enter description here...\n * @ppp private\n * @var string $name\n ");
        PHPLexerUtils.next(ts, PHPTokenId.PHPDOC_COMMENT_END, "*/");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, "\n");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_VAR, "var");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, " ");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_VARIABLE, "$name");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, " ");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_OPERATOR, "=");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, " ");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING, "\"ahoj\"");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, "\n");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testPHPDocumentor3() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<?php/**\n * Comment 1\n */\nvar $name = \"ahoj\"\n /**\n * Comment 2\n */\nvar $age = 10?>", PHPTokenId.language());
        PHPLexerUtils.next(ts, PHPTokenId.PHP_OPENTAG, "<?php");
        PHPLexerUtils.next(ts, PHPTokenId.PHPDOC_COMMENT_START, "/**");
        PHPLexerUtils.next(ts, PHPTokenId.PHPDOC_COMMENT, "\n * Comment 1\n ");
        PHPLexerUtils.next(ts, PHPTokenId.PHPDOC_COMMENT_END, "*/");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, "\n");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_VAR, "var");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, " ");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_VARIABLE, "$name");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, " ");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_OPERATOR, "=");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, " ");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING, "\"ahoj\"");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, "\n ");
        PHPLexerUtils.next(ts, PHPTokenId.PHPDOC_COMMENT_START, "/**");
        PHPLexerUtils.next(ts, PHPTokenId.PHPDOC_COMMENT, "\n * Comment 2\n ");
        PHPLexerUtils.next(ts, PHPTokenId.PHPDOC_COMMENT_END, "*/");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, "\n");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_VAR, "var");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, " ");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_VARIABLE, "$age");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, " ");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_OPERATOR, "=");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, " ");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_NUMBER, "10");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testPHPDocumentor4() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<?php/**\n This File is free software; you can redistribute it and/or modify\n */?>", PHPTokenId.language());
        PHPLexerUtils.next(ts, PHPTokenId.PHP_OPENTAG, "<?php");
        PHPLexerUtils.next(ts, PHPTokenId.PHPDOC_COMMENT_START, "/**");
        PHPLexerUtils.next(ts, PHPTokenId.PHPDOC_COMMENT, "\n This File is free software; you can redistribute it and/or modify\n ");
        PHPLexerUtils.next(ts, PHPTokenId.PHPDOC_COMMENT_END, "*/");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testPHPDocumentor5() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<?php/**\n This File is free software; \n*   <dd> \"/^word.* /\" => REGEX(^word.*)\n */?>", PHPTokenId.language());
        PHPLexerUtils.next(ts, PHPTokenId.PHP_OPENTAG, "<?php");
        PHPLexerUtils.next(ts, PHPTokenId.PHPDOC_COMMENT_START, "/**");
        PHPLexerUtils.next(ts, PHPTokenId.PHPDOC_COMMENT, "\n This File is free software; \n*   <dd> \"/^word.* /\" => REGEX(^word.*)\n ");
        PHPLexerUtils.next(ts, PHPTokenId.PHPDOC_COMMENT_END, "*/");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    // not termitnated doc
    public void testPHPDocumentor6() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<?php/**\n This File is free software;", PHPTokenId.language());
        PHPLexerUtils.next(ts, PHPTokenId.PHP_OPENTAG, "<?php");
        PHPLexerUtils.next(ts, PHPTokenId.PHPDOC_COMMENT_START, "/**");
        PHPLexerUtils.next(ts, PHPTokenId.PHPDOC_COMMENT, "\n This File is free software;");
    }

    public void testShortOpenTag() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<?php echo \"ahoj\" ?>", PHPTokenId.language());

        PHPLexerUtils.next(ts, PHPTokenId.PHP_OPENTAG, "<?php");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, " ");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_ECHO, "echo");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, " ");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING, "\"ahoj\"");
        PHPLexerUtils.next(ts, PHPTokenId.WHITESPACE, " ");
        PHPLexerUtils.next(ts, PHPTokenId.PHP_CLOSETAG, "?>");
    }

    public void testInlineHtml() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("<html>\n    <head>\n        <title></title>\n    </head>\n    <body>\n        <?php\n          \n        ?>\n    </body>\n</html>", PHPTokenId.language());
        PHPLexerUtils.printTokenSequence(ts, "testInlineHtml"); ts.moveStart();
    }

    public void testHeroDoc_00() throws Exception {
        performTest("lexer/heredoc00");
    }

    public void testHeroDoc_01() throws Exception {
        performTest("lexer/heredoc01");
    }

    public void testIssue138261 () throws Exception {
        performTest("lexer/issue138261");
    }

    public void testIssue144337 () throws Exception {
        performTest("lexer/issue144337");
    }

    public void testIssue198572() throws Exception {
        performTest("lexer/issue198572");
    }

    public void testIssue221484() throws Exception {
        performTest("lexer/issue221484");
    }

    public void testHereDoc_02() throws Exception {
        performTest("lexer/heredoc_02");
    }

    public void testHereDoc_03() throws Exception {
        performTest("lexer/heredoc_03");
    }

    public void testHereDoc_04() throws Exception {
        performTest("lexer/heredoc_04");
    }

    // HEREDOC
    public void testNetBeans1563_01() throws Exception {
        performTest("lexer/netbeans1563_01");
    }

    public void testNetBeans1563_02() throws Exception {
        performTest("lexer/netbeans1563_02");
    }

    public void testNetBeans1563_03() throws Exception {
        performTest("lexer/netbeans1563_03");
    }

    public void testNetBeans1563_04() throws Exception {
        performTest("lexer/netbeans1563_04");
    }

    public void testIssue222092_01() throws Exception {
        performTest("lexer/issue222092_01");
    }

    public void testIssue222092_02() throws Exception {
        performTest("lexer/issue222092_02");
    }

    public void testIssue229960() throws Exception {
        performTest("lexer/issue229960");
    }

    public void testYieldKeyword() throws Exception {
        performTest("lexer/yieldKeyword");
    }

    public void testIssue235973() throws Exception {
        performTest("lexer/issue235973");
    }

}
