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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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
package org.netbeans.modules.apisupport.osgidemo;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.DialogDisplayerImpl;
import org.netbeans.modules.apisupport.project.InstalledFileLocatorImpl;
import org.netbeans.modules.apisupport.project.TestAntLogger;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;

/**
 * Invokes various Ant targets over osgidemo sample.
 *
 * @author Tomas Musil
 */
public abstract class BuildSampleApplicationBase extends TestBase {

    private File sampleFolder = null;

    static {
        // #65461: do not try to load ModuleInfo instances from ant module
        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
        LayerTestBase.Lkp.setLookup(new Object[0]);
        DialogDisplayerImpl.returnFromNotify(DialogDescriptor.NO_OPTION);
        FileUtil.setMIMEType("properties", "text/x-properties");
    }

    public BuildSampleApplicationBase(String testName) {
        super(testName);
    }

    protected abstract Map<String,Object> params();

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        noDataDir = true;
        super.setUp();
        InstalledFileLocatorImpl.registerDestDir(destDirF);
        TestAntLogger.getDefault().setEnabled(true);
    }

    @Override
    protected void tearDown() throws Exception {
        TestAntLogger.getDefault().setEnabled(false);
    }

    public int runAntTargetsOnSample(String[] targets) throws Exception {
        String p = System.getProperty("sample.project");
        FileObject fo;
        if (p == null) {
            InputStream is = getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/apisupport/osgidemo/EquinoxDemo.zip");
            assertNotNull(is);
            sampleFolder = new File(getWorkDir(), "sample");
            sampleFolder.mkdir();
            fo = FileUtil.toFileObject(sampleFolder);
            assertNotNull(fo);
            SampleAppWizardIterator.unZipFile(null, is, params(), fo);
            File buildProps = new File(getWorkDir(), "build.properties");
            File privateFolder = new File(new File(sampleFolder, "nbproject"), "private");
            privateFolder.mkdir();
            FileObject platfPrivateProps = FileUtil.copyFile(FileUtil.toFileObject(buildProps), FileUtil.toFileObject(privateFolder), "platform-private");
            assertNotNull(platfPrivateProps);
            { // In the target platform, libs.junit4 may be in the extra cluster rather than platform:
                FileObject platfProps = fo.getFileObject("nbproject/platform.properties");
                EditableProperties props = new EditableProperties(false);
                is = platfProps.getInputStream();
                props.load(is);
                is.close();
                props.setProperty("cluster.path", (props.getProperty("cluster.path") + ":${nbplatform.active.dir}/extra").split("(?<=:)"));
                OutputStream os = platfProps.getOutputStream();
                props.store(os);
                os.close();
            }
            System.setProperty("sample.project", sampleFolder.getPath());
        } else {
            sampleFolder = new File(p);
            fo = FileUtil.toFileObject(sampleFolder);
            assertNotNull(fo);
        }


        Project sampleSuite = ProjectManager.getDefault().findProject(fo);
        assertNotNull(sampleSuite);
        FileObject buildScript = sampleSuite.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        assertNotNull(buildScript);
        assertTrue(buildScript.isValid());
        Properties props = new Properties();

        ExecutorTask et = ActionUtils.runTarget(buildScript, targets, props);
        et.waitFinished();
        return et.result();
    }

    public void testAntInitAndClean() throws Exception {
        int ret = runAntTargetsOnSample(new String[]{"clean"});
        assertEquals("build successful", 0, ret);

        FileObject fo = FileUtil.toFileObject(sampleFolder);
        Enumeration<? extends FileObject> en = fo.getChildren(true);
        while (en.hasMoreElements()) {
            FileObject data = en.nextElement();
            if (data.isFolder()) {
                continue;
            }
            String s = data.asText();
            if (s.contains("#if")) {
                fail("Wrong content of " + data +":\n" + s);
            }
        }
    }

    public void testBuildZip() throws Exception {
        int ret = runAntTargetsOnSample(new String[]{"build-zip"});
        assertEquals("build-zipant target should return zero - build successful", 0, ret);
        File dist = new File(sampleFolder, "dist");
        File zipFile = new File(dist, "osgisample.zip");
        assertTrue("ZIP file should be in dist folder", zipFile.exists());
    }

    public void testBuild() throws Exception {
        int ret = runAntTargetsOnSample(new String[] {"build"});
        assertEquals("build ant target should return zero - build successful", 0 , ret);
        File buildFolder = new File(sampleFolder,"build");
        assertTrue("build folder should exist", buildFolder.exists() && buildFolder.isDirectory());
        String[] children = buildFolder.list();
        assertTrue("build folder is not empty", children.length>0);

    }

    public void testBuildNBMs() throws Exception {
        int ret = runAntTargetsOnSample(new String[] {"nbms"});
        assertEquals("build ant target should return zero - build successful", 0 , ret);
        File buildFolder = new File(sampleFolder,"build");
        File updatesFolder = new File(buildFolder,"updates");
        assertTrue("build/update folder exists", updatesFolder.exists() && updatesFolder.isDirectory());
        File showBundles = new File(updatesFolder, "org-netbeans-demo-osgi-showbundles.jar");
        assertTrue("Our NBM is in build/updates folder", showBundles.exists());
        assertEquals("1 nbm is in build/updates folder", 1, updatesFolder.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(".+[.](nbm|jar)$");
            }
        }).length);
    }

    public void testTest() throws Exception {
        int ret = runAntTargetsOnSample(new String[]{"test"});
        assertEquals("test ant target should return zero - build successful", 0, ret);
        FileObject fo = FileUtil.toFileObject(sampleFolder);
        FileObject result = fo.getFileObject("showbundles/build/test/unit/results/TEST-org.netbeans.demo.osgi.showbundles.InstallerTest.xml");
        assertNotNull("Test result found", result);
        final String out = result.asText("UTF-8");
        if (!out.contains("testsuite errors=\"0\" failures=\"0\"")) {
            fail("Expecting no errors and failures:\n" + out);
        }
    }

    public void testClean() throws Exception {
        int ret = runAntTargetsOnSample(new String[]{"clean"});
        assertEquals("clean ant target should return zero - build successful", 0, ret);
        File buildFolder = new File(sampleFolder, "build");
        File distFolder = new File(sampleFolder, "dist");
        assertTrue("build and dist folders are deleted", !distFolder.exists() && !buildFolder.exists());
    }
}

