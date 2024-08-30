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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import static org.netbeans.api.project.ProjectUtils.getPreferences;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.COMPILE_EXPLODE_ACTION;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.EXPLODE_ACTION;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.HOT_DEPLOY;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.RELOAD_FILE;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.RELOAD_ICON;
import org.netbeans.modules.fish.payara.micro.project.DeployOnSaveManager.DeployArtifact;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener;
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
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
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
    @ActionReference(path = "Menu/BuildProject", position = 55),
    @ActionReference(path = "Projects/org-netbeans-modules-maven/Actions", position = 1020),
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
        if (project == null) {
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
                RunUtils.run(rc);
            });
        }
    }

    public static void reloadApplication(String buildPath, DeployArtifact deployArtifact) {
        try {
            boolean metadataChanged = false;
            List<String> sourcesChanged = new ArrayList<>();
            FileObject destRoot = FileUtil.createFolder(new File(buildPath));
            if (deployArtifact != null) {
                for (ArtifactListener.Artifact artifact : deployArtifact.getArtifacts()) {
                    File altDistFile = artifact.getDistributionPath();
                    FileObject checkFile = FileUtil.toFileObject(FileUtil.normalizeFile(altDistFile));
                    String relative = FileUtil.getRelativePath(destRoot, checkFile);
                    sourcesChanged.add(relative);
                    if (checkFile.getExt().equals("xml") || checkFile.getExt().equals("properties")) {
                        metadataChanged = true;
                    }
                }
            }

            Preferences pref = getPreferences(deployArtifact.getProject(), MicroApplication.class, true);
            Boolean hotDeploy = pref.getBoolean(HOT_DEPLOY, false);
            File reloadFile = new File(buildPath, RELOAD_FILE);
            if (hotDeploy) {
                Properties props = new Properties();
                props.setProperty("hotdeploy", "true");
                if (metadataChanged) {
                    props.setProperty("metadatachanged", "true");
                }
                if (!sourcesChanged.isEmpty()) {
                    props.setProperty("sourceschanged", String.join(",", sourcesChanged));
                }
                try (FileOutputStream outputStrem = new FileOutputStream(reloadFile)) {
                    props.store(outputStrem, null);
                } catch (Exception ex) {
                    throw new IllegalStateException("Unable to save .reload file " + ex.toString());
                }
            } else if (reloadFile.exists()) {
                try (PrintWriter pw = new PrintWriter(reloadFile)) {
                } catch (FileNotFoundException ex) {
                    throw new IllegalStateException("Unable to find .reload file " + ex.toString());
                }
                reloadFile.setLastModified(System.currentTimeMillis());
            } else {
                try {
                    reloadFile.createNewFile();
                } catch (IOException ex) {
                    throw new IllegalStateException("Unable to create .reload file " + ex.toString());
                }
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
