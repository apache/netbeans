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

import static org.netbeans.modules.fish.payara.micro.plugin.Constants.COMMAND_EXPLODE;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.COMPILE_EXPLODE_ACTION;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.DEBUG_SINGLE_ACTION;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.EXPLODE_ACTION;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.MAVEN_WAR_PROJECT_TYPE;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.PROFILE_SINGLE_ACTION;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.RUN_SINGLE_ACTION;
import java.io.File;
import static java.util.Arrays.asList;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.ExecutionResultChecker;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import static org.netbeans.spi.project.ActionProvider.COMMAND_BUILD;
import static org.netbeans.spi.project.ActionProvider.COMMAND_CLEAN;
import static org.netbeans.spi.project.ActionProvider.COMMAND_DEBUG;
import static org.netbeans.spi.project.ActionProvider.COMMAND_PROFILE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_REBUILD;
import static org.netbeans.spi.project.ActionProvider.COMMAND_RUN;
import org.netbeans.spi.project.ProjectServiceProvider;

/**
 *
 * @author Gaurav Gupta <gaurav.gupta@payara.fish>
 */
@ProjectServiceProvider(
        service = {
            ExecutionResultChecker.class,
            PrerequisitesChecker.class
        }, 
        projectType = MAVEN_WAR_PROJECT_TYPE
)
public class ExecutionChecker implements ExecutionResultChecker, PrerequisitesChecker {
    
    private static final String COMMAND_BUILD_WITH_DEPENDENCIES = "build-with-dependencies";
    
    private static final Set<String> BUILD_ACTIONS = new HashSet<>(asList(new String[]{
        COMMAND_CLEAN, 
        COMMAND_BUILD, 
        COMMAND_REBUILD,
        COMMAND_BUILD_WITH_DEPENDENCIES,
        COMPILE_EXPLODE_ACTION, 
        EXPLODE_ACTION
    }));
    
    private static final Set<String> RUN_ACTIONS = new HashSet<>(asList(new String[]{
        COMMAND_RUN, 
        COMMAND_DEBUG, 
        COMMAND_PROFILE, 
        RUN_SINGLE_ACTION, 
        DEBUG_SINGLE_ACTION,
        PROFILE_SINGLE_ACTION
    }));
    
    
    @Override
    public boolean checkRunConfig(RunConfig config) {
        Project project = config.getProject();
        MicroApplication microApplication = MicroApplication.getInstance(project);
        if (microApplication != null) {
            if (BUILD_ACTIONS.contains(config.getActionName())) {
                microApplication.setBuilding(true, config.getActionName());
            }else if (RUN_ACTIONS.contains(config.getActionName())) {
                microApplication.setRunning(true, config.getActionName());
            }
        }
        return true;
    }

    @Override
    public void executionResult(RunConfig config, ExecutionContext res, int resultCode) {
        Project project = config.getProject();
        MicroApplication microApplication = MicroApplication.getInstance(project);
        if (microApplication != null) {
            if (BUILD_ACTIONS.contains(config.getActionName())) {
                if(config.getActionName().contains(COMMAND_BUILD) 
                        || config.getActionName().contains(COMMAND_EXPLODE) ){
                    reloadApplication(microApplication);
                }
                microApplication.setBuilding(false);
            } else if (RUN_ACTIONS.contains(config.getActionName())) {
                microApplication.setRunning(false);
            }
        }
    }
    
    public static void reloadApplication(MicroApplication application) {
        if (!application.isRunning()) {
            return;
        }
        String buildPath = application.getMavenProject().getBuild().getDirectory()
                + File.separator
                + application.getMavenProject().getBuild().getFinalName();
        ReloadAction.reloadApplication(buildPath);
    }
    

}
