/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.hibernate.loaders.cfg.multiview;

import org.netbeans.modules.hibernate.loaders.cfg.*;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.EditDialog;
import org.openide.util.NbBundle;

/**
 * Panel containing the collection cache table and add/edit/remove buttons
 * 
 * @author Dongmei Cao
 */
public class CollectionCachesTablePanel extends DefaultTablePanel {

    private CollectionCachesTableModel model;
    private HibernateCfgDataObject configDataObject;

    /** Creates new form CollectionCachesTablePanel */
    public CollectionCachesTablePanel(final HibernateCfgDataObject dObj, final CollectionCachesTableModel model) {
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
            final CollectionCachePanel dialogPanel = new CollectionCachePanel();

            if (!add) {
                String className = (String) model.getValueAt(row, 0);
                String region = (String) model.getValueAt(row, 1);
                String usage = (String) model.getValueAt(row, 2);
                dialogPanel.initValues(className, region, usage);
            }
            
            // Action listener for the Browse button
            dialogPanel.addBrowseClassActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
                        org.netbeans.api.project.SourceGroup[] groups = Util.getJavaSourceGroups(configDataObject);
                        org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
                        if (fo!=null) {
                            String className = Util.getResourcePath(groups,fo);
                            dialogPanel.getClassTextField().setText(className);
                        }
                    } catch (java.io.IOException ex) {}
                }
            });

            EditDialog dialog = new EditDialog(dialogPanel, NbBundle.getMessage(SecurityTablePanel.class, "LBL_Collection_Cache"), add) {

                protected String validate() {
                    // TODO: more validation code later
                    String className = dialogPanel.getClassName();
                    if (className.length() == 0) {
                        return NbBundle.getMessage(SecurityTablePanel.class, "TXT_Class_Empty");
                    }
                    return null;
                }
            };

            if (add) {
                dialog.setValid(false);
            } // disable OK button

            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getClassTextField().getDocument().addDocumentListener(docListener);

            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            dialogPanel.getClassTextField().getDocument().removeDocumentListener(docListener);

            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                configDataObject.modelUpdatedFromUI();
                String className = dialogPanel.getClassName();
                String region = dialogPanel.getRegion();
                String usage = dialogPanel.getUsage();
                if (add) {
                    model.addRow(new String[]{className, region, usage});
                } else {
                    model.editRow(row, new String[]{className, region, usage});
                }
            }
        }
    }
}
