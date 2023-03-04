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
package org.netbeans.modules.textmate.lexer;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.textmate.lexer.TextmateTokenId.LanguageHierarchyImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class TextmateLexerTest extends NbTestCase {
    
    public TextmateLexerTest(String name) {
        super(name);
    }
    
    public void testRestart() throws Exception {
        clearWorkDir();

        FileObject grammar = FileUtil.createData(FileUtil.getConfigRoot(), "Editors/text/test/grammar.json");
        try (OutputStream out = grammar.getOutputStream();
             Writer w = new OutputStreamWriter(out)) {
            w.write("{ \"scopeName\": \"test\", " +
                    " \"patterns\": [\n" +
                    "  { \"name\": \"string.test\",\n" +
                    "    \"begin\": \"x([^x]+)x\",\n" +
                    "    \"end\": \"x\\\\1x\"\n" +
                    "   },\n" +
                    "  { \"name\": \"whitespace.test\",\n" +
                    "    \"match\": \" +\"\n" +
                    "   }\n" +
                    "]}\n");
        }
        grammar.setAttribute(LanguageHierarchyImpl.GRAMMAR_MARK, "test");
        Document doc = new PlainDocument();
        doc.insertString(0, " xaax xbbx\nxccx xaax ", null);
        doc.putProperty(Language.class, new LanguageHierarchyImpl("text/test", "test").language());
        TokenHierarchy<Document> hi = TokenHierarchy.get(doc);
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, " ");
        assertTokenProperties(ts, "test", "whitespace.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, "xaax");
        assertTokenProperties(ts, "test", "string.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, " xbbx\n");
        assertTokenProperties(ts, "test", "string.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, "xccx ");
        assertTokenProperties(ts, "test", "string.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, "xaax");
        assertTokenProperties(ts, "test", "string.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, " ");
        assertTokenProperties(ts, "test", "whitespace.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.UNTOKENIZED, "\n");
        assertFalse(ts.moveNext());
        doc.insertString(8, "b", null);
        ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, " ");
        assertTokenProperties(ts, "test", "whitespace.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, "xaax");
        assertTokenProperties(ts, "test", "string.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, " xbbbx\n");
        assertTokenProperties(ts, "test", "string.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, "xccx ");
        assertTokenProperties(ts, "test", "string.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, "xaax");
        assertTokenProperties(ts, "test", "string.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, " ");
        assertTokenProperties(ts, "test", "whitespace.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.UNTOKENIZED, "\n");
        assertFalse(ts.moveNext());
        doc.insertString(14, "c", null);
        ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, " ");
        assertTokenProperties(ts, "test", "whitespace.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, "xaax");
        assertTokenProperties(ts, "test", "string.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, " xbbbx\n");
        assertTokenProperties(ts, "test", "string.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, "xcccx ");
        assertTokenProperties(ts, "test", "string.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, "xaax");
        assertTokenProperties(ts, "test", "string.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, " ");
        assertTokenProperties(ts, "test", "whitespace.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.UNTOKENIZED, "\n");
        assertFalse(ts.moveNext());
        doc.insertString(3, "a", null);
        ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, " ");
        assertTokenProperties(ts, "test", "whitespace.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, "xaaax");
        assertTokenProperties(ts, "test", "string.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, " xbbbx\n");
        assertTokenProperties(ts, "test", "string.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, "xcccx xaax \n");
        assertFalse(ts.moveNext());
        doc.remove(3, 1);
        ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, " ");
        assertTokenProperties(ts, "test", "whitespace.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, "xaax");
        assertTokenProperties(ts, "test", "string.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, " xbbbx\n");
        assertTokenProperties(ts, "test", "string.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, "xcccx ");
        assertTokenProperties(ts, "test", "string.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, "xaax");
        assertTokenProperties(ts, "test", "string.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, " ");
        assertTokenProperties(ts, "test", "whitespace.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.UNTOKENIZED, "\n");
        assertFalse(ts.moveNext());
    }

    public void testNETBEANS2430() throws Exception {
        FileObject grammar = FileUtil.createData(FileUtil.getConfigRoot(), "Editors/text/test/grammar.json");
        try (OutputStream out = grammar.getOutputStream();
             Writer w = new OutputStreamWriter(out)) {
            w.write("{ \"scopeName\": \"test\", " +
                    " \"patterns\": [\n" +
                    "  { \"name\": \"string.test\",\n" +
                    "    \"begin\": \"x([^x]+)x\",\n" +
                    "    \"end\": \"x\\\\1x\"\n" +
                    "   },\n" +
                    "  { \"name\": \"whitespace.test\",\n" +
                    "    \"match\": \" +\"\n" +
                    "   }\n" +
                    "]}\n");
        }
        grammar.setAttribute(LanguageHierarchyImpl.GRAMMAR_MARK, "test");
        Document doc = new PlainDocument();
        doc.insertString(0, " xaax xbbx\nxccx xaax ", null);
        doc.putProperty(Language.class, new LanguageHierarchyImpl("text/test", "test").language());
        TokenHierarchy hi = TokenHierarchy.get(doc);
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, " ");
        assertTokenProperties(ts, "test", "whitespace.test");
        //do not fully lex
        doc.insertString(3, "a", null);
        ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, " ");
        assertTokenProperties(ts, "test", "whitespace.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, "xaaax");
        assertTokenProperties(ts, "test", "string.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, " xbbx\n");
        assertTokenProperties(ts, "test", "string.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, "xccx xaax \n");
        assertFalse(ts.moveNext());
    }

    public void testUTF8() throws Exception {
        clearWorkDir();

        FileObject grammar = FileUtil.createData(FileUtil.getConfigRoot(), "Editors/text/test/grammar.json");
        try (OutputStream out = grammar.getOutputStream();
             Writer w = new OutputStreamWriter(out)) {
            w.write("{ \"scopeName\": \"test\", " +
                    " \"patterns\": [\n" + 
                    "  { \"name\": \"ident.test\",\n" +
                    "    \"match\": \"[^ ]+\"\n" +
                    "   }\n" +
                    "]}\n");
        }
        grammar.setAttribute(LanguageHierarchyImpl.GRAMMAR_MARK, "test");
        Document doc = new PlainDocument();
        doc.insertString(0, " večerníček večerníček  ", null);
        doc.putProperty(Language.class, new LanguageHierarchyImpl("text/test", "test").language());
        TokenHierarchy<Document> hi = TokenHierarchy.get(doc);
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, " ");
        assertTokenProperties(ts, "test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, "večerníček");
        assertTokenProperties(ts, "test", "ident.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, " ");
        assertTokenProperties(ts, "test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, "večerníček");
        assertTokenProperties(ts, "test", "ident.test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, "  ");
        assertTokenProperties(ts, "test");
        LexerTestUtilities.assertNextTokenEquals(ts, TextmateTokenId.TEXTMATE, "\n");
        assertFalse(ts.moveNext());
    }

    private void assertTokenProperties(TokenSequence<?> ts, String... properties) {
        assertEquals(ts.token().getProperty("categories"), Arrays.asList(properties));
    }

}
