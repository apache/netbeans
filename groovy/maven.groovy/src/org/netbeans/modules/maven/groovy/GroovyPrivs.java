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

package org.netbeans.modules.maven.groovy;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.PrivilegedTemplates;

@ProjectServiceProvider(service=PrivilegedTemplates.class, projectType="org-netbeans-modules-maven")
public class GroovyPrivs implements PrivilegedTemplates {

    private final Project project;

    public GroovyPrivs(Project project) {
        this.project = project;
    }
    
    @Override public String[] getPrivilegedTemplates() {
        if (ProjectUtils.getSources(project).getSourceGroups(GroovySourcesImpl.TYPE_GROOVY).length > 0) {
            return new String[] {
                "Templates/Groovy/GroovyClass.groovy",
                "Templates/Groovy/GroovyScript.groovy",
                "Templates/Other/Folder"
            };
        } else {
            return new String[0];
        }
    }

}
