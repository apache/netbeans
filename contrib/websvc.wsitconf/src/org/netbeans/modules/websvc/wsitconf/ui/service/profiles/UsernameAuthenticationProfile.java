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
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.undo.UndoManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.spi.WsitProvider;
import org.netbeans.modules.websvc.wsitconf.spi.features.AdvancedSecurityFeature;
import org.netbeans.modules.websvc.wsitconf.spi.features.ClientDefaultsFeature;
import org.netbeans.modules.websvc.wsitconf.spi.features.SecureConversationFeature;
import org.netbeans.modules.websvc.wsitconf.spi.features.ServiceDefaultsFeature;
import org.netbeans.modules.websvc.wsitconf.spi.features.ValidatorsFeature;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.KeystorePanel;
import org.netbeans.modules.websvc.wsitconf.util.DefaultSettings;
import org.netbeans.modules.websvc.wsitconf.util.ServerUtils;
import org.netbeans.modules.websvc.wsitconf.util.UndoCounter;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.CallbackHandler;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.ProtectionToken;
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
public class UsernameAuthenticationProfile extends ProfileBase 
        implements SecureConversationFeature,ClientDefaultsFeature,ServiceDefaultsFeature,ValidatorsFeature, AdvancedSecurityFeature  {
    
    public static final String DEFAULT_USERNAME = "wsitUser";
    public static final String DEFAULT_PASSWORD = "changeit";

    private static final Logger logger = Logger.getLogger(UsernameAuthenticationProfile.class.getName());
    
    public int getId() {
        return 10;
    }

    public String getDisplayName() {
        return ComboConstants.PROF_USERNAME;
    }

    public String getDescription() {
        return ComboConstants.PROF_USERNAME_INFO;
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

        JPanel profConfigPanel = new UsernameAuthentication(component, this);
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
    
    public boolean isServiceDefaultSetupUsed(WSDLComponent component, Project p) {
        if (ProprietarySecurityPolicyModelHelper.isAnyValidatorSet(component)) return false;
        String storeAlias = ProprietarySecurityPolicyModelHelper.getStoreAlias(component, false);
        String storeLoc = ProprietarySecurityPolicyModelHelper.getStoreLocation(component, false);
        String storePasswd = ProprietarySecurityPolicyModelHelper.getStorePassword(component, false);
        if ((Util.isEqual(DefaultSettings.getDefaultPassword(p), storePasswd)) &&
            (Util.isEqual(ServerUtils.getStoreLocation(p, false, false), storeLoc)) &&
            (Util.isEqual(ProfilesModelHelper.XWS_SECURITY_SERVER, storeAlias))) { 
            return true;
        }
        return false;
    }

    public void setServiceDefaults(WSDLComponent component, Project p) {
        ProprietarySecurityPolicyModelHelper.clearValidators(component);
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, false, false);
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, true, false);
//        if (Util.isTomcat(p)) {
            String storeLoc = ServerUtils.getStoreLocation(p, false, false);
            ProprietarySecurityPolicyModelHelper.setStoreLocation(component, storeLoc, false, false);
            ProprietarySecurityPolicyModelHelper.setStoreType(component, KeystorePanel.JKS, false, false);
            ProprietarySecurityPolicyModelHelper.setStorePassword(component, DefaultSettings.getDefaultPassword(p), false, false);
//        }
        if (ServerUtils.isGlassfish(p)) {
            WsitProvider wp = p.getLookup().lookup(WsitProvider.class);
            if (wp != null) {
                wp.createUser();
            }
        }
        ProprietarySecurityPolicyModelHelper.setKeyStoreAlias(component,ProfilesModelHelper.XWS_SECURITY_SERVER, false);
    }

    public void setClientDefaults(WSDLComponent component, WSDLComponent serviceBinding, Project p) {
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, false, true);
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, true, true);
        ProprietarySecurityPolicyModelHelper.removeCallbackHandlerConfiguration((Binding) component);
        ProprietarySecurityPolicyModelHelper.setCallbackHandler(
                (Binding)component, CallbackHandler.USERNAME_CBHANDLER, null, DEFAULT_USERNAME, true);
        ProprietarySecurityPolicyModelHelper.setCallbackHandler(
                (Binding)component, CallbackHandler.PASSWORD_CBHANDLER, null, DEFAULT_PASSWORD, true);
        ProprietarySecurityPolicyModelHelper.setHandlerTimestampTimeout((Binding) component, null, true);
//        if (Util.isTomcat(p)) {
            String tstoreLoc = ServerUtils.getStoreLocation(p, true, true);
            ProprietarySecurityPolicyModelHelper.setStoreLocation(component, tstoreLoc, true, true);
            ProprietarySecurityPolicyModelHelper.setStoreType(component, KeystorePanel.JKS, true, true);
            ProprietarySecurityPolicyModelHelper.setStorePassword(component, KeystorePanel.DEFAULT_PASSWORD, true, true);
//        }
        ProprietarySecurityPolicyModelHelper.setTrustPeerAlias(component,ProfilesModelHelper.XWS_SECURITY_SERVER, true);        
    }

    public boolean isClientDefaultSetupUsed(WSDLComponent component, Binding serviceBinding, Project p) {
        if (ProprietarySecurityPolicyModelHelper.isAnyValidatorSet(component)) return false;
        String trustAlias = ProprietarySecurityPolicyModelHelper.getStoreAlias(component, true);
        String trustPasswd = ProprietarySecurityPolicyModelHelper.getStorePassword(component, true);
        String trustLoc = ProprietarySecurityPolicyModelHelper.getStoreLocation(component, true);
        if (ProfilesModelHelper.XWS_SECURITY_SERVER.equals(trustAlias)) {
            String user = ProprietarySecurityPolicyModelHelper.getDefaultUsername((Binding)component);
            String passwd = ProprietarySecurityPolicyModelHelper.getDefaultPassword((Binding)component);
            if ((Util.isEqual(DEFAULT_PASSWORD, passwd)) &&
                (Util.isEqual(DEFAULT_USERNAME, user)) && 
                (Util.isEqual(DefaultSettings.getDefaultPassword(p), trustPasswd)) &&
                (Util.isEqual(ServerUtils.getStoreLocation(p, true, true), trustLoc))) {
                return true;
            }
        }
        return false;
    }

    public boolean isSecureConversation(WSDLComponent component) {
        WSDLComponent topSecBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(component);
        WSDLComponent protTokenKind = SecurityTokensModelHelper.getTokenElement(topSecBinding, ProtectionToken.class);
        WSDLComponent protToken = SecurityTokensModelHelper.getTokenTypeElement(protTokenKind);        
        return (protToken instanceof SecureConversationToken);
    }

    public void enableSecureConversation(WSDLComponent component, boolean enable) {
        ProfilesModelHelper.getInstance(PolicyModelHelper.getConfigVersion(component)).setSecureConversation(component, enable);
    }

    public boolean isValidatorSupported(ConfigVersion cfgVersion, String validatorType) {
        return true;
    }
    
}
