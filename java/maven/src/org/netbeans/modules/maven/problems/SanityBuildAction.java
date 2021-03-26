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
import java.util.concurrent.Future;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.TestChecker;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig.ReactorStyle;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.execute.BeanRunConfig;
import static org.netbeans.modules.maven.problems.Bundle.*;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;

/**
 * Corrective action to run some target which can download plugins or parent POMs.
 * At worst it will show the same problem in the Output Window, so the user is more likely
 * to believe that there really is a problem with their project, not NetBeans.
 */
@Messages({"ACT_validate=Priming Build",
            "ACT_start_validate=Priming build was started."})
public class SanityBuildAction implements ProjectProblemResolver {

    private final Project nbproject;
    
    /**
     * The priming build, which is currently pending or recently completed.
     * Initially empty.
     */
    private volatile CompletableFuture<ProjectProblemsProvider.Result> pendingResult;

    public SanityBuildAction(Project nbproject) {
        this.nbproject = nbproject;
    }

    public SanityBuildAction(Project nbproject, Future<ProjectProblemsProvider.Result> otherResult) {
        this.nbproject = nbproject;
    }
    
    public Future<ProjectProblemsProvider.Result> getPendingResult() {
        return this.pendingResult;
    }

    @Override
    public CompletableFuture<ProjectProblemsProvider.Result> resolve() {
        CompletableFuture<ProjectProblemsProvider.Result> pr = pendingResult;
        if (pr != null && !pr.isDone()) {
            return pendingResult;
        }
        final CompletableFuture<ProjectProblemsProvider.Result> publicResult = new CompletableFuture<>();
        
        Runnable toRet = new Runnable() {
            @Override
            public void run() {
                try {
                    BeanRunConfig config = new BeanRunConfig();
                    config.setExecutionDirectory(FileUtil.toFile(nbproject.getProjectDirectory()));
                    NbMavenProject mavenPrj = nbproject.getLookup().lookup(NbMavenProject.class);
                    if (mavenPrj != null
                            && mavenPrj.getMavenProject().getVersion() != null 
                            && mavenPrj.getMavenProject().getVersion().endsWith("SNAPSHOT")) {
                        config.setGoals(Arrays.asList("--fail-at-end", "install")); // NOI18N
                    } else {
                        config.setGoals(Arrays.asList("--fail-at-end", "package")); // NOI18N
                    }
                    config.setReactorStyle(ReactorStyle.ALSO_MAKE);
                    config.setProperty(TestChecker.PROP_SKIP_TEST, "true"); //priming doesn't need test execution, just compilation
                    config.setProject(nbproject);
                    String label = build_label(nbproject.getProjectDirectory().getNameExt());
                    config.setExecutionName(label);
                    config.setTaskDisplayName(label);
                    ExecutorTask et = RunUtils.run(config);
                    et.addTaskListener(t -> {
                        ProjectProblemsProvider.Result r = ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED, ACT_start_validate());
                        publicResult.complete(r);
                    });
                } catch (RuntimeException | Error e) {
                    // always report completness, otherwise tasks that wait on priming build could block indefinitely.
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
        MavenModelProblemsProvider.RP.submit(toRet);
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
