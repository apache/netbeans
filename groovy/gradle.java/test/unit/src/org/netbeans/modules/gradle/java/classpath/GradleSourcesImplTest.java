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
package org.netbeans.modules.gradle.java.classpath;

import java.io.File;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.apisupport.project.InstalledFileLocatorImpl;
import org.netbeans.modules.project.uiapi.ProjectOpenedTrampoline;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

/**
 *
 * @author lkishalmi
 */
public class GradleSourcesImplTest extends NbTestCase {

    public GradleSourcesImplTest(String name) {
        super(name);
    }

    private FileObject d;
    /** Represents destination directory with NetBeans (always available). */
    protected File destDirF;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        d = FileUtil.toFileObject(getWorkDir());
        destDirF = getTestNBDestDir();
    }

    public void testGeneratedSources() throws Exception { // #187595
        InstalledFileLocatorImpl.registerDestDir(destDirF);

        TestFileUtils.writeFile(d,
                "build.gradle",
                "apply plugin: 'java'\n" +
                "sourceSets { main { java { srcDirs = [ 'src', 'build/gen-src' ] }}}");
        FileObject src = FileUtil.createFolder(d, "src/");
        FileObject gsrc = FileUtil.createFolder(d, "build/gen-src");
        FileObject source = src.createData("Whatever.java");
        FileObject generated = gsrc.createData("WhateverGen.java");
        Project prj = ProjectManager.getDefault().findProject(d);
        assertNotNull(prj);
        ProjectOpenedTrampoline.DEFAULT.projectOpened(prj.getLookup().lookup(ProjectOpenedHook.class));
        Sources srcs = ProjectUtils.getSources(prj);
        SourceGroup[] groups = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        assertEquals(2, groups.length);
        assertTrue(groups[0].contains(source));
        assertFalse(groups[0].contains(generated));
        assertTrue(groups[1].contains(generated));
        assertFalse(groups[1].contains(source));
    }

    private static File getTestNBDestDir() {
        String destDir = System.getProperty("test.netbeans.dest.dir");
        // set in project.properties as test-unit-sys-prop.test.netbeans.dest.dir
        assertNotNull("test.netbeans.dest.dir property has to be set when running within binary distribution", destDir);
        return new File(destDir);
    }

}
