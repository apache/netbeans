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

package org.netbeans.modules.gradle.javaee.web.newproject;

import org.netbeans.modules.gradle.javaee.web.WebModuleProviderImpl;
import org.netbeans.modules.gradle.spi.newproject.SimpleGradleWizardIterator;
import org.netbeans.modules.gradle.spi.newproject.TemplateOperation;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Laszlo Kishalmi
 */
@TemplateRegistration(folder="Project/Gradle", position=200, displayName="#template.webAppProject", iconBase="org/netbeans/modules/gradle/javaee/resources/javaeeProjectIcon.png", description="WebApplicationDescription.html")
@Messages("template.webAppProject=Web Application")
public class WebApplicationProjectWizard extends SimpleGradleWizardIterator {

    private static final String INDEX_TEMPLATE = "Templates/JSP_Servlet/Html.html"; //NOI18N
    private static final String DEFAULT_LICENSE_TEMPLATE = "/Templates/Licenses/license-default.txt"; //NOI18N


    @Messages("LBL_WebApplicationProject=Java Application with Gradle")
    public WebApplicationProjectWizard() {
        super(Bundle.LBL_WebApplicationProject(), initParams());
    }

    private static Map<String, Object> initParams() {
        Map<String, Object> params = new HashMap<>();
        params.put(PROP_PLUGINS, Arrays.asList("java", "jacoco", "war"));
        return params;
    }

    @Override
    protected List<? extends WizardDescriptor.Panel<WizardDescriptor>> createPanels() {
        return Arrays.asList(
                createProjectAttributesPanel(null),
                new ServerSelectionPanel(J2eeModule.Type.WAR)
        );
    }

    @Override
    protected void collectOperations(TemplateOperation ops, Map<String, Object> params) {
        super.collectOperations(ops, params);

        File rootDir = (File) params.get(PROP_PROJECT_ROOT);

        Map<String, Object> mainParams = new HashMap<>(params);
        mainParams.put("project", new DummyProject());
        ops.createFolder(new File(rootDir, "src/main/webapp/WEB-INF")); //NOI18N
        File target = new File(rootDir, "src/main/webapp/index"); //NOI18N
        ops.openFromTemplate(INDEX_TEMPLATE, target, mainParams);

        String instanceId = (String) params.get(ServerSelectionPanel.PROP_SERVER);
        String profileId = (String) params.get(ServerSelectionPanel.PROP_PROFILE);

        List<String> dependencies = new LinkedList<>();
        dependencies.addAll(webDependencies(profileId));
        dependencies.add("");
        dependencies.add("testImplementation     'junit:junit:4.13'");
        params.put(PROP_DEPENDENCIES, dependencies);

        String projectName = (String) params.get(PROP_NAME);

        ops.addConfigureProject(rootDir, (Project project) -> {
            JavaEEProjectSettings.setServerInstanceID(project, instanceId);
            WebModuleProviderImpl impl = project.getLookup().lookup(WebModuleProviderImpl.class);

            impl.getModuleImpl().setContextPath("/" + projectName);
        });


    }

    public static class DummyProject {
        public String getLicensePath() {
            return DEFAULT_LICENSE_TEMPLATE;
        }
    }

    private static List<String> webDependencies(String profileId) {
        Profile profile = Profile.fromPropertiesString(profileId);
        List<String> ret = new LinkedList<>();
        if (profile == Profile.JAKARTA_EE_11_WEB) {
            ret.add("providedCompile 'jakarta.platform:jakarta.jakartaee-web-api:11.0.0'");
        }
        if (profile == Profile.JAKARTA_EE_11_FULL) {
            ret.add("providedCompile 'jakarta.platform:jakarta.jakartaee-api:11.0.0'");
        }
        if (profile == Profile.JAKARTA_EE_10_WEB) {
            ret.add("providedCompile 'jakarta.platform:jakarta.jakartaee-web-api:10.0.0'");
        }
        if (profile == Profile.JAKARTA_EE_10_FULL) {
            ret.add("providedCompile 'jakarta.platform:jakarta.jakartaee-api:10.0.0'");
        }
        if (profile == Profile.JAKARTA_EE_9_1_WEB) {
            ret.add("providedCompile 'jakarta.platform:jakarta.jakartaee-web-api:9.1.0'");
        }
        if (profile == Profile.JAKARTA_EE_9_1_FULL) {
            ret.add("providedCompile 'jakarta.platform:jakarta.jakartaee-api:9.1.0'");
        }
        if (profile == Profile.JAKARTA_EE_9_WEB) {
            ret.add("providedCompile 'jakarta.platform:jakarta.jakartaee-web-api:9.0.0'");
        }
        if (profile == Profile.JAKARTA_EE_9_FULL) {
            ret.add("providedCompile 'jakarta.platform:jakarta.jakartaee-api:9.0.0'");
        }
        if (profile == Profile.JAKARTA_EE_8_WEB) {
            ret.add("providedCompile 'jakarta.platform:jakarta.jakartaee-web-api:8.0.0'");
        }
        if (profile == Profile.JAKARTA_EE_8_FULL) {
            ret.add("providedCompile 'jakarta.platform:jakarta.jakartaee-api:8.0.0'");
        }
        if (profile == Profile.JAVA_EE_8_WEB) {
            ret.add("providedCompile 'javax:javaee-web-api:8.0'");
        }
        if (profile == Profile.JAVA_EE_8_FULL) {
            ret.add("providedCompile 'javax:javaee-api:8.0'");
        }
        if (profile == Profile.JAVA_EE_7_WEB) {
            ret.add("providedCompile 'javax:javaee-web-api:7.0'");
        }
        if (profile == Profile.JAVA_EE_7_FULL) {
            ret.add("providedCompile 'javax:javaee-api:7.0'");
        }
        if (profile == Profile.JAVA_EE_6_WEB) {
            ret.add("providedCompile 'javax:javaee-web-api:6.0'");
        }
        if (profile == Profile.JAVA_EE_6_FULL) {
            ret.add("providedCompile 'javax:javaee-api:6.0'");
        }
        if (profile == Profile.JAVA_EE_5) {
            ret.add("providedCompile 'javaee:javaee-api:5'");
            ret.add("providedCompile 'javax.servlet:servlet-api:2.5'");
        }
        return ret;
    }
}
