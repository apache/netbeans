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

import org.netbeans.modules.apisupport.project.api.Util;
import java.io.File;
import java.util.Collections;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectGenerator;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 * Check that missing or invalid *.properties files do not badly break projects.
 * More or less corresponds to issue #66404 and others.
 * @author Jesse Glick
 */
public final class BrokenPlatformReferenceTest extends NbTestCase {
    
    public BrokenPlatformReferenceTest(String name) {
        super(name);
    }
    
    /** a fake but valid-looking install dir; the default NB platform */
    private File install;
    /** an alternate valid install dir */
    private File install2;
    /** the user dir */
    private File user;
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setLayersAndInstances(getClass().getClassLoader());
        NbPlatform.reset();
        user = new File(getWorkDir(), "user");
        user.mkdirs();
        System.setProperty("netbeans.user", user.getAbsolutePath());
        install = new File(getWorkDir(), "install");
        TestBase.makePlatform(install);
        // Now set up build.properties accordingly:
        InstalledFileLocatorImpl.registerDestDir(install);
        EditableProperties ep = PropertyUtils.getGlobalProperties();
        ep.put("nbplatform.default.netbeans.dest.dir", install.getAbsolutePath());
        ep.put("nbplatform.default.harness.dir", "${nbplatform.default.netbeans.dest.dir}/harness");
        PropertyUtils.putGlobalProperties(ep);
        install2 = new File(getWorkDir(), "install2");
        TestBase.makePlatform(install2);
        NbPlatform.addPlatform("install2", install2, "install2");
    }
    
    /** Make sure everything is working as expected when there are no breakages. */
    public void testEverythingNormal() throws Exception {
        // Try making a standalone module w/ default platform, confirm loaded OK.
        File d = new File(getWorkDir(), "standalone");
        NbModuleProjectGenerator.createStandAloneModule(d, "x", "X", null, null, NbPlatform.PLATFORM_ID_DEFAULT, false, true);
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(d));
        NbPlatform pl = p.getPlatform(false);
        assertNotNull(pl);
        assertEquals(install, pl.getDestDir());
        assertEquals(pl, p.getPlatform(true));
        // Same but w/ a non-default platform.
        d = new File(getWorkDir(), "standalone2");
        NbModuleProjectGenerator.createStandAloneModule(d, "x", "X", null, null, "install2", false, true);
        p = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(d));
        pl = p.getPlatform(false);
        assertNotNull(pl);
        assertEquals(install2, pl.getDestDir());
        // Same for suites.
        File sd = new File(getWorkDir(), "suite");
        SuiteProjectGenerator.createSuiteProject(sd, NbPlatform.PLATFORM_ID_DEFAULT, false);
        d = new File(getWorkDir(), "suitecomp");
        NbModuleProjectGenerator.createSuiteComponentModule(d, "x", "X", null, null, sd, false, true);
        SuiteProject s = (SuiteProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(sd));
        pl = s.getPlatform(false);
        assertNotNull(pl);
        assertEquals(install, pl.getDestDir());
        assertEquals(pl, s.getPlatform(true));
        p = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(d));
        assertEquals(pl, p.getPlatform(false));
        // And again w/ a non-default platform.
        sd = new File(getWorkDir(), "suite2");
        SuiteProjectGenerator.createSuiteProject(sd, "install2", false);
        d = new File(getWorkDir(), "suitecomp2");
        NbModuleProjectGenerator.createSuiteComponentModule(d, "x", "X", null, null, sd, false, true);
        s = (SuiteProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(sd));
        pl = s.getPlatform(false);
        assertNotNull(pl);
        assertEquals(install2, pl.getDestDir());
        p = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(d));
        assertEquals(pl, p.getPlatform(false));
    }
    
    /** Test that use of default platform is OK even if platform-private.properties is initially missing; must be created. */
    public void testMissingPlatformPrivatePropertiesDefaultPlatform() throws Exception {
        // Try making a standalone module w/ default platform.
        File d = new File(getWorkDir(), "standalone");
        NbModuleProjectGenerator.createStandAloneModule(d, "x", "X", null, null, NbPlatform.PLATFORM_ID_DEFAULT, false, true);
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(d));
        p.open();
        assertEquals(Collections.singletonMap("user.properties.file", new File(user, "build.properties").getAbsolutePath()),
                Util.loadProperties(p.getProjectDirectory().getFileObject("nbproject/private/platform-private.properties")));
        // Same for suite.
        File sd = new File(getWorkDir(), "suite");
        SuiteProjectGenerator.createSuiteProject(sd, NbPlatform.PLATFORM_ID_DEFAULT, false);
        SuiteProject s = (SuiteProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(sd));
        s.open();
        assertEquals(Collections.singletonMap("user.properties.file", new File(user, "build.properties").getAbsolutePath()),
                Util.loadProperties(s.getProjectDirectory().getFileObject("nbproject/private/platform-private.properties")));
    }
    
    /** Test that use of default platform is still fine even if platform-private.properties is initially incorrect; must be corrected. */
    public void testIncorrectPlatformPrivatePropertiesDefaultPlatform() throws Exception {
        // Try making a standalone module w/ default platform.
        File d = new File(getWorkDir(), "standalone");
        NbModuleProjectGenerator.createStandAloneModule(d, "x", "X", null, null, NbPlatform.PLATFORM_ID_DEFAULT, false, true);
        FileObject props = FileUtil.createData(FileUtil.toFileObject(d), "nbproject/private/platform-private.properties");
        Util.storeProperties(props, new EditableProperties(Collections.singletonMap("user.properties.file", "bogus")));
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(d));
        NbPlatform pl = p.getPlatform(true); // with fallback=false, who knows what it will be
        assertNotNull(pl);
        assertEquals(install, pl.getDestDir());
        p.open();
        assertEquals(Collections.singletonMap("user.properties.file", new File(user, "build.properties").getAbsolutePath()),
                Util.loadProperties(props));
        assertEquals(pl, p.getPlatform(true));
        assertEquals(pl, p.getPlatform(false)); // now should be corrected even w/o fallback
        // Same for suite. Check a component module too.
        File sd = new File(getWorkDir(), "suite");
        SuiteProjectGenerator.createSuiteProject(sd, NbPlatform.PLATFORM_ID_DEFAULT, false);
        props = FileUtil.createData(FileUtil.toFileObject(sd), "nbproject/private/platform-private.properties");
        Util.storeProperties(props, new EditableProperties(Collections.singletonMap("user.properties.file", "bogus")));
        d = new File(getWorkDir(), "suitecomp");
        NbModuleProjectGenerator.createSuiteComponentModule(d, "x", "X", null, null, sd, false, true);
        SuiteProject s = (SuiteProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(sd));
        p = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(d));
        pl = s.getPlatform(true);
        assertNotNull(pl);
        assertEquals(install, pl.getDestDir());
        assertEquals(pl, p.getPlatform(true));
        s.open();
        p.open(); // just in case
        assertEquals(Collections.singletonMap("user.properties.file", new File(user, "build.properties").getAbsolutePath()),
                Util.loadProperties(props));
        assertEquals(pl, s.getPlatform(true));
        assertEquals(pl, s.getPlatform(false));
        assertEquals(pl, p.getPlatform(true));
        assertEquals(pl, p.getPlatform(false));
    }
    
    public void testUsableModuleListForBrokenPlatform() throws Exception {
        File sd = new File(getWorkDir(), "suite");
        SuiteProjectGenerator.createSuiteProject(sd, NbPlatform.PLATFORM_ID_DEFAULT, false);
        File d = new File(getWorkDir(), "suitecomp");
        NbModuleProjectGenerator.createSuiteComponentModule(d, "x", "X", null, null, sd, false, true);
        TestBase.delete(sd);
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(d));
        ModuleEntry e = p.getModuleList().getEntry("core");
        assertNotNull("#67148: can find core.jar from default platform in " + p, e);
        assertEquals("correct JAR path", new File(new File(new File(install, "platform"), "core"), "core.jar"), e.getJarLocation());
        p.open(); // check for errors
    }
    
    // XXX to test, for suite projects, suite component module projects, and standalone projects:
    // - return default platform if ${netbeans.dest.dir} undefined in any way or not pointing to valid platform [partly tested]
    // - OpenProjectHook fixes, or creates, platform-private.properties to point to current build.properties [in progress; need to test non-default platforms valid in new b.props]
    // - in OPH, platform.properties is fixed to use default if no value for nbplatform.active (and netbeans.dest.dir not independently set!) or points to invalid platform
    // - all problems are notified to user (maybe move ModuleProperties.reportLostPlatform, and change MP.runFromTests)
    
}
