/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.fish.payara.micro.project;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.COMPILE_EXPLODE_ACTION;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.EXPLODE_ACTION;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.RELOAD_FILE;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.RELOAD_ICON;
import org.netbeans.modules.fish.payara.micro.project.MicroApplication;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunUtils;
import static org.netbeans.modules.maven.api.execute.RunUtils.isCompileOnSaveEnabled;
import org.netbeans.modules.maven.execute.ActionToGoalUtils;
import org.netbeans.modules.maven.execute.ModelRunConfig;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import static org.openide.util.NbBundle.getMessage;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Gaurav Gupta <gaurav.gupta@payara.fish>
 */
@ActionID(
        id = ReloadAction.ID,
        category = ReloadAction.CATEGORY
)
@ActionRegistration(
        displayName = "#CTL_ReloadAppAction",
        iconBase = RELOAD_ICON,
        lazy = true
)
@ActionReferences({
    @ActionReference(path = "Menu/BuildProject", position = 55)
    ,
    @ActionReference(path = "Toolbars/Build", position = 325)
    ,
    @ActionReference(path = "Projects/org-netbeans-modules-maven/Actions", position = 1000)
    ,
    @ActionReference(path = "Shortcuts", name = "DS-A")
})
@Messages("CTL_ReloadAppAction=Reload")
public class ReloadAction extends AbstractAction {

    static final String CATEGORY = "Build";

    static final String ID = "org.netbeans.modules.payara.micro.action.reload";

    private static final RequestProcessor RP = new RequestProcessor(ReloadAction.class.getName());

    @Override
    public void actionPerformed(ActionEvent e) {
        Lookup context = Utilities.actionsGlobalContext();
        Project project = context.lookup(Project.class);
        if(project == null) {
            return;
        }
        NbMavenProject nbMavenProject = project.getLookup().lookup(NbMavenProject.class);
        MavenProject mavenProject = nbMavenProject.getMavenProject();
        
        MicroApplication microApplication = MicroApplication.getInstance(project);
        if (microApplication == null) {
            StatusDisplayer.getDefault()
                    .setStatusText(getMessage(ReloadAction.class, "ERR_Payara_Micro_Plugin_Not_Found", mavenProject.getArtifactId()));
        } else if (!isCompileOnSaveEnabled(microApplication.getProject())) {
            StatusDisplayer.getDefault()
                    .setStatusText(getMessage(ReloadAction.class, "ERR_Compile_On_Save_Not_Enabled", mavenProject.getArtifactId()));
        } else {
            RP.post(() -> {
                String action = RunUtils.isCompileOnSaveEnabled(project) ? EXPLODE_ACTION : COMPILE_EXPLODE_ACTION;
                NetbeansActionMapping mapping = ActionToGoalUtils.getDefaultMapping(action, project);
                ModelRunConfig rc = new ModelRunConfig(project, mapping, mapping.getActionName(), null, Lookup.EMPTY, false);
                rc.setTaskDisplayName(getMessage(ReloadAction.class, "TXT_Reload", mavenProject.getArtifactId()));
                rc.getGoals().addAll(MicroActionsProvider.getGoals(action));
                RunUtils.run(rc);
            });
        }
    }

    public static void reloadApplication(String buildPath) {
        File check = new File(buildPath, RELOAD_FILE);
        if (check.exists()) {
            check.setLastModified(System.currentTimeMillis());
        } else {
            try {
                check.createNewFile();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }


}
