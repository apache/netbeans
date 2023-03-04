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

package org.netbeans.modules.diff.options;

import org.netbeans.modules.options.colors.spi.FontsColorsController;
import org.netbeans.modules.options.colors.ColorModel;
import org.netbeans.modules.diff.DiffModuleConfig;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.openide.util.NbBundle;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.util.*;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.spi.options.OptionsPanelController;

/**
 * Copied from org.netbeans.modules.options.colors.HighlightingPanel
 * 
 * copied from editor/options.
 * @author Maros Sandor
 */
@OptionsPanelController.Keywords(keywords={"diff colors", "#KW_DiffOptions"}, location=OptionsDisplayer.FONTSANDCOLORS, tabTitle= "#LBL_DiffOptions")
public class DiffColorsPanel extends javax.swing.JPanel implements ActionListener, FontsColorsController {
    
    private static final String ATTR_NAME_ADDED = "added";
    private static final String ATTR_NAME_DELETED = "deleted";
    private static final String ATTR_NAME_CHANGED = "changed";

    private static final String ATTR_NAME_MERGE_UNRESOLVED = "merge.unresolved";
    private static final String ATTR_NAME_MERGE_APPLIED = "merge.applied";
    private static final String ATTR_NAME_MERGE_NOTAPPLIED = "merge.notapplied";
    private static final String ATTR_NAME_SIDEBAR_DELETED = "sidebar.deleted";
    private static final String ATTR_NAME_SIDEBAR_CHANGED = "sidebar.changed";
    private static final String DEFAULT_BACKGROUND = "default.background"; //NOI18N
    
    private boolean		        listen;
    private List<AttributeSet>  categories;
    private boolean             changed;
    
    public DiffColorsPanel() {
        initComponents ();

        setName(loc("LBL_DiffOptions_Tab")); //NOI18N
        
        lCategories.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        lCategories.setVisibleRowCount (6);
        lCategories.addListSelectionListener (new ListSelectionListener() {
            public void valueChanged (ListSelectionEvent e) {
                if (!listen) return;
                refreshUI ();
            }
        });
        lCategories.setCellRenderer (new CategoryRenderer());
        cbBackground.addActionListener (this);
        btnResetToDefaults.addActionListener(this);
    }

    
    public void actionPerformed (ActionEvent evt) {
        if (!listen) return;
        if (evt.getSource() == btnResetToDefaults) {
            resetToDefaults();
            refreshUI();
        } else {
            updateData ();
        }
        fireChanged();
    }
    
    public void update(ColorModel colorModel) {
        listen = false;
        int index = lCategories.getSelectedIndex();
        lCategories.setListData(new Vector(getCategories()));
        if (index >= 0 && index < lCategories.getModel().getSize()) {
            lCategories.setSelectedIndex(index);
        } else {
            lCategories.setSelectedIndex(0);
        }
        refreshUI ();	
        listen = true;
        changed = false;
    }
    
    public void cancel () {
        changed = false;
        categories = null;
    }
    
    public void applyChanges() {
        List<AttributeSet> colors = getCategories();
        for (AttributeSet color : colors) {
            if (ATTR_NAME_ADDED.equals(color.getAttribute(StyleConstants.NameAttribute))) DiffModuleConfig.getDefault().setAddedColor((Color) color.getAttribute(StyleConstants.Background)); 
            if (ATTR_NAME_CHANGED.equals(color.getAttribute(StyleConstants.NameAttribute))) DiffModuleConfig.getDefault().setChangedColor((Color) color.getAttribute(StyleConstants.Background)); 
            if (ATTR_NAME_DELETED.equals(color.getAttribute(StyleConstants.NameAttribute))) DiffModuleConfig.getDefault().setDeletedColor((Color) color.getAttribute(StyleConstants.Background)); 
            if (ATTR_NAME_MERGE_APPLIED.equals(color.getAttribute(StyleConstants.NameAttribute))) DiffModuleConfig.getDefault().setAppliedColor((Color) color.getAttribute(StyleConstants.Background)); 
            if (ATTR_NAME_MERGE_NOTAPPLIED.equals(color.getAttribute(StyleConstants.NameAttribute))) DiffModuleConfig.getDefault().setNotAppliedColor((Color) color.getAttribute(StyleConstants.Background)); 
            if (ATTR_NAME_MERGE_UNRESOLVED.equals(color.getAttribute(StyleConstants.NameAttribute))) DiffModuleConfig.getDefault().setUnresolvedColor((Color) color.getAttribute(StyleConstants.Background));
            if (ATTR_NAME_SIDEBAR_DELETED.equals(color.getAttribute(StyleConstants.NameAttribute))) DiffModuleConfig.getDefault().setSidebarDeletedColor((Color) color.getAttribute(StyleConstants.Background));
            if (ATTR_NAME_SIDEBAR_CHANGED.equals(color.getAttribute(StyleConstants.NameAttribute))) DiffModuleConfig.getDefault().setSidebarChangedColor((Color) color.getAttribute(StyleConstants.Background));
        }
        changed = false;
    }
    
    public boolean isChanged () {
        return changed;
    }
    
    public void setCurrentProfile (String currentProfile) {
        refreshUI ();
    }

    public void deleteProfile (String profile) {
    }
    
    public JComponent getComponent() {
        return this;
    }
        
    // other methods ...........................................................
    
    Collection<AttributeSet> getHighlightings () {
        return getCategories();
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (DiffColorsPanel.class, key);
    }
    
    private void fireChanged() {
        List<AttributeSet> colors = getCategories();
        boolean isChanged = false;
        for (AttributeSet color : colors) {
            isChanged |= fireChanged(color);
        }
        changed = isChanged;
    }
    
    private boolean fireChanged(AttributeSet color) {
        Color c = (Color) color.getAttribute(StyleConstants.Background);
        if (ATTR_NAME_ADDED.equals(color.getAttribute(StyleConstants.NameAttribute))) {
            return !DiffModuleConfig.getDefault().getAddedColor().equals(c != null ? c : DiffModuleConfig.getDefault().getDefaultAddedColor());
        }
        if (ATTR_NAME_CHANGED.equals(color.getAttribute(StyleConstants.NameAttribute))) {
            return !DiffModuleConfig.getDefault().getChangedColor().equals(c != null ? c : DiffModuleConfig.getDefault().getDefaultChangedColor());
        }
        if (ATTR_NAME_DELETED.equals(color.getAttribute(StyleConstants.NameAttribute))) {
            return !DiffModuleConfig.getDefault().getDeletedColor().equals(c != null ? c : DiffModuleConfig.getDefault().getDefaultDeletedColor());
        }
        if (ATTR_NAME_MERGE_APPLIED.equals(color.getAttribute(StyleConstants.NameAttribute))) {
            return !DiffModuleConfig.getDefault().getAppliedColor().equals(c != null ? c : DiffModuleConfig.getDefault().getDefaultAppliedColor());
        }
        if (ATTR_NAME_MERGE_NOTAPPLIED.equals(color.getAttribute(StyleConstants.NameAttribute))) {
            return !DiffModuleConfig.getDefault().getNotAppliedColor().equals(c != null ? c : DiffModuleConfig.getDefault().getDefaultNotAppliedColor());
        }
        if (ATTR_NAME_MERGE_UNRESOLVED.equals(color.getAttribute(StyleConstants.NameAttribute))) {
            return !DiffModuleConfig.getDefault().getUnresolvedColor().equals(c != null ? c : DiffModuleConfig.getDefault().getDefaultUnresolvedColor());
        }
        if (ATTR_NAME_SIDEBAR_DELETED.equals(color.getAttribute(StyleConstants.NameAttribute))) {
            return !DiffModuleConfig.getDefault().getSidebarDeletedColor().equals(c != null ? c : DiffModuleConfig.getDefault().getDefaultSidebarDeletedColor());
        }
        if (ATTR_NAME_SIDEBAR_CHANGED.equals(color.getAttribute(StyleConstants.NameAttribute))) {
            return !DiffModuleConfig.getDefault().getSidebarChangedColor().equals(c != null ? c : DiffModuleConfig.getDefault().getDefaultSidebarChangedColor());
        }
        return false;
    }
    
    private void updateData () {
        int index = lCategories.getSelectedIndex();
        if (index < 0) return;
        
        List<AttributeSet> categories = getCategories();
        AttributeSet category = categories.get(lCategories.getSelectedIndex());
        SimpleAttributeSet c = new SimpleAttributeSet(category);
        
        Color color = cbBackground.getSelectedColor();
        if (color != null) {
            c.addAttribute(StyleConstants.Background, color);
        } else {
            c.removeAttribute(StyleConstants.Background);
        }
        
        categories.set(index, c);
    }
    
    private void refreshUI () {
        int index = lCategories.getSelectedIndex();
        if (index < 0) {
            cbBackground.setEnabled(false);
            return;
        }
        cbBackground.setEnabled(true);
        
        List<AttributeSet> categories = getCategories();
	    AttributeSet category = categories.get(index);
        
        listen = false;
        // set values
        cbBackground.setSelectedColor((Color) category.getAttribute(StyleConstants.Background));
        listen = true;
    }

    private void resetToDefaults() {
        List<AttributeSet> categories = getCategories();
        for (ListIterator<AttributeSet> it = categories.listIterator(); it.hasNext(); ) {
            AttributeSet category = it.next();
            SimpleAttributeSet c = new SimpleAttributeSet(category);
            if (!category.getAttribute(DEFAULT_BACKGROUND).equals(c.getAttribute(StyleConstants.Background))) {
                c.addAttribute(StyleConstants.Background, category.getAttribute(DEFAULT_BACKGROUND));
                it.set(c);
            }
        }
    }
    
    private List<AttributeSet> getCategories() {
        if (categories == null) {
            categories = getDiffHighlights();
        }
        return categories;
    }

    private List<AttributeSet> getDiffHighlights() {
        List<AttributeSet> attrs = new ArrayList<AttributeSet>();
        SimpleAttributeSet sas = null;
        
        sas = new SimpleAttributeSet();
        StyleConstants.setBackground(sas, DiffModuleConfig.getDefault().getAddedColor());
        sas.addAttribute(StyleConstants.NameAttribute, ATTR_NAME_ADDED);
        sas.addAttribute(DEFAULT_BACKGROUND, DiffModuleConfig.getDefault().getDefaultAddedColor());
        sas.addAttribute(EditorStyleConstants.DisplayName, NbBundle.getMessage(DiffOptionsPanel.class, "LBL_AddedColor"));
        attrs.add(sas);

        sas = new SimpleAttributeSet();
        StyleConstants.setBackground(sas, DiffModuleConfig.getDefault().getDeletedColor());
        sas.addAttribute(StyleConstants.NameAttribute, ATTR_NAME_DELETED);
        sas.addAttribute(DEFAULT_BACKGROUND, DiffModuleConfig.getDefault().getDefaultDeletedColor());
        sas.addAttribute(EditorStyleConstants.DisplayName, NbBundle.getMessage(DiffOptionsPanel.class, "LBL_DeletedColor"));
        attrs.add(sas);

        sas = new SimpleAttributeSet();
        StyleConstants.setBackground(sas, DiffModuleConfig.getDefault().getChangedColor());
        sas.addAttribute(StyleConstants.NameAttribute, ATTR_NAME_CHANGED);
        sas.addAttribute(DEFAULT_BACKGROUND, DiffModuleConfig.getDefault().getDefaultChangedColor());
        sas.addAttribute(EditorStyleConstants.DisplayName, NbBundle.getMessage(DiffOptionsPanel.class, "LBL_ChangedColor"));
        attrs.add(sas);
        
        sas = new SimpleAttributeSet();
        StyleConstants.setBackground(sas, DiffModuleConfig.getDefault().getAppliedColor());
        sas.addAttribute(StyleConstants.NameAttribute, ATTR_NAME_MERGE_APPLIED);
        sas.addAttribute(DEFAULT_BACKGROUND, DiffModuleConfig.getDefault().getDefaultAppliedColor());
        sas.addAttribute(EditorStyleConstants.DisplayName, NbBundle.getMessage(DiffOptionsPanel.class, "LBL_AppliedColor"));
        attrs.add(sas);

        sas = new SimpleAttributeSet();
        StyleConstants.setBackground(sas, DiffModuleConfig.getDefault().getNotAppliedColor());
        sas.addAttribute(StyleConstants.NameAttribute, ATTR_NAME_MERGE_NOTAPPLIED);
        sas.addAttribute(DEFAULT_BACKGROUND, DiffModuleConfig.getDefault().getDefaultNotAppliedColor());
        sas.addAttribute(EditorStyleConstants.DisplayName, NbBundle.getMessage(DiffOptionsPanel.class, "LBL_NotAppliedColor"));
        attrs.add(sas);

        sas = new SimpleAttributeSet();
        StyleConstants.setBackground(sas, DiffModuleConfig.getDefault().getUnresolvedColor());
        sas.addAttribute(DEFAULT_BACKGROUND, DiffModuleConfig.getDefault().getDefaultUnresolvedColor());
        sas.addAttribute(StyleConstants.NameAttribute, ATTR_NAME_MERGE_UNRESOLVED);
        sas.addAttribute(EditorStyleConstants.DisplayName, NbBundle.getMessage(DiffOptionsPanel.class, "LBL_UnresolvedColor"));
        attrs.add(sas);

        sas = new SimpleAttributeSet();
        StyleConstants.setBackground(sas, DiffModuleConfig.getDefault().getSidebarDeletedColor());
        sas.addAttribute(DEFAULT_BACKGROUND, DiffModuleConfig.getDefault().getDefaultSidebarDeletedColor());
        sas.addAttribute(StyleConstants.NameAttribute, ATTR_NAME_SIDEBAR_DELETED);
        sas.addAttribute(EditorStyleConstants.DisplayName, NbBundle.getMessage(DiffOptionsPanel.class, "LBL_SidebarDeletedColor"));
        attrs.add(sas);

        sas = new SimpleAttributeSet();
        StyleConstants.setBackground(sas, DiffModuleConfig.getDefault().getSidebarChangedColor());
        sas.addAttribute(DEFAULT_BACKGROUND, DiffModuleConfig.getDefault().getDefaultSidebarChangedColor());
        sas.addAttribute(StyleConstants.NameAttribute, ATTR_NAME_SIDEBAR_CHANGED);
        sas.addAttribute(EditorStyleConstants.DisplayName, NbBundle.getMessage(DiffOptionsPanel.class, "LBL_SidebarChangedColor"));
        attrs.add(sas);

        return attrs;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lCategories = new javax.swing.JList();
        jLabel3 = new javax.swing.JLabel();
        cbBackground = new org.openide.awt.ColorComboBox();
        btnResetToDefaults = new javax.swing.JButton();

        jLabel1.setLabelFor(lCategories);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DiffColorsPanel.class, "DiffColorsPanel.jLabel1.text")); // NOI18N

        lCategories.setModel(new DefaultListModel());
        jScrollPane1.setViewportView(lCategories);

        jLabel3.setLabelFor(cbBackground);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(DiffColorsPanel.class, "DiffColorsPanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnResetToDefaults, org.openide.util.NbBundle.getMessage(DiffColorsPanel.class, "DiffColorsPanel.btnResetToDefaults.text")); // NOI18N
        btnResetToDefaults.setToolTipText(org.openide.util.NbBundle.getMessage(DiffColorsPanel.class, "DiffColorsPanel.btnResetToDefaults.TTtext")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbBackground, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(btnResetToDefaults))))
                    .addComponent(jLabel1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(cbBackground, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(btnResetToDefaults)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnResetToDefaults;
    private org.openide.awt.ColorComboBox cbBackground;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList lCategories;
    // End of variables declaration//GEN-END:variables
    
}
