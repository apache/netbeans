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

import org.codehaus.plexus.util.DirectoryScanner;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.core.startup.TestModuleDeployer;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.ExecutionResultChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service=ExecutionResultChecker.class, projectType="org-netbeans-modules-maven/" + NbMavenProject.TYPE_NBM)
public class ExecutionChecker implements ExecutionResultChecker {

    private Project project;

    public ExecutionChecker(Project prj) {
        project = prj;
    }

    @Override
    public void executionResult(RunConfig config, ExecutionContext res, int resultCode) {

        if (NbmActionGoalProvider.NBMRELOAD.equals(config.getActionName()) && resultCode == 0) {
            DirectoryScanner scanner = new DirectoryScanner();
            NbMavenProject prj = project.getLookup().lookup(NbMavenProject.class);
            File basedir = new File(prj.getMavenProject().getBuild().getDirectory(), "nbm"); //NOI18N
            scanner.setBasedir(basedir);
            scanner.setIncludes(new String[]{
                        "**/modules/*.jar", //NOI18N
                        "**/modules/eager/*.jar", //NOI18N
                        "**/modules/autoload/*.jar" //NOI18N
                    });
            scanner.scan();
            String[] incl = scanner.getIncludedFiles();
            if (incl != null && incl.length > 0) {
                if (incl[0].indexOf("eager") > -1 || incl[0].indexOf("autoload") > -1) { //NOI18N
                    res.getInputOutput().getErr().println("NetBeans: Cannot reload 'autoload' or 'eager' modules.");
                }
                try {
                    res.getInputOutput().getOut().println("NetBeans: Deploying NBM module in development IDE...");
                    TestModuleDeployer.deployTestModule(FileUtil.normalizeFile(new File(basedir, incl[0])));
                } catch (IOException ex) {
                    res.getInputOutput().getOut().println("NetBeans: Error redeploying NBM module in development IDE.");
                    Logger.getLogger(ExecutionChecker.class.getName()).log(Level.INFO, "Error reloading netbeans module in development IDE.", ex); //NOI18N
                }
            } else {
                res.getInputOutput().getErr().println("NetBeans: Cannot find any built NetBeans Module artifacts for reload.");
            }

        }
    }
}
