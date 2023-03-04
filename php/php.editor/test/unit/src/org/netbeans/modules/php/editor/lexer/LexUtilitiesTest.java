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

import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.openide.filesystems.FileObject;

/**
 *
 * @author petr
 */
public class LexUtilitiesTest extends PHPLexerTestBase {

    public LexUtilitiesTest(String testName) {
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

    public void testGetMostEmbeddedTokenSequenceLocking() throws Exception {
        FileObject file = getTestFile("testfiles/embeddings.php");
        Document doc = getDocument(file);
        LexUtilities.getMostEmbeddedTokenSequence(doc, 0, true);
        LexUtilities.getMostEmbeddedTokenSequence(doc, 0, false);
    }

    public void testGetMostEmbeddedTokenSequence() throws Exception {
        FileObject file = getTestFile("testfiles/embeddings.php");
        //the file content:
        //<div style="color: red"><? echo "hello"; ?></div>
        //01234567890123456789012345678901234567890123456789
        //0         1         2         3         4
        Document doc = getDocument(file);

        check(doc, 0, HTMLTokenId.language());
        check(doc, 5, HTMLTokenId.language());
        check(doc, 47, HTMLTokenId.language());
        check(doc, 26, PHPTokenId.language());
        check(doc, 40, PHPTokenId.language());
        check(doc, 14, CssTokenId.language());

    }

    private void check(Document doc, int offset, Language l) {
        TokenSequence ts = LexUtilities.getMostEmbeddedTokenSequence(doc, offset, true);
        assertNotNull(ts);
        assertEquals(l, ts.language());
    }

}
