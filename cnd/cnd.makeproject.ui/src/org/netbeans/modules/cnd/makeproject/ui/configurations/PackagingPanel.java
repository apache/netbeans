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
package org.netbeans.modules.cnd.makeproject.ui.configurations;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.PackagingConfiguration;
import org.netbeans.modules.cnd.makeproject.api.PackagerDescriptor;
import org.netbeans.modules.cnd.makeproject.api.PackagerInfoElement;
import org.netbeans.modules.cnd.makeproject.api.PackagerManager;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 */
public class PackagingPanel extends javax.swing.JPanel implements HelpCtx.Provider, PropertyChangeListener {

    private PackagingConfiguration packagingConfiguration;
    private final PropertyEditorSupport editor;
    private final MakeConfiguration conf;
    private PackagingInfoOuterPanel packagingInfoOuterPanel = null;
    private PackagingInfoPanel packagingInfoPanel = null;
    private PackagingAdditionalInfoPanel packagingAdditionalInfoPanel = null;
    private PackagingFilesOuterPanel packagingFilesOuterPanel = null;
    private PackagingFilesPanel packagingFilesPanel = null;

    /** Creates new form PackagingPanel */
    public PackagingPanel(PackagingConfiguration packagingConfiguration, PropertyEditorSupport editor, PropertyEnv env, MakeConfiguration conf) {
        initComponents();

        this.packagingConfiguration = packagingConfiguration;
        this.editor = editor;
        this.conf = conf;

        env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        env.addPropertyChangeListener(this);

        // Add tabs
        String type = packagingConfiguration.getType().getValue();
        PackagerDescriptor packager = PackagerManager.getDefault().getPackager(packagingConfiguration.getType().getValue());
        if (packager.hasInfoList()) {
            packagingInfoOuterPanel = new PackagingInfoOuterPanel(
                    packagingInfoPanel = new PackagingInfoPanel(packagingConfiguration.getHeaderSubList(type), packagingConfiguration),
                    packagingAdditionalInfoPanel = new PackagingAdditionalInfoPanel(packagingConfiguration.getAdditionalInfo().getValue(), packagingConfiguration));
            packagingFilesPanel = new PackagingFilesPanel(packagingConfiguration.getFiles().getValue(), conf.getBaseFSPath());
        } else {
            packagingFilesPanel = new PackagingFiles4Panel(packagingConfiguration.getFiles().getValue(), conf.getBaseFSPath());
        }
        packagingFilesOuterPanel = new PackagingFilesOuterPanel(packagingFilesPanel, packagingConfiguration);

        tabbedPane.addTab(getString("InfoPanelText"), packagingInfoOuterPanel);
        tabbedPane.addTab(getString("FilePanelText"), packagingFilesOuterPanel);

        if (packager.hasInfoList()) {
            // Add tabs
            tabbedPane.setEnabledAt(0, true);
            tabbedPane.setEnabledAt(1, true);
            tabbedPane.setSelectedIndex(0);
        } else {
            // Add tabs
            tabbedPane.setEnabledAt(0, false);
            tabbedPane.setEnabledAt(1, true);
            tabbedPane.setSelectedIndex(1);
        }

        //  See IZ 142846
        setPreferredSize(new Dimension(600, 500));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID) {
            editor.setValue(getPropertyValue());
        }
    }

    private Object getPropertyValue() throws IllegalStateException {
        PackagerDescriptor packager = PackagerManager.getDefault().getPackager(packagingConfiguration.getType().getValue());
        if (packager.hasInfoList()) {
            List<PackagerInfoElement> oldList = packagingConfiguration.getInfo().getValue();
            List<PackagerInfoElement> newList = new ArrayList<>();
            // Copy all other types over
            for (PackagerInfoElement elem : oldList) {
                if (!elem.getPackager().equals(packagingConfiguration.getType().getValue())) {
                    newList.add(elem);
                }
            }
            // Copy edited list
            List<PackagerInfoElement> editedList = packagingInfoPanel.getListData();
            editedList.forEach((elem) -> {
                newList.add(elem);
            });
            packagingConfiguration.getInfo().setValue(newList);
            // Additional info
            packagingConfiguration.getAdditionalInfo().setValue(packagingAdditionalInfoPanel.getListData());
        }

        packagingConfiguration.getFiles().setValue(new ArrayList<>(packagingFilesPanel.getListData()));
        return packagingConfiguration;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("PackagingFiles"); // NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        tabbedPane = new javax.swing.JTabbedPane();

        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(tabbedPane, gridBagConstraints);
        tabbedPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PackagingPanel.class, "PackagingPanel.tabbedPane.AccessibleContext.accessibleName")); // NOI18N
        tabbedPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PackagingPanel.class, "PackagingPanel.tabbedPane.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables

    private static String getString(String s) {
        return NbBundle.getBundle(PackagingPanel.class).getString(s);
    }
}
