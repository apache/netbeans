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

package org.netbeans.modules.php.editor.csl;

import org.netbeans.modules.php.editor.completion.PHPCodeCompletion;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
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
import org.netbeans.modules.csl.api.OverridingMethods;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizerRegistration;
import static org.netbeans.modules.php.api.util.FileUtils.PHP_MIME_TYPE;
import static org.netbeans.modules.php.editor.csl.PHPLanguage.ACTIONS;
import org.netbeans.modules.php.editor.indent.PHPFormatter;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.parser.GSFPHPParser;
import org.netbeans.modules.php.editor.verification.PHPHintsProvider;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Petr Pisl
 */
@LanguageRegistration(mimeType=PHP_MIME_TYPE, useMultiview=true)
@PathRecognizerRegistration(mimeTypes=PHP_MIME_TYPE, sourcePathIds=PhpSourcePath.SOURCE_CP,
        libraryPathIds={PhpSourcePath.BOOT_CP, PhpSourcePath.PROJECT_BOOT_CP}, binaryLibraryPathIds={})
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

public class PHPLanguage extends DefaultLanguageConfig {

    @MultiViewElement.Registration(
        displayName="#LBL_PHPEditorTab",
        iconBase="org/netbeans/modules/php/editor/resources/php16.png",
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="php.source",
        mimeType=PHP_MIME_TYPE,
        position=1
    )
    @NbBundle.Messages("LBL_PHPEditorTab=&Source")
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }
    public static final String ACTIONS = "Loaders/" + PHP_MIME_TYPE + "/Actions";
    public static final String LINE_COMMENT_PREFIX = "//"; // NOI18N

    @Override
    public String getLineCommentPrefix() {
        return LINE_COMMENT_PREFIX;
    }

    @Override
    public boolean isIdentifierChar(char c) {
        return Character.isJavaIdentifierPart(c) || (c == '@'); //NOI18N
    }

    @Override
    public Language getLexerLanguage() {
        return PHPTokenId.language();
    }

    @Override
    public String getDisplayName() {
        return "PHP";
    }

    @Override
    public String getPreferredExtension() {
        return "php"; // NOI18N
    }

    // Service Registrations

    @Override
    public Parser getParser() {
        return new GSFPHPParser();
    }

    @Override
    public CodeCompletionHandler getCompletionHandler() {
        return new PHPCodeCompletion();
    }

    @Override
    public SemanticAnalyzer getSemanticAnalyzer() {
        return new SemanticAnalysis();
    }

    @Override
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new PhpStructureScanner();
    }

    @Override
    public DeclarationFinder getDeclarationFinder() {
        return new DeclarationFinderImpl();
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
    public boolean hasFormatter() {
        return true;
    }

    @Override
    public Formatter getFormatter() {
        return new PHPFormatter();
    }

    @Override
    public KeystrokeHandler getKeystrokeHandler() {
        return new PHPBracketCompleter();
    }

    @Override
    public InstantRenamer getInstantRenamer() {
        return new InstantRenamerImpl();
    }

    @Override
    public boolean hasHintsProvider() {
        return true;
    }

    @Override
    public HintsProvider getHintsProvider() {
        return new PHPHintsProvider();
    }

    @Override
    public IndexSearcher getIndexSearcher() {
        return new PHPTypeSearcher();
    }

    @Override
    public EmbeddingIndexerFactory getIndexerFactory() {
        return new PHPIndexer.Factory();
    }

    @Override
    public Set<String> getLibraryPathIds() {
        return new HashSet<>(Arrays.asList(PhpSourcePath.BOOT_CP, PhpSourcePath.PROJECT_BOOT_CP));
    }

    @Override
    public Set<String> getSourcePathIds() {
        return Collections.singleton(PhpSourcePath.SOURCE_CP);
    }

    @Override
    public OverridingMethods getOverridingMethods() {
        return new OverridingMethodsImpl();
    }

}
