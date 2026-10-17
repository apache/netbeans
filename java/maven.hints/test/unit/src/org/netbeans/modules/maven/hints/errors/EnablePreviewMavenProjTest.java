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
package org.netbeans.modules.maven.hints.errors;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

import static junit.framework.TestCase.assertEquals;
import org.netbeans.modules.java.hints.spi.preview.PreviewEnabler;

public class EnablePreviewMavenProjTest extends NbTestCase {

    private FileObject work;

    public EnablePreviewMavenProjTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        work = FileUtil.toFileObject(getWorkDir());
    }

    public void testAddEverything() throws Exception {
        runTest("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                </project>
                """,
                "21",
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                                <configuration>
                                    <compilerArgs>
                                        <arg>--enable-preview</arg>
                                    </compilerArgs>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                    <properties>
                        <maven.compiler.release>21</maven.compiler.release>
                    </properties>
                </project>
                """);
    }

    public void testOverwriteReleaseProperty() throws Exception {
        runTest("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <properties>
                        <maven.compiler.release>17</maven.compiler.release>
                    </properties>
                </project>
                """,
                "21",
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                                <configuration>
                                    <compilerArgs>
                                        <arg>--enable-preview</arg>
                                    </compilerArgs>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                    <properties>
                        <maven.compiler.release>21</maven.compiler.release>
                    </properties>
                </project>
                """);
    }

    public void testOverwriteReleaseConfigOption() throws Exception {
        runTest("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                                <configuration>
                                    <release>17</release>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                    <properties>
                        <maven.compiler.release>17</maven.compiler.release>
                    </properties>
                </project>
                """,
                "21",
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                                <configuration>
                                    <release>21</release>
                                    <compilerArgs>
                                        <arg>--enable-preview</arg>
                                    </compilerArgs>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                    <properties>
                        <maven.compiler.release>17</maven.compiler.release>
                    </properties>
                </project>
                """);
    }

    public void testOverwriteSourceProperty() throws Exception {
        runTest("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <properties>
                        <maven.compiler.source>17</maven.compiler.source>
                    </properties>
                </project>
                """,
                "21",
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                                <configuration>
                                    <compilerArgs>
                                        <arg>--enable-preview</arg>
                                    </compilerArgs>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                    <properties>
                        <maven.compiler.source>21</maven.compiler.source>
                        <maven.compiler.target>21</maven.compiler.target>
                    </properties>
                </project>
                """);
    }

    public void testOverwriteSourceConfigOption() throws Exception {
        runTest("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                                <configuration>
                                    <source>17</source>
                                    <target>17</target>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                    <properties>
                        <maven.compiler.source>17</maven.compiler.source>
                    </properties>
                </project>
                """,
                "21",
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                                <configuration>
                                    <source>21</source>
                                    <target>21</target>
                                    <compilerArgs>
                                        <arg>--enable-preview</arg>
                                    </compilerArgs>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                    <properties>
                        <maven.compiler.source>17</maven.compiler.source>
                    </properties>
                </project>
                """);
    }

    public void testReleasePropertyOverSourceProperty() throws Exception {
        runTest("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <properties>
                        <maven.compiler.source>17</maven.compiler.source>
                        <maven.compiler.release>17</maven.compiler.release>
                    </properties>
                </project>
                """,
                "21",
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                                <configuration>
                                    <compilerArgs>
                                        <arg>--enable-preview</arg>
                                    </compilerArgs>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                    <properties>
                        <maven.compiler.source>17</maven.compiler.source>
                        <maven.compiler.release>21</maven.compiler.release>
                    </properties>
                </project>
                """);
    }

    public void testSourceConfigOptionOverReleaseProperty() throws Exception {
        runTest("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                                <configuration>
                                    <source>17</source>
                                    <target>17</target>
                                    <compilerArgs>
                                        <arg>-Dany=value</arg>
                                    </compilerArgs>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                    <properties>
                        <maven.compiler.release>17</maven.compiler.release>
                    </properties>
                </project>
                """,
                "21",
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                                <configuration>
                                    <source>21</source>
                                    <target>21</target>
                                    <compilerArgs>
                                        <arg>-Dany=value</arg>
                                        <arg>--enable-preview</arg>
                                    </compilerArgs>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                    <properties>
                        <maven.compiler.release>17</maven.compiler.release>
                    </properties>
                </project>
                """);
    }

    public void testReleaseConfigOptionOverSourceConfigOption() throws Exception {
        runTest("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                                <configuration>
                                    <source>17</source>
                                    <target>17</target>
                                    <release>17</release>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """,
                "21",
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                                <configuration>
                                    <source>17</source>
                                    <target>17</target>
                                    <release>21</release>
                                    <compilerArgs>
                                        <arg>--enable-preview</arg>
                                    </compilerArgs>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """);
    }

    public void testReleaseConfigOptionOverSourceProperty() throws Exception {
        runTest("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                                <configuration>
                                    <release>17</release>
                                    <compilerArgs>
                                    </compilerArgs>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                    <properties>
                        <maven.compiler.source>17</maven.compiler.source>
                    </properties>
                </project>
                """,
                "21",
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                                <configuration>
                                    <release>21</release>
                                    <compilerArgs>
                                        <arg>--enable-preview</arg>
                                    </compilerArgs>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                    <properties>
                        <maven.compiler.source>17</maven.compiler.source>
                    </properties>
                </project>
                """);
    }

    public void testCanChangeSourceLevel() throws Exception {
        runTest("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                                <configuration>
                                    <release>${maven.compiler.source}</release>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                    <properties>
                        <maven.compiler.source>17</maven.compiler.source>
                    </properties>
                </project>
                """,
                "21",
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                                <configuration>
                                    <release>${maven.compiler.source}</release>
                                    <compilerArgs>
                                        <arg>--enable-preview</arg>
                                    </compilerArgs>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                    <properties>
                        <maven.compiler.source>17</maven.compiler.source>
                    </properties>
                </project>
                """,
                false);
    }

    public void testOldPluginNoRelease() throws Exception {
        runTest("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.5.1</version>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """,
                "21",
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.5.1</version>
                                <configuration>
                                    <compilerArgs>
                                        <arg>--enable-preview</arg>
                                    </compilerArgs>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                    <properties>
                        <maven.compiler.source>21</maven.compiler.source>
                        <maven.compiler.target>21</maven.compiler.target>
                    </properties>
                </project>
                """,
                true);
    }

    public void testNewPluginNoRelease() throws Exception {
        runTest("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.6.0</version>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """,
                "21",
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.6.0</version>
                                <configuration>
                                    <compilerArgs>
                                        <arg>--enable-preview</arg>
                                    </compilerArgs>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                    <properties>
                        <maven.compiler.release>21</maven.compiler.release>
                    </properties>
                </project>
                """,
                true);
    }

    public void testNoSourceChange1() throws Exception {
        runTest("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                            </plugin>
                        </plugins>
                    </build>
                    <properties>
                        <maven.compiler.release>21</maven.compiler.release>
                    </properties>
                </project>
                """,
                null,
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                                <configuration>
                                    <compilerArgs>
                                        <arg>--enable-preview</arg>
                                    </compilerArgs>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                    <properties>
                        <maven.compiler.release>21</maven.compiler.release>
                    </properties>
                </project>
                """,
                true);
    }

    public void testNoSourceChange2() throws Exception {
        runTest("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                                <configuration>
                                    <source>21</source>
                                    <target>21</target>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """,
                null,
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                                <configuration>
                                    <source>21</source>
                                    <target>21</target>
                                    <compilerArgs>
                                        <arg>--enable-preview</arg>
                                    </compilerArgs>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """,
                true);
    }

    public void testUpdatePluginWithImplicitGroupId() throws Exception {
        runTest("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                                <configuration>
                                    <release>21</release>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """,
                "21",
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                                <configuration>
                                    <release>21</release>
                                    <compilerArgs>
                                        <arg>--enable-preview</arg>
                                    </compilerArgs>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """);
    }

    public void testUpdatePluginInPluginManagement() throws Exception {
        runTest("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <properties>
                        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                        <maven.compiler.release>21</maven.compiler.release>
                    </properties>
                    <build>
                        <pluginManagement>
                            <plugins>
                                <plugin>
                                    <groupId>org.apache.maven.plugins</groupId>
                                    <artifactId>maven-compiler-plugin</artifactId>
                                    <version>3.11.0</version>
                                </plugin>
                            </plugins>
                        </pluginManagement>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """,
                "21",
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <properties>
                        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                        <maven.compiler.release>21</maven.compiler.release>
                    </properties>
                    <build>
                        <pluginManagement>
                            <plugins>
                                <plugin>
                                    <groupId>org.apache.maven.plugins</groupId>
                                    <artifactId>maven-compiler-plugin</artifactId>
                                    <version>3.11.0</version>
                                    <configuration>
                                        <compilerArgs>
                                            <arg>--enable-preview</arg>
                                        </compilerArgs>
                                    </configuration>
                                </plugin>
                            </plugins>
                        </pluginManagement>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """);
    }

    public void testPluginManagement() throws Exception {
        runTest("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <pluginManagement>
                            <plugins>
                                <plugin>
                                    <groupId>org.apache.maven.plugins</groupId>
                                    <artifactId>maven-compiler-plugin</artifactId>
                                    <version>3.11.0</version>
                                    <configuration>
                                        <release>17</release>
                                    </configuration>
                                </plugin>
                            </plugins>
                        </pluginManagement>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                                <configuration>
                                    <release>${maven.compiler.source}</release>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                    <properties>
                        <maven.compiler.source>17</maven.compiler.source>
                    </properties>
                </project>
                """,
                "21",
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <pluginManagement>
                            <plugins>
                                <plugin>
                                    <groupId>org.apache.maven.plugins</groupId>
                                    <artifactId>maven-compiler-plugin</artifactId>
                                    <version>3.11.0</version>
                                    <configuration>
                                        <release>21</release>
                                        <compilerArgs>
                                            <arg>--enable-preview</arg>
                                        </compilerArgs>
                                    </configuration>
                                </plugin>
                            </plugins>
                        </pluginManagement>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                                <configuration>
                                    <release>${maven.compiler.source}</release>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                    <properties>
                        <maven.compiler.source>17</maven.compiler.source>
                    </properties>
                </project>
                """);
    }

    public void testHasBuildButNotCompilerPlugin() throws Exception {
        runTest("""
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-surefire-plugin</artifactId>
                                <version>3.1.0</version>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """,
                "21",
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>test</groupId>
                    <artifactId>mavenproject1</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>jar</packaging>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-surefire-plugin</artifactId>
                                <version>3.1.0</version>
                            </plugin>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.11.0</version>
                                <configuration>
                                    <compilerArgs>
                                        <arg>--enable-preview</arg>
                                    </compilerArgs>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                    <properties>
                        <maven.compiler.release>21</maven.compiler.release>
                    </properties>
                </project>
                """);
    }

    private void runTest(String original, String newSL, String expected) throws Exception {
        runTest(original, newSL, expected, true);
    }

    private void runTest(String original, String newSL, String expected, boolean canChangeSL) throws Exception {
        FileObject pom = TestFileUtils.writeFile(work, "pom.xml", original);

        POMModel model = POMModelFactory.getDefault().getModel(Utilities.createModelSource(pom));
        Project project = ProjectManager.getDefault().findProject(pom.getParent());

        PreviewEnabler enabler = new EnablePreviewMavenProj.FactoryImpl().enablerFor(pom);

        assertEquals(canChangeSL, enabler.canChangeSourceLevel());
        enabler.enablePreview(newSL);

        String actual = pom.asText();

        assertEquals(expected, actual);
    }
}
