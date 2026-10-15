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

package org.netbeans.modules.maven.apisupport;

import java.awt.EventQueue;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import org.apache.maven.repository.RepositorySystem;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.modules.maven.api.MavenValidators;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries.Result;
import org.netbeans.modules.maven.options.MavenVersionSettings;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Severity;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.ValidatorUtils;
import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.swing.SwingValidationGroup;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

import static org.netbeans.modules.maven.apisupport.Bundle.ADD_Module_Name;
import static org.netbeans.modules.maven.apisupport.Bundle.NbmWizardPanelVisual_wait;

/**
 *
 * @author mkleint
 */
public class NbmWizardPanelVisual extends javax.swing.JPanel {

    private static final RequestProcessor RP = new RequestProcessor(NbmWizardPanelVisual.class);

    @Messages("NbmWizardPanelVisual.wait=Searching...")
    private static final String SEARCHING = NbmWizardPanelVisual_wait();
    private static final String RELEASE_PREFIX = "RELEASE";
    private static final int MINIMUM_VERSION = 111;
    private static final Set<String> IGNORE_RELEASES = new HashSet<>(Arrays.asList("RELEASE120"));

    private final NbmWizardPanel panel;
    private final ValidationGroup vg = ValidationGroup.create();
    private final ValidationGroup vgEnabled = ValidationGroup.create();

    boolean isApp = false;
    private boolean isLoaded = false;
    private AggregateProgressHandle handle;
    private final Object HANDLE_LOCK = new Object();

    @SuppressWarnings("unchecked") // SIMPLEVALIDATION-48
    @Messages({"ADD_Module_Name=NetBeans Module ArtifactId", 
             "Handle_Download=Downloading Archetype"})
    public NbmWizardPanelVisual(NbmWizardPanel panel) {
        this.panel = panel;
        initComponents();
        final Archetype arch = panel.getArchetype();
        isApp = NbmWizardIterator.NB_APP_ARCH.equals(arch);
        if(!isApp) {
            cbAddModule.setVisible(false);
            txtAddModule.setVisible(false);            
        }        
        SwingUtilities.invokeLater(this::initValidators);
        
        RP.post(() -> {

            EventQueue.invokeLater(() -> versionCombo.setModel(new DefaultComboBoxModel(new Object[] {SEARCHING})));

            List<RepositoryInfo> info = MavenNbModuleImpl.netbeansRepo();               
            final Object key = this;               
            if (info == null || info.contains(null)) {
                try {
                    //transient remove central, make central transient too
                    RepositoryPreferences.getInstance().addTransientRepository(
                            key,
                            RepositorySystem.DEFAULT_REMOTE_REPO_ID,
                            RepositorySystem.DEFAULT_REMOTE_REPO_ID,
                            RepositorySystem.DEFAULT_REMOTE_REPO_URL,
                            RepositoryInfo.MirrorStrategy.NON_WILDCARD
                    );
                    info = MavenNbModuleImpl.netbeansRepo();
                } catch (URISyntaxException x) {
                    Exceptions.printStackTrace(x);
                }
            }

            if (info != null) {
                final Result<NBVersionInfo> result = RepositoryQueries.getVersionsResult("org.netbeans.cluster", "platform", Collections.unmodifiableList(info));
                List<String> versions = filterVersions(result);

                if (result.isPartial()) {
                    versions.add(SEARCHING);
                    //we return the values we have and schedule retrieval of the rest.
                    RP.post(() -> {
                        result.waitForSkipped();
                        RepositoryPreferences.getInstance().removeTransientRepositories(key);
                        List<String> allVersions = filterVersions(result);
                        EventQueue.invokeLater(() -> {
                            versionCombo.setModel(new DefaultComboBoxModel(allVersions.toArray()));
                            versionComboActionPerformed(null);
                        });
                    });
                } else {
                    RepositoryPreferences.getInstance().removeTransientRepositories(key);
                }

                isLoaded = true;
                EventQueue.invokeLater(() -> {
                    versionCombo.setModel(new DefaultComboBoxModel(versions.toArray()));
                    versionComboActionPerformed(null);
                });
            }
        });
        
    }

    private static List<String> filterVersions(Result<NBVersionInfo> result) {
        List<String> versions = result.getResults().stream()
                .map(NBVersionInfo::getVersion)
                .filter((v) -> v.startsWith(RELEASE_PREFIX) && v.length() > RELEASE_PREFIX.length())
                .filter((v) -> versionOf(v) >= MINIMUM_VERSION)
                .filter((v) -> !IGNORE_RELEASES.contains(v))
                .sorted((v1, v2) -> v2.compareTo(v1))
                .collect(Collectors.toCollection(ArrayList::new)); // must be mutable
        if (versions.isEmpty()) {
            versions.add(MavenVersionSettings.getDefault().getNBVersion()); // add a fallback version
        }
        versions.add(NbmWizardIterator.SNAPSHOT_VERSION);
        return versions;
    }

    private static int versionOf(String release) {
        int end;
        for (end = RELEASE_PREFIX.length(); end < release.length(); end++) {
            if (!Character.isDigit(release.charAt(end)))
                break;
        }
        return Integer.parseInt(release.substring(RELEASE_PREFIX.length(), end));
    }

    private void initValidators() {
        if (isApp) {
            vg.add(txtAddModule, ValidatorUtils.merge(
                MavenValidators.createArtifactIdValidators(),
                StringValidators.REQUIRE_VALID_FILENAME
            ));
            SwingValidationGroup.setComponentName(txtAddModule, ADD_Module_Name());
        }
        vgEnabled.add(versionCombo, new Validator<String>() {
            
            @Override
            public void validate(Problems prblms, String name, String value) {
                if (SEARCHING.equals(value) || !isLoaded) {
                    prblms.add("Still searching", Severity.FATAL);
                }
            }
            
            @Override
            public Class modelType() {
                return String.class;
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        versionLabel = new javax.swing.JLabel();
        versionCombo = new javax.swing.JComboBox();
        cbOsgiDeps = new javax.swing.JCheckBox();
        cbAddModule = new javax.swing.JCheckBox();
        txtAddModule = new javax.swing.JTextField();

        versionLabel.setLabelFor(versionCombo);
        org.openide.awt.Mnemonics.setLocalizedText(versionLabel, NbBundle.getMessage(NbmWizardPanelVisual.class, "NbmWizardPanelVisual.versionLabel.text")); // NOI18N

        versionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                versionComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbOsgiDeps, org.openide.util.NbBundle.getMessage(NbmWizardPanelVisual.class, "NbmWizardPanelVisual.cbOsgiDeps.text")); // NOI18N

        cbAddModule.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(cbAddModule, org.openide.util.NbBundle.getMessage(NbmWizardPanelVisual.class, "NbmWizardPanelVisual.cbAddModule.text")); // NOI18N
        cbAddModule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbAddModuleActionPerformed(evt);
            }
        });

        txtAddModule.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(versionLabel)
                        .addGap(8, 8, 8)
                        .addComponent(versionCombo, 0, 396, Short.MAX_VALUE))
                    .addComponent(cbOsgiDeps)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cbAddModule)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtAddModule, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(versionLabel)
                    .addComponent(versionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbOsgiDeps)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbAddModule)
                    .addComponent(txtAddModule, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(176, Short.MAX_VALUE))
        );

        versionLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbmWizardPanelVisual.class, "NbmWizardPanelVisual.versionLabel.accessibledesc")); // NOI18N
        versionCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NbmWizardPanelVisual.class, "NbmWizardPanelVisual.cbVersion.accessiblename")); // NOI18N
        versionCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbmWizardPanelVisual.class, "NbmWizardPanelVisual.cbVersion.accessibledesc")); // NOI18N
        cbOsgiDeps.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbmWizardPanelVisual.class, "NbmWizardPanelVisual.cbOsgiDeps.accessibledesc")); // NOI18N
        cbAddModule.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbmWizardPanelVisual.class, "NbmWizardPanelVisual.cbAddModule.accessibledesc")); // NOI18N
        txtAddModule.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NbmWizardPanelVisual.class, "NbmWizardPanelVisual.txtAddModule.accessiblename")); // NOI18N
        txtAddModule.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbmWizardPanelVisual.class, "NbmWizardPanelVisual.txtAddModule.accessibledesc")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NbmWizardPanelVisual.class, "NbmWizardPanelVisual.panel.accessiblename")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbmWizardPanelVisual.class, "NbmWizardPanelVisual.panel.accessibledesc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void cbAddModuleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbAddModuleActionPerformed
        // TODO add your handling code here:
        txtAddModule.setEnabled(cbAddModule.isSelected());
        vg.performValidation();
}//GEN-LAST:event_cbAddModuleActionPerformed

    private void versionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_versionComboActionPerformed
        vgEnabled.performValidation();
    }//GEN-LAST:event_versionComboActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbAddModule;
    private javax.swing.JCheckBox cbOsgiDeps;
    private javax.swing.JTextField txtAddModule;
    private javax.swing.JComboBox versionCombo;
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables


     void store(WizardDescriptor d) {
        synchronized (HANDLE_LOCK) {
            if (handle != null) {
                handle.finish();
                handle = null;
            }
        }
        d.putProperty(NbmWizardIterator.OSGIDEPENDENCIES, cbOsgiDeps.isSelected());
         if (isApp) {
             if (cbAddModule.isSelected()) {
                 d.putProperty(NbmWizardIterator.NBM_ARTIFACTID, txtAddModule.getText().trim());
             } else {
                 d.putProperty(NbmWizardIterator.NBM_ARTIFACTID, null);
             }
         }
         String version = (String) versionCombo.getSelectedItem();
         if (version != null && !version.equals(SEARCHING)) {
             d.putProperty(NbmWizardIterator.NB_VERSION, version);
         }
        SwingUtilities.invokeLater(() -> {
            panel.getValidationGroup().remove(vg);
            panel.getEnabledStateValidationGroup().remove(vgEnabled);
        });
    }

    void read(WizardDescriptor d) {
        synchronized (HANDLE_LOCK) {
            if (handle != null) {
                handle.finish();
                handle = null;
            }
        }        
        
        Boolean b = (Boolean) d.getProperty(NbmWizardIterator.OSGIDEPENDENCIES);
        if (b != null) {
            cbOsgiDeps.setSelected(b);
        }
        if (isApp) {
            String artifId = (String) d.getProperty("artifactId");
            String val = (String) d.getProperty(NbmWizardIterator.NBM_ARTIFACTID);
            cbAddModule.setSelected(val != null);
            if (val == null) {
                val = artifId + "-sample";
            }
            txtAddModule.setText(val);

        }
        String version = (String) d.getProperty(NbmWizardIterator.NB_VERSION);
        if (version != null) {
            versionCombo.setSelectedItem(version);
        }
        SwingUtilities.invokeLater(() -> {
            panel.getValidationGroup().addItem(vg, true);
            panel.getEnabledStateValidationGroup().addItem(vgEnabled, true);
            vgEnabled.performValidation();
        });
    }
}
