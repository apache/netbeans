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

package org.netbeans.modules.gradle.java.newproject;

import org.netbeans.modules.gradle.java.newproject.Bundle;
import static org.netbeans.modules.gradle.spi.newproject.BaseGradleWizardIterator.PROP_PACKAGE_BASE;
import org.netbeans.modules.gradle.spi.newproject.SimpleGradleWizardIterator;
import org.netbeans.modules.gradle.spi.newproject.TemplateOperation;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.templates.TemplateRegistration;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Laszlo Kishalmi
 */
@TemplateRegistration(folder="Project/Gradle", position=100, displayName="#template.simpleAppProject", iconBase="org/netbeans/modules/gradle/java/resources/javaseProjectIcon.png", description="SimpleApplicationDescription.html")
@Messages("template.simpleAppProject=Java Application")
public class SimpleApplicationProjectWizard extends SimpleGradleWizardIterator {

    private static final String MAIN_TEMPLATE = "Templates/Classes/Main.java"; //NOI18N
    private static final String DEFAULT_LICENSE_TEMPLATE = "/Templates/Licenses/license-default.txt"; //NOI18N
    
    @Messages("LBL_SimpleApplicationProject=Java Application with Gradle")
    public SimpleApplicationProjectWizard() {
        super(Bundle.LBL_SimpleApplicationProject(), initParams());
    }

    private static Map<String, Object> initParams() {
        Map<String, Object> params = new HashMap<>();
        params.put(PROP_PLUGINS, Arrays.asList("java", "jacoco", "application"));
        params.put(PROP_DEPENDENCIES, Arrays.asList(
                "testImplementation     'junit:junit:4.13'"
        ));
        return params;
    }
    
    @Override
    protected List<? extends WizardDescriptor.Panel<WizardDescriptor>> createPanels() {
        return Collections.singletonList(createProjectAttributesPanel(new MainClassPanel()));
    }

    @Override
    protected void collectOperations(TemplateOperation ops, Map<String, Object> params) {
        super.collectOperations(ops, params);
        String packageBase = (String) params.get(PROP_PACKAGE_BASE);
        String mainClassName = (String) params.get(MainClassPanel.PROP_MAIN_CLASS_NAME);
        
        File mainJava = (File) params.get(PROP_MAIN_JAVA_DIR);
        File packageDir = new File(mainJava, packageBase.replace('.', '/'));
        
        Map<String, Object> mainParams = new HashMap<>(params);
        mainParams.put("project", new DummyProject());
        mainParams.put("package", packageBase); //NOI18N
        mainParams.put("name", mainClassName); //NOI18N
        File target = new File(packageDir, mainClassName + ".java"); //NOI18N
        ops.openFromTemplate(MAIN_TEMPLATE, target, mainParams);
    }

    public static class DummyProject {
        public String getLicensePath() {
            return DEFAULT_LICENSE_TEMPLATE;
        }
    }

}
