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

package org.netbeans.modules.apisupport.project;

import java.io.File;
import java.util.Collections;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

public class ExternalBuildDirTest extends TestBase {

    public ExternalBuildDirTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        InstalledFileLocatorImpl.registerDestDir(destDirF);
    }
    
    // XXX to be tested: FileOwnerQuery on build products; Source/JavadocForBinaryQuery; SharabilityQuery
    
    public void testBuild() throws Exception {
        SuiteProject suite = generateSuite("suite");
        EditableProperties ep = suite.getHelper().getProperties("nbproject/platform.properties");
        ep.put("suite.dir", "${basedir}");
        ep.put("suite.build.dir", "${suite.dir}/../build");
        ep.put("build.dir", "${suite.build.dir}/${ant.project.name}");
        suite.getHelper().putProperties("nbproject/platform.properties", ep);
        ProjectManager.getDefault().saveProject(suite);
        suite.open();
        NbModuleProject mod1 = generateSuiteComponent(suite, "mod1");
        TestFileUtils.writeFile(mod1.getProjectDirectory(), "src/org/example/mod1/C1.java", "package org.example.mod1; public class C1 {}");
        ProjectXMLManager.getInstance(FileUtil.toFile(mod1.getProjectDirectory())).replacePublicPackages(Collections.singleton("org.example.mod1"));
        ProjectManager.getDefault().saveProject(mod1);
        mod1.open();
        NbModuleProject mod2 = generateSuiteComponent(suite, "mod2");
        TestFileUtils.writeFile(mod2.getProjectDirectory(), "src/org/example/mod2/C2.java", "package org.example.mod2; public class C2 extends org.example.mod1.C1 {}");
        ProjectXMLManager.getInstance(FileUtil.toFile(mod2.getProjectDirectory())).addDependency(new ModuleDependency(mod2.getModuleList().getEntry("org.example.mod1")));
        ProjectManager.getDefault().saveProject(mod2);
        mod2.open();
        assertEquals(0, ActionUtils.runTarget(suite.getProjectDirectory().getFileObject("build.xml"), new String[] {"build"}, null).result());
        assertTrue(new File(getWorkDir(), "build/cluster/modules/org-example-mod2.jar").isFile());
        assertTrue(new File(getWorkDir(), "build/org.example.mod1/classes/org/example/mod1/C1.class").isFile());
        assertFalse(new File(getWorkDir(), "suite/build").exists());
        assertTrue(new File(getWorkDir(), "suite/mod1").isDirectory());
        assertFalse(new File(getWorkDir(), "suite/mod1/build").exists());
        assertFalse(new File(getWorkDir(), "suite/mod2/build").exists());
    }

}
