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

/*
 * BindingOperationPanel.java
 *
 * Created on February 19, 2006, 8:48 AM
 */

package org.netbeans.modules.websvc.customization.multiview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JComponent;
import org.netbeans.modules.websvc.api.customization.model.BindingCustomization;
import org.netbeans.modules.websvc.api.customization.model.BindingOperationCustomization;
import org.netbeans.modules.websvc.api.customization.model.CustomizationComponentFactory;
import org.netbeans.modules.websvc.api.customization.model.DefinitionsCustomization;
import org.netbeans.modules.websvc.api.customization.model.EnableMIMEContent;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

/**
 *
 * @author  Roderico Cruz
 */
public class BindingOperationPanel extends SaveableSectionInnerPanel  {
    private BindingOperation bindingOperation;
    private WSDLModel model;
    private boolean wsdlDirty;
    private ModelChangeListener modelListener;
    private ModelChangeListener primaryModelListener;
    private Definitions primaryDefinitions;
    private WSDLModel primaryModel;
    
    /** Creates new form BindingOperationPanel */
    public BindingOperationPanel(SectionView view,
            BindingOperation bindingOperation, Definitions primaryDefinitions){
        super(view);
        this.bindingOperation = bindingOperation;
        this.primaryDefinitions = primaryDefinitions;
        this.model = this.bindingOperation.getModel();
        this.primaryModel = this.primaryDefinitions.getModel();
        initComponents();
        bindingName.setText(getParentOfBindingOperation(bindingOperation));
        
        sync();
        
        addModifier(enableMIMEContentCB);
        
        modelListener = new ModelChangeListener();
        PropertyChangeListener pcl = WeakListeners.propertyChange(modelListener, model);
        model.addPropertyChangeListener(pcl);
        
        if(primaryModel != model){
            primaryModelListener = new ModelChangeListener();
            PropertyChangeListener l = WeakListeners.propertyChange(primaryModelListener, primaryModel);
            primaryModel.addPropertyChangeListener(l);
        }
    }
    
    private String getParentOfBindingOperation(BindingOperation op){
        Binding binding = (Binding)op.getParent();
        return binding.getName();
    }
    
    class ModelChangeListener implements PropertyChangeListener{
        public void propertyChange(PropertyChangeEvent evt) {
            Object source = evt.getSource();
            if (source instanceof EnableMIMEContent){
                EnableMIMEContent emc = (EnableMIMEContent)source;
                WSDLComponent parent = emc.getParent();
                if(parent instanceof DefinitionsCustomization ||
                        parent instanceof BindingCustomization){
                    sync();
                }
            }
        }
    }
    
    private void sync(){
        List<BindingOperationCustomization> ee =
                bindingOperation.getExtensibilityElements(BindingOperationCustomization.class);
        if(ee.size() == 1){
            BindingOperationCustomization boc = ee.get(0);
            EnableMIMEContent emc = boc.getEnableMIMEContent();
            if(emc != null){
                setEnableMIMEContent(emc.isEnabled());
            } else{
                setEnableMIMEContent(getMIMEContentOfParent());
            }
        } else{
            setEnableMIMEContent(getMIMEContentOfParent());
        }
    }
    
    private boolean getMIMEContentOfParent(){
        boolean isMIMEContent = false;
        Binding binding = (Binding)bindingOperation.getParent();
        List<BindingCustomization> bcs = binding.getExtensibilityElements(BindingCustomization.class);
        if(bcs.size() > 0) {  //there is a BindingCustomization
            BindingCustomization bc = bcs.get(0);
            EnableMIMEContent mimeContent = bc.getEnableMIMEContent();
            if(mimeContent != null){ //there is a mime content
                isMIMEContent =  mimeContent.isEnabled();
            }else{
                isMIMEContent = getMIMEContentFromDefinitions(primaryDefinitions);
            }
        } else{ //there is no BindingCustomization, look in Definitions
            isMIMEContent = getMIMEContentFromDefinitions(primaryDefinitions);
        }
        return isMIMEContent;
    }
    
    private boolean getMIMEContentFromDefinitions(Definitions definitions){
        List<DefinitionsCustomization> dcs = definitions.getExtensibilityElements(DefinitionsCustomization.class);
        if(dcs.size() > 0){
            DefinitionsCustomization dc = dcs.get(0);
            EnableMIMEContent mimeContent = dc.getEnableMIMEContent();
            if(mimeContent != null){
                return mimeContent.isEnabled();
            }
        }
        return false;
    }
    
    public void setEnableMIMEContent(boolean enable){
        enableMIMEContentCB.setSelected(enable);
    }
    
    public Boolean getEnableMIMEContent(){
        return enableMIMEContentCB.isSelected();
    }
    
    public JComponent getErrorComponent(String string) {
        return new javax.swing.JButton("error");
    }
    
    public void linkButtonPressed(Object object, String string) {
    }
    
    public void setValue(JComponent jComponent, Object object) {
        List <BindingOperationCustomization> ee =
                bindingOperation.getExtensibilityElements(BindingOperationCustomization.class);
        CustomizationComponentFactory factory = CustomizationComponentFactory.getDefault();
        
        try {
            if(jComponent == enableMIMEContentCB){
                if(ee.size() > 0){ //there is an extensibility element
                    BindingOperationCustomization boc = ee.get(0);
                    EnableMIMEContent emc = boc.getEnableMIMEContent();
                    if(emc == null){ //there is no EnableMIMEContent, create one
                        try{
                            model.startTransaction();
                            emc = factory.createEnableMIMEContent(model);
                            emc.setEnabled(this.getEnableMIMEContent());
                            boc.setEnableMIMEContent(emc);
                            wsdlDirty = true;
                        } finally{
                                model.endTransaction();
                        }
                    } else{ //there is an EnableMIMEContent, reset it
                        try{
                            model.startTransaction();
                            emc.setEnabled(this.getEnableMIMEContent());
                            wsdlDirty = true;
                        } finally{
                                model.endTransaction();
                        }
                    }
                } else{  //there is no extensibility element, add a new one and add a new
                    //MIME content element
                    BindingOperationCustomization boc = factory.createBindingOperationCustomization(model);
                    EnableMIMEContent emc = factory.createEnableMIMEContent(model);
                    try{
                        model.startTransaction();
                        emc.setEnabled(this.getEnableMIMEContent());
                        boc.setEnableMIMEContent(emc);
                        bindingOperation.addExtensibilityElement(boc);
                        wsdlDirty = true;
                    } finally{
                            model.endTransaction();
                    }
                }
            }
        }
        catch(IllegalStateException ex){
            Exceptions.attachSeverity(ex, Level.WARNING);
            Exceptions.printStackTrace(ex);
        }
        
    }
    
    public boolean wsdlIsDirty() {
        return wsdlDirty;
    }
    
    public void save() {
        if(wsdlDirty){
           this.setModelDirty(model);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        emcButtonGroup = new javax.swing.ButtonGroup();
        enableMIMEContentCB = new javax.swing.JCheckBox();
        bindingLabel = new javax.swing.JLabel();
        bindingName = new javax.swing.JLabel();

        enableMIMEContentCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_ENABLE_MIME_CONTENT").charAt(0));
        enableMIMEContentCB.setText(org.openide.util.NbBundle.getBundle(BindingOperationPanel.class).getString("LBL_ENABLE_MIME_CONTENT")); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle"); // NOI18N
        enableMIMEContentCB.setToolTipText(bundle.getString("TOOLTIP_ENABLE_MIME")); // NOI18N
        enableMIMEContentCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        enableMIMEContentCB.setContentAreaFilled(false);
        enableMIMEContentCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        bindingLabel.setText(bundle.getString("LBL_ENCLOSING_BINDING")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bindingLabel)
                        .addGap(22, 22, 22)
                        .addComponent(bindingName, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(enableMIMEContentCB))
                .addContainerGap(201, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bindingLabel)
                    .addComponent(bindingName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                .addComponent(enableMIMEContentCB)
                .addContainerGap())
        );

        enableMIMEContentCB.getAccessibleContext().setAccessibleName(bundle.getString("LBL_ENABLE_MIME_CONTENT")); // NOI18N
        enableMIMEContentCB.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_ENABLE_MIME_CONTENT")); // NOI18N
        bindingLabel.getAccessibleContext().setAccessibleName(bundle.getString("LBL_ENCLOSING_BINDING")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bindingLabel;
    private javax.swing.JLabel bindingName;
    private javax.swing.ButtonGroup emcButtonGroup;
    private javax.swing.JCheckBox enableMIMEContentCB;
    // End of variables declaration//GEN-END:variables
    
}
