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
package org.netbeans.modules.css.prep.editor.less;

import org.netbeans.modules.css.prep.editor.EmptyStructureScanner;
import org.netbeans.modules.css.prep.editor.EmptyParser;
import org.netbeans.api.lexer.Language;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.css.prep.editor.EmptyDeclarationFinder;
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
    "LessResolver=LESS CSS Files",
    "LBL_LESSEditorTab=&Source"
})
@MIMEResolver.ExtensionRegistration(
        extension = {"less"},
        displayName = "#LessResolver",
        mimeType = "text/less",
        position = 1200)
@LanguageRegistration(mimeType = "text/less", useMultiview = true)
public class LessCslLanguage extends DefaultLanguageConfig {
  
    @MultiViewElement.Registration(displayName = "#LBL_LESSEditorTab",
            iconBase = "org/netbeans/modules/css/prep/cssprep.png",
            persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
            preferredID = "less.source",
            mimeType = "text/less",
            position = 1)
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

    @Override
    public Language getLexerLanguage() {
        return LessLanguage.getLanguageInstance();
    }

    @Override
    public String getDisplayName() {
        return "lesscss";
    }

    @Override
    public String getPreferredExtension() {
        return "less"; // NOI18N
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
