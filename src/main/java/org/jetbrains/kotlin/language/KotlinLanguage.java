/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.language;

import org.jetbrains.kotlin.completion.KotlinCodeCompletionHandler;
import org.jetbrains.kotlin.structurescanner.KotlinStructureScanner;
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser;
import org.jetbrains.kotlin.diagnostics.netbeans.textinterceptor.KotlinKeystrokeHandler;
import org.jetbrains.kotlin.highlighter.netbeans.KotlinTokenId;
import org.jetbrains.kotlin.highlighter.occurrences.KotlinOccurrencesFinder;
import org.jetbrains.kotlin.highlighter.semanticanalyzer.KotlinSemanticAnalyzer;
import org.jetbrains.kotlin.hints.KotlinHintsProvider;
import org.jetbrains.kotlin.indexer.KotlinIndexerFactory;
import org.jetbrains.kotlin.refactorings.rename.KotlinInstantRenamer;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.InstantRenamer;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;

/**
 * Class that is responsible for Kotlin language registration.
 * @author Александр
 */
@LanguageRegistration(mimeType = "text/x-kt")
public class KotlinLanguage extends DefaultLanguageConfig {

    private final Parser kotlinParser = new KotlinParser();
    private final SemanticAnalyzer kotlinSemanticAnalyzer = 
            new KotlinSemanticAnalyzer();
    private final StructureScanner kotlinStructureScanner = 
            new KotlinStructureScanner();
    private final HintsProvider kotlinHintsProvider = 
            new KotlinHintsProvider();
    private final OccurrencesFinder kotlinOccurrencesFinder = 
            new KotlinOccurrencesFinder();
    private final CodeCompletionHandler kotlinCompletionHandler =
            new KotlinCodeCompletionHandler();
    
    @Override
    public Language getLexerLanguage() {
        return KotlinTokenId.Companion.getLanguage();
    }

    @Override
    public String getDisplayName() {
        return "KT";
    }

    @Override
    public Parser getParser(){
        return kotlinParser;
    }

    @Override
    public String getLineCommentPrefix() {
        return "//";
    } 
    
    @Override
    public SemanticAnalyzer getSemanticAnalyzer() {
        return kotlinSemanticAnalyzer;
    }
    
    @Override
    public boolean hasStructureScanner(){
        return true;
    }
    
    @Override
    public StructureScanner getStructureScanner(){
        return kotlinStructureScanner;
    }
    
    @Override
    public boolean hasHintsProvider() {
        return true;
    }
    
    @Override 
    public HintsProvider getHintsProvider() {
        return kotlinHintsProvider;
    }
    
    @Override
    public boolean hasOccurrencesFinder() {
        return true;
    }
    
    @Override
    public OccurrencesFinder getOccurrencesFinder() {
        return kotlinOccurrencesFinder;
    }
    
    @Override
    public CodeCompletionHandler getCompletionHandler() {
        return kotlinCompletionHandler;
    }
    
    @Override
    public InstantRenamer getInstantRenamer() {
        return new KotlinInstantRenamer();
    }
    
    @Override
    public EmbeddingIndexerFactory getIndexerFactory() {
        return new KotlinIndexerFactory();
    }
    
    @Override
    public KeystrokeHandler getKeystrokeHandler() {
        return new KotlinKeystrokeHandler();
    }
    
}