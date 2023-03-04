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
/*
 * CPVendorPanel.java -- synopsis.
 *
 */


package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.j2ee.sun.api.restricted.ResourceConfigurator;
import org.netbeans.modules.j2ee.sun.api.restricted.ResourceUtils;
import org.netbeans.modules.j2ee.sun.sunresources.beans.Field;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroupHelper;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldHelper;
import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.openide.filesystems.FileObject;
import org.openide.loaders.TemplateWizard;

public class CPVendorPanel extends javax.swing.JPanel
implements ChangeListener, DocumentListener, ListDataListener, WizardConstants {

    static final long serialVersionUID = 93474632245456421L;
    
    private ArrayList dbconns;
    private ResourceConfigHelper helper;
    private FieldGroup generalGroup, propGroup, vendorGroup;
    private boolean useExistingConnection = true;
    private final String[] vendors;
    private boolean firstTime = true;
    private boolean setupValid = true;
    
    private static final String CONST_TRUE = "true"; // NOI18N

    public final ResourceBundle bundle = ResourceBundle.getBundle("org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.Bundle"); //NOI18N
    private final List listeners = new ArrayList();

    protected final CPVendor panel;
    
    /** Creates new form DBSchemaConnectionpanel */
    @SuppressWarnings("LeakingThisInConstructor")
    public CPVendorPanel(CPVendor panel, ResourceConfigHelper helper, Wizard wiardInfo) {
        this.firstTime = true;
        this.panel = panel;
        this.helper = helper;

        this.generalGroup = FieldGroupHelper.getFieldGroup(wiardInfo, __General); 
        this.propGroup = FieldGroupHelper.getFieldGroup(wiardInfo, __Properties); 
        this.vendorGroup = FieldGroupHelper.getFieldGroup(wiardInfo, __PropertiesURL); 
        ButtonGroup bg = new ButtonGroup();
        dbconns = new ArrayList();
        
        setName(bundle.getString("TITLE_ConnPoolWizardPanel_dbConn")); //NOI18N

        initComponents ();
                
        nameLabel.setLabelFor(nameField);
        nameComboBox.registerKeyboardAction(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    nameComboBox.requestFocus();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);            

        bg.add(existingConnRadioButton);
        bg.add(newCofigRadioButton);
        bg.getSelection().addChangeListener(this);
        try{
            DatabaseConnection[] cons = ConnectionManager.getDefault().getConnections();
            for(int i=0; i < cons.length; i++){
                existingConnComboBox.addItem(cons[i].getName());
                dbconns.add(cons[i]);
            }
        }catch(Exception ex){
            // Connection could not be found
        }
        if (existingConnComboBox.getItemCount() == 0) {
            existingConnComboBox.insertItemAt(bundle.getString("NoConnection"), 0); //NOI18N
            newCofigRadioButton.setSelected(true);
            newCofigRadioButton.setEnabled(true);
            nameComboBox.setEnabled(true);
            existingConnComboBox.setEnabled(false);
        } else {
            existingConnComboBox.insertItemAt(bundle.getString("SelectFromTheList"), 0); //NOI18N
            existingConnRadioButton.setSelected(true);
            existingConnRadioButton.setEnabled(true);
            existingConnComboBox.setEnabled(true);
            nameComboBox.setEnabled(false);
            setExistingConnData();
        }
        
        Field vendorField = FieldHelper.getField(this.generalGroup, __DatabaseVendor);
        vendors = FieldHelper.getTags(vendorField);
        for (int i = 0; i < vendors.length; i++) {
            nameComboBox.addItem(bundle.getString("DBVendor_" + vendors[i]));   //NOI18N
        }
        
        if (nameComboBox.getItemCount() == 0) {
            nameComboBox.insertItemAt(bundle.getString("NoTemplate"), 0); //NOI18N
        } else {
            nameComboBox.insertItemAt(bundle.getString("SelectFromTheList"), 0); //NOI18N
        }
        nameComboBox.setSelectedIndex(0);
        
        existingConnComboBox.getModel().addListDataListener(this);
        nameComboBox.getModel().addListDataListener(this);
        isXA.setSelected(helper.getData().getString(__IsXA).equals(CONST_TRUE));  //NOI18N
        isXA.addChangeListener(this);
        newCofigRadioButton.addChangeListener(this);
        
        this.firstTime = false;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        descriptionTextArea = new javax.swing.JTextArea();
        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        existingConnRadioButton = new javax.swing.JRadioButton();
        existingConnComboBox = new javax.swing.JComboBox();
        newCofigRadioButton = new javax.swing.JRadioButton();
        nameComboBox = new javax.swing.JComboBox();
        isXA = new javax.swing.JCheckBox();

        setMaximumSize(new java.awt.Dimension(600, 350));
        setMinimumSize(new java.awt.Dimension(600, 350));
        setPreferredSize(new java.awt.Dimension(600, 350));

        descriptionTextArea.setColumns(20);
        descriptionTextArea.setEditable(false);
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setRows(5);
        descriptionTextArea.setText(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "Description")); // NOI18N
        descriptionTextArea.setWrapStyleWord(true);
        descriptionTextArea.setFocusable(false);
        descriptionTextArea.setOpaque(false);
        descriptionTextArea.setRequestFocusEnabled(false);
        descriptionTextArea.setVerifyInputWhenFocusTarget(false);

        nameLabel.setLabelFor(nameField);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "LBL_pool-name")); // NOI18N

        nameField.setText(this.helper.getData().getString(__Name));
        nameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CPVendorPanel.this.nameFieldActionPerformed(evt);
            }
        });
        nameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                CPVendorPanel.this.nameFieldKeyReleased(evt);
            }
        });

        existingConnRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(existingConnRadioButton, org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "ExistingConnection")); // NOI18N

        existingConnComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CPVendorPanel.this.existingConnComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(newCofigRadioButton, org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "NewConfiguration")); // NOI18N

        nameComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CPVendorPanel.this.nameComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(isXA, org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "isXA")); // NOI18N
        isXA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CPVendorPanel.this.isXAActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(existingConnComboBox, 0, 535, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(isXA)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(nameComboBox, 0, 532, Short.MAX_VALUE))
                    .addComponent(newCofigRadioButton)
                    .addComponent(existingConnRadioButton)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(nameLabel)
                        .addGap(10, 10, 10)
                        .addComponent(nameField, javax.swing.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(descriptionTextArea)
                .addGap(9, 9, 9))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(descriptionTextArea, javax.swing.GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE)
                .addGap(17, 17, 17)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameLabel))
                .addGap(18, 18, 18)
                .addComponent(existingConnRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(existingConnComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(newCofigRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(nameComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(isXA)
                .addGap(67, 67, 67))
        );

        descriptionTextArea.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "ACS_DescriptionA11yName")); // NOI18N
        descriptionTextArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "ACS_DescriptionA11yDesc")); // NOI18N
        nameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "ACS_pool-nameA11yDesc")); // NOI18N
        existingConnRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "ACS_ExistingConnectionA11yDesc")); // NOI18N
        existingConnComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "ACS_ExistingConnectionComboBoxA11yName")); // NOI18N
        existingConnComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "ACS_ExistingConnectionComboBoxA11yDesc")); // NOI18N
        newCofigRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "ACS_NewConnectionA11yDesc")); // NOI18N
        nameComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "ACS_NewConnectionComboBoxA11yName")); // NOI18N
        nameComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "ACS_NewConnectionComboBoxA11yDesc")); // NOI18N
        isXA.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "ACS_isXA_A11yDesc")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(11, 11, 11))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "TITLE_ConnPoolWizardPanel_dbConn")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "TITLE_ConnPoolWizardPanel_dbConn")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void nameFieldKeyReleased(java.awt.event.KeyEvent evt) {
        // Add your handling code here:
        ResourceConfigData data = this.helper.getData();
        String value = data.getString(__Name);
        String newValue = nameField.getText();
        if (!value.equals(newValue)) {
            this.helper.getData().setString(__Name, newValue);
        }
        fireChange();
    }

    private void nameFieldActionPerformed(java.awt.event.ActionEvent evt) {
        // Add your handling code here:
        setResourceName();
    }
    
    public String getNameField() {
        return nameField.getText();
    }
    
    private void setResourceName() {
        ResourceConfigData data = this.helper.getData();
        String value = data.getString(__Name);
        String newValue = nameField.getText();
        if (!value.equals(newValue)) {
            this.helper.getData().setString(__Name, newValue);
            fireChange();
        }
        
        if((this.getRootPane().getDefaultButton() != null) && (this.getRootPane().getDefaultButton().isEnabled())){
            this.getRootPane().getDefaultButton().doClick();
        }
    }
    
    private void isXAActionPerformed(java.awt.event.ActionEvent evt) {
        // Add your handling code here:
        setNewConfigData(false); 
    }

    private void nameComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
        // Add your handling code here:
        setNewConfigData(true);           
    }
    
    private void setNewConfigData(boolean replaceProps) {
        if (firstTime) {
            return;
        }
        int index = nameComboBox.getSelectedIndex();

        if (index > 0) {
            if (useExistingConnection) {
                useExistingConnection = false; 
            }
            ResourceConfigData data = this.helper.getData();
            data.setString(__IsCPExisting, "false"); //NOI18N
            String vendorName = vendors[index - 1];     
            String savedVendorName = data.getString(__DatabaseVendor);
            String savedXA = data.getString(__IsXA);
            String XA = isXA.isSelected()?CONST_TRUE:"false";  //NOI18N
            boolean vendorNotChanged = vendorName.equals(savedVendorName);
            boolean isXANotChanged = XA.equals(savedXA);

            if (vendorNotChanged && isXANotChanged) {
                return;
            }
            if (!vendorNotChanged) {
                data.setString(__DatabaseVendor, vendorName);
            }
            if (!isXANotChanged) {
                data.setString(__IsXA, XA);
            }
            
            setDataSourceClassNameAndResTypeInData(vendorName);
            
            if (replaceProps) {
                setPropertiesInData(vendorName);
            }
        }
    }
    
    private void setDataSourceClassNameAndResTypeInData(String vendorName) {
        //change datasource classname following database vendor change
        ResourceConfigData data = this.helper.getData();
        Field dsField;
        if (isXA.isSelected()) {
            dsField = FieldHelper.getField(this.generalGroup, __XADatasourceClassname);
        } else {
            dsField = FieldHelper.getField(this.generalGroup, __DatasourceClassname);
        }
        data.setString(__DatasourceClassname, FieldHelper.getConditionalFieldValue(dsField, vendorName));
        
        if (isXA.isSelected()) {
            data.setString(__ResType, "javax.sql.XADataSource");  //NOI18N
            data.setString(__IsXA, CONST_TRUE);  //NOI18N
        }else {
            data.setString(__ResType, "javax.sql.DataSource");  //NOI18N
            data.setString(__IsXA, "false");  //NOI18N
        }
    }
    
    @SuppressWarnings("UseOfObsoleteCollectionType")
    private void setPropertiesInData(String vendorName) {
        //change standard properties following database vendor change
        ResourceConfigData data = this.helper.getData();
        data.setProperties(new Vector());
        Field[] propFields = this.propGroup.getField();
        for (int i = 0; i < propFields.length; i++) {
            String value = FieldHelper.getConditionalFieldValue(propFields[i], vendorName);
            String name = propFields[i].getName();
            if (name.equals(__Url) && value.length() > 0) {
                data.addProperty(name, FieldHelper.toUrl(value));
            } else if (name.equals(__DatabaseName) && value.length() > 0) {
                data.addProperty(name, FieldHelper.toUrl(value));
            } else if (name.equals(__User) || name.equals(__Password)) {
                data.addProperty(propFields[i].getName(), value);
            }else{
                //All Others
                if(value.length() > 0 && (value.equals(__NotApplicable))){
                    data.addProperty(propFields[i].getName(), ""); //NOI18N
                }
            }
        }
    }
        
    private void existingConnComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
        setExistingConnData();
    }
    
    @SuppressWarnings("UseOfObsoleteCollectionType")
    private void setExistingConnData() {
        if(existingConnComboBox.getSelectedIndex() > 0) {
            if (!useExistingConnection) {
                this.helper.getData().setResourceName(__JdbcConnectionPool);
                useExistingConnection = true;  
            }
            this.helper.getData().setString(__IsCPExisting, CONST_TRUE); //NOI18N
            DatabaseConnection dbconn = (DatabaseConnection)dbconns.get(existingConnComboBox.getSelectedIndex() - 1);
            String url = dbconn.getDatabaseURL();
            String user = dbconn.getUser();
            String password = dbconn.getPassword();
            if(user != null && (password == null || password.trim().length() == 0)){ 
                password = "()"; //NOI18N
            }
            String tmpStr = url;
            
            Field urlField = FieldHelper.getField(this.vendorGroup, "vendorUrls"); //NOI18N
            String vendorName = FieldHelper.getOptionNameFromValue(urlField, tmpStr);
                        
            ResourceConfigData data = this.helper.getData();    
            data.setProperties(new Vector());
            data.setString(__DatabaseVendor, vendorName);
            
            if (vendorName.equals("pointbase")) {  //NOI18N
                data.addProperty(__DatabaseName, dbconn.getDatabaseURL());
            }else if(vendorName.startsWith("derby")) {  //NOI18N)
                setDerbyProps(vendorName, url);
            }else {
                data.addProperty(__Url, url);
            }
            data.addProperty(__User, user);
            data.addProperty(__Password, password);
            
            setDataSourceClassNameAndResTypeInData(vendorName);
        }

    }
    
    @SuppressWarnings("UseOfObsoleteCollectionType")
    private void setDerbyProps(String vendorName, String url) {
        //change standard properties following database vendor change
        ResourceConfigData data = this.helper.getData();
        data.setProperties(new Vector());
        data.addProperty(__Url, url);
        Field[] propFields = this.propGroup.getField();
        for (int i = 0; i < propFields.length; i++) {
            String value = FieldHelper.getConditionalFieldValue(propFields[i], vendorName);
            if(value.equals(__NotApplicable)){
                String name = propFields[i].getName();
                if(vendorName.equals("derby_net")) {//NOI18N
                    String hostName = "";
                    String portNumber = "";
                    String databaseName = "";
                    try{
                        String workingUrl = url.substring(url.indexOf("//") + 2, url.length());
                        ResourceConfigurator rci = new ResourceConfigurator();
                        hostName = rci.getDerbyServerName(workingUrl);
                        portNumber = rci.getDerbyPortNo(workingUrl);
                        databaseName = rci.getDerbyDatabaseName(workingUrl);
                    }catch(java.lang.StringIndexOutOfBoundsException ex){
                    }
                    if (name.equals(__DerbyPortNumber)) {
                        data.addProperty(name, portNumber);
                    } else if (name.equals(__DerbyDatabaseName)) {
                        data.addProperty(name, databaseName);
                    } else if (name.equals(__ServerName)) {
                        data.addProperty(name, hostName);
                    }
                }   
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JComboBox existingConnComboBox;
    private javax.swing.JRadioButton existingConnRadioButton;
    private javax.swing.JCheckBox isXA;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox nameComboBox;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JRadioButton newCofigRadioButton;
    // End of variables declaration//GEN-END:variables

    public boolean hasValidData() {
        if(! setupValid){
            panel.setErrorMsg(bundle.getString("Err_InvalidSetup"));
            return false;
        }
        panel.setErrorMsg(bundle.getString("Empty_String"));
        String name = nameField.getText();
        if (name == null || name.length() == 0){
            panel.setErrorMsg(bundle.getString("Err_InvalidName"));
            return false;
        }else if(! ResourceUtils.isLegalResourceName(name)){
            panel.setErrorMsg(bundle.getString("Err_InvalidName"));
            return false;
        }else if(! ResourceUtils.isUniqueFileName(name, this.helper.getData().getTargetFileObject(), __ConnectionPoolResource)){
            panel.setErrorMsg(bundle.getString("Err_DuplFileName"));
            return false;
        }
        
        if (existingConnRadioButton.isSelected()) {
            if (existingConnComboBox.getSelectedIndex() > 0) {
                return true;
            } else {
                panel.setErrorMsg(bundle.getString("Err_ChooseDBConn"));
            }
        }else if (newCofigRadioButton.isSelected()) {
            if (nameComboBox.getSelectedIndex() > 0) {
                return true;
            } else {
                panel.setErrorMsg(bundle.getString("Err_ChooseDBVendor"));
            }
        } 
        
        return false;
    }

    @Override
    public void removeUpdate(final javax.swing.event.DocumentEvent event) {
        fireChange();
    }
    
    @Override
    public void changedUpdate(final javax.swing.event.DocumentEvent event) {
        fireChange();
    }
    
    @Override
    public void insertUpdate(final javax.swing.event.DocumentEvent event) {
        fireChange();
    }

    @Override
    public void intervalAdded(final javax.swing.event.ListDataEvent p1) {
        fireChange();
    }
    
    @Override
    public void intervalRemoved(final javax.swing.event.ListDataEvent p1) {
        fireChange();
    }
    
    @Override
    public void contentsChanged(final javax.swing.event.ListDataEvent p1) {
        fireChange();
    }

    @Override
    public void stateChanged(final javax.swing.event.ChangeEvent p1) {
        if (firstTime) {
            return;
        }
        if (p1.getSource().getClass() == javax.swing.JToggleButton.ToggleButtonModel.class) {
            if (existingConnRadioButton.isSelected()) {
                //To solve a problem on Win2K only
                if (firstTime) {
                    return;
                }
                existingConnComboBox.setEnabled(true);
                nameComboBox.setEnabled(false);
//                isXA.setEnabled(false);
                setExistingConnData();
            } else {
                existingConnComboBox.setEnabled(false);
                nameComboBox.setEnabled(true);
                setNewConfigData(true);
            }  
        }
        fireChange();
    }
    
    public CPVendorPanel setFirstTime(boolean first) {
        this.firstTime = first;
        return this;
    }

    private void fireChange() {
        ChangeEvent event = new ChangeEvent(this);
        ArrayList tempList;

        synchronized (listeners) {
            tempList = new ArrayList(listeners);
        }

        Iterator iter = tempList.iterator();
        while (iter.hasNext()) {
            ((ChangeListener)iter.next()).stateChanged(event);
        }
    }

    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public void read(Object settings) {
        TemplateWizard wizard = (TemplateWizard) settings;
        String targetName = wizard.getTargetName();
        if (this.helper.getData().getString(__DynamicWizPanel).equals(CONST_TRUE)) { //NOI18N
            targetName = null;
        }
        FileObject setupFolder = ResourceUtils.getResourceDirectory(this.helper.getData().getTargetFileObject());
        this.helper.getData().setTargetFileObject(setupFolder);
        if (setupFolder != null) {
            String resourceName = this.helper.getData().getString(__Name);
            if ((resourceName != null) && (!resourceName.equals(""))) {
                targetName = resourceName;
            }
            targetName = ResourceUtils.createUniqueFileName(targetName, setupFolder, __ConnectionPoolResource);
            this.helper.getData().setTargetFile(targetName);
            this.nameField.setText(targetName);
            this.helper.getData().setString(__Name, targetName);
        } else {
            setupValid = false;
        }
    }
    
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void setInitialFocus(){
        new setFocus(nameField);
    }
    

}
