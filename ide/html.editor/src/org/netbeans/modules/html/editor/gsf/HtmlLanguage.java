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
package org.netbeans.modules.html.editor.gsf;

import org.netbeans.modules.csl.api.InstantRenamer;
import org.netbeans.modules.html.editor.hints.HtmlHintsProvider;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.CommentHandler;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.parsing.spi.Parser;

@LanguageRegistration(mimeType="text/html", useCustomEditorKit=true) //NOI18N
public class HtmlLanguage extends DefaultLanguageConfig {
    
    public HtmlLanguage() {
    }
   
    @Override
    public CommentHandler getCommentHandler() {
        return new HtmlCommentHandler();
    }

    @Override
    public Language getLexerLanguage() {
        return HTMLTokenId.language();
    }

    @Override
    public boolean isIdentifierChar(char c) {
        return Character.isLetter(c);
    }

    @Override
    public String getDisplayName() {
        return "HTML";
    }
    
    @Override
    public String getPreferredExtension() {
        return "html"; // NOI18N
    }

    // Service registrations
    
    @Override
    public boolean isUsingCustomEditorKit() {
        return true;
    }

    @Override
    public Parser getParser() {
        return new HtmlGSFParser();
    }

    @Override
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new HtmlStructureScanner();
    }

    @Override
    public DeclarationFinder getDeclarationFinder() {
        return new HtmlDeclarationFinder();
    }



    @Override
    public SemanticAnalyzer getSemanticAnalyzer() {
        return new HtmlSemanticAnalyzer();
    }

    @Override
    public KeystrokeHandler getKeystrokeHandler() {
        return new HtmlKeystrokeHandler();
    }

    @Override
    public HintsProvider getHintsProvider() {
        return new HtmlHintsProvider();
    }

    @Override
    public boolean hasHintsProvider() {
        return true;
    }
    
    @Override
    public InstantRenamer getInstantRenamer() {
        return new HtmlRenameHandler();
    }

}
