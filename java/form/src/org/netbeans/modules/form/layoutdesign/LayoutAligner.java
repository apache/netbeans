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

import java.util.*;

/**
 * Aligning algorithms.
 *
 * @author Jan Stola
 */
class LayoutAligner implements LayoutConstants {
    /** Layout designer that invoked the layout aligner. */
    private LayoutDesigner designer;
    /** The corresponding layout model. */
    private LayoutModel layoutModel;
    /** The corresponding layout operations. */
    private LayoutOperations operations;

    /**
     * Creates new <code>LayoutAligner</code>.
     *
     * @param designer layout designer to use.
     * @param layoutModel layout model to use.
     * @param operations layout operations to use.
     */
    LayoutAligner(LayoutDesigner designer, LayoutModel layoutModel, LayoutOperations operations) {
        this.designer = designer;
        this.layoutModel = layoutModel;
        this.operations = operations;
    }
    
    /**
     * Aligns given components in the specified direction.
     *
     * @param componentIds IDs of components that should be aligned.
     * @param closed determines if closed group should be created.
     * @param dimension dimension to align in.
     * @param alignment requested alignment.
     */
    void alignIntervals(LayoutInterval[] intervals, boolean closed, int dimension, int alignment) {        
        // Find nearest common (parallel) parent
        LayoutInterval parParent = LayoutInterval.getCommonParent(intervals);
        if (parParent.isSequential()) {
            parParent = parParent.getParent();
        }

        // Divide layout intervals into pre/aligned/post parallel groups.
        markByAlignAttributes(parParent, intervals);
        List<LayoutInterval> removedSeqs = new LinkedList<LayoutInterval>();
        parParent = splitByAlignAttrs(parParent, removedSeqs, alignment, closed, dimension, false);

        // Transfer the intervals into common parallel parent
        List gapsToResize = transferToParallelParent(intervals, parParent, alignment, closed);
        LayoutInterval returnPar = LayoutInterval.getFirstParent(parParent, PARALLEL);
        returnRemovedIntervals((returnPar == null) ? parParent : returnPar, removedSeqs, dimension);

        if (alignment != CENTER) {
            // Calculate leading and trailing intervals
            Map<LayoutInterval,LayoutInterval> leadingMap = new HashMap<LayoutInterval,LayoutInterval>();
            Map<LayoutInterval,LayoutInterval> trailingMap = new HashMap<LayoutInterval,LayoutInterval>();
            for (int i=0; i<intervals.length; i++) {
                LayoutInterval interval = intervals[i];
                LayoutInterval parent = interval.getParent();
                // Special handling for standalone component intervals in parParent
                if (parent == parParent) parent = interval;
                LayoutInterval leading = leadingMap.get(parent);
                LayoutInterval trailing = trailingMap.get(parent);
                if ((leading == null) || (parent.indexOf(leading) > parent.indexOf(interval))) {
                    leadingMap.put(parent, interval);
                }
                if ((trailing == null) || (parent.indexOf(trailing) < parent.indexOf(interval))) {
                    trailingMap.put(parent, interval);
                }
            }        
            // Create arrays of leading/trailing intervals
            LayoutInterval[] leadingIntervals = new LayoutInterval[leadingMap.size()];
            LayoutInterval[] trailingIntervals = new LayoutInterval[trailingMap.size()];
            Iterator iter = leadingMap.values().iterator();
            int counter = 0;
            while (iter.hasNext()) {
                LayoutInterval interval = (LayoutInterval)iter.next();
                leadingIntervals[counter++] = interval;
            }
            iter = trailingMap.values().iterator();
            counter = 0;
            while (iter.hasNext()) {
                LayoutInterval interval = (LayoutInterval)iter.next();
                trailingIntervals[counter++] = interval;
            }

            // Perform alignment of the intervals transfered into common parallel parent
            if (closed) {
                align(leadingIntervals, trailingIntervals, true, dimension, alignment);
            } else {
                LayoutInterval[] itervalsToAlign = (alignment == LEADING) ? leadingIntervals : trailingIntervals;
                align(itervalsToAlign, null, false, dimension, alignment);
            }
            
            // Must be done after align() to keep original eff. alignment inside align() method
            iter = gapsToResize.iterator();
            while (iter.hasNext()) {
                LayoutInterval gap = (LayoutInterval)iter.next();
                operations.setIntervalResizing(gap, true);
            }
        }

        // Do some clean up
        designer.destroyGroupIfRedundant(parParent, null);
    }

    /**
     * Marks layout intervals by <code>ATTR_ALIGN_PRE</code> and
     * <code>ATTR_ALIGN_POST</code> attributes. The first one is assigned
     * to the intervals that are in front of some from the passed
     * <code>intervals</code>. The second one is assigned to the intervals
     * that are behind some of the passed <code>intervals</code>.
     * Note that some intervals may obtain both of them.
     *
     * @param parParent parallel parent of all passed intervals
     * (the marking is restricted to this interval).
     * @param intervals intervals according which the marking is done.
     */
    private void markByAlignAttributes(LayoutInterval parParent, LayoutInterval[] intervals) {
        layoutModel.changeIntervalAttribute(parParent, LayoutInterval.ATTR_ALIGN_PRE, true);
        layoutModel.changeIntervalAttribute(parParent, LayoutInterval.ATTR_ALIGN_POST, true);
        for (int i=0; i<intervals.length; i++) {
            LayoutInterval interval = intervals[i];
            while (interval != parParent) {
                LayoutInterval parent = interval.getParent();
                layoutModel.changeIntervalAttribute(interval, LayoutInterval.ATTR_ALIGN_PRE, true);
                layoutModel.changeIntervalAttribute(interval, LayoutInterval.ATTR_ALIGN_POST, true);
                if (parent.isSequential()) {
                    int index = parent.indexOf(interval);
                    for (int j=0; j<parent.getSubIntervalCount(); j++) {
                        if (j < index) {
                            markByAlignAttribute(parent.getSubInterval(j), LayoutInterval.ATTR_ALIGN_PRE);
                        } else if (j > index) {
                            markByAlignAttribute(parent.getSubInterval(j), LayoutInterval.ATTR_ALIGN_POST);
                        }
                    }
                }
                interval = parent;
            }
        }
    }

    /**
     * Marks the <code>interval</code> by the given attribute. If the interval
     * is group, then all subintervals are marked as well.
     *
     * @param interval interval to mark.
     * @param attr attr to be assigned.
     */
    private void markByAlignAttribute(LayoutInterval interval, int attr) {
        layoutModel.changeIntervalAttribute(interval, attr, true);
        if (interval.isGroup()) {
            Iterator iter = interval.getSubIntervals();
            while (iter.hasNext()) {
                markByAlignAttribute((LayoutInterval)iter.next(), attr);
            }
        }
    }

    private LayoutInterval splitByAlignAttrs(LayoutInterval interval, List<LayoutInterval> removedSeqs, int alignment, boolean closed, int dimension, boolean optimize) {
       if (interval.isGroup()) {
            for (int i=interval.getSubIntervalCount()-1; i>=0; i--) {
                LayoutInterval subInterval = interval.getSubInterval(i);
                splitByAlignAttrs(subInterval, removedSeqs, alignment, closed, dimension, true);
            }
            if (interval.isParallel()) {
                if (interval.hasAttribute(LayoutInterval.ATTR_ALIGN_PRE)
                    && interval.hasAttribute(LayoutInterval.ATTR_ALIGN_POST)) {
                    LayoutInterval prePar = new LayoutInterval(PARALLEL);
                    layoutModel.changeIntervalAttribute(prePar, LayoutInterval.ATTR_ALIGN_PRE, true);
                    LayoutInterval midPar = new LayoutInterval(PARALLEL);
                    layoutModel.changeIntervalAttribute(midPar, LayoutInterval.ATTR_ALIGN_PRE, true);
                    layoutModel.changeIntervalAttribute(midPar, LayoutInterval.ATTR_ALIGN_POST, true);
                    LayoutInterval postPar = new LayoutInterval(PARALLEL);
                    layoutModel.changeIntervalAttribute(postPar, LayoutInterval.ATTR_ALIGN_POST, true);

                    // Calculate statistics for this parallel group
                    int minMid = Short.MAX_VALUE;
                    int maxMid = Short.MIN_VALUE;
                    int maxPre = Short.MIN_VALUE;
                    int minPost = Short.MAX_VALUE;
                    int maxMidWidth = 0;
                    Iterator iter = interval.getSubIntervals();
                    while (iter.hasNext()) {
                        LayoutInterval subInterval = (LayoutInterval)iter.next();
                        boolean preSub = subInterval.hasAttribute(LayoutInterval.ATTR_ALIGN_PRE);
                        boolean postSub = subInterval.hasAttribute(LayoutInterval.ATTR_ALIGN_POST);
                        LayoutInterval trailingPre = null;
                        LayoutInterval leadingMid = null;
                        LayoutInterval trailingMid = null;
                        LayoutInterval leadingPost = null;
                        if (subInterval.isSequential() && preSub && postSub) {
                            Iterator subIter = subInterval.getSubIntervals();
                            while (subIter.hasNext()) {
                                LayoutInterval subSubInterval = (LayoutInterval)subIter.next();
                                boolean preSubSub = subSubInterval.hasAttribute(LayoutInterval.ATTR_ALIGN_PRE);
                                boolean postSubSub = subSubInterval.hasAttribute(LayoutInterval.ATTR_ALIGN_POST);
                                if (preSubSub) {
                                    if (postSubSub) {
                                        if (leadingMid == null) {
                                            leadingMid = subSubInterval;
                                        }
                                        trailingMid = subSubInterval;                                    
                                    } else {
                                        if (!subSubInterval.isEmptySpace() || (subSubInterval.getPreferredSize() == NOT_EXPLICITLY_DEFINED)) {
                                            trailingPre = subSubInterval;
                                        }
                                    }
                                } else if (postSubSub) {
                                    if ((leadingPost == null) && (!subSubInterval.isEmptySpace() || (subSubInterval.getPreferredSize() == NOT_EXPLICITLY_DEFINED))) {
                                        leadingPost = subSubInterval;
                                    }
                                }
                            }
                        } else {
                            if (preSub) {
                                if (postSub) {
                                    leadingMid = subInterval;
                                    trailingMid = subInterval;                                    
                                } else {
                                    trailingPre = subInterval;
                                }
                            } else if (postSub) {
                                leadingPost = subInterval;
                            }
                        }
                        
                        if (trailingPre != null) {
                            maxPre = Math.max(maxPre, getPosition(trailingPre, dimension, TRAILING));
                        }
                        if (leadingMid != null) { // implies trailingMid != null
                            int midLeading = getPosition(leadingMid, dimension, LEADING);
                            int midTrailing = getPosition(trailingMid, dimension, TRAILING);
                            int width = midTrailing - midLeading;
                            minMid = Math.min(minMid, midLeading);
                            maxMid = Math.max(maxMid, midTrailing);
                            maxMidWidth = Math.max(maxMidWidth, width);
                        }
                        if (leadingPost != null) {
                            minPost = Math.min(minPost, getPosition(leadingPost, dimension, LEADING));
                        }
                    }

                    // Perform the split
                    for (int i=interval.getSubIntervalCount()-1; i>=0; i--) {
                        LayoutInterval subInterval = interval.getSubInterval(i);
                        layoutModel.removeInterval(subInterval);
                        if (subInterval.isSequential()) {
                            if (!subInterval.hasAttribute(LayoutInterval.ATTR_ALIGN_PRE)
                                && !subInterval.hasAttribute(LayoutInterval.ATTR_ALIGN_POST)) {
                                removedSeqs.add(subInterval);
                                continue;
                            }
                            LayoutInterval preSeq = new LayoutInterval(SEQUENTIAL);
                            layoutModel.changeIntervalAttribute(preSeq, LayoutInterval.ATTR_ALIGN_PRE, true);
                            LayoutInterval midSeq = new LayoutInterval(SEQUENTIAL);
                            layoutModel.changeIntervalAttribute(midSeq, LayoutInterval.ATTR_ALIGN_PRE, true);
                            layoutModel.changeIntervalAttribute(midSeq, LayoutInterval.ATTR_ALIGN_POST, true);
                            LayoutInterval postSeq = new LayoutInterval(SEQUENTIAL);
                            layoutModel.changeIntervalAttribute(postSeq, LayoutInterval.ATTR_ALIGN_POST, true);

                            int[] leading = new int[subInterval.getSubIntervalCount()];
                            int[] trailing = new int[leading.length];
                            for (int j=0; j<subInterval.getSubIntervalCount(); j++) {
                                LayoutInterval subSubInterval = subInterval.getSubInterval(j);
                                leading[j] = getPosition(subSubInterval, dimension, LEADING);
                                trailing[j] = getPosition(subSubInterval, dimension, TRAILING);
                            }

                            // Update some gaps
                            LayoutInterval lastPre = null;
                            LayoutInterval lastPreGap = null;
                            LayoutInterval firstPostGap = null;
                            LayoutInterval firstMid = null;
                            LayoutInterval lastMid = null;
                            LayoutInterval firstPost = null;
                            for (int j=0; j<subInterval.getSubIntervalCount(); j++) {
                                LayoutInterval subSubInterval = subInterval.getSubInterval(j);
                                boolean pre = subSubInterval.hasAttribute(LayoutInterval.ATTR_ALIGN_PRE);
                                boolean post = subSubInterval.hasAttribute(LayoutInterval.ATTR_ALIGN_POST);
                                if (pre && !post) {
                                    lastPreGap = subSubInterval;
                                }
                                if (post && !pre) {
                                    if (firstPostGap == null) {
                                        firstPostGap = subSubInterval;
                                    }
                                }
                                if (pre && post) {
                                    if (firstMid == null) {
                                        firstMid = subSubInterval;
                                    }
                                    lastMid = subSubInterval;
                                }
                            }
                            firstPost = firstPostGap;
                            lastPre = lastPreGap;
                            if ((firstPostGap != null) && !firstPostGap.isEmptySpace()) {
                                firstPostGap = null;
                            }
                            if ((lastPreGap != null) && !lastPreGap.isEmptySpace()) {
                                lastPreGap = null;
                            }
                            if (alignment == LEADING) {
                                int bias = Math.max(0, maxPre - minMid);
                                int shift = (LayoutInterval.getEffectiveAlignment(firstMid) == LEADING) ? bias : 0;
                                if (lastPreGap != null) {
                                    int delta = shortenGap(lastPreGap, getPosition(firstMid, dimension, LEADING) - minMid - shift);
                                    trailing[subInterval.indexOf(lastPreGap)] -= delta;
                                }
                                shift = (LayoutInterval.getEffectiveAlignment(lastMid) == LEADING) ? bias : 0; 
                                if (firstPostGap != null) {
                                    int delta = shortenGap(firstPostGap, maxMidWidth - (getPosition(lastMid, dimension, TRAILING) - minMid) + shift);
                                    leading[subInterval.indexOf(firstPostGap)] += delta;
                                }
                            }
                            if (alignment == TRAILING) {
                                int bias = Math.max(0, maxMid - minPost);
                                int shift = (LayoutInterval.getEffectiveAlignment(lastMid) == TRAILING) ? bias : 0;
                                if (firstPostGap != null) {
                                    int delta = shortenGap(firstPostGap, maxMid - getPosition(lastMid, dimension, TRAILING) - shift);
                                    leading[subInterval.indexOf(firstPostGap)] += delta;
                                }
                                shift = (LayoutInterval.getEffectiveAlignment(firstMid) == TRAILING) ? bias : 0;
                                if (lastPreGap != null) {
                                    int delta = shortenGap(lastPreGap, maxMidWidth - (maxMid - getPosition(firstMid, dimension, LEADING)) + shift);
                                    trailing[subInterval.indexOf(lastPreGap)] -= delta;
                                }
                            }
                            
                            // Set alignment of sequences
                            setAlignmentAccordingEffectiveAlignment(preSeq, lastPre);
                            setAlignmentAccordingEffectiveAlignment(midSeq, firstMid);
                            setAlignmentAccordingEffectiveAlignment(postSeq, firstPost);
                            
                            for (int j=subInterval.getSubIntervalCount()-1; j>=0; j--) {
                                LayoutInterval subSubInterval = subInterval.getSubInterval(j);
                                boolean pre = subSubInterval.hasAttribute(LayoutInterval.ATTR_ALIGN_PRE);
                                boolean post = subSubInterval.hasAttribute(LayoutInterval.ATTR_ALIGN_POST);
                                assert pre || post; 
                                LayoutInterval seqToInsertInto;
                                if (pre && !post && ((alignment == LEADING) || closed)) {
                                    seqToInsertInto = preSeq;
                                } else if (post && !pre && ((alignment == TRAILING) || closed)) {
                                    seqToInsertInto = postSeq;
                                } else {
                                    seqToInsertInto = midSeq;
                                }
                                expandCurrentSpace(seqToInsertInto, dimension, leading[j], trailing[j]);
                                layoutModel.removeInterval(subSubInterval);
                                layoutModel.addInterval(subSubInterval, seqToInsertInto, 0);
                            }
                            putGroupToGroup(preSeq, prePar, 0);
                            putGroupToGroup(midSeq, midPar, 0);
                            putGroupToGroup(postSeq, postPar, 0);
                        } else {
                            boolean pre = subInterval.hasAttribute(LayoutInterval.ATTR_ALIGN_PRE);
                            boolean post = subInterval.hasAttribute(LayoutInterval.ATTR_ALIGN_POST);
                            if (!pre && !post) {
                                removedSeqs.add(subInterval);
                            } else if (pre && !post && ((alignment == LEADING) || closed)) {
                                layoutModel.addInterval(subInterval, prePar, 0);
                                prePar.getCurrentSpace().expand(subInterval.getCurrentSpace());
                            } else if (post && !pre && ((alignment == TRAILING) || closed)) {
                                layoutModel.addInterval(subInterval, postPar, 0);
                                postPar.getCurrentSpace().expand(subInterval.getCurrentSpace());
                            } else {
                                layoutModel.addInterval(subInterval, midPar, 0);
                                midPar.getCurrentSpace().expand(subInterval.getCurrentSpace());
                            }
                        }
                    }
                    LayoutInterval parent = interval.getParent();
                    int index;
                    if (parent != null) {
                        index = layoutModel.removeInterval(interval);
                    } else {
                        parent = interval;
                        index = 0;
                    }
                    if (!parent.isSequential()) {
                        LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
                        layoutModel.changeIntervalAttribute(seq, LayoutInterval.ATTR_ALIGN_PRE, true);
                        layoutModel.changeIntervalAttribute(seq, LayoutInterval.ATTR_ALIGN_POST, true);
                        layoutModel.addInterval(seq, parent, index);
                        index = 0;
                        parent = seq;
                    }
                    putGroupToGroup(postPar, parent, index);
                    interval = putGroupToGroup(midPar, parent, index, optimize);
                    putGroupToGroup(prePar, parent, index);
                }
            }
        }
        return interval;
    }

    private LayoutInterval putGroupToGroup(LayoutInterval groupToInsert, LayoutInterval group, int index) {
        return putGroupToGroup(groupToInsert, group, index, true);
    }
    
    private LayoutInterval putGroupToGroup(LayoutInterval groupToInsert, LayoutInterval group, int index, boolean optimize) {
        // Remove empty spaces from parallel group
        if (groupToInsert.isParallel()) {
            LayoutInterval emptySpace = null;
            for (int i=groupToInsert.getSubIntervalCount()-1; i>=0; i--) {
                LayoutInterval interval = groupToInsert.getSubInterval(i);
                if (interval.isEmptySpace()) {
                    emptySpace = interval;
                    layoutModel.removeInterval(interval);
                }
            }
            if ((groupToInsert.getSubIntervalCount() == 0) && (emptySpace != null)) {
                // Use the last empty space as a replacement for the group - handled below
                layoutModel.addInterval(emptySpace, groupToInsert, 0);
            }
        }
        if (groupToInsert.getSubIntervalCount() > 0) {
            LayoutRegion region = groupToInsert.getCurrentSpace();
            while (optimize && (groupToInsert.getSubIntervalCount() == 1)) {
                LayoutInterval interval = groupToInsert.getSubInterval(0);
                layoutModel.removeInterval(interval);
                layoutModel.setIntervalAlignment(interval, groupToInsert.getAlignment());
                groupToInsert = interval;
            }
            if (optimize && groupToInsert.isSequential() && group.isSequential()) {
                for (int i=groupToInsert.getSubIntervalCount()-1; i>=0; i--) {
                    LayoutInterval subInterval = groupToInsert.getSubInterval(i);
                    layoutModel.removeInterval(subInterval);
                    layoutModel.addInterval(subInterval, group, index);
                }
                groupToInsert = null;
            } else {
                layoutModel.addInterval(groupToInsert, group, index);
            }
            group.getCurrentSpace().expand(region);
        }
        return groupToInsert;
    }

    /**
     * Expands the current space of <code>interval</code>.
     *
     * @param interval interval whose current space should be expanded.
     * @param dimension dimension in which the expansion occurs.
     * @param leading lower bound of the expansion.
     * @param trailing upper bound of the expansion.
     */
    private void expandCurrentSpace(LayoutInterval interval, int dimension, int leading, int trailing) {
        LayoutRegion region = new LayoutRegion();
        region.positions[dimension][LEADING] = leading;
        region.positions[dimension][TRAILING] = trailing;
        interval.getCurrentSpace().expand(region, dimension);
    }
    
    private int getPosition(LayoutInterval interval, int dimension, int alignment) {
        if (interval.isEmptySpace()) {
            LayoutInterval parent = interval.getParent();
            assert parent.isSequential() && ((alignment == LEADING) || (alignment == TRAILING));
            int index = parent.indexOf(interval);
            if (alignment == LEADING) {
                return (index > 0) ?
                parent.getSubInterval(index-1).getCurrentSpace().positions[dimension][TRAILING] :
                parent.getCurrentSpace().positions[dimension][LEADING];                
            } else { // alignment == TRAILING
                return  (index+1 < parent.getSubIntervalCount()) ?
                    parent.getSubInterval(index+1).getCurrentSpace().positions[dimension][LEADING] :
                    parent.getCurrentSpace().positions[dimension][TRAILING];
            }
        } else {
            return interval.getCurrentSpace().positions[dimension][alignment];
        }
    }

    /**
     * Ensures that the nearest parallel parent of the given intervals is the passed one.
     *
     * @param intervals intervals that should be transfered into the given parallel parent.
     * @param parParent parallel group that is already parent (but maybe not the nearest
     * parallel parent) of the given intervals.
     * @param requested alignment.
     * @return <code>List</code> of intervals (gaps) that should become resizable.
     */
    private List transferToParallelParent(LayoutInterval[] intervals, LayoutInterval parParent, int alignment, boolean closed) {
        // Determine dimension used to align components
        LayoutComponent temp = intervals[0].getComponent();
        int dimension = (temp.getLayoutInterval(HORIZONTAL) == intervals[0]) ? HORIZONTAL : VERTICAL;

        // Calculate extreme coordinates
        int leadingPosition = Short.MAX_VALUE;
        int trailingPosition = 0;
        int targetEffAlignment = LayoutConstants.DEFAULT;
        for (int i=0; i<intervals.length; i++) {
            LayoutInterval interval = intervals[i];
            
            // This method should be called only for components
            assert interval.isComponent();
            
            LayoutRegion region = interval.getCurrentSpace();
            int leading = region.positions[dimension][LEADING];
            int trailing = region.positions[dimension][TRAILING];
            leadingPosition = Math.min(leading, leadingPosition);
            trailingPosition = Math.max(trailing, trailingPosition);
            
            int effAlignment = LayoutInterval.getEffectiveAlignment(interval);
            if (((effAlignment == LEADING) || (effAlignment == TRAILING))
                && ((targetEffAlignment == DEFAULT) || (effAlignment == alignment))) {
                targetEffAlignment = effAlignment;
            }
        }
        
        boolean resizable = false;
        boolean sequenceResizable;
        boolean leadingGaps = true;
        boolean trailingGaps = true;
        List<LayoutInterval> gapsToResize = new LinkedList<LayoutInterval>();
        List<LayoutInterval> sequenceGapsToResize;
        LayoutInterval[] firstIntervals = new LayoutInterval[intervals.length];
        LayoutInterval[] lastIntervals = new LayoutInterval[intervals.length];
        
        // List of new sequence groups for individual intervals
        List<LayoutInterval> intervalList = Arrays.asList(intervals);
        List<List<LayoutInterval>> newSequences = new LinkedList<List<LayoutInterval>>();
        Map<LayoutInterval,Integer> gapSizes = new HashMap<LayoutInterval,Integer>();
        for (int i=0; i<intervals.length; i++) {
            LayoutInterval interval = intervals[i];
                
            // Find intervals that should be in the same sequence with the transfered interval
            List transferedComponents = transferedComponents(intervals, i, parParent);
            LayoutInterval firstInterval = null;
            LayoutInterval lastInterval = null;
            for (int j = transferedComponents.size()-1; j>=0; j--) {
                LayoutInterval trInterval = (LayoutInterval)transferedComponents.get(j);
                if (intervalList.contains(trInterval)) {
                    firstInterval = trInterval;
                    if (lastInterval == null) {
                        lastInterval = trInterval;
                    }
                } else if (alignment == CENTER) {
                    transferedComponents.remove(trInterval);
                }
            }
            firstIntervals[i] = firstInterval;
            lastIntervals[i] = lastInterval;
            
            // List of LayoutIntervals in the new sequence group
            List<LayoutInterval> newSequenceList = new LinkedList<LayoutInterval>();
            newSequences.add(newSequenceList);
            sequenceResizable = false;
            sequenceGapsToResize = new LinkedList<LayoutInterval>();
            Iterator iter = transferedComponents.iterator();
            
            // Determine leading gap of the sequence
            LayoutRegion parentRegion = parParent.getCurrentSpace();
            LayoutInterval leadingInterval = (LayoutInterval)iter.next();
            LayoutRegion leadingRegion = leadingInterval.getCurrentSpace();
            if ((alignment == TRAILING) && !closed) {
                int preGap = leadingRegion.positions[dimension][LEADING]
                    - parentRegion.positions[dimension][LEADING];
                LayoutInterval gapInterval = LayoutInterval.getNeighbor(leadingInterval, LEADING, false, true, false);
                leadingGaps = leadingGaps && (preGap != 0);
                if ((gapInterval != null) && gapInterval.isEmptySpace() && parParent.isParentOf(gapInterval)
                    && (LayoutInterval.getCurrentSize(gapInterval, dimension) == preGap)) {
                    LayoutInterval gap = cloneGap(gapInterval);
                    newSequenceList.add(gap);
                    gapSizes.put(gap, new Integer(preGap));
                    if (alignment == TRAILING) {
                        sequenceResizable = sequenceResizable || LayoutInterval.canResize(gap);
                    }
                } else {
                    maybeAddGap(newSequenceList, preGap, true);
                    if ((preGap != 0) && (alignment == TRAILING) && (leadingInterval == firstInterval)) {
                        LayoutInterval gap = newSequenceList.get(newSequenceList.size() - 1);
                        if (LayoutInterval.getEffectiveAlignment(leadingInterval) == TRAILING) {
                            layoutModel.setIntervalSize(gap, USE_PREFERRED_SIZE, preGap, USE_PREFERRED_SIZE);
                            sequenceGapsToResize.add(gap);
                        }
                    }
                }
            }
            
            // Determine content of the sequence
            boolean afterDefiningInterval = false;
            newSequenceList.add(leadingInterval);
            while (iter.hasNext()) {
                if (leadingInterval == interval) {
                    afterDefiningInterval = true;
                }
                LayoutInterval trailingInterval = (LayoutInterval)iter.next();
                if (((alignment == TRAILING) && (!afterDefiningInterval || (leadingInterval == interval)))
                    || ((alignment == LEADING) && afterDefiningInterval)) {
                    sequenceResizable = sequenceResizable || LayoutInterval.canResize(leadingInterval);
                }
                
                // Determine gap between before the processed interval
                LayoutRegion trailingRegion = trailingInterval.getCurrentSpace();
                LayoutInterval gapInterval = LayoutInterval.getNeighbor(leadingInterval, TRAILING, false, true, false);
                int gapSize = trailingRegion.positions[dimension][LEADING]
                    - leadingRegion.positions[dimension][TRAILING];
                boolean gapFound = false;
                if (gapInterval.isEmptySpace()) {
                    LayoutInterval neighbor = LayoutInterval.getNeighbor(gapInterval, TRAILING, false, true, false);
                    if (neighbor == trailingInterval) {
                        gapFound = true;
                        LayoutInterval gap = cloneGap(gapInterval);
                        newSequenceList.add(gap);
                        gapSizes.put(gap, new Integer(gapSize));
                        if (((alignment == TRAILING) && !afterDefiningInterval)
                            || ((alignment == LEADING) && afterDefiningInterval)) {
                            sequenceResizable = sequenceResizable || LayoutInterval.canResize(gap);
                        }
                    }
                }
                if (!gapFound) {
                    maybeAddGap(newSequenceList, gapSize, (alignment == CENTER));
                }
                if (((leadingInterval == lastInterval) && (alignment == LEADING)
                      && (LayoutInterval.getEffectiveAlignment(trailingInterval) == TRAILING))
                    || ((trailingInterval == firstInterval) && (alignment == TRAILING))
                      && (LayoutInterval.getEffectiveAlignment(leadingInterval) == LEADING)) {
                    LayoutInterval gap = newSequenceList.get(newSequenceList.size() - 1);
                    if (!LayoutInterval.canResize(gap)) {
                        sequenceGapsToResize.add(gap);
                    }
                }
                
                newSequenceList.add(trailingInterval);
                leadingInterval = trailingInterval;
                leadingRegion = trailingRegion;
            }
            
            // Determine trailing gap of the sequence
            if ((alignment == LEADING) || ((alignment == TRAILING) && (leadingInterval == lastInterval))) {
                sequenceResizable = sequenceResizable || LayoutInterval.canResize(leadingInterval);
            }
            if ((alignment == LEADING) && !closed) {
                int postGap = parentRegion.positions[dimension][TRAILING]
                    - leadingRegion.positions[dimension][TRAILING];
                trailingGaps = trailingGaps && (postGap != 0);
                LayoutInterval gapInterval = LayoutInterval.getNeighbor(leadingInterval, TRAILING, false, true, false);
                if ((gapInterval != null) && gapInterval.isEmptySpace() && parParent.isParentOf(gapInterval)
                    && (LayoutInterval.getCurrentSize(gapInterval, dimension) == postGap)) {
                    LayoutInterval gap = cloneGap(gapInterval);
                    newSequenceList.add(gap);
                    gapSizes.put(gap, new Integer(postGap));
                    if (alignment == LEADING) {
                        sequenceResizable = sequenceResizable || LayoutInterval.canResize(gap);
                    }
                } else {
                    maybeAddGap(newSequenceList, postGap, true);
                }
            }
            resizable = resizable || sequenceResizable;
            if (!sequenceResizable) {
                gapsToResize.addAll(sequenceGapsToResize);
            }
        }
        
        // Modify transfered gaps adjacent to aligned components
        if (alignment != CENTER) {
            Iterator listIter = newSequences.iterator();
            for (int i=0; i<intervals.length; i++) {
                List newSequenceList = (List)listIter.next();
                Iterator iter = newSequenceList.iterator();
                LayoutInterval gapCandidate = null;
                while (iter.hasNext()) {
                    LayoutInterval interval = (LayoutInterval)iter.next();
                    if (((interval == firstIntervals[i]) && (alignment == LEADING))
                        || ((interval == lastIntervals[i]) && (alignment == TRAILING))) {
                        LayoutRegion region = interval.getCurrentSpace();
                        int diff = 0;
                        if (alignment == TRAILING) {
                            if (iter.hasNext()) {
                                gapCandidate = (LayoutInterval)iter.next();
                                diff = trailingPosition - region.positions[dimension][TRAILING];
                            } else {
                                break;
                            }
                        } else {
                            diff = region.positions[dimension][LEADING] - leadingPosition;
                        }
                        if ((gapCandidate != null) && (gapCandidate.isEmptySpace())) {
                            if ((!leadingGaps && (alignment == LEADING) && (newSequenceList.indexOf(gapCandidate) == 0))
                                || (!trailingGaps && (alignment == TRAILING) && !iter.hasNext())) {
                                newSequenceList.remove(gapCandidate);
                            } else {
                                Integer size = gapSizes.get(gapCandidate);
                                int minSize = gapCandidate.getMinimumSize();
                                int prefSize = gapCandidate.getPreferredSize();
                                int maxSize = gapCandidate.getMaximumSize();
                                if (diff > 0) {
                                    if (size != null) {
                                        int actualSize = size.intValue();
                                        diff += prefSize - actualSize;
                                    }
                                    if (minSize >= 0) {
                                        minSize = (minSize - diff > 0) ? minSize - diff : NOT_EXPLICITLY_DEFINED;
                                    }
                                    if (prefSize >= 0) {
                                        prefSize = (prefSize - diff > 0) ? prefSize - diff : NOT_EXPLICITLY_DEFINED;
                                    }
                                    if ((maxSize >= 0) && (maxSize != Short.MAX_VALUE)) {
                                        maxSize = (maxSize - diff > 0) ? maxSize - diff : USE_PREFERRED_SIZE;
                                    }                            
                                }
                                if ((targetEffAlignment == alignment) && (maxSize == Short.MAX_VALUE)) {
                                    maxSize = USE_PREFERRED_SIZE;
                                }
                                layoutModel.setIntervalSize(gapCandidate, minSize, prefSize, maxSize);
                            }
                        }
                        break;
                    }
                    gapCandidate = interval;
                }
            }
        }

        // The content of all new sequence groups is known.
        // We can update the layout model.
        Iterator listIter = newSequences.iterator();
        while (listIter.hasNext()) {
            List newSequenceList = (List)listIter.next();
            LayoutInterval newSequence = new LayoutInterval(SEQUENTIAL);
            if (alignment == CENTER) {
                newSequence.setAlignment(CENTER);
            }
            Iterator iter = newSequenceList.iterator();
            int sequenceAlignment = DEFAULT;
            while (iter.hasNext()) {
                LayoutInterval compInterval = (LayoutInterval)iter.next();
                if (compInterval.isComponent()) { // e.g. compInterval.getParent() != null
                    if (sequenceAlignment == DEFAULT) {
                        sequenceAlignment = LayoutInterval.getEffectiveAlignment(compInterval);
                    }
                    designer.takeOutInterval(compInterval, parParent);
                    layoutModel.setIntervalAlignment(compInterval, DEFAULT);
                }
                layoutModel.addInterval(compInterval, newSequence, -1);
            }
            if ((alignment != CENTER) && !LayoutInterval.wantResize(newSequence)) {
                newSequence.setAlignment(sequenceAlignment);
            }
            if (newSequenceList.size() == 1) {
                LayoutInterval compInterval = (LayoutInterval)newSequenceList.get(0);
                layoutModel.removeInterval(compInterval);
                if (newSequence.getAlignment() != DEFAULT) {
                    layoutModel.setIntervalAlignment(compInterval, newSequence.getAlignment());
                }
                newSequence = compInterval;
            }
            layoutModel.addInterval(newSequence, parParent, -1);
        }
        if (alignment == CENTER) {
            layoutModel.setGroupAlignment(parParent, alignment);
        }
        
        // Check resizability
        if ((gapsToResize.size() > 0) && !resizable && (alignment != CENTER)) {
            operations.suppressGroupResizing(parParent);
        }
        return gapsToResize;
    }

    /** PENDING
     * Determines layout components that will be transfered to the specified
     * parallel parent together with the given layout component.
     *
     * @param interval layout component to transfer to the parallel parent.
     * @param parParent parallel parent to transfer the component to.
     * @return <code>List</code> of <code>LayoutInterval</code> objects.
     */
    private List<LayoutInterval> transferedComponents(LayoutInterval[] intervals, int index, LayoutInterval parParent) {
        LayoutInterval interval = intervals[index];
        LayoutInterval oppInterval = oppositeComponentInterval(interval);
        List<LayoutInterval> transferedComponents = new LinkedList<LayoutInterval>();
        List<LayoutInterval> components = new LinkedList<LayoutInterval>();
        componentsInGroup(parParent, components);
        /*for (int i=0; i<intervals.length; i++) {
            if (i == index) continue;
            transferCandidates(interval, intervals[i], components);
        }*/
        Iterator iter = components.iterator();
        while (iter.hasNext()) {
            LayoutInterval candidate = (LayoutInterval)iter.next();
            LayoutInterval oppCandidate = oppositeComponentInterval(candidate);
            if (alignedIntervals(oppInterval, oppCandidate, BASELINE)
                || alignedIntervals(oppInterval, oppCandidate, LEADING)
                || alignedIntervals(oppInterval, oppCandidate, TRAILING)
                || alignedIntervals(oppInterval, oppCandidate, CENTER)) {
                if (parParent.isParentOf(candidate)) {
                    transferedComponents.add(candidate);
                }
            }
        }
        if (!transferedComponents.contains(interval)) {
            transferedComponents.add(interval);
        }

        // Sort layout components according to their current bounds
        transferedComponents.sort(new Comparator<LayoutInterval>() {
            @Override
            public int compare(LayoutInterval interval1, LayoutInterval interval2) {
                LayoutComponent comp = interval1.getComponent();
                int dimension = (comp.getLayoutInterval(VERTICAL) == interval1)
                    ? VERTICAL : HORIZONTAL;
                LayoutRegion region1 = interval1.getCurrentSpace();
                LayoutRegion region2 = interval2.getCurrentSpace();
                int value1 = region1.positions[dimension][LEADING];
                int value2 = region2.positions[dimension][LEADING];
                return (value1 - value2);
            }
        });
        return transferedComponents;
    }

    /**
     * Places all components that are in the <code>group</code>
     * into <code>components</code> collection.
     *
     * @param group layout interval that is scanned for components.
     * @param components collection of <code>LayoutInterval</code>s
     * of layout components in the group.
     */
    private void componentsInGroup(LayoutInterval group, Collection<LayoutInterval> components) {
        Iterator iter = group.getSubIntervals();
        while (iter.hasNext()) {
            LayoutInterval interval = (LayoutInterval)iter.next();
            if (interval.isGroup()) {
                componentsInGroup(interval, components);
            } else if (interval.isComponent() && !components.contains(interval)) {
                components.add(interval);
            }
        }
    }

    /**
     * Aligns given intervals to a parallel group. The intervals are supposed
     * to have the same first parallel parent.
     */
    private boolean align(LayoutInterval[] leadingInts, LayoutInterval[] trailingInts, boolean closed, int dimension, int alignment) {
        // find common parallel group for aligned intervals
        LayoutInterval commonGroup = null;
        LayoutInterval[] intervals = leadingInts;
        for (int i=0; i < intervals.length; i++) {
            LayoutInterval interval = intervals[i];
            LayoutInterval parent = interval.getParent();
            if (!parent.isParallel()) {
                parent = parent.getParent();
                assert parent.isParallel();
            }
            if (commonGroup == null || (parent != commonGroup && parent.isParentOf(commonGroup))) {
                commonGroup = parent;
            }
            else {
                assert parent == commonGroup || commonGroup.isParentOf(parent);
            }
        }

        // prepare separation to groups
        List<LayoutInterval> aligned = new LinkedList<LayoutInterval>();
        List<List> restLeading = new LinkedList<List>();
        List<List> restTrailing = new LinkedList<List>();
        int mainEffectiveAlign = -1;
        int originalCount = commonGroup.getSubIntervalCount();

        for (int i=0; i < intervals.length; i++) {
            LayoutInterval interval = intervals[i];
            LayoutInterval parent = interval.getParent();
            LayoutInterval parParent = parent.isParallel() ? parent : parent.getParent();
            if (parParent != commonGroup) {
                interval = getAlignSubstitute(interval, commonGroup, alignment);
                if (interval == null) {
                    return false; // cannot align
                }
                parent = interval.getParent();
            }

            if (parent.isSequential()) {
                mainEffectiveAlign = LayoutInterval.getEffectiveAlignment(interval); // [need better way to collect - here it takes the last one...]

                // extract the interval surroundings
                int extractCount = operations.extract(interval, closed ? trailingInts[i] : interval, alignment, closed,
                                                      restLeading, restTrailing);
                if (extractCount == 1) { // the parent won't be reused
                    layoutModel.removeInterval(parent);
                    aligned.add(interval);
                }
                else { // we'll reuse the parent sequence in the new group
                    aligned.add(parent);
                }
            }
            else {
                aligned.add(interval);
            }
        }

        // prepare the group where the aligned intervals will be placed
        LayoutInterval group;
        LayoutInterval commonSeq;
        boolean remainder = !restLeading.isEmpty() || !restTrailing.isEmpty();

        if ((!remainder && mainEffectiveAlign == alignment)
            || (aligned.size() == originalCount
                && commonGroup.getParent() != null))
        {   // reuse the original group - avoid unnecessary nesting
            group = commonGroup;
            if (remainder) { // need a sequence for the remainder groups
                LayoutInterval groupParent = group.getParent();
                if (groupParent.isSequential()) {
                    commonSeq = groupParent;
                }
                else { // insert a new one
                    int index = layoutModel.removeInterval(group);
                    commonSeq = new LayoutInterval(SEQUENTIAL);
                    commonSeq.setAlignment(group.getAlignment());
                    layoutModel.addInterval(commonSeq, groupParent, index);
                    layoutModel.setIntervalAlignment(group, DEFAULT);
                    layoutModel.addInterval(group, commonSeq, -1);
                }
            }
            else commonSeq = null;
        }
        else { // need to create a new group
            group = new LayoutInterval(PARALLEL);
            if (remainder) { // need a new sequence for the remainder groups
                commonSeq = new LayoutInterval(SEQUENTIAL);
                commonSeq.add(group, -1);
                layoutModel.addInterval(commonSeq, commonGroup, -1);
            }
            else {
                commonSeq = null;
                layoutModel.addInterval(group, commonGroup, -1);
            }
            layoutModel.setGroupAlignment(group, alignment);
        }

        // add the intervals and their neighbors to the main aligned group
        // [need to fix the resizability (fill) and compute effective alignment]
        for (Iterator<LayoutInterval> it=aligned.iterator(); it.hasNext(); ) {
            LayoutInterval interval = it.next();
            if (interval.getParent() != group) {
                layoutModel.removeInterval(interval);
                operations.addContent(interval, group, -1);
            }
            layoutModel.setIntervalAlignment(interval, alignment);
        }

        // create the remainder groups around the main one
        if (!restLeading.isEmpty()) {
            // [should change to operations.addGroupContent]
            designer.createRemainderGroup(restLeading, commonSeq, commonSeq.indexOf(group), LEADING, mainEffectiveAlign, dimension);
        }
        if (!restTrailing.isEmpty()) {
            // [should change to operations.addGroupContent]
            designer.createRemainderGroup(restTrailing, commonSeq, commonSeq.indexOf(group), TRAILING, mainEffectiveAlign, dimension);
        }

        return true;
    }

    /**
     * Clones given layout interval (empty space).
     *
     * @return clone of the given layout interval (empty space).
     */
    private LayoutInterval cloneGap(LayoutInterval interval) {
        assert interval.isEmptySpace();
        LayoutInterval gap = new LayoutInterval(SINGLE);
        gap.setMinimumSize(interval.getMinimumSize());
        gap.setPreferredSize(interval.getPreferredSize());
        gap.setMaximumSize(interval.getMaximumSize());
        return gap;
    }

    /**
     * Helper method that adds layout interval (empty space) to the given
     * list (when the specified size is positive).
     *
     * @param list list the gap should be added to.
     * @param size size of the original space.
     */
    private void maybeAddGap(List<LayoutInterval> list, int size, boolean forceSize) {
        if (size > 0) {
            LayoutInterval gapInterval = new LayoutInterval(SINGLE);
            if (forceSize) {
                layoutModel.setIntervalSize(gapInterval, size, size, size);
            }
            list.add(gapInterval);
        }
    }

    private static boolean compatibleGroupAlignment(int groupAlign, int align) {
        return groupAlign == align
               || ((groupAlign == LEADING || groupAlign == TRAILING)
                   && (align == LEADING || align == TRAILING || align == DEFAULT));
    }

    private static boolean alignedIntervals(LayoutInterval interval1, LayoutInterval interval2, int alignment) {
        LayoutInterval commonParent;
        LayoutInterval otherInterval;
        if (interval1.isParentOf(interval2)) {
            commonParent = interval1;
            otherInterval = interval2;
        }
        else if (interval2.isParentOf(interval1)) {
            commonParent = interval2;
            otherInterval = interval1;
        }
        else {
            commonParent = interval1.getParent();
            while (commonParent != null) {
                if (!hasAlignmentInParent(interval1, alignment)) {
                    return false;
                }
                if (commonParent.isParentOf(interval2)) {
                    break;
                }
                interval1 = commonParent;
                commonParent = interval1.getParent();
            }
            if (commonParent == null) {
                return false;
            }
            otherInterval = interval2;
        }

        do {
            if (!hasAlignmentInParent(otherInterval, alignment)) {
                return false;
            }
            otherInterval = otherInterval.getParent();
        }
        while (otherInterval != commonParent);
        return true;
    }

    private static boolean hasAlignmentInParent(LayoutInterval interval, int alignment) {
        LayoutInterval parent = interval.getParent();
        if (parent.isSequential()) {
            if (alignment == LEADING) {
                return parent.getSubInterval(0) == interval;
            }
            if (alignment == TRAILING) {
                return parent.getSubInterval(parent.getSubIntervalCount()-1) == interval;
            }
            return false;
        }
        else { // parallel group
            assert interval.getAlignment() != alignment || compatibleGroupAlignment(parent.getGroupAlignment(), alignment);
            return interval.getAlignment() == alignment
                   || LayoutInterval.wantResize(interval);
        }
    }

    private static LayoutInterval getAlignSubstitute(LayoutInterval toAlignWith, LayoutInterval commonParParent, int alignment) {
        assert alignment == LEADING || alignment == TRAILING;

        while (toAlignWith != null && LayoutInterval.getFirstParent(toAlignWith, PARALLEL) != commonParParent) {
            if (LayoutInterval.isAlignedAtBorder(toAlignWith, alignment)) {
                toAlignWith = toAlignWith.getParent();
            }
            else return null;
        }
        return toAlignWith;
    }

    /**
     * Returns layout interval for the opposite dimension for the given
     * layout interval of a layout component.
     *
     * @param interval layout interval of some layout component.
     * @return layout interval for the opposite dimension for the given
     * layout interval of a layout component.
     */
    private LayoutInterval oppositeComponentInterval(LayoutInterval interval) {
        assert interval.isComponent();
        LayoutComponent component = interval.getComponent();
        int oppDimension = (component.getLayoutInterval(HORIZONTAL) == interval)
            ? VERTICAL : HORIZONTAL;
        return component.getLayoutInterval(oppDimension);
    }

    private int shortenGap(LayoutInterval gap, int delta) {
        assert gap.isEmptySpace();
        int prefSize = gap.getPreferredSize();
        if (prefSize == NOT_EXPLICITLY_DEFINED) {
            return 0; // don't shorten paddings
        } else {
            int newPref = prefSize - delta;
            newPref = (newPref > 0) ? newPref : NOT_EXPLICITLY_DEFINED; // Use padding
            if (LayoutInterval.canResize(gap)) {
                layoutModel.setIntervalSize(gap, NOT_EXPLICITLY_DEFINED, newPref, Short.MAX_VALUE);
            } else {
                layoutModel.setIntervalSize(gap, USE_PREFERRED_SIZE, newPref, USE_PREFERRED_SIZE);
            }
            return (prefSize > delta) ? delta : prefSize;
        }
    }

    private void returnRemovedIntervals(LayoutInterval parParent, List removed, int dimension) {
        LayoutRegion parRegion = parParent.getCurrentSpace();
        Iterator iter = removed.iterator();
        while (iter.hasNext()) {
            LayoutInterval interval = (LayoutInterval)iter.next();
            LayoutRegion region = interval.getCurrentSpace();
            int pre = Math.max(0, region.positions[dimension][LEADING] - parRegion.positions[dimension][LEADING]);
            int post = Math.max(0, parRegion.positions[dimension][TRAILING] - region.positions[dimension][TRAILING]);
            if (((pre != 0) || (post != 0)) && !interval.isSequential()) {
                LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
                if (interval.getAlignment() != BASELINE)
                    layoutModel.setIntervalAlignment(seq, interval.getAlignment());
                layoutModel.setIntervalAlignment(interval, DEFAULT);
                layoutModel.addInterval(interval, seq, -1);
                interval = seq;
            }
            
            // interval.isSequence() by now - remove boundary empty spaces
            if (pre != 0) {
                LayoutInterval first = interval.getSubInterval(0);
                if (first.isEmptySpace()) {
                    layoutModel.removeInterval(first);
                    region = interval.getSubInterval(0).getCurrentSpace();
                    pre = Math.max(0, region.positions[dimension][LEADING] - parRegion.positions[dimension][LEADING]);
                }
            }
            if (post != 0) {
                LayoutInterval last = interval.getSubInterval(interval.getSubIntervalCount()-1);
                if (last.isEmptySpace()) {
                    layoutModel.removeInterval(last);
                    region = interval.getSubInterval(interval.getSubIntervalCount()-1).getCurrentSpace();
                    post = Math.max(0, parRegion.positions[dimension][TRAILING] - region.positions[dimension][TRAILING]);
                }
            }
            
            // Insert new empty spaces
            if (pre != 0) {
                LayoutInterval gap = new LayoutInterval(SINGLE);
                gap.setSize(pre);
                if (interval.getAlignment() == TRAILING) operations.setIntervalResizing(gap, true);
                layoutModel.addInterval(gap, interval, 0);
            }
            if (post != 0) {
                LayoutInterval gap = new LayoutInterval(SINGLE);
                gap.setSize(post);
                if (interval.getAlignment() == LEADING) operations.setIntervalResizing(gap, true);
                layoutModel.addInterval(gap, interval, -1);
            }
            
            // Insert into parParent
            layoutModel.addInterval(interval, parParent, -1);
        }
    }

    private void setAlignmentAccordingEffectiveAlignment(LayoutInterval aligned, LayoutInterval interval) {
        if (interval == null) return;
        int alignment = LayoutInterval.getEffectiveAlignment(interval);
        if ((alignment == LEADING) || (alignment == TRAILING)) {
            layoutModel.setIntervalAlignment(aligned, alignment);
        }
    }

}
