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
package org.jetbrains.kotlin.highlighter.netbeans;

import org.jetbrains.kotlin.language.KotlinLanguageHierarchy;
import org.jetbrains.kotlin.highlighter.KotlinTokenScanner;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * This class parses code from editor. It uses {@link KotlinTokenScanner}.
 * @author Александр
 */
public class KotlinLexerProxy implements Lexer<KotlinTokenId> {
    
    private final LexerRestartInfo<KotlinTokenId> info;
    private KotlinTokenScanner kotlinTokenScanner;
    private final LexerInput input;
    private boolean firstTime = true;
    
    public KotlinLexerProxy(LexerRestartInfo<KotlinTokenId> info){
        this.info = info;
        input = info.input();
    }
    
    /**
     * Returns a token to highlight. This method gets the token from 
     * {@link KotlinTokenScanner#getNextToken() getNextToken} method.
     * @return {@link Token}
     */
    @Override
    public Token<KotlinTokenId> nextToken(){
        if (firstTime){
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
