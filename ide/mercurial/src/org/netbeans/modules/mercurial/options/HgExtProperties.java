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
package org.netbeans.modules.mercurial.options;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.Properties;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.ui.properties.HgPropertiesNode;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Padraig O'Briain
 */
public class HgExtProperties implements ActionListener, DocumentListener {
    
    private PropertiesPanel panel;
    private File root;
    private PropertiesTable propTable;
    private HgProgressSupport support;
    private File loadedValueFile;
    private Font fontTextArea;
    
    /** Creates a new instance of HgExtProperties */
    public HgExtProperties(PropertiesPanel panel, PropertiesTable propTable, File root) {
        this.panel = panel;
        this.propTable = propTable;
        this.root = root;
        panel.getTxtAreaValue().getDocument().addDocumentListener(this);
        ((JTextField) panel.getComboName().getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
        propTable.getTable().addMouseListener(new TableMouseListener());
        panel.getBtnAdd().addActionListener(this);
        panel.getBtnRemove().addActionListener(this);
        panel.getComboName().setEditable(true);
        panel.getBtnAdd().setEnabled(false);
        initPropertyNameCbx();
        refreshProperties(); 
    }
    
    public PropertiesPanel getPropertiesPanel() {
        return panel;
    }
    
    public void setPropertiesPanel(PropertiesPanel panel) {
        this.panel = panel;
    }
    
    public File getRoot() {
        return root;
    }
    
    public void setRoot(File root) {
        this.root = root;
    }
    
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        
        if (source.equals(panel.getBtnAdd())) {
            addProperty();
        }
        
        if (source.equals(panel.getBtnRemove())) {
            removeProperties();
        }
        
    }
    
    protected void initPropertyNameCbx() {
        List<String> lstName = new ArrayList<String>(8);

        ComboBoxModel comboModel = new DefaultComboBoxModel(new Vector<String>(lstName));
        panel.getComboName().setModel(comboModel);
        panel.getComboName().getEditor().setItem(""); // NOI18N
    }
    
    protected String getPropertyValue() {
        return panel.getTxtAreaValue().getText();
    }
    
    protected String getPropertyName() {
        Object selectedItem = panel.getComboName().getSelectedObjects()[0];
        if (selectedItem == null) {
            return panel.getComboName().getEditor().getItem().toString().trim();
        } else {
            return selectedItem.toString().trim();
        }
    }
    
    protected void refreshProperties() {        
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor();
        try {
            support = new HgProgressSupport() {
                protected void perform() {
                    Properties props = HgModuleConfig.getDefault().getProperties(root, "extensions"); // NOI18N
                    final HgPropertiesNode[] hgProps = new HgPropertiesNode[props.size()];
                    int i = 0;

                    for (Enumeration e = props.propertyNames(); e.hasMoreElements() ; ) {
                        String name = (String) e.nextElement();
                        String tmp = props.getProperty(name);
                        String value = tmp != null ? tmp : ""; // NOI18N
                        hgProps[i] = new HgPropertiesNode(name, value);
                        i++;
                    }
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            propTable.setNodes(hgProps);
                            setSelected(0);
                            propTable.getTable().getSelectionModel().setSelectionInterval(0,0);
                        }
                    });
                }
            };
            support.start(rp, (HgURL) null, org.openide.util.NbBundle.getMessage(HgExtProperties.class, "LBL_Properties_Progress")); // NOI18N
        } finally {
            support = null;
        }
    }
    
    private boolean addProperty(String name, String value) {
        HgPropertiesNode[] hgPropertiesNodes = propTable.getNodes();
        for (int i = 0; i < hgPropertiesNodes.length; i++) {
            String hgPropertyName = hgPropertiesNodes[propTable.getModelIndex(i)].getName(); 
            if (hgPropertyName.equals(name)) {
                hgPropertiesNodes[propTable.getModelIndex(i)].setValue(value); 
                propTable.setNodes(hgPropertiesNodes);
                return true;
            } 
        }
        HgPropertiesNode[] hgProps = new HgPropertiesNode[hgPropertiesNodes.length + 1];
        for (int i = 0; i < hgPropertiesNodes.length; i++) {
            hgProps[i] = hgPropertiesNodes[i];
        }
        hgProps[hgPropertiesNodes.length] = new HgPropertiesNode(name, value);
        propTable.setNodes(hgProps); 
        return true;
    }

    public void addProperty() {
        if (addProperty(getPropertyName(), getPropertyValue())) {
            panel.getComboName().getEditor().setItem(""); // NOI18N
            panel.getTxtAreaValue().setText(""); // NOI18N
        }
    }
    
    public void setProperties() {
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor();
        try {
            support = new HgProgressSupport() {
                protected void perform() {
                    try {
                        HgModuleConfig.getDefault().clearProperties(root, "extensions"); // NOI18N
                        HgPropertiesNode[] hgPropertiesNodes = propTable.getNodes();
                        for (int i = 0; i < hgPropertiesNodes.length; i++) {
                            String hgPropertyName = hgPropertiesNodes[propTable.getModelIndex(i)].getName();
                            String hgPropertyValue = hgPropertiesNodes[propTable.getModelIndex(i)].getValue();
                            HgModuleConfig.getDefault().setProperty(root, "extensions", hgPropertyName, hgPropertyValue, true); // NOI18N
                        }
                    } catch (IOException ex) {
                        HgModuleConfig.notifyParsingError();
                    }
                }
            };
            support.start(rp, (HgURL) null, org.openide.util.NbBundle.getMessage(HgExtProperties.class, "LBL_Properties_Progress")); // NOI18N
        } finally {
            support = null;
        }
    }

    public void removeProperties() {
        final int[] rows = propTable.getSelectedItems();       
        // No rows selected
        if (rows.length == 0) return;
        HgPropertiesNode[] hgPropertiesNodes = propTable.getNodes();
        if (hgPropertiesNodes.length == 0) return; // no prop to remove
        HgPropertiesNode[] hgProps = new HgPropertiesNode[hgPropertiesNodes.length - rows.length];        
        // Translate view index to model index
        for(int i = 0; i < rows.length; i++){
            rows[i] = propTable.getModelIndex(rows[i]);
        }
        int j = 0;
        int k = 0;
        for (int i = 0; i < hgPropertiesNodes.length; i++) {
            if (i != rows[j]) {
                hgProps[k++] = hgPropertiesNodes[i];
            } else {
                if (j < rows.length - 1) j++;
            }
        }
        propTable.setNodes(hgProps);
        panel.getComboName().getEditor().setItem(""); // NOI18N
        panel.getTxtAreaValue().setText(""); // NOI18N
    }
    
    public void insertUpdate(DocumentEvent event) {
        validateUserInput(event);
    }

    public void removeUpdate(DocumentEvent event) {
        validateUserInput(event);
    }

    public void changedUpdate(DocumentEvent event) {
        validateUserInput(event);
    }
    
    private void validateUserInput(DocumentEvent event) {
        
        Document doc = event.getDocument();
        String name = panel.getComboName().getEditor().getItem().toString().trim();
        String value = panel.getTxtAreaValue().getText().trim();
        
        if (name.length() == 0 || name.indexOf(" ") > 0) { // NOI18N
            panel.getBtnAdd().setEnabled(false);
        } else {
            panel.getBtnAdd().setEnabled(true);
        }
    }    

    private void setSelected(int index) {
        HgPropertiesNode[] hgPropertiesNodes = propTable.getNodes();
        if (hgPropertiesNodes == null || hgPropertiesNodes.length == 0 || index < 0) {
            return;
        }
        final String hgPropertyName = hgPropertiesNodes[propTable.getModelIndex(index)].getName();
        final String hgPropertyValue = hgPropertiesNodes[propTable.getModelIndex(index)].getValue();
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                ComboBoxModel targetsModel;
                Set<String> initialSet = new LinkedHashSet<String>();
                initialSet.add(hgPropertyName);
                targetsModel = new DefaultComboBoxModel(new Vector<String>(initialSet));
                panel.getComboName().setModel(targetsModel);
                panel.getTxtAreaValue().setText(hgPropertyValue);               
            }
        });
    }

    public class TableMouseListener extends MouseAdapter {
        
        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getClickCount() == 1) {
                int[] rows = propTable.getSelectedItems();
                if(rows != null && rows.length > 0) 
                    setSelected(rows[0]);
            }
        }
}
}
