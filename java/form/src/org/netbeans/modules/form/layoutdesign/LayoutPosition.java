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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Code for representing positions of components in the layout structure.
 *
 * @author Tomas Pavek
 */
public class LayoutPosition implements LayoutConstants {

    /**
     * Keeps information about position of selected components intervals
     * already placed in the layout. The position is remembered relatively to
     * other components (not groups) so it remains valid even after removing the
     * selected components (when being moved or resized).
     */
    static final class OriginalPosition {
        private LayoutInterval root;
        private InSequence[] inSequence;
        private InParallel[] inParallel;
        private int definedAlignment;
        private int effectiveAlignment;
        private boolean discontinuousClosedAlignment;
        private LayoutRegion groupSpace;
        private boolean suppressedResizing; // at least one component resizing, in suppressed group
        private boolean wholeResizing; // one resizing, or multiple in parallel - all resizing

        int getAlignment() {
            return definedAlignment;
        }

        boolean snapped(int alignment) {
            if (alignment == LEADING || alignment == TRAILING ) { // check sequential
                if (inSequence != null && inSequence[alignment] != null
                        && inSequence[alignment].snapped) {
                    return true;
                }
            }
            // check parallel
            if ((alignment == LEADING || alignment == TRAILING || alignment == CENTER || alignment == BASELINE)
                    && inParallel != null && inParallel[alignment] != null
                    && inParallel[alignment].aligned) {
                return true;
            }
            return false;
        }

        private int getAlignedEdges() {
            int alignment = DEFAULT;
            if (inParallel != null) {
                for (int a=LEADING; a <= BASELINE; a++) {
                    if (inParallel[a] != null && inParallel[a].aligned) {
                        if (alignment == DEFAULT) {
                            alignment = a;
                        } else if (a == LEADING || a == TRAILING) {
                            alignment = LayoutRegion.ALL_POINTS; // both L and T (resizing)
                        }
                        if (a != LEADING) {
                            break;
                        }
                    }
                }
            }
            return alignment;
        }

        void changeAlignment(int oldA, int newA) {
            inParallel[newA] = inParallel[oldA];
            inParallel[oldA] = null;
        }

        boolean isClosedAlignment() {
            return definedAlignment == CENTER || definedAlignment == BASELINE || discontinuousClosedAlignment;
        }

        LayoutInterval getAlignedRep(int alignment) {
            InParallel parallel = (alignment == LEADING || alignment == TRAILING || alignment == CENTER || alignment == BASELINE)
                                    && inParallel != null ? inParallel[alignment] : null;
            if (parallel != null) {
                return parallel.componentNeighbor;
            }
            return null;
        }

        LayoutRegion getGroupSpace() {
            return groupSpace;
        }

        boolean isClosedSpace(int alignment) {
            return (alignment == LEADING || alignment == TRAILING || alignment == CENTER || alignment == BASELINE)
                    && inParallel != null && inParallel[alignment] != null
                    && inParallel[alignment].closedGroupEdge;
        }

        boolean isSuppressedResizing() {
            return suppressedResizing;
        }

        boolean isWholeResizing() {
            return wholeResizing;
        }

        boolean atFixedPosition(int alignment) {
            return (alignment == LEADING || alignment == TRAILING)
                    && inSequence != null && inSequence[alignment] != null
                    && inSequence[alignment].fixedRelativePosition;
        }

        /**
         * Checks if given interval was in sequence with the original interval
         * (on either side).
         * @param fully If true then all components of 'ínterval' must be in
         *              sequence, if false then at least one.
         * @return true if components of 'interval' where in sequence with
         *              original interval
         */
        boolean wasInSequence(LayoutInterval interval, int dimension, boolean fully) {
            if (inSequence == null) {
                return false;
            }
            boolean somethingInSeq = false;
            Iterator<LayoutInterval> it = LayoutUtils.getComponentIterator(interval);
            while (it.hasNext()) {
                LayoutInterval comp = it.next();
                boolean inSeq = false;
                for (int i=LEADING; i <= TRAILING; i++) {
                    InSequence seq = inSequence[i];
                    if (seq == null || seq.componentNeighbors == null) {
                        continue;
                    }
                    for (LayoutInterval li : seq.componentNeighbors) {
                        if (comp == li) {
                            inSeq = true;
                        } else if (LayoutRegion.distance(comp.getCurrentSpace(), li.getCurrentSpace(), dimension, i^1, i)
                                   * (i==LEADING ? 1:-1) >= 0) {
                            LayoutInterval commonParent = LayoutInterval.getCommonParent(comp, li);
                            if (commonParent != null && commonParent.isSequential()) {
                                inSeq = true;
                            }
                        }
                        if (inSeq) {
                            somethingInSeq = true;
                            break;
                        }
                    }
                    if (inSeq) {
                        break;
                    }
                }
                if ((!inSeq && fully) || (inSeq && !fully)) {
                    break;
                }
            }
            return somethingInSeq;
        }
    }

    /**
     * Describes how to add (include) given components directly into the layout.
     * New position of this kind is computed by LayoutFeeder based on user
     * action. Old position for comparison can be created from OriginalPosition
     * for moved/resized components.
     */
    static final class IncludeDesc {
        LayoutInterval parent;
        int index = -1; // if adding next to
        boolean newSubGroup; // can be true if parent is sequential (parallel subgroup for part of the sequence is to be created)
        LayoutInterval neighbor; // if included in a sequence with single interval (which is not in sequence yet)
        LayoutInterval snappedParallel; // not null if aligning in parallel
        LayoutInterval snappedNextTo; // not null if snapped next to (can but need not be 'neighbor')
        PaddingType paddingType; // type of padding if snapped (next to)
        int alignment = DEFAULT; // defined edge
        boolean fixedPosition; // whether distance from the neighbor is definitely fixed
        int distance = Integer.MAX_VALUE;
        int ortDistance = Integer.MAX_VALUE;

        boolean snapped() {
            return snappedNextTo != null || snappedParallel != null;
        }
    }

    private static class InSequence {
        List<LayoutInterval> componentNeighbors;
        boolean snapped;
        PaddingType paddingType;
        boolean fixedRelativePosition;
    }

    private static class InParallel {
        boolean aligned; // whether any selected component aligned at group edge
        LayoutInterval componentNeighbor; // a representative component in parallel
        int componentNeighborDepth;
        boolean componentNeighborAtBorder; // whether the related component placed or aligned at group edge
        boolean closedGroupEdge;
        private boolean indent;
    }

    /**
     * Computes actual position description of given interval in the layout.
     * This position survives removal of the interval e.g. when it is being
     * resized, and can be used later to reconstruct the direct IncludeDesc
     * position for re-adding the interval.
     * @param components the selected components interval
     * @param inCommonParent intervals that represent the selected components in
     *        their first common parent, see LayoutFeeder.getIntervalsInCommonParent
     */
    static OriginalPosition getOriginalPosition(List<LayoutInterval> components,
                                                List<LayoutInterval> inCommonParent,
                                                int dimension) {
        LayoutInterval first = inCommonParent.get(0);
        LayoutInterval last = inCommonParent.get(inCommonParent.size()-1);
        LayoutInterval parent = first.getParent();
        LayoutInterval parParent = LayoutInterval.getFirstParent(first, PARALLEL);

        OriginalPosition pos = new OriginalPosition();
        pos.root = LayoutInterval.getRoot(first);

        // what is in sequence?
        pos.inSequence = new InSequence[2];
        for (int i=LEADING; i <= TRAILING; i++) {
            pos.inSequence[i] = getSequentialPosition(i==LEADING ? first:last, dimension, i);
        }
        LayoutInterval parentSeq = getCommonSequence(pos);

        // aligned?
        int definedAlign = DEFAULT;
        for (LayoutInterval li : inCommonParent) {
            if (li.getParent() == parParent) {
                int align = li.getAlignment();
                if (definedAlign == DEFAULT) {
                    definedAlign = align;
                } else if (definedAlign != align) {
                    definedAlign = LayoutRegion.NO_POINT;
                    break;
                }
            }
        }
        if (definedAlign == LayoutRegion.NO_POINT) {
            definedAlign = DEFAULT;
        }

        // what is in parallel?
        int effAlign = DEFAULT;
        boolean discontinuousClosedAlign = false;
        if (parentSeq == null || parentSeq.isParentOf(parParent)) {
            pos.inParallel = new InParallel[4];
            if (parParent.getGroupAlignment() == CENTER || parParent.getGroupAlignment() == BASELINE) {
                effAlign = parParent.getGroupAlignment();
                pos.inParallel[effAlign] = getParallelPosition(inCommonParent, dimension, effAlign);
            } else {
                if (parent.isSequential()) {
                    int e1 = LayoutInterval.getEffectiveAlignment(first, LEADING, false);
                    int e2 = LayoutInterval.getEffectiveAlignment(last, TRAILING, false);
                    if (e1 == e2 || e2 == DEFAULT) {
                        effAlign = e1;
                    } else if (e1 == DEFAULT) {
                        effAlign = e2;
                    }
                } else {
                    effAlign = LayoutInterval.getEffectiveAlignment(first);
                }
                for (int i=LEADING; i <= TRAILING; i++) {
                    pos.inParallel[i] = getParallelPosition(inCommonParent, dimension, i);
                }
                if (components.size() > 1 && !pos.inParallel[LEADING].indent && !pos.inParallel[TRAILING].indent) {
                    // Consider special case where the selected components are
                    // aligned on BASELINE or CENTER but not in the same group.
                    boolean sameGroup = true;
                    LayoutInterval group = null;
                    for (LayoutInterval comp : components) {
                        int a = comp.getAlignment();
                        if (a == CENTER || a == BASELINE) {
                            discontinuousClosedAlign = true;
                        }
                        LayoutInterval p = comp.getParent();
                        if (sameGroup && (group == null || group == p)) {
                            group = p;
                        } else {
                            sameGroup = false;
                        }
                    }
                    if (sameGroup) {
                        discontinuousClosedAlign = false;
                    }
                }
            }
            pos.groupSpace = new LayoutRegion();
            pos.groupSpace.set(dimension, parParent.getCurrentSpace());
        }
        pos.definedAlignment = definedAlign;
        pos.effectiveAlignment = effAlign;
        pos.discontinuousClosedAlignment = discontinuousClosedAlign;//scatteredClosedAlign != DEFAULT;

        // resizing state
        boolean wholeResizing = inCommonParent.size() == 1 || parParent.isParallel();
        boolean suppResChecked = false;
        boolean suppressedResizing = false;
        for (LayoutInterval li : inCommonParent) {
            if (LayoutInterval.wantResize(li)) {
                if (!suppResChecked) {
                    suppressedResizing = !LayoutInterval.canResizeInLayout(li);
                    suppResChecked = true;
                }
            } else {
                wholeResizing = false;
            }
        }
        pos.wholeResizing = wholeResizing;
        pos.suppressedResizing = suppressedResizing;

        return pos;
    }

    /**
     * Creates IncludeDesc position description for given OriginalPosition
     * representing now already removed components (being moved or resized).
     */
    static IncludeDesc getInclusionFromOriginal(OriginalPosition orig, int dimension, int alignment) {
        if (alignment < 0) {
            alignment = getBaseAlignment(orig);
        }
        int origAlign = orig.getAlignedEdges();
        IncludeDesc incl = new IncludeDesc();

        LayoutInterval leadingNeighbor = getNeighborInSequence(orig, LEADING);
        LayoutInterval trailingNeighbor = getNeighborInSequence(orig, TRAILING);
        LayoutInterval parentSeq = getCommonSequence(orig);
        LayoutInterval parent;

        // determine the parent
        InParallel par;
        if (orig.inParallel != null) {
            par = orig.inParallel[((origAlign==CENTER || origAlign==BASELINE) && alignment != origAlign) ? origAlign : alignment];
        } else {
            par = null;
        }
        LayoutInterval parallelComp = par != null ? par.componentNeighbor : null;
        if (parallelComp != null) {
            parent = parallelComp.getParent();
            for (int d=par.componentNeighborDepth; d > 0; d--) {
                if (parent.getParent() == null
                        || (leadingNeighbor != null && parent.isParentOf(leadingNeighbor))
                        || (trailingNeighbor != null && parent.isParentOf(trailingNeighbor))) {
                    break;
                }
                parent = parent.getParent();
            }
            if (parent.isSequential()) {
                incl.newSubGroup = true; // parallel with part of the sequence
            }
        } else if (parentSeq != null) { // sequential neighbors on both sides
            parent = parentSeq;
        } else if (leadingNeighbor != null || trailingNeighbor != null) { // just one neighbor
            LayoutInterval oneNeighbor = leadingNeighbor != null ? leadingNeighbor : trailingNeighbor;
            parent = oneNeighbor.getParent();
            LayoutInterval seqParent = null;
            while (parent != null) {
                if (parent.isSequential()) {
                    seqParent = parent;
                }
                parent = parent.getParent();
            }
            if (seqParent != null) {
                parent = seqParent;
            } else {
                parent = oneNeighbor.getParent();
                if (parent == null) {
                    assert oneNeighbor == orig.root;
                    parent = oneNeighbor;
                }
            }
        } else {
            parent = orig.root;
        }

        if (parent.isParallel()) {
            LayoutInterval neighborInSeq;
            int neighborDirection;
            if (trailingNeighbor != null && parent.isParentOf(trailingNeighbor)) {
                neighborInSeq = trailingNeighbor;
                neighborDirection = TRAILING;
            } else if (leadingNeighbor != null && parent.isParentOf(leadingNeighbor)) {
                neighborInSeq = leadingNeighbor;
                neighborDirection = LEADING;
            } else {
                neighborInSeq = null;
                neighborDirection = -1;
            }
            if (neighborInSeq != null) {
                List<LayoutInterval> allSeqNeighbors = getSequentialNeighbors(orig, neighborDirection);
                boolean compact = neighborInSeq.isParallel() || allSeqNeighbors.size() == 1; // limitation: IncludeDesc.neighbor can be only one
                LayoutInterval p = neighborInSeq.getParent();
                if (p == parent) { // same level
                    if (compact) {
                        incl.neighbor = neighborInSeq;
                        incl.index = (neighborDirection == TRAILING) ? 0 : 1;
                    }
                } else { // the seq. neighbor is deeper, try to go up as close to parent as possible
                    do {
                        if (p.isParallel()) {
                            List<LayoutInterval> list = LayoutUtils.getSideComponents(p, neighborDirection^1, false, false);
                            list.removeAll(allSeqNeighbors);
                            if (!list.isEmpty()) { // going up we'd get in sequence with something that should stay in parallel
                                parent = p;
                                if (neighborInSeq.getParent() == p) {
                                    incl.neighbor = neighborInSeq;
                                    incl.index = (neighborDirection == TRAILING) ? 0 : 1;
                                }
                                break;
                            }
                        }
                        LayoutInterval pp = p.getParent();
                        if (pp == parent) {
                            if (p.isParallel()) {
                                if (compact) {
                                    incl.neighbor = p;
                                    incl.index = (neighborDirection == TRAILING) ? 0 : 1;
                                }
                            } else {
                                parent = p; // down to sequence
                                incl.index = (neighborDirection == TRAILING) ? 0 : p.getSubIntervalCount();
                            }
                            break;
                        }
                        p = pp;
                    } while (p != null); // (actually should never go to null)
                }
            }
        }
        incl.parent = parent;

        if (parent.isSequential() && !incl.newSubGroup) {
            // multiple selected component might have been each parallel with something else
            // example: 3 baseline rows by 3 components, selecting first components of 2nd and 3rd row
            if (leadingNeighbor != null && parent.isParentOf(leadingNeighbor)) {
                int i = LayoutInterval.getIndexInParent(leadingNeighbor, parent);
                LayoutInterval neighborInSeq = LayoutInterval.getDirectNeighbor(parent.getSubInterval(i), TRAILING, true);
                if (neighborInSeq != null && neighborInSeq != trailingNeighbor
                        && (trailingNeighbor == null || !neighborInSeq.isParentOf(trailingNeighbor))) {
                    incl.newSubGroup = true; // in the sequence but not in trailingNeighbor
                }
            }
            if (trailingNeighbor != null && parent.isParentOf(trailingNeighbor)) {
                int i = LayoutInterval.getIndexInParent(trailingNeighbor, parent);
                LayoutInterval neighborInSeq = LayoutInterval.getDirectNeighbor(parent.getSubInterval(i), LEADING, true);
                if (neighborInSeq != null && neighborInSeq != leadingNeighbor
                        && (leadingNeighbor == null || !neighborInSeq.isParentOf(leadingNeighbor))) {
                    incl.newSubGroup = true;
                }
            }
        }

        // determine alignment and parallel snap
        incl.alignment = alignment;
        if (origAlign == LEADING || origAlign == TRAILING || origAlign == LayoutRegion.ALL_POINTS) {
            if (par != null && par.aligned) {
                LayoutInterval parParent = parent.isParallel() ? parent : parent.getParent();
                // can we consider parallel snap with this parent?
                boolean parentAlign = (parallelComp == null);
                if (parallelComp != null && !par.componentNeighborAtBorder && (alignment == LEADING || alignment == TRAILING)) {
                    parentAlign = true;
                    InSequence s = orig.inSequence[alignment];
                    if (s != null && s.componentNeighbors != null) {
                        for (LayoutInterval li : s.componentNeighbors) {
                            if (parParent.isParentOf(li)) {
                                parentAlign = false; // it's not the original par. parent, it's parent of something that was in sequence
                                break;
                            }
                        }
                    }
                }
                if (parentAlign) {
                    if (parParent == orig.root) {
                        incl.snappedParallel = orig.root;
                    } else if (LayoutInterval.isAlignedAtBorder(parParent, orig.root, alignment)) {
                        incl.snappedParallel = parParent;
                    }
                } else if (par.componentNeighborAtBorder) {
                    LayoutInterval aligned = parallelComp;
                    LayoutInterval prev = null;
                    do {
                        LayoutInterval p = aligned.getParent();
                        if (p.isSequential()) {
                            prev = aligned;
                            aligned = p;
                            p = p.getParent();
                        }
                        if (p == parParent) {
                            if (parent == parParent && (!aligned.isSequential() || LayoutInterval.isAlignedAtBorder(prev, parent, alignment))) {
                                incl.snappedParallel = parent;
                            } else {
                                incl.snappedParallel = aligned.isSequential() ? prev : aligned;
                            }
                            break;
                        } else if (LayoutInterval.isAlignedAtBorder(aligned, alignment)
                                && p.getCurrentSpace().positions[dimension][alignment] == orig.groupSpace.positions[dimension][alignment]) {
                            // go up
                            prev = aligned;
                            aligned = p;
                        } else {
                            break; // not aligned
                        }
                    } while(aligned.getParent() != null);
                }
            }
        } else if (origAlign == CENTER || origAlign == BASELINE) {
            incl.alignment = origAlign;
            incl.snappedParallel = parent.getGroupAlignment() == origAlign ? parent : par.componentNeighbor;
        }

        if (alignment == LEADING || alignment == TRAILING) {
            // determine whether need to snap parallel due to indent
            if (incl.snappedParallel == null && par != null && par.indent && parent.isSequential()
                    && parallelComp != null && parallelComp.getParent() == parent) {
                incl.snappedParallel = parallelComp;
            }
            // determine snappedNextTo
            if (incl.snappedParallel == null) {
                for (int a=LEADING; a <= TRAILING; a++) {
                    InSequence s = orig.inSequence[a];
                    if (a == alignment && s != null && s.snapped) {
                        LayoutInterval neighborInSeq = (a == LEADING) ? leadingNeighbor : trailingNeighbor;
                        if (neighborInSeq != null) {
                            if (parent.isParentOf(neighborInSeq)) {
                                int index = LayoutInterval.getIndexInParent(neighborInSeq, parent);
                                if (index > -1) {
                                    incl.snappedNextTo = parent.getSubInterval(index);
                                    incl.paddingType = s.paddingType;
                                }
                            } else if (neighborInSeq.getParent().isParentOf(parent)) {
                                incl.snappedNextTo = neighborInSeq;
                                incl.paddingType = s.paddingType;
                            }
                        } else {
                            incl.snappedNextTo = orig.root;
                        }
                    }
                }
            }
            // determine "fixed position"
            for (int a=LEADING; a <= TRAILING; a++) {
                InSequence s = orig.inSequence[a];
                if (a == alignment && s != null) {
                    incl.fixedPosition = s.fixedRelativePosition;
                }
            }
        }

        // determine index in sequence
        if (parent.isSequential() && incl.index < 0 && parent.getSubIntervalCount() > 0) {
            int a = alignment;
            if (a != LEADING && a != TRAILING) {
                a = LEADING;
            }
            for (int n=2; n > 0; a^=1, n--) {
                LayoutInterval neighborInSeq = (a == LEADING) ? leadingNeighbor : trailingNeighbor;
                if (neighborInSeq != null) {
                    int index = LayoutInterval.getIndexInParent(neighborInSeq, parent);
                    if (index > -1) {
                        if (a == LEADING) {
                            index++;
                            if (index + 1 <= parent.getSubIntervalCount()) {
                                index++;
                            }
                        }
                        incl.index = index;
                        break;
                    }
                } else if (incl.newSubGroup) {
                    int index = (a == LEADING) ? 0 : parent.getSubIntervalCount();
                    if (parent.getSubInterval(index - (a==TRAILING ? 1:0)).isEmptySpace()
                            && orig.inParallel != null && orig.inParallel[a] != null
                            && orig.inParallel[a].componentNeighborAtBorder) {
                        index += (a == LEADING) ? 1 : -1;
                    }
                    incl.index = index;
                    break;
                }
            }
        }

        return incl;
    }

    private static InSequence getSequentialPosition(LayoutInterval interval, int dimension, int alignment) {
        LayoutInterval neighbor = null;
        boolean snapped = false;
        PaddingType paddingType = null;
        LayoutInterval gap = LayoutInterval.getNeighbor(interval, alignment, false, true, false);
        if (gap != null && LayoutInterval.isFixedDefaultPadding(gap)) {
            LayoutInterval prev = LayoutInterval.getDirectNeighbor(gap, alignment^1, true);
            if (prev == interval || LayoutInterval.isPlacedAtBorder(interval, prev, dimension, alignment)) {
                LayoutInterval next = LayoutInterval.getNeighbor(gap, alignment, true, true, false);
                if (next != null) {
                    if (next.getParent() == gap.getParent()
                        || next.getCurrentSpace().positions[dimension][alignment^1]
                           == gap.getParent().getCurrentSpace().positions[dimension][alignment])
                    {   // the next interval is really at preferred distance
                        neighbor = next;
                        paddingType = gap.getPaddingType();
                    }
                } else { // likely next to the root group border
                    next = LayoutInterval.getRoot(interval);
                    if (LayoutInterval.isPlacedAtBorder(gap.getParent(), next, dimension, alignment)) {
                        neighbor = next;
                    }
                }
            }
        }
        if (neighbor != null) {
            snapped = true;
        } else {
            neighbor = LayoutInterval.getNeighbor(interval, alignment, true, true, false);
        }
        List<LayoutInterval> neighbors;
        if (neighbor != null && !neighbor.isParentOf(interval)) {
            LayoutInterval first = LayoutUtils.getOutermostComponent(neighbor, dimension, alignment^1);
            neighbors = LayoutUtils.getSideComponents(neighbor, alignment^1, false, false);
            if (first != null) {
                neighbors.remove(first);
                neighbors.add(0, first);
            }
            if (neighbors.isEmpty()) {
                neighbors = null;
            }
        } else {
            neighbors = null;
        }

        InSequence seqPos = new InSequence();
        seqPos.componentNeighbors = neighbors;
        seqPos.snapped = snapped;
        seqPos.paddingType = paddingType;
        seqPos.fixedRelativePosition = isFixedRelativePosition(interval, alignment);
        return seqPos;
    }

    private static InParallel getParallelPosition(List<LayoutInterval> inCommonParent, int dimension, int alignment) {
        InParallel par = new InParallel();
        LayoutInterval parParent = LayoutInterval.getFirstParent(inCommonParent.get(0), PARALLEL);
        if (alignment == CENTER || alignment == BASELINE) {
            par.aligned = true;
            par.closedGroupEdge = true;
        } else if (alignment == LEADING || alignment == TRAILING) {
            // go through selected to find out if some is aligned at parallel group edge
            for (LayoutInterval interval : inCommonParent) {
                if (!par.aligned && LayoutInterval.isAlignedAtBorder(interval, parParent, alignment)
                        && !LayoutUtils.getSideComponents(interval, alignment, true, true).isEmpty()) {
                    par.aligned = true;
                    par.closedGroupEdge = true;
                    break;
                }
            }
        }
        if (parParent.getParent() == null) { // root
            par.closedGroupEdge = true;
        }
        // go through the rest (other then selected) to find a suitable parallel neighbor
        LayoutInterval parallelComp = null;
        boolean parCompAligned = false;
        boolean parCompAtBorder = false;
        int parCompDepth = 0;
        for (Iterator<LayoutInterval> it=parParent.getSubIntervals(); it.hasNext(); ) {
            LayoutInterval interval = it.next();
            boolean containsSelection = false;
            for (LayoutInterval sel : inCommonParent) {
                if (interval == sel || interval.isParentOf(sel)) {
                    containsSelection = true;
                    break;
                }
            }
            if (containsSelection) {
                continue;
            }
            Iterator<LayoutInterval> it2 = LayoutUtils.getComponentIterator(interval);
            while (it2.hasNext()) {
                LayoutInterval li = it2.next();
                if (li.isComponent()) {
                    boolean aligned;
                    boolean atBorder;
                    if (par.aligned) {
                        if (alignment == LEADING || alignment == TRAILING) {
                            aligned = LayoutInterval.isAlignedAtBorder(li, parParent, alignment);
                            atBorder = aligned || LayoutInterval.isPlacedAtBorder(li, parParent, dimension, alignment);
                        } else {
                            aligned = true;
                            atBorder = LayoutInterval.isPlacedAtBorder(li, parParent, dimension, LEADING)
                                    || LayoutInterval.isPlacedAtBorder(li, parParent, dimension, TRAILING);
                        }
                    } else { // otherwise does not matter if aligned or at border
                        aligned = false;
                        atBorder = false;
                    }
                    int depth = LayoutInterval.getDepthInParent(li, parParent);
                    boolean better = false;
                    if (parallelComp == null) {
                        better = true;
                    } else if (aligned && !parCompAligned) {
                        better = true;
                    } else if (aligned == parCompAligned) {
                        if (atBorder && !parCompAtBorder) {
                            better = true;
                        } else if (atBorder == parCompAtBorder) {
                            if (depth < parCompDepth || (depth == parCompDepth && alignment == TRAILING)) {
                                better = true;
                            }
                        }
                    }
                    if (better) {
                        parallelComp = li;
                        parCompAligned = aligned;
                        parCompAtBorder = atBorder;
                        parCompDepth = depth;
                    }
                    if (aligned && !par.closedGroupEdge) {
                        par.closedGroupEdge = true;
                    }
                }
            }
        }
        par.componentNeighbor = parallelComp;
        par.componentNeighborDepth = parCompDepth - 1;
        par.componentNeighborAtBorder = parCompAtBorder;

        // detection of just one indented component
        if ((alignment == LEADING || alignment == TRAILING)
                && inCommonParent.size() == 1 && !par.aligned
                && parallelComp != null && LayoutInterval.isAlignedAtBorder(parallelComp, parParent, alignment)) {
            LayoutInterval one = inCommonParent.get(0);
            LayoutInterval seq = one.getParent();
            if (seq.isSequential() && LayoutInterval.getCount(seq, -1, true) == 1) {
                LayoutInterval indentGap = LayoutInterval.getDirectNeighbor(one, alignment, false);
                if (indentGap != null && indentGap.isEmptySpace() && !LayoutInterval.canResize(indentGap)
                        && !LayoutInterval.isAlignedAtBorder(seq, LayoutInterval.getRoot(parParent), alignment)
                        && indentGap.getPreferredSize() < parallelComp.getCurrentSpace().size(dimension)) {
                    par.indent = true;
                }
            }
        }
        return par;
    }

    private static boolean isFixedRelativePosition(LayoutInterval interval, int edge) {
        assert edge == LEADING || edge == TRAILING;
        LayoutInterval parent = interval.getParent();
        if (parent == null) {
            return true;
        }
        if (parent.isSequential()) {
            LayoutInterval li = LayoutInterval.getDirectNeighbor(interval, edge, false);
            if (li != null) {
                return !LayoutInterval.wantResize(li);
            } else {
                interval = parent;
                parent = interval.getParent();
            }
        }
        if (!LayoutInterval.isAlignedAtBorder(interval, parent, edge)
                && LayoutInterval.contentWantResize(parent)) {
            return false;
        }
        return isFixedRelativePosition(parent, edge);
    }

    private static LayoutInterval getCommonSequence(OriginalPosition orig) {
        if (orig.inSequence != null) {
            InSequence s1 = orig.inSequence[LEADING];
            InSequence s2 = orig.inSequence[TRAILING];
            if (s1 != null && s2 != null && s1.componentNeighbors != null && s2.componentNeighbors != null) {
                return LayoutInterval.getCommonParent(s1.componentNeighbors.get(0), s2.componentNeighbors.get(0));
            }
        }
        return null;
    }

    private static LayoutInterval getNeighborInSequence(OriginalPosition orig, int alignment) {
        InSequence s = orig.inSequence != null ? orig.inSequence[alignment] : null;
        if (s != null && s.componentNeighbors != null) {
            int count = s.componentNeighbors.size();
            if (count > 1) {
                LayoutInterval[] intervals = s.componentNeighbors.toArray(new LayoutInterval[count]);
                LayoutInterval commonParent = LayoutInterval.getCommonParent(intervals);
                if (commonParent != null && commonParent.isParallel()) {
                    List<LayoutInterval> sideComps = LayoutUtils.getSideComponents(commonParent, alignment^1, false, false);
                    if (sideComps != null && sideComps.size() == count) {
                        return commonParent; // still the same group as originally
                    }
                }
            }
            return s.componentNeighbors.get(0);
        }
        return null;
    }

    private static List<LayoutInterval> getSequentialNeighbors(OriginalPosition orig, int alignment) {
        InSequence s = orig.inSequence != null ? orig.inSequence[alignment] : null;
        if (s != null && s.componentNeighbors != null) {
            return s.componentNeighbors;
        }
        return Collections.EMPTY_LIST;
    }

    private static int getBaseAlignment(OriginalPosition orig) {
        int alignment = orig.getAlignedEdges();
        if (alignment == LEADING || alignment == TRAILING || alignment == CENTER || alignment == BASELINE) {
            return alignment;
        }
        if (orig.definedAlignment != DEFAULT) {
            return orig.definedAlignment;
        }

        InSequence lPos = null;
        InSequence tPos = null;
        if (orig.inSequence != null) {
            for (int a=0; a < orig.inSequence.length; a++) {
                InSequence seqPos = orig.inSequence[a];
                InSequence other = orig.inSequence[a^1];
                if (seqPos != null && seqPos.snapped && (other == null || !other.snapped)) {
                    return a;
                }
                if (lPos == null) {
                    lPos = seqPos;
                    tPos = other;
                }
            }
        }
        if (orig.effectiveAlignment != DEFAULT) {
            return orig.effectiveAlignment;
        }
        if (lPos != null && tPos != null) {
            if (lPos.fixedRelativePosition) {
                return LEADING;
            } else if (tPos.fixedRelativePosition) {
                return TRAILING;
            }
        }
        return lPos != null || tPos == null ? LEADING : TRAILING;
    }
}
