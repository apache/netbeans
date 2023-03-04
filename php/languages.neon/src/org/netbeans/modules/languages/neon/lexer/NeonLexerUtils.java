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
package org.netbeans.modules.languages.neon.lexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class NeonLexerUtils {

    private NeonLexerUtils() {
    }

    public static <T extends TokenId> TokenSequence<T> seqForText(String text, Language<T> language) {
        TokenHierarchy<?> hi = TokenHierarchy.create(text, language);
        return hi.tokenSequence(language);
    }

    public static String getFileContent(File file) throws Exception {
        StringBuilder sb = new StringBuilder();
        String lineSep = "\n"; //NOI18N
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append(lineSep);
            line = br.readLine();
        }
        br.close();
        return sb.toString();
    }

    public static String replaceLinesAndTabs(String input) {
        return input.replace("\n", "\\n") //NOI18N
                    .replace("\r", "\\r") //NOI18N
                    .replace("\t", "\\t"); //NOI18N
    }

}
