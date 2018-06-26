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

package org.netbeans.modules.j2ee.clientproject.ui.customizer;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.clientproject.AppClientProject;
import org.netbeans.modules.j2ee.common.SharabilityUtility;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.javaee.project.api.ui.utils.J2eePlatformUiSupport;
import org.netbeans.modules.javaee.project.api.ui.utils.MessageUtils;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.MouseUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  phrebejk
 */
public class CustomizerRun extends JPanel implements HelpCtx.Provider {
    
    private final AppClientProjectProperties uiProperties;
    
    private final String oldServerInstanceId;
    
    public CustomizerRun( AppClientProjectProperties uiProperties ) {
        initComponents();
        
        this.uiProperties = uiProperties;
        
        this.oldServerInstanceId = uiProperties.J2EE_SERVER_INSTANCE_MODEL.getSelectedItem() != null
                ? J2eePlatformUiSupport.getServerInstanceID(uiProperties.J2EE_SERVER_INSTANCE_MODEL.getSelectedItem())
                : null;
        
        uiProperties.JAVAC_CLASSPATH_MODEL.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                setMessages();
            }
        });
        
        jTextFieldMainClass.setDocument( uiProperties.MAIN_CLASS_MODEL );
        jTextFieldArgs.setDocument( uiProperties.APPLICATION_ARGS_MODEL );
        jTextVMOptions.setDocument( uiProperties.RUN_JVM_ARGS_MODEL );
        jTextWorkingDirectory.setDocument( uiProperties.RUN_WORK_DIR_MODEL );
        
        jButtonMainClass.addActionListener( new MainClassListener( uiProperties.getProject().getSourceRoots(), jTextFieldMainClass ) );
        
        jComboBoxJ2eeSpecVersion.setModel (uiProperties.J2EE_PLATFORM_MODEL );
        jComboBoxServer.setModel (uiProperties.J2EE_SERVER_INSTANCE_MODEL );
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx( CustomizerRun.class );
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelMainClass = new javax.swing.JLabel();
        jTextFieldMainClass = new javax.swing.JTextField();
        jButtonMainClass = new javax.swing.JButton();
        jLabelArgs = new javax.swing.JLabel();
        jTextFieldArgs = new javax.swing.JTextField();
        jLabelWorkingDirectory = new javax.swing.JLabel();
        jTextWorkingDirectory = new javax.swing.JTextField();
        jButtonWorkingDirectoryBrowse = new javax.swing.JButton();
        jLabelVMOptions = new javax.swing.JLabel();
        jTextVMOptions = new javax.swing.JTextField();
        jLabelVMOptionsExample = new javax.swing.JLabel();
        jLabelServer = new javax.swing.JLabel();
        jComboBoxServer = new javax.swing.JComboBox();
        jLabelJ2EE = new javax.swing.JLabel();
        jComboBoxJ2eeSpecVersion = new javax.swing.JComboBox();
        errorLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jLabelMainClass.setLabelFor(jTextFieldMainClass);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelMainClass, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_MainClass_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jLabelMainClass, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(jTextFieldMainClass, gridBagConstraints);
        jTextFieldMainClass.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AD_jTextFieldMainClass")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMainClass, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_MainClass_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        add(jButtonMainClass, gridBagConstraints);
        jButtonMainClass.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(CustomizerRun.class).getString("AD_jButtonMainClass")); // NOI18N

        jLabelArgs.setLabelFor(jTextFieldArgs);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelArgs, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_Args_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabelArgs, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(jTextFieldArgs, gridBagConstraints);
        jTextFieldArgs.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(CustomizerRun.class).getString("AD_jTextFieldArgs")); // NOI18N

        jLabelWorkingDirectory.setLabelFor(jTextWorkingDirectory);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelWorkingDirectory, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_Working_Directory")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jLabelWorkingDirectory, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(jTextWorkingDirectory, gridBagConstraints);
        jTextWorkingDirectory.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AD_CustomizeRun_Run_Working_Directory")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonWorkingDirectoryBrowse, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_Working_Directory_Browse")); // NOI18N
        jButtonWorkingDirectoryBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonWorkingDirectoryBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        add(jButtonWorkingDirectoryBrowse, gridBagConstraints);
        jButtonWorkingDirectoryBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AD_CustomizeRun_Run_Working_Directory_Browse")); // NOI18N

        jLabelVMOptions.setLabelFor(jTextVMOptions);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelVMOptions, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_VM_Options")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jLabelVMOptions, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(jTextVMOptions, gridBagConstraints);
        jTextVMOptions.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AD_CustomizeRun_Run_VM_Options")); // NOI18N

        jLabelVMOptionsExample.setLabelFor(jTextFieldMainClass);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelVMOptionsExample, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_VM_Options_Example")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(jLabelVMOptionsExample, gridBagConstraints);
        jLabelVMOptionsExample.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_VM_Options_Example")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabelServer, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Server_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jLabelServer, gridBagConstraints);

        jComboBoxServer.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxServerItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(jComboBoxServer, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabelJ2EE, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_J2EE_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabelJ2EE, gridBagConstraints);

        jComboBoxJ2eeSpecVersion.setEnabled(false);
        jComboBoxJ2eeSpecVersion.setPrototypeDisplayValue("MMMMMMMMM" /* "Java EE 5" */);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(jComboBoxJ2eeSpecVersion, gridBagConstraints);

        errorLabel.setForeground(new java.awt.Color(89, 71, 191));
        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, " ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        add(errorLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
            
    private void jButtonWorkingDirectoryBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonWorkingDirectoryBrowseActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        
        String workDir = jTextWorkingDirectory.getText();
        if (workDir.equals("")) {
            workDir = FileUtil.toFile(uiProperties.getProject().getProjectDirectory()).getAbsolutePath();
        }
        chooser.setSelectedFile(new File(workDir));
        chooser.setDialogTitle(NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_Working_Directory_Browse_Title"));
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) { //NOI18N
            File file = FileUtil.normalizeFile(chooser.getSelectedFile());
            jTextWorkingDirectory.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonWorkingDirectoryBrowseActionPerformed

private void jComboBoxServerItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxServerItemStateChanged
        // TODO add your handling code here:
        setMessages();
}//GEN-LAST:event_jComboBoxServerItemStateChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel errorLabel;
    private javax.swing.JButton jButtonMainClass;
    private javax.swing.JButton jButtonWorkingDirectoryBrowse;
    private javax.swing.JComboBox jComboBoxJ2eeSpecVersion;
    private javax.swing.JComboBox jComboBoxServer;
    private javax.swing.JLabel jLabelArgs;
    private javax.swing.JLabel jLabelJ2EE;
    private javax.swing.JLabel jLabelMainClass;
    private javax.swing.JLabel jLabelServer;
    private javax.swing.JLabel jLabelVMOptions;
    private javax.swing.JLabel jLabelVMOptionsExample;
    private javax.swing.JLabel jLabelWorkingDirectory;
    private javax.swing.JTextField jTextFieldArgs;
    private javax.swing.JTextField jTextFieldMainClass;
    private javax.swing.JTextField jTextVMOptions;
    private javax.swing.JTextField jTextWorkingDirectory;
    // End of variables declaration//GEN-END:variables
    
    private void setMessages() {
        MessageUtils.clear(errorLabel);
    }
    
    // Innercasses -------------------------------------------------------------
    
    private class MainClassListener implements ActionListener /*, DocumentListener */ {
        
        private final JButton okButton;
        private SourceRoots sourceRoots;
        private JTextField mainClassTextField;
        
        MainClassListener( SourceRoots sourceRoots, JTextField mainClassTextField ) {
            this.sourceRoots = sourceRoots;
            this.mainClassTextField = mainClassTextField;
            this.okButton  = new JButton(NbBundle.getMessage(CustomizerRun.class, "LBL_ChooseMainClass_OK"));
            this.okButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerRun.class, "AD_ChooseMainClass_OK"));
        }
        
        // Implementation of ActionListener ------------------------------------
        
        /** Handles button events
         */
        public void actionPerformed( ActionEvent e ) {
            
            // only chooseMainClassButton can be performed
            
            final MainClassChooser panel = new MainClassChooser(sourceRoots.getRoots());
            Object[] options = new Object[] {
                okButton,
                DialogDescriptor.CANCEL_OPTION
            };
            panel.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    if (e.getSource() instanceof MouseEvent && MouseUtils.isDoubleClick(((MouseEvent)e.getSource()))) {
                        // click button and finish the dialog with selected class
                        okButton.doClick();
                    } else {
                        okButton.setEnabled(panel.getSelectedMainClass() != null);
                    }
                }
            });
            okButton.setEnabled(false);
            DialogDescriptor desc = new DialogDescriptor(
                    panel,
                    NbBundle.getMessage(CustomizerRun.class, "LBL_ChooseMainClass_Title" ),
                    true,
                    options,
                    options[0],
                    DialogDescriptor.BOTTOM_ALIGN,
                    null,
                    null);
            //desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
            Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
            dlg.setVisible(true);
            if (desc.getValue() == options[0]) {
                mainClassTextField.setText(panel.getSelectedMainClass());
            }
            dlg.dispose();
        }
        
    }
    
}
