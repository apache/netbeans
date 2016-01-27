package org.black.kotlin.highlighter.netbeans;

import org.black.kotlin.highlighter.TokenType;
import org.netbeans.api.lexer.TokenId;

/**
 * Custom class for Kotlin token.
 * @author Александр
 */
public class KotlinToken<T extends TokenId>{

    private final KotlinTokenId kotlinTokenId;
    private final String text;
    private final TokenType type;

    public KotlinToken(T val, String text, TokenType type){
        
        kotlinTokenId = (KotlinTokenId) val;
        this.text = text;
        this.type = type;
    }
    

    public TokenId id() {
        return kotlinTokenId;
    }


    public CharSequence text() {
        return text;
    }


    public int length() {
        return text.length();
    }


    public TokenType getType(){
        return type;
    }
}
