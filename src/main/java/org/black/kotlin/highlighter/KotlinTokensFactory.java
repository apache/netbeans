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
        else 
            return TokenType.UNDEFINED;
    }
    
}
