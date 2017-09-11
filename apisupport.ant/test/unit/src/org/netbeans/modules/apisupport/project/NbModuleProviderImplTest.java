/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.apisupport.project;

import java.io.File;
import java.util.Collections;
import java.util.logging.Level;
import static org.junit.Assert.*;
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
