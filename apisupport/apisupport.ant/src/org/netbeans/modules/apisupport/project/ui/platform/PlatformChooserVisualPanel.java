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

package org.netbeans.modules.apisupport.project.ui.platform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.apisupport.project.ui.ModuleUISettings;
import org.netbeans.modules.apisupport.project.api.BasicVisualPanel;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * First panel from <em>Adding New Platform</em> wizard panels. Allows user to
 * choose platform directory.
 *
 * @author Martin Krauskopf
 */
public class PlatformChooserVisualPanel extends BasicVisualPanel
        implements PropertyChangeListener {
    
    /** Creates new form BasicInfoVisualPanel */
    public PlatformChooserVisualPanel(WizardDescriptor setting) {
        super(setting);
        initComponents();
        initAccessibility();
        String location = ModuleUISettings.getDefault().getLastUsedNbPlatformLocation();
        if (location != null) {
            //#199448
            File curDir = new File(location);
            if( curDir.equals(platformChooser.getCurrentDirectory()) && null != curDir.getParentFile() ) {
                platformChooser.setCurrentDirectory(curDir.getParentFile());
            }
            platformChooser.setCurrentDirectory(curDir);
        }
        platformChooser.setAcceptAllFileFilterUsed(false);
        platformChooser.setFileFilter(new FileFilter() {
            public boolean accept(File f)  {
                return f.isDirectory();
            }
            public String getDescription() {
                return getMessage("CTL_PlatformFolder");
            }
        });
        platformChooser.addPropertyChangeListener(this);
        setName(NbPlatformCustomizer.CHOOSER_STEP);
        platformChooser.putClientProperty(
                "JFileChooser.appBundleIsTraversable", "always"); // NOI18N #73124
    }

    public void addNotify() {
        super.addNotify();
        checkForm();
    }
    
    /** Stores collected data into model. */
    void storeData() {
        File file = platformChooser.getSelectedFile();
        if (file != null) {
            getSettings().putProperty(NbPlatformCustomizer.PLAF_DIR_PROPERTY,
                    file.getAbsolutePath());
            getSettings().putProperty(NbPlatformCustomizer.PLAF_LABEL_PROPERTY,
                    plafNameValue.getText());
        } // when wizard is cancelled file is null
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (propName.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
            checkForm();
        }
    }
    
    private void checkForm() {
        File selFile = platformChooser.getSelectedFile();
        boolean invalid = true;
        if (selFile != null) { // #73123
            File plafDir = FileUtil.normalizeFile(selFile);
            if (/* #60133 */ NbPlatform.isPlatformDirectory(plafDir)) {
                try {
                    setPlafLabel(cleanupLabel(NbPlatform.computeDisplayName(plafDir)));
                } catch (IOException e) {
                    setPlafLabel(plafDir.getAbsolutePath());
                }
                plafLabelValue.setText(NbPlatform.getComputedLabel(plafDir));
                plafLabelValue.setCaretPosition(0);
                if (!NbPlatform.isSupportedPlatform(plafDir)) {
                    setError(getMessage("MSG_UnsupportedPlatform"));
                } else if (NbPlatform.contains(plafDir)) {
                    setError(getMessage("MSG_AlreadyAddedPlatform"));
                } else if (!NbPlatform.isLabelValid(plafNameValue.getText())) {
                    setWarning(getMessage("MSG_NameIsAlreadyUsedGoToNext"));
                } else {
                    markValid();
                    ModuleUISettings.getDefault().setLastUsedNbPlatformLocation(plafDir.getParentFile().getAbsolutePath());
                }
                invalid = false;
            }
        }
        if (invalid) {
            markInvalid();
            setPlafLabel(null);
            plafLabelValue.setText(null);
            storeData();
        }
    }
    
    private void setPlafLabel(String label) {
        plafNameValue.setText(label);
        plafNameValue.setCaretPosition(0);
        storeData();
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(PlatformChooserVisualPanel.class, key);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        infoPanel = new javax.swing.JPanel();
        plafLabel = new javax.swing.JLabel();
        plafLabelValue = new javax.swing.JTextField();
        plafName = new javax.swing.JLabel();
        plafNameValue = new javax.swing.JTextField();
        filler = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        platformChooser = new javax.swing.JFileChooser();

        java.awt.GridBagLayout infoPanelLayout = new java.awt.GridBagLayout();
        infoPanelLayout.columnWidths = new int[] {0};
        infoPanelLayout.rowHeights = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0};
        infoPanel.setLayout(infoPanelLayout);

        plafLabel.setLabelFor(plafLabelValue);
        org.openide.awt.Mnemonics.setLocalizedText(plafLabel, NbBundle.getMessage(PlatformChooserVisualPanel.class, "LBL_PlatformName_N")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        infoPanel.add(plafLabel, gridBagConstraints);

        plafLabelValue.setColumns(15);
        plafLabelValue.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        infoPanel.add(plafLabelValue, gridBagConstraints);

        plafName.setLabelFor(plafNameValue);
        org.openide.awt.Mnemonics.setLocalizedText(plafName, org.openide.util.NbBundle.getMessage(PlatformChooserVisualPanel.class, "LBL_PlatformName_P")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        infoPanel.add(plafName, gridBagConstraints);

        plafNameValue.setColumns(15);
        plafNameValue.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        infoPanel.add(plafNameValue, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.weighty = 1.0;
        infoPanel.add(filler, gridBagConstraints);

        setLayout(new java.awt.BorderLayout());

        platformChooser.setAccessory(infoPanel);
        platformChooser.setControlButtonsAreShown(false);
        platformChooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
        add(platformChooser, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.Box.Filler filler;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JLabel plafLabel;
    private javax.swing.JTextField plafLabelValue;
    private javax.swing.JLabel plafName;
    private javax.swing.JTextField plafNameValue;
    private javax.swing.JFileChooser platformChooser;
    // End of variables declaration//GEN-END:variables
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(getMessage("ACS_PlatformChooserVisualPanel"));
        plafNameValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_plafLabelValue"));
    }

    private static final Pattern LABEL_PATTERN = Pattern.compile("NetBeans (?:Platform|IDE) (Dev|[0-9.]+)(?: [(]Build .+[)])?"); // NOI18N
    static String cleanupLabel(String label) { // #200660
        Matcher m = LABEL_PATTERN.matcher(label);
        if (m.matches()) {
            String v1 = m.group(1);
            String v2;
            if (v1.equals("Dev")) { // NOI18N
                v2 = "dev"; // NOI18N
            } else {
                v2 = v1.replace(".", ""); // NOI18N
            }
            return "nb" + v2; // NOI18N
        }
        return label;
    }
    
}
