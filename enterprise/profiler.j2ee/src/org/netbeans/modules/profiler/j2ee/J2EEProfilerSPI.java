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

package org.netbeans.modules.profiler.j2ee;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.lib.profiler.common.AttachSettings;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.common.SessionSettings;
import org.netbeans.lib.profiler.common.event.ProfilingStateAdapter;
import org.netbeans.lib.profiler.common.event.ProfilingStateEvent;
import org.netbeans.lib.profiler.common.integration.IntegrationUtils;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.profiler.api.*;
import org.netbeans.modules.profiler.NetBeansProfiler;
import org.netbeans.modules.profiler.nbimpl.project.ProjectUtilities;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.ProfilerStorage;
import org.netbeans.modules.profiler.api.project.ProjectProfilingSupport;
import org.netbeans.modules.profiler.utilities.ProfilerUtils;
import org.openide.DialogDisplayer;


/**
 * Implementation of org.netbeans.modules.j2ee.deployment.profiler.spi.Profiler
 *
 * @author Tomas Hurka
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "J2EEProfilerSPI_FailedDeterminePlatformMsg=Failed to determine version of Java platform: {0}",
    "J2EEProfilerSPI_DirectAttachMsg=\nThe server will be started for Direct Attach with these settings:   \n\n    Java Platform: {0}\n    Agent port: {1}\n\nClick \"OK\" to start the server.",
    "J2EEProfilerSPI_FailedLoadSettingsMsg=Failed to load attach settings for main project: {0}",
    "J2EEProfilerSPI_StoppingServerMsg=Stopping profiled server",
    "J2EEProfilerSPI_StoppedServerMsg=Profiled server stopped",
    "J2EEProfilerSPI_StoppingServerFailedMsg=Stopping profiled server failed"
})
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.j2ee.deployment.profiler.spi.Profiler.class)
public class J2EEProfilerSPI implements org.netbeans.modules.j2ee.deployment.profiler.spi.Profiler, ProgressObject {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private class StopAgentStatus implements DeploymentStatus {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private StateType state;
        private String message;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public StopAgentStatus(String msg, StateType stype) {
            int lastEx;

            if ((lastEx = msg.lastIndexOf("Exception:")) > 0) { //NOI18N
                message = msg.substring(lastEx + "Exception:".length()); //NOI18N
            } else {
                message = msg;
            }

            state = stype;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public ActionType getAction() {
            return ActionType.EXECUTE;
        }

        public CommandType getCommand() {
            return CommandType.STOP;
        }

        public boolean isCompleted() {
            return state == StateType.COMPLETED;
        }

        public boolean isFailed() {
            return state == StateType.FAILED;
        }

        public String getMessage() {
            return message;
        }

        public boolean isRunning() {
            return state == StateType.RUNNING;
        }

        public StateType getState() {
            return state;
        }
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private DeploymentStatus stopAgentStatus;
    private InstanceProperties lastServerInstanceProperties = null;

    // --- ProgressObject implementation -----------------------------------------
    private Collection<ProgressListener> listeners = new CopyOnWriteArraySet<ProgressListener>();
    private boolean profilerAgentShutdownProgress = false;
    private boolean profilerAgentStarting = false;
    private boolean refreshServerInstance = false;
    private boolean serverStartedFromIDE = false;
    private /*static final*/ int STARTING_STATE_TIMEOUT = 20000; // timeout for starting the agent [ms]
    private long profilerAgentStartingTime = -1;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public J2EEProfilerSPI() {
        String startingStateTimeout = System.getProperty("profiler.agent.startup.timeout"); // NOI18N

        if ((startingStateTimeout != null) && !"".equals(startingStateTimeout)) { // NOI18N

            try {
                int timeout = Integer.parseInt(startingStateTimeout);
                STARTING_STATE_TIMEOUT = timeout;
                ProfilerLogger.log(">>> Profiler agent startup timeout redefined to " + STARTING_STATE_TIMEOUT + " ms"); // NOI18N
            } catch (Exception ex) {
            }

        }

        String refreshServerInstanceProperty = System.getProperty("profiler.serverstate.refresh"); // NOI18N

        if ((refreshServerInstanceProperty != null) && "true".equals(refreshServerInstanceProperty)) { // NOI18N
            refreshServerInstance = true;
            ProfilerLogger.log(">>> Profiler will update server instance after attaching to it and after stopping it."); // NOI18N
        }

        fireShutdownStartedEvent();

        if (refreshServerInstance) {
            Profiler.getDefault().addProfilingStateListener(new ProfilingStateAdapter() {
                public void profilingStateChanged(ProfilingStateEvent e) {
                    // Profiling started
                    if (e.getNewState() == Profiler.PROFILING_RUNNING) {
                        if (getState() == ProfilerSupport.STATE_PROFILING) { // Profiler SPI is used for profiling, ServerInstance will be refreshed after profiling ends

                            if (refreshServerInstance && (lastServerInstanceProperties != null)
                                    && (Profiler.getDefault().getProfilingMode() == Profiler.MODE_ATTACH)) {
                                lastServerInstanceProperties.refreshServerInstance(); // Attaching to server started from Runtime tab, server state refresh is required
                            }

                            serverStartedFromIDE = true;
                        }
                    }

                    // Profiling finished
                    if (e.getNewState() == Profiler.PROFILING_INACTIVE) {
                        if (refreshServerInstance && serverStartedFromIDE && (lastServerInstanceProperties != null)) {
                            lastServerInstanceProperties.refreshServerInstance();
                        }

                        lastServerInstanceProperties = null;
                        serverStartedFromIDE = false;
                    }
                }
            });
        }
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public boolean isCancelSupported() {
        return false;
    }

    public ClientConfiguration getClientConfiguration(TargetModuleID id) {
        return null;
    }

    public DeploymentStatus getDeploymentStatus() {
        return stopAgentStatus;
    }

    // --- Extension for J2EEProjectTypeProfiler to detect if a server was started from the IDE
    public boolean isProfiledServerStartedFromIDE() {
        return serverStartedFromIDE;
    }

    public TargetModuleID[] getResultTargetModuleIDs() {
        return null;
    }

    /**
     * This method is a shortcut to call {@linkplain J2EEProfilerSPI#getSettings(java.lang.String, boolean) }
     * in verbose mode
     *
     * @param   serverInstanceID ID of the server instance that is going to be started
     *
     * @return  required settings or <code>null</code> if user cancelled starting
     *          the server.
     */
    public ProfilerServerSettings getSettings(String serverInstanceID) {
        return getSettings(serverInstanceID, true);
    }

    /**
     * This method is used from the Runtime tab to obtain settings for starting
     * the server. It displays dialog and let the user choose required mode
     * (direct/dynamic attach) and other settings for the server startup.
     *
     * @param   serverInstanceID ID of the server instance that is going to be started
     * @param   verbose Whether to show the informational dialog
     *
     * @return  required settings or <code>null</code> if user cancelled starting
     *          the server.
     */
    public ProfilerServerSettings getSettings(String serverInstanceID, boolean verbose) {
        // obtain agent ID
        int agentID = J2EEProjectProfilingSupportProvider.generateAgentID();

        // obtain agent port
        int agentPort = J2EEProjectProfilingSupportProvider.generateAgentPort();

        // obtain Java platform
        org.netbeans.modules.profiler.api.JavaPlatform agentJavaPlatform = J2EEProjectProfilingSupportProvider.generateAgentJavaPlatform(serverInstanceID);

        if (agentJavaPlatform == null) {
            lastServerInstanceProperties = null;

            return null; // cancelled by the user
        }

        String javaVersion = agentJavaPlatform.getPlatformJDKVersion();

        if (javaVersion == null) {
            ProfilerDialogs.displayError(Bundle.J2EEProfilerSPI_FailedDeterminePlatformMsg(agentJavaPlatform.getDisplayName()));
            lastServerInstanceProperties = null;

            return null;
        }

        String localPlatform = IntegrationUtils.getLocalPlatform(agentJavaPlatform.getPlatformArchitecture());

        // init JVM arguments
        String[] jvmArgs = new String[2];

        // init environment variables to be set
        String[] env = new String[0];

        // populate jvmArgs and env
        jvmArgs[1] = "-Dnbprofiler.agentid=" + agentID; // NOI18N

        if (javaVersion.equals(CommonConstants.JDK_15_STRING)) {
            // JDK 1.5 used
            //jvmArgs[0] = IDEUtils.getAntProfilerStartArgument15(agentPort); // incorrect brackets when spaces are in path
            jvmArgs[0] = IntegrationUtils.getProfilerAgentCommandLineArgs(localPlatform, IntegrationUtils.PLATFORM_JAVA_50,
                                                                          false, agentPort);
        } else {
            // JDK 1.6 or later used
            //jvmArgs[0] = IDEUtils.getAntProfilerStartArgument16(agentPort); // incorrect brackets when spaces are in path
            jvmArgs[0] = IntegrationUtils.getProfilerAgentCommandLineArgs(localPlatform, IntegrationUtils.PLATFORM_JAVA_60,
                                                                          false, agentPort);
        }
        
        ProfilerServerSettings profilerServerSettings = new ProfilerServerSettings(convert(agentJavaPlatform), jvmArgs, env);

        if (verbose && DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(Bundle.J2EEProfilerSPI_DirectAttachMsg(
                                                                                                  agentJavaPlatform.getDisplayName(),
                                                                                                  "" + agentPort // NOI18N
                                                                                              ), 
                                                                         NotifyDescriptor.OK_CANCEL_OPTION,
                                                                         NotifyDescriptor.INFORMATION_MESSAGE)) != NotifyDescriptor.OK_OPTION) {
            lastServerInstanceProperties = null;

            return null; // cancelled by the user
        }

        Project mainProject = ProjectUtilities.getMainProject();

        if (ProjectUtilities.isJavaProject(mainProject)) {
            Properties p = new Properties();
            try {
                ProfilerStorage.loadProjectProperties(p, mainProject, "attach"); // NOI18N
            } catch (IOException e) {
                ProfilerDialogs.displayWarning(Bundle.J2EEProfilerSPI_FailedLoadSettingsMsg(e.getMessage()));
                ProfilerLogger.log(e);
            }

            AttachSettings attachSettings = new AttachSettings();
            if (p.isEmpty()) {
                attachSettings.setRemote(false);
                attachSettings.setDirect(true);
                attachSettings.store(p);
                try {
                    ProfilerStorage.saveProjectProperties(p, mainProject, "attach"); // NOI18N
                } catch (IOException e) {
                    ProfilerLogger.log(e);
                }
            } else {
                attachSettings.load(p);
            }
        }

        ProfilerLogger.log(">>> Generated settings for server startup - direct attach (blocking):"); // NOI18N
        ProfilerLogger.log(profilerServerSettings.toString());

        lastServerInstanceProperties = InstanceProperties.getInstanceProperties(serverInstanceID);

        return profilerServerSettings;
    }

    /**
     * Returns state of Profiler agent instance started from the IDE. It detects
     * possible response from an unknown (not started from the IDE) Profiler
     * agent, in this case it returns STATE_INACTIVE.
     *
     * @return state of Profiler agent instance.
     */
    public synchronized int getState() {
        // #231271 - commented out, meant for debugging only
//        int profilerAgentID = J2EEProjectProfilingSupportProvider.getLastAgentID();
//        int profilerAgentPort = J2EEProjectProfilingSupportProvider.getLastAgentPort();
//        int agentState = checkState();
//        ProfilerLogger.log(">>> Profiler agent [port=" + profilerAgentPort + ", id=" + profilerAgentID + "]: "
//                           + getPublicAgentStateString(agentState)); // NOI18N
//
//        return agentState;

        return checkState();
    }

    public boolean isStopSupported() {
        return false;
    }

    public synchronized void addProgressListener(ProgressListener listener) {
        boolean notify = false;

        listeners.add(listener);

        if ((stopAgentStatus != null) && !stopAgentStatus.isRunning()) {
            notify = true;
        }

        if (notify) {
            ProfilerUtils.runInProfilerRequestProcessor(new Runnable() {
                    public void run() {
                        fireHandleProgressEvent(stopAgentStatus);
                    }
                });

        }
    }

    /**
     * This method is used from the <code>nbstartprofiledserver</code>
     * task to connect the Profiler to a server ready for profiling.
     *
     * @param profilerProperties list of profiler properties defined in the project.
     *
     * @return <code>true</code> if the Profiler successfully attached to the server.
     */
    public boolean attachProfiler(Map antProjectProperties) {
        return performProfilerAttach(antProjectProperties);
    }

    public void cancel() {
    }

    public void fireShutdownCompletedEvent() {
        fireHandleProgressEvent(new StopAgentStatus(Bundle.J2EEProfilerSPI_StoppedServerMsg(), StateType.COMPLETED));
    }

    public void fireShutdownFailedEvent() {
        fireHandleProgressEvent(new StopAgentStatus(Bundle.J2EEProfilerSPI_StoppingServerFailedMsg(), StateType.FAILED));
    }

    public void fireShutdownStartedEvent() {
        fireHandleProgressEvent(new StopAgentStatus(Bundle.J2EEProfilerSPI_StoppingServerMsg(), StateType.RUNNING));
    }

    // --- Profiler SPI interface implementation ---------------------------------

    /**
     * Inform the profiler that some server is starting in the profile mode. It
     * allows the Profiler to correctly detect STATE_STARTING.
     */
    public void notifyStarting() {
        int profilerAgentPort = J2EEProjectProfilingSupportProvider.getLastAgentPort();
        NetBeansProfiler.getDefaultNB().cleanForProfilingOnPort(profilerAgentPort); // try to kill an agent on port if some exists

        profilerAgentStartingTime = System.currentTimeMillis();
        profilerAgentStarting = true;
    }
    
    public synchronized void removeProgressListener(ProgressListener listener) {
        listeners.remove(listener);
    }

    /**
     * Stops the Profiler agent if in STATE_BLOCKING state. Otherwise does nothing,
     * letting the server to be stopped normally via the server plugin.
     *
     * @return object used to monitor progress of shutdown.
     */
    public ProgressObject shutdown() {
        // shutdown is already in progress, do nothing
        if (profilerAgentShutdownProgress) {
            return this;
        }

        // set the shutdown progress flag
        profilerAgentShutdownProgress = true;

        // run all the shutdown stuff in separate thread
        Runnable task = new Runnable() {
            public void run() {
                try {
                    // notify listeners that shutdown has begun
                    fireShutdownStartedEvent();

                    // if the agent is currently starting or failed to start, wait for starting the agent or agent startup timeout
                    if (getState() == ProfilerSupport.STATE_STARTING) {
                        while (getState() == ProfilerSupport.STATE_STARTING) {
                            try {
                                Thread.sleep(500);
                            } catch (Exception ex) {
                            } // will always timeout if agent is not running
                        }
                    }

                    // we only stop the agent if in STATE_BLOCKING state
                    if (getState() == ProfilerSupport.STATE_BLOCKING) {
                        int profilerAgentID = J2EEProjectProfilingSupportProvider.getLastAgentID();
                        int profilerAgentPort = J2EEProjectProfilingSupportProvider.getLastAgentPort();
                        Profiler.getDefault().shutdownBlockedAgent("localhost", profilerAgentPort, profilerAgentID); // NOI18N

                        for (int i = 0; i < 60; i++) { // 30sec timeout on profiled application shutdown (thread sleeps 500ms)

                            if (getState() == ProfilerSupport.STATE_INACTIVE) {
                                // notify listeners that agent has been stopped
                                fireShutdownCompletedEvent();

                                return;
                            }

                            try {
                                Thread.sleep(500);
                            } catch (Exception ex) {
                            }
                        }

                        // notify listeners that agent shutdown failed
                        fireShutdownFailedEvent();
                    } else {
                        // notify listeners that shutdown has finished - actually no action has been taken
                        fireShutdownCompletedEvent();
                    }
                } finally {
                    // reset the shutdown progress flag
                    profilerAgentShutdownProgress = false;
                    J2EEProjectProfilingSupportProvider.resetLastValues();
                }
            }
        };

        ProfilerUtils.runInProfilerRequestProcessor(task);

        // return (ProgressObject)this
        return this;
    }

    public void stop() {
    }

    // Agent state as obtained by NetBeansProfiler.getAgentState()
    private String getInternalAgentStateString(int agentState) {
        if (agentState == CommonConstants.AGENT_STATE_NOT_RUNNING) {
            return "AGENT_STATE_NOT_RUNNING"; // NOI18N
        }

        if (agentState == CommonConstants.AGENT_STATE_READY_DYNAMIC) {
            return "AGENT_STATE_READY_DYNAMIC"; // NOI18N
        }

        if (agentState == CommonConstants.AGENT_STATE_READY_DIRECT) {
            return "AGENT_STATE_READY_DIRECT"; // NOI18N
        }

        if (agentState == CommonConstants.AGENT_STATE_CONNECTED) {
            return "AGENT_STATE_CONNECTED"; // NOI18N
        }

        if (agentState == CommonConstants.AGENT_STATE_DIFFERENT_ID) {
            return "AGENT_STATE_DIFFERENT_ID"; // NOI18N
        }

        if (agentState == CommonConstants.AGENT_STATE_OTHER_SESSION_IN_PROGRESS) {
            return "AGENT_STATE_OTHER_SESSION_IN_PROGRESS"; // NOI18N
        }

        return "UNKNOWN AGENT STATE"; // NOI18N
    }

    // Profiler state
    private String getProfilingStateString(int profilingState) {
        if (profilingState == Profiler.PROFILING_INACTIVE) {
            return "PROFILING_INACTIVE"; // NOI18N
        }

        if (profilingState == Profiler.PROFILING_STARTED) {
            return "PROFILING_STARTED"; // NOI18N
        }

        if (profilingState == Profiler.PROFILING_RUNNING) {
            return "PROFILING_RUNNING"; // NOI18N
        }

        if (profilingState == Profiler.PROFILING_PAUSED) {
            return "PROFILING_PAUSED"; // NOI18N
        }

        if (profilingState == Profiler.PROFILING_STOPPED) {
            return "PROFILING_STOPPED"; // NOI18N
        }

        return "UNKNOWN PROFILER STATE"; // NOI18N
    }

    // Agent state as obtained by Profiler.getState()
    private String getPublicAgentStateString(int agentState) {
        if (agentState == ProfilerSupport.STATE_INACTIVE) {
            return "STATE_INACTIVE"; // NOI18N
        }

        if (agentState == ProfilerSupport.STATE_STARTING) {
            return "STATE_STARTING"; // NOI18N
        }

        if (agentState == ProfilerSupport.STATE_BLOCKING) {
            return "STATE_BLOCKING"; // NOI18N
        }

        if (agentState == ProfilerSupport.STATE_RUNNING) {
            return "STATE_RUNNING"; // NOI18N
        }

        if (agentState == ProfilerSupport.STATE_PROFILING) {
            return "STATE_PROFILING"; // NOI18N
        }

        return "UNKNOWN AGENT STATE"; // NOI18N
    }

    // --- Private implementation ------------------------------------------------
    private int checkState() {
        int profilerAgentID = J2EEProjectProfilingSupportProvider.getLastAgentID();
        int profilerAgentPort = J2EEProjectProfilingSupportProvider.getLastAgentPort();
        int currentAgentState = NetBeansProfiler.getDefault().getAgentState("localhost", profilerAgentPort, profilerAgentID); // NOI18N
                                                                                                                              //System.err.println(">>> Detected internal agent state: " + getInternalAgentStateString(currentAgentState));

        // Should not happed, doesn't give much sense...
        if (currentAgentState == CommonConstants.AGENT_STATE_OTHER_SESSION_IN_PROGRESS) {
            profilerAgentStarting = false;

            return ProfilerSupport.STATE_INACTIVE;
        }

        // Other than expected agent is running on the port
        if (currentAgentState == CommonConstants.AGENT_STATE_DIFFERENT_ID) {
            profilerAgentStarting = false;

            return ProfilerSupport.STATE_INACTIVE;
        }

        // Agent is not running, may be starting or inactive
        if (currentAgentState == CommonConstants.AGENT_STATE_NOT_RUNNING) {
            if (profilerAgentStarting) {
                if (System.currentTimeMillis() > (profilerAgentStartingTime + STARTING_STATE_TIMEOUT)) {
                    profilerAgentStarting = false;

                    return ProfilerSupport.STATE_INACTIVE;
                } else {
                    return ProfilerSupport.STATE_STARTING;
                }
            } else {
                return ProfilerSupport.STATE_INACTIVE;
            }
        }

        // Agent is ready for direct attach
        if (currentAgentState == CommonConstants.AGENT_STATE_READY_DIRECT) {
            profilerAgentStarting = false;

            return ProfilerSupport.STATE_BLOCKING;
        }

        // Agent is ready for dynamic attach
        if (currentAgentState == CommonConstants.AGENT_STATE_READY_DYNAMIC) {
            profilerAgentStarting = false;

            return ProfilerSupport.STATE_RUNNING;
        }

        // Agent is running and profiling session is in progress
        if (currentAgentState == CommonConstants.AGENT_STATE_CONNECTED) {
            profilerAgentStarting = false;

            return ProfilerSupport.STATE_PROFILING;
        }

        // Default response, actually the program flow won't get here
        return ProfilerSupport.STATE_INACTIVE;
    }

    private void fireHandleProgressEvent(DeploymentStatus status) {
        ProgressEvent evt = new ProgressEvent(this, null, status);
        stopAgentStatus = status;

        for (ProgressListener target : listeners) {
            target.handleProgressEvent(evt);
        }
    }

    private boolean performProfilerAttach(Map props) {
        org.netbeans.api.project.Project profiledProject = null;

        String projectDir = (String) props.get("profiler.info.project.dir"); // NOI18N

        if (projectDir != null) {
            FileObject projectFO = FileUtil.toFileObject(FileUtil.normalizeFile(new File(projectDir)));

            if (projectFO != null) {
                try {
                    profiledProject = ProjectManager.getDefault().findProject(projectFO);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    lastServerInstanceProperties = null;

                    return false;
                }
            }
        }

        final org.netbeans.api.project.Project projectToUse = profiledProject;
        final ProfilingSettings ps = new ProfilingSettings();
        final SessionSettings ss = new SessionSettings();

        ps.load(props);
        try {
            ss.load(props);
        } catch (IllegalArgumentException e) {
            ProfilerDialogs.displayWarning(e.getLocalizedMessage());
        }

        if (!ProjectProfilingSupport.get(projectToUse).isProfilingSupported()) { // unsupported project
            lastServerInstanceProperties = null;

            return false;
        }

        lastServerInstanceProperties = InstanceProperties.getInstanceProperties(J2EEProjectProfilingSupportProvider.getServerInstanceID(projectToUse));

        ((NetBeansProfiler) Profiler.getDefault()).setProfiledProject(projectToUse, null);

        if (!Profiler.getDefault().connectToStartedApp(ps, ss)) {
            ProfilerLogger.severe("Error connecting to started app"); // NOI18N
        }

        return true;
    }

    private JavaPlatform convert(org.netbeans.modules.profiler.api.JavaPlatform agentJavaPlatform) {
        String platformName = agentJavaPlatform.getPlatformId();
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms(null, new Specification("j2se", null)); // NOI18N

        for (int i = 0; i < platforms.length; i++) {
            JavaPlatform platform = platforms[i];
            String antName = (String) platform.getProperties().get("platform.ant.name"); // NOI18N

            if (antName.equals(platformName)) {
                return platform;
            }
        }
        throw new IllegalArgumentException("Platform "+platformName+" not found"); // NOI18N        
    }
}
