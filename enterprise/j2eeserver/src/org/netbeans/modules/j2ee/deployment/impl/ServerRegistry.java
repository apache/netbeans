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

package org.netbeans.modules.j2ee.deployment.impl;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.deployment.plugins.api.AlreadyRegisteredException;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInitializationException;
import org.netbeans.modules.j2ee.deployment.profiler.spi.Profiler;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public final class ServerRegistry implements java.io.Serializable {

    private static final Logger LOGGER = Logger.getLogger(ServerRegistry.class.getName());

    public static final String DIR_INSTALLED_SERVERS = "/J2EE/InstalledServers"; //NOI18N
    public static final String DIR_JSR88_PLUGINS = "/J2EE/DeploymentPlugins"; //NOI18N
    public static final String URL_ATTR = InstanceProperties.URL_ATTR;
    public static final String TARGETNAME_ATTR = "targetName"; //NOI18N
    public static final String SERVER_NAME = "serverName"; //NOI18N
    
    private static ServerRegistry instance = null;
    public static synchronized ServerRegistry getInstance() {
        if(instance == null) instance = new ServerRegistry();
        return instance;

        //PENDING need to get this from lookup
        //    return (ServerRegistry) Lookup.getDefault().lookup(ServerRegistry.class);
    }

    /** Utility method that returns true if the ServerRegistry was initialized
     * during the current IDE session and false otherwise.
     */
    public static synchronized boolean wasInitialized () {
        return instance != null && instance.servers != null && instance.instances != null;
    }
    private transient Map<String, Server> servers = null;
    private transient Map<String, ServerInstance> instances = null;
    private final transient Collection<PluginListener> pluginListeners = new CopyOnWriteArrayList<PluginListener>();
    private final transient Collection<InstanceListener> instanceListeners = new CopyOnWriteArrayList<InstanceListener>();
    private transient PluginInstallListener pluginL;
    private transient InstanceInstallListener instanceL;

    private ServerRegistry() {
        super();
    }

    private synchronized void init() {
        LOGGER.log(Level.FINEST, "Entering registry initialization"); // NOI18N

        if (servers != null && instances != null) {
            return;
        }

        servers = new HashMap<String, Server>();
        instances = new HashMap<String, ServerInstance>();

        FileObject dir = FileUtil.getConfigFile(DIR_JSR88_PLUGINS);
        if (dir != null) {
            LOGGER.log(Level.FINE, "Loading server plugins"); // NOI18N
            dir.addFileChangeListener(pluginL = new PluginInstallListener(dir));
            FileObject[] ch = dir.getChildren();
            for (int i = 0; i < ch.length; i++) {
                addPlugin(ch[i]);
            }

            LOGGER.log(Level.FINE, "Loading server instances"); // NOI18N
            dir = FileUtil.getConfigFile(DIR_INSTALLED_SERVERS);
            if (dir == null) {
                try {
                    FileObject root = FileUtil.getConfigRoot();
                    dir = FileUtil.createFolder(root, DIR_INSTALLED_SERVERS);
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Could not create DIR_INSTALLED_SERVERS folder");
                }
            }
            if (dir != null) {
                dir.addFileChangeListener(instanceL = new InstanceInstallListener(dir));
                ch = dir.getChildren();
                for (int i = 0; i < ch.length; i++) {
                    addInstance(ch[i]);
                }
            }

            LOGGER.log(Level.FINE, "Finish initializing plugins"); // NOI18N
            List<String> notInitialized = new LinkedList<String>();
            for (Map.Entry<String, Server> entry : serversMap().entrySet()) {
                OptionalDeploymentManagerFactory odmf = entry.getValue().getOptionalFactory();
                if (null != odmf) {
                    try {
                        odmf.finishServerInitialization();
                    } catch (ServerInitializationException sie) {
                        LOGGER.log(Level.INFO, "Server plugin not initialized", sie);
                        notInitialized.add(entry.getKey());                        
                    } catch (RuntimeException ex) {
                        LOGGER.log(Level.WARNING, "Plugin implementation BUG -- Unexpected Exception from finishServerInitialization", ex);
                        notInitialized.add(entry.getKey());
                    }
                }
            }
            serversMap().keySet().removeAll(notInitialized);
        } else {
            LOGGER.log(Level.WARNING, "No DIR_JSR88_PLUGINS folder found, no server plugins will be availabe"); // NOI18N
        }
    }

    private Map<String, Server> serversMap() {
        init();
        return servers;
    }
    private synchronized Map<String, ServerInstance> instancesMap() {
        init();
        return instances;
    }

    private void addPlugin(FileObject fo) {
        String name = ""; //NOI18N
        try {
            if (fo.isFolder()) {
                if (fo.getFileObject("Descriptor") == null) { // NOI18N
                    LOGGER.log(Level.WARNING, "No server descriptor found in {0}", fo.getPath());
                    return;
                }
                name = fo.getName();
                Server server = null;
                synchronized (this) {
                    if (serversMap().containsKey(name)) {
                        return;
                    }
                    server = new Server(fo);
                    serversMap().put(name, server);
                    configNamesByType = null;
                }
                if (server != null) {
                    firePluginListeners(server, true);
                    fetchInstances(server);
                }
            }
        } catch (Exception e) {
            //LOGGER.log(Level.WARNING, "Plugin installation failed {0}", fo.toString()); //NOI18N
            LOGGER.log(Level.INFO, null, e);
        }
    }

    private void fetchInstances(Server server) {
        FileObject dir = FileUtil.getConfigFile(DIR_INSTALLED_SERVERS);
        FileObject[] ch = dir.getChildren();
        for (int i = 0; i < ch.length; i++) {
            String url = (String) ch[i].getAttribute(URL_ATTR);
            if (url != null && server.handlesUri(url)) {
                addInstance(ch[i]);
            }
        }
    }

    private void removePlugin(FileObject fo) {
        Server server = null;
        synchronized (this) {
            String name = fo.getName();

            server = (Server) serversMap().get(name);
            if (server != null) {
                // remove all registered server instances of the given server type
                ServerInstance[] tmp = getServerInstances();
                for (int i = 0; i < tmp.length; i++) {
                    ServerInstance si = tmp[i];
                    if (server.equals(si.getServer())) {
                        removeServerInstance(si.getUrl());
                    }
                }
            }
            serversMap().remove(name);
            configNamesByType = null;
        }
        if (server != null) {
            firePluginListeners(server, false);
        }
    }

    class PluginInstallListener extends FileChangeAdapter {
        private final FileObject dir;

        private PluginInstallListener(FileObject dir) {
            this.dir = dir;
        }
        @Override
        public void fileFolderCreated(FileEvent fe) {
            addPlugin(fe.getFile());
        }
        @Override
        public void fileDeleted(FileEvent fe) {
            removePlugin(fe.getFile());
        }
    }

    class InstanceInstallListener extends FileChangeAdapter {
        private final FileObject dir;

        private InstanceInstallListener(FileObject dir) {
            this.dir = dir;
        }
        @Override
        public void fileDataCreated(FileEvent fe) {
            super.fileDataCreated(fe);
            addInstance(fe.getFile());
        }
        // PENDING should support removing of instances?
    }

    public Collection<Server> getServers() {
        return serversMap().values();
    }

    public synchronized Collection<ServerInstance> getInstances() {
        return new ArrayList(instancesMap().values());
    }

    public synchronized String[] getInstanceURLs() {
        return instancesMap().keySet().toArray(new String[instancesMap().size()]);
    }

    public void checkInstanceAlreadyExists(String url) throws InstanceCreationException {
        if (getServerInstance(url) != null) {
            String msg = NbBundle.getMessage(ServerRegistry.class, "MSG_InstanceAlreadyExists", url);
            throw new InstanceCreationException(msg);
        }
    }

    public void checkInstanceExists(String url) {
        if (getServerInstance(url) == null) {
            String msg = NbBundle.getMessage(ServerRegistry.class, "MSG_InstanceNotExists", url);
            throw new IllegalStateException(msg);
        }
    }

    public Server getServer(String name) {
        return (Server) serversMap().get(name);
    }

    public void addPluginListener(PluginListener pl) {
        pluginListeners.add(pl);
    }

    public synchronized ServerInstance getServerInstance(String url) {
        return instancesMap().get(url);
    }

    public void removeServerInstance(String url) {
        if (url == null)
            return;

        ServerInstance tmp = null;
        synchronized (this) {
            tmp = instancesMap().remove(url);
        }
        if (tmp != null) {
            fireInstanceListeners(url, false);
            clearInstanceStorage(url);
        }
    }

    public synchronized ServerInstance[] getServerInstances() {
        ServerInstance[] ret = new ServerInstance[instancesMap().size()];
        return instancesMap().values().toArray(ret);
    }

    public static FileObject getInstanceFileObject(String url) {
        FileObject installedServersDir = FileUtil.getConfigFile(DIR_INSTALLED_SERVERS);
        if (installedServersDir == null) {
            return null;
        }
        FileObject[] installedServers = installedServersDir.getChildren();
        for (int i=0; i<installedServers.length; i++) {
            String val = (String) installedServers[i].getAttribute(URL_ATTR);
            if (val != null && val.equals(url))
                return installedServers[i];
        }
        return null;
    }

    /**
     * Add a new server instance in the server registry.
     *
     * @param  url URL to access deployment manager.
     * @param  username username used by the deployment manager.
     * @param  password password used by the deployment manager.
     * @param  displayName display name wich represents server instance in IDE.
     * @param initialProperties any other properties to set during the instance creation.
     *             If the map contains any of InstanceProperties.URL_ATTR,
     *             InstanceProperties.USERNAME_ATTR, InstanceProperties.PASSWORD_ATTR
     *             or InstanceProperties.DISPLAY_NAME_ATTR they will be ignored
     *             - the explicit parameter values are always used.
     *             <code>null</code> is accepted.
     *
     * @throws InstanceCreationException when instance with same url is already
     *         registered.
     */
    public void addInstance(String url, String username, String password,
            String displayName, boolean withoutUI, boolean nonPersistent,
            Map<String, String> initialproperties) throws InstanceCreationException {

        // should never have empty url; UI should have prevented this
        // may happen when autoregistered instance is removed
        if (url == null || url.equals("")) { //NOI18N
            LOGGER.log(Level.INFO, NbBundle.getMessage(ServerRegistry.class, "MSG_EmptyUrl"));
            return;
        }

        checkInstanceAlreadyExists(url);
        try {
            addInstanceImpl(url, username, password, displayName, withoutUI,
                    initialproperties, true, nonPersistent);
        } catch (InstanceCreationException ice) {
            InstanceCreationException e = new InstanceCreationException(NbBundle.getMessage(ServerRegistry.class, "MSG_FailedToCreateInstance", displayName));
            e.initCause(ice);
            throw e;
        }
    }

    private synchronized void writeInstanceToFile(String url, String username,
            String password, String serverName) throws IOException {

        if (url == null) {
            Logger.getLogger("global").log(Level.SEVERE, NbBundle.getMessage(ServerRegistry.class, "MSG_NullUrl"));
            return;
        }

        FileObject dir = FileUtil.getConfigFile(DIR_INSTALLED_SERVERS);
        FileObject instanceFOs[] = dir.getChildren();
        FileObject instanceFO = null;
        for (int i=0; i<instanceFOs.length; i++) {
            if (url.equals(instanceFOs[i].getAttribute(URL_ATTR)))
                instanceFO = instanceFOs[i];
        }
        
        if (instanceFO == null) {
            String name = FileUtil.findFreeFileName(dir,"instance",null);
            instanceFO = dir.createData(name);
        }
        instanceFO.setAttribute(URL_ATTR, url);
        instanceFO.setAttribute(InstanceProperties.USERNAME_ATTR, username);
        savePassword(instanceFO, password,
                NbBundle.getMessage(ServerRegistry.class, "MSG_KeyringDisplayName", serverName));
    }

    private synchronized void clearInstanceStorage(final String url) {
        FileObject instanceFO = getInstanceFileObject(url);
        if (instanceFO != null) {
            try {
                instanceFO.delete();
            } catch (IOException ioe) {
                LOGGER.log(Level.INFO, null, ioe);
            }
        }

        Keyring.delete(getPasswordKey(url));
    }

    /**
     * Add a new server instance in the server registry.
     *
     * @param url URL to access deployment manager.
     * @param username username used by the deployment manager.
     * @param password password used by the deployment manager.
     * @param displayName display name wich represents server instance in IDE.
     * @param initialProperties any other properties to set during the instance creation.
     *             If the map contains any of InstanceProperties.URL_ATTR,
     *             InstanceProperties.USERNAME_ATTR, InstanceProperties.PASSWORD_ATTR
     *             or InstanceProperties.DISPLAY_NAME_ATTR they will be ignored
     *             - the explicit parameter values are always used.
     *             <code>null</code> is accepted.
     *
     * @return <code>true</code> if the server instance was created successfully,
     *             <code>false</code> otherwise.
     */
    private void addInstanceImpl(String url, String username,
            String password, String displayName, boolean withoutUI,
            Map<String, String> initialProperties, boolean loadPlugins, boolean nonPersistent) throws InstanceCreationException {

        if (url == null) {
            // may happen when autoregistered instance is removed
            LOGGER.log(Level.FINE, "Tried to add instance with null url");
        }

        synchronized (this) {
            if (instancesMap().containsKey(url)) {
                throw new AlreadyRegisteredException("already exists");
            }

            LOGGER.log(Level.FINE, "Registering instance {0}", url);

            Map<String, String> properties = cleanInitialProperties(initialProperties);

            Collection serversMap = serversMap().values();
            for (Iterator i = serversMap.iterator(); i.hasNext();) {
                Server server = (Server) i.next();
                try {
                    if (server.handlesUri(url)) {
                        ServerInstance tmp = new ServerInstance(server, url, nonPersistent);
                        // PENDING persist url/password in ServerString as well
                        instancesMap().put(url, tmp);

                        if (!nonPersistent) {
                            writeInstanceToFile(url, username, password, server.getDisplayName());
                        } else {
                            tmp.getInstanceProperties().setProperty(
                                    InstanceProperties.URL_ATTR, url);
                            tmp.getInstanceProperties().setProperty(
                                    InstanceProperties.USERNAME_ATTR, username);
                            tmp.getInstanceProperties().setProperty(
                                    InstanceProperties.PASSWORD_ATTR, password);
                        }

                        tmp.getInstanceProperties().setProperty(
                                InstanceProperties.REGISTERED_WITHOUT_UI, Boolean.toString(withoutUI));
                        if (displayName != null) {
                            tmp.getInstanceProperties().setProperty(
                                    InstanceProperties.DISPLAY_NAME_ATTR, displayName);
                        }

                        for (Map.Entry<String, String> entry : properties.entrySet()) {
                            tmp.getInstanceProperties().setProperty(entry.getKey(), entry.getValue());
                        }

                        // try to create a disconnected deployment manager to see
                        // whether the instance is not corrupted - see #46929
                        DeploymentManager manager = server.getDisconnectedDeploymentManager(url);
                        // FIXME this shouldn't be called in synchronized block
                        if (manager != null) {
                            fireInstanceListeners(url, true);
                            return; //  true;
                        } else {
                            clearInstanceStorage(url);
                            instancesMap().remove(url);
                        }
                    }
                } catch (Exception e) {
                    if (instancesMap().containsKey(url)) {
                        clearInstanceStorage(url);
                        instancesMap().remove(url);
                    }
                    LOGGER.log(Level.INFO, null, e);
                }
            }
        }

        if (loadPlugins) {
            // don't wait for FS event, try to load the plugin now
            FileObject dir = FileUtil.getConfigFile(DIR_JSR88_PLUGINS);
            if (dir != null) {
                for (FileObject fo : dir.getChildren()) {
                    // if it is already registered this is noop
                    addPlugin(fo);
                }
            }

            addInstanceImpl(url, username, password, displayName, withoutUI, initialProperties, false, nonPersistent);
            return;
        }

        throw new InstanceCreationException("No handlers for " + url);
    }

    private Map<String, String> cleanInitialProperties(Map<String, String> initialProperties) {
        if (initialProperties == null) {
            return Collections.<String, String>emptyMap();
        }

        Map<String,String> properties = new HashMap<>(initialProperties);
        properties.remove(InstanceProperties.URL_ATTR);
        properties.remove(InstanceProperties.USERNAME_ATTR);
        properties.remove(InstanceProperties.PASSWORD_ATTR);
        properties.remove(InstanceProperties.DISPLAY_NAME_ATTR);
        properties.remove(InstanceProperties.REGISTERED_WITHOUT_UI);
        return properties;
    }

    public void addInstance(FileObject fo) {
        String url = (String) fo.getAttribute(URL_ATTR);
        String username = (String) fo.getAttribute(InstanceProperties.USERNAME_ATTR);
        // this is ok and avoids deadlock - we are adding FO
        // either it is new FO with password - so we can read it
        // or it is already existing and password is stored in keyring
        String password = (String) fo.getAttribute(InstanceProperties.PASSWORD_ATTR);
        String displayName = (String) fo.getAttribute(InstanceProperties.DISPLAY_NAME_ATTR);
        String withoutUI = (String) fo.getAttribute(InstanceProperties.REGISTERED_WITHOUT_UI);
        boolean withoutUIFlag = withoutUI == null ? false : Boolean.valueOf(withoutUI);
        try {
            addInstanceImpl(url, username, password, displayName, withoutUIFlag, null, false, false);
        } catch (AlreadyRegisteredException ex) {
            LOGGER.log(Level.FINE, "Instance already registered {0}", url);
        } catch (InstanceCreationException ice) {
            LOGGER.log(Level.INFO, "Could not create instance {0}", url);
            LOGGER.log(Level.FINE, null, ice);
        }
    }

    public void addInstanceListener(InstanceListener il) {
        instanceListeners.add(il);
    }

    public void removeInstanceListener(InstanceListener il) {
        instanceListeners.remove(il);
    }

    public void removePluginListener(PluginListener pl) {
        pluginListeners.remove(pl);
    }

    private void firePluginListeners(Server server, boolean add) {
        LOGGER.log(Level.FINE, "Firing plugin listener"); // NOI18N
        for (PluginListener pl : pluginListeners) {
            if (add) {
                pl.serverAdded(server);
            } else {
                pl.serverRemoved(server);
            }
        }
    }

    private void fireInstanceListeners(String instance, boolean add) {
        for (InstanceListener l : instanceListeners) {
            if(add) {
                l.instanceAdded(instance);
            } else {
                l.instanceRemoved(instance);
            }
        }
    }

    public interface PluginListener extends EventListener {

        public void serverAdded(Server name);

        public void serverRemoved(Server name);

    }

    /* GuardedBy("this") */
    private transient Map<J2eeModule.Type, Set<String>> configNamesByType = null;
    private static final J2eeModule.Type[] ALL_TYPES = new J2eeModule.Type[] {
        J2eeModule.Type.EAR, J2eeModule.Type.RAR, J2eeModule.Type.CAR, J2eeModule.Type.EJB, J2eeModule.Type.WAR };

    private void initConfigNamesByType() {
        synchronized (this) {
            if (configNamesByType != null) {
                return;
            }
            configNamesByType = new HashMap<>();
            for (int i = 0 ; i < ALL_TYPES.length; i++) {
                Set<String> configNames = new HashSet<>();
                for (Iterator j=servers.values().iterator(); j.hasNext();) {
                    Server s = (Server) j.next();
                    String[] paths = s.getDeploymentPlanFiles(ALL_TYPES[i]);
                    if (paths == null)
                        continue;
                    for (int k=0 ; k<paths.length; k++) {
                        File path = new File(paths[k]);
                        configNames.add(path.getName());
                    }
                }
                configNamesByType.put(ALL_TYPES[i], configNames);
            }
        }
    }

    public boolean isConfigFileName(String name, J2eeModule.Type type) {
	initConfigNamesByType();
        synchronized (this) {
            Set<String> configNames = configNamesByType.get(type);
            return (configNames != null && configNames.contains(name));
        }
    }

    /** Return profiler if any is registered in the IDE, null otherwise. */
    public static Profiler getProfiler() {
        return (Profiler)Lookup.getDefault().lookup(Profiler.class);
    }

    @CheckForNull
    static String readPassword(@NonNull final String url) {
        char[] passwordChars = Keyring.read(getPasswordKey(url));
        if (passwordChars != null) {
            String password = String.valueOf(passwordChars);
            Arrays.fill(passwordChars, ' ');
            return password;
        }
        return null;
    }

    static void savePassword(@NonNull final String url, @NullAllowed final String password,
            @NullAllowed final String displayName) {

        if (password == null) {
            return;
        }
        Keyring.save(getPasswordKey(url), password.toCharArray(), displayName);
    }
    
    static void savePassword(@NonNull final FileObject fo, @NullAllowed final String password,
            @NullAllowed final String displayName) {
        
        if (password == null) {
            return;
        }
        String url = (String) fo.getAttribute(InstanceProperties.URL_ATTR);
        if (url == null) {
            return;
        }
        Keyring.save(getPasswordKey(url), password.toCharArray(), displayName);
        try {
            fo.setAttribute(InstanceProperties.PASSWORD_ATTR, null);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }    

    private static String getPasswordKey(String url) {
        StringBuilder builder = new StringBuilder("j2eeserver:");
        builder.append(url);
        return builder.toString();
    }
}
