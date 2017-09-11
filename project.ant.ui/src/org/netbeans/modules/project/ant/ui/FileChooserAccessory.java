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

package org.netbeans.modules.project.ant.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.ButtonModel;
import javax.swing.JFileChooser;
import org.netbeans.api.project.ant.FileChooser;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.project.ant.ui.VariablesModel.Variable;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import static org.netbeans.modules.project.ant.ui.Bundle.*;

/**
 * Accessory allowing to choose how file is referenced from a project - relative
 * or absolute.
 * @see FileChooser
 * @author David Konecny
 */
public final class FileChooserAccessory extends javax.swing.JPanel
        implements ActionListener, PropertyChangeListener {

    private File baseFolder;
    private final File sharedLibrariesFolder;
    private boolean copyAllowed;
    private JFileChooser chooser;
    private List<String> copiedRelativeFiles = null;
    private VariablesModel varModel;
    private boolean enableVariableBasedSelection = false;

    private boolean userSelection = false;

    /**
     * Constructor for usage from FileChooser.
     */
    public FileChooserAccessory(JFileChooser chooser, File baseFolder, File sharedLibrariesFolder, boolean copyAllowed) {
        assert baseFolder != null;
        assert !baseFolder.isFile();
        if (sharedLibrariesFolder != null) {
            assert !sharedLibrariesFolder.isFile() : true;
            if (!sharedLibrariesFolder.equals(FileUtil.normalizeFile(sharedLibrariesFolder))) {
                throw new IllegalArgumentException("Parameter file was not "+  // NOI18N
                    "normalized. Was "+sharedLibrariesFolder+" instead of "+
                        FileUtil.normalizeFile(sharedLibrariesFolder));  // NOI18N
            }
        }
        this.baseFolder = baseFolder;
        this.sharedLibrariesFolder = sharedLibrariesFolder;
        this.copyAllowed = copyAllowed && sharedLibrariesFolder != null;
        this.chooser = chooser;
        initComponents();
        //copyPanel.setVisible(copyAllowed);
        rbCopy.addActionListener(this);
        rbRelative.addActionListener(this);
        rbAbsolute.addActionListener(this);
        rbVariable.addActionListener(this);
        if (chooser != null) {
            chooser.addPropertyChangeListener(this);
        }
        if (sharedLibrariesFolder != null) {
            copyTo.setText(sharedLibrariesFolder.getAbsolutePath());
        }
        enableAccessory(false);
        if (!copyAllowed) {
            rbCopy.setVisible(false);
            copyTo.setVisible(false);
        }
        enableVariableBasedSelection(enableVariableBasedSelection);
    }

    public void enableVariableBasedSelection(boolean enable) {
        this.enableVariableBasedSelection = enable;
        rbVariable.setVisible(enableVariableBasedSelection);
        variablePath.setVisible(enableVariableBasedSelection);
        variablesButton.setVisible(enableVariableBasedSelection);
    }

    public String[] getFiles() {
        assert isRelative();
        if (isCopy()) {
            return copiedRelativeFiles.toArray(new String[copiedRelativeFiles.size()]);
        } else {
            List<File> files = Arrays.asList(getSelectedFiles());
            List<String> l = getRelativeFiles(files);
            return l.toArray(new String[l.size()]);
        }
    }

    @Messages({
        "# {0} - sharable library location", "FileChooserAccessory.warning1=Files can be only copied to the sharable libraries location ({0}) or its subfolder(s).",
        "# {0} - folder location to be created", "FileChooserAccessory.warning2=Are you sure you want to create folder {0} ?",
        "FileChooserAccessory.warning3=Some of the files already exist in destination directory. Are you sure you want to override them?",
        "FileChooserAccessory.warning4=Some of the selected files are folders. Are you sure you want to copy the folder and all its subfolders recursively?"
    })
    public boolean canApprove() {
        if (!isCopy()) {
            return true;
        }
        File f = FileUtil.normalizeFile(new File(copyTo.getText()));
        if (!f.getPath().equals(sharedLibrariesFolder.getPath()) &&
                !(f.getPath()).startsWith(sharedLibrariesFolder.getPath()+File.separatorChar)) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(FileChooserAccessory_warning1(sharedLibrariesFolder.getPath())));

            return false;
        }
        final File[] files = getSelectedFiles();
        if (f.exists()) {
            for (File file : files) {
                File testFile = new File(f, file.getName());
                if (testFile.exists()) {
                    if (NotifyDescriptor.YES_OPTION != DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(FileChooserAccessory_warning3()))) {
                        return false;
                    }
                    break;
                }
            }
        } else {
            if (NotifyDescriptor.YES_OPTION != DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(FileChooserAccessory_warning2(f.getPath())))) {
                return false;
            }
        }
        for (File file : files) {
            if (file.isDirectory()) {
                if (NotifyDescriptor.YES_OPTION != DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(FileChooserAccessory_warning4()))) {
                    return false;
                }
                break;
            }
        }

        return true;
    }

    public void copyFilesIfNecessary() throws IOException {
        if (!isCopy()) {
            return;
        }
        File f = FileUtil.normalizeFile(new File(copyTo.getText()));
        FileUtil.createFolder(f);
        FileObject fo = FileUtil.toFileObject(f);
        List<File> selectedFiles = Arrays.asList(getSelectedFiles());
        copyFiles(selectedFiles, fo);
    }

    private void enableAccessory(boolean enable) {
        rbRelative.setEnabled(enable);
        relativePath.setEnabled(enable);
        rbCopy.setEnabled(enable && copyAllowed);
        rbAbsolute.setEnabled(enable);
        absolutePath.setEnabled(enable);
        copyTo.setEnabled(enable);
        copyTo.setEditable(enable && rbCopy.isSelected());
        if (enable) {
            File f = getSelectedFiles()[0];
            boolean b = getVariablesModel().getRelativePath(f, true) != null;
            rbVariable.setEnabled(b);
            variablePath.setEnabled(b);
        } else {
            rbVariable.setEnabled(false);
            variablePath.setEnabled(false);
        }
        variablePath.setEditable(false);
        variablesButton.setEnabled(enable);
    }

    private File[] getSelectedFiles() {
        File files[];
        if (chooser.isMultiSelectionEnabled()) {
            files = chooser.getSelectedFiles();
        } else {
            if (chooser.getSelectedFile() != null) {
                files = new File[] { chooser.getSelectedFile() };
            } else {
                files = new File[0];
            }
        }
        for (int i = 0; i < files.length; i++) {
            // #135677 - user could type "../folder" and pressed OK 
            //           normalize such a filename:
            files[i] = FileUtil.normalizeFile(files[i]);
        }
        return files;
    }

    public boolean isRelative() {
        return (rbRelative.isEnabled() && rbRelative.isSelected()) || isCopy();
    }

    public boolean isVariableBased() {
        return rbVariable.isEnabled() && rbVariable.isSelected();
    }

    private boolean isCopy() {
        return rbCopy.isEnabled() && rbCopy.isSelected();
    }

    @Override public void actionPerformed(ActionEvent e) {
        copyTo.setEditable(e.getSource() == rbCopy);
    }

    @Override public void propertyChange(PropertyChangeEvent e) {
        if (!(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(e.getPropertyName()) ||
                JFileChooser.SELECTED_FILES_CHANGED_PROPERTY.equals(e.getPropertyName()))) {
            return;
        }
        File[] files = getSelectedFiles();
        enableAccessory(files.length != 0);
        update(Arrays.asList(files));
    }

    @Messages("FileChooserAccessory.noSuitableVariable=<no suitable variable>")
    private void update(List<File> files) {
        StringBuilder absolute = new StringBuilder();
        StringBuilder relative = new StringBuilder();
        StringBuilder variable = new StringBuilder();
        boolean isRelative = true;
        for (File file : files) {
            String varPath = getVariablesModel().getRelativePath(file, true);
            if (absolute.length() != 0) {
                absolute.append(", ");
                relative.append(", ");
                if (varPath != null) {
                    variable.append(", ");
                }
            }
            absolute.append(file.getAbsolutePath());
            String s = PropertyUtils.relativizeFile(baseFolder, file);
            if (s == null) {
                isRelative = false;
            }
            relative.append(s);

            if (varPath != null) {
                variable.append(varPath);
            }
        }
        rbRelative.setEnabled(isRelative && rbRelative.isEnabled());
        relativePath.setText(relative.toString());
        relativePath.setCaretPosition(0);
        relativePath.setToolTipText(relative.toString());
        if (!isRelative) {
            relativePath.setText("");
            relativePath.setToolTipText("");
        }
        absolutePath.setText(absolute.toString());
        absolutePath.setCaretPosition(0);
        absolutePath.setToolTipText(absolute.toString());

        if (variable.length() == 0) {
            variable.append(FileChooserAccessory_noSuitableVariable());
        }
        variablePath.setText(variable.toString());
        variablePath.setCaretPosition(0);
        variablePath.setToolTipText(variable.toString());

        //when deciding on predefined file, we can assume certain options to be preferable.
        ButtonModel selection = buttonGroup1.getSelection();
        if(selection == null || !selection.isEnabled() || !userSelection){
            if (areCollocated(baseFolder, files)) {
                rbRelative.setSelected(true);
            } else if (areVarRelated(files)) {
                rbVariable.setSelected(true);
            } else if (copyAllowed) {
                rbCopy.setSelected(true);
            } else {
                rbAbsolute.setSelected(true);
            }
            if (selection != buttonGroup1.getSelection()){
                userSelection = false;
            }
        }
    }

    private boolean areVarRelated(Collection<File> files){
        VariablesModel model = getVariablesModel();
        for(File file: files){
            if (model.getRelativePath(file, true) == null){
                return false;
            }
        }
        return true;
    }


    private boolean areCollocated(File base, Collection<File> files){
        for(File file: files){
            if (!CollocationQuery.areCollocated(Utilities.toURI(base), Utilities.toURI(file))) {
                return false;
            }
        }
        return true;
    }

    private VariablesModel getVariablesModel() {
        if (varModel == null) {
            varModel = new VariablesModel();
        }
        return varModel;
    }

    private List<String> getRelativeFiles(List<File> files) {
        List<String> fs = new ArrayList<String>();
        for (File file : files) {
            String s = PropertyUtils.relativizeFile(baseFolder, file);
            if (s != null) {
                fs.add(s);
            }
        }
        return fs;
    }

    public String[] getVariableBasedFiles() {
        assert isVariableBased();
        List<File> files = Arrays.asList(getSelectedFiles());
        List<String> l = getVariableBasedFiles(files);
        return l.toArray(new String[l.size()]);
    }

    private List<String> getVariableBasedFiles(List<File> files) {
        List<String> fs = new ArrayList<String>();
        for (File file : files) {
            String s = getVariablesModel().getRelativePath(file, false);
            if (s != null) {
                fs.add(s);
            }
        }
        return fs;
    }

    @Messages({"# {0} - file location", "FileChooserAccessory_no_such_file=Cannot copy nonexistent path {0} to libraries folder."})
    private void copyFiles(List<File> files, FileObject newRoot) throws IOException {
        List<File> fs = new ArrayList<File>();
        for (File file : files) {
            FileObject fo = FileUtil.toFileObject(file);
            if (fo == null) {
                throw Exceptions.attachLocalizedMessage(new FileNotFoundException(file.toString()), FileChooserAccessory_no_such_file(file));
            }
            FileObject newFO;
            if (fo.isFolder()) {
                newFO = copyFolderRecursively(fo, newRoot);
            } else {
                FileObject foExists = newRoot.getFileObject(fo.getName(), fo.getExt());
                if (foExists != null) {
                    foExists.delete();
                }
                newFO = FileUtil.copyFile(fo, newRoot, fo.getName(), fo.getExt());
            }
            fs.add(FileUtil.toFile(newFO));
        }
        copiedRelativeFiles = getRelativeFiles(fs);
    }

    public static FileObject copyFolderRecursively(final FileObject sourceFolder, final FileObject destination) throws IOException {
        FileUtil.runAtomicAction(new FileSystem.AtomicAction() {
            @Override public void run() throws IOException {
                assert sourceFolder.isFolder() : sourceFolder;
                assert destination.isFolder() : destination;
                FileObject destinationSubFolder = destination.getFileObject(sourceFolder.getName());
                if (destinationSubFolder == null) {
                    destinationSubFolder = destination.createFolder(sourceFolder.getName());
                }
                for (FileObject fo : sourceFolder.getChildren()) {
                    if (fo.isFolder()) {
                        copyFolderRecursively(fo, destinationSubFolder);
                    } else {
                        FileObject foExists = destinationSubFolder.getFileObject(fo.getName(), fo.getExt());
                        if (foExists != null) {
                            foExists.delete();
                        }
                        FileUtil.copyFile(fo, destinationSubFolder, fo.getName(), fo.getExt());
                    }
                }

            }
        });
        return destination.getFileObject(sourceFolder.getName());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        lblReference = new javax.swing.JLabel();
        rbRelative = new javax.swing.JRadioButton();
        relativePath = new javax.swing.JTextField();
        rbVariable = new javax.swing.JRadioButton();
        variablePath = new javax.swing.JTextField();
        variablesButton = new javax.swing.JButton();
        rbCopy = new javax.swing.JRadioButton();
        copyTo = new javax.swing.JTextField();
        rbAbsolute = new javax.swing.JRadioButton();
        absolutePath = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(lblReference, org.openide.util.NbBundle.getMessage(FileChooserAccessory.class, "FileChooserAccessory.lblReference.text")); // NOI18N

        buttonGroup1.add(rbRelative);
        rbRelative.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rbRelative, org.openide.util.NbBundle.getMessage(FileChooserAccessory.class, "FileChooserAccessory.rbRelative.text")); // NOI18N
        rbRelative.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbActionPerformed(evt);
            }
        });

        relativePath.setEditable(false);
        relativePath.setText("null");

        buttonGroup1.add(rbVariable);
        org.openide.awt.Mnemonics.setLocalizedText(rbVariable, org.openide.util.NbBundle.getMessage(FileChooserAccessory.class, "FileChooserAccessory.rbVariable.text")); // NOI18N
        rbVariable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(variablesButton, org.openide.util.NbBundle.getMessage(FileChooserAccessory.class, "FileChooserAccessory.variablesButton.text")); // NOI18N
        variablesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                variablesButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(rbCopy);
        org.openide.awt.Mnemonics.setLocalizedText(rbCopy, org.openide.util.NbBundle.getMessage(FileChooserAccessory.class, "FileChooserAccessory.rbCopy.text")); // NOI18N
        rbCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbActionPerformed(evt);
            }
        });

        copyTo.setEditable(false);
        copyTo.setText("null");

        buttonGroup1.add(rbAbsolute);
        org.openide.awt.Mnemonics.setLocalizedText(rbAbsolute, org.openide.util.NbBundle.getMessage(FileChooserAccessory.class, "FileChooserAccessory.rbAbsolute.text")); // NOI18N
        rbAbsolute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbActionPerformed(evt);
            }
        });

        absolutePath.setEditable(false);
        absolutePath.setText("null");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblReference)
                    .addComponent(rbRelative)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rbVariable)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(variablePath, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(variablesButton))
                    .addComponent(rbCopy, javax.swing.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(copyTo, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE))
                    .addComponent(rbAbsolute)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(absolutePath, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(relativePath, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblReference)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbRelative)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(relativePath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbVariable)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(variablesButton)
                    .addComponent(variablePath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbCopy)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(copyTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbAbsolute)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(absolutePath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        rbRelative.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FileChooserAccessory.class, "ACSD_FileChooserAccessory_NA")); // NOI18N
        rbVariable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FileChooserAccessory.class, "ACSD_FileChooserAccessory_NA")); // NOI18N
        variablesButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FileChooserAccessory.class, "ACSD_FileChooserAccessory.variablesButton.text")); // NOI18N
        rbCopy.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FileChooserAccessory.class, "ACSD_FileChooserAccessory_NA")); // NOI18N
        rbAbsolute.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FileChooserAccessory.class, "ACSD_FileChooserAccessory_NA")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void variablesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_variablesButtonActionPerformed
    Variable selected = VariablesPanel.showVariablesCustomizer();
    varModel = null;
    File[] files = getSelectedFiles();
    if (selected != null && ((files.length > 0 && getVariablesModel().getRelativePath(files[0], true) == null) ||
            files.length == 0)) {
        chooser.setSelectedFile(selected.getValue());
        files = getSelectedFiles();
    }
    enableAccessory(files.length != 0);
    update(Arrays.asList(files));
}//GEN-LAST:event_variablesButtonActionPerformed

private void rbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbActionPerformed
    userSelection = true;
}//GEN-LAST:event_rbActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField absolutePath;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTextField copyTo;
    private javax.swing.JLabel lblReference;
    private javax.swing.JRadioButton rbAbsolute;
    private javax.swing.JRadioButton rbCopy;
    private javax.swing.JRadioButton rbRelative;
    private javax.swing.JRadioButton rbVariable;
    private javax.swing.JTextField relativePath;
    private javax.swing.JTextField variablePath;
    private javax.swing.JButton variablesButton;
    // End of variables declaration//GEN-END:variables
}
