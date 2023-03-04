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
