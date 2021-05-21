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

import java.io.File;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.api.customizer.support.CheckBoxUpdater;
import org.netbeans.modules.maven.api.customizer.support.TextComponentUpdater;
import static org.netbeans.modules.maven.apisupport.Bundle.*;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

public class InstallerPanel extends JPanel implements HelpCtx.Provider {

    private static final String PROP_LICENSE = "installerLicenseFile";
    private static final String PROP_LINUX = "installerOsLinux";
    private static final String PROP_MAC = "installerOsMacosx";
    private static final String PROP_PACK200 = "installerPack200Enable";
    private static final String PROP_SOLARIS = "installerOsSolaris";
    private static final String PROP_WINDOWS = "installerOsWindows";

    private static final String GOAL = "build-installers";

    private final ProjectCustomizer.Category category;
    private final Project project;
    private final ModelHandle2 handle;

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    private InstallerPanel(ProjectCustomizer.Category category, Project project, ModelHandle2 handle) {
        this.category = category;
        this.project = project;
        this.handle = handle;
        initComponents();
        new BooleanPropUpdater(PROP_WINDOWS, true, windowsCheckBox);
        new BooleanPropUpdater(PROP_SOLARIS, true, solarisCheckBox);
        new BooleanPropUpdater(PROP_LINUX, true, linuxCheckBox);
        new BooleanPropUpdater(PROP_MAC, true, macCheckBox);
        new StringPropUpdater(PROP_LICENSE, licenseField, licenseLabel);
        new BooleanPropUpdater(PROP_PACK200, true, pack200CheckBox);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        windowsCheckBox = new javax.swing.JCheckBox();
        linuxCheckBox = new javax.swing.JCheckBox();
        macCheckBox = new javax.swing.JCheckBox();
        solarisCheckBox = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        licenseLabel = new javax.swing.JLabel();
        licenseField = new javax.swing.JTextField();
        licenseButton = new javax.swing.JButton();
        pack200CheckBox = new javax.swing.JCheckBox();
        jSeparator2 = new javax.swing.JSeparator();
        pack200Info = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(InstallerPanel.class, "InstallerPanel.Platforms.Label")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(windowsCheckBox, org.openide.util.NbBundle.getMessage(InstallerPanel.class, "InstallerPanel.OSLabelWindows")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(linuxCheckBox, org.openide.util.NbBundle.getMessage(InstallerPanel.class, "InstallerPanel.OSLabelLinux")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(macCheckBox, org.openide.util.NbBundle.getMessage(InstallerPanel.class, "InstallerPanel.OSLabelMacOS")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(solarisCheckBox, org.openide.util.NbBundle.getMessage(InstallerPanel.class, "InstallerPanel.OSLabelSolaris")); // NOI18N

        licenseLabel.setLabelFor(licenseField);
        org.openide.awt.Mnemonics.setLocalizedText(licenseLabel, org.openide.util.NbBundle.getMessage(InstallerPanel.class, "InstallerPanel.licenseLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(licenseButton, org.openide.util.NbBundle.getMessage(InstallerPanel.class, "InstallerPanel.licenseButton.text")); // NOI18N
        licenseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                licenseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(pack200CheckBox, org.openide.util.NbBundle.getMessage(InstallerPanel.class, "InstallerPanel.pack200checkBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(pack200Info, org.openide.util.NbBundle.getMessage(InstallerPanel.class, "InstallerPanel.Pack200.Description.Text")); // NOI18N
        pack200Info.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        pack200Info.setFocusable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(windowsCheckBox)
                            .addComponent(linuxCheckBox)
                            .addComponent(macCheckBox)
                            .addComponent(solarisCheckBox))
                        .addGap(53, 509, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(licenseLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(licenseField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(licenseButton)
                        .addContainerGap())))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pack200CheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, 772, Short.MAX_VALUE)
                        .addContainerGap())
                    .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(pack200Info, javax.swing.GroupLayout.DEFAULT_SIZE, 749, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(windowsCheckBox)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(linuxCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(macCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(solarisCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(licenseLabel)
                    .addComponent(licenseField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(licenseButton))
                .addGap(6, 6, 6)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pack200CheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pack200Info, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
                .addGap(99, 99, 99))
        );

        jLabel2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(InstallerPanel.class, "InstallerPanel.Platforms.Label")); // NOI18N
        windowsCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InstallerPanel.class, "InstallerPanel.OSLabelWindows.AccessibleContext.accessible")); // NOI18N
        linuxCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InstallerPanel.class, "InstallerPanel.OSLabelLinux.AccessibleContext.accessible")); // NOI18N
        macCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InstallerPanel.class, "InstallerPanel.OSLabelMacOS.AccessibleContext.accessible")); // NOI18N
        solarisCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InstallerPanel.class, "InstallerPanel.OSLabelSolaris.AccessibleContext.accessible")); // NOI18N
        pack200CheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InstallerPanel.class, "InstallerPanel.jCheckBox5.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    @Messages("InstallerPanel_not_collocated=License file reference might not be portable; maybe copy into project.")
    private void licenseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_licenseButtonActionPerformed
        File dir = FileUtil.toFile(project.getProjectDirectory());
        if (dir == null) {
            return;
        }
        JFileChooser jfc = new JFileChooser(dir);
        if (jfc.showOpenDialog(licenseButton) == JFileChooser.APPROVE_OPTION) {
            File license = jfc.getSelectedFile();
            licenseField.setText(FileUtilities.relativizeFile(dir, license));
            if (CollocationQuery.areCollocated(Utilities.toURI(license), Utilities.toURI(dir))) {
                category.setErrorMessage(null);
            } else {
                category.setErrorMessage(InstallerPanel_not_collocated());
            }
        }
    }//GEN-LAST:event_licenseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JButton licenseButton;
    private javax.swing.JTextField licenseField;
    private javax.swing.JLabel licenseLabel;
    private javax.swing.JCheckBox linuxCheckBox;
    private javax.swing.JCheckBox macCheckBox;
    private javax.swing.JCheckBox pack200CheckBox;
    private javax.swing.JLabel pack200Info;
    private javax.swing.JCheckBox solarisCheckBox;
    private javax.swing.JCheckBox windowsCheckBox;
    // End of variables declaration//GEN-END:variables

    private static Configuration config(POMModel pomModel) {
        Build build = pomModel.getProject().getBuild();
        if (build == null) {
            build = pomModel.getFactory().createBuild();
            pomModel.getProject().setBuild(build);
        }
        Plugin nbmPlugin = PluginBackwardPropertyUtils.findPluginFromBuild(build);
        if (nbmPlugin == null) {
            nbmPlugin = pomModel.getFactory().createPlugin();
            nbmPlugin.setGroupId(MavenNbModuleImpl.GROUPID_APACHE);
            nbmPlugin.setArtifactId(MavenNbModuleImpl.NBM_PLUGIN);
            nbmPlugin.setExtensions(Boolean.TRUE);
            build.addPlugin(nbmPlugin);
        }
        Configuration config = nbmPlugin.getConfiguration();
        if (config == null) {
            config = pomModel.getFactory().createConfiguration();
            nbmPlugin.setConfiguration(config);
        }
        return config;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("maven_settings");
    }    

    private class BooleanPropUpdater extends CheckBoxUpdater implements ModelOperation<POMModel> {

        private final String property;
        private final boolean dflt;
        private Boolean modifiedValue;
        private final String pomValue;

        BooleanPropUpdater(String property, boolean dflt, JCheckBox comp) {
            super(comp);
            this.property = property;
            this.dflt = dflt;
            pomValue = PluginBackwardPropertyUtils.getPluginProperty(project, property, GOAL, null);
        }

        @org.netbeans.api.annotations.common.SuppressWarnings("NP_BOOLEAN_RETURN_NULL")
        @Override public Boolean getValue() {
            if (modifiedValue != null) {
                return modifiedValue;
                }
            
            return pomValue != null ? Boolean.valueOf(pomValue) : null;
            }

        @Override public boolean getDefaultValue() {
            return dflt;
        }

        @Override public void setValue(Boolean value) {
            if (Utilities.compareObjects(value, getValue())) {
                return;
            }

            modifiedValue = value;
            handle.removePOMModification(this);
            if (pomValue != null && pomValue.equals(modifiedValue)) {
                //ignore now, we already have what we want in the project.
            } else {
                handle.addPOMModification(this);
            }
        }

        @Override
        public void performOperation(POMModel model) {
            Configuration config = config(model);
            if (modifiedValue != null) {
                config.setSimpleParameter(property, modifiedValue != null ? Boolean.toString(modifiedValue) : Boolean.toString(getDefaultValue()));
            } else {
                //TODO for this case config(model) method which creates the configuration element is wrong..
                POMExtensibilityElement e = ModelUtils.getOrCreateChild(config, property, config.getModel());
                config.removeExtensibilityElement(e);
            }
        }

    }

    private class StringPropUpdater extends TextComponentUpdater implements ModelOperation<POMModel>{

        private final String property;
        private String modifiedValue;
        private String pomValue;


        StringPropUpdater(String property, JTextComponent comp, JLabel label) {
            super(comp, label);
            this.property = property;
            pomValue = PluginBackwardPropertyUtils.getPluginProperty(project, property, GOAL, null);
        }

        @Override public String getValue() {
            if (modifiedValue != null) {
                return modifiedValue;
                }
            
            return pomValue != null ? pomValue : "";
            }

        @Override public String getDefaultValue() {
            return "";
        }

        @Override public void setValue(String value) {
            if (Utilities.compareObjects(value, getValue())) {
                return;
            }
            if (value == null) {
                value = getDefaultValue();
            }
            modifiedValue = value;
            handle.removePOMModification(this);
            if (pomValue != null && pomValue.equals(modifiedValue)) {
                //we already have what we want in the pom.. skip
            } else {
                handle.addPOMModification(this);
            }
        }

        @Override
        public void performOperation(POMModel model) {
            Configuration config = config(model);
            POMExtensibilityElement e = ModelUtils.getOrCreateChild(config, property, config.getModel());
            if (modifiedValue == null || modifiedValue.isEmpty()) {
                //TODO for this case config(model) method which creates the configuration element is wrong..
                config.removeExtensibilityElement(e);
            } else {
                e.setElementText(modifiedValue);
            }
        }

    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-maven", category="Build", position=1000)
    public static class Provider implements ProjectCustomizer.CompositeCategoryProvider {

        @Messages("LBL_InstallerPanel=Installer")
        @Override public ProjectCustomizer.Category createCategory(Lookup context) {
            Project project = context.lookup(Project.class);
            NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
            if (watcher != null && NbMavenProject.TYPE_NBM_APPLICATION.equalsIgnoreCase(watcher.getPackagingType())) {
                String version = PluginBackwardPropertyUtils.getPluginVersion(watcher.getMavenProject());
                if (version != null && new ComparableVersion(version).compareTo(new ComparableVersion("3.7-SNAPSHOT")) >= 0) {
                    return ProjectCustomizer.Category.create("Installer", LBL_InstallerPanel(), null);
                }
            }
            return null;
        }

        @Override public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
            return new InstallerPanel(category, context.lookup(Project.class), context.lookup(ModelHandle2.class));
        }

    }

}
