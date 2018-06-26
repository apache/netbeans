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

import java.text.NumberFormat;
import javax.swing.JCheckBox;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RequiredConfigurationHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author Martin Grebac
 */
public class AdvancedConfigPanelClient extends SectionInnerPanel {

    private WSDLModel serviceModel;

    private Binding binding;
    private boolean inSync = false;

    private Project project;
    
    private static final String DEFAULT_LIFETIME = "36000";                     //NOI18N
    private static final String DEFAULT_RMRESENDINTERVAL = "2000";              //NOI18N
    private static final String DEFAULT_RMCLOSETIMEOUT = "0";                    //NOI18N
    private static final String DEFAULT_RMREQUESTACKINTERVAL = "200";           //NOI18N

    private DefaultFormatterFactory lifetimeDff = null;
    private DefaultFormatterFactory closeTimeoutDff = null;
    private DefaultFormatterFactory rmSendDff = null;
    private DefaultFormatterFactory rmReqDff = null;
    private DefaultFormatterFactory timeoutDff = null;
    private DefaultFormatterFactory freshnessDff = null;
    private DefaultFormatterFactory skewDff = null;

    public AdvancedConfigPanelClient(SectionView view, Node node, Binding binding, WSDLModel serviceModel) {
        super(view);
        this.serviceModel = serviceModel;
        this.binding = binding;

        FileObject fo = node.getLookup().lookup(FileObject.class);
        if (fo != null) {
            project = FileOwnerQuery.getOwner(fo);
        }

        lifetimeDff = new DefaultFormatterFactory();
        NumberFormat lifetimeFormat = NumberFormat.getIntegerInstance();
        lifetimeFormat.setGroupingUsed(false);
        lifetimeFormat.setParseIntegerOnly(true);
        lifetimeFormat.setMaximumFractionDigits(0);
        NumberFormatter lifetimeFormatter = new NumberFormatter(lifetimeFormat);
        lifetimeFormatter.setCommitsOnValidEdit(true);
        lifetimeFormatter.setMinimum(0);
        lifetimeDff.setDefaultFormatter(lifetimeFormatter);

        rmSendDff = new DefaultFormatterFactory();
        NumberFormat rmSendFormat = NumberFormat.getIntegerInstance();
        rmSendFormat.setGroupingUsed(false);
        rmSendFormat.setParseIntegerOnly(true);
        rmSendFormat.setMaximumFractionDigits(0);
        NumberFormatter rmSendFormatter = new NumberFormatter(rmSendFormat);
        rmSendFormatter.setCommitsOnValidEdit(true);
        rmSendFormatter.setMinimum(0);
        rmSendDff.setDefaultFormatter(rmSendFormatter);

        rmReqDff = new DefaultFormatterFactory();
        NumberFormat rmReqFormat = NumberFormat.getIntegerInstance();
        rmReqFormat.setGroupingUsed(false);
        rmReqFormat.setParseIntegerOnly(true);
        rmReqFormat.setMaximumFractionDigits(0);
        NumberFormatter rmReqFormatter = new NumberFormatter(rmReqFormat);
        rmReqFormatter.setCommitsOnValidEdit(true);
        rmReqFormatter.setMinimum(0);
        rmReqDff.setDefaultFormatter(rmReqFormatter);

        timeoutDff = new DefaultFormatterFactory();
        NumberFormat timeoutFormat = NumberFormat.getIntegerInstance();
        timeoutFormat.setGroupingUsed(false);
        timeoutFormat.setParseIntegerOnly(true);
        timeoutFormat.setMaximumFractionDigits(0);
        NumberFormatter timeoutFormatter = new NumberFormatter(timeoutFormat);
        timeoutFormatter.setCommitsOnValidEdit(true);
        timeoutFormatter.setMinimum(0);
        timeoutDff.setDefaultFormatter(timeoutFormatter);

        closeTimeoutDff = new DefaultFormatterFactory();
        NumberFormat rmCloseTimeoutFormat = NumberFormat.getIntegerInstance();
        rmCloseTimeoutFormat.setGroupingUsed(false);
        rmCloseTimeoutFormat.setParseIntegerOnly(true);
        rmCloseTimeoutFormat.setMaximumFractionDigits(0);
        NumberFormatter rmCloseTimeoutFormatter = new NumberFormatter(rmCloseTimeoutFormat);
        rmCloseTimeoutFormatter.setCommitsOnValidEdit(true);
        rmCloseTimeoutFormatter.setMinimum(0);
        closeTimeoutDff.setDefaultFormatter(rmCloseTimeoutFormatter);

        freshnessDff = new DefaultFormatterFactory();
        NumberFormat freshnessFormat = NumberFormat.getIntegerInstance();
        freshnessFormat.setGroupingUsed(false);
        freshnessFormat.setParseIntegerOnly(true);
        freshnessFormat.setMaximumFractionDigits(0);
        NumberFormatter freshnessFormatter = new NumberFormatter(freshnessFormat);
        freshnessFormatter.setCommitsOnValidEdit(true);
        freshnessFormatter.setMinimum(0);
        freshnessDff.setDefaultFormatter(freshnessFormatter);

        skewDff = new DefaultFormatterFactory();
        NumberFormat skewFormat = NumberFormat.getIntegerInstance();
        skewFormat.setGroupingUsed(false);
        skewFormat.setParseIntegerOnly(true);
        skewFormat.setMaximumFractionDigits(0);
        NumberFormatter skewFormatter = new NumberFormatter(skewFormat);
        skewFormatter.setCommitsOnValidEdit(true);
        skewFormatter.setMinimum(0);
        skewDff.setDefaultFormatter(skewFormatter);

        initComponents();
        /* issue 232988: the background color issues with dark metal L&F
        lifeTimeLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        lifeTimeTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        renewExpiredChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        requireCancelChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        rmAckRequestField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        rmAckRequestLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        rmResendField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        rmResendLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        rmCloseTimeoutLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        rmCloseTimeoutField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        maxClockSkewField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        maxClockSkewLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        freshnessField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        freshnessLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        revocationChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        */
//        rmInactTimeoutField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
//        rmInactTimeoutLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());

        addImmediateModifier(rmAckRequestField);
        addImmediateModifier(rmCloseTimeoutField);
        addImmediateModifier(rmResendField);
//        addImmediateModifier(rmInactTimeoutField);
        addImmediateModifier(lifeTimeTextField);
        addImmediateModifier(renewExpiredChBox);
        addImmediateModifier(requireCancelChBox);
        addImmediateModifier(maxClockSkewField);
        addImmediateModifier(freshnessField);
        addImmediateModifier(revocationChBox);

        sync();
    }

    public void sync() {
        inSync = true;

        String lifeTime = ProprietarySecurityPolicyModelHelper.getLifeTime(binding, true);
        if (lifeTime == null) {
            lifeTimeTextField.setText(DEFAULT_LIFETIME);
        } else {
            lifeTimeTextField.setText(lifeTime);
        }

        String skew = ProprietarySecurityPolicyModelHelper.getMaxClockSkew(binding);
        if (skew == null) {
            maxClockSkewField.setText(ProprietarySecurityPolicyModelHelper.DEFAULT_MAXCLOCKSKEW);
        } else {
            maxClockSkewField.setText(skew);
        }

        String freshness = ProprietarySecurityPolicyModelHelper.getTimestampFreshness(binding);
        if (freshness == null) {
            freshnessField.setText(ProprietarySecurityPolicyModelHelper.DEFAULT_TIMESTAMPFRESHNESS);
        } else {
            freshnessField.setText(freshness);
        }

        String rmResendInterval = RMModelHelper.getResendInterval(binding);
        if (rmResendInterval == null) {
            rmResendField.setText(DEFAULT_RMRESENDINTERVAL);
        } else {
            rmResendField.setText(rmResendInterval);
        }

//        ConfigVersion configVersion = PolicyModelHelper.getConfigVersion(binding);
//        String rmInactInterval = RMModelHelper.getInstance(configVersion).getInactivityTimeout(binding);
//        if (rmInactInterval == null) {
//            rmInactTimeoutField.setText(RMModelHelper.DEFAULT_INACT_TIMEOUT);
//        } else {
//            rmInactTimeoutField.setText(rmInactInterval);
//        }
//
        String rmCloseTimeout = RMModelHelper.getCloseTimeout(binding);
        if (rmCloseTimeout == null) {
            rmCloseTimeoutField.setText(DEFAULT_RMCLOSETIMEOUT);
        } else {
            rmCloseTimeoutField.setText(rmCloseTimeout);
        }

        String rmAckRequest = RMModelHelper.getAckRequestInterval(binding);
        if (rmAckRequest == null) {
            rmAckRequestField.setText(DEFAULT_RMREQUESTACKINTERVAL);
        } else {
            rmAckRequestField.setText(rmAckRequest);
        }

        setChBox(renewExpiredChBox, ProprietarySecurityPolicyModelHelper.isRenewExpired(binding));
        setChBox(requireCancelChBox, ProprietarySecurityPolicyModelHelper.isRequireCancel(binding));

        setChBox(revocationChBox, ProprietarySecurityPolicyModelHelper.isRevocationEnabled(binding));

        enableDisable();

        inSync = false;
    }

    @Override
    public void setValue(javax.swing.JComponent source, Object value) {
        if (!inSync) {

            Util.checkMetroLibrary(project);
            
            ConfigVersion configVersion = PolicyModelHelper.getConfigVersion(binding);
            RMModelHelper rmh = RMModelHelper.getInstance(configVersion);
            if (source.equals(lifeTimeTextField)) {
                Integer lifeTime = (Integer) lifeTimeTextField.getValue();
                String lifetimeStr = (lifeTime == null) ? null : lifeTime.toString();
                if ((lifetimeStr == null) || (lifetimeStr.length() == 0) || (DEFAULT_LIFETIME.equals(lifetimeStr))) {
                    ProprietarySecurityPolicyModelHelper.setLifeTime(binding, null, true);
                } else {
                    ProprietarySecurityPolicyModelHelper.setLifeTime(binding, lifetimeStr, true);
                }
            }

            if (source.equals(maxClockSkewField)) {
                Integer skew = (Integer) maxClockSkewField.getValue();
                String skewStr = (skew == null) ? null : skew.toString();
                if ((skewStr == null) || (skewStr.length() == 0) ||
                    (ProprietarySecurityPolicyModelHelper.DEFAULT_MAXCLOCKSKEW.equals(skewStr))) {
                        ProprietarySecurityPolicyModelHelper.setMaxClockSkew(binding, null, true);
                } else {
                    ProprietarySecurityPolicyModelHelper.setMaxClockSkew(binding, skewStr, true);
                }
            }

            if (source.equals(freshnessField)) {
                Integer freshness = (Integer) freshnessField.getValue();
                String freshnessStr = (freshness == null) ? null : freshness.toString();
                if ((freshnessStr == null) || (freshnessStr.length() == 0) ||
                    (ProprietarySecurityPolicyModelHelper.DEFAULT_TIMESTAMPFRESHNESS.equals(freshnessStr))) {
                        ProprietarySecurityPolicyModelHelper.setTimestampFreshness(binding, null, true);
                } else {
                    ProprietarySecurityPolicyModelHelper.setTimestampFreshness(binding, freshnessStr, true);
                }
            }

//            if (source.equals(rmInactTimeoutField)) {
//                Integer inactInt = (Integer)rmInactTimeoutField.getValue();
//                String inactIntStr = (inactInt == null) ? null : inactInt.toString();
//                if ((inactIntStr == null) || (inactIntStr.length() == 0) ||
//                    (RMModelHelper.DEFAULT_INACT_TIMEOUT.equals(inactIntStr))) {
//                    rmh.enableRM(binding, false);
//                } else {
//                    rmh.setInactivityTimeout(binding, inactIntStr);
//                }
//            }

            if (source.equals(rmResendField)) {
                Integer resendInt = (Integer)rmResendField.getValue();
                String resendIntStr = (resendInt == null) ? null : resendInt.toString();
                if ((resendIntStr == null) || (resendIntStr.length() == 0) || (DEFAULT_RMRESENDINTERVAL.equals(resendIntStr))) {
                    rmh.setResendInterval(binding, null);
                } else {
                    rmh.setResendInterval(binding, resendIntStr);
                }
            }

            if (source.equals(rmCloseTimeoutField)) {
                Integer closeTimeout = (Integer)rmCloseTimeoutField.getValue();
                String closeTimeoutStr = (closeTimeout == null) ? null : closeTimeout.toString();
                if ((closeTimeoutStr == null) || (closeTimeoutStr.length() == 0) || (DEFAULT_RMCLOSETIMEOUT.equals(closeTimeoutStr))) {
                    rmh.setCloseTimeout(binding, null);
                } else {
                    rmh.setCloseTimeout(binding, closeTimeoutStr);
                }
            }

            if (source.equals(rmAckRequestField)) {
                Integer ackRequestInt = (Integer)rmAckRequestField.getValue();
                String ackRequestStr = (ackRequestInt == null) ? null : ackRequestInt.toString();
                if ((ackRequestStr == null) || (ackRequestStr.length() == 0) || (DEFAULT_RMREQUESTACKINTERVAL.equals(ackRequestStr))) {
                    rmh.setAckRequestInterval(binding, null);
                } else {
                    rmh.setAckRequestInterval(binding, ackRequestStr);
                }
            }

            if (source.equals(renewExpiredChBox)) {
                ProprietarySecurityPolicyModelHelper.setRenewExpired(binding, renewExpiredChBox.isSelected());
            }

            if (source.equals(requireCancelChBox)) {
                ProprietarySecurityPolicyModelHelper.setRequireCancel(binding, requireCancelChBox.isSelected());
            }

            if (source.equals(revocationChBox)) {
                ProprietarySecurityPolicyModelHelper.setRevocation(binding, revocationChBox.isSelected(), true);
            }
        }
    }

    private void enableDisable() {
        Binding serviceBinding = PolicyModelHelper.getBinding(serviceModel, binding.getName());
        boolean rmEnabled = RMModelHelper.getInstance(ConfigVersion.CONFIG_1_0).isRMEnabled(serviceBinding) ||
                            RMModelHelper.getInstance(ConfigVersion.CONFIG_1_3).isRMEnabled(serviceBinding) ||
                            RMModelHelper.getInstance(ConfigVersion.CONFIG_2_0).isRMEnabled(serviceBinding);
        boolean secConvConfigRequired = RequiredConfigurationHelper.isSecureConversationParamRequired(serviceBinding);

        rmAckRequestLabel.setEnabled(rmEnabled);
        rmAckRequestField.setEnabled(rmEnabled);
        rmResendLabel.setEnabled(rmEnabled);
        rmResendField.setEnabled(rmEnabled);
//        rmInactTimeoutField.setEnabled(rmEnabled);
//        rmInactTimeoutLabel.setEnabled(rmEnabled);
        rmCloseTimeoutField.setEnabled(rmEnabled);
        rmCloseTimeoutLabel.setEnabled(rmEnabled);

        lifeTimeTextField.setEnabled(secConvConfigRequired);
        lifeTimeLabel.setEnabled(secConvConfigRequired);
        renewExpiredChBox.setEnabled(secConvConfigRequired);
        requireCancelChBox.setEnabled(secConvConfigRequired);

        boolean security = SecurityPolicyModelHelper.isSecurityEnabled(serviceBinding);
        requireCancelChBox.setEnabled(security);
        maxClockSkewField.setEnabled(security);
        freshnessField.setEnabled(security);
    }

    private void setChBox(JCheckBox chBox, Boolean enable) {
        if (enable == null) {
            chBox.setSelected(false);
        } else {
            chBox.setSelected(enable);
        }
    }

    @Override
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lifeTimeLabel = new javax.swing.JLabel();
        renewExpiredChBox = new javax.swing.JCheckBox();
        requireCancelChBox = new javax.swing.JCheckBox();
        rmResendLabel = new javax.swing.JLabel();
        rmAckRequestLabel = new javax.swing.JLabel();
        rmResendField = new javax.swing.JFormattedTextField();
        rmAckRequestField = new javax.swing.JFormattedTextField();
        lifeTimeTextField = new javax.swing.JFormattedTextField();
        rmCloseTimeoutLabel = new javax.swing.JLabel();
        rmCloseTimeoutField = new javax.swing.JFormattedTextField();
        maxClockSkewLabel = new javax.swing.JLabel();
        maxClockSkewField = new javax.swing.JFormattedTextField();
        freshnessLabel = new javax.swing.JLabel();
        freshnessField = new javax.swing.JFormattedTextField();
        revocationChBox = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();

        lifeTimeLabel.setLabelFor(lifeTimeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(lifeTimeLabel, org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "LBL_AdvancedConfigPanel_SCTokenLifeTime")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(renewExpiredChBox, org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "LBL_AdvancedConfigPanel_RenewExpired")); // NOI18N
        renewExpiredChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(requireCancelChBox, org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "LBL_AdvancedConfigPanel_RequireCancel")); // NOI18N
        requireCancelChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        rmResendLabel.setLabelFor(rmResendField);
        org.openide.awt.Mnemonics.setLocalizedText(rmResendLabel, org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "LBL_AdvancedConfigPanel_ResendInterval")); // NOI18N

        rmAckRequestLabel.setLabelFor(rmAckRequestField);
        org.openide.awt.Mnemonics.setLocalizedText(rmAckRequestLabel, org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "LBL_AdvancedConfigPanel_AckRequestInterval")); // NOI18N

        rmResendField.setFormatterFactory(rmSendDff);

        rmAckRequestField.setFormatterFactory(rmReqDff);

        lifeTimeTextField.setFormatterFactory(lifetimeDff);

        rmCloseTimeoutLabel.setLabelFor(rmCloseTimeoutField);
        org.openide.awt.Mnemonics.setLocalizedText(rmCloseTimeoutLabel, org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "LBL_AdvancedConfigPanel_CloseTimeout")); // NOI18N

        rmCloseTimeoutField.setFormatterFactory(closeTimeoutDff);

        maxClockSkewLabel.setLabelFor(rmResendField);
        org.openide.awt.Mnemonics.setLocalizedText(maxClockSkewLabel, org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "LBL_AdvancedConfigPanel_MaxClockSkew")); // NOI18N

        maxClockSkewField.setFormatterFactory(skewDff);

        freshnessLabel.setLabelFor(rmResendField);
        org.openide.awt.Mnemonics.setLocalizedText(freshnessLabel, org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "LBL_AdvancedConfigPanel_TimestampFreshness")); // NOI18N

        freshnessField.setFormatterFactory(freshnessDff);

        org.openide.awt.Mnemonics.setLocalizedText(revocationChBox, org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "LBL_AdvancedConfigPanel_Revocation")); // NOI18N
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
                            .addComponent(rmResendLabel)
                            .addComponent(rmCloseTimeoutLabel)
                            .addComponent(rmAckRequestLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(rmResendField, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
                            .addComponent(rmCloseTimeoutField, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
                            .addComponent(rmAckRequestField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)))
                    .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
                    .addComponent(renewExpiredChBox)
                    .addComponent(requireCancelChBox)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lifeTimeLabel)
                        .addGap(50, 50, 50)
                        .addComponent(lifeTimeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE))
                    .addComponent(revocationChBox)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(maxClockSkewLabel)
                            .addComponent(freshnessLabel))
                        .addGap(69, 69, 69)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(freshnessField, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE)
                            .addComponent(maxClockSkewField, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rmResendLabel)
                    .addComponent(rmResendField, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rmCloseTimeoutLabel)
                    .addComponent(rmCloseTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rmAckRequestLabel)
                    .addComponent(rmAckRequestField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lifeTimeLabel)
                    .addComponent(lifeTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(renewExpiredChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(requireCancelChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxClockSkewLabel)
                    .addComponent(maxClockSkewField, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(freshnessLabel)
                    .addComponent(freshnessField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(revocationChBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {freshnessField, lifeTimeTextField, maxClockSkewField, rmAckRequestField, rmCloseTimeoutField, rmResendField});

        lifeTimeLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "ACSN_AdvancedConfigPanel_SCTokenLifeTime")); // NOI18N
        lifeTimeLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "ACSD_AdvancedConfigPanel_SCTokenLifeTime")); // NOI18N
        renewExpiredChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "ACSN_AdvancedConfigPanel_RenewExpired")); // NOI18N
        renewExpiredChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "ACSD_AdvancedConfigPanel_RenewExpired")); // NOI18N
        requireCancelChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "ACSN_AdvancedConfigPanel_RequireCancel")); // NOI18N
        requireCancelChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "ACSD_AdvancedConfigPanel_RequireCancel")); // NOI18N
        rmResendLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "ACSN_AdvancedConfigPanel_ResendInterval")); // NOI18N
        rmResendLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "ACSD_AdvancedConfigPanel_ResendInterval")); // NOI18N
        rmAckRequestLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "ACSN_AdvancedConfigPanel_AckRequestInterval")); // NOI18N
        rmAckRequestLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "ACSD_AdvancedConfigPanel_AckRequestInterval")); // NOI18N
        rmCloseTimeoutLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "ACSN_AdvancedConfigPanel_CloseTimeout")); // NOI18N
        rmCloseTimeoutLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AdvancedConfigPanelClient.class, "ACSD_AdvancedConfigPanel_CloseTimeout")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField freshnessField;
    private javax.swing.JLabel freshnessLabel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lifeTimeLabel;
    private javax.swing.JFormattedTextField lifeTimeTextField;
    private javax.swing.JFormattedTextField maxClockSkewField;
    private javax.swing.JLabel maxClockSkewLabel;
    private javax.swing.JCheckBox renewExpiredChBox;
    private javax.swing.JCheckBox requireCancelChBox;
    private javax.swing.JCheckBox revocationChBox;
    private javax.swing.JFormattedTextField rmAckRequestField;
    private javax.swing.JLabel rmAckRequestLabel;
    private javax.swing.JFormattedTextField rmCloseTimeoutField;
    private javax.swing.JLabel rmCloseTimeoutLabel;
    private javax.swing.JFormattedTextField rmResendField;
    private javax.swing.JLabel rmResendLabel;
    // End of variables declaration//GEN-END:variables

}
