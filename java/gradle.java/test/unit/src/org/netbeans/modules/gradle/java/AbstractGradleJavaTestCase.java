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
package org.netbeans.modules.gradle.java;

import java.io.File;
import java.io.IOException;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
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
public class AbstractGradleJavaTestCase extends NbTestCase {

    public AbstractGradleJavaTestCase(String name) {
        super(name);
    }

    /** Represents destination directory with NetBeans (always available). */
    protected File destDirF;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        destDirF = getTestNBDestDir();
        InstalledFileLocatorImpl.registerDestDir(destDirF);
    }

    protected FileObject createGradleProject(String buildScript) throws IOException {
        return getGradleProject(buildScript, null);
    }

    protected Project openProject(FileObject projectDir) throws IOException {
        Project prj = ProjectManager.getDefault().findProject(projectDir);
        assertNotNull(prj);
        ProjectOpenedTrampoline.DEFAULT.projectOpened(prj.getLookup().lookup(ProjectOpenedHook.class));
        return prj;
    }

    protected FileObject getGradleProject(String buildScript, String settingsScript) throws IOException {
        FileObject ret = FileUtil.toFileObject(getWorkDir());
        TestFileUtils.writeFile(ret, "build.gradle", buildScript);
        if (settingsScript != null) {
            TestFileUtils.writeFile(ret, "settings.gradle", settingsScript);
        }
        return ret;
    }

    private static File getTestNBDestDir() {
        String destDir = System.getProperty("test.netbeans.dest.dir");
        // set in project.properties as test-unit-sys-prop.test.netbeans.dest.dir
        assertNotNull("test.netbeans.dest.dir property has to be set when running within binary distribution", destDir);
        return new File(destDir);
    }
}
