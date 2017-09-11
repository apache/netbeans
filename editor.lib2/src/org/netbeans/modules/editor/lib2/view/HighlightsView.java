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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.editor.lib2.view;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.View;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

/**
 * View with highlights. This is the most used view.
 * <br>
 * It can either have no highlights (attributes == null) or simple attributes
 * (attributes == non-
 *
 * @author Miloslav Metelka
 */

public class HighlightsView extends EditorView {

    // -J-Dorg.netbeans.modules.editor.lib2.view.HighlightsView.level=FINE
    private static final Logger LOG = Logger.getLogger(HighlightsView.class.getName());

    /** Offset of start offset of this view. */
    private int rawEndOffset; // 24-super + 4 = 28 bytes

    /** Length of text occupied by this view. */
    private int length; // 28 + 4 = 32 bytes

    /**
     * Attributes for rendering. It can be CompoundAttributes instance
     * in which case the getAttributes() methods must return "this".
     */
    private final AttributeSet attributes; // 32 + 4 = 36 bytes

    /**
     * TextLayout or null if not initialized (or cleared).
     */
    private TextLayout textLayout; // 36 + 4 = 40 bytes
    
    /**
     * Cache width since TextLayout.getCaretInfo() is non-trivial.
     */
    private float width; // 40 + 4 = 44 bytes
    
    /**
     * Last successful text layout breaking should speed up repetitive line wrapping.
     */
    private TextLayoutBreakInfo breakInfo; // 44 + 4 = 48 bytes

    public HighlightsView(int length, AttributeSet attributes) {
        super(null);
        assert (length > 0) : "length=" + length + " <= 0"; // NOI18N
        this.length = length;
        this.attributes = attributes;
    }

    @Override
    public float getPreferredSpan(int axis) {
        checkTextLayoutValid();
        if (axis == View.X_AXIS) {
            return getWidth();
        } else {
            EditorView.Parent parent = (EditorView.Parent) getParent();
            return (parent != null) ? parent.getViewRenderContext().getDefaultRowHeight() : 0f;
        }
    }
    
    float getWidth() {
        return width;
    }
    
    @Override
    public int getRawEndOffset() {
        return rawEndOffset;
    }

    @Override
    public void setRawEndOffset(int rawOffset) {
        this.rawEndOffset = rawOffset;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public int getStartOffset() {
        return getEndOffset() - getLength();
    }

    @Override
    public int getEndOffset() {
        EditorView.Parent parent = (EditorView.Parent) getParent();
        return (parent != null) ? parent.getViewEndOffset(rawEndOffset) : rawEndOffset;
    }

    @Override
    public Document getDocument() {
        View parent = getParent();
        return (parent != null) ? parent.getDocument() : null;
    }

    @Override
    public AttributeSet getAttributes() {
        return attributes;
    }

    /**
     * @return Valid textLayout instance or TextLayoutPart or null.
     */
    TextLayout getTextLayout() {
        return textLayout;
    }
    
    void setTextLayout(TextLayout textLayout, float width) {
        this.textLayout = textLayout;
        this.width = width;
    }
    
    TextLayoutBreakInfo getBreakInfo() {
        return breakInfo;
    }

    public void setBreakInfo(TextLayoutBreakInfo breakInfo) {
        this.breakInfo = breakInfo;
    }
    
    TextLayout createPartTextLayout(int shift, int length) {
        checkTextLayoutValid();
        if (breakInfo == null) {
            breakInfo = new TextLayoutBreakInfo(textLayout.getCharacterCount());
        }
        TextLayout partTextLayout = breakInfo.findPartTextLayout(shift, length);
        if (partTextLayout == null) {
            DocumentView docView = getDocumentView();
            Document doc = docView.getDocument();
            CharSequence docText = DocumentUtilities.getText(doc);
            int startOffset = getStartOffset();
            String text = docText.subSequence(startOffset + shift, startOffset + shift + length).toString();
            if (docView.op.isNonPrintableCharactersVisible()) {
                text = text.replace(' ', DocumentViewOp.PRINTING_SPACE);
            }
            AttributeSet attrs = ViewUtils.getFirstAttributes(getAttributes());
            Font font = ViewUtils.getFont(attrs, docView.op.getDefaultFont());
            partTextLayout = docView.op.createTextLayout(text, font);
            breakInfo.add(shift, length, partTextLayout);
        }
        return partTextLayout;
    }

    ParagraphView getParagraphView() {
        return (ParagraphView) getParent();
    }

    DocumentView getDocumentView() {
        ParagraphView paragraphView = getParagraphView();
        return (paragraphView != null) ? paragraphView.getDocumentView() : null;
    }

    @Override
    public Shape modelToViewChecked(int offset, Shape alloc, Position.Bias bias) {
        checkTextLayoutValid();
        int relOffset = Math.max(0, offset - getStartOffset());
        Shape ret = HighlightsViewUtils.indexToView(textLayout, null, relOffset, bias, getLength(), alloc);
        return ret;
    }

    @Override
    public int viewToModelChecked(double x, double y, Shape hViewAlloc, Position.Bias[] biasReturn) {
        checkTextLayoutValid();
        int offset = getStartOffset() +
                HighlightsViewUtils.viewToIndex(textLayout, x, hViewAlloc, biasReturn);
                
        return offset;
    }

    @Override
    public int getNextVisualPositionFromChecked(int offset, Bias bias, Shape alloc, int direction, Bias[] biasRet) {
        checkTextLayoutValid();
        int startOffset = getStartOffset();
        return HighlightsViewUtils.getNextVisualPosition(
                offset, bias, alloc, direction, biasRet,
                textLayout, startOffset, startOffset, getLength(), getDocumentView());
    }

    @Override
    public void paint(Graphics2D g, Shape hViewAlloc, Rectangle clipBounds) {
        checkTextLayoutValid();
        int viewStartOffset = getStartOffset();
        DocumentView docView = getDocumentView();
        // TODO render only necessary parts
        HighlightsViewUtils.paintHiglighted(g, hViewAlloc, clipBounds,
                docView, this, viewStartOffset,
                textLayout, viewStartOffset, 0, getLength());
    }
    
    @Override
    public View breakView(int axis, int offset, float x, float len) {
        checkTextLayoutValid();
        View part = HighlightsViewUtils.breakView(axis, offset, x, len, this, 0, getLength(), textLayout);
        return (part != null) ? part : this;
    }

    @Override
    public View createFragment(int p0, int p1) {
        checkTextLayoutValid();
        int startOffset = getStartOffset();
        ViewUtils.checkFragmentBounds(p0, p1, startOffset, getLength());
        return new HighlightsViewPart(this, p0 - startOffset, p1 - p0);
    }

    private void checkTextLayoutValid() {
        DocumentView docView;
        assert (textLayout != null) : "TextLayout is null in " + this + // NOI18N
                (((docView = getDocumentView()) != null) ? "\nDOC-VIEW:\n" + docView.toStringDetailNeedsLock() : ""); // NOI18N
    }

    @Override
    protected String getDumpName() {
        return "HV";
    }

    @Override
    protected StringBuilder appendViewInfo(StringBuilder sb, int indent, String xyInfo, int importantChildIndex) {
        super.appendViewInfo(sb, indent, xyInfo, importantChildIndex);
        sb.append(" TL=");
        if (textLayout == null) {
            sb.append("<NULL>");
        } else {
            sb.append(TextLayoutUtils.toStringShort((TextLayout) textLayout));
        }
        return sb;
    }
    
    @Override
    public String toString() {
        return appendViewInfo(new StringBuilder(200), 0, "", -1).toString();
    }

}
