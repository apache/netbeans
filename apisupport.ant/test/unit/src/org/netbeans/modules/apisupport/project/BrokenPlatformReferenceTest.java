/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
