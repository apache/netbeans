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
 * BindingPanel.java
 *
 * Created on February 19, 2006, 8:46 AM
 */

package org.netbeans.modules.websvc.customization.multiview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JComponent;
import org.netbeans.modules.websvc.api.customization.model.BindingCustomization;
import org.netbeans.modules.websvc.api.customization.model.CustomizationComponentFactory;
import org.netbeans.modules.websvc.api.customization.model.DefinitionsCustomization;
import org.netbeans.modules.websvc.api.customization.model.EnableMIMEContent;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

/**
 *
 * @author  Roderico Cruz
 */
public class BindingPanel extends SaveableSectionInnerPanel {
    private Binding binding;
    private WSDLModel model;
    private boolean wsdlDirty;
    private ModelChangeListener modelListener;
    private BindingActionListener actionListener;
    private Definitions primaryDefinitions;
    
    /** Creates new form BindingPanel */
    public BindingPanel(SectionView view, Binding binding, Definitions primaryDefinitions){
        super(view);
        this.binding = binding;
        this.primaryDefinitions = primaryDefinitions;
        this.model = this.binding.getModel();
        initComponents();
        
        sync();
        
        modelListener = new ModelChangeListener();
        WSDLModel primaryModel = primaryDefinitions.getModel();
        PropertyChangeListener pcl = WeakListeners.propertyChange(modelListener, primaryModel);
        primaryModel.addPropertyChangeListener(pcl);
        
        actionListener = new BindingActionListener();
        ActionListener al = (ActionListener)WeakListeners.create(ActionListener.class, actionListener,
                enableMIMEContentCB);
        enableMIMEContentCB.addActionListener(al);
    }
    
    class ModelChangeListener implements PropertyChangeListener{
        public void propertyChange(PropertyChangeEvent evt) {
            Object source = evt.getSource();
            if (source instanceof EnableMIMEContent){
                EnableMIMEContent emc = (EnableMIMEContent)source;
                WSDLComponent parent = emc.getParent();
                if(parent instanceof DefinitionsCustomization){
                    sync();
                }
            }
        }
    }
    
    class BindingActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            setValue((JComponent)e.getSource(), null);
        }
    }
    
    private boolean getMIMEContentOfParent(){
        List<DefinitionsCustomization> dcs = primaryDefinitions.getExtensibilityElements(DefinitionsCustomization.class);
        if(dcs.size() > 0) {
            DefinitionsCustomization dc = dcs.get(0);
            EnableMIMEContent mimeContent = dc.getEnableMIMEContent();
            if(mimeContent != null){
                return mimeContent.isEnabled();
            }
        }
        return false;
    }
    
    private void sync(){
        List<BindingCustomization> ee =
                binding.getExtensibilityElements(BindingCustomization.class);
        if(ee.size() == 1){
            BindingCustomization bc = ee.get(0);
            EnableMIMEContent emc = bc.getEnableMIMEContent();
            if(emc != null){
                setEnableMIMEContent(emc.isEnabled());
            } else{
                setEnableMIMEContent(getMIMEContentOfParent());
            }
        } else{
            setEnableMIMEContent(getMIMEContentOfParent());
        }
    }
    
    public void setEnableMIMEContent(boolean enable){
        enableMIMEContentCB.setSelected(enable);
    }
    
    public boolean getEnableMIMEContent(){
        return enableMIMEContentCB.isSelected();
    }
    
    public JComponent getErrorComponent(String string) {
        return new javax.swing.JButton("error");
    }
    
    public void linkButtonPressed(Object object, String string) {
    }
    
    public void setValue(JComponent jComponent, Object object) {
        List <BindingCustomization> ee =
                binding.getExtensibilityElements(BindingCustomization.class);
        CustomizationComponentFactory factory = CustomizationComponentFactory.getDefault();
        
        try {
            if(jComponent == enableMIMEContentCB){
                if(ee.size() > 0){ //there is an extensibility element
                    BindingCustomization bc = ee.get(0);
                    EnableMIMEContent emc = bc.getEnableMIMEContent();
                    if(emc == null){ //there is no EnableMIMEContent, create one
                        try{
                            model.startTransaction();
                            emc = factory.createEnableMIMEContent(model);
                            emc.setEnabled(this.getEnableMIMEContent());
                            bc.setEnableMIMEContent(emc);
                            wsdlDirty = true;
                        } finally{
                                model.endTransaction();
                            
                        }
                    } else{ //there is an EnableWrapperStyle, reset it
                        try{
                            model.startTransaction();
                            emc.setEnabled(this.getEnableMIMEContent());
                            wsdlDirty = true;
                        } finally{
                                model.endTransaction();
                        }
                    }
                } else{  //there is no extensibility element, add a new one and add a new
                    //wrapper style element
                    BindingCustomization bc = factory.createBindingCustomization(model);
                    EnableMIMEContent emc = factory.createEnableMIMEContent(model);
                    try{
                        model.startTransaction();
                        emc.setEnabled(this.getEnableMIMEContent());
                        bc.setEnableMIMEContent(emc);
                        binding.addExtensibilityElement(bc);
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

        enableMIMEContentCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_ENABLE_MIME_CONTENT").charAt(0));
        enableMIMEContentCB.setText(org.openide.util.NbBundle.getBundle(BindingPanel.class).getString("LBL_ENABLE_MIME_CONTENT")); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle"); // NOI18N
        enableMIMEContentCB.setToolTipText(bundle.getString("TOOLTIP_ENABLE_MIME")); // NOI18N
        enableMIMEContentCB.setActionCommand(bundle.getString("LBL_ENABLE_MIME_CONTENT")); // NOI18N
        enableMIMEContentCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        enableMIMEContentCB.setContentAreaFilled(false);
        enableMIMEContentCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(enableMIMEContentCB)
                .addContainerGap(409, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(enableMIMEContentCB)
                .addContainerGap(30, Short.MAX_VALUE))
        );

        enableMIMEContentCB.getAccessibleContext().setAccessibleName(bundle.getString("LBL_ENABLE_MIME_CONTENT")); // NOI18N
        enableMIMEContentCB.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_ENABLE_MIME_CONTENT")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup emcButtonGroup;
    private javax.swing.JCheckBox enableMIMEContentCB;
    // End of variables declaration//GEN-END:variables
    
}
