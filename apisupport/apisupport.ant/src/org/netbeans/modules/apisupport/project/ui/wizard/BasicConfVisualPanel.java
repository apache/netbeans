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

package org.netbeans.modules.apisupport.project.ui.wizard;

import java.io.File;
import java.io.IOException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.ApisupportAntUtils;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import static org.netbeans.modules.apisupport.project.ui.wizard.Bundle.*;
import org.openide.util.NbBundle.Messages;

/**
 * Second UI panel of <code>NewNbModuleWizardIterator</code> for
 * <em>standalone</em> module creating mode. Allow user to enter basic
 * configuration:
 *
 * <ul>
 *  <li>Code Name Base</li>
 *  <li>Module Display Name</li>
 *  <li>Localizing Bundle</li>
 * </ul>
 *
 * @author Martin Krauskopf
 */
final class BasicConfVisualPanel extends NewTemplateVisualPanel {
    
    private boolean wasBundleUpdated;
    
    private boolean listenersAttached;
    private final DocumentListener cnbDL;
    private final DocumentListener bundleDL;
    
    public BasicConfVisualPanel(final NewModuleProjectData data) {
        super(data);
        initComponents();
        cnbDL = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) { 
                checkValues(true, false); 
            }
        };
        if (isLibraryWizard()) {
            // We do not intend to support OSGi-style lib wrappers.
            // These would need to use Bundle-ClassPath etc.
            osgi.setVisible(false);
        }
        bundleDL = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) { 
                wasBundleUpdated = true; 
                checkValues(false, true); 
            }
        };
    }
    
    private boolean checkCodeNameBase() {
        String dotName = getCodeNameBaseValue();
        if (dotName.length() == 0) {
            setInfo(getMessage("MSG_EmptyCNB"), false);
        } else if (!ApisupportAntUtils.isValidJavaFQN(dotName)) {
            setError(getMessage("MSG_InvalidCNB"));
        } else if (getData().isSuiteComponent() && cnbIsAlreadyInSuite(getData().getSuiteRoot(), dotName)) {
            setError(NbBundle.getMessage(BasicConfVisualPanel.class, "MSG_ComponentWithSuchCNBAlreadyInSuite", dotName));
        } else {
            // update bundle from the cnb
            String slashName = dotName.replace('.', '/');
            if (! wasBundleUpdated) {
                bundleValue.setText(slashName + "/Bundle.properties"); // NOI18N
                wasBundleUpdated = false;
            }
            if (getData().isNetBeansOrg()) {
                // Ensure that official naming conventions are respected.
                String cnbShort = ModuleList.abbreviate(dotName);
                String name = getData().getProjectName();
                if (!name.equals(cnbShort)) {
                    setError(NbBundle.getMessage(BasicConfVisualPanel.class, "BasicConfVisualPanel_err_wrong_nborg_name", cnbShort));
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    @Messages({
        "BasicConfVisualPanel_err_bundle_empty=Bundle cannot be empty.",
        "BasicConfVisualPanel_err_bundle_def_pkg=Cannot use default package for bundle.",
        "BasicConfVisualPanel_err_bundle_extension=Bundle must have \".properties\" extension.",
        "BasicConfVisualPanel_err_bundle_is_not_valid=Bundle is not valid."
    })
    private boolean checkBundle() {
        String path = getBundleValue();
        if (path.length() == 0) {
            setError(BasicConfVisualPanel_err_bundle_empty());
            return false;
        }
        if (path.indexOf('/') == -1) {
            setError(BasicConfVisualPanel_err_bundle_def_pkg());
            return false;
        }
        if (!path.endsWith(".properties")) {
            setError(BasicConfVisualPanel_err_bundle_extension());
            return false;
        }
        if (!ApisupportAntUtils.isValidFilePath(path)) {
            setError(BasicConfVisualPanel_err_bundle_is_not_valid());
            return false;
        }
        return true;
    }
    
    private void checkValues(boolean preferCNB, boolean preferBundle) {
        if (preferCNB && ! checkCodeNameBase())
            return; // invalid CNB
        if (preferBundle && ! checkBundle())
            return; // invalid Bundle
        if (! preferCNB && ! checkCodeNameBase())
            return;  // invalid CNB
        if (! preferBundle && ! checkBundle())
            return; // invalid Bundle
        // all valid
        markValid();
    }
    
    void refreshData() {
        String dn = getData().getProjectDisplayName();
        displayNameValue.setText(dn);
        checkValues(true, false);
    }
    
    /** Stores collected data into model. */
    void storeData() {
        // change will be fired -> update data
        getData().setCodeNameBase(getCodeNameBaseValue());
        getData().setProjectDisplayName(displayNameValue.getText());
        getData().setBundle(getBundleValue());
        getData().setOsgi(osgi.isSelected());
    }
    
    private String getCodeNameBaseValue() {
        return codeNameBaseValue.getText().trim();
    }
    
    private String getBundleValue() {
        return bundleValue.getText().trim();
    }
    
    private boolean cnbIsAlreadyInSuite(String suiteDir, String cnb) {
        FileObject suiteDirFO = FileUtil.toFileObject(new File(suiteDir));
        try {
            Project suite = ProjectManager.getDefault().findProject(suiteDirFO);
            if (suite == null) { // #180644
                return false;
            }
            for (Project p : SuiteUtils.getSubProjects(suite)) {
                if (ProjectUtils.getInformation(p).getName().equals(cnb)) {
                    return true;
                }
            }
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        }
        return false;
    }
    
    public @Override void addNotify() {
        super.addNotify();
        attachDocumentListeners();
    }
    
    public @Override void removeNotify() {
        // prevent checking when the panel is not "active"
        removeDocumentListeners();
        super.removeNotify();
    }
    
    private void attachDocumentListeners() {
        if (!listenersAttached) {
            codeNameBaseValue.getDocument().addDocumentListener(cnbDL);
            bundleValue.getDocument().addDocumentListener(bundleDL);
            listenersAttached = true;
        }
    }
    
    private void removeDocumentListeners() {
        if (listenersAttached) {
            codeNameBaseValue.getDocument().removeDocumentListener(cnbDL);
            bundleValue.getDocument().removeDocumentListener(bundleDL);
            listenersAttached = false;
        }
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(BasicConfVisualPanel.class, key);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        confPanel = new javax.swing.JPanel();
        codeNameBase = new javax.swing.JLabel();
        codeNameBaseValue = new javax.swing.JTextField();
        filler = new javax.swing.JLabel();
        cnbHint = new javax.swing.JLabel();
        displayName = new javax.swing.JLabel();
        displayNameValue = new javax.swing.JTextField();
        bundle = new javax.swing.JLabel();
        bundleValue = new javax.swing.JTextField();
        osgi = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        confPanel.setLayout(new java.awt.GridBagLayout());

        codeNameBase.setLabelFor(codeNameBaseValue);
        org.openide.awt.Mnemonics.setLocalizedText(codeNameBase, org.openide.util.NbBundle.getMessage(BasicConfVisualPanel.class, "LBL_CodeNameBase")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 6, 12);
        confPanel.add(codeNameBase, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 4, 0);
        confPanel.add(codeNameBaseValue, gridBagConstraints);
        codeNameBaseValue.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BasicConfVisualPanel.class, "ACS_CTL_CodeNameBaseValue")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        confPanel.add(filler, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cnbHint, getMessage("LBL_CodeNameBaseHint"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        confPanel.add(cnbHint, gridBagConstraints);

        displayName.setLabelFor(displayNameValue);
        org.openide.awt.Mnemonics.setLocalizedText(displayName, org.openide.util.NbBundle.getMessage(BasicConfVisualPanel.class, "LBL_ModuleDisplayName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 12);
        confPanel.add(displayName, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        confPanel.add(displayNameValue, gridBagConstraints);
        displayNameValue.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BasicConfVisualPanel.class, "ACS_CTL_DisplayNameValue")); // NOI18N

        bundle.setLabelFor(bundleValue);
        org.openide.awt.Mnemonics.setLocalizedText(bundle, org.openide.util.NbBundle.getMessage(BasicConfVisualPanel.class, "LBL_LocalizingBundle")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 5, 0, 12);
        confPanel.add(bundle, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 0);
        confPanel.add(bundleValue, gridBagConstraints);
        bundleValue.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BasicConfVisualPanel.class, "ACS_CTL_BundleValue")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(osgi, NbBundle.getMessage(BasicConfVisualPanel.class, "BasicConfVisualPanel.osgi")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        confPanel.add(osgi, gridBagConstraints);
        osgi.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BasicConfVisualPanel.class, "BasicConfVisualPanel.osgi.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        add(confPanel, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BasicConfVisualPanel.class, "ACS_BasicConfVisualPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bundle;
    private javax.swing.JTextField bundleValue;
    private javax.swing.JLabel cnbHint;
    private javax.swing.JLabel codeNameBase;
    private javax.swing.JTextField codeNameBaseValue;
    private javax.swing.JPanel confPanel;
    private javax.swing.JLabel displayName;
    private javax.swing.JTextField displayNameValue;
    private javax.swing.JLabel filler;
    private javax.swing.JCheckBox osgi;
    // End of variables declaration//GEN-END:variables
    
}
