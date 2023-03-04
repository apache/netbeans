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
package org.netbeans.modules.css.editor.csl;

import org.netbeans.api.lexer.Language;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.InstantRenamer;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.CommentHandler;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.css.lib.api.CssParserFactory;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizerRegistration;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 * Configuration for CSS
 */
@LanguageRegistration(mimeType = "text/css", useMultiview = true) //NOI18N
//index all source roots only
@PathRecognizerRegistration(mimeTypes = "text/css", libraryPathIds = {}, binaryLibraryPathIds = {}) //NOI18N
public class CssLanguage extends DefaultLanguageConfig {

    public static final String CSS_MIME_TYPE = "text/css";//NOI18N

    @MultiViewElement.Registration(displayName = "#LBL_CSSEditorTab",
        iconBase = "org/netbeans/modules/css/resources/style_sheet_16.png",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "css.source",
        mimeType = "text/css",
        position = 1)
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

    public CssLanguage() {
    }

    @Override
    public DeclarationFinder getDeclarationFinder() {
        return new CssDeclarationFinder();
    }

    @Override
    public boolean isIdentifierChar(char c) {
        /** Includes things you'd want selected as a unit when double clicking in the editor */
        //also used for completion items filtering!
        return Character.isJavaIdentifierPart(c)
                || (c == '-') || (c == '@')
                || (c == '&') || (c == '_')
                || (c == '#') ;
    }

    @Override
    public CommentHandler getCommentHandler() {
        return new CssCommentHandler();
    }

    @Override
    public Language getLexerLanguage() {
        return CssTokenId.language();
    }

    @Override
    public String getDisplayName() {
        return "CSS"; //NOI18N ???
    }

    @Override
    public String getPreferredExtension() {
        return "css"; // NOI18N
    }

    @Override
    public InstantRenamer getInstantRenamer() {
        return new CssInstantRenamer();
    }

    // Service Registrations
    @Override
    public SemanticAnalyzer getSemanticAnalyzer() {
        return new CssSemanticAnalyzer();
    }

    @Override
    public Parser getParser() {
        return CssParserFactory.getDefault().createParser(null);
    }

    @Override
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new CssStructureScanner();
    }

    @Override
    public CodeCompletionHandler getCompletionHandler() {
        return new CssCompletion();
    }

    @Override
    public KeystrokeHandler getKeystrokeHandler() {
        return new CssBracketCompleter();
    }

    @Override
    public boolean hasHintsProvider() {
        return true;
    }

    @Override
    public HintsProvider getHintsProvider() {
        return new CssHintsProvider();
    }

    @Override
    public OccurrencesFinder getOccurrencesFinder() {
        return new CssOccurrencesFinder();
    }

    @Override
    public boolean hasOccurrencesFinder() {
        return true;
    }
}
