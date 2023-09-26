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
package org.netbeans.modules.languages.antlr.v4;

import org.netbeans.modules.languages.antlr.*;
import org.netbeans.api.lexer.Language;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.languages.antlr.AntlrTokenId.AntlrLanguageHierarchy;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;
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
 * @author lkishalmi
 */
@NbBundle.Messages(
        "ANTLRv4Resolver=ANTLR v4 Grammar"
)
@MIMEResolver.ExtensionRegistration(displayName = "#ANTLRv4Resolver",
        extension = "g4",
        mimeType = Antlr4Language.MIME_TYPE,
        position = 287
)

@ActionReferences({
    @ActionReference(
            path = "Loaders/text/x-antlr4/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    ),
    @ActionReference(
            path = "Loaders/text/x-antlr4/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/text/x-antlr4/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400
    ),
    @ActionReference(
            path = "Loaders/text/x-antlr4/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.PasteAction"),
            position = 500,
            separatorAfter = 600
    ),
    @ActionReference(
            path = "Loaders/text/x-antlr4/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 700
    ),
    @ActionReference(
            path = "Loaders/text/x-antlr4/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 800,
            separatorAfter = 900
    ),
    @ActionReference(
            path = "Loaders/text/x-antlr4/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 1000,
            separatorAfter = 1100
    ),
    @ActionReference(
            path = "Loaders/text/x-antlr4/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1200,
            separatorAfter = 1300
    ),
    @ActionReference(
            path = "Loaders/text/x-antlr4/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1400
    ),
    @ActionReference(
            path = "Loaders/text/x-antlr4/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1500
    ),
    @ActionReference(
            path = "Editors/text/x-antlr4/Popup",
            id = @ActionID(category = "Refactoring", id = "org.netbeans.modules.refactoring.api.ui.WhereUsedAction"),
            position = 1600
    ),
})

@LanguageRegistration(mimeType = Antlr4Language.MIME_TYPE, useMultiview = true)
public final class Antlr4Language extends DefaultLanguageConfig {

    public static final String MIME_TYPE = "text/x-antlr4";

    @Override
    public Language getLexerLanguage() {
        return language;
    }

    @Override
    public String getDisplayName() {
        return Bundle.ANTLRv4Resolver();
    }

    @Override
    public Formatter getFormatter() {
        return new Antlr4Formatter();
    }

    @Override
    public boolean hasFormatter() {
        return true;
    }

    @Override
    public String getPreferredExtension() {
        return "g4";
    }

    @Override
    public Parser getParser() {
        return new AntlrParser() {
            @Override
            protected AntlrParserResult<?> createParserResult(Snapshot snapshot) {
                return new Antlr4ParserResult(snapshot);
            }
        };
    }

    @Override
    public String getLineCommentPrefix() {
        return "//"; // NOI18N
    }

    @Override
    public DeclarationFinder getDeclarationFinder() {
        return new AntlrDeclarationFinder();
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new AntlrStructureScanner();
    }

    @Override
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public OccurrencesFinder getOccurrencesFinder() {
        return new AntlrOccurrencesFinder();
    }

    @Override
    public boolean hasOccurrencesFinder() {
        return true;
    }
    private static final Language<AntlrTokenId> language
            = new AntlrLanguageHierarchy() {

                @Override
                protected String mimeType() {
                    return Antlr4Language.MIME_TYPE;
                }

                @Override
                protected Lexer<AntlrTokenId> createLexer(LexerRestartInfo<AntlrTokenId> info) {
                    return  new Antlr4Lexer(info);
                }

    }.language();

    @NbBundle.Messages("Source=&Source")
    @MultiViewElement.Registration(
            displayName = "#Source",
            persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
            mimeType = Antlr4Language.MIME_TYPE,
            preferredID = "antlr4.source",
            position = 100
    )
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

}
