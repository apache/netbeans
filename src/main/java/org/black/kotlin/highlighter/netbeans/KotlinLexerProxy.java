package org.black.kotlin.highlighter.netbeans;

import org.black.kotlin.highlighter.KotlinTokenScanner;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author РђР»РµРєСЃР°РЅРґСЂ
 */
public class KotlinLexerProxy implements Lexer<KotlinTokenId> {
    
    private final LexerRestartInfo<KotlinTokenId> info;
    private KotlinTokenScanner kotlinTokenScanner;
    private final LexerInput input;
    private boolean firstTime = true;
    
    public KotlinLexerProxy(LexerRestartInfo<KotlinTokenId> info){
        this.info = info;
        input = info.input();
        kotlinTokenScanner = null;
    }
    
    @Override
    public Token<KotlinTokenId> nextToken(){
        if (firstTime == true){
            kotlinTokenScanner = new KotlinTokenScanner(input);
            firstTime = false;
        }
        
        
        KotlinToken token = kotlinTokenScanner.getNextToken();
        
        if (input.readLength() < 1) {
            return null;
        }
        
        
        if (token == null)
            return info.tokenFactory().createToken(KotlinLanguageHierarchy.getToken(7));
        

        return info.tokenFactory().createToken(KotlinLanguageHierarchy.getToken(token.id().ordinal()));
    }
    
    @Override
    public Object state() {
        return null;
    }

    @Override
    public void release() {
    }
    
}
