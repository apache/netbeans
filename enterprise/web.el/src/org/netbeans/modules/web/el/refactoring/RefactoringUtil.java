/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.el.refactoring;

import com.sun.el.parser.Node;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.Position.Bias;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.web.el.ELElement;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionRef;
import org.openide.util.Parameters;

/**
 * XXX: need a common module for sharing code with refactorings, same stuff here
 * as e.g. in jpa.refactoring.
 */
public final class RefactoringUtil {

    private RefactoringUtil() {
    }

    /**
     * Encodes angle brackets and highlights the {@code offsetRange} within
     * in the given {@code expressionOffset}.
     * @param text the full text within which the given offsets must be.
     * @param expressionOffset the offset of the full expression (within
     *  the given {@code text}).
     * @param offsetRange the range within {@code expressionOffset} to highlight.
     * @return
     */
    static String encodeAndHighlight(String text, OffsetRange expressionOffset, OffsetRange nodeOffset) {
        StringBuilder result = new StringBuilder(text.length() + 7);
        String expression = text.substring(expressionOffset.getStart(), expressionOffset.getEnd());
        result.append(encodeAngleBrackets(text.substring(0, expressionOffset.getStart())));
        result.append(highlight(expression, nodeOffset));
        result.append(encodeAngleBrackets(text.substring(expressionOffset.getEnd())));
        return result.toString();
    }
    
    private static String encodeAngleBrackets(String str) {
        return str.replace("<", "&lt;").replace(">", "&gt;"); //NOI18N
    }

    private static String highlight(String text, OffsetRange offsetRange) {
        StringBuilder result = new StringBuilder(text.length() + 7);
        result.append(text.substring(0, offsetRange.getStart()));
        result.append("<b>");
        result.append(encodeAngleBrackets(text.subSequence(offsetRange.getStart(), offsetRange.getEnd()).toString()));
        result.append("</b>");
        result.append(text.substring(offsetRange.getEnd()));
        return result.toString();
    }

    /**
     * Gets the name of the property associated with the given accessor.
     *
     * @param accessor the name of the accessor method of the property. Must follow the JavaBeans
     * naming conventions, i.e. start with 'get/set/is' followed by an uppercase letter,
     * otherwise it is assumed that the name of the property directly matches with
     * the getter. Must not be null or empty.
     *
     * @return the property name resolved from the given <code>getter</code>, i.e.
     * if the given arg was <code>getProperty</code>, this method will return
     * <code>property</code>.
     */
    public static String getPropertyName(String accessor) {
        return getPropertyName(accessor, null, false);
    }

    /**
     * Gets the name of the property associated with the given accessor and return type. Use this method in cases that
     * the accessor represents a methodName.
     *
     * @param accessor the name of the accessor method of the property. Must follow the JavaBeans
     * naming conventions, i.e. start with 'get/set/is' followed by an uppercase letter,
     * otherwise it is assumed that the name of the property directly matches with
     * the getter. Must not be null or empty.
     * @param returnType {@code TypeMirror} of the return type if the accessor represents a method,
     * {@code null} otherwise
     *
     * @return the property name resolved from the given <code>getter</code>, i.e.
     * if the given arg was <code>getProperty</code>, this method will return
     * <code>property</code>.
     */
    public static String getPropertyName(String accessor, TypeMirror returnType) {
        //XXX: leaving out 'set' for refactoring, need more clever AST analysis to be able to
        // tell apart getters and setters in EL
        return getPropertyName(accessor, returnType, false);
    }

    public static String getPropertyName(String accessor, TypeMirror returnType, boolean includeSetter) {
        Parameters.notEmpty("accessor", accessor); //NO18N
        int prefixLength = getPrefixLength(accessor, includeSetter);
        String withoutPrefix = accessor.substring(prefixLength);
        if (withoutPrefix.isEmpty()) { // method name is simply is/get/set
            return accessor;
        }
        char firstChar = withoutPrefix.charAt(0);

        if (!Character.isUpperCase(firstChar)) {
            return accessor;
        }

        //method property which is prefixed by 'is' but doesn't return boolean
        if (returnType != null && accessor.startsWith("is") && returnType.getKind() != TypeKind.BOOLEAN) { //NOI18N
            return accessor;
        }

        //check the second char, if its also uppercase, the property name must be preserved
        if(withoutPrefix.length() > 1 && Character.isUpperCase(withoutPrefix.charAt(1))) {
            return withoutPrefix;
        }

        return Character.toLowerCase(firstChar) + withoutPrefix.substring(1);
    }
    
    public static boolean isPropertyAccessor(String accessor, TypeMirror returnType) {
        return !getPropertyName(accessor, returnType).equals(accessor);
    }
    
    private static int getPrefixLength(String accessor, boolean includeSetter) {
        List<String> accessorPrefixes = new ArrayList<>();
        accessorPrefixes.add("get");
        if (includeSetter) {
            accessorPrefixes.add("set");
        }
        accessorPrefixes.add("is");

        for (String prefix : accessorPrefixes) {
            if (accessor.startsWith(prefix)) {
                return prefix.length();
            }
        }
        return 0;
    }

    static PositionRef[] getPostionRefs(ELElement elem, Node targetNode) {
        OffsetRange nodeRange = elem.getOriginalOffset(targetNode);

        CloneableEditorSupport editor = GsfUtilities.findCloneableEditorSupport(elem.getSnapshot().getSource().getFileObject());
        if (editor != null) {
            PositionRef start = editor.createPositionRef(nodeRange.getStart(), Bias.Forward);
            PositionRef end = editor.createPositionRef(nodeRange.getEnd(), Bias.Backward);
            return new PositionRef[]{start, end};
        }
        return new PositionRef[0];
    }
}
