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
package org.netbeans.modules.gradle;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;

public class NbGradleProjectFactoryTest extends NbTestCase {
    private FileObject root;

    public NbGradleProjectFactoryTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        LocalFileSystem fs = new LocalFileSystem();
        fs.setRootDirectory(getWorkDir());
        root = fs.getRoot();
    }

    public void testNull() throws Exception {
        assertFalse(NbGradleProjectFactory.isProjectCheck(null, false));
        assertFalse(NbGradleProjectFactory.isProjectCheck(null, true));
    }

    public void testPomAndGradle() throws Exception {
        FileObject prj = root;
        FileObject pom = FileUtil.createData(prj, "pom.xml");
        FileObject gradle = FileUtil.createData(prj, "build.gradle");

        assertTrue("Gradle wins", NbGradleProjectFactory.isProjectCheck(prj, false));
        assertFalse("Pom wins", NbGradleProjectFactory.isProjectCheck(prj, true));
    }

    public void testPomNestedAndGradleNot() throws Exception {
        FileObject parentPrj = root;
        FileObject parentPom = FileUtil.createData(parentPrj, "pom.xml");
        FileObject prj = FileUtil.createFolder(parentPrj, "child");
        FileObject pom = FileUtil.createData(prj, "pom.xml");
        FileObject gradle = FileUtil.createData(prj, "build.gradle");

        assertFalse("Pom wins on settings", NbGradleProjectFactory.isProjectCheck(prj, true));
        assertFalse("Pom wins on parent pom", NbGradleProjectFactory.isProjectCheck(prj, false));
    }

    public void testPomAndGradleBothNested() throws Exception {
        FileObject parentPrj = root;
        FileObject parentPom = FileUtil.createData(parentPrj, "pom.xml");
        FileObject parentGradle = FileUtil.createData(parentPrj, "build.gradle");
        FileObject prj = FileUtil.createFolder(parentPrj, "child");
        FileObject pom = FileUtil.createData(prj, "pom.xml");
        FileObject gradle = FileUtil.createData(prj, "build.gradle");

        assertFalse("Parent Pom wins on settings", NbGradleProjectFactory.isProjectCheck(parentPrj, true));
        assertTrue("Parent Gradle wins", NbGradleProjectFactory.isProjectCheck(parentPrj, false));

        assertFalse("Pom wins on settings", NbGradleProjectFactory.isProjectCheck(prj, true));
        assertTrue("Gradle wins on parent build.gradle", NbGradleProjectFactory.isProjectCheck(prj, false));
    }

}
