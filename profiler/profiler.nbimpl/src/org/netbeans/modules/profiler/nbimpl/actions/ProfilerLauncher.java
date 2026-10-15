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
package org.netbeans.modules.profiler.nbimpl.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.lib.profiler.common.AttachSettings;
import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.common.SessionSettings;
import org.netbeans.lib.profiler.common.integration.IntegrationUtils;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.global.Platform;
import org.netbeans.modules.profiler.HeapDumpWatch;
import org.netbeans.modules.profiler.actions.ProfilingSupport;
import org.netbeans.modules.profiler.api.GestureSubmitter;
import org.netbeans.modules.profiler.api.JavaPlatform;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.ProfilerIDESettings;
import org.netbeans.modules.profiler.api.ProjectUtilities;
import org.netbeans.modules.profiler.api.project.ProjectProfilingSupport;
import org.netbeans.modules.profiler.nbimpl.NetBeansProfiler;
import org.netbeans.modules.profiler.nbimpl.project.AntProjectSupport;
import org.netbeans.modules.profiler.nbimpl.providers.JavaPlatformManagerImpl;
import org.netbeans.modules.profiler.spi.JavaPlatformProvider;
import org.netbeans.modules.profiler.utilities.ProfilerUtils;
import org.netbeans.modules.profiler.utils.IDEUtils;
import org.netbeans.modules.profiler.v2.ProfilerSession;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Bachorik
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ProfilerLauncher_unsupportedProject=Profiling not supported for {0}",
    "ProfilerLauncher_noFeature=<html><b>No profiling feature selected.</b><br><br>Please select at least one profiling feature for the session.</html>"
})
public class ProfilerLauncher {
    private static final Logger LOG = Logger.getLogger(ProfilerLauncher.class.getName());
    
    private static final String AGENT_ARGS = "agent.jvmargs"; // NOI18N
    private static final String LINUX_THREAD_TIMER_KEY = "-XX:+UseLinuxPosixThreadCPUClocks"; // NOI18N
    private static final String ARGS_PREFIX = "profiler.netbeansBindings.jvmarg"; // NOI18N
    
    public static final class Command {
        private final String command;
        public Command(String command) { this.command = command; }
        String get() { return command; }
    }
    
    @ServiceProvider(service=ProfilerSession.Provider.class)
    public static final class SessionProvider extends ProfilerSession.Provider {
        public ProfilerSession createSession(Lookup context) {
            return new ProfilerSessionImpl(context);
        }
    }
    
    private static final class ProfilerSessionImpl extends ProfilerSession {
        
        ProfilerSessionImpl(Lookup _context) {
            super(NetBeansProfiler.getDefaultNB(), _context);
        }

        public boolean start() {
            final NetBeansProfiler profiler = (NetBeansProfiler)getProfiler();
            
            Project project = (Project)getProject();
            profiler.setProfiledProject(project, getFile());
            
            final ProfilingSettings pSettings = getProfilingSettings();
            if (pSettings == null) { // #250237 ?
                ProfilerDialogs.displayError(Bundle.ProfilerLauncher_noFeature());
                return false;
            }
            
            if (isAttach()) {
                final AttachSettings aSettings = getAttachSettings();
                
                // Log profiler usage
                if (project == null) GestureSubmitter.logAttachExternal(pSettings, aSettings);
                else GestureSubmitter.logAttachApp(project, pSettings, aSettings);
                
                ProfilerUtils.runInProfilerRequestProcessor(new Runnable() {
                    public void run() { profiler.attachToApp(pSettings, aSettings); }
                });
            } else {
                String command = normalizedCommand(getContext());
                
                if (!ProjectSensitivePerformer.supportsProfileProject(command, project) &&
                    !FileSensitivePerformer.supportsProfileFile(command, getFile())) {
                    ProfilerDialogs.displayError(Bundle.ProfilerLauncher_unsupportedProject(ProjectUtilities.getDisplayName(project)));
                    return false;
                }
                
                // Log profiler usage
                GestureSubmitter.logProfileApp(project, pSettings);
                
                final Session s = newSession(command, getContext());
                if (s != null) {
                    s.setProfilingSettings(pSettings);
                    s.run();
                    
                    // Not sure at this point if the profiling session will be started
                    // or not (main class may be missing etc.). Let's say starting the
                    // session has been cancelled if it's not running after 1200 ms.
                    //
                    // If it actually becomes alive after that time, the Profile button
                    // will be correctly pressed again so it won't hurt user experience.
                    int aliveCheck = Integer.getInteger("profiler.nbimpl.aliveCheck", 1200); // NOI18N
                    profiler.checkAliveAfter(aliveCheck); // #247826 ?
                } else {
                    return false;
                }
            }
            return true;
        }

        public boolean modify() {
            ProfilerUtils.runInProfilerRequestProcessor(new Runnable() {
                public void run() {
                    getProfiler().modifyCurrentProfiling(getProfilingSettings());
                }
            });
            return true;
        }

        public boolean stop() {
            ProfilerUtils.runInProfilerRequestProcessor(new Runnable() {
                public void run() {
                    if (isAttach()) getProfiler().detachFromApp();
                    else getProfiler().stopApp();
                }
            });
            return true;
        }
        
        protected synchronized boolean isCompatibleContext(Lookup _context) {
            // Compare projects
            Project _project = _context.lookup(Project.class);
            if (!Objects.equals(getProject(), _project)) return false;
            
            // Command and/or file can be changed if not profiling
            if (!inProgress()) return true;
            
            // Compare commands
            String c = normalizedCommand(getContext());
            String _c = normalizedCommand(_context);
            if (!Objects.equals(c, _c)) return false;
            
            // Compare files
            FileObject f = getContext().lookup(FileObject.class);
            FileObject _f = _context.lookup(FileObject.class);
            if (!Objects.equals(f, _f)) return false;
            
            return true;
        }
        
        public Lookup.Provider getProject() {
            return getContext().lookup(Project.class);
        }
        
        public FileObject getFile() {
            return getContext().lookup(FileObject.class);
        }
        
        // Handles no command from context action in editor/navigator
        private static String normalizedCommand(Lookup context) {
            Command command = context.lookup(Command.class);
            String _command = command == null ? null : command.get();
            return _command == null ? ActionProvider.COMMAND_PROFILE : _command;
        }
        
    }

    public static final class Session  {
        private SessionSettings ss;
        private ProfilingSettings ps;
        private Map<String, String> props;
        private Launcher launcher;
        private Project project;
        private FileObject fo;
        private JavaPlatform platform;
        private String command;
        private Lookup context;
        private final Map<String, Object> customProps = new HashMap<String, Object>();
        
        private boolean configured = false;
        private boolean rerun;

        public static Session createSession(String command, Lookup context) {
            Session s = new Session(command, context);
            config(s);
            lastSession = s;
            
            return s;
        }
        
        public static Session createSession(Project p) {
            Session s = new Session(p);
            config(s);
            return s;
        }
        
        private Session(Project p) {
            this.props = new HashMap<String, String>();
            this.launcher = null;
            this.project = p;
        }
        
        private Session(String command, Lookup context) {
            assert command != null;
            assert context != null;
            
            this.project = context.lookup(Project.class);
            this.fo = context.lookup(FileObject.class);
            this.command = command;
            this.props = new HashMap<String, String>();
            this.context = context;
            
            initLauncher();
        }

        public ProfilingSettings getProfilingSettings() {
            return ps;
        }
        
        public void setProfilingSettings(ProfilingSettings ps) {
            this.ps = ps;
        }
        
        public SessionSettings getSessionSettings() {
            return ss;
        }
        
        public void setSessionSettings(SessionSettings ss) {
            this.ss = ss;
            this.ss.store(props);
        }
        
        public Map<String, String> getProperties() {
            if (!configured) {
                if (configure()) {
                    return props;
                } else {
                    return null;
                }
            }
            return props;
        }
        
        public Project getProject() {
            return project;
        }
        
        public JavaPlatform getPlatform() {
            return platform;
        }
        
        public Lookup getContext() {
            return context;
        }
        
        public FileObject getFile() {
            return fo;
        }
        
        public String getCommand() {
            return command;
        }
        
        public Object getAttribute(String name) {
            return customProps.get(name);
        }
        
        public void setAttribute(String name, Object value) {
            customProps.put(name, value);
        }
        
        public boolean hasAttribute(String name) {
            return customProps.containsKey(name);
        }
        
        public boolean isConfigured() {
            return configured;
        }
        
        public boolean configure() {
            if (ss == null || ss.getJavaExecutable() == null) return false; // No platform defined; fail
            if (ps == null) return false; // Unsupported workflow (lazy settings not supported in this version)
            
            NetBeansProfiler.getDefaultNB().setProfiledProject(project, fo);

//            // ** display select task panel
//            ProfilingSettings ps = ProfilingSupport.getDefault().selectTaskForProfiling(project, ss, fo, true);
//            if (ps != null) {
//                this.ps = ps;
//                this.ps.store(props); // TODO: check whether necessary or not!
                
            setupAgentEnv(platform, ss, ProfilerIDESettings.getInstance(), ps, project, props);
            
            AntProjectSupport antSupport = AntProjectSupport.get(project);
            antSupport.configurePropertiesForProfiling(props, fo);

            rerun = false;
            configured = true;
//            }
            return configured;
        }
        
        public void run() {
            if (launcher != null) {
                launcher.launch(rerun);
                rerun = true;
            } else {
                // LOG
            }
        }
        
        private void initLauncher() {
            Project p = null;
            if (project != null) {
                p = project;
                
            } else if (fo != null) {
                p = FileOwnerQuery.getOwner(fo);
            }
            
            if (p != null) {
                LauncherFactory f = p.getLookup().lookup(LauncherFactory.class);
                if (f != null) {
                    launcher = f.createLauncher(this);
                }
            }
        }
    }
    
    public interface Launcher {
        void launch(boolean rerun);
    }
    
    public interface LauncherFactory {
        Launcher createLauncher(Session session);
    }
    
    @ProjectServiceProvider(service=LauncherFactory.class, projectTypes={
        @ProjectType(id="org-netbeans-modules-java-j2seproject"), 
        @ProjectType(id="org-netbeans-modules-ant-freeform"),
        @ProjectType(id="org-netbeans-modules-apisupport-project"),
        @ProjectType(id="org-netbeans-modules-apisupport-project-suite"),
        @ProjectType(id="org-netbeans-modules-j2ee-earproject"),
        @ProjectType(id="org-netbeans-modules-j2ee-ejbjarproject"),
        @ProjectType(id="org-netbeans-modules-web-project"),
        @ProjectType(id="org-netbeans-modules-autoproject"),
        @ProjectType(id="org-netbeans-modules-java-j2semodule")
    })
    public static final class AntLauncherFactory implements LauncherFactory {
        private final Project prj;
        public AntLauncherFactory(Project prj) {
            this.prj = prj;
        }

        @Override
        public Launcher createLauncher(Session session) {
            ActionProvider ap = prj.getLookup().lookup(ActionProvider.class);
            if (ap != null) {
                return new AntLauncher(ap, session.command, session.context);
            }
            
            return null;
        }
    }
    
    private static final class AntLauncher implements Launcher {
        private ActionProvider ap;
        private String command;
        private Lookup context;

        public AntLauncher(ActionProvider ap, String command, Lookup context) {
            this.ap = ap;
            this.command = command;
            this.context = context;
        }

        @Override
        public void launch(boolean rerun) {
            ap.invokeAction(
                command, 
                context
            );
        }
    }
    
    private static Session lastSession;
    
    public static Session newSession(@NonNull final String command, @NonNull final Lookup context) {
        return Session.createSession(command, context);                
    }
    
    public static Session getLastSession() {
        return lastSession;
    }
    
    public static void clearLastSession() {
        lastSession = null;
    }
    
    public static boolean canRelaunch() {
        return lastSession != null;
    }
    
    @NbBundle.Messages({
        "InvalidPlatformProjectMsg=The Java platform defined for the project is invalid. Right-click the project\nand choose a different platform using Properties | Libraries | Java Platform.\n\nInvalid platform: {0}",
        "InvalidPlatformProfilerMsg=The Java platform defined for profiling is invalid. Choose a different platform\nin Tools | Options | Miscellaneous | Profiler | Profiler Java Platform.\n\nInvalid platform: {0}",
        "FailedDetermineJavaPlatformMsg=Failed to determine version of Java platform: {0}",
        "LBL_Unknown=Unknown"
    })
    private static void config(Session session) {
        Project project = null;
        if (session.project == null) {
            if (session.fo != null) {
                project = FileOwnerQuery.getOwner(session.fo);
            }
        } else {
            project = session.project;
        }
        if (project == null) return; // sanity check; we need project here
        
        if (ProfilingSupport.checkProfilingInProgress()) {
            return;
        }
        
        final ProjectProfilingSupport pSupport = ProjectProfilingSupport.get(project);
        // *** java platform recheck
        JavaPlatform platform = pSupport.getProjectJavaPlatform();
        String javaFile = platform != null ? platform.getPlatformJavaFile() : null;
        if (javaFile == null) return;
//        
//        if (javaFile == null) {
//            if (ProfilerIDESettings.getInstance().getJavaPlatformForProfiling() == null) {
//                // used platform defined for project
//                ProfilerDialogs.displayError(Bundle.InvalidPlatformProjectMsg(platform != null ? platform.getDisplayName() : Bundle.LBL_Unknown()));
//            } else {
//                // used platform defined in Options / Profiler
//                ProfilerDialogs.displayError(Bundle.InvalidPlatformProfilerMsg(platform != null ? platform.getDisplayName() : Bundle.LBL_Unknown()));
//            }
//            return;
//        }
        
        final SessionSettings ss = new SessionSettings();
        // *** session settings setup
        pSupport.setupProjectSessionSettings(ss);
        ProfilerIDESettings gps = ProfilerIDESettings.getInstance();

        ss.setPortNo(gps.getPortNo());
        // ***

        final String javaVersion = platform.getPlatformJDKVersion();

        if (javaVersion == null) {
            ProfilerDialogs.displayError(Bundle.FailedDetermineJavaPlatformMsg(platform.getDisplayName()));

            return;
        }
        
        session.ss = ss;
        session.platform = platform;
    }
    
    private static void setupAgentEnv(JavaPlatform platform, SessionSettings ss, ProfilerIDESettings gps, ProfilingSettings pSettings, Project project, Map<String, String> props) {
        final boolean remote = isRemotePlatform(platform);
        String javaVersion = platform.getPlatformJDKVersion();
        String agentArgs;        
        if (remote) {
            String platformString = IntegrationUtils.getPlatformByOSAndArch(
                    Platform.getOperatingSystem(platform.getSystemProperties().get("os.name")),                 //NOI18N
                    Platform.getSystemArchitecture(platform.getSystemProperties().get("sun.arch.data.model")),   //NOI18N
                    platform.getSystemProperties().get("os.arch"),   //NOI18N
                    getArchAbi(platform)
                    );
            String platformVersion = IntegrationUtils.getJavaPlatformFromJavaVersionString(platform.getPlatformJDKVersion());
            final String prefix = getRemotePlatformWorkDirectory(platform)+project.getProjectDirectory().getName()+"/dist/remotepack";   //NOI18N
            agentArgs = IntegrationUtils.getRemoteProfilerAgentCommandLineArgsWithoutQuotes(
                prefix, platformString, platformVersion, ss.getPortNo());
            
        } else {
            if (javaVersion.equals(CommonConstants.JDK_15_STRING)) {
            // JDK 1.5 used
                agentArgs = IDEUtils.getAntProfilerStartArgument15(ss.getPortNo(), ss.getSystemArchitecture());
                if (platform.getPlatformJDKMinor() >= 7) {
                    activateOOMProtection(gps, props, project);
                } else {
                    ProfilerLogger.log("Profiler.OutOfMemoryDetection: Disabled. Not supported JVM. Use at least 1.4.2_12 or 1.5.0_07"); // NOI18N
                }
            } else if (javaVersion.equals(CommonConstants.JDK_16_STRING)) {
                // JDK 1.6 used
                agentArgs = IDEUtils.getAntProfilerStartArgument16(ss.getPortNo(), ss.getSystemArchitecture());
                activateOOMProtection(gps, props, project);
            } else if (javaVersion.equals(CommonConstants.JDK_17_STRING)) {
                agentArgs = IDEUtils.getAntProfilerStartArgument17(ss.getPortNo(), ss.getSystemArchitecture());
                activateOOMProtection(gps, props, project);
            } else if (javaVersion.equals(CommonConstants.JDK_18_STRING)) {
                agentArgs =  IDEUtils.getAntProfilerStartArgument18(ss.getPortNo(), ss.getSystemArchitecture());
                activateOOMProtection(gps, props, project);
            } else if (javaVersion.equals(CommonConstants.JDK_19_STRING)) {
                agentArgs =  IDEUtils.getAntProfilerStartArgument19(ss.getPortNo(), ss.getSystemArchitecture());
                activateOOMProtection(gps, props, project);
            } else if (javaVersion.equals(CommonConstants.JDK_110_BEYOND_STRING)) {
                agentArgs =  IDEUtils.getAntProfilerStartArgument110Beyond(ss.getPortNo(), ss.getSystemArchitecture());
                activateOOMProtection(gps, props, project);
            } else {
                throw new IllegalArgumentException("Unsupported JDK " + javaVersion); // NOI18N
            }
        }
        assert agentArgs != null;
        props.put(AGENT_ARGS, agentArgs);
        props.put(ARGS_PREFIX + ".agent", agentArgs); // NOI18N

        if (Platform.isLinux() && javaVersion.equals(CommonConstants.JDK_16_STRING)) {
            activateLinuxPosixThreadTime(pSettings, props);
        }
        
        props.put("profiler.info.project.dir", project.getProjectDirectory().getPath()); //NOI18N
    }
    
    private static String getArchAbi( JavaPlatform platform) {
        String abiWord = platform.getSystemProperties().get("sun.arch.abi"); //NOI18N
        if (abiWord == null) {
            abiWord = "";
        }
        return abiWord;
    }

    private static boolean isRemotePlatform(final JavaPlatform platform) {
        JavaPlatformManagerImpl impl = Lookup.getDefault().lookup(JavaPlatformManagerImpl.class);
        if (impl == null) {
            LOG.warning("No instance of JavaPlatformManagerImpl found in Lookup");  //NOI18N
            return false;
        }
        for (JavaPlatformProvider jpp : impl.getPlatforms(JavaPlatformManagerImpl.REMOTE_J2SE)) {
            if (platform.getPlatformId() != null && platform.getPlatformId().equals(jpp.getPlatformId())) {
                return true;
            }
        }
        return false;
    }
    
    private static String getRemotePlatformWorkDirectory(final JavaPlatform platform) {
        JavaPlatformManagerImpl impl = Lookup.getDefault().lookup(JavaPlatformManagerImpl.class);
        for (JavaPlatformProvider jpp : impl.getPlatforms(JavaPlatformManagerImpl.REMOTE_J2SE)) {
            if (platform.getPlatformId() != null && platform.getPlatformId().equals(jpp.getPlatformId())) {
                org.netbeans.api.java.platform.JavaPlatform platformDelegate =
                        impl.getPlatformDelegate(jpp);
                if (platformDelegate == null) {
                    return null;
                }
                String workdir = platformDelegate.getProperties().get("platform.work.folder");    //NOI18N            
                return (workdir.endsWith("/"))?(workdir):(workdir+"/");   //NOI18N
            }
        }
        return null;
    }
    
    private static void activateLinuxPosixThreadTime(ProfilingSettings ps, Map<String, String> props) {
        if (ps.getThreadCPUTimerOn()) {
            props.put("profiler.info.jvmargs", LINUX_THREAD_TIMER_KEY + " " + props.get("profiler.info.jvmargs")); // NOI18N
            ProfilerLogger.log("Profiler.UseLinuxPosixThreadCPUClocks: Enabled"); // NOI18N
        }
    }

    private static void activateOOMProtection(ProfilerIDESettings gps, Map<String, String> props, Project project) {
        if (gps.isOOMDetectionEnabled()) {
            String oldArgs = props.get("profiler.info.jvmargs");//NOI18N
            oldArgs = (oldArgs != null) ? oldArgs : "";//NOI18N

            StringBuilder oomArgsBuffer = new StringBuilder(oldArgs);
            String heapDumpPath = HeapDumpWatch.getHeapDumpPath(project);

            if ((heapDumpPath != null) && (heapDumpPath.length() > 0)) {
                // used as an argument for starting java process
                if (heapDumpPath.contains(" ")) {
                    heapDumpPath = "\"" + heapDumpPath + "\"";//NOI18N
                }

                final String oom = "-XX:+HeapDumpOnOutOfMemoryError";
                final String path = "-XX:HeapDumpPath=" + heapDumpPath;
                oomArgsBuffer.append(" ").append(oom); // NOI18N
                oomArgsBuffer.append(" ").append(path).append(" "); // NOI18N
                props.put(ARGS_PREFIX + ".outOfMemory", oom); // NOI18N
                props.put(ARGS_PREFIX + ".heapDumpPath", path); // NOI18N

                ProfilerLogger.log("Profiler.OutOfMemoryDetection: Enabled"); // NOI18N
            }

            props.put("profiler.info.jvmargs", oomArgsBuffer.toString()); // NOI18N
        }
    }
}
