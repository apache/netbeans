/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.form.layoutdesign;

import java.awt.*;
import java.util.*;
import java.util.List;
import static org.netbeans.modules.form.layoutdesign.VisualState.GapInfo;

/*
Finding position procedure [sort of out of date]:
- find vertical position first - preferring aligned position
- find horizontal position - preferring derived (next to) position

finding position in a dimension:
- find best derived positions for each alignment (L, T):
  - go down from the root, looking for distances (opposite edges)
  - exclude elements not overlapping in the other dimension
  - if a distance is smaller than SNAP_DISTANCE, compare with the best position so far:
    - better position is that with better alignment in the opposite dimension
      - i.e. has smallest alignment distance (any)
    - otherwise the distance must be closer
    - otherwise the lower interval in hierarchy is preferred (given that
      it is visited only if the position is inside higher)
- find best aligned positions for each alignment (L, T, C, B):
  - go down from the root, looking for distances (same edges)
  - check only component elements
  - if a distance is smaller than SNAP_DISTANCE, compare with the best position so far:
    - closer distance is better
    - otherwise the distance in the other dimension must be closer
- choose the best position
  - the unpreferred position must have twice smaller distance, and be better
    by at least SNAP_DISTANCE/2
*/

final class LayoutDragger implements LayoutConstants {

    private VisualMapper visualMapper;

    private LayoutPainter layoutPainter;

    // fixed (initial) parameters of the operation ---

    // type of the operation, one of ADDING, MOVING, RESIZING
    private int operation;
    private static final int ADDING = 0;
    private static final int MOVING = 1;
    private static final int RESIZING = 2;

    // components being moved
    private LayoutComponent[] movingComponents;

    // for each dimension defines what component edges are to be moved:
    //   - LayoutRegion.ALL_POINTS means whole component is moved
    //   - LEADING or TRAILING means the component is resized
    //   - LayoutRegion.NO_POINT means the component is not changed in given dimension
    private int movingEdges[]; // array according to dimension (HORIZONTAL or VERTICAL)

    // indicates whether the operation is resizing - according to movingEdges
//    private boolean resizing;

    // initial components' positions and sizes (defines the formation to move)
    private LayoutRegion[] movingFormation;

    // initial mouse cursor position (when moving/resizing starts)
    private int[] startCursorPosition;

    private SizeDef[] sizing;

    // when resizing a gap directly (not resizing a component)
    private GapInfo resizingGap;

    // parameters changed with each move step ---

    // last mouse cursor position
    private int[] lastCursorPosition = new int[] { LayoutRegion.UNKNOWN, LayoutRegion.UNKNOWN };

    // direction of mouse move from last position
    private int[] moveDirection = new int[] { LEADING, LEADING };

    // dimension that is temporarily locked (kept unchanged)
    private int lockedDimension = -1;

    // container the components are being moved in/over
    private LayoutComponent targetContainer;

    // relevant root intervals where the moving/resizing components are targeted to
    private LayoutInterval[] targetRoots;

    // actual components' bounds of moving/resizing components
    private LayoutRegion[] movingBounds;

    // last found positions for the moving/resizing component
    private PositionDef[] bestPositions = new PositionDef[DIM_COUNT];
    
    // determines whether the dragged components can snap to baseline
    private boolean canSnapToBaseline;

    // the following fields hold various parameters used during the procedure
    // of finding a suitable position for the moving/resizing components 
    // (fields are used not to need to pass the parameters through all the
    // methods again and again and to avoid repetitive creation of arrays) ---

    private LayoutRegion movingSpace;
    private int dimension;
    private boolean snapping;

    // arrays of position candidates for the moving/resizing component
    private PositionDef[][] findingsNextTo;
    private PositionDef[][] findingsAligned;

    // constants ---

    static final int[] ALL_EDGES = { LayoutRegion.ALL_POINTS,
                                     LayoutRegion.ALL_POINTS };

    // length of tips of painted guiding lines
    private static final int GL_TIP = 10;

    // distance in which components are drawn to guiding line
    private static final int SNAP_DISTANCE = 8;

    // max. orthogonal distance from a component to be still recognized as "next to"
    private static final int ORT_DISTANCE = 8;
    
    private static BasicStroke dashedStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_MITER, 5.0f, new float[]{5.0f, 2.0f}, 0.0f);

    // -----
    // setup

    // Constructor for component dragging (moving, resizing).
    LayoutDragger(LayoutComponent[] comps,
                  LayoutRegion[] compBounds,
                  int[] initialCursorPos,
                  int[] movingEdges,
                  VisualMapper mapper,
                  LayoutPainter painter)
    {
        for (int i=0; i < DIM_COUNT; i++) {
            if (movingEdges[i] == LEADING || movingEdges[i] == TRAILING) {
                operation = RESIZING;
                break;
            }
        }
        if (operation != RESIZING) {
            operation = comps[0].getParent() == null ? ADDING : MOVING;
        }

        this.movingComponents = comps;
        this.movingFormation = compBounds;
        this.startCursorPosition = initialCursorPos;
        this.movingEdges = movingEdges;
        visualMapper = mapper;
        layoutPainter = painter;

        movingBounds = new LayoutRegion[compBounds.length];
        movingSpace = new LayoutRegion(); // TODO why here? it's set again in move
        for (int i=0; i < compBounds.length; i++) {
            movingBounds[i] = new LayoutRegion(); // TODO why here? it's set again in move
            movingSpace.expand(compBounds[i]);
        }

        // set canSnapToBaseline field
        if (comps.length == 1) {
            canSnapToBaseline = true; // real baseline position will be checked on the first move
        } else {
            LayoutInterval parent = comps[0].getLayoutInterval(VERTICAL).getParent();
            for (int i=1; i < comps.length; i++) {
                if (comps[i].getLayoutInterval(VERTICAL).getParent() != parent) {
                    parent = null;
                    break;
                }
            }
            canSnapToBaseline = (parent != null) && (parent.getGroupAlignment() == BASELINE);
        }

        findingsNextTo = new PositionDef[DIM_COUNT][];
        findingsAligned = new PositionDef[DIM_COUNT][];
        for (int i=0; i < DIM_COUNT; i++) {
            int n = LayoutRegion.POINT_COUNT[i];
            findingsNextTo[i] = new PositionDef[n];
            findingsAligned[i] = new PositionDef[n];
            for (int j=0; j < n; j++) {
                findingsNextTo[i][j] = new PositionDef();
                findingsAligned[i][j] = new PositionDef();
            }
        }

        if (operation == RESIZING) {
            sizing = new SizeDef[DIM_COUNT];
            LayoutComponent comp = movingComponents[0]; // [limitation: only one component can be resized]
            LayoutRegion space = movingFormation[0];
            java.awt.Dimension prefSize = comp.isLayoutContainer()
                    ? visualMapper.getComponentMinimumSize(comp.getId())
                    : visualMapper.getComponentPreferredSize(comp.getId());
            for (int i=0; i < DIM_COUNT; i++) {
                if (isResizing(i)) {
                    SizeDef sizeDef = new SizeDef();
                    sizing[i] = sizeDef;
                    sizeDef.originalSize = space.size(i);
                    sizeDef.minSize = i == HORIZONTAL ? prefSize.width : prefSize.height;
                    if (comp.isLayoutContainer()) {
                        // [TODO: ideally the resizing gap should be handled for each layer]
                        sizeDef.contPrefSize = sizeDef.originalSize
                                - LayoutInterval.getDiffToDefaultSize(comp.getLayoutInterval(i), true);
                        LayoutInterval resGap = findContainerResizingGap(comp.getDefaultLayoutRoot(i));
                        if (resGap != null) {
                            sizeDef.adjustingGap = resGap;
                            sizeDef.originalGapSize = LayoutInterval.getCurrentSize(resGap, i);
                            sizeDef.defaultGapSize = LayoutUtils.getSizeOfDefaultGap(resGap, visualMapper);
                            if (LayoutInterval.canResize(resGap)) {
                                sizeDef.minSizeWithDefaultGap = sizeDef.minSize;
                                if (isZeroResizingGap(resGap)) {
                                    sizeDef.minSizeWithZeroGap = sizeDef.originalSize - sizeDef.originalGapSize;
                                }
                            } else {
                                sizeDef.minSizeWithDefaultGap = sizeDef.minSize - sizeDef.originalGapSize + sizeDef.defaultGapSize;
                                if (isZeroResizingGap(resGap)) {
                                    sizeDef.minSizeWithZeroGap = sizeDef.minSize - sizeDef.originalGapSize;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Constructor for gap resizing (no component manipulation).
    LayoutDragger(GapInfo gapToResize, int resizingEdge, int[] initialCursorPos,
                  VisualMapper mapper, LayoutPainter painter) {
        resizingGap = gapToResize;
        dimension = gapToResize.dimension;
        movingEdges = new int[DIM_COUNT];
        for (int dim=0; dim < DIM_COUNT; dim++) {
            movingEdges[dim] = dim == dimension ? resizingEdge : LayoutRegion.NO_POINT;
        }
        operation = RESIZING;
        startCursorPosition = initialCursorPos;
        visualMapper = mapper;
        layoutPainter = painter;
        LayoutRegion gapSpace = new LayoutRegion();
        gapSpace.set(gapToResize.paintRect, -1);
        gapSpace.set(dimension, gapToResize.position, gapToResize.position+gapToResize.currentSize);
        movingFormation = new LayoutRegion[] { gapSpace };
        movingSpace = new LayoutRegion(gapSpace);
        bestPositions[dimension] = new PositionDef();
        bestPositions[dimension].alignment = resizingEdge;
    }

    private LayoutInterval findContainerResizingGap(LayoutInterval group) {
        for (Iterator it=group.getSubIntervals(); it.hasNext(); ) {
            LayoutInterval li = (LayoutInterval) it.next();
            if (li.isEmptySpace() && li.hasAttribute(LayoutInterval.ATTR_DESIGN_CONTAINER_GAP)) {
                return li;
            }
            else if (li.isGroup()) {
                LayoutInterval gap = findContainerResizingGap(li);
                if (gap != null) {
                    return gap;
                }
            }
        }
        return null;
    }

    private static boolean isZeroResizingGap(LayoutInterval gap) {
        return LayoutInterval.getNeighbor(gap, TRAILING, false, true, false) == null;
    }

    void setTargetContainer(LayoutComponent container, LayoutInterval[] roots) {
        if (container != targetContainer) {
            bestPositions[HORIZONTAL] = bestPositions[VERTICAL] = null;
        }
        targetContainer = container;
        targetRoots = roots;
    }

    LayoutComponent getTargetContainer() {
        return targetContainer;
    }

    LayoutInterval[] getTargetRoots() {
        return targetRoots;
    }

    boolean isResizing() {
        return operation == RESIZING;
    }

    boolean isResizing(int dim) {
        return movingEdges[dim] == LEADING || movingEdges[dim] == TRAILING;
    }

    int getResizingEdge(int dim) {
        return movingEdges[dim];
    }

    LayoutComponent[] getMovingComponents() {
        return movingComponents;
    }

    GapInfo getResizingGap() {
        return resizingGap;
    }

    VisualMapper getVisualMapper() {
        return visualMapper;
    }

    private boolean isComponentOperation() {
        return movingComponents != null && resizingGap == null;
    }

//    private boolean isResizingGapOperation() {
//        return operation == RESIZING && resizingGap != null;
//    }

    // -----
    // results

    LayoutRegion[] getMovingBounds() {
        return movingBounds;
    }
    
    LayoutRegion getMovingSpace() {
        return movingSpace;
    }

    PositionDef[] getPositions() {
/*        for (dimension=0; dimension < DIM_COUNT; dimension++) {
            if (movingEdges[dimension] != LayoutRegion.NO_POINT) {
                PositionDef best = bestPositions[dimension];
                if (best == null && !isResizing(dimension)) { // not found, retry without position restriction
                    snapping = false;
                    findBestPosition();
                }
            }
        } */
        return bestPositions;
    }

    SizeDef[] getSizes() {
        return sizing;
    }

    boolean componentSnappedToDefaultSize(int dimension) {
        if (isResizing(dimension) && bestPositions[dimension] == null) {
            int size = movingSpace.size(dimension);
            SizeDef sizeDef = sizing[dimension];
            return size == sizeDef.minSize || size == sizeDef.contPrefSize
                    || size == sizeDef.minSizeWithDefaultGap || size == sizeDef.minSizeWithZeroGap;
        }
        return false;
    }

    // -----
    // moving & painting

    void move(int[] cursorPos, boolean autoPositioning, boolean lockDimension) {
        // translate mouse cursor position, compute move direction, ...
        int lockCandidate = -1; // dimension that might be locked if there's aligned position
        int minDelta = Integer.MAX_VALUE;
        for (int i=0; i < DIM_COUNT; i++) {
            cursorPos[i] -= startCursorPosition[i]; // translate to diff from the initial state
            int currentPos = cursorPos[i];
            int lastPos = lastCursorPosition[i];
            lastCursorPosition[i] = currentPos;
            if (lastPos == LayoutRegion.UNKNOWN) { // first move step, can't calc direction
                lockDimension = false; // can't lock yet
            }
            else {
                int delta = currentPos - lastPos;
                if (delta != 0) { // position changed in this direction
                    moveDirection[i] = delta > 0 ? TRAILING : LEADING;
                }
                if (movingEdges[i] != LayoutRegion.ALL_POINTS) {
                    lockDimension = false; // can't lock - this is not pure moving
                }
                else if (lockedDimension < 0) { // not locked yet
                    PositionDef pos = bestPositions[i];
                    if (pos != null && !pos.nextTo && delta < minDelta) {
                        lockCandidate = i;
                        minDelta = delta;
                    }
                }
            }
        }

        snapping = autoPositioning;

        if (isComponentOperation()) {
            // check locked dimension
            if (lockDimension) {
                if (lockedDimension < 0) { // not set yet
                    lockedDimension = lockCandidate;
                }
            }
            else lockedDimension = -1;

            // compute actual position of the moving components
            for (int i=0; i < movingBounds.length; i++) {
                for (int j=0; j < DIM_COUNT; j++) {
                    if (j != lockedDimension) {
                        movingBounds[i].set(j, movingFormation[i]);
                        movingBounds[i].reshape(j, movingEdges[j], cursorPos[j]);
                    }
                    // for locked dimension the space is already set and not changing
                }
            }
            movingSpace = new LayoutRegion();
            for (int i=0; i<movingBounds.length; i++) {
                movingSpace.expand(movingBounds[i]);
            }
            if (canSnapToBaseline) { // awfull, but working
                int baselinePos = movingBounds[0].positions[VERTICAL][BASELINE];
                if (baselinePos > 0) {
                    movingSpace.positions[VERTICAL][BASELINE] = baselinePos;
                } else {
                    canSnapToBaseline = false;
                }
            }

            // reset findings
            for (int i=0; i < DIM_COUNT; i++) {
                if (i != lockedDimension) {
                    bestPositions[i] = null;
                    for (int j=0; j < LayoutRegion.POINT_COUNT[i]; j++) {
                        findingsNextTo[i][j].reset();
                        findingsAligned[i][j].reset();
                    }
                }
            }

            // find position in the layout
            if (autoPositioning) {
                // important: looking for vertical position first
                for (dimension=DIM_COUNT-1; dimension >= 0; dimension--) {
                    if (dimension != lockedDimension
                        && movingEdges[dimension] != LayoutRegion.NO_POINT) {
                        // look for a suitable position in this dimension
                        int snapDistance = moveComponents();
                        // snap effect
                        if (snapDistance != LayoutRegion.UNKNOWN) {
                            cursorPos[dimension] -= snapDistance;
                            for (LayoutRegion mb : movingBounds) {
                                mb.reshape(dimension, movingEdges[dimension], -snapDistance);
                            }
                            movingSpace.reshape(dimension, movingEdges[dimension], -snapDistance);
                        }
                    }
                }
            }
        } else if (resizingGap != null) {
            movingSpace.set(dimension, movingFormation[0]);
            movingSpace.reshape(dimension, movingEdges[dimension], cursorPos[dimension]);

            int snapDistance = resizeGap();

            if (snapDistance != 0) { // snap effect
                cursorPos[dimension] -= snapDistance;
                movingSpace.reshape(dimension, movingEdges[dimension], -snapDistance);
            }
        }

        // translate mouse cursor position back to absolute coordinates
        for (int i=0; i < DIM_COUNT; i++) {
            cursorPos[i] += startCursorPosition[i];
        }
    }

    void paintMoveFeedback(Graphics2D g) {
        if (isComponentOperation()) {
            paintComponents(g);
        } else if (resizingGap != null) {
            paintGap(g);
        }
    }

    private void paintComponents(Graphics2D g) {
        for (int i=0; i < DIM_COUNT; i++) {    
            LayoutDragger.PositionDef position = bestPositions[i];
            if (position != null) {
                boolean inRoot = (position.interval.getParent() == null);
                int dir = 1-i; // opposite direction
                int align = position.alignment;
                LayoutInterval interval = position.interval;
                LayoutInterval parent = interval.getParent();
                boolean parentUsed;
                do {
                    parentUsed = false;
                    if (parent != null && parent.isParallel()) {
                        // check if the interval alignment coincides with parent
                        if (align == LEADING || align == TRAILING) {
                            LayoutRegion parRegion = parent.getCurrentSpace();
                            if (!position.nextTo
                                && LayoutRegion.distance(parRegion, movingSpace, i, align, align) == 0)
                            {
                                parentUsed = true;
                            }
                        }
                        else if (align == parent.getGroupAlignment()) {
                            parentUsed = true;
                        }
                    }
                    if (parentUsed) {
                        interval = parent;
                        parent = LayoutInterval.getFirstParent(parent, PARALLEL);
                    }
                } while (parentUsed);
                LayoutRegion posRegion = interval.getCurrentSpace();
                LayoutRegion contRegion = targetRoots[0].getCurrentSpace();
                int conty1 = contRegion.positions[dir][LEADING];
                int conty2 = contRegion.positions[dir][TRAILING];
                int posx = posRegion.positions[i][(inRoot || !position.nextTo) ? align : 1-align];
                int posy1 = posRegion.positions[dir][LEADING]-GL_TIP;
                posy1 = Math.max(posy1, conty1);
                int posy2 = posRegion.positions[dir][TRAILING]+GL_TIP;
                posy2 = Math.min(posy2, conty2);
                int x = movingSpace.positions[i][align];
                int y1 = movingSpace.positions[dir][LEADING]-GL_TIP;
                y1 = Math.max(y1, conty1);
                int y2 = movingSpace.positions[dir][TRAILING]+GL_TIP;
                y2 = Math.min(y2, conty2);
                Stroke oldStroke = g.getStroke();
                g.setStroke(dashedStroke);
                if (position.nextTo) { // adding next to
                    int padIndex = -1;
                    int paintedPad = 0;
                    if (position.paddingSizes != null && position.paddingSizes.length > 1) {
                        // more padding variants possible
                        assert position.paddingType != null && position.snapped;
                        for (int j=0; j < PADDINGS.length; j++) {
                            if (PADDINGS[j] == position.paddingType) {
                                padIndex = j;
                                if (j > 0) {
                                    paintedPad = position.paddingSizes[j];
                                } 
                                break;
                            }
                        }
                    }
                    if (i == HORIZONTAL) {
                        g.drawLine(x, Math.min(y1, posy1), x, Math.max(y2, posy2));
                    } else {
                        g.drawLine(Math.min(y1, posy1), x, Math.max(y2, posy2), x);
                    }
                    while (--padIndex >= 0) { // paint additional lines indicating smaller paddings
                        if (PADDINGS[padIndex] != PaddingType.INDENT) {
                            int pad = position.paddingSizes[padIndex];
                            int dx = paintedPad - pad;
                            x -= (align == LEADING ? dx : -dx);
                            if (i == HORIZONTAL) {
                                g.drawLine(x, Math.min(y1, posy1), x, Math.max(y2, posy2));
                            } else {
                                g.drawLine(Math.min(y1, posy1), x, Math.max(y2, posy2), x);
                            }
                            paintedPad = pad;
                        }
                    }
                } else { // adding aligned
                    //int ay1 = Math.min(y1, posy1);
                    //int ay2 = Math.max(y2, posy2);
                    if (x == posx) {
                        if (i == HORIZONTAL) {
                            g.drawLine(posx, Math.min(y1, posy1), posx, Math.max(y2, posy2));
                        } else {
                            g.drawLine(Math.min(y1, posy1), posx, Math.max(y2, posy2), posx);
                        }
                    }
                    else { // indented position
                        if (i == HORIZONTAL) {
                            g.drawLine(posx, posy1, posx, posy2);
                            g.drawLine(x, y1, x, y2);
                        }
                        else {
                            g.drawLine(posy1, posx, posy2, posx);
                            g.drawLine(y1, x, y2, x);
                        }
                    }
                }
                g.setStroke(oldStroke);
            }
            else if (componentSnappedToDefaultSize(i)) { // resizing snapped to default preferred size
                int align = movingEdges[i];
                int x1 = movingSpace.positions[i][align];
                int x2 = movingSpace.positions[i][align^1];
                int y = movingSpace.positions[i^1][CENTER];
                Stroke oldStroke = g.getStroke();
                g.setStroke(dashedStroke);
                if (i == HORIZONTAL) {
                    g.drawLine(x1, y, x2, y);
                }
                else {
                    g.drawLine(y, x1, y, x2);
                }
                g.setStroke(oldStroke);
            }
        }
    }

    private void paintGap(Graphics2D g) {
        Rectangle r = movingSpace.toRectangle(new Rectangle());
        if (dimension == HORIZONTAL) {
            r.y = resizingGap.paintRect.y;
            r.height = resizingGap.paintRect.height;
        } else { // VERTICAL
            r.x = resizingGap.paintRect.x;
            r.width = resizingGap.paintRect.width;
        }
        layoutPainter.paintGapResizing(g, resizingGap, r);
        PositionDef gapPos = bestPositions[dimension];
        if (gapPos.snapped) {
            paintGapResizingSnap(g, resizingGap, movingSpace.positions[dimension],
                    gapPos.alignment, gapPos.paddingType, false, null);
        }
    }

    private static void paintGapResizingSnap(Graphics2D g, GapInfo gapInfo,
            int[] paintPos, int resEdge, PaddingType resPaddingType,
            boolean atContainerBorder, int[] clipPos) {
        Stroke oldStroke = g.getStroke();
        g.setStroke(dashedStroke);
        int ortPos1 = gapInfo.ortPositions[LEADING];
        if (clipPos != null && ortPos1 < clipPos[LEADING]) {
            ortPos1 = clipPos[LEADING];
        }
        ortPos1 -= GL_TIP;
        int ortPos2 = gapInfo.ortPositions[TRAILING];
        if (clipPos != null && ortPos2 > clipPos[TRAILING]) {
            ortPos2 = clipPos[TRAILING];
        }
        ortPos2 += GL_TIP;
        int d = resEdge == LEADING ? -1 : 1;
        int start = paintPos[resEdge^1];
        for (PaddingType p : PaddingType.values()) {
            if (p == PaddingType.INDENT) {
                continue;
            }
            int pos = start + (gapInfo.defaultGapSizes[p.ordinal()]*d);
            if (atContainerBorder) {
                pos--; // not to get repainted with container selection border
            }
            if (gapInfo.dimension == HORIZONTAL) {
                g.drawLine(pos, ortPos1, pos, ortPos2);
            } else { // VERTICAL
                g.drawLine(ortPos1, pos, ortPos2, pos);
            }
            if (p == resPaddingType || resPaddingType == null) {
                break;
            }
        }
        g.setStroke(oldStroke);
    }

    static void paintGapResizingSnap(Graphics2D g, GapInfo gapInfo, LayoutComponent gapContainer, VisualState visualState) {
        int edge = gapInfo.resizeTrailing ? TRAILING : LEADING;
        PaddingType pt = gapInfo.gap.getPaddingType();
        boolean atContainerBorder = edge==TRAILING && pt == null
                && gapInfo.defaultGapSizes.length == 1 && gapInfo.defaultGapSizes[0] > 0;

        Shape clip = visualState.getComponentVisibilityClip(gapContainer);
        int[] clipPos;
        if (clip instanceof Rectangle) {
            Rectangle r = (Rectangle) clip;
            int c1;
            int c2;
            if (gapInfo.dimension == HORIZONTAL) {
                c1 = r.y;
                c2 = c1 + r.height;
            } else {
                c1 = r.x;
                c2 = c1 + r.width;
            }
            clipPos = new int[] { c1, c2 };
        } else {
            clipPos = null;
        }

        paintGapResizingSnap(g, gapInfo,
                new int[] { gapInfo.position, gapInfo.position+gapInfo.currentSize },
                edge, pt, atContainerBorder, clipPos);
    }

    String[] positionCode() {
        String[] code = new String[DIM_COUNT];
        if (isComponentOperation()) {
            for (int i=0; i < DIM_COUNT; i++) {    
                LayoutDragger.PositionDef position = bestPositions[i];
                if (position != null) {
                    int alignment = position.alignment;
                    if (position.nextTo) { // adding next to
                        String paddingCode = position.interval.getParent() == null// == targetContainer.getLayoutRoot(i)
                            ? "Container" : paddingTypeCode(position.paddingType); // NOI18N
                        code[i] = "nextTo" + paddingCode // NOI18N
                                + dimensionCode(i) + alignmentCode(alignment);
                    } else { // adding aligned
                        int x = movingSpace.positions[i][alignment];
                        int posx = position.interval.getCurrentSpace().positions[i][alignment];
                        if (x == posx) {
                            code[i] = "align" + dimensionCode(i) + alignmentCode(alignment); // NOI18N
                        } else {
                            code[i] = "indent"; // NOI18N
                        }
                    }
                }
            }
        }
        if (code[0] == null) {
            code[0] = code[1];
            code[1] = null;
        }
        if (code[0] == null) {
            if (isResizing()) {
                code[0] = isComponentOperation() ? "generalResizing" : "gapResizing"; // NOI18N
            } else {
                code[0] = "generalPosition"; // NOI18N
            }
        }
        return code;
    }

    String getToolTipText() {
        String hint = null;
        if (isResizing()) {
            if (isComponentOperation()) {
                StringBuilder sb = new StringBuilder();
                for (int dim=0; dim < DIM_COUNT; dim++) {
                    if (isResizing(dim)) {
                        String size = componentSnappedToDefaultSize(dim)
                                ? "default" // TODO internationalize
                                : Integer.toString(movingSpace.size(dim));
                        if (sb.length() > 0) {
                            sb.append(", "); // TODO internationalize
                        }
                        sb.append(size);
                    }
                }
                hint = sb.toString();
            } else if (resizingGap != null) {
                PositionDef gapPos = bestPositions[dimension];
                if (gapPos.snapped) {
                    hint = VisualState.getDefaultGapDisplayName(gapPos.paddingType);
                } else {
                    hint = Integer.toString(gapPos.distance);
                }
            }
        }
        return hint;
    }

    private static String dimensionCode(int dim) {
        return (dim == HORIZONTAL) ? "Horizontal" : "Vertical"; // NOI18N
    }

    private static String alignmentCode(int alignment) {
        String code = null;
        switch (alignment) {
            case LEADING: code = "Leading"; break; // NOI18N
            case TRAILING: code = "Trailing"; break; // NOI18N
            case BASELINE: code = "Baseline"; break; // NOI18N
        }
        return code;
    }

    private static String paddingTypeCode(PaddingType paddingType) {
        if (paddingType == PaddingType.UNRELATED) {
            return "Unrelated"; // NOI18N
        } else if (paddingType == PaddingType.SEPARATE) {
            return "Separate"; // NOI18N
        } else {
            return "Related"; // NOI18N
        }
    }

    // -----
    // finding position in the layout

    /**
     * For the moving/resizing component represented by 'movingSpace' finds the
     * most suitable position the component could be placed to. Works in the
     * dimension defined by 'dimension' field.
     */
    private int moveComponents() {
        PositionDef best;
        int snapDistance = LayoutRegion.UNKNOWN;

        if (targetContainer != null) {
            PositionDef bestNextTo;
            PositionDef bestAligned;
            LayoutInterval layoutRoot = targetRoots[dimension];
            int edges = movingEdges[dimension];

            // [we could probably find the best position directly in scanning, not
            //  separately for each alignment point, choosing the best one additionally here
            //  (only issue is that BASELINE is preferred no matter the distance score)]
            // 1st go through the layout and find position candidates
            checkRootForNextTo(layoutRoot, edges);
            scanLayoutForNextTo(layoutRoot, edges);
            bestNextTo = chooseBestNextTo();

            if (snapping) { // finding aligned position makes sense only if we can snap to it
                checkRootForAligned(layoutRoot, edges);
                scanLayoutForAligned(layoutRoot, edges);
                bestAligned = chooseBestAligned();
            }
            else bestAligned = null;

            // 2nd choose the best position
            if (bestAligned == null) {
                best = bestNextTo;
            }
            else if (bestNextTo == null) {
                best = bestAligned;
            }
            else { // both available
                boolean preferredNextTo = isPreferredNextTo(bestNextTo, bestAligned);
                int nextToDst = smallestDistance(findingsNextTo[dimension]);
                int alignedDst = smallestDistance(findingsAligned[dimension]);
                if (!relatedPositions(bestNextTo, bestAligned)) {
                    // penalize the aligned position according to distance in the other dimension
                    int alignedOrtDst = Math.abs(LayoutRegion.nonOverlapDistance(
                        bestAligned.interval.getCurrentSpace(), movingSpace, dimension ^ 1));
                    alignedDst = getDistanceScore(alignedDst, alignedOrtDst);
                }
                if (preferredNextTo) {
                    best = alignedDst*2 <= nextToDst && nextToDst - alignedDst >= SNAP_DISTANCE/2 ?
                           bestAligned : bestNextTo;
                }
                else {
                    best = nextToDst*2 <= alignedDst && alignedDst - nextToDst >= SNAP_DISTANCE/2 ?
                           bestNextTo : bestAligned;
                }
                if (best == bestNextTo) {
                    PositionDef equalAligned = getAlignedEqualToNextTo(bestNextTo);
                    if (equalAligned != null)
                        best = equalAligned;
                }
            }
        }
        else {
            best = null;
        }

        if (snapping) {
            if (isResizing(dimension)) {
                int size = movingSpace.size(dimension);
                SizeDef sd = sizing[dimension];
                int sizeDiff = size - sd.minSize;
                int diff2 = size - sd.contPrefSize;
                if (Math.abs(diff2) < Math.abs(sizeDiff)) {
                    sizeDiff = diff2;
                }
                diff2 = size - sd.minSizeWithDefaultGap;
                if (Math.abs(diff2) < Math.abs(sizeDiff)) {
                    sizeDiff = diff2;
                }
                diff2 = size - sd.minSizeWithZeroGap;
                if (Math.abs(diff2) < Math.abs(sizeDiff)) {
                    sizeDiff = diff2;
                }
                int absDiff = Math.abs(sizeDiff);
                if (absDiff < SNAP_DISTANCE && (best == null || absDiff < Math.abs(best.distance))) {
                    best = null; // snapping to preferred size has precedence here
                    snapDistance = movingEdges[dimension] == LEADING ? -sizeDiff : sizeDiff;
                }
            }
            if (best != null) {
                snapDistance = best.distance;
            }
        }

        bestPositions[dimension] = best;
        return snapDistance;
    }

    /**
     * Checks distance of the leading/trailing edges of the moving component
     * to the padding positions in the root layout interval. (Distance in root
     * interval is checked differently than scanLayoutForNextTo method does.)
     * @param layoutRoot layout interval to be checked
     * @param alignment determines which edges of the moving space should be
     *        checked - can be LEADING or TRAILING, or ALL_POINTS for both
     */
    private void checkRootForNextTo(LayoutInterval layoutRoot, int alignment) {
        assert alignment == LayoutRegion.ALL_POINTS || alignment == LEADING || alignment == TRAILING;

        if (operation == RESIZING && isValidNextToResizing(layoutRoot, alignment) != 1)
            return;

        LayoutRegion rootSpace = layoutRoot.getCurrentSpace();

        for (int i = LEADING; i <= TRAILING; i++) {
            if (alignment == LayoutRegion.ALL_POINTS || alignment == i) {
                int distance = LayoutRegion.distance(rootSpace, movingSpace,
                                                     dimension, i, i);
                assert distance != LayoutRegion.UNKNOWN;
                if (snapping) {
                    // PENDING consider the resulting interval when moving more components
                    int[] pads = findPaddings(null, movingComponents[0].getLayoutInterval(dimension), null, dimension, i);
                    int pad = (pads != null && pads.length > 0) ? pads[0] : 0;
                    distance += (i == LEADING ? -pad : pad);
                }

                if (!snapping || Math.abs(distance) < SNAP_DISTANCE) {
                    PositionDef bestSoFar = findingsNextTo[dimension][i];
                    assert !bestSoFar.isSet();
                    bestSoFar.interval = layoutRoot;
                    bestSoFar.alignment = i;
                    bestSoFar.distance = distance;
                    bestSoFar.nextTo = true;
                    bestSoFar.snapped = snapping && Math.abs(distance) < SNAP_DISTANCE;
                    bestSoFar.paddingType = null;
                    bestSoFar.paddingSizes = null;
                }
            }
        }
    }

    /**
     * Recursively scans given interval for suitable sub-intervals next to
     * which the moving component could be suitably positioned.
     * @param interval group to scan
     * @param alignment determines which edges of the moving space should be
     *        checked - can be LEADING or TRAILING, or ALL_POINTS for both
     * @return position of the group as a whole to the moving space (corresponds
     *         to the edge by which the moving component is attached)
     */
    private int scanLayoutForNextTo(LayoutInterval interval, int alignment) {
        assert alignment == LayoutRegion.ALL_POINTS || alignment == LEADING || alignment == TRAILING;

        int groupOuterAlignment = DEFAULT;

        for (int idx=0, count=interval.getSubIntervalCount(); idx < count; idx++) {
            LayoutInterval sub = interval.getSubInterval(idx);
            if (sub.isEmptySpace()) {
                continue;
            }

            if (!orthogonalOverlap(interval, idx))
                continue;

            int nextToAlignment = DEFAULT;

            if (sub.isComponent()) {
                if (isValidInterval(sub)
                    && (operation != RESIZING || isValidNextToResizing(sub, alignment) == 1))
                {   // sub is a component, not being moved/resized
                    nextToAlignment = checkNextToPosition(sub, alignment);
                }
            }
            else if (sub.isSequential()) {
                nextToAlignment = scanLayoutForNextTo(sub, alignment);
            }
            else { // parallel group
                // check if the group is not going to be dissolved (contains moving interval)
                boolean validForRef = isValidInterval(sub);
                int validResizing = validForRef && operation == RESIZING ?
                                    isValidNextToResizing(sub, alignment) : 1;
                int subGroupOuterAlign;

                if (validResizing != -1 && canGoInsideForNextTo(sub, validForRef)) {
                    int align = alignment;
                    for (int i = LEADING; i <= TRAILING; i++) {
                        if (alignment != LayoutRegion.ALL_POINTS && i != alignment) {
                            continue; // skip irrelevant alignment
                        }
                        int insideDst = LayoutRegion.distance(sub.getCurrentSpace(), movingSpace, dimension, i, i)
                                        * (i == LEADING ? 1 : -1);
                        if (insideDst < -SNAP_DISTANCE) {
                            // out of the subgroup - there is nothing "next to" inside
                            if (align == LayoutRegion.ALL_POINTS)
                                align = i ^ 1;
                            else
                                align = LayoutRegion.NO_POINT;
                        }
                    }
                    if (align != LayoutRegion.NO_POINT) {
                        subGroupOuterAlign = scanLayoutForNextTo(sub, align);
                    }
                    else subGroupOuterAlign = DEFAULT;
                }
                else subGroupOuterAlign = alignment;

                if (validForRef && validResizing == 1 && subGroupOuterAlign != DEFAULT) {
                    nextToAlignment = checkNextToPosition(sub, subGroupOuterAlign);
                }
            }

            if (nextToAlignment != DEFAULT) {
                if (interval.isSequential()) {
                    // for sequence only first and last intervals can be used for outer alignment
                    if (groupOuterAlignment == DEFAULT && (idx == 0 || idx+1 == count)) {
                        if (idx != 0) {
                            nextToAlignment = nextToAlignment == TRAILING ? DEFAULT : LEADING;
                        }
                        else if (idx+1 != count) {
                            nextToAlignment = nextToAlignment == LEADING ? DEFAULT : TRAILING;
                        }
                        groupOuterAlignment = nextToAlignment;
                    }
                }
                else {
                    // check if 'sub' is aligned at the corresponding border of the
                    // group - to know if the whole group could not be next to
                    if (LayoutInterval.wantResize(sub)) {
                        if (nextToAlignment == LayoutRegion.ALL_POINTS) {
                            groupOuterAlignment = LayoutRegion.ALL_POINTS; // both L and T can happen
                        }
                        else if (groupOuterAlignment == DEFAULT) {
                            groupOuterAlignment = nextToAlignment; // "next to" side has 'sub' aligned
                        }
                    }
                    else if ((nextToAlignment == LayoutRegion.ALL_POINTS
                              || (nextToAlignment^1) == sub.getAlignment())
                             && groupOuterAlignment == DEFAULT)
                    {   // 'sub' aligned at the "next to" side
                        groupOuterAlignment = sub.getAlignment() ^ 1;
                    }
                }
            }
        }

        return groupOuterAlignment;
    }

    /**
     * Checks if moving interval can be considered as overlapping in the
     * orthogonal dimension (thus being relevant for next to snapping) with
     * given interval's sub-interval.
     */
    private boolean orthogonalOverlap(LayoutInterval interval, int index) {
        LayoutInterval sub = interval.getSubInterval(index);
        LayoutRegion subSpace = sub.getCurrentSpace();
        if (LayoutRegion.overlap(movingSpace, subSpace, dimension^1, 0))
            return true;

        if (dimension == VERTICAL) { // there may be some exceptions in vertical dimension
            if (sub.isSequential()) // sub-sequence to be checked later
                return true;

            // [note: can do this reliably only for root vertical sequence - with
            //        more sequences LayoutFeeder might ignore it anyway]
            if (interval.getParent() != null && interval.getParent().getSubIntervalCount() > 1)
                return false;

            if (!LayoutRegion.overlap(movingSpace, interval.getCurrentSpace(), dimension, 0))
                return true; // not in parallel with any sibling
            if (interval.isSequential()) {
                // check if it is before first or after last
                if (LayoutRegion.distance(movingSpace, subSpace, dimension, TRAILING, LEADING) > 0) {
                    // moving interval is located in front of 'sub''
                    while (--index >= 0) {
                        LayoutInterval li = interval.getSubInterval(index);
                        if (!li.isEmptySpace() && isValidInterval(li))
                            break;
                    }
                    if (index < 0) // 'sub' is the first interval
                        return true;
                }
                else if (LayoutRegion.distance(subSpace, movingSpace, dimension, TRAILING, LEADING) > 0) {
                    // moving interval is locate behind 'sub''
                    while (++index < interval.getSubIntervalCount()) {
                        LayoutInterval li = interval.getSubInterval(index);
                        if (!li.isEmptySpace() && isValidInterval(li))
                            break;
                    }
                    if (index == interval.getSubIntervalCount()) // 'sub' is the last interval
                        return true;
                }
            }
        }

        return false;
    }

    private int checkNextToPosition(LayoutInterval sub, int alignment) {
        int nextToAlignment = DEFAULT;
        LayoutRegion subSpace = sub.getCurrentSpace();
        for (int i = LEADING; i <= TRAILING; i++) {
            if (alignment != LayoutRegion.ALL_POINTS && i != alignment)
                continue; // skip irrelevant edge

            boolean validDistance;
            int distance = LayoutRegion.distance(subSpace, movingSpace,
                                                 dimension, i^1, i);
            PaddingType paddingType = null;
            int[] pads = null;
            if (snapping) {
                // if the examined interval already has neighbor default gap,
                // then use its padding type and don't check other
                LayoutInterval gap = LayoutInterval.getNeighbor(sub, i^1, false, true, false);
                if (gap != null && LayoutInterval.isFixedDefaultPadding(gap)) {
                    LayoutInterval neighbor = LayoutInterval.getNeighbor(gap, i^1, true, true, false);
                    if (neighbor != null && isValidInterval(neighbor)) {
                        paddingType = gap.getPaddingType();
                        if (paddingType == null) {
                            paddingType = PaddingType.RELATED;
                        }
                    } // otherwise the gap is next to the moving interval, so not relevant
                }
                // PENDING consider the resulting interval when moving more components
                pads = findPaddings(sub, movingComponents[0].getLayoutInterval(dimension),
                                          paddingType, dimension, i);
                int padsCount = pads != null ? pads.length : 0;
                int orient = (i == LEADING ? -1 : 1);
                int padDst = distance + (padsCount > 0 ? orient * pads[0] : 0);
                if (paddingType == null) {
                    // find out the closest padding position and adjust the distance
                    paddingType = PaddingType.RELATED;
                    for (int j=1; j < padsCount; j++) {
                        if (PADDINGS[j] != PaddingType.INDENT) {
                            int d = distance + (orient * pads[j]);
                            if (Math.abs(d) < Math.abs(padDst)) {
                                padDst = d;
                                paddingType = PADDINGS[j];
                            }
                        }
                    }
                }
                distance = padDst;
                validDistance = Math.abs(distance) < SNAP_DISTANCE;
            }
            else {
                validDistance = (i == LEADING ? distance > 0 : distance < 0);
            }
            if (validDistance) {
                nextToAlignment = nextToAlignment == DEFAULT ? i : LayoutRegion.ALL_POINTS;

                PositionDef bestSoFar = findingsNextTo[dimension][i];
                if (!bestSoFar.isSet() || compareNextToPosition(sub, distance, bestSoFar) > 0) {
                    bestSoFar.interval = sub;
                    bestSoFar.alignment = i;
                    bestSoFar.distance = distance;
                    bestSoFar.nextTo = true;
                    bestSoFar.snapped = snapping;
                    bestSoFar.paddingType = paddingType;
                    bestSoFar.paddingSizes = pads;
                }
            }
        }
        return nextToAlignment;
    }

    /**
     * Compares given "next to" position with the best position found so far.
     * Cares about the visual aspect only, not the logical structure.
     * @return int as result of comparison: 1 - new position is better
     *                                     -1 - old position is better
     *                                      0 - the positions are equal
     */
    private int compareNextToPosition(LayoutInterval newInterval, int newDistance,
                                      PositionDef bestSoFar)
    {
        if (!bestSoFar.isSet())
            return 1; // best not set yet

        LayoutRegion newSpace = newInterval.getCurrentSpace();
        LayoutRegion oldSpace = bestSoFar.interval.getCurrentSpace();
        int oldDistance = Math.abs(bestSoFar.distance);

        // 1st compare the direct distance
        if (newDistance < 0)
            newDistance = -newDistance;
        if (newDistance != oldDistance) {
            return newDistance < oldDistance ? 1 : -1;
        }

        if (newInterval.isParentOf(bestSoFar.interval)) {
            return 1;
        }

        // 2nd compare the orthogonal distance
        int newOrtDst = Math.abs(
                LayoutRegion.minDistance(newSpace, movingSpace, dimension ^ 1));
        int oldOrtDst = Math.abs(
                LayoutRegion.minDistance(oldSpace, movingSpace, dimension ^ 1));
        if (newOrtDst != oldOrtDst) {
            return newOrtDst < oldOrtDst ? 1 : -1;
        }

        return 0;
    }

    private static boolean canGoInsideForNextTo(LayoutInterval subGroup, boolean valid) {
        // can't go inside a group if it has "closed" group alignment (center
        // or baseline) - only can if the group is not valid (i.e. going to be
        // removed so just one not-aligned interval might remain)
        return subGroup.isSequential()
               || (subGroup.isParallel()
                   && (!valid
                       || (subGroup.getGroupAlignment() != CENTER
                           && subGroup.getGroupAlignment() != BASELINE)));
    }

    /**
     * Checks distance of the leading/trailing edges of the moving component
     * to the border positions in the root layout interval. (Distance in root
     * interval is checked differently than scanLayoutForAligned method does.)
     * @param layoutRoot layout interval to be checked
     * @param alignment determines which edges of the moving space should be
     *        checked - can be LEADING or TRAILING, or ALL_POINTS for both
     */
    private void checkRootForAligned(LayoutInterval layoutRoot, int alignment) {
        assert alignment == LayoutRegion.ALL_POINTS || alignment == LEADING || alignment == TRAILING;

        if (operation == RESIZING && !isValidAlignedResizing(layoutRoot, alignment))
            return;

        LayoutRegion rootSpace = layoutRoot.getCurrentSpace();

        for (int i = LEADING; i <= TRAILING; i++) {
            if (alignment == LayoutRegion.ALL_POINTS || alignment == i) {
                int distance = LayoutRegion.distance(rootSpace, movingSpace,
                                                     dimension, i, i);
                if (distance != LayoutRegion.UNKNOWN
                    && Math.abs(distance) < SNAP_DISTANCE)
                {   // compare the actual distance with the best one
                    PositionDef bestSoFar = findingsAligned[dimension][i];
                    assert !bestSoFar.isSet();
                    bestSoFar.interval = layoutRoot;
                    bestSoFar.alignment = i;
                    bestSoFar.distance = distance;
                    bestSoFar.nextTo = false;
                    bestSoFar.snapped = true;
                }
            }
        }
    }

    /**
     * Recursively scans given interval for suitable sub-intervals which the
     * moving component could be aligned with.
     * @param alignment determines which edges of the moving space should be
     *        checked - can be LEADING or TRAILING, or ALL_POINTS for both
     */
    private void scanLayoutForAligned(LayoutInterval interval, int alignment) {
        assert alignment == LayoutRegion.ALL_POINTS || alignment == LEADING || alignment == TRAILING;

        Iterator it = interval.getSubIntervals();
        while (it.hasNext()) {
            LayoutInterval sub = (LayoutInterval) it.next();
            if (sub.isEmptySpace())
                continue;

            // can align with component, or with baseline group if moving comp has no baseline
            boolean alignableClosedGroup = sub.isGroup() && sub.getGroupAlignment() == BASELINE
                                           && !canSnapToBaseline && isValidInterval(sub);

            if (((sub.isComponent() && isValidInterval(sub)) || alignableClosedGroup)
                && (operation != RESIZING || isValidAlignedResizing(sub, alignment)))
            {   // check distance of all alignment points of the moving
                // component to the examined interval space
                for (int i=0; i < LayoutRegion.POINT_COUNT[dimension]; i++) {
                    if (alignment == LayoutRegion.ALL_POINTS || i == alignment) {
                        int indentedDst = getIndentedDistance(sub, i);
                        int directDst = getDirectDistance(sub, i);
                        int distance = Math.abs(indentedDst) < Math.abs(directDst) ?
                                       indentedDst : directDst;
                        if (checkAlignedDistance(distance, sub.getCurrentSpace(), i)) {
                            // compare the actual distance with the best one
                            PositionDef bestSoFar = findingsAligned[dimension][i];
                            if (compareAlignedPosition(sub, distance, bestSoFar) >= 0) {
                                // >= 0 means we naturally prefer later components
                                bestSoFar.interval = sub;
                                bestSoFar.alignment = i;
                                bestSoFar.distance = distance;
                                bestSoFar.nextTo = false;
                                bestSoFar.snapped = true;
                            }
                        }
                    }
                }
            }

            if (sub.getSubIntervalCount() > 0 && !alignableClosedGroup
                && LayoutRegion.overlap(sub.getCurrentSpace(), movingSpace, dimension, SNAP_DISTANCE/2))
            {   // the group overlaps with the moving space so it makes sense to dive into it
                scanLayoutForAligned(sub, alignment);
            }
        }
    }

    private int getIndentedDistance(LayoutInterval interval, int alignment) {
        if (dimension == HORIZONTAL && alignment == LEADING) {
            // indented position is limited to horizontal dimension, left alignment
            LayoutRegion examinedSpace = interval.getCurrentSpace();
            int verticalDst = LayoutRegion.distance(examinedSpace, movingSpace,
                                                    VERTICAL, TRAILING, LEADING);
            if (verticalDst >= 0 && verticalDst < 2 * SNAP_DISTANCE) {
                // PENDING does it have a sense to generalize this for multiselection?
                int indent = findIndent(interval.getComponent(), movingComponents[0],
                                        dimension, alignment);
                if (indent > 0) {
                    return LayoutRegion.distance(examinedSpace, movingSpace,
                                                 dimension, alignment, alignment)
                           - indent;
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    private int getDirectDistance(LayoutInterval interval, int alignment) {
        return checkValidAlignment(interval, alignment) ?
               LayoutRegion.distance(interval.getCurrentSpace(), movingSpace,
                                     dimension, alignment, alignment) :
               Integer.MAX_VALUE;
    }

    private boolean checkValidAlignment(LayoutInterval interval, int alignment) {
        int presentAlign = interval.getAlignment();
        // check if the interval is not the last one to remain in parallel group
        if (presentAlign != DEFAULT) {
            boolean lastOne = true;
            Iterator it = interval.getParent().getSubIntervals();
            while (it.hasNext()) {
                LayoutInterval li = (LayoutInterval) it.next();
                if (li != interval && isValidInterval(li)) {
                    lastOne = false;
                    break;
                }
            }
            if (lastOne) {
                presentAlign = DEFAULT;
            }
        }

        if (alignment == LEADING || alignment == TRAILING) {
            // leading and trailing can't align with "closed" alignments
            if (presentAlign == CENTER || presentAlign == BASELINE)
                return false;
        }
        else if (alignment == CENTER) {
            // center alignment is allowed only with already centered intervals
            // (center alignment needs to be set up explicitly first)
            if (presentAlign != CENTER)
                return false;
        }
        else if (alignment == BASELINE) {
            // baseline can't go with other "closed" alignments
            if (presentAlign == CENTER)
                return false;
        }
        return true;
    }

    private boolean checkAlignedDistance(int distance, LayoutRegion examinedSpace, int alignment) {
        if (distance != LayoutRegion.UNKNOWN && Math.abs(distance) < SNAP_DISTANCE) {
            // check if there is nothing in the way along the line of aligned edges
            int x1, x2, y1, y2;
            int indent = movingSpace.positions[dimension][alignment]
                         - examinedSpace.positions[dimension][alignment] - distance;
            if (indent == 0) {
                x1 = examinedSpace.positions[dimension][alignment] - SNAP_DISTANCE/2;
                x2 = examinedSpace.positions[dimension][alignment] + SNAP_DISTANCE/2;
                y2 = movingSpace.positions[dimension^1][LEADING];
            }
            else {
                x1 = examinedSpace.positions[dimension][alignment];
                x2 = x1 + indent + SNAP_DISTANCE/2;
                y2 = movingSpace.positions[dimension^1][TRAILING];
                // note indent is not offered at all by getIndentedDistance if
                // the vertical position "does not match"
            }
            y1 = examinedSpace.positions[dimension^1][TRAILING];
            if (y1 > y2) {
                y1 = movingSpace.positions[dimension^1][TRAILING];
                y2 = examinedSpace.positions[dimension^1][LEADING];
                if (y1 > y2) { // orthogonally overlaps - so can see it
                    return true;
                }
            }
            return !contentOverlap(targetRoots[dimension],
                                   x1, x2, y1, y2, dimension);
        }
        return false;
    }

    private boolean contentOverlap(LayoutInterval group, int x1, int x2, int y1, int y2, int dim) {
        int[][] groupPos = group.getCurrentSpace().positions;
        for (int i=0, n=group.getSubIntervalCount(); i < n; i++) {
            LayoutInterval li = group.getSubInterval(i);
            LayoutInterval prev = null;
            LayoutInterval next = null;
            int _x1, _x2, _y1, _y2;
            if (li.isEmptySpace()) {
                if (group.isParallel()) {
                    continue;
                }
                for (int ii=i-1; ii >= 0; ii--) { // for robustness to occurrence of consecutive gaps
                    prev = group.getSubInterval(ii);
                    if (!prev.isEmptySpace()) {
                        break;
                    } else {
                        prev = null;
                    }
                }
                _x1 = prev != null ? prev.getCurrentSpace().positions[dim][TRAILING] : groupPos[dim][LEADING];
                while (i+1 < n) { // for robustness to occurrence of consecutive gaps
                    next = group.getSubInterval(i+1);
                    if (!next.isEmptySpace()) {
                        break;
                    } else {
                        next = null;
                        i++;
                    }
                }
                _x2 = next != null ? next.getCurrentSpace().positions[dim][LEADING] : groupPos[dim][TRAILING];
                _y1 = groupPos[dim^1][LEADING];
                _y2 = groupPos[dim^1][TRAILING];
                if (_y1 < y1) {
                    _y2 = _y1;
                }
                else if (_y2 > y2) {
                    _y1 = _y2;
                }
            }
            else {
                int[][] positions = li.getCurrentSpace().positions;
                _x1 = positions[dim][LEADING];
                _x2 = positions[dim][TRAILING];
                _y1 = positions[dim^1][LEADING];
                _y2 = positions[dim^1][TRAILING];
            }

            if (_x1 < x2 && _x2 > x1 && _y1 < y2 && _y2 > y1) { // overlap
                if (li.isComponent()) {
                    if (isValidInterval(li))
                        return true;
                }
                else if (li.isEmptySpace()) {
                    if (prev != null && next != null // first/last space is not in the way
                        && (li.getMinimumSize() == NOT_EXPLICITLY_DEFINED || li.getMinimumSize() == USE_PREFERRED_SIZE)
                        && li.getPreferredSize() == NOT_EXPLICITLY_DEFINED
                        && (li.getMaximumSize() == NOT_EXPLICITLY_DEFINED || li.getMaximumSize() == USE_PREFERRED_SIZE))
                    {   // preferred padding might be in the way
                        if ((!prev.isComponent() || isValidInterval(prev))
                            && (!next.isComponent() || isValidInterval(next)))
                        {   // preferred padding between valid intervals (i.e. not next to the moving component itself)
                            return true;
                        }
                    }
                    if (_x1 >= x1 && _x2 <= x2)
                        return false; // goes over a gap in a sequence - so no overlap
                }
                else if (li.isGroup() && contentOverlap(li, x1, x2, y1, y2, dim)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return int as result of comparison: 1 - new position is better
     *                                     -1 - old position is better
     *                                      0 - the positions are equal
     */
    private int compareAlignedPosition(LayoutInterval newInterval,
                                       int newDistance,
                                       PositionDef bestSoFar)
    {
        if (!bestSoFar.isSet())
            return 1; // best not set yet

        // compute direct distance
        if (newDistance < 0)
            newDistance = -newDistance;
        int oldDistance = Math.abs(bestSoFar.distance);

        if (newInterval.getParent() == null) {
            return newDistance < oldDistance ? 1 : -1;
        }
        if (bestSoFar.interval.getParent() == null) {
            return oldDistance < newDistance ? -1 : 1;
        }

        // compute orthogonal distance
        LayoutRegion newSpace = newInterval.getCurrentSpace();
        LayoutRegion oldSpace = bestSoFar.interval.getCurrentSpace();
        int newOrtDst = Math.abs(
                LayoutRegion.nonOverlapDistance(newSpace, movingSpace, dimension ^ 1));
        int oldOrtDst = Math.abs(
                LayoutRegion.nonOverlapDistance(oldSpace, movingSpace, dimension ^ 1));

        // compute score
        int newScore = getDistanceScore(newDistance, newOrtDst);
        int oldScore = getDistanceScore(oldDistance, oldOrtDst);

        if (newScore != oldScore) {
            return newScore < oldScore ? 1 : -1;
        }
        return 0;
    }

    private static int getDistanceScore(int directDistance, int ortDistance) {
        // orthogonal distance >= SNAP_DISTANCE is penalized
        return directDistance + ortDistance / SNAP_DISTANCE;
    }

    private PositionDef chooseBestNextTo() {
        PositionDef[] positions = findingsNextTo[dimension];
        PositionDef bestPos = null;
        int bestDst = 0;
        for (int i=0; i < positions.length; i++) {
            PositionDef pos = positions[i];
            if (pos.isSet()) {
                int dst = Math.abs(pos.distance);
                if (bestPos == null || dst < bestDst
                        || (dst == bestDst && moveDirection[dimension] == i)) {
                    bestPos = pos;
                    bestDst = dst;
                }
            }
        }
        return bestPos;
    }

    private PositionDef chooseBestAligned() {
        PositionDef[] positions = findingsAligned[dimension];
        PositionDef bestPos = null;
        for (int i=positions.length-1; i >= 0; i--) {
            PositionDef pos = positions[i];
            if (pos.isSet()) {
                if (i == BASELINE || i == CENTER) {
                    return pos;
                }
                if (bestPos == null) {
                    bestPos = pos;
                }
                else {
                    int c = compareAlignedPosition(pos.interval, pos.distance, bestPos);
                    if (c == 0) {
                        c = compareAlignedDirection(pos, bestPos);
                    }
                    if (c > 0) {// || (c == 0 && moveDirection[dimension] != bestPos.alignment)) {
                        bestPos = pos;
                    }
                }
            }
        }
        return bestPos;
    }

    private int compareAlignedDirection(PositionDef pos1, PositionDef pos2) {
        boolean p1 = isSuitableAlignment(pos1);
        boolean p2 = isSuitableAlignment(pos2);
        if (p1 == p2) {
            p1 = (pos1.alignment == moveDirection[dimension]);
            p2 = (pos2.alignment == moveDirection[dimension]);
            if (p1 == p2)
                return 0;
        }
        return p1 ? 1 : -1;
    }

    private static boolean isSuitableAlignment(PositionDef pos) {
        assert pos.alignment == LEADING || pos.alignment == TRAILING;
        LayoutInterval parParent = LayoutInterval.getFirstParent(pos.interval, PARALLEL);
        return LayoutInterval.isAlignedAtBorder(pos.interval, parParent, pos.alignment)
               || !LayoutInterval.isAlignedAtBorder(pos.interval, parParent, pos.alignment^1);
    }

    private static int smallestDistance(PositionDef[] positions) {
        int bestDst = -1;
        for (int i=0; i < positions.length; i++) {
            PositionDef pos = positions[i];
            if (pos.isSet()) {
                int dst = Math.abs(pos.distance);
                if (bestDst < 0 || dst < bestDst) {
                    bestDst = dst;
                }
            }
        }
        return bestDst;
    }

    private boolean isPreferredNextTo(PositionDef bestNextTo, PositionDef bestAligned) {
        if (bestNextTo != null && bestAligned != null) {
            if (operation == RESIZING) {
                // prefer aligned resizing if already aligned at the other edge
                // otherwise prefer next to
                LayoutInterval resizing = movingComponents[0].getLayoutInterval(dimension);
                int fixedEdge = movingEdges[dimension] ^ 1;
                if (bestAligned.interval.isParentOf(resizing)) {
                    if (LayoutInterval.isAlignedAtBorder(resizing, bestAligned.interval, fixedEdge)) {
                        return false;
                    }
                }
                else {
                    LayoutInterval commonParent = LayoutInterval.getCommonParent(resizing, bestAligned.interval);
                    if (LayoutInterval.isAlignedAtBorder(resizing, commonParent, fixedEdge)
                        && LayoutInterval.isAlignedAtBorder(bestAligned.interval, commonParent, fixedEdge))
                        return false;
                }
                return true;
            }
        }
        return dimension == HORIZONTAL;
    }

    private static boolean relatedPositions(PositionDef nextTo, PositionDef aligned) {
        if (nextTo.interval == null || aligned.interval == null)
            return false;

        LayoutInterval neighbor = LayoutInterval.getNeighbor(
                aligned.interval, nextTo.alignment, true, true, false);
        return neighbor == nextTo.interval
               || (neighbor == null && nextTo.interval.getParent() == null);
    }

    private PositionDef getAlignedEqualToNextTo(PositionDef bestNextTo) {
        if (operation == RESIZING || !bestNextTo.snapped)
            return null;

        int alignment = bestNextTo.alignment;
        PositionDef alignedAlternative = findingsAligned[dimension][alignment];
        if (alignedAlternative != null && alignedAlternative.distance == bestNextTo.distance) {
            // choose aligned position if its interval is just next to
            // bestNextTo.interval (both positions would lead to same result)
            LayoutInterval neighbor = LayoutInterval.getNeighbor(
                             alignedAlternative.interval, alignment, true, true, false);
            if (neighbor != null
                    && (neighbor == bestNextTo.interval || neighbor.isParentOf(bestNextTo.interval)))
                return alignedAlternative;
        }
        return null;
    }

    /**
     * Checks whether given interval can be used for the moving interval to
     * relate to. Returns false for other moving intervals, or for groups that
     * won't survive removal of the moving intervals from their original
     * positions.
     */
    private boolean isValidInterval(LayoutInterval interval) {
        if (operation == ADDING) {
            return true;
        }

        if (interval.isGroup()) {
            // as the moving intervals are going to be removed first, a valid
            // group must contain at least two other intervals - otherwise it
            // is dissolved before the moving intervals are re-added
            int count = 0;
            Iterator it = interval.getSubIntervals();
            while (it.hasNext()) {
                LayoutInterval li = (LayoutInterval) it.next();
                if ((!li.isEmptySpace() || interval.isSequential())
                    && isValidInterval(li))
                {   // filling gap (in parallel group) does not count
                    count++;
                    if (count > 1)
                        return true;
                }
            }
            return false;
        }
        else {
            for (int i=0; i < movingComponents.length; i++) {
                if (movingComponents[i].getLayoutInterval(dimension) == interval) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * @return 1 - is valid for next to resizing
     *         0 - not valid, but some sub-interval could be
     *        -1 - not valid, even no sub-interval
     */
    private int isValidNextToResizing(LayoutInterval interval, int alignment) {
        assert alignment == LEADING || alignment == TRAILING;
        LayoutInterval resizing = movingComponents[0].getLayoutInterval(dimension);
        if (interval.isParentOf(resizing)) {
            return interval.getParent() == null && clearWayToParent(resizing, interval, dimension, alignment)
                   && (!toDeepToMerge(resizing, interval, alignment)
                       || LayoutInterval.getNeighbor(resizing, alignment, true, true, false) == null) ?
                   1 : 0;
        }

        LayoutInterval commonParent = LayoutInterval.getCommonParent(interval, resizing);
        if (commonParent.isSequential()) {
            if (toDeepToMerge(resizing, commonParent, alignment)
                && LayoutInterval.getNeighbor(resizing, alignment, true, true, false) != interval)
                return -1;

            resizing = getClearWayToParent(resizing, commonParent, dimension, alignment);
            if (resizing == null)
                return -1;

            while (interval.getParent() != commonParent) {
                interval = interval.getParent();
            }

            int startIndex = commonParent.indexOf(alignment == LEADING ? interval : resizing) + 1;
            int endIndex = commonParent.indexOf(alignment == LEADING ? resizing : interval) - 1;
            return startIndex <= endIndex
                   && !LayoutUtils.contentOverlap(movingSpace, commonParent, startIndex, endIndex, dimension^1) ?
                   1 : -1;
        }
        return -1;
    }

    private boolean isValidAlignedResizing(LayoutInterval interval, int alignment) {
        // the examined interval position must be reachable with positive size
        // of the resizing interval
        int dst = LayoutRegion.distance(movingSpace, interval.getCurrentSpace(),
                                        dimension, alignment^1, alignment);
        if ((alignment == LEADING && dst <= 0) || (alignment == TRAILING && dst >= 0)) {
            // now exclude resizing across an interval in the same sequence
            LayoutInterval resizing = movingComponents[0].getLayoutInterval(dimension);
            if (interval.isParentOf(resizing)) {
                if (!clearWayToParent(resizing, interval, dimension, alignment))
                    return false;
                if (toDeepToMerge(resizing, interval, alignment)) {
                    LayoutInterval neighbor = LayoutInterval.getNeighbor(resizing, alignment, true, true, false);
                    if (neighbor != null && interval.isParentOf(neighbor))
                        return false;
                }
                return true;
            }
            else {
                LayoutInterval commonParent = LayoutInterval.getCommonParent(interval, resizing);
                if (commonParent.isParallel())
                    return true;

                if (toDeepToMerge(resizing, commonParent, alignment))
                    return false;

                // if in a sequence, aligning is possible if the intervals don't overlap orthogonally
                resizing = getClearWayToParent(resizing, commonParent, dimension, alignment);
                if (resizing == null)
                    return false;

                while (interval.getParent() != commonParent) {
                    interval = interval.getParent();
                }

                int startIndex, endIndex;
                if (alignment == LEADING) {
                    startIndex = commonParent.indexOf(interval);
                    endIndex = commonParent.indexOf(resizing) - 1;
                }
                else { // TRAILING
                    startIndex = commonParent.indexOf(resizing) + 1;
                    endIndex = commonParent.indexOf(interval);
                }
                return startIndex <= endIndex
                       && !LayoutUtils.contentOverlap(movingSpace, commonParent, startIndex, endIndex, dimension^1);
            }
        }
        return false;
    }

    private static boolean clearWayToParent(LayoutInterval interval, LayoutInterval parent, int dimension, int alignment) {
        return getClearWayToParent(interval, parent, dimension, alignment) != null;
    }

    private static LayoutInterval getClearWayToParent(LayoutInterval interval,
            LayoutInterval topParent, int dimension, int alignment)
    {
        LayoutRegion space = interval.getCurrentSpace();
        LayoutInterval parent = interval.getParent();
        while (parent != topParent) {
            if (parent.isSequential()) {
                int startIndex, endIndex;
                if (alignment == LEADING) {
                    startIndex = 0;
                    endIndex = parent.indexOf(interval) - 1;
                }
                else {
                    startIndex = parent.indexOf(interval) + 1;
                    endIndex = parent.getSubIntervalCount() - 1;
                }
                if (startIndex <= endIndex
                    && LayoutUtils.contentOverlap(space, parent, startIndex, endIndex, dimension^1))
                {   // there is a sub-interval in the way
                    return null;
                }
            }
            interval = parent;
            parent = interval.getParent();
        }
        return interval;
    }

    // When resizing we can't attach the component edge to a component that is
    // more than one level of unaligned parallel groups away.
    // See LayoutFeeder.accommodateSizeInSequence.
    private static boolean toDeepToMerge(LayoutInterval interval, LayoutInterval parent, int alignment) {
        int level = 0;
        int a = DEFAULT;
        LayoutInterval prev = null;
        LayoutInterval p = interval.getParent();
        while (p != parent) {
            if (p.isParallel()) {
                if (a == DEFAULT) {
                    a = interval.getAlignment();
                    if (a != alignment)
                        level++;
                }
                else if (!LayoutInterval.isAlignedAtBorder(prev, p, a)) {
                    level++;
                    if (level > 1)
                        return true;
                }
                prev = p;
            }
            interval = p;
            p = interval.getParent();
        }
        return level >= 2;
    }

    /**
     * Finds value of padding between a moving component and given layout
     * interval.
     * @param alignment edge of the component
     */
    int[] findPaddings(LayoutInterval interval, LayoutInterval moving, PaddingType paddingType,
                       int dimension, int alignment)
    {
        int oppAlignment = (alignment == LEADING) ? TRAILING : LEADING;
        List movingComps = LayoutUtils.edgeSubComponents(moving, alignment, false);
        List fixedComps = LayoutUtils.edgeSubComponents(interval, oppAlignment, false);
        List sources = (alignment == LEADING) ? fixedComps : movingComps;
        List targets = (alignment == LEADING) ? movingComps : fixedComps;
        Map<String,LayoutRegion> map = new HashMap<String,LayoutRegion>();
        for (int i=0; i<movingComponents.length; i++) {
            map.put(movingComponents[i].getId(), movingBounds[i]);
        }
        return LayoutUtils.getSizesOfDefaultGap(sources, targets, paddingType,
                                                visualMapper, targetContainer.getId(), map);
    }

    /**
     * @return <= 0 if no indentation is recommended for given component pair
     */
    int findIndent(LayoutComponent mainComp, LayoutComponent indentedComp,
                          int dimension, int alignment)
    {
        return visualMapper.getPreferredPadding(mainComp.getId(), indentedComp.getId(),
            dimension, alignment, PaddingType.INDENT);
    }

    // -----
    // gap resizing

    private int resizeGap() {
        PositionDef gapPos = bestPositions[dimension];
        int resSize = movingSpace.size(dimension);
        int newSize = resSize; //resizingGap.currentSize + sizeChange;
        int[] defaultGapSizes = resizingGap.defaultGapSizes;
        gapPos.snapped = false;
        gapPos.paddingType = null;
        if (snapping && defaultGapSizes != null) {
            int bestDefaultIndex = -1;
            int bestDefaultDiff = Integer.MAX_VALUE;
            for (int i=0; i < defaultGapSizes.length; i++) {
                if (i == PaddingType.INDENT.ordinal()) {
                    continue;
                }
                int diff = Math.abs(newSize - defaultGapSizes[i]);
                if (diff < SNAP_DISTANCE && diff < bestDefaultDiff) {
                    bestDefaultIndex = i;
                    bestDefaultDiff = diff;
                }
            }
            if (bestDefaultIndex > -1) {
                newSize = defaultGapSizes[bestDefaultIndex];
                gapPos.snapped = true;
                gapPos.paddingType = defaultGapSizes.length > 1 ? PaddingType.values()[bestDefaultIndex] : null;
            }
        }
        gapPos.distance = newSize;
        return (gapPos.alignment == LEADING) ? (newSize - resSize) : (resSize - newSize);
    }

    // -----
    // innerclasses

    static class PositionDef {
        int distance = LayoutRegion.UNKNOWN;

        LayoutInterval interval;
        int alignment = LayoutRegion.NO_POINT;
        boolean nextTo;
        boolean snapped;
        PaddingType paddingType;
        private int[] paddingSizes; // remembered for painting

        private void reset() {
            distance = LayoutRegion.UNKNOWN;
            interval = null;
            alignment = LayoutRegion.NO_POINT;
        }

        private boolean isSet() {
            return interval != null;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("distance=").append(distance); // NOI18N
            sb.append(",alignment=").append(alignment); // NOI18N
            sb.append(",nextTo=").append(nextTo); // NOI18N
            sb.append(",snapped=").append(snapped); // NOI18N
            return sb.toString();
        }
    }

    /**
     * Keeps various information about original size of the resized component
     * (for one dimension). In case it is a container, it also keeps info about
     * the "container resizing gap" (if there is a resizing gap in the container
     * layout that follows the container resizing).
     */
    static class SizeDef {
        private int originalSize;
        private int minSize;
        private int contPrefSize = Short.MIN_VALUE;
        private int minSizeWithDefaultGap = Short.MIN_VALUE;
        private int minSizeWithZeroGap = Short.MIN_VALUE;
        private LayoutInterval adjustingGap; // inside resizing container
        private int originalGapSize;
        private int defaultGapSize;

        LayoutInterval getResizedGap() {
            return adjustingGap;
        }

        int getResizedGapSize(int currentSize) {
            if (adjustingGap == null) {
                return LayoutRegion.UNKNOWN;
            }
            if (currentSize == minSizeWithZeroGap) {
                return 0;
            }
            if (currentSize == minSizeWithDefaultGap) {
                return NOT_EXPLICITLY_DEFINED;
            }
            int gapSize;
            if (LayoutInterval.canResize(adjustingGap)) {
                gapSize = originalGapSize - originalSize + currentSize;
                if (gapSize == 0) {
                    if (minSizeWithZeroGap < 0 && adjustingGap.getMinimumSize() != 0) {
                        gapSize = NOT_EXPLICITLY_DEFINED;
                    }
                } else {
                    int minGapSize = defaultGapSize < 0 || adjustingGap.getMinimumSize() == 0 ?
                                     0 : defaultGapSize;
                    if (gapSize <= minGapSize) {
                        gapSize = NOT_EXPLICITLY_DEFINED;
                    }
                }
            } else {
                if (currentSize < minSize) {
                    gapSize = originalGapSize - minSize + currentSize;
                    if (gapSize < 0) {
                        gapSize = NOT_EXPLICITLY_DEFINED;
                    }
                } else {
                    return LayoutRegion.UNKNOWN;
                }
            }
            return gapSize;
        }

        int getOriginalSize() {
            return originalSize;
        }
    }
}
