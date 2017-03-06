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
package org.jetbrains.kotlin.highlighter

import org.jetbrains.kotlin.lexer.KtTokens
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens

class KotlinTokensFactory {

    fun getToken(leafElement: PsiElement): TokenType {
        if (leafElement !is LeafPsiElement) return TokenType.UNDEFINED

        val elementType = leafElement.getElementType()
        return when {
            elementType in KtTokens.KEYWORDS ||
                    elementType in KtTokens.SOFT_KEYWORDS ||
                    elementType in KtTokens.MODIFIER_KEYWORDS -> TokenType.KEYWORD

            elementType in KtTokens.STRINGS ||
                    KtTokens.OPEN_QUOTE == elementType ||
                    KtTokens.CLOSING_QUOTE == elementType -> TokenType.STRING

            KtTokens.IDENTIFIER == elementType -> TokenType.IDENTIFIER
            
            elementType in KtTokens.WHITESPACES -> TokenType.WHITESPACE

            elementType == KtTokens.EOL_COMMENT -> TokenType.SINGLE_LINE_COMMENT

            elementType in KtTokens.COMMENTS ||
                    elementType in KDocTokens.KDOC_HIGHLIGHT_TOKENS -> TokenType.MULTI_LINE_COMMENT

            KDocTokens.TAG_NAME == elementType -> TokenType.KDOC_LINK

            else -> TokenType.UNDEFINED
        }
    }

}