package org.black.kotlin.highlighter;

/**
 *
 * @author Александр
 */

public enum TokenType{
    KEYWORD, IDENTIFIER, STRING, SINGLE_LINE_COMMENT,
    MULTI_LINE_COMMENT, KDOC_TAG_NAME, WHITESPACE,
    UNDEFINED, EOF;
    
    public int getId(TokenType type){
        switch(type){
            case KEYWORD:
                return 0;
            case IDENTIFIER:
                return 1;
            case STRING:
                return 2;
            case SINGLE_LINE_COMMENT:
                return 3;
            case MULTI_LINE_COMMENT:
                return 4;
            case KDOC_TAG_NAME:
                return 5;
            case WHITESPACE:
            case UNDEFINED:
            case EOF:
                return 6;
        }
        
        return 0;
    }
    
}