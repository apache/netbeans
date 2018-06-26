/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    extension = "groovy",
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
