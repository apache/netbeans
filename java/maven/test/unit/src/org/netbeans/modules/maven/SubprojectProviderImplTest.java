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

package org.netbeans.modules.maven;

import java.util.Collections;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

public class SubprojectProviderImplTest extends NbTestCase {

    public SubprojectProviderImplTest(String n) {
        super(n);
    }

    private FileObject d;
    protected @Override void setUp() throws Exception {
        clearWorkDir();
        d = FileUtil.toFileObject(getWorkDir());
    }

    public void testProjectModules() throws Exception {
        TestFileUtils.writeFile(d, "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>p1</artifactId>" +
                "<version>0</version>" +
                "<packaging>pom</packaging>" +
                "<modules>" +
                "<module>sub</module>" +
                "</modules>" +
                "</project>");
        TestFileUtils.writeFile(d, "sub/pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>p2</artifactId>" +
                "<version>0</version>" +
                "</project>");
        Project p1 = ProjectManager.getDefault().findProject(d);
        Project p2 = ProjectManager.getDefault().findProject(d.getFileObject("sub"));
        assertEquals(Collections.singleton(p2), p1.getLookup().lookup(SubprojectProvider.class).getSubprojects());
    }

    public void testNonDirUsedAsModule() throws Exception { // #199502
        TestFileUtils.writeFile(d, "pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>p1</artifactId>" +
                "<version>0</version>" +
                "<packaging>pom</packaging>" +
                "<modules>" +
                "<module>sub/pom.xml</module>" +
                "</modules>" +
                "</project>");
        TestFileUtils.writeFile(d, "sub/pom.xml",
                "<project xmlns='http://maven.apache.org/POM/4.0.0'>" +
                "<modelVersion>4.0.0</modelVersion>" +
                "<groupId>grp</groupId>" +
                "<artifactId>p2</artifactId>" +
                "</project>");
        Project p1 = ProjectManager.getDefault().findProject(d);
        assertEquals(Collections.emptySet(), p1.getLookup().lookup(SubprojectProvider.class).getSubprojects());
    }

}
