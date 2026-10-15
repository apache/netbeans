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
package org.netbeans.modules.gradle.groovy.newproject;

import java.util.List;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.gradle.spi.newproject.BaseGradleWizardIterator;
import org.netbeans.modules.gradle.spi.newproject.GradleInitWizard;
import org.openide.util.NbBundle;

/**
 *
 * @author lkishalmi
 */
public class Wizards {

    private Wizards() {}

    private static final List<Integer> JAVA_VERSIONS = List.of(21, 17, 11, 8);

    @TemplateRegistration(folder="Project/GradleGroovy", position=350, displayName="#template.simpleAppProject", iconBase="org/netbeans/modules/gradle/groovy/resources/groovyProjectIcon.png", description="SimpleApplicationDescription.html")
    @NbBundle.Messages({
        "template.simpleAppProject=Groovy Application",
        "LBL_SimpleApplicationProject=Groovy Application with Gradle"
    })
    public static BaseGradleWizardIterator createJavaApplication() {
        return  GradleInitWizard.create("groovy-application", Bundle.LBL_SimpleApplicationProject())
                .withJavaVersions(JAVA_VERSIONS)
                .withImportantPaths(List.of(
                        "app",
                        "app/src/main/groovy/${package}/App.groovy"
                ))
                .build();
    }

    @TemplateRegistration(folder="Project/GradleGroovy", position=360, displayName="#template.simpleLibProject", iconBase="org/netbeans/modules/gradle/groovy/resources/groovyProjectIcon.png", description="SimpleLibraryDescription.html")
    @NbBundle.Messages({
        "template.simpleLibProject=Groovy Class Library",
        "LBL_SimpleLibraryProject=Groovy Class Library with Gradle"
    })
    public static BaseGradleWizardIterator createJavaLibrary() {
        return  GradleInitWizard.create("groovy-library", Bundle.LBL_SimpleApplicationProject())
                .withJavaVersions(JAVA_VERSIONS)
                .withImportantPaths(List.of(
                        "lib",
                        "lib/src/main/groovy/${package}/Library.groovy"
                ))
                .build();
    }
}
