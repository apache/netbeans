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

import java.awt.Toolkit;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.netbeans.modules.form.layoutdesign.LayoutPosition.OriginalPosition;
import static org.netbeans.modules.form.layoutdesign.LayoutPosition.IncludeDesc;

/**
 * This class is responsible for adding layout intervals to model based on
 * mouse actions done by the user (input provided from LayoutDragger). When an
 * instance is created, it analyzes the original positions - before the adding
 * operation is performed (this is needed in case of resizing). Then 'add'
 * method is called to add the intervals on desired place. It is responsibility
 * of the caller to remove the intervals/components from original locations
 * before calling 'add'.
 * Note this class does not add LayoutComponent instances to model.
 *
 * @author Tomas Pavek
 */

class LayoutFeeder implements LayoutConstants {

    boolean imposeSize;
    boolean optimizeStructure;

    private LayoutModel layoutModel;
    private LayoutOperations operations;

    private LayoutDragger dragger;
    private OriginalPosition[] originalPositions = new OriginalPosition[DIM_COUNT];
    private List[] undoMarks = new List[DIM_COUNT];
    private LayoutRegion originalSpace;
    private LayoutDragger.PositionDef[] newPositions = new LayoutDragger.PositionDef[DIM_COUNT];
    private LayoutInterval[][] selectedComponentIntervals = new LayoutInterval[DIM_COUNT][]; // horizontal, vertical // [get rid of]
    private Boolean[] becomeResizing = new Boolean[DIM_COUNT];
    private Collection<LayoutInterval>[] unresizedOnRemove;

    // working context (actual dimension)
    private int dimension;
    private LayoutInterval addingInterval;
    private LayoutRegion addingSpace;
    private boolean solveOverlap;
    private LayoutRegion closedSpace;
    private OriginalPosition originalPosition;
    private IncludeDesc originalInclusion1;
    private IncludeDesc originalInclusion2;
    private Object undoCheckMark;

    // params used when searching for the right place (inclusion)
    private int aEdge;
    private LayoutInterval aSnappedParallel;
    private LayoutInterval aSnappedNextTo;
    private PaddingType aPaddingType;

    // -----

    LayoutFeeder(LayoutComponent[] selectedComponents, LayoutComponent targetContainer,
                 LayoutOperations operations, LayoutDragger dragger) {
        this.layoutModel = operations.getModel();
        this.operations = operations;
        this.dragger = dragger;

        boolean stayInContainer = true;
        for (LayoutComponent c : selectedComponents) {
            if (c.getParent() == null || c.getParent() != targetContainer) {
                stayInContainer = false;
                break;
            }
        }

        for (int dim=0; dim < DIM_COUNT; dim++) {
            LayoutInterval[] compIntervals = new LayoutInterval[selectedComponents.length];
            for (int i=0; i < selectedComponents.length; i++) {
                compIntervals[i] = selectedComponents[i].getLayoutInterval(dim);
            }
            selectedComponentIntervals[dim] = compIntervals;
            List<LayoutInterval> selCompList = Arrays.asList(compIntervals);
            List<LayoutInterval> inCommonParent = stayInContainer ? getIntervalsInCommonParent(compIntervals) : null;
            OriginalPosition originalPos;
            if (inCommonParent != null && !inCommonParent.isEmpty()) {
                originalPos = LayoutPosition.getOriginalPosition(selCompList, inCommonParent, dim);
            } else {
                originalPos = null;
            }

            if (dragger.isResizing(dim)) {
                LayoutInterval resizingComp = compIntervals[0];
                LayoutInterval parent = resizingComp.getParent();
                int origAlignment = resizingComp.getRawAlignment();
                LayoutDragger.PositionDef newPos = dragger.getPositions()[dim];
                int alignedEdge = originalPos.getAlignment();
                if ((alignedEdge == CENTER || alignedEdge == BASELINE)
                        && newPos != null && newPos.snapped
                        && (newPos.alignment == LEADING || newPos.alignment == TRAILING)
                        && newPos.interval != parent && newPos.interval.getParent() != parent) {
                    // special case: resizing a baseline component and snapping
                    // outside its group - it should not stay on baseline
                    originalPos.changeAlignment(alignedEdge, newPos.alignment^1);
                    resizingComp.setAlignment(newPos.alignment^1); // to influence checkResizing, will be restored to origAlignment
                }
                newPositions[dim] = newPos;
                becomeResizing[dim] = checkResizing(resizingComp, dragger, dim);
                resizingComp.setAlignment(origAlignment);
                if (layoutModel.isChangeRecording()) {
                    undoMarks[dim] = new ArrayList();
                }
            } else {
                if (!dragger.isResizing()) { // adding or moving
                    newPositions[dim] = dragger.getPositions()[dim];
                }
                if (stayInContainer && originalPos != null) {
                    if (layoutModel.isChangeRecording()) {
                        undoMarks[dim] = new ArrayList();
                    }
                }
            }
            originalPositions[dim] = originalPos;
        }
    }

    void setUndo(Object start, Object end, int dim) {
        if (undoMarks[dim] != null && start != null && !start.equals(end)) {
            undoMarks[dim].add(start);
            undoMarks[dim].add(end);
        }
    }

    void setUp(LayoutRegion origSpace, Collection<LayoutInterval>[] unresizedOnRemove) {
        this.originalSpace = origSpace;
        this.unresizedOnRemove = unresizedOnRemove;
    }

    void add(LayoutInterval[] addingInts) {
        addingSpace = dragger.getMovingSpace();

        int overlapDim = getDimensionSolvingOverlap(newPositions);
        for (int dim=overlapDim, dc=0; dc < DIM_COUNT; dim^=1, dc++) {
            dimension = dim;
            addingInterval = addingInts[dim];
            assert addingInterval.getParent() == null : layoutModel.dump(addingInterval, dim) + " " + dc; // is bug #223709 present here already? // NOI18N
            solveOverlap = overlapDim == dim;
            LayoutInterval root = dragger.getTargetRoots()[dim];
            originalPosition = originalPositions[dim];
            IncludeDesc originalPos1 = null;
            IncludeDesc originalPos2 = null;
            LayoutDragger.PositionDef newPos = newPositions[dim];

            if (originalPosition != null) {
                int alignment;
                if (dragger.isResizing(dim)) {
                    alignment = dragger.getResizingEdge(dim)^1;
                    if ((newPos == null || !newPos.snapped) && !originalPosition.snapped(alignment)) {
                        alignment = DEFAULT;
                    }
                } else {
                    alignment = DEFAULT;
                }
                originalPos1 = LayoutPosition.getInclusionFromOriginal(originalPosition, dim, alignment);
                if (!dragger.isResizing(dim)) {
                    alignment = originalPos1.alignment;
                    if (alignment == LEADING || alignment == TRAILING) {
                        IncludeDesc pos2 = LayoutPosition.getInclusionFromOriginal(originalPosition, dim, alignment^1);
                        if (pos2.snapped() || !originalPos1.snapped()) {
                            originalPos2 = pos2;
                        } // don't remember second position if not snapped, one is enough
                    }
                }
            }
            originalInclusion1 = originalPos1;
            originalInclusion2 = originalPos2;
            undoCheckMark = layoutModel.getChangeMark();
            closedSpace = null;
            boolean onBaseline = originalPos1 != null && (originalPos1.alignment == BASELINE || originalPos1.alignment == CENTER);

            if (dragger.isResizing()) {
                if (dragger.isResizing(dim)) {
                    boolean res = Boolean.TRUE.equals(becomeResizing[dim]);
                    int min = res ? NOT_EXPLICITLY_DEFINED : USE_PREFERRED_SIZE;
                    int max = res ? Short.MAX_VALUE : USE_PREFERRED_SIZE;
                    int pref;
                    if (onBaseline && newPos != null && newPos.snapped && !newPos.nextTo
                            && (newPos.alignment == LEADING || newPos.alignment == TRAILING)
                            && originalPos1.snappedParallel != null) {
                        // resized to same size of another baseline component - we can't
                        // create an accommodating reizing component, just set same size
                        pref = originalPos1.snappedParallel.getCurrentSpace().size(dim);
                    } else {
                        pref = addingSpace.size(dim);
                    }
                    layoutModel.setIntervalSize(addingInterval, min, pref, max);
                    operations.enableFlexibleSizeDefinition(addingInterval, true);
                }
            }

            if (!dragger.isResizing(dim) && newPos != null
                    && (newPos.alignment == CENTER || newPos.alignment == BASELINE)) {
                // simplified adding/moving to closed group
                if (originalPos1 == null || originalPos1.alignment != newPos.alignment
                        || !equalSnap(originalPos1.snappedParallel, newPos.interval, newPos.alignment)
                        || !restoreDimension()) { // if not staying the same...
                    aEdge = newPos.alignment;
                    aSnappedParallel = newPos.interval;
                    addSimplyAligned();
                }
                continue;
            }
            if (dragger.isResizing() && onBaseline) {
                // simplified resizing in closed group
                if (!restoreDimension()) { // if not staying the same...
                    aEdge = originalPos1.alignment;
                    aSnappedParallel = originalPos1.snappedParallel;
                    addSimplyAligned();
                }
                continue;
            }

            // prepare task for searching the position
            IncludeDesc inclusion1 = null;
            IncludeDesc inclusion2 = null;

            List<IncludeDesc> inclusions = new LinkedList<IncludeDesc>();
            boolean preserveOriginal;
            boolean originalSignificant = dragger.isResizing(dimension) && originalPos1 != null
                    && (originalPos1.snapped()
                        || newPos == null || newPos.nextTo || newPos.interval == null // no new snap in parallel
                        || originalPos1.neighbor != null // something is next to
                        || (originalPos1.parent.isSequential() // something is next to
                            && (!originalPos1.newSubGroup
                                || !originalPos1.parent.isParentOf(newPos.interval))));

            if (dragger.isResizing(dim^1)
                    && (originalSignificant || (newPos == null && originalPos1 != null))) {
                // resizing in the other dimension, renew the original position
                aEdge = originalPos1.alignment;
                aSnappedParallel = originalPos1.snappedParallel;
                aSnappedNextTo = originalPos1.snappedNextTo;
                aPaddingType = originalPos1.paddingType;
                preserveOriginal = false;
            } else if (newPos != null) {
                // snapped in dragger, always find the position
                aEdge = newPos.alignment;
                aSnappedParallel = newPos.snapped && !newPos.nextTo ? newPos.interval : null;
                aSnappedNextTo = newPos.snapped && newPos.nextTo ? newPos.interval : null;
                aPaddingType = newPos.paddingType;
                preserveOriginal = originalSignificant;
            } else if (dragger.isResizing(dim) && originalPos1 != null) {
                // resizing only in this dimension and without snap, check for
                // possible growing in parallel with part of its own parent sequence
                aEdge = originalPos1.alignment;
                if (originalPos1.alignment != dragger.getResizingEdge(dim)) {
                    aSnappedParallel = originalPos1.snappedParallel;
                    aSnappedNextTo = originalPos1.snappedNextTo;
                    aPaddingType = originalPos1.paddingType;
                    preserveOriginal = true;
                } else {
                    aSnappedParallel = aSnappedNextTo = null;
                    aPaddingType = null;
                    preserveOriginal = false;
                }
            } else { // otherwise plain moving without snap
                aEdge = DEFAULT;
                aSnappedParallel = aSnappedNextTo = null;
                aPaddingType = null;
                preserveOriginal = false;
            }

            analyzeParallel(root, inclusions);

            if (inclusions.isEmpty()) { // no suitable inclusion found (nothing in sequence)
                assert aSnappedParallel != null;
                if (dragger.isResizing() && originalPos1 != null && originalPos1.alignment == aEdge) {
                    inclusions.add(originalPos1);
                } else {
                    addAligningInclusion(inclusions);
                }
            } else {
                if (inclusions.size() > 1) {
                    // Original position can't express multiple inclusions in parallel, can be misleading.
                    // If some of them should not be counted, they would be treated already in considerSequentialPosition.
                    preserveOriginal = false;
                }
                IncludeDesc preferred = addAligningInclusion(inclusions); // make sure it is there...
                if (inclusions.size() > 1) {
                    if ((preferred == null || (preserveOriginal && originalPos1.alignment == aEdge))
                            && dragger.isResizing()) {
                        preferred = originalPos1;
                    }
                    mergeParallelInclusions(inclusions, preferred, preserveOriginal);
                    assert inclusions.size() == 1;
                }
            }

            IncludeDesc found = inclusions.get(0);
            inclusions.clear();

            if (preserveOriginal) { // i.e. resizing in this dimension
                inclusion1 = originalPos1;
                if (found != originalPos1) {
                    if (newPos != null) {
                        inclusion2 = found;
                    }
                    LayoutInterval foundP = found.parent;
                    LayoutInterval origP = originalPos1.parent;
                    // here we try to keep the original inclusion, but it may need adjustment
                    if (foundP.isSequential() && origP.isSequential()) {
                        if (found.newSubGroup && (foundP == origP || foundP.isParentOf(origP)) && LayoutUtils.contentOverlap(addingInterval, origP, dim)) {
                            inclusion1.newSubGroup = true; // resizing along something in the existing sequence (expanding in parallel)
                        } else if (!found.newSubGroup && (origP == foundP || origP.isParentOf(foundP))) {
                            inclusion1.newSubGroup = false; // shrinking so not in parallel with the sequence anymore (reducing to sequence)
                        }
                    } else if (foundP.isParallel() && origP.isSequential()) {
                        if (found.neighbor == null && foundP.isParentOf(origP) && LayoutUtils.contentOverlap(addingInterval, origP, dim)) {
                            inclusion1.newSubGroup = true; // expanding in parallel
                        } // opposite case (from parallel to sequential combination) is strange here
                    } else if (origP.isParallel() && foundP.isSequential()) {
                        if (originalPos1.neighbor != null && foundP.isParentOf(origP)
                                && LayoutUtils.contentOverlap(addingInterval, originalPos1.neighbor, dim)
                                && !LayoutUtils.contentOverlap(addingInterval, originalPos1.neighbor, dim^1)) {
                            inclusion1.neighbor = null; // expanding in parallel
                        } else if (originalPos1.neighbor == null && origP.isParentOf(foundP) && !found.newSubGroup) {
                            inclusion1.parent = foundP; // reducing to sequence
                            inclusion1.index = found.index;
                        }
                    } else if (foundP == origP) { // i.e. both parallel
                        inclusion1.neighbor = found.neighbor;
                        inclusion1.index = found.index;
                    }
                }
            } else {
                inclusion1 = found;

                boolean secondRound;
                if (newPos != null) {
                    if (dragger.isResizing(dim^1) && originalSignificant) {
                        // find inclusion based on the position from dragger
                        // (first round was for renewing the original position)
                        assert dragger.isResizing(dim);
                        aEdge = newPos.alignment;
                        aSnappedParallel = !newPos.nextTo ? newPos.interval : null;
                        aSnappedNextTo = newPos.snapped && newPos.nextTo ? newPos.interval : null;
                        aPaddingType = newPos.paddingType;
                        secondRound = true;
                    } else if (inclusion1.parent.isSequential()) {
                        // compute inclusion for the other than snapped edge,
                        // it might want to go into a neighbor parallel group
                        aEdge = newPos.alignment ^ 1;
                        aSnappedParallel = null;
                        aSnappedNextTo = null;
                        aPaddingType = null;
                        secondRound = true;
                    } else {
                        secondRound = false;
                    }
                } else if (dragger.isResizing(dim^1) && originalPos2 != null) {
                     // renew the second original position
                    assert !dragger.isResizing(dim);
                    secondRound = true;
                    aEdge = originalPos2.alignment;
                    aSnappedParallel = originalPos2.snappedParallel;
                    aSnappedNextTo = originalPos2.snappedNextTo;
                    aPaddingType = originalPos2.paddingType;
                } else {
                    secondRound = false;
                }

                if (secondRound) {
                    // second round searching
                    analyzeParallel(root, inclusions);

                    if (inclusions.isEmpty()) { // no suitable inclusion found
                        assert aSnappedParallel != null;
                        if (originalPos2 != null && originalPos2.alignment == aEdge) {
                            inclusions.add(originalPos2);
                        } else {
                            addAligningInclusion(inclusions);
                        }
                    } else {
                        IncludeDesc preferred = addAligningInclusion(inclusions);
                        if (inclusions.size() > 1) {
                            if (preferred == null) { // [ && dragger.isResizing() ??? ]
                                preferred = originalPos2 != null ? originalPos2 : originalPos1;
                            }
                            mergeParallelInclusions(inclusions, preferred, false);
                            assert inclusions.size() == 1;
                        }
                    }
                    inclusion2 = inclusions.get(0);
                    inclusions.clear();

                    if (LayoutInterval.getRoot(inclusion1.parent) != root) {
                        // the first inclusion parent optimized out during the second mergeParallelInclusions
                        inclusion1.parent = inclusion2.parent;
                        inclusion1.newSubGroup = inclusion2.newSubGroup;
                        inclusion1.neighbor = inclusion2.neighbor;
                        inclusion1.index = inclusion2.index;
                        inclusion2 = null;
                    } else if (!dragger.isResizing() && newPos != null
                               && !inclusion1.parent.isParentOf(inclusion2.parent)) {
                        // secondary inclusion for the other than snapped edge not relevant
                        inclusion2 = null;
                    }
                }
            }

            if (!preferClosedPosition(inclusion1, originalPos1)) {
                cancelResizingOfMovingComponent();
            } else if (inclusion2 != null) {
                preferClosedPosition(inclusion2, originalPos2);
            }

            int m = mergeSequentialInclusions(inclusion1, inclusion2);
            if (m == 1 || m == 2) {
                if (m == 2) {
                    inclusion1 = inclusion2;
                }
                inclusion2 = null;
            }

            // now may detect more cases when the component needs to be set as resizing
            if (dragger.isResizing(dim)) {
                checkResizing2(inclusion1, inclusion2);
            }

            if (!unchangeDimension(inclusion1, inclusion2)) { // if not staying the same...
                addInterval(inclusion1, inclusion2,
                        !dragger.isResizing() && !LayoutUtils.getComponentIterator(root).hasNext());
            }
        }
    }

    /**
     * For selected components finds the intervals that represent them in a
     * common parent. If a representing interval contains other than selected
     * components, then the multiple components cannot be represented by a set
     * of intervals under one parent (selection does not make a coherent block).
     * @param compIntervals
     * @return list of intervals under one parent representing the components,
     *         or empty list if the components can't be represented
     */
    private static List<LayoutInterval> getIntervalsInCommonParent(LayoutInterval[] compIntervals) {
        List<LayoutInterval> inParent = null;
        LayoutInterval commonParent = LayoutInterval.getCommonParent(compIntervals);
        if (commonParent != null) {
            if (compIntervals.length == 1) {
                inParent = Collections.singletonList(compIntervals[0]);
            } else {
                Set<LayoutInterval> comps = new HashSet<LayoutInterval>();
                Collections.addAll(comps, compIntervals);
                if (commonParent != null) {
                    boolean found = false;
                    boolean previous = false;
                    boolean all = true;
                    for (int i=0; i < commonParent.getSubIntervalCount(); i++) {
                        LayoutInterval li = commonParent.getSubInterval(i);
                        if (!li.isEmptySpace()) {
                            int sel = isInSelectedComponents(li, compIntervals, true);
                            if (sel > 0) { // 'li' contains selected components and nothing else
                                if (!found) {
                                    found = true;
                                    inParent = new ArrayList<LayoutInterval>();
                                } else if (!previous && commonParent.isSequential()) {
                                    inParent.clear();
                                    break; // not a continuous sub-sequence
                                }
                                previous = true;
                                inParent.add(li);
                            } else if (sel < 0) { // 'li' does not contain any selected component
                                previous = false;
                                all = false;
                            } else { // 'li' contains both, not continuous
                                if (inParent != null) {
                                    inParent.clear();
                                }
                                break;
                            }
                        }
                    }
                    if (commonParent.isParallel() && commonParent.getParent() != null && found && all) {
                        // entire parallel group selected, use it as a whole
                        inParent.clear();
                        inParent.add(commonParent);
                    }
                }
            }
        }
        if (inParent == null) {
            inParent = Collections.emptyList();
        }
        return inParent;
    }

    private static int isInSelectedComponents(LayoutInterval interval,
                         LayoutInterval[] selectedComps, boolean firstLevel) {
        if (interval.isComponent()) {
            for (LayoutInterval li : selectedComps) {
                if (li == interval) {
                    return 1;
                }
            }
        } else if (interval.isGroup()) { // in subgroup all must be in selected
            boolean oneEnough = firstLevel && interval.isParallel();
            boolean inSelected = false;
            boolean notInSelected = false;
            for (int i=0; i < interval.getSubIntervalCount(); i++) {
                LayoutInterval li = interval.getSubInterval(i);
                if (!li.isEmptySpace()) {
                    int sel = isInSelectedComponents(li, selectedComps, false);
                    if (sel > 0) { // all from 'li' in selected
                        if (oneEnough) {
                            return 1;
                        }
                        inSelected = true;
                    } else if (sel < 0) { // nothing from 'li' in selected
                        notInSelected = true;
                    } else { // partially selected, not continuous
                        return 0;
                    }
                }
            }
            if (inSelected && !notInSelected) { // contains continuous block of selected
                return 1;
            } else if (!inSelected) { // does not contain any selected
                return -1;
            } else { // contains some selected, but not continuous
                return 0;
            }
        }
        return -1;
    }

    /**
     * Determines whether addingInterval that is being resized should be set to
     * auto-resizing. The analysis is based on current position (before the
     * interval is removed for new placement) and new position from dragger.
     * There's also checkResizing2 called later for situations this method can't detect.
     * @return true if the interval should be made resizing
     */
    private static Boolean checkResizing(LayoutInterval interval, LayoutDragger dragger, int dim) {
        LayoutDragger.PositionDef newPos = dragger.getPositions()[dim];
        int resizingEdge = dragger.getResizingEdge(dim);
        int fixedEdge = resizingEdge^1;
        Boolean resizing = null;

        if (newPos != null && newPos.snapped && newPos.interval != null) {
            int align1, align2;
            LayoutInterval parent;
            if (newPos.interval.isParentOf(interval)) {
                parent = newPos.interval;
                align1 = LayoutInterval.getEffectiveAlignmentInParent(interval, newPos.interval, fixedEdge);
                align2 = resizingEdge;
            }
            else {
                parent = LayoutInterval.getCommonParent(interval, newPos.interval);
                align1 = determineFixedEdgeAlignemnt(interval, parent, fixedEdge, dim); //LayoutInterval.getEffectiveAlignmentInParent(interval, parent, fixedEdge);
                align2 = align1 == resizingEdge ?
                        resizingEdge : // whole component tied to resizing edge
                        LayoutInterval.getEffectiveAlignmentInParent(newPos.interval, parent, newPos.nextTo ? fixedEdge : resizingEdge);
            }
            if ((align1 == resizingEdge && LayoutInterval.wantResize(interval.getParent()))
                || (newPos.nextTo && newPos.interval == LayoutInterval.getNeighbor(interval, resizingEdge, true, true, false)
                    && dragger.getMovingSpace().size(dim) <= dragger.getSizes()[dim].getOriginalSize())) {
                resizing = Boolean.FALSE;
            } else if (align1 != align2
                    && (align1 == LEADING || align1 == TRAILING) && (align2 == LEADING || align2 == TRAILING)
                    && (parent.getParent() == null || LayoutInterval.wantResize(parent))) {
                resizing = Boolean.TRUE;
            }
        }
        // [maybe we should consider also potential resizability of the component,
        //  not only on resizing operation - the condition should be:
        //  isComponentResizable(interval.getComponent(), dimension)  ]
        return resizing;
    }

    private static int determineFixedEdgeAlignemnt(LayoutInterval resizingInterval, LayoutInterval parent, int fixedEdge, int dimension) {
        if (parent.isSequential() && LayoutInterval.wantResize(resizingInterval)) {
            LayoutInterval parParent = LayoutInterval.getFirstParent(resizingInterval, PARALLEL);
            if (parent.isParentOf(parParent)
                    && LayoutInterval.isPlacedAtBorder(resizingInterval, parParent, dimension, fixedEdge)
                    && LayoutUtils.anythingAtGroupEdge(parParent, resizingInterval, dimension, fixedEdge)) {
                return fixedEdge;
            }
        }
        return LayoutInterval.getEffectiveAlignmentInParent(resizingInterval, parent, fixedEdge);
    }

    /**
     * Recognize situation when the new positions in given dimension look same
     * as the original ones and so would be better not to change anything.
     * In such case the original state in this dimension is restored.
     * @param ndesc1 description of the first new position
     * @param ndesc2 description of the second new position (for opposite edge), or null
     * @return true if the actual dimension should stay in original state that is
     *         also successfully restored
     */
    private boolean unchangeDimension(IncludeDesc ndesc1, IncludeDesc ndesc2) {
        if (undoMarks[dimension] == null || !layoutModel.getChangeMark().equals(undoCheckMark)) {
            return false; // also when a new components is being added
        }
        if (dragger.isResizing(dimension)) {
            return false;
        }
        IncludeDesc odesc1 = originalInclusion1;
        if (odesc1 == null || ndesc1 == null || odesc1.parent.getSubIntervalCount() == 0) {
            return false;
        }
        IncludeDesc odesc2 = originalInclusion2;
        if (ndesc2 != null && odesc2 == null) {
            return false;
        }

        if ((ndesc1.alignment == LEADING || ndesc1.alignment == TRAILING)
                && ndesc1.alignment != odesc1.alignment
                && odesc2 != null
                && ndesc1.alignment == odesc2.alignment) {
            IncludeDesc tmp = odesc1;
            odesc1 = odesc2;
            odesc2 = tmp;
        }

        int align = odesc1.alignment;
        if (ndesc1.alignment != DEFAULT && ndesc1.alignment != odesc1.alignment) {
            align = ndesc1.alignment;
        }
        boolean multi = selectedComponentIntervals[dimension].length > 1;
        boolean originalClosedAlignment = multi && originalPosition.isClosedAlignment();

        int dst = LayoutRegion.distance(originalSpace, addingSpace, dimension, CENTER, CENTER);
        if (dst >= -5 && dst <= 5 && originalClosedAlignment) {
            dst = 0; // hack for multiple components separate on baseline
        }
        if (dst != 0 && !equalNextTo(ndesc1, odesc1, align)) {
            return false;
        }
        if (!originalClosedAlignment && align != odesc1.alignment) {
            return plainAlignmentChange(ndesc1, odesc1);
        }

        boolean equalToOriginal = false;
        LayoutInterval np = ndesc1.parent; // new parent
        LayoutInterval op = odesc1.parent; // old parent
        if (np != op) {
            if (np.isParentOf(op)) { // moving to "wider" position
                if (np.isParallel()) {
                    if (ndesc1.neighbor == null) {
                        if (op.isParallel()) {
                            if (LayoutInterval.isClosedGroup(op, align^1)) {
                                equalToOriginal = true;
                            } else {
                                LayoutInterval neighbor = LayoutInterval.getNeighbor(op, align^1, true, true, false);
                                if (neighbor == null || !np.isParentOf(neighbor)) {
                                    equalToOriginal = true; // no neighbor that would make the wider position different
                                }
                            }
                        } else if (multi && odesc1.newSubGroup) {
                            equalToOriginal = true;
                        }
                    }
                } else if (op.isParallel()
                        && (equalNextTo(ndesc1, odesc1, align)
                           || equalSnap(ndesc1.snappedParallel, odesc1.snappedParallel, align))) {
                    equalToOriginal = true;
                }
            }
        } else if ((np.isParallel() && ndesc1.neighbor == odesc1.neighbor)
                || (np.isSequential() && ndesc1.newSubGroup == odesc1.newSubGroup)) {
            equalToOriginal = true;
        }
        if (equalToOriginal) {
            return restoreDimension();
        }
        return false;
    }

    private boolean restoreDimension() {
        boolean undone = false;
        List undoList = undoMarks[dimension];
        if (undoList != null) {
            for (int n=undoList.size()-1; n > 0; n-=2) {
                Object startMark = undoList.get(n-1);
                Object endMark = undoList.get(n);
                undone |= layoutModel.revert(startMark, endMark);
            }
            undoList.clear();
//            if (undone) {
//                Toolkit.getDefaultToolkit().beep();
//            }
        }
        return undone;
    }

    private static boolean equalNextTo(IncludeDesc iDesc1, IncludeDesc iDesc2, int alignment) {
        if (iDesc1.parent.isSequential() && iDesc1.snappedNextTo != null) {
            if (iDesc1.parent == iDesc2.parent && iDesc1.snappedNextTo == iDesc2.snappedNextTo) {
                return iDesc1.paddingType == iDesc2.paddingType;
            } else if (iDesc2.snappedParallel != null
                    && (iDesc1.parent == iDesc2.parent
                        || (iDesc1.parent.isParentOf(iDesc2.parent) && iDesc1.newSubGroup && (iDesc2.parent.isParallel() || iDesc2.newSubGroup)))) {
                // snap next to is equal to align in parallel with something that is next to the same thing
                LayoutInterval neighbor = LayoutInterval.getNeighbor(iDesc2.snappedParallel, alignment, false, true, true);
                if (neighbor != null && neighbor.isEmptySpace() // && neighbor.getPaddingType() == iDesc1.paddingType
                        && neighbor.getPreferredSize() == NOT_EXPLICITLY_DEFINED) {
                    neighbor = LayoutInterval.getDirectNeighbor(neighbor, alignment, false);
                    if (neighbor == null) {
                        neighbor = LayoutInterval.getRoot(iDesc2.snappedParallel);
                    }
                    if (equalSnap(neighbor, iDesc1.snappedNextTo, alignment)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean equalSnap(LayoutInterval interval1, LayoutInterval interval2, int alignment) {
        if (interval1 != null && interval2 != null) {
            if (interval1 == interval2) {
                return true;
            }
            if (alignment == LEADING || alignment == TRAILING) {
                if (interval1.isParentOf(interval2)) {
                    return LayoutInterval.isAlignedAtBorder(interval2, interval1, alignment);
                } else if (interval2.isParentOf(interval1)) {
                    return LayoutInterval.isAlignedAtBorder(interval1, interval2, alignment);
                } else {
                    return LayoutUtils.alignedIntervals(interval1, interval2, alignment);
//                    LayoutInterval parent = LayoutInterval.getCommonParent(interval1, interval2);
//                    return parent != null && parent.isParallel()
//                           && LayoutInterval.isAlignedAtBorder(interval1, parent, alignment)
//                           && LayoutInterval.isAlignedAtBorder(interval2, parent, alignment);
                }
            } else if (alignment == CENTER || alignment == BASELINE) {
                return (interval1 == interval2.getParent() && interval1.getGroupAlignment() == alignment && interval2.getAlignment() == alignment)
                        || (interval2 == interval1.getParent() && interval2.getGroupAlignment() == alignment && interval1.getAlignment() == alignment)
                        || (interval1.getParent() == interval2.getParent() && interval1.getAlignment() == alignment && interval2.getAlignment() == alignment);
            }
        }
        return false;
    }

    private boolean plainAlignmentChange(IncludeDesc ndesc, IncludeDesc odesc) {
        if (closedSpace != null && odesc.snappedParallel != null) {
            if (restoreDimension()) {
                LayoutInterval li = dragger.getMovingComponents()[0].getLayoutInterval(dimension);
                while (li.getParent() != null) {
                    if (li.getParent().isParallel()) {
                        layoutModel.setIntervalAlignment(li, ndesc.alignment);
                        break;
                    }
                    li = li.getParent();
                }
                return true;
            }
        }
        return false;
    }

    // -----
    // overlap analysis

    private int getDimensionSolvingOverlap(LayoutDragger.PositionDef[] positions) {
        if (dragger.isResizing(HORIZONTAL) && !dragger.isResizing(VERTICAL)) {
            return HORIZONTAL;
        }
        if ((dragger.isResizing(VERTICAL) && !dragger.isResizing(HORIZONTAL))
            || (positions[HORIZONTAL] != null && positions[HORIZONTAL].snapped && (positions[VERTICAL] == null || !positions[VERTICAL].snapped))
            || (positions[VERTICAL] != null && !positions[VERTICAL].nextTo && positions[VERTICAL].snapped
                && (positions[VERTICAL].interval.getParent() == null)
                && !existsComponentPlacedAtBorder(positions[VERTICAL].interval, VERTICAL, positions[VERTICAL].alignment))) {
            return VERTICAL;
        }
        if (positions[VERTICAL] != null && positions[VERTICAL].nextTo && positions[VERTICAL].snapped
            && (positions[VERTICAL].interval.getParent() == null)) {
            int alignment = positions[VERTICAL].alignment;
            int[][] overlapSides = overlappingGapSides(dragger.getTargetRoots()[HORIZONTAL],
                                                       dragger.getMovingSpace());
            if (((alignment == LEADING) || (alignment == TRAILING))
                && (overlapSides[VERTICAL][1-alignment] != 0)
                && (overlapSides[VERTICAL][alignment] == 0)) {
                return VERTICAL;
            }
        }
        if ((positions[HORIZONTAL] == null || !positions[HORIZONTAL].snapped)
            && (positions[VERTICAL] == null || !positions[VERTICAL].snapped)) {
            boolean[] overlapDim = overlappingGapDimensions(dragger.getTargetRoots()[HORIZONTAL],
                                                            dragger.getMovingSpace());
            if (overlapDim[VERTICAL] && !overlapDim[HORIZONTAL]) {
                return VERTICAL;
            }
        }
        return HORIZONTAL;
    }

    /**
     * Checks whether there is a component placed at the border
     * of the specified interval.
     *
     * @param interval interval to check.
     * @param dimension dimension that should be considered.
     * @param alignment alignment that should be considered.
     */
    private static boolean existsComponentPlacedAtBorder(LayoutInterval interval, int dimension, int alignment) {
        Iterator iter = interval.getSubIntervals();
        while (iter.hasNext()) {
            LayoutInterval subInterval = (LayoutInterval)iter.next();
            if (LayoutInterval.isPlacedAtBorder(interval, dimension, alignment)) {
                if (subInterval.isComponent()) {
                    return true;
                } else if (subInterval.isGroup()) {
                    if (existsComponentPlacedAtBorder(subInterval, dimension, alignment)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Fills the given list by components that overlap with the <code>region</code>.
     *
     * @param overlaps list that should be filled by overlapping components.
     * @param group layout group that is scanned by this method.
     * @param region region to check.
     */
    private static void fillOverlappingComponents(List<LayoutComponent> overlaps, LayoutInterval group, LayoutRegion region) {
        Iterator iter = group.getSubIntervals();
        while (iter.hasNext()) {
            LayoutInterval subInterval = (LayoutInterval)iter.next();
            if (subInterval.isGroup()) {
                fillOverlappingComponents(overlaps, subInterval, region);
            } else if (subInterval.isComponent()) {
                LayoutComponent component = subInterval.getComponent();
                LayoutRegion compRegion = subInterval.getCurrentSpace();
                if (LayoutRegion.overlap(compRegion, region, HORIZONTAL, 0)
                    && LayoutRegion.overlap(compRegion, region, VERTICAL, 0)) {
                    overlaps.add(component);
                }
            }
        }
    }

    // Helper method for getDimensionSolvingOverlap() method
    private static boolean[] overlappingGapDimensions(LayoutInterval layoutRoot, LayoutRegion region) {
        boolean[] result = new boolean[2];
        int[][] overlapSides = overlappingGapSides(layoutRoot, region);
        for (int i=0; i<DIM_COUNT; i++) {
            result[i] = (overlapSides[i][0] == 1) && (overlapSides[i][1] == 1);
        }
        return result;
    }

    // Helper method for getDimensionSolvingOverlap() method
    private static int[][] overlappingGapSides(LayoutInterval layoutRoot, LayoutRegion region) {
        int[][] overlapSides = new int[][] {{0,0},{0,0}};
        List<LayoutComponent> overlaps = new LinkedList<LayoutComponent>();
        fillOverlappingComponents(overlaps, layoutRoot, region);
        Iterator<LayoutComponent> iter = overlaps.iterator();
        while (iter.hasNext()) {
            LayoutComponent component = iter.next();
            LayoutRegion compRegion = component.getLayoutInterval(HORIZONTAL).getCurrentSpace();
            for (int i=0; i<DIM_COUNT; i++) {
                int[] edges = overlappingSides(compRegion, region, i);
                for (int j=0; j<2; j++) {
                    if (edges[j] == 1) {
                        overlapSides[i][j] = 1;
                    } else if (edges[j] == -1) {
                        if (overlapSides[i][j] == -1) {
                            overlapSides[i][j] = 1;
                        } else if (overlapSides[i][j] == 0) {
                            overlapSides[i][j] = -1;
                        }
                    }
                }
            }
        }
        return overlapSides;
    }

    // Helper method for overlappingGapSides() method
    private static int[] overlappingSides(LayoutRegion compRegion, LayoutRegion region, int dimension) {
        int[] sides = new int[2];
        int compLeading = compRegion.positions[dimension][LEADING];
        int compTrailing = compRegion.positions[dimension][TRAILING];
        int regLeading = region.positions[dimension][LEADING];
        int regTrailing = region.positions[dimension][TRAILING];
        if ((regLeading < compTrailing) && (compTrailing < regTrailing)) {
            sides[0] = 1;
        }
        if ((regLeading < compLeading) && (compLeading < regTrailing)) {
            sides[1] = 1;
        }
        if ((sides[0] == 1) &&  (sides[1] == 1)) {
            sides[0] = sides[1] = -1;
        }
        return sides;
    }

    // -----
    // the following methods work in context of adding to actual dimension

    /**
     * An auto-resizing component that is being moved needs to be set to fixed
     * under certain conditions not to grow on the new location. Depending on
     * where it is placed in the end, its resizing may be re-enabled.
     */
    private void cancelResizingOfMovingComponent() {
        if (!dragger.isResizing() && originalPosition != null && originalPosition.isSuppressedResizing()) {
            Object start = layoutModel.getChangeMark();
            if (addingInterval.isComponent()) {
                assert LayoutInterval.wantResize(addingInterval);
                operations.eliminateResizing(addingInterval, dimension, null);
            } else if (addingInterval.isParallel()) {
                addingInterval.setMaximumSize(USE_PREFERRED_SIZE);
            }
            Object end = layoutModel.getChangeMark();
            setUndo(start, end, dimension); // this change should be undone if doing restoreDimension()
            if (start.equals(undoCheckMark)) {
                undoCheckMark = end;
            }
        }
    }

    /**
     * Completes check for auto-resizing when exact inclusions are known for the
     * adding interval (that has been resized). Here we detect situations when
     * it should be made resizing just to accommodate to fixed size of some other
     * components, in result forming a parallel group with suppressed resizing.
     */
    private void checkResizing2(IncludeDesc iDesc1, IncludeDesc iDesc2) {
        // The checkResizing method detected resizing when a component spans
        // over resizing space (edges have opposite anchors). 
        if (!LayoutInterval.wantResize(addingInterval)
                && becomeResizing[dimension] == null
                && (iDesc1.snapped() || iDesc1.fixedPosition)
                && iDesc2 != null && iDesc2.snapped()) {
            assert dragger.getResizingEdge(dimension) == iDesc2.alignment;
            // the fixed edge
            LayoutInterval snap1;
            if (iDesc1.snapped()) {
                snap1 = iDesc1.snappedParallel != null ? iDesc1.snappedParallel : iDesc1.snappedNextTo;
            } else if (iDesc1.neighbor != null) {
                snap1 = iDesc1.neighbor;
            } else if (iDesc1.parent.isParallel()) {
                snap1 = iDesc1.parent;
            } else { // in a sequence - find the neighbor
                snap1 = null;
                LayoutInterval p = iDesc1.parent;
                int i = iDesc1.index;
                if (iDesc1.alignment == LEADING && i >= p.getSubIntervalCount()) {
                    i = p.getSubIntervalCount() - 1;
                }
                while (i >= 0 && i < p.getSubIntervalCount()) {
                    LayoutInterval li = p.getSubInterval(i);
                    if (!li.isEmptySpace()) {
                        snap1 = li;
                        break;
                    }
                    i += (iDesc1.alignment == TRAILING ? 1 : -1);
                }
                if (snap1 == null) {
                    snap1 = iDesc1.parent.getParent();
                }
            }
            // the resizing edge snap
            LayoutInterval snap2 = iDesc2.snappedParallel != null
                                   ? iDesc2.snappedParallel : iDesc2.snappedNextTo;
            if (snapEqualToParallelSnap(snap2, snap2==iDesc2.snappedParallel, iDesc2.alignment, snap1)
                    && (snapEqualToParallelSnap(snap1, snap1==iDesc1.snappedParallel, iDesc1.alignment, snap2)
                        || tiedToParallelSnap(snap1, iDesc1.alignment, snap2))) {
                // have parallel snap on both sides
                operations.setIntervalResizing(addingInterval, true);
            }
        }
    }

    /**
     * @return true if given inclusion snaps to some component in parallel, or
     *         there is a component to which it could equally snap to sequentially
     */
    private boolean snapEqualToParallelSnap(LayoutInterval snapped, boolean parallel, int alignment, LayoutInterval otherSnapped) {
        if (snapped.getParent() == null) { // snapped to root
            LayoutInterval group = otherSnapped.isGroup() ? otherSnapped : otherSnapped.getParent();
            while (group != snapped) {
                if (LayoutInterval.isPlacedAtBorder(group, snapped, dimension, alignment)) {
                    break;
                }
                group = group.getParent();
            }
            return LayoutUtils.anythingAtGroupEdge(group, null, dimension, alignment);
        }
        if (parallel) {
            return true;
        }
        LayoutInterval neighborGap = LayoutInterval.getDirectNeighbor(snapped, alignment^1, false);
        return neighborGap != null && neighborGap.isEmptySpace()
                && neighborGap.getPreferredSize() == NOT_EXPLICITLY_DEFINED
                && neighborGap.getDiffToDefaultSize() == 0;
    }

    private boolean tiedToParallelSnap(LayoutInterval snapped, int alignment, LayoutInterval otherSnapped) {
        LayoutInterval tieParent;
        if (otherSnapped == null || otherSnapped.getParent() == null) {
            tieParent = LayoutInterval.getFirstParent(snapped, PARALLEL);
        } else if (otherSnapped.isParallel() && otherSnapped.isParentOf(snapped)) {
            tieParent = otherSnapped;
        } else {
            tieParent = LayoutInterval.getCommonParent(snapped, otherSnapped);
            if (tieParent != null && tieParent.isSequential()) {
                LayoutInterval p = snapped;
                if (p != tieParent) {
                    while (p.getParent() != tieParent) {
                        p = p.getParent();
                    }
                }
                tieParent = p.isParallel() ? p : null;
            }
        }
        if (tieParent != null) {
            if (tieParent.isParentOf(snapped)) {
                snapped = tiedToParent(snapped, tieParent, false, dimension, alignment);
            } // otherwise snapped is likely root
            if (snapped != null && LayoutUtils.anythingAtGroupEdge(tieParent, snapped, dimension, alignment)) {
                return true;
            }
        }
        return false;
    }

    private static LayoutInterval tiedToParent(LayoutInterval interval, LayoutInterval tieParent, boolean defGap, int dimension, int alignment) {
        if (LayoutInterval.wantResize(interval)) {
            return null;
        }
        LayoutInterval parParent = interval;
        while (parParent != tieParent) {
            interval = parParent;
            LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(interval, alignment, false);
            while (neighbor != null) {
                if (LayoutInterval.canResize(neighbor)
                    || (neighbor.isEmptySpace() && defGap
                        && neighbor.getPreferredSize() != NOT_EXPLICITLY_DEFINED)) {
                    return null;
                }
                interval = neighbor;
                neighbor = LayoutInterval.getDirectNeighbor(interval, alignment, false);
            }
            if (interval.isEmptySpace()) {
                interval = interval.getParent();
                assert interval.isSequential();
            }
            parParent = LayoutInterval.getFirstParent(interval, PARALLEL);
            if (!LayoutInterval.isPlacedAtBorder(interval, parParent, dimension, alignment)) {
                return null;
            }
        }

        while (interval != tieParent && interval != null && interval.getParent() != tieParent) {
            interval = interval.getParent();
        }
        return interval;
    }

    /**
     * Adds aligned with an interval to existing group, or creates new.
     * (Now used only to a limited extent for closed groups only.)
     */
    private void addSimplyAligned() {
        int alignment = aEdge;
        assert alignment == CENTER || alignment == BASELINE;
        layoutModel.setIntervalAlignment(addingInterval, alignment);

        LayoutInterval group;
        LayoutRegion currentSpace;
        if (aSnappedParallel.isParallel() && aSnappedParallel.getGroupAlignment() == alignment) {
            group = aSnappedParallel;
            currentSpace = aSnappedParallel.getCurrentSpace();
        } else {
            group = aSnappedParallel.getParent();
            if (group.isParallel() && group.getGroupAlignment() == alignment) {
                currentSpace = group.getCurrentSpace();
            } else {
                int alignIndex = layoutModel.removeInterval(aSnappedParallel);
                LayoutInterval subGroup = new LayoutInterval(PARALLEL);
                subGroup.setGroupAlignment(alignment);
                if (group.isParallel()) {
                    subGroup.setAlignment(aSnappedParallel.getAlignment());
                }
                layoutModel.setIntervalAlignment(aSnappedParallel, alignment);
                layoutModel.addInterval(aSnappedParallel, subGroup, -1);
                layoutModel.addInterval(subGroup, group, alignIndex);
                group = subGroup;
                currentSpace = aSnappedParallel.getCurrentSpace();
            }
        }
        layoutModel.addInterval(addingInterval, group, -1);

        // adjusting surrounding gaps if growing
        for (int e=LEADING; e <= TRAILING; e++) {
            LayoutInterval gap = LayoutInterval.getNeighbor(group, e, false, true, false);
            if (gap == null || !gap.isEmptySpace() || LayoutInterval.isDefaultPadding(gap)) {
                continue;
            }
            int growth;
            if (gap.getParent() == group.getParent()) { // direct gap
                growth = LayoutRegion.distance(addingSpace, currentSpace, dimension, e, e);
            } else {
                LayoutInterval superGroup = LayoutInterval.getDirectNeighbor(gap, e^1, true);
                growth = LayoutRegion.distance(addingSpace, superGroup.getCurrentSpace(), dimension, e, e);
            }
            if (e == TRAILING) {
                growth *= -1;
            }
            if (growth > 0) {
                LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(gap, e, true);
                int npos = (neighbor != null) ?
                    neighbor.getCurrentSpace().positions[dimension][e^1] :
                    LayoutInterval.getFirstParent(gap, PARALLEL).getCurrentSpace().positions[dimension][e];
                int dist = addingSpace.positions[dimension][e] - npos;
                if (e == TRAILING) {
                    dist *= -1;
                }
                if (dist < LayoutInterval.getCurrentSize(gap, dimension) && dist > 0) {
                    operations.resizeInterval(gap, dist);
                }
            }
        }
    }

    private void addInterval(IncludeDesc iDesc1, IncludeDesc iDesc2, boolean newRoot) {
        addToGroup(iDesc1, iDesc2, true);

        List<LayoutInterval> added = getAddedIntervals();

        // align in parallel if required
        if (iDesc1.snappedParallel != null || (iDesc2 != null && iDesc2.snappedParallel != null)) {
            if (iDesc2 != null && iDesc2.snappedParallel != null) {
                boolean dangerousAligning = iDesc1.snappedParallel != null && iDesc1.snappedParallel.getParent() != null
                                            && iDesc1.snappedParallel.isParentOf(iDesc2.snappedParallel);
                LayoutInterval group = alignInParallel(getAlignRep(added, iDesc2.alignment), iDesc2.snappedParallel, iDesc2.alignment);
                if (dangerousAligning && iDesc1.snappedParallel.getParent() == null) {
                    // this aligning eliminated (replaced) the parallel group referred to in iDesc1.snappedParallel
                    iDesc1.snappedParallel = group; // should be already aligned at what we need
                }
            }
            if (iDesc1.snappedParallel != null) {
                if (iDesc1.snappedParallel.getParent() == null && iDesc1.snappedParallel.getSubIntervalCount() == 0
                        && iDesc1 == originalInclusion1) {
                    // Attempt to workaround bug #222436. The assumption is it might happen during resizing
                    // a component: the fixed edge was aligned in parallel with some parent and this parent
                    // got optimized out somewhere during adding the component. We can determine the align-with
                    // interval again from the original position.
                    iDesc1.snappedParallel = LayoutPosition.getInclusionFromOriginal(originalPosition, dimension, iDesc1.alignment).snappedParallel;
                }
                alignInParallel(getAlignRep(added, iDesc1.alignment), iDesc1.snappedParallel, iDesc1.alignment);
            }
        }

        // may want to disable resizing of parallel parent group
        checkParallelResizing(added, iDesc1, iDesc2);

        if (!newRoot) { // adjust to grown content
            accommodateOutPosition(added);
        }
        LayoutInterval interval = getAddedIntervals().get(0); // get again, groups might have changed

        if (dragger.isResizing(dimension) && LayoutInterval.wantResize(interval)) {
            operations.suppressResizingOfSurroundingGaps(interval);
        }

        // avoid unnecessary parallel group nesting
        operations.mergeParallelGroups(LayoutInterval.getRoot(selectedComponentIntervals[dimension][0]));

        // optimize repeating gaps at the edges of parallel parent group
        LayoutInterval parent = null;
        do {
            interval = getAddedIntervals().get(0); // get again, groups might have changed
            LayoutInterval p = LayoutInterval.getFirstParent(interval, PARALLEL);
            if (p != parent) { // repeat at the same level if it re-arranges groups
                parent = p;
                operations.optimizeGaps(parent, dimension);
            } else { // then go up
                parent = LayoutInterval.getFirstParent(parent, PARALLEL);
                while (parent != null) {
                    operations.optimizeGaps(parent, dimension);
                    parent = LayoutInterval.getFirstParent(parent, PARALLEL);
                }
            }
        } while (parent != null);
    }

    private List<LayoutInterval> getAddedIntervals() {
        List<LayoutInterval> added = getIntervalsInCommonParent(selectedComponentIntervals[dimension]);
        if (added.isEmpty()) {
            added = Collections.singletonList(selectedComponentIntervals[dimension][0]);
        }
        return added;
    }

    private LayoutInterval getAlignRep(List<LayoutInterval> added, int alignment) {
        LayoutInterval first = added.get(0);
        if (added.size() == 1) {
            return first;
        } else {
            LayoutInterval parent = first.getParent();
            if (parent.isSequential()) {
                return alignment != TRAILING ? first : added.get(added.size()-1);
            } else {
                return parent;
            }
        }
    }

    private void addToGroup(IncludeDesc iDesc1, IncludeDesc iDesc2, boolean definite) {
        assert iDesc2 == null || (iDesc1.parent == iDesc2.parent
                                  && iDesc1.newSubGroup == iDesc2.newSubGroup
                                  && iDesc1.neighbor == iDesc2.neighbor);
        LayoutInterval root = dragger.getTargetRoots()[dimension];
        checkRoot(iDesc1.parent, root, null, dimension);
        checkRoot(iDesc1.snappedParallel, root, iDesc1.parent, dimension);
        if (iDesc2 != null) {
            checkRoot(iDesc2.snappedParallel, root, iDesc2.parent, dimension);
        }

        LayoutInterval parent = iDesc1.parent;
        LayoutInterval seq = null;
        boolean subseq = false;
        int index = 0;

        if (parent.isSequential()) {
            if (iDesc1.newSubGroup) {
                LayoutRegion space = closedSpace == null ? addingSpace : closedSpace;
                int closeAlign1 = getExtractCloseAlign(iDesc1);
                int closeAlign2 = getExtractCloseAlign(iDesc2);
                if (closeAlign1 == DEFAULT && closedSpace != null && iDesc1.index > 0) {
                    closeAlign1 = (iDesc1.alignment != TRAILING) ? LEADING : TRAILING;
                }
                if (closeAlign2 == DEFAULT && closedSpace != null && iDesc1.index < iDesc1.parent.getSubIntervalCount()) {
                    closeAlign2 = (iDesc1.alignment != TRAILING) ? TRAILING : LEADING;
                }
                LayoutInterval subgroup = extractParallelSequence(
                        parent, space, closeAlign1, closeAlign2, iDesc1.alignment, null);
                if (subgroup != null) {
                    seq = new LayoutInterval(SEQUENTIAL);
                    parent = subgroup;
                    subseq = true;
                }
            }
            if (seq == null) {
                seq = parent;
                parent = seq.getParent();
                index = iDesc1.index;
            }
            if (iDesc2 != null && iDesc2.alignment == dragger.getResizingEdge(dimension)) {
                alignWithResizingInSubgroup(seq, parent, iDesc2);
            }
        } else { // parallel parent
            LayoutInterval neighbor = iDesc1.neighbor;
            if (neighbor != null) {
                checkNeighbor(neighbor, parent, dimension);
                seq = new LayoutInterval(SEQUENTIAL);
                layoutModel.addInterval(seq, parent, layoutModel.removeInterval(neighbor));
                seq.setAlignment(neighbor.getAlignment());
                layoutModel.setIntervalAlignment(neighbor, DEFAULT);
                layoutModel.addInterval(neighbor, seq, 0);
                if (iDesc1.index > -1) {
                    index = iDesc1.index;
                } else if (getAddDirection(neighbor, iDesc1.alignment) == TRAILING) {
                    index = 1;
                } // otherwise 0
            } else {
                seq = new LayoutInterval(SEQUENTIAL);
                if (iDesc1.snapped()) {
                    seq.setAlignment(iDesc1.alignment);
                }
            }
        }

        assert iDesc1.alignment >= 0 || iDesc2 == null;
        assert iDesc2 == null || iDesc2.alignment == (iDesc1.alignment^1);
        assert parent.isParallel();
        checkRoot(parent, root, iDesc1.parent, dimension);

        LayoutInterval[] outBounds = new LayoutInterval[2]; // outermost boundary intervals (not gaps)
        boolean[] span = new boolean[2]; // [what it really is??]
        boolean[] outOfGroup = new boolean[2];
        boolean[] expanded = new boolean[2];
        LayoutInterval[] neighbors = new LayoutInterval[2]; // direct neighbors in the sequence (not gaps)
        LayoutInterval[] gaps = new LayoutInterval[2]; // new gaps to create
        LayoutInterval originalGap = null;
        boolean minorOriginalGap;
        int[] centerDst = new int[2]; // LEADING, TRAILING

        // find the neighbors for the adding interval and determine the original gap
        int count = seq.getSubIntervalCount();
        if (index > count)
            index = count;
        for (int i = LEADING; i <= TRAILING; i++) {
            int idx1 = i == LEADING ? index - 1 : index;
            int idx2 = i == LEADING ? index - 2 : index + 1;
            if (idx1 >= 0 && idx1 < count) {
                LayoutInterval li = seq.getSubInterval(idx1);
                if (li.isEmptySpace()) {
                    originalGap = li;
                    if (idx2 >= 0 && idx2 < count) {
                        neighbors[i] = seq.getSubInterval(idx2);
                    }
                } else  {
                    neighbors[i] = li;
                }
            }
            IncludeDesc iiDesc = (iDesc1.alignment < 0 || iDesc1.alignment == i) ? iDesc1 : iDesc2;

            if (iiDesc != null && iiDesc.snappedParallel != null) {
                span[i] = true;
            } else if (iiDesc != null && iiDesc.snappedNextTo != null) {
                if (!parent.isParentOf(iiDesc.snappedNextTo)) {
                    LayoutInterval li = parent;
                    LayoutInterval gap = null;
                    do {
                        li = LayoutInterval.getNeighbor(li, i, false, true, false);
                        if (li != null) {
                            if (li.isEmptySpace()) {
                                gap = li;
                            }
                        }
                    } while (li != null && li != iiDesc.snappedNextTo && !li.isParentOf(iiDesc.snappedNextTo));
                    if (gap != null && (li != null || iiDesc.snappedNextTo.getParent() == null)
                            && LayoutInterval.isDefaultPadding(gap)) {
                        span[i] = true;
                    }
                }
            }
            outOfGroup[i] = stickingOutOfGroup(parent, i);
            if (neighbors[i] != null) {
                outBounds[i] = neighbors[i];
            } else {
                outBounds[i] = getPerceivedParentNeighbor(parent, addingSpace, outOfGroup[i], dimension, i);
            }
            if (definite && neighbors[i] == null) {
                if (!subseq && parent.getParent() != null
                        && outOfGroup[i]
                        && (LayoutInterval.canResize(parent) || !LayoutInterval.wantResize(addingInterval))
                        && shouldExpandOverGroupEdge(parent, outBounds[i], i)) {
                    // adding over group edge that is not apparent to the user (doesn't expect it to move)
                    if (seq.getParent() == null) {
                        layoutModel.addInterval(seq, parent, -1); // temporary
                        parent = separateSequence(seq, i, iDesc1, iDesc2);
                        checkRoot(parent, root, iDesc1.parent, dimension);
                        layoutModel.removeInterval(seq);
                        expanded[i] = true;
                    } else {
                        parent = separateSequence(seq, i, iDesc1, iDesc2);
                        checkRoot(parent, root, iDesc1.parent, dimension);
                        expanded[i] = true;
                    }
                } else if (subseq && iDesc1.parent.getParent().getParent() != null
                        && stickingOutOfGroup(iDesc1.parent.getParent(), i)
                        && shouldExpandOverGroupEdge(iDesc1.parent.getParent(), outBounds[i], i)) {
                    // adding over group edge that is not apparent to the user (doesn't expect it to move)
                    boolean parentAligned = LayoutInterval.getEffectiveAlignmentInParent(parent, null, i) == i;
                    LayoutInterval p = separateSequence(iDesc1.parent, i, iDesc1, iDesc2);
                    if (parent.getSubIntervalCount() == 0 && parent.getParent() == null) {
                        parent = p; // optimized out during the operation
                        checkRoot(parent, root, iDesc1.parent, dimension);
                    } else {
                        checkRoot(parent, root, iDesc1.parent, dimension);
                        int outPos = p.getCurrentSpace().positions[dimension][i];
                        int size = (outPos - parent.getCurrentSpace().positions[dimension][i]) * (i==LEADING ? -1:1);
                        if (size > 0 && LayoutInterval.getEffectiveAlignmentInParent(parent, p, i) == i) {
                            // group gained more space by the parallelization, may need a support gap
                            boolean supportedInside = false;
                            for (int ii=0; ii < parent.getSubIntervalCount(); ii++) {
                                LayoutInterval sibling = parent.getSubInterval(ii);
                                if (LayoutInterval.isAlignedAtBorder(sibling, i)) {
                                    LayoutInterval supportGap = new LayoutInterval(SINGLE);
                                    supportGap.setSizes(parentAligned ? USE_PREFERRED_SIZE : NOT_EXPLICITLY_DEFINED,
                                                        size, parentAligned ? USE_PREFERRED_SIZE : Short.MAX_VALUE);
                                    operations.insertGap(supportGap, sibling, outPos, dimension, i);
                                    supportedInside = true;
                                }
                            }
                            if (!supportedInside) {
                                size = (outPos - addingSpace.positions[dimension][i]) * (i==LEADING ? -1:1);
                                if (size > 0) {
                                    LayoutInterval supportGap = new LayoutInterval(SINGLE);
                                    supportGap.setSizes(parentAligned ? USE_PREFERRED_SIZE : NOT_EXPLICITLY_DEFINED,
                                                        size, parentAligned ? USE_PREFERRED_SIZE : Short.MAX_VALUE);
                                    operations.insertGap(supportGap, parent.getParent(), outPos, dimension, i);
                                }
                            }
                        }
                        setCurrentPositionToParent(parent, p, dimension, i);
                    }
                }
            }
            if (iDesc1.alignment < 0) { // no alignment known
                centerDst[i] = addingSpace.positions[dimension][CENTER]
                        - (outBounds[i] == parent || outBounds[i].isParentOf(parent) ?
                              outBounds[i].getCurrentSpace().positions[dimension][i] : 
                              getPerceivedNeighborPosition(outBounds[i], addingSpace, dimension, i));
                if (i == TRAILING) {
                    centerDst[i] *= -1;
                }
            }
        }
        minorOriginalGap = originalGap != null && !LayoutInterval.canResize(originalGap)
                && ((neighbors[LEADING] == null && LayoutInterval.getEffectiveAlignment(neighbors[TRAILING], LEADING, true) == TRAILING)
                  || (neighbors[TRAILING] == null && LayoutInterval.getEffectiveAlignment(neighbors[LEADING], TRAILING, true) == LEADING));

        // compute leading and trailing gaps
        int edges = 2;
        for (int i=LEADING; edges > 0; i^=1, edges--) {
            gaps[i] = null;
            LayoutInterval outerNeighbor = neighbors[i] == null ?
                    LayoutInterval.getNeighbor(parent, i, false, true, false) : null;
            IncludeDesc iiDesc, otherDesc;
            if (iDesc1.alignment < 0 || iDesc1.alignment == i) {
                iiDesc = iDesc1;
                otherDesc = iDesc2;
            } else {
                iiDesc = iDesc2;
                otherDesc = iDesc1;
            }

            if (neighbors[i] == null && iiDesc != null) { // at the start/end of the sequence
                if (iiDesc.snappedNextTo != null
                    && outerNeighbor != null && LayoutInterval.isDefaultPadding(outerNeighbor))
                {   // the padding is outside of the parent already
                    continue;
                }
                if (iiDesc.snappedParallel != null) {
                    if (seq.isParentOf(iiDesc.snappedParallel)) {
                        if (originalGap == null) {
                            continue;
                        }
                    } else if (canAlignWith(iiDesc.snappedParallel, parent, i)) {
                        continue;
                    }
                }
            }

            boolean aligned;
            if (iDesc2 == null) { // one position defined
                if (iiDesc != null && !iiDesc.snapped() && dragger.isResizing() && originalPosition != null) {
                    // resizing - keep original alignment
                    aligned = originalPosition.atFixedPosition(i);
                } else if (iDesc1.alignment < 0) {
                    // no specific alignment - decide based on distance
                    aligned = centerDst[i] < centerDst[i^1]
                          || (centerDst[i] == centerDst[i^1] && i == LEADING);
                } else {
                    aligned = (i == iDesc1.alignment);
                    if (iDesc1.snappedParallel != null) {
                        if (seq.isParentOf(iDesc1.snappedParallel)) {
                            // special case - aligning with interval in the same sequence - to subst. its position
                            aligned = (i == (iDesc1.alignment^1));
                        } else if (parent.isParentOf(iDesc1.snappedParallel)
                                && originalGap != null && LayoutInterval.canResize(originalGap)) {
                            // follow alignment of the snapped interval (see ALT_Positioning18Test situation)
                            int relatedAlign = LayoutInterval.getEffectiveAlignmentInParent(iDesc1.snappedParallel, parent, iDesc1.alignment);
                            if (relatedAlign == LEADING || relatedAlign == TRAILING) {
                                aligned = (i == relatedAlign);
                            }
                        }
                    }
                }
            } else { // both positions defined
                if (dragger.isResizing(dimension)) {
                    if (LayoutInterval.wantResize(addingInterval)) {
                        aligned = true;
                    } else if (dragger.getResizingEdge(dimension) == i
                            && originalPosition != null && !originalPosition.atFixedPosition(i)
                            && originalGap != null && LayoutInterval.canResize(originalGap)) {
                        aligned = false;
                    } else {
                        aligned = iiDesc.fixedPosition || (originalPosition != null && originalPosition.atFixedPosition(i));
                    }
                } else {
                    aligned = iiDesc.fixedPosition
                            || (dragger.isResizing() && originalPosition != null && originalPosition.atFixedPosition(i));
                }
            }

            boolean minorGap = false;
            boolean noMinPadding = false;
            LayoutInterval otherPar = otherDesc != null ? otherDesc.snappedParallel : null;
            checkRoot(parent, root, iDesc1.parent, dimension);
            checkRoot(otherPar, root, iDesc1.parent, dimension);
            if (!aligned && neighbors[i] == null) { // at the end of the sequence
                if (originalGap == null) {
                    if (outerNeighbor != null && outerNeighbor.isEmptySpace()) {
                        //continue; // unaligned ending gap not needed - there's a gap outside the parent
                        minorGap = true;
                        noMinPadding = true;
                    } else if (otherPar != null && otherPar.getParent() != null) {
                        minorGap = parent.isParentOf(otherPar)
                            || LayoutInterval.getCount(parent, i^1, true) > 0
                            || (neighbors[i^1] == null && LayoutUtils.alignedIntervals(parent, otherPar, i^1));
                    } else if (!outOfGroup[i^1] && !expanded[i]
                            && (LayoutInterval.getCount(parent, i^1, true) > 0
                                || (LayoutInterval.getCount(parent, LayoutRegion.ALL_POINTS, true) > 0
                                    && !LayoutInterval.contentWantResize(parent)))) {
                        minorGap = true;
                    }
                    if (outerNeighbor == null) {
                        if (otherPar != null && (!otherPar.isParallel() || parent.isParentOf(otherPar))) {
                            // aligned directly with some component or group as a whole (not from inside)
                            noMinPadding = !endsWithNonZeroGap(otherPar, i, parent);
                        } else {
                            boolean wantMinPadding = (otherPar == null)
                                    && (seq.getSubIntervalCount() == 0 || endsWithNonZeroGap(seq, i^1, null));
                                      // [or just neighbors[i^1] != null?]
                            noMinPadding = followEndingPaddingFromNeighbors(
                                        (otherPar != null ? otherPar : parent), seq, i, wantMinPadding)
                                    ^ wantMinPadding;
                        }
                    }
                } else if (minorOriginalGap) { // there is already an unaligned fixed ending gap
                    minorGap = true;
                }
            }

            boolean fixedGap = aligned;

            if (!aligned) {
                if ((minorGap && !LayoutInterval.wantResize(parent)) || LayoutInterval.wantResize(addingInterval)) {
                    fixedGap = true;
                } else if (originalGap != null && LayoutInterval.canResize(originalGap)) {
                    // i.e. fixedGap kept false
                    if (originalGap.getMinimumSize() == 0 && neighbors[i] == null) {
                        noMinPadding = true;
                    }
                } else if (originalGap != null && !minorOriginalGap) {
                    if (!span[i^1]
                        || (neighbors[i] == null && !LayoutInterval.canResize(originalGap))
                        || (neighbors[i] != null
                            && (LayoutInterval.getEffectiveAlignment(neighbors[i], i^1, true) == (i^1)
                                || !tiedToParallelSnap(neighbors[i], i, otherPar)))) {
                        fixedGap = true;
                    }
                } else if (LayoutInterval.wantResize(seq)) {
                    fixedGap = true;
                } else if (neighbors[i] != null) {
                    if (LayoutInterval.getEffectiveAlignment(neighbors[i], i^1, true) == (i^1)) {
                        fixedGap = true;
                    }
                } else if (otherPar != null) {
                    if (parent.isParentOf(otherPar)) {
                        if (LayoutInterval.getEffectiveAlignmentInParent(otherPar, parent, i^1) == i) {
                            fixedGap = true;
                        }
                    } else {
                        LayoutInterval p = LayoutInterval.getCommonParent(otherPar, parent);
                        if (p == parent) {
                            p = p.getParent();
                        }
                        if (p != null && LayoutInterval.getEffectiveAlignmentInParent(parent, p, i) == (i^1)) {
                            fixedGap = true;
                        }
                    }
                } else if (!span[i^1]) {
                    LayoutInterval alignParent = LayoutInterval.getCommonParent(outBounds[LEADING], outBounds[TRAILING]);
                    int[] effa = new int[2];
                    for (int e=LEADING; e <= TRAILING; e++) {
                        LayoutInterval b = outBounds[e];
                        if (b == alignParent) {
                            effa[e] = e;
                        } else {
                            int edge = (b == parent || b.isParentOf(parent)) ? e : e^1; // parent or neighbor
                            effa[e] = LayoutInterval.getEffectiveAlignmentInParent(b, alignParent, edge);
                        }
                    }
                    if (effa[LEADING] == effa[TRAILING]) {
                        fixedGap = true;
                    }
                }
            }
            if (!aligned && (!fixedGap || minorGap)
                    && neighbors[i] == null && originalGap == null
                    && seq.getSubIntervalCount() == 0 && seq.getRawAlignment() == DEFAULT
                    && (otherPar == null || !parent.isParentOf(otherPar))) {
                // this new sequence should be aligned to opposite edge
                layoutModel.setIntervalAlignment(seq, i^1);
            }
            if (fixedGap && noMinPadding) { // minorPadding
                continue;
            }

            LayoutInterval gap = new LayoutInterval(SINGLE);
            if (!minorGap || !fixedGap) {
                if (iiDesc == null || iiDesc.snappedNextTo == null) {
                    // the gap possibly needs an explicit size
                    int distance;
                    LayoutRegion space = iiDesc != null && iiDesc.snappedParallel != null ?
                                         iiDesc.snappedParallel.getCurrentSpace() : addingSpace;
                    distance = neighbors[i] != null ?
                        LayoutRegion.distance(neighbors[i].getCurrentSpace(), space, dimension, i^1, i) :
                        LayoutRegion.distance(parent.getCurrentSpace(), space, dimension, i, i);
                    if (i == TRAILING) {
                        distance *= -1;
                    }

                    if (distance > 0) {
                        int pad;
                        if (neighbors[i] != null || outerNeighbor == null) {
                            int[] pads = dragger.findPaddings(neighbors[i], addingInterval, PaddingType.RELATED, dimension, i);
                            pad = (pads != null && pads.length > 0) ? pads[0] : 0;
                        } else {
                            pad = Short.MIN_VALUE; // has no neighbor, but is not related to container border
                        }
                        if (distance > pad || (fixedGap && distance != pad)) {
                            gap.setPreferredSize(distance);
                            if (fixedGap) {
                                gap.setMinimumSize(USE_PREFERRED_SIZE);
                                gap.setMaximumSize(USE_PREFERRED_SIZE);
                            }
                        }
                    } else if (noMinPadding) {
                        gap.setPreferredSize(0);
                    }
                    if (fixedGap && gap.getPreferredSize() == NOT_EXPLICITLY_DEFINED
                            && neighbors[i] != null
                            && !LayoutUtils.isDefaultGapValidForNeighbor(neighbors[i], i^1)) {
                        // likely a parallel neighbor group ending with gaps (could be created by mergeParallelInclusions)
                        continue;
                    }
                } else {
                    gap.setPaddingType(iiDesc.paddingType);
                }
            }
            if (!fixedGap) {
                if (noMinPadding) {
                    gap.setMinimumSize(0);
                }
                gap.setMaximumSize(Short.MAX_VALUE);
            }
            gap.setAttribute(LayoutInterval.ATTR_FLEX_SIZEDEF);

            gaps[i] = gap;

            // if anchored towards open parent group edge, we may want to move the
            // sequence out to place it independently on the rest of the group
            if (definite && (otherDesc == null || otherDesc.alignment < 0)) {
                if (!subseq && parent.getParent() != null) {
                    // a) adding into main sequence directly
                    if (!fixedGap && neighbors[i] != null
                            && outBounds[i^1] != parent
                            && !parent.isParentOf(outBounds[i^1])) {
                        // should not close open group by making a resizing sequence
                        parent = separateSequence(seq, i^1, iDesc1, iDesc2);
                        checkRoot(parent, root, iDesc1.parent, dimension);
                        if (i == TRAILING) {
                            edges++; // we need to revisit the LEADING gap
                        }
                    }
                } else if (subseq && iDesc1.parent.getParent().getParent() != null) {
                    // b) adding into sub-sequence (in parallel with part of main
                    // sequence), iDesc1.parent is the main sequence
                    if (!fixedGap
                            && LayoutInterval.getDirectNeighbor(parent, i, true) != null
                            && outBounds[i^1] != iDesc1.parent.getParent()
                            && !iDesc1.parent.getParent().isParentOf(outBounds[i^1])) {
                        // should not close open group by making a resizing sequence
                        LayoutInterval p = separateSequence(iDesc1.parent, i^1, iDesc1, iDesc2);
                        if (parent.getSubIntervalCount() == 0 && parent.getParent() == null) {
                            parent = p; // optimized out during the operation
                            checkRoot(parent, root, iDesc1.parent, dimension);
                        } else {
                            checkRoot(parent, root, iDesc1.parent, dimension);
                            setCurrentPositionToParent(parent, p, dimension, i^1);
                        }
                        if (i == TRAILING) {
                            edges++; // we need to revisit the LEADING gap
                        }
                    }
                }
            }
        }

        // try to determine actual positions of the sequence ends
        for (int i = LEADING; i <= TRAILING; i++) {
            if (neighbors[i] == null && !seq.getCurrentSpace().isSet(dimension, i)) {
                int pos = LayoutRegion.UNKNOWN;
                IncludeDesc iiDesc = iDesc1.alignment < 0 || iDesc1.alignment == i ? iDesc1 : iDesc2;
                if (iiDesc != null && iiDesc.snappedParallel != null) {
                    pos = iiDesc.snappedParallel.getCurrentSpace().positions[dimension][i];
                } else if (iiDesc != null && iiDesc.snappedNextTo != null) {
                    pos = parent.getCurrentSpace().positions[dimension][i];
                } else if (gaps[i] == null || gaps[i].getPreferredSize() == 0) {
                    pos = addingInterval.getCurrentSpace().positions[dimension][i];
                }
                if (pos != LayoutRegion.UNKNOWN) {
                    seq.getCurrentSpace().setPos(dimension, i, pos);
                }
            }
        }

        if (seq.getParent() == null) { // newly created sequence
            assert seq.getSubIntervalCount() == 0;
            checkRoot(parent, root, iDesc1.parent, dimension);
            if (gaps[LEADING] == null && gaps[TRAILING] == null) { // after all, the sequence is not needed
                layoutModel.setIntervalAlignment(addingInterval, seq.getAlignment());
                layoutModel.addInterval(addingInterval, parent, -1);
                return;
            } else {
                layoutModel.addInterval(seq, parent, -1);
            }
        }

        // aligning in parallel with interval in the same sequence was resolved
        // by substituting its position
        if (iDesc1.snappedParallel != null && seq.isParentOf(iDesc1.snappedParallel)) {
            iDesc1.snappedParallel = null; // set to null not to try alignInParallel later
        }

        // finally add the surrounding gaps and the interval
        if (originalGap != null) {
            index = layoutModel.removeInterval(originalGap);
        }
        else if (neighbors[TRAILING] != null) {
            index = seq.indexOf(neighbors[TRAILING]);
        }
        else if (neighbors[LEADING] != null) {
            index = seq.getSubIntervalCount();
        }
        else index = 0;

        if (gaps[LEADING] != null) {
            layoutModel.addInterval(gaps[LEADING], seq, index++);
        }
        layoutModel.setIntervalAlignment(addingInterval, DEFAULT);

        if (definite) {
            index += operations.addContent(addingInterval, seq, index);
        } else { // avoid optimizations
            assert !addingInterval.isSequential();
            layoutModel.addInterval(addingInterval, seq, index++);
        }
        if (gaps[TRAILING] != null) {
            layoutModel.addInterval(gaps[TRAILING], seq, index);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(LayoutFeeder.class.getName());

    private static void checkRoot(LayoutInterval interval, LayoutInterval expectedRoot, LayoutInterval originalParent, int dim) {
        if (interval != null) {
            LayoutInterval root = LayoutInterval.getRoot(interval);
            if (root != expectedRoot) {
                LOGGER.log(Level.WARNING, "Found interval misplaced from its expected root");
                LOGGER.log(Level.WARNING, LayoutPersistenceManager.dumpInterval(null, interval, dim, 2));
                LOGGER.log(Level.WARNING, LayoutPersistenceManager.dumpInterval(null, root, dim, 2));
                LOGGER.log(Level.WARNING, LayoutPersistenceManager.dumpInterval(null, expectedRoot, dim, 2));
                if (originalParent != null) {
                    LOGGER.log(Level.WARNING, LayoutPersistenceManager.dumpInterval(null, originalParent, dim, 2));
                }
                String removeStackTrace = LayoutInterval.getRemoveStacktrace(root);
                if (removeStackTrace != null) {
                    LOGGER.log(Level.WARNING, removeStackTrace);
                } else {
                    LOGGER.log(Level.WARNING, "no remove stacktrace"); // NOI18N
                }
                throw new IllegalStateException("Interval lost from root, please report this exception. Related to bug 222703."); // NOI18N
            }
        }
    }

    private static void checkNeighbor(LayoutInterval neighbor, LayoutInterval expectedParent, int dim) {
        if (neighbor.getParent() != expectedParent) {
            LOGGER.log(Level.WARNING, "Wrong neighbor position, please report this. Related to bug 217611."); // NOI18N
            LOGGER.log(Level.WARNING, LayoutPersistenceManager.dumpInterval(null, neighbor, dim, 2));
            LOGGER.log(Level.WARNING, LayoutPersistenceManager.dumpInterval(null, expectedParent, dim, 2));
            LOGGER.log(Level.WARNING, LayoutPersistenceManager.dumpInterval(null, LayoutInterval.getRoot(expectedParent), dim, 2));
            assert false;
        }
    }

    private int getExtractCloseAlign(IncludeDesc iDesc) {
        // aligned within the same sequence and without indent?
        return iDesc != null && iDesc.snappedParallel != null
               && iDesc.parent.isParentOf(iDesc.snappedParallel)
               && LayoutRegion.distance(addingSpace, iDesc.snappedParallel.getCurrentSpace(),
                                        dimension, iDesc.alignment, iDesc.alignment) == 0
       ? iDesc.alignment : -1;
    }

    private LayoutInterval extractParallelSequence(LayoutInterval seq,
                                                   LayoutRegion space,
                                                   int closeAlign1,
                                                   int closeAlign2,
                                                   int refPoint,
                                                   int[] visualBoundary)
    {
        int count = seq.getSubIntervalCount();
        int startIndex = 0;
        int endIndex = count - 1;
        int startPos = seq.getCurrentSpace().positions[dimension][LEADING];
        int endPos = seq.getCurrentSpace().positions[dimension][TRAILING];
        int startPosBoundary = visualBoundary != null ? visualBoundary[LEADING] : LayoutRegion.UNKNOWN;
        int endPosBoundary = visualBoundary != null ? visualBoundary[TRAILING] : LayoutRegion.UNKNOWN;
        boolean closeStart = closeAlign1 == LEADING || closeAlign2 == LEADING;
        boolean closeEnd = closeAlign1 == TRAILING || closeAlign2 == TRAILING;
        if (refPoint < 0) {
            refPoint = CENTER;
        }

        for (int i=0; i < count; i++) {
            LayoutInterval li = seq.getSubInterval(i);
            if (li.isEmptySpace())
                continue;

            LayoutRegion subSpace = li.getCurrentSpace();
            boolean forcedParallel = !solveOverlap && LayoutUtils.contentOverlap(space, li, dimension);
            if (!forcedParallel && LayoutUtils.contentOverlap(space, li, dimension^1)) { // orthogonal overlap
                // this interval makes a hard boundary
                if (getAddDirection(space, subSpace, dimension, refPoint) == LEADING) {
                    // given interval is positioned before this subinterval (trailing boundary)
                    endIndex = i - 1;
                    endPos = subSpace.positions[dimension][LEADING];
                    break;
                }
                else { // given interval points behind this one (leading boundary)
                    startIndex = i + 1;
                    startPos = subSpace.positions[dimension][TRAILING];
                }
            } else if (startPosBoundary != LayoutRegion.UNKNOWN && subSpace.positions[dimension][LEADING] < startPosBoundary) {
                // this interval is positioned before allowed visual boundary
                startIndex = i + 1;
                startPos = subSpace.positions[dimension][TRAILING];
            } else if (endPosBoundary != LayoutRegion.UNKNOWN && subSpace.positions[dimension][TRAILING] > endPosBoundary) {
                // this interval is positioned after allowed visual boundary
                endIndex = i - 1;
                endPos = subSpace.positions[dimension][LEADING];
                break;
            } else if (closeStart || closeEnd) { // go for smallest parallel part possible
                int[] detPos = space.positions[dimension];
                int[] subPos = subSpace.positions[dimension];
                if (closeStart) {
                    if (detPos[LEADING] >= subPos[TRAILING]) {
                        startIndex = i + 1;
                        startPos = subPos[TRAILING];
                    } else if (detPos[LEADING] >= subPos[LEADING]) {
                        startIndex = i;
                        startPos = subPos[LEADING];
                    }
                }
                if (closeEnd && detPos[TRAILING] <= subPos[TRAILING]) {
                    if (detPos[TRAILING] > subPos[LEADING]) {
                        endIndex = i;
                        endPos = subPos[TRAILING];
                        break;
                    } else { // detPos[TRAILING] <= subPos[LEADING]
                        endIndex = i - 1;
                        endPos = subPos[LEADING];
                        break;
                    }
                }
            }
        }

        if (startIndex > endIndex
                || (startIndex == endIndex && seq.getSubInterval(startIndex).isEmptySpace())) {
            return null; // no useful part of the sequence can be parallel to the given space
        }
        if (startIndex == 0 && endIndex == count-1) { // whole sequence is parallel
            return seq.getParent();
        }
        if (startIndex == endIndex) {
            LayoutInterval li = seq.getSubInterval(startIndex);
            if (li.isParallel()) {
                return li;
            }
        }

        if (seq.getParent() != null) {
            if (!LayoutRegion.isValidCoordinate(startPos) && startIndex == 0) {
                startPos = seq.getParent().getCurrentSpace().positions[dimension][LEADING];
            }
            if (!LayoutRegion.isValidCoordinate(endPos) && endIndex == count-1) {
                endPos = seq.getParent().getCurrentSpace().positions[dimension][TRAILING];
            }
        }

        LayoutInterval group = new LayoutInterval(PARALLEL);
        if (startIndex == 0 && LayoutInterval.getEffectiveAlignmentInParent(
                seq.getSubInterval(0), seq.getParent(), LEADING) == TRAILING) {
            group.setGroupAlignment(TRAILING);
        } // otherwise leave as LEADING
        if (startIndex == endIndex) {
            LayoutInterval li = layoutModel.removeInterval(seq, startIndex);
            layoutModel.addInterval(li, group, 0);
        }
        else {
            LayoutInterval interSeq = new LayoutInterval(SEQUENTIAL);
            group.add(interSeq, 0);
            int i = startIndex;
            while (i <= endIndex) {
                LayoutInterval li = layoutModel.removeInterval(seq, i);
                endIndex--;
                layoutModel.addInterval(li, interSeq, -1);
            }
        }
        layoutModel.addInterval(group, seq, startIndex);

        group.getCurrentSpace().set(dimension, startPos, endPos);

        return group;
    }

    private void alignWithResizingInSubgroup(LayoutInterval seqWithResizing, LayoutInterval group, IncludeDesc iDesc) {
        if (LayoutInterval.wantResize(addingInterval) || !iDesc.snapped()
                || !group.isParallel() || group.getSubIntervalCount() > 2) {
            return;
        }
        LayoutInterval toAlign = group.getSubInterval(0);
        if (seqWithResizing.getParent() == null) {
            if (group.getSubIntervalCount() != 1) {
                return;
            }
        } else { // existing sequence already in group
            if (group.getSubIntervalCount() != 2) {
                return;
            }
            if (toAlign == seqWithResizing) {
                toAlign = group.getSubInterval(1);
            }
        }
        int alignment = iDesc.alignment; // the resizing edge
        if (LayoutUtils.anythingAtGroupEdge(toAlign, null, dimension, alignment)
                && !LayoutUtils.anythingAtGroupEdge(toAlign, null, dimension, alignment^1)) {
            layoutModel.setGroupAlignment(group, alignment);
            if (toAlign.getAlignment() != alignment) {
                layoutModel.setIntervalAlignment(toAlign, DEFAULT);
            }
            if (seqWithResizing.getParent() == group && seqWithResizing.getAlignment() != alignment) {
                layoutModel.setIntervalAlignment(seqWithResizing, DEFAULT);
            }
        }
    }

    private static void setCurrentPositionToParent(LayoutInterval interval, LayoutInterval parent, int dimension, int alignment) {
        if (!parent.isParentOf(interval)) {
            return;
        }
        int parentPos = parent.getCurrentSpace().positions[dimension][alignment];
        if (!LayoutRegion.isValidCoordinate(parentPos)) {
            return;
        }
        for (LayoutInterval li = interval; li != parent; li = li.getParent()) {
            li.getCurrentSpace().setPos(dimension, alignment, parentPos);
        }
    }

    private static LayoutInterval getPerceivedParentNeighbor(LayoutInterval parent,
                                    LayoutRegion space, boolean outOfGroup, int dimension, int alignment) {
        assert parent.isParallel();
        LayoutInterval interval = null;
        LayoutInterval neighbor = null;
        boolean done = false;
        do {
            neighbor = null;
            while (neighbor == null && parent.getParent() != null) {
                if (isSignificantGroupEdge(parent, alignment, outOfGroup)
                        /*|| (!outOfGroup && dimension == VERTICAL)*/) {
                    break;
                }

                neighbor = LayoutInterval.getDirectNeighbor(parent, alignment, true);
                if (neighbor == null) {
                    interval = parent;
                    parent = interval.getParent();
                    if (parent.isSequential()) {
                        interval = parent;
                        parent = interval.getParent();
                    }
                }
            }

            if (neighbor == null) {
                done = true; // use the parent
            } else { // look for neighbor of the parent that has orthogonal overlap with the given space
                do {
                    if (LayoutUtils.contentOverlap(space, neighbor, dimension^1)) {
                        done = true;
                    } else { // the space can "go through" this neighbor
                        neighbor = LayoutInterval.getDirectNeighbor(neighbor, alignment, true);
                    }
                } while (!done && neighbor != null);
                if (neighbor == null) {
                    interval = parent;
                    parent = interval.getParent();
                    if (parent.isSequential()) {
                        interval = parent;
                        parent = interval.getParent();
                    }
                }
            }
        } while (!done);
        return neighbor != null ? neighbor : parent;
    }

    private static boolean shouldExpandOverGroupEdge(LayoutInterval group, LayoutInterval outBound, int alignment) {
        if (group != outBound && !group.isParentOf(outBound)
                && !isSignificantGroupEdge(group, alignment, true)) {
            boolean open = !LayoutInterval.isClosedGroup(group, alignment);
            LayoutInterval li = group;
            boolean gapNotWorthExpanding = false;
            while ((li = LayoutInterval.getNeighbor(li, alignment, false, true, false)) != null) {
                if (li == outBound || li.isParentOf(outBound)
                        || LayoutInterval.getDirectNeighbor(li, alignment^1, false) == outBound) {
                    return false; // reached outBound, found nothing bigger than a gap
                }
                if (gapNotWorthExpanding) { // now already more than a gap
                    return true;
                }
                if (li.isEmptySpace()
                    && ((li.getPreferredSize() == NOT_EXPLICITLY_DEFINED && li.getDiffToDefaultSize() == 0 && LayoutInterval.canResize(group))
                        || (open && LayoutInterval.canResize(li)))) {
                    gapNotWorthExpanding = true;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isSignificantGroupEdge(LayoutInterval group, int alignment, boolean placedOver) {
        assert group.isParallel();
        return LayoutInterval.isExplicitlyClosedGroup(group)
               || (!placedOver && LayoutUtils.edgeSubComponents(group, alignment, true).size() > 1);
    }

    private static int getPerceivedNeighborPosition(LayoutInterval firstNeighbor, LayoutRegion space, int dimension, int alignment) {
        LayoutInterval neighbor = firstNeighbor;
        do {
            int pos = getPerceivedNeighborPosition0(neighbor, space, dimension, alignment);
            if (pos != Integer.MIN_VALUE) {
                return pos;
            }
            neighbor = LayoutInterval.getDirectNeighbor(neighbor, alignment, true);
        } while (neighbor != null);
        return firstNeighbor.getCurrentSpace().positions[dimension][alignment^1];
    }

    private static int getPerceivedNeighborPosition0(LayoutInterval neighbor, LayoutRegion space, int dimension, int alignment) {
        assert !neighbor.isEmptySpace();

        int neighborPos = Integer.MIN_VALUE;
        if (neighbor.isComponent()) {
            if (LayoutRegion.overlap(space, neighbor.getCurrentSpace(), dimension^1, 0)) {
                neighborPos = neighbor.getCurrentSpace().positions[dimension][alignment^1];
            }
        } else {
            int n = neighbor.getSubIntervalCount();
            int i, d;
            if (neighbor.isParallel() || alignment == TRAILING) {
                d = 1;
                i = 0;
            } else {
                d = -1;
                i = n - 1;
            }
            while (i >=0 && i < n) {
                LayoutInterval sub = neighbor.getSubInterval(i);
                i += d;
                if (sub.isEmptySpace()) {
                    continue;
                }

                int pos = getPerceivedNeighborPosition0(sub, space, dimension, alignment);
                if (pos != Integer.MIN_VALUE) {
                    if (neighbor.isSequential()) {
                        neighborPos = pos;
                        break;
                    } else if (neighborPos == Integer.MIN_VALUE || pos*d < neighborPos*d) {
                        neighborPos = pos;
                        // continue, there can still be a closer position
                    }
                }
            }
        }
        return neighborPos;
    }

    private boolean endsWithNonZeroGap(LayoutInterval interval, int edge, LayoutInterval inParent) {
        assert edge == LEADING || edge == TRAILING;
        if (interval.isParallel() && inParent == null) {
            for (Iterator<LayoutInterval> it=interval.getSubIntervals(); it.hasNext(); ) {
                LayoutInterval li = it.next();
                if (endsWithNonZeroGap(li, edge, null)) {
                    return true;
                }
            }
            return false;
        }
        if (inParent != null) {
            LayoutInterval firstSeq = interval; // just for the case inParent is not parent of interval
            LayoutInterval parent = interval.getParent();
            while (parent != null && parent != inParent) {
                if (parent.isSequential()) {
                    if (!firstSeq.isSequential()) {
                        firstSeq = parent;
                    }
                    interval = parent;
                }
                parent = parent.getParent();
            }
            if (parent == null) {
                interval = firstSeq;
            }
        }
        if (interval.isSequential() && interval.getSubIntervalCount() > 0) {
            int idx = (edge == LEADING) ? 0 : interval.getSubIntervalCount()-1;
            LayoutInterval li = interval.getSubInterval(idx);
            return li.isEmptySpace() && li.getMinimumSize() != 0;
        }
        return false;
    }

    private boolean followEndingPaddingFromNeighbors(LayoutInterval parent, LayoutInterval interval, int edge, boolean wantMinPadding) {
        boolean someAligned = false;
        for (Iterator<LayoutInterval> it=parent.getSubIntervals(); it.hasNext(); ) {
            LayoutInterval li = it.next();
            if (li != interval && !li.isParentOf(interval)
                    && wantMinPadding == endsWithNonZeroGap(li, edge^1, null)) {
                someAligned = true;
                if (wantMinPadding == endsWithNonZeroGap(li, edge, null)) {
                    return true; // there's an aligned neighbor with expected ending spaces
                }
            }
        }
        return !someAligned;
    }

    private LayoutInterval separateSequence(LayoutInterval seq, int alignment, IncludeDesc iDesc1, IncludeDesc iDesc2) {
        // if IncludeDesc.snappedParallel is a valid group, check if it does not get eliminated by the parallelization
        if (iDesc1 == null || iDesc1.snappedParallel == null || iDesc1.snappedParallel.getSubIntervalCount() == 0) {
            iDesc1 = null;
        }
        if (iDesc2 == null || iDesc2.snappedParallel == null || iDesc2.snappedParallel.getSubIntervalCount() == 0) {
            iDesc2 = null;
        }

        // [TODO repeatedly up to given parent]
        LayoutInterval parentPar = seq.getParent();
        assert parentPar.isParallel();
        while (!parentPar.getParent().isSequential()) {
            parentPar = parentPar.getParent();
        }
        LayoutInterval parentSeq = parentPar.getParent(); // sequential

        int d = alignment == LEADING ? -1 : 1;
        int n = parentSeq.getSubIntervalCount();
        int end = parentSeq.indexOf(parentPar) + d;
        while (end >= 0 && end < n) {
            LayoutInterval sub = parentSeq.getSubInterval(end);
            if (!sub.isEmptySpace()) {
                if (LayoutUtils.contentOverlap(addingSpace, sub, dimension^1)) {
                    break;
                }
            }
            end += d;
        }

        int endPos = end >= 0 && end < n ?
                     parentSeq.getSubInterval(end).getCurrentSpace().positions[dimension][alignment^1] :
                     parentSeq.getParent().getCurrentSpace().positions[dimension][alignment];
        end -= d;
        operations.parallelizeWithParentSequence(seq, end, dimension);
        parentPar = seq.getParent();
        parentPar.getCurrentSpace().positions[dimension][alignment] = endPos;
        if (iDesc1 != null && iDesc1.snappedParallel.getSubIntervalCount() == 0) {
            iDesc1.snappedParallel = parentPar;
        }
        if (iDesc2 != null && iDesc2.snappedParallel.getSubIntervalCount() == 0) {
            iDesc2.snappedParallel = parentPar;
        }
        return parentPar;
    }

    /**
     * When an interval is added or resized out of current boundaries of its
     * parent, this method tries to accommodate the size increment in the parent
     * (and its parents). It acts according to the current visual position of
     * the interval - how it exceeds the current parent border. In the simplest
     * form the method tries to shorten the nearest gap in the parent sequence.
     */
    private void accommodateOutPosition(List<LayoutInterval> added) {
        LayoutInterval interval = added.get(0);
        LayoutInterval parent = interval.getParent();
        int alignment = DEFAULT;
        if (parent.isSequential()) {
            int align = parent.getAlignment();
            if (align == LEADING || align == TRAILING) {
                for (int i=0; i < 2; i++) {
                    align ^= 1;
                    LayoutInterval li = align == LEADING ? interval : added.get(added.size()-1);
                    if (LayoutInterval.getDirectNeighbor(li, align, true) == null) {
                        alignment = align;
                        interval = li;
                        break;
                    }
                }
            }
        } else {
            int align = interval.getAlignment();
            if (align == LEADING || align == TRAILING) {
                alignment = align ^ 1;
                int maxPos = LayoutRegion.UNKNOWN;
                for (LayoutInterval li : added) {
                    int pos = li.getCurrentSpace().positions[dimension][alignment];
                    if (maxPos == LayoutRegion.UNKNOWN
                            || (alignment == LEADING && pos < maxPos)
                            || (alignment == TRAILING && pos > maxPos)) {
                        maxPos = pos;
                        interval = li;
                    }
                }
            }
        }
        if (alignment == DEFAULT) {
            return;
        }

        int pos = interval.getCurrentSpace().positions[dimension][alignment];
        assert pos != LayoutRegion.UNKNOWN;
        int sizeIncrement = Integer.MIN_VALUE;
        int d = alignment == LEADING ? -1 : 1;
        int[] groupPos = null;
        LayoutInterval prev = null;

        do {
            if (parent.isSequential()) {
                if (sizeIncrement > 0) {
                    int accommodated = accommodateSizeInSequence(interval, prev, sizeIncrement, alignment);
                    sizeIncrement -= accommodated;
                }
                LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(interval, alignment, false);
                if (neighbor != null && (!neighbor.isEmptySpace())) {
                    // not a border interval in the sequence, can't go up
                    return;
                }
                if (prev == null) {
                    prev = interval;
                } else if (parent.isParentOf(prev)) {
                    while (prev.getParent() != parent) {
                        prev = prev.getParent();
                    }
                } else { // moved out - parallel with whole sequence
                    parent = parent.getParent();
                }
            } else {
                groupPos = parent.getCurrentSpace().positions[dimension];
                if (groupPos[alignment] != LayoutRegion.UNKNOWN) {
                    if (interval.isParallel() && prev != null
                        && interval.getCurrentSpace().positions[dimension][alignment] != groupPos[alignment]
                        && groupGrowingVisibly(interval, prev, alignment)) {
                        // par. group in par. group that is bigger, move the sticking out interval up
                        if (prev.getParent().isSequential()) {
                            prev = prev.getParent();
                        }
                        int align = prev.getAlignment();
                        layoutModel.removeInterval(prev);
                        layoutModel.addInterval(prev, parent, -1);
                        if (prev.getAlignment() != align) {
                            layoutModel.setIntervalAlignment(prev, align);
                        }
                    }
                    sizeIncrement = (pos - groupPos[alignment]) * d;
                    if (sizeIncrement > 0) {
                        int subPos[] = interval.getCurrentSpace().positions[dimension];
                        if (!interval.getCurrentSpace().isSet(dimension)
                                || subPos[alignment]*d < pos*d) {
                            subPos[alignment] = pos;
                        }
                    }
                } else {
                    groupPos = null;
                }
                if (!interval.isSequential() || prev == null) {
                    prev = interval;
                }
            }
            interval = parent;
            parent = interval.getParent();
        }
        while ((sizeIncrement > 0 || sizeIncrement == Integer.MIN_VALUE)
               && parent != null
               && (!parent.isParallel() || LayoutInterval.isAlignedAtBorder(interval, parent, alignment^1)/*interval.getAlignment() != alignment*/));
               // can't accommodate at the aligned side [but could probably turn to other side - update 'pos', etc]
    }

    private int accommodateSizeInSequence(LayoutInterval interval, LayoutInterval lower, int sizeIncrement, int alignment) {
        LayoutInterval parent = interval.getParent();
        assert parent.isSequential();
        int increment = sizeIncrement;
        int idx = parent.indexOf(interval) + (alignment == LEADING ? -1:1);
        LayoutInterval neighbor = null;
        if (idx >= 0 && idx < parent.getSubIntervalCount()) {
            neighbor = parent.getSubInterval(idx);
            if (neighbor.isEmptySpace()) {
                if (neighbor.getPreferredSize() != NOT_EXPLICITLY_DEFINED || neighbor.getMaximumSize() == Short.MAX_VALUE) {
                    int pad = determinePadding(interval, neighbor.getPaddingType(), dimension, alignment);
                    int currentSize = LayoutInterval.getCurrentSize(neighbor, dimension);
                    int size = currentSize - increment;
                    if (size <= pad) {
                        size = NOT_EXPLICITLY_DEFINED;
                        increment -= currentSize - pad;
                    } else {
                        increment = 0;
                    }
                    operations.resizeInterval(neighbor, size);
                    if (LayoutInterval.wantResize(neighbor) && LayoutInterval.wantResize(interval)) {
                        // cancel gap resizing if the neighbor is also resizing
                        // [though sometimes the superflous resizing gap might have already existed before, which we don't know]
                        int min = neighbor.getPreferredSize() == NOT_EXPLICITLY_DEFINED && neighbor.getMinimumSize() == NOT_EXPLICITLY_DEFINED
                                ? NOT_EXPLICITLY_DEFINED : USE_PREFERRED_SIZE;
                        layoutModel.setIntervalSize(neighbor, min, neighbor.getPreferredSize(), USE_PREFERRED_SIZE);
                    }
                }
            } else if (neighbor.isParallel()) { // parallel group may have border gaps to reduce
                LayoutInterval comp = LayoutUtils.getOutermostComponent(neighbor, dimension, alignment^1);
                int extPos = lower.getCurrentSpace().positions[dimension][alignment];
                int pos1 = neighbor.getCurrentSpace().positions[dimension][alignment^1];
                int pos2 = comp.getCurrentSpace().positions[dimension][alignment^1];
                // need to reduce gaps to reach extPos, but can't go beyond pos2
                if (pos2 != pos1 && ((alignment == TRAILING && extPos > pos1)
                                  || (alignment == LEADING && extPos < pos1))) {
                    if ((alignment == TRAILING && pos2 > extPos) || (alignment == LEADING && pos2 < extPos)) {
                        pos2 = extPos;
                    }
                    for (LayoutInterval gap : LayoutUtils.getSideGaps(neighbor, alignment^1, false)) {
                        int gapSize = gap.getPreferredSize();
                        if (gapSize > 0) {
                            int gapPos2 = LayoutUtils.getVisualPosition(gap, dimension, alignment);
                            int adjustedSize = alignment == TRAILING ? (gapPos2 - pos2) : (pos2 - gapPos2);
                            if (adjustedSize >=0 && adjustedSize < gapSize) {
                                operations.resizeInterval(gap, adjustedSize);
                            }
                        }
                    }
                    increment -= Math.abs(pos2 - pos1);
                    neighbor.getCurrentSpace().setPos(dimension, alignment^1, pos2);
                }
            }
        }

        // Intervals aligned at group edge could move due to group growing with
        // the newly added interval, so may want to add support gaps to compensate.
        // This is suitable if there is some visible empty space next to the group
        // (e.g. ALT_Bug129494_1Test), but not if the group is visibly snapped to
        // something (ALT_SizeDefinition02Test) or having accommodating content.
        if (LayoutInterval.canResize(interval)
            && (sizeIncrement != increment
                || (neighbor != null && neighbor.isEmptySpace()
                    && (neighbor.getPreferredSize() != NOT_EXPLICITLY_DEFINED
                        || (LayoutInterval.canResize(neighbor) && neighbor.getDiffToDefaultSize() != 0))))) {
            int outPos = lower.getCurrentSpace().positions[dimension][alignment];
            LayoutInterval inPar = lower.getParent().isSequential() ? lower.getParent() : lower;
            boolean supportGapAdded = false;
            for (int i=0; i < interval.getSubIntervalCount(); i++) {
                LayoutInterval sibling = interval.getSubInterval(i);
                if (sibling != inPar && LayoutInterval.isAlignedAtBorder(sibling, alignment)) {
                    for (LayoutInterval sub : LayoutUtils.getSideSubIntervals(sibling, alignment, true, true, true, true)) {
                        if (!LayoutInterval.wantResize(sub)) {
                            // have some fixed component or gap at aligned edge
                            LayoutInterval supportGap = new LayoutInterval(SINGLE);
                            supportGap.setSizes(USE_PREFERRED_SIZE, sizeIncrement, USE_PREFERRED_SIZE);
                            operations.insertGap(supportGap, sibling, outPos, dimension, alignment);
                            supportGapAdded = true;
                            break;
                        }
                    }
                }
            }
            if (supportGapAdded && inPar.isSequential()) {
                int i = alignment == LEADING ? 0 : inPar.getSubIntervalCount()-1;
                // zero resizing gap is sometimes created after the added component (here not necessary and may cause problems)
                if (inPar.getSubInterval(i).isEmptySpace()) {
                    layoutModel.removeInterval(inPar, i);
                }
            }
        }

        return sizeIncrement - increment;
    }

    private boolean groupGrowingVisibly(LayoutInterval group, LayoutInterval interval, int edge) {
        // assuming it's already known that 'interval' sticks out of 'group' (added or resized)
        int idx = LayoutInterval.getIndexInParent(interval, group);
        LayoutInterval temp = idx >=0 ? group.remove(idx) : null; // don't consider the sequence of 'interval'
        List<LayoutInterval> l = LayoutUtils.getSideComponents(group, edge, false, true);
        if (temp != null) {
            group.add(temp, idx);
        }
        return !l.isEmpty();
    }

    /**
     * This method aligns an interval (just simply added to the layout - so it
     * is already placed correctly where it should appear) in parallel with
     * another interval.
     * @return parallel group with aligned intervals if some aligning changes happened,
     *         null if addingInterval has already been aligned or could not be aligned
     */
    private LayoutInterval alignInParallel(LayoutInterval interval, LayoutInterval toAlignWith, int alignment) {
        assert alignment == LEADING || alignment == TRAILING;

        if (toAlignWith.isParentOf(interval) // already aligned to parent
                || interval.isParentOf(toAlignWith)) { // can't align with own subinterval
            return null; // contained intervals can't be aligned
        } else {
            LayoutInterval commonParent = LayoutInterval.getCommonParent(interval, toAlignWith);
            if (commonParent == null || commonParent.isSequential()) {
                return null; // can't align unrelated intervals or in the same sequence
            }
        }

        // if not in same parallel group try to substitute interval with parent
        boolean resizing = LayoutInterval.wantResize(interval);
        LayoutInterval aligning = interval; // may be substituted with parent
        LayoutInterval parParent = LayoutInterval.getFirstParent(interval, PARALLEL);
        while (!parParent.isParentOf(toAlignWith)) {
            if (LayoutInterval.isAlignedAtBorder(aligning, parParent, alignment)) { // substitute with parent
                // allow parent resizing if substituting for resizing interval
                if (resizing && !LayoutInterval.canResize(parParent))
                    operations.enableGroupResizing(parParent);
                aligning = parParent;
                parParent = LayoutInterval.getFirstParent(aligning, PARALLEL);
            }
            else parParent = null;
            if (parParent == null) // not parent of toAlignWith
                return null; // can't align with interval from different branch
        }

        // hack: remove aligning interval temporarily not to influence next analysis
        LayoutInterval tempRemoved = aligning;
        while (tempRemoved.getParent() != parParent)
            tempRemoved = tempRemoved.getParent();
        int removedIndex = parParent.remove(tempRemoved);

        // check if we shouldn't rather align with a whole group (parent of toAlignWith)
        boolean alignWithParent = false;
        LayoutInterval alignParent;
        do {
            alignParent = LayoutInterval.getFirstParent(toAlignWith, PARALLEL);
            if (alignParent == null) {
                parParent.add(tempRemoved, removedIndex); // add back temporarily removed
                return null; // aligning with parent (the interval must be already aligned)
            }
            if (canSubstAlignWithParent(toAlignWith, dimension, alignment, dragger.isResizing())) {
                // toAlignWith is at border so we can perhaps use the parent instead
                if (alignParent == parParent) {
                    if (LayoutInterval.getNeighbor(aligning, alignment, false, true, false) == null) {
                        alignWithParent = true;
                    }
                }
                else toAlignWith = alignParent;
            }
        }
        while (toAlignWith == alignParent);

        parParent.add(tempRemoved, removedIndex); // add back temporarily removed

        if (alignParent != parParent)
            return null; // can't align (toAlignWith is too deep)

        if (aligning != interval) {
            if (!LayoutInterval.isAlignedAtBorder(toAlignWith, alignment)) {
                // may have problems with S-layout
                int dst = LayoutRegion.distance(aligning.getCurrentSpace(),
                                                toAlignWith.getCurrentSpace(),
                                                dimension, alignment, alignment)
                          * (alignment == TRAILING ? -1 : 1);
                if (dst > 0) { // try to eliminate effect of avoiding S-layout
                    // need to exclude 'interval' - remove it temporarily
                    tempRemoved = interval;
                    while (tempRemoved.getParent() != aligning)
                        tempRemoved = tempRemoved.getParent();
                    removedIndex = aligning.remove(tempRemoved);

                    operations.cutStartingGap(aligning, dst, dimension, alignment);

                    aligning.add(tempRemoved, removedIndex); // add back temporarily removed
                }
            }
            optimizeStructure = true;
        }

        int effAlign1 = LayoutInterval.getEffectiveAlignment(toAlignWith, alignment, true);

        int indent = LayoutRegion.distance(toAlignWith.getCurrentSpace(), interval.getCurrentSpace(),
                                           dimension, alignment, alignment);
        boolean onPlace = aligning != interval
                && LayoutRegion.distance(aligning.getCurrentSpace(), interval.getCurrentSpace(),
                                         dimension, alignment, alignment) == 0;
        if (indent != 0 && onPlace) {
            // if there's a gap next to indent, its size needs to be reduced (ALT_Indent02Test)
            LayoutInterval indentNeighbor = LayoutInterval.getDirectNeighbor(aligning, alignment, false);
            if (indentNeighbor != null && indentNeighbor.isEmptySpace()
                    && indentNeighbor.getPreferredSize() > 0) {
                int size = indentNeighbor.getPreferredSize() - Math.abs(indent);
                if (size < 0) {
                    size = NOT_EXPLICITLY_DEFINED;
                }
                operations.resizeInterval(indentNeighbor, size);
            }
        }

        // separate content that is out of the emerging group
        List<LayoutInterval> alignedList = new ArrayList<LayoutInterval>(2);
        List<List> remainder = new ArrayList<List>(2);
        int originalCount = parParent.getSubIntervalCount();

        int extAlign1 = extract(toAlignWith, alignedList, remainder, alignment);
        extract(aligning, alignedList, remainder, alignment);

        assert !alignWithParent || remainder.isEmpty();

        // add indent if needed
        if (indent != 0) {
            LayoutInterval indentGap = new LayoutInterval(SINGLE);
            indentGap.setSize(Math.abs(indent));
            // [need to use default padding for indent gap]
            LayoutInterval indented = onPlace ? aligning : interval;
            LayoutInterval parent = indented.getParent();
            if (parent == null || !parent.isSequential()) {
                LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
                if (parent != null) {
                    layoutModel.addInterval(seq, parent, layoutModel.removeInterval(indented));
                }
                layoutModel.setIntervalAlignment(indented, DEFAULT);
                layoutModel.addInterval(indented, seq, 0);
                parent = seq;
            }
            layoutModel.addInterval(indentGap, parent, alignment == LEADING ? 0 : -1);
            if (interval == aligning) {
                alignedList.set(alignedList.size()-1, parent);
            }
        }

        // prepare the group where the aligned intervals will be placed
        LayoutInterval group;
        LayoutInterval commonSeq;
        if (alignWithParent || (originalCount == 2 && parParent.getParent() != null)) {
            // reuse the original group - avoid unnecessary nesting
            group = parParent;
            if (!remainder.isEmpty()) { // need a sequence for the remainder group
                LayoutInterval groupParent = group.getParent();
                if (groupParent.isSequential()) {
                    commonSeq = groupParent;
                }
                else { // insert a new one
                    int index = layoutModel.removeInterval(group);
                    commonSeq = new LayoutInterval(SEQUENTIAL);
                    commonSeq.setAlignment(group.getAlignment());
                    layoutModel.addInterval(commonSeq, groupParent, index);
//                    commonSeq.getCurrentSpace().set(dimension, groupParent.getCurrentSpace());
                    layoutModel.setIntervalAlignment(group, DEFAULT);
                    layoutModel.addInterval(group, commonSeq, -1);
                }
            }
            else commonSeq = null;
        }
        else { // need to create a new group
            group = new LayoutInterval(PARALLEL);
            group.setGroupAlignment(alignment);
            if (!remainder.isEmpty()) { // need a new sequence for the remainder group
                commonSeq = new LayoutInterval(SEQUENTIAL);
                commonSeq.add(group, 0);
                if (effAlign1 == LEADING || effAlign1 == TRAILING) {
                    commonSeq.setAlignment(effAlign1);
                }
                layoutModel.addInterval(commonSeq, parParent, -1);
//                commonSeq.getCurrentSpace().set(dimension, parParent.getCurrentSpace());
            }
            else {
                commonSeq = null;
                if (effAlign1 == LEADING || effAlign1 == TRAILING) {
                    group.setAlignment(effAlign1);
                }
                layoutModel.addInterval(group, parParent, -1);
            }
            if (alignment == LEADING || alignment == TRAILING) {
                int alignPos = toAlignWith.getCurrentSpace().positions[dimension][alignment];
                int outerPos = parParent.getCurrentSpace().positions[dimension][alignment^1];
                group.getCurrentSpace().set(dimension,
                                            alignment == LEADING ? alignPos : outerPos,
                                            alignment == LEADING ? outerPos : alignPos);
            }
        }

        // add the intervals and their separated neighbors to the aligned group
        LayoutInterval aligning2 = alignedList.get(1);
        if (aligning2.getParent() != group) {
            if (aligning2.getParent() != null) {
                layoutModel.removeInterval(aligning2);
            }
            layoutModel.addInterval(aligning2, group, -1);
        }
        if (!LayoutInterval.isAlignedAtBorder(aligning2, alignment)) {
            layoutModel.setIntervalAlignment(aligning2, alignment);
        }

        LayoutInterval aligning1 = alignedList.get(0);
        if (aligning1.getParent() != group) {
            if (aligning1.getParent() != null) {
                layoutModel.removeInterval(aligning1);
            }
            layoutModel.addInterval(aligning1, group, -1);
            if (group == parParent && effAlign1 == alignment
                    && !LayoutInterval.isAlignedAtBorder(aligning1, group, effAlign1)) {
                layoutModel.setIntervalAlignment(aligning1, effAlign1); // not to lose original alignment in reused group
            }
        }

        if ((!dragger.isResizing(dimension) || dragger.getResizingEdge(dimension) != alignment)
                && group.getSubIntervalCount() == 2) {
            if (!LayoutInterval.isAlignedAtBorder(aligning1, alignment)
                    && !LayoutInterval.isAlignedAtBorder(aligning2, alignment^1)) {
                layoutModel.setIntervalAlignment(aligning1, alignment);
            }
            if (LayoutInterval.isAlignedAtBorder(aligning1, group.getGroupAlignment())
                    && LayoutInterval.isAlignedAtBorder(aligning2, group.getGroupAlignment())) {
                layoutModel.setIntervalAlignment(aligning1, DEFAULT);
                layoutModel.setIntervalAlignment(aligning2, DEFAULT);
            } else if (LayoutInterval.isAlignedAtBorder(aligning1, alignment)
                    && LayoutInterval.isAlignedAtBorder(aligning2, alignment)) {
                layoutModel.setGroupAlignment(group, alignment);
                layoutModel.setIntervalAlignment(aligning1, DEFAULT);
                layoutModel.setIntervalAlignment(aligning2, DEFAULT);
            }
        }

        // create the remainder group next to the aligned group
        if (!remainder.isEmpty()) {
            int index = commonSeq.indexOf(group);
            if (alignment == TRAILING)
                index++;
            LayoutInterval sideGroup = operations.addGroupContent(
                    remainder, commonSeq, index, dimension, alignment/*, effAlign*/);
            if (sideGroup != null) {
                int pos1 = parParent.getCurrentSpace().positions[dimension][alignment];
                int pos2 = toAlignWith.getCurrentSpace().positions[dimension][alignment];
                sideGroup.getCurrentSpace().set(dimension,
                                                alignment == LEADING ? pos1 : pos2,
                                                alignment == LEADING ? pos2 : pos1);
                operations.optimizeGaps(sideGroup, dimension);
                operations.mergeParallelGroups(sideGroup);
            }
        }

        if (toAlignWith.isParallel()) { // try to reduce possible unnececssary nesting
            operations.dissolveRedundantGroup(toAlignWith);
        }

        return group;
    }

    private int extract(LayoutInterval interval, List<LayoutInterval> toAlign, List<List> toRemain, int alignment) {
        int effAlign = LayoutInterval.getEffectiveAlignment(interval, alignment, false);
        LayoutInterval parent = interval.getParent();
        if (parent.isSequential()) {
            int extractCount = operations.extract(interval, alignment, false,
                                                  alignment == LEADING ? toRemain : null,
                                                  alignment == LEADING ? null : toRemain);
            if (extractCount == 1) { // the parent won't be reused
                layoutModel.removeInterval(parent);
                toAlign.add(interval);
            } else { // we'll reuse the parent sequence in the new group
                toAlign.add(parent);
            }
        }
        else {
            toAlign.add(interval);
        }
        return effAlign;
    }

    /**
     * Detect when an adjusting resizing interval was created over a fixed area
     * that requires to suppress resizing of the parent group.
     */
    private void checkParallelResizing(List<LayoutInterval> added, IncludeDesc iDesc1, IncludeDesc iDesc2) {
        boolean one = added.size() == 1;
        LayoutInterval interval = added.get(0);
        LayoutInterval parallelInt;
        LayoutInterval group = interval.getParent();
        if (group.isSequential()) {
            parallelInt = group;
            group = group.getParent();
        } else  {
            parallelInt = interval;
        }

        // find resizing neighbor gap of added interval
        LayoutInterval neighborGap = null;
        if (interval != parallelInt) {
            assert parallelInt.isSequential();
            LayoutInterval gap = LayoutInterval.getDirectNeighbor(interval, LEADING, false); // added.get(0)
            if (gap != null && gap.isEmptySpace() && LayoutInterval.canResize(gap)) {
                neighborGap = gap;
            } else {
                gap = LayoutInterval.getDirectNeighbor(added.get(added.size()-1), TRAILING, false);
                if (gap != null && gap.isEmptySpace() && LayoutInterval.canResize(gap)) {
                    neighborGap = gap;
                }
            }
        }

        // one interval resized to resizing or a resizing neighbor gap created
        if (one && LayoutInterval.wantResize(interval)) {
            if (!dragger.isResizing(dimension)) {
                return;
            }
        } else if (neighborGap == null) {
            return;
        }
        // Now we know a resizing interval was created over the parallel group.
        // We may want to adjust resizing of the group (e.g. suppress it if the
        // content is otherwise fixed) or eliminate resizing gaps that are no
        // longer needed for size definition of the group.

        // do nothing in root and in parallel group tied closely to root on both edges
        int rootAlign = DEFAULT;
        if (group.getParent() == null) {
            rootAlign = LayoutRegion.ALL_POINTS;
        } else {
            if (iDesc1.snappedNextTo != null && iDesc1.snappedNextTo.getParent() == null) {
                rootAlign = iDesc1.alignment;
            }
            if (iDesc2 != null && iDesc2.snappedNextTo != null && iDesc2.snappedNextTo.getParent() == null) {
                rootAlign = rootAlign == DEFAULT ? iDesc2.alignment : LayoutRegion.ALL_POINTS;
            }
            if (rootAlign == LEADING || rootAlign == TRAILING) {
                // one edge snapped next to root - check the other one for full span
                int remIdx = group.remove(parallelInt); // temporarily
                LayoutInterval neighbor = LayoutInterval.getNeighbor(group, rootAlign^1, false, true, true);
                if ((neighbor != null
                     && neighbor.getPreferredSize() == NOT_EXPLICITLY_DEFINED
                     && LayoutInterval.getEffectiveAlignmentInParent(group, LayoutInterval.getRoot(group), rootAlign^1) == (rootAlign^1))
                     ||
                    (neighbor == null
                     && LayoutInterval.isAlignedAtBorder(group, LayoutInterval.getRoot(group), rootAlign^1)))
                {   // the other group edge tied closely to root
                    rootAlign = LayoutRegion.ALL_POINTS;
                }
                group.add(parallelInt, remIdx);
            }
        }

        if (rootAlign != LayoutRegion.ALL_POINTS) {
            if (!LayoutInterval.canResize(group)
                && ((iDesc1.snappedNextTo != null && !group.isParentOf(iDesc1.snappedNextTo))
                     || (iDesc2 != null && iDesc2.snappedNextTo != null && !group.isParentOf(iDesc2.snappedNextTo))))
            {   // snapped out of the group - it might not want to be suppressed (will check right away)
                operations.enableGroupResizing(group);
            }

            // suppress par. group resizing if it is otherwise fixed
            while (LayoutInterval.canResize(group) && group.getParent() != null) {
                boolean otherResizing = false;
                boolean samePosition = false;
                boolean onEdge = true;
                for (int i=0; i < group.getSubIntervalCount(); i++) {
                    LayoutInterval li = group.getSubInterval(i);
                    if (li != parallelInt) {
                        if (LayoutInterval.wantResize(li)) {
                            otherResizing = true;
                            break;
                        }
                        if (group.isParallel() && !samePosition) {
                            int align = li.getAlignment();
                            if (align == LEADING || align == TRAILING)
                                samePosition = getExpectedBorderPosition(parallelInt, dimension, align^1)
                                // [adding space instead of parallelInt's space does not work with indent]
                                               == getExpectedBorderPosition(li, dimension, align^1);
                        }
                    } else if (group.isSequential() && i != 0 && i+1 != group.getSubIntervalCount()) {
                        onEdge = false;
                        break;
                    }
                }
                if (otherResizing || !onEdge) {
                    break;
                }  else if (samePosition) {
                    operations.suppressGroupResizing(group);
                    break;
                }
                parallelInt = group;
                group = group.getParent();
            }

            if (!LayoutInterval.canResize(group)) {
                // reset explicit size of interval or gap - subordinate to fixed content
                if (neighborGap != null) { // resizing neighbor gap created
                    boolean explicitSize = neighborGap.getPreferredSize() > 0;
                    layoutModel.setIntervalSize(neighborGap, NOT_EXPLICITLY_DEFINED, NOT_EXPLICITLY_DEFINED, Short.MAX_VALUE);
                    if (explicitSize) {
                        neighborGap.setAttribute(LayoutInterval.ATTR_SIZE_DIFF);
                    }
                }
                if (unresizedOnRemove != null && unresizedOnRemove[dimension] != null) {
                    // resizing of some intervals may be restored (those made fixed
                    // when temporary removed the manipulated ones)
                    for (LayoutInterval li : unresizedOnRemove[dimension]) {
                        if (!LayoutInterval.canResize(li) && group.isParentOf(li)) {
                            boolean l = LayoutInterval.isPlacedAtBorder(li, group, dimension, LEADING);
                            boolean t = LayoutInterval.isPlacedAtBorder(li, group, dimension, TRAILING);
                            if ((li.getParent() == group && l && t)
                                    || (li.getParent() != group && (l || t))) {
                                if (li.isParallel()) {
                                    operations.enableGroupResizing(li);
                                } else if (li.isSingle()) {
                                    operations.setIntervalResizing(li, true);
                                }
                            }
                        }
                    }
                }
            }

            if (interval.isComponent() && neighborGap == null
                && (parallelInt == interval
                    || (parallelInt == interval.getParent()
                        && LayoutInterval.getCount(parallelInt, LayoutRegion.ALL_POINTS, true) == 1))) {
                // look for same sized components
                operations.setParallelSameSize(group, parallelInt, dimension);
            }
        }

        operations.completeGroupResizing(group, dimension);
    }

    private int getExpectedBorderPosition(LayoutInterval interval, int dimension, int alignment) {
        LayoutInterval comp = LayoutUtils.getOutermostComponent(interval, dimension, alignment);
        int pos = comp.getCurrentSpace().positions[dimension][alignment];
        LayoutInterval neighbor = LayoutInterval.getNeighbor(comp, alignment, false, true, false);
        if (neighbor != null && neighbor.isEmptySpace() && interval.isParentOf(neighbor)) {
            int diff = neighbor.getPreferredSize();
            if (diff == NOT_EXPLICITLY_DEFINED)
                diff = LayoutUtils.getSizeOfDefaultGap(neighbor, operations.getMapper());
            if (alignment == LEADING)
                diff *= -1;
            pos += diff;
        }
        return pos;
    }

    private int determinePadding(LayoutInterval interval, PaddingType paddingType,
                                 int dimension, int alignment)
    {
        LayoutInterval neighbor = LayoutInterval.getNeighbor(interval, alignment, true, true, false);
        if (paddingType == null) {
            paddingType = PaddingType.RELATED;
        }
        // need to go through dragger as the component of 'interval' is not in model yet
        int[] pads = dragger.findPaddings(neighbor, interval, paddingType, dimension, alignment);
        return (pads != null && pads.length > 0) ? pads[0] : 0;
    }

    // -----

    private void analyzeParallel(LayoutInterval group, List<IncludeDesc> inclusions) {
        Iterator it = group.getSubIntervals();
        while (it.hasNext()) {
            LayoutInterval sub = (LayoutInterval) it.next();
            if (sub.isEmptySpace())
                continue;

            LayoutRegion subSpace = sub.getCurrentSpace();

            if (sub.isParallel() && shouldEnterGroup(sub)) {
                // group space contains significant edge
                analyzeParallel(sub, inclusions);
            } else if (sub.isSequential()) {
                // always analyze sequence - it may be valid even if there is no
                // overlap (not required in vertical dimension)
                analyzeSequential(sub, inclusions);
            } else {
                boolean ortOverlap = LayoutUtils.contentOverlap(addingSpace, sub, dimension^1);
                boolean toSeq = considerSequentialPosition(sub, ortOverlap);
                int margin = (dimension == VERTICAL && !toSeq ? 4 : 0);
                boolean dimOverlap = LayoutRegion.overlap(addingSpace, subSpace, dimension, margin);
                if (toSeq) {
                    int ortDistance = 0;
                    if (dimOverlap) { // overlaps in both dimensions
                        if (!solveOverlap && LayoutUtils.contentOverlap(addingSpace, sub)) {
                            continue;
                        }
                        imposeSize = true;
                    } else if (!ortOverlap) {
                        int dstL = LayoutRegion.distance(subSpace, addingSpace,
                                                         dimension^1, TRAILING, LEADING);
                        int dstT = LayoutRegion.distance(addingSpace, subSpace,
                                                         dimension^1, TRAILING, LEADING);
                        ortDistance = dstL >= 0 ? dstL : dstT;
                    }

                    int distance = LayoutRegion.UNKNOWN;
                    if (aSnappedNextTo != null) {
                        // check if aSnappedNextTo is related to this position with 'sub' as neighbor
                        LayoutInterval neighbor;
                        if (sub == aSnappedNextTo
                            || sub.isParentOf(aSnappedNextTo)
                            || aSnappedNextTo.getParent() == null
                            || (neighbor = LayoutInterval.getNeighbor(sub, aEdge, true, true, false)) == aSnappedNextTo
                            || (neighbor != null && neighbor.isParentOf(aSnappedNextTo)))
                        {   // nextTo snap is relevant to this position
                            distance = -1; // IncludeDesc.snappedNextTo will be set if distance == -1
                        }
                    }
                    if (distance != -1) {
                        if (!dimOverlap) { // determine distance from 'sub'
                            int dstL = LayoutRegion.distance(subSpace, addingSpace,
                                                             dimension, TRAILING, LEADING);
                            int dstT = LayoutRegion.distance(addingSpace, subSpace,
                                                             dimension, TRAILING, LEADING);
                            distance = dstL >= 0 ? dstL : dstT;
                        }
                        else distance = 0; // overlapping
                    }

                    IncludeDesc iDesc = addInclusion(group, false, distance, ortDistance, inclusions);
                    if (iDesc != null) {
                        iDesc.neighbor = sub;
                        iDesc.index = getAddDirection(sub, getAddingPoint()) == LEADING ? 0 : 1;
                    }
                }
            }
        }

        if (inclusions.isEmpty()) { // no inclusion found yet
            if (group.getParent() == null
                && (aSnappedParallel == null || canAlignWith(aSnappedParallel, group, aEdge)))
            {   // this is the last (top) valid group
                int distance = aSnappedNextTo == group ? -1 : Integer.MAX_VALUE;
                addInclusion(group, false, distance, Integer.MAX_VALUE, inclusions);
            }
        }
    }

    private void analyzeSequential(LayoutInterval group, List<IncludeDesc> inclusions) {
        boolean inSequence = false;
        boolean parallelWithSequence = false;
        int startIndex = -1, endIndex = -1;
        int startPos = LayoutRegion.UNKNOWN, endPos = LayoutRegion.UNKNOWN;
        boolean inSequenceParallelSnap = false;
        int distance = Integer.MAX_VALUE;
        int ortDistance = Integer.MAX_VALUE;

        for (int i=0,n=group.getSubIntervalCount(); i < n; i++) {
            LayoutInterval sub = group.getSubInterval(i);
            if (sub.isEmptySpace()) {
                if (startIndex == i) {
                    startIndex++;
                }
                continue;
            }

            LayoutRegion subSpace = sub.getCurrentSpace();

            // first analyze the interval as a possible sub-group
            if (sub.isParallel() && shouldEnterGroup(sub)) { // group space contains significant edge
                IncludeDesc[] before = inclusions.isEmpty() ? null : inclusions.toArray(new IncludeDesc[0]);

                analyzeParallel(sub, inclusions);

                if ((before == null && !inclusions.isEmpty())
                    || (before != null
                        && (before.length < inclusions.size()
                            || !Arrays.asList(before).containsAll(inclusions)))) {
                    return; // found something
                }
            }

            // second analyze the interval as a single element for "next to" placement
            boolean ortOverlap = LayoutUtils.contentOverlap(addingSpace, sub, dimension^1);
            boolean toSeq = considerSequentialPosition(sub, ortOverlap);
            int margin = (dimension == VERTICAL && !toSeq
                    && (aSnappedNextTo == null || !group.isParentOf(aSnappedNextTo)) ? 4 : 0);
            boolean dimOverlap = LayoutRegion.overlap(addingSpace, subSpace, dimension, margin);
            if (toSeq) {
                if (dimOverlap) { // overlaps in both dimensions
                    if (!solveOverlap && LayoutUtils.contentOverlap(addingSpace, sub)) { // don't want to solve the overlap in this sequence
                        parallelWithSequence = true;
                        continue;
                    }
                    if (ortOverlap) {
                        imposeSize = true;
                    }
                    distance = ortDistance = 0;
                } else { // determine distance from the interval
                    int dstL = LayoutRegion.distance(subSpace, addingSpace,
                                                     dimension, TRAILING, LEADING);
                    int dstT = LayoutRegion.distance(addingSpace, subSpace,
                                                     dimension, TRAILING, LEADING);
                    if (dstL >= 0 && dstL < distance)
                        distance = dstL;
                    if (dstT >= 0 && dstT < distance)
                        distance = dstT;

                    if (ortOverlap) {
                        ortDistance = 0;
                    } else { // remember also the orthogonal distance
                        dstL = LayoutRegion.distance(subSpace, addingSpace,
                                                     dimension^1, TRAILING, LEADING);
                        dstT = LayoutRegion.distance(addingSpace, subSpace,
                                                     dimension^1, TRAILING, LEADING);
                        if (dstL > 0 && dstL < ortDistance)
                            ortDistance = dstL;
                        if (dstT > 0 && dstT < ortDistance)
                            ortDistance = dstT;
                    }
                }
                inSequence = true;
                if (aSnappedParallel != null && (sub == aSnappedParallel || sub.isParentOf(aSnappedParallel))
                        && addingSpace.positions[dimension][aEdge] == subSpace.positions[dimension][aEdge]) {
                    inSequenceParallelSnap = true;
                }
                if (getAddDirection(sub, getAddingPoint()) == LEADING) {
                    endIndex = i;
                    if (!ortOverlap) {
                        endPos = subSpace.positions[dimension][LEADING];
                    }
                    break; // this interval is already after the adding one, no need to continue
                } else { // intervals before this one are irrelevant
                    parallelWithSequence = false;
                    startIndex = i + 1;
                    if (!ortOverlap) {
                        startPos = subSpace.positions[dimension][TRAILING];
                    }
                }
            } else { // no orthogonal overlap, moreover in vertical dimension located parallelly
                parallelWithSequence = true;
            }
        }

        if (inSequence) {
            if (startIndex < 0) {
                startIndex = 0;
            }
            if (endIndex < 0) {
                endIndex = group.getSubIntervalCount();
            }
            if (forwardIntoSubParallel(group, startIndex, endIndex, inclusions)) {
                return;
            }
            // so it make sense to add the interval to this sequence
            if (aSnappedNextTo != null) {
                if (group.isParentOf(aSnappedNextTo) || aSnappedNextTo.getParent() == null) {
                    distance = -1; // preferred distance
                } else {
                    LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(group.getParent(), aEdge, true);
                    if (neighbor != null
                        && (neighbor == aSnappedNextTo
                            || LayoutInterval.isAlignedAtBorder(aSnappedNextTo, neighbor, aEdge^1))) {
                        distance = -1; // preferred distance
                    }
                }
            }
            IncludeDesc iDesc = addInclusion(group, parallelWithSequence, distance, ortDistance, inclusions);
            if (iDesc != null) {
                if (iDesc.snappedParallel != null && group.isParentOf(iDesc.snappedParallel)
                         && !parallelWithSequence && dragger.isResizing(dimension^1)) {
                    // original position likely aligned with indent, but now resized next to (indent bigger than the component)
                    iDesc.snappedParallel = null;
                }
                if (parallelWithSequence && closedSpace == null) {
                    // In some cases need to reduce the maximum open position that would correspond to
                    // orthogonally overlapping boundaries. E.g. in vertical dimension to prefer creating
                    // rows, or when resizing/moving to preserve original space where the component was fit.
                    if (iDesc.snappedParallel != null) {
                        if (group.isParentOf(iDesc.snappedParallel) && !LayoutInterval.isAlignedAtBorder(aSnappedParallel, group.getParent(), aEdge)) {
                            if (aEdge == LEADING && startPos != LayoutRegion.UNKNOWN) {
                                startPos = iDesc.snappedParallel.getCurrentSpace().positions[dimension][LEADING];
                                startIndex = LayoutInterval.getIndexInParent(iDesc.snappedParallel, group);
                            } else if (aEdge == TRAILING && endPos != LayoutRegion.UNKNOWN) {
                                endPos = iDesc.snappedParallel.getCurrentSpace().positions[dimension][TRAILING];
                                endIndex = LayoutInterval.getIndexInParent(iDesc.snappedParallel, group);
                            }
                        }
                    } else if (dragger.isResizing()) {
                        if (originalInclusion1.parent == group && originalPosition.getGroupSpace() != null) {
                            closedSpace = new LayoutRegion(originalPosition.getGroupSpace());
                            if (dragger.getResizingEdge(dimension) == TRAILING && endPos != LayoutRegion.UNKNOWN) {
                                closedSpace.setPos(dimension, TRAILING, endPos);
                            } else if (dragger.getResizingEdge(dimension) == LEADING && startPos != LayoutRegion.UNKNOWN) {
                                closedSpace.setPos(dimension, LEADING, startPos);
                            }
                            closedSpace.expand(addingSpace);
                        }
                    }
                    if (closedSpace == null && (startPos != LayoutRegion.UNKNOWN || endPos != LayoutRegion.UNKNOWN)) {
                        if (startPos == LayoutRegion.UNKNOWN) {
                            startPos = group.getCurrentSpace().positions[dimension][LEADING];
                        }
                        if (endPos == LayoutRegion.UNKNOWN) {
                            endPos = group.getCurrentSpace().positions[dimension][TRAILING];
                        }
                        if (startPos != LayoutRegion.UNKNOWN && endPos != LayoutRegion.UNKNOWN) {
                            closedSpace = new LayoutRegion();
                            closedSpace.set(dimension, startPos, endPos);
                            closedSpace.expand(addingSpace);
                        }
                    }
                }
                if (!inSequenceParallelSnap) {
                    iDesc.index = (aEdge == LEADING) ? startIndex : endIndex;
                } else { // special case - aligning with interval in the same sequence - to insert at this position
                    int idx = LayoutInterval.getIndexInParent(aSnappedParallel, group);
                    iDesc.index = (aEdge == LEADING) ? idx : idx+1;
                }
            }
        }
    }

    private int getAddingPoint() {
        if (aEdge < 0) {
            return CENTER;
        } else if ((aEdge == LEADING || aEdge == TRAILING)
                   && aSnappedNextTo == null && aSnappedParallel == null) {
            // secondary edge that does not snap
            LayoutDragger.PositionDef primaryPos = newPositions[dimension];
            return primaryPos != null && primaryPos.snapped && primaryPos.alignment == (aEdge^1)
                    ? primaryPos.alignment : CENTER;
        }
        return aEdge;
    }

    /**
     * Checks whether addingInterval should be considered in sequential relation
     * with given interval. The basic criteria is if the two intervals overlap
     * in the orthogonal dimension, but more needs to be taken into account.
     */
    private boolean considerSequentialPosition(LayoutInterval interval, boolean ortOverlap) {
        int[][] space1 = interval.getCurrentSpace().positions;
        int[][] space2 = addingSpace.positions;
        int direction[] = new int[2]; // direction in which addingInterval is located from 'interval'
        for (int i=0; i < DIM_COUNT; i++) {
            if (space1[i][TRAILING] <= space2[i][LEADING]) {
                direction[i] = TRAILING;
            } else if (space1[i][LEADING] >= space2[i][TRAILING]) {
                direction[i] = LEADING;
            } else {
                direction[i] = -1; // overlap
            }
        }

        if (!ortOverlap && (direction[dimension] == -1 || !canConsiderSequence(interval, newPositions[dimension]))) {
            return false;
        }

        // in resizing operation consider the original seq. relation (if in the same ort. overlap situation)
        if (dragger.isResizing() && originalPosition != null
                && ortOverlap == LayoutUtils.contentOverlap(originalSpace, interval, dimension^1)) {
            if (!dragger.isResizing(dimension) // resizing only in the orthogonal dimension
                    || dragger.getResizingEdge(dimension) == direction[dimension] // resizing the opposite edge than 'interval' is located
                    || (direction[dimension] == -1 && ortOverlap)) { // overlapping with 'interval' in both dimensions
                // sequential relation should not change, keep it
                return originalPosition.wasInSequence(interval, dimension, false);
            } else if (!ortOverlap // overlap in neither dimension
                       && originalPosition.wasInSequence(interval, dimension, false)) { // keep original only if it was in sequence
                return true;
            }
        } // in all other cases reconsider

        if (!solveOverlap && direction[dimension^1] == -1
                && LayoutUtils.isOverlapPreventedInOtherDimension(addingInterval, interval, dimension)) {
            return false; // there will not be orthogonal overlap if placed sequentially in the other dimension
        }

        if (!ortOverlap) {
            if (!canConsiderSequence(interval, originalInclusion1) || !canConsiderSequence(interval, originalInclusion2)) {
                return false;
            }
            if (dimension == VERTICAL) {
                LayoutRegion middleSpace = new LayoutRegion();
                for (int i=0; i < DIM_COUNT; i++) {
                    if (i == dimension) {
                        middleSpace.set(i, space2[i][LEADING], space2[i][TRAILING]);
                    } else {
                        if (direction[i] == TRAILING) {
                            middleSpace.set(i, space1[i][TRAILING], space2[i][LEADING]);
                        } else {
                            middleSpace.set(i, space2[i][TRAILING], space1[i][LEADING]);
                        }
                    }
                }
                for (LayoutComponent middleComp : VisualState.getComponentsInRegion(
                        dragger.getTargetContainer(), dragger.getTargetRoots(), middleSpace)) {
                    if (LayoutUtils.isOverlapPreventedInOtherDimension(interval, middleComp.getLayoutInterval(dimension), dimension)) {
                        return false;
                    }
                }
                return true;
            }
        }

        // If adding on baseline or center in the other dimension, it may
        // influence what should be considered overlapping in that dimension.
        // 1) The overlap might be irrelevant if the interval's counter part in
        //    the other dimension is in sequence with the baseline/center group.
        // 2) Or in contrary, if e.g. shrinking a baseline component out of actual overlap,
        //    it will stay in the group in the end, so should be considered overlapping.
        LayoutInterval ortAligned = null;
        int ortAlignment = -1;
        LayoutDragger.PositionDef otherDimPos = newPositions[dimension^1];
        if (otherDimPos != null && otherDimPos.snapped) {
            ortAlignment = otherDimPos.alignment;
            if (ortAlignment == CENTER || ortAlignment == BASELINE) {
                ortAligned = otherDimPos.interval; // i.e. snapped to baseline/center
            }
        } else if (dragger.isResizing(dimension^1)) { // resizing withous snap
            OriginalPosition ortOrigPos = originalPositions[dimension^1];
            if (ortOrigPos != null) {
                ortAlignment = ortOrigPos.getAlignment();
            }
            if (ortAlignment == CENTER || ortAlignment == BASELINE) {
                ortAligned = ortOrigPos.getAlignedRep(ortAlignment);
            }
        }
        if (ortAligned != null) {
            // anticipating addSimplyAligned will be used in the other dimension,
            // creating/preserving closed group with baseline or center alignment
            if (!ortAligned.isParallel()) {
                LayoutInterval li = LayoutInterval.getFirstParent(ortAligned, PARALLEL);
                if (li.getGroupAlignment() == ortAlignment) {
                    ortAligned = li;
                }
            }
            Iterator<LayoutInterval> it = LayoutUtils.getComponentIterator(ortAligned);
            while (it.hasNext()) {
                LayoutInterval li = it.next().getComponent().getLayoutInterval(dimension);
                if (interval == li || interval.isParentOf(li)) {
                    return true; // so there is overlap (2)
                }
            }
            LayoutInterval ortInterval = LayoutUtils.getComponentIterator(interval).next()
                    .getComponent().getLayoutInterval(dimension^1);
            if (LayoutInterval.getCommonParent(ortAligned, ortInterval).isSequential()) {
                return false; // so in sequence with the center/baseline group (1)
            }
        }

        return ortOverlap;
    }

    private static boolean canConsiderSequence(LayoutInterval interval, LayoutDragger.PositionDef newPos) {
        return newPos == null || !newPos.snapped || newPos.nextTo || canConsiderSequence(interval, newPos.interval, newPos.alignment);
    }

    private boolean canConsiderSequence(LayoutInterval interval, IncludeDesc originalPos) {
        return !dragger.isResizing() || originalPos == null || originalPos.snappedParallel == null
                || dragger.getResizingEdge(dimension) == originalPos.alignment
                || canConsiderSequence(interval, originalPos.snappedParallel, originalPos.alignment);
    }

    private static boolean canConsiderSequence(LayoutInterval interval, LayoutInterval snappedParallel, int alignment) {
        if (snappedParallel == null) {
            return true;
        }
        if (interval == snappedParallel || interval.isParentOf(snappedParallel)) {
            return false;
        }
        LayoutInterval parent = interval.getParent();
        if (parent.isSequential()) {
            if (parent.isParentOf(snappedParallel)) {
                return true;
            } else {
                parent = parent.getParent();
            }
        }
        return parent != null
                && (parent == snappedParallel
                    || (parent.isParentOf(snappedParallel) && LayoutInterval.isAlignedAtBorder(snappedParallel, parent, alignment)));
    }

    private IncludeDesc addInclusion(LayoutInterval parent,
                                     boolean subgroup,
                                     int distance,
                                     int ortDistance,
                                     List<IncludeDesc> inclusions)
    {
        if (!inclusions.isEmpty()) {
            int index = inclusions.size() - 1;
            IncludeDesc last = inclusions.get(index);
            boolean useLast = false;
            boolean useNew = false;

            boolean ortOverlap1 = last.ortDistance == 0;
            boolean ortOverlap2 = ortDistance == 0;
            if (ortOverlap1 != ortOverlap2) {
                useLast = ortOverlap1;
                useNew = ortOverlap2;
            }
            else if (ortOverlap1) { // both having orthogonal overlap
                useLast = useNew = true;
            }
            else { // none having orthogonal overlap (could happen in vertical dimension)
                if (last.ortDistance != ortDistance) {
                    useLast = last.ortDistance < ortDistance;
                    useNew = ortDistance < last.ortDistance;
                }
                else if (last.distance != distance) {
                    useLast = last.distance < distance;
                    useNew = distance < last.distance;
                }
            }
            if (!useLast && !useNew) { // could not choose according to distance, so prefer deeper position
                LayoutInterval parParent = last.parent.isParallel() ?
                                           last.parent : last.parent.getParent();
                useNew = parParent.isParentOf(parent);
                useLast = !useNew;
            }

            if (!useLast)
                inclusions.remove(index);
            if (!useNew)
                return null;
        }

        IncludeDesc iDesc = new IncludeDesc();
        iDesc.parent = parent;
        iDesc.newSubGroup = subgroup;
        iDesc.alignment = aEdge;
        iDesc.snappedParallel = aSnappedParallel;
        if (distance == -1) {
            iDesc.snappedNextTo = aSnappedNextTo;
            iDesc.paddingType = aPaddingType;
            iDesc.fixedPosition = true;
        }
        iDesc.distance = distance;
        iDesc.ortDistance = ortDistance;
        inclusions.add(iDesc);

        return iDesc;
    }

    /**
     * Adds an inclusion for parallel aligning if none of found non-overlapping
     * inclusions is compatible with the required aligning.
     * Later mergeParallelInclusions may still unify the inclusions, but if not
     * then the inclusion created here is used - because requested parallel
     * aligning needs to be preserved even if overlapping can't be avoided.
     */
    private IncludeDesc addAligningInclusion(List<IncludeDesc> inclusions) {
        if (aSnappedParallel == null) {
            return null;
        }
        for (IncludeDesc inc : inclusions) {
            if (canAlignWith(aSnappedParallel, inc.parent, aEdge)) {
                return null;
            }
        }

        IncludeDesc iDesc = new IncludeDesc();
        if (!aSnappedParallel.isParallel()) {
            // If the component to align with spans the whole parallel group, it
            // may be desirable to align with the whole group instead in order to:
            // 1) not influence the group size (if there's anything resizable),
            // 2) preserve the group inner alignment of components at the opposite
            // edge (avoiding nesting that is more tending to S-layout problem).
            LayoutInterval parent = LayoutInterval.getFirstParent(aSnappedParallel, PARALLEL);
            if (parent != null && parent.getParent() != null
                    && LayoutInterval.isPlacedAtBorder(aSnappedParallel, parent, dimension, aEdge)
                    && ((LayoutInterval.contentWantResize(parent) && !LayoutInterval.wantResize(addingInterval))
                        || !LayoutInterval.isAlignedAtBorder(aSnappedParallel, parent, aEdge))) {
                iDesc.snappedParallel = parent;
                iDesc.parent = LayoutInterval.getFirstParent(parent, PARALLEL);
                boolean same = false;
                for (IncludeDesc inc : inclusions) {
                    if (inc.snappedParallel == aSnappedParallel) {
                        inc.snappedParallel = iDesc.snappedParallel;
                        if (inc.parent == iDesc.parent) {
                            same = true;
                        }
                    }
                }
                if (same) {
                    return null; // we have such inclusion after all (just corrected its snappedParallel)
                }
            }
        }
        if (iDesc.snappedParallel == null) {
            iDesc.snappedParallel = aSnappedParallel;
            iDesc.parent = LayoutInterval.getFirstParent(aSnappedParallel, PARALLEL);
        }
        if (iDesc.parent == null) {
            iDesc.parent = aSnappedParallel;
        }
        iDesc.alignment = aEdge;
        inclusions.add(0, iDesc);
        return iDesc;
    }

    private boolean forwardIntoSubParallel(LayoutInterval seq, int startIndex, int endIndex, List<IncludeDesc> inclusions) {
        if (aSnappedParallel != null) {
            return false;
        }

        LayoutInterval[] neighbors = new LayoutInterval[2];
        boolean aimsToGroup[] = new boolean[2];
        for (int e=LEADING; e <= TRAILING; e++) {
            if (aSnappedNextTo != null && e == (aEdge^1)) {
                continue;
            }
            LayoutInterval neighbor = null;
            if (e == LEADING) {
                if (startIndex-1 >= 0) {
                    neighbor = seq.getSubInterval(startIndex-1);
                    if (neighbor.isEmptySpace() && startIndex-2 >= 0) {
                        neighbor = seq.getSubInterval(startIndex-2);
                    }
                }
            } else if (endIndex < seq.getSubIntervalCount()) {
                neighbor = seq.getSubInterval(endIndex);
            }
            if (neighbor != null && neighbor.isParallel()) {
                neighbors[e] = neighbor;
                aimsToGroup[e] = addingOverGroupEdge(neighbor, e^1);
            }
        }

        if (dimension == VERTICAL && !aimsToGroup[LEADING] && !aimsToGroup[TRAILING]) {
            return false;
        }

        int tryFirst;
        if (neighbors[LEADING] != null && neighbors[TRAILING] != null
                && aimsToGroup[LEADING] == aimsToGroup[TRAILING]) {
            int d1 = LayoutRegion.distance(neighbors[LEADING].getCurrentSpace(), addingSpace,
                                           dimension, TRAILING, LEADING);
            int d2 = LayoutRegion.distance(addingSpace, neighbors[TRAILING].getCurrentSpace(),
                                           dimension, TRAILING, LEADING);
            tryFirst = d1 <= d2 ? LEADING : TRAILING;
        } else if (aimsToGroup[LEADING]) {
            tryFirst = (!aimsToGroup[TRAILING] || aEdge != TRAILING) ? LEADING : TRAILING;
        } else {
            tryFirst = !aimsToGroup[TRAILING] ? LEADING : TRAILING;
        }
        for (int e=tryFirst, edgeCount=2; edgeCount > 0; edgeCount--, e^=1) {
            LayoutInterval neighbor = neighbors[e];
            if (neighbor != null && groupOpenToEnter(neighbor, e^1)) {
                int count = inclusions.size();
                analyzeParallel(neighbor, inclusions);
                if (inclusions.size() > count) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean addingOverGroupEdge(LayoutInterval group, int alignment) {
        int[] apos = addingSpace.positions[dimension];
        int[] gpos = group.getCurrentSpace().positions[dimension];
        return apos[LEADING] < gpos[alignment] && apos[TRAILING] > gpos[alignment];
    }

    private boolean stickingOutOfGroup(LayoutInterval group, int alignment) {
        int[] apos = addingSpace.positions[dimension];
        int[] gpos = group.getCurrentSpace().positions[dimension];
        return alignment == LEADING ? apos[LEADING] < gpos[LEADING] : apos[TRAILING] > gpos[TRAILING];
    }

    private boolean hasOpenRoomForAdding(LayoutInterval group, int alignment) {
        assert group.isParallel();
        LayoutRegion groupSpace = group.getCurrentSpace();
        for (LayoutInterval comp : LayoutUtils.edgeSubComponents(group, alignment, false)) {
            LayoutRegion compSpace = comp.getCurrentSpace();
            if (LayoutRegion.overlap(addingSpace, compSpace, dimension^1, 0)
                && compSpace.positions[dimension][alignment] == groupSpace.positions[dimension][alignment]) {
                return false;
            }
        }
        return true;
    }

    /**
     * The inclusion determined by analyzeParallel method is always the most open
     * possible at given location. However, at some specific conditions a closed
     * position can be preferred (i.e. placing the component into a parallel
     * group instead of independently with it). In such case this methods
     * modifies the given IncludeDesc object and returns true.
     */
    private boolean preferClosedPosition(IncludeDesc newDesc, IncludeDesc origDesc) {
        if (originalPosition == null) {
            return false;
        }

        // Check if something is moved within a closed group (typically moved
        // vertically within a column).
        if (origDesc != null && origDesc != newDesc && originalPosition.isClosedSpace(origDesc.alignment)
                && layoutModel.getChangeMark().equals(undoCheckMark)) {
            LayoutInterval origParent = origDesc.parent;
            if (origParent.isSequential() && !origDesc.newSubGroup) {
                origParent = origParent.getParent();
            }
            LayoutRegion origClosedSpace = originalPosition.getGroupSpace();
            if ((newDesc.parent == origParent || newDesc.parent.isParentOf(origParent))
                  && LayoutRegion.pointInside(addingSpace, LEADING, origClosedSpace, dimension)
                  && LayoutRegion.pointInside(addingSpace, TRAILING, origClosedSpace, dimension)
                  && newDesc.snappedNextTo == null
                  && (newDesc.snappedParallel == null || newDesc.snappedParallel == origParent
                      || origParent.isParentOf(newDesc.snappedParallel))) {
                boolean sameNeighbors;
                if (origParent.isParallel()) {
                    sameNeighbors = (newDesc.neighbor == origDesc.neighbor);
                } else {
                    sameNeighbors = true;
                    for (Iterator<LayoutInterval> it=origParent.getSubIntervals(); it.hasNext(); ) {
                        LayoutInterval sub = it.next();
                        if (sub.isEmptySpace()) {
                            continue;
                        }
                        LayoutRegion subSpace = sub.getCurrentSpace();
                        if (LayoutUtils.contentOverlap(addingSpace, sub, dimension^1)
                                && LayoutRegion.overlap(addingSpace, subSpace, dimension, 0)
                                   != LayoutRegion.overlap(origClosedSpace, subSpace, dimension, 0)) {
                            sameNeighbors = false;
                            break;
                        }
                    }
                }
                if (sameNeighbors) {
                    newDesc.parent = origParent;
                    newDesc.index = origDesc.parent == origParent ? origDesc.index : -1;
                    newDesc.newSubGroup = origDesc.newSubGroup;
                    closedSpace = new LayoutRegion(origClosedSpace);
                    closedSpace.set(dimension^1, addingSpace);
                    return true;
                }
            }
        }

        // Check if a resizing component is moved from a group where everything
        // is resizing to a similar group (typically the case of groups with
        // suppresed resizing).
        if (!dragger.isResizing()
                && originalPosition.isWholeResizing()
                && newDesc.snappedParallel != null && newDesc.neighbor == null) {
            LayoutInterval snapParent;
            if (newDesc.snappedParallel.isParallel()) {
                snapParent = newDesc.snappedParallel;
            } else {
                snapParent = LayoutInterval.getFirstParent(newDesc.snappedParallel, PARALLEL);
                if (!LayoutInterval.isAlignedAtBorder(newDesc.snappedParallel, snapParent, aEdge)) {
                    snapParent = null;
                }
            }
            if (snapParent != null
                    && newDesc.parent.isParentOf(snapParent)
                    && (!LayoutInterval.canResize(snapParent)
                        || (!originalPosition.isSuppressedResizing() && LayoutInterval.wantResize(snapParent)))) {
                newDesc.parent = snapParent;
                newDesc.index = -1;
                newDesc.neighbor = null;
                newDesc.newSubGroup = false;
                closedSpace = new LayoutRegion(snapParent.getCurrentSpace());
                closedSpace.set(dimension^1, addingSpace);
                if (originalPosition.isSuppressedResizing() && originalPosition.isWholeResizing()) {
                    int defSizeDef = LayoutInterval.getDefaultSizeDef(addingInterval);
                    if (addingInterval.getPreferredSize() != defSizeDef) {
                        operations.resizeInterval(addingInterval, defSizeDef);
                    }
                    int overSize = addingInterval.getDiffToDefaultSize();
                    if (overSize > 0) {
                        if (newDesc.alignment == TRAILING) {
                            addingInterval.getCurrentSpace().reshape(dimension, LEADING, overSize);
                        } else {
                            addingInterval.getCurrentSpace().reshape(dimension, TRAILING, -overSize);
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * @param preserveOriginal if true, original inclusion needs to be preserved,
     *        will be merged with new inclusion sequentially; if false, original
     *        inclusion is just consulted when choosing best inclusion
     */
    private void mergeParallelInclusions(List<IncludeDesc> inclusions, IncludeDesc original, boolean preserveOriginal) {
        // 1st step - find representative (best) inclusion
        IncludeDesc best = null;
        boolean bestOriginal = false;
        for (Iterator it=inclusions.iterator(); it.hasNext(); ) {
            IncludeDesc iDesc = (IncludeDesc) it.next();
            if (original == null || !preserveOriginal || canCombine(iDesc, original)) {
                if (best != null) {
                    boolean originalCompatible = original != null && !preserveOriginal
                                                 && iDesc.parent == original.parent;
                    if (!bestOriginal && originalCompatible) {
                        best = iDesc;
                        bestOriginal = true;
                    }
                    else if (bestOriginal == originalCompatible) {
                        LayoutInterval group1 = best.parent.isSequential() ?
                                                best.parent.getParent() : best.parent;
                        LayoutInterval group2 = iDesc.parent.isSequential() ?
                                                iDesc.parent.getParent() : iDesc.parent;
                        if (group1.isParentOf(group2)) {
                            best = iDesc; // deeper is better
                        }
                        else if (!group2.isParentOf(group1) && iDesc.distance < best.distance) {
                            best = iDesc;
                        }
                    }
                }
                else {
                    best = iDesc;
                    bestOriginal = original != null && !preserveOriginal && iDesc.parent == original.parent;
                }
            }
        }

        if (best == null) { // nothing compatible with original position
            assert preserveOriginal;
            inclusions.clear();
            inclusions.add(original);
            return;
        }

        LayoutInterval commonGroup = best.parent.isSequential() ? best.parent.getParent() : best.parent;

        // 2nd remove incompatible inclusions, move compatible ones to same level
        for (Iterator it=inclusions.iterator(); it.hasNext(); ) {
            IncludeDesc iDesc = (IncludeDesc) it.next();
            if (iDesc != best) {
                if (!compatibleInclusions(iDesc, best, dimension)) {
                    it.remove();
                } else if (iDesc.parent == best.parent && iDesc.neighbor == best.neighbor
                           && (iDesc.neighbor != null || iDesc.index == iDesc.index)) {
                    it.remove(); // same inclusion twice (detect for better robustness)
                } else if (iDesc.newSubGroup && LayoutUtils.contentOverlap(iDesc.parent, best.parent, dimension^1)) {
                    it.remove(); // don't try to solve what is already overlapping
                } else {
                    LayoutInterval group = iDesc.parent.isSequential() ?
                                           iDesc.parent.getParent() : iDesc.parent;
                    if (group.isParentOf(commonGroup)) {
                        LayoutInterval neighbor = iDesc.parent.isSequential() ?
                                                  iDesc.parent : iDesc.neighbor;
                        layoutModel.removeInterval(neighbor);
                        // [what about the alignment?]
                        layoutModel.addInterval(neighbor, commonGroup, -1);
                        // possibly adjust fixed gaps due to a position shift
                        for (int e=LEADING; e <= TRAILING; e++) {
                            int d = (e==LEADING) ? 1 : -1;
                            int posDiff = LayoutRegion.distance(group.getCurrentSpace(), commonGroup.getCurrentSpace(), dimension, e, e);
                            if (posDiff != LayoutRegion.UNKNOWN) {
                                posDiff *= d;
                            }
                            if (posDiff > 0) {
                                for (LayoutInterval gap : LayoutUtils.getSideGaps(neighbor, e, true)) {
                                    int currentSize = LayoutInterval.canResize(gap) ? NOT_EXPLICITLY_DEFINED : gap.getPreferredSize();
                                    if (currentSize > posDiff) {
                                        operations.resizeInterval(gap, currentSize - posDiff);
                                    }
                                }
                            }
                        }
                        if (iDesc.parent == group) {
                            iDesc.parent = commonGroup;
                        }
                        if (group.getSubIntervalCount() == 1 && group.getParent() != null) {
                            LayoutInterval parent = group.getParent();
                            LayoutInterval last = layoutModel.removeInterval(group, 0);
                            int index = layoutModel.removeInterval(group);
                            operations.addContent(last, parent, index, dimension);
                            updateInclusionsForEliminatedGroup(inclusions, group, parent, last, index);
                            if (last.getParent() == null) { // dissolved into parent
                                updateInclusionsForEliminatedGroup(inclusions, last, parent, null, index);
                                if (commonGroup == last) {
                                    commonGroup = parent; // parent is parallel in this case
                                }
                            }
                        }
                    }
                }
            }
        }

        if (original != null && original.parent.isParallel() && original.snappedParallel != null
                && original.ortDistance != 0 && inclusions.size() > 1) {
            // inclusion not overlapping orthogonally is not meaningful (either
            // forced by addAlignedInclusion, or it's an original position of
            // resizing in orthogonal dimension that started not overlapping)
            inclusions.remove(original);
        }
        if (inclusions.size() == 1)
            return;

        // 3rd analyze inclusions requiring a subgroup (parallel with part of sequence)
        LayoutInterval subGroup = null;
        int subEffAlign = -1;
        LayoutInterval nextTo = null;
        List<List> separatedLeading = new LinkedList<List>();
        List<List> separatedTrailing = new LinkedList<List>();

        for (Iterator it=inclusions.iterator(); it.hasNext(); ) {
            IncludeDesc iDesc = (IncludeDesc) it.next();
            if (iDesc.parent.isSequential() && iDesc.newSubGroup) {
                int[] parallelBoundaries = collectNeighborPositions(inclusions, iDesc, addingSpace, dimension);
                LayoutInterval parSeq = extractParallelSequence(iDesc.parent, addingSpace, -1, -1, iDesc.alignment, parallelBoundaries);
                if (parSeq != null) {
                    assert parSeq.isParallel(); // parallel group with part of the original sequence
                    if (subGroup == null) {
                        subGroup = parSeq;
                        subEffAlign = LayoutInterval.getEffectiveAlignment(parSeq);
                    } else {
                        do {
                            LayoutInterval sub = layoutModel.removeInterval(parSeq, 0);
                            layoutModel.addInterval(sub, subGroup, -1);
                        } while (parSeq.getSubIntervalCount() > 0);
                        // correct (shift) current positions of the common subgroup
                        if (subEffAlign == LEADING || subEffAlign == TRAILING) {
                            LayoutRegion commSpace = subGroup.getCurrentSpace();
                            LayoutRegion space = parSeq.getCurrentSpace();
                            int e1 = subEffAlign;
                            int e2 = (subEffAlign^1);
                            int d = (e1==LEADING) ? 1 : -1;
                            if (LayoutRegion.distance(commSpace, space, dimension, e1, e1)*d > 0) {
                                commSpace.setPos(dimension, LEADING, space.positions[dimension][LEADING]);
                            }
                            if (LayoutRegion.distance(commSpace, space, dimension, e2, e2)*d > 0) {
                                commSpace.setPos(dimension, e2, space.positions[dimension][e2]);
                            }
                        }
                    }
                    // extract surroundings of the group in the sequence
                    operations.extract(parSeq, DEFAULT, true, separatedLeading, separatedTrailing);
                    layoutModel.removeInterval(parSeq);
                    layoutModel.removeInterval(iDesc.parent);
                } else { // nothing left to extract, the whole sequence is likely covered by the parallel neighbors
                    LayoutRegion seqSpace = iDesc.parent.getCurrentSpace();
                    if (seqSpace.isSet(dimension)) {
                        for (int e=LEADING; e <= TRAILING; e++) {
                            if (parallelBoundaries[e] != LayoutRegion.UNKNOWN) {
                                int sPos = seqSpace.positions[dimension][e^1];
                                int bPos = parallelBoundaries[e];
                                if (e == TRAILING) {
                                    sPos = -sPos; bPos = -bPos;
                                }
                                if (bPos >= sPos) { // parallel neigbors span the whole sequence
                                    iDesc.newSubGroup = false;
                                    iDesc.index = (e == TRAILING ? 0 : iDesc.parent.getSubIntervalCount());
                                    break;
                                }
                            }
                        }
                    }
                    if (iDesc.newSubGroup) {
                        it.remove();
                        if (inclusions.size() == 1) {
                            if (separatedLeading.isEmpty() && separatedTrailing.isEmpty()) {
                                return;
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        }

        int extractAlign = DEFAULT;
        if (subGroup != null) {
            if (separatedLeading.isEmpty())
                extractAlign = TRAILING;
            if (separatedTrailing.isEmpty())
                extractAlign = LEADING;
        }
        // Surroundings of adding interval determined in step 4 are created separately
        // one by one, but we need to unify the resizability of all the gaps next to
        // the adding interval together.
        boolean[] anyResizingNeighbor = new boolean[2];
        int[] fixedSideGaps = new int[2];
        List<LayoutInterval[]> unifyGaps = null;

        // Placing the adding interval into individual inclusions in step 4
        // could destroy it if it's a sequential group of multiple components.
        LayoutInterval addingMultiWrapper;
        if (addingInterval.isSequential()) {
            addingMultiWrapper = new LayoutInterval(PARALLEL);
            addingMultiWrapper.add(addingInterval, 0);
            addingMultiWrapper.getCurrentSpace().set(addingSpace);
            addingInterval = addingMultiWrapper;
        } else {
            addingMultiWrapper = null;
        }

        // 4th collect surroundings of adding interval
        // (the intervals will go into a side group in step 5, or into subgroup
        //  of 'subGroup' next to the adding interval if in previous step some
        //  content was separated into a parallel subgroup of a sequence)
        LayoutInterval subsubGroup = null;
        for (Iterator it=inclusions.iterator(); it.hasNext(); ) {
            IncludeDesc iDesc = (IncludeDesc) it.next();
            if (iDesc.parent.isParallel() || !iDesc.newSubGroup) {
                // add to this inclusion (temporarily) - then can get surroundings
                LayoutInterval snp = null;
                if (iDesc.snappedParallel != null && !iDesc.parent.isParentOf(iDesc.snappedParallel)) {
                    snp = iDesc.snappedParallel;  // it may be removed from layout at this moment,
                    iDesc.snappedParallel = null; // and we won't do aligning in parallel anyway
                }
                addToGroup(iDesc, null, false);
                if (snp != null) {
                    iDesc.snappedParallel = snp;
                }

                if (subGroup == null && !LayoutInterval.wantResize(addingInterval)) {
                    // now we may have L and T gaps next to the added interval
                    LayoutInterval lGap = LayoutInterval.getDirectNeighbor(addingInterval, LEADING, false);
                    LayoutInterval tGap = LayoutInterval.getDirectNeighbor(addingInterval, TRAILING, false);
                    if (lGap != null && lGap.isEmptySpace() && tGap != null && tGap.isEmptySpace()) {
                        LayoutInterval[] gaps = new LayoutInterval[] { lGap, tGap };
                        for (int i=LEADING; i <= TRAILING; i++) {
                            if (!LayoutInterval.canResize(gaps[i])) {
                                if (LayoutInterval.hasAnyResizingNeighbor(gaps[i], i)) {
                                    anyResizingNeighbor[i] = true;
                                    gaps[i] = null;
                                }
                                fixedSideGaps[i]++;
                            }
                        }
                        if (gaps[LEADING] != null && gaps[TRAILING] != null) {
                            if (unifyGaps == null) {
                                unifyGaps = new ArrayList<>();
                            }
                            unifyGaps.add(gaps);
                        }
                    }
                }
                // extract the surroundings
                operations.extract(addingInterval, extractAlign, extractAlign == DEFAULT,
                                   separatedLeading, separatedTrailing);
                LayoutInterval parent = addingInterval.getParent();
                layoutModel.removeInterval(addingInterval);
                layoutModel.removeInterval(parent);
                if (extractAlign != DEFAULT && LayoutInterval.getCount(parent, LayoutRegion.ALL_POINTS, true) >= 1) {
                    if (subsubGroup == null) {
                        subsubGroup = new LayoutInterval(PARALLEL);
                        subsubGroup.setGroupAlignment(extractAlign);
                    }
                    operations.addContent(parent, subsubGroup, -1, dimension);
                }
            }
            if (iDesc.snappedNextTo != null)
                nextTo = iDesc.snappedNextTo;
            if (iDesc != best)
                it.remove();
        }
        if (!inclusions.contains(best)) {
            inclusions.add(best);
        }

        if (addingMultiWrapper != null) {
            addingInterval = addingMultiWrapper.remove(0);
        }

        if (unifyGaps != null) {
            // unify resizability of the border gaps collected for individual inclusions
            for (LayoutInterval[] gaps : unifyGaps) {
                int preferredFixedSide = fixedSideGaps[LEADING] >= fixedSideGaps[TRAILING] ? LEADING : TRAILING;
                for (int i=LEADING; i <= TRAILING; i++) {
                    if (LayoutInterval.canResize(gaps[i]) && !anyResizingNeighbor[i]
                            && (anyResizingNeighbor[i^1] || preferredFixedSide == i)) {
                        operations.setIntervalResizing(gaps[i], false);
                        if (!LayoutInterval.canResize(gaps[i^1])) {
                            operations.setIntervalResizing(gaps[i^i], true);
                        }
                        break;
                    }
                }
            }
        } else if (subGroup != null && (subEffAlign == LEADING || subEffAlign == TRAILING)) {
            // adjust size of the border gaps in the extracted sub-group (some may have shifted)
            int d = (subEffAlign==LEADING) ? 1 : -1;
            int groupPos = subGroup.getCurrentSpace().positions[dimension][subEffAlign];
            for (LayoutInterval gap : LayoutUtils.getSideGaps(subGroup, subEffAlign, true)) {
                int currentSize = LayoutInterval.canResize(gap) ? NOT_EXPLICITLY_DEFINED : gap.getPreferredSize();
                if (currentSize > 0) {
                    int pos = LayoutUtils.getVisualPosition(gap, dimension, subEffAlign^1);
                    int expectedSize = (pos - groupPos) * d;
                    if (expectedSize > 0 && expectedSize < currentSize) {
                        operations.resizeInterval(gap, expectedSize);
                    }
                }
            }
        }

        // prepare the common group for merged content
        int[] borderPos = commonGroup.getCurrentSpace().positions[dimension];
        int[] neighborPos = (subGroup != null ? subGroup : addingInterval).getCurrentSpace().positions[dimension];
        LayoutInterval commonSeq;
        int index;
        if (commonGroup.getSubIntervalCount() == 0 && commonGroup.getParent() != null) {
            // the common group got empty - eliminate it to avoid unncessary nesting
            LayoutInterval parent = commonGroup.getParent();
            index = layoutModel.removeInterval(commonGroup);
            updateInclusionsForEliminatedGroup_ParallelSnap(inclusions, commonGroup, parent, null);
            if (parent.isSequential()) {
                commonSeq = parent;
                commonGroup = parent.getParent();
            }
            else { // parallel parent
                commonSeq = new LayoutInterval(SEQUENTIAL);
                commonSeq.setAlignment(commonGroup.getAlignment());
                layoutModel.addInterval(commonSeq, parent, index);
                commonGroup = parent;
                index = 0;
            }
        }
        else {
            commonSeq = new LayoutInterval(SEQUENTIAL);
            layoutModel.addInterval(commonSeq, commonGroup, -1);
            index = 0;
        }
        if (commonSeq.getSubIntervalCount() == 0) {
            commonSeq.getCurrentSpace().set(dimension, commonGroup.getCurrentSpace());
        }

        // 5th create groups of merged content around the adding component
        LayoutInterval sideGroupLeading = null;
        LayoutInterval sideGroupTrailing = null;
        if (!separatedLeading.isEmpty()) {
            int checkCount = commonSeq.getSubIntervalCount(); // remember ...
            sideGroupLeading = operations.addGroupContent(
                    separatedLeading, commonSeq, index, dimension, LEADING); //, mainEffectiveAlign
            index += commonSeq.getSubIntervalCount() - checkCount;
        }
        if (!separatedTrailing.isEmpty()) {
            sideGroupTrailing = operations.addGroupContent(
                    separatedTrailing, commonSeq, index, dimension, TRAILING); //, mainEffectiveAlign
        }
        if (sideGroupLeading != null) {
            int checkCount = commonSeq.getSubIntervalCount(); // remember ...
            sideGroupLeading.getCurrentSpace().set(dimension, borderPos[LEADING], neighborPos[LEADING]);
            operations.optimizeGaps(sideGroupLeading, dimension);
            index += commonSeq.getSubIntervalCount() - checkCount;
        }
        if (sideGroupTrailing != null) {
            sideGroupTrailing.getCurrentSpace().set(dimension, neighborPos[TRAILING], borderPos[TRAILING]);
            operations.optimizeGaps(sideGroupTrailing, dimension);
        }

        // 6th adjust the final inclusion
        best.parent = commonSeq;
        best.newSubGroup = false;
        best.neighbor = null;

        LayoutInterval separatingGap;
        int gapIdx = index;
        if (gapIdx == commonSeq.getSubIntervalCount()) {
            gapIdx--;
            separatingGap = commonSeq.getSubInterval(gapIdx);
        }
        else {
            separatingGap = commonSeq.getSubInterval(gapIdx);
            if (!separatingGap.isEmptySpace()) {
                gapIdx--;
                if (gapIdx > 0)
                    separatingGap = commonSeq.getSubInterval(gapIdx);
            }
        }
        if (!separatingGap.isEmptySpace())
            separatingGap = null;
        else if (subGroup == null) {
            index = gapIdx;
            // eliminate the gap if caused by addToGroup called to separate adding
            // interval's surroundings to side groups; the gap will be created
            // again when addToGroup is called definitively (for merged inclusions)
            if (index == 0 && !LayoutInterval.isAlignedAtBorder(commonSeq, LEADING)) {
                layoutModel.removeInterval(separatingGap);
                separatingGap = null;
            }
            else if (index == commonSeq.getSubIntervalCount()-1 && !LayoutInterval.isAlignedAtBorder(commonSeq, TRAILING)) {
                layoutModel.removeInterval(separatingGap);
                separatingGap = null;
            }
        }

        best.snappedNextTo = nextTo;
        if (nextTo != null)
            best.fixedPosition = true;

        // 7th resolve subgroup
        if (subGroup != null) {
            if (separatingGap != null
                && (extractAlign == DEFAULT
                    || (extractAlign == LEADING && index > gapIdx)
                    || (extractAlign == TRAILING && index <= gapIdx)))
            {   // subGroup goes next to a separating gap - which is likely superflous
                // (the extracted parallel sequence in subGroup has its own gap)
                layoutModel.removeInterval(separatingGap);
                if (index >= gapIdx && index > 0)
                    index--;
            }
            int subIdx = index;
            if (subsubGroup != null && subsubGroup.getSubIntervalCount() > 0) {
                LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
                seq.setAlignment(best.alignment);
                operations.addContent(subsubGroup, seq, 0, dimension);
                layoutModel.addInterval(seq, subGroup, -1);
                // [should run optimizeGaps on subsubGroup?]
                best.parent = seq;
                index = extractAlign == LEADING ? 0 : seq.getSubIntervalCount();
            }
            else {
                best.newSubGroup = true;
            }
            operations.addContent(subGroup, commonSeq, subIdx, dimension);
        }

        operations.mergeConsecutiveGaps(commonSeq, index-1, dimension);

        best.index = index;

        updateReplacedOriginalGroup(commonGroup, commonSeq, index);

        if (subGroup != null && best.newSubGroup && best.snappedParallel != null) {
            // after reconfiguring into subgroup it may require to re-check the aligned inclusion (bug 203742)
            IncludeDesc alignedDesc = addAligningInclusion(inclusions);
            if (alignedDesc != null) {
                inclusions.remove(best.parent.isParentOf(alignedDesc.parent) ? best : alignedDesc);
            }
        }

        optimizeStructure = true;
    }

    private static boolean compatibleInclusions(IncludeDesc iDesc1, IncludeDesc iDesc2, int dimension) {
        LayoutInterval group1 = iDesc1.parent.isSequential() ?
                                iDesc1.parent.getParent() : iDesc1.parent;
        LayoutInterval group2 = iDesc2.parent.isSequential() ?
                                iDesc2.parent.getParent() : iDesc2.parent;
        if (group1 == group2) {
            return true;
        }

        if (group1.isParentOf(group2)) {
            // swap so group2 is parent of group1 (iDesc1 the deeper inclusion)
            LayoutInterval temp = group1;
            group1 = group2;
            group2 = temp;
            IncludeDesc itemp = iDesc1;
            iDesc1 = iDesc2;
            iDesc2 = itemp;
        }
        else if (!group2.isParentOf(group1)) {
            return false;
        }

        LayoutInterval neighbor; // to be moved into the deeper group (in parallel)
        if (iDesc2.parent.isSequential()) {
            if (iDesc2.parent.isParentOf(iDesc1.parent)) {
                // in the same sequence, can't combine in parallel
                return false;
            }
            neighbor = iDesc2.parent;
        } else {
            neighbor = iDesc2.neighbor;
        }
        if (neighbor == null) {
            return false;
        }
        LayoutRegion spaceToHold = new LayoutRegion(neighbor.getCurrentSpace());
        LayoutInterval lComp = LayoutUtils.getOutermostComponent(neighbor, dimension, 0);
        LayoutInterval tComp = LayoutUtils.getOutermostComponent(neighbor, dimension, 1);
        if (lComp != null && tComp != null) {
            spaceToHold.set(dimension,
                            lComp.getCurrentSpace().positions[dimension][LEADING],
                            tComp.getCurrentSpace().positions[dimension][TRAILING]);
        }
        LayoutRegion spaceAvailable = group1.getCurrentSpace();
        return LayoutRegion.pointInside(spaceToHold, LEADING, spaceAvailable, dimension)
               && LayoutRegion.pointInside(spaceToHold, TRAILING, spaceAvailable, dimension);
    }

    private void updateInclusionsForEliminatedGroup(List<IncludeDesc> inclusions,
            LayoutInterval replacedGroup, LayoutInterval newGroup, LayoutInterval groupContent, int index) {
        for (IncludeDesc iDesc : inclusions) {
            updateReplacedGroup(iDesc, replacedGroup, newGroup, groupContent, index);
        }
        if (originalPosition != null) {
            if (originalInclusion1 != null) {
                updateReplacedGroup(originalInclusion1, replacedGroup, newGroup, groupContent, index);
            }
            if (originalInclusion2 != null) {
                updateReplacedGroup(originalInclusion2, replacedGroup, newGroup, groupContent, index);
            }
        }
        updateInclusionsForEliminatedGroup_ParallelSnap(inclusions, replacedGroup, newGroup, groupContent);
    }

    private static void updateReplacedGroup(IncludeDesc iDesc,
            LayoutInterval replacedGroup, LayoutInterval newGroup, LayoutInterval groupContent, int index) {
        if (iDesc.parent == replacedGroup) {
            if (newGroup.isSequential()) {
                if (replacedGroup.isParallel()) {
                    iDesc.newSubGroup = true;
                } else if (iDesc.index >= 0 && index >= 0) {
                    iDesc.index += index;
                }
            }
            iDesc.parent = newGroup;
        }
    }

    private void updateInclusionsForEliminatedGroup_ParallelSnap(List<IncludeDesc> inclusions,
            LayoutInterval replacedGroup, LayoutInterval newGroup, LayoutInterval groupContent) {
        for (IncludeDesc iDesc : inclusions) {
            updateReplacedGroup_ParallelSnap(iDesc, replacedGroup, newGroup, groupContent);
        }
        if (originalPosition != null) {
            if (originalInclusion1 != null) {
                updateReplacedGroup_ParallelSnap(originalInclusion1, replacedGroup, newGroup, groupContent);
            }
            if (originalInclusion2 != null) {
                updateReplacedGroup_ParallelSnap(originalInclusion2, replacedGroup, newGroup, groupContent);
            }
        }
    }
    private void updateReplacedGroup_ParallelSnap(IncludeDesc iDesc,
            LayoutInterval replacedGroup, LayoutInterval newGroup, LayoutInterval groupContent) {
        if (iDesc.snappedParallel == replacedGroup) {
            if (groupContent != null && (groupContent.isComponent() || groupContent.isParallel())) {
                iDesc.snappedParallel = groupContent;
            } else {
                iDesc.snappedParallel = newGroup != null && newGroup.isParallel() ? newGroup : null;
            }
        }
    }

    private void updateReplacedOriginalGroup(LayoutInterval newGroup, LayoutInterval newSeq, int index) {
        updateReplacedOriginalGroup(originalInclusion1, newGroup, newSeq, index);
        updateReplacedOriginalGroup(originalInclusion2, newGroup, newSeq, index);
    }

    private static void updateReplacedOriginalGroup(IncludeDesc iDesc, LayoutInterval newGroup, LayoutInterval newSeq, int index) {
        if (iDesc != null && LayoutInterval.getRoot(newGroup) != LayoutInterval.getRoot(iDesc.parent)) {
            if (iDesc.parent.isParallel()) {
                iDesc.parent = newGroup;
            } else if (newSeq != null) {
                iDesc.parent = newSeq;
                iDesc.index = index;
            }
        }
    }

    private static int[] collectNeighborPositions(List<IncludeDesc> inclusions, IncludeDesc exclude,
                                                  LayoutRegion space, int dimension) {
        int minPos = Integer.MIN_VALUE;
        int maxPos = Integer.MAX_VALUE;
        LayoutInterval[] neighbors = new LayoutInterval[2];
        for (Iterator<IncludeDesc> it=inclusions.iterator(); it.hasNext(); ) {
            IncludeDesc iDesc = it.next();
            LayoutInterval parent = iDesc.parent;
            if (iDesc != exclude) {
                neighbors[LEADING] = null;
                neighbors[TRAILING] = null;
                if (parent.isParallel()) {
                    if (iDesc.neighbor != null) {
                        LayoutRegion nSpace = iDesc.neighbor.getCurrentSpace();
                        if (nSpace.isSet(dimension)) {
                            int dir = getAddDirection(space, nSpace, dimension, CENTER);
                            neighbors[dir^1] = iDesc.neighbor;
                        }
                    }
                } else if (!iDesc.newSubGroup) {
                    int i = iDesc.index;
                    if (i < 0) {
                        i = parent.getSubIntervalCount() - 1;
                    }
                    if (i >= 0 && i < parent.getSubIntervalCount()) {
                        LayoutInterval neighbor = parent.getSubInterval(i);
                        if (!neighbor.isEmptySpace()) {
                            neighbors[TRAILING] = neighbor;
                            i--; // now possibly on empty space
                        }
                    }
                    if (i > 0) {
                        LayoutInterval neighbor = parent.getSubInterval(i-1);
                        if (!neighbor.isEmptySpace()) {
                            neighbors[LEADING] = neighbor;
                        }
                    }
                    if (neighbors[TRAILING] == null && i+1 < parent.getSubIntervalCount()) {
                        LayoutInterval neighbor = parent.getSubInterval(i+1);
                        if (!neighbor.isEmptySpace()) {
                            neighbors[TRAILING] = neighbor;
                        }
                    }
                } else {
                    for (int i=0; i < parent.getSubIntervalCount(); i++) {
                        LayoutInterval li = parent.getSubInterval(i);
                        if (li.isEmptySpace()) {
                            continue;
                        }
                        LayoutRegion subSpace = li.getCurrentSpace();
                        if (LayoutRegion.overlap(space, subSpace, dimension^1, 0)) {
                            if (subSpace.positions[dimension][TRAILING] <= space.positions[dimension][LEADING]) {
                                neighbors[LEADING] = li;
                            } else if (subSpace.positions[dimension][LEADING] >= space.positions[dimension][TRAILING]) {
                                neighbors[TRAILING] = li;
                                break;
                            }
                        }
                    }
                }
                if (neighbors[LEADING] != null) {
                    int pos = neighbors[LEADING].getCurrentSpace().positions[dimension][TRAILING];
                    if (pos != LayoutRegion.UNKNOWN && pos > minPos) {
                        minPos = pos;
                    }
                }
                if (neighbors[TRAILING] != null) {
                    int pos = neighbors[TRAILING].getCurrentSpace().positions[dimension][LEADING];
                    if (pos != LayoutRegion.UNKNOWN && pos < maxPos) {
                        maxPos = pos;
                    }
                }
            }
        }
        if (maxPos == Integer.MAX_VALUE) {
            maxPos = LayoutRegion.UNKNOWN;
        }
        if (minPos == Integer.MIN_VALUE) {
            minPos = LayoutRegion.UNKNOWN;
        }
        return new int[] { minPos, maxPos };
    }

    /**
     * @return 1 if only iDesc1 can be used,
     *         2 if only iDesc2 can be used,
     *         3 if both incusions were merged successfully
     */
    private int mergeSequentialInclusions(IncludeDesc iDesc1, IncludeDesc iDesc2) {
        if (iDesc2 == null || !canCombine(iDesc1, iDesc2)) {
            return 1;
        }
        assert (iDesc1.alignment == LEADING || iDesc1.alignment == TRAILING)
                && (iDesc2.alignment == LEADING || iDesc2.alignment == TRAILING)
                && iDesc1.alignment == (iDesc2.alignment^1);

        boolean orig1 = iDesc1 == originalInclusion1;
        if (iDesc1.parent == iDesc2.parent) {
            if (orig1) {
                iDesc1.newSubGroup = iDesc2.newSubGroup;
                iDesc1.neighbor = iDesc2.neighbor;
            }
            return 3;
        }

        LayoutInterval commonGroup;
        boolean nextTo;
        if (iDesc1.parent.isParentOf(iDesc2.parent)) {
            commonGroup = iDesc1.parent;
            nextTo = iDesc1.neighbor != null || iDesc2.snappedNextTo != null || iDesc2.parent.isSequential();
        } else if (iDesc2.parent.isParentOf(iDesc1.parent)) {
            if (dragger.isResizing(dimension) && releaseFromSubGroup(iDesc2, iDesc1)) {
                return 2;
            }
            commonGroup = iDesc2.parent;
            nextTo = iDesc2.neighbor != null || iDesc1.snappedNextTo != null || iDesc1.parent.isSequential();
            if (orig1 && iDesc1.neighbor != null && !LayoutUtils.contentOverlap(addingSpace, iDesc1.neighbor, dimension^1)) {
                iDesc1.neighbor = null; // original neighbor is not intersecting in other dimension
            }
        } else {
            commonGroup = LayoutInterval.getFirstParent(iDesc1.parent, SEQUENTIAL);
            nextTo = false;
        }

        if (commonGroup.isSequential() || nextTo) {
            // inclusions in common sequence or the upper inclusion has the lower as neighbor
            if (iDesc1.alignment == TRAILING) {
                IncludeDesc temp = iDesc1;
                iDesc1 = iDesc2;
                iDesc2 = temp;
            } // so iDesc1 is leading and iDesc2 trailing
            mergeInclusionsInCommonSequence(iDesc1, iDesc2, commonGroup);
        } else { // common group is parallel - there is nothing in sequence, so nothing to extract
            assert iDesc1.parent.isParallel() && iDesc2.parent.isParallel()
                   && (commonGroup == iDesc1.parent || commonGroup == iDesc2.parent)
                   && iDesc1.neighbor == null && iDesc2.neighbor == null;

            if (iDesc1.snappedParallel != null && iDesc2.snappedParallel != null
                    && commonGroup.isParentOf(iDesc1.snappedParallel) && commonGroup.isParentOf(iDesc2.snappedParallel)) {
                // if aligning on both sides it could be placed in first common parent
                LayoutInterval parParent = iDesc1.snappedParallel;
                if (!parParent.isParentOf(iDesc2.snappedParallel)) {
                    parParent = LayoutInterval.getFirstParent(iDesc1.snappedParallel, PARALLEL);
                    if (!parParent.isParentOf(iDesc2.snappedParallel)) {
                        parParent = iDesc2.snappedParallel;
                        if (!parParent.isParentOf(iDesc1.snappedParallel)) {
                            parParent = LayoutInterval.getFirstParent(iDesc2.snappedParallel, PARALLEL);
                            if (!parParent.isParentOf(iDesc1.snappedParallel)) {
                                parParent = null;
                            }
                        }
                    }
                }
                if (parParent != null && commonGroup.isParentOf(parParent)
                        && canAlignWith(iDesc1.snappedParallel, parParent, iDesc1.alignment)
                        && canAlignWith(iDesc2.snappedParallel, parParent, iDesc2.alignment)) {
                    iDesc1.parent = parParent;
                    iDesc2.parent = parParent;
                    return 3;
                }
            }

            if ((iDesc2.snappedNextTo == null && iDesc2.snappedParallel == null)
                || (iDesc2.snappedParallel != null && canAlignWith(iDesc2.snappedParallel, iDesc1.parent, iDesc2.alignment)))
            {   // iDesc2 can adapt to iDesc1
                iDesc2.parent = iDesc1.parent;
                return 3;
            }

            if (iDesc2.parent == commonGroup) {
                IncludeDesc temp = iDesc1;
                iDesc1 = iDesc2;
                iDesc2 = temp;
            } // so iDesc1 is super-group and iDesc2 subgroup
            assert iDesc2.snappedNextTo == null;

            if (iDesc2.snappedParallel == iDesc2.parent) {
                iDesc2.parent = LayoutInterval.getFirstParent(iDesc2.parent, PARALLEL);
                if (iDesc2.parent == iDesc1.parent)
                    return 3;
            }

            if (iDesc2.snappedParallel == null || canAlignWith(iDesc2.snappedParallel, iDesc1.parent, iDesc2.alignment)) {
                // subgroup is either not snapped at all, or can align also in parent group
                iDesc2.parent = iDesc1.parent;
                return 3;
            }

            if (LayoutInterval.isAlignedAtBorder(iDesc2.parent, iDesc1.parent, iDesc1.alignment)) {
                iDesc1.parent = iDesc2.parent;
                return 3; // subgroup is aligned to parent group edge
            }

            LayoutInterval seq = iDesc2.parent.getParent();
            if (seq.isSequential() && seq.getParent() == iDesc1.parent) {
                int index = seq.indexOf(iDesc2.parent) + (iDesc1.alignment == LEADING ? -1 : 1);
                LayoutInterval gap = (index == 0 || index == seq.getSubIntervalCount()-1) ?
                                     seq.getSubInterval(index) : null;
                if (gap != null
                    && LayoutInterval.isFixedDefaultPadding(gap)
                    && iDesc1.snappedNextTo == iDesc1.parent
                    && LayoutInterval.wantResize(seq))
                {   // subgroup is at preferred gap from parent - corresponds to parent's snappedNextTo
                    iDesc1.parent = iDesc2.parent;
                    iDesc1.snappedNextTo = null;
                    iDesc1.snappedParallel = iDesc2.parent;
                    return 3;
                }

                if (gap != null && gap.isEmptySpace() && iDesc1.snappedParallel == iDesc1.parent) {
                    // need to make the subgroup aligned to parent group
                    int gapSize = LayoutInterval.getCurrentSize(gap, dimension);
                    copyGapInsideGroup(gap, gapSize, iDesc2.parent, iDesc1.alignment);
                    layoutModel.removeInterval(gap);
                    iDesc1.parent = iDesc2.parent;
                    return 3;
                }
            }

            iDesc2.parent = iDesc1.parent; // prefer super-group otherwise
        }

        return 3;
    }

    /**
     * Detects S-layout situation that should be solved by breaking the
     * interval's original alignment inside sub-group (in favor of keeping
     * intervals aligned at sub-group edge).
     */
    private boolean releaseFromSubGroup(IncludeDesc iDesc1, IncludeDesc iDesc2) {
        if (iDesc2.parent.isParentOf(iDesc1.parent)) {
            IncludeDesc temp = iDesc1;
            iDesc1 = iDesc2;
            iDesc2 = temp;
        } else if (!iDesc1.parent.isParentOf(iDesc2.parent)) {
            return false;
        }
        LayoutInterval outP = iDesc1.parent;
        LayoutInterval subP = iDesc2.parent;
        LayoutInterval outSnap = iDesc1.snappedParallel;
        LayoutInterval subSnap = iDesc2.snappedParallel;
        if ((outSnap == null || (subP != outSnap && !subP.isParentOf(outSnap)))
                && (subSnap == null || !canAlignWith(subSnap, outP, iDesc2.alignment))) {
            boolean tiedInSubParent;
            if (outP.isSequential()) {
                LayoutInterval parentAsNeighbor = outP.getSubInterval(LayoutInterval.getIndexInParent(subP, outP));
                tiedInSubParent = LayoutUtils.contentOverlap(addingSpace, parentAsNeighbor, dimension^1);
            } else {
                tiedInSubParent = false;
            }
            if (!tiedInSubParent) {
                boolean significantSubEdge = false;
                LayoutInterval p = subP;
                do {
                    if (p.isParallel() && LayoutUtils.anythingAtGroupEdge(p, null, dimension, iDesc1.alignment)) {
                        significantSubEdge = true;
                        break;
                    }
                    p = p.getParent();
                } while (p != outP);
                if (significantSubEdge) {
                    return true;
                }
            }
        }
        return false;
    }

    private void mergeInclusionsInCommonSequence(IncludeDesc iDesc1, IncludeDesc iDesc2, LayoutInterval commonGroup) {
        boolean more;
        do {
            more = false;
            int startIndex = 0;
            LayoutInterval ext1 = null;
            int depth1 = 0;
            boolean startGap = false;
            int endIndex = 0;
            LayoutInterval ext2 = null;
            int depth2 = 0;
            boolean endGap = false;
            boolean goingParallel = false;

            if (commonGroup.isSequential()) {
                if (commonGroup.isParentOf(iDesc1.parent)) {
                    startIndex = LayoutInterval.getIndexInParent(iDesc1.parent, commonGroup);
                    depth1 = 1;
                    ext1 = intervalToExtractIntoCommonSequence(iDesc1, commonGroup);
                    if (ext1 != null) {
                        int d = LayoutInterval.getDepthInParent(iDesc1.parent, ext1);
                        if (d > 0) {
                            depth1 += d;
                        }
                    }
                    if (dragger.isResizing(dimension)) {
                        LayoutInterval inSequence = (iDesc1.parent.isSequential() && !iDesc1.newSubGroup)
                                ? iDesc1.parent : iDesc1.neighbor;
                        if (inSequence != null && LayoutUtils.contentOverlap(addingSpace, inSequence, dimension)) {
                            goingParallel = true; // resizing in parallel with original sequence
                        }
                    }
                } else {
                    startIndex = iDesc1.index;
                    if (iDesc1.snappedParallel != null && commonGroup.isParentOf(iDesc1.snappedParallel)
                            && iDesc1.newSubGroup && dragger.isResizing(dimension)) {
                        startIndex = LayoutInterval.getIndexInParent(iDesc1.snappedParallel, commonGroup);
                    } else {
                        if (startIndex == commonGroup.getSubIntervalCount()) {
                            startIndex--;
                        }
                        startGap = commonGroup.getSubInterval(startIndex).isEmptySpace();
                    }
                }

                if (commonGroup.isParentOf(iDesc2.parent)) {
                    endIndex = LayoutInterval.getIndexInParent(iDesc2.parent, commonGroup);
                    depth2 = 1;
                    ext2 = intervalToExtractIntoCommonSequence(iDesc2, commonGroup);
                    if (ext2 != null) {
                        int d = LayoutInterval.getDepthInParent(iDesc2.parent, ext2);
                        if (d > 0) {
                            depth2 += d;
                        }
                    }
                    if (dragger.isResizing(dimension)) {
                        LayoutInterval inSequence = (iDesc2.parent.isSequential() && !iDesc2.newSubGroup)
                                ? iDesc2.parent : iDesc2.neighbor;
                        if (inSequence != null && LayoutUtils.contentOverlap(addingSpace, inSequence, dimension)) {
                            goingParallel = true; // resizing in parallel with original sequence
                        }
                    }
                } else {
                    endIndex = iDesc2.index;
                    if (iDesc2.snappedParallel != null && commonGroup.isParentOf(iDesc2.snappedParallel)) {
                        if (iDesc2.newSubGroup && dragger.isResizing(dimension)) {
                            endIndex = LayoutInterval.getIndexInParent(iDesc2.snappedParallel, commonGroup);
                        }
                        if (endIndex == commonGroup.getSubIntervalCount()) {
                            endIndex--;
                        }
                    } else if (endIndex > 0) {
                        endGap = commonGroup.getSubInterval(--endIndex).isEmptySpace();
                    }
                }
            }

            boolean validSection = (endIndex > startIndex + 1 || (endIndex == startIndex+1
                    && (!startGap || iDesc1.snappedParallel != null) && (!endGap || iDesc2.snappedParallel != null)));
            if (validSection && (ext1 != null || ext2 != null)) {
                // there is a significant part of the common sequence to be parallelized
                LayoutInterval extSeq = new LayoutInterval(SEQUENTIAL);
                LayoutInterval startInt = commonGroup.getSubInterval(startIndex);
                LayoutInterval endInt = commonGroup.getSubInterval(endIndex);
                int posL = LayoutUtils.getVisualPosition(startInt, dimension, LEADING);
                int posT = LayoutUtils.getVisualPosition(endInt, dimension, TRAILING);

                // check visual overlap of the extracted intervals with the part of the sequence
                LayoutInterval parConnectingGap = null;
                LayoutInterval extConnectingGap = null;
                // temporarily remove ext1 and ext2 for analysis
                LayoutInterval parent1, parent2;
                int idx1, idx2;
                if (ext1 != null) {
                    parent1 = ext1.getParent();
                    idx1 = parent1.remove(ext1);
                } else {
                    parent1 = null; idx1 = -1;
                }
                if (ext2 != null) {
                    parent2 = ext2.getParent();
                    idx2 = parent2.remove(ext2);
                } else {
                    parent2 = null; idx2 = -1;
                }
                // Check if can extract ext1 and ext2 in parallel with whole sub-sequence
                // from startIndex to endIndex, or just with the first/last interval at
                // startIndex or endIndex (due to orthogonal overlap of ext1/ext2 with
                // something from the sequence).
                if (ext1 != null
                        && !LayoutInterval.isClosedGroup(startInt, TRAILING)
                        && LayoutUtils.contentOverlap(ext1, commonGroup, startIndex+1, endIndex, dimension^1)
                        && !LayoutUtils.contentOverlap(startInt, commonGroup, startIndex+1, endIndex, dimension^1)) {
                    while (startIndex < endIndex) {
                        layoutModel.addInterval(layoutModel.removeInterval(commonGroup, startIndex+1), extSeq, -1);
                        endIndex--;
                    }
                    if (extSeq.getSubIntervalCount() > 0) {
                        LayoutInterval li = extSeq.getSubInterval(extSeq.getSubIntervalCount()-1);
                        if (li.isEmptySpace()) { // cut everything after startInt, so at least clone the last gap
                            parConnectingGap = LayoutInterval.cloneInterval(li, null);
                        }
                        li = extSeq.getSubInterval(0);
                        if (li.isEmptySpace()) {
                            extConnectingGap = li;
                        }
                    }
                    if (ext2 != null) {
                        parent2.add(ext2, idx2);
                        ext2 = null; // don't extract ext2 in this round (its parent just moved to extSeq)
                        if (depth2 == 1) {
                            depth2 = 2; // don't adjust iDesc2 and do one more round
                        }
                    }
                } else if (ext2 != null
                           && !LayoutInterval.isClosedGroup(endInt, LEADING)
                           && LayoutUtils.contentOverlap(ext2, commonGroup, startIndex, endIndex-1, dimension^1)
                           && !LayoutUtils.contentOverlap(endInt, commonGroup, startIndex, endIndex-1, dimension^1)) {
                    while (startIndex < endIndex) {
                        layoutModel.addInterval(layoutModel.removeInterval(commonGroup, startIndex), extSeq, -1);
                        endIndex--;
                    }
                    if (extSeq.getSubIntervalCount() > 0) {
                        LayoutInterval li = extSeq.getSubInterval(0);
                        if (li.isEmptySpace()) { // cut everything before endInt, so at least clone the first gap
                            parConnectingGap = LayoutInterval.cloneInterval(li, null);
                        }
                        li = extSeq.getSubInterval(extSeq.getSubIntervalCount()-1);
                        if (li.isEmptySpace()) {
                            extConnectingGap = li;
                        }
                    }
                    if (ext1 != null) {
                        parent1.add(ext1, idx1);
                        ext1 = null; // don't extract ext1 in this round (its parent just moved to extSeq)
                        if (depth1 == 1) {
                            depth1 = 2; // don't adjust iDesc1 and do one more round
                        }
                    }
                }
                // return back ext1 and ext2
                if (ext1 != null) {
                    parent1.add(ext1, idx1);
                }
                if (ext2 != null) {
                    parent2.add(ext2, idx2);
                }

                LayoutInterval parGroup;
                if (startIndex == 0 && endIndex == commonGroup.getSubIntervalCount()-1) {
                    // parallel with whole sequence
                    parGroup = commonGroup.getParent();
                } else { // separate part of the original sequence
                    parGroup = new LayoutInterval(PARALLEL);
                    LayoutInterval parSeq = new LayoutInterval(SEQUENTIAL);
                    parGroup.add(parSeq, 0);
                    parGroup.getCurrentSpace().set(dimension, posL, posT);
                    while (startIndex <= endIndex) {
                        layoutModel.addInterval(layoutModel.removeInterval(commonGroup, startIndex), parSeq, -1);
                        endIndex--;
                    }
                    layoutModel.addInterval(parGroup, commonGroup, startIndex);
                }
                layoutModel.addInterval(extSeq, parGroup, -1);
                if (ext1 != null) {
                    LayoutInterval parent = ext1.getParent();
                    layoutModel.removeInterval(ext1);
                    if (LayoutInterval.hasAnyResizingNeighbor(parent, TRAILING)
                            && LayoutInterval.getCount(parent, TRAILING, true) > 0
                            && !LayoutInterval.wantResize(parent)) {
                        operations.maintainSize(parent, LayoutInterval.wantResize(ext1), dimension, false);
                    }
                    if (parent.getSubIntervalCount() == 1) {
                        LayoutInterval last = layoutModel.removeInterval(parent, 0);
                        operations.addContent(last, parent.getParent(), layoutModel.removeInterval(parent), dimension);
                        if (parent == startInt) {
                            startInt = last;
                        }
                    }
                    int beforeCount = extSeq.getSubIntervalCount();
                    operations.addContent(ext1, extSeq, 0, dimension);
                    if (depth1 == 1 && !iDesc1.parent.isSequential()) {
                        iDesc1.index = extSeq.getSubIntervalCount() - beforeCount;
                    }
                    if (depth2 <= 1) {
                        if (ext2 == null || !iDesc2.parent.isSequential()) {
                            iDesc2.index = extSeq.getSubIntervalCount();
                        } else {
                            iDesc2.index += extSeq.getSubIntervalCount();
                        }
                    }
                    if (ext2 != null) {
                        LayoutInterval gap = new LayoutInterval(SINGLE);
                        int size = LayoutRegion.distance(ext1.getCurrentSpace(), ext2.getCurrentSpace(), dimension, LEADING, TRAILING);
                        gap.setSize(size);
                        layoutModel.addInterval(gap, extSeq, -1);
                    } else { // could have moved things next to startInt to extSeq
                        if (parConnectingGap != null) {
                            parent = startInt.getParent();
                            if (!parent.isSequential()) {
                                LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
                                layoutModel.addInterval(seq, parent, layoutModel.removeInterval(startInt));
                                layoutModel.addInterval(startInt, seq, 0);
                                parent = seq;
                            }
                            assert parent.indexOf(startInt) == 0;
                            layoutModel.addInterval(parConnectingGap, parent, 1);
                        }
                        if (extConnectingGap != null) {
                            operations.accommodateGap(extConnectingGap, dimension);
                            if (LayoutInterval.wantResize(startInt) && !LayoutInterval.wantResize(extSeq)) {
                                operations.setIntervalResizing(extConnectingGap, true);
                            }
                        }
                    }
                } else {
                    iDesc1.index = 0;
                    if (depth2 <= 1 && !iDesc2.parent.isSequential()) {
                        iDesc2.index = extSeq.getSubIntervalCount();
                    }
                }
                if (ext2 != null) {
                    LayoutInterval parent = ext2.getParent();
                    if (ext2.getAlignment() == TRAILING) {
                        extSeq.setAlignment(TRAILING);
                    }
                    layoutModel.removeInterval(ext2);
                    if (LayoutInterval.hasAnyResizingNeighbor(parent, LEADING)
                            && LayoutInterval.getCount(parent, LEADING, true) > 0
                            && !LayoutInterval.wantResize(parent)) {
                        operations.maintainSize(parent, LayoutInterval.wantResize(ext2), dimension, false);
                    }
                    if (parent.getSubIntervalCount() == 1) {
                        LayoutInterval last = layoutModel.removeInterval(parent, 0);
                        operations.addContent(last, parent.getParent(), layoutModel.removeInterval(parent), dimension);
                        if (parent == endInt) {
                            endInt = last;
                        }
                    }
                    operations.addContent(ext2, extSeq, -1, dimension);
                    if (ext1 == null) { // could have moved things next to endInt to extSeq
                        if (parConnectingGap != null) {
                            parent = endInt.getParent();
                            if (!parent.isSequential()) {
                                LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
                                layoutModel.addInterval(seq, parent, layoutModel.removeInterval(endInt));
                                layoutModel.addInterval(endInt, seq, 0);
                                parent = seq;
                            }
                            assert parent.indexOf(endInt) == 0;
                            layoutModel.addInterval(parConnectingGap, parent, 0);
                        }
                        if (extConnectingGap != null) {
                            operations.accommodateGap(extConnectingGap, dimension);
                            if (LayoutInterval.wantResize(endInt) && !LayoutInterval.wantResize(extSeq)) {
                                operations.setIntervalResizing(extConnectingGap, true);
                            }
                        }
                    }
                }

                if (depth1 <= 1) {
                    boolean newSubGroupBeforeMerge = iDesc1.parent == commonGroup && iDesc1.newSubGroup;
                    iDesc1.parent = extSeq;
                    if (iDesc2.newSubGroup) {
                        iDesc1.newSubGroup = true;
                    } else if (newSubGroupBeforeMerge) {
                        iDesc1.newSubGroup = false; // actually just created the sub-group
                    }
                    iDesc1.neighbor = null;
                }
                if (depth2 <= 1) {
                    iDesc2.parent = extSeq;
                    if (iDesc1.newSubGroup) {
                        iDesc2.newSubGroup = true;
                    }
                    iDesc2.neighbor = null;
                }
                commonGroup = extSeq;
                more = depth1 > 1 || depth2 > 1;
                optimizeStructure = true;
            } else if (ext1 == null && ext2 == null && validSection) {
                // nothing to extract, but the resizing interval still to be in
                // parallel with part of the sequence
                if (commonGroup.isParentOf(iDesc1.parent)) {
                    iDesc1.index = startIndex;
                }
                if (commonGroup.isParentOf(iDesc2.parent)) {
                    iDesc2.index = endIndex;
                }
                iDesc1.parent = iDesc2.parent = commonGroup;
                iDesc1.newSubGroup = iDesc2.newSubGroup = true;
                iDesc1.neighbor = iDesc2.neighbor = null;
            } else {
                // prefer sub-group in case of end position, outer group in case
                // of resizing in parallel with sub-group
                boolean p1 = iDesc1.parent.isParentOf(iDesc2.parent);
                boolean p2 = iDesc2.parent.isParentOf(iDesc1.parent);
                if ((p2 && !goingParallel) || (p1 && goingParallel)) {
                    iDesc2.parent = iDesc1.parent;
                    iDesc2.index = iDesc1.index;
                    iDesc2.newSubGroup = iDesc1.newSubGroup;
                    iDesc2.neighbor = iDesc1.neighbor;
                    if (endGap) // there's an outer gap
                        iDesc2.fixedPosition = false;
                } else if ((p1 && !goingParallel) || (p2 && goingParallel)) {
                    iDesc1.parent = iDesc2.parent;
                    iDesc1.index = iDesc2.index;
                    iDesc1.newSubGroup = iDesc2.newSubGroup;
                    iDesc1.neighbor = iDesc2.neighbor;
                    if (startGap) // there's an outer gap
                        iDesc1.fixedPosition = false;
                }
            }
        } while (more);

        // might originally be snapped to a group that has just been optimized out
        // [TODO better would be to subst. it with a representative component]
        if (iDesc1.snappedParallel != null && iDesc1.snappedParallel.isParallel() && iDesc1.snappedParallel.getSubIntervalCount() == 0) {
            iDesc1.snappedParallel = null;
        }
        if (iDesc2.snappedParallel != null && iDesc2.snappedParallel.isParallel() && iDesc2.snappedParallel.getSubIntervalCount() == 0) {
            iDesc2.snappedParallel = null;
        }
    }

    private static LayoutInterval intervalToExtractIntoCommonSequence(IncludeDesc iDesc, LayoutInterval commonSeq) {
        assert commonSeq.isSequential() && commonSeq.isParentOf(iDesc.parent);
        LayoutInterval ext = null;
        LayoutInterval interval = iDesc.parent.isParallel() && iDesc.neighbor != null
                ? iDesc.neighbor : iDesc.parent;
        boolean aligned = iDesc.parent.isParallel() && iDesc.neighbor == null;
        while (interval.getParent() != commonSeq) {
            if (aligned && !LayoutInterval.isAlignedAtBorder(interval, iDesc.alignment)) {
                aligned = false;
            }
            ext = interval;
            interval = interval.getParent();
        }
        if (aligned) {
            ext = null;
        }
        return ext;
    }

    /**
     * Moves a gap next to a parallel group into the parallel group - i.e. each
     * interval in the group gets extended by the gap. Sort of opposite to
     * LayoutOperations.optimizeGaps.
     * @param alignment which side of the group is extended
     */
    private void copyGapInsideGroup(LayoutInterval gap, int gapSize, LayoutInterval group, int alignment) {
        assert gap.isEmptySpace() && (alignment == LEADING || alignment == TRAILING);

        if (alignment == LEADING)
            gapSize = -gapSize;

        group.getCurrentSpace().positions[dimension][alignment] += gapSize;

        List<LayoutInterval> originalGroup = new ArrayList<LayoutInterval>(group.getSubIntervalCount());
        for (Iterator<LayoutInterval> it=group.getSubIntervals(); it.hasNext(); ) {
            originalGroup.add(it.next());
        }

        for (LayoutInterval sub : originalGroup) {
            LayoutInterval gapClone = LayoutInterval.cloneInterval(gap, null);
            if (sub.isSequential()) {
                sub.getCurrentSpace().positions[dimension][alignment] += gapSize;
                int index = alignment == LEADING ? 0 : sub.getSubIntervalCount();
                operations.insertGapIntoSequence(gapClone, sub, index, dimension);
            }
            else {
                LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
                seq.getCurrentSpace().set(dimension, sub.getCurrentSpace());
                seq.getCurrentSpace().positions[dimension][alignment] += gapSize;
                seq.setAlignment(sub.getRawAlignment());
                layoutModel.addInterval(seq, group, layoutModel.removeInterval(sub));
                layoutModel.setIntervalAlignment(sub, DEFAULT);
                layoutModel.addInterval(sub, seq, 0);
                layoutModel.addInterval(gapClone, seq, alignment == LEADING ? 0 : 1);
            }
        }
    }

    private boolean shouldEnterGroup(LayoutInterval group) {
        assert group.isParallel();
        if (positionToEnterGroup(group)) {
            if (aSnappedParallel != null || aSnappedNextTo != null || aEdge == DEFAULT) {
                return true;
            }
            // Otherwise secondary edge that does not snap. Makes sense if primary
            // edge snapped in sequence and secondary aims into neighbor parallel
            // group - should it enter or not?
            if (!stickingOutOfGroup(group, aEdge^1) || groupOpenToEnter(group, aEdge^1)) {
                return true;
            }
        }
        return false;
    }

    private boolean positionToEnterGroup(LayoutInterval group) {
        if (group.getGroupAlignment() == BASELINE && aSnappedParallel == null) {
            return false;
        }

        int alignment = aEdge != DEFAULT ? aEdge : CENTER;
        LayoutRegion groupSpace = group.getCurrentSpace();
        if (LayoutRegion.pointInside(addingSpace, alignment, groupSpace, dimension)) {
            if (aEdge == DEFAULT) {
                // for easier inserting between groups we consider 10 pixels
                // border as not yet in the group
                if (!LayoutRegion.pointInside(addingSpace, LEADING, groupSpace, dimension)
                        || !LayoutRegion.pointInside(addingSpace, TRAILING, groupSpace, dimension)) {
                    // not entirely within the group
                    int[] apos = addingSpace.positions[dimension];
                    int[] gpos = groupSpace.positions[dimension];
                    if (getAddDirection(addingSpace, groupSpace, dimension, CENTER) == LEADING) {
                        if (LayoutInterval.isClosedGroup(group, LEADING)) {
                            int dCL = apos[CENTER] - gpos[LEADING];
                            int dCC = gpos[CENTER] - apos[CENTER];
                            if (dCL < 10 && dCL < dCC) {
                                return false; // out of the group
                            }
                        }
                    } else {
                        if (LayoutInterval.isClosedGroup(group, TRAILING)) {
                            int dCT = gpos[TRAILING] - apos[CENTER];
                            int dCC = apos[CENTER] - gpos[CENTER];
                            if (dCT < 10 && dCT < dCC) {
                                return false; // out of the group
                            }
                        }
                    }
                }
                return true;
            }
            if (aSnappedParallel == null || group == aSnappedParallel
                    || group.isParentOf(aSnappedParallel)
                    || LayoutUtils.contentOverlap(addingSpace, group, dimension^1)) {
                return true;
            }
            if (alignment == LEADING || alignment == TRAILING) {
                // Determine if within or under 'group' one might align in parallel
                // with required 'aSnappedParallel' interval that is out of the group.
                LayoutInterval interval = aSnappedParallel;
                LayoutInterval parent = LayoutInterval.getFirstParent(interval, PARALLEL);
                while (parent != null && LayoutInterval.isAlignedAtBorder(interval, parent, alignment)) {
                    if (parent.isParentOf(group) && LayoutInterval.isAlignedAtBorder(group, parent, alignment)) {
                        return true;
                    }
                    interval = parent;
                    parent = LayoutInterval.getFirstParent(interval, PARALLEL);
                }
            }
        }
        return false;
    }

    private boolean groupOpenToEnter(LayoutInterval group, int alignment) {
        if (group.getGroupAlignment() == BASELINE) {
            return false;
        }
        boolean placedOver = addingOverGroupEdge(group, alignment);
        if (!LayoutInterval.isClosedGroup(group, alignment)
            || (placedOver
                && hasOpenRoomForAdding(group, alignment)
                && !isSignificantGroupEdge(group, alignment, true))) {
            // also check if not ort. overlapping with everything
            boolean nextToEverything = true;
            for (Iterator<LayoutInterval> it=group.getSubIntervals(); it.hasNext(); ) {
                LayoutInterval li = it.next();
                if (!LayoutUtils.contentOverlap(addingSpace, li, dimension^1)
                        || !considerSequentialPosition(li, true)) {
                    nextToEverything = false;
                    break;
                }
            }
            if (!nextToEverything) {
                if (placedOver) {
                    return true;
                } else {
                    LayoutDragger.PositionDef primaryPos = newPositions[dimension];
                    if (primaryPos == null || !primaryPos.snapped // there is no snap
                            || primaryPos.alignment == aEdge // or this is the primary position
                            || alignment != (primaryPos.alignment^1)) { // secondary position on the other side than primary
                        LayoutInterval significantNeighbor = LayoutInterval.getDirectNeighbor(group, alignment, true);
                        if (significantNeighbor == null
                                || !LayoutRegion.overlap(addingSpace, significantNeighbor.getCurrentSpace(), dimension, 0)) {
                            return true;
                        } // otherwise there already is something next to the group and we have
                          // no reason to try to enter it when our adding position is also next to
                    }
                }
            }
        }
        return false;
    }

    /**
     * @return whether being in 'group' (having it as first parallel parent)
     *         allows parallel align with 'interval'
     */
    private boolean canAlignWith(LayoutInterval interval, LayoutInterval group, int alignment) {
        if (group.isSequential())
            group = group.getParent();

        if (interval == group)
            return true; // can align to group border from inside

        LayoutInterval parent = interval.getParent();
        if (parent == null) {
            if (!interval.isParentOf(group)) {
                return false; // something's wrong, either 'interval' or 'group' got out of the hierarchy
            }
            parent = interval;
        } else if (parent.isSequential()) {
            parent = parent.getParent();
        }

        while (parent != null && parent != group && !parent.isParentOf(group)) {
            if (canSubstAlignWithParent(interval, dimension, alignment, dragger.isResizing(dimension))) {
                interval = parent;
                parent = LayoutInterval.getFirstParent(interval, PARALLEL);
            }
            else parent = null;
        }
        if (parent == null)
            return false;
        if (parent == group)
            return true;
        // otherwise parent.isParentOf(group)
        // we silently assume that addingInterval will end up aligned in 'group'
        if (LayoutInterval.isAlignedAtBorder(group, parent, alignment)) {
            return true;
        }
        if (LayoutInterval.isAlignedAtBorder(interval, parent, alignment)) {
            LayoutInterval neighbor = LayoutInterval.getNeighbor(group, alignment, false, true, false);
            if (neighbor == null || !parent.isParentOf(neighbor)) {
                return true;
            }
        }
        return false;
    }

    private static boolean canSubstAlignWithParent(LayoutInterval interval, int dimension, int alignment, boolean placedAtBorderEnough) {
        LayoutInterval parent = LayoutInterval.getFirstParent(interval, PARALLEL);
        boolean aligned = LayoutInterval.isAlignedAtBorder(interval, parent, alignment);
        if (!aligned
            && LayoutInterval.getDirectNeighbor(interval, alignment, false) == null
            && LayoutInterval.isPlacedAtBorder(interval, parent, dimension, alignment))
        {   // not aligned, but touching parallel group border
            aligned = placedAtBorderEnough
                      || LayoutInterval.getDirectNeighbor(parent, alignment, true) != null
                      || LayoutInterval.isClosedGroup(parent, alignment);
            if (!aligned) { // check if the group can be considered "closed" at alignment edge
                boolean allTouching = true;
                for (Iterator it=parent.getSubIntervals(); it.hasNext(); ) {
                    LayoutInterval li = (LayoutInterval) it.next();
                    if (li.getAlignment() == alignment || LayoutInterval.wantResize(li)) {
                        aligned = true;
                        break;
                    }
                    else if (allTouching && !LayoutInterval.isPlacedAtBorder(li, dimension, alignment)) {
                        allTouching = false;
                    }
                }
                if (allTouching)
                    aligned = true;
            }
        }
        return aligned;
    }

    private boolean canCombine(IncludeDesc iDesc1, IncludeDesc iDesc2) {
        if (iDesc1.parent == iDesc2.parent)
            return true;

        if (iDesc1.parent.isParentOf(iDesc2.parent))
            return isBorderInclusion(iDesc2);
        else if (iDesc2.parent.isParentOf(iDesc1.parent))
            return isBorderInclusion(iDesc1);
        else {
            LayoutInterval parParent1 = iDesc1.parent.isParallel() ? iDesc1.parent : iDesc1.parent.getParent();
            LayoutInterval parParent2 = iDesc2.parent.isParallel() ? iDesc2.parent : iDesc2.parent.getParent();
            return parParent1.getParent() == parParent2.getParent()
                   && isBorderInclusion(iDesc1)
                   && isBorderInclusion(iDesc2)
                   && LayoutInterval.getDirectNeighbor(parParent1, iDesc1.alignment^1, true) == parParent2;
        }
    }

    private boolean isBorderInclusion(IncludeDesc iDesc) {
        if (iDesc.alignment != LEADING && iDesc.alignment != TRAILING)
            return false;

        if (iDesc.parent.isSequential()) {
            int startIndex = iDesc.alignment == LEADING ? iDesc.index : 0;
            int endIndex;
            if (iDesc.alignment == LEADING) {
                endIndex = iDesc.parent.getSubIntervalCount() - 1;
            } else {
                endIndex = iDesc.index - 1;
                if (endIndex >= iDesc.parent.getSubIntervalCount()) {
                    // if comming from original position the original index might be too high
                    endIndex = iDesc.parent.getSubIntervalCount() - 1;
                }
            }
            return startIndex > endIndex
                   || !LayoutUtils.contentOverlap(addingSpace, iDesc.parent, startIndex, endIndex, dimension^1);
        } else if (iDesc.snappedParallel != null) {
            return iDesc.snappedParallel == iDesc.parent
                   || !iDesc.parent.isParentOf(iDesc.snappedParallel)
                   || LayoutInterval.isPlacedAtBorder(iDesc.snappedParallel, iDesc.parent, dimension, iDesc.alignment);
        } else {
            return iDesc.neighbor == null
                   || (iDesc.alignment == LEADING && iDesc.index >= 1)
                   || (iDesc.alignment == TRAILING && iDesc.index == 0);
        }
    }

    private int getAddDirection(LayoutInterval interval, int alignment) {
        LayoutRegion space = interval.getCurrentSpace();
        if (dragger.isResizing(dimension)) {
            int fixedEdge = dragger.getResizingEdge(dimension) ^ 1;
            int dst = LayoutRegion.distance(addingSpace, space, dimension, fixedEdge, fixedEdge);
            if (dst == 0) {
                return fixedEdge;
            } else {
                return dst > 0 ? LEADING : TRAILING;
            }
        } else {
            return getAddDirection(addingSpace, space, dimension, alignment);
        }
    }

    private static int getAddDirection(LayoutRegion adding, LayoutRegion existing,
                                       int dimension, int alignment) {
        return LayoutRegion.distance(adding, existing, dimension, alignment, CENTER) > 0 ?
               LEADING : TRAILING;
    }
}
