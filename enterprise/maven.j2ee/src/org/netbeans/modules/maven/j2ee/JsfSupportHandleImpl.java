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

package org.netbeans.modules.maven.j2ee;

import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.netbeans.modules.web.jsfapi.spi.JsfSupportHandle;
import org.netbeans.spi.project.ProjectServiceProvider;

/**
 * Seems that after the changes made in #213800, this whole class can be deleted.
 * Do that after the NetBeans 7.3 final release.
 *
 * @author Martin Janicek
 */
@ProjectServiceProvider(service = JsfSupportHandle.class, projectType = "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR)
public class JsfSupportHandleImpl extends JsfSupportHandle {

    private final Project project;

    
    public JsfSupportHandleImpl(Project project) {
        this.project = project;
    }

    @Override
    protected boolean isEnabled() {
        String packaging = project.getLookup().lookup(NbMavenProject.class).getPackagingType();
        return MavenProjectSupport.isWebSupported(project, packaging);
    }
}
