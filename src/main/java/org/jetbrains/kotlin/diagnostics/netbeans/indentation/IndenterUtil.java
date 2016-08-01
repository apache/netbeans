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
package org.jetbrains.kotlin.diagnostics.netbeans.indentation;

import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.kotlin.utils.LineEndUtil;
import org.jetbrains.kotlin.lexer.KtTokens;

/**
 *
 * @author Александр
 */
public class IndenterUtil {
    public static final char SPACE_CHAR = ' ';
    public static final char TAB_CHAR = '\t';
    public static final String TAB_STRING = Character.toString(TAB_CHAR);
    public static final int DEFAULT_INDENT = 4;
    
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
        return DEFAULT_INDENT;
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
