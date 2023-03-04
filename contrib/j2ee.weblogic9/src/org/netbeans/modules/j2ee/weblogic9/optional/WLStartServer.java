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

package org.netbeans.modules.j2ee.weblogic9.optional;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.api.extexecution.base.BaseExecutionDescriptor;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.modules.j2ee.deployment.plugins.api.CommonServerBridge;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerSupport;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.weblogic.common.api.RuntimeListener;
import org.netbeans.modules.weblogic.common.api.WebLogicRuntime;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public final class WLStartServer extends StartServer {

    private static final String JAVA_VENDOR_VARIABLE = "JAVA_VENDOR"; // NOI18N

    private static final String JAVA_OPTIONS_VARIABLE = "JAVA_OPTIONS"; // NOI18N

    private static final String MEMORY_OPTIONS_VARIABLE= "USER_MEM_ARGS"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(WLStartServer.class.getName());

    /* GuardedBy(WLStartServer.class) */
    private static Set<String> SERVERS_IN_DEBUG;

    private final WLDeploymentManager dm;

    public WLStartServer(WLDeploymentManager dm) {
        this.dm = dm;
    }

    @Override
    public ServerDebugInfo getDebugInfo(Target target) {
        return new ServerDebugInfo(dm.getHost(), Integer.valueOf(
                dm.getInstanceProperties().getProperty(
                WLPluginProperties.DEBUGGER_PORT_ATTR)));
    }

    @Override
    public boolean isAlsoTargetServer(Target target) {
        return true;
    }

    @Override
    public boolean isDebuggable(Target target) {
        if (!dm.isRemote() && !isServerInDebug(dm.getUri())) {
            return false;
        }
        if (!isRunning()) {
            return false;
        }
        // XXX
        return !dm.isRemote()
                || Boolean.valueOf(dm.getInstanceProperties().getProperty(WLPluginProperties.REMOTE_DEBUG_ENABLED));
    }

    @Override
    public boolean isRunning() {
        WebLogicRuntime runtime = WebLogicRuntime.getInstance(dm.getCommonConfiguration());
        return runtime.isRunning();
    }

    @Override
    public boolean needsStartForAdminConfig() {
        return true;
    }

    @Override
    public boolean needsStartForConfigure() {
        return false;
    }

    @Override
    public boolean needsStartForTargetList() {
        return true;
    }

    @Override
    public ProgressObject startDebugging(Target target) {
        LOGGER.log(Level.FINER, "Starting server in debug mode"); // NOI18N

        WLServerProgress serverProgress = new WLServerProgress(this);
        String serverName = dm.getInstanceProperties().getProperty(
                InstanceProperties.DISPLAY_NAME_ATTR);
        String uri = dm.getUri();

        WebLogicRuntime runtime = WebLogicRuntime.getInstance(dm.getCommonConfiguration());
        runtime.start(new DefaultInputProcessorFactory(uri, false), new DefaultInputProcessorFactory(uri, true),
                new StartListener(dm, serverName, serverProgress), getStartDebugVariables(dm), null);

        addServerInDebug(uri);
        return serverProgress;
    }

    @Override
    public ProgressObject startDeploymentManager() {
        LOGGER.log(Level.FINER, "Starting server"); // NOI18N

        WLServerProgress serverProgress = new WLServerProgress(this);
        String serverName = dm.getInstanceProperties().getProperty(
                InstanceProperties.DISPLAY_NAME_ATTR);
        String uri = dm.getUri();

        WebLogicRuntime runtime = WebLogicRuntime.getInstance(dm.getCommonConfiguration());
        runtime.start(new DefaultInputProcessorFactory(uri, false), new DefaultInputProcessorFactory(uri, true),
                new StartListener(dm, serverName, serverProgress), getStartVariables(dm), null);

        removeServerInDebug(uri);
        return serverProgress;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer#startProfiling(javax.enterprise.deploy.spi.Target, org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerServerSettings)
     */
    @Override
    public ProgressObject startProfiling(Target target) {
        LOGGER.log(Level.FINER, "Starting server in profiling mode"); // NOI18N

        final WLServerProgress serverProgress = new WLServerProgress(this);
        final String serverName = dm.getInstanceProperties().getProperty(
                InstanceProperties.DISPLAY_NAME_ATTR);

        String uri = dm.getUri();

        final WebLogicRuntime runtime = WebLogicRuntime.getInstance(dm.getCommonConfiguration());
        runtime.start(new DefaultInputProcessorFactory(uri, false), new DefaultInputProcessorFactory(uri, true),
                new StartListener(dm, serverName, serverProgress) {

            @Override
            public void onExit() {
                int state = ProfilerSupport.getState();
                if (state == ProfilerSupport.STATE_INACTIVE) {
                    serverProgress.notifyStart(StateType.FAILED,
                            NbBundle.getMessage(WLStartServer.class,
                                    "MSG_START_PROFILED_SERVER_FAILED", serverName));
                    runtime.kill();
                }
            }
        }, getStartProfileVariables(dm), new WebLogicRuntime.RunningCondition() {

            @Override
            public boolean isRunning() {
                int state = ProfilerSupport.getState();
                return state == ProfilerSupport.STATE_BLOCKING
                        || state == ProfilerSupport.STATE_RUNNING
                        || state == ProfilerSupport.STATE_PROFILING;
            }
        });

        removeServerInDebug(uri);
        return serverProgress;
    }

    @Override
    public ProgressObject stopDeploymentManager() {
        LOGGER.log(Level.FINER, "Stopping server"); // NOI18N

        WLServerProgress serverProgress = new WLServerProgress(this);
        String serverName = dm.getInstanceProperties().getProperty(
                InstanceProperties.DISPLAY_NAME_ATTR);
        String uri = dm.getUri();

        WebLogicRuntime runtime = WebLogicRuntime.getInstance(dm.getCommonConfiguration());
        runtime.stop(new DefaultInputProcessorFactory(uri, false), new DefaultInputProcessorFactory(uri, true),
                new StopListener(dm, serverName, serverProgress));

        removeServerInDebug(uri);
        return serverProgress;
    }

    @Override
    public boolean supportsStartDeploymentManager() {
        return !dm.isRemote();
    }

    @Override
    public boolean supportsStartProfiling( Target target ) {
        return !dm.isRemote();
    }

    @Override
    public boolean supportsStartDebugging(Target target) {
        //if we can start it we can debug it
        return supportsStartDeploymentManager();
    }

    @Override
    public boolean needsRestart(Target target) {
        return dm.isRestartNeeded();
    }

    private static synchronized void addServerInDebug(String uri) {
        if (SERVERS_IN_DEBUG == null) {
            SERVERS_IN_DEBUG = new HashSet<String>(1);
        }
        SERVERS_IN_DEBUG.add(uri);
    }

    private static synchronized void removeServerInDebug(String uri) {
        if (SERVERS_IN_DEBUG == null) {
            return;
        }
        SERVERS_IN_DEBUG.remove(uri);
    }

    private static synchronized boolean isServerInDebug(String uri) {
        return SERVERS_IN_DEBUG != null && SERVERS_IN_DEBUG.contains(uri);
    }

    private static Map<String, String> getStartVariables(WLDeploymentManager dm) {
        Map<String, String> ret = new HashMap<String, String>();

        String javaOpts = dm.getInstanceProperties().getProperty(WLPluginProperties.JAVA_OPTS);
        StringBuilder sb = new StringBuilder((javaOpts != null && javaOpts.trim().length() > 0)
                ? javaOpts.trim() : "");
        for (StartupExtender args : StartupExtender.getExtenders(
                Lookups.singleton(CommonServerBridge.getCommonInstance(dm.getUri())), StartupExtender.StartMode.NORMAL)) {
            for (String singleArg : args.getArguments()) {
                sb.append(' ').append(singleArg);
            }
        }

        configureProxy(dm.getInstanceProperties(), ret, sb);
        if (sb.length() > 0) {
            ret.put(JAVA_OPTIONS_VARIABLE, sb.toString());
        }

        String vendor = dm.getInstanceProperties().getProperty(WLPluginProperties.VENDOR);
        if (vendor != null && vendor.trim().length() > 0) {
            ret.put(JAVA_VENDOR_VARIABLE, vendor.trim());
        }
        String memoryOptions = dm.getInstanceProperties().getProperty(
                WLPluginProperties.MEM_OPTS);
        if (memoryOptions != null && memoryOptions.trim().length() > 0) {
            ret.put(MEMORY_OPTIONS_VARIABLE, memoryOptions.trim());
        }
        return ret;
    }

    private static Map<String, String> getStartDebugVariables(WLDeploymentManager dm) {
        Map<String, String> ret = new HashMap<String, String>();
        int debugPort = 4000;
        debugPort = Integer.parseInt(dm.getInstanceProperties().getProperty(
                WLPluginProperties.DEBUGGER_PORT_ATTR));

        StringBuilder javaOptsBuilder = new StringBuilder();
        String javaOpts = dm.getInstanceProperties().getProperty(
                WLPluginProperties.JAVA_OPTS);
        if (javaOpts != null && javaOpts.trim().length() > 0) {
            javaOptsBuilder.append(javaOpts.trim());
        }
        if (javaOptsBuilder.length() > 0) {
            javaOptsBuilder.append(" ");// NOI18N
        }
        javaOptsBuilder.append("-agentlib:jdwp=server=y,suspend=n,transport=dt_socket,address="); // NOI18N
        javaOptsBuilder.append(debugPort);
        for (StartupExtender args : StartupExtender.getExtenders(
                Lookups.singleton(CommonServerBridge.getCommonInstance(dm.getUri())), StartupExtender.StartMode.DEBUG)) {
            for (String singleArg : args.getArguments()) {
                javaOptsBuilder.append(' ').append(singleArg);
            }
        }

        configureProxy(dm.getInstanceProperties(), ret, javaOptsBuilder);
        if (javaOptsBuilder.length() > 0) {
            ret.put(JAVA_OPTIONS_VARIABLE, javaOptsBuilder.toString());
        }
        String memoryOptions = dm.getInstanceProperties().getProperty(
                WLPluginProperties.MEM_OPTS);
        if (memoryOptions != null && memoryOptions.trim().length() > 0) {
            ret.put(MEMORY_OPTIONS_VARIABLE, memoryOptions.trim());
        }
        return ret;
    }

    private static Map<String, String> getStartProfileVariables(WLDeploymentManager dm) {
        Map<String, String> ret = new HashMap<String, String>();
        StringBuilder javaOptsBuilder = new StringBuilder();
        String javaOpts = dm.getInstanceProperties().getProperty(
                WLPluginProperties.JAVA_OPTS);
        if (javaOpts != null && javaOpts.trim().length() > 0) {
            javaOptsBuilder.append(" ");                              // NOI18N
            javaOptsBuilder.append(javaOpts.trim());
        }

        for (StartupExtender args : StartupExtender.getExtenders(
                Lookups.singleton(CommonServerBridge.getCommonInstance(dm.getUri())), StartupExtender.StartMode.PROFILE)) {
            for (String singleArg : args.getArguments()) {
                javaOptsBuilder.append(' ').append(singleArg);
            }
        }

        configureProxy(dm.getInstanceProperties(), ret, javaOptsBuilder);
        String toAdd = javaOptsBuilder.toString().trim();
        if (!toAdd.isEmpty()) {
            ret.put(JAVA_OPTIONS_VARIABLE, toAdd);
        }
        return ret;
    }

    private static void configureProxy(InstanceProperties props, Map<String, String> env, StringBuilder javaOpts) {
        if (Boolean.valueOf(props.getProperty(WLPluginProperties.PROXY_ENABLED))) {
            configureProxy(javaOpts);
        } else {
            env.put("http_proxy", ""); // NOI18N
        }
    }

    private static StringBuilder configureProxy(StringBuilder sb) {
        final String[] PROXY_PROPS = {
            "http.proxyHost", // NOI18N
            "http.proxyPort", // NOI18N
            "https.proxyHost", // NOI18N
            "https.proxyPort", // NOI18N
        };
        for (String prop : PROXY_PROPS) {
            if (sb.indexOf(prop) < 0) {
                String value = System.getProperty(prop);
                if (value != null) {
                    if (sb.length() > 0) {
                        sb.append(' '); // NOI18N
                    }
                    sb.append(" -D").append(prop).append("=").append(value); // NOI18N
                }
            }
        }

        appendNonProxyHosts(sb);
        return sb;
    }

    private static StringBuilder appendNonProxyHosts(StringBuilder sb) {
        if (sb.indexOf(NonProxyHostsHelper.HTTP_NON_PROXY_HOSTS) < 0) { // NOI18N
            String nonProxyHosts = NonProxyHostsHelper.getNonProxyHosts();
            if (!nonProxyHosts.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(' '); // NOI18N
                }
                sb.append("-D"); // NOI18N
                sb.append(NonProxyHostsHelper.HTTP_NON_PROXY_HOSTS);
                sb.append("="); // NOI18N
                sb.append('"').append(nonProxyHosts).append('"'); // NOI18N
            }
        }
        return sb;
    }

    private static class DefaultInputProcessorFactory implements BaseExecutionDescriptor.InputProcessorFactory {

        private final String uri;

        private final boolean error;

        public DefaultInputProcessorFactory(String uri, boolean error) {
            this.uri = uri;
            this.error = error;
        }
        
        @Override
        public InputProcessor newInputProcessor() {
            InputOutput io = UISupport.getServerIO(uri);
            if (io == null) {
                return null;
            }

            return org.netbeans.api.extexecution.print.InputProcessors.printing(
                    error ? io.getErr() : io.getOut(), new ErrorLineConvertor(), true);
        }
    }

    private static class StartListener implements RuntimeListener {

        private final WLDeploymentManager dm;

        private final String serverName;

        private final WLServerProgress serverProgress;

        public StartListener(WLDeploymentManager dm, String serverName, WLServerProgress serverProgress) {
            this.dm = dm;
            this.serverName = serverName;
            this.serverProgress = serverProgress;
        }
        
        @Override
        public void onStart() {
            serverProgress.notifyStart(StateType.RUNNING,
                    NbBundle.getMessage(WLStartServer.class, "MSG_START_SERVER_IN_PROGRESS", serverName));
        }

        @Override
        public void onFinish() {
            // noop
        }

        @Override
        public void onFail() {
            serverProgress.notifyStart(StateType.FAILED,
                    NbBundle.getMessage(WLStartServer.class, "MSG_START_SERVER_FAILED", serverName));
        }

        @Override
        public void onProcessStart() {
            InputOutput io = UISupport.getServerIO(dm.getUri());
            if (io == null) {
                return;
            }

            dm.getLogManager().stop();
            try {
                // as described in the api we reset just ouptut
                io.getOut().reset();
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }

            io.select();
        }

        @Override
        public void onProcessFinish() {
            InputOutput io = UISupport.getServerIO(dm.getUri());
            if (io != null) {
                io.getOut().close();
                io.getErr().close();
            }
        }

        @Override
        public void onRunning() {
            dm.setRestartNeeded(false);
            serverProgress.notifyStart(StateType.COMPLETED,
                    NbBundle.getMessage(WLStartServer.class, "MSG_SERVER_STARTED", serverName));
        }

        @Override
        public void onTimeout() {
            serverProgress.notifyStart(StateType.FAILED,
                    NbBundle.getMessage(WLStartServer.class, "MSG_START_SERVER_TIMEOUT"));
        }

        @Override
        public void onInterrupted() {
            serverProgress.notifyStart(StateType.FAILED,
                    NbBundle.getMessage(WLStartServer.class, "MSG_START_SERVER_INTERRUPTED"));
        }

        @Override
        public void onException(Exception ex) {
            LOGGER.log(Level.WARNING, null, ex);
            serverProgress.notifyStart(StateType.FAILED,
                    NbBundle.getMessage(WLStartServer.class, "MSG_START_SERVER_FAILED", serverName));
        }

        @Override
        public void onExit() {
            // noop
        }
    }

    private static class StopListener implements RuntimeListener {

        private final WLDeploymentManager dm;

        private final String serverName;

        private final WLServerProgress serverProgress;

        public StopListener(WLDeploymentManager dm, String serverName, WLServerProgress serverProgress) {
            this.dm = dm;
            this.serverName = serverName;
            this.serverProgress = serverProgress;
        }

        @Override
        public void onStart() {
            serverProgress.notifyStart(StateType.RUNNING,
                    NbBundle.getMessage(WLStartServer.class, "MSG_STOP_SERVER_IN_PROGRESS", serverName));
        }

        @Override
        public void onFinish() {
            serverProgress.notifyStop(StateType.COMPLETED,
                    NbBundle.getMessage(WLStartServer.class, "MSG_SERVER_STOPPED", serverName));
        }

        @Override
        public void onFail() {
            serverProgress.notifyStop(StateType.FAILED,
                    NbBundle.getMessage(WLStartServer.class, "MSG_STOP_SERVER_FAILED", serverName));
        }

        @Override
        public void onProcessStart() {
            InputOutput io = UISupport.getServerIO(dm.getUri());
            if (io == null) {
                return;
            }

            dm.getLogManager().stop();
            try {
                // as described in the api we reset just ouptut
                io.getOut().reset();
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }

            io.select();
        }

        @Override
        public void onProcessFinish() {
            InputOutput io = UISupport.getServerIO(dm.getUri());
            if (io != null) {
                io.getOut().close();
                io.getErr().close();
            }
        }

        @Override
        public void onRunning() {
            serverProgress.notifyStop(StateType.RUNNING,
                    NbBundle.getMessage(WLStartServer.class, "MSG_STOP_SERVER_IN_PROGRESS", serverName));
        }

        @Override
        public void onTimeout() {
            serverProgress.notifyStop(StateType.FAILED,
                    NbBundle.getMessage(WLStartServer.class, "MSG_STOP_SERVER_TIMEOUT"));
        }

        @Override
        public void onInterrupted() {
            serverProgress.notifyStart(StateType.FAILED,
                    NbBundle.getMessage(WLStartServer.class, "MSG_STOP_SERVER_INTERRUPTED"));
        }

        @Override
        public void onException(Exception ex) {
            LOGGER.log(Level.WARNING, null, ex);
            serverProgress.notifyStart(StateType.FAILED,
                    NbBundle.getMessage(WLStartServer.class, "MSG_STOP_SERVER_FAILED", serverName));
        }

        @Override
        public void onExit() {
            // noop
        }

    }
}
