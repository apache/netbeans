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

package org.netbeans.modules.j2ee.earproject.ui.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.javaee.project.api.ui.utils.J2eePlatformUiSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.web.browser.api.BrowserUISupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public final class CustomizerRun extends JPanel implements HelpCtx.Provider {
    private static final long serialVersionUID = 1L;
    
    private String[] serverInstanceIDs;
    private String[] serverNames;
    
    /** Whether this panel was already initialized. */
    private boolean initialized;
    
    private final EarProjectProperties uiProperties;
    
    public CustomizerRun(EarProjectProperties earProperties) {
        this.uiProperties = earProperties;
        initComponents();
        this.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(CustomizerRun.class, "ACS_CustomizeRun_A11YDesc")); // NOI18N
        clientModuleUriCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateEnabled();
            }
        });
        initValues();
        updateEnabled();
    }
    
    private JComboBox createBrowserComboBox() {
        JComboBox combo = BrowserUISupport.createBrowserPickerComboBox(
                uiProperties.BROWSERS_MODEL.getSelectedBrowserId(), true, false,
                uiProperties.BROWSERS_MODEL);
        return combo;
    }

    private boolean isWebModuleSelected() {
        return clientModuleUriCombo.getSelectedItem() == null ||
                !((ClientModuleItem)clientModuleUriCombo.getSelectedItem()).appClient;
    }
    
    private void updateEnabled() {
        boolean isWebUri = isWebModuleSelected();
        
        handleWebModuleRelated(isWebUri);
        
        jTextMainClass.setEnabled(!isWebUri);
        jTextArgs.setEnabled(!isWebUri);
        jTextVMOptions.setEnabled(!isWebUri);
        jLabelMainClass.setEnabled(!isWebUri);
        jLabelArgs.setEnabled(!isWebUri);
        jLabelVMOptions.setEnabled(!isWebUri);
        jLabelVMOptionsExample.setEnabled(!isWebUri);
    }
    
    public void initValues() {
        if (initialized) {
            return;
        }
        jTextFieldRelativeURL.setDocument( uiProperties.LAUNCH_URL_RELATIVE_MODEL );
        jCheckBoxDisplayBrowser.setModel( uiProperties.DISPLAY_BROWSER_MODEL ); 
        jComboBoxServer.setModel( uiProperties.J2EE_SERVER_INSTANCE_MODEL );
        jTextMainClass.setDocument(uiProperties.MAIN_CLASS_MODEL);
        jTextArgs.setDocument(uiProperties.ARUGMENTS_MODEL);
        jTextVMOptions.setDocument(uiProperties.VM_OPTIONS_MODEL);
        clientModuleUriCombo.setModel(uiProperties.CLIENT_MODULE_MODEL);
        jCheckBoxDeployOnSave.setModel(uiProperties.DEPLOY_ON_SAVE_MODEL);

        Profile j2eeProfile = uiProperties.getProject().getJ2eeProfile();
        if (j2eeProfile != null) {
            jTextFieldVersion.setText(j2eeProfile.getDisplayName());
        }
        setDeployOnSaveState();
        handleWebModuleRelated();
        initialized = true;
    }
    
    private void handleWebModuleRelated() {
        handleWebModuleRelated(isWebModuleSelected());
    }
    
    private void handleWebModuleRelated(Boolean isWebUri) {
        jCheckBoxDisplayBrowser.setEnabled(isWebUri);
        jLabelContextPathDesc.setEnabled(isWebUri && jCheckBoxDisplayBrowser.isSelected());
        jLabelRelativeURL.setEnabled(isWebUri && jCheckBoxDisplayBrowser.isSelected());
        jTextFieldRelativeURL.setEnabled(isWebUri && jCheckBoxDisplayBrowser.isSelected());
        jTextFieldRelativeURL.setEditable(isWebUri && jCheckBoxDisplayBrowser.isSelected());
        jBrowserLabel.setEnabled(isWebUri && jCheckBoxDisplayBrowser.isSelected());
        jBrowserComboBox.setEnabled(isWebUri && jCheckBoxDisplayBrowser.isSelected());
    }
    
    private int getLongestVersionLength() {
        // FIXME fix UI to handle the case without this stuff :/
        return Profile.JAVA_EE_6_FULL.getDisplayName().length();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelContextPath = new javax.swing.JLabel();
        jLabelServer = new javax.swing.JLabel();
        jComboBoxServer = new javax.swing.JComboBox();
        clientModuleUriCombo = new javax.swing.JComboBox();
        webInfoPanel = new javax.swing.JPanel();
        jLabelContextPathDesc = new javax.swing.JLabel();
        jLabelRelativeURL = new javax.swing.JLabel();
        jTextFieldRelativeURL = new javax.swing.JTextField();
        jCheckBoxDisplayBrowser = new javax.swing.JCheckBox();
        jBrowserLabel = new javax.swing.JLabel();
        jBrowserComboBox = createBrowserComboBox();
        clientInfoPanel = new javax.swing.JPanel();
        jLabelVMOptionsExample = new javax.swing.JLabel();
        jTextVMOptions = new javax.swing.JTextField();
        jLabelVMOptions = new javax.swing.JLabel();
        jLabelArgs = new javax.swing.JLabel();
        jLabelMainClass = new javax.swing.JLabel();
        jTextMainClass = new javax.swing.JTextField();
        jTextArgs = new javax.swing.JTextField();
        filler = new javax.swing.JLabel();
        jLabelVersion = new javax.swing.JLabel();
        jTextFieldVersion = new javax.swing.JTextField();
        jCheckBoxDeployOnSave = new javax.swing.JCheckBox();
        dosDescription = new javax.swing.JLabel();

        jLabelContextPath.setLabelFor(clientModuleUriCombo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelContextPath, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_ClientModuleURI_JLabel")); // NOI18N

        jLabelServer.setLabelFor(jComboBoxServer);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelServer, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Server_JLabel")); // NOI18N

        jComboBoxServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxServerActionPerformed(evt);
            }
        });

        webInfoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_WebModInfo"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabelContextPathDesc, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_ContextPathDesc_JLabel")); // NOI18N

        jLabelRelativeURL.setLabelFor(jTextFieldRelativeURL);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelRelativeURL, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_RelativeURL_JLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxDisplayBrowser, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_DisplayBrowser_JCheckBox")); // NOI18N
        jCheckBoxDisplayBrowser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxDisplayBrowserActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jBrowserLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_Browser")); // NOI18N

        javax.swing.GroupLayout webInfoPanelLayout = new javax.swing.GroupLayout(webInfoPanel);
        webInfoPanel.setLayout(webInfoPanelLayout);
        webInfoPanelLayout.setHorizontalGroup(
            webInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(webInfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(webInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(webInfoPanelLayout.createSequentialGroup()
                        .addComponent(jLabelRelativeURL)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldRelativeURL, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE))
                    .addGroup(webInfoPanelLayout.createSequentialGroup()
                        .addGroup(webInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBoxDisplayBrowser)
                            .addComponent(jLabelContextPathDesc))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(webInfoPanelLayout.createSequentialGroup()
                        .addComponent(jBrowserLabel)
                        .addGap(35, 35, 35)
                        .addComponent(jBrowserComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        webInfoPanelLayout.setVerticalGroup(
            webInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(webInfoPanelLayout.createSequentialGroup()
                .addComponent(jCheckBoxDisplayBrowser)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelContextPathDesc)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(webInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelRelativeURL)
                    .addComponent(jTextFieldRelativeURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(webInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBrowserLabel)
                    .addComponent(jBrowserComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTextFieldRelativeURL.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "ACS_CustomizeRun_RelativeURL_A11YDesc")); // NOI18N
        jCheckBoxDisplayBrowser.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "ACS_CustomizeRun_DisplayBrowser_A11YDesc")); // NOI18N

        clientInfoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_ClientInfo"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabelVMOptionsExample, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_VM_Options_Example")); // NOI18N

        jLabelVMOptions.setLabelFor(jTextVMOptions);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelVMOptions, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_VM_Options")); // NOI18N

        jLabelArgs.setLabelFor(jTextArgs);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelArgs, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_Args_JLabel")); // NOI18N

        jLabelMainClass.setLabelFor(jTextMainClass);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelMainClass, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_MainClass_JLabel")); // NOI18N

        javax.swing.GroupLayout clientInfoPanelLayout = new javax.swing.GroupLayout(clientInfoPanel);
        clientInfoPanel.setLayout(clientInfoPanelLayout);
        clientInfoPanelLayout.setHorizontalGroup(
            clientInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(clientInfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(clientInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabelMainClass)
                    .addComponent(jLabelArgs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelVMOptions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(clientInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelVMOptionsExample)
                    .addComponent(jTextVMOptions, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)
                    .addComponent(jTextArgs, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)
                    .addComponent(jTextMainClass, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE))
                .addContainerGap())
        );
        clientInfoPanelLayout.setVerticalGroup(
            clientInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(clientInfoPanelLayout.createSequentialGroup()
                .addGroup(clientInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelMainClass)
                    .addComponent(jTextMainClass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(clientInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextArgs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelArgs))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(clientInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextVMOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelVMOptions))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabelVMOptionsExample))
        );

        jLabelVersion.setLabelFor(jTextFieldVersion);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelVersion, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Version_JLabel")); // NOI18N

        jTextFieldVersion.setEditable(false);
        jTextFieldVersion.setColumns(getLongestVersionLength());

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxDeployOnSave, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_DeployOnSave_JCheckBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(dosDescription, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_DeployOnSave_Description")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(filler)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabelVersion)
                                    .addComponent(jLabelContextPath)
                                    .addComponent(jLabelServer))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(clientModuleUriCombo, 0, 533, Short.MAX_VALUE)
                                    .addComponent(jComboBoxServer, 0, 533, Short.MAX_VALUE)
                                    .addComponent(jTextFieldVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(dosDescription, javax.swing.GroupLayout.DEFAULT_SIZE, 642, Short.MAX_VALUE))
                            .addComponent(jCheckBoxDeployOnSave)
                            .addComponent(webInfoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(clientInfoPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(filler)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabelContextPath)
                        .addComponent(clientModuleUriCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelServer)
                    .addComponent(jComboBoxServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelVersion)
                    .addComponent(jTextFieldVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7)
                .addComponent(jCheckBoxDeployOnSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dosDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(webInfoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clientInfoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jComboBoxServer.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "ACS_CustomizeRun_Server_A11YDesc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void jCheckBoxDisplayBrowserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDisplayBrowserActionPerformed
    handleWebModuleRelated();//GEN-LAST:event_jCheckBoxDisplayBrowserActionPerformed
}                                                       
    
    private void jComboBoxServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxServerActionPerformed
//        if (jComboBoxServer.getSelectedIndex() == -1 || !initialized)//GEN-LAST:event_jComboBoxServerActionPerformed
//            return;
//        String newCtxPath = null ; // wm.getContextPath(serverInstanceIDs [jComboBoxServer.getSelectedIndex ()]);
        setDeployOnSaveState();
    }                                               
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel clientInfoPanel;
    private javax.swing.JComboBox clientModuleUriCombo;
    private javax.swing.JLabel dosDescription;
    private javax.swing.JLabel filler;
    private javax.swing.JComboBox jBrowserComboBox;
    private javax.swing.JLabel jBrowserLabel;
    private javax.swing.JCheckBox jCheckBoxDeployOnSave;
    private javax.swing.JCheckBox jCheckBoxDisplayBrowser;
    private javax.swing.JComboBox jComboBoxServer;
    private javax.swing.JLabel jLabelArgs;
    private javax.swing.JLabel jLabelContextPath;
    private javax.swing.JLabel jLabelContextPathDesc;
    private javax.swing.JLabel jLabelMainClass;
    private javax.swing.JLabel jLabelRelativeURL;
    private javax.swing.JLabel jLabelServer;
    private javax.swing.JLabel jLabelVMOptions;
    private javax.swing.JLabel jLabelVMOptionsExample;
    private javax.swing.JLabel jLabelVersion;
    private javax.swing.JTextField jTextArgs;
    private javax.swing.JTextField jTextFieldRelativeURL;
    private javax.swing.JTextField jTextFieldVersion;
    private javax.swing.JTextField jTextMainClass;
    private javax.swing.JTextField jTextVMOptions;
    private javax.swing.JPanel webInfoPanel;
    // End of variables declaration//GEN-END:variables
    
    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerRun.class);
    }

    private void setDeployOnSaveState() {
        if (uiProperties.J2EE_SERVER_INSTANCE_MODEL.getSelectedItem() != null) {
            String serverInstanceID = J2eePlatformUiSupport.getServerInstanceID(
                    uiProperties.J2EE_SERVER_INSTANCE_MODEL.getSelectedItem());

            J2eeModule module = uiProperties.getProject().getAppModule().getJ2eeModule();
            ServerInstance instance = Deployment.getDefault().getServerInstance(serverInstanceID);

            try {
                jCheckBoxDeployOnSave.setEnabled(instance.isDeployOnSaveSupported(module));
            } catch (InstanceRemovedException ex) {
                jCheckBoxDeployOnSave.setEnabled(false);
            }
        } else {
            jCheckBoxDeployOnSave.setEnabled(false);
        }
    }

    public static ApplicationUrisComboBoxModel createApplicationUrisComboBoxModel(EarProject project) {
        return new ApplicationUrisComboBoxModel(project);
    }
    
    public static final class ApplicationUrisComboBoxModel extends AbstractListModel implements ComboBoxModel {
        
        private List<ClientModuleItem> values;
        private EarProject project;
        private ClientModuleItem selected;
        
        public ApplicationUrisComboBoxModel(EarProject project) {
            this.project = project;
            initValues(EarProjectProperties.getJarContentAdditional(project));
        }
        
        public Object getElementAt(int index) {
            return values.get(index);
        }

        private boolean setSelectedItem(String clientModuleURI, String appClient) {
            if (clientModuleURI != null && values.contains(new ClientModuleItem(clientModuleURI, false))) {
                setSelectedItem(new ClientModuleItem(clientModuleURI, false));
                return true;
            } else if (appClient != null && values.contains(new ClientModuleItem(appClient, true))) {
                setSelectedItem(new ClientModuleItem(appClient, true));
                return true;
            }
            return false;
        }
        
        public void storeSelectedItem(EditableProperties ep) {
            ClientModuleItem sel = (ClientModuleItem)getSelectedItem();
            if (sel == null) {
                ep.remove(EarProjectProperties.APPLICATION_CLIENT);
                ep.remove(EarProjectProperties.CLIENT_MODULE_URI);
            } else if (sel.isAppClient()) {
                ep.setProperty(EarProjectProperties.APPLICATION_CLIENT, sel.getUri());
                ep.setProperty(EarProjectProperties.CLIENT_MODULE_URI, getClientModuleUriForAppClient(project));
            } else {
                ep.remove(EarProjectProperties.APPLICATION_CLIENT);
                ep.setProperty(EarProjectProperties.CLIENT_MODULE_URI, sel.getUri());
            }
        }
        
        public int getSize() {
            return values.size();
        }
        
        public Object getSelectedItem() {
            return selected;
        }
        
        public void setSelectedItem(Object obj) {
            selected = (ClientModuleItem)obj;
        }
        
        void refresh(List<ClassPathSupport.Item> list) {
            initValues(list);
            fireContentsChanged(this, 0, values.size());
        }
        
        private void initValues(List<ClassPathSupport.Item> list) {
            Set<ClientModuleItem> items = new TreeSet<ClientModuleItem>();
            for (Project p : EarProjectProperties.getApplicationSubprojects(list, J2eeModule.Type.WAR)) {
                items.add(new ClientModuleItem(ProjectUtils.getInformation(p).getName(), false));
            }
            for (Project p : EarProjectProperties.getApplicationSubprojects(list, J2eeModule.Type.CAR)) {
                items.add(new ClientModuleItem(ProjectUtils.getInformation(p).getName(), true));
            }
            values = new ArrayList<ClientModuleItem>(items);
            EditableProperties ep = project.getAntProjectHelper().getProperties(
                    AntProjectHelper.PROJECT_PROPERTIES_PATH);
            String clientModuleURI = ep.getProperty(EarProjectProperties.CLIENT_MODULE_URI);
            String appClient = ep.getProperty(EarProjectProperties.APPLICATION_CLIENT);
            if (!setSelectedItem(clientModuleURI, appClient)) {
                setSelectedItem(values.size() > 0 ? values.get(0) : null);
            }
        }

        private static String getClientModuleUriForAppClient(EarProject project) {
            String name = project.getAntProjectHelper().getProperties(
                    AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(EarProjectProperties.JAR_NAME);
            if (name.endsWith(".ear")) { // NOI18N
                name = name.substring(0, name.length() - 4);
            }
            return name + "/${" + EarProjectProperties.APPLICATION_CLIENT + '}'; // NOI18N
        }
        
        public static void moduleWasRemove(Project removedProject, EditableProperties props) {
            String name = ProjectUtils.getInformation(removedProject).getName();
            if (name.equals(props.getProperty(EarProjectProperties.APPLICATION_CLIENT)) ||
                name.equals(props.getProperty(EarProjectProperties.CLIENT_MODULE_URI))) {
                props.remove(EarProjectProperties.APPLICATION_CLIENT);
                props.remove(EarProjectProperties.CLIENT_MODULE_URI);
            }
        }
        
        public static void initializeProperties(final EarProject project, final String warName, final String carName) {
            ProjectManager.mutex().writeAccess(new Runnable() {
                public void run() {
                    try {
                        EditableProperties ep = project.getUpdateHelper().getProperties(
                                AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        if (warName != null) {
                            ep.remove(EarProjectProperties.APPLICATION_CLIENT);
                            ep.setProperty(EarProjectProperties.CLIENT_MODULE_URI, warName);
                        } else if (carName != null) {
                            ep.setProperty(EarProjectProperties.APPLICATION_CLIENT, carName);
                            ep.setProperty(EarProjectProperties.CLIENT_MODULE_URI, getClientModuleUriForAppClient(project));
                        }
                        project.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                        ProjectManager.getDefault().saveProject(project);
                    } catch (IOException e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            });
        }
        
     }
    
    public static final class ClientModuleItem implements Comparable {
        private String uri;
        private boolean appClient;

        public ClientModuleItem(String uri, boolean appClient) {
            assert uri != null;
            this.uri = uri;
            this.appClient = appClient;
        }

        public String getUri() {
            return uri;
        }

        public boolean isAppClient() {
            return appClient;
        }
        
        @Override
        public String toString() {
            return uri;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof ClientModuleItem)) {
                return false;
            }
            return uri.equals(((ClientModuleItem)obj).uri);
        }

        @Override
        public int hashCode() {
            return uri.hashCode();
        }

        public int compareTo(Object obj) {
            if (obj == null || !(obj instanceof ClientModuleItem)) {
                return -1;
            }
            return uri.compareTo(((ClientModuleItem)obj).uri);
        }

    }
}
