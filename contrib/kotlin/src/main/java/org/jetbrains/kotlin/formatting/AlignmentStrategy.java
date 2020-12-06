/**
 * *****************************************************************************
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
 ******************************************************************************
 */
package org.jetbrains.kotlin.formatting;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtClassInitializer;
import org.jetbrains.kotlin.psi.KtFunction;
import org.jetbrains.kotlin.psi.KtPropertyAccessor;

/**
 *
 * @author Alexander.Baratynski
 */
public class AlignmentStrategy {

    private static final Set<String> BLOCK_ELEMENT_TYPES = new HashSet<>(Arrays.asList(
            "IF", "FOR", "WHILE", "CLASS_BODY", "FUNCTION_LITERAL_EXPRESSION",
            "WHEN", "WHEN_ENTRY", "DO_WHILE", "PROPERTY_ACCESSOR"));

    public AlignmentStrategy(ASTNode parsedFile, int lineIndentation, String lineDelimiter) {
    }

    public static int computeIndent(ASTNode node) {
        int indent = 0;
        while (node != null) {
            indent = AlignmentStrategy.updateIndent(node, indent);
            node = node.getTreeParent();
        }

        return indent;
    }

    public static int updateIndent(ASTNode node, int indent) {
        return isBlockElement(node) ? indent + 1 : indent;
    }

    public static boolean isBlockElement(ASTNode node) {
        IElementType elementType = node.getElementType();
        if ("BLOCK".equals(elementType.toString())) {
            PsiElement parent = node.getPsi().getParent();
            if (parent instanceof KtFunction || parent instanceof KtClass || parent instanceof KtClassInitializer
                    || parent instanceof KtPropertyAccessor) {
                return true;
            }
        } else if (BLOCK_ELEMENT_TYPES.contains(elementType.toString())) {
            return true;
        }

        return false;
    }

}
