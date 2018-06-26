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
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.InstantRenamer;
import org.netbeans.modules.csl.api.OccurrencesFinder;
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
        iconBase = "org/netbeans/modules/javascript2/editor/resources/javascript.png",
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

//    @Override
//    public SemanticAnalyzer getSemanticAnalyzer() {
//        return new JsSemanticAnalyzer();
//    }

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
