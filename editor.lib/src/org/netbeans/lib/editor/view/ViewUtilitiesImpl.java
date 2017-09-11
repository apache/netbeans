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

package org.netbeans.lib.editor.view;

import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.View;
import org.netbeans.editor.view.spi.FlyView;

/**
 * Various utility methods related to views.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class ViewUtilitiesImpl {

    private ViewUtilitiesImpl() {
    }

    /**
     * Find child index representing the given offset
     * or -1 if the offset is not represented by any child view
     * i.e. it is outside of the bounds of this view
     * or when the given view does not have any children.
     *
     * @param view view having children to be inspected.
     * @param offset the position >= 0.
     * @return index of the child view representing the given position
     *  or -1 if <code>
     *        offset < view.getStartOffset()
     *     || offset >= view.getEndOffset()</code>
     *     || getViewCount() == 0
     *  </code>.
     */
    public static int findViewIndexBounded(View view, int offset) {
        int viewStartOffset = view.getStartOffset();
        if (offset == viewStartOffset)
            return 0;
	if ((offset >= viewStartOffset) && (offset < view.getEndOffset())) {
	    return findViewIndex(view, offset);
	}

	return -1; // outside of view bounds
    }

    /**
     * Find the child view index that best represents the given offset.
     * <br>
     * Algorithm uses binary search.
     * <br>
     * The semantics is similar
     * to {@link javax.swing.text.Element.getElementIndex(int)} semantics.
     *
     * @param view view having children to be inspected.
     * @param offset the position &gt;= 0
     * @return index of the child view representing the given position.
     *   <br>
     *   0 if <code>offset < getView(0).getStartOffset()</code>.
     *   <br>
     *   <code>getViewCount() - 1</code>
     *     if <code>offset >= getView(viewCount - 1).getEndOffset() </code>.
     *   <br>
     *   -1 if <code>getViewCount() == 0</code>.
     */
    public static int findViewIndex(View view, int offset) {
        return findViewIndex(view, offset, null);
    }

    /**
     * Do the actual finding algorithm and use updater
     * in case there is a view starting exactly
     * at the requested offset.
     */
    private static int findViewIndex(View view, int offset, OffsetMatchUpdater updater) {
        FlyView.Parent flyParent = (view instanceof FlyView.Parent)
            ? (FlyView.Parent)view
            : null;

        int low = 0;
        int high = view.getViewCount() - 1;
        
        if (high == -1) { // no children => return -1
            return -1;
        }
        
        while (low <= high) {
            int mid = (low + high) / 2;
            int midStartOffset = (flyParent != null)
                ? flyParent.getStartOffset(mid)
                : view.getView(mid).getStartOffset();
            
            if (midStartOffset < offset) {
                low = mid + 1;
            } else if (midStartOffset > offset) {
                high = mid - 1;
            } else { // element starts at offset
                if (updater != null) {
                    mid = updater.updateIndex(mid, offset, view, flyParent);
                }
                return mid;
            }
        }

        if (high < 0) {
            high = 0;
        }
        return high;
    }

    /**
     * The semantics is the same like <code>findViewIndex()</code>
     * but if there are any empty views at the offset (e.g. because
     * of removal) then they are skipped and the first empty view
     * is returned.
     *
     * @param view view having children to be inspected.
     * @param offset the position &gt;= 0
     * @param lowerAdjacent if set to true
     *  and the returned child view would start right at offset
     *  then return the non-empty left adjacent view (which ends
     *  at the <code>offset</code>). This may be useful e.g. in case
     *  of removal when the view was affected by removal (view's end was removed)
     *  but now after removal it seems that it naturally ends at <code>offset</code>.
     * @return index of the child view representing the given offset.
     */
    public static int findLowerViewIndex(View view, int offset, boolean lowerAdjacent) {
        OffsetMatchUpdater updater = lowerAdjacent
            ? LowerOffsetMatchUpdater.adjacent
            : LowerOffsetMatchUpdater.normal;

        return findViewIndex(view, offset, updater);
    }

    /**
     * The semantics is the same like <code>findViewIndex()</code>
     * but if there are any empty views at the offset (e.g. because
     * of removal) then they are skipped and the first non-empty view
     * starting at the offset is returned.
     *
     * @param view view having children to be inspected.
     * @param offset the position &gt;= 0
     * @param excludeAtOffset if set to true
     *  and the returned non-empty child view would start right at offset
     *  then return the left adjacent view (which ends
     *  at the <code>offset</code>). This may be useful e.g. in case
     *  of insertion to not replace a view that starts right at the offset
     *  and was not in fact affected by insertion.
     * @return index of the child view representing the given offset.
     */
    public static int findUpperViewIndex(View view, int offset, boolean excludeAtOffset) {
        OffsetMatchUpdater updater = excludeAtOffset
            ? UpperOffsetMatchUpdater.exclude
            : UpperOffsetMatchUpdater.normal;

        return findViewIndex(view, offset, updater);
    }

    /**
     * Return existing rectangle or create new one.
     * @param r rectangle which is checked to correspond to next parameters.
     * @param x x coordinate of returned rectangle.
     * @param y y coordinate of returned rectangle.
     * @param width width of returned rectangle.
     * @param height height of returned rectangle.
     * @return rectangle <code>r</code> passed to this method
     *  in case it corresponds to all the other parameters.
     *  Newly created rectangle instance otherwise.
     */
    public static Rectangle maybeNew(Rectangle r, int x, int y, int width, int height) {
        if (r == null || r.x != x || r.y != y || r.width != width || r.height != height) {
            return new Rectangle(x, y, width, height);
        } else {
            return r;
        }
    }
    
    /**
     * Return existing rectangle or create new one.
     * @param origRect rectangle which is checked to correspond to tested rect.
     * @param testRect rectangle being tested for equality to origRect.
     * @return rectangle <code>origRect</code> passed to this method
     *  in case it corresponds to <code>testRect</code>.
     *  Newly created rectangle instance (being equal to testRect) otherwise.
     */
    public static Rectangle maybeNew(Rectangle origRect, Rectangle testRect) {
        if (origRect == null || !origRect.equals(testRect)) {
            origRect = new Rectangle(testRect);
        }
        return origRect;
    }

    public static String axisToString(int axis) {
        switch (axis) {
            case View.X_AXIS:
                return "x"; // NOI18N
                
            case View.Y_AXIS:
                return "y"; // NOI18N
                
            default:
                return "<invalid-axis-value=" + axis + ">"; // NOI18N
        }
    }

    /**
     * Provides a way to determine the next visually represented model
     * location that one might place a caret.  Some views may not be visible,
     * they might not be in the same order found in the model, or they just
     * might not allow access to some of the locations in the model.
     * <p>
     * This implementation assumes the views are layed out in a logical
     * manner. That is, that the view at index x + 1 is visually after
     * the View at index x, and that the View at index x - 1 is visually
     * before the View at x. There is support for reversing this behavior
     * only if the passed in <code>View</code> is an instance of
     * <code>GapBoxView</code>.
     * The <code>GapBoxView</code>
     * must then override the <code>flipEastAndWestAtEnds</code> method.
     *
     * @param v View to query
     * @param pos the position to convert >= 0
     * @param a the allocated region to render into
     * @param direction the direction from the current position that can
     *  be thought of as the arrow keys typically found on a keyboard;
     *  this may be one of the following:
     *  <ul>
     *  <li><code>SwingConstants.WEST</code>
     *  <li><code>SwingConstants.EAST</code>
     *  <li><code>SwingConstants.NORTH</code>
     *  <li><code>SwingConstants.SOUTH</code>
     *  </ul>
     * @param biasRet an array contain the bias that was checked
     * @return the location within the model that best represents the next
     *  location visual position
     * @exception BadLocationException
     * @exception IllegalArgumentException if <code>direction</code> is invalid
     */
    static int getNextVisualPositionFrom(View v, int pos, Position.Bias b,
    Shape alloc, int direction, Position.Bias[] biasRet)
    throws BadLocationException {

        if (v.getViewCount() == 0) {
            // Nothing to do.
            return pos;
        }

        boolean top = (direction == SwingConstants.NORTH ||
        direction == SwingConstants.WEST);
        int retValue;
        if (pos == -1) {
            // Start from the first View.
            int childIndex = (top) ? v.getViewCount() - 1 : 0;
            View child = v.getView(childIndex);
            Shape childBounds = v.getChildAllocation(childIndex, alloc);
            retValue = child.getNextVisualPositionFrom(pos, b, childBounds,
            direction, biasRet);
            if (retValue == -1 && !top && v.getViewCount() > 1) {
                // Special case that should ONLY happen if first view
                // isn't valid (can happen when end position is put at
                // beginning of line.
                child = v.getView(1);
                childBounds = v.getChildAllocation(1, alloc);
                retValue = child.getNextVisualPositionFrom(-1, biasRet[0],
                childBounds,
                direction, biasRet);
            }

        } else {
            int increment = (top) ? -1 : 1;
            int childIndex;
            if (b == Position.Bias.Backward && pos > 0) {
                childIndex = v.getViewIndex(pos - 1, Position.Bias.Forward);
            } else {
                childIndex = v.getViewIndex(pos, Position.Bias.Forward);
            }

            View child = v.getView(childIndex);
            Shape childBounds = v.getChildAllocation(childIndex, alloc);
            retValue = child.getNextVisualPositionFrom(pos, b, childBounds,
            direction, biasRet);
/*            if ((direction == SwingConstants.EAST ||
                direction == SwingConstants.WEST) &&
                (v instanceof GapBoxView) &&
                ((GapBoxView)v).flipEastAndWestAtEnds(pos, b)
            ) {
                increment *= -1;
            }
 */

            childIndex += increment;
            if (retValue == -1 && childIndex >= 0 &&
            childIndex < v.getViewCount()) {
                child = v.getView(childIndex);
                childBounds = v.getChildAllocation(childIndex, alloc);
                retValue = child.getNextVisualPositionFrom(
                -1, b, childBounds, direction, biasRet);
                // If there is a bias change, it is a fake position
                // and we should skip it. This is usually the result
                // of two elements side be side flowing the same way.
                if (retValue == pos && biasRet[0] != b) {
                    return getNextVisualPositionFrom(v, pos, biasRet[0],
                    alloc, direction,
                    biasRet);
                }
            }
            else if (retValue != -1 && biasRet[0] != b &&
                ((increment == 1 && child.getEndOffset() == retValue) ||
                (increment == -1 &&
                child.getStartOffset() == retValue)) &&
                childIndex >= 0 && childIndex < v.getViewCount()
            ) {
                // Reached the end of a view, make sure the next view
                // is a different direction.
                child = v.getView(childIndex);
                childBounds = v.getChildAllocation(childIndex, alloc);
                Position.Bias originalBias = biasRet[0];
                int nextPos = child.getNextVisualPositionFrom(
                    -1, b, childBounds, direction, biasRet);

                if (biasRet[0] == b) {
                    retValue = nextPos;
                } else {
                    biasRet[0] = originalBias;
                }
            }
        }
        return retValue;
    }

    public static void checkViewHierarchy(View v) {
        checkChildrenParent(v);
    }
    
    private static void checkChildrenParent(View v) {
        int cnt = v.getViewCount();
        for (int i = 0; i < cnt; i++) {
            View child = v.getView(i);
            View childParent = child.getParent();
            if (childParent != v) {
                throw new IllegalStateException("child=" + child // NOI18N
                    + " has parent=" + childParent // NOI18N
                    + " instead of " + v // NOI18N
                );
            }
            checkChildrenParent(child);
        }
    }

    /**
     * Updates index of the view returned by <code>findViewIndex()</code>
     * in case there is a view starting exactly at the given offset.
     */
    interface OffsetMatchUpdater {
        
        int updateIndex(int viewIndex, int offset, View view, FlyView.Parent flyParent);

    }
    
    static class LowerOffsetMatchUpdater implements OffsetMatchUpdater {
        
        static final LowerOffsetMatchUpdater normal = new LowerOffsetMatchUpdater(false);
        static final LowerOffsetMatchUpdater adjacent = new LowerOffsetMatchUpdater(true);
        
        private final boolean lowerAdjacent;
        
        LowerOffsetMatchUpdater(boolean lowerAdjacent) {
            this.lowerAdjacent = lowerAdjacent;
        }
       
        public int updateIndex(int viewIndex, int offset, View view, FlyView.Parent flyParent) {
            while (--viewIndex >= 0) {
                int startOffset = (flyParent != null)
                    ? flyParent.getStartOffset(viewIndex)
                    : view.getView(viewIndex).getStartOffset();
                if (startOffset != offset) { // view starts below offset
                    if (lowerAdjacent) {
                        viewIndex--; // return the lower view that ends at offset
                    }

                    break;
                }
            }
            
            return viewIndex + 1;
        }

    }
        
    static class UpperOffsetMatchUpdater implements OffsetMatchUpdater {
        
        static final UpperOffsetMatchUpdater normal = new UpperOffsetMatchUpdater(false);
        static final UpperOffsetMatchUpdater exclude = new UpperOffsetMatchUpdater(true);
        
        private final boolean excludeAtOffset;
        
        UpperOffsetMatchUpdater(boolean excludeAtOffset) {
            this.excludeAtOffset = excludeAtOffset;
        }
       
        public int updateIndex(int viewIndex, int offset, View view, FlyView.Parent flyParent) {
            int lastViewIndex = view.getViewCount() - 1;

            while (true) {
                int endOffset = (flyParent != null)
                    ? flyParent.getEndOffset(viewIndex)
                    : view.getView(viewIndex).getEndOffset();
                if (endOffset != offset) { // view ends after offset
                    if (excludeAtOffset) {
                        viewIndex--; // return the lower view that ends at offset
                    }
                    break;
                }
             
                if (viewIndex == lastViewIndex) { // over last view
                    break;
                }
                viewIndex++;
            }
            
            return viewIndex;
        }

    }
        
}
