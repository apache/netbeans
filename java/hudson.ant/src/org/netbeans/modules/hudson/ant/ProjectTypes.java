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

package org.netbeans.modules.hudson.ant;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Registrations of currently supported Ant-based project types.
 */
public abstract class ProjectTypes implements AntBasedJobCreator {

    @ServiceProvider(service=AntBasedJobCreator.class)
    public static class J2SE extends ProjectTypes {
        public J2SE() {
            super("org.netbeans.modules.java.j2seproject", // NOI18N
                new TargetTemplate[] {
                    // XXX use appropriate properties from project evaluator where possible
                    new TargetTemplate("jar", "ProjectTypes.buildJar.text", true, "dist/", "dist/javadoc/", null, false, null, null), // NOI18N
                    new TargetTemplate("javadoc", "ProjectTypes.buildJavadoc.text", false, null, null, "dist/javadoc", false, null, null), // NOI18N
                    new TargetTemplate("test", "ProjectTypes.runTests.text", false, null, null, null, true,
                            "build/test/results/TEST-*.xml", "ignore.failing.tests=true"), // NOI18N
                });
        }
    }

    @ServiceProvider(service=AntBasedJobCreator.class)
    public static class Web extends ProjectTypes {
        public Web() {
            super("org.netbeans.modules.web.project", // NOI18N
                new TargetTemplate[] {
                    new TargetTemplate("dist", "ProjectTypes.buildDist.text", true, "dist/", "dist/javadoc/", null, false, null, null), // NOI18N
                    new TargetTemplate("javadoc", "ProjectTypes.buildJavadoc.text", false, null, null, "dist/javadoc", false, null, null), // NOI18N
                    new TargetTemplate("test", "ProjectTypes.runTests.text", false, null, null, null, true,
                            "build/test/results/TEST-*.xml", "ignore.failing.tests=true"), // NOI18N
                });
        }
    }

    @ServiceProvider(service=AntBasedJobCreator.class)
    public static class EJB extends ProjectTypes {
        public EJB() {
            super("org.netbeans.modules.j2ee.ejbjarproject", // NOI18N
                new TargetTemplate[] {
                    new TargetTemplate("dist", "ProjectTypes.buildDist.text", true, "dist/", "dist/javadoc/", null, false, null, null), // NOI18N
                    new TargetTemplate("javadoc", "ProjectTypes.buildJavadoc.text", false, null, null, "dist/javadoc", false, null, null), // NOI18N
                    new TargetTemplate("test", "ProjectTypes.runTests.text", false, null, null, null, true,
                            "build/test/results/TEST-*.xml", "ignore.failing.tests=true"), // NOI18N
                });
        }
    }

    @ServiceProvider(service=AntBasedJobCreator.class)
    public static class CAR extends ProjectTypes {
        public CAR() {
            super("org.netbeans.modules.j2ee.clientproject", // NOI18N
                new TargetTemplate[] {
                    new TargetTemplate("dist", "ProjectTypes.buildDist.text", true, "dist/", "dist/javadoc/", null, false, null, null), // NOI18N
                    new TargetTemplate("javadoc", "ProjectTypes.buildJavadoc.text", false, null, null, "dist/javadoc", false, null, null), // NOI18N
                    new TargetTemplate("test", "ProjectTypes.runTests.text", false, null, null, null, true,
                            "build/test/results/TEST-*.xml", "ignore.failing.tests=true"), // NOI18N
                });
        }
    }

    @ServiceProvider(service=AntBasedJobCreator.class)
    public static class EAR extends ProjectTypes {
        public EAR() {
            super("org.netbeans.modules.j2ee.earproject", // NOI18N
                new TargetTemplate[] {
                    new TargetTemplate("dist", "ProjectTypes.buildDist.text", true, "dist/", "dist/javadoc/", null, false, null, null), // NOI18N
                });
        }
    }

    private final String type;
    private final TargetTemplate[] templates;

    private ProjectTypes(String type, TargetTemplate[] templates) {
        this.type = type;
        this.templates = templates;
    }

    public String type() {
        return type;
    }

    public Configuration forProject(Project project) {
        return new ConfigurationImpl(project);
    }

    private class ConfigurationImpl implements Configuration {

        private final Project project;

        ConfigurationImpl(Project project) {
            this.project = project;
        }

        public List<Target> targets() {
            List<Target> targets = new ArrayList<Target>();
            for (final TargetTemplate template : templates) {
                targets.add(new Target() {
                    final boolean selected = !template.checkForTests || hasTests(project);
                    public String antName() {
                        return template.antName;
                    }
                    public String labelWithMnemonic() {
                        return NbBundle.getMessage(ProjectTypes.class, template.bundleKey);
                    }
                    public boolean selected() {
                        return selected;
                    }
                    public boolean enabled() {
                        return selected && !template.mandatory;
                    }
                    public ArchivePattern artifactArchival() {
                        if (template.artifactIncludes != null) {
                            return new ArchivePattern() {
                                public String includes() {
                                    return template.artifactIncludes;
                                }
                                public String excludes() {
                                    return template.artifactExcludes;
                                }
                            };
                        } else {
                            return null;
                        }
                    }
                    public String javadocDir() {
                        return template.javadocDir;
                    }
                    public String testResults() {
                        return template.testResults;
                    }
                    public String properties() {
                        return template.properties;
                    }
                    boolean hasTests(Project project) {
                        for (SourceGroup g : ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                            FileObject root = g.getRootFolder();
                            if (UnitTestForSourceQuery.findSources(root).length > 0 && containsSources(root)) {
                                return true;
                            }
                        }
                        return false;
                    }
                    boolean containsSources(FileObject f) {
                        if (f.isFolder()) {
                            for (FileObject k : f.getChildren()) {
                                if (containsSources(k)) {
                                    return true;
                                }
                            }
                            return false;
                        } else {
                            return f.hasExt("java"); // NOI18N
                        }
                    }
                });
            }
            return targets;
        }

    }

    private static class TargetTemplate {
        private final String antName;
        private final String bundleKey;
        private final boolean mandatory;
        private final String artifactIncludes;
        private final String artifactExcludes;
        private final String javadocDir;
        /** #159831: check whether it makes sense to run any tests. */
        private final boolean checkForTests;
        private final String testResults;
        private final String properties;
        // XXX unwieldy, would be nicer to have a builder
        TargetTemplate(String antName, String bundleKey, boolean mandatory, String artifactIncludes, String artifactExcludes,
                String javadocDir, boolean checkForTests, String testResults, String properties) {
            this.antName = antName;
            this.bundleKey = bundleKey;
            assert !mandatory || !checkForTests;
            this.mandatory = mandatory;
            this.artifactIncludes = artifactIncludes;
            this.artifactExcludes = artifactExcludes;
            this.javadocDir = javadocDir;
            this.checkForTests = checkForTests;
            this.testResults = testResults;
            this.properties = properties;
        }
    }

}
