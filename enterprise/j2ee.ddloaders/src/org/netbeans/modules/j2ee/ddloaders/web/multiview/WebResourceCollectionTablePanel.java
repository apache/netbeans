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

import org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.EditDialog;
import org.openide.util.NbBundle;

/**
 * WebResourceCollectionTablePanel.java
 *
 * Panel for displaying the web resource collection table.
 *
 * @author  ptliu
 */
public class WebResourceCollectionTablePanel extends DefaultTablePanel {
    private DDDataObject dObj;
    private WebResourceCollectionTableModel model;
    private SecurityConstraint constraint;
    
    /**
     * Creates new form WebResourceCollectionTablePanel
     */
    public WebResourceCollectionTablePanel(final DDDataObject dObj,
            final WebResourceCollectionTableModel model) {
        super(model);
        this.dObj = dObj;
        this.model = model;
        
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
    
    void setModel(WebApp webApp, SecurityConstraint constraint,
            WebResourceCollection[] collections) {
        model.setData(constraint, collections);
        model.setWebApp(webApp);
        this.constraint = constraint;
    }
    
    private class TableActionListener implements java.awt.event.ActionListener {
        private boolean add;
        
        TableActionListener(boolean add) {
            this.add=add;
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            final int row = (add?-1:getTable().getSelectedRow());
            final WebResourceCollectionPanel dialogPanel = new WebResourceCollectionPanel();
            
            if (!add) {
                WebResourceCollection col = model.getWebResourceCollection(row);
                dialogPanel.setResourceName(col.getWebResourceName());
                dialogPanel.setDescription(col.getDefaultDescription());
                dialogPanel.setUrlPatterns(col.getUrlPattern());
                dialogPanel.setHttpMethods(col.getHttpMethod());
            }
            
            EditDialog dialog = new EditDialog(dialogPanel,NbBundle.getMessage(EjbRefsTablePanel.class,"TTL_WebResource"),add) {
                protected String validate() {
                    String name = dialogPanel.getResourceName().trim();
                    WebResourceCollection webResource = null;
                    
                    if (row != -1) 
                        webResource = model.getWebResourceCollection(row);
                    
                    if (name.length()==0) {
                        return NbBundle.getMessage(WebResourceCollectionTablePanel.class,"TXT_EmptyWebResourceName");
                    } else {
                        WebResourceCollection[] col = constraint.getWebResourceCollection();
                        
                        for (int i = 0; i < col.length; i++) {
                            if (col[i] != webResource && name.equals(col[i].getWebResourceName())) {
                                return NbBundle.getMessage(WebResourceCollectionTablePanel.class,"TXT_WebResourceNameExists",name);
                            }
                        }
                    }
                    
                    String[] urlPatterns = dialogPanel.getUrlPatterns();
                    if (urlPatterns.length == 0) {
                        return NbBundle.getMessage(WebResourceCollectionTablePanel.class,"TXT_EmptyUrlPatterns",name);
                    }
                    return null;
                }
            };
            
            if (add)
                dialog.setValid(false); // disable OK button
            
            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getResourceNameTF().getDocument().addDocumentListener(docListener);
            dialogPanel.getDescriptionTF().getDocument().addDocumentListener(docListener);
            dialogPanel.getUrlPatternsTF().getDocument().addDocumentListener(docListener);
        
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            
            dialogPanel.getResourceNameTF().getDocument().removeDocumentListener(docListener);
            dialogPanel.getDescriptionTF().getDocument().removeDocumentListener(docListener);
            dialogPanel.getUrlPatternsTF().getDocument().removeDocumentListener(docListener);
            
            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                
                String resourceName = dialogPanel.getResourceName();
                String[] urlPatterns = dialogPanel.getUrlPatterns();
                String[] httpMethods = dialogPanel.getSelectedHttpMethods();
                String description = dialogPanel.getDescription();
                
                
                if (add)
                    model.addRow(new Object[] {resourceName, urlPatterns,
                    httpMethods, description});
                else
                    model.editRow(row, new Object[]{resourceName, 
                    urlPatterns, httpMethods, description});
              
                dObj.setChangedFromUI(false);
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.GridBagLayout());

    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
