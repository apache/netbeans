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

import java.awt.*;

import javax.swing.*;
import org.openide.nodes.Node;

import org.netbeans.modules.xml.multiview.Utils;
import org.openide.awt.Mnemonics;

/**
 * A container for <code>NodeSectionPanel</code>s. Provides fold button and support
 * for defining "header actions", i.e. top level actions by means of <code>setHeaderActions()</code>
 * method.
 * 
 *
 * @author  mkuchtiak
 */
public class SectionContainer extends javax.swing.JPanel implements NodeSectionPanel, ContainerPanel {

    //private HashMap map = new HashMap();
    //private JScrollPane scrollPane;
    private Node activeNode=null;
    private SectionView sectionView;
    private String title;
    private Node root;
    private boolean active;
    private int sectionCount=0;
    private int index;
    private boolean foldable;

    public SectionContainer(SectionView sectionView, Node root, String title) {
        this(sectionView, root, title, true);
    }
    
    public SectionContainer(SectionView sectionView, Node root, String title, boolean foldable) {
        this.sectionView=sectionView;
        this.root=root;
        this.foldable=foldable;
        initComponents();
        // issue 233048: the background color issues with dark metal L&F
        //setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        //titlePanel.setBackground(foldable?SectionVisualTheme.getSectionHeaderColor():SectionVisualTheme.getContainerHeaderColor());
        //actionPanel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        headerSeparator.setForeground(SectionVisualTheme.getSectionHeaderLineColor());
        fillerLine.setForeground(SectionVisualTheme.getFoldLineColor());
        fillerEnd.setForeground(SectionVisualTheme.getFoldLineColor());
        Mnemonics.setLocalizedText(titleButton, title);
        titleButton.setToolTipText(titleButton.getText());
        this.title=titleButton.getText();
        titleButton.addMouseListener(new org.openide.awt.MouseUtils.PopupMouseAdapter() {
            protected void showPopup(java.awt.event.MouseEvent e) {
                JPopupMenu popup = getNode().getContextMenu();
                popup.show(titleButton,e.getX(), e.getY());
            }
        });
        
        if (foldable) {
            foldButton.setSelected(true);
        } else {
            remove(fillerLine);
            remove(fillerEnd);
            remove(foldButton);
        }
        setIcon(true);
    }
    
    /** Method from NodeSectionPanel interface */
    public Node getNode() {
        return root; 
    }
    /** Method from ContainerPanel interface */
    public Node getRoot() {
        return root; 
    }
    
    /** Method from NodeSectionPanel interface */
    public void open(){
        if (foldable) {
            foldButton.setSelected(true);
            contentPanel.setVisible(true);
            fillerLine.setVisible(true);
            fillerEnd.setVisible(true);
            setIcon(true);
        }
    }

    /** Method from NodeSectionPanel interface */
    public void scroll() {
        Utils.scrollToVisible(this);
    }
    
    /** Method from NodeSectionPanel interface */
    public void setActive(boolean active) {
        // issue 233048: the background color issues with dark metal L&F
        // titlePanel.setBackground(active?SectionVisualTheme.getSectionHeaderActiveColor():
        //      (foldable?SectionVisualTheme.getSectionHeaderColor():SectionVisualTheme.getContainerHeaderColor()));
        if (active && !this.equals(sectionView.getActivePanel())) {
            sectionView.sectionSelected(true);
            sectionView.setActivePanel(this);
            sectionView.selectNode(root);
        }
        this.active=active;
    }
    
    /** Method from NodeSectionPanel interface */
    public boolean isActive() {
        return active;
    }

    /** Maps section to a node
    */
    private void mapSection(Node key, NodeSectionPanel panel){
        sectionView.mapSection(key,panel);
    }
    
    /** Maps section to a node
    */
    private void deleteSection(Node key){
        sectionView.deleteSection(key);
    }
    
    /** Method from ContainerPanel interface */
    public NodeSectionPanel getSection(Node key){
        return sectionView.getSection(key);
    }
    
    public void addSection(NodeSectionPanel section, boolean open) {
        addSection(section);
        if (open) {
            section.open();
            section.scroll();
            section.setActive(true);
        }
    }
    
    /** Method from ContainerPanel interface */
    public void addSection(NodeSectionPanel section){
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = sectionCount;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        contentPanel.add((JPanel)section,gridBagConstraints);  
        section.setIndex(sectionCount);
        mapSection(section.getNode(), section);
        sectionCount++;
    }
    /** Method from ContainerPanel interface */
    public void removeSection(NodeSectionPanel section) {
        int panelIndex = section.getIndex();
        contentPanel.remove((JPanel)section);
        
        // the rest components have to be moved up
        java.awt.Component[] components = contentPanel.getComponents();
        java.util.AbstractList removedPanels = new java.util.ArrayList(); 
        for (int i=0;i<components.length;i++) {
            if (components[i] instanceof NodeSectionPanel) {
                NodeSectionPanel pan = (NodeSectionPanel)components[i];
                int index = pan.getIndex();
                if (index>panelIndex) {
                    contentPanel.remove((JPanel)pan);
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
            contentPanel.add((JPanel)pan,gridBagConstraints);
        }
        deleteSection(section.getNode());
        sectionCount--;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        foldButton = new javax.swing.JToggleButton();
        headerSeparator = new javax.swing.JSeparator();
        contentPanel = new javax.swing.JPanel();
        actionPanel = new javax.swing.JPanel();
        fillerLine = new javax.swing.JSeparator();
        fillerEnd = new javax.swing.JSeparator();
        titlePanel = new javax.swing.JPanel();
        titleButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        foldButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/xml/multiview/resources/plus.gif"))); // NOI18N
        foldButton.setToolTipText(org.openide.util.NbBundle.getMessage(SectionContainer.class, "HINT_FOLD_BUTTON")); // NOI18N
        foldButton.setBorder(null);
        foldButton.setBorderPainted(false);
        foldButton.setContentAreaFilled(false);
        foldButton.setFocusPainted(false);
        foldButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/xml/multiview/resources/minus.gif"))); // NOI18N
        foldButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                foldButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 2);
        add(foldButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        add(headerSeparator, gridBagConstraints);

        contentPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        add(contentPanel, gridBagConstraints);

        actionPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 2, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(actionPanel, gridBagConstraints);

        fillerLine.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(fillerLine, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 2);
        add(fillerEnd, gridBagConstraints);

        titlePanel.setLayout(new java.awt.BorderLayout());

        titleButton.setFont(titleButton.getFont().deriveFont(titleButton.getFont().getStyle() | java.awt.Font.BOLD, titleButton.getFont().getSize()+2));
        titleButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        titleButton.setBorderPainted(false);
        titleButton.setContentAreaFilled(false);
        titleButton.setFocusPainted(false);
        titleButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        titleButton.setMargin(new java.awt.Insets(0, 4, 0, 4));
        titleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                titleButtonActionPerformed(evt);
            }
        });
        titlePanel.add(titleButton, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(titlePanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void titleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_titleButtonActionPerformed
        // TODO add your handling code here:
        if (foldable) {
            if (!foldButton.isSelected()) {
                open();
                foldButton.setSelected(true);
            } else {
                if (isActive()) {
                    foldButton.setSelected(false);
                    contentPanel.setVisible(false);
                    fillerLine.setVisible(false);
                    fillerEnd.setVisible(false);
                    setIcon(false);
                }
            }
        }
        if (!isActive()) setActive(true);
    }//GEN-LAST:event_titleButtonActionPerformed

    private void foldButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_foldButtonActionPerformed
        // TODO add your handling code here:
        contentPanel.setVisible(foldButton.isSelected());
        fillerLine.setVisible(foldButton.isSelected());
        fillerEnd.setVisible(foldButton.isSelected());
        setIcon(foldButton.isSelected());
    }//GEN-LAST:event_foldButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actionPanel;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JSeparator fillerEnd;
    private javax.swing.JSeparator fillerLine;
    private javax.swing.JToggleButton foldButton;
    private javax.swing.JSeparator headerSeparator;
    private javax.swing.JButton titleButton;
    private javax.swing.JPanel titlePanel;
    // End of variables declaration//GEN-END:variables
    
    /** Method from NodeSectionPanel interface */
    public void setIndex(int index) {
        this.index=index;
    }
    
    /** Method from NodeSectionPanel interface */
    public int getIndex() {
        return index;
    }
    
    private javax.swing.JButton[] headerButtons;
    
    public void setHeaderActions(Action[] actions) {
        headerButtons = new javax.swing.JButton[actions.length];
        for (int i=0;i<actions.length;i++) {
            headerButtons[i] = new javax.swing.JButton(actions[i]);
            headerButtons[i].setMargin(new java.awt.Insets(0,14,0,14));
            headerButtons[i].setOpaque(false);
            actionPanel.add(headerButtons[i]);
        }
    }
    
    public javax.swing.JButton[] getHeaderButtons(){
        return headerButtons;
    }
    
    public boolean isFoldable() {
        return foldable;
    }
    
    private void setIcon(boolean opened) {
        java.awt.Image image=null;
        if (opened)
            image = (root == null ? null : root.getOpenedIcon(java.beans.BeanInfo.ICON_COLOR_16x16));
        else
            image = (root == null ? null : root.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16));
        if (image != null) {
            titleButton.setIcon(new javax.swing.ImageIcon(image));
        }
    }

    
}
