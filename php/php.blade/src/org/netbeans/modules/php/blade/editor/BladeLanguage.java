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
package org.netbeans.modules.php.blade.editor;

import org.netbeans.modules.php.blade.editor.hints.BladeHintsProvider;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.spi.lexer.Lexer;
import org.openide.util.NbBundle;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.CommentHandler;
import static org.netbeans.modules.php.blade.editor.BladeLanguage.ACTIONS;
import org.netbeans.modules.php.blade.editor.completion.BladeCompletionHandler;
import org.netbeans.modules.php.blade.editor.format.BladeFormatter;
import org.netbeans.modules.php.blade.editor.lexer.BladeLexer;
import org.netbeans.modules.php.blade.editor.lexer.BladeTokenId;
import org.netbeans.modules.php.blade.editor.lexer.BladeTokenId.BladeLanguageHierarchy;
import org.netbeans.modules.php.blade.editor.navigator.BladeStructureScanner;
import org.netbeans.modules.php.blade.editor.parser.BladeParser;
import org.netbeans.modules.php.blade.editor.parser.BladeParserResult;

/**
 *
 * @author Haidu Bogdan
 */
@MIMEResolver.Registration(
        resource = "../resources/mime-resolver.xml",
        displayName = "#LBL_Blade_LOADER",
        position = 1
)

@NbBundle.Messages({
    "LBL_Blade_LOADER=Blade template files"
})
@LanguageRegistration(mimeType = "text/x-blade", useMultiview = true)
@ActionReferences({
    @ActionReference(id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"), path = ACTIONS, position = 100),
    @ActionReference(id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"), path = ACTIONS, position = 300, separatorBefore = 200),
    @ActionReference(id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"), path = ACTIONS, position = 400),
    @ActionReference(id = @ActionID(category = "Edit", id = "org.openide.actions.PasteAction"), path = ACTIONS, position = 500, separatorAfter = 600),
    @ActionReference(id = @ActionID(category = "System", id = "org.openide.actions.NewAction"), path = ACTIONS, position = 700),
    @ActionReference(id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"), path = ACTIONS, position = 800),
    @ActionReference(id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"), path = ACTIONS, position = 900, separatorAfter = 1000),
    @ActionReference(id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"), path = ACTIONS, position = 1100, separatorAfter = 1200),
    @ActionReference(id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"), path = ACTIONS, position = 1300, separatorAfter = 1400),
    @ActionReference(id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"), path = ACTIONS, position = 1500),
    @ActionReference(id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"), path = ACTIONS, position = 1600),
    @ActionReference(id = @ActionID(category = "TemplateActions", id = "org.netbeans.modules.php.blade.editor.actions.FindUsage"),
            path = ACTIONS, separatorBefore = 1700, position = 1800),
    @ActionReference(id = @ActionID(category = "System", id = "org.netbeans.modules.php.blade.editor.actions.AntlrDebug"), path = ACTIONS, position = 1900), 
}
)
public class BladeLanguage extends DefaultLanguageConfig {

    public BladeLanguage() {
        super();
    }

    public static final String ACTIONS = "Loaders/" + BladeLanguage.MIME_TYPE + "/Actions"; //NOI18N
    public static final String MIME_TYPE = "text/x-blade"; //NOI18N

    @Override
    public Language<BladeTokenId> getLexerLanguage() {
        return language;
    }

    @Override
    public String getDisplayName() {
        return "Blade"; //NOI18N
    }

    @Override
    public String getPreferredExtension() {
        return "blade.php"; // NOI18N
    }

    @Override
    public Parser getParser() {
        return new BladeParser();
    }

    //we need this to avoid lang assertion error
    @Deprecated
    @Override
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new BladeStructureScanner();
    }

    @Override
    public CodeCompletionHandler getCompletionHandler() {
        return new BladeCompletionHandler();
    }

    @Override
    public CommentHandler getCommentHandler() {
        return new BladeCommentHandler();
    }

    @Override
    public boolean hasHintsProvider() {
        return true;
    }

    @Override
    public HintsProvider getHintsProvider() {
        return new BladeHintsProvider();
    }

    @Override
    public boolean hasFormatter() {
        return true;
    }

    @Override
    public Formatter getFormatter() {
        return new BladeFormatter();
    }

    @Override
    public DeclarationFinder getDeclarationFinder() {
        return new BladeDeclarationFinder();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public SemanticAnalyzer<BladeParserResult> getSemanticAnalyzer() {
        return new BladeSemanticAnalyzer();
    }

    /**
     * flag for detecting if we are in a string context enables to select the
     * blade view "layout.index" string value on double click without
     * interpreting the same thing for javascript objects
     */
    public static volatile Boolean hasQuote = false;

    @Override
    public boolean isIdentifierChar(char c) {
        /**
         * Includes things you'd want selected as a unit when double clicking in
         * the editor
         */
        //also used for completion items filtering!
        if (c == '"' || c == '\'') {
            hasQuote = true;
        }
        return Character.isJavaIdentifierPart(c)
                || (c == '@')
                || (hasQuote && c == '.') || (c == '_');
    }

    private static final Language<BladeTokenId> language
            = new BladeLanguageHierarchy() {

                @Override
                protected String mimeType() {
                    return BladeLanguage.MIME_TYPE;
                }

                @Override
                protected Lexer<BladeTokenId> createLexer(LexerRestartInfo<BladeTokenId> info) {
                    return new BladeLexer(info);
                }

            }.language();

    @NbBundle.Messages("Source=&Source Blade")
    @MultiViewElement.Registration(
            displayName = "#Source",
            persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
            mimeType = BladeLanguage.MIME_TYPE,
            preferredID = "blade.source",
            position = 100
    )
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

}
