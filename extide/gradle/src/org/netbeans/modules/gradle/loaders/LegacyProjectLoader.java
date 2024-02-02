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
package org.netbeans.modules.gradle.loaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;
import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildActionExecuter;
import org.gradle.tooling.BuildController;
import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressEvent;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.ProjectConnection;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.gradle.GradleProject;
import org.netbeans.modules.gradle.GradleProjectErrorNotifications;
import org.netbeans.modules.gradle.NbGradleProjectImpl;
import static org.netbeans.modules.gradle.loaders.GradleDaemon.GRADLE_LOADER_RP;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.GradleReport;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.NbGradleProject.Quality;
import static org.netbeans.modules.gradle.api.NbGradleProject.Quality.EVALUATED;
import static org.netbeans.modules.gradle.api.NbGradleProject.Quality.FULL_ONLINE;
import static org.netbeans.modules.gradle.api.NbGradleProject.Quality.SIMPLE;
import org.netbeans.modules.gradle.tooling.internal.NbProjectInfo;
import org.netbeans.modules.gradle.tooling.internal.NbProjectInfo.Report;
import org.netbeans.modules.gradle.api.execute.GradleCommandLine;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.modules.gradle.cache.ProjectInfoDiskCache;
import org.netbeans.modules.gradle.execute.GradleNetworkProxySupport;
import org.netbeans.modules.gradle.spi.GradleSettings;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;

import static org.netbeans.modules.gradle.loaders.Bundle.*;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author lkishalmi
 */
//@ProjectServiceProvider(service = GradleProjectLoader.class, projectTypes = @ProjectType(id = NbGradleProject.GRADLE_PROJECT_TYPE, position=500))
public class LegacyProjectLoader extends AbstractProjectLoader {
    /**
     * Thread which will log output from the build process, eventually. Note that the project loader runs single-threaded, 
     * so one task RP should be sufficient.
     */
    private static final RequestProcessor   DAEMON_LOG_RP = new RequestProcessor(LegacyProjectLoader.class);
    
    private enum GoOnline { NEVER, ON_DEMAND, ALWAYS }

    private static final Logger LOG = Logger.getLogger(LegacyProjectLoader.class.getName());


    private static AtomicLong timeInLoad = new AtomicLong();
    private static AtomicInteger loadedProjects = new AtomicInteger();

    private static final boolean DEBUG_GRADLE_INFO_ACTION = Boolean.getBoolean("netbeans.debug.gradle.info.action"); //NOI18N


    public LegacyProjectLoader(ReloadContext ctx) {
        super(ctx);
    }

    @Override
    public GradleProject load() {
        GradleProject ret;
        try {
            ret = GRADLE_LOADER_RP.submit(new ProjectLoaderTask(ctx)).get();
            updateSubDirectoryCache(ret);
        } catch (InterruptedException | ExecutionException ex) {
            ret = null;
        }
        return ret;
    }

    @Override
    public boolean isEnabled() {
        return ctx.aim.betterThan(EVALUATED);
    }

    @NbBundle.Messages({
        "# {0} - project directory",
        "TIT_LOAD_FAILED=Cannot load: {0}",
        "# {0} - project name",
        "TIT_LOAD_ISSUES={0} has some issues"
    })
    private static GradleProject loadGradleProject(ReloadContext ctx, CancellationToken token, ProgressListener pl) {
        long start = System.currentTimeMillis();
        NbProjectInfo info = null;
        NbGradleProject.Quality quality = ctx.aim;
        GradleBaseProject base = ctx.previous.getBaseProject();

        ProjectConnection pconn = ctx.project.getLookup().lookup(ProjectConnection.class);
        GradleProjectErrorNotifications errors = ctx.project.getLookup().lookup(GradleProjectErrorNotifications.class);


        GradleCommandLine cmd = new GradleCommandLine(RunUtils.getCompatibleGradleDistribution(ctx.project), ctx.cmd);
        cmd.setFlag(GradleCommandLine.Flag.CONFIGURE_ON_DEMAND, GradleSettings.getDefault().isConfigureOnDemand());
        cmd.setFlag(GradleCommandLine.Flag.CONFIGURATION_CACHE, GradleSettings.getDefault().getUseConfigCache());
        cmd.addParameter(GradleCommandLine.Parameter.INIT_SCRIPT, GradleDaemon.initScript());
        cmd.setStackTrace(GradleCommandLine.StackTrace.SHORT);
        cmd.addProjectProperty("nbSerializeCheck", "true");

        GoOnline goOnline;
        if (GradleSettings.getDefault().isOffline()) {
            goOnline = GoOnline.NEVER;
        } else if (ctx.aim == FULL_ONLINE) {
            goOnline = GoOnline.ALWAYS;
        } else {
            switch (GradleSettings.getDefault().getDownloadLibs()) {
                case NEVER:
                    goOnline = GoOnline.NEVER;
                    break;
                case ALWAYS:
                    goOnline = GoOnline.ALWAYS;
                    break;
                default:
                    goOnline = GoOnline.ON_DEMAND;
            }
        }
        try {

            errors.clear();
            AtomicBoolean onlineResult = new AtomicBoolean();
            info = retrieveProjectInfo(ctx.project, goOnline, pconn, cmd, token, pl, onlineResult);
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("Retrieved project info:");
                List<String> keys = new ArrayList<>(info.getInfo().keySet());
                Collections.sort(keys);
                for (String s : keys) {
                    Object o = info.getInfo().get(s);
                    // format just the 1st level:
                    if (o instanceof Collection) {
                        Collection c = (Collection)o;
                        if (!c.isEmpty()) {
                            LOG.finer(String.format("    %-20s: [", s));
                            for (Object x: c) {
                                if (Object[].class.isInstance(x)) {
                                    x = Arrays.asList((Object[])x);
                                }
                                LOG.finer(String.format("    %-20s", x));
                            }
                            LOG.finer("    ]");
                            continue;
                        }
                    } else if (o instanceof Map) {
                        Map m = (Map)o;
                        if (!m.isEmpty()) {
                            LOG.finer(String.format("    %-20s: {", s));
                            List<String> mkeys = new ArrayList<>(m.keySet());
                            Collections.sort(mkeys);
                            for (String k : mkeys) {
                                Object x = m.get(k);
                                if (Object[].class.isInstance(x)) {
                                    x = Arrays.asList((Object[])x);
                                }
                                LOG.finer(String.format("        %-20s:%s", k, x));
                            }
                            LOG.finer("    }");
                        }
                        continue;
                    }
                    LOG.finer(String.format("    %-20s:%s", s, o));
                }
            }
            List<Report> errorReports = info.getReports().stream().filter(r -> r.getSeverity() == Report.Severity.ERROR).collect(Collectors.toList());
            if (!info.getProblems().isEmpty()) {
                errors.openNotification(
                        TIT_LOAD_ISSUES(base.getProjectDir().getName()),
                        TIT_LOAD_ISSUES(base.getProjectDir().getName()),
                        GradleProjectErrorNotifications.bulletedList(info.getProblems()));
            }
            if (!info.getReports().isEmpty()) {
                errors.openNotification(
                        TIT_LOAD_ISSUES(base.getProjectDir().getName()),
                        TIT_LOAD_ISSUES(base.getProjectDir().getName()),
                        GradleProjectErrorNotifications.bulletedList(
                                info.getReports().stream().map(r -> r.getMessage()).collect(Collectors.toList())
                        ));
            }
            if (!info.hasException()) {
                if (!info.getProblems().isEmpty() || !errorReports.isEmpty()) {
                    if (LOG.isLoggable(Level.FINE)) {
                        // If we do not have exception, but seen some problems the we mark the quality as SIMPLE
                        Object o = new ArrayList<String>(info.getReports().stream().
                                map(LegacyProjectLoader::copyReport).
                                map((r) -> r.formatReportForHintOrProblem(
                                        true, 
                                        FileUtil.toFileObject(
                                                ctx.project.getGradleFiles().getBuildScript()
                                        )
                                )).
                                collect(Collectors.toList())
                        );
                        LOG.log(Level.FINE, "Project {0} loaded without exception, but with problems: {1}", 
                                new Object[] {
                                    ctx.project, 
                                    o
                                }
                        );
                    }                    
                    quality = SIMPLE;
                } else {
                    // the project has been either fully loaded, or online checked
                    quality = onlineResult.get() ? Quality.FULL_ONLINE : Quality.FULL;
                }
            } else {
                if (info.getProblems().isEmpty() && errorReports.isEmpty()) {
                    String problem = info.getGradleException();
                    String[] lines = problem.split("\n");
                    LOG.log(INFO, "Failed to retrieve project information for: {0}\nReason: {1}", new Object[] {base.getProjectDir(), problem}); //NOI18N
                    errors.openNotification(TIT_LOAD_FAILED(base.getProjectDir().getName()), lines[0], problem);
                    return ctx.previous.invalidate(problem);
                } else {
                    List<GradleReport> reps = new ArrayList<>();
                    for (Report r : info.getReports()) {
                        reps.add(copyReport(r));
                    }
                    Object o = new ArrayList<String>(reps.stream().
                            map((r) -> r.formatReportForHintOrProblem(
                                true, 
                                FileUtil.toFileObject(
                                    ctx.project.getGradleFiles().getBuildScript()
                                )
                            )).
                            collect(Collectors.toList())
                    );
                    LOG.log(Level.FINE, "Project {0} loaded with exception, and with problems: {1}", 
                            new Object[] {
                                ctx.project, 
                                o
                            }
                    );
                    LOG.log(FINE, "Thrown exception:", info.getGradleException()); //NOI18N
                    File f = ctx.project.getGradleFiles().getBuildScript();
                    for (String s : info.getProblems()) {
                        reps.add(GradleProject.createGradleReport(f == null ? null : f.toPath(), s));
                    }
                    return ctx.previous.invalidate(reps.toArray(new GradleReport[0]));
                }
            }
        } catch (GradleConnectionException | IllegalStateException ex) {
            LOG.log(FINE, "Failed to retrieve project information for: " + base.getProjectDir(), ex);
            List<GradleReport> problems = exceptionsToProblems(ctx.project.getGradleFiles().getBuildScript(), ex);
            errors.openNotification(TIT_LOAD_FAILED(base.getProjectDir()), ex.getMessage(), GradleProjectErrorNotifications.bulletedList(problems));
            return ctx.previous.invalidate(problems.toArray(new GradleReport[0]));
        } finally {
            loadedProjects.incrementAndGet();
        }
        long finish = System.currentTimeMillis();
        timeInLoad.getAndAdd(finish - start);
        LOG.log(FINE, "Loaded project {0} in {1} msec", new Object[]{base.getProjectDir(), finish - start});
        if (SwingUtilities.isEventDispatchThread()) {
            LOG.log(FINE, "Load happened on AWT event dispatcher", new RuntimeException());
        }
        ProjectInfoDiskCache.QualifiedProjectInfo qinfo = new ProjectInfoDiskCache.QualifiedProjectInfo(quality, info);
        GradleProject ret = createGradleProject(ctx.project.getGradleFiles(), qinfo);
        GradleArtifactStore.getDefault().processProject(ret);
        if (info.getMiscOnly()) {
            ret = ctx.previous;
        } else {
            saveCachedProjectInfo(qinfo, ret);
        }
        return ret;
    }
    
    static GradleReport copyReport(Report orig) {
        String rawLoc = orig.getScriptLocation();
        String loc = null;
        
        if (rawLoc != null) {
        // strip potential script displayname garbage.
            Matcher m = FILE_PATH_FROM_LOCATION.matcher(rawLoc);
            if (m.matches()) {
                loc = m.group(1);
            }
        }
        GradleReport.Severity s;
        if (orig.getSeverity() == null) {
            s = GradleReport.Severity.ERROR;
        } else switch (orig.getSeverity()) {
            case INFO:
                s = GradleReport.Severity.INFO;
                break;
            case WARNING:
                s = GradleReport.Severity.WARNING;
                break;
            case EXCEPTION:
                s = GradleReport.Severity.EXCEPTION;
                break;
            default:
                s = GradleReport.Severity.ERROR;
                break;
        }
        
        String[] lines = orig.getDetail() == null ? null : orig.getDetail().split("\n");
        return GradleProject.createGradleReport(s, orig.getErrorClass(), loc, orig.getLineNumber(), orig.getMessage(),
                orig.getCause() == null ? null : copyReport(orig.getCause()), lines);
    }
    
    private static List<GradleReport> causesToProblems(Throwable ex) {
        List<GradleReport> problems = new ArrayList<>();
        Throwable th = ex;
        while (th != null) {
            problems.add(GradleProject.createGradleReport(null, th.getMessage()));
            ex = th;
            th = th.getCause();
            if (ex == th) {
                break;
            }
        }
        return problems;
    }
    
    @NbBundle.Messages({
        "# {0} - previous part",
        "# {1} - appended part",
        "FMT_AppendMessage={0} {1}",
        "# {0} - the error message",
        "# {1} - the file / line",
        "FMT_MessageWithLocation={0} ({1})"
    })
    /**
     * Rearranges the exception stack messages to be more readable. A typical Gradle build exception is a 
     * {@link GradleConnectionException} that wraps the actual exception. The message of this exception
     * is completely useless except possibly for gradle wrapper/distribution path.
     * 
     * The next to rearrange is the positional information - the message should come first as it
     * often appears in the title. The positional information holder is not a part of oficial tooling API
     * so a little hack is used to extract the information from the exception chain.
     * 
     * The rest of messages is coalesced into one text. Location, if present, is appended at the end.
     */
    private static List<GradleReport> exceptionsToProblems(File script, Throwable t) {
        if (!(t instanceof GradleConnectionException)) {
            return causesToProblems(t);
        }
        return Collections.singletonList(createReport(script, t.getCause(), new boolean[1]));
    }
    
    private static String getLocation(Throwable locationAwareEx) {
        try {
            Method locationAccessor = locationAwareEx.getClass().getMethod("getLocation"); // NOI18N
            return (String)locationAccessor.invoke(locationAwareEx);
        } catch (ReflectiveOperationException ex) {
            LOG.log(Level.FINE,"Error getting location", ex);
        } catch (IllegalArgumentException iae) {
            LOG.log(Level.FINE, "This probably should not happen: " + locationAwareEx.getClass().getName(), iae);
        }
        return null;
    }

    private static int getLineNumber(Throwable locationAwareEx) {
        try {
            Method lineNumberAccessor = locationAwareEx.getClass().getMethod("getLineNumber"); // NOI18N
            Integer i = (Integer)lineNumberAccessor.invoke(locationAwareEx);
            return i != null ? i : -1;
        } catch (ReflectiveOperationException ex) {
            LOG.log(Level.FINE,"Error getting line number", ex);
        } catch (IllegalArgumentException iae) {
            LOG.log(Level.FINE, "This probably should not happen: " + locationAwareEx.getClass().getName(), iae);
        }
        return -1;
    }

    /**
     * LocationAwareException uses ScriptSource.getDisplayName() in its location; so the filename is prepended by 'build file', usually
     * capitalized. Who knows what other labels the resources gradle uses can have ? Add newly discovered ones to the regexp.
     */
    private static final Pattern FILE_PATH_FROM_LOCATION = Pattern.compile("(?:build|settings) file '(.*)'(?: line:.*)$", Pattern.CASE_INSENSITIVE);

    /**
     * Converts exception hierarchy into chain of {@link GradleReports}. Each LocationAwareException's data
     * are used to annotated its nested cause's message.
     * @param e the throwable
     * @return head of {@link GradleRepor} chain.
     */
    private static GradleReport createReport(File p, Throwable e, boolean[] user) {
        return createReport(p, e, true, user);
    }
    
    private static GradleReport createReport(File p, Throwable e, boolean top, boolean[] user) {
        if (e == null) {
            return null;
        }

        Throwable reported = e;
        String loc = null;
        int line = -1;
        GradleReport nested = null;
        
        if (e.getClass().getName().endsWith("LocationAwareException")) { // NOI18N
            user[0] = true;
            String rawLoc = getLocation(e);
            if (rawLoc != null) {
                Matcher m = FILE_PATH_FROM_LOCATION.matcher(rawLoc);
                loc = m.matches() ? m.group(1) : rawLoc;
                line = getLineNumber(e);
            }
            reported = e.getCause();
        } else {
            reported = e;
        }
        String cn = e.getClass().getName();
        if (cn.contains("GradleScriptException") || cn.contains("ResolutionException")) {
            user[0] = true;
        }
        if (reported.getCause() != null && reported.getCause() != reported) {
            nested = createReport(p, reported.getCause(), false, user);
        }
        String m = reported.getMessage();
        if (m == null) {
            m = reported.getClass().getSimpleName();
        }
        String[] traceLines = null;
        if (top) {
            LOG.log(Level.WARNING, "Loading of script {0} threw an exception {2}", new Object[] { p, reported.getClass().getName() } );
            // need to log the exception at severity INFO so it does not appear as a red problem in Notifications.
            LOG.log(Level.INFO, "Stacktrace from gradle daemon:", reported);
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                reported.printStackTrace(pw);
            }
            String[] l = sw.toString().split("\n");
            traceLines = Arrays.copyOf(l, Math.min(l.length, 100));
        }
        return GradleProject.createGradleReport(GradleReport.Severity.ERROR, reported.getClass().getName(), loc, line, m, nested, user[0] ? null : traceLines);
    }

    private static BuildActionExecuter<NbProjectInfo> createInfoAction(ProjectConnection pconn, GradleCommandLine cmd, CancellationToken token, ProgressListener pl) {
        BuildActionExecuter<NbProjectInfo> ret = pconn.action(new NbProjectInfoAction());
        cmd.configure(ret);
        if (DEBUG_GRADLE_INFO_ACTION) {
            // This would start the Gradle Daemon in Debug Mode, so the Tooling API can be debugged as well
            ret.addJvmArguments("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5006");
        }
        if (LOG.isLoggable(Level.FINEST)) {
            ret.addArguments("--debug");
        } else if (LOG.isLoggable(Level.FINER)) {
            ret.addArguments("--info");
        } else {
            ret.addArguments("--warn");
        }
        if (token != null) {
            ret.withCancellationToken(token);
        }

        if (pl != null) {
            ret.addProgressListener(pl);
        }
        return ret;
    }

    @NbBundle.Messages({
        "ERR_UserAbort=Project analysis aborted by the user."
    })
    private static NbProjectInfo retrieveProjectInfo(NbGradleProjectImpl projectImpl, GoOnline goOnline, ProjectConnection pconn, GradleCommandLine cmd, CancellationToken token, ProgressListener pl, AtomicBoolean wasOnline) throws GradleConnectionException, IllegalStateException {
        NbProjectInfo ret;

        GradleSettings settings = GradleSettings.getDefault();

        GradleCommandLine online = new GradleCommandLine(cmd);
        GradleCommandLine offline = new GradleCommandLine(cmd);

        if (goOnline != GoOnline.ALWAYS) {
            if (settings.getDownloadSources() == GradleSettings.DownloadMiscRule.ALWAYS) {
                //online.addProjectProperty("downloadSources", "ALL"); //NOI18N
            }
            if (settings.getDownloadJavadoc() == GradleSettings.DownloadMiscRule.ALWAYS) {
                //online.addProjectProperty("downloadJavadoc", "ALL"); //NOI18N
            }
            offline.addFlag(GradleCommandLine.Flag.OFFLINE);
        }

        if (goOnline == GoOnline.NEVER || goOnline == GoOnline.ON_DEMAND) {
            BuildActionExecuter<NbProjectInfo> action = createInfoAction(pconn, offline, token, pl);
            wasOnline.set(!offline.hasFlag(GradleCommandLine.Flag.OFFLINE));
            try {
                ret = runInfoAction(action);
                if (goOnline == GoOnline.NEVER || !ret.hasException()) {
                    return ret;
                }
            } catch (GradleConnectionException | IllegalStateException ex) {
                LOG.log(Level.FINE, "Project {0} loaded with exception for mode {1}", 
                        new Object[] { projectImpl, goOnline });
                LOG.log(Level.FINE, "Thrown exception is: ", ex);
                if (goOnline == GoOnline.NEVER) {
                    throw ex;
                }
            }
        }

        BuildActionExecuter<NbProjectInfo> action = createInfoAction(pconn, online, token, pl);        
        // since we're going online, check the network settings:
        GradleNetworkProxySupport support = projectImpl.getLookup().lookup(GradleNetworkProxySupport.class);
        if (support != null) {
            try {
                GradleNetworkProxySupport.ProxyResult result = support.checkProxySettings().get();
                switch (result.getStatus()) {
                    case ABORT:
                        LOG.log(Level.FINE, "User cancelled the project load");
                        throw new IllegalStateException(Bundle.ERR_UserAbort());
                }
                action = result.configure(action);
            } catch (InterruptedException ex) {
                throw new IllegalStateException(ex);
            } catch (ExecutionException ex) {
                throw new IllegalStateException(ex);
            }
        }
        
        wasOnline.set(true);
        return runInfoAction(action);
    }
    

    /**
     * Makes a workaround for standard {@link PipedOutputStream} wait.
     * <p>The {@link PipedInputStream#read()}, in case the receive buffer is
     * empty at the time of the call, waits for up to 1000ms.
     * {@link PipedOutputStream#write(int)} does call <code>sink.receive</code>,
     * but does not <code>notify()</code> the sink object so that read's
     * wait() terminates.
     * <p>
     * As a result, the read side of the pipe waits full 1000ms even though data
     * become available during the wait.
     * <p>
     * The workaround is to simply {@link PipedOutputStream#flush} after write,
     * which returns from wait()s immediately.
     *
     * @author Svata Dedic Copyright (C) 2020
     */
    static class ImmediatePipedOutputStream extends PipedOutputStream {
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            super.write(b, off, len);
            flush();
        }

        @Override
        public void write(int b) throws IOException {
            super.write(b);
            flush();
        }
    }

    private static NbProjectInfo runInfoAction(BuildActionExecuter<NbProjectInfo> action) {
        class LogDelegate implements Runnable {
            final BufferedReader rdr;
            
            LogDelegate(InputStream is) throws IOException {
                rdr = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            }
            
            public void run() {
                boolean first = true;
                try {
                    String line;
                    while ((line = rdr.readLine()) != null) {
                        if (first) {
                            LOG.log(Level.FINER, "[gradle] ---- daemon log starting");
                            first = false;
                        }
                        LOG.log(Level.FINER, "[gradle] {0}", line);
                    }
                } catch (IOException ex) {
                } finally {
                    LOG.log(Level.FINER, "[gradle] ---- log terminated");
                }
            }
        }
        
        OutputStream logStream = null;
        try {
            if (LOG.isLoggable(Level.FINER)) {
                if (LOG.isLoggable(Level.FINEST)) {
                    action.addArguments("--debug"); // NOI18N
                }
                PipedOutputStream pos = new ImmediatePipedOutputStream();
                try {
                    logStream = pos;
                    DAEMON_LOG_RP.post(new LogDelegate(new PipedInputStream(pos)));
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
                action.setStandardOutput(pos);
                action.setStandardError(pos);
            }
        
            return action.run(); 
        } finally {
            if (logStream != null) {
                try {
                    logStream.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    private static class NbProjectInfoAction implements Serializable, BuildAction<NbProjectInfo> {

        @Override
        public NbProjectInfo execute(BuildController bc) {
            return bc.getModel(NbProjectInfo.class);
        }
    }

    private static class ProjectLoaderTask implements Callable<GradleProject>, Cancellable {

        private final ReloadContext ctx;
        private CancellationTokenSource tokenSource;

        public ProjectLoaderTask(ReloadContext ctx) {
            this.ctx = ctx;
        }

        @NbBundle.Messages({
            "# {0} - The project name",
            "LBL_Loading=Loading {0}",
            "# {0} (re)load reason",
            "# {1} project name",
            "FMT_ProjectLoadReason={0} ({1})"
        })
        @Override
        public GradleProject call() throws Exception {
            tokenSource = GradleConnector.newCancellationTokenSource();
            String msg;
            if (ctx.description != null) {
                msg = Bundle.FMT_ProjectLoadReason(ctx.description, ctx.previous.getBaseProject().getName());
            } else {
                msg = Bundle.LBL_Loading(ctx.previous.getBaseProject().getName());
            }
            final ProgressHandle handle = ProgressHandle.createHandle(msg, this);
            ProgressListener pl = (ProgressEvent pe) -> {
                handle.progress(pe.getDescription());
            };
            handle.start();
            try {
                return loadGradleProject(ctx, tokenSource.token(), pl);
            } catch (Throwable ex) {
                LOG.log(WARNING, ex.getMessage(), ex);
                throw ex;
            } finally {
                handle.finish();
            }
        }

        @Override
        public boolean cancel() {
            if (tokenSource != null) {
                tokenSource.cancel();
            }
            return true;
        }

    }

}
