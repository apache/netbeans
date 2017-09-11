/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.lib.editor.util.ArrayUtilities;

/**
 * Utilities related to rectangular selection.
 *
 * @author Miloslav Metelka
 */
public class RectangularSelectionUtils {
    
    /** Boolean property defining whether rectangular should be reset after document change */
    public static final String RECTANGULAR_DO_NOT_RESET_AFTER_DOCUMENT_CHANGE = "rectangular-document-change-allowed"; // NOI18N
    
    /** Boolean property defining whether selection is being rectangular in a particular text component. */
    private static final String RECTANGULAR_SELECTION_PROPERTY = "rectangular-selection"; // NOI18N

    /** List of positions (with even size) defining regions of rectangular selection. Maintained by BaseCaret. */
    private static final String RECTANGULAR_SELECTION_REGIONS_PROPERTY = "rectangular-selection-regions"; // NOI18N
    
    public static void resetRectangularSelection(JTextComponent c) {
        c.getCaretPosition();
        c.putClientProperty(RECTANGULAR_SELECTION_REGIONS_PROPERTY, new ArrayList<Position>());
        boolean value = !isRectangularSelection(c);
        RectangularSelectionUtils.setRectangularSelection(c, Boolean.valueOf(value) );
        RectangularSelectionUtils.setRectangularSelection(c, Boolean.valueOf(!value));
    }
    
    public static boolean isRectangularSelection(JComponent c) {
        return Boolean.TRUE.equals(c.getClientProperty(RECTANGULAR_SELECTION_PROPERTY));
    }

    public static void setRectangularSelection(JComponent c, boolean value) {
        c.putClientProperty(RECTANGULAR_SELECTION_PROPERTY, value);
    }

    public static String getRectangularSelectionProperty() {
        return RECTANGULAR_SELECTION_PROPERTY;
    }

    public static List<Position> regionsCopy(JComponent c) {
        @SuppressWarnings("unchecked") List<Position> regions =
                (List<Position>) c.getClientProperty(RECTANGULAR_SELECTION_REGIONS_PROPERTY);
        List<Position> lRegions;
        if (regions != null) {
            synchronized (regions) {
                lRegions = new ArrayList<Position>(regions);
            }
        } else {
            lRegions = null;
        }
        return lRegions;
    }

    public static boolean removeSelection(JTextComponent tc) throws BadLocationException {
        List<Position> regions = regionsCopy(tc);
        return removeSelection(tc.getDocument(), regions);
    }

    public static boolean removeSelection(Document doc, List<Position> regions) throws BadLocationException {
        boolean textRemoved = false;
        int regionsLength = regions.size();
        for (int i = 0; i < regionsLength; ) {
            int startOffset = regions.get(i++).getOffset();
            int endOffset = regions.get(i++).getOffset();
            int len = endOffset - startOffset;
            doc.remove(startOffset, len);
            textRemoved |= (len > 0);
        }
        return textRemoved;
    }
    
    public static void removeChar(JTextComponent tc, boolean nextChar) throws BadLocationException {
        List<Position> regions = regionsCopy(tc);
        Document doc = tc.getDocument();
        doc.putProperty(RECTANGULAR_DO_NOT_RESET_AFTER_DOCUMENT_CHANGE, Boolean.TRUE);
        int regionsLength = regions.size();
        Element lineRoot = null;
        int lineIndex = 0;
        for (int i = 1; i < regionsLength; i += 2) {
            int offset = regions.get(i).getOffset();
            if (lineRoot == null) {
                lineRoot = doc.getDefaultRootElement();
                lineIndex = lineRoot.getElementIndex(offset);
            }
            Element line = lineRoot.getElement(lineIndex++);
            if (nextChar) {
                if (offset < line.getEndOffset() - 1) {
                    doc.remove(offset, 1);
                }
            } else { // Previous char (Backspace)
                if (offset > line.getStartOffset()) {
                    doc.remove(offset - 1, 1);
                }
            }
        }
    }

    public static void insertText(Document doc, List<Position> regions, String text) throws BadLocationException {
        doc.putProperty(RECTANGULAR_DO_NOT_RESET_AFTER_DOCUMENT_CHANGE, Boolean.TRUE);
        int regionsLength = regions.size();
        for (int i = 1; i < regionsLength; i += 2) {
            int offset = regions.get(i).getOffset();
            doc.insertString(offset, text, null);
        }
    }

    public static String regionsToString(List<Position> regions) {
        StringBuilder sb = new StringBuilder(200);
        int regionsLength = regions.size();
        int digitCount = ArrayUtilities.digitCount(regionsLength >>> 1);
        for (int i = 0; i < regionsLength; ) {
            int startOffset = regions.get(i++).getOffset();
            int endOffset = regions.get(i++).getOffset();
            ArrayUtilities.appendBracketedIndex(sb, i >>> 1, digitCount);
            sb.append('<').append(startOffset).append(',').append(endOffset).append(">\n"); // NOI18N
        }
        return sb.toString();
    }
    
}
