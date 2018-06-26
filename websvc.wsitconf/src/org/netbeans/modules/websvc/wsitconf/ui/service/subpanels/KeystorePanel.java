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
import org.netbeans.modules.websvc.wsitconf.util.ServerUtils;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;

/**
 *
 * @author Martin Grebac
 */
public class KeystorePanel extends JPanel {

    public static final String PKCS12 = "PKCS12";      //NOI18N
    public static final String JKS = "JKS";            //NOI18N

    public static final String DEFAULT_PASSWORD="changeit";    //NOI18N
    public static final String DEFAULT_PASSWORD2="adminadmin";    //NOI18N
    
    private WSDLComponent comp;

    private boolean jsr109 = false;
    private Project project = null;
    
    private boolean client;
    private String keystoreType = JKS;
    
    private boolean inSync = false;
    
    private ConfigVersion cfgVersion = null;
    
    public KeystorePanel(WSDLComponent comp, Project p, boolean jsr109, boolean client, ConfigVersion cfgVersion) {
        super();
        this.comp = comp;
        this.jsr109 = jsr109;
        this.project = p;
        this.client = client;
        this.cfgVersion = cfgVersion;
        
        initComponents();

        /* issue 232988: the background color issues with dark metal L&F
        keyAliasCombo.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        keyAliasLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        keyPasswordLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        keyPasswordField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        keystoreLocationLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        keystoreLocationTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        keystorePasswordLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        keystorePasswordField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        aliasSelectorLbl.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        aliasSelectorField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        */

        sync();
    }

    private String getKeystoreAlias() {
        return (String) this.keyAliasCombo.getSelectedItem();
    }

    private void setKeystoreAlias(String alias) {
        this.keyAliasCombo.setSelectedItem(alias);
    }

    private String getKeyPassword() {
        return String.valueOf(this.keyPasswordField.getPassword());
    }

    private void setKeyPassword(String password) {
        this.keyPasswordField.setText(password);
    }

    private String getSelector() {
        String path = this.aliasSelectorField.getText();
        if ("".equals(path) || (path == null)) {    //NOI18N
            return null;
        }
        return path;
    }

    private void setSelector(String selector) {
        this.aliasSelectorField.setText(selector);
    }
    
    private char[] getCharKeystorePassword() {
        return keystorePasswordField.getPassword();
    }
    
    private String getKeystorePassword() {
        return String.valueOf(this.keystorePasswordField.getPassword());
    }

    private void setKeystorePassword(String password) {
        this.keystorePasswordField.setText(password);
    }

    private void setKeystorePath(String path) {
        this.keystoreLocationTextField.setText(path);
    }
    
    private String getKeystorePath() {
        String path = this.keystoreLocationTextField.getText();
        if ("".equals(path) || (path == null)) {    //NOI18N
            return null;
        }
        return path;
    }

    private void setKeystoreType(String type) {
        this.keystoreType = type;
    }
    
    String keyStoreLocation = null;
    String ksType = null;
    String keyStorePassword = null;
    String keyStoreAlias = null;
    String keyPassword = null;
    String aliasSelector = null;
    
    public void sync() {
        inSync = true;

        keyStoreLocation = ProprietarySecurityPolicyModelHelper.getStoreLocation(comp, false);
        if (keyStoreLocation != null) {
            setKeystorePath(keyStoreLocation);
        } else if (jsr109) {
            setKeystorePath(ServerUtils.getStoreLocation(project, false, client));
        }

        ksType = ProprietarySecurityPolicyModelHelper.getStoreType(comp, false);
        if (ksType != null) {
            setKeystoreType(ksType);
        }
        
        keyStorePassword = ProprietarySecurityPolicyModelHelper.getStorePassword(comp, false);
        if (keyStorePassword != null) {
            setKeystorePassword(keyStorePassword);
            reloadAliases();
        } else if (jsr109) {
            setKeystorePassword(keyStorePassword = DEFAULT_PASSWORD);
            if (!reloadAliases()) {
                String adminPassword = Util.getPassword(project);
                setKeystorePassword(keyStorePassword = adminPassword);
            }
            if (!reloadAliases()) {
                setKeystorePassword(keyStorePassword = "");
            }
        }

        keyStoreAlias = ProprietarySecurityPolicyModelHelper.getStoreAlias(comp, false);
        setKeystoreAlias(keyStoreAlias);

        keyPassword = ProprietarySecurityPolicyModelHelper.getKeyPassword(comp);
        if (keyPassword != null) {
            setKeyPassword(keyPassword);
        }

        aliasSelector = ProprietarySecurityPolicyModelHelper.getAliasSelector(comp);
        if (aliasSelector != null) {
            setSelector(aliasSelector);
        }
        
        enableDisable();

        inSync = false;
    }

    private void enableDisable() {
//        boolean gf = Util.isGlassfish(project);
//        keyPasswordField.setEnabled(!gf);
//        keyPasswordLabel.setEnabled(!gf);
    }
        
    public void storeState() {
        String keystoreAlias = getKeystoreAlias();        
        if ((keystoreAlias == null) || (keystoreAlias.length() == 0)) {
            ProprietarySecurityPolicyModelHelper.setKeyStoreAlias(comp, null, client);
        } else {
            ProprietarySecurityPolicyModelHelper.setKeyStoreAlias(comp, keystoreAlias, client);
        }
        // do not store anything else if GF is target server
        String keyPasswd = getKeyPassword();
        String keyStorePasswd = getKeystorePassword();
        String keyStoreLoc = getKeystorePath();

//        if (!Util.isGlassfish(project) || 
//            ((keyPasswd != null) && (!keyPasswd.equals(this.keyPassword))) || 
//            ((keyStorePasswd != null) && (!keyStorePasswd.equals(this.keyStorePassword))) || 
//            ((keyStoreLoc != null) && (!keyStoreLoc.equals(this.keyStoreLocation)))
//            ) {
                if ((keyPasswd == null) || (keyPasswd.length() == 0)) {
                    ProprietarySecurityPolicyModelHelper.setKeyPassword(comp, null, client);
                } else {
                    ProprietarySecurityPolicyModelHelper.setKeyPassword(comp, keyPasswd, client);
                }
                if ((keyStorePasswd == null) || (keyStorePasswd.length() == 0)) {
                    ProprietarySecurityPolicyModelHelper.setStorePassword(comp, null, false, client);
                } else {
                    ProprietarySecurityPolicyModelHelper.setStorePassword(comp, keyStorePasswd, false, client);
                }
                ProprietarySecurityPolicyModelHelper.setStoreType(comp, keystoreType, false, client);
                ProprietarySecurityPolicyModelHelper.setStoreLocation(comp, keyStoreLoc, false, client);
//        }

        String selector = getSelector();
        // IZ#129480, failed to clear selector textfield
        //if (selector != null) {
            ProprietarySecurityPolicyModelHelper.setAliasSelector(comp, selector, false);
        //}
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        keystoreLocationLabel = new javax.swing.JLabel();
        keystorePasswordLabel = new javax.swing.JLabel();
        keystoreLocationTextField = new javax.swing.JTextField();
        keystoreLocationButton = new javax.swing.JButton();
        keyAliasLabel = new javax.swing.JLabel();
        keyPasswordLabel = new javax.swing.JLabel();
        keyAliasCombo = new javax.swing.JComboBox();
        keystorePasswordField = new javax.swing.JPasswordField();
        keyPasswordField = new javax.swing.JPasswordField();
        loadkeysButton = new javax.swing.JButton();
        aliasSelectorLbl = new javax.swing.JLabel();
        aliasSelectorField = new javax.swing.JTextField();
        aliasSelectorButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(keystoreLocationLabel, org.openide.util.NbBundle.getMessage(KeystorePanel.class, "LBL_KeyStorePanel_LocationLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(keystorePasswordLabel, org.openide.util.NbBundle.getMessage(KeystorePanel.class, "LBL_StorePanel_StorePassword")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(keystoreLocationButton, org.openide.util.NbBundle.getMessage(KeystorePanel.class, "LBL_KeystorePanel_Browse")); // NOI18N
        keystoreLocationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keystoreLocationButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(keyAliasLabel, org.openide.util.NbBundle.getMessage(KeystorePanel.class, "LBL_KeyStorePanel_KeyAliasLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(keyPasswordLabel, org.openide.util.NbBundle.getMessage(KeystorePanel.class, "LBL_Keystore_KeyPasswordLabel")); // NOI18N

        keyAliasCombo.setEditable(true);

        org.openide.awt.Mnemonics.setLocalizedText(loadkeysButton, org.openide.util.NbBundle.getMessage(KeystorePanel.class, "LBL_LoadKeys")); // NOI18N
        loadkeysButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadkeysButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(aliasSelectorLbl, org.openide.util.NbBundle.getMessage(KeystorePanel.class, "LBL_Keystore_KeySelectorLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(aliasSelectorButton, org.openide.util.NbBundle.getMessage(KeystorePanel.class, "LBL_KeystorePanel_Browse")); // NOI18N
        aliasSelectorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aliasSelectorButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(keystoreLocationLabel)
                    .addComponent(keystorePasswordLabel)
                    .addComponent(keyAliasLabel)
                    .addComponent(keyPasswordLabel)
                    .addComponent(aliasSelectorLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(keystoreLocationTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(keystoreLocationButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(keyAliasCombo, javax.swing.GroupLayout.Alignment.LEADING, 0, 169, Short.MAX_VALUE)
                            .addComponent(keystorePasswordField, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                            .addComponent(keyPasswordField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(loadkeysButton)
                        .addGap(68, 68, 68))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(aliasSelectorField, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(aliasSelectorButton)))
                .addGap(4, 4, 4))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keystoreLocationLabel)
                    .addComponent(keystoreLocationButton)
                    .addComponent(keystoreLocationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keystorePasswordLabel)
                    .addComponent(keystorePasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keyAliasLabel)
                    .addComponent(keyAliasCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(loadkeysButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keyPasswordLabel)
                    .addComponent(keyPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aliasSelectorLbl)
                    .addComponent(aliasSelectorField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(aliasSelectorButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {keyAliasCombo, keyPasswordField, keystoreLocationTextField, keystorePasswordField});

    }// </editor-fold>//GEN-END:initComponents

    private void loadkeysButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadkeysButtonActionPerformed
        boolean success = reloadAliases();
        if (!success) {
            DialogDisplayer.getDefault().notifyLater(
                new NotifyDescriptor.Message(NbBundle.getMessage(KeystorePanel.class, "MSG_WrongPassword"   //NOI18N
                )));
        }
    }//GEN-LAST:event_loadkeysButtonActionPerformed
    
    private void keystoreLocationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keystoreLocationButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setDialogTitle(NbBundle.getMessage(KeystorePanel.class, "LBL_KeystoreBrowse_Title")); //NOI18N
        chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new StoreFileFilter());
        File f = new File(keystoreLocationTextField.getText());
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
                setKeystorePath(file.getPath());
                String extension = FileUtil.getExtension(file.getName());
                keystoreType = StoreFileFilter.JKS_EXT.equals(extension) ? JKS : PKCS12;
            }
        }
    }//GEN-LAST:event_keystoreLocationButtonActionPerformed

    private void aliasSelectorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aliasSelectorButtonActionPerformed
        if (project != null) {
            ClassDialog classDialog = new ClassDialog(project, "com.sun.xml.wss.AliasSelector"); //NOI18N
            classDialog.show();
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    setSelector(selectedClass);
                    ProprietarySecurityPolicyModelHelper.setAliasSelector(comp, selectedClass, false);          
                    break;
                }
            }
        }        
}//GEN-LAST:event_aliasSelectorButtonActionPerformed

    private boolean reloadAliases() {
        List<String> aliasList;
        try {
            aliasList = Util.getAliases(getKeystorePath(), getCharKeystorePassword(), keystoreType);
        } catch (IOException ex) {
            return false;
        }
        keyAliasCombo.removeAllItems();
        if (aliasList != null) {
            keyAliasCombo.addItem("");  //NOI18N
            Iterator<String> aliases = aliasList.iterator();
            while (aliases.hasNext()) {
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
    private javax.swing.JButton aliasSelectorButton;
    private javax.swing.JTextField aliasSelectorField;
    private javax.swing.JLabel aliasSelectorLbl;
    private javax.swing.JComboBox keyAliasCombo;
    private javax.swing.JLabel keyAliasLabel;
    private javax.swing.JPasswordField keyPasswordField;
    private javax.swing.JLabel keyPasswordLabel;
    private javax.swing.JButton keystoreLocationButton;
    private javax.swing.JLabel keystoreLocationLabel;
    private javax.swing.JTextField keystoreLocationTextField;
    private javax.swing.JPasswordField keystorePasswordField;
    private javax.swing.JLabel keystorePasswordLabel;
    private javax.swing.JButton loadkeysButton;
    // End of variables declaration//GEN-END:variables
    
}
