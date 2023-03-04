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
package org.netbeans.modules.php.latte.csl;

import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.php.latte.completion.LatteCompletionHandler;
import org.netbeans.modules.php.latte.hints.LatteHintsProvider;
import org.netbeans.modules.php.latte.lexer.LatteTopTokenId;
import org.netbeans.modules.php.latte.parser.LatteParser;
import org.netbeans.modules.php.latte.parser.LatteParserResult;
import org.netbeans.modules.php.latte.semantic.LatteSemanticAnalyzer;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
@LanguageRegistration(mimeType = LatteLanguage.LATTE_MIME_TYPE, useCustomEditorKit = true)
public class LatteLanguage extends DefaultLanguageConfig {
    public static final String LATTE_MIME_TYPE = "text/x-latte"; //NOI18N

    @Override
    public Language<LatteTopTokenId> getLexerLanguage() {
        return LatteTopTokenId.language();
    }

    @Override
    public String getDisplayName() {
        return "Latte"; //NOI18N
    }

    @Override
    public boolean isIdentifierChar(char c) {
        return Character.isJavaIdentifierPart(c) || (c == '$') || (c == '_');
    }

    @Override
    public Parser getParser() {
        return new LatteParser();
    }

    @Override
    public SemanticAnalyzer<LatteParserResult> getSemanticAnalyzer() {
        return new LatteSemanticAnalyzer();
    }

    @Override
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new LatteStructureScanner();
    }

    @Override
    public CodeCompletionHandler getCompletionHandler() {
        return new LatteCompletionHandler();
    }

    @Override
    public boolean isUsingCustomEditorKit() {
        return true;
    }

    @Override
    public boolean hasHintsProvider() {
        return true;
    }

    @Override
    public HintsProvider getHintsProvider() {
        return new LatteHintsProvider();
    }

}
