/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.smarty.editor.embedding;

import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.lexer.PHPTopTokenId;
import org.netbeans.modules.php.smarty.editor.gsf.TplLanguage;
import org.netbeans.modules.php.smarty.editor.lexer.TplTokenId;
import org.netbeans.modules.php.smarty.editor.lexer.TplTopTokenId;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class TplEmbeddingProviderTest extends CslTestBase {

    TokenHierarchy th;
    TokenSequence<TplTopTokenId> ts;


    public TplEmbeddingProviderTest(String name) {
        super(name);
    }

    public void testBaseHtmlEmbedding() throws Exception {
        FileObject file = getTestFile("testfiles/embedding/base-file.tpl");
        Document doc = getDocument(file);

        th = TokenHierarchy.get(doc);
        ts = th.tokenSequence(TplTopTokenId.language());

        // hr|ef
        int offset = 5; 
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_HTML);
        assertEquals(HTMLTokenId.ARGUMENT, getHtmlToken(offset).id());

        // {|$variable
        offset = 35;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_SMARTY_OPEN_DELIMITER);
        assertNullEmbeddingForLanguage(HTMLTokenId.language());

        // {$v|ariable
        offset = 37;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_SMARTY);
        assertNullEmbeddingForLanguage(HTMLTokenId.language());

        // <|/a>
        offset = 47;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_HTML);
        assertEquals(HTMLTokenId.TAG_OPEN_SYMBOL, getHtmlToken(offset).id());

        // {p|hp}
        offset = 53;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_SMARTY);
        assertNullEmbeddingForLanguage(HTMLTokenId.language());

        // {php}\n|
        offset = 57;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_PHP);
        assertNullEmbeddingForLanguage(HTMLTokenId.language());

        // <d|iv
        offset = 91;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_HTML);
        assertNullEmbeddingForLanguage(PHPTopTokenId.language());

        // my|DivId
        offset = 100;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_HTML);
        assertEquals(HTMLTokenId.VALUE_CSS, getHtmlToken(offset).id());

        // Bl|ah
        offset = 160;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_HTML);
        assertEquals(HTMLTokenId.SCRIPT, getHtmlToken(offset).id());
    }

    public void testBaseTplEmbedding() throws Exception {
        FileObject file = getTestFile("testfiles/embedding/base-file.tpl");
        Document doc = getDocument(file);

        th = TokenHierarchy.get(doc);
        ts = th.tokenSequence(TplTopTokenId.language());

        // hr|ef
        int offset = 5;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_HTML);
        assertEquals(HTMLTokenId.ARGUMENT, getHtmlToken(offset).id());

        // {|$variable
        offset = 35;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_SMARTY_OPEN_DELIMITER);
        assertNullEmbeddingForLanguage(TplTokenId.language());

        // {$v|ariable
        offset = 37;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_SMARTY);
        assertEquals(getTplToken(offset).id(), TplTokenId.PHP_VARIABLE);

        // <|/a>
        offset = 47;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_HTML);
        assertNullEmbeddingForLanguage(TplTokenId.language());

        // {p|hp}
        offset = 53;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_SMARTY);
        assertEquals(getTplToken(offset).id(), TplTokenId.FUNCTION);

        // {php}\n|
        offset = 57;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_PHP);
        assertNullEmbeddingForLanguage(TplTokenId.language());

        // <d|iv
        offset = 91;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_HTML);
        assertNullEmbeddingForLanguage(TplTokenId.language());

        // my|DivId
        offset = 100;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_HTML);
        assertNullEmbeddingForLanguage(TplTokenId.language());

        // Bl|ah
        offset = 160;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_HTML);
        assertNullEmbeddingForLanguage(TplTokenId.language());
    }

    public void testBasePhpEmbedding() throws Exception {
        FileObject file = getTestFile("testfiles/embedding/base-file.tpl");
        Document doc = getDocument(file);

        th = TokenHierarchy.get(doc);
        ts = th.tokenSequence(TplTopTokenId.language());

        // hr|ef
        int offset = 5;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_HTML);
        assertNullEmbeddingForLanguage(PHPTopTokenId.language());

        // {|$variable
        offset = 35;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_SMARTY_OPEN_DELIMITER);
        assertNullEmbeddingForLanguage(PHPTopTokenId.language());

        // {$v|ariable
        offset = 37;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_SMARTY);
        assertNullEmbeddingForLanguage(PHPTopTokenId.language());

        // <|/a>
        offset = 47;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_HTML);
        assertNullEmbeddingForLanguage(PHPTopTokenId.language());

        // {p|hp}
        offset = 53;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_SMARTY);
        assertNullEmbeddingForLanguage(HTMLTokenId.language());

        // {php}\n|
        offset = 57;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_PHP);
        assertEquals(getPhpToken(offset).id(), PHPTokenId.WHITESPACE);

        // e|cho
        offset = 62;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_PHP);
        assertEquals(getPhpToken(offset).id(), PHPTokenId.PHP_ECHO);

        // any| PHP
        offset = 69;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_PHP);
        assertEquals(getPhpToken(offset).id(), PHPTokenId.WHITESPACE);

        // <d|iv
        offset = 91;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_HTML);
        assertNullEmbeddingForLanguage(PHPTopTokenId.language());

        // my|DivId
        offset = 100;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_HTML);
        assertNullEmbeddingForLanguage(PHPTopTokenId.language());

        // Bl|ah
        offset = 160;
        assertEquals(getTplTopToken(offset).id(), TplTopTokenId.T_HTML);
        assertNullEmbeddingForLanguage(PHPTopTokenId.language());
    }

    private Token<TplTopTokenId> getTplTopToken(int offset) {
        ts.move(offset);
        assertTrue(ts.moveNext());
        return ts.token();
    }

    private Token<HTMLTokenId> getHtmlToken(int offset) {
        TokenSequence<HTMLTokenId> htmlts = ts.embeddedJoined(HTMLTokenId.language());
        assertNotNull(htmlts);
        htmlts.move(offset);
        assertTrue(htmlts.moveNext());
        return htmlts.token();
    }

    private Token<TplTokenId> getTplToken(int offset) {
        TokenSequence<TplTokenId> htmlts = ts.embeddedJoined(TplTokenId.language());
        assertNotNull(htmlts);
        htmlts.move(offset);
        assertTrue(htmlts.moveNext());
        return htmlts.token();
    }

    private Token<PHPTokenId> getPhpToken(int offset) {
        TokenSequence<PHPTokenId> phpts = ts.embeddedJoined(PHPTokenId.languageInPHP());
        assertNotNull(phpts);
        phpts.move(offset);
        assertTrue(phpts.moveNext());
        return phpts.token();
    }

    private void assertNullEmbeddingForLanguage(Language language) {
        TokenSequence htmlts = ts.embeddedJoined(language);
        assertNull(htmlts);
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new TplLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return "text/x-tpl";
    }
}
