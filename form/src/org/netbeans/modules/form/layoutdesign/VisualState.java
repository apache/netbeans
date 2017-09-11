/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.form.layoutdesign;

import java.awt.*;
import java.awt.geom.Area;
import java.util.*;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * This class computes various data from the actual visual state of the real
 * layout constructed from the model. Based on that it also does some
 * adjustments and optimizations in the model (size definitions).
 *
 * @author Tomas Pavek
 */
public class VisualState implements LayoutConstants {

    private LayoutModel layoutModel;
    private VisualMapper visualMapper;

    // component -> gaps around the component
    private Map<LayoutComponent, List<GapInfo>> componentGaps = new HashMap<LayoutComponent, List<GapInfo>>();
    private Map<LayoutComponent, List<GapInfo>> compGapsToUpdate;
    // component -> gaps inside the component (that is a container)
    private Map<LayoutComponent, List<GapInfo>> containerGaps = new HashMap<LayoutComponent, List<GapInfo>>();
    private Map<LayoutComponent, List<GapInfo>> contGapsToUpdate;

    private Map<LayoutComponent, Shape> clippingCache = new HashMap<LayoutComponent, Shape>();

    private static final int PROXIMITY = 32;

    private static String[] PADDING_DISPLAY_NAMES;

    static class GapInfo {
        LayoutInterval gap;
        int dimension;
        boolean resizeLeading, resizeTrailing;
        int position;
        int minSize;
        int currentSize;
        int[] ortPositions; // orthogonal L and T positions of all component neighbors (for gap vizualization)
        Rectangle paintRect;
        List<String> overlappingComponents;
        int[] defaultGapSizes;
        String description;
    }

    VisualState(LayoutModel model, VisualMapper mapper) {
        this.layoutModel = model;
        this.visualMapper = mapper;
        layoutModel.addListener(new LayoutModel.Listener() {
            @Override
            public void layoutChanged(LayoutEvent ev) {
                int type = ev.getType();
                if (type != LayoutEvent.INTERVAL_SIZE_CHANGED
                        && type != LayoutEvent.INTERVAL_PADDING_TYPE_CHANGED) {
                    componentGaps.clear();
                    if (compGapsToUpdate != null) {
                        compGapsToUpdate.clear();
                    }
                    containerGaps.clear();
                    if (contGapsToUpdate != null) {
                        contGapsToUpdate.clear();
                    }
                } // otherwise we can keep the current GapInfo set and just do update
            }
        });
    }

    void updateCurrentSpaceOfComponents(LayoutComponent container, boolean top) {
        if (top) {
            Rectangle bounds = container.getParent() != null ?
                    visualMapper.getComponentBounds(container.getId()) : null;
            container.setCurrentBounds(bounds, -1);
        }
        container.setCurrentInterior(visualMapper.getContainerInterior(container.getId()));
        for (LayoutComponent subComp : container.getSubcomponents()) {
            Rectangle bounds = visualMapper.getComponentBounds(subComp.getId());
            int baseline = visualMapper.getBaselinePosition(subComp.getId(), bounds.width, bounds.height);
            subComp.setCurrentBounds(bounds, baseline);
        }

        Dimension minimum = visualMapper.getComponentMinimumSize(container.getId());
        Rectangle bounds = visualMapper.getComponentBounds(container.getId());
        container.setDiffToMinimumSize(HORIZONTAL, bounds.width - minimum.width);
        container.setDiffToMinimumSize(VERTICAL, bounds.height - minimum.height);

        prepareGapsForUpdate();
        clippingCache.clear();
    }

    /**
     * We want to preserve the same GapInfo instances between layout rebuilds
     * where no changes in structure happened. This method gets called when the
     * layout has been rebuilt and the GapInfo objects that survived need to be
     * updated. That can also happen when the root designed component is changed
     * in the design view, in which case some gaps may become out of the view,
     * so need to be removed.
     */
    private void prepareGapsForUpdate() {
        if (componentGaps.isEmpty() && containerGaps.isEmpty()) {
            return; // we do this only once per entire update
        }

        for (Map.Entry<LayoutComponent, List<GapInfo>> e : componentGaps.entrySet()) {
            LayoutComponent comp = e.getKey();
            if (comp.getParent() != null && isComponentInDesignView(comp.getParent())) {
                List<GapInfo> gaps = e.getValue();
                if (!gaps.isEmpty()) {
                    if (compGapsToUpdate == null) {
                        compGapsToUpdate = new HashMap<LayoutComponent, List<GapInfo>>();
                    }
                    compGapsToUpdate.put(comp, e.getValue());
                } // Empty list could be a placeholder for no visible gaps of former design root.
            }
        }
        componentGaps.clear();

        for (Map.Entry<LayoutComponent, List<GapInfo>> e : containerGaps.entrySet()) {
            LayoutComponent comp = e.getKey();
            if (isComponentInDesignView(comp)) {
                if (contGapsToUpdate == null) {
                    contGapsToUpdate = new HashMap<LayoutComponent, List<GapInfo>>();
                }
                contGapsToUpdate.put(comp, e.getValue());
            }
        }
        containerGaps.clear();
    }

    private boolean isComponentInDesignView(LayoutComponent component) {
        return visualMapper.getComponentBounds(component.getId()) != null;
    }

    // Assuming updateCurrentSpaceOfComponents has already been done.
    void updateCurrentSpaceOfGroups(LayoutInterval group, int dimension, int[] parentEdgePositions) {
        if (group.getSubIntervalCount() == 0) {
            return;
        }

        LayoutRegion groupSpace = group.getCurrentSpace();
        if (group.getParent() != null) {
            assert !groupSpace.isSet(dimension);
            // take aligned edge positions from parent if known
            LayoutRegion parentSpace = group.getParent().getCurrentSpace();
            for (int e=LEADING; e <= TRAILING; e++) {
                if (parentSpace.isSet(dimension, e) && LayoutInterval.isAlignedAtBorder(group, e)) {
                    groupSpace.setPos(dimension, e, parentSpace.positions[dimension][e]);
                }
            }
        }
        if (group.getGroupAlignment() == BASELINE) {
            groupSpace.positions[dimension][BASELINE] = LayoutRegion.UNKNOWN;
        }

        LayoutRegion space = new LayoutRegion();
        boolean[] def = { false, false }; // edge position defined by aligned sub-interval?
        boolean[] undef = { false, false }; // aligned sub-interval not knowing edge position?
        int[] endGapSize = { -1, -1 }; // sizes of gaps at the ends of a sequence
        int[] groupEdgePositions = getGroupPositionLimits(group, dimension, parentEdgePositions);

        for (int i=0; i < group.getSubIntervalCount(); i++) {
            LayoutInterval sub = group.getSubInterval(i);
            if (sub.isEmptySpace()) {
                if (group.isSequential() && (i == 0 || i == group.getSubIntervalCount()-1)) {
                    // first or last gap in a sequence
                    int align = (i == 0) ? LEADING : TRAILING;
                    if (!groupSpace.isSet(dimension, align)) { // need to compute this edge
                        if (!LayoutInterval.canResize(sub)) {
                            if (sub.getPreferredSize() != NOT_EXPLICITLY_DEFINED) {
                                endGapSize[align] = sub.getPreferredSize();
                            } else { //if (LayoutInterval.getEffectiveAlignment(sub, align) == (align^1))
                                endGapSize[align] = LayoutUtils.getSizeOfDefaultGap(sub, visualMapper);
                            }
                        }
                        if (endGapSize[align] >= 0) {
                            def[align] = true;
                        } else {
                            undef[align] = true;
                        }
                    }
                }
            } else {
                LayoutRegion subSpace = sub.getCurrentSpace();
                if (sub.isGroup()) {
                    subSpace.reset();
                    updateCurrentSpaceOfGroups(sub, dimension, groupEdgePositions);
                } else if (sub.isComponent() && group.getGroupAlignment() == BASELINE
                           && groupSpace.positions[dimension][BASELINE] == LayoutRegion.UNKNOWN) {
                    groupSpace.positions[dimension][BASELINE] = subSpace.positions[dimension][BASELINE];
                }
                space.expand(subSpace, dimension);
                groupSpace.expand(subSpace, dimension^1);
                for (int e=LEADING; e <= TRAILING; e++) {
                    if (!groupSpace.isSet(dimension, e)) {
                        if (subSpace.isSet(dimension, e)) {
                            if (!def[e] && LayoutInterval.isAlignedAtBorder(sub, e)) {
                                def[e] = true;
                            }
                        } else if (!def[e] && !undef[e] && LayoutInterval.isAlignedAtBorder(sub, e)) {
                            undef[e] = true;
                        }
                    }
                }
            }
        }

        for (int e=LEADING; e <= TRAILING; e++) {
            if (groupSpace.isSet(dimension, e)) {
                continue;
            }
            int d = (e==LEADING ? -1 : 1);
            if (space.isSet(dimension, e) && (def[e] || !undef[e])) {
                // There is some aligned sub-interval knowing its position, or
                // no sub-interval that would not know its position.
                groupSpace.setPos(dimension, e, space.positions[dimension][e]);
                if (endGapSize[e] >= 0) {
                    int change = endGapSize[e] * d;
                    int gapPos = groupSpace.positions[dimension][e] + change;
                    int limitPos = parentEdgePositions != null ? parentEdgePositions[e] : LayoutRegion.UNKNOWN;
                    if (limitPos != LayoutRegion.UNKNOWN && gapPos*d > limitPos*d) {
                        // As bug 203628 shows, we can't rely on that group size
                        // defined by fixed explicit gap is computed correctly. So we
                        // prefer the group position be determinde from edge components.
                        change = (limitPos - gapPos + change) * d;
                    }
                    if (change != 0) {
                        groupSpace.reshape(dimension, e, change);
                    }
                }
            } else if (group.isParallel()) {
                // No sub-intervals knowing its real edge position. We should be able
                // to compute the position at least if they all end with a resizing gap.
                int outPos = LayoutRegion.UNKNOWN;
                for (LayoutInterval comp : LayoutUtils.getSideComponents(group, e, false, false)) {
                    int pos = group.getSubInterval(LayoutInterval.getIndexInParent(comp, group))
                              .getCurrentSpace().positions[dimension][e];
                    if (pos == LayoutRegion.UNKNOWN) {
                        pos = comp.getCurrentSpace().positions[dimension][e];
                        LayoutInterval borderGap = LayoutInterval.getNeighbor(comp, e, false, true, false);
                        if (borderGap != null && borderGap.isEmptySpace() && group.isParentOf(borderGap)) {
                            int gapSize = borderGap.getPreferredSize();
                            if (gapSize == NOT_EXPLICITLY_DEFINED) {
                                gapSize = LayoutUtils.getSizeOfDefaultGap(borderGap, visualMapper);
                            }
                            if (gapSize >= 0) {
                                pos += gapSize * d;
                            } else { // this can be whatever, bad luck
                                outPos = LayoutRegion.UNKNOWN;
                                break;
                            }
                        }
                    }
                    if (outPos == LayoutRegion.UNKNOWN || pos*d > outPos*d) {
                        outPos = pos;
                    }
                }
                if (outPos != LayoutRegion.UNKNOWN) {
                    groupSpace.setPos(dimension, e, outPos);
                }
            }
        }

        completeUknownGroupPositions(group, dimension);
    }

    private static int[] getGroupPositionLimits(LayoutInterval group, int dimension, int[] parentEdgePositions) {
        int[] pos = new int[] { LayoutRegion.UNKNOWN, LayoutRegion.UNKNOWN };
        for (int e=LEADING; e <= TRAILING; e++) {
            if (group.getParent() == null) {
                pos[e] = group.getCurrentSpace().positions[dimension][e]; // root defined by the container itself
            } else if (parentEdgePositions != null && parentEdgePositions[e] != LayoutRegion.UNKNOWN
                    && LayoutInterval.isAlignedAtBorder(group, e)) {
                pos[e] = parentEdgePositions[e]; // inheriting from parent
            } else {
                List<LayoutInterval> l = LayoutUtils.getSideComponents(group, e, true, true);
                if (!l.isEmpty()) {
                    pos[e] = l.get(0).getCurrentSpace().positions[dimension][e];
                } else {
                    pos[e] = parentEdgePositions[e]; // inheriting from parent
                }
            }
        }
        return pos;
    }

    private static void completeUknownGroupPositions(LayoutInterval group, int dimension) {
        LayoutRegion groupSpace = group.getCurrentSpace();
        for (int i=0; i < group.getSubIntervalCount(); i++) {
            LayoutInterval sub = group.getSubInterval(i);
            if (sub.isGroup()) {
                LayoutRegion subSpace = sub.getCurrentSpace();
                boolean subSpaceCompleted = false;
                for (int e=LEADING; e <= TRAILING; e++) {
                    if (subSpace.isSet(dimension, e)) {
                        continue;
                    }
                    if (group.isSequential()) {
                        if ((i == 0 && e == LEADING) || (i == group.getSubIntervalCount()-1 && e == TRAILING)) {
                            // first or last in sequence - set according to parent
                            if (groupSpace.isSet(dimension, e)) {
                                subSpace.setPos(dimension, e, groupSpace.positions[dimension][e]);
                            }
                        } else { // set according to neighbor
                            LayoutInterval neighbor = group.getSubInterval(i + (e==LEADING ? -1:1));
                            LayoutRegion nSpace = neighbor.getCurrentSpace();
                            assert nSpace.isSet(dimension, e^1); // otherwise completely undefined edge
                            subSpace.setPos(dimension, e, nSpace.positions[dimension][e^1]);
                        }
                    } else if (groupSpace.isSet(dimension, e)) { // set according to parallel parent
                        assert LayoutInterval.isAlignedAtBorder(sub, group, e)
                                || group.getSubIntervalCount() == 1;
                        subSpace.setPos(dimension, e, groupSpace.positions[dimension][e]);
                    }
                    if (subSpace.isSet(dimension, e)) {
                        subSpaceCompleted = true;
                    }
                }
                if (subSpaceCompleted) {
                    completeUknownGroupPositions(sub, dimension);
                }
            }
        }
    }

    // Assuming updateCurrentSpaceOfGroups has already been done.
    int collectResizingDiffs(LayoutInterval group, int dimension) {
        int groupDiff = 0; // difference of current size vs. default size (assuming it's resizing)
        int biggestDefSize = Integer.MIN_VALUE; // biggest default size of an interval in parallel group

        for (int i=0; i < group.getSubIntervalCount(); i++) {
            LayoutInterval sub = group.getSubInterval(i);
            int diff = 0;
            if (sub.isGroup()) {
                diff = collectResizingDiffs(sub, dimension);
            } else if (LayoutInterval.wantResize(sub)) {
                int defaultSize;
                if (sub.isEmptySpace()) {
                    int min = sub.getMinimumSize();
                    defaultSize = (min == NOT_EXPLICITLY_DEFINED) ?
                        LayoutUtils.getSizeOfDefaultGap(sub, visualMapper) : min;
                    // gaps with 0 min size are never used as default padding, so 0 is default size
                } else { // component
                    java.awt.Dimension prefSize = visualMapper.getComponentPreferredSize(sub.getComponent().getId());
                    defaultSize = (dimension == HORIZONTAL) ? prefSize.width : prefSize.height;
                }
                int currentSize = LayoutInterval.getCurrentSize(sub, dimension);
                diff = currentSize - defaultSize;
                sub.setDiffToDefaultSize(diff);
                if (sub.getPreferredSize() == currentSize
                        || (sub.getPreferredSize() == NOT_EXPLICITLY_DEFINED && diff == 0)) {
                    sub.unsetAttribute(LayoutInterval.ATTR_SIZE_DIFF);
                } else {
                    sub.setAttribute(LayoutInterval.ATTR_SIZE_DIFF);
                }
                if (diff < 0) { // to parent contribute with diff from 0 (ALT_SizeDefinition04Test)
                    diff = currentSize;
                } else { // for sub-container count with the diff of its inner root
                    diff = LayoutInterval.getDiffToDefaultSize(sub, true);
                }
            } else { // fixed intervals are considered as 0 diff (despite they can
                sub.setDiffToDefaultSize(0); // have explicit size different from default)
                sub.unsetAttribute(LayoutInterval.ATTR_SIZE_DIFF);
            }

            if (group.isSequential()) {
                groupDiff += diff;
            } else { // in parallel group
                int defSize = LayoutInterval.getCurrentSize(sub, dimension) - diff;
                if (defSize > biggestDefSize) {
                    biggestDefSize = defSize;
                }
            }
        }

        if (group.isParallel() && biggestDefSize != Integer.MIN_VALUE) {
            groupDiff = group.getCurrentSpace().size(dimension) - biggestDefSize;
        }
        group.setDiffToDefaultSize(groupDiff);
        return groupDiff;
    }

    // Assuming collectResizingDiffs has already been done.
    void updateToActualSize(LayoutInterval group, int dimension, int sizeUpdate) {
        Set<LayoutInterval> defaultCandidates = new HashSet();
        updateToActualSize(group, dimension, sizeUpdate, defaultCandidates);
        if (sizeUpdate == 1 && !defaultCandidates.isEmpty()) {
            // There are some candidates (i.e. "active" intervals with ATTR_FLEX_SIZEDEF
            // attribute) for optimizing which one should have the explicit size set.
            updateToActualSize(group, dimension, 2, defaultCandidates);
        }
        // proces intervals left for default size (size difinition moved elsewhere)
        for (LayoutInterval li : defaultCandidates) {
            int pref = LayoutInterval.getDefaultSizeDef(li);
            int min = li.isEmptySpace() ? pref : li.getMinimumSize();
            layoutModel.setIntervalSize(li, min, pref, li.getMaximumSize());
        }
    }

    /**
     * Updates preferred size of resizing intervals so they match (and define)
     * the actual designed size of the container.
     * @param sizeUpdate What to do with size of resizing intervals:
     *   0 - set to all default size (overall preferred size is defined as
     *       default without need of any explicit size of resizing intervals),
     *   1 - keep default size where default, update where explicit and not
     *       ATTR_FLEX_SIZEDEF; may set no size anywhere,
     *   2 - must set explicit size somewhere in the group (if 1 does not
     *       succeed, find best candidate from all to define overall size).
     * @return true if explicit size was updated in some resizing subinterval
     */
    private boolean updateToActualSize(LayoutInterval group, int dimension, int sizeUpdate,
                                       Set<LayoutInterval> defaultCandidates) {
        boolean forceAtSecond;
        if (sizeUpdate == 2 && group.isParallel()
                && (group.getParent() == null || !LayoutInterval.canResize(group))) {
            forceAtSecond = true;
            sizeUpdate = 1; // first try if some existing expl. size can be updated
        } else {
            forceAtSecond = false;
        }
        boolean updated = false; // explicit size updated anywhere?
        boolean updateMissing = false;

        do {
            LayoutInterval repInt = null; // best representative interval to hold the explicit size
            if (sizeUpdate == 2 && group.isParallel()) {
                int minDiff = Integer.MAX_VALUE;
                for (Iterator<LayoutInterval> it = group.getSubIntervals(); it.hasNext(); ) {
                    LayoutInterval sub = it.next();
                    int diff = LayoutInterval.getDiffToDefaultSize(sub, true); // for sub-container substitute with diff of its root
                    // In parallel group only the interval with smallest diff is
                    // forced to update to explicit size. In case more have same
                    // diff, the one that already has an explicit size is preferred.
                    if (diff < 0) {
                        diff = 100 - diff; // penalize negative diffs
                    }
                    if (diff > 0
                        && LayoutInterval.canResize(sub)
                        && (diff < minDiff
                            || (diff == minDiff
                                && (repInt == null
                                    || (sub.getPreferredSize() != NOT_EXPLICITLY_DEFINED
                                        && repInt.getPreferredSize() == NOT_EXPLICITLY_DEFINED))))) {
                        minDiff = diff;
                        repInt = sub;
                    }
                }
            }

            boolean sizeDefined = false;
            for (int i=0; i < group.getSubIntervalCount(); i++) {
                LayoutInterval sub = group.getSubInterval(i);
                boolean forceUpdate = (sizeUpdate == 2) && (group.isSequential() || sub == repInt);
                boolean updatedSub = false;
                int diff = LayoutInterval.getDiffToDefaultSize(sub, true);
                if (diff < 0 && sub.isEmptySpace() && sub.getPreferredSize() == NOT_EXPLICITLY_DEFINED
                        && group.isSequential() && (i == 0 || i == group.getSubIntervalCount()-1)) {
                    diff = 0; // default gap at group boundaries can sometimes be laid out incorrectly (squeezed by a few pixels)
                }
                if (diff == 0 // may support size of the group
                    && forceAtSecond && sizeUpdate == 1 && !sizeDefined // we may need second round
                    && LayoutInterval.getCurrentSize(sub, dimension)
                        == LayoutInterval.getCurrentSize(group, dimension)) {
                    sizeDefined = true;
                }
                if (sub.isGroup()) {
                    int subUpdate;
                    if (diff == 0) {
                        subUpdate = 0;
                    } else if (forceUpdate || !LayoutInterval.canResize(sub)) {
                        subUpdate = 2;
                    } else {
                        subUpdate = sizeUpdate == 0 ? 0 : 1;
                    }
                    updatedSub = updateToActualSize(sub, dimension, subUpdate, defaultCandidates);
                } else {
                    boolean single;
                    if (sub.isComponent() && sub.getComponent().isLayoutContainer()) {
                        // subcontainer - process its root as a group
                        LayoutComponent subContainer = sub.getComponent();
                        LayoutInterval root = subContainer.getDefaultLayoutRoot(dimension); // [TODO all roots]
                        int subUpdate;
                        if (diff == 0 || subContainer.getDiffToMinimumSize(dimension) < 0) {
                            subUpdate = 0;
                        } else if (forceUpdate || !LayoutInterval.canResize(sub)) {
                            subUpdate = 2;
                        } else {
                            subUpdate = sizeUpdate == 0 ? 0 : 1;
                        }
                        updatedSub = updateToActualSize(root, dimension, subUpdate, defaultCandidates);
                        // may need explicit size as component
                        single = subContainer.getDiffToMinimumSize(dimension) < 0;
                        if (!single && LayoutInterval.wantResize(sub)) {
                            if (updatedSub || diff == 0) {
                                sub.unsetAttribute(LayoutInterval.ATTR_SIZE_DIFF);
                            } else {
                                sub.setAttribute(LayoutInterval.ATTR_SIZE_DIFF);
                            }
                        }
                    } else { // gap or leaf component
                        single = true;
                    }
                    if (single && LayoutInterval.wantResize(sub)) { // resizing component or gap
                        int min = sub.getMinimumSize();
                        int pref = sub.getPreferredSize();
                        int max = sub.getMaximumSize();
                        int defaultPref = LayoutInterval.getDefaultSizeDef(sub);
                        boolean pretendDefault = pref != defaultPref && pref != NOT_EXPLICITLY_DEFINED
                                    && diff != 0 && sub.hasAttribute(LayoutInterval.ATTR_FLEX_SIZEDEF);
                        if (pretendDefault) {
                            pref = defaultPref;
                        }
                        int currentSize = LayoutInterval.getCurrentSize(sub, dimension);
                        int lastSize = sub.getLastActualSize();
                        if (lastSize < 0) { // not known yet since loading
                            sub.setLastActualSize(currentSize); // compare with this size next time
                        }
                        if (sizeUpdate == 0 || diff == 0) {
                            // this interval does not need explicit preferred size set to define overall size
                            layoutModel.setIntervalSize(sub, min, defaultPref, max);
                            pretendDefault = false;
                        } else if (forceUpdate
                                || (pref != defaultPref
                                    && pref != currentSize
                                    && (lastSize >= Short.MAX_VALUE
                                        || (lastSize >= 0 && lastSize != currentSize)))) {
                            // update defined preferred size to actual size (with exception
                            // of intervals whose actual size has not changed since loading)
                            layoutModel.setIntervalSize(sub, min, currentSize, max);
                            pretendDefault = false;
                            sub.setLastActualSize(Integer.MAX_VALUE); // always update next time
                        } // otherwise default size is left
                        if (pretendDefault) {
                            defaultCandidates.add(sub);
                        } else {
                            defaultCandidates.remove(sub);
                            pref = sub.getPreferredSize();
                            if (sizeUpdate > 0 && pref == currentSize) {
                                updatedSub = true;
                            }
                        }
                        if (diff == 0 || pref == currentSize) {
                            sub.unsetAttribute(LayoutInterval.ATTR_SIZE_DIFF);
                        } else {
                            sub.setAttribute(LayoutInterval.ATTR_SIZE_DIFF);
                        }
                    }
                }
                if (LayoutInterval.canResize(sub)) {
                    if (updatedSub) {
                        updated = true;
                    } else if (group.isSequential() && diff != 0) {
                        updateMissing = true;
                    }
                }
            }

            if (forceAtSecond) {
                if (sizeUpdate == 1 && !updated && !sizeDefined) {
                    // volunteer to hold the expl. size not found, and it's needed
                    sizeUpdate = 2; // now force it as was requested
                } else {
                    forceAtSecond = false;
                }
            }
        } while (forceAtSecond);

        return updated && !updateMissing;
    }

    // determine if the container needs explicit size in its parent
    void updateContainerSize(LayoutComponent container) {
        Dimension preferred = visualMapper.getComponentPreferredSize(container.getId());
        for (int i=0; i < DIM_COUNT; i++) {
            LayoutInterval outer = container.getLayoutInterval(i);
            int currentSize = outer.getCurrentSpace().size(i);
            int pref = i == HORIZONTAL ? preferred.width : preferred.height;
            boolean externalSize =
                (visualMapper.hasExplicitPreferredSize(container.getId()) && currentSize != pref)
                || (container.getDiffToMinimumSize(i) < 0 && currentSize != pref);
            resizeInterval(outer, externalSize ? currentSize : NOT_EXPLICITLY_DEFINED);
        }
    }

    void resizeInterval(LayoutInterval interval, int size) {
        assert interval.isSingle() && (size >= 0 || size == NOT_EXPLICITLY_DEFINED);
        int min = interval.getMinimumSize();
        int max = interval.getMaximumSize();
        if (min == interval.getPreferredSize() && (max < Short.MAX_VALUE || min > 0)) {
            min = size;
        }
        if (max == interval.getPreferredSize()) {
            max = max > 0 ? size : USE_PREFERRED_SIZE;
        }
        layoutModel.setIntervalSize(interval, min, size, max);
    }

    // to be called after removing some intervals if done as part of other
    // operation that continues and needs actual spaces and size diffs up-to-date
    void updateSpaceAfterRemove(LayoutInterval root, int dimension) {
        updateCurrentSpaceOfGroups(root, dimension, null);
        if (collectResizingDiffs(root, dimension) != 0) {
            updateToActualSize(root, dimension, 2);
        }
    }

    Collection<GapInfo> getComponentGaps(LayoutComponent component) {
        List<GapInfo> gaps = componentGaps.get(component);
        if (gaps == null) {
            if (compGapsToUpdate == null || (gaps=compGapsToUpdate.remove(component)) == null) {
                if (component.getParent() != null && isComponentInDesignView(component.getParent())) {
                    gaps = computeComponentGapInfo(component);
                } else if (component.isLayoutContainer()) {
                    gaps = Collections.emptyList(); // cache empty for container that is a (design) root
                } else {
                    return null; // nobody should ask for this
                }
            } else {
                updateComponentGapInfo(component, gaps);
            }
            componentGaps.put(component, gaps);
        }
        return gaps;
    }

    Collection<GapInfo> getContainerGaps(LayoutComponent container) {
        List<GapInfo> gaps = containerGaps.get(container);
        if (gaps == null) {
            if (contGapsToUpdate == null || (gaps=contGapsToUpdate.remove(container)) == null) {
                if (isComponentInDesignView(container)) {
                    gaps = computeContainerGapInfo(container);
                } else {
                    return null; // nobody should ask for this
                }
            } else {
                updateContainerGapInfo(container, gaps);
            }
            containerGaps.put(container, gaps);
        }
        return gaps;
    }

    // Compute neighbor gaps for given component (in both dimensions).
    private List<GapInfo> computeComponentGapInfo(LayoutComponent component) {
        List<GapInfo> gapList = new ArrayList<GapInfo>(4);
        for (int dim=VERTICAL; dim >= HORIZONTAL; dim--) {
            LayoutInterval li = component.getLayoutInterval(dim);
            LayoutInterval gap = LayoutInterval.getNeighbor(li, LEADING, false, true, false);
            if (gap != null && gap.isEmptySpace()) {
                GapInfo gapInfo = createOrUpdateGapInfo(null, gap, dim, component, component.getParent());
                if (gapInfo != null) {
                    gapList.add(gapInfo);
                }
            }
            gap = LayoutInterval.getNeighbor(li, TRAILING, false, true, false);
            if (gap != null && gap.isEmptySpace()) {
                GapInfo gapInfo = createOrUpdateGapInfo(null, gap, dim, component, component.getParent());
                if (gapInfo != null) {
                    gapList.add(gapInfo);
                }
            }
        }
        return gapList;
    }

    // Compute all gaps inside given container.
    private List<GapInfo> computeContainerGapInfo(LayoutComponent container) {
        List<GapInfo> gapList = new LinkedList<GapInfo>();
        List<LayoutInterval> l = new LinkedList<LayoutInterval>();
        for (int dim=VERTICAL; dim >= HORIZONTAL; dim--) {
            l.add(container.getLayoutRoot(0, dim));
            while (!l.isEmpty()) {
                LayoutInterval interval = l.remove(0);
                if (interval.isGroup()) {
                    Iterator<LayoutInterval> it = interval.getSubIntervals();
                    while (it.hasNext()) {
                        l.add(it.next());
                    }
                } else if (interval.isEmptySpace()) {
                    GapInfo gapInfo = createOrUpdateGapInfo(null, interval, dim, null, container);
                    if (gapInfo != null) {
                        gapList.add(gapInfo);
                    }
                }
            }
        }
        return gapList;
    }

    private void updateComponentGapInfo(LayoutComponent component, List<GapInfo> gapList) {
        for (GapInfo gapInfo : gapList) {
            createOrUpdateGapInfo(gapInfo, null, gapInfo.dimension, component, component.getParent());
        }        
    }

    private void updateContainerGapInfo(LayoutComponent container, List<GapInfo> gapList) {
        if (container.isLayoutContainer()) {
            LayoutInterval[] roots = new LayoutInterval[] { container.getDefaultLayoutRoot(HORIZONTAL),
                                                            container.getDefaultLayoutRoot(VERTICAL) };
            for (GapInfo gapInfo : gapList) {
                if (roots[gapInfo.dimension].isParentOf(gapInfo.gap)) {
                    createOrUpdateGapInfo(gapInfo, null, gapInfo.dimension, null, container);
                }
            }
        }
    }

    private GapInfo createOrUpdateGapInfo(GapInfo gapInfo, LayoutInterval gap,
            int dimension, LayoutComponent component, LayoutComponent container) {
        if (gap == null) {
            gap = gapInfo.gap;
        }
        LayoutInterval parent = gap.getParent();
        if (parent.isParallel()) {
            return null;
        }

        // compute gap position in its dimension
        boolean resizing = LayoutInterval.canResize(gap);
        LayoutInterval[] neighbors = new LayoutInterval[2];
        int[] gapPos = new int[2];
        int index = parent.indexOf(gap);
        if (index > 0) {
            neighbors[LEADING] = parent.getSubInterval(index-1);
            if (neighbors[LEADING].isEmptySpace()) {
                return null; // for robustness, 2 consecutive gaps happened somehow, would throw AE below
            }
            gapPos[LEADING] = neighbors[LEADING].getCurrentSpace().positions[dimension][TRAILING];
        } else {
            gapPos[LEADING] = (resizing ? parent.getParent() : parent)
                    .getCurrentSpace().positions[dimension][LEADING];
        }
        if (index+1 < parent.getSubIntervalCount()) {
            neighbors[TRAILING] = parent.getSubInterval(index+1);
            if (neighbors[TRAILING].isEmptySpace()) {
                return null; // for robustness, 2 consecutive gaps happened somehow, would throw AE below
            }
            gapPos[TRAILING] = neighbors[TRAILING].getCurrentSpace().positions[dimension][LEADING];
        } else {
            gapPos[TRAILING] = (resizing ? parent.getParent() : parent)
                    .getCurrentSpace().positions[dimension][TRAILING];
        }
        if (neighbors[LEADING] == null && neighbors[TRAILING] == null) {
            return null;
        }

        if (gapInfo == null) {
            gapInfo = new GapInfo();
            gapInfo.gap = gap;
        }
        gapInfo.dimension = dimension;
        gapInfo.position = gapPos[LEADING];

        // determine actual and minimum size of the gap
        int wholeSize = gapPos[TRAILING] - gapPos[LEADING];
        int minSize = gap.getMinimumSize();
        int prefSize = gap.getPreferredSize();
        if (minSize == USE_PREFERRED_SIZE) {
            minSize = gap.getPreferredSize();
        }
        if (minSize == NOT_EXPLICITLY_DEFINED || prefSize == NOT_EXPLICITLY_DEFINED) {
            int defaultSize = resizing || neighbors[LEADING] == null || neighbors[TRAILING] == null
                    ? LayoutUtils.getSizeOfDefaultGap(gap, visualMapper) : wholeSize;
            if (minSize == NOT_EXPLICITLY_DEFINED) {
                minSize = defaultSize;
            }
            if (prefSize == NOT_EXPLICITLY_DEFINED) {
                prefSize = defaultSize;
            }
        }
        gapInfo.minSize = minSize;
        gapInfo.currentSize = resizing ? wholeSize : prefSize;

        // determine position of the resize handle - in which direction the gap would grow
        gapInfo.resizeLeading = false;
        gapInfo.resizeTrailing = false;
        LayoutInterval parParent = parent.getParent();
        LayoutInterval li = gap;
        do {
            int align = LayoutInterval.getEffectiveAlignmentInParent(li, null, LEADING);
            if (align != LEADING && li == gap && LayoutDesigner.shouldAbsorbExplicitSizeChange(gap)) {
                // fixed gap with some resizing neighbor to absorb size change
                gapInfo.resizeLeading = true;
                if (align != TRAILING) { // resizing intervals on both sides
                    gapInfo.resizeTrailing = true;
                }
                break;
            }
            LayoutInterval inPar = li.getParent().isSequential() ? li.getParent() : li;
            if (align == TRAILING) {
                if (!LayoutInterval.isAlignedAtBorder(inPar, parParent, LEADING)
                        && !LayoutInterval.isPlacedAtBorder(inPar, parParent, dimension, LEADING)) {
                    // there's a space to grow in leading direction
                    gapInfo.resizeLeading = true;
                    break;
                }
            } else if (LayoutInterval.isAlignedAtBorder(inPar, parParent, TRAILING)
                       || LayoutInterval.isPlacedAtBorder(inPar, parParent, dimension, TRAILING)) {
                // would grow to trailing side
                break;
            }
            li = parParent;
            parParent = LayoutInterval.getFirstParent(li, PARALLEL);
        } while (parParent != null);

        if (!gapInfo.resizeLeading) {
            gapInfo.resizeTrailing = true;
        }

        // determine suitable space in the orthogonal dimension to visualize the gap
        int[] ortPos = { Integer.MAX_VALUE, Integer.MIN_VALUE };
        for (int e=LEADING; e <= TRAILING; e++) {
            if (neighbors[e] != null) {
                List<LayoutInterval> relatedComps = LayoutUtils.getSideComponents(neighbors[e], e^1, true, false);
                if (relatedComps.isEmpty()) {
                    relatedComps = LayoutUtils.getSideComponents(neighbors[e], e^1, false, false);
                }
                for (LayoutInterval comp : relatedComps) {
                    int[][] pos = comp.getCurrentSpace().positions;
                    if (component == null || comp.getComponent() == component
                            || Math.abs((pos[dimension][e^1] - gapPos[e])) < PROXIMITY) {
                        // for selected component consider only those parallel neigbor
                        // components that are close enough to the gap
                        int p = pos[dimension^1][LEADING];
                        if (p < ortPos[LEADING]) {
                            ortPos[LEADING] = p;
                        }
                        p = pos[dimension^1][TRAILING];
                        if (p > ortPos[TRAILING]) {
                            ortPos[TRAILING] = p;
                        }
                    }
                }
            }
        }
        gapInfo.ortPositions = ortPos;
        gapInfo.paintRect = null;
        gapInfo.overlappingComponents = (container != null) ? computeOverlappingComponents(gapInfo, container) : null;

        // format description of the gap
        StringBuilder fmtKey = new StringBuilder();
        fmtKey.append(dimension == HORIZONTAL ? "FMT_GAP_DESCRIPTION_H" : "FMT_GAP_DESCRIPTION_V"); // NOI18N
        fmtKey.append(resizing ? "R" : "F"); // NOI18N
        List<String> sizeParam = new ArrayList<String>(2);
        prefSize = gap.getPreferredSize();
        if (prefSize == NOT_EXPLICITLY_DEFINED) {
            PaddingType pt = gap.getPaddingType();
            if (pt == null
                    && (neighbors[LEADING] != null || LayoutInterval.getNeighbor(gap, LEADING, true, true, false) != null)
                    && (neighbors[TRAILING] != null || LayoutInterval.getNeighbor(gap, TRAILING, true, true, false) != null)) {
                pt = PaddingType.RELATED;
            }
            sizeParam.add(getDefaultGapDisplayName(pt));
            if (resizing && gap.getDiffToDefaultSize() != 0) {
                fmtKey.append("2"); // NOI18N
                sizeParam.add(Integer.toString(wholeSize));
            }
        } else {
            sizeParam.add(Integer.toString(prefSize));
            if (resizing && prefSize != wholeSize) {
                fmtKey.append("2"); // NOI18N
                sizeParam.add(Integer.toString(wholeSize));
            }
        }
        gapInfo.description = NbBundle.getMessage(VisualState.class, fmtKey.toString(), sizeParam.toArray());

        return gapInfo;
    }

    private static List<String> computeOverlappingComponents(GapInfo gapInfo, LayoutComponent container) {
        List<String> overlapping = null;
        LayoutRegion gapFakeRegion = new LayoutRegion();
        gapFakeRegion.set(gapInfo.dimension, gapInfo.position, gapInfo.position + gapInfo.currentSize);
        gapFakeRegion.set(gapInfo.dimension^1, gapInfo.ortPositions[LEADING], gapInfo.ortPositions[TRAILING]);
        for (LayoutComponent comp : container.getSubcomponents()) {
            if (LayoutRegion.overlap(comp.getCurrentSpace(), gapFakeRegion)) {
                if (overlapping == null) {
                    overlapping = new LinkedList<String>();
                }
                overlapping.add(comp.getId());
            }
        }
        return overlapping;
    }

    // -----

    Shape getComponentVisibilityClip(LayoutComponent component) {
        Shape clip = clippingCache.get(component);
        if (!clippingCache.containsKey(component)) {
            clip = visualMapper.getComponentVisibilityClip(component.getId());
            clippingCache.put(component, clip);
        }
        return clip;
    }

    /**
     * Returns a shape object suitable as a clip region for painting given gaps.
     * If there are some components that would be painted over, the provided
     * clip shape contains holes for these components. Otherwise the same
     * 'baseClipeRect' instance is returned, meaning no additional restriction
     * is needed for painting the gaps.
     */
    Shape getClipForGapPainting(Collection<GapInfo> gaps, Shape baseClipRect) {
        if (gaps == null || gaps.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Area clip = null;
        // Remove area of all overlapping components
        for (GapInfo gap : gaps) {
            if (gap.overlappingComponents != null) {
                for (String componentId : gap.overlappingComponents) {
                    if (clip == null) {
                        clip = new Area(baseClipRect);
                    }
                    Rectangle componentBounds = visualMapper.getComponentBounds(componentId);
                    Area componentArea = new Area(componentBounds);
                    clip.subtract(componentArea);
                }
            }
        }
        if (clip != null) {
            return clip;
        }
        return baseClipRect;
    }

    static String getDefaultGapDisplayName(PaddingType pt) {
        if (PADDING_DISPLAY_NAMES == null) {
            ResourceBundle bundle = NbBundle.getBundle(VisualState.class);
            PADDING_DISPLAY_NAMES = new String[] {
                bundle.getString("DEFAULT_GAP_SMALL"), // NOI18N
                bundle.getString("DEFAULT_GAP_MEDIUM"), // NOI18N
                bundle.getString("INDENT_GAP"), // NOI18N
                bundle.getString("DEFAULT_GAP_LARGE"), // NOI18N
                bundle.getString("DEFAULT_GAP_UNSPECIFIED") // NOI18N
            };
        }
        return PADDING_DISPLAY_NAMES[pt != null ? pt.ordinal() : 4];
    }

    static List<LayoutComponent> getComponentsInRegion(LayoutComponent container, LayoutInterval[] roots, LayoutRegion space) {
        List<LayoutComponent> list = null;
        for (LayoutComponent comp : container.getSubcomponents()) {
            if (LayoutRegion.overlap(space, comp.getCurrentSpace())
                    && (roots == null || roots[HORIZONTAL] == LayoutInterval.getRoot(comp.getLayoutInterval(HORIZONTAL)))) {
                if (list == null) {
                    list = new LinkedList<LayoutComponent>();
                }
                list.add(comp);
            }
        }
        return list != null ? list : Collections.EMPTY_LIST;
    }
}
