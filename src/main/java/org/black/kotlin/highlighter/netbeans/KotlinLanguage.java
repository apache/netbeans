package org.black.kotlin.highlighter.netbeans;

import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;

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

}
