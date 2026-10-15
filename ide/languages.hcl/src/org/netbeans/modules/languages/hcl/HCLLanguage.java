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
package org.netbeans.modules.languages.hcl;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import org.netbeans.api.editor.fold.FoldTemplate;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.editor.fold.FoldTypeProvider;
import org.netbeans.spi.lexer.EmbeddingPresence;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Laszlo Kishalmi
 */
@NbBundle.Messages(
        "HCLResolver=HCL Files"
)
@MIMEResolver.ExtensionRegistration(displayName = "#HCLResolver",
        extension = "hcl",
        mimeType = HCLLanguage.MIME_TYPE,
        position = 306
)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/x-hcl/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    ),
    @ActionReference(
            path = "Loaders/text/x-hcl/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/text/x-hcl/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400
    ),
    @ActionReference(
            path = "Loaders/text/x-hcl/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.PasteAction"),
            position = 500,
            separatorAfter = 600
    ),
    @ActionReference(
            path = "Loaders/text/x-hcl/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 700
    ),
    @ActionReference(
            path = "Loaders/text/x-hcl/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 800,
            separatorAfter = 900
    ),
    @ActionReference(
            path = "Loaders/text/x-hcl/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 1000,
            separatorAfter = 1100
    ),
    @ActionReference(
            path = "Loaders/text/x-hcl/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1200,
            separatorAfter = 1300
    ),
    @ActionReference(
            path = "Loaders/text/x-hcl/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1400
    ),
    @ActionReference(
            path = "Loaders/text/x-hcl/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1500
    ),
    @ActionReference(
            path = "Editors/text/x-hcl/Popup",
            id = @ActionID(category = "Refactoring", id = "org.netbeans.modules.refactoring.api.ui.WhereUsedAction"),
            position = 1600
    ),})

@LanguageRegistration(mimeType = HCLLanguage.MIME_TYPE, useMultiview = true)
public class HCLLanguage extends DefaultLanguageConfig {
    public static final String MIME_TYPE="text/x-hcl";

    @Override
    public Language getLexerLanguage() {
        return language;
    }

    @Override
    public String getDisplayName() {
        return Bundle.HCLResolver();
    }

    @Override
    public String getPreferredExtension() {
        return "hcl";
    }

    @Override
    public String getLineCommentPrefix() {
        return "#"; // NOI18N
    }

    @Override
    public Parser getParser() {
        return new NbHCLParser<HCLParserResult>(HCLParserResult::new);
    }

    @Override
    public SemanticAnalyzer getSemanticAnalyzer() {
        return new HCLSemanticAnalyzer();
    }

    @Override
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public boolean isIdentifierChar(char c) {
        return Character.isJavaIdentifierPart(c) || (c == '-');
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new HCLStructureScanner();
    }

    static final Language<HCLTokenId> language = new LanguageHierarchy<HCLTokenId>() {

        @Override
        protected String mimeType() {
            return HCLLanguage.MIME_TYPE;
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
            mimeType = HCLLanguage.MIME_TYPE,
            preferredID = "hcl.source",
            position = 100
    )
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

    @MimeRegistration(mimeType = HCLLanguage.MIME_TYPE, service = FoldTypeProvider.class, position=230)
    public static class HCLFold implements FoldTypeProvider {

        @NbBundle.Messages({
            "FT_label_heredoc=Heredocs",
            "FT_template_heredoc=<<..."
        })
        public static final FoldType HEREDOC = FoldType.create("heredoc", Bundle.FT_label_heredoc(), new FoldTemplate(2,0, Bundle.FT_template_heredoc()));

        @NbBundle.Messages({
            "FT_label_tuple=Tuples (List/Sets)",
            "FT_template_tuple=[...]"
        })
        public static final FoldType TUPLE = FoldType.create("tuple", Bundle.FT_label_tuple(), new FoldTemplate(1,1, Bundle.FT_template_tuple()));
        @NbBundle.Messages({
            "FT_label_object=Maps or Objects",
            "FT_template_object={...}"
        })
        public static final FoldType OBJECT = FoldType.create("object", Bundle.FT_label_object(), new FoldTemplate(1,1, Bundle.FT_template_object()));

        private static final List<FoldType> SUPPORTED = Arrays.asList(
                FoldType.CODE_BLOCK,
                FoldType.COMMENT,
                HCLFold.HEREDOC,
                FoldType.INITIAL_COMMENT,
                HCLFold.OBJECT,
                HCLFold.TUPLE
        );

        @Override
        public Collection getValues(Class type) {
            return type == FoldType.class ? SUPPORTED : null;
        }

        @Override
        public boolean inheritable() {
            return true;
        }

    }
}
