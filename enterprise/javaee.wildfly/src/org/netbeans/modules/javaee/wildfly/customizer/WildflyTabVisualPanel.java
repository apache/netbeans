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
package org.netbeans.modules.javaee.wildfly.customizer;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.javaee.wildfly.WildflyDeploymentManager;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginProperties;
import org.netbeans.modules.javaee.wildfly.util.WildFlyProperties;
import org.openide.util.NbBundle;

public final class WildflyTabVisualPanel extends JPanel {
    /** The wizard panel descriptor associated with this GUI panel.
     * If you need to fire state changes or something similar, you can
     * use this handle to do so.
     */

    private final transient WildFlyProperties targetData;
    private final transient WildflyDeploymentManager dm;


    /**
     * Creates new form WildflyTabVisualPanel
     */
    public WildflyTabVisualPanel(DeploymentManager dm) {
        this.dm = (WildflyDeploymentManager)dm;
        targetData = new WildFlyProperties(this.dm);
        initComponents();
        configFile.setText(targetData.getServerProfile());


        configFile.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                locationChanged();
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                locationChanged();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                locationChanged();
            }
        });
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(WildflyTabVisualPanel.class,
                "StepName_EnterDomainDirectory");                                // NOI18N
    }



    String getParentDirectory() {
        return configFile.getText();
    }

    // Event handling
    //
    private final Set<ChangeListener> listeners = ConcurrentHashMap.newKeySet(2);
    public final void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    public final void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }

    void locationChanged() {
        fireChangeEvent();
    }


    private String browseDomainLocation(){
        String insLocation = null;
        JFileChooser chooser = new JFileChooser();

        decorateChooser(chooser,configFile.getText(),
                NbBundle.getMessage(WildflyTabVisualPanel.class,
                "LBL_Choose_Domain"));                                          //NOI18N
        int returnValue = chooser.showDialog(this,
                NbBundle.getMessage(WildflyTabVisualPanel.class,
                "LBL_Choose_Button"));                                          //NOI18N

        if(returnValue == JFileChooser.APPROVE_OPTION){
            insLocation = chooser.getSelectedFile().getAbsolutePath();
        }
        return insLocation;
    }

    void decorateChooser(JFileChooser chooser,String fname,String title) {
        chooser.setDialogTitle(title);                                           //NOI18N
        chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);

        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File file) {
                return file.isDirectory() || (file.isFile() && file.getName().endsWith(".xml"));
            }

            @Override
            public String getDescription() {
                return "";
            }
        });
        chooser.setApproveButtonMnemonic(NbBundle.getMessage(WildflyTabVisualPanel.class,
                "Choose_Button_Mnemonic").charAt(0));                           //NOI18N
        chooser.setMultiSelectionEnabled(false);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setApproveButtonToolTipText(NbBundle.getMessage(WildflyTabVisualPanel.class,
                "LBL_Chooser_Name"));                                           //NOI18N

        chooser.getAccessibleContext().
                setAccessibleName(NbBundle.getMessage(WildflyTabVisualPanel.class,
                "LBL_Chooser_Name"));                                           //NOI18N
        chooser.getAccessibleContext().
                setAccessibleDescription(NbBundle.getMessage(WildflyTabVisualPanel.class,
                "LBL_Chooser_Name"));                                           //NOI18N
        if (null != fname && fname.length() > 0) {
            File sel = new File(fname);
            if (sel.isDirectory())
                chooser.setCurrentDirectory(sel);
            else
                chooser.setSelectedFile(sel);
        }
    }



    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        description = new javax.swing.JLabel();
        parentDirectoryLabel = new javax.swing.JLabel();
        configFile = new javax.swing.JTextField();
        openInstanceDirectorySelector = new javax.swing.JButton();
        spaceHack = new javax.swing.JLabel();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/javaee/wildfly/customizer/Bundle"); // NOI18N
        description.setText(bundle.getString("TXT_instanceDirectoryDescription2")); // NOI18N
        description.setEnabled(false);
        description.setFocusable(false);

        parentDirectoryLabel.setLabelFor(configFile);
        org.openide.awt.Mnemonics.setLocalizedText(parentDirectoryLabel, org.openide.util.NbBundle.getMessage(WildflyTabVisualPanel.class, "LBL_ParentFolder")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(openInstanceDirectorySelector, org.openide.util.NbBundle.getMessage(WildflyTabVisualPanel.class, "LBL_openInstanceDirectorySelector")); // NOI18N
        openInstanceDirectorySelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openInstanceDirectorySelectorActionPerformed(evt);
            }
        });

        spaceHack.setEnabled(false);
        spaceHack.setFocusable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spaceHack)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(parentDirectoryLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(configFile, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(openInstanceDirectorySelector)
                                .addGap(12, 12, 12))
                            .addComponent(description, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(spaceHack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(description)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(parentDirectoryLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(configFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(openInstanceDirectorySelector))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        configFile.getAccessibleContext().setAccessibleDescription(bundle.getString("DSC_instanceDirectory")); // NOI18N
        openInstanceDirectorySelector.getAccessibleContext().setAccessibleDescription(bundle.getString("DSC_openInstanceDirectorySelector")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void openInstanceDirectorySelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openInstanceDirectorySelectorActionPerformed
        String val = browseDomainLocation();
        dm.getInstanceProperties().setProperty(WildflyPluginProperties.PROPERTY_CONFIG_FILE, val);
        if (null != val && val.length() >=1)
            configFile.setText(val);
    }//GEN-LAST:event_openInstanceDirectorySelectorActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField configFile;
    private javax.swing.JLabel description;
    private javax.swing.JButton openInstanceDirectorySelector;
    private javax.swing.JLabel parentDirectoryLabel;
    private javax.swing.JLabel spaceHack;
    // End of variables declaration//GEN-END:variables

}
