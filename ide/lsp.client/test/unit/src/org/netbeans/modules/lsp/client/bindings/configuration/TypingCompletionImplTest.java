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
package org.netbeans.modules.lsp.client.bindings.configuration;

import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.junit.Test;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.lsp.client.spi.friend.LanguageConfiguration;
import org.netbeans.modules.editor.lib2.typinghooks.TypingHooksSpiAccessor;
import org.netbeans.modules.textmate.lexer.TextmateTokenId;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor.MutableContext;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import static org.junit.Assert.*;

public class TypingCompletionImplTest {

//    @Test
    public void testBracketCompletion() throws Exception {
        doTest("""
               |
               """,
               LanguageConfiguration.from("{ 'autoClosingPairs': [ { 'open': '(', 'close': ')' } ] }"),
               new Input('(',
                         """
                         (|)
                         """),
               new Input(')',
                         """
                         ()|
                         """));
        doTest("""
               a|
               """,
               LanguageConfiguration.from("{ 'autoClosingPairs': [ { 'open': '(', 'close': ')' } ] }"),
               new Input('(',
                         """
                         a(|)
                         """),
               new Input(')',
                         """
                         a()|
                         """));
        doTest("""
               <!-|
               """,
               LanguageConfiguration.from("{ 'autoClosingPairs': [ { 'open': '<!--', 'close': '-->' } ] }"),
               new Input('-',
                         """
                         <!--|-->
                         """));
        doTest("""
               !-|
               """,
               LanguageConfiguration.from("{ 'autoClosingPairs': [ { 'open': '<!--', 'close': '-->' } ] }"),
               new Input('-',
                         """
                         !--|
                         """));
        doTest("""
               |
               """,
               LanguageConfiguration.from("{ 'autoClosingPairs': [ { 'open': 'č', 'close': 'ž' } ] }"),
               new Input('č',
                         """
                         č|ž
                         """),
               new Input('ž',
                         """
                         čž|
                         """));
        doTest("""
               |
               """,
               LanguageConfiguration.from("{ 'autoClosingPairs': [ { 'open': '(', 'close': ')' } ] }"),
               new Input('č',
                         """
                         č|
                         """));
        FileObject systemConfigRoot = FileUtil.getSystemConfigRoot();
        FileObject grammar = FileUtil.createData(systemConfigRoot, "Editors/text/x-test/grammar.json");
        grammar.setAttribute("textmate-grammar", "source.c");
        try (InputStream is = TypingCompletionImplTest.class.getResourceAsStream("c.tmLanguage.json");
             OutputStream os = grammar.getOutputStream()) {
            FileUtil.copy(is, os);
        }
        TextmateTokenId.LanguageHierarchyImpl.refreshGrammars();
        doTest("""
               "|"
               """,
               LanguageConfiguration.from("{ 'autoClosingPairs': [ { 'open': '(', 'close': ')', 'notIn': [ 'string' ] } ] }"),
               new Input('(',
                         """
                         "(|"
                         """));
        doTest("""
               |
               """,
               LanguageConfiguration.from("{ 'autoClosingPairs': [ [ '(', ')' ] ] }"),
               new Input('(',
                         """
                         (|)
                         """),
               new Input(')',
                         """
                         ()|
                         """));
        doTest("""
               |>
               """,
               LanguageConfiguration.from("{ 'autoClosingPairs': [ { 'open': '(', 'close': ')' } ] }"),
               new Input('(',
                         """
                         (|>
                         """));
        doTest("""
               |>
               """,
               LanguageConfiguration.from("{ 'autoClosingPairs': [ { 'open': '(', 'close': ')' } ], 'autoCloseBefore': '>' }"),
               new Input('(',
                         """
                         (|)>
                         """));
    }

    @Test
    public void testAutoSurrounding() throws Exception {
        doTest("""
               ^hello|
               """,
               LanguageConfiguration.from("{ 'surroundingPairs': [ [ '(', ')' ] ] }"),
               new Input('(',
                         """
                         (hello)|
                         """));
    }

    private void doTest(String code, LanguageConfiguration config, Input... inputs) throws Exception {
        NbEditorDocument doc = new NbEditorDocument("text/x-test");
        JTextComponent c = new JEditorPane();
        c.setDocument(doc);
        ParsedCode parsedCode = detectSpan(code);
        doc.insertString(0, parsedCode.code(), null);
        Caret caretObj = c.getCaret();
        caretObj.setDot(parsedCode.mark());
        caretObj.moveDot(parsedCode.caret());
        TypingCompletionImpl typing = new TypingCompletionImpl(config);
        for (Input input : inputs) {
            int first = Math.min(caretObj.getMark(), caretObj.getDot());
            int second = Math.max(caretObj.getMark(), caretObj.getDot());
            String typed = String.valueOf(input.c());
            MutableContext context = TypingHooksSpiAccessor.get().createTtiContext(c, doc.createPosition(first), typed, doc.getText(first, second - first));
            boolean callAfterInsert = false;
            if (!typing.beforeInsert(context)) {
                doc.remove(first, second - first);
                caretObj.setDot(first);
                typing.insert(context);
                callAfterInsert = true;
            }
            Object[] data = TypingHooksSpiAccessor.get().getTtiContextData(context);
            if (data == null) {
                data = new Object[] {typed, 1, false};
            }
            doc.insertString(first, (String) data[0], null);
            caretObj.setDot(caretObj.getDot() + (int) data[1]);
            assertFalse((Boolean) data[2]);
            if (callAfterInsert) {
                typing.afterInsert(context);
            }
            String expected = input.expectedOutput();
            String[] expectedParts = expected.split("\\|");
            int expectedCaret = expectedParts[0].length();
            expected = expectedParts[0] + expectedParts[1];
            assertEquals(expectedCaret, caretObj.getDot());
            assertEquals(expected, doc.getText(0, doc.getLength()));
        }
    }

    private ParsedCode detectSpan(String code) {
        int caret = code.replace("^", "").indexOf('|');
        assertTrue("Caret marked incorrectly", caret != (-1));
        int mark = code.replace("|", "").indexOf('^');
        if (mark == (-1)) {
            mark = caret;
        }
        code = code.replace("|", "").replace("^", "");
        return new ParsedCode(code, mark, caret);
    }

    private record ParsedCode(String code, int mark, int caret) {}

    private record Input(char c, String expectedOutput) {}
}
