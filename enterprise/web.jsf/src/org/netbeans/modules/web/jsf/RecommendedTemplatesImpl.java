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
package org.netbeans.modules.web.jsf;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.RecommendedTemplates;

/**
 * Registers JSF related templates for the projects.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
@ProjectServiceProvider(service = RecommendedTemplates.class, projectType = {
    "org-netbeans-modules-java-j2seproject",
    "org-netbeans-modules-web-project",
    "org-netbeans-modules-maven"
})
public class RecommendedTemplatesImpl implements RecommendedTemplates {

    private static final String[] TYPES = new String[]{
        "jsf-types" //NOI18N
    };
    
    public RecommendedTemplatesImpl(Project project) {
    }

    @Override
    public String[] getRecommendedTypes() {
        return TYPES;
    }
}
