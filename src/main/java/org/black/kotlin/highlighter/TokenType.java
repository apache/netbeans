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