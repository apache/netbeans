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
package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice;

import java.util.ResourceBundle;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurityBinding;
import org.netbeans.modules.j2ee.sun.dd.api.common.PortInfo;
import org.netbeans.modules.j2ee.sun.dd.api.common.WsdlPort;
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNodeInnerPanel;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDTextFieldEditorModel;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.TextItemEditorModel;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.PortInfoNode;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.netbeans.modules.xml.multiview.ItemEditorHelper;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;


/**
 *
 * @author Peter Williams
 */
public class PortInfoPanel extends BaseSectionNodeInnerPanel {

    private final ResourceBundle webserviceBundle = NbBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice.Bundle"); // NOI18N
    
    // data model & version
    private PortInfoNode portInfoNode;
    private boolean isWebApp; // TODO replace with enum...
    private boolean isEjbJar;

    // Data storage for when these types are not selected.
    private PortInfo portInfo;
    
    // setup flag, disables listeners until addNotify()
    private boolean setup;

    // Required to reuse the old radio button code.
    private XmlMultiViewDataSynchronizer synchronizer;

    public PortInfoPanel(SectionNodeView sectionNodeView, final PortInfoNode portInfoNode, final ASDDVersion version) {
        super(sectionNodeView, version);
        this.portInfoNode = portInfoNode;
        this.setup = true;
        
        this.portInfo = (PortInfo) portInfoNode.getBinding().getSunBean();
        
        initComponents();
        initUserComponents(sectionNodeView);
    }
    
    private void initUserComponents(SectionNodeView sectionNodeView) {
        SunDescriptorDataObject dataObject = (SunDescriptorDataObject) sectionNodeView.getDataObject();
        this.synchronizer = dataObject.getModelSynchronizer();
        this.isWebApp = J2eeModule.Type.WAR.equals(dataObject.getModuleType());
        this.isEjbJar = J2eeModule.Type.EJB.equals(dataObject.getModuleType());
        
        boolean hasMessageSecurityBinding = false;

        try {
            hasMessageSecurityBinding = (portInfo.getMessageSecurityBinding() != null);
        } catch (VersionNotSupportedException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        jChkEnableMsgSecurity.setSelected(hasMessageSecurityBinding);
        enableMessageSecurityUI(hasMessageSecurityBinding);

//            if(portInfo != null) {
//                stubPropertiesPanel.setModel(selectedPortInfo, asVersion);
//                callPropertiesPanel.setModel(selectedPortInfo, asVersion);
//            } else {
//                PortInfo stub = StorageBeanFactory.getStorageBeanFactory(asVersion).createPortInfo();
//                stubPropertiesPanel.setModel(stub, asVersion);
//                callPropertiesPanel.setModel(stub, asVersion);
//            }
//        } finally {
//            selectedPortSetup = false;
//        }
        
        addRefreshable(new ItemEditorHelper(jTxtServiceEI, new PortInfoTextFieldEditorModel(synchronizer, PortInfo.SERVICE_ENDPOINT_INTERFACE)));
        addRefreshable(new ItemEditorHelper(jTxtLocalpart, new WsdlPortTextFieldEditorModel(synchronizer, WsdlPort.LOCALPART)));
        addRefreshable(new ItemEditorHelper(jTxtNamespaceURI, new WsdlPortTextFieldEditorModel(synchronizer, WsdlPort.NAMESPACEURI)));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPnlPortInfoDesc = new javax.swing.JPanel();
        jLblServiceEI = new javax.swing.JLabel();
        jTxtServiceEI = new javax.swing.JTextField();
        jLblNamespaceURI = new javax.swing.JLabel();
        jTxtNamespaceURI = new javax.swing.JTextField();
        jLblLocalpart = new javax.swing.JLabel();
        jTxtLocalpart = new javax.swing.JTextField();
        jLblEnableMsgSecurity = new javax.swing.JLabel();
        jChkEnableMsgSecurity = new javax.swing.JCheckBox();
        jBtnEditBindings = new javax.swing.JButton();

        setAlignmentX(LEFT_ALIGNMENT);
        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        jPnlPortInfoDesc.setOpaque(false);
        jPnlPortInfoDesc.setLayout(new java.awt.GridBagLayout());

        jLblServiceEI.setLabelFor(jTxtServiceEI);
        jLblServiceEI.setText(webserviceBundle.getString("LBL_ServiceEndPoint_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPnlPortInfoDesc.add(jLblServiceEI, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPnlPortInfoDesc.add(jTxtServiceEI, gridBagConstraints);
        jTxtServiceEI.getAccessibleContext().setAccessibleName(webserviceBundle.getString("ACSN_ServiceEndPoint")); // NOI18N
        jTxtServiceEI.getAccessibleContext().setAccessibleDescription(webserviceBundle.getString("ACSD_ServiceEndPoint")); // NOI18N

        jLblNamespaceURI.setLabelFor(jTxtNamespaceURI);
        jLblNamespaceURI.setText(webserviceBundle.getString("LBL_NamespaceURI_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPnlPortInfoDesc.add(jLblNamespaceURI, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPnlPortInfoDesc.add(jTxtNamespaceURI, gridBagConstraints);
        jTxtNamespaceURI.getAccessibleContext().setAccessibleName(webserviceBundle.getString("ACSN_NamespaceURI")); // NOI18N
        jTxtNamespaceURI.getAccessibleContext().setAccessibleDescription(webserviceBundle.getString("ACSD_NamespaceURI")); // NOI18N

        jLblLocalpart.setLabelFor(jTxtLocalpart);
        jLblLocalpart.setText(webserviceBundle.getString("LBL_Localpart_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPnlPortInfoDesc.add(jLblLocalpart, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPnlPortInfoDesc.add(jTxtLocalpart, gridBagConstraints);
        jTxtLocalpart.getAccessibleContext().setAccessibleName(webserviceBundle.getString("ACSN_Localpart")); // NOI18N
        jTxtLocalpart.getAccessibleContext().setAccessibleDescription(webserviceBundle.getString("ACSD_Localpart")); // NOI18N

        jLblEnableMsgSecurity.setText(webserviceBundle.getString("LBL_EnableMsgSecurity_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPnlPortInfoDesc.add(jLblEnableMsgSecurity, gridBagConstraints);

        jChkEnableMsgSecurity.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jChkEnableMsgSecurity.setOpaque(false);
        jChkEnableMsgSecurity.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jChkEnableMsgSecurityItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 12);
        jPnlPortInfoDesc.add(jChkEnableMsgSecurity, gridBagConstraints);

        jBtnEditBindings.setText(webserviceBundle.getString("LBL_EditMsgSecBindings")); // NOI18N
        jBtnEditBindings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnEditBindingsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPnlPortInfoDesc.add(jBtnEditBindings, gridBagConstraints);
        jBtnEditBindings.getAccessibleContext().setAccessibleName(webserviceBundle.getString("ACSN_EditMsgSecBindings")); // NOI18N
        jBtnEditBindings.getAccessibleContext().setAccessibleDescription(webserviceBundle.getString("ACSD_EditMsgSecBindings")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 5);
        add(jPnlPortInfoDesc, gridBagConstraints);

        getAccessibleContext().setAccessibleName(webserviceBundle.getString("ACSN_SelectedPortInfo")); // NOI18N
        getAccessibleContext().setAccessibleDescription(webserviceBundle.getString("ACSD_SelectedPortInfo")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void jBtnEditBindingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnEditBindingsActionPerformed
        if(!setup) {
            try {
                startUIChange();
                String asCloneVersion = isWebApp ? version.getWebAppVersionAsString() : 
                    (isEjbJar ? version.getEjbJarVersionAsString() : version.getAppClientVersionAsString());
                
                MessageSecurityBinding binding = portInfo.getMessageSecurityBinding();
                if(binding == null) {
                    binding = portInfo.newMessageSecurityBinding();
                    portInfo.setMessageSecurityBinding(binding);
                }
                
                EditBindingMultiview.editMessageSecurityBinding(this, true, binding, version, asCloneVersion);
            } catch (VersionNotSupportedException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            } finally {
                endUIChange();
                synchronizer.requestUpdateData();
            }
        }
    }//GEN-LAST:event_jBtnEditBindingsActionPerformed

    private void jChkEnableMsgSecurityItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jChkEnableMsgSecurityItemStateChanged
        if(!setup) {
            boolean hasMessageSecurity = Utils.interpretCheckboxState(evt);
            
            try {
                startUIChange();
                if(!hasMessageSecurity) {
                    portInfo.setMessageSecurityBinding(null);
                }
            } catch (VersionNotSupportedException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            } finally {
                endUIChange();
                synchronizer.requestUpdateData();
            }
            
            enableMessageSecurityUI(hasMessageSecurity);
        }
    }//GEN-LAST:event_jChkEnableMsgSecurityItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBtnEditBindings;
    private javax.swing.JCheckBox jChkEnableMsgSecurity;
    private javax.swing.JLabel jLblEnableMsgSecurity;
    private javax.swing.JLabel jLblLocalpart;
    private javax.swing.JLabel jLblNamespaceURI;
    private javax.swing.JLabel jLblServiceEI;
    private javax.swing.JPanel jPnlPortInfoDesc;
    private javax.swing.JTextField jTxtLocalpart;
    private javax.swing.JTextField jTxtNamespaceURI;
    private javax.swing.JTextField jTxtServiceEI;
    // End of variables declaration//GEN-END:variables

    @Override
    public void addNotify() {
        super.addNotify();
        setup = false;
    }
    
//	private void initUserComponents() {
//		/** Add stub properties table panel :
//		 *  TableEntry list has two properties: Name, Value
//		 */
//		ArrayList tableColumns = new ArrayList(2);
//		tableColumns.add(new GenericTableModel.ValueEntry("Name",				// NOI18N - property name
//			webserviceBundle.getString("LBL_Name_Column"), true));	// NOI18N
//		tableColumns.add(new GenericTableModel.ValueEntry("Value",				// NOI18N - property name
//			webserviceBundle.getString("LBL_Value_Column"), true));	// NOI18N
//
//        stubPropertiesModel = new GenericTableModel(PortInfo.STUB_PROPERTY,
//			stubPropertyFactory, tableColumns);
//        stubPropertiesModel.addTableModelListener(this);
//		stubPropertiesPanel = new GenericTablePanel(stubPropertiesModel,
//			webserviceBundle, "StubProperties",	// NOI18N - property name
//			DynamicPropertyPanel.class, HelpContext.HELP_SERVICE_PORT_STUB_PROPERTY_POPUP,
//			PropertyListMapping.getPropertyList(PropertyListMapping.SERVICE_REF_STUB_PROPERTIES));
//
//        GridBagConstraints gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
//        gridBagConstraints.fill = GridBagConstraints.BOTH;
//        gridBagConstraints.weightx = 1.0;
//        gridBagConstraints.weighty = 1.0;
//		gridBagConstraints.insets = new Insets(0, 6, 0, 5);
//		add(stubPropertiesPanel, gridBagConstraints);
//
//		/** Add call properties table panel :
//		 *  Uses same TableEntry list as stubProperties
//		 */
//        callPropertiesModel = new GenericTableModel(PortInfo.CALL_PROPERTY,
//			callPropertyFactory, tableColumns);
//        callPropertiesModel.addTableModelListener(this);
//		callPropertiesPanel = new GenericTablePanel(callPropertiesModel,
//			webserviceBundle, "CallProperties",	// NOI18N - property name
//			DynamicPropertyPanel.class, HelpContext.HELP_SERVICE_PORT_CALL_PROPERTY_POPUP,
//			PropertyListMapping.getPropertyList(PropertyListMapping.SERVICE_REF_CALL_PROPERTIES));
//
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
//        gridBagConstraints.fill = GridBagConstraints.BOTH;
//        gridBagConstraints.weightx = 1.0;
//        gridBagConstraints.weighty = 1.0;
//		gridBagConstraints.insets = new Insets(0, 6, 5, 5);
//		add(callPropertiesPanel, gridBagConstraints);
//	}

//    public void setPortInfoMapping(PortInfoMapping piMap) {
//        try {
//            ASDDVersion asVersion = masterPanel.getBean().getAppServerVersion();
//            selectedPortSetup = true;
//
//            ...
//
//            if(selectedPortInfo != null) {
//                stubPropertiesPanel.setModel(selectedPortInfo, asVersion);
//                callPropertiesPanel.setModel(selectedPortInfo, asVersion);
//            } else {
//                PortInfo stub = StorageBeanFactory.getStorageBeanFactory(asVersion).createPortInfo();
//                stubPropertiesPanel.setModel(stub, asVersion);
//                callPropertiesPanel.setModel(stub, asVersion);
//            }
//        } finally {
//            selectedPortSetup = false;
//        }
//    }

    private void enableMessageSecurityUI(boolean enable) {
        jBtnEditBindings.setEnabled(enable);
    }

    public String getHelpId() {
        return "AS_CFG_ServiceRefPortInfo"; // NOI18N
    }
    
//    // New for migration to sun DD API model.  Factory instance to pass to generic table model
//    // to allow it to create callProperty and stubProperty beans.
//	private static GenericTableModel.ParentPropertyFactory stubPropertyFactory =
//        new GenericTableModel.ParentPropertyFactory() {
//            public CommonDDBean newParentProperty(ASDDVersion asVersion) {
//                return StorageBeanFactory.getStorageBeanFactory(asVersion).createStubProperty();
//            }
//        };
//
//    private static GenericTableModel.ParentPropertyFactory callPropertyFactory =
//        new GenericTableModel.ParentPropertyFactory() {
//            public CommonDDBean newParentProperty(ASDDVersion asVersion) {
//                return StorageBeanFactory.getStorageBeanFactory(asVersion).createCallProperty();
//            }
//        };
    
    // Model class for handling updates to the text fields
    private class PortInfoTextFieldEditorModel extends DDTextFieldEditorModel {

        public PortInfoTextFieldEditorModel(XmlMultiViewDataSynchronizer synchronizer, String propertyName) {
            super(synchronizer, propertyName);
        }
        
        public PortInfoTextFieldEditorModel(XmlMultiViewDataSynchronizer synchronizer, String propertyName, String attributeName) {
            super(synchronizer, propertyName, attributeName);
        }

        @Override
        protected CommonDDBean getBean() {
            return portInfo;
        }
        
    }

    private class WsdlPortTextFieldEditorModel extends TextItemEditorModel {

        private String propertyName;
        
        public WsdlPortTextFieldEditorModel(XmlMultiViewDataSynchronizer synchronizer, String propertyName) {
            super(synchronizer, true, true);
            
            this.propertyName = propertyName;
        }
        
        @Override
        protected String getValue() {
            WsdlPort wp = portInfo.getWsdlPort();
            return (wp != null) ? (String) wp.getValue(propertyName) : null;
        }
        
        @Override
        protected void setValue(String value) {
            WsdlPort wp = portInfo.getWsdlPort();
            if(wp == null) {
                wp = portInfo.newWsdlPort();
                portInfo.setWsdlPort(wp);
            }

            wp.setValue(propertyName, value);

            if(isEmpty(wp)) {
                portInfo.setWsdlPort(null);
            }
            
            // If this was a virtual bean, commit it to the graph.
            if(portInfoNode.addVirtualBean()) {
                // update if necessary
            }
        }
    }
    
    private static boolean isEmpty(WsdlPort wp) {
        return Utils.strEmpty(wp.getLocalpart()) && 
                Utils.strEmpty(wp.getNamespaceURI());
    }    
}
