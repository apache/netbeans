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
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Pisl
 */
public class PHPDocCommentLexerTest extends NbTestCase {

    public PHPDocCommentLexerTest(String testName) {
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

    public void testSimpleComment() throws Exception{
        TokenSequence<?> ts = PHPLexerUtils.seqForText("comment 1", PHPDocCommentTokenId.language());
        PHPLexerUtils.printTokenSequence(ts, "testSimpleComment"); ts.moveStart();
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_COMMENT, "comment 1");
    }

    public void testCommentTags() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("comment 1\n * @link\n * @name\n * @desc", PHPDocCommentTokenId.language());
        //PHPLexerUtils.printTokenSequence(ts, "testSimpleComment"); ts.moveStart();
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_COMMENT, "comment 1\n * ");
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_ANNOTATION, "@link");
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_COMMENT, "\n * ");
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_ANNOTATION, "@name");
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_COMMENT, "\n * ");
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_ANNOTATION, "@desc");

    }

    public void testNotMatchInput1() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("*   <dd> \"*word\"  => ENDS_WITH(word)\n *   <dd> \"/^word.* /\" => REGEX(^word.*)\n *   <dd> \"word*word\" => REGEX(word.*word)", PHPDocCommentTokenId.language());
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_COMMENT, "*   ");
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_HTML_TAG, "<dd>");
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_COMMENT, " \"*word\"  => ENDS_WITH(word)\n *   ");
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_HTML_TAG, "<dd>");
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_COMMENT, " \"/^word.* /\" => REGEX(^word.*)\n *   ");
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_HTML_TAG, "<dd>");
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_COMMENT, " \"word*word\" => REGEX(word.*word)");
    }

    public void testPropertyTagTags() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText("" +
                "comment 1\n" +
                " * @property int age how old she is\n" +
                " * @property-read string nick readonly property\n" +
                " * @property-write boolean death", PHPDocCommentTokenId.language());
        PHPLexerUtils.printTokenSequence(ts, "testSimpleComment"); ts.moveStart();
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_COMMENT, "comment 1\n * ");
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_ANNOTATION, "@property");
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_COMMENT, " int age how old she is\n * ");
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_ANNOTATION, "@property-read");
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_COMMENT, " string nick readonly property\n * ");
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_ANNOTATION, "@property-write");
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_COMMENT, " boolean death");
    }

    public void testIssue144337() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText(" @", PHPDocCommentTokenId.language());
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_COMMENT, " ");
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_COMMENT, "@");
    }

    public void testHtmlInComment() throws Exception {
        TokenSequence<?> ts = PHPLexerUtils.seqForText(" Some <b>bold</b> text.\n *\n * @param type $param", PHPDocCommentTokenId.language());
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_COMMENT, " Some ");
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_HTML_TAG, "<b>");
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_COMMENT, "bold");
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_HTML_TAG, "</b>");
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_COMMENT, " text.\n *\n * ");
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_ANNOTATION, "@param");
        PHPLexerUtils.next(ts, PHPDocCommentTokenId.PHPDOC_COMMENT, " type $param");
    }

}
