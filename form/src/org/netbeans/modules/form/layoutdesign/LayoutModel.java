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
import javax.swing.undo.*;


/**
 * This class manages layout data of a form. Specifically it:
 * - provides entry points for exploring the layout,
 * - allows to add/remove layout intervals and components,
 * - allows to listen on changes,
 * - manages an undo/redo queue for the layout, provides undo/redo marks,
 *   and allows to perform undo/redo to given mark.
 *
 * @author Tomas Pavek, Jan Stola
 */

public class LayoutModel implements LayoutConstants {

    // map String component Id -> LayoutComponent instance
    private Map<String,LayoutComponent> idToComponents = new HashMap<String,LayoutComponent>();

    // list of listeners registered on LayoutModel
    private ArrayList<Listener> listeners;

    // handler that takes care of complex removal of components, with additional
    // adjustments in the layout
    private RemoveHandler removeHandler;

    // handler that takes care of setting explicit size to components or gaps,
    // with additional adjustments in the layout
    private ResizeHandler resizeHandler;

    // layout changes recording and undo/redo
    private boolean recordingChanges = true;
    private boolean undoRedoInProgress;
    private int changeMark;
    private int oldestMark;
    private int changeCountHardLimit = 10000;
    private Map<Integer,LayoutEvent> undoMap = new HashMap<Integer,LayoutEvent>(500);
    private Map<Integer,LayoutEvent> redoMap = new HashMap<Integer,LayoutEvent>(100);
    private LayoutUndoableEdit lastUndoableEdit;

    // remembers whether the model was corrected/upgraded during loading
    private boolean corrected;

    // -----

    /**
     * Basic mapping method. Returns LayoutComponent for given Id.
     * @return LayoutComponent of given Id, null if there is no such component
     *         registered in the model
     */
    public LayoutComponent getLayoutComponent(String compId) {
        return idToComponents.get(compId);
    }

    public void addRootComponent(LayoutComponent comp) {
        addComponent(comp, null, -1);
    }

    /**
     * Removes component from the component hierarchy, its intervals from the
     * layout structure, and if fromModel is true then also from the LayoutModel
     * registry.
     */
    public void removeComponent(String compId, boolean fromModel) {
        LayoutComponent comp = getLayoutComponent(compId);
        if (comp != null) {
            if (removeHandler != null) {
                removeHandler.removeComponents(new LayoutComponent[] { comp }, fromModel);
            } else {
                removeComponentAndIntervals(comp, true);
            }
        }
    }

    /**
     * @return false if the component does not exist in the layout model
     */
    public boolean changeComponentToContainer(String componentId) {
        LayoutComponent component = getLayoutComponent(componentId);
        if (component != null) {
            setLayoutContainer(component, true);
            return true;
        }
        return false;
    }

    /**
     * Changes a container to a component (that cannot contain sub-components).
     * All its current sub-components are removed. Those not being containers
     * are also removed from the model - containers remain in model.
     * @return false if the component does not exist in the layout model
     */
    public boolean changeContainerToComponent(String componentId) {
        LayoutComponent component = getLayoutComponent(componentId);
        if (component == null) {
            return false;
        }
        // remove subcomponents
        for (int i=component.getSubComponentCount()-1; i>=0; i--) {
            LayoutComponent sub = component.getSubComponent(i);
            if (sub.isLayoutContainer()) {
                // subcontainer stays in layout model, but need to remove its
                // layout interval from parent layout (now invalid)
                removeComponentAndIntervals(sub, false);
            } else {
                // remove from layout model, don't have to remove from layout
                // (thrown away as a whole)
                removeComponent(sub, true);
            }
        }
        // cancel container (also disconnects its layout)
        setLayoutContainer(component, false);
        // container changed to component can stay in model only if it has some parent
        if (component.getParent() == null) {
            removeComponent(component, true);
        }
        return true;
    }

    // -----

    void registerComponent(LayoutComponent comp, boolean recursive) {
        registerComponentImpl(comp);
        if (recursive && comp.isLayoutContainer()) {
            for (LayoutComponent subComp : comp.getSubcomponents()) {
                registerComponent(subComp, recursive);
            }
        }
    }
    
    void registerComponentImpl(LayoutComponent comp) {
        LayoutComponent lc = idToComponents.put(comp.getId(), comp);

        if (lc != comp) {
            // record undo/redo and fire event
            LayoutEvent.Component ev = new LayoutEvent.Component(this, LayoutEvent.COMPONENT_REGISTERED);
            ev.setComponent(comp);
            addChange(ev);
            fireEvent(ev);
        } // else noop => don't need change event
    }

    void unregisterComponent(LayoutComponent comp, boolean recursive) {
        if (recursive && comp.isLayoutContainer()) {
            for (LayoutComponent subComp : comp.getSubcomponents()) {
                unregisterComponent(subComp, recursive);
            }
        }
        removeComponentFromLinkSizedGroup(comp, HORIZONTAL);
        removeComponentFromLinkSizedGroup(comp, VERTICAL);
        unregisterComponentImpl(comp);
    }

    void unregisterComponentImpl(LayoutComponent comp) {
        LayoutComponent lc = idToComponents.remove(comp.getId());

        if (lc != null) {
            // record undo/redo and fire event
            LayoutEvent.Component ev = new LayoutEvent.Component(this, LayoutEvent.COMPONENT_UNREGISTERED);
            ev.setComponent(comp);
            addChange(ev);
            fireEvent(ev);
        } // else noop => don't need change event
    }

    void changeComponentId(LayoutComponent comp, String newId) {
        unregisterComponentImpl(comp);
        comp.setId(newId);
        registerComponentImpl(comp);
    }

    void replaceComponent(LayoutComponent comp, LayoutComponent substComp) {
        assert substComp.getParent() == null;
        for (int i=0; i<DIM_COUNT; i++) {
            LayoutInterval interval = comp.getLayoutInterval(i);
            LayoutInterval substInt = substComp.getLayoutInterval(i);
            assert substInt.getParent() == null;
            setIntervalAlignment(substInt, interval.getRawAlignment());
            setIntervalSize(substInt, interval.getMinimumSize(),
                    interval.getPreferredSize(), interval.getMaximumSize());
            LayoutInterval parentInt = interval.getParent();
            if (parentInt != null) {
                int index = removeInterval(interval);
                addInterval(substInt, parentInt, index);
            }
        }

        LayoutComponent parent = comp.getParent();
        if (parent != null) {
            int index = removeComponentImpl(comp);
            addComponentImpl(substComp, parent, index);
        }
        unregisterComponentImpl(comp);
        registerComponentImpl(substComp);
    }

    Iterator getAllComponents() {
        return idToComponents.values().iterator();
    }

    Collection<LayoutComponent> getTopContainers() {
        List<LayoutComponent> containers = new LinkedList<LayoutComponent>();
        for (LayoutComponent comp : idToComponents.values()) {
            if (comp.isLayoutContainer() && comp.getParent() == null) {
                containers.add(comp);
            }
        }
        return containers;
    }

    // Note this method does not care about adding the layout intervals of the
    // component, it must be done in advance.
    void addComponent(LayoutComponent component, LayoutComponent parent, int index) {
        addComponentImpl(component, parent, index);
        registerComponent(component, true);
    }

    void addComponentImpl(LayoutComponent component, LayoutComponent parent, int index) {
        assert component.getParent() == null;

        if (parent != null) {
            assert getLayoutComponent(parent.getId()) == parent;
            index = parent.addComponent(component, index);
        }
        else {
            assert component.isLayoutContainer();
        }

        // record undo/redo and fire event
        LayoutEvent.Component ev = new LayoutEvent.Component(this, LayoutEvent.COMPONENT_ADDED);
        ev.setComponent(component, parent, index);
        addChange(ev);
        fireEvent(ev);
    }

    // Low level removal - removes the component from parent, unregisters it,
    // records the change for undo/redo, and fires an event. Does nothing to
    // the layout intervals of the component.
    void removeComponent(LayoutComponent component, boolean fromModel) {
        removeComponentImpl(component);
        if (fromModel && (getLayoutComponent(component.getId()) != null)) {
            unregisterComponent(component, true);
        }
    }

    int removeComponentImpl(LayoutComponent component) {
        int index;
        LayoutComponent parent = component.getParent();
        if (parent != null) {
            index = parent.removeComponent(component);
        } else {
            return -1; // the removal operation is "noop"
        }

        // record undo/redo and fire event
        LayoutEvent.Component ev = new LayoutEvent.Component(this, LayoutEvent.COMPONENT_REMOVED);
        ev.setComponent(component, parent, index);
        addChange(ev);
        fireEvent(ev);

        return index;
    }

    void removeComponentAndIntervals(LayoutComponent comp, boolean fromModel) {
        if (comp.getParent() != null) {
            for (int i=0; i < DIM_COUNT; i++) {
                LayoutInterval interval = comp.getLayoutInterval(i);
                if (interval.getParent() != null) {
                    removeInterval(interval);
                }
            }
        }
        removeComponent(comp, fromModel);
    }

    LayoutInterval[] addNewLayoutRoots(LayoutComponent container) {
        LayoutInterval[] newRoots = container.addNewLayoutRoots();

        // record undo/redo (don't fire event)
        LayoutEvent.Component ev = new LayoutEvent.Component(this, LayoutEvent.LAYOUT_ROOTS_ADDED);
        ev.setLayoutRoots(container, newRoots, -1);
        addChange(ev);

        return newRoots;
    }

    LayoutInterval[] removeLayoutRoots(LayoutComponent container, LayoutInterval oneRoot) {
        LayoutInterval[] roots = container.getLayoutRoots(oneRoot);
        if (roots != null) {
            int index = container.removeLayoutRoots(roots);

            // record undo/redo (don't fire event)
            LayoutEvent.Component ev = new LayoutEvent.Component(this, LayoutEvent.LAYOUT_ROOTS_REMOVED);
            ev.setLayoutRoots(container, roots, index);
            addChange(ev);
        }
        return roots;
    }

    void addInterval(LayoutInterval interval, LayoutInterval parent, int index) {
        assert interval.getParent() == null;

        index = parent.add(interval, index);

        // record undo/redo and fire event
        LayoutEvent.Interval ev = new LayoutEvent.Interval(this, LayoutEvent.INTERVAL_ADDED);
        ev.setInterval(interval, parent, index);
        addChange(ev);
        fireEvent(ev);
    }

    // Low level removal - removes the interval from parent, records the
    // change for undo/redo, and fires an event.
    int removeInterval(LayoutInterval interval) {
        LayoutInterval parent = interval.getParent();
        int index = parent.remove(interval);

        // record undo/redo and fire event
        LayoutEvent.Interval ev = new LayoutEvent.Interval(this, LayoutEvent.INTERVAL_REMOVED);
        ev.setInterval(interval, parent, index);
        addChange(ev);
        fireEvent(ev);

        return index;
    }

    LayoutInterval removeInterval(LayoutInterval parent, int index) {
        LayoutInterval interval = parent.remove(index);

        // record undo/redo and fire event
        LayoutEvent.Interval ev = new LayoutEvent.Interval(this, LayoutEvent.INTERVAL_REMOVED);
        ev.setInterval(interval, parent, index);
        addChange(ev);
        fireEvent(ev);

        return interval;
    }
    
    void changeIntervalAttribute(LayoutInterval interval, int attribute, boolean set) {
        int oldAttributes = interval.getAttributes();
        if (set) {
            interval.setAttribute(attribute);
        } else {
            interval.unsetAttribute(attribute);
        }
        int newAttributes = interval.getAttributes();
        if (newAttributes != oldAttributes) {
            // record undo/redo (don't fire event)
            LayoutEvent.Interval ev = new LayoutEvent.Interval(this, LayoutEvent.INTERVAL_ATTRIBUTES_CHANGED);
            ev.setAttributes(interval, oldAttributes, newAttributes);
            addChange(ev);
        }
    }

    void setIntervalAlignment(LayoutInterval interval, int alignment) {
        int oldAlignment = interval.getRawAlignment();
        interval.setAlignment(alignment);

        // record undo/redo (don't fire event)
        LayoutEvent.Interval ev = new LayoutEvent.Interval(this, LayoutEvent.INTERVAL_ALIGNMENT_CHANGED);
        ev.setAlignment(interval, oldAlignment, alignment);
        addChange(ev);
    }

    void setGroupAlignment(LayoutInterval group, int alignment) {
        int oldAlignment = group.getGroupAlignment();
        if (alignment == oldAlignment) {
            return;
        }
        group.setGroupAlignment(alignment);

        // record undo/redo (don't fire event)
        LayoutEvent.Interval ev = new LayoutEvent.Interval(this, LayoutEvent.GROUP_ALIGNMENT_CHANGED);
        ev.setAlignment(group, oldAlignment, alignment);
        addChange(ev);
    }

    void setLayoutContainer(LayoutComponent component, boolean container) {
        boolean oldContainer = component.isLayoutContainer();
        if (oldContainer != container) {
            List<LayoutInterval[]> roots = oldContainer ? component.getLayoutRoots() : null;
            component.setLayoutContainer(container, null);
            if (container) {
                roots = component.getLayoutRoots();
            }

            // record undo/redo (don't fire event)
            LayoutEvent.Component ev = new LayoutEvent.Component(this, LayoutEvent.CONTAINER_ATTR_CHANGED);
            ev.setContainer(component, roots);
            addChange(ev);            
        }
    }

    public void setUserIntervalSize(LayoutInterval interval, int dimension, int size) {
        int min = interval.getMinimumSize();
        int pref = interval.getPreferredSize();
        int max = interval.getMaximumSize();
        if (min == pref && max == USE_PREFERRED_SIZE && pref != size) {
            min = USE_PREFERRED_SIZE;
        }
        if (resizeHandler != null) {
            resizeHandler.setIntervalSize(interval, dimension, min, size, max);
        } else {
            setIntervalSize(interval, min, size, max);
        }
        changeIntervalAttribute(interval, LayoutInterval.ATTR_FLEX_SIZEDEF, false);
    }

    public void setUserIntervalSize(LayoutInterval interval, int dimension, int size, boolean resizing) {
        int min, max;
        if (size == NOT_EXPLICITLY_DEFINED && interval.isComponent()) {
            size = LayoutInterval.getDefaultSizeDef(interval);
        }
        boolean sizeChange = size != interval.getPreferredSize();
        if (resizing) {
            if (!interval.isEmptySpace()) {
                min = NOT_EXPLICITLY_DEFINED;
            } else {
                min = (size == 0 || (size != NOT_EXPLICITLY_DEFINED && interval.getMinimumSize() == 0))
                        ? 0 : NOT_EXPLICITLY_DEFINED;
            }
            max = Short.MAX_VALUE;
        } else {
            min = (size == 0 || size == NOT_EXPLICITLY_DEFINED) ? size : USE_PREFERRED_SIZE;
            max = USE_PREFERRED_SIZE;
        }
        if (resizeHandler != null) {
            resizeHandler.setIntervalSize(interval, dimension, min, size, max);
        } else {
            setIntervalSize(interval, min, size, max);
        }

        if (sizeChange) {
            changeIntervalAttribute(interval, LayoutInterval.ATTR_FLEX_SIZEDEF, false);
        }
    }

    public boolean setIntervalSize(LayoutInterval interval, int min, int pref, int max) {
        int oldMin = interval.getMinimumSize();
        int oldPref = interval.getPreferredSize();
        int oldMax = interval.getMaximumSize();
        if (min == oldMin && pref == oldPref && max == oldMax) {
            return false; // no change
        }
        interval.setSizes(min, pref, max);
        if (interval.isComponent()) {
            LayoutComponent comp = interval.getComponent();
            boolean horizontal = (interval == comp.getLayoutInterval(HORIZONTAL));
            if (oldMin != min) {
                comp.firePropertyChange(horizontal ? PROP_HORIZONTAL_MIN_SIZE : PROP_VERTICAL_MIN_SIZE,
                    new Integer(oldMin), new Integer(min));
            }
            if (oldPref != pref) {
                comp.firePropertyChange(horizontal ? PROP_HORIZONTAL_PREF_SIZE : PROP_VERTICAL_PREF_SIZE,
                    new Integer(oldPref), new Integer(pref));
            }
            if (oldMax != max) {
                comp.firePropertyChange(horizontal ? PROP_HORIZONTAL_MAX_SIZE : PROP_VERTICAL_MAX_SIZE,
                    new Integer(oldMax), new Integer(max));
            }
        }

        // record undo/redo and fire event
        LayoutEvent.Interval ev = new LayoutEvent.Interval(this, LayoutEvent.INTERVAL_SIZE_CHANGED);
        ev.setSize(interval, oldMin, oldPref, oldMax, min, pref, max);
        addChange(ev);
        fireEvent(ev);
        return true;
    }

    public void setPaddingType(LayoutInterval interval, PaddingType paddingType) {
        PaddingType oldPadding = interval.getPaddingType();
        if (oldPadding != paddingType) {
            interval.setPaddingType(paddingType);

            // record undo/redo (don't fire event)
            LayoutEvent.Interval ev = new LayoutEvent.Interval(this, LayoutEvent.INTERVAL_PADDING_TYPE_CHANGED);
            ev.setPaddingType(interval, oldPadding, paddingType);
            addChange(ev);
        }
    }

    /**
     * Does a non-recursive copy of components and layout of given source
     * container. Assuming the target container is empty (or does not exist yet).
     * LayoutComponent instances are created automatically as needed, using the
     * provided IDs.
     * @param sourceModel the source LayoutModel
     * @param sourceContainerId ID of the source container
     * @param sourceToTargetId mapping between the original and the copied
     *        components' IDs
     * @param targetContainerId ID of the target container
     */
    public void copyContainerLayout(LayoutModel sourceModel, String sourceContainerId,
                Map<String, String> sourceToTargetId, String targetContainerId) {
        LayoutComponent sourceContainer = sourceModel.getLayoutComponent(sourceContainerId);
        LayoutComponent targetContainer = getLayoutComponent(targetContainerId);
        if (targetContainer == null) {
            targetContainer = new LayoutComponent(targetContainerId, true);
            addRootComponent(targetContainer);
        } else if (!targetContainer.isLayoutContainer()) {
            changeComponentToContainer(targetContainerId);
        }
        copyContainerLayout(sourceContainer, sourceToTargetId, targetContainer);
    }

    void copyContainerLayout(LayoutComponent sourceContainer,
            Map<String, String> sourceToTargetId, LayoutComponent targetContainer) {
        // Create LayoutComponents
        for (LayoutComponent sourceComp : sourceContainer.getSubcomponents()) {
            String targetId = sourceToTargetId.get(sourceComp.getId());
            LayoutComponent targetComp = getLayoutComponent(targetId);
            if (targetComp == null) {
                targetComp = new LayoutComponent(targetId, sourceComp.isLayoutContainer());
            }
            if (targetComp.getParent() == null) {
                addComponent(targetComp, targetContainer, -1);
            }
        }
        // Copy LayoutIntervals
        int i = 0;
        for (LayoutInterval[] sourceRoots : sourceContainer.getLayoutRoots()) {
            LayoutInterval[] targetRoots;
            if (i == targetContainer.getLayoutRootCount()) {
                targetRoots = addNewLayoutRoots(targetContainer);
            } else { // make sure it's clean
                targetRoots = targetContainer.getLayoutRoots().get(i);
                for (int dim=0; dim<DIM_COUNT; dim++) {
                    for (int n=targetRoots[dim].getSubIntervalCount(); n > 0; n--) {
                        removeInterval(targetRoots[dim], n-1);
                    }
                }
            }
            for (int dim=0; dim<DIM_COUNT; dim++) {
                copySubIntervals(sourceRoots[dim], targetRoots[dim], sourceToTargetId);
            }
            i++;
        }
    }

    private void copySubIntervals(LayoutInterval sourceInterval, LayoutInterval targetInterval, Map/*<String,String>*/ sourceToTargetIds) {
        Iterator iter = sourceInterval.getSubIntervals();
        while (iter.hasNext()) {
            LayoutInterval sourceSub = (LayoutInterval)iter.next();
            LayoutInterval clone = null;
            if (sourceSub.isComponent()) {
                String compId = (String)sourceToTargetIds.get(sourceSub.getComponent().getId());
                LayoutComponent comp = getLayoutComponent(compId);
                int dimension = (sourceSub == sourceSub.getComponent().getLayoutInterval(HORIZONTAL)) ? HORIZONTAL : VERTICAL;
                clone = comp.getLayoutInterval(dimension);
            }
            LayoutInterval targetSub = LayoutInterval.cloneInterval(sourceSub, clone);
            if (sourceSub.isGroup()) {
                copySubIntervals(sourceSub, targetSub, sourceToTargetIds);
            }
            addInterval(targetSub, targetInterval, -1);
        }
    }

    // assuming target container is empty
    void moveContainerLayout(LayoutComponent sourceContainer, LayoutComponent targetContainer) {
        if (!sourceContainer.isLayoutContainer() || !targetContainer.isLayoutContainer()
                || targetContainer.getSubComponentCount() > 0) {
            throw new IllegalArgumentException();
        }

        while (sourceContainer.getSubComponentCount() > 0) {
            LayoutComponent sub = sourceContainer.getSubComponent(0);
            removeComponent(sub, false);
            addComponent(sub, targetContainer, -1);
        }

        List<LayoutInterval[]> transferRoots = sourceContainer.getLayoutRoots();
        List<LayoutInterval[]> originalTargetRoots = targetContainer.getLayoutRoots();

        sourceContainer.setLayoutRoots(null); // clear
        LayoutEvent.Component ev = new LayoutEvent.Component(this, LayoutEvent.LAYOUT_ROOTS_CHANGED);
        ev.setLayoutRoots(sourceContainer, transferRoots, sourceContainer.getLayoutRoots());
        addChange(ev);

        targetContainer.setLayoutRoots(transferRoots);
        ev = new LayoutEvent.Component(this, LayoutEvent.LAYOUT_ROOTS_CHANGED);
        ev.setLayoutRoots(targetContainer, originalTargetRoots, transferRoots);
        addChange(ev);
    }

    LayoutInterval[] createIntervalsFromBounds(Map<LayoutComponent, Rectangle> compToBounds) {
        RegionInfo region = new RegionInfo(compToBounds);
        region.calculateIntervals();
        LayoutInterval[] result = new LayoutInterval[DIM_COUNT];
        for (int dim=0; dim<DIM_COUNT; dim++) {
            result[dim] = region.getInterval(dim);
        }
        return result;
    }

    private class RegionInfo {
        private LayoutInterval horizontal = null;
        private LayoutInterval vertical = null;
        private Map<LayoutComponent, Rectangle> compToBounds;
        private int minx;
        private int maxx;
        private int miny;
        private int maxy;
        private int dimension;

        public RegionInfo(Map<LayoutComponent, Rectangle> compToBounds) {
            this.compToBounds = compToBounds;
            this.dimension = -1;
            minx = miny = 0;
            updateRegionBounds();
        }

        private RegionInfo(Map<LayoutComponent, Rectangle> compToBounds, int dimension) {
            this.compToBounds = compToBounds;
            this.dimension = dimension;
            minx = miny = Short.MAX_VALUE;
            updateRegionBounds();
        }

        private void updateRegionBounds() {
            maxy = maxx = Short.MIN_VALUE;
            for (Rectangle bounds : compToBounds.values()) {
                minx = Math.min(minx, bounds.x);
                miny = Math.min(miny, bounds.y);
                maxx = Math.max(maxx, bounds.x + bounds.width);
                maxy = Math.max(maxy, bounds.y + bounds.height);
            }
        }

        public void calculateIntervals() {
            if (compToBounds.size() == 1) {
                Map.Entry<LayoutComponent, Rectangle> e = compToBounds.entrySet().iterator().next();
                LayoutComponent comp = e.getKey();
                Rectangle bounds = e.getValue();
                horizontal = comp.getLayoutInterval(HORIZONTAL);
                horizontal = prefixByGap(horizontal, bounds.x - minx);
                vertical = comp.getLayoutInterval(VERTICAL);
                vertical = prefixByGap(vertical, bounds.y - miny);
                return;
            }
            int effDim = -1;
            List<Map<LayoutComponent, Rectangle>> parts = null;
            Map<LayoutComponent, Rectangle> removedCompToBounds = null;
            do {                
                boolean remove = ((dimension == -1) && (effDim == HORIZONTAL))
                    || ((dimension != -1) && (effDim != -1));
                if (remove) {
                    effDim = -1;
                }
                if (dimension == -1) {
                    switch (effDim) {
                        case -1: effDim = VERTICAL; break;
                        case VERTICAL: effDim = HORIZONTAL; break;
                        case HORIZONTAL: remove = true;
                    }
                } else {
                    effDim = dimension;
                }
                if (remove) { // no cut found, remove some component
                    Map.Entry<LayoutComponent, Rectangle> e = compToBounds.entrySet().iterator().next();
                    LayoutComponent comp = e.getKey();
                    Rectangle bounds = e.getValue();
                    if (removedCompToBounds == null) {
                        removedCompToBounds = new HashMap<LayoutComponent, Rectangle>();
                    }
                    removedCompToBounds.put(comp, bounds);
                    compToBounds.remove(comp);
                }
                Set<Integer> cutSet = createPossibleCuts(effDim);
                parts = cutIntoParts(cutSet, effDim);            
            } while (!compToBounds.isEmpty() && parts.isEmpty());
            dimension = effDim;
            List<RegionInfo> regions = new LinkedList<RegionInfo>();
            Iterator<Map<LayoutComponent, Rectangle>> iter = parts.iterator();
            while (iter.hasNext()) {
                Map<LayoutComponent, Rectangle> part = iter.next();
                RegionInfo region = new RegionInfo(part, (dimension == HORIZONTAL) ? VERTICAL : HORIZONTAL);
                region.calculateIntervals();
                regions.add(region);
            }
            mergeSubRegions(regions, dimension);
            if (removedCompToBounds != null) {
                for (int dim = HORIZONTAL; dim <= VERTICAL; dim++) {
                    LayoutInterval parent = (dim == HORIZONTAL) ? horizontal : vertical;
                    if (!parent.isParallel()) {
                        LayoutInterval parGroup = new LayoutInterval(PARALLEL);
                        add(parent, parGroup);
                        if (dim == HORIZONTAL) {
                            horizontal = parGroup;
                        } else {
                            vertical = parGroup;
                        }
                        parent = parGroup;
                    }
                    for (Map.Entry<LayoutComponent, Rectangle> entry : removedCompToBounds.entrySet()) {
                        LayoutComponent comp = entry.getKey();
                        Rectangle bounds = entry.getValue();
                        LayoutInterval interval = comp.getLayoutInterval(dim);
                        int gap = (dim == HORIZONTAL) ? bounds.x - minx : bounds.y - miny;
                        interval = prefixByGap(interval, gap);
                        add(interval, parent);
                    }
                }
            }
        }
        
        private SortedSet<Integer> createPossibleCuts(int dimension) {
            SortedSet<Integer> cutSet = new TreeSet<Integer>();
            for (Rectangle bounds : compToBounds.values()) {
                // Leading lines are sufficient
                int leading = (dimension == HORIZONTAL) ? bounds.x : bounds.y;
                cutSet.add(new Integer(leading));
            }
            cutSet.add(new Integer((dimension == HORIZONTAL) ? maxx : maxy));
            return cutSet;
        }
        
        private List<Map<LayoutComponent,Rectangle>> cutIntoParts(Set<Integer> cutSet, int dimension) {
            List<Map<LayoutComponent,Rectangle>> parts = new LinkedList<Map<LayoutComponent,Rectangle>>();
            Iterator<Integer> iter = cutSet.iterator();
            while (iter.hasNext()) {
                Integer cutInt = iter.next();
                int cut = cutInt.intValue();
                boolean isCut = true;
                Map<LayoutComponent, Rectangle> preCompToBounds = new HashMap<LayoutComponent, Rectangle>();
                Map<LayoutComponent, Rectangle> postCompToBounds = new HashMap<LayoutComponent, Rectangle>();
                Iterator<Map.Entry<LayoutComponent, Rectangle>> it = compToBounds.entrySet().iterator();                
                while (isCut && it.hasNext()) {
                    Map.Entry<LayoutComponent, Rectangle> entry = it.next();
                    LayoutComponent comp = entry.getKey();
                    Rectangle bounds = entry.getValue();
                    int leading = (dimension == HORIZONTAL) ? bounds.x : bounds.y;
                    int trailing = leading + ((dimension == HORIZONTAL) ? bounds.width : bounds.height);
                    if (leading >= cut) {
                        postCompToBounds.put(comp, bounds);
                    } else if (trailing <= cut) {
                        preCompToBounds.put(comp, bounds);
                    } else {
                        isCut = false;
                    }
                }
                if (isCut && !preCompToBounds.isEmpty()
                    // the last cut candidate (end of the region) cannot be the first cut
                    && (!parts.isEmpty() || (preCompToBounds.size() != compToBounds.size()))) {
                    compToBounds.keySet().removeAll(preCompToBounds.keySet());
                    parts.add(preCompToBounds);
                }
            }
            return parts;
        }
        
        private void mergeSubRegions(List regions, int dimension) {
            if (regions.isEmpty()) {
                horizontal = new LayoutInterval(PARALLEL);
                vertical = new LayoutInterval(PARALLEL);
                return;
            }
            LayoutInterval seqGroup = new LayoutInterval(SEQUENTIAL);
            LayoutInterval parGroup = new LayoutInterval(PARALLEL);
            int lastSeqTrailing = (dimension == HORIZONTAL) ? minx : miny;
            Iterator iter = regions.iterator();
            while (iter.hasNext()) {
                RegionInfo region = (RegionInfo)iter.next();
                LayoutInterval seqInterval;
                LayoutInterval parInterval;
                int seqGap;
                int parGap;
                if (dimension == HORIZONTAL) {
                    seqInterval = region.horizontal;
                    parInterval = region.vertical;
                    parGap = region.miny - miny;
                    seqGap = region.minx - lastSeqTrailing;
                    lastSeqTrailing = region.maxx;
                } else {
                    seqInterval = region.vertical;
                    parInterval = region.horizontal;
                    parGap = region.minx - minx;
                    seqGap = region.miny - lastSeqTrailing;
                    lastSeqTrailing = region.maxy;
                }
                // PENDING optimization of the resulting layout model
                if (seqGap > 0) {
                    LayoutInterval gap = new LayoutInterval(SINGLE);
                    gap.setSize(seqGap);
                    seqGroup.add(gap, -1);
                }
                add(seqInterval, seqGroup);
                parInterval = prefixByGap(parInterval, parGap);
                add(parInterval, parGroup);
            }
            if (dimension == HORIZONTAL) {
                horizontal = seqGroup;
                vertical = parGroup;
            } else {
                horizontal = parGroup;
                vertical = seqGroup;
            }
        }
        
        private LayoutInterval prefixByGap(LayoutInterval interval, int size) {
            if (size > 0) {
                LayoutInterval gap = new LayoutInterval(SINGLE);
                gap.setSize(size);
                if (interval.isSequential()) {
                    interval.add(gap, 0);
                } else {
                    LayoutInterval group = new LayoutInterval(SEQUENTIAL);
                    group.add(gap, -1);
                    add(interval, group);
                    interval = group;
                }
            }
            return interval;
        }

        private void add(LayoutInterval interval, LayoutInterval parent) {
            if (interval.isComponent()) { // needs to be undoable
                addInterval(interval, parent, -1);
            } else {
                parent.add(interval, -1);
            }
        }

        public LayoutInterval getInterval(int dimension) {
            return (dimension == HORIZONTAL) ? horizontal : vertical;
        }

    }

    // -----

    void setRemoveHandler(RemoveHandler h) {
        removeHandler = h;
    }

    RemoveHandler getRemoveHandler() {
        return removeHandler;
    }

    interface RemoveHandler {
        void removeComponents(LayoutComponent[] components, boolean fromModel);
    }

    void setResizeHandler(ResizeHandler h) {
        resizeHandler = h;
    }

    ResizeHandler getResizeHandler() {
        return resizeHandler;
    }

    interface ResizeHandler {
        void setIntervalSize(LayoutInterval interval, int dimension, int min, int pref, int max);
    }

    // -----
    // listeners registration, firing methods (no synchronization)

    void addListener(Listener l) {
        if (listeners == null) {
            listeners = new ArrayList<Listener>();
        }
        else {
            listeners.remove(l);
        }
        listeners.add(l);
    }

    void removeListener(Listener l) {
        if (listeners != null) {
            listeners.remove(l);
        }
    }

    private void fireEvent(LayoutEvent event) {
        if (listeners != null) {
            for (Listener l : listeners) {
                l.layoutChanged(event);
            }
        }
    }

    /**
     * Listener interface for changes in the layout model.
     */
    interface Listener {
        void layoutChanged(LayoutEvent ev);
    }

    // -----
    // changes recording and undo/redo

    public boolean isChangeRecording() {
        return recordingChanges;
    }

    public void setChangeRecording(boolean record) {
        recordingChanges = record;
    }

    boolean isUndoRedoInProgress() {
        return undoRedoInProgress;
    }

    public Object getChangeMark() {
        return new Integer(changeMark);
    }

    public boolean endUndoableEdit() {
        boolean empty = true;
        if (lastUndoableEdit != null) {
            lastUndoableEdit.endMark = getChangeMark();
            if (!lastUndoableEdit.endMark.equals(lastUndoableEdit.startMark)) {
                empty = false;
            }
            lastUndoableEdit = null;
        }
        return !empty;
    }

    public boolean isUndoableEditInProgress() {
        return (lastUndoableEdit != null);
    }

    public UndoableEdit getUndoableEdit() {
        if (recordingChanges && !undoRedoInProgress) {
            LayoutUndoableEdit undoEdit = new LayoutUndoableEdit();
            undoEdit.startMark = getChangeMark();
            endUndoableEdit();
            lastUndoableEdit = undoEdit;
            return undoEdit;
        }
        return null;
    }

    private void addChange(LayoutEvent change) {
        if (recordingChanges && !undoRedoInProgress) {
            redoMap.clear();
            if (undoMap.isEmpty())
                oldestMark = changeMark;

            undoMap.put(new Integer(changeMark++), change);

            while (undoMap.size() > changeCountHardLimit) {
                undoMap.remove(new Integer(oldestMark++));
            }
        }
    }

    boolean undo(Object startMark, Object endMark) {
        assert !undoRedoInProgress;
        boolean undone = false;
        int start = ((Integer)startMark).intValue();
        int end = ((Integer)endMark).intValue();
        undoRedoInProgress = true;
        while (end > start) {
            Integer key = new Integer(--end);
            LayoutEvent change = undoMap.remove(key);
            if (change != null) {
                change.undo();
                redoMap.put(key, change);
                undone = true;
            }
        }
        undoRedoInProgress = false;

        return undone;
    }

    boolean redo(Object startMark, Object endMark) {
        assert !undoRedoInProgress;
        int start = ((Integer)startMark).intValue();
        int end = ((Integer)endMark).intValue();
        undoRedoInProgress = true;

        while (start < end) {
            Integer key = new Integer(start++);
            LayoutEvent change = redoMap.remove(key);
            if (change != null) {
                change.redo();
                undoMap.put(key, change);
            }
        }

        undoRedoInProgress = false;
        return true;
    }

    boolean revert(Object startMark, Object endMark) {
        assert !undoRedoInProgress;
        boolean reverted = false;
        int start = ((Integer)startMark).intValue();
        int end = ((Integer)endMark).intValue();
        undoRedoInProgress = true;
        while (end > start) {
            Integer key = new Integer(--end);
            LayoutEvent change = undoMap.remove(key);
            if (change != null) {
                change.undo();
                reverted = true;
            }
        }
        undoRedoInProgress = false;
        if (lastUndoableEdit != null && startMark.equals(lastUndoableEdit.startMark)) {
            lastUndoableEdit.startMark = endMark;
        }
        return reverted;
    }

    void releaseChanges(Object fromMark, Object toMark) {
        int m1 = ((Integer)fromMark).intValue();
        int m2 = ((Integer)toMark).intValue();

        while (m1 < m2) {
            Integer m = new Integer(m1);
            undoMap.remove(m);
            redoMap.remove(m);
            m1++;
        }
    }

    /**
     * UndoableEdit implementation for series of changes in layout model.
     */
    private class LayoutUndoableEdit extends AbstractUndoableEdit {
        private Object startMark;
        private Object endMark;

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            if (endMark == null) {
                assert lastUndoableEdit == this;
                endMark = getChangeMark();
                lastUndoableEdit = null;
            }
            LayoutModel.this.undo(startMark, endMark);
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            LayoutModel.this.redo(startMark, endMark);
        }

        @Override
        public String getUndoPresentationName() {
            return ""; // NOI18N
        }
        @Override
        public String getRedoPresentationName() {
            return ""; // NOI18N
        }

        @Override
        public void die() {
            releaseChanges(startMark, endMark != null ? endMark : getChangeMark());
        }
    }

    /**
     * Returns dump of the layout model. For debugging and testing purposes only.
     *
     * @return dump of the layout model.
     */
    public String dump(final Map<String,String> idToNameMap) {
        return dump(idToNameMap, null, true);
    }

    /**
     * Returns dump of the layout model of given container.
     * For debugging and testing purposes only.
     *
     * @return dump of the layout model.
     */
    public String dump(final Map<String,String> idToNameMap, String contId, boolean subcontainers) {
        Set<LayoutComponent> roots = new TreeSet<LayoutComponent>(new Comparator<LayoutComponent>() {
            // comparator to ensure stable order of dump; according to tree
            // hierarchy, order within container, name
            @Override
            public int compare(LayoutComponent lc1, LayoutComponent lc2) {
                if (lc1 == lc2)
                    return 0;
                // parent always first
                if (lc1.isParentOf(lc2))
                    return -1;
                if (lc2.isParentOf(lc1))
                    return 1;
                // get the same level under common parent
                LayoutComponent parent = LayoutComponent.getCommonParent(lc1, lc2);
                while (lc1.getParent() != parent)
                    lc1 = lc1.getParent();
                while (lc2.getParent() != parent)
                    lc2 = lc2.getParent();
                if (parent != null) { // in the same tree
                    return parent.indexOf(lc1) < parent.indexOf(lc2) ? -1 : 1;
                }
                else { // in distinct trees
                    String id1 = lc1.getId();
                    String id2 = lc2.getId();
                    if (idToNameMap != null) {
                        id1 = idToNameMap.get(id1);
                        id2 = idToNameMap.get(id2);
                        if (id1 == null) {
                            return -1;
                        }
                        if (id2 == null) {
                            return 1;
                        }
                    }
                    return id1.compareTo(id2);
                }
            }
        });
        LayoutComponent rootComp = contId != null ? getLayoutComponent(contId) : null;
        Iterator<Map.Entry<String,LayoutComponent>> iter = idToComponents.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String,LayoutComponent> entry = iter.next();
            LayoutComponent comp = entry.getValue();
            if (comp.isLayoutContainer()
                    && (rootComp == null || comp == rootComp
                        || (subcontainers && rootComp.isParentOf(comp)))) {
                roots.add(comp);
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<LayoutModel>\n"); // NOI18N
        Iterator rootIter = roots.iterator();
        while (rootIter.hasNext()) {
            LayoutComponent root = (LayoutComponent)rootIter.next();
            String rootId = root.getId();
            if (idToNameMap != null) {
                rootId = idToNameMap.get(rootId);
            }
            if (rootId != null)
                sb.append("  <Root id=\"").append(rootId).append("\">\n"); // NOI18N
            else
                sb.append("  <Root>\n"); // NOI18N
            sb.append(saveContainerLayout(root, idToNameMap, 2, true));
            sb.append("  </Root>\n"); // NOI18N
        }
        sb.append("</LayoutModel>\n"); // NOI18N
        return sb.toString();
    }
    
    /**
     * Returns dump of the layout interval.
     *
     * @param interval interval whose dump should be returned.
     * @param dimension dimension in which the layout interval resides.
     */
    public String dump(LayoutInterval interval, int dimension) {
        return LayoutPersistenceManager.dumpInterval(this, interval, dimension, 2);
    }
    
    /**
     * Saves given layout container into a String.
     *
     * @param container the layout container to be saved
     * @param idToNameMap map for translating component Ids to names suitable
     *        for saving
     * @param indent determines size of indentation
     * @param humanReadable determines whether constants should be replaced
     * by human readable expressions
     * @return dump of the layout model of given container
     */
    public String saveContainerLayout(LayoutComponent container, Map<String,String> idToNameMap, int indent, boolean humanReadable) {
        return LayoutPersistenceManager.saveContainer(this, container, idToNameMap, indent, humanReadable);
    }

    /**
     * Loads the layout of the given container.
     *
     * @param containerId ID of the layout container to be loaded
     * @param layoutNodeList XML data to load
     * @param nameToIdMap map from component names to component IDs
     */
    public void loadContainerLayout(String containerId, org.w3c.dom.NodeList layoutNodeList, Map<String,String> nameToIdMap)
        throws java.io.IOException
    {
        LayoutPersistenceManager.loadContainer(this, containerId, layoutNodeList, nameToIdMap);
    }

    /**
     * Returns whether the model was repaired (because of some error found) or
     * upgraded automatically during loading. After loading, it might be a good
     * idea to save the corrected state, so to mark the loaded layout as modified.
     * @return whether the model was changed during loading or saving
     */
    public boolean wasCorrected() {
        return corrected;
    }

    void setCorrected() {
        corrected = true;
    }

    /* 
     * LINKSIZE 
     */
    
    // each object in the map is a List and contains list of components within the group
    private Map<Integer,List<String>> linkSizeGroupsH = new HashMap<Integer,List<String>>();
    private Map<Integer,List<String>> linkSizeGroupsV = new HashMap<Integer,List<String>>();
    
    private int maxLinkGroupId = 0;

    void addComponentToLinkSizedGroup(int groupId, String compId, int dimension) {
                
        if (NOT_EXPLICITLY_DEFINED == groupId) { // 
            return;
        }
        if (maxLinkGroupId < groupId) {
            maxLinkGroupId=groupId;
        }
        Integer groupIdInt = new Integer(groupId);
        Map<Integer,List<String>> linkSizeGroups = (dimension == HORIZONTAL) ? linkSizeGroupsH : linkSizeGroupsV;
        List<String> l = linkSizeGroups.get(groupIdInt);
        if ((l != null) && (l.contains(compId) || !sameContainer(compId, l.get(0)))) {
            return;
        }
        addComponentToLinkSizedGroupImpl(groupId, compId, dimension);
    }

    void addComponentToLinkSizedGroupImpl(int groupId, String compId, int dimension) {
        LayoutComponent lc = getLayoutComponent(compId);
        Integer groupIdInt = new Integer(groupId);
        Map<Integer,List<String>> linkSizeGroups = (dimension == HORIZONTAL) ? linkSizeGroupsH : linkSizeGroupsV;
        List<String> l = linkSizeGroups.get(groupIdInt);
        if (l != null) {
            l.add(lc.getId());
        } else {
            l = new ArrayList<String>();
            l.add(lc.getId());
            linkSizeGroups.put(groupIdInt, l);
        }

        int oldLinkSizeId = lc.getLinkSizeId(dimension);
        lc.setLinkSizeId(groupId, dimension);
        
        // record undo/redo and fire event
        LayoutEvent.Component ev = new LayoutEvent.Component(this, LayoutEvent.INTERVAL_LINKSIZE_CHANGED);
        ev.setLinkSizeGroup(lc, oldLinkSizeId, groupId, dimension);
        addChange(ev);
        fireEvent(ev);
    }

    private boolean sameContainer(String compId1, String compId2) {
        LayoutComponent lc1 = getLayoutComponent(compId1);
        LayoutComponent lc2 = getLayoutComponent(compId2);
        return lc1.getParent().equals(lc2.getParent());
    }
    
    void removeComponentFromLinkSizedGroup(LayoutComponent comp, int dimension) {

        if (comp == null) return;
        
        int linkId = comp.getLinkSizeId(dimension);
        if (linkId != NOT_EXPLICITLY_DEFINED) {

            Map<Integer,List<String>> map = (dimension == HORIZONTAL) ? linkSizeGroupsH : linkSizeGroupsV;
            Integer linkIdInt = new Integer(linkId);
            
            List<String> l = map.get(linkIdInt);
            l.remove(comp.getId());
            comp.setLinkSizeId(NOT_EXPLICITLY_DEFINED, dimension);
            
            if (l.size() == 1) {
                LayoutComponent lc = getLayoutComponent(l.get(0));
                int oldLinkSizeId = lc.getLinkSizeId(dimension);
                lc.setLinkSizeId(NOT_EXPLICITLY_DEFINED, dimension);
                map.remove(linkIdInt);
                // record undo/redo and fire event
                LayoutEvent.Component ev = new LayoutEvent.Component(this, LayoutEvent.INTERVAL_LINKSIZE_CHANGED);
                ev.setLinkSizeGroup(lc, oldLinkSizeId, NOT_EXPLICITLY_DEFINED, dimension);
                addChange(ev);
                fireEvent(ev);
            }
            
            if (l.isEmpty()) {
                map.remove(linkIdInt);
            }

            // record undo/redo and fire event
            LayoutEvent.Component ev = new LayoutEvent.Component(this, LayoutEvent.INTERVAL_LINKSIZE_CHANGED);
            ev.setLinkSizeGroup(comp, linkId, NOT_EXPLICITLY_DEFINED, dimension);
            addChange(ev);
            fireEvent(ev);
        }
    }
    
    /**
     * @return returns FALSE if components are not linked, and if so, they are linked in the same group
     *         returns TRUE if all components are in the same linksize group
     *         returns INVALID if none of above is true
     */
    public int areComponentsLinkSized(List<String> components, int dimension) {

        if (components.size() == 1) {
            String id = components.get(0);
            boolean retVal = (getLayoutComponent(id).isLinkSized(dimension));
            return retVal ? TRUE : FALSE;
        }
        
        Iterator i = components.iterator();
        List<Integer> idsFound = new ArrayList<Integer>();

        while (i.hasNext()) {
            String cid = (String)i.next();
            LayoutComponent lc = getLayoutComponent(cid);
            Integer linkSizeId =  new Integer(lc.getLinkSizeId(dimension));
            if (!idsFound.contains(linkSizeId)) {
                idsFound.add(linkSizeId);
            }
            if (idsFound.size() > 2) { // components are from at least two different groups
                return INVALID;
            }
        }
        if (idsFound.size() == 1) {
            if (idsFound.contains(new Integer(NOT_EXPLICITLY_DEFINED))) {
                return FALSE;
            }
            return TRUE;
        }
        if (idsFound.contains(new Integer(NOT_EXPLICITLY_DEFINED))) { // == 2 elements
            return FALSE;
        } else {
            return INVALID;
        }
    }
        
    Map<Integer,List<String>> getLinkSizeGroups(int dimension) {
        if (HORIZONTAL == dimension) {
            return linkSizeGroupsH;
        } 
        if (VERTICAL == dimension) {
            return linkSizeGroupsV;
        }
        return null; // incorrect dimension passed
    }
    
    public void unsetSameSize(List/*<String>*/ components, int dimension) {
        Iterator i = components.iterator();
        while (i.hasNext()) {
            String cid = (String)i.next();
            LayoutComponent lc = getLayoutComponent(cid);
            removeComponentFromLinkSizedGroup(lc, dimension);            
        }
    }
    
    public void setSameSize(List/*<String>*/ components, int dimension) {
        Iterator i = components.iterator();
        int groupId = findGroupId(components, dimension);
        
        while (i.hasNext()) {
            String cid = (String)i.next();
            LayoutComponent lc = getLayoutComponent(cid);
            addComponentToLinkSizedGroup(groupId, lc.getId(), dimension); 
        }
    }
    
    private int findGroupId(List/*<String*/ components, int dimension) {
        Iterator i = components.iterator();
        while (i.hasNext()) {
            String cid = (String)i.next();
            LayoutComponent lc = getLayoutComponent(cid);
            if (lc.isLinkSized(dimension)) {
                return lc.getLinkSizeId(dimension);
            }
        }
        return ++maxLinkGroupId;
    }
}
