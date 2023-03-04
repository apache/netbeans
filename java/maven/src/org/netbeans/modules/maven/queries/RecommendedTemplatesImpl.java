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

package org.netbeans.modules.maven.queries;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;

@ProjectServiceProvider(service={RecommendedTemplates.class, PrivilegedTemplates.class}, projectType="org-netbeans-modules-maven")
public final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {

    private static final String[] JAR_APPLICATION_TYPES = {
        "java-classes",
        "java-main-class",
        "java-forms",
        "gui-java-application",
        "java-beans",
        "oasis-XML-catalogs",
        "XML",
        "web-service-clients",
        "REST-clients",
        "wsdl",
        "junit",
        "selenium-java-types",
        "simple-files",
    };
    private static final String[] JAR_PRIVILEGED_NAMES = {
        "Templates/Classes/Class.java",
        "Templates/Classes/Package",
        "Templates/Classes/Interface.java",
        "Templates/GUIForms/JPanel.java",
        "Templates/GUIForms/JFrame.java",
        "Templates/WebServices/WebServiceClient",
    };
    private static final String[] POM_APPLICATION_TYPES = {
        "XML",
        "simple-files",
    };
    private static final String[] POM_PRIVILEGED_NAMES = {
        "Templates/XML/XMLWizard",
        "Templates/Other/Folder",
    };
    private static final String[] ALL_TYPES = {
        "java-classes",
        "java-main-class",
        "java-forms",
        "java-beans",
        "j2ee-types",
        "gui-java-application",
        "java-beans",
        "oasis-XML-catalogs",
        "XML",
        "ant-script",
        "ant-task",
        "web-service-clients",
        "REST-clients",
        "wsdl",
        "servlet-types",
        "web-types",
        "junit",
        "selenium-java-types",
        "simple-files",
        "ear-types",
    };
    private static final String[] GENERIC_WEB_TYPES = {
        "java-classes",
        "java-main-class",
        "java-beans",
        "oasis-XML-catalogs",
        "XML",
        "wsdl",
        "junit",
        "selenium-java-types",
        "simple-files",
    };
    private static final String[] GENERIC_EJB_TYPES = {
        "java-classes",
        "wsdl",
        "java-beans",
        "java-main-class",
        "oasis-XML-catalogs",
        "XML",
        "junit",
        "selenium-java-types",
        "simple-files",
    };
    private static final String[] GENERIC_EAR_TYPES = {
        "XML",
        "wsdl",
        "simple-files",
    };

    private final List<String> prohibited;
    private final Project project;
    
    public RecommendedTemplatesImpl(Project proj) {
        project = proj;
        prohibited = new ArrayList<String>();
        prohibited.add(NbMavenProject.TYPE_EAR);
        prohibited.add(NbMavenProject.TYPE_EJB);
        prohibited.add(NbMavenProject.TYPE_WAR);
        prohibited.add(NbMavenProject.TYPE_NBM);
        prohibited.add(NbMavenProject.TYPE_OSGI);
    }
    
    @Override public String[] getRecommendedTypes() {
        String packaging = project.getLookup().lookup(NbMavenProject.class).getPackagingType();
        if (packaging == null) {
            packaging = NbMavenProject.TYPE_JAR;
        }
        packaging = packaging.trim();
        if (NbMavenProject.TYPE_POM.equals(packaging)) {
            if (ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA).length > 0) {
                return JAR_APPLICATION_TYPES.clone(); // #192735
                // #192735
            }
            return POM_APPLICATION_TYPES.clone();
        }
        if (NbMavenProject.TYPE_JAR.equals(packaging)) {
            return JAR_APPLICATION_TYPES.clone();
        }
        if (NbMavenProject.TYPE_WAR.equals(packaging)) {
            return GENERIC_WEB_TYPES.clone();
        }
        if (NbMavenProject.TYPE_EJB.equals(packaging)) {
            return GENERIC_EJB_TYPES.clone();
        }
        if (NbMavenProject.TYPE_EAR.equals(packaging)) {
            return GENERIC_EAR_TYPES.clone();
        }
        if (prohibited.contains(packaging)) {
            return new String[0];
        }
        // If packaging is unknown, any type of sources is recommanded.
        //TODO in future we probably can try to guess based on what plugins are
        // defined in the lifecycle.
        // If packaging is unknown, any type of sources is recommanded.
        //TODO in future we probably can try to guess based on what plugins are
        // defined in the lifecycle.
        return ALL_TYPES.clone();
    }
    
    @Override public String[] getPrivilegedTemplates() {
        String packaging = project.getLookup().lookup(NbMavenProject.class).getPackagingType();
        if (packaging == null) {
            packaging = NbMavenProject.TYPE_JAR;
        }
        packaging = packaging.trim();
        if (NbMavenProject.TYPE_POM.equals(packaging)) {
            return POM_PRIVILEGED_NAMES.clone();
        }
        if (prohibited.contains(packaging)) {
            return new String[0];
        }
        return JAR_PRIVILEGED_NAMES.clone();
    }

}
