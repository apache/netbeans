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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.editor.util.GapList;


/**
 * Information about line wrapping that may be attached to {@link ParagraphViewChildren}.
 * <br>
 * Wrapping uses constant wrap line height - children height from {@link ParagraphViewChildren}.
 * 
 * @author Miloslav Metelka
 */

final class WrapInfo extends GapList<WrapLine> {

    // -J-Dorg.netbeans.modules.editor.lib2.view.WrapInfo.level=FINER
    private static final Logger LOG = Logger.getLogger(WrapInfo.class.getName());

    private static final long serialVersionUID  = 0L;

    /**
     */
    float width; // 24 + 4 = 28 bytes
    
    /**
     * Updater in case the update was not finished yet due to lazy operation.
     */
    WrapInfoUpdater updater; // 28 + 4 = 32 bytes

    WrapInfo() {
        super(2);
    }
    
    int wrapLineCount() {
        return size();
    }

    float wrapLineHeight(ParagraphViewChildren children) {
        return children.childrenHeight();
    }
    
    float height(ParagraphViewChildren children) {
        return size() * wrapLineHeight(children);
    }
    
    float width() {
        return width;
    }
    
    void setWidth(float width) {
        this.width = width;
    }

    void paintWrapLines(ParagraphViewChildren children, ParagraphView pView,
            int startIndex, int endIndex,
            Graphics2D g, Shape alloc, Rectangle clipBounds)
    {
        DocumentView docView = pView.getDocumentView();
        if (docView == null) { // Not paint unless connected to hierarchy
            return;
        }
        JTextComponent textComponent = docView.getTextComponent();
        TextLayout lineContinuationTextLayout = docView.op.getLineContinuationCharTextLayout();
        Rectangle2D.Double allocBounds = ViewUtils.shape2Bounds(alloc);
        float wrapLineHeight = wrapLineHeight(children);
        double allocOrigX = allocBounds.x;
        allocBounds.y += startIndex * wrapLineHeight;
        allocBounds.height = wrapLineHeight; // Stays for whole rendering
        int lastWrapLineIndex = size() - 1;
        for (int i = startIndex; i < endIndex; i++) {
            WrapLine wrapLine = get(i);
            ViewPart startPart = wrapLine.startPart;
            if (startPart != null) {
                allocBounds.width = startPart.width;
                startPart.view.paint(g, allocBounds, clipBounds);
                allocBounds.x += startPart.width;
            }
            if (wrapLine.hasFullViews()) { // Render the views
                double visualOffset = children.startVisualOffset(wrapLine.firstViewIndex);
                assert (wrapLine.endViewIndex <= children.size()) : "Invalid for endViewIndex=" + // NOI18N
                        wrapLine.endViewIndex + ", wrapInfo:\n" + // NOI18N
                        this.toString(pView) + "\nParagraphView:\n" + pView; // NOI18N
                // Simulate start x for children rendering
                allocBounds.x -= visualOffset;
                children.paintChildren(pView, g, allocBounds, clipBounds,
                        wrapLine.firstViewIndex, wrapLine.endViewIndex);
                allocBounds.x += children.startVisualOffset(wrapLine.endViewIndex);
            }
            ViewPart endPart = wrapLine.endPart;
            if (endPart != null) {
                allocBounds.width = endPart.width;
                endPart.view.paint(g, allocBounds, clipBounds);
                allocBounds.x += endPart.width;
            }
            // Paint wrap mark
            if (i != lastWrapLineIndex) { // but not on last wrap line
                PaintState paintState = PaintState.save(g);
                try {
                    ViewUtils.applyForegroundColor(g, null, textComponent);
                    HighlightsViewUtils.paintTextLayout(g, allocBounds, lineContinuationTextLayout, docView);
                } finally {
                    paintState.restore();
                }
            }
            allocBounds.x = allocOrigX;
            allocBounds.y += wrapLineHeight;
        }
    }

    public void checkIntegrity(ParagraphView paragraphView) {
        if (ViewHierarchyImpl.CHECK_LOG.isLoggable(Level.FINE)) {
            String err = findIntegrityError(paragraphView);
            if (err != null) {
                String msg = "WrapInfo INTEGRITY ERROR! - " + err; // NOI18N
                ViewHierarchyImpl.CHECK_LOG.finer(msg + "\n"); // NOI18N
                ViewHierarchyImpl.CHECK_LOG.finer(toString(paragraphView)); // toString() impl should append newline
                // For finest level stop throw real ISE otherwise just log the stack
                if (ViewHierarchyImpl.CHECK_LOG.isLoggable(Level.FINEST)) {
                    throw new IllegalStateException(msg);
                } else {
                    ViewHierarchyImpl.CHECK_LOG.log(Level.INFO, msg, new Exception());
                }
            }
        }
    }

    public String findIntegrityError(ParagraphView paragraphView) {
        String err = null;
        int lastOffset = paragraphView.getStartOffset();
        for (int i = 0; i < size(); i++) {
            WrapLine wrapLine = get(i);
            ViewPart startPart = wrapLine.startPart;
            boolean nonEmptyLine = false;
            if (startPart != null) {
                nonEmptyLine = true;
                int startPartOffset = startPart.view.getStartOffset();
                if (startPartOffset != lastOffset) {
                    err = "startViewPart.getStartOffset()=" + startPartOffset + // NOI18N
                            " != lastOffset=" + lastOffset; // NOI18N
                }
                lastOffset = startPart.view.getEndOffset();
            }
            int startViewIndex = wrapLine.firstViewIndex;
            int endViewIndex = wrapLine.endViewIndex;
            if (startViewIndex != endViewIndex) {
                nonEmptyLine = true;
                boolean validIndices = true;
                if (startViewIndex < 0) {
                    validIndices = false;
                    if (err == null) {
                        err = "startViewIndex=" + startViewIndex + " < 0; endViewIndex=" + endViewIndex; // NOI18N
                    }
                }
                if (endViewIndex < startViewIndex) {
                    validIndices = false;
                    if (err == null) {
                        err = "endViewIndex=" + endViewIndex + " < startViewIndex=" + startViewIndex; // NOI18N
                    }
                }
                if (endViewIndex > paragraphView.getViewCount()) {
                    validIndices = false;
                    if (err == null) {
                        err = "endViewIndex=" + endViewIndex + " > getViewCount()=" + paragraphView.getViewCount(); // NOI18N
                    }
                }
                if (validIndices) {
                    EditorView childView = paragraphView.getEditorView(startViewIndex);
                    if (err == null && childView.getStartOffset() != lastOffset) {
                        err = "startChildView.getStartOffset()=" + childView.getStartOffset() // NOI18N
                                + " != lastOffset=" + lastOffset; // NOI18N
                    }
                    childView = paragraphView.getEditorView(endViewIndex - 1);
                    lastOffset = childView.getEndOffset();
                }
            }
            ViewPart endPart = wrapLine.endPart;
            if (endPart != null) {
                nonEmptyLine = true;
                int endPartOffset = endPart.view.getStartOffset();
                if (err == null && lastOffset != endPartOffset) {
                    err = "endViewPart.getStartOffset()=" + endPartOffset + // NOI18N
                            " != lastOffset=" + lastOffset; // NOI18N
                }
                lastOffset = endPart.view.getEndOffset();
            }
            if (!nonEmptyLine && err == null) {
                err = "Empty"; // NOI18N
            }
            if (err != null) {
                err = "WrapLine[" + i + "]: " + err; // NOI18N
                break;
            }
        }
        if (err == null && lastOffset != paragraphView.getEndOffset()) {
            err = "Not all offsets covered: lastOffset=" + lastOffset + " != parEndOffset=" + // NOI18N
                    paragraphView.getEndOffset();
        }
        return err;
    }

    String dumpWrapLine(ParagraphView pView, int wrapLineIndex) {
        return "Invalid wrapLine["  + wrapLineIndex + "]:\n" + toString((ParagraphView)pView); // NOI18N
    }

    public String appendInfo(StringBuilder sb, ParagraphView paragraphView, int indent) { // Expected to not append append newline at end
        int wrapLineCount = size();
        int digitCount = ArrayUtilities.digitCount(wrapLineCount);
        for (int i = 0; i < wrapLineCount; i++) {
            sb.append('\n'); // Expected to append newline first
            ArrayUtilities.appendSpaces(sb, indent + 4);
            sb.append("WL");
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            WrapLine wrapLine = get(i);
            sb.append("SV:"); // NOI18N
            ViewPart startPart = wrapLine.startPart;
            if (startPart != null) {
                sb.append("<").append(startPart.view.getStartOffset()).append(","); // NOI18N
                sb.append(startPart.view.getEndOffset()).append(">"); // NOI18N
            } else {
                sb.append("NULL"); // NOI18N
            }
            sb.append("; x=").append(wrapLine.startPartWidth()); // NOI18N
            int startViewIndex = wrapLine.firstViewIndex;
            int endViewIndex = wrapLine.endViewIndex;
            sb.append(" [").append(startViewIndex).append(","); // NOI18N
            sb.append(endViewIndex).append("] "); // NOI18N
            if (paragraphView != null && startViewIndex != endViewIndex) {
                if (startViewIndex > endViewIndex) {
                    sb.append("ERROR!!! startViewIndex=").append(startViewIndex); // NOI18N
                    sb.append(" > endViewIndex=").append(endViewIndex); // NOI18N
                } else {
                    int childCount = paragraphView.getViewCount();
                    if (startViewIndex == childCount) {
                        sb.append("<").append(paragraphView.getEndOffset()).append(">"); // NOI18N
                    } else {
                        if (startViewIndex <= childCount) {
                            EditorView startChild = paragraphView.getEditorView(startViewIndex);
                            sb.append("<").append(startChild.getStartOffset()); // NOI18N
                        } else {
                            sb.append("<invalid-index=" + startViewIndex); // NOI18N
                        }
                        sb.append(",");
                        if (endViewIndex <= childCount) {
                            EditorView lastChild = paragraphView.getEditorView(endViewIndex - 1);
                            sb.append(lastChild.getEndOffset()); // NOI18N
                        } else {
                            sb.append("invalid-index=").append(endViewIndex); // NOI18N
                        }
                        sb.append("> ");
                    }
                }
            }
            sb.append("EV:"); // NOI18N
            ViewPart endViewPart = wrapLine.endPart;
            if (endViewPart != null) {
                sb.append("<").append(endViewPart.view.getStartOffset()).append(","); // NOI18N
                sb.append(endViewPart.view.getEndOffset()).append(">"); // NOI18N
            } else {
                sb.append("NULL"); // NOI18N
            }
        }
        return sb.toString();
    }

    public String toString(ParagraphView paragraphView) {
        return appendInfo(new StringBuilder(200), paragraphView, 0);
    }

    @Override
    public String toString() {
        return toString(null);
    }

}
