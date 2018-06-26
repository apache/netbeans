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

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.EditDialog;
import org.openide.util.NbBundle;
 
/** EjbRefsTablePanel - panel containing table for EJB references
 *
 * @author  mk115033
 * Created on April 11, 2005
 */
public class EjbRefsTablePanel extends DefaultTablePanel {
    private EjbRefTableModel model;
    private WebApp webApp;
    private DDDataObject dObj;
    /**
     * Indicates whether we are dealing with a Java EE 5 project.
     */
    private boolean javaEE5;
    
    /** Creates new form ContextParamPanel */
    public EjbRefsTablePanel(final DDDataObject dObj, final EjbRefTableModel model) {
    	super(model);
    	this.model=model;
        this.dObj=dObj;
        Project project = FileOwnerQuery.getOwner(dObj.getPrimaryFile());
        this.javaEE5 = ProjectUtil.isJavaEE5orHigher(project);
        
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

    void setModel(WebApp webApp, EjbRef[] params1, EjbLocalRef[] params2) {
        CommonDDBean[] params = new CommonDDBean[params1.length+params2.length];
        int k=0;
        for (int i=0;i<params1.length;i++) {
            params[i]=params1[i];
            k++;
        }
        for (int j=0;j<params2.length;j++) {
            params[k+j]=params2[j];
        }
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
            final EjbRefPanel dialogPanel = new EjbRefPanel();
            if (!add) {
                CommonDDBean ref = model.getEjbRef(row);
                if (ref instanceof EjbRef) {
                    EjbRef ejbRef = (EjbRef)ref;
                    dialogPanel.setEjbName(ejbRef.getEjbRefName());
                    dialogPanel.setBeanType(ejbRef.getEjbRefType());
                    dialogPanel.setInterfaceType("Remote"); //NOI18N
                    dialogPanel.setHome(ejbRef.getHome());
                    dialogPanel.setInterface(ejbRef.getRemote());
                    dialogPanel.setLink(ejbRef.getEjbLink());
                    dialogPanel.setDescription(ejbRef.getDefaultDescription());
                } else {
                    EjbLocalRef ejbRef = (EjbLocalRef)ref;
                    dialogPanel.setEjbName(ejbRef.getEjbRefName());
                    dialogPanel.setBeanType(ejbRef.getEjbRefType());
                    dialogPanel.setInterfaceType("Local"); //NOI18N
                    dialogPanel.setHome(ejbRef.getLocalHome());
                    dialogPanel.setInterface(ejbRef.getLocal());
                    dialogPanel.setLink(ejbRef.getEjbLink());
                    dialogPanel.setDescription(ejbRef.getDefaultDescription());
                }
            }
            EditDialog dialog = new EditDialog(dialogPanel,NbBundle.getMessage(EjbRefsTablePanel.class,"TTL_EjbRef"),add) {
                protected String validate() {
                    String name = dialogPanel.getEjbName().trim();
                    String home = dialogPanel.getHome().trim();
                    String remote = dialogPanel.getInterface().trim();
                    if (name.length()==0) {
                        return NbBundle.getMessage(EjbRefsTablePanel.class,"TXT_EmptyEjbRefName");
                    } else {
                        EjbRef[] refs = webApp.getEjbRef();
                        boolean exists=false;
                        int countEjbRef=refs.length;
                        for (int i=0;i<countEjbRef;i++) {
                            if (row!=i && name.equals(refs[i].getEjbRefName())) {
                                exists=true;
                                break;
                            }
                        }
                        if (!exists) {
                            EjbLocalRef[] localRefs = webApp.getEjbLocalRef();
                            for (int i=0;i<localRefs.length;i++) {
                                if (row!=i+countEjbRef && name.equals(localRefs[i].getEjbRefName())) {
                                    exists=true;
                                    break;
                                }
                            }
                        }
                        if (exists) {
                            return NbBundle.getMessage(EjbRefsTablePanel.class,"TXT_EjbRefNameExists",name);
                        }
                    }
                    if (home.length()==0 && !javaEE5) {
                        return NbBundle.getMessage(EjbRefsTablePanel.class,"TXT_EmptyEjbHome");
                    }
                    if (remote.length()==0) {
                        return NbBundle.getMessage(EjbRefsTablePanel.class,"TXT_EmptyEjbInterface");
                    }
                    return null;
                }
            };
            
            if (add) dialog.setValid(false); // disable OK button
            else dialogPanel.getInterfaceTypeCB().setEnabled(false);
            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getNameTF().getDocument().addDocumentListener(docListener);
            dialogPanel.getHomeTF().getDocument().addDocumentListener(docListener);
            dialogPanel.getInterfaceTF().getDocument().addDocumentListener(docListener);
            
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            dialogPanel.getNameTF().getDocument().removeDocumentListener(docListener);
            dialogPanel.getHomeTF().getDocument().removeDocumentListener(docListener);
            dialogPanel.getInterfaceTF().getDocument().removeDocumentListener(docListener);
            
            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                String name = dialogPanel.getEjbName().trim();
                String beanType = dialogPanel.getBeanType();
                String interfaceType = dialogPanel.getInterfaceType();
                String home = dialogPanel.getHome().trim();
                String remote = dialogPanel.getInterface().trim();
                String link = dialogPanel.getLink().trim();
                String description = dialogPanel.getDescription();
                if (add) model.addRow(new String[]{name,beanType,interfaceType,home,remote,link,description});
                else model.editRow(row,new String[]{name,beanType,interfaceType,home,remote,link,description});
                dObj.setChangedFromUI(false);
            }
        }   
    }
}
