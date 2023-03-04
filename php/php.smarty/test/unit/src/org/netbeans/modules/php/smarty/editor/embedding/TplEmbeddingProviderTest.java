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
