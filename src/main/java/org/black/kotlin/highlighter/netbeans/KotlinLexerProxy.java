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
        //KotlinCharStream stream = new KotlinCharStream(info.input());
        input = info.input();
        kotlinTokenScanner = new KotlinTokenScanner();
        
//        input.backup(input.readLength());
    }
    
    @Override
    public Token<KotlinTokenId> nextToken(){
        KotlinToken token = kotlinTokenScanner.getNextToken();
//        int ch = input.read();
//        while (ch != 10 || ch != LexerInput.EOF){
//            ch = input.read();
//        }
//        if (ch == LexerInput.EOF)
//            return null;
//        DialogDisplayer.getDefault().notify(new NotifyDescriptor.
//            Message(input.readText()));
//        if (input.readLength() < 1) {
//            return null;
//        }
        input.read();
        input.read();
        input.read();
        
        return info.tokenFactory().createToken(KotlinLanguageHierarchy.getToken(0));
        //return token;
    }
    
    @Override
    public Object state() {
        return null;
    }

    @Override
    public void release() {
   
    }
    
}
