package org.black.kotlin.highlighter;


import org.jetbrains.kotlin.lexer.JetTokens;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens;
/**
 *
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
    
    
    TokenType getToken(PsiElement leafElement){
        if (!(leafElement instanceof LeafPsiElement))
            return TokenType.UNDEFINED;
        
        IElementType elementType = leafElement.getNode().getElementType();
        if (JetTokens.KEYWORDS.contains(elementType) 
                || JetTokens.SOFT_KEYWORDS.contains(elementType) 
                        || JetTokens.MODIFIER_KEYWORDS.contains(elementType)){
            return keywordToken;
        } 
        else if (JetTokens.STRINGS.contains(elementType) || elementType == JetTokens.OPEN_QUOTE
                || elementType == JetTokens.CLOSING_QUOTE){
            return stringToken;
        }
        else if (JetTokens.WHITESPACES.contains(elementType)){
            return whitespaceToken;
        }
        else if (elementType == JetTokens.EOL_COMMENT){
            return singleLineCommentToken;
        }
        else if (JetTokens.COMMENTS.contains(elementType) 
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
