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

import java.util.*;


/**
 * This class collects various static methods for examining the layout.
 * For modifying methods see LayoutOperations class.
 *
 * @author Tomas Pavek
 */

public class LayoutUtils implements LayoutConstants {

    private LayoutUtils() {
    }

    public static LayoutInterval getAdjacentEmptySpace(LayoutComponent comp, int dimension, int direction) {
        LayoutInterval interval = comp.getLayoutInterval(dimension);
        LayoutInterval gap = LayoutInterval.getNeighbor(interval, direction, false, true, false);
        if (gap != null && gap.isEmptySpace()) {
            LayoutInterval gapNeighbor = LayoutInterval.getDirectNeighbor(gap, direction^1, true);
            if (gapNeighbor == interval || LayoutInterval.isPlacedAtBorder(interval, gapNeighbor, dimension, direction)) {
                return gap;
            }
        }
        return null;
//        LayoutInterval parent;
//        while ((parent = interval.getParent()) != null) {
//            if (parent.isSequential()) {
//                int index = parent.indexOf(interval);
//                if (direction == LEADING) {
//                    if (index == 0) {
//                        interval = parent;
//                    } else {
//                        LayoutInterval candidate = parent.getSubInterval(index-1);
//                        return candidate.isEmptySpace() ? candidate : null;
//                    }
//                } else {
//                    if (index == parent.getSubIntervalCount()-1) {
//                        interval = parent;
//                    } else {
//                        LayoutInterval candidate = parent.getSubInterval(index+1);
//                        return candidate.isEmptySpace() ? candidate : null;                        
//                    }
//                }
//            } else {
//                // PENDING how should we determine the space: isAlignedAtBorder, isPlacedAtBorder, any?
//                if (LayoutInterval.isPlacedAtBorder(interval, dimension, direction)) {
//                    interval = parent;
//                } else {
//                    return null;
//                }
//            }
//        }
//        return null;
    }

    public static boolean hasAdjacentComponent(LayoutComponent comp, int dimension, int direction) {
        return LayoutInterval.getNeighbor(comp.getLayoutInterval(dimension), direction, true, true, false)
                != null;
    }

    // -----
    // package private utils

    static LayoutInterval getOutermostComponent(LayoutInterval interval, int dimension, int alignment) {
        if (interval.isComponent()) {
            return interval;
        }

        assert alignment == LEADING || alignment == TRAILING;

        if (interval.isSequential()) {
            int d = alignment == LEADING ? 1 : -1;
            int i = alignment == LEADING ? 0 : interval.getSubIntervalCount()-1;
            while (i >= 0 && i < interval.getSubIntervalCount()) {
                LayoutInterval li = interval.getSubInterval(i);
                if (li.isEmptySpace()) {
                    i += d;
                } else {
                    return getOutermostComponent(li, dimension, alignment);
                }
            }
        } else if (interval.isParallel()) {
            LayoutInterval best = null;
            int pos = Integer.MAX_VALUE;
            for (int i=0, n=interval.getSubIntervalCount(); i < n; i++) {
                LayoutInterval li = interval.getSubInterval(i);
                li = getOutermostComponent(li, dimension, alignment);
                if (li != null) {
                    if (LayoutInterval.isAlignedAtBorder(li, interval, alignment)) {
                        return li;
                    }
                    int p = li.getCurrentSpace().positions[dimension][alignment]
                            * (alignment == LEADING ? 1 : -1);
                    if (p < pos) {
                        best = li;
                        pos = p;
                    }
                }
            }
            return best;
        }
        return null;
    }

    /**
     * @param exclude except this one
     * @return true if there is a component placed directly or with default gap
     *         at the group edge (does not have to be aligned)
     */
    static boolean anythingAtGroupEdge(LayoutInterval group, LayoutInterval exclude, int dimension, int alignment) {
        List<LayoutInterval> list = new LinkedList<LayoutInterval>();
        list.add(group);
        while (!list.isEmpty()) {
            LayoutInterval interval = list.remove(0);
            if (interval.isParallel()) {
                for (Iterator<LayoutInterval> it = interval.getSubIntervals(); it.hasNext(); ) {
                    LayoutInterval li = it.next();
                    if (li != exclude
                        && (LayoutInterval.isAlignedAtBorder(li, interval, alignment)
                            || LayoutInterval.isPlacedAtBorder(li, interval, dimension, alignment))) {
                        if (li.isComponent()) {
                            return true;
                        } else if (li.isGroup()) {
                            list.add(li);
                        }
                    }
                }
            } else if (interval.isSequential()) {
                LayoutInterval li = interval.getSubInterval(
                        alignment==LEADING ? 0 : interval.getSubIntervalCount()-1);
                if (li.isComponent()
                    || (li.isEmptySpace() && li.getPreferredSize() == NOT_EXPLICITLY_DEFINED
                        && !LayoutInterval.canResize(li))) {
                    return true;
                } else if (li.isParallel()) {
                    list.add(li);
                }
            }
        }
        return false;
    }

    static int getRemainingCount(LayoutInterval group, List<LayoutInterval> componentsToRemove, boolean nonEmpty) {
        int remainingCount = LayoutInterval.getCount(group, LayoutRegion.ALL_POINTS, nonEmpty);
        // adjust the count in parent for removal of the selected components
        for (Iterator<LayoutInterval> it=group.getSubIntervals(); it.hasNext(); ) {
            LayoutInterval sub = it.next();
            boolean allToRemove = false;
            for (Iterator<LayoutInterval> it2 = LayoutUtils.getComponentIterator(sub); it2.hasNext(); ) {
                allToRemove = componentsToRemove.contains(it2.next());
                if (!allToRemove) {
                    break;
                }
            }
            if (allToRemove) { // 'sub' will go away
                remainingCount--; 
            }
        }
        return remainingCount;
    }

    static int getPositionWithoutGap(Collection<LayoutInterval> intervals, int dimension, int alignment) {
        assert alignment == LEADING || alignment == TRAILING;
        int outermostPos = Integer.MIN_VALUE;
        for (LayoutInterval li : intervals) {
            assert !li.isEmptySpace();
            LayoutInterval interval = null;
            if (li.isSequential()) {
                int idx = (alignment == LEADING ? 0 : li.getSubIntervalCount()-1);
                while (idx >= 0 && idx < li.getSubIntervalCount()) {
                    interval = li.getSubInterval(idx);
                    if (!interval.isEmptySpace()) {
                        break;
                    } else {
                        interval = null;
                        idx += (alignment == LEADING ? 1 : -1);
                    }
                }
            } else {
                interval = li;
            }
            if (interval != null) {
                int pos = interval.getCurrentSpace().positions[dimension][alignment];
                if (LayoutRegion.isValidCoordinate(pos)
                        && (outermostPos == Integer.MIN_VALUE
                            || (alignment == LEADING && pos < outermostPos)
                            || (alignment == TRAILING && pos > outermostPos))) {
                    outermostPos = pos;
                }
            }
        }
        return outermostPos;
    }

    static int getSizeOfDefaultGap(LayoutInterval gap, VisualMapper visualMapper) {
        PaddingType gapType = gap.getPaddingType() != null ? gap.getPaddingType() : PaddingType.RELATED;
        int[] pads = getSizesOfDefaultGap(gap, gapType, visualMapper);
        return (pads != null && pads.length > 0) ? pads[0] : 0;
    }

    static int[] getSizesOfDefaultGap(LayoutInterval gap, VisualMapper visualMapper) {
        return getSizesOfDefaultGap(gap, null, visualMapper);
    }

    private static int[] getSizesOfDefaultGap(LayoutInterval interval, PaddingType gapType, VisualMapper visualMapper) {
        assert interval.isEmptySpace();
        LayoutInterval parent = interval.getParent();
        if (parent.isParallel()) {
            return new int[] { interval.getPreferredSize() };
        }
        
        // Find intervals that contain sources and targets
        LayoutInterval candidate = interval;
        LayoutInterval srcInt = null;
        LayoutInterval targetInt = null;
        while ((parent != null) && ((srcInt == null) || (targetInt == null))) {
            int index = parent.indexOf(candidate);
            if ((srcInt == null) && (index > 0)) {
                srcInt = parent.getSubInterval(index-1);
            }
            if ((targetInt == null) && (index < parent.getSubIntervalCount()-1)) {
                targetInt = parent.getSubInterval(index+1);
            }
            if ((srcInt == null) || (targetInt == null)) {
                do {
                    candidate = parent;
                    parent = parent.getParent();
                } while ((parent != null) && parent.isParallel());
            }
        }
        
        // Find sources and targets inside srcInt and targetInt
        List sources = getSideComponents(srcInt, TRAILING, true, false);
        List targets = getSideComponents(targetInt, LEADING, true, false);

        // Calculate size of gap from sources and targets and their positions
        return getSizesOfDefaultGap(sources, targets, gapType,
                                    visualMapper, null, Collections.<String,LayoutRegion>emptyMap());
    }

    /**
     * Finds out the sizes of given types of default gaps between the trailing
     * edge of a set of "source" components and the leading edge of a set of
     * "target" components.
     * @param gapType the padding type whose size should be returned, or null
     *        if all types should be determined
     * @return array of sizes - one element if specific padding type is asked or
     *         four elements if null is provided
     */
    static int[] getSizesOfDefaultGap(List sources, List targets, PaddingType gapType,
                VisualMapper visualMapper, String contId, Map<String,LayoutRegion> boundsMap) {
        if (((sources != null) && (sources.isEmpty()))
            || ((targets != null) && (targets.isEmpty()))) {
            return null; // Preferred gap not between components
        }
        sources = (sources == null) ? Collections.EMPTY_LIST : sources;
        targets = (targets == null) ? Collections.EMPTY_LIST : targets;
        boolean containerGap = false;
        int containerGapAlignment = -1;
        LayoutInterval temp = null;
        if (sources.isEmpty()) {
            if (targets.isEmpty()) {
                return new int[] { 0 };
            } else {
                // Leading container gap
                containerGap = true;
                containerGapAlignment = LEADING;
                temp = (LayoutInterval)targets.get(0);
            }
        } else {
            temp = (LayoutInterval)sources.get(0);
            if (targets.isEmpty()) {
                // Trailing container gap
                containerGap = true;
                containerGapAlignment = TRAILING;
            }
        }
        int dimension = (temp == temp.getComponent().getLayoutInterval(HORIZONTAL)) ? HORIZONTAL : VERTICAL;
        // Calculate max of sources and min of targets
        int max = Short.MIN_VALUE;
        int min = Short.MAX_VALUE;
        boolean positionsNotUpdated = false;
        Iterator iter = sources.iterator();
        while (iter.hasNext()) {
            LayoutInterval source = (LayoutInterval)iter.next();
            LayoutRegion region = sizeOfEmptySpaceHelper(source, boundsMap);
            int trailing = region.positions[dimension][TRAILING];
            if (trailing == LayoutRegion.UNKNOWN) {
                positionsNotUpdated = true; break;
            } else {
                max = Math.max(max, trailing);
            }
        }
        iter = targets.iterator();
        while (iter.hasNext()) {
            LayoutInterval target = (LayoutInterval)iter.next();
            LayoutRegion region = sizeOfEmptySpaceHelper(target, boundsMap);
            int leading = region.positions[dimension][LEADING];
            if (leading == LayoutRegion.UNKNOWN) {
                positionsNotUpdated = true; break;
            } else {
                min = Math.min(min, leading);
            }
        }

        int[] sizes;
        if (containerGap) {
            sizes = new int[1];
            iter = sources.isEmpty() ? targets.iterator() : sources.iterator();
            while (iter.hasNext()) {
                LayoutInterval interval = (LayoutInterval)iter.next();
                LayoutComponent component = interval.getComponent();
                LayoutRegion region = sizeOfEmptySpaceHelper(interval, boundsMap);
                String parentId = (contId == null) ? component.getParent().getId() : contId; 
                int padding = visualMapper.getPreferredPaddingInParent(parentId, component.getId(), dimension, containerGapAlignment);
                int position = region.positions[dimension][containerGapAlignment];
                int delta = (containerGapAlignment == LEADING) ? (position - min) : (max - position);
                if (!positionsNotUpdated) padding -= delta;
                sizes[0] = Math.max(sizes[0], padding);
            }            
        } else {
            PaddingType[] paddingTypes = // just one, or all types of gaps
                    gapType != null ? new PaddingType[] { gapType } : PADDINGS;
            sizes = new int[paddingTypes.length]; 
            Iterator srcIter = sources.iterator();
            while (srcIter.hasNext()) {
                LayoutInterval srcCandidate = (LayoutInterval)srcIter.next();                
                String srcId = srcCandidate.getComponent().getId();
                LayoutRegion srcRegion = sizeOfEmptySpaceHelper(srcCandidate, boundsMap);
                int srcDelta = max - srcRegion.positions[dimension][TRAILING];
                Iterator targetIter = targets.iterator();
                while (targetIter.hasNext()) {
                    LayoutInterval targetCandidate = (LayoutInterval)targetIter.next();
                    String targetId = targetCandidate.getComponent().getId();
                    LayoutRegion targetRegion = sizeOfEmptySpaceHelper(targetCandidate, boundsMap);
                    int targetDelta = targetRegion.positions[dimension][LEADING] - min;
                    for (int i=0; i < paddingTypes.length; i++) {
                        PaddingType type = paddingTypes[i];
                        int padding = visualMapper.getPreferredPadding(srcId,
                            targetId, dimension, LEADING, type);
                        if (!positionsNotUpdated) padding -= srcDelta + targetDelta;
                        sizes[i] = Math.max(sizes[i], padding);
                    }
                }
            }
        }
        return sizes;
    }

    private static LayoutRegion sizeOfEmptySpaceHelper(LayoutInterval interval, Map<String,LayoutRegion> boundsMap) {
        LayoutComponent component = interval.getComponent();
        String compId = component.getId();
        if (boundsMap.containsKey(compId)) {
            return boundsMap.get(compId);
        } else {
            return interval.getCurrentSpace();
        }
    }

    static int getVisualPosition(LayoutInterval interval, int dimension, int alignment) {
        if (interval.isEmptySpace()) {
            assert alignment == LEADING || alignment == TRAILING;
            LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(interval, alignment, false);
            if (neighbor != null) {
                interval = neighbor;
                alignment ^= 1;
            }
            else interval = LayoutInterval.getFirstParent(interval, PARALLEL);
        }
        return interval.getCurrentSpace().positions[dimension][alignment];
    }

    static int determineDimension(LayoutInterval interval) {
        Iterator<LayoutInterval> it = getComponentIterator(interval);
        if (it.hasNext()) {
            LayoutInterval comp = it.next();
            return comp == comp.getComponent().getLayoutInterval(HORIZONTAL)
                    ? HORIZONTAL : VERTICAL;
        }
        return -1;
    }

    /**
     * Lists all components that lie at given side of the group (i.e. there is
     * no gap next to them in the direction to the group edge, they're at the
     * border).
     * @param root layout interval to be be examined
     * @param edge the requested edge where the components should be looked for,
     *        LEADING or TRAILING
     * @param aligned if true, the components also must be aligned at the root's
     *        edge, otherwise it's enough there's no gap interval next to them
     * @return List of intervals that represent components that fulfill the condition
     */
    static List<LayoutInterval> edgeSubComponents(LayoutInterval root, int edge, boolean aligned) {
        return getSideSubIntervals(root, edge, true, false, true, aligned, false);
    }

    /**
     * Recursivelly collects single subintervals (i.e. gaps or components)
     * located closest to given side of an interval.
     * @param interval The interval to inspect (e.g. a group).
     * @param edge At what edge (LEADING or TRAILING).
     * @param components Looking for components?
     * @param gaps Looking for gaps?
     * @param mustBeLast If true, the side interval must be the last one in a
     *        parallel branch. If false, it must be just last of its kind.
     * @param aligned If true, the side interval must have effective alignment
     *        towards the given edge of the 'interval' parent.
     * @return List of side intervals that meet the criteria.
     */
    static List<LayoutInterval> getSideSubIntervals(LayoutInterval interval, int edge,
                                  boolean components, boolean gaps,
                                  boolean mustBeLast, boolean aligned) {
        return getSideSubIntervals(interval, edge, components, gaps, mustBeLast, aligned, false);
    }

    private static List<LayoutInterval> getSideSubIntervals(LayoutInterval interval, int edge,
                                  boolean components, boolean gaps,
                                  boolean mustBeLast, boolean aligned,
                                  boolean justFirst) {
        if (edge != LEADING && edge != TRAILING) {
            throw new IllegalArgumentException();
        }
        List<LayoutInterval> intervals = null;
        List<LayoutInterval> candidates = new LinkedList<LayoutInterval>();
        if (interval != null) {
            intervals = new LinkedList<LayoutInterval>();
            candidates.add(interval);
        }
        int d = edge == LEADING ? -1 : 1;
        while (!candidates.isEmpty() && (!justFirst || intervals.isEmpty())) {
            LayoutInterval candidate = candidates.remove(0);
            if (candidate.isGroup()) {
                if (candidate.isSequential() && candidate.getSubIntervalCount() > 0) {
                    int index = edge == LEADING ? 0 : candidate.getSubIntervalCount()-1;
                    LayoutInterval sub = candidate.getSubInterval(index);
                    candidates.add(sub);
                    if (!mustBeLast && sub.isSingle()) {
                        index -= d;
                        if (index >= 0 && index < candidate.getSubIntervalCount()
                                && (!aligned || !LayoutInterval.canResize(sub))) {
                            candidates.add(candidate.getSubInterval(index));
                        }
                    }
                } else {
                    Iterator<LayoutInterval> subs = candidate.getSubIntervals();
                    while (subs.hasNext()) {
                        LayoutInterval li = subs.next();
                        if (!aligned || LayoutInterval.isAlignedAtBorder(li, edge)) {
                            candidates.add(li);
                        }
                    }
                }
            } else if ((components && candidate.isComponent())
                       || (gaps && candidate.isEmptySpace())) {
                intervals.add(candidate);
            }
        }
        return intervals;
    }

    static List<LayoutInterval> getSideComponents(LayoutInterval interval, int edge,
                                                  boolean mustBeLast, boolean aligned) {
        return getSideSubIntervals(interval, edge, true, false, mustBeLast, aligned, false);
    }

    static boolean hasSideComponents(LayoutInterval interval, int edge,
                                     boolean mustBeLast, boolean aligned) {
        return !getSideSubIntervals(interval, edge, true, false, mustBeLast, aligned, true).isEmpty();
    }

    static List<LayoutInterval> getSideGaps(LayoutInterval interval, int edge, boolean aligned) {
        return getSideSubIntervals(interval, edge, false, true, true, aligned, false);
    }

    static boolean hasSideGaps(LayoutInterval interval, int edge, boolean aligned) {
        return !getSideSubIntervals(interval, edge, false, true, true, aligned, true).isEmpty();
    }

    static boolean alignedIntervals(LayoutInterval interval1, LayoutInterval interval2, int alignment) {
        LayoutInterval parent = LayoutInterval.getCommonParent(interval1, interval2);
        return parent != null && parent.isParallel()
               && LayoutInterval.isAlignedAtBorder(interval1, parent, alignment)
               && LayoutInterval.isAlignedAtBorder(interval2, parent, alignment);
    }

    static boolean isDefaultGapValidForNeighbor(LayoutInterval neighbor, int neighborEdge) {
        if (!hasSideComponents(neighbor, neighborEdge, true, false)
            || (!hasSideComponents(neighbor, neighborEdge, true, true)
                && hasSideGaps(neighbor, neighborEdge, true))) {
            // GroupLayout can't compute default gap if the neighbor has
            // no edge component, or even if it is, none is aligned at
            // group edge while there is an aligned gap (so the only
            // aligned interval at the edge facing the default gap is a gap).
            return false;
        }
        return true;
    }

    /**
     * Computes whether a space overlaps with content of given interval.
     * The difference from LayoutRegion.overlap(...) is that this method goes
     * recursivelly down to components in case interval is a group - does not
     * use the union space for whole group (which might be inaccurate).
     */
    static boolean contentOverlap(LayoutRegion space, LayoutInterval interval, int dimension) {
        return contentOverlap(space, interval, -1, -1, dimension);
    }

    static boolean contentOverlap(LayoutRegion space, LayoutInterval interval, int fromIndex, int toIndex, int dimension) {
        LayoutRegion examinedSpace = interval.getCurrentSpace();
        if (!interval.isGroup()) {
            return LayoutRegion.overlap(space, examinedSpace, dimension, 0);
        }
        boolean overlap = !examinedSpace.isSet(dimension)
                          || LayoutRegion.overlap(space, examinedSpace, dimension, 0);
        if (overlap) {
            if (fromIndex < 0)
                fromIndex = 0;
            if (toIndex < 0)
                toIndex = interval.getSubIntervalCount()-1;
            assert fromIndex <= toIndex;

            overlap = false;
            for (int i=fromIndex; i <= toIndex; i++) {
                LayoutInterval li = interval.getSubInterval(i);
                if (!li.isEmptySpace() && contentOverlap(space, li, dimension)) {
                    overlap = true;
                    break;
                }
            }
        }
        return overlap;
    }

    /**
     * Finds out whether components under one interval overlap with components
     * under another interval (in given dimension).
     */
    static boolean contentOverlap(LayoutInterval interval1, LayoutInterval interval2, int dimension) {
        return contentOverlap(interval1, interval2, 0, interval2.getSubIntervalCount()-1, dimension);
    }

    /**
     * @param fromIndex initial index of sub-interval in interval2
     * @param toIndex last index to consider under interval2
     */
    static boolean contentOverlap(LayoutInterval interval1, LayoutInterval interval2,
                                  int fromIndex, int toIndex, int dimension)
    {
        if (!interval2.isGroup()) {
            if (!interval1.isGroup()) {
                return LayoutRegion.overlap(interval1.getCurrentSpace(),
                                            interval2.getCurrentSpace(), dimension, 0);
            }
            LayoutInterval temp = interval1;
            interval1 = interval2;
            interval2 = temp;
        }

        // [more efficient algorithm based on region merging and ordering could be found...]
        List<LayoutInterval> int2list = null;
        List<LayoutInterval> addList = null;
        Iterator it1 = getComponentIterator(interval1);
        while (it1.hasNext()) {
            LayoutRegion space1 = ((LayoutInterval)it1.next()).getCurrentSpace();
            Iterator it2 = int2list != null ?
                           int2list.iterator() :
                           getComponentIterator(interval2, fromIndex, toIndex);
            if (int2list == null && it1.hasNext()) {
                int2list = new LinkedList<LayoutInterval>();
                addList = int2list;
            }
            while (it2.hasNext()) {
                LayoutInterval li2 = (LayoutInterval) it2.next();
                if (LayoutRegion.overlap(space1, li2.getCurrentSpace(), dimension, 0))
                    return true;
                if (addList != null)
                    addList.add(li2);
            }
            addList = null;
        }
        return false;
    }

    /**
     * Finds out whether given space overlaps with some component under given
     * interval. Unlike other contentOverlap methods that are for one dimension
     * only, here we look for a full overlap. It can be that a space overlaps
     * some components (intersects in coordinates) in each dimension, but none
     * in both dimensions together.
     */
    static boolean contentOverlap(LayoutRegion space, LayoutInterval interval) {
        for (Iterator<LayoutInterval> it=getComponentIterator(interval); it.hasNext(); ) {
            LayoutRegion compSpace = it.next().getCurrentSpace();
            if (LayoutRegion.overlap(space, compSpace, HORIZONTAL, 0)
                    && LayoutRegion.overlap(space, compSpace, VERTICAL, 0)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks the layout structure of the orthogonal dimension whether an overlap
     * of components of 'addingInterval' with components of 'existingInterval'
     * is prevented, in other of words if in the orthogonal dimension the
     * intervals of the given components are placed sequentially. (It's assumed
     * that in the other dimension the intervals have been already added.)
     */
    static boolean isOverlapPreventedInOtherDimension(LayoutInterval addingInterval,
                                                      LayoutInterval existingInterval,
                                                      int dimension)
    {
        int otherDim = dimension^1;
        Iterator<LayoutInterval> addIt = getComponentIterator(addingInterval);
        do {
            LayoutInterval otherDimAdd = addIt.next().getComponent().getLayoutInterval(otherDim);
            Iterator<LayoutInterval> exIt = getComponentIterator(existingInterval);
            do {
                LayoutInterval otherDimEx = exIt.next().getComponent().getLayoutInterval(otherDim);
                LayoutInterval parent = LayoutInterval.getCommonParent(otherDimAdd, otherDimEx);
                if (parent == null || parent.isParallel()) {
                    return false;
                }
            } while (exIt.hasNext());
        } while (addIt.hasNext());
        // Here we know that all adding component intervals are in a sequence
        // with the questioned interval in the orthogonal dimension (where 
        // already added), so there can't be an orthogonal overlap.
        return true;
    }

    static Iterator<LayoutInterval> getComponentIterator(LayoutInterval interval) {
        return new ComponentIterator(interval, 0, interval.getSubIntervalCount()-1);
    }

    static Iterator<LayoutInterval> getComponentIterator(LayoutInterval interval, int startIndex, int endIndex) {
        return new ComponentIterator(interval, startIndex, endIndex);
    }

    private static class ComponentIterator implements Iterator {
        private LayoutInterval root;
        private int startIndex, endIndex;
        private boolean initialized;
        private int index;
        private LayoutInterval next;

        ComponentIterator(LayoutInterval interval, int startIndex, int endIndex) {
            root = interval;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            findNext();
            initialized = true;
        }

        private void findNext() {
            LayoutInterval parent;
            int idx;
            if (next == null) {
                if (initialized)
                    return;
                if (!root.isGroup()) {
                    if (root.isComponent()) {
                        next = root;
                    }
                    return;
                }
                parent = root; // let's start from root
                idx = startIndex;
            }
            else if (next != root) { // somewhere in the structure
                parent = next.getParent();
                idx = index + 1;
            }
            else { // root is component, already used
                next = null;
                return;
            }

            next = null;
            do {
                while (idx < parent.getSubIntervalCount()) {
                    if (parent == root && idx > endIndex)
                        return; // out of the root set
                    LayoutInterval sub = parent.getSubInterval(idx);
                    if (sub.isComponent()) { // that's it
                        next = sub;
                        index = idx;
                        return;
                    }
                    if (sub.isGroup()) { // go down
                        parent = sub;
                        idx = 0;
                    }
                    else idx++;
                }
                if (parent != root) { // go up
                    idx = parent.getParent().indexOf(parent) + 1;
                    parent = parent.getParent();
                }
                else break; // all scanned
            }
            while (true);
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Object next() {
            if (next == null)
                throw new NoSuchElementException();

            Object ret = next;
            findNext();
            return ret;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
