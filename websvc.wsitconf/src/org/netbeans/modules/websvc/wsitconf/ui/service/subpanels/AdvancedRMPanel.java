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

package org.netbeans.modules.websvc.wsitconf.ui.service.subpanels;

import java.text.NumberFormat;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import javax.swing.GroupLayout;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMModelHelper;
import org.netbeans.modules.xml.wsdl.model.Binding;
import javax.swing.*;
import org.netbeans.modules.websvc.wsitconf.spi.SaveablePanel;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMDeliveryAssurance;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMSequenceBinding;

/**
 *
 * @author Martin Grebac
 */
public class AdvancedRMPanel extends JPanel implements SaveablePanel {

    private Binding binding;
    private boolean inSync = false;

    private DefaultFormatterFactory milisecondsff = null;
    private DefaultFormatterFactory maxBufff = null;
    
    private ConfigVersion cfgVersion = null;
    
    public AdvancedRMPanel(Binding binding, ConfigVersion cfgVersion) {
        super();
        this.binding = binding;
        this.cfgVersion = cfgVersion;

        milisecondsff = new DefaultFormatterFactory();
        NumberFormat millisecondsFormat = NumberFormat.getIntegerInstance();        
        millisecondsFormat.setGroupingUsed(false);
        NumberFormatter millisecondsFormatter = new NumberFormatter(millisecondsFormat);
        millisecondsFormat.setMaximumIntegerDigits(8);
        millisecondsFormatter.setCommitsOnValidEdit(true);
        millisecondsFormatter.setMinimum(0);
        millisecondsFormatter.setMaximum(99999999);
        milisecondsff.setDefaultFormatter(millisecondsFormatter);

        maxBufff = new DefaultFormatterFactory();
        NumberFormat maxBufFormat = NumberFormat.getIntegerInstance();
        maxBufFormat.setGroupingUsed(false);
        NumberFormatter maxBufFormatter = new NumberFormatter(maxBufFormat);
        maxBufFormat.setMaximumIntegerDigits(8);
        maxBufFormatter.setCommitsOnValidEdit(true);
        maxBufFormatter.setMinimum(0);
        maxBufFormatter.setMaximum(99999999);
        maxBufff.setDefaultFormatter(maxBufFormatter);

        initComponents();

        inSync = true;
        for (RMDeliveryAssurance assurance : RMDeliveryAssurance.values()) {
            deliveryAssuranceCombo.addItem(assurance);
        }
        inSync = false;
        
        sync();
        refresh();
    }

    private void sync() {
        inSync = true;
        
        String inactivityTimeout = RMModelHelper.getInstance(cfgVersion).getInactivityTimeout(binding);
        if (inactivityTimeout == null) { // no setup exists yet - set the default
            setTextField(inactivityTimeoutTextfield, RMModelHelper.DEFAULT_INACT_TIMEOUT);
        } else {
            setTextField(inactivityTimeoutTextfield, inactivityTimeout);
        } 
        
        String maxRcvBufferSize = RMModelHelper.getMaxReceiveBufferSize(binding);
        if (maxRcvBufferSize == null) { // no setup exists yet - set the default
            setTextField(maxBufTextField, RMModelHelper.DEFAULT_MAXRCVBUFFERSIZE);
        } else {
            setTextField(maxBufTextField, maxRcvBufferSize);
        } 

        setChBox(flowControlChBox, RMModelHelper.isFlowControl(binding));

        RMDeliveryAssurance assurance = RMDeliveryAssurance.getValue(cfgVersion, binding);
        if (assurance == null) {
            assurance = RMDeliveryAssurance.getDefault();
        }
        setCombo(deliveryAssuranceCombo, assurance);
        
        RMSequenceBinding seq = RMSequenceBinding.getValue(cfgVersion, binding);
        if (seq == null) {
            seq = RMSequenceBinding.getDefault();
        }

        enableDisable();
        inSync = false;
    }

    protected void setCombo(JComboBox combo, Object item) {
        if (item == null) {
            combo.setSelectedIndex(0);
        } else {
            combo.setSelectedItem(item);
        }
    }
    
    // max receive buffer size
    private Number getTextField(JFormattedTextField textField) {
        return (Number) textField.getValue();
    }
    
    private void setTextField(JFormattedTextField field, String value) {
        field.setText(value);
    }

    protected void setChBox(JCheckBox chBox, Boolean enable) {
        if (enable == null) {
            chBox.setSelected(false);
        } else {
            chBox.setSelected(enable);
        }
    }
    
    public void storeState() {

        boolean flowControl = flowControlChBox.isSelected();
        if (flowControl != RMModelHelper.isFlowControl(binding)) {
            RMModelHelper.getInstance(cfgVersion).enableFlowControl(binding, flowControl);
        }

        Number timeout = getTextField(inactivityTimeoutTextfield);
        if ((timeout == null) || (RMModelHelper.DEFAULT_INACT_TIMEOUT.equals(timeout.toString()))) {
            RMModelHelper.getInstance(cfgVersion).setInactivityTimeout(binding, null);
        } else {
            RMModelHelper.getInstance(cfgVersion).setInactivityTimeout(binding, timeout.toString());
        }

        Number bufSize = getTextField(maxBufTextField);
        if ((bufSize == null) || (RMModelHelper.DEFAULT_MAXRCVBUFFERSIZE.equals(bufSize.toString()))) {
            RMModelHelper.setMaxReceiveBufferSize(binding, null);
        } else {
            RMModelHelper.setMaxReceiveBufferSize(binding, bufSize.toString());
        }

        ((RMDeliveryAssurance)deliveryAssuranceCombo.getSelectedItem()).set(cfgVersion, binding);        
    }
    
    private void enableDisable() {
        boolean flowSelected = flowControlChBox.isSelected();
        maxBufLabel.setEnabled(flowSelected);
        maxBufTextField.setEnabled(flowSelected);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        flowControlChBox = new javax.swing.JCheckBox();
        maxBufLabel = new javax.swing.JLabel();
        inactivityTimeoutLabel = new javax.swing.JLabel();
        inactivityTimeoutTextfield = new javax.swing.JFormattedTextField();
        maxBufTextField = new javax.swing.JFormattedTextField();
        deliveryAssuranceLabel = new javax.swing.JLabel();
        deliveryAssuranceCombo = new javax.swing.JComboBox();

        flowControlChBox.setText(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "LBL_AdvancedRM_FlowControlChBox")); // NOI18N
        flowControlChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        flowControlChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flowControlChBoxActionPerformed(evt);
            }
        });

        maxBufLabel.setText(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "LBL_AdvancedRM_maxBufLabel")); // NOI18N

        inactivityTimeoutLabel.setText(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "LBL_AdvancedRM_InactivityTimeoutLabel")); // NOI18N

        inactivityTimeoutTextfield.setFormatterFactory(milisecondsff);

        maxBufTextField.setColumns(8);
        maxBufTextField.setFormatterFactory(maxBufff);

        deliveryAssuranceLabel.setText(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "LBL_AdvancedRM_DeliveryAssurance")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(flowControlChBox)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(maxBufLabel)
                            .addComponent(inactivityTimeoutLabel)
                            .addComponent(deliveryAssuranceLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(deliveryAssuranceCombo, 0, 135, Short.MAX_VALUE)
                            .addComponent(inactivityTimeoutTextfield, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                            .addComponent(maxBufTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deliveryAssuranceLabel)
                    .addComponent(deliveryAssuranceCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(flowControlChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxBufLabel)
                    .addComponent(maxBufTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(inactivityTimeoutLabel)
                    .addComponent(inactivityTimeoutTextfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {deliveryAssuranceCombo, inactivityTimeoutTextfield, maxBufTextField});

        flowControlChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "LBL_AdvancedRM_FlowControl_ACSD")); // NOI18N
        maxBufLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "LBL_AdvancedRM_MaxFlowBufSize_ACSD")); // NOI18N
        inactivityTimeoutLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "LBL_AdvancedRM_InactTimeout_ACSD")); // NOI18N
        inactivityTimeoutTextfield.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "TXT_AdvancedRM_InactTimeout_ACSN")); // NOI18N
        inactivityTimeoutTextfield.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "TXT_AdvancedRM_InactTimeout_ACSD")); // NOI18N
        maxBufTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "TXT_AdvancedRM_MaxBuf_ACSN")); // NOI18N
        maxBufTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "TXT_AdvancedRM_MaxBuf_ACSD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void refresh() {
        javax.swing.GroupLayout layout = (GroupLayout) this.getLayout();
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(flowControlChBox)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(maxBufLabel)
                            .addComponent(inactivityTimeoutLabel)
                            .addComponent(deliveryAssuranceLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(deliveryAssuranceCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(inactivityTimeoutTextfield, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                            .addComponent(maxBufTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deliveryAssuranceLabel)
                    .addComponent(deliveryAssuranceCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(flowControlChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxBufLabel)
                    .addComponent(maxBufTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(inactivityTimeoutLabel)
                    .addComponent(inactivityTimeoutTextfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.VERTICAL, deliveryAssuranceCombo, inactivityTimeoutTextfield, maxBufTextField);

        flowControlChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "LBL_AdvancedRM_FlowControl_ACSD")); // NOI18N
        maxBufLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "LBL_AdvancedRM_MaxFlowBufSize_ACSD")); // NOI18N
        inactivityTimeoutLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "LBL_AdvancedRM_InactTimeout_ACSD")); // NOI18N
        inactivityTimeoutTextfield.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "TXT_AdvancedRM_InactTimeout_ACSN")); // NOI18N
        inactivityTimeoutTextfield.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "TXT_AdvancedRM_InactTimeout_ACSD")); // NOI18N
        maxBufTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "TXT_AdvancedRM_MaxBuf_ACSN")); // NOI18N
        maxBufTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedRMPanel.class, "TXT_AdvancedRM_MaxBuf_ACSD")); // NOI18N
        validate();
    }

    private void flowControlChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_flowControlChBoxActionPerformed
        enableDisable();
    }//GEN-LAST:event_flowControlChBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox deliveryAssuranceCombo;
    private javax.swing.JLabel deliveryAssuranceLabel;
    private javax.swing.JCheckBox flowControlChBox;
    private javax.swing.JLabel inactivityTimeoutLabel;
    private javax.swing.JFormattedTextField inactivityTimeoutTextfield;
    private javax.swing.JLabel maxBufLabel;
    private javax.swing.JFormattedTextField maxBufTextField;
    // End of variables declaration//GEN-END:variables
    
}
