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

package org.netbeans.modules.form.layoutdesign.support;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

import org.netbeans.modules.form.layoutdesign.*;

/**
 * This class constructs real layout of AWT/Swing components based on the
 * layout model.
 *
 * @author Jan Stola, Tomas Pavek
 */
public class SwingLayoutBuilder {

    /**
     * Default value for PADDING_SEPARATED type of gap.
     */
    public static final int PADDING_SEPARATE_VALUE = 18;

    private LayoutModel layoutModel;

    /**
     * Container being layed out.
     */
    private Container container;

    /**
     * LayoutComponent for the container.
     */
    private LayoutComponent containerLC;

    /**
     * Maps from component ID to Component.
     */
    private Map<String,Component> componentIDMap;

    /**
     * To workaround some cases of default ending container gaps
     * GroupLayout does not like.
     */
    private Set<LayoutInterval> unsupportedContResGaps;

    public SwingLayoutBuilder(LayoutModel layoutModel,
                              Container container, String containerId) {
        componentIDMap = new HashMap<String,Component>();
        this.layoutModel = layoutModel;
        this.container = container;
        this.containerLC = layoutModel.getLayoutComponent(containerId);
    }

    /**
     * Sets up layout of a container and adds all components to it according
     * to the layout model. This method is used for initial construction of
     * the layout visual representation (layout view).
     */
    public void setupContainerLayout(Component[] components, String[] compIds) {
//        addComponentsToContainer(components, compIds);
        for (int counter = 0; counter < components.length; counter++) {
            componentIDMap.put(compIds[counter], components[counter]);
        }
        createLayout();
    }

    /**
     * Adds new components to a container (according to the layout model).
     * This method is used for incremental updates of the layout view.
     */
//    public void addComponentsToContainer(Component[] components, String[] compIds) {
//        if (components.length != compIds.length) {
//            throw new IllegalArgumentException("Sizes must match");
//        }
//        for (int counter = 0; counter < components.length; counter++) {
//            componentIDMap.put(compIds[counter], components[counter]);
//        }
//        layout();
//    }

    /**
     * Removes components from a container. This method is used for incremental
     * updates of the layout view.
     */
    public void removeComponentsFromContainer(Component[] components, String[] compIds) {
        if (components.length != compIds.length) {
            throw new IllegalArgumentException("Sizes must match"); // NOI18N
        }
        for (int counter = 0; counter < components.length; counter++) {
            componentIDMap.remove(compIds[counter]);
        }
        createLayout();
    }

    /**
     * Clears given container - removes all components. This method is used
     * for incremental updates of the layout view.
     */
    public void clearContainer() {
        // Issue 121068 - componentResized event lost, but needed by JSlider
        // forces new componentResized event when the container is laid out
        issue121068Hack();
        container.removeAll();
        componentIDMap.clear();
    }

    private void issue121068Hack() {
        for (int i=0; i < container.getComponentCount(); i++) {
            Component comp = container.getComponent(i);
            if (comp instanceof JSlider) {
                comp.setBounds(0,0,0,0);
            }
        }
    }

    public void createLayout() {
        Throwable th = null;
        boolean reset = true;
        container.removeAll();
        unsupportedContResGaps = null;
        try {
            GroupLayout layout = new GroupLayout(container);
            container.setLayout(layout);

            // Issue 123320
            for (Component comp : componentIDMap.values()) {
                container.add(comp);
            }

            GroupLayout.Group[] layoutGroups = new GroupLayout.Group[2];
            // with multiple roots add the highest roots first so the components appear at the top
            for (int i=containerLC.getLayoutRootCount()-1; i >= 0; i--) {
                for (int dim=0; dim < layoutGroups.length; dim++) {
                    LayoutInterval interval = containerLC.getLayoutRoot(i, dim);
                    GroupLayout.Group group = composeGroup(layout, interval, true, true);
                    if (layoutGroups[dim] == null) {
                        layoutGroups[dim] = group;
                    } else { // add multiple roots into one parallel group
                        GroupLayout.ParallelGroup parallel;
                        if (!(layoutGroups[dim] instanceof GroupLayout.ParallelGroup)) {
                            parallel = layout.createParallelGroup();
                            parallel.addGroup(layoutGroups[dim]);
                            layoutGroups[dim] = parallel;
                        } else {
                            parallel = (GroupLayout.ParallelGroup) layoutGroups[dim];
                        }
                        parallel.addGroup(group);
                    }
                }
            }
            layout.setHorizontalGroup(layoutGroups[0]);
            layout.setVerticalGroup(layoutGroups[1]);
            composeLinks(layout);
            // Try to create the layout (to be able to reset it in case of some problem)
            layout.layoutContainer(container);
            layout.invalidateLayout(container);
            unsupportedContResGaps = null;
            reset = false;
        } finally {
            if (reset) {
                container.setLayout(null);
            }
        }
    }
    
    public void doLayout() {
        container.doLayout();
    }

    public static boolean isRelevantContainer(Container cont) {
        LayoutManager layout = cont.getLayout();
        return layout != null ? isRelevantLayoutManager(layout.getClass().getName()) : false;
    }

    public static boolean isRelevantLayoutManager(String layoutClassName) {
        return "org.jdesktop.layout.GroupLayout".equals(layoutClassName) // NOI18N
               || "javax.swing.GroupLayout".equals(layoutClassName); // NOI18N
    }

    // -----

    private GroupLayout.Group composeGroup(GroupLayout layout, LayoutInterval interval,
                                            boolean first, boolean last) {
        GroupLayout.Group group = null;
        if (interval.isGroup()) {            
            if (interval.isParallel()) {
                GroupLayout.Alignment groupAlignment = convertAlignment(interval.getGroupAlignment());
                boolean notResizable = interval.getMaximumSize() == LayoutConstants.USE_PREFERRED_SIZE;
                group = layout.createParallelGroup(groupAlignment, !notResizable);
            } else if (interval.isSequential()) {
                group = layout.createSequentialGroup();
            } else {
                assert false;
            }
            Iterator subIntervals = interval.getSubIntervals();
            while (subIntervals.hasNext()) {
                LayoutInterval subInterval = (LayoutInterval)subIntervals.next();
                fillGroup(layout, group, subInterval,
                          first,
                          last && (!interval.isSequential() || !subIntervals.hasNext()));
                if (first && interval.isSequential()) {
                    first = false;
                }
            }
        } else {
            group = layout.createSequentialGroup();
            fillGroup(layout, group, interval, true, true);
        }
        return group;
    }
    
    private void fillGroup(GroupLayout layout, GroupLayout.Group group, LayoutInterval interval,
                           boolean first, boolean last) {
        int alignment = getIntervalAlignment(interval);
        if (interval.isGroup()) {
            if (group instanceof GroupLayout.SequentialGroup) {
                ((GroupLayout.SequentialGroup)group).addGroup(composeGroup(layout, interval, first, last));
            } else {
                if (interval.isSequential() && last) {
                    LayoutInterval contResGap = SwingLayoutUtils.getUnsupportedResizingContainerGap(interval);
                    if (contResGap != null) { // GroupLayout bug workaround - will use fixed gap instead
                        addUnsupportedContResGap(contResGap);
                        alignment = LayoutConstants.LEADING;
                    }
                }
                ((GroupLayout.ParallelGroup)group).addGroup(
                        convertAlignment(alignment),
                        composeGroup(layout, interval, first, last));
            }
        } else {
            int minimum = interval.getMinimumSize();
            int preferred = interval.getPreferredSize();
            int maximum = interval.getMaximumSize();
            int min = convertSize(minimum, interval);
            int pref = convertSize(preferred, interval);
            int max = convertSize(maximum, interval);
            if (interval.isComponent()) {
                LayoutComponent layoutComp = interval.getComponent();
                Component comp = componentIDMap.get(layoutComp.getId());
                assert (comp != null);
                boolean horizontal = layoutComp.getLayoutInterval(LayoutConstants.HORIZONTAL) == interval;
                if (minimum == LayoutConstants.NOT_EXPLICITLY_DEFINED) {
                    if (horizontal && comp.getClass().getName().equals("javax.swing.JComboBox")) { // Issue 68612 // NOI18N
                        min = 0;
                    } else if (preferred >= 0) {
                        Dimension minDim = comp.getMinimumSize();
                        int compMin = horizontal ? minDim.width : minDim.height;
                        if (compMin > preferred) {
                            min = convertSize(LayoutConstants.USE_PREFERRED_SIZE, interval);
                        }
                    }
                }
                // workaround for bug in GroupLayout that does not align properly on baseline
                // if some component has 0 preferred width (even if actual size is bigger)
                if (pref == 0 && max >= Short.MAX_VALUE && horizontal
                        && layoutComp.getLayoutInterval(LayoutConstants.VERTICAL).getAlignment() == LayoutConstants.BASELINE) {
                    pref = 1;
                }
                if (group instanceof GroupLayout.SequentialGroup) {
                    ((GroupLayout.SequentialGroup)group).addComponent(comp, min, pref, max);
                } else {
                    GroupLayout.ParallelGroup pGroup = (GroupLayout.ParallelGroup)group;
                    pGroup.addComponent(comp, convertAlignment(alignment), min, pref, max);
                }
            } else {
                assert interval.isEmptySpace();
                if (interval.isDefaultPadding()) {
                    assert (group instanceof GroupLayout.SequentialGroup);
                    GroupLayout.SequentialGroup seqGroup = (GroupLayout.SequentialGroup)group;
                    if (first || last) {
                        if (last && isUnsupportedContResGap(interval)) {
                            // workaround GroupLayout bug - default container gap
                            // should not be resizing
                            seqGroup.addContainerGap();
                        } else {
                            seqGroup.addContainerGap(pref, max);
                        }
                    } else {
                        LayoutConstants.PaddingType paddingType = interval.getPaddingType();
                        if (paddingType == null || paddingType == LayoutConstants.PaddingType.RELATED) {
                            seqGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, pref, max);
                        } else if (paddingType == LayoutConstants.PaddingType.UNRELATED) {
                            seqGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, pref, max);
                        } else if (paddingType == LayoutConstants.PaddingType.SEPARATE) {
                            // special case - SEPARATE padding not known by LayoutStyle
                            if (pref == GroupLayout.DEFAULT_SIZE) {
                                pref = PADDING_SEPARATE_VALUE;
                            }
                            if (max == GroupLayout.DEFAULT_SIZE) {
                                max = PADDING_SEPARATE_VALUE;
                            }
                            seqGroup.addGap(PADDING_SEPARATE_VALUE, pref, max);
                        } else {
                            assert paddingType == LayoutConstants.PaddingType.INDENT;
                            // TBD
                        }
                    }
                } else {
                    if (min < 0) min = pref; // min == GroupLayout.PREFERRED_SIZE
                    min = Math.min(pref, min);
                    max = Math.max(pref, max);
                    if (group instanceof GroupLayout.SequentialGroup) {
                        if (pref < 0) { // survive invalid pref size (#159536)
                            ((GroupLayout.SequentialGroup)group)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, max);
                        } else {
                            ((GroupLayout.SequentialGroup)group).addGap(min, pref, max);
                        }
                    } else {
                        if (pref < 0) { // survive invalid pref size (#159536)
                            pref = 0;
                            min = 0;
                        }
                        ((GroupLayout.ParallelGroup)group).addGap(min, pref, max);
                    }
                }
            }
        }
    }

    /**
     * Filters out invalid use of BASELINE alignment (see issue 78035).
     * This method is a last resort to avoid failure in building the view.
     * See also LayoutModel.checkAndFixGroup method.
     */
    private static int getIntervalAlignment(LayoutInterval interval) {
        int alignment = interval.getAlignment();
        LayoutInterval group = interval.getParent();
        if (group.isParallel()) {
            int groupAlignment = group.getGroupAlignment();
            if ((alignment == LayoutConstants.BASELINE && groupAlignment != LayoutConstants.BASELINE)
                || (alignment != LayoutConstants.BASELINE && groupAlignment == LayoutConstants.BASELINE))
            {   // illegal combination, follow the group alignment
                alignment = groupAlignment;
                System.err.println("WARNING: Illegal use of baseline alignment, ignoring interval's alignment."); // NOI18N
//                assert false;
            }
        }
        else if (alignment != LayoutConstants.DEFAULT) {
            System.err.println("WARNING: Ignoring non-default alignment of interval in sequential group."); // NOI18N
//            assert false;
        }

        return alignment;
    }

    private static GroupLayout.Alignment convertAlignment(int alignment) {
        GroupLayout.Alignment groupAlignment;
        switch (alignment) {
            case LayoutConstants.DEFAULT: groupAlignment = GroupLayout.Alignment.LEADING; break;
            case LayoutConstants.LEADING: groupAlignment = GroupLayout.Alignment.LEADING; break;
            case LayoutConstants.TRAILING: groupAlignment = GroupLayout.Alignment.TRAILING; break;
            case LayoutConstants.CENTER: groupAlignment = GroupLayout.Alignment.CENTER; break;
            case LayoutConstants.BASELINE: groupAlignment = GroupLayout.Alignment.BASELINE; break;
            default: throw new IllegalArgumentException("Alignment: " + alignment); // NOI18N
        }
        return groupAlignment;
    }
    
    private int convertSize(int size, LayoutInterval interval) {
        int convertedSize;
        switch (size) {
            case LayoutConstants.NOT_EXPLICITLY_DEFINED: convertedSize = GroupLayout.DEFAULT_SIZE; break;
            case LayoutConstants.USE_PREFERRED_SIZE:
                if (interval.isEmptySpace()) {
                    int pref = interval.getPreferredSize();
                    assert pref != LayoutConstants.USE_PREFERRED_SIZE;
                    if (pref == LayoutConstants.USE_PREFERRED_SIZE) {
                        convertedSize = GroupLayout.DEFAULT_SIZE; // bug 244115 - prevent StackOverflowError
                    } else {
                        convertedSize = convertSize(pref, interval);
                    }
                } else {
                    convertedSize = GroupLayout.PREFERRED_SIZE;
                }
                break;
            default: convertedSize = (size >= 0) ? size : GroupLayout.DEFAULT_SIZE;
                     break;
        }
        return convertedSize;
    }

    private void composeLinks(GroupLayout layout) {
        composeLinks(layout, LayoutConstants.HORIZONTAL);
        composeLinks(layout, LayoutConstants.VERTICAL);
    }
    
    private void composeLinks(GroupLayout layout, int dimension) {

        Map<Integer,List<String>> links = SwingLayoutUtils.createLinkSizeGroups(containerLC, dimension);
        
        Set<Integer> linksSet = links.keySet();
        Iterator<Integer> i = linksSet.iterator();
        while (i.hasNext()) {
            List<String> group = links.get(i.next());
            List<Component> components = new ArrayList<Component>();
            for (int j=0; j < group.size(); j++) {
                String compId = group.get(j);
                LayoutComponent lc = layoutModel.getLayoutComponent(compId);
                if (lc != null) {
                    Component comp = componentIDMap.get(lc.getId());
                    if (comp == null) {
                        return;
                    } else {
                        components.add(comp);
                    }
                }
            }
            Component[] compArray = components.toArray(new Component[0]);
            if (compArray != null) {
                if (dimension == LayoutConstants.HORIZONTAL) {
                    layout.linkSize(SwingConstants.HORIZONTAL, compArray);
                }
                if (dimension == LayoutConstants.VERTICAL) {
                    layout.linkSize(SwingConstants.VERTICAL, compArray);
                }
            }
        }
    }

    private void addUnsupportedContResGap(LayoutInterval gap) {
        if (unsupportedContResGaps == null) {
            unsupportedContResGaps = new HashSet<LayoutInterval>();
        }
        unsupportedContResGaps.add(gap);
    }

    private boolean isUnsupportedContResGap(LayoutInterval gap) {
        return unsupportedContResGaps != null && unsupportedContResGaps.contains(gap);
    }
}
