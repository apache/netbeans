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
import org.netbeans.modules.websvc.wsitconf.util.UndoCounter;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wizard.DerivedKeyPasswordValidatorCreator;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.CallbackHandler;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.Validator;
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
 * UsernameAuthPasswordDerivedKeysProfile definition
 *
 * @author Martin Grebac
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.wsitconf.spi.SecurityProfile.class)
public class UsernameAuthPasswordDerivedKeysProfile extends ProfileBase 
        implements SecureConversationFeature,ClientDefaultsFeature,ServiceDefaultsFeature,ValidatorsFeature, AdvancedSecurityFeature  {
    
    private static final Logger logger = Logger.getLogger(UsernameAuthPasswordDerivedKeysProfile.class.getName());
    
    @Override
    public int getId() {
        return 12;
    }

    @Override
    public String getDisplayName() {
        return ComboConstants.PROF_USERNAME_PASSWORDDERIVED;
    }

    @Override
    public String getDescription() {
        return ComboConstants.PROF_USERNAME_PASSWORDDERIVED_INFO;
    }
    
    /**
     * Should return true if the profile is set on component, false otherwise
     */
    @Override
    public boolean isCurrentProfile(WSDLComponent component) {
        return getDisplayName().equals(ProfilesModelHelper.getWSITSecurityProfile(component));
    }
    
    @Override()
    public void displayConfig(WSDLComponent component, UndoManager undoManager) {
        UndoCounter undoCounter = new UndoCounter();
        WSDLModel model = component.getModel();
        
        model.addUndoableEditListener(undoCounter);

        JPanel profConfigPanel = new UsernameAuthPasswordDerivedKeys(component, this);
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

    String PKG = "validators";

    @Override
    public boolean isServiceDefaultSetupUsed(WSDLComponent component, Project p) {
        if (!ProprietarySecurityPolicyModelHelper.isAnyValidatorSet(component)) return false;
        String val = ProprietarySecurityPolicyModelHelper.getValidator(component, Validator.USERNAME_VALIDATOR);
        if (val == null) return false;
        if (val.equals(PKG + ".DerivedKeyPasswordValidator")) {
            return true;
        }
        return false;
    }

    @Override
    public void setServiceDefaults(WSDLComponent component, Project p) {
        ProprietarySecurityPolicyModelHelper.clearValidators(component);
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, false, false);
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, true, false);

        FileObject targetFolder = null;

        Sources sources = ProjectUtils.getSources(p);
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if ((sourceGroups != null) && (sourceGroups.length > 0)) {
            targetFolder = sourceGroups[0].getRootFolder();
        }

        DerivedKeyPasswordValidatorCreator creator = new DerivedKeyPasswordValidatorCreator();
        String cbName = "DerivedKeyPasswordValidator";

        if (targetFolder != null) {
            if (targetFolder.getFileObject(PKG) == null) {
                try {
                    targetFolder = targetFolder.createFolder(PKG);
                } catch (IOException ex) {
                    Logger.getLogger("global").log(Level.SEVERE, null, ex);
                }
            } else {
                targetFolder = targetFolder.getFileObject(PKG);
            }
            if (targetFolder.getFileObject(cbName, "java") == null) {
                creator.generate(targetFolder, cbName);
            }
        }
        ProprietarySecurityPolicyModelHelper.setValidator(
                (Binding)component, Validator.USERNAME_VALIDATOR, PKG + "." + cbName, false);

    }

    @Override
    public void setClientDefaults(WSDLComponent component, WSDLComponent serviceBinding, Project p) {
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, false, true);
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, true, true);
        ProprietarySecurityPolicyModelHelper.removeCallbackHandlerConfiguration((Binding) component);
        ProprietarySecurityPolicyModelHelper.setCallbackHandler(
                (Binding)component, CallbackHandler.USERNAME_CBHANDLER, null, UsernameAuthenticationProfile.DEFAULT_USERNAME, true);
        ProprietarySecurityPolicyModelHelper.setCallbackHandler(
                (Binding)component, CallbackHandler.PASSWORD_CBHANDLER, null, UsernameAuthenticationProfile.DEFAULT_PASSWORD, true);
        ProprietarySecurityPolicyModelHelper.setHandlerTimestampTimeout((Binding) component, null, true);
        ProprietarySecurityPolicyModelHelper.setHandlerIterations((Binding) component, "1000", true);
    }

    @Override
    public boolean isClientDefaultSetupUsed(WSDLComponent component, Binding serviceBinding, Project p) {
        if (ProprietarySecurityPolicyModelHelper.isAnyValidatorSet(component)) return false;
        String user = ProprietarySecurityPolicyModelHelper.getDefaultUsername((Binding)component);
        String passwd = ProprietarySecurityPolicyModelHelper.getDefaultPassword((Binding)component);
        if ((Util.isEqual(UsernameAuthenticationProfile.DEFAULT_PASSWORD, passwd)) &&
            (Util.isEqual(UsernameAuthenticationProfile.DEFAULT_USERNAME, user))) {
                return true;
        }
        return false;
    }

    @Override
    public boolean isSecureConversation(WSDLComponent component) {
        WSDLComponent topSecBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(component);
        WSDLComponent protTokenKind = SecurityTokensModelHelper.getTokenElement(topSecBinding, ProtectionToken.class);
        WSDLComponent protToken = SecurityTokensModelHelper.getTokenTypeElement(protTokenKind);        
        return (protToken instanceof SecureConversationToken);
    }

    @Override
    public void enableSecureConversation(WSDLComponent component, boolean enable) {
        ProfilesModelHelper.getInstance(PolicyModelHelper.getConfigVersion(component)).setSecureConversation(component, enable);
    }

    @Override
    public boolean isValidatorSupported(ConfigVersion cfgVersion, String validatorType) {
        return true;
    }
    
}
