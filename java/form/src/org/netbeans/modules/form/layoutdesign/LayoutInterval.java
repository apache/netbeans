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

package org.netbeans.modules.form.layoutdesign;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Tomas Pavek
 */
public final class LayoutInterval implements LayoutConstants {

    /** Marker attribute for a gap that can be adjusted when resizing whole
     * container. */
    static final int ATTR_DESIGN_CONTAINER_GAP = 4;
    
    /** Marker attribute for a parallel group with significant perceived
     * boundaries on both sides (i.e. not visually open for extending). */
    static final int ATTR_CLOSED_GROUP = 32;

    // attributes used during aligning
    static final int ATTR_ALIGN_PRE = 64;
    static final int ATTR_ALIGN_POST = 128;

    /** Indicates that the current size of the interval differs from the defined
     * preferred size (either default or explicit). It means that the actual
     * size of the interval is determined by something else. Used for resizing
     * gaps and components. */
    static final int ATTR_SIZE_DIFF = 512;

    /** Marks resizing intervals that are considered flexible as of their actual
     * defined size - the system can change freely between explicit size, or
     * default, based on other intervals in the layout. Without the attribute
     * the size definition is preserved (i.e. explicit vs default). */
    static final int ATTR_FLEX_SIZEDEF = 1024;

    /** Marks a root that passed an optimization routine. If not set on a root it
     * indicates that the root needs to undergo a structure optimization. */
    static final int ATTR_OPTIMIZED = 2048;

    /** Attributes tied to actual state of layout or operation, need to be
     * refreshed regularly. */
    static final int REFRESHING_ATTRS = ATTR_DESIGN_CONTAINER_GAP
                                        | ATTR_ALIGN_PRE
                                        | ATTR_ALIGN_POST
                                        | ATTR_SIZE_DIFF;

    // values 1 and 2 were used in the past (FILL, FORMER_FILL), should be saved
    // if loaded, should not be used for anything else now
    static final int ATTR_PERSISTENT_MASK = 3 | ATTR_CLOSED_GROUP;

    // type of the interval - SINGLE, SEQUENTIAL, PARALLEL
    private int type;

    // additional attributes set on the interval as bit flags
    private int attributes;

    // alignment of the interval (if in a parallel group)
    private int alignment = DEFAULT;

    // parent interval (group )
    private LayoutInterval parentInterval;

    // internall alignment of a group (if this is a parallel group)
    private int groupAlignment = LEADING;

    // contained sub-intervals (if this is a group)
    private List<LayoutInterval> subIntervals;

    // associated LayoutComponent (if any)
    private LayoutComponent layoutComponent;

    // type of padding (default gap; if this is a preferred gap)
    private PaddingType paddingType;
    private String[] paddingDefComps; // 2 components, needed for INDENT gap

    // minimum, preferred, and maximum size definitions
    private int minSize;
    private int prefSize;
    private int maxSize;

    // current position and size of the interval in the visual representation
    private LayoutRegion currentSpace;

    private int lastActualSize = Integer.MAX_VALUE; // by default do not track actual size

    private int diffToDefaultSize;

    // -----
    // setup methods - each setter should be called max. once after creation,
    // other changes should be done via LayoutModel to be fired and recorded
    // for undo/redo

    LayoutInterval(int type) {
        this.type = type;
        minSize = NOT_EXPLICITLY_DEFINED;
        prefSize = NOT_EXPLICITLY_DEFINED;
        if (type == SEQUENTIAL || type == PARALLEL) {
            subIntervals = new ArrayList<LayoutInterval>();
            maxSize = NOT_EXPLICITLY_DEFINED; // group can resize by default
        }
        else {
            assert type == SINGLE;
            maxSize = USE_PREFERRED_SIZE;
        }
    }

    void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    void setGroupAlignment(int alignment) {
        assert alignment != DEFAULT && type == PARALLEL;
        groupAlignment = alignment;
    }

    void setComponent(LayoutComponent comp) {
        this.layoutComponent = comp;
    }

    void setMinimumSize(int size) {
        // for groups we expect only two states - shrinking suppressed/allowed
        assert isSingle() || (size == USE_PREFERRED_SIZE || size == NOT_EXPLICITLY_DEFINED);
        minSize = size;
    }

    void setPreferredSize(int size) {
        assert (size != USE_PREFERRED_SIZE && isSingle()) || (size == NOT_EXPLICITLY_DEFINED); // groups should not have explicit size
        prefSize = size;
    }

    void setMaximumSize(int size) {
        // Maximum size is only expected to have two states defining resizability.
        // For single intervals it should be USE_PREFERRED_SIZE (don't resize)
        // or Short.MAX_VALUE (want resize).
        // For groups it should be USE_PREFERRED_SIZE (suppressed resizing on
        // the group regardless what the content wants) or NOT_EXPLICITLY_DEFINED
        // (can resize, derived from the content of the group).
        // Not all asserted, there may be some weird combinations in old forms.
        assert (isSingle() && size != NOT_EXPLICITLY_DEFINED)
               || (isGroup() && (size == USE_PREFERRED_SIZE || size == NOT_EXPLICITLY_DEFINED));
        if (size != maxSize && size == Short.MAX_VALUE) {
            lastActualSize = Integer.MAX_VALUE;
        }
        maxSize = size;
    }

    void setSize(int size) {
        setMinimumSize(size);
        setPreferredSize(size);
        setMaximumSize(USE_PREFERRED_SIZE);
    }

    void setSizes(int min, int pref, int max) {
        setMinimumSize(min);
        setPreferredSize(pref);
        setMaximumSize(max);
    }

    /**
     * Returns the minimum size of the interval. Instead of a specific size
     * it may also return one of the constants NOT_EXPLICITLY_DEFINED or
     * USE_PREFERRED_SIZE.
     * @return minimum interval size, or one of the constants:
     *         NOT_EXPLICITLY_DEFINED or USE_PREFERRED_SIZE
     */
    public int getMinimumSize() {
        return minSize;
    }

    /**
     * Returns the preferred size of the interval. If no specific size was set,
     * it returns NOT_EXPLICITLY_DEFINED constant.
     * @return preferred size of the interval, or NOT_EXPLICITLY_DEFINED constant
     */
    public int getPreferredSize() {
        return prefSize;
    }

    /**
     * Returns the maximum size of the interval. Instead of a specific size
     * it may return also one of the constants NOT_EXPLICITLY_DEFINED or
     * USE_PREFERRED_SIZE.
     * @return maximum interval size, or one of the constants:
     *         NOT_EXPLICITLY_DEFINED or USE_PREFERRED_SIZE
     */
    public int getMaximumSize() {
        return maxSize;
    }

    void setPaddingType(PaddingType type) {
        paddingType = type;
    }

    String[] getPaddingDefComponents() {
        return paddingDefComps;
    }

    void setPaddingDefComponents(String compId1, String compId2) {
        if (compId1 == null) {
            paddingDefComps = null;
        } else {
            paddingDefComps = new String[] { compId1, compId2 };
        }
    }

    // ---------
    // public methods

    /**
     * Returns the type of the structure of the interval. It can be a single
     * interval or a group with sub-intervals arranged either sequentially
     * or parallelly.
     * @return type of the interval: SINGLE, SEQUENTIAL, or PARALLEL
     */
    public int getType() {
        return type;
    }

    /**
     * Returns alignment of the interval within a parallel group. If the
     * interval is not part of a parallel group, the alignment is meaningless.
     * @return alignment of the interval within a parallel group (LEADING,
     *         TRAILING, CENTER, or BASELINE); DEFAULT if in a sequential group
     */
    public int getAlignment() {
        return alignment == DEFAULT && parentInterval != null
                                    && parentInterval.isParallel() ?
            parentInterval.getGroupAlignment() : alignment;
    }

    /**
     * Returns the common alignment of sub-intervals within a group (makes
     * sense only for a parallel group).
     * @return alignment of the group (LEADING, TRAILING, CENTER, or BASELINE)
     */
    public int getGroupAlignment() {
        return groupAlignment;
    }

    /**
     * Returns number of sub-intervals of this interval.
     * @return number of sub-intervals of this interval, 0 if it is not a group
     */
    public int getSubIntervalCount() {
        return subIntervals != null ? subIntervals.size() : 0;
    }

    /**
     * Returns an iterator of sub-intervals.
     * @return iterator of sub-intervals, empty if there are no sub-intervals
     */
    public Iterator<LayoutInterval> getSubIntervals() {
        return subIntervals != null ? subIntervals.iterator() :
                                      Collections.EMPTY_LIST.iterator();
    }

    /**
     * If this interval represents a component's width or height, this methods
     * returns the component.
     * @return LayoutComponent instance representing the associated component.
     *         Null if this interval does not represent a component.
     */
    public LayoutComponent getComponent() {
        return layoutComponent;
    }

    // helper methods (redundant - based on derived information)

    public boolean isParallel() {
        return type == PARALLEL;
    }

    public boolean isSequential() {
        return type == SEQUENTIAL;
    }

    /**
     * Returns whether this interval defines a lyout component.
     * @return true if this interval represents a layout component,
     *         false otherwise
     */
    public boolean isComponent() {
        return layoutComponent != null;
    }

    /**
     * Returns whether this interval defines an "empty" space (gap) in the
     * layout, not including nor being able to include any component.
     * @return true if this is a single interval not representing a component,
     *         false otherwise
     */
    public boolean isEmptySpace() {
        return type == SINGLE && layoutComponent == null;
    }

    public boolean isDefaultPadding() {
        return isEmptySpace() && (getMinimumSize() == NOT_EXPLICITLY_DEFINED
                                  || getPreferredSize() == NOT_EXPLICITLY_DEFINED);
    }

    public PaddingType getPaddingType() {
        return paddingType;
    }

    public boolean isSingle() {
        return type == SINGLE;
    }

    /**
     * Returns whether this interval represents a group structure that can have
     * have sub-intervals.
     * @return whether this interval is a group, either sequential or parallel
     */
    public boolean isGroup() {
        return type == SEQUENTIAL || type == PARALLEL;
    }

    /**
     * @return whether the interval is allowed to grow (according to its
     *         definition); if allowed, the real growing possibility may still
     *         depend on the associated component
     */
//    public boolean isAllowedToGrow() {
//        return maxSize != USE_PREFERRED_SIZE
//               && (prefSize == NOT_EXPLICITLY_DEFINED
//                   || maxSize == NOT_EXPLICITLY_DEFINED
//                   || maxSize > prefSize);
//    }

    /**
     * @return whether the interval is allowed to shrink (according to its
     *         definition); if allowed, the real growing possibility may still
     *         depend on the associated component
     */
//    public boolean isAllowedToShrink() {
//        return minSize != USE_PREFERRED_SIZE
//               && (prefSize == NOT_EXPLICITLY_DEFINED
//                   || minSize == NOT_EXPLICITLY_DEFINED
//                   || minSize < prefSize);
//    }

    // end of public methods
    // -----

    boolean hasAttribute(int attr) {
        return (attributes & attr) == attr;
    }

    void setAttribute(int attr) {
        attributes |= attr;
    }

    void unsetAttribute(int attr) {
        attributes &= ~attr;
    }

    /**
     * Sets attributes of the layout interval. Should be used by persistence manager only!
     *
     * @param attrs attributes.
     */
    void setAttributes(int attrs) {
        attributes = attrs;
    }
    
    /**
     * Returns attributes of this layout interval. You should use
     * <code>hasAttribute()</code> when you are interested in one
     * particular attribute.
     */
    int getAttributes() {
        return attributes;
    }

    /**
     * @return the value of the alignment field of the interval - unlike
     *         getAlignment() it does not ask the parent if not set (DEFAULT)
     */
    int getRawAlignment() {
        return alignment;
    }

    // -----

    public LayoutInterval getParent() {
        return parentInterval;
    }

    int add(LayoutInterval interval, int index) {
        if (interval == null) {
            throw new NullPointerException();
        }
        if (getParent() == interval) {
            throw new IllegalArgumentException("Cannot add parent as a sub-interval!"); // NOI18N
        }
        if (index < 0) {
            index = subIntervals.size();
        }
        subIntervals.add(index, interval);
        interval.parentInterval = this;
        return index;
    }

    int remove(LayoutInterval interval) {
        int index = subIntervals.indexOf(interval);
        if (index >= 0) {
            subIntervals.remove(index);
            interval.parentInterval = null;
            if (interval.isGroup()) {
                addRemovedIntervalStacktrace(interval);
            }
        }
        return index;
    }

    LayoutInterval remove(int index) {
        LayoutInterval interval = subIntervals.get(index);
        subIntervals.remove(index);
        interval.parentInterval = null;
        if (interval.isGroup()) {
            addRemovedIntervalStacktrace(interval);
        }
        return interval;
    }

    public LayoutInterval getSubInterval(int index) {
        return subIntervals != null ? subIntervals.get(index) : null;
    }

    int indexOf(LayoutInterval interval) {
        return subIntervals != null ? subIntervals.indexOf(interval) : -1;
    }

    public boolean isParentOf(LayoutInterval interval) {
        if (isGroup()) {
            do {
                interval = interval.getParent();
                if (interval == this)
                    return true;
            }
            while (interval != null);
        }
        return false;
    }

    public LayoutInterval getRoot() {
        return LayoutInterval.getRoot(this);
    }

    // -----
    // current state of the layout - current position and size of layout
    // interval kept to be available quickly for the layout designer

    LayoutRegion getCurrentSpace() {
        assert !isEmptySpace(); // nobody should be interested in gap positions directly
        if (currentSpace == null) {
            currentSpace = new LayoutRegion();
        }
        return currentSpace;
    }

    void setCurrentSpace(LayoutRegion space) {
        currentSpace = space;
    }

    int getDiffToDefaultSize() {
        return diffToDefaultSize;
    }

    void setDiffToDefaultSize(int diff) {
        diffToDefaultSize = diff;
    }

    /**
     * For special purpose, to determine when to change pref. size of resizing
     * intervals, not guaranteed to return some real size.
     * @return the remembered size value
     */
    int getLastActualSize() {
        return lastActualSize;
    }

    /**
     * For special purpose, to determine when to change pref. size of resizing
     * intervals.
     * @param size the size to remember
     */
    void setLastActualSize(int size) {
        lastActualSize = size;
    }

    // -----
    // static helper methods

    /**
     * @return the closest parent interval that matches given type
     */
    static LayoutInterval getFirstParent(LayoutInterval interval, int type) {
        LayoutInterval parent = interval.getParent();
        while (parent != null && parent.getType() != type) {
            parent = parent.getParent();
        }
        return parent;
    }

    static LayoutInterval getRoot(LayoutInterval interval) {
        while (interval.getParent() != null) {
            interval = interval.getParent();
        }
//        assert interval.isParallel();
        return interval;
    }

    static LayoutInterval getRoot(LayoutInterval interval, int type) {
        LayoutInterval root = null;
        do {
            if (interval.getType() == type) {
                root = interval;
            }
            interval = interval.getParent();
        } while (interval != null);
        return root;
    }

    /**
     * Finds common parent of the given intervals.
     *
     * @param intervals intervals whose parent should be found.
     * @return common parent of the given intervals.
     */
    static LayoutInterval getCommonParent(LayoutInterval[] intervals) {
        LayoutInterval parent = intervals[0].getParent();
        if (parent != null) {
            for (int i=1; i<intervals.length; i++) {
                parent = getCommonParent(parent, intervals[i]);
            }
        }
        return parent;
    }

    /**
     * Finds common parent of two given intervals. In case one interval is
     * parent of the other then this interval is returned directly, not its
     * parent.
     *
     * @param interval1 interval whose parent should be found.
     * @param interval2 interval whose parent should be found.
     * @return common parent of two given intervals.
     */
    static LayoutInterval getCommonParent(LayoutInterval interval1, LayoutInterval interval2) {
        // Find all parents of given intervals
        Iterator parents1 = parentsOfInterval(interval1).iterator();
        Iterator parents2 = parentsOfInterval(interval2).iterator();
        LayoutInterval parent1 = (LayoutInterval)parents1.next();
        LayoutInterval parent2 = (LayoutInterval)parents2.next();
        assert (parent1 == parent2);
        
        // Candidate for the common parent
        LayoutInterval parent = null;
        while (parent1 == parent2) {
            parent = parent1;
            if (parents1.hasNext()) {
                parent1 = (LayoutInterval)parents1.next();
            } else {
                break;
            }
            if (parents2.hasNext()) {
                parent2 = (LayoutInterval)parents2.next();
            } else {
                break;
            }
        }
        return parent;
    }

    /**
     * Calculates all parents of the given interval.
     *
     * @param interval interval whose parents should be found.
     * @return <code>List</code> of <code>LayoutInterval</code> objects that
     * are parents of the given interval. The root is the first in the list;
     * the interval itelf is also included - at the end.
     */
    private static List<LayoutInterval> parentsOfInterval(LayoutInterval interval) {
        List<LayoutInterval> parents = new LinkedList<LayoutInterval>();
        while (interval != null) {
            parents.add(0, interval);
            interval = interval.getParent();
        }
        return parents;
    }

    static int getCount(LayoutInterval group, int alignment, boolean nonEmpty) {
        int n = 0;
        Iterator it = group.getSubIntervals();
        while (it.hasNext()) {
            LayoutInterval li = (LayoutInterval) it.next();
            if ((group.isSequential()
                 || alignment == LayoutRegion.ALL_POINTS
                 || li.getAlignment() == alignment
                 || wantResize(li))
                && (!nonEmpty || !li.isEmptySpace()))
            {   // count in
                n++;
            }
        }
        return n;
    }

    static LayoutInterval getDirectNeighbor(LayoutInterval interval, int alignment, boolean nonEmpty) {
        LayoutInterval parent = interval.getParent();
        if (parent == null || parent.isParallel())
            return null;

        LayoutInterval neighbor = null;
        int d = (alignment == LEADING ? -1 : 1);
        int n = parent.getSubIntervalCount();
        int index = parent.indexOf(interval) + d;
        while (index >= 0 && index < n && neighbor == null) {
            LayoutInterval li = parent.getSubInterval(index);
            index += d;
            if (!nonEmpty || !li.isEmptySpace()) {
                neighbor = li;
            }
        }
        return neighbor;
    }

    /**
     * Get a sequential neighbor for given interval in given direction.
     * @param alignment direction in which the neighbor is looked for (LEADING or TRAILING)
     * @param nonEmpty true if empty spaces (gaps) should be skipped
     * @param outOfParent true if can go up (out of the first sequential parent)
     *                         for an indirect neighbor
     * @param aligned true if the indirect neighbor must be in contact with the
     *                     given interval
     */
    public static LayoutInterval getNeighbor(LayoutInterval interval,
                                             int alignment,
                                             boolean nonEmpty,
                                             boolean outOfParent,
                                             boolean aligned)
    {
        assert alignment == LEADING || alignment == TRAILING;

        LayoutInterval neighbor = null;
        LayoutInterval parent = interval;
        int d = (alignment == LEADING ? -1 : 1);

        do {
            do { // find sequential parent first
                interval = parent;
                parent = interval.getParent();
                if (aligned && parent != null && parent.isParallel()
                    && !isAlignedAtBorder(interval, alignment))
                {   // interval not aligned in parent
                    parent = null;
                }
            }
            while (parent != null && parent.isParallel());

            if (parent != null) { // look for the neighbor in the sequence
                neighbor = getDirectNeighbor(interval, alignment, nonEmpty);
            }
        }
        while (neighbor == null && parent != null && outOfParent);

        return neighbor;
    }

    static boolean startsWithEmptySpace(LayoutInterval interval, int alignment) {
        assert alignment == LEADING || alignment == TRAILING;
        if (interval.isSingle()) {
            return interval.isEmptySpace();
        }
        if (interval.isSequential()) {
            int index = alignment == LEADING ? 0 : interval.getSubIntervalCount()-1;
            return startsWithEmptySpace(interval.getSubInterval(index), alignment);
        }
        else { // parallel group
            for (Iterator it=interval.getSubIntervals(); it.hasNext(); ) {
                LayoutInterval li = (LayoutInterval) it.next();
                if (startsWithEmptySpace(li, alignment)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks whether an interval is permanently aligned to its parent at given
     * border. (The asked relation is hard, always maintained by the layout.)
     * For a sequential parent the interval is aligned if it is first or last.
     * For parallel parent the interval must have the given alignment in the
     * group, or be resizing.
     */
    public static boolean isAlignedAtBorder(LayoutInterval interval, int alignment) {
        if (alignment != LEADING && alignment != TRAILING) {
            return false;
        }
        LayoutInterval parent = interval.getParent();
        if (parent == null) {
            return false;
        }
        if (parent.isSequential()) {
            int index = alignment == LEADING ? 0 : parent.getSubIntervalCount()-1;
            return interval == parent.getSubInterval(index);
        }
        else { // parallel parent
            return interval.getAlignment() == alignment
                   || wantResize(interval);
        }
    }

    /**
     * Checks whether an interval is permanently aligned with a given parent
     * interval - need not be the direct parent. This is a multi-level version
     * of the other (simple) isAlignedAtBorder method.
     */
    static boolean isAlignedAtBorder(LayoutInterval interval, LayoutInterval parent, int alignment) {
        do {
            if (!isAlignedAtBorder(interval, alignment)) {
                return false;
            }
            interval = interval.getParent();
        }
        while (interval != parent);
        return true;
    }

    /**
     * Checks whether given interval is placed at border side of its parent.
     * Cares about the current visual situation only - the place may change if
     * the alignment is not backed by the layout structure.
     * Note this method requires the current visual state (positions) of the
     * relevant intervals to be up-to-date.
     */
    static boolean isPlacedAtBorder(LayoutInterval interval, int dimension, int alignment) {
        if (alignment != LEADING && alignment != TRAILING) {
            return false;
        }
        LayoutInterval parent = interval.getParent();
        if (parent == null) {
            return false;
        }
        if (interval.isEmptySpace()) {
            if (parent.isSequential()) {
                int index = alignment == LEADING ? 0 : parent.getSubIntervalCount()-1;
                return interval == parent.getSubInterval(index);
            }
            else { // gap in parallel parent
                return true;
            }
        }
        else { // check visual position
           return LayoutRegion.distance(interval.getCurrentSpace(), parent.getCurrentSpace(),
                                        dimension, alignment, alignment) == 0;
        }
    }

    /**
     * Checks whether an interval is placed at border side of given parent
     * (need not be the direct parent). This is a multi-level version of the
     * simpler isPlacededAtBorder method.
     * Note this method requires the current visual state (positions) of the
     * relevant intervals to be up-to-date.
     */
    static boolean isPlacedAtBorder(LayoutInterval interval, LayoutInterval parent, int dimension, int alignment) {
        if (alignment != LEADING && alignment != TRAILING) {
            return false;
        }
        if (interval.isEmptySpace()) {
            LayoutInterval p = interval.getParent();
            if (p.isSequential()) {
                int index = alignment == LEADING ? 0 : p.getSubIntervalCount()-1;
                if (interval != p.getSubInterval(index)) {
                    return false;
                }
            }
            if (p == parent) {
                return true;
            }
            interval = p;
        }
        return LayoutRegion.distance(interval.getCurrentSpace(), parent.getCurrentSpace(),
                                     dimension, alignment, alignment) == 0
               && parent.isParentOf(interval);
    }

    // [to be replaced by separate methods like isAlignedAtBorder, isPlacedBorder, isLastInterval]
    static boolean isBorderInterval(LayoutInterval interval, int alignment, boolean attached) {
        LayoutInterval parent = interval.getParent();
        if (parent != null && (alignment == LEADING || alignment == TRAILING)) {
            if (parent.isSequential()) {
                int index = alignment == LEADING ? 0 : parent.getSubIntervalCount()-1;
                while (index >= 0 && index < parent.getSubIntervalCount()) {
                    LayoutInterval li = parent.getSubInterval(index);
                    if (li == interval) {
                        return true;
                    }
                    else if (attached || !li.isEmptySpace()) {
                        return false;
                    }
                    index += alignment == LEADING ? 1 : -1;
                }
            }
            else {
                return !attached
                       || interval.getAlignment() == alignment
                       || wantResize(interval);
            }
//                if (interval.getAlignment() == alignment) {
//                return interval.getCurrentSpace().positions[dimension][alignment]
//                       == parent.getCurrentSpace().positions[dimension][alignment];
        }
        return false;
    }

    static boolean isClosedGroup(LayoutInterval group, int alignment) {
        assert group.isParallel();

        if (group.hasAttribute(ATTR_CLOSED_GROUP)
            || group.getGroupAlignment() == CENTER
            || group.getGroupAlignment() == BASELINE)
        {
            return true;
        }

        if (!canResize(group)) {
            return true;
        }

        Iterator it = group.getSubIntervals();
        while (it.hasNext()) {
            LayoutInterval li = (LayoutInterval) it.next();
            if (li.getAlignment() == alignment || wantResize(li)) {
                return true;
            }
        }
        return false;
    }

    static boolean isExplicitlyClosedGroup(LayoutInterval group) {
        return group.hasAttribute(ATTR_CLOSED_GROUP);
    }

    static boolean isDefaultPadding(LayoutInterval interval) {
        return interval.isEmptySpace() && (interval.getMinimumSize() == NOT_EXPLICITLY_DEFINED
                                           || interval.getPreferredSize() == NOT_EXPLICITLY_DEFINED);
    }

    static boolean isFixedDefaultPadding(LayoutInterval interval) {
        return interval.isEmptySpace()
               && (interval.getMinimumSize() == NOT_EXPLICITLY_DEFINED || interval.getMinimumSize() == USE_PREFERRED_SIZE)
               && interval.getPreferredSize() == NOT_EXPLICITLY_DEFINED
               && (interval.getMaximumSize() == NOT_EXPLICITLY_DEFINED || interval.getMaximumSize() == USE_PREFERRED_SIZE);
    }

    /**
     * @return whether given interval is allowed to resize (not defined as fixed)
     */
    public static boolean canResize(LayoutInterval interval) {
        // [don't care about shrinking, assuming min possibly not defined - is it ok?]
        int max = interval.getMaximumSize();
        int pref = interval.getPreferredSize();
        assert interval.isGroup() || max != NOT_EXPLICITLY_DEFINED;
        if ((max != pref && max != USE_PREFERRED_SIZE) || max == NOT_EXPLICITLY_DEFINED) {
            if (interval.isComponent()) {
                LayoutComponent comp = interval.getComponent();
                int dimension = comp.getLayoutInterval(HORIZONTAL) == interval ? HORIZONTAL : VERTICAL;
                if (comp.isLinkSized(dimension)) {
                    return false; // components with linked size actually can't resize
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Finds out whether given interval would resize if allowed (given more
     * space by its parent).
     * @return whether given interval would resize if given opportunity
     */
    public static boolean wantResize(LayoutInterval interval) {
        return canResize(interval)
               && (!interval.isGroup() || contentWantResize(interval));
    }

    /**
     * Finds out whether given interval would resize if allowed (given more
     * space by its parent). This method also considers resizing of the whole
     * layout (some parent of the interval could block the resizing).
     * @return whether given interval would resize if given opportunity
     */
    static boolean wantResizeInLayout(LayoutInterval interval) {
        return wantResizeInParent(interval, null);
    }

    static boolean wantResizeInParent(LayoutInterval interval, LayoutInterval parent) {
        return canResizeInParent(interval, parent) && wantResize(interval);
    }

    static boolean canResizeInLayout(LayoutInterval interval) {
        return canResizeInParent(interval, null);
    }

    static boolean canResizeInParent(LayoutInterval interval, LayoutInterval parent) {
        if (!canResize(interval)) {
            return false;
        }
        while (interval.getParent() != parent) {
            interval = interval.getParent();
            if (!canResize(interval)) {
                return false;
            }
        }
        return true;
    }

    static boolean contentWantResize(LayoutInterval group) {
        boolean subres = false;
        Iterator it = group.getSubIntervals();
        while (it.hasNext()) {
            if (wantResize((LayoutInterval)it.next())) {
                subres = true;
                break;
            }
        }
        return subres;
    }

    static boolean subordinateSize(LayoutInterval interval) {
        return interval.isSingle() && canResize(interval) && interval.getPreferredSize() == 0
                && interval.hasAttribute(ATTR_SIZE_DIFF);
    }

    static int getCurrentSize(LayoutInterval interval, int dimension) {
        if (dimension < 0) {
            assert interval.isComponent();
            dimension = interval.getComponent().getLayoutInterval(HORIZONTAL) == interval
                    ? HORIZONTAL: VERTICAL;
        }
        if (!interval.isEmptySpace()) {
            return interval.getCurrentSpace().size(dimension);
        }
        if (!canResize(interval) && interval.getPreferredSize() != NOT_EXPLICITLY_DEFINED) {
            return interval.getPreferredSize();
        }

        int[] pos = getCurrentPositions(interval, dimension);
        return pos[TRAILING] - pos[LEADING];
    }

    static int[] getCurrentPositions(LayoutInterval interval, int dimension) {
        if (dimension < 0) {
            assert interval.isComponent();
            dimension = interval.getComponent().getLayoutInterval(HORIZONTAL) == interval
                    ? HORIZONTAL: VERTICAL;
        }

        int posL;
        int posT;

        if (!interval.isEmptySpace()) {
            posL = interval.getCurrentSpace().positions[dimension][LEADING];
            posT = interval.getCurrentSpace().positions[dimension][TRAILING];
        } else {
            LayoutInterval parent = interval.getParent();
            if (parent.isSequential()) {
                int index = parent.indexOf(interval);
                posL = index > 0 ?
                    parent.getSubInterval(index-1).getCurrentSpace().positions[dimension][TRAILING] :
                    (canResize(interval) ? parent.getParent() : parent).getCurrentSpace().positions[dimension][LEADING];
                posT = index+1 < parent.getSubIntervalCount() ?
                    parent.getSubInterval(index+1).getCurrentSpace().positions[dimension][LEADING] :
                    (canResize(interval) ? parent.getParent() : parent).getCurrentSpace().positions[dimension][TRAILING];
            } else {
                posL = parent.getCurrentSpace().positions[dimension][LEADING];
                posT = parent.getCurrentSpace().positions[dimension][TRAILING];
            }
        }

        return new int[] { posL, posT };
    }

    /**
     * What size definition should the given interval have as preferred size if
     * it it should not be an explicit value and the current state should be
     * preserved? The choice is between 0 and NOT_EXPLICITLY_DEFINED. Zero size
     * should be used in cases of resizing components when the natural default
     * size would be bigger than current size, or prevent shrinking under it
     * that was allowed so far.
     */
    static int getDefaultSizeDef(LayoutInterval interval) {
        if (interval.isComponent() && LayoutInterval.canResize(interval)) {
            LayoutComponent comp = interval.getComponent();
            int dim = comp.getLayoutInterval(HORIZONTAL) == interval ? HORIZONTAL: VERTICAL;
            if (subordinateSize(interval)
                    && !interval.hasAttribute(ATTR_FLEX_SIZEDEF)
                    && (interval.diffToDefaultSize != 0 || getCurrentSize(interval, dim) > 0)) {
                return 0;
            }
            if (comp.isLayoutContainer()) {
                if (comp.getDiffToMinimumSize(dim) < 0) {
                    return 0;
                }
            } else if (interval.diffToDefaultSize < 0) {
                return 0;
            }
        } else if (interval.isEmptySpace() && interval.getMinimumSize() == 0) {
            return 0;
        }
        return NOT_EXPLICITLY_DEFINED;
    }

    static int getDiffToDefaultSize(LayoutInterval interval, boolean inside) {
        if (inside && interval.isComponent()) {
            LayoutComponent comp = interval.getComponent();
            if (comp.isLayoutContainer()) {
                int dim = comp.getLayoutInterval(HORIZONTAL) == interval ? HORIZONTAL: VERTICAL;
                int outDiff = comp.getDiffToMinimumSize(dim);
                int inDiff = comp.getDefaultLayoutRoot(dim).diffToDefaultSize;
                if (outDiff < 0) {
                    if (inDiff < 0) {
                        outDiff += inDiff;
                    } // e.g. JInternalFrame may have outDiff < 0 and inDiff > 0 (it has pref size < min size)
                    return outDiff;
                } else {
                    return inDiff;
                }
            }
        }
        return interval.diffToDefaultSize;
    }

    /**
     * Computes effective alignment of an interval in its parent. In case of
     * a sequential parent, the effective interval alignment depends on other
     * intervals and their resizability. E.g. if a preceding interval is
     * resizing then the interval is effectively "pushed" to the trailing end.
     * If there are no other intervals resizing then the parent alignment is
     * returned. If there are resizing intervals on both sides, or the interval
     * itself is resizing, then the there is no (positive) effective alignment.
     * @return LEADING, TRAILING, or DEFAULT
     */
    static int getEffectiveAlignment(LayoutInterval interval) {
        LayoutInterval parent = interval.getParent();
        if (parent.isParallel())
            return interval.getAlignment();

        if (LayoutInterval.wantResize(interval))
            return DEFAULT;

        boolean before = true;
        boolean leadingFixed = true;
        boolean trailingFixed = true;
        Iterator it = parent.getSubIntervals();
        do {
            LayoutInterval li = (LayoutInterval) it.next();
            if (li == interval) {
                before = false;
            }
            else if (LayoutInterval.wantResize(li)) {
                if (before)
                    leadingFixed = false;
                else
                    trailingFixed = false;
            }
        }
        while (it.hasNext());

        if (leadingFixed && !trailingFixed)
            return LEADING;
        if (!leadingFixed && trailingFixed)
            return TRAILING;
        if (leadingFixed && trailingFixed)
            return parent.getAlignment();

        return DEFAULT; // !leadingFixed && !trailingFixed
    }

    /**
     * Computes effective alignment of given interval's edge in its direct
     * parent. In case of a sequential parent, the effective interval alignment
     * depends on other intervals and their resizability.
     * @return effective alignment within parent, or DEFAULT in case of
     *         ambiguous alignment in sequential parent
     */
    static int getEffectiveAlignment(LayoutInterval interval, int edge, boolean considerParentSequenceAlignment) {
        assert edge == LEADING || edge == TRAILING;

        boolean wantResize = LayoutInterval.wantResize(interval);

        LayoutInterval parent = interval.getParent();
        if (parent.isParallel())
            return wantResize ? edge : interval.getAlignment();

        int n = parent.getSubIntervalCount();
        int i = edge == LEADING ? 0 : n-1;
        int d = edge == LEADING ? 1 : -1;
        boolean before = true;
        boolean beforeFixed = true;
        boolean afterFixed = true;
        while (i >=0 && i < n) {
            LayoutInterval li = parent.getSubInterval(i);
            if (li == interval) {
                before = false;
                if (wantResize) {
                    afterFixed = false;
                }
            } else if (LayoutInterval.wantResize(li)) {
                if (before)
                    beforeFixed = false;
                else
                    afterFixed = false;
            }
            i += d;
        }

        if (beforeFixed && !afterFixed)
            return edge;
        if (!beforeFixed && afterFixed)
            return edge^1;
        if (beforeFixed && afterFixed) {
            if (considerParentSequenceAlignment) {
                int parentAlignment = parent.getAlignment();
                if (parentAlignment == LEADING || parentAlignment == TRAILING) {
                    return parentAlignment;
                }
            } else {
                return edge;
            }
        }

        return DEFAULT;
    }

    /**
     * Computes effective alignment of an interval's edge relatively to given
     * parent.
     * @param interval the interval whose edge alignment should be determined
     * @param parent the parent interval in which the alignment should be
     *        determined; null can be used to go up to the root
     * @return effective alignment within parent, or DEFAULT in case of
     *         ambiguous alignment in sequential parent
     */
    static int getEffectiveAlignmentInParent(LayoutInterval interval, LayoutInterval parent, int edge) {
        assert parent == null || parent.isParentOf(interval);
        int alignment = edge;
        do {
            alignment = getEffectiveAlignment(interval, alignment, true);
            interval = interval.getParent();
            if (alignment != LEADING && alignment != TRAILING) {
                while (interval != parent && interval.getParent() != null) {
                    if (getEffectiveAlignment(interval) != alignment) {
                        return DEFAULT;
                    }
                    interval = interval.getParent();
                }
            }
        }
        while (interval != parent && interval.getParent() != null);
        return alignment;
    }

    static boolean hasAnyResizingNeighbor(LayoutInterval interval, int alignment) {
        assert alignment == LEADING || alignment == TRAILING;
        LayoutInterval parent = interval.getParent();
        if (parent.isSequential()) {
            int d = alignment==LEADING ? -1:1;
            int index = parent.indexOf(interval) + d;
            while (index >= 0 && index < parent.getSubIntervalCount()) {
                if (LayoutInterval.wantResize(parent.getSubInterval(index))) {
                    return true;
                }
                index += d;
            }
        }
        return false;
    }

    static int getIndexInParent(LayoutInterval interval, LayoutInterval parent) {
        while (interval != null) {
            if (interval.getParent() == parent) {
                return parent.indexOf(interval);
            }
            interval = interval.getParent();
        }
        return -1;
    }

    static int getDepthInParent(LayoutInterval interval, LayoutInterval parent) {
        int depth = 0;
        while (interval != null && interval != parent) {
            depth++;
            interval = interval.getParent();
        }
        return interval != null ? depth : -1;
    }

    /**
     * Creates clone of the given interval. Doesn't clone content of groups, nor
     * it sets LayoutComponent. Just the type, alignments and sizes are copied.
     *
     * @param interval interval to be cloned.
     * @param clone interval that should contain cloned data. Can be <code>null</code>.
     * @return shallow clone of the interval.
     */
    static LayoutInterval cloneInterval(LayoutInterval interval, LayoutInterval clone) {        
        clone = (clone == null) ? new LayoutInterval(interval.getType()) : clone;
        clone.setAlignment(interval.getAlignment());
        clone.setAttributes(interval.getAttributes() & ATTR_PERSISTENT_MASK);
        if (interval.getType() == PARALLEL) {
            clone.setGroupAlignment(interval.getGroupAlignment());
        }
        clone.setSizes(interval.getMinimumSize(), interval.getPreferredSize(), interval.getMaximumSize());
        if (isDefaultPadding(interval)) {
            clone.setPaddingType(interval.getPaddingType());
        }
        return clone;
    }

    // -----
    // special error diagnostics for localizing bug 240634/222703, to be removed once fixed

    private static Map<LayoutInterval, Throwable> removedIntervalsMap;

    private static void addRemovedIntervalStacktrace(LayoutInterval li) {
        if (removedIntervalsMap != null) {
            removedIntervalsMap.put(li, new Throwable());
        }
    }

    static String getRemoveStacktrace(LayoutInterval li) {
        Throwable t = removedIntervalsMap != null ? removedIntervalsMap.get(li) : null;
        if (t != null) {
            StackTraceElement[] ste = t.getStackTrace();
            StringBuilder sb = new StringBuilder();
            for (int i=1; i < ste.length; i++) {
                sb.append("      at "); // NOI18N
                sb.append(ste[i].toString());
                sb.append("\n"); // NOI18N
            }
            if (sb.length() > 0) {
                return "remove stacktrace:\n" + sb.toString(); // NOI18N
            }
        }
        return null;
    }

    static void prepareDiagnostics() {
        cleanDiagnostics();
        removedIntervalsMap = new HashMap<>();
    }

    static void cleanDiagnostics() {
        if (removedIntervalsMap != null) {
            removedIntervalsMap.clear();
            removedIntervalsMap = null;
        }
    }
}
