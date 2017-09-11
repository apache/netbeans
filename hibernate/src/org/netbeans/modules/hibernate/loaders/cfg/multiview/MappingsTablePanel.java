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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hibernate.loaders.cfg.multiview;

import org.netbeans.modules.hibernate.loaders.cfg.*;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.EditDialog;
import org.openide.util.NbBundle;

/**
 *
 * @author Dongmei Cao
 */
public class MappingsTablePanel extends DefaultTablePanel {

    private MappingsTableModel model;
    private HibernateCfgDataObject configDataObject;

    /** Creates new form MappingsTablePanel */
    public MappingsTablePanel(final HibernateCfgDataObject dObj, final MappingsTableModel model) {
        super(model);
        this.model = model;
        this.configDataObject = dObj;
        removeButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configDataObject.modelUpdatedFromUI();
                int row = getTable().getSelectedRow();
                model.removeRow(row);
            }
        });
        editButton.addActionListener(new TableActionListener(false));
        addButton.addActionListener(new TableActionListener(true));
    }

    private class TableActionListener implements java.awt.event.ActionListener {

        private boolean add;

        TableActionListener(boolean add) {
            this.add = add;
        }

        public void actionPerformed(java.awt.event.ActionEvent evt) {
            final int row = (add ? -1 : getTable().getSelectedRow());
            final MappingPanel dialogPanel = new MappingPanel(configDataObject);

            if (!add) {
                String resourceName = (String) model.getValueAt(row, 0);
                String fileName = (String) model.getValueAt(row, 1);
                String jarName = (String) model.getValueAt(row, 2);
                String packageName = (String) model.getValueAt(row, 3);
                String className = (String) model.getValueAt(row, 4);

                dialogPanel.initValues(resourceName, fileName, jarName, packageName, className);
            }

            // Add listeners to the buttons
            addActionListeners(dialogPanel);

            EditDialog dialog = new EditDialog(dialogPanel, NbBundle.getMessage(SecurityTablePanel.class, "LBL_Mapping"), add) {

                protected String validate() {
                    // TODO: more validation code later
                    String resourceName = dialogPanel.getResourceName();
                    String fileName = dialogPanel.getFileName();
                    String jarName = dialogPanel.getJarName();
                    String packageName = dialogPanel.getPackageName();
                    String className = dialogPanel.getClassName();

                    // One of them should be specified
                    if (resourceName.length() == 0 &&
                            fileName.length() == 0 &&
                            jarName.length() == 0 &&
                            packageName.length() == 0 &&
                            className.length() == 0) {
                        return NbBundle.getMessage(SecurityTablePanel.class, "TXT_Mapping_Empty");
                    }
                    return null;
                }
            };

            if (add) {
                // enambe/disable OK button
                dialog.setValid(dialogPanel.isDataValid());
            } 

            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getResourceTextField().getDocument().addDocumentListener(docListener);
            dialogPanel.getFileTextField().getDocument().addDocumentListener(docListener);
            dialogPanel.getJarTextField().getDocument().addDocumentListener(docListener);
            dialogPanel.getPacakgeTextField().getDocument().addDocumentListener(docListener);
            dialogPanel.getClassTextField().getDocument().addDocumentListener(docListener);

            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            dialogPanel.getResourceTextField().getDocument().removeDocumentListener(docListener);
            dialogPanel.getFileTextField().getDocument().removeDocumentListener(docListener);
            dialogPanel.getJarTextField().getDocument().removeDocumentListener(docListener);
            dialogPanel.getPacakgeTextField().getDocument().removeDocumentListener(docListener);
            dialogPanel.getClassTextField().getDocument().removeDocumentListener(docListener);

            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                configDataObject.modelUpdatedFromUI();

                String resourceName = dialogPanel.getResourceName();
                String fileName = dialogPanel.getFileName();
                String jarName = dialogPanel.getJarName();
                String packageName = dialogPanel.getPackageName();
                String className = dialogPanel.getClassName();

                if (add) {
                    model.addRow(new String[]{resourceName, fileName, jarName, packageName, className});
                } else {
                    model.editRow(row, new String[]{resourceName, fileName, jarName, packageName, className});
                }
            }
        }

        private void addActionListeners(final MappingPanel dialogPanel) {
            
            // Action listener for the Browse file button
            /*dialogPanel.addFileButtonActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
                        org.netbeans.api.project.SourceGroup[] groups = Util.getJavaSourceGroups(configDataObject);
                        org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
                        if (fo != null) {
                            String className = Util.getResourcePath(groups, fo, '/', true);
                            dialogPanel.getFileTextField().setText(className);
                        }
                    } catch (java.io.IOException ex) {
                    }
                }
            });*/

            // Action listener for the Browse jar button
            /*dialogPanel.addJarButtonActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
                        org.netbeans.api.project.SourceGroup[] groups = Util.getJavaSourceGroups(configDataObject);
                        org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
                        if (fo != null) {
                            String className = Util.getResourcePath(groups, fo, '/', true);
                            dialogPanel.getJarTextField().setText(className);
                        }
                    } catch (java.io.IOException ex) {
                    }
                }
            });*/

            // Action listener for the Browse package button
            dialogPanel.addPackageButtonListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
                        org.netbeans.api.project.SourceGroup[] groups = Util.getJavaSourceGroups(configDataObject);
                        org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
                        if (fo != null) {
                            String className = Util.getResourcePath(groups, fo, '.', true);
                            dialogPanel.getPacakgeTextField().setText(className);
                        }
                    } catch (java.io.IOException ex) {
                    }
                }
            });

            // Action listener for the Browse class button
            dialogPanel.addClassButtonActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
                        org.netbeans.api.project.SourceGroup[] groups = Util.getJavaSourceGroups(configDataObject);
                        org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
                        if (fo != null) {
                            String className = Util.getResourcePath(groups, fo);
                            dialogPanel.getClassTextField().setText(className);
                        }
                    } catch (java.io.IOException ex) {
                    }
                }
            });
        }
    }
}
