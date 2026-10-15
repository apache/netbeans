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
package org.netbeans.modules.languages.env;

import org.netbeans.api.lexer.Language;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import static org.netbeans.modules.languages.env.EnvFileResolver.MIME_TYPE;
import org.netbeans.modules.languages.env.completion.EnvCompletionHandler;
import org.netbeans.modules.languages.env.hints.EnvHintsProvider;
import org.netbeans.modules.languages.env.lexer.EnvLexer;
import org.netbeans.modules.languages.env.lexer.EnvTokenId;
import org.netbeans.modules.languages.env.lexer.EnvTokenId.EnvLanguageHierarchy;
import org.netbeans.modules.languages.env.parser.EnvParser;
import org.netbeans.modules.languages.env.parser.EnvParserResult;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.openide.util.*;
import org.openide.windows.TopComponent;

@LanguageRegistration(mimeType = "text/x-env", useMultiview = true)
public class EnvLanguage extends DefaultLanguageConfig {

    @NbBundle.Messages("Source=&Source")
    @MultiViewElement.Registration(displayName = "#Source",
            iconBase = "org/netbeans/modules/languages/env/resources/env_file_16.png",
            persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
            preferredID = "env.source",
            mimeType = MIME_TYPE,
            position = 2)
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

    public EnvLanguage() {
        super();
    }

    @Override
    public Language<EnvTokenId> getLexerLanguage() {
        return language;
    }

    @Override
    public String getDisplayName() {
        return "Env"; // NOI18N
    }

    @Override
    public String getPreferredExtension() {
        return "env"; // NOI18N
    }

    @Override
    public boolean isIdentifierChar(char c) {
        return super.isIdentifierChar(c) || c == '_' || c == '.' || c == '-' || c == '@'; // NOI18N
    }

    @Override
    public String getLineCommentPrefix() {
        return "#"; // NOI18N
    }
    
    @Override
    public Parser getParser() {
        return new EnvParser();
    }
    
    @Override
    public OccurrencesFinder<EnvParserResult> getOccurrencesFinder() {
        return new EnvOccurencesFinder();
    }
    
    @Override
    public boolean hasOccurrencesFinder() {
        return true;
    }
    
    @Override
    public DeclarationFinder getDeclarationFinder() {
        return new EnvDeclarationFinder();
    }
    
    @Override
    public CodeCompletionHandler getCompletionHandler() {
        return new EnvCompletionHandler();
    }

    @Override
    public boolean hasHintsProvider() {
        return true;
    }
    
    @Override
    public HintsProvider getHintsProvider() {
        return new EnvHintsProvider();
    }
    
    private static final Language<EnvTokenId> language
            = new EnvLanguageHierarchy() {

                @Override
                protected String mimeType() {
                    return MIME_TYPE;
                }

                @Override
                protected Lexer<EnvTokenId> createLexer(LexerRestartInfo<EnvTokenId> info) {
                    return new EnvLexer(info);
                }

            }.language();
}