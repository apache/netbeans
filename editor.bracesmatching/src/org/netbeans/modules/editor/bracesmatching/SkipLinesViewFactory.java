/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.bracesmatching;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;
import org.netbeans.modules.editor.lib2.view.EditorView;
import org.netbeans.modules.editor.lib2.view.EditorViewFactory;
import org.netbeans.modules.editor.lib2.view.ViewRenderContext;
import org.netbeans.modules.editor.lib2.view.ViewUtils;

/**
 * This view factory works in cooperation with {@link BraceMatchingSidebarComponent}. It helps to skip unnecessary lines
 * in the brace match tooltip. Instead of unimportant lines, the NullView will just print indent and ellipsis to suggest
 * some text was skipped.
 * <p/>
 * This factory does not support editor changes; it is only usable in the non-editable popup.
 * 
 * @author sdedic
 */
final class SkipLinesViewFactory extends EditorViewFactory {
    /**
     * Property that holds the suppressed lines info
     */
    public static final String PROP_SUPPRESS_RANGES = "nbeditorui.braces.suppressLines";
    
    /**
     * Value of the suppressed lines info
     */
    private int[] suppressRanges = null;
    
    public SkipLinesViewFactory(View documentView) {
        super(documentView);
        JTextComponent comp = textComponent();
        this.suppressRanges = (int[])comp.getClientProperty(PROP_SUPPRESS_RANGES);
    }

    @Override
    public void restart(int startOffset, int endOffset, boolean createViews) {
        JTextComponent comp = textComponent();
        this.suppressRanges = (int[])comp.getClientProperty(PROP_SUPPRESS_RANGES);
    }

    @Override
    public void continueCreation(int startOffset, int endOffset) {
    }

    @Override
    public int nextViewStartOffset(int offset) {
        if (suppressRanges == null) {
            return Integer.MAX_VALUE;
        }
        if (offset >= suppressRanges[0]) {
            return Integer.MAX_VALUE;
        }
        return suppressRanges[0];
    }

    @Override
    public EditorView createView(int startOffset, int limitOffset, boolean forcedLimit, EditorView origView, int nextOrigViewOffset) {
        return new NullView(textComponent(), suppressRanges[0], suppressRanges[1], suppressRanges[2]);
    }

    @Override
    public int viewEndOffset(int startOffset, int limitOffset, boolean forcedLimit) {
        if (suppressRanges != null && suppressRanges[1] <= limitOffset) {
            return suppressRanges[1];
        }
        return -1;
    }

    @Override
    public void finishCreation() {
    }
    
    /**
     * The actual view class.
     */
    private static class NullView extends EditorView {
        int start;
        int end;
        int rawOffset;
        int indent;
        JTextComponent component;
        
        public NullView(JTextComponent component, int start, int end, int indent) {
            super(null);
            this.start = start;
            this.end = end;
            this.indent = indent;
            this.component = component;
        }
        
        @Override
        public int getStartOffset() {
            return start;
        }
        
        @Override
        public int getEndOffset() {
            return end;
        }
        
        @Override
        public int getRawEndOffset() {
            return rawOffset;
        }

        @Override
        public int getLength() {
            return end - start;
        }

        @Override
        public void setRawEndOffset(int rawEndOffset) {
            this.rawOffset = rawEndOffset;
        }

        @Override
        public void paint(Graphics2D g, Shape alloc, Rectangle clipBounds) {
            Rectangle2D.Double allocBounds = ViewUtils.shape2Bounds(alloc);
            TextLayout textLayout = getTextLayout();
            if (textLayout != null) {
                g.setColor(component.getForeground());
                EditorView.Parent parent = (EditorView.Parent) getParent();
                float ascent = parent.getViewRenderContext().getDefaultAscent();
                float x = (float) allocBounds.getX();
                float y = (float) allocBounds.getY();
                textLayout.draw(g, x, y + ascent);
            }
        }

        @Override
        public Shape modelToViewChecked(int offset, Shape alloc, Position.Bias bias) {
            return alloc;
        }

        @Override
        public int viewToModelChecked(double x, double y, Shape alloc, Position.Bias[] biasReturn) {
            return start;
        }

        @Override
        public float getPreferredSpan(int axis) {
            EditorView.Parent parent = (EditorView.Parent) getParent();
            if (axis == View.X_AXIS) {
                float advance = 0;
                TextLayout textLayout = getTextLayout();
                if (textLayout == null) {
                    return 0f;
                }
                return textLayout.getAdvance();
            } else {
                return (parent != null) ? parent.getViewRenderContext().getDefaultRowHeight() : 0f;
            }
        }
        
        private TextLayout collapsedTextLayout;
        
        private TextLayout getTextLayout() {
            if (collapsedTextLayout == null) {
                EditorView.Parent parent = (EditorView.Parent) getParent();
                ViewRenderContext context = parent.getViewRenderContext();
                FontRenderContext frc = context.getFontRenderContext();
                assert (frc != null) : "Null FontRenderContext"; // NOI18N
                Font font = context.getRenderFont(component.getFont());
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < indent; i++) {
                    sb.append(' ');
                }
                sb.append("...");
                String text = sb.toString();
                collapsedTextLayout = new TextLayout(text, font, frc);
            }
            return collapsedTextLayout;
        }
    }
    
    public static class Factory implements EditorViewFactory.Factory {

        @Override
        public EditorViewFactory createEditorViewFactory(View documentView) {
            return new SkipLinesViewFactory(documentView);
        }

        @Override
        public int weight() {
            return 200;
        }
        
    }
}
