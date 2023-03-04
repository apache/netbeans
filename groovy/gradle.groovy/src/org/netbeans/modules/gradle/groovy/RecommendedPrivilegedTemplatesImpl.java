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
package org.netbeans.modules.gradle.groovy;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;

/**
 *
 * @author lkishalmi
 */
@ProjectServiceProvider(service = {RecommendedTemplates.class, PrivilegedTemplates.class}, projectType = NbGradleProject.GRADLE_PLUGIN_TYPE + "/groovy-base")
public class RecommendedPrivilegedTemplatesImpl  implements RecommendedTemplates, PrivilegedTemplates {

    // List of primarily supported templates categories
    private static final String[] TYPES = new String[] {
        "groovy"
    };

    private static final String[] TEMPLATES = new String[] {
        "Templates/Groovy/GroovyClass.groovy",
        "Templates/Groovy/GroovyTrait.groovy"
    };

    @Override
    public String[] getRecommendedTypes() {
        return TYPES;
    }

    @Override
    public String[] getPrivilegedTemplates() {
        return TEMPLATES;
    }
}