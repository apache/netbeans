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

package org.netbeans.modules.cnd.discovery.wizard;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.api.ProviderPropertyType;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;

/**
 *
 */
public final class SelectObjectFilesPanel extends JPanel {
    private final SelectObjectFilesWizard wizard;
    private final List<ProviderControl> controls = new ArrayList<>();

    /** Creates new form DiscoveryVisualPanel1 */
    public SelectObjectFilesPanel(SelectObjectFilesWizard wizard) {
        this.wizard = wizard;
        initComponents();
    }

    void read(DiscoveryDescriptor wizardDescriptor) {
        DiscoveryProvider provider = wizardDescriptor.getProvider();
        if (provider != null) {
            providerPanel.removeAll();
            controls.clear();
            boolean first = true;
            for(String key : provider.getPropertyKeys()){
                ProviderProperty<?> property = provider.getProperty(key);
                switch(property.getPropertyType().kind()) {
                    case MakeLogFile:
                    case BinaryFile:
                    case Folder:
                    case BinaryFiles:
                        ProviderControl pc = new ProviderControl(key, property, wizardDescriptor, providerPanel, wizard);
                        controls.add(pc);
                        if (first) {
                            instructionsTextArea.setText(property.getDescription());
                            first = false;
                        }
                        break;
                    case ArtifactFileSystem:
                        try {
                            FileSystem fileSystem = wizardDescriptor.getProject().getProjectDirectory().getFileSystem();
                            if (ProviderPropertyType.LogFileSystemPropertyType == property.getPropertyType()) {
                                ProviderPropertyType.LogFileSystemPropertyType.setProperty(provider, fileSystem);
                            } else if (ProviderPropertyType.BinaryFileSystemPropertyType == property.getPropertyType()) {
                                ProviderPropertyType.BinaryFileSystemPropertyType.setProperty(provider, fileSystem);
                            }
                        } catch (FileStateInvalidException ex) {
                        }
                        break;
                    default:
                        // unsuported UI
                        break;
                }
            }
        }
    }

    void store(DiscoveryDescriptor wizardDescriptor) {
        for(ProviderControl pc : controls){
            pc.store();
        }
        wizardDescriptor.setInvokeProvider(true);
    }

    boolean valid() {
        if (controls.size() == 0){
            return false;
        }
        for(ProviderControl pc : controls){
            if (!pc.valid()){
                return false;
            }
        }
        return true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        providerPanel = new javax.swing.JPanel();
        instructionPanel = new javax.swing.JPanel();
        instructionsTextArea = new javax.swing.JTextArea();

        setPreferredSize(new java.awt.Dimension(400, 300));
        setLayout(new java.awt.GridBagLayout());

        providerPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(providerPanel, gridBagConstraints);

        instructionPanel.setLayout(new java.awt.BorderLayout());

        instructionsTextArea.setBackground(instructionPanel.getBackground());
        instructionsTextArea.setEditable(false);
        instructionsTextArea.setLineWrap(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/discovery/wizard/Bundle"); // NOI18N
        instructionsTextArea.setText(bundle.getString("BuildActionsInstructions")); // NOI18N
        instructionsTextArea.setWrapStyleWord(true);
        instructionsTextArea.setOpaque(false);
        instructionPanel.add(instructionsTextArea, java.awt.BorderLayout.SOUTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(instructionPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
            
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel instructionPanel;
    private javax.swing.JTextArea instructionsTextArea;
    private javax.swing.JPanel providerPanel;
    // End of variables declaration//GEN-END:variables

}

