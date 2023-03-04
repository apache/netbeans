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

package org.netbeans.modules.editor.lib2.view;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.View;

/**
 * Base class for views in editor view hierarchy.
 * <br>
 * In general there are three types of views:<ul>
 * <li>Document view</li>
 * <li>Paragraph views</li>
 * <li>Children of paragraph views which include highlights view, newline view and others.</li>
 * </ul>
 * <br>
 * Paragraph views have their start offset based over a swing text position. Their end offset
 * is based on last child's end offset.
 * <br>
 * Children of paragraph views have their start offset based over a relative distance
 * to their parent's paragraph view's start offset. Therefore their start offset does not mutate
 * upon modification unless the whole paragraph's start offset mutates.
 * Their {@link #getLength()} method should remain stable upon document mutations
 * (this way the view builder can iterate over them when calculating last affected view
 * once the new views become created).
 *
 * @author Miloslav Metelka
 */

public abstract class EditorView extends View {

    // -J-Dorg.netbeans.modules.editor.lib2.view.EditorView.level=FINE
    private static final Logger LOG = Logger.getLogger(EditorView.class.getName());

    /**
     * Raw visual offset of view's end along the parent's major axis (axis along which the children are laid out).
     */
    private double rawEndVisualOffset; // 16=super + 8 = 24 bytes

    public EditorView(Element element) {
        super(element);
    }

    /**
     * Get raw end offset of the view which may transform to real end offset
     * when post-processed by parent view.
     * <br>
     * <b>Note:</b> Typical clients should NOT call this method (they should call
     * {@link #getEndOffset()} method instead).
     *
     * @return raw end offset of the view or -1 if the view does not support
     * storage of the raw offsets (e.g. a ParagraphView).
     */
    public abstract int getRawEndOffset();

    /**
     * Set raw end offset of the view.
     *
     * @param rawEndOffset raw start offset of the view. This method will not be called
     *  if {@link #getRawOffset()} returns -1.
     */
    public abstract void setRawEndOffset(int rawEndOffset);

    /**
     * Textual length of the view.
     *
     * @return &gt;=0 length of the view.
     */
    public int getLength() {
        return getEndOffset() - getStartOffset();
    }

    /**
     * @return raw end visual offset along parent's major axis. It must be post-processed
     *   by parent (if it uses gap-based storage) to become a real end visual offset.
     */
    public final double getRawEndVisualOffset() {
        return rawEndVisualOffset;
    }
    
    /**
     * Parent view can set the view's raw end visual offset along the parent view's
     * major axis (axis along which the children are laid out) by using this method.
     *
     * @param rawEndVisualOffset raw offset value along the major axis of parent view.
     *  It is not particularly useful without postprocessing by the parent.
     */
    public final void setRawEndVisualOffset(double rawEndVisualOffset) {
        this.rawEndVisualOffset = rawEndVisualOffset;
    }

    /**
     * Paint into the given bounds.
     *
     * @param g non-null graphics to render into.
     * @param alloc non-null bounds allocated to this view. It can be mutated if necessary.
     */
    public abstract void paint(Graphics2D g, Shape alloc, Rectangle clipBounds);

    /**
     * {@inheritDoc}
     */
    @Override
    public final void paint(Graphics g, Shape alloc) {
        if (alloc != null) {
            if (g instanceof Graphics2D) {
                paint((Graphics2D)g, alloc, g.getClipBounds());
            }
        }
    }

    /**
     * Provides a way to determine the next visually represented model
     * location at which one might place a caret.
     * Some views may not be visible,
     * they might not be in the same order found in the model, or they just
     * might not allow access to some of the locations in the model.
     *
     * @param offset the position to convert >= 0. -1 can be passed to request
     *  "first available position" at the view's start in the particular direction.
     *  For example -1 with SwingConstants.WEST means getStartOffset().
     * @param bias bias for the offset.
     * @param alloc non-null bounds allocated to this view.
     * @param direction the direction from the current position that can
     *  be thought of as the arrow keys typically found on a keyboard.
     *  This will be one of the following values:
     * <ul>
     * <li>SwingConstants.WEST
     * <li>SwingConstants.EAST
     * <li>SwingConstants.NORTH
     * <li>SwingConstants.SOUTH
     * </ul>
     * @return the location within the model that best represents the next
     *  location visual position
     * @exception IllegalArgumentException if <code>direction</code>
     *		doesn't have one of the legal values above
     */
    public int getNextVisualPositionFromChecked(int offset, Position.Bias bias, Shape alloc,
            int direction, Position.Bias[] biasRet)
    {
        try {
            return super.getNextVisualPositionFrom(offset, bias, alloc, direction, biasRet);
        } catch (BadLocationException e) {
            return getStartOffset(); // Should not happen
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getNextVisualPositionFrom(int offset, Position.Bias bias, Shape alloc,
            int direction, Position.Bias[] biasRet) throws BadLocationException
    {
        if (offset != -1) { // -1 is allowed as a special case; although javadoc in View prohibits it!
            checkBounds(offset);
        }
        checkBias(bias);
        if (alloc != null) {
            offset = getNextVisualPositionFromChecked(offset, bias, alloc, direction, biasRet);
        }
        return offset;
    }

    /**
     * Provides a mapping, for a given character,
     * from the document model coordinate space
     * to the view coordinate space.
     *
     * @param offset the position of the desired character (>=0)
     * @param alloc the area of the view, which encompasses the requested character
     * @param bias the bias toward the previous character or the
     *  next character represented by the offset, in case the
     *  position is a boundary of two views; <code>b</code> will have one
     *  of these values:
     * <ul>
     * <li> <code>Position.Bias.Forward</code>
     * <li> <code>Position.Bias.Backward</code>
     * </ul>
     * @return the bounding box, in view coordinate space,
     *		of the character at the specified position
     * @exception IllegalArgumentException if <code>b</code> is not one of the
     *		legal <code>Position.Bias</code> values listed above
     * @see View#viewToModel
     */
    public abstract Shape modelToViewChecked(int offset, Shape alloc, Position.Bias bias);

    /**
     * {@inheritDoc}
     */
    @Override
    public final Shape modelToView(int offset, Shape alloc, Position.Bias bias) throws BadLocationException {
        checkBounds(offset);
        checkBias(bias);
        if (alloc != null) {
            return modelToViewChecked(offset, alloc, bias);
        } else {
            return null;
        }
    }

    /**
     * Provides a mapping, for a given region,
     * from the document model coordinate space
     * to the view coordinate space. The specified region is
     * created as a union of the first and last character positions.
     *
     * @param offset0 the position of the first character (>=0)
     * @param bias0 the bias of the first character position,
     *  toward the previous character or the
     *  next character represented by the offset, in case the
     *  position is a boundary of two views; <code>b0</code> will have one
     *  of these values:
     * <ul>
     * <li> <code>Position.Bias.Forward</code>
     * <li> <code>Position.Bias.Backward</code>
     * </ul>
     * @param offset1 the position of the last character (>=0)
     * @param bias1 the bias for the second character position, defined
     *		one of the legal values shown above
     * @param alloc bounds allocated to this view. It should be modified
     *  to contain the resulting bounding box which is a union of the region specified
     *		by the first and last character positions.
     * @see View#viewToModel
     */
    public Shape modelToViewChecked(int offset0, Position.Bias bias0, int offset1, Position.Bias bias1, Shape alloc) {
        Shape start = modelToViewChecked(offset0, alloc, bias0);
        Shape end = modelToViewChecked(offset1, alloc, bias1);
        Rectangle2D.Double mutableBounds = null;
        if (start != null) {
            mutableBounds = ViewUtils.shape2Bounds(start);
            if (end != null) {
                Rectangle2D endRect = ViewUtils.shapeAsRect(end);
                if (mutableBounds.getY() != endRect.getY()) {
                    Rectangle2D allocRect = ViewUtils.shapeAsRect(alloc);
                    // If it spans lines, force it to be the width of the view.
                    mutableBounds.x = allocRect.getX();
                    mutableBounds.width = allocRect.getWidth();
                }
                mutableBounds.add(endRect);
            }
        }
        return mutableBounds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Shape modelToView(int offset0, Position.Bias bias0, int offset1, Position.Bias bias1,
            Shape alloc) throws BadLocationException
    {
        checkBounds(offset0);
        checkBias(bias0);
        checkBounds(offset1);
        checkBias(bias1);
        if (alloc != null) {
            return modelToViewChecked(offset0, bias0, offset1, bias1, alloc);
        } else {
            return null;
        }
    }

    /**
     * Provides a mapping from the view coordinate space to the logical
     * coordinate space of the model. The <code>biasReturn</code>
     * argument will be filled in to indicate that the point given is
     * closer to the next character in the model or the previous
     * character in the model.
     *
     * @param x the X coordinate >= 0
     * @param y the Y coordinate >= 0
     * @param alloc bounds allocated to this view.
     * @return the location within the model that best represents the
     *  given point in the view >= 0.  The <code>biasReturn</code>
     *  argument will be
     * filled in to indicate that the point given is closer to the next
     * character in the model or the previous character in the model.
     */
    public abstract int viewToModelChecked(double x, double y, Shape alloc, Position.Bias[] biasReturn);

    /**
     * {@inheritDoc}
     */
    @Override
    public final int viewToModel(float x, float y, Shape alloc, Position.Bias[] biasReturn) {
        if (alloc != null) {
            return viewToModelChecked((double)x, (double)y, alloc, biasReturn);
        } else {
            return getStartOffset();
        }
    }

    public String getToolTipTextChecked(double x, double y, Shape allocation) {
        return null;
    }

    @Override
    public String getToolTipText(float x, float y, Shape allocation) {
            return getToolTipTextChecked(x, y, allocation); // Use coords in doubles
    }

    public JComponent getToolTip(double x, double y, Shape allocation) {
        return null;
    }

    /**
     * Returns the child view index representing the given position in
     * the view. This iterates over all the children returning the
     * first with a bounds that contains <code>x</code>, <code>y</code>.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param alloc current allocation of the View.
     * @return  index of the view representing the given location, or
     *   -1 if no view represents that position
     */
    public int getViewIndexChecked(double x, double y, Shape alloc) {
        return -1; // No subviews by default
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getViewIndex(float x, float y, Shape alloc) {
        if (alloc != null) {
            return getViewIndexChecked((double)x, (double)y, alloc);
        } else {
            return -1;
        }
    }

    @Override
    public Document getDocument() {
        View parent = getParent();
        // By default do not assume non-null element for view construction => return null
        // Possibly use getElement().getDocument() in descendants
        return (parent != null) ? parent.getDocument() : null;
    }

    protected String getDumpName() {
        return "EV"; // EditorView abbrev; NOI18N
    }

    public final String getDumpId() {
        int viewCount = getViewCount();
        return getDumpName() + "@" + ViewUtils.toStringId(this) + // NOI18N
                ((viewCount > 0) ? "#" + getViewCount() : ""); // NOI18N
    }

    public void checkIntegrityIfLoggable() {
        if (ViewHierarchyImpl.CHECK_LOG.isLoggable(Level.FINE)) {
            checkIntegrity(); // NOI18N
        }
    }

    public void checkIntegrity() {
        String err = findTreeIntegrityError(); // Check integrity of the document view
        if (err != null) {
            StringBuilder sb = new StringBuilder(200);
            sb.append("View hierarchy INTEGRITY ERROR! - ").append(err);
            sb.append("\nErrorneous view hierarchy:\n");
            appendViewInfo(sb, 0, "", -2); // -2 means detailed info
            // For finest level stop throw real ISE otherwise just log the stack
            if (ViewHierarchyImpl.CHECK_LOG.isLoggable(Level.FINEST)) {
                throw new IllegalStateException(sb.toString());
            } else {
                ViewHierarchyImpl.CHECK_LOG.log(Level.INFO, sb.toString(), new Exception());
            }
        }
    }

    public String findIntegrityError() {
        String err = null;
        if (getStartOffset() + getLength() != getEndOffset()) {
            err = "getStartOffset()=" + getStartOffset() + " + getLength()=" + getLength() + // NOI18N
                    " != getEndOffset()=" + getEndOffset(); // NOI18N
        }
        return err;
    }

    public String findTreeIntegrityError() {
        String err = findIntegrityError();
        if (err == null) { // Check children
            int viewCount = getViewCount();
            int startOffset = getStartOffset();
            int endOffset = getEndOffset();
            int lastOffset = startOffset;
            for (int i = 0; i < viewCount; i++) {
                EditorView child = (EditorView) getView(i);
                if (child.getParent() != this) {
                    err = "child.getParent() != this";
                }
                if (err == null) {
                    // Check proper children parenting since e.g. paragraph view may derive
                    // its end offset from last child
                    int childViewCount = child.getViewCount();
                    for (int j = 0; j < childViewCount; j++) {
                        EditorView childChild = (EditorView) child.getView(j);
                        EditorView childChildParent = (EditorView) childChild.getParent();
                        if (childChildParent != child) {
                            String ccpStr = (childChildParent != null) ? childChildParent.getDumpId() : "<NULL>";
                            err = "childChild[" + j + "].getParent()=" + ccpStr + // NOI18N
                                    " != child=" + child.getDumpId(); // NOI18N
                            break;
                        }
                    }
                }
                int childStartOffset = child.getStartOffset();
                int childEndOffset = child.getEndOffset();
                boolean noChildInfo = false;
                if (err == null) {
                    if (childStartOffset != lastOffset) {
                        err = "childStartOffset=" + childStartOffset + ", lastOffset=" + lastOffset; // NOI18N
                    } else if (childStartOffset < 0) {
                        err = "childStartOffset=" + childStartOffset + " < 0"; // NOI18N
                    } else if (childStartOffset > childEndOffset) {
                        err = "childStartOffset=" + childStartOffset + " > childEndOffset=" + childEndOffset; // NOI18N
                    } else if (childEndOffset > endOffset) {
                        err = "childEndOffset=" + childEndOffset + " > parentEndOffset=" + endOffset; // NOI18N
                    } else {
                        err = child.findTreeIntegrityError();
                        noChildInfo = true;
                    }
                }

                if (err != null) {
                    return getDumpId() + "[" + i + "]=" + (noChildInfo ? "" : child.getDumpId() + ": ") + err + '\n';
                }
                lastOffset = childEndOffset;
            }
            if (viewCount > 0 && lastOffset != endOffset) {
                err = "lastChild.getEndOffset()=" + lastOffset + " != endOffset=" + endOffset; // NOI18N
            }
        }
        return err;
    }

    protected StringBuilder appendViewInfo(StringBuilder sb, int indent, String xyInfo, int importantChildIndex) {
        sb.append(getDumpId()).append(" ");
        int startOffset = getStartOffset();
        int endOffset = getEndOffset();
        sb.append('<').append(startOffset);
        sb.append(',');
        sb.append(endOffset).append('>');
        sb.append(xyInfo); // Should include extra space at end
//        sb.append(";REVO=").append(getRawEndVisualOffset());
        // Do not getPreferredSpan() since it may be expensive (for HighlightsView calls getTextLayout())
        return sb;
    }

    private void checkBounds(int offset) throws BadLocationException {
        Document doc = getDocument();
        if (offset < 0 || offset > doc.getLength() + 1) {
            throw new BadLocationException("Invalid offset=" + offset + ", docLen=" + doc.getLength(), offset); // NOI18N
        }
    }

    private void checkBias(Position.Bias bias) {
        if (bias == null) { // Position.Bias is final class so only null value is invalid
            throw new IllegalArgumentException("Null bias prohibited.");
        }
    }

    public interface Parent {

        /**
         * Get start offset of a child view based on view's raw offset.
         * @param rawChildEndOffset relative child's raw offset.
         * @return real offset.
         */
        int getViewEndOffset(int rawChildEndOffset);
        
        /**
         * Get view's rendering context
         * @return 
         */
        ViewRenderContext getViewRenderContext();

    }

}
