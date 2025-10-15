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

package org.netbeans.modules.groovy.editor.api.parser;

import java.util.Collections;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.lexer.Language;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.IndexSearcher;
import org.netbeans.modules.csl.api.InstantRenamer;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.groovy.editor.api.GroovyIndexer;
import org.netbeans.modules.groovy.editor.api.StructureAnalyzer;
import org.netbeans.modules.groovy.editor.api.completion.CompletionHandler;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import static org.netbeans.modules.groovy.editor.api.parser.GroovyLanguage.*;
import org.netbeans.modules.groovy.editor.hints.infrastructure.GroovyHintsProvider;
import org.netbeans.modules.groovy.editor.language.GroovyBracketCompleter;
import org.netbeans.modules.groovy.editor.language.GroovyDeclarationFinder;
import org.netbeans.modules.groovy.editor.language.GroovyFormatter;
import org.netbeans.modules.groovy.editor.language.GroovyInstantRenamer;
import org.netbeans.modules.groovy.editor.language.GroovySemanticAnalyzer;
import org.netbeans.modules.groovy.editor.language.GroovyTypeSearcher;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizerRegistration;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 * Language/lexing configuration for Groovy.
 *
 * @author Tor Norbye
 * @author Martin Adamek
 */
@MIMEResolver.ExtensionRegistration(
    mimeType = GROOVY_MIME_TYPE,
    displayName = "#GroovyResolver",
    extension = {"groovy", "gvy", "gy", "gsh"},
    position = 281
)
@LanguageRegistration(
    mimeType = GROOVY_MIME_TYPE,
    useMultiview = true
)
@PathRecognizerRegistration(
    mimeTypes = GROOVY_MIME_TYPE,
    sourcePathIds = ClassPath.SOURCE,
    libraryPathIds = {},
    binaryLibraryPathIds = {}
)
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
    @ActionReference(id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"), path = ACTIONS, position = 1600)
})
public class GroovyLanguage extends DefaultLanguageConfig {

    public static final String GROOVY_MIME_TYPE = "text/x-groovy";
    public static final String ACTIONS = "Loaders/" + GROOVY_MIME_TYPE + "/Actions";

    // Copy of groovy/support/resources icon because some API change caused
    // that it's not possible to refer to resource from different module
    private static final String GROOVY_FILE_ICON_16x16 = "org/netbeans/modules/groovy/editor/resources/GroovyFile16x16.png";

    public GroovyLanguage() {
    }

    @MultiViewElement.Registration(
        displayName = "#CTL_SourceTabCaption",
        mimeType = GROOVY_MIME_TYPE,
        iconBase = GROOVY_FILE_ICON_16x16,
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "groovy.source",
        position = 1
    )
    public static MultiViewEditorElement createEditor(Lookup lkp) {
        return new MultiViewEditorElement(lkp);
    }

    @Override
    public String getLineCommentPrefix() {
        return "//"; // NOI18N
    }

    @Override
    public boolean isIdentifierChar(char c) {
        return Character.isJavaIdentifierPart(c) || (c == '$');
    }

    @Override
    public Language getLexerLanguage() {
        return GroovyTokenId.language();
    }

    @Override
    public String getDisplayName() {
        return "Groovy"; // NOI18N
    }

    @Override
    public String getPreferredExtension() {
        return "groovy"; // NOI18N
    }

    @Override
    public Parser getParser() {
        return new GroovyParser();
    }

    @Override
    public boolean hasFormatter() {
        return true;
    }

    @Override
    public Formatter getFormatter() {
        return new GroovyFormatter();
    }

    @Override
    public KeystrokeHandler getKeystrokeHandler() {
        return new GroovyBracketCompleter();
    }

    @Override
    public CodeCompletionHandler getCompletionHandler() {
        return new CompletionHandler();
    }

    @Override
    public SemanticAnalyzer getSemanticAnalyzer() {
        return new GroovySemanticAnalyzer();
    }

    @Override
    public boolean hasOccurrencesFinder() {
        return true;
    }

    @Override
    public OccurrencesFinder getOccurrencesFinder() {
        return new GroovyOccurrencesFinder();
    }

    @Override
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new StructureAnalyzer();
    }

    @Override
    public boolean hasHintsProvider() {
        return true;
    }

    @Override
    public HintsProvider getHintsProvider() {
        return new GroovyHintsProvider();
    }

    @Override
    public DeclarationFinder getDeclarationFinder() {
        return new GroovyDeclarationFinder();
    }

    @Override
    public InstantRenamer getInstantRenamer() {
        return new GroovyInstantRenamer();
    }

    @Override
    public IndexSearcher getIndexSearcher() {
        return new GroovyTypeSearcher();
    }

    @Override
    public EmbeddingIndexerFactory getIndexerFactory() {
        return new GroovyIndexer.Factory();
    }

    @Override
    public Set<String> getSourcePathIds() {
        return Collections.singleton(ClassPath.SOURCE);
    }
}
