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

package org.netbeans.spi.java.project.support.ui;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.ant.FileChooser;
import org.netbeans.spi.java.project.support.JavadocAndSourceRootDetection;
import static org.netbeans.spi.java.project.support.ui.Bundle.*;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author  mkleint
 */
class EditJarPanel extends javax.swing.JPanel {

    private EditJarSupport.Item item;
    private AntProjectHelper helper;

    private EditJarPanel() {
        initComponents();
    }

    EditJarPanel(EditJarSupport.Item item, AntProjectHelper helper) {
        this();
        this.item = item;
        this.helper = helper;
        txtJar.setText(stripOffVariableMarkup(item.getJarFile()));
        if (item.getSourceFile() != null) {
            txtSource.setText(stripOffJARContent(stripOffVariableMarkup(item.getSourceFile())));
        }
        if (item.getJavadocFile() != null) {
            txtJavadoc.setText(stripOffJARContent(stripOffVariableMarkup(item.getJavadocFile())));
        }
    }

    private static String stripOffVariableMarkup(String v) {
        if (!v.startsWith("${var.")) { // NOI18N
            return v;
        }
        int i = v.replace('\\', '/').indexOf('/'); // NOI18N
        if (i == -1) {
            i = v.length();
        }
        return v.substring(6, i-1)+v.substring(i);
    }
    
    private static String stripOffJARContent(String v) {
        int i = v.indexOf("!/");
        if (i == -1) { // NOI18N
            return v;
        } else {
            return v.substring(0, i);
        }
    }

    private static Set<String> getVariableNames() {
        Set<String> names = new HashSet<String>();
        for (String v : PropertyUtils.getGlobalProperties().keySet()) {
            if (!v.startsWith("var.")) { // NOI18N
                continue;
            }
            names.add(v.substring(4));
        }
        return names;
    }
    
    private static String addVariableMarkup(String v) {
        int i = v.replace('\\', '/').indexOf('/'); // NOI18N
        if (i == -1) {
            i = v.length();
        }
        String varName = v.substring(0, i);
        if (!getVariableNames().contains(varName)) {
            return v;
        }
        return "${var." + varName + "}" + v.substring(i); // NOI18N
    }

    private static String convertPath(AntProjectHelper helper, String path, boolean javadoc) {
        String val = addVariableMarkup(path);
        String eval = helper.getStandardPropertyEvaluator().evaluate(val);
        if (eval == null) {
            return val;
        }
        FileObject fo = helper.resolveFileObject(eval);
        if (fo == null) {
            return val;
        }
        boolean archiveFile = false;
        if (FileUtil.isArchiveFile(fo)) {
            FileObject afo = FileUtil.getArchiveRoot(fo);
            if (afo == null) {
                Logger.getLogger(EditJarPanel.class.getName()).warning("Cannot open archive: " + FileUtil.getFileDisplayName(fo));  //NOI18N
                return val;
            }
            fo = afo;
            archiveFile = true;
        }
        FileObject root;
        if (javadoc) {
            root = JavadocAndSourceRootDetection.findJavadocRoot(fo);
        } else {
            root = JavadocAndSourceRootDetection.findSourceRoot(fo);
        }
        if (root != null) {
           if (FileUtil.isParentOf(fo, root)) {
                if (archiveFile) {
                    val += "!/"; //NOI18N
                }
                val += (val.replace('\\', '/').endsWith("/") ? "" : File.separator); // NOI18N
                String relPath = FileUtil.getRelativePath(fo, root);
                assert relPath != null : "fo="+fo+" root="+root; // NOI18N
                if (relPath.length() > 0) {
                    relPath += "/"; // NOI18N
                    if (!archiveFile) {
                        relPath = relPath.replace('/', File.separatorChar); //NOI18N
                    }
                    val += relPath;
                }
           } else if (FileUtil.isParentOf(root,fo)) {
               final File rootFile = FileUtil.toFile(root);
               return rootFile != null ? addVariableMarkup(rootFile.getAbsolutePath()) : val;
           }
        }
        return val;
    }

    EditJarSupport.Item assignValues() {
        if (txtSource.getText() != null && txtSource.getText().trim().length() > 0) {
            item.setSourceFile(convertPath(helper, txtSource.getText().trim(), false));
        } else {
            item.setSourceFile(null);
        }
        if (txtJavadoc.getText() != null && txtJavadoc.getText().trim().length() > 0) {
            item.setJavadocFile(convertPath(helper, txtJavadoc.getText().trim(), true));
        } else {
            item.setJavadocFile(null);
        }
        return item;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblJar = new javax.swing.JLabel();
        txtJar = new javax.swing.JTextField();
        lblJavadoc = new javax.swing.JLabel();
        txtJavadoc = new javax.swing.JTextField();
        btnJavadoc = new javax.swing.JButton();
        lblSource = new javax.swing.JLabel();
        txtSource = new javax.swing.JTextField();
        btnSource = new javax.swing.JButton();

        lblJar.setLabelFor(txtJar);
        org.openide.awt.Mnemonics.setLocalizedText(lblJar, org.openide.util.NbBundle.getMessage(EditJarPanel.class, "EditJarPanel.lblJar.text")); // NOI18N

        txtJar.setEditable(false);

        lblJavadoc.setLabelFor(txtJavadoc);
        org.openide.awt.Mnemonics.setLocalizedText(lblJavadoc, org.openide.util.NbBundle.getMessage(EditJarPanel.class, "EditJarPanel.lblJavadoc.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnJavadoc, org.openide.util.NbBundle.getMessage(EditJarPanel.class, "EditJarPanel.btnJavadoc.text")); // NOI18N
        btnJavadoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnJavadocActionPerformed(evt);
            }
        });

        lblSource.setLabelFor(txtSource);
        org.openide.awt.Mnemonics.setLocalizedText(lblSource, org.openide.util.NbBundle.getMessage(EditJarPanel.class, "EditJarPanel.lblSource.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnSource, org.openide.util.NbBundle.getMessage(EditJarPanel.class, "EditJarPanel.btnSource.text")); // NOI18N
        btnSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSourceActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblJar)
                    .addComponent(lblJavadoc)
                    .addComponent(lblSource))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtSource, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                            .addComponent(txtJavadoc, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnSource)
                            .addComponent(btnJavadoc)))
                    .addComponent(txtJar, javax.swing.GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblJar)
                    .addComponent(txtJar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblJavadoc)
                    .addComponent(btnJavadoc)
                    .addComponent(txtJavadoc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSource)
                    .addComponent(btnSource)
                    .addComponent(txtSource, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblJar.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditJarPanel.class, "ACSD_lblJar")); // NOI18N
        txtJar.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditJarPanel.class, "ACSD_lblJar")); // NOI18N
        lblJavadoc.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditJarPanel.class, "ACSD_lblJavadoc")); // NOI18N
        txtJavadoc.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditJarPanel.class, "ACSD_lblJavadoc")); // NOI18N
        btnJavadoc.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditJarPanel.class, "ACSD_btnJavadoc")); // NOI18N
        lblSource.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditJarPanel.class, "ACSD_lblSource")); // NOI18N
        txtSource.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditJarPanel.class, "ACSD_lblSource")); // NOI18N
        btnSource.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditJarPanel.class, "ACSD_btnSource")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditJarPanel.class, "ACSD_EditJarPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    @Messages("LBL_Edit_Jar_Panel_browse=Select JAR/folder")
    private void btnJavadocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnJavadocActionPerformed
        // Let user search for the Jar file
        FileChooser chooser;
        if (helper.isSharableProject()) {
            chooser = new FileChooser(helper, true);
        } else {
            chooser = new FileChooser(FileUtil.toFile(helper.getProjectDirectory()), null);
        }
        chooser.enableVariableBasedSelection(true);
        chooser.setFileHidingEnabled(false);
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle(LBL_Edit_Jar_Panel_browse());
        //#61789 on old macosx (jdk 1.4.1) these two method need to be called in this order.
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new SimpleFileFilter(
                "Javadoc Entry (folder, ZIP or JAR file)", 
                new String[]{"ZIP", "JAR"}));   // NOI18N 
        File curDir = helper.resolveFile(helper.getStandardPropertyEvaluator().evaluate(item.getJarFile()));
        chooser.setCurrentDirectory(curDir);
        int option = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)); // Sow the chooser

        if (option == JFileChooser.APPROVE_OPTION) {
            String files[];
            try {
                files = chooser.getSelectedPaths();
            } catch (IOException ex) {
                // TODO: add localized message
                Exceptions.printStackTrace(ex);
                return;
            }
            txtJavadoc.setText(chooser.getSelectedPathVariables() != null ? stripOffVariableMarkup(chooser.getSelectedPathVariables()[0]) : files[0]);
        }
        
    }//GEN-LAST:event_btnJavadocActionPerformed

    private void btnSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSourceActionPerformed
        FileChooser chooser;
        if (helper.isSharableProject()) {
            chooser = new FileChooser(helper, true);
        } else {
            chooser = new FileChooser(FileUtil.toFile(helper.getProjectDirectory()), null);
        }
        chooser.enableVariableBasedSelection(true);
        chooser.setFileHidingEnabled(false);
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle(LBL_Edit_Jar_Panel_browse());
        //#61789 on old macosx (jdk 1.4.1) these two method need to be called in this order.
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new SimpleFileFilter(
                "Source Entry (folder, ZIP or JAR file)", 
                new String[]{"ZIP", "JAR"}));   // NOI18N 
        File curDir = helper.resolveFile(helper.getStandardPropertyEvaluator().evaluate(item.getJarFile()));
        chooser.setCurrentDirectory(curDir);
        int option = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)); // Sow the chooser

        if (option == JFileChooser.APPROVE_OPTION) {
            String files[];
            try {
                files = chooser.getSelectedPaths();
            } catch (IOException ex) {
                // TODO: add localized message
                Exceptions.printStackTrace(ex);
                return;
            }
            txtSource.setText(chooser.getSelectedPathVariables() != null ? stripOffVariableMarkup(chooser.getSelectedPathVariables()[0]) : files[0]);
        }

    }//GEN-LAST:event_btnSourceActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnJavadoc;
    private javax.swing.JButton btnSource;
    private javax.swing.JLabel lblJar;
    private javax.swing.JLabel lblJavadoc;
    private javax.swing.JLabel lblSource;
    private javax.swing.JTextField txtJar;
    private javax.swing.JTextField txtJavadoc;
    private javax.swing.JTextField txtSource;
    // End of variables declaration//GEN-END:variables
    private static class SimpleFileFilter extends FileFilter {

        private String description;
        private Collection extensions;

        public SimpleFileFilter(String description, String[] extensions) {
            this.description = description;
            this.extensions = Arrays.asList(extensions);
        }

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String name = f.getName();
            int index = name.lastIndexOf('.');   //NOI18N
            if (index <= 0 || index == name.length() - 1) {
                return false;
            }
            String extension = name.substring(index + 1).toUpperCase();
            return this.extensions.contains(extension);
        }

        public String getDescription() {
            return this.description;
        }
    }
}
