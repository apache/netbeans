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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.apisupport.project.ui.ApisupportAntUIUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.NotifyDescriptor;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import static org.netbeans.modules.apisupport.project.ui.customizer.SingleModuleProperties.JAVAC_RELEASE;
import static org.netbeans.modules.apisupport.project.ui.customizer.SingleModuleProperties.JAVAC_SOURCE;

/**
 * Represents <em>Sources</em> panel in Netbeans Module customizer.
 *
 * @author mkrauskopf
 */
final class CustomizerSources extends NbPropertyPanel.Single {

    private boolean srcLevelValueBeingUpdated;
    
    CustomizerSources(final SingleModuleProperties props, ProjectCustomizer.Category category) {
        super(props, CustomizerSources.class, category);
        initComponents();
        initAccessibility();
        refresh();
        srcLevelValue.addActionListener(new ActionListener() { // #66278
            public void actionPerformed(ActionEvent e) {
                if (srcLevelValueBeingUpdated) {
                    return;
                }
                final String oldLevel = getProperty(getJavacLanguageLevelKey());
                final String newLevel = (String) srcLevelValue.getSelectedItem();
                SpecificationVersion jdk5 = new SpecificationVersion("1.5"); // NOI18N
                if (new SpecificationVersion(oldLevel).compareTo(jdk5) < 0 && new SpecificationVersion(newLevel).compareTo(jdk5) >= 0) {
                    EventQueue.invokeLater(new Runnable() { // wait for combo to close, at least
                        public void run() {
                            if (!ApisupportAntUIUtils.showAcceptCancelDialog(
                                    getMessage("CustomizerSources.title.enable_lint"),
                                    getMessage("CustomizerSources.text.enable_lint"),
                                    getMessage("CustomizerSources.button.enable_lint"),
                                    getMessage("CustomizerSources.button.skip_lint"),
                                    NotifyDescriptor.QUESTION_MESSAGE)) {
                                return;
                            }
                            String options = getProperty(SingleModuleProperties.JAVAC_COMPILERARGS);
                            String added = "-Xlint -Xlint:-serial"; // NOI18N
                            if (options == null || options.length() == 0) {
                                options = added;
                            } else {
                                options = options + " " + added; // NOI18N
                            }
                            setProperty(SingleModuleProperties.JAVAC_COMPILERARGS, options);
                        }
                    });
                }
            }
        });
    }
    
    @Override
    protected void refresh() {
        if (getProperties().getSuiteDirectoryPath() == null) {
            moduleSuite.setVisible(false);
            moduleSuiteValue.setVisible(false);
        } else {
            ApisupportAntUIUtils.setText(moduleSuiteValue, getProperties().getSuiteDirectoryPath());
        }
        assert !srcLevelValueBeingUpdated;
        srcLevelValueBeingUpdated = true;
        try {
            srcLevelValue.removeAllItems();
            String[] levels = sourceLevels(getProperties().getActiveJavaPlatform());
            for (String level : levels) {
                srcLevelValue.addItem(level);
            }
            srcLevelValue.setSelectedItem(getProperty(getJavacLanguageLevelKey()));
        } finally {
            srcLevelValueBeingUpdated = false;
        }
        ApisupportAntUIUtils.setText(prjFolderValue, getProperties().getProjectDirectory());
    }
    
    @Override
    public void store() {
        setProperty(getJavacLanguageLevelKey(), (String) srcLevelValue.getSelectedItem());
    }

    private String getJavacLanguageLevelKey() {
        return containsProperty(JAVAC_RELEASE) ? JAVAC_RELEASE : JAVAC_SOURCE;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        prjFolder = new javax.swing.JLabel();
        srcLevel = new javax.swing.JLabel();
        srcLevelValue = new javax.swing.JComboBox();
        filler = new javax.swing.JLabel();
        prjFolderValue = new javax.swing.JTextField();
        moduleSuite = new javax.swing.JLabel();
        moduleSuiteValue = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        prjFolder.setLabelFor(prjFolderValue);
        org.openide.awt.Mnemonics.setLocalizedText(prjFolder, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "LBL_ProjectFolder"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(prjFolder, gridBagConstraints);

        srcLevel.setLabelFor(srcLevelValue);
        org.openide.awt.Mnemonics.setLocalizedText(srcLevel, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "LBL_SourceLevel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 12);
        add(srcLevel, gridBagConstraints);

        srcLevelValue.setPrototypeDisplayValue("mmm");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 0);
        add(srcLevelValue, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.weighty = 1.0;
        add(filler, gridBagConstraints);

        prjFolderValue.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(prjFolderValue, gridBagConstraints);

        moduleSuite.setLabelFor(moduleSuiteValue);
        org.openide.awt.Mnemonics.setLocalizedText(moduleSuite, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "LBL_ModeleSuite"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(moduleSuite, gridBagConstraints);

        moduleSuiteValue.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(moduleSuiteValue, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel filler;
    private javax.swing.JLabel moduleSuite;
    private javax.swing.JTextField moduleSuiteValue;
    private javax.swing.JLabel prjFolder;
    private javax.swing.JTextField prjFolderValue;
    private javax.swing.JLabel srcLevel;
    private javax.swing.JComboBox srcLevelValue;
    // End of variables declaration//GEN-END:variables
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(CustomizerSources.class, key);
    }
    
    private void initAccessibility() {
        srcLevelValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_SrcLevelValue"));
        moduleSuiteValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_ModuleSuiteValue"));
        prjFolderValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_PrjFolderValue"));
    }
    
    private String[] sourceLevels(JavaPlatform platform) {
        List<String> levels = new ArrayList<>();
        levels.add("1.4");
        levels.add("1.5");
        levels.add("1.6");
        levels.add("1.7");
        levels.add("1.8");
        try {
            String platformVersion = platform.getSpecification().getVersion().toString();
            int maxLevel = Integer.parseInt(platformVersion.split("\\.")[0]);
            for (int level = 9; level <= maxLevel; level++) {
                levels.add(Integer.toString(level));
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return levels.toArray(new String[0]);
    }

}
