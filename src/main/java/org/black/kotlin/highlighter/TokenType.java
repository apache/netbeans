package org.black.kotlin.highlighter;

/**
 * Possible Kotlin token types.
 * @author Александр
 */

public enum TokenType{
    KEYWORD, IDENTIFIER, STRING, SINGLE_LINE_COMMENT,
    MULTI_LINE_COMMENT, KDOC_TAG_NAME, WHITESPACE,
    ANNOTATION, KDOC_LINK, UNDEFINED, EOF;
    
    /**
     * Returns a token id number based on its' type.
     * @return {@link TokenType}
     */
    public int getId(){
        switch(this){
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
                return 6;
            case ANNOTATION:
                return 8;
            case KDOC_LINK:
                return 9;
            case UNDEFINED:
            case EOF:
            default:
                return 7;
        }
    }
    
}