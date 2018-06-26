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

/*
 * ServicePanel.java
 *
 * Created on February 19, 2006, 8:55 AM
 */

package org.netbeans.modules.websvc.customization.multiview;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.api.customization.model.CustomizationComponentFactory;
import org.netbeans.modules.websvc.api.customization.model.JavaClass;
import org.netbeans.modules.websvc.api.customization.model.ServiceCustomization;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.WeakListeners;
import org.netbeans.modules.xml.multiview.Error;

/**
 *
 * @author  Roderico Cruz
 */
public class ServicePanel extends SaveableSectionInnerPanel {
    private Service service;
    private WSDLModel model;
    private boolean wsdlDirty;
    private DefaultItemListener itemListener;
    
    /** Creates new form ServicePanel */
    public ServicePanel(SectionView view, Service service){
        super(view);
        this.service = service;
        this.model = this.service.getModel();
        initComponents();
        disableEnterKey();
        sync();
        
        itemListener = new DefaultItemListener();
        ItemListener il = (ItemListener)WeakListeners.create(ItemListener.class,
                itemListener,defaultJavaClassCB);
        defaultJavaClassCB.addItemListener(il);
        
        addModifier(javaClassText);
        addModifier(defaultJavaClassCB);
        addValidatee(javaClassText);
    }
    
    class DefaultItemListener implements ItemListener{
        public void itemStateChanged(ItemEvent e) {
            if(defaultJavaClassCB.isSelected()){
                javaClassText.setEnabled(false);
            } else{
                javaClassText.setEnabled(true);
                javaClassText.requestFocus();
            }
        }
    }
    
    private void sync(){
        List<ServiceCustomization> ee = service.getExtensibilityElements(ServiceCustomization.class);
        if(ee.size() > 0 ){
            ServiceCustomization sc = ee.get(0);
            JavaClass jc = sc.getJavaClass();
            if(jc != null){
                setJavaClass(jc.getName());
            } else{
                defaultJavaClassCB.setSelected(true);
                javaClassText.setEnabled(false);
            }
        } else{
            defaultJavaClassCB.setSelected(true);
            javaClassText.setEnabled(false);
        }
    }
    
    public void setJavaClass(String name){
        javaClassText.setText(name);
    }
    
    public JComponent getErrorComponent(String string) {
        return new javax.swing.JButton("error");
    }
    
    public void linkButtonPressed(Object object, String string) {
    }
    
    public void setValue(JComponent jComponent, Object object) {
        List <ServiceCustomization> ee =
                service.getExtensibilityElements(ServiceCustomization.class);
        CustomizationComponentFactory factory = CustomizationComponentFactory.getDefault();
        if(jComponent == javaClassText ||
                jComponent == defaultJavaClassCB ){
            String text = javaClassText.getText();
            if(text != null && !text.trim().equals("") &&
                    !defaultJavaClassCB.isSelected()){
                if(!JaxWsUtils.isJavaIdentifier(text)){
                    return;
                }
                if(ee.size() == 1){  //there is existing extensibility element
                    ServiceCustomization sc = ee.get(0);
                    JavaClass jc = sc.getJavaClass();
                    if(jc == null){
                        try{
                            jc = factory.createJavaClass(model);
                            model.startTransaction();
                            jc.setName(text); //TODO Need to validate this before setting it
                            sc.setJavaClass(jc);
                            wsdlDirty = true;
                        } finally{
                                model.endTransaction();
                        }
                    } else{ //javamethod already exists
                        //reset the JavaMethod
                        try{
                            model.startTransaction();
                            jc.setName(text);
                            wsdlDirty = true;
                        } finally{
                                model.endTransaction();
                        }
                    }
                    
                } else{  //there is no ExtensibilityElement
                    //create extensibility element and add JavaClass
                    ServiceCustomization sc = factory.createServiceCustomization(model);
                    JavaClass jc = factory.createJavaClass(model);
                    try{
                        model.startTransaction();
                        jc.setName(text);
                        sc.setJavaClass(jc);
                        service.addExtensibilityElement(sc);
                        wsdlDirty = true;
                    } finally{
                            model.endTransaction();
                    }
                }
            } else{ //text field is empty, no JavaClass customization
                if(ee.size() == 1){
                    ServiceCustomization sc = ee.get(0);
                    JavaClass jc = sc.getJavaClass();
                    if(jc != null){
                        try{
                            model.startTransaction();
                            sc.removeJavaClass(jc);
                            //if sc has no more children, remove it as well
                            if(sc.getChildren().size() == 0){
                                service.removeExtensibilityElement(sc);
                            }
                            wsdlDirty = true;
                        } finally{
                                model.endTransaction();
                        }
                    }
                }
            }
        }
    }
    
    public void documentChanged(JTextComponent comp, String val) {
        if(comp == javaClassText){
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
        if(source == javaClassText){
            String className = "";
            List <ServiceCustomization> ee =
                    service.getExtensibilityElements(ServiceCustomization.class);
            if(ee.size() == 1){
                ServiceCustomization sc = ee.get(0);
                JavaClass jc = sc.getJavaClass();
                if(jc != null){
                    className = jc.getName();
                }
            }
            javaClassText.setText(className);
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

        javaClassLabel = new javax.swing.JLabel();
        javaClassText = new javax.swing.JTextField();
        defaultJavaClassCB = new javax.swing.JCheckBox();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle"); // NOI18N
        javaClassLabel.setText(bundle.getString("LBL_JAVA_CLASS")); // NOI18N

        javaClassText.setToolTipText(bundle.getString("TOOLTIP_SERVICE_CLASS")); // NOI18N

        defaultJavaClassCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_USE_DEFAULT").charAt(0));
        defaultJavaClassCB.setText(bundle.getString("LBL_USE_DEFAULT")); // NOI18N
        defaultJavaClassCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        defaultJavaClassCB.setContentAreaFilled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(javaClassLabel)
                .addGap(21, 21, 21)
                .addComponent(javaClassText, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(defaultJavaClassCB)
                .addContainerGap(70, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(javaClassLabel)
                    .addComponent(javaClassText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(defaultJavaClassCB))
                .addContainerGap(29, Short.MAX_VALUE))
        );

        javaClassLabel.getAccessibleContext().setAccessibleName(bundle.getString("LBL_JAVA_CLASS")); // NOI18N
        javaClassText.getAccessibleContext().setAccessibleName(bundle.getString("LBL_JAVA_CLASS")); // NOI18N
        javaClassText.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_JAVA_CLASS")); // NOI18N
        defaultJavaClassCB.getAccessibleContext().setAccessibleName(bundle.getString("LBL_USE_DEFAULT")); // NOI18N
        defaultJavaClassCB.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_USE_DEFAULT")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox defaultJavaClassCB;
    private javax.swing.JLabel javaClassLabel;
    private javax.swing.JTextField javaClassText;
    // End of variables declaration//GEN-END:variables
    
}
