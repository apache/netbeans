/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2.view;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.View;

/**
 *
 * @author Miloslav Metelka
 */
public class TestHighlightsView extends EditorView {
    
    // -J-Dorg.netbeans.modules.editor.lib2.view.HighlightsView.level=FINE
    private static final Logger LOG = Logger.getLogger(HighlightsView.class.getName());

    /** Offset of start offset of this view. */
    private int rawEndOffset; // 24-super + 4 = 28 bytes

    /** Length of text occupied by this view. */
    private int length; // 28 + 4 = 32 bytes

    /** Attributes for rendering */
    private AttributeSet attributes; // 32 + 4 = 36 bytes

    public TestHighlightsView(int offset, int length, AttributeSet attributes) {
        super(null);
        assert (length > 0) : "length=" + length + " <= 0"; // NOI18N
        this.rawEndOffset = offset + length;
        this.length = length;
        this.attributes = attributes;
    }

    @Override
    public float getPreferredSpan(int axis) {
        return 1f;
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
    
    public void setStartOffset(int startOffset) {
        setRawEndOffset(startOffset + getLength());
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
    
    public void setAttributes(AttributeSet attrs) {
        this.attributes = attrs;
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
        return modelToViewChecked(offset, alloc, bias, -1);
    }

    public Shape modelToViewChecked(int offset, Shape alloc, Position.Bias bias, int index) {
        return alloc;
    }

    @Override
    public int viewToModelChecked(double x, double y, Shape alloc, Position.Bias[] biasReturn) {
        return viewToModelChecked(x, y, alloc, biasReturn, -1);
    }

    public int viewToModelChecked(double x, double y, Shape alloc, Position.Bias[] biasReturn, int index) {
        return getStartOffset();
    }

    @Override
    public int getNextVisualPositionFromChecked(int offset, Bias bias, Shape alloc, int direction, Bias[] biasRet) {
        return offset;
    }

    @Override
    public void paint(Graphics2D g, Shape alloc, Rectangle clipBounds) {
    }
    
    @Override
    public View breakView(int axis, int offset, float x, float len) {
        return this;
    }

    @Override
    public View createFragment(int p0, int p1) {
        return this;
    }

    @Override
    protected String getDumpName() {
        return "THV";
    }

    @Override
    protected StringBuilder appendViewInfo(StringBuilder sb, int indent, String xyInfo, int importantChildIndex) {
        super.appendViewInfo(sb, indent, xyInfo, importantChildIndex);
        return sb;
    }

    @Override
    public String toString() {
        return appendViewInfo(new StringBuilder(200), 0, "", -1).toString();
    }

}
