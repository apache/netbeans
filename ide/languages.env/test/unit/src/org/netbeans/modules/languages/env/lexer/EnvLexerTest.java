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
package org.netbeans.modules.languages.env.lexer;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.languages.env.EnvLanguage;
import org.netbeans.modules.languages.env.EnvTestBase;
import org.netbeans.modules.languages.env.EnvTestUtils;

public class EnvLexerTest extends EnvTestBase {

    public EnvLexerTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        LexerTestUtilities.setTesting(true);
    }

    public void testLexer_01() throws Exception {
        checkLexer("testfiles/lexer/env01.env");
    }

    private void checkLexer(final String filePath) throws Exception {
        String fileContent = Files.readString(new File(getDataDir(), filePath).toPath(), StandardCharsets.UTF_8);
        EnvLanguage langSettings = new EnvLanguage();
        Language<EnvTokenId> language;
        language = langSettings.getLexerLanguage();
        TokenHierarchy<?> th = TokenHierarchy.create(fileContent, language);
        TokenSequence<? extends EnvTokenId> ts = th.tokenSequence(language);
        assertNotNull("Can not obtain token sequence for file: " + filePath, ts);
        StringBuilder result = new StringBuilder();
        while (ts.moveNext()) {
            EnvTokenId tokenId = ts.token().id();
            CharSequence tokenText = ts.token().text();
            result.append("Token #");
            result.append(ts.index());
            result.append(" ");
            result.append(tokenId.name());
            String token = EnvTestUtils.replaceLinesAndTabs(tokenText.toString());
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
}
