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
 * PortPanel.java
 *
 * Created on February 19, 2006, 8:58 AM
 */

package org.netbeans.modules.websvc.customization.multiview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.api.customization.model.JavaMethod;
import org.netbeans.modules.websvc.api.customization.model.PortCustomization;
import org.netbeans.modules.websvc.api.customization.model.Provider;
import org.netbeans.modules.websvc.api.customization.model.JAXWSQName;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.netbeans.modules.xml.multiview.Error;

/**
 *
 * @author  Roderico Cruz
 */
public class PortPanel extends SaveableSectionInnerPanel {
    private Port port;
    private WSDLModel model;
    private boolean wsdlDirty;
    private DefaultItemListener defaultListener;
    private ProviderActionListener providerActionListener;
    private Node node;
    
    /** Creates new form PortPanel */
    public PortPanel(SectionView view, Port port,
            Node node) {
        super(view);
        this.port = port;
        this.model = this.port.getModel();
        this.node = node;
        initComponents();
        disableEnterKey();
        sync();
        
        defaultListener = new DefaultItemListener();
        ItemListener itemListener = WeakListeners.create(ItemListener.class, defaultListener,
                defaultMethodCB);
        defaultMethodCB.addItemListener(itemListener);
        
        if(!isClient(node)){
            providerActionListener = new ProviderActionListener();
            ActionListener providerListener = WeakListeners.create(ActionListener.class,
                    providerActionListener, providerCB);
            providerCB.addActionListener(providerListener);
        } else{
            providerCB.setVisible(false);
        }
        
        addModifier(portAccessMethodText);
        addModifier(defaultMethodCB);
        addValidatee(portAccessMethodText);
    }
    
    class DefaultItemListener implements ItemListener{
        public void itemStateChanged(ItemEvent e) {
            if(defaultMethodCB.isSelected()){
                portAccessMethodText.setEnabled(false);
            } else{
                portAccessMethodText.setEnabled(true);
                portAccessMethodText.requestFocus();
            }
        }
    }
    
    class ProviderActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == providerCB){
                if(providerCB.isSelected()){
                    NotifyDescriptor.Confirmation notifyDesc =
                            new NotifyDescriptor.Confirmation(NbBundle.getMessage
                            (ExternalBindingTablePanel.class, "WARN_PROVIDER_INTERFACE"),
                            NotifyDescriptor.YES_NO_OPTION);
                    DialogDisplayer.getDefault().notify(notifyDesc);
                    if((notifyDesc.getValue() == NotifyDescriptor.NO_OPTION)){
                        providerCB.setSelected(false);
                        return;
                    }
                }
                setValue(providerCB, null);
            }
        }
    }
    
    private String getParentOfPort(Port port){
        Service service = (Service)port.getParent();
        return service.getName();
    }
    private void sync(){
        List<PortCustomization> ee = port.getExtensibilityElements(PortCustomization.class);
        if(ee.size() == 1){
            PortCustomization pc = ee.get(0);
            JavaMethod jm = pc.getJavaMethod();
            if(jm != null){
                setPortAccessMethod(jm.getName());
            } else{
                defaultMethodCB.setSelected(true);
                portAccessMethodText.setEnabled(false);
            }
            Provider provider = pc.getProvider();
            if(provider != null){
                if(provider.isEnabled()){
                    providerCB.setSelected(true);
                }else{
                    providerCB.setSelected(false);
                }
            } else{
                providerCB.setSelected(false);
            }
        } else{
            providerCB.setSelected(false);
            defaultMethodCB.setSelected(true);
            portAccessMethodText.setEnabled(false);
        }
    }
    
    public void setPortAccessMethod(String name){
        portAccessMethodText.setText(name);
    }
    
    public String getPortAccessMethod(){
        return portAccessMethodText.getText();
    }
    
    public void setProvider(boolean enable){
        providerCB.setSelected(enable);
    }
    
    public boolean isProvider(){
        return providerCB.isSelected();
    }
    
    public JComponent getErrorComponent(String string) {
        return new javax.swing.JButton("error");
    }
    
    public void linkButtonPressed(Object object, String string) {
    }
    
    public void setValue(JComponent jComponent, Object object) {
        List <PortCustomization> ee =
                port.getExtensibilityElements(PortCustomization.class);
        try {
            if(jComponent == providerCB){
                if(ee.size() == 1){ //there is a PortCustomization element
                    PortCustomization pc = ee.get(0);
                    Provider provider = pc.getProvider();
                    if(isProvider()){ //provider is selected
                        if(provider == null){ //there is no provider
                            try{
                                provider = (Provider) model.getFactory().create(pc, JAXWSQName.PROVIDER.getQName());
                                model.startTransaction();
                                provider.setEnabled(true);
                                pc.setProvider(provider);
                                wsdlDirty = true;
                            } finally{
                                model.endTransaction();
                            }
                        }
                    } else{ //provider is not selected, remove the Provider element
                        if(provider != null){
                            try{
                                model.startTransaction();
                                pc.removeProvider(provider);
                                //if there are no more children, remove PortCustomization
                                if(pc.getChildren().size() == 0){
                                    port.removeExtensibilityElement(pc);
                                }
                                wsdlDirty = true;
                            } finally{
                                model.endTransaction();
                            }
                        }
                    }
                } else{  //no port customization
                    //if provider is set, create extensibility element and add Provider
                    if(isProvider()){
                        WSDLComponentFactory factory = model.getFactory();
                        PortCustomization pc = (PortCustomization) factory.create(port,
                                JAXWSQName.BINDINGS.getQName());
                        Provider provider = (Provider) factory.create(pc, JAXWSQName.PROVIDER.getQName());
                        try{
                            model.startTransaction();
                            provider.setEnabled(true);
                            pc.setProvider(provider);
                            port.addExtensibilityElement(pc);
                            wsdlDirty = true;
                        } finally{
                            model.endTransaction();
                        }
                    } 
                }
            } else if(jComponent == portAccessMethodText
                    || jComponent == defaultMethodCB ){
                String text = portAccessMethodText.getText();
                if(text != null && !text.trim().equals("")
                && !defaultMethodCB.isSelected()){ //Java method was specified
                    if(!JaxWsUtils.isJavaIdentifier(text)){
                        return;
                    }
                    if(ee.size() == 1){  //there is existing extensibility element
                        PortCustomization pc = ee.get(0);
                        JavaMethod jm = pc.getJavaMethod();
                        if(jm == null){ //no JavaMethod
                            try{
                                jm = (JavaMethod) model.getFactory().create(pc, JAXWSQName.METHOD.getQName());
                                model.startTransaction();
                                jm.setName(text); //TODO Need to validate this before setting it
                                pc.setJavaMethod(jm);
                                wsdlDirty = true;
                            } finally{
                                model.endTransaction();
                            }
                        } else{ //javamethod already exists
                            //reset the JavaMethod
                            try{
                                model.startTransaction();
                                jm.setName(text);
                                wsdlDirty = true;
                            }finally{
                                model.endTransaction();
                            }
                        }
                    }else{  //there is no ExtensibilityElement
                        //create extensibility element and add JavaMethod
                        WSDLComponentFactory factory = model.getFactory();
                        PortCustomization pc = (PortCustomization) factory.create(port,
                                JAXWSQName.BINDINGS.getQName());
                        JavaMethod jm = (JavaMethod) factory.create(pc, JAXWSQName.METHOD.getQName());
                        try{
                            model.startTransaction();
                            jm.setName(text);
                            pc.setJavaMethod(jm);
                            port.addExtensibilityElement(pc);
                            
                            wsdlDirty = true;
                        }finally{
                            model.endTransaction();
                        }
                    }
                } else{ //text is empty, use default
                    if(ee.size() == 1){
                        PortCustomization pc = ee.get(0);
                        JavaMethod jm = pc.getJavaMethod();
                        if(jm != null){
                            try{
                                model.startTransaction();
                                pc.removeJavaMethod(jm);
                                //if there are no more children, remove PortCustomization
                                if(pc.getChildren().size() == 0){
                                    port.removeExtensibilityElement(pc);
                                }
                                wsdlDirty = true;
                            }finally{
                                model.endTransaction();
                            }
                        }
                    }
                }
            }
        }
        catch(IllegalStateException ex){
            Exceptions.attachSeverity(ex, Level.WARNING);
            Exceptions.printStackTrace(ex);
        }
    }
    
    public void documentChanged(JTextComponent comp, String val) {
        if(comp == portAccessMethodText){
            if(!JaxWsUtils.isJavaIdentifier(val)){
                getSectionView().getErrorPanel().
                        setError(new Error(Error.TYPE_FATAL,
                        Error.ERROR_MESSAGE, val, comp));
                return;
            }
        }
        getSectionView().getErrorPanel().clearError();
    }
    
    public void rollbackValue(JTextComponent source) {
        if(source == portAccessMethodText){
            String methodName = "";
            List <PortCustomization> ee =
                    port.getExtensibilityElements(PortCustomization.class);
            if(ee.size() == 1){
                PortCustomization pc = ee.get(0);
                JavaMethod jm = pc.getJavaMethod();
                if(jm != null){
                    methodName = jm.getName();
                }
            }
            portAccessMethodText.setText(methodName);
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

        portAccessLabel = new javax.swing.JLabel();
        portAccessMethodText = new javax.swing.JTextField();
        providerCB = new javax.swing.JCheckBox();
        defaultMethodCB = new javax.swing.JCheckBox();
        serviceLabel = new javax.swing.JLabel();
        serviceName = new javax.swing.JLabel();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle"); // NOI18N
        portAccessLabel.setText(bundle.getString("LBL_PORT_ACCESS_METHOD")); // NOI18N

        portAccessMethodText.setToolTipText(bundle.getString("TOOLTIP_GET_PORT")); // NOI18N

        providerCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_USE_PROVIDER").charAt(0));
        providerCB.setText(bundle.getString("LBL_USE_PROVIDER")); // NOI18N
        providerCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        providerCB.setContentAreaFilled(false);
        providerCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        defaultMethodCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_USE_DEFAULT").charAt(0));
        defaultMethodCB.setText(bundle.getString("LBL_USE_DEFAULT")); // NOI18N
        defaultMethodCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        defaultMethodCB.setContentAreaFilled(false);
        defaultMethodCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        serviceLabel.setText(bundle.getString("LBL_ENCLOSING_SERVICE")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(serviceLabel)
                            .addComponent(portAccessLabel))
                        .addGap(14, 14, 14)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(portAccessMethodText, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(20, 20, 20)
                                .addComponent(defaultMethodCB))
                            .addComponent(serviceName, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(providerCB))
                .addContainerGap(77, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serviceLabel)
                    .addComponent(serviceName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(portAccessLabel)
                    .addComponent(portAccessMethodText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(defaultMethodCB))
                .addGap(21, 21, 21)
                .addComponent(providerCB)
                .addGap(19, 19, 19))
        );

        portAccessLabel.getAccessibleContext().setAccessibleName(bundle.getString("LBL_PORT_ACCESS_METHOD")); // NOI18N
        portAccessMethodText.getAccessibleContext().setAccessibleName(bundle.getString("LBL_PORT_ACCESS_METHOD")); // NOI18N
        portAccessMethodText.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_PORT_ACCESS_METHOD")); // NOI18N
        providerCB.getAccessibleContext().setAccessibleName(bundle.getString("LBL_USE_PROVIDER")); // NOI18N
        providerCB.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_USE_PROVIDER")); // NOI18N
        defaultMethodCB.getAccessibleContext().setAccessibleName(bundle.getString("LBL_USE_DEFAULT")); // NOI18N
        defaultMethodCB.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_USE_DEFAULT")); // NOI18N
        serviceLabel.getAccessibleContext().setAccessibleName(bundle.getString("LBL_ENCLOSING_SERVICE")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox defaultMethodCB;
    private javax.swing.JLabel portAccessLabel;
    private javax.swing.JTextField portAccessMethodText;
    private javax.swing.JCheckBox providerCB;
    private javax.swing.JLabel serviceLabel;
    private javax.swing.JLabel serviceName;
    // End of variables declaration//GEN-END:variables
    
}
