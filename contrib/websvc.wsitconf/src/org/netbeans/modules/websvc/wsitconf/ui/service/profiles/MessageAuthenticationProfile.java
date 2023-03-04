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

package org.netbeans.modules.websvc.wsitconf.ui.service.profiles;

import java.awt.Dialog;
import javax.swing.JPanel;
import javax.swing.undo.UndoManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.spi.features.AdvancedSecurityFeature;
import org.netbeans.modules.websvc.wsitconf.spi.features.ClientDefaultsFeature;
import org.netbeans.modules.websvc.wsitconf.spi.features.SecureConversationFeature;
import org.netbeans.modules.websvc.wsitconf.spi.features.ServiceDefaultsFeature;
import org.netbeans.modules.websvc.wsitconf.spi.features.TrustStoreFeature;
import org.netbeans.modules.websvc.wsitconf.spi.features.ValidatorsFeature;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.util.UndoCounter;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.AlgoSuiteModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.BootstrapPolicy;
import org.netbeans.modules.websvc.wsitmodelext.security.SecurityPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.TransportBinding;
import org.netbeans.modules.websvc.wsitmodelext.security.TrustElement;
import org.netbeans.modules.websvc.wsitmodelext.security.WssElement;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.CallbackHandler;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SecureConversationToken;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 * Transport Security Profile definition
 *
 * @author Martin Grebac
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.wsitconf.spi.SecurityProfile.class)
public class MessageAuthenticationProfile extends ProfileBase 
        implements SecureConversationFeature,ClientDefaultsFeature,ServiceDefaultsFeature,ValidatorsFeature, AdvancedSecurityFeature, TrustStoreFeature {

    private static final String DEFAULT_USERNAME = "wsit";
    private static final String DEFAULT_PASSWORD = "wsitPassword";
    
    public int getId() {
        return 40;
    }
    
    public String getDisplayName() {
        return ComboConstants.PROF_MSGAUTHSSL;
    }

    public String getDescription() {
        return ComboConstants.PROF_MSGAUTHSSL_INFO;
    }
    
    /**
     * Should return true if the profile is set on component, false otherwise
     */
    public boolean isCurrentProfile(WSDLComponent component) {
        return getDisplayName().equals(ProfilesModelHelper.getWSITSecurityProfile(component));
    }
    
    @Override()
    public void displayConfig(WSDLComponent component, UndoManager undoManager) {
        UndoCounter undoCounter = new UndoCounter();
        WSDLModel model = component.getModel();
        
        model.addUndoableEditListener(undoCounter);

        JPanel profConfigPanel = new MessageAuthentication(component, this);
        DialogDescriptor dlgDesc = new DialogDescriptor(profConfigPanel, getDisplayName());
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);

        dlg.setVisible(true); 
        if (dlgDesc.getValue() == DialogDescriptor.CANCEL_OPTION) {
            for (int i=0; i<undoCounter.getCounter();i++) {
                if (undoManager.canUndo()) {
                    undoManager.undo();
                }
            }
        }
        
        model.removeUndoableEditListener(undoCounter);
    }
    
    public void setServiceDefaults(WSDLComponent component, Project p) {
        ProprietarySecurityPolicyModelHelper.clearValidators(component);
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, false, false);
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, true, false);
    }
 
    public void setClientDefaults(WSDLComponent component, WSDLComponent serviceBinding, Project p) {
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, false, true);
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, true, true);
        ProprietarySecurityPolicyModelHelper.removeCallbackHandlerConfiguration((Binding) component);
        ProprietarySecurityPolicyModelHelper.setCallbackHandler(
                (Binding)component, CallbackHandler.USERNAME_CBHANDLER, null, DEFAULT_USERNAME, true);
        ProprietarySecurityPolicyModelHelper.setCallbackHandler(
                (Binding)component, CallbackHandler.PASSWORD_CBHANDLER, null, DEFAULT_PASSWORD, true);
    }
    
    public boolean isServiceDefaultSetupUsed(WSDLComponent component, Project p) {
        if (ProprietarySecurityPolicyModelHelper.isAnyValidatorSet(component)) return false;
        String keyAlias = ProprietarySecurityPolicyModelHelper.getStoreAlias(component, false);
        String trustAlias = ProprietarySecurityPolicyModelHelper.getStoreAlias(component, true);
        String trustLoc = ProprietarySecurityPolicyModelHelper.getStoreLocation(component, true);
        String keyLoc = ProprietarySecurityPolicyModelHelper.getStoreLocation(component, false);
        String keyPasswd = ProprietarySecurityPolicyModelHelper.getStorePassword(component, false);
        String trustPasswd = ProprietarySecurityPolicyModelHelper.getStorePassword(component, true);
        if ((keyAlias == null) && (trustAlias == null)  && (trustLoc == null) && (keyLoc == null) && (trustPasswd == null) && (keyPasswd == null)) {
            return true;
        }
        return false;
    }

    public boolean isClientDefaultSetupUsed(WSDLComponent component, Binding serviceBinding, Project p) {
        if (ProprietarySecurityPolicyModelHelper.isAnyValidatorSet(component)) return false;
        String trustLoc = ProprietarySecurityPolicyModelHelper.getStoreLocation(component, true);
        String keyLoc = ProprietarySecurityPolicyModelHelper.getStoreLocation(component, false);
        String keyPasswd = ProprietarySecurityPolicyModelHelper.getStorePassword(component, false);
        String trustPasswd = ProprietarySecurityPolicyModelHelper.getStorePassword(component, true);
        String keyAlias = ProprietarySecurityPolicyModelHelper.getStoreAlias(component, false);
        String trustAlias = ProprietarySecurityPolicyModelHelper.getStoreAlias(component, true);
        if ((keyAlias == null) && (trustAlias == null)  && (trustLoc == null) && (keyLoc == null) && (trustPasswd == null) && (keyPasswd == null)) {
            String user = ProprietarySecurityPolicyModelHelper.getDefaultUsername((Binding)component);
            String passwd = ProprietarySecurityPolicyModelHelper.getDefaultPassword((Binding)component);
            if (Util.isEqual(DEFAULT_PASSWORD, passwd) && Util.isEqual(DEFAULT_USERNAME, user)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSecureConversation(WSDLComponent component) {
        WSDLComponent endToken = SecurityTokensModelHelper.getSupportingToken(component, SecurityTokensModelHelper.ENDORSING);
        return (endToken != null);
    }

    public void enableSecureConversation(WSDLComponent component, boolean enable) {
        ConfigVersion cfgVersion = PolicyModelHelper.getConfigVersion(component);
        SecurityTokensModelHelper stmh = SecurityTokensModelHelper.getInstance(cfgVersion);
        if (enable) {
            WSDLModel model = component.getModel();

            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }

            try {
                SecurityPolicyModelHelper securityPolicyModelHelper = SecurityPolicyModelHelper.getInstance(cfgVersion);
                AlgoSuiteModelHelper asmh = AlgoSuiteModelHelper.getInstance(cfgVersion);
                RMModelHelper rmh = RMModelHelper.getInstance(cfgVersion);
                PolicyModelHelper ProprietarySecurityPolicyModelHelper = PolicyModelHelper.getInstance(cfgVersion);
                
                securityPolicyModelHelper.enableTrust(component, cfgVersion);
                SecurityTokensModelHelper.removeSupportingTokens(component);
                
                WSDLComponent suppToken = stmh.setSupportingTokens(component, 
                        ComboConstants.SECURECONVERSATION, SecurityTokensModelHelper.ENDORSING);
                Policy p = ProprietarySecurityPolicyModelHelper.createElement(suppToken, PolicyQName.POLICY.getQName(cfgVersion), Policy.class, false);
                BootstrapPolicy bp = ProprietarySecurityPolicyModelHelper.createElement(p, SecurityPolicyQName.BOOTSTRAPPOLICY.getQName(cfgVersion), BootstrapPolicy.class, false);
                p = ProprietarySecurityPolicyModelHelper.createElement(bp, PolicyQName.POLICY.getQName(cfgVersion), Policy.class, false);
                TransportBinding tb = ProprietarySecurityPolicyModelHelper.createElement(p, SecurityPolicyQName.TRANSPORTBINDING.getQName(cfgVersion), TransportBinding.class, false);

                boolean rm = rmh.isRMEnabled(component);
                securityPolicyModelHelper.setDefaultTargets(p, true, rm);
                
                stmh.setTokenType(tb, ComboConstants.TRANSPORT, ComboConstants.HTTPS);
                securityPolicyModelHelper.setLayout(tb, ComboConstants.STRICT);
                securityPolicyModelHelper.enableIncludeTimestamp(tb, true);
                asmh.setAlgorithmSuite(tb, ComboConstants.BASIC128);

                if (ConfigVersion.CONFIG_1_0.equals(cfgVersion)) {
                    stmh.setSupportingTokens(p,
                            ComboConstants.USERNAME, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                } else {
                    stmh.setSupportingTokens(p,
                            ComboConstants.USERNAME, SecurityTokensModelHelper.SIGNED_ENCRYPTED);
                }
                
                WssElement wss = securityPolicyModelHelper.enableWss(bp, true);
//                securityPolicyModelHelper.enableMustSupportRefKeyIdentifier(wss, true);
                securityPolicyModelHelper.enableMustSupportRefIssuerSerial(wss, true);
                securityPolicyModelHelper.enableMustSupportRefThumbprint(wss, true);
                securityPolicyModelHelper.enableMustSupportRefEncryptedKey(wss, true);

                TrustElement trust = securityPolicyModelHelper.enableTrust(bp, cfgVersion);
                securityPolicyModelHelper.enableMustSupportIssuedTokens(trust, true);
                securityPolicyModelHelper.enableRequireClientEntropy(trust, true);
                securityPolicyModelHelper.enableRequireServerEntropy(trust, true);
                
            } finally {
                if (!isTransaction) {
                    model.endTransaction();
                }
            }
        } else {
            SecurityTokensModelHelper.removeSupportingTokens(component);
            if (ConfigVersion.CONFIG_1_0.equals(cfgVersion)) {
                stmh.setSupportingTokens(component, ComboConstants.USERNAME, SecurityTokensModelHelper.SIGNED_SUPPORTING);
            } else {
                stmh.setSupportingTokens(component, ComboConstants.USERNAME, SecurityTokensModelHelper.SIGNED_ENCRYPTED);
            }
        }
    }
    
    @Override
    public void profileSelected(WSDLComponent component, boolean updateServiceUrl, ConfigVersion configVersion) {
        ProfilesModelHelper pmh = ProfilesModelHelper.getInstance(configVersion);
        RMModelHelper rmh = RMModelHelper.getInstance(configVersion);
        pmh.setSecurityProfile(component, getDisplayName(), updateServiceUrl);
        boolean isRM = rmh.isRMEnabled(component);
        if (isRM) {
            enableSecureConversation(component, true);
        }
    }
    
    public boolean isValidatorSupported(ConfigVersion cfgVersion, String validatorType) {
        return true;
    }

    public boolean isTrustStoreRequired(WSDLComponent component, boolean client) {
        WSDLComponent secBinding = null;
        WSDLComponent endToken = SecurityTokensModelHelper.getSupportingToken(component, SecurityTokensModelHelper.ENDORSING);
        WSDLComponent secConvT = SecurityTokensModelHelper.getTokenElement(endToken, SecureConversationToken.class);
        boolean secConv = (secConvT instanceof SecureConversationToken);

        if (secConv) {
            WSDLComponent bootPolicy = SecurityTokensModelHelper.getTokenElement(secConvT, BootstrapPolicy.class);
            secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(bootPolicy);
            Policy p = (Policy) secBinding.getParent();
            p = PolicyModelHelper.getTopLevelElement(bootPolicy, Policy.class,false);
            WSDLComponent tokenKind = SecurityTokensModelHelper.getSupportingToken(p, SecurityTokensModelHelper.ENDORSING);
            if (tokenKind == null) {
                return false;
            }
        } else {
            secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(component);
            WSDLComponent tokenKind = SecurityTokensModelHelper.getSupportingToken(component, SecurityTokensModelHelper.ENDORSING);
            if (tokenKind == null) {
                return false;
            }
        }
        return true;
    }

}
