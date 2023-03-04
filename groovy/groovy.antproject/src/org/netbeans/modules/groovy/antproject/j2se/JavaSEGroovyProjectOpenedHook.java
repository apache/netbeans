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

import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.antproject.common.BuildScriptHelper;
import org.netbeans.modules.groovy.antproject.common.BuildScriptType;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;

/**
 *
 * @author Martin Janicek
 */
@ProjectServiceProvider(
    service =
        ProjectOpenedHook.class,
    projectType = {
        "org-netbeans-modules-java-j2seproject"
    }
)
public class JavaSEGroovyProjectOpenedHook extends ProjectOpenedHook {

    private final Project project;

    public JavaSEGroovyProjectOpenedHook(Project project) {
        this.project = project;
    }

    @Override
    protected void projectOpened() {
        BuildScriptHelper.refreshBuildScript(project, BuildScriptType.J2SE.getStylesheet(), true);
    }

    @Override
    protected void projectClosed() {
    }
}
