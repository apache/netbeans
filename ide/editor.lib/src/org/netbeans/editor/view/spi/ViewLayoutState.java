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

package org.netbeans.editor.view.spi;

import javax.swing.text.View;

/**
 * Wrapper around a view that caches computed layout information
 * about preferred, minimum and maximum span along minor axis
 * and the preferred span along the major axis.
 * <br>
 * It's primarily intended to be used for views being children
 * of <code>GapBoxView</code> based implementations.
 * <br>
 * After a change occurs in the wrapped view it calls
 * <code>getParent().preferenceChanged(this, width, height)</code>
 * the parent view will notify the layout state by calling
 * {@link #viewPreferenceChanged(width, height)} to mark
 * that the cached values need to be updated.
 * <br>
 * At some point later the parent view calls
 * {@link #updateLayout()} to update the layout of the child
 * and synchronize the caching variables.
 *
 * <p>
 * The layout state keeps one of the axes (either <code>View.X_AXIS</code>
 * or <code>View.Y_AXIS</code> as the major one i.e. the children
 * are laid out along that axis. The other axis is called minor.
 * <br>
 * The layout along the major axis dedicates the span
 * equal to preferred span of the given view (wrapped by a layout state).
 * <br>
 * Span  along minor axis is more complex and it's specific
 * to particular implementations of {@link ViewLayoutState.Parent}.
 *
 * <p>
 * It's encouraged that all the view implementations
 * wishing to be used as children
 * of <code>GapBoxView</code> based implementations
 * in this framework implement this interface.
 * <br>
 * The benefit for the view implementing this interface
 * is that no default wrapper object implementing
 * this interface needs to be created which saves memory.
 * <br>
 * Moreover unlike the default wrapper
 * the view can better decide which values really need to be cached
 * and which (e.g. maximum or minimum span being equal to preferred span)
 * do not need to be cached
 * which can furhter eliminate certain caching variables
 * and save additional memory.
 * <br>
 * Another reason is faster finding of the index value of the child view
 * in its parent view (e.g. in preferenceChanged())
 * which is constant time compared to logartihmic time (binary search used).
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface ViewLayoutState {
    
    /**
     * Get the view that this layout state is wrapping.
     * @return view that this layout state is wrapping.
     */
    public View getView();
    
    /**
     * Check whether this layout state and its associated
     * view is flyweight (there is just one immutable shared instance of it).
     *
     * @return true if this layout state and its associated view
     *  are flyweight or false if not.
     */
    public boolean isFlyweight();
    
    /**
     * Get the raw index of the view (wrapped by this layout state)
     * in its parent view.
     * <br>
     * Storing of the index is useful for the parent which
     * can then find the child's index in a constant time.
     * <br>
     *
     * @return raw integer index that must be post-processed
     *   by parent (if it uses gap-based storage) to become a real index.
     *   <br>
     *   Returned valud is undefined for flyweight layout states.
     */
    public int getViewRawIndex();
    
    /**
     * Parent can set the index of the view (wrapped by this layout state)
     * in itself.
     * <br>
     * This method must not be called on flyweight layout states.
     *
     * @param viewRawIndex raw index value. It can differ from
     *  the real index because parent could preprocess
     *  it because of possible use of a gap translation.
     */
    public void setViewRawIndex(int viewRawIndex);

    /**
     * Select which axis will be used as major axis by this layout state.
     * @param majorAxis major axis either <code>View.X_AXIS</code>
     *  or <code>View.Y_AXIS</code>.
     * @return either this layout state if this state
     *  is able to work with the given major axis
     *  or another layout state if this state is only
     *  able to work over the other major axis.
     *
     *  <p>
     *  A view (e.g. a flyweight view)
     *  that implements this interface can decide to only allow
     *  one axis as the major one.
     *  <br>
     *  That should mostly be fine because usually the view is tiled
     *  only along one axis.
     *  <br>
     *  The view may throw <code>IllegalStateException</code>
     *  in such case.
     */
    public ViewLayoutState selectLayoutMajorAxis(int majorAxis);

    /**
     * Get the preferred span along major axis.
     * <br>
     * The value is expected to be cached for fast access.
     *
     * <p>
     * If the preferred span of the wrapped view changes
     * then the value of the major axis span must change as well
     * typically once the next layout state update takes place.
     * <br>
     * The wrapped view should call <code>View.preferenceChanged</code>
     * in its parent once its preferred span changes.
     * The parent view will later call
     * i.e. <code>updateLayout()</code> to update
     * the variable holding the preferred span along major axis.
     * <br>
     * <code>Parent.majorAxisPreferenceChanged()</code>
     * must be called immediately after that change
     * to notify the parent.
     *
     * <p>
     * Although the value is returned as double
     * (see {@link #getLayoutMajorAxisOffset()})
     * it can be maintained as float if the resolution of the float
     * is sufficient to create proper deltas
     * in <code>Parent#majorAxisPreferenceChanged()</code>.
     * <br>
     * For example if a document has millions of lines it's necessary
     * to maintain line offsets in document view as doubles
     * but assuming that each line is only up to several tenths of pixels high
     * it's enough to hold line height as float in line view layout state.
     */
    public double getLayoutMajorAxisPreferredSpan();

    /**
     * Get the raw visual offset of the view along the parent view's
     * major axis.
     *
     * <p>
     * Double is chosen instead of float because for y as major axis
     * the truncation could occur when computing the offset
     * for large files with many lines or for very long lines.
     * <br>
     * The resolution of mantissa of floats is 23 bits
     * so assuming the line height is e.g. 17 pixels
     * and we have more than 250.000 lines in the docuemnt
     * (which is a lot to write but not so much for e.g.
     * generated xml files) the last bit would be lost
     * resulting in every odd line being shifted one
     * pixel above incorrectly.
     * <br>
     * The views can still decide to use floats for internal storage
     * of this value if the precision is sufficient.
     *
     * @return raw double visual offset along major axis. It must be post-processed
     *   by parent (if it uses gap-based storage) to become a real index.
     *   <br>
     *   Returned valud is undefined for flyweight layout states.
     */
    public double getLayoutMajorAxisRawOffset();
    
    /**
     * Parent can set the view's raw offset along the parent view's
     * major axis using this method.
     * <br>
     * This method must not be called on flyweight layout states.
     *
     * @param layoutMajorAxisRawOffset raw offset value along the major axis.
     *  It is not particularly useful without postprocessing by the parent.
     */
    public void setLayoutMajorAxisRawOffset(double layoutMajorAxisRawOffset);
    
    /**
     * Get the preferred span of the view along minor axis.
     * <br>
     * The value is expected to be cached for fast access.
     *
     * <p>
     * If there is a dedicated variable for this value then
     * that variable should be updated during {@link #updateLayout()}
     * which usually happens some time after the view
     * has called <code>View.preferenceChanged()</code>
     * in the parent.
     * <br>
     * After the value gets updated the layout state must immediately call
     * <code>Parent.minorAxisPreferenceChanged()</code>
     * to notify the parent about the change of minor span.
     */
    public float getLayoutMinorAxisPreferredSpan();

    /**
     * Get the minimum span of the view along minor axis.
     * <br>
     * The value is expected to be cached for fast access.
     * <br>
     * By default <code>GapBoxView</code> implementations
     * do not use the minimum span of their children
     * so the particular layout state may decide not to cache
     * the minimum span value and return preferred span instead
     * to save memory that would otherwise be used for caching variables.
     *
     * <p>
     * If there is a dedicated variable for this value then
     * that variable should be updated during {@link #updateLayout()}
     * which usually happens once the view
     * has called <code>View.preferenceChanged()</code>
     * in the parent.
     * <br>
     * After the value gets updated the layout state must immediately call
     * <code>Parent.minorAxisPreferenceChanged()</code>
     * to notify the parent about the change of minor span.
     */
    public float getLayoutMinorAxisMinimumSpan();

    /**
     * Get the maximum span of the view along minor axis.
     * <br>
     * The value is expected to be cached for fast access.
     * <br>
     * As the default <code>GapBoxView</code> implementations
     * do not use the maximum span of their children
     * the particular layout state may decide not to cache
     * the maximum span value and return preferred span instead
     * to save memory that would otherwise be used for caching variables.
     *
     * <p>
     * If there is a dedicated variable for this value then
     * that variable gets updated by {@link #updateLayout()}
     * which usually happens once the view
     * has called <code>View.preferenceChanged()</code>
     * in the parent.
     * <br>
     * After the value gets updated the layout state must immediately call
     * <code>Parent.minorAxisPreferenceChanged()</code>
     * to notify the parent about the change of minor span.
     */
    public float getLayoutMinorAxisMaximumSpan();
    
    /**
     * Get alignment along the minor axis.
     * <br>
     * The value is expected to be cached for fast access.
     *
     * <p>
     * If there is a dedicated variable for this value then
     * that variable gets updated by {@link #updateLayout()}
     * which usually happens once the view
     * has called <code>View.preferenceChanged()</code>
     * in its parent view which in turn calls {@link #viewPreferenceChanged()}.
     * <br>
     * After the value gets updated the layout state must call
     * <code>Parent.minorAxisPreferenceChanged()</code>
     * to notify the parent about the change of minor span.
     */
    public float getLayoutMinorAxisAlignment();

    /**
     * Do actual layout updating.
     * <br>
     * The first thing to do
     * is to see if any work actually needs to be done.
     * <br>
     * Certain views such as line-wrapping views may react
     * to <code>View.setSize()</code> being called on them
     * by changing its preferred span along an axis.
     * For such views the actual internal updating must be done twice
     * (because of another call to <code>parent.preferenceChanged()</code>).
     * <br>
     * Anyway upon exit of this method the {@link #isLayoutValid()}
     * must return true.
     * <br>
     * This method must be no-op for flyweight layout states
     * because they should have their values up-to-date since
     * their construction without ever calling this method.
     * <br>
     * The view is responsible for repainting itself if that's necessary.
     * 
     * Prior to asking the component to repaint the appropriate region
     */
    public void updateLayout();

    /**
     * Notify this layout state that the preferences has changed
     * for the view that it wraps.
     * <br>
     * This gets called in response to the wrapped view calling
     * <code>View.preferenceChanged()</code> in its parent.
     * <br>
     * Usually this method only makes sense when the layout state
     * is a generic wrapper around a black-box view. If layout state
     * and the view are one object then this method is usually no-op.
     * <br>
     * The layout state should just mark its internal state as changed
     * but wait for layout update (that will be called by parent)
     * to update the layout variables.
     * <br>
     * This method must be no-op for flyweight layout states.
     *
     * @param width true if the width preference has changed
     * @param height true if the height preference has changed
     */
    public void viewPreferenceChanged(boolean width, boolean height);
    
    /**
     * Parent calls this method to mark the current size of the view as invalid
     * so that the next layout update of this layout state
     * will call <code>View.setSize()</code>
     * using {@link #getLayoutMajorAxisPreferredSpan()} for major axis span
     * and {@link ViewLayoutState.Parent#getMinorAxisSpan(ViewLayoutState)}
     * for minor axis span.
     * <br>
     * Parent is responsible for scheduling of layout update for the child
     * after calling this method.
     * <br>
     * This method must be no-op for flyweight layout states.
     */
    public void markViewSizeInvalid();
    
    /**
     * Check whether there are any layout duties present.
     *
     * @return true if there are no layout duties or false
     *  if the layout needs to be udpated.
     *  <br>
     *  Flyweight layout states must always return <code>true</code> here.
     */
    public boolean isLayoutValid();

    /**
     * Interface that the parent view of the view
     * wrapped by <code>ViewLayoutState</code>
     * is expected to implement.
     */
    public interface Parent {
        
        /**
         * By using this method a child layout state notifies its parent that
         * its requirement for span along the major axis has changed
         * against the previous value.
         * <br>
         * It can be done anytime but typically during
         * {@link #updateLayout()} execution.
         * <br>
         * The child will be automatically scheduled for repaint (together
         * with all children that follow it in the parent view) after
         * this method gets called.
         *
         * @param child child layout state which preference has changed.
         * @param majorAxisSpanDelta delta between the new span and the original span.
         */
        public void majorAxisPreferenceChanged(ViewLayoutState child,
        double majorAxisSpanDelta);
        
        /**
         * By using this method a child layout state notifies its parent that
         * either preferred, minimum or maximum spans or aligment
         * along the minor axis has changed against their previous values.
         * <br>
         * It can be done anytime but typically during
         * {@link #updateLayout()} execution.
         *
         * @param child child layout state which preference has changed.
         */
        public void minorAxisPreferenceChanged(ViewLayoutState child);
        
        /**
         * Notify this view that layout of the particular child
         * has become invalid and needs to be updated.
         * <br>
         * The parent can update the child's layout either immediately
         * or later.
         */
        public void layoutInvalid(ViewLayoutState child);
        
        /**
         * Get span of the given child along the minor axis.
         * <br>
         * Parent computes the span according to its own layout policy
         * for the children.
         * <br>
         * This method is typically called from the layout state's methods
         * and is thus useful for non-flyweight layout states only
         * as flyweights do not maintain the parent.
         *
         * @param child child layout state for which the span is being
         *  determined.
         */
        public float getMinorAxisSpan(ViewLayoutState child);

        /**
         * Inform the parent that the child layout state needs a repaint.
         * <br>
         * This method can be called anytime although usually
         * it's called during the layout state's <code>updateLayout()</code>.
         * <br>
         * This method can be called repetively. The lowest offsets
         * should finally be used by the parent.
         *
         * @param child child that needs its area to be repainted.
         * @param majorAxisOffset offset along the major axis defining
         *  the begining of the repaint region. If the allocation
         *  has changed along the major axis the view is fully repainted
         *  (see <code>majorAxisPreferenceChanged()</code>).
         *  <br>
         *  This parameter is typically zero but can be used
         *  e.g. for line-wrapping views when typing on the last line.
         * @param minorAxisOffset offset along the minor axis
         *  defining the begining of the repaint region.
         * @param majorAxisSpan span along the major axis
         *  that should be repainted. If it is set to zero then
         *  it means that the end of the repaint region along the major axis
         *  span is determined by the span allocated for the child
         *  in this parent.
         * @param minorAxisSpan span along the minor axis
         *  that should be repainted. If it is set to zero then
         *  it means that the end of the repaint region along the minor axis
         *  span is determined by the span of this parent.
         */
        public void repaint(ViewLayoutState child,
        double majorAxisOffset, double majorAxisSpan,
        float minorAxisOffset, float minorAxisSpan);

    }

}
