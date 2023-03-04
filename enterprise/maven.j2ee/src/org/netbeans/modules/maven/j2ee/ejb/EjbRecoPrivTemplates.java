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
package org.netbeans.modules.maven.j2ee.ejb;

import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;

/**
 * Maven EJB Recommended and Privileged templates implementation
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
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EJB
    }
)
public class EjbRecoPrivTemplates implements RecommendedTemplates, PrivilegedTemplates {

    private J2eeProjectCapabilities capabilities;
    private Project project;
    
    
    public EjbRecoPrivTemplates(Project project) {
        this.project = project;
    }
    
    private static final String[] EJB_RECOMMENDED_TYPES_5 = new String[] {
        "ejb-deployment-descriptor",// NOI18N
        "ejb-types",            // NOI18N
        "ejb-types_3_0",        // NOI18N
        "ejb-types-server",     // NOI18N
        "web-services",         // NOI18N
        "web-service-clients",  // NOI18N
        "j2ee-types",           // NOI18N
    };

    private static final String[] EJB_RECOMMENDED_TYPES_6 = new String[] {
        "ejb-deployment-descriptor",// NOI18N
        "ejb-types",                // NOI18N
        "ejb-types-server",         // NOI18N
        "ejb-types_3_1",            // NOI18N
        "web-services",             // NOI18N
        "web-service-clients",      // NOI18N
        "wsdl",                     // NOI18N
        "j2ee-types"                // NOI18N
    };

    private static final String[] EJB_PRIVILEGED_NAMES_5 = new String[] {
        "Templates/J2EE/Session",               // NOI18N
        "Templates/J2EE/Message",               // NOI18N
        "Templates/Classes/Class.java",         // NOI18N
        "Templates/Classes/Package",            // NOI18N
        "Templates/Persistence/Entity.java",    // NOI18N
        "Templates/Persistence/RelatedCMP",     // NOI18N
        "Templates/WebServices/WebService",     // NOI18N
        "Templates/WebServices/WebServiceClient"// NOI18N
    };

    private static final String[] EJB_PRIVILEGED_NAMES_6 = EJB_PRIVILEGED_NAMES_5;
    
    
    @Override
    public String[] getRecommendedTypes() {
        initCapabilities();
        if (capabilities.isEjb32Supported()) {
            return EJB_RECOMMENDED_TYPES_6;
        }
        if (capabilities.isEjb31Supported()) {
            return EJB_RECOMMENDED_TYPES_6;
        }
        if (capabilities.isEjb30Supported()) {
            return EJB_RECOMMENDED_TYPES_5;
        }
        return EJB_RECOMMENDED_TYPES_5;
    }
    
    @Override
    public String[] getPrivilegedTemplates() {
        initCapabilities();
        if (capabilities.isEjb32Supported()) {
            return EJB_PRIVILEGED_NAMES_6;
        }
        if (capabilities.isEjb31Supported()) {
            return EJB_PRIVILEGED_NAMES_6;
        }
        if (capabilities.isEjb30Supported()) {
            return EJB_PRIVILEGED_NAMES_5;
        }
        return EJB_PRIVILEGED_NAMES_5;
    }
    
    private void initCapabilities() {
        if (capabilities == null) {
            capabilities = J2eeProjectCapabilities.forProject(project);
        }
    }
}
