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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Set;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.JmsDestinationDefinition;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Panel for specifying message destination. Project or server message destination can be chosen.
 * @author Tomas Mysik
 */
@SuppressWarnings("serial") // not used to be serialized
public class MdbLocationPanelVisual extends javax.swing.JPanel {

    public static final String CHANGED = MdbLocationPanelVisual.class.getName() + ".CHANGED";
    public static final String SCANNED = MdbLocationPanelVisual.class.getName() + ".SCANNED";

    private final Project project;
    private final J2eeModuleProvider provider;
    private final boolean isDestinationCreationSupportedByServerPlugin;

    private Set<MessageDestination> moduleDestinations;
    private Set<MessageDestination> serverDestinations;
    
    // private because correct initialization is needed
    @NbBundle.Messages({
        "MdbLocationPanel.warn.scanning.in.progress=Scanning in progress, parial results shown..."
    })
    private MdbLocationPanelVisual(Project project, J2eeModuleProvider provider, Set<MessageDestination> moduleDestinations, Set<MessageDestination> serverDestinations) {
        initComponents();
        projectDestinationsCombo.setModel(new ProjectDestinationsComboModel(projectDestinationsCombo.getEditor()));
        this.project = project;
        this.provider = provider;
        this.moduleDestinations = moduleDestinations;
        this.serverDestinations = serverDestinations;
        isDestinationCreationSupportedByServerPlugin = provider.getConfigSupport().supportsCreateMessageDestination();

        // scanning in progress?
        if (!SourceUtils.isScanInProgress()) {
            scanningLabel.setVisible(false);
        } else {
            ClasspathInfo classPathInfo = MdbLocationPanel.getClassPathInfo(project);
            JavaSource javaSource = JavaSource.create(classPathInfo);
            try {
                javaSource.runWhenScanFinished(new Task<CompilationController>() {
                    @Override
                    public void run(CompilationController parameter) throws Exception {
                        fire(true);
                    }
                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * Factory method for creating new instance.
     * @param provider Java EE module provider.
     * @param moduleDestinations project message destinations.
     * @param serverDestinations server message destinations.
     * @return MessageEJBWizardVisualPanel instance.
     */
    public static MdbLocationPanelVisual newInstance(final Project project, final J2eeModuleProvider provider,
            final Set<MessageDestination> moduleDestinations, final Set<MessageDestination> serverDestinations) {
        MdbLocationPanelVisual mdp = new MdbLocationPanelVisual(project, provider, moduleDestinations, serverDestinations);
        mdp.initialize();
        return mdp;
    }

    /**
     * Get the message destination. If the destination is instance of the {@code JmsDestinationDefinition} and its
     * flag {@code toGenerate} is set to {@code true} the destination should be generated into the target file.
     *
     * @return selected destination or <code>null</code> if no destination type is selected.
     */
    public MessageDestination getDestination() {
        if (projectDestinationsRadio.isSelected()) {
            if (projectDestinationsCombo.getSelectedItem() == null
                    || ((String) projectDestinationsCombo.getSelectedItem()).isEmpty()) {
                return null;
            } else {
                final String selectedDestination = (String) projectDestinationsCombo.getSelectedItem();
                for (MessageDestination messageDestination : moduleDestinations) {
                    if (messageDestination.getName().equals(selectedDestination)) {
                        // predefined project's message destinations
                        return messageDestination;
                    }
                }
                // message destination is unknown
                return new JmsDestinationDefinition(selectedDestination, Type.QUEUE, false);
            }
        } else if (serverDestinationsRadio.isSelected()) {
            return (MessageDestination) serverDestinationsCombo.getSelectedItem();
        }
        return null;
    }
    
    public boolean isServerConfigured() {
        String id = provider.getServerInstanceID();
        try {
            return id != null && Deployment.getDefault().getServerInstance(id).getJ2eePlatform() != null;
        } catch (InstanceRemovedException ex) {
            return false;
        }
    }

    private void initialize() {
        registerListeners();
        setupAddButton();
        handleComboBoxes();
        
        populate();
    }
    
    private void registerListeners() {
        // radio buttons
        projectDestinationsRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                fire(false);
                handleComboBoxes();
            }
        });
        serverDestinationsRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                fire(false);
                handleComboBoxes();
            }
        });

        // combo boxes
        projectDestinationsCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                fire(false);
            }
        });
        serverDestinationsCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                fire(false);
            }
        });

        // text into the project combobox
        final JTextComponent tc = (JTextComponent) projectDestinationsCombo.getEditor().getEditorComponent();
        tc.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                fireUpdate();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                fireUpdate();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                fireUpdate();
            }
            private void fireUpdate() {
                fire(false);
            }
        });
    }
    private void setupAddButton() {
        if (!isDestinationCreationSupportedByServerPlugin) {
            // missing server?
            addButton.setEnabled(false);
        }
    }
   
    private void handleComboBoxes() {
        projectDestinationsCombo.setEnabled(projectDestinationsRadio.isSelected());
        serverDestinationsCombo.setEnabled(serverDestinationsRadio.isSelected());
    }
    
    private void populate() {
        MessageDestinationUiSupport.populateDestinations(moduleDestinations, projectDestinationsCombo, null);
        MessageDestinationUiSupport.populateDestinations(serverDestinations, serverDestinationsCombo, null);
    }
    
    private void fire(boolean fromScanning) {
        if (fromScanning) {
            firePropertyChange(SCANNED, null, null);
            scanningLabel.setVisible(SourceUtils.isScanInProgress());
        }
        firePropertyChange(CHANGED, null, null);
    }

    void store(WizardDescriptor descriptor) {
        descriptor.putProperty(MdbWizard.PROP_DESTINATION_TYPE, getDestination());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        destinationsGroup = new javax.swing.ButtonGroup();
        projectDestinationsRadio = new javax.swing.JRadioButton();
        serverDestinationsRadio = new javax.swing.JRadioButton();
        projectDestinationsCombo = new javax.swing.JComboBox();
        addButton = new javax.swing.JButton();
        serverDestinationsCombo = new javax.swing.JComboBox();
        scanningLabel = new javax.swing.JLabel();

        destinationsGroup.add(projectDestinationsRadio);
        projectDestinationsRadio.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(projectDestinationsRadio, org.openide.util.NbBundle.getMessage(MdbLocationPanelVisual.class, "LBL_ProjectDestinations")); // NOI18N
        projectDestinationsRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        projectDestinationsRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        destinationsGroup.add(serverDestinationsRadio);
        org.openide.awt.Mnemonics.setLocalizedText(serverDestinationsRadio, org.openide.util.NbBundle.getMessage(MdbLocationPanelVisual.class, "LBL_ServerDestinations")); // NOI18N
        serverDestinationsRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        serverDestinationsRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        projectDestinationsCombo.setEditable(true);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(MdbLocationPanelVisual.class, "LBL_Add")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        scanningLabel.setForeground(new java.awt.Color(102, 102, 102));
        org.openide.awt.Mnemonics.setLocalizedText(scanningLabel, org.openide.util.NbBundle.getMessage(MdbLocationPanelVisual.class, "MdbLocationPanelVisual.scanningLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(projectDestinationsRadio)
                            .addComponent(serverDestinationsRadio))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(serverDestinationsCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(projectDestinationsCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(scanningLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectDestinationsRadio)
                    .addComponent(addButton)
                    .addComponent(projectDestinationsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverDestinationsRadio)
                    .addComponent(serverDestinationsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(scanningLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        MessageDestination destination = 
                MessageDestinationUiSupport.prepareMessageDestination(project, provider, moduleDestinations, serverDestinations);
        if (destination != null) {
            moduleDestinations.add(destination);
            MessageDestinationUiSupport.populateDestinations(moduleDestinations, projectDestinationsCombo, destination);
        }
    }//GEN-LAST:event_addButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.ButtonGroup destinationsGroup;
    private javax.swing.JComboBox projectDestinationsCombo;
    private javax.swing.JRadioButton projectDestinationsRadio;
    private javax.swing.JLabel scanningLabel;
    private javax.swing.JComboBox serverDestinationsCombo;
    private javax.swing.JRadioButton serverDestinationsRadio;
    // End of variables declaration//GEN-END:variables

    void refreshDestinations(Set<MessageDestination> moduleDestinations, Set<MessageDestination> serverDestinations) {
        this.moduleDestinations = moduleDestinations;
        this.serverDestinations = serverDestinations;
    }

    @SuppressWarnings("serial") // not used to be serialized
    private static class ProjectDestinationsComboModel extends DefaultComboBoxModel {

        private final ComboBoxEditor comboEditor;

        public ProjectDestinationsComboModel(ComboBoxEditor comboEditor) {
            this.comboEditor = comboEditor;
        }

        @Override
        public Object getSelectedItem() {
            Object selectedItem = super.getSelectedItem();
            if (selectedItem instanceof MessageDestination) {
                return ((MessageDestination) selectedItem).getName();
            } else {
                return comboEditor.getItem();
            }
        }

    }

}
