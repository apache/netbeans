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

package org.netbeans.modules.spellchecker.bindings.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.spellchecker.spi.language.TokenList;

/**
 *
 * @author Jan Lahoda
 */
public class JavaTokenListTest extends NbTestCase {
    
    public JavaTokenListTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    public void testSimpleWordBroker() throws Exception {
        tokenListTest(
            "/**tes test*/ testt testtt /*testttt*//**testtttt*//**testttttt*/",
            "tes", "test", "testtttt", "testttttt"
        );
    }

    public void testPairTags() throws Exception {
        tokenListTest(
            "/**tes <code>test</code> <pre>testt</pre> <a href='testtt'>testttt</a> testttttt*/",
            "tes", "testttttt"
        );
    }

    public void testSimplewriting() throws Exception {
        tokenListTestWithWriting(
            "/**tes test*/ testt testtt /*testttt*//**testtttt*//**testttttt*/",
            14, "bflmpsvz", 13,
            "testtttt", "testttttt"
        );
    }

    public void testDotDoesNotSeparateWords() throws Exception {
        tokenListTest(
                "/**tes.test*/",
                "tes", "test"
        );
    }
    
    public void testTagHandling() throws Exception {
        tokenListTest(
                "/**@see aba.abb.abc.abd abe @param abf abg abh @author abi abj abk abl\n abm abn @throws abo.abp abq*/",
                "abe", "abg", "abh", "abm", "abn", "abq"
        );
    }
    
    public void testLinkHandling() throws Exception {
        tokenListTest(
                "/**{@link aba abb abc} {abd }abe*/",
                "abd", "abe"
        );
    }

    public void testEntities() throws Exception {
        tokenListTest(
                "/** &gt; &#62; */"
        );
    }
    
    public void testIsIdentifierLike() throws Exception {
        assertTrue(JavaTokenList.isIdentifierLike("JTable"));
        assertTrue(JavaTokenList.isIdentifierLike("getData"));
        assertTrue(JavaTokenList.isIdentifierLike("setTestingData"));

        assertFalse(JavaTokenList.isIdentifierLike("test"));
        assertFalse(JavaTokenList.isIdentifierLike("code"));
        assertFalse(JavaTokenList.isIdentifierLike("data"));
    }

    public void testPositions() throws Exception {
        Document doc = new PlainDocument();
        
        doc.putProperty(Language.class, JavaTokenId.language());
        
        doc.insertString(0, "/**tes test <pre>testt</pre> <a href='testtt'>testttt</a> testttttt*/", null);
        
        TokenList l = new JavaTokenList(doc);
        
        l.setStartOffset(9);
        assertTrue(l.nextWord());
        assertEquals(7, l.getCurrentWordStartOffset());
        assertTrue("test".equals(l.getCurrentWordText().toString()));
    }
    
    private void tokenListTest(String documentContent, String... golden) throws Exception {
        Document doc = new PlainDocument();
        
        doc.putProperty(Language.class, JavaTokenId.language());
        
        doc.insertString(0, documentContent, null);
        
        List<String> words = new ArrayList<String>();
        TokenList l = new JavaTokenList(doc);
        
        l.setStartOffset(0);
        
        while (l.nextWord()) {
            words.add(l.getCurrentWordText().toString());
        }
        
        assertEquals(Arrays.asList(golden), words);
    }

    private void tokenListTestWithWriting(String documentContent, int offset, String text, int startOffset, String... golden) throws Exception {
        Document doc = new PlainDocument();
        
        doc.putProperty(Language.class, JavaTokenId.language());
        
        doc.insertString(0, documentContent, null);
        
        List<String> words = new ArrayList<String>();
        TokenList l = new JavaTokenList(doc);
        
        while (l.nextWord()) {
        }

        doc.insertString(offset, text, null);
        
        l.setStartOffset(startOffset);
        
        while (l.nextWord()) {
            words.add(l.getCurrentWordText().toString());
        }

        assertEquals(Arrays.asList(golden), words);
    }

}
