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

package org.netbeans.modules.maven.problems;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.TestChecker;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig.ReactorStyle;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.execute.BeanRunConfig;
import org.netbeans.modules.maven.execute.MavenProxySupport;
import org.netbeans.modules.maven.execute.MavenProxySupport.ProxyResult;
import org.netbeans.modules.maven.modelcache.MavenProjectCache;
import org.netbeans.modules.maven.options.MavenSettings;
import static org.netbeans.modules.maven.problems.Bundle.*;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

/**
 * Corrective action to run some target which can download plugins or parent POMs.
 * At worst it will show the same problem in the Output Window, so the user is more likely
 * to believe that there really is a problem with their project, not NetBeans.
 */
@Messages({"ACT_validate=Priming Build",
            "ACT_PrimingComplete=Priming build was completed",
            "ACT_start_validate=Priming build was started."})
public class SanityBuildAction implements ProjectProblemResolver {
    private static final Logger LOG = Logger.getLogger(SanityBuildAction.class.getName());
    
    private final Project nbproject;
    private final Supplier<Boolean> checkSupplier;
    
    /**
     * The priming build, which is currently pending or recently completed.
     * Initially empty.
     */
    private volatile CompletableFuture<ProjectProblemsProvider.Result> pendingResult;

    public SanityBuildAction(Project nbproject, Supplier<Boolean> checkSupplier) {
        this.nbproject = nbproject;
        this.checkSupplier = checkSupplier;
    }

    public SanityBuildAction(Project nbproject, Supplier<Boolean> checkSupplier, Future<ProjectProblemsProvider.Result> otherResult) {
        this.nbproject = nbproject;
        this.checkSupplier = checkSupplier;
    }
    
    public Future<ProjectProblemsProvider.Result> getPendingResult() {
        return this.pendingResult;
    }

    @NbBundle.Messages({
        "ERR_SanityBuildCancalled=Sanity build cancelled",
        "# {0} - message",
        "ERR_ProxyUpdateFailed=Proxy setup failed: {0}"
    })
    @Override
    public CompletableFuture<ProjectProblemsProvider.Result> resolve() {
        return resolve(null);
    }
    
    public CompletableFuture<ProjectProblemsProvider.Result> resolve(Lookup context) {
        CompletableFuture<ProjectProblemsProvider.Result> pr = pendingResult;
        if (pr != null && !pr.isDone()) {
            LOG.log(Level.FINE, "SanityBuild.resolve returns: {0}", pendingResult);
            return pendingResult;
        }
        final CompletableFuture<ProjectProblemsProvider.Result> publicResult = new CompletableFuture<>();

        Runnable toRet = new Runnable() {
            @Override
            public void run() {
                /**
                 * From the appearance of the Sanity action, the project's state may have changed. In that case the sanity build should not run maven again.
                 */
                if (!checkSupplier.get()) {
                    ProjectProblemsProvider.Result r = ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED, ACT_start_validate());
                    if (r != null) {
                        LOG.log(Level.FINE, "Project {0} is OK before sanity build action.", nbproject);
                        publicResult.complete(r);
                        return;
                    }
                }
                try {
                    LOG.log(Level.FINE, "Configuring sanity build");
                    BeanRunConfig config = new BeanRunConfig();
                    if (context != null) {
                        config.setActionContext(context);
                    }
                    config.setExecutionDirectory(FileUtil.toFile(nbproject.getProjectDirectory()));
                    NbMavenProject mavenPrj = nbproject.getLookup().lookup(NbMavenProject.class);
                    MavenProject loaded = NbMavenProject.getPartialProject(mavenPrj.getMavenProject());
                    String goals;
                    if (mavenPrj != null
                            && mavenPrj.getMavenProject().getVersion() != null 
                            && mavenPrj.getMavenProject().getVersion().endsWith("SNAPSHOT")) {
                        goals = NbPreferences.forModule(MavenSettings.class).get("primingBuild.snapshot.goals", "install");
                    } else {
                        goals = NbPreferences.forModule(MavenSettings.class).get("primingBuild.regular.goals", "package");
                    }
                    config.setGoals(Arrays.asList("--fail-at-end", goals)); // NOI18N
                    config.setReactorStyle(ReactorStyle.ALSO_MAKE);
                    config.setProperty(TestChecker.PROP_SKIP_TEST, "true"); //priming doesn't need test execution, just compilation
                    config.setProject(nbproject);
                    String label = build_label(nbproject.getProjectDirectory().getNameExt());
                    config.setExecutionName(label);
                    config.setTaskDisplayName(label);
                    
                    MavenProxySupport mps = nbproject.getLookup().lookup(MavenProxySupport.class);
                    if (mps != null) {
                        ProxyResult res;
                        try {
                            res = mps.checkProxySettings().get();
                            if (res.getStatus() == MavenProxySupport.Status.ABORT) {
                                ProjectProblemsProvider.Result r = ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.UNRESOLVED, ERR_SanityBuildCancalled());
                                publicResult.complete(r);
                                return;
                            }
                        } catch (ExecutionException ex) {
                            ProjectProblemsProvider.Result r = ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.UNRESOLVED, ERR_ProxyUpdateFailed(ex.getLocalizedMessage()));
                            publicResult.complete(r);
                            return;
                        } catch (InterruptedException ex) {
                            ProjectProblemsProvider.Result r = ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.UNRESOLVED, ERR_SanityBuildCancalled());
                            publicResult.complete(r);
                            return;
                        }
                    }
                    long t = System.currentTimeMillis();
                    LOG.log(Level.FINE, "Executing sanity build: goals = {0}, properties = {1}", new Object[] { config.getGoals(), config.getProperties() });
                    ExecutorTask et = RunUtils.run(config);
                    if (et != null) {
                        et.waitFinished();
                        ProjectProblemsProvider.Result r = ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED, ACT_start_validate());
                        LOG.log(Level.FINE, "Sanity build of {0} finished, took {1} ms.", new Object[] { nbproject, System.currentTimeMillis() - t});
                        publicResult.complete(r);
                    }
                } catch (RuntimeException | Error e) {
                    // always report completness, otherwise tasks that wait on priming build could block indefinitely.
                    LOG.log(Level.FINE, "Sanity build failed", e);
                    publicResult.completeExceptionally(e);
                    throw e;
                }
            }
        };
        synchronized (this) {
            if (pendingResult != pr) {
                // someone has started or completed the pending result before us: back off, use the
                // existing one.
                return pendingResult;
            }
            pendingResult = publicResult;
        }
        ((NbMavenProjectImpl)this.nbproject).scheduleProjectOperation(MavenModelProblemsProvider.RP, toRet, 0);
        return publicResult;
    }
    
    @Override
    public int hashCode() {
        int hash = SanityBuildAction.class.hashCode();
        hash = 67 * hash + (this.nbproject != null ? this.nbproject.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SanityBuildAction other = (SanityBuildAction) obj;
        if (this.nbproject != other.nbproject && (this.nbproject == null || !this.nbproject.equals(other.nbproject))) {
            return false;
        }
        return true;
    }

    
}
