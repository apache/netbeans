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

package org.netbeans.modules.j2ee.persistence.wizard;

import java.awt.Component;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author  Pavel Buzek
 */
public class PersistenceClientEntitySelectionVisual extends JPanel {

    private static final long serialVersionUID = -4552755466067867817L;
    
    private ChangeSupport changeSupport = new ChangeSupport(this);
    private Project project;

    //private PersistenceUnit persistenceUnit;
    private boolean createPU = true;//right now this panel is used in wizards with required pu (but need to handle if pu already created)

    private EntityClosure entityClosure;
    private final boolean disableNoIdSelection;


    public PersistenceClientEntitySelectionVisual(String name, 
            WizardDescriptor wizard) 
    {
        this( name , wizard , false );
    }
    
    
    public PersistenceClientEntitySelectionVisual(String name, 
            WizardDescriptor wizard , boolean requireReferencedClasses ) 
    {
        setName(name);
        initComponents();
        ListSelectionListener selectionListener = ( (ListSelectionEvent e) -> updateButtons() );
        listAvailable.getSelectionModel().addListSelectionListener(selectionListener);
        listSelected.getSelectionModel().addListSelectionListener(selectionListener);
        disableNoIdSelection = wizard.getProperty(PersistenceClientEntitySelection.DISABLENOIDSELECTION) == Boolean.TRUE;
        if ( requireReferencedClasses ){
            cbAddRelated.setSelected( true );
            cbAddRelated.setVisible( false );
        }
    }

    /**
     * @return if wizard have selected option to create new pu.
     */
    public boolean getCreatePersistenceUnit() {
        return createPU && createPUCheckbox.isVisible();//if checkbox isn't visible, regardless of selection, pu creation is not required
    }

    private Set<String> getSelectedEntities(JList list) {
        Set<String> result = new HashSet<>();
        for (Object elem : Util.getSelectedItems(list, true)){
            result.add((String) elem);
        }
        return result;
    }
    private Set<String> getEnabledEntities(JList list) {
        Set<String> result = new HashSet<>();
        for (Object elem : Util.getEnabledItems(list)){
            result.add((String) elem);
        }
        return result;
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        labelAvailableEntities = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listAvailable = new javax.swing.JList();
        createPUCheckbox = new javax.swing.JCheckBox();
        workAround1 = new javax.swing.JPanel();
        panelButtons = new javax.swing.JPanel();
        buttonAdd = new javax.swing.JButton();
        buttonRemove = new javax.swing.JButton();
        buttonAddAll = new javax.swing.JButton();
        buttonRemoveAll = new javax.swing.JButton();
        workAround2 = new javax.swing.JPanel();
        labelSelectedEntities = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        listSelected = new javax.swing.JList();
        cbAddRelated = new javax.swing.JCheckBox();

        setPreferredSize(new java.awt.Dimension(500, 100));
        setLayout(new java.awt.GridBagLayout());

        labelAvailableEntities.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "MNE_AvailableEntityClasses").charAt(0));
        labelAvailableEntities.setLabelFor(listAvailable);
        labelAvailableEntities.setText(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "LBL_AvailableEntities")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(labelAvailableEntities, gridBagConstraints);

        jPanel1.setLayout(new java.awt.BorderLayout());

        listAvailable.setCellRenderer(ENTITY_LIST_RENDERER_AV);
        jScrollPane1.setViewportView(listAvailable);
        listAvailable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "LBL_AvailableEntitiesList")); // NOI18N
        listAvailable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "ACSD_AvailableEntitiesList")); // NOI18N

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

        createPUCheckbox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(createPUCheckbox, org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "LBL_CreatePersistenceUnit")); // NOI18N
        createPUCheckbox.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        createPUCheckbox.setEnabled(false);
        createPUCheckbox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                createPUCheckboxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(createPUCheckbox, gridBagConstraints);

        javax.swing.GroupLayout workAround1Layout = new javax.swing.GroupLayout(workAround1);
        workAround1.setLayout(workAround1Layout);
        workAround1Layout.setHorizontalGroup(
            workAround1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1, Short.MAX_VALUE)
        );
        workAround1Layout.setVerticalGroup(
            workAround1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 444, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 32;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(workAround1, gridBagConstraints);

        panelButtons.setLayout(new java.awt.GridBagLayout());

        buttonAdd.setMnemonic(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "MNE_Add").charAt(0));
        buttonAdd.setText(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "LBL_Add")); // NOI18N
        buttonAdd.setActionCommand("&Add >");
        buttonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 74;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        panelButtons.add(buttonAdd, gridBagConstraints);
        buttonAdd.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "ACSD_Add")); // NOI18N

        buttonRemove.setMnemonic(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "MNE_Remove").charAt(0));
        buttonRemove.setText(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "LBL_Remove")); // NOI18N
        buttonRemove.setActionCommand("< &Remove");
        buttonRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRemoveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 54;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        panelButtons.add(buttonRemove, gridBagConstraints);
        buttonRemove.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "ACSD_Remove")); // NOI18N

        buttonAddAll.setMnemonic(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "MNE_AddAll").charAt(0));
        buttonAddAll.setText(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "LBL_AddAll")); // NOI18N
        buttonAddAll.setActionCommand("Add A&ll >>");
        buttonAddAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddAllActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 52;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        panelButtons.add(buttonAddAll, gridBagConstraints);
        buttonAddAll.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "ACSD_AddAll")); // NOI18N

        buttonRemoveAll.setMnemonic(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "MNE_RemoveAll").charAt(0));
        buttonRemoveAll.setText(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "LBL_RemoveAll")); // NOI18N
        buttonRemoveAll.setActionCommand("<< Re&moveAll");
        buttonRemoveAll.setMaximumSize(new java.awt.Dimension(137, 23));
        buttonRemoveAll.setMinimumSize(new java.awt.Dimension(137, 23));
        buttonRemoveAll.setPreferredSize(new java.awt.Dimension(137, 23));
        buttonRemoveAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRemoveAllActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        panelButtons.add(buttonRemoveAll, gridBagConstraints);
        buttonRemoveAll.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "ACSD_RemoveAll")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 33;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        add(panelButtons, gridBagConstraints);

        javax.swing.GroupLayout workAround2Layout = new javax.swing.GroupLayout(workAround2);
        workAround2.setLayout(workAround2Layout);
        workAround2Layout.setHorizontalGroup(
            workAround2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1, Short.MAX_VALUE)
        );
        workAround2Layout.setVerticalGroup(
            workAround2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 444, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 34;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(workAround2, gridBagConstraints);

        labelSelectedEntities.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "MNE_SelectedEntityClasses").charAt(0));
        labelSelectedEntities.setLabelFor(listSelected);
        labelSelectedEntities.setText(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "LBL_SelectedEntities")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 222;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(labelSelectedEntities, gridBagConstraints);

        jPanel2.setLayout(new java.awt.BorderLayout());

        listSelected.setCellRenderer(ENTITY_LIST_RENDERER_SEL);
        jScrollPane2.setViewportView(listSelected);
        listSelected.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "LBL_SelectedEntitiesList")); // NOI18N
        listSelected.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "ACSD_SelectedEntitiesList")); // NOI18N

        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 222;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel2, gridBagConstraints);

        cbAddRelated.setMnemonic(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "MNE_InludeRelated").charAt(0));
        cbAddRelated.setSelected(true);
        cbAddRelated.setText(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "LBL_IncludeReferenced")); // NOI18N
        cbAddRelated.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbAddRelated.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbAddRelatedActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 222;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(cbAddRelated, gridBagConstraints);
        cbAddRelated.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "LBL_IncludeReferencedCheckbox")); // NOI18N
        cbAddRelated.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "ACSD_IncludeReferencedCheckbox")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void cbAddRelatedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbAddRelatedActionPerformed
        listSelected.clearSelection();
        listAvailable.clearSelection();
        entityClosure.setClosureEnabled(cbAddRelated.isSelected());

        changeSupport.fireChange();
    }//GEN-LAST:event_cbAddRelatedActionPerformed

    private void createPUCheckboxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_createPUCheckboxItemStateChanged
        createPU = createPUCheckbox.isVisible() && createPUCheckbox.isSelected();

    }//GEN-LAST:event_createPUCheckboxItemStateChanged

    private void buttonRemoveAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRemoveAllActionPerformed

        entityClosure.removeAllEntities();         listSelected.clearSelection();         updateButtons();         changeSupport.fireChange();     }//GEN-LAST:event_buttonRemoveAllActionPerformed

    private void buttonAddAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddAllActionPerformed

        listAvailable.clearSelection();         entityClosure.addEntities(getEnabledEntities(listAvailable));         updateButtons();         changeSupport.fireChange();     }//GEN-LAST:event_buttonAddAllActionPerformed

    private void buttonRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRemoveActionPerformed

        entityClosure.removeEntities(getSelectedEntities(listSelected));         listSelected.clearSelection();         updateButtons();          changeSupport.fireChange();     }//GEN-LAST:event_buttonRemoveActionPerformed

    private void buttonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddActionPerformed

        entityClosure.addEntities(getSelectedEntities(listAvailable));         listAvailable.clearSelection();         updateButtons();          changeSupport.fireChange();     }//GEN-LAST:event_buttonAddActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAdd;
    private javax.swing.JButton buttonAddAll;
    private javax.swing.JButton buttonRemove;
    private javax.swing.JButton buttonRemoveAll;
    private javax.swing.JCheckBox cbAddRelated;
    private javax.swing.JCheckBox createPUCheckbox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel labelAvailableEntities;
    private javax.swing.JLabel labelSelectedEntities;
    private javax.swing.JList listAvailable;
    private javax.swing.JList listSelected;
    private javax.swing.JPanel panelButtons;
    private javax.swing.JPanel workAround1;
    private javax.swing.JPanel workAround2;
    // End of variables declaration//GEN-END:variables
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public boolean valid(WizardDescriptor wizard) {
        
        SourceGroup[] groups = SourceGroups.getJavaSourceGroups(project);
        if (groups.length > 0) {
            ClassPath compileCP = ClassPath.getClassPath(groups[0].getRootFolder(), ClassPath.COMPILE);
            if (compileCP==null || compileCP.findResource("javax/persistence/Entity.class") == null) { // NOI18N
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "ERR_NoPersistenceProvider"));
                return false;
            }
        }
        if (!ProviderUtil.isValidServerInstanceOrNone(project)){
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class,"ERR_MissingServer")); //NOI18N
            return false;
        }
        if (!entityClosure.isModelReady()) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "scanning-in-progress"));
            return false;
        }

        if (listSelected.getModel().getSize() == 0) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "MSG_NoEntityClassesSelected"));
            return false;
        }

        if (entityClosure.isEjbModuleInvolved()) {
            wizard.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "WARN_DetectedEntitiesFromEjbModule"));
            return true;
        }

        wizard.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, " "); //NOI18N
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, " "); //NOI18N
        return true;
    }

    
    public void read(WizardDescriptor settings) {
        project = Templates.getProject(settings);

        EntityClassScope entityClassScope = EntityClassScope.getEntityClassScope(project.getProjectDirectory());
        
        entityClosure = EntityClosure.create(entityClassScope, project);
        entityClosure.addChangeListener( (ChangeEvent e) -> {
            changeSupport.fireChange();
            updateAddAllButton();
        });
        entityClosure.setClosureEnabled(cbAddRelated.isSelected());
        listAvailable.setModel(new EntityListModel(entityClosure, true));
        listSelected.setModel(new EntityListModel(entityClosure, false));
        @SuppressWarnings("unchecked")
        List<String> entities = (List<String>) settings.getProperty(WizardProperties.ENTITY_CLASS);
        if (entities == null) {
            entities = new ArrayList<String>();
        }
        entityClosure.addEntities(new HashSet<String>(entities));
        updateButtons();
        updatePersistenceUnitButton();
    }

    public void store(WizardDescriptor settings) {
        ListModel model = listSelected.getModel();
        if (model instanceof EntityListModel) {
            EntityListModel elm = (EntityListModel) model;
            settings.putProperty(WizardProperties.ENTITY_CLASS, elm.getEntityClasses());
        }
    }

    private void updateButtons() {
        Set selectedItems = Util.getSelectedItems(listAvailable, true);
        buttonAdd.setEnabled(!selectedItems.isEmpty());
        updateAddAllButton();
        buttonRemove.setEnabled(listSelected.getSelectedValues().length > 0);
        buttonRemoveAll.setEnabled(!entityClosure.getSelectedEntities().isEmpty());
    }

    private void updateAddAllButton(){
        buttonAddAll.setEnabled(!Util.getEnabledItems(listAvailable).isEmpty());
    }
    
    public void updatePersistenceUnitButton() {
        boolean visible = true;
        if (ProviderUtil.isValidServerInstanceOrNone(project) && visible) {
            try {
                visible = !ProviderUtil.persistenceExists(project);
            } catch (InvalidPersistenceXmlException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        createPUCheckbox.setVisible(visible);
    }

    private final ListCellRenderer ENTITY_LIST_RENDERER_AV = new EntityListCellRenderer( true );
    private final ListCellRenderer ENTITY_LIST_RENDERER_SEL = new EntityListCellRenderer( false );

    private class EntityListModel extends AbstractListModel implements ChangeListener {

        private EntityClosure entityClosure;
        private List<String> entities = new ArrayList<>();
        private boolean available;

        EntityListModel(EntityClosure entityClosure, boolean available) {
            this.entityClosure = entityClosure;
            this.available = available;
            entityClosure.addChangeListener(this);
            refresh();
        }

        @Override
        public int getSize() {
            return entities.size();
        }

        @Override
        public Object getElementAt(int index) {
            return entities.get(index);
        }

        /**
         * @return the fully qualified names of the entities in this model.
         */ 
        public List<String> getEntityClasses() {
            return entities;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            refresh();
        }

        private void refresh() {
            int oldSize = getSize();
            entities = new ArrayList<String>(available ? entityClosure.getAvailableEntities() : entityClosure.getSelectedEntities());
            Collections.sort(entities);
            fireContentsChanged(this, 0, Math.max(oldSize, getSize()));
        }
    }

    private final class EntityListCellRenderer extends DefaultListCellRenderer {

        private boolean available;

        public EntityListCellRenderer(boolean available) {
            setOpaque(true);
            this.available = available;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            JLabel label = (JLabel)component;
            String text = null;
            boolean disable = false;
            if (value instanceof Entity) {
                Entity entity = ((Entity) value);
                text = entity.getClass2();
                if (text != null) {
                    String simpleName = JavaIdentifiers.unqualify(text);
                    String packageName = text.length() > simpleName.length() ? text.substring(0, text.length() - simpleName.length() - 1) : "<default package>";
                    text = simpleName + " (" + packageName + ")";
                } else {
                    Logger.getLogger("global").log(Level.INFO, "Entity:" + value + " returns null from getClass2(); see IZ 80024"); //NOI18N
                }
                if(disableNoIdSelection && available && entityClosure.haveId(entity.getClass2())!=Boolean.TRUE){
                    text += " (" + NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "ERR_NoId") + ")";//NOI18N
                    disable = disableNoIdSelection;
                }
            }
            if (text == null) {
                text = value.toString();
                if(disableNoIdSelection && available && entityClosure.haveId(text)!=Boolean.TRUE && entityClosure.getEntity(text)!=null){
                    text += " (" + NbBundle.getMessage(PersistenceClientEntitySelectionVisual.class, "ERR_NoId") + ")";//NOI18N
                    disable = disableNoIdSelection;
                }
            }
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            if(text.length() == 0) {
                text = " ";
            }
            label.setEnabled((entityClosure.getAvailableEntities().contains(value) || entityClosure.getWantedEntities().contains(value)) && !disable);
            //setFont(list.getFont());
            label.setText(text);
            return label;
        }
    }
}
