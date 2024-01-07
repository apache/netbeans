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
package org.netbeans.modules.javascript2.editor;

import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.javascript2.editor.classpath.ClassPathProviderImpl;
import org.netbeans.modules.javascript2.editor.formatter.JsFormatter;
import org.netbeans.modules.javascript2.editor.hints.JsHintsProvider;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.editor.navigation.DeclarationFinderImpl;
import org.netbeans.modules.javascript2.editor.navigation.JsIndexSearcher;
import org.netbeans.modules.javascript2.editor.navigation.OccurrencesFinderImpl;
import org.netbeans.modules.javascript2.editor.parser.JsParser;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizerRegistration;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Petr Pisl, Tor Norbye
 */

@LanguageRegistration(mimeType="text/javascript", useMultiview = true) //NOI18N
@PathRecognizerRegistration(mimeTypes="text/javascript", libraryPathIds=ClassPathProviderImpl.BOOT_CP, binaryLibraryPathIds={})
public class JsLanguage extends DefaultLanguageConfig {

    @MIMEResolver.ExtensionRegistration(
        extension={ "js", "sdoc", "jsx" },
        displayName="#JsResolver",
        mimeType=JsTokenId.JAVASCRIPT_MIME_TYPE,
        position=190
    )
    @NbBundle.Messages("JsResolver=JavaScript Files")
    @MultiViewElement.Registration(displayName = "#LBL_JsEditorTab",
        iconBase = "org/netbeans/modules/javascript2/editor/resources/javascript.png",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "javascript.source",
        mimeType = JsTokenId.JAVASCRIPT_MIME_TYPE,
        position = 1)
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

    public JsLanguage() {
        super();
    }

    @Override
    public org.netbeans.api.lexer.Language getLexerLanguage() {
        // has to be done here since JS hasn't its own project, also see issue #165915
        // It was moved here from the JsLanguage initialization since the the language is called much earlier than the
        // JavaScipt is really needed. Calling it in the #getLexerLanguage() should ensure to be the CP registration
        // called once the JS will be really nedded (means also for PHP, JSP, ... since they embedd HTML and HTML
        // coloring embeding initialize way to call this method. Disadvantage of this solution is that it's to be called
        // once per opened file but since the CP is registered it's only about one condition.
        ClassPathProviderImpl.registerJsClassPathIfNeeded();

        return JsTokenId.javascriptLanguage();
    }

    @Override
    public String getDisplayName() {
        return "JavaScript"; //NOI18N
    }

    @Override
    public Parser getParser() {
        return new JsParser();
    }

    @Override
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public boolean hasHintsProvider() {
        return true;
    }

    @Override
    public HintsProvider getHintsProvider() {
        return new JsHintsProvider();
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new JsStructureScanner(JsTokenId.javascriptLanguage());
    }

    @Override
    public SemanticAnalyzer getSemanticAnalyzer() {
        return new JsSemanticAnalyzer();
    }

    @Override
    public DeclarationFinder getDeclarationFinder() {
        return new DeclarationFinderImpl(JsTokenId.javascriptLanguage());
    }

    @Override
    public boolean hasOccurrencesFinder() {
        return true;
    }

    @Override
    public OccurrencesFinder getOccurrencesFinder() {
        return new OccurrencesFinderImpl();
    }

    @Override
    public CodeCompletionHandler getCompletionHandler() {
        return new JsCodeCompletion();
    }

//    @Override
//    public EmbeddingIndexerFactory getIndexerFactory() {
//        return new JsIndexer.Factory();
//    }

    @Override
    public String getLineCommentPrefix() {
        return "//";    //NOI18N
    }

    @Override
    public Formatter getFormatter() {
        return new JsFormatter(JsTokenId.javascriptLanguage());
    }

    @Override
    public boolean hasFormatter() {
        return true;
    }

    @Override
    public InstantRenamer getInstantRenamer() {
        return new JsInstantRenamer();
    }

    @Override
    public boolean isIdentifierChar(char c) {
        return super.isIdentifierChar(c)
                // due to CC filtering of DOC annotations - see GsfCompletionProvider#getCompletableLanguage()
                || c == '@' //NOI18N
                || c == '#' //NOI18N
                //// see issue #214978 - it goes to the CodeTemplateCompletionProvider#query(), it would probably deserve
                ////  new API in the next release or are we wrongly embedding the jQuery? For now this fix doesn't look to
                ////  make troubles to another areas.
                // || c == ':' || c == '.'; //NOI18N
                ;
    }

    @Override
    public IndexSearcher getIndexSearcher() {
        return new JsIndexSearcher();
    }

    @Override
    public KeystrokeHandler getKeystrokeHandler() {
        return new JsKeyStrokeHandler();
    }

}
