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

import java.util.*;
import org.w3c.dom.*;

/**
 * Class responsible for loading and saving of layout model.
 *
 * @author Jan Stola
 */
class LayoutPersistenceManager implements LayoutConstants {
    /** Layout model to load/save. */
    private LayoutModel layoutModel;
    /** Currently processed layout container. */
    private LayoutComponent layoutContainer;
    /** Index of the currently processed layout root interval. */
    private int rootIndex;
    /** Currently processed dimension. */
    private int dimension;
    /** Map from component IDs to names or vice versa. */
    private Map<String, String> idNameMap;
    /** Determines whether constants should be replaces by human readable expressions. */
    private boolean humanReadable;
    /** Size of current indent. */
    private int indent;
    /** String buffer used to save layout. */
    private StringBuilder sb;
    
    // elements names
    static final String XML_DIMENSION_LAYOUT = "DimensionLayout"; // NOI18N
    static final String XML_GROUP = "Group"; // NOI18N
    static final String XML_COMPONENT = "Component"; // NOI18N
    static final String XML_EMPTY_SPACE = "EmptySpace"; // NOI18N
    
    // attributes names
    static final String ATTR_DIMENSION_DIM = "dim"; // NOI18N
    static final String ATTR_GROUP_TYPE = "type"; // NOI18N
    static final String ATTR_PADDING_TYPE = "type"; // NOI18N
    static final String ATTR_SIZE_MIN = "min"; // NOI18N
    static final String ATTR_SIZE_PREF = "pref"; // NOI18N
    static final String ATTR_SIZE_MAX = "max"; // NOI18N
    static final String ATTR_ALIGNMENT = "alignment"; // NOI18N
    static final String ATTR_GROUP_ALIGNMENT = "groupAlignment"; // NOI18N
    static final String ATTR_LINK_SIZE = "linkSize"; // NOI18N
    static final String ATTR_COMPONENT_ID = "id"; // NOI18N
    static final String ATTR_ATTRIBUTES = "attributes"; // NOI18N
    static final String ATTR_ROOT_INDEX = "rootIndex"; // NOI18N
    
    // attribute values
    static final String VALUE_DIMENSION_HORIZONTAL = "horizontal"; // NOI18N
    static final String VALUE_DIMENSION_VERTICAL = "vertical"; // NOI18N
    static final String VALUE_ALIGNMENT_LEADING = "leading"; // NOI18N
    static final String VALUE_ALIGNMENT_TRAILING = "trailing"; // NOI18N
    static final String VALUE_ALIGNMENT_CENTER = "center"; // NOI18N
    static final String VALUE_ALIGNMENT_BASELINE = "baseline"; // NOI18N
    static final String VALUE_SIZE_PREFERRED = "$pref"; // NOI18N
    static final String VALUE_SIZE_MAX = "Short.MAX_VALUE"; // NOI18N
    static final String VALUE_GROUP_PARALLEL = "parallel"; // NOI18N
    static final String VALUE_GROUP_SEQUENTIAL = "sequential"; // NOI18N
    static final String VALUE_PADDING_RELATED = "related"; // NOI18N
    static final String VALUE_PADDING_UNRELATED = "unrelated"; // NOI18N
    static final String VALUE_PADDING_SEPARATE = "separate"; // NOI18N
    static final String VALUE_PADDING_INDENT = "indent"; // NOI18N

    /**
     * Creates new <code>LayoutPersistenceManager</code>.
     *
     * @param layoutModel layout model to load/save.
     */
    private LayoutPersistenceManager(LayoutModel layoutModel) {
        this.layoutModel = layoutModel;
    }

    /**
     * Returns the layout model saved as XML in a String.
     *
     * @param layoutModel layout model to save
     * @param container the layout container to be saved
     * @param idToNameMap map for translating component Ids to names suitable
     *        for saving
     * @param indent determines size of indentation
     * @param humanReadable determines whether constants should be replaced
     * by human readable expressions
     * @return the layout model saved in a String
     */
    static String saveContainer(LayoutModel layoutModel, LayoutComponent container,
                                Map<String,String> idToNameMap, int indent, boolean humanReadable)
    {
        LayoutPersistenceManager lpm = new LayoutPersistenceManager(layoutModel);
        lpm.layoutContainer = container;
        lpm.idNameMap = idToNameMap;
        lpm.indent = indent;
        lpm.humanReadable = humanReadable;
        return lpm.saveLayout();
    }

    private String saveLayout() {
        sb = new StringBuilder();
        for (dimension=0; dimension < DIM_COUNT; dimension++) {
            indent().append('<').append(XML_DIMENSION_LAYOUT);
            sb.append(' ').append(ATTR_DIMENSION_DIM).append("=\""); // NOI18N
            if (humanReadable) {
                switch (dimension) {
                    case HORIZONTAL: sb.append(VALUE_DIMENSION_HORIZONTAL); break;
                    case VERTICAL: sb.append(VALUE_DIMENSION_VERTICAL); break;
                    default: sb.append(dimension); break;
                }
            } else {
                sb.append(dimension);
            }
            sb.append("\">\n"); // NOI18N
            // in case of multiple roots save the additional roots under the first one
            rootIndex = 0;
            saveInterval(layoutContainer.getLayoutRoot(0, dimension));
            indent().append("</").append(XML_DIMENSION_LAYOUT).append(">\n"); // NOI18N
        }
        return sb.toString();
    }

    /**
     * Returns dump of the layout interval.
     *
     * @param layoutModel layout model to dump
     * @param interval the layout interval that should be dumped
     * @param dimension the dimension where the interval belongs
     * @param indent determines size of indentation
     * @return dump of the layout model
     */
    static String dumpInterval(LayoutModel layoutModel, LayoutInterval interval,
                               int dimension, int indent)
    {
        LayoutPersistenceManager lpm = new LayoutPersistenceManager(layoutModel);
        lpm.indent = indent;
        lpm.humanReadable = true;
        lpm.dimension = dimension;
        lpm.sb = new StringBuilder();
        lpm.saveInterval(interval);
        return lpm.sb.toString();
    }
    
    /**
     * Dumps the information about the given layout interval.
     *
     * @param interval layout interval to dump.
     */
    private void saveInterval(LayoutInterval interval) {
        indent++;
        indent();
        if (interval.isGroup()) {
            sb.append('<').append(XML_GROUP).append(' ');
            sb.append(ATTR_GROUP_TYPE).append("=\""); // NOI18N
            if (humanReadable) {
                sb.append(interval.isParallel() ? VALUE_GROUP_PARALLEL : VALUE_GROUP_SEQUENTIAL);
            } else {
                sb.append(interval.getType());
            }
            sb.append("\""); // NOI18N
            if (interval.getParent() == null && rootIndex > 0) {
                // mark the additional roots by a root index attribute
                sb.append(" ").append(ATTR_ROOT_INDEX).append("=\""); // NOI18N
                sb.append(rootIndex);
                sb.append("\""); // NOI18N
            }
            saveAlignment(interval.getRawAlignment(), false);
            if (interval.isParallel()) {
                saveAlignment(interval.getGroupAlignment(), true);
            }
            saveSize(interval.getMinimumSize(), ATTR_SIZE_MIN);
            saveSize(interval.getMaximumSize(), ATTR_SIZE_MAX);
            saveAttributes(interval.getAttributes());
            sb.append(">\n"); // NOI18N
            indent++;
            Iterator iter = interval.getSubIntervals();
            while (iter.hasNext()) {
                LayoutInterval subInterval = (LayoutInterval)iter.next();
                saveInterval(subInterval);
            }
            if (interval.getParent() == null && rootIndex == 0 && layoutContainer != null) {
                // save the additional roots under the main one
                for (int i=1; i < layoutContainer.getLayoutRootCount(); i++) {
                    rootIndex = i;
                    saveInterval(layoutContainer.getLayoutRoot(rootIndex, dimension));
                }
            }
            indent--;
            indent().append("</").append(XML_GROUP).append(">\n"); // NOI18N
        } else {
            if (interval.isComponent()) {
                String name = interval.getComponent().getId();
                if (idNameMap != null) {
                    name = idNameMap.get(name);
                    assert (name != null);
                }
                sb.append('<').append(XML_COMPONENT).append(' ');
                sb.append(ATTR_COMPONENT_ID).append("=\"").append(name).append("\""); // NOI18N
                saveLinkSize(interval.getComponent().getLinkSizeId(dimension));
                saveAlignment(interval.getRawAlignment(), false);
            } else if (interval.isEmptySpace()) {
                sb.append('<').append(XML_EMPTY_SPACE);
                if (interval.isDefaultPadding()) {
                    savePaddingType(interval.getPaddingType());
                    // TBD save components for indent gap
                }
            } else {
                assert false;
            }
            saveSize(interval.getMinimumSize(), ATTR_SIZE_MIN);
            saveSize(interval.getPreferredSize(), ATTR_SIZE_PREF);
            saveSize(interval.getMaximumSize(), ATTR_SIZE_MAX);
            saveAttributes(interval.getAttributes());
            sb.append("/>\n"); // NOI18N
        }
        indent--;
    }

    /**
     * Saves linkSize group identifier
     *
     * @param linksizeid 
     */
    private void saveLinkSize(int linkSizeId) {
        if (linkSizeId != NOT_EXPLICITLY_DEFINED) {
            sb.append(" ").append(ATTR_LINK_SIZE).append("=\"").append(linkSizeId).append("\""); // NOI18N
        }
    }
    
    /**
     * Saves group/interval alignemnt.
     *
     * @param alignemnt alignment to save.
     * @param group determines whether it is a group alignment.
     */
    private void saveAlignment(int alignment, boolean group) {
        String attrPrefix = " " + (group ? ATTR_GROUP_ALIGNMENT : ATTR_ALIGNMENT) + "=\""; // NOI18N
        if (humanReadable) {
            if (alignment != DEFAULT) {
                sb.append(attrPrefix);
                switch (alignment) {
                    case LEADING: sb.append(VALUE_ALIGNMENT_LEADING); break;
                    case TRAILING: sb.append(VALUE_ALIGNMENT_TRAILING); break;
                    case CENTER: sb.append(VALUE_ALIGNMENT_CENTER); break;
                    case BASELINE: sb.append(VALUE_ALIGNMENT_BASELINE); break;
                    default: assert false;
                }
                sb.append("\""); // NOI18N
            }
        } else {
            if (alignment != DEFAULT) {
                sb.append(attrPrefix).append(alignment).append("\""); // NOI18N
            }
        }
    }
    
    /**
     * Saves size parameter of some layout interval.
     *
     * @param size value of the size parameter.
     * @param attr name of the size parameter.
     */
    private void saveSize(int size, String attr) {
        String attrPrefix = " " + attr + "=\""; // NOI18N            
        if (humanReadable) {
            if (size != NOT_EXPLICITLY_DEFINED) {
                sb.append(attrPrefix);
                if (size == USE_PREFERRED_SIZE) {
                    sb.append(VALUE_SIZE_PREFERRED);
                } else {
                    if (size == Short.MAX_VALUE) {
                        sb.append(VALUE_SIZE_MAX);
                    } else {
                        sb.append(size);
                    }
                }
                sb.append("\""); // NOI18N
            }
        } else {
            if (size != NOT_EXPLICITLY_DEFINED) {
                sb.append(attrPrefix).append(size).append("\""); // NOI18N
            }
        }
    }

    private void savePaddingType(LayoutConstants.PaddingType paddingType) {
        if (paddingType != null && paddingType != LayoutConstants.PaddingType.RELATED) {
            sb.append(' ').append(ATTR_PADDING_TYPE).append("=\""); // NOI18N
            String str;
            switch (paddingType) {
                case UNRELATED: str = VALUE_PADDING_UNRELATED; break;
                case SEPARATE: str = VALUE_PADDING_SEPARATE; break;
                case INDENT: str = VALUE_PADDING_INDENT; break;
                default: str = VALUE_PADDING_RELATED; break;
            }
            sb.append(str).append("\""); // NOI18N
        }
    }

    /**
     * Saves attributes of some layout interval.
     *
     * @param attributes attributes of some layout interval.
     */
    private void saveAttributes(int attributes) {
        if (!humanReadable)
            attributes &= LayoutInterval.ATTR_PERSISTENT_MASK;
        sb.append(' ').append(ATTR_ATTRIBUTES).append("=\""); // NOI18N
        sb.append(attributes).append("\""); // NOI18N
    }

    /**
     * Performs indentation.
     *
     * @return indented <code>StringBuffer</code>.
     */
    private StringBuilder indent() {
        char[] spaces = new char[2*indent];
        Arrays.fill(spaces, ' ');
        return sb.append(spaces);
    }
    
    /**
     * Loads the layout of the given container. Does not load containers
     * recursively, is called for each container separately.
     *
     * @param layoutModel layout model to load
     * @param containerId ID of the layout container to be loaded
     * @param layoutNodeList XML data to load
     * @param nameToIdMap map from component names to component IDs
     */
    static void loadContainer(LayoutModel layoutModel, String containerId,
                              NodeList layoutNodeList, Map<String,String> nameToIdMap)
        throws java.io.IOException
    {
        LayoutPersistenceManager lpm = new LayoutPersistenceManager(layoutModel);
        lpm.idNameMap = nameToIdMap;
        lpm.loadLayout(containerId, layoutNodeList);
    }

    // should be called only on newly created LayoutPersistenceManager for each
    // loaded container (don't call repeatedly)
    private void loadLayout(String containerId, NodeList layoutNodeList)
        throws java.io.IOException
    {
        layoutContainer = layoutModel.getLayoutComponent(containerId);
        if (layoutContainer == null) {
            layoutContainer = new LayoutComponent(containerId, true);
            layoutModel.addRootComponent(layoutContainer);
        }

        for (int i=0; i<layoutNodeList.getLength(); i++) {
            Node dimLayoutNode = layoutNodeList.item(i);
            if (!(dimLayoutNode instanceof Element)
                    || !dimLayoutNode.getNodeName().equals(XML_DIMENSION_LAYOUT)) {
                continue;
            }
            Node dimAttrNode = dimLayoutNode.getAttributes().getNamedItem(ATTR_DIMENSION_DIM);
            dimension = integerFromNode(dimAttrNode);
            rootIndex = 0;
            LayoutInterval layoutRoot = layoutContainer.getLayoutRoot(0, dimension);
            NodeList subNodes = dimLayoutNode.getChildNodes();
            for (int j=0; j<subNodes.getLength(); j++) {
                Node node = subNodes.item(j);
                if (node instanceof Element) {
                    loadGroup(layoutRoot, node);
                    break; // just one root is loaded
                }
            }
        }

        correctMissingName(); // recover from missing component name if needed
        checkMissingComponentsInDimension(); // check if the container layout is valid - all components having intervals in both dimensions
    }

    /**
     * Loads layout of the given group.
     *
     * @param group group whose layout information should be loaded.
     * @param groupNode node holding the information about the layout of the group.
     */
    private void loadGroup(LayoutInterval group, Node groupNode)
        throws java.io.IOException
    {
        NamedNodeMap attrMap = groupNode.getAttributes();
        Node alignmentNode = attrMap.getNamedItem(ATTR_ALIGNMENT);
        Node groupAlignmentNode = attrMap.getNamedItem(ATTR_GROUP_ALIGNMENT);
        Node minNode = attrMap.getNamedItem(ATTR_SIZE_MIN);
        Node maxNode = attrMap.getNamedItem(ATTR_SIZE_MAX);
        int alignment = (alignmentNode == null) ? DEFAULT : integerFromNode(alignmentNode);
        group.setAlignment(alignment);
        if (group.isParallel()) {
            int groupAlignment = (groupAlignmentNode == null) ? DEFAULT : integerFromNode(groupAlignmentNode);
            if (groupAlignment != DEFAULT) {
                group.setGroupAlignment(groupAlignment);
            }
        }
        int min = (minNode == null) ? NOT_EXPLICITLY_DEFINED : integerFromNode(minNode);
        int max = (maxNode == null) ? NOT_EXPLICITLY_DEFINED : integerFromNode(maxNode);
        group.setMinimumSize(min);
        group.setMaximumSize(max);
        loadAttributes(group, attrMap);
        NodeList subNodes = groupNode.getChildNodes();
        for (int i=0; i<subNodes.getLength(); i++) {
            Node subNode = subNodes.item(i);
            if (!(subNode instanceof Element)) {
                continue;
            }
            String nodeName = subNode.getNodeName();
            if (XML_GROUP.equals(nodeName)) {
                LayoutInterval subGroup = null;
                int groupType = integerFromNode(subNode.getAttributes().getNamedItem(ATTR_GROUP_TYPE));
                // the sub-group might represent an additional separate root
                if (group.getParent() == null && groupType == PARALLEL) {
                    Node rootIndexNode = subNode.getAttributes().getNamedItem(ATTR_ROOT_INDEX);
                    if (rootIndexNode != null) {
                        rootIndex = integerFromNode(rootIndexNode);
                        while (rootIndex >= layoutContainer.getLayoutRootCount()) {
                            layoutContainer.addNewLayoutRoots();
                        }
                        subGroup = layoutContainer.getLayoutRoot(rootIndex, dimension);
                    }
                }
                if (subGroup == null) { // this is a normal group
                    subGroup = new LayoutInterval(groupType);
                    group.add(subGroup, -1);
                }
                loadGroup(subGroup, subNode);
            } else if (XML_EMPTY_SPACE.equals(nodeName)) {
                loadEmptySpace(group, subNode);
            } else {
                assert XML_COMPONENT.equals(nodeName);
                loadComponent(group, subNode);
            }
        }
        if (dimension == VERTICAL) {
            checkAndFixGroup(group);
        }
    }

    /**
     * Loads information about empty space.
     *
     * @param parent layout parent of the empty space.
     * @param spaceNode node with the information about the empty space.
     */
    private void loadEmptySpace(LayoutInterval parent, Node spaceNode) {
        LayoutInterval space = new LayoutInterval(SINGLE);
        NamedNodeMap attrMap = spaceNode.getAttributes();
        loadSizes(space, attrMap);
        loadAttributes(space, attrMap);
        parent.add(space, -1);
    }
    
    /**
     * Loads information about component.
     *
     * @param parent layout parent of the loaded layout interval.
     * @param componentNode node with the information about the component.
     */
    private void loadComponent(LayoutInterval parent, Node componentNode)
        throws java.io.IOException
    {
        NamedNodeMap attrMap = componentNode.getAttributes();
        String name = attrMap.getNamedItem(ATTR_COMPONENT_ID).getNodeValue();
        Node linkSizeId = attrMap.getNamedItem(ATTR_LINK_SIZE);
        String id = idNameMap.get(name);
        if (id == null) { // try to workaround the missing name error (issue 77092)
            id = useTemporaryId(name);
        }
        Node alignmentNode = attrMap.getNamedItem(ATTR_ALIGNMENT);
        int alignment = (alignmentNode == null) ? DEFAULT : integerFromNode(alignmentNode);
        LayoutComponent layoutComponent = layoutModel.getLayoutComponent(id);
        if (layoutComponent == null) {
            layoutComponent = new LayoutComponent(id, false);
            // assuming the layout tree is loaded from bottom, so if a component
            // is not yet in the model at this point, it can only be a component,
            // not a container (that would be already loaded)
        }
        if (layoutComponent.getParent() == null) {
            layoutModel.addComponent(layoutComponent, layoutContainer, -1);
        }
        LayoutInterval interval = layoutComponent.getLayoutInterval(dimension);
        if (interval.getParent() != null) { // try to workaround error of a component more than once in the layout (bug 149608, 118562)
            System.err.println("WARNING: Component " + name + " found more than once in the " // NOI18N
                    + (dimension == HORIZONTAL ? "horizontal":"vertical") // NOI18N
                    + " layout definition. Removing superfluous occurrences."); // NOI18N
            layoutModel.setCorrected();
            return;
        }
        interval.setAlignment(alignment);
        if (linkSizeId != null) {
            layoutModel.addComponentToLinkSizedGroup(integerFromNode(linkSizeId), layoutComponent.getId(), dimension);
        }
        loadSizes(interval, attrMap);
        loadAttributes(interval, attrMap);
        parent.add(interval, -1);
    }
    
    /**
     * Loads size information of the given interval.
     *
     * @param interval layout interval whose size information should be loaded.
     * @param attrMap map with size information.
     */
    private void loadSizes(LayoutInterval interval, org.w3c.dom.NamedNodeMap attrMap) {
        Node minNode = attrMap.getNamedItem(ATTR_SIZE_MIN);
        Node prefNode = attrMap.getNamedItem(ATTR_SIZE_PREF);
        Node maxNode = attrMap.getNamedItem(ATTR_SIZE_MAX);
        int min = (minNode == null) ? NOT_EXPLICITLY_DEFINED : integerFromNode(minNode);
        int pref = (prefNode == null) ? NOT_EXPLICITLY_DEFINED : integerFromNode(prefNode);
        int max = (maxNode == null) ? NOT_EXPLICITLY_DEFINED : integerFromNode(maxNode);
        if (pref != NOT_EXPLICITLY_DEFINED && pref < 0) { // autocorrect invalid pref size (#159536)
            System.err.println("WARNING: Invalid preferred size (" // NOI18N
                    + Integer.toString(pref)
                    + ") of a layout interval encountered, reset to default."); // NOI18N
            pref = NOT_EXPLICITLY_DEFINED;
            layoutModel.setCorrected();
        }
        interval.setSizes(min, pref, max);
        if (max == Short.MAX_VALUE) {
            interval.setLastActualSize(Integer.MIN_VALUE);
        }

        if (interval.isDefaultPadding()) {
            Node paddingNode = attrMap.getNamedItem(ATTR_PADDING_TYPE);
            String paddingStr = paddingNode != null ? paddingNode.getNodeValue() : null;
            PaddingType paddingType = null;
            if (paddingStr != null && !paddingStr.equals(VALUE_PADDING_RELATED)) {
                if (paddingStr.equals(VALUE_PADDING_UNRELATED)) {
                    paddingType = LayoutConstants.PaddingType.UNRELATED;
                } else if (paddingStr.equals(VALUE_PADDING_SEPARATE)) {
                    paddingType = LayoutConstants.PaddingType.SEPARATE;
                } else if (paddingStr.equals(VALUE_PADDING_INDENT)) {
                    paddingType = LayoutConstants.PaddingType.INDENT;
                }
            }
            if (paddingType != null) {
                interval.setPaddingType(paddingType);
                // TBD read components for indent
            }
        }
    }
    
    /**
     * Loads attributes of the given interval.
     *
     * @param interval layout interval whose attributes should be loaded.
     * @param attrMap map with attribute information.
     */
    private void loadAttributes(LayoutInterval interval, org.w3c.dom.NamedNodeMap attrMap) {
        Node attributesNode = attrMap.getNamedItem(ATTR_ATTRIBUTES);
        int attributes = 0;
        if (attributesNode != null) {
            attributes = integerFromNode(attributesNode);
            attributes &= LayoutInterval.ATTR_PERSISTENT_MASK;
        }
        interval.setAttributes(attributes);
    }
    
    /**
     * Extracts integer value from the given node.
     *
     * @param node node that has integer as its value.
     * @return integer value extracted from the given node.
     */
    private static int integerFromNode(Node node) {
        String nodeStr = node.getNodeValue();
        return Integer.parseInt(nodeStr);
    }

    // -----
    // error recovery

    /**
     * This method is used during loading to check the alignment validity of
     * given group and its subintervals. It checks use of BASELINE alignment of
     * the group and the subintervals. Some invalid combinations were allowed by
     * GroupLayout in version 1.0. See issue 78035 for details. This method also
     * fixes the invalid combinations and sets the 'corrected' flag. This is
     * needed for loading because wrong layouts still might exist from the past
     * (and we still can't quite exclude it can't be created even now).
     * BASELINE group can only contain BASELINE intervals, and vice versa,
     * BASELINE interval can only be placed in BASELINE group.
     * BASELINE can be set only on individual components.
     * LEADING, TRAILING and CENTER alignments can be combined freely.
     */
    private void checkAndFixGroup(LayoutInterval group) {
        if (group.isParallel()) {
            int groupAlign = group.getGroupAlignment();
            int baselineCount = 0;

            Iterator iter = group.getSubIntervals();
            while (iter.hasNext()) {
                LayoutInterval subInterval = (LayoutInterval)iter.next();
                if (subInterval.getAlignment() == BASELINE) {
                    if (!subInterval.isComponent()) {
                        subInterval.setAlignment(groupAlign == BASELINE ? LEADING : DEFAULT);
                        layoutModel.setCorrected();
                        System.err.println("WARNING: Invalid use of BASELINE [1], corrected automatically"); // NOI18N
                    }
                    else baselineCount++;
                }
            }

            if (baselineCount > 0) {
                if (baselineCount < group.getSubIntervalCount()) {
                    // separate baseline intervals to a subgroup
                    LayoutInterval subGroup = new LayoutInterval(PARALLEL);
                    subGroup.setGroupAlignment(BASELINE);
                    for (int i=0; i < group.getSubIntervalCount(); ) {
                        LayoutInterval subInterval = group.getSubInterval(i);
                        if (subInterval.getAlignment() == BASELINE) {
                            group.remove(i);
                            subGroup.add(subInterval, -1);
                        }
                        else i++;
                    }
                    if (groupAlign == BASELINE) {
                        group.setGroupAlignment(LEADING);
                    }
                    group.add(subGroup, -1);
                    layoutModel.setCorrected();
                    System.err.println("WARNING: Invalid use of BASELINE [2], corrected automatically"); // NOI18N
                }
                else if (groupAlign != BASELINE) {
                    group.setGroupAlignment(BASELINE);
                    layoutModel.setCorrected();
                    System.err.println("WARNING: Invalid use of BASELINE [3], corrected automatically"); // NOI18N
                }
            }
            else if (groupAlign == BASELINE && group.getSubIntervalCount() > 0) {
                group.setGroupAlignment(LEADING);
                layoutModel.setCorrected();
                System.err.println("WARNING: Invalid use of BASELINE [4], corrected automatically"); // NOI18N
            }
        }
    }

    // The following code tries to fix a missing component name in the layout
    // definition. Due to a bug (see issues 77092, 76749) it may happen that
    // one component in the layout XML has "null" or duplicate name (while it is
    // correct in the metacomponent). If it is the only wrong name in the
    // container then it can be deduced from the map provided for converting
    // names to IDs (it is the only ID not used).

    private final String TEMPORARY_ID = "<temp_id>"; // NOI18N
    private String missingNameH;
    private String missingNameV;

    private String useTemporaryId(String name) throws java.io.IOException {
        if (dimension == HORIZONTAL) {
            if (missingNameH == null && (missingNameV == null || missingNameV.equals(name))) {
                missingNameH = name;
                return TEMPORARY_ID;
            }
        }
        else if (dimension == VERTICAL) {
            if (missingNameV == null && (missingNameH == null || missingNameH.equals(name))) {
                missingNameV = name;
                return TEMPORARY_ID;
            }
        }
        throw new java.io.IOException("Undefined component referenced in layout: "+name); // NOI18N
    }

    private void correctMissingName() throws java.io.IOException {
        if (missingNameH == null && missingNameV == null)
            return; // no problem

        if (missingNameH != null && missingNameV != null && missingNameH.equals(missingNameV)
            && idNameMap.size() == layoutContainer.getSubComponentCount())
        {   // we have one unknown name in each dimension, let's infer it from the idNameMap
            for (Map.Entry<String, String> e : idNameMap.entrySet()) { // name -> id
                String id = e.getValue();
                LayoutComponent comp = layoutModel.getLayoutComponent(id);
                if (comp == null) { // this is the missing id, set it to the temporary component
                    comp = layoutModel.getLayoutComponent(TEMPORARY_ID);
                    layoutModel.changeComponentId(comp, id);
                }
                else if (comp.getParent() == null) { // already loaded as a container
                    // replace the temporary component
                    LayoutComponent tempComp = layoutModel.getLayoutComponent(TEMPORARY_ID);
                    layoutModel.replaceComponent(tempComp, comp);
                }
                else continue;

                layoutModel.setCorrected();
                System.err.println("WARNING: Invalid component name in layout: "+missingNameH // NOI18N
                            +", corrected automatically to: "+e.getKey()); // NOI18N
                return;
            }
        }

        layoutModel.removeComponent(TEMPORARY_ID, true);
        throw new java.io.IOException("Undefined component referenced in layout: " // NOI18N
                + (missingNameH != null ? missingNameH : missingNameV));
    }

    /**
     * This method finds components in given container that have layout interval only
     * in one dimension. Such layout would fail to build in GroupLayout later.
     * The form is considered corrupt, should not open.
     */
    private void checkMissingComponentsInDimension() throws java.io.IOException {
        for (LayoutComponent comp : layoutContainer.getSubcomponents()) {
            for (int dim=0; dim < DIM_COUNT; dim++) {
                LayoutInterval li = comp.getLayoutInterval(dim);
                if (li.getParent() == null) { // component interval not in layout
                    LayoutInterval inOtherDim = comp.getLayoutInterval(dim^1);
                    if (inOtherDim.getParent() != null) {
                        // component is missing in one dimension
                        String id = comp.getId();
                        String name = "unknown";
                        for (Map.Entry<String,String> e : idNameMap.entrySet()) {
                            if (e.getValue().equals(id)) {
                                name = e.getKey(); // idNameMap maps name->id during loading
                                break;
                            }
                        }
                        throw new java.io.IOException("Layout corrupted, component "+name+" missing in one dimension."); // NOI18N
                    }
                }
            }            
        }
    }
}
