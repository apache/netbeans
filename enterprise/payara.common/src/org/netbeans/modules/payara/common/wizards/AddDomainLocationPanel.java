/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.payara.common.wizards;

import java.awt.Component;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.payara.tooling.data.PayaraVersion;
import org.netbeans.modules.payara.tooling.server.config.ConfigBuilderProvider;
import org.netbeans.modules.payara.tooling.server.config.PayaraConfigManager;
import org.netbeans.modules.payara.tooling.server.config.JavaSEPlatform;
import org.netbeans.modules.payara.tooling.server.config.JavaSESet;
import org.netbeans.modules.payara.tooling.utils.ServerUtils;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.payara.common.PayaraInstance;
import org.netbeans.modules.payara.common.ui.IpComboBox;
import org.netbeans.modules.payara.spi.ServerUtilities;
import org.netbeans.modules.payara.spi.Utils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.payara.tooling.data.PayaraConfig;

public class AddDomainLocationPanel implements WizardDescriptor.Panel, ChangeListener {

    private final String PROP_ERROR_MESSAGE = WizardDescriptor.PROP_ERROR_MESSAGE;
    private final String PROP_WARNING_MESSAGE = WizardDescriptor.PROP_WARNING_MESSAGE;
    private final String PROP_INFO_MESSAGE = WizardDescriptor.PROP_INFO_MESSAGE;

    private final ServerWizardIterator wizardIterator;
    private AddDomainLocationVisualPanel component;
    private WizardDescriptor wizard;
    private final transient List<ChangeListener> listeners
            = new CopyOnWriteArrayList<>();
    private String gfRoot;

    /** Default Java SE platform is supported by selected Payara server. */
    boolean defaultJavaSESupported;

    /**
     * 
     * @param instantiatingIterator 
     */
    public AddDomainLocationPanel(ServerWizardIterator wizardIterator) {
        this.wizardIterator = wizardIterator;
        wizard = null;
    }

    private final AtomicBoolean isValidating = new AtomicBoolean();

    /**
     * 
     * @return 
     */
    @Override
    public boolean isValid() {
        if (isValidating.compareAndSet(false, true)) {
            try {
                AddDomainLocationVisualPanel panel = (AddDomainLocationVisualPanel) getComponent();
                if (wizardIterator.isLocal()) {
                    return validateForLocalDomain(panel);
                } else {
                    return validateForRemoteDomain(panel);
                }
            } finally {
                isValidating.set(false);
            }
        }
        return true;
    }

    /**
     * 
     * @param ev 
     */
    @Override
    public void stateChanged(ChangeEvent ev) {
        fireChangeEvent(ev);
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChangeEvent(ChangeEvent ev) {
        for(ChangeListener listener: listeners) {
            listener.stateChanged(ev);
        }
    }

    /**
     * 
     * @return 
     */
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new AddDomainLocationVisualPanel();
            component.addChangeListener(this);
        }
        return component;
    }

    /**
     * 
     * @return 
     */
    @Override
    public HelpCtx getHelp() {
        FileObject fo = FileUtil.getConfigFile("Services/JavaHelp/org-netbeans-modules-usersguide-helpset.xml");
        if (null != fo)
            return new HelpCtx("registering_app_server_hk2_domain"); //NOI18N
        else
            return null;
    }

    /**
     * 
     * @param settings 
     */
    @Override
    public void readSettings(Object settings) {
        if (wizard == null) {
            wizard = (WizardDescriptor) settings;
        }
        gfRoot = wizardIterator.getPayaraRoot();
        ((AddDomainLocationVisualPanel) getComponent())
                .initModels(gfRoot, wizardIterator.isLocal());

        // Check if default Java SE platform is supported\
        // by selected Payara server.
        JavaPlatform defaultJava = JavaPlatform.getDefault();
        JavaSEPlatform defaultJavaPlatform = JavaSEPlatform.toValue(
                    defaultJava.getSpecification().getVersion().toString());
        PayaraVersion gpayaraVersion
                = ServerUtils.getServerVersion(gfRoot);
        PayaraConfig configAdapter = PayaraConfigManager.getConfig(
                ConfigBuilderProvider.getBuilderConfig(gpayaraVersion));
        JavaSESet javaSEConfig = configAdapter != null
                ? configAdapter.getJavaSE() : null;
        Set<JavaSEPlatform> javaSEPlatfors = javaSEConfig != null
                ? javaSEConfig.platforms() : null;
        defaultJavaSESupported = javaSEPlatfors != null
                ? javaSEPlatfors.contains(defaultJavaPlatform) : false;
        wizardIterator.serDefaultJavaSESupported(defaultJavaSESupported);
    }

    /**
     * 
     * @param settings 
     */
    @Override
    public void storeSettings(Object settings) {
    }

    /**
     * Sets Payara server target, administrator's user name and password into
     * wizard iterator.
     * <p/>
     * @param wizardIterator Target wizard iterator object.
     * @param panel Source wizard panel component.
     */
    private static void setGlobalValues(ServerWizardIterator wizardIterator, AddDomainLocationVisualPanel panel) {
        wizardIterator.setTargetValue(panel.getTargetValue());
        wizardIterator.setUserName(panel.getUserNameValue());
        wizardIterator.setPassword(panel.getPasswordValue());
        wizardIterator.setAdminPort(Integer.parseInt(panel.getAdminPortValue()));
        wizardIterator.setHttpPort(Integer.parseInt(panel.getHttpPortValue()));
    }

    private String validateLocalHost(final Object rawHost) {
        if (rawHost instanceof IpComboBox.InetAddr) {
            return ((IpComboBox.InetAddr)rawHost).toString();
        } else if (rawHost instanceof String) {
            String host = (String)rawHost;
            if (host.length() == 0) {
                host = IpComboBox.IP_4_127_0_0_1_NAME;
            }
            return host;
        } else {
            return IpComboBox.IP_4_127_0_0_1_NAME;
        }
    }
    
    private boolean validateForLocalDomain(AddDomainLocationVisualPanel panel) throws MissingResourceException {
        String domainField = panel.getLocalDomain().trim();
        File domainDirCandidate = new File(gfRoot, PayaraInstance.DEFAULT_DOMAINS_FOLDER + File.separator + domainField); // NOI18N
        String host = validateLocalHost(panel.getLocalHost());
        if (domainField.length() < 1) {
            if (!Utils.canWrite(domainDirCandidate)) {
                // the user needs to enter the name of a directory for
                // a personal domain
                wizard.putProperty(PROP_INFO_MESSAGE, NbBundle.getMessage(this.getClass(), "MSG_EnterDomainDirectory")); // NOI18N
            } else {
                // the user probably deleted a valid name from the field.
                wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(this.getClass(), "MSG_MustHaveName")); // NOI18N
            }
            return false;
        }
        int dex = domainField.indexOf(File.separator);
        // Existing domain
        if (AddServerLocationPanel.isRegisterableDomain(domainDirCandidate, wizardIterator)) {
            String uri = wizardIterator.formatUri(PayaraInstance.DEFAULT_HOST_NAME, wizardIterator.getAdminPort(), panel.getTargetValue(),
                    new File(gfRoot, PayaraInstance.DEFAULT_DOMAINS_FOLDER).getAbsolutePath(), domainField);
            if (-1 == wizardIterator.getHttpPort()) {
                wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(this.getClass(), "ERR_InvalidDomainData", domainField)); // NOI18N
                return false;
            }
            if (-1 == wizardIterator.getAdminPort()) {
                wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(this.getClass(), "ERR_InvalidDomainData", domainField)); // NOI18N
                return false;
            }
            if (wizardIterator.hasServer(uri)) {
                wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(this.getClass(), "ERR_DomainAlreadyRegistered", domainField)); // NOI18N
                return false;
            }
            // the entry resolves to a domain name that we can register
            wizardIterator.setDomainLocation(domainDirCandidate.getAbsolutePath());
            // Let's believe to what user provided in UI
            wizardIterator.setHostName(host);
            panel.setPortsFields(wizardIterator.getAdminPort(),
                    wizardIterator.getHttpPort(), true);
            setGlobalValues(wizardIterator, panel);
            wizard.putProperty(PROP_INFO_MESSAGE, NbBundle.getMessage(this.getClass(), "MSG_RegisterExistingEmbedded", domainField)); // NOI18N
            return true;
        } else {
            panel.setPortsFields(false);
            List<String> errors = new LinkedList<>();
            int dasPort = strToInt(panel.getAdminPortValue(),
                    "AddDomainLocationPanel.invalidDasPort", errors);
            int httpPort = strToInt(panel.getHttpPortValue(),
                    "AddDomainLocationPanel.invalidHttpPort", errors);
            if (dasPort < 0 || httpPort < 0) {
                if(errors.isEmpty()) {
                    if (dasPort < 0){
                       errors.add(NbBundle.getMessage(this.getClass(), "AddDomainLocationPanel.invalidDasPort", dasPort));
                    }
                    if (httpPort < 0){
                       errors.add(NbBundle.getMessage(this.getClass(), "AddDomainLocationPanel.invalidHttpPort", httpPort));
                    }
                }
                wizard.putProperty(PROP_ERROR_MESSAGE, joinErrorMessages(errors));
                return false;
            }
            if (dasPort == httpPort) {
                wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(this.getClass(), "AddDomainLocationPanel.duplicatePortAssigned", httpPort));
                return false;
            }
        }
        File domainsDir = domainDirCandidate.getParentFile();
        if (Utils.canWrite(domainsDir) && dex < 0 && !ServerUtilities.isTP2(gfRoot) &&
                !domainDirCandidate.exists()) {
            wizardIterator.setDomainLocation(domainDirCandidate.getAbsolutePath());
            wizardIterator.setHostName(host);
            wizardIterator.setUseDefaultPorts(panel.getUseDefaultPorts());
            setGlobalValues(wizardIterator, panel);
            if (defaultJavaSESupported) {
                wizard.putProperty(PROP_INFO_MESSAGE,
                        NbBundle.getMessage(this.getClass(),
                        "MSG_CreateEmbedded", domainField)); // NOI18N
            } else {
                wizard.putProperty(PROP_WARNING_MESSAGE,
                        NbBundle.getMessage(this.getClass(),
                        "WRN_CreateEmbedded", domainField)); // NOI18N               
            }
            return defaultJavaSESupported;
        }
        domainDirCandidate = new File(domainField);
        String domainLoc = domainDirCandidate.getAbsolutePath();
        if (AddServerLocationPanel.isRegisterableDomain(domainDirCandidate)) {
            // the entry resolves to a domain name that we can register
            //String domainLoc = domainDirCandidate.getAbsolutePath();
            wizardIterator.setDomainLocation(domainLoc);
            wizardIterator.setHostName(host);
            setGlobalValues(wizardIterator, panel);
            wizard.putProperty(PROP_INFO_MESSAGE, NbBundle.getMessage(this.getClass(), "MSG_RegisterExisting", domainField)); // NOI18N
            org.netbeans.modules.payara.common.utils.Util.readServerConfiguration(domainDirCandidate, wizardIterator);
            return true;
        }
        if (AddServerLocationPanel.canCreate(domainDirCandidate) && !ServerUtilities.isTP2(gfRoot)) {
            wizardIterator.setDomainLocation(domainLoc);
            wizard.putProperty(PROP_INFO_MESSAGE, NbBundle.getMessage(this.getClass(), "MSG_CreateDomain", domainField)); // NOI18N
            wizardIterator.setUseDefaultPorts(panel.getUseDefaultPorts());
            wizardIterator.setHostName(host);
            setGlobalValues(wizardIterator, panel);
            return true;
        }
        if (new File(domainsDir, domainField).exists()) {
            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(this.getClass(), "ERR_UnusableDomain", domainField)); // NOI18N
        } else if (domainDirCandidate.exists()) {
            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(this.getClass(), "ERR_UnusableDomain", domainField)); // NOI18N
        } else {
            wizard.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(this.getClass(), "ERR_CannotCreateDomain", domainField)); // NOI18N
        }
        return false;
    }

    /**
     * Convert {@link String} representation of integer value into real integer.
     * <p/>
     * @param str    {@link String} representation of integer value.
     * @param msgKey Error message key.
     * @return Real non negative integer value or <code>-1</code> when value
     *         could not be converted
     */
    private int strToInt(final String str, final String msgKey,
            final List<String> errors) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            errors.add(NbBundle.getMessage(this.getClass(), msgKey, str));
            return -1;
        }        
    }

    /***
     * Join error messages.
     * <p/>
     * @param errors {@link List} of individual error messages to be joined.
     * @return Joined error messages from errors {@link List}.
     */
    private String joinErrorMessages(final List<String> errors) {
        final String eol = "<br/>";
        int length = 0;
        for (String error : errors) {
            length += (length > 0 ? eol.length() : 0) + error.length();
        }
        StringBuilder sb = new StringBuilder(length);
        for (String error : errors) {
            if (sb.length() > 0) {
                sb.append(eol);
            }
            sb.append(error);
        }
        return sb.toString();
    }
    
    /**
     * There is not much to verify for remote domain.
     * <p/>
     * @param panel Domain specific attributes panel.
     * @return Is form valid?
     */
    private boolean validateForRemoteDomain(
            final AddDomainLocationVisualPanel panel) {
        String host = panel.getRemoteHost();
        List<String> errors = new LinkedList<>();
        int dasPort = strToInt(panel.getAdminPortValue(),
                "AddDomainLocationPanel.invalidDasPort", errors);
        int httpPort = strToInt(panel.getHttpPortValue(),
                "AddDomainLocationPanel.invalidHttpPort", errors);
        if (dasPort < 0 || httpPort < 0) {
            wizard.putProperty(PROP_ERROR_MESSAGE, joinErrorMessages(errors));
            return false;
        }
        wizardIterator.setAdminPort(dasPort);
        wizardIterator.setHttpPort(httpPort);
        wizardIterator.setHostName(host);
        wizardIterator.setRemoteDomain(panel.getRemoteDomain());
        setGlobalValues(wizardIterator, panel);
        wizard.putProperty(PROP_INFO_MESSAGE, NbBundle.getMessage(
                this.getClass(), "AddDomainLocationPanel.remoteInstance",
                host, Integer.toString(dasPort), Integer.toString(httpPort)));
        return true;
    }
    
}
