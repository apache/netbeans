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
import org.netbeans.modules.j2ee.dd.api.web.ErrorPage;
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
public class ErrorPagesTablePanel extends DefaultTablePanel {
    
    private static final Logger LOG = Logger.getLogger(ErrorPagesTablePanel.class.getName());
    
    private ErrorPagesTableModel model;
    private WebApp webApp;
    private DDDataObject dObj;
    
    /** Creates new form ErrorPagesTablePanel */
    public ErrorPagesTablePanel(final DDDataObject dObj, final ErrorPagesTableModel model) {
    	super(model);
    	this.model=model;
        this.dObj=dObj;
        webApp = dObj.getWebApp();
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                int row = getTable().getSelectedRow();
                model.removeRow(row);
                dObj.setChangedFromUI(false);
            }
        });
        addButton.addActionListener(new TableActionListener(true));
        editButton.addActionListener(new TableActionListener(false));
    }

    void setModel(WebApp webApp, ErrorPage[] pages) {
        model.setData(webApp,pages);
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
                NbBundle.getMessage(ErrorPagesTablePanel.class,"LBL_errorPage"),
                NbBundle.getMessage(ErrorPagesTablePanel.class,"LBL_errorCode"),
                NbBundle.getMessage(ErrorPagesTablePanel.class,"LBL_exceptionType")
            };

            String[] a11y_desc = new String[]{
                NbBundle.getMessage(ErrorPagesTablePanel.class,"ACSD_errorPage"),
                NbBundle.getMessage(ErrorPagesTablePanel.class,"ACSD_errorCode"),
                NbBundle.getMessage(ErrorPagesTablePanel.class,"ACSD_exceptionType")
            };
            SimpleDialogPanel.DialogDescriptor descriptor =
                    new SimpleDialogPanel.DialogDescriptor(labels, true);
            if (!add) {
                Integer val = (Integer)model.getValueAt(row,1);
                String[] initValues = new String[] {
                    (String)model.getValueAt(row,0),
                    val==null?"":(val).toString(),
                    (String)model.getValueAt(row,2)
                };
                descriptor.setInitValues(initValues);
            }
            descriptor.setButtons(new boolean[]{true,false,false});
            descriptor.setA11yDesc(a11y_desc);
            final SimpleDialogPanel dialogPanel = new SimpleDialogPanel(descriptor);
            if (add) {
                dialogPanel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ErrorPagesTablePanel.class,"ACSD_add_err_page"));
                dialogPanel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ErrorPagesTablePanel.class,"ACSD_add_err_page"));
            }else {
                dialogPanel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ErrorPagesTablePanel.class,"ACSD_edit_err_page"));
                dialogPanel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ErrorPagesTablePanel.class,"ACSD_edit_err_page"));
            }
            dialogPanel.getTextComponents()[0].setEditable(false);
            dialogPanel.getCustomizerButtons()[0].addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
                        org.netbeans.api.project.SourceGroup[] groups = DDUtils.getDocBaseGroups(dObj);
                        org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
                        if (fo!=null) {
                            String res = DDUtils.getResourcePath(groups,fo,'/',true);
                            dialogPanel.getTextComponents()[0].setText("/"+res);
                        }
                    } catch (java.io.IOException ex) {
                        LOG.log(Level.FINE, "ignored exception", ex); //NOI18N
                    }
                }
            });
            EditDialog dialog = new EditDialog(dialogPanel,NbBundle.getMessage(ErrorPagesTablePanel.class,"TTL_ErrorPage"),add) {
                protected String validate() {
                    String[] values = dialogPanel.getValues();
                    String page = values[0].trim();
                    String code = values[1].trim();
                    String exc = values[2].trim();
                    if (page.length()==0) {
                        return NbBundle.getMessage(ErrorPagesTablePanel.class,"TXT_EmptyErrorPageLocation");
                    }
                    if (code.length()==0 && exc.length()==0) {
                        return NbBundle.getMessage(ErrorPagesTablePanel.class,"TXT_EP_BothMissing");
                    } else if (code.length()>0 && exc.length()>0) {
                        return NbBundle.getMessage(ErrorPagesTablePanel.class,"TXT_EP_BothSpecified");
                    } else if (code.length()>0)  {
                        Integer c = null;
                        try {
                            c = new Integer(code); 
                        } catch (NumberFormatException ex) {
                            LOG.log(Level.FINE, "ignored exception", ex); //NOI18N
                        }
                        if (c==null) {
                            return NbBundle.getMessage(ErrorPagesTablePanel.class,"TXT_EP_wrongNumber",code);
                        } else {
                            ErrorPage[] pages = webApp.getErrorPage();
                            boolean exists=false;
                            for (int i=0;i<pages.length;i++) {
                                if (row!=i && c.equals(pages[i].getErrorCode())) {
                                    exists=true;
                                    break;
                                }
                            }
                            if (exists) {
                                return NbBundle.getMessage(ErrorPagesTablePanel.class,"TXT_ErrorCodeExists",c);
                            }
                        }
                    } else {
                        ErrorPage[] pages = webApp.getErrorPage();
                        boolean exists=false;
                        for (int i=0;i<pages.length;i++) {
                            if (row!=i && exc.equals(pages[i].getExceptionType())) {
                                exists=true;
                                break;
                            }
                        }
                        if (exists) {
                            return NbBundle.getMessage(ErrorPagesTablePanel.class,"TXT_ExcTypeExists",exc);
                        }
                    }
                    return null;
                }
            };
            if (add) dialog.setValid(false); // disable OK button
            
            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getTextComponents()[0].getDocument().addDocumentListener(docListener);
            dialogPanel.getTextComponents()[1].getDocument().addDocumentListener(docListener);
            dialogPanel.getTextComponents()[2].getDocument().addDocumentListener(docListener);
            
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            dialogPanel.getTextComponents()[0].getDocument().removeDocumentListener(docListener);
            dialogPanel.getTextComponents()[1].getDocument().removeDocumentListener(docListener);
            dialogPanel.getTextComponents()[2].getDocument().removeDocumentListener(docListener);
            
            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                String[] values = dialogPanel.getValues();
                String page = values[0].trim();
                String code = values[1].trim();
                String exc = values[2].trim();
                if (add)
                    model.addRow(new Object[]{page,(code.length()==0?null:new Integer(code)),(exc.length()==0?null:exc)});
                else 
                    model.editRow(row,new Object[]{page,(code.length()==0?null:new Integer(code)),(exc.length()==0?null:exc)});
                dObj.setChangedFromUI(false);
            }
        }
    }
}
