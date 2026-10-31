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

import java.util.regex.Pattern;
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
import org.netbeans.modules.editor.bracesmatching.MasterMatcher;
import org.netbeans.modules.editor.bracesmatching.SpiAccessor;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;

public class BracesMatcherFactoryImplTest {

    @Test
    public void testBraceMatcher() throws Exception {
        doTest("""
               foo1 %{%
               ^}^|
               foo3
               """,
               LanguageConfiguration.from("{ 'brackets': [ [ '{', '}' ] ] }"),
               true);
        doTest("""
               foo1 %{%
               ^|}^
               foo3
               """,
               LanguageConfiguration.from("{ 'brackets': [ [ '{', '}' ] ] }"),
               false);
        doTest("""
               foo1 ^{|^
               %}%
               foo3
               """,
               LanguageConfiguration.from("{ 'brackets': [ [ '{', '}' ] ] }"),
               true);
        doTest("""
               foo1 ^|{^
               %}%
               foo3
               """,
               LanguageConfiguration.from("{ 'brackets': [ [ '{', '}' ] ] }"),
               false);
        doTest("""
               foo1 %{{%
               ^}}^|
               foo3
               """,
               LanguageConfiguration.from("{ 'brackets': [ [ '{{', '}}' ] ] }"),
               true);
        doTest("""
               foo1 %{{%
               ^|}}^
               foo3
               """,
               LanguageConfiguration.from("{ 'brackets': [ [ '{{', '}}' ] ] }"),
               false);
        doTest("""
               foo1 ^{{|^
               %}}%
               foo3
               """,
               LanguageConfiguration.from("{ 'brackets': [ [ '{{', '}}' ] ] }"),
               true);
        doTest("""
               foo1 ^|{{^
               %}}%
               foo3
               """,
               LanguageConfiguration.from("{ 'brackets': [ [ '{{', '}}' ] ] }"),
               false);
        doTest("""
               foo1 %{{%
               ^}|}^
               foo3
               """,
               LanguageConfiguration.from("{ 'brackets': [ [ '{{', '}}' ] ] }"),
               true); //we'll find the bracket only backwards - is that OK?
        doTest("""
               foo1 %begin%
               ^end^|
               foo3
               """,
               LanguageConfiguration.from("{ 'brackets': [ [ 'begin', 'end' ] ] }"),
               true);
        doTest("""
               foo1 %begin%
               |^end^
               foo3
               """,
               LanguageConfiguration.from("{ 'brackets': [ [ 'begin', 'end' ] ] }"),
               false);
        doTest("""
               foo1 ^begin^|
               %end%
               foo3
               """,
               LanguageConfiguration.from("{ 'brackets': [ [ 'begin', 'end' ] ] }"),
               true);
        doTest("""
               foo1 |^begin^
               %end%
               foo3
               """,
               LanguageConfiguration.from("{ 'brackets': [ [ 'begin', 'end' ] ] }"),
               false);
    }

    private void doTest(String code, LanguageConfiguration config, boolean backwards) throws Exception {
        NbEditorDocument doc = new NbEditorDocument("text/x-test");
        JTextComponent c = new JEditorPane();
        c.setDocument(doc);
        int caret = code.replace("%", "").replace("^", "").indexOf('|');
        assertTrue(caret != (-1));
        int[] origin = detectSpan(code.replace("|", "").replace("%", ""), '^');
        int[] matching = detectSpan(code.replace("|", "").replace("^", ""), '%');
        doc.insertString(0, code.replace("|", "").replace("^", "").replace("%", ""), null);
        LanguageConfigurationDataProvider.setLanguageConfiguration(config);
        MasterMatcher.markTestThread(); //TODO: could be done only once
        BracesMatcher matcher = new BracesMatcherFactoryImpl().createMatcher(SpiAccessor.get().createCaretContext(doc, caret, backwards, backwards ? caret : doc.getLength() - caret));
        assertArrayEquals(origin, matcher.findOrigin());
        assertArrayEquals(matching, matcher.findMatches());
    }

    private int[] detectSpan(String input, char mark) {
        String[] parts = input.split(Pattern.quote("" + mark));
        assertEquals(3, parts.length);
        return new int[] {parts[0].length(), parts[0].length() + parts[1].length()};
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
