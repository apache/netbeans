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

import java.io.File;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.TestFileUtils;

public class NbMavenProjectFactoryTest extends NbTestCase {

    public NbMavenProjectFactoryTest(String n) {
        super(n);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
    }

    public void testIsProjectRegular() throws Exception {
        File d = getWorkDir();
        TestFileUtils.writeFile(new File(d, "pom.xml"), "...");
        assertTrue(ProjectManager.getDefault().isProject(FileUtil.toFileObject(d)));
    }

    public void testIsProjectNotDiskFile() throws Exception {
        FileObject r = FileUtil.createMemoryFileSystem().getRoot();
        r.createData("pom.xml");
        assertFalse(ProjectManager.getDefault().isProject(r));
    }

    public void testIsProjectArchetypeResources() throws Exception {
        File d1 = getWorkDir();
        TestFileUtils.writeFile(new File(d1, "pom.xml"), "...");
        assertTrue(ProjectManager.getDefault().isProject(FileUtil.toFileObject(d1)));
        File d2 = new File(d1, "src/main/resources/archetype-resources");
        TestFileUtils.writeFile(new File(d2, "pom.xml"), "...");
        assertFalse(ProjectManager.getDefault().isProject(FileUtil.toFileObject(d2)));
        File d3 = new File(d2, "subprj");
        TestFileUtils.writeFile(new File(d3, "pom.xml"), "...");
        assertFalse(ProjectManager.getDefault().isProject(FileUtil.toFileObject(d3)));
    }

    public void testIsProjectTargetDir() throws Exception { // #198162
        File d1 = getWorkDir();
        TestFileUtils.writeFile(new File(d1, "pom.xml"), "...");
        assertTrue(ProjectManager.getDefault().isProject(FileUtil.toFileObject(d1)));
        File d2 = new File(d1, "target");
        TestFileUtils.writeFile(new File(d2, "pom.xml"), "...");
        assertFalse(ProjectManager.getDefault().isProject(FileUtil.toFileObject(d2)));
    }

}
