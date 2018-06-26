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
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.j2ee.ejbjarproject.TestPlatformProvider;
import org.netbeans.modules.j2ee.ejbjarproject.test.TestBase;
import org.netbeans.modules.j2ee.ejbjarproject.test.TestUtil;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 * Test's adding generate source in addons directory to source classpath.
 */
public class SourcePathImplAddonsTest extends TestBase{
    private static final String SRC_ROOT_1 = "generated-sources/srcroot1";  // No I18N
    private static final String SRC_ROOT_2 = "generated-sources/srcroot2";  // No I18N
    private static final String SRC_ROOT_3 = "generated-sources/srcroot3";  // No I18N
    private static final String DEFAULT_PLATFORM_ROOT = "DefaultPlatformRoot"; // No I18N
    private static final String EJB_PROJ1 = "EJBModule1" ;
    
    private FileObject scratchFO;
    private FileObject projdirFO;
    private ProjectManager pm;
    private EjbJarProject proj;
    private AntProjectHelper helper;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // setup some platforms -- needed for testing findClassPath(FileObject, ClassPath.BOOT)
        scratchFO = TestUtil.makeScratchDir(this);
        FileObject defaultPlatformBootRoot = scratchFO.createFolder(DEFAULT_PLATFORM_ROOT);
        ClassPath defBCP = ClassPathSupport.createClassPath(new URL[] { defaultPlatformBootRoot.getURL() });

        MockLookup.setLayersAndInstances(new TestPlatformProvider(defBCP, defBCP));
        
        assertTrue("No Java platforms found.", JavaPlatformManager.getDefault().getInstalledPlatforms().length >= 2);
        
        // setup the project
        File f = new File(getDataDir().getAbsolutePath(), "projects/" + EJB_PROJ1);
        
        projdirFO = scratchFO.getFileObject(EJB_PROJ1);
        
        // Delete if previously not deleted.
        if (projdirFO != null){
            projdirFO.delete();
        }
        projdirFO = scratchFO.createFolder(EJB_PROJ1);
        
        File origCopy = new File(getDataDir().getAbsolutePath(),
                "projects/" + EJB_PROJ1);
        TestUtil.copyDir(origCopy, FileUtil.toFile(projdirFO));
        
        pm = ProjectManager.getDefault();
        proj = pm.findProject(projdirFO).getLookup().lookup(EjbJarProject.class);
        helper = proj.getAntProjectHelper();
    }
    
    @Override
    protected void tearDown() throws Exception {
        // Delete
        if (projdirFO != null){
            try {
                projdirFO.delete();
            } catch (Exception ex){
                //Log
            }
        }
        
        scratchFO = null;
        projdirFO = null;
        pm = null;
        super.tearDown();
    }
    
    public SourcePathImplAddonsTest(String testName) {
        super(testName);
    }
    
    private void createAddonsSrcRoot(FileObject buildFo, String srcRoot) throws IOException {
        StringTokenizer stk = null;
        stk = new StringTokenizer(srcRoot, "/"); // No I18N
        String dir = null;
        FileObject currFo = buildFo;
        FileObject child = null;
        while(stk.hasMoreTokens()){
            dir = stk.nextToken();
            child = currFo.getFileObject(dir);
            if (child == null){
                child = currFo.createFolder(dir);
            }
            currFo = child;
        }
    }
    
    private void assertContainsURL(List<ClassPath.Entry> list, URL url, boolean present){
        ClassPath.Entry cpe = null;
        Iterator<ClassPath.Entry> itr = itr = list.iterator();
        
        if (present){
            boolean found = false;
            while (itr.hasNext()){
                cpe = itr.next();
                if (url.equals(cpe.getURL())){
                    found = true;
                }
            }
            assertTrue(found);
        } else {
            while (itr.hasNext()){
                cpe = itr.next();
                assertFalse(url.equals(cpe.getURL()));
            }
        }
    }
    
    /**
     * Test's newly added source root appears in source classpath.
     **/
    public void testAddonsCreateSourceRoot() throws Exception {
        ClassPathProviderImpl cpProvider = proj.getClassPathProvider();
        ClassPath[] cps = cpProvider.getProjectClassPaths(ClassPath.SOURCE);
        ClassPath cp = cps[0];
        List<ClassPath.Entry> entries = cp.entries();
        assertNotNull("Entries can not be null", entries);
        String buildDir = helper.getStandardPropertyEvaluator().getProperty(EjbJarProjectProperties.BUILD_DIR);
        assertNotNull("There is no build.dir property", buildDir);
        File srcRoot1File = new File(helper.resolveFile(buildDir), SRC_ROOT_1);
        URL url = srcRoot1File.toURI().toURL();
        if (!srcRoot1File.exists()) {
            url = new URL(url.toExternalForm() + "/");
        }
        
        assertContainsURL(entries, url, false);
        FileObject buildFo = helper.getProjectDirectory().getFileObject(buildDir);
        if (buildFo == null){
            buildFo = helper.getProjectDirectory().createFolder(buildDir);
        }
        
        createAddonsSrcRoot(buildFo, SRC_ROOT_1);
        Thread.sleep(1 * 1000); // Allow for event to propagate for a second.
        
        assertContainsURL(cp.entries(), url, true);
    }
    
    /**
     * Test's deletion of source root also removes that root from the source classpath.
     * Since Deletion is recognized only after new folder creation event.
     **/
    public void testAddonsRemoveSourceRoot() throws Exception {
        ClassPathProviderImpl cpProvider = proj.getClassPathProvider();
        ClassPath[] cps = cpProvider.getProjectClassPaths(ClassPath.SOURCE);
        ClassPath cp = cps[0];
        String buildDir = helper.getStandardPropertyEvaluator().getProperty(EjbJarProjectProperties.BUILD_DIR);
        File srcRootFile1 = new File(helper.resolveFile(buildDir), SRC_ROOT_1);
        URL url1 = srcRootFile1.toURI().toURL();
        if (!srcRootFile1.exists()) {
            url1 = new URL(url1.toExternalForm() + "/");
        }
        
        // Simulate folder creation thru NB task.
        FileObject buildDirFO = helper.resolveFileObject(buildDir);
        if (buildDirFO == null){
            buildDirFO = helper.getProjectDirectory().createFolder(buildDir);
        }
        
        createAddonsSrcRoot(buildDirFO, SRC_ROOT_1);
        Thread.sleep(1 * 1000); // Allow for event to propagate for a second.
        assertContainsURL(cp.entries(), url1, true);
        
        FileObject src1Fo = buildDirFO.getFileObject(SRC_ROOT_1);
        TestUtil.deleteRec(FileUtil.toFile(src1Fo));
        
        File addonModuleDir2 = new File(helper.resolveFile(buildDir), SRC_ROOT_2);
        URL url2 = addonModuleDir2.toURI().toURL();
        if (!addonModuleDir2.exists()) {
            url2 = new URL(url2.toExternalForm() + "/");
        }
        createAddonsSrcRoot(buildDirFO, SRC_ROOT_2);
        Thread.sleep(1 * 1000); // Allow for event to propagate for a second.
        assertContainsURL(cp.entries(), url1, false);
        assertContainsURL(cp.entries(), url2, true);
    }
    /**
     * Test's newly added multiple source root appears in source classpath.
     **/
    public void testAddonsMultipleSourceRoot() throws Exception {
        ClassPathProviderImpl cpProvider = proj.getClassPathProvider();
        ClassPath[] cps = cpProvider.getProjectClassPaths(ClassPath.SOURCE);
        ClassPath cp = cps[0];
        List<ClassPath.Entry> entries = cp.entries();
        assertNotNull("Entries can not be null", entries);
        
        String buildDir = helper.getStandardPropertyEvaluator().getProperty(EjbJarProjectProperties.BUILD_DIR);
        assertNotNull("There is no build.dir property", buildDir);
        File addonModuleDir1 = new File(helper.resolveFile(buildDir), SRC_ROOT_1);
        File addonModuleDir2 = new File(helper.resolveFile(buildDir), SRC_ROOT_2);
        File addonModuleDir3 = new File(helper.resolveFile(buildDir), SRC_ROOT_3);
        
        URL url1 = addonModuleDir1.toURI().toURL();
        URL url2 = addonModuleDir2.toURI().toURL();
        URL url3 = addonModuleDir3.toURI().toURL();
        
        if (!addonModuleDir1.exists()) {
            url1 = new URL(url1.toExternalForm() + "/");
        }
        
        if (!addonModuleDir2.exists()) {
            url2 = new URL(url2.toExternalForm() + "/");
        }
        
        if (!addonModuleDir3.exists()) {
            url3 = new URL(url3.toExternalForm() + "/");
        }
        
        assertContainsURL(entries, url1, false);
        assertContainsURL(entries, url2, false);
        assertContainsURL(entries, url3, false);
        
        FileUtil.createFolder(addonModuleDir1);
        FileUtil.createFolder(addonModuleDir2);
        FileUtil.refreshFor(helper.resolveFile(buildDir));
        
        // Simulate folder creation thru NB task.
        FileObject buildDirFO = helper.resolveFileObject(buildDir);
        if (buildDirFO == null){
            buildDirFO = helper.getProjectDirectory().createFolder(buildDir);
        }

        createAddonsSrcRoot(buildDirFO, SRC_ROOT_3);
        
        Thread.sleep(1 * 1000); // Allow for event to propagate for a second.
        
        assertContainsURL(cp.entries(), url1, true);
        assertContainsURL(cp.entries(), url2, true);
        assertContainsURL(cp.entries(), url3, true);
    }
}
