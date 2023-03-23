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
package org.netbeans.modules.languages.hcl.tfvars;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.languages.hcl.HCLTokenId;
import org.netbeans.modules.languages.hcl.BasicHCLLexer;
import org.netbeans.spi.lexer.EmbeddingPresence;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author lkishalmi
 */
@NbBundle.Messages(
        "TFVarsResolver=Terraform Variables"
)
@MIMEResolver.ExtensionRegistration(displayName = "#TFVarsResolver",
        extension = "tfvars",
        mimeType = TFVarsLanguage.MIME_TYPE,
        position = 304
)

@LanguageRegistration(mimeType = TFVarsLanguage.MIME_TYPE, useMultiview = true)
public final class TFVarsLanguage extends DefaultLanguageConfig {

    public static final String MIME_TYPE = "text/x-tfvars+x-hcl";

    @Override
    public Language getLexerLanguage() {
        return language;
    }

    @Override
    public String getDisplayName() {
        return Bundle.TFVarsResolver();
    }

    @Override
    public String getPreferredExtension() {
        return "tfvars";
    }

    @Override
    public String getLineCommentPrefix() {
        return "#"; // NOI18N
    }

    private static final Language<HCLTokenId> language = new LanguageHierarchy<HCLTokenId>() {

        @Override
        protected String mimeType() {
            return TFVarsLanguage.MIME_TYPE;
        }

        @Override
        protected Lexer<HCLTokenId> createLexer(LexerRestartInfo<HCLTokenId> info) {
            return new BasicHCLLexer(info);
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

    @NbBundle.Messages("Source=&Source")
    @MultiViewElement.Registration(
            displayName = "#Source",
            persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
            mimeType = TFVarsLanguage.MIME_TYPE,
            preferredID = "tfvars.source",
            position = 100
    )
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

}
