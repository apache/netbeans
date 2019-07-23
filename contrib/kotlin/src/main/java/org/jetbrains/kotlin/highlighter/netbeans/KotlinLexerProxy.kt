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
package org.jetbrains.kotlin.highlighter.netbeans

import org.jetbrains.kotlin.language.KotlinLanguageHierarchy
import org.jetbrains.kotlin.highlighter.KotlinTokenScanner
import org.netbeans.api.lexer.Token
import org.netbeans.spi.lexer.Lexer
import org.netbeans.spi.lexer.LexerInput
import org.netbeans.spi.lexer.LexerRestartInfo

class KotlinLexerProxy(private val info: LexerRestartInfo<KotlinTokenId>) : Lexer<KotlinTokenId> {
    
    private val kotlinTokenScanner: KotlinTokenScanner by lazy { KotlinTokenScanner(input) }
    private val input: LexerInput = info.input()
    
    override fun nextToken(): Token<KotlinTokenId>? {
        val token = kotlinTokenScanner.getNextToken()
        
        if (input.readLength() < 1) return null
        
        return if (token == null) {
            info.tokenFactory().createToken(KotlinLanguageHierarchy.getToken(7))
        }  else {
            info.tokenFactory().createToken(KotlinLanguageHierarchy.getToken(token.id().ordinal()))
        }
    }
    
    override fun state() = null
    override fun release() {}
}