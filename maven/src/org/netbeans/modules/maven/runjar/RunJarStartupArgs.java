/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.runjar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.ActionProviderImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.ActiveJ2SEPlatformProvider;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.LateBoundPrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.execute.BeanRunConfig;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

@ProjectServiceProvider(service=LateBoundPrerequisitesChecker.class, projectType="org-netbeans-modules-maven/" + NbMavenProject.TYPE_JAR)
public class RunJarStartupArgs implements LateBoundPrerequisitesChecker {

    @Override public boolean checkRunConfig(RunConfig config, ExecutionContext con) {
        String actionName = config.getActionName();
        StartupExtender.StartMode mode;
        if (ActionProvider.COMMAND_RUN.equals(actionName) || ActionProviderImpl.RUN_MAIN.equals(actionName)) {
            mode = StartupExtender.StartMode.NORMAL;
        } else if (ActionProvider.COMMAND_DEBUG.equals(actionName) || ActionProviderImpl.DEBUG_MAIN.equals(actionName)) {
            mode = StartupExtender.StartMode.DEBUG;
        } else if (ActionProvider.COMMAND_PROFILE.equals(actionName) || ActionProvider.COMMAND_PROFILE_SINGLE.equals(actionName) || ActionProviderImpl.PROFILE_MAIN.equals(actionName)) {
            mode = StartupExtender.StartMode.PROFILE;
        } else if (ActionProvider.COMMAND_PROFILE_TEST_SINGLE.equals(actionName)) {
            mode = StartupExtender.StartMode.TEST_PROFILE;
        } else {
            // XXX could also set argLine for COMMAND_TEST and relatives (StartMode.TEST_*); need not be specific to TYPE_JAR
            return true;
        }
        boolean isTestScope = false;
        for (Map.Entry<? extends String, ? extends String> entry : config.getProperties().entrySet()) {
            if (entry.getKey().equals("exec.args")) {
                List<String> args = new ArrayList<String>();
                InstanceContent ic = new InstanceContent();
                Project p = config.getProject();
                if (p != null) {
                    ic.add(p);
                    ActiveJ2SEPlatformProvider pp = p.getLookup().lookup(ActiveJ2SEPlatformProvider.class);
                    if (pp != null) {
                        ic.add(pp.getJavaPlatform());
                    }
                }
                for (StartupExtender group : StartupExtender.getExtenders(new AbstractLookup(ic), mode)) {
                    args.addAll(group.getArguments());
                }
                if (!args.isEmpty()) {
                    StringBuilder b = new StringBuilder();
                    for (String arg : args) {
                        b.append(arg).append(' ');
                    }
                    b.append(entry.getValue());
                    config.setProperty(entry.getKey(), b.toString());
                }
            }
            if (entry.getKey().equals("exec.classpathScope") && "test".equals(entry.getValue())) {
                isTestScope = true;
            }
        }
        if (isTestScope) { //#230190
            String[] goals = config.getGoals().toArray(new String[0]);
            for (int i = 0; i < goals.length;i++) {
                if ("process-classes".equals(goals[i])) {
                    goals[i] = "process-test-classes";
                }
            }
            ((BeanRunConfig)config).setGoals(new ArrayList<String>(Arrays.asList(goals)));
        }
        return true;
    }

}
