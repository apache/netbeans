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
package org.netbeans.modules.rust.cargo.language;

import org.netbeans.api.lexer.Language;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.rust.cargo.filetype.CargoDataObject;
import static org.netbeans.modules.rust.cargo.filetype.CargoDataObject.MIME_TYPE;
import org.netbeans.modules.rust.cargo.language.semantic.CargoTOMLSemanticAnalyzer;
import org.netbeans.modules.rust.cargo.language.structure.CargoTOMLStructureScanner;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 *
 * @author antonio
 */
@LanguageRegistration(mimeType = CargoDataObject.MIME_TYPE, useMultiview = true)
public class CargoTOMLLanguageConfig extends DefaultLanguageConfig {

    private static final Language<CargoTOMLTokenID> CARGO_TOML_LANGUAGE = new CargoTOMLLanguage().language();

    @Override
    public Language<CargoTOMLTokenID> getLexerLanguage() {
        return CARGO_TOML_LANGUAGE;
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new CargoTOMLStructureScanner();
    }

    @Override
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public Parser getParser() {
        return new CargoTOMLLanguageParser();
    }

    @Override
    public SemanticAnalyzer<CargoTOMLLanguageParser.CargoTOMLLanguageParserResult> getSemanticAnalyzer() {
        return new CargoTOMLSemanticAnalyzer();
    }

    @Override
    public String getDisplayName() {
        return "Cargo.toml"; // NOI18N
    }

    @Messages("LBL_Cargo_EDITOR=&Source")
    @MultiViewElement.Registration(
            displayName = "#LBL_Cargo_EDITOR",
            iconBase = "org/netbeans/modules/rust/cargo/filetype/cargo.toml.png",
            mimeType = MIME_TYPE,
            persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
            preferredID = "Cargo",
            position = 1000
    )
    public static MultiViewEditorElement createEditor(Lookup context) {
        return new MultiViewEditorElement(context);
    }


}
