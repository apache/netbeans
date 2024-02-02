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
        runTest("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "</project>",
                "21",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "                <configuration>\n" +
                "                    <compilerArgs>\n" +
                "                        <arg>--enable-preview</arg>\n" +
                "                    </compilerArgs>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "    <properties>\n" +
                "        <maven.compiler.release>21</maven.compiler.release>\n" +
                "    </properties>\n" +
                "</project>");
    }

    public void testOverwriteReleaseProperty() throws Exception {
        runTest("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <properties>\n" +
                "        <maven.compiler.release>17</maven.compiler.release>\n" +
                "    </properties>\n" +
                "</project>",
                "21",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "                <configuration>\n" +
                "                    <compilerArgs>\n" +
                "                        <arg>--enable-preview</arg>\n" +
                "                    </compilerArgs>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "    <properties>\n" +
                "        <maven.compiler.release>21</maven.compiler.release>\n" +
                "    </properties>\n" +
                "</project>");
    }

    public void testOverwriteReleaseConfigOption() throws Exception {
        runTest("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "                <configuration>\n" +
                "                    <release>17</release>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "    <properties>\n" +
                "        <maven.compiler.release>17</maven.compiler.release>\n" +
                "    </properties>\n" +
                "</project>",
                "21",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "                <configuration>\n" +
                "                    <release>21</release>\n" +
                "                    <compilerArgs>\n" +
                "                        <arg>--enable-preview</arg>\n" +
                "                    </compilerArgs>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "    <properties>\n" +
                "        <maven.compiler.release>17</maven.compiler.release>\n" +
                "    </properties>\n" +
                "</project>");
    }

    public void testOverwriteSourceProperty() throws Exception {
        runTest("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <properties>\n" +
                "        <maven.compiler.source>17</maven.compiler.source>\n" +
                "    </properties>\n" +
                "</project>",
                "21",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "                <configuration>\n" +
                "                    <compilerArgs>\n" +
                "                        <arg>--enable-preview</arg>\n" +
                "                    </compilerArgs>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "    <properties>\n" +
                "        <maven.compiler.source>21</maven.compiler.source>\n" +
                "        <maven.compiler.target>21</maven.compiler.target>\n" +
                "    </properties>\n" +
                "</project>");
    }

    public void testOverwriteSourceConfigOption() throws Exception {
        runTest("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "                <configuration>\n" +
                "                    <source>17</source>\n" +
                "                    <target>17</target>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "    <properties>\n" +
                "        <maven.compiler.source>17</maven.compiler.source>\n" +
                "    </properties>\n" +
                "</project>",
                "21",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "                <configuration>\n" +
                "                    <source>21</source>\n" +
                "                    <target>21</target>\n" +
                "                    <compilerArgs>\n" +
                "                        <arg>--enable-preview</arg>\n" +
                "                    </compilerArgs>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "    <properties>\n" +
                "        <maven.compiler.source>17</maven.compiler.source>\n" +
                "    </properties>\n" +
                "</project>");
    }

    public void testReleasePropertyOverSourceProperty() throws Exception {
        runTest("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <properties>\n" +
                "        <maven.compiler.source>17</maven.compiler.source>\n" +
                "        <maven.compiler.release>17</maven.compiler.release>\n" +
                "    </properties>\n" +
                "</project>",
                "21",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "                <configuration>\n" +
                "                    <compilerArgs>\n" +
                "                        <arg>--enable-preview</arg>\n" +
                "                    </compilerArgs>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "    <properties>\n" +
                "        <maven.compiler.source>17</maven.compiler.source>\n" +
                "        <maven.compiler.release>21</maven.compiler.release>\n" +
                "    </properties>\n" +
                "</project>");
    }

    public void testSourceConfigOptionOverReleaseProperty() throws Exception {
        runTest("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "                <configuration>\n" +
                "                    <source>17</source>\n" +
                "                    <target>17</target>\n" +
                "                    <compilerArgs>\n" +
                "                        <arg>-Dany=value</arg>\n" +
                "                    </compilerArgs>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "    <properties>\n" +
                "        <maven.compiler.release>17</maven.compiler.release>\n" +
                "    </properties>\n" +
                "</project>",
                "21",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "                <configuration>\n" +
                "                    <source>21</source>\n" +
                "                    <target>21</target>\n" +
                "                    <compilerArgs>\n" +
                "                        <arg>-Dany=value</arg>\n" +
                "                        <arg>--enable-preview</arg>\n" +
                "                    </compilerArgs>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "    <properties>\n" +
                "        <maven.compiler.release>17</maven.compiler.release>\n" +
                "    </properties>\n" +
                "</project>");
    }

    public void testReleaseConfigOptionOverSourceConfigOption() throws Exception {
        runTest("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "                <configuration>\n" +
                "                    <source>17</source>\n" +
                "                    <target>17</target>\n" +
                "                    <release>17</release>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "</project>",
                "21",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "                <configuration>\n" +
                "                    <source>17</source>\n" +
                "                    <target>17</target>\n" +
                "                    <release>21</release>\n" +
                "                    <compilerArgs>\n" +
                "                        <arg>--enable-preview</arg>\n" +
                "                    </compilerArgs>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "</project>");
    }

    public void testReleaseConfigOptionOverSourceProperty() throws Exception {
        runTest("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "                <configuration>\n" +
                "                    <release>17</release>\n" +
                "                    <compilerArgs>\n" +
                "                    </compilerArgs>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "    <properties>\n" +
                "        <maven.compiler.source>17</maven.compiler.source>\n" +
                "    </properties>\n" +
                "</project>",
                "21",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "                <configuration>\n" +
                "                    <release>21</release>\n" +
                "                    <compilerArgs>\n" +
                "                        <arg>--enable-preview</arg>\n" +
                "                    </compilerArgs>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "    <properties>\n" +
                "        <maven.compiler.source>17</maven.compiler.source>\n" +
                "    </properties>\n" +
                "</project>");
    }

    public void testCanChangeSourceLevel() throws Exception {
        runTest("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "                <configuration>\n" +
                "                    <release>${maven.compiler.source}</release>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "    <properties>\n" +
                "        <maven.compiler.source>17</maven.compiler.source>\n" +
                "    </properties>\n" +
                "</project>",
                "21",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "                <configuration>\n" +
                "                    <release>${maven.compiler.source}</release>\n" +
                "                    <compilerArgs>\n" +
                "                        <arg>--enable-preview</arg>\n" +
                "                    </compilerArgs>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "    <properties>\n" +
                "        <maven.compiler.source>17</maven.compiler.source>\n" +
                "    </properties>\n" +
                "</project>",
                false);
    }

    public void testOldPluginNoRelease() throws Exception {
        runTest("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.5.1</version>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "</project>",
                "21",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.5.1</version>\n" +
                "                <configuration>\n" +
                "                    <compilerArgs>\n" +
                "                        <arg>--enable-preview</arg>\n" +
                "                    </compilerArgs>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "    <properties>\n" +
                "        <maven.compiler.source>21</maven.compiler.source>\n" +
                "        <maven.compiler.target>21</maven.compiler.target>\n" +
                "    </properties>\n" +
                "</project>",
                true);
    }

    public void testNewPluginNoRelease() throws Exception {
        runTest("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.6.0</version>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "</project>",
                "21",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.6.0</version>\n" +
                "                <configuration>\n" +
                "                    <compilerArgs>\n" +
                "                        <arg>--enable-preview</arg>\n" +
                "                    </compilerArgs>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "    <properties>\n" +
                "        <maven.compiler.release>21</maven.compiler.release>\n" +
                "    </properties>\n" +
                "</project>",
                true);
    }

    public void testNoSourceChange1() throws Exception {
        runTest("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "    <properties>\n" +
                "        <maven.compiler.release>21</maven.compiler.release>\n" +
                "    </properties>\n" +
                "</project>",
                null,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "                <configuration>\n" +
                "                    <compilerArgs>\n" +
                "                        <arg>--enable-preview</arg>\n" +
                "                    </compilerArgs>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "    <properties>\n" +
                "        <maven.compiler.release>21</maven.compiler.release>\n" +
                "    </properties>\n" +
                "</project>",
                true);
    }

    public void testNoSourceChange2() throws Exception {
        runTest("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "                <configuration>\n" +
                "                    <source>21</source>\n" +
                "                    <target>21</target>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "</project>",
                null,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "                <configuration>\n" +
                "                    <source>21</source>\n" +
                "                    <target>21</target>\n" +
                "                    <compilerArgs>\n" +
                "                        <arg>--enable-preview</arg>\n" +
                "                    </compilerArgs>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "</project>",
                true);
    }

    public void testUpdatePluginWithImplicitGroupId() throws Exception {
        runTest("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "                <configuration>\n" +
                "                    <release>21</release>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "</project>",
                "21",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "                <configuration>\n" +
                "                    <release>21</release>\n" +
                "                    <compilerArgs>\n" +
                "                        <arg>--enable-preview</arg>\n" +
                "                    </compilerArgs>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "</project>");
    }

    public void testUpdatePluginInPluginManagement() throws Exception {
        runTest("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <properties>\n" +
                "        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
                "        <maven.compiler.release>21</maven.compiler.release>\n" +
                "    </properties>\n" +
                "    <build>\n" +
                "        <pluginManagement>\n" +
                "            <plugins>\n" +
                "                <plugin>\n" +
                "                    <groupId>org.apache.maven.plugins</groupId>\n" +
                "                    <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                    <version>3.11.0</version>\n" +
                "                </plugin>\n" +
                "            </plugins>\n" +
                "        </pluginManagement>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "</project>",
                "21",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <properties>\n" +
                "        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
                "        <maven.compiler.release>21</maven.compiler.release>\n" +
                "    </properties>\n" +
                "    <build>\n" +
                "        <pluginManagement>\n" +
                "            <plugins>\n" +
                "                <plugin>\n" +
                "                    <groupId>org.apache.maven.plugins</groupId>\n" +
                "                    <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                    <version>3.11.0</version>\n" +
                "                    <configuration>\n" +
                "                        <compilerArgs>\n" +
                "                            <arg>--enable-preview</arg>\n" +
                "                        </compilerArgs>\n" +
                "                    </configuration>\n" +
                "                </plugin>\n" +
                "            </plugins>\n" +
                "        </pluginManagement>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "</project>");
    }

    public void testPluginManagement() throws Exception {
        runTest("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <pluginManagement>\n" +
                "            <plugins>\n" +
                "                <plugin>\n" +
                "                    <groupId>org.apache.maven.plugins</groupId>\n" +
                "                    <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                    <version>3.11.0</version>\n" +
                "                    <configuration>\n" +
                "                        <release>17</release>\n" +
                "                    </configuration>\n" +
                "                </plugin>\n" +
                "            </plugins>\n" +
                "        </pluginManagement>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "                <configuration>\n" +
                "                    <release>${maven.compiler.source}</release>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "    <properties>\n" +
                "        <maven.compiler.source>17</maven.compiler.source>\n" +
                "    </properties>\n" +
                "</project>",
                "21",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <pluginManagement>\n" +
                "            <plugins>\n" +
                "                <plugin>\n" +
                "                    <groupId>org.apache.maven.plugins</groupId>\n" +
                "                    <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                    <version>3.11.0</version>\n" +
                "                    <configuration>\n" +
                "                        <release>21</release>\n" +
                "                        <compilerArgs>\n" +
                "                            <arg>--enable-preview</arg>\n" +
                "                        </compilerArgs>\n" +
                "                    </configuration>\n" +
                "                </plugin>\n" +
                "            </plugins>\n" +
                "        </pluginManagement>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "                <configuration>\n" +
                "                    <release>${maven.compiler.source}</release>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "    <properties>\n" +
                "        <maven.compiler.source>17</maven.compiler.source>\n" +
                "    </properties>\n" +
                "</project>");
    }

    public void testHasBuildButNotCompilerPlugin() throws Exception {
        runTest("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-surefire-plugin</artifactId>\n" +
                "                <version>3.1.0</version>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "</project>",
                "21",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>test</groupId>\n" +
                "    <artifactId>mavenproject1</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-surefire-plugin</artifactId>\n" +
                "                <version>3.1.0</version>\n" +
                "            </plugin>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.11.0</version>\n" +
                "                <configuration>\n" +
                "                    <compilerArgs>\n" +
                "                        <arg>--enable-preview</arg>\n" +
                "                    </compilerArgs>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "    <properties>\n" +
                "        <maven.compiler.release>21</maven.compiler.release>\n" +
                "    </properties>\n" +
                "</project>");
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
