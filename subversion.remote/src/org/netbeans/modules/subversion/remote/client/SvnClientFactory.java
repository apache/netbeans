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
package org.netbeans.modules.subversion.remote.client;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.SvnModuleConfig;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.cli.CommandlineClient;
import org.netbeans.modules.subversion.remote.config.SvnConfigFiles;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileSystem;

/**
 * A SvnClient factory
 *
 * 
 */
public class SvnClientFactory {
    
    /** the only existing SvnClientFactory instance */
    private static final Map<FileSystem, SvnClientFactory> instances = new HashMap<>();
    /** the only existing ClientAdapterFactory instance */
    private ClientAdapterFactory factory;
    /** if an exception occurred */
    private SVNClientException exception;
    private final FileSystem fileSystem;

    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.subversion.remote.client.SvnClientFactory"); //NOI18N
    public static final String FACTORY_TYPE_COMMANDLINE = "commandline"; //NOI18N
    public static final String DEFAULT_FACTORY = FACTORY_TYPE_COMMANDLINE; // javahl is default
    private boolean cli16Version;

    /** Creates a new instance of SvnClientFactory */
    private SvnClientFactory(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    /**
     * Returns the only existing SvnClientFactory instance
     *
     * @return the SvnClientFactory instance
     */
    public synchronized static SvnClientFactory getInstance(Context context) {
        return getFactory(context);
    }

    /**
     * Initializes the SvnClientFactory instance
     */
    private synchronized static SvnClientFactory getFactory(Context context) {
        FileSystem fs = context.getFileSystem();
        if (fs != null) {
            SvnClientFactory fac = instances.get(fs);
            if (fac == null) {
                fac = new SvnClientFactory(fs);
                fac.setup();
                instances.put(fs, fac);
            }
            return fac;
        }
        return null;
    }

    /**
     * Resets the SvnClientFactory instance.
     * Call this method if user's preferences regarding used client change.
     */
    public synchronized static void resetClient() {
        for(Map.Entry<FileSystem, SvnClientFactory> entry : instances.entrySet()) {
            SvnConfigFiles.getInstance(entry.getKey()).reset();
        }
        instances.clear();
    }
    
    public static boolean isCLI() {
        return true;
    }

    public boolean isCLIOldFormat () {
        return cli16Version;
    }

    /**
     * Returns a SvnClient, which isn't configured in any way.
     * Knows no username, password, has no SvnProgressSupport<br/>
     * Such an instance isn't supposed to work properly when calling remote svn commands.
     *
     * @return the SvnClient
     */
    public SvnClient createSvnClient(Context context) throws SVNClientException {
        if(exception != null) {
            throw exception;
        }
        return factory.createSvnClient();
    }

    /**
     *
     * Returns a SvnClient which is configured with the given <tt>username</tt>,
     * <tt>password</tt>, <tt>repositoryUrl</tt> and the <tt>support</tt>.<br>
     * In case a http proxy was given via <tt>pd</tt> an according entry for the <tt>repositoryUrl</tt>
     * will be created in the svn config file.
     * The mask <tt>handledExceptions</tt> specifies which exceptions are to be handled.
     *
     * @param repositoryUrl
     * @param support
     * @param username
     * @param password
     * @param handledExceptions
     *
     * @return the configured SvnClient
     *
     */
    public SvnClient createSvnClient(Context context, SVNUrl repositoryUrl, SvnProgressSupport support, String username, char[] password, int handledExceptions) throws SVNClientException {
        if(exception != null) {
            throw exception;
        }
        try {
            return factory.createSvnClient(context, repositoryUrl, support, username, password, handledExceptions);
        } catch (Error err) {
            throw new SVNClientException(err);
        }
    }
    
    /**
     * A SVNClientAdapterFactory will be setup, according to the svnClientAdapterFactory property.<br>
     * The CommandlineClientAdapterFactory is default as long no value is set for svnClientAdapterFactory.
     *
     */
    private void setup() {
        try {
            exception = null;
            if (!VCSFileProxySupport.isConnectedFileSystem(fileSystem)) {
                throw new SVNClientException("Remote host "+fileSystem+" is not connected."); //NOI18N
            }
            // ping config file copying
            SvnConfigFiles.getInstance(fileSystem);
            
            String factoryType = getDefaultFactoryType();
            
            if(factoryType.trim().equals(FACTORY_TYPE_COMMANDLINE)) {
                setupCommandline();
            } else {              
                throw new SVNClientException("Unknown factory: " + factoryType); //NOI18N
            }
        } catch (SVNClientException e) {
            exception = e;
        } catch (Throwable ex) {
            exception = new SVNClientException(ex);
        }
    }

    /**
     * Throws an exception if no SvnClientAdapter is available.
     */
    public static void checkClientAvailable(Context root) throws SVNClientException {
        SvnClientFactory res = getFactory(root);
        if (res != null && res.exception != null) {
            throw res.exception;
        }
    }

    public static boolean isClientAvailable(Context root) {
        SvnClientFactory res = getFactory(root);
        if (res == null || res.exception != null) {
            return false;
        }
        return true;
    }

    /**
     * Immediately returns true if the factory has been initialized, otherwise returns false.
     * @return
     */
    public static boolean isInitialized (Context context) {
        FileSystem fs = context.getFileSystem();
        if (fs != null) {
            return instances.get(fs) != null;
        }
        return false;
    }

    private void setupCommandline () {
        if(!checkCLIExecutable()) {
            return;
        }
        
        factory = new ClientAdapterFactory(fileSystem);
        LOG.info("running on commandline");
    }

    private boolean checkCLIExecutable() {
        exception = null;
        SVNClientException ex = null;
        try {
            checkVersion();
        } catch (SVNClientException e) {
            ex = e;
        }
        if(ex == null) {
            // works on first shot
            LOG.fine("svn client returns correct version");
            return true;
        }
        String execPath = SvnModuleConfig.getDefault(fileSystem).getExecutableBinaryPath();
        if(execPath != null && !execPath.trim().equals("")) {
            exception = ex;
            LOG.log(Level.WARNING, "executable binary path set to {0} yet client not available.", new Object[] { execPath });
            return false; 
        }
        LOG.fine("svn client isn't set on path yet. Will check known locations...");
        String[] locations = new String[] {"/usr/local/bin/", "/usr/bin/"}; //NOI18N
        String name = "svn"; //NOI18N
        for (String loc : locations) {
            File file = new File(loc, name);
            LOG.log(Level.FINE, "checking existence of {0}", new Object[] { file.getAbsolutePath() });
            if (file.exists()) {                
                SvnModuleConfig.getDefault(fileSystem).setExecutableBinaryPath(loc);
                try {
                    checkVersion();
                } catch (SVNClientException e) {
                    ex = e;
                    continue;
                }
                LOG.log(Level.INFO, "found svn executable binary. Setting executable binary path to {0}", new Object[] { loc });
                return true;
            }
        }
        exception = ex;
        return false;
    }

    private void checkVersion() throws SVNClientException {
        CommandlineClient cc = new CommandlineClient(fileSystem);
        try {
            setConfigDir(cc);
            cli16Version = cc.checkSupportedVersion();
        } catch (SVNClientException e) {
            LOG.log(Level.FINE, "checking version", e);
            throw e;
        }
    }

    private void setConfigDir (SvnClient client) {
        if (client != null) {
            try {
                VCSFileProxy nbConfigPath = SvnConfigFiles.getNBConfigPath(fileSystem);
                if (nbConfigPath != null) {
                    VCSFileProxy configDir = nbConfigPath.normalizeFile();
                    client.setConfigDirectory(configDir);
                }
            } catch (SVNClientException | IOException ex) {
                // not interested, just log
                LOG.log(Level.INFO, null, ex);
            }
        }
    }
    
    private String getDefaultFactoryType () {
        SvnModuleConfig config = SvnModuleConfig.getDefault(fileSystem);
        String factoryType = config.getGlobalSvnFactory();
        config.setForceCommnandlineClient(false);

        if (factoryType == null || factoryType.trim().isEmpty()) {
            factoryType = config.getPreferredFactoryType(DEFAULT_FACTORY);
        }
        return factoryType;
    }

    private final class ClientAdapterFactory {
        private final FileSystem fileSystem;
        
        private ClientAdapterFactory(FileSystem fs) {
            fileSystem = fs;
        }

        protected CommandlineClient createAdapter() {
            return new CommandlineClient(fileSystem); //SVNClientAdapterFactory.createSVNClient(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT);
        }

        protected SvnClientInvocationHandler getInvocationHandler(CommandlineClient adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions) {
            return new SvnClientInvocationHandler(adapter, desc, support, handledExceptions);
        }

        SvnClient createSvnClient() {
            SvnClientInvocationHandler handler = getInvocationHandler(createAdapter(), createDescriptor(null), null, -1);
            SvnClient client = createSvnClient(handler);
            setConfigDir(client);
            return client;
        }

        /**
         *
         * Returns a SvnClientInvocationHandler instance which is configured with the given <tt>adapter</tt>,
         * <tt>support</tt> and a SvnClientDescriptor for <tt>repository</tt>.
         *
         * @param adapter
         * @param support
         * @param repository
         *
         * @return the created SvnClientInvocationHandler instance
         *
         */
        public SvnClient createSvnClient(Context context, SVNUrl repositoryUrl, SvnProgressSupport support, String username, char[] password, int handledExceptions) {
            CommandlineClient adapter = createAdapter();
            SvnClientInvocationHandler handler = getInvocationHandler(adapter, createDescriptor(repositoryUrl), support, handledExceptions);
            setupAdapter(adapter, username, password);
            return createSvnClient(handler);
        }

        private SvnClientDescriptor createDescriptor(final SVNUrl repositoryUrl) {
            return new SvnClientDescriptor() {
                @Override
                public SVNUrl getSvnUrl() {
                    return repositoryUrl;
                }
            };
        }

        private SvnClient createSvnClient(SvnClientInvocationHandler handler) {
            Class<SvnClient> proxyClass = (Class<SvnClient>) Proxy.getProxyClass(SvnClient.class.getClassLoader(), new Class[] {SvnClient.class});
            Subversion.getInstance().cleanupFilesystem();
            try {
                Constructor<SvnClient> c = proxyClass.getConstructor(InvocationHandler.class);
                return c.newInstance(handler);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, null, e);
            }
            return null;
        }

        protected void setupAdapter(CommandlineClient adapter, String username, char[] password) {
            adapter.setUsername(username);
            // do not set password for javahl, it seems that in that case the password is stored permanently in ~/.subversion/auth
            adapter.setPassword(password == null ? "" : new String(password)); //NOI18N
            try {
                VCSFileProxy nbConfigPath = SvnConfigFiles.getNBConfigPath(fileSystem);
                if (nbConfigPath != null) {
                    VCSFileProxy configDir = nbConfigPath.normalizeFile();
                    adapter.setConfigDirectory(configDir);
                }
            } catch (SVNClientException | IOException ex) {
                SvnClientExceptionHandler.notifyException(new Context(VCSFileProxy.createFileProxy(fileSystem.getRoot())), ex, false, false);
            }
        }
    }
}
