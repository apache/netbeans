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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.ejbjarproject.classpath;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.j2ee.ejbjarproject.TestPlatformProvider;
import org.netbeans.modules.j2ee.ejbjarproject.test.TestBase;
import org.netbeans.modules.j2ee.ejbjarproject.test.TestUtil;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Andrei Badea
 */
public class ClassPathProviderImplTest extends TestBase {
    
    private static final String DEFAULT_PLATFORM_ROOT = "DefaultPlatformRoot";
    
    private Project project;
    private FileObject sourceRoot;
    private FileObject testRoot;
    private FileObject sourceClass;
    private FileObject testClass;

    public ClassPathProviderImplTest(String testName) {
        super(testName);
    }
    
    @Override
    public void setUp() throws Exception {
        // setup some platforms -- needed for testing findClassPath(FileObject, ClassPath.BOOT)
        FileObject scratch = TestUtil.makeScratchDir(this);
        FileObject defaultPlatformBootRoot = scratch.createFolder(DEFAULT_PLATFORM_ROOT);
        ClassPath defBCP = ClassPathSupport.createClassPath(new URL[] { defaultPlatformBootRoot.getURL() });

        MockLookup.setLayersAndInstances(new TestPlatformProvider(defBCP, defBCP));
        
        assertTrue("No Java platforms found.", JavaPlatformManager.getDefault().getInstalledPlatforms().length >= 2);
        
        // setup the project
        File f = new File(getDataDir().getAbsolutePath(), "projects/EJBModule1");
        project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        Sources src = project.getLookup().lookup(Sources.class);
        SourceGroup[] groups = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        
        sourceRoot = findSourceRoot(groups, "${src.dir}");
        assertNotNull(sourceRoot);
        testRoot = findSourceRoot(groups, "${test.src.dir}");
        assertNotNull(testRoot);
        
        sourceClass = sourceRoot.getFileObject("pkg/NewClass.java");
        assertNotNull(sourceClass);
        testClass = testRoot.getFileObject("pkg/NewClassTest.java");
        assertNotNull(testClass);
    }
    
    public void testClassPaths() throws Exception {
        ClassPathProvider cpp = ((EjbJarProject)project).getClassPathProvider();
        assertTrue("No ClassPathProvider in project lookup!", cpp != null);
        
        ClassPath cp;
        
        // testing all cp's twice as the second time they come from a cache
        
        // sources
        
        cp = cpp.findClassPath(sourceClass, ClassPath.SOURCE);
        checkSourceSourceClassPath(cp);
        cp = cpp.findClassPath(sourceClass, ClassPath.SOURCE);
        checkSourceSourceClassPath(cp);
        
        cp = cpp.findClassPath(sourceClass, ClassPath.COMPILE);
        checkCompileClassPath(cp);
        cp = cpp.findClassPath(sourceClass, ClassPath.COMPILE);
        checkCompileClassPath(cp);
        
        cp = cpp.findClassPath(sourceClass, ClassPath.EXECUTE);
        checkSourceExecuteClassPath(cp);
        cp = cpp.findClassPath(sourceClass, ClassPath.EXECUTE);
        checkSourceExecuteClassPath(cp);
        
        cp = cpp.findClassPath(sourceClass, ClassPath.BOOT);
        checkBootClassPath(cp);
        cp = cpp.findClassPath(sourceClass, ClassPath.BOOT);
        checkBootClassPath(cp);
        
        // test sources
        
        cp = cpp.findClassPath(testClass, ClassPath.SOURCE);
        checkTestSourceClassPath(cp);
        cp = cpp.findClassPath(testClass, ClassPath.SOURCE);
        checkTestSourceClassPath(cp);
        
        cp = cpp.findClassPath(testClass, ClassPath.COMPILE);
        checkCompileClassPath(cp);
        cp = cpp.findClassPath(testClass, ClassPath.COMPILE);
        checkCompileClassPath(cp);
        
        cp = cpp.findClassPath(testClass, ClassPath.EXECUTE);
        checkTestExecuteClassPath(cp);
        cp = cpp.findClassPath(testClass, ClassPath.EXECUTE);
        checkTestExecuteClassPath(cp);
        
        cp = cpp.findClassPath(testClass, ClassPath.BOOT);
        checkBootClassPath(cp);
        cp = cpp.findClassPath(testClass, ClassPath.BOOT);
        checkBootClassPath(cp);
    }
    
    private void checkSourceSourceClassPath(ClassPath cp) {
        FileObject[] roots = cp.getRoots();
        assertEquals(1, roots.length);
        assertTrue(cp.getRoots()[0].equals(sourceRoot));
    }
    
    private void checkSourceExecuteClassPath(ClassPath cp) {
        // this jar is on debug.classpath
        assertTrue(classPathEntriesContainJar(cp.entries(), "jar1.jar"));
    }
    
    private void checkTestSourceClassPath(ClassPath cp) {
        FileObject[] roots = cp.getRoots();
        assertEquals(1, roots.length);
        assertTrue(cp.getRoots()[0].equals(testRoot));
    }
    
    private void checkTestExecuteClassPath(ClassPath cp) {
        // this jar is on run.test.classpath
        assertTrue(classPathEntriesContainJar(cp.entries(), "jar2.jar"));
    }
    
    private void checkCompileClassPath(ClassPath cp) {
        // this jar is on javac.classpath
        assertTrue(classPathEntriesContainJar(cp.entries(), "jar0.jar"));
        // XXX should also test J2EE classpath
    }

    private void checkBootClassPath(ClassPath cp) {
        assertTrue(classPathEntriesContainFolder(cp.entries(), DEFAULT_PLATFORM_ROOT));
    }
    
    private static boolean classPathEntriesContainJar(List entries, String name) {
        for (Iterator i = entries.iterator(); i.hasNext();) {
            ClassPath.Entry e = (ClassPath.Entry)i.next();
            URL jar = FileUtil.getArchiveFile(e.getURL());
            if (jar != null) {
                if (name.equals(new File(URI.create(jar.toExternalForm())).getName())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static boolean classPathEntriesContainFolder(List entries, String name) {
        for (Iterator i = entries.iterator(); i.hasNext();) {
            ClassPath.Entry e = (ClassPath.Entry)i.next();
            URL folder = e.getURL();
            if ("file".equals(folder.getProtocol())) {
                if (name.equals(new File(URI.create(folder.toExternalForm())).getName())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static FileObject findSourceRoot(SourceGroup[] groups, String name) {
        for (int i = 0; i < groups.length; i++) {
            if (name.equals(groups[i].getName())) {
                return groups[i].getRootFolder();
            }
        }
        return null;
    }
}
