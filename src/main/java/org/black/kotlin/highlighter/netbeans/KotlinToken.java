package org.black.kotlin.highlighter.netbeans;

import org.black.kotlin.highlighter.TokenType;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;

/**
 * Custom class for Kotlin token.
 * @author Александр
 */
public class KotlinToken<T extends TokenId>{

    private final T val;
    private final KotlinTokenId kotlinTokenId;
    private final String text;
    private final int offset;
    private final TokenType type;

    public KotlinToken(T val, String text, int offset, TokenType type){
        
        this.val = val;
        kotlinTokenId = (KotlinTokenId) this.val;
        this.text = text;
        this.offset = offset;
        this.type = type;
    }
    

    public TokenId id() {
        return kotlinTokenId;
    }


    public CharSequence text() {
        return text;
    }


    public boolean isCustomText() {
        return type == TokenType.STRING || type == TokenType.UNDEFINED;
    }


    public int length() {
        return text.length();
    }


    public int offset(TokenHierarchy th) {
        return offset;
    }


    public boolean isFlyweight() {
        return false;
    }


    public PartType partType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    public boolean hasProperties() {
        return false;
    }


    public Object getProperty(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public TokenType getType(){
        return type;
    }
}
