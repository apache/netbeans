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

package org.netbeans.modules.websvc.wsitconf.ui.service.subpanels;

import java.text.NumberFormat;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import org.netbeans.modules.xml.wsdl.model.Binding;

import javax.swing.*;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;

/**
 *
 * @author Martin Grebac
 */
public class AdvancedSecurityPanel extends JPanel {

    private Binding binding;
    private boolean inSync = false;

    private DefaultFormatterFactory freshnessff = null;
    private DefaultFormatterFactory skewff = null;
    
    private ConfigVersion cfgVersion = null;
    
    public AdvancedSecurityPanel(Binding binding, ConfigVersion cfgVersion) {
        this.binding = binding;
        this.cfgVersion = cfgVersion;
        
        freshnessff = new DefaultFormatterFactory();
        NumberFormat freshnessFormat = NumberFormat.getIntegerInstance();
        freshnessFormat.setGroupingUsed(false);
        NumberFormatter freshnessFormatter = new NumberFormatter(freshnessFormat);
        freshnessFormat.setMaximumIntegerDigits(8);
        freshnessFormatter.setCommitsOnValidEdit(true);
        freshnessFormatter.setMinimum(0);
        freshnessFormatter.setMaximum(99999999);
        freshnessff.setDefaultFormatter(freshnessFormatter);
                
        skewff = new DefaultFormatterFactory();
        NumberFormat skewFormat = NumberFormat.getIntegerInstance();
        skewFormat.setGroupingUsed(false);
        NumberFormatter skewFormatter = new NumberFormatter(skewFormat);
        skewFormat.setMaximumIntegerDigits(8);
        skewFormatter.setCommitsOnValidEdit(true);
        skewFormatter.setMinimum(0);
        skewFormatter.setMaximum(99999999);
        skewff.setDefaultFormatter(skewFormatter);

        initComponents();
        
        sync();
    }

    private void sync() {
        inSync = true;
        
        String maxClockSkew = ProprietarySecurityPolicyModelHelper.getMaxClockSkew(binding);
        if (maxClockSkew == null) { // no setup exists yet - set the default
            setMaxClockSkew(ProprietarySecurityPolicyModelHelper.DEFAULT_MAXCLOCKSKEW);
        } else {
            setMaxClockSkew(maxClockSkew);
        } 

        String freshnessLimit = ProprietarySecurityPolicyModelHelper.getTimestampFreshness(binding);
        if (freshnessLimit == null) { // no setup exists yet - set the default
            setFreshness(ProprietarySecurityPolicyModelHelper.DEFAULT_TIMESTAMPFRESHNESS);
        } else {
            setFreshness(freshnessLimit);
        } 

        setRevocation(ProprietarySecurityPolicyModelHelper.isRevocationEnabled(binding));

        enableDisable();
        inSync = false;
    }

    private Number getMaxClockSkew() {
        return (Number) this.maxClockSkewField.getValue();
    }
    
    private void setMaxClockSkew(String value) {
        this.maxClockSkewField.setText(value);
    }

    private Number getFreshness() {
        return (Number) this.freshnessField.getValue();
    }
    
    private void setFreshness(String value) {
        this.freshnessField.setText(value);
    }

    private void setRevocation(Boolean enable) {
        if (enable == null) {
            this.revocationChBox.setSelected(false);
        } else {
            this.revocationChBox.setSelected(enable);
        }
    }

    public Boolean getRevocation() {
        if (revocationChBox.isSelected()) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
        
    public void storeState() {

        ProprietarySecurityPolicyModelHelper.setRevocation(binding, revocationChBox.isSelected(), false);
        
        Number freshness = getFreshness();
        if ((freshness == null) || 
            (ProprietarySecurityPolicyModelHelper.DEFAULT_TIMESTAMPFRESHNESS.equals(freshness.toString()))) {
                ProprietarySecurityPolicyModelHelper.setTimestampFreshness(binding, null, false);
        } else {
            ProprietarySecurityPolicyModelHelper.setTimestampFreshness(binding, freshness.toString(), false);
        }

        Number skew = getMaxClockSkew();
        if ((skew == null) || (ProprietarySecurityPolicyModelHelper.DEFAULT_MAXCLOCKSKEW.equals(skew.toString()))) {
            ProprietarySecurityPolicyModelHelper.setMaxClockSkew(binding, null, false);
        } else {
            ProprietarySecurityPolicyModelHelper.setMaxClockSkew(binding, skew.toString(), false);
        }

    }
    
    private void enableDisable() {
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        maxClockSkewLabel = new javax.swing.JLabel();
        freshnessLabel = new javax.swing.JLabel();
        freshnessField = new javax.swing.JFormattedTextField();
        maxClockSkewField = new javax.swing.JFormattedTextField();
        revocationChBox = new javax.swing.JCheckBox();

        maxClockSkewLabel.setText(org.openide.util.NbBundle.getMessage(AdvancedSecurityPanel.class, "LBL_AdvancedSec_maxClockSkew")); // NOI18N

        freshnessLabel.setText(org.openide.util.NbBundle.getMessage(AdvancedSecurityPanel.class, "LBL_AdvancedSec_TimestampFreshnessLabel")); // NOI18N

        freshnessField.setFormatterFactory(freshnessff);

        maxClockSkewField.setColumns(8);
        maxClockSkewField.setFormatterFactory(skewff);

        revocationChBox.setText(org.openide.util.NbBundle.getMessage(AdvancedSecurityPanel.class, "LBL_AdvancedSec_Revocation")); // NOI18N
        revocationChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        revocationChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(freshnessLabel)
                            .addComponent(maxClockSkewLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(maxClockSkewField, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                            .addComponent(freshnessField, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)))
                    .addComponent(revocationChBox))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxClockSkewLabel)
                    .addComponent(maxClockSkewField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(freshnessLabel)
                    .addComponent(freshnessField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(revocationChBox)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField freshnessField;
    private javax.swing.JLabel freshnessLabel;
    private javax.swing.JFormattedTextField maxClockSkewField;
    private javax.swing.JLabel maxClockSkewLabel;
    private javax.swing.JCheckBox revocationChBox;
    // End of variables declaration//GEN-END:variables
    
}
