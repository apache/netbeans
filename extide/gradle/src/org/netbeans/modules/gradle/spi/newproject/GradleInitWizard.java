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
package org.netbeans.modules.gradle.spi.newproject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.gradle.api.execute.GradleDistributionManager;
import org.netbeans.modules.gradle.newproject.GradleInitPanel;
import org.netbeans.modules.gradle.spi.GradleSettings;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author lkishalmi
 */
public final class GradleInitWizard {

    public static final String PROP_JAVA_VERSIONS = "javaVersions";     //NOI18N
    public static final String PROP_JAVA_VERSION  = "javaVersion";      //NOI18N
    public static final String PROP_TEST_FRAMEWORKS = "testFrameworks"; //NOI18N
    public static final String PROP_TEST_FRAMEWORK = "testFramework";   //NOI18N
    public static final String PROP_DSL = "DSL";                        //NOI18N
    public static final String PROP_COMMENTS = "comments";              //NOI18N

    @NbBundle.Messages({
        "LBL_DSL_GROOVY=Groovy",
        "LBL_DSL_KOTLIN=Kotlin"
    })
    public enum GradleDSL {
        GROOVY,
        KOTLIN;

        @Override
        public String toString() {
            return switch(this) {
                case GROOVY -> Bundle.LBL_DSL_GROOVY();
                case KOTLIN -> Bundle.LBL_DSL_KOTLIN();
            };
        }
    }

    @NbBundle.Messages({
        "LBL_TFW_CPP_TEST=C++ Test",
        "LBL_TFW_JUNIT=JUnit 4",
        "LBL_TFW_JUNIT_5=JUnit 5",
        "LBL_TFW_KOTLIN_TEST=Kotlin Test",
        "LBL_TFW_SCALA_TEST=Scala Test",
        "LBL_TFW_SPOCK=Spock",
        "LBL_TFW_TESTNG=Test NG",
        "LBL_TFW_XCTEST=XCode Test",
    })
    public enum TestFramework {
        CPP_TEST("cpptest"),
        JUNIT("junit"),
        JUNIT_5("junit-jupiter"),
        KOTLIN_TEST("kotlintest"),
        SCALA_TEST("scalatest"),
        SPOCK("spock"),
        TESTNG("testng"),
        XCTEST("xctest");

        private final String id;

        private TestFramework(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
        
        @Override
        public String toString() {
            return switch(this) {
                case CPP_TEST -> Bundle.LBL_TFW_CPP_TEST();
                case JUNIT -> Bundle.LBL_TFW_JUNIT();
                case JUNIT_5 -> Bundle.LBL_TFW_JUNIT_5();
                case KOTLIN_TEST -> Bundle.LBL_TFW_KOTLIN_TEST();
                case SCALA_TEST -> Bundle.LBL_TFW_SCALA_TEST();
                case SPOCK -> Bundle.LBL_TFW_SPOCK();
                case TESTNG -> Bundle.LBL_TFW_TESTNG();
                case XCTEST-> Bundle.LBL_TFW_XCTEST();
            };
        }
    }

    private final String type;
    private final String title;

    private Integer preferredJavaVersion;
    private TestFramework preferredTestFramework;
    private List<Integer> javaVersions;
    private List<TestFramework> testFrameworks;
    private List<String> important = List.of();

    private GradleInitWizard(String type, String title) {
        this.type = type;
        this.title = title;
    }

    
    public static GradleInitWizard create(String type, String title) {
        return new GradleInitWizard(type, title);
    }

    public GradleInitWizard withJavaVersions(List<Integer> javaVersions) {
        this.javaVersions = javaVersions;
        return this;
    }

    public GradleInitWizard withTestframeworks(List<TestFramework> testFrameworks) {
        this.testFrameworks = testFrameworks;
        return this;
    }

    public GradleInitWizard withPreferredJava(Integer version) {
        this.preferredJavaVersion = version;
        return this;
    }

    public GradleInitWizard withPreferredTestFramework(TestFramework framework) {
        this.preferredTestFramework = framework;
        return this;
    }

    public GradleInitWizard withImportantPaths(List<String> important) {
        this.important = important;
        return this;
    }

    public BaseGradleWizardIterator build() {
        return new GradleInitWizardIterator(type, title, important) {
            @Override
            protected WizardDescriptor initData(WizardDescriptor data) {
                if (javaVersions != null) {
                    data.putProperty(PROP_JAVA_VERSIONS, javaVersions);
                }
                if (testFrameworks != null) {
                    data.putProperty(PROP_TEST_FRAMEWORKS, testFrameworks);
                }
                if (preferredJavaVersion != null) {
                    data.putProperty(PROP_JAVA_VERSION, preferredJavaVersion);
                }
                if (preferredTestFramework != null) {
                    data.putProperty(PROP_TEST_FRAMEWORK, preferredTestFramework);
                }
                return data;
            }
        };
    };

    private static class GradleInitWizardIterator extends BaseGradleWizardIterator {
        private final String type;
        private final String title;

        private final List<String> important;

        private GradleInitWizardIterator(String type, String title, List<String> important) {
            this.type = type;
            this.title = title;
            this.important = important;
        }

        @Override
        protected void collectOperations(TemplateOperation ops, Map<String, Object> params) {
            String name = (String) params.get(PROP_NAME);
            String packageBase = (String) params.get(PROP_PACKAGE_BASE);
            File loc = (File) params.get(CommonProjectActions.PROJECT_PARENT_FOLDER);

            File root = new File(loc, name);

            TemplateOperation.InitOperation init = ops.createGradleInit(root, type);
            init.projectName(name);
            if (packageBase != null) {
                init.basePackage(packageBase);
            }

            GradleDSL dsl = (GradleDSL) params.get(PROP_DSL);
            init.dsl(dsl == GradleDSL.KOTLIN ? "kotlin" : "groovy"); //NOI18N

            if (params.get(PROP_JAVA_VERSION) != null) {
                init.javaVersion(params.get(PROP_JAVA_VERSION).toString());
            }

            if (params.get(PROP_TEST_FRAMEWORK) != null) {
                init.testFramework(((TestFramework)params.get(PROP_TEST_FRAMEWORK)).getId());
            }

            init.comments((Boolean)params.get(PROP_COMMENTS));
            init.add();

            if (!GradleSettings.getDefault().isOffline()) {
                try {
                    init.gradleVersion(GradleDistributionManager.get().currentDistribution().getVersion());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            List<String> open = important.stream()
                    .map((s) -> packageBase != null ? s.replace("${package}", packageBase.replace('.', '/')) : s) //NOI18N
                    .map((s) -> s.replace("${projectName}", name)) //NOI18N
                    .toList();
            ops.addProjectPreload(root, open);
        }


        @Override
        protected List<? extends WizardDescriptor.Panel<WizardDescriptor>> createPanels() {
            return List.of(new GradleInitPanel());
        }

        @Override
        protected String getTitle() {
            return title;
        }
    }


}
