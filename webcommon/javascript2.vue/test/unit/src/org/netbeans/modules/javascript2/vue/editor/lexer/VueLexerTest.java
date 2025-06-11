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
package org.netbeans.modules.javascript2.vue.editor.lexer;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.javascript2.vue.editor.VueLanguage;
import org.netbeans.modules.javascript2.vue.editor.VueTestBase;

/**
 *
 * @author bogdan.haidu
 */
public class VueLexerTest extends VueTestBase {

    public VueLexerTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        LexerTestUtilities.setTesting(true);
    }

    public void testAttributeBindingLexer_01() throws Exception {
        checkLexer("testfiles/lexer/vue/attribute_binding_01.vue");
    }

    public void testTemplateLexer_01() throws Exception {
        checkLexer("testfiles/lexer/vue/template_01.vue");
    }

    public void testUserInputLexer_01() throws Exception {
        checkLexer("testfiles/lexer/vue/user_input_01.vue");
    }

    public void testComponentLexer_01() throws Exception {
        checkLexer("testfiles/lexer/vue/component_01.vue");
    }
    
    public void testJavascriptPugScript_01() throws Exception {
        checkLexer("testfiles/lexer/vue/javascript_pug_script.vue");
    }

    private void checkLexer(final String filePath) throws Exception {
        String fileContent = Files.readString(new File(getDataDir(), filePath).toPath(), StandardCharsets.UTF_8);
        VueLanguage langSettings = new VueLanguage();
        Language<VueTokenId> language;
        language = langSettings.getLexerLanguage();
        TokenHierarchy<?> th = TokenHierarchy.create(fileContent, language);
        TokenSequence<? extends VueTokenId> ts = th.tokenSequence(language);
        assertNotNull("Can not obtain token sequence for file: " + filePath, ts);
        StringBuilder result = new StringBuilder();
        while (ts.moveNext()) {
            VueTokenId tokenId = ts.token().id();
            CharSequence tokenText = ts.token().text();
            result.append("Token #");
            result.append(ts.index());
            result.append(" ");
            result.append(tokenId.name());
            String token = replaceLinesAndTabs(tokenText.toString());
            if (!token.isEmpty()) {
                result.append(" ");
                result.append("[");
                result.append(token);
                result.append("]");
            }
            result.append("\n");
        }

        assertDescriptionMatches(filePath, result.toString(), false, ".lexer");
    }

    private String replaceLinesAndTabs(String input) {
        String escapedString = input;
        escapedString = escapedString.replaceAll("\n", "\\\\n"); // NOI18N
        escapedString = escapedString.replaceAll("\r", "\\\\r"); // NOI18N
        escapedString = escapedString.replaceAll("\t", "\\\\t"); // NOI18N
        return escapedString;
    }
}
