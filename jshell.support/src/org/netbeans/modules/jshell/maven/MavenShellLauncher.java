/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.maven;

import java.io.IOException;
import java.util.List;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jshell.launch.ShellAgent;
import org.netbeans.modules.jshell.launch.ShellLaunchManager;
import org.netbeans.modules.jshell.launch.PropertyNames;
import org.netbeans.modules.jshell.project.LaunchedProjectOpener;
import org.netbeans.modules.jshell.project.ShellProjectUtils;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.ExecutionResultChecker;
import org.netbeans.modules.maven.api.execute.LateBoundPrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author sdedic
 */
@ProjectServiceProvider(
    projectType = "org-netbeans-modules-maven",
    service = {
        LateBoundPrerequisitesChecker.class,
        PrerequisitesChecker.class,
        ExecutionResultChecker.class
    }
)
public class MavenShellLauncher implements PrerequisitesChecker, LateBoundPrerequisitesChecker, ExecutionResultChecker {
    private static final String PLACEHOLDER_JSHELL_ARGS = "${jshell.args}"; // NOI18N
    private static final String PROPERTY_EXEC_ARGS = "exec.args"; // NOI18N
    private static final String PROPERTY_JSHELL_KEY = "jshell.authKey"; // NOI18N
    private static final String PROPERTY_JSHELL_AGENT = "jshell.agent"; // NOI18N
    
    @Override
    public boolean checkRunConfig(RunConfig config) {
        return true;
    }

    @Override
    public void executionResult(RunConfig config, ExecutionContext res, int resultCode) {
        String s = config.getProperties().get(PROPERTY_JSHELL_KEY);
        if (s == null) {
            return;
        }
        RequestProcessor.getDefault().post(() -> {
            ShellLaunchManager.getInstance().destroyAgent(s);
        });
    }

    @Override
    public boolean checkRunConfig(RunConfig config, ExecutionContext con) {
        boolean enabled = Boolean.parseBoolean(config.getProperties().get(PropertyNames.JSHELL_ENABLED));
        if (!enabled) {
            return true;
        }
        LaunchedProjectOpener.init();
        
        Project project = config.getProject();
        boolean isDebug = config.getActionName().equals("debug");
        boolean ok = isDebug ||
                config.getActionName().equals("run");
        
        if (!ok) {
            return true;
        }
        ShellAgent agent;
        ShellLaunchManager mgr = ShellLaunchManager.getInstance();
        try {
            agent = mgr.openForProject(project, isDebug);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
        agent.setIO(con.getInputOutput(), config.getTaskDisplayName());
        JavaPlatform platform = ShellProjectUtils.findPlatform(project);
        List<String> args = ShellLaunchManager.buildLocalJVMAgentArgs(
                platform,
                agent, 
                config.getProperties()::get
        );
        String execArgs = config.getProperties().get(PROPERTY_EXEC_ARGS);
        if (execArgs != null) {
            StringBuilder sb = new StringBuilder();
            for (String a : args) {
                if (sb.length() > 0) {
                    sb.append(" "); // NOI18N
                }
                sb.append(a);
            }
            String newArgs;

            if (execArgs.contains(PLACEHOLDER_JSHELL_ARGS)) {
                newArgs = execArgs.replace(PLACEHOLDER_JSHELL_ARGS, sb.toString());
            } else {
                newArgs = sb.append(" ").append(execArgs).toString(); // NOI18N
            }
            config.setProperty(PROPERTY_EXEC_ARGS, newArgs);
        }        
        config.setProperty(PROPERTY_JSHELL_AGENT, args.get(args.size() -1));
        config.setProperty(PROPERTY_JSHELL_KEY, agent.getAuthorizationKey());
        return true;
    }
    
}
