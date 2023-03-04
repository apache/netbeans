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
package org.netbeans.modules.jshell.project;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jshell.env.JShellEnvironment;
import org.netbeans.modules.jshell.env.ShellRegistry;
import org.netbeans.modules.jshell.launch.ShellAgent;
import org.netbeans.modules.jshell.launch.ShellLaunchManager;
import org.netbeans.spi.project.ui.support.MainProjectSensitiveActions;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
public class OpenRunningShellAction implements ProjectActionPerformer {

    @Override
    public boolean enable(Project project) {
        Collection<ShellAgent> agents = ShellLaunchManager.getInstance().getLiveAgents(project);
        return !agents.isEmpty();
    }

    @Override
    public void perform(Project project) {
        Collection<ShellAgent> agents = ShellLaunchManager.getInstance().getLiveAgents(project);
        Set<ShellAgent> waiting = new HashSet<>(agents);
        Collection<JShellEnvironment> envs = ShellRegistry.get().openedShells(project);
        for (JShellEnvironment e : envs) {
            ShellAgent a = LaunchedProjectOpener.get().getProjectAgent(e);
            if (a != null) {
                waiting.remove(a);
            }
        }
        if (!waiting.isEmpty()) {
            ShellAgent selected = waiting.iterator().next();
            LaunchedProjectOpener.get().openAgentShell(selected);
        } else if (!envs.isEmpty()) {
            try {
                envs.iterator().next().open();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
    }
    
    @NbBundle.Messages({
            "# {0} - type of message",
            "# {1} - 1st projct name",
            "LBL_OpenShellForMainProject=&Open Java Shell for {0,choice,-1#Main Project|0#Project|1#Project ({1})|1<{0} Projects}"
    })
    public static Action action() {
        Action a = MainProjectSensitiveActions.mainProjectSensitiveAction(new OpenRunningShellAction(), 
                NbBundle.getMessage(OpenRunningShellAction.class, "LBL_OpenShellForMainProject"), null);
        a.putValue("iconBase", "org/netbeans/modules/jshell/resources/jshell-terminal.png");
        return a; 
    }
}
