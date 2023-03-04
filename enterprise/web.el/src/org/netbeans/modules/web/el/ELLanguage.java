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

package org.netbeans.modules.web.el;

import org.netbeans.modules.web.el.base.ELCommentHandler;
import org.netbeans.modules.web.el.navigation.ELDeclarationFinder;
import org.netbeans.modules.web.el.completion.ELCodeCompletionHandler;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.spi.CommentHandler;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.el.lexer.api.ELTokenId;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizerRegistration;
import org.netbeans.modules.web.el.hints.ELHintsProvider;

/**
 * CSL language for Expression Language
 *
 * @author Erno Mononen
 */
@LanguageRegistration(mimeType=ELLanguage.MIME_TYPE)
@PathRecognizerRegistration(mimeTypes=ELLanguage.MIME_TYPE, libraryPathIds={}, binaryLibraryPathIds={})
public class ELLanguage extends DefaultLanguageConfig {

    public static final String MIME_TYPE = "text/x-el"; //NOI18N

    @Override
    public Language getLexerLanguage() {
        return ELTokenId.language();
    }

    @Override
    public String getDisplayName() {
        return "EL";
    }

    @Override
    public Parser getParser() {
        return new ELParser();
    }

    @Override
    public EmbeddingIndexerFactory getIndexerFactory() {
        return new ELIndexer.Factory();
    }

    @Override
    public boolean hasHintsProvider() {
        return true;
    }

    @Override
    public HintsProvider getHintsProvider() {
        return new ELHintsProvider();
    }

    @Override
    public boolean hasOccurrencesFinder() {
        return true;
    }

    @Override
    public OccurrencesFinder getOccurrencesFinder() {
        return new ELOccurrencesFinder();
    }

    @Override
    public CodeCompletionHandler getCompletionHandler() {
        return new ELCodeCompletionHandler();
    }

    @Override
    public DeclarationFinder getDeclarationFinder() {
        return new ELDeclarationFinder();
    }

    @Override
    public CommentHandler getCommentHandler() {
        // Expression Language doesn't supports any comments. Instead of that the code is commented out as the XHTML code.
        return new ELCommentHandler();
    }

}
