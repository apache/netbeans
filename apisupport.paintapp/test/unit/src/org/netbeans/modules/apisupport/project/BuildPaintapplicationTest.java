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

import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests Paintapp sample.
 * Invokes various Ant targets over Paintapp sample.
 *
 * @author Tomas Musil
 */
public class BuildPaintapplicationTest extends TestBase {
    
    private File paintFolder = null;
    
    static {
        // #65461: do not try to load ModuleInfo instances from ant module
        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
        LayerTestBase.Lkp.setLookup(new Object[0]);
        DialogDisplayerImpl.returnFromNotify(DialogDescriptor.NO_OPTION);
    }
    
    public BuildPaintapplicationTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        noDataDir = true;
        super.setUp();
        InstalledFileLocatorImpl.registerDestDir(destDirF);
        TestAntLogger.getDefault().setEnabled(true);
    }
    
    protected void tearDown() throws Exception {
        TestAntLogger.getDefault().setEnabled(false);
    }
    
    /**
     * Extracts paintapp to workdir, then platform properties are copied and ant task(s) is called. Build status is returned
     */
    public int runAntTargetsOnPaintapp(String[] targets) throws Exception{
        InputStream is = getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/apisupport/paintapp/PaintAppProject.zip");
        assertNotNull(is);
        paintFolder = new File(getWorkDir(),"paintapp");
        paintFolder.mkdir();
        FileObject fo = FileUtil.toFileObject(paintFolder);
        assertNotNull(fo);
        
        
        try {
            FileUtil.extractJar(fo,is);
        } finally {
            is.close();
        }
        
        File buildProps = new File(getWorkDir(), "build.properties");
        File privateFolder = new File(new File(paintFolder, "nbproject"),"private");
        privateFolder.mkdir();
        
        FileObject platfPrivateProps = FileUtil.copyFile(FileUtil.toFileObject(buildProps), FileUtil.toFileObject(privateFolder), "platform-private");
        assertNotNull(platfPrivateProps);
        SuiteProject PaintappSuite = (SuiteProject) ProjectManager.getDefault().findProject(fo);
        assertNotNull(PaintappSuite);
        FileObject buildScript = PaintappSuite.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        assertNotNull(buildScript);
        assertTrue(buildScript.isValid());
        
        Properties p = new Properties();
        p.setProperty("jnlp.sign.jars", "false");

        System.out.println("------------- BUILD OUTPUT --------------");
        ExecutorTask et = ActionUtils.runTarget(buildScript, targets, p);
        et.waitFinished();
        System.out.println("-----------------------------------------");
        
        return et.result();      
    }
    
    /**
     * Invokes build-jnlp target on paintapp
     */
    public void testBuildJNLP() throws Exception {
        int ret = runAntTargetsOnPaintapp(new String[] {"build-jnlp"});
        assertEquals("build-jnlp ant target should return zero - build successful", 0 , ret);
        File dist = new File(paintFolder,"dist");
        File warFile = new File(dist,"paintit.war");
        assertTrue("paintapp.war file should be in dist folder", warFile.exists());
   
    }
    
    /**
     * Invokes build-zip target on paintapp
     */
    public void testBuildZip() throws Exception {
        int ret = runAntTargetsOnPaintapp(new String[] {"build-zip"});
        assertEquals("build-zipant target should return zero - build successful", 0 , ret);
        File dist = new File(paintFolder,"dist");
        File zipFile = new File(dist,"paintit.zip");
        assertTrue("paintit.zip file should be in dist folder", zipFile.exists());
    }
    
    /**
     * Invokes build target on paintapp
     */
    public void testBuild() throws Exception {
        int ret = runAntTargetsOnPaintapp(new String[] {"build"});
        assertEquals("build ant target should return zero - build successful", 0 , ret);
        File buildFolder = new File(paintFolder,"build");
        assertTrue("build folder should exist", buildFolder.exists() && buildFolder.isDirectory());
        String[] children = buildFolder.list();
        assertTrue("build folder is not empty", children.length>0);
        
    }
    
    /**
     * Invokes nbms target on paintapp
     */
    public void testBuildNBMs() throws Exception {
        int ret = runAntTargetsOnPaintapp(new String[] {"nbms"});
        assertEquals("build ant target should return zero - build successful", 0 , ret);
        File buildFolder = new File(paintFolder,"build");
        File updatesFolder = new File(buildFolder,"updates");
        assertTrue("build/update folder exists", updatesFolder.exists() && updatesFolder.isDirectory());
        File paintNbm = new File(updatesFolder, "org-netbeans-paint.nbm");
        File colorchooserNbm = new File(updatesFolder, "org-netbeans-swing-colorchooser.nbm");
        assertTrue("Both nbms(paint+colorchooser) are in build/updates folder", paintNbm.exists() && colorchooserNbm.exists());
    }
    
    /**
     * Invokes clean target on paintapp
     */
    public void testClean() throws Exception {
        int ret = runAntTargetsOnPaintapp(new String[] {"clean"});
        assertEquals("clean ant target should return zero - build successful", 0 , ret);
        File buildFolder = new File(paintFolder,"build");
        File distFolder = new File(paintFolder,"dist");
        assertTrue("build and dist folders are deleted", !distFolder.exists() && !buildFolder.exists());
    }
}

