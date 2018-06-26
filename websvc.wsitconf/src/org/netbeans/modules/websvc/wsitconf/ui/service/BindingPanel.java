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

package org.netbeans.modules.websvc.wsitconf.ui.service;

import java.awt.Color;
import java.awt.Dialog;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.undo.UndoManager;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfile;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfileRegistry;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.AdvancedRMPanel;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.KeystorePanel;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.STSConfigServicePanel;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.TruststorePanel;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.ValidatorsPanel;
import org.netbeans.modules.websvc.wsitconf.util.UndoCounter;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.TransportModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import javax.swing.*;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.api.WSITConfigProvider;
import org.netbeans.modules.websvc.wsitconf.spi.ProjectSpecificSecurity;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityCheckerRegistry;
import org.netbeans.modules.websvc.wsitconf.spi.WsitProvider;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.AdvancedSecurityPanel;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.KerberosConfigPanel;
import org.netbeans.modules.websvc.wsitconf.util.DefaultSettings;
import org.netbeans.modules.websvc.wsitconf.util.ServerUtils;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.AddressingModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMSequenceBinding;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.WSITModelSupport;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.websvc.wsstack.api.WSStackVersion;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 */
public class BindingPanel extends SectionInnerPanel {

    private WSDLModel model;
    private Node node;
    private Binding binding;
    private UndoManager undoManager;
    private Project project;
    private Service service;
    private JaxWsModel jaxwsmodel;

    private WsitProvider wsitProvider;

    private String oldProfile;

    private boolean doNotSync = false;

    private boolean inSync = false;
    private boolean isFromJava = true;
    private boolean jsr109 = false;

    private final Color RED = new java.awt.Color(255, 0, 0);
    private final Color REGULAR;

    private boolean updateServiceUrl = true;

    private SortedSet<ConfigVersion> supportedConfigVersions = new TreeSet<ConfigVersion>();

    public BindingPanel(SectionView view, Node node, Project p, Binding binding, UndoManager undoManager, JaxWsModel jaxwsmodel) {
        super(view);
        this.model = binding.getModel();
        this.project = p;
        this.node = node;
        this.undoManager = undoManager;
        this.binding = binding;
        this.jaxwsmodel = jaxwsmodel;

        this.wsitProvider = project.getLookup().lookup(WsitProvider.class);
        if (wsitProvider != null) {
            jsr109 = wsitProvider.isJsr109Project();
        }

        initComponents();

        REGULAR = profileInfoField.getForeground();

        if (node != null) {
            service = node.getLookup().lookup(Service.class);
            isFromJava = !WSITModelSupport.isServiceFromWsdl(node);
        } else {
            isFromJava = false;
        }

        /* issue 232988: the background color issues with dark metal L&F
        mtomChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        rmChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        orderedChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        securityChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        profileComboLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        profileCombo.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        profileInfoField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        stsChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        tcpChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        fiChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        devDefaultsChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        jSeparator1.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        jSeparator2.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        jSeparator3.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        jSeparator4.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        cfgVersionLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        cfgVersionCombo.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        addrChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        */
        profileInfoField.setFont(mtomChBox.getFont());

        // detect and fill appropriate config options
        WSStackVersion wsStackVersion = WSITConfigProvider.getDefault().getHighestWSStackVersion(project);
        inSync = true;
        for (ConfigVersion cfgVersion : ConfigVersion.values()) {
            if ((wsStackVersion != null) && (cfgVersion.isSupported(wsStackVersion) && (cfgVersion.isVisible()))) {
                supportedConfigVersions.add(cfgVersion);
                cfgVersionCombo.addItem(cfgVersion);
            }
        }
        if (supportedConfigVersions.isEmpty()) {
            supportedConfigVersions.add(ConfigVersion.CONFIG_1_0);
            cfgVersionCombo.addItem(ConfigVersion.CONFIG_1_0);
        }
        inSync = false;

        String CONVERT = NbBundle.getMessage(BindingPanel.class, "LBL_Convert");
        String LEAVE = NbBundle.getMessage(BindingPanel.class, "LBL_LeaveIntact");
        String[] OPTIONS = new String[] {CONVERT, LEAVE};

        ConfigVersion configVersion = PolicyModelHelper.getWrittenConfigVersion(binding);
        if ((configVersion != null) && (!supportedConfigVersions.contains(configVersion))) {
            NotifyDescriptor dlgDesc = new NotifyDescriptor(
                NbBundle.getMessage(BindingPanel.class, "TXT_UnsupportedProfileDetected"),
                new NotifyDescriptor.Confirmation("test").getTitle(),
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE,
                OPTIONS, LEAVE);
            DialogDisplayer.getDefault().notify(dlgDesc);
            if (CONVERT.equals(dlgDesc.getValue())) {
                PolicyModelHelper.setConfigVersion(binding,
                    (ConfigVersion) supportedConfigVersions.toArray()[supportedConfigVersions.size() - 1],
                    project);
            } else if (LEAVE.equals(dlgDesc.getValue())) {
                cfgVersionCombo.addItem(configVersion);
                supportedConfigVersions.add(configVersion);
                enableDisable();
            } else {
                this.setVisible(false);
            }
        } else if (configVersion == null) {
            if ((supportedConfigVersions != null) && (supportedConfigVersions.size() > 0)) {
                PolicyModelHelper.setConfigVersion(binding,
                    (ConfigVersion) supportedConfigVersions.toArray()[supportedConfigVersions.size() - 1],
                    project);
            } else {
                ConfigVersion cfgVersion = ConfigVersion.CONFIG_1_0;
                PolicyModelHelper.setConfigVersion(binding, cfgVersion, project);
                supportedConfigVersions.add(cfgVersion);
                cfgVersionCombo.addItem(cfgVersion);
            }
        }

        addImmediateModifier(cfgVersionCombo);
        addImmediateModifier(mtomChBox);
        addImmediateModifier(rmChBox);
        addImmediateModifier(orderedChBox);
        addImmediateModifier(securityChBox);
        addImmediateModifier(profileCombo);
        addImmediateModifier(stsChBox);
        addImmediateModifier(tcpChBox);
        addImmediateModifier(fiChBox);
        addImmediateModifier(devDefaultsChBox);
        addImmediateModifier(addrChBox);

        sync();

        if ((!isFromJava) &&
            (PolicyModelHelper.getPolicyUriForElement(binding) == null) &&
            (ProfilesModelHelper.isServiceUrlHttps(binding))) {
                updateServiceUrl = false;
        }

        model.addComponentListener(new ComponentListener() {
            @Override
            public void valueChanged(ComponentEvent evt) {
                if (!doNotSync) {
                    sync();
                }
            }
            @Override
            public void childrenAdded(ComponentEvent evt) {
                if (!doNotSync) {
                    sync();
                }
            }
            @Override
            public void childrenDeleted(ComponentEvent evt) {
                if (!doNotSync) {
                    sync();
                }
            }
        });
    }

    private void fillProfileCombo(boolean sts) {
        profileCombo.removeAllItems();
        Set<SecurityProfile> profiles = SecurityProfileRegistry.getDefault().getSecurityProfiles();
        for (SecurityProfile profile : profiles) {
            if (profile.isProfileSupported(project, binding, sts)) {
                profileCombo.addItem(profile.getDisplayName());
            }
        }
    }

    private ConfigVersion getUserExpectedConfigVersion() {
        return (ConfigVersion) cfgVersionCombo.getSelectedItem();
    }
    
    private void sync() {
        inSync = true; doNotSync = true;
        try {            
            ConfigVersion configVersion = PolicyModelHelper.getConfigVersion(binding);
            cfgVersionCombo.setSelectedItem(configVersion);

            boolean addrEnabled = AddressingModelHelper.isAddressingEnabled(binding);
            setChBox(addrChBox, addrEnabled);

            boolean mtomEnabled = TransportModelHelper.isMtomEnabled(binding);
            setChBox(mtomChBox, mtomEnabled);

            boolean fiEnabled = TransportModelHelper.isFIEnabled(binding);
            setChBox(fiChBox, !fiEnabled);

            boolean tcpEnabled = TransportModelHelper.isTCPEnabled(binding);
            setChBox(tcpChBox, tcpEnabled);

            boolean rmEnabled = RMModelHelper.getInstance(configVersion).isRMEnabled(binding);
            setChBox(rmChBox, rmEnabled);
            setChBox(orderedChBox, RMModelHelper.getInstance(configVersion).isOrderedEnabled(binding));

            boolean stsEnabled = ProprietarySecurityPolicyModelHelper.isSTSEnabled(binding);
            setChBox(stsChBox, stsEnabled);

            fillProfileCombo(stsEnabled);

            boolean securityEnabled = SecurityPolicyModelHelper.isSecurityEnabled(binding);
            setChBox(securityChBox, securityEnabled);
            if (securityEnabled) {
                String profile = ProfilesModelHelper.getSecurityProfile(binding);
                setSecurityProfile(profile);
                boolean defaults = ProfilesModelHelper.isServiceDefaultSetupUsed(profile, binding, project);
                setChBox(devDefaultsChBox, defaults);
                oldProfile = profile;
            } else {
                setSecurityProfile(ComboConstants.PROF_USERNAME);
                setChBox(devDefaultsChBox, true);
            }

            enableDisable();
        } finally {
            inSync = false; doNotSync = false;
        }
        refresh();
    }

    @Override
    public void setValue(javax.swing.JComponent source, Object value) {
        if (inSync) return;

        Util.checkMetroLibrary(project);
        
        ConfigVersion userExpectedCfgVersion = getUserExpectedConfigVersion();
        if (source.equals(cfgVersionCombo)) {
            doNotSync = true;
            try {
                PolicyModelHelper.setConfigVersion(binding, userExpectedCfgVersion, project);
                try {
                    inSync = true;
                    fillProfileCombo(stsChBox.isSelected());
                } finally {
                    inSync = false;
                }
            } finally {
                doNotSync = false;
            }
            sync();
        }

        RMModelHelper rmh = RMModelHelper.getInstance(userExpectedCfgVersion);

        if (source.equals(rmChBox)) {
            boolean rm = rmh.isRMEnabled(binding);
            if (rmChBox.isSelected() != rm) {
                rmh.enableRM(binding, rmChBox.isSelected());
                if (securityChBox.isSelected()) {
                    if (!ProfilesModelHelper.isSCEnabled(binding)) {
                        ProfilesModelHelper.getInstance(userExpectedCfgVersion).setSecureConversation(binding, true);
                    }
                    if (!(ConfigVersion.CONFIG_1_0.equals(userExpectedCfgVersion)) && rmChBox.isSelected()) {
                        String profile = ProfilesModelHelper.getSecurityProfile(binding);
                        if (ProfilesModelHelper.isSSLProfile(profile)) {
                            RMSequenceBinding.SECURED_TRANSPORT.set(userExpectedCfgVersion, binding);
                        } else {
                            RMSequenceBinding.SECURED_TOKEN.set(userExpectedCfgVersion, binding);
                        }
                    }
                }
            }
        }

        if (source.equals(orderedChBox)) {
            boolean ordered = rmh.isOrderedEnabled(binding);
            if (orderedChBox.isSelected() != ordered) {
                rmh.enableOrdered(binding, orderedChBox.isSelected());
            }
        }

        if (source.equals(mtomChBox)) {
            boolean mtom = TransportModelHelper.isMtomEnabled(binding);
            if (mtomChBox.isSelected() != mtom) {
                TransportModelHelper.enableMtom(binding, mtomChBox.isSelected());
            }
        }

        if (source.equals(fiChBox)) {
            boolean fi = TransportModelHelper.isFIEnabled(binding);
            if (!fiChBox.isSelected() != fi) { // fast infoset has a reverted meaning
                TransportModelHelper.enableFI(binding, !fiChBox.isSelected());
            }
        }

        if (source.equals(tcpChBox)) {
            boolean tcp = TransportModelHelper.isTCPEnabled(binding);
            if (tcpChBox.isSelected() != tcp) {
                String name = null;
                String serviceName = null;
                String implClass = null;
                if (service != null) {
                    name = service.getName();
                    serviceName = service.getServiceName();
                    implClass = service.getImplementationClass();
                }
                TransportModelHelper.enableTCP(name, serviceName, implClass, isFromJava, binding, project, tcpChBox.isSelected());
            }
        }

        if (source.equals(addrChBox)) {
            boolean addr = AddressingModelHelper.isAddressingEnabled(binding);
            if (addrChBox.isSelected() != addr) {
                if (addrChBox.isSelected()) { 
                    AddressingModelHelper.getInstance(getUserExpectedConfigVersion()).enableAddressing(binding, true);
                } else {
                    AddressingModelHelper.disableAddressing(binding);
                }
            }
        }

        ProjectSpecificSecurity pss = null;
        if (wsitProvider != null) {
            pss = wsitProvider.getProjectSecurityUpdater();
        }
        if (source.equals(securityChBox)) {
            String profile = (String) profileCombo.getSelectedItem();
            if (securityChBox.isSelected()) {
                Util.checkMetroRtLibrary(project, true);
                profileCombo.setSelectedItem(profile);
                if (devDefaultsChBox.isSelected()) {
                    DefaultSettings.fillDefaults(project, false,true);
                    ProfilesModelHelper.setServiceDefaults((String) profileCombo.getSelectedItem(), binding, project);
                    if (ProfilesModelHelper.isSSLProfile(profile)) {
                        if (pss != null) {
                            pss.setSSLAttributes(binding);
                        }
                    }
                }
            } else {
                if (devDefaultsChBox.isSelected()) {
                    if (ProfilesModelHelper.isSSLProfile(profile)) {
                        if (pss != null) {
                            pss.unsetSSLAttributes(binding);
                        }
                    }
                }
                DefaultSettings.unfillDefaults(project);
                SecurityPolicyModelHelper spmh = SecurityPolicyModelHelper.getInstance(getUserExpectedConfigVersion());
                spmh.disableSecurity(binding, true);
            }
            oldProfile = profile;
        }

        if (source.equals(devDefaultsChBox)) {
            if (devDefaultsChBox.isSelected()) {
                DefaultSettings.fillDefaults(project, false,true);
                ProfilesModelHelper.setServiceDefaults((String) profileCombo.getSelectedItem(), binding, project);
            } else {
                DefaultSettings.unfillDefaults(project);
            }
        }

        if (source.equals(stsChBox)) {
            if (stsChBox.isSelected() != ProprietarySecurityPolicyModelHelper.isSTSEnabled(binding)) {
                ProprietarySecurityPolicyModelHelper.getInstance(getUserExpectedConfigVersion()).
                        enableSTS(binding, stsChBox.isSelected());
                inSync = true; fillProfileCombo(true); inSync = false;
            }
        }

        if (source.equals(profileCombo)) {
            doNotSync = true;
            try {
                String profile = (String) profileCombo.getSelectedItem();
                ProfilesModelHelper.getInstance(getUserExpectedConfigVersion()).setSecurityProfile(binding, profile, oldProfile, updateServiceUrl);
                if (devDefaultsChBox.isSelected()) {
                    ProfilesModelHelper.setServiceDefaults(profile, binding, project);
                    if (ProfilesModelHelper.isSSLProfile(profile) && !ProfilesModelHelper.isSSLProfile(oldProfile)) {
                        if (pss != null) {
                            pss.setSSLAttributes(binding);
                        }
                    }
                    if (!ProfilesModelHelper.isSSLProfile(profile) && ProfilesModelHelper.isSSLProfile(oldProfile)) {
                        if (pss != null) {
                            pss.unsetSSLAttributes(binding);
                        }
                    }
                }
                boolean defUsed = ProfilesModelHelper.isServiceDefaultSetupUsed(profile, binding, project);
                inSync = true; devDefaultsChBox.setSelected(defUsed); inSync = false;
                profileInfoField.setText(SecurityProfileRegistry.getDefault().getProfile(profile).getDescription());
                oldProfile = profile;
            } finally {
                doNotSync = false;
            }
        }

        enableDisable();
    }

    public Boolean getChBox(JCheckBox chBox) {
        if (chBox.isSelected()) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private void setChBox(JCheckBox chBox, Boolean enable) {
        if (enable == null) {
            chBox.setSelected(false);
        } else {
            chBox.setSelected(enable);
        }
    }

    // SECURITY PROFILE
    private void setSecurityProfile(String profile) {
        this.profileCombo.setSelectedItem(profile);
        SecurityProfile sp = SecurityProfileRegistry.getDefault().getProfile(profile);
        if (!ComboConstants.PROF_NOTRECOGNIZED.equals(profile)) {
            this.profileInfoField.setText(sp.getDescription());
        }
    }

    @Override
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        SectionView view = getSectionView();
        enableDisable();
        if (view != null) {
            view.getErrorPanel().clearError();
        }
    }

    @Override
    public void rollbackValue(javax.swing.text.JTextComponent source) {
    }

    @Override
    protected void endUIChange() { }

    @Override
    public void linkButtonPressed(Object ddBean, String ddProperty) { }

    @Override
    public javax.swing.JComponent getErrorComponent(String errorId) {
        return new JButton();
    }

    private void enableDisable() {

        cfgVersionCombo.setEnabled(cfgVersionCombo.getItemCount() > 1);
        cfgVersionLabel.setEnabled(cfgVersionCombo.getItemCount() > 1);

        boolean relSelected = rmChBox.isSelected();
        orderedChBox.setEnabled(relSelected);
        rmAdvanced.setEnabled(relSelected);

        tcpChBox.setEnabled(true);

        boolean amSec = SecurityCheckerRegistry.getDefault().isNonWsitSecurityEnabled(node, jaxwsmodel);

        // everything is ok, disable security
        if (!amSec) {

            boolean gf = ServerUtils.isGlassfish(project);

            securityChBox.setEnabled(true);
            profileInfoField.setForeground(REGULAR);

            boolean secSelected = securityChBox.isSelected();

            profileComboLabel.setEnabled(secSelected);
            profileCombo.setEnabled(secSelected);
            profileInfoField.setEnabled(secSelected);

            boolean keyStoreConfigRequired = true;
            boolean trustStoreConfigRequired = true;
            boolean kerberosConfigRequired = false;

            boolean stsAllowed = true;
            boolean defaults = devDefaultsChBox.isSelected();

            profConfigButton.setEnabled(secSelected);

            boolean validatorsSupported = false;
            boolean advancedConfigSupported = false;

            if (secSelected) {

                String secProfile = ProfilesModelHelper.getSecurityProfile(binding);

                validatorsSupported = ProfilesModelHelper.isValidatorsSupported(secProfile);
                advancedConfigSupported = ProfilesModelHelper.isAdvancedSecuritySupported(secProfile);
                
                boolean defaultsSupported = ProfilesModelHelper.isServiceDefaultSetupSupported(secProfile);
                if (!defaultsSupported) defaults = false;
                devDefaultsChBox.setEnabled(defaultsSupported);

                boolean isSSL = ProfilesModelHelper.isSSLProfile(secProfile);
                if (isSSL) {
                    keyStoreConfigRequired = false;
                }
                trustStoreConfigRequired = ProfilesModelHelper.isTruststoreRequired(secProfile, binding, false);
                if (ComboConstants.PROF_KERBEROS.equals(secProfile)) {
                    keyStoreConfigRequired = false;
                    trustStoreConfigRequired = false;
                    kerberosConfigRequired = true;
                }

                if (stsAllowed) {
                    if (ComboConstants.PROF_SAMLHOLDER.equals(secProfile) ||
                        ComboConstants.PROF_SAMLSENDER.equals(secProfile) ||
                        ComboConstants.PROF_SAMLSSL.equals(secProfile)) {
                            stsAllowed = false;
                    }
                }

                if (trustStoreConfigRequired && gf) {
                    if (ComboConstants.PROF_USERNAME.equals(secProfile)) {
                        trustStoreConfigRequired = false;
                    }
                }

            } else {
                devDefaultsChBox.setEnabled(false);
            }

            secAdvancedButton.setEnabled(secSelected && !defaults && advancedConfigSupported);

            stsChBox.setEnabled(secSelected && !isFromJava && stsAllowed);

            boolean stsSelected = stsChBox.isSelected();
            stsConfigButton.setEnabled(stsSelected);

            if (stsSelected) {
                trustStoreConfigRequired = true;
                keyStoreConfigRequired = true;
                validatorsSupported = true;
            }

            validatorsButton.setEnabled(secSelected && !(ConfigVersion.CONFIG_1_0.equals(getUserExpectedConfigVersion()) && gf) && !defaults && validatorsSupported);
            keyButton.setEnabled(secSelected && keyStoreConfigRequired && !defaults);
            trustButton.setEnabled(secSelected && trustStoreConfigRequired && !defaults);
            kerberosCfgButton.setEnabled(secSelected && kerberosConfigRequired && !defaults);

            addrChBox.setEnabled(!relSelected && !secSelected);

        } else { // no wsit fun, there's access manager security selected
            profileComboLabel.setEnabled(false);
            profileCombo.setEnabled(false);
            profileInfoField.setEnabled(false);
            profConfigButton.setEnabled(false);
            stsChBox.setEnabled(false);
            devDefaultsChBox.setEnabled(false);
            stsConfigButton.setEnabled(false);
            securityChBox.setEnabled(false);
            validatorsButton.setEnabled(false);
            secAdvancedButton.setEnabled(false);
            keyButton.setEnabled(false);
            trustButton.setEnabled(false);
            profileInfoField.setEnabled(true);
            profileInfoField.setForeground(RED);
            profileInfoField.setText(NbBundle.getMessage(BindingPanel.class, "TXT_AMSecSelected"));
            addrChBox.setEnabled(false);
            cfgVersionLabel.setEnabled(false);
            cfgVersionCombo.setEnabled(false);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mtomChBox = new javax.swing.JCheckBox();
        rmChBox = new javax.swing.JCheckBox();
        securityChBox = new javax.swing.JCheckBox();
        orderedChBox = new javax.swing.JCheckBox();
        profileComboLabel = new javax.swing.JLabel();
        profileCombo = new javax.swing.JComboBox();
        rmAdvanced = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        stsChBox = new javax.swing.JCheckBox();
        tcpChBox = new javax.swing.JCheckBox();
        jSeparator3 = new javax.swing.JSeparator();
        keyButton = new javax.swing.JButton();
        trustButton = new javax.swing.JButton();
        stsConfigButton = new javax.swing.JButton();
        profConfigButton = new javax.swing.JButton();
        fiChBox = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        profileInfoField = new javax.swing.JTextArea();
        validatorsButton = new javax.swing.JButton();
        devDefaultsChBox = new javax.swing.JCheckBox();
        secAdvancedButton = new javax.swing.JButton();
        kerberosCfgButton = new javax.swing.JButton();
        cfgVersionLabel = new javax.swing.JLabel();
        cfgVersionCombo = new javax.swing.JComboBox();
        jSeparator4 = new javax.swing.JSeparator();
        addrChBox = new javax.swing.JCheckBox();
        jSeparator5 = new javax.swing.JSeparator();

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

        org.openide.awt.Mnemonics.setLocalizedText(mtomChBox, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_mtomChBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(rmChBox, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_rmChBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(securityChBox, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_securityChBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(orderedChBox, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_OrderedChBox")); // NOI18N

        profileComboLabel.setLabelFor(profileCombo);
        org.openide.awt.Mnemonics.setLocalizedText(profileComboLabel, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_profileComboLabel")); // NOI18N

        profileCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SAML Sender Vouches With Certificates", "Anonymous with Bilateral Certificates" }));

        org.openide.awt.Mnemonics.setLocalizedText(rmAdvanced, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_Advanced")); // NOI18N
        rmAdvanced.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rmAdvancedActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(stsChBox, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_stsChBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(tcpChBox, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_tcpChBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(keyButton, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_keystoreButton")); // NOI18N
        keyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(trustButton, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_truststoreButton")); // NOI18N
        trustButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trustButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(stsConfigButton, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_stsConfigButton")); // NOI18N
        stsConfigButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stsConfigButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(profConfigButton, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_keyConfigButton")); // NOI18N
        profConfigButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profConfigButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(fiChBox, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_fiChBox")); // NOI18N

        profileInfoField.setEditable(false);
        profileInfoField.setLineWrap(true);
        profileInfoField.setText("This is a text This is a text This is a text This is a text This is a text This is a text This is");
        profileInfoField.setWrapStyleWord(true);
        profileInfoField.setAutoscrolls(false);
        profileInfoField.setOpaque(false);
        jScrollPane1.setViewportView(profileInfoField);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/wsitconf/ui/service/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(validatorsButton, bundle.getString("LBL_validatorsButton")); // NOI18N
        validatorsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validatorsButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(devDefaultsChBox, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_Defaults")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(secAdvancedButton, bundle.getString("LBL_Section_Service_Advanced")); // NOI18N
        secAdvancedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secAdvancedButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(kerberosCfgButton, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_kerberosCfgButton")); // NOI18N
        kerberosCfgButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kerberosCfgButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cfgVersionLabel, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_versionChBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addrChBox, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_addrChBox")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addrChBox)
                    .addComponent(jSeparator4, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cfgVersionLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cfgVersionCombo, 0, 366, Short.MAX_VALUE))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
                    .addComponent(mtomChBox)
                    .addComponent(rmChBox)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rmAdvanced)
                            .addComponent(orderedChBox))
                        .addGap(79, 79, 79))
                    .addComponent(securityChBox)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(jScrollPane1))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(profileComboLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(profileCombo, 0, 234, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(profConfigButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(devDefaultsChBox))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(validatorsButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(secAdvancedButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(keyButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(trustButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(kerberosCfgButton)))
                        .addGap(109, 109, 109))
                    .addComponent(jSeparator3, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
                    .addComponent(tcpChBox)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(stsChBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stsConfigButton))
                    .addComponent(fiChBox)
                    .addComponent(jSeparator5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {kerberosCfgButton, keyButton, secAdvancedButton, trustButton, validatorsButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cfgVersionLabel)
                    .addComponent(cfgVersionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mtomChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rmChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(orderedChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rmAdvanced)
                .addGap(8, 8, 8)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(securityChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(profileComboLabel)
                    .addComponent(profileCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(profConfigButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(devDefaultsChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(trustButton)
                    .addComponent(keyButton)
                    .addComponent(kerberosCfgButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(secAdvancedButton)
                    .addComponent(validatorsButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stsChBox)
                    .addComponent(stsConfigButton))
                .addGap(11, 11, 11)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(tcpChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fiChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addrChBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {kerberosCfgButton, keyButton, secAdvancedButton, trustButton, validatorsButton});

        mtomChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_mtomChBox_ACSN")); // NOI18N
        mtomChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_mtomChBox_ACSD")); // NOI18N
        rmChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_rmChBox_ACSN")); // NOI18N
        rmChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_rmChBox_ACSD")); // NOI18N
        securityChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_securityChBox_ACSN")); // NOI18N
        securityChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_securityChBox_ACSD")); // NOI18N
        orderedChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_OrderedChBox_ACSN")); // NOI18N
        orderedChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_OrderedChBox_ACSD")); // NOI18N
        profileComboLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_profileComboLabel_ACSN")); // NOI18N
        profileComboLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_profileComboLabel_ACSD")); // NOI18N
        rmAdvanced.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_Advanced_ACSN")); // NOI18N
        rmAdvanced.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_Advanced_ACSD")); // NOI18N
        stsChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_stsChBox_ACSN")); // NOI18N
        stsChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_stsChBox_ACSD")); // NOI18N
        tcpChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_tcpChBox_ACSN")); // NOI18N
        tcpChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_tcpChBox_ACSD")); // NOI18N
        keyButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_keystoreButton_ACSN")); // NOI18N
        keyButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_keystoreButton_ACSD")); // NOI18N
        trustButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_truststoreButton_ACSN")); // NOI18N
        trustButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_truststoreButton_ACSD")); // NOI18N
        stsConfigButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_stsConfigButton_ACSN")); // NOI18N
        stsConfigButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_stsConfigButton_ACSD")); // NOI18N
        profConfigButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_keyConfigButton_ACSN")); // NOI18N
        profConfigButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_keyConfigButton_ACSD")); // NOI18N
        fiChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_fiChBox_ACSN")); // NOI18N
        fiChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_fiChBox_ACSD")); // NOI18N
        validatorsButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_validatorsButton_ACSN")); // NOI18N
        validatorsButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_validatorsButton_ACSD")); // NOI18N
        devDefaultsChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_Defaults_ACSN")); // NOI18N
        devDefaultsChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_Defaults_ACSD")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "Panel_ACSN")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "Panel_ACSD")); // NOI18N
        getAccessibleContext().setAccessibleParent(this);
    }// </editor-fold>//GEN-END:initComponents

    private void refresh() {

       javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addrChBox)
                    .addComponent(jSeparator4, javax.swing.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cfgVersionLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cfgVersionCombo, 0, 344, Short.MAX_VALUE))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
                    .addComponent(mtomChBox)
                    .addComponent(rmChBox)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rmAdvanced)
                            .addComponent(orderedChBox))
                        .addGap(79, 79, 79))
                    .addComponent(securityChBox)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(jScrollPane1))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(profileComboLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(profileCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(profConfigButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(devDefaultsChBox))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(validatorsButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(secAdvancedButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(keyButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(trustButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(kerberosCfgButton)))
                        .addGap(109, 109, 109))
                    .addComponent(jSeparator3, javax.swing.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
                    .addComponent(tcpChBox)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(stsChBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stsConfigButton))
                    .addComponent(fiChBox)
                    .addComponent(jSeparator5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.HORIZONTAL, kerberosCfgButton, keyButton, secAdvancedButton, trustButton, validatorsButton);

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cfgVersionLabel)
                    .addComponent(cfgVersionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mtomChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rmChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(orderedChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rmAdvanced)
                .addGap(8, 8, 8)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(securityChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(profileComboLabel)
                    .addComponent(profileCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(profConfigButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(devDefaultsChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(trustButton)
                    .addComponent(keyButton)
                    .addComponent(kerberosCfgButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(secAdvancedButton)
                    .addComponent(validatorsButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stsChBox)
                    .addComponent(stsConfigButton))
                .addGap(11, 11, 11)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(tcpChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fiChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addrChBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.VERTICAL, kerberosCfgButton, keyButton, secAdvancedButton, trustButton, validatorsButton);

        mtomChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_mtomChBox_ACSN")); // NOI18N
        mtomChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_mtomChBox_ACSD")); // NOI18N
        rmChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_rmChBox_ACSN")); // NOI18N
        rmChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_rmChBox_ACSD")); // NOI18N
        securityChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_securityChBox_ACSN")); // NOI18N
        securityChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_securityChBox_ACSD")); // NOI18N
        orderedChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_OrderedChBox_ACSN")); // NOI18N
        orderedChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_OrderedChBox_ACSD")); // NOI18N
        profileComboLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_profileComboLabel_ACSN")); // NOI18N
        profileComboLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_profileComboLabel_ACSD")); // NOI18N
        rmAdvanced.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_Advanced_ACSN")); // NOI18N
        rmAdvanced.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_Advanced_ACSD")); // NOI18N
        stsChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_stsChBox_ACSN")); // NOI18N
        stsChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_stsChBox_ACSD")); // NOI18N
        tcpChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_tcpChBox_ACSN")); // NOI18N
        tcpChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_tcpChBox_ACSD")); // NOI18N
        keyButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_keystoreButton_ACSN")); // NOI18N
        keyButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_keystoreButton_ACSD")); // NOI18N
        trustButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_truststoreButton_ACSN")); // NOI18N
        trustButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_truststoreButton_ACSD")); // NOI18N
        stsConfigButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_stsConfigButton_ACSN")); // NOI18N
        stsConfigButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_stsConfigButton_ACSD")); // NOI18N
        profConfigButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_keyConfigButton_ACSN")); // NOI18N
        profConfigButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_keyConfigButton_ACSD")); // NOI18N
        fiChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_fiChBox_ACSN")); // NOI18N
        fiChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_fiChBox_ACSD")); // NOI18N
        validatorsButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_validatorsButton_ACSN")); // NOI18N
        validatorsButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_validatorsButton_ACSD")); // NOI18N
        devDefaultsChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_Defaults_ACSN")); // NOI18N
        devDefaultsChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_Defaults_ACSD")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "Panel_ACSN")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "Panel_ACSD")); // NOI18N
        getAccessibleContext().setAccessibleParent(this);
        
    }

private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
        enableDisable();
}//GEN-LAST:event_formFocusGained

private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
        enableDisable();
}//GEN-LAST:event_formAncestorAdded

    private void validatorsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validatorsButtonActionPerformed
        String profile = (String) profileCombo.getSelectedItem();
        ValidatorsPanel vPanel = new ValidatorsPanel(binding, project, profile, getUserExpectedConfigVersion()); //NOI18N
        DialogDescriptor dlgDesc = new DialogDescriptor(vPanel,
                NbBundle.getMessage(BindingPanel.class, "LBL_Validators_Panel_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);

        dlg.setVisible(true);

        if (dlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
            vPanel.storeState();
        }

    }//GEN-LAST:event_validatorsButtonActionPerformed

    private void profConfigButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profConfigButtonActionPerformed
        String prof = (String) profileCombo.getSelectedItem();
        SecurityProfile p = SecurityProfileRegistry.getDefault().getProfile(prof);
        p.displayConfig(binding, undoManager);
    }//GEN-LAST:event_profConfigButtonActionPerformed

    private void stsConfigButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stsConfigButtonActionPerformed
        UndoCounter undoCounter = new UndoCounter();
        model.addUndoableEditListener(undoCounter);

        STSConfigServicePanel stsConfigPanel = new STSConfigServicePanel(project, binding, getUserExpectedConfigVersion());
        DialogDescriptor dlgDesc = new DialogDescriptor(stsConfigPanel,
                NbBundle.getMessage(BindingPanel.class, "LBL_STSConfig_Panel_Title")); //NOI18N
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
    }//GEN-LAST:event_stsConfigButtonActionPerformed

    private void trustButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trustButtonActionPerformed
        String profile = (String) profileCombo.getSelectedItem();
        TruststorePanel storePanel = new TruststorePanel(binding, project, jsr109, profile, false, getUserExpectedConfigVersion());
        DialogDescriptor dlgDesc = new DialogDescriptor(storePanel,
                NbBundle.getMessage(BindingPanel.class, "LBL_Truststore_Panel_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);

        dlg.setVisible(true);
        if (dlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
            storePanel.storeState();
        }
    }//GEN-LAST:event_trustButtonActionPerformed

    private void keyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyButtonActionPerformed
        KeystorePanel storePanel = new KeystorePanel(binding, project, jsr109, false, getUserExpectedConfigVersion());
        DialogDescriptor dlgDesc = new DialogDescriptor(storePanel,
                NbBundle.getMessage(BindingPanel.class, "LBL_Keystore_Panel_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);

        dlg.setVisible(true);

        if (dlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
            storePanel.storeState();
        }
    }//GEN-LAST:event_keyButtonActionPerformed

    private void rmAdvancedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rmAdvancedActionPerformed
        AdvancedRMPanel advancedRMPanel = new AdvancedRMPanel(binding, getUserExpectedConfigVersion()); //NOI18N
        DialogDescriptor dlgDesc = new DialogDescriptor(advancedRMPanel,
                NbBundle.getMessage(BindingPanel.class, "LBL_AdvancedRM_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);

        dlg.setVisible(true);

        if (dlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
            advancedRMPanel.storeState();
        }
    }//GEN-LAST:event_rmAdvancedActionPerformed

    private void secAdvancedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secAdvancedButtonActionPerformed
        AdvancedSecurityPanel advancedSecPanel = new AdvancedSecurityPanel(binding, getUserExpectedConfigVersion()); //NOI18N
        DialogDescriptor dlgDesc = new DialogDescriptor(advancedSecPanel,
                NbBundle.getMessage(BindingPanel.class, "LBL_AdvancedSec_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);

        dlg.setVisible(true);

        if (dlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
            advancedSecPanel.storeState();
        }
}//GEN-LAST:event_secAdvancedButtonActionPerformed

    private void kerberosCfgButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kerberosCfgButtonActionPerformed
        KerberosConfigPanel panel = new KerberosConfigPanel(binding, project, getUserExpectedConfigVersion());
        DialogDescriptor dlgDesc = new DialogDescriptor(panel,
                NbBundle.getMessage(BindingPanel.class, "LBL_KerberosConfig_Panel_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);

        dlg.setVisible(true);
        if (dlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
            panel.storeState();
        }
}//GEN-LAST:event_kerberosCfgButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox addrChBox;
    private javax.swing.JComboBox cfgVersionCombo;
    private javax.swing.JLabel cfgVersionLabel;
    private javax.swing.JCheckBox devDefaultsChBox;
    private javax.swing.JCheckBox fiChBox;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JButton kerberosCfgButton;
    private javax.swing.JButton keyButton;
    private javax.swing.JCheckBox mtomChBox;
    private javax.swing.JCheckBox orderedChBox;
    private javax.swing.JButton profConfigButton;
    private javax.swing.JComboBox profileCombo;
    private javax.swing.JLabel profileComboLabel;
    private javax.swing.JTextArea profileInfoField;
    private javax.swing.JButton rmAdvanced;
    private javax.swing.JCheckBox rmChBox;
    private javax.swing.JButton secAdvancedButton;
    private javax.swing.JCheckBox securityChBox;
    private javax.swing.JCheckBox stsChBox;
    private javax.swing.JButton stsConfigButton;
    private javax.swing.JCheckBox tcpChBox;
    private javax.swing.JButton trustButton;
    private javax.swing.JButton validatorsButton;
    // End of variables declaration//GEN-END:variables
}
