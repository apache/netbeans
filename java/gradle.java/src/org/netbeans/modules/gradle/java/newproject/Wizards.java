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

import java.util.List;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.gradle.spi.newproject.BaseGradleWizardIterator;
import org.netbeans.modules.gradle.spi.newproject.GradleInitWizard;
import org.netbeans.modules.gradle.spi.newproject.GradleInitWizard.TestFramework;
import org.openide.util.NbBundle;

import static org.netbeans.modules.gradle.spi.newproject.GradleInitWizard.TestFramework.*;

/**
 *
 * @author lkishalmi
 */
public final class Wizards {

    private Wizards() {};

    private static final List<Integer> JAVA_VERSIONS = List.of(22, 21, 17, 11, 8);
    private static final List<TestFramework> JAVA_TEST_FRAMEWORKS = List.of(
            JUNIT,
            JUNIT_5,
            TESTNG
    );

    @TemplateRegistration(folder="Project/Gradle", position=100, displayName="#template.simpleAppProject", iconBase="org/netbeans/modules/gradle/java/resources/javaseProjectIcon.png", description="SimpleApplicationDescription.html")
    @NbBundle.Messages({
        "template.simpleAppProject=Java Application",
        "LBL_SimpleApplicationProject=Java Application with Gradle"
    })
    public static BaseGradleWizardIterator createJavaApplication() {
        return GradleInitWizard.create("java-application", Bundle.LBL_SimpleApplicationProject())
                .withJavaVersions(JAVA_VERSIONS)
                .withTestframeworks(JAVA_TEST_FRAMEWORKS)
                .withPreferredTestFramework(JUNIT_5)
                .withImportantPaths(List.of(
                        "app",
                        "app/src/main/java/${package}/App.java"
                ))
                .build();
    }

    @TemplateRegistration(folder="Project/Gradle", position=110, displayName="#template.simpleLibProject", iconBase="org/netbeans/modules/gradle/java/resources/javaseProjectIcon.png", description="SimpleLibraryDescription.html")
    @NbBundle.Messages({
        "template.simpleLibProject=Java Class Library",
        "LBL_SimpleLibraryProject=Java Class Library with Gradle"
    })
    public static BaseGradleWizardIterator createJavaLibrary() {
        return GradleInitWizard.create("java-library", Bundle.LBL_SimpleApplicationProject())
                .withJavaVersions(JAVA_VERSIONS)
                .withTestframeworks(JAVA_TEST_FRAMEWORKS)
                .withPreferredTestFramework(JUNIT_5)
                .withImportantPaths(List.of(
                        "lib",
                        "lib/src/main/java/${package}/Library.java"
                ))
                .build();
    }
}
