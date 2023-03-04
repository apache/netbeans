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

import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

/**
 * This class manages layout information about a component in the layout.
 * It refers to corresponding layout intervals (horizontal and vertical) in the
 * layout structure.
 *
 * A layout component can be found according to its Id in the layout model.
 *
 * The component may serve the role of layout container - then it also defines
 * top (root) intervals (horizontal and vertical) for its internal layouts.
 * 
 * @see LayoutInterval
 *
 * @author Tomas Pavek
 */

public final class LayoutComponent implements LayoutConstants {

    // Identification of the component in the model
    private String componentId;

    // The parent component of this component
    private LayoutComponent parentComponent;

    // Layout intervals representing the component in the layout hierarchy.
    // There is one interval for each dimension.
    private LayoutInterval[] layoutIntervals;

    // Potential resizability of the component in the design area.
    private boolean[] resizability;

    // Root layout intervals of a container layout. There is one interval for
    // each dimension. Defined by components that are layout containers, i.e.
    // managing layout of their subcomponents. Otherwise the array is null.
    private List<LayoutInterval[]> layoutRoots;

    // Subcomponents of this component.
    private List<LayoutComponent> subComponents;

    // Difference of actual visualized size to component's minimum size.
    private int[] diffToMinSize;

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    // horizontal size-link
    private int horizontalLinkId = NOT_EXPLICITLY_DEFINED;
    
    // vertical size-link
    private int verticalLinkId = NOT_EXPLICITLY_DEFINED;
    
    // -----
    // setup

    public LayoutComponent(String id, boolean isContainer) {
        if (id == null)
            throw new NullPointerException();
        componentId = id;
        layoutIntervals = new LayoutInterval[DIM_COUNT];
        for (int i=0; i < DIM_COUNT; i++) {
            layoutIntervals[i] = new LayoutInterval(SINGLE);
            layoutIntervals[i].setComponent(this);
            layoutIntervals[i].setSizes(USE_PREFERRED_SIZE,
                                        NOT_EXPLICITLY_DEFINED,
                                        USE_PREFERRED_SIZE);
        }
        if (isContainer) {
            createRoots();
        }
    }

    public LayoutComponent(String id, boolean isContainer, int initialWidth, int initialHeight) {
        this(id, isContainer);
        if (isContainer) {
            LayoutInterval[] defaultRoots = layoutRoots.get(0);
            for (int i=0; i < DIM_COUNT; i++) {
                LayoutInterval gap = new LayoutInterval(SINGLE);
                gap.setSizes(0, i==HORIZONTAL ? initialWidth : initialHeight, Short.MAX_VALUE);
                defaultRoots[i].add(gap, 0);
            }
        }
        else {
            layoutIntervals[HORIZONTAL].setPreferredSize(initialWidth);
            layoutIntervals[VERTICAL].setPreferredSize(initialHeight);
        }
    }

    private void createRoots() {
        layoutRoots = new LinkedList<LayoutInterval[]>();
        addNewLayoutRoots();
    }

    // -----

    void setId(String id) {
        componentId = id;
    }

    public String getId() {
        return componentId;
    }

    public LayoutComponent getParent() {
        return parentComponent;
    }

    public boolean isParentOf(LayoutComponent comp) {
        do {
            comp = comp.getParent();
            if (comp == this)
                return true;
        }
        while (comp != null);
        return false;
    }

    static LayoutComponent getCommonParent(LayoutComponent comp1, LayoutComponent comp2) {
        // Find all parents of given components
        Iterator<LayoutComponent> parents1 = parentsOfComponent(comp1).iterator();
        Iterator<LayoutComponent> parents2 = parentsOfComponent(comp2).iterator();
        LayoutComponent parent1 = parents1.next();
        LayoutComponent parent2 = parents2.next();

        // Candidate for the common parent
        LayoutComponent parent = null;
        while (parent1 == parent2) {
            parent = parent1;
            if (parents1.hasNext()) {
                parent1 = parents1.next();
            } else {
                break;
            }
            if (parents2.hasNext()) {
                parent2 = parents2.next();
            } else {
                break;
            }
        }
        return parent;
    }

    private static List<LayoutComponent> parentsOfComponent(LayoutComponent comp) {
        List<LayoutComponent> parents = new LinkedList<LayoutComponent>();
        while (comp != null) {
            parents.add(0, comp);
            comp = comp.getParent();
        }
        return parents;
    }

    public LayoutInterval getLayoutInterval(int dimension) {
        return layoutIntervals[dimension];
    }

    void setLayoutInterval(LayoutInterval interval, int dimension) {
        layoutIntervals[dimension] = interval;
    }

    LayoutRegion getCurrentSpace() {
        return getLayoutInterval(HORIZONTAL).getCurrentSpace();
    }

    public boolean isLayoutContainer() {
        return layoutRoots != null;
    }

    void setResizability(boolean[] resizability) {
        this.resizability = resizability;
    }

    boolean[] getResizability() {
        return resizability;
    }

    LayoutInterval[] getParentRoots() {
        return parentComponent != null ? parentComponent.getLayoutRoots(layoutIntervals[0]) : null;
    }

    void setDiffToMinimumSize(int dimension, int diff) {
        if (diffToMinSize == null) {
            diffToMinSize = new int[DIM_COUNT];
        }
        diffToMinSize[dimension] = diff;
    }

    int getDiffToMinimumSize(int dimension) {
        assert isLayoutContainer();
        return diffToMinSize != null ? diffToMinSize[dimension] : 0;
    }

    // -----
    // subcomponents

    public List<LayoutComponent> getSubcomponents() {
        return subComponents != null && subComponents.size() > 0 ?
               Collections.unmodifiableList(subComponents) : Collections.EMPTY_LIST;
    }

    int getSubComponentCount() {
        return (subComponents == null) ? 0 : subComponents.size();
    }
    
    LayoutComponent getSubComponent(int index) {
        return subComponents.get(index);
    }

    int indexOf(LayoutComponent comp) {
        return subComponents != null ? subComponents.indexOf(comp) : -1;
    }

//    int add(LayoutComponent comp) {
//        return add(comp, -1);
//    }

    int addComponent(LayoutComponent comp, int index) {
        assert isLayoutContainer();

        if (subComponents == null) {
            subComponents = new LinkedList<LayoutComponent>();
        }
        if (index < 0) {
            index = subComponents.size();
        }
        subComponents.add(index, comp);
        comp.parentComponent = this;

        return index;
    }

    int removeComponent(LayoutComponent comp) {
        if (subComponents != null) {
            Iterator it = subComponents.iterator();
            int index = -1;
            while (it.hasNext()) {
                index++;
                if (comp == it.next()) {
                    it.remove();
                    comp.parentComponent = null;
                    return index;
                }
            }
        }
        return -1;
    }

    // -----
    // container's layout roots

    public int getLayoutRootCount() {
        return layoutRoots != null ? layoutRoots.size() : 0;
    }

    public LayoutInterval getLayoutRoot(int rootIndex, int dimension) {
        return layoutRoots.get(rootIndex)[dimension];
    }

    LayoutInterval getDefaultLayoutRoot(int dimension) {
        return layoutRoots.get(0)[dimension];
    }

    List<LayoutInterval[]> getLayoutRoots() {
        return layoutRoots;
    }

    void setLayoutRoots(List<LayoutInterval[]> roots) {
        LayoutRegion[] space;
        if (layoutRoots != null && !layoutRoots.isEmpty()) {
            space = new LayoutRegion[] {
                layoutRoots.get(0)[HORIZONTAL].getCurrentSpace(),
                layoutRoots.get(0)[VERTICAL].getCurrentSpace() };
        } else {
            space = null;
        }

        if (roots == null && layoutRoots != null) {
            // instead of no roots create default empty roots (to keep this a container)
            // for no roots use setLayoutContainer(false, null)
            createRoots();
        } else {
            layoutRoots = roots;
        }

        if (space != null) {
            for (LayoutInterval[] r : layoutRoots) {
                for (int i=0; i < r.length; i++) {
                    LayoutRegion rs = r[i].getCurrentSpace();
                    if (space[i].isSet() && !rs.isSet()) {
                        rs.set(space[i]);
                    }
                }
            }
        }
    }

    LayoutInterval[] getLayoutRoots(LayoutInterval interval) {
        interval = LayoutInterval.getRoot(interval);
        for (LayoutInterval[] roots : layoutRoots) {
            for (int dim=0; dim < DIM_COUNT; dim++) {
                if (interval == roots[dim]) {
                    return roots;
                }
            }
        }
        return null;
    }

    int getLayoutRootsIndex(LayoutInterval interval) {
        interval = LayoutInterval.getRoot(interval);
        int index = -1;
        for (LayoutInterval[] roots : layoutRoots) {
            index++;
            for (int dim=0; dim < DIM_COUNT; dim++) {
                if (interval == roots[dim]) {
                    return index;
                }
            }
        }
        return -1;
    }

    void addLayoutRoots(LayoutInterval[] roots, int index) {
        if (index < 0) {
            index = layoutRoots.size();
        }
        layoutRoots.add(index, roots);
    }

    int removeLayoutRoots(LayoutInterval[] roots) {
        Iterator<LayoutInterval[]> it = layoutRoots.iterator();
        int index = -1;
        while (it.hasNext()) {
            index++;
            if (roots == it.next()) {
                it.remove();
                return index;
            }
        }
        return -1;
    }

    LayoutInterval[] addNewLayoutRoots() {
        LayoutInterval[] roots = new LayoutInterval[DIM_COUNT];
        for (int i=0; i < DIM_COUNT; i++) {
            roots[i] = new LayoutInterval(PARALLEL);
        }
        layoutRoots.add(roots);
        return roots;
    }

    void setLayoutContainer(boolean isContainer, List<LayoutInterval[]> roots) {
        if (isContainer != isLayoutContainer()) {
            if (isContainer) {
                if (roots == null) {
                    createRoots();
                } else {
                    layoutRoots = roots;
                }
            }
            else {
                layoutRoots = null;
                subComponents = null;
            }
        }
    }

    static boolean isUnplacedComponent(LayoutComponent comp) {
        LayoutComponent cont = comp.getParent();
        return cont != null && comp.getParentRoots()[HORIZONTAL]
                                 != cont.getDefaultLayoutRoot(HORIZONTAL);
    }

    // -----
    // current state of the layout - current position and size of component
    // kept to be available quickly for the layout designer

    void setCurrentBounds(Rectangle bounds, int baseline) {
        LayoutRegion space = layoutIntervals[0].getCurrentSpace();
        if (bounds != null) {
            space.set(bounds, baseline > 0 ? bounds.y + baseline : LayoutRegion.UNKNOWN);
        } else {
            space.reset();
        }
        for (int i=1; i < layoutIntervals.length; i++) {
            layoutIntervals[i].setCurrentSpace(space);
        }
    }

    void setCurrentInterior(Rectangle bounds) {
        LayoutRegion space = null;
        for (LayoutInterval[] roots : layoutRoots) {
            for (int i=0; i < roots.length; i++) {
                if (space == null) {
                    space = roots[i].getCurrentSpace();
                    space.set(bounds, LayoutRegion.UNKNOWN);
                }
                else {
                    roots[i].setCurrentSpace(space);
                }
            }
        }
    }

    // -----

    /**
     * @return whether this intervals size is linked with some other component in a direction horizontal or vertical
     */
    public boolean isLinkSized(int dimension) {
        if (dimension == HORIZONTAL) {
            return NOT_EXPLICITLY_DEFINED != horizontalLinkId;
        }
        return NOT_EXPLICITLY_DEFINED != verticalLinkId;
    }
    
    /**
     * @return whether this intervals size is linked with some other component in a direction horizontal or vertical
     */
    public int getLinkSizeId(int dimension) {
        if (dimension == HORIZONTAL) {
            return horizontalLinkId;
        }
        return verticalLinkId;
    }

    /**
     * @return whether this intervals size is linked with some other component in a direction horizontal or vertical
     */
    public void setLinkSizeId(int id, int dimension) {
        if (dimension == HORIZONTAL) {
            horizontalLinkId = id;
        } else {
            verticalLinkId = id;
        }
        
    }

    // -----
    // listener support

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
    void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
    
}
