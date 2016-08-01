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
