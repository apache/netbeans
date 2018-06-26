/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        result.append(encodeAngleBrackets(text.substring(expressionOffset.getEnd(), text.length())));
        return result.toString();
    }
    
    private static String encodeAngleBrackets(String str) {
        return str.replaceAll("<", "&lt;").replaceAll(">", "&gt;"); //NOI18N
    }

    private static String highlight(String text, OffsetRange offsetRange) {
        StringBuilder result = new StringBuilder(text.length() + 7);
        result.append(text.substring(0, offsetRange.getStart()));
        result.append("<b>");
        result.append(encodeAngleBrackets(text.subSequence(offsetRange.getStart(), offsetRange.getEnd()).toString()));
        result.append("</b>");
        result.append(text.substring(offsetRange.getEnd(), text.length()));
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
