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

package org.netbeans.modules.versioning.util.options;

import java.awt.Component;
import org.netbeans.modules.options.colors.spi.FontsColorsController;
import org.netbeans.modules.options.colors.ColorModel;
import org.openide.util.LookupEvent;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.*;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.versioning.util.OptionsPanelColorProvider;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.WeakListeners;

/**
 * 
 * @author Maros Sandor, Ondra Vrabec
 */
@OptionsPanelController.Keywords(keywords={"versioning", "#KW_AnnotationColorsPanel"}, location=OptionsDisplayer.FONTSANDCOLORS, tabTitle="#CTL_AnnotationColorsPanel.tabName")
@Messages({ "CTL_AnnotationColorsPanel.title=&Versioning", "CTL_AnnotationColorsPanel.tabName=Versioning" })
public class AnnotationColorsPanel extends javax.swing.JPanel implements ActionListener, FontsColorsController {

    private boolean listen;
    private final HashMap<OptionsPanelColorProvider, VersioningSystemColors>  vcsColors;
    private final Lookup.Result<OptionsPanelColorProvider> result;
    private final LookupListener vcsListener;
    private boolean changed;
    
    public AnnotationColorsPanel() {
        initComponents ();

        setName(Bundle.CTL_AnnotationColorsPanel_title());

        result = Lookup.getDefault().lookupResult(OptionsPanelColorProvider.class);
        vcsColors = new HashMap<OptionsPanelColorProvider, VersioningSystemColors> ();
        versioningSystemsList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateColorList();
                }
            }
        });
        result.addLookupListener(WeakListeners.create(
            LookupListener.class,
            vcsListener = new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    initSystems();
                }
            }, result
        ));
        initSystems();

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
        btnResetToDefaults.addActionListener (this);
        versioningSystemsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                setComponentOrientation(list.getComponentOrientation());
                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
                setText((String) ((OptionsPanelColorProvider) value).getName());

                setEnabled(list.isEnabled());
                setFont(list.getFont());
                setBorder(cellHasFocus ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder); //NOI18N
                return this;
            }
        });
        listen = true;
    }
    
    public void actionPerformed (ActionEvent evt) {
        if (!listen) return;
        if (evt.getSource() == btnResetToDefaults) {
            resetToDefaults();
            refreshUI();
        } else {
            updateData();
        }
        boolean isChanged = false;
        for (VersioningSystemColors vsc : vcsColors.values()) {
            isChanged |= vsc.changed;
        }
        changed = isChanged;
    }
    
    public void update(ColorModel colorModel) {
        if (!listen) return;
        listen = false;
        refreshUI ();	
        listen = true;
        changed = false;
    }
    
    public void cancel () {
        changed = false;
        vcsColors.clear();
    }
    
    public void applyChanges() {
        for (Map.Entry<OptionsPanelColorProvider, VersioningSystemColors> e : vcsColors.entrySet()) {
            e.getValue().saveColors();
        }
        vcsColors.clear();
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
    
    private void updateData () {
        if (versioningSystemsList.getSelectedValue() == null) return;
        int index = lCategories.getSelectedIndex();
        if (index < 0) return;
        Color color = cbBackground.getSelectedColor();
        VersioningSystemColors colors = vcsColors.get((OptionsPanelColorProvider)versioningSystemsList.getSelectedValue());
        colors.setColor(index, color);
    }
    
    private void refreshUI () {
        if (versioningSystemsList.getSelectedValue() == null) {
            return;
        }
        int index = lCategories.getSelectedIndex();
        if (index < 0) {
            cbBackground.setEnabled(false);
            return;
        }
        cbBackground.setEnabled(true);
        
        List<AttributeSet> categories = getCategories();
	AttributeSet category = categories.get(lCategories.getSelectedIndex());
        
        listen = false;
        // set values
        cbBackground.setSelectedColor((Color) category.getAttribute(StyleConstants.Background));
        listen = true;
    }
    
    private List<AttributeSet> getCategories() {
        OptionsPanelColorProvider provider = (OptionsPanelColorProvider)versioningSystemsList.getSelectedValue();
        VersioningSystemColors colors = getProviderColors(provider);
        return colors.getColorAttributes();
    }

    private VersioningSystemColors getProviderColors (OptionsPanelColorProvider provider) {
        VersioningSystemColors colors = vcsColors.get(provider);
        if (colors == null) {
            colors = new VersioningSystemColors(provider);
            vcsColors.put(provider, colors);
        }
        return colors;
    }
    
    private List<AttributeSet> getAllCategories() {
        List<AttributeSet> allColors = new ArrayList<AttributeSet>();
        ListModel model = versioningSystemsList.getModel();
        for (int i = 0; i < model.getSize(); ++i) {
            OptionsPanelColorProvider provider = (OptionsPanelColorProvider) model.getElementAt(i);
            allColors.addAll(getProviderColors(provider).getColorAttributes());
        }
        return allColors;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        versioningSystemsList = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        containerPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lCategories = new javax.swing.JList();
        jLabel3 = new javax.swing.JLabel();
        btnResetToDefaults = new javax.swing.JButton();
        cbBackground = new org.openide.awt.ColorComboBox();
        jLabel4 = new javax.swing.JLabel();

        versioningSystemsList.setModel(new DefaultListModel());
        versioningSystemsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(versioningSystemsList);

        jLabel2.setLabelFor(versioningSystemsList);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getBundle(AnnotationColorsPanel.class).getString("AnnotationColorsPanel.jLabel2.text")); // NOI18N

        jLabel1.setLabelFor(lCategories);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(AnnotationColorsPanel.class, "AnnotationColorsPanel.jLabel1.text")); // NOI18N

        lCategories.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lCategories.setMinimumSize(new java.awt.Dimension(100, 0));
        jScrollPane1.setViewportView(lCategories);

        jLabel3.setLabelFor(cbBackground);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(AnnotationColorsPanel.class, "AnnotationColorsPanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnResetToDefaults, org.openide.util.NbBundle.getMessage(AnnotationColorsPanel.class, "AnnotationColorsPanel.btnResetToDefaults.text")); // NOI18N
        btnResetToDefaults.setToolTipText(org.openide.util.NbBundle.getMessage(AnnotationColorsPanel.class, "AnnotationColorsPanel.btnResetToDefaults.TTtext")); // NOI18N

        jLabel4.setFont(jLabel4.getFont().deriveFont(jLabel4.getFont().getSize()-1f));
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/util/resources/info.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(AnnotationColorsPanel.class, "AnnotationColorsPanel.jLabel4.text")); // NOI18N

        javax.swing.GroupLayout containerPanelLayout = new javax.swing.GroupLayout(containerPanel);
        containerPanel.setLayout(containerPanelLayout);
        containerPanelLayout.setHorizontalGroup(
            containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(containerPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(containerPanelLayout.createSequentialGroup()
                                .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnResetToDefaults)
                                    .addGroup(containerPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbBackground, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(containerPanelLayout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                        .addGap(20, 20, 20))
                    .addGroup(containerPanelLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addContainerGap(387, Short.MAX_VALUE))))
        );
        containerPanelLayout.setVerticalGroup(
            containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, containerPanelLayout.createSequentialGroup()
                        .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(cbBackground, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addComponent(btnResetToDefaults)
                        .addContainerGap())
                    .addComponent(jScrollPane1)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, 0, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(containerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(containerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnResetToDefaults;
    private org.openide.awt.ColorComboBox cbBackground;
    private javax.swing.JPanel containerPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList lCategories;
    private javax.swing.JList versioningSystemsList;
    // End of variables declaration//GEN-END:variables

    private void initSystems () {
        final Collection<? extends OptionsPanelColorProvider> providers = result.allInstances();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                ((DefaultListModel) versioningSystemsList.getModel()).removeAllElements();
                for (OptionsPanelColorProvider provider : providers) {
                    ((DefaultListModel) versioningSystemsList.getModel()).addElement(provider);
                }
                if (versioningSystemsList.getModel().getSize() >= 1) {
                    versioningSystemsList.setSelectedIndex(0);
                }
            }
        });
    }

    private void updateColorList() {
        List<AttributeSet> allCats = getAllCategories();
        lCategories.setListData(allCats.toArray(new AttributeSet[0]));
        lCategories.setPreferredSize(null);
        int width = lCategories.getPreferredSize().width;
        if (versioningSystemsList.getSelectedValue() == null) {
            lCategories.setEnabled(false);
            lCategories.setListData(new Object[0]);
        } else {
            lCategories.setEnabled(true);
            lCategories.setListData(new Vector<AttributeSet>(getCategories()));
            lCategories.setSelectedIndex(0);
        }
        lCategories.setPreferredSize(new Dimension(width, lCategories.getPreferredSize().height));
    }

    private void resetToDefaults() {
        OptionsPanelColorProvider provider = (OptionsPanelColorProvider)versioningSystemsList.getSelectedValue();
        VersioningSystemColors colors = vcsColors.get(provider);
        if (colors != null) {
            colors.resetToDefaults();
        }
    }

    private static class VersioningSystemColors {
        private final Map<String, Color[]> colors;
        private boolean changed;
        private ArrayList<AttributeSet> colorAttributes;
        private ArrayList<AttributeSet> savedColorAttributes;
        private final OptionsPanelColorProvider provider;

        public VersioningSystemColors(OptionsPanelColorProvider provider) {
            this.colors = provider.getColors();
            if (colors == null) {
                throw new NullPointerException("Null colors for " + provider); // NOI18N
            }
            this.provider = provider;
            // initialize saved colors list
            savedColorAttributes = new ArrayList<AttributeSet>(colors.size());
            for (Map.Entry<String, Color[]> e : colors.entrySet()) {
                SimpleAttributeSet sas = new SimpleAttributeSet();
                StyleConstants.setBackground(sas, e.getValue()[0]);
                sas.addAttribute(StyleConstants.NameAttribute, e.getKey());
                sas.addAttribute(EditorStyleConstants.DisplayName, e.getKey());
                savedColorAttributes.add(sas);
            }
        }

        public void saveColors () {
            if (changed) {
                Map<String, Color> colorsToSave = new HashMap<String, Color>(colors.size());
                for (Map.Entry<String, Color[]> e : colors.entrySet()) {
                    colorsToSave.put(e.getKey(), e.getValue()[0]);
                }
                provider.colorsChanged(colorsToSave);
            }
        }

        public synchronized List<AttributeSet> getColorAttributes() {
            ArrayList<AttributeSet> attrs = this.colorAttributes;
            if (attrs == null) {
                attrs = new ArrayList<AttributeSet>(colors.size());
                for (Map.Entry<String, Color[]> e : colors.entrySet()) {
                    SimpleAttributeSet sas = new SimpleAttributeSet ();
                    StyleConstants.setBackground(sas, e.getValue()[0]);
                    sas.addAttribute(StyleConstants.NameAttribute, e.getKey());
                    sas.addAttribute(EditorStyleConstants.DisplayName, e.getKey());
                    attrs.add(sas);
                }
                this.colorAttributes = attrs;
            }
            return attrs;
        }

        public void setColor(int index, Color color) {
            if (color == null) return;
            AttributeSet attr = colorAttributes.get(index);
            SimpleAttributeSet c = new SimpleAttributeSet(attr);
            if (attr != null) {
                c.addAttribute(StyleConstants.Background, color);
            } else {
                c.removeAttribute(StyleConstants.Background);
            }
            colorAttributes.set(index, c);
            Color[] savedColor = colors.get((String)c.getAttribute(StyleConstants.NameAttribute));
            savedColor[0] = color;
            fireChanged();
        }

        /**
         * Resets colors to default values.
         */
        public synchronized void resetToDefaults() {
            for (Map.Entry<String, Color[]> e : colors.entrySet()) {
                e.getValue()[0] = e.getValue()[1];
            }
            fireChanged();
            colorAttributes = null;
        }
        
        private void fireChanged() {
            for (int i = 0; i < savedColorAttributes.size(); i++) {
                Color current = colors.get((String) colorAttributes.get(i).getAttribute(StyleConstants.NameAttribute))[0];
                Color saved = (Color) savedColorAttributes.get(i).getAttribute(StyleConstants.Background);
                if (!current.equals(saved)) {
                    changed = true;
                    return;
                }
            }
            changed = false;
        }
        
    }
}
