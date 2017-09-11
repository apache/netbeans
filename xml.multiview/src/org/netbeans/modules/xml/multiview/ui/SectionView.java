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

package org.netbeans.modules.xml.multiview.ui;

import javax.swing.JPanel;
import java.awt.*;

import org.openide.nodes.Node;
import org.netbeans.modules.xml.multiview.cookies.SectionFocusCookie;

/**
 * This class acts as a container for <code>NodeSectionPanel</code>s. Generally
 * used with {@link org.netbeans.modules.xml.multiview.ui.SectionPanel}.
 *
 * @author mkuchtiak
 */
public class SectionView extends PanelView implements SectionFocusCookie, ContainerPanel {
    private JPanel scrollPanel, filler;
    javax.swing.JScrollPane scrollPane;
    private java.util.Hashtable map;
    private int sectionCount=0;
    private NodeSectionPanel activePanel;
    private InnerPanelFactory factory = null;
    boolean sectionSelected;
    
    
    /**
     * Constructs a new SectionView.
     * @param factory the factory for creating inner panels.
     */ 
    public SectionView(InnerPanelFactory factory) {
        super();
        this.factory=factory;
    }
    
    /**
     * Constructs a new SectionView.
     */
    public SectionView() {
        super();
    }
    
    public void initComponents() {
        super.initComponents();
        map = new java.util.Hashtable();
        setLayout(new java.awt.BorderLayout());
        scrollPanel = new JPanel();
        scrollPanel.setLayout(new java.awt.GridBagLayout());
        scrollPane = new javax.swing.JScrollPane();
        scrollPane.setViewportView(scrollPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        filler = new JPanel();
        // issue 233048: the background color issues with dark metal L&F
        // filler.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Opens and activates the given <code>panel</code>.
     */
    public boolean focusSection(NodeSectionPanel panel) {
        panel.open();
        openParents((JPanel)panel);
        panel.scroll();
        setActivePanel(panel);
        panel.setActive(true);
        return true;
    }
    
    protected void openSection(Node node){
        NodeSectionPanel panel = (NodeSectionPanel) map.get(node);
        if (panel != null) {
            focusSection(panel);
        }
    }
    
    private void openParents(JPanel panel){
        javax.swing.JScrollPane scrollP = null;
        NodeSectionPanel parentSection=null;
        java.awt.Container ancestor = panel.getParent();
        while (ancestor !=null && scrollP == null){
            if (ancestor instanceof javax.swing.JScrollPane){
                scrollP = (javax.swing.JScrollPane) ancestor;
            }
            if (ancestor instanceof NodeSectionPanel){
                parentSection = (NodeSectionPanel) ancestor;
                parentSection.open();
            }
            ancestor = ancestor.getParent();
        }
    }
    
    void mapSection(Node key, NodeSectionPanel panel){
        map.put(key,panel);
    }
    
    void deleteSection(Node key){
        map.remove(key);
    }
    
    /**
     * Gets the corresponding <code>NodeSectionPanel</code> for the
     * given <code>key</code>.
     * @return the corresponding panel or null.
     */
    public NodeSectionPanel getSection(Node key){
        return (NodeSectionPanel)map.get(key);
    }
    
    /**
     * Adds a section for this.
     * @param section the section to be added.
     * @param open indicates whether given <code>section</code>
     * should be opened.
     */
    public void addSection(NodeSectionPanel section, boolean open) {
        addSection(section);
        if (open) {
            section.open();
            section.scroll();
            section.setActive(true);
        }
    }
    
    /**
     * Adds a section for this.
     * @param section the section to be added.
     */
    public void addSection(NodeSectionPanel section) {
        scrollPanel.remove(filler);
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = sectionCount;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        //gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 6);
        scrollPanel.add((JPanel)section,gridBagConstraints);
        section.setIndex(sectionCount);
        
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = sectionCount+1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 2.0;
        //gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 6);
        scrollPanel.add(filler,gridBagConstraints);
        
        mapSection(section.getNode(), section);
        sectionCount++;
    }
    /**
     * Removes given <code>node</code> and its corresponding
     * section.
     */
    public void removeSection(Node node) {
        NodeSectionPanel section = getSection(node);
        if (section!=null) {
            // looking for enclosing container
            java.awt.Container cont = ((java.awt.Component)section).getParent();
            while (cont!=null && !(cont instanceof ContainerPanel)) {
                cont = cont.getParent();
            }
            if ( cont!= null) {
                // removing last active component
                ContainerPanel contPanel = (ContainerPanel)cont;
                if (section instanceof SectionPanel) {
                    Object key = ((SectionPanel)section).getKey();
                    if (key!=null && key==getLastActive()) {
                        setLastActive(null);
                    }
                }
                // removing section
                contPanel.removeSection(section);
                // removing node
                contPanel.getRoot().getChildren().remove(new Node[]{node});
            }
        }
    }
    
    /**
     * Removes given <code>panel</code> and moves up remaining panels.
     */
    public void removeSection(NodeSectionPanel panel){
        int panelIndex = panel.getIndex();
        scrollPanel.remove((JPanel)panel);
        
        // the rest components have to be moved up
        java.awt.Component[] components = scrollPanel.getComponents();
        java.util.AbstractList removedPanels = new java.util.ArrayList();
        for (int i=0;i<components.length;i++) {
            if (components[i] instanceof NodeSectionPanel) {
                NodeSectionPanel pan = (NodeSectionPanel)components[i];
                int index = pan.getIndex();
                if (index>panelIndex) {
                    scrollPanel.remove((JPanel)pan);
                    pan.setIndex(index-1);
                    removedPanels.add(pan);
                }
            }
        }
        for (int i=0;i<removedPanels.size();i++) {
            NodeSectionPanel pan = (NodeSectionPanel)removedPanels.get(i);
            java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = pan.getIndex();
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            //gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 6);
            scrollPanel.add((JPanel)pan,gridBagConstraints);
        }
        deleteSection(panel.getNode());
        sectionCount--;
    }
    
    /**
     * Sets given <code>activePanel</code> as the currently active panel.
     */
    public void setActivePanel(NodeSectionPanel activePanel) {
        if (this.activePanel!=null && this.activePanel!=activePanel) {
            this.activePanel.setActive(false);
        }
        this.activePanel = activePanel;
        if (activePanel instanceof SectionPanel) {
            setLastActive(((SectionPanel)activePanel).getKey());
        }
    }
    
    public NodeSectionPanel getActivePanel() {
        return activePanel;
    }
    
    public void selectNode(Node node) {
        setManagerSelection(new Node[]{node});
    }
    
    /**
     * Opens the panels that are associated with the given
     * <code>nodes</code>.
     */
    public void showSelection(org.openide.nodes.Node[] nodes) {
        if (sectionSelected) {
            sectionSelected=false;
            return;
        }
        if (nodes!=null && nodes.length>0) {
            openSection(nodes[0]);
        }
    }
    
    void sectionSelected(boolean sectionSelected) {
        this.sectionSelected=sectionSelected;
    }
    
    protected org.netbeans.modules.xml.multiview.Error validateView() {
        return null;
    }
    
    /**
     * @return panel with the given <code>key</code> or null
     * if no matching panel was found.
     */
    public SectionPanel findSectionPanel(Object key) {
        java.util.Enumeration en = map.keys();
        while (en.hasMoreElements()) {
            NodeSectionPanel pan = (NodeSectionPanel)map.get(en.nextElement());
            if (pan instanceof SectionPanel) {
                SectionPanel p = (SectionPanel)pan;
                if (key==p.getKey()) {
                    return p;
                }
            }
        }
        return null;
    }
    
    InnerPanelFactory getInnerPanelFactory() {
        return factory;
    }
    
    public void setInnerPanelFactory(InnerPanelFactory factory) {
        this.factory=factory;
    }
    
    /**
     * Opens the panel identified by given <code>key</code>.
     */
    public void openPanel(Object key) {
        if (key!=null) {
            SectionPanel panel = findSectionPanel(key);
            if (panel!=null) {
                if (panel.getInnerPanel()==null) panel.open();
                openParents((JPanel)panel);
                panel.scroll();
                panel.setActive(true);
            }
        }
    }
    
    private Object getLastActive() {
        ToolBarDesignEditor toolBarDesignEditor = getToolBarDesignEditor();
        return toolBarDesignEditor == null ? null : toolBarDesignEditor.getLastActive();
    }
    
    private void setLastActive(Object key) {
        ToolBarDesignEditor toolBarDesignEditor = getToolBarDesignEditor();
        if(toolBarDesignEditor != null) {
            toolBarDesignEditor.setLastActive(key);
        }
    }
    
    protected ToolBarDesignEditor getToolBarDesignEditor() {
        Container parent = getParent();
        return parent == null ? null : (ToolBarDesignEditor) parent.getParent();
    }
}
