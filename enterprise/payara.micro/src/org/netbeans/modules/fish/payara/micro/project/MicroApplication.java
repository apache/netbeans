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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.PAYARA_MICRO_MAVEN_PLUGIN;
import org.netbeans.modules.maven.api.NbMavenProject;

/**
 *
 * @author Gaurav Gupta <gaurav.gupta@payara.fish>
 */
public class MicroApplication {

    private final Project project;
    
    private final MavenProject mavenProject;
    
    private ActionType buildActionType;
    
    private boolean buildAction;

    private ActionType runActionType;

    private final AtomicInteger runActionCount = new AtomicInteger();

    private boolean reloadAction;
    
    private static final String SINGLE = ".single.deploy";
    
    public MicroApplication(Project project) {
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
        this.buildActionType = ActionType.toAction(actionType.replace("-", "_"));
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
        this.runActionType = ActionType.toAction(actionType.replace(SINGLE, ""));
        setRunning(running);
    }

    public void setRunning(boolean running) {
        if (running) {
            runActionCount.incrementAndGet();
        } else if (isRunning()) { // skip negative decrement
            runActionCount.decrementAndGet();
        }
        NbMavenProject.fireMavenProjectReload(project);
    }

    public boolean isRunning() {
//        runActionCount.getAndSet(calcRunningInstanceCount());
        return runActionCount.get() > 0;
    }

    private int calcRunningInstanceCount() {
        System.out.println("calcRunningInstanceCount");
        List<String> processIds = new ArrayList<>();
        String executorFilter = "gav=" + mavenProject.getGroupId() + ":" + mavenProject.getArtifactId() + ":" + mavenProject.getVersion();
        final Runtime re = Runtime.getRuntime();
        try {
            Process jpsProcess = re.exec("jps -v");
            InputStream inputStream = jpsProcess.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = in.readLine()) != null) {
                if (line.contains(executorFilter)) {
                    String[] split = line.split(" ");
                    processIds.add(split[0]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return processIds.size();
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
    
    public static MicroApplication getInstance(Project project) {
        MicroApplication microApplication = null;
        if (project != null) {
           MicroApplicationContent content = project.getLookup().lookup(MicroApplicationContent.class);
           microApplication = content.getMicroApplication();
        }
        return microApplication;
    }
    
    public static MicroApplication registerInstance(Project project) {
        MicroApplication microApplication = null;
        if (project != null) {
            MicroApplicationContent content = project.getLookup().lookup(MicroApplicationContent.class);
            if(content != null) {
                microApplication = new MicroApplication(project);
                content.setMicroApplication(microApplication);
            }
        }
        return microApplication;
    }
    
    public static boolean isPayaraMicroProject(Project project) {
        NbMavenProject nbMavenProject = project.getLookup().lookup(NbMavenProject.class);
        MavenProject mavenProject = nbMavenProject.getMavenProject();
        return mavenProject.getPluginArtifactMap()
                .get(PAYARA_MICRO_MAVEN_PLUGIN) != null;
    }
    
}
