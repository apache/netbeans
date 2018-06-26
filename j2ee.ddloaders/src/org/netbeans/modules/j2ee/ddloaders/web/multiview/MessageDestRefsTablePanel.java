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

import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.EditDialog;
import org.openide.util.NbBundle;

/** MessageDestRefsTablePanel - panel containing table for message destination references
 *
 * @author  mk115033
 * Created on April 14, 2005
 */
public class MessageDestRefsTablePanel extends DefaultTablePanel {
    private MessageDestRefTableModel model;
    private WebApp webApp;
    private DDDataObject dObj;
    
    /** Creates new form ContextParamPanel */
    public MessageDestRefsTablePanel(final DDDataObject dObj, final MessageDestRefTableModel model) {
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

    void setModel(WebApp webApp, MessageDestinationRef[] params) {
        model.setData(webApp,params);
        this.webApp=webApp;
    }
    
    private class TableActionListener implements java.awt.event.ActionListener {
        private boolean add;
        TableActionListener(boolean add) {
            this.add=add;
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            
            final int row = (add?-1:getTable().getSelectedRow());
            final MessageDestRefPanel dialogPanel = new MessageDestRefPanel();
            if (!add) {
                MessageDestinationRef resRef = model.getMessageDestRef(row);
                dialogPanel.setMessageDestRefName(resRef.getMessageDestinationRefName());
                dialogPanel.setMessageDestRefType(resRef.getMessageDestinationType());
                dialogPanel.setUsage(resRef.getMessageDestinationUsage());
                dialogPanel.setLink(resRef.getMessageDestinationLink()); //NOI18N
                dialogPanel.setDescription(resRef.getDefaultDescription());
            }
            EditDialog dialog = new EditDialog(dialogPanel,NbBundle.getMessage(MessageDestRefsTablePanel.class,"TTL_MessageDestRef"),add) {
                protected String validate() {
                    String name = dialogPanel.getMessageDestRefName().trim();
                    if (name.length()==0) {
                        return NbBundle.getMessage(MessageDestRefsTablePanel.class,"TXT_EmptyMessageDestRefName");
                    } else {
                        try {
                            MessageDestinationRef[] params = webApp.getMessageDestinationRef();
                            boolean exists=false;
                            for (int i=0;i<params.length;i++) {
                                if (row!=i && name.equals(params[i].getMessageDestinationRefName())) {
                                    exists=true;
                                    break;
                                }
                            }
                            if (exists) {
                                return NbBundle.getMessage(MessageDestRefsTablePanel.class,"TXT_MessageDestRefNameExists",name);
                            }
                        } catch (VersionNotSupportedException ex) {
                            return NbBundle.getMessage(MessageDestRefsTablePanel.class,"TXT_MessageDestNotSupported");
                        }
                    }
                    return null;
                }
            };
            
            if (add) dialog.setValid(false); // disable OK button
            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getNameTF().getDocument().addDocumentListener(docListener);
            
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            dialogPanel.getNameTF().getDocument().removeDocumentListener(docListener);
            
            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                String name = dialogPanel.getMessageDestRefName().trim();
                String type = dialogPanel.getMessageDestRefType();
                String usage = dialogPanel.getUsage();
                String link = dialogPanel.getLink().trim();
                String description = dialogPanel.getDescription();
                if (add) model.addRow(new String[]{name,type,usage,link,description});
                else model.editRow(row,new String[]{name,type,usage,link,description});
                dObj.setChangedFromUI(false);
            }
        }
    }
}
