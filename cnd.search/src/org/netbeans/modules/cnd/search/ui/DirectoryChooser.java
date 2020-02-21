/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.search.ui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.api.ui.FileChooserBuilder;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 */
public final class DirectoryChooser extends javax.swing.JPanel implements PropertyChangeListener {

    public static final String VALID_SELECTION = "validSelectionProperty"; // NOI18N
    private final JFileChooser chooser;
    private final JButton selectButton;
    private FileObject validatedFileObject = null;
    private Dialog dialog;

    public static FileObject chooseDirectory(Frame window, ExecutionEnvironment env, String initPath) {
        DirectoryChooser chooser = new DirectoryChooser(env, initPath);
        boolean cancelled = false;

        DialogDescriptor dd = new DialogDescriptor(chooser,
                NbBundle.getMessage(DirectoryChooser.class, "DirectoryChooser.title", env.getDisplayName()), // NOI18N
                true,
                new Object[]{chooser.selectButton, DialogDescriptor.CANCEL_OPTION},
                chooser.selectButton, DialogDescriptor.DEFAULT_ALIGN, null, null, true);
        chooser.dialog = DialogDisplayer.getDefault().createDialog(dd);
        chooser.dialog.setVisible(true);
        chooser.dialog.dispose();

        chooser.dialog = null;

        if (!cancelled && chooser.selectButton.equals(dd.getValue())) {
            return chooser.validatedFileObject;
        }

        return null;
    }

    /**
     * Creates new form SearchRootChooser
     */
    private DirectoryChooser(ExecutionEnvironment env, String path) {
        initComponents();
        selectButton = new JButton();
        Mnemonics.setLocalizedText(selectButton, NbBundle.getMessage(DirectoryChooser.class, "DirectoryChooser.ok_button.text")); // NOI18N
        FileChooserBuilder fcBuilder = new FileChooserBuilder(env);

        chooser = fcBuilder.createFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setControlButtonsAreShown(false);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }

            @Override
            public String getDescription() {
                return NbBundle.getMessage(DirectoryChooser.class, "DirectoryChooser.fileFilter.description"); // NOI18N
            }
        });

        chooser.addPropertyChangeListener(DirectoryChooser.this);
        chooser.setFocusCycleRoot(false);
        chooserPanel.add(chooser);

        // escape key has a special meaning in a file chooser...
        // in addition need to register own action to close the dialog.. (?)
        chooser.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "esc_pressed"); // NOI18N
        chooser.getActionMap().put("esc_pressed", new AbstractAction() { // NOI18N

            @Override
            public void actionPerformed(ActionEvent ae) {
                if (dialog != null) {
                    dialog.setVisible(false);
                }
            }
        });


        if (path != null && !path.isEmpty()) {
            chooser.setCurrentDirectory(new File(path));
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        chooser.rescanCurrentDirectory();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        chooserPanel = new javax.swing.JPanel();

        chooserPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chooserPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chooserPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel chooserPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (propName.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
            checkForm();
        }
    }

    private void checkForm() {
        File selection = chooser.getSelectedFile();
        if (selection != null) {
            FileObject fo = FileSystemProvider.fileToFileObject(selection);
            if (fo.canRead()) {
                validatedFileObject = fo;
                firePropertyChange(VALID_SELECTION, null, true);
                selectButton.setEnabled(true);
            } else {
                firePropertyChange(VALID_SELECTION, null, false);
                selectButton.setEnabled(false);
            }
        }
    }
}
