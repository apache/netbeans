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

import static org.netbeans.modules.fish.payara.micro.plugin.Constants.MAVEN_WAR_PROJECT_TYPE;
import java.io.InputStream;
import java.util.Set;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider;
import org.netbeans.modules.maven.spi.actions.MavenActionsProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Gaurav Gupta <gaurav.gupta@payara.fish>
 */
@ProjectServiceProvider(
        service = MavenActionsProvider.class,
        projectType = MAVEN_WAR_PROJECT_TYPE
)
public final class MicroActionsProvider extends AbstractMavenActionsProvider {

    @StaticResource
    private static final String ACTION_MAPPINGS = "org/netbeans/modules/fish/payara/micro/project/resources/action-mapping.xml";

    private final Project project;

    public MicroActionsProvider(Project project) {
        this.project = project;
    }
    
    @Override
    public boolean isActionEnable(String action, Project project, Lookup lookup) {
        if ("micro-explode".equals(action)) {
            MicroApplication ma = MicroApplication.getInstance(project);
            return (ma != null) && ma.isRunning();
        }
        return super.isActionEnable(action, project, lookup);
    }

    @Override
    public Set<String> getSupportedDefaultActions() {
        Set<String> ret = super.getSupportedDefaultActions();
        MicroApplication ma = MicroApplication.getInstance(project);
        if ((ma != null) && ma.isRunning()) {
            ret.add(Actions.COMMAND_MICRO_RELOAD);
        }
        return ret;
    }

    
    @Override
    protected InputStream getActionDefinitionStream() {
        return MicroActionsProvider.class.getClassLoader().getResourceAsStream(ACTION_MAPPINGS);
    }

}