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
