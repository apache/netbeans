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

package org.netbeans.modules.cnd.ui.options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import org.netbeans.modules.cnd.utils.CndLanguageStandards;
import org.netbeans.modules.cnd.utils.CndLanguageStandards.CndLanguageStandard;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.ui.CndUIConstants;
import org.netbeans.modules.cnd.utils.NamedOption;
import org.netbeans.modules.cnd.utils.ui.StringArrayCustomEditor;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 */
@OptionsPanelController.Keywords(keywords={"#OtherOptionsKeywords"}, location=CndUIConstants.TOOLS_OPTIONS_CND_CATEGORY_ID, tabTitle= "#TAB_CndOtherOptionsTab")
/*package-local*/ final class CndOtherOptionsPanel extends javax.swing.JPanel implements ActionListener {

    public CndOtherOptionsPanel() {
        setName("TAB_CndOtherOptionsTab"); // NOI18N (used as a pattern...)
        initComponents();
        initGeneratedComponents();
    }

    void applyChanges() {
        for (ExtensionsElements ee : eeList) {
            ee.apply();
        }
        for (Entity e : entities) {
            NamedOption.getAccessor().setBoolean(e.se.getName(), e.cb.isSelected());
        }
        isChanged = false;
    }

    void update() {
        for (ExtensionsElements ee : eeList) {
            ee.update();
        }
        for (Entity e : entities) {
            e.cb.setSelected(NamedOption.getAccessor().getBoolean(e.se.getName()));
        }
        isChanged = false;
    }

    // for OptionsPanelSupport
    private boolean isChanged = false;

    void cancel() {
        isChanged = false;
    }

    boolean isChanged() {
        return isChanged;
    }
    
    private boolean areExtensionsChanged() {
        boolean changed = false;
        for (ExtensionsElements ee : eeList) {
            List<String> current = ee.getValues();
            Collection<String> saved = ee.es.getValues();
            changed |= !ee.es.getDefaultExtension().equals(ee.defaultValue) || !equalsStandards(ee.es.getDefaultStandard(), ee.defaultStandard) || current.size() != saved.size() || !current.containsAll(saved);
            if (changed) {
                return true;
            }
        }
        return false;
    }

    private boolean equalsStandards(CndLanguageStandard st1, CndLanguageStandard st2) {
        if (st1 == null) {
            return st2 == null; 
        } else {
            return st1.equals(st2);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        isChanged = areExtensionsChanged();
    }
    
    private void selectDefaultStandard(ItemEvent event, ExtensionsElements ee) {
	if (event.getStateChange() == ItemEvent.SELECTED) {
            ee.defaultStandard = (CndLanguageStandard) event.getItem();
            isChanged = areExtensionsChanged();
        }
    }
    
    private void editExtensionsButtonActionPerformed(ExtensionsElements ee) {
        StringArrayCustomEditor editor = new StringArrayCustomEditor(
                ee.getValues().toArray(new String[]{}), ee.defaultValue,
                getMessage("EE_ItemLabel"), getMessage("EE_ItemLabel_Mnemonic").charAt(0),  // NOI18N
                getMessage("EE_ItemListLabel"), getMessage("EE_ItemListLabel_Mnemonic").charAt(0),  // NOI18N
                false);
        
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        outerPanel.add(editor, gridBagConstraints);
        
        Object[] options = new Object[] {NotifyDescriptor.OK_OPTION};
        DialogDescriptor dd = new DialogDescriptor(outerPanel, getMessage("ExtensionsListEditorTitle"), true, options, NotifyDescriptor.OK_OPTION, 0, null, null);
        
        DialogDisplayer dialogDisplayer = DialogDisplayer.getDefault();
        java.awt.Dialog dl = dialogDisplayer.createDialog(dd);
        dl.getAccessibleContext().setAccessibleDescription(getMessage("ExtensionsListEditorTitle_AD"));
        dl.pack();
        dl.setSize(new java.awt.Dimension(300, (int)dl.getPreferredSize().getHeight()));
        
        try {
            dl.setVisible(true);
            if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                ee.defaultValue = editor.getDefaultValue();
                ee.setValues( editor.getItemList() );
                isChanged = areExtensionsChanged();
            }
        } catch (Throwable th) {
            if (!(th.getCause() instanceof InterruptedException)) {
                throw new RuntimeException(th);
            }
        } finally {
            dl.dispose();
        }

    }

    private final List<ExtensionsElements> eeList = new ArrayList<ExtensionsElements>();

    private void initGeneratedComponents() {
        Collection<MIMEExtensions> orderedExtensions = MIMEExtensions.getCustomizable();
        for (MIMEExtensions ext : orderedExtensions) {
            final ExtensionsElements ee = new ExtensionsElements(ext);

            ee.label.setText(ext.getLocalizedDescription());
            ee.button.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    editExtensionsButtonActionPerformed(ee);
                }
            });
            ee.standard.addItemListener(new ItemListener(){
                @Override
                public void itemStateChanged(ItemEvent event) {
                    selectDefaultStandard(event, ee);
                }
            });

            eeList.add(ee);
        }
        for(NamedOption ee : Lookups.forPath(NamedOption.OTHER_CATEGORY).lookupAll(NamedOption.class)) {
            if (ee.isVisible()) {
                addEntity(ee);
            }
        }
        
        GroupLayout layout = new GroupLayout(extensionPanel);
        extensionPanel.setLayout(layout);
        GroupLayout.SequentialGroup horizontalGroup = layout.createSequentialGroup();
        horizontalGroup.addGap(6, 6, 6);

        GroupLayout.ParallelGroup labelsGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        JLabel columnHeader1 = new JLabel(NbBundle.getMessage(CndOtherOptionsPanel.class, "EE_Type_Header")); //NOI18N
        labelsGroup.addComponent(columnHeader1);
        for (int i = 0; i < eeList.size(); i++) {
            labelsGroup.addComponent(eeList.get(i).label);
        }

        horizontalGroup.addGroup(labelsGroup);
        horizontalGroup.addGap(4, 4, 4);

        GroupLayout.ParallelGroup textfieldsGroup = layout.createParallelGroup(GroupLayout.Alignment.CENTER, false);
        JLabel columnHeader2 = new JLabel(NbBundle.getMessage(CndOtherOptionsPanel.class, "EE_Extensions_Header")); //NOI18N
        textfieldsGroup.addComponent(columnHeader2);
        for (int i = 0; i <  eeList.size(); i++) {
            textfieldsGroup.addComponent(eeList.get(i).textfield, GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE);
        }
        horizontalGroup.addGroup(textfieldsGroup);
        horizontalGroup.addGap(6, 6, 6);
        
        GroupLayout.ParallelGroup buttonsGroup = layout.createParallelGroup(GroupLayout.Alignment.CENTER);
        JLabel columnHeader3 = new JLabel();
        buttonsGroup.addComponent(columnHeader3);
        for (int i = 0; i < eeList.size(); i++) {
            buttonsGroup.addComponent(eeList.get(i).button);
        }
        horizontalGroup.addGroup(buttonsGroup);
        horizontalGroup.addGap(6, 6, 6);

        GroupLayout.ParallelGroup standardGroup = layout.createParallelGroup(GroupLayout.Alignment.CENTER);      
        JLabel columnHeader4 = new JLabel(NbBundle.getMessage(CndOtherOptionsPanel.class, "EE_Standard_Header")); //NOI18N
        standardGroup.addComponent(columnHeader4);
        for (int i = 0; i < eeList.size(); i++) {
            standardGroup.addComponent(eeList.get(i).standard);
        }
        horizontalGroup.addGroup(standardGroup);

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                    .addGroup(horizontalGroup)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        
        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup()
                .addContainerGap();
        
        verticalGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(columnHeader1)
                    .addComponent(columnHeader2, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
                    .addComponent(columnHeader3, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
                    .addComponent(columnHeader4, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE));
        verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        for (int i = 0; i < eeList.size(); i++) {
            ExtensionsElements ee = eeList.get(i);
            verticalGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(ee.label)
                        .addComponent(ee.textfield, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
                        .addComponent(ee.button, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
                        .addComponent(ee.standard, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE));
            if (i !=  eeList.size() - 1) {
                verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
            } else {
                verticalGroup.addContainerGap(20, Short.MAX_VALUE);
            }
                
        }

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(verticalGroup)
        );
        
        layout = new GroupLayout(optionsPanel);
        optionsPanel.setLayout(layout);

        GroupLayout.ParallelGroup pg = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup sg = layout.createSequentialGroup();
        for (Entity e : entities) {
            pg.addComponent(e.cb);
            sg.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(e.cb);
        }

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pg))).addContainerGap()));

        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(sg.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        extensionPanel = new javax.swing.JPanel();
        optionsPanel = new javax.swing.JPanel();

        extensionPanel.setOpaque(false);

        javax.swing.GroupLayout extensionPanelLayout = new javax.swing.GroupLayout(extensionPanel);
        extensionPanel.setLayout(extensionPanelLayout);
        extensionPanelLayout.setHorizontalGroup(
            extensionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 423, Short.MAX_VALUE)
        );
        extensionPanelLayout.setVerticalGroup(
            extensionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 118, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout optionsPanelLayout = new javax.swing.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 182, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(extensionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(optionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(extensionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(optionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel extensionPanel;
    private javax.swing.JPanel optionsPanel;
    // End of variables declaration//GEN-END:variables

    private static String getMessage(String resourceName) {
        return NbBundle.getMessage(CndOtherOptionsPanel.class, resourceName);
    }

    private static class Entity {

        public final NamedOption se;
        public final JCheckBox cb;

        public Entity(NamedOption se, JCheckBox cb) {
            this.se = se;
            this.cb = cb;
        }
    }
    private final List<Entity> entities = new ArrayList<Entity>();
    private void addEntity(NamedOption ne) {
        JCheckBox cb = new JCheckBox();
        Mnemonics.setLocalizedText(cb, ne.getDisplayName());
        if (ne.getDescription() != null) {
            cb.setToolTipText(ne.getDescription());
        }
        cb.setOpaque(false);
        entities.add(new Entity(ne, cb));
        cb.addActionListener(this);
    }

    
    private static final class ExtensionsElements {

        public ExtensionsElements(MIMEExtensions es) {
            this.es = es;
            update();
            textfield.setContentType("text/html");  // NOI18N
            textfield.setEditable(false);
            // bug 233412, fix for dark theme
            textfield.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
            
            for(CndLanguageStandard st : CndLanguageStandards.getSupported(es.getMIMEType())) {
                standard.addItem(st);
            }
            updateTextField();
            button.setText(getMessage("CndOtherOptionsPanel.Extensions.EditButton"));
        }

        private static final String DELIMITER = ", "; // NOI18N
    
        public void updateTextField() {
            StringBuilder text = new StringBuilder();
            for (String elem : list) {
                if (text.length() > 0) {
                    text.append(DELIMITER);
                }
                if (elem.equals(defaultValue)) {
                    elem = "<b>" + elem + "</b>"; // NOI18N
                }
                text.append(elem);
            }

            textfield.setText(text.toString());
        }

        List<String> getValues() {
            return Collections.unmodifiableList(list);
        }
        
        void setValues(String[] values) {
            list = Arrays.asList(values);
            Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
            updateTextField();
        }

        public void apply() {
            es.setExtensions(getValues(), defaultValue);
            es.setDefaultStandard(defaultStandard);
        }

        public void update() {
            list = new ArrayList<String>(es.getValues());
            defaultValue = es.getDefaultExtension();
            defaultStandard = es.getDefaultStandard();
            updateTextField();
            if (defaultStandard != null) {
                standard.setSelectedItem(defaultStandard);
            }
        }
        
        private final MIMEExtensions es;
        public final JLabel label = new JLabel();
        public final JEditorPane textfield = new JEditorPane();
        public final JButton button = new JButton();
        public final JComboBox standard = new JComboBox();
        private List<String> list;
        private String defaultValue;
        private CndLanguageStandard defaultStandard;
        
    }
}
