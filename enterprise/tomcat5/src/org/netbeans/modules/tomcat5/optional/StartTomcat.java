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

package org.netbeans.modules.tomcat5.optional;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.modules.j2ee.deployment.plugins.api.CommonServerBridge;
import org.netbeans.modules.tomcat5.progress.ProgressEventSupport;
import org.netbeans.modules.tomcat5.progress.Status;
import org.netbeans.modules.tomcat5.util.LogManager;
import org.netbeans.modules.tomcat5.util.Utils;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerSupport;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import org.netbeans.modules.tomcat5.util.TomcatProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.xml.sax.SAXException;

/** Extension to Deployment API that enables starting of Tomcat.
 *
 * @author Radim Kubacki, Pavel Buzek
 */
public final class StartTomcat extends StartServer implements ProgressObject {
    
    public static final String CATALINA_BAT     = "catalina.bat";    // NOI18N
    public static final String CATALINA_SH      = "catalina.sh";     // NOI18N
    public static final String CATALINA_50_BAT  = "catalina.50.bat"; // NOI18N
    public static final String CATALINA_50_SH   = "catalina.50.sh";  // NOI18N
    
    public static final String SETCLASSPATH_BAT = "setclasspath.bat"; // NOI18N
    public static final String SETCLASSPATH_SH  = "setclasspath.sh";  // NOI18N
        
    public static final String TAG_CATALINA_HOME = "catalina_home"; // NOI18N
    public static final String TAG_CATALINA_BASE = "catalina_base"; // NOI18N
    
    public static final String TAG_JPDA = "jpda"; // NOI18N
    public static final String TAG_JPDA_STARTUP = "jpda_startup"; // NOI18N

    /** Startup command tag. */
    public static final String TAG_EXEC_CMD      = "catalina"; // NOI18N
    public static final String TAG_EXEC_STARTUP  = "exec_startup"; // NOI18N
    public static final String TAG_EXEC_SHUTDOWN = "exec_shutdown"; // NOI18N
    public static final String TAG_SECURITY_OPT = "security_option"; //NOI18N
    public static final String TAG_FORCE_OPT = "force_option"; //NOI18N

    /** Debug startup/shutdown tag */
    public static final String TAG_DEBUG_CMD   = "catalina"; // NOI18N

    // at most 5 instances starting stopping at the same time
    private static final RequestProcessor SERVER_CONTROL_RP = new RequestProcessor("Tomcat Control", 5); 
    
    private static final RequestProcessor SERVER_STREAMS_RP = new RequestProcessor("Tomcat Streams", 5 * 2); 
    
    /** Normal mode */
    private static final int MODE_RUN     = 0;
    /** Debug mode */
    private static final int MODE_DEBUG   = 1;
    /** Profile mode */
    private static final int MODE_PROFILE = 2;
    
    /** For how long should we keep trying to get response from the server. */
    private static final long DEFAULT_TIMEOUT_DELAY = 180000;

    private static final Pattern WINDOWS_ESCAPED_JAVA_OPTS = Pattern.compile("^set\\s\"JAVA_OPTS.*$");
    
    private static final Logger LOGGER = Logger.getLogger(StartTomcat.class.getName());
        
    private TomcatManager tm;

    // GuardedBy("this")
    private boolean javaOptsEscaped;

    // GuardedBy("this")
    private Date lastCheckedStart;
    
    private ProgressEventSupport pes;
    private int currentServerPort; // current server port Tomcat is running on
    
    private static Map<String, Object> isDebugModeUri = Collections.synchronizedMap(new HashMap<>(2,1));
    
    public StartTomcat (DeploymentManager manager) {
        assert manager instanceof TomcatManager : 
            "Illegal DeploymentManager instance: " + manager.getClass().getName(); // NIO18N
        tm = (TomcatManager)manager;
        tm.setStartTomcat (this);
        pes = new ProgressEventSupport (this);
        currentServerPort = tm.getServerPort();
    }
    
    @Override
    public boolean supportsStartDeploymentManager () {
        return true;
    }
    
    @Override
    public boolean supportsStartProfiling(Target target) {
        return true;
    }
    
    /** Start Tomcat server if the TomcatManager is not connected.
     */
    @Override
    public ProgressObject startDeploymentManager () {
        LOGGER.log(Level.FINE, "StartTomcat.startDeploymentManager called on {0}", tm);    // NOI18N
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, CommandType.START, "", StateType.RUNNING));
        SERVER_CONTROL_RP.post(new StartRunnable(MODE_RUN, CommandType.START),
                0, Thread.NORM_PRIORITY);
        isDebugModeUri.remove(tm.getUri());
        return this;
    }
    
    /**
     * Returns true if the admin server is also a target server (share the same vm).
     * Start/stopping/debug apply to both servers.
     * @return true when admin is also target server
     */
    @Override
    public boolean isAlsoTargetServer(Target target) { return true; }

    /**
     * Returns true if the admin server should be started before configure.
     */
    @Override
    public boolean needsStartForConfigure() { return false; }

    /**
     * Returns true if the admin server should be started before asking for
     * target list.
     */
    @Override
    public boolean needsStartForTargetList() { return false; }

    /**
     * Returns true if the admin server should be started before admininistrative configuration.
     */
    @Override
    public boolean needsStartForAdminConfig() { return false; }
    
    @Override
    public boolean needsRestart(Target target) {
         return tm.getNeedsRestart();
    }

    /**
     * Returns true if this admin server is running.
     */
    @Override
    public boolean isRunning() {
        return tm.isRunning (true);
    }

    /**
     * Returns true if this target is in debug mode.
     */
    @Override
    public boolean isDebuggable(Target target) {
        if (!isDebugModeUri.containsKey(tm.getUri())) {
            return false;
        }
        if (!isRunning()) {
            isDebugModeUri.remove(tm.getUri());
            return false;
        }
        return true;
    }

    /**
     * Stops the admin server. The DeploymentManager object will be disconnected.
     * All diagnostic should be communicated through ServerProgres with no 
     * exceptions thrown.
     * @return ServerProgress object used to monitor start server progress
     */
    @Override
    public ProgressObject stopDeploymentManager() { 
        LOGGER.log(Level.FINE, "StartTomcat.stopDeploymentManager called on {0}", tm);    // NOI18N
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, CommandType.STOP, "", StateType.RUNNING));
        SERVER_CONTROL_RP.post(new StartRunnable(MODE_RUN, CommandType.STOP),
                0, Thread.NORM_PRIORITY);
        isDebugModeUri.remove(tm.getUri());
        return this;
    }

    /**
     * Start or restart the target in debug mode.
     * If target is also domain admin, the amdin is restarted in debug mode.
     * All diagnostic should be communicated through ServerProgres with no exceptions thrown.
     * @param target the target server
     * @return ServerProgress object to monitor progress on start operation
     */
    @Override
    public ProgressObject startDebugging(Target target) {
        LOGGER.log(Level.FINE, "StartTomcat.startDebugging called on {0}", tm);    // NOI18N
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, CommandType.START, "", StateType.RUNNING));
        SERVER_CONTROL_RP.post(new StartRunnable(MODE_DEBUG, CommandType.START),
                0, Thread.NORM_PRIORITY);
        return this;
    }
    
    @Override
    public ProgressObject startProfiling(Target target) {
        LOGGER.log(Level.FINE, "StartTomcat.startProfiling called on {0}", tm); // NOI18N
        pes.fireHandleProgressEvent(null, new Status(
                                                ActionType.EXECUTE, 
                                                CommandType.START, 
                                                "",  // NOI18N
                                                StateType.RUNNING));
        SERVER_CONTROL_RP.post(new StartRunnable(MODE_PROFILE, CommandType.START),
                0, Thread.NORM_PRIORITY);
        return this;
    }

    @Override
    public ServerDebugInfo getDebugInfo(Target target) { 
        ServerDebugInfo sdi;
        TomcatProperties tp = tm.getTomcatProperties();
        if (tp.getDebugType().toLowerCase().indexOf("socket") != -1) {  // NOI18N
            sdi = new ServerDebugInfo("localhost", tp.getDebugPort());  // NOI18N
        } else {
            sdi = new ServerDebugInfo("localhost", tp.getSharedMem());  // NOI18N
        }
        return sdi;
    }
    
    private class StartRunnable implements Runnable {
        
        private int mode;
        private CommandType command = CommandType.START;
        
        public StartRunnable(int mode, CommandType command) {
            this.mode = mode;
            this.command = command;
        }
        
        @Override
        public synchronized void run () {
            // PENDING check whether is runs or not
            TomcatProperties tp = tm.getTomcatProperties();
            File homeDir = tp.getCatalinaHome();
            if (homeDir == null || !homeDir.exists()) {
                fireCmdExecProgressEvent(
                    command == CommandType.START ? "MSG_NoHomeDirStart" : "MSG_NoHomeDirStop",
                    StateType.FAILED);
                return;
            }
            File baseDir = tp.getCatalinaBase();
            if (baseDir == null) {
                baseDir = homeDir;
            } else {
                if (baseDir != null) {
                    String[] files = baseDir.list();
                    if (files == null || files.length == 0) {
                        baseDir = tm.createBaseDir(baseDir, homeDir);
                    }
                }
                if (baseDir == null) {
                    fireCmdExecProgressEvent(
                        command == CommandType.START ? "MSG_NoBaseDirStart" : "MSG_NoBaseDirStop",
                        StateType.FAILED);
                    return;
                }
            }
            
            // check whether the startup script - catalina.sh/bat exists
            File startupScript = getStartupScript();
            if (!startupScript.exists()) {
                final String MSG = NbBundle.getMessage(
                        StartTomcat.class, 
                        command == CommandType.START ? "MSG_StartFailedNoStartScript" : "MSG_StopFailedNoStartScript",
                        startupScript.getAbsolutePath());
                pes.fireHandleProgressEvent(
                    null,
                    new Status(ActionType.EXECUTE, command, MSG, StateType.FAILED));
                return;
            }
            
            // install the monitor
            if (command == CommandType.START) {
                try {
                    MonitorSupport.synchronizeMonitorWithFlag(tm, true);
                } catch (IOException e) {
                    if (MonitorSupport.getMonitorFlag(tm)) {
                        // tomcat has been started with monitor enabled
                        MonitorSupport.setMonitorFlag(tm, false);
                        fireCmdExecProgressEvent(!tm.isTomcat50() && !tm.isTomcat55() ? "MSG_enableMonitorSupportErr60" : "MSG_enableMonitorSupportErr", StateType.FAILED);
                    } else {
                        // tomcat has been started with monitor disabled
                        fireCmdExecProgressEvent("MSG_disableMonitorSupportErr", StateType.FAILED);
                    }
                    LOGGER.log(Level.INFO, null, e);
                    return;
                } catch (SAXException e) {
                    // fault, but not a critical one
                    LOGGER.log(Level.INFO, null, e);
                }
                try {
                    DebugSupport.allowDebugging(tm);
                } catch (IOException | SAXException e) {
                    // fault, but not a critical one
                    LOGGER.log(Level.INFO, null, e);
                }
            }
            
            currentServerPort = tm.getServerPort(); // remember the server port
            int shutdownPort = tm.getShutdownPort();
            
            if (command == CommandType.START) {
                // check whether the server ports are free
                if (!Utils.isPortFree(currentServerPort)) {
                    fireCmdExecProgressEvent("MSG_StartFailedServerPortInUse", String.valueOf(currentServerPort), StateType.FAILED);
                    return;
                }
                if (!Utils.isPortFree(shutdownPort)) {
                    fireCmdExecProgressEvent("MSG_StartFailedShutdownPortInUse", String.valueOf(shutdownPort), StateType.FAILED);
                    return;
                }
            }
            
            // set the JAVA_OPTS value
            String javaOpts = tp.getJavaOpts();            
            // use the IDE proxy settings if the 'use proxy' checkbox is selected
            // do not override a property if it was set manually by the user
            StringBuilder sb = new StringBuilder(javaOpts);
            if (tp.getProxyEnabled()) {
                final String[] PROXY_PROPS = {
                    "http.proxyHost",       // NOI18N
                    "http.proxyPort",       // NOI18N
                    "http.nonProxyHosts",   // NOI18N
                    "https.proxyHost",      // NOI18N
                    "https.proxyPort",      // NOI18N
                };
                boolean isWindows = Utilities.isWindows();
                for (String prop : PROXY_PROPS) {
                    if (!javaOpts.contains(prop)) {
                        String value = System.getProperty(prop);
                        if (value != null) {
                            if ("http.nonProxyHosts".equals(prop)) { // NOI18N
                                if (isWindows) {
                                    boolean javaOptsEscaped = isJavaOptsEscaped();
                                    if (javaOptsEscaped) {
                                        value = value.replaceAll("\\|", "^|"); // NOI18N
                                    } else {
                                        value = "\"" + value + "\""; // NOI18N
                                    }
                                } else if (tm.isAboveTomcat70()) {
                                    value = "\"" + value + "\""; // NOI18N
                                }
                            }
                            sb.append(" -D").append(prop).append("=").append(value); // NOI18N
                        }
                    }
                }
            }

            if (command == CommandType.START) {
                for (StartupExtender args : StartupExtender.getExtenders(
                        Lookups.singleton(CommonServerBridge.getCommonInstance(tm.getUri())), getMode(mode))) {
                    for (String singleArg : args.getArguments()) {
                        sb.append(' ').append(singleArg);
                    }
                }
            }
            javaOpts = sb.toString();
            
            JavaPlatform platform = getJavaPlatform();
            String jdkVersion = platform.getSpecification().getVersion().toString();
            
            if (tm.isBundledTomcat()) {
                // work-arounding problems caused by the compatibility pack when running on 1.5
                // ensure that the catalina class loader is set properly
                patchCatalinaProperties(tp.getCatalinaDir(), "1.4".equals(jdkVersion)); // NOI18N
            }
            
            if ((mode == MODE_DEBUG) && (command == CommandType.START)) {

                NbProcessDescriptor pd  = null;
                if (tp.getSecManager()) {
                    pd = defaultDebugStartDesc (TAG_DEBUG_CMD, TAG_JPDA_STARTUP, TAG_SECURITY_OPT);
                } else {
                    pd = defaultDebugStartDesc (TAG_DEBUG_CMD, TAG_JPDA_STARTUP);
                }
                try {
                    fireCmdExecProgressEvent("MSG_startProcess", StateType.RUNNING);
                    Process p = null;
                    
                    String address;
                    String transport;
                    if (tp.getDebugType().toLowerCase().indexOf("socket") != -1) { // NOI18N
                        transport = "dt_socket"; // NOI18N
                        address = Integer.toString(tp.getDebugPort());
                    } else {
                        transport = "dt_shmem"; // NOI18N
                        address = tp.getSharedMem();
                    }
                    LOGGER.log(Level.FINE, "transport: {0}", transport);    // NOI18N
                    LOGGER.log(Level.FINE, "address: {0}", address);    // NOI18N
                    p = pd.exec (
                        new TomcatFormat(startupScript, homeDir),
                        new String[] {
                            "JAVA_HOME="        + getJavaHome(platform), // NOI18N
                            "JRE_HOME=",  // NOI18N ensure that JRE_HOME system property won't be used instead of JAVA_HOME
                            "JAVA_OPTS="        + javaOpts, // NOI18N
                            "JPDA_TRANSPORT="   + transport,        // NOI18N
                            "JPDA_ADDRESS="     + address,          // NOI18N
                            "CATALINA_HOME="    + homeDir.getAbsolutePath(),    // NOI18N
                            "CATALINA_BASE="    + baseDir.getAbsolutePath(),    // NOI18N
                            // this is used in the setclasspath.sb/bat script for work-arounding 
                            // problems caused by the compatibility pack when running on 1.5
                            "NB_TOMCAT_JDK="    + jdkVersion,   // NOI18N
                            TomcatManager.KEY_UUID + "=" + tm.getUri()
                        },
                        true,
                        new File (homeDir, "bin") // NOI18N
                    );
                    tm.setTomcatProcess(p);
                    openLogs();
                } catch (java.io.IOException ioe) {
                    LOGGER.log(Level.FINE, null, ioe);
                    fireCmdExecProgressEvent(command == CommandType.START ? "MSG_StartFailedIOE" : "MSG_StopFailedIOE",
                            startupScript.getAbsolutePath(), StateType.FAILED);
                    return;
                }
            } else {
                NbProcessDescriptor pd = null;
                if (command == CommandType.START) {
                    if (tp.getSecManager()) {
                        pd = defaultExecDesc(TAG_EXEC_CMD, TAG_EXEC_STARTUP, TAG_SECURITY_OPT);
                    } else {
                        pd = defaultExecDesc(TAG_EXEC_CMD, TAG_EXEC_STARTUP);
                    }
                } else {
                    if (tp.getForceStop() && Utilities.isUnix()) {
                        pd = defaultExecDesc(TAG_EXEC_CMD, TAG_EXEC_SHUTDOWN, TAG_FORCE_OPT);
                    } else {
                        pd = defaultExecDesc(TAG_EXEC_CMD, TAG_EXEC_SHUTDOWN);
                    }
                }
                try {
                    fireCmdExecProgressEvent(command == CommandType.START ? "MSG_startProcess" : "MSG_stopProcess",
                            StateType.RUNNING);
                    Process p = pd.exec (
                        new TomcatFormat (startupScript, homeDir),
                        new String[] { 
                            "JAVA_HOME="        + getJavaHome(platform),   // NOI18N
                            "JRE_HOME=",  // NOI18N ensure that JRE_HOME system property won't be used instead of JAVA_HOME
                            "JAVA_OPTS="        + javaOpts, // NOI18N
                            "CATALINA_HOME="    + homeDir.getAbsolutePath(),    // NOI18N
                            "CATALINA_BASE="    + baseDir.getAbsolutePath(),    // NOI18N
                            // this is used in the setclasspath.sb/bat script for work-arounding 
                            // problems caused by the compatibility pack when running on 1.5
                            "NB_TOMCAT_JDK="    + jdkVersion,       // NOI18N
                            TomcatManager.KEY_UUID + "=" + tm.getUri()
                        },
                        true,
                        new File (homeDir, "bin")
                    );
                    if (command == CommandType.START) {
                        tm.setTomcatProcess(p);
                        openLogs();
                    } else {
                        // #58554 workaround
                        SERVER_STREAMS_RP.post(new StreamConsumer(p.getInputStream()), 0, Thread.MIN_PRIORITY);
                        SERVER_STREAMS_RP.getDefault().post(new StreamConsumer(p.getErrorStream()), 0, Thread.MIN_PRIORITY);
                    }
                } catch (java.io.IOException ioe) {
                    LOGGER.log(Level.FINE, null, ioe);    // NOI18N
                    fireCmdExecProgressEvent(command == CommandType.START ? "MSG_StartFailedIOE" : "MSG_StopFailedIOE",
                            startupScript.getAbsolutePath(), StateType.FAILED);
                    return;
                }
            }            
            fireCmdExecProgressEvent("MSG_waiting", StateType.RUNNING);
            if (hasCommandSucceeded()) {
                if (command == CommandType.START) {
                    // reset the need restart flag
                    tm.setNeedsRestart(false);
                    if (mode == MODE_DEBUG) {
                        isDebugModeUri.put(tm.getUri(), new Object());
                    }
                }
                fireCmdExecProgressEvent(command == CommandType.START ? "MSG_Started" : "MSG_Stopped", 
                                         StateType.COMPLETED);
            } else {
                fireCmdExecProgressEvent(command == CommandType.START ? "MSG_StartFailed" : "MSG_StopFailed", 
                                         StateType.FAILED);
            }
        }

        private StartupExtender.StartMode getMode(int mode) {
            switch (mode) {
                case MODE_PROFILE:
                    return StartupExtender.StartMode.PROFILE;
                case MODE_DEBUG:
                    return StartupExtender.StartMode.DEBUG;
                default:
                    return StartupExtender.StartMode.NORMAL;
            }
        }

        /** Open JULI log and server output */
        private void openLogs() {
            LogManager logManager = tm.logManager();
            if (logManager.hasJuliLog()) {
                logManager.openJuliLog();
            }
            logManager.closeServerLog();
            logManager.openServerLog();
        }
        
        /**
         * Fires command progress event of action type <code>ActionType.EXECUTE</code>.
         *
         * @param resName event status message from the bundle, specified by the 
         *        resource name.
         * @param stateType event state type.
         */
        private void fireCmdExecProgressEvent(String resName, StateType stateType) {
            String msg = NbBundle.getMessage(StartTomcat.class, resName);
            pes.fireHandleProgressEvent(
                null,
                new Status(ActionType.EXECUTE, command, msg, stateType));
        }
        
        /**
         * Fires command progress event of action type <code>ActionType.EXECUTE</code>.
         *
         * @param resName event status message from the bundle, specified by the 
         *        resource name.
         * @param arg1 the argument to use when formating the message
         * @param stateType event state type.
         */
        private void fireCmdExecProgressEvent(String resName, Object arg1, StateType stateType) {
            String msg = NbBundle.getMessage(StartTomcat.class, resName, arg1);
            pes.fireHandleProgressEvent(
                null,
                new Status(ActionType.EXECUTE, command, msg, stateType));
        }
        
        /**
         * Try to get response from the server, whether the START/STOP command has 
         * succeeded.
         *
         * @return <code>true</code> if START/STOP command completion was verified,
         *         <code>false</code> if time-out ran out.
         */
        private boolean hasCommandSucceeded() {          
            long timeout = System.currentTimeMillis();
            
            if (command == CommandType.START) {
                timeout += tm.getTomcatProperties().getStartupTimeout() * 1000l;
            }
            else if (command == CommandType.STOP) {
                timeout += tm.getTomcatProperties().getShutdownTimeout() * 1000l;
            }
            else {
                timeout += DEFAULT_TIMEOUT_DELAY;
            }
            
            while (true) {
                boolean isRunning = isRunning();
                if (command == CommandType.START) {
                    if (isRunning) {
                        return true;
                    }
                    if (isStopped()) {
                        // Tomcat failed to start, process is finished
                        return false;
                    }
                    if (mode == MODE_PROFILE) {
                        int state = ProfilerSupport.getState();
                        if (state == ProfilerSupport.STATE_BLOCKING || 
                            state == ProfilerSupport.STATE_RUNNING  ||
                            state == ProfilerSupport.STATE_PROFILING) {
                            return true;
                        } else if (state == ProfilerSupport.STATE_INACTIVE) {
                            return false;
                        }
                    }
                }
                if (command == CommandType.STOP) {
                    if (isStopped()) {
                        // give server a few secs to finish its shutdown, not responding
                        // does not necessarily mean its is still not running
                        try {
                            Thread.sleep(2000);
                        } catch(InterruptedException ie) {}
                        return true;
                    }
                }
                // if time-out ran out, suppose command failed
                if (System.currentTimeMillis() > timeout) {
                    return false;
                }
                try {
                    Thread.sleep(1000); // take a nap before next retry
                } catch(InterruptedException ie) {}
            }
        }
    }
    
    /** Return true if the server is stopped. If the server was started from within
     * the IDE, determin the server state from the process exit code, otherwise try
     * to ping it. */
    private boolean isStopped() {
        Process proc = tm.getTomcatProcess();
        if (proc != null) {
            try {
                proc.exitValue();
                // process is stopped
                return true;
            } catch (IllegalThreadStateException e) { 
                // process is still running
                return false;
            }
        } else {
            int timeout = tm.getTomcatProperties().getRunningCheckTimeout();
            return !Utils.pingTomcat(tm.getServerPort(), timeout, tm.getServerHeader(), tm.getPlainUri());
        }
    }
    
    /** This implementation does nothing.
     * Target is already started when Tomcat starts.
     */
    public ProgressObject startServer (Target target) {
        return null;
    }
    
    @Override
    public boolean supportsStartDebugging(Target target) {
        return true;
    }

    @Override
    public ClientConfiguration getClientConfiguration (TargetModuleID targetModuleID) {
        return null;
    }
    
    @Override
    public DeploymentStatus getDeploymentStatus () {
        return pes.getDeploymentStatus ();
    }
    
    @Override
    public TargetModuleID[] getResultTargetModuleIDs () {
        return new TargetModuleID [] {};
    }
    
    @Override
    public boolean isCancelSupported () {
        return false;
    }
    
    @Override
    public void cancel () 
    throws OperationUnsupportedException {
        throw new OperationUnsupportedException ("");
    }
    
    @Override
    public boolean isStopSupported () {
        return false;
    }
    
    @Override
    public void stop () 
    throws OperationUnsupportedException {
        throw new OperationUnsupportedException ("");
    }
    
    @Override
    public void addProgressListener (ProgressListener pl) {
        pes.addProgressListener (pl);
    }
    
    @Override
    public void removeProgressListener (ProgressListener pl) {
        pes.removeProgressListener (pl);
    }
    
    
    @Override
    public String toString () {
        return "StartTomcat [" + tm + "]"; // NOI18N
    }
    
    public int getCurrentServerPort() {
        return currentServerPort;
    }
    
    // private helper methods -------------------------------------------------    
    
    private static NbProcessDescriptor defaultExecDesc(String command, String argCommand, String option) {
        return new NbProcessDescriptor (
            "{" + command + "}",  // NOI18N
            "{" + argCommand + "}" + " {" + option + "}",  // NOI18N
            NbBundle.getMessage (StartTomcat.class, "MSG_TomcatExecutionCommand")
        );
    }
    
    private static NbProcessDescriptor defaultExecDesc(String command, String argCommand) {
        return new NbProcessDescriptor (
            "{" + command + "}",     // NOI18N
            "{" + argCommand + "}",  // NOI18N
            NbBundle.getMessage (StartTomcat.class, "MSG_TomcatExecutionCommand")
        );
    }

    private static NbProcessDescriptor defaultDebugStartDesc(String command, String jpdaCommand, String option) {
        return new NbProcessDescriptor (
            "{" + command + "}",  // NOI18N
            "{" + TAG_JPDA + "}" + " {" + jpdaCommand + "}" + " {" + option + "}",  // NOI18N
            NbBundle.getMessage (StartTomcat.class, "MSG_TomcatExecutionCommand")
        );
    }
    
    private static NbProcessDescriptor defaultDebugStartDesc(String command, String jpdaCommand) {
        return new NbProcessDescriptor (
            "{" + command + "}",  // NOI18N
            "{" + TAG_JPDA + "}" + " {" + jpdaCommand + "}",  // NOI18N
            NbBundle.getMessage (StartTomcat.class, "MSG_TomcatExecutionCommand")
        );
    }
    
    private String getJavaHome(JavaPlatform platform) {
        FileObject fo = (FileObject)platform.getInstallFolders().iterator().next();
        return FileUtil.toFile(fo).getAbsolutePath();
    }
    
    /** Return the catalina startup script file. */
    private File getStartupScript() {
        TomcatProperties tp = tm.getTomcatProperties();
        if (tp.getCustomScript()) {
            return new File(tp.getScriptPath());
        }
        String startupScript = Utilities.isWindows() ? CATALINA_BAT : CATALINA_SH;
        return new File(tp.getCatalinaHome(), "/bin/" + startupScript); // NOI18N
    }
    
    private JavaPlatform getJavaPlatform() {
        JavaPlatform platform = tm.getTomcatProperties().getJavaPlatform();
        if (platform.getInstallFolders().size() <= 0) {
            LOGGER.log(Level.INFO, "The Java Platform used by Tomcat is broken; using the default one");
            return JavaPlatform.getDefault();
        }
        return platform;
    }

    private boolean isJavaOptsEscaped() {
        if (Utilities.isUnix()) {
            return false;
        }

        FileObject start = FileUtil.toFileObject(FileUtil.normalizeFile(getStartupScript()));
        if (start == null) {
            return false;
        }

        synchronized (this) {
            if (lastCheckedStart != null && !start.lastModified().after(lastCheckedStart)) {
                return javaOptsEscaped;
            }

            javaOptsEscaped = false;
            lastCheckedStart = start.lastModified();
            try {
                for (String line : start.asLines("UTF-8")) { // NOI18N
                    if (WINDOWS_ESCAPED_JAVA_OPTS.matcher(line).matches()) {
                        javaOptsEscaped = true;
                        break;
                    }
                }
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }

            return javaOptsEscaped;
        }
    }

    /** enable/disable ${catalina.home}/common/endorsed/*.jar in the catalina class
     loader in the catalina.properties file */
    private void patchCatalinaProperties(File catalinaBase, final boolean endorsedEnabled) {
        File catalinaProp = new File(catalinaBase, "conf/catalina.properties"); // NOI18N
        if (!catalinaProp.exists()) {
            return; // catalina.properties does not exist, can't do anything
        }
        EditableProperties props = new EditableProperties(false);
        try (InputStream is = new BufferedInputStream(new FileInputStream(catalinaProp))) {
            props.load(is);
            String COMMON_LOADER = "common.loader"; // NOI18N
            String commonLoader = props.getProperty(COMMON_LOADER);
            if (commonLoader != null) {
                String COMMON_ENDORSED = "${catalina.home}/common/endorsed/*.jar"; // NOI18N 
                int idx = commonLoader.indexOf(COMMON_ENDORSED);
                if (endorsedEnabled) {
                    if (idx == -1) { // common/endorsed/*.jar is not present, add it
                        String COMMON_LIB = "${catalina.home}/" + tm.libFolder() + "/*.jar"; // NOI18N
                        int commonLibIdx = commonLoader.indexOf(COMMON_LIB);
                        StringBuilder sb = new StringBuilder(commonLibIdx == -1 
                                ? commonLoader 
                                : commonLoader.substring(0, commonLibIdx));
                        if (commonLibIdx != -1) {
                            sb.append(COMMON_ENDORSED).append(',').append(commonLoader.substring(commonLibIdx));
                        } else {
                            if (commonLoader.trim().length() != 0) {
                                sb.append(',');
                            }
                            sb.append(COMMON_ENDORSED);
                        }
                        props.setProperty(COMMON_LOADER, sb.toString());
                    } else {
                        return;
                    }
                } else {
                    if (idx != -1) { // common/endorsed/*.jar is present, remove it
                        String strBefore = commonLoader.substring(0, idx);
                        int commaIdx = strBefore.lastIndexOf(",");
                        StringBuilder sb = new StringBuilder(commonLoader.substring(0, commaIdx == -1 ? idx : commaIdx));
                        String strAfter = commonLoader.substring(idx + COMMON_ENDORSED.length());
                        if (commaIdx == -1) {
                            // we have to cut off the trailing comman after the endorsed lib
                            int trailingCommaIdx = strAfter.indexOf(",");
                            if (trailingCommaIdx != -1) {
                                strAfter = strAfter.substring(trailingCommaIdx + 1);
                            }
                        }
                        sb.append(strAfter);
                        props.setProperty(COMMON_LOADER, sb.toString());
                    } else {
                        return;
                    }
                }
            }
            // store changes
            try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(catalinaProp))) {
                props.store(out);
            }
        } catch (FileNotFoundException fnfe) {
            LOGGER.log(Level.INFO, null, fnfe);
        } catch (IOException ioe) {
            LOGGER.log(Level.INFO, null, ioe);
        }
    }
    
    /** Utility class that just "consumes" the input stream - #58554 workaround
     */
    private static class StreamConsumer implements Runnable {
        
        private BufferedInputStream in;
        
        public StreamConsumer(InputStream is) {
            in = new BufferedInputStream(is);
        }

        @Override
        public void run() {
            try {
                byte buffer[] = new byte[1024];
                while (true) {
                    int n = in.read(buffer);
                    if (n < 0) {
                        break;
                    }
                    LOGGER.log(Level.FINE, new String(buffer, 0, n));
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.FINE, null, ioe);
            } finally {
                try { in.close(); } catch (IOException ioe) {}
            }
        }
    };
    
    /** Format that provides value usefull for Tomcat execution. 
     * Currently this is only the name of startup wrapper.
    */
    private static class TomcatFormat extends org.openide.util.MapFormat {
        
        private static final long serialVersionUID = 992972967554321415L;
        
        public TomcatFormat(File startupScript, File homeDir) {
            super(new HashMap<String, Object>());
            Map<String, String> map = getMap();
            String scriptPath = startupScript.getAbsolutePath();
            map.put(TAG_EXEC_CMD,       scriptPath);
            map.put(TAG_EXEC_STARTUP,   "run");         // NOI18N
            map.put(TAG_EXEC_SHUTDOWN,  "stop");        // NOI18N
            map.put(TAG_DEBUG_CMD,      scriptPath);
            map.put(TAG_JPDA,           "jpda");        // NOI18N
            map.put(TAG_JPDA_STARTUP,   "run");         // NOI18N
            map.put(TAG_SECURITY_OPT,   "-security");   // NOI18N
            map.put(TAG_FORCE_OPT,      "-force");      // NOI18N
            map.put(TAG_CATALINA_HOME,  homeDir.getAbsolutePath());
        }
    }
}
