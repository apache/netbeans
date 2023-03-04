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

package org.netbeans.modules.j2ee.persistence.wizard.unit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.support.DatabaseExplorerUIs;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.core.api.support.Strings;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.util.PersistenceProviderComboboxHelper;
import org.netbeans.modules.j2ee.persistence.util.SourceLevelChecker;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel.TableGeneration;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  Martin Adamek
 */
public class PersistenceUnitWizardPanelJdbc extends PersistenceUnitWizardPanel{
    private final RequestProcessor RP = new RequestProcessor(PersistenceUnitWizardPanelJdbc.class.getSimpleName(), 2);

    public PersistenceUnitWizardPanelJdbc(Project project, ChangeListener changeListener,  boolean editName) {
        this(project, changeListener, editName, TableGeneration.CREATE);
    }
    
    public PersistenceUnitWizardPanelJdbc(final Project project, ChangeListener changeListener,
            boolean editName, TableGeneration tg) {
            
        super(project);
        initComponents();
        setTableGeneration(tg);
        libraryCombo.setEnabled(false);
        
        RP.post( () -> {
            PersistenceProviderComboboxHelper comboHelper = new PersistenceProviderComboboxHelper(project);
            comboHelper.connect(libraryCombo);
            libraryCombo.setEnabled(true);
            checkValidity(); 
        });
        
        unitNameTextField.setText(Util.getCandidateName(project));
        unitNameTextField.selectAll();
        // unit name editing is not available when adding first PU
        unitNameTextField.setVisible(editName);
        unitNameLabel.setVisible(editName);
        
        DatabaseExplorerUIs.connect(jdbcCombo, ConnectionManager.getDefault());
        jdbcCombo.addItemListener( (ItemEvent e) -> checkValidity() );
        
        unitNameTextField.getDocument().addDocumentListener(new ValidationListener());
        errorMessage.setForeground(Color.RED);
        updateWarning();
    }
    
    
    /**
     * Pre-selects appropriate table generation strategy radio button.
     */
    private void setTableGeneration(TableGeneration tg){
        if (TableGeneration.CREATE.equals(tg)){
            ddlCreate.setSelected(true);
        } else if (TableGeneration.DROP_CREATE.equals(tg)){
            ddlDropCreate.setSelected(true);
        } else {
            ddlUnkown.setSelected(true);
        }
    }
    
    /**
     * Checks whether this panel is in valid state (see <code>#isValidPanel()</code>)
     * and fires appropriate property changes.
     */
    private void checkValidity(){
        if (isValidPanel()) {
            firePropertyChange(IS_VALID, false, true);
        } else {
            firePropertyChange(IS_VALID, true, false);
        }
    }
    
    
    
    @Override
    public String getPersistenceUnitName() {
        return unitNameTextField.getText();
    }
    
    @Override
    public Provider getSelectedProvider(){
        return (Provider) libraryCombo.getSelectedItem();
    }
    
    public DatabaseConnection getPersistenceConnection() {
        return (DatabaseConnection) jdbcCombo.getSelectedItem();
    }
    
    @Override
    public void setPreselectedDB(String db) {
        boolean hasItem = false;
        for (int i = 0; i < jdbcCombo.getItemCount(); i++) {
            if (jdbcCombo.getItemAt(i) instanceof DatabaseConnection) {
                DatabaseConnection conn = (DatabaseConnection) jdbcCombo.getItemAt(i);
                if (conn.getDisplayName().equals(db) || conn.toString().equals(db)) {
                    hasItem = true;
                    break;
                }
            }
        }
        jdbcCombo.setSelectedItem(ConnectionManager.getDefault().getConnection(db));
        jdbcCombo.setEnabled(!hasItem);
    }
    
    @Override
    public String getTableGeneration() {
        if (ddlCreate.isSelected()) {
            return Provider.TABLE_GENERATION_CREATE;
        } else if (ddlDropCreate.isSelected()) {
            return Provider.TABLE_GENERATION_DROPCREATE;
        } else {
            return Provider.TABLE_GENERATTION_UNKOWN;
        }
    }
    
    @Override
    public boolean isValidPanel() {
        setErrorMessage("");
        Sources sources=ProjectUtils.getSources(project);
        SourceGroup groups[]=sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if(groups == null || groups.length == 0) {
            setErrorMessage(NbBundle.getMessage(PersistenceUnitWizardDescriptor.class,"ERR_JavaSourceGroup")); //NOI18N
            return false;
        }
        if( !libraryCombo.isEnabled() ) {
            setErrorMessage(NbBundle.getMessage(PersistenceUnitWizardPanelJdbc.class,"LBL_Wait")); //NOI18N
            return false;
        }
        if (!(libraryCombo.getSelectedItem() instanceof Provider)) {
            return false;
        }
        if (!(jdbcCombo.getSelectedItem() instanceof DatabaseConnection)) {
            return false;
        }
        try{
            if (!isNameValid()){
                return false;
            }
        } catch (InvalidPersistenceXmlException ipx){
            setErrorMessage(NbBundle.getMessage(PersistenceUnitWizardDescriptor.class,"ERR_InvalidPersistenceXml", ipx.getPath())); //NOI18N
            return false;
        }
        return true;
    }
    
    /**
     * Checks whether name of the persistence unit is valid, i.e. it's not
     * empty and it's unique.
     */
    private boolean isNameValid() throws InvalidPersistenceXmlException{
        return Strings.isEmpty(getPersistenceUnitName()) ? false : isNameUnique();
    }
    
    @Override
    public void setErrorMessage(String msg) {
        errorMessage.setText(msg);
        errorMessage.setVisible(msg!=null && msg.length()>0);
    }
    private void updateWarning() {
        Object provObj = libraryCombo.getSelectedItem();
        Provider prov = (Provider) (provObj instanceof Provider ? provObj : null);
        String warning = null;
        if(prov != null){
            String ver = ProviderUtil.getVersion(prov);
            if(ver!=null && !Persistence.VERSION_1_0.equals(ver)){
                if(Util.isJPAVersionSupported(project, ver)){
                    String sourceLevel = SourceLevelChecker.getSourceLevel(project);
                    if(sourceLevel !=null ){
                        if(sourceLevel.matches("1\\.[0-5]([^0-9].*)?")) {//1.0-1.5
                            warning  = NbBundle.getMessage(PersistenceUnitWizard.class, "ERR_WrongSourceLevel", sourceLevel);
                        }
                    }
                } else {
                    warning  = NbBundle.getMessage(PersistenceUnitWizard.class, "ERR_UnsupportedJpaVersion", ver, Util.getJPAVersionSupported(project, ver));
                }
            }
        }
        ImageIcon icon = null;
        if(warning != null){
            icon = ImageUtilities.loadImageIcon("org/netbeans/modules/j2ee/persistence/ui/resources/warning.gif", false); //NOI18N
        } else {
            warning = "  ";
        }
        createPUWarningLabel.setIcon(icon);
        createPUWarningLabel.setText(warning);
        createPUWarningLabel.setToolTipText(warning);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        tableCreationButtonGroup = new javax.swing.ButtonGroup();
        unitNameLabel = new javax.swing.JLabel();
        unitNameTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        ddlCreate = new javax.swing.JRadioButton();
        ddlDropCreate = new javax.swing.JRadioButton();
        ddlUnkown = new javax.swing.JRadioButton();
        libraryLabel = new javax.swing.JLabel();
        libraryCombo = new javax.swing.JComboBox();
        jdbcCombo = new javax.swing.JComboBox();
        jdbcLabel = new javax.swing.JLabel();
        warnPanel = new javax.swing.JPanel();
        errorMessage = new javax.swing.JLabel();
        createPUWarningLabel = new ShyLabel();

        setName(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelJdbc.class, "LBL_Step1")); // NOI18N

        unitNameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("MN_UnitName").charAt(0));
        unitNameLabel.setLabelFor(unitNameTextField);
        unitNameLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelJdbc.class, "LBL_UnitName")); // NOI18N

        unitNameTextField.setColumns(40);
        unitNameTextField.setText("em");

        jLabel1.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelJdbc.class, "LBL_SpecifyPersistenceProvider")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelJdbc.class, "LBL_TableGeneration")); // NOI18N

        tableCreationButtonGroup.add(ddlCreate);
        ddlCreate.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("CHB_Create_mnem").charAt(0));
        ddlCreate.setSelected(true);
        ddlCreate.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelJdbc.class, "LBL_Create")); // NOI18N
        ddlCreate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        tableCreationButtonGroup.add(ddlDropCreate);
        ddlDropCreate.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("CHB_DropCreate_mnem").charAt(0));
        ddlDropCreate.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelJdbc.class, "LBL_DropCreate")); // NOI18N
        ddlDropCreate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        tableCreationButtonGroup.add(ddlUnkown);
        ddlUnkown.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("CHB_None_mnem").charAt(0));
        ddlUnkown.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelJdbc.class, "LBL_None")); // NOI18N
        ddlUnkown.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        libraryLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("MN_Library").charAt(0));
        libraryLabel.setLabelFor(libraryCombo);
        libraryLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelJdbc.class, "LBL_Library")); // NOI18N

        libraryCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                libraryComboItemStateChanged(evt);
            }
        });
        libraryCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                libraryComboActionPerformed(evt);
            }
        });

        jdbcCombo.setRenderer(new JdbcListCellRenderer());

        jdbcLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("MN_JdbcConnection").charAt(0));
        jdbcLabel.setLabelFor(jdbcCombo);
        jdbcLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelJdbc.class, "LBL_JdbcConnection")); // NOI18N

        warnPanel.setLayout(new java.awt.BorderLayout());

        errorMessage.setPreferredSize(new java.awt.Dimension(0, 20));
        warnPanel.add(errorMessage, java.awt.BorderLayout.NORTH);

        createPUWarningLabel.setText(" ");
        createPUWarningLabel.setPreferredSize(new java.awt.Dimension(4, 20));
        warnPanel.add(createPUWarningLabel, java.awt.BorderLayout.PAGE_END);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(unitNameLabel)
                            .addComponent(libraryLabel)
                            .addComponent(jdbcLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(libraryCombo, 0, 372, Short.MAX_VALUE)
                            .addComponent(jdbcCombo, 0, 372, Short.MAX_VALUE)
                            .addComponent(unitNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE)))
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ddlCreate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ddlDropCreate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ddlUnkown))
                    .addComponent(warnPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 552, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(unitNameLabel)
                    .addComponent(unitNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(libraryLabel)
                    .addComponent(libraryCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jdbcLabel)
                    .addComponent(jdbcCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ddlCreate)
                    .addComponent(ddlDropCreate)
                    .addComponent(ddlUnkown)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(warnPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(112, Short.MAX_VALUE))
        );

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle"); // NOI18N
        unitNameLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_UnitName")); // NOI18N
        jLabel2.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_TableGeneration")); // NOI18N
        ddlCreate.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Create")); // NOI18N
        ddlDropCreate.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_DropCreate")); // NOI18N
        ddlUnkown.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_None")); // NOI18N
        libraryLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Library")); // NOI18N
        jdbcLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_JdbcConnection")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void libraryComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_libraryComboActionPerformed
        checkValidity();
    }//GEN-LAST:event_libraryComboActionPerformed

    private void libraryComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_libraryComboItemStateChanged
        updateWarning();
    }//GEN-LAST:event_libraryComboItemStateChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel createPUWarningLabel;
    private javax.swing.JRadioButton ddlCreate;
    private javax.swing.JRadioButton ddlDropCreate;
    private javax.swing.JRadioButton ddlUnkown;
    private javax.swing.JLabel errorMessage;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JComboBox jdbcCombo;
    private javax.swing.JLabel jdbcLabel;
    private javax.swing.JComboBox libraryCombo;
    private javax.swing.JLabel libraryLabel;
    private javax.swing.ButtonGroup tableCreationButtonGroup;
    private javax.swing.JLabel unitNameLabel;
    private javax.swing.JTextField unitNameTextField;
    private javax.swing.JPanel warnPanel;
    // End of variables declaration//GEN-END:variables
    
    /**
     * Document listener that invokes <code>checkValidity</code> when
     * changes are made.
     */
    private class ValidationListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            checkValidity();
        }
        @Override
        public void removeUpdate(DocumentEvent e) {
            checkValidity();
        }
        @Override
        public void changedUpdate(DocumentEvent e) {
            checkValidity();
        }
    }
    /**
     * A crude attempt at a label which doesn't expand its parent.
     */
    private static final class ShyLabel extends JLabel {

        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            size.width = 0;
            return size;
        }

        @Override
        public Dimension getMinimumSize() {
            Dimension size = super.getMinimumSize();
            size.width = 0;
            return size;
        }
    }
}
