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

package org.netbeans.modules.tomcat5.deploy;

import org.netbeans.modules.tomcat5.j2ee.TomcatPlatformImpl;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.tomcat5.config.gen.Server;
import org.openide.filesystems.*;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.tomcat5.optional.StartTomcat;
import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;
import org.netbeans.api.extexecution.ExternalProcessSupport;
import org.netbeans.modules.tomcat5.TomEEWarListener;
import org.netbeans.modules.tomcat5.TomcatFactory;
import org.netbeans.modules.tomcat5.progress.MultiProgressObjectWrapper;
import org.netbeans.modules.tomcat5.util.*;
import org.openide.util.NbBundle;


/**
 * DeploymentManager that can deploy to
 * Tomcat 5 using manager application.
 *
 * @author Petr Hejl, Radim Kubacki
 */
public class TomcatManager implements DeploymentManager {

    public enum TomcatVersion {
        TOMCAT_50(50), TOMCAT_55(55), TOMCAT_60(60), TOMCAT_70(70), 
        TOMCAT_80(80), TOMCAT_90(90), TOMCAT_100(100), TOMCAT_101(101),
        TOMCAT_110(110);
        
        TomcatVersion(int version) { this.version = version; }
        private final int version;
        public int version() { return version; }
        /**
         * 
         * @param tv TomcatVersion
         * @return true if the version is equal or greater, false otherwise
         */
        public boolean isAtLeast(TomcatVersion tv) {
            int comparisonResult = this.compareTo(tv);
            return (comparisonResult >= 0);
        }
    }

    public enum TomEEVersion {
        TOMEE_15(15), TOMEE_16(16), TOMEE_17(17), TOMEE_70(70), 
        TOMEE_71(71), TOMEE_80(80), TOMEE_90(90), TOMEE_100(100);
        
        TomEEVersion(int version) { this.version = version; }
        private final int version;
        public int version() { return version; }
        /**
         * 
         * @param tv TomEEVersion
         * @return true if the version is equal or greater, false otherwise
         */
        public boolean isAtLeast(TomEEVersion teev) {
            int comparisonResult = this.compareTo(teev);
            return (comparisonResult >= 0);
        }
    };

    public enum TomEEType {TOMEE_OPENEJB, TOMEE_WEBPROFILE, TOMEE_JAXRS, TOMEE_MICROPROFILE, TOMEE_PLUS, TOMEE_PLUME};

    public static final String KEY_UUID = "NB_EXEC_TOMCAT_START_PROCESS_UUID"; //NOI18N

    private static final Logger LOGGER = Logger.getLogger(TomcatManager.class.getName());

    /** Enum value for get*Modules methods. */
    static final int ENUM_AVAILABLE = 0;

    /** Enum value for get*Modules methods. */
    static final int ENUM_RUNNING = 1;

    /** Enum value for get*Modules methods. */
    static final int ENUM_NONRUNNING = 2;

    public static final String PROP_BUNDLED_TOMCAT = "is_it_bundled_tomcat"; // NOI18N

    /** Manager state. */
    private final boolean connected;

    /** uri of this DeploymentManager. */
    private final String uri;

    private StartTomcat startTomcat;

    /** System process of the started Tomcat */
    private Process process;

    /** Easier access to some server.xml settings. */
    private TomcatManagerConfig tomcatManagerConfig;

    /** LogManager manages all context and shared context logs for this TomcatManager. */
    private final LogManager logManager = new LogManager(this);

    private TomcatPlatformImpl tomcatPlatform;

    private final TomcatProperties tp;

    private final TomcatVersion tomcatVersion;

    /* GuardedBy(this) */
    private boolean tomEEChecked;

    /* GuardedBy(this) */
    private TomEEWarListener tomEEWarListener;

    /* GuardedBy(this) */
    private TomEEVersion tomEEVersion;

    /* GuardedBy(this) */
    private TomEEType tomEEType;

    private final InstanceProperties ip;

    private boolean needsRestart;

    private boolean misconfiguredProxy;

    /** Creates an instance of connected TomcatManager
     * @param conn <CODE>true</CODE> to create connected manager
     * @param uri URI for DeploymentManager
     * @param uname username
     * @param passwd password
     */
    public TomcatManager(boolean conn, String uri, TomcatVersion tomcatVersion)
            throws IllegalArgumentException {
        LOGGER.log(Level.FINE, "Creating connected TomcatManager uri={0}", uri); //NOI18N
        this.connected = conn;
        this.tomcatVersion = tomcatVersion;
        this.uri = uri;
        ip = InstanceProperties.getInstanceProperties(getUri());
        assert ip != null;

        this.tp = new TomcatProperties(this);
    }

    public InstanceProperties getInstanceProperties() {
        return ip;
    }

    public boolean isBundledTomcat() {
        if (ip == null) {
            return false;
        }
        String val = ip.getProperty(PROP_BUNDLED_TOMCAT);
        return val != null ? Boolean.valueOf(val)
                           : false;
    }

    public TomcatProperties getTomcatProperties() {
        return tp;
    }

    public synchronized void setNeedsRestart(boolean needsRestart) {
        this.needsRestart = needsRestart;
    }

    public synchronized boolean getNeedsRestart() {
        return needsRestart;
    }

    public synchronized boolean isMisconfiguredProxy() {
        return misconfiguredProxy;
    }

    public synchronized void setMisconfiguredProxy(boolean misconfiguredProxy) {
        this.misconfiguredProxy = misconfiguredProxy;
    }

    /**
     * Returns true if the server is running.
     *
     * @param checkResponse should be checked whether is the server responding - is really up?
     * @return <code>true</code> if the server is running.
     */
    public boolean isRunning(boolean checkResponse) {
        return isRunning(tp.getRunningCheckTimeout(), checkResponse);
    }

    /**
     * Returns true if the server is running.
     *
     * @param timeout for how long should we keep trying to detect the running state.
     * @param checkResponse should be checked whether is the server responding - is really up?
     * @return <code>true</code> if the server is running.
     */
    public boolean isRunning(int timeout, boolean checkResponse) {
        Process proc = getTomcatProcess();
        if (proc != null) {
            try {
                // process is stopped
                proc.exitValue();
                return false;
            } catch (IllegalThreadStateException e) {
                // process is running
                if (!checkResponse) {
                    return true;
                }
            }
        }
        if (checkResponse) {
            return Utils.pingTomcat(getServerPort(), timeout, getServerHeader(), getPlainUri()); // is tomcat responding?
        } else {
            return false; // cannot resolve the state
        }
    }

    /** Returns identifier of TomcatManager. This is not a real URI!
     * @return URI including home and base specification
     */
    public String getUri () {
        switch (tomcatVersion) {
            case TOMCAT_110:
                return TomcatFactory.TOMCAT_URI_PREFIX_110 + uri;
            case TOMCAT_101:
                return TomcatFactory.TOMCAT_URI_PREFIX_101 + uri;
            case TOMCAT_100:
                return TomcatFactory.TOMCAT_URI_PREFIX_100 + uri;
            case TOMCAT_90:
                return TomcatFactory.TOMCAT_URI_PREFIX_90 + uri;
            case TOMCAT_80:
                return TomcatFactory.TOMCAT_URI_PREFIX_80 + uri;
            case TOMCAT_70:
                return TomcatFactory.TOMCAT_URI_PREFIX_70 + uri;
            case TOMCAT_60:
                return TomcatFactory.TOMCAT_URI_PREFIX_60 + uri;
            case TOMCAT_55:
                return TomcatFactory.TOMCAT_URI_PREFIX_55 + uri;
            case TOMCAT_50:
            default:
                return TomcatFactory.TOMCAT_URI_PREFIX_50 + uri;
        }
    }

    /** Returns URI of TomcatManager (manager application).
     * @return URI without home and base specification
     */
    public String getPlainUri () {
        if (tomcatVersion.isAtLeast(TomcatVersion.TOMCAT_70)) {
            return "http://" + tp.getHost() + ":" + getCurrentServerPort() + "/manager/text/"; //NOI18N
        }
        return "http://" + tp.getHost() + ":" + getCurrentServerPort() + "/manager/"; //NOI18N
    }

    /** Returns URI of TomcatManager.
     * @return URI without home and base specification
     */
    public String getServerUri () {
        return "http://" + tp.getHost() + ":" + getCurrentServerPort(); //NOI18N
    }

    /**
     * Return path to catalina work directory, which is used to store generated
     * sources and classes from JSPs.
     *
     * @return path to catalina work directory.
     */
    public String getCatalinaWork() {
        TomcatManagerConfig tmConfig = getTomcatManagerConfig();
        String engineName = tmConfig.getEngineElement().getAttributeValue("name"); //NOI18N
        String hostName = tmConfig.getHostElement().getAttributeValue("name"); //NOI18N
        StringBuilder catWork = new StringBuilder(tp.getCatalinaDir().toString());
        catWork.append("/work/").append(engineName).append("/").append(hostName); //NOI18N
        return catWork.toString();
    }

    /** Ensure that the catalina base folder is ready, generate it if empty. */
    public void ensureCatalinaBaseReady() {
        File baseDir = tp.getCatalinaBase();
        if (baseDir != null) {
            String[] files = baseDir.list();
            // if empty, copy all the needed files from the catalina home folder
            if (files == null || files.length == 0) {
                // TODO: display a progress dialog
                createBaseDir(baseDir, tp.getCatalinaHome());
                // check whether filesystem sees it
                if (FileUtil.toFileObject(baseDir) == null) {
                    // try to refresh parent file object
                    File parentDir = baseDir.getParentFile();
                    if (parentDir != null) {
                        FileObject parentFileObject = FileUtil.toFileObject(parentDir);
                        if (parentFileObject != null) {
                            parentFileObject.refresh();
                        }
                    }
                }
            }
        }
    }

    public StartTomcat getStartTomcat(){
        return startTomcat;
    }

    public void setStartTomcat (StartTomcat st){
        startTomcat = st;
    }

    /**
     * Returns true if this server is started in debug mode AND debugger is attached to it.
     * Doesn't matter whether the thread are suspended or not.
     */
    public boolean isDebugged() {

        ServerDebugInfo sdi = null;

        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();

        sdi = getStartTomcat().getDebugInfo(null);
        if (sdi == null) {
            LOGGER.log(Level.INFO, "DebuggerInfo cannot be found for: {0}", this.toString());
        }

        for (int i=0; i < sessions.length; i++) {
            Session s = sessions[i];
            if (s != null) {
                Object o = s.lookupFirst(null, AttachingDICookie.class);
                if (o != null) {
                    AttachingDICookie attCookie = (AttachingDICookie)o;
                    if (sdi.getTransport().equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                        if (attCookie.getSharedMemoryName().equalsIgnoreCase(sdi.getShmemName())) {
                            return true;
                        }
                    } else {
                        if (attCookie.getHostName().equalsIgnoreCase(sdi.getHost())) {
                            if (attCookie.getPortNumber() == sdi.getPort()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Returns true if this server is started in debug mode AND debugger is attached to it
     * AND threads are suspended (e.g. debugger stopped on breakpoint)
     */
    public boolean isSuspended() {

        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        ServerDebugInfo sdi = getStartTomcat().getDebugInfo(null);
        if (sdi == null) {
            LOGGER.log(Level.INFO, "DebuggerInfo cannot be found for: {0}", this.toString());
        }

        for (int i=0; i < sessions.length; i++) {
            Session s = sessions[i];
            if (s != null) {
                Object o = s.lookupFirst(null, AttachingDICookie.class);
                if (o != null) {
                    AttachingDICookie attCookie = (AttachingDICookie)o;
                    if (sdi.getTransport().equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                        String shmem = attCookie.getSharedMemoryName();
                        if (shmem == null) {
                            continue;
                        }
                        if (shmem.equalsIgnoreCase(sdi.getShmemName())) {
                            Object d = s.lookupFirst(null, JPDADebugger.class);
                            if (d != null) {
                                JPDADebugger jpda = (JPDADebugger)d;
                                if (jpda.getState() == JPDADebugger.STATE_STOPPED) {
                                    return true;
                                }
                            }
                        }
                    } else {
                        String host = attCookie.getHostName();
                        if (host == null) {
                            continue;
                        }
                        if (host.equalsIgnoreCase(sdi.getHost())) {
                            if (attCookie.getPortNumber() == sdi.getPort()) {
                                Object d = s.lookupFirst(null, JPDADebugger.class);
                                if (d != null) {
                                    JPDADebugger jpda = (JPDADebugger)d;
                                    if (jpda.getState() == JPDADebugger.STATE_STOPPED) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }
   
    public boolean isAboveTomcat70() {
        return tomcatVersion .isAtLeast(TomcatVersion.TOMCAT_70);
    }
    
    public boolean isTomcat110() {
        return tomcatVersion == TomcatVersion.TOMCAT_110;
    }
    
    public boolean isTomcat101() {
        return tomcatVersion == TomcatVersion.TOMCAT_101;
    }
    
    public boolean isTomcat100() {
        return tomcatVersion == TomcatVersion.TOMCAT_100;
    }
    
    public boolean isTomcat90() {
        return tomcatVersion == TomcatVersion.TOMCAT_90;
    }

    public boolean isTomcat80() {
        return tomcatVersion == TomcatVersion.TOMCAT_80;
    }

    public boolean isTomcat70() {
        return tomcatVersion == TomcatVersion.TOMCAT_70;
    }

    public boolean isTomcat60() {
        return tomcatVersion == TomcatVersion.TOMCAT_60;
    }

    public boolean isTomcat55() {
        return tomcatVersion == TomcatVersion.TOMCAT_55;
    }

    public boolean isTomcat50() {
        return tomcatVersion == TomcatVersion.TOMCAT_50;
    }

    public synchronized boolean isTomEE() {
        loadTomEEInfo();
        return tomEEVersion != null;
    }

    public synchronized boolean isTomEEJaxRS() {
        switch (tomEEType) {
            case TOMEE_PLUME:
            case TOMEE_PLUS:
            case TOMEE_MICROPROFILE:
            case TOMEE_WEBPROFILE:
            case TOMEE_JAXRS:
                return true;
            default:
                return false;
        }
    }
    
    public boolean isTomEE10() {
        return tomEEVersion == TomEEVersion.TOMEE_100;
    }
    
    public boolean isTomEE9() {
        return tomEEVersion == TomEEVersion.TOMEE_90;
    }
    
    public boolean isTomEE8() {
        return tomEEVersion == TomEEVersion.TOMEE_80;
    }
    
    public boolean isTomEEplume() {
        return tomEEType == TomEEType.TOMEE_PLUME;
    }
    
    public boolean isJpa30() {
        return isTomEE9();
    }

    public boolean isJpa31() {
        return false;
    }
    
    public boolean isJpa32() {
        return isTomEE10();
    }

    public boolean isJpa22() {
        return isTomEE8();
    }
    
    public boolean isJpa21() {
        return tomEEVersion.isAtLeast(TomEEVersion.TOMEE_70) && !isTomEE9();
    }
    
    public boolean isJpa20() {
        return tomEEVersion.isAtLeast(TomEEVersion.TOMEE_15) && !isTomEE9();
    }
    
    public boolean isJpa10() {
        return isJpa20(); // All TomEE versions up to 8 support JPA 1.0
    }

    /** Returns Tomcat lib folder: "lib" for  Tomcat 6.0 or greater and "common/lib" for Tomcat 5.x or less*/
    public String libFolder() {
        // Tomcat 5.x and 6.0 uses different lib folder
        return tomcatVersion.isAtLeast(TomcatVersion.TOMCAT_60) ? "lib" : "common/lib"; // NOI18N
    }

    public TomcatVersion getTomcatVersion() {
        return tomcatVersion;
    }

    public synchronized TomEEVersion getTomEEVersion() {
        loadTomEEInfo();
        return tomEEVersion;
    }
    
    public synchronized TomEEType getTomEEType() {
        loadTomEEInfo();
        return tomEEType;
    }

    public void loadTomEEInfo() {
        boolean fireListener = false;
        synchronized (this) {
            if (tomEEChecked) {
                return;
            }
            assert tomEEWarListener == null;

            tomEEChecked = true;
            tomEEVersion = TomcatFactory.getTomEEVersion(tp.getCatalinaHome(), tp.getCatalinaBase());
            tomEEType = tomEEVersion == null ? null : TomcatFactory.getTomEEType(tp.getCatalinaHome(), tp.getCatalinaBase());
            if (tomEEVersion == null) {
                tomEEWarListener = new TomEEWarListener(tp, (TomEEVersion version, TomEEType type) -> {
                    synchronized (TomcatManager.this) {
                        tomEEVersion = version;
                        tomEEType = type;
                    }
                    getTomcatPlatform().notifyLibrariesChanged();
                });
                File listenFile;
                if (tp.getCatalinaBase() != null) {
                    listenFile = new File(tp.getCatalinaBase(), "webapps"); // NOI18N
                } else {
                    listenFile = new File(tp.getCatalinaHome(), "webapps"); // NOI18N
                }

                FileUtil.addFileChangeListener(tomEEWarListener, listenFile);
                fireListener = true;
            }
            LOGGER.log(Level.INFO, "TomEE version {0}, type {1}", new Object[] {tomEEVersion, tomEEType});
        }
        if (fireListener) {
            tomEEWarListener.checkAndRefresh();
        }
    }

// --- DeploymentManager interface implementation ----------------------

    @Override
    public DeploymentConfiguration createConfiguration (DeployableObject deplObj)
    throws InvalidModuleException {
        throw new RuntimeException("This should never be called"); // NOI18N
    }

    @Override
    public Locale getCurrentLocale () {
        return Locale.getDefault ();
    }

    @Override
    public Locale getDefaultLocale () {
        return Locale.getDefault ();
    }

    @Override
    public Locale[] getSupportedLocales () {
        return Locale.getAvailableLocales ();
    }

    @Override
    public boolean isLocaleSupported (Locale locale) {
        if (locale == null) {
            return false;
        }

        Locale [] supLocales = getSupportedLocales ();
        for (int i =0; i<supLocales.length; i++) {
            if (locale.equals (supLocales[i])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public TargetModuleID[] getAvailableModules (ModuleType moduleType, Target[] targetList)
    throws TargetException, IllegalStateException {
        return modules (ENUM_AVAILABLE, moduleType, targetList);
    }

    @Override
    public TargetModuleID[] getNonRunningModules (ModuleType moduleType, Target[] targetList)
    throws TargetException, IllegalStateException {
        return modules (ENUM_NONRUNNING, moduleType, targetList);
    }

    @Override
    public TargetModuleID[] getRunningModules (ModuleType moduleType, Target[] targetList)
    throws TargetException, IllegalStateException {
        return modules (ENUM_RUNNING, moduleType, targetList);
    }

    @Override
    public Target[] getTargets () throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.getTargets called on disconnected instance");   // NOI18N
        }

        // PENDING
        return new TomcatTarget [] {
            new TomcatTarget (uri, "Tomcat at "+uri, getServerUri ())
        };
    }

    @Override
    public DConfigBeanVersionType getDConfigBeanVersion () {
        // PENDING
        return null;
    }

    @Override
    public void setDConfigBeanVersion (DConfigBeanVersionType version)
    throws DConfigBeanVersionUnsupportedException {
        if (!DConfigBeanVersionType.V1_3_1.equals (version)) {
            throw new DConfigBeanVersionUnsupportedException ("unsupported version");
        }
    }

    @Override
    public boolean isDConfigBeanVersionSupported (DConfigBeanVersionType version) {
        return DConfigBeanVersionType.V1_3_1.equals (version);
    }

    @Override
    public boolean isRedeploySupported () {
        // XXX what this really means
        return false;
    }

    @Override
    public ProgressObject redeploy (TargetModuleID[] targetModuleID, InputStream inputStream, InputStream inputStream2)
    throws UnsupportedOperationException, IllegalStateException {
        // PENDING
        throw new UnsupportedOperationException ("TomcatManager.redeploy not supported yet.");
    }

    @Override
    public ProgressObject redeploy (TargetModuleID[] tmID, File file, File file2)
    throws UnsupportedOperationException, IllegalStateException {
        // PENDING
        throw new UnsupportedOperationException ("TomcatManager.redeploy not supported yet.");
    }

    @Override
    public void release () {
    }

    @Override
    public void setLocale (Locale locale) throws UnsupportedOperationException {
    }

    @Override
    public ProgressObject start (TargetModuleID[] tmID) throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.start called on disconnected instance");   // NOI18N
        }
        if (tmID.length != 1 || !(tmID[0] instanceof TomcatModule)) {
            throw new IllegalStateException ("TomcatManager.start invalid TargetModuleID passed");   // NOI18N
        }

        TomcatManagerImpl impl = new TomcatManagerImpl (this);
        impl.start ((TomcatModule)tmID[0]);
        return impl;
    }

    @Override
    public ProgressObject stop (TargetModuleID[] tmID) throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.stop called on disconnected instance");   // NOI18N
        }
        if (tmID.length != 1 || !(tmID[0] instanceof TomcatModule)) {
            throw new IllegalStateException ("TomcatManager.stop invalid TargetModuleID passed");   // NOI18N
        }

        TomcatManagerImpl impl = new TomcatManagerImpl (this);
        impl.stop ((TomcatModule)tmID[0]);
        return impl;
    }

    @Override
    public ProgressObject undeploy (TargetModuleID[] tmID) throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.undeploy called on disconnected instance");   // NOI18N
        }

        if (tmID == null) {
            throw new NullPointerException("TomcatManager.undeploy the tmID argument must not be null."); // NOI18N
        }

        if (tmID.length == 0) {
            throw new IllegalArgumentException("TomcatManager.undeploy at least one TargetModuleID object must be passed."); // NOI18N
        }

        for (int i = 0; i < tmID.length; i++) {
            if (!(tmID[i] instanceof TomcatModule)) {
                throw new IllegalStateException ("TomcatManager.undeploy invalid TargetModuleID passed: " + tmID[i].getClass().getName());   // NOI18N
            }
        }

        TomcatManagerImpl[] tmImpls = new TomcatManagerImpl[tmID.length];
        for (int i = 0; i < tmID.length; i++) {
            tmImpls[i] = new TomcatManagerImpl (this);
        }
        // wrap all the progress objects into a single one
        ProgressObject po = new MultiProgressObjectWrapper(tmImpls);

        for (int i = 0; i < tmID.length; i++) {
            TomcatModule tm = (TomcatModule) tmID[i];
            // it should not be allowed to undeploy the /manager application
            if ("/manager".equals(tm.getPath())) { // NOI18N
                String msg = NbBundle.getMessage(TomcatModule.class, "MSG_CannotUndeployManager");
                throw new IllegalStateException(msg);
            }
            tmImpls[i].remove(tm);
        }
        return po;
    }

    /** Deploys web module using deploy command
     * @param targets Array containg one web module
     * @param is Web application stream
     * @param deplPlan Server specific data
     * @throws IllegalStateException when TomcatManager is disconnected
     * @return Object that reports about deployment progress
     */
    @Override
    public ProgressObject distribute (Target[] targets, InputStream is, InputStream deplPlan)
    throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.distribute called on disconnected instance");   // NOI18N
        }
        LOGGER.log(Level.FINE, "TomcatManager.distribute streams");
        TomcatManagerImpl impl = new TomcatManagerImpl (this);
        impl.deploy (targets[0], is, deplPlan);
        return impl;
    }

    /** Deploys web module using install command
     * @param targets Array containg one web module
     * @param moduleArchive directory with web module or WAR file
     * @param deplPlan Server specific data
     * @throws IllegalStateException when TomcatManager is disconnected
     * @return Object that reports about deployment progress
     */
    @Override
    public ProgressObject distribute (Target[] targets, File moduleArchive, File deplPlan)
    throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.distribute called on disconnected instance");   // NOI18N
        }
        LOGGER.log(Level.FINE, "TomcatManager.distribute archive={0}, plan={1}", new Object[]{moduleArchive.getPath(), deplPlan.getPath()}); // NOI18N
        TomcatManagerImpl impl = new TomcatManagerImpl (this);
        impl.install (targets[0], moduleArchive, deplPlan);
        return impl;
    }

    @Override
    public ProgressObject distribute(Target[] target, ModuleType moduleType, InputStream inputStream, InputStream inputStream0) throws IllegalStateException {
        return distribute(target, inputStream, inputStream0);
    }

// --- End of DeploymentManager interface implementation ----------------------

    /** Utility method that retrieve the list of J2EE application modules
     * distributed to the identified targets.
     * @param state     One of available, running, non-running constants.
     * @param moduleType    Predefined designator for a J2EE module type.
     * @param targetList    A list of deployment Target designators.
     */
    private TargetModuleID[] modules (int state, ModuleType moduleType, Target[] targetList)
    throws TargetException, IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.modules called on disconnected instance");   // NOI18N
        }
        if (targetList.length != 1) {
            throw new TargetException ("TomcatManager.modules supports only one target");   // NOI18N
        }

        if (!ModuleType.WAR.equals (moduleType)) {
            return new TargetModuleID[0];
        }

        TomcatManagerImpl impl = new TomcatManagerImpl (this);
        return impl.list (targetList[0], state);
    }

    /** Connected / disconnected status.
     * @return <CODE>true</CODE> when connected.
     */
    public boolean isConnected () {
        return connected;
    }

    @Override
    public String toString () {
        return "Tomcat manager ["+uri+", home "+tp.getCatalinaHome()+", base "+tp.getCatalinaBase()+(connected?"conneceted":"disconnected")+"]";    // NOI18N
    }

    public void setServerPort(int port) {
        ensureCatalinaBaseReady(); // generated the catalina base folder if empty
        if (TomcatInstallUtil.setServerPort(port, tp.getServerXml())) {
            tp.setServerPort(port);
        }
    }

    public void setShutdownPort(int port) {
        ensureCatalinaBaseReady(); // generated the catalina base folder if empty
        if (TomcatInstallUtil.setShutdownPort(port, tp.getServerXml())) {
            tp.setShutdownPort(port);
        }
    }

    /** If Tomcat is running, return the port it was started with. Please note that
     * the value in the server.xml (returned by getServerPort()) may differ. */
    public int getCurrentServerPort() {
        if (startTomcat != null && isRunning(false)) {
            return startTomcat.getCurrentServerPort();
        } else {
            return getServerPort();
        }
    }

    /** Return server port defined in the server.xml file, this value may differ
     * from the actual port Tomcat is currently running on @see #getCurrentServerPort(). */
    public int getServerPort() {
        ensureConnectionInfoUptodate();
        return tp.getServerPort();
    }

    public int getShutdownPort() {
        ensureConnectionInfoUptodate();
        return tp.getShutdownPort();
    }

    public String getServerHeader() {
        ensureConnectionInfoUptodate();
        return tp.getServerHeader();
    }
    
    private void ensureConnectionInfoUptodate() {
        File serverXml = tp.getServerXml();
        long timestamp = -1;
        if (serverXml.exists()) {
            timestamp = serverXml.lastModified();
            if (timestamp > tp.getTimestamp()) {
                try {
                    // for the bundled tomcat we cannot simply use the server.xml
                    // file from the home folder, since we change the port numbers
                    // during base folder generation
                    if (isBundledTomcat() && !new File(tp.getCatalinaBase(), "conf/server.xml").exists()) { // NOI18N
                        tp.setTimestamp(timestamp);
                        tp.setServerPort(TomcatProperties.DEF_VALUE_BUNDLED_SERVER_PORT);
                        tp.setShutdownPort(TomcatProperties.DEF_VALUE_BUNDLED_SHUTDOWN_PORT);
                        return;
                    }
                    Server server = Server.createGraph(serverXml);
                    tp.setTimestamp(timestamp);
                    tp.setServerPort(Integer.parseInt(TomcatInstallUtil.getPort(server)));
                    tp.setShutdownPort(Integer.parseInt(TomcatInstallUtil.getShutdownPort(server)));
                    tp.setServerHeader(TomcatInstallUtil.getServerHeader(server));
                } catch (IOException | RuntimeException ioe) {
                    LOGGER.log(Level.INFO, null, ioe);
                }
            }
        }
    }

    public Server getRoot() {
        try {
            return Server.createGraph(tp.getServerXml());
        } catch (IOException e) {
            LOGGER.log(Level.FINE, null, e);
            return null;
        } catch (RuntimeException e) {
            LOGGER.log(Level.INFO, null, e);
            return null;
        }
    }

    /** Initializes base dir for use with Tomcat 5.0.x.
     *  @param baseDir directory for base dir.
     *  @param homeDir directory to copy config files from.
     *  @return File with absolute path for created dir or <CODE>null</CODE> when ther is an error.
     */
    public File createBaseDir(File baseDir, File homeDir) {
        File targetFolder;
        if (!baseDir.isAbsolute ()) {
            baseDir = new File(System.getProperty("netbeans.user")+System.getProperty("file.separator")+baseDir);
            targetFolder = new File(System.getProperty("netbeans.user"));

        } else {
            targetFolder = baseDir.getParentFile ();
        }

        try {

            if (targetFolder == null) {
                LOGGER.log(Level.INFO, "Cannot find parent folder for base dir {0}", baseDir.getPath());
                return null;
            }
            File baseDirFO = new File (targetFolder, baseDir.getName ());
            baseDirFO.mkdir ();

            // create directories
            String [] subdirs = new String [] {
                "conf",   // NOI18N
                "conf/Catalina",   // NOI18N
                "conf/Catalina/localhost",   // NOI18N
                "logs",   // NOI18N
                "work",   // NOI18N
                "temp",   // NOI18N
                "webapps", // NOI18N
                // TOMEE dir
                "conf/conf.d" // NOI18N
            };
            for (int i = 0; i<subdirs.length; i++) {
                File dest = new File (baseDirFO, subdirs [i]);
                dest.mkdirs ();
            }
            // copy config files
            final String ADMIN_XML = "conf/Catalina/localhost/admin.xml";
            String [] files = new String [] {
                "conf/catalina.policy",   // NOI18N
                "conf/catalina.properties",   // NOI18N
                "conf/logging.properties", // NOI18N
                "conf/server.xml",   // NOI18N
                "conf/tomcat-users.xml",   // NOI18N
                "conf/web.xml",   // NOI18N
                ADMIN_XML,   // NOI18N For bundled tomcat 5.0.x
                "conf/Catalina/localhost/manager.xml",   // NOI18N
                // TOMEE files
                "conf/system.properties", // NOI18N
                "conf/tomee.xml", // NOI18N
                "conf/conf.d/hsql.properties" // NOI18N
            };
            boolean[] userReadOnly = new boolean[] {
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                false,
                false
            };
            String [] patternFrom = new String [] {
                null,
                null,
                null,
                null,
                "</tomcat-users>",   // NOI18N
                null,
                "docBase=\"../server/webapps/admin\"", // NOI18N For bundled tomcat 5.0.x
                isTomcat50() || isTomcat55() ? "docBase=\"../server/webapps/manager\"" : null, // NOI18N
                null,
                null,
                null
            };
            String passwd = null;
            if (isBundledTomcat()) {
                passwd = tp.getPassword();
                if ("ide_manager".equals(passwd)) { // NOI18N
                    // change the default password that comes from the bundled Tomcat module
                    passwd = Utils.generatePassword(8);
                    tp.setPassword(passwd);
                }
            }

            String usersString = null;
            if (passwd != null) {
                if (isAboveTomcat70()) {
                    usersString = "<user username=\"ide\" password=\"" + passwd + "\" roles=\"manager-script,admin\"/>\n</tomcat-users>";
                } else {
                    usersString = "<user username=\"ide\" password=\"" + passwd + "\" roles=\"manager,admin\"/>\n</tomcat-users>";
                }
            }
            String [] patternTo = new String [] {
                null,
                null,
                null,
                null,
                usersString,
                null,
                "docBase=\"${catalina.home}/server/webapps/admin\"", // NOI18N For bundled tomcat 5.0.x
                isTomcat50() || isTomcat55() ? "docBase=\"${catalina.home}/server/webapps/manager\"" : null, // NOI18N
                null,
                null,
                null
            };
            for (int i = 0; i < files.length; i++) {
                // get folder from, to, name and ext
                int slash = files[i].lastIndexOf ("/");
                String sfolder = files[i].substring (0, slash);
                File fromDir = new File (homeDir, sfolder); // NOI18N
                File toDir = new File (baseDir, sfolder); // NOI18N
                File targetFile = new File (toDir, files[i].substring (slash+1));

                if (patternTo[i] == null) {
                    File fileToCopy = new File(homeDir, files[i]);
                    if (!fileToCopy.exists()) {
                        LOGGER.log(Level.INFO, "Cannot copy file {0} to the Tomcat base dir, since it does not exist.", fileToCopy.getAbsolutePath());
                        continue;
                    }
                    try (FileInputStream is = new FileInputStream(fileToCopy);
                            FileOutputStream os = new FileOutputStream(targetFile)) {
                        FileUtil.copy(is, os);
                    } catch (IOException ioe) {
                        LOGGER.log(Level.INFO, null, ioe);
                    }
                } else {
                    // use patched version
                    if (!copyAndPatch (
                        new File (fromDir, files[i].substring (slash+1)),
                        targetFile,
                        patternFrom[i],
                        patternTo[i]
                        )) {
                        if (!(ADMIN_XML.equals(files[i]) && !(new File (fromDir, files[i].substring (slash+1))).exists()) ){
                            LOGGER.log(Level.INFO, "Cannot create config file {0}", files[i]);
                            continue;
                        }
                    }
                }

                if (userReadOnly[i]) {
                    if (targetFile.setReadable(false, false)) {
                        targetFile.setReadable(true);
                    }
                    if (targetFile.setWritable(false, false)) {
                        targetFile.setWritable(true);
                    }
                }
            }

            // deploy the ROOT context, if exists
            if (new File(homeDir, "webapps/ROOT").exists()) { // NOI18N
                writeToFile(new File(baseDir, "conf/Catalina/localhost/ROOT.xml"), // NOI18N
                    "<Context path=\"\" docBase=\"${catalina.home}/webapps/ROOT\"/>\n"); // NOI18N
            }
            // since tomcat 6.0 the manager app lives in the webapps folder
            if (!isTomcat50() && !isTomcat55() && new File(homeDir, "webapps/manager").exists()) { // NOI18N
                writeToFile(new File(baseDir, "conf/Catalina/localhost/manager.xml"), // NOI18N
                     "<Context docBase=\"${catalina.home}/webapps/manager\" antiResourceLocking=\"false\" privileged=\"true\"/>\n"); // NOI18N
            }
            // TOMEE deploy
            if (isTomEE()) {
                File tomee = TomcatFactory.getTomEEWebAppJar(homeDir);
                if (tomee != null) {
                    // cd to lib and to app folder
                    File folder = tomee.getParentFile().getParentFile();
                    assert folder != null;
                    writeToFile(new File(baseDir, "conf/Catalina/localhost/" + folder.getName() + ".xml"), // NOI18N
                        "<Context path=\"" + folder.getName() + "\" docBase=\"${catalina.home}/webapps/" + folder.getName() + "\"/>\n"); // NOI18N
                }
            }

            if (isBundledTomcat()) {
                // create a special directory for the HTTP Monitor libs under the CATALINA_BASE
                // directory, since the user might not have write access to CATALINA_HOME
                TomcatInstallUtil.patchCatalinaProperties(new File(baseDir, "conf/catalina.properties")); // NOI18N
                TomcatInstallUtil.createNBLibDirectory(baseDir);
            }
        } catch (java.io.IOException ioe) {
            LOGGER.log(Level.INFO, null, ioe);
            return null;
        }
        if (isBundledTomcat()) {
            TomcatInstallUtil.patchBundledServerXml(new File(baseDir, "conf/server.xml")); // NOI18N
        }
        return baseDir;
    }

    /**
     * Create a file and fill it with the data.
     */
    private void writeToFile(File file, String data) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(data);
        }
    }

    /** Copies server.xml file and patches appBase="webapps" to
     * appBase="$CATALINA_HOME/webapps" during the copy.
     * @return success status.
     */
    private boolean copyAndPatch (File src, File dst, String from, String to) {
        
        if (!src.exists()) {
            return false;
        }
        try (Reader r = new BufferedReader (new InputStreamReader (new FileInputStream (src), StandardCharsets.UTF_8));
                Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream (dst), StandardCharsets.UTF_8))) {
             // NOI18N
            StringBuilder sb = new StringBuilder();
            final char[] BUFFER = new char[4096];
            int len;

            for (;;) {
                len = r.read (BUFFER);
                if (len == -1) {
                    break;
                }
                sb.append (BUFFER, 0, len);
            }
            int idx = sb.toString ().indexOf (from);
            if (idx >= 0) {
                sb.replace (idx, idx+from.length (), to);  // NOI18N
            }
            else {
                // Something unexpected
                LOGGER.log(Level.INFO, "Pattern {0} not found in {1}", new Object[]{from, src.getPath()}); // NOI18N
            }
            out.write (sb.toString ());

        } catch (java.io.IOException ioe) {
            LOGGER.log(Level.INFO, null, ioe);
            return false;
        }
        return true;
    }

    /**
     * Open a context log for the specified module, if specified module does not
     * have its own logger defined, open shared context log instead.
     *
     * @param module module its context log should be opened
     */
    public void openLog(TargetModuleID module) {
        TomcatModule tomcatModule = null;
        if (module instanceof TomcatModule) {
            tomcatModule = (TomcatModule)module;
        } else {
            try {
                TargetModuleID[] tomMod = getRunningModules(ModuleType.WAR, new Target[]{module.getTarget()});
                for (int i = 0; i < tomMod.length; i++) {
                    if (module.getModuleID().equals(tomMod[i].getModuleID())) {
                        tomcatModule = (TomcatModule)tomMod[i];
                        break;
                    }
                }
            } catch (TargetException te) {
                LOGGER.log(Level.INFO, null, te);
            }
        }
        if (tomcatModule != null && logManager.hasContextLogger(tomcatModule)) {
            logManager.openContextLog(tomcatModule);
        } else {
            logManager.openSharedContextLog();
        }
    }

    /**
     * Return <code>TomcatManagerConfig</code> for easier access to some server.xml
     * settings.
     *
     * @return <code>TomcatManagerConfig</code> for easier access to some server.xml
     *         settings.
     */
    public synchronized TomcatManagerConfig getTomcatManagerConfig() {
        if (tomcatManagerConfig == null) {
            tomcatManagerConfig = new TomcatManagerConfig(tp.getServerXml());
        }
        return tomcatManagerConfig;
    }

    /**
     * Return <code>LogManager</code> which manages all context and shared context
     * logs for this <code>TomcatManager</code>.
     *
     * @return <code>LogManager</code> which manages all context and shared context
     *         logs for this <code>TomcatManager</code>.
     */
    public LogManager logManager() {
        return logManager;
    }

    /**
     * Set the <code>Process</code> of the started Tomcat.
     *
     * @param <code>Process</code> of the started Tomcat.
     */
    public synchronized void setTomcatProcess(Process p) {
        process = p;
    }

    /**
     * Return <code>Process</code> of the started Tomcat.
     *
     * @return <code>Process</code> of the started Tomcat, <code>null</code> if
     *         Tomcat wasn't started by IDE.
     */
    public synchronized Process getTomcatProcess() {
        return process;
    }

    /** Terminates the running Tomcat process. */
    public void terminate() {
        Process proc = getTomcatProcess();
        if (proc != null) {
            Map<String, String> env = new HashMap<>();
            env.put(KEY_UUID, uri);
            ExternalProcessSupport.destroy(process, env);
        }
    }

    public synchronized TomcatPlatformImpl getTomcatPlatform() {
        if (tomcatPlatform == null) {
            tomcatPlatform = new TomcatPlatformImpl(this);
        }
        return tomcatPlatform;
    }

}
