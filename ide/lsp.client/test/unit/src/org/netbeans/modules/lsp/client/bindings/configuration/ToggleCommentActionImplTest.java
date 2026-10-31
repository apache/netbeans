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

import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.junit.Test;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.lsp.client.spi.friend.LanguageConfiguration;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;
import static org.junit.Assert.*;

public class ToggleCommentActionImplTest {

    @Test
    public void testLineComment() throws Exception {
        doTest("""
               foo1
               foo|2
               foo3
               """,
               LanguageConfiguration.from("{ 'comments': { 'lineComment': '//' } }"),
               """
               foo1
               // foo2
               foo3
               """);
        doTest("""
               foo1
               |
               foo3
               """,
               LanguageConfiguration.from("{ 'comments': { 'lineComment': '//' } }"),
               """
               foo1
               //\s
               foo3
               """);
        doTest("""
               foo1
               // foo|2
               foo3
               """,
               LanguageConfiguration.from("{ 'comments': { 'lineComment': '//' } }"),
               """
               foo1
               foo2
               foo3
               """);
        doTest("""
               foo1
               //foo|2
               foo3
               """,
               LanguageConfiguration.from("{ 'comments': { 'lineComment': '//' } }"),
               """
               foo1
               foo2
               foo3
               """);
        doTest("""
               foo1
               //|
               foo3
               """,
               LanguageConfiguration.from("{ 'comments': { 'lineComment': '//' } }"),
               """
               foo1

               foo3
               """);
        doTest("""
               foo1
                   foo|2
                       foo|3
               """,
               LanguageConfiguration.from("{ 'comments': { 'lineComment': '//' } }"),
               """
               foo1
                   // foo2
                   //     foo3
               """);
        doTest("""
               foo1
                   // f|oo2
                   //     f|oo3
               """,
               LanguageConfiguration.from("{ 'comments': { 'lineComment': '//' } }"),
               """
               foo1
                   foo2
                       foo3
               """);
        doTest("""
               foo1
                   foo|2
                       foo|3
               """,
               LanguageConfiguration.from("{ 'comments': { 'lineComment': { 'comment': '//', 'noIndent': false } } }"),
               """
               foo1
                   // foo2
                   //     foo3
               """);
        doTest("""
               foo1
                   foo|2
                       foo|3
               """,
               LanguageConfiguration.from("{ 'comments': { 'lineComment': { 'comment': '//', 'noIndent': true } } }"),
               """
               foo1
               //     foo2
               //         foo3
               """);
        doTest("""
               foo1
               //     fo|o2
               //         foo|3
               """,
               LanguageConfiguration.from("{ 'comments': { 'lineComment': { 'comment': '//', 'noIndent': true } } }"),
               """
               foo1
                   foo2
                       foo3
               """);
        doTest("""
               foo1
               //     fo|o2
               //         foo|3
               """,
               LanguageConfiguration.from("{ 'comments': { 'lineComment': { 'comment': '//', 'noIndent': false } } }"),
               """
               foo1
                   foo2
                       foo3
               """);
    }

    //TODO: block comment

    private void doTest(String code, LanguageConfiguration config, String expected) throws Exception {
        //TODO: could be nice to be also test mark > caret.
        NbEditorDocument doc = new NbEditorDocument("text/x-test");
        JTextComponent c = new JEditorPane();
        c.setDocument(doc);
        String[] parts = code.split("\\|");
        int caret;
        int mark;
        if (parts.length == 1) {
            fail("No caret position!");
            throw new AssertionError();
        } else if (parts.length == 2) {
            caret = mark = parts[0].length();
            code = parts[0] + parts[1];
        } else if (parts.length == 3) {
            mark = parts[0].length();
            caret = parts[0].length() + parts[1].length();
            code = parts[0] + parts[1] + parts[2];
        } else {
            fail("Unknow caret pattern!");
            throw new AssertionError();
        }
        assertTrue(caret != (-1));
        doc.insertString(0, code, null);
        Caret caretObj = c.getCaret();
        caretObj.setDot(mark);
        caretObj.moveDot(caret);
        caretObj.setSelectionVisible(true); //TODO: should be able to control true/false in the test?
        LanguageConfigurationDataProvider.setLanguageConfiguration(config);
        new ToggleCommentActionImpl().actionPerformed(null, c);
        String actual = doc.getText(0, doc.getLength());
        assertEquals(expected, actual);
    }

    @ServiceProvider(service=MimeDataProvider.class)
    public static final class LanguageConfigurationDataProvider implements MimeDataProvider {

        private static final LiveLookup LOOKUP = new LiveLookup();

        @Override
        public Lookup getLookup(MimePath mimePath) {
            if ("text/x-test".equals(mimePath.getPath())) {
                return LOOKUP;
            }

            return null;
        }

        public static void setLanguageConfiguration(LanguageConfiguration config) {
            LOOKUP.setLookupsInt(Lookups.fixed(config));
        }

        private static final class LiveLookup extends ProxyLookup {
            public void setLookupsInt(Lookup... lookups) {
                super.setLookups(lookups);
            }
        }
    }
}
