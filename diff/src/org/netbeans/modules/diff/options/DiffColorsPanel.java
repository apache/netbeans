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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
