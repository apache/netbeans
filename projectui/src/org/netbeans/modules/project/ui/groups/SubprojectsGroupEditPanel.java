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

package org.netbeans.modules.project.ui.groups;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import static org.netbeans.modules.project.ui.groups.Group.KEY_PATH;
import static org.netbeans.modules.project.ui.groups.Group.NODE;
import static org.netbeans.modules.project.ui.groups.GroupEditPanel.PROP_READY;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * Panel to configure state of an existing subproject-based group.
 * @author Jesse Glick
 */
public class SubprojectsGroupEditPanel extends GroupEditPanel {

    private final SubprojectsGroup g;

    public SubprojectsGroupEditPanel(SubprojectsGroup g) {
        this.g = g;
        initComponents();
        DocumentListener l = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                firePropertyChange(PROP_READY, null, null);
            }
            @Override public void removeUpdate(DocumentEvent e) {
                firePropertyChange(PROP_READY, null, null);
            }
            @Override public void changedUpdate(DocumentEvent e) {}
        };
        nameField.setText(g.getName());
        nameField.getDocument().addDocumentListener(l);
        FileObject dir = g.getMasterProjectDirectory();
        if (dir != null) {
            File d = FileUtil.toFile(dir);
            if (d != null) {
                masterProjectField.setText(d.getAbsolutePath());
            }
        }
        masterProjectField.getDocument().addDocumentListener(l);
    }

    @Override
    public void applyChanges() {
        g.setName(nameField.getText().trim());
        updateMasterProject();
    }
    
    private void updateMasterProject() {
        String s = masterProjectField.getText();
        if (s != null && s.length() > 0) {
            File f = new File(s);
            FileObject fo = FileUtil.toFileObject(f);
            if (fo != null && fo.isFolder()) {
                 try {
                    Project p = ProjectManager.getDefault().findProject(fo);
                    if (p != null){
                        String path = p.getProjectDirectory().toURL().toExternalForm();
                        Preferences pref = NODE.node(g.id);
                        pref.put(KEY_PATH, path);
                        if(g.equals(Group.getActiveGroup())) {
                            RequestProcessor.getDefault().post(new Runnable() {
                                @Override
                                public void run() {
                                    Group.open(g, null, false, null);
                                }
                            });
                        }
                    }
                 } catch (IOException x) {
                    Exceptions.printStackTrace(x);
                 }
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        masterProjectLabel = new javax.swing.JLabel();
        masterProjectField = new javax.swing.JTextField();
        masterProjectButton = new javax.swing.JButton();

        nameLabel.setLabelFor(nameField);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(SubprojectsGroupEditPanel.class, "SubprojectsGroupEditPanel.nameLabel.text")); // NOI18N

        masterProjectLabel.setLabelFor(masterProjectField);
        org.openide.awt.Mnemonics.setLocalizedText(masterProjectLabel, org.openide.util.NbBundle.getMessage(SubprojectsGroupEditPanel.class, "SubprojectsGroupEditPanel.masterProjectLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(masterProjectButton, org.openide.util.NbBundle.getMessage(SubprojectsGroupEditPanel.class, "SubprojectsGroupEditPanel.masterProjectButton.text")); // NOI18N
        masterProjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                masterProjectButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nameLabel)
                    .addComponent(masterProjectLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nameField, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(masterProjectField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(masterProjectButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(masterProjectLabel)
                    .addComponent(masterProjectField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(masterProjectButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        nameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SubprojectsGroupEditPanel.class, "SubprojectsGroupEditPanel.nameLabel.AccessibleContext.accessibleDescription")); // NOI18N
        nameField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SubprojectsGroupEditPanel.class, "SubprojectsGroupEditPanel.nameField.AccessibleContext.accessibleName")); // NOI18N
        nameField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SubprojectsGroupEditPanel.class, "SubprojectsGroupEditPanel.nameField.AccessibleContext.accessibleDescription")); // NOI18N
        masterProjectLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SubprojectsGroupEditPanel.class, "SubprojectsGroupEditPanel.masterProjectLabel.AccessibleContext.accessibleDescription")); // NOI18N
        masterProjectField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SubprojectsGroupEditPanel.class, "SubprojectsGroupEditPanel.masterProjectField.AccessibleContext.accessibleName")); // NOI18N
        masterProjectField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SubprojectsGroupEditPanel.class, "SubprojectsGroupEditPanel.masterProjectField.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SubprojectsGroupEditPanel.class, "SubprojectsGroupEditPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SubprojectsGroupEditPanel.class, "SubprojectsGroupEditPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void masterProjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_masterProjectButtonActionPerformed
        JFileChooser chooser = ProjectChooser.projectChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (f != null) {
                masterProjectField.setText(f.getAbsolutePath());
                //firePropertyChange(PROP_READY, null, null);
            }
        }
    }//GEN-LAST:event_masterProjectButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton masterProjectButton;
    private javax.swing.JTextField masterProjectField;
    private javax.swing.JLabel masterProjectLabel;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel nameLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean isReady() {
        if(!doCheckExistingGroups(nameField, g)) {
            return false;
        }
        String s = masterProjectField.getText();
        if (s == null || s.length() == 0) {
            return false;
        }
        else {
            File f = FileUtil.normalizeFile(new File(s));
            FileObject fo = FileUtil.toFileObject(f);
            if (fo != null && fo.isFolder()) {
                try {
                    return ProjectManager.getDefault().findProject(fo) != null;
                } catch (IOException x) {
                    Exceptions.printStackTrace(x);
                }
            } else {
                return false;
            }
        }
        return true;
    }

}
