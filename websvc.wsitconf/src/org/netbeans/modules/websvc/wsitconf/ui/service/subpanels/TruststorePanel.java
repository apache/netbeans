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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.netbeans.modules.websvc.wsitconf.ui.service.subpanels;

import java.io.IOException;
import org.netbeans.modules.websvc.wsitconf.ui.StoreFileFilter;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import javax.swing.*;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.ui.ClassDialog;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.util.ServerUtils;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;

/**
 *
 * @author Martin Grebac
 */
public class TruststorePanel extends JPanel {

    private static final String PKCS12 = "PKCS12";  //NOI18N
    private static final String JKS = "JKS";        //NOI18N

    private static final String DEFAULT_PASSWORD="changeit";    //NOI18N

    private WSDLComponent comp;

    private String storeType = JKS;

    private boolean jsr109 = false;
    private Project project = null;
    private String profile = null;
    
    private boolean inSync = false;
    
    private ConfigVersion cfgVersion = null;

    private boolean client;
    
    public TruststorePanel(WSDLComponent comp, Project p, boolean jsr109, String profile, boolean client, ConfigVersion cfgVersion) {
        super();
        this.comp = comp;
        this.jsr109 = jsr109;
        this.project = p;
        this.profile = profile;
        this.client = client;
        this.cfgVersion = cfgVersion;
        
        initComponents();

        /* issue 232988: the background color issues with dark metal L&F
        keyAliasCombo.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        keyAliasLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        storeLocationLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        storeLocationTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        storePasswordLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        storePasswordField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        certSelectorLbl.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        certSelectorButton.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        certSelectorField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        */

        sync();
    }

    private String getPeerAlias() {
        return (String) this.keyAliasCombo.getSelectedItem();
    }

    private void setPeerAlias(String alias) {
        this.keyAliasCombo.setSelectedItem(alias);
    }

    private char[] getCharStorePassword() {
        return storePasswordField.getPassword();
    }
    
    private String getStorePassword() {
        return String.valueOf(this.storePasswordField.getPassword());
    }

    private void setStorePassword(String password) {
        this.storePasswordField.setText(password);
    }

    private void setStoreLocation(String path) {
        this.storeLocationTextField.setText(path);
    }
    
    private String getStoreLocation() {
        String path = this.storeLocationTextField.getText();
        if ("".equals(path) || (path == null)) {    //NOI18N
            return null;
        }
        return path;
    }

    private void setStoreType(String type) {
        this.storeType = type;
    }

    private String getSelector() {
        String path = this.certSelectorField.getText();
        if ("".equals(path) || (path == null)) {    //NOI18N
            return null;
        }
        return path;
    }

    private void setSelector(String selector) {
        this.certSelectorField.setText(selector);
    }
    
    String storeLocation = null;
    String ksType = null;
    String storePassword = null;
    String certSelector = null;
    
    public void sync() {
        inSync = true;
        
        storeLocation = ProprietarySecurityPolicyModelHelper.getStoreLocation(comp, true);
        if (storeLocation != null) {
            setStoreLocation(storeLocation);
        } else if (jsr109) {
            setStoreLocation(ServerUtils.getStoreLocation(project, true, client));
        }

        ksType = ProprietarySecurityPolicyModelHelper.getStoreType(comp, true);
        if (ksType != null) {
            setStoreType(ksType);
        }
       
        storePassword = ProprietarySecurityPolicyModelHelper.getStorePassword(comp, true);
        if (storePassword != null) {
            setStorePassword(storePassword);
            reloadAliases();
        } else if (jsr109) {
            setStorePassword(storePassword = DEFAULT_PASSWORD);
            if (!reloadAliases()) {
                String adminPassword = Util.getPassword(project);
                setStorePassword(storePassword = adminPassword);
            }
            if (!reloadAliases()) {
                setStorePassword(storePassword = "");
            }
        }

        String peerAlias = ProprietarySecurityPolicyModelHelper.getTrustPeerAlias(comp);
        setPeerAlias(peerAlias);

        certSelector = ProprietarySecurityPolicyModelHelper.getCertSelector(comp);
        if (certSelector != null) {
            setSelector(certSelector);
        }
        
        enableDisable();
        
        inSync = false;
    }
    
    private void enableDisable() {
        if (!client) {
            boolean aliasRequired = true;
            if (ComboConstants.PROF_USERNAME.equals(profile) ||
                ComboConstants.PROF_ENDORSCERT.equals(profile) ||
                ComboConstants.PROF_SAMLSENDER.equals(profile) ||
                ComboConstants.PROF_SAMLHOLDER.equals(profile) ||
                ComboConstants.PROF_STSISSUED.equals(profile) ||
                ComboConstants.PROF_STSISSUEDCERT.equals(profile) ||
                ComboConstants.PROF_STSISSUEDENDORSE.equals(profile) ||
                ComboConstants.PROF_STSISSUEDSUPPORTING.equals(profile) ||
                ComboConstants.PROF_MUTUALCERT.equals(profile)) {
                aliasRequired = false;
            }
            keyAliasCombo.setEnabled(aliasRequired);
            keyAliasLabel.setEnabled(aliasRequired);
            loadkeysButton.setEnabled(aliasRequired);
        } else {
            
        }
                
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        storeLocationLabel = new javax.swing.JLabel();
        storePasswordLabel = new javax.swing.JLabel();
        storeLocationTextField = new javax.swing.JTextField();
        storeLocationButton = new javax.swing.JButton();
        keyAliasLabel = new javax.swing.JLabel();
        keyAliasCombo = new javax.swing.JComboBox();
        storePasswordField = new javax.swing.JPasswordField();
        loadkeysButton = new javax.swing.JButton();
        certSelectorLbl = new javax.swing.JLabel();
        certSelectorField = new javax.swing.JTextField();
        certSelectorButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(storeLocationLabel, org.openide.util.NbBundle.getMessage(TruststorePanel.class, "LBL_KeyStorePanel_LocationLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(storePasswordLabel, org.openide.util.NbBundle.getMessage(TruststorePanel.class, "LBL_TruststorePanel_TruststorePassword")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(storeLocationButton, org.openide.util.NbBundle.getMessage(TruststorePanel.class, "LBL_TruststorePanel_Browse")); // NOI18N
        storeLocationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storeLocationButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(keyAliasLabel, org.openide.util.NbBundle.getMessage(TruststorePanel.class, "LBL_KeyStorePanel_KeyAliasLabel")); // NOI18N

        keyAliasCombo.setEditable(true);

        org.openide.awt.Mnemonics.setLocalizedText(loadkeysButton, org.openide.util.NbBundle.getMessage(TruststorePanel.class, "LBL_LoadKeys")); // NOI18N
        loadkeysButton.setActionCommand("&Load Aliases");
        loadkeysButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadkeysButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(certSelectorLbl, org.openide.util.NbBundle.getMessage(TruststorePanel.class, "LBL_Truststore_AliasSelectorLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(certSelectorButton, org.openide.util.NbBundle.getMessage(TruststorePanel.class, "LBL_TruststorePanel_Browse")); // NOI18N
        certSelectorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                certSelectorButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(storeLocationLabel)
                            .addComponent(storePasswordLabel)
                            .addComponent(keyAliasLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(keyAliasCombo, javax.swing.GroupLayout.Alignment.LEADING, 0, 164, Short.MAX_VALUE)
                                    .addComponent(storePasswordField, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(loadkeysButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 84, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(certSelectorField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE)
                                    .addComponent(storeLocationTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(certSelectorLbl)
                        .addGap(384, 384, 384)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(storeLocationButton)
                    .addComponent(certSelectorButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(storeLocationLabel)
                    .addComponent(storeLocationButton)
                    .addComponent(storeLocationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(storePasswordLabel)
                    .addComponent(storePasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keyAliasLabel)
                    .addComponent(keyAliasCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(loadkeysButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(certSelectorLbl)
                    .addComponent(certSelectorField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(certSelectorButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {certSelectorField, keyAliasCombo, storeLocationTextField, storePasswordField});

    }// </editor-fold>//GEN-END:initComponents

    private void loadkeysButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadkeysButtonActionPerformed
        boolean success = reloadAliases();
        if (!success) {
            DialogDisplayer.getDefault().notifyLater(
                    new NotifyDescriptor.Message(NbBundle.getMessage(TruststorePanel.class, "MSG_WrongPassword"   //NOI18N
                    )));
        }
    }//GEN-LAST:event_loadkeysButtonActionPerformed
    
    private void storeLocationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storeLocationButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setDialogTitle(NbBundle.getMessage(TruststorePanel.class, "LBL_TruststoreBrowse_Title")); //NOI18N
        chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new StoreFileFilter());
        File f = new File(storeLocationTextField.getText());
        if ((f != null) && (f.exists())) {
            if (f.isDirectory()) {
                chooser.setCurrentDirectory(f);
            } else {
                chooser.setCurrentDirectory(f.getParentFile());
            }
        }
        if (chooser.showOpenDialog(this)== JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                setStoreLocation(file.getPath());
                String extension = FileUtil.getExtension(file.getName());
                storeType = StoreFileFilter.JKS_EXT.equals(extension) ? JKS : PKCS12;
            }
        }
    }//GEN-LAST:event_storeLocationButtonActionPerformed

    private void certSelectorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_certSelectorButtonActionPerformed
        if (project != null) {
            ClassDialog classDialog = new ClassDialog(project, "java.security.cert.CertSelector"); //NOI18N
            classDialog.show();
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    setSelector(selectedClass);
                    ProprietarySecurityPolicyModelHelper.setCertSelector(comp, selectedClass, client);
                    break;
                }
            }
        }
}//GEN-LAST:event_certSelectorButtonActionPerformed

    public void storeState() {
        String peerAlias = getPeerAlias();
        if ((peerAlias != null) && (peerAlias.length() == 0)) {
            ProprietarySecurityPolicyModelHelper.setTrustPeerAlias(comp, null, client);
        } else {
            ProprietarySecurityPolicyModelHelper.setTrustPeerAlias(comp, peerAlias, client);
        }
        
        String storePasswd = getStorePassword();
        String storeLoc = getStoreLocation();
        
//        if (!Util.isGlassfish(project) ||
//            ((storePasswd != null) && (!storePasswd.equals(this.storePassword))) || 
//            ((storeLoc != null) && (!storeLoc.equals(this.storeLocation)))
//                ) {
            if ((storePasswd != null) && (storePasswd.length() == 0)) {
                ProprietarySecurityPolicyModelHelper.setStorePassword(comp, null, true, client);
            } else {
                ProprietarySecurityPolicyModelHelper.setStorePassword(comp, storePasswd, true, client);
            }

            ProprietarySecurityPolicyModelHelper.setStoreType(comp, storeType, true, client);
            ProprietarySecurityPolicyModelHelper.setStoreLocation(comp, storeLoc, true, client);
//        }
            
        String selector = getSelector();
        // IZ#129480, failed to clear selector textfield
        //if (selector != null) {
            ProprietarySecurityPolicyModelHelper.setCertSelector(comp, selector, client);
        //}
            
    }
    
    private boolean reloadAliases() {
        List<String> aliasList;
        try {
            aliasList = Util.getAliases(getStoreLocation(), getCharStorePassword(), storeType);
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        keyAliasCombo.removeAllItems();
        if (aliasList != null) {
            keyAliasCombo.addItem("");  //NOI18N
            Iterator<String> aliases = aliasList.iterator();
            while (aliases.hasNext()){
                String alias = aliases.next();
                keyAliasCombo.addItem(alias);
            }
            if (keyAliasCombo.getItemCount() > 1) {
                keyAliasCombo.setSelectedIndex(1);
            }
        }
        return true;
    }
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton certSelectorButton;
    private javax.swing.JTextField certSelectorField;
    private javax.swing.JLabel certSelectorLbl;
    private javax.swing.JComboBox keyAliasCombo;
    private javax.swing.JLabel keyAliasLabel;
    private javax.swing.JButton loadkeysButton;
    private javax.swing.JButton storeLocationButton;
    private javax.swing.JLabel storeLocationLabel;
    private javax.swing.JTextField storeLocationTextField;
    private javax.swing.JPasswordField storePasswordField;
    private javax.swing.JLabel storePasswordLabel;
    // End of variables declaration//GEN-END:variables
    
}
