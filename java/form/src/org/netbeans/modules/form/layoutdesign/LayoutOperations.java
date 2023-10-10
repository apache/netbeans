/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.form.layoutdesign;

import java.awt.Dimension;
import java.util.*;

/**
 * This class serves as a library of various useful and well-defined operations
 * on the layout model.
 *
 * @author Tomas Pavek
 */

class LayoutOperations implements LayoutConstants {

    private LayoutModel layoutModel;

    private VisualMapper visualMapper;

    private static final boolean PREFER_ZERO_GAPS = true;
    private static final boolean SYMETRIC_ZERO_GAPS = true;

    LayoutOperations(LayoutModel model, VisualMapper mapper) {
        layoutModel = model;
        visualMapper = mapper;
    }

    LayoutModel getModel() {
        return layoutModel;
    }

    VisualMapper getMapper() {
        return visualMapper;
    }

    // -----

    /**
     * Extracts surroundings of given interval (placed in a sequential group).
     * Extracted intervals are removed and go to the 'restLeading' and
     * 'restTrailing' lists. Does not extract/remove the interval itself.
     */
    int extract(LayoutInterval interval, int alignment, boolean closed,
                List<List> restLeading, List<List> restTrailing) {
        return extract(interval, interval, alignment, closed, restLeading, restTrailing);
    }
        
    int extract(LayoutInterval leading, LayoutInterval trailing, int alignment, boolean closed,
                List<List> restLeading, List<List> restTrailing)
    {
        LayoutInterval seq = leading.getParent();
        assert seq.isSequential();

        int leadingIndex = seq.indexOf(leading);
        int trailingIndex = seq.indexOf(trailing);
        int count = seq.getSubIntervalCount();
        int extractCount;
        if (closed) {
            extractCount = trailingIndex - leadingIndex + 1;
        } else if (alignment != LEADING && alignment != TRAILING) {
            extractCount = 1;
        }
        else {
            extractCount = alignment == LEADING ? count - leadingIndex : leadingIndex + 1;
        }

        if (extractCount < seq.getSubIntervalCount()) {
            List<Object/*Integer or LayoutInterval*/> toRemainL = null;
            List<Object/*Integer or LayoutInterval*/> toRemainT = null;
            int startIndex = alignment == LEADING ? leadingIndex : leadingIndex - extractCount + 1;
            int endIndex = alignment == LEADING ? trailingIndex + extractCount - 1 : trailingIndex;
            Iterator it = seq.getSubIntervals();
            for (int idx=0; it.hasNext(); idx++) {
                LayoutInterval li = (LayoutInterval) it.next();
                if (idx < startIndex) {
                    if (toRemainL == null) {
                        toRemainL = new LinkedList<Object>();
                        toRemainL.add(new Integer(LayoutInterval.getEffectiveAlignment(li)));
                    }
                    toRemainL.add(li);
                }
                else if (idx > endIndex) {
                    if (toRemainT == null) {
                        toRemainT = new LinkedList<Object>();
                        toRemainT.add(new Integer(LayoutInterval.getEffectiveAlignment(li)));
                    }
                    toRemainT.add(li);
                }
            }
            if (toRemainL != null) {
                it = toRemainL.iterator();
                it.next();
                do {
                    layoutModel.removeInterval((LayoutInterval)it.next());
                }
                while (it.hasNext());
                restLeading.add(toRemainL);
            }
            if (toRemainT != null) {
                it = toRemainT.iterator();
                it.next();
                do {
                    layoutModel.removeInterval((LayoutInterval)it.next());
                }
                while (it.hasNext());
                restTrailing.add(toRemainT);
            }
        }

        return extractCount;
    }

    /**
     * Adds parallel content of a group specified in List to given sequence.
     * Used to create a remainder parallel group to a group of aligned intervals.
     * @param list the content of the group, output from 'extract' method
     * @param seq a sequential group where to add to
     * @param index the index in the sequence where to add
     * @param dimension
     * @param position the position of the remainder group relative to the main
     *        group (LEADING or TRAILING)
//     * @param mainAlignment effective alignment of the main group (LEADING or
//     *        TRAILING or something else meaning not aligned)
     * @return parallel group if it has been created, or null
     */
    LayoutInterval addGroupContent(List<List> list, LayoutInterval seq,
                                   int index, int dimension, int position/*, int mainAlignment*/)
    {
        assert seq.isSequential() && (position == LEADING || position == TRAILING);
        boolean resizingFillGap = false;
        LayoutInterval commonGap = null;
        boolean onlyGaps = true;

        // Remove sequences just with one gap
        for (int i=list.size()-1; i >= 0; i--) {
            List subList = list.get(i);
            assert subList.size() >= 2;
            if (subList.size() == 2) { // there is just one interval
                LayoutInterval li = (LayoutInterval) subList.get(1);
                if (li.isEmptySpace()) {
                    if (commonGap == null || li.getPreferredSize() > commonGap.getPreferredSize())
                        commonGap = li;
                    if (LayoutInterval.canResize(li))
                        resizingFillGap = true;
                    list.remove(i);
                }
                else onlyGaps = false;
            }
            else onlyGaps = false;
        }

        if (onlyGaps) { // just one gap
            if (resizingFillGap && !LayoutInterval.canResize(commonGap))
                layoutModel.setIntervalSize(commonGap, NOT_EXPLICITLY_DEFINED,
                                                       commonGap.getPreferredSize(),
                                                       Short.MAX_VALUE);
            insertGapIntoSequence(commonGap, seq, index, dimension);
            return null;
        }

        if (list.size() == 1) { // just one sequence
            List subList = list.get(0);
            for (int n=subList.size(),i=n-1; i > 0; i--) { // skip alignment at 0
                LayoutInterval li = (LayoutInterval) subList.get(i);
                if (resizingFillGap && li.isEmptySpace() && !LayoutInterval.canResize(li)
                    && ((i == 1 && position == TRAILING) || (i == n-1 && position == LEADING)))
                {   // make the end gap resizing
                    layoutModel.setIntervalSize(
                            li, NOT_EXPLICITLY_DEFINED, li.getPreferredSize(), Short.MAX_VALUE);
                }
                if (li.isEmptySpace()
                        && ((i == 1 && position == LEADING) || (i == n-1 && position == TRAILING))) {
                    insertGapIntoSequence(li, seq, index, dimension);
                } else {
                    layoutModel.addInterval(li, seq, index);
                }
            }
            return null;
        }

        // create parallel group for multiple intervals/sequences
        LayoutInterval group = new LayoutInterval(PARALLEL);
//        if (position == mainAlignment) {
//            // [but this should eliminate resizability only for gaps...]
//            group.setMinimumSize(USE_PREFERRED_SIZE);
//            group.setMaximumSize(USE_PREFERRED_SIZE);
//        }
////        group.setGroupAlignment(alignment);

        // fill the group
        for (Iterator it=list.iterator(); it.hasNext(); ) {
            List subList = (List) it.next();
            LayoutInterval interval;
            if (subList.size() == 2) { // there is just one interval - use it directly
                int alignment = ((Integer)subList.get(0)).intValue();
                interval = (LayoutInterval) subList.get(1);
                if (alignment == LEADING || alignment == TRAILING)
                    layoutModel.setIntervalAlignment(interval, alignment);
            }
            else { // there are more intervals - create sequence
                interval = new LayoutInterval(SEQUENTIAL);
                int alignment = ((Integer)subList.get(0)).intValue();
                if (alignment == LEADING || alignment == TRAILING)
                    interval.setAlignment(alignment);
                for (int i=1,n=subList.size(); i < n; i++) {
                    LayoutInterval li = (LayoutInterval) subList.get(i);
                    if (resizingFillGap && li.isEmptySpace() && !LayoutInterval.canResize(li)
                        && ((i == 1 && position == TRAILING) || (i == n-1 && position == LEADING)))
                    {   // make the end gap resizing
                        layoutModel.setIntervalSize(
                                li, NOT_EXPLICITLY_DEFINED, li.getPreferredSize(), Short.MAX_VALUE);
                    }
                    layoutModel.addInterval(li, interval, -1);
                }
            }
            layoutModel.addInterval(interval, group, -1);
        }

        layoutModel.addInterval(group, seq, index);

        return group;
    }

    /**
     * Adds 'interval' to 'target'. If needed, 'interval' is dismounted and
     * instead its sub-intervals are added. This is done e.g. when adding a
     * sequence to another sequence, or if adding a parallel group to another
     * parallel group with same alignment. Also redundant groups are canceled
     * (containing just one interval).
     * @return the increase of sub-intervals in target
     */
    int addContent(LayoutInterval interval, LayoutInterval target, int index) {
        return addContent(interval, target, index, -1);
    }

    /**
     * Adds 'interval' (or its content) to 'target'. In addition to the other
     * addContent method this one takes care of merging consecutive gaps that
     * may occur when adding content of a sequence to another sequence.
     */
    int addContent(LayoutInterval interval, LayoutInterval target, int index, int dimension) {
        int count = target.getSubIntervalCount();
        while (interval.isGroup() && interval.getSubIntervalCount() == 1) {
            interval = layoutModel.removeInterval(interval, 0);
        }

        if (interval.isSequential() && target.isSequential()) {
            if (index < 0) {
                index = target.getSubIntervalCount();
            }
            int startIndex = index;
            while (interval.getSubIntervalCount() > 0) {
                LayoutInterval li = layoutModel.removeInterval(interval, 0);
                layoutModel.addInterval(li, target, index++);
            }
            if (dimension == HORIZONTAL || dimension == VERTICAL) {
                // consecutive gaps may happen where the sequence was inserted
                startIndex--; // before first added
                index--; // last added
                if (startIndex >= 0 && mergeConsecutiveGaps(target, startIndex, dimension)) {
                    index--; // one less
                }
                mergeConsecutiveGaps(target, index, dimension);
            }
        } else if (interval.isParallel() && target.isParallel()) {
            layoutModel.addInterval(interval, target, index);
            dissolveRedundantGroup(interval);
        } else {
            if (target.isSequential() && interval.getRawAlignment() != DEFAULT) {
                layoutModel.setIntervalAlignment(interval, DEFAULT);
            }
            layoutModel.addInterval(interval, target, index);
        }
        return target.getSubIntervalCount() - count;
    }

    void resizeInterval(LayoutInterval interval, int size) {
        assert size >= 0 || size == NOT_EXPLICITLY_DEFINED;
        int min = (interval.getMinimumSize() == interval.getPreferredSize()
                   && interval.getMaximumSize() < Short.MAX_VALUE) ?
                  size : interval.getMinimumSize();
        int max = interval.getMaximumSize() == interval.getPreferredSize() ?
                  ((size == NOT_EXPLICITLY_DEFINED) ? USE_PREFERRED_SIZE : size) : interval.getMaximumSize();
        layoutModel.setIntervalSize(interval, min, size, max);
    }

    void setIntervalResizing(LayoutInterval interval, boolean resizing) {
        layoutModel.setIntervalSize(interval,
            resizing ? NOT_EXPLICITLY_DEFINED : USE_PREFERRED_SIZE,
            interval.getPreferredSize(),
            resizing ? Short.MAX_VALUE : USE_PREFERRED_SIZE);
    }

    void suppressGroupResizing(LayoutInterval group) {
        // don't for root group
        if (group.getParent() != null) {
            layoutModel.setIntervalSize(group, group.getMinimumSize(),
                                               group.getPreferredSize(),
                                               USE_PREFERRED_SIZE);
        }
    }

    void enableGroupResizing(LayoutInterval group) {
        layoutModel.setIntervalSize(group, group.getMinimumSize(),
                                           group.getPreferredSize(),
                                           NOT_EXPLICITLY_DEFINED);
    }

    void eliminateResizing(LayoutInterval interval, int dimension, Collection<LayoutInterval> eliminated) {
        if (interval.isSingle()) {
            if (LayoutInterval.wantResize(interval)) {
                int pref = LayoutRegion.UNKNOWN;
                if (interval.hasAttribute(LayoutInterval.ATTR_SIZE_DIFF)) {
                    pref = LayoutInterval.getCurrentSize(interval, dimension);
                }
                if (pref == LayoutRegion.UNKNOWN) {
                    pref = interval.getPreferredSize();
                }
                layoutModel.setIntervalSize(interval, USE_PREFERRED_SIZE, pref, USE_PREFERRED_SIZE);
                if (eliminated != null) {
                    eliminated.add(interval);
                }
            }
        } else {
            for (Iterator<LayoutInterval> it=interval.getSubIntervals(); it.hasNext(); ) {
                eliminateResizing(it.next(), dimension, eliminated);
            }
        }
    }

    Collection<LayoutInterval> eliminateRedundantSuppressedResizing(LayoutInterval group, int dimension) {
        if (!group.isParallel()) {
            group = group.getParent();
        }
        if (group.getParent() != null && !LayoutInterval.canResize(group)) {
            int groupSize = group.getCurrentSpace().size(dimension);
            if (groupSize <= 0) {
                return Collections.EMPTY_LIST; // unknown current size
            }
            LayoutInterval oneResizing = null;
            boolean span = false;
            for (Iterator<LayoutInterval> it=group.getSubIntervals(); it.hasNext(); ) {
                LayoutInterval li = it.next();
                if (LayoutInterval.wantResize(li)) {
                    if (oneResizing == null) {
                        oneResizing = li;
                    } else {
                        return Collections.EMPTY_LIST; // more than one is not redundant
                    }
                } else if (!span && !li.isEmptySpace()
                           && li.getCurrentSpace().size(dimension) == groupSize) {
                    span = true;
                }
            }
            if (oneResizing != null && !span) {
                List<LayoutInterval> l = new LinkedList<LayoutInterval>();
                if (!oneResizing.isGroup() || !suppressResizingInSubgroup(oneResizing, l)) {
                    eliminateResizing(oneResizing, dimension, l);
                }
                enableGroupResizing(group);
                return l;
            }
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * @return true if group either does not want to resize, or was set to
     * suppressed resizing when parallel with more than one resizing sub interval
     */
    private boolean suppressResizingInSubgroup(LayoutInterval group, Collection<LayoutInterval> eliminated) {
        LayoutInterval oneResizing = null;
        for (Iterator<LayoutInterval> it=group.getSubIntervals(); it.hasNext(); ) {
            LayoutInterval li = it.next();
            boolean res;
            if (LayoutInterval.canResize(li)) {
                if (li.isGroup()) {
                    res = !suppressResizingInSubgroup(li, eliminated);
                } else {
                    res = true;
                }
            } else {
                res = false;
            }
            if (res) {
                if (oneResizing == null) {
                    oneResizing = li;
                } else if (group.isParallel()) { // more than 1 resizing
                    suppressGroupResizing(group);
                    eliminated.add(group);
                    return true;
                } else {
                    return false; // can't suppress a sequence
                }
            }
        }
        return oneResizing == null;
    }

    boolean completeGroupResizing(LayoutInterval group, int dimension) {
        if (!PREFER_ZERO_GAPS || !group.isParallel() || !LayoutInterval.canResize(group)
                || !LayoutInterval.contentWantResize(group)) {
            return false;
        }
        boolean gapAdded = false;
        List<LayoutInterval> list = null;
        for (Iterator<LayoutInterval> it=group.getSubIntervals(); it.hasNext(); ) {
            LayoutInterval li = it.next();
            if (!li.isEmptySpace() && !LayoutInterval.wantResize(li)
                    && (li.getAlignment() == LEADING || li.getAlignment() == TRAILING)) {
                if (list == null) {
                    list = new LinkedList<LayoutInterval>();
                }
                list.add(li);
            }
        }
        if (list != null) {
            for (LayoutInterval li : list) {
                LayoutInterval gap = new LayoutInterval(SINGLE);
                gap.setMinimumSize(0);
                gap.setPreferredSize(0);
                gap.setMaximumSize(Short.MAX_VALUE);
                gap.setAttribute(LayoutInterval.ATTR_FLEX_SIZEDEF);
                insertGap(gap, li,
                          li.getCurrentSpace().positions[dimension][li.getAlignment()^1],
                          dimension, li.getAlignment()^1);
                if (gap.getParent() != null) {
                    gapAdded = true;
                }
            }
        }
        return gapAdded;
    }

    void enableFlexibleSizeDefinition(LayoutInterval interval, boolean subcontainers) {
        if (interval.isGroup()) {
            Iterator<LayoutInterval> it = interval.getSubIntervals();
            while (it.hasNext()) {
                enableFlexibleSizeDefinition(it.next(), subcontainers);
            }
        } else {
            layoutModel.changeIntervalAttribute(interval, LayoutInterval.ATTR_FLEX_SIZEDEF, true);
            if (interval.isComponent() && subcontainers && interval.getComponent().isLayoutContainer()) {
                LayoutComponent comp = interval.getComponent();
                int dimension = comp.getLayoutInterval(HORIZONTAL) == interval ? HORIZONTAL : VERTICAL;
                enableFlexibleSizeDefinition(comp.getDefaultLayoutRoot(dimension), true);
            }
        }
    }

    /**
     * Sets all components in a parallel group that have the same size with
     * 'aligned' to resizing so they all accommodate to same size.
     */
    void setParallelSameSize(LayoutInterval group, LayoutInterval aligned, int dimension) {
        assert group.isParallel();

        LayoutInterval alignedComp = getOneNonEmpty(aligned);

        for (Iterator it=group.getSubIntervals(); it.hasNext(); ) {
            LayoutInterval li = (LayoutInterval) it.next();
            if (li == aligned) {
                continue;
            }
            if (li.isParallel()) {
                if (li.getGroupAlignment() == LEADING || li.getGroupAlignment() == TRAILING) { // not for baseline group
                    setParallelSameSize(li, alignedComp, dimension);
                }
            } else {
                LayoutInterval sub = getOneNonEmpty(li);
                if (sub != null
                    && LayoutRegion.sameSpace(alignedComp.getCurrentSpace(), sub.getCurrentSpace(), dimension)
                    && !LayoutInterval.wantResize(li)) {
                    // viusally aligned subinterval
                    if (sub.isParallel()) {
                        if (sub.getGroupAlignment() == LEADING || sub.getGroupAlignment() == TRAILING) { // not for baseline group
                            setParallelSameSize(sub, alignedComp, dimension);
                        }
                    } else if (!isPressurizedComponent(sub)) {
                        // make this component filling the group - effectively keeping same size
                        if (!LayoutInterval.isAlignedAtBorder(li, aligned.getAlignment())) {
                            layoutModel.setIntervalAlignment(li, aligned.getAlignment());
                        }
                        int min = sub.getMinimumSize();
                        layoutModel.setIntervalSize(sub,
                                min != USE_PREFERRED_SIZE ? min : NOT_EXPLICITLY_DEFINED,
                                sub.getPreferredSize(),
                                Short.MAX_VALUE);
                    }
                }
            }
        }
    }

    private static LayoutInterval getOneNonEmpty(LayoutInterval interval) {
        if (!interval.isSequential()) {
            return interval;
        }

        LayoutInterval nonEmpty = null;
        for (Iterator it=interval.getSubIntervals(); it.hasNext(); ) {
            LayoutInterval li = (LayoutInterval) it.next();
            if (!li.isEmptySpace()) {
                if (nonEmpty == null) {
                    nonEmpty = li;
                } else {
                    return null;
                }
            }
        }
        return nonEmpty;
    }

    /**
     * Is the component set to explicit fixed size smaller than its natural
     * minimum size?
     */
    private boolean isPressurizedComponent(LayoutInterval interval) {
        int pref = interval.getPreferredSize();
        if (interval.isComponent() && !LayoutInterval.canResize(interval)
                && pref != NOT_EXPLICITLY_DEFINED) {
            LayoutComponent comp = interval.getComponent();
            Dimension minSize = visualMapper.getComponentMinimumSize(comp.getId());
            if (minSize != null) {
                int min = comp.getLayoutInterval(HORIZONTAL) == interval ? minSize.width : minSize.height;
                if (pref < min) {
                    return true;
                }
            }
        }
        return false;
    }

    void mergeParallelGroups(LayoutInterval group) {
        for (int i=group.getSubIntervalCount()-1; i >= 0; i--) {
            LayoutInterval sub = group.getSubInterval(i);
            if (sub.isGroup()) {
                mergeParallelGroups(sub);
                dissolveRedundantGroup(sub);
            }
        }
    }

    /**
     * Dissolves given group to parent group in case it is redundant.
     * @return true if the group was dissolved
     */
    boolean dissolveRedundantGroup(LayoutInterval group) {
        LayoutInterval parent = group.getParent();
        if (parent == null)
            return false;

        boolean justOne = (group.getSubIntervalCount() == 1);
        boolean dissolve = false;
        if (justOne) {
            dissolve = true;
        } else if (group.isSequential() && parent.isSequential()) {
            dissolve = true;
        } else if (group.isParallel() && parent.isParallel()) {
            int gA = group.getGroupAlignment();
            int pA = parent.getGroupAlignment();
            if (parent.getSubIntervalCount() == 1
                    && (gA == pA || (gA != BASELINE && gA != CENTER))) {
                dissolve = true;
            } else { // check for compatible alignment and resizability
                int align = group.getAlignment();
                boolean sameAlign = true;
                boolean subResizing = false;
                Iterator it = group.getSubIntervals();
                while (it.hasNext()) {
                    LayoutInterval li = (LayoutInterval) it.next();
                    if (!subResizing && LayoutInterval.wantResize(li)) {
                        subResizing = true;
                    }
                    if (li.getAlignment() != align) {
                        sameAlign = false;
                    }
                }
                boolean compatible;
                if (subResizing && (sameAlign || gA != BASELINE)) {
                    compatible = false;
                    boolean resizingAlreadyContained;
                    if (LayoutInterval.canResize(group)) {
                        resizingAlreadyContained = true;
                    } else if (!LayoutInterval.canResize(parent)) {
                        int dim = LayoutUtils.determineDimension(parent);
                        resizingAlreadyContained = dim < 0
                                || LayoutInterval.getCurrentSize(parent, dim) == LayoutInterval.getCurrentSize(group, dim);
                    } else {
                        resizingAlreadyContained = false;
                    }
                    if (resizingAlreadyContained) {
                        it = parent.getSubIntervals();
                        while (it.hasNext()) {
                            LayoutInterval li = (LayoutInterval) it.next();
                            if (li != group && LayoutInterval.wantResize(li)) {
                                compatible = true;
                                break;
                            }
                        }
                        if (!compatible && (align == LEADING || align == TRAILING)) {
                            LayoutInterval neighbor = LayoutInterval.getNeighbor(
                                    parent, group.getAlignment()^1, false, true, true);
                            if (neighbor != null && neighbor.isEmptySpace()
                                && neighbor.getPreferredSize() == NOT_EXPLICITLY_DEFINED)
                            {   // default fixed padding means there is no space for
                                // independent size change, so the subgroup can be merged
                                compatible = true;
                            }
                        }
                    }
                }
                else compatible = sameAlign;

                dissolve = compatible;
            }
        }

        if (dissolve) { // the sub-group can be dissolved into parent group
            if (parent.isParallel()) { // moving to parallel group
                int alignmentInParent = group.getAlignment();
                int index = layoutModel.removeInterval(group);
                while (group.getSubIntervalCount() > 0) {
                    LayoutInterval li = group.getSubInterval(0);
                    int align = li.getAlignment();
                    layoutModel.removeInterval(li);
                    if (justOne) {
                        if ((align != DEFAULT && align != alignmentInParent)
                                || (align == DEFAULT && alignmentInParent != parent.getGroupAlignment())) {
                            layoutModel.setIntervalAlignment(li, group.getRawAlignment());
                        }
                        if ((!LayoutInterval.canResize(group) || align == BASELINE)
                                && LayoutInterval.wantResize(li)) {
                            // resizing interval in fixed group - make it fixed
                            if (li.isGroup()) {
                                suppressGroupResizing(li);
                            } else {
                                layoutModel.setIntervalSize(li, USE_PREFERRED_SIZE, li.getPreferredSize(), USE_PREFERRED_SIZE);
                            }
                        }
                    } else if (group.isParallel()) { // from parallel group
                        if (li.getRawAlignment() == DEFAULT
                            && group.getGroupAlignment() != parent.getGroupAlignment())
                        {   // force alignment explicitly
                            layoutModel.setIntervalAlignment(li, align);
                        }
                    }
                    layoutModel.addInterval(li, parent, index++);
                }
            } else { // moving to sequential group
                int dim = LayoutUtils.determineDimension(group);
                int index = layoutModel.removeInterval(group);
                addContent(group, parent, index, dim);
            }
            if (parent.getSubIntervalCount() == 1) {
                dissolveRedundantGroup(parent.getSubInterval(0));
            }
            return true;
        }
        return false;
    }

    /** NOT USED
     * This method goes through a sequential group and moves each interval next
     * to an open edge of a parallel group into the group.
     * @param parent sequential group to process
     * @param dimension
     */
    void moveInsideSequential(LayoutInterval parent, int dimension) {
        assert parent.isSequential();
        if (!parent.isSequential())
            return;

        int alignment = LEADING;
        do {
            LayoutInterval extend = findIntervalToExtend(parent, dimension, alignment);
            if (extend == null) {
                if (alignment == LEADING) {
                    alignment = TRAILING;
                    extend = findIntervalToExtend(parent, dimension, alignment);
                }
                if (extend == null)
                    break;
            }

            LayoutInterval inGroup = extend.getParent(); // group to infiltrate
            LayoutInterval outGroup = inGroup;
            while (outGroup.getParent() != parent) {
                outGroup = outGroup.getParent();
            }
            int index = parent.indexOf(outGroup);
            int d = alignment == LEADING ? -1 : 1;

            // will the group remain open at the opposite edge?
            boolean commonEndingGap = true;
            for (int i=index-d, n=parent.getSubIntervalCount(); i >= 0 && i < n; i-=d) {
                LayoutInterval li = parent.getSubInterval(i);
                if ((!li.isEmptySpace() || (i-d >= 0 && i-d < n)) // ignore last gap
                    && LayoutInterval.wantResize(li))
                {   // resizing interval will close the group
                    // possibly need to separate the rest of the group not to be influenced
                    LayoutInterval endGap = parent.getSubInterval(alignment == LEADING ? n-1 : 0);
                    if (endGap == null || endGap.getPreferredSize() != NOT_EXPLICITLY_DEFINED) {
                        commonEndingGap = false;
                        LayoutInterval closing = extend;
                        int borderPos = parent.getCurrentSpace().positions[dimension][alignment^1];
                        do {
                            LayoutInterval par = closing.getParent();
                            if (par.isParallel()) {
                                separateGroupContent(closing, borderPos, dimension, alignment^1);
                            }
                            closing = par;
                        }
                        while (closing != outGroup);
                    }
                    break;
                }
            }

            int extendPos = extend.getCurrentSpace().positions[dimension][alignment^1];
            if (!extend.isSequential()) {
                LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
                seq.setAlignment(extend.getAlignment());
                layoutModel.addInterval(seq, inGroup, layoutModel.removeInterval(extend));
                layoutModel.setIntervalAlignment(extend, DEFAULT);
                layoutModel.addInterval(extend, seq, 0);
                extend = seq;
            }

            // move the intervals from outside inside the group, next to found interval (extend)
            LayoutInterval connectingGap = null;
            int idx, addIdx;
            if (alignment == LEADING) {
                idx = index + 1; // start behind the group
                addIdx = extend.getSubIntervalCount(); // add behind the interval
            }
            else {
                idx = index - 1; // start before the group
                addIdx = 0; // add before the interval
            }
            while (idx >= 0 && idx < parent.getSubIntervalCount()) {
                LayoutInterval li = parent.getSubInterval(idx);
                if (li.isEmptySpace()) {
                    if (connectingGap == null) { // first gap
                        if (extendPos != outGroup.getCurrentSpace().positions[dimension][alignment^1]) {
                            // need to extend the first gap (extended interval inside group is smaller than the group)
                            int neighborPos = parent.getSubInterval(idx-d).getCurrentSpace().positions[dimension][alignment];
                            int distance = d * (extendPos - neighborPos);
                            if (distance > 0)
                                resizeInterval(li, distance);
                        }
                        connectingGap = li;
                    }
                    else if ((idx == 0 || idx == parent.getSubIntervalCount()-1)
                             && commonEndingGap)
                    {   // keep the last gap out
                        break;
                    }
                }
                layoutModel.removeInterval(li);
                layoutModel.addInterval(li, extend, addIdx);
                if (alignment == LEADING)
                    addIdx++;
                else
                    idx--;
            }

            // check if the sequence was not whole moved into the group
            if (parent.getSubIntervalCount() == 1) { // only neighborGroup remained, eliminate the parent group
                assert outGroup == parent.getSubInterval(0);
                layoutModel.removeInterval(outGroup);
                LayoutInterval superParent = parent.getParent();
                addContent(outGroup, superParent, layoutModel.removeInterval(parent));
                break;
            }
        }
        while (true);
    }

    private LayoutInterval findIntervalToExtend(LayoutInterval parent, int dimension, int alignment) {
        int d = alignment == LEADING ? -1 : 1;
        int count = parent.getSubIntervalCount();
        int idx = alignment == LEADING ? count-1 : 0;
        boolean atBorder = true;
        boolean gap = false;

        while (idx >= 0 && idx < parent.getSubIntervalCount()) {
            LayoutInterval sub = parent.getSubInterval(idx);
            if (sub.isEmptySpace()) {
                gap = true;
            }
            else {
                if (!atBorder && gap && sub.isParallel()
                    && !LayoutInterval.isClosedGroup(sub, alignment^1))
                {   // this open parallel sub-group might be a candidate to move inside to
                    int startIndex, endIndex;
                    if (alignment == LEADING) {
                        startIndex = idx + 1;
                        endIndex = parent.getSubIntervalCount() - 1;
                    }
                    else {
                        startIndex = 0;
                        endIndex = idx - 1;
                    }
                    LayoutInterval extend = prepareGroupExtension(
                            sub, parent, startIndex, endIndex, dimension, alignment^1);
                    if (extend != null)
                        return extend;
                }
                gap = false;
                atBorder = false;
            }
            idx += d;
        }
        return null;
    }

    private LayoutInterval prepareGroupExtension(LayoutInterval group,
                    LayoutInterval parent, int startIndex, int endIndex,
                    int dimension, int alignment)
    {
        boolean allOverlapping = true;
        LayoutInterval singleOverlap = null;
        List<LayoutInterval> overlapList = null;

        // looking for all intervals the given space is located next to
        Iterator it = group.getSubIntervals();
        while (it.hasNext()) {
            LayoutInterval li = (LayoutInterval) it.next();
            if (!li.isEmptySpace()) {
                if (LayoutUtils.contentOverlap(li, parent, startIndex, endIndex, dimension^1)) {
                    // interval overlaps orthogonally
                    if (singleOverlap == null) {
                        singleOverlap = li;
                    }
                    else {
                        if (overlapList == null) {
                            overlapList = new LinkedList<LayoutInterval>();
                            overlapList.add(singleOverlap);
                        }
                        overlapList.add(li);
                    }
                }
                else allOverlapping = false;
            }
        }

        if (allOverlapping || singleOverlap == null)
            return null; // spans whole group or nothing

        if (overlapList != null) { // overlaps multiple intervals
            LayoutInterval subGroup = new LayoutInterval(PARALLEL);
            subGroup.setGroupAlignment(alignment^1);
            subGroup.setAlignment(alignment^1);
            int index = -1;
            do {
                LayoutInterval li = overlapList.remove(0);
                int idx = layoutModel.removeInterval(li);
                if (index < 0) {
                    index = idx;
                }
                layoutModel.addInterval(li, subGroup, -1);
                subGroup.getCurrentSpace().expand(li.getCurrentSpace());
            }
            while (overlapList.size() > 0);

            layoutModel.addInterval(subGroup, group, index);
            singleOverlap = subGroup;
        }
        else {
            LayoutInterval subParallel;
            if (singleOverlap.isSequential()) {
                subParallel = singleOverlap.getSubInterval(
                              alignment == LEADING ? 0 : singleOverlap.getSubIntervalCount()-1);
                if (!subParallel.isParallel())
                    subParallel = null;
            }
            else if (singleOverlap.isParallel()) {
                subParallel = singleOverlap;
            }
            else subParallel = null;

            if (subParallel != null && !LayoutInterval.isClosedGroup(subParallel, alignment)) {
                LayoutInterval subOverlap = prepareGroupExtension(
                        subParallel, parent, startIndex, endIndex, dimension, alignment);
                if (subOverlap != null)
                    singleOverlap = subOverlap;
            }
        }

        return singleOverlap;
    }

    // [couldn't parallelizeWithParentSequence be used instead? or LayoutFeeder.separateSequence?]
    private void separateGroupContent(LayoutInterval separate, int outPos, int dimension, int alignment) {
        LayoutInterval group = separate.getParent();
        assert group.isParallel();
        LayoutInterval remainder = null;
        LayoutInterval remainderGroup = null;
        LayoutRegion remainderSpace = null;

        for (int i=0; i < group.getSubIntervalCount(); ) {
            LayoutInterval li = group.getSubInterval(i);
            if (li != separate) {
                assert li.getAlignment() == (alignment^1);
                layoutModel.removeInterval(li);
                if (remainder == null) {
                    remainder = li;
                }
                else {
                    if (remainderGroup == null) {
                        remainderGroup = new LayoutInterval(PARALLEL);
                        remainderGroup.setAlignment(alignment^1);
                        remainderGroup.setGroupAlignment(alignment^1);
                        layoutModel.addInterval(remainder, remainderGroup, 0);
                        remainder = remainderGroup;
                    }
                    layoutModel.addInterval(li, remainderGroup, -1);
                }
                if (!li.isEmptySpace()) {
                    if (remainderSpace == null) {
                        remainderSpace = new LayoutRegion();
                    }
                    remainderSpace.expand(li.getCurrentSpace());
                }
            }
            else i++;
        }
        remainder.setCurrentSpace(remainderSpace);

        LayoutInterval remainderGap;
        int remainderPos = remainderSpace.positions[dimension][alignment];
        if (LayoutRegion.isValidCoordinate(outPos)) {
            int gapSize = alignment == LEADING ? remainderPos - outPos : outPos - remainderPos;
            remainderGap = new LayoutInterval(SINGLE);
            remainderGap.setSizes(NOT_EXPLICITLY_DEFINED, gapSize, Short.MAX_VALUE);
        }
        else { // take the existing gap next to group [this case is not used currently]
            remainderGap = LayoutInterval.getDirectNeighbor(group, alignment, false);
            if (remainderGap != null && remainderGap.isEmptySpace()) {
                layoutModel.removeInterval(remainderGap);
                // [should check for last interval in parent]
                LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(group, alignment, true);
                outPos = neighbor != null ?
                    neighbor.getCurrentSpace().positions[dimension][alignment^1] :
                    group.getParent().getCurrentSpace().positions[dimension][alignment];
                int gapSize = alignment == LEADING ? remainderPos - outPos : outPos - remainderPos;
                resizeInterval(remainderGap, gapSize);
            }
            else remainderGap = null;
        }
        if (remainderGap != null) {
            LayoutInterval seq;
            if (remainder.isSequential()) {
                seq = remainder;
            }
            else {
                 seq = new LayoutInterval(SEQUENTIAL);
                 layoutModel.setIntervalAlignment(remainder, DEFAULT);
                 layoutModel.addInterval(remainder, seq, 0);
            }
            layoutModel.addInterval(remainderGap, seq, alignment == LEADING ? 0 : -1);
            layoutModel.addInterval(seq, group, -1);
            group.getCurrentSpace().positions[dimension][alignment] = outPos;
        }
        else {
            layoutModel.addInterval(remainder, group, -1);
        }
    }

    /**
     * Makes given interval parallel with part of its parent sequence.
     */
    void parallelizeWithParentSequence(LayoutInterval interval, int endIndex, int dimension) {
        LayoutInterval parent = interval.getParent();
        assert parent.isParallel();
        LayoutInterval parParent = parent;
        while (!parParent.getParent().isSequential()) {
            parParent = parParent.getParent();
        }
        LayoutInterval parentSeq = parParent.getParent();

        int startIndex = parentSeq.indexOf(parParent);
        if (endIndex < 0)
            endIndex = parentSeq.getSubIntervalCount() - 1;
        else if (startIndex > endIndex) {
            int temp = startIndex;
            startIndex = endIndex;
            endIndex = temp;
        }

        layoutModel.removeInterval(interval);
        // TODO compensate possible group shrinking when removing the biggest interval
        if (interval.getAlignment() == DEFAULT) {
            layoutModel.setIntervalAlignment(interval, parent.getGroupAlignment());
        }
        addParallelWithSequence(interval, parentSeq, startIndex, endIndex, dimension);

        if (parent.getSubIntervalCount() == 1) {
            addContent(layoutModel.removeInterval(parent, 0),
                       parent.getParent(),
                       layoutModel.removeInterval(parent),
                       dimension);
        }
        else if (parent.getSubIntervalCount() == 0) {
            layoutModel.removeInterval(parent);
        }
    }

    void addParallelWithSequence(LayoutInterval interval, LayoutInterval seq, int startIndex, int endIndex, int dimension) {
        LayoutInterval group;
        if (startIndex > 0 || endIndex < seq.getSubIntervalCount()-1) {
            group = new LayoutInterval(PARALLEL);
            if (interval.getAlignment() != DEFAULT) {
                group.setGroupAlignment(interval.getAlignment());
            }
            int startPos = LayoutUtils.getVisualPosition(seq.getSubInterval(startIndex), dimension, LEADING);
            int endPos = LayoutUtils.getVisualPosition(seq.getSubInterval(endIndex), dimension, TRAILING);
            group.getCurrentSpace().set(dimension, startPos, endPos);

            if (startIndex != endIndex) {
                LayoutInterval subSeq = new LayoutInterval(SEQUENTIAL);
                subSeq.setAlignment(interval.getAlignment()); // [was: seq.getAlignment()]
                for (int n=endIndex-startIndex+1; n > 0; n--) {
                    layoutModel.addInterval(layoutModel.removeInterval(seq, startIndex), subSeq, -1);
                }
                layoutModel.addInterval(subSeq, group, 0);
            }
            else {
                layoutModel.addInterval(layoutModel.removeInterval(seq, startIndex), group, 0);
            }
            layoutModel.addInterval(group, seq, startIndex);
            group.getCurrentSpace().expand(interval.getCurrentSpace(), dimension);
        }
        else {
            group = seq.getParent();
        }
        layoutModel.addInterval(interval, group, -1);
    }

    int optimizeGaps(LayoutInterval group, int dimension) {
        for (int i=0; i < group.getSubIntervalCount(); i++) {
            LayoutInterval li = group.getSubInterval(i);
            if (li.isEmptySpace() && group.getSubIntervalCount() > 1) {
                // remove container supporting gap
                layoutModel.removeInterval(group, i);
                i--;
            }
        }
        if (group.getSubIntervalCount() <= 1) {
            return -1;
        }

        // 1) Determine which intervals have ending gaps for optimization, what's
        // their alignment and resizability, and whether whole group should be
        // processed or just a part (subgroup).
        boolean anyAlignedLeading = false; // if false the group is open at leading edge
        boolean anyAlignedTrailing = false; // if false the group is open at trailing edge
        boolean contentResizing = false;
        IntervalSet processLeading = null;
        IntervalSet processTrailing = null;
        boolean subGroupLeading = false;
        boolean subGroupTrailing = false;

        {
        // 1a) Analyze where the ending gaps are and how aligned.
        IntervalSet[] alignedGaps = new IntervalSet[] { new IntervalSet(), new IntervalSet() };
        IntervalSet[] alignedNoGaps = new IntervalSet[] { new IntervalSet(), new IntervalSet() };
        IntervalSet[] unalignedFixedGaps = new IntervalSet[] { new IntervalSet(), new IntervalSet() };
        IntervalSet[] unalignedResGaps = new IntervalSet[] { new IntervalSet(), new IntervalSet() };
        IntervalSet[] unalignedNoGaps = new IntervalSet[] { new IntervalSet(), new IntervalSet() };

        for (int i=0; i < group.getSubIntervalCount(); i++) {
            LayoutInterval li = group.getSubInterval(i);

            boolean leadAlign = false;
            boolean trailAlign = false;
            LayoutInterval leadingGap = null;
            LayoutInterval trailingGap = null;
            boolean leadGapRes = false;
            boolean trailGapRes = false;
            boolean contentRes = false;
            boolean noResizing = false;

            if (li.isSequential()) {
                // find out effective alignment of the sequence content without border gaps
                for (int j=0; j < li.getSubIntervalCount(); j++) {
                    LayoutInterval sub = li.getSubInterval(j);
                    if (j == 0 && sub.isEmptySpace()) {
                        leadingGap = sub;
                        leadGapRes = LayoutInterval.wantResize(sub);
                    } else if (j+1 == li.getSubIntervalCount() && sub.isEmptySpace()) {
                        trailingGap = sub;
                        trailGapRes = LayoutInterval.wantResize(sub);
                    } else if (!contentRes && LayoutInterval.wantResize(sub)) {
                        contentRes = true;
                    }
                }
                if (!contentRes) {
                    if (leadGapRes || trailGapRes) {
                        leadAlign = trailGapRes && !leadGapRes;
                        trailAlign = leadGapRes && !trailGapRes;
                    } else {
                        noResizing = true;
                    }
                }
            } else if (LayoutInterval.wantResize(li)) {
                contentRes = true;
            } else {
                noResizing = true;
            }
            if (contentRes) {
                leadAlign = trailAlign = true;
            } else if (noResizing) {
                int alignment = li.getAlignment();
                leadAlign = (alignment == LEADING);
                trailAlign = (alignment == TRAILING);
            }

            if (leadingGap != null) {
                if (leadAlign) {
                    alignedGaps[LEADING].add(li, !noResizing);
                } else if (leadGapRes) {
                    unalignedResGaps[LEADING].add(li, true);
                } else {
                    unalignedFixedGaps[LEADING].add(li, false);
                }
            } else if (!LayoutUtils.hasSideGaps(li, LEADING, true)) {
                if (leadAlign) {
                    alignedNoGaps[LEADING].add(li, !noResizing);
                } else {
                    unalignedNoGaps[LEADING].add(li, false);
                }
            }
            if (trailingGap != null) {
                if (trailAlign) {
                    alignedGaps[TRAILING].add(li, !noResizing);
                } else if (trailGapRes) {
                    unalignedResGaps[TRAILING].add(li, true);
                } else {
                    unalignedFixedGaps[TRAILING].add(li, false);
                }
            } else if (!LayoutUtils.hasSideGaps(li, TRAILING, true)) {
                if (trailAlign) {
                    alignedNoGaps[TRAILING].add(li, !noResizing);
                } else {
                    unalignedNoGaps[TRAILING].add(li, false);
                }
            }
        }

        // 1b) Find out what gaps to optimize on each side of the group.
        IntervalSet[] alignedVariants = countAlignedVariants(alignedGaps, unalignedFixedGaps, unalignedResGaps);
        IntervalSet[] unalignedVariants = countUnalignedVariants(unalignedFixedGaps, unalignedResGaps, unalignedNoGaps, dimension);
        IntervalSet[] leadingVariants = new IntervalSet[] { alignedVariants[LEADING], unalignedVariants[LEADING] };
        IntervalSet[] trailingVariants = new IntervalSet[] { alignedVariants[TRAILING], unalignedVariants[TRAILING] };
        IntervalSet bestLeading = null;
        IntervalSet bestTrailing = null;

        for (int i=0; i < leadingVariants.length; i++) {
            IntervalSet iSet = leadingVariants[i];
            if (bestLeading == null || iSet.count() > bestLeading.count()) {
                bestLeading = iSet;
            }
        }
        for (int i=0; i < trailingVariants.length; i++) {
            IntervalSet iSet = trailingVariants[i];
            if (bestTrailing == null || iSet.count() > bestTrailing.count()) {
                bestTrailing = iSet;
            }
        }
        if (bestLeading.count() < group.getSubIntervalCount()
                && bestTrailing.count() < group.getSubIntervalCount()) {
            // can't optimize everything on both sides, so check combinations and
            // look for a suitable subgroup
            IntervalSet bestCombine = null;
            for (int i=0; i < leadingVariants.length; i++) {
                IntervalSet lSet = leadingVariants[i];
                for (int j=0; j < trailingVariants.length; j++) {
                    IntervalSet tSet = trailingVariants[j];
                    IntervalSet comSet = new IntervalSet();
                    for (int ii=0; ii < group.getSubIntervalCount(); ii++) {
                        LayoutInterval li = group.getSubInterval(ii);
                        if (lSet.contains(li) && tSet.contains(li)) {
                            comSet.add(li, LayoutInterval.wantResize(li));
                        }
                    }
                    if (bestCombine == null || comSet.count() > bestCombine.count()) {
                        bestCombine = comSet;
                    }
                }
            }
            if (bestCombine != null) {
                IntervalSet bestSingle = bestLeading.count() > bestTrailing.count()
                                         ? bestLeading : bestTrailing;
                if (bestSingle.count() - bestCombine.count() >= bestCombine.count()) {
                    // subgroup for one side
                    if (bestSingle == bestLeading) {
                        bestTrailing.clear();
                    } else {
                        bestLeading.clear();
                    }
                } else { // subgroup for both sides
                    bestLeading = bestCombine;
                    bestTrailing = bestCombine;
                }
            }
        }

        processLeading = bestLeading;
        if (processLeading.count() < 2) {
            processLeading.clear();
        }
        processTrailing = bestTrailing;
        if (processTrailing.count() < 2) {
            processTrailing.clear();
        }
        subGroupLeading = processLeading.count() < group.getSubIntervalCount();
        subGroupTrailing = processTrailing.count() < group.getSubIntervalCount();

        if (processLeading.count() > 0 || processTrailing.count() > 0) {
            // now when knowing which intervals are relevant for optimization,
            // determine their alignment and resizability
            for (int i=0; i < group.getSubIntervalCount(); i++) {
                LayoutInterval li = group.getSubInterval(i);
                boolean isL = processLeading.contains(li);
                boolean isT = processTrailing.contains(li);
                if (!isL && !isT) {
                    continue;
                }

                boolean leadAlign = false;
                boolean trailAlign = false;
                boolean contentRes = false;
                boolean noResizing = false;

                if (li.isSequential()) {
                    boolean leadGapRes = false;
                    boolean trailGapRes = false;
                    for (int j=0; j < li.getSubIntervalCount(); j++) {
                        LayoutInterval sub = li.getSubInterval(j);
                        if (j == 0 && isL && sub.isEmptySpace()) {
                            leadGapRes = LayoutInterval.wantResize(sub);
                        } else if (j+1 == li.getSubIntervalCount() && isT && sub.isEmptySpace()) {
                            trailGapRes = LayoutInterval.wantResize(sub);
                        } else if (!contentRes && LayoutInterval.wantResize(sub)) {
                            contentRes = true;
                        }
                    }
                    if (!contentRes) {
                        if (leadGapRes || trailGapRes) {
                            leadAlign = trailGapRes && !leadGapRes;
                            trailAlign = leadGapRes && !trailGapRes;
                        } else {
                            noResizing = true;
                        }
                    }
                } else if (LayoutInterval.wantResize(li)) {
                    contentRes = true;
                } else {
                    noResizing = true;
                }
                if (contentRes) {
                    leadAlign = trailAlign = true;
                } else if (noResizing) {
                    int alignment = li.getAlignment();
                    leadAlign = (alignment == LEADING);
                    trailAlign = (alignment == TRAILING);
                }

                if (leadAlign && isL) {
                    anyAlignedLeading = true;
                }
                if (trailAlign && isT) {
                    anyAlignedTrailing = true;
                }
                if (contentRes) {
                    contentResizing = true;
                }
            }
        } else {
            contentResizing = LayoutInterval.wantResize(group);
        }
        }

        // 2) Remove gaps where needed (to be substituted, or if just invalid).
        boolean defaultLeadingPadding = false;
        boolean defaultTrailingPadding = false;
        PaddingType leadingPadding = null;
        PaddingType trailingPadding = null;
        boolean effectiveExplicitGapLeading = false;
        boolean effectiveExplicitGapTrailing = false;
        boolean resizingGapLeading = false;
        boolean resizingGapTrailing = false;
        LayoutInterval zeroGapLeading = null;
        LayoutInterval zeroGapTrailing = null;
        boolean validLeadingGapRemoved = false;
        boolean validTrailingGapRemoved = false;
        int commonGapLeadingSize = Integer.MIN_VALUE;
        int commonGapTrailingSize = Integer.MIN_VALUE;
        boolean mayNeedSecondPass = false;
        List<LayoutInterval> reduceToZeroGapsLeading = new LinkedList<>();
        List<LayoutInterval> reduceToZeroGapsTrailing = new LinkedList<>();

        for (int i=0; i < group.getSubIntervalCount(); i++) {
            LayoutInterval li = group.getSubInterval(i);
            if (!li.isSequential() || li.getSubIntervalCount() == 0) {
                continue;
            }
            LayoutInterval gap = li.getSubInterval(0);
            if (gap.isEmptySpace()) {
                boolean process = processLeading.contains(li);
                if (!isEndingGapUsable(li, dimension, LEADING, process, contentResizing)) {
                    // default gap that would not work
                    layoutModel.removeInterval(gap);
                    gap = null;
                }
                if (gap != null && process) {
                    if (isEndingGapEffective(li, dimension, LEADING)) {
                        if (gap.getPreferredSize() == NOT_EXPLICITLY_DEFINED) {
                            // default padding to be used as common gap
                            defaultLeadingPadding = true;
                            leadingPadding = gap.getPaddingType();
                        } else {
                            effectiveExplicitGapLeading = true;
                        }
                        if (commonGapLeadingSize == Integer.MIN_VALUE) {
                            commonGapLeadingSize = (gap.getMinimumSize() != USE_PREFERRED_SIZE)
                                    ? gap.getMinimumSize() : gap.getPreferredSize();
                        }
                    }
                    if (gap.getMaximumSize() >= Short.MAX_VALUE) {
                        if (anyAlignedLeading) {
                            reduceToZeroGapsLeading.add(gap); // will change to zero gap instead
                            gap = null;
                        } else {
                            if (li.getAlignment() == LEADING) { // need to change alignment as we removed resizing gap
                                layoutModel.setIntervalAlignment(li, TRAILING);
                            }
                            resizingGapLeading = true;
                            if (gap.getPreferredSize() == 0) {
                                zeroGapLeading = gap;
                            }
                        }
                    }
                    if (gap != null) {
                        layoutModel.removeInterval(gap);
                        validLeadingGapRemoved = true;
                    }
                }
            }

            gap = li.getSubInterval(li.getSubIntervalCount() - 1);
            if (gap.isEmptySpace()) {
                boolean process = processTrailing.contains(li);
                if (!isEndingGapUsable(li, dimension, TRAILING, process, contentResizing)) {
                    // default gap that would not work
                    layoutModel.removeInterval(gap);
                    gap = null;
                }
                if (gap != null && process) {
                    if (isEndingGapEffective(li, dimension, TRAILING)) {
                        if (gap.getPreferredSize() == NOT_EXPLICITLY_DEFINED) {
                            // default padding to be used as common gap
                            defaultTrailingPadding = true;
                            trailingPadding = gap.getPaddingType();
                        } else {
                            effectiveExplicitGapTrailing = true;
                        }
                        if (commonGapTrailingSize == Integer.MIN_VALUE) {
                            commonGapTrailingSize = (gap.getMinimumSize() != USE_PREFERRED_SIZE)
                                    ? gap.getMinimumSize() : gap.getPreferredSize();
                        }
                    }
                    if (gap.getMaximumSize() >= Short.MAX_VALUE) {
                        if (anyAlignedTrailing) {
                            reduceToZeroGapsTrailing.add(gap); // will change to zero gap instead
                            gap = null;
                        } else {
                            if (li.getAlignment() == TRAILING) { // need to change alignment as we removed resizing gap
                                layoutModel.setIntervalAlignment(li, LEADING);
                            }
                            resizingGapTrailing = true;
                            if (gap.getPreferredSize() == 0) {
                                zeroGapTrailing = gap;
                            }
                        }
                    }
                    if (gap != null) {
                        layoutModel.removeInterval(gap);
                        validTrailingGapRemoved = true;
                    }
                }
            }

            if (li.getSubIntervalCount() == 1) {
                // only one interval remained in sequence - cancel the sequence
                layoutModel.removeInterval(group, i); // removes li from group
                LayoutInterval sub = layoutModel.removeInterval(li, 0); // removes last interval from li
                layoutModel.setIntervalAlignment(sub, li.getRawAlignment());
                layoutModel.addInterval(sub, group, i);
                if (processLeading.contains(li)) {
                    processLeading.intervals.remove(li);
                    processLeading.intervals.add(sub);
                }
                if (processTrailing.contains(li)) {
                    processTrailing.intervals.remove(li);
                    processTrailing.intervals.add(sub);
                }
                if (sub.isParallel()) {
                    mayNeedSecondPass = true;
                }
            }
        }
        if (!validLeadingGapRemoved && !validTrailingGapRemoved) {
            return -1;
        }

        if ((resizingGapLeading || resizingGapTrailing)
            && (!LayoutInterval.canResize(group) || contentResizing)) {
            // removed a resizing gap, but it should be fixed when out of the group
            if (!subGroupLeading) {
                resizingGapLeading = false;
            }
            if (!subGroupTrailing) {
                resizingGapTrailing = false;
            }
            if (!contentResizing) { // after removing resizing gaps the group with suppressed resizing has only fixed content
                enableGroupResizing(group);
            }
        }

        // 3) Create new L and T gaps to substitute removed gaps.
        int[] groupOuterPos = group.getCurrentSpace().positions[dimension];
        assert groupOuterPos[LEADING] > Short.MIN_VALUE && groupOuterPos[TRAILING] > Short.MIN_VALUE;
        int groupInnerPosLeading = processLeading.count() > 0 ?
                LayoutUtils.getPositionWithoutGap(processLeading.intervals, dimension, LEADING) :
                groupOuterPos[LEADING];
        int groupInnerPosTrailing = processTrailing.count() > 0 ?
                LayoutUtils.getPositionWithoutGap(processTrailing.intervals, dimension, TRAILING) :
                groupOuterPos[TRAILING];

        LayoutInterval leadingGap = null;
        LayoutInterval trailingGap = null;
        if (validLeadingGapRemoved) {
            if (!anyAlignedLeading) { // group is open at leading edge
                int size = groupInnerPosLeading - groupOuterPos[LEADING];
                if (size > 0 || defaultLeadingPadding) {
                    leadingGap = new LayoutInterval(SINGLE);
                    if (defaultLeadingPadding) {
                        leadingGap.setPaddingType(leadingPadding);
                    } else if (effectiveExplicitGapLeading) {
                        leadingGap.setPreferredSize(size);
                        if (resizingGapLeading) {
                            if (size < 0 || commonGapLeadingSize < 0 || commonGapLeadingSize <= size) {
                                leadingGap.setMinimumSize(commonGapLeadingSize);
                            }
                        } else if (size != NOT_EXPLICITLY_DEFINED) {
                            leadingGap.setMinimumSize(USE_PREFERRED_SIZE);
                            leadingGap.setMaximumSize(USE_PREFERRED_SIZE);
                        }
                    }
                    if (resizingGapLeading) {
                        leadingGap.setMaximumSize(Short.MAX_VALUE);
                    }
                    leadingGap.setAttribute(LayoutInterval.ATTR_FLEX_SIZEDEF);
                } else if (size == 0) {
                    leadingGap = zeroGapLeading;
                }
            } else {
                leadingGap = new LayoutInterval(SINGLE);
                leadingGap.setSize(commonGapLeadingSize);
                if (commonGapLeadingSize == DEFAULT) {
                    leadingGap.setPaddingType(leadingPadding);
                }
                if (!reduceToZeroGapsLeading.isEmpty()) {
                    int sizeDiff = groupInnerPosLeading - groupOuterPos[LEADING];
                    for (LayoutInterval gap : reduceToZeroGapsLeading) {
                        int gapSize = gap.getPreferredSize() - sizeDiff;
                        if (gapSize < 0) {
                            gapSize = 0;
                        }
                        layoutModel.setIntervalSize(gap, 0, gapSize, Short.MAX_VALUE);
                    }
                }
            }
        }
        if (validTrailingGapRemoved) {
            if (!anyAlignedTrailing) { // group is open at trailing edge
                int size = groupOuterPos[TRAILING] - groupInnerPosTrailing;
                if (size > 0 || defaultTrailingPadding) {
                    trailingGap = new LayoutInterval(SINGLE);
                    if (defaultTrailingPadding) {
                        trailingGap.setPaddingType(trailingPadding);
                    } else if (effectiveExplicitGapTrailing) {
                        trailingGap.setPreferredSize(size);
                        if (resizingGapTrailing) {
                            if (size < 0 || commonGapTrailingSize < 0 || commonGapTrailingSize <= size) {
                                trailingGap.setMinimumSize(commonGapTrailingSize);
                            }
                        } else if (size != NOT_EXPLICITLY_DEFINED) {
                            trailingGap.setMinimumSize(USE_PREFERRED_SIZE);
                            trailingGap.setMaximumSize(USE_PREFERRED_SIZE);
                        }
                    }
                    if (resizingGapTrailing) {
                        trailingGap.setMaximumSize(Short.MAX_VALUE);
                    }
                    trailingGap.setAttribute(LayoutInterval.ATTR_FLEX_SIZEDEF);
                } else if (size == 0) {
                    trailingGap = zeroGapTrailing;
                }
            } else {
                trailingGap = new LayoutInterval(SINGLE);
                trailingGap.setSize(commonGapTrailingSize);
                if (commonGapTrailingSize == DEFAULT) {
                    trailingGap.setPaddingType(trailingPadding);
                }
                if (!reduceToZeroGapsTrailing.isEmpty()) {
                    int sizeDiff = groupOuterPos[TRAILING] - groupInnerPosTrailing;
                    for (LayoutInterval gap : reduceToZeroGapsTrailing) {
                        int gapSize = gap.getPreferredSize() - sizeDiff;
                        if (gapSize < 0) {
                            gapSize = 0;
                        }
                        layoutModel.setIntervalSize(gap, 0, gapSize, Short.MAX_VALUE);
                    }
                }
            }
        }

        // 4) Place the L/T subst. gaps outside the group, or create sub-group.
        if ((leadingGap != null && subGroupLeading)
               || (trailingGap != null && subGroupTrailing)) {
            // have a gap to put next to a subgroup (stays inside 'group')
            LayoutInterval subGroup = new LayoutInterval(PARALLEL);
            subGroup.setGroupAlignment(group.getGroupAlignment());
            int commonAlignment = -1;
            for (int i=0; i < group.getSubIntervalCount();) {
                LayoutInterval li = group.getSubInterval(i);
                if ((subGroupLeading && processLeading.contains(li))
                        || (subGroupTrailing && processTrailing.contains(li))) {
                    int align = li.getAlignment();
                    if (commonAlignment == -1) {
                        commonAlignment = align;
                    } else if (align != commonAlignment) {
                        commonAlignment = LayoutRegion.ALL_POINTS;
                    }
                    layoutModel.removeInterval(group, i);
                    layoutModel.addInterval(li, subGroup, -1);
                } else {
                    i++;
                }
            }
            LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
            if (subGroupLeading && leadingGap != null) {
                layoutModel.addInterval(leadingGap, seq, -1);
                leadingGap = null;
            }
            seq.add(subGroup, -1);
            if (subGroupTrailing && trailingGap != null) {
                layoutModel.addInterval(trailingGap, seq, -1);
                trailingGap = null;
            }
            layoutModel.addInterval(seq, group, -1);
            if (commonAlignment != LayoutRegion.ALL_POINTS
                    && seq.getAlignment() != commonAlignment) {
                seq.setAlignment(commonAlignment);
            }
            int pos[] = subGroup.getCurrentSpace().positions[dimension];
            pos[LEADING] = groupInnerPosLeading;
            pos[TRAILING] = groupInnerPosTrailing;
            pos[CENTER] = (groupInnerPosLeading + groupInnerPosTrailing) / 2;
        }

        boolean someGapOutsideGroup = (leadingGap != null || trailingGap != null);
        boolean originalRootGroup = group.getParent() == null;
        if (someGapOutsideGroup && !originalRootGroup) {
            if (leadingGap != null) {
                groupOuterPos[LEADING] = groupInnerPosLeading;
            }
            if (trailingGap != null) {
                groupOuterPos[TRAILING] = groupInnerPosTrailing;
            }
            groupOuterPos[CENTER] = (groupOuterPos[LEADING] + groupOuterPos[TRAILING]) / 2;
        }
        if (leadingGap != null) {
            group = insertGap(leadingGap, group, groupInnerPosLeading, dimension, LEADING);
        }
        if (trailingGap != null) {
            group = insertGap(trailingGap, group, groupInnerPosTrailing, dimension, TRAILING);
        }
        if (someGapOutsideGroup && originalRootGroup && group.getParent() != null) {
            group.getCurrentSpace().set(dimension, groupInnerPosLeading, groupInnerPosTrailing);
        }

        int idx = (someGapOutsideGroup && group.getParent() != null)
                ? group.getParent().indexOf(group) : -1;

        if (mayNeedSecondPass) {
            int count = group.getSubIntervalCount();
            mergeParallelGroups(group);
            if (group.getSubIntervalCount() > count) {
                idx = optimizeGaps(group, dimension);
            }
        }
        return idx;
    }

    private static class IntervalSet {
        Set<LayoutInterval> intervals;
        boolean resizing;

        void add(LayoutInterval li, boolean res) {
            if (intervals == null) {
                intervals = new HashSet<LayoutInterval>();
            }
            intervals.add(li);
            if (res) {
                resizing = true;
            }
        }

        void add(IntervalSet is) {
            if (is.count() > 0) {
                if (intervals == null) {
                    intervals = new HashSet<LayoutInterval>();
                }
                intervals.addAll(is.intervals);
                if (is.resizing) {
                    resizing = true;
                }
            }
        }

        int count() {
            return intervals != null ? intervals.size() : 0;
        }

        Collection<LayoutInterval> intervals() {
            if (intervals != null) {
                return intervals;
            }
            return Collections.emptyList();
        }

        boolean contains(LayoutInterval interval) {
            return intervals != null ? intervals.contains(interval) : false;
        }

        void clear() {
            intervals = null;
            resizing = false;
        }
    }

    private static IntervalSet[] countAlignedVariants(IntervalSet[] alignedGaps,
                                                      IntervalSet[] unalignedFixedGaps,
                                                      IntervalSet[] unalignedResGaps) {
        IntervalSet[] sets = new IntervalSet[2];
        int[] alignedGapSizes = new int[2];
        for (int a=LEADING; a <= TRAILING; a++) {
            int gapSize = Integer.MIN_VALUE;
            for (LayoutInterval li : alignedGaps[a].intervals()) {
                LayoutInterval gap = li.getSubInterval(a==LEADING ? 0 : li.getSubIntervalCount()-1);
                if (gapSize == Integer.MIN_VALUE) {
                    gapSize = gap.getPreferredSize();
                } else if (gap.getPreferredSize() != gapSize) {
                    gapSize = Integer.MIN_VALUE;
                    break;
                }
            }
            IntervalSet iSet = new IntervalSet();
            if (gapSize != Integer.MIN_VALUE) {
                iSet.add(alignedGaps[a]);
                for (LayoutInterval li : unalignedFixedGaps[a].intervals()) {
                    LayoutInterval gap = li.getSubInterval(a==LEADING ? 0 : li.getSubIntervalCount()-1);
                    if (gap.getPreferredSize() == gapSize) {
                        iSet.add(li, false);
                    }
                }
            }
            alignedGapSizes[a] = gapSize;
            sets[a] = iSet;
        }

        for (int a=LEADING; a <= TRAILING; a++) {
            int gapSize = alignedGapSizes[a];
            if (gapSize == Integer.MIN_VALUE || !sets[a].resizing || !PREFER_ZERO_GAPS) {
                continue;
            }
            // Allow resizing gaps to combine with aligned gaps (if they
            // have same min size, typically default). A common fixed gap
            // will be separated out of the group, and min. size of the
            // resizing gap set to 0.
            for (LayoutInterval li : unalignedResGaps[a].intervals()) {
                LayoutInterval gap = li.getSubInterval(a==LEADING ? 0 : li.getSubIntervalCount()-1);
                int minSize = gap.getMinimumSize();
                if (minSize == USE_PREFERRED_SIZE) {
                    minSize = gap.getPreferredSize();
                }
                if (minSize == gapSize) {
                    boolean add = true;
                    if (SYMETRIC_ZERO_GAPS) { // do that only if there's not an opposite gap that is not going to combine
                        LayoutInterval otherGap = li.getSubInterval(a==LEADING ? li.getSubIntervalCount()-1 : 0);
                        if (otherGap.isEmptySpace()) { // we have a gap on the opposite side
                            if (sets[a^1].count() == 0 || (sets[a^1].count() == 1 && sets[a^1].intervals.contains(li))) {
                                add = false; // there is no aligned gap on the other side to combine with
                            } else {
                                int otherMinSize = otherGap.getMinimumSize();
                                if (otherMinSize == USE_PREFERRED_SIZE) {
                                    otherMinSize = otherGap.getPreferredSize();
                                }
                                if (otherMinSize != alignedGapSizes[a^1]) {
                                    add = false; // the gap on the other side is different, not going to combine
                                }
                            }
                        }
                    }
                    if (add) {
                        sets[a].add(li, true);
                    }
                }
            }
        }
        return sets;
    }

    /** For experiments - controls how gaps at the unaligned side are treated.
     * Strictly they should be extracted only if all intervals have them. But
     * there are operations that add them unnecessarily, so there are often
     * mixed groups that would not be fixed. Also extracting these gaps has
     * no effect on current static layout. Historically any number is allowed. */
    private static final boolean allUnalignedGapsRequired = false;

    private static IntervalSet[] countUnalignedVariants(IntervalSet[] unalignedFixedGaps,
                                                        IntervalSet[] unalignedResGaps,
                                                        IntervalSet[] unalignedNoGaps,
                                                        int dimension) {
        IntervalSet[] sets = new IntervalSet[2];
        for (int a=LEADING; a <= TRAILING; a++) {
            IntervalSet zeroGaps = new IntervalSet();
            IntervalSet nonZeroGaps = new IntervalSet();
            for (LayoutInterval li : unalignedFixedGaps[a].intervals()) {
                LayoutInterval gap = li.getSubInterval(a==LEADING ? 0 : li.getSubIntervalCount()-1);
                if (gap.getMinimumSize() == 0) {
                    zeroGaps.add(li, false);
                } else {
                    nonZeroGaps.add(li, false);
                }
            }
            for (LayoutInterval li : unalignedResGaps[a].intervals()) {
                LayoutInterval gap = li.getSubInterval(a==LEADING ? 0 : li.getSubIntervalCount()-1);
                if (gap.getMinimumSize() == 0) {
                    zeroGaps.add(li, true);
                } else {
                    nonZeroGaps.add(li, true);
                }
            }
            IntervalSet iSet = new IntervalSet();
            iSet.add(nonZeroGaps.count() >= zeroGaps.count() ? nonZeroGaps : zeroGaps);
//            iSet.add(unalignedFixedGaps[a]);
//            iSet.add(unalignedResGaps[a]);
            if (!allUnalignedGapsRequired && unalignedNoGaps[a].count() > 0 && iSet.count() > 0) {
                // "no gaps" may be processed together with "unaligned gaps" with
                // one exception: when a "no gaps" interval spans the whole group
                // and so does also some interval with default ending gap
                boolean anyDefaultGap = false;
                for (LayoutInterval li : /*unalignedFixedGaps[a]*/iSet.intervals()) {
                    LayoutInterval gap = li.getSubInterval(a==LEADING ? 0 : li.getSubIntervalCount()-1);
                    if (gap.getPreferredSize() == NOT_EXPLICITLY_DEFINED
                            && LayoutInterval.isPlacedAtBorder(li, dimension, a)) {
                        anyDefaultGap = true;
                        break;
                    }
                }
                if (!anyDefaultGap) {
                    iSet.add(unalignedNoGaps[a]);
                } else {
                    for (LayoutInterval li : unalignedNoGaps[a].intervals()) {
                        if (!LayoutInterval.isPlacedAtBorder(li, dimension, a)) {
                            iSet.add(li, false);
                        }
                    }
                }
            }
            sets[a] = iSet;
        }
        return sets;
    }

    private boolean isEndingGapEffective(LayoutInterval seq, int dimension, int alignment) {
        assert seq.isSequential() && (alignment == LEADING || alignment == TRAILING);
        int idx = alignment == LEADING ? 0 : seq.getSubIntervalCount() - 1;
        LayoutInterval gap = seq.getSubInterval(idx);
        assert gap.isEmptySpace();
        int prefDistance = gap.getPreferredSize();
        if (LayoutInterval.canResize(gap) && prefDistance == NOT_EXPLICITLY_DEFINED
                && gap.hasAttribute(LayoutInterval.ATTR_SIZE_DIFF)) {
            return false;
        }
        if (LayoutInterval.isAlignedAtBorder(seq, alignment)) {
            return true;
        }

        int d = alignment == LEADING ? 1 : -1;
        LayoutInterval neighbor = seq.getSubInterval(idx+d);

        if (prefDistance == NOT_EXPLICITLY_DEFINED) {
            prefDistance = LayoutUtils.getSizeOfDefaultGap(gap, visualMapper);
        }
        int pos1 = neighbor.getCurrentSpace().positions[dimension][alignment];
        LayoutInterval outerNeighbor = LayoutInterval.getNeighbor(gap, alignment, false, true, false);
        int pos2;
        if (outerNeighbor != null) {
            if (outerNeighbor.isEmptySpace()) {
                if (seq.getParent() == null) {
                    return false; // can't say
                }
                pos2 = seq.getParent().getCurrentSpace().positions[dimension][alignment];
            } else {
                pos2 = outerNeighbor.getCurrentSpace().positions[dimension][alignment^1];
            }
        } else {
            pos2 = LayoutInterval.getRoot(seq).getCurrentSpace().positions[dimension][alignment];
        }
        int currentDistance = (pos1 - pos2) * d;
        return currentDistance <= prefDistance;
    }

    private boolean isEndingGapUsable(LayoutInterval seq, int dimension, int alignment,
                                      boolean toBeProcessed, boolean groupResizing) {
        assert seq.isSequential() && (alignment == LEADING || alignment == TRAILING);
        int idx = alignment == LEADING ? 0 : seq.getSubIntervalCount() - 1;
        LayoutInterval gap = seq.getSubInterval(idx);
        if (gap.getPreferredSize() == NOT_EXPLICITLY_DEFINED) {
            LayoutInterval neighbor = LayoutInterval.getNeighbor(gap, alignment, false, true, false);
            if (neighbor != null) {
                if (!LayoutUtils.isDefaultGapValidForNeighbor(neighbor, alignment^1)) {
                    return false;
                }
                if (!toBeProcessed) {
                    if (LayoutInterval.isAlignedAtBorder(seq, alignment)) {
                        LayoutInterval par = seq.getParent();
                        while (par.getParent() != null && par.getParent().isParallel()) {
                            par = par.getParent();
                        }
                        if (par != seq.getParent()
                                && !LayoutInterval.isAlignedAtBorder(seq, par, alignment)
                                && !LayoutInterval.isPlacedAtBorder(seq, par, dimension, alignment)) {
                            // aligned default gap that is not touching its neighbor is suspicious
                            return false;
                        }
                    } else if (!LayoutInterval.canResize(gap) && groupResizing) { // unaligned default gap at group edge can be problematic
                        setIntervalResizing(gap, true); // in resizing group it can also be resizing
                    }
                }
            }
        } else if (gap.getPreferredSize() == 0) {
            if (!LayoutInterval.canResize(gap)) {
                return false;
            }
            for (int i=0; i < seq.getSubIntervalCount(); i++) {
                LayoutInterval sub = seq.getSubInterval(i);
                if (sub != gap && !sub.isEmptySpace() && LayoutInterval.wantResize(sub)) {
                    return false; // resizing zero gap in resizing sequence does not make sense
                                  // (see bug 202636, attachment 111677)
                }
            }
        }
        return true;
    }

    /**
     * Optimizes edge gaps within a parallel group in respect to neighbor gaps
     * out of the group. (The optimizeGaps method only cares about the edge gaps
     * inside the group.)
     * Some situations are eliminated where edge gaps in a group would have a
     * neighbor gap right next to the group. It's undesirable when there are no
     * other intervals defining the group edge.
     * @param group
     * @param dimension
     */
    void optimizeGaps2(LayoutInterval group, int dimension) {
        LayoutInterval lGroup = null, tGroup = null;
        LayoutInterval lGap = LayoutInterval.getNeighbor(group, LEADING, false, true, false);
        if (lGap != null && lGap.isEmptySpace()) {
            lGroup = LayoutInterval.getDirectNeighbor(lGap, TRAILING, true);
        }
        LayoutInterval tGap = LayoutInterval.getNeighbor(group, TRAILING, false, true, false);
        if (tGap != null && tGap.isEmptySpace()) {
            tGroup = LayoutInterval.getDirectNeighbor(tGap, LEADING, true);
        }
        if (lGroup != null && lGroup == tGroup) {
            eliminateEndingGaps(lGroup, new LayoutInterval[] { lGap, tGap }, dimension);
        } else {
            if (lGroup != null && (tGroup == null || tGroup.isParentOf(lGroup))) {
                eliminateEndingGaps(lGroup, new LayoutInterval[] { lGap, null }, dimension);
                if (tGroup != null) {
                    lGap = LayoutInterval.getDirectNeighbor(tGroup, LEADING, false);
                    if (lGap != null && !lGap.isEmptySpace()) {
                        lGap = null;
                    }
                    eliminateEndingGaps(tGroup, new LayoutInterval[] { lGap, tGap }, dimension);
                }
            } else if (tGroup != null) {
                assert lGroup == null || lGroup.isParentOf(tGroup);
                eliminateEndingGaps(tGroup, new LayoutInterval[] { null, tGap }, dimension);
                if (lGroup != null) {
                    tGap = LayoutInterval.getDirectNeighbor(lGroup, TRAILING, false);
                    if (tGap != null && !tGap.isEmptySpace()) {
                        tGap = null;
                    }
                    eliminateEndingGaps(lGroup, new LayoutInterval[] { lGap, tGap }, dimension);
                }
            }
        }
    }

    void eliminateEndingGaps(LayoutInterval group, LayoutInterval[] outGaps, int dimension) {
        IntervalSet[] alignedGap = new IntervalSet[2];
        IntervalSet[] alignedNoGap = new IntervalSet[2];
        IntervalSet[] unalignedGap = new IntervalSet[2];
        int inPos[] = new int[2];
        int outPos[] = new int[2];

        // Analyze placement and alignment of ending gaps.
        for (int e = LEADING; e <= TRAILING; e++) {
            alignedGap[e] = new IntervalSet();
            alignedNoGap[e] = new IntervalSet();
            unalignedGap[e] = new IntervalSet();
            if (outGaps[e] != null) {
                Iterator<LayoutInterval> it = group.getSubIntervals();
                while (it.hasNext()) {
                    LayoutInterval li = it.next();
                    determineEndings(li, dimension, e, alignedGap[e], alignedNoGap[e], unalignedGap[e]);
                }
                inPos[e] = group.getCurrentSpace().positions[dimension][e];
                outPos[e] = LayoutUtils.getVisualPosition(outGaps[e], dimension, e);
            }
        }

        // Determine which sides of the group deserves changes of the ending gaps.
        boolean independentEdges = unalignedGap[LEADING].count() > 0 && unalignedGap[TRAILING].count() > 0
                && unalignedGap[LEADING].count() + unalignedGap[TRAILING].count() == group.getSubIntervalCount();
        boolean[] edgeNotDefined = new boolean[2];
        boolean[] allGaps = new boolean[2];
        boolean[] allGapsToReduce = new boolean[2];
        for (int e = LEADING; e <= TRAILING; e++) {
            if (outGaps[e] != null) {
                edgeNotDefined[e] = (alignedNoGap[e].count() == 0);
                allGaps[e] = (alignedGap[e].count() + unalignedGap[e].count() == group.getSubIntervalCount());
                allGapsToReduce[e] = (unalignedGap[e].count() == group.getSubIntervalCount())
                        || (allGaps[e] && !LayoutInterval.canResize(group));
            }
        }
        boolean processEdge[] = new boolean[2];
        for (int e = LEADING; e <= TRAILING; e++) {
            if (outGaps[e] != null) {
                if (LayoutInterval.wantResize(outGaps[e]) && !LayoutInterval.canResize(group)
                        && (!edgeNotDefined[e] || !allGaps[e])) {
                    processEdge[e] = false;
                } else if (edgeNotDefined[e]) {
                    processEdge[e] = alignedGap[e].count() > 0 || unalignedGap[e].count() > 0;
                } else if (unalignedGap[e].count() > 0
                        && (edgeNotDefined[e^1] || unalignedGap[e^1].count() == 0 || independentEdges)
                        && (!LayoutInterval.wantResize(outGaps[e]) || !LayoutInterval.wantResize(group))) {
                    // chance to eliminate the unaligned gaps
                    if (PREFER_ZERO_GAPS) { // keep if only resizing zero gaps
                        for (LayoutInterval li : unalignedGap[e].intervals) {
                            LayoutInterval gap = li.getSubInterval(e==LEADING ? 0 : li.getSubIntervalCount()-1);
                            if (gap.getMinimumSize() != 0 || gap.getMaximumSize() != Short.MAX_VALUE) {
                                processEdge[e] = true;
                                break;
                            }
                        }
                    } else { // don't want keep zero gaps, eliminate all unaligned gaps
                        processEdge[e] = true;
                    }
                }
            }
        }
        if (!processEdge[LEADING] && !processEdge[TRAILING]) {
            return;
        }

        // Create new groups and move relevant intervals with ending gaps into them.
        Collection<LayoutInterval>[] processIntervals = new Collection[2];
        LayoutInterval[] newGroups = new LayoutInterval[2];
        for (int i=0; i < group.getSubIntervalCount(); i++) {
            LayoutInterval li = group.getSubInterval(i);
            for (int e = LEADING; e <= TRAILING; e++) {
                if (!processEdge[e]) {
                    continue;
                }
                if (alignedGap[e].contains(li) || unalignedGap[e].contains(li)) {
                    LayoutInterval newGroup = newGroups[e];
                    if (newGroup == null) {
                        if (!independentEdges && newGroups[e^1] != group) { // need just one new group
                            newGroup = newGroups[e^1];
                        }
                        if (newGroup == null) {
                            newGroup = allGaps[e] ? group : new LayoutInterval(PARALLEL);
                        }
                        newGroups[e] = newGroup;
                    }
                    if (newGroup != group && (processIntervals[e^1] == null || !processIntervals[e^1].contains(li))) {
                        if (processIntervals[e] == null) {
                            processIntervals[e] = new ArrayList(group.getSubIntervalCount());
                        }
                        processIntervals[e].add(li);
                    }
                }
            }
        }
        if (!independentEdges && processIntervals[LEADING] != null && processIntervals[TRAILING] != null
                && processIntervals[LEADING].size() + processIntervals[TRAILING].size() == group.getSubIntervalCount()) {
            // all intervals to be processed after all (combining both edges), keep the original group
            newGroups[LEADING] = newGroups[TRAILING] = group;
        } else {
            for (int e = LEADING; e <= TRAILING; e++) {
                if (processIntervals[e] != null) {
                    for (LayoutInterval li : processIntervals[e]) {
                        assert li.getParent() == group;
                        layoutModel.removeInterval(li);
                        layoutModel.addInterval(li, newGroups[e], -1);
                    }
                }
            }
        }

        // Add the groups in parallel with the outer gaps.
        LayoutInterval parentSeq = group.getParent();
        if (independentEdges && !allGaps[LEADING] && !allGaps[TRAILING]
                && processEdge[LEADING] && processEdge[TRAILING]) {
            assert group.getSubIntervalCount() == 0;
            LayoutInterval superGroup = new LayoutInterval(PARALLEL);
            superGroup.getCurrentSpace().set(dimension, outPos[LEADING], outPos[TRAILING]);
            for (int e = LEADING; e <= TRAILING; e++) {
                LayoutInterval sideGroup = newGroups[e];
                LayoutInterval outGap = outGaps[e^1];
                assert sideGroup != null && sideGroup != group && outGap.getParent() == parentSeq;
                LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
                layoutModel.removeInterval(outGap);
                if (superGroup.getParent() == null) {
                    int idx = layoutModel.removeInterval(group);
                    layoutModel.addInterval(superGroup, parentSeq, idx);
                }
                if (e == TRAILING) {
                    layoutModel.addInterval(outGap, seq, 0);
                    sideGroup.getCurrentSpace().set(dimension, inPos[LEADING], outPos[TRAILING]);
                }
                layoutModel.addInterval(sideGroup, seq, -1);
                if (e == LEADING) {
                    layoutModel.addInterval(outGap, seq, -1);
                    sideGroup.getCurrentSpace().set(dimension, outPos[LEADING], inPos[TRAILING]);
                }
                layoutModel.addInterval(seq, superGroup, -1);
            }
            for (int e = LEADING; e <= TRAILING; e++) {
                if (newGroups[e].getSubIntervalCount() == 1) {
                    dissolveRedundantGroup(newGroups[e]);
                }
            }
        } else {
            LayoutInterval subSeq = null;
            for (int e = LEADING; e <= TRAILING; e++) {
                LayoutInterval superGroup = newGroups[e];
                LayoutInterval outGap = outGaps[e];
                if (superGroup != null) {
                    if (superGroup != group) {
                        assert outGap.getParent() == parentSeq;
                        boolean groupAlreadyAdded;
                        if (subSeq == null) {
                            subSeq = new LayoutInterval(SEQUENTIAL);
                            groupAlreadyAdded = false;
                        } else {
                            groupAlreadyAdded = true;
                        }
                        layoutModel.removeInterval(outGap);
                        if (e == LEADING) {
                            layoutModel.addInterval(outGap, subSeq, 0);
                        }
                        if (!groupAlreadyAdded) {
                            int idx = layoutModel.removeInterval(group);
                            int subAlign = -1;
                            if (group.getSubIntervalCount() > 1) {
                                layoutModel.addInterval(group, subSeq, -1);
                            } else {
                                assert group.getSubIntervalCount() == 1;
                                LayoutInterval li = group.getSubInterval(0);
                                subAlign = li.getAlignment(); // follow alignment of the last interval
                                layoutModel.removeInterval(li);
                                if (li.isSequential()) {
                                    addContent(subSeq, li, 0);
                                    subSeq = li;
                                } else {
                                    addContent(li, subSeq, -1);
                                }
                            }
                            layoutModel.addInterval(subSeq, superGroup, -1);
                            if (subAlign != -1 && subSeq.getAlignment() != subAlign) {
                                subSeq.setAlignment(subAlign);
                            }
                            layoutModel.addInterval(superGroup, parentSeq, idx);
                            superGroup.getCurrentSpace().set(dimension,
                                newGroups[LEADING] != null ? outPos[LEADING] : inPos[LEADING],
                                newGroups[TRAILING] != null ? outPos[TRAILING] : inPos[TRAILING]);
                        }
                        if (e == TRAILING) {
                            layoutModel.addInterval(outGap, subSeq, -1);
                        }
                    } else if (allGapsToReduce[e]) {
                        group.getCurrentSpace().positions[dimension][e] = 
                            LayoutUtils.getOutermostComponent(group, dimension, e).getCurrentSpace().positions[dimension][e];
                    } else {
                        layoutModel.removeInterval(outGap);
                        group.getCurrentSpace().positions[dimension][e] = outPos[e];
                    }
                }
            }
        }
        if (parentSeq.getSubIntervalCount() == 1) {
            // outer gaps moved inside, only the new group left - don't need the sequence
            LayoutInterval superParent = parentSeq.getParent();
            dissolveRedundantGroup(parentSeq);
            // also the parallel group we created might have been eliminated
            for (int e = LEADING; e <= TRAILING; e++) {
                if (newGroups[e] != null && newGroups[e].getParent() == null) {
                    newGroups[e] = superParent;
                }
            }
        }

        // Adjust ending gaps, now in parallel with the outer gaps
        for (int e = LEADING; e <= TRAILING; e++) {
            if (newGroups[e] != null) {
                if (newGroups[e] != group || !allGapsToReduce[e]) {
                    LayoutInterval gapCopy = LayoutInterval.cloneInterval(outGaps[e], null);
                    for (Iterator<LayoutInterval> it=newGroups[e].getSubIntervals(); it.hasNext(); ) {
                        LayoutInterval li = it.next();
                        if (independentEdges || alignedGap[e].contains(li) || unalignedGap[e].contains(li)) {
                            extendWithGap(li, gapCopy, dimension, e);
                        }
                    }
                } else { // just reducing all side gaps in a group with suppressed resizing
                    int reduceSize = (inPos[e] - group.getCurrentSpace().positions[dimension][e]) * (e==LEADING ? -1:1);
                    if (reduceSize >= 0) {
                        boolean gapsResizing = false;
                        for (LayoutInterval gap : LayoutUtils.getSideSubIntervals(group, e, false, true, true, false)) {
                            boolean resizing = LayoutInterval.canResize(gap) && LayoutInterval.canResize(group);
                            if (gap.getPreferredSize() == NOT_EXPLICITLY_DEFINED || resizing
                                    || !LayoutInterval.isAlignedAtBorder(gap, LayoutInterval.getFirstParent(gap, PARALLEL), e)) {
                                layoutModel.removeInterval(gap);
                            } else {
                                LayoutInterval neighbor = LayoutInterval.getNeighbor(gap, e^1, true, false, true);
                                if (neighbor != null) {
                                    int currentGapSize = (inPos[e] - neighbor.getCurrentSpace().positions[dimension][e]) * (e==LEADING ? -1:1);
                                    if (currentGapSize > 0) {
                                        if (currentGapSize > reduceSize) {
                                            resizeInterval(gap, currentGapSize - reduceSize);
                                        } else {
                                            layoutModel.removeInterval(gap);
                                        }
                                    }
                                }
                            }
                            if (resizing) {
                                gapsResizing = true;
                            }
                        }
                        if (gapsResizing && !LayoutInterval.canResize(outGaps[e])) {
                            setIntervalResizing(outGaps[e], true);
                        }
                        int adjustedOutGapSize = ((outPos[e] - inPos[e]) * (e==LEADING ? -1:1)) + reduceSize;
                        resizeInterval(outGaps[e], adjustedOutGapSize);
                    }
                }
            }
        }
    }

    private void determineEndings(LayoutInterval interval, int dimension, int alignment,
                                  IntervalSet alignedGap, IntervalSet alignedNoGap, IntervalSet unalignedGap) {
        boolean alignedFound = false;
        boolean gapFound = false;
        boolean onEdgeWithoutGapFound = false;

        List<LayoutInterval> list = new LinkedList<LayoutInterval>();
        list.add(interval);
        while (!list.isEmpty()) {
            LayoutInterval li = list.remove(0);
            if (li.isSequential()) {
                for (int i=0; i < li.getSubIntervalCount(); i++) {
                    LayoutInterval sub = li.getSubInterval(i);
                    boolean edge = (alignment == LEADING && i == 0) || (alignment == TRAILING && i == li.getSubIntervalCount()-1);
                    if (edge) {
                        if (LayoutInterval.isAlignedAtBorder(sub, interval.getParent(), alignment)//li == interval && li.getAlignment() == alignment
                                && (!sub.isEmptySpace() || !LayoutInterval.wantResize(sub))) {
                            alignedFound = true;
                        }
                        if (sub.isEmptySpace()) {
                            gapFound = true;
                        } else {
                            list.add(sub);
                        }
                    }
                    if ((!edge || !sub.isEmptySpace()) && !alignedFound
                            && LayoutInterval.wantResizeInParent(sub, interval)) {
                        alignedFound = true;
                    }
                }
            } else {
                if (li == interval && li.getAlignment() == alignment) {
                    alignedFound = true;
                }
                if (li.isParallel()) {
                    for (int i=0; i < li.getSubIntervalCount(); i++) {
                        LayoutInterval sub = li.getSubInterval(i);
                        list.add(sub);
                    }
                } else {
                    if (!alignedFound && LayoutInterval.wantResizeInParent(li,
                            (li == interval) ? interval.getParent() : interval)) {
                        alignedFound = true;
                    }
                    if (!li.isEmptySpace()) {
                        int borderPos = LayoutInterval.getFirstParent(li, PARALLEL)
                                .getCurrentSpace().positions[dimension][alignment];
                        if (li.getCurrentSpace().positions[dimension][alignment] == borderPos) {
                            onEdgeWithoutGapFound = true;
                        }
                    }
                }
            }
        }
        if (alignedFound) {
            if (gapFound && !onEdgeWithoutGapFound) {
                alignedGap.add(interval, LayoutInterval.wantResize(interval));
            } else {
                alignedNoGap.add(interval, LayoutInterval.wantResize(interval));
            }
        } else if (gapFound && !onEdgeWithoutGapFound) {
            unalignedGap.add(interval, LayoutInterval.wantResize(interval));
        }
    }

    private void extendWithGap(LayoutInterval interval, LayoutInterval gap, int dimension, int alignment) {
        List<LayoutInterval> list = new LinkedList<LayoutInterval>();
        list.add(interval);
        while (!list.isEmpty()) {
            LayoutInterval li = list.remove(0);
            if (li.isSequential()) {
                int i = (alignment == LEADING) ? 0 : li.getSubIntervalCount()-1;
                LayoutInterval sub = li.getSubInterval(i);
                if (sub.isEmptySpace()) {
                    int d = (alignment == LEADING) ? 1 : -1;
                    int gapSize = (li.getSubInterval(i+d).getCurrentSpace().positions[dimension][alignment]
                                   - interval.getParent().getCurrentSpace().positions[dimension][alignment]) * d;
                    eatGap(sub, gap, gapSize);
                } else {
                    list.add(sub);
                }
            } else if (li.isParallel()) {
                for (int i=0; i < li.getSubIntervalCount(); i++) {
                    LayoutInterval sub = li.getSubInterval(i);
                    list.add(sub);
                }
            }
        }
    }

    boolean cutStartingGap(LayoutInterval group, int size, int dimension, int alignment) {
        assert group.isGroup() && size > 0 && (alignment == LEADING || alignment == TRAILING);
        // [just very simple impl. for now - considering just one sequence...]
        LayoutInterval seq = null;
        if (group.isSequential()) {
            seq = group;
        }
        else if (group.getSubIntervalCount() == 1) {
            LayoutInterval li = group.getSubInterval(0);
            if (li.isSequential() && LayoutInterval.isAlignedAtBorder(li, alignment))
                seq = li;
        }
        if (seq != null && seq.getSubIntervalCount() > 1) {
            LayoutInterval gap = seq.getSubInterval(alignment == LEADING ? 0 : seq.getSubIntervalCount()-1);
            LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(gap, alignment^1, true);
            if (gap != null && gap.isEmptySpace() && neighbor != null) {
                int currentSize = gap.getPreferredSize();
                if (currentSize == NOT_EXPLICITLY_DEFINED) {
                    currentSize = LayoutRegion.distance(group.getCurrentSpace(), neighbor.getCurrentSpace(),
                                                        dimension, alignment, alignment)
                                  * (alignment == TRAILING ? -1 : 1);
                }
                if (currentSize >= size) {
                    if (currentSize > size)
                        resizeInterval(gap, currentSize - size);
                    else
                        layoutModel.removeInterval(gap);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Inserts a gap before or after specified interval. If in a sequence, the
     * method takes care about merging gaps if there is already some as neighbor.
     * Expects the actual positions of the sequence are up-to-date.
     * @param gap the gap to be inserted
     * @param interval the interval before or after which the gap is added
     * @param pos expected real position of the end of the interval where the gap
     *        is added (need not correspond to that stored in the interval)
     * @param dimension
     * @param alignment at which side of the interval the gap is added
     * (LEADING - before, TRAILING - after)
     */
    LayoutInterval insertGap(LayoutInterval gap, LayoutInterval interval, int pos, int dimension, int alignment) {
        assert alignment == LEADING || alignment == TRAILING;
        assert gap.isEmptySpace();

        boolean parentPos = false;
        if (interval.isSequential()) {
            interval = interval.getSubInterval(alignment == LEADING ? 0 : interval.getSubIntervalCount()-1);
            if (interval.isEmptySpace()) {
                interval = LayoutInterval.getDirectNeighbor(interval, alignment^1, true);
                parentPos = true;
            }
        }

        LayoutInterval parent = interval.getParent();
        if (parent == null) {
            assert interval.isParallel();
            parent = interval;
            if (parent.getSubIntervalCount() > 1) {
                LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
                seq.getCurrentSpace().set(dimension,
                    (alignment == LEADING || LayoutInterval.canResize(gap)) ? pos : interval.getCurrentSpace().positions[dimension][LEADING],
                    (alignment == LEADING || LayoutInterval.canResize(gap)) ? interval.getCurrentSpace().positions[dimension][TRAILING] : pos);
                layoutModel.addInterval(seq, parent, -1);
                interval = new LayoutInterval(PARALLEL);
                interval.getCurrentSpace().set(dimension, parent.getCurrentSpace());
                layoutModel.addInterval(interval, seq, 0);
                while (parent.getSubIntervalCount() > 1) {
                    layoutModel.addInterval(layoutModel.removeInterval(parent, 0), interval, -1);
                }
                parent = seq;
            }
            else {
                interval = parent.getSubInterval(0);
                if (interval.isSequential()) {
                    parent = interval;
                    int subIdx = alignment == LEADING ? 0 : parent.getSubIntervalCount()-1;
                    interval = parent.getSubInterval(subIdx);
                    if (interval.isEmptySpace()) {
                        subIdx += alignment == LEADING ? 1 : -1;
                        LayoutInterval neighbor = subIdx >= 0 && subIdx < parent.getSubIntervalCount() ?
                                                  parent.getSubInterval(subIdx) : null;
                        int[] outerSpace = parent.getParent().getCurrentSpace().positions[dimension];
                        int otherPos = neighbor != null ? neighbor.getCurrentSpace().positions[dimension][alignment] :
                                                          outerSpace[alignment^1];
                        int mergedSize = (outerSpace[alignment] - otherPos) * (alignment == LEADING ? -1 : 1);
                        eatGap(interval, gap, mergedSize);
                        return neighbor != null ? neighbor : interval;
                    }
                }
                else {
                    LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
                    seq.getCurrentSpace().set(dimension,
                        (alignment == LEADING) ? pos : interval.getCurrentSpace().positions[dimension][LEADING],
                        (alignment == LEADING) ? interval.getCurrentSpace().positions[dimension][TRAILING] : pos);
                    layoutModel.addInterval(seq, parent, -1);
                    layoutModel.removeInterval(interval);
                    layoutModel.addInterval(interval, seq, -1);
                    parent = seq;
                }
            }
        }
        if (parent.isSequential()) {
            // we can't use insertGapIntoSequence here because 'pos' can be special
            LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(interval, alignment, false);
            if (neighbor != null && neighbor.isEmptySpace()) {
                LayoutInterval next = LayoutInterval.getDirectNeighbor(neighbor, alignment, false);
                int mergedSize;
                if (next != null) {
                    mergedSize = pos - next.getCurrentSpace().positions[dimension][alignment^1];
                } else if (!parentPos) {
                    mergedSize = pos - parent.getCurrentSpace().positions[dimension][alignment];
                } else {
                    mergedSize = interval.getCurrentSpace().positions[dimension][alignment] - pos;
                }
                if (alignment == TRAILING) {
                    mergedSize = -mergedSize;
                }
                eatGap(neighbor, gap, mergedSize);
            }
            else {
                int idx = parent.indexOf(interval) + (alignment == LEADING ? 0 : 1);
                layoutModel.addInterval(gap, parent, idx);
            }
        }
        else { // parallel parent
            LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
            int idx = layoutModel.removeInterval(interval);
            seq.setAlignment(interval.getAlignment());
            seq.getCurrentSpace().set(dimension,
                (alignment == LEADING) ? pos : interval.getCurrentSpace().positions[dimension][LEADING],
                (alignment == LEADING) ? interval.getCurrentSpace().positions[dimension][TRAILING] : pos);
            layoutModel.addInterval(seq, parent, idx);
            layoutModel.setIntervalAlignment(interval, DEFAULT);
            layoutModel.addInterval(interval, seq, 0);
            layoutModel.addInterval(gap, seq, alignment == LEADING ? 0 : 1);
        }

        return interval;
    }

    int insertGapIntoSequence(LayoutInterval gap, LayoutInterval seq, int index, int dimension) {
        assert gap.isEmptySpace();
        LayoutInterval otherGap = null;
        int alignment = DEFAULT;
        if (index >= 0 && index < seq.getSubIntervalCount()) {
            otherGap = seq.getSubInterval(index);
            if (otherGap.isEmptySpace())
                alignment = TRAILING;
        }
        if (alignment == DEFAULT && index > 0) {
            otherGap = seq.getSubInterval(index-1);
            if (otherGap.isEmptySpace())
                alignment = LEADING;
        }
        if (alignment == DEFAULT) {
            layoutModel.addInterval(gap, seq, index);
                return index; // gap was added normally
        }

        int pos1, pos2;
        LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(otherGap, alignment, true);
        pos1 = neighbor != null ?
               neighbor.getCurrentSpace().positions[dimension][alignment^1] :
               seq.getCurrentSpace().positions[dimension][alignment];
        neighbor = LayoutInterval.getDirectNeighbor(otherGap, alignment^1, true);
        pos2 = neighbor != null ?
               neighbor.getCurrentSpace().positions[dimension][alignment] :
               seq.getCurrentSpace().positions[dimension][alignment^1];

        eatGap(otherGap, gap, Math.abs(pos2 - pos1));
        return alignment == LEADING ? index-1 : index; // gap was eaten
    }

    /**
     * Merges consecutive gaps in given sequential group if there are some on
     * given index and index+1 positions.
     * @return true if gaps were merged (so there's now one gap instead of two)
     */
    boolean mergeConsecutiveGaps(LayoutInterval seq, int index, int dimension) {
        assert seq.isSequential();
        if (index < 0 || index >= seq.getSubIntervalCount()) {
            return false;
        }
        LayoutInterval current = seq.getSubInterval(index);
        LayoutInterval next = index+1 < seq.getSubIntervalCount() ? seq.getSubInterval(index+1) : null;
        if (next != null && current.isEmptySpace() && next.isEmptySpace()) {
            int la;
            LayoutRegion lr;
            if (index > 0) {
                la = TRAILING;
                lr = seq.getSubInterval(index-1).getCurrentSpace();
            } else {
                la = LEADING;
                lr = seq.getCurrentSpace();
            }
            int ta;
            LayoutRegion tr;
            if (index+2 < seq.getSubIntervalCount()) {
                ta = LEADING;
                tr = seq.getSubInterval(index+2).getCurrentSpace();
            } else {
                ta = TRAILING;
                tr = seq.getCurrentSpace();
            }
            eatGap(current, next, LayoutRegion.distance(lr, tr, dimension, la, ta));
            return true;
        }
        return false;
    }

    void eatGap(LayoutInterval main, LayoutInterval eaten, int currentMergedSize) {
        int pref1 = main.getPreferredSize();
        int pref2 = eaten.getPreferredSize();

        int min;
        int min1 = main.getMinimumSize();
        if (min1 == USE_PREFERRED_SIZE) {
            min1 = pref1;
        }
        int min2 = eaten.getMinimumSize();
        if (min2 == USE_PREFERRED_SIZE) {
            min2 = pref2;
        }

        if (!LayoutInterval.canResize(main) && !LayoutInterval.canResize(eaten)) {
            min = USE_PREFERRED_SIZE;
        } else if (min1 == NOT_EXPLICITLY_DEFINED || min2 == NOT_EXPLICITLY_DEFINED) {
            min = NOT_EXPLICITLY_DEFINED;
        } else if (min1 == 0) {
            min = min1;
        } else {
            min = min1 + min2;
        }

        int pref;
        if (pref1 == 0) {
            pref = pref2;
        } else if (pref2 == 0) {
            pref = pref1;
        } else if (pref1 == NOT_EXPLICITLY_DEFINED || pref2 == NOT_EXPLICITLY_DEFINED) {
            pref = currentMergedSize;
            if (pref == NOT_EXPLICITLY_DEFINED) {
                if (pref1 > 0) {
                    pref = pref1;
                } else if (pref2 > 0) {
                    pref = pref2;
                }
            }
        } else if (currentMergedSize < 0
                || LayoutInterval.getDirectNeighbor(main, LEADING, false) == null
                || LayoutInterval.getDirectNeighbor(eaten, TRAILING, false) == null
                || LayoutInterval.getDirectNeighbor(main, TRAILING, false) == null
                || LayoutInterval.getDirectNeighbor(eaten, LEADING, false) == null) {
            pref = pref1 + pref2;
        } else {
            pref = currentMergedSize;
        }

        int max = main.getMaximumSize() >= Short.MAX_VALUE || eaten.getMaximumSize() >= Short.MAX_VALUE ?
                  Short.MAX_VALUE : USE_PREFERRED_SIZE;

        layoutModel.setIntervalSize(main, min, pref, max);
        if (eaten.getParent() != null) {
            layoutModel.removeInterval(eaten);
        }
    }

    /**
     * Fixes invalid configurations of gaps, e.g. two (or more) adjacent gaps,
     * adjacent components with no gap, invalid default gaps, etc. To be used as
     * a filter for loaded (old) forms or converted layouts that could
     * potentially be buggy.
     * @return true if any change was made
     */
    boolean fixSurplusOrMissingGaps(LayoutInterval group, int dimension) {
        assert group.isGroup();
        boolean updated = false;
        if (group.isSequential()) {
            LayoutInterval prev = null;
            for (int i=0; i < group.getSubIntervalCount(); i++) {
                LayoutInterval interval = group.getSubInterval(i);
                if (interval.isEmptySpace()) {
                    if (prev != null && prev.isEmptySpace()) {
                        // two adjancent gaps
                        eatGap(prev, interval, NOT_EXPLICITLY_DEFINED);
                        i--; // one interval less
                        interval = group.getSubInterval(i); // the merged gap
                        updated = true;
                    } else if (i > 0 && i+1 < group.getSubIntervalCount()
                            && interval.getPreferredSize() == NOT_EXPLICITLY_DEFINED
                            && !LayoutInterval.canResize(interval)) {
                        boolean removeInvalid;
                        if (!LayoutUtils.isDefaultGapValidForNeighbor(group.getSubInterval(i-1), TRAILING)) {
                            removeInvalid = true;
                        } else {
                            LayoutInterval neighbor = group.getSubInterval(i+1);
                            removeInvalid = !neighbor.isEmptySpace()
                                    && !LayoutUtils.isDefaultGapValidForNeighbor(neighbor, LEADING);
                        }
                        if (removeInvalid) { // invalid default gap (can't be computed)
                            layoutModel.removeInterval(group, i);
                            i--; // one interval less
                            interval = group.getSubInterval(i);
                            updated = true;
                        }
                    }
                    if (interval.getPreferredSize() == 0 && interval.getMinimumSize() == NOT_EXPLICITLY_DEFINED) {
                        // gaps with 0 pref size and default min size render as default,
                        // but presented to the user as 0 size; this is wrong configuration
                        layoutModel.setIntervalSize(interval, NOT_EXPLICITLY_DEFINED, NOT_EXPLICITLY_DEFINED, interval.getMaximumSize());
                    }
                } else if (prev != null && prev.isComponent() && interval.isComponent()) {
                    // no gap between two components
                    LayoutInterval dummyGap = new LayoutInterval(SINGLE);
                    dummyGap.setSize(0);
                    layoutModel.addInterval(dummyGap, group, i);
                    i++; // one interval more
                    updated = true;
                }
                prev = interval;
            }
        }
        Iterator<LayoutInterval> iter = group.getSubIntervals();
        while (iter.hasNext()) {
            LayoutInterval subInterval = iter.next();
            if (subInterval.isGroup()) {
                updated |= fixSurplusOrMissingGaps(subInterval, dimension);
            }
        }
        return updated;
    }

    void suppressResizingOfSurroundingGaps(LayoutInterval interval) {
        LayoutInterval parent = interval.getParent();
        while (parent != null) {
            if (parent.isSequential()) {
                for (Iterator it=parent.getSubIntervals(); it.hasNext(); ) {
                    LayoutInterval sub = (LayoutInterval) it.next();
                    if (sub != interval && sub.isEmptySpace() && LayoutInterval.canResize(sub)) {
                        int pref = sub.getPreferredSize();
                        int min = sub.getMinimumSize() != pref ? USE_PREFERRED_SIZE : pref;
                        int max = USE_PREFERRED_SIZE;
                        layoutModel.setIntervalSize(sub, min, pref, max);
                    }
                }
            }
            else if (!LayoutInterval.canResize(parent))
                break;
            interval = parent;
            parent = interval.getParent();
        }
    }

    /**
     * Sets size of given gap to actual space size between its neighbor intervals.
     * Expects the actual positions are up-to-date.
     * @param gap
     * @param dimension
     */
    void accommodateGap(LayoutInterval gap, int dimension) {
        assert gap.isEmptySpace();
        LayoutInterval parent = gap.getParent();
        if (parent.isSequential()) {
            int pos1, pos2;
            LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(gap, LEADING, true);
            if (neighbor != null) {
               pos1 = neighbor.getCurrentSpace().positions[dimension][TRAILING];
            } else {
                pos1 = parent.getCurrentSpace().positions[dimension][LEADING];
                if (!LayoutRegion.isValidCoordinate(pos1) && parent.getParent() != null) {
                    pos1 = parent.getParent().getCurrentSpace().positions[dimension][LEADING];
                }
            }

            neighbor = LayoutInterval.getDirectNeighbor(gap, TRAILING, true);
            if (neighbor != null) {
               pos2 = neighbor.getCurrentSpace().positions[dimension][LEADING];
            } else {
                pos2 = parent.getCurrentSpace().positions[dimension][TRAILING];
                if (!LayoutRegion.isValidCoordinate(pos1) && parent.getParent() != null) {
                    pos1 = parent.getParent().getCurrentSpace().positions[dimension][TRAILING];
                }
            }

            if (LayoutRegion.isValidCoordinate(pos1) && LayoutRegion.isValidCoordinate(pos2)) {
                int size = pos2 - pos1;
                if (size > 0 && (gap.getPreferredSize() != NOT_EXPLICITLY_DEFINED
                                 || LayoutUtils.getSizeOfDefaultGap(gap, visualMapper) != size)) {
                    resizeInterval(gap, size);
                }
            }
        } // not needed in parallel group
    }

    boolean eliminateUnwantedZeroGap(LayoutInterval gap) {
        if (gap != null && gap.isEmptySpace() && gap.getPreferredSize() == 0
                && !LayoutInterval.canResize(gap) && gap.getParent() != null) {
            boolean eliminate = false;
            if (gap.getParent().isParallel()) {
                eliminate = true;
            } else {
                for (int e=LEADING; e <= TRAILING; e++) {
                    LayoutInterval neighbor = LayoutInterval.getNeighbor(gap, e, false, true, true);
                    if (neighbor != null
                            && !LayoutUtils.hasSideComponents(neighbor, e^1, true, true)) {
                        eliminate = true;
                        break;
                    }
                }
            }
            if (eliminate) {
                layoutModel.removeInterval(gap);
                return true;
            }
        }
        return false;
    }

    /**
     * This method ensures that given group maintains its current size after an
     * interval has been removed. It adds a new gap or modifies some existing
     * for that. The gap can also be added to group's parent if the group is
     * open at one side. Returns the group for which the size was adjusted
     * (either wihin the group or next to it). This group should then go through
     * optimization of its edge gaps vs the neighbor gaps (via optimizeGaps2).
     * @param group
     * @param wasResizing whether the removed interval was resizing
     * @param dimension
     * @param optimize whether structure optimization can be done
     * @return the group for which the size was fixed (or null if it was not necessary)
     */
    LayoutInterval maintainSize(LayoutInterval group, boolean wasResizing, int dimension, boolean optimize) {
        return maintainSize(group, wasResizing, dimension, null, 0, optimize);
    }

    /**
     * This method ensures that given group maintains its current size after an
     * interval has been removed. It adds a new gap or modifies some existing
     * for that. The gap can also be added to group's parent if the group is
     * open at one side. Returns the group for which the size was adjusted
     * (either wihin the group or next to it). This group should then go through
     * optimization of its edge gaps vs the neighbor gaps (via optimizeGaps2).
     * @param group
     * @param wasResizing
     * @param dimension
     * @param excluded don't count this sub-interval (it's the one losing size
     *                 because its sub-interval was removed)
     * @param excludedSize size of the excluded interval it would have after
     *                     removing the sub-interval
     * @return the group for which the size was fixed (or null if it was not necessary)
     */
    LayoutInterval maintainSize(LayoutInterval group, boolean wasResizing, int dimension,
                                LayoutInterval excluded, int excludedSize, boolean optimize)
    {
        assert group.isParallel(); // [also not used for center or baseline groups]

        int groupSize = group.getCurrentSpace().size(dimension);
        int[] groupPos = group.getCurrentSpace().positions[dimension];

        boolean allSameAlignment = true;
        int alignment = DEFAULT;
        int leadCompPos = Integer.MAX_VALUE;
        int trailCompPos = Integer.MIN_VALUE;
        int maxSubSize = Integer.MIN_VALUE;
        LayoutInterval biggest = null;

        for (Iterator<LayoutInterval> it=group.getSubIntervals(); it.hasNext(); ) {
            LayoutInterval li = it.next();
            if (LayoutInterval.wantResize(li)) {
                return null;
            } else {
                int align = li.getAlignment();
                int l = LayoutRegion.UNKNOWN;
                int t = LayoutRegion.UNKNOWN;
                if (li != excluded) {
                    int size = li.getCurrentSpace().size(dimension);
                    if (size > groupSize) {
                        size = groupSize;
                    }
                    if (size > maxSubSize) {
                        maxSubSize = size;
                        biggest = li;
                    }
                } else {
                    if (excludedSize > maxSubSize) {
                        maxSubSize = excludedSize;
                    }
                    if (align == LEADING) {
                        l = groupPos[LEADING];
                        t = groupPos[LEADING] + excludedSize;
                    } else if (align == TRAILING) {
                        l = groupPos[TRAILING] - excludedSize;
                        t = groupPos[TRAILING];
                    }
                }
                if (l == LayoutRegion.UNKNOWN) {
                    l = LayoutUtils.getOutermostComponent(li, dimension, LEADING)
                            .getCurrentSpace().positions[dimension][LEADING];
                }
                if (t == LayoutRegion.UNKNOWN) {
                    t = LayoutUtils.getOutermostComponent(li, dimension, TRAILING)
                            .getCurrentSpace().positions[dimension][TRAILING];
                }
                if (l < leadCompPos) {
                    leadCompPos = l;
                }
                if (t > trailCompPos) {
                    trailCompPos = t;
                }

                if (allSameAlignment) {
                    if (alignment == DEFAULT) {
                        alignment = align;
                    } else if (alignment != align) {
                        allSameAlignment = false;
                    }
                }
            }
        }

        if (maxSubSize == groupSize) {
            if (!wasResizing) {
                return null; // we have same size and have not lost resizing
            }
            LayoutInterval seqRoot = LayoutInterval.getRoot(group, SEQUENTIAL);
            if (seqRoot != null && LayoutInterval.wantResize(seqRoot)) {
                return null; // this group lost resizing, but there's something else
            }
            // otherwise compensate by a zero resizing gap placed somewhere,
            // which will be eliminated, but cause some existing gap resizing
        }

        LayoutInterval parent = group.getParent();
        if (allSameAlignment && parent != null) {
            // fixed content, same alignment, the group can shrink, compensate out of the group
            if (alignment != LEADING && alignment != TRAILING) {
                alignment = (groupPos[TRAILING] - trailCompPos) >= (leadCompPos - groupPos[LEADING]) 
                        ? LEADING : TRAILING; // i.e. the opposite edge to where to compensate
            }
            if (alignment == LEADING) {
                groupPos[TRAILING] = trailCompPos;
            } else {
                groupPos[LEADING] = leadCompPos;
            }
            groupPos[CENTER] = (groupPos[LEADING] + groupPos[LEADING]) / 2;

            if (!LayoutInterval.canResize(group)) { // resizing disabled on the group
                wasResizing = false;
                enableGroupResizing(group); // there's nothing resizing in the group anymore
            }

            if (parent.isParallel()
                    && group.getAlignment() == alignment) {
                // can compensate in parent
                group = maintainSize(parent, wasResizing, dimension, group, maxSubSize, optimize);
            } else { // one open edge - can compensate by a gap next to the group
                boolean border;
                if (parent.isParallel()) {
                    border = true;
                } else {
                    int idx = parent.indexOf(group);
                    border = (alignment == LEADING && idx == parent.getSubIntervalCount()-1)
                             || (alignment == TRAILING && idx == 0);
                }
                int min, max;
                if (wasResizing) {
                    min = NOT_EXPLICITLY_DEFINED;
                    max = Short.MAX_VALUE;
                } else {
                    min = max = USE_PREFERRED_SIZE;
                }
                LayoutInterval gap = new LayoutInterval(SINGLE);
                gap.setSizes(min, groupSize - maxSubSize, max);
                insertGap(gap, group,
                          (alignment == LEADING) ? trailCompPos : leadCompPos,
                          dimension, alignment^1);
                if (parent.isSequential()) {
                    parent = parent.getParent();
                }
                if (border && optimize) {
                    optimizeGaps(parent, dimension);
                    optimizeGaps2(parent, dimension);
                }
            }
        } else { // fixed content, different alignments, compensate by adding a
                 // border gap to some sub-interval
            LayoutInterval ext; // to extend with a gap
            int min, pref, max;
            if (excluded != null) {
                ext = excluded;
                pref = groupSize - excludedSize;
            } else {
                ext = biggest;
                pref = groupSize - maxSubSize;
            }
            alignment = ext.getAlignment();
            if (alignment == LEADING || alignment == TRAILING) {
                if (wasResizing) {
                    LayoutInterval outGap = LayoutInterval.getNeighbor(ext, alignment^1, false, true, false);
                    min = (outGap != null && outGap.isEmptySpace()) ? 0 : NOT_EXPLICITLY_DEFINED;
                    max = Short.MAX_VALUE;
                } else {
                    min = max = USE_PREFERRED_SIZE;
                }

                LayoutInterval extGap = null;
                if (ext.isSequential()) {
                    extGap = ext.getSubInterval(alignment == LEADING ? ext.getSubIntervalCount()-1 : 0);
                    if (extGap.isEmptySpace()) {
                        if (min == 0 && extGap.getMinimumSize() != 0) {
                            min = NOT_EXPLICITLY_DEFINED;
                        } else if (min == NOT_EXPLICITLY_DEFINED && extGap.getMinimumSize() == 0 && max == Short.MAX_VALUE) {
                            min = 0;
                        }
                        layoutModel.setIntervalSize(extGap, min, pref, max);
                    } else {
                        extGap = null;
                    }
                }
                if (extGap == null) {
                    extGap = new LayoutInterval(SINGLE);
                    extGap.setSizes(min, pref, max);
                    insertGap(extGap, ext,
                            (alignment == LEADING) ? groupPos[LEADING] + maxSubSize : groupPos[TRAILING] - maxSubSize,
                            dimension, alignment^1);
                }
                if (optimize) {
                    optimizeGaps(group, dimension);
                }
            }
        }
        return group;
    }
}
