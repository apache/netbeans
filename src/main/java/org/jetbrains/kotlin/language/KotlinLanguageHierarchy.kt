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
package org.jetbrains.kotlin.language

import org.netbeans.spi.lexer.LanguageHierarchy
import org.netbeans.spi.lexer.Lexer
import org.netbeans.spi.lexer.LexerRestartInfo
import org.jetbrains.kotlin.highlighter.TokenType
import org.jetbrains.kotlin.highlighter.netbeans.KotlinLexerProxy
import org.jetbrains.kotlin.highlighter.netbeans.KotlinTokenId


class KotlinLanguageHierarchy() : LanguageHierarchy<KotlinTokenId>() {

    companion object {
        private val tokens = listOf(
                KotlinTokenId(TokenType.KEYWORD.name, TokenType.KEYWORD.name, 0),
                KotlinTokenId(TokenType.IDENTIFIER.name, TokenType.IDENTIFIER.name, 1),
                KotlinTokenId(TokenType.STRING.name,TokenType.STRING.name,2),
                KotlinTokenId(TokenType.SINGLE_LINE_COMMENT.name,TokenType.SINGLE_LINE_COMMENT.name,3),
                KotlinTokenId(TokenType.MULTI_LINE_COMMENT.name,TokenType.MULTI_LINE_COMMENT.name,4),
                KotlinTokenId(TokenType.KDOC_TAG_NAME.name,TokenType.KDOC_TAG_NAME.name,5),
                KotlinTokenId(TokenType.WHITESPACE.name,TokenType.WHITESPACE.name,6),
                KotlinTokenId(TokenType.UNDEFINED.name,TokenType.UNDEFINED.name,7),
                KotlinTokenId(TokenType.ANNOTATION.name,TokenType.ANNOTATION.name,8),
                KotlinTokenId(TokenType.KDOC_LINK.name,TokenType.KDOC_LINK.name,9)
        )
        
        fun getToken(id: Int) = tokens.filter{ it.ordinal() == id }.first()
    }
    
    override fun createTokenIds() = tokens
    override fun createLexer(info: LexerRestartInfo<KotlinTokenId>) = KotlinLexerProxy(info)
    override fun mimeType() = "text/x-kt"
}