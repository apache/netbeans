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
package org.netbeans.modules.css.prep.editor.scss;

import org.netbeans.api.lexer.Language;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.css.prep.editor.EmptyDeclarationFinder;
import org.netbeans.modules.css.prep.editor.EmptyParser;
import org.netbeans.modules.css.prep.editor.EmptyStructureScanner;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "ScssResolver=SCSS Files",
    "LBL_SCSSEditorTab=&Source"
})
@MIMEResolver.ExtensionRegistration(
        extension = {"scss"},
        displayName = "#ScssResolver",
        mimeType = "text/scss",
        position = 1250)
@LanguageRegistration(mimeType = "text/scss", useMultiview = true)
public class ScssCslLanguage extends DefaultLanguageConfig {
  
    @MultiViewElement.Registration(displayName = "#LBL_SCSSEditorTab",
            iconBase = "org/netbeans/modules/css/prep/cssprep.png",
            persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
            preferredID = "scss.source",
            mimeType = "text/scss",
            position = 1)
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

    @Override
    public Language getLexerLanguage() {
        return ScssLanguage.getLanguageInstance();
    }

    @Override
    public String getDisplayName() {
        return "scss";
    }

    @Override
    public String getPreferredExtension() {
        return "scss"; // NOI18N
    }

    @Override
    public Parser getParser() {
        return new EmptyParser();
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new EmptyStructureScanner();
    }

    @Override
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public DeclarationFinder getDeclarationFinder() {
        return new EmptyDeclarationFinder();
    }

}
