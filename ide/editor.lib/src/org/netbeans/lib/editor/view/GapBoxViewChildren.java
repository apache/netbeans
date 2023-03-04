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

package org.netbeans.lib.editor.view;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.text.AbstractDocument;
import javax.swing.text.View;
import org.netbeans.editor.view.spi.EstimatedSpanView;
import org.netbeans.editor.view.spi.LockView;
import org.netbeans.editor.view.spi.ViewInsets;
import org.netbeans.editor.view.spi.ViewLayoutQueue;
import org.netbeans.editor.view.spi.ViewLayoutState;

//import org.netbeans.spi.lexer.util.GapObjectArray;

/**
 * Maintainer of the children of the {@link GapBoxView}.
 * <br>
 * It also acts as a runnable task for flushing requirement
 * changes of the view it works for.
 *
 * Besides the current implementation there could be
 * an implementation for small number of children (e.g. up to 20)
 * which would not have to use index gap and which would use indexes
 * &lt;= 127 that could be merged into shorts by pairs.
 * This approach could save about 40 bytes.
 * <br>
 * However the GapBoxView is able to unload its children
 * dynamically which can save much more memory
 * than the described simplified implementation.
 * 
 * @author Miloslav Metelka
 * @version 1.00
 */

class GapBoxViewChildren extends GapObjectArray implements GapObjectArray.RemoveUpdater {

    GapBoxViewChildren(GapBoxView view) {
        this.view = view;

        indexGapLength = INITIAL_INDEX_GAP_LENGTH;
        majorAxisOffsetGapLength = INITIAL_MAJOR_AXIS_OFFSET_GAP_LENGTH;
    }

    /**
     * Initial size of the index gap.
     * <br>
     * The value is chosen big enough so that
     * no index ever reaches a value
     * equal to half of it (about 500 million).
     * That allows to use the gap length
     * for testing of whether the particular
     * raw index is below or above the gap.
     */
    private static final int INITIAL_INDEX_GAP_LENGTH
        = Integer.MAX_VALUE >> 1;

    /**
     * Initial size of the offset gap.
     *
     * <p>
     * Double is chosen for gap length instead of float
     * because for y as major axis
     * the truncation could occur when computing the offset
     * for large files.
     * <br>
     * The resolution of mantissa of floats is 23 bits
     * so assuming the line height is e.g. 17 pixels
     * and we have more than 250.000 lines in the docuemnt
     * (which is a lot to write but not so much for e.g.
     * generated xml files) the last bit would be lost
     * resulting in every odd line being shifted one
     * pixel above incorrectly.
     * <br>
     * The chosen double is big enough to cover
     * arbitrarily long documents but retain fractions
     * when fractional metrics would be used.
     * <br>
     * When zero is used as initial gap length
     * then the gap length will be under zero
     * but there are no problems with it
     * as the index of gap start is held.
     */
    private static final double INITIAL_MAJOR_AXIS_OFFSET_GAP_LENGTH
        = 0d;

    /**
     * A <code>GapBoxView</code> instance for which
     * this class operates.
     */
    protected final GapBoxView view; // 20 bytes GapObjectArray + 4 = 24 bytes

    /**
     * Length of the offset gap in the children.
     *
     * <p>
     * Double is chosen instead of float because for y as major axis
     * the truncation could occur when computing the offset
     * for large files.
     * <br>
     * The resolution of mantissa of floats is 23 bits
     * so assuming the line height is e.g. 17 pixels
     * and we have more than 250.000 lines in the docuemnt
     * (which is a lot to write but not so much for e.g.
     * generated xml files) the last bit would be lost
     * resulting in every odd line being shifted one
     * pixel above incorrectly.
     */
    private double majorAxisOffsetGapLength; // 24 + 8 = 32 bytes

    /**
     * Index of the first child that has it <code>majorAxisRawOffset</code>
     * increased by <code>majorAxisOffsetGapLength</code>.
     */
    private int majorAxisOffsetGapIndex; // 32 + 4 = 36 bytes

    /**
     * Length of the index gap in children.
     * <br>
     * The initial gap length is chosen big enough
     * so that even a half of the gap length value
     * should never be reached. That allows
     * to use the gap length for testing
     * whether the particular raw index
     * is below or above the gap.
     */
    private int indexGapLength; // 36 + 4 = 40 bytes

    /**
     * Index of the first child which needs the layout update.
     * <br>
     * The value is only valid if <code>updateLayoutChildCount &gt= 0</code>.
     * <br>
     * Although the layout-related information of the child is invalid
     * its majorAxisRawOffset should hold the right value.
     * <br>
     * The index should always point to a non-flyweight child.
     */
    private int firstUpdateLayoutChildIndex; // 40 + 4 = 44 bytes

    /**
     * Number of children that need layout update.
     * <br>
     * The range starts at <code>firstUpdateLayoutChildIndex</code>
     * and ends at <code>firstUpdateLayoutChildIndex + updateLayoutChildCount</code>
     */
    private int updateLayoutChildCount; // 44 + 4 = 48 bytes
    
    /**
     * Preferred span of the whole view along the minor axis.
     */
    private float minorAxisPreferredSpan; // 48 + 4 = 52 bytes

    /**
     * Index of the child that has a maximum preferred span along
     * the minor axis among all the children.
     * <br>
     * -1 means there are no children available yet.
     */
    private int maxMinorAxisPreferredSpanChildIndex = -1; // 52 + 4 = 56 bytes

    /**
     * Index of the first child that needs to be repainted
     * during the next flushing of the requirement changes.
     */
    private int firstRepaintChildIndex = -1; // 56 + 4 = 60 bytes

    // Mem: 60 (64 aligned) + 16+ GapObjectArray.array

    public int getChildCount() {
        return getItemCount();
    }

    public ViewLayoutState getChild(int index) {
        return (ViewLayoutState)getItem(index);
    }

    public int getChildIndex(ViewLayoutState child) {
        int childIndex = getChildIndexNoCheck(child);
        if (childIndex >= getChildCount() || getChild(childIndex) != child) {
            childIndex = -1;
        }
        return childIndex;
    }

    public int getChildIndexNoCheck(ViewLayoutState child) {
        return getTranslatedChildIndex(child.getViewRawIndex());
    }

    private int getTranslatedChildIndex(int rawIndex) {
        if (rawIndex >= indexGapLength) {
            rawIndex -= indexGapLength;
        }
        return rawIndex;
    }

    public void replace(int index, int length, View[] views) {
        // The method gets called only if there is something to do
        // i.e. (length > 0) || (views.length > 0)
        
        checkConsistency();

        int insertLength = (views != null) ? views.length : 0;

//        System.out.println("getChildCount()=" + getChildCount() + ", index=" + index + ", length=" + length + ", insertLength=" + insertLength);

        // Update indexes that would lay in the removed area
        // and recompute indexes according to (insertLength - removeLength)
        if (isReplaceRemovingIndexes(index, length)) { // need to find neighbor
            /* Find a neighbor of the children being removed. 
             * The neighbor will possibly replace the children
             * in the internal datastructures.
             */
            int neighborIndex;
            int neighborIndexAfterReplace;
            ViewLayoutState neighbor;
            int endRemoveIndex = index + length;
            if (index > 0) {
                neighborIndex = index - 1;
                neighborIndexAfterReplace = neighborIndex;
                neighbor = getChild(neighborIndex);

            } else if (endRemoveIndex < getChildCount()) {
                neighborIndex = endRemoveIndex;
                neighborIndexAfterReplace = index + insertLength;
                neighbor = getChild(neighborIndex);
            } else { // no neighbor
                neighborIndex = -1;
                neighborIndexAfterReplace = -1;
                neighbor = null;
            }

            replaceUpdateIndexes(index, length, insertLength,
                neighborIndex, neighborIndexAfterReplace, neighbor);
            
        } else { // not removing important children to which indexes point            
            replaceUpdateIndexes(index, length, insertLength);
        }

        double childMajorAxisOffset = 0;
        if (length == 0) { // no remove so will certainly be adding
            if (getChildCount() == 0) { // adding to empty
                ensureCapacity(insertLength); // should alloc exactly the insertLength
            }

        } else { // at least one child removed
            int endIndex = index + length;
            
            removeInvalidChildIndexesArea(index, length);

            moveMajorAxisOffsetGap(endIndex); // endIndex to leave removed with natural offsets
            moveIndexGap(endIndex);

            // Update offset gap
            childMajorAxisOffset = getMajorAxisOffset(index); // under gap because length > 0
            double majorAxisRemovedSpan = getMajorAxisOffset(endIndex)
                - childMajorAxisOffset;
            majorAxisOffsetGapIndex = index;
            majorAxisOffsetGapLength += majorAxisRemovedSpan; // increase the gap
            
            // Update indexes
            indexGapLength += length;

            // The removeUpdate() gets invoked for each removed ViewLayoutState
            remove(index, length, this); // remove from gap object array

        }

        // Handle insert
        if (insertLength > 0) {
            if (length == 0) { // no removed children
                moveIndexGap(index);
                moveMajorAxisOffsetGap(index);
                childMajorAxisOffset = getMajorAxisOffset(index) - majorAxisOffsetGapLength;
            }

            // childMajorAxisOffset must now be below offset gap

            int majorAxis = view.getMajorAxis();
            boolean estimatedSpan = view.isEstimatedSpan();
            boolean childEstimatedSpan;
            boolean insertLengthAboveThreshold;
            if (estimatedSpan) {
                childEstimatedSpan = true;
                insertLengthAboveThreshold = false; // do not schedule resetting
                
            } else { // not view's estimated span
                insertLengthAboveThreshold = (insertLength >= view.getReplaceEstimatedThreshold());
                childEstimatedSpan = insertLengthAboveThreshold;
            }
            
            for (int i = 0; i < insertLength; i++) {
                ViewLayoutState child = view.createChild(views[i]);
                View childView = child.getView();
                int childIndex = index + i;

                if (childEstimatedSpan && (childView instanceof EstimatedSpanView)) {
                    ((EstimatedSpanView)childView).setEstimatedSpan(childEstimatedSpan);
                }

                child.selectLayoutMajorAxis(majorAxis);

                insertItem(childIndex, child);
                indexGapLength--;
                majorAxisOffsetGapIndex++;

                if (!child.isFlyweight()) {
                    child.setViewRawIndex(childIndex); // all added are below gap
                    child.setLayoutMajorAxisRawOffset(childMajorAxisOffset);
                    
                    // Set the parent => that may invoke bunch of actions
                    // including change of the major axis span
                    childView.setParent(view);
                    
                    // Make sure the layout gets updated for the child
                    child.viewPreferenceChanged(true, true);
                    
                    // Index gap should not be moved because of the constraints for the GapBoxView
                    // (setting of the parent must not cause another view adding/removal)
                    // Offset map could be moved by calling childView.setParent()
                    moveMajorAxisOffsetGap(childIndex + 1);
                    
                    // Need to recompute childMajorAxisOffset - could be changed by setParent()
                    // Offset gap above the child -> no need to translate raw offset
                    childMajorAxisOffset = child.getLayoutMajorAxisRawOffset() + child.getLayoutMajorAxisPreferredSpan();

                } else { // flyweight view
                    childMajorAxisOffset += child.getLayoutMajorAxisPreferredSpan();
                }
                
            }
            
            if (childEstimatedSpan && !estimatedSpan) {
                view.resetEstimatedSpan(index, insertLength);
            }
        }
        
        // major axis has changed very likely - would be hard to verify precisely so mark for sure
        view.markMajorAxisPreferenceChanged();
        // Mark child indexes as invalid - make it as last operation because
        // it may be decided to update the layout immediately
        markLayoutInvalid(index, insertLength);
        // assuming change along major axis => make sure the repaint till the end will follow
        view.markRepaint(index, true);
        
        // No need to schedule update of the whole children.
        // It's handled in GapBoxView.replace()

        checkConsistency();
    }

    // Implements GapObjectArray.RemoveUpdater
    public void removeUpdate(Object removedItem) {
        releaseChild((ViewLayoutState)removedItem);
    }

    protected void releaseChild(ViewLayoutState child) {
        if (!child.isFlyweight()) {
            child.getView().setParent(null);
        }
        
    }

    /**
     * Check whether ongoing replace will remove any children
     * to which important indexes point.
     *
     * @return true if the replace will remove the important children
     *  or false otherwise.
     */
    protected boolean isReplaceRemovingIndexes(int index, int removeLength) {
        int ind = getMaxMinorAxisPreferredSpanChildIndex();
        return (!view.isChildrenLayoutNecessary()
            && ind >= index && ind < index + removeLength);
    }
            

    /**
     * Update indices because of the replace.
     * <br>
     * None of the important indexes points to any of the removed children.
     *
     * @param index index at which the replace is done
     * @param removeLength number of removed children
     * @param insertLength number of inserted children
     * @return true if the minor axis was likely changed due to replace done
     *  in children or false otherwise.
     */
    protected void replaceUpdateIndexes(int index, int removeLength, int insertLength) {
        // Work only if the complete children layout is not scheduled.
        // If it is then the indexes could be invalid anyway.
        if (!view.isChildrenLayoutNecessary()) {
            int endRemoveIndex = index + removeLength;
            // Check whether the index is not above the removed area and if so
            // update it. It cannot be inside removed area otherwise
            // update with neighbor would be invoked instead of this method.
            int ind = getMaxMinorAxisPreferredSpanChildIndex();
            if (ind >= endRemoveIndex) { // must update by diff
                setMaxMinorAxisPreferredSpanChildIndex(ind + insertLength - removeLength);
            }
        }
    }
    
    /**
     * Update indices because of the replace.
     * @param index index at which the replace is done
     * @param removeLength number of removed children
     * @param insertLength number of inserted children
     * @param neighborIndex index of the neighbor that certainly stays in the array
     *  after the possible removal takes place. If no children stay then
     *  the index is -1. The index is given in original index space
     *  i.e. if the neighbor is above removed area then
     *  <code>insertLength - removeLength</code> must be added to it
     *  so that index after removal is obtained.
     * @param neighborIndexAfterReplace index of the neighbor that stays
     *  in the array updated so that it points to the "real" after-replace index.
     *  It's -1 if neighbor index is -1.
     * @param neighbor neighbor child that stays in the array or null
     *  if there is no such child (index is -1).
     * @return true if the minor axis was likely changed due to replace done
     *  in children or false otherwise.
     */
    protected void replaceUpdateIndexes(int index, int removeLength, int insertLength,
    int neighborIndex, int neighborIndexAfterReplace, ViewLayoutState neighbor) {
        
        // Work only if the complete children layout is not scheduled.
        // If it is then the indexes could be invalid anyway.
        if (!view.isChildrenLayoutNecessary()) {
            int endRemoveIndex = index + removeLength;
            /* Check whether any of the removed children points
             * to maxMinorAxisPreferredSpanChildIndex.
             * If so update the index to point to the neighbor if appropriate.
             *
             * BTW here as there is just one tested index
             * it is clear that the index points into the affected area
             * so in fact there would not have to be checking for that.
             * However if there would several different indexes
             * then e.g. just one could be affected.
             */
            int ind = getMaxMinorAxisPreferredSpanChildIndex();
            if (ind >= endRemoveIndex) { // must update by diff
                setMaxMinorAxisPreferredSpanChildIndex(ind + insertLength - removeLength);

            } else if (ind >= index) { // in removed area -> need to change to neighbor
                if (neighbor == null || neighbor.getLayoutMinorAxisPreferredSpan()
                    < view.getMinorAxisPreferredSpan()
                ) {
                    // Do not know whether there is not any other child
                    // with the same span as the removed child.
                    // Anyway need the complete layout to check that.
                    view.markChildrenLayoutNecessary();
                }
                setMaxMinorAxisPreferredSpanChildIndex(neighborIndexAfterReplace);
            }
        }
    }

    /**
     * Get the visual offset of the child along the major axis.
     */
    public double getMajorAxisOffset(int childIndex) {
        if (childIndex < getChildCount()) { // to be able to return offset after last view
            ViewLayoutState child = getChild(childIndex);
            if (!child.isFlyweight()) {
                double offset = child.getLayoutMajorAxisRawOffset();
                if (childIndex >= majorAxisOffsetGapIndex) {
                    offset -= majorAxisOffsetGapLength;
                }
                return offset;
            }
        }

        double majorAxisOffset = 0;
        while (--childIndex >= 0) {
            ViewLayoutState child = getChild(childIndex);
            majorAxisOffset += child.getLayoutMajorAxisPreferredSpan();
            if (!child.isFlyweight()) {
                double offset = child.getLayoutMajorAxisRawOffset();
                if (childIndex >= majorAxisOffsetGapIndex) {
                    offset -= majorAxisOffsetGapLength;
                }
                majorAxisOffset += offset;
                break;
            }
        }

        return majorAxisOffset;
    }

    /**
     * Get current value of the preferred span along major axis
     * by subtracting offset gap length from the initial gap length
     */
    protected double getMajorAxisPreferredSpan() {
        return INITIAL_MAJOR_AXIS_OFFSET_GAP_LENGTH - majorAxisOffsetGapLength;
    }
    
    protected final float getMinorAxisPreferredSpan() {
        return minorAxisPreferredSpan;
    }
    
    protected void setMinorAxisPreferredSpan(float minorAxisPreferredSpan) {
        this.minorAxisPreferredSpan = minorAxisPreferredSpan;
    }

    /**
     * What is the offset along the minor axis
     */
    protected float getMinorAxisOffset(ViewLayoutState child) {
        float minorAxisAssignedSpan = view.getMinorAxisAssignedSpan();
        float childPreferredSpan = child.getLayoutMinorAxisPreferredSpan();
        if (childPreferredSpan < minorAxisAssignedSpan) {
            // can't make the child to fill the whole span, so align it
            float align = child.getLayoutMinorAxisAlignment();
            return ((minorAxisAssignedSpan - childPreferredSpan) * align);
        }

        return 0f;
    }
    
    protected float getMinorAxisSpan(ViewLayoutState child) {
        float minorAxisAssignedSpan = view.getMinorAxisAssignedSpan();
        float childPreferredSpan = child.getLayoutMinorAxisPreferredSpan();
        return (childPreferredSpan < minorAxisAssignedSpan) // child span lower than overall span
            ? childPreferredSpan
            : minorAxisAssignedSpan;
    }

    /**
     * Notification that a particular child has changed its preference
     * along the major axis.
     * <br>
     * No layout update is scheduled as it's assumed
     * that the child has already caused a layout update being scheduled
     * by calling <code>View.preferenceChanged()</code> in its parent.
     */
    protected void majorAxisPreferenceChanged(ViewLayoutState child,
    int childIndex, double majorAxisSpanDelta) {

        moveMajorAxisOffsetGap(childIndex + 1);
        majorAxisOffsetGapLength -= majorAxisSpanDelta;
        view.markMajorAxisPreferenceChanged();
    }

    /**
     * Notification that a particular child has changed its preference
     * along the minor axis.
     * <br>
     * No layout update is scheduled as it's assumed
     * that the child has already caused a layout update being scheduled
     * by calling <code>View.preferenceChanged()</code> in its parent.
     */
    protected void minorAxisPreferenceChanged(ViewLayoutState child, int childIndex) {
        // Work only if the complete children layout is not scheduled.
        // If it is then the indexes could be invalid anyway.
        if (!view.isChildrenLayoutNecessary()) {
            float preferredSpan = child.getLayoutMinorAxisPreferredSpan();
            float minorAxisPreferredSpan = getMinorAxisPreferredSpan();
            int maxPreferredSpanIndex= getMaxMinorAxisPreferredSpanChildIndex();

            if (maxPreferredSpanIndex == -1 // no children yet
                || preferredSpan > minorAxisPreferredSpan
            ) {
                setMinorAxisPreferredSpan(preferredSpan);
                view.markMinorAxisPreferenceChanged(); // span is wider -> chnage preference
                setMaxMinorAxisPreferredSpanChildIndex(childIndex);

            } else if (childIndex == maxPreferredSpanIndex
                && preferredSpan < minorAxisPreferredSpan
            ) { // used to be max but now shrinked - which should now be max?
                // Do not know here - need complete layout to decide
                // which is max and whether total span has shrinked
                view.markChildrenLayoutNecessary();
            }
        }
    }
    
    public int getChildStartOffset(int childIndex) {
        ViewLayoutState child = getChild(childIndex);
        if (!child.isFlyweight()) {
            return child.getView().getStartOffset();
        }
        
        int startOffset = 0;
        while (--childIndex >= 0) {
            child = getChild(childIndex);
            /* In case of flyweight view the length of the view is added.
             * In case of regular view it's ending offset.
             */
            startOffset += child.getView().getEndOffset();
            if (!child.isFlyweight()) {
                break;
            }
        }
        
        return startOffset;
    }

    public int getChildEndOffset(int childIndex) {
        int endOffset = 0;
        while (childIndex >= 0) {
            ViewLayoutState child = getChild(childIndex--);
            endOffset += child.getView().getEndOffset();
            if (!child.isFlyweight()) { // break on first non-flyweight
                break;
            }
        }
        
        return endOffset;
    }

    /**
     * Get current allocation of a child view in this view.
     * <em>This does not do update the offsets in the ViewLayoutState
     * records.</em>
     * @param index childIndex of the child
     * @param targetRect rectangle to which the allocation is set.
     *  If it's null a new rectangle is created, set and returned.
     * @return rectangle filled with the child allocation.
     */
    public Rectangle getChildCoreAllocation(int childIndex, Rectangle targetRect) {
        childrenUpdateLayout(childIndex); // update child layouts till this index

        if (targetRect == null) {
            targetRect = new Rectangle();
        }

        ViewLayoutState child = getChild(childIndex);
        int majorAxisOffsetInt = (int)getMajorAxisOffset(childIndex);
        int minorAxisOffsetInt = (int)getMinorAxisOffset(child);
        int majorAxisSpanInt = (int)child.getLayoutMajorAxisPreferredSpan();
        int minorAxisSpanInt = (int)getMinorAxisSpan(child);

        if (view.isXMajorAxis()) {
            targetRect.x = majorAxisOffsetInt;
            targetRect.y = minorAxisOffsetInt;
            targetRect.width = majorAxisSpanInt;
            targetRect.height = minorAxisSpanInt;

        } else {
            targetRect.x = minorAxisOffsetInt;
            targetRect.y = majorAxisOffsetInt;
            targetRect.width = minorAxisSpanInt;
            targetRect.height = majorAxisSpanInt;
        }

        ViewInsets insets = view.getInsets();
        if (insets != null) {
            targetRect.x += (int)insets.getLeft();
            targetRect.y += (int)insets.getTop();
        }

        return targetRect;
    }
    
    /**
     * Locate the view responsible for an offset into the box
     * along the major axis.  Make sure that offsets are set
     * on the ViewLayoutState objects up to the given target span
     * past the desired offset.
     *
     * @return index of the view representing the given visual
     *   location (majorAxisOffset), or -1 if the offset is below
     *   all the available views. <code>getChildCount()</code> is returned
     *   if the given offset is greater or equal to the visual end
     *   of the last child view.
     */
    public int getChildIndexAtCorePoint(float x, float y) {
        int childCount = getChildCount();
        int low = 0;
        int high = childCount - 1;
        
        if (high == -1) { // return -1 if there are no children
            return -1;
        }
        
        double majorAxisOffset = view.isXMajorAxis() ? x : y;

        int luChildCount = getUpdateLayoutChildCount();
        if (luChildCount > 0) { // some children not validated yet
            int firstLUChildIndex = getFirstUpdateLayoutChildIndex();
            if (getMajorAxisOffset(firstLUChildIndex) <= majorAxisOffset) { // above invalid
                childrenUpdateLayout(high); // update till the last child
                low = firstLUChildIndex;
                
            } else { // below invalid area
                high = firstLUChildIndex - 1;
            }
        }
        
        while (low <= high) {
            int mid = (low + high) / 2;
            ViewLayoutState child = getChild(mid);
            double midMajorAxisOffset = getMajorAxisOffset(mid);
            
            if (midMajorAxisOffset < majorAxisOffset) {
                low = mid + 1;
            } else if (midMajorAxisOffset > majorAxisOffset) {
                high = mid - 1;
            } else {
                // view starting exactly at the given position found
                return mid;
            }
        }
        
        // Not found exactly. 'high' points to token that possibly "contains"
        // the requested offset.
        
        // Return childCount if the last view does not contain the given offset
        if (high < 0) {
            high = 0;
        }
            
        return high;
    }

    /**
     * Paint the children that intersect the clip area.
     *
     * @param g graphics to render into.
     * @param alloc allocation for the parent view including the insets.
     *  The rectangle can be mutated.
     */
    protected void paintChildren(Graphics g, Rectangle alloc) {
        int viewAllocX = alloc.x;
        int viewAllocY = alloc.y;
        // int viewAllocWidth = alloc.width;
        // int viewAllocHeight = alloc.height;
        
        alloc = g.getClipBounds(alloc); // get the clip - reuse alloc variable
        if (alloc == null) { // no clip => return
            return;
        }
        int clipX = alloc.x;
        int clipY = alloc.y;
        boolean isXMajorAxis = view.isXMajorAxis();
        int clipEnd = isXMajorAxis
            ? (clipX + alloc.width)
            : (clipY + alloc.height);
        

        int childIndex = getChildIndexAtCorePoint(clipX, clipY);
        int childCount = getChildCount();

        for (int i = Math.max(childIndex, 0); i < childCount; i++) {
            ViewLayoutState child = getChild(i);
            // Reuse alloc for child's allocation
            alloc = getChildCoreAllocation(i, alloc);
            alloc.x += viewAllocX;
            alloc.y += viewAllocY;
            /*
             * Paint the child if it lies before the end of the clip.
             * We should also check whether the child
             * fits into the allocation given to this view by checking whether
             *     parentViewAllocation.intersects(childAllocation))
             * but for the orthogonal use this should not be necessary.
             */
            int allocStart = isXMajorAxis ? alloc.x : alloc.y;
                    
            if (allocStart < clipEnd) { // child at least partly inside clip
                
                View v = child.getView();
                v.paint(g, alloc);
            } else {
                break; // stop painting of children
            }
        }
    }

    protected final int getFirstRepaintChildIndex() {
        return firstRepaintChildIndex;
    }
    
    final void setFirstRepaintChildIndex(int firstRepaintChildIndex) {
        this.firstRepaintChildIndex = firstRepaintChildIndex;
    }

    final int getMaxMinorAxisPreferredSpanChildIndex() {
        return maxMinorAxisPreferredSpanChildIndex;
    }

    void setMaxMinorAxisPreferredSpanChildIndex(int maxMinorAxisPreferredSpanChildIndex) {
        this.maxMinorAxisPreferredSpanChildIndex = maxMinorAxisPreferredSpanChildIndex;
    }

    /**
     * Get index of the first child that needs layout update.
     */
    final int getFirstUpdateLayoutChildIndex() {
        return firstUpdateLayoutChildIndex;
    }
    
    /**
     * Get number of children that need layout update.
     * <br>
     * The first child that needs layout is determined
     * by {@link #getFirstUpdateLayoutChildIndex()}.
     */
    final int getUpdateLayoutChildCount() {
        return updateLayoutChildCount;
    }
    
    void markLayoutInvalid(int firstChildIndex, int count) {
        if (count > 0) {
            if (updateLayoutChildCount > 0) {
                int endInvalid = firstUpdateLayoutChildIndex + updateLayoutChildCount; // precede assignment that follows
                firstUpdateLayoutChildIndex = Math.min(firstUpdateLayoutChildIndex, firstChildIndex);
                updateLayoutChildCount = Math.max(endInvalid, firstChildIndex + count)
                    - firstUpdateLayoutChildIndex;

            } else { // no invalid indexes yet
                firstUpdateLayoutChildIndex = firstChildIndex;
                updateLayoutChildCount = count;
                // Mark the layout of the view as invalid (propagate to parent).
                // This must be done as last operation here because
                // parent may immediately start updating of the children' layout
                view.markLayoutInvalid();
            }
        }
    }

    /**
     * Forget that indexes in the given index area were possibly
     * marked as invalid (needing layout recomputation).
     */
    void removeInvalidChildIndexesArea(int firstChildIndex, int count) {
        int endChildIndex = firstChildIndex + count;
        if (updateLayoutChildCount > 0) { // some invalid children
            int endInvalid = firstUpdateLayoutChildIndex + updateLayoutChildCount;
            if (endInvalid > firstChildIndex) { // overlap or after
                if (firstUpdateLayoutChildIndex >= endChildIndex) { // after removed range
                    firstUpdateLayoutChildIndex -= count;
                } else if (firstUpdateLayoutChildIndex < firstChildIndex) { // starts below invalid area
                    updateLayoutChildCount -= Math.min(endInvalid, endChildIndex) - firstChildIndex;
                } else { // invalid are starts inside removed area
                    updateLayoutChildCount -= Math.min(endInvalid, endChildIndex) - firstUpdateLayoutChildIndex;
                    firstUpdateLayoutChildIndex = endChildIndex;
                    while (updateLayoutChildCount > 0 && getChild(firstUpdateLayoutChildIndex).isFlyweight()) {
                        firstUpdateLayoutChildIndex++;
                        updateLayoutChildCount--;
                    }
                }
            }
        }
    }

    /**
     * If necessary update layout of the particular child and possibly
     * all the children that precede it that require their layout to be updated.
     *
     * @param tillChildIndex index of the child that should be updated. Indexes above it
     *  can stay not updated.
     */
    protected final void childrenUpdateLayout(int tillChildIndex) {
        while (updateLayoutChildCount > 0 && firstUpdateLayoutChildIndex <= tillChildIndex) {
            updateLayoutChildCount--;
            ViewLayoutState child = getChild(firstUpdateLayoutChildIndex++);
            child.updateLayout();
        }
    }
    
    protected final void childrenUpdateLayout() {
        childrenUpdateLayout(Integer.MAX_VALUE);
    }
    
    /**
     * Compute complete layout information of the children along the minor axis.
     * This is typically done when a particular child changes its layout
     * in a way that affects the whole view. For example if the child
     * having the maximum span along the minor axis shrinks then
     * it's unknown whether it's still the one with the maximum span
     * or whether there isn't other child having the same or bigger span.
     * <br>
     * This default implementation only computes a child with maximum
     * preferred span along the minor axis that defines the span
     * of the whole view but subclasses may do e.g. baseline layout.
     * <br>
     * This method is called during layout update of the children.
     */
    protected void childrenLayout() {
        // Find child with maximum prefered span
        // along the minor axis

        int childCount = getChildCount();
        int maxPreferredSpanChildIndex = -1;
        float maxPreferredSpan = 0f;

        for (int i = 0; i < childCount; i++) {
            ViewLayoutState child = getChild(i);
            float span = child.getLayoutMinorAxisPreferredSpan();
            if (span > maxPreferredSpan) {
                maxPreferredSpanChildIndex = i;
                maxPreferredSpan = span;
            }

        }
        // Remember the maximum child
        setMaxMinorAxisPreferredSpanChildIndex(maxPreferredSpanChildIndex);

        // This will be preferred span of the view along minor axis
        if (maxPreferredSpan != getMinorAxisPreferredSpan()) {
            setMinorAxisPreferredSpan(maxPreferredSpan);
            view.markMinorAxisPreferenceChanged();
        }
    }

    protected void unload() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            releaseChild(getChild(i));
        }
    }

    private void moveIndexGap(int index) {
        checkConsistency();

        int gapLen = indexGapLength; // cache to local var
        int belowIndex = index;
        boolean updated = false;
        while (--belowIndex >= 0) {
            ViewLayoutState child = getChild(belowIndex);
            if (!child.isFlyweight()) {
                int rawIndex = child.getViewRawIndex();
                if (rawIndex >= gapLen) {
                    child.setViewRawIndex(rawIndex - gapLen);
                    updated = true;
                } else { // below index gap
                    break; // stop
                }
            }
        }

        if (!updated) { // need to check whether the gap starts at index
            int childCount = getChildCount();
            while (index < childCount) {
                ViewLayoutState child = getChild(index++);
                if (!child.isFlyweight()) {
                    int rawIndex = child.getViewRawIndex();
                    if (rawIndex < gapLen) { // below gap
                        child.setViewRawIndex(rawIndex + gapLen);
                    } else {
                        break; // found first child already above gap
                    }
                }
            }
        }

        checkConsistency();
    }

    private void moveMajorAxisOffsetGap(int index) {
        if (index == majorAxisOffsetGapIndex) {
            return; // no work
        }

        checkConsistency();

        if (index < majorAxisOffsetGapIndex) { // need to move gap start down
            while (--majorAxisOffsetGapIndex >= index) {
                ViewLayoutState child = getChild(majorAxisOffsetGapIndex);
                if (!child.isFlyweight()) {
                    child.setLayoutMajorAxisRawOffset(child.getLayoutMajorAxisRawOffset()
                        + majorAxisOffsetGapLength);
                }
            }
            majorAxisOffsetGapIndex++;

        } else { // index above gap index or equal to it
            while (majorAxisOffsetGapIndex < index) { // need to move gap start down
                ViewLayoutState child = getChild(majorAxisOffsetGapIndex);
                if (!child.isFlyweight()) {
                    child.setLayoutMajorAxisRawOffset(child.getLayoutMajorAxisRawOffset()
                        - majorAxisOffsetGapLength);
                }
                majorAxisOffsetGapIndex++;
            }
        }

        checkConsistency();
    }

    private void checkConsistency() {
        if (true) {
            return; // disabled
        }
//        System.out.println(toStringDetail());
        
        int childCount = getChildCount();
        if (majorAxisOffsetGapIndex > childCount) {
            throw new IllegalStateException(
                "majorAxisOffsetGapIndex=" + majorAxisOffsetGapIndex // NOI18N
                + " > childCount=" + childCount // NOI18N
            );
        }

        int indexGapStart = computeIndexGapStart();
        int lastRawIndex = -1;
        double lastMajorAxisOffset = 0;
        for (int i = 0; i < childCount; i++) {
            ViewLayoutState child = getChild(i);

            int rawIndex = child.getViewRawIndex();
            if (rawIndex <= lastRawIndex) {
                throw new IllegalStateException("rawIndex=" + rawIndex // NOI18N
                    + " at index=" + i // NOI18N
                    + " <= lastRawIndex=" + lastRawIndex); // NOI18N
            }

            if (rawIndex >= indexGapLength && rawIndex < indexGapStart) {
                throw new IllegalStateException("Above gap rawIndex=" + rawIndex // NOI18N
                    + " at index=" + i // NOI18N
                    + " but gap seems to start at index " + indexGapStart); // NOI18N
            }

            if (rawIndex < indexGapLength && rawIndex >= indexGapStart) {
                throw new IllegalStateException("Below gap rawIndex=" + rawIndex // NOI18N
                    + " at index=" + i // NOI18N
                    + " but gap seems to start at index " + indexGapStart); // NOI18N
            }

            if (getChildIndexNoCheck(child) != i) {
                throw new IllegalStateException("Child at index=" + i // NOI18N
                    + " has internal index=" + getChildIndexNoCheck(child)); // NOI18N
            }


            double childMajorAxisOffset = child.getLayoutMajorAxisRawOffset();
            if (i >= majorAxisOffsetGapIndex) {
                childMajorAxisOffset -= majorAxisOffsetGapLength;
            }

            if (childMajorAxisOffset < lastMajorAxisOffset) {
                throw new IllegalStateException(
                    "childMajorAxisOffset=" + childMajorAxisOffset // NOI18N
                    + " at index=" + i // NOI18N
                    + " < lastMajorAxisOffset=" + lastMajorAxisOffset); // NOI18N
            }

            if (childMajorAxisOffset < 0) {
                throw new IllegalStateException("Major axis offset at index=" + i // NOI18N
                    + " is " + childMajorAxisOffset + " < 0"); // NOI18N
            }
        }
    }

    private int computeIndexGapStart() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (getChild(i).getViewRawIndex() >= indexGapLength) {
                return  i;
            }
        }
        return childCount;
    }

    public String toStringDetail() {
        StringBuffer sb = new StringBuffer();
        sb.append(view.toString());
        sb.append(", indexGapStart="); // NOI18N
        sb.append(computeIndexGapStart());
        sb.append(", majorAxisOffsetGapIndex="); // NOI18N
        sb.append(majorAxisOffsetGapIndex);
        sb.append(", majorAxisOffsetGapLength="); // NOI18N
        sb.append(majorAxisOffsetGapLength);
        

        sb.append(view.childrenToString());

        return sb.toString();
    }

}
