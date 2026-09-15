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
package org.netbeans.modules.payara.server.maven.project;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import static org.netbeans.modules.payara.server.maven.plugin.Constants.PAYARA_SERVER_MAVEN_PLUGIN;
import static org.netbeans.modules.payara.server.maven.plugin.Constants.PLUGIN_ARTIFACT_ID;
import static org.netbeans.modules.payara.server.maven.plugin.Constants.PLUGIN_GROUP_ID;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.openide.execution.ExecutorTask;

/**
 * @author Gaurav Gupta <gaurav.gupta@azul.com>
 */
public class ServerMavenApplication {

    private final Project project;

    private final MavenProject mavenProject;

    private volatile ActionType buildActionType;

    private volatile boolean buildAction;

    private volatile ActionType runActionType;

    private final AtomicInteger runActionCount = new AtomicInteger();

    private volatile boolean reloadAction;

    private final List<ExecutorTask> runningTasks = new ArrayList<>();

    private static final String SINGLE = ".single.deploy";

    public ServerMavenApplication(Project project) {
        this.project = project;
        NbMavenProject nbMavenProject = project.getLookup().lookup(NbMavenProject.class);
        this.mavenProject = nbMavenProject.getMavenProject();
    }

    public Project getProject() {
        return project;
    }

    public MavenProject getMavenProject() {
        return mavenProject;
    }

    public void setBuilding(boolean buildAction, String actionType) {
        ActionType at = ActionType.toAction(actionType.replace("-", "_"));
        this.buildActionType = at;
        setBuilding(buildAction);
    }

    public void setBuilding(boolean buildAction) {
        this.buildAction = buildAction;
        NbMavenProject.fireMavenProjectReload(project);
    }

    public ActionType getBuildActionType() {
        return buildActionType;
    }

    public boolean isBuilding() {
        return buildAction;
    }

    public void setRunning(boolean running, String actionType) {
        ActionType at = ActionType.toAction(actionType.replace(SINGLE, ""));
        this.runActionType = at;
        setRunning(running);
    }

    public void setRunning(boolean running) {
        if (running) {
            runActionCount.incrementAndGet();
        } else if (isRunning()) {
            runActionCount.decrementAndGet();
        }
        NbMavenProject.fireMavenProjectReload(project);
    }

    public boolean isRunning() {
        return runActionCount.get() > 0;
    }

    public int getRunningInstanceCount() {
        return runActionCount.get();
    }

    public ActionType getRunActionType() {
        return runActionType;
    }

    public void setLoading(boolean loading) {
        this.reloadAction = loading;
        NbMavenProject.fireMavenProjectReload(project);
    }

    public boolean isLoading() {
        return reloadAction;
    }

    public synchronized void addRunningTask(ExecutorTask task) {
        runningTasks.add(task);
    }

    public synchronized void stopAllRunningTasks() {
        for (ExecutorTask task : runningTasks) {
            task.stop();
        }
        runningTasks.clear();
    }

    public static ServerMavenApplication getInstance(Project project) {
        if (project != null) {
            ServerMavenApplicationContent content = project.getLookup().lookup(ServerMavenApplicationContent.class);
            if (content != null) {
                return content.getApplication();
            }
        }
        return null;
    }

    public static boolean isPayaraServerMavenProject(Project project) {
        if (project == null) {
            return false;
        }
        NbMavenProject nbMavenProject = project.getLookup().lookup(NbMavenProject.class);
        if (nbMavenProject == null) {
            return false;
        }
        MavenProject mp = nbMavenProject.getMavenProject();
        // Check resolved artifacts first (populated after Maven resolution)
        if (mp.getPluginArtifactMap().containsKey(PAYARA_SERVER_MAVEN_PLUGIN)) {
            return true;
        }
        // Check declared build plugins (populated from the POM model, no resolution needed)
        return mp.getBuildPlugins().stream()
                .anyMatch(p -> PLUGIN_GROUP_ID.equals(p.getGroupId())
                            && PLUGIN_ARTIFACT_ID.equals(p.getArtifactId()));
    }
}
