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
package org.netbeans.modules.websvc.saas.ui.wizards;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.SaasGroup;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel.State;
import org.netbeans.modules.websvc.saas.util.WsdlUtil;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Enables searching for Web Services, via an URL, on the local file system
 * or in some uddiRegistry (UDDI)
 * @author Winston Prakash, cao
 */
public class AddWebServiceDlg extends JPanel implements ActionListener {

    public static final String DEFAULT_PACKAGE_HOLDER = NbBundle.getMessage(AddWebServiceDlg.class, "MSG_ClickToOverride"); // NOI18N

    private DialogDescriptor dlg = null;
    private Dialog dialog;
    private static String previousDirectory = null;
    private static JFileChooser wsdlFileChooser;
    private final FileFilter WSDL_FILE_FILTER = new ServiceFileFilter();
    private final SaasGroup group;
    private final boolean jaxRPCAvailable;
    private final String defaultMsg;
    private boolean allControlsDisabled;
    
    private static final String[] KEYWORDS = {
        "abstract", "continue", "for", "new", "switch", // NOI18N
        "assert", "default", "if", "package", "synchronized", // NOI18N
        "boolean", "do", "goto", "private", "this", // NOI18N
        "break", "double", "implements", "protected", "throw", // NOI18N
        "byte", "else", "import", "public", "throws", // NOI18N
        "case", "enum", "instanceof", "return", "transient", // NOI18N
        "catch", "extends", "int", "short", "try", // NOI18N
        "char", "final", "interface", "static", "void", // NOI18N
        "class", "finally", "long", "strictfp", "volatile", // NOI18N
        "const", "float", "native", "super", "while", // NOI18N

        "true", "false", "null" // NOI18N

    };
    private static final Set<String> KEYWORD_SET = new HashSet<String>(KEYWORDS.length * 2);
    

    static {
        KEYWORD_SET.addAll(Arrays.asList(KEYWORDS));
    }

    public AddWebServiceDlg(SaasGroup group) {
        initComponents();
        myInitComponents();
        this.group = group;
        jaxRPCAvailable = WsdlUtil.isJAXRPCAvailable();
        defaultMsg = jaxRPCAvailable ? "" : NbBundle.getMessage(AddWebServiceDlg.class, "WARNING_JAXRPC_UNAVAILABLE");

        checkServicesModel();
    }

    private static boolean isValidPackageName(String packageName) {
        if (packageName == null || packageName.length() == 0) { // let jaxws pick package name

            return true;
        } else if (!Character.isJavaIdentifierStart(packageName.charAt(0))) {
            return false;
        } else {
            java.util.StringTokenizer pkgIds = new java.util.StringTokenizer(packageName, "."); // NOI18N

            while (pkgIds.hasMoreTokens()) {
                String nextIdStr = pkgIds.nextToken();
                if (KEYWORD_SET.contains(nextIdStr)) {
                    return false;
                }

                char[] nextId = nextIdStr.toCharArray();
                if (!Character.isJavaIdentifierStart(nextId[0])) {
                    return false;
                }

                for (int i = 1; i < nextId.length; i++) {
                    if (!Character.isJavaIdentifierPart(nextId[i])) {
                        return false;
                    }
                }
            }

            boolean lastDot = false;
            for (int i = 0; i < packageName.length(); i++) {
                boolean isDot = packageName.charAt(i) == '.';
                if (isDot && lastDot) {
                    return false;
                }
                lastDot = isDot;
            }

            return !packageName.endsWith("."); // NOI18N
        }
    }

    private void setErrorMessage(String msg) {
        if (msg == null || msg.length() == 0) {
            errorLabel.setVisible(false);
            
            if (dlg != null) {
                dlg.setValid(true);
            }
        } else {
            errorLabel.setVisible(true);
            errorLabel.setText(msg);
            
            if (dlg != null) {
                if (msg.equals(defaultMsg)) {
                    dlg.setValid(true);
                } else {
                    dlg.setValid(false);
                }
            }
        }
    }

    private void checkValues() {
        // Check the package name
        final String packageName = jTxtpackageName.getText().trim();
        boolean defaultPackage = DEFAULT_PACKAGE_HOLDER.equals(packageName) || packageName.length() == 0;
        if (!defaultPackage && !isValidPackageName(packageName)) {
            setErrorMessage(NbBundle.getMessage(AddWebServiceDlg.class, "INVALID_PACKAGE"));
        } else if (jTxtLocalFilename.isEnabled()) {
            String localText = jTxtLocalFilename.getText().trim();
            if (localText.length() == 0) {
                setErrorMessage(NbBundle.getMessage(AddWebServiceDlg.class, "EMPTY_FILE"));
                return;
            }

            File f = new File(localText);
            if (!f.exists()) {
                setErrorMessage(NbBundle.getMessage(AddWebServiceDlg.class, "INVALID_FILE_NOT_FOUND"));
            } else if (!f.isFile()) {
                setErrorMessage(NbBundle.getMessage(AddWebServiceDlg.class, "INVALID_FILE_NOT_FILE"));
            } else if (group.serviceExists(localText)) {
                setErrorMessage(NbBundle.getMessage(AddWebServiceDlg.class, "SERVICE_ALREADY_EXISTS_FOR_FILE"));
            } else {
                setErrorMessage(defaultMsg);
            }
        } else if (jTxServiceURL.isEnabled()) {
            String urlText = jTxServiceURL.getText().trim();
            if (urlText.length() == 0) {
                setErrorMessage(NbBundle.getMessage(AddWebServiceDlg.class, "EMPTY_URL"));
                return;
            }

            try {
                URL url = new URL(urlText);

                if (group.serviceExists(urlText)) {
                    setErrorMessage(NbBundle.getMessage(AddWebServiceDlg.class, "SERVICE_ALREADY_EXISTS_FOR_URL"));
                } else {
                    setErrorMessage(defaultMsg);
                }
            } catch (MalformedURLException ex) {
                setErrorMessage(NbBundle.getMessage(AddWebServiceDlg.class, "INVALID_URL"));
            }
        } else {
            setErrorMessage(defaultMsg);
        }
    }

    private void myInitComponents() {
        wsdlFileChooser = new JFileChooser();
        ServiceFileFilter myFilter = new ServiceFileFilter();
        wsdlFileChooser.setFileFilter(myFilter);

        jTxtLocalFilename.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                checkValues();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkValues();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkValues();
            }
        });


        jTxServiceURL.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                checkValues();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkValues();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkValues();
            }
        });
        
         jTxtpackageName.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                checkValues();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkValues();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkValues();
            }
        });

        enableControls();

        setDefaults();

        jTxtpackageName.setText(DEFAULT_PACKAGE_HOLDER);
        jTxtpackageName.setForeground(Color.GRAY);
    }

    public void displayDialog() {

        dlg = new DialogDescriptor(this, NbBundle.getMessage(AddWebServiceDlg.class, "ADD_WEB_SERVICE"),
                true, NotifyDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN, this.getHelpCtx(), this);
 
        dialog = DialogDisplayer.getDefault().createDialog(dlg);
        dlg.setValid(false);
        dialog.setVisible(true);
        
        if (dlg.getValue() == DialogDescriptor.OK_OPTION) {
            createService();
        }
    }

    /** XXX once we implement context sensitive help, change the return */
    public HelpCtx getHelpCtx() {
        return new HelpCtx("projrave_ui_elements_server_nav_add_websvcdb");
    }

    private void setDefaults() {
        jRbnUrl.setSelected(true);
        jRbnFilesystem.setSelected(false);
        enableControls();
    }

    private void enableControls() {
        if (allControlsDisabled) {
            return;
        }

        if (jRbnUrl.isSelected()) {
            jTxServiceURL.setEnabled(true);
            jTxServiceURL.requestFocusInWindow();
            jTxtLocalFilename.setEnabled(false);
            jLblChooseSource.setLabelFor(jTxServiceURL);
        } else if (jRbnFilesystem.isSelected()) {
            jTxtLocalFilename.setEnabled(true);
            jTxtLocalFilename.requestFocusInWindow();
            jTxServiceURL.setEnabled(false);
            jLblChooseSource.setLabelFor(jTxtLocalFilename);
        }
    }

    private void disableAllControls() {
        allControlsDisabled = true;
        jBtnBrowse.setEnabled(false);
        jRbnFilesystem.setEnabled(false);
        jRbnUrl.setEnabled(false);
        jTxServiceURL.setEnabled(false);
        jTxtLocalFilename.setEnabled(false);
        jTxtpackageName.setEnabled(false);
        pkgNameLbl.setEnabled(false);
    }

    private void enableAllControls() {
        allControlsDisabled = false;
        jBtnBrowse.setEnabled(true);
        jRbnFilesystem.setEnabled(true);
        jRbnUrl.setEnabled(true);
        jTxServiceURL.setEnabled(true);
        jTxtLocalFilename.setEnabled(true);
        jTxtpackageName.setEnabled(true);
        pkgNameLbl.setEnabled(true);
    }

    private String fixFileURL(String inFileURL) {
        String returnFileURL = inFileURL;

        try {
            File f = new File(returnFileURL);
            return f.toURI().toURL().toString();
        } catch (Exception ex) {
            if (returnFileURL.substring(0, 1).equalsIgnoreCase("/")) {
                returnFileURL = "file://" + returnFileURL;
            } else {
                returnFileURL = "file:///" + returnFileURL;
            }
        }
        return returnFileURL;
    }

    /**
     * This represents the event on the "Add" button
     */
    private void createService() {
        if ((jTxServiceURL.getText() == null) && (jTxtLocalFilename.getText() == null)) {
            return;
        }
        final String url;
        if (jRbnUrl.isSelected()) {
            url = jTxServiceURL.getText().trim();
        } else {
            url = fixFileURL(jTxtLocalFilename.getText().trim());
        }
        String packageName = jTxtpackageName.getText().trim();
        if (packageName.equals(NbBundle.getMessage(AddWebServiceDlg.class, "MSG_ClickToOverride"))) {
            packageName = ""; //NOI18N

        }

        dialog.setVisible(false);
        dialog.dispose();
        dialog = null;

        try {
            SaasServicesModel.getInstance().createSaasService(group, url, packageName);
        } catch (Exception ex) {
             NotifyDescriptor.Message msg = new NotifyDescriptor.Message(ex.getMessage());
                    DialogDisplayer.getDefault().notify(msg);
        }   
    }

    private void checkServicesModel() {
        if (SaasServicesModel.getInstance().getState() != State.READY) {
            setErrorMessage(NbBundle.getMessage(AddWebServiceDlg.class, "INIT_WEB_SERVICES_MANAGER"));
            disableAllControls();
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    SaasServicesModel.getInstance().initRootGroup();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            enableAllControls();
                            enableControls();
                            checkValues();
                        }
                    });
                }
            });
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLblChooseSource = new javax.swing.JLabel();
        jRbnFilesystem = new javax.swing.JRadioButton();
        jTxtLocalFilename = new javax.swing.JTextField();
        jBtnBrowse = new javax.swing.JButton();
        jRbnUrl = new javax.swing.JRadioButton();
        jTxServiceURL = new javax.swing.JTextField();
        pkgNameLbl = new javax.swing.JLabel();
        jTxtpackageName = new javax.swing.JTextField();
        errorLabel = new javax.swing.JLabel();
        errorLabel.setVisible(false);

        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                formAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        jLblChooseSource.setLabelFor(jTxServiceURL);
        org.openide.awt.Mnemonics.setLocalizedText(jLblChooseSource, NbBundle.getMessage(AddWebServiceDlg.class, "LBL_WsdlSource")); // NOI18N

        buttonGroup1.add(jRbnFilesystem);
        org.openide.awt.Mnemonics.setLocalizedText(jRbnFilesystem, org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "LBL_WsdlSourceFilesystem")); // NOI18N
        jRbnFilesystem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRbnFilesystemActionPerformed(evt);
            }
        });

        jTxtLocalFilename.setColumns(20);

        org.openide.awt.Mnemonics.setLocalizedText(jBtnBrowse, org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "LBL_Browse")); // NOI18N
        jBtnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnBrowseActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRbnUrl);
        jRbnUrl.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jRbnUrl, org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "LBL_WsdlUrl")); // NOI18N
        jRbnUrl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRbnUrlActionPerformed(evt);
            }
        });

        jTxServiceURL.setColumns(20);

        pkgNameLbl.setLabelFor(jTxtpackageName);
        org.openide.awt.Mnemonics.setLocalizedText(pkgNameLbl, org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "PACKAGE_LABEL")); // NOI18N

        jTxtpackageName.setColumns(20);
        jTxtpackageName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTxtpackageNameMouseClicked(evt);
            }
        });

        errorLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pkgNameLbl)
                            .addComponent(jRbnFilesystem)
                            .addComponent(jRbnUrl))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTxServiceURL, javax.swing.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                                    .addComponent(jTxtLocalFilename, javax.swing.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jBtnBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(14, 14, 14))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jTxtpackageName, javax.swing.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
                                .addGap(136, 136, 136))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLblChooseSource)
                            .addComponent(errorLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 796, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLblChooseSource, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jBtnBrowse)
                        .addComponent(jTxtLocalFilename, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jRbnFilesystem))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRbnUrl)
                    .addComponent(jTxServiceURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pkgNameLbl)
                    .addComponent(jTxtpackageName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(errorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLblChooseSource.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "LBL_WsdlSource")); // NOI18N
        jRbnFilesystem.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.localFilelRadioButton.ACC_desc"));
        jTxtLocalFilename.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.localFileComboBox.ACC_name"));
        jTxtLocalFilename.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.localFileComboBox.ACC_desc"));
        jBtnBrowse.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.localFileButton.ACC_desc"));
        jRbnUrl.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.urlRadioButton.ACC_desc"));
        jTxServiceURL.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.urlComboBox.ACC_name"));
        jTxServiceURL.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.urlComboBox.ACC_desc"));
        jTxtpackageName.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.packageTextField.ACC_name"));
        jTxtpackageName.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.packageTextField.ACC_desc"));
        errorLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.errorLabel.ACC_name"));

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.main.ACC_name")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.main.ACC_desc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void jRbnUrlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRbnUrlActionPerformed
    // TODO add your handling code here:
    enableControls();

}//GEN-LAST:event_jRbnUrlActionPerformed

private void jBtnBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnBrowseActionPerformed

    jRbnFilesystem.setSelected(false);
    jRbnFilesystem.setSelected(true);
    enableControls();

    JFileChooser chooser = new JFileChooser(previousDirectory);
    chooser.setMultiSelectionEnabled(false);
    chooser.setAcceptAllFileFilterUsed(false);
    chooser.addChoosableFileFilter(WSDL_FILE_FILTER);
    chooser.setFileFilter(WSDL_FILE_FILTER);

    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        File wsdlFile = chooser.getSelectedFile();
        jTxtLocalFilename.setText(wsdlFile.getAbsolutePath());
        previousDirectory = wsdlFile.getPath();
    }
}//GEN-LAST:event_jBtnBrowseActionPerformed

private void jRbnFilesystemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRbnFilesystemActionPerformed

    enableControls();
}//GEN-LAST:event_jRbnFilesystemActionPerformed

private void jTxtpackageNameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTxtpackageNameMouseClicked
    jTxtpackageName.selectAll();
    jTxtpackageName.setForeground(Color.BLACK);
}//GEN-LAST:event_jTxtpackageNameMouseClicked

private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
// TODO add your handling code here:
    enableControls();
}//GEN-LAST:event_formAncestorAdded

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JButton jBtnBrowse;
    private javax.swing.JLabel jLblChooseSource;
    private javax.swing.JRadioButton jRbnFilesystem;
    private javax.swing.JRadioButton jRbnUrl;
    private javax.swing.JTextField jTxServiceURL;
    private javax.swing.JTextField jTxtLocalFilename;
    private javax.swing.JTextField jTxtpackageName;
    private javax.swing.JLabel pkgNameLbl;
    // End of variables declaration//GEN-END:variables

    private static class ServiceFileFilter extends javax.swing.filechooser.FileFilter {

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            
            String ext = FileUtil.getExtension(f.getName());
            for (int i = 0; i < Saas.SUPPORTED_EXTENSIONS.length; i++) {
                if (Saas.SUPPORTED_EXTENSIONS[i].equalsIgnoreCase(ext)) {
                    return true;
                }
            }
             
            return false;
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(AddWebServiceDlg.class, "LBL_WsdlFilterDescription"); // NOI18N

        }
    }
}
