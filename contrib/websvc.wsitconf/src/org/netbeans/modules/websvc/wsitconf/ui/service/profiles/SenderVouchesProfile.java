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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.undo.UndoManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
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
import org.netbeans.modules.websvc.wsitconf.wizard.SamlCallbackCreator;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.security.BootstrapPolicy;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.CallbackHandler;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.ProtectionToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SecureConversationToken;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;

/**
 * Transport Security Profile definition
 *
 * @author Martin Grebac
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.wsitconf.spi.SecurityProfile.class)
public class SenderVouchesProfile extends ProfileBase 
        implements SecureConversationFeature,ClientDefaultsFeature,ServiceDefaultsFeature,ValidatorsFeature, AdvancedSecurityFeature {
    
    private static final String PKGNAME = "samlcb";

    public int getId() {
        return 70;
    }

    public String getDisplayName() {
        return ComboConstants.PROF_SAMLSENDER;
    }

    public String getDescription() {
        return ComboConstants.PROF_SAMLSENDER_INFO;
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

        JPanel profConfigPanel = new SenderVouches(component, this);
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
    
    @Override
    public boolean isProfileSupported(Project p, WSDLComponent component, boolean sts) {
        ConfigVersion configVersion = PolicyModelHelper.getConfigVersion(component);
        return !ConfigVersion.CONFIG_1_0.equals(configVersion);
    }

    public void setClientDefaults(WSDLComponent component, WSDLComponent serviceBinding, Project p) {
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, false, true);
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, true, true);
        ProprietarySecurityPolicyModelHelper.removeCallbackHandlerConfiguration((Binding) component);
//        if (Util.isTomcat(p)) {
            String storeLoc = ServerUtils.getStoreLocation(p, false, true);
            ProprietarySecurityPolicyModelHelper.setStoreLocation(component, storeLoc, false, true);
            ProprietarySecurityPolicyModelHelper.setStoreType(component, KeystorePanel.JKS, false, true);
            ProprietarySecurityPolicyModelHelper.setStorePassword(component, DefaultSettings.getDefaultPassword(p), false, true);

            String tstoreLoc = ServerUtils.getStoreLocation(p, true, true);
            ProprietarySecurityPolicyModelHelper.setStoreLocation(component, tstoreLoc, true, true);
            ProprietarySecurityPolicyModelHelper.setStoreType(component, KeystorePanel.JKS, true, true);
            ProprietarySecurityPolicyModelHelper.setStorePassword(component, DefaultSettings.getDefaultPassword(p), true, true);
//        }
        ProprietarySecurityPolicyModelHelper.setKeyStoreAlias(component,ProfilesModelHelper.XWS_SECURITY_CLIENT, true);
        ProprietarySecurityPolicyModelHelper.setTrustPeerAlias(component,ProfilesModelHelper.XWS_SECURITY_SERVER, true);
                
        FileObject targetFolder = null;
        
        Sources sources = ProjectUtils.getSources(p);
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if ((sourceGroups != null) && (sourceGroups.length > 0)) {
            targetFolder = sourceGroups[0].getRootFolder();
        }
        
        SamlCallbackCreator samlCreator = new SamlCallbackCreator();
        String samlVersion = getSamlVersion((Binding)serviceBinding);
        String cbName = "SamlCallbackHandler";
        
        if (targetFolder != null) {
            if (targetFolder.getFileObject(PKGNAME) == null) {
                try {
                    targetFolder = targetFolder.createFolder(PKGNAME);
                } catch (IOException ex) {
                    Logger.getLogger("global").log(Level.SEVERE, null, ex);
                }
            } else {
                targetFolder = targetFolder.getFileObject(PKGNAME);
            }
            if (ComboConstants.SAML_V2011.equals(samlVersion)) {
                cbName = "Saml20SVCallbackHandler";
                if (targetFolder.getFileObject(cbName, "java") == null) {
                    samlCreator.generateSamlCBHandler(targetFolder, 
                        cbName, SamlCallbackCreator.SV, SamlCallbackCreator.SAML20);
                }
            } else {
                cbName = "Saml11SVCallbackHandler";
                if (targetFolder.getFileObject(cbName, "java") == null) {
                    samlCreator.generateSamlCBHandler(targetFolder, 
                        cbName, SamlCallbackCreator.SV, SamlCallbackCreator.SAML11);
                }
            }
        }
        ProprietarySecurityPolicyModelHelper.setCallbackHandler(
                (Binding)component, CallbackHandler.SAML_CBHANDLER, PKGNAME + "." + cbName, null, true);
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
        ProprietarySecurityPolicyModelHelper.setKeyStoreAlias(component,ProfilesModelHelper.XWS_SECURITY_SERVER, false);
    }
    
    public boolean isServiceDefaultSetupUsed(WSDLComponent component, Project p) {
        if (ProprietarySecurityPolicyModelHelper.isAnyValidatorSet(component)) return false;
        String keyAlias = ProprietarySecurityPolicyModelHelper.getStoreAlias(component, false);
        String keyLoc = ProprietarySecurityPolicyModelHelper.getStoreLocation(component, false);
        String keyPasswd = ProprietarySecurityPolicyModelHelper.getStorePassword(component, false);
        if ((Util.isEqual(DefaultSettings.getDefaultPassword(p), keyPasswd)) &&
            (Util.isEqual(ServerUtils.getStoreLocation(p, false, false), keyLoc)) &&
            (Util.isEqual(ProfilesModelHelper.XWS_SECURITY_SERVER, keyAlias))) { 
            return true;
        }
        return false;
    }

    public boolean isClientDefaultSetupUsed(WSDLComponent component, Binding serviceBinding, Project p) {
        
        if (ProprietarySecurityPolicyModelHelper.isAnyValidatorSet(component)) return false;
        String samlVersion = getSamlVersion(serviceBinding);
        String cbName = null;
        
        if (ComboConstants.SAML_V2011.equals(samlVersion)) {
            cbName = "Saml20SVCallbackHandler";
        } else {
            cbName = "Saml11SVCallbackHandler";
        }
        
        String keyAlias = ProprietarySecurityPolicyModelHelper.getStoreAlias(component, false);
        String trustAlias = ProprietarySecurityPolicyModelHelper.getStoreAlias(component, true);
        String trustLoc = ProprietarySecurityPolicyModelHelper.getStoreLocation(component, true);
        String keyLoc = ProprietarySecurityPolicyModelHelper.getStoreLocation(component, false);
        String keyPasswd = ProprietarySecurityPolicyModelHelper.getStorePassword(component, false);
        String trustPasswd = ProprietarySecurityPolicyModelHelper.getStorePassword(component, true);
        
        String cbHandler = ProprietarySecurityPolicyModelHelper.getCallbackHandler((Binding)component, CallbackHandler.SAML_CBHANDLER);
        
        if ((Util.isEqual(PKGNAME + "." + cbName, cbHandler)) &&
            (Util.isEqual(ProfilesModelHelper.XWS_SECURITY_CLIENT, keyAlias)) &&
            (Util.isEqual(ProfilesModelHelper.XWS_SECURITY_SERVER, trustAlias)) &&
            (Util.isEqual(DefaultSettings.getDefaultPassword(p), keyPasswd)) &&
            (Util.isEqual(DefaultSettings.getDefaultPassword(p), trustPasswd)) &&
            (Util.isEqual(ServerUtils.getStoreLocation(p, true, true), trustLoc)) &&
            (Util.isEqual(ServerUtils.getStoreLocation(p, false, false), keyLoc))) {
            return true;
        }
        return false;
    }
    
    private String getSamlVersion(Binding serviceBinding) {
        ConfigVersion cfgVersion = PolicyModelHelper.getConfigVersion(serviceBinding);
        WSDLComponent topSecBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(serviceBinding);
        WSDLComponent protTokenKind = SecurityTokensModelHelper.getTokenElement(topSecBinding, ProtectionToken.class);
        WSDLComponent protToken = SecurityTokensModelHelper.getTokenTypeElement(protTokenKind);
        
        int suppTokenType = SecurityTokensModelHelper.SIGNED_ENCRYPTED;
        if (ConfigVersion.CONFIG_1_0.equals(cfgVersion)) {
            suppTokenType = SecurityTokensModelHelper.SIGNED_SUPPORTING;
        }

        WSDLComponent tokenKind = null;
        boolean secConv = (protToken instanceof SecureConversationToken);
        
        if (secConv) {
            WSDLComponent bootPolicy = SecurityTokensModelHelper.getTokenElement(protToken, BootstrapPolicy.class);
            Policy pp = PolicyModelHelper.getTopLevelElement(bootPolicy, Policy.class,false);
            
            tokenKind = SecurityTokensModelHelper.getSupportingToken(pp, suppTokenType);
        } else {
            tokenKind = SecurityTokensModelHelper.getSupportingToken(serviceBinding, suppTokenType);
        }

        WSDLComponent token = SecurityTokensModelHelper.getTokenTypeElement(tokenKind);
        return SecurityTokensModelHelper.getTokenProfileVersion(token);
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
