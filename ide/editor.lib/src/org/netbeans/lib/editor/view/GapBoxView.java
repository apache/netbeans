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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import org.netbeans.editor.view.spi.EstimatedSpanView;
import org.netbeans.editor.view.spi.ViewInsets;
import org.netbeans.editor.view.spi.ViewLayoutState;
import org.netbeans.lib.editor.util.swing.ElementUtilities;

/**
 * Composite view implementation inspired
 * by the {@link javax.swing.text.AsyncBoxView}
 * holding its children in <code>GapObjectArray</code>
 * and capable of posting complex layout changes
 * into a separate layout thread.
 * <br>
 * This implementation is synchronous by default
 * but it contains hooks where the asynchronous behavior
 * can be installed.
 *
 * <p>
 * The operation of this view relies on the presence
 * of {@link LockView} under the root view in the view hierarchy.
 * <br>
 * All the view operation is expected to be single-threaded.
 * <br>
 * The view can only work with document instances
 * extending {@link javax.swing.text.AbstractDocument}
 * <br>
 * The view effectively only operates with preferred spans
 * of its children. However it can be extended
 * to consider minimum and maximum spans as well
 * if the particular layout algorithm would require it.
 *
 * <p>
 * This view implementation separates information
 * related to its children from the information required
 * for its own operation. The children are kept in a separate object.
 * <br>
 * The view allows to release its children after the layout information
 * for the whole view was determined but the view is not actively
 * being rendered. Releasing of the children saves memory
 * without loosing valuable information
 * about view's own layout.
 * <br>
 * The view does not initialize its children
 * upon call to {@link #setParent(javax.swing.text.View)}
 * thus saving memory and cpu time.
 * Only if it was previously asked for some information
 * related to children (e.g. <code>getViewCount()</code>)
 * or for preferred, minimum or maximum span of this view
 * it will initialize them during <code>setParent()</code>.
 * Once the parent view was set then the children
 * are populated immediately once anyone asks
 * for children related information or for spans.
 *
 * <p>
 * The view can be constructed and work with element parameter being null.
 * <br>
 * The following things need to be ensured when using the view in such setup:
 * <ul>
 *   <li> <code>getDocument()</code>,
 *        <code>getStartOffset()</code>,
 *        <code>getEndOffset()</code>,
 *        <code>getAttributes()</code>
 *           must be overriden to not delegate to element.
 *
 *   <li> <code>insertUpdate()</code> and <code>removeUpdate()</code>
 *       methods will not find any element changes.
 *
 *   <li> <code>reloadChildren()</code> should be revisited
 *       whether it will create possible children in a proper way.
 *
 * </ul>
 * 
 * <p>
 * Various constraints for using of this view implementation:
 * <ul>
 *   <li> setParent() implementations of the views being added
 *      to this view are allowed to trigger
 *      <code>ViewLayoutState.Parent.majorAxisPreferenceChanged()</code>
 *      synchronously.
 *   <li> setParent() implementations of the views being added
 *      to this view are not allowed to add or remove another children
 *      synchronously.
 * </ul>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class GapBoxView extends View
implements ViewLayoutState.Parent, ViewLayoutState,
EstimatedSpanView {

    // -J-Dorg.netbeans.lib.editor.view.GapBoxView.level=FINE
    private static final Logger LOG = Logger.getLogger(GapBoxView.class.getName());
    
    /**
     * Bit value in <code>statusBits</code> determining
     * whether X_AXIS is the major axis.
     * <br>
     * If this flag is not set then Y_AXIS
     * is the major axis.
     */
    private static final int X_MAJOR_AXIS_BIT = 1;

    /**
     * Bit value in <code>statusBits</code> determining
     * whether the major axis of this view
     * is orthogonal to the major axis of the layout state
     * that this view acts like.
     */
    private static final int MAJOR_AXES_ORTHOGONAL_BIT = 2;
    
    /**
     * Bit value in <code>statusBits</code> determining
     * whether preference for this view changed along the major axis.
     */
    private static final int MAJOR_AXIS_PREFERENCE_CHANGED_BIT = 4;
    
    /**
     * Bit value in <code>statusBits</code> determining
     * whether preference for this view changed along the minor axis.
     */
    private static final int MINOR_AXIS_PREFERENCE_CHANGED_BIT = 8;

    /**
     * Bit value in <code>statusBits</code> determining
     * whether complete layout of the children has to be done
     * during the next layout update of the children.
     */
    private static final int CHILDREN_LAYOUT_NECESSARY_BIT = 16;

    /**
     * Bit value in <code>statusBits</code> determining
     * whether there are any pending repaint requests from children
     * to be processed.
     */
    private static final int REPAINT_PENDING_BIT = 32;

    /**
     * Bit value in <code>statusBits</code> determining
     * whether more than one view along the major axis
     * needs to be repainted. The first view to repaint
     * is determined by <code>firstRepaintChildIndex</code>.
     * The whole area starting with the first view
     * to repaint till the end of the view will be repainted.
     */
    private static final int REPAINT_TILL_END_BIT = 64;

    /**
     * Bit value in <code>statusBits</code> determining
     * whether the preferred, minimum and maximum
     * spans along the axes are estimated instead
     * of measured exactly.
     */
    private static final int ESTIMATED_SPAN_BIT = 128;
    
    /**
     * Bit value in <code>statusBits</code> determining
     * whether the updateLayout() is currently being executed.
     */
    private static final int UPDATE_LAYOUT_IN_PROGRESS = 256;
    
    /**
     * Bit value in <code>statusBits</code> determining
     * whether this view is actively acting as layout state
     * i.e. if it has parent view being instance
     * of <code>ViewLayoutState.Parent</code>.
     */
    private static final int ACTIVE_LAYOUT_STATE = 512;
    

    // Bits related to this view acting as layout state follow

    /**
     * Bit value in <code>statusBits</code> determining
     * whether the x is the major axis of this view
     * participating as implementation of ViewLayoutState.
     */
    private static final int LAYOUT_STATE_X_MAJOR_AXIS_BIT
        = (ACTIVE_LAYOUT_STATE << 1);

    /**
     * Bit value in <code>statusBits</code> determining
     * that size of the view needs to be set again
     * by <code>View.setSize()</code>
     */
    private static final int LAYOUT_STATE_VIEW_SIZE_INVALID_BIT
        = (LAYOUT_STATE_X_MAJOR_AXIS_BIT << 1);
    
    /**
     * Last bit in <code>statusBits</code> used for operation
     * of this view. Subclasses may use higher bits if they want.
     */
    protected static final int GAP_BOX_VIEW_LAST_USED_STATUS_BIT
        = LAYOUT_STATE_VIEW_SIZE_INVALID_BIT;
    
    /**
     * Bit composition in <code>statusBits</code> determining
     * whether any of the parameters affecting layout have
     * changed and need to be updated.
     */
    private static final int LAYOUT_STATE_ANY_INVALID
        = MAJOR_AXIS_PREFERENCE_CHANGED_BIT
            | MINOR_AXIS_PREFERENCE_CHANGED_BIT
            | LAYOUT_STATE_VIEW_SIZE_INVALID_BIT
            | REPAINT_PENDING_BIT;

    /**
     * Composition of the bits forming the status of this view.
     */
    private int statusBits; // 16 bytes View + 4 = 20 bytes
    
    /**
     * Maintainer of </code>ViewLayoutState</code> children.
     */
    private GapBoxViewChildren children; // 20 + 4 = 24 bytes

    /**
     * Raw offset along the major axis of this view 
     * when acting as layout state.
     */
    private double layoutStateMajorAxisRawOffset; // 24 + 8 = 32 bytes

    /**
     * Raw index of this view in its parent
     * when acting as layout state.
     */
    private int viewRawIndex; // 32 + 4 = 36 bytes
    
    /**
     * Cached preferred span along the major axis useful when this view acts
     * as layout state. Its value gets updated during layout update.
     * <br>
     * This is also the value returned by getMinimumSize(),
     * getPreferredSize(), and getMaximumSize() along
     * the major axis if the children are not present.
     * <br>
     * The value does include insets.
     * <br>
     * <code>float</code> is chosen as the view most typically
     * has major axes orthogonal (see isMajorAxesOrthogonal()).
     */
    private float lastMajorAxisPreferredSpan; // 36 + 4 = 40 bytes

    /**
     * Cached preferred span along the minor axis.
     * <br>
     * This is also the value returned by getMinimumSize(),
     * getPreferredSize(), and getMaximumSize() along
     * the major axis if the children are not present.
     * <br>
     * The value does include insets.
     */
    private float lastMinorAxisPreferredSpan; // 40 + 4 = 44 bytes
    
    /**
     * Current span along the minor axis as set by <code>setSize()</code>.
     * <br>
     * The value does include insets.
     */
    private float minorAxisAssignedSpan; // 44 + 4 = 48 bytes
    
    /**
     * Construct a composite box view over the given element.
     *
     * @param elem the element of the model to represent.
     * @param majorAxis the axis to tile along.  This can be
     *  either X_AXIS or Y_AXIS.
     */
    public GapBoxView(Element elem, int majorAxis) {
        super(elem);

        if (majorAxis == View.X_AXIS) {
            setStatusBits(X_MAJOR_AXIS_BIT);
        } // by default there should be no bits set
    }

    /**
     * Determines the preferred span for this view along an
     * axis.
     *
     * @param axis may be either View.X_AXIS or View.Y_AXIS
     * @return   the span the view would like to be rendered into &gt;= 0.
     *           Typically the view is told to render into the span
     *           that is returned, although there is no guarantee.
     *           The parent may choose to resize or break the view.
     * @exception IllegalArgumentException for an invalid axis type
     */
    public float getPreferredSpan(int axis) {
//        assert ViewUtilities.isAxisValid(axis);

        return (axis == getMajorAxis())
            ? (float)getMajorAxisPreferredSpan()
            : getMinorAxisPreferredSpan();
    }
    
    /**
     * Determines the minimum span for this view along an
     * axis.
     *
     * @param axis may be either <code>View.X_AXIS</code> or
     *		<code>View.Y_AXIS</code>
     * @return  the minimum span the view can be rendered into
     */
    public @Override float getMinimumSpan(int axis) {
        // If the following implementation gets overriden the view should
        // consider additional caching variables because it's acting
        // as a layout state.
        return getPreferredSpan(axis); // getResizeWeight() not reflected
    }
    
    /**
     * Determines the maximum span for this view along an
     * axis.
     *
     * @param axis may be either <code>View.X_AXIS</code> or
     *		<code>View.Y_AXIS</code>
     * @return  the maximum span the view can be rendered into
     */
    public @Override float getMaximumSpan(int axis) {
        // If the following implementation gets overriden the view should
        // consider additional caching variables because it's acting
        // as a layout state.
        return getPreferredSpan(axis); // getResizeWeight() not reflected
    }
    
    public @Override float getAlignment(int axis) {
        return 0.0f;
    }

    /**
     * Get the insets.
     * The default implementation here only returns null
     * but it can be redefined by subclasses.
     * @return insets of this view or null for no insets.
     */
    public ViewInsets getInsets() {
        return null;
    }

    /**
     * Get current preferred span along the major axis.
     */
    final double getMajorAxisPreferredSpan() {
        return (children != null)
            ? children.getMajorAxisPreferredSpan() + getMajorAxisInsetSpan()
            : lastMajorAxisPreferredSpan; // if no children then use cached value
    }
    
    final float getMinorAxisPreferredSpan() {
        return (children != null)
            ? children.getMinorAxisPreferredSpan() + getMinorAxisInsetSpan()
            : lastMinorAxisPreferredSpan;
    }

    /**
     * Get span along minor axis assigned to this view
     * by calling <code>View.setSize()</code> on it.
     */
    final float getMinorAxisAssignedSpan() {
        return minorAxisAssignedSpan;
    }
    
    /**
     * Fetch the major axis (the axis the children
     * are tiled along).  This will have a value of
     * either X_AXIS or Y_AXIS.
     */
    public final int getMajorAxis() {
        return isXMajorAxis() ? View.X_AXIS : View.Y_AXIS;
    }
    
    /**
     * Fetch the minor axis (the axis orthoginal
     * to the tiled axis).  This will have a value of
     * either X_AXIS or Y_AXIS.
     */
    public final int getMinorAxis() {
        return isXMajorAxis() ? View.Y_AXIS : View.X_AXIS;
    }

    /**
     * Return true if X is major axis or false if Y is major axis.
     */
    final boolean isXMajorAxis() {
        return isStatusBitsNonZero(X_MAJOR_AXIS_BIT);
    }

    /**
     * Test whether the major axis of this view
     * is orthogonal to the major axis of the layout state
     * that this view acts like.
     *
     * @return true if the axes are orthogonal (which is more common case)
     *  or false if the axes are equal.
     *  <br>
     *  For example if a line view extends GapBoxView
     *  then it has X as the major axis but it has an Y layout state
     *  major axis (when acting as layout state for a document view)
     *  so this method would return true in this case.
     */
    final boolean isMajorAxesOrthogonal() {
        return isStatusBitsNonZero(MAJOR_AXES_ORTHOGONAL_BIT);
    }

    /**
     * Returns the number of child views of this view.
     *
     * @return the number of views &gt;= 0
     * @see #getView(int)
     */
    public @Override int getViewCount() {
        return getChildren().getChildCount();
    }
    
    /**
     * Returns the view in this container with the particular index.
     *
     * @param index index of the desired view, &gt;= 0 and &lt; getViewCount()
     * @return the view at index <code>index</code>
     */
    public @Override View getView(int index) {
        return getChild(index).getView();
    }
    
    /*
     * Replaces child views.  If there are no views to remove
     * this acts as an insert.  If there are no views to
     * add this acts as a remove.  Views being removed will
     * have the parent set to <code>null</code>,
     * and the internal reference to them removed so that they
     * may be garbage collected.
     * 
     * <p>
     * It is necessary to call <code>updateLayout()</code>
     * on this view at some point later so that the possible
     * layout changes are done.
     *
     * @param index the starting index into the child views >= 0
     * @param length the number of existing views to replace >= 0
     * @param views the child views to insert
     */
    public @Override void replace(int index, int length, View[] views) {
        if (length < 0) {
            throw new IllegalArgumentException("length=" + length + " < 0"); // NOI18N
        }
        
        if (length == 0 && (views == null || views.length == 0)) { // nothing to do
            return;
        }

        // make sure that the children are populated
        GapBoxViewChildren ch = getChildren();

        // Handle raplace in children only if either length > 0
        // or insertLength > 0 or both are > 0
        ch.replace(index, length, views);
    }

    /**
     * Get minimum number of children that must be added at once
     * by replace() so that the addition is treated as a lengthy
     * operation which means that all the added children will
     * have the estimated span flag turned on and
     * {@link #scheduleResetChildrenEstimatedSpan(int)}
     * will be called to walk through the added children
     * and reset their estimated span to false.
     */
    protected int getReplaceEstimatedThreshold() {
        return Integer.MAX_VALUE;
    }

    public final boolean isEstimatedSpan() {
        return isStatusBitsNonZero(ESTIMATED_SPAN_BIT);
    }
    
    public void setEstimatedSpan(boolean estimatedSpan) {
        if (isEstimatedSpan() != estimatedSpan) { // really changed
            if (estimatedSpan) {
                setStatusBits(ESTIMATED_SPAN_BIT);
            } else {
                clearStatusBits(ESTIMATED_SPAN_BIT);

                // If children exist make sure the task is scheduled to update them
                if (children != null) {
                    int viewCount = getViewCount();
                    if (viewCount > 0) {
                        resetEstimatedSpan(0, viewCount); // reset all children
                    }
                }
            }
            
        }
    }
    
    /**
     * Set estimated span flag to false on the given children views.
     * <br>
     * This method is called from both <code>setEstimatedSpan()</code>
     * and from <code>children.replace()</code> if the number of added
     * children exceeds threshold count.
     * <br>
     * Subclasses may want to do this on the background.
     */
    protected void resetEstimatedSpan(int childIndex, int count) {
        while (--count >= 0) {
            ViewLayoutState child = getChild(childIndex);
            View childView = child.getView();
            if (childView instanceof EstimatedSpanView) {
                ((EstimatedSpanView)childView).setEstimatedSpan(false);
            }
            childIndex++;
        }
    }
        
    /**
     * Remove the child views in the given index range
     * and let the default building mechanism to build the child views.
     *
     * <p>
     * It is necessary to call <code>updateLayout()</code>
     * on this view at some point later so that the possible
     * layout changes are done.
     *
     * @param index index of the first child view to be rebuilt
     * @param count number of chilren in the children array to be rebuilt.
     *   If <code>index + count<code> is past the end of the children available
     *   the value of count will be decreased accordingly.
     */
    public void rebuild(int index, int count) {
        if (count != 0) {
            int startOffset = (index == 0) ? -1 : getView(index - 1).getEndOffset();
            int viewCount = getViewCount();
            int endIndex = Math.min(index + count, viewCount);
            int endOffset = (endIndex == viewCount) ? -1 : getView(endIndex).getStartOffset();
            boolean loggable = LOG.isLoggable(Level.FINE);
            long tm = 0;
            if (loggable) {
                tm = System.currentTimeMillis();
            }
            reloadChildren(index, count, startOffset, endOffset);
            if (loggable) {
                LOG.fine("GapBoxView.rebuild(): " + (System.currentTimeMillis() - tm) + // NOI18N
                    "ms; index=" + index + // NOI18N
                    ", count=" + count + // NOI18N
                    ", <" + startOffset + ", " + endOffset + ">\n" // NOI18N
                );
            }
        }
    }
    
    /**
     * Rebuild based on specification of the offset range.
     *
     * @param startOffset starting offset of the area in which the views
     *  should be rebuilt.
     * @param endOffset ending offset of the area in which the views
     *  should be rebuilt.
     */
    public void offsetRebuild(int startOffset, int endOffset) {
        int index = ViewUtilitiesImpl.findLowerViewIndex(this, startOffset, false);
        int count;
        if (index == -1) { // no child views
            index = 0;
            count = 0;
            
        } else { // child views exist
            count = ViewUtilitiesImpl.findUpperViewIndex(this, endOffset, true) - index + 1;
        }
        
        rebuild(index, count);
    }
    
    /**
     * Sets the parent of the view.
     * The children are only initialized if someone
     * has previously asked for information
     * related to children (e.g. <code>getViewCount()</code>)
     * or for preferred, minimum or maximum span of this view.
     *
     * @param parent the parent of the view, <code>null</code> if none
     */
    public @Override void setParent(View parent) {
        super.setParent(parent);
        
        /* Make sure that the children get loaded.
         * It is necessary to do because children preferences will
         * define the preferences of the parent.
         */
        if (parent != null) {
            if (parent instanceof ViewLayoutState.Parent) {
                setStatusBits(ACTIVE_LAYOUT_STATE);
            } else {
                clearStatusBits(ACTIVE_LAYOUT_STATE);
            }
            
            // Resolving whether active layout state must be resolved prior getChildren()
            getChildren();
            
        } else { // parent is being set to null
            releaseChildren();
            clearStatusBits(ACTIVE_LAYOUT_STATE);
        }
    }
    
    public final boolean isActiveLayoutState() {
        return isStatusBitsNonZero(ACTIVE_LAYOUT_STATE);
    }

    GapBoxViewChildren getChildren() {
        if (children == null) {
            children = createChildren();

            // Possibly load the children
            View parent = getParent();
            if (parent != null) { // initialize with valid view factory only
                reloadChildren(0, 0, -1, -1);
            }
        }
        
        return children;
    }
    
    /**
     * Get children or null if the children were not yet initialized.
     */
    final GapBoxViewChildren getChildrenNull() {
        return children;
    }

    /**
     * Ask for releasing of the children.
     * The view will still remember the last allocated size
     * and preferred, minimum and maximum spans.
     * However various operations like painting or translations
     * between model and visual positions will
     * make the children to be loaded again.
     */
    public void releaseChildren() {
        if (children != null) {
            unloadChildren();

            children.unload();
            children = null;
        }
    }

    // Implements ViewLayoutState
    public final View getView() {
        return this;
    }
    
    // Implements ViewLayoutState
    public ViewLayoutState selectLayoutMajorAxis(int axis) {
//        assert ViewUtilities.isAxisValid(axis);

        if (axis == View.X_AXIS) {
            setStatusBits(LAYOUT_STATE_X_MAJOR_AXIS_BIT);
        } else { // y as layout major axis
            clearStatusBits(LAYOUT_STATE_X_MAJOR_AXIS_BIT);
        }
        
        // Determine whether major axis of this view
        // is orthogonal to major axis for acting as layout state
        if (axis == getMajorAxis()) { // major axes equal
            clearStatusBits(MAJOR_AXES_ORTHOGONAL_BIT);
        } else { // major axes orthogonal
            setStatusBits(MAJOR_AXES_ORTHOGONAL_BIT);
        }
        
        return this;
    }
    
    // Implements ViewLayoutState
    public boolean isFlyweight() {
        return false;
    }
    
    // Implements ViewLayoutState
    public void updateLayout() {
        if (isLayoutValid()) { // Nothing to do
            return;
        }
        
        if (isStatusBitsNonZero(UPDATE_LAYOUT_IN_PROGRESS)) {
            return;
        }
        setStatusBits(UPDATE_LAYOUT_IN_PROGRESS);

        View parent = getParent();
        if (parent == null) { // disconnected from hierarchy
            return;
        }
        ViewLayoutState.Parent lsParent = (parent instanceof ViewLayoutState.Parent)
            ? (ViewLayoutState.Parent)parent
            : null;

        // Make sure all individual pending children layout updates are addressed
        children.childrenUpdateLayout();

        // Layout the children if necessary
        if (isChildrenLayoutNecessary()) {
            resetChildrenLayoutNecessary();

            children.childrenLayout(); // re-compute layout info for children
        }

        // Update cached variable corresponding to layout state major axis
        boolean parentWillRepaint = false;

        // Check whether preference did not change along a particular axis
        // and if so message preferenceChanged to parent.
        // Cache the following two vars before they get cleared in next section:
        boolean majorAxisPreferenceChanged = isMajorAxisPreferenceChanged();
        boolean minorAxisPreferenceChanged = isMinorAxisPreferenceChanged();
        resetAxesPreferenceChanged();

        if (majorAxisPreferenceChanged) {
            // Update the cached value for the major axis
            if (children != null) { // only if children exist
                double delta = updateLastMajorAxisPreferredSpan();
                if (delta != 0.0d && lsParent != null) {
                    if (isMajorAxesOrthogonal()) {
                        lsParent.minorAxisPreferenceChanged(this);
                    } else {
                        lsParent.majorAxisPreferenceChanged(this, delta);
                        parentWillRepaint = true; 
                    }
                }
            }
        }

        if (minorAxisPreferenceChanged) {
            // Update the cached value for the minor axis
            if (children != null) { // only if children exist
                double delta = updateLastMinorAxisPreferredSpan();
                if (delta != 0.0d && lsParent != null) {
                    if (isMajorAxesOrthogonal()) {
                        lsParent.majorAxisPreferenceChanged(this, delta);
                        parentWillRepaint = true; 
                    } else {
                        lsParent.minorAxisPreferenceChanged(this);
                    }
                }
            }
        }

        // If not active layout state propagate preference change upwards
        // If this is active layout state then this was already propagated
        // by marking itself as needing layout update which is now being
        // updated.
        if (majorAxisPreferenceChanged || minorAxisPreferenceChanged || !isActiveLayoutState()) {
            // Either of major or minor axis (or both) has changed
            boolean horizontalChange = false;
            boolean verticalChange = false;

            if (isXMajorAxis()) {
                horizontalChange = majorAxisPreferenceChanged;
                verticalChange = minorAxisPreferenceChanged;
            } else {
                horizontalChange = minorAxisPreferenceChanged;
                verticalChange = majorAxisPreferenceChanged;
            }

            parent.preferenceChanged(this, horizontalChange, verticalChange);
        }

        // Check whether size must be set on this view
        if (isStatusBitsNonZero(LAYOUT_STATE_VIEW_SIZE_INVALID_BIT)) {
            clearStatusBits(LAYOUT_STATE_VIEW_SIZE_INVALID_BIT);

            if (lsParent != null) { // should only be done when having layout state parent
                float width;
                float height;
                float layoutStateMajorAxisSpan = getPreferredSpan(getLayoutStateMajorAxis());
                float layoutStateMinorAxisSpan = lsParent.getMinorAxisSpan(this);
                if (isXLayoutStateMajorAxis()) {
                    width = layoutStateMajorAxisSpan;
                    height = layoutStateMinorAxisSpan;
                } else {
                    width = layoutStateMinorAxisSpan;
                    height = layoutStateMajorAxisSpan;
                }

                setSize(width, height);
            }
        }
        
        if (children != null && isRepaintPending()) {
            if (!parentWillRepaint) {
                processRepaint(lsParent);
            }
            // After painting is finished reset the variables
            resetRepaintPending();
        }
        
        clearStatusBits(UPDATE_LAYOUT_IN_PROGRESS);
        
        // Call recursively to make sure that there is no more work.
        updateLayout();
    }
    
    /**
     * Update the layout in response to receiving notification of
     * change from the model.
     *
     * @param ec changes to the element this view is responsible
     *  for (may be null if there were no changes).
     * @param e the change information from the associated document
     * @param a the current allocation of the view
     * @see #insertUpdate
     * @see #removeUpdate
     * @see #changedUpdate
     */
    protected @Override void updateLayout(DocumentEvent.ElementChange ec, DocumentEvent e, Shape a) {
        
        //super.updateLayout(ec, e, a);
    }

    /**
     * Called by children to mark layout of this view invalid.
     * This only has effect if this view is active layout state.
     */
    public void layoutInvalid(ViewLayoutState child) {
        int childIndex = children.getChildIndexNoCheck(child);
        children.markLayoutInvalid(childIndex, 1);
    }
    
    protected void markLayoutInvalid() {
        if (isActiveLayoutState()) {
            ((ViewLayoutState.Parent)getParent()).layoutInvalid(this);
        } else { // not active layout state
            // Update layout immediately - subclasses can override
            directUpdateLayout();
        }
    }
    
    /**
     * This method is called when this view is not acting as active
     * layout state and its layout becomes invalid.
     * <br>
     * By default the layout is updated immediately
     * but subclasses may change that but they must ensure
     * that the layout will be updated later.
     */
    protected void directUpdateLayout() {
        updateLayout();
    }
    
    /**
     * Process pending repaint requests from children.
     * <br>
     * Children are guaranteed to be non-null once this method gets called.
     */
    protected void processRepaint(ViewLayoutState.Parent lsParent) {
        if (lsParent != null) { // parent view is ViewLayoutState.Parent
            int firstRepaintChildIndex = children.getFirstRepaintChildIndex();
            double majorAxisOffset = children.getMajorAxisOffset(firstRepaintChildIndex);
            double repaintMajorOffset;
            double repaintMajorSpan;
            float repaintMinorOffset;
            float repaintMinorSpan;
            if (isRepaintTillEnd()
                || firstRepaintChildIndex >= getViewCount() // bit strange but possible after last child remove
            ) {
                if (isMajorAxesOrthogonal()) {
                    repaintMajorOffset = 0;
                    repaintMajorSpan = 0; // till end of view's span in parent
                    repaintMinorOffset = (float)majorAxisOffset;
                    repaintMinorSpan = 0; // till parent view minor span end

                } else { // major axes equal
                    repaintMajorOffset = majorAxisOffset;
                    repaintMajorSpan = 0; // till end of view's span in parent
                    repaintMinorOffset = 0;
                    repaintMinorSpan = 0; // till parent view minor span end
                }

            } else { // repainting just single child that did not change major axis span
                double majorAxisSpan = getChild(firstRepaintChildIndex).getLayoutMajorAxisPreferredSpan();
                if (isMajorAxesOrthogonal()) {
                    repaintMajorOffset = 0;
                    repaintMajorSpan = 0; // till end of view's span in parent
                    repaintMinorOffset = (float)majorAxisOffset;
                    repaintMinorSpan = (float)majorAxisSpan;

                } else { // major axes equal
                    repaintMajorOffset = majorAxisOffset;
                    repaintMajorSpan = majorAxisSpan;
                    repaintMinorOffset = 0;
                    repaintMinorSpan = 0; // till parent view minor span end
                }
            }
            
            lsParent.repaint(this, repaintMajorOffset, repaintMajorSpan,
                repaintMinorOffset, repaintMinorSpan);
            
        } else { // do not know allocation here => repaint whole component
            Component c = getContainer();
            if (c != null) {
                c.repaint();
            }
        }
    }

    /**
     * Mark that the child with the given index should be repainted.
     *
     * @param childIndex index of child that should be marked for repaint.
     * @param repaintTillEnd if set to true then all children following
     *  the child should be repainted as well.
     * @return true if lower child index was marked for repaint by this method
     *  than there was before.
     */
    protected boolean markRepaint(int childIndex, boolean repaintTillEnd) {
        boolean lowerIndexMarked = false;
        if (children != null) {
            int firstRepaintChildIndex = children.getFirstRepaintChildIndex();
            if (!isRepaintTillEnd()) { // not repainting more yet
                if (firstRepaintChildIndex == -1) { // no repainting yet
                    lowerIndexMarked = true;
                    markRepaintPending();
                    children.setFirstRepaintChildIndex(childIndex);
                    if (repaintTillEnd) {
                        setStatusBits(REPAINT_TILL_END_BIT);
                    }

                } else if (firstRepaintChildIndex != childIndex) { // other child than first
                    if (childIndex < firstRepaintChildIndex) {
                        lowerIndexMarked = true;
                        children.setFirstRepaintChildIndex(childIndex);
                    }
                    setStatusBits(REPAINT_TILL_END_BIT); // surely will repaint to end

                } else { // same child already scheduled for repaint
                    if (repaintTillEnd) {
                        setStatusBits(REPAINT_TILL_END_BIT);
                    }
                }

            } else { // repaint more children already - firstRepaintChildIndex must be valid
                if (childIndex < firstRepaintChildIndex) {
                    lowerIndexMarked = true;
                    children.setFirstRepaintChildIndex(childIndex);
                }
            }
        }
        
        return lowerIndexMarked;
    }
    
    public final boolean isRepaintPending() {
        return isStatusBitsNonZero(REPAINT_PENDING_BIT);
    }
    
    protected final void markRepaintPending() {
        setStatusBits(REPAINT_PENDING_BIT);
    }
    
    protected void resetRepaintPending() {
        if (children != null) {
            children.setFirstRepaintChildIndex(-1);
        }
        clearStatusBits(REPAINT_PENDING_BIT | REPAINT_TILL_END_BIT);
    }
    
    public final boolean isRepaintTillEnd() {
        return isStatusBitsNonZero(REPAINT_TILL_END_BIT);
    }
    
    /**
     * Test whether the preference along the layout state minor axis
     * has really changed.
     * <br>
     * The default implementation only checks preferred span
     * but the implementation reflecting minimum and maximum spans
     * can extend this method.
     *
     * @return true if it has really changed or false if not.
     */
    protected boolean isLayoutMinorAxisPreferenceChanged(boolean majorAxesOrthogonal) {
        double delta;
        if (majorAxesOrthogonal) {
            // processing minor layout state axis but it's in fact major view axis
            delta = updateLastMajorAxisPreferredSpan();
        } else { // major axes equal
            // processing minor layout state axis which is also minor view axis
            delta = updateLastMinorAxisPreferredSpan();
        }
        
        return (delta != 0.0d);
    }

    private double updateLastMinorAxisPreferredSpan() {
        float currentMinorAxisPreferredSpan = children.getMinorAxisPreferredSpan();
        double delta = currentMinorAxisPreferredSpan - lastMinorAxisPreferredSpan;
        lastMinorAxisPreferredSpan = currentMinorAxisPreferredSpan;
        return delta;
    }
    
    private double updateLastMajorAxisPreferredSpan() {
        double currentMajorAxisPreferredSpan = children.getMajorAxisPreferredSpan();
        double delta = currentMajorAxisPreferredSpan - lastMajorAxisPreferredSpan;
        // Here the truncation occurs but if the major axes are orthogonal
        // or if the spans are not big enough to exhaust float precision
        // this should not hurt.
        lastMajorAxisPreferredSpan = (float)currentMajorAxisPreferredSpan;
        return delta;
    }
    
    // Implements ViewLayoutState
    public boolean isLayoutValid() {
        return !isStatusBitsNonZero(LAYOUT_STATE_ANY_INVALID)
            && (children == null || children.getUpdateLayoutChildCount() == 0);
    }

    // Implements ViewLayoutState
    public double getLayoutMajorAxisPreferredSpan() {
        return (isMajorAxesOrthogonal())
            ? lastMinorAxisPreferredSpan
            : lastMajorAxisPreferredSpan;
    }            
    
    // Implements ViewLayoutState
    public float getLayoutMinorAxisPreferredSpan() {
        return isMajorAxesOrthogonal()
            ? lastMajorAxisPreferredSpan
            : lastMinorAxisPreferredSpan;
    }

    // Implements ViewLayoutState
    public float getLayoutMinorAxisMinimumSpan() {
        // It has to be overriden if the layout state minimum span is maintained
        return getLayoutMinorAxisPreferredSpan();
    }

    // Implements ViewLayoutState
    public float getLayoutMinorAxisMaximumSpan() {
        // It has to be overriden if the layout state maximum span is maintained
        return getLayoutMinorAxisPreferredSpan();
    }
    
    // Implements ViewLayoutState
    public float getLayoutMinorAxisAlignment() {
        // Alignment is assumed not to change over time
        // It needs to be cached if that's not true
        return getAlignment(getLayoutStateMinorAxis());
    }
    
    // Implements ViewLayoutState
    public double getLayoutMajorAxisRawOffset() {
        return layoutStateMajorAxisRawOffset;
    }
    
    // Implements ViewLayoutState
    public void setLayoutMajorAxisRawOffset(double majorAxisRawOffset) {
        this.layoutStateMajorAxisRawOffset = majorAxisRawOffset;
    }
    
    protected final ViewLayoutState.Parent getLayoutStateParent() {
        View parent = getParent();
        return (parent instanceof ViewLayoutState.Parent)
            ? ((ViewLayoutState.Parent)parent)
            : null;
    }

    protected final boolean isXLayoutStateMajorAxis() {
        return (isStatusBitsNonZero(LAYOUT_STATE_X_MAJOR_AXIS_BIT));
    }
    
    protected final int getLayoutStateMajorAxis() {
        return (isStatusBitsNonZero(LAYOUT_STATE_X_MAJOR_AXIS_BIT))
            ? View.X_AXIS 
            : View.Y_AXIS;
    }

    protected final int getLayoutStateMinorAxis() {
        return (isStatusBitsNonZero(LAYOUT_STATE_X_MAJOR_AXIS_BIT))
            ? View.Y_AXIS 
            : View.X_AXIS;
    }
    
    // Implements ViewLayoutState
    public int getViewRawIndex() {
        return viewRawIndex;
    }
    
    // Implements ViewLayoutState
    public void setViewRawIndex(int viewRawIndex) {
        this.viewRawIndex = viewRawIndex;
    }

    // Implements ViewLayoutState
    public void viewPreferenceChanged(boolean width, boolean height) {
        markViewSizeInvalid();
    }
    
    // Implements ViewLayoutState
    public void markViewSizeInvalid() {
        setStatusBits(LAYOUT_STATE_VIEW_SIZE_INVALID_BIT);
    }

    // Implements ViewLayoutState.Parent
    /**
     * Preference of one of the children has changed along the major axis.
     */
    public void majorAxisPreferenceChanged(ViewLayoutState child, double majorAxisSpanDelta) {
        int childIndex = getChildIndexNoCheck(child);
        if (majorAxisSpanDelta != 0.0d) {
            // repaint till end as the children above the index get shifted
            markRepaint(childIndex, true);
            children.majorAxisPreferenceChanged(child, childIndex, majorAxisSpanDelta);

        } else { // make sure that the child gets repainted
            markRepaint(childIndex, false);
        }
    }

    // Implements ViewLayoutState.Parent
    /**
     * Preference of one of the children has changed along the minor axis.
     */
    public void minorAxisPreferenceChanged(ViewLayoutState child) {
        int childIndex = getChildIndexNoCheck(child);
        markRepaint(childIndex, false);
        children.minorAxisPreferenceChanged(child, childIndex);
    }
    
    // Implements ViewLayoutState.Parent
    /**
     * Get span of the given child along the minor axis of this view.
     */
    public float getMinorAxisSpan(ViewLayoutState child) {
        // Delegate to children
        return getChildren().getMinorAxisSpan(child);
    }
    
    // Implements ViewLayoutState.Parent
    public void repaint(ViewLayoutState child,
    double majorAxisOffset, double majorAxisSpan,
    float minorAxisOffset, float minorAxisSpan) {

        int childIndex = getChildIndexNoCheck(child);
        markRepaint(childIndex, false);
    }

    /**
     * Test whether complete layout of the children necessary.
     */
    public final boolean isChildrenLayoutNecessary() {
        return isStatusBitsNonZero(CHILDREN_LAYOUT_NECESSARY_BIT);
    }
    
    /**
     * Mark that a complete layout of children is necessary.
     * <br>
     * This method does no scheduling of the children layout update.
     */
    public final void markChildrenLayoutNecessary() {
        setStatusBits(CHILDREN_LAYOUT_NECESSARY_BIT);
    }
    
    final void resetChildrenLayoutNecessary() {
        clearStatusBits(CHILDREN_LAYOUT_NECESSARY_BIT);
    }

    /**
     * Child views can call this on the parent to indicate that
     * the preference has changed and should be reconsidered
     * for layout.  This is reimplemented to queue new work
     * on the layout thread.  This method gets messaged from
     * multiple threads via the children.
     *
     * @param childView the child view of this view or null to signal
     *  change in this view. 
     * @param width true if the width preference has changed
     * @param height true if the height preference has changed
     * @see javax.swing.JComponent#revalidate
     */
    public @Override void preferenceChanged(View childView, boolean width, boolean height) {
        if (childView == null) { // notify parent about this view change
            getParent().preferenceChanged(this, width, height);

        } else { // Child of this view has changed
            // First find the index of the child view
            int index;
            // Try to cast the view to ViewLayoutState and find index that way 
            if (childView instanceof ViewLayoutState) {
                // Trust the view to be really child of this view - check is done later
                index = getChildIndexNoCheck((ViewLayoutState)childView);
            } else { // child view not instance of ViewLayoutState
                // Use binary search to find the view
                index = getViewIndex(childView.getStartOffset());
            }

            ViewLayoutState child = getChild(index);
            if (child.getView() != childView) {
                int ind;
                for (ind = getViewCount() - 1; ind >= 0; ind--) {
                    if (getView(ind) == childView) {
                        break;
                    }
                }
                if (ind == -1) {
                    throw new IllegalArgumentException("childView=" // NOI18N
                        + childView + " not child of view " + this); // NOI18N

                } else { // is child but at different index
                    throw new IllegalStateException(
                        "Internal error. Child expected at index=" + index // NOI18N
                        + " but found at index=" + ind); // NOI18N
                }
            }

            // Mark the child as invalid
            child.viewPreferenceChanged(width, height);

            // Mark the layout of the child as invalid - this must be done
            // _after_ the real changes affecting child's layout were performed
            // because the layout may be directly updated
            // by the parent during the call of the following method.
            children.markLayoutInvalid(index, 1);
        }
    }
    
    /**
     * Sets the size of the view.  This should cause
     * layout of the view if the view caches any layout
     * information.
     *
     * <p>
     * The propagation of this operation to child views
     * can be done asynchronously if appropriate.
     *
     * @param width the width &gt;= 0
     * @param height the height &gt;= 0
     */
    public @Override void setSize(float width, float height) {
        float targetMajorAxisSpan;
        float targetMinorAxisSpan;
        if (isXMajorAxis()) {
            targetMajorAxisSpan = width;
            targetMinorAxisSpan = height;
        } else { // Y is major axis
            targetMajorAxisSpan = height;
            targetMinorAxisSpan = width;
        }
        
        // Span along major axis is ignored by default
        setSpanOnMajorAxis(targetMajorAxisSpan);
        setSpanOnMinorAxis(targetMinorAxisSpan);
    }
    
    protected void setSpanOnMajorAxis(float targetMajorAxisSpan) {
        // along the major axis the value is ignored by default
        // but subclasses doing e.g. line wrapping can override that
    }
    
    protected void setSpanOnMinorAxis(float targetMinorAxisSpan) {
        if (targetMinorAxisSpan != minorAxisAssignedSpan) {
            minorAxisAssignedSpan = targetMinorAxisSpan;
            //float targetSpanNoInsets = targetMinorAxisSpan - getMinorAxisInsetSpan();

            // do not recompute children if estimated span or estimated change task running
            if (!isEstimatedSpan() && !isChildrenResizeDisabled()) {
                // mark all of the ViewLayoutState instances as needing to
                // resize the child.
                int viewCount = getViewCount();
                if (viewCount != 0) {
                    markSizeInvalid(0, viewCount);
                }
            }
        }
    }

    /**
     * This method marks sizes of all the children as invalid
     * so the next layout update will resize each children.
     * <br>
     * This is made as protected method since large complex views
     * may consider this operation lengthy with certain amount
     * of children so they may need to do this operation in background
     * and delegate to this implementation for small amount 
     * of children only.
     *
     * @param &gt;0 total number of child views of this view. It's given
     *  as parameter because subclasses will typically decide their
     *  behavior based on the total view count.
     */
    protected void markSizeInvalid(int childIndex, int count) {
        while (--count >= 0) {
            ViewLayoutState child = getChild(childIndex);
            if (!child.isFlyweight()) {
                child.markViewSizeInvalid();
            }
            childIndex++;
        }

        // Mark the layout of the child as invalid - this must be done
        // _after_ the real changes affecting child's layout were performed
        // because the layout may be directly updated
        // by the parent during the call of the following method.
        children.markLayoutInvalid(childIndex, count);
    }

    /**
     * Return true if the children should not be attempted to resize
     * once <code>setSize()</code> is called on this view.
     * <br>
     * Turning this on may save considerable time but it should be only
     * used if the views truly do not react on <code>setSize()</code>
     * e.g. this should *not* be used if line-wrapping is turned on.
     */
    protected boolean isChildrenResizeDisabled() {
        return false; // by default must resize children upon setSize() on view
    }
    
    /**
     * Fetches the allocation for the given child view.
     * This enables finding out where various views
     * are located, without assuming the views store
     * their location.  This returns null since the
     * default is to not have any child views.
     *
     * @param index the index of the child, &gt;= 0 and &lt; getViewCount()
     * @param a  the allocation to this view.
     * @return the allocation to the child
     */
    public @Override Shape getChildAllocation(int index, Shape a) {
        if (a == null) {
            return null;
        }

        Rectangle alloc = reallocate(a); // returned rect can be modified
        int thisViewAllocX = alloc.x;
        int thisViewAllocY = alloc.y;

        getChildren().getChildCoreAllocation(index, alloc); // alloc overwritten
        alloc.x += thisViewAllocX;
        alloc.y += thisViewAllocY;
        
        // Add insets if necessary
        ViewInsets insets = getInsets();
        if (insets != null) {
            alloc.x += insets.getLeft();
            alloc.y += insets.getRight();
        }

        return alloc;
    }
    
    /**
     * Fetches the child view index at the given point.
     * This is called by the various View methods that
     * need to calculate which child to forward a message
     * to.
     *
     * @param x the X coordinate &gt;= 0
     * @param y the Y coordinate &gt;= 0
     * @param a the allocation to thid view
     * @return index of the view that best represents the given visual
     *   location or -1 if there are no children.
     *   <br>
     *   If the point is below the area of the first child view
     *   then the index of the first child view is returned.
     *   <br>
     *   If the point is above the area of the last child view
     *   then the index of the last child view is returned.
     */
    public int getViewIndexAtPoint(float x, float y, Shape a) {
        Rectangle alloc = reallocate(a); // returned rect can be modified
        x -= alloc.x;
        y -= alloc.y;

        // Subtract insets if necessary
        ViewInsets insets = getInsets();
        if (insets != null) {
            x -= insets.getLeft();
            y -= insets.getRight();
        }

        return getChildren().getChildIndexAtCorePoint(x, y);
    }
    
    /**
     * Returns the child view index representing the given position in
     * the model.
     *
     * @param offset the position >= 0.
     * @param b either forward or backward bias.
     * @return  index of the view representing the given position, or 
     *   -1 if no view represents that position
     */
    public @Override int getViewIndex(int offset, Position.Bias b) {
	if (b == Position.Bias.Backward) {
	    offset -= 1;
	}
        
        return getViewIndex(offset);
    }
    
    /**
     * Returns the child view index representing the given position in
     * the model.
     *
     * @param offset the position >= 0.
     * @return  index of the view representing the given position, or 
     *   -1 if no view represents that position
     */
    public int getViewIndex(int offset) {
        return ViewUtilitiesImpl.findViewIndexBounded(this, offset);
    }

    /**
     * Render the view using the given allocation and
     * rendering surface.
     *
     * @param g the rendering surface to use
     * @param a the allocated region to render into
     * @see View#paint
     */
    public void paint(Graphics g, Shape a) {
        Rectangle alloc = reallocate(a); // returned rect can be modified
        getChildren().paintChildren(g, alloc);
    }
    
    /**
     * Provides a mapping from the document model coordinate space
     * to the coordinate space of the view mapped to it.
     *
     * @param pos the position to convert &gt;= 0
     * @param a the allocated region to render into
     * @param b the bias toward the previous character or the
     *  next character represented by the offset, in case the
     *  position is a boundary of two views.
     * @return the bounding box of the given position is returned
     * @exception BadLocationException  if the given position does
     *   not represent a valid location in the associated document
     * @exception IllegalArgumentException for an invalid bias argument
     * @see View#viewToModel
     */
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        int index = getViewIndex(pos, b);
        if (index >= 0) {
            Shape ca = getChildAllocation(index, a);

            // forward to the child view
            ViewLayoutState child = getChild(index);
            View cv = child.getView();
            return cv.modelToView(pos, ca, b);
        } else {
            Document doc = getDocument();
            int docLen = (doc != null) ? doc.getLength() : -1;
            throw new BadLocationException("Offset " + pos + " with bias " + b + " is outside of the view" //NOI18N
                + ", children = " + getViewCount() //NOI18N
                + (getViewCount() > 0 ? " covering offsets <" +  //NOI18N
                    getView(0).getStartOffset() + ", " +  //NOI18N
                    getView(getViewCount() - 1).getEndOffset() + ">" : "") + //NOI18N
                    ", docLen=" + docLen
                , pos);
        }
    }
    
    /**
     * Provides a mapping from the view coordinate space to the logical
     * coordinate space of the model.  The biasReturn argument will be
     * filled in to indicate that the point given is closer to the next
     * character in the model or the previous character in the model.
     * <p>
     * This is expected to be called by the GUI thread, holding a
     * read-lock on the associated model.  It is implemented to
     * locate the child view and determine it's allocation with a
     * lock on the ChildLocator object, and to call viewToModel
     * on the child view with a lock on the ViewLayoutState object
     * to avoid interaction with the layout thread.
     *
     * @param x the X coordinate &gt;= 0
     * @param y the Y coordinate &gt;= 0
     * @param a the allocated region to render into
     * @return the location within the model that best represents the
     *  given point in the view &gt;= 0.  The biasReturn argument will be
     * filled in to indicate that the point given is closer to the next
     * character in the model or the previous character in the model.
     */
    public int viewToModel(float x, float y, Shape a, Position.Bias[] biasReturn) {
        int pos;    // return position
        int index;  // child index to forward to
        Shape ca;   // child allocation
        
        index = getViewIndexAtPoint(x, y, a);
        index = Math.max(index, 0);
        if (index < getViewCount()) {
            ca = getChildAllocation(index, a);

            // forward to the child view
            ViewLayoutState child = getChild(index);
            View v = child.getView();
            pos = v.viewToModel(x, y, ca, biasReturn);

        } else { // at the end
            int endOff = getEndOffset();
            Document doc = getDocument();
            pos = (doc!=null && doc.getLength() < endOff) ? doc.getLength() : endOff;
        }

        return pos;
    }
    
    /**
     * Provides a way to determine the next visually represented model
     * location that one might place a caret.  Some views may not be visible,
     * they might not be in the same order found in the model, or they just
     * might not allow access to some of the locations in the model.
     *
     * @param pos the position to convert &gt;= 0
     * @param a the allocated region to render into
     * @param direction the direction from the current position that can
     *  be thought of as the arrow keys typically found on a keyboard;
     *  this may be one of the following:
     *  <ul>
     *  <code>SwingConstants.WEST</code>
     *  <code>SwingConstants.EAST</code>
     *  <code>SwingConstants.NORTH</code>
     *  <code>SwingConstants.SOUTH</code>
     *  </ul>
     * @param biasRet an array contain the bias that was checked
     * @return the location within the model that best represents the next
     *  location visual position
     * @exception BadLocationException
     * @exception IllegalArgumentException if <code>direction</code> is invalid
     */
    public @Override int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a,
    int direction, Position.Bias[] biasRet) throws BadLocationException {

        return ViewUtilitiesImpl.getNextVisualPositionFrom(
            this, pos, b, a, direction, biasRet);
    }

    /**
     * Fetch the object representing the layout state of
     * of the child at the given index.
     *
     * @param index the child index.
     *   This must be a value &gt;= 0 and &lt; getViewCount().
     * @throws IndexOutOfBoundsException in case the index was invalid.
     */
    protected final ViewLayoutState getChild(int index) {
        return getChildren().getChild(index);
    }
    
    /**
     * Get the index of the given child layout state in this view.
     *
     * @param child layout state which index in this view should be found.
     * @return &gt;=0 integer index of the given child in this view.
     *   Returns -1 if the given child is not present at the given index
     *   in this view.
     */
    protected final int getChildIndex(ViewLayoutState child) {
        return getChildren().getChildIndex(child);
    }
    
    /**
     * Get the index of the given child layout state in this view.
     *
     * @param child layout state which index in this view should be found.
     * @return &gt;=0 integer index of the given child in this view.
     *   <b>Note:</b> This method does no checking whether the child
     *   is really the child of this view.
     */
    protected final int getChildIndexNoCheck(ViewLayoutState child) {
        return getChildren().getChildIndexNoCheck(child);
    }

    /**
     * Can be overriden by subclasses to return
     * a different children implementation.
     */
    GapBoxViewChildren createChildren() {
        return new GapBoxViewChildren(this);
    }

    protected boolean useCustomReloadChildren() {
        return (getElement() == null);
    }
    
    public @Override void insertUpdate(DocumentEvent evt, Shape a, ViewFactory f) {
        // #38993 - until parent is set - do not do anything
        if (children == null && getParent() == null) {
            return;
        }

        if (useCustomReloadChildren()) {
            customInsertUpdate(evt, a, f);
        } else { // custom insert update
            super.insertUpdate(evt, a, f); // default element-based update
        }
    }

    protected void customInsertUpdate(DocumentEvent evt, Shape a, ViewFactory f) {
        int[] offsetRange = getInsertUpdateRebuildOffsetRange(evt);
        if (offsetRange != null) {
            offsetRebuild(offsetRange[0], offsetRange[1]);
        } else {
            forwardUpdate(null, evt, a, f);
        }
    }
    
    /**
     * Get the offset area in which the views should be rebuilt
     * in reaction to insert update in the underlying document.
     *
     * @param evt document event for the document modification.
     * @return two-item integer array containing starting and ending offset
     *  of the area to be rebuilt or <code>null</code> in case
     *  no views should be rebuilt.
     */
    protected int[] getInsertUpdateRebuildOffsetRange(DocumentEvent evt) {
        DocumentEvent.ElementChange lineChange = evt.getChange(evt.getDocument().getDefaultRootElement());
        if (lineChange == null) {
            return null;
        }

        int startOffset = evt.getOffset();
        int endOffset = startOffset + evt.getLength();
        int[] offsetRange = new int[] {startOffset, endOffset};
        Element[] addedLines = lineChange.getChildrenAdded();
        ElementUtilities.updateOffsetRange(addedLines, offsetRange);
        Element[] removedLines = lineChange.getChildrenRemoved();
        ElementUtilities.updateOffsetRange(removedLines, offsetRange);
        return offsetRange;
    }
    
    public @Override void removeUpdate(DocumentEvent evt, Shape a, ViewFactory f) {
        // #38993 - until parent is set - do not do anything
        if (children == null && getParent() == null) {
            return;
        }

        if (useCustomReloadChildren()) {
            customRemoveUpdate(evt, a, f);
        } else {
            super.removeUpdate(evt, a, f); // default element-based update
        }
    }

    protected void customRemoveUpdate(DocumentEvent evt, Shape a, ViewFactory f) {
        int[] offsetRange = getRemoveUpdateRebuildOffsetRange(evt);
        if (offsetRange != null) {
            offsetRebuild(offsetRange[0], offsetRange[1]);
        } else {
            forwardUpdate(null, evt, a, f);
        }
    }
    
    /**
     * Get the offset area in which the views should be rebuilt
     * in reaction to insert update in the underlying document.
     *
     * @param evt document event for the document modification.
     * @return two-item integer array containing starting and ending offset
     *  of the area to be rebuilt or <code>null</code> in case
     *  no views should be rebuilt.
     */
    protected int[] getRemoveUpdateRebuildOffsetRange(DocumentEvent evt) {
        DocumentEvent.ElementChange lineChange = evt.getChange(evt.getDocument().getDefaultRootElement());
        if (lineChange == null) {
            return null;
        }

        int startOffset = evt.getOffset();
        int endOffset = startOffset;
        int[] offsetRange = new int[] {startOffset, endOffset};
        Element[] addedLines = lineChange.getChildrenAdded();
        ElementUtilities.updateOffsetRange(addedLines, offsetRange);
        Element[] removedLines = lineChange.getChildrenRemoved();
        ElementUtilities.updateOffsetRange(removedLines, offsetRange);
        return offsetRange;
    }
    
    public @Override void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        // #38993 - until parent is set - do not do anything
        if (children == null && getParent() == null) {
            return;
        }
        
        super.changedUpdate(e, a, f);
    }
    
    /**
     * Load the children in the selected range of offsets.
     * <br>
     * Some implementations may reload all the present children if necessary.
     *
     * @param index index at which the views should be added/replaced.
     * @param removeLength number of removed children views. It is useful
     *  when rebuilding children for a portion of the view.
     * @param startOffset starting offset of the loading. It can be -1
     *  to indicate the loading from <code>View.getStartOffset()</code>.
     * @param endOffset ending offset of the loading. It can be -1
     *  to indicate the loading till <code>View.getEndOffset()</code>.
     */
    protected void reloadChildren(int index, int removeLength, int startOffset, int endOffset) {
        if (useCustomReloadChildren()) {
            if (startOffset == -1) {
                startOffset = getStartOffset();
            }
            if (endOffset == -1) {
                endOffset = getEndOffset();
            }

            customReloadChildren(index, removeLength, startOffset, endOffset);

        } else { // element load of children
            Element elem = getElement();
            int startIndex;
            if (startOffset == -1) {
                startIndex = 0;
            } else {
                if (index == 0) {
                    if (startOffset != getStartOffset()) {
                        throw new IllegalArgumentException("Invalid startOffset=" + startOffset); // NOI18N
                    }
                } else {
                    if (startOffset != getView(index - 1).getEndOffset()) {
                        throw new IllegalArgumentException("Invalid startOffset=" + startOffset); // NOI18N
                    }
                }
                startIndex = index;
            }

            int endIndex = (endOffset == -1)
                ? elem.getElementCount()
                : elem.getElementIndex(endOffset - 1) + 1;

// TODO uncomment            assert (startIndex == index);

            elementReloadChildren(index, removeLength, endIndex - startIndex);
        }
    }

    /**
     * Loads child views by tracking child elements of the element
     * this view was created for.
     * @param index index at which the views should be added/replaced.
     * @param removeLength number of removed children views. It is useful
     *  when rebuilding children for a portion of the view.
     * @param elementIndex index of the first child element for which
     *  the view should be created
     * @param elementCount number of elements for which the views should be created.
     */
    protected void elementReloadChildren(int index, int removeLength,
    int elementCount) {

        Element e = getElement();
        View[] added = null;

        ViewFactory f = getViewFactory();
        // Null view factory can mean that one of the grand parents is already disconnected
        // from the view hierarchy. No added children for null factory.
            
        if (f != null) {
            added = new View[elementCount];
            for (int i = 0; i < elementCount; i++) {
                added[i] = f.create(e.getElement(index + i));
            }

        }

        replace(index, removeLength, added);
    }
    
    /**
     * Loads child views in a custom way.
     *
     * @param index index at which the views should be added/replaced.
     * @param removeLength number of removed children views. It is useful
     *  when rebuilding children for a portion of the view.
     * @param startOffset starting offset from which the loading starts.
     * @param endOffset ending offset where the loading ends.
     */
    protected void customReloadChildren(int index, int removeLength,
    int startOffset, int endOffset) {

        View[] added = null;
        ViewFactory f = getViewFactory();
        // Null view factory can mean that one of the grand parents is already disconnected
        // from the view hierarchy. No added children for null factory.
        
        if (f != null) {
            Element elem = getElement();

            int elementCount = elem.getElementCount();
            int elementIndex = (elem != null) ? elem.getElementIndex(startOffset) : -1;
            if (elementIndex >= elementCount) {
                return; // Create no after last element
            }
            List childViews = new ArrayList();
            int viewCount = getViewCount();

            loop:
            while (startOffset < endOffset) {
                // Create custom child
                View childView = createCustomView(f, startOffset, endOffset, elementIndex);
                if (childView == null) {
                    throw new IllegalStateException("No view created for area (" // NOI18N
                        + startOffset + ", " + endOffset + ")"); // NOI18N
                }

                // Assuming childView.getStartOffset() is at startOffset
                childViews.add(childView);

                // Update elementIndex
                int childViewEndOffset = childView.getEndOffset();
                while (childViewEndOffset > endOffset) {
/*                    throw new IllegalStateException(
                        "childViewEndOffset=" + childViewEndOffset // NOI18N
                        + " > endOffset=" + endOffset // NOI18N
                    );
 */
                    /* The created child view interferes with a view
                     * that is still present and which is not planned
                     * to be removed.
                     * This can happen e.g. when a fold hierarchy change
                     * (caused by a document change) is fired
                     * prior to the document change gets fired
                     * to the view hierarchy.
                     * The fix for that situation is to continue to remove
                     * the present views until the end of the created view will match
                     * a beginning of a present view.
                     */
                    if (index + removeLength >= viewCount) {
                        // Should not happen but can't remove past the last view
                        break;
                    }
                    endOffset = getView(index + removeLength).getEndOffset();
                    removeLength++;
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine(
                            "GapBoxView.customReloadChildren(): Increased removeLength to "  // NOI18N
                            + removeLength + ", eo=" + endOffset // NOI18N
                        );
                    }
                }

                Element childElem = elem.getElement(elementIndex);
                while (childElem.getEndOffset() <= childViewEndOffset) {
                    elementIndex++;
                    if (elementIndex == elementCount) {
                        // #115034
                        break loop;
                    }
                    childElem = elem.getElement(elementIndex);
                }

                startOffset = childViewEndOffset;
            }

            added = new View[childViews.size()];
            childViews.toArray(added);
        }

        replace(index, removeLength, added);
    }
    
    /**
     * Create custom child view starting at <code>startOffset</code>.
     *
     * @param f view factory to be used.
     * @param startOffset offset at which the created view must start.
     * @param maxEndOffset maximum ending offset to which the created view
     *  may span.
     * @param elementIndex index of the child element that best represents
     *  the startOffset. The element is child of the element that this view
     *  is responsible for. If this view is not based by element then this
     *  parameter will be -1.
     */
    protected View createCustomView(ViewFactory f,
    int startOffset, int maxEndOffset, int elementIndex) {


/*
        // Default implementation delegating to view factory
        // is here just to show the possible functionality
        // and clarify the variables

        View v;
        if (parentElement != null) {
            Element elem = parentElement.getElement(elementIndex);
            if (elem.getStartOffset() != startOffset) {
                throw new IllegalStateException("Not element boundary");
            }

            if (elem.getEndOffset() > maxEndOffset) {
                throw new IllegalStateException("Beyond maximum ending offset");
            }

            v = f.create(elem);

        } else { // no element - need more information
            return null;
        }
 */
            
        return null;
    }

    /**
     * Subclasses may override this method and deallocate resources
     * bound to presence of children.
     * <br>
     * It's called by {@link #releaseChildren()} to unallocate
     * the resources for children.
     *
     * <p>
     * Once this method finishes all the children will
     * be set null as a parent and the reference
     * to children will be cleared.
     */
    protected void unloadChildren() {
    }
    
    /**
     * New ViewLayoutState records are created through
     * this method to allow subclasses the extend
     * the ViewLayoutState records to do/hold more
     */
    protected ViewLayoutState createChild(View v) {
        ViewLayoutState child;
        if (v instanceof ViewLayoutState) {
            child = (ViewLayoutState)v;
        } else { // view does not implement ViewLayoutState
            child = createDefaultChild(v);
        }
        return child;   
    }

    /**
     * Return default implementation of the view layout state wrapper.
     */
    protected ViewLayoutState createDefaultChild(View v) {
        return new SimpleViewLayoutState(v); // only handle preferred spans
    }
    
    protected final boolean isMajorAxisPreferenceChanged() {
        return (isStatusBitsNonZero(MAJOR_AXIS_PREFERENCE_CHANGED_BIT));
    }

    protected void markMajorAxisPreferenceChanged() {
        setStatusBits(MAJOR_AXIS_PREFERENCE_CHANGED_BIT);
    }
    
    protected final boolean isMinorAxisPreferenceChanged() {
        return (isStatusBitsNonZero(MINOR_AXIS_PREFERENCE_CHANGED_BIT));
    }

    protected void markMinorAxisPreferenceChanged() {
        setStatusBits(MINOR_AXIS_PREFERENCE_CHANGED_BIT);
    }
    
    protected final void resetAxesPreferenceChanged() {
        clearStatusBits(MAJOR_AXIS_PREFERENCE_CHANGED_BIT | MINOR_AXIS_PREFERENCE_CHANGED_BIT);
    }
    
    /**
     * Get the span along an axis that is taken up by the view insets.
     *
     * @param axis the axis to determine the total insets along,
     *  either X_AXIS or Y_AXIS.
     * @return span along the given axis taken up by view insets.
     */
    protected final float getInsetSpan(int axis) {
//        assert ViewUtilities.isAxisValid(axis);

        ViewInsets insets = getInsets();
        return (insets != null)
            ? ((axis == X_AXIS) ? insets.getLeftRight() : insets.getTopBottom())
            : 0;
    }

    /**
     * Get the span along major axis that is taken up by the view insets.
     *
     * @return span along major axis taken up by view insets.
     */
    protected final float getMajorAxisInsetSpan() {
        ViewInsets insets = getInsets();
        return (insets != null)
            ? (isXMajorAxis() ? insets.getLeftRight() : insets.getTopBottom())
            : 0;
    }

    /**
     * Get the span along minor axis that is taken up by the view insets.
     *
     * @return span along minor axis taken up by view insets.
     */
    protected final float getMinorAxisInsetSpan() {
        ViewInsets insets = getInsets();
        return (insets != null)
            ? (isXMajorAxis() ? insets.getTopBottom() : insets.getLeftRight())
            : 0;
    }

    protected final int getStatusBits(int bits) {
        return (statusBits & bits);
    }
    
    protected final boolean isStatusBitsNonZero(int bits) {
        return (getStatusBits(bits) != 0);
    }
    
    protected final void setStatusBits(int bits) {
        statusBits |= bits;
    }
    
    protected final void clearStatusBits(int bits) {
        statusBits &= ~bits;
    }

    /**
     * Reallocate the view to the new size given by the passed shape.
     *
     * @param a shape to which to reallocate the view.
     * @return rectangle bounding the shape. The returned rectangle
     *  can be mutated.
     */
    protected Rectangle reallocate(Shape a) {
        Rectangle alloc = a.getBounds(); // makes a fresh rectangle instance
        
        setSize(alloc.width, alloc.height); // set new size
        
        return alloc;
    }

    // Implements FlyView.Parent
    public int getStartOffset(int childViewIndex) {
        return getChildren().getChildStartOffset(childViewIndex);
    }
    
    // Implements FlyView.Parent
    public int getEndOffset(int childViewIndex) {
        return getChildren().getChildEndOffset(childViewIndex);
    }
    
    public String childToString(int childIndex) {
        StringBuffer sb = new StringBuffer();
        appendChildToStringBuffer(sb, childIndex, 0);
        return sb.toString();
    }

    public void appendChildToStringBuffer(StringBuffer sb, int childIndex, int indent) {
        ViewLayoutState child = getChild(childIndex);
        View childView = child.getView();
        Document doc = getDocument();
        boolean isFly = child.isFlyweight();
        boolean isEstimated = (childView instanceof EstimatedSpanView)
            && ((EstimatedSpanView)childView).isEstimatedSpan();
        boolean layoutValid = child.isLayoutValid();
        double offset = children.getMajorAxisOffset(childIndex);
        boolean indexesDiffer = !isFly && (getChildIndexNoCheck(child) != childIndex);
        boolean showRaw = false; // change for debugging purposes

        sb.append((isFly ? 'F' : 'R')); // flyweight / regular NOI18N
        sb.append(':');
        if (indexesDiffer) {
            sb.append(" WRONG-INDEX=" + getChildIndexNoCheck(child)); // NOI18N
        }
        if (showRaw) {
            sb.append("rI=" + child.getViewRawIndex()); // NOI18N
        }
        sb.append('<');
        appendOffsetInfo(sb, doc, childView.getStartOffset());
        sb.append(',');
        appendOffsetInfo(sb, doc, childView.getEndOffset());
        sb.append('>');
            
        sb.append(", major=").append(child.getLayoutMajorAxisPreferredSpan()); // NOI18N
        sb.append("(off=").append(offset); // NOI18N

        if (showRaw) {
            sb.append('(').append(child.getLayoutMajorAxisRawOffset()).append(')'); // NOI18N
        }
        
        sb.append("), minor[pref=").append(child.getLayoutMinorAxisPreferredSpan()); // NOI18N
        sb.append(", min=").append(child.getLayoutMinorAxisMinimumSpan()); // NOI18N
        sb.append(", max=").append(child.getLayoutMinorAxisMaximumSpan()); // NOI18N
        sb.append("] "); // NOI18N
        sb.append(isEstimated ? "E" : ""); // NOI18N
        sb.append(layoutValid ? "" : "I"); // NOI18N
         
        // Possibly add view description if GapBoxView
        if (childView instanceof GapBoxView) {
            sb.append("\n"); // NOI18N
            appendSpaces(sb, indent + 4);
            sb.append("VIEW: "); // NOI18N
            sb.append(childView.toString());
            sb.append(((GapBoxView)childView).childrenToString(indent + 4));
        }
    }
    
    private static void appendOffsetInfo(StringBuffer sb, Document doc, int offset) {
        sb.append(offset);
        sb.append('[');
        // TODO - removed dependency on o.n.e.Utilities
        sb.append(org.netbeans.editor.Utilities.debugPosition(
            (org.netbeans.editor.BaseDocument)doc, offset));
        sb.append(']');
    }
    
    private static void appendSpaces(StringBuffer sb, int spaceCount) {
        while (--spaceCount >= 0) {
            sb.append(' ');
        }
    }

    public String childrenToString() {
        return childrenToString(0);
    }

    public String childrenToString(int indent) {
        StringBuffer sb = new StringBuffer();

        int viewCount = getViewCount();
        int totalDigitCount = Integer.toString(viewCount).length();
        for (int i = 0; i < viewCount; i++) {
            sb.append('\n');
            String iToString = Integer.toString(i);
            appendSpaces(sb, indent + (totalDigitCount - iToString.length()));

            sb.append('[');
            sb.append(iToString);
            sb.append("]: "); // NOI18N
            appendChildToStringBuffer(sb, i, indent);
        }

        return sb.toString();
    }

    public @Override String toString() {
        // Must not return anything about children because
        // that could cause them to be initialized (e.g. by getViewCount())
        return "lastMajorAxisPreferredSpan=" + lastMajorAxisPreferredSpan // NOI18N
            + ", lastMinorAxisPreferredSpan=" + lastMinorAxisPreferredSpan // NOI18N
            + ", minorAxisAssignedSpan=" + getMinorAxisAssignedSpan(); // NOI18N
    }

}
