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
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jshell.env.JShellEnvironment;
import org.netbeans.modules.jshell.env.ShellRegistry;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lahvac
 */
public class REPLAction2 implements ProjectActionPerformer {

    @ActionID(category="Project", id="org.netbeans.modules.java.repl.REPLAction2")
//    @ActionReference(path = "Menu/BuildProject", position = 93)
    @ActionRegistration(
            displayName="#DN_ProjectJavaRun",
            iconBase = "org/netbeans/modules/jshell/resources/jshell-terminal.png"
    )
    @Messages({
        "DN_ProjectJavaRun=Execute Java Shell"
    })
    public static Action create() {
        return ProjectSensitiveActions.projectSensitiveAction(new REPLAction2(), Bundle.DN_ProjectJavaRun(), null);
    }

    @Override
    public boolean enable(Project project) {
        if (ShellProjectUtils.findPlatform(project) == null) {
            return false;
        }
        ActionProvider p = project.getLookup().lookup(ActionProvider.class);
        if (p == null) {
            return false;
        }
        return p.isActionEnabled(ActionProvider.COMMAND_BUILD, Lookups.singleton(project));
    }

    @NbBundle.Messages({
        "ERR_CannotBuildProject=Could not build the project",
        "ERR_ProjectBuildFailed=Project build failed, please check Output window",
    })
    @Override
    public void perform(Project project) {
        ActionProvider p = project.getLookup().lookup(ActionProvider.class);
        // check whether the is CoS enabled fo the project
        if (ShellProjectUtils.isCompileOnSave(project)) {
            doRunShell(project);
            return;
        }
        if (p == null || !p.isActionEnabled(ActionProvider.COMMAND_BUILD, Lookups.singleton(project))) {
            StatusDisplayer.getDefault().setStatusText(Bundle.ERR_CannotBuildProject());
            return;
        }
        p.invokeAction(ActionProvider.COMMAND_BUILD, Lookups.fixed(project, new ActionProgress() {
            @Override
            protected void started() {
                // no op
            }

            @Override
            public void finished(boolean success) {
                if (success) {
                    doRunShell(project);
                } else {
                    StatusDisplayer.getDefault().setStatusText(Bundle.ERR_ProjectBuildFailed());
                }
            }
        }));
    }
    
    private void doRunShell(Project project) {
        JShellEnvironment env;
        try {
            env = ShellRegistry.get().openProjectSession(project);
            env.open();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public static Action contextAction() {
        Action a = ProjectSensitiveActions.projectSensitiveAction(new REPLAction2(), 
                Bundle.DN_ProjectJavaRun(), null);
        a.putValue("iconBase", "org/netbeans/modules/jshell/resources/jshell-terminal.png");
        return a;
    }
}
