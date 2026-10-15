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

import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import org.netbeans.modules.form.layoutdesign.LayoutComponent;
import org.netbeans.modules.form.layoutdesign.LayoutConstants;
import org.netbeans.modules.form.layoutdesign.LayoutInterval;
import org.netbeans.modules.form.layoutdesign.LayoutModel;

/**
 * Generates Java layout code based on the passed layout model.
 *
 * @author Jan Stola
 */
public class SwingLayoutCodeGenerator {
    private static final String LAYOUT_VAR_NAME = "layout"; // NOI18N
    private String layoutVarName;
    private boolean useLayoutLibrary;

    /**
     * Maps from component ID to <code>ComponentInfo</code>.
     */
    private Map<String,ComponentInfo> componentIDMap;

    /**
     * To workaround some cases of default ending container gaps
     * GroupLayout does not like.
     */
    private Set<LayoutInterval> unsupportedContResGaps;

    /**
     * Creates new <code>SwingLayoutCodeGenerator</code>.
     *
     * @param layoutModel layout model of the form.
     */
    public SwingLayoutCodeGenerator(LayoutModel layoutModel) {
        componentIDMap = new HashMap<String,ComponentInfo>();
    }

    /**
     * Generates Java layout code for the specified container. The generated
     * code is written to the <code>writer</code>.
     *
     * @param writer the writer to generate the code into.
     * @param container the container whose code should be generated.
     * @param contExprStr code expression representing the container.
     * @param contVarName variable name of the container, used to derive the
     *        local variable name for the layout instance
     * @param infos data about subcomponents.
     * @param useLibrary whether to use swing-layout library or Java 6 code
     */
    public void generateContainerLayout(Writer writer, LayoutComponent container,
            String contExprStr, String contVarName, ComponentInfo infos[],
            boolean useLibrary)
        throws IOException
    {
        useLayoutLibrary = useLibrary;
        if (contVarName == null) {
            layoutVarName = LAYOUT_VAR_NAME;
        } else {
            layoutVarName = contVarName + Character.toUpperCase(LAYOUT_VAR_NAME.charAt(0))
                + LAYOUT_VAR_NAME.substring(1);
        }
        unsupportedContResGaps = null;
        fillMap(infos);
        generateInstantiation(writer, contExprStr);

        LayoutInterval[][] extraRoots;
        int rootCount = container.getLayoutRootCount();
        if (rootCount > 1) {
            // prepare generating multiple roots into one group
            extraRoots = new LayoutInterval[LayoutConstants.DIM_COUNT][rootCount-1];
            for (int i=1; i < rootCount; i++) {
                for (int dim=0; dim < LayoutConstants.DIM_COUNT; dim++) {
                    extraRoots[dim][i-1] = container.getLayoutRoot(i, dim);
                    assert extraRoots[dim][i-1].isParallel();
                }
            }
        } else {
            extraRoots = null;
        }
        for (int dim=0; dim < LayoutConstants.DIM_COUNT; dim++) {
            StringBuilder sb = new StringBuilder();
            composeGroup(sb, container.getLayoutRoot(0, dim),
                         extraRoots != null ? extraRoots[dim] : null,
                         true, true);
            writer.write(layoutVarName
                    + (dim == LayoutConstants.HORIZONTAL ? ".setHorizontalGroup(\n" : ".setVerticalGroup(\n") // NOI18N
                    + sb.toString() + "\n);\n"); // NOI18N

            sb = new StringBuilder();
            composeLinks(sb, container, layoutVarName, dim);
            writer.write(sb.toString());
        }
        unsupportedContResGaps = null;
    }                                       

    /**
     * Fills the <code>componentIDMap</code>.
     *
     * @param infos information about components.
     */
    private void fillMap(ComponentInfo[] infos) {
        for (int counter = 0; counter < infos.length; counter++) {
            componentIDMap.put(infos[counter].id, infos[counter]);
        }
    }
    
    /**
     * Generates the "header" of the code e.g. instantiation of the layout
     * and call to the <code>setLayout</code> method.
     */
    private void generateInstantiation(Writer writer, String contExprStr) throws IOException {
        writer.write(getLayoutName() + " " + layoutVarName + " "); // NOI18N
        writer.write("= new " + getLayoutName() + "(" + contExprStr + ");\n"); // NOI18N
        writer.write(contExprStr + ".setLayout(" + layoutVarName + ");\n"); // NOI18N
    }
    
    /**
     * Generates layout code for a group that corresponds
     * to the <code>interval</code>.
     *
     * @param layout buffer to generate the code into.
     * @param interval layout model of the group.
     */
    private void composeGroup(StringBuilder layout,
                              LayoutInterval group, LayoutInterval[] extraGroups,
                              boolean first, boolean last)
        throws IOException
    {
        int groupAlignment = group.getGroupAlignment();
        if (group.isParallel()) {
            boolean notResizable = group.getMaximumSize() == LayoutConstants.USE_PREFERRED_SIZE;
            String alignmentStr = convertAlignment(groupAlignment);
            layout.append(layoutVarName).append(".createParallelGroup("); // NOI18N
            layout.append(alignmentStr);
            if (notResizable) {
                layout.append(", false"); // NOI18N
            }
            layout.append(")"); // NOI18N
        } else {
            layout.append(layoutVarName).append(".createSequentialGroup()"); // NOI18N
        }

        Iterator subIntervals = group.getSubIntervals();
        while (subIntervals.hasNext()) {
            layout.append("\n"); // NOI18N
            LayoutInterval subInterval = (LayoutInterval)subIntervals.next();
            fillGroup(layout, subInterval, first,
                      last && (!group.isSequential() || (!subIntervals.hasNext() && extraGroups == null)),
                      groupAlignment);
            if (first && group.isSequential()) {
                first = false;
            }
        }
        if (extraGroups != null) {
            for (LayoutInterval g : extraGroups) {
                layout.append("\n"); // NOI18N
                fillGroup(layout, g, first, last, groupAlignment);
                // assuming extra groups are always parallel 
            }
        }
    }
    
    /**
     * Generate layout code for one element in the group.
     *
     * @param layout buffer to generate the code into.
     * @param interval layout model of the element.
     * @param groupAlignment alignment of the enclosing group.
     */
    private void fillGroup(StringBuilder layout, LayoutInterval interval,
        boolean first, boolean last, int groupAlignment) throws IOException {
        if (interval.isGroup()) {
            layout.append(getAddGroupStr());
            int alignment = interval.getAlignment();
            if (interval.isSequential() && last) {
                LayoutInterval contResGap = SwingLayoutUtils.getUnsupportedResizingContainerGap(interval);
                if (contResGap != null) { // GroupLayout bug workaround - will use fixed gap instead
                    addUnsupportedContResGap(contResGap);
                    alignment = LayoutConstants.LEADING;
                }
            }
            if ((alignment != LayoutConstants.DEFAULT) && interval.getParent().isParallel() && alignment != groupAlignment
                    && alignment != LayoutConstants.BASELINE && groupAlignment != LayoutConstants.BASELINE) {
                String alignmentStr = convertAlignment(alignment);
                layout.append(alignmentStr).append(", "); // NOI18N
            }
            composeGroup(layout, interval, null, first, last);
        } else {
            int min = interval.getMinimumSize();
            int pref = interval.getPreferredSize();
            int max = interval.getMaximumSize();
            if (interval.isComponent()) {
                layout.append(getAddComponentStr());
                int alignment = interval.getAlignment();
                LayoutComponent layoutComp = interval.getComponent();
                ComponentInfo info = componentIDMap.get(layoutComp.getId());
                boolean horizontal = layoutComp.getLayoutInterval(LayoutConstants.HORIZONTAL) == interval;
                if (min == LayoutConstants.NOT_EXPLICITLY_DEFINED) {
                    if (horizontal && info.component.getClass().getName().equals("javax.swing.JComboBox")) { // Issue 68612 // NOI18N
                        min = 0;
                    } else if (pref >= 0) {
                        Dimension minSize = info.component.getMinimumSize();
                        int compMin = horizontal ? minSize.width : minSize.height;
                        if (compMin > pref) {
                            min = LayoutConstants.USE_PREFERRED_SIZE;
                        }
                    }
                }
                // workaround for bug in GroupLayout that does not align properly on baseline
                // if some component has 0 preferred width (even if actual size is bigger)
                if (pref == 0 && max >= Short.MAX_VALUE && horizontal
                        && layoutComp.getLayoutInterval(LayoutConstants.VERTICAL).getAlignment() == LayoutConstants.BASELINE) {
                    pref = 1;
                }
                assert (info.variableName != null);
                if (interval.getParent().isSequential() || (alignment == LayoutConstants.DEFAULT) || (alignment == groupAlignment)
                        || alignment == LayoutConstants.BASELINE || groupAlignment == LayoutConstants.BASELINE) {
                    layout.append(info.variableName);
                } else {
                    String alignmentStr = convertAlignment(alignment);
                    if (useLayoutLibrary())
                        layout.append(alignmentStr).append(", ").append(info.variableName); // NOI18N
                    else // in JDK the component comes first
                        layout.append(info.variableName).append(", ").append(alignmentStr); // NOI18N
                }
                int status = SwingLayoutUtils.getResizableStatus(info.component);
                
                if (!((pref == LayoutConstants.NOT_EXPLICITLY_DEFINED) &&
                    ((min == LayoutConstants.NOT_EXPLICITLY_DEFINED)
                     || ((min == LayoutConstants.USE_PREFERRED_SIZE)
                        && !info.sizingChanged
                        && (status == SwingLayoutUtils.STATUS_NON_RESIZABLE))) &&
                    ((max == LayoutConstants.NOT_EXPLICITLY_DEFINED)
                     || ((max == LayoutConstants.USE_PREFERRED_SIZE)
                        && !info.sizingChanged
                        && (status == SwingLayoutUtils.STATUS_NON_RESIZABLE))
                     || ((max == Short.MAX_VALUE)
                        && !info.sizingChanged
                        && (status == SwingLayoutUtils.STATUS_RESIZABLE))))) {                
                    layout.append(", "); // NOI18N
                    generateSizeParams(layout, min, pref, max);
                }
            } else if (interval.isEmptySpace()) {
                boolean preferredGap;
                LayoutConstants.PaddingType gapType = interval.getPaddingType();
                if (interval.isDefaultPadding()) {
                    if (gapType != null && gapType == LayoutConstants.PaddingType.SEPARATE) {
                        // special case - SEPARATE padding not known by LayoutStyle
                        preferredGap = false;
                        if (min == LayoutConstants.NOT_EXPLICITLY_DEFINED) {
                            min = SwingLayoutBuilder.PADDING_SEPARATE_VALUE;
                        }
                        if (pref == LayoutConstants.NOT_EXPLICITLY_DEFINED) {
                            pref = SwingLayoutBuilder.PADDING_SEPARATE_VALUE;
                        }
                    } else {
                        preferredGap = true;
                    }
                } else {
                    preferredGap = false;
                }
                if (preferredGap) {
                    if (first || last) {
                        layout.append(getAddContainerGapStr());
                    } else {
                        layout.append(getAddPreferredGapStr());
                        if (gapType == LayoutConstants.PaddingType.INDENT) {
                            // TBD: comp1, comp2
                            pref = max = LayoutConstants.NOT_EXPLICITLY_DEFINED; // always fixed
                        }
                        layout.append(getPaddingTypeStr(gapType));
                    }
                    if ((pref != LayoutConstants.NOT_EXPLICITLY_DEFINED)
                        || (max != LayoutConstants.NOT_EXPLICITLY_DEFINED // NOT_EXPLICITLY_DEFINED is the same as USE_PREFERRED_SIZE in this case
                            && max != LayoutConstants.USE_PREFERRED_SIZE
                            && (!last || !isUnsupportedContResGap(interval)))) { // workaround GroupLayout bug
                        if (!first && !last) {
                            layout.append(',').append(' ');
                        }
                        layout.append(convertSize(pref)).append(", "); // NOI18N
                        layout.append(convertSize(max));
                    }
                } else {
                    if (min == LayoutConstants.USE_PREFERRED_SIZE) {
                        min = pref;
                    }
                    if (max == LayoutConstants.USE_PREFERRED_SIZE) {
                        max = pref;
                    }
                    layout.append(getAddGapStr());
                    if (min < 0) min = pref; // min == GroupLayout.PREFERRED_SIZE
                    min = Math.min(pref, min);
                    max = Math.max(pref, max);
                    generateSizeParams(layout, min, pref, max);
                }
            } else {
                assert false;
            }
        }
        layout.append(")"); // NOI18N
    }
    
    /**
     * Generates minimum/preferred/maximum size parameters..
     *
     * @param layout buffer to generate the code into.
     * @param min minimum size.
     * @param pref preffered size.
     * @param max maximum size.
     */
    private void generateSizeParams(StringBuilder layout, int min, int pref, int max) {
        layout.append(convertSize(min)).append(", "); // NOI18N
        layout.append(convertSize(pref)).append(", "); // NOI18N
        layout.append(convertSize(max));
    }

    /**
     * Converts alignment from the layout model constants
     * to <code>GroupLayout</code> constants.
     *
     * @param alignment layout model alignment constant.
     * @return <code>GroupLayout</code> alignment constant that corresponds
     * to the given layout model one.
     */
    private String convertAlignment(int alignment) {
        String groupAlignment = null;
        switch (alignment) {
            case LayoutConstants.LEADING: groupAlignment = "LEADING"; break; // NOI18N
            case LayoutConstants.TRAILING: groupAlignment = "TRAILING"; break; // NOI18N
            case LayoutConstants.CENTER: groupAlignment = "CENTER"; break; // NOI18N
            case LayoutConstants.BASELINE: groupAlignment = "BASELINE"; break; // NOI18N
            default: assert false; break;
        }
        return useLayoutLibrary() ?
            getLayoutName() + "." + groupAlignment : // NOI18N
            getLayoutName() + ".Alignment." + groupAlignment; // NOI18N
    }
    
    /**
     * Converts minimum/preferred/maximums size from the layout model constants
     * to <code>GroupLayout</code> constants.
     *
     * @param size minimum/preferred/maximum size from layout model.
     * @return minimum/preferred/maximum size or <code>GroupLayout</code> constant
     * that corresponds to the given layout model one.
     */
    private String convertSize(int size) {
        String convertedSize;
        switch (size) {
            case LayoutConstants.NOT_EXPLICITLY_DEFINED: convertedSize = getLayoutName() + ".DEFAULT_SIZE"; break; // NOI18N
            case LayoutConstants.USE_PREFERRED_SIZE: convertedSize = getLayoutName() + ".PREFERRED_SIZE"; break; // NOI18N
            case Short.MAX_VALUE: convertedSize = "Short.MAX_VALUE"; break; // NOI18N
            default: convertedSize = (size >= 0) ? Integer.toString(size) : getLayoutName() + ".DEFAULT_SIZE"; break; // NOI18N
        }
        return convertedSize;
    }

    private void composeLinks(StringBuilder layout, LayoutComponent containerLC, String layoutVarName, int dimension) throws IOException {

        Map<Integer,List<String>> linkSizeGroups = SwingLayoutUtils.createLinkSizeGroups(containerLC, dimension);
        
        Collection<List<String>> linkGroups = linkSizeGroups.values();
        Iterator<List<String>> linkGroupsIt = linkGroups.iterator();
        while (linkGroupsIt.hasNext()) {
            List<String> l = linkGroupsIt.next();
            // sort so that the generated line is always the same when no changes were made
            l.sort(new Comparator<String>() {
                @Override
                public int compare(String id1, String id2) {
                    ComponentInfo info1 = componentIDMap.get(id1);
                    ComponentInfo info2 = componentIDMap.get(id2);                    
                    return info1.variableName.compareTo(info2.variableName);
                }
            });
            if (l.size() > 1) {
                layout.append("\n\n").append(layoutVarName).append(".linkSize("); // NOI18N
                if (!useLayoutLibrary()) {
                    layout.append("javax.swing.SwingConstants"); // NOI18N
                    layout.append(dimension == LayoutConstants.HORIZONTAL ?
                                  ".HORIZONTAL, " : ".VERTICAL, "); // NOI18N
                }
                layout.append("new java.awt.Component[] {"); //NOI18N
                Iterator i = l.iterator();
                boolean first = true;
                while (i.hasNext()) {
                    String cid = (String)i.next();
                    ComponentInfo info = componentIDMap.get(cid);
                    if (first) {
                        first = false;
                        layout.append(info.variableName);
                    } else {
                        layout.append(", ").append(info.variableName); // NOI18N
                    }
                }
                layout.append( "}"); // NOI18N
                if (useLayoutLibrary()) {
                    layout.append( ", "); // NOI18N
                    layout.append(getLayoutName());
                    layout.append(dimension == LayoutConstants.HORIZONTAL ?
                                  ".HORIZONTAL" : ".VERTICAL"); // NOI18N
                }
                layout.append(");\n\n"); // NOI18N
            }
        }
    }

    /**
     * Information about one component.
     */
    public static class ComponentInfo {
        /** ID of the component. */
        public String id;
        /** Variable name of the component. */
        public String variableName;
        /** The component's class. */
        public Component component;
        /**
         * Determines whether size properties (e.g. minimumSize, preferredSize
         * or maximumSize properties of the component has been changed).
         */
        public boolean sizingChanged;
    }

    // -----
    // type of generated code: swing-layout library vs JDK

    boolean useLayoutLibrary() {
        return useLayoutLibrary;
    }

    private String getLayoutName() {
        return useLayoutLibrary() ? "org.jdesktop.layout.GroupLayout" : "javax.swing.GroupLayout"; // NOI18N
    }

    private String getLayoutStyleName() {
        return useLayoutLibrary() ? "org.jdesktop.layout.LayoutStyle" : "javax.swing.LayoutStyle"; // NOI18N
    }

    private String getAddComponentStr() {
        return useLayoutLibrary() ? ".add(" : ".addComponent("; // NOI18N
    }

    private String getAddGapStr() {
        return useLayoutLibrary() ? ".add(" : ".addGap("; // NOI18N
    }

    private String getAddPreferredGapStr() {
        return ".addPreferredGap("; // NOI18N
    }

    private String getAddContainerGapStr() {
        return ".addContainerGap("; // NOI18N
    }

    private String getAddGroupStr() {
        return useLayoutLibrary() ? ".add(" : ".addGroup("; // NOI18N
    }

    private String getPaddingTypeStr(LayoutConstants.PaddingType paddingType) {
        String str;
        if (paddingType == null || paddingType == LayoutConstants.PaddingType.RELATED) {
            str = ".RELATED"; // NOI18N
        } else if (paddingType == LayoutConstants.PaddingType.UNRELATED) {
            str = ".UNRELATED"; // NOI18N
        } else if (paddingType == LayoutConstants.PaddingType.INDENT) {
            str = ".INDENT"; // NOI18N
        } else {
            return null;
        }
        
        return getLayoutStyleName() + (useLayoutLibrary ? "" : ".ComponentPlacement") // NOI18N
                + str;
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
