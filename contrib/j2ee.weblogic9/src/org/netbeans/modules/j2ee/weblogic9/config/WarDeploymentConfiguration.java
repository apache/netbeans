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

package org.netbeans.modules.j2ee.weblogic9.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ContextRootConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentDescriptorConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentPlanConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ServerLibraryConfiguration;
import org.netbeans.modules.j2ee.weblogic9.dd.model.WebApplicationModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;

/**
 * Web module deployment configuration handles creation and updating of the 
 * weblogic.xml configuration file.
 *
 * @author Petr Hejl
 * @author sherold
 */
public class WarDeploymentConfiguration extends WLDeploymentConfiguration
        implements ServerLibraryConfiguration, ModuleConfiguration,
        ContextRootConfiguration, DeploymentPlanConfiguration, PropertyChangeListener, DeploymentDescriptorConfiguration {

    private static final Logger LOGGER = Logger.getLogger(WarDeploymentConfiguration.class.getName());

    private final ChangeSupport serverLibraryChangeSupport = new ChangeSupport(this);
    
    private final ConfigurationModifier<WebApplicationModel> modifier = new ConfigurationModifier<WebApplicationModel>();

    private final File file;

    private final J2eeModule j2eeModule;

    private final DataObject dataObject;

    private final FileChangeListener weblogicXmlListener = new WeblogicXmlListener();
    
    private final Version serverVersion;

    private WebApplicationModel webLogicWebApp;

    private Set<ServerLibraryDependency> originalDeps;
    
    public WarDeploymentConfiguration(J2eeModule j2eeModule) {
        this(j2eeModule, null);
    }

    /**
     * Creates a new instance of WarDeploymentConfiguration 
     */
    public WarDeploymentConfiguration(J2eeModule j2eeModule, Version serverVersion) {

        super(j2eeModule, serverVersion);
        this.j2eeModule = j2eeModule;
        this.serverVersion = serverVersion;
        file = j2eeModule.getDeploymentConfigurationFile("WEB-INF/weblogic.xml"); // NOI18N
        FileUtil.addFileChangeListener(weblogicXmlListener, file);

        getWeblogicWebApp();
        DataObject dataObject = null;
        try {
            dataObject = DataObject.find(FileUtil.toFileObject(file));
            dataObject.addPropertyChangeListener(this);
        } catch(DataObjectNotFoundException donfe) {
            Exceptions.printStackTrace(donfe);
        }
        this.dataObject = dataObject;
    }
    
    public Lookup getLookup() {
        return Lookups.fixed(this);
    }
    

    public J2eeModule getJ2eeModule() {
        return j2eeModule;
    }

    public void dispose() {
        if (dataObject != null) {
            dataObject.removePropertyChangeListener(this);
        }
    }

    @Override
    public boolean isDescriptorRequired() {
        return true;
    }
    
    /**
     * Listen to weblogic.xml document changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == DataObject.PROP_MODIFIED &&
                Boolean.FALSE.equals(evt.getNewValue())) {
            // dataobject has been modified, webLogicWebApp graph is out of sync
            synchronized (this) {
                webLogicWebApp = null;
            }
        }
    }
   
    /**
     * Return WeblogicWebApp graph. If it was not created yet, load it from the file
     * and cache it. If the file does not exist, generate it.
     *
     * @return WeblogicWebApp graph or null if the weblogic.xml file is not parseable.
     */
    public final synchronized WebApplicationModel getWeblogicWebApp() {
        if (webLogicWebApp == null) {
            try {
                if (file.exists()) {
                    // load configuration if already exists
                    try {
                        webLogicWebApp = WebApplicationModel.forFile(file);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    } catch (RuntimeException re) {
                        // weblogic.xml is not parseable, do nothing
                        LOGGER.log(Level.INFO, null, re);
                    }
                } else {
                    // create weblogic.xml if it does not exist yet
                    webLogicWebApp = generateWeblogicWebApp();
                    webLogicWebApp.write(file);
                }
            } catch (ConfigurationException ce) {
                Exceptions.printStackTrace(ce);
            }
        }
        return webLogicWebApp;
    }
    
    // FIXME this is not a proper implementation - deployment PLAN should be saved
    // not a deployment descriptor
    public void save(OutputStream os) throws ConfigurationException {
        WebApplicationModel webLogicWebApp = getWeblogicWebApp();
        if (webLogicWebApp == null) {
            String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_cannotSaveNotParseableConfFile", file.getPath());
            throw new ConfigurationException(msg);
        }
        try {
            webLogicWebApp.write(os);
        } catch (IOException ioe) {
            String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_CannotUpdateFile", file.getPath());
            throw new ConfigurationException(msg, ioe);
        }
    }
    
    /**
     * Genereate Context graph.
     */
    private WebApplicationModel generateWeblogicWebApp() {
        WebApplicationModel webApp = WebApplicationModel.generate(serverVersion);
        webApp.setContextRoot("");
        webApp.setKeepJspGenerated(true);
        webApp.setDebug(true);
// DISABLED SINCE 7.1.1 see #206798
//        webApp.setFastSwap(true);
        return webApp;
    }
    
    // TODO: this contextPath fix code will be removed, as soon as it will 
    // be moved to the web project
    private boolean isCorrectCP(String contextPath) {
        boolean correct = true;
        if (!contextPath.equals("") && !contextPath.startsWith("/")) { //NOI18N
            correct = false;
        } else if (contextPath.endsWith("/")) { //NOI18N
            correct = false;
        } else if (contextPath.contains("//")) { //NOI18N
            correct = false;
        }
        return correct;
    }
    
    public String getContextRoot() throws ConfigurationException {
        WebApplicationModel webLogicWebApp = getWeblogicWebApp();
        if (webLogicWebApp == null) { // graph not parseable
            String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_CannotReadContextRoot", file.getPath());
            throw new ConfigurationException(msg);
        }
        return webLogicWebApp.getContextRoot();
    }

    public void setContextRoot(String contextRoot) throws ConfigurationException {
        // TODO: this contextPath fix code will be removed, as soon as it will 
        // be moved to the web project
        
        String currentCP = "";
        if (contextRoot != null) {
            currentCP = contextRoot;
        }

        if (!isCorrectCP(currentCP)) {
            String ctxRoot = currentCP;
            java.util.StringTokenizer tok = new java.util.StringTokenizer(currentCP,"/"); //NOI18N
            StringBuffer buf = new StringBuffer(); //NOI18N
            while (tok.hasMoreTokens()) {
                buf.append("/"+tok.nextToken()); //NOI18N
            }
            ctxRoot = buf.toString();
            NotifyDescriptor desc = new NotifyDescriptor.Message(
                    NbBundle.getMessage (WarDeploymentConfiguration.class, "MSG_invalidCP", currentCP),
                    NotifyDescriptor.Message.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            currentCP = ctxRoot;
        }
        final String newContextPath = currentCP;
        modifier.modify(new WeblogicWebAppModifier() {
            @Override
            public void modify(WebApplicationModel webLogicWebApp) {
                webLogicWebApp.setContextRoot(newContextPath);
            }
        }, dataObject, file);
    }

    @Override
    public void bindDatasourceReference(final String referenceName, final String jndiName) throws ConfigurationException {
        if (referenceName == null || referenceName.length() == 0
                || jndiName == null || jndiName.length() == 0) {
            return;
        }

        modifier.modify(new WeblogicWebAppModifier() {
            @Override
            public void modify(WebApplicationModel webLogicWebApp) {
                webLogicWebApp.setReference(referenceName, jndiName);
            }
        }, dataObject, file);
    }

    @Override
    public String findDatasourceJndiName(String referenceName) throws ConfigurationException {
        WebApplicationModel webLogicWebApp = getWeblogicWebApp();
        if (webLogicWebApp == null) { // graph not parseable
            String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_CannotReadReferenceName", file.getPath());
            throw new ConfigurationException(msg);
        }
        return webLogicWebApp.getReferenceJndiName(referenceName);
    }

    @Override
    public void bindMessageDestinationReference(String referenceName, String connectionFactoryName, String destName, Type type) throws ConfigurationException {
        // noop for 1.5
    }

    @Override
    public void configureLibrary(@NonNull final ServerLibraryDependency library) throws ConfigurationException {
        assert library != null;

        modifier.modify(new WeblogicWebAppModifier() {
            @Override
            public void modify(WebApplicationModel webLogicWebApp) {
                webLogicWebApp.addLibrary(library);
            }
        }, dataObject, file);
    }

    @Override
    public Set<ServerLibraryDependency> getLibraries() throws ConfigurationException {
        WebApplicationModel webLogicWebApp = getWeblogicWebApp();
        if (webLogicWebApp == null) { // graph not parseable
            String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_CannotReadServerLibraries", file.getPath());
            throw new ConfigurationException(msg);
        }

        return webLogicWebApp.getLibraries();
    }

    @Override
    public void addLibraryChangeListener(@NonNull ChangeListener listener) {
        Parameters.notNull("listener", listener);

        boolean load = false;
        synchronized (this) {
            load = originalDeps == null;
        }
        if (load) {
            Set<ServerLibraryDependency> deps = null;
            try {
                deps = getLibraries();
            } catch(ConfigurationException ex) {
                deps = Collections.emptySet();
            }
            synchronized (this) {
                if (originalDeps == null) {
                    originalDeps = deps;
                }
            }
        }

        serverLibraryChangeSupport.addChangeListener(listener);
    }

    @Override
    public void removeLibraryChangeListener(@NonNull ChangeListener listener) {
        Parameters.notNull("listener", listener);
        serverLibraryChangeSupport.removeChangeListener(listener);
    }

    private void fireChange() {
        Set<ServerLibraryDependency> oldDeps = null;
        synchronized (this) {
            if (originalDeps == null) {
                // nobody is listening
                return;
            }
            oldDeps = new HashSet<ServerLibraryDependency>(originalDeps);
        }

        Set<ServerLibraryDependency> deps = new HashSet<ServerLibraryDependency>();
        try {
            deps.addAll(getLibraries());
        } catch (ConfigurationException ex) {
            // noop - empty set
        }
        boolean fire = false;
        for (ServerLibraryDependency old : oldDeps) {
            if (!deps.remove(old)) {
                fire = true;
                break;
            }
        }
        if (!deps.isEmpty()) {
            fire = true;
        }
        if (fire) {
            serverLibraryChangeSupport.fireChange();
        }
    }

    private class WeblogicXmlListener implements FileChangeListener {

        @Override
        public void fileFolderCreated(FileEvent fe) {
            // noop
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            fireChange();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            fireChange();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            fireChange();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            fireChange();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // noop
        }
    }

    private abstract class WeblogicWebAppModifier implements ConfigurationModifier.DescriptorModifier<WebApplicationModel> {

        @Override
        public WebApplicationModel load() {
            return getWeblogicWebApp();
        }

        @Override
        public WebApplicationModel load(byte[] source) throws IOException {
            return WebApplicationModel.forInputStream(new ByteArrayInputStream(source));
        }

        @Override
        public void save(WebApplicationModel context) {
            synchronized (WarDeploymentConfiguration.this) {
                webLogicWebApp = context;
            }
        }
    }
}
