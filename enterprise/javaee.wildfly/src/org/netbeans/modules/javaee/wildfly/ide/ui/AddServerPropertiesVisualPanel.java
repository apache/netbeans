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
package org.netbeans.modules.javaee.wildfly.ide.ui;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.NbBundle;


/**
 *
 * @author Ivan Sidorkin
 */
public class AddServerPropertiesVisualPanel extends JPanel {

    private final Set<ChangeListener> listeners = ConcurrentHashMap.newKeySet();

    private javax.swing.JComboBox  domainField;  // Domain name (list of registered domains) can be edited
    private javax.swing.JTextField domainPathField;  //
    private javax.swing.JLabel     domainLabel;
    private javax.swing.JLabel     domainPathLabel;
    private javax.swing.JLabel     label1;
    private javax.swing.JPanel     panel1;
    private javax.swing.JLabel     hostLabel;
    private javax.swing.JTextField hostField;
    private javax.swing.JLabel     portLabel;
    private javax.swing.JTextField portField;
    private javax.swing.JLabel     managementPortLabel;
    private javax.swing.JTextField managementPortField;
    private javax.swing.JLabel     portOffsetLabel;
    private javax.swing.JTextField portOffsetField;
    private javax.swing.JLabel     userLabel;
    private javax.swing.JTextField userField;
    private javax.swing.JLabel     passwordLabel;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JComboBox  serverType;  // Local or Remote


    /** Creates a new instance of AddServerPropertiesVisualPanel */
    public AddServerPropertiesVisualPanel() {
        init();
        setName(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "TITLE_ServerProperties")); //NOI18N
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public void removeChangeListener(ChangeListener l ) {
        listeners.remove(l);
    }

    private void somethingChanged() {
        fireChangeEvent();
    }

    private void fireChangeEvent() {
        ChangeEvent ev = new ChangeEvent(this);
        new ArrayList<>(listeners).forEach(l -> l.stateChanged(ev));
    }

    public boolean isLocalServer(){
        return  ("Local".equals(serverType.getSelectedItem()));
    }

    public String getHost(){
        return hostField.getText().trim();
    }

    public String getPort(){
        return portField.getText().trim();
    }
 
    public String getPortOffSet(){
        return portOffsetField.getText().trim();
    }

    public String getManagementPort(){
        return managementPortField.getText().trim();
    }

    public String getUser(){
        return userField.getText();
    }

    public String getPassword(){
        return new String(passwordField.getPassword());
    }

    public String getDomainPath(){
        return domainPathField.getText();
    }

    public String getDomain(){
        return (String)domainField.getSelectedItem();
    }

    private void domainChanged(){
        DomainComboModel model = (DomainComboModel)domainField.getModel();
        String path = model.getCurrentPath();
        domainPathField.setText(path);
        portField.setText(WildflyPluginUtils.getHTTPConnectorPort(path));
        managementPortField.setText(WildflyPluginUtils.getManagementConnectorPort(path));
        fireChangeEvent();
    }

    void installLocationChanged() {
        DomainComboModel domainModel = (DomainComboModel) domainField.getModel();
        String serverLocation = WildflyPluginProperties.getInstance().getInstallLocation();
        domainModel.setDomains(WildflyPluginUtils.getRegisteredDomains(serverLocation));
        String configLocation = WildflyPluginProperties.getInstance().getConfigLocation();
        File folder = new File(configLocation).getParentFile();
        if (folder != null) {
            folder = folder.getParentFile();
        }
        File domainDir = folder;
        if (domainDir != null) {
            if (domainModel.hasDomain(domainDir.getName())) {
                domainModel.setSelectedItem(domainDir.getName());
            } else {
                domainModel.addDomain(domainDir.getName(), domainDir.getAbsolutePath());
            }
        } else {
            domainModel.setSelectedItem(null);
        }
        domainChanged();
    }

    private void serverTypeChanged(){

        if (isLocalServer()){  //NOI18N
            domainLabel.setVisible(true);
            domainField.setVisible(true);

            domainPathLabel.setVisible(true);
            domainPathField.setVisible(true);

            hostField.setEditable(true);
        } else {  // REMOTE

            domainLabel.setVisible(false);
            domainField.setVisible(false);

            domainPathLabel.setVisible(false);
            domainPathField.setVisible(false);

            hostField.setEditable(true);
        }

        somethingChanged();
    }

    private void init(){
        java.awt.GridBagConstraints gridBagConstraints;

        label1 = new JLabel(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "TXT_PROPERTY_TEXT")); //NOI18N

        serverType = new JComboBox(new String[]{"Local","Remote"});//NOI18N
        serverType.addActionListener((ActionEvent e) -> serverTypeChanged());


        domainPathLabel = new JLabel(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_DomainPath"));//NOI18N
        domainPathField = new JTextField();
        domainPathField.setColumns(20);
        domainPathField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_DomainPath"));
        domainPathField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_DomainPath"));


        panel1 = new JPanel();

        //Domain combobox
        domainLabel = new JLabel();
        String serverLocation = WildflyPluginProperties.getInstance().getInstallLocation();
        domainField = new JComboBox(new DomainComboModel(WildflyPluginUtils.getRegisteredDomains(serverLocation)));
        domainField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_Domain"));
        domainField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_Domain"));

        domainField.addActionListener((ActionEvent e) -> domainChanged());

        domainLabel.setLabelFor(domainField);
        org.openide.awt.Mnemonics.setLocalizedText(domainLabel, org.openide.util.NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_Domain")); // NOI18N

        hostLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_Host")); // NOI18N

        hostField = new JTextField();
        hostField.setColumns(20);
        hostField.setEditable(true);
        hostField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_Host"));
        hostField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_Host"));
        hostField.addKeyListener(new SomeChangesListener());

        hostLabel.setLabelFor(hostField);

        portLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(portLabel, NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_Port")); // NOI18N

        portField = new JTextField();
        portField.setColumns(20);
        portField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_Port"));
        portField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_Port"));
        portField.addKeyListener(new SomeChangesListener());

        portLabel.setLabelFor(portField);

        managementPortLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(managementPortLabel, NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_Management_Port")); // NOI18N

        managementPortField = new JTextField();
        managementPortField.setColumns(20);
        managementPortField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_Management_Port"));
        managementPortField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_Management_Port"));
        managementPortField.addKeyListener(new SomeChangesListener());

        managementPortLabel.setLabelFor(managementPortField);

        portOffsetLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(portOffsetLabel, NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_Port_Offset")); // NOI18N

        portOffsetField = new JTextField();
        portOffsetField.setColumns(20);
        portOffsetField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_Port_Offset"));
        portOffsetField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_Port_Offset"));
        portOffsetField.addKeyListener(new SomeChangesListener());

        portOffsetLabel.setLabelFor(portOffsetField);

        userLabel = new JLabel(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_User"));//NOI18N
        userField = new JTextField();
        userField.addKeyListener(new SomeChangesListener());

        passwordLabel = new JLabel(NbBundle.getMessage(AddServerPropertiesVisualPanel.class, "LBL_Password"));//NOI18N
        passwordField = new JPasswordField();
        passwordField.addKeyListener(new SomeChangesListener());


        setLayout(new java.awt.GridBagLayout());

        setFocusable(false);

        setMinimumSize(new java.awt.Dimension(280, 217));
        //-------------- some label --------------------
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(label1, gridBagConstraints);



        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridx = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);

        add(serverType, gridBagConstraints);


        //-------------- domain ---------------
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 6);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(domainLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(domainField, gridBagConstraints);




        //-------------- domain path ---------------
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 6);
        add(domainPathLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(domainPathField, gridBagConstraints);

        //-------------- host ---------------
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 6);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(hostLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(hostField, gridBagConstraints);


        //-------------- port ---------------
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 6);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(portLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(portField, gridBagConstraints);

        //-------------- management port ---------------
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 6);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(managementPortLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(managementPortField, gridBagConstraints);

        //-------------- port offset ---------------
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 6);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(portOffsetLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(portOffsetField, gridBagConstraints);


        //-------------- User ---------------
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 6);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(userLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(userField, gridBagConstraints);



        //-------------- Password ---------------
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 6);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(passwordLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(passwordField, gridBagConstraints);

        //-------------  panell to fill out free space ------------------------
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;


        domainPathField.setEnabled(false);

        userField.setVisible(true);
        userLabel.setVisible(true);
        passwordField.setVisible(true);
        passwordLabel.setVisible(true);


        serverType.setVisible(false);

        add(panel1, gridBagConstraints);

        hostField.setText("localhost");//NOI18N
        portField.setText(WildflyPluginUtils.getHTTPConnectorPort(domainPathField.getText()));//NOI18N
        portOffsetField.setText("0");//NOI18N
        domainChanged();

    }


    private class SomeChangesListener implements KeyListener{

        @Override
        public void keyTyped(KeyEvent e){}

        @Override
        public void keyPressed(KeyEvent e){}

        @Override
        public void keyReleased(KeyEvent e){ somethingChanged();}

    }

}


class DomainComboModel extends AbstractListModel implements ComboBoxModel{
    private int current = -1;
    private String[][] domains = null;


    public void addDomain(String domain, String path){
        String[][] newDomains = new String[domains.length+1][2];
        int i = 0;
        for(;i<domains.length; i++){
            newDomains[i][0] = domains[i][0];
            newDomains[i][1] = domains[i][1];
        }
        newDomains[i][0] = domain;
        newDomains[i][1] = path;
        domains = newDomains;

    }

    public DomainComboModel(Map domains){
        setDomains(domains);
    }

    public void setDomains(Map domains) {

        current = -1;
        this.domains = null;

        int len = domains.size();
        this.domains = new String[len][2];
        Set en = domains.keySet();

        if (len > 0) current = 0;

        int i = 0;
        for(Object key : en) {
            this.domains[i][0] = (String)key;
            this.domains[i][1] = (String)domains.get(this.domains[i][0]);
            if(this.domains[i][0].equalsIgnoreCase("default")) //NOI18N
                current=i;
            i++;
        }
    }

    @Override
    public Object  getSelectedItem() {
        if (current ==-1 )
            return "";
        return domains[current][0];
    }

    @Override
    public void setSelectedItem(Object anItem) {
        for (int i = 0; i < getSize(); i++){
            if (domains[i][0].equals(anItem)){
                current = i;
                fireContentsChanged(this, -1, -1);
                return;
            }
        }
        current = -1;
        //currentVal = (String)anItem;
        fireContentsChanged(this, -1, -1);
    }

    @Override
    public Object getElementAt(int index){
        return domains[index][0];
    }

    @Override
    public int 	getSize(){
        return domains.length;
    }
    //----------------------------------------------------

    public String getCurrentPath(){
        if (current == -1) return "";
        return domains[current][1];
    }

    public boolean hasDomain(String domain){
        for (int i = 0; i < getSize(); i++){
            if (domains[i][0].equals(domain)){
                return true;
            }
        }
        return false;
    }

}
