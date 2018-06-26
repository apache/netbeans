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
                    val==null?"":((Integer)val).toString(),
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
