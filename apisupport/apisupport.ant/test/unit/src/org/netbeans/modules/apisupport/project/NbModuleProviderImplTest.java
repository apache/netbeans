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
package org.netbeans.modules.apisupport.project;

import java.io.File;
import java.util.logging.Level;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;

public class NbModuleProviderImplTest extends TestBase {

    public NbModuleProviderImplTest(String testName) {
        super(testName);
    }

    @Override protected Level logLevel() {
        return Level.INFO;
    }

    @Override protected void setUp() throws Exception {
        super.setUp();
        InstalledFileLocatorImpl.registerDestDir(destDirF);
    }

    public void testGetDependencyVersion() throws Exception {
        File targetPrjDir = new File(getWorkDir(), "testModule");
        NbModuleProjectGenerator.createStandAloneModule(
                targetPrjDir,
                "org.example.testModule", // cnb
                "Testing Module", // display name
                "org/example/testModule/resources/Bundle.properties",
                null,
                NbPlatform.PLATFORM_ID_DEFAULT, // platform id
                false,
                false);
        FileObject fo = FileUtil.toFileObject(targetPrjDir);

        Project p = ProjectManager.getDefault().findProject(fo);

        assertNotNull("have a project in " + targetPrjDir, p);

        NbModuleProvider nbModuleProvider = p.getLookup().lookup(NbModuleProvider.class);

        assertNotNull(nbModuleProvider);

        SpecificationVersion possibleVersion = nbModuleProvider.getDependencyVersion("org.openide.util");
        assertNotNull("initially reports version from platform", possibleVersion);
        assertTrue(possibleVersion.toString(), possibleVersion.compareTo(new SpecificationVersion("8.19")) >= 0);

        nbModuleProvider.addDependencies(new NbModuleProvider.ModuleDependency[] {new NbModuleProvider.ModuleDependency("org.openide.util", null, new SpecificationVersion("6.0"), true)});

        assertTrue(nbModuleProvider.hasDependency("org.openide.util"));
        SpecificationVersion v = nbModuleProvider.getDependencyVersion("org.openide.util");
        assertNotNull(v);
        assertTrue(v.compareTo(new SpecificationVersion("8.22")) >= 0);

        nbModuleProvider.addDependencies(new NbModuleProvider.ModuleDependency[] {new NbModuleProvider.ModuleDependency("org.openide.util", null, new SpecificationVersion("7.0"), true)});

        assertTrue(nbModuleProvider.hasDependency("org.openide.util"));
        v = nbModuleProvider.getDependencyVersion("org.openide.util");
        assertNotNull(v);
        assertTrue(v.compareTo(new SpecificationVersion("8.22")) >= 0);
    }

}
