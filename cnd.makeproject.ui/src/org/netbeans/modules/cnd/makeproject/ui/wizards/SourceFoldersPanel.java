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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/*package*/ final class SourceFoldersPanel extends javax.swing.JPanel implements HelpCtx.Provider {

    private final SourceFoldersDescriptorPanel controller;
    private final SourceFilesPanel sourceFilesPanel;
    private boolean firstTime = true;

    public SourceFoldersPanel(SourceFoldersDescriptorPanel controller) {
        initComponents();
        this.controller = controller;
        sourceFilesPanel = new SourceFilesPanel(controller);
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        headerFoldersOuterPanel.add(sourceFilesPanel, gridBagConstraints);
        instructionsTextArea.setBackground(instructionPanel.getBackground());

        getAccessibleContext().setAccessibleDescription(getString("SourceFoldersPanel_AD"));
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(SourceFoldersPanel.class);
    }

    void read(WizardDescriptor settings) {
        if (firstTime) {
            FSPath wd = WizardConstants.PROPERTY_PROJECT_FOLDER.get(settings);
            //sourceFilesPanel.setSeed(workingdir, workingdir);
            sourceFilesPanel.getSourceListData().add(new FolderEntry(wd, wd.getPath()));
            sourceFilesPanel.setFoldersFilter(MakeConfigurationDescriptor.DEFAULT_IGNORE_FOLDERS_PATTERN_EXISTING_PROJECT);
            sourceFilesPanel.setResolveSymLinks(MakeProjectOptions.getResolveSymbolicLinks());
            firstTime = false;
        }
    }

    void store(WizardDescriptor wizardDescriptor) {
        WizardConstants.PROPERTY_SOURCE_FOLDERS.put(wizardDescriptor, sourceFilesPanel.getSourceListData().iterator()); // NOI18N
        WizardConstants.PROPERTY_SOURCE_FOLDERS_LIST.put(wizardDescriptor, new ArrayList<>(sourceFilesPanel.getSourceListData())); // NOI18N
        if (sourceFilesPanel.getFoldersFilter().trim().length() == 0) {
            // change empty pattern on "no ignore folder pattern"
            WizardConstants.PROPERTY_SOURCE_FOLDERS_FILTER.put(wizardDescriptor, MakeConfigurationDescriptor.DEFAULT_NO_IGNORE_FOLDERS_PATTERN); // NOI18N
        } else {
            WizardConstants.PROPERTY_SOURCE_FOLDERS_FILTER.put(wizardDescriptor, sourceFilesPanel.getFoldersFilter()); // NOI18N
        }
        WizardConstants.PROPERTY_RESOLVE_SYM_LINKS.put(wizardDescriptor, sourceFilesPanel.getResolveSymLinks());
        WizardConstants.PROPERTY_TEST_FOLDERS.put(wizardDescriptor, sourceFilesPanel.getTestListData().iterator()); // NOI18N
        WizardConstants.PROPERTY_TEST_FOLDERS_LIST.put(wizardDescriptor, new ArrayList<>(sourceFilesPanel.getTestListData())); // NOI18N
    }

    boolean valid(WizardDescriptor settings) {
        String regex = sourceFilesPanel.getFoldersFilter();
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        headerFoldersOuterPanel = new javax.swing.JPanel();
        instructionPanel = new javax.swing.JPanel();
        instructionsTextArea = new javax.swing.JTextArea();

        setPreferredSize(new java.awt.Dimension(450, 350));
        setLayout(new java.awt.GridBagLayout());

        headerFoldersOuterPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(headerFoldersOuterPanel, gridBagConstraints);

        instructionPanel.setLayout(new java.awt.GridBagLayout());

        instructionsTextArea.setEditable(false);
        instructionsTextArea.setLineWrap(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle"); // NOI18N
        instructionsTextArea.setText(bundle.getString("SourceFilesInstructions")); // NOI18N
        instructionsTextArea.setWrapStyleWord(true);
        instructionsTextArea.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        instructionPanel.add(instructionsTextArea, gridBagConstraints);
        instructionsTextArea.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "SourceFoldersInfo_AN")); // NOI18N
        instructionsTextArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SourceFoldersPanel.class, "SourceFoldersInfo_AD")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        add(instructionPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel headerFoldersOuterPanel;
    private javax.swing.JPanel instructionPanel;
    private javax.swing.JTextArea instructionsTextArea;
    // End of variables declaration//GEN-END:variables

    private static String getString(String s) {
        return NbBundle.getMessage(PanelProjectLocationVisual.class, s);
    }
}
