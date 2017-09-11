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
