/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.apisupport.project.ui.ApisupportAntUIUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.NotifyDescriptor;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;

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
                final String oldLevel = getProperty(SingleModuleProperties.JAVAC_SOURCE);
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
            for (int i = 0; i < SingleModuleProperties.SOURCE_LEVELS.length; i++) {
                srcLevelValue.addItem(SingleModuleProperties.SOURCE_LEVELS[i]);
            }
            srcLevelValue.setSelectedItem(getProperty(SingleModuleProperties.JAVAC_SOURCE));
        } finally {
            srcLevelValueBeingUpdated = false;
        }
        ApisupportAntUIUtils.setText(prjFolderValue, getProperties().getProjectDirectory());
    }
    
    public void store() {
        setProperty(SingleModuleProperties.JAVAC_SOURCE,
                (String) srcLevelValue.getSelectedItem());
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
    
}
