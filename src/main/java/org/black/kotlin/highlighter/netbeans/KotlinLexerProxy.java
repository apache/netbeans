package org.black.kotlin.highlighter.netbeans;

import org.black.kotlin.highlighter.KotlinTokenScanner;
import org.black.kotlin.highlighter.TokenType;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Александр
 */
public class KotlinLexerProxy implements Lexer<KotlinTokenId> {
    
    private LexerRestartInfo<KotlinTokenId> info;
    private KotlinTokenScanner kotlinTokenScanner;
    private LexerInput input;
    
    public KotlinLexerProxy(LexerRestartInfo<KotlinTokenId> info){
        this.info = info;
        input = info.input();
        kotlinTokenScanner = new KotlinTokenScanner(input);
        
    }
    
    @Override
    public Token<KotlinTokenId> nextToken(){
        KotlinToken token = kotlinTokenScanner.getNextToken();
        
            
        if (input.readLength() < 1) {
            return null;
        }
        
        if (token == null)//token.getType().equals(TokenType.EOF))
            return null;
        
        
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
