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

package org.netbeans.modules.groovy.antproject.web;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.antproject.common.BuildScriptHelper;
import org.netbeans.modules.groovy.antproject.common.BuildScriptType;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;

/**
 *
 * @author Martin Janicek
 */
@ProjectServiceProvider(
    service =
        ProjectXmlSavedHook.class,
    projectType = {
        "org-netbeans-modules-web-project"
    }
)
public class WebGroovyXmlSavedHook extends ProjectXmlSavedHook {

    private final Project project;

    public WebGroovyXmlSavedHook(Project project) {
        this.project = project;
    }

    @Override
    protected void projectXmlSaved() throws IOException {
        BuildScriptHelper.refreshBuildScript(project, BuildScriptType.WEB.getStylesheet(), true);
    }
}
