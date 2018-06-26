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

package org.netbeans.modules.web.project.classpath;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
//import org.netbeans.api.project.TestUtil;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.project.TestPlatformProvider;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.test.TestUtil;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Andrei Badea
 */
public class ClassPathProviderImplTest extends NbTestCase {
    
    private Project project;
    private Project project59055;
    private AntProjectHelper helper;
    private AntProjectHelper helper59055;
    private FileObject sourceRoot;
    private FileObject sourceRoot59055_1;
    private FileObject sourceRoot59055_2;
    private FileObject testRoot;
    private FileObject testRoot59055_1;
    private FileObject testRoot59055_2;
    private FileObject webRoot;
    private FileObject webRoot59055;
    private FileObject bootPlatformRoot;
    private FileObject sourceClass;
    private FileObject sourceClass59055_1;
    private FileObject sourceClass59055_2;
    private FileObject testClass;
    private FileObject testClass59055_1;
    private FileObject testClass59055_2;
    private FileObject jspPage;
    private FileObject jspPage59055;

    public ClassPathProviderImplTest(String testName) {
        super(testName);
    }
    
    public void setUp() throws Exception {
        // setup some platforms -- needed for testing findClassPath(FileObject, ClassPath.BOOT)
        FileObject scratch = TestUtil.makeScratchDir(this);
        bootPlatformRoot = scratch.createFolder("DefaultPlatformRoot");
        ClassPath defBCP = ClassPathSupport.createClassPath(new URL[] { bootPlatformRoot.getURL() });

        MockLookup.setLayersAndInstances(new TestPlatformProvider(defBCP, defBCP));
        
        assertTrue("No Java platforms found.", JavaPlatformManager.getDefault().getInstalledPlatforms().length >= 2);
        
        // setup the project
        File f = new File(getDataDir().getAbsolutePath(), "projects/WebApplication1");
        project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        Sources src = (Sources)project.getLookup().lookup(Sources.class);
        SourceGroup[] groups = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        
        sourceRoot = findSourceRoot(groups, "${src.dir}");
        assertNotNull(sourceRoot);
        testRoot = findSourceRoot(groups, "${test.src.dir}");
        assertNotNull(testRoot);
        
        sourceClass = sourceRoot.getFileObject("pkg/NewClass.java");
        assertNotNull(sourceClass);
        testClass = testRoot.getFileObject("pkg/NewClassTest.java");
        assertNotNull(testClass);
        
        // XXX should not cast to WebProject
        helper = ((WebProject)project).getAntProjectHelper();
        String web = helper.getStandardPropertyEvaluator().getProperty(WebProjectProperties.WEB_DOCBASE_DIR);
        webRoot = helper.resolveFileObject(web);
        jspPage = webRoot.getFileObject("index.jsp");
        
        //===========================================================
        // setup the project for issue #59055
        //===========================================================
        File f59055 = new File(getDataDir().getAbsolutePath(), "projects/WebApplication59055");
        project59055 = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f59055));
        Sources src59055 = (Sources) project59055.getLookup().lookup(Sources.class);
        SourceGroup[] groups59055 = src59055.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        
        sourceRoot59055_1 = findSourceRoot(groups59055, "${src.dir}");
        assertNotNull(sourceRoot59055_1);
        sourceRoot59055_2 = findSourceRoot(groups59055, "${src.src2.dir}");
        assertNotNull(sourceRoot59055_2);
        testRoot59055_1 = findSourceRoot(groups59055, "${test.src.dir}");
        assertNotNull(testRoot59055_1);
        testRoot59055_2 = findSourceRoot(groups59055, "${test.test2.dir}");
        assertNotNull(testRoot59055_2);
        
        sourceClass59055_1 = sourceRoot59055_1.getFileObject("org/wa59055/Class1.java");
        assertNotNull(sourceClass59055_1);
        sourceClass59055_2 = sourceRoot59055_2.getFileObject("org/wa59055/extra/Class2.java");
        assertNotNull(sourceClass59055_2);
        testClass59055_1 = testRoot59055_1.getFileObject("org/wa59055/Class1Test.java");
        assertNotNull(testClass59055_1);
        testClass59055_2 = testRoot59055_2.getFileObject("org/wa59055/extra/Class2Test.java");
        assertNotNull(testClass59055_2);
        
        // XXX should not cast to WebProject
        helper59055 = ((WebProject) project59055).getAntProjectHelper();
        String web59055 = helper59055.getStandardPropertyEvaluator().getProperty(WebProjectProperties.WEB_DOCBASE_DIR);
        webRoot59055 = helper59055.resolveFileObject(web59055);
        jspPage59055 = webRoot59055.getFileObject("index.jsp");
    }
    
    public void testClassPaths() throws Exception {
        System.out.println("ClassPathProviderImplTest - WebApplication1");
        ClassPathProvider cpp = ((WebProject)project).getClassPathProvider();
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
        
        // JSP pages
        
        cp = cpp.findClassPath(jspPage, ClassPath.SOURCE);
        checkJSPSourceClassPath(cp);
        cp = cpp.findClassPath(jspPage, ClassPath.SOURCE);
        checkJSPSourceClassPath(cp);
        
        //===========================================================
        //test special source structure described in issue #59055
        //===========================================================
        //TODO: test also ClassPath.COMPILE, ClassPath.EXECUTE, ClassPath.BOOT for WebApplication59055
        System.out.println("ClassPathProviderImplTest - WebApplication59055");
        cpp = ((WebProject)project59055).getClassPathProvider();
        assertTrue("No ClassPathProvider in project lookup!", cpp != null);
        
        //ordinary sources
        cp = cpp.findClassPath(sourceClass59055_1, ClassPath.SOURCE);
        checkSourceSourceClassPath59055(cp);
        cp = cpp.findClassPath(sourceClass59055_1, ClassPath.SOURCE);
        checkSourceSourceClassPath59055(cp);
        //sources under web pages directory
        cp = cpp.findClassPath(sourceClass59055_2, ClassPath.SOURCE);
        checkSourceSourceClassPath59055(cp);
        cp = cpp.findClassPath(sourceClass59055_2, ClassPath.SOURCE);
        checkSourceSourceClassPath59055(cp);
        
        //ordinary test sources
        cp = cpp.findClassPath(testClass59055_1, ClassPath.SOURCE);
        checkTestSourceClassPath59055(cp);
        cp = cpp.findClassPath(testClass59055_1, ClassPath.SOURCE);
        checkTestSourceClassPath59055(cp);
        //test sources under web pages directory
        cp = cpp.findClassPath(testClass59055_2, ClassPath.SOURCE);
        checkTestSourceClassPath59055(cp);
        cp = cpp.findClassPath(testClass59055_2, ClassPath.SOURCE);
        checkTestSourceClassPath59055(cp);
        
        cp = cpp.findClassPath(jspPage59055, ClassPath.SOURCE);
        checkJSPSourceClassPath59055(cp);
        cp = cpp.findClassPath(jspPage59055, ClassPath.SOURCE);
        checkJSPSourceClassPath59055(cp);
    }
    
    private void checkSourceSourceClassPath(ClassPath cp) {
        FileObject[] roots = cp.getRoots();
        assertEquals(1, roots.length);
        assertTrue(cp.getRoots()[0].equals(sourceRoot));
    }
    
    private void checkSourceSourceClassPath59055(ClassPath cp) {
        FileObject[] roots = cp.getRoots();
        assertEquals(2, roots.length);
        assertTrue(cp.getRoots()[0].equals(sourceRoot59055_1));
        assertTrue(cp.getRoots()[1].equals(sourceRoot59055_2));
    }
    
    private void checkSourceExecuteClassPath(ClassPath cp) throws Exception {
        // this jar is on debug.classpath
        assertTrue(classPathContainsURL(cp, resolveURL("libs/jar1.jar")));
    }
    
    private void checkTestSourceClassPath(ClassPath cp) {
        FileObject[] roots = cp.getRoots();
        assertEquals(1, roots.length);
        assertTrue(cp.getRoots()[0].equals(testRoot));
    }
    
    private void checkTestSourceClassPath59055(ClassPath cp) {
        FileObject[] roots = cp.getRoots();
        assertEquals(2, roots.length);
        assertTrue(cp.getRoots()[0].equals(testRoot59055_1));
        assertTrue(cp.getRoots()[1].equals(testRoot59055_2));
    }
    
    private void checkTestExecuteClassPath(ClassPath cp) throws Exception {
        // this jar is on run.test.classpath
        assertTrue(classPathContainsURL(cp, resolveURL("libs/jar2.jar")));
    }
    
    private void checkJSPSourceClassPath(ClassPath cp) {
        FileObject[] roots = cp.getRoots();
        assertEquals(2, roots.length);
        assertTrue(cp.getRoots()[0].equals(webRoot));
        assertTrue(cp.getRoots()[1].equals(sourceRoot));
    }

    private void checkJSPSourceClassPath59055(ClassPath cp) {
        FileObject[] roots = cp.getRoots();
        assertEquals(3, roots.length);
        assertTrue(cp.getRoots()[0].equals(webRoot59055));
        assertTrue(cp.getRoots()[1].equals(sourceRoot59055_1));
        assertTrue(cp.getRoots()[2].equals(sourceRoot59055_2));
    }
    
    private void checkCompileClassPath(ClassPath cp) throws Exception {
        // this jar is on javac.classpath
        assertTrue(classPathContainsURL(cp, resolveURL("libs/jar0.jar")));
        // XXX should also test J2EE classpath
    }

    private void checkBootClassPath(ClassPath cp) throws Exception {
        assertTrue(classPathContainsURL(cp, bootPlatformRoot.getURL()));
    }
    
    private URL resolveURL(String relative) throws Exception {
        return helper.resolveFileObject(relative).getURL();
    }
    
    private static final boolean classPathContainsURL(ClassPath cp, URL url) {
        if (FileUtil.isArchiveFile(url)) {
            url = FileUtil.getArchiveRoot(url);
        }
        for (Iterator i = cp.entries().iterator(); i.hasNext();) {
            ClassPath.Entry e = (ClassPath.Entry)i.next();
            if (e.getURL().equals(url)) {
                return true;
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
