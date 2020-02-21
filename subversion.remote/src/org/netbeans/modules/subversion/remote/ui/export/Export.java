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
package org.netbeans.modules.subversion.remote.ui.export;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.subversion.remote.SvnModuleConfig;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public class Export implements DocumentListener, FocusListener, ActionListener {
    private static final String SCAN_AFTER_EXPORT = "scan_after_export";        // NOI18N
    private static final String EXPORT_FROM_DIRECTORY = "exportFromDirectory";  // NOI18N

    
    private final VCSFileProxy fromFile;
    private final ExportPanel panel;
    private final JButton okButton;
    private final JButton cancelButton;
    private final DialogDescriptor dialogDescriptor;
    private final FileSystem fileSystem;

    public Export(VCSFileProxy fromFile, boolean localChanges) {
        
        this.fromFile = fromFile;
        fileSystem = VCSFileProxySupport.getFileSystem(fromFile);
        
        panel = new ExportPanel();

        panel.scanCheckBox.setSelected(SvnModuleConfig.getDefault(fileSystem).getPreferences().getBoolean(SCAN_AFTER_EXPORT, false));
        panel.exportFromTextField.setText(fromFile.getPath());
        panel.browseToFolderButton.addActionListener(this);
        panel.exportToTextField.getDocument().addDocumentListener(this);

        panel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(Export.class, "CTL_ExportDialog_Title"));                   // NOI18N

        okButton = new JButton(NbBundle.getMessage(Export.class, "CTL_Export"));
        okButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(Export.class, "CTL_Export"));
        cancelButton = new JButton(NbBundle.getMessage(Export.class, "CTL_Cancel"));                                      // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(Export.class, "CTL_Cancel"));    // NOI18N

        dialogDescriptor =
                new DialogDescriptor(
                    panel,
                    NbBundle.getMessage(Export.class, "CTL_ExportDialog_Title"),
                    true,
                    new Object[]{okButton, cancelButton},
                    okButton,
                    DialogDescriptor.DEFAULT_ALIGN,
                    null,
                    null);
        okButton.setEnabled(false);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(Export.class, "CTL_ExportDialog_Title"));                // NOI18N

        validateUserInput();
    }       
    
    private void validateUserInput() {
        String toPath = panel.exportToTextField.getText().trim();
        if(toPath.equals("")) {
            okButton.setEnabled(false);
            setErrorText(NbBundle.getMessage(Export.class, "MSG_MISSING_TO_FOLDER"));
            return;
        }
        resetErrorText();
        okButton.setEnabled(true);
    }    

    void setErrorText(String txt) {
        panel.invalidValuesLabel.setVisible(true);
        panel.invalidValuesLabel.setText(txt);
    }
    
    private void resetErrorText() {
        panel.invalidValuesLabel.setVisible(false);
        panel.invalidValuesLabel.setText("");        
    }

    VCSFileProxy getToFile() {
        return VCSFileProxySupport.getResource(fileSystem, panel.exportToTextField.getText());
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        validateUserInput();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        validateUserInput();        
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        validateUserInput();        
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
        validateUserInput();        
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if(evt.getSource() == panel.browseToFolderButton) {
            onBrowse();
        }
    }

    boolean showDialog() {
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(Export.class, "CTL_ExportDialog_Title"));                     // NOI18N

        dialog.setVisible(true);
        return dialogDescriptor.getValue() == okButton;
    }

    private void onBrowse() {
        VCSFileProxy defaultDir = defaultWorkingDirectory();
        JFileChooser fileChooser = VCSFileProxySupport.createFileChooser(defaultDir);
        fileChooser.setDialogTitle(NbBundle.getMessage(Export.class, "BK0010"));// NOI18N
        fileChooser.setMultiSelectionEnabled(false);
        FileFilter[] old = fileChooser.getChoosableFileFilters();
        for (int i = 0; i < old.length; i++) {
            FileFilter fileFilter = old[i];
            fileChooser.removeChoosableFileFilter(fileFilter);

        }
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showDialog(panel, NbBundle.getMessage(Export.class, "BK0009"));// NOI18N
        VCSFileProxy f = VCSFileProxySupport.getSelectedFile(fileChooser);
        panel.exportToTextField.setText(f.getPath());
    }

    private VCSFileProxy defaultWorkingDirectory() {
        VCSFileProxy defaultDir = null;
        String current = panel.exportFromTextField.getText();
        if (current != null && !(current.trim().equals(""))) {  // NOI18N
            VCSFileProxy currentFile = VCSFileProxySupport.getResource(fileSystem, current);
            while (currentFile != null && currentFile.exists() == false) {
                currentFile = currentFile.getParentFile();
            }
            if (currentFile != null) {
                if (currentFile.isFile()) {
                    defaultDir = currentFile.getParentFile();
                } else {
                    defaultDir = currentFile;
                }
            }
        }

        if (defaultDir == null) {
            String coDir = SvnModuleConfig.getDefault(fileSystem).getPreferences().get(EXPORT_FROM_DIRECTORY, null);
            if(coDir != null) {
                defaultDir = VCSFileProxySupport.getResource(fileSystem, coDir);
            }
        }
        
        // TODO: Is it posssible to export between different FS?
        // TODO: Allow to select full rmote projects
        //if (defaultDir == null) {
        //    VCSFileProxy projectFolder = ProjectChooser.getProjectsFolder();
        //    if (projectFolder.exists() && projectFolder.isDirectory()) {
        //        defaultDir = projectFolder;
        //    }
        //}

        if (defaultDir == null) {
            defaultDir = VCSFileProxySupport.getHome(fromFile);
        }

        return defaultDir;
    }

    VCSFileProxy getFromFile() {
        return fromFile;
    }

    boolean getScanAfterExport() {
        return panel.scanCheckBox.isSelected();
    }
}
