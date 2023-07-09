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
package org.netbeans.modules.languages.yaml;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.InstantRenamer;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.LanguageProvider;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.openide.awt.*;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * GSF Configuration for YAML
 *
 * @author Tor Norbye
 */
@MIMEResolver.ExtensionRegistration(displayName = "#YAMLResolver",
        extension = {"yml", "yaml"},
        mimeType = "text/x-yaml",
        position = 280
)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/x-yaml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    ),
    @ActionReference(
            path = "Loaders/text/x-yaml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/text/x-yaml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400
    ),
    @ActionReference(
            path = "Loaders/text/x-yaml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.PasteAction"),
            position = 500,
            separatorAfter = 600
    ),
    @ActionReference(
            path = "Loaders/text/x-yaml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 700
    ),
    @ActionReference(
            path = "Loaders/text/x-yaml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 800,
            separatorAfter = 900
    ),
    @ActionReference(
            path = "Loaders/text/x-yaml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 1000,
            separatorAfter = 1100
    ),
    @ActionReference(
            path = "Loaders/text/x-yaml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1200,
            separatorAfter = 1300
    ),
    @ActionReference(
            path = "Loaders/text/x-yaml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1400
    ),
    @ActionReference(
            path = "Loaders/text/x-yaml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1500
    )
})
@LanguageRegistration(mimeType = YamlLanguage.MIME_TYPE, useMultiview = true)
public class YamlLanguage extends DefaultLanguageConfig {

    @Override
    public Language getLexerLanguage() {
        return language();
    }

    @Override
    public String getDisplayName() {
        return "YAML";
    }

    @Override
    public String getLineCommentPrefix() {
        return "#"; // NOI18N
    }

    @Override
    public Parser getParser() {
        return new YamlParser();
    }

    @Override
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new YamlScanner();
    }

    @Override
    public SemanticAnalyzer getSemanticAnalyzer() {
        return new YamlSemanticAnalyzer();
    }

    @Override
    public KeystrokeHandler getKeystrokeHandler() {
        return new YamlKeystrokeHandler();
    }

    @Override
    public CodeCompletionHandler getCompletionHandler() {
        return new YamlCompletion();
    }

    @Override
    public InstantRenamer getInstantRenamer() {
        return null;
    }

    public static final Language<YamlTokenId> language() {
        return language;
    }

    private static final Language<YamlTokenId> language =
        new LanguageHierarchy<YamlTokenId>() {

            @Override
            protected String mimeType() {
                return MIME_TYPE;
            }

            @Override
            protected Collection<YamlTokenId> createTokenIds() {
                return EnumSet.allOf(YamlTokenId.class);
            }

            @Override
            protected Lexer<YamlTokenId> createLexer(LexerRestartInfo<YamlTokenId> info) {
                return new YamlLexer(info);
            }

            @Override
            protected LanguageEmbedding<? extends TokenId> embedding(Token<YamlTokenId> token,
                    LanguagePath languagePath, InputAttributes inputAttributes) {
                switch (token.id()) {
                    case RUBY_EXPR:
                    case RUBY:
                        return findLanguage(YamlLanguage.RUBY_MIME_TYPE);
                    case PHP:
                        return findLanguage(YamlLanguage.PHP_MIME_TYPE);
                    default:
                        return null;
                }
            }
    }.language();

    private static LanguageEmbedding<? extends TokenId> findLanguage(String mimeType) {
        Language<? extends TokenId> ret = null;

        Collection<? extends LanguageProvider> providers = Lookup.getDefault().lookupAll(LanguageProvider.class);
        for (LanguageProvider provider : providers) {
            ret = provider.findLanguage(mimeType);
            if (ret != null) {
                break;
            }
        }

        return ret != null ? LanguageEmbedding.create(ret, 0, 0, false) : null;
    }

    /**
     * MIME type for YAML. Don't change this without also consulting the various
     * XML files that cannot reference this value directly.
     */
    public static final String MIME_TYPE = "text/x-yaml"; // NOI18N

    public static final String RUBY_MIME_TYPE = "text/x-ruby"; // NOI18N
    public static final String PHP_MIME_TYPE = "text/x-php5"; // NOI18N


    @NbBundle.Messages("Source=&Source")
    @MultiViewElement.Registration(
            displayName="#Source",
            iconBase="org/netbeans/modules/languages/yaml/yaml_files_16.png",
            persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
            mimeType=MIME_TYPE,
            preferredID="yaml.source",
            position=100
    )
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }
}
