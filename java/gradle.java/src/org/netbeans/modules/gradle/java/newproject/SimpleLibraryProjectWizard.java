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
import org.netbeans.modules.gradle.spi.newproject.SimpleGradleWizardIterator;
import static org.netbeans.modules.gradle.spi.newproject.SimpleGradleWizardIterator.PROP_DEPENDENCIES;
import static org.netbeans.modules.gradle.spi.newproject.SimpleGradleWizardIterator.PROP_PLUGINS;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.templates.TemplateRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author Laszlo Kishalmi
 */
@TemplateRegistration(folder="Project/Gradle", position=110, displayName="#template.simpleLibProject", iconBase="org/netbeans/modules/gradle/java/resources/javaseProjectIcon.png", description="SimpleLibraryDescription.html")
@NbBundle.Messages("template.simpleLibProject=Java Class Library")
public class SimpleLibraryProjectWizard extends SimpleGradleWizardIterator {

    @NbBundle.Messages("LBL_SimpleLibraryProject=Java Class Library with Gradle")
    public SimpleLibraryProjectWizard() {
        super(Bundle.LBL_SimpleLibraryProject(), initParams());
    }
    
    private static Map<String, Object> initParams() {
        Map<String, Object> params = new HashMap<>();
        params.put(PROP_PLUGINS, Arrays.asList("java", "jacoco", "maven-publish"));
        params.put(PROP_DEPENDENCIES, Arrays.asList(
                "testImplementation     'junit:junit:4.13'"
        ));
        return params;
    }

}
    
