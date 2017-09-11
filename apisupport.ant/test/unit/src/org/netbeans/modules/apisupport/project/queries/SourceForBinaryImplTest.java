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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.Manifest;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.ApisupportAntUtils;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectGenerator;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Test functionality of SourceForBinaryImpl.
 * @author Jesse Glick
 */
public class SourceForBinaryImplTest extends TestBase {
    
    public SourceForBinaryImplTest(String name) {
        super(name);
    }
    
    public void testFindSourceRootForCompiledClasses() throws Exception {
        doTestFindSourceRootForCompiledClasses("java.project/src", "java.project/build/classes");
        doTestFindSourceRootForCompiledClasses("java.project/test/unit/src", "java.project/build/test/unit/classes");
        doTestFindSourceRootForCompiledClasses("ant.freeform/src", "ant.freeform/build/classes");
        doTestFindSourceRootForCompiledClasses("ant.freeform/test/unit/src", "ant.freeform/build/test/unit/classes");
    }

    /* XXX fails because o.n.m.a.p.queries.UpdateTrackingFileOwnerQuery does not scan extra-compilation-units
    public void testExtraCompilationUnits() throws Exception {
        doTestFindSourceRootForCompiledClasses("o.apache.tools.ant.module/src-bridge", "o.apache.tools.ant.module/build/bridge-classes");
        // Have to load at least one module to get the scan going.
        ClassPath.getClassPath(FileUtil.toFileObject(file("beans/src")), ClassPath.COMPILE);
        check("o.apache.tools.ant.module/src-bridge", TestBase.CLUSTER_JAVA + "/ant/nblib/bridge.jar");
    }
     */
    
    public void testFindSourceRootForModuleJar() throws Exception {
        check("java.project/src", TestBase.CLUSTER_JAVA + "/modules/org-netbeans-modules-java-project.jar");
        check("openide.loaders/src", TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar");
        check("o.n.bootstrap/src", TestBase.CLUSTER_PLATFORM + "/lib/boot.jar");
        check("diff/src", TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-diff.jar");
        check("editor.lib/src", TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-editor-lib.jar");
        check("nbjunit/src", "harness/modules/org-netbeans-modules-nbjunit.jar");
        check("apisupport.project/test/unit/src",file("nbbuild/build/testdist/unit/" + TestBase.CLUSTER_APISUPPORT + "/org-netbeans-modules-apisupport-project/tests.jar"));
    }
    
    public void testExternalModules() throws Exception {
        ClassPath.getClassPath(resolveEEP("/suite1/action-project/src"), ClassPath.COMPILE);
        check(resolveEEPPath("/suite1/action-project/src"), resolveEEPFile("/suite1/build/cluster/modules/org-netbeans-examples-modules-action.jar"));
        ClassPath.getClassPath(resolveEEP("/suite3/dummy-project/src"), ClassPath.COMPILE);
        check(resolveEEPPath("/suite3/dummy-project/src"),
              resolveEEPFile("/suite3/dummy-project/build/cluster/modules/org-netbeans-examples-modules-dummy.jar"));
        // test dependencies
        ClassPath.getClassPath(resolveEEP("/suite4/module1/test/unit/src"),ClassPath.COMPILE);
        check(resolveEEPPath("/suite4/module1/test/unit/src"),resolveEEPFile("/suite4/build/testdist/unit/cluster/module1/tests.jar"));
    }
    
    public void testCompletionWorks_69735() throws Exception {
        SuiteProject suite = generateSuite("suite");
        NbModuleProject project = TestBase.generateSuiteComponent(suite, "module");
        File library = new File(getWorkDir(), "test-library-0.1_01.jar");
        createJar(library, Collections.<String,String>emptyMap(), new Manifest());
        FileObject libraryFO = FileUtil.toFileObject(library);
        FileObject yyJar = FileUtil.copyFile(libraryFO, FileUtil.toFileObject(getWorkDir()), "yy");
        
        // library wrapper
        File suiteDir = suite.getProjectDirectoryFile();
        File wrapperDirF = new File(new File(getWorkDir(), "suite"), "wrapper");
        NbModuleProjectGenerator.createSuiteLibraryModule(
                wrapperDirF,
                "yy", // 69735 - the same name as jar
                "Testing Wrapper (yy)", // display name
                "org/example/wrapper/resources/Bundle.properties",
                suiteDir, // suite directory
                null,
                new File[] { FileUtil.toFile(yyJar)} );
        
        ApisupportAntUtils.addDependency(project, "yy", null, null, true, null);
        ProjectManager.getDefault().saveProject(project);
        
        URL wrappedJar = FileUtil.urlForArchiveOrDir(new File(wrapperDirF, "release/modules/ext/yy.jar"));
        assertEquals("no sources for wrapper", 0, SourceForBinaryQuery.findSourceRoots(wrappedJar).getRoots().length);
    }
    
    private void check(String srcS, File jarF) throws Exception {
        File srcF = PropertyUtils.resolveFile(nbRootFile(), srcS);
        FileObject src = FileUtil.toFileObject(srcF);
        assertNotNull("have " + srcF, src);
        URL u = FileUtil.getArchiveRoot(Utilities.toURI(jarF).toURL());
        assertEquals("right results for " + u,
            Collections.singletonList(src),
            trimGenerated(Arrays.asList(SourceForBinaryQuery.findSourceRoots(u).getRoots())));
    }
    
    private void check(String srcS, String jarS) throws Exception {
        ClassPath.getClassPath(FileUtil.toFileObject(file(srcS)), ClassPath.COMPILE);
        check(srcS, file("nbbuild/netbeans/" + jarS));
    }
    
    private void doTestFindSourceRootForCompiledClasses(String srcPath, String classesPath) throws Exception {
        File classesF = file(classesPath);
        File srcF = file(srcPath);
        FileObject src = FileUtil.toFileObject(srcF);
        assertNotNull("have " + srcF, src);
        URL u = FileUtil.urlForArchiveOrDir(classesF);
        assertEquals("right source root for " + u,
            Collections.singletonList(src),
            trimGenerated(Arrays.asList(SourceForBinaryQuery.findSourceRoots(u).getRoots())));
    }

    private List<FileObject> trimGenerated(List<FileObject> dirs) {
        List<FileObject> result = new ArrayList<FileObject>();
        for (FileObject dir : dirs) {
            if (!dir.getName().endsWith("-generated")) {
                result.add(dir);
            }
        }
        return result;
    }
    
}
