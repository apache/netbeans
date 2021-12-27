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

package org.netbeans.modules.gradle.spring;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.RecommendedTemplates;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service=RecommendedTemplates.class, projectType=NbGradleProject.GRADLE_PLUGIN_TYPE + "/java")
public class RecommendedTemplatesImpl implements RecommendedTemplates {

    private static final String[] TYPES = new String[] {
        "spring-types" //NOI18N
    };
            
    @Override
    public String[] getRecommendedTypes() {
        return TYPES; //NOI18N
    }
    
}
