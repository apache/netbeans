/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
                || c == '@'; //NOI18N
                // see issue #214978 - it goes to the CodeTemplateCompletionProvider#query(), it would probably deserve
                //  new API in the next release or are we wrongly embedding the jQuery? For now this fix doesn't look to
                //  make troubles to another areas.
//                || c == '#' || c == ':' || c == '.'; //NOI18N
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
