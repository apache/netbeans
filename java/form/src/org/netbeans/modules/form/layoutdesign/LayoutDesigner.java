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


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import org.openide.loaders.DataObject;
import static org.netbeans.modules.form.layoutdesign.VisualState.GapInfo;

public final class LayoutDesigner implements LayoutConstants {

    private LayoutModel layoutModel;

    private VisualMapper visualMapper;

    private LayoutDragger dragger;

    private LayoutOperations operations;

    private VisualState visualState;

    private LayoutPainter layoutPainter;
    private boolean paintAlignment;
    private boolean paintGaps;

    private ModelHandler modelHandler;

    private List<LayoutComponent> selectedComponents = new LinkedList<LayoutComponent>();
    private GapInfo selectedGap;
    private LayoutInterval lastWheelGap;
    private boolean paintWheelGapSnap;
    private javax.swing.Timer wheelPaintTimer;

    private boolean updateDataAfterBuild = true;
    private boolean preferredSizeChanged;
    private boolean visualStateUpToDate;
    private boolean[] designerResized;

    private Collection<LayoutInterval>[] unresizedOnRemove;

    private static boolean forcePaintAlignment;
    private static boolean forcePaintGaps;

    // -----

    public LayoutDesigner(LayoutModel model, VisualMapper mapper) {
        layoutModel = model;
        visualMapper = mapper;
        operations = new LayoutOperations(model, mapper);
        visualState = new VisualState(model, mapper);
        layoutPainter = new LayoutPainter(model, visualState);

        String sysProp = System.getProperty("netbeans.form.paint_layout_alignment"); // NOI18N
        if ("false".equalsIgnoreCase(sysProp)) { // NOI18N
            forcePaintAlignment = true;
        } else {
            paintAlignment = true;
            if ("true".equalsIgnoreCase(sysProp)) { // NOI18N
                forcePaintAlignment = true;
            }
        }
        sysProp = System.getProperty("netbeans.form.paint_layout_gaps"); // NOI18N
        if ("false".equalsIgnoreCase(sysProp)) { // NOI18N
            forcePaintGaps = true;
        } else {
            paintGaps = true;
            if ("true".equalsIgnoreCase(sysProp)) { // NOI18N
                forcePaintGaps = true;
            }
        }

        modelHandler = new ModelHandler();
        layoutModel.setRemoveHandler(modelHandler);
        layoutModel.setResizeHandler(modelHandler);
        layoutModel.addListener(modelHandler);
    }

    private class ModelHandler implements LayoutModel.RemoveHandler, LayoutModel.ResizeHandler, LayoutModel.Listener {
        @Override
        public void removeComponents(LayoutComponent[] components, boolean fromModel) {
            LayoutDesigner.this.removeComponents(components, fromModel);
        }

        @Override
        public void setIntervalSize(LayoutInterval interval, int dimension, int min, int pref, int max) {
            LayoutDesigner.this.setIntervalSize(interval, dimension, min, pref, max);
        }

        @Override
        public void layoutChanged(LayoutEvent ev) {
            int type = ev.getType();
            if (type != LayoutEvent.INTERVAL_SIZE_CHANGED
                    && type != LayoutEvent.INTERVAL_PADDING_TYPE_CHANGED) {
                selectedGap = null;
                if (type == LayoutEvent.COMPONENT_UNREGISTERED && selectedComponents != null) {
                    for (Iterator<LayoutComponent> it = selectedComponents.iterator(); it.hasNext(); ) {
                        LayoutComponent comp = it.next();
                        if (layoutModel.getLayoutComponent(comp.getId()) == null) {
                            it.remove();
                        }
                    }
                }
            }
        }
    }

    /**
     * Must be called to make the LayoutDesigner properly handle certain changes
     * done directly on LayoutModel. In case of multiple designers it allows to
     * choose the right (currently active) one.
     */
    public void setActive(boolean active) {
        layoutModel.removeListener(modelHandler);
        if (active) {
            layoutModel.setRemoveHandler(modelHandler);
            layoutModel.setResizeHandler(modelHandler);
            layoutModel.addListener(modelHandler);
        } else {
            if (layoutModel.getRemoveHandler() == modelHandler) {
                layoutModel.setRemoveHandler(null);
            }
            if (layoutModel.getResizeHandler() == modelHandler) {
                layoutModel.setResizeHandler(null);
            }
        }
    }

    // -------
    // updates of the current visual state stored in the model

    /**
     * To be called after a container is newly built or changed in the designer.
     * Updates all containers in the tree according to the actual visual
     * appearance. Three types of updates may happen:
     * 1) The actual visual information associated with layout intervals is set
     *    (current space and difference from default size). This happens always,
     *    it does not change the model.
     * 2) Sizes of resizing components are updated in the layout intervals to
     *    match the current size of the design. This affects the model - may
     *    change some values, but not the structure.
     * 3) Structure of marked containers is optimized, potential problems fixed,
     *    e.g. after some user operations, or after opening just loaded design.
     *    Done for interval roots without ATTR_OPTIMIZED attribute (which is then set).
     * 
     * Updates 2) and 3) are only done when 'updateDataAfterBuild' field is set.
     * If they do some change in the model, rebuild of all visible containers is
     * requested immediately.
     * @return true if model has been changed during the update
     */
    public boolean updateCurrentState() {
        // TODO avoid updating one layout model by multiple designers
        if (logTestCode()) {
            testCode.add("// > UPDATE CURRENT STATE"); //NOI18N
	}
        Object changeMark = layoutModel.getChangeMark();
        List<LayoutComponent> updatedContainers = updateDataAfterBuild ? new LinkedList<LayoutComponent>()
                : null; // e.g. after undo we should not try to change anything in the model

        updateCurrentState(updatedContainers);

        preferredSizeChanged = false;
        updateDataAfterBuild = false;
        designerResized = null;

        if (updatedContainers != null && !updatedContainers.isEmpty()) {
            for (LayoutComponent comp : updatedContainers) {
                rebuildLayoutRecursively(comp);
            }
            updateCurrentState(null);
        }

        if (logTestCode()) {
            testCode.add("ld.updateCurrentState();"); //NOI18N
            testCode.add("// < UPDATE CURRENT STATE"); //NOI18N
        }

        visualStateUpToDate = true;
        return !changeMark.equals(layoutModel.getChangeMark());
    }

    private void rebuildLayoutRecursively(LayoutComponent container) {
        for (int i=0; i < container.getSubComponentCount(); i++) {
            LayoutComponent sub = container.getSubComponent(i);
            if (sub.isLayoutContainer()) {
                rebuildLayoutRecursively(sub);
            }
        }
        visualMapper.rebuildLayout(container.getId());
    }

    public void externalSizeChangeHappened() {
        visualStateUpToDate = false;
        updateDataAfterBuild = true;
        if (logTestCode()) {
            testCode.add("ld.externalSizeChangeHappened();"); // NOI18N
        }
    }

    /**
     * Used to inform about that the user resized the whole designer, which was
     * not done together with resizing a container (i.e. top level container is
     * of different kind). This means that all containers that might have been
     * resized indirectly with that should be updated to have their actual size
     * defined from inside.
     * @param contId
     * @param dimension 
     */
    public void designerResized(boolean horizontally, boolean vertically) {
        if (designerResized == null) {
            designerResized = new boolean[DIM_COUNT];
        }
        if (horizontally) {
            designerResized[HORIZONTAL] = true;
        }
        if (vertically) {
            designerResized[VERTICAL] = true;
        }
        if (logTestCode()) {
            testCode.add("ld.designerResized(" + horizontally + ", " + vertically + ");"); // NOI18N
        }
    }

    public boolean isPreferredSizeChanged() {
        return preferredSizeChanged;
    }

    private void requireStructureOptimization(LayoutComponent container) {
        if (container != null) {
            for (LayoutInterval[] roots : container.getLayoutRoots()) {
                for (int dim=0; dim < DIM_COUNT; dim++) {
                    roots[dim].unsetAttribute(LayoutInterval.ATTR_OPTIMIZED);
                }
            }
        }
    }

    public void componentDefaultSizeChanged(String compId) {
        if (layoutModel.isUndoRedoInProgress()) {
            return;
        }
        LayoutComponent comp = layoutModel.getLayoutComponent(compId);
        if (comp == null || comp.getParent() == null) {
            return;
        }
        Dimension prefSize = null;
        boolean rebuild = false;
        for (int dim=0; dim < DIM_COUNT; dim++) {
            LayoutInterval li = comp.getLayoutInterval(dim);
            if (!LayoutInterval.wantResize(li) && li.getPreferredSize() == NOT_EXPLICITLY_DEFINED) {
                int currSize = LayoutInterval.getCurrentSize(li, dim);
                if (currSize != LayoutRegion.UNKNOWN) {
                    if (prefSize == null) {
                        prefSize = visualMapper.getComponentPreferredSize(compId);
                    }
                    if (prefSize == null) {
                        return;
                    }
                    int grow = (dim==HORIZONTAL ? prefSize.width : prefSize.height) - currSize;
                    if (grow > 0) {
                        LayoutInterval p = li.getParent();
                        do {
                            int absorb;
                            if (p.isSequential()) {
                                absorb = p.getDiffToDefaultSize() - li.getDiffToDefaultSize();
                            } else {
                                absorb = LayoutInterval.getCurrentSize(p, dim) - LayoutInterval.getCurrentSize(li, dim);
                            }
                            if (absorb > 0) {
                                grow -= absorb;
                            }
                            li = p;
                            p = p.getParent();
                        } while (p != null && grow > 0);
                        if (grow > 0) { // can't absorb the grow without possibly
                            // shrinking some resizing component under default size
                            preferredSizeChanged = true;
                            if (li.getDiffToDefaultSize() > 0) { // absorb at least this part
                                setDefaultSizeInContainer(li, false); // li is root
                                rebuild = true;
                            }
                        }
                    }
                }
            }
        }
        if (rebuild) {
            visualMapper.rebuildLayout(comp.getParent().getId());
        }
    }

    private void updateCurrentState(Collection<LayoutComponent> updatedContainers) {
        List<LayoutComponent> l = new LinkedList<LayoutComponent>();
        for (LayoutComponent cont : layoutModel.getTopContainers()) {
            l.add(cont);
            while (!l.isEmpty()) {
                cont = l.remove(0);
                if (visualMapper.getComponentBounds(cont.getId()) != null) {
                    break;
                } else { // container not built, but some sub-container could be
                    for (int i=0; i < cont.getSubComponentCount(); i++) {
                        LayoutComponent sub = cont.getSubComponent(i);
                        if (sub.isLayoutContainer()) {
                            l.add(sub);
                        }
                    }
                    cont = null;
                }
            }
            l.clear();

            if (cont != null) { // this container is built (in designer)
                Object mark = layoutModel.getChangeMark();
                updateContainerAfterBuild(cont, true);
                if (updatedContainers != null && !layoutModel.getChangeMark().equals(mark)) {
                    updatedContainers.add(cont); // something has changed
                }
            }
        }
    }

    public void dumpTestcode(DataObject form) {
        LayoutTestUtils.dumpTestcode(testCode, form, getModelCounter());
        testCode = new ArrayList<String>();
        testCode0 = new ArrayList<String>();
        beforeMove = new ArrayList<String>();
        move1 = new ArrayList<String>();
        move2 = new ArrayList<String>();
        isMoving = false;
    }
    
    // -----
    // adding, moving, resizing

    /**
     * Determines a subset of components that can be visually dragged together.
     * If the components are in diferrent containers then no dragging is
     * possible (returns empty list). Otherwise the components that have the
     * same parent and are under the same layout roots are returned.
     * Nonexisting or parent-less components are ignored.
     * @param component Ids of components selected for dragging
     * @return List of Ids of components that can be dragged together
     */
    public List<String> getDraggableComponents(List<String> componentIds) {
        List<LayoutComponent> components = new ArrayList<>(componentIds.size());
        for (String compId : componentIds) {
            LayoutComponent comp = layoutModel.getLayoutComponent(compId);
            if (comp != null) {
                components.add(comp);
            }
        }

        LayoutComponent container = null;
        List<String> draggable = null;
        int commonLayer = -1;
        for (LayoutComponent comp : components) {
            if (comp.getParent() == null) {
                continue;
            }
            if (container == null) {
                container = comp.getParent();
            } else if (comp.getParent() != container) {
                return Collections.emptyList();
            }

            int layerIndex = container.getLayoutRootsIndex(comp.getLayoutInterval(HORIZONTAL));
            assert layerIndex >= 0;
            if (layerIndex >= commonLayer) {
                if (commonLayer < 0) {
                    draggable = new ArrayList<String>(componentIds.size());
                } else if (layerIndex > commonLayer) {
                    draggable.clear();
                }
                commonLayer = layerIndex;
                draggable.add(comp.getId());
            }
        }
        if (draggable == null) {
            draggable = Collections.emptyList();
        }
        if (draggable.isEmpty() && components.size() == 1 && paintGaps && selectedGap != null) {
            LayoutComponent comp = components.get(0);
            if (comp.isLayoutContainer()
                    && comp.getDefaultLayoutRoot(selectedGap.dimension).isParentOf(selectedGap.gap)) {
                // root container with selected gap is considered "draggable"
                return Collections.singletonList(comp.getId());
            }
        }
        return draggable;
    }

    /**
     * Checks whether given component is "unplaced" - i.e. in other than default
     * layer. Result of adding without mouse positioning (e.g. copying). In
     * current model such a component needs to be moved additionally by the user
     * to be placed in the default layer among other components.
     * @param compId id of the component
     * @return true if the component is placed in an additional layer
     */
    public boolean isUnplacedComponent(String compId) {
        LayoutComponent comp = layoutModel.getLayoutComponent(compId);
        return comp != null ? LayoutComponent.isUnplacedComponent(comp) : false;
    }

    public void startAdding(LayoutComponent[] comps,
                            Rectangle[] bounds,
                            Point hotspot,
                            String defaultContId)
    {
        if (logTestCode()) {
            testCode.add("// > START ADDING"); //NOI18N
	}
        prepareDragger(comps, bounds, hotspot, LayoutDragger.ALL_EDGES);
        if (logTestCode()) {
            testCode.add("{"); // NOI18N
	    // lc should be already filled in the MetaComponentCreator.getPrecreatedComponent
            LayoutTestUtils.writeLayoutComponentArray(testCode, "comps", "lc");				    //NOI18N
            LayoutTestUtils.writeRectangleArray(testCode, "bounds", bounds);				    //NOI18N
            LayoutTestUtils.writeString(testCode, "defaultContId", defaultContId);			    //NOI18N         
            testCode.add("Point hotspot = new Point(" + (int)(hotspot.getX()) + "," +			    //NOI18N
			    (int)(hotspot.getY()) + ");");						    //NOI18N
            testCode.add("ld.startAdding(comps, bounds, hotspot, defaultContId);");			    //NOI18N
            testCode.add("}");										    //NOI18N
        }
        if (defaultContId != null) {
            setDragTarget(layoutModel.getLayoutComponent(defaultContId), comps, false);
        }
        if (logTestCode()) {
            testCode.add("// < START ADDING"); //NOI18N
	}
    }
    
    public void startMoving(String[] compIds, Rectangle[] bounds, Point hotspot) {
        if (logTestCode()) {
            testCode.add("// > START MOVING"); //NOI18N
        }

        LayoutComponent[] comps = new LayoutComponent[compIds.length];
        for (int i=0; i < compIds.length; i++) {
            comps[i] = layoutModel.getLayoutComponent(compIds[i]);
        }
        prepareDragger(comps, bounds, hotspot, LayoutDragger.ALL_EDGES);

        if (logTestCode()) {
            testCode.add("{"); //NOI18N
            LayoutTestUtils.writeStringArray(testCode, "compIds", compIds);		//NOI18N
            LayoutTestUtils.writeRectangleArray(testCode, "bounds", bounds);		//NOI18N
            testCode.add("Point hotspot = new Point(" + (int)(hotspot.getX()) + "," +   //NOI18N
		    (int)(hotspot.getY()) + ");");					//NOI18N
            testCode.add("ld.startMoving(compIds, bounds, hotspot);");			//NOI18N
            testCode.add("}");								//NOI18N
        }
        
        setDragTarget(comps[0].getParent(), comps, false);

        if (logTestCode()) {
            testCode.add("// < START MOVING"); //NOI18N
	}
    }

    // [change to one component only?]
    public boolean startResizing(String[] compIds,
                                 Rectangle[] bounds,
                                 Point hotspot,
                                 int[] resizeEdges,
                                 boolean inLayout)
    {
        LayoutComponent[] comps = new LayoutComponent[compIds.length];
        for (int i=0; i < compIds.length; i++) {
            comps[i] = layoutModel.getLayoutComponent(compIds[i]);
        }

        int gapResizability = getSelectedGapResizability0(hotspot);
        if (gapResizability == LEADING || gapResizability == TRAILING) {
            if (resizeEdges[selectedGap.dimension] == gapResizability
                    && comps[0].isLayoutContainer()
                    && comps[0].getDefaultLayoutRoot(selectedGap.dimension).isParentOf(selectedGap.gap)) {
                // going to just resize a gap (simplified dragging mode)
                lastWheelGap = null;
                dragger = new LayoutDragger(selectedGap, gapResizability, new int[] { hotspot.x, hotspot.y },
                                            visualMapper, layoutPainter);
                return false;
            }
        }

        int[] edges = new int[DIM_COUNT];
        for (int i=0; i < DIM_COUNT; i++) {
            edges[i] = resizeEdges[i] == LEADING || resizeEdges[i] == TRAILING ?
                       resizeEdges[i] : LayoutRegion.NO_POINT;
        }

        if (logTestCode()) {
            testCode.add("// > START RESIZING"); //NOI18N
        }

        prepareDragger(comps, bounds, hotspot, edges);

        if (logTestCode()) {
            testCode.add("{"); //NOI18N
            LayoutTestUtils.writeStringArray(testCode, "compIds", compIds);		//NOI18N
            LayoutTestUtils.writeRectangleArray(testCode, "bounds", bounds);		//NOI18N
            testCode.add("Point hotspot = new Point(" + (int)(hotspot.getX()) + "," +   //NOI18N
		    (int)(hotspot.getY()) + ");");					//NOI18N
            LayoutTestUtils.writeIntArray(testCode, "resizeEdges", resizeEdges);	//NOI18N
            testCode.add("boolean inLayout = " + inLayout + ";");			// NOI18N
            testCode.add("ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);"); //NOI18N
            testCode.add("}");								//NOI18N
        }

        if (inLayout) {
            setDragTarget(comps[0].getParent(), comps, true);
        } else {
            setDragTarget(null, null, true);
        }

        if (logTestCode()) {
            testCode.add("// < START RESIZING"); //NOI18N
	}
        return true;
    }

    private void prepareDragger(LayoutComponent[] comps,
                                Rectangle[] bounds,
                                Point hotspot,
                                int[] edges)
    {
        if (comps.length != bounds.length)
            throw new IllegalArgumentException();

        LayoutRegion[] movingFormation = new LayoutRegion[bounds.length];
        for (int i=0; i < bounds.length; i++) {
            int baseline = visualMapper.getBaselinePosition(comps[i].getId(), bounds[i].width, bounds[i].height);
            int baselinePos = baseline > 0 ? bounds[i].y + baseline : LayoutRegion.UNKNOWN;
            movingFormation[i] = new LayoutRegion();
            movingFormation[i].set(bounds[i], baselinePos);
        }

        dragger = new LayoutDragger(comps,
                                    movingFormation,
                                    new int[] { hotspot.x, hotspot.y },
                                    edges,
                                    visualMapper, layoutPainter);
    }

    /**
     * @param p mouse cursor position in coordinates of the whole design area;
     *        it is adjusted if the position is changed (due to a snap effect)
     * @param containerId for container the cursor is currently moved over,
     *        can be null if e.g. a root container is resized
     * @param autoPositioning if true, searching for optimal position will be
     *        performed - a found position will be painted and the moving
     *        component snapped to it
     * @param lockDimension if true, one dimension is locked for this move
     *        (does not change); the dimension to lock must have aligned
     *        position found in previous move steps - if this is true for both
     *        dimensions then the one with smaller delta is chosen
     * @param bounds (output) bounds of moving components after the move
     */
    public void move(Point p,
                     String containerId,
                     boolean autoPositioning,
                     boolean lockDimension,
                     Rectangle[] bounds) {

        if (!visualStateUpToDate || dragger == null) {
            return; // visual state of layout structure not updated yet (from last operation)
        }

	int x = (p != null) ? p.x : 0;
	int y = (p != null) ? p.y : 0;

        boolean logTestCode = logTestCode() && dragger.getResizingGap() == null;
	if (logTestCode) {
            // this terrible code here is to store only two last move() calls
            if (!isMoving) {
                isMoving = true;
                // backup all current entries and clear the testcode list
                beforeMove = new ArrayList<String>();
                beforeMove.addAll(testCode);
                testCode = new ArrayList<String>();
		lastMovePoint = new Point(0,0);
            }

	    if (!((x == lastMovePoint.x) && (y == lastMovePoint.y))) {
		lastMovePoint = new Point(x, y);
                move1 = move2;
		testCode0 = testCode;
	    }

            move2 = new ArrayList<String>();
            move2.add("// > MOVE");
            testCode = new ArrayList<String>();
        }

        if (!dragger.isResizing() && (!lockDimension || dragger.getTargetContainer() == null)) {
            setDragTarget(layoutModel.getLayoutComponent(containerId), dragger.getMovingComponents(), false);
        }

        cursorPos[HORIZONTAL] = p.x;
        cursorPos[VERTICAL] = p.y;

        dragger.move(cursorPos, autoPositioning, lockDimension);
        
        p.x = cursorPos[HORIZONTAL];
        p.y = cursorPos[VERTICAL];

        if (bounds != null && dragger.getMovingBounds() != null) {
            LayoutRegion[] current = dragger.getMovingBounds();
            for (int i=0; i < current.length; i++) {
                current[i].toRectangle(bounds[i]);
            }
        }

        if (logTestCode) {
            move2.add("{"); //NOI18N
            move2.add("Point p = new Point(" + x + "," + y + ");"); //NOI18N
            LayoutTestUtils.writeString(move2, "containerId", containerId); //NOI18N
            move2.add("boolean autoPositioning = " + autoPositioning + ";"); //NOI18N
            move2.add("boolean lockDimension = " + lockDimension + ";"); //NOI18N
            LayoutTestUtils.writeRectangleArray(move2, "bounds", bounds); //NOI18N
            move2.add("ld.move(p, containerId, autoPositioning, lockDimension, bounds);"); //NOI18N
            move2.add("}"); //NOI18N
            move2.add("// < MOVE"); //NOI18N
        }
    }

    private void setDragTarget(LayoutComponent targetContainer, LayoutComponent[] movingComps, boolean resizing) {
        LayoutComponent prevContainer = dragger.getTargetContainer();
        LayoutInterval[] roots;
        if (targetContainer != null) {
            roots = resizing && movingComps.length > 0
                    ? movingComps[0].getParentRoots()
                    : getActiveLayoutRoots(targetContainer);
        } else {
            roots = null;
        }
        dragger.setTargetContainer(targetContainer, roots);

        if (prevContainer != targetContainer) {
            updateDraggingVisibility(prevContainer, movingComps, resizing, false);
            updateDraggingVisibility(targetContainer, movingComps, resizing, true);
        }
    }

    /**
     * Returns the current target container after the last dragging operation
     * (i.e. after the last call of 'move'). It may be different from the
     * container originally passed to 'move'.
     */
    public String getDragTargetContainer() {
        LayoutComponent comp = dragger.getTargetContainer();
        return comp != null ? comp.getId() : null;
    }

    // temporarily hide components from other layers when dragging in a container
    private void updateDraggingVisibility(LayoutComponent container, LayoutComponent[] movingComps, boolean resizing, boolean draggingIn) {
        if (container != null) {
            LayoutInterval[] targetRoots = resizing && movingComps.length > 0
                    ? movingComps[0].getParentRoots()
                    : getActiveLayoutRoots(container);
            if (targetRoots != null) {
                for (LayoutComponent comp : container.getSubcomponents()) {
                    for (LayoutComponent m : movingComps) {
                        if (m == comp) {
                            comp = null;
                            break;
                        }
                    }
                    if (comp != null && LayoutInterval.getRoot(comp.getLayoutInterval(HORIZONTAL)) != targetRoots[HORIZONTAL]) {
                        visualMapper.setComponentVisibility(comp.getId(), !draggingIn);
                    }
                }
            }
        }
    }

    public void endMoving(boolean committed) {
        if (dragger == null) {
            return; // redundant call
        }

        boolean logTestCode = logTestCode() && dragger.getResizingGap() == null;
        if (logTestCode) {
            if (committed) {
                beforeMove.addAll(testCode0);
                beforeMove.addAll(move1);
                beforeMove.addAll(testCode);
                beforeMove.addAll(move2);
                testCode = beforeMove;
            }
            testCode.add("// > END MOVING"); //NOI18N
            isMoving = false;
        }
        try {
            LayoutComponent targetContainer = dragger.getTargetContainer();
            LayoutComponent[] components = dragger.getMovingComponents();
            updateDraggingVisibility(targetContainer, components, dragger.isResizing(), false);

            if (!committed) {
                return;
            }

            if (dragger.getResizingGap() != null) { // resizng gap
                GapInfo resizedGap = dragger.getResizingGap();
                LayoutDragger.PositionDef result = dragger.getPositions()[resizedGap.dimension];
                int newSize = result.snapped ? NOT_EXPLICITLY_DEFINED : result.distance;
                layoutModel.setUserIntervalSize(resizedGap.gap, resizedGap.dimension, newSize);
                if (newSize == NOT_EXPLICITLY_DEFINED && result.paddingType != null) {
                    layoutModel.setPaddingType(resizedGap.gap, result.paddingType);
                }
                // TODO test code for setting the new size of the gap
            } else if (targetContainer != null) { // adding/moving/resizing a component in layout container
                LayoutFeeder f = null;
                boolean newComponents = components[0].getParent() == null;
                // determine the interval to add in each dimension
                LayoutInterval[] addingInts;
                if (components.length > 1) {
                    if (newComponents) { // adding multiple new components (not in layout)
                        // (special case - moving multiple components from another layout)
                        LayoutRegion movingSpace = dragger.getMovingSpace();
                        int dx = movingSpace.positions[HORIZONTAL][LEADING];
                        int dy = movingSpace.positions[VERTICAL][LEADING];
                        LayoutRegion[] movingBounds = dragger.getMovingBounds();
                        Map<LayoutComponent, Rectangle> compToRect = new HashMap<LayoutComponent, Rectangle>();
                        for (int i=0; i < components.length; i++) {
                            for (int dim=0; dim < DIM_COUNT; dim++) {
                                components[i].getLayoutInterval(dim).getCurrentSpace().set(dim, movingBounds[i]);
                            }
                            Rectangle r = movingBounds[i].toRectangle(new Rectangle());
                            r.x -= dx;
                            r.y -= dy;
                            compToRect.put(components[i], r);
                        }
                        addingInts = layoutModel.createIntervalsFromBounds(compToRect);
                    } else { // moving multiple existing components (already in layout, no resizing)
                        f = new LayoutFeeder(components, targetContainer, operations, dragger);
                        LayoutInterval[] commonParents = new LayoutInterval[DIM_COUNT];
                        Map<LayoutComponent, LayoutComponent> compMap = new HashMap<LayoutComponent, LayoutComponent>();
                        LayoutRegion origSpace = new LayoutRegion();
                        for (LayoutComponent comp : components) {
                            for (int dim=0; dim < DIM_COUNT; dim++) {
                                if (commonParents[dim] == null) {
                                    commonParents[dim] = comp.getLayoutInterval(dim);
                                } else {
                                    commonParents[dim] = LayoutInterval.getCommonParent(
                                            commonParents[dim], comp.getLayoutInterval(dim));
                                }
                            }
                            compMap.put(comp, comp); // moving the same components
                            origSpace.expand(comp.getLayoutInterval(HORIZONTAL).getCurrentSpace());
                        }
                        unresizedOnRemove = null;
                        addingInts = new LayoutInterval[DIM_COUNT];
                        for (int dim=0; dim < DIM_COUNT; dim++) {
                            Object start = layoutModel.getChangeMark();
                            addingInts[dim] = restrictedCopy(commonParents[dim], compMap, origSpace, dim, null);
                            visualState.updateSpaceAfterRemove(components[0].getParent().getDefaultLayoutRoot(dim), dim);
                            Object end = layoutModel.getChangeMark();
                            f.setUndo(start, end, dim);
                        }
                        for (int i=0; i < components.length; i++) {
                            LayoutRegion bounds = dragger.getMovingBounds()[i];
                            for (int dim=0; dim < DIM_COUNT; dim++) {
                                components[i].getLayoutInterval(dim).getCurrentSpace().set(dim, bounds);
                            }
                        }
                        for (int dim=0; dim < DIM_COUNT; dim++) {
                            addingInts[dim].getCurrentSpace().set(dragger.getMovingSpace());
                            visualState.updateCurrentSpaceOfGroups(addingInts[dim], dim, null);
                        }
                        f.setUp(origSpace, unresizedOnRemove);
                        unresizedOnRemove = null;
                        // component intervals were moved to the new enclosing group,
                        // so also remove the components from their container
                        for (LayoutComponent comp : components) {
                            if (comp.getParent() != targetContainer) {
                                layoutModel.removeComponent(comp, false);
                                layoutModel.addComponent(comp, targetContainer, -1);
                            }
                        }
                    }
                } else { // one component (adding/moving/resizing)
                    addingInts = new LayoutInterval[DIM_COUNT];
                    LayoutComponent comp = components[0];
                    for (int dim=0; dim < DIM_COUNT; dim++) {
                        addingInts[dim] = comp.getLayoutInterval(dim);
                    }
                }

                if (newComponents) { // ensure the interval size matches the real component size
                    LayoutRegion[] movingBounds = dragger.getMovingBounds();
                    for (int i=0; i < components.length; i++) {
                        LayoutComponent comp = components[i];
                        Dimension preferred = visualMapper.getComponentPreferredSize(comp.getId());
                        for (int dim=0; dim < DIM_COUNT; dim++) {
                            LayoutInterval li = comp.getLayoutInterval(dim);
                            li.setAttribute(LayoutInterval.ATTR_FLEX_SIZEDEF);
                            int size = movingBounds[i].size(dim);
                            if (preferred == null || size != ((dim == HORIZONTAL) ? preferred.width : preferred.height)) {
                                li.setPreferredSize(size);
                            }
                        }
                    }
                }

                addComponents(components, targetContainer, addingInts, newComponents, f);
            } else { // resizing root container
                assert dragger.isResizing();

                LayoutRegion space = dragger.getMovingBounds()[0];
                for (int dim=0; dim < DIM_COUNT; dim++) {
                    components[0].getLayoutInterval(dim).setCurrentSpace(space);
                }
                if (components[0].isLayoutContainer()) {
                    updateContainerAfterResized(components[0], dragger.getSizes());
                }
            }

            visualStateUpToDate = false;
            updateDataAfterBuild = true;
        } finally {
            dragger = null;
            if (logTestCode) {
                testCode.add("ld.endMoving(" + committed + ");"); //NOI18N
                testCode.add("// < END MOVING"); //NOI18N
            }
        }
    }

    private void addComponents(LayoutComponent[] components, LayoutComponent targetContainer, LayoutInterval[] addingInts, boolean freshComponents, LayoutFeeder f) {
        LayoutFeeder layoutFeeder;
        if (f != null) {
            layoutFeeder = f;
        } else {
            layoutFeeder = createFeeder(components, targetContainer);
            // [HACK]
             for (int dim=0; dim < DIM_COUNT; dim++) {
                 LayoutRegion space = new LayoutRegion();
                 space.set(dragger.getMovingSpace());
                 addingInts[dim].setCurrentSpace(space);
            }
        }

        LayoutInterval.prepareDiagnostics(); // bug 240634/222703
        try {
            // add the components' intervals
            layoutFeeder.add(addingInts);
        } finally {
            LayoutInterval.cleanDiagnostics();
        }

        if (dragger.isResizing()) {
            for (LayoutComponent comp : components) {
                if (comp.isLayoutContainer()) {
                    // container size needs to be defined from inside before the layout is built
                    updateContainerAfterResized(components[0], dragger.getSizes());
                } else { // component might have been resized to default size
                    for (int dim=0; dim < DIM_COUNT; dim++) {
                        LayoutInterval compInt = comp.getLayoutInterval(dim);
                        int size = compInt.getPreferredSize();
                        if (dragger.isResizing(dim) && size != NOT_EXPLICITLY_DEFINED) {
                            boolean setToDefault = false;
                            if (dragger.componentSnappedToDefaultSize(dim)) {
                                setToDefault = true;
                            } else if (compInt.getAlignment() == CENTER || compInt.getAlignment() == BASELINE) {
                                // center or baseline resizing needs extra check for default size
                                java.awt.Dimension prefSize = visualMapper.getComponentPreferredSize(comp.getId());
                                int defaultSize = (dim == HORIZONTAL) ? prefSize.width : prefSize.height;
                                if (defaultSize == size) {
                                    setToDefault = true;
                                }
                            }
                            if (setToDefault) {
                                operations.resizeInterval(comp.getLayoutInterval(dim), NOT_EXPLICITLY_DEFINED);
                            }
                        }
                    }
                }
            }
        }

        if (layoutFeeder.optimizeStructure) {
            requireStructureOptimization(targetContainer);
        }
    }

    private LayoutFeeder createFeeder(LayoutComponent[] components, LayoutComponent targetContainer) {
        assert components.length == 1 || (components.length  > 1 && components[0].getParent() == null);
        LayoutFeeder feeder = new LayoutFeeder(components, targetContainer, operations, dragger);
        unresizedOnRemove = null;
        LayoutRegion origSpace = null;
        for (LayoutComponent comp : components) {
            if (comp.getParent() != null) {
                if (origSpace == null) {
                    origSpace = new LayoutRegion();
                    origSpace.set(comp.getCurrentSpace());
                } else {
                    origSpace.expand(comp.getCurrentSpace());
                }
                for (int dim=0; dim < DIM_COUNT; dim++) {
                    LayoutInterval compInt = comp.getLayoutInterval(dim);
                    if (compInt.getParent() != null) {
                        Object start = layoutModel.getChangeMark();
                        removeComponentInterval(compInt, dim);
                        visualState.updateSpaceAfterRemove(comp.getParent().getDefaultLayoutRoot(dim), dim);
                        if (dragger.isResizing(dim)) {
                            layoutModel.removeComponentFromLinkSizedGroup(comp, dim);
                        }
                        Object end = layoutModel.getChangeMark();
                        feeder.setUndo(start, end, dim);
                    }
                }
                if (comp.getParent() != targetContainer) {
                    layoutModel.removeComponent(comp, false);
                }
            }
            if (comp.getParent() == null) {
                layoutModel.addComponent(comp, targetContainer, -1);
            }
        }
        feeder.setUp(origSpace, unresizedOnRemove);
        unresizedOnRemove = null;
        return feeder;
    }

    private void addUnspecified(LayoutComponent[] components, LayoutComponent targetContainer, LayoutInterval[] addingInts) {
        // add the components to the model (to the target container)
        for (LayoutComponent comp : components) {
            layoutModel.addComponent(comp, targetContainer, -1);
        }
        LayoutInterval[] targetRoots = layoutModel.addNewLayoutRoots(targetContainer);
        for (int dim=0; dim < DIM_COUNT; dim++) {
            LayoutInterval interval = addingInts[dim];
            LayoutInterval seq = interval.isSequential() ? interval : new LayoutInterval(SEQUENTIAL);
            LayoutInterval gap = new LayoutInterval(SINGLE);
            boolean resizing = LayoutInterval.wantResize(interval);
            if (!resizing) {
                gap.setSizes(0, 0, Short.MAX_VALUE);
            }
            seq.add(gap, 0);
            if (interval != seq) {
                layoutModel.addInterval(interval, seq, -1);
                layoutModel.setIntervalAlignment(interval, DEFAULT);
            }
            gap = new LayoutInterval(SINGLE);
            if (!resizing) {
                gap.setSizes(0, 0, Short.MAX_VALUE);
            }
            seq.add(gap, -1);
            layoutModel.addInterval(seq, targetRoots[dim], -1);
        }
    }

    private void addToEmpty(LayoutComponent[] components, LayoutComponent targetContainer, LayoutInterval[] addingInts) {
        assert targetContainer.getSubComponentCount() == 0;
        for (LayoutComponent comp : components) {
            layoutModel.addComponent(comp, targetContainer, -1);
        }
        LayoutInterval[] roots = getActiveLayoutRoots(targetContainer);
        for (int dim=0; dim < DIM_COUNT; dim++) {
            LayoutInterval root = roots[dim];
            assert root.isParallel();
            LayoutInterval interval = addingInts[dim];
            if (!interval.isParallel()) {
                layoutModel.addInterval(interval, root, -1);
            } else {
                while (interval.getSubIntervalCount() > 0) {
                    LayoutInterval li = layoutModel.removeInterval(interval, 0);
                    layoutModel.addInterval(li, root, -1);
                }
            }
        }
    }

    /**
     * Creates copy of the given interval restricted to specified components
     * and region (<code>space</code>).
     *
     * @param interval interval whose restricted copy should be created.
     * @param componentMap components that determine the restriction.
     * @param space region (current space) that determine the restriction.
     * @param dimension dimension that restricted layout interval belongs to.
     * @param temp internal helper parameter for recursive invocation.
     * Pass <code>null</code> when you invoke this method.
     */
    private LayoutInterval restrictedCopy(LayoutInterval interval,
        Map<LayoutComponent, LayoutComponent> componentMap, LayoutRegion space, int dimension, List<Object> temp) {
        boolean processTemp = (temp == null);
        if (temp == null) {
            temp = new LinkedList<Object>();
        }
        if (interval.isGroup()) {
            boolean parallel = interval.isParallel();
            LayoutInterval copy = LayoutInterval.cloneInterval(interval, null);
            Iterator iter = interval.getSubIntervals();
            int compCount = 0; // Number of components already copied to the group
            boolean includeGap = false; // Helper variables that allow us to insert gaps
            int firstGapToInclude = 0;  // instead of components that has been filtered out.
            int gapStart = interval.getCurrentSpace().positions[dimension][LEADING];
            while (iter.hasNext()) {
                LayoutInterval sub = (LayoutInterval)iter.next();
                LayoutInterval subCopy = restrictedCopy(sub, componentMap, space, dimension, temp);
                if (subCopy != null) {
                    if (!sub.isEmptySpace()) {
                        if (includeGap) {
                            gapStart = Math.max(space.positions[dimension][LEADING], gapStart);
                            int size = sub.getCurrentSpace().positions[dimension][LEADING] - gapStart;
                            integrateGap(copy, size, firstGapToInclude);
                            includeGap = false;
                        }
                        gapStart = sub.getCurrentSpace().positions[dimension][TRAILING];
                        firstGapToInclude = copy.getSubIntervalCount();
                    }                    
                    if (sub.isComponent()) {
                        // Remember where to add component intervals - they cannot
                        // be moved immediately because the model listener would
                        // destroy the adjacent intervals before we would be able
                        // to copy them.
                        temp.add(subCopy);
                        temp.add(copy);
                        temp.add(new Integer(subCopy.getRawAlignment()));
                        temp.add(new Integer(copy.getSubIntervalCount() + compCount));
                        compCount++;
                    } else {
                        layoutModel.addInterval(subCopy, copy, -1);
                    }
                } else {
                    if (!parallel) {
                        includeGap = true;
                    }
                }
            }
            if (includeGap) {
                gapStart = Math.max(space.positions[dimension][LEADING], gapStart);
                int gapEnd = Math.min(space.positions[dimension][TRAILING], interval.getCurrentSpace().positions[dimension][TRAILING]);
                integrateGap(copy, gapEnd - gapStart, firstGapToInclude);
            }
            if (copy.getSubIntervalCount() + compCount > 0) {
                if (processTemp) {
                    // Insert component intervals
                    iter = temp.iterator();
                    while (iter.hasNext()) {
                        LayoutInterval comp = (LayoutInterval)iter.next();
                        LayoutInterval parent = (LayoutInterval)iter.next();
                        int alignment = ((Integer)iter.next()).intValue();
                        int index = ((Integer)iter.next()).intValue();
                        if (comp.getParent() != null) { // component reused - not copied, just moved
                            removeComponentInterval(comp, dimension);
                        }
                        layoutModel.setIntervalAlignment(comp, alignment);
                        layoutModel.addInterval(comp, parent, index);
                    }
                    // Consolidate the groups where the components has been added
                    iter = temp.iterator();
                    while (iter.hasNext()) {
                        iter.next(); // skip the component
                        LayoutInterval group = (LayoutInterval)iter.next();
                        iter.next(); iter.next(); // skip alignment and index
                        LayoutInterval parent = group.getParent();
                        while (group.getSubIntervalCount() == 1 && parent != null) {
                            LayoutInterval sub = group.getSubInterval(0);
                            layoutModel.removeInterval(sub);
                            int alignment = group.getAlignment();
                            int index = layoutModel.removeInterval(group);
                            layoutModel.setIntervalAlignment(sub, alignment);
                            layoutModel.addInterval(sub, parent, index);
                            group = sub;
                        }
                    }
                    compCount = 0;
                }
                // consolidate copy
                if ((copy.getSubIntervalCount() == 1) && (compCount == 0)) {
                    LayoutInterval subCopy = copy.getSubInterval(0);
                    layoutModel.removeInterval(subCopy);
                    layoutModel.setIntervalAlignment(subCopy, copy.getAlignment());
                    if (copy.isSequential() && subCopy.isEmptySpace()) {
                        copy = null;
                    } else {
                        copy = subCopy;
                    }
                }
                return copy;
            } else {
                return null;
            }
        } else if (interval.isComponent()) {
            LayoutComponent comp = componentMap.get(interval.getComponent());
            if (comp != null) {
                if (comp != interval.getComponent()) { // there is a copied component available
                    interval = LayoutInterval.cloneInterval(interval, comp.getLayoutInterval(dimension));
                } // otherwise the component will be moved from its original location
                return interval;
            } else { // skip this component
                return null;
            }
        } else {
            assert interval.isEmptySpace();
            int[] bounds = LayoutInterval.getCurrentPositions(interval, dimension);
            int rangeStart = space.positions[dimension][LEADING];
            int rangeEnd = space.positions[dimension][TRAILING];
            if ((bounds[0] < rangeEnd) && (bounds[1] > rangeStart)) {
                LayoutInterval gap = new LayoutInterval(SINGLE);
                gap.setAttributes(interval.getAttributes());
                if ((bounds[0] < rangeStart) || (bounds[1] > rangeEnd)) {
                    // Partial overlap with the provides space
                    int min = interval.getMinimumSize();
                    if (min >= 0) min = USE_PREFERRED_SIZE;
                    int pref = Math.min(bounds[1], rangeEnd) - Math.max(bounds[0], rangeStart);
                    int max = interval.getMaximumSize();
                    if (max >= 0) max = USE_PREFERRED_SIZE;
                    gap.setSizes(min, pref, max);
                } else {
                    gap.setSizes(interval.getMinimumSize(), interval.getPreferredSize(), interval.getMaximumSize());
                    gap.setPaddingType(interval.getPaddingType());
                }
                return gap;
            } else {
                // Outside the provided space
                return null;
            }
        }        
    }

    /**
     * Helper method used by <code>restrictedCopy()</code> method.
     * Replaces empty spaces at the end of the sequential group
     * by an empty space of the specified size. Only empty spaces
     * with index >= boundary are replaced.
     *
     * @param seqGroup sequential group.
     * @param size size of the empty space that should be added.
     * @param boundary index in the sequential group that limits
     * the replacement of the empty spaces.
     */
    private void integrateGap(LayoutInterval seqGroup, int size, int boundary) {
        while ((seqGroup.getSubIntervalCount() > boundary)
            && seqGroup.getSubInterval(seqGroup.getSubIntervalCount()-1).isEmptySpace()) {
            layoutModel.removeInterval(seqGroup.getSubInterval(seqGroup.getSubIntervalCount()-1));
        }
        if (size > 0) {
            LayoutInterval gap = new LayoutInterval(SINGLE);
            gap.setSize(size);
            layoutModel.addInterval(gap, seqGroup, -1);
        }
    }
    
    /**
     * Removes currently dragged components from layout model. Called when
     * the components were dragged out of the form (or to a container not
     * managed by this layout model).
     */
    public void removeDraggedComponents() {
        if (dragger != null) {
            removeComponentsFromParent(dragger.getMovingComponents());
            endMoving(false);
        }
    }

    public void paintMoveFeedback(Graphics2D g) {
        if (dragger != null) { // Dragger might not be initialized yet
            dragger.paintMoveFeedback(g);
        }
    }

    public boolean acceptsMouseWheel(Point p) { // TODO also accept out of gap if it went out during resizing
        return pointInSelectegGap(p);
    }

    /**
     * @param events Number of coalesced wheel events.
     * @param units Total number of units to scroll.
     * @return true if the applied change deserves a repaint
     */
    public String mouseWheelMoved(int events, int units) {
        if (!paintGaps || selectedComponents.isEmpty() || selectedGap == null
                || events <= 0 || units == 0) {
            return null;
        }

        lastWheelGap = selectedGap.gap;
        paintWheelGapSnap = false;
        LayoutInterval gap = selectedGap.gap;
        int currentSize = selectedGap.currentSize;
        int newSize = Integer.MIN_VALUE;
        units = units * -1; // wheel up should increase the gap size
        PaddingType newPaddingType = null;
        int[] defaultSizes = selectedGap.defaultGapSizes;
        if (defaultSizes != null && defaultSizes.length > 0) {
            int defPrefSize = gap.getPreferredSize();
            int defIndex = -1;
            boolean snapped;
            if (defPrefSize > 0 || LayoutInterval.canResize(gap)) {
                snapped = false;
                for (int i=0; i < defaultSizes.length; i++) {
                    if (i != PaddingType.INDENT.ordinal()) {
                        int defSize = defaultSizes[i];
                        if (currentSize >= defSize) {
                            defIndex = i;
                        }
                        if (currentSize <= defSize) {
                            snapped = (currentSize == defSize || currentSize == 0);
                            break;
                        }
                    }
                }
            } else {
                snapped = true;
                if (defPrefSize == NOT_EXPLICITLY_DEFINED) {
                    defIndex = (gap.getPaddingType() != null ? gap.getPaddingType().ordinal() : 0);
                }
                // otherwise defIndex stays -1 for 0 size
            }

            int count = events;
            int maxIndex = defaultSizes.length - 1;
            if (units > 0) { // growing
                if (defIndex < maxIndex) {
                    if (!snapped) {
                        count--;
                        defIndex++;
                        if (defIndex == PaddingType.INDENT.ordinal()) {
                            defIndex++;
                        }
                    }
                    while (count > 0 && defIndex < maxIndex) {
                        count--;
                        defIndex++;
                        if (defIndex == PaddingType.INDENT.ordinal()) {
                            defIndex++;
                        }
                    }
                    if (count > 0) {
                        currentSize = defaultSizes[defIndex];
                        units = units * count / events;
                        events = count;
                    } else {
                        newSize = NOT_EXPLICITLY_DEFINED;
                        if (defaultSizes.length > 1) {
                            newPaddingType = PaddingType.values()[defIndex];
                        }
                    }
                }
            } else { // shrinking
                if (defIndex == maxIndex && !snapped) {
                    units = accelerateWheel(events, units);
                    int diffToDefault = defaultSizes[maxIndex] - currentSize; // result is negative number
                    if (diffToDefault < units) { // above default size
                        newSize = currentSize + units;
                    } else if (diffToDefault == units) { // right on defualt size
                        newSize = NOT_EXPLICITLY_DEFINED;
                        if (defaultSizes.length > 1) {
                            newPaddingType = PaddingType.values()[maxIndex];
                        }
                    } else { // going below default size
                        count -= count * diffToDefault / units;
                        if (count == 0) { // but not significantly, so stay on default
                            newSize = NOT_EXPLICITLY_DEFINED;
                            if (defaultSizes.length > 1) {
                                newPaddingType = PaddingType.values()[maxIndex];
                            }
                        } else { // compute which default size below
                            units -= diffToDefault;
                            snapped = true;
                        }
                    }
                }
                if (newSize == Integer.MIN_VALUE) {
                    int minIndex = LayoutInterval.getDefaultSizeDef(gap) == 0 ? -1 : 0; // -1 index if we can go to 0 size
                    if (!snapped) {
                        count--;
                        defIndex--;
                        if (defIndex == PaddingType.INDENT.ordinal()) {
                            defIndex--;
                        }
                    }
                    while (count > 0 && defIndex > minIndex) {
                        count--;
                        defIndex--;
                        if (defIndex == PaddingType.INDENT.ordinal()) {
                            defIndex--;
                        }
                    }
                    if (defIndex < 0) {
                        newSize = 0;
                    } else {
                        newSize = NOT_EXPLICITLY_DEFINED;
                        if (defaultSizes.length > 1) {
                            newPaddingType = PaddingType.values()[defIndex];
                        }
                    }
                }
            }
        }
        if (newSize == Integer.MIN_VALUE) {
            units = accelerateWheel(events, units);
            newSize = currentSize + units;
            if (newSize < 0) {
                newSize = 0;
            }
        }

        String hint;
        layoutModel.setUserIntervalSize(selectedGap.gap, selectedGap.dimension, newSize);
        if (newSize == NOT_EXPLICITLY_DEFINED) {
            layoutModel.setPaddingType(selectedGap.gap, newPaddingType);
            // paint snapped size of default gap...
            paintWheelGapSnap = true;
            // ... but hide it after a while
            final String containerId = selectedComponents.get(0).getId();
            if (wheelPaintTimer != null) {
                wheelPaintTimer.stop();
            }
            wheelPaintTimer = new javax.swing.Timer(1500, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (lastWheelGap != null && paintWheelGapSnap && wheelPaintTimer == e.getSource()) {
                        paintWheelGapSnap = false;
                        LayoutComponent comp = layoutModel.getLayoutComponent(containerId);
                        if (comp != null) {
                            visualMapper.repaintDesigner(containerId);
                        }
                    } // oterwise some repaint must have already happened
                }
            });
            wheelPaintTimer.setRepeats(false);
            wheelPaintTimer.start();

            hint = VisualState.getDefaultGapDisplayName(newPaddingType);
        } else {
            hint = Integer.toString(newSize);
        }
        return hint;
    }

    private static int accelerateWheel(int events, int units) {
        if (events > 5) {
            return units * 2;
        } else if (events > 3) {
            return units * 3 / 2;
        }
        return units;
    }

    // -----

    /**
     * Call to process mouse click inside a selected component. As a result,
     * a gap can be selected if clicking on an empty space in a container.
     * This method assumes the clicked component has been already set via
     * setSelectedComponents method (before calling this method).
     * @param p The point of click in coordinates of the whole design area.
     * @return true if repaint is needed (selection changed)
     */
    public boolean selectInside(Point p) {
        if (!paintGaps || !visualStateUpToDate) {
            return false;
        }

        GapInfo selected = null;
        for (LayoutComponent component : selectedComponents) {
            if (!component.isLayoutContainer()) {
                continue;
            }
            // Preferentially try to select a gap of a component selected before the
            // click (if clicking into its parent). If such a component was selected,
            // LayoutPainter still has the painted gaps at this moment.
            Collection<GapInfo> gaps = layoutPainter.getPaintedGapsForContainer(component);
            if (gaps != null && gaps.isEmpty()) {
                gaps = null;
            }
            boolean searchedAll = false;
            selected = null;
            do {
                if (gaps == null) {
                    gaps = visualState.getContainerGaps(component);
                    searchedAll = true;
                }
                for (GapInfo gapInfo : gaps) {
                    if (pointInGap(p, gapInfo)
                            && component.getDefaultLayoutRoot(gapInfo.dimension).isParentOf(gapInfo.gap)) {
                        selected = gapInfo;
                        if (selected == selectedGap) {
                            break;
                        } // otherwise preferring last one
                    }
                }
                gaps = null;
            } while (!searchedAll && selected == null);

            if (selected != null) {
                break;
            }
        }

        if (selected != selectedGap) {
            if (selected != null) {
                selected.defaultGapSizes = LayoutUtils.getSizesOfDefaultGap(selected.gap, visualMapper);
            }
            selectedGap = selected;
            lastWheelGap = null;
            return true;
        }
        return false;
    }

    private boolean pointInSelectegGap(Point p) {
        if (paintGaps && selectedGap != null) {
            if (pointInGap(p, selectedGap)
                    || (selectedGap.gap == lastWheelGap && pointInGapStripe(p, selectedGap))) {
                return true;
            }
            int res = LayoutPainter.pointOnResizeHandler(selectedGap, p);
            return res == LEADING || res == TRAILING;
        }
        return false;
    }

    private static boolean pointInGap(Point p, GapInfo g) {
        if (g.paintRect != null) {
            return g.paintRect.contains(p);
        }
        if (g.dimension == HORIZONTAL) {
            return p.x >= g.position && p.x < g.position + g.currentSize
                   && p.y >= g.ortPositions[LEADING] && p.y < g.ortPositions[TRAILING];
        } else {
            return p.x >= g.ortPositions[LEADING] && p.x < g.ortPositions[TRAILING]
                   && p.y >= g.position && p.y < g.position + g.currentSize;
        }
    }

    private static boolean pointInGapStripe(Point p, GapInfo g) {
        if (g.dimension == HORIZONTAL) {
            return p.y >= g.ortPositions[LEADING] && p.y < g.ortPositions[TRAILING];
        } else {
            return p.x >= g.ortPositions[LEADING] && p.x < g.ortPositions[TRAILING];
        }
    }

    /**
     * Let the designer know about components selected by the user. The selected
     * components are then considered when painting alignment/anchors and gaps,
     * and when clicking on gaps.
     * All components should be from the same container (have the same parent).
     * @param compIds 
     */
    public void setSelectedComponents(String... compIds) {
        selectedComponents.clear();
        selectedGap = null;
        lastWheelGap = null;
        for (String id : compIds) {
            LayoutComponent comp = layoutModel.getLayoutComponent(id);
            if (comp != null) {
                selectedComponents.add(comp);
            } // TODO filter as getDraggableComponents does?
        }
    }

    /**
     * Paints layout information for selected components (anchor links,
     * alignment in groups, gaps).
     * @param g graphics object to use
     */
    public void paintSelection(Graphics2D g) {
        if (!visualStateUpToDate) {
            return;
        }
        if (paintGaps) {
            layoutPainter.paintGaps(g, selectedComponents, selectedGap);
            if (lastWheelGap != null && paintWheelGapSnap
                    && selectedGap != null && selectedGap.gap == lastWheelGap) {
                LayoutComponent gapContainer = null;
                for (LayoutComponent comp : selectedComponents) {
                    if (comp.isLayoutContainer()) {
                        gapContainer = comp;
                        break;
                    }
                }
                LayoutDragger.paintGapResizingSnap(g, selectedGap, gapContainer, visualState);
            }
        }
        layoutPainter.paintComponents(g, selectedComponents, paintAlignment);
    }

    /**
     * Checks whether a selected inner element that can be resized via mouse is
     * under mouse cursor at given position. (Practically this can only be a gap.)
     * @param p Position of the mouse cursor (in designer coordinates).
     * @return Two int array representing resizable edge in each dimension,
     *         or null if there is no gap to resize on given point.
     */
    public int[] getInnerResizability(Point p) {
        int resAlign = getSelectedGapResizability0(p);
        if (resAlign < 0) {
            return null;
        }
        return new int[] { selectedGap.dimension == HORIZONTAL ? resAlign : -1,
                           selectedGap.dimension == VERTICAL ? resAlign : -1};
    }

    private int getSelectedGapResizability0(Point p) {
        return paintGaps && selectedGap != null
                ? LayoutPainter.pointOnResizeHandler(selectedGap, p) : -1;
    }

    public String getToolTipText(Point p) {
        if (dragger != null) {
            return dragger.getToolTipText();
        }
        if (pointInSelectegGap(p)) {
            return selectedGap.description;
        }
        return null;
    }

    /**
     * Data structure representing a layout gap to be customized externally. The
     * public fields 'definedSize', 'paddingType' and 'resizing' are those to be
     * edited. Additional information about the gap is available via public methods.
     */
    public static final class EditableGap {
        public int definedSize; // editable
        public PaddingType paddingType; // editable
        public boolean resizing; // editable

        private int dimension;
        private int actualSize;
        private boolean canHaveDefaultValue;
        private PaddingType[] possiblePaddingTypes; // null if can't have default value, or if a container gap
        private String[] paddingDisplayNames; // same array length as possibleDefaultTypes, or 1 if a container gap

        private LayoutInterval gap;

        private EditableGap(LayoutInterval gap) {
            this.gap = gap;
        }

        public int getDimension() { return dimension; }
        public int getActualSize() { return actualSize; }
        public boolean canHaveDefaultValue() { return canHaveDefaultValue; }
        public PaddingType[] getPossiblePaddingTypes() { return possiblePaddingTypes; }
        public String[] getPaddingDisplayNames() { return paddingDisplayNames; }
    }

    /**
     * Provides information about layout gaps that can be edited at the moment.
     * It returns either a single-element array if just one gap is selected, or
     * a four-element array if exactly one component is selected (so gaps around
     * the component), or null if there's no suitable selection.
     * @return array of EditableGap, or null if nothing for editing is currently selected
     */
    public EditableGap[] getEditableGaps() {
        if (paintGaps && selectedGap != null) {
            LayoutInterval gap = selectedGap.gap;
            EditableGap eg = new EditableGap(gap);
            eg.definedSize = gap.getPreferredSize();
            if (eg.definedSize == NOT_EXPLICITLY_DEFINED) {
                eg.paddingType = gap.getPaddingType();
            }
            eg.resizing = LayoutInterval.canResize(gap);
            eg.dimension = selectedGap.dimension;
            eg.actualSize = selectedGap.currentSize;
            initEditableGapDefaults(eg, selectedGap.defaultGapSizes);
            return new EditableGap[] { eg };
        }

        if (selectedComponents.size() == 1) {
            LayoutComponent comp = selectedComponents.get(0);
            if (comp.getParent() != null) {
                List<EditableGap> gapList = new ArrayList<EditableGap>(4);
                boolean anyGap = false;
                for (int dim=0; dim < DIM_COUNT; dim++) {
                    LayoutInterval li = comp.getLayoutInterval(dim);
                    for (int e=LEADING; e <= TRAILING; e++) {
                        LayoutInterval gap = LayoutInterval.getNeighbor(li, e, false, true, false);
                        if (gap != null && gap.isEmptySpace()) {
                            anyGap = true;
                            EditableGap eg = new EditableGap(gap);
                            eg.definedSize = gap.getPreferredSize();
                            if (eg.definedSize == NOT_EXPLICITLY_DEFINED) {
                                eg.paddingType = gap.getPaddingType();
                            }
                            eg.resizing = LayoutInterval.canResize(gap);
                            eg.dimension = dim;
                            eg.actualSize = LayoutInterval.getCurrentSize(gap, dim);
                            initEditableGapDefaults(eg, LayoutUtils.getSizesOfDefaultGap(gap, visualMapper));
                            gapList.add(eg);
                        } else {
                            gapList.add(null);
                        }
                    }
                }
                if (anyGap) {
                    return gapList.toArray(new EditableGap[4]);
                }
            }
        }
        return null;
    }

    private static void initEditableGapDefaults(EditableGap eg, int[] defaultSizes) {
        if (defaultSizes != null) {
            if (defaultSizes.length == 1) {
                eg.canHaveDefaultValue = true;
                eg.paddingDisplayNames = new String[] { VisualState.getDefaultGapDisplayName(null) };
            } else if (defaultSizes.length > 1) {
                List<PaddingType> padList = new ArrayList<PaddingType>(defaultSizes.length);
                List<String> nameList = new ArrayList<String>(defaultSizes.length);
                for (int i=0; i < defaultSizes.length; i++) {
                    if (i != PaddingType.INDENT.ordinal()) { // TODO should also detect when INDENT can be used...
                        PaddingType pt = PaddingType.values()[i];
                        padList.add(pt);
                        nameList.add(VisualState.getDefaultGapDisplayName(pt));
                    }
                }
                eg.canHaveDefaultValue = true;
                eg.possiblePaddingTypes = padList.toArray(new PaddingType[0]);
                eg.paddingDisplayNames = nameList.toArray(new String[0]);
            }
        }
    }

    /**
     * Applies changes in given layout gaps, obtained earlier from getEditableGaps()
     * and then changed externally.
     * @param editableGaps 
     */
    public void applyEditedGaps(EditableGap[] editableGaps) {
        for (EditableGap eg : editableGaps) {
            if (eg != null) {
                layoutModel.setUserIntervalSize(eg.gap, eg.dimension, eg.definedSize, eg.resizing);
                layoutModel.setPaddingType(eg.gap, eg.paddingType);
            }
        }
    }

    public String[] positionCode() {
        return dragger != null ? dragger.positionCode() : new String[2];
    }

    public void setPaintAlignment(boolean paint) {
        if (!forcePaintAlignment) {
            paintAlignment = paint;
        }
    }

    public void setPaintGaps(boolean paint) {
        if (!forcePaintGaps) {
            paintGaps = paint;
        }
    }

    // -----
    // copying

    /**
     * Copy components with layout specified by sourceToTargetId map to
     * the given target container. Assuming all components come from one
     * container and one layer (layout roots pair), i.e. forming one piece of
     * layout.
     * If copying all components of one container to an empty container then
     * exact 1:1 copy is created, otherwise the layout copy is placed into a
     * separate layer not to interact with the existing layout.
     * If copying within the same container, the copied components are placed
     * slightly shifted from the original ones. Otherwise placed in the center.
     * This method requires Ids for the copied components provided
     * (LayoutComponent instances are created automatically).
     * @param sourceModel the source LayoutModel
     * @param sourceToTargetId components mapping between the original and the copied
     *        components' Ids; same Ids can be used - then the components are
     *        moved
     * @param targetContainerId the Id of the target container
     */
    public void copyLayout(LayoutModel sourceModel, Map<String, String> sourceToTargetId, String targetContainerId) {
        if (sourceToTargetId.isEmpty()) {
            return;
        }
        if (sourceModel == null) {
            sourceModel = layoutModel;
        }

        Map.Entry<String, String> firstEntry = sourceToTargetId.entrySet().iterator().next();
        LayoutComponent sourceContainer = sourceModel.getLayoutComponent(firstEntry.getKey()).getParent();
        LayoutComponent targetContainer = layoutModel.getLayoutComponent(targetContainerId);
        if (sourceContainer != targetContainer
                && sourceContainer.getSubComponentCount() == sourceToTargetId.size()
                && (targetContainer == null || targetContainer.getSubComponentCount() == 0)) {
            // copying/moving entire content of a container into an empty target container
            if (sourceModel != layoutModel || !firstEntry.getKey().equals(firstEntry.getValue())) {
                layoutModel.copyContainerLayout(sourceContainer, sourceToTargetId, targetContainer);
            } else { // same source and target component - don't copy, just move
                layoutModel.moveContainerLayout(sourceContainer, targetContainer);
                // source roots were cleared
                LayoutInterval[] sourceRoots = getActiveLayoutRoots(sourceContainer);
                for (int i=0; i < DIM_COUNT; i++) {
                    if (sourceRoots[i].getCurrentSpace().isSet(i)) {
                        propEmptyContainer(sourceRoots[i], i);
                    }
                }
            }
        } else { // copying part of the layout
            // collect the components, create new if needed, compute bounds, ...
            Map<LayoutComponent, LayoutComponent> sourceToTargetComp
                        = new HashMap<LayoutComponent, LayoutComponent>();
            LayoutComponent[] sourceComponents = new LayoutComponent[sourceToTargetId.size()];
            LayoutComponent[] targetComponents = new LayoutComponent[sourceToTargetId.size()];
            Rectangle[] bounds = new Rectangle[sourceToTargetId.size()];
            LayoutRegion overallSpace = new LayoutRegion();
            LayoutInterval[] commonParents = new LayoutInterval[DIM_COUNT];
            int i = 0;
            for (Map.Entry<String, String> entry : sourceToTargetId.entrySet()) {
                String sourceId = entry.getKey();
                LayoutComponent sourceLC = sourceModel.getLayoutComponent(sourceId);
                String targetId = entry.getValue();
                LayoutComponent targetLC = layoutModel.getLayoutComponent(targetId);
                if (targetLC == null) {
                    targetLC = new LayoutComponent(targetId, false);
                } else if (targetLC.getParent() == targetContainer) {
                    throw new IllegalArgumentException("The component is already placed in the target layout container"); // NOI18N
                }
                sourceToTargetComp.put(sourceLC, targetLC);
                targetComponents[i] = targetLC;

                LayoutRegion space = sourceLC.getLayoutInterval(0).getCurrentSpace();
                overallSpace.expand(space);
                bounds[i] = space.toRectangle(new Rectangle());
                sourceComponents[i] = sourceLC;
                i++;

                for (int dim=0; dim < DIM_COUNT; dim++) {
                    if (commonParents[dim] == null) {
                        commonParents[dim] = sourceLC.getLayoutInterval(dim);
                    } else {
                        commonParents[dim] = LayoutInterval.getCommonParent(
                                commonParents[dim], sourceLC.getLayoutInterval(dim));
                    }
                }
            }

            // copy the intervals
            LayoutInterval[] addingInts = new LayoutInterval[DIM_COUNT];
            for (int dim=0; dim < DIM_COUNT; dim++) {
                addingInts[dim] = restrictedCopy(commonParents[dim], sourceToTargetComp, overallSpace, dim, null);
            }

            // place the copied intervals
            int[] shift = getCopyShift(sourceComponents, targetContainer, overallSpace, sourceContainer == targetContainer);
            if (shift != null) { // place near the original positions
                // in case components are moved (not copied) make sure both the
                // components and intervals are correctly removed from the original place
                if (targetComponents.length > 1) {
                    // for multiple components the intervals are extracted in restrictedCopy
                    // (their common parent is removed), so now also remove the components
                    // so addComponents does not try to remove the intervals later
                    for (LayoutComponent comp : targetComponents) {
                        if (comp.getParent() != null) {
                            layoutModel.removeComponent(comp, false);
                        }
                    }
                } // for single component both the component and intervals will be removed in addComponents
                prepareDragger(targetComponents, bounds, new Point(0, 0), LayoutDragger.ALL_EDGES);
                dragger.setTargetContainer(targetContainer, getTargetRootsForCopy(targetContainer));
                dragger.move(shift, false, false);
                addComponents(targetComponents, targetContainer, addingInts, false, null);
                dragger = null;
            } else { // place in center
                removeComponents(targetComponents, false); // for the case they are moved (not copied)
                addUnspecified(targetComponents, targetContainer, addingInts);
            }
        }
        visualStateUpToDate = false;
        updateDataAfterBuild = true;
    }

    /**
     * Copy components without layout to the given target container. The layout
     * is determined from the visual bounds of the components. Can be used for
     * copying external components (from different layout) to a container, or
     * for converting container's layout to this layout model. New
     * LayoutComponent instances are created as needed, added all to the
     * specified target container (centered, in separate layer of layout roots).
     * @param idToBounds mapping component Ids to the visual bounds
     * @param targetContainerId the Id of the target container
     * @param relative if true then the position of the entire formation
     *        is set to 0, 0 before the conversion starts
     */
    public void copyLayoutFromOutside(Map<String, Rectangle> idToBounds, String targetContainerId, boolean relative) {
        LayoutComponent targetContainer = layoutModel.getLayoutComponent(targetContainerId);
        if (targetContainer.getSubComponentCount() > 0) {
            relative = true;
        }
        Map<LayoutComponent, Rectangle> compToBounds = new HashMap<LayoutComponent, Rectangle>();
        LayoutComponent[] components = new LayoutComponent[idToBounds.size()];
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int i = 0;
        for (Map.Entry<String, Rectangle> entry : idToBounds.entrySet()) {
            String targetId = entry.getKey();
            LayoutComponent targetLC = layoutModel.getLayoutComponent(targetId);
            if (targetLC == null) {
                targetLC = new LayoutComponent(targetId, false);
            } else if (targetLC.getParent() != null) {
                throw new IllegalArgumentException("Target component already exists and is placed in the layout"); // NOI18N
            }
            Rectangle r = new Rectangle(entry.getValue());
            compToBounds.put(targetLC, r);
            components[i] = targetLC;
            LayoutRegion compSpace = new LayoutRegion(r, LayoutRegion.UNKNOWN);
            Dimension preferred = visualMapper.getComponentPreferredSize(targetId);
            for (int dim=0; dim < DIM_COUNT; dim++) {
                LayoutInterval li = targetLC.getLayoutInterval(dim);
                operations.enableFlexibleSizeDefinition(li, true);
                int size = compSpace.size(dim);
                if (preferred == null || size != ((dim == HORIZONTAL) ? preferred.width : preferred.height)) {
                    li.setPreferredSize(compSpace.size(dim));
                }
            }
            if (relative) {
                minX = Math.min(minX, compSpace.positions[HORIZONTAL][LEADING]);
                minY = Math.min(minY, compSpace.positions[VERTICAL][LEADING]);
            }
            i++;
        }
        if (relative) {
            // need the overall bounds enclosure based on 0, 0
            for (Rectangle r : compToBounds.values()) {
                r.x -= minX;
                r.y -= minY;
            }
        }
        LayoutInterval[] addingInts = layoutModel.createIntervalsFromBounds(compToBounds);
        if (relative) {
            addUnspecified(components, targetContainer, addingInts);
        } else {
            addToEmpty(components, targetContainer, addingInts);
        }
        visualStateUpToDate = false;
        updateDataAfterBuild = true;
    }

    /**
     * Duplicates layout of given components. Duplicated components are added
     * sequentially along given axis (dimension), in parallel in the orthogonal
     * dimension.
     * @param sourceIds Ids of the source components
     * @param targetIds Ids for the duplicates (non-existing layout components
     *        are created automatically with the given Ids)
     * @param dimension the dimension in which the layout should be duplicated
     *        (extended sequentially); HORIZONTAL, VERTICAL, or < 0
     * @param direction the direction of addition - LEADING or TRAILING
     */
    public void duplicateLayout(String[] sourceIds, String[] targetIds, int dimension, int direction) {
        if (logTestCode()) {
            testCode.add("// > DUPLICATE"); // NOI18N
            testCode.add("{"); // NOI18N
            LayoutTestUtils.writeStringArray(testCode, "sourceIds", sourceIds); // NOI18N
            LayoutTestUtils.writeStringArray(testCode, "targetIds", targetIds); // NOI18N
            testCode.add("int dimension = " + dimension + ";"); // NOI18N
            testCode.add("int direction = " + direction + ";"); // NOI18N
            testCode.add("ld.duplicateLayout(sourceIds, targetIds, dimension, direction);"); // NOI18N
            testCode.add("}"); // NOI18N
            testCode.add("// < DUPLICATE"); // NOI18N
        }
        LayoutComponent[] sourceComps = new LayoutComponent[sourceIds.length];
        LayoutInterval[][] sourceIntervals = new LayoutInterval[DIM_COUNT][sourceIds.length];
        LayoutComponent[] targetComps = new LayoutComponent[targetIds.length];
        Map<LayoutComponent, LayoutComponent> compMap = new HashMap<LayoutComponent, LayoutComponent>();
        LayoutComponent container = null;
        for (int i=0; i < sourceComps.length; i++) {
            LayoutComponent sourceLC = layoutModel.getLayoutComponent(sourceIds[i]);
            LayoutComponent parent = sourceLC.getParent();
            if (i == 0) {
                container = parent;
            } else if (parent != container) {
                throw new IllegalArgumentException("Duplicated components must be in the same container."); // NOI18N
            }
            sourceComps[i] = sourceLC;

            LayoutComponent targetLC = layoutModel.getLayoutComponent(targetIds[i]);
            if (targetLC == null) {
                targetLC = new LayoutComponent(targetIds[i], false);
            } else if (targetLC.getParent() != null) {
                throw new IllegalArgumentException("Target component already exists and is placed in the layout"); // NOI18N
            }
            layoutModel.addComponent(targetLC, container, -1);
            compMap.put(sourceLC, targetLC);
            targetComps[i] = targetLC;
            for (int dim=0; dim < DIM_COUNT; dim++) {
                LayoutInterval li = sourceLC.getLayoutInterval(dim);
                sourceIntervals[dim][i] = li;
            }
        }

        int seqDim = dimension < 0 ? getSeqDuplicatingDimension(sourceComps) : dimension;
        int seqDir = direction < 0 ? getSeqDuplicatingDirection(sourceComps, seqDim) : direction;
        int parDim = seqDim ^ 1;

        duplicateSequentially(sourceIntervals[seqDim], compMap, seqDim, seqDir);
        duplicateInParallel(sourceIntervals[parDim], compMap, parDim);

        requireStructureOptimization(container);
        visualStateUpToDate = false;
        updateDataAfterBuild = true;
    }

    private static int getSeqDuplicatingDimension(LayoutComponent[] components) {
        return VERTICAL;
    }

    private static int getSeqDuplicatingDirection(LayoutComponent[] components, int dimension) {
        return TRAILING;
    }

    private void duplicateSequentially(LayoutInterval[] intervals,
            Map<LayoutComponent, LayoutComponent> componentMap,
            int dimension, int direction) {
        // determine roots to duplicate, eliminate subcontained
        Set<LayoutInterval> dupRoots = new HashSet<LayoutInterval>();
        for (LayoutInterval li : intervals) {
            // check if not under already determined root
            LayoutInterval parent = li.getParent();
            while (parent != null) {
                if (dupRoots.contains(parent)) {
                    break;
                } else {
                    parent = parent.getParent();
                }
            }
            if (parent == null) { // not determined yet
                parent = li.getParent();
                while (parent != null) {
                    if (shouldDuplicateWholeGroup(parent, li, intervals)) {
                        li = parent;
                        parent = li.getParent();
                    } else {
                        if (li.isGroup()) { // we might find a parent of some previous root
                            for (Iterator<LayoutInterval> it = dupRoots.iterator(); it.hasNext(); ) {
                                LayoutInterval dRoot = it.next();
                                if (li.isParentOf(dRoot)) {
                                    it.remove();
                                }
                            }
                        }
                        dupRoots.add(li);
                        break;
                    }
                }
                if (parent == null) { // whole layout root to be duplicated
                    dupRoots.clear();
                    dupRoots.add(li);
                    break;
                }
            }
        }

        // duplicate the roots
        while (!dupRoots.isEmpty()) {
            LayoutInterval dRoot = dupRoots.iterator().next();
            // prepare parent sequence
            LayoutInterval seq;
            LayoutInterval parent = dRoot.getParent();
            if (parent != null) {
                if (dRoot.isSequential()) {
                    seq = dRoot;
                } else if (parent.isSequential()) {
                    seq = parent;
                } else {
                    seq = new LayoutInterval(SEQUENTIAL);
                    layoutModel.addInterval(seq, parent, layoutModel.removeInterval(dRoot));
                    layoutModel.addInterval(dRoot, seq, -1);
                }
            } else { // duplicating entire layout root
                LayoutInterval group = new LayoutInterval(PARALLEL);
                group.setGroupAlignment(dRoot.getGroupAlignment());
                while (dRoot.getSubIntervalCount() > 0) {
                    layoutModel.addInterval(layoutModel.removeInterval(dRoot, 0), group, -1);
                }
                seq = new LayoutInterval(SEQUENTIAL);
                layoutModel.addInterval(seq, dRoot, -1);
                layoutModel.addInterval(group, seq, -1);
            }
            // determine parts of the sequence to duplicate
            int start = -1;
            boolean wholeSeq = dupRoots.remove(seq);
            LayoutRegion space = seq.getParent().getCurrentSpace();
            for (int i=0; i < seq.getSubIntervalCount(); i++) {
                LayoutInterval sub = seq.getSubInterval(i);
                boolean duplicate = !sub.isEmptySpace() && (wholeSeq || dupRoots.remove(sub));
                boolean last = (i+1 == seq.getSubIntervalCount());
                if (duplicate && start < 0) {
                    start = i;
                }
                if (start >= 0 && ((!duplicate && !sub.isEmptySpace()) || last)) {
                    int count = i - start;
                    if (last && duplicate) {
                        count++;
                    } else if (seq.getSubInterval(i-1).isEmptySpace()) {
                        count--;
                    }
                    if (count > 0) { // copy the continuous section within the sequence
                        for (int j=start; j < start+count; j++) {
                            LayoutInterval li = seq.getSubInterval(j);
                            LayoutInterval copy = restrictedCopy(li, componentMap, space, dimension, null);
                            if (direction == LEADING) {
                                layoutModel.addInterval(copy, seq, start);
                                start++;  i++;  j++;
                            } else { // TRAILING
                                layoutModel.addInterval(copy, seq, j+count);
                                i++;
                            }
                        }
                        // need a gap between the original and duplicated section
                        int gapIndex;
                        LayoutInterval gap = null;
                        if (direction == LEADING) {
                            gapIndex = start + count; // here should be the original gap to use
                            if (gapIndex < seq.getSubIntervalCount()) {
                                gap = seq.getSubInterval(gapIndex);
                            }
                            gapIndex = start; // here it should be placed
                        } else { // TRAILING
                            gapIndex = start - 1; // here should be the original gap to use
                            if (gapIndex >= 0) {
                                gap = seq.getSubInterval(gapIndex);
                            }
                            gapIndex = start + count; // here it should be placed
                        }
                        LayoutInterval newGap = new LayoutInterval(SINGLE);
                        if (gap != null && gap.isEmptySpace()) {
                            LayoutInterval.cloneInterval(gap, newGap);
                        }
                        layoutModel.addInterval(newGap, seq, gapIndex);
                        i++;
                    }
                    start = -1;
                }
            }
            dupRoots.remove(dRoot); // should not be needed, but just for sure
        }
    }

    private static boolean shouldDuplicateWholeGroup(LayoutInterval group, LayoutInterval knownSub, LayoutInterval[] dupIntervals) {
        assert group.isGroup();
        if (group.isParallel() && knownSub != null
                && knownSub.getAlignment() != LEADING && knownSub.getAlignment() != TRAILING) {
            return true; // need to duplicate next to whole center/baseline group
        }
        Iterator it = group.getSubIntervals();
        while (it.hasNext()) {
            LayoutInterval sub = (LayoutInterval) it.next();
            if (sub == knownSub || sub.isEmptySpace()) {
                continue;
            }
            boolean included;
            if (sub.isGroup()) {
                included = shouldDuplicateWholeGroup(sub, null, dupIntervals);
            } else {
                assert sub.isComponent();
                included = false;
                for (LayoutInterval li : dupIntervals) {
                    if (li == sub) {
                        included = true;
                        break;
                    }
                }
            }
            if (included && group.isParallel()) {
                return true; // one is enough in parallel group
            }
            if (!included && group.isSequential()) {
                return false; // all required in a sequence
            }
        }
        return group.isSequential();
    }

    private void duplicateInParallel(LayoutInterval[] intervals, Map<LayoutComponent, LayoutComponent> componentMap, int dimension) {
        Map<LayoutInterval, LayoutInterval> intMap = new HashMap<LayoutInterval, LayoutInterval>();
        for (LayoutInterval li : intervals) {
            intMap.put(li, componentMap.get(li.getComponent()).getLayoutInterval(dimension));
        }
        for (LayoutInterval li : intervals) {
            LayoutInterval copy = intMap.get(li);
            if (copy == null) {
                continue;
            }
            LayoutInterval parent = li.getParent();
            if (parent.isParallel()) {
                LayoutInterval.cloneInterval(li, copy);
                layoutModel.setIntervalAlignment(copy, li.getRawAlignment());
                layoutModel.addInterval(copy, parent, -1);
            } else { // in sequence
                int index = parent.indexOf(li);
                int start = getDuplicationBoundary(parent, index, intMap.keySet(), LEADING);
                int end = getDuplicationBoundary(parent, index, intMap.keySet(), TRAILING);
                int gapStart = LayoutUtils.getVisualPosition(parent.getSubInterval(start), dimension, LEADING);
                LayoutInterval normalGap = null;
                boolean substGap = false;
                LayoutInterval parSeq = new LayoutInterval(SEQUENTIAL);
                for (int i=start; i <= end; i++) {
                    LayoutInterval sub = parent.getSubInterval(i);
                    copy = intMap.remove(sub);
                    if (copy != null) {
                        LayoutInterval.cloneInterval(sub, copy);
                        if (normalGap != null) {
                            LayoutInterval copyGap = new LayoutInterval(SINGLE);
                            LayoutInterval.cloneInterval(normalGap, copyGap);
                            layoutModel.addInterval(copyGap, parSeq, -1);
                            normalGap = null;
                        } else if (substGap) {
                            LayoutInterval gap = new LayoutInterval(SINGLE);
                            int gapEnd = sub.getCurrentSpace().positions[dimension][LEADING];
                            gap.setSize(gapEnd - gapStart);
                            layoutModel.addInterval(gap, parSeq, -1);
                            substGap = false;
                        }
                        layoutModel.addInterval(copy, parSeq, -1);
                        gapStart = sub.getCurrentSpace().positions[dimension][TRAILING];
                    } else if (!sub.isEmptySpace()) { // skipped component
                        normalGap = null;
                        substGap = true;
                    } else if (!substGap) { // normal gap
                        normalGap = sub;
                    }
                }
                if (normalGap != null) {
                    LayoutInterval copyGap = new LayoutInterval(SINGLE);
                    LayoutInterval.cloneInterval(normalGap, copyGap);
                    layoutModel.addInterval(copyGap, parSeq, -1);
                } else if (substGap) {
                    LayoutInterval gap = new LayoutInterval(SINGLE);
                    int gapEnd = LayoutUtils.getVisualPosition(parent.getSubInterval(end), dimension, TRAILING);
                    gap.setSize(gapEnd - gapStart);
                    layoutModel.addInterval(gap, parSeq, -1);
                }
                operations.addParallelWithSequence(parSeq, parent, start, end, dimension);
            }
        }
    }

    private static int getDuplicationBoundary(LayoutInterval seq, int index, Set<LayoutInterval> dupIntervals, int direction) {
        assert seq.isSequential();
        int d = (direction == LEADING ? -1 : 1);
        index += d;
        while (index >= 0 && index < seq.getSubIntervalCount()) {
            LayoutInterval sub = seq.getSubInterval(index);
            if (sub.isParallel()) {
                break; // [This forces enclosing the component in a closed group
                // which might be unnecessary (at least in horizontal dimension, in
                // vertical it is most probably ok). Maybe we should parallelize with
                // parallel members if they don't contain another duplicated component.]
            }
            index += d;
        }
        return index - d;
    }

    public boolean canEncloseInContainer(String[] compIds) {
        LayoutComponent commonContainer = null;
        LayoutInterval commonRoot = null;
        for (String id : compIds) {
            LayoutComponent comp = layoutModel.getLayoutComponent(id);
            if (comp == null) {
                return false;
            }
            LayoutComponent cont = comp.getParent();
            if (cont == null) {
                return false;
            }
            if (commonContainer == null) {
                commonContainer = cont;
            } else if (commonContainer != cont) {
                return false;
            }
            LayoutInterval root = LayoutInterval.getRoot(comp.getLayoutInterval(HORIZONTAL));
            if (root == null) {
                return false;
            }
            if (commonRoot == null) {
                commonRoot = root;
            } else if (commonRoot != root) {
                return false;
            }
        }
        return true;
    }

    public void encloseInContainer(String[] compIds, String contId) {
        LayoutComponent enclosingCont = layoutModel.getLayoutComponent(contId);
        if (enclosingCont == null) {
            enclosingCont = new LayoutComponent(contId, true);
        } else if (enclosingCont.getParent() != null) {
            throw new IllegalArgumentException("Target container already exists and is placed in the layout."); // NOI18N
        } else if (enclosingCont.getSubComponentCount() > 0) {
            throw  new IllegalArgumentException("Target container is not empty."); // NOI18N
        }
        LayoutComponent parentCont = null;
        LayoutComponent[] components = new LayoutComponent[compIds.length];
        LayoutInterval[] commonParents = new LayoutInterval[DIM_COUNT];
        boolean[] resizing = new boolean[DIM_COUNT];
        Map<LayoutComponent, LayoutComponent> compMap = new HashMap<LayoutComponent, LayoutComponent>();
        LayoutRegion overallSpace = new LayoutRegion();
        int i = 0;
        for (String id : compIds) {
            LayoutComponent comp = layoutModel.getLayoutComponent(id);
            components[i++] = comp;
            compMap.put(comp, comp);
            if (parentCont == null) {
                parentCont = comp.getParent();
            }
            for (int dim=0; dim < DIM_COUNT; dim++) {
                LayoutInterval compInt = comp.getLayoutInterval(dim);
                overallSpace.expand(compInt.getCurrentSpace(), dim);

                commonParents[dim] = (commonParents[dim] == null)
                        ? compInt : LayoutInterval.getCommonParent(commonParents[dim], compInt);
            }
        }
        // determine the layout roots pair where the enclosing happens, and resizability
        LayoutInterval[] parentRoots = new LayoutInterval[DIM_COUNT];
        List<LayoutInterval> wl = new LinkedList<LayoutInterval>(); // working list for traversing intervals
        for (int dim=0; dim < DIM_COUNT; dim++) {
            LayoutInterval compParent = commonParents[dim];
            parentRoots[dim] = LayoutInterval.getRoot(compParent);
            // now check if the selection contains some resizing intervals, excluding border gaps
            if (LayoutInterval.canResize(compParent)) {
                wl.add(compParent);
                while (!wl.isEmpty()) {
                    LayoutInterval li = wl.remove(0);
                    if (li.isSingle()) {
                        if (LayoutInterval.wantResize(li)) {
                            resizing[dim] = true;
                            wl.clear(); // done
                        }
                    } else {
                        for (int ii=0; ii < li.getSubIntervalCount(); ii++) {
                            LayoutInterval sub = li.getSubInterval(ii);
                            if (LayoutInterval.canResize(sub)
                                && (sub.isGroup() || sub.isComponent()
                                    || (li.isSequential() && ii > 0 && ii+1 < li.getSubIntervalCount()))) {
                                wl.add(sub);
                            }
                        }
                    }
                }
            }
        }
        // initialize the dragger with the roots to make sure ther roots are not
        // removed when the enclosing components are removed (in case they were
        // alone in alternate roots)
        prepareDragger(new LayoutComponent[] { enclosingCont },
                new Rectangle[] { overallSpace.toRectangle(new Rectangle()) },
                new Point(0, 0),
                LayoutDragger.ALL_EDGES);
        dragger.setTargetContainer(parentCont, parentRoots);
        // move the components to the enclosing container
        if (enclosingCont.isLayoutContainer()) {
            LayoutInterval[] extractedInts = new LayoutInterval[DIM_COUNT];
            for (int dim=0; dim < DIM_COUNT; dim++) {
                LayoutInterval extract = commonParents[dim];
                if (extract.isComponent()) { // just one component being enclosed
                    removeComponentInterval(extract, dim);
                    extractedInts[dim] = extract;
                } else {
                    extractedInts[dim] = restrictedCopy(extract, compMap, overallSpace, dim, null);
                }
                visualState.updateSpaceAfterRemove(parentCont.getDefaultLayoutRoot(dim), dim);
            }
            for (LayoutComponent comp : components) {
                layoutModel.removeComponent(comp, false);
                layoutModel.addComponent(comp, enclosingCont, -1);
            }
            for (int dim=0; dim < DIM_COUNT; dim++) {
                // make sure the root is empty (clear possible "offset" gap) 
                LayoutInterval root = enclosingCont.getDefaultLayoutRoot(dim);
                for (int n=root.getSubIntervalCount(); n > 0; n--) {
                    root.remove(n-1);
                }
                assert root.isParallel();
                LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
                seq.add(new LayoutInterval(SINGLE), -1);
                layoutModel.addInterval(extractedInts[dim], seq, -1);
                seq.add(new LayoutInterval(SINGLE), -1);
                layoutModel.addInterval(seq, root, -1);
            }
        } else {
            // in this case the enclosing container is not a container in this
            // layout model, so just remove the components (e.g. enclosing into a tabbed pane)
            removeComponentsFromParent(components);
        }
        // now position the enclosing container on the original location of components
        LayoutInterval[] addingInts = new LayoutInterval[DIM_COUNT];
        for (int dim=0; dim < DIM_COUNT; dim++) {
            LayoutInterval interval = enclosingCont.getLayoutInterval(dim);
            addingInts[dim] = interval;
            if (resizing[dim]) {
                interval.setSizes(DEFAULT, DEFAULT, Short.MAX_VALUE);
            } else {
                interval.setSizes(USE_PREFERRED_SIZE, DEFAULT, USE_PREFERRED_SIZE);
            }
        }
        dragger.move(new int[] { 10, 10 }, true, false);
        dragger.move(new int[] { 0, 0 }, true, false);
        addComponents(new LayoutComponent[] { enclosingCont }, parentCont, addingInts, false, null);
        dragger = null;
        visualStateUpToDate = false;
        updateDataAfterBuild = true;
    }

    /**
     * Add a single new component (targetId) to given target container. No layout
     * information is provided, so the component is placed on a default location
     * in the layout. This means centered within the container, in a separate
     * layer of layout roots (not to interact with the rest of the layout).
     * The size of the component can be determined either by a "source"
     * component (typically if adding a copy of another component), or by
     * provided Dimension object (compSize).
     * @param targetId Id of the added component; may already be in the model,
     *        but not in a container; created automatically if needed
     * @param sourceId Id of the source component - optional - if provided, the
     *        size definition of this component is copied to the added component
     * @param compSize the size of the component - used if the source component
     *        is not provided to determine the size from; can be null
     * @param targetContainerId the Id of the target container (must exist in the model)
     */
    public void addUnspecifiedComponent(String targetId,
                                        String sourceId, Dimension compSize,
                                        String targetContainerId) {
        LayoutComponent targetContainer = layoutModel.getLayoutComponent(targetContainerId);
        LayoutComponent targetLC = layoutModel.getLayoutComponent(targetId);
        if (targetLC == null) {
            targetLC = new LayoutComponent(targetId, false);
        } else if (targetLC.getParent() != null) {
            if (targetLC.getParent() != targetContainer && targetId.equals(sourceId)) { // move from other container
                removeComponents(new LayoutComponent[] { targetLC }, false);
            } else {
                throw new IllegalArgumentException("Target component already exists and is placed in the layout"); // NOI18N
            }
        }
        LayoutComponent sourceLC = layoutModel.getLayoutComponent(sourceId);
        LayoutInterval[] addingInts = new LayoutInterval[DIM_COUNT];
        if (sourceLC != null) {
            for (int dim=0; dim < DIM_COUNT; dim++) {
                LayoutInterval li = sourceLC.getLayoutInterval(dim);
                addingInts[dim] = LayoutInterval.cloneInterval(li, targetLC.getLayoutInterval(dim));
            }
        } else {
            Dimension preferred = compSize != null
                    ? visualMapper.getComponentPreferredSize(targetId) : null;
            for (int dim=0; dim < DIM_COUNT; dim++) {
                LayoutInterval li = targetLC.getLayoutInterval(dim);
                addingInts[dim] = li;
                if (preferred != null) {
                    int size = (dim == HORIZONTAL) ? compSize.width : compSize.height;
                    int pref = (dim == HORIZONTAL) ? preferred.width : preferred.height;
                    if (size != pref) {
                        li.setPreferredSize(size);
                    }
                }
            }
        }
        addUnspecified(new LayoutComponent[] { targetLC }, targetContainer, addingInts);
        visualStateUpToDate = false;
        updateDataAfterBuild = true;
    }

    /**
     * Add a new component to the target layout. No layout information is
     * provided, so the component is added to a default location. (This means
     * centered within the container, in a separate layer of layout roots - not
     * to interact with the rest of the layout.)
     * @param component the component to be added; may already exist in the model,
     *        but not placed in a container
     * @param targetContainerId Id of the target container; can be null
     */
    public void addUnspecifiedComponent(LayoutComponent component, String targetContainerId) {
        if (component.getParent() != null) {
            throw new IllegalArgumentException("The component already exists and is placed in the layout"); // NOI18N
        }
        LayoutComponent targetContainer = layoutModel.getLayoutComponent(targetContainerId);
        if (targetContainer == null) {
            layoutModel.addRootComponent(component);
            updateDataAfterBuild = true;
        } else {
            LayoutInterval[] addingInts = new LayoutInterval[DIM_COUNT];
            for (int dim=0; dim < DIM_COUNT; dim++) {
                addingInts[dim] = component.getLayoutInterval(dim);
            }
            addUnspecified(new LayoutComponent[] { component }, targetContainer, addingInts);
            visualStateUpToDate = false;
        }
    }

    private LayoutInterval[] getTargetRootsForCopy(LayoutComponent targetContainer) {
        LayoutInterval[] roots = layoutModel.addNewLayoutRoots(targetContainer);
        LayoutRegion space = getContainerSpace(targetContainer);
        for (int i=0; i < DIM_COUNT; i++) {
            roots[i].setCurrentSpace(space);
        }
        return roots;
    }

    private static int[] getCopyShift(LayoutComponent[] sourceComponents, LayoutComponent targetContainer, LayoutRegion compSpace, boolean relative) {
        LayoutRegion contSpace = getContainerSpace(targetContainer);
        if (!compSpace.isSet() || !contSpace.isSet()) {
            return null;
        }
        int[] move = new int[DIM_COUNT];
        if (relative) {
            for (int dim=0; dim < DIM_COUNT; dim++) {
                move[dim] = suggestCopyShift(sourceComponents, dim);
            }
            if (move[HORIZONTAL] == 0 && move[VERTICAL] == 0) {
                move[HORIZONTAL] = move[VERTICAL] = 10;
            }
        } else {
            for (int dim=0; dim < DIM_COUNT; dim++) {
                move[dim] = (contSpace.size(dim) - compSpace.size(dim))/2
                            - compSpace.positions[dim][LEADING]
                            + contSpace.positions[dim][LEADING];
            }            

        }
        return move;
    }

    private static int suggestCopyShift(LayoutComponent[] sourceComponents, int dimension) {
        if (!isAnyComponentSnappedToRoot(sourceComponents, dimension, TRAILING)) {
            return 10;
        } else if (!isAnyComponentSnappedToRoot(sourceComponents, dimension, LEADING)) {
            return -10;
        } else {
            return 0; // snapped to root border on both sides
        }
    }

    private static boolean isAnyComponentSnappedToRoot(LayoutComponent[] components, int dimension, int alignment) {
        LayoutInterval root = null;
        for (LayoutComponent comp : components) {
            LayoutInterval compInt = comp.getLayoutInterval(dimension);
            if (root == null) {
                root = LayoutInterval.getRoot(compInt);
            }
            if (LayoutInterval.isPlacedAtBorder(compInt, root, dimension, alignment)
                    || isSnappedNextToInParent(compInt, root, dimension, alignment)) {
                return true;
            }
        }
        return false;
    }

    // [move this to LayoutInterval?]
    private static boolean isSnappedNextToInParent(LayoutInterval interval, LayoutInterval parent, int dimension, int alignment) {
        LayoutInterval gap = LayoutInterval.getNeighbor(interval, alignment, false, true, false);
        if (gap != null && LayoutInterval.isFixedDefaultPadding(gap) && parent.isParentOf(gap)) {
            LayoutInterval back = LayoutInterval.getDirectNeighbor(gap, alignment^1, true);
            if ((back == interval || LayoutInterval.isPlacedAtBorder(interval, back, dimension, alignment))
                    && LayoutInterval.getNeighbor(gap, alignment, true, true, false) == null
                    && LayoutInterval.isPlacedAtBorder(gap.getParent(), parent, dimension, alignment)) {
                return true;
            }
        }
        return false;
    }

    private static LayoutRegion getContainerSpace(LayoutComponent container) {
        return container.getDefaultLayoutRoot(HORIZONTAL).getCurrentSpace();
    }

    private LayoutInterval[] getActiveLayoutRoots(LayoutComponent container) {
        return container.getLayoutRoots().get(0);
        // [in future we may keep the default "layer" for each container stored somewhere]
    }

    // -----

    private boolean isComponentResizable(LayoutComponent comp, int dimension) {
        boolean[] res = comp.getResizability();
        if (res == null) {
            res = visualMapper.getComponentResizability(comp.getId(), new boolean[DIM_COUNT]);
            comp.setResizability(res);
        }
        return res[dimension];
    }

    /**
     * Changes global alignment (anchor) of the layout component.
     *
     * @param comp component whose alignment should be changed.
     * @param dimension dimension the alignment should be applied in.
     * @param alignment desired alignment.
     */
    public void adjustComponentAlignment(LayoutComponent comp, int dimension, int alignment) {
        if (logTestCode()) {
            testCode.add("// > ADJUST COMPONENT ALIGNMENT"); //NOI18N
            testCode.add("{"); //NOI18N
            testCode.add("LayoutComponent comp = lm.getLayoutComponent(\"" + comp.getId() + "\");"); //NOI18N
            testCode.add("int dimension = " + dimension + ";");	    //NOI18N
            testCode.add("int alignment = " + alignment + ";");          //NOI18N 
            testCode.add("ld.adjustComponentAlignment(comp, dimension, alignment);"); //NOI18N
            testCode.add("}"); //NOI18N
        }
        LayoutInterval interval = comp.getLayoutInterval(dimension);
        
        // Skip non-resizable groups
        LayoutInterval parent = interval.getParent();
        while (parent != null) {
            if (!LayoutInterval.canResize(parent)) {
                interval = parent;
            }
            parent = parent.getParent();
        }
        assert !LayoutInterval.wantResize(interval);
        
        parent = interval.getParent();
        while (parent != null) {
            if (parent.isParallel()) {
                if (LayoutInterval.wantResize(parent) && !LayoutInterval.wantResize(interval)) {
                    int alg = interval.getAlignment();
                    if (alg != alignment) {
                        // add fixed supporting gap in the anchor direction, and change alignment
                        int pos = parent.getCurrentSpace().positions[dimension][alignment];
                        int size = (pos - interval.getCurrentSpace().positions[dimension][alignment]) * (alignment == LEADING ? -1 : 1);
                        if (size > 0) {
                            LayoutInterval gap = new LayoutInterval(SINGLE);
                            gap.setSize(size);
                            operations.insertGap(gap, interval, pos, dimension, alignment);
                        }
                        layoutModel.setIntervalAlignment(interval, alignment);
                    }
                    break; // assuming the anchor was clear before, so we need only one correction
                }
            } else { // in sequence
                // first eliminate resizing gaps in the desired anchor direction
                boolean before = true;
                boolean seqWasResizing = false;
                boolean otherSidePushing = false;
                for (int i=0; i<parent.getSubIntervalCount(); i++) {
                    LayoutInterval li = parent.getSubInterval(i);
                    if (li == interval) {
                        before = false;
                    } else if (LayoutInterval.wantResize(li)) {
                        if ((before && (alignment == LEADING)) || (!before && (alignment == TRAILING))) {
                            assert li.isEmptySpace();
                            int expCurrentSize = NOT_EXPLICITLY_DEFINED;
                            if (li.getDiffToDefaultSize() != 0 && li.getPreferredSize() <= 0) {
                                expCurrentSize = LayoutInterval.getCurrentSize(li, dimension);
                            }
                            operations.setIntervalResizing(li, false);
                            if (expCurrentSize > 0) {
                                operations.resizeInterval(li, expCurrentSize);
                            } else if (operations.eliminateUnwantedZeroGap(li)) {
                                i--;
                            }
                            seqWasResizing = true;
                        } else {
                            otherSidePushing = true;
                        }
                    }
                }
                // second, if needed make a resizing gap in the other direction
                if (!otherSidePushing && parent.getAlignment() != alignment
                    && (seqWasResizing
                        || (!LayoutInterval.wantResize(parent)
                            && LayoutInterval.wantResize(parent.getParent())))) {
                    layoutModel.setIntervalAlignment(parent, alignment);
                    boolean insertGap = false;
                    int index = parent.indexOf(interval);
                    if (alignment == LEADING) {
                        if (parent.getSubIntervalCount() <= index+1) {
                            insertGap = true;
                            index = -1;
                        } else {
                            index++;
                            LayoutInterval candidate = parent.getSubInterval(index);
                            if (candidate.isEmptySpace()) {
                                operations.setIntervalResizing(candidate, true);
                            } else {
                                insertGap = true;
                            }
                        }
                    } else {
                        assert (alignment == TRAILING);
                        if (index == 0) {
                            insertGap = true;
                        } else {                            
                            LayoutInterval candidate = parent.getSubInterval(index-1);
                            if (candidate.isEmptySpace()) {
                                operations.setIntervalResizing(candidate, true);
                            } else {
                                insertGap = true;
                            }
                        }
                    }
                    if (insertGap) { // could not change existing gap, adding new
                        LayoutInterval gap = new LayoutInterval(SINGLE);
                        operations.setIntervalResizing(gap, true);
                        layoutModel.setIntervalSize(gap, 0, 0, gap.getMaximumSize());
                        layoutModel.addInterval(gap, parent, index);
                        operations.optimizeGaps2(parent.getParent(), dimension);
                        parent = interval.getParent();
                    }
                    if (!seqWasResizing) { // also may need a supporting gap in the anchor direction
                        int pos = parent.getParent().getCurrentSpace().positions[dimension][alignment];
                        int size = (pos - parent.getCurrentSpace().positions[dimension][alignment]) * (alignment == LEADING ? -1 : 1);
                        if (size > 0) {
                            LayoutInterval gap = new LayoutInterval(SINGLE);
                            gap.setSize(size);
                            operations.insertGap(gap, parent, pos, dimension, alignment);
                        }
                    }
                    break; // assuming the anchor was clear before, so we need only one correction
                }
            }
            interval = parent;
            parent = parent.getParent();
        }
        visualStateUpToDate = false;
        updateDataAfterBuild = true;
        if (logTestCode()) {
            testCode.add("// < ADJUST COMPONENT ALIGNMENT"); //NOI18N
	}
    }

    /**
     * Returns alignment of the component as the first item of the array.
     * The second item of the array indicates whether the alignment can
     * be changed to leading or trailing (e.g. if the current value is not
     * enforced by other resizable components). The returned alignment is
     * global e.g. it shows which edge of the container the component will track.
     *
     * @param comp component whose alignment should be determined.
     * @param dimension dimension in which the alignment should be determined.
     * @return alignment (or -1 if the component doesn't have a global alignment)
     * as the first item of the array and (canBeChangedToLeading ? 1 : 0) +
     * (canBeChangedToTrailing ? 2 : 0) as the second item.
     */
    public int[] getAdjustableComponentAlignment(LayoutComponent comp, int dimension) {
        // Desperate workaround for issue 121243 - do not throw exception if something went wrong
        if (comp == null) return new int[] {-1, 0};
        LayoutInterval interval = comp.getLayoutInterval(dimension);
        boolean leadingFixed = true;
        boolean trailingFixed = true;
        boolean leadingAdjustable = true;
        boolean trailingAdjustable = true;
        
        if (LayoutInterval.wantResize(interval)) {
            leadingFixed = trailingFixed = leadingAdjustable = trailingAdjustable = false;
        }
        LayoutInterval parent = interval.getParent();
        while (parent != null) {
            if (!LayoutInterval.canResize(parent)) {
                leadingFixed = trailingFixed = leadingAdjustable = trailingAdjustable = true;
            } else if (parent.isParallel()) {
                if (LayoutInterval.wantResize(parent) && !LayoutInterval.wantResize(interval)) {
                    int alignment = interval.getAlignment();
                    if (alignment == LEADING) {
                        trailingFixed = false;
                    } else if (alignment == TRAILING) {
                        leadingFixed = false;
                    }
                }
            } else {
                boolean before = true;
                Iterator iter = parent.getSubIntervals();
                while (iter.hasNext()) {
                    LayoutInterval li = (LayoutInterval)iter.next();
                    if (li == interval) {
                        before = false;
                    } else if (LayoutInterval.wantResize(li)) {
                        boolean space = li.isEmptySpace();
                        if (before) {
                            leadingFixed = false;
                            if (!space) {
                                leadingAdjustable = false;
                            }
                        } else {
                            trailingFixed = false;
                            if (!space) {
                                trailingAdjustable = false;
                            }
                        }
                    }
                }
            }
            interval = parent;
            parent = parent.getParent();
        }
        int adjustable = (leadingAdjustable ? 1 << LEADING : 0) + (trailingAdjustable ? 1 << TRAILING : 0);
        if (leadingFixed && trailingFixed) {
            // As if top level group wantResize()
            if (LEADING == interval.getGroupAlignment()) {
                trailingFixed = false;
            } else {
                leadingFixed = false;
            }
        }
        int alignment;
        if (leadingFixed) {
            // !trailingFixed
            alignment = LEADING;
        } else {
            if (trailingFixed) {
                alignment = TRAILING;
            } else {
                alignment = -1;
            }
        }
        return new int[] {alignment, adjustable};
    }

    /**
     * Determines whether given component is auto-resizing in the layout (i.e.
     * will grow if the container grows) in given direction.
     *
     * @param comp component whose resizability should be determined.
     * @param dimension dimension in which the resizability should be determined.
     * @return <code>true</code> if the component is resizing, returns
     * <code>false</code> otherwise.
     */
    public boolean isComponentResizing(LayoutComponent comp, int dimension) {
        return LayoutInterval.wantResizeInLayout(comp.getLayoutInterval(dimension));
    }

    /**
     * Returns preferred size of the given interval (in pixels).
     *
     * @param interval interval whose preferred size should be determined.
     * @return preferred size of the given interval.
     */
    private int prefSizeOfInterval(LayoutInterval interval) {
        int dimension = -1;
        if (interval.isComponent()) {
            LayoutComponent comp = interval.getComponent();
            dimension = (interval == comp.getLayoutInterval(HORIZONTAL)) ? HORIZONTAL : VERTICAL;
            if (comp.isLinkSized(dimension)) {
                Collection linked = (Collection)layoutModel.getLinkSizeGroups(dimension).get(new Integer(comp.getLinkSizeId(dimension)));
                Iterator iter = linked.iterator();
                int prefSize = 0;
                while (iter.hasNext()) {
                    String compId = (String)iter.next();
                    LayoutComponent component = layoutModel.getLayoutComponent(compId);
                    LayoutInterval intr = component.getLayoutInterval(dimension);
                    int pref = intr.getPreferredSize();
                    if (pref == NOT_EXPLICITLY_DEFINED) {
                        Dimension prefDim = visualMapper.getComponentPreferredSize(compId);
                        pref = (dimension == HORIZONTAL) ? prefDim.width : prefDim.height;
                    }
                    prefSize = Math.max(pref, prefSize);
                }
                return prefSize;
            }
        }
        int prefSize = interval.getPreferredSize();
        if (prefSize == NOT_EXPLICITLY_DEFINED) {
            if (interval.isComponent()) {
                LayoutComponent comp = interval.getComponent();
                Dimension pref = visualMapper.getComponentPreferredSize(comp.getId());
                return (dimension == HORIZONTAL) ? pref.width : pref.height;
            } else if (interval.isEmptySpace()) {
                return LayoutUtils.getSizeOfDefaultGap(interval, visualMapper);
            } else {
                assert interval.isGroup();
                prefSize = 0;
                Iterator iter = interval.getSubIntervals();
                if (interval.isSequential()) {
                    while (iter.hasNext()) {
                        LayoutInterval subInterval = (LayoutInterval)iter.next();
                        prefSize += prefSizeOfInterval(subInterval);
                    }
                } else {
                    while (iter.hasNext()) {
                        LayoutInterval subInterval = (LayoutInterval)iter.next();
                        prefSize = Math.max(prefSize, prefSizeOfInterval(subInterval));
                    }                    
                }
            }
        }
        return prefSize;    
    }

    /**
     * Sets component resizability. Makes the component resizing or fixed.
     *
     * @param comp component whose resizability should be set.
     * @param dimension dimension in which the resizability should be changed.
     * @param resizable determines whether the component should be made
     * resizable in the given dimension.
     */
    public void setComponentResizing(LayoutComponent comp, int dimension, boolean resizing) {
        if (logTestCode()) {
            testCode.add("// > SET COMPONENT RESIZING"); //NOI18N
            testCode.add("{"); //NOI18N
            testCode.add("LayoutComponent comp = lm.getLayoutComponent(\"" + comp.getId() + "\");"); //NOI18N
            testCode.add("int dimension = " + dimension + ";"); //NOI18N
            testCode.add("boolean resizing = " + resizing + ";"); //NOI18N   
            testCode.add("ld.setComponentResizing(comp, dimension, resizing);"); //NOI18N
            testCode.add("}"); //NOI18N
        }
        LayoutInterval interval = comp.getLayoutInterval(dimension);
        
        // Unset the same-size if we are making the component resizable
        if (resizing && comp.isLinkSized(dimension)) {
            Collection linked = (Collection)layoutModel.getLinkSizeGroups(dimension).get(new Integer(comp.getLinkSizeId(dimension)));
            Collection toChange;
            if (linked.size() == 2) { // The second component will be unlinked, too.
                toChange = linked;
            } else {
                toChange = Collections.singletonList(comp.getId());
            }
            Iterator iter = toChange.iterator();
            while (iter.hasNext()) {
                String compId = (String)iter.next();
                LayoutComponent component = layoutModel.getLayoutComponent(compId);
                LayoutInterval intr = component.getLayoutInterval(dimension);
                Dimension prefDim = visualMapper.getComponentPreferredSize(compId);
                int prefSize = (dimension == HORIZONTAL) ? prefDim.width : prefDim.height;
                int currSize = intr.getCurrentSpace().size(dimension);
                if (currSize == prefSize) {
                    currSize = NOT_EXPLICITLY_DEFINED;
                }
                layoutModel.setIntervalSize(intr, intr.getMinimumSize(), currSize, intr.getMaximumSize());
            }
        }
        
        LayoutInterval parent = interval.getParent();
        operations.setIntervalResizing(interval, resizing);
        int delta = 0;
        if (!resizing) {
            int currSize = LayoutInterval.getCurrentSize(interval, dimension);
            int prefSize = prefSizeOfInterval(interval);
            delta = currSize - prefSize;
            if (delta != 0) {
                layoutModel.setIntervalSize(interval, interval.getMinimumSize(), currSize, interval.getMaximumSize());
            }
        }
        LayoutInterval intr = interval;
        LayoutInterval par = parent;
        while (par != null) {
            if (par.isParallel() && resizing) {
                int groupCurrSize = LayoutInterval.getCurrentSize(par, dimension);
                int currSize = LayoutInterval.getCurrentSize(intr, dimension);
                if (groupCurrSize != currSize) {
                    LayoutInterval seqGroup = intr;
                    LayoutInterval space = new LayoutInterval(SINGLE);
                    space.setSize(groupCurrSize - currSize);
                    int alignment = intr.getAlignment();
                    int index = (alignment == LEADING) ? -1 : 0;
                    if (intr.isSequential()) {
                        int spaceIndex = (alignment == LEADING) ? intr.getSubIntervalCount()-1 : 0;
                        LayoutInterval adjacentSpace = intr.getSubInterval(spaceIndex);
                        if (adjacentSpace.isEmptySpace()) {
                            int spaceSize = LayoutInterval.getCurrentSize(adjacentSpace, dimension);
                            layoutModel.removeInterval(adjacentSpace);
                            space.setSize(groupCurrSize - currSize + spaceSize);
                        }
                    } else {
                        seqGroup = new LayoutInterval(SEQUENTIAL);
                        layoutModel.setIntervalAlignment(intr, DEFAULT);
                        seqGroup.setAlignment(alignment);
                        int i = layoutModel.removeInterval(intr);
                        layoutModel.addInterval(intr, seqGroup, -1);
                        layoutModel.addInterval(seqGroup, par, i);
                    }
                    layoutModel.addInterval(space, seqGroup, index);
                    seqGroup.getCurrentSpace().set(dimension, par.getCurrentSpace());
                }
            } else if (par.isSequential()) {
                // Change resizability of gaps
                boolean parentSeq = (parent == par);
                List<LayoutInterval> resizableList = new LinkedList<LayoutInterval>();
                int alignment = parentSeq ? LayoutInterval.getEffectiveAlignment(interval) : 0;
                LayoutInterval leadingGap = null;
                LayoutInterval trailingGap = null;
                boolean afterDefining = false;
                for (int i=0; i < par.getSubIntervalCount(); i++) {
                    LayoutInterval candidate = par.getSubInterval(i);
                    if (candidate == interval) {
                        afterDefining = true;
                    }
                    if (candidate.isEmptySpace()) {
                        if (resizing) {
                            int currSize = LayoutInterval.getCurrentSize(candidate, dimension);
                            operations.setIntervalResizing(candidate, false);
                            int prefSize = prefSizeOfInterval(candidate);
                            if (currSize != prefSize) {
                                layoutModel.setIntervalSize(candidate, candidate.getMinimumSize(),
                                    currSize, candidate.getMaximumSize());
                                delta += currSize - prefSize;
                            }
                            if (operations.eliminateUnwantedZeroGap(candidate)) {
                                i--;
                            }
                        } else if (parentSeq) {
                            boolean glue = (candidate.getPreferredSize() != NOT_EXPLICITLY_DEFINED);
                            if (glue) {
                                trailingGap = candidate;
                            } else {
                                if (afterDefining && ((trailingGap == null) || (trailingGap.getPreferredSize() == NOT_EXPLICITLY_DEFINED))) {
                                    trailingGap = candidate;
                                }
                            }
                            if ((leadingGap == null) && !afterDefining) {
                                leadingGap = candidate;
                            } else {
                                if (glue && (leadingGap == null || leadingGap.getPreferredSize() == NOT_EXPLICITLY_DEFINED)) {
                                    leadingGap = candidate;
                                }
                            }
                        }
                    } else {
                        if (candidate.getMaximumSize() == Short.MAX_VALUE) {
                            resizableList.add(candidate);
                        }
                    }
                }
                if (resizableList.size() > 0) {
                    Iterator<LayoutInterval> iter = resizableList.iterator();
                    delta = (LayoutInterval.getCurrentSize(par, dimension) - prefSizeOfInterval(par) + delta)/resizableList.size();
                    while (iter.hasNext()) {
                        LayoutInterval candidate = iter.next();
                        if (candidate.isGroup()) {
                            // PENDING currSize could change - we can't modify prefSize of group directly
                        } else {
                            if (candidate == interval) {
                                if (delta != 0) {
                                    int prefSize = prefSizeOfInterval(candidate);
                                    layoutModel.setIntervalSize(candidate, candidate.getMinimumSize(),
                                        Math.max(0, prefSize - delta), candidate.getMaximumSize());
                                }
                            } else {
                                int currSize = LayoutInterval.getCurrentSize(candidate, dimension);
                                layoutModel.setIntervalSize(candidate, candidate.getMinimumSize(),
                                    Math.max(0, currSize - delta), candidate.getMaximumSize());                            
                            }
                        }
                    }
                }
                if (parentSeq) {
                    if (!LayoutInterval.wantResize(par)) {
                        LayoutInterval gap = null;
                        if ((alignment == TRAILING) && (leadingGap != null)) {
                            gap = leadingGap;
                            operations.setIntervalResizing(leadingGap, !resizing);
                        }
                        if ((alignment == LEADING) && (trailingGap != null)) {
                            gap = trailingGap;
                            operations.setIntervalResizing(trailingGap, !resizing);
                        }
                        if ((gap != null) && (delta != 0) && (gap.getPreferredSize() != NOT_EXPLICITLY_DEFINED)) {
                            layoutModel.setIntervalSize(gap, gap.getMinimumSize(), 
                                Math.max(0, gap.getPreferredSize() - delta), gap.getMaximumSize());
                        }
                        operations.eliminateUnwantedZeroGap(gap);
                    }
                    parent = par.getParent(); // use parallel parent for group resizing check
                }
            }
            intr = par;
            par = par.getParent();
        }
        
        // Unset the same size once all changes in gap sizes are done
        if (resizing) {
            layoutModel.unsetSameSize(Collections.singletonList(comp.getId()), dimension);
        }

        if (resizing) {
            // cancel possible suppressed resizing
            while (parent != null) {
                if (!LayoutInterval.canResize(parent)) {
                    operations.enableGroupResizing(parent);
                }
                parent = parent.getParent();
            }
        }

        requireStructureOptimization(comp.getParent());
        visualStateUpToDate = false;
        updateDataAfterBuild = true;
        if (logTestCode()) {
	    testCode.add("// < SET COMPONENT RESIZING"); //NOI18N
	}
    }
    
    /**
     * Checks if given components can be aligned together in a parallel group
     * using the 'align' method. For now it is enough if the components are in
     * the same container and layer (under same layout roots).
     * @param componentIds IDs of components that should be aligned
     * @return true if the components can be aligned in a parallel group
     */
    public boolean canAlign(Collection<String> componentIds) {
        if (componentIds != null && componentIds.size() > 1) {
            LayoutInterval commonRoot = null;
            for (String id : componentIds) {
                LayoutComponent component = layoutModel.getLayoutComponent(id);
                if (component == null || component.getParent() == null) {
                    return false;
                }
                LayoutInterval root = LayoutInterval.getRoot(component.getLayoutInterval(HORIZONTAL));
                if (commonRoot == null) {
                    commonRoot = root;
                } else if (root != commonRoot) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Aligns given components in the specified direction.
     *
     * @param componentIds IDs of components that should be aligned.
     * @param closed determines if closed group should be created.
     * @param dimension dimension to align in.
     * @param alignment requested alignment.
     */
    public void align(Collection componentIds, boolean closed, int dimension, int alignment) {
        if (logTestCode()) {
            testCode.add("// > ALIGN"); //NOI18N
            testCode.add("{"); //NOI18N
	    LayoutTestUtils.writeCollection(testCode, "componentIds", componentIds); //NOI18N
            testCode.add("boolean closed = " + closed + ";"); //NOI18N
            testCode.add("int dimension = " + dimension + ";");        //NOI18N   
            testCode.add("int alignment = " + alignment + ";");         //NOI18N  
            testCode.add("ld.align(componentIds, closed, dimension, alignment);"); //NOI18N
            testCode.add("}"); //NOI18N
        }
        LayoutComponent container = null;
        LayoutInterval[] intervals = new LayoutInterval[componentIds.size()];
        int counter = 0;
        Iterator iter = componentIds.iterator();        
        while (iter.hasNext()) {
            String id = (String)iter.next();
            LayoutComponent component = layoutModel.getLayoutComponent(id);
            if (container == null) {
                container = component.getParent();
            }
            intervals[counter++] = component.getLayoutInterval(dimension);            
        }

        new LayoutAligner(this, layoutModel, operations).alignIntervals(intervals, closed, dimension, alignment);

        requireStructureOptimization(container);
        visualStateUpToDate = false;
        updateDataAfterBuild = true;
        if (logTestCode()) {
	    testCode.add("// < ALIGN"); //NOI18N
	}
    }

    private boolean destroyRedundantGroups(LayoutInterval interval) {
        boolean updated = false;
        for (int i=interval.getSubIntervalCount()-1; i>=0; i--) {
            if (i >= interval.getSubIntervalCount()) continue;
            LayoutInterval subInterval = interval.getSubInterval(i);
            if (subInterval.isGroup()) {
                destroyRedundantGroups(subInterval);
                destroyGroupIfRedundant(subInterval, interval);
                updated |= (subInterval.getParent() == null);
            }
        }
        return updated;
    }

    /**
     * Destroys the given group if it is redundant in the layout model.
     *
     * @param group group whose necessity should be checked.
     * @param boundary parent of the group that limits the changes that
     * should be made e.g. no changes outside of this group even if it were
     * itself redundant. Can be <code>null</code> if there's no boundary.
     */
    void destroyGroupIfRedundant(LayoutInterval group, LayoutInterval boundary) {
        if ((group == null) || (!group.isGroup()) || (group == boundary)) return;
        LayoutInterval parent = group.getParent();
        // Don't destroy root intervals
        if (parent == null) return;

        // Remove empty groups
        if (LayoutInterval.getCount(group, LayoutRegion.ALL_POINTS, true) == 0) {
            takeOutInterval(group, boundary);
            return;
        }

        if (operations.dissolveRedundantGroup(group)) {
            destroyGroupIfRedundant(parent, boundary);
        }
    }

    /**
     * Removes the given interval from the layout model. Consolidates
     * parent groups if necessary.
     *
     * @param interval interval that should be removed.
     * @param boundary parent of the group that limits the changes that
     * should be made e.g. no changes outside of this group even if it were
     * itself redundant. Can be <code>null</code> if there's no boundary.
     */
    void takeOutInterval(LayoutInterval interval, LayoutInterval boundary) {
        LayoutInterval parent = interval.getParent();
        int index = parent.indexOf(interval);
        List<LayoutInterval> toRemove = new LinkedList<LayoutInterval>();
        toRemove.add(interval);
        if (parent.isSequential()) {
            // Remove leading gap
            if (index > 0) {
                LayoutInterval li = parent.getSubInterval(index-1);
                if (li.isEmptySpace()) {
                    toRemove.add(li);
                }
            }
            // Remove trailing gap
            if (index+1 < parent.getSubIntervalCount()) {
                LayoutInterval li = parent.getSubInterval(index+1);
                if (li.isEmptySpace()) {
                    toRemove.add(li);
                }
            }
            // Add dummy gap if necessary
            if ((toRemove.size() == 3) && (parent.getSubIntervalCount() > 3)) {
                LayoutInterval gap = new LayoutInterval(SINGLE);
                if (interval.isComponent() && (interval.getComponent().getLayoutInterval(VERTICAL) == interval)) {
                    int alignment = LayoutInterval.getEffectiveAlignment(interval);
                    int size = 0;
                    for (int i=0; i<3; i++) {
                        size += LayoutInterval.getCurrentSize(toRemove.get(i), VERTICAL);
                    }
                    gap.setSizes(NOT_EXPLICITLY_DEFINED, size, (alignment == TRAILING) ? Short.MAX_VALUE : USE_PREFERRED_SIZE);
                }
                layoutModel.addInterval(gap, parent, index);
            }
        }
        Iterator iter = toRemove.iterator();
        while (iter.hasNext()) {
            LayoutInterval remove = (LayoutInterval)iter.next();
            layoutModel.removeInterval(remove);
        }
        // Consolidate parent
        destroyGroupIfRedundant(parent, boundary);
    }

    // -----

    // [should change to operations.addGroupContent]
    /**
     * Creates a remainder parallel group (remainder to a main group of
     * aligned intervals).
     * @param list the content of the group, output from 'extract' method
     * @param seq a sequential group where to add to
     * @param index the index of the main group in the sequence
     * @param position the position of the remainder group relative to the main
     *        group (LEADING or TRAILING)
     * @param mainAlignment effective alignment of the main group (LEADING or
     *        TRAILING or something else meaning not aligned)
     * @param dimension dimension the remainder group is created in.
     */
    void createRemainderGroup(List list, LayoutInterval seq,
                                      int index, int position, int mainAlignment, int dimension)
    {
        assert seq.isSequential() && (position == LEADING || position == TRAILING);
        if (position == TRAILING) {
            index++;
        }
        // [revisit the way how spaces are handled - in accordance to optimizeGaps]
        
        LayoutInterval gap = null;
        LayoutInterval leadingGap = null;
        LayoutInterval trailingGap = null;
        boolean onlyGaps = true;
        boolean gapLeads = true;
        boolean gapTrails = true;

        // Remove sequences just with one gap
        for (int i = list.size()-1; i>=0; i--) {
            List subList = (List)list.get(i);
            if (subList.size() == 2) { // there is just one interval
                int alignment = ((Integer)subList.get(0)).intValue();
                LayoutInterval li = (LayoutInterval) subList.get(1);
                if (li.isEmptySpace()) {
                    if (gap == null || li.getMaximumSize() > gap.getMaximumSize()) {
                        gap = li;
                    }
                    if (isFixedPadding(li)) {
                        if (alignment == LEADING) {
                            leadingGap = li;
                            gapTrails = false;
                        }
                        else if (alignment == TRAILING) {
                            trailingGap = li;
                            gapLeads = false;
                        }
                    }
                    else {
                        gapLeads = false;
                        gapTrails = false;
                    }
                    list.remove(i);
                }
                else {
                    onlyGaps = false;
                }
            }
        }

        if (list.size() == 1) { // just one sequence, need not a group
            List subList = (List) list.get(0);
            Iterator itr = subList.iterator();
            itr.next(); // skip alignment
            do {
                LayoutInterval li = (LayoutInterval) itr.next();
                layoutModel.addInterval(li, seq, index++);
            }
            while (itr.hasNext());
            return;
        }

        // find common ending gaps, possibility to eliminate some...
        for (Iterator it=list.iterator(); it.hasNext(); ) {
            List subList = (List) it.next();
            if (subList.size() != 2) { // there are more intervals (will form a sequential group)
                onlyGaps = false;

                boolean first = true;
                Iterator itr = subList.iterator();
                itr.next(); // skip seq. alignment
                do {
                    LayoutInterval li = (LayoutInterval) itr.next();
                    if (first) {
                        first = false;
                        if (isFixedPadding(li))
                            leadingGap = li;
                        else
                            gapLeads = false;
                    }
                    else if (!itr.hasNext()) {
                        if (isFixedPadding(li))
                            trailingGap = li;
                        else
                            gapTrails = false;
                    }
                }
                while (itr.hasNext());
            }
        }

        if (onlyGaps) {
            operations.insertGapIntoSequence(gap, seq, index, dimension);
            return;
        }

        // create group
        LayoutInterval group = new LayoutInterval(PARALLEL);
        if (position == mainAlignment) {
            // [but this should eliminate resizability only for gaps...]
            group.setMinimumSize(USE_PREFERRED_SIZE);
            group.setMaximumSize(USE_PREFERRED_SIZE);
        }

        // fill the group
        for (Iterator it=list.iterator(); it.hasNext(); ) {
            List subList = (List) it.next();

            if (gapLeads) {
                subList.remove(1);
            }
            if (gapTrails) {
                subList.remove(subList.size()-1);
            }

            LayoutInterval interval;
            if (subList.size() == 2) { // there is just one interval - use it directly
                int alignment = ((Integer)subList.get(0)).intValue();
                interval = (LayoutInterval) subList.get(1);
                if (alignment == LEADING || alignment == TRAILING) {
                    layoutModel.setIntervalAlignment(interval, alignment);
                }
            }
            else { // there are more intervals - group them in a sequence
                interval = new LayoutInterval(SEQUENTIAL);
                Iterator itr = subList.iterator();
                int alignment = ((Integer)itr.next()).intValue();
                if (alignment == LEADING || alignment == TRAILING) {
                    interval.setAlignment(alignment);
                }
                do {
                    LayoutInterval li = (LayoutInterval) itr.next();
                    layoutModel.addInterval(li, interval, -1);
                }
                while (itr.hasNext());
            }
            layoutModel.addInterval(interval, group, -1);
        }

        // add the group to the sequence
        if (gapLeads) {
            layoutModel.addInterval(leadingGap, seq, index++);
        }
        layoutModel.addInterval(group, seq, index++);
        if (gapTrails) {
            layoutModel.addInterval(trailingGap, seq, index);
        }
    }

    static boolean isFixedPadding(LayoutInterval interval) {
        return interval.isEmptySpace()
               && (interval.getMinimumSize() == NOT_EXPLICITLY_DEFINED || interval.getMinimumSize() == USE_PREFERRED_SIZE)
               && interval.getPreferredSize() == NOT_EXPLICITLY_DEFINED
               && (interval.getMaximumSize() == NOT_EXPLICITLY_DEFINED || interval.getMaximumSize() == USE_PREFERRED_SIZE);
    }

    // -----

    // requires the layout image up-to-date (all positions known)
    // requires the group contains some component (at least indirectly)
    private int optimizeGaps(LayoutInterval group, int dimension, boolean recursive) {
        assert group.isParallel();

        // sub-groups first (not using iterator, intervals may change)
        if (recursive) {
            for (int i=0; i < group.getSubIntervalCount(); i++) {
                LayoutInterval li = group.getSubInterval(i);
                if (li.isParallel()) {
                    optimizeGaps(li, dimension, recursive);
                }
                else if (li.isSequential()) {
                    for (int ii=0; ii < li.getSubIntervalCount(); ii++) {
                        LayoutInterval llii = li.getSubInterval(ii);
                        if (llii.isParallel()) {
                            int idx = optimizeGaps(llii, dimension, recursive);
                            if (idx >= 0) // position in sequence changed (a gap inserted)
                                ii = idx;
                        }
                    }
                }
            }
        }

        if (group.getGroupAlignment() == BASELINE) {
            return -1;
        }
        int nonEmptyCount = LayoutInterval.getCount(group, LayoutRegion.ALL_POINTS, true);
        if (nonEmptyCount <= 1) {
            if (group.getParent() == null) {
                if (group.getSubIntervalCount() > 1) {
                    // [removing container supporting gap]
                    for (int i=group.getSubIntervalCount()-1; i >= 0; i--) {
                        if (group.getSubInterval(i).isEmptySpace()) {
                            layoutModel.removeInterval(group, i);
                            break;
                        }
                    }
                }
                else if (group.getSubIntervalCount() == 0) {
                    // [sort of hack - would be nice the filling gap is ensured somewhere else]
                    propEmptyContainer(group, dimension);
                }
            } else { // [dissolving one-member group should not be here]
                assert (nonEmptyCount == 1);
                assert (group.getSubIntervalCount() == 1);
                LayoutInterval interval = group.getSubInterval(0);
                //int alignment = interval.getAlignment();
                layoutModel.removeInterval(interval);
                layoutModel.setIntervalAlignment(interval, group.getAlignment());
                LayoutInterval parent = group.getParent();
                int index = layoutModel.removeInterval(group);
                if (parent.isSequential() && interval.isSequential()) {
                    // dissolve the sequential group in its parent                    
                    for (int i=interval.getSubIntervalCount()-1; i>=0; i--) {
                        LayoutInterval subInterval = interval.getSubInterval(i);
                        layoutModel.removeInterval(subInterval);
                        layoutModel.addInterval(subInterval, parent, index);
                    }
                    eliminateConsecutiveGaps(parent, dimension);
                } else {
                    layoutModel.addInterval(interval, parent, index);
                }
            }
            return -1;
        }

        return operations.optimizeGaps(group, dimension);
    }

    // -----

    public void setDefaultSize(String compId) {
        if (logTestCode()) {
            testCode.add("// > SET DEFAULT SIZE"); //NOI18N
            testCode.add("ld.setDefaultSize(\"" + compId + "\");"); //NOI18N
        }        
        LayoutComponent component = layoutModel.getLayoutComponent(compId);
        if (component != null) {
            setDefaultSize(component, true);
        }
        if (logTestCode()) {
            testCode.add("// < SET DEFAULT SIZE"); //NOI18N
	}
    }

    private void setDefaultSize(LayoutComponent component, boolean top) {
        if (component.isLayoutContainer()) {
            for (LayoutComponent comp : component.getSubcomponents()) {
                if (comp.isLayoutContainer()) {
                    setDefaultSize(comp, false);
                }
            }
            for (LayoutInterval[] roots : component.getLayoutRoots()) {
                for (int dim=0; dim < DIM_COUNT; dim++) {
                    setDefaultSizeInContainer(roots[dim], true);
                    operations.enableFlexibleSizeDefinition(roots[dim], false);
                }
            }
        }
        String[] subComps = visualMapper.getIndirectSubComponents(component.getId());
        if (subComps != null) {
            for (String subId : subComps) {
                LayoutComponent comp = layoutModel.getLayoutComponent(subId);
                if (comp != null) {
                    setDefaultSize(comp, false);
                }
            }
        }
        if (component.getParent() != null) {
            Dimension prefSize = null;
            for (int dim=0; dim < DIM_COUNT; dim++) {
                LayoutInterval li = component.getLayoutInterval(dim);
                if (top) {
                    int currSize = LayoutInterval.getCurrentSize(li, dim);
                    if (currSize != LayoutRegion.UNKNOWN) {
                        if (prefSize == null) {
                            prefSize = visualMapper.getComponentPreferredSize(component.getId());
                        }
                        if (prefSize != null && (dim==HORIZONTAL ? prefSize.width : prefSize.height) < currSize
                                && LayoutInterval.canResize(li)) {
                            enableShrinking(li);
                            preferredSizeChanged = true;
                        }
                    }
                }
                operations.resizeInterval(li, NOT_EXPLICITLY_DEFINED);
            }
        } else {
            preferredSizeChanged = true;
        }
        updateDataAfterBuild = true;
    }

    private void setDefaultSizeInContainer(LayoutInterval interval, boolean complete) {
        if (!interval.isGroup()) {
            if (LayoutInterval.canResize(interval)) {
                operations.resizeInterval(interval,
                        interval.getMinimumSize() != USE_PREFERRED_SIZE ? interval.getMinimumSize() : NOT_EXPLICITLY_DEFINED);
            }
        } else if (complete || LayoutInterval.canResize(interval)) {
            for (Iterator it=interval.getSubIntervals(); it.hasNext(); ) {
                setDefaultSizeInContainer((LayoutInterval)it.next(), complete);
            }
        }
    }

    private static void cleanRefreshAttrs(LayoutInterval group) {
        group.unsetAttribute(LayoutInterval.REFRESHING_ATTRS);
        for (int i=0,n=group.getSubIntervalCount(); i < n; i++) {
            LayoutInterval li = group.getSubInterval(i);
            if (li.isGroup()) {
                cleanRefreshAttrs(li);
            } else {
                li.unsetAttribute(LayoutInterval.REFRESHING_ATTRS);
            }
        }
    }

    private void findContainerResizingGap(LayoutInterval rootInterval, int dimension) {
        if (!LayoutInterval.wantResize(rootInterval) && // See issue 66849
            (LayoutInterval.getCurrentSize(rootInterval, dimension) != prefSizeOfInterval(rootInterval))) {
            // Resizing gap would change the layout
            return;
        }
        // find gap for container resizing
        int gapPosition = TRAILING;
        LayoutInterval resGap = findContainerResizingGap(rootInterval, dimension, gapPosition);
        if (resGap == null) {
            gapPosition = LEADING;
            resGap = findContainerResizingGap(rootInterval, dimension, gapPosition);
            if (resGap == null) {
                gapPosition = -1;
                resGap = findContainerResizingGap(rootInterval, dimension, gapPosition);
                if (resGap == null) {
                    return;
                }
            }
        }
        else if (!LayoutInterval.canResize(resGap)) { // we prefer resizing gaps
            LayoutInterval gap = findContainerResizingGap(rootInterval, dimension, LEADING);
            if (gap != null && LayoutInterval.canResize(gap)) {
                resGap = gap;
                gapPosition = LEADING;
            }
            else {
                gap = findContainerResizingGap(rootInterval, dimension, -1);
                if (gap != null && LayoutInterval.canResize(gap)) {
                    resGap = gap;
                    gapPosition = -1;
                }
            }
        }

        resGap.setAttribute(LayoutInterval.ATTR_DESIGN_CONTAINER_GAP);
    }

    private static LayoutInterval findContainerResizingGap(LayoutInterval group, int dimension, int alignment) {
        assert group.isParallel();
        LayoutInterval theGap = null;
        for (Iterator it=group.getSubIntervals(); it.hasNext(); ) {
            LayoutInterval sub = (LayoutInterval) it.next();
            LayoutInterval gap = null;
            if (sub.isSequential()) {
                int n = sub.getSubIntervalCount();
                if (alignment == LEADING || alignment == TRAILING) { // looking for ending gap
                    LayoutInterval li = sub.getSubInterval(alignment == LEADING ? 0 : n-1);
                    if (li.isEmptySpace()) {
                        if (LayoutInterval.canResize(li)
                            || (alignment == TRAILING
                                && (li.getPreferredSize() != NOT_EXPLICITLY_DEFINED || LayoutInterval.wantResize(sub)))) {
                            gap = li;
                        }
                    } else if (li.isParallel()) {
                        gap = findContainerResizingGap(li, dimension, alignment);
                    }
                } else { // somewhere in the middle
                    for (int i=n-2; i > 0; i--) {
                        LayoutInterval li = sub.getSubInterval(i);
                        if (li.isEmptySpace() && LayoutInterval.canResize(li)) {
                            gap = li;
                            break;
                        }
                    }
                }
            } else if (sub.isParallel()) {
                gap = findContainerResizingGap(sub, dimension, alignment);
            }

            if (gap != null) {
                if (theGap == null) {
                    theGap = gap;
                } else {
                    return null; // can't use more than one gap
                }
            }
        }
        return theGap;
    }

    private void updateContainerAfterBuild(LayoutComponent container, boolean top) {
        visualState.updateCurrentSpaceOfComponents(container, top);
        for (LayoutComponent subComp : container.getSubcomponents()) {
            if (subComp.isLayoutContainer()) {
                updateContainerAfterBuild(subComp, false);
            }
        }

        for (int i=0; i < DIM_COUNT; i++) {
            LayoutInterval defaultRoot = getActiveLayoutRoots(container)[i];
            for (LayoutInterval[] roots : container.getLayoutRoots()) {
                LayoutInterval root = roots[i];
                if (updateDataAfterBuild) {
                    optimizeStructure1(root, root == defaultRoot, i);
                    visualState.updateCurrentSpaceOfGroups(root, i, null); // just updating current space, no changes
                    optimizeStructure2(root, i);
                    cleanRefreshAttrs(root);
                    findContainerResizingGap(root, i);
                    int diffFromDefault = visualState.collectResizingDiffs(root, i);
                    if (top) { // update size definition in the whole hierarchy (for resizing intervals)
                        int sizeUpdate;
                        if (diffFromDefault == 0) {
                            sizeUpdate = 0; // everything can be default (no need to define size)
                        } else if (designerResized == null || !designerResized[i]) {
                            // Update existing explicit sizes without unnecesarily changing
                            // (optimizing) the default vs explicit size assignment. Especially just
                            // after loaded don't want to change sizes that are sligtly off due to
                            // small platform/L&F differences (or Swing quirkiness). Here the update
                            // will only be forced in activelly changed areas, otherwise only on
                            // direct changes, e.g. when resizing a container (so not here).
                            sizeUpdate = 1;
                        } else {
                            sizeUpdate = 2; // must set explicit size somewhere to support actual container size
                        }
                        visualState.updateToActualSize(root, i, sizeUpdate);
                    }
                } else {
                    visualState.updateCurrentSpaceOfGroups(root, i, null);
                    visualState.collectResizingDiffs(root, i);
                }
            }
        }
        if (!top && updateDataAfterBuild && container.getParent() != null) {
            visualState.updateContainerSize(container);
        }
    }

    private void updateContainerAfterResized(LayoutComponent container, LayoutDragger.SizeDef[] resizingDef) {
        container.setCurrentInterior(visualMapper.getContainerInterior(container.getId()));
        for (LayoutComponent subComp : container.getSubcomponents()) {
            Rectangle bounds = visualMapper.getComponentBounds(subComp.getId());
            int baseline = visualMapper.getBaselinePosition(subComp.getId(), bounds.width, bounds.height);
            subComp.setCurrentBounds(bounds, baseline);
            if (subComp.isLayoutContainer()) {
                updateContainerAfterResized(subComp, null);
            }
        }

        for (int dim=0; dim < DIM_COUNT; dim++) {
            LayoutInterval outer = container.getLayoutInterval(dim);
            int currentSize = outer.getCurrentSpace().size(dim);
            for (LayoutInterval[] roots : container.getLayoutRoots()) {
                LayoutInterval root = roots[dim];
                if (root.getSubIntervalCount() == 0) {
                    continue;
                }
                visualState.updateCurrentSpaceOfGroups(root, dim, null);
                cleanRefreshAttrs(root);
                int diffFromDefault = visualState.collectResizingDiffs(root, dim);
                if (resizingDef != null && resizingDef[dim] != null) { // this is the resized container and dimension directly
                    visualState.updateToActualSize(root, dim, diffFromDefault != 0 ? 2 : 0); // does for the whole hierarchy
                    updateContainerResizingGap(root, resizingDef[dim], currentSize, dim);
                }
            }
        }
    }

    private void updateContainerResizingGap(LayoutInterval root, LayoutDragger.SizeDef resizingDef,
                                            int currentSize, int dim) {
        LayoutInterval resGap = resizingDef.getResizedGap();
        if (resGap != null) { // this is it (special gap for design time resizing)
            LayoutInterval gapParent = resGap.getParent();
            if (gapParent != null && root.isParentOf(gapParent)) {
                LayoutInterval seqRoot = LayoutInterval.getRoot(resGap, SEQUENTIAL);
                int size = resizingDef.getResizedGapSize(currentSize);
                int index = gapParent.indexOf(resGap);
                if (size == 0 && (index == 0 || index == gapParent.getSubIntervalCount()-1)) { // remove the gap
                    layoutModel.removeInterval(resGap);
                    if (gapParent.getSubIntervalCount() == 1) {
                        LayoutInterval last = layoutModel.removeInterval(gapParent, 0);
                        operations.addContent(last, gapParent.getParent(), layoutModel.removeInterval(gapParent));
                    } else if (LayoutInterval.canResize(resGap) && !LayoutInterval.wantResize(seqRoot)) {
                        // don't lose resizability of the layout
                        index = index == 0 ? gapParent.getSubIntervalCount()-1 : 0;
                        LayoutInterval otherGap = gapParent.getSubInterval(index);
                        if (otherGap.isEmptySpace()) { // the gap should be resizing
                            layoutModel.setIntervalSize(otherGap,
                                NOT_EXPLICITLY_DEFINED, otherGap.getPreferredSize(), Short.MAX_VALUE);
                        }
                    }
                } else if (size != LayoutRegion.UNKNOWN && !LayoutInterval.canResize(resGap)) {
                    if (!LayoutInterval.wantResize(seqRoot)) {
                        // correction: missing resizing interval, make the gap resizing
                        layoutModel.setIntervalSize(resGap, NOT_EXPLICITLY_DEFINED, size, Short.MAX_VALUE);
                    } else {
                        operations.resizeInterval(resGap, size);
                    }
                }
            }
        } else if (!LayoutInterval.wantResize(root)) {
            // no resizing gap in fixed layout of resizing container
            int minLayoutSize = computeMinimumDesignSize(root);
            int growth = root.getCurrentSpace().size(dim) - minLayoutSize;
            if (growth > 0) { // correction: add new resizing gap at the end to hold the new extra space
                LayoutInterval endGap = new LayoutInterval(SINGLE);
                endGap.setSizes(NOT_EXPLICITLY_DEFINED, growth, Short.MAX_VALUE);
                operations.insertGap(endGap, root, minLayoutSize, dim, TRAILING);
            }
        }
    }

    private void optimizeStructure1(LayoutInterval root, boolean defaultRoot, int dimension) {
        if (!root.hasAttribute(LayoutInterval.ATTR_OPTIMIZED)) {
            if (root.getSubIntervalCount() == 0) {
                if (defaultRoot) {
                    propEmptyContainer(root, dimension); // add a filling gap
                }
            } else {
                fixGroupSizes(root);
                destroyRedundantGroups(root);
                operations.fixSurplusOrMissingGaps(root, dimension);
            }
        }
    }

    private void optimizeStructure2(LayoutInterval root, int dimension) {
        if (!root.hasAttribute(LayoutInterval.ATTR_OPTIMIZED)) {
            Object mark = layoutModel.getChangeMark();
            optimizeGaps(root, dimension, true);
            destroyRedundantGroups(root);
            if (!layoutModel.getChangeMark().equals(mark)) {
                // something changed, update again
                visualState.updateCurrentSpaceOfGroups(root, dimension, null);
            }
            root.setAttribute(LayoutInterval.ATTR_OPTIMIZED);
        }
    }

    /**
     * Suppressed resizing should be used only on parallel groups. It used to
     * be set on sequential groups sometimes by error. If some old form with
     * such a group is opened, it is fixed here.
     */
    private void fixGroupSizes(LayoutInterval group) {
        for (int i=0; i < group.getSubIntervalCount(); i++) {
            LayoutInterval li = group.getSubInterval(i);
            if (li.isGroup()) {
                fixGroupSizes(li);
            }
        }
        int min = group.getMinimumSize();
        if (min != NOT_EXPLICITLY_DEFINED) {
            min = NOT_EXPLICITLY_DEFINED;
        }
        int pref = group.getPreferredSize();
        if (pref != NOT_EXPLICITLY_DEFINED) {
            pref = NOT_EXPLICITLY_DEFINED;
        }
        int max = group.getMaximumSize();
        if (max != NOT_EXPLICITLY_DEFINED
                && (!group.isParallel() || group.getMaximumSize() != USE_PREFERRED_SIZE)) {
            max = NOT_EXPLICITLY_DEFINED;
        }
        if (min != group.getMinimumSize() || pref != group.getPreferredSize()
                || max != group.getMaximumSize()) {
            layoutModel.setIntervalSize(group, min, pref, max);
        }
    }

    private void propEmptyContainer(LayoutInterval root, int dimension) {
        assert root.getParent() == null && root.getSubIntervalCount() == 0;
        LayoutInterval gap = new LayoutInterval(SINGLE);
        gap.setSizes(0, root.getCurrentSpace().size(dimension), Short.MAX_VALUE);
        layoutModel.addInterval(gap, root, 0);
    }

    private int computeMinimumDesignSize(LayoutInterval interval) {
        int size = 0;
        if (interval.isSingle()) {
            int min = interval.getMinimumSize();
            size = min == USE_PREFERRED_SIZE ? interval.getPreferredSize() : min;
            if (size == NOT_EXPLICITLY_DEFINED) {
                if (interval.isComponent()) {
                    LayoutComponent comp = interval.getComponent();
                    Dimension dim = min == USE_PREFERRED_SIZE ?
                                    visualMapper.getComponentPreferredSize(comp.getId()) :
                                    visualMapper.getComponentMinimumSize(comp.getId());
                    size = interval==comp.getLayoutInterval(HORIZONTAL) ? dim.width : dim.height;
                }
                else { // gap
                    size = LayoutUtils.getSizeOfDefaultGap(interval, visualMapper);
                }
            }
        }
        else if (interval.isSequential()) {
            for (int i=0, n=interval.getSubIntervalCount(); i < n; i++) {
                size += computeMinimumDesignSize(interval.getSubInterval(i));
            }
        }
        else { // parallel group
            for (int i=0, n=interval.getSubIntervalCount(); i < n; i++) {
                size = Math.max(size, computeMinimumDesignSize(interval.getSubInterval(i)));
            }
        }
        return size;
    }

    // -----

    /**
     * Removes given components from the layout, adjusting the overall layout
     * accordingly (so e.g. positions/sizes of other components don't change,
     * size of the entire container is unaffected, etc).
     */
    public void removeComponents(String... compIds) {
        LayoutComponent[] components = new LayoutComponent[compIds.length];
        for (int i=0; i < compIds.length; i++) {
            components[i] = layoutModel.getLayoutComponent(compIds[i]);
        }
        removeComponents(components, true);
    }

    /**
     * Removes components from parent container, removes their intervals and if
     * a component is not a layout container, it is also unregistered from
     * LayoutModel.
     */
    public void removeComponentsFromParent(String... compIds) {
        LayoutComponent[] components = new LayoutComponent[compIds.length];
        for (int i=0; i < compIds.length; i++) {
            components[i] = layoutModel.getLayoutComponent(compIds[i]);
        }
        removeComponentsFromParent(components);
    }

    private void removeComponents(LayoutComponent[] components, boolean fromModel) {
        Set<LayoutComponent> conts = new HashSet<>();
        for (LayoutComponent comp : components) {
            if (comp != null) {
                if (logTestCode() && fromModel) {
                    testCode.add("lm.removeComponent(\"" + comp.getId() + "\", true);"); // NOI18N
                }
                selectedComponents.remove(comp);
                if (comp.getParent() != null) {
                    conts.add(comp.getParent());
                }
                removeComponentIntervals(comp);
                layoutModel.removeComponent(comp, fromModel);
            }
        }
        for (LayoutComponent cont : conts) {
            for (int dim=0; dim < DIM_COUNT; dim++) {
                visualState.updateSpaceAfterRemove(cont.getDefaultLayoutRoot(dim), dim);
            }
        }
    }

    private void removeComponentsFromParent(LayoutComponent[] components) {
        for (LayoutComponent comp : components) {
            if (comp != null) {
                removeComponentIntervals(comp);
                layoutModel.removeComponent(comp, !comp.isLayoutContainer());
            }
        }
    }

    private void removeComponentIntervals(LayoutComponent comp) {
        if (comp.getParent() != null) {
            for (int dim=0; dim < DIM_COUNT; dim++) {
                LayoutInterval interval = comp.getLayoutInterval(dim);
                if (interval.getParent() != null) {
                    removeComponentInterval(interval, dim);
                }
            }
        }
    }

    private void removeComponentInterval(LayoutInterval interval, int dimension) {
        LayoutComponent comp = interval.getComponent();
        assert comp != null;
        LayoutInterval parent = interval.getParent();
        int index = layoutModel.removeInterval(interval);
        LayoutInterval root = LayoutInterval.getRoot(parent);
        intervalRemoved(parent, index, LayoutInterval.wantResize(interval), dimension);
        LayoutComponent container = comp.getParent();
        if (container != null && root.getSubIntervalCount() == 0) {
            // Empty root - eliminate the layer if appropriate.
            // Beware of #127988, #130186.
            // Default layer eliminated if the component goes away from
            // the container and there's just one additional layer.
            // Additional layer eliminated if the component goes away or
            // is moved within container (going to default layer).
            if (root == getActiveLayoutRoots(container)[dimension]) {
                // default layer empty
                if (container.getLayoutRootCount() == 2
                        && (dragger == null || dragger.getTargetContainer() != container)) {
                    layoutModel.removeLayoutRoots(container, root);
                } else { // no layer to be made default or component removed just temporarily
                    propEmptyContainer(root, dimension);
                }
            } else { // additional layer empty
                boolean eliminate;
                if (dragger == null) {
                    eliminate = true;
                } else {
                    LayoutInterval[] targetRoots = dragger.getTargetRoots();
                    eliminate = targetRoots == null
                                || (root != targetRoots[HORIZONTAL] && root != targetRoots[VERTICAL]);
                }
                if (eliminate) {
                    layoutModel.removeLayoutRoots(container, root);
                }
            } // (resized or enclosed components stay in their layer)
        }
    }

    // recursive
    private void intervalRemoved(LayoutInterval parent, int index, boolean wasResizing, int dimension) {
        Collection<LayoutInterval> unresized = null;
        if (parent.isSequential()) {
            LayoutInterval leadingGap;
            LayoutInterval leadingNeighbor;
            if (index > 0) {
                LayoutInterval li = parent.getSubInterval(index-1);
                if (li.isEmptySpace()) {
                    leadingGap = li;
                    layoutModel.removeInterval(li);
                    index--;
                    leadingNeighbor = index > 0 ? parent.getSubInterval(index-1) : null;
                }
                else {
                    leadingGap = null;
                    leadingNeighbor = li;
                }
            }
            else {
                leadingGap = null;
                leadingNeighbor = null;
            }

            LayoutInterval trailingGap;
            LayoutInterval trailingNeighbor;
            if (index < parent.getSubIntervalCount()) {
                LayoutInterval li = parent.getSubInterval(index);
                if (li.isEmptySpace()) {
                    trailingGap = li;
                    layoutModel.removeInterval(li);
                    trailingNeighbor = index < parent.getSubIntervalCount() ?
                                       parent.getSubInterval(index) : null;
                }
                else {
                    trailingGap = null;
                    trailingNeighbor = li;
                }
            }
            else {
                trailingGap = null;
                trailingNeighbor = null;
            }

            boolean gapsResizing = (leadingGap != null && LayoutInterval.canResize(leadingGap))
                                || (trailingGap != null && LayoutInterval.canResize(trailingGap));

            LayoutInterval superParent = parent.getParent();

            // [check for last interval (count==1), if parallel superParent try to re-add the interval]
            if (parent.getSubIntervalCount() == 0) { // nothing remained
                int idx = layoutModel.removeInterval(parent);
                intervalRemoved(superParent, idx, wasResizing || gapsResizing, dimension);
                return;
            } else { // the sequence remains
                boolean restoreResizing = gapsResizing || (wasResizing && !LayoutInterval.contentWantResize(parent));

                if ((leadingNeighbor != null && trailingNeighbor != null) // inside a sequence
                    || (leadingNeighbor != null && ((trailingGap != null && LayoutInterval.canResize(trailingGap))
                                                    || (!restoreResizing && LayoutInterval.getEffectiveAlignment(leadingNeighbor, TRAILING, true) == TRAILING)))
                    || (trailingNeighbor != null && ((leadingGap != null && LayoutInterval.canResize(leadingGap))
                                                    || (!restoreResizing && LayoutInterval.getEffectiveAlignment(trailingNeighbor, LEADING, true) == LEADING)))) {
                    // in the middle or at aligned side - create a placeholder gap (filling the original space)
                    int min, pref = Integer.MIN_VALUE, max;
                    if (!restoreResizing) {
                        min = max = USE_PREFERRED_SIZE;
                    } else {
                        min = (leadingNeighbor == null && leadingGap != null && leadingGap.getMinimumSize() == 0)
                               || (trailingNeighbor == null && trailingGap != null && trailingGap.getMinimumSize() == 0)
                           ? 0 : NOT_EXPLICITLY_DEFINED;
                        max = Short.MAX_VALUE;
                    }

                    if (pref == Integer.MIN_VALUE) {
                        pref = LayoutRegion.distance(
                                (leadingNeighbor != null ? leadingNeighbor : parent).getCurrentSpace(),
                                (trailingNeighbor != null ? trailingNeighbor : parent).getCurrentSpace(),
                                dimension,
                                leadingNeighbor != null ? TRAILING : LEADING,
                                trailingNeighbor != null ? LEADING : TRAILING);
                    }

                    LayoutInterval gap = new LayoutInterval(SINGLE);
                    gap.setSizes(min, pref, max);
                    gap.setAttribute(LayoutInterval.ATTR_FLEX_SIZEDEF);
                    layoutModel.addInterval(gap, parent, index);

                    if (trailingNeighbor != null && trailingNeighbor.isParallel() && trailingGap == null) {
                        operations.eliminateEndingGaps(trailingNeighbor, new LayoutInterval[] {gap,null}, dimension);
                    }
                    if (gap.getParent() == parent // i.e. not optimized for trailing neighbor already
                            && leadingNeighbor != null && leadingNeighbor.isParallel() && leadingGap == null) {
                        operations.eliminateEndingGaps(leadingNeighbor, new LayoutInterval[] {null,gap}, dimension);
                    }
                    if ((leadingNeighbor == null && leadingGap == null)
                            || (trailingNeighbor == null && trailingGap == null)) {
                        operations.optimizeGaps2(superParent, dimension);
                    }
                    destroyRedundantGroups(superParent);
                } else { // this is an "open" end - compensate the size in the parent
                    int resizingAlignment = -1;
                    if (restoreResizing) { // could affect effective alignment of the rest of the sequence
                        if (leadingNeighbor == null && parent.getAlignment() == LEADING) {
                            layoutModel.setIntervalAlignment(parent, TRAILING);
                            resizingAlignment = LEADING; // return the alignment if a compensating resizing gap is added later
                        } else if (trailingNeighbor == null && parent.getAlignment() == TRAILING) {
                            layoutModel.setIntervalAlignment(parent, LEADING);
                            resizingAlignment = TRAILING; // return the alignment if a compensating resizing gap is added later
                        }
                    }
                    // return back ending gap if suitable
                    if (leadingNeighbor != null && trailingGap != null) {
                        layoutModel.addInterval(trailingGap, parent, -1);
                    } else if (trailingNeighbor != null && leadingGap != null) {
                        layoutModel.addInterval(leadingGap, parent, 0);
                    }

                    LayoutInterval exclude;
                    if (parent.getSubIntervalCount() == 1) {
                        LayoutInterval last = layoutModel.removeInterval(parent, 0);
                        layoutModel.addInterval(last, superParent, layoutModel.removeInterval(parent));
                        layoutModel.setIntervalAlignment(last, parent.getRawAlignment());
                        exclude = last;
                    } else { // adjust current space of the parent sequence
                             // (border interval at the open end removed)
                        int l = (trailingNeighbor != null && leadingNeighbor == null ?
                            trailingNeighbor : parent).getCurrentSpace().positions[dimension][LEADING];
                        int t = (leadingNeighbor != null && trailingNeighbor == null ?
                            leadingNeighbor : parent).getCurrentSpace().positions[dimension][TRAILING];
                        parent.getCurrentSpace().set(dimension, l, t);
                        exclude = parent;
                    }
                    LayoutInterval adjusted = operations.maintainSize(superParent, restoreResizing,
                            dimension, exclude, exclude.getCurrentSpace().size(dimension), true);
                    if (adjusted != null) {
                        operations.optimizeGaps2(adjusted, dimension);
                    } else {
                        unresized = operations.eliminateRedundantSuppressedResizing(superParent, dimension);
                        if ((unresized == null || unresized.isEmpty())
                            && operations.completeGroupResizing(superParent, dimension)) {
                            // resizing gap added, may need gap optimization (e.g. ALT_Resizing04Test.doChanges2)
                            // which may also need to propagate whole way up (e.g. bug 203129)
                            LayoutInterval p = superParent;
                            while (p.getParent() != null) {
                                int idx = operations.optimizeGaps(p, dimension);
                                if (idx < 0) {
                                    break;
                                }
                                LayoutInterval seq = p.getParent();
                                if (seq.isSequential()
                                        && (idx < 1 || !seq.getSubInterval(0).isEmptySpace())
                                        && (idx+1 >= seq.getSubIntervalCount() || !seq.getSubInterval(idx+1).isEmptySpace())) {
                                    break;
                                }
                                p = LayoutInterval.getFirstParent(p, PARALLEL);
                            }
                        }
                    }

                    if (restoreResizing && resizingAlignment != -1) {
                        // return back original alignment if resizing was returned to the sequence
                        LayoutInterval parentResizingAgain = (resizingAlignment == LEADING ?
                                               trailingNeighbor : leadingNeighbor).getParent();
                        if (parentResizingAgain != null && parentResizingAgain.isSequential()
                                && LayoutInterval.wantResizeInLayout(parentResizingAgain)) {
                            layoutModel.setIntervalAlignment(parentResizingAgain, resizingAlignment);
                        }
                    }
                }

                if (restoreResizing && !LayoutInterval.canResize(superParent) && !LayoutInterval.contentWantResize(superParent)) {
                    operations.enableGroupResizing(superParent); // cancel suppressed resizing
                }
            }
        } else {
            if (parent.getParent() == null && parent.getSubIntervalCount() == 0) {
                return;
            }

            int groupAlign = parent.getGroupAlignment();
            if (groupAlign == LEADING || groupAlign == TRAILING) {
                unresized = operations.eliminateRedundantSuppressedResizing(parent, dimension);
                LayoutInterval adjusted = operations.maintainSize(parent, wasResizing, dimension, true);
                operations.optimizeGaps2(adjusted != null ? adjusted : parent, dimension);
            } else {
                preventParallelCollapse(parent, dimension);
            }

            if (parent.getParent() != null) {
                LayoutInterval superParent = LayoutInterval.getFirstParent(parent, PARALLEL);
                if (operations.dissolveRedundantGroup(parent)) { // [can it leave consecutive gaps?]
                    parent = superParent;
                }

                if (parent.getParent() != null && parent.getSubIntervalCount() > 1
                        && wasResizing && !LayoutInterval.contentWantResize(parent)) {
                    operations.enableGroupResizing(parent);
                }
            }
        }

        if (unresized != null && !unresized.isEmpty()) {
            if (unresizedOnRemove == null) {
                unresizedOnRemove = new Collection[2];
            }
            unresizedOnRemove[dimension] = unresized;
        }
    }

    private void eliminateConsecutiveGaps(LayoutInterval group, int dimension) {
        for (int index=0; index < group.getSubIntervalCount()-1; ) {
            if (!operations.mergeConsecutiveGaps(group, index, dimension)) {
                index++;
            }
        }
    }

    /**
     * Deals with a special situation when a large centered or baselined component
     * is deleted when there's something else in parallel yet not overlapping, but
     * may overlap with the other components from the group once the big component
     * disappears. In general this means some components are in parallel in both
     * dimensions, not overlapping only thanks to the size of the group, supported
     * by the big component. The solution is to place the components in a sequence
     * (which might be now possible when the big component is away).
     * This is just a workaround covering simple cases, mainly for old layouts.
     * We now try to avoid components that are too high to align on baseline at all.
     * @param group The group from which an interval has just been deleted.
     * @param dimension
     */
    private void preventParallelCollapse(LayoutInterval group, int dimension) {
        assert group.getGroupAlignment() == CENTER || group.getGroupAlignment() == BASELINE;

        // first determine if the group gets smaller with the component deleted
        LayoutRegion groupSpace = group.getCurrentSpace();
        LayoutRegion reducedSpace = new LayoutRegion();
        Iterator<LayoutInterval> it = group.getSubIntervals();
        while (it.hasNext()) {
            LayoutInterval li = it.next();
            reducedSpace.expand(li.getCurrentSpace(), dimension);
        }
        if (reducedSpace.size(dimension) >= groupSpace.size(dimension)) {
            return;
        }

        // collect intervals that are in parallel but should be in sequence with 'group'
        List<LayoutInterval> seqListL = null;
        List<LayoutInterval> seqListT = null;
        int minDistL = Integer.MAX_VALUE;
        int minDistT = Integer.MAX_VALUE;
        boolean found = false;

        LayoutInterval parent = group.getParent();
        while (parent != null) {
            if (parent.isParallel()) {
                if (parent.getGroupAlignment() == CENTER || parent.getGroupAlignment() == BASELINE) {
                    break;
                }
                it = parent.getSubIntervals();
                while (it.hasNext()) {
                    LayoutInterval li = it.next();
                    if (li != group && !li.isParentOf(group) && !li.isEmptySpace()) {
                        LayoutRegion neighborSpace = li.getCurrentSpace();
                        if (LayoutRegion.overlap(neighborSpace, groupSpace, dimension^1, 0)
                                && LayoutRegion.overlap(neighborSpace, groupSpace, dimension, 0)
                                && !LayoutRegion.overlap(neighborSpace, reducedSpace, dimension, 0)) {
                            // overlapped with original group but not with the reduced space without the removed interval
                            int dist = LayoutRegion.distance(neighborSpace, reducedSpace, dimension, TRAILING, LEADING);
                            if (dist >= 0) {
                                if (seqListL == null) {
                                    seqListL = new ArrayList<LayoutInterval>();
                                }
                                seqListL.add(li);
                                if (dist < minDistL) {
                                    minDistL = dist;
                                }
                            } else {
                                dist = LayoutRegion.distance(reducedSpace, neighborSpace, dimension, TRAILING, LEADING);
                                if (seqListT == null) {
                                    seqListT = new ArrayList<LayoutInterval>();
                                }
                                seqListT.add(li);
                                if (dist < minDistT) {
                                    minDistT = dist;
                                }
                            }
                        }
                    }
                }
                found = (seqListL != null && !seqListL.isEmpty()) || (seqListT != null && !seqListT.isEmpty());
                if (found) {
                    break;
                }
            }
            parent = parent.getParent();
        }

        if (found) {
            assert parent.isParallel();
            LayoutInterval seq = null;
            if (group.getParent().isParallel()) { // create new sequence
                parent = group.getParent();
                int index = layoutModel.removeInterval(group);
                seq = new LayoutInterval(SEQUENTIAL);
                seq.setAlignment(group.getAlignment());
                layoutModel.setIntervalAlignment(group, DEFAULT);
                layoutModel.addInterval(seq, parent, index);
                layoutModel.addInterval(group, seq, -1);
            } else { // try to reuse the sequence the group is in, the moved
                // intervals need to be parallelized with the rest of the sequence
                int indexInSeq = 0;
                LayoutInterval p = group;
                do {
                    LayoutInterval li = p;
                    p = p.getParent();
                    if (p.isSequential()) {
                        indexInSeq = p.indexOf(li);
                        seq = p;
                        break; // not clear which parent sequence to use,
                               // stopping here we use the lowest
                    }
                } while (p != parent);

                if (seqListL != null && !seqListL.isEmpty()) {
                    // extract the leading intervals, skip neighbor gap
                    LayoutInterval sub = null;
                    while (indexInSeq > 0) {
                        LayoutInterval li = layoutModel.removeInterval(seq, 0);
                        indexInSeq--;
                        if (!li.isEmptySpace() || indexInSeq > 0) {
                            if (sub == null) {
                                sub = new LayoutInterval(SEQUENTIAL);
                                seqListL.add(sub);
                            }
                            layoutModel.addInterval(li, sub, -1);
                        }
                    }
                }
                if (seqListT != null && !seqListT.isEmpty()) {
                    // exctract the trailing intervals, skip neighbor gap
                    LayoutInterval sub = null;
                    while (indexInSeq+1 < seq.getSubIntervalCount()) {
                        LayoutInterval li = layoutModel.removeInterval(seq, seq.getSubIntervalCount()-1);
                        if (!li.isEmptySpace() || indexInSeq+1 < seq.getSubIntervalCount()) {
                            if (sub == null) {
                                sub = new LayoutInterval(SEQUENTIAL);
                                seqListT.add(sub);
                            }
                            layoutModel.addInterval(li, sub, 0);
                        }
                    }
                }
            }

            LayoutInterval seqIntL = createIntervalFromList(seqListL, LEADING);
            if (seqIntL != null) {
                LayoutInterval gap = new LayoutInterval(SINGLE);
                gap.setSize(minDistL);
                layoutModel.addInterval(gap, seq, 0);
                layoutModel.setIntervalAlignment(seqIntL, DEFAULT);
                operations.addContent(seqIntL, seq, 0);
            }
            LayoutInterval seqIntT = createIntervalFromList(seqListT, TRAILING);
            if (seqIntT != null) {
                LayoutInterval gap = new LayoutInterval(SINGLE);
                gap.setSize(minDistT);
                layoutModel.addInterval(gap, seq, -1);
                layoutModel.setIntervalAlignment(seqIntT, DEFAULT);
                operations.addContent(seqIntT, seq, -1);
            }

            // merging sequencies may cause only one remains in parent
            if (parent.getSubIntervalCount() == 1 && parent.getParent() != null) { // last interval in parallel group
                // cancel the group and move the interval up
                LayoutInterval remaining = parent.getSubInterval(0);
                layoutModel.removeInterval(remaining);
                layoutModel.setIntervalAlignment(remaining, parent.getAlignment());
                LayoutInterval superParent = parent.getParent();
                int i = layoutModel.removeInterval(parent);
                operations.addContent(remaining, superParent, i, dimension);
            }
        } else { // try to compensate smaller group size in a neighbor gap
            for (int e=LEADING; e <= TRAILING; e++) {
                LayoutInterval gap = LayoutInterval.getNeighbor(group, e, false, true, false);
                if (gap != null && gap.isEmptySpace() && !LayoutInterval.isDefaultPadding(gap)) {
                    if (gap.getParent() == group.getParent()) { // direct gap
                        LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(gap, e, true);
                        int npos = (neighbor != null) ?
                            neighbor.getCurrentSpace().positions[dimension][e^1] :
                            LayoutInterval.getFirstParent(gap, PARALLEL).getCurrentSpace().positions[dimension][e];
                        int dist = reducedSpace.positions[dimension][e] - npos;
                        if (e == TRAILING) {
                            dist *= -1;
                        }
                        if (dist > LayoutInterval.getCurrentSize(gap, dimension)) {
                            operations.resizeInterval(gap, dist);
                        }
                    } else {
                        LayoutInterval superGroup = LayoutInterval.getDirectNeighbor(gap, e^1, true);
                        LayoutInterval li = group;
                        while (li.getParent() != superGroup) {
                            li = li.getParent();
                        }
                        int diff = LayoutRegion.distance(groupSpace, reducedSpace, dimension, e, e);
                        if (e == TRAILING) {
                            diff *= -1;
                        }
                        operations.maintainSize(superGroup, false, dimension, li,
                                li.getCurrentSpace().size(dimension) - diff, true);
                    }
                }
            }
        }
    }

    private LayoutInterval createIntervalFromList(List<LayoutInterval> seqList, int alignment) {
        LayoutInterval seqInt = null;
        if (seqList != null && !seqList.isEmpty()) {
            if (seqList.size() == 1) {
                LayoutInterval li = seqList.get(0);
                if (li.getParent() != null) {
                    layoutModel.removeInterval(li);
                }
                seqInt = li;
            } else {
                seqInt = new LayoutInterval(PARALLEL);
                seqInt.setGroupAlignment(alignment);
                for (LayoutInterval li : seqList) {
                    if (li.getParent() != null) {
                        layoutModel.removeInterval(li);
                    }
                    layoutModel.addInterval(li, seqInt, -1);
                }
            }
        }
        return seqInt;
    }

    private void setIntervalSize(LayoutInterval interval, int dimension, int min, int pref, int max) {
        if (logTestCode() && interval.isComponent()) {
            testCode.add("ld.setIntervalSize(lm.getLayoutComponent(\"" + interval.getComponent().getId() + "\").getLayoutInterval(" + dimension + "), "
                    + dimension + ", " + min + ", " + pref + ", " + max + ");"); // NOI18N
        }
        int oldSize = interval.getPreferredSize();
        if (layoutModel.setIntervalSize(interval, min, pref, max)) {
            intervalResized(interval, dimension, oldSize, pref);
        }
    }

    // called when explicit size is set to a component interval (not resized via dragger)
    private void intervalResized(LayoutInterval interval, int dimension, int oldSize, int newSize) {
        if (LayoutInterval.wantResizeInLayout(interval)) {
            if (newSize > 0 && newSize < LayoutInterval.getCurrentSize(interval, dimension)) {
                // Parallel intervals may block shrinking. Force smaller size by resetting
                // pref. size of all parallel resizing intervals. (If the new set size
                // is default or 0, we don't force shrinking.)
                enableShrinking(interval);
            }
            if ((oldSize == 0 || oldSize == NOT_EXPLICITLY_DEFINED) && newSize > 0
                    && interval.hasAttribute(LayoutInterval.ATTR_SIZE_DIFF)) {
                // Sequential intervals may shrink undesirably when setting explicit size
                // to a resizing interval that did not have the size defined so far.
                // There might be other such resizing intervals in sequence, they also
                // need explicit size set now.
                for (int a=LEADING; a <= TRAILING; a++) {
                    LayoutInterval li = LayoutInterval.getNeighbor(interval, a, false, true, false);
                    while (li != null) {
                        if (li.hasAttribute(LayoutInterval.ATTR_SIZE_DIFF) && LayoutInterval.wantResize(li)) {
                            if (li.isGroup()) {
                                visualState.updateToActualSize(li, dimension, 2);
                            } else {
                                int currSize = LayoutInterval.getCurrentSize(li, dimension);
                                if (currSize > 0) {
                                    operations.resizeInterval(li, currSize);
                                    layoutModel.changeIntervalAttribute(li, LayoutInterval.ATTR_FLEX_SIZEDEF, false);
                                }
                            }
                        }
                        li = LayoutInterval.getNeighbor(li, a, false, true, false);
                    }
                }
            }
        } else {
            if (shouldAbsorbExplicitSizeChange(interval)) {
                return;
            }
        }
        preferredSizeChanged = true; // let the designer adjust its preferred size
    }

    static boolean shouldAbsorbExplicitSizeChange(LayoutInterval changing) {
        assert changing.isSingle();
        if (LayoutInterval.canResize(changing)) {
            return false;
        }
        List<LayoutInterval> list = new LinkedList<LayoutInterval>();
        LayoutInterval parent = changing.getParent();
        LayoutInterval prev = null;
        while (parent != null) {
            if (parent.isSequential()) {
                list.add(parent);
                do {
                    LayoutInterval li = list.remove(0);
                    if (LayoutInterval.canResize(li)) {
                        if (li.isGroup() && (prev == null || !li.isParentOf(prev))) {
                            for (Iterator<LayoutInterval> subIt=li.getSubIntervals(); subIt.hasNext(); ) {
                                list.add(subIt.next());
                            }
                        } else if (li != changing && li.isSingle()
                                && ((li.getPreferredSize() != LayoutInterval.getDefaultSizeDef(li)
                                     && li.isEmptySpace())
                                    || li.getDiffToDefaultSize() > 0)) {
                            return true; // absorbing gap
                        }
                    }
                } while (!list.isEmpty());
            }
            prev = parent;
            parent = parent.getParent();
        }
        return false;
    }

    /**
     * Enables shrinking of given interval in the designed container by
     * resetting pref. size of parallel neighbors to 0.
     */
    private void enableShrinking(LayoutInterval interval) {
        LayoutInterval parent = LayoutInterval.getFirstParent(interval, PARALLEL);
        while (parent != null) {
            Iterator it = parent.getSubIntervals();
            while (it.hasNext()) {
                LayoutInterval li = (LayoutInterval) it.next();
                if (li != interval && !li.isParentOf(interval)) {
                    resetResizingIntervals(li);
                }
            }
            interval = parent;
            parent = LayoutInterval.getFirstParent(interval, PARALLEL);
        }
    }

    private void resetResizingIntervals(LayoutInterval interval) {
        if (interval.isGroup()) {
            if (LayoutInterval.canResize(interval)) {
                Iterator<LayoutInterval> it = interval.getSubIntervals();
                while (it.hasNext()) {
                    resetResizingIntervals(it.next());
                }
            }
        } else if (LayoutInterval.wantResize(interval)) {
            if (interval.isEmptySpace()) {
                operations.resizeInterval(interval, LayoutInterval.getDefaultSizeDef(interval));
            } else { // component to shrink (it will re-grow as needed)
                int minSize = interval.getMinimumSize();
                int prefSize = interval.getPreferredSize();
                int accommodatingSize = minSize < 0 ? 0 : minSize;
                operations.resizeInterval(interval, accommodatingSize);
                if (prefSize != 0 && accommodatingSize == 0) {
                    layoutModel.changeIntervalAttribute(interval, LayoutInterval.ATTR_FLEX_SIZEDEF, true);
                }
            } // otherwise would not shrink anyway, keep default size
        }
    }

    // -----
    // auxiliary fields holding temporary objects used frequently

    // converted cursor position used during moving/resizing
    private int[] cursorPos = { 0, 0 };

    // -----
    // test generation support

    static final String TEST_SWITCH = "netbeans.form.layout_test"; // NOI18N

    /* stores test code lines */
    public List<String> testCode = new ArrayList<String>();

    // these below are used for removing unwanted move entries, otherwise the code can exceed 10000 lines in a few seconds of form editor work ;O)
    private List<String> testCode0 = new ArrayList<String>();
    private List<String> beforeMove = new ArrayList<String>();
    private List<String> move1 = new ArrayList<String>();
    private List<String> move2 = new ArrayList<String>();
    private boolean isMoving = false;
    
    private int modelCounter = -1;
    
    private Point lastMovePoint = new Point(0, 0);

    public int getModelCounter() {
        return modelCounter;
    }

    public void setModelCounter(int modelCounter) {
        this.modelCounter = modelCounter;
    }

    public static boolean testMode() {
        return Boolean.getBoolean(TEST_SWITCH);
    }

    public boolean logTestCode() {
        return modelCounter > -1 && Boolean.getBoolean(TEST_SWITCH);
    }
}
