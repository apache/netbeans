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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import java.util.logging.Logger;
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
import static org.netbeans.modules.gradle.loaders.GradleDaemon.GRADLE_LOADER_RP;
import static org.netbeans.modules.gradle.loaders.GradleDaemon.INIT_SCRIPT;
import static org.netbeans.modules.gradle.loaders.GradleDaemon.TOOLING_JAR;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.NbGradleProject;
import static org.netbeans.modules.gradle.api.NbGradleProject.Quality.EVALUATED;
import static org.netbeans.modules.gradle.api.NbGradleProject.Quality.FULL_ONLINE;
import static org.netbeans.modules.gradle.api.NbGradleProject.Quality.SIMPLE;
import org.netbeans.modules.gradle.api.NbProjectInfo;
import org.netbeans.modules.gradle.api.execute.GradleCommandLine;
import org.netbeans.modules.gradle.cache.ProjectInfoDiskCache;
import org.netbeans.modules.gradle.spi.GradleSettings;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;

import static org.netbeans.modules.gradle.loaders.Bundle.*;

/**
 *
 * @author lkishalmi
 */
//@ProjectServiceProvider(service = GradleProjectLoader.class, projectTypes = @ProjectType(id = NbGradleProject.GRADLE_PROJECT_TYPE, position=500))
public class LegacyProjectLoader extends AbstractProjectLoader {

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


        GradleCommandLine cmd = new GradleCommandLine(ctx.cmd);
        cmd.setFlag(GradleCommandLine.Flag.CONFIGURE_ON_DEMAND, GradleSettings.getDefault().isConfigureOnDemand());
        cmd.addParameter(GradleCommandLine.Parameter.INIT_SCRIPT, INIT_SCRIPT);
        cmd.setStackTrace(GradleCommandLine.StackTrace.SHORT);
        cmd.addSystemProperty(GradleDaemon.PROP_TOOLING_JAR, TOOLING_JAR);
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
            info = retrieveProjectInfo(goOnline, pconn, cmd, token, pl);

            if (!info.getProblems().isEmpty()) {
                errors.openNotification(
                        TIT_LOAD_ISSUES(base.getProjectDir().getName()),
                        TIT_LOAD_ISSUES(base.getProjectDir().getName()),
                        GradleProjectErrorNotifications.bulletedList(info.getProblems()));                
            }
            if (!info.hasException()) {
                if (!info.getProblems().isEmpty()) {
                    // If we do not have exception, but seen some problems the we mark the quality as SIMPLE
                    quality = SIMPLE;
                } else {
                    quality = ctx.aim;
                }
            } else {
                if (info.getProblems().isEmpty()) {
                    String problem = info.getGradleException();
                    String[] lines = problem.split("\n");
                    LOG.log(INFO, "Failed to retrieve project information for: {0}\nReason: {1}", new Object[] {base.getProjectDir(), problem}); //NOI18N
                    errors.openNotification(TIT_LOAD_FAILED(base.getProjectDir().getName()), lines[0], problem);
                    return ctx.previous.invalidate(problem);
                } else {
                    return ctx.previous.invalidate(info.getProblems().toArray(new String[0]));
                }
            }
        } catch (GradleConnectionException | IllegalStateException ex) {
            LOG.log(FINE, "Failed to retrieve project information for: " + base.getProjectDir(), ex);
            List<String> problems = new ArrayList<>();
            Throwable th = ex;
            while (th != null) {
                problems.add(th.getMessage());
                th = th.getCause();
            }
            errors.openNotification(TIT_LOAD_FAILED(base.getProjectDir()), ex.getMessage(), GradleProjectErrorNotifications.bulletedList(problems));
            return ctx.previous.invalidate(problems.toArray(new String[0]));
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
        GradleProject ret = createGradleProject(qinfo);
        GradleArtifactStore.getDefault().processProject(ret);
        if (info.getMiscOnly()) {
            ret = ctx.previous;
        } else {
            saveCachedProjectInfo(qinfo, ret);
        }
        return ret;
    }

    private static BuildActionExecuter<NbProjectInfo> createInfoAction(ProjectConnection pconn, GradleCommandLine cmd, CancellationToken token, ProgressListener pl) {
        BuildActionExecuter<NbProjectInfo> ret = pconn.action(new NbProjectInfoAction());
        cmd.configure(ret);
        if (DEBUG_GRADLE_INFO_ACTION) {
            // This would start the Gradle Daemon in Debug Mode, so the Tooling API can be debugged as well
            ret.addJvmArguments("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5006");
        }
        if (token != null) {
            ret.withCancellationToken(token);
        }

        if (pl != null) {
            ret.addProgressListener(pl);
        }
        return ret;
    }

    private static NbProjectInfo retrieveProjectInfo(GoOnline goOnline, ProjectConnection pconn, GradleCommandLine cmd, CancellationToken token, ProgressListener pl) throws GradleConnectionException, IllegalStateException {
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
            try {
                ret = action.run();
                if (goOnline == GoOnline.NEVER || !ret.hasException()) {
                    return ret;
                }
            } catch (GradleConnectionException | IllegalStateException ex) {
                if (goOnline == GoOnline.NEVER) {
                    throw ex;
                }
            }
        }

        BuildActionExecuter<NbProjectInfo> action = createInfoAction(pconn, online, token, pl);
        ret = action.run();
        return ret;
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
