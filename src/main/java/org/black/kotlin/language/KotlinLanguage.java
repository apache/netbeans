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
package org.black.kotlin.language;

import org.black.kotlin.structurescanner.KotlinStructureScanner;
import org.black.kotlin.diagnostics.netbeans.parser.KotlinParser;
import org.black.kotlin.highlighter.netbeans.KotlinTokenId;
import org.black.kotlin.highlighter.occurrences.KotlinOccurrencesFinder;
import org.black.kotlin.highlighter.semanticanalyzer.KotlinSemanticAnalyzer;
import org.black.kotlin.hints.KotlinHintsProvider;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.parsing.spi.Parser;

/**
 * Class that is responsible for Kotlin language registration.
 * @author Александр
 */
@LanguageRegistration(mimeType = "text/x-kt")
public class KotlinLanguage extends DefaultLanguageConfig {

    @Override
    public Language getLexerLanguage() {
        return KotlinTokenId.getLanguage();
    }

    @Override
    public String getDisplayName() {
        return "KT";
    }

    @Override
    public Parser getParser(){
        return new KotlinParser();
    }

    @Override
    public String getLineCommentPrefix() {
        return "//";
    } 
    
    @Override
    public SemanticAnalyzer getSemanticAnalyzer() {
        return new KotlinSemanticAnalyzer();
    }
    
    @Override
    public boolean hasStructureScanner(){
        return true;
    }
    
    @Override
    public StructureScanner getStructureScanner(){
        return new KotlinStructureScanner();
    }
    
    @Override
    public boolean hasHintsProvider() {
        return true;
    }
    
    @Override 
    public HintsProvider getHintsProvider() {
        return new KotlinHintsProvider();
    }
    
    @Override
    public boolean hasOccurrencesFinder() {
        return true;
    }
    
    @Override
    public OccurrencesFinder getOccurrencesFinder() {
        return new KotlinOccurrencesFinder();
    }
    
}