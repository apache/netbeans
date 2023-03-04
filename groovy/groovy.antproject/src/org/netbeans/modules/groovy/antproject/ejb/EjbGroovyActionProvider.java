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

package org.netbeans.modules.groovy.antproject.ejb;

import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.antproject.base.AbstractGroovyActionProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;

/**
 * Implementation of the ActionProvider for Ant based Java EE projects with groovy files.
 *
 * This enables to customize some ant targets (e.g. used for running mixed Java/Groovy tests)
 * in the similar way as it was implemented for J2SE projects. Basically this is only a changed
 * copy of org.netbeans.modules.groovy.support.GroovyActionProvider with respect to differences
 * between J2SE build-impl.xml and Java EJB project build-impl.xml (different target names etc.)
 *
 * @author Martin Janicek
 */
@ProjectServiceProvider(
    service =
        ActionProvider.class,
    projectType = {
        "org-netbeans-modules-j2ee-ejbjarproject"
    }
)
public class EjbGroovyActionProvider extends AbstractGroovyActionProvider {

    public EjbGroovyActionProvider(Project project) {
        super(project);
    }

    @Override
    protected void addProjectSpecificActions(Map<String, String> actionMap) {
        actionMap.put(COMMAND_DEBUG_SINGLE, "debug-single-main");    // NOI18N
        actionMap.put(COMMAND_RUN_SINGLE, "run-main");               // NOI18N
    }
}
