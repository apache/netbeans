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

package org.netbeans.modules.maven.api;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.model.Dependency;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

public class PluginPropertyUtilsTest extends NbTestCase {

    public PluginPropertyUtilsTest(String name) {
        super(name);
    }

    private FileObject d;
    protected @Override void setUp() throws Exception {
        clearWorkDir();
        d = FileUtil.toFileObject(getWorkDir());
    }

    public void testGetPluginPropertyEvaluated() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "<build><directory>${project.basedir}/build/maven/${project.artifactId}/target</directory>" +
                "<plugins>" +
                "<plugin><groupId>g</groupId><artifactId>p</artifactId><version>0</version><configuration><key>${project.reporting.outputDirectory}/stuff</key></configuration></plugin>" +
                "</plugins></build>" +
                "</project>");
        assertEquals(new File(getWorkDir(), "build/maven/a/target/site/stuff"), new File(PluginPropertyUtils.getPluginProperty(ProjectManager.getDefault().findProject(d), "g", "p", "key", null)));
    }

    public void testGetPluginPropertyNotString() throws Exception { // #207098
        TestFileUtils.writeFile(d, "pom.xml",
                "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "<build><directory>${project.basedir}/build/maven/${project.artifactId}/target</directory>" +
                "<plugins>" +
                "<plugin><groupId>g</groupId><artifactId>p</artifactId><version>0</version><configuration><key/></configuration></plugin>" +
                "</plugins></build>" +
                "</project>");
        assertNull(null, PluginPropertyUtils.getPluginProperty(ProjectManager.getDefault().findProject(d), "g", "p", "key", null));
    }

    public void testGetReportPluginVersionM2() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "<reporting><plugins>" +
                "<plugin><groupId>g</groupId><artifactId>r</artifactId><version>17</version></plugin>" +
                "</plugins></reporting>" +
                "</project>");
        assertEquals("17", PluginPropertyUtils.getReportPluginVersion(ProjectManager.getDefault().findProject(d).getLookup().lookup(NbMavenProject.class).getMavenProject(), "g", "r"));
    }

    public void testGetReportPluginVersionM3() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "<build><plugins><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-site-plugin</artifactId><version>3.0</version><configuration><reportPlugins>" +
                "<plugin><groupId>g</groupId><artifactId>r</artifactId><version>17</version></plugin>" +
                "</reportPlugins></configuration></plugin></plugins></build>" +
                "</project>");
        assertEquals("17", PluginPropertyUtils.getReportPluginVersion(ProjectManager.getDefault().findProject(d).getLookup().lookup(NbMavenProject.class).getMavenProject(), "g", "r"));
    }

    public void testGetReportPluginPropertyM2() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "<reporting><plugins>" +
                "<plugin><groupId>g</groupId><artifactId>r</artifactId><version>0</version><configuration><key>value</key></configuration></plugin>" +
                "</plugins></reporting>" +
                "</project>");
        assertEquals("value", PluginPropertyUtils.getReportPluginProperty(ProjectManager.getDefault().findProject(d), "g", "r", "key", null));
    }

    public void testGetReportPluginPropertyM3() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "<build><plugins><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-site-plugin</artifactId><version>3.0</version><configuration><reportPlugins>" +
                "<plugin><groupId>g</groupId><artifactId>r</artifactId><version>0</version><configuration><key>value</key></configuration></plugin>" +
                "</reportPlugins></configuration></plugin></plugins></build>" +
                "</project>");
        assertEquals("value", PluginPropertyUtils.getReportPluginProperty(ProjectManager.getDefault().findProject(d), "g", "r", "key", null));
    }

    public void testGetReportPluginPropertyListM2() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "<reporting><plugins>" +
                "<plugin><groupId>g</groupId><artifactId>r</artifactId><version>0</version><configuration><things><thing>one</thing><thing>two</thing></things></configuration></plugin>" +
                "</plugins></reporting>" +
                "</project>");
        assertEquals("[one, two]", Arrays.toString(PluginPropertyUtils.getReportPluginPropertyList(ProjectManager.getDefault().findProject(d), "g", "r", "things", "thing", null)));
    }

    public void testGetReportPluginPropertyListM3() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version>" +
                "<build><plugins><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-site-plugin</artifactId><version>3.0</version><configuration><reportPlugins>" +
                "<plugin><groupId>g</groupId><artifactId>r</artifactId><version>0</version><configuration><things><thing>one</thing><thing>two</thing></things></configuration></plugin>" +
                "</reportPlugins></configuration></plugin></plugins></build>" +
                "</project>");
        assertEquals("[one, two]", Arrays.toString(PluginPropertyUtils.getReportPluginPropertyList(ProjectManager.getDefault().findProject(d), "g", "r", "things", "thing", null)));
    }
    public void testGetCompilerArgs() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project>"
                        + "<modelVersion>4.0.0</modelVersion>"
                        + "<groupId>g</groupId>"
                        + "<artifactId>a</artifactId>"
                        + "<version>0</version>"
                        + "<build>"
                        + "<plugins><plugin>"
                        + "<groupId>org.apache.maven.plugins</groupId>"
                        + "<artifactId>maven-compiler-plugin</artifactId>"
                        + "<version>3.8.0</version>"
                        + "<configuration><compilerArgs><arg>--enable-preview</arg></compilerArgs>"
                        + "</configuration>"
                        + "</plugin></plugins></build></project>");
        assertEquals("[--enable-preview]", Arrays.toString(PluginPropertyUtils.getPluginPropertyList(ProjectManager.getDefault().findProject(d), "org.apache.maven.plugins", "maven-compiler-plugin", "compilerArgs", "arg", null)));
    }

    public void testDependencyListBuilder() throws Exception {
        String testPom =
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                <modelVersion>4.0.0</modelVersion>
                <groupId>let.me.reproduce</groupId>
                <artifactId>annotation-processor-netbeans-reproducer</artifactId>
                <version>1.0-SNAPSHOT</version>
                <packaging>jar</packaging>
                <properties>
                    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                    <maven.compiler.release>21</maven.compiler.release>
                    <exec.mainClass>let.me.reproduce.annotation.processor.netbeans.reproducer.Main</exec.mainClass>
                </properties>

                <dependencies>
                    <dependency>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct</artifactId>
                        <version>1.5.5.Final</version>
                    </dependency>
                    <dependency>
                        <groupId>io.soabase.record-builder</groupId>
                        <artifactId>record-builder-core</artifactId>
                        <version>42</version>
                    </dependency>
                </dependencies>

                <build>
                    <plugins>
                        <plugin>
                            <groupId>org.apache.maven.plugins</groupId>
                            <artifactId>maven-compiler-plugin</artifactId>
                            <version>3.13.0</version>
                            <configuration>
                                <parameters>true</parameters>
                                <annotationProcessorPaths>
                                    <path>
                                        <groupId>org.mapstruct</groupId>
                                        <artifactId>mapstruct-processor</artifactId>
                                        <version>1.5.5.Final</version>
                                    </path>
                                    <path>
                                        <groupId>io.soabase.record-builder</groupId>
                                        <artifactId>record-builder-processor</artifactId>
                                        <version>42</version>
                                    </path>
                                </annotationProcessorPaths>
                            </configuration>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """;
        Xpp3Dom configRoot = Xpp3DomBuilder.build(new StringReader(testPom)).getChild("build").getChild("plugins").getChildren()[0].getChild("configuration");

        // Matching filter for propertyItemName should yield correct result
        PluginPropertyUtils.DependencyListBuilder bld = new PluginPropertyUtils.DependencyListBuilder(
                null,
                "annotationProcessorPaths",
                null
        );
        List<Dependency> dependencies = bld.build(configRoot, PluginPropertyUtils.DUMMY_EVALUATOR);
        assertEquals(2, dependencies.size());

        String testPom2 =
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                <modelVersion>4.0.0</modelVersion>
                <groupId>let.me.reproduce</groupId>
                <artifactId>annotation-processor-netbeans-reproducer</artifactId>
                <version>1.0-SNAPSHOT</version>
                <packaging>jar</packaging>
                <properties>
                    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                    <maven.compiler.release>21</maven.compiler.release>
                    <exec.mainClass>let.me.reproduce.annotation.processor.netbeans.reproducer.Main</exec.mainClass>
                </properties>

                <dependencies>
                    <dependency>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct</artifactId>
                        <version>1.5.5.Final</version>
                    </dependency>
                    <dependency>
                        <groupId>io.soabase.record-builder</groupId>
                        <artifactId>record-builder-core</artifactId>
                        <version>42</version>
                    </dependency>
                </dependencies>

                <build>
                    <plugins>
                        <plugin>
                            <groupId>org.apache.maven.plugins</groupId>
                            <artifactId>maven-compiler-plugin</artifactId>
                            <version>3.13.0</version>
                            <configuration>
                                <parameters>true</parameters>
                                <annotationProcessorPaths>
                                    <annotationProcessorPath>
                                        <groupId>org.mapstruct</groupId>
                                        <artifactId>mapstruct-processor</artifactId>
                                        <version>1.5.5.Final</version>
                                    </annotationProcessorPath>
                                    <annotationProcessorPath2>
                                        <groupId>io.soabase.record-builder</groupId>
                                        <artifactId>record-builder-processor</artifactId>
                                        <version>42</version>
                                    </annotationProcessorPath2>
                                </annotationProcessorPaths>
                            </configuration>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """;

        // Filter with null value for propertyItemName should yield full list
        Xpp3Dom configRoot2 = Xpp3DomBuilder.build(new StringReader(testPom2)).getChild("build").getChild("plugins").getChildren()[0].getChild("configuration");
        PluginPropertyUtils.DependencyListBuilder bld2 = new PluginPropertyUtils.DependencyListBuilder(
                null,
                "annotationProcessorPaths",
                null
        );
        List<Dependency> dependencies3 = bld2.build(configRoot2, PluginPropertyUtils.DUMMY_EVALUATOR);
        assertEquals(2, dependencies3.size());
    }

    public void testDependencyBuilderWithDependencyManagement() throws IOException {
        TestFileUtils.writeFile(d, "pom.xml",
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>let.me.reproduce</groupId>
                    <artifactId>annotation-processor-netbeans-reproducer</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>org.projectlombok</groupId>
                                <artifactId>lombok</artifactId>
                                <version>1.18.36</version>
                            </dependency>
                        </dependencies>
                    </dependencyManagement>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.13.0</version>
                                <configuration>
                                    <annotationProcessorPaths>
                                        <path>
                                            <groupId>org.projectlombok</groupId>
                                            <artifactId>lombok</artifactId>
                                            <type>jar</type>
                                        </path>
                                    </annotationProcessorPaths>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """
        );
        Project project = ProjectManager.getDefault().findProject(d);
        assert project != null;
        PluginPropertyUtils.PluginConfigPathParams query = new PluginPropertyUtils.PluginConfigPathParams("org.apache.maven.plugins", "maven-compiler-plugin", "annotationProcessorPaths");
        query.setDefaultScope(Artifact.SCOPE_RUNTIME);
        query.setGoal("runtime");
        List<ArtifactResolutionException> errorList = new ArrayList<>();
        List<Artifact> artifacts = PluginPropertyUtils.getPluginPathProperty(project, query, true, errorList);
        assertNotNull(artifacts);
        assert artifacts != null;
        assertEquals(1, artifacts.size());
        assertEquals("org.projectlombok", artifacts.get(0).getGroupId());
        assertEquals("lombok", artifacts.get(0).getArtifactId());
        assertEquals("1.18.36", artifacts.get(0).getVersion());
        assertNull(artifacts.get(0).getClassifier());
    }

    public void testDependencyBuilderWithoutVersion() throws IOException {
        TestFileUtils.writeFile(d, "pom.xml",
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>let.me.reproduce</groupId>
                    <artifactId>annotation-processor-netbeans-reproducer</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.13.0</version>
                                <configuration>
                                    <annotationProcessorPaths>
                                        <path>
                                            <groupId>org.projectlombok</groupId>
                                            <artifactId>lombok</artifactId>
                                        </path>
                                    </annotationProcessorPaths>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """
        );
        Project project = ProjectManager.getDefault().findProject(d);
        assert project != null;
        PluginPropertyUtils.PluginConfigPathParams query = new PluginPropertyUtils.PluginConfigPathParams("org.apache.maven.plugins", "maven-compiler-plugin", "annotationProcessorPaths");
        query.setDefaultScope(Artifact.SCOPE_RUNTIME);
        query.setGoal("runtime");
        List<ArtifactResolutionException> errorList = new ArrayList<>();
        List<Artifact> artifacts = PluginPropertyUtils.getPluginPathProperty(project, query, true, errorList);
        assertNotNull(artifacts);
        assert artifacts != null;
        assertEquals(0, artifacts.size());
    }
}
