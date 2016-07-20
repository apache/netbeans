package org.black.kotlin.language;

import org.black.kotlin.diagnostics.netbeans.codefolding.KotlinStructureScanner;
import org.black.kotlin.diagnostics.netbeans.parser.KotlinParser;
import org.black.kotlin.highlighter.netbeans.KotlinTokenId;
import org.black.kotlin.highlighter.occurrences.KotlinOccurrencesFinder;
import org.black.kotlin.highlighter.semanticanalyzer.KotlinSemanticAnalyzer;
import org.netbeans.api.lexer.Language;
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
    
//    @Override
//    public boolean hasStructureScanner(){
//        return true;
//    }
//    
//    @Override
//    public StructureScanner getStructureScanner(){
//        return new KotlinStructureScanner();
//    }
    
//    @Override
//    public boolean hasOccurrencesFinder() {
//        return true;
//    }
//    
//    @Override
//    public OccurrencesFinder getOccurrencesFinder() {
//        return new KotlinOccurrencesFinder();
//    }
    
}