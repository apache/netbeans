/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.problems;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
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

    public SanityBuildAction(Project nbproject) {
        this.nbproject = nbproject;
    }

    @Override
    public Future<ProjectProblemsProvider.Result> resolve() {
        FutureTask<ProjectProblemsProvider.Result> toRet = new FutureTask<ProjectProblemsProvider.Result>(new Callable<ProjectProblemsProvider.Result>() {
            @Override
            public ProjectProblemsProvider.Result call() throws Exception {
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
                RunUtils.run(config);
                return ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED, ACT_start_validate());
            }
        });
        MavenModelProblemsProvider.RP.execute(toRet);
        return toRet;
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
