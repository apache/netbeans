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
package org.netbeans.modules.maven.j2ee.web;

import java.util.ArrayList;
import java.util.Arrays;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;

/**
 * Maven Recommended and Privileged templates implementation for web applications
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
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR
    }
)
public class WebRecoPrivTemplates implements RecommendedTemplates, PrivilegedTemplates {

    private Project project;

    public WebRecoPrivTemplates(Project project) {
        this.project = project;
    }


    private static final String[] WEB_TYPES = new String[] {
        "html5",                // NOI18N
        "servlet-types",        // NOI18N
        "web-types",            // NOI18N
        "web-types-server"      // NOI18N
    };

    private static final String[] WEB_TYPES_5 = new String[] {
        "html5",                // NOI18N
        "servlet-types",        // NOI18N
        "web-types",            // NOI18N
        "web-types-server",     // NOI18N
        "web-services",         // NOI18N
        "web-service-clients",  // NOI18N
        "REST-clients"          // NOI18N
    };

    private static final String[] WEB_TYPES_6 = WEB_TYPES_5;

    private static final String[] WEB_TYPES_EJB = new String[] {
        "ejb-types",            // NOI18N
        "ejb-types-server",     // NOI18N
        "ejb-types_3_0",        // NOI18N
        "ejb-types_3_1",        // NOI18N
        "ejb-types_3_1_full",   // NOI18N
        "ejb-deployment-descriptor", // NOI18N
    };

    private static final String[] WEB_TYPES_EJB_LITE = new String[] {
        "ejb-types",                // NOI18N
        "ejb-types_3_0",            // NOI18N
        "ejb-types_3_1",            // NOI18N
        "ejb-deployment-descriptor" // NOI18N
    };

    private static final String[] WEB_TYPES_EJB32_LITE = new String[] {
        "ejb-types",            // NOI18N
        "ejb-types_3_0",        // NOI18N
        "ejb-types_3_1",        // NOI18N
        "ejb-types_3_2",        // NOI18N
        "ejb-deployment-descriptor", // NOI18N
    };


    private static final String[] WEB_PRIVILEGED_NAMES = new String[] {
        "Templates/JSP_Servlet/JSP.jsp",            // NOI18N
        "Templates/JSP_Servlet/Html.html",          // NOI18N
        "Templates/JSP_Servlet/Servlet.java",       // NOI18N
        "Templates/Classes/Class.java",             // NOI18N
        "Templates/Classes/Package",                // NOI18N
        "Templates/Other/Folder",                   // NOI18N
    };

    private static final String[] WEB_PRIVILEGED_NAMES_5 = new String[] {
        "Templates/JSP_Servlet/JSP.jsp",            // NOI18N
        "Templates/JSP_Servlet/Html.html",          // NOI18N
        "Templates/JSP_Servlet/Servlet.java",       // NOI18N
        "Templates/Classes/Class.java",             // NOI18N
        "Templates/Classes/Package",                // NOI18N
        "Templates/Persistence/Entity.java", // NOI18N
        "Templates/Persistence/RelatedCMP", // NOI18N
        "Templates/Persistence/JsfFromDB", // NOI18N
        "Templates/WebServices/WebService.java",    // NOI18N
        "Templates/WebServices/WebServiceClient",   // NOI18N
        "Templates/WebServices/RestServicesFromDatabase",  //NOI18N
        "Templates/Other/Folder"                   // NOI18N
    };

    private static final String[] WEB_PRIVILEGED_NAMES_6 = WEB_PRIVILEGED_NAMES_5;
    private static final String[] WEB_PRIVILEGED_NAMES_EE6_FULL = new String[] {
        "Templates/J2EE/Session", // NOI18N
        "Templates/J2EE/Message"  // NOI18N
    };

    private static final String[] WEB_PRIVILEGED_NAMES_EE6_WEB = new String[] {
        "Templates/J2EE/Session"  // NOI18N
    };

    private static final String[] WEB_PRIVILEGED_NAMES_EE7_WEB = new String[] {
        "Templates/J2EE/TimerSession"   // NOI18N
    };

    @Override
    public String[] getRecommendedTypes() {
        WebModule web = WebModule.getWebModule(project.getProjectDirectory());
        if (web != null) {
            Profile p = web.getJ2eeProfile();
            if (Profile.JAVA_EE_5.equals(p)) {
                return WEB_TYPES_5;
            }
            if (p != null && p.isAtLeast(Profile.JAVA_EE_6_WEB)) {
                ArrayList<String> toRet = new ArrayList<String>(Arrays.asList(WEB_TYPES_6));
                J2eeProjectCapabilities cap = J2eeProjectCapabilities.forProject(project);
                if (cap != null) {
                    if (cap.isEjb31Supported() || isServerSupportingEJB31()) {
                        toRet.addAll(Arrays.asList(WEB_TYPES_EJB));
                    } else if (cap.isEjb32LiteSupported() || cap.isEjb40LiteSupported()) {
                        toRet.addAll(Arrays.asList(WEB_TYPES_EJB32_LITE));
                    } else if (cap.isEjb31LiteSupported()) {
                        toRet.addAll(Arrays.asList(WEB_TYPES_EJB_LITE));
                    }
                }
                return toRet.toArray(new String[0]);
            }
        }
        return WEB_TYPES;
    }

    @Override
    public String[] getPrivilegedTemplates() {
        WebModule web = WebModule.getWebModule(project.getProjectDirectory());
        if (web != null) {
            Profile p = web.getJ2eeProfile();
            if (Profile.JAVA_EE_5.equals(p)) {
                return WEB_PRIVILEGED_NAMES_5;
            }
            if (p != null && p.isAtLeast(Profile.JAVA_EE_6_WEB)) {
                ArrayList<String> toRet = new ArrayList<String>(Arrays.asList(WEB_PRIVILEGED_NAMES_6));
                J2eeProjectCapabilities cap = J2eeProjectCapabilities.forProject(project);
                if (cap != null) {
                    if (cap.isEjb31Supported() || isServerSupportingEJB31()) {
                        toRet.addAll(Arrays.asList(WEB_PRIVILEGED_NAMES_EE6_FULL));
                    }
                    if (cap.isEjb31LiteSupported()) {
                        toRet.addAll(Arrays.asList(WEB_PRIVILEGED_NAMES_EE6_WEB));
                    }
                    if (cap.isEjb32LiteSupported() || cap.isEjb40LiteSupported()) {
                        toRet.addAll(Arrays.asList(WEB_PRIVILEGED_NAMES_EE7_WEB));
                    }
                }
                return toRet.toArray(new String[0]);
            }
        }
        return WEB_PRIVILEGED_NAMES;
    }

    private boolean isServerSupportingEJB31() {
        if (ProjectUtil.getSupportedProfiles(project).contains(Profile.JAVA_EE_6_FULL)
                || ProjectUtil.getSupportedProfiles(project).contains(Profile.JAVA_EE_7_FULL)
                || ProjectUtil.getSupportedProfiles(project).contains(Profile.JAVA_EE_8_FULL)
                || ProjectUtil.getSupportedProfiles(project).contains(Profile.JAKARTA_EE_8_FULL)) {

            return true;
        }
        return false;
    }
}
