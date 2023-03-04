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

package org.netbeans.modules.groovy.antproject.j2se;

import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.antproject.base.AbstractGroovyActionProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;

/**
 * This ActionProvider support few basic operations.
 * - Compile File
 * - Run File
 * - Test File
 * - Debug File
 * - Debug Test File
 *
 * @author Martin Adamek
 * @author Martin Janicek
 */
@ProjectServiceProvider(
    service =
        ActionProvider.class,
    projectType =
        "org-netbeans-modules-java-j2seproject"
)
public class JavaSEGroovyActionProvider extends AbstractGroovyActionProvider {

    public JavaSEGroovyActionProvider(Project project) {
        super(project);
    }

    @Override
    protected void addProjectSpecificActions(Map<String, String> actionMap) {
        actionMap.put(COMMAND_DEBUG_SINGLE, "debug-single"); // NOI18N
        actionMap.put(COMMAND_RUN_SINGLE, "run-single");     // NOI18N
    }
}
