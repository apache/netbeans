/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
