/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
