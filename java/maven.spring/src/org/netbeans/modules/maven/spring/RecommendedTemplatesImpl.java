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

package org.netbeans.modules.maven.spring;

import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.RecommendedTemplates;

/**
 *
 * @author Milos Kleint
 */
@ProjectServiceProvider(service=RecommendedTemplates.class, projectType="org-netbeans-modules-maven")
public class RecommendedTemplatesImpl implements RecommendedTemplates {

    private static final String[] SPRING_TYPES = new String[] {
        "spring-types" // NOI18N
    };

    private static final String[] SPRING_WEB_TYPES = new String[] {
        "spring-web-types" // NOI18N
    };

    private Project prj;

    public RecommendedTemplatesImpl(Project prj) {
        this.prj = prj;
    }

    @Override
    public String[] getRecommendedTypes() {
        NbMavenProject project = prj.getLookup().lookup(NbMavenProject.class);
        return NbMavenProject.TYPE_WAR.equals(project.getPackagingType())
                ? SPRING_WEB_TYPES : SPRING_TYPES;
    }
}
