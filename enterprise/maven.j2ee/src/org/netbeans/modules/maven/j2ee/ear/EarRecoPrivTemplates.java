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
package org.netbeans.modules.maven.j2ee.ear;

import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;

/**
 * Maven Recommended and Privileged templates implementation for EAR applications
 * 
 * @author Martin Janicek
 */
@org.netbeans.api.annotations.common.SuppressWarnings("EI_EXPOSE_REP")
@ProjectServiceProvider(
    service = {
        RecommendedTemplates.class,
        PrivilegedTemplates.class
    },
    projectType = {
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EAR
    }
)
public class EarRecoPrivTemplates implements RecommendedTemplates, PrivilegedTemplates {
    
    private static final String[] EAR_RECOMMENDED_TYPES = new String[] {
        "ear-types",                     // NOI18N
    };
    
    private static final String[] EAR_PRIVILEGED_NAMES = new String[] {
        "Templates/J2EE/ApplicationXml", //NOI18N
        "Templates/Other/Folder"         //NOI18N
    };
    
    
    @Override
    public String[] getRecommendedTypes() {
        return EAR_RECOMMENDED_TYPES;
    }
    
    @Override
    public String[] getPrivilegedTemplates() {
        return EAR_PRIVILEGED_NAMES;
    }
}
