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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.netbeans.modules.websvc.wsitconf.ui.client;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.wsitconf.api.WSITConfigProvider;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityCheckerRegistry;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.websvc.wsstack.api.WSStackVersion;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 */
public class STSClientPanel extends SectionInnerPanel {

    private Node node;
    private Binding binding;
    private boolean inSync = false;
    private JaxWsModel jaxwsmodel;
    private Project project;

    public STSClientPanel(SectionView view, Node node, Binding binding, JaxWsModel jaxWsModel) {
        super(view);
        this.node = node;
        this.binding = binding;
        this.jaxwsmodel = jaxWsModel;

        FileObject fo = node.getLookup().lookup(FileObject.class);
        if (fo != null) {
            project = FileOwnerQuery.getOwner(fo);
        }

        initComponents();

        /* issue 232988: the background color issues with dark metal L&F
        endpointLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        endpointTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        metadataLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        metadataField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        namespaceLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        namespaceTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        portNameLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        portNameTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        serviceNameLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        serviceNameTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        wsdlLocationLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        wsdlLocationTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        trustVersionLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        trustVersionCombo.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        shareTokenChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        */
        shareTokenChBox.setToolTipText(NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_ShareToken_Tooltip"));

        inSync = true;
        trustVersionCombo.addItem(ComboConstants.TRUST_10);
        trustVersionCombo.addItem(ComboConstants.TRUST_13);
        inSync = false;
        
        addImmediateModifier(endpointTextField);
        addImmediateModifier(namespaceTextField);
        addImmediateModifier(portNameTextField);
        addImmediateModifier(serviceNameTextField);
        addImmediateModifier(wsdlLocationTextField);
        addImmediateModifier(metadataField);
        addImmediateModifier(trustVersionCombo);
        addImmediateModifier(shareTokenChBox);

        sync();
    }

    public void sync() {
        inSync = true;

        String endpoint = ProprietarySecurityPolicyModelHelper.getPreSTSEndpoint(binding);
        if (endpoint != null) {
            setEndpoint(endpoint);
        }

        String metadata = ProprietarySecurityPolicyModelHelper.getPreSTSMetadata(binding);
        if (metadata != null) {
            setMetadata(metadata);
        }
        
        String namespace = ProprietarySecurityPolicyModelHelper.getPreSTSNamespace(binding);
        if (namespace != null) {
            setNamespace(namespace);
        } 

        String portName = ProprietarySecurityPolicyModelHelper.getPreSTSPortName(binding);
        if (portName != null) {
            setPortName(portName);
        } 

        String serviceName = ProprietarySecurityPolicyModelHelper.getPreSTSServiceName(binding);
        if (serviceName != null) {
            setServiceName(serviceName);
        } 

        String wsdlLocation = ProprietarySecurityPolicyModelHelper.getPreSTSWsdlLocation(binding);
        if (wsdlLocation != null) {
            setWsdlLocation(wsdlLocation);
        } 
        
        String wstVersion = ProprietarySecurityPolicyModelHelper.getPreSTSWstVersion(binding);
        if (wstVersion != null) {
            setWstVersion(wstVersion);
        }
        
        boolean shareToken = ProprietarySecurityPolicyModelHelper.isPreSTSShareToken(binding);
        setShareToken(shareToken);

        inSync = false;
    }

    private String getEndpoint() {
        return this.endpointTextField.getText();
    }

    private void setEndpoint(String url) {
        this.endpointTextField.setText(url);
    }

    private String getMetadata() {
        return this.metadataField.getText();
    }

    private void setMetadata(String url) {
        this.metadataField.setText(url);
    }

    private boolean isShareToken() {
        return this.shareTokenChBox.isSelected();
    }

    private void setShareToken(boolean shareToken) {
        this.shareTokenChBox.setSelected(shareToken);
    }

    private String getNamespace() {
        return this.namespaceTextField.getText();
    }

    private void setNamespace(String ns) {
        this.namespaceTextField.setText(ns);
    }
    
    private String getServiceName() {
        return this.serviceNameTextField.getText();
    }

    private void setServiceName(String sname) {
        this.serviceNameTextField.setText(sname);
    }
    
    private String getPortName() {
        return this.portNameTextField.getText();
    }

    private void setPortName(String pname) {
        this.portNameTextField.setText(pname);
    }

    private String getWsdlLocation() {
        return this.wsdlLocationTextField.getText();
    }

    private void setWsdlLocation(String wsdlLocation) {
        this.wsdlLocationTextField.setText(wsdlLocation);
    }

    private String getWstVersion() {
        if (ComboConstants.TRUST_13.equals(trustVersionCombo.getSelectedItem())) {
            return ComboConstants.TRUST_13_POLICYSTR;
        } else {
            return ComboConstants.TRUST_10_POLICYSTR;
        }
    }

    private void setWstVersion(String wstVersion) {        
        if (ComboConstants.TRUST_13_POLICYSTR.equals(wstVersion)) {
            trustVersionCombo.setSelectedItem(ComboConstants.TRUST_13);
        } else {
            trustVersionCombo.setSelectedItem(ComboConstants.TRUST_10);
        }
    }
    
    @Override
    public void setValue(javax.swing.JComponent source, Object value) {
        if (!inSync) {

            Util.checkMetroLibrary(project);

            if (source.equals(endpointTextField)) {
                String endpoint = getEndpoint();
                if ((endpoint != null) && (endpoint.length() == 0)) {
                    ProprietarySecurityPolicyModelHelper.setPreSTSEndpoint(binding, null);
                } else {
                    ProprietarySecurityPolicyModelHelper.setPreSTSEndpoint(binding, endpoint);
                }
            }

            if (source.equals(metadataField)) {
                String metad = getMetadata();
                if ((metad != null) && (metad.length() == 0)) {
                    ProprietarySecurityPolicyModelHelper.setPreSTSMetadata(binding, null);
                } else {
                    ProprietarySecurityPolicyModelHelper.setPreSTSMetadata(binding, metad);
                }
            }

            if (source.equals(namespaceTextField)) {
                String ns = getNamespace();
                if ((ns != null) && (ns.length() == 0)) {
                    ProprietarySecurityPolicyModelHelper.setPreSTSNamespace(binding, null);
                } else {
                    ProprietarySecurityPolicyModelHelper.setPreSTSNamespace(binding, ns);
                }
            }

            if (source.equals(serviceNameTextField)) {
                String sname = getServiceName();
                if ((sname != null) && (sname.length() == 0)) {
                    ProprietarySecurityPolicyModelHelper.setPreSTSServiceName(binding, null);
                } else {
                    ProprietarySecurityPolicyModelHelper.setPreSTSServiceName(binding, sname);
                }
            }

            if (source.equals(portNameTextField)) {
                String pname = getPortName();
                if ((pname != null) && (pname.length() == 0)) {
                    ProprietarySecurityPolicyModelHelper.setPreSTSPortName(binding, null);
                } else {
                    ProprietarySecurityPolicyModelHelper.setPreSTSPortName(binding, pname);
                }
            }

            if (source.equals(wsdlLocationTextField)) {
                String wsdlLoc = getWsdlLocation();
                if ((wsdlLoc != null) && (wsdlLoc.length() == 0)) {
                    ProprietarySecurityPolicyModelHelper.setPreSTSWsdlLocation(binding, null);
                } else {
                    ProprietarySecurityPolicyModelHelper.setPreSTSWsdlLocation(binding, wsdlLoc);
                }
            }

            if (source.equals(trustVersionCombo)) {
                String version = getWstVersion();
                ProprietarySecurityPolicyModelHelper.setPreSTSWstVersion(binding, version);
            }
            
            if (source.equals(shareTokenChBox)) {                
                ProprietarySecurityPolicyModelHelper.setPreSTSShareToken(binding, isShareToken());
            }

            enableDisable();
        }
    }
    
    @Override
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        enableDisable();
    }

    @Override
    public void rollbackValue(javax.swing.text.JTextComponent source) {
    }
    
    @Override
    protected void endUIChange() {
    }

    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }

    public javax.swing.JComponent getErrorComponent(String errorId) {
        return null;
    }

    private void enableDisable() {
        
        WSStackVersion wsStackVersion = WSITConfigProvider.getDefault().getHighestWSStackVersion(project);
        
        boolean cfg20 = ConfigVersion.CONFIG_2_0.isSupported(wsStackVersion);
        boolean amSec = SecurityCheckerRegistry.getDefault().isNonWsitSecurityEnabled(node, jaxwsmodel);

        endpointLabel.setEnabled(!amSec);
        endpointTextField.setEnabled(!amSec);
        metadataField.setEnabled(!amSec);
        metadataLabel.setEnabled(!amSec);
        namespaceLabel.setEnabled(!amSec);
        namespaceLabel.setEnabled(!amSec);
        portNameLabel.setEnabled(!amSec);
        portNameTextField.setEnabled(!amSec);
        serviceNameLabel.setEnabled(!amSec);
        serviceNameTextField.setEnabled(!amSec);
        shareTokenChBox.setEnabled(!amSec && cfg20);
        shareTokenChBox.setEnabled(!amSec && cfg20);
        wsdlLocationLabel.setEnabled(!amSec);
        wsdlLocationTextField.setEnabled(!amSec);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        endpointLabel = new javax.swing.JLabel();
        wsdlLocationLabel = new javax.swing.JLabel();
        endpointTextField = new javax.swing.JTextField();
        wsdlLocationTextField = new javax.swing.JTextField();
        serviceNameLabel = new javax.swing.JLabel();
        serviceNameTextField = new javax.swing.JTextField();
        portNameLabel = new javax.swing.JLabel();
        namespaceLabel = new javax.swing.JLabel();
        portNameTextField = new javax.swing.JTextField();
        namespaceTextField = new javax.swing.JTextField();
        metadataLabel = new javax.swing.JLabel();
        metadataField = new javax.swing.JTextField();
        trustVersionLabel = new javax.swing.JLabel();
        trustVersionCombo = new javax.swing.JComboBox();
        shareTokenChBox = new javax.swing.JCheckBox();

        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });
        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                formAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        endpointLabel.setLabelFor(endpointTextField);
        org.openide.awt.Mnemonics.setLocalizedText(endpointLabel, org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_Endpoint")); // NOI18N
        endpointLabel.setToolTipText("The maximum number of seconds the time stamp remains valid.");

        wsdlLocationLabel.setLabelFor(wsdlLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(wsdlLocationLabel, org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_WsdlLocation")); // NOI18N
        wsdlLocationLabel.setToolTipText("The maximum number of seconds the sending clock can deviate from the receiving clock.");

        endpointTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        wsdlLocationTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        serviceNameLabel.setLabelFor(serviceNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(serviceNameLabel, org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_ServiceName")); // NOI18N

        serviceNameTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        portNameLabel.setLabelFor(portNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(portNameLabel, org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_PortName")); // NOI18N

        namespaceLabel.setLabelFor(namespaceTextField);
        org.openide.awt.Mnemonics.setLocalizedText(namespaceLabel, org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_Namespace")); // NOI18N

        portNameTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        namespaceTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        metadataLabel.setLabelFor(metadataField);
        org.openide.awt.Mnemonics.setLocalizedText(metadataLabel, org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_Metadata")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(trustVersionLabel, org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_PolicyVersion")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(shareTokenChBox, org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_ShareToken")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(namespaceLabel)
                            .addComponent(endpointLabel)
                            .addComponent(wsdlLocationLabel)
                            .addComponent(metadataLabel)
                            .addComponent(serviceNameLabel)
                            .addComponent(portNameLabel)
                            .addComponent(trustVersionLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(namespaceTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                            .addComponent(portNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                            .addComponent(serviceNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                            .addComponent(wsdlLocationTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                            .addComponent(endpointTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                            .addComponent(metadataField, javax.swing.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                            .addComponent(trustVersionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(shareTokenChBox))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(endpointLabel)
                    .addComponent(endpointTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(wsdlLocationLabel)
                    .addComponent(wsdlLocationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(metadataLabel)
                    .addComponent(metadataField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serviceNameLabel)
                    .addComponent(serviceNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(portNameLabel)
                    .addComponent(portNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(namespaceLabel)
                    .addComponent(namespaceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(trustVersionLabel)
                    .addComponent(trustVersionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(shareTokenChBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        endpointLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_Endpoint_ACSN")); // NOI18N
        endpointLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_Endpoint_ACSD")); // NOI18N
        wsdlLocationLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_WsdlLocation_ACSN")); // NOI18N
        wsdlLocationLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_WsdlLocation_ACSD")); // NOI18N
        serviceNameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_ServiceName_ACSN")); // NOI18N
        serviceNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_ServiceName_ACSD")); // NOI18N
        portNameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_PortName_ACSN")); // NOI18N
        portNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_PortName_ACSD")); // NOI18N
        namespaceLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_Namespace_ACSN")); // NOI18N
        namespaceLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_Namespace_ACSD")); // NOI18N
        metadataLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_Metadata_ACSN")); // NOI18N
        metadataLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(STSClientPanel.class, "LBL_STSPanel_Metadata_ACSD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
    enableDisable();
}//GEN-LAST:event_formFocusGained

private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
    enableDisable();
}//GEN-LAST:event_formAncestorAdded
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel endpointLabel;
    private javax.swing.JTextField endpointTextField;
    private javax.swing.JTextField metadataField;
    private javax.swing.JLabel metadataLabel;
    private javax.swing.JLabel namespaceLabel;
    private javax.swing.JTextField namespaceTextField;
    private javax.swing.JLabel portNameLabel;
    private javax.swing.JTextField portNameTextField;
    private javax.swing.JLabel serviceNameLabel;
    private javax.swing.JTextField serviceNameTextField;
    private javax.swing.JCheckBox shareTokenChBox;
    private javax.swing.JComboBox trustVersionCombo;
    private javax.swing.JLabel trustVersionLabel;
    private javax.swing.JLabel wsdlLocationLabel;
    private javax.swing.JTextField wsdlLocationTextField;
    // End of variables declaration//GEN-END:variables
    
}
