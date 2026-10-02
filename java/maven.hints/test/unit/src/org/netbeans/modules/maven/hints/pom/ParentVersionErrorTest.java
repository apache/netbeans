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

import java.util.Collections;
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

public class ParentVersionErrorTest extends NbTestCase {
    
    public ParentVersionErrorTest(String n) {
        super(n);
    }

    private FileObject work;

    protected @Override void setUp() throws Exception {
        clearWorkDir();
        work = FileUtil.toFileObject(getWorkDir());
    }

    public void testBasicUsage() throws Exception {
        TestFileUtils.writeFile(work, "pom.xml",
                """
                <project xmlns='http://maven.apache.org/POM/4.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd'>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>grp</groupId>
                    <artifactId>common</artifactId>
                    <version>1.1</version>
                </project>
                """);
        FileObject pom = TestFileUtils.writeFile(work, "prj/pom.xml", 
                """
                <project xmlns='http://maven.apache.org/POM/4.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd'>
                    <modelVersion>4.0.0</modelVersion>
                    <parent>
                        <groupId>grp</groupId>
                        <artifactId>common</artifactId>
                        <version>1.0</version>
                    </parent>
                    <version>1.0</version>
                    <artifactId>prj</artifactId>
                </project>
                """);
        POMModel model = POMModelFactory.getDefault().getModel(Utilities.createModelSource(pom));
        Project prj = ProjectManager.getDefault().findProject(pom.getParent());
        assertEquals(1, new ParentVersionError().getErrorsForDocument(model, prj).size());
    }

    public void testSpecialRelativePath() throws Exception { // #194281
        TestFileUtils.writeFile(work, "common.xml", 
                """
                <project xmlns='http://maven.apache.org/POM/4.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd'>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>grp</groupId>
                    <artifactId>common</artifactId>
                    <version>1.0</version>
                </project>
                """);
        FileObject pom = TestFileUtils.writeFile(work, "prj/pom.xml", 
                """
                <project xmlns='http://maven.apache.org/POM/4.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd'>
                    <modelVersion>4.0.0</modelVersion>
                    <parent>
                        <groupId>grp</groupId>
                        <artifactId>common</artifactId>
                        <relativePath>../common.xml</relativePath>
                    </parent>
                    <artifactId>prj</artifactId>
                </project>
                """);
        POMModel model = POMModelFactory.getDefault().getModel(Utilities.createModelSource(pom));
        Project prj = ProjectManager.getDefault().findProject(pom.getParent());
        assertEquals(Collections.<ErrorDescription>emptyList(), new ParentVersionError().getErrorsForDocument(model, prj));
    }
    
    public void testVariablePresentInVersion() throws Exception { // #194281
        TestFileUtils.writeFile(work, "pom.xml", 
                """
                <project xmlns='http://maven.apache.org/POM/4.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd'>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>grp</groupId>
                    <artifactId>common</artifactId>
                    <version>${revision}</version>
                    <properties>
                       <revision>1.1</revision>
                    </properties>
                </project>
                """);
        FileObject pom = TestFileUtils.writeFile(work, "prj/pom.xml", 
                """
                <project xmlns='http://maven.apache.org/POM/4.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd'>
                    <modelVersion>4.0.0</modelVersion>
                    <parent>
                        <groupId>grp</groupId>
                        <artifactId>common</artifactId>
                        <version>${revision}</version>
                    </parent>
                    <artifactId>prj</artifactId>
                </project>
                """);
        POMModel model = POMModelFactory.getDefault().getModel(Utilities.createModelSource(pom));
        Project prj = ProjectManager.getDefault().findProject(pom.getParent());
        assertEquals(Collections.<ErrorDescription>emptyList(), new ParentVersionError().getErrorsForDocument(model, prj));
    }
}
