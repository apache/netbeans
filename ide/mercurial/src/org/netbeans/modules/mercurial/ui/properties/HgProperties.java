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
package org.netbeans.modules.mercurial.ui.properties;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Enumeration;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Padraig O'Briain
 */
public class HgProperties implements ListSelectionListener {
    
    public static final String HGPROPNAME_USERNAME = "username"; // NOI18N
    public static final String HGPROPNAME_DEFAULT_PULL = "default-pull"; // NOI18N
    public static final String HGPROPNAME_DEFAULT_PUSH = "default-push"; // NOI18N

    private PropertiesPanel panel;
    private File root;
    private PropertiesTable propTable;
    private HgProgressSupport support;
    private File loadedValueFile;
    private Font fontTextArea;
    private HgPropertiesNode[] initHgProps;
    
    /** Creates a new instance of HgProperties */
    public HgProperties(PropertiesPanel panel, PropertiesTable propTable, File root) {
        this.panel = panel;
        this.propTable = propTable;
        this.root = root;
        propTable.getTable().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        propTable.getTable().getSelectionModel().addListSelectionListener(this);
        
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
    
    protected String getPropertyValue() {
        return panel.txtAreaValue.getText();
    }
    
    protected void refreshProperties() {        
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        try {
            support = new HgProgressSupport() {
                protected void perform() {
                    Properties props = HgModuleConfig.getDefault().getProperties(root);
                    if (props == null) {
                        return;
                    }
                    HgPropertiesNode[] hgProps = new HgPropertiesNode[props.size()];
                    initHgProps = new HgPropertiesNode[props.size()];
                    int i = 0;

                    for (Enumeration e = props.propertyNames(); e.hasMoreElements() ; ) {
                        String name = (String) e.nextElement();
                        String tmp = props.getProperty(name);
                        String value = tmp != null ? tmp : ""; // NOI18N
                        hgProps[i] = new HgPropertiesNode(name, value);
                        initHgProps[i] = new HgPropertiesNode(name, value);
                        i++;
                     }
                     propTable.setNodes(hgProps);
                }
            };
            support.start(rp, root, org.openide.util.NbBundle.getMessage(HgProperties.class, "LBL_Properties_Progress")); // NOI18N
        } finally {
            support = null;
        }
    }
    
    public void setProperties() {
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        try {
            support = new HgProgressSupport() {
                protected void perform() {
                    HgPropertiesNode[] hgPropertiesNodes = propTable.getNodes();
                    try {
                    for (int i = 0; i < hgPropertiesNodes.length; i++) {
                            String hgPropertyName = hgPropertiesNodes[i].getName();
                            String hgPropertyValue = hgPropertiesNodes[i].getValue();
                            boolean bPropChanged = !(initHgProps[i].getValue()).equals(hgPropertyValue);
                            if (bPropChanged && hgPropertyValue.trim().length() >= 0) {
                                if (hgPropertyName.equals(HGPROPNAME_USERNAME) &&
                                        !HgModuleConfig.getDefault().isUserNameValid(hgPropertyValue)) {
                                    JOptionPane.showMessageDialog(Utilities.findDialogParent(),
                                            NbBundle.getMessage(HgProperties.class, "MSG_WARN_USER_NAME_TEXT"), // NOI18N
                                            NbBundle.getMessage(HgProperties.class, "MSG_WARN_FIELD_TITLE"), // NOI18N
                                            JOptionPane.WARNING_MESSAGE);
                                } else {
                                    HgModuleConfig.getDefault().setProperty(root, hgPropertyName, hgPropertyValue);
                                }
                            }
                        }
                    } catch (IOException ex) {
                        HgModuleConfig.notifyParsingError();
                    }
                    HgRepositoryContextCache.getInstance().reset();
                }
            };
            support.start(rp, root, org.openide.util.NbBundle.getMessage(HgProperties.class, "LBL_Properties_Progress")); // NOI18N
        } finally {
            support = null;
        }
    }

    private int lastIndex = -1;
    
    
    public void updateLastSelection () {
        HgPropertiesNode[] hgPropertiesNodes = propTable.getNodes();
        if (lastIndex >= 0) {
            hgPropertiesNodes[lastIndex].setValue(getPropertyValue());
        }
    }

    public void valueChanged (ListSelectionEvent e) {
        int index = propTable.getTable().getSelectedRow();
        if (index < 0) {
            lastIndex = -1;
            return;
        }
        HgPropertiesNode[] hgPropertiesNodes = propTable.getNodes();
        if (lastIndex >= 0) {
            hgPropertiesNodes[lastIndex].setValue(getPropertyValue());
        }
        panel.txtAreaValue.setText(hgPropertiesNodes[index].getValue());
        lastIndex = index;
    }
}
