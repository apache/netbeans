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
package org.netbeans.modules.j2ee.weblogic9.ui.wizard;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.WLTrustHandler;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * The main class of the custom wizard for registering a new server instance.
 * It performs all the orchestration of the panels and actually creates the
 * instance.
 *
 * @author Kirill Sorokin
 */
public class WLInstantiatingIterator  implements WizardDescriptor.InstantiatingIterator {

    private static final Logger LOGGER = Logger.getLogger(WLInstantiatingIterator.class.getName());

    /**
     * Since the WizardDescriptor does not expose the property name for the
     * error message label, we have to keep it here also
     */
    private static final String PROP_DISPLAY_NAME = "ServInstWizard_displayName"; // NOI18N

    /**
     * The default debugger port for the instance, it will be assigned to it
     * at creation time and can be changed via the properties sheet
     */
    public static final String DEFAULT_DEBUGGER_PORT = "8787"; // NOI18N

    public static final String DEFAULT_PROXY_ENABLED = "true"; // NOI18N

    public static final String DEFAULT_MAC_MEM_OPTS_HEAP = "-Xmx1024m"; // NOI18N

    public static final String DEFAULT_MAC_MEM_OPTS_PERM = "-XX:PermSize=256m"; // NOI18N

    private static final Version JDK8_ONLY_SERVER_VERSION = Version.fromJsr277NotationWithFallback("12.2.1"); // NOI18N

    /**
     * The parent wizard descriptor
     */
    private WizardDescriptor wizardDescriptor;

    /**
     * A misterious method whose purpose is obviously in freeing the resources
     * obtained by the wizard during instance registration. We do not need such
     * functionality, thus we do not implement it.
     */
    public void uninitialize(WizardDescriptor wizardDescriptor) {
        // do nothing as we do not need to release any resources
    }

    /**
     * This method initializes the wizard. AS for us the only thing we should
     * do is save the wizard descriptor handle.
     *
     * @param wizardDescriptor the parent wizard descriptor
     */
    public void initialize(WizardDescriptor wizardDescriptor) {
        this.wizardDescriptor = wizardDescriptor;
    }

    /**
     * Returns the name for the wizard. I failed to find a place where it
     * could be used, so we do not return anything sensible
     *
     * @return the wizard name
     */
    public String name() {
        return ""; // NOI18N
    }

    /**
     * This methos actually creates the instance. It fetches all the required
     * parameters, builds the URL and calls
     * InstanceProperties.createInstamceProperties(), which registers the
     * instance.
     *
     * @return a set of created instance properties
     */
    @Override
    public Set instantiate() throws IOException {
        // initialize the resulting set
        Set result = new HashSet();

        String displayName = (String) wizardDescriptor.getProperty(PROP_DISPLAY_NAME);

        result.add(instantiate(displayName));
        return result;
    }

    private InstanceProperties instantiate(String displayName) throws IOException {
        // if all the data is normally validated - create the instance and
        // attach the additional properties
        Map<String, String> props = new HashMap<String, String>();
        props.put(WLPluginProperties.SERVER_ROOT_ATTR, serverRoot);
        props.put(WLPluginProperties.DOMAIN_ROOT_ATTR, domainRoot);
        props.put(WLPluginProperties.DOMAIN_NAME, domainName);
        props.put(WLPluginProperties.PORT_ATTR, port);
        props.put(WLPluginProperties.HOST_ATTR, host);
        props.put(WLPluginProperties.REMOTE_ATTR, Boolean.toString(remote));
        props.put(WLPluginProperties.SECURED_ATTR, Boolean.toString(ssl));
        props.put(WLTrustHandler.TRUST_EXCEPTION_PROPERTY, null);
        props.put(WLPluginProperties.REMOTE_DEBUG_ENABLED, Boolean.toString(remoteDebug));
        props.put(WLPluginProperties.PROXY_ENABLED, DEFAULT_PROXY_ENABLED);
        if (remoteDebug) {
            props.put(WLPluginProperties.DEBUGGER_PORT_ATTR, debugPort);
        } else {
            props.put(WLPluginProperties.DEBUGGER_PORT_ATTR, DEFAULT_DEBUGGER_PORT);
        }

        if (Utilities.isMac()) {
            StringBuilder memOpts = new StringBuilder(DEFAULT_MAC_MEM_OPTS_HEAP);
            Version version = WLPluginProperties.getServerVersion(new File(serverRoot));
            if (version != null && !JDK8_ONLY_SERVER_VERSION.isBelowOrEqual(version)) {
                memOpts.append(' '); // NOI18N
                memOpts.append(DEFAULT_MAC_MEM_OPTS_PERM);
            }
            props.put(WLPluginProperties.MEM_OPTS, memOpts.toString());
        }

        try {
            // remove the certificate from the struststore - safety catch
            WLTrustHandler.removeFromTrustStore(url);
        } catch (GeneralSecurityException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        InstanceProperties ip = InstanceProperties.createInstanceProperties(
                url, username, password, displayName, props);

        return ip;
    }

    /**
     * Helper method for decorating error message as HTML. Workaround for line wrap.
     */
    /*package*/ static String decorateMessage(String message) {
        if (message == null) {
            return null;
        }
        if (message.toUpperCase(Locale.ENGLISH).startsWith("<HTML>")) {
            return message;
        }
        return "<html>" + message.replace("<",  "&lt;").replace(">",  "&gt;") + "</html>"; // NIO18N
    }
    // the main and additional instance properties
    private String serverRoot;
    private String domainRoot;
    private String username;
    private String password;
    private String url;
    private String domainName;
    private String port;
    private String debugPort;
    private String host;
    private boolean remote;
    private boolean ssl;
    private boolean remoteDebug;
    private Version serverVersion;


    /**
     * Setter for the instance url.
     *
     * @param url the new instance url
     */
    public void setUrl(String url) {
        this.url = url;
    }
    
    /**
     * Setter for the instance host.
     *
     * @param url the new instance host
     */
    public void setHost(String host) {
        this.host = host;
    }
    
    /**
     * Setter for port.
     * @param port the new instance port
     */
    public void setPort(String port){
        this.port = port;
    }
    
    /**
     * Setter for domain name.
     * @param name the new instance domain name
     */
    public void setDomainName(String name){
        domainName = name;
    }

    /**
     * Setter for the server installation directory.
     *
     * @param serverRoot the new server installation directory path
     */
    public void setServerRoot(String serverRoot) {
        this.serverRoot = serverRoot;

        // reinit the instances list
        if (serverPropertiesPanel instanceof ServerLocalPropertiesPanel) {
            ((ServerLocalPropertiesPanel) serverPropertiesPanel).getVisual().updateInstancesList();
            ((ServerLocalPropertiesPanel) serverPropertiesPanel).getVisual().updateJpa2Button();
        }
    }

    /**
     * Getter for the server installation directory
     *
     * @return the server installation directory path
     */
    public String getServerRoot() {
        return this.serverRoot;
    }
    
    /**
     * Getter for the http port
     *
     * @return the http port
     */
    public String getPort(){
        return port;
    }
    
    /**
     * Getter for the host
     *
     * @return the host
     */
    public String getHost(){
        return host;
    }
    
    /**
     * Getter for the domain name
     *
     * @return the domain name
     */
    public String getDomainName(){
        return domainName;
    }

    /**
     * Setter for the profile root directory
     *
     * @param domainRoot the new profile root directory path
     */
    public void setDomainRoot(String domainRoot) {
        this.domainRoot = domainRoot;
    }

    /**
     * Getter for the profile root directory
     *
     * @return the profile root directory path
     */
    public String getDomainRoot() {
        return domainRoot;
    }

    /**
     * Setter for the username
     *
     * @param username the new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Setter for the password
     *
     * @param password the new password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRemote() {
        return remote;
    }

    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public boolean isRemoteDebug() {
        return remoteDebug;
    }

    public void setRemoteDebug(boolean remoteDebug) {
        this.remoteDebug = remoteDebug;
    }

    public String getDebugPort() {
        return debugPort;
    }

    public void setDebugPort(String debugPort) {
        this.debugPort = debugPort;
    }

    public Version getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(Version serverVersion) {
        this.serverVersion = serverVersion;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Panels section
    ////////////////////////////////////////////////////////////////////////////
    /**
     * The steps names for the wizard: Server Location & Instance properties
     */
    private final String[] steps = new String[] {
        NbBundle.getMessage(WLInstantiatingIterator.class, "SERVER_LOCATION_STEP"),  // NOI18N
        NbBundle.getMessage(WLInstantiatingIterator.class, "SERVER_PROPERTIES_STEP") // NOI18N
    };

    /**
     * The wizard's panels
     */
    private ServerLocationPanel serverLocationPanel;
    private WizardDescriptor.Panel serverPropertiesPanel;

    /**
     * Index of the currently shown panel
     */
    private int index = 0;

    /**
     * Tells whether the wizard has previous panels. Basically controls the
     * Back button
     */
    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    /**
     * Reverts the wizard to the previous panel if available.
     * If the previous panel is not available a NoSuchElementException will be
     * thrown.
     */
    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    /**
     * Tells whether the wizard has next panels. Basically controls the
     * Next button
     */
    @Override
    public boolean hasNext() {
        return index < 1;
    }

    /**
     * Proceeds the wizard to the next panel if available.
     * If the next panel is not available a NoSuchElementException will be
     * thrown.
     */
    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    /**
     * Returns the current panel of the wizard
     *
     * @return current panel of the wizard
     */
    @Override
    public WizardDescriptor.Panel current() {
        switch (index) {
            case 0:
               return getLocationPanel();
            case 1:
                return getPropertiesPanel();
            default:
                throw new IllegalStateException();
        }
    }

    private WizardDescriptor.Panel getLocationPanel() {
        if (serverLocationPanel == null) {
            serverLocationPanel = new ServerLocationPanel(this);
            initComponent(serverLocationPanel.getComponent(), 0);
        }
        return serverLocationPanel;
    }

    private WizardDescriptor.Panel getPropertiesPanel() {
        if ((isRemote() && (serverPropertiesPanel instanceof ServerRemotePropertiesPanel))
                || !isRemote() && (serverPropertiesPanel instanceof ServerLocalPropertiesPanel)) {
            return serverPropertiesPanel;
        }

        serverPropertiesPanel = isRemote() ? createRemotePanel() : createLocalPanel();
        return serverPropertiesPanel;
    }

    private WizardDescriptor.Panel createLocalPanel() {
        ServerLocalPropertiesPanel serverLocalPanel = new ServerLocalPropertiesPanel(this);
        initComponent(serverLocalPanel.getComponent(), 1);
        serverLocalPanel.getVisual().updateInstancesList();
        serverLocalPanel.getVisual().updateJpa2Button();
        return serverLocalPanel;
    }

    private WizardDescriptor.Panel createRemotePanel() {
        ServerRemotePropertiesPanel serverRemotePanel = new ServerRemotePropertiesPanel(this);
        initComponent(serverRemotePanel.getComponent(), 1);
        return serverRemotePanel;
    }

    private void initComponent(Component c, int step) {
        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            // Step #.
            jc.putClientProperty(
                WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, step);

            // Step name (actually the whole list for reference).
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
        }
    }


    ////////////////////////////////////////////////////////////////////////////
    // Listeners section
    ////////////////////////////////////////////////////////////////////////////
    /**
     * The registered listeners
     */
    private final List<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();

    /**
     * Removes an already registered listener in a synchronized manner
     *
     * @param listener a listener to be removed
     */
    @Override
    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Registers a new listener in a synchronized manner
     *
     * @param listener a listener to be registered
     */
    @Override
    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Notifies all the listeners of the supplied event
     *
     * @param event the event to be passed to the listeners
     */
    private void fireChangeEvent(ChangeEvent event) {
        for (ChangeListener l : listeners) {
            l.stateChanged(event);
        }
    }
}
