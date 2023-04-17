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
package org.netbeans.modules.languages.hcl.terraform;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.languages.hcl.HCLLanguage;
import org.netbeans.modules.languages.hcl.HCLTokenId;
import org.netbeans.modules.languages.hcl.NbHCLParser;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.lexer.EmbeddingPresence;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.NbBundle;

/**
 *
 * @author lkishalmi
 */
@NbBundle.Messages(
        "TerraformResolver=Terraform Files"
)
@MIMEResolver.ExtensionRegistration(displayName = "#TerraformResolver",
        extension = "tf",
        mimeType = TerraformLanguage.MIME_TYPE,
        position = 305
)
@LanguageRegistration(mimeType = TerraformLanguage.MIME_TYPE, useMultiview = true)
public final class TerraformLanguage extends HCLLanguage {

    public static final String MIME_TYPE = "text/x-terraform+x-hcl";

    @Override
    public Language getLexerLanguage() {
        return language;
    }

    @Override
    public String getDisplayName() {
        return Bundle.TerraformResolver();
    }

    @Override
    public String getPreferredExtension() {
        return "tf";
    }

    @Override
    public Parser getParser() {
        return new NbHCLParser<TerraformParserResult>(TerraformParserResult::new);
    }

    private static final Language<HCLTokenId> language = new LanguageHierarchy<HCLTokenId>() {

        @Override
        protected String mimeType() {
            return TerraformLanguage.MIME_TYPE;
        }

        @Override
        protected Lexer<HCLTokenId> createLexer(LexerRestartInfo<HCLTokenId> info) {
            return new TerraformHCLLexer(info);
        }

        @Override
        protected Collection<HCLTokenId> createTokenIds() {
            return EnumSet.allOf(HCLTokenId.class);
        }

        @Override
        protected LanguageEmbedding<?> embedding(Token<HCLTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            return HCLTokenId.INTERPOLATION == token.id() ? LanguageEmbedding.create(language(), 0, 0) : null;
        }

        @Override
        protected EmbeddingPresence embeddingPresence(HCLTokenId id) {
            return HCLTokenId.INTERPOLATION == id ? EmbeddingPresence.CACHED_FIRST_QUERY : EmbeddingPresence.NONE;
        }
    }.language();

}
