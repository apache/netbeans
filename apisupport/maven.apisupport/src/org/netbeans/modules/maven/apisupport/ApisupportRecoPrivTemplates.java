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

package org.netbeans.modules.maven.apisupport;

import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;

/**
 * apisupport specific part of RecommendedTemplates and PrivilegedTemplates,
 * @author Milos Kleint
 */
@ProjectServiceProvider(service={RecommendedTemplates.class, PrivilegedTemplates.class}, projectType="org-netbeans-modules-maven/" + NbMavenProject.TYPE_NBM)
public class ApisupportRecoPrivTemplates implements RecommendedTemplates, PrivilegedTemplates {
    
        private static final String[] NBM_PRIVILEGED_NAMES = {
            "Templates/Classes/Class.java", // NOI18N
            "Templates/Classes/Package", // NOI18N
            "Templates/Classes/Interface.java", // NOI18N
            //"Templates/GUIForms/JPanel.java", // NOI18N
            "Templates/JUnit/SimpleJUnitTest.java", // NOI18N
            "Templates/" + UIUtil.TEMPLATE_FOLDER + "/" + UIUtil.TEMPLATE_ACTION_ID,
            "Templates/" + UIUtil.TEMPLATE_FOLDER + "/" + UIUtil.TEMPLATE_WINDOW_ID,
            //"Templates/Other/properties.properties", // NOI18N
        };
        private static final String[] NBM_TYPES = {         
            "java-classes",         // NOI18N
            "java-main-class",      // NOI18N
            "java-forms",           // NOI18N
            "java-beans",           // NOI18N
            "oasis-XML-catalogs",   // NOI18N
            "XML",                  // NOI18N
            "junit",                // NOI18N                    
            "simple-files",         // NOI18N
            "REST-clients",         // NOI18N
            UIUtil.TEMPLATE_CATEGORY,
        };
        
    
    public @Override String[] getRecommendedTypes() {
        return NBM_TYPES;
    }
    
    public @Override String[] getPrivilegedTemplates() {
        return NBM_PRIVILEGED_NAMES;
    }
    
}
