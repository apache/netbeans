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


import org.jetbrains.kotlin.lexer.KtTokens;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens;
/**
 * Factory of possible Kotlin tokens.
 * @author Александр
 */
public class KotlinTokensFactory {

    TokenType keywordToken = TokenType.KEYWORD;
    TokenType identifierToken = TokenType.IDENTIFIER;
    TokenType stringToken = TokenType.STRING;
    TokenType singleLineCommentToken = TokenType.SINGLE_LINE_COMMENT;
    TokenType multiLineCommentToken = TokenType.MULTI_LINE_COMMENT;
    TokenType kdocTagNameToken = TokenType.KDOC_TAG_NAME;
    TokenType whitespaceToken = TokenType.WHITESPACE;
    TokenType annotationToken = TokenType.ANNOTATION;
    TokenType kdocLink = TokenType.KDOC_LINK;
    
    /**
     * Returns {@link TokenType} based on input {@link PsiElement}.
     * @param leafElement input PsiElement.
     * @return {@link TokenType}
     */
    TokenType getToken(PsiElement leafElement){
        if (!(leafElement instanceof LeafPsiElement))
            return TokenType.UNDEFINED;
        
        IElementType elementType = leafElement.getNode().getElementType();
        if (KtTokens.KEYWORDS.contains(elementType) 
                || KtTokens.SOFT_KEYWORDS.contains(elementType) 
                        || KtTokens.MODIFIER_KEYWORDS.contains(elementType)){
            return keywordToken;
        } 
        else if (KtTokens.STRINGS.contains(elementType) || elementType == KtTokens.OPEN_QUOTE
                || elementType == KtTokens.CLOSING_QUOTE){
            return stringToken;
        }
        else if (elementType == KtTokens.IDENTIFIER){
            return identifierToken;
        }
        else if (KtTokens.WHITESPACES.contains(elementType)){
            return whitespaceToken;
        }
        else if (elementType == KtTokens.EOL_COMMENT){
            return singleLineCommentToken;
        }
        else if (KtTokens.COMMENTS.contains(elementType) 
                || KDocTokens.KDOC_HIGHLIGHT_TOKENS.contains(elementType)){
            return multiLineCommentToken;
        }
        else if (elementType == KDocTokens.TAG_NAME){
            return kdocTagNameToken;
        }
        else if (elementType == KtTokens.ANNOTATION_KEYWORD ){
            return annotationToken;
        }
        else if (elementType == KDocTokens.MARKDOWN_INLINE_LINK ||
                elementType == KDocTokens.MARKDOWN_LINK){
            return kdocLink;
        }
        else 
            return TokenType.UNDEFINED;
    }
    
}
