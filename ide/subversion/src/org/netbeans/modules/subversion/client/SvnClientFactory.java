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
package org.netbeans.modules.subversion.client;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.libs.svnclientadapter.SvnClientAdapterFactory;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.client.cli.CommandlineClient;
import org.netbeans.modules.subversion.config.SvnConfigFiles;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Utilities;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNPromptUserPassword;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * A SvnClient factory
 *
 * @author Tomas Stupka
 */
public class SvnClientFactory {
    
    /** the only existing SvnClientFactory instance */
    private static SvnClientFactory instance;
    /** the only existing ClientAdapterFactory instance */
    private static ClientAdapterFactory factory;
    /** if an exception occured */
    private static SVNClientException exception = null;

    /** indicates that something went terribly wrong with javahl init during the previous nb session */
    private static boolean javahlCrash = false;
    private static final int JAVAHL_INIT_NOCRASH = 1;
    private static final int JAVAHL_INIT_STOP_REPORTING = 2;

    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.subversion.client.SvnClientFactory");
    public static final String FACTORY_TYPE_COMMANDLINE = "commandline"; //NOI18N
    public static final String FACTORY_TYPE_JAVAHL = "javahl"; //NOI18N
    public static final String FACTORY_TYPE_SVNKIT = "svnkit"; //NOI18N
    public static final String DEFAULT_FACTORY = FACTORY_TYPE_JAVAHL; // javahl is default
    private static boolean cli16Version;

    public enum ConnectionType {
        javahl,
        cli,
        svnkit
    }

    /** Creates a new instance of SvnClientFactory */
    private SvnClientFactory() {
    }

    /**
     * Returns the only existing SvnClientFactory instance
     *
     * @return the SvnClientFactory instance
     */
    public static synchronized SvnClientFactory getInstance() {
        init();
        return instance;
    }

    /**
     * Initializes the SvnClientFactory instance
     */
    public static synchronized void init() {
        if(instance == null) {
            SvnClientFactory fac = new SvnClientFactory();
            fac.setup();
            instance = fac;
        }
    }

    /**
     * Resets the SvnClientFactory instance.
     * Call this method if user's preferences regarding used client change.
     */
    public static synchronized void resetClient() {
        instance = null;
        SvnConfigFiles.getInstance().reset();
    }
    
    public static boolean isCLI() {
        if(!isClientAvailable()) return false;
        assert factory != null;
        return factory.connectionType() == ConnectionType.cli;
    }

    public static boolean isCLIOldFormat () {
        return cli16Version;
    }

    public static boolean isJavaHl() {
        if(!isClientAvailable()) return false;
        assert factory != null;
        return factory.connectionType() == ConnectionType.javahl;
    }

    public static boolean isSvnKit() {
        if(!isClientAvailable()) return false;
        assert factory != null;
        return factory.connectionType() == ConnectionType.svnkit;
    }

    public static boolean hasSvnKit() {
        return SvnClientAdapterFactory.getInstance(SvnClientAdapterFactory.Client.SVNKIT) != null;
    }

    /**
     * Returns a SvnClient, which isn't configured in any way.
     * Knows no username, password, has no SvnProgressSupport<br/>
     * Such an instance isn't supposed to work properly when calling remote svn commands.
     *
     * @return the SvnClient
     */
    public SvnClient createSvnClient() throws SVNClientException {
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
    public SvnClient createSvnClient(SVNUrl repositoryUrl, SvnProgressSupport support, String username, char[] password, int handledExceptions) throws SVNClientException {
        if(exception != null) {
            throw exception;
        }
        try {
            return factory.createSvnClient(repositoryUrl, support, username, password, handledExceptions);
        } catch (Error err) {
            throw new SVNClientException(err);
        }
    }
    
    /**
     * Switches to commandline client. Call this as a fallback when no
     * integrated svn clients work with some working copies
     */
    static void switchToCLI () {
        LOG.log(Level.INFO, "Switching forcefully to a commandline client"); //NOI18N
        SvnModuleConfig.getDefault().setPreferredFactoryType(FACTORY_TYPE_COMMANDLINE);
        SvnModuleConfig.getDefault().setForceCommnandlineClient(true);
        resetClient();
    }

    /**
     * A SVNClientAdapterFactory will be setup, according to the svnClientAdapterFactory property.<br>
     * The CommandlineClientAdapterFactory is default as long no value is set for svnClientAdapterFactory.
     *
     */
    private void setup() {
        try {
            exception = null;
            // ping config file copying
            SvnConfigFiles.getInstance();

            String factoryType = getDefaultFactoryType(false);
            
            if (factoryType.trim().equals(FACTORY_TYPE_JAVAHL)) {
                if(setupJavaHl()) {
                    return;
                }
                LOG.log(Level.INFO, "JavaHL not available. Falling back on SvnKit.");
                if(setupSvnKit()) {
                    return;
                }          
                LOG.log(Level.INFO, "SvnKit not available. Falling back on commandline.");
                setupCommandline();
            } else if(factoryType.trim().equals(FACTORY_TYPE_SVNKIT)) {
                if(setupSvnKit()) {
                    return;
                }
                LOG.log(Level.INFO, "SvnKit not available. Falling back on javahl.");
                if(setupJavaHl()) {
                    return;
                }
                LOG.log(Level.INFO, "JavaHL not available. Falling back on comandline.");
                setupCommandline();
            } else if(factoryType.trim().equals(FACTORY_TYPE_COMMANDLINE)) {
                setupCommandline();
            } else {              
                throw new SVNClientException("Unknown factory: " + factoryType);
            }
        } catch (SVNClientException e) {
            exception = e;
        }
    }

    /**
     * Throws an exception if no SvnClientAdapter is available.
     */
    public static void checkClientAvailable() throws SVNClientException {
        init();
        if(exception != null) throw exception;
    }

    public static boolean isClientAvailable() {
        init();
        return exception == null;
    }

    /**
     * Immediately returns true if the factory has been initialized, otherwise returns false.
     * @return
     */
    public static boolean isInitialized () {
        return instance != null;
    }

    public static boolean wasJavahlCrash() {
        init();
        if(javahlCrash) {
            javahlCrash = false;
            return true;
        }
        return false;
    }

    private boolean setupJavaHl () {

        String jhlInitFile = Places.getUserDirectory().getAbsolutePath() + "/config/svn/jhlinit";
        File initFile = new File(jhlInitFile);

        if(checkJavahlCrash(initFile) && !SvnClientFactory.FACTORY_TYPE_JAVAHL.equals(SvnModuleConfig.getDefault().getForcedSvnFactory())) {
            return false;
        }
        try {
            initFile.delete();
            initFile.createNewFile();
        } catch (IOException ex) {
            // should not happen
            LOG.log(Level.INFO, null, ex);
        }

        final SvnClientAdapterFactory f;
        try {            
            f = SvnClientAdapterFactory.getInstance(SvnClientAdapterFactory.Client.JAVAHL);
            if(f == null) {
               return false;
            }
        } finally {
            // write the flag even if javahl not available -
            // we just want to now on the next run that javahl didn't crash the jvm,
            // so we will try to init javahl again
            writeJavahlInitFlag(initFile, JAVAHL_INIT_NOCRASH);
        }
        factory = new ClientAdapterFactory() {
            @Override
            protected ISVNClientAdapter createAdapter() {
                return f.createClient();
            }
            @Override
            protected SvnClientInvocationHandler getInvocationHandler(ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions) {
                return new SvnClientInvocationHandler(adapter, desc, support, handledExceptions, ConnectionType.javahl);
            }
            @Override
            protected ISVNPromptUserPassword createCallback(SVNUrl repositoryUrl, int handledExceptions) {
                return new JhlClientCallback(repositoryUrl, handledExceptions);
            }
            @Override
            protected ConnectionType connectionType() {
                return ConnectionType.javahl;
            }
        };
        LOG.info("running on javahl");
        return true;
    }
        
    private boolean checkJavahlCrash(File initFile) {
        if(!initFile.exists()) {
            LOG.fine("trying to init javahl first time.");
            return false;
        }
        FileReader r = null;
        try {
            r = new FileReader(initFile);
            int i = r.read();
            try { r.close(); r = null; } catch(IOException e) {}
            switch(i) {
                case -1: // empty means we crashed
                    writeJavahlInitFlag(initFile, JAVAHL_INIT_STOP_REPORTING);
                    javahlCrash = true;
                    LOG.log(Level.WARNING, "It appears that subversion java bindings initialization caused trouble in a previous Netbeans session. Please report.");
                    return true;
                case JAVAHL_INIT_STOP_REPORTING:
                    LOG.fine("won't init javahl due to problem in a previous try.");
                    return true;
                case JAVAHL_INIT_NOCRASH:
                    LOG.fine("will try init javahl.");
                    return false;
            }
        } catch (IOException ex) {
            LOG.log(Level.INFO, null, ex);
        } finally {
            try { if(r != null) r.close(); } catch (IOException ex) { }
        }
        return false;  // optimistic attitude
    }

    private void writeJavahlInitFlag(File initFile, int flag) {
        FileWriter w = null;
        try {
            w = new FileWriter(initFile);
            w.write(flag);
            w.flush();
        } catch (IOException ex) {
            LOG.log(Level.INFO, null, ex);
        } finally {
            try { if(w != null) w.close(); } catch (IOException ex) { }
        }
    }

    private boolean  setupSvnKit () {
        final SvnClientAdapterFactory f = SvnClientAdapterFactory.getInstance(SvnClientAdapterFactory.Client.SVNKIT);
        if(f == null) {            
            return false;
        }
        factory = new ClientAdapterFactory() {
            @Override
            protected ISVNClientAdapter createAdapter() {
                return f.createClient();
            }
            @Override
            protected SvnClientInvocationHandler getInvocationHandler(ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions) {
                return new SvnClientInvocationHandler(adapter, desc, support, handledExceptions, ConnectionType.svnkit);
            }
            @Override
            protected ISVNPromptUserPassword createCallback(SVNUrl repositoryUrl, int handledExceptions) {
                return new SvnKitClientCallback(repositoryUrl, handledExceptions);
            }
            @Override
            protected ConnectionType connectionType() {
                return ConnectionType.svnkit;
            }
            
            @Override
            /**
             * Slightly different from the default one
             */
            protected void setupAdapter(ISVNClientAdapter adapter, String username, char[] password, ISVNPromptUserPassword callback) {
                if (callback != null) {
                    adapter.addPasswordCallback(callback);
                }
                try {
                    File configDir = FileUtil.normalizeFile(new File(SvnConfigFiles.getNBConfigPath()));
                    adapter.setConfigDirectory(configDir);
                } catch (SVNClientException ex) {
                    SvnClientExceptionHandler.notifyException(ex, false, false);
                }
                adapter.setUsername(username);
                adapter.setPassword(password == null ? "" : new String(password)); //NOI18N
            }
        };
        LOG.fine("Setting svnkit prop: svnkit.http.methods=Basic");
        System.setProperty("svnkit.http.methods", "Basic"); //NOI18N
        LOG.info("svnClientAdapter running on svnkit");
        return true;
    }

    public void setupCommandline () {
        if(!checkCLIExecutable()) return;
        
        factory = new ClientAdapterFactory() {
            @Override
            protected ISVNClientAdapter createAdapter() {
                return new CommandlineClient(); //SVNClientAdapterFactory.createSVNClient(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT);
            }
            @Override
            protected SvnClientInvocationHandler getInvocationHandler(ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions) {
                return new SvnClientInvocationHandler(adapter, desc, support, handledExceptions, ConnectionType.cli);
            }
            @Override
            protected ISVNPromptUserPassword createCallback(SVNUrl repositoryUrl, int handledExceptions) {
                return null;
            }
            @Override
            protected ConnectionType connectionType() {
                return ConnectionType.cli;
            }
        };
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
        String execPath = SvnModuleConfig.getDefault().getExecutableBinaryPath();
        if(execPath != null && !execPath.trim().equals("")) {
            exception = ex;
            LOG.log(Level.WARNING, "executable binary path set to {0} yet client not available.", new Object[] { execPath });
            return false; 
        }
        if(Utilities.isUnix()) {
            LOG.fine("svn client isn't set on path yet. Will check known locations...");
            String[] locations = new String[] {"/usr/local/bin/", "/usr/bin/"};
            String name = "svn";
            for (String loc : locations) {
                File file = new File(loc, name);
                LOG.log(Level.FINE, "checking existence of {0}", new Object[] { file.getAbsolutePath() });
                if (file.exists()) {                
                    SvnModuleConfig.getDefault().setExecutableBinaryPath(loc);
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
        }
        exception = ex;
        return false;
    }

    private void checkVersion() throws SVNClientException {
        CommandlineClient cc = new CommandlineClient();
        try {
            setConfigDir(cc);
            cli16Version = cc.checkSupportedVersion();
        } catch (SVNClientException e) {
            LOG.log(Level.FINE, "checking version", e);
            throw e;
        }
    }

    private void setConfigDir (ISVNClientAdapter client) {
        if (client != null) {
            File configDir = FileUtil.normalizeFile(new File(SvnConfigFiles.getNBConfigPath()));
            try {
                client.setConfigDirectory(configDir);
            } catch (SVNClientException ex) {
                // not interested, just log
                LOG.log(Level.INFO, null, ex);
            }
        }
    }
    
    private String getDefaultFactoryType (boolean forcedCommandlineFallbackEnabled) {
        SvnModuleConfig config = SvnModuleConfig.getDefault();
        String factoryType = config.getGlobalSvnFactory();
        if (forcedCommandlineFallbackEnabled 
                && (factoryType == null || factoryType.trim().isEmpty())
                && config.isForcedCommandlineClient()) {
            // fallback to commandline only if factoryType is not set explicitely
            factoryType = FACTORY_TYPE_COMMANDLINE;
            LOG.log(Level.INFO, "setup: using commandline as the client - saved in preferences");
        } else {
            config.setForceCommnandlineClient(false);
        }

        if (factoryType == null || factoryType.trim().isEmpty()) {
            factoryType = config.getPreferredFactoryType(DEFAULT_FACTORY);
        }
        return factoryType;
    }

    private abstract class ClientAdapterFactory {

        protected abstract ISVNClientAdapter createAdapter();
        protected abstract SvnClientInvocationHandler getInvocationHandler(ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions);
        protected abstract ISVNPromptUserPassword createCallback(SVNUrl repositoryUrl, int handledExceptions);
        protected abstract ConnectionType connectionType();

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
        public SvnClient createSvnClient(SVNUrl repositoryUrl, SvnProgressSupport support, String username, char[] password, int handledExceptions) {
            ISVNClientAdapter adapter = createAdapter();
            SvnClientInvocationHandler handler = getInvocationHandler(adapter, createDescriptor(repositoryUrl), support, handledExceptions);
            setupAdapter(adapter, username, password, createCallback(repositoryUrl, handledExceptions));
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

        protected void setupAdapter(ISVNClientAdapter adapter, String username, char[] password, ISVNPromptUserPassword callback) {
            adapter.setUsername(username);
            if(callback != null) {
                adapter.addPasswordCallback(callback);
            } else {
                // do not set password for javahl, it seems that in that case the password is stored permanently in ~/.subversion/auth
                adapter.setPassword(password == null ? "" : new String(password)); //NOI18N
            }
            try {
                File configDir = FileUtil.normalizeFile(new File(SvnConfigFiles.getNBConfigPath()));
                adapter.setConfigDirectory(configDir);
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(ex, false, false);
            }
        }
    }
}
