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

package org.netbeans.modules.maven.apisupport;

import java.io.IOException;
import java.util.Arrays;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.spi.ExecProject;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import static org.netbeans.modules.maven.apisupport.Bundle.*;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Task;

@ProjectServiceProvider(service=ExecProject.class, projectType="org-netbeans-modules-maven/" + NbMavenProject.TYPE_NBM)
public class MavenExecProject implements ExecProject {

    private final Project p;

    public MavenExecProject(Project p) {
        this.p = p;
    }

    @Messages({"# {0} - project display name", "MavenExecProject_run=Run {0}"})
    @Override public Task execute(String... args) throws IOException {
        Project app = MavenNbModuleImpl.findAppProject(p);
        if (app == null) {
            NbMavenProject prj = p.getLookup().lookup(NbMavenProject.class);
            throw new IOException("No open nbm-application project found to contain " + prj.getMavenProject().getId() + ". Please open the aplication project and try again.");
        }
        // XXX build w/ deps first? to do so, RP.post a Runnable which does both actions and calls result()
        RunConfig cfg = RunUtils.createRunConfig(FileUtil.toFile(app.getProjectDirectory()), app,
                // cf. platformActionMappings.xml
                MavenExecProject_run(ProjectUtils.getInformation(app).getDisplayName()), Arrays.asList("install", "nbm:run-platform"));
        StringBuilder argsS = new StringBuilder();
        for (String arg : args) {
            if (argsS.length() > 0) {
                argsS.append(' ');
            }
            argsS.append(arg);
        }
        NbMavenProject appPrj = app.getLookup().lookup(NbMavenProject.class);
        if (NetBeansRunParamsIDEChecker.usingNbmPlugin311(appPrj.getMavenProject())) {
            cfg.setProperty(NetBeansRunParamsIDEChecker.PROPERTY, argsS.toString());
        } else {
            cfg.setProperty(NetBeansRunParamsIDEChecker.OLD_PROPERTY, argsS.toString());
        }
            
        return RunUtils.run(cfg);
    }

}
