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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
