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

package org.netbeans.modules.maven.runjar;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.execute.ActionToGoalUtils;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Reader;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

public class RunJarPrereqCheckerTest extends NbTestCase {

    public RunJarPrereqCheckerTest(String n) {
        super(n);
    }
    
    private FileObject d;

    protected @Override void setUp() throws Exception {
        clearWorkDir();
        d = FileUtil.toFileObject(getWorkDir());
    }

    public void testWriteMapping() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>testgrp</groupId>
                    <artifactId>testart</artifactId>
                    <version>1.0</version>
                </project>
                """);
        Project p = ProjectManager.getDefault().findProject(d);
        RunJarPrereqChecker.writeMapping("run", p, "my.App");
        TestFileUtils.touch(d.getFileObject("nbactions.xml"), null);
        M2ConfigProvider usr = p.getLookup().lookup(M2ConfigProvider.class);
        ActionToGoalMapping mapping = new NetbeansBuildActionXpp3Reader().read(new StringReader(usr.getDefaultConfig().getRawMappingsAsString()));
        assertEquals(1, mapping.getActions().size());
        RunJarPrereqChecker.writeMapping("run", p, "another.App");
        TestFileUtils.touch(d.getFileObject("nbactions.xml"), null);
        mapping = new NetbeansBuildActionXpp3Reader().read(new StringReader(usr.getDefaultConfig().getRawMappingsAsString()));
        assertEquals(1, mapping.getActions().size());
    }

    public void testMainClassManifest() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>testgrp</groupId>
                    <artifactId>testart</artifactId>
                    <version>1.0</version>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-jar-plugin</artifactId>
                                <configuration>
                                    <archive>
                                        <manifest>
                                            <mainClass>com.mycompany.Main2</mainClass>
                                        </manifest>
                                    </archive>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build></project>
                """);
        checkMainClass("com.mycompany.Main2");
    }

    public void testMainClassManifestProperty() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>testgrp</groupId>
                    <artifactId>testart</artifactId>
                    <version>1.0</version>
                    <properties>
                        <clazz>com.mycompany.Main1</clazz>
                    </properties>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-jar-plugin</artifactId>
                                <configuration>
                                    <archive>
                                        <manifest>
                                            <mainClass>${clazz}</mainClass>
                                        </manifest>
                                    </archive>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build></project>
                """);
        checkMainClass("com.mycompany.Main1");
    }

    public void testMainClassExecConfig() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>testgrp</groupId>
                    <artifactId>testart</artifactId>
                    <version>1.0</version>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.codehaus.mojo</groupId>
                                <artifactId>exec-maven-plugin</artifactId>
                                <configuration>
                                    <mainClass>com.example.Main2</mainClass>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build></project>
                """);
        checkMainClass("com.example.Main2");
    }

    public void testMainClassExecProperty1() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>testgrp</groupId>
                    <artifactId>testart</artifactId>
                    <version>1.0</version>
                    <properties>
                        <mainClass>org.demo.Main2</mainClass>
                    </properties>
                </project>
                """);
        checkMainClass("org.demo.Main2");
    }

    public void testMainClassExecProperty2() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>testgrp</groupId>
                    <artifactId>testart</artifactId>
                    <version>1.0</version>
                    <properties>
                        <exec.mainClass>org.demo.Main2</exec.mainClass>
                        <exec.java.bin>${java.home}/bin/java</exec.java.bin>
                    </properties>
                </project>
                """);
        checkMainClass("org.demo.Main2");
    }

    public void testMainClassExecProperty3() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>testgrp</groupId>
                    <artifactId>testart</artifactId>
                    <version>1.0</version>
                    <properties>
                        <project.mainclass>org.demo.Main2</project.mainclass>
                        <exec.java.bin>${java.home}/bin/java</exec.java.bin>
                    </properties>
                </project>
                """);
        checkMainClass("org.demo.Main2");
    }

    private void checkMainClass(String mainClass) throws IOException {
        Project proj = ProjectManager.getDefault().findProject(d);
        RunConfig rc = ActionToGoalUtils.createRunConfig(ActionProvider.COMMAND_RUN, proj.getLookup().lookup(NbMavenProjectImpl.class), proj.getLookup());
        rc.addProperties(Map.of("testMainClass", "${packageClassName}"));
        new RunJarPrereqChecker().checkRunConfig(rc);
        assertEquals(mainClass, rc.getProperties().get("testMainClass"));
    }
}
