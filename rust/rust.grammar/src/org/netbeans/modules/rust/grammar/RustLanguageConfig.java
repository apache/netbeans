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
package org.netbeans.modules.rust.grammar;

import org.netbeans.modules.rust.grammar.structure.RustStructureScanner;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.parsing.spi.Parser;

/**
 *
 */
@LanguageRegistration(mimeType = "text/x-rust", useMultiview = true)
public class RustLanguageConfig extends DefaultLanguageConfig {

    private static final Language<RustTokenID> RUST_LANGUAGE = new RustLanguage().language();

    @Override
    public Language<RustTokenID> getLexerLanguage() {
        return RUST_LANGUAGE;
    }

    @Override
    public String getDisplayName() {
        return "Rust"; // NOI18N
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new RustStructureScanner();
    }

    @Override
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public Parser getParser() {
        return new RustLanguageParser();
    }

    @Override
    public String getLineCommentPrefix() {
        return "//"; // NOI18N
    }

    @Override
    public DeclarationFinder getDeclarationFinder() {
        return new RustDeclarationFinder();
    }

}
