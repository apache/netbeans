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
