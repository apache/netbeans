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
 * CustomizerJVM.java
 *
 * Created on 20.07.2010, 15:25:26
 */

package org.netbeans.modules.j2ee.weblogic9.ui.nodes;

import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties.JvmVendor;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;

/**
 *
 * @author den
 */
class CustomizerJVM extends javax.swing.JPanel {

    private static final long serialVersionUID = 3411155308004602121L;

    private final WLDeploymentManager manager;
    
    CustomizerJVM(WLDeploymentManager manager) {
        this.manager = manager;
        initComponents();
        
        initValues();
    }

    private void initValues() {
        JvmVendor vendor = JvmVendor.fromPropertiesString(manager.getInstanceProperties().getProperty(
                WLPluginProperties.VENDOR));
        List<JvmVendor> vendors = new ArrayList<JvmVendor>(5);
        Properties runtimeProps = WLPluginProperties
                .getRuntimeProperties(manager.getInstanceProperties()
                        .getProperty(WLPluginProperties.DOMAIN_ROOT_ATTR));
        final String beaHome = runtimeProps.getProperty(WLPluginProperties.
                BEA_JAVA_HOME);
        final String sunHome = runtimeProps.getProperty(WLPluginProperties.
                SUN_JAVA_HOME);
        final Properties javaHomeProps = 
            (Properties) runtimeProps.get(WLPluginProperties.JAVA_HOME);
        if (beaHome != null && beaHome.trim().length() > 0) {
            vendors.add(WLPluginProperties.JvmVendor.ORACLE);
        }
        if (sunHome!= null && sunHome.trim().length() > 0) {
            vendors.add(WLPluginProperties.JvmVendor.SUN);
        }
        vendors.add(WLPluginProperties.JvmVendor.DEFAULT);
        
        for (Enumeration<Object> keys = javaHomeProps.keys(); keys.hasMoreElements();) {
            String key = (String) keys.nextElement();
            if (key.length() > 0 && !key.equals(JvmVendor.SUN.toPropertiesString()) && 
                    !key.equals(JvmVendor.ORACLE.toPropertiesString())) {
                vendors.add(JvmVendor.fromPropertiesString(key));
            }            
        }
        
        vendorName.setModel(new DefaultComboBoxModel(vendors.toArray()));
        vendorName.setSelectedItem(vendor);
        
        if (vendor == JvmVendor.DEFAULT) {
            javaHome.setText(javaHomeProps.getProperty("")); // NOI18N
        } else if (vendor == JvmVendor.ORACLE) {
            javaHome.setText(beaHome);
        } else if (vendor == JvmVendor.SUN) {
            javaHome.setText(sunHome);
        } else {
            javaHome.setText(javaHomeProps.getProperty(vendor.toPropertiesString()));
        }
        
        vendorName.addItemListener( new ItemListener() {
            
            @Override
            public void itemStateChanged(ItemEvent event) {
                JvmVendor item = (JvmVendor) event.getItem();
                if (item == JvmVendor.DEFAULT) {
                    javaHome.setText(javaHomeProps.getProperty(""));
                } else {
                    if (item == JvmVendor.ORACLE) {
                        javaHome.setText(beaHome);
                    } else if (item == JvmVendor.SUN) {
                        javaHome.setText(sunHome);
                    } else {
                        javaHome.setText(javaHomeProps.getProperty(item.toPropertiesString()));
                    }
                }
                manager.getInstanceProperties().setProperty(
                        WLPluginProperties.VENDOR, item.toPropertiesString());                
            }
        });
        
        String javaOpts = manager.getInstanceProperties().getProperty(
                WLPluginProperties.JAVA_OPTS);
        if (javaOpts != null) {
            vmOptions.setText(javaOpts.trim());
        }
        
        String memOpts = manager.getInstanceProperties().getProperty(
                WLPluginProperties.MEM_OPTS);
        if (memOpts != null) {
            memoryOptions.setText(memOpts.trim());
        }
        
        vmOptions.getDocument().addDocumentListener( 
                new PropertyDocumentListener(manager, WLPluginProperties.JAVA_OPTS, 
                        vmOptions));
        
        memoryOptions.getDocument().addDocumentListener( 
                new PropertyDocumentListener(manager, WLPluginProperties.MEM_OPTS, 
                        memoryOptions));

        proxyCheckBox.setEnabled(!manager.isRemote());
        proxyCheckBox.setSelected(!manager.isRemote()
                && Boolean.valueOf(manager.getInstanceProperties().getProperty(WLPluginProperties.PROXY_ENABLED)));
        proxyCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    manager.getInstanceProperties().setProperty(WLPluginProperties.PROXY_ENABLED, Boolean.TRUE.toString());
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    manager.getInstanceProperties().setProperty(WLPluginProperties.PROXY_ENABLED, Boolean.FALSE.toString());
                }
            }
        });

        debugModeCheckBox.setEnabled(manager.isRemote());
        debugModeCheckBox.setSelected(!manager.isRemote()
                || Boolean.valueOf(manager.getInstanceProperties().getProperty(WLPluginProperties.REMOTE_DEBUG_ENABLED)));
        debugModeCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    portSpinner.setEnabled(true);
                    manager.getInstanceProperties().setProperty(WLPluginProperties.REMOTE_DEBUG_ENABLED, Boolean.TRUE.toString());
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    portSpinner.setEnabled(false);
                    manager.getInstanceProperties().setProperty(WLPluginProperties.REMOTE_DEBUG_ENABLED, Boolean.FALSE.toString());
                }
            }
        });

        portSpinner.setEnabled(!manager.isRemote() || debugModeCheckBox.isSelected());
        final SpinnerNumberModel debugPortModel = new SpinnerNumberModel(
                Integer.parseInt(manager.getInstanceProperties().getProperty(WLPluginProperties.DEBUGGER_PORT_ATTR)), 0, 65535, 1);
        debugPortModel.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                manager.getInstanceProperties().setProperty(WLPluginProperties.DEBUGGER_PORT_ATTR,
                        ((Integer) debugPortModel.getValue()).toString());
            }
        });
        portSpinner.setModel(debugPortModel);
        portSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(portSpinner, "#"));

        JTextField portSpinnerTextField = ((JSpinner.NumberEditor) portSpinner.getEditor()).getTextField();
        // work-around for jspinner incorrect fonts
        Font font = portSpinnerTextField.getFont();
        portSpinner.setFont(font);

        vendorName.setEnabled(!manager.isRemote());
        vmOptions.setEnabled(!manager.isRemote());
        memoryOptions.setEnabled(!manager.isRemote());
        noteChangesLabel.setVisible(!manager.isRemote());

        if (manager.isRemote()) {
            addAncestorListener(new AncestorListener() {

                @Override
                public void ancestorRemoved(AncestorEvent event) {
                    manager.getInstanceProperties().refreshServerInstance();
                }

                @Override
                public void ancestorAdded(AncestorEvent event) {
                }

                @Override
                public void ancestorMoved(AncestorEvent event) {
                }
            });
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javaHomeLabel = new javax.swing.JLabel();
        javaHome = new javax.swing.JTextField();
        vmOptionsLabel = new javax.swing.JLabel();
        noteChangesLabel = new javax.swing.JLabel();
        vmOptions = new javax.swing.JTextField();
        vmOptionsSampleLabel = new javax.swing.JLabel();
        vendorLabel = new javax.swing.JLabel();
        vendorName = new javax.swing.JComboBox();
        memoryOptions = new javax.swing.JTextField();
        memoryOptionsLabel = new javax.swing.JLabel();
        memoryOptionsCommentLabel = new javax.swing.JLabel();
        portLabel = new javax.swing.JLabel();
        portSpinner = new javax.swing.JSpinner();
        debugModeCheckBox = new javax.swing.JCheckBox();
        proxyCheckBox = new javax.swing.JCheckBox();

        javaHomeLabel.setLabelFor(javaHome);
        org.openide.awt.Mnemonics.setLocalizedText(javaHomeLabel, org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "LBL_JavaHome")); // NOI18N

        javaHome.setEditable(false);

        vmOptionsLabel.setLabelFor(vmOptions);
        org.openide.awt.Mnemonics.setLocalizedText(vmOptionsLabel, org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "LBL_VmOptions")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(noteChangesLabel, org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "CustomizerJVM.noteChangesLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(vmOptionsSampleLabel, org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "LBL_VmOptionsSample")); // NOI18N

        vendorLabel.setLabelFor(vendorName);
        org.openide.awt.Mnemonics.setLocalizedText(vendorLabel, org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "LBL_JvmVendor")); // NOI18N

        memoryOptionsLabel.setLabelFor(memoryOptions);
        org.openide.awt.Mnemonics.setLocalizedText(memoryOptionsLabel, org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "LBL_VmMemoryOptions")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(memoryOptionsCommentLabel, org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "LBL_VmMemoryOptionsComment")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(portLabel, org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "CustomizerJVM.portLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(debugModeCheckBox, org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "CustomizerJVM.debugModeCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(proxyCheckBox, org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "CustomizerJVM.proxyCheckBox.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(noteChangesLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(vendorLabel)
                            .addComponent(javaHomeLabel)
                            .addComponent(vmOptionsLabel)
                            .addComponent(memoryOptionsLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(javaHome)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(memoryOptionsCommentLabel)
                                    .addComponent(vmOptionsSampleLabel)
                                    .addComponent(vendorName, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(vmOptions)
                            .addComponent(memoryOptions))
                        .addGap(12, 12, 12))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(portLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(portSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(debugModeCheckBox)
                            .addComponent(proxyCheckBox))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(vendorLabel)
                    .addComponent(vendorName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(javaHomeLabel)
                    .addComponent(javaHome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(vmOptionsLabel)
                    .addComponent(vmOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(vmOptionsSampleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(memoryOptionsLabel)
                    .addComponent(memoryOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(memoryOptionsCommentLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(proxyCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(debugModeCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(portLabel)
                    .addComponent(portSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(noteChangesLabel)
                .addContainerGap())
        );

        javaHomeLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSN_JavaHome")); // NOI18N
        javaHome.getAccessibleContext().setAccessibleName(javaHomeLabel.getAccessibleContext().getAccessibleName());
        javaHome.getAccessibleContext().setAccessibleDescription(javaHomeLabel.getAccessibleContext().getAccessibleDescription());
        vmOptionsLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSN_VmOptions")); // NOI18N
        vmOptionsLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSN_VmOptions")); // NOI18N
        noteChangesLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSN_Note")); // NOI18N
        noteChangesLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSD_Note")); // NOI18N
        vmOptions.getAccessibleContext().setAccessibleName(vmOptionsLabel.getAccessibleContext().getAccessibleName());
        vmOptions.getAccessibleContext().setAccessibleDescription(vmOptionsLabel.getAccessibleContext().getAccessibleDescription());
        vmOptionsSampleLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSN_VmOptionsSample")); // NOI18N
        vmOptionsSampleLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSD_VmOptionsSample")); // NOI18N
        vendorLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSN_Vendor")); // NOI18N
        vendorLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSD_Vendor")); // NOI18N
        vendorName.getAccessibleContext().setAccessibleName(vendorLabel.getAccessibleContext().getAccessibleName());
        vendorName.getAccessibleContext().setAccessibleDescription(vendorLabel.getAccessibleContext().getAccessibleDescription());
        memoryOptions.getAccessibleContext().setAccessibleName(memoryOptionsLabel.getAccessibleContext().getAccessibleName());
        memoryOptions.getAccessibleContext().setAccessibleDescription(memoryOptionsLabel.getAccessibleContext().getAccessibleDescription());
        memoryOptionsLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSN_VmMemoryOptions")); // NOI18N
        memoryOptionsLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerJVM.class, "ACSD_VmMemoryOptions")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox debugModeCheckBox;
    private javax.swing.JTextField javaHome;
    private javax.swing.JLabel javaHomeLabel;
    private javax.swing.JTextField memoryOptions;
    private javax.swing.JLabel memoryOptionsCommentLabel;
    private javax.swing.JLabel memoryOptionsLabel;
    private javax.swing.JLabel noteChangesLabel;
    private javax.swing.JLabel portLabel;
    private javax.swing.JSpinner portSpinner;
    private javax.swing.JCheckBox proxyCheckBox;
    private javax.swing.JLabel vendorLabel;
    private javax.swing.JComboBox vendorName;
    private javax.swing.JTextField vmOptions;
    private javax.swing.JLabel vmOptionsLabel;
    private javax.swing.JLabel vmOptionsSampleLabel;
    // End of variables declaration//GEN-END:variables

}
