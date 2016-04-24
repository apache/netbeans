package org.black.kotlin.diagnostics.netbeans.indentation;

import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.black.kotlin.utils.LineEndUtil;
import org.jetbrains.kotlin.lexer.KtTokens;

/**
 *
 * @author Александр
 */
public class IndenterUtil {
    public static final char SPACE_CHAR = ' ';
    public static final char TAB_CHAR = '\t';
    public static final String TAB_STRING = Character.toString(TAB_CHAR);
    
    public static String createWhiteSpace(int curIndent, int countBreakLines, String lineSeparator) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < countBreakLines; i++) {
            stringBuilder.append(lineSeparator);
        }
        
        String whiteSpace = getIndentString();
        for (int i = 0; i < curIndent; i++) {
            stringBuilder.append(whiteSpace);
        }
        
        return stringBuilder.toString();
    }
    
    public static String getIndentString() {
        if (isSpacesForTabs()) {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < getDefaultIndent(); i++) {
               result.append(SPACE_CHAR);
            }
            return result.toString();
        } else {
            return new Character(TAB_CHAR).toString();
        }
    }
    
    public static int getLineSeparatorsOccurences(String text) {
        int result = 0;
        
        for (char c : text.toCharArray()) {
            if (c == LineEndUtil.NEW_LINE_CHAR) {
                result++;
            }
        }
        
        return result;
    }
    
    public static boolean isNewLine(LeafPsiElement psiElement) {
        return psiElement.getElementType() == KtTokens.WHITE_SPACE && psiElement.getText().contains(LineEndUtil.NEW_LINE_STRING);
    }
    
    public static int getDefaultIndent() {
        return 4;
    }
    
    public static boolean isSpacesForTabs() {
        return true;
    }
    
    public static boolean isWhiteSpaceChar(char c) {
        return c == SPACE_CHAR || c == TAB_CHAR;
    }
    
    public static boolean isWhiteSpaceOrNewLine(char c) {
        return c == SPACE_CHAR || c == TAB_CHAR || c == LineEndUtil.NEW_LINE_CHAR;
    }
}
