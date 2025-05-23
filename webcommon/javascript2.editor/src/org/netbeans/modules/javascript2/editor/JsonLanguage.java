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
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.InstantRenamer;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.javascript2.editor.classpath.ClassPathProviderImpl;
import org.netbeans.modules.javascript2.editor.formatter.JsFormatter;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.editor.navigation.JsonOccurrencesFinder;
import org.netbeans.modules.javascript2.editor.parser.JsonParser;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizerRegistration;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Petr Hejl
 */

@LanguageRegistration(mimeType="text/x-json", useMultiview = true) //NOI18N
@PathRecognizerRegistration(mimeTypes="text/x-json", libraryPathIds=ClassPathProviderImpl.BOOT_CP, binaryLibraryPathIds={})
public class JsonLanguage extends DefaultLanguageConfig {

    private static final boolean NAVIGATOR = Boolean.valueOf(
            System.getProperty(String.format("%s.navigator", JsonLanguage.class.getSimpleName()),   //NOI18N
                    Boolean.TRUE.toString()));
    private static final boolean FINDER = Boolean.valueOf(
            System.getProperty(String.format("%s.finder", JsonLanguage.class.getSimpleName()),      //NOI18N
                    Boolean.TRUE.toString()));

    //~ Inner classes

    @MIMEResolver.Registration(displayName = "jshintrc", resource = "jshintrc-resolver.xml", position = 124)
    @MIMEResolver.ExtensionRegistration(
        extension={ "json" },
        displayName="#JsonResolver",
        mimeType=JsTokenId.JSON_MIME_TYPE,
        position=195
    )
    @NbBundle.Messages("JsonResolver=JSON Files")
    @MultiViewElement.Registration(displayName = "#LBL_JsonEditorTab",
        iconBase = "org/netbeans/modules/javascript2/editor/resources/json.png",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "json.source",
        mimeType = JsTokenId.JSON_MIME_TYPE,
        position = 1)
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

    public JsonLanguage() {
        super();
        // has to be done here since JS hasn't its own project, also see issue #165915
        ClassPathProviderImpl.registerJsClassPathIfNeeded();
    }

    @Override
    public org.netbeans.api.lexer.Language getLexerLanguage() {
        return JsTokenId.jsonLanguage();
    }

    @Override
    public String getDisplayName() {
        return "JSON"; //NOI18N
    }

    @Override
    public Parser getParser() {
        return new JsonParser();
    }

    @Override
    public boolean hasStructureScanner() {
        return NAVIGATOR;
    }

    @Override
    public StructureScanner getStructureScanner() {
        return NAVIGATOR ?
                new JsStructureScanner(JsTokenId.jsonLanguage()) :
                null;
    }

    @Override
    public SemanticAnalyzer getSemanticAnalyzer() {
        return new JsonSemanticAnalyzer();
    }

// todo: tzezula - disable for now
//    @Override
//    public DeclarationFinder getDeclarationFinder() {
//        return new DeclarationFinderImpl(JsTokenId.jsonLanguage());
//    }

    @Override
    public boolean hasOccurrencesFinder() {
        return FINDER;
    }

    @Override
    public OccurrencesFinder getOccurrencesFinder() {
        return FINDER ?
                new JsonOccurrencesFinder() :
                null;
    }

    @Override
    public CodeCompletionHandler getCompletionHandler() {
        return new JsonCodeCompletion();
    }

//    @Override
//    public EmbeddingIndexerFactory getIndexerFactory() {
//        return new JsIndexer.Factory();
//    }

    @Override
    public Formatter getFormatter() {
        return new JsFormatter(JsTokenId.jsonLanguage());
    }

    @Override
    public boolean hasFormatter() {
        return true;
    }

    @Override
    public InstantRenamer getInstantRenamer() {
        return new JsonInstantRenamer();
    }
}
