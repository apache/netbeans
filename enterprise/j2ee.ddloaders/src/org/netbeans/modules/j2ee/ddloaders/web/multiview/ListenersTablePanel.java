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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.EditDialog;
import org.netbeans.modules.xml.multiview.ui.SimpleDialogPanel;
import org.openide.util.NbBundle;

/**
 *
 * @author  mk115033
 * Created on October 1, 2002, 3:52 PM
 */
public class ListenersTablePanel extends DefaultTablePanel {
    private ListenerTableModel model;
    private WebApp webApp;
    private DDDataObject dObj;
    
    /** Creates new form ListenersPanel */
    public ListenersTablePanel(final DDDataObject dObj, final ListenerTableModel model) {
    	super(model);
    	this.model=model;
        this.dObj=dObj;
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                int row = getTable().getSelectedRow();
                model.removeRow(row);
                dObj.setChangedFromUI(false);
            }
        });
        editButton.addActionListener(new TableActionListener(false));
        addButton.addActionListener(new TableActionListener(true));
    }

    void setModel(WebApp webApp, Listener[] listeners) {
        model.setData(webApp,listeners);
        this.webApp=webApp;
    }
    
    private class TableActionListener implements java.awt.event.ActionListener {
        private boolean add;
        TableActionListener(boolean add) {
            this.add=add;
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            final int row = (add?-1:getTable().getSelectedRow());
            String[] labels = new String[]{
                NbBundle.getMessage(ListenersTablePanel.class,"LBL_listenerClass"),
                NbBundle.getMessage(ListenersTablePanel.class,"LBL_description")
            };
            String[] a11y_desc = new String[]{
                NbBundle.getMessage(ListenersTablePanel.class,"ACSD_listener_class"),
                NbBundle.getMessage(ListenersTablePanel.class,"ACSD_listener_desc")
            };
            SimpleDialogPanel.DialogDescriptor descriptor =
                    new SimpleDialogPanel.DialogDescriptor(labels, true);
            if (!add) {
                String[] initValues = new String[] {
                    (String)model.getValueAt(row,0),
                    (String)model.getValueAt(row,1)
                };
                descriptor.setInitValues(initValues);
            }
            descriptor.setButtons(new boolean[]{true,false});
            descriptor.setTextField(new boolean[]{true,false});
            descriptor.setA11yDesc(a11y_desc);
            final SimpleDialogPanel dialogPanel = new SimpleDialogPanel(descriptor);
            if (add) {
                dialogPanel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ListenersTablePanel.class,"ACSD_add_listenerClass"));
                dialogPanel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ListenersTablePanel.class,"ACSD_add_listenerClass"));
            }else {
                dialogPanel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ListenersTablePanel.class,"ACSD_edit_listenerClass"));
                dialogPanel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ListenersTablePanel.class,"ACSD_edit_listenerClass"));
            }
            dialogPanel.getCustomizerButtons()[0].addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
                        org.netbeans.api.project.SourceGroup[] groups = DDUtils.getJavaSourceGroups(dObj);
                        org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
                        if (fo!=null) {
                            String className = DDUtils.getResourcePath(groups,fo);
                            dialogPanel.getTextComponents()[0].setText(className);
                        }
                    } catch (java.io.IOException ex) {
                        Logger.getLogger("ListenersTablePanel").log(Level.FINE, "ignored exception", ex); //NOI18N
                    }
                }
            });
            EditDialog dialog = new EditDialog(dialogPanel,NbBundle.getMessage(ListenersTablePanel.class,"TTL_Listener"),add) {
                protected String validate() {
                    String[] values = dialogPanel.getValues();
                    String name = values[0];
                    if (name.length()==0) {
                            return NbBundle.getMessage(ListenersTablePanel.class,"TXT_EmptyListenerClass");
                    } else {
                        Listener[] listeners = webApp.getListener();
                        boolean exists=false;
                        for (int i=0;i<listeners.length;i++) {
                            if (row!=i && name.equals(listeners[i].getListenerClass())) {
                                exists=true;
                                break;
                            }
                        }
                        if (exists) {
                            return NbBundle.getMessage(ListenersTablePanel.class,"TXT_ListenerClassExists",name);
                        }
                    }
                    return null;
                }
            };
            if (add) dialog.setValid(false); // disable OK button
            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getTextComponents()[0].getDocument().addDocumentListener(docListener);
            
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            dialogPanel.getTextComponents()[0].getDocument().removeDocumentListener(docListener);
            
            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                String[] values = dialogPanel.getValues();
                String name = values[0];
                String description = values[1];
                if (add) model.addRow(new String[]{name,description});
                else model.editRow(row,new String[]{name,description});
                dObj.setChangedFromUI(false);
            }
        }
    }    
}
