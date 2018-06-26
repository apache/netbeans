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
import org.netbeans.modules.websvc.wsitconf.spi.features.ServiceDefaultsFeature;
import org.netbeans.modules.websvc.wsitconf.spi.features.ValidatorsFeature;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.util.UndoCounter;
import org.netbeans.modules.websvc.wsitconf.wizard.SamlCallbackCreator;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.CallbackHandler;
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
public class SAMLAuthorizationOverSSLProfile extends ProfileBase 
        implements ClientDefaultsFeature,ServiceDefaultsFeature,ValidatorsFeature, AdvancedSecurityFeature {
    
    private static final String PKGNAME = "samlcb";

    public int getId() {
        return 50;
    }

    public String getDisplayName() {
        return ComboConstants.PROF_SAMLSSL;
    }

    public String getDescription() {
        return ComboConstants.PROF_SAMLSSL_INFO;
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

        JPanel profConfigPanel = new SAMLAuthorizationOverSSL(component, this);
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
        return true;
    }    
    
    public void setClientDefaults(WSDLComponent component, WSDLComponent serviceBinding, Project p) {
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, false, true);
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, true, true);
        ProprietarySecurityPolicyModelHelper.removeCallbackHandlerConfiguration((Binding) component);

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
    }
    
    public boolean isServiceDefaultSetupUsed(WSDLComponent component, Project p) {
        if (ProprietarySecurityPolicyModelHelper.isAnyValidatorSet(component)) return false;
        return true;
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
        
        String cbHandler = ProprietarySecurityPolicyModelHelper.getCallbackHandler((Binding)component, CallbackHandler.SAML_CBHANDLER);
        if ((PKGNAME + "." + cbName).equals(cbHandler)) {
            return true;
        }
        return false;
    }

    private String getSamlVersion(Binding serviceBinding) {
        WSDLComponent tokenKind = SecurityTokensModelHelper.getSupportingToken(serviceBinding, SecurityTokensModelHelper.SIGNED_SUPPORTING);
        WSDLComponent token = SecurityTokensModelHelper.getTokenTypeElement(tokenKind);
        return SecurityTokensModelHelper.getTokenProfileVersion(token);
    }    
    
    public boolean isValidatorSupported(ConfigVersion cfgVersion, String validatorType) {
        return true;
    }
}
