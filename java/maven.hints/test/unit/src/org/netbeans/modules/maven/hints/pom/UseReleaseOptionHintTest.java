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
package org.netbeans.modules.maven.hints.pom;

import java.util.List;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

import static junit.framework.TestCase.assertEquals;

/**
 *
 * @author mbien
 */
public class UseReleaseOptionHintTest extends NbTestCase {

    // contains JDK 9+ compatible compiler plugin (3.10.1)
    private static final ComparableVersion JDK_9_PLUS_COMPATIBLE = new ComparableVersion("3.9.1");

    // contains old default compiler plugin, not supporting the release javac option
    private static final ComparableVersion JDK_8_COMPATIBLE = new ComparableVersion("3.8.0");

    private FileObject work;

    public UseReleaseOptionHintTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        work = FileUtil.toFileObject(getWorkDir());
        PomModelUtils.activeMavenVersion = JDK_9_PLUS_COMPATIBLE;
    }

    public void testImplicitCompilerPlugin() throws Exception {
        FileObject pom = TestFileUtils.writeFile(work, "pom.xml",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
            "    <modelVersion>4.0.0</modelVersion>\n" +
            "    <groupId>test</groupId>\n" +
            "    <artifactId>mavenproject1</artifactId>\n" +
            "    <version>1.0-SNAPSHOT</version>\n" +
            "    <packaging>jar</packaging>\n" +
            "    <properties>\n" +
            "        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
            "        <exec.mainClass>test.mavenproject1.Mavenproject1</exec.mainClass>\n" +
            "        <maven.compiler.source>11</maven.compiler.source>\n" +
            "        <maven.compiler.target>11</maven.compiler.target>\n" +
            "    </properties>\n" +
            "</project>");

        POMModel model = POMModelFactory.getDefault().getModel(Utilities.createModelSource(pom));
        Project project = ProjectManager.getDefault().findProject(pom.getParent());

        PomModelUtils.activeMavenVersion = JDK_8_COMPATIBLE;
        List<ErrorDescription> hints = new UseReleaseOptionHint().getErrorsForDocument(model, project);
        assertEquals(0, hints.size());
        
        PomModelUtils.activeMavenVersion = JDK_9_PLUS_COMPATIBLE;
        hints = new UseReleaseOptionHint().getErrorsForDocument(model, project);
        assertEquals(2, hints.size());
    }

    private static final String COMPILER_POM =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
            "    <modelVersion>4.0.0</modelVersion>\n" +
            "    <groupId>test</groupId>\n" +
            "    <artifactId>mavenproject1</artifactId>\n" +
            "    <version>1.0-SNAPSHOT</version>\n" +
            "    <packaging>jar</packaging>\n" +
            "    <properties>\n" +
            "        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
            "        <exec.mainClass>test.mavenproject1.Mavenproject1</exec.mainClass>\n" +
            "        <prop>11</prop>\n" +
            "    </properties>\n" +
            "    <build>\n" +
            "        <plugins>\n" +
            "            <plugin>\n" +
            "                <groupId>org.apache.maven.plugins</groupId>\n" +
            "                <artifactId>maven-compiler-plugin</artifactId>\n" +
            "                <version>3.10.1</version>\n" +
            "                <configuration>\n" +
            "                    <source>11</source>\n" +
            "                    <target>11</target>\n" +
            "                </configuration>" +
            "                <executions>\n" +
            "                    <execution>\n" +
            "                        <id>default-compile</id>\n" +
            "                        <configuration>\n" +
            "                            <source>${prop}</source>\n" +
            "                            <target>${prop}</target>\n" +
            "                        </configuration>\n" +
            "                    </execution>\n" +
            "                    <execution>\n" +
            "                        <id>default-testCompile</id>\n" +
            "                        <configuration>\n" +
            "                            <source>17</source>\n" +
            "                            <target>17</target>\n" +
            "                        </configuration>\n" +
            "                    </execution>\n" +
            "                </executions>\n" +
            "            </plugin>\n" +
            "        </plugins>\n" +
            "    </build>\n" +
            "</project>";

    public void testCompilerPlugin() throws Exception {
        FileObject pom = TestFileUtils.writeFile(work, "pom.xml", COMPILER_POM);

        POMModel model = POMModelFactory.getDefault().getModel(Utilities.createModelSource(pom));
        Project project = ProjectManager.getDefault().findProject(pom.getParent());

        List<ErrorDescription> hints = new UseReleaseOptionHint().getErrorsForDocument(model, project);
        assertEquals(6, hints.size());
    }

    public void testCompilerPluginWithNoGroupID() throws Exception {
        FileObject pom = TestFileUtils.writeFile(work, "pom.xml", COMPILER_POM.replaceFirst("<groupId>org.apache.maven.plugins</groupId>", ""));

        POMModel model = POMModelFactory.getDefault().getModel(Utilities.createModelSource(pom));
        Project project = ProjectManager.getDefault().findProject(pom.getParent());

        List<ErrorDescription> hints = new UseReleaseOptionHint().getErrorsForDocument(model, project);
        assertEquals(6, hints.size());
    }

    public void testOldCompilerPlugin() throws Exception {
        FileObject pom = TestFileUtils.writeFile(work, "pom.xml", COMPILER_POM.replaceFirst("3.10.1", "3.5"));

        POMModel model = POMModelFactory.getDefault().getModel(Utilities.createModelSource(pom));
        Project project = ProjectManager.getDefault().findProject(pom.getParent());

        List<ErrorDescription> hints = new UseReleaseOptionHint().getErrorsForDocument(model, project);
        assertEquals(0, hints.size());
    }

    public void testCompilerPluginWithOldTarget() throws Exception {
        FileObject pom = TestFileUtils.writeFile(work, "pom.xml", COMPILER_POM.replace("11", "5").replace("17", "1.4"));

        POMModel model = POMModelFactory.getDefault().getModel(Utilities.createModelSource(pom));
        Project project = ProjectManager.getDefault().findProject(pom.getParent());

        List<ErrorDescription> hints = new UseReleaseOptionHint().getErrorsForDocument(model, project);
        assertEquals(0, hints.size());
    }

}
