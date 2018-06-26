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
import javax.swing.JPanel;
import javax.swing.undo.UndoManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.spi.features.AdvancedSecurityFeature;
import org.netbeans.modules.websvc.wsitconf.spi.features.SecureConversationFeature;
import org.netbeans.modules.websvc.wsitconf.spi.features.ValidatorsFeature;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.util.UndoCounter;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.ProtectionToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SecureConversationToken;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 * Kerberos Profile definition
 *
 * @author Martin Grebac
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.wsitconf.spi.SecurityProfile.class)
public class KerberosProfile extends ProfileBase 
        implements SecureConversationFeature,ValidatorsFeature, AdvancedSecurityFeature {
    
    public int getId() {
        return 25;
    }

    public String getDisplayName() {
        return ComboConstants.PROF_KERBEROS;
    }

    public String getDescription() {
        return ComboConstants.PROF_KERBEROS_INFO;
    }
    
    @Override
    public boolean isProfileSupported(Project p, WSDLComponent component, boolean sts) {
        ConfigVersion configVersion = PolicyModelHelper.getConfigVersion(component);
        return !ConfigVersion.CONFIG_1_0.equals(configVersion);
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

        JPanel profConfigPanel = new Kerberos(component, this);
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

    public boolean isSecureConversation(WSDLComponent component) {
        WSDLComponent topSecBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(component);
        WSDLComponent protTokenKind = SecurityTokensModelHelper.getTokenElement(topSecBinding, ProtectionToken.class);
        WSDLComponent protToken = SecurityTokensModelHelper.getTokenTypeElement(protTokenKind);        
        return (protToken instanceof SecureConversationToken);
    }

    public void enableSecureConversation(WSDLComponent component, boolean enable) {
        ProfilesModelHelper pmh = ProfilesModelHelper.getInstance(PolicyModelHelper.getConfigVersion(component));
        pmh.setSecureConversation(component, enable);
    }
    
    public boolean isValidatorSupported(ConfigVersion cfgVersion, String validatorType) {
        return !ConfigVersion.CONFIG_1_0.equals(cfgVersion);
    }
}
