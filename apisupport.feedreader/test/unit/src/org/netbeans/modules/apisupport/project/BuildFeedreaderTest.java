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
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests Feedreader sample.
 * Invokes various Ant targets over Feedreader sample.
 *
 * @author Tomas Musil
 */
public class BuildFeedreaderTest extends TestBase {
    
    private File feedFolder = null;
    
    static {
        // #65461: do not try to load ModuleInfo instances from ant module
        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
        LayerTestBase.Lkp.setLookup(new Object[0]);
        DialogDisplayerImpl.returnFromNotify(DialogDescriptor.NO_OPTION);
    }
    
    public BuildFeedreaderTest(String testName) {
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
     * Extracts feedreader to workdir, then platform properties are copied and ant task(s) is called. Build status is returned
     */
    public int runAntTargetsOnFeedreader(String[] targets) throws Exception{
        InputStream is = getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/apisupport/feedreader/FeedReaderProject.zip");
        assertNotNull(is);
        feedFolder = new File(getWorkDir(),"feedreader");
        feedFolder.mkdir();
        FileObject fo = FileUtil.toFileObject(feedFolder);
        assertNotNull(fo);
        
        
        try {
            FileUtil.extractJar(fo,is);
        } finally {
            is.close();
        }
        
        File buildProps = new File(getWorkDir(), "build.properties");
        File privateFolder = new File(new File(feedFolder, "nbproject"),"private");
        privateFolder.mkdir();
        
        FileObject platfPrivateProps = FileUtil.copyFile(FileUtil.toFileObject(buildProps), FileUtil.toFileObject(privateFolder), "platform-private");
        assertNotNull(platfPrivateProps);
        SuiteProject feedreaderSuite = (SuiteProject) ProjectManager.getDefault().findProject(fo);
        assertNotNull(feedreaderSuite);
        FileObject buildScript = feedreaderSuite.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
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
     * Invokes build-jnlp target on feedreader
     */
    public void testBuildJNLP() throws Exception {
        int ret = runAntTargetsOnFeedreader(new String[] {"build-jnlp"});
        assertFileExists("dist/feedreader.war");
        assertEquals("build-jnlp ant target should return zero - build successful", 0 , ret);
    }
    
    /**
     * Invokes build-zip target on feedreader
     */
    public void testBuildZip() throws Exception {
        int ret = runAntTargetsOnFeedreader(new String[] {"build-zip"});
        assertFileExists("dist/feedreader.zip");
        assertEquals("build-zipant target should return zero - build successful", 0 , ret);
    }
    
    /**
     * Invokes build target on feedreader
     */
    public void testBuild() throws Exception {
        int ret = runAntTargetsOnFeedreader(new String[] {"build"});
        assertEquals("build ant target should return zero - build successful", 0 , ret);
    }
    
    /**
     * Invokes nbms target on feedreader
     */
    public void testBuildNBMs() throws Exception {
        int ret = runAntTargetsOnFeedreader(new String[] {"nbms"});
        assertFileExists("build/updates/com-sun-syndication-fetcher.nbm");
        assertFileExists("build/updates/com-sun-syndication.nbm");
        assertFileExists("build/updates/org-jdom.nbm");
        assertFileExists("build/updates/org-netbeans-feedreader.nbm");
        assertFileExists("build/updates/updates.xml");
        assertEquals("build ant target should return zero - build successful", 0 , ret);
    }
    
    /**
     * Invokes clean target on feedreader
     */
    public void testClean() throws Exception {
        int ret = runAntTargetsOnFeedreader(new String[] {"clean"});
        assertFalse("Empty build",new File(feedFolder,"build").exists());
        assertFalse("Empty dist",new File(feedFolder,"dist").exists());
        
        assertEquals("clean ant target should return zero - build successful", 0 , ret);
    }

    private void assertFileExists(String relPath) {
        assertTrue("Feed reader folder exists",feedFolder.exists());
        File f = new File (feedFolder,relPath);
        assertTrue("File ${feedreader}/" + relPath,f.exists());
    }
    
}

